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
package libomv.Gui.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import libomv.Gui.StateController;
import libomv.Gui.channels.AbstractChannel;
import libomv.Gui.channels.GroupChannel;
import libomv.Gui.channels.LocalChannel;
import libomv.Gui.channels.PrivateChannel;
import libomv.Gui.components.ButtonTabPane;
import libomv.Gui.components.list.FriendList;
import libomv.Gui.components.list.GroupList;
import libomv.io.GridClient;
import libomv.model.agent.AlertMessageCallbackArgs;
import libomv.model.agent.ChatCallbackArgs;
import libomv.model.agent.ChatSourceType;
import libomv.model.agent.ChatType;
import libomv.model.agent.InstantMessage;
import libomv.model.agent.InstantMessageCallbackArgs;
import libomv.model.agent.InstantMessageDialog;
import libomv.model.agent.InstantMessageOnline;
import libomv.model.agent.MuteEntry;
import libomv.model.agent.MuteType;
import libomv.model.friend.FriendInfo;
import libomv.model.friend.FriendNotificationCallbackArgs;
import libomv.model.friend.FriendshipResponseCallbackArgs;
import libomv.model.friend.FriendshipTerminatedCallbackArgs;
import libomv.types.Predicate;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Helpers;

public class CommWindow extends JFrame {
	private static final Logger logger = Logger.getLogger(CommWindow.class);
	private static final long serialVersionUID = 1L;

	private JTabbedPane jTpComm;
	private JTabbedPane jTpContacts;

	private MainControl _Main;
	private GridClient _Client;
	private FriendList _FriendList;
	private GroupList _GroupList;

	private LocalChannel localChat;
	private HashMap<UUID, AbstractChannel> channels;

	private Callback<ChatCallbackArgs> chatCallback = new ChatMessageCallback();
	private Callback<AlertMessageCallbackArgs> alertCallback = new AlertMessageCallback();
	private Callback<InstantMessageCallbackArgs> instantCallback = new InstantMessageCallback();
	private Callback<FriendNotificationCallbackArgs> friendNotificationCallback = new FriendNotificationCallback();
	private Callback<FriendshipResponseCallbackArgs> friendshipResponseCallback = new FriendshipResponseCallback();
	private Callback<FriendshipTerminatedCallbackArgs> friendshipTerminatedCallback = new FriendshipTerminatedCallback();

	public CommWindow(MainControl main) {
		super();

		_Main = main;
		_Client = main.getGridClient();

		setTitle("Communication");

		channels = new HashMap<UUID, AbstractChannel>();

		// Triggered when a local chat message is received
		_Client.Self.OnChat.add(chatCallback);
		// Triggered when an alert message is received
		_Client.Self.OnAlertMessage.add(alertCallback);
		// Triggered when an IM is received
		_Client.Self.OnInstantMessage.add(instantCallback);
		// Triggered when the online status of a friend has changed
		_Client.Friends.OnFriendNotification.add(friendNotificationCallback);
		// Triggered when someone has accepted or rejected our friendship request
		_Client.Friends.OnFriendshipResponse.add(friendshipResponseCallback);
		// Triggered when someone has terminated friendship with us
		_Client.Friends.OnFriendshipTerminated.add(friendshipTerminatedCallback);

		// Choose a sensible minimum size.
		setPreferredSize(new Dimension(360, 440));
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(getJTpComm());

		// Display the window.
		pack();
		setVisible(true);
	}

	protected void finalize() throws Throwable {
		_Client.Self.OnChat.remove(chatCallback);
		_Client.Self.OnAlertMessage.remove(alertCallback);
		_Client.Self.OnInstantMessage.remove(instantCallback);
		_Client.Friends.OnFriendNotification.remove(friendNotificationCallback);
		_Client.Friends.OnFriendshipResponse.remove(friendshipResponseCallback);
		_Client.Friends.OnFriendshipTerminated.remove(friendshipTerminatedCallback);

		super.finalize();
	}

