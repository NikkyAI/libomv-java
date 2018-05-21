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
package libomv.io.assets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.concurrent.FutureCallback;
import org.apache.log4j.Logger;

import libomv.ImportExport.collada.Asset;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDMap;
import libomv.assets.AssetAnimation;
import libomv.assets.AssetBodypart;
import libomv.assets.AssetCallingCard;
import libomv.assets.AssetClothing;
import libomv.assets.AssetGesture;
import libomv.assets.AssetItem;
import libomv.assets.AssetLandmark;
import libomv.assets.AssetMesh;
import libomv.assets.AssetMutable;
import libomv.assets.AssetNotecard;
import libomv.assets.AssetPrim;
import libomv.assets.AssetScriptBinary;
import libomv.assets.AssetScriptText;
import libomv.assets.AssetSound;
import libomv.assets.AssetTexture;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.UploadBakedTextureMessage;
import libomv.capabilities.CapsMessage.UploaderRequestComplete;
import libomv.capabilities.CapsMessage.UploaderRequestUpload;
import libomv.imaging.ManagedImage.ImageCodec;
import libomv.inventory.InventoryItem;
import libomv.io.DownloadManager;
import libomv.io.DownloadManager.DownloadResult;
import libomv.io.GridClient;
import libomv.io.LibSettings;
import libomv.io.capabilities.CapsClient;
import libomv.model.Simulator;
import libomv.model.asset.AssetDownload;
import libomv.model.asset.AssetType;
import libomv.model.asset.AssetUpload;
import libomv.model.asset.BakedTextureUploadedCallback;
import libomv.model.asset.ChannelType;
import libomv.model.asset.DelayedTransfer;
import libomv.model.asset.ImageDownload;
import libomv.model.asset.ImageReceiveProgressCallbackArgs;
import libomv.model.asset.ImageType;
import libomv.model.asset.InitiateDownloadCallbackArgs;
import libomv.model.asset.MeshDownload;
import libomv.model.asset.SourceType;
import libomv.model.asset.StatusCode;
import libomv.model.asset.TargetType;
import libomv.model.asset.Transfer;
import libomv.model.asset.TransferError;
import libomv.model.asset.XferDownload;
import libomv.model.texture.TextureRequestState;
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
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.RefObject;
import libomv.utils.TimeoutEvent;

// Summary description for AssetManager.
public class AssetManager implements PacketCallback {
	private static final Logger logger = Logger.getLogger(AssetManager.class);

	private class MeshDownloadCallback implements Callback<DownloadResult> {
		private MeshDownload download;

		public MeshDownloadCallback(MeshDownload download) {
			this.download = download;
		}

		@Override
		public boolean callback(DownloadResult result) {
			if (result.finished) {
				if (result.data != null) // success
				{
					download.assetData = result.data;
				}
				download.callbacks.dispatch(download);
			}
			return result.finished;
		}
	}

	public CallbackHandler<XferDownload> onXferReceived = new CallbackHandler<>();
	private CallbackHandler<AssetUpload> onAssetUploaded = new CallbackHandler<>();
	private CallbackHandler<AssetUpload> onUploadProgress = new CallbackHandler<>();
	private CallbackHandler<InitiateDownloadCallbackArgs> onInitiateDownload = new CallbackHandler<>();
	private CallbackHandler<ImageReceiveProgressCallbackArgs> onImageReceiveProgress = new CallbackHandler<>();

	private TexturePipeline texDownloads;
	private DownloadManager httpDownloads;
	private GridClient client;

	/*
	 * Transfers based on the asset ID to maintain an overview of currently active
	 * transfers
	 */
	private Map<UUID, Transfer> activeDownloads;

	/* Transfers based on the transaction ID used by the old transfer system */
	private Map<UUID, Transfer> assetTransfers;

	/* Transfers based on the transaction ID used by the even older xfer system */
	private Map<Long, Transfer> xferTransfers;

	private ExecutorService threadPool;
	private Future<?> threadResult = null;
	private BlockingQueue<AssetUpload> pendingUpload;

	// Texture download cache
	private AssetCache cache;

	/*
	 * Default constructor
	 *
	 * @param client A reference to the GridClient object
	 */
	public AssetManager(GridClient client) {
		this.client = client;
		cache = new AssetCache(client);

		xferTransfers = new HashMap<>();
		pendingUpload = new ArrayBlockingQueue<>(1);

		assetTransfers = new HashMap<>();
		activeDownloads = new HashMap<>();

		texDownloads = new TexturePipeline(client, cache);

		threadPool = Executors.newSingleThreadExecutor();

		httpDownloads = new DownloadManager();

		// Transfer packets for downloading large assets
		client.network.registerCallback(PacketType.TransferInfo, this);
		client.network.registerCallback(PacketType.TransferPacket, this);

		// Xfer packets for uploading large assets
		client.network.registerCallback(PacketType.RequestXfer, this);
		client.network.registerCallback(PacketType.ConfirmXferPacket, this);
		client.network.registerCallback(PacketType.AssetUploadComplete, this);

		// Xfer packets for downloading misc assets
		client.network.registerCallback(PacketType.SendXferPacket, this);
		client.network.registerCallback(PacketType.AbortXfer, this);

		// Simulator is responding to a request to download a file
		client.network.registerCallback(PacketType.InitiateDownload, this);
	}

