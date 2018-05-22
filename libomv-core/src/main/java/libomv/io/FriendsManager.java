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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import libomv.inventory.InventoryException;
import libomv.inventory.InventoryFolder.FolderType;
import libomv.model.Simulator;
import libomv.model.agent.InstantMessageCallbackArgs;
import libomv.model.agent.InstantMessageDialog;
import libomv.model.agent.InstantMessageOnline;
import libomv.model.friend.FriendFoundReplyCallbackArgs;
import libomv.model.friend.FriendInfo;
import libomv.model.friend.FriendListChangedCallbackArgs;
import libomv.model.friend.FriendNotificationCallbackArgs;
import libomv.model.friend.FriendRights;
import libomv.model.friend.FriendRightsCallbackArgs;
import libomv.model.friend.FriendshipOfferedCallbackArgs;
import libomv.model.friend.FriendshipResponseCallbackArgs;
import libomv.model.friend.FriendshipTerminatedCallbackArgs;
import libomv.model.login.BuddyListEntry;
import libomv.model.login.LoginProgressCallbackArgs;
import libomv.model.login.LoginStatus;
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
public class FriendsManager implements PacketCallback {
	private static final Logger logger = Logger.getLogger(FriendsManager.class);

	public final CallbackHandler<FriendNotificationCallbackArgs> onFriendNotification = new CallbackHandler<>();

	public final CallbackHandler<FriendListChangedCallbackArgs> onFriendListChanged = new CallbackHandler<>();

	public final CallbackHandler<FriendRightsCallbackArgs> onFriendRights = new CallbackHandler<>();

	public CallbackHandler<FriendFoundReplyCallbackArgs> onFriendFoundReply = new CallbackHandler<>();

	public CallbackHandler<FriendshipOfferedCallbackArgs> onFriendshipOffered = new CallbackHandler<>();

	public CallbackHandler<FriendshipResponseCallbackArgs> onFriendshipResponse = new CallbackHandler<>();

	public CallbackHandler<FriendshipTerminatedCallbackArgs> onFriendshipTerminated = new CallbackHandler<>();

	private GridClient client;

	/**
	 * A dictionary of key/value pairs containing known friends of this avatar.
	 *
	 * The Key is the {@link UUID} of the friend, the value is a {@link FriendInfo}
	 * object that contains detailed information including permissions you have and
	 * have given to the friend
	 */
	private HashList<UUID, FriendInfo> friendList = new HashList<>();
	/**
	 * A Dictionary of key/value pairs containing current pending friendship offers.
	 *
	 * The key is the {@link UUID} of the avatar making the request, the value is
	 * the {@link UUID} of the request which is used to accept or decline the
	 * friendship offer
	 */
	private Map<UUID, UUID> friendRequests = new HashMap<>();

	/**
	 * Internal constructor
	 *
	 * @param client
	 *            A reference to the ClientManager Object
	 */
	public FriendsManager(GridClient client) {
		this.client = client;

		this.client.agent.onInstantMessage.add(new Self_OnInstantMessage());

		this.client.login.registerLoginProgressCallback(new Network_OnConnect(), new String[] { "buddy-list" }, false);

		this.client.network.registerCallback(PacketType.OnlineNotification, this);
		this.client.network.registerCallback(PacketType.OfflineNotification, this);
		this.client.network.registerCallback(PacketType.ChangeUserRights, this);
		this.client.network.registerCallback(PacketType.TerminateFriendship, this);
		this.client.network.registerCallback(PacketType.FindAgent, this);
		this.client.network.registerCallback(PacketType.UUIDNameReply, this);
	}

	public HashList<UUID, FriendInfo> getFriendList() {
		return friendList;
	}

	private void addFriend(FriendInfo info) {
		synchronized (friendList) {
			if (!friendList.containsKey(info.getID())) {
				friendList.put(info.getID(), info);

				onFriendListChanged.dispatch(new FriendListChangedCallbackArgs(info, true));
			}
		}
	}

	private FriendInfo removeFriend(UUID uuid) {
		synchronized (friendList) {
			if (friendList.containsKey(uuid)) {
				FriendInfo info = friendList.get(uuid);
				onFriendListChanged.dispatch(new FriendListChangedCallbackArgs(info, false));
				return friendList.remove(uuid);
			}
		}
		return null;
	}

