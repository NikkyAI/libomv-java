/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Portions Copyright (c) 2006, Lateral Arts Limited
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.assets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.nio.concurrent.FutureCallback;

import libomv.DownloadManager;
import libomv.DownloadManager.DownloadResult;
import libomv.GridClient;
import libomv.LibSettings;
import libomv.Simulator;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDMap;
import libomv.assets.AssetItem.AssetType;
import libomv.assets.TexturePipeline.TextureRequestState;
import libomv.capabilities.CapsClient;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.UploadBakedTextureMessage;
import libomv.capabilities.CapsMessage.UploaderRequestComplete;
import libomv.capabilities.CapsMessage.UploaderRequestUpload;
import libomv.imaging.ManagedImage.ImageCodec;
import libomv.inventory.InventoryItem;
import libomv.packets.AbortXferPacket;
import libomv.packets.AssetUploadCompletePacket;
import libomv.packets.AssetUploadRequestPacket;
import libomv.packets.ConfirmXferPacketPacket;
import libomv.packets.InitiateDownloadPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.RequestXferPacket;
import libomv.packets.SendXferPacketPacket;
import libomv.packets.TransferAbortPacket;
import libomv.packets.TransferInfoPacket;
import libomv.packets.TransferPacketPacket;
import libomv.packets.TransferRequestPacket;
import libomv.types.UUID;
import libomv.types.PacketCallback;
import libomv.utils.CallbackArgs;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.RefObject;
import libomv.utils.TimeoutEvent;

// Summary description for AssetManager.
public class AssetManager implements PacketCallback
{
	// #region Enums
	public enum EstateAssetType
	{
		None, Covenant;

		public static EstateAssetType setValue(int value)
		{
			return values()[value + 1];
		}

		public static byte getValue(EstateAssetType value)
		{
			return (byte) (value.ordinal() - 1);
		}

		public byte getValue()
		{
			return (byte) (ordinal() - 1);
		}
	}

	public enum StatusCode
	{
		OK(0),
		// Transfer completed
		Done(1), Skip(2), Abort(3),
		// Unknown error occurred
		Error(-1),
		// Equivalent to a 404 error
		UnknownSource(-2),
		// Client does not have permission for that resource
		InsufficientPermissions(-3),
		// Unknown status
		Unknown(-4);

		public static StatusCode setValue(int value)
		{
			for (StatusCode e : values())
			{
				if (e._value == value)
					return e;
			}
			return Unknown;
		}

		public static byte getValue(StatusCode value)
		{
			for (StatusCode e : values())
			{
				if (e == value)
					return e._value;
			}
			return Unknown._value;
		}

		public byte getValue()
		{
			return _value;
		}

		private final byte _value;

		private StatusCode(int value)
		{
			_value = (byte) value;
		}
	}

	public enum ChannelType
	{
		Unknown,
		// Unknown
		Misc,
		// Virtually all asset transfers use this channel
		Asset;

		public static ChannelType setValue(int value)
		{
			return values()[value];
		}

		public static byte getValue(ChannelType value)
		{
			return (byte)value.ordinal();
		}

		public byte getValue()
		{
			return (byte)ordinal();
		}
	}

	public enum SourceType
	{
		//
		Unknown,
		//
		Unused,
		// Asset from the asset server
		Asset,
		// Inventory item
		SimInventoryItem,
		// Estate asset, such as an estate covenant
		SimEstate;

		public static SourceType setValue(int value)
		{
			return values()[value];
		}

		public static byte getValue(SourceType value)
		{
			return (byte)value.ordinal();
		}

		public byte getValue()
		{
			return (byte)ordinal();
		}
	}

	public enum TargetType
	{
		Unknown, File, VFile;

		public static TargetType setValue(int value)
		{
			return values()[value];
		}

		public static byte getValue(TargetType value)
		{
			return (byte)value.ordinal();
		}

		public byte getValue()
		{
			return (byte)ordinal();
		}
	}

	// When requesting image download, type of the image requested
	public enum ImageType
	{
		// Normal in-world object texture
		Normal,
		// Local baked avatar texture
		Baked,
		// Server baked avatar texture
		ServerBaked;

		public static ImageType setValue(int value)
		{
			return values()[value];
		}

		public static byte getValue(ImageType value)
		{
			return (byte)value.ordinal();
		}

		public byte getValue()
		{
			return (byte)ordinal();
		}
	}

	public enum TransferError
	{
		None(0), Failed(-1), AssetNotFound(-3), AssetNotFoundInDatabase(-4), InsufficientPermissions(-5), EOF(-39), CannotOpenFile(
				-42), FileNotFound(-43), FileIsEmpty(-44), TCPTimeout(-23016), CircuitGone(-23017);

		public static TransferError setValue(int value)
		{
			for (TransferError e : values())
			{
				if (e._value == value)
					return e;
			}
			return Failed;
		}

		public static byte getValue(TransferError value)
		{
			for (TransferError e : values())
			{
				if (e == value)
					return e._value;
			}
			return Failed._value;
		}

		public byte getValue()
		{
			return _value;
		}

		private final byte _value;

		private TransferError(int value)
		{
			_value = (byte) value;
		}
	}

	// #endregion Enums

	// #region Transfer Classes
	protected class DelayedTransfer
	{
		public StatusCode Status;
		public byte[] Data;
		
		public DelayedTransfer(StatusCode status, byte[] data)
		{
			this.Status = status;
			this.Data = data;
		}
	}
	
	public class Transfer
	{
		public UUID ItemID;
		public int Size;
		public AssetType AssetType;
		public byte[] AssetData;

		protected UUID TransactionID;
		protected int Transferred;
		protected int PacketNum;
		public boolean Success;
		protected long TimeSinceLastPacket;
		protected HashMap<Integer, DelayedTransfer> delayed;
		public String suffix;

		public Transfer()
		{
			AssetData = Helpers.EmptyBytes;
			delayed = new HashMap<Integer, DelayedTransfer>();
		}
	}

	public class XferDownload extends Transfer
	{
		public long XferID;
		public String Filename = Helpers.EmptyString;
		public TransferError Error = TransferError.None;

		public XferDownload()
		{
			super();
		}
	}

	public class AssetDownload extends Transfer
	{
		public ChannelType Channel;
		public SourceType Source;
		public TargetType Target;
		public StatusCode Status;
		public float Priority;
		private Simulator Simulator;
		private CallbackHandler<AssetDownload> callbacks;
	
		public AssetDownload()
		{
			super();
		}
		
		public boolean gotInfo()
		{
			return Size > 0;
		}
	}

	public class ImageDownload extends Transfer
	{
		public ImageType ImageType;
		public ImageCodec Codec;
		public int DiscardLevel;
		public float Priority;
		// The current {@link TextureRequestState} which identifies the current
		// status of the request
		public TextureRequestState State;
		// If true, indicates the callback will be fired whenever new data is
		// returned from the simulator.
		// This is used to progressively render textures as portions of the
		// texture are received.
		public boolean ReportProgress;
		// The callback to fire when the request is complete, will include
		// the {@link TextureRequestState} and the <see cref="AssetTexture"/>
		// object containing the result data
		public CallbackHandler<ImageDownload> callbacks;
		
		public ImageDownload()
		{
			super();
		}

		public boolean gotInfo()
		{
			return Size > 0;
		}
	}

	public class MeshDownload extends Transfer
	{
		public UUID ItemID;
		private CallbackHandler<MeshDownload> callbacks;

		public MeshDownload()
		{
			super();
		}
	}
	
	public class AssetUpload extends Transfer
	{
		public UUID AssetID;
		public long XferID;

		public AssetUpload()
		{
			super();
		}
	}

