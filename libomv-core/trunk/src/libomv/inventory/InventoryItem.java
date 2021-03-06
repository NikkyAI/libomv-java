/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
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
import libomv.inventory.InventoryNode.InventoryType;
import libomv.types.Permissions;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

/**
 * A leaf node in our Inventory
 */
public class InventoryItem extends InventoryNode
{
	private static final long serialVersionUID = 1L;

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
	public UUID assetID;
	/* The combined {@link OpenMetaverse.Permissions} of this item */
	public Permissions Permissions;
	/* The type of item from {@link libomv.assets.AssetItem.AssetType} */
	public AssetType assetType;
	/* The {@link OpenMetaverse.UUID} of the creator of this item */
//	public UUID CreatorID;
	/* A Description of this item */
	public String Description;
	/*
	 * The {@link OpenMetaverse.Group} s {@link OpenMetaverse.UUID} this item is
	 * set to or owned by
	 */
//	public UUID GroupID;
	/*
	 * If true, item is owned by a group
	 */
//	public boolean GroupOwned;
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
	public UUID TransactionID;
	/* The {@link OpenMetaverse.UUID} of the previous owner of the item */
//	public UUID LastOwnerID;

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
		InventoryItem item = null;
		switch (type)
		{
			case Texture:
				item = new InventoryTexture(id);
				break;
			case Sound:
				item = new InventorySound(id);
				break;
			case CallingCard:
				item = new InventoryCallingCard(id);
				break;
			case Landmark:
				item = new InventoryLandmark(id);
				break;
			case Object:
				item = new InventoryObject(id);
				break;
			case Notecard:
				item = new InventoryNotecard(id);
				break;
//			case Category:                                really a folder
//				item = new InventoryCategory(id);
//				break;
			case LSL:
				item = new InventoryLSL(id);
				break;
			case Snapshot:
				item = new InventorySnapshot(id);
				break;
			case Attachment:
				item = new InventoryAttachment(id);
				break;
			case Wearable:
				item = new InventoryWearable(id);
				break;
			case Animation:
				item = new InventoryAnimation(id);
				break;
			case Gesture:
				item = new InventoryGesture(id);
				break;
			default:
				try
				{
					item = (InventoryItem)Class.forName("Inventory " + type).getConstructor(id.getClass()).newInstance(id);
				}
				catch (Exception ex)
				{
					Logger.Log("Error instantiating an InventoryItem through class name", LogLevel.Error, ex);
				}
		}
		return item;
	}

	public static InventoryItem create(InventoryType type, UUID id, UUID parentID, UUID ownerID)
	{
		InventoryItem item = create(type, id);
		if (item != null)
		{
			item.parentID = parentID;
			item.ownerID = ownerID;
		}
		return item;
	}
	
	public InventoryItem()
	{
		super();
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

	public InventoryItem(OSDMap map)
	{
		super();
		fromOSD(map);
	}

	@Override
	public InventoryType getType()
	{
		return InventoryType.Unknown;
	}
	
	@Override
	public Date getModifyTime()
	{
		return CreationDate;
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

	@Override
	protected OSDMap toOSD()
	{
		OSDMap map = super.toOSD();
		map.put("item_id", OSD.FromUUID(itemID));
		map.put("asset_id", OSD.FromUUID(assetID));
		map.put("type", OSD.FromInteger(assetType.getValue()));
		map.put("inv_type", OSD.FromInteger(getType().getValue()));

		map.put("flags", OSD.FromUInteger(ItemFlags));

		map.put("permissions", Permissions.serialize());
	
		map.put("created_at", OSD.FromDate(CreationDate));
		return map;
	}

	protected void fromOSD(OSDMap map)
	{
		InventoryType type = InventoryType.setValue(map.get("inv_type").AsInteger());
		if (type == InventoryType.Texture && AssetType.setValue(map.get("type").AsInteger()) == AssetType.Object)
		{
			type = InventoryType.Attachment;
		}

		
		UUID itemID = map.get("asset_id").AsUUID();
		if (itemID == null)
			itemID = map.get("item_id").AsUUID();
		InventoryItem item = create(type, itemID);

		item.ownerID = map.get("agent_id").AsUUID();
		item.parentID = map.get("parent_id").AsUUID();
		
		item.name = map.get("name").AsString();
		item.Description = map.get("desc").AsString();
		item.assetID = map.get("asset_id").AsUUID();
		item.assetType = AssetType.setValue(map.get("type").AsInteger());
		item.CreationDate =  Helpers.UnixTimeToDateTime(map.get("created_at").AsReal());
		item.ItemFlags = map.get("flags").AsUInteger();
		item.Permissions = libomv.types.Permissions.fromOSD(map.get("permissions"));

		OSDMap sale = (OSDMap)map.get("sale_info");
		item.SalePrice = sale.get("sale_price").AsInteger();
		item.saleType = SaleType.setValue(sale.get("sale_type").AsInteger());
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
		assetID = (UUID) info.readObject();
		Permissions = (Permissions) info.readObject();
		assetType = AssetType.setValue(info.readByte());
		Description = info.readUTF();
		SalePrice = info.readInt();
		saleType = SaleType.setValue(info.readByte());
		ItemFlags = info.readInt();
		CreationDate = (Date) info.readObject();
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
		info.writeObject(assetID);
		info.writeObject(Permissions);
		info.writeByte(assetType.getValue());
		info.writeUTF(Description);
		info.writeInt(SalePrice);
		info.writeByte(saleType.getValue());
		info.writeInt(ItemFlags);
		info.writeObject(CreationDate);
	}

	@Override
	public String toString()
	{
		return assetType + " " + assetID + " (" + assetType + " " + itemID + ") '" + name + "'/'" +
                    Description + "' " + Permissions;
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
		return assetID.hashCode() ^ Permissions.hashCode() ^ assetType.hashCode() ^ getType().hashCode()
				^ Description.hashCode() ^ SalePrice ^ saleType.hashCode() ^ ItemFlags ^ CreationDate.hashCode();
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
	 * Determine whether the specified {@link OpenMetaverse.InventoryNode}
	 * object is equal to the current object
	 * 
	 * @param o
	 *            The {@link OpenMetaverse.InventoryNode} object to compare
	 *            against
	 * @return true if objects are the same
	 */
	@Override
	public boolean equals(InventoryNode o)
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
		return o != null && super.equals(o) && o.assetType.equals(assetType) && o.assetID.equals(assetID) && o.CreationDate.equals(CreationDate)
				&& o.Description.equals(Description) && o.ItemFlags == ItemFlags && o.getType().equals(getType())
				&& o.Permissions.equals(Permissions) && o.SalePrice == SalePrice && o.saleType.equals(saleType);
	}
}
