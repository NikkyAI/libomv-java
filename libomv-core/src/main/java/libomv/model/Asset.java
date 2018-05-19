package libomv.model;

import java.util.HashMap;

import libomv.imaging.ManagedImage.ImageCodec;
import libomv.model.Texture.TextureRequestState;
import libomv.types.UUID;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;

public interface Asset {

	/** The different types of grid assets */
	public enum AssetType {
		/** Unknown asset type */
		Unknown(-1),
		/** Texture asset, stores in JPEG2000 J2C stream format */
		Texture(0),
		/** Sound asset */
		Sound(1),
		/** Calling card for another avatar */
		CallingCard(2),
		/** Link to a location in world */
		Landmark(3),
		// [Obsolete] Legacy script asset, you should never see one of these
		Script(4),
		/** Collection of textures and parameters that can be worn by an avatar */
		Clothing(5),
		/** Primitive that can contain textures, sounds, scripts and more */
		Object(6),
		/** Notecard asset */
		Notecard(7),
		/** Holds a collection of inventory items. "Category" in the Linden viewer */
		Folder(8),
		/** Linden scripting language script */
		LSLText(10),
		/** LSO bytecode for a script */
		LSLBytecode(11),
		/** Uncompressed TGA texture */
		TextureTGA(12),
		/** Collection of textures and shape parameters that can be worn */
		Bodypart(13),
		/** Uncompressed sound */
		SoundWAV(17),
		/** Uncompressed TGA non-square image, not to be used as a texture */
		ImageTGA(18),
		/** Compressed JPEG non-square image, not to be used as a texture */
		ImageJPEG(19),
		/** Animation */
		Animation(20),
		/** Sequence of animations, sounds, chat, and pauses */
		Gesture(21),
		/** Simstate file */
		Simstate(22),
		/** Asset is a link to another inventory item */
		Link(24),
		/** Asset is a link to another inventory folder */
		LinkFolder(25),
		/** Marketplace Folder. Same as an Category but different display methods */
		MarketplaceFolder(26),
		/** Linden mesh format */
		Mesh(49);

		private static final String[] _AssetTypeNames = new String[] { "texture", // 0
				"sound", // 1
				"callcard", // 2
				"landmark", // 3
				"script", // 4
				"clothing", // 5
				"object", // 6
				"notecard", // 7
				"category", // 8
				Helpers.EmptyString, // 9
				"lsltext", // 10
				"lslbyte", // 11
				"txtr_tga", // 12
				"bodypart", // 13
				Helpers.EmptyString, // 14
				Helpers.EmptyString, // 15
				Helpers.EmptyString, // 16
				"snd_wav", // 17
				"img_tga", // 18
				"jpeg", // 19
				"animatn", // 20
				"gesture", // 21
				"simstate", // 22
				Helpers.EmptyString, // 23
				"link", // 24
				"link_f", // 25
				Helpers.EmptyString, // 26
				Helpers.EmptyString, // 27
				Helpers.EmptyString, // 28
				Helpers.EmptyString, // 29
				Helpers.EmptyString, // 30
				Helpers.EmptyString, // 31
				Helpers.EmptyString, // 32
				Helpers.EmptyString, // 33
				Helpers.EmptyString, // 34
				Helpers.EmptyString, // 35
				Helpers.EmptyString, // 36
				Helpers.EmptyString, // 37
				Helpers.EmptyString, // 38
				Helpers.EmptyString, // 39
				Helpers.EmptyString, // 40
				Helpers.EmptyString, // 41
				Helpers.EmptyString, // 42
				Helpers.EmptyString, // 43
				Helpers.EmptyString, // 44
				Helpers.EmptyString, // 45
				Helpers.EmptyString, // 46
				Helpers.EmptyString, // 47
				Helpers.EmptyString, // 48
				"mesh", // 49
		};

		/**
		 * Translate a string name of an AssetType into the proper Type
		 *
		 * @param type
		 *            A string containing the AssetType name
		 * @return The AssetType which matches the string name, or AssetType.Unknown if
		 *         no match was found
		 */
		public static AssetType setValue(String value) {
			if (value != null) {
				try {
					return setValue(Integer.parseInt(value, 10));
				} catch (NumberFormatException ex) {
				}

				int i = 0;
				for (String name : _AssetTypeNames) {
					i++;
					if (name.compareToIgnoreCase(value) == 0) {
						return values()[i];
					}
				}
			}
			return Unknown;
		}

		public static AssetType setValue(int value) {
			for (AssetType e : values()) {
				if (e._value == value)
					return e;
			}
			return Unknown;
		}

		public byte getValue() {
			return _value;
		}

		@Override
		public String toString() {
			int i = ordinal() - 1;
			if (i >= 0 && ordinal() < _AssetTypeNames.length)
				return _AssetTypeNames[i];
			return "unknown";
		}

		private final byte _value;

		private AssetType(int value) {
			this._value = (byte) value;
		}
	}

	// #region Enums
	public enum EstateAssetType {
		None, Covenant;

		public static EstateAssetType setValue(int value) {
			return values()[value + 1];
		}

