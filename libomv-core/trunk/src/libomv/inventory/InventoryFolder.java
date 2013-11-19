/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Copyright (c) 2009-2012, Frederick Martian
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.assets.AssetItem.AssetType;
import libomv.types.UUID;

/**
 * A folder contains {@link libomv.inventory.InventoryNode}s and has certain
 * attributes specific to itself
 */
public class InventoryFolder extends InventoryNode
{
	private static final long serialVersionUID = 1L;
	// The Preferred {@link T:libomv.assets.AssetItem.AssetType} for a folder.
	public AssetType preferredType;
	// The Version of this folder
	public int version;
	// The number of descendents in this folder. This value can be different to the actual
	// number of children, if the contents of the folder hasn't been completely fetched yet. 
	protected int descendentCount;
	// The list of children this folder contains
	protected ArrayList<InventoryNode> children;
	
	/**
	 * Constructor
	 * 
	 * @param itemID
	 *            UUID of the folder
	 */
	public InventoryFolder(UUID itemID)
	{
		super(itemID);
		preferredType = AssetType.Unknown;
		version = 1;
	}

	public InventoryFolder(UUID itemID, UUID ownerID)
	{
		this(itemID);
		this.ownerID = ownerID;
	}

	public InventoryFolder(UUID itemID, UUID parentID, UUID ownerID)
	{
		this(itemID);
		this.parentID = parentID;
		this.ownerID = ownerID;
	}

	public InventoryFolder(OSDMap map)
	{
		super();
		fromOSD(map);
	}

	@Override
	public InventoryType getType()
	{
		return InventoryType.Folder;
	}
	
	@Override
	public Date getModifyTime()
	{
		Date newest = new Date(); //.MinValue;
		for (InventoryNode node : children)
		{
			Date t = node.getModifyTime();
			if (t.after(newest))
				newest = t;
		}
		return newest;
	}

	/**
	 * Returns a copy of the arraylist of children. We return a copy so nobody can mess with
	 * our tree structure.
	 * 
	 * @return an arraylist containing the children nodes of this folder or null if there is
	 * no children list yet
	 */
	public ArrayList<InventoryNode> getContents()
	{
		if (children != null)
			return new ArrayList<InventoryNode>(children);
		return null;
	}

	@Override
	protected OSDMap toOSD()
	{
		return toOSD(false);
	}
	
	protected OSDMap toOSD(boolean descendentRoot)
	{
		OSDMap map = super.toOSD();
		map.put(descendentRoot ? "folder_id" : "category_id", OSD.FromUUID(itemID));
		map.put("type_default", OSD.FromInteger(preferredType.getValue()));
		map.put("version", OSD.FromInteger(version));
		if (descendentRoot)
		{
			map.put("descendents", OSD.FromInteger(descendentCount));
		}
		return map;
	}
	
	protected void fromOSD(OSDMap map)
	{
		super.fromOSD(map);
		if (ownerID == null)
			ownerID = map.get("owner_id").AsUUID();
		UUID folderID = map.get("category_id").AsUUID();
		if (folderID == null)
			folderID = map.get("folder_id").AsUUID();
		version =  map.get("version").AsInteger();
		preferredType = AssetType.setValue(map.get("type_default").AsInteger());
		if (map.containsKey("descendents"))
		{
			descendentCount =  map.get("descendents").AsInteger();		
		}
	}
	
	/**
	 * Initializes an InventoryFolder object from a serialization stream
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

		preferredType = AssetType.setValue(info.readByte());
		version = info.readInt();

		int num = info.readInt();
		if (num >= 0)
		{
			children = new ArrayList<InventoryNode>(num);
			for (int i = 0; i < num; i++)
				children.add((InventoryNode)info.readObject());
		}
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
		info.writeByte(preferredType.getValue());
		info.writeInt(version);
		if (children == null)
		{
			info.writeInt(-1);
		}
		else
		{
			info.writeInt(children.size());
			Iterator<InventoryNode> iter = children.iterator();
			while (iter.hasNext())
				info.writeObject(iter.next());
		}
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() ^ preferredType.hashCode() ^ version ^ children.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		InventoryFolder folder = (InventoryFolder) ((o instanceof InventoryFolder) ? o : null);
		return folder != null && equals(folder);
	}

	@Override
	public boolean equals(InventoryNode o)
	{
		InventoryFolder folder = (InventoryFolder) ((o instanceof InventoryFolder) ? o : null);
		return folder != null && equals(folder);
	}

	public final boolean equals(InventoryFolder o)
	{
		return super.equals(o) && o.preferredType.equals(preferredType) && o.version == version;
	}
}