	/**
	 * Return the chat channel component for the uuid
	 *
	 * @param uuid
	 *            The uuid identifying the channel (avatar uuid or group uuid, null
	 *            returns the local chat channel)
	 * @return the AbstractChannel object to communicate with
	 */
	public AbstractChannel getChannel(UUID uuid) {
		if (uuid == null || uuid.equals(UUID.Zero)) {
			if (localChat == null) {
				localChat = new LocalChannel(_Main);
			}
			return localChat;
		}
		return channels.get(uuid);
	}

	/**
	 * Make the correct tab active
	 *
	 * @param focus
	 *            The main tab to activate. Possible values: null or empty :
	 *            communication channels OnlinePanel.cmdFriends : friend list
	 *            OnlinePanel.cmdGroups : group list
	 * @param uuid
	 *            The channel UUID to activate. null activates the local chat
	 *            channel
	 */

	public void setFocus(String focus, UUID uuid) {
		if (focus == null || focus.isEmpty()) {
			getJTpComm().setSelectedIndex(getJTpComm().indexOfComponent(getChannel(uuid)));
		} else {
			getJTpComm().setSelectedIndex(0);
			if (focus.equals(StateController.cmdFriends)) {
				getJTpContacts().setSelectedIndex(0);
			} else if (focus.equals(StateController.cmdGroups)) {
				getJTpContacts().setSelectedIndex(1);
			}
		}
	}

	private FriendList getFriendList() {
		if (_FriendList == null)
			_FriendList = new FriendList(_Main, this);
		return _FriendList;
	}

	private GroupList getGroupList() {
		if (_GroupList == null)
			_GroupList = new GroupList(_Main, this);
		return _GroupList;
	}