	public int getFriendIndex(UUID uuid) {
		synchronized (friendList) {
			return friendList.getKeyPosition(uuid);
		}
	}

	public FriendInfo getFriend(int index) {
		synchronized (friendList) {
			return friendList.get(index);
		}
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case OnlineNotification:
		case OfflineNotification:
			handleFriendNotification(packet, simulator);
			break;
		case ChangeUserRights:
			handleChangeUserRights(packet, simulator);
			break;
		case TerminateFriendship:
			handleTerminateFriendship(packet, simulator);
			break;
		case FindAgent:
			handleFindAgentReply(packet, simulator);
			break;
		case UUIDNameReply:
			handleUUIDNameReply(packet, simulator);
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
	public final void acceptFriendship(UUID fromAgentID, UUID imSessionID) throws Exception {
		if (client.inventory == null)
			throw new InventoryException(
					"Inventory not instantiated. Need to lookup CallingCard folder in order to accept a friendship request.");

		UUID callingCardFolder = client.inventory.findFolderForType(FolderType.CallingCard).itemID;

		AcceptFriendshipPacket request = new AcceptFriendshipPacket();
		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();
		request.TransactionID = imSessionID;
		request.FolderID = new UUID[1];
		request.FolderID[0] = callingCardFolder;

		client.network.sendPacket(request);

		addFriend(new FriendInfo(fromAgentID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline));

		synchronized (friendRequests) {
			friendRequests.remove(fromAgentID);
		}

		client.avatars.requestAvatarName(fromAgentID, null);
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
	public final void declineFriendship(UUID fromAgentID, UUID imSessionID) throws Exception {
		DeclineFriendshipPacket request = new DeclineFriendshipPacket();
		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();
		request.TransactionID = imSessionID;
		client.network.sendPacket(request);

		synchronized (friendRequests) {
			friendRequests.remove(fromAgentID);
		}
	}

	/**
	 * Overload: Offer friendship to an avatar.
	 *
	 * @param agentID
	 *            System ID of the avatar you are offering friendship to
	 * @throws Exception
	 */
	public final void offerFriendship(UUID agentID) throws Exception {
		offerFriendship(agentID, "Do you want to be my friend?");
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
	public final void offerFriendship(UUID agentID, String message) throws Exception {
		if (client.inventory == null)
			throw new InventoryException(
					"Inventory not instantiated. Need to lookup CallingCard folder in order to offer friendship.");

		UUID folderID = client.inventory.findFolderForType(FolderType.CallingCard).itemID;
		client.agent.instantMessage(client.agent.getName(), agentID, message, folderID,
				InstantMessageDialog.FriendshipOffered, InstantMessageOnline.Online);
	}

	/**
	 * Terminate a friendship with an avatar
	 *
	 * @param agentID
	 *            System ID of the avatar you are terminating the friendship with
	 * @throws Exception
	 */
	public final void terminateFriendship(UUID agentID) throws Exception {
		FriendInfo friend = removeFriend(agentID);
		if (friend != null) {
			TerminateFriendshipPacket request = new TerminateFriendshipPacket();
			request.AgentData.AgentID = client.agent.getAgentID();
			request.AgentData.SessionID = client.agent.getSessionID();
			request.OtherID = agentID;

			client.network.sendPacket(request);
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
	private void handleTerminateFriendship(Packet packet, Simulator simulator) {
		TerminateFriendshipPacket itsOver = (TerminateFriendshipPacket) packet;
		FriendInfo friend = removeFriend(itsOver.OtherID);

		onFriendshipTerminated.dispatch(
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
	public final void grantRights(FriendInfo info) throws Exception {
		grantRights(info.getID(), info.theirRights);
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
	public final void grantRights(UUID friendID, byte rights) throws Exception {
		GrantUserRightsPacket request = new GrantUserRightsPacket();
		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();
		request.Rights = new GrantUserRightsPacket.RightsBlock[1];
		request.Rights[0] = request.new RightsBlock();
		request.Rights[0].AgentRelated = friendID;
		request.Rights[0].RelatedRights = rights;

		client.network.sendPacket(request);
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
	public final void mapFriend(UUID friendID) throws Exception {
		FindAgentPacket stalk = new FindAgentPacket();
		stalk.AgentBlock.Hunter = client.agent.getAgentID();
		stalk.AgentBlock.Prey = friendID;
		stalk.AgentBlock.SpaceIP = 0; // Will be filled in by the simulator
		stalk.LocationBlock = new FindAgentPacket.LocationBlockBlock[1];
		stalk.LocationBlock[0] = stalk.new LocationBlockBlock();
		stalk.LocationBlock[0].GlobalX = 0.0; // Filled in by the simulator
		stalk.LocationBlock[0].GlobalY = 0.0;

		client.network.sendPacket(stalk);
	}

	/**
	 * Use to track a friends movement on the grid
	 *
	 * @param friendID
	 *            Friends Key
	 * @throws Exception
	 */
	public final void trackFriend(UUID friendID) throws Exception {
		TrackAgentPacket stalk = new TrackAgentPacket();
		stalk.AgentData.AgentID = client.agent.getAgentID();
		stalk.AgentData.SessionID = client.agent.getSessionID();
		stalk.PreyID = friendID;

		client.network.sendPacket(stalk);
	}

	/**
	 * Ask for a notification of friend's online status
	 *
	 * @param friendID
	 *            Friend's UUID
	 * @throws Exception
	 */
	public final void requestOnlineNotification(UUID friendID) throws Exception {
		GenericMessagePacket gmp = new GenericMessagePacket();
		gmp.AgentData.AgentID = client.agent.getAgentID();
		gmp.AgentData.SessionID = client.agent.getSessionID();
		gmp.AgentData.TransactionID = UUID.ZERO;

		gmp.MethodData.setMethod(Helpers.stringToBytes("requestonlinenotification"));
		gmp.MethodData.Invoice = UUID.ZERO;
		gmp.ParamList = new GenericMessagePacket.ParamListBlock[1];
		gmp.ParamList[0] = gmp.new ParamListBlock();
		gmp.ParamList[0].setParameter(Helpers.stringToBytes(friendID.toString()));

		client.network.sendPacket(gmp);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The received packet data
	 * @param simulator
	 *            The simulator for which the even the packet data is
	 */
	private void handleFriendNotification(Packet packet, Simulator simulator) throws Exception {
		List<UUID> requestids = new ArrayList<>();
		FriendInfo friend = null;
		UUID[] agentIDs = null;
		boolean doNotify = false;

		if (packet.getType() == PacketType.OnlineNotification) {
			OnlineNotificationPacket notification = (OnlineNotificationPacket) packet;
			agentIDs = notification.AgentID;
			for (UUID agentID : notification.AgentID) {
				synchronized (friendList) {
					if (!friendList.containsKey(agentID)) {
						// Mark this friend for a name request
						requestids.add(agentID);
						friend = new FriendInfo(agentID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline);
						addFriend(friend);
					} else {
						friend = friendList.get(agentID);
					}
				}
				doNotify |= !friend.getIsOnline();
				friend.setIsOnline(true);
			}
		} else if (packet.getType() == PacketType.OfflineNotification) {
			OfflineNotificationPacket notification = (OfflineNotificationPacket) packet;
			agentIDs = notification.AgentID;
			for (UUID agentID : notification.AgentID) {
				synchronized (friendList) {
					if (!friendList.containsKey(agentID)) {
						// Mark this friend for a name request
						requestids.add(agentID);

						friend = new FriendInfo(agentID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline);
						addFriend(friend);
					} else {
						friend = friendList.get(agentID);
					}
				}
				doNotify |= friend.getIsOnline();
				friend.setIsOnline(false);
			}
		}

		// Only notify when there was a change in online status
		if (doNotify)
			onFriendNotification.dispatch(
					new FriendNotificationCallbackArgs(agentIDs, packet.getType() == PacketType.OnlineNotification));

		if (requestids.size() > 0) {
			client.avatars.requestAvatarNames(requestids, null);
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
	private void handleChangeUserRights(Packet packet, Simulator simulator) throws Exception {
		if (packet.getType() == PacketType.ChangeUserRights) {
			FriendInfo friend;
			ChangeUserRightsPacket rights = (ChangeUserRightsPacket) packet;

			synchronized (friendList) {
				for (ChangeUserRightsPacket.RightsBlock block : rights.Rights) {
					if (friendList.containsKey(block.AgentRelated)) {
						friend = friendList.get(block.AgentRelated);
						friend.theirRights = FriendRights.setValue(block.RelatedRights);

						onFriendRights.dispatch(new FriendRightsCallbackArgs(friend));
					} else if (block.AgentRelated.equals(client.agent.getAgentID())) {
						if (friendList.containsKey(rights.AgentID)) {
							friend = friendList.get(rights.AgentID);
							friend.myRights = FriendRights.setValue(block.RelatedRights);

							onFriendRights.dispatch(new FriendRightsCallbackArgs(friend));
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
	private void handleFindAgentReply(Packet packet, Simulator simulator) throws Exception {
		if (onFriendFoundReply.count() > 0) {
			FindAgentPacket reply = (FindAgentPacket) packet;

			UUID prey = reply.AgentBlock.Prey;
			float values[] = new float[2];
			long regionHandle = Helpers.globalPosToRegionHandle((float) reply.LocationBlock[0].GlobalX,
					(float) reply.LocationBlock[0].GlobalY, values);

			onFriendFoundReply.dispatch(
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
	private void handleUUIDNameReply(Packet packet, Simulator simulator) throws Exception {
		UUIDNameReplyPacket reply = (UUIDNameReplyPacket) packet;

		for (UUIDNameReplyPacket.UUIDNameBlockBlock block : reply.UUIDNameBlock) {
			synchronized (friendList) {
				if (friendList.containsKey(block.ID)) {
					friendList.get(block.ID).setName(Helpers.bytesToString(block.getFirstName()) + " "
							+ Helpers.bytesToString(block.getLastName()));
				}
			}
		}
	}

	private class Self_OnInstantMessage implements Callback<InstantMessageCallbackArgs> {
		@Override
		public boolean callback(InstantMessageCallbackArgs e) {
			UUID friendID = e.getIM().fromAgentID;
			String name = e.getIM().fromAgentName;

			switch (e.getIM().dialog) {
			case FriendshipOffered:
				UUID sessionID = e.getIM().imSessionID;
				synchronized (friendRequests) {
					friendRequests.put(friendID, sessionID);
				}
				onFriendshipOffered.dispatch(new FriendshipOfferedCallbackArgs(friendID, name, sessionID));
				break;
			case FriendshipAccepted:
				FriendInfo friend = new FriendInfo(friendID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline);
				friend.setName(name);
				addFriend(friend);
				onFriendshipResponse.dispatch(new FriendshipResponseCallbackArgs(friendID, name, true));
				try {
					requestOnlineNotification(friendID);
				} catch (Exception ex) {
					logger.error(GridClient.Log("Error requesting online notification", client), ex);
				}
				break;
			case FriendshipDeclined:
				onFriendshipResponse.dispatch(new FriendshipResponseCallbackArgs(friendID, name, false));
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
				if (e.getReply().buddyList != null) {
					synchronized (friendList) {
						for (BuddyListEntry buddy : e.getReply().buddyList) {
							UUID bubid = UUID.parse(buddy.buddyID);
							if (!friendList.containsKey(bubid)) {
								addFriend(new FriendInfo(bubid, buddy.buddyRightsGiven, buddy.buddyRightsHas));
							}
						}
					}
				}
				List<UUID> request = new ArrayList<>();

				synchronized (friendList) {
					if (friendList.size() > 0) {
						for (FriendInfo kvp : friendList.values()) {
							if (kvp.getName() == null || kvp.getName().isEmpty()) {
								request.add(kvp.getID());
							}
						}
						onFriendListChanged.dispatch(new FriendListChangedCallbackArgs(null, true));
					}
				}

				if (request.size() > 0) {
					try {
						client.avatars.requestAvatarNames(request, null);
					} catch (Exception e1) {
					}
				}
			}
			return false;
		}
	}
}
