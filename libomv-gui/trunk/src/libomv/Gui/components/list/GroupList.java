/**
 * Copyright (c) 2009-2011, Frederick Martian
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import libomv.FriendsManager.FriendInfo;
import libomv.GroupManager.Group;
import libomv.GroupManager.GroupOperationCallbackArgs;
import libomv.Gui.components.list.SortedListModel.SortOrder;
import libomv.Gui.windows.MainControl;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class GroupList extends JScrollPane
{
	private static final long serialVersionUID = 1L;

	private MainControl _Main;
	
	private JList jLGroupsList;
	
	/**
	 * Constructs a list to display
	 */
	public GroupList(MainControl main)
	{
		super();
		_Main = main;

		_Main.getGridClient().Groups.OnGroupJoinedReply.add(new GroupJoined());
		_Main.getGridClient().Groups.OnGroupLeaveReply.add(new GroupLeave());

//		_Main.getGridClient().Groups.OnCurrentGroups.add(new GroupCurrentGroups());
//		_Main.getGridClient().Groups.OnGroupCreatedReply.add(new GroupCreated());
//      _Main.getGridClient().Self.OnMuteListUpdated.add(new MuteListUpdated());
		// CapsEventQueue must be working for this
//      _Main.getGridClient().Groups.RequestCurrentGroups();
		
		// Choose a sensible minimum size.
		setPreferredSize(new Dimension(200, 0));
		// Add the friends list to the viewport.
		setViewportView(getJGroupsList());
	}
	
	private final JList getJGroupsList()
	{
		if (jLGroupsList == null)
		{
			jLGroupsList = new JList(new SortedListModel(new DefaultListModel(), SortOrder.ASCENDING));
			// install a mouse handler
			jLGroupsList.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					// Select the item first.
					jLGroupsList.setSelectedIndex(jLGroupsList.locationToIndex(e.getPoint()));

					// If an index is selected...
					if (jLGroupsList.getSelectedIndex() >= 0)
					{
						// If the left mouse button was pressed
						if (SwingUtilities.isLeftMouseButton(e))
						{
							// Look for a double click.
							if (e.getClickCount() >= 2)
							{
								// Get the associated agent.
								Group group = (Group) jLGroupsList.getSelectedValue();
								// TODO: Create a group chat
								
							}
						}
						// If the right mouse button was pressed...
						else if (SwingUtilities.isRightMouseButton(e))
						{
							GroupPopupMenu gpm = new GroupPopupMenu((Group) (jLGroupsList.getSelectedValue()));
							gpm.show(jLGroupsList, e.getX(), e.getY());
						}
					}
				}
			});
			
			// Initialize the list with the values from the friends manager
			DefaultListModel model = (DefaultListModel) ((SortedListModel) jLGroupsList.getModel()).getUnsortedModel();
			model.copyInto(_Main.getGridClient().Groups.GroupList.values().toArray());
			// create Renderer and display
			jLGroupsList.setCellRenderer(new GroupListRow());
			// only allow single selections.
			jLGroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return jLGroupsList;
	}

	/**
	 * Find the entry based on the groups UUID
	 * 
	 * @param id The UUID of the group
	 * @return returns the group info if found, null otherwise
	 */
	public FriendInfo findGroup(UUID id)
	{
		DefaultListModel model = (DefaultListModel) ((SortedListModel) getJGroupsList().getModel()).getUnsortedModel();
		for (Enumeration<?> e = model.elements(); e.hasMoreElements();)
		{
			FriendInfo info = (FriendInfo) e.nextElement();
			if (info.getID().equals(id))
			{
				return info;
			}
		}
		return null;
	}
	
	/**
	 * Add a group to the list
	 * 
	 * @param info The group info to add to the list
	 * @return true if the group was added, false if it was replaced
	 */
	public boolean addGroup(Group info)
	{
		DefaultListModel model = (DefaultListModel) ((SortedListModel) getJGroupsList().getModel()).getUnsortedModel();
		int idx = model.indexOf(info);
		if (idx < 0)
			model.add(model.size(), info);
		else
			model.set(idx, info);
		return idx < 0;
	}

	/**
	 * Remove a group from the list
	 * 
	 * @param info The group info to remove from the list
	 * @return true if the group info was successfully removed, false if the group could not be found, 
	 */
	public boolean removeGroup(Group info)
	{
		DefaultListModel model = (DefaultListModel) ((SortedListModel) getJGroupsList().getModel()).getUnsortedModel();
		int idx = model.indexOf(info);
		if (idx < 0)
			return false;
		model.remove(idx);
		return true;
	}
	
	/**
	 * Change group info in the list
	 * 
	 * @param info The group info to change
	 * @return true if the group info was successfully changed, false otherwise
	 */
	public boolean changeGroup(Group info)
	{
		DefaultListModel model = (DefaultListModel) ((SortedListModel) getJGroupsList().getModel()).getUnsortedModel();
		int idx = model.indexOf(info);
		if (idx >= 0)
			model.set(idx, info);
		return idx >= 0;
	}

	private class GroupListRow extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			// The toString() function from Group is used to set the label text
			JLabel jlblName = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			Color background, foreground;
			if (isSelected)
			{
				background = list.getSelectionBackground();
				foreground = list.getSelectionForeground();
			}
			else
			{
				background = list.getBackground();
				foreground = list.getForeground();
			}
			jlblName.setBackground(background);
			jlblName.setForeground(foreground);
			
			Font font = jlblName.getFont();
			int style = font.getStyle();
			Group group = (Group) value;

			if (_Main.getGridClient().Self.getActiveGroup().equals(group.getID()))
			{
				style |= Font.BOLD;
			}
			else
			{
				style &= ~Font.BOLD;
			}
			jlblName.setFont(new Font(font.getFamily(), style, font.getSize()));
			return jlblName;
		}
	}

	private class GroupPopupMenu extends JPopupMenu
	{
		private static final long serialVersionUID = 1L;
		// The friend associated with the menu
		private Group _Info;

		// The menu item used to send the group a message
		private JMenuItem jmiSendMessage;
		// The menu item used to activate the group
		private JMenuItem jmiActivate;
		// The menu item to view the group titles
		private JMenuItem jmiInvite;
		// The menu item to view the group info
		private JMenuItem jmiGroupInfo;
		// The menu item to create a new group
		private JMenuItem jmiCreateGroup;
		// The menu item to search for groups
		private JMenuItem jmiSearchGroup;
		// The menu item used to remove the group from the list
		private JMenuItem jmiLeaveGroup;

		/**
		 * Constructor
		 * 
		 * @param client
		 *            The client to use to communicate with the grid
		 * @param info
		 *            The friend to generate the menu for.
		 */
		public GroupPopupMenu(Group info)
		{
			super();
			this._Info = info;

			// Send message
			add(getJmiSendMessage());
			// Activate this group
			add(getJmiActivate(_Main.getGridClient().Self.getActiveGroup().equals(_Info.getID())));
			// Add the group invitation menu
			add(getJmiInvite());
			// Add the group info menu item
			add(getJmiGroupInfo());
			// Add the group creation menu item
			add(getJmiCreateGroup());
			// Add the search group menu item
			add(getJmiSearchGroup());
			add(new JPopupMenu.Separator());
			// Allow removing as a friend.
			add(getJmiLeaveGroup());
		}

		/**
		 * Get the send message menu item
		 * 
		 * @return The "send message" menu item
		 */
		private JMenuItem getJmiSendMessage()
		{
			if (jmiSendMessage == null)
			{
				jmiSendMessage = new JMenuItem("Send message");
				// Add an ActionListener
				jmiSendMessage.addActionListener(new ActionListener()
				{
					/**
					 * Called when an action is performed
					 * 
					 * @param e
					 *            The ActionEvent
					 */
					@Override
					public void actionPerformed(ActionEvent e)
					{
						// TODO: Open a group chat
					}
				});
			}
			return jmiSendMessage;
		}
	
		/**
		 * Get the activate group menu item
		 * 
		 * @return The "activate group" menu item
		 */
		private JMenuItem getJmiActivate(boolean disable)
		{
			if (jmiActivate == null)
			{
				jmiActivate = new JMenuItem("Activate");
				// add an ActionListener
				jmiActivate.addActionListener(new ActionListener()
				{
					/**
					 * Called when an action is performed
					 * 
					 * @param e
					 *            The ActionEvent
					 */
					@Override
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							_Main.getGridClient().Groups.ActivateGroup(_Info.getID());
						}
						catch (Exception ex)
						{
							Logger.Log("ActivateGroup failed", LogLevel.Error, _Main.getGridClient(), ex);
						}
					}
				});
			}
			jmiActivate.setEnabled(!disable);
			return jmiActivate;
		}
		
		/**
		 * Get the group invite menu item
		 * 
		 * @return The "group invite" menu item
		 */
		private JMenuItem getJmiInvite()
		{
			if (jmiInvite == null)
			{
				jmiInvite = new JMenuItem("Invite ..");
				jmiInvite.addActionListener(new ActionListener()
				{
					/**
					 * Called when an action is performed
					 * 
					 * @param e
					 *            The ActionEvent
					 */
					@Override
					public void actionPerformed(ActionEvent e)
					{
						// TODO: Open Group Invitation window
					}
				});
			}
			return jmiInvite;
		}
		
		/**
		 * Get the group info menu item
		 * 
		 * @return The "group info" menu item
		 */
		private JMenuItem getJmiGroupInfo()
		{
			if (jmiGroupInfo == null)
			{
				jmiGroupInfo = new JMenuItem("Info ..");
				// add an ActionListener
				jmiGroupInfo.addActionListener(new ActionListener()
				{
					/**
					 * Called when an action is performed
					 * 
					 * @param e
					 *            The ActionEvent
					 */
					@Override
					public void actionPerformed(ActionEvent e)
					{
						// TODO: open group info dialog
					}
				});
			}
			return jmiGroupInfo;
		}

		/**
		 * Get the create group menu item
		 * 
		 * @return The "create group" menu item
		 */
		private JMenuItem getJmiCreateGroup()
		{
			if (jmiCreateGroup == null)
			{
				jmiCreateGroup = new JMenuItem("Create ..");
				// add an ActionListener
				jmiCreateGroup.addActionListener(new ActionListener()
				{
					/**
					 * Called when an action is performed
					 * 
					 * @param e
					 *            The ActionEvent
					 */
					@Override
					public void actionPerformed(ActionEvent e)
					{
						// TODO: open create group dialog
					}
				});
			}
			return jmiCreateGroup;
		}

		/**
		 * Get the search group menu item
		 * 
		 * @return The "search group" menu item
		 */
		private JMenuItem getJmiSearchGroup()
		{
			if (jmiSearchGroup == null)
			{
				jmiSearchGroup = new JMenuItem("Search ..");
				// add an ActionListener
				jmiSearchGroup.addActionListener(new ActionListener()
				{
					/**
					 * Called when an action is performed
					 * 
					 * @param e
					 *            The ActionEvent
					 */
					@Override
					public void actionPerformed(ActionEvent e)
					{
						// TODO: open search group dialog
					}
				});
			}
			return jmiSearchGroup;
		}

		/**
		 * Get the remove as friend menu item
		 * 
		 * @return The remove as friend menu item
		 */
		private JMenuItem getJmiLeaveGroup()
		{
			if (jmiLeaveGroup == null)
			{
				jmiLeaveGroup = new JMenuItem("Leave ..");
				// Add an action listener
				jmiLeaveGroup.addActionListener(new ActionListener()
				{
					/**
					 * Called when an action is performed
					 * 
					 * @param e
					 *            The ActionEvent
					 */
					@Override
					public void actionPerformed(ActionEvent e)
					{
						// Terminate the membership
						try
						{
							_Main.getGridClient().Groups.LeaveGroup(_Info.getID());
						}
						catch (Exception ex)
						{
							Logger.Log("LeaveGroup failed", LogLevel.Error, _Main.getGridClient(), ex);
						}
					}
				});
			}
			return jmiLeaveGroup;
		}
	}
	
	private class GroupJoined implements Callback<GroupOperationCallbackArgs>
	{
		@Override
		public boolean callback(GroupOperationCallbackArgs args)
		{
			return false;
		}
	}
	
	private class GroupLeave implements Callback<GroupOperationCallbackArgs>
	{
		@Override
		public boolean callback(GroupOperationCallbackArgs args)
		{
			return false;
		}
	}
}