	@Override
	protected void finalize() {
		client.network.unregisterCallback(PacketType.TransferInfo, this);
		client.network.unregisterCallback(PacketType.TransferPacket, this);

		// Xfer packets for uploading large assets
		client.network.unregisterCallback(PacketType.RequestXfer, this);
		client.network.unregisterCallback(PacketType.ConfirmXferPacket, this);
		client.network.unregisterCallback(PacketType.AssetUploadComplete, this);

		// Xfer packets for downloading misc assets
		client.network.unregisterCallback(PacketType.SendXferPacket, this);
		client.network.unregisterCallback(PacketType.AbortXfer, this);

		// Simulator is responding to a request to download a file
		client.network.unregisterCallback(PacketType.InitiateDownload, this);

		texDownloads.shutdown();
		texDownloads = null;

		activeDownloads = null;
		assetTransfers = null;
		pendingUpload = null;
		xferTransfers = null;

		if (threadResult != null)
			threadResult.cancel(true);
		threadPool.shutdownNow();

		httpDownloads.shutdown();

		// Transfer packets for downloading large assets
		client = null;
		cache = null;
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case TransferInfo:
			handleTransferInfo(packet, simulator);
			break;
		case TransferPacket:
			handleTransferPacket(packet, simulator);
			break;
		case RequestXfer:
			handleRequestXfer(packet, simulator);
			break;
		case ConfirmXferPacket:
			handleConfirmXferPacket(packet, simulator);
			break;
		case AssetUploadComplete:
			handleAssetUploadComplete(packet, simulator);
			break;
		case SendXferPacket:
			handleSendXferPacket(packet, simulator);
			break;
		case AbortXfer:
			handleAbortXfer(packet, simulator);
			break;
		case InitiateDownload:
			handleInitiateDownloadPacket(packet, simulator);
			break;
		default:
			break;
		}
	}

	public AssetCache getCache() {
		return cache;
	}

	/**
	 * Request an asset download through the almost deprecated Xfer system
	 *
	 * @param filename
	 *            Filename of the asset to request
	 * @param deleteOnCompletion
	 *            Whether or not to delete the asset off the server after it is
	 *            retrieved
	 * @param useBigPackets
	 *            Use large transfer packets or not
	 * @param vFileID
	 *            UUID of the file to request, if filename is left empty
	 * @param vFileType
	 *            Asset type of <code>vFileID</code>, or
	 *            <code>AssetType.Unknown</code> if filename is not empty
	 * @param fromCache
	 *            Sets the FilePath in the request to Cache (4) if true, otherwise
	 *            Unknown (0) is used
	 * @return The transaction ID that this Asset download identifies
	 * @throws Exception
	 */
	public long requestAssetXfer(String filename, boolean deleteOnCompletion, boolean useBigPackets, UUID vFileID,
			AssetType vFileType, boolean fromCache) throws Exception {
		XferDownload transfer = new XferDownload();
		transfer.xferID = new UUID().AsLong();
		transfer.filename = filename;
		transfer.itemID = vFileID;
		transfer.assetType = vFileType;

		// Add this transfer to the dictionary
		synchronized (xferTransfers) {
			xferTransfers.put(transfer.xferID, transfer);
		}

		RequestXferPacket request = new RequestXferPacket();
		request.XferID.ID = transfer.xferID;
		request.XferID.setFilename(Helpers.stringToBytes(filename));
		request.XferID.FilePath = fromCache ? (byte) 4 : (byte) 0;
		request.XferID.DeleteOnCompletion = deleteOnCompletion;
		request.XferID.UseBigPackets = useBigPackets;
		request.XferID.VFileID = vFileID;
		request.XferID.VFileType = vFileType.getValue();

		client.network.sendPacket(request);

		return transfer.xferID;
	}

	/**
	 * Request an asset download
	 *
	 * @param assetID
	 *            Asset UUID
	 * @param type
	 *            Asset type, must be correct for the transfer to succeed
	 * @param priority
	 *            Whether to give this transfer an elevated priority
	 * @param callback
	 *            The callback to fire when the simulator responds with the asset
	 *            data
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID requestAsset(UUID assetID, AssetType type, boolean priority, Callback<AssetDownload> callback)
			throws Exception {
		return requestAsset(assetID, type, priority, SourceType.Asset, callback);
	}

	/**
	 * Request an asset download
	 *
	 * @param assetID
	 *            Asset UUID
	 * @param type
	 *            Asset type, must be correct for the transfer to succeed
	 * @param priority
	 *            Whether to give this transfer an elevated priority
	 * @param sourceType
	 *            Source location of the requested asset
	 * @param callback
	 *            The callback to fire when the simulator responds with the asset
	 *            data
	 * @return The transaction ID that this asset download identifies
	 *
	 * @throws Exception
	 */
	public UUID requestAsset(UUID assetID, AssetType type, boolean priority, SourceType sourceType,
			Callback<AssetDownload> callback) throws Exception {
		return requestAsset(assetID, UUID.Zero, UUID.Zero, type, priority, sourceType, null, callback);
	}

	/**
	 * Request an asset download
	 *
	 * @param assetID
	 *            Asset UUID
	 * @param type
	 *            Asset type, must be correct for the transfer to succeed
	 * @param priority
	 *            Whether to give this transfer an elevated priority
	 * @param sourceType
	 *            Source location of the requested asset
	 * @param transactionID
	 *            UUID of the transaction
	 * @param callback
	 *            The callback to fire when the simulator responds with the asset
	 *            data
	 * @return The transaction ID that this asset download identifies
	 *
	 * @throws Exception
	 */
	public UUID requestAsset(UUID assetID, UUID itemID, UUID taskID, AssetType type, boolean priority,
			SourceType sourceType, UUID transactionID, Callback<AssetDownload> callback) throws Exception {
		Simulator simulator = client.network.getCurrentSim();

		// Build the request packet and send it
		TransferRequestPacket request = checkAssetCache(assetID, type, transactionID, priority, sourceType, simulator,
				callback, "asset");
		if (request != null) {
			byte[] paramField = UUID.isZeroOrNull(taskID) ? new byte[20] : new byte[96];
			assetID.toBytes(paramField, 0);
			System.arraycopy(Helpers.Int32ToBytesL(type.getValue()), 0, paramField, 16, 4);
			if (!UUID.isZeroOrNull(taskID)) {
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
	 * @param transactionID
	 *            The transaction ID of the asset download to abort
	 *
	 * @throws Exception
	 */
	public boolean abortAssetTransfer(UUID transactionID) throws Exception {
		AssetDownload download;
		synchronized (activeDownloads) {
			download = (AssetDownload) assetTransfers.remove(transactionID);
			if (download != null)
				activeDownloads.remove(download.itemID);
		}

		if (download != null) {
			// Abort the transfer
			TransferAbortPacket abort = new TransferAbortPacket();
			abort.TransferInfo.ChannelType = download.channel.getValue();
			abort.TransferInfo.TransferID = download.transactionID;
			download.simulator.sendPacket(abort);

			download.success = false;

			// Fire the event with our transfer that contains Success = false
			if (download.callbacks != null) {
				download.status = StatusCode.Abort;
				download.callbacks.dispatch(download);
			}
			return true;
		}
		return false;
	}

	/**
	 * Request an asset download for the inventory
	 *
	 * @param item
	 *            Use UUID.Zero if you do not have the asset ID but have all the
	 *            necessary permissions
	 * @param priority
	 *            Whether to prioritize this asset download or not
	 * @param callback
	 *            The callback to fire when the simulator responds with the asset
	 *            data
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID requestInventoryAsset(InventoryItem item, boolean priority, Callback<AssetDownload> callback)
			throws Exception {
		return requestInventoryAsset(item.assetID, item.itemID, UUID.Zero, item.getOwnerID(), item.assetType, priority,
				callback);
	}

	/**
	 * Request an asset download for the inventory
	 *
	 * @param assetID
	 *            Use UUID.Zero if you do not have the asset ID but have all the
	 *            necessary permissions
	 * @param itemID
	 *            The item ID of this asset in the inventory
	 * @param taskID
	 *            Use UUID.Zero if you are not requesting an asset from an object
	 *            inventory
	 * @param ownerID
	 *            The owner of this asset
	 * @param type
	 *            Asset type
	 * @param priority
	 *            Whether to prioritize this asset download or not
	 * @param callback
	 *            The callback to fire when the simulator responds with the asset
	 *            data
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID requestInventoryAsset(UUID assetID, UUID itemID, UUID taskID, UUID ownerID, AssetType type,
			boolean priority, Callback<AssetDownload> callback) throws Exception {
		Simulator simulator = client.network.getCurrentSim();

		// Build the request packet and send it
		TransferRequestPacket request = checkAssetCache(assetID, type, null, priority, SourceType.SimInventoryItem,
				simulator, callback, "asset");
		if (request != null) {
			byte[] paramField = new byte[100];
			client.agent.getAgentID().toBytes(paramField, 0);
			client.agent.getSessionID().toBytes(paramField, 16);
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
	 * @param assetID
	 *            Use UUID.Zero if you do not have the asset ID but have all the
	 *            necessary permissions
	 * @param type
	 *            Asset type
	 * @param priority
	 *            Whether to prioritize this asset download or not
	 * @param callback
	 *            The callback to fire when the simulator responds with the asset
	 *            data
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID requestEstateAsset(UUID assetID, AssetType type, boolean priority, Callback<AssetDownload> callback)
			throws Exception {
		Simulator simulator = client.network.getCurrentSim();

		// Build the request packet and send it
		TransferRequestPacket request = checkAssetCache(assetID, type, null, priority, SourceType.SimEstate, simulator,
				callback, "asset");
		if (request != null) {
			byte[] paramField = new byte[36];
			client.agent.getAgentID().toBytes(paramField, 0);
			client.agent.getSessionID().toBytes(paramField, 16);
			Helpers.Int32ToBytesL(type.getValue(), paramField, 32);
			request.TransferInfo.setParams(paramField);

			simulator.sendPacket(request);
			return request.TransferInfo.TransferID;
		}
		return null;
	}

	private TransferRequestPacket checkAssetCache(UUID assetID, AssetType type, UUID transactionID, boolean priority,
			SourceType sourceType, Simulator simulator, Callback<AssetDownload> callback, String suffix) {
		AssetDownload transfer;
		// Check asset cache first
		byte[] data = cache.get(assetID, suffix);
		if (data != null) {
			// Is caller interested to get a callback?
			if (callback != null) {
				transfer = new AssetDownload();
				transfer.itemID = assetID;
				transfer.assetData = data;
				transfer.assetType = type;
				transfer.success = true;
				transfer.status = StatusCode.OK;

				try {
					callback.callback(transfer);
				} catch (Throwable ex) {
					logger.error(GridClient.Log(ex.getMessage(), client), ex);
				}
			}
			return null;
		}

		/*
		 * If we already have this asset requested and in the download queue just add
		 * the new callback to this request
		 */
		synchronized (activeDownloads) {
			transfer = (AssetDownload) activeDownloads.get(assetID);
		}
		if (transfer != null) {
			transfer.callbacks.add(callback);
			return null;
		}

		// Add this transfer to the dictionary
		transfer = new AssetDownload();
		transfer.transactionID = UUID.isZeroOrNull(transactionID) ? new UUID() : transactionID;
		transfer.itemID = assetID;
		// transfer.AssetType = type; // Set again in TransferInfoHandler.
		transfer.priority = 100.0f + (priority ? 1.0f : 0.0f);
		transfer.channel = ChannelType.Asset;
		transfer.source = sourceType;
		transfer.simulator = simulator;
		transfer.suffix = suffix;
		transfer.callbacks = new CallbackHandler<>();
		transfer.callbacks.add(callback);

		synchronized (activeDownloads) {
			assetTransfers.put(transfer.transactionID, transfer);
			activeDownloads.put(assetID, transfer);
		}

		// Build the request packet and send it
		TransferRequestPacket request = new TransferRequestPacket();
		request.TransferInfo.ChannelType = transfer.channel.getValue();
		request.TransferInfo.Priority = transfer.priority;
		request.TransferInfo.SourceType = sourceType.getValue();
		request.TransferInfo.TransferID = transfer.transactionID;
		return request;
	}

	/**
	 * Request an asset be uploaded to the simulator
	 *
	 * @param asset
	 *            The {@link Asset} Object containing the asset data
	 * @param storeLocal
	 *            If True, the asset once uploaded will be stored on the simulator
	 *            in which the client was connected in addition to being stored on
	 *            the asset server
	 * @return The transaction ID that this asset upload identifies
	 * @throws Exception
	 */
	public UUID requestUpload(AssetItem asset, boolean storeLocal) throws Exception {
		if (asset.assetData == null) {
			throw new IllegalArgumentException("Can't upload an asset with no data (did you forget to call Encode?)");
		}
		return requestUpload(null, asset.getAssetType(), asset.assetData, storeLocal, new UUID());
	}

	/**
	 * Request an asset be uploaded to the simulator
	 *
	 * @param type
	 *            The {@link AssetType} of the asset being uploaded
	 * @param data
	 *            A byte array containing the encoded asset data
	 * @param storeLocal
	 *            If True, the asset once uploaded will be stored on the simulator
	 *            in which the client was connected in addition to being stored on
	 *            the asset server
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID requestUpload(AssetType type, byte[] data, boolean storeLocal) throws Exception {
		return requestUpload(null, type, data, storeLocal, new UUID());
	}

	/**
	 * Request an asset be uploaded to the simulator
	 *
	 * @param assetID
	 * @param type
	 *            Asset type to upload this data as
	 * @param data
	 *            A byte array containing the encoded asset data
	 * @param storeLocal
	 *            If True, the asset once uploaded will be stored on the simulator
	 *            in which the client was connected in addition to being stored on
	 *            the asset server
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	public UUID requestUpload(RefObject<UUID> assetID, AssetType type, byte[] data, boolean storeLocal)
			throws Exception {
		return requestUpload(assetID, type, data, storeLocal, new UUID());
	}

	/**
	 * Request an asset be uploaded to the simulator
	 *
	 * @param assetID
	 * @param type
	 *            Asset type to upload this data as
	 * @param data
	 *            A byte array containing the encoded asset data
	 * @param storeLocal
	 *            If True, the asset once uploaded will be stored on the simulator
	 *            in which the client was connected in addition to being stored on
	 *            the asset server
	 * @param transactionID
	 *            The unique transaction ID that this asset download identifies
	 * @return The transaction ID that this asset download identifies
	 * @throws Exception
	 */
	private UUID requestUpload(RefObject<UUID> assetID, AssetType type, byte[] data, boolean storeLocal,
			UUID transactionID) throws Exception {
		AssetUpload upload = new AssetUpload();
		/* Create a new asset ID for this asset */
		upload.assetID = UUID.Combine(transactionID, client.agent.getSecureSessionID());
		if (assetID != null)
			assetID.argvalue = upload.assetID;
		upload.assetData = data;
		upload.assetType = type;
		upload.size = data.length;
		upload.xferID = 0;
		upload.transactionID = transactionID;

		// Build and send the upload packet
		AssetUploadRequestPacket request = new AssetUploadRequestPacket();
		request.AssetBlock.StoreLocal = storeLocal;
		request.AssetBlock.Tempfile = false; // This field is deprecated
		request.AssetBlock.TransactionID = transactionID;
		request.AssetBlock.Type = type.getValue();

		if (data.length + 100 < LibSettings.MAX_PACKET_SIZE) {
			logger.info(GridClient
					.Log(String.format("Beginning asset upload [Single Packet], ID: %s, AssetID: %s, Size: %d",
							upload.transactionID, upload.assetID, upload.size), client));

			synchronized (activeDownloads) {
				assetTransfers.put(transactionID, upload);
			}

			// The whole asset will fit in this packet, makes things easy
			request.AssetBlock.setAssetData(data);
			upload.transferred = data.length;
		} else {
			logger.info(GridClient
					.Log(String.format("Beginning asset upload [Multiple Packets], ID: %s, AssetID: %s, Size: %d",
							upload.transactionID, upload.assetID, upload.size), client));

			// Asset is too big, send in multiple packets
			request.AssetBlock.setAssetData(Helpers.EmptyBytes);

			// Wait for the previous upload to receive a RequestXferPacket
			final int UPLOAD_CONFIRM_TIMEOUT = 20 * 1000;
			if (!pendingUpload.offer(upload, UPLOAD_CONFIRM_TIMEOUT, TimeUnit.MILLISECONDS)) {
				throw new Exception("Timeout waiting for previous asset upload to begin");
			}
		}

		client.network.sendPacket(request);
		return upload.transactionID;
	}

	public void requestUploadBakedTexture(final byte[] textureData, final BakedTextureUploadedCallback callback)
			throws IOException {
		URI url = client.network.getCapabilityURI(CapsEventType.UploadBakedTexture.toString());
		if (url != null) {
			// Fetch the uploader capability
			CapsClient request = new CapsClient(client, CapsEventType.UploadBakedTexture.toString());

			class RequestUploadBakedTextureComplete implements FutureCallback<OSD> {
				@Override
				public void completed(OSD result) {
					if (result instanceof OSDMap) {
						UploadBakedTextureMessage message = client.messages.new UploadBakedTextureMessage();
						message.deserialize((OSDMap) result);
						if (message.request.state.equals("complete")) {
							callback.callback(((UploaderRequestComplete) message.request).assetID);
							return;
						} else if (message.request.state.equals("upload")) {
							URI uploadUrl = ((UploaderRequestUpload) message.request).url;
							if (uploadUrl != null) {
								try {
									// POST the asset data
									CapsClient upload = new CapsClient(client,
											CapsEventType.UploadBakedTexture.toString());
									upload.executeHttpPost(uploadUrl, textureData, "application/octet-stream", null,
											new RequestUploadBakedTextureComplete(), client.settings.CAPS_TIMEOUT);
								} catch (IOException ex) {
									logger.warn(GridClient.Log("Bake upload failed", client));
									callback.callback(UUID.Zero);
								}
							}
							return;
						}
					}
					logger.warn(GridClient.Log("Bake upload failed", client));
					callback.callback(UUID.Zero);
				}

				@Override
				public void cancelled() {
					logger.warn(GridClient.Log("Bake upload canelled", client));
					callback.callback(UUID.Zero);
				}

				@Override
				public void failed(Exception ex) {
					logger.warn(GridClient.Log("Bake upload failed", client), ex);
					callback.callback(UUID.Zero);
				}
			}
			request.executeHttpPost(url, new OSDMap(), OSDFormat.Xml, new RequestUploadBakedTextureComplete(),
					client.settings.CAPS_TIMEOUT);
		} else {
			logger.info(GridClient.Log("UploadBakedTexture not available, falling back to UDP method", client));

			threadResult = threadPool.submit(new Runnable() {
				@Override
				public void run() {
					final UUID transactionID = new UUID();
					final TimeoutEvent<Boolean> uploadEvent = new TimeoutEvent<>();

					Callback<AssetUpload> udpCallback = new Callback<AssetUpload>() {
						@Override
						public boolean callback(AssetUpload e) {
							if (transactionID.equals(e.transactionID)) {
								uploadEvent.set(true);
								callback.callback(e.success ? e.assetID : UUID.Zero);
							}
							return false;
						}
					};

					onAssetUploaded.add(udpCallback);
					Boolean success;
					try {
						requestUpload(null, AssetType.Texture, textureData, true, transactionID);
						success = uploadEvent.waitOne(client.settings.TRANSFER_TIMEOUT);
					} catch (Exception t) {
						success = false;
					}
					onAssetUploaded.remove(udpCallback);
					if (success == null || !success) {
						callback.callback(UUID.Zero);
					}
				}
			});
		}
	}

	/**
	 * Request a texture asset from the simulator using the
	 * <see cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 *
	 * @param textureID
	 *            The <see cref="UUID"/> of the texture asset to download
	 * @param imageType
	 *            The <see cref="ImageType"/> of the texture asset. Use
	 *            <see cref="ImageType.Normal"/> for most textures, or
	 *            <see cref="ImageType.Baked"/> for baked layer texture assets
	 * @param priority
	 *            A float indicating the requested priority for the transfer. Higher
	 *            priority values tell the simulator to prioritize the request
	 *            before lower valued requests. An image already being transferred
	 *            using the <see cref="TexturePipeline"/> can have its priority
	 *            changed by resending the request with the new priority value
	 * @param discardLevel
	 *            Number of quality layers to discard. This controls the end marker
	 *            of the data sent. Sending with value -1 combined with priority of
	 *            0 cancels an in-progress transfer. A bug exists in the Linden
	 *            Simulator where a -1 will occasionally be sent with a non-zero
	 *            priority indicating an off-by-one error.
	 * @param packetStart
	 *            The packet number to begin the request at. A value of 0 begins the
	 *            request from the start of the asset texture
	 * @param callback
	 *            The <see cref="TextureDownloadCallback"/> callback to fire when
	 *            the image is retrieved. The callback will contain the result of
	 *            the request and the texture asset data
	 * @param progress
	 *            If true, the callback will be fired for each chunk of the
	 *            downloaded image. The callback asset parameter will contain all
	 *            previously received chunks of the texture asset starting from the
	 *            beginning of the request
	 * @returns true if the request could be satisfied from cache or sent
	 *          successfully
	 */
	public boolean requestImage(UUID textureID, ImageType imageType, float priority, int discardLevel, int packetStart,
			Callback<ImageDownload> callback, boolean progress) {
		if (UUID.isZeroOrNull(textureID) || callback == null)
			return false;

		ImageDownload download = null;

		/* Check if we have it already in our cache */
		byte[] assetData = cache.get(textureID, "tex");
		if (assetData != null) {
			if (callback != null) {
				download = new ImageDownload();
				download.itemID = textureID;
				download.assetData = assetData;
				download.imageType = imageType;
				download.state = TextureRequestState.Finished;
				callback.callback(download);
			}
			if (progress)
				client.assets.fireImageProgressEvent(textureID, assetData.length, assetData.length);
			return true;
		}

		synchronized (activeDownloads) {
			download = (ImageDownload) activeDownloads.get(textureID);
			if (download != null) {
				download.callbacks.add(callback);
				return true;
			}
			download = new ImageDownload();
			download.itemID = textureID;
			download.assetData = assetData;
			download.imageType = imageType;
			download.state = TextureRequestState.Started;
			download.priority = priority;
			download.discardLevel = discardLevel;
			download.reportProgress = progress;
			download.suffix = "tex";
			download.callbacks = new CallbackHandler<>();
			download.callbacks.add(callback);
			activeDownloads.put(textureID, download);
		}

		boolean sent = false;
		if (client.settings.getBool(LibSettings.USE_HTTP_TEXTURES)
				&& client.network.getCapabilityURI("GetTexture") != null) {
			sent = httpRequestTexture(download);
		}

		if (!sent) {
			sent = texDownloads.requestTexture(download);
		}
		return sent;
	}

	/**
	 * Overload: Request a texture asset from the simulator using the
	 * <see cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 *
	 * @param textureID
	 *            The <see cref="UUID"/> of the texture asset to download
	 * @param callback
	 *            The <see cref="TextureDownloadCallback"/> callback to fire when
	 *            the image is retrieved. The callback will contain the result of
	 *            the request and the texture asset data
	 */
	public boolean requestImage(UUID textureID, Callback<ImageDownload> callback) {
		return requestImage(textureID, ImageType.Normal, 101300.0f, 0, 0, callback, false);
	}

	/**
	 * Overload: Request a texture asset from the simulator using the
	 * <see cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 *
	 * @param textureID
	 *            The <see cref="UUID"/> of the texture asset to download
	 * @param imageType
	 *            The <see cref="ImageType"/> of the texture asset. Use
	 *            <see cref="ImageType.Normal"/> for most textures, or
	 *            <see cref="ImageType.Baked"/> for baked layer texture assets
	 * @param callback
	 *            The <see cref="TextureDownloadCallback"/> callback to fire when
	 *            the image is retrieved. The callback will contain the result of
	 *            the request and the texture asset data
	 */
	public boolean requestImage(UUID textureID, ImageType imageType, Callback<ImageDownload> callback) {
		return requestImage(textureID, imageType, 101300.0f, 0, 0, callback, false);
	}

	/**
	 * Overload: Request a texture asset from the simulator using the
	 * <see cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 *
	 * @param textureID
	 *            The <see cref="UUID"/> of the texture asset to download
	 * @param imageType
	 *            The <see cref="ImageType"/> of the texture asset. Use
	 *            <see cref="ImageType.Normal"/> for most textures, or
	 *            <see cref="ImageType.Baked"/> for baked layer texture assets
	 * @param callback
	 *            The <see cref="TextureDownloadCallback"/> callback to fire when
	 *            the image is retrieved. The callback will contain the result of
	 *            the request and the texture asset data
	 * @param progress
	 *            If true, the callback will be fired for each chunk of the
	 *            downloaded image. The callback asset parameter will contain all
	 *            previously received chunks of the texture asset starting from the
	 *            beginning of the request
	 */
	public boolean requestImage(UUID textureID, ImageType imageType, Callback<ImageDownload> callback,
			boolean progress) {
		return requestImage(textureID, imageType, 101300.0f, 0, 0, callback, progress);
	}

	/**
	 * Cancel a texture request
	 *
	 * @param textureID
	 *            The texture assets <see cref="UUID"/>
	 * @throws Exception
	 */
	public void requestImageCancel(UUID textureID) throws Exception {
		synchronized (activeDownloads) {
			activeDownloads.remove(textureID);
		}
		texDownloads.abortTextureRequest(textureID);
	}

	/**
	 * Requests download of a mesh asset
	 *
	 * @param meshID
	 *            UUID of the mesh asset
	 * @param callback
	 *            Callback when the request completes
	 */
	public boolean requestMesh(final UUID meshID, Callback<MeshDownload> callback) {
		if (UUID.isZeroOrNull(meshID) || callback == null)
			return false;

		MeshDownload download = null;

		// Do we have this mesh asset in the cache?
		byte[] data = cache.get(meshID, "mesh");
		if (data != null) {
			download = new MeshDownload();
			download.itemID = meshID;
			download.assetType = AssetType.Mesh;
			download.assetData = data;
			callback.callback(download);
			return true;
		}

		URI url = client.network.getCapabilityURI("GetMesh");
		if (url != null) {
			synchronized (activeDownloads) {
				download = (MeshDownload) activeDownloads.get(meshID);
				if (download != null) {
					download.callbacks.add(callback);
					return true;
				}
				download = new MeshDownload();
				download.itemID = meshID;
				download.suffix = "mesh";
				download.callbacks = new CallbackHandler<>();
				download.callbacks.add(callback);
				activeDownloads.put(meshID, download);
			}

			try {
				url = new URI(String.format("%s/?mesh_id=%s", url, meshID));

				Callback<DownloadResult> downloadCallback = new MeshDownloadCallback(download);
				httpDownloads.enque(url, client.settings.CAPS_TIMEOUT, null,
						cache.cachedAssetFile(download.itemID, download.suffix), downloadCallback);
				return true;
			} catch (URISyntaxException ex) {
				logger.warn(GridClient.Log("Failed to fetch mesh asset {c}: " + ex.getMessage(), client));
				callback.callback(null);
			}
		} else {
			logger.error(GridClient.Log("GetMesh capability not available", client));
			callback.callback(null);
		}
		return false;
	}

	/**
	 * Fetch avatar texture on a grid capable of server side baking
	 *
	 * @param avatarID
	 *            ID of the avatar
	 * @param textureID
	 *            ID of the texture
	 * @param bakeName
	 *            Name of the part of the avatar texture applies to
	 * @param callback
	 *            Callback invoked on operation completion
	 * @throws URISyntaxException
	 * @return true if image could be retrieved from the cache or the texture
	 *         request could be successfully sent
	 */
	public boolean requestServerBakedImage(UUID avatarID, final UUID textureID, String bakeName,
			final Callback<ImageDownload> callback) throws URISyntaxException {
		if (UUID.isZeroOrNull(avatarID) || UUID.isZeroOrNull(textureID) || callback == null)
			return false;

		final ImageDownload download = new ImageDownload();
		download.itemID = textureID;
		download.assetType = AssetType.Texture;
		download.imageType = ImageType.ServerBaked;
		download.suffix = "tex";

		byte[] assetData = cache.get(textureID, download.suffix);
		// Do we have this image in the cache?
		if (assetData != null) {
			download.state = TextureRequestState.Finished;
			download.assetData = assetData;
			callback.callback(download);

			fireImageProgressEvent(textureID, assetData.length, assetData.length);
			return true;
		}

		String appearenceUri = client.network.getAgentAppearanceServiceURL();
		if (appearenceUri == null || appearenceUri.isEmpty()) {
			download.state = TextureRequestState.NotFound;
			callback.callback(download);
			return false;
		}
		URI url = new URI(appearenceUri + "texture/" + avatarID + "/" + bakeName + "/" + textureID);
		Callback<DownloadResult> downloadCallback = new Callback<DownloadResult>() {
			@Override
			public boolean callback(DownloadResult result) {
				if (result.finished) {
					if (result.data != null) // success
					{
						download.state = TextureRequestState.Finished;
						download.assetData = result.data;
						callback.callback(download);

						fireImageProgressEvent(textureID, result.data.length, result.data.length);
					} else {
						download.state = TextureRequestState.Timeout;
						download.assetData = result.data;
						callback.callback(download);
						logger.warn(
								GridClient.Log("Failed to fetch server bake {" + textureID + "}: empty data", client));
					}
				} else {
					fireImageProgressEvent(textureID, result.current, result.full);
				}
				return result.finished;
			}

		};
		httpDownloads.enque(url, client.settings.CAPS_TIMEOUT, "image/x-j2c",
				cache.cachedAssetFile(download.itemID, download.suffix), downloadCallback);
		return true;
	}

	/**
	 * Lets TexturePipeline class fire the progress event
	 *
	 * @param texureID
	 *            The texture ID currently being downloaded
	 * @param transferredBytes
	 *            The number of bytes transferred
	 * @param totalBytes
	 *            The total number of bytes expected
	 */
	public final void fireImageProgressEvent(UUID texureID, long transferredBytes, long totalBytes) {
		try {
			onImageReceiveProgress
					.dispatch(new ImageReceiveProgressCallbackArgs(texureID, transferredBytes, totalBytes));
		} catch (Throwable ex) {
			logger.error(GridClient.Log(ex.getMessage(), client), ex);
		}
	}

	/**
	 * Helper method for downloading textures via GetTexture cap Same signature as
	 * the UDP variant since we need all the params to pass to the UDP
	 * TexturePipeline in case we need to fall back to it Linden servers currently
	 * (1.42) don't support bakes downloads via HTTP)
	 *
	 * @param textureID
	 * @param imageType
	 * @param priority
	 * @param discardLevel
	 * @param packetStart
	 * @param callback
	 * @param progress
	 */
	private boolean httpRequestTexture(final ImageDownload download) {
		try {
			URI url = new URI(
					String.format("%s/?texture_id=%s", client.network.getCapabilityURI("GetTexture"), download.itemID));
			Callback<DownloadResult> downloadCallback = new Callback<DownloadResult>() {
				@Override
				public boolean callback(DownloadResult result) {
					if (result.finished) {
						if (result.data != null) // success
						{
							synchronized (activeDownloads) {
								activeDownloads.remove(download.itemID);
							}

							download.codec = ImageCodec.J2K;
							download.state = TextureRequestState.Finished;
							download.assetData = result.data;
							download.callbacks.dispatch(download);

							fireImageProgressEvent(download.itemID, result.data.length, result.data.length);
						} else {
							download.state = TextureRequestState.Pending;
							download.callbacks.dispatch(download);
							logger.warn(GridClient
									.Log(String.format("Failed to fetch texture {%s} over HTTP, falling back to UDP",
											download.itemID), client));
							texDownloads.requestTexture(download);
						}
					} else {
						fireImageProgressEvent(download.itemID, result.current, result.full);
					}
					return result.finished;
				}

			};
			httpDownloads.enque(url, client.settings.CAPS_TIMEOUT, "image/x-j2c",
					cache.cachedAssetFile(download.itemID, download.suffix), downloadCallback);
			return true;

		} catch (URISyntaxException ex) {
			download.state = TextureRequestState.Pending;
			download.callbacks.dispatch(download);
			logger.warn(
					GridClient.Log(String.format("Failed to fetch texture {%s} over HTTP, falling back to UDP: {%s}",
							download.itemID, ex.getMessage()), client));
		}
		return false;
	}

	// #region Helpers
	public static AssetItem createAssetItem(AssetType type, UUID assetID, byte[] assetData) {
		try {
			switch (type) {
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
				logger.error("Unimplemented asset type: " + type);
			}
		} catch (Exception ex) {
			logger.error("Exception occurred when creating an asset", ex);
		}
		return new AssetMutable(type, assetID, assetData);
	}
	// #endregion Helpers

	// #region old asset transfer system

	private boolean processDelayedData(AssetDownload download, DelayedTransfer data) {
		while (data != null) {
			System.arraycopy(data.data, 0, download.assetData, download.transferred, data.data.length);
			download.transferred += data.data.length;
			if (data.status == StatusCode.OK && download.transferred >= download.size) {
				download.status = StatusCode.Done;
			} else {
				download.status = data.status;
			}

			if (download.status != StatusCode.OK) {
				synchronized (activeDownloads) {
					assetTransfers.remove(download.transactionID);
					activeDownloads.remove(download.itemID);
				}
				download.delayed.clear();

				download.success = download.status == StatusCode.Done;
				if (download.success) {
					logger.debug(
							GridClient.Log("Transfer for asset " + download.itemID.toString() + " completed", client));

					// Cache successful asset download
					cache.put(download.itemID, download.assetData, download.suffix);
				} else {
					logger.warn(GridClient.Log("Transfer failed with status code " + download.status, client));
				}

				download.callbacks.dispatch(download);
				return true;
			}
			download.packetNum++;
			data = download.delayed.remove(download.packetNum);
		}
		return false;
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @throws Exception
	 */
	private void handleTransferInfo(Packet packet, Simulator simulator) throws Exception {
		TransferInfoPacket info = (TransferInfoPacket) packet;

		AssetDownload download = null;
		synchronized (activeDownloads) {
			download = (AssetDownload) assetTransfers.get(info.TransferInfo.TransferID);
		}
		if (download == null) {
			logger.warn(GridClient.Log("Received a TransferInfo packet for an asset we didn't request, TransferID: "
					+ info.TransferInfo.TransferID, client));
			return;
		}

		download.channel = ChannelType.setValue(info.TransferInfo.ChannelType);
		download.status = StatusCode.setValue(info.TransferInfo.Status);
		download.target = TargetType.setValue(info.TransferInfo.TargetType);
		download.size = info.TransferInfo.Size;

		if (download.status != StatusCode.OK) {
			logger.warn(GridClient.Log("Transfer failed with status code " + download.status, client));

			synchronized (activeDownloads) {
				assetTransfers.remove(download.transactionID);
				activeDownloads.remove(download.itemID);
			}
			download.delayed.clear();

			// No valid data could have been received before the TransferInfo packet
			download.assetData = null;

			// Fire the event with our transfer that contains Success = false;
			download.callbacks.dispatch(download);
		} else {
			download.assetData = new byte[download.size];
			byte[] data = info.TransferInfo.getParams();

			if (download.source == SourceType.Asset && data.length == 20) {
				download.itemID = new UUID(data, 0);
				download.assetType = AssetType.setValue(Helpers.BytesToInt32L(data, 16));

				logger.debug(String.format("TransferInfo packet received. AssetID: %s Type: %s", download.itemID,
						download.assetType));
			} else if (download.source == SourceType.SimInventoryItem && data.length == 100) {
				// TODO: Can we use these?
				UUID agentID = new UUID(data, 0);
				UUID sessionID = new UUID(data, 16);
				UUID ownerID = new UUID(data, 32);
				UUID taskID = new UUID(data, 48);
				UUID itemID = new UUID(data, 64);
				download.itemID = new UUID(data, 80);
				download.assetType = AssetType.setValue(Helpers.BytesToInt32L(data, 96));

				logger.debug(String.format(
						"TransferInfo packet received. AgentID: %s SessionID: %s OwnerID: %s TaskID: %s ItemID: %s AssetID: %s Type: %s",
						agentID, sessionID, ownerID, taskID, itemID, download.itemID, download.assetType));
			} else {
				logger.warn(GridClient.Log(String.format(
						"Received a TransferInfo packet with a SourceType of %s and a Params field length of %d",
						download.source, data.length), client));
			}
			processDelayedData(download, download.delayed.remove(download.packetNum));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @throws Exception
	 */
	private void handleTransferPacket(Packet packet, Simulator simulator) throws Exception {
		TransferPacketPacket asset = (TransferPacketPacket) packet;

		AssetDownload download = null;
		synchronized (activeDownloads) {
			download = (AssetDownload) assetTransfers.get(asset.TransferData.TransferID);
		}
		if (download != null) {
			StatusCode status = StatusCode.setValue(asset.TransferData.Status);
			DelayedTransfer info = new DelayedTransfer(status, asset.TransferData.getData());

			if (!download.gotInfo() || asset.TransferData.Packet != download.packetNum) {
				/*
				 * We haven't received the header yet, or the packet number is higher than the
				 * currently expected packet. Put it in the out of order hashlist
				 */
				download.delayed.put(asset.TransferData.Packet, info);
			} else {
				processDelayedData(download, info);
			}
		}
	}

	// /#endregion Transfer Callbacks

	// /#region Xfer Callbacks

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private void handleInitiateDownloadPacket(Packet packet, Simulator simulator) {
		InitiateDownloadPacket request = (InitiateDownloadPacket) packet;
		try {
			onInitiateDownload
					.dispatch(new InitiateDownloadCallbackArgs(Helpers.BytesToString(request.FileData.getSimFilename()),
							Helpers.BytesToString(request.FileData.getViewerFilename())));
		} catch (Exception ex) {
			logger.error(GridClient.Log(ex.getMessage(), client), ex);
		}
	}

	private void sendNextUploadPacket(AssetUpload upload) throws Exception {
		SendXferPacketPacket send = new SendXferPacketPacket();

		send.XferID.ID = upload.xferID;
		send.XferID.Packet = upload.packetNum++;

		// The first packet reserves the first four bytes of the data for the
		// total length of the asset and appends 1000 bytes of data after that
		int off = send.XferID.Packet == 0 ? 4 : 0, len = 1000;
		if (upload.transferred + len >= upload.size) {
			// Last packet
			len = upload.size - upload.transferred;
			send.XferID.Packet |= 0x80000000; // This signals the final packet
		}

		byte[] data = new byte[off + len];
		if (send.XferID.Packet == 0)
			Helpers.Int32ToBytesL(upload.size, data, 0);
		System.arraycopy(upload.assetData, upload.transferred, data, off, len);
		send.DataPacket.setData(data);
		upload.transferred += len;

		send.DataPacket.setData(data);
		client.network.sendPacket(send);
	}

	private void sendConfirmXferPacket(long xferID, int packetNum) throws Exception {
		ConfirmXferPacketPacket confirm = new ConfirmXferPacketPacket();
		confirm.XferID.ID = xferID;
		confirm.XferID.Packet = packetNum;

		client.network.sendPacket(confirm);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @throws Exception
	 */
	private void handleRequestXfer(Packet packet, Simulator simulator) throws Exception {
		RequestXferPacket request = (RequestXferPacket) packet;
		AssetUpload upload = pendingUpload.poll();
		if (upload == null) {
			logger.warn(GridClient.Log("Received a RequestXferPacket for an unknown asset upload", client));
			return;
		}

		upload.xferID = request.XferID.ID;
		upload.assetType = AssetType.setValue(request.XferID.VFileType);

		synchronized (xferTransfers) {
			xferTransfers.put(upload.xferID, upload);
		}
		// Send the first packet containing actual asset data
		sendNextUploadPacket(upload);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @throws Exception
	 */
	private void handleConfirmXferPacket(Packet packet, Simulator simulator) throws Exception {
		ConfirmXferPacketPacket confirm = (ConfirmXferPacketPacket) packet;

		AssetUpload upload = null;
		synchronized (xferTransfers) {
			upload = (AssetUpload) xferTransfers.get(confirm.XferID.ID);
		}
		logger.debug(String.format("ACK for upload %s of asset type %s (%d/%d)", upload.assetID, upload.assetType,
				upload.transferred, upload.size));

		onUploadProgress.dispatch(upload);

		if (upload.transferred < upload.size) {
			sendNextUploadPacket(upload);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private void handleAssetUploadComplete(Packet packet, Simulator simulator) {
		AssetUploadCompletePacket complete = (AssetUploadCompletePacket) packet;

		AssetUpload upload = null;
		synchronized (activeDownloads) {
			upload = (AssetUpload) assetTransfers.remove(complete.AssetBlock.UUID);
		}
		if (upload != null) {
			synchronized (xferTransfers) {
				xferTransfers.remove(upload.xferID);
			}
			onAssetUploaded.dispatch(upload);
		} else {
			logger.debug(String.format(
					"Got an AssetUploadComplete on an unrecognized asset, AssetID: %s, Type: %s, Success: %s",
					complete.AssetBlock.UUID, AssetType.setValue(complete.AssetBlock.Type),
					complete.AssetBlock.Success));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("unlikely-arg-type")
	private void handleSendXferPacket(Packet packet, Simulator simulator) throws Exception {
		SendXferPacketPacket xfer = (SendXferPacketPacket) packet;
		XferDownload download = null;
		synchronized (xferTransfers) {
			download = (XferDownload) xferTransfers.get(xfer.XferID.ID);
		}
		if (download != null) {
			// Apply a mask to get rid of the "end of transfer" bit
			int packetNum = xfer.XferID.Packet & 0x7FFFFFFF;

			// Check for out of order packets, possibly indicating a resend
			if (packetNum != download.packetNum) {
				if (packetNum == download.packetNum - 1) {
					logger.debug(
							GridClient.Log("Resending Xfer download confirmation for packet " + packetNum, client));
					sendConfirmXferPacket(download.xferID, packetNum);
				} else {
					logger.warn(GridClient.Log("Out of order Xfer packet in a download, got " + packetNum
							+ " expecting " + download.packetNum, client));
					// Re-confirm the last packet we actually received
					sendConfirmXferPacket(download.xferID, download.packetNum - 1);
				}
				return;
			}

			byte[] bytes = xfer.DataPacket.getData();
			if (packetNum == 0) {
				// This is the first packet received in the download, the first
				// four bytes are a size integer
				// in little endian ordering
				download.size = Helpers.BytesToInt32L(bytes);
				download.assetData = new byte[download.size];

				logger.debug("Received first packet in an Xfer download of size " + download.size);

				System.arraycopy(bytes, 4, download.assetData, 0, bytes.length - 4);
				download.transferred += bytes.length - 4;
			} else {
				System.arraycopy(bytes, 0, download.assetData, 1000 * packetNum, bytes.length);
				download.transferred += bytes.length;
			}

			// Increment the packet number to the packet we are expecting next
			download.packetNum++;

			// Confirm receiving this packet
			sendConfirmXferPacket(download.xferID, packetNum);

			if ((xfer.XferID.Packet & 0x80000000) != 0) {
				// This is the last packet in the transfer
				if (!Helpers.isEmpty(download.filename)) {
					logger.debug(GridClient.Log("Xfer download for asset " + download.filename + " completed", client));
				} else {
					logger.debug(GridClient.Log("Xfer download for asset " + download.itemID.toString() + " completed",
							client));
				}

				download.success = true;
				synchronized (xferTransfers) {
					xferTransfers.remove(download.transactionID);
				}
				onXferReceived.dispatch(download);
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private void handleAbortXfer(Packet packet, Simulator simulator) {
		AbortXferPacket abort = (AbortXferPacket) packet;
		XferDownload download = null;

		synchronized (xferTransfers) {
			download = (XferDownload) xferTransfers.remove(abort.XferID.ID);
		}

		if (download != null && onXferReceived.count() > 0) {
			download.success = false;
			download.error = TransferError.setValue(abort.XferID.Result);

			onXferReceived.dispatch(download);
		}
	}
	// #endregion Xfer Callbacks
}
