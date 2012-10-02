/**
 * Copyright (c) 2010-2011, Frederick Martian
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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import libomv.AgentManager.AlertMessageCallbackArgs;
import libomv.AgentManager.BalanceCallbackArgs;
import libomv.AgentManager.ChatCallbackArgs;
import libomv.AgentManager.ChatSourceType;
import libomv.AgentManager.InstantMessageCallbackArgs;
import libomv.AgentManager.MuteEntry;
import libomv.AgentManager.MuteType;
import libomv.AgentManager.TeleportLureCallbackArgs;
import libomv.FriendsManager.FriendNotificationCallbackArgs;
import libomv.FriendsManager.FriendshipOfferedCallbackArgs;
import libomv.FriendsManager.FriendshipResponseCallbackArgs;
import libomv.FriendsManager.FriendshipTerminatedCallbackArgs;
import libomv.GroupManager.GroupInvitationCallbackArgs;
import libomv.Gui.components.list.FriendList;
import libomv.Gui.components.list.GroupList;
import libomv.Gui.windows.CommWindow;
import libomv.Gui.windows.MainControl;
import libomv.inventory.InventoryException;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class OnlinePanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	public static final String cmdFriends = "friends";
	public static final String cmdGroups = "groups";
	public static final String cmdInventory = "inventory";
	public static final String cmdSearch = "search";
	public static final String cmdMaps = "maps";
	public static final String cmdObjects = "objects";
	public static final String cmdMedia = "media";
	public static final String cmdVoice = "voice";
	
	private MainControl _Main;
	private CommWindow _Comm;

	private FriendList _FriendList;
	private GroupList _GroupList;
	
	private JMenuBar jMbMain;
	private JLabel jMiAmount;
	private JPanel jSceneViewer;

	private Callback<BalanceCallbackArgs> balanceUpdateCallback = new BalanceUpdate();
	private Callback<TeleportLureCallbackArgs> teleportLureCallback = new TeleportLure();
	private Callback<ChatCallbackArgs> chatCallback = new ChatMessage();
	private Callback<AlertMessageCallbackArgs> alertCallback = new AlertMessage();
	private Callback<InstantMessageCallbackArgs> instantCallback = new InstantMessage();
	private Callback<FriendNotificationCallbackArgs> friendNotificationCallback = new FriendNotification();
	private Callback<FriendshipResponseCallbackArgs> friendshipResponseCallback = new FriendshipResponse();
	private Callback<FriendshipOfferedCallbackArgs> friendshipOfferedCallback = new FriendshipOffered();
	private Callback<FriendshipTerminatedCallbackArgs> friendshipTerminatedCallback = new FriendshipTerminated();
	private Callback<GroupInvitationCallbackArgs> groupInvitationCallback = new GroupInvitation();

	public OnlinePanel(MainControl main)
	{
		_Main = main;
		
		_Main.getGridClient().Self.OnBalanceUpdated.add(balanceUpdateCallback);
		// Triggered when someone offers us a teleport
		_Main.getGridClient().Self.OnTeleportLure.add(teleportLureCallback);
		// Triggered when a local chat message is received
		_Main.getGridClient().Self.OnChat.add(chatCallback);
		// Triggered when an alert message is received
		_Main.getGridClient().Self.OnAlertMessage.add(alertCallback);
		// Triggered when an IM is received
		_Main.getGridClient().Self.OnInstantMessage.add(instantCallback);
		// Triggered when the online status of a friend has changed
		_Main.getGridClient().Friends.OnFriendNotification.add(friendNotificationCallback);
		// Triggered when someone has accepted or rejected our friendship request
		_Main.getGridClient().Friends.OnFriendshipResponse.add(friendshipResponseCallback);
		// Triggered when someone has offered us friendship
		_Main.getGridClient().Friends.OnFriendshipOffered.add(friendshipOfferedCallback);
		// Triggered when someone has terminated friendship with us
		_Main.getGridClient().Friends.OnFriendshipTerminated.add(friendshipTerminatedCallback);
		// Triggered when someone has invited us to a group
		_Main.getGridClient().Groups.OnGroupInvitation.add(groupInvitationCallback);

		main.setContentArea(getSceneViewer());
		main.setMenuBar(getJMBar());

		initializePanel();	
	}
	
	protected void finalize() throws Throwable
	{
		_Main.getGridClient().Self.OnBalanceUpdated.remove(balanceUpdateCallback);
		_Main.getGridClient().Self.OnTeleportLure.remove(teleportLureCallback);
		_Main.getGridClient().Self.OnChat.remove(chatCallback);
		_Main.getGridClient().Self.OnAlertMessage.remove(alertCallback);
		_Main.getGridClient().Self.OnInstantMessage.remove(instantCallback);
		_Main.getGridClient().Friends.OnFriendNotification.remove(friendNotificationCallback);
		_Main.getGridClient().Friends.OnFriendshipResponse.remove(friendshipResponseCallback);
		_Main.getGridClient().Friends.OnFriendshipOffered.remove(friendshipOfferedCallback);
		_Main.getGridClient().Friends.OnFriendshipTerminated.remove(friendshipTerminatedCallback);
		_Main.getGridClient().Groups.OnGroupInvitation.remove(groupInvitationCallback);

		super.finalize();
	} 

	public CommWindow getCommWindow()
	{
		if (_Comm == null)
			_Comm = new CommWindow(_Main);
		return _Comm;
	}

	public FriendList getFriendList()
	{
		if (_FriendList == null)
			_FriendList = new FriendList(_Main);
		return _FriendList;
	}
	
	public GroupList getGroupList()
	{
		if (_GroupList == null)
			_GroupList = new GroupList(_Main);
		return _GroupList;
	}

	/**
	 * This method initializes mainJMenuBar
	 * 
	 * @return JMenuBar
	 */
	private JMenuBar getJMBar()
	{
		if (jMbMain == null)
		{
			jMbMain = new JMenuBar();

			JMenu file = new JMenu("File");
			
			JMenuItem jMiFileOpen = _Main.newMenuItem("Open...", this, "open");
			file.add(jMiFileOpen);
			file.addSeparator();

			JMenuItem jMiSettings = _Main.newMenuItem("Settings...", this, MainControl.cmdSettings);
			file.add(jMiSettings);
			file.addSeparator();
			
			JMenuItem jMiFileQuit = _Main.newMenuItem("Quit", this, MainControl.cmdQuit);
			file.add(jMiFileQuit);

			jMbMain.add(file);

			JMenu world = new JMenu("World");
			JMenuItem jMiFriends = _Main.newMenuItem("Friends", this, cmdFriends);
			world.add(jMiFriends);

			JMenuItem jMiGroups = _Main.newMenuItem("Groups", this, cmdGroups);
			world.add(jMiGroups);
			
			JMenuItem jMiInventory = _Main.newMenuItem("Inventory", this, cmdInventory);
			world.add(jMiInventory);
			
			JMenuItem jMiSearch = _Main.newMenuItem("Search", this, cmdSearch);
			world.add(jMiSearch);
			
			JMenuItem jMiMap = _Main.newMenuItem("Map", this, cmdMaps);
			world.add(jMiMap);
			
			JMenuItem jMiObjects = _Main.newMenuItem("Objects", this, cmdObjects);
			world.add(jMiObjects);

			JMenuItem jMiMedia = _Main.newMenuItem("Media", this, cmdMedia);
			world.add(jMiMedia);

			JMenuItem jMiVoice = _Main.newMenuItem("Voice", this, cmdVoice);
			world.add(jMiVoice);

			jMbMain.add(world);

			JMenu help = new JMenu("Help");
			JMenuItem jMiBugReports = _Main.newMenuItem("Bugs/Feature Request...", this, MainControl.cmdBugs);
			help.add(jMiBugReports);

			JMenuItem jMiUpdates = _Main.newMenuItem("Check for Updates...", this, MainControl.cmdUpdates);
			help.add(jMiUpdates);

			JMenuItem jMiDebugConsole = _Main.newMenuItem("Debug Console...", this, MainControl.cmdDebugCon);
			help.add(jMiDebugConsole);

			help.addSeparator();

			JMenuItem jMiAbout = _Main.newMenuItem("About Libomv Client...", this, MainControl.cmdAbout);
			help.add(jMiAbout);
			jMbMain.add(help);
			// jMbMain.setHelpMenu(help); // needed for portability (Motif, etc.).
			
			jMbMain.add(Box.createGlue());

			jMbMain.add(getJAmount());
		}
		return jMbMain;
	}
	
	private JLabel getJAmount()
	{
		if (jMiAmount == null)
		{
			_Main.getGridClient().Self.getBalance();
			jMiAmount = new JLabel(String.format("%s %s", _Main.getGridClient().getGrid(null).currencySym, _Main.getGridClient().Self.getBalance()));
		}
		return jMiAmount;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		/* Handle local events */
		if (e.getActionCommand().equals(cmdFriends) ||
			e.getActionCommand().equals(cmdGroups))
		{
			getCommWindow().setFocus(e.getActionCommand(), null);
			getCommWindow().setVisible(true);
		}
		else if (e.getActionCommand().equals(cmdInventory))
		{
			
		}
		/* Pass to main window to be handled */
		_Main.actionPerformed(e);
	}

	private JPanel getSceneViewer()
	{
		if (jSceneViewer == null)
		{
			jSceneViewer = new JPanel();
		}
		return jSceneViewer;
	}

	private void initializePanel()
	{
	}
	
	private class BalanceUpdate implements Callback<BalanceCallbackArgs>
	{
		@Override
		public boolean callback(BalanceCallbackArgs params)
		{
			String symbol = _Main.getGridClient().getGrid(null).currencySym;
			getJAmount().setText(String.format("%s %s", symbol == null || symbol.isEmpty() ? "$" : symbol, params.getBalance()));
			if (!params.getFirst() && params.getDelta() > 50)
			{
				if (params.getDelta() < 0)
				{
					/* Create money gone sound */
				}
				else
				{
					/* Create cash register sound */
				}
			}
			return false;
		}
		
	}
	
	private class TeleportLure implements Callback<TeleportLureCallbackArgs>
	{
		@Override
		public boolean callback(final TeleportLureCallbackArgs args)
		{
			final UUID agentID = args.getFromID();
			final UUID lureID = args.getLureID();
			
			try
			{
				EventQueue.invokeAndWait(new Runnable()
				{
					public void run()
					{
						int result = JOptionPane.showConfirmDialog(_Main.getMainJFrame(), args.getFromName() +
						                                   " has offered you a teleport with the following message: '" +
						                                   args.getMessage() + "'. Do you wish to accept?",
						                                   "Teleportation Offer", JOptionPane.YES_NO_OPTION);
						args.setAccepted(result == JOptionPane.OK_OPTION);
					}
				});

				// Accept or decline the request
				_Main.getGridClient().Self.TeleportLureRespond(agentID, lureID, args.getAccepted());
			}
			catch (Exception ex)
			{
				Logger.Log("Teleportation failed", LogLevel.Error, _Main.getGridClient(), ex);
			}
			return false;
		}
	}

	private class ChatMessage implements Callback<ChatCallbackArgs>
	{
		@Override
		public boolean callback(final ChatCallbackArgs params)
		{
			if (params.getMessage() == null || params.getMessage().isEmpty())
				return false;
			
	        // Check if the sender agent is muted
	        if (params.getSourceType() == ChatSourceType.Agent)
	        {
	            for (MuteEntry me : _Main.getGridClient().Self.MuteList.values())
	            {
	                if (me.Type == MuteType.Resident && me.ID.equals(params.getSourceID()));
	                	return false;
	            }
	        }

	        // Check if sender object is muted
	        if (params.getSourceType() == ChatSourceType.Object)
	        {
	           for (MuteEntry me : _Main.getGridClient().Self.MuteList.values())
	            {
	                if ((me.Type == MuteType.Resident && me.ID.equals(params.getOwnerID())) || // Owner muted
	                    (me.Type == MuteType.Object && me.ID.equals(params.getSourceID())) || // Object muted by ID
	                    (me.Type == MuteType.ByName && me.Name.equals(params.getFromName()))) // Object muted by name
	                		return false;
	            }
	        }
	        
	        if (_Main.getGridClient().RLV.isEnabled() && params.getMessage().startsWith("@"))
	        {
	        	_Main.getGridClient().RLV.tryProcessCommand(params);
	        	return false;
	        }
	        
	        String fromName = null;
	        if (params.getSourceType() == ChatSourceType.Agent)
	        {
	        	fromName = _Main.getGridClient().Avatars.LocalAvatarNameLookup(params.getSourceID());
	        }
        	if (fromName == null || fromName.isEmpty())
        		fromName = params.getFromName();
        	
	        final String name = fromName;
			try
			{
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
				        getCommWindow().printChatMessage(params.getSourceType(), params.getSourceID(), name, params.getMessage(), params.getType());
					}
				});
			}
			catch (Exception ex)
			{
				Logger.Log("Error invoking to send alert message to local chat", LogLevel.Error, _Main.getGridClient(), ex);
			}
	        return false;
		}
	}
	
	private class AlertMessage implements Callback<AlertMessageCallbackArgs>
	{
		@Override
		public boolean callback(final AlertMessageCallbackArgs params)
		{
			try
			{
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						getCommWindow().printAlertMessage(params.getAlert());
					}
				});
			}
			catch (Exception ex)
			{
				Logger.Log("Error invoking to send alert message to local chat", LogLevel.Error, _Main.getGridClient(), ex);
			}
			return false;
		}		
	}
	
	private class InstantMessage implements Callback<InstantMessageCallbackArgs>
	{
		@Override
		public boolean callback(final InstantMessageCallbackArgs params)
		{
			try
			{
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						getCommWindow().printInstantMessage(params.getIM());
					}
				});
			}
			catch (Exception ex)
			{
				Logger.Log("Error invoking to send alert message to local chat", LogLevel.Error, _Main.getGridClient(), ex);
			}
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
			String verb = e.getAccepted() ? "accepted" : "declined";
			String title = "Friendship " + verb;
			String message = e.getName() + " has " + verb + " your friendship offer.";

			/* Show dialog informing about the decision of the other */
			JOptionPane.showMessageDialog(_Main.getMainJFrame(), message, title, JOptionPane.PLAIN_MESSAGE);		
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
			final UUID uuid = e.getFriendID();
			final UUID session = e.getSessionID();

			/* Prompt user for acceptance of friendship offer */
			int result = JOptionPane.showConfirmDialog(_Main.getMainJFrame(), e.getName() + " wants to be your friend. Do you accept this request?", 
					                                   "Friendship Request", JOptionPane.YES_NO_OPTION);
			try
			{
				if (result == JOptionPane.OK_OPTION)
				{
					_Main.getGridClient().Friends.AcceptFriendship(uuid, session);
				}
				else
				{
					_Main.getGridClient().Friends.DeclineFriendship(uuid, session);
				}
			}
			catch (InventoryException ex)
			{
				Logger.Log("Inventory Exception", LogLevel.Error, _Main.getGridClient(), ex);
			}
			catch (Exception ex)
			{
				Logger.Log("Exception sending response", LogLevel.Error, _Main.getGridClient(), ex);
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
			/* Inform user about the friendship termination */
			JOptionPane.showMessageDialog(_Main.getMainJFrame(), e.getName() + " terminated the friendship.", 
					                                   "Friendship Termination", JOptionPane.PLAIN_MESSAGE);

			return false;
		}
	}
	
	/**
	 * Triggered when a someone invites us to a group
	 */
	private class GroupInvitation implements Callback<GroupInvitationCallbackArgs>
	{
		@Override
		public boolean callback(GroupInvitationCallbackArgs args)
		{
			final UUID groupID = args.getGroupID();
			final UUID session = args.getSessionID();

			int result = JOptionPane.showConfirmDialog(_Main.getMainJFrame(), args.getFromName() +
													   " has invited you to join a group with following message '" +
					                                   args.getMessage() + "'. Do you accept this offer?",
                                                       "Group Invitation", JOptionPane.YES_NO_OPTION);		
			try
			{
				_Main.getGridClient().Self.GroupInviteRespond(groupID, session, result == JOptionPane.OK_OPTION);
			}
			catch (Exception ex)
			{
				Logger.Log("Exception when trying to respond to group invitation", LogLevel.Error, _Main.getGridClient(), ex);
			}
			return false;
		}
	}
}
