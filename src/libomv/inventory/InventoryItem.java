/**
 * Copyright (c) 2006-2008, openmetaverse.org
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
package libomv.inventory;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import libomv.ObjectManager.SaleType;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.assets.AssetItem.AssetType;
import libomv.types.Permissions;
import libomv.types.UUID;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

/* An Item in Inventory */
public class InventoryItem extends InventoryBase
{
	//
	private static final long serialVersionUID = 1L;

	/** Inventory Item Types, eg Script, Notecard, Folder, etc */
	public enum InventoryType
	{
		/** Unknown */
		Unknown(-1),
		/** Texture */
		Texture(0),
		/** Sound */
		Sound(1),
		/** Calling Card */
		CallingCard(2),
		/** Landmark */
		Landmark(3),
		// [Obsolete("See LSL")]
		Script(4),
		// [Obsolete("See Wearable")]
		Clothing(5),
		/** Object */
		Object(6),
		/** Notecard */
		Notecard(7),
		/** */
		Category(8),
		/** Folder */
		Folder(8),
		/** */
		RootCategory(9),
		/** an LSL Script */
		LSL(10),
		// [Obsolete("See LSL")] LSLBytecode = 11,
		// [Obsolete("See Texture")] TextureTGA = 12,
		// [Obsolete] Bodypart = 13,
		// [Obsolete] Trash = 14,
		/** */
		Snapshot(15),
		// [Obsolete] LostAndFound = 16,
		/** */
		Attachment(17),
		/** */
		Wearable(18),
		/** */
		Animation(19),
		/**	*/
		Gesture(20);

		private static final String[] _InventoryTypeNames = new String[] { "texture", "sound", "callcard", "landmark",
				"script", "clothing", "object", "notecard", "category", "root", "script", "", "", "", "", "snapshot",
				"", "attach", "wearable", "animation", "gesture" };

		/**
		 * Translate a string name of an AssetType into the proper Type
		 * 
		 * @param type
		 *            A string containing the AssetType name
		 * @return The AssetType which matches the string name, or
		 *         AssetType.Unknown if no match was found
		 */
		public static InventoryType setValue(String value)
		{
			for (int i = 0; i < _InventoryTypeNames.length; i++)
			{
				if (value.compareToIgnoreCase(_InventoryTypeNames[i]) == 0)
				{
					return values()[i + 1];
				}
			}
			return Unknown;
		}

		public static InventoryType setValue(int value)
		{
			for (InventoryType e : values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return _value;
		}

		@Override
		public String toString()
		{
			int i = ordinal() - 1;
			if (i >= 0 && ordinal() < _InventoryTypeNames.length)
				return _InventoryTypeNames[i];
			return "unknown";
		}

		private final byte _value;

		private InventoryType(int value)
		{
			this._value = (byte) value;
		}
	}

	/** Types of wearable assets */
	public enum WearableType
	{
		/** Invalid wearable asset */
		Invalid(-1),
		/** Body shape */
		Shape(0),
		/** Skin textures and attributes */
		Skin(1),
		/** Hair */
		Hair(2),
		/** Eyes */
		Eyes(3),
		/** Shirt */
		Shirt(4),
		/** Pants */
		Pants(5),
		/** Shoes */
		Shoes(6),
		/** Socks */
		Socks(7),
		/** Jacket */
		Jacket(8),
		/** Gloves */
		Gloves(9),
		/** Undershirt */
		Undershirt(10),
		/** Underpants */
		Underpants(11),
		/** Skirt */
		Skirt(12),
		/** Alpha mask to hide parts of the avatar */
		Alpha(13),
		/** Tattoo */
		Tattoo(14);

		public static WearableType setValue(int value)
		{
			for (WearableType e : values())
			{
				if (e._value == value)
					return e;
			}
			return Invalid;
		}

		public byte getValue()
		{
			return _value;
		}

		private final byte _value;

		private WearableType(int value)
		{
			_value = (byte) value;
		}
	}