	public class ImageRequest
	{
		public UUID ImageID;
		public ImageType Type;
		public float Priority;
		public int DiscardLevel;

		public ImageRequest(UUID imageid, ImageType type, float priority, int discardLevel)
		{
			ImageID = imageid;
			Type = type;
			Priority = priority;
			DiscardLevel = discardLevel;
		}

	}

	// #endregion Transfer Classes

	// #region Callbacks

	/**
	 * Callback used upon completion of baked texture upload
	 * 
	 * @param newAssetID
	 *            Asset UUID of the newly uploaded baked texture
	 */
	public abstract class BakedTextureUploadedCallback
	{
		abstract public void callback(UUID newAssetID);
	}

	// #endregion Callback

	// #region Callback

	public CallbackHandler<XferDownload> OnXferReceived = new CallbackHandler<XferDownload>();

	private CallbackHandler<AssetUpload> OnAssetUploaded = new CallbackHandler<AssetUpload>();
	private CallbackHandler<AssetUpload> OnUploadProgress = new CallbackHandler<AssetUpload>();

	// Provides data for InitiateDownloaded event
	public class InitiateDownloadCallbackArgs implements CallbackArgs
	{
		private final String m_SimFileName;
		private final String m_ViewerFileName;

		// Filename used on the simulator
		public final String getSimFileName()
		{
			return m_SimFileName;
		}

		// Filename used by the client
		public final String getViewerFileName()
		{
			return m_ViewerFileName;
		}

		public InitiateDownloadCallbackArgs(String simFilename, String viewerFilename)
		{
			this.m_SimFileName = simFilename;
			this.m_ViewerFileName = viewerFilename;
		}
	}

	private CallbackHandler<InitiateDownloadCallbackArgs> OnInitiateDownload = new CallbackHandler<InitiateDownloadCallbackArgs>();

	// Provides data for ImageReceiveProgress event
	public class ImageReceiveProgressCallbackArgs implements CallbackArgs
	{
		private final UUID m_ImageID;
		private final long m_Received;
		private final long m_Total;

		// UUID of the image that is in progress
		public final UUID getImageID()
		{
			return m_ImageID;
		}

		// Number of bytes received so far
		public final long getReceived()
		{
			return m_Received;
		}

		// Image size in bytes
		public final long getTotal()
		{
			return m_Total;
		}

		public ImageReceiveProgressCallbackArgs(UUID imageID, long received, long total)
		{
			this.m_ImageID = imageID;
			this.m_Received = received;
			this.m_Total = total;
		}
	}

	private CallbackHandler<ImageReceiveProgressCallbackArgs> OnImageReceiveProgress = new CallbackHandler<ImageReceiveProgressCallbackArgs>();

	// #endregion Events

	// Texture download cache
	private AssetCache _Cache;

	public AssetCache getCache()
	{
		return _Cache;
	}

	private TexturePipeline _TexDownloads;

    private DownloadManager _HttpDownloads;

    private GridClient _Client;

    /* Transfers based on the asset ID to maintain an overview of currently active transfers */
	private HashMap<UUID, Transfer> _ActiveDownloads;

	/* Transfers based on the transaction ID used by the old transfer system */
	private HashMap<UUID, Transfer> _AssetTransfers;

	/* Transfers based on the transaction ID used by the even older xfer system */
	private HashMap<Long, Transfer> _XferTransfers;

	private ExecutorService _ThreadPool;
	private Future<?> _ThreadResult = null;

	private BlockingQueue<AssetUpload> _PendingUpload;

	/*
	 * Default constructor
	 * 
	 * @param client A reference to the GridClient object
	 */
	public AssetManager(GridClient client)
	{
		_Client = client;
		_Cache = new AssetCache(client);
		
		_XferTransfers = new HashMap<Long, Transfer>();
		_PendingUpload = new ArrayBlockingQueue<AssetUpload>(1);
				
		_AssetTransfers = new HashMap<UUID, Transfer>();
		_ActiveDownloads = new HashMap<UUID, Transfer>();
		
		_TexDownloads = new TexturePipeline(client, _Cache);

		_ThreadPool = Executors.newSingleThreadExecutor();

		_HttpDownloads = new DownloadManager();
			
		// Transfer packets for downloading large assets
		_Client.Network.RegisterCallback(PacketType.TransferInfo, this);
		_Client.Network.RegisterCallback(PacketType.TransferPacket, this);

		// Xfer packets for uploading large assets
		_Client.Network.RegisterCallback(PacketType.RequestXfer, this);
		_Client.Network.RegisterCallback(PacketType.ConfirmXferPacket, this);
		_Client.Network.RegisterCallback(PacketType.AssetUploadComplete, this);

		// Xfer packets for downloading misc assets
		_Client.Network.RegisterCallback(PacketType.SendXferPacket, this);
		_Client.Network.RegisterCallback(PacketType.AbortXfer, this);

		// Simulator is responding to a request to download a file
		_Client.Network.RegisterCallback(PacketType.InitiateDownload, this);
	}