	private JTabbedPane getJTpComm() {
		if (jTpComm == null) {
			jTpComm = new JTabbedPane(JTabbedPane.BOTTOM);
			jTpComm.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			jTpComm.setBorder(new LineBorder(new Color(0, 0, 0)));

			jTpComm.add("Contacts", getJTpContacts());
			jTpComm.add("Local Chat", getChannel(null));
			// Install container listener so we can detect tab panes removed through the tab
			// close button
			jTpComm.addContainerListener(new ContainerAdapter() {
				@Override
				public void componentRemoved(ContainerEvent e) {
					Component comp = e.getChild();
					if (comp instanceof AbstractChannel) {
						channels.remove(((AbstractChannel) comp).getUUID());
					}
				}

			});
			jTpComm.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					Component comp = getJTpComm().getSelectedComponent();
					if (comp instanceof AbstractChannel) {
						highlightChannel((AbstractChannel) comp, false);
					}
				}
			});
		}
		return jTpComm;
	}

	private JTabbedPane getJTpContacts() {
		if (jTpContacts == null) {
			jTpContacts = new JTabbedPane(JTabbedPane.TOP);
			jTpContacts.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			jTpContacts.setBorder(new LineBorder(new Color(0, 0, 0)));

			jTpContacts.add("Friends", getFriendList());
			jTpContacts.add("Groups", getGroupList());
		}
		return jTpContacts;
	}

	public boolean addChannel(AbstractChannel channel) {
		return addChannel(channel.getUUID(), channel);
	}

	public boolean addChannel(UUID uuid, AbstractChannel channel) {
		if (channels.put(uuid, channel) == null) {
			getJTpComm().add(channel.getName(), channel);
			getJTpComm().setTabComponentAt(getJTpComm().indexOfComponent(channel), new ButtonTabPane(getJTpComm()));
			getJTpComm().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			return true;
		}
		return false;
	}

	public void removeChannel(AbstractChannel channel) {
		getJTpComm().remove(channel);
	}

	/**
	 * Displays notification in the local chat tab
	 *
	 * @param fromName
	 *            Name from whom the message is
	 * @param msg
	 *            Message to be printed in the chat tab
	 * @param style
	 *            Style of the message to be printed, normal, object, etc
	 * @param highlightChatTab
	 *            Highlight (and flash in taskbar) chat tab if not selected
	 */
	public void displayInLocalChat(String fromName, String msg, String style, boolean highlightChatTab)
			throws BadLocationException {
		AbstractChannel channel = getChannel(null);
		channel.receiveMessage(null, null, fromName, ": " + msg, style);
		if (highlightChatTab)
			highlightChannel(channel, true);
	}

	public void highlightChannel(AbstractChannel channel, boolean highlight) {
		int index = getJTpComm().indexOfComponent(channel);
		if (index >= 0) {
			getJTpComm().setBackgroundAt(index,
					highlight && index != getJTpComm().getSelectedIndex() ? Color.orange : null);
		}
	}

	private class AlertMessageCallback implements Callback<AlertMessageCallbackArgs> {
		@Override
		public boolean callback(final AlertMessageCallbackArgs params) {
			// workaround the stupid autopilot alerts
			if (!params.getAlert().toLowerCase().contains("autopilot canceled")) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							AbstractChannel channel = getChannel(null);
							channel.receiveMessage(null, null, Helpers.EmptyString, params.getAlert(),
									AbstractChannel.STYLE_SYSTEM);
							highlightChannel(channel, true);
						} catch (BadLocationException ex) {
							logger.error(GridClient.Log("Failed to send alert message", _Client), ex);
						}
					}
				});
			}
			return false;
		}
	}

	private class ChatMessageCallback implements Callback<ChatCallbackArgs> {
		@Override
		public boolean callback(final ChatCallbackArgs params) {
			boolean muted = false;
			// Do some checks first to determine if we should ignore the message
			switch (params.getSourceType()) {
			case Agent:
				// Check if the sender agent is muted
				muted = _Client.Self.findMuteEntry(new Predicate<MuteEntry>() {
					public boolean evaluate(MuteEntry me) {
						return (me.Type == MuteType.Resident && me.ID.equals(params.getSourceID()));
					}
				});
				break;
			case Object:
				// Check if sender object is muted
				muted = _Client.Self.findMuteEntry(new Predicate<MuteEntry>() {
					public boolean evaluate(MuteEntry me) {
						return ((me.Type == MuteType.Resident && me.ID.equals(params.getOwnerID())) || // Owner muted
						(me.Type == MuteType.Object && me.ID.equals(params.getSourceID())) || // Object muted by ID
						(me.Type == MuteType.ByName && me.Name.equals(params.getFromName()))); // Object muted by name
					}
				});
				break;
			default:
				break;
			}

			if (muted) {
				return false;
			}

			if (params.getMessage().startsWith("@")) {
				_Main.getStateControl().RLV.tryProcessCommand(params);
				return false;
			}

			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					ChatSourceType sourceType = params.getSourceType();
					UUID sourceID = params.getSourceID();
					String message = params.getMessage();

					String fromName = null;
					if (sourceType == ChatSourceType.Agent) {
						fromName = _Client.Avatars.LocalAvatarNameLookup(sourceID);
					}
					if (fromName == null || fromName.isEmpty())
						fromName = params.getFromName();

					AbstractChannel channel = getChannel(null);
					String style = AbstractChannel.STYLE_REGULAR;
					StringBuilder localMessage = new StringBuilder();
					try {
						switch (params.getType()) {
						case StartTyping:
							localMessage.append(fromName + " is typing");
						case StopTyping:
							channel.receiveStatus(sourceID, localMessage.toString(), AbstractChannel.STYLE_SYSTEM);
							break;
						default:
							if (message != null && !message.isEmpty()) {
								if (!message.toLowerCase().startsWith("/me ")) {
									switch (params.getType()) {
									case Shout:
										localMessage.append(" shouts");
										break;
									case Whisper:
										localMessage.append(" whispers");
										break;
									default:
										break;
									}
									localMessage.append(": ");
									if (sourceType == ChatSourceType.Agent && !message.startsWith("/")
											&& _Main.getStateControl().RLV.restrictionActive("recvchat",
													sourceID.toString()))
										localMessage.append("...");
									else
										localMessage.append(message);
								} else {
									if (sourceType == ChatSourceType.Agent && _Main.getStateControl().RLV
											.restrictionActive("recvemote", sourceID.toString()))
										localMessage.append(" ...");
									else
										localMessage.append(message.substring(3));
									style = AbstractChannel.STYLE_ACTION;
								}

								switch (params.getSourceType()) {
								case Agent:
									if (fromName.endsWith("Linden"))
										style = AbstractChannel.STYLE_SYSTEM;
									break;

								case Object:
									if (params.getType() == ChatType.OwnerSay) {
										style = AbstractChannel.STYLE_OBJECT;
									} else {
										style = AbstractChannel.STYLE_OBJECT;
									}
									break;
								default:
									break;
								}
								channel.receiveMessage(null, sourceID, fromName, localMessage.toString(), style);
								highlightChannel(channel, true);
							}
						}
					} catch (Exception ex) {
						logger.error(GridClient.Log("Error invoking to send alert message to local chat", _Client), ex);
					}
				}
			});
			return false;
		}
	}

	private class InstantMessageCallback implements Callback<InstantMessageCallbackArgs> {
		@Override
		public boolean callback(final InstantMessageCallbackArgs params) {
			final InstantMessage message = params.getIM();

			// Message from someone we muted?
			if (_Client.Self.findMuteEntry(new Predicate<MuteEntry>() {
				public boolean evaluate(MuteEntry me) {
					return (me.Type == MuteType.Resident && me.ID.equals(message.FromAgentID));
				}
			})) {
				return false;
			}

			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						switch (message.Dialog) {
						case SessionSend:
							if (_Client.Groups.GroupList.containsKey(message.IMSessionID)) {
								handleIM(message, true);
							} else {
								handleConferenceIM(message);
							}
							break;
						case MessageFromAgent:
							if (message.FromAgentName.equals("Second Life")) {
								handleIMFromObject(message);
							} else if (message.GroupIM || _Client.Groups.GroupList.containsKey(message.IMSessionID)) {
								handleIM(message, true);
							} else if (message.BinaryBucket.length > 1) { // conference
								handleConferenceIM(message);
							} else {
								handleIM(message, false);
							}
							break;
						case MessageFromObject:
							handleIMFromObject(message);
							break;
						case StartTyping:
						case StopTyping:
							handleTypingStatus(message, message.Dialog == InstantMessageDialog.StartTyping);
							break;
						case MessageBox:
							// addNotification(new GenericNotification(this, message.Message));
							logger.warn(GridClient.Log("Generic notice. Dialog not yet implemented", _Client));
							break;
						case RequestTeleport:
							if (_Main.getStateControl().RLV.autoAcceptTP(message.FromAgentID)) {
								displayInLocalChat("", "Auto accepting teleport from " + message.FromAgentName,
										AbstractChannel.STYLE_REGULAR, true);
								_Client.Self.TeleportLureRespond(message.FromAgentID, message.IMSessionID, true);
							} else {
								answerTeleportLure(message);
							}
							break;
						case GroupInvitation:
							answerGroupInvitation(message);
							break;
						case FriendshipOffered:
							if (message.FromAgentName.equals("Second Life")) {
								handleIMFromObject(message);
							} else {
								answerFriendshipPrompt(message);
							}
							break;
						case InventoryAccepted:
							displayInLocalChat(message.FromAgentName, " accepted your inventory offer.",
									AbstractChannel.STYLE_REGULAR, true);
							break;
						case InventoryDeclined:
							displayInLocalChat(message.FromAgentName, " declined your inventory offer.",
									AbstractChannel.STYLE_REGULAR, true);
							break;
						case GroupNotice:
							// Is this group muted?
							if (!_Client.Self.findMuteEntry(new Predicate<MuteEntry>() {
								public boolean evaluate(MuteEntry me) {
									return (me.Type == MuteType.Group && me.ID.equals(message.FromAgentID));
								}
							})) {
								// addNotification(new GroupNoticeNotification(this, message));
								logger.warn(GridClient.Log("Group notice. Dialog not yet implemented", _Client));
							}
							break;
						case InventoryOffered:
							// addNotification(new InventoryOfferNotification(this, message));
							logger.warn(GridClient.Log("Inventory offered. Dialog not yet implemented", _Client));
							break;
						case TaskInventoryOffered:
							// Is the object muted by name?
							if (!_Client.Self.findMuteEntry(new Predicate<MuteEntry>() {
								public boolean evaluate(MuteEntry me) {
									return (me.Type == MuteType.ByName && me.Name.equals(message.FromAgentName));
								}
							})) {
								// addNotification(new InventoryOfferNotification(this, message));
								logger.warn(GridClient.Log("Inventory offered. Dialog not yet implemented", _Client));
							}
							break;
						default:
							break;
						}
					} catch (Exception ex) {
						logger.error(GridClient.Log("Failed to dispatch InstantMessage", _Client), ex);
					}
				}
			});
			return false;
		}
	}

	private void handleTypingStatus(InstantMessage message, boolean typing) throws BadLocationException {
		AbstractChannel channel = getChannel(null);
		channel.receiveStatus(message.FromAgentID, typing ? message.FromAgentName + " is typing" : null,
				AbstractChannel.STYLE_SYSTEM);
	}

	private void handleIMFromObject(final InstantMessage message) throws BadLocationException {
		// Is the object or the owner muted?
		if (!_Client.Self.findMuteEntry(new Predicate<MuteEntry>() {
			public boolean evaluate(MuteEntry me) {
				return ((me.Type == MuteType.Object && me.ID.equals(message.IMSessionID)) || // muted object by id
				(me.Type == MuteType.ByName && me.Name.equals(message.FromAgentName)) || // object muted by name
				(me.Type == MuteType.Resident && me.ID.equals(message.FromAgentID))); // object's owner muted
			}
		})) {
			displayInLocalChat(message.FromAgentName, message.Message, AbstractChannel.STYLE_OBJECT, true);
		}
	}

	private void handleIM(final InstantMessage message, boolean group) throws Exception {
		if (group) {
			// Ignore group IM from a muted group
			if (_Client.Self.findMuteEntry(new Predicate<MuteEntry>() {
				public boolean evaluate(MuteEntry me) {
					return (me.Type == MuteType.Group
							&& (me.ID.equals(message.IMSessionID) || me.ID.equals(message.FromAgentID)));
				}
			})) {
				return;
			}
		}

		AbstractChannel channel = getChannel(message.FromAgentID);
		if (channel == null) {
			if (message.Message == null || message.Message.isEmpty()) {
				// if the channel doesn't exist yet and we don't have a real message to report,
				// return now
				return;
			}

			if (group) {
				channel = new GroupChannel(_Main, message.FromAgentName, message.FromAgentID, message.IMSessionID);
			} else {
				channel = new PrivateChannel(_Main, message.FromAgentName, message.FromAgentID, message.IMSessionID);
			}
			addChannel(channel);
		}

		String style = AbstractChannel.STYLE_REGULAR;
		String localMessage = null;
		if (message.Offline == InstantMessageOnline.Offline)
			style = AbstractChannel.STYLE_OFFLINE;

		if (_Main.getStateControl().RLV.restrictionActive("recvchat", message.FromAgentID)) {
			localMessage = "*** IM blocked by your viewer";
			_Client.Self.InstantMessage(_Client.Self.getName(), message.FromAgentID,
					"***  The Resident you messaged is prevented from reading your instant messages at the moment, please try again later.",
					message.IMSessionID, InstantMessageDialog.BusyAutoResponse, InstantMessageOnline.Offline);

		}

		// _Main.MediaManager.PlayUISound(UISounds.IM);

		if (message.Message.toLowerCase().startsWith("/me ")) {
			localMessage = message.Message.substring(3);
		} else {
			localMessage = ": " + message.Message;
		}
		channel.receiveMessage(message.Timestamp, message.FromAgentID, message.FromAgentName, localMessage, style);
		highlightChannel(channel, true);
	}

	private void handleConferenceIM(InstantMessage message) throws BadLocationException {
		AbstractChannel channel = getChannel(message.IMSessionID);
		if (channel == null) {
			if (message.Message == null || message.Message.isEmpty()) {
				// if the channel doesn't exist yet and we don't have a real message to report,
				// return now
				return;
			}
			channel = new PrivateChannel(_Main, message.FromAgentName, message.FromAgentID, message.IMSessionID);
			addChannel(channel);
		}

		String style = AbstractChannel.STYLE_REGULAR;
		String localMessage = null;

		// _Main.MediaManager.PlayUISound(UISounds.IM);

		if (message.Message.toLowerCase().startsWith("/me ")) {
			localMessage = message.Message.substring(3);
		} else {
			localMessage = ": " + message.Message;
		}
		channel.receiveMessage(message.Timestamp, message.FromAgentID, message.FromAgentName, localMessage, style);
		highlightChannel(channel, true);
	}

	private void answerTeleportLure(InstantMessage message) {
		int result = JOptionPane.showConfirmDialog(
				_Main.getJFrame(), message.FromAgentName + " has offered you a teleport with the following message: '"
						+ message.Message + "'. Do you wish to accept?",
				"Teleportation Offer", JOptionPane.YES_NO_OPTION);
		// Accept or decline the request
		try {
			_Client.Self.TeleportLureRespond(message.FromAgentID, message.IMSessionID, result == JOptionPane.OK_OPTION);
		} catch (Exception ex) {
			logger.error(GridClient.Log("Response to teleportation invite failed", _Client), ex);
		}
	}

	private void answerFriendshipPrompt(InstantMessage message) {
		/* Prompt user for acceptance of friendship offer */
		int result = JOptionPane.showConfirmDialog(_Main.getJFrame(),
				message.FromAgentName + " wants to be your friend. Do you accept this request?", "Friendship Request",
				JOptionPane.YES_NO_OPTION);
		try {
			if (result == JOptionPane.OK_OPTION) {
				_Client.Friends.AcceptFriendship(message.FromAgentID, message.IMSessionID);
			} else {
				_Client.Friends.DeclineFriendship(message.FromAgentID, message.IMSessionID);
			}
		} catch (Exception ex) {
			logger.error(GridClient.Log("Failed to answer to friendship offer", _Client), ex);
		}
	}

	private void answerGroupInvitation(InstantMessage message) {
		int result = JOptionPane.showConfirmDialog(
				_Main.getJFrame(), message.FromAgentName + " has invited you to join a group with following message '"
						+ message.Message + "'. Do you accept this offer?",
				"Group Invitation", JOptionPane.YES_NO_OPTION);
		try {
			_Client.Self.GroupInviteRespond(message.FromAgentID, message.IMSessionID, result == JOptionPane.OK_OPTION);
		} catch (Exception ex) {
			logger.error(GridClient.Log("Exception when trying to respond to group invitation", _Client), ex);
		}
	}

	/**
	 * Triggered when online status changes are reported
	 */
	private class FriendNotificationCallback implements Callback<FriendNotificationCallbackArgs> {
		@Override
		public boolean callback(final FriendNotificationCallbackArgs e) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					AbstractChannel channel = getChannel(null);
					String message = e.getOnline() ? " is online" : " is offline";
					for (UUID uuid : e.getAgentID()) {
						FriendInfo info = _Client.Friends.getFriendList().get(uuid);
						try {
							channel.receiveMessage(null, uuid, info.getName(), message,
									AbstractChannel.STYLE_INFORMATIONAL);
							highlightChannel(channel, true);
						} catch (BadLocationException ex) {
							logger.error(GridClient.Log("Failed to print alert message", _Client), ex);
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
	private class FriendshipResponseCallback implements Callback<FriendshipResponseCallbackArgs> {
		@Override
		public boolean callback(FriendshipResponseCallbackArgs e) {
			String verb = e.getAccepted() ? "accepted" : "declined";
			final String title = "Friendship " + verb;
			final String message = e.getName() + " has " + verb + " your friendship offer.";

			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					/* Show dialog informing about the decision of the other */
					JOptionPane.showMessageDialog(_Main.getJFrame(), message, title, JOptionPane.PLAIN_MESSAGE);
				}
			});
			return false;
		}
	}

	/**
	 * Triggered when a user has removed us from their friends list
	 */
	private class FriendshipTerminatedCallback implements Callback<FriendshipTerminatedCallbackArgs> {
		@Override
		public boolean callback(final FriendshipTerminatedCallbackArgs e) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					/* Inform user about the friendship termination */
					JOptionPane.showMessageDialog(_Main.getJFrame(), e.getName() + " terminated the friendship.",
							"Friendship Termination", JOptionPane.PLAIN_MESSAGE);
				}
			});
			return false;
		}
	}
}