	/* Upper half of the Flags field for inventory items */
	// [Flags]
	public static class InventoryItemFlags
	{
		public static final int None = 0;
		/*
		 * Indicates that the NextOwner permission will be set to the most
		 * restrictive set of permissions found in the object set (including
		 * linkset items and object inventory items) on next rez
		 */
		public static final int ObjectSlamPerm = 0x100;
		/* Indicates that the object sale information has been changed */
		public static final int ObjectSlamSale = 0x1000;
		/*
		 * If set, and a slam bit is set, indicates BaseMask will be overwritten
		 * on Rez
		 */
		public static final int ObjectOverwriteBase = 0x010000;
		/*
		 * If set, and a slam bit is set, indicates OwnerMask will be
		 * overwritten on Rez
		 */
		public static final int ObjectOverwriteOwner = 0x020000;
		/*
		 * If set, and a slam bit is set, indicates GroupMask will be
		 * overwritten on Rez
		 */
		public static final int ObjectOverwriteGroup = 0x040000;
		/*
		 * If set, and a slam bit is set, indicates EveryoneMask will be
		 * overwritten on Rez
		 */
		public static final int ObjectOverwriteEveryone = 0x080000;
		/*
		 * If set, and a slam bit is set, indicates NextOwnerMask will be
		 * overwritten on Rez
		 */
		public static final int ObjectOverwriteNextOwner = 0x100000;
		/* Indicates whether this object is composed of multiple items or not */
		public static final int ObjectHasMultipleItems = 0x200000;
		/*
		 * Indicates that the asset is only referenced by this inventory item.
		 * If this item is deleted or updated to reference a new assetID, the
		 * asset can be deleted
		 */
		public static final int SharedSingleReference = 0x40000000;

		public static int setValue(int value)
		{
			return value & _mask;
		}

		public static int getValue(int value)
		{
			return value;
		}

		private static final int _mask = 0x7F1100;
	}

	/* The {@link OpenMetaverse.UUID} of this item */
	public UUID AssetID;
	/* The combined {@link OpenMetaverse.Permissions} of this item */
	public Permissions Permissions;
	/* The type of item from {@link OpenMetaverse.AssetType} */
	public AssetType assetType;
	/* The {@link OpenMetaverse.UUID} of the creator of this item */
	public UUID CreatorID;
	/* A Description of this item */
	public String Description;
	/*
	 * The {@link OpenMetaverse.Group} s {@link OpenMetaverse.UUID} this item is
	 * set to or owned by
	 */
	public UUID GroupID;
	/*
	 * If true, item is owned by a group
	 */
	public boolean GroupOwned;
	/* The price this item can be purchased for */
	public int SalePrice;
	/* The type of sale from the {@link OpenMetaverse.SaleType} enum */
	public SaleType saleType;
	/*
	 * Combined flags from {@link libomv.InventoryItem.InventoryItemFlags} and
	 * item specific types
	 */
	public int ItemFlags;
	/*
	 * Time and date this inventory item was created, stored as UTC (Coordinated
	 * Universal Time)
	 */
	public Date CreationDate;
	/* Used to update the AssetID in requests sent to the server */
	// public UUID TransactionID;
	/* The {@link OpenMetaverse.UUID} of the previous owner of the item */
	public UUID LastOwnerID;

	/**
	 * Wrapper for creating a new {@link InventoryItem} object
	 * 
	 * @param type
	 *            The type of item from the {@link InventoryType} enum
	 * @param id
	 *            The {@link UUID} of the newly created object
	 * @return An {@link InventoryItem} object with the type and id passed
	 */
	public static InventoryItem create(InventoryType type, UUID id)
	{
		switch (type)
		{
			case Texture:
				return new InventoryTexture(id);
			case Sound:
				return new InventorySound(id);
			case CallingCard:
				return new InventoryCallingCard(id);
			case Landmark:
				return new InventoryLandmark(id);
			case Object:
				return new InventoryObject(id);
			case Notecard:
				return new InventoryNotecard(id);
			case Category:
				return new InventoryCategory(id);
			case LSL:
				return new InventoryLSL(id);
			case Snapshot:
				return new InventorySnapshot(id);
			case Attachment:
				return new InventoryAttachment(id);
			case Wearable:
				return new InventoryWearable(id);
			case Animation:
				return new InventoryAnimation(id);
			case Gesture:
				return new InventoryGesture(id);
			default:
				try
				{
					return (InventoryItem)Class.forName("Inventory " + type).getConstructor(id.getClass()).newInstance(id);
				}
				catch (Exception ex)
				{
					Logger.Log("Error instantiating an InventoryItem through class name", LogLevel.Error, ex);
				}
		}
		return null;
	}

	/**
	 * Construct a new InventoryItem object of a specific Type
	 * 
	 * @param type
	 *            The type of item from {@link OpenMetaverse.InventoryType}
	 * @param itemID
	 *            {@link OpenMetaverse.UUID} of the item
	 */
	public InventoryItem(UUID itemID)
	{
		super(itemID);
	}

