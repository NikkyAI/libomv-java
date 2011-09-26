/**
 * Copyright (c) 2009, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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

import libomv.types.UUID;

/** Base class for all Asset types */
public abstract class AssetItem
{
	/** The different types of grid assets */
	public enum AssetType
	{
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
		/** Holds a collection of inventory items */
		Folder(8),
		/** Root inventory folder */
		RootFolder(9),
		/** Linden scripting language script */
		LSLText(10),
		/** LSO bytecode for a script */
		LSLBytecode(11),
		/** Uncompressed TGA texture */
		TextureTGA(12),
		/** Collection of textures and shape parameters that can be worn */
		Bodypart(13),
		/** Trash folder */
		TrashFolder(14),
		/** Snapshot folder */
		SnapshotFolder(15),
		/** Lost and found folder */
		LostAndFoundFolder(16),
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
		/** Contains landmarks for favorites */
		FavoriteFolder(23),
		/** Asset is a link to another inventory item */
		Link(24),
		/** Asset is a link to another inventory folder */
		LinkFolder(25),
		/** Beginning of the range reserved for ensembles */
		EnsembleStart(26),
		/** End of the range reserved for ensembles */
		EnsembleEnd(45),
		/**
		 * Folder containing inventory links to wearables and attachments that
		 * are part of the current outfit
		 */
		CurrentOutfitFolder(46),
		/**
		 * Folder containing inventory items or links to inventory items of
		 * wearables and attachments together make a full outfit
		 */
		OutfitFolder(47),
		/** Root folder for the folders of type OutfitFolder */
		MyOutfitsFolder(48),
		/** */
		InboxFolder(49);

		private static final String[] _AssetTypeNames = new String[] { "texture", "sound", "callcard", "landmark",
				"script", "clothing", "object", "notecard", "category", "root", "lsltext", "lslbyte", "txtr_tga",
				"bodypart", "trash", "snapshot", "lstndfnd", "snd_wav", "img_tga", "jpeg", "animatn", "gesture",
				"simstate", "favorites", "link", "linkfldr" };

		/**
		 * Translate a string name of an AssetType into the proper Type
		 * 
		 * @param type
		 *            A string containing the AssetType name
		 * @return The AssetType which matches the string name, or
		 *         AssetType.Unknown if no match was found
		 */
		public static AssetType setValue(String value)
		{
			int i = 0;
			for (String name : _AssetTypeNames)
			{
				i++;
				if (name.compareToIgnoreCase(value) == 0)
				{
					return values()[i];
				}
			}
			return Unknown;
		}

		public static AssetType setValue(int value)
		{
			for (AssetType e : values())
			{
				if (e._value == value)
					return e;
			}
			return Unknown;
		}

		public byte getValue()
		{
			return _value;
		}

		@Override
		public String toString()
		{
			int i = ordinal() - 1;
			if (i >= 0 && ordinal() < _AssetTypeNames.length)
				return _AssetTypeNames[i];
			return "unknown";
		}

		private final byte _value;

		private AssetType(int value)
		{
			this._value = (byte) value;
		}
	}

	/** A byte array containing the raw asset data */
	public byte[] AssetData;
	/** True if the asset is only stored on the server temporarily */
	public boolean Temporary;
	/** The assets unique unique ID */
	protected UUID AssetID;

	public UUID getAssetID()
	{
		return AssetID;
	}

	public void setAssetID(UUID value)
	{
		AssetID = value;
	}

	/** The "type" of asset, Notecard, Animation, etc */
	public abstract AssetType getAssetType();

	/**
	 * Construct a new Asset object
	 */
	public AssetItem()
	{
	}

	/**
	 * Construct a new Asset object
	 * 
	 * @param assetID
	 *            A unique <see cref="UUID"/> specific to this asset
	 * @param assetData
	 *            A byte array containing the raw asset data
	 */
	public AssetItem(UUID assetID, byte[] assetData)
	{
		AssetID = assetID;
		AssetData = assetData;
	}

	/**
	 * Regenerates the <code>AssetData</code> byte array from the properties of
	 * the derived class.
	 */
	public abstract void Encode();

	/**
	 * Decodes the AssetData, placing it in appropriate properties of the
	 * derived class.
	 * 
	 * @return True if the asset decoding succeeded, otherwise false
	 */
	public abstract boolean Decode();
}
