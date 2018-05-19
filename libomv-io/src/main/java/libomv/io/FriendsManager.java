/**
 * Copyright (c) 2006-2014, openmetaverse.org
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
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
package libomv.io;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import libomv.inventory.InventoryException;
import libomv.inventory.InventoryFolder.FolderType;
import libomv.model.Agent.InstantMessageCallbackArgs;
import libomv.model.Agent.InstantMessageDialog;
import libomv.model.Agent.InstantMessageOnline;
import libomv.model.Login.BuddyListEntry;
import libomv.model.Login.LoginProgressCallbackArgs;
import libomv.model.Login.LoginStatus;
import libomv.model.Simulator;
import libomv.packets.AcceptFriendshipPacket;
import libomv.packets.ChangeUserRightsPacket;
import libomv.packets.DeclineFriendshipPacket;
import libomv.packets.FindAgentPacket;
import libomv.packets.GenericMessagePacket;
import libomv.packets.GrantUserRightsPacket;
import libomv.packets.OfflineNotificationPacket;
import libomv.packets.OnlineNotificationPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.TerminateFriendshipPacket;
import libomv.packets.TrackAgentPacket;
import libomv.packets.UUIDNameReplyPacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.HashList;
import libomv.utils.Helpers;

/**
 * This class is used to add and remove avatars from your friends list and to
 * manage their permission.
 */
public class FriendsManager implements PacketCallback, libomv.model.Friend {
	private static final Logger logger = Logger.getLogger(FriendsManager.class);

	public final CallbackHandler<FriendNotificationCallbackArgs> OnFriendNotification = new CallbackHandler<FriendNotificationCallbackArgs>();

	public final CallbackHandler<FriendListChangedCallbackArgs> OnFriendListChanged = new CallbackHandler<FriendListChangedCallbackArgs>();

	public final CallbackHandler<FriendRightsCallbackArgs> OnFriendRights = new CallbackHandler<FriendRightsCallbackArgs>();

	public CallbackHandler<FriendFoundReplyCallbackArgs> OnFriendFoundReply = new CallbackHandler<FriendFoundReplyCallbackArgs>();

	public CallbackHandler<FriendshipOfferedCallbackArgs> OnFriendshipOffered = new CallbackHandler<FriendshipOfferedCallbackArgs>();

	public CallbackHandler<FriendshipResponseCallbackArgs> OnFriendshipResponse = new CallbackHandler<FriendshipResponseCallbackArgs>();

	public CallbackHandler<FriendshipTerminatedCallbackArgs> OnFriendshipTerminated = new CallbackHandler<FriendshipTerminatedCallbackArgs>();
	// #endregion callback handlers

	private GridClient _Client;

	/**
	 * A dictionary of key/value pairs containing known friends of this avatar.
	 *
	 * The Key is the {@link UUID} of the friend, the value is a {@link FriendInfo}
	 * object that contains detailed information including permissions you have and
	 * have given to the friend
	 */
	private HashList<UUID, FriendInfo> _FriendList = new HashList<UUID, FriendInfo>();

	public HashList<UUID, FriendInfo> getFriendList() {
		return _FriendList;
	}

	private void addFriend(FriendInfo info) {
		synchronized (_FriendList) {
			if (!_FriendList.containsKey(info.getID())) {
				_FriendList.put(info.getID(), info);

				OnFriendListChanged.dispatch(new FriendListChangedCallbackArgs(info, true));
			}
		}
	}

	private FriendInfo removeFriend(UUID uuid) {
		synchronized (_FriendList) {
			if (_FriendList.containsKey(uuid)) {
				FriendInfo info = _FriendList.get(uuid);
				OnFriendListChanged.dispatch(new FriendListChangedCallbackArgs(info, false));
				return _FriendList.remove(uuid);
			}
		}
		return null;
	}

	public int getFriendIndex(UUID uuid) {
		synchronized (_FriendList) {
			return _FriendList.getKeyPosition(uuid);
		}
	}

	public FriendInfo getFriend(int index) {
		synchronized (_FriendList) {
			return _FriendList.get(index);
		}
	}