	@Override
	public InventoryType getType()
	{
		return InventoryType.Unknown;
	}
	
	/**
	 * Indicates inventory item is a link
	 * 
	 * @return True if inventory item is a link to another inventory item
	 */
	public final boolean IsLink()
	{
		return assetType == AssetType.Link || assetType == AssetType.LinkFolder;
	}

	public OSD Serialize()
	{
		OSDMap map = (OSDMap) super.toOSD();
		map.put("AssetUUID", OSD.FromUUID(AssetID));
		map.put("Permissions", Permissions.Serialize());
		map.put("AssetType", OSD.FromInteger(assetType.getValue()));
		map.put("CreatorID", OSD.FromUUID(CreatorID));
		map.put("Description", OSD.FromString(Description));
		map.put("GroupID", OSD.FromUUID(GroupID));
		map.put("GroupOwned", OSD.FromBoolean(GroupOwned));
		map.put("SalePrice", OSD.FromInteger(SalePrice));
		map.put("SaleType", OSD.FromInteger(saleType.getValue()));
		map.put("Flags", OSD.FromInteger(ItemFlags));
		map.put("CreationDate", OSD.FromDate(CreationDate));
		map.put("LastOwnerID", OSD.FromUUID(LastOwnerID));
		return map;
	}

	@Override
	public void fromOSD(OSD osd)
	{
		super.fromOSD(osd);
		if (osd instanceof OSDMap)
		{
			OSDMap map = (OSDMap) osd;

			AssetID = map.get("AssetUUID").AsUUID();
			Permissions = new Permissions(map.get("Permissions"));
			assetType = AssetType.setValue(map.get("AssetType").AsInteger());
			CreatorID = map.get("CreatorID").AsUUID();
			Description = map.get("Description").AsString();
			GroupID = map.get("GroupID").AsUUID();
			GroupOwned = map.get("GroupOwned").AsBoolean();
			SalePrice = map.get("SalePrice").AsInteger();
			saleType = SaleType.setValue(map.get("SaleType").AsInteger());
			ItemFlags = map.get("Flags").AsInteger();
			CreationDate = map.get("CreationDate").AsDate();
			LastOwnerID = map.get("LastOwnerID").AsUUID();
		}
	}

	/**
	 * Initializes an InventoryItem object from a serialization stream
	 * 
	 * @param info
	 *            serialization stream
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Override
	protected void readObject(ObjectInputStream info) throws IOException, ClassNotFoundException
	{
		super.readObject(info);
		if (serialVersionUID != info.readLong())
			throw new InvalidObjectException("InventoryItem serial version mismatch");
		AssetID = (UUID) info.readObject();
		Permissions = (Permissions) info.readObject();
		assetType = AssetType.setValue(info.readByte());
		CreatorID = (UUID) info.readObject();
		Description = info.readUTF();
		GroupID = (UUID) info.readObject();
		GroupOwned = info.readBoolean();
		SalePrice = info.readInt();
		saleType = SaleType.setValue(info.readByte());
		ItemFlags = info.readInt();
		CreationDate = (Date) info.readObject();
		LastOwnerID = (UUID) info.readObject();
	}

	/**
	 * Write Serilization data for this InventoryFolder object to the stream
	 * 
	 * @param info
	 *            serialization stream
	 * @throws IOException
	 */
	@Override
	protected void writeObject(ObjectOutputStream info) throws IOException
	{
		super.writeObject(info);
		info.writeLong(serialVersionUID);
		info.writeObject(AssetID);
		info.writeObject(Permissions);
		info.writeByte(assetType.getValue());
		info.writeObject(CreatorID);
		info.writeUTF(Description);
		info.writeObject(GroupID);
		info.writeBoolean(GroupOwned);
		info.writeInt(SalePrice);
		info.writeByte(saleType.getValue());
		info.writeInt(ItemFlags);
		info.writeObject(CreationDate);
		info.writeObject(LastOwnerID);
	}

	/**
	 * Generates a number corresponding to the value of the object to support
	 * the use of a hash table. Suitable for use in hashing algorithms and data
	 * structures such as a hash table
	 * 
	 * @return A Hashcode of all the combined InventoryItem fields
	 */
	@Override
	public int hashCode()
	{
		return AssetID.hashCode() ^ Permissions.hashCode() ^ assetType.hashCode() ^ getType().hashCode()
				^ Description.hashCode() ^ GroupID.hashCode() ^ ((Boolean) GroupOwned).hashCode() ^ SalePrice
				^ saleType.hashCode() ^ ItemFlags ^ CreationDate.hashCode() ^ LastOwnerID.hashCode();
	}

