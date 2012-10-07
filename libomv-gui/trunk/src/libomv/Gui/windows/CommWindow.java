/**
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
package libomv.Gui.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import libomv.AgentManager.AlertMessageCallbackArgs;
import libomv.AgentManager.ChatCallbackArgs;
import libomv.AgentManager.ChatSourceType;
import libomv.AgentManager.ChatType;
import libomv.AgentManager.InstantMessage;
import libomv.AgentManager.InstantMessageCallbackArgs;
import libomv.AgentManager.InstantMessageDialog;
import libomv.AgentManager.InstantMessageOnline;
import libomv.AgentManager.MuteEntry;
import libomv.AgentManager.MuteType;
import libomv.AgentManager.TeleportLureCallbackArgs;
import libomv.FriendsManager.FriendInfo;
import libomv.FriendsManager.FriendNotificationCallbackArgs;
import libomv.FriendsManager.FriendshipOfferedCallbackArgs;
import libomv.FriendsManager.FriendshipResponseCallbackArgs;
import libomv.FriendsManager.FriendshipTerminatedCallbackArgs;
import libomv.GridClient;
import libomv.GroupManager.GroupInvitationCallbackArgs;
import libomv.Gui.channels.AbstractChannel;
import libomv.Gui.channels.GroupChannel;
import libomv.Gui.channels.PrivateChannel;
import libomv.Gui.channels.LocalChannel;
import libomv.Gui.components.ButtonTabPane;
import libomv.Gui.components.list.FriendList;
import libomv.Gui.components.list.GroupList;
import libomv.core.state.OnlineController;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class CommWindow extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JTabbedPane jTpComm;
	private JTabbedPane jTpContacts;
	
	private MainControl _Main;
	private FriendList _FriendList;
	private GroupList _GroupList;

	private LocalChannel localChat;
	private HashMap<UUID, AbstractChannel> channels;

	private Callback<TeleportLureCallbackArgs> teleportLureCallback = new TeleportLureCallback();
	private Callback<ChatCallbackArgs> chatCallback = new ChatMessageCallback();
	private Callback<AlertMessageCallbackArgs> alertCallback = new AlertMessageCallback();
	private Callback<InstantMessageCallbackArgs> instantCallback = new InstantMessageCallback();
	private Callback<FriendNotificationCallbackArgs> friendNotificationCallback = new FriendNotificationCallback();
	private Callback<FriendshipResponseCallbackArgs> friendshipResponseCallback = new FriendshipResponseCallback();
	private Callback<FriendshipOfferedCallbackArgs> friendshipOfferedCallback = new FriendshipOfferedCallback();
	private Callback<FriendshipTerminatedCallbackArgs> friendshipTerminatedCallback = new FriendshipTerminatedCallback();
	private Callback<GroupInvitationCallbackArgs> groupInvitationCallback = new GroupInvitationCallback();

	public CommWindow(MainControl main)
	{
		super();
		
		_Main = main;
		
		setTitle("Communication");
		
		channels = new HashMap<UUID, AbstractChannel>();
		
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

		// Choose a sensible minimum size.
		setPreferredSize(new Dimension(360, 440));
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(getJTpComm());

		//Display the window.
        pack();
        setVisible(true);
	}
	
	protected void finalize() throws Throwable
	{
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

	/**
	 * Return the chat channel component for the uuid
	 * 
	 * @param uuid The uuid identifying the channel (avatar uuid or group uuid, null returns the local chat channel)
	 * @return the AbstractChannel object to communicate with
	 */
	public AbstractChannel getChannel(UUID uuid)
	{
		if (uuid == null || uuid.equals(UUID.Zero))
		{
			if (localChat == null)
			{
				localChat = new LocalChannel(_Main);
			}
			return localChat;
		}
		return channels.get(uuid);
	}
	
	/**
	 * Make the correct tab active
	 * 
	 * @param focus The main tab to activate. Possible values:
	 *             null or empty          : communication channels
	 *             OnlinePanel.cmdFriends : friend list
	 *             OnlinePanel.cmdGroups  : group list
	 * @param uuid The channel UUID to activate. null activates the local chat channel 
	 */
	
	public void setFocus(String focus, UUID uuid)
	{
		if (focus == null || focus.isEmpty())
		{
			getJTpComm().setSelectedIndex(getJTpComm().indexOfComponent(getChannel(uuid)));
		}
		else
		{
			getJTpComm().setSelectedIndex(0);
			if (focus.equals(OnlineController.cmdFriends))
			{
				getJTpContacts().setSelectedIndex(0);
			}
			else if (focus.equals(OnlineController.cmdGroups))
			{
				getJTpContacts().setSelectedIndex(1);
			}
		}
	}

	private FriendList getFriendList()
	{
		if (_FriendList == null)
			_FriendList = new FriendList(_Main, this);
		return _FriendList;
	}
	
	private GroupList getGroupList()
	{
		if (_GroupList == null)
			_GroupList = new GroupList(_Main, this);
		return _GroupList;
	}

	private JTabbedPane getJTpComm()
	{
		if (jTpComm == null)
		{
			jTpComm = new JTabbedPane(JTabbedPane.BOTTOM);
			jTpComm.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			jTpComm.setBorder(new LineBorder(new Color(0, 0, 0)));

			jTpComm.add("Contacts", getJTpContacts());
			jTpComm.add("Local Chat", getChannel(null));
			// Install container listener so we can detect tab panes removed through the tab close button
			jTpComm.addContainerListener(new ContainerListener()
			{
				@Override
				public void componentAdded(ContainerEvent e)
				{
					// Nothing to do now
				}

				@Override
				public void componentRemoved(ContainerEvent e)
				{
					Component comp = e.getComponent();
					if (comp instanceof AbstractChannel)
					{
						channels.remove(((AbstractChannel)comp).getUUID());
					}
				}
				
			});
			jTpComm.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent e)
				{
					Component comp = getJTpComm().getSelectedComponent();
					if (comp instanceof AbstractChannel)
					{
						highlightChannel((AbstractChannel)comp, false);
					}
				}
			});
		}
		return jTpComm;
	}
	
    private JTabbedPane getJTpContacts()
	{
		if (jTpContacts == null)
		{
			jTpContacts = new JTabbedPane(JTabbedPane.TOP);
			jTpContacts.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			jTpContacts.setBorder(new LineBorder(new Color(0, 0, 0)));

			jTpContacts.add("Friends", getFriendList());
			jTpContacts.add("Groups", getGroupList());
		}
		return jTpContacts;
	}

	public boolean addChannel(AbstractChannel channel)
	{
		if (channels.put(channel.getUUID(), channel) == null)
		{
			getJTpComm().add(channel.getName(), channel);
			getJTpComm().setTabComponentAt(getJTpComm().indexOfComponent(channel), new ButtonTabPane(getJTpComm()));
			getJTpComm().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			return true;
		}
		return false;
	}
	
	public void removeChannel(AbstractChannel channel)
	{
		getJTpComm().remove(channel);
	}
	
	public void highlightChannel(AbstractChannel channel, boolean highlight)
	{
		int index = getJTpComm().indexOfComponent(channel);
		if (index >= 0)
		{
			getJTpComm().setBackgroundAt(index, highlight && index != getJTpComm().getSelectedIndex() ? Color.orange : null);
		}
	}

	private class TeleportLureCallback implements Callback<TeleportLureCallbackArgs>
	{
		@Override
		public boolean callback(final TeleportLureCallbackArgs args)
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					int result = JOptionPane.showConfirmDialog(_Main.getJFrame(), args.getFromName() +
					                                   " has offered you a teleport with the following message: '" +
					                                   args.getMessage() + "'. Do you wish to accept?",
					                                   "Teleportation Offer", JOptionPane.YES_NO_OPTION);
					// Accept or decline the request
					try
					{
						_Main.getGridClient().Self.TeleportLureRespond(args.getFromID(), args.getLureID(), result == JOptionPane.OK_OPTION);
					}
					catch (Exception ex)
					{
						Logger.Log("Response to teleportation invite failed", LogLevel.Error, _Main.getGridClient(), ex);
					}
				}
			});
			return false;
		}
	}

	private class AlertMessageCallback implements Callback<AlertMessageCallbackArgs>
	{
		@Override
		public boolean callback(final AlertMessageCallbackArgs params)
		{
			//workaround the stupid autopilot alerts
			if (!params.getAlert().toLowerCase().contains("autopilot canceled"))
			{
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
			            try
			            {
			                AbstractChannel channel = getChannel(null);
			                channel.receiveMessage(null, null, Helpers.EmptyString, params.getAlert(), AbstractChannel.STYLE_SYSTEM);		
			                highlightChannel(channel, true);
			        	}
			            catch (BadLocationException ex)
			            {
			        		Logger.Log("Failed to send alert message", LogLevel.Error, _Main.getGridClient(), ex);
			            }
					}
				});
		 	}
			return false;
		}		
	}
	
    private class ChatMessageCallback implements Callback<ChatCallbackArgs>
	{
		@Override
		public boolean callback(final ChatCallbackArgs params)
		{
			final GridClient client = _Main.getGridClient();
			// Do some checks first to determine if we should ignore the message
			switch (params.getSourceType())
			{
				case Agent:
			        // Check if the sender agent is muted
		            for (MuteEntry me : client.Self.MuteList.values())
		            {
		                if (me.Type == MuteType.Resident && me.ID.equals(params.getSourceID()));
		                	return false;
		            }
		            break;
				case Object:
			        // Check if sender object is muted
		            for (MuteEntry me : client.Self.MuteList.values())
		            {
		                if ((me.Type == MuteType.Resident && me.ID.equals(params.getOwnerID())) || // Owner muted
		                    (me.Type == MuteType.Object && me.ID.equals(params.getSourceID())) || // Object muted by ID
		                    (me.Type == MuteType.ByName && me.Name.equals(params.getFromName()))) // Object muted by name
		                		return false;
		            }
			}
	        
			if (params.getMessage().startsWith("@") && client.RLV != null)
	        {
				client.RLV.tryProcessCommand(params);
	        	return false;
	        }

			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					ChatSourceType sourceType = params.getSourceType();
					UUID sourceID = params.getSourceID();
					String message = params.getMessage();
			        
			        String fromName = null;
			        if (sourceType == ChatSourceType.Agent)
			        {
			        	fromName = client.Avatars.LocalAvatarNameLookup(sourceID);
			        }
		        	if (fromName == null || fromName.isEmpty())
		        		fromName = params.getFromName();
		        	
		            try
		            {
		                AbstractChannel channel = getChannel(null);
			            String style = AbstractChannel.STYLE_REGULAR;
						StringBuilder localMessage = new StringBuilder();
			            switch (params.getType())
			            {
		            		case StartTyping:
		            			localMessage.append(fromName + " is typing");
		            		case StopTyping:
			                	channel.receiveStatus(sourceID, localMessage.toString(), AbstractChannel.STYLE_SYSTEM);
			                	break;
			                default:
								if (message != null && !message.isEmpty())
								{
						            if (!message.toLowerCase().startsWith("/me "))
						            {
						            	switch (params.getType())
						            	{
						    				case Shout:
						    					localMessage.append(" shouts");
						    					break;
						    				case Whisper:
						    					localMessage.append(" whispers");
						    					break;
						            	}
						            	localMessage.append(": ");
						                if (sourceType == ChatSourceType.Agent && !message.startsWith("/") && client.RLV != null && client.RLV.restrictionActive("recvchat", sourceID.toString()))
						                	localMessage.append("...");
						                else
						                	localMessage.append(message);
						    		}
						            else
						            {
						                if (sourceType == ChatSourceType.Agent && client.RLV != null && client.RLV.restrictionActive("recvemote", sourceID.toString()))
						                	localMessage.append(" ...");
						                else
						                	localMessage.append(message.substring(3));
						                style = AbstractChannel.STYLE_ACTION;
						            }

						            switch (params.getSourceType())
						            {
						                case Agent:
						                	if (fromName.endsWith("Linden"))
						                		style = AbstractChannel.STYLE_SYSTEM;
						                    break;

						                case Object:
						                    if (params.getType() == ChatType.OwnerSay)
						                    {
						                        style = AbstractChannel.STYLE_OBJECT;
						                    }
						                    else
						                    {
						                        style = AbstractChannel.STYLE_OBJECT;
						                    }
						                    break;
						            }

					                channel.receiveMessage(null, sourceID, fromName, localMessage.toString(), style);		
					                highlightChannel(channel, true);
								}
			            }
			        }
			        catch (Exception ex)
			        {
			        	Logger.Log("Error invoking to send alert message to local chat", LogLevel.Error, client, ex);
			        }
				}
			});
	        return false;
		}
	}
	
	private class InstantMessageCallback implements Callback<InstantMessageCallbackArgs>
	{
		@Override
		public boolean callback(final InstantMessageCallbackArgs params)
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					InstantMessage message = params.getIM();
					GridClient client = _Main.getGridClient();
					AbstractChannel channel = getChannel(message.FromAgentID);
					if (channel == null && (message.Message == null || message.Message.isEmpty()))
					{
						// if the channel doesn't exist yet and we don't have a real message to report, return now
						return;
					}
					
					if (message.GroupIM)
					{
						if (channel == null)
						{
							channel = new GroupChannel(_Main, message.FromAgentName, message.FromAgentID, message.IMSessionID);
							addChannel(channel);
						}
					}
					else
					{
						if (channel == null)
						{
							channel = new PrivateChannel(_Main, message.FromAgentName, message.FromAgentID, message.IMSessionID);
							addChannel(channel);
						}
					}

					try
					{
						String style = AbstractChannel.STYLE_OBJECT;
						String localMessage = null;
						switch (message.Dialog)
						{
							case StartTyping:
								localMessage = message.FromAgentName + " is typing";
							case StopTyping:
			                	channel.receiveStatus(message.FromAgentID, localMessage, AbstractChannel.STYLE_SYSTEM);
			                	break;
							case MessageFromAgent:
								style = AbstractChannel.STYLE_REGULAR;
							case MessageFromObject:
								if (message.Offline == InstantMessageOnline.Offline)
									style = AbstractChannel.STYLE_OFFLINE;

								if (client.RLV != null && client.RLV.restrictionActive("recvchat", message.FromAgentID.toString()))
								{
									localMessage = "*** IM blocked by your viewer";
									client.Self.InstantMessage(client.Self.getName(), params.getIM().FromAgentID,
											 "***  The Resident you messaged is prevented from reading your instant messages at the moment, please try again later.",
											params.getIM().IMSessionID, InstantMessageDialog.BusyAutoResponse, InstantMessageOnline.Offline);
									
								}
								if (message.Message.toLowerCase().startsWith("/me "))
					            {
					            	localMessage = message.Message.substring(3);
					            }
								else
								{
									localMessage = ": " + message.Message;
								}
					            channel.receiveMessage(message.Timestamp, message.FromAgentID, message.FromAgentName, localMessage, style);
								highlightChannel(channel, true);
						}
					}
				    catch (Exception ex)
				    {
						Logger.Log("Failed to print alert message", LogLevel.Error, _Main.getGridClient(), ex);
				    }
				}
			});
			return false;
		}		
	}

	/**
	 * Triggered when online status changes are reported
	 */
	private class FriendNotificationCallback implements Callback<FriendNotificationCallbackArgs>
	{
		@Override
		public boolean callback(final FriendNotificationCallbackArgs e)
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
				    AbstractChannel channel = getChannel(null);
					String message = e.getOnline() ? " is online" : " is offline";
					GridClient client = _Main.getGridClient();
					for (UUID uuid : e.getAgentID())
					{
			        	FriendInfo info = client.Friends.getFriendList().get(uuid);
					    try
					    {
							channel.receiveMessage(null, uuid, info.getName(), message, AbstractChannel.STYLE_INFORMATIONAL);
							highlightChannel(channel, true);
						}
					    catch (BadLocationException ex)
					    {
					    	Logger.Log("Failed to print alert message", LogLevel.Error, client, ex);
					    }
					}
				}
			});
			return false;
		}
	}

	/**
	 * Triggered when a user has accepted or declined our friendship offer
	 */
	private class FriendshipResponseCallback implements Callback<FriendshipResponseCallbackArgs>
	{
		@Override
		public boolean callback(FriendshipResponseCallbackArgs e)
		{
			String verb = e.getAccepted() ? "accepted" : "declined";
			final String title = "Friendship " + verb;
			final String message = e.getName() + " has " + verb + " your friendship offer.";

			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					/* Show dialog informing about the decision of the other */
					JOptionPane.showMessageDialog(_Main.getJFrame(), message, title, JOptionPane.PLAIN_MESSAGE);		
				}
			});
			return false;
		}
	}

	/**
	 * Triggered when a user has sent us a friendship offer
	 */
	private class FriendshipOfferedCallback implements Callback<FriendshipOfferedCallbackArgs>
	{
		@Override
		public boolean callback(final FriendshipOfferedCallbackArgs e)
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					GridClient client = _Main.getGridClient();
					/* Prompt user for acceptance of friendship offer */
					int result = JOptionPane.showConfirmDialog(_Main.getJFrame(), e.getName() + " wants to be your friend. Do you accept this request?", "Friendship Request", JOptionPane.YES_NO_OPTION);
					try
					{
						if (result == JOptionPane.OK_OPTION)
						{
							client.Friends.AcceptFriendship(e.getFriendID(), e.getSessionID());
						}
						else
						{
							client.Friends.DeclineFriendship(e.getFriendID(), e.getSessionID());
						}
					}
					catch (Exception ex)
					{
				    	Logger.Log("Failed to answer to friendship offer", LogLevel.Error, client, ex);
					}
				}
			});
			return false;
		}
	}

	/**
	 * Triggered when a user has removed us from their friends list
	 */
	private class FriendshipTerminatedCallback implements Callback<FriendshipTerminatedCallbackArgs>
	{
		@Override
		public boolean callback(final FriendshipTerminatedCallbackArgs e)
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					/* Inform user about the friendship termination */
					JOptionPane.showMessageDialog(_Main.getJFrame(), e.getName() + " terminated the friendship.", 
							                                   "Friendship Termination", JOptionPane.PLAIN_MESSAGE);
				}
			});
			return false;
		}
	}
	
	/**
	 * Triggered when someone invites us to a group
	 */
	private class GroupInvitationCallback implements Callback<GroupInvitationCallbackArgs>
	{
		@Override
		public boolean callback(final GroupInvitationCallbackArgs args)
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					GridClient client = _Main.getGridClient();
					int result = JOptionPane.showConfirmDialog(_Main.getJFrame(), args.getFromName() +
															   " has invited you to join a group with following message '" +
							                                   args.getMessage() + "'. Do you accept this offer?",
		                                                       "Group Invitation", JOptionPane.YES_NO_OPTION);		
					try
					{
						client.Self.GroupInviteRespond(args.getGroupID(), args.getSessionID(), result == JOptionPane.OK_OPTION);
					}
					catch (Exception ex)
					{
						Logger.Log("Exception when trying to respond to group invitation", LogLevel.Error, client, ex);
					}
				}
			});
			return false;
		}
	}
}