	@Override
	protected void finalize()
	{
		_Client.Network.UnregisterCallback(PacketType.TransferInfo, this);
		_Client.Network.UnregisterCallback(PacketType.TransferPacket, this);

		// Xfer packets for uploading large assets
		_Client.Network.UnregisterCallback(PacketType.RequestXfer, this);
		_Client.Network.UnregisterCallback(PacketType.ConfirmXferPacket, this);
		_Client.Network.UnregisterCallback(PacketType.AssetUploadComplete, this);

		// Xfer packets for downloading misc assets
		_Client.Network.UnregisterCallback(PacketType.SendXferPacket, this);
		_Client.Network.UnregisterCallback(PacketType.AbortXfer, this);

		// Simulator is responding to a request to download a file
		_Client.Network.UnregisterCallback(PacketType.InitiateDownload, this);
		
		_TexDownloads.shutdown();
		_TexDownloads = null;

		_ActiveDownloads = null;
		_AssetTransfers = null;
		_PendingUpload = null;
		_XferTransfers = null;

		if (_ThreadResult != null)
			_ThreadResult.cancel(true);
		_ThreadPool.shutdownNow();

		_HttpDownloads.shutdown();

		// Transfer packets for downloading large assets
		_Client = null;
		_Cache = null;
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case TransferInfo:
				HandleTransferInfo(packet, simulator);
				break;
			case TransferPacket:
				HandleTransferPacket(packet, simulator);
				break;
			case RequestXfer:
				HandleRequestXfer(packet, simulator);
				break;
			case ConfirmXferPacket:
				HandleConfirmXferPacket(packet, simulator);
				break;
			case AssetUploadComplete:
				HandleAssetUploadComplete(packet, simulator);
				break;
			case SendXferPacket:
				HandleSendXferPacket(packet, simulator);
				break;
			case AbortXfer:
				HandleAbortXfer(packet, simulator);
				break;
			case InitiateDownload:
				HandleInitiateDownloadPacket(packet, simulator);
				break;
			default:
				break;
		}
	}

	/**
	 * Request an asset download through the almost deprecated Xfer system
	 *
	 * @param filename Filename of the asset to request
	 * @param deleteOnCompletion Whether or not to delete the asset off the server after it is
	 *            retrieved
	 * @param useBigPackets Use large transfer packets or not
	 * @param vFileID UUID of the file to request, if filename is left empty
	 * @param vFileType Asset type of <code>vFileID</code>, or
	 *            <code>AssetType.Unknown</code> if filename is not empty
	 * @param fromCache Sets the FilePath in the request to Cache (4) if true,
	 *            otherwise Unknown (0) is used
	 * @return The transaction ID that this Asset download identifies
	 * @throws Exception
	 */
	public long RequestAssetXfer(String filename, boolean deleteOnCompletion, boolean useBigPackets,
			UUID vFileID, AssetType vFileType, boolean fromCache) throws Exception
	{
		XferDownload transfer = new XferDownload();
		transfer.XferID = new UUID().AsLong();
		transfer.Filename = filename;
		transfer.ItemID = vFileID;
		transfer.AssetType = vFileType;

		// Add this transfer to the dictionary
		synchronized (_XferTransfers)
		{
			_XferTransfers.put(transfer.XferID, transfer);
		}

		RequestXferPacket request = new RequestXferPacket();
		request.XferID.ID = transfer.XferID;
		request.XferID.setFilename(Helpers.StringToBytes(filename));
		request.XferID.FilePath = fromCache ? (byte) 4 : (byte) 0;
		request.XferID.DeleteOnCompletion = deleteOnCompletion;
		request.XferID.UseBigPackets = useBigPackets;
		request.XferID.VFileID = vFileID;
		request.XferID.VFileType = vFileType.getValue();

		_Client.Network.sendPacket(request);

		return transfer.XferID;
	}

	/**
	 * Request an asset download
	 *
	 * @param assetID Asset UUID
	 * @param type Asset type, must be correct for the transfer to succeed
	 * @param priority Whether to give this transfer an elevated priority
	 * @param callback The callback to fire when the simulator responds with the
	 *            asset data
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID RequestAsset(UUID assetID, AssetType type, boolean priority, Callback<AssetDownload> callback)
			throws Exception
	{
		return RequestAsset(assetID, type, priority, SourceType.Asset, callback);
	}

	/**
	 * Request an asset download
	 *
	 * @param assetID Asset UUID
	 * @param type Asset type, must be correct for the transfer to succeed
	 * @param priority Whether to give this transfer an elevated priority
	 * @param sourceType Source location of the requested asset
	 * @param callback The callback to fire when the simulator responds with the
	 *            asset data
	 * @return The transaction ID that this asset download identifies
	 * 
	 * @throws Exception
	 */
	public UUID RequestAsset(UUID assetID, AssetType type, boolean priority, SourceType sourceType, Callback<AssetDownload> callback) throws Exception
	{
		return RequestAsset(assetID, UUID.Zero, UUID.Zero, type, priority, sourceType, null, callback);
    }

	/**
	 * Request an asset download
	 *
	 * @param assetID Asset UUID
	 * @param type Asset type, must be correct for the transfer to succeed
	 * @param priority Whether to give this transfer an elevated priority
	 * @param sourceType Source location of the requested asset
	 * @param transactionID UUID of the transaction
	 * @param callback The callback to fire when the simulator responds with the asset data
	 * @return The transaction ID that this asset download identifies
	 *
	 * @throws Exception
	 */
     public UUID RequestAsset(UUID assetID, UUID itemID, UUID taskID, AssetType type, boolean priority, SourceType sourceType, UUID transactionID, Callback<AssetDownload> callback) throws Exception
     {
		Simulator simulator = _Client.Network.getCurrentSim();

		// Build the request packet and send it
		TransferRequestPacket request = CheckAssetCache(assetID, type, transactionID, priority, sourceType, simulator, callback, "asset");
		if (request != null)
		{		
			byte[] paramField = UUID.isZeroOrNull(taskID) ? new byte[20] : new byte[96];
			assetID.toBytes(paramField, 0);
			System.arraycopy(Helpers.Int32ToBytesL(type.getValue()), 0, paramField, 16, 4);
			if (!UUID.isZeroOrNull(taskID))
			{
				taskID.toBytes(paramField, 48);
				itemID.toBytes(paramField, 64);
				assetID.toBytes(paramField, 80);
            }
			request.TransferInfo.setParams(paramField);

			simulator.sendPacket(request);
			return request.TransferInfo.TransferID;
		}
		return null; 
	}

	/**
	 * Abort an asset download
	 *
	 * @param transactionID The transaction ID of the asset download to abort

	 * @throws Exception
	 */
	public boolean AbortAssetTransfer(UUID transactionID) throws Exception
	{
		AssetDownload download;
		synchronized (_ActiveDownloads)
		{
			download = (AssetDownload)_AssetTransfers.remove(transactionID);
			if (download != null)
				_ActiveDownloads.remove(download.ItemID);
		}
		
		if (download != null)
		{
			// Abort the transfer
			TransferAbortPacket abort = new TransferAbortPacket();
			abort.TransferInfo.ChannelType = download.Channel.getValue();
			abort.TransferInfo.TransferID = download.TransactionID;
			download.Simulator.sendPacket(abort);

			download.Success = false;

			// Fire the event with our transfer that contains Success = false
			if (download.callbacks != null)
			{
				download.Status = StatusCode.Abort;
				download.callbacks.dispatch(download);
			}
			return true;
		}
		return false;
	}

	/**
	 * Request an asset download for the inventory
	 *
	 * @param item Use UUID.Zero if you do not have the asset ID but have all the
	 *            necessary permissions
	 * @param priority Whether to prioritize this asset download or not
	 * @param callback The callback to fire when the simulator responds with the
	 *            asset data
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID RequestInventoryAsset(InventoryItem item, boolean priority, Callback<AssetDownload> callback)
			throws Exception
	{
		return RequestInventoryAsset(item.assetID, item.itemID, UUID.Zero, item.getOwnerID(), item.assetType, priority, callback);
	}
	
	/**
	 * Request an asset download for the inventory
	 *
	 * @param assetID Use UUID.Zero if you do not have the asset ID but have all the
	 *            necessary permissions
	 * @param itemID The item ID of this asset in the inventory
	 * @param taskID Use UUID.Zero if you are not requesting an asset from an
	 *            object inventory
	 * @param ownerID The owner of this asset
	 * @param type Asset type
	 * @param priority Whether to prioritize this asset download or not
	 * @param callback The callback to fire when the simulator responds with the
	 *            asset data
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID RequestInventoryAsset(UUID assetID, UUID itemID, UUID taskID, UUID ownerID, AssetType type,
			boolean priority, Callback<AssetDownload> callback) throws Exception
	{
		Simulator simulator = _Client.Network.getCurrentSim();

		// Build the request packet and send it
		TransferRequestPacket request = CheckAssetCache(assetID, type, null, priority, SourceType.SimInventoryItem, simulator, callback, "asset");
		if (request != null)
		{		
			byte[] paramField = new byte[100];
			_Client.Self.getAgentID().toBytes(paramField, 0);
			_Client.Self.getSessionID().toBytes(paramField, 16);
			ownerID.toBytes(paramField, 32);
			taskID.toBytes(paramField, 48);
			itemID.toBytes(paramField, 64);
			assetID.toBytes(paramField, 80);
			Helpers.Int32ToBytesL(type.getValue(), paramField, 96);
			request.TransferInfo.setParams(paramField);

			simulator.sendPacket(request);
			return request.TransferInfo.TransferID;
		}
		return null; 
	}

	/**
	 * Request an asset download for estates
	 *
	 * @param assetID Use UUID.Zero if you do not have the asset ID but have all the
	 *            necessary permissions
	 * @param type Asset type
	 * @param priority Whether to prioritize this asset download or not
	 * @param callback The callback to fire when the simulator responds with the
	 *            asset data
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID RequestEstateAsset(UUID assetID, AssetType type, boolean priority, Callback<AssetDownload> callback) throws Exception
	{
		Simulator simulator = _Client.Network.getCurrentSim();

		// Build the request packet and send it
		TransferRequestPacket request = CheckAssetCache(assetID, type, null, priority, SourceType.SimEstate, simulator, callback, "asset");
		if (request != null)
		{		
			byte[] paramField = new byte[36];
			_Client.Self.getAgentID().toBytes(paramField, 0);
			_Client.Self.getSessionID().toBytes(paramField, 16);
			Helpers.Int32ToBytesL(type.getValue(), paramField, 32);
			request.TransferInfo.setParams(paramField);

			simulator.sendPacket(request);
			return request.TransferInfo.TransferID;
		}
		return null; 
	}

	private TransferRequestPacket CheckAssetCache(UUID assetID, AssetType type, UUID transactionID, boolean priority, SourceType sourceType, Simulator simulator, Callback<AssetDownload> callback, String suffix)
	{
		AssetDownload transfer;
		// Check asset cache first
		byte[] data = _Cache.get(assetID, suffix);
		if (data != null)
		{
			// Is caller interested to get a callback?
			if (callback != null)
			{
				transfer = new AssetDownload();
				transfer.ItemID = assetID;
				transfer.AssetData = data;
				transfer.AssetType = type;
				transfer.Success = true;
				transfer.Status = StatusCode.OK;

				try
				{
					callback.callback(transfer);
				}
				catch (Throwable ex)
				{
					Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
				}
			}
			return null;
		}
		
		/* If we already have this asset requested and in the download queue just add the new callback to this request */
		synchronized (_ActiveDownloads)
		{
			transfer = (AssetDownload)_ActiveDownloads.get(assetID);
		}
		if (transfer != null)
		{
			transfer.callbacks.add(callback);
			return null;
		}
		
		// Add this transfer to the dictionary
		transfer = new AssetDownload();
		transfer.TransactionID = UUID.isZeroOrNull(transactionID) ? new UUID() : transactionID;
		transfer.ItemID = assetID;
		// transfer.AssetType = type; // Set again in TransferInfoHandler.
		transfer.Priority = 100.0f + (priority ? 1.0f : 0.0f);
		transfer.Channel = ChannelType.Asset;
		transfer.Source = sourceType;
		transfer.Simulator = simulator;
		transfer.suffix = suffix;
		transfer.callbacks = new CallbackHandler<AssetDownload>();
		transfer.callbacks.add(callback);

		synchronized (_ActiveDownloads)
		{
			_AssetTransfers.put(transfer.TransactionID, transfer);
			_ActiveDownloads.put(assetID, transfer);
		}

		// Build the request packet and send it
		TransferRequestPacket request = new TransferRequestPacket();
		request.TransferInfo.ChannelType = transfer.Channel.getValue();
		request.TransferInfo.Priority = transfer.Priority;
		request.TransferInfo.SourceType = sourceType.getValue();
		request.TransferInfo.TransferID = transfer.TransactionID;
		return request;
	}
	
	/**
	 * Request an asset be uploaded to the simulator
	 *
	 * @param asset The {@link Asset} Object containing the asset data
	 * @param storeLocal If True, the asset once uploaded will be stored on the
	 *            simulator in which the client was connected in addition to
	 *            being stored on the asset server
	 * @return The transaction ID that this asset upload identifies
	 * @throws Exception
	 */
	public UUID RequestUpload(AssetItem asset, boolean storeLocal) throws Exception
	{
		if (asset.AssetData == null)
		{
			throw new IllegalArgumentException("Can't upload an asset with no data (did you forget to call Encode?)");
		}
		return RequestUpload(null, asset.getAssetType(), asset.AssetData, storeLocal, new UUID());
	}

	/**
	 * Request an asset be uploaded to the simulator
	 *
	 * @param type The {@link AssetType} of the asset being uploaded
	 * @param data A byte array containing the encoded asset data
	 * @param storeLocal If True, the asset once uploaded will be stored on the
	 *            simulator in which the client was connected in addition to
	 *            being stored on the asset server
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID RequestUpload(AssetType type, byte[] data, boolean storeLocal) throws Exception
	{
		return RequestUpload(null, type, data, storeLocal, new UUID());
	}

	/**
	 * Request an asset be uploaded to the simulator
	 *
	 * @param assetID
	 * @param type Asset type to upload this data as
	 * @param data A byte array containing the encoded asset data
	 * @param storeLocal If True, the asset once uploaded will be stored on the
	 *            simulator in which the client was connected in addition to
	 *            being stored on the asset server
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID RequestUpload(RefObject<UUID> assetID, AssetType type, byte[] data, boolean storeLocal)
			throws Exception
	{
		return RequestUpload(assetID, type, data, storeLocal, new UUID());
	}
	
	/**
	 * Request an asset be uploaded to the simulator
	 *
	 * @param assetID
	 * @param type Asset type to upload this data as
	 * @param data A byte array containing the encoded asset data
	 * @param storeLocal If True, the asset once uploaded will be stored on the
	 *            simulator in which the client was connected in addition to
	 *            being stored on the asset server
	 * @param transactionID The unique transaction ID that this asset download identifies
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	private UUID RequestUpload(RefObject<UUID> assetID, AssetType type, byte[] data, boolean storeLocal, UUID transactionID)
			throws Exception
	{
		AssetUpload upload = new AssetUpload();
		/* Create a new asset ID for this asset */
		upload.AssetID = UUID.Combine(transactionID, _Client.Self.getSecureSessionID());
		if (assetID != null)
			assetID.argvalue = upload.AssetID;
		upload.AssetData = data;
		upload.AssetType = type;
		upload.Size = data.length;
		upload.XferID = 0;
		upload.TransactionID = transactionID;

		// Build and send the upload packet
		AssetUploadRequestPacket request = new AssetUploadRequestPacket();
		request.AssetBlock.StoreLocal = storeLocal;
		request.AssetBlock.Tempfile = false; // This field is deprecated
		request.AssetBlock.TransactionID = transactionID;
		request.AssetBlock.Type = type.getValue();

		if (data.length + 100 < LibSettings.MAX_PACKET_SIZE)
		{
			Logger.Log(String.format("Beginning asset upload [Single Packet], ID: %s, AssetID: %s, Size: %d",
					upload.TransactionID, upload.AssetID, upload.Size), LogLevel.Info, _Client);

			synchronized (_ActiveDownloads) 
			{
				_AssetTransfers.put(transactionID, upload);
			}

			// The whole asset will fit in this packet, makes things easy
			request.AssetBlock.setAssetData(data);
			upload.Transferred = data.length;
		}
		else
		{
			Logger.Log(String.format("Beginning asset upload [Multiple Packets], ID: %s, AssetID: %s, Size: %d",
					upload.TransactionID, upload.AssetID, upload.Size), LogLevel.Info, _Client);

			// Asset is too big, send in multiple packets
			request.AssetBlock.setAssetData(Helpers.EmptyBytes);

			// Wait for the previous upload to receive a RequestXferPacket
			final int UPLOAD_CONFIRM_TIMEOUT = 20 * 1000;
			if (!_PendingUpload.offer(upload, UPLOAD_CONFIRM_TIMEOUT, TimeUnit.MILLISECONDS))
			{
				throw new Exception("Timeout waiting for previous asset upload to begin");
			}
		}
			
		_Client.Network.sendPacket(request);
		return upload.TransactionID;
	}

	public void RequestUploadBakedTexture(final byte[] textureData, final BakedTextureUploadedCallback callback)
			throws IOException
	{
		URI url = _Client.Network.getCapabilityURI(CapsEventType.UploadBakedTexture.toString());
		if (url != null)
		{
			// Fetch the uploader capability
			CapsClient request = new CapsClient(_Client, CapsEventType.UploadBakedTexture.toString());

			class RequestUploadBakedTextureComplete implements FutureCallback<OSD>
			{
				@Override
				public void completed(OSD result)
				{
					if (result instanceof OSDMap)
					{
						UploadBakedTextureMessage message = _Client.Messages.new UploadBakedTextureMessage();
						message.Deserialize((OSDMap) result);
						if (message.Request.State.equals("complete"))
						{
							callback.callback(((UploaderRequestComplete) message.Request).AssetID);
							return;
						}
						else if (message.Request.State.equals("upload"))
						{
							URI uploadUrl = ((UploaderRequestUpload) message.Request).Url;
							if (uploadUrl != null)
							{
								try
								{
									// POST the asset data
									CapsClient upload = new CapsClient(_Client, CapsEventType.UploadBakedTexture.toString());
									upload.executeHttpPost(uploadUrl, textureData, "application/octet-stream", null,
											new RequestUploadBakedTextureComplete(), _Client.Settings.CAPS_TIMEOUT);
								}
								catch (IOException ex)
								{
									Logger.Log("Bake upload failed", LogLevel.Warning, _Client);
									callback.callback(UUID.Zero);
								}
							}
							return;
						}
					}
					Logger.Log("Bake upload failed", LogLevel.Warning, _Client);
					callback.callback(UUID.Zero);
				}

				@Override
				public void cancelled()
				{
					Logger.Log("Bake upload canelled", LogLevel.Warning, _Client);
					callback.callback(UUID.Zero);
				}

				@Override
				public void failed(Exception ex)
				{
					Logger.Log("Bake upload failed", LogLevel.Warning, _Client, ex);
					callback.callback(UUID.Zero);
				}
			}
			request.executeHttpPost(url, new OSDMap(), OSDFormat.Xml, new RequestUploadBakedTextureComplete(), _Client.Settings.CAPS_TIMEOUT);
		}
		else
		{
			Logger.Log("UploadBakedTexture not available, falling back to UDP method", LogLevel.Info, _Client);

			_ThreadResult = _ThreadPool.submit(new Runnable()
			{
				@Override
				public void run()
				{
					final UUID transactionID = new UUID();
					final TimeoutEvent<Boolean> uploadEvent = new TimeoutEvent<Boolean>();

					Callback<AssetUpload> udpCallback = new Callback<AssetUpload>()
					{
						@Override
						public boolean callback(AssetUpload e)
						{
							if (transactionID.equals(e.TransactionID))
							{
								uploadEvent.set(true);
								callback.callback(e.Success ? e.AssetID : UUID.Zero);
							}
							return false;
						}
					};

					OnAssetUploaded.add(udpCallback);
					Boolean success;
					try
					{
						RequestUpload(null, AssetType.Texture, textureData, true, transactionID);
						success = uploadEvent.waitOne(_Client.Settings.TRANSFER_TIMEOUT);
					}
					catch (Exception t)
					{
						success = false;
					}
					OnAssetUploaded.remove(udpCallback);
					if (success == null || !success)
					{
						callback.callback(UUID.Zero);
					}
				}
			});
		}
	}

	/**
	 * Request a texture asset from the simulator using the <see
	 * cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 *
	 * @param textureID The <see cref="UUID"/> of the texture asset to download
	 * @param imageType The <see cref="ImageType"/> of the texture asset. Use <see
	 *            cref="ImageType.Normal"/> for most textures, or <see
	 *            cref="ImageType.Baked"/> for baked layer texture assets
	 * @param priority A float indicating the requested priority for the transfer.
	 *            Higher priority values tell the simulator to prioritize the
	 *            request before lower valued requests. An image already being
	 *            transferred using the <see cref="TexturePipeline"/> can have
	 *            its priority changed by resending the request with the new
	 *            priority value
	 * @param discardLevel Number of quality layers to discard. This controls the end
	 *            marker of the data sent. Sending with value -1 combined with
	 *            priority of 0 cancels an in-progress transfer. A bug exists in
	 *            the Linden Simulator where a -1 will occasionally be sent with
	 *            a non-zero priority indicating an off-by-one error.
	 * @param packetStart The packet number to begin the request at. A value of 0 begins
	 *            the request from the start of the asset texture
	 * @param callback The <see cref="TextureDownloadCallback"/> callback to fire
	 *            when the image is retrieved. The callback will contain the
	 *            result of the request and the texture asset data
	 * @param progress If true, the callback will be fired for each chunk of the
	 *            downloaded image. The callback asset parameter will contain
	 *            all previously received chunks of the texture asset starting
	 *            from the beginning of the request
	 * @returns true if the request could be satisfied from cache or sent successfully
	 */
	public boolean RequestImage(UUID textureID, ImageType imageType, float priority, int discardLevel,
			int packetStart, Callback<ImageDownload> callback, boolean progress)
	{
		if (UUID.isZeroOrNull(textureID) || callback == null)
			return false;

		ImageDownload download = null;
		
		/* Check if we have it already in our cache */
		byte[] assetData = _Cache.get(textureID, "tex");
		if (assetData != null)
		{
			if (callback != null)
			{
				download = new ImageDownload();
				download.ItemID = textureID;
				download.AssetData = assetData;
				download.ImageType = imageType;
				download.State = TextureRequestState.Finished;
				callback.callback(download);
			}
			if (progress)
				_Client.Assets.FireImageProgressEvent(textureID, assetData.length, assetData.length);
			return true;
		}

		synchronized (_ActiveDownloads)
		{
			download = (ImageDownload)_ActiveDownloads.get(textureID);
			if (download != null)
			{
				download.callbacks.add(callback);
				return true;
			}
			download = new ImageDownload();
			download.ItemID = textureID;
			download.AssetData = assetData;
			download.ImageType = imageType;
			download.State = TextureRequestState.Started;
			download.Priority = priority;
			download.DiscardLevel = discardLevel;
			download.ReportProgress = progress;
			download.suffix = "tex";
			download.callbacks = new CallbackHandler<ImageDownload>();
			download.callbacks.add(callback);
			_ActiveDownloads.put(textureID, download);
		}
		
		boolean sent = false;
		if (_Client.Settings.getBool(LibSettings.USE_HTTP_TEXTURES) && _Client.Network.getCapabilityURI("GetTexture") != null)
		{
			sent = HttpRequestTexture(download);
		}

		if (!sent)
		{
			sent = _TexDownloads.RequestTexture(download);
		}
		return sent;
	}

	/**
	 * Overload: Request a texture asset from the simulator using the <see
	 * cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 *
	 * @param textureID The <see cref="UUID"/> of the texture asset to download
	 * @param callback The <see cref="TextureDownloadCallback"/> callback to fire
	 *            when the image is retrieved. The callback will contain the
	 *            result of the request and the texture asset data
	 */
	public boolean RequestImage(UUID textureID, Callback<ImageDownload> callback)
	{
		return RequestImage(textureID, ImageType.Normal, 101300.0f, 0, 0, callback, false);
	}

	/**
	 * Overload: Request a texture asset from the simulator using the <see
	 * cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 *
	 * @param textureID The <see cref="UUID"/> of the texture asset to download
	 * @param imageType The <see cref="ImageType"/> of the texture asset. Use <see
	 *            cref="ImageType.Normal"/> for most textures, or <see
	 *            cref="ImageType.Baked"/> for baked layer texture assets
	 * @param callback The <see cref="TextureDownloadCallback"/> callback to fire
	 *            when the image is retrieved. The callback will contain the
	 *            result of the request and the texture asset data
	 */
	public boolean RequestImage(UUID textureID, ImageType imageType, Callback<ImageDownload> callback)
	{
		return RequestImage(textureID, imageType, 101300.0f, 0, 0, callback, false);
	}

	/**
	 * Overload: Request a texture asset from the simulator using the <see
	 * cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 *
	 * @param textureID The <see cref="UUID"/> of the texture asset to download
	 * @param imageType The <see cref="ImageType"/> of the texture asset. Use <see
	 *            cref="ImageType.Normal"/> for most textures, or <see
	 *            cref="ImageType.Baked"/> for baked layer texture assets
	 * @param callback The <see cref="TextureDownloadCallback"/> callback to fire
	 *            when the image is retrieved. The callback will contain the
	 *            result of the request and the texture asset data
	 * @param progress If true, the callback will be fired for each chunk of the
	 *            downloaded image. The callback asset parameter will contain
	 *            all previously received chunks of the texture asset starting
	 *            from the beginning of the request
	 */
	public boolean RequestImage(UUID textureID, ImageType imageType, Callback<ImageDownload> callback, boolean progress)
	{
		return RequestImage(textureID, imageType, 101300.0f, 0, 0, callback, progress);
	}

	/**
	 * Cancel a texture request
	 *
	 * @param textureID The texture assets <see cref="UUID"/>
	 * @throws Exception
	 */
	public void RequestImageCancel(UUID textureID) throws Exception
	{
		synchronized (_ActiveDownloads)
		{
			_ActiveDownloads.remove(textureID);
		}
		_TexDownloads.AbortTextureRequest(textureID);
	}

	private class MeshDownloadCallback implements Callback<DownloadResult>
	{
		private MeshDownload download;
		
		public MeshDownloadCallback(MeshDownload download)
		{
			this.download = download;
		}
		
		@Override
		public boolean callback(DownloadResult result)
		{
			if (result.finished)
			{
				if (result.data != null) // success
				{
					download.AssetData = result.data;
				}
				download.callbacks.dispatch(download);
			}
			return result.finished;
		}
	}

	/**
	 * Requests download of a mesh asset
	 *
	 * @param meshID UUID of the mesh asset
	 * @param callback Callback when the request completes
	 */
	public boolean RequestMesh(final UUID meshID, Callback<MeshDownload> callback)
	{
		if (UUID.isZeroOrNull(meshID) || callback == null)
			return false;

		MeshDownload download = null;
		
		// Do we have this mesh asset in the cache?
		byte[] data = _Cache.get(meshID, "mesh");
		if (data != null)
		{
			download = new MeshDownload();
			download.ItemID = meshID;
			download.AssetType = AssetType.Mesh;
			download.AssetData = data;
			callback.callback(download);
			return true;
		}

		URI url = _Client.Network.getCapabilityURI("GetMesh");
		if (url != null)
		{
			synchronized (_ActiveDownloads)
			{
				download = (MeshDownload)_ActiveDownloads.get(meshID);
				if (download != null)
				{
					download.callbacks.add(callback);
					return true;
				}
				download = new MeshDownload();
				download.ItemID = meshID;
				download.suffix = "mesh";
				download.callbacks = new CallbackHandler<MeshDownload>();
				download.callbacks.add(callback);
				_ActiveDownloads.put(meshID, download);
			}

			try
			{
				url = new URI(String.format("%s/?mesh_id=%s", url, meshID));

				Callback<DownloadResult> downloadCallback = new MeshDownloadCallback(download);
				_HttpDownloads.enque(url, _Client.Settings.CAPS_TIMEOUT, null, _Cache.cachedAssetFile(download.ItemID, download.suffix), downloadCallback);
				return true;
			}
			catch (URISyntaxException ex)
			{
				Logger.Log("Failed to fetch mesh asset {c}: " + ex.getMessage(), LogLevel.Warning, _Client);
				callback.callback(null);
			}
		}
		else
		{
			Logger.Log("GetMesh capability not available", LogLevel.Error, _Client);
			callback.callback(null);
		}
		return false;
	}

	/**
	 * Fetch avatar texture on a grid capable of server side baking
	 *
	 * @param avatarID ID of the avatar
	 * @param textureID ID of the texture
	 * @param bakeName Name of the part of the avatar texture applies to
	 * @param callback Callback invoked on operation completion
	 * @throws URISyntaxException
	 * @return true if image could be retrieved from the cache or the texture request could be successfully sent
	 */
	public boolean RequestServerBakedImage(UUID avatarID, final UUID textureID, String bakeName, final Callback<ImageDownload> callback) throws URISyntaxException
	{
		if (UUID.isZeroOrNull(avatarID) || UUID.isZeroOrNull(textureID) || callback == null)
			return false;
		
		final ImageDownload download = new ImageDownload();
		download.ItemID = textureID;
		download.AssetType = AssetType.Texture;
		download.ImageType = ImageType.ServerBaked;
		download.suffix = "tex";

		byte[] assetData = _Cache.get(textureID, download.suffix);
		// Do we have this image in the cache?
		if (assetData != null)
		{
			download.State = TextureRequestState.Finished;
			download.AssetData = assetData; 
			callback.callback(download);

			FireImageProgressEvent(textureID, assetData.length, assetData.length);
			return true;
		}

		String appearenceUri = _Client.Network.getAgentAppearanceServiceURL();
		if (appearenceUri == null || appearenceUri.isEmpty())
		{
			download.State = TextureRequestState.NotFound;
			callback.callback(download);
			return false;
		}
		URI url = new URI(appearenceUri + "texture/" + avatarID + "/" + bakeName + "/" + textureID);
		Callback<DownloadResult> downloadCallback = new Callback<DownloadResult>()
		{
			@Override
			public boolean callback(DownloadResult result)
			{
				if (result.finished)
				{
					if (result.data != null) // success
					{
						download.State = TextureRequestState.Finished;
						download.AssetData = result.data;
						callback.callback(download);

						FireImageProgressEvent(textureID, result.data.length, result.data.length);
					}
					else
					{
						download.State = TextureRequestState.Timeout;
						download.AssetData = result.data; 
						callback.callback(download);
						Logger.Log("Failed to fetch server bake {" + textureID + "}: empty data", LogLevel.Warning, _Client);
					}
				}
				else
				{
					FireImageProgressEvent(textureID, result.current, result.full);
				}
				return result.finished;
			}

		};
		_HttpDownloads.enque(url, _Client.Settings.CAPS_TIMEOUT, "image/x-j2c", _Cache.cachedAssetFile(download.ItemID, download.suffix), downloadCallback);
		return true;
	}

	/**
	 * Lets TexturePipeline class fire the progress event
	 *
	 * @param texureID The texture ID currently being downloaded
	 * @param transferredBytes The number of bytes transferred
	 * @param totalBytes The total number of bytes expected
	 */
	public final void FireImageProgressEvent(UUID texureID, long transferredBytes, long totalBytes)
	{
		try
		{
			OnImageReceiveProgress.dispatch(new ImageReceiveProgressCallbackArgs(texureID, transferredBytes, totalBytes));
		}
		catch (Throwable ex)
		{
			Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
		}
	}

	/**
	 * Helper method for downloading textures via GetTexture cap
	 * Same signature as the UDP variant since we need all the params to pass to the UDP TexturePipeline
	 * in case we need to fall back to it Linden servers currently (1.42) don't support bakes downloads via HTTP)
	 *
	 * @param textureID
	 * @param imageType
	 * @param priority
	 * @param discardLevel
	 * @param packetStart
	 * @param callback
	 * @param progress
	 */
	private boolean HttpRequestTexture(final ImageDownload download)
	{
		try
		{
			URI url = new URI(String.format("%s/?texture_id=%s", _Client.Network.getCapabilityURI("GetTexture"), download.ItemID));
			Callback<DownloadResult> downloadCallback = new Callback<DownloadResult>()
			{
				@Override
				public boolean callback(DownloadResult result)
				{
					if (result.finished)
					{
						if (result.data != null) // success
						{
							synchronized (_ActiveDownloads)
							{
								_ActiveDownloads.remove(download.ItemID);
							}

							download.Codec = ImageCodec.J2K;
							download.State = TextureRequestState.Finished;
							download.AssetData = result.data;
							download.callbacks.dispatch(download);

							FireImageProgressEvent(download.ItemID, result.data.length, result.data.length);
						}
						else
						{
							download.State = TextureRequestState.Pending;
							download.callbacks.dispatch(download);
							Logger.Log(String.format("Failed to fetch texture {%s} over HTTP, falling back to UDP", download.ItemID), LogLevel.Warning, _Client);
							_TexDownloads.RequestTexture(download);
						}
					}
					else
					{
						FireImageProgressEvent(download.ItemID, result.current, result.full);
					}
					return result.finished;
				}

			};
			_HttpDownloads.enque(url, _Client.Settings.CAPS_TIMEOUT, "image/x-j2c", _Cache.cachedAssetFile(download.ItemID, download.suffix), downloadCallback);
			return true;

		}
		catch (URISyntaxException ex)
		{
			download.State = TextureRequestState.Pending;
			download.callbacks.dispatch(download);
			Logger.Log(String.format("Failed to fetch texture {%s} over HTTP, falling back to UDP: {%s}", download.ItemID, ex.getMessage()), LogLevel.Warning, _Client);
		}
		return false;
	}

	// #region Helpers
	public static AssetItem CreateAssetItem(AssetType type, UUID assetID, byte[] assetData)
	{
		try
		{
			switch (type)
			{
				case Animation:
					return new AssetAnimation(assetID, assetData);
				case Bodypart:
					return new AssetBodypart(assetID, assetData);
				case CallingCard:
					return new AssetCallingCard(assetID, assetData);
				case Clothing:
					return new AssetClothing(assetID, assetData);
				case Gesture:
					return new AssetGesture(assetID, assetData);
				case Landmark:
					return new AssetLandmark(assetID, assetData);
				case LSLBytecode:
					return new AssetScriptBinary(assetID, assetData);
				case LSLText:
					return new AssetScriptText(assetID, assetData);
				case Mesh:
					return new AssetMesh(assetID, assetData);
				case Notecard:
					return new AssetNotecard(assetID, assetData);
				case Object:
					return new AssetPrim(assetID, assetData);
				case Sound:
					return new AssetSound(assetID, assetData);
				case Texture:
					return new AssetTexture(assetID, assetData);
				default:
					Logger.Log("Unimplemented asset type: " + type, LogLevel.Error);
			}
		}
		catch (Exception ex)
		{
			Logger.Log("Exception occurred when creating an asset", Logger.LogLevel.Error, ex);
		}
		return new AssetMutable(type, assetID, assetData);
	}
	// #endregion Helpers

	// #region old asset transfer system

	private boolean processDelayedData(AssetDownload download, DelayedTransfer data)
	{
		while (data != null)
		{
			System.arraycopy(data.Data, 0, download.AssetData, download.Transferred, data.Data.length);
			download.Transferred += data.Data.length;
			if (data.Status == StatusCode.OK && download.Transferred >= download.Size)
			{
				download.Status = StatusCode.Done;
			}
			else
			{
				download.Status = data.Status;
			}
			
			if (download.Status != StatusCode.OK)
			{
				synchronized (_ActiveDownloads)
				{
					_AssetTransfers.remove(download.TransactionID);
					_ActiveDownloads.remove(download.ItemID);
				}
				download.delayed.clear();

				download.Success = download.Status == StatusCode.Done;
				if (download.Success)
				{
					Logger.DebugLog("Transfer for asset " + download.ItemID.toString() + " completed", _Client);

					// Cache successful asset download
					_Cache.put(download.ItemID, download.AssetData, download.suffix);
				}
				else
				{
					Logger.Log("Transfer failed with status code " + download.Status, LogLevel.Warning, _Client);
				}
					
				download.callbacks.dispatch(download);
				return true;
			}
			download.PacketNum++;
			data = download.delayed.remove(download.PacketNum);
		}
		return false;
	}
	
	/**
	 * Process an incoming packet and raise the appropriate events
	 * @throws Exception 
	 */
	private void HandleTransferInfo(Packet packet, Simulator simulator) throws Exception
	{
		TransferInfoPacket info = (TransferInfoPacket) packet;

		AssetDownload download = null;
		synchronized (_ActiveDownloads)
		{
			download = (AssetDownload) _AssetTransfers.get(info.TransferInfo.TransferID);
		}
		if (download == null)
		{
			Logger.Log("Received a TransferInfo packet for an asset we didn't request, TransferID: "
					+ info.TransferInfo.TransferID, LogLevel.Warning, _Client);			
			return;
		}

		download.Channel = ChannelType.setValue(info.TransferInfo.ChannelType);
		download.Status = StatusCode.setValue(info.TransferInfo.Status);
		download.Target = TargetType.setValue(info.TransferInfo.TargetType);
		download.Size = info.TransferInfo.Size;

		if (download.Status != StatusCode.OK)
		{
			Logger.Log("Transfer failed with status code " + download.Status, LogLevel.Warning, _Client);

			synchronized (_ActiveDownloads)
			{
				_AssetTransfers.remove(download.TransactionID);
				_ActiveDownloads.remove(download.ItemID);
			}
			download.delayed.clear();

			// No valid data could have been received before the TransferInfo packet
			download.AssetData = null;

			// Fire the event with our transfer that contains Success = false;
			download.callbacks.dispatch(download);
		}
		else
		{
			download.AssetData = new byte[download.Size];
			byte[] data = info.TransferInfo.getParams();

			if (download.Source == SourceType.Asset && data.length == 20)
			{
				download.ItemID = new UUID(data, 0);
				download.AssetType = AssetType.setValue(Helpers.BytesToInt32L(data, 16));

				Logger.DebugLog(String.format("TransferInfo packet received. AssetID: %s Type: %s",
						download.ItemID, download.AssetType));
			}
			else if (download.Source == SourceType.SimInventoryItem && data.length == 100)
			{
				// TODO: Can we use these?
				UUID agentID = new UUID(data, 0);
				UUID sessionID = new UUID(data, 16);
				UUID ownerID = new UUID(data, 32);
				UUID taskID = new UUID(data, 48);
				UUID itemID = new UUID(data, 64);
				download.ItemID = new UUID(data, 80);
				download.AssetType = AssetType.setValue(Helpers.BytesToInt32L(data, 96));

				Logger.DebugLog(String
						.format("TransferInfo packet received. AgentID: %s SessionID: %s OwnerID: %s TaskID: %s ItemID: %s AssetID: %s Type: %s",
								agentID, sessionID, ownerID, taskID, itemID, download.ItemID, download.AssetType));
			}
			else
			{
				Logger.Log(String.format(
						"Received a TransferInfo packet with a SourceType of %s and a Params field length of %d",
						download.Source, data.length), LogLevel.Warning, _Client);
			}
			processDelayedData(download, download.delayed.remove(download.PacketNum));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @throws Exception
	 */
	private void HandleTransferPacket(Packet packet, Simulator simulator) throws Exception
	{
		TransferPacketPacket asset = (TransferPacketPacket) packet;

		AssetDownload download = null;
		synchronized (_ActiveDownloads)
		{
			download = (AssetDownload) _AssetTransfers.get(asset.TransferData.TransferID);
		}
		if (download != null)
		{
			StatusCode status = StatusCode.setValue(asset.TransferData.Status);
			DelayedTransfer info = new DelayedTransfer(status, asset.TransferData.getData());
			
			if (!download.gotInfo() || asset.TransferData.Packet != download.PacketNum)
			{
				/* We haven't received the header yet, or the packet number is higher than the currently
				 * expected packet. Put it in the out of order hashlist */
				download.delayed.put(asset.TransferData.Packet, info);
			}
			else
			{
				processDelayedData(download, info);
			}
		}
	}

	// /#endregion Transfer Callbacks

	// /#region Xfer Callbacks

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private void HandleInitiateDownloadPacket(Packet packet, Simulator simulator)
	{
		InitiateDownloadPacket request = (InitiateDownloadPacket) packet;
		try
		{
			OnInitiateDownload.dispatch(new InitiateDownloadCallbackArgs(Helpers.BytesToString(request.FileData
					.getSimFilename()), Helpers.BytesToString(request.FileData.getViewerFilename())));
		}
		catch (Exception ex)
		{
			Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
		}
	}

	private void SendNextUploadPacket(AssetUpload upload) throws Exception
	{
		SendXferPacketPacket send = new SendXferPacketPacket();

		send.XferID.ID = upload.XferID;
		send.XferID.Packet = upload.PacketNum++;

		// The first packet reserves the first four bytes of the data for the
		// total length of the asset and appends 1000 bytes of data after that
		int off = send.XferID.Packet == 0 ? 4 : 0, len = 1000;
		if (upload.Transferred + len >= upload.Size)
		{
			// Last packet
			len = upload.Size - upload.Transferred;
			send.XferID.Packet |= 0x80000000; // This signals the final packet
		}
		
		byte[] data = new byte[off + len];
		if (send.XferID.Packet == 0)
			Helpers.Int32ToBytesL(upload.Size, data, 0);
		System.arraycopy(upload.AssetData, upload.Transferred, data, off, len);
		send.DataPacket.setData(data);
		upload.Transferred += len;

		send.DataPacket.setData(data);
		_Client.Network.sendPacket(send);
	}

	private void SendConfirmXferPacket(long xferID, int packetNum) throws Exception
	{
		ConfirmXferPacketPacket confirm = new ConfirmXferPacketPacket();
		confirm.XferID.ID = xferID;
		confirm.XferID.Packet = packetNum;

		_Client.Network.sendPacket(confirm);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @throws Exception
	 */
	private void HandleRequestXfer(Packet packet, Simulator simulator) throws Exception
	{
		RequestXferPacket request = (RequestXferPacket) packet;
		AssetUpload upload = _PendingUpload.poll();
		if (upload == null)
		{
			Logger.Log("Received a RequestXferPacket for an unknown asset upload", LogLevel.Warning, _Client);
			return;
		}

		upload.XferID = request.XferID.ID;
		upload.AssetType = AssetType.setValue(request.XferID.VFileType);

		synchronized (_XferTransfers)
		{
			_XferTransfers.put(upload.XferID, upload);
		}
		// Send the first packet containing actual asset data
		SendNextUploadPacket(upload);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @throws Exception
	 */
	private void HandleConfirmXferPacket(Packet packet, Simulator simulator) throws Exception
	{
		ConfirmXferPacketPacket confirm = (ConfirmXferPacketPacket) packet;

		AssetUpload upload = null;
		synchronized (_XferTransfers)
		{
			upload = (AssetUpload) _XferTransfers.get(confirm.XferID.ID);
		}
		Logger.DebugLog(String.format("ACK for upload %s of asset type %s (%d/%d)", upload.AssetID, upload.AssetType,
					upload.Transferred, upload.Size));

		OnUploadProgress.dispatch(upload);

		if (upload.Transferred < upload.Size)
		{
			SendNextUploadPacket(upload);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private void HandleAssetUploadComplete(Packet packet, Simulator simulator)
	{
		AssetUploadCompletePacket complete = (AssetUploadCompletePacket) packet;

		AssetUpload upload = null;
		synchronized (_ActiveDownloads)
		{
			upload = (AssetUpload)_AssetTransfers.remove(complete.AssetBlock.UUID);
		}
		if (upload != null)
		{
			synchronized (_XferTransfers)
			{
				_XferTransfers.remove(upload.XferID);
			}
			OnAssetUploaded.dispatch(upload);
		}
		else
		{
			Logger.Log(String.format(
					"Got an AssetUploadComplete on an unrecognized asset, AssetID: %s, Type: %s, Success: %s",
					complete.AssetBlock.UUID, AssetType.setValue(complete.AssetBlock.Type),
					complete.AssetBlock.Success), LogLevel.Warning);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @throws Exception
	 */
	private void HandleSendXferPacket(Packet packet, Simulator simulator) throws Exception
	{
		SendXferPacketPacket xfer = (SendXferPacketPacket) packet;
		XferDownload download = null;
		synchronized (_XferTransfers)
		{
			download = (XferDownload) _XferTransfers.get(xfer.XferID.ID);
		}
		if (download != null)
		{
			// Apply a mask to get rid of the "end of transfer" bit
			int packetNum = xfer.XferID.Packet & 0x7FFFFFFF;

			// Check for out of order packets, possibly indicating a resend
			if (packetNum != download.PacketNum)
			{
				if (packetNum == download.PacketNum - 1)
				{
					Logger.DebugLog("Resending Xfer download confirmation for packet " + packetNum, _Client);
					SendConfirmXferPacket(download.XferID, packetNum);
				}
				else
				{
					Logger.Log("Out of order Xfer packet in a download, got " + packetNum + " expecting "
							+ download.PacketNum, LogLevel.Warning, _Client);
					// Re-confirm the last packet we actually received
					SendConfirmXferPacket(download.XferID, download.PacketNum - 1);
				}
				return;
			}

			byte[] bytes = xfer.DataPacket.getData();
			if (packetNum == 0)
			{
				// This is the first packet received in the download, the first
				// four bytes are a size integer
				// in little endian ordering
				download.Size = Helpers.BytesToInt32L(bytes);
				download.AssetData = new byte[download.Size];

				Logger.DebugLog("Received first packet in an Xfer download of size " + download.Size);

				System.arraycopy(bytes, 4, download.AssetData, 0, bytes.length - 4);
				download.Transferred += bytes.length - 4;
			}
			else
			{
				System.arraycopy(bytes, 0, download.AssetData, 1000 * packetNum, bytes.length);
				download.Transferred += bytes.length;
			}

			// Increment the packet number to the packet we are expecting next
			download.PacketNum++;

			// Confirm receiving this packet
			SendConfirmXferPacket(download.XferID, packetNum);

			if ((xfer.XferID.Packet & 0x80000000) != 0)
			{
				// This is the last packet in the transfer
				if (!Helpers.isEmpty(download.Filename))
				{
					Logger.DebugLog("Xfer download for asset " + download.Filename + " completed", _Client);
				}
				else
				{
					Logger.DebugLog("Xfer download for asset " + download.ItemID.toString() + " completed", _Client);
				}

				download.Success = true;
				synchronized (_XferTransfers)
				{
					_XferTransfers.remove(download.TransactionID);
				}
				OnXferReceived.dispatch(download);
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private void HandleAbortXfer(Packet packet, Simulator simulator)
	{
		AbortXferPacket abort = (AbortXferPacket) packet;
		XferDownload download = null;

		synchronized (_XferTransfers)
		{
			download = (XferDownload) _XferTransfers.remove(abort.XferID.ID);
		}

		if (download != null && OnXferReceived.count() > 0)
		{
			download.Success = false;
			download.Error = TransferError.setValue(abort.XferID.Result);

			OnXferReceived.dispatch(download);
		}
	}
	// #endregion Xfer Callbacks
}