	/**
	 * Compares an object
	 * 
	 * @param o
	 *            The object to compare
	 * @return true if comparison object matches
	 */
	@Override
	public boolean equals(Object o)
	{
		InventoryItem item = (InventoryItem) ((o instanceof InventoryItem) ? o : null);
		return item != null && equals(item);
	}

	/**
	 * Determine whether the specified {@link OpenMetaverse.InventoryBase}
	 * object is equal to the current object
	 * 
	 * @param o
	 *            The {@link OpenMetaverse.InventoryBase} object to compare
	 *            against
	 * @return true if objects are the same
	 */
	@Override
	public boolean equals(InventoryBase o)
	{
		InventoryItem item = (InventoryItem) ((o instanceof InventoryItem) ? o : null);
		return item != null && equals(item);
	}

	/**
	 * Determine whether the specified {@link OpenMetaverse.InventoryItem}
	 * object is equal to the current object
	 * 
	 * @param o
	 *            The {@link OpenMetaverse.InventoryItem} object to compare
	 *            against
	 * @return true if objects are the same
	 */
	public final boolean equals(InventoryItem o)
	{
		return o != null && super.equals(o) && o.assetType.equals(assetType) && o.AssetID.equals(AssetID)
				&& o.CreationDate.equals(CreationDate) && o.Description.equals(Description) && o.ItemFlags == ItemFlags
				&& o.GroupID.equals(GroupID) && o.GroupOwned == GroupOwned && o.getType().equals(getType())
				&& o.Permissions.equals(Permissions) && o.SalePrice == SalePrice && o.saleType.equals(saleType)
				&& o.LastOwnerID.equals(LastOwnerID);
	}

	/*
	 * // Output this item as XML
	 * 
	 * // <param name="outputAssets">Include an asset data as well, //
	 * TRUE/FALSE</param> public String toXML(boolean outputAssets) throws
	 * Exception { String output = "<item ";
	 * 
	 * output += "name = '" + xmlSafe(name) + "' "; output += "uuid = '" + UUID
	 * + "' "; output += "invtype = '" + inventoryType + "' "; output +=
	 * "type = '" + assetType + "' ";
	 * 
	 * output += "description = '" + xmlSafe(description) + "' "; output +=
	 * "crc = '" + CRC + "' "; output += "debug = '" +
	 * InventoryPacketHelper.InventoryUpdateCRC(this) + "' "; output +=
	 * "ownerid = '" + ownerID + "' "; output += "creatorid = '" + creatorID +
	 * "' ";
	 * 
	 * output += "assetid = '" + assetID + "' "; output += "groupid = '" +
	 * groupID + "' ";
	 * 
	 * output += "groupowned = '" + groupOwned + "' "; output +=
	 * "creationdate = '" + creationDate + "' "; output += "flags = '" +
	 * itemFlags + "' ";
	 * 
	 * output += "saletype = '" + saleType + "' "; output += "saleprice = '" +
	 * salePrice + "' "; output += "basemask = '" + permissions.BaseMask + "' ";
	 * output += "everyonemask = '" + permissions.EveryoneMask + "' "; output +=
	 * "nextownermask = '" + permissions.NextOwnerMask + "' "; output +=
	 * "groupmask = '" + permissions.GroupMask + "' "; output += "ownermask = '"
	 * + permissions.OwnerMask + "' ";
	 * 
	 * output += "/>\n";
	 * 
	 * return output; }
	 */
}

/*
 * 1044 ItemData (Variable) 0047 GroupOwned (BOOLEAN / 1) 0149 CRC (U32 / 1)
 * 0159 CreationDate (S32 / 1) 0345 SaleType (U8 / 1) 0395 BaseMask (U32 / 1)
 * 0506 Name (Variable / 1) 0562 InvType (S8 / 1) 0630 Type (S8 / 1) 0680
 * AssetID (LLUUID / 1) 0699 GroupID (LLUUID / 1) 0716 SalePrice (S32 / 1) 0719
 * OwnerID (LLUUID / 1) 0736 CreatorID (LLUUID / 1) 0968 ItemID (LLUUID / 1)
 * 1025 FolderID (LLUUID / 1) 1084 EveryoneMask (U32 / 1) 1101 Description
 * (Variable / 1) 1189 Flags (U32 / 1) 1348 NextOwnerMask (U32 / 1) 1452
 * GroupMask (U32 / 1) 1505 OwnerMask (U32 / 1)
 */
