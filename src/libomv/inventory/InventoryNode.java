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
//

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import libomv.types.UUID;

public class InventoryNode implements Serializable
{
	private static final long serialVersionUID = 1L;

	private InventoryBase data;
    private InventoryNode parent;
    private HashMap<UUID, InventoryNode> nodes;
    private boolean needsUpdate = false;

    public final InventoryBase getData()
    {
        return data;
    }
    public final void setData(InventoryBase value)
    {
        data = value;
    }

    public final InventoryNode getParent()
    {
        return parent;
    }
    public final void setParent(InventoryNode value)
    {
        parent = value;
    }

    public final UUID getParentID_()
    {
        return parent.getData().UUID;
    }

    public final HashMap<UUID, InventoryNode> getNodes()
    {
        if (nodes == null)
        {
            nodes = new HashMap<UUID, InventoryNode>();
        }
        return nodes;
    }
    public final void setNodes(HashMap<UUID, InventoryNode> value)
    {
        nodes = value;
    }

    /* For inventory folder nodes specifies weather the folder needs to be
       refreshed from the server */
    public final boolean getNeedsUpdate()
    {
        return needsUpdate;
    }
    public final void setNeedsUpdate(boolean value)
    {
        needsUpdate = value;
    }

    public InventoryNode()
    {
    }

    public InventoryNode(InventoryBase data)
    {
        this.data = data;
    }

    /* De-serialization constructor for the InventoryNode Class */
    public InventoryNode(InventoryBase data, InventoryNode parent)
    {
        this.data = data;
        this.parent = parent;

        if (parent != null)
        {
            // Add this node to the collection of parent nodes
            parent.nodes.put(data.UUID, this);
        }
    }

    /**
     * Initializes an InventoryItem object from a serialization stream
     *
     * @param info serialization stream
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream info) throws IOException, ClassNotFoundException
    {
        if (serialVersionUID != info.readLong())
        	throw new InvalidObjectException("InventoryItem serial version mismatch");
        data = (InventoryBase)info.readObject();
        parent = (InventoryNode)info.readObject();
        needsUpdate = info.readBoolean();
        Object obj = info.readObject();
        if (obj instanceof HashMap)
            nodes = (HashMap<UUID, InventoryNode>)obj;
        else
        	throw new InvalidObjectException("");
    }

    /**
     * Write Serilization data for this InventoryFolder object to the stream
     *
     * @param info serialization stream
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream info) throws IOException
    {
		info.writeLong(serialVersionUID);
		info.writeObject(data);
		info.writeObject(parent);
		info.writeBoolean(needsUpdate);
		info.writeObject(nodes);
    }

    @Override
    public String toString()
    {
        if (data == null)
        {
            return "[Empty Node]";
        }
        return data.toString();
    }
}
