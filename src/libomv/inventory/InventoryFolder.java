/**
 * Copyright (c) 2007-2008, openmetaverse.org
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

import libomv.assets.AssetItem.AssetType;
import libomv.types.UUID;

/** A folder contains {@link T:OpenMetaverse.InventoryItem}s and has certain attributes specific to itself */
public class InventoryFolder extends InventoryBase
{
	private static final long serialVersionUID = 1L;
	// The Preferred {@link T:OpenMetaverse.AssetType} for a folder.
    public AssetType preferredType;
    // The Version of this folder
    public int version;
    // Number of child items this folder contains.
    public int descendentCount;

    /** 
     * Constructor
     *
     * @param itemID UUID of the folder
     */
    public InventoryFolder(UUID itemID)
    {
        super(itemID);
        preferredType = AssetType.Unknown;
        version = 1;
        // set to -1 to indicate that we don't know the descendent count yet
        descendentCount = -1;
    }

    /**
     * Initializes an InventoryFolder object from a serialization stream
     * 
     * @param info serialization stream
     * @throws ClassNotFoundException 
     * @throws IOException 
     */
    protected void readObject(ObjectInputStream info) throws IOException, ClassNotFoundException
    {
        super.readObject(info);
        if (serialVersionUID != info.readLong())
        	throw new InvalidObjectException("InventoryItem serial version mismatch");
        preferredType = AssetType.setValue(info.readByte());
        version = info.readInt();
        descendentCount = info.readInt();
    }

    /**
     * Write Serilization data for this InventoryFolder object to the stream
     * 
     * @param info serialization stream
     * @throws IOException 
     */
    @Override
    protected void writeObject(ObjectOutputStream info) throws IOException
    {
        super.writeObject(info);
        info.writeLong(serialVersionUID);
        info.writeByte(preferredType.getValue());
        info.writeInt(version);
        info.writeInt(descendentCount);
    }

    @Override
    public String toString()
    {
        return Name;
    }

    @Override
    public int hashCode()
    {
        return preferredType.hashCode() ^ version ^ descendentCount;
    }

    @Override
    public boolean equals(Object o)
    {
        InventoryFolder folder = (InventoryFolder)((o instanceof InventoryFolder) ? o : null);
        return folder != null && equals(folder);
    }

    @Override
    public boolean equals(InventoryBase o)
    {
        InventoryFolder folder = (InventoryFolder)((o instanceof InventoryFolder) ? o : null);
        return folder != null && equals(folder);
    }

    public final boolean equals(InventoryFolder o)
    {
        return super.equals((InventoryBase)((o instanceof InventoryBase) ? o : null)) && o.descendentCount == descendentCount && o.preferredType == preferredType && o.version == version;
    }
}
