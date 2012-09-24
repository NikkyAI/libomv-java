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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import libomv.FriendsManager.FriendInfo;
import libomv.FriendsManager.FriendNotificationCallbackArgs;
import libomv.FriendsManager.FriendRightsCallbackArgs;
import libomv.FriendsManager.FriendshipOfferedCallbackArgs;
import libomv.FriendsManager.FriendshipResponseCallbackArgs;
import libomv.FriendsManager.FriendshipTerminatedCallbackArgs;
import libomv.GridClient;
import libomv.Gui.Resources;
import libomv.inventory.InventoryException;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

// List to display the friends
public class FriendList extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private static final String cmdPayTo = "payTo";
	private static final String cmdStartIM = "startIM";
	private static final String cmdProfile = "profile";
	private static final String cmdTeleportTo = "teleportTo";
	private static final String cmdTeleportAsk = "teleportAsk";
	private static final String cmdAutopilotTo = "autopilotTo";
	private static final String cmdFriendRemove = "friendRemove";
	
	private static ImageIcon empty;
	private static ImageIcon offline;
	private static ImageIcon online;
	private static ImageIcon canSee;
	private static ImageIcon canMap;
	private static ImageIcon canEditMine;
	private static ImageIcon canEditTheirs;

	private GridClient _Client;
	private List<FriendInfo> indexList;

	private JScrollPane jScrollPane;
	private JTable jLFriendsList;
	private JPanel jButtonPanel;
	private JButton jBtnSendMessage;
	private JButton jBtnProfile;
	private JButton jBtnMoney;
	private JButton jBtnTpOffer;
	private JButton jBtnRemove;
	private JButton jBtnTeleportTo;
	private JButton jBtnAutopilotTo;

	/**
	 * Constructs a list to display
	 */
	public FriendList(GridClient client)
	{
		super();
		this._Client = client;

		// install friend change event handlers
		_Client.Friends.OnFriendRights.add(new FriendRightsChanged());
		_Client.Friends.OnFriendNotification.add(new FriendNotification());
		_Client.Friends.OnFriendshipResponse.add(new FriendshipResponse());
		_Client.Friends.OnFriendshipOffered.add(new FriendshipOffered());
		_Client.Friends.OnFriendshipTerminated.add(new FriendshipTerminated());

		empty = null;
		offline = Resources.loadIcon(Resources.ICON_OFFLINE);
		online = Resources.loadIcon(Resources.ICON_ONLINE);
		canSee = Resources.loadIcon(Resources.ICON_VISIBLE_ONLINE);
		canMap = Resources.loadIcon(Resources.ICON_VISIBLE_MAP);
		canEditMine = Resources.loadIcon(Resources.ICON_EDIT_MINE);
		canEditTheirs = Resources.loadIcon(Resources.ICON_EDIT_THEIRS);
		
		// Choose a sensible minimum size.
		setPreferredSize(new Dimension(290, 400));

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{200, 90, 0};
        gridBagLayout.rowHeights = new int[]{400, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};    
        setLayout(gridBagLayout);
		
		GridBagConstraints gbConstraint = new GridBagConstraints();
		gbConstraint.fill = GridBagConstraints.BOTH;
        gbConstraint.insets = new Insets(0, 0, 0, 5);
		gbConstraint.gridx = 0;
		gbConstraint.gridy = 0;
		add(getJScrollPane(), gbConstraint);
		
		gbConstraint = new GridBagConstraints();
		gbConstraint.fill = GridBagConstraints.VERTICAL;
		gbConstraint.gridx = 1;
		gbConstraint.gridy = 0;
		add(getButtonPanel(), gbConstraint);
	}
	
	private void installAction(AbstractButton element, String command)
	{
		element.setActionCommand(command);
		element.addActionListener(this);
	}
	
	private JPanel getButtonPanel()
	{
		if (jButtonPanel == null)
		{
			jButtonPanel = new JPanel();
			jButtonPanel.setBorder(new EmptyBorder(12, 12, 5, 12));
			jButtonPanel.setLayout(new GridLayout(12, 1, 0, 10));
		
			jBtnSendMessage = new JButton("Send message");
			installAction(jBtnSendMessage, cmdStartIM);
			jButtonPanel.add(jBtnSendMessage);
			
			jBtnProfile = new JButton("Profile ..");
			installAction(jBtnProfile, cmdProfile);
			jButtonPanel.add(jBtnProfile);

			JLabel lblSpacer1 = new JLabel("");
			jButtonPanel.add(lblSpacer1);

			jBtnMoney = new JButton("Pay ..");
			installAction(jBtnMoney, cmdPayTo);
			jButtonPanel.add(jBtnMoney);

			jBtnTpOffer = new JButton("Offer Teleport ..");
			installAction(jBtnTpOffer, cmdTeleportAsk);
			jButtonPanel.add(jBtnTpOffer);

			jBtnRemove = new JButton("Remove ..");
			installAction(jBtnRemove, cmdFriendRemove);
			jButtonPanel.add(jBtnRemove);		

			JLabel lblSpacer2 = new JLabel("");
			jButtonPanel.add(lblSpacer2);

			jBtnTeleportTo = new JButton("Teleport to ..");
			installAction(jBtnTeleportTo, cmdTeleportTo);
			jButtonPanel.add(jBtnTeleportTo);

			jBtnAutopilotTo = new JButton("Autopilot to ..");
			installAction(jBtnAutopilotTo, cmdAutopilotTo);
			jButtonPanel.add(jBtnAutopilotTo);
		}
		return jButtonPanel;
	}
	
	private JScrollPane getJScrollPane()
	{
		if (jScrollPane == null)
		{
			jScrollPane = new JScrollPane(getJFriendsList(),
					                      ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					                      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	        jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			getJFriendsList().setFillsViewportHeight(true);
		}
		return jScrollPane;
	}

    private class FriendsTableData extends AbstractTableModel
    {
		private static final long serialVersionUID = 1L;

		private String[] columnNames = {"Online",
                                        "Name",
                                        "Can see me",
                                        "Can map me",
                                        "Can edit mine",
                                        "Can see them",
                                        "Can map them",
                                        "Can edit theirs"};
		
        public int getColumnCount()
        {
            return 8;
        }
 
        public int getRowCount()
        {
            return _Client.Friends.getFriendList().size();
        }
 
        public String getColumnName(int col)
        {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col)
        {
            FriendInfo info = _Client.Friends.getFriend(row);
            if (info != null)
            {
            	switch (col)
            	{
            		case 0:
            			return info.getIsOnline() ? online : empty;
                	case 1:
                		return info.getName();
                	case 2:
                		return info.getCanSeeMeOnline();
                	case 3:
                		return info.getCanSeeMeOnMap();
                	case 4:
                		return info.getCanModifyMyObjects();
                	case 5:
                		return info.getCanSeeThemOnline();
                	case 6:
                		return info.getCanSeeThemOnMap();
                	case 7:
                		return info.getCanModifyTheirObjects();
            	}
            }
            return null;
        }
        
        /* JTable uses this method to determine the default renderer/editor for each cell. */
        public Class<?> getColumnClass(int c)
        {
            switch (c)
            {
            	case 0:
            		return ImageIcon.class;
            	case 1:
            		return String.class;
            	default:
            		return Boolean.class;
            }
        }
        
        /*
         * Don't need to implement this method unless your table's editable.
         */
        public boolean isCellEditable(int row, int col)
        {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return (col >= 2 && col <= 4);
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col)
        {
            FriendInfo info = _Client.Friends.getFriend(row);
            if (info != null)
            {
            	switch (col)
            	{
                	case 2:
                		info.setCanSeeMeOnline((Boolean)value);
                	case 3:
                		info.setCanSeeMeOnMap((Boolean)value);
                	case 4:
                		info.setCanModifyMyObjects((Boolean)value);
            	}
            }
            fireTableCellUpdated(row, col);
        }
    }

    // This customized renderer can render objects of the type Text and Icon
    private class HeaderCellRenderer extends DefaultTableCellRenderer
    {
    	private static final long serialVersionUID = 1L;
    
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column)
        {
            // Inherit the colors and font from the header component
            if (table != null)
            {
                JTableHeader header = table.getTableHeader();
                if (header != null)
                {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            if (value instanceof ImageIcon)
            {
                setIcon((ImageIcon)value);
                setText(Helpers.EmptyString);
            }
            else
            {
                setText((value == null) ? "" : value.toString());
                setIcon(null);
            }
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    };

    private final JTable getJFriendsList()
	{
		if (jLFriendsList == null)
		{
			jLFriendsList = new JTable(new FriendsTableData())
			{
				private static final long serialVersionUID = 1L;

				//Implement table header tool tips.
			    protected JTableHeader createDefaultTableHeader()
			    {
			        return new JTableHeader(columnModel)
			        {
						private static final long serialVersionUID = 1L;

					    protected TableCellRenderer createDefaultRenderer()
					    {
					    	return new HeaderCellRenderer();
					    }
					    
			            public String getToolTipText(MouseEvent e)
			            {
			                java.awt.Point p = e.getPoint();
			                int index = columnModel.getColumnIndexAtX(p.x);
			                int realIndex = columnModel.getColumn(index).getModelIndex();
			                return jLFriendsList.getColumnName(realIndex);
			            }
			        };
			    }			
			};
			
			jLFriendsList.setPreferredScrollableViewportSize(new Dimension(200, 400));
			jLFriendsList.setFillsViewportHeight(true);
			jLFriendsList.setShowVerticalLines(true);

			// only allow single selections.
			jLFriendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
	        int headerWidth = 0;
	        int cellWidth = 0;
	        Component comp = null;
	        TableColumn column = null;
	        TableModel model = jLFriendsList.getModel();
	        TableCellRenderer headerRenderer = jLFriendsList.getTableHeader().getDefaultRenderer();
	        for (int i = 0; i < 8; i++)
	        {
	            column = jLFriendsList.getColumnModel().getColumn(i);
	 
	            switch (i)
	            {
	            	case 0:
	            		column.setHeaderValue(offline);
	            		break;
	            	case 2:
	            		column.setHeaderValue(canSee);
	            		break;
	            	case 3:
	            		column.setHeaderValue(canMap);
	            		break;
	            	case 4:
	            		column.setHeaderValue(canEditMine);
	            		break;
	            	case 5:
	            		column.setHeaderValue(canSee);
	            		break;
	            	case 6:
	            		column.setHeaderValue(canMap);
	            		break;
	            	case 7:
	            		column.setHeaderValue(canEditTheirs);
	            		break;
	            }
	            if (i != 1)
	            {
	            	comp = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
	            	headerWidth = comp.getPreferredSize().width;
	 
	            	comp = jLFriendsList.getDefaultRenderer(model.getColumnClass(i)).
	            			             getTableCellRendererComponent(jLFriendsList, model.getValueAt(0, i), false, false, 0, i);
	            	cellWidth = comp.getPreferredSize().width;
	            	column.setMaxWidth(Math.max(headerWidth, cellWidth));
	            }
	            else
	            {
	            	comp = jLFriendsList.getDefaultRenderer(model.getColumnClass(i)).
	            			             getTableCellRendererComponent(jLFriendsList, "A very long name that should fill out", false, false, 0, i);
	            	cellWidth = comp.getPreferredSize().width;
	            	column.setPreferredWidth(Math.max(headerWidth, cellWidth));
	            }
	            	
	        }
			
			// install a mouse handler
			jLFriendsList.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					// If an index is selected...
					if (jLFriendsList.getSelectedRow() >= 0)
					{
						// If the left mouse button was pressed
						if (SwingUtilities.isLeftMouseButton(e))
						{
							// Look for a double click.
							if (e.getClickCount() >= 2)
							{
								// Get the associated agent.
								FriendInfo friend = getSelectedFriendRow();
								// Only allow creation of a chat window if the avatar name is resolved.
								if (friend.getName() != null && !friend.getName().isEmpty())
								{
									// TODO: Create a private chat
								}
							}
						}
						// If the right mouse button was pressed...
						else if (SwingUtilities.isRightMouseButton(e))
						{
							FriendPopupMenu fpm = new FriendPopupMenu(_Client, getSelectedFriendRow());
							fpm.show(jLFriendsList, e.getX(), e.getY());
						}
					}
				}
			});			
		}
		return jLFriendsList;
	}

	/**
	 * Triggered when the other avatar has changed our rights
	 */
	private class FriendRightsChanged implements Callback<FriendRightsCallbackArgs>
	{
		@Override
		public boolean callback(FriendRightsCallbackArgs e)
		{
			changeFriend(e.getFriendInfo().getID());
			return false;
		}
	}

	/**
	 * Triggered when online status changes are reported
	 */
	private class FriendNotification implements Callback<FriendNotificationCallbackArgs>
	{
		@Override
		public boolean callback(FriendNotificationCallbackArgs e)
		{
			for (UUID uuid : e.getAgentID())
			{
				changeFriend(uuid);
			}
			return false;
		}
	}

	/**
	 * Triggered when a user has accepted or declined our friendship offer
	 */
	private class FriendshipResponse implements Callback<FriendshipResponseCallbackArgs>
	{
		@Override
		public boolean callback(FriendshipResponseCallbackArgs e)
		{
			UUID uuid = e.getAgentID();
			String name = e.getName();
			
			/* Show dialog informing about the decision of the other */
			
			if (e.getAccepted())
			{
				invalidateData();
			}
			return false;
		}
	}

	/**
	 * Triggered when a user has sent us a friendship offer
	 */
	private class FriendshipOffered implements Callback<FriendshipOfferedCallbackArgs>
	{
		@Override
		public boolean callback(FriendshipOfferedCallbackArgs e)
		{
			UUID uuid = e.getFriendID();
			String name = e.getName();
			try
			{
				/* Prompt user for acceptance of friendship offer */
				boolean accepted = true;
		
				/* if accepted, send acceptance message */
				if (accepted)
				{
					_Client.Friends.AcceptFriendship(uuid, e.getSessionID());
					invalidateData();
				}
				else
				{
					_Client.Friends.DeclineFriendship(uuid, e.getSessionID());
				}
			}
			catch (InventoryException ex)
			{
				Logger.Log("Inventory Exception", LogLevel.Error, _Client, ex);
			}
			catch (Exception ex)
			{
				Logger.Log("Exception sending response", LogLevel.Error, _Client, ex);
			}
			return false;
		}
	}

	/**
	 * Triggered when a user has removed us from their friends list
	 */
	private class FriendshipTerminated implements Callback<FriendshipTerminatedCallbackArgs>
	{
		@Override
		public boolean callback(FriendshipTerminatedCallbackArgs e)
		{
			invalidateData();
			return false;
		}
	}
		
	private FriendInfo getSelectedFriendRow()
	{
		return _Client.Friends.getFriend(jLFriendsList.convertRowIndexToModel(jLFriendsList.getSelectedRow()));
	}

	
	/**
	 * Change friend info in the list
	 * 
	 * @param uuid The friend to change
	 */
	private void changeFriend(UUID uuid)
	{
		ListSelectionEvent event = new ListSelectionEvent(this, _Client.Friends.getFriendIndex(uuid), _Client.Friends.getFriendIndex(uuid), false);
		getJFriendsList().valueChanged(event);
	}

	private void invalidateData()
	{
		ListSelectionEvent event = new ListSelectionEvent(this, 0, _Client.Friends.getFriendList().size() - 1, false);
		getJFriendsList().valueChanged(event);
	}
	
	private class FriendPopupMenu extends JPopupMenu
	{
		private static final long serialVersionUID = 1L;
		// The friend associated with the menu
		private FriendInfo _Info;
		// The client to use to communicate with the grid
		private GridClient _Client;

		// The menu item used to send the agent a message
		private JMenuItem jmiSendMessage;
		// The menu item used to request the avatar's profile
		private JMenuItem jmiProfile;
		// The menu item used to send money to another avatar
		private JMenuItem jmiMoneyTransfer;
		// The menu item used to offer the agent teleportation
		private JMenuItem jmiOfferTeleport;
		// The menu item used to remove the agent as a friend
		private JMenuItem jmiRemoveAsFriend;
		// The menu item used to teleport to the agent
		private JMenuItem jmiTeleportTo;
		// The menu item used to autopilot to an agent
		private JMenuItem jmiAutopilotTo;

		/**
		 * Constructor
		 * 
		 * @param client
		 *            The client to use to communicate with the grid
		 * @param info
		 *            The friend to generate the menu for.
		 */
		public FriendPopupMenu(GridClient client, FriendInfo info)
		{
			super();
			this._Info = info;
			this._Client = client;

			// Send message
			add(getJmiSendMessage());
			// Add the profile menu item
			add(getJmiProfile());
			// Send money transfer
			add(getJmiMoneyTransfer());
			if (_Info.getIsOnline())
			{
				// Offer teleport.
				add(getJmiOfferTeleport());
				if (_Client.Network.getCurrentSim().getAvatarPositions().containsKey(_Info.getID()))
				{
					add(new JPopupMenu.Separator());
					// Allow teleporting to the agent
					add(getJmiTeleportTo());
					// Allow autopiloting to the agent
					add(getJmiAutopilotTo());
				}

			}
			add(new JPopupMenu.Separator());
			// Allow removing as a friend
			add(getJmiRemoveAsFriend());
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
				installAction(jmiSendMessage, cmdStartIM);
			}
			if (_Info.getName() == null || _Info.getName().isEmpty())
				jmiSendMessage.setEnabled(false);
			return jmiSendMessage;
		}

		/**
		 * Get the profile menu item
		 * 
		 * @return The "profile" menu item
		 */
		private JMenuItem getJmiProfile()
		{
			if (jmiProfile == null)
			{
				jmiProfile = new JMenuItem("Profile ..");
				// add an ActionListener
				installAction(jmiProfile, cmdProfile);
			}
			return jmiProfile;
		}

		/**
		 * Get the money transfer menu item
		 * 
		 * @return The "pay" menu item
		 */
		private JMenuItem getJmiMoneyTransfer()
		{
			if (jmiMoneyTransfer == null)
			{
				jmiMoneyTransfer = new JMenuItem("Pay ..");
				// add an ActionListener
				installAction(jmiMoneyTransfer, cmdPayTo);
			}
			return jmiMoneyTransfer;
		}

		/**
		 * Get the teleport offer menu item
		 * 
		 * @return The teleport offer menu item.
		 */
		private JMenuItem getJmiOfferTeleport()
		{
			if (jmiOfferTeleport == null)
			{
				jmiOfferTeleport = new JMenuItem("Offer Teleport ..");
				// Add an action listener.
				installAction(jmiOfferTeleport, cmdTeleportAsk);
			}
			return jmiOfferTeleport;
		}

		/**
		 * Get the remove as friend menu item
		 * 
		 * @return The remove as friend menu item
		 */
		private JMenuItem getJmiRemoveAsFriend()
		{
			if (jmiRemoveAsFriend == null)
			{
				jmiRemoveAsFriend = new JMenuItem("Remove ..");
				// Add an action listener
				installAction(jmiRemoveAsFriend, cmdFriendRemove);
			}
			return jmiRemoveAsFriend;
		}

		/**
		 * Get the teleport to menu item
		 * 
		 * @return The teleport to menu item
		 */
		private JMenuItem getJmiTeleportTo()
		{
			if (jmiTeleportTo == null)
			{
				jmiTeleportTo = new JMenuItem("Teleport to");
				// Add an ActionListener
				installAction(jmiTeleportTo, cmdTeleportTo);
			}
			return jmiTeleportTo;
		}

		/**
		 * Get the autopilot to menu item. If it has not been initialised, it is
		 * initialised upon first call.
		 * 
		 * @return The autopilot to menu item.
		 */
		private JMenuItem getJmiAutopilotTo()
		{
			if (jmiAutopilotTo == null)
			{
				jmiAutopilotTo = new JMenuItem("Autopilot to");
				// Add an ActionListener.
				installAction(jmiAutopilotTo, cmdAutopilotTo);
			}
			return jmiAutopilotTo;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		FriendInfo info = getSelectedFriendRow();
		if (e.getActionCommand().equals(cmdPayTo))
		{
			// TODO: open a money transfer dialog			
		}
		else if (e.getActionCommand().equals(cmdProfile))
		{
			// TODO: open avatar profile dialog			
		}
		else if (e.getActionCommand().equals(cmdStartIM))
		{
			// TODO: Open a private chat with the friend
		}
		else if (e.getActionCommand().equals(cmdFriendRemove))
		{
			try
			{
				_Client.Friends.TerminateFriendship(info.getID());
			}
			catch (Exception ex)
			{
				Logger.Log("TerminateFriendship failed", LogLevel.Error, _Client, ex);
			}
		}
		else if (e.getActionCommand().equals(cmdTeleportTo))
		{
			try
			{
				Vector3 pos = _Client.Network.getCurrentSim().getAvatarPositions().get(info.getID());
				_Client.Self.Teleport(_Client.Network.getCurrentSim().Name, pos);
			}
			catch (Exception ex)
			{
				Logger.Log("Teleporting to " + info.getName() + " failed", LogLevel.Error, _Client, ex);
			}
		}
		else if (e.getActionCommand().equals(cmdTeleportAsk))
		{
			try
			{
				_Client.Self.SendTeleportLure(info.getID());
			}
			catch (Exception ex)
			{
				Logger.Log("SendTeleportLure failed", LogLevel.Error, _Client, ex);
			}
		}
		else if (e.getActionCommand().equals(cmdAutopilotTo))
		{
			try
			{
				Vector3 pos = _Client.Network.getCurrentSim().getAvatarPositions().get(info.getID());
				_Client.Self.AutoPilotLocal((int) pos.X, (int) pos.Y, pos.Y);
			}
			catch (Exception ex)
			{
				Logger.Log("Autopiloting to " + info.getName() + " failed", LogLevel.Error, _Client, ex);
			}
		}
	}
}