	/**
	 * A Dictionary of key/value pairs containing current pending friendship offers.
	 *
	 * The key is the {@link UUID} of the avatar making the request, the value is
	 * the {@link UUID} of the request which is used to accept or decline the
	 * friendship offer
	 */
	private HashMap<UUID, UUID> _FriendRequests = new HashMap<UUID, UUID>();

	/**
	 * Internal constructor
	 *
	 * @param client
	 *            A reference to the ClientManager Object
	 */
	public FriendsManager(GridClient client) {
		_Client = client;

		_Client.Self.OnInstantMessage.add(new Self_OnInstantMessage());

		_Client.Login.RegisterLoginProgressCallback(new Network_OnConnect(), new String[] { "buddy-list" }, false);

		_Client.Network.RegisterCallback(PacketType.OnlineNotification, this);
		_Client.Network.RegisterCallback(PacketType.OfflineNotification, this);
		_Client.Network.RegisterCallback(PacketType.ChangeUserRights, this);
		_Client.Network.RegisterCallback(PacketType.TerminateFriendship, this);
		_Client.Network.RegisterCallback(PacketType.FindAgent, this);
		_Client.Network.RegisterCallback(PacketType.UUIDNameReply, this);

	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case OnlineNotification:
		case OfflineNotification:
			HandleFriendNotification(packet, simulator);
			break;
		case ChangeUserRights:
			HandleChangeUserRights(packet, simulator);
			break;
		case TerminateFriendship:
			HandleTerminateFriendship(packet, simulator);
			break;
		case FindAgent:
			HandleFindAgentReply(packet, simulator);
			break;
		case UUIDNameReply:
			HandleUUIDNameReply(packet, simulator);
			break;
		default:
			break;
		}
	}

	/**
	 * Accept a friendship request
	 *
	 * @param fromAgentID
	 *            agentID of avatatar to form friendship with
	 * @param imSessionID
	 *            imSessionID of the friendship request message
	 * @throws Exception
	 * @throws InventoryException
	 */
	public final void AcceptFriendship(UUID fromAgentID, UUID imSessionID) throws Exception, InventoryException {
		if (_Client.Inventory == null)
			throw new InventoryException(
					"Inventory not instantiated. Need to lookup CallingCard folder in order to accept a friendship request.");

		UUID callingCardFolder = _Client.Inventory.FindFolderForType(FolderType.CallingCard).itemID;

		AcceptFriendshipPacket request = new AcceptFriendshipPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.TransactionID = imSessionID;
		request.FolderID = new UUID[1];
		request.FolderID[0] = callingCardFolder;

		_Client.Network.sendPacket(request);

		addFriend(new FriendInfo(fromAgentID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline));

		synchronized (_FriendRequests) {
			_FriendRequests.remove(fromAgentID);
		}

		_Client.Avatars.RequestAvatarName(fromAgentID, null);
	}

	/**
	 * Decline a friendship request
	 *
	 * @param fromAgentID
	 *            {@link UUID} of friend
	 * @param imSessionID
	 *            imSessionID of the friendship request message
	 * @throws Exception
	 */
	public final void DeclineFriendship(UUID fromAgentID, UUID imSessionID) throws Exception {
		DeclineFriendshipPacket request = new DeclineFriendshipPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.TransactionID = imSessionID;
		_Client.Network.sendPacket(request);

		synchronized (_FriendRequests) {
			_FriendRequests.remove(fromAgentID);
		}
	}

	/**
	 * Overload: Offer friendship to an avatar.
	 *
	 * @param agentID
	 *            System ID of the avatar you are offering friendship to
	 * @throws Exception
	 */
	public final void OfferFriendship(UUID agentID) throws Exception {
		OfferFriendship(agentID, "Do you want to be my friend?");
	}

	/**
	 * Offer friendship to an avatar.
	 *
	 * @param agentID
	 *            System ID of the avatar you are offering friendship to
	 * @param message
	 *            A message to send with the request
	 * @throws Exception
	 */
	public final void OfferFriendship(UUID agentID, String message) throws Exception {
		if (_Client.Inventory == null)
			throw new InventoryException(
					"Inventory not instantiated. Need to lookup CallingCard folder in order to offer friendship.");

		UUID folderID = _Client.Inventory.FindFolderForType(FolderType.CallingCard).itemID;
		_Client.Self.InstantMessage(_Client.Self.getName(), agentID, message, folderID,
				InstantMessageDialog.FriendshipOffered, InstantMessageOnline.Online);
	}

	/**
	 * Terminate a friendship with an avatar
	 *
	 * @param agentID
	 *            System ID of the avatar you are terminating the friendship with
	 * @throws Exception
	 */
	public final void TerminateFriendship(UUID agentID) throws Exception {
		FriendInfo friend = removeFriend(agentID);
		if (friend != null) {
			TerminateFriendshipPacket request = new TerminateFriendshipPacket();
			request.AgentData.AgentID = _Client.Self.getAgentID();
			request.AgentData.SessionID = _Client.Self.getSessionID();
			request.OtherID = agentID;

			_Client.Network.sendPacket(request);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private void HandleTerminateFriendship(Packet packet, Simulator simulator) {
		TerminateFriendshipPacket itsOver = (TerminateFriendshipPacket) packet;
		FriendInfo friend = removeFriend(itsOver.OtherID);

		OnFriendshipTerminated.dispatch(
				new FriendshipTerminatedCallbackArgs(itsOver.OtherID, friend != null ? friend.getName() : null));
	}

	/**
	 * Change the rights of a friend avatar.
	 *
	 * @param info
	 *            the {@link FriendInfo} of the friend
	 * @throws Exception
	 *
	 *             This method will implicitly set the rights to those contained in
	 *             the info parameter.
	 */
	public final void GrantRights(FriendInfo info) throws Exception {
		GrantRights(info.getID(), info.theirRights);
	}

	/**
	 * Change the rights of a friend avatar.
	 *
	 * @param friendID
	 *            the {@link UUID} of the friend
	 * @param rights
	 *            the new rights to give the friend
	 * @throws Exception
	 *
	 *             This method will implicitly set the rights to those passed in the
	 *             rights parameter.
	 */
	public final void GrantRights(UUID friendID, byte rights) throws Exception {
		GrantUserRightsPacket request = new GrantUserRightsPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.Rights = new GrantUserRightsPacket.RightsBlock[1];
		request.Rights[0] = request.new RightsBlock();
		request.Rights[0].AgentRelated = friendID;
		request.Rights[0].RelatedRights = rights;

		_Client.Network.sendPacket(request);
	}

	/**
	 * Use to map a friends location on the grid.
	 *
	 * @param friendID
	 *            Friends UUID to find
	 * @throws Exception
	 *
	 *             {@link E:OnFriendFound}
	 */
	public final void MapFriend(UUID friendID) throws Exception {
		FindAgentPacket stalk = new FindAgentPacket();
		stalk.AgentBlock.Hunter = _Client.Self.getAgentID();
		stalk.AgentBlock.Prey = friendID;
		stalk.AgentBlock.SpaceIP = 0; // Will be filled in by the simulator
		stalk.LocationBlock = new FindAgentPacket.LocationBlockBlock[1];
		stalk.LocationBlock[0] = stalk.new LocationBlockBlock();
		stalk.LocationBlock[0].GlobalX = 0.0; // Filled in by the simulator
		stalk.LocationBlock[0].GlobalY = 0.0;

		_Client.Network.sendPacket(stalk);
	}

	/**
	 * Use to track a friends movement on the grid
	 *
	 * @param friendID
	 *            Friends Key
	 * @throws Exception
	 */
	public final void TrackFriend(UUID friendID) throws Exception {
		TrackAgentPacket stalk = new TrackAgentPacket();
		stalk.AgentData.AgentID = _Client.Self.getAgentID();
		stalk.AgentData.SessionID = _Client.Self.getSessionID();
		stalk.PreyID = friendID;

		_Client.Network.sendPacket(stalk);
	}

	/**
	 * Ask for a notification of friend's online status
	 *
	 * @param friendID
	 *            Friend's UUID
	 * @throws Exception
	 */
	public final void RequestOnlineNotification(UUID friendID) throws Exception {
		GenericMessagePacket gmp = new GenericMessagePacket();
		gmp.AgentData.AgentID = _Client.Self.getAgentID();
		gmp.AgentData.SessionID = _Client.Self.getSessionID();
		gmp.AgentData.TransactionID = UUID.Zero;

		gmp.MethodData.setMethod(Helpers.StringToBytes("requestonlinenotification"));
		gmp.MethodData.Invoice = UUID.Zero;
		gmp.ParamList = new GenericMessagePacket.ParamListBlock[1];
		gmp.ParamList[0] = gmp.new ParamListBlock();
		gmp.ParamList[0].setParameter(Helpers.StringToBytes(friendID.toString()));

		_Client.Network.sendPacket(gmp);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The received packet data
	 * @param simulator
	 *            The simulator for which the even the packet data is
	 */
	private void HandleFriendNotification(Packet packet, Simulator simulator) throws Exception {
		ArrayList<UUID> requestids = new ArrayList<UUID>();
		FriendInfo friend = null;
		UUID[] agentIDs = null;
		boolean doNotify = false;

		if (packet.getType() == PacketType.OnlineNotification) {
			OnlineNotificationPacket notification = (OnlineNotificationPacket) packet;
			agentIDs = notification.AgentID;
			for (UUID agentID : notification.AgentID) {
				synchronized (_FriendList) {
					if (!_FriendList.containsKey(agentID)) {
						// Mark this friend for a name request
						requestids.add(agentID);
						friend = new FriendInfo(agentID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline);
						addFriend(friend);
					} else {
						friend = _FriendList.get(agentID);
					}
				}
				doNotify |= !friend.getIsOnline();
				friend.setIsOnline(true);
			}
		} else if (packet.getType() == PacketType.OfflineNotification) {
			OfflineNotificationPacket notification = (OfflineNotificationPacket) packet;
			agentIDs = notification.AgentID;
			for (UUID agentID : notification.AgentID) {
				synchronized (_FriendList) {
					if (!_FriendList.containsKey(agentID)) {
						// Mark this friend for a name request
						requestids.add(agentID);

						friend = new FriendInfo(agentID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline);
						addFriend(friend);
					} else {
						friend = _FriendList.get(agentID);
					}
				}
				doNotify |= friend.getIsOnline();
				friend.setIsOnline(false);
			}
		}

		// Only notify when there was a change in online status
		if (doNotify)
			OnFriendNotification.dispatch(
					new FriendNotificationCallbackArgs(agentIDs, packet.getType() == PacketType.OnlineNotification));

		if (requestids.size() > 0) {
			_Client.Avatars.RequestAvatarNames(requestids, null);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The received packet data
	 * @param simulator
	 *            The simulator for which the even the packet data is
	 */
	private void HandleChangeUserRights(Packet packet, Simulator simulator) throws Exception {
		if (packet.getType() == PacketType.ChangeUserRights) {
			FriendInfo friend;
			ChangeUserRightsPacket rights = (ChangeUserRightsPacket) packet;

			synchronized (_FriendList) {
				for (ChangeUserRightsPacket.RightsBlock block : rights.Rights) {
					if (_FriendList.containsKey(block.AgentRelated)) {
						friend = _FriendList.get(block.AgentRelated);
						friend.theirRights = FriendRights.setValue(block.RelatedRights);

						OnFriendRights.dispatch(new FriendRightsCallbackArgs(friend));
					} else if (block.AgentRelated.equals(_Client.Self.getAgentID())) {
						if (_FriendList.containsKey(rights.AgentID)) {
							friend = _FriendList.get(rights.AgentID);
							friend.myRights = FriendRights.setValue(block.RelatedRights);

							OnFriendRights.dispatch(new FriendRightsCallbackArgs(friend));
						}
					}
				}
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The received packet data
	 * @param simulator
	 *            The simulator for which the packet data is
	 */
	private void HandleFindAgentReply(Packet packet, Simulator simulator) throws Exception {
		if (OnFriendFoundReply.count() > 0) {
			FindAgentPacket reply = (FindAgentPacket) packet;

			UUID prey = reply.AgentBlock.Prey;
			float values[] = new float[2];
			long regionHandle = Helpers.GlobalPosToRegionHandle((float) reply.LocationBlock[0].GlobalX,
					(float) reply.LocationBlock[0].GlobalY, values);

			OnFriendFoundReply.dispatch(
					new FriendFoundReplyCallbackArgs(prey, regionHandle, new Vector3(values[0], values[1], 0f)));
		}
	}

	/**
	 * Process an incoming UUIDNameReply Packet and insert Full Names into the
	 * FriendList Dictionary
	 *
	 * @param packet
	 *            Incoming Packet to process</param>
	 * @param simulator
	 *            Unused
	 */
	private void HandleUUIDNameReply(Packet packet, Simulator simulator) throws Exception {
		UUIDNameReplyPacket reply = (UUIDNameReplyPacket) packet;

		for (UUIDNameReplyPacket.UUIDNameBlockBlock block : reply.UUIDNameBlock) {
			synchronized (_FriendList) {
				if (_FriendList.containsKey(block.ID)) {
					_FriendList.get(block.ID).setName(Helpers.BytesToString(block.getFirstName()) + " "
							+ Helpers.BytesToString(block.getLastName()));
				}
			}
		}
	}

	private class Self_OnInstantMessage implements Callback<InstantMessageCallbackArgs> {
		@Override
		public boolean callback(InstantMessageCallbackArgs e) {
			UUID friendID = e.getIM().FromAgentID;
			String name = e.getIM().FromAgentName;

			switch (e.getIM().Dialog) {
			case FriendshipOffered:
				UUID sessionID = e.getIM().IMSessionID;
				synchronized (_FriendRequests) {
					_FriendRequests.put(friendID, sessionID);
				}
				OnFriendshipOffered.dispatch(new FriendshipOfferedCallbackArgs(friendID, name, sessionID));
				break;
			case FriendshipAccepted:
				FriendInfo friend = new FriendInfo(friendID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline);
				friend.setName(name);
				addFriend(friend);
				OnFriendshipResponse.dispatch(new FriendshipResponseCallbackArgs(friendID, name, true));
				try {
					RequestOnlineNotification(friendID);
				} catch (Exception ex) {
					logger.error(GridClient.Log("Error requesting online notification", _Client), ex);
				}
				break;
			case FriendshipDeclined:
				OnFriendshipResponse.dispatch(new FriendshipResponseCallbackArgs(friendID, name, false));
				break;
			default:
				break;
			}
			return false;
		}
	}

	/**
	 * Populate FriendList {@link InternalDictionary} with data from the login reply
	 */
	private class Network_OnConnect implements Callback<LoginProgressCallbackArgs> {
		@Override
		public boolean callback(LoginProgressCallbackArgs e) {
			if (e.getStatus() == LoginStatus.Success) {
				if (e.getReply().BuddyList != null) {
					synchronized (_FriendList) {
						for (BuddyListEntry buddy : e.getReply().BuddyList) {
							UUID bubid = UUID.parse(buddy.buddy_id);
							if (!_FriendList.containsKey(bubid)) {
								addFriend(new FriendInfo(bubid, buddy.buddy_rights_given, buddy.buddy_rights_has));
							}
						}
					}
				}
				ArrayList<UUID> request = new ArrayList<UUID>();

				synchronized (_FriendList) {
					if (_FriendList.size() > 0) {
						for (FriendInfo kvp : _FriendList.values()) {
							if (kvp.getName() == null || kvp.getName().isEmpty()) {
								request.add(kvp.getID());
							}
						}
						OnFriendListChanged.dispatch(new FriendListChangedCallbackArgs(null, true));
					}
				}

				if (request.size() > 0) {
					try {
						_Client.Avatars.RequestAvatarNames(request, null);
					} catch (Exception e1) {
					}
				}
			}
			return false;
		}
	}
}
