package libomv.Gui.components.list;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryManager;
import libomv.inventory.InventoryNode;
import libomv.inventory.InventoryNode.InventoryType;

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
