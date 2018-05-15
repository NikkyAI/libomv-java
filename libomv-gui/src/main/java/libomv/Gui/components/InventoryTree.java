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
package libomv.Gui.components;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;

import libomv.Gui.components.list.InventoryTreeModel;
import libomv.inventory.InventoryNode;
import libomv.io.GridClient;

public class InventoryTree extends JScrollPane implements TreeWillExpandListener
{
	private static final long serialVersionUID = 1L;

	private GridClient _Client;

	private JTree jTrInventory;

	public InventoryTree(GridClient client)
	{
		super();
		this._Client = client;

		setViewportView(getJTInventory());
	}

	private JTree getJTInventory()
	{
		if (jTrInventory == null)
		{
            jTrInventory = new JTree(new InventoryTreeModel(_Client.Inventory));
            jTrInventory.setRootVisible(false);
            jTrInventory.setCellRenderer(new CellRenderer(jTrInventory.getCellRenderer()));
		}
		return jTrInventory;
	}

	/**
	 * A TreeCellRenderer displays each node of a tree. The default renderer
	 * displays arbitrary Object nodes by calling their toString() method. The
	 * Component.toString() method returns long strings with extraneous
	 * information. Therefore, we use this "wrapper" implementation of
	 * TreeCellRenderer to convert nodes from Component objects to useful String
	 * values before passing those String values on to the default renderer.
	 */
	static class CellRenderer implements TreeCellRenderer
	{
		TreeCellRenderer renderer; // The renderer we are a wrapper for

		// Constructor: just remember the renderer
		public CellRenderer(TreeCellRenderer renderer)
		{
			this.renderer = renderer;
		}

		// This is the only TreeCellRenderer method.
		// Compute the string to display, and pass it to the wrapped renderer
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
				                                      boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			String name = ((InventoryNode)value).name; // Component name
			// Use the wrapped renderer object to do the real work
			return renderer.getTreeCellRendererComponent(tree, name, selected, expanded, leaf, row, hasFocus);
		}
	}

	@Override
	public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException
	{
		// TODO Auto-generated method stub

	}
}