		public static byte getValue(EstateAssetType value) {
			return (byte) (value.ordinal() - 1);
		}

		public byte getValue() {
			return (byte) (ordinal() - 1);
		}
	}

	public enum StatusCode {
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

		public static StatusCode setValue(int value) {
			for (StatusCode e : values()) {
				if (e._value == value)
					return e;
			}
			return Unknown;
		}

		public static byte getValue(StatusCode value) {
			for (StatusCode e : values()) {
				if (e == value)
					return e._value;
			}
			return Unknown._value;
		}

		public byte getValue() {
			return _value;
		}

		private final byte _value;

		private StatusCode(int value) {
			_value = (byte) value;
		}
	}

	public enum ChannelType {
		Unknown,
		// Unknown
		Misc,
		// Virtually all asset transfers use this channel
		Asset;

		public static ChannelType setValue(int value) {
			return values()[value];
		}

		public static byte getValue(ChannelType value) {
			return (byte) value.ordinal();
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	public enum SourceType {
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

		public static SourceType setValue(int value) {
			return values()[value];
		}

		public static byte getValue(SourceType value) {
			return (byte) value.ordinal();
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	public enum TargetType {
		Unknown, File, VFile;

		public static TargetType setValue(int value) {
			return values()[value];
		}

		public static byte getValue(TargetType value) {
			return (byte) value.ordinal();
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	// When requesting image download, type of the image requested
	public enum ImageType {
		// Normal in-world object texture
		Normal,
		// Local baked avatar texture
		Baked,
		// Server baked avatar texture
		ServerBaked;

		public static ImageType setValue(int value) {
			return values()[value];
		}

		public static byte getValue(ImageType value) {
			return (byte) value.ordinal();
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	public enum TransferError {
		None(0), Failed(-1), AssetNotFound(-3), AssetNotFoundInDatabase(-4), InsufficientPermissions(-5), EOF(
				-39), CannotOpenFile(-42), FileNotFound(-43), FileIsEmpty(-44), TCPTimeout(-23016), CircuitGone(-23017);

		public static TransferError setValue(int value) {
			for (TransferError e : values()) {
				if (e._value == value)
					return e;
			}
			return Failed;
		}

		public static byte getValue(TransferError value) {
			for (TransferError e : values()) {
				if (e == value)
					return e._value;
			}
			return Failed._value;
		}

		public byte getValue() {
			return _value;
		}

		private final byte _value;

		private TransferError(int value) {
			_value = (byte) value;
		}
	}

	// #region Transfer Classes
	public class DelayedTransfer {
		public StatusCode Status;
		public byte[] Data;

		public DelayedTransfer(StatusCode status, byte[] data) {
			this.Status = status;
			this.Data = data;
		}
	}

	// TODO:FIXME Changing several fields to public, they need getters instead!
	public class Transfer {
		public UUID ItemID;
		public int Size;
		public AssetType AssetType;
		public byte[] AssetData;

		public UUID TransactionID; // protected
		public int Transferred; // protected
		public int PacketNum; // protected
		public boolean Success;
		public long TimeSinceLastPacket; // protected
		public HashMap<Integer, DelayedTransfer> delayed; // protected
		public String suffix;

		public Transfer() {
			AssetData = Helpers.EmptyBytes;
			delayed = new HashMap<Integer, DelayedTransfer>();
		}
	}

	public class XferDownload extends Transfer {
		public long XferID;
		public String Filename = Helpers.EmptyString;
		public TransferError Error = TransferError.None;

		public XferDownload() {
			super();
		}
	}

	// TODO:FIXME
	// Changing several fields to public, they need getters instead!
	public class AssetDownload extends Transfer {
		public ChannelType Channel;
		public SourceType Source;
		public TargetType Target;
		public StatusCode Status;
		public float Priority;
		public Simulator Simulator; // private
		public CallbackHandler<AssetDownload> callbacks; // private

		public AssetDownload() {
			super();
		}

		public boolean gotInfo() {
			return Size > 0;
		}
	}

	public class ImageDownload extends Transfer {
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

		public ImageDownload() {
			super();
		}

		public boolean gotInfo() {
			return Size > 0;
		}
	}

	// TODO:FIXME
	// Changing several fields to public, they need getters instead!
	public class MeshDownload extends Transfer {
		public UUID ItemID;
		public CallbackHandler<MeshDownload> callbacks; // private

		public MeshDownload() {
			super();
		}
	}

	public class AssetUpload extends Transfer {
		public UUID AssetID;
		public long XferID;

		public AssetUpload() {
			super();
		}
	}

	public class ImageRequest {
		public UUID ImageID;
		public ImageType Type;
		public float Priority;
		public int DiscardLevel;

		public ImageRequest(UUID imageid, ImageType type, float priority, int discardLevel) {
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
	public abstract class BakedTextureUploadedCallback {
		abstract public void callback(UUID newAssetID);
	}

	// #endregion Callback

	// #region Callback

	/** The "type" of asset, Notecard, Animation, etc */
	public AssetType getAssetType();

}
