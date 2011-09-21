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
import java.io.Serializable;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.types.UUID;

/* Base class for Inventory Items */
public abstract class InventoryBase implements Serializable
{
    //
	private static final long serialVersionUID = 1L;

	// {@link OpenMetaverse.UUID} of item/folder
    public UUID UUID;
    // {@link OpenMetaverse.UUID} of parent folder
    public UUID ParentUUID;
    // Name of item/folder */
    public String Name;
    // Item/Folder Owners {@link OpenMetaverse.UUID}
    public UUID OwnerID;

    /**
     * Constructor
     */
    public InventoryBase()
    {
    }

    /**
     * Constructor, takes an itemID as a parameter
     *
     * @param itemID The {@link OpenMetaverse.UUID} of the item
     */
    public InventoryBase(UUID itemID)
    {
        UUID = itemID;
    }

    public InventoryBase(OSD osd)
    {
    	fromOSD(osd);
    }

    public OSD toOSD()
    {
        OSDMap map = new OSDMap();
        map.put("UUID", OSD.FromUUID(UUID));
        map.put("ParentUUID", OSD.FromUUID(ParentUUID));
        map.put("Name", OSD.FromString(Name));
        map.put("OwnerID", OSD.FromUUID(OwnerID));
        return map;
    }

    public void fromOSD(OSD osd)
    {
    	if (osd instanceof OSDMap)
    	{
    	   OSDMap map = (OSDMap)osd;
           UUID = map.get("UUID").AsUUID();
           ParentUUID =  map.get("ParentUUID").AsUUID();
           Name =  map.get("Name").AsString();
           OwnerID =  map.get("OwnerID").AsUUID();
    	}
    }

    protected void readObject(ObjectInputStream info) throws IOException, ClassNotFoundException
    {
        if (serialVersionUID != info.readLong())
        	throw new InvalidObjectException("InventoryItem serial version mismatch");
        UUID = (UUID)info.readObject();
        ParentUUID = (UUID)info.readObject();
        Name = info.readUTF();
        OwnerID = (UUID)info.readObject();
    }

	protected void writeObject(ObjectOutputStream info) throws IOException
    {
		info.writeLong(serialVersionUID);
    	info.writeObject(UUID);
    	info.writeObject(ParentUUID);
    	info.writeUTF(Name);
    	info.writeObject(OwnerID);
    }

    /** Generates a number corresponding to the value of the object to support the use of a hash table,
     *  suitable for use in hashing algorithms and data structures such as a hash table
     *
     *  @return A Hashcode of all the combined InventoryBase fields
     */
    @Override
	public int hashCode()
    {
        return UUID.hashCode() ^ ParentUUID.hashCode() ^ Name.hashCode() ^ OwnerID.hashCode();
    }

    /** Determine whether the specified {@link OpenMetaverse.InventoryBase} object is equal to the current object
     *
     *  @param o InventoryBase object to compare against
     *  @return true if objects are the same
     */
    @Override
	public boolean equals(Object o)
    {
        InventoryBase inv = (InventoryBase)((o instanceof InventoryBase) ? o : null);
        return inv != null && equals(inv);
    }

    /** Determine whether the specified {@link OpenMetaverse.InventoryBase} object is equal to the current object
     *
     *  @param o InventoryBase object to compare against
     *  @return true if objects are the same
     */
    public boolean equals(InventoryBase o)
    {
        return o != null && o.UUID.equals(UUID) && o.ParentUUID.equals(ParentUUID) && o.Name.equals(Name) && o.OwnerID.equals(OwnerID);
    }
}
