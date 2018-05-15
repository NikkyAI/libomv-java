/**
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
 * - Neither the name of the libomv-java project nor the names of its
 *   contributors may be used to endorse or promote products derived from
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
package libomv.Gui.components.list;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryNode;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.io.inventory.InventoryManager;

public class InventoryTreeModel implements TreeModel
{
	private InventoryManager _Root;
	public InventoryTreeModel(InventoryManager root)
	{
		_Root = root;
	}

	/**
	 * Tree Model implementation for use in JTree objects
	 */
    protected EventListenerList listenerList = new EventListenerList();
	
	@Override
	public void addTreeModelListener(TreeModelListener listener)
	{
		listenerList.add(TreeModelListener.class, listener);
	}

	@Override
	public InventoryNode getChild(Object parent, int idx)
	{
		if (parent != null && ((InventoryNode)parent).getType() == InventoryType.Folder)
		{
			return _Root.getChildren((InventoryFolder)parent).get(idx);
		}
		return null;
	}

	@Override
	public int getChildCount(Object parent)
	{
		if (parent != null && ((InventoryNode)parent).getType() == InventoryType.Folder)
		{
			return _Root.getChildren((InventoryFolder)parent).size();
		}
		return 0;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		if (parent != null && ((InventoryNode)parent).getType() == InventoryType.Folder)
		{
			return _Root.getChildren((InventoryFolder)parent).indexOf(child);
		}
		return -1;
	}

	@Override
	public InventoryFolder getRoot()
	{
		return _Root.getRoot();
	}

	@Override
	public boolean isLeaf(Object node)
	{
		return ((InventoryNode)node).getType() != InventoryType.Folder;
	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener)
	{
		listenerList.remove(TreeModelListener.class, listener);
	}

	@Override
	public void valueForPathChanged(TreePath path, Object value)
	{
		if (value instanceof InventoryNode)
		{
			InventoryFolder parent = (InventoryFolder)path.getPathComponent(path.getPathCount() - 2);
			_Root.updateChild(parent, path.getLastPathComponent(), value);
		}
		else if (value instanceof String)
		{
			((InventoryNode)path.getLastPathComponent()).name = (String)value;
		}
	}
	
    /*
     * Notify all listeners that have registered interest for notification on this event type.
     * The event instance is lazily created using the parameters passed into the fire method.
     * @see EventListenerList
     */
	protected void fireNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children)
	{
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i]==TreeModelListener.class)
            {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)listeners[i + 1]).treeNodesChanged(e);
            }          
        }
	}

    /*
     * Notify all listeners that have registered interest for notification on this event type. 
     * The event instance is lazily created using the parameters passed into the fire method.
     * @see EventListenerList
     */
    protected void fireTreeNodesInserted(Object source, Object[] path, 
                                        int[] childIndices, Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i]==TreeModelListener.class)
            {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)listeners[i + 1]).treeNodesInserted(e);
            }          
        }
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     * @see EventListenerList
     */
    protected void fireTreeNodesRemoved(Object source, Object[] path, 
                                        int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)listeners[i + 1]).treeNodesRemoved(e);
            }          
        }
    }

    /*
     * Notify all listeners that have registered interest for notification on this event type.
     * The event instance is lazily created using the parameters passed into the fire method.
     * @see EventListenerList
     */
    protected void fireTreeStructureChanged(Object source, Object[] path, 
                                        int[] childIndices, Object[] children)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i]==TreeModelListener.class)
            {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)listeners[i + 1]).treeStructureChanged(e);
            }          
        }
    }
}
