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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsMessage.AgentDropGroupMessage;
import libomv.capabilities.CapsMessage.AgentGroupDataUpdateMessage;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.IMessage;
import libomv.io.capabilities.CapsCallback;
import libomv.io.capabilities.CapsClient;
import libomv.model.Group;
import libomv.model.Simulator;
import libomv.model.agent.InstantMessageCallbackArgs;
import libomv.model.agent.InstantMessageDialog;
import libomv.model.agent.InstantMessageOnline;
import libomv.model.asset.AssetType;
import libomv.model.group.BannedAgentsCallbackArgs;
import libomv.model.group.CurrentGroupsCallbackArgs;
import libomv.model.group.GroupAccountDetails;
import libomv.model.group.GroupAccountSummary;
import libomv.model.group.GroupAccountSummaryReplyCallbackArgs;
import libomv.model.group.GroupAccountTransactions;
import libomv.model.group.GroupAccountTransactions.TransactionEntry;
import libomv.model.group.GroupBanAction;
import libomv.model.group.GroupCreatedReplyCallbackArgs;
import libomv.model.group.GroupDroppedCallbackArgs;
import libomv.model.group.GroupInvitationCallbackArgs;
import libomv.model.group.GroupMember;
import libomv.model.group.GroupMembersReplyCallbackArgs;
import libomv.model.group.GroupNamesCallbackArgs;
import libomv.model.group.GroupNotice;
import libomv.model.group.GroupNoticesListEntry;
import libomv.model.group.GroupNoticesListReplyCallbackArgs;
import libomv.model.group.GroupOperationCallbackArgs;
import libomv.model.group.GroupProfileCallbackArgs;
import libomv.model.group.GroupProposal;
import libomv.model.group.GroupProposalItem;
import libomv.model.group.GroupRole;
import libomv.model.group.GroupRoleUpdate;
import libomv.model.group.GroupRolesDataReplyCallbackArgs;
import libomv.model.group.GroupRolesMembersReplyCallbackArgs;
import libomv.model.group.GroupTitle;
import libomv.model.group.GroupTitlesReplyCallbackArgs;
import libomv.packets.ActivateGroupPacket;
import libomv.packets.AgentDataUpdateRequestPacket;
import libomv.packets.AgentDropGroupPacket;
import libomv.packets.AgentGroupDataUpdatePacket;
import libomv.packets.CreateGroupReplyPacket;
import libomv.packets.CreateGroupRequestPacket;
import libomv.packets.EjectGroupMemberReplyPacket;
import libomv.packets.EjectGroupMemberRequestPacket;
import libomv.packets.GroupAccountDetailsReplyPacket;
import libomv.packets.GroupAccountDetailsRequestPacket;
import libomv.packets.GroupAccountSummaryReplyPacket;
import libomv.packets.GroupAccountSummaryRequestPacket;
import libomv.packets.GroupAccountTransactionsReplyPacket;
import libomv.packets.GroupActiveProposalItemReplyPacket;
import libomv.packets.GroupMembersReplyPacket;
import libomv.packets.GroupMembersRequestPacket;
import libomv.packets.GroupNoticeRequestPacket;
import libomv.packets.GroupNoticesListReplyPacket;
import libomv.packets.GroupNoticesListRequestPacket;
import libomv.packets.GroupProfileReplyPacket;
import libomv.packets.GroupProfileRequestPacket;
import libomv.packets.GroupRoleChangesPacket;
import libomv.packets.GroupRoleDataReplyPacket;
import libomv.packets.GroupRoleDataRequestPacket;
import libomv.packets.GroupRoleMembersReplyPacket;
import libomv.packets.GroupRoleMembersRequestPacket;
import libomv.packets.GroupRoleUpdatePacket;
import libomv.packets.GroupTitleUpdatePacket;
import libomv.packets.GroupTitlesReplyPacket;
import libomv.packets.GroupTitlesRequestPacket;
import libomv.packets.GroupVoteHistoryItemReplyPacket;
import libomv.packets.InviteGroupRequestPacket;
import libomv.packets.JoinGroupReplyPacket;
import libomv.packets.JoinGroupRequestPacket;
import libomv.packets.LeaveGroupReplyPacket;
import libomv.packets.LeaveGroupRequestPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.SetGroupAcceptNoticesPacket;
import libomv.packets.SetGroupContributionPacket;
import libomv.packets.StartGroupProposalPacket;
import libomv.packets.UUIDGroupNameReplyPacket;
import libomv.packets.UUIDGroupNameRequestPacket;
import libomv.packets.UpdateGroupInfoPacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.HashList;
import libomv.utils.HashMapInt;
import libomv.utils.Helpers;

// Handles all network traffic related to reading and writing group
// information
public class GroupManager implements PacketCallback, CapsCallback {
	private static final Logger logger = Logger.getLogger(GroupManager.class);

	private GridClient _Client;

	// Currently-active group members requests
	private ArrayList<UUID> GroupMembersRequests;
	// Currently-active group roles requests
	private ArrayList<UUID> GroupRolesRequests;
	// Currently-active group role-member requests
	private ArrayList<UUID> GroupRolesMembersRequests;
	// Dictionary keeping group members while request is in progress
	private HashMap<UUID, HashMap<UUID, GroupMember>> TempGroupMembers;
	// Dictionary keeping member/role mapping while request is in progress
	private HashMap<UUID, ArrayList<Entry<UUID, UUID>>> TempGroupRolesMembers;
	// Dictionary keeping GroupRole information while request is in progress
	private HashMap<UUID, HashMap<UUID, GroupRole>> TempGroupRoles;
	// Caches groups this avatar is member of
	public HashList<UUID, Group> GroupList;
	// Caches group names of all groups known to us
	public HashMap<UUID, String> GroupNames;

	public CallbackHandler<CurrentGroupsCallbackArgs> OnCurrentGroups = new CallbackHandler<CurrentGroupsCallbackArgs>();

	public CallbackHandler<GroupNamesCallbackArgs> OnGroupNamesReply = new CallbackHandler<GroupNamesCallbackArgs>();

	public CallbackHandler<GroupProfileCallbackArgs> OnGroupProfile = new CallbackHandler<GroupProfileCallbackArgs>();

	public CallbackHandler<GroupMembersReplyCallbackArgs> OnGroupMembersReply = new CallbackHandler<GroupMembersReplyCallbackArgs>();

	public CallbackHandler<GroupRolesDataReplyCallbackArgs> OnGroupRoleDataReply = new CallbackHandler<GroupRolesDataReplyCallbackArgs>();

	public CallbackHandler<GroupRolesMembersReplyCallbackArgs> OnGroupRoleMembers = new CallbackHandler<GroupRolesMembersReplyCallbackArgs>();

	public CallbackHandler<GroupTitlesReplyCallbackArgs> OnGroupTitles = new CallbackHandler<GroupTitlesReplyCallbackArgs>();

	public CallbackHandler<GroupAccountSummaryReplyCallbackArgs> OnGroupAccountSummaryReply = new CallbackHandler<GroupAccountSummaryReplyCallbackArgs>();

	public CallbackHandler<GroupCreatedReplyCallbackArgs> OnGroupCreatedReply = new CallbackHandler<GroupCreatedReplyCallbackArgs>();

	public CallbackHandler<GroupOperationCallbackArgs> OnGroupJoinedReply = new CallbackHandler<GroupOperationCallbackArgs>();

	public CallbackHandler<GroupOperationCallbackArgs> OnGroupLeaveReply = new CallbackHandler<GroupOperationCallbackArgs>();

	public CallbackHandler<GroupDroppedCallbackArgs> OnGroupDropped = new CallbackHandler<GroupDroppedCallbackArgs>();

	public CallbackHandler<GroupOperationCallbackArgs> OnGroupMemberEjected = new CallbackHandler<GroupOperationCallbackArgs>();

	public CallbackHandler<GroupNoticesListReplyCallbackArgs> OnGroupNoticesListReply = new CallbackHandler<GroupNoticesListReplyCallbackArgs>();

	public CallbackHandler<GroupInvitationCallbackArgs> OnGroupInvitation = new CallbackHandler<GroupInvitationCallbackArgs>();

	public CallbackHandler<BannedAgentsCallbackArgs> OnBannedAgents = new CallbackHandler<BannedAgentsCallbackArgs>();

	public HashMap<UUID, Callback<GroupAccountDetails>> OnGroupAccountDetailsCallbacks = new HashMap<UUID, Callback<GroupAccountDetails>>();

	public HashMap<UUID, Callback<GroupAccountTransactions>> OnGroupAccountTransactionsCallbacks = new HashMap<UUID, Callback<GroupAccountTransactions>>();

	private class InstantMessageCallback implements Callback<InstantMessageCallbackArgs> {
		@Override
		public boolean callback(InstantMessageCallbackArgs e) {
			if (OnGroupInvitation.count() > 0 && e.getIM().Dialog == InstantMessageDialog.GroupInvitation) {
				byte[] bucket = e.getIM().BinaryBucket;
				int fee = -1;
				UUID roleID = null;
				if (bucket.length == 20) {
					fee = Helpers.BytesToInt32B(bucket);
					roleID = new UUID(bucket, 4);
				}

				GroupInvitationCallbackArgs args = new GroupInvitationCallbackArgs(e.getSimulator(),
						e.getIM().IMSessionID, e.getIM().FromAgentID, roleID, e.getIM().FromAgentName,
						e.getIM().Message, fee);
				OnGroupInvitation.dispatch(args);
			}
			return false;
		}
	}

	public GroupManager(GridClient client) {
		_Client = client;

		TempGroupMembers = new HashMap<UUID, HashMap<UUID, GroupMember>>();
		GroupMembersRequests = new ArrayList<UUID>();
		TempGroupRoles = new HashMap<UUID, HashMap<UUID, GroupRole>>();
		GroupRolesRequests = new ArrayList<UUID>();
		TempGroupRolesMembers = new HashMap<UUID, ArrayList<Entry<UUID, UUID>>>();
		GroupRolesMembersRequests = new ArrayList<UUID>();
		GroupList = new HashList<UUID, Group>();
		GroupNames = new HashMap<UUID, String>();

		_Client.Self.OnInstantMessage.add(new InstantMessageCallback());

		_Client.Network.RegisterCallback(CapsEventType.AgentGroupDataUpdate, this);
		// deprecated in simulator v1.27
		_Client.Network.RegisterCallback(PacketType.AgentGroupDataUpdate, this);

		_Client.Network.RegisterCallback(CapsEventType.AgentDropGroup, this);
		// deprecated in simulator v1.27
		_Client.Network.RegisterCallback(PacketType.AgentDropGroup, this);

		_Client.Network.RegisterCallback(PacketType.GroupTitlesReply, this);
		_Client.Network.RegisterCallback(PacketType.GroupProfileReply, this);
		_Client.Network.RegisterCallback(PacketType.GroupMembersReply, this);
		_Client.Network.RegisterCallback(PacketType.GroupRoleDataReply, this);
		_Client.Network.RegisterCallback(PacketType.GroupRoleMembersReply, this);
		_Client.Network.RegisterCallback(PacketType.GroupActiveProposalItemReply, this);
		_Client.Network.RegisterCallback(PacketType.GroupVoteHistoryItemReply, this);
		_Client.Network.RegisterCallback(PacketType.GroupAccountSummaryReply, this);
		_Client.Network.RegisterCallback(PacketType.GroupAccountDetailsReply, this);
		_Client.Network.RegisterCallback(PacketType.GroupAccountTransactionsReply, this);
		_Client.Network.RegisterCallback(PacketType.CreateGroupReply, this);
		_Client.Network.RegisterCallback(PacketType.JoinGroupReply, this);
		_Client.Network.RegisterCallback(PacketType.LeaveGroupReply, this);
		_Client.Network.RegisterCallback(PacketType.UUIDGroupNameReply, this);
		_Client.Network.RegisterCallback(PacketType.EjectGroupMemberReply, this);
		_Client.Network.RegisterCallback(PacketType.GroupNoticesListReply, this);
	}

	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) throws Exception {
		switch (message.getType()) {
		case AgentGroupDataUpdate:
			HandleAgentGroupDataUpdate(message, simulator);
		case AgentDropGroup:
			HandleAgentDropGroup(message, simulator);
		default:
			break;
		}
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case AgentGroupDataUpdate:
			HandleAgentGroupDataUpdate(packet, simulator);
			break;
		case AgentDropGroup:
			HandleAgentDropGroup(packet, simulator);
			break;
		case GroupTitlesReply:
			HandleGroupTitlesReply(packet, simulator);
			break;
		case GroupProfileReply:
			HandleGroupProfileReply(packet, simulator);
			break;
		case GroupMembersReply:
			HandleGroupMembers(packet, simulator);
			break;
		case GroupRoleDataReply:
			HandleGroupRoleDataReply(packet, simulator);
			break;
		case GroupRoleMembersReply:
			HandleGroupRoleMembersReply(packet, simulator);
			break;
		case GroupActiveProposalItemReply:
			HandleGroupActiveProposalItem(packet, simulator);
			break;
		case GroupVoteHistoryItemReply:
			HandleGroupVoteHistoryItem(packet, simulator);
			break;
		case GroupAccountSummaryReply:
			HandleGroupAccountSummaryReply(packet, simulator);
			break;
		case GroupAccountDetailsReply:
			HandleGroupAccountDetails(packet, simulator);
			break;
		case GroupAccountTransactionsReply:
			HandleGroupAccountTransactions(packet, simulator);
			break;
		case CreateGroupReply:
			HandleCreateGroupReply(packet, simulator);
			break;
		case JoinGroupReply:
			HandleJoinGroupReply(packet, simulator);
			break;
		case LeaveGroupReply:
			HandleLeaveGroupReply(packet, simulator);
			break;
		case EjectGroupMemberReply:
			HandleEjectGroupMemberReply(packet, simulator);
			break;
		case GroupNoticesListReply:
			HandleGroupNoticesListReply(packet, simulator);
			break;
		case UUIDGroupNameReply:
			HandleUUIDGroupNameReply(packet, simulator);
			break;
		default:
			break;
		}
	}

	/**
	 * Request a current list of groups the avatar is a member of.
	 *
	 * CAPS Event Queue must be running for this to work since the results come
	 * across CAPS.
	 *
	 * @throws Exception
	 */
	public final void RequestCurrentGroups() throws Exception {
		AgentDataUpdateRequestPacket request = new AgentDataUpdateRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		_Client.Network.sendPacket(request);
	}

	/**
	 * Lookup name of group based on groupID
	 *
	 * @param groupID
	 *            groupID of group to lookup name for.
	 * @throws Exception
	 */
	public final void RequestGroupName(UUID groupID) throws Exception {
		// if we already have this in the cache, return from cache instead of
		// making a request
		synchronized (GroupNames) {
			if (GroupNames.containsKey(groupID)) {
				HashMap<UUID, String> groupNames = new HashMap<UUID, String>();
				groupNames.put(groupID, GroupNames.get(groupID));

				OnGroupNamesReply.dispatch(new GroupNamesCallbackArgs(groupNames));
				return;
			}
		}

		UUIDGroupNameRequestPacket req = new UUIDGroupNameRequestPacket();
		req.ID = new UUID[1];
		req.ID[0] = groupID;
		_Client.Network.sendPacket(req);
	}

	/**
	 * Request lookup of multiple group names
	 *
	 * @param groupIDs
	 *            List of group IDs to request.
	 * @throws Exception
	 */
	public final void RequestGroupNames(ArrayList<UUID> groupIDs) throws Exception {
		HashMap<UUID, String> groupNames = new HashMap<UUID, String>();
		ArrayList<UUID> tempIDs = new ArrayList<UUID>();
		synchronized (GroupNames) {
			for (UUID groupID : groupIDs) {
				if (GroupNames.containsKey(groupID)) {
					groupNames.put(groupID, GroupNames.get(groupID));
				} else {
					tempIDs.add(groupID);
				}
			}
		}

		if (tempIDs.size() > 0) {
			UUIDGroupNameRequestPacket req = new UUIDGroupNameRequestPacket();
			req.ID = new UUID[tempIDs.size()];
			for (int i = 0; i < tempIDs.size(); i++) {
				req.ID[i] = tempIDs.get(i);
			}
			_Client.Network.sendPacket(req);
		}

		// fire handler from cache
		if (groupNames.size() > 0)
			OnGroupNamesReply.dispatch(new GroupNamesCallbackArgs(groupNames));
	}

	/**
	 * Lookup group profile data such as name, enrollment, founder, logo, etc
	 * Subscribe to <code>OnGroupProfile</code> event to receive the results.
	 *
	 * @param group
	 * @param group
	 *            group ID (UUID)
	 * @throws Exception
	 */
	public final void RequestGroupProfile(UUID group) throws Exception {
		GroupProfileRequestPacket request = new GroupProfileRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.GroupID = group;

		_Client.Network.sendPacket(request);
	}

	private class GroupMembersHandlerCaps implements FutureCallback<OSD> {
		private UUID requestID;

		public GroupMembersHandlerCaps(UUID requestID) {
			this.requestID = requestID;
		}

		@Override
		public void completed(OSD result) {
			try {
				OSDMap res = (OSDMap) result;
				// int memberCount = res.get("member_count").AsInteger();
				OSDArray titlesOSD = (OSDArray) res.get("titles");
				String[] titles = new String[titlesOSD.size()];
				for (int i = 0; i < titlesOSD.size(); i++) {
					titles[i] = titlesOSD.get(i).AsString();
				}
				UUID groupID = res.get("group_id").AsUUID();
				long defaultPowers = ((OSDMap) res.get("defaults")).get("default_powers").AsULong();
				OSDMap membersOSD = (OSDMap) res.get("members");
				HashMap<UUID, GroupMember> groupMembers = new HashMap<UUID, GroupMember>(membersOSD.size());
				for (String memberID : membersOSD.keySet()) {
					OSDMap member = (OSDMap) membersOSD.get(memberID);

					GroupMember groupMember = new GroupMember(UUID.parse(memberID));
					groupMember.Contribution = member.get("donated_square_meters").AsInteger();
					groupMember.IsOwner = "Y" == member.get("owner").AsString();
					groupMember.OnlineStatus = member.get("last_login").AsString();
					groupMember.Powers = defaultPowers;
					if (member.containsKey("powers")) {
						groupMember.Powers = member.get("powers").AsULong();
					}
					groupMember.Title = titles[member.get("title").AsInteger()];
					groupMembers.put(groupMember.ID, groupMember);
				}
				OnGroupMembersReply.dispatch(new GroupMembersReplyCallbackArgs(requestID, groupID, groupMembers));
			} catch (Exception ex) {
				logger.error(GridClient.Log("Failed to decode result of GroupMemberData capability: ", _Client), ex);
			}
		}

		@Override
		public void failed(Exception ex) {
			logger.error(GridClient.Log("Failed to request GroupMemberData capability: ", _Client), ex);
		}

		@Override
		public void cancelled() {
			logger.error(GridClient.Log("GroupMemberData capability request canceled!", _Client));
		}
	}

	/**
	 * Request a list of group members. Subscribe to <code>OnGroupMembers</code>
	 * event to receive the results.
	 *
	 * @param group
	 *            group ID (UUID)
	 * @return UUID of the request, use to index into cache
	 * @throws Exception
	 */
	public final UUID RequestGroupMembers(UUID group) throws Exception {
		UUID requestID = new UUID();
		URI url = _Client.Network.getCapabilityURI("GroupMemberData");
		if (url != null) {
			CapsClient req = new CapsClient(_Client, "GroupMemberData");
			OSDMap requestData = new OSDMap(1);
			requestData.put("group_id", OSD.FromUUID(group));
			req.executeHttpPost(url, requestData, OSDFormat.Xml, new GroupMembersHandlerCaps(requestID),
					_Client.Settings.CAPS_TIMEOUT * 4);
		} else {
			synchronized (GroupMembersRequests) {
				GroupMembersRequests.add(requestID);
			}

			GroupMembersRequestPacket request = new GroupMembersRequestPacket();

			request.AgentData.AgentID = _Client.Self.getAgentID();
			request.AgentData.SessionID = _Client.Self.getSessionID();
			request.GroupData.GroupID = group;
			request.GroupData.RequestID = requestID;

			_Client.Network.sendPacket(request);
		}
		return requestID;
	}

	/**
	 * Request group roles Subscribe to <code>OnGroupRoles</code> event to receive
	 * the results.
	 *
	 * @param group
	 *            group ID (UUID)
	 * @return UUID of the request, use to index into cache
	 * @throws Exception
	 */
	public final UUID RequestGroupRoles(UUID group) throws Exception {
		UUID requestID = new UUID();
		synchronized (GroupRolesRequests) {
			GroupRolesRequests.add(requestID);
		}

		GroupRoleDataRequestPacket request = new GroupRoleDataRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.GroupData.GroupID = group;
		request.GroupData.RequestID = requestID;

		_Client.Network.sendPacket(request);
		return requestID;
	}

	/**
	 * Request members (members,role) role mapping for a group. Subscribe to
	 * <code>OnGroupRolesMembers</code> event to receive the results.
	 *
	 * @param group
	 *            group ID (UUID)
	 * @return UUID of the request, use to index into cache
	 * @throws Exception
	 */
	public final UUID RequestGroupRolesMembers(UUID group) throws Exception {
		UUID requestID = new UUID();
		synchronized (GroupRolesRequests) {
			GroupRolesMembersRequests.add(requestID);
		}

		GroupRoleMembersRequestPacket request = new GroupRoleMembersRequestPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.GroupData.GroupID = group;
		request.GroupData.RequestID = requestID;
		_Client.Network.sendPacket(request);
		return requestID;
	}

	/**
	 * Request a groups Titles Subscribe to <code>OnGroupTitles</code> event to
	 * receive the results.
	 *
	 * @param group
	 *            group ID (UUID)
	 * @return UUID of the request, use to index into cache
	 * @throws Exception
	 */
	public final UUID RequestGroupTitles(UUID group) throws Exception {
		UUID requestID = new UUID();

		GroupTitlesRequestPacket request = new GroupTitlesRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.AgentData.GroupID = group;
		request.AgentData.RequestID = requestID;

		_Client.Network.sendPacket(request);
		return requestID;
	}

	/**
	 * Begin to get the group account summary Subscribe to the
	 * <code>OnGroupAccountSummary</code> event to receive the results.
	 *
	 * @param group
	 *            group ID (UUID)
	 * @param intervalDays
	 *            How long of an interval
	 * @param currentInterval
	 *            Which interval (0 for current, 1 for last)
	 * @throws Exception
	 */
	public final void RequestGroupAccountSummary(UUID group, int intervalDays, int currentInterval) throws Exception {
		GroupAccountSummaryRequestPacket p = new GroupAccountSummaryRequestPacket();
		p.AgentData.AgentID = _Client.Self.getAgentID();
		p.AgentData.SessionID = _Client.Self.getSessionID();
		p.AgentData.GroupID = group;
		// TODO: Store request ID to identify the callback
		p.MoneyData.RequestID = new UUID();
		p.MoneyData.CurrentInterval = currentInterval;
		p.MoneyData.IntervalDays = intervalDays;
		_Client.Network.sendPacket(p);
	}

	/**
	 * Begin to get the group account details Subscribe to the
	 * <code>OnGroupAccountDetails</code> event to receive the results.
	 *
	 * @param group
	 *            group ID (UUID)
	 * @param intervalDays
	 *            How long of an interval
	 * @param currentInterval
	 *            Which interval (0 for current, 1 for last)
	 * @throws Exception
	 */
	public final void RequestGroupAccountDetails(UUID group, int intervalDays, int currentInterval) throws Exception {
		GroupAccountDetailsRequestPacket p = new GroupAccountDetailsRequestPacket();
		p.AgentData.AgentID = _Client.Self.getAgentID();
		p.AgentData.SessionID = _Client.Self.getSessionID();
		p.AgentData.GroupID = group;
		// TODO: Store request ID to identify the callback
		p.MoneyData.RequestID = new UUID();
		p.MoneyData.CurrentInterval = currentInterval;
		p.MoneyData.IntervalDays = intervalDays;
		_Client.Network.sendPacket(p);
	}

	/**
	 * Invites a user to a group
	 *
	 * @param group
	 *            The group to invite to
	 * @param roles
	 *            A list of roles to invite a person to
	 * @param personkey
	 *            Key of person to invite
	 * @throws Exception
	 */
	public final void Invite(UUID group, ArrayList<UUID> roles, UUID personkey) throws Exception {
		InviteGroupRequestPacket igp = new InviteGroupRequestPacket();

		igp.AgentData = igp.new AgentDataBlock();
		igp.AgentData.AgentID = _Client.Self.getAgentID();
		igp.AgentData.SessionID = _Client.Self.getSessionID();

		igp.GroupID = group;

		igp.InviteData = new InviteGroupRequestPacket.InviteDataBlock[roles.size()];

		for (int i = 0; i < roles.size(); i++) {
			igp.InviteData[i] = igp.new InviteDataBlock();
			igp.InviteData[i].InviteeID = personkey;
			igp.InviteData[i].RoleID = roles.get(i);
		}

		_Client.Network.sendPacket(igp);
	}

	/**
	 * Set a group as the current active group
	 *
	 * @param id
	 *            group ID (UUID)
	 * @throws Exception
	 */
	public final void ActivateGroup(UUID id) throws Exception {
		ActivateGroupPacket activate = new ActivateGroupPacket();
		activate.AgentData.AgentID = _Client.Self.getAgentID();
		activate.AgentData.SessionID = _Client.Self.getSessionID();
		activate.AgentData.GroupID = id;

		_Client.Network.sendPacket(activate);
	}

	/**
	 * Change the role that determines your active title
	 *
	 * @param group
	 *            Group ID to use
	 * @param role
	 *            Role ID to change to
	 * @throws Exception
	 */
	public final void ActivateTitle(UUID group, UUID role) throws Exception {
		GroupTitleUpdatePacket gtu = new GroupTitleUpdatePacket();
		gtu.AgentData.AgentID = _Client.Self.getAgentID();
		gtu.AgentData.SessionID = _Client.Self.getSessionID();
		gtu.AgentData.TitleRoleID = role;
		gtu.AgentData.GroupID = group;

		_Client.Network.sendPacket(gtu);
	}

	/**
	 * Set this avatar's tier contribution
	 *
	 * @param group
	 *            Group ID to change tier in
	 * @param contribution
	 *            amount of tier to donate
	 * @throws Exception
	 */
	public final void SetGroupContribution(UUID group, int contribution) throws Exception {
		SetGroupContributionPacket sgp = new SetGroupContributionPacket();
		sgp.AgentData.AgentID = _Client.Self.getAgentID();
		sgp.AgentData.SessionID = _Client.Self.getSessionID();
		sgp.Data.GroupID = group;
		sgp.Data.Contribution = contribution;

		_Client.Network.sendPacket(sgp);
	}

	/**
	 * Save wheather agent wants to accept group notices and list this group in
	 * their profile
	 *
	 * @param groupID
	 *            Group <see cref="UUID"/>
	 * @param acceptNotices
	 *            Accept notices from this group
	 * @param listInProfile
	 *            List this group in the profile
	 * @throws Exception
	 */
	public final void SetGroupAcceptNotices(UUID groupID, boolean acceptNotices, boolean listInProfile)
			throws Exception {
		SetGroupAcceptNoticesPacket p = new SetGroupAcceptNoticesPacket();
		p.AgentData.AgentID = _Client.Self.getAgentID();
		p.AgentData.SessionID = _Client.Self.getSessionID();
		p.Data.GroupID = groupID;
		p.Data.AcceptNotices = acceptNotices;
		p.ListInProfile = listInProfile;

		_Client.Network.sendPacket(p);
	}

	/**
	 * Request to join a group
	 *
	 * @param id
	 *            group ID (UUID) to join.
	 * @throws Exception
	 */
	public final void RequestJoinGroup(UUID id) throws Exception {
		JoinGroupRequestPacket join = new JoinGroupRequestPacket();
		join.AgentData.AgentID = _Client.Self.getAgentID();
		join.AgentData.SessionID = _Client.Self.getSessionID();

		join.GroupID = id;

		_Client.Network.sendPacket(join);
	}

	/**
	 * Request to create a new group. If the group is successfully created, L$100
	 * will automatically be deducted
	 *
	 * Subscribe to <code>OnGroupCreated</code> event to receive confirmation.
	 *
	 * @param group
	 *            Group struct containing the new group info
	 * @throws Exception
	 */
	public final void RequestCreateGroup(Group group) throws Exception {
		CreateGroupRequestPacket cgrp = new CreateGroupRequestPacket();
		cgrp.AgentData = cgrp.new AgentDataBlock();
		cgrp.AgentData.AgentID = _Client.Self.getAgentID();
		cgrp.AgentData.SessionID = _Client.Self.getSessionID();

		cgrp.GroupData = cgrp.new GroupDataBlock();
		cgrp.GroupData.AllowPublish = group.AllowPublish;
		cgrp.GroupData.setCharter(Helpers.StringToBytes(group.Charter));
		cgrp.GroupData.InsigniaID = group.InsigniaID;
		cgrp.GroupData.MaturePublish = group.MaturePublish;
		cgrp.GroupData.MembershipFee = group.MembershipFee;
		cgrp.GroupData.setName(Helpers.StringToBytes(group.getName()));
		cgrp.GroupData.OpenEnrollment = group.OpenEnrollment;
		cgrp.GroupData.ShowInList = group.ShowInList;

		_Client.Network.sendPacket(cgrp);
	}

	/**
	 * Update a group's profile and other information
	 *
	 * @param id
	 *            Groups ID (UUID) to update.
	 * @param group
	 *            Group struct to update.
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public final void UpdateGroup(UUID id, Group group) throws Exception {
		UpdateGroupInfoPacket cgrp = new UpdateGroupInfoPacket();
		cgrp.AgentData = cgrp.new AgentDataBlock();
		cgrp.AgentData.AgentID = _Client.Self.getAgentID();
		cgrp.AgentData.SessionID = _Client.Self.getSessionID();

		cgrp.GroupData = cgrp.new GroupDataBlock();
		cgrp.GroupData.GroupID = id;
		cgrp.GroupData.AllowPublish = group.AllowPublish;
		cgrp.GroupData.setCharter(Helpers.StringToBytes(group.Charter));
		cgrp.GroupData.InsigniaID = group.InsigniaID;
		cgrp.GroupData.MaturePublish = group.MaturePublish;
		cgrp.GroupData.MembershipFee = group.MembershipFee;
		cgrp.GroupData.OpenEnrollment = group.OpenEnrollment;
		cgrp.GroupData.ShowInList = group.ShowInList;

		_Client.Network.sendPacket(cgrp);
	}

	/**
	 * Eject a user from a group
	 *
	 * @param group
	 *            Group ID to eject the user from
	 * @param member
	 *            Avatar's key to eject
	 * @throws Exception
	 */
	public final void EjectUser(UUID group, UUID member) throws Exception {
		EjectGroupMemberRequestPacket eject = new EjectGroupMemberRequestPacket();
		eject.AgentData = eject.new AgentDataBlock();
		eject.AgentData.AgentID = _Client.Self.getAgentID();
		eject.AgentData.SessionID = _Client.Self.getSessionID();

		eject.GroupID = group;

		eject.EjecteeID = new UUID[1];
		eject.EjecteeID[0] = member;

		_Client.Network.sendPacket(eject);
	}

	/**
	 * Update role information
	 *
	 * @param role
	 *            Modified role to be updated
	 * @throws Exception
	 */
	public final void UpdateRole(GroupRole role) throws Exception {
		GroupRoleUpdatePacket gru = new GroupRoleUpdatePacket();
		gru.AgentData.AgentID = _Client.Self.getAgentID();
		gru.AgentData.SessionID = _Client.Self.getSessionID();
		gru.AgentData.GroupID = role.GroupID;
		gru.RoleData = new GroupRoleUpdatePacket.RoleDataBlock[1];
		gru.RoleData[0] = gru.new RoleDataBlock();
		gru.RoleData[0].setName(Helpers.StringToBytes(role.Name));
		gru.RoleData[0].setDescription(Helpers.StringToBytes(role.Description));
		gru.RoleData[0].Powers = role.Powers;
		gru.RoleData[0].RoleID = role.ID;
		gru.RoleData[0].setTitle(Helpers.StringToBytes(role.Title));
		gru.RoleData[0].UpdateType = GroupRoleUpdate.UpdateAll.getValue();
		_Client.Network.sendPacket(gru);
	}

	/**
	 * Create a new group role
	 *
	 * @param group
	 *            Group ID to update
	 * @param role
	 *            Role to create
	 * @throws Exception
	 */
	public final void CreateRole(UUID group, GroupRole role) throws Exception {
		GroupRoleUpdatePacket gru = new GroupRoleUpdatePacket();
		gru.AgentData.AgentID = _Client.Self.getAgentID();
		gru.AgentData.SessionID = _Client.Self.getSessionID();
		gru.AgentData.GroupID = group;
		gru.RoleData = new GroupRoleUpdatePacket.RoleDataBlock[1];
		gru.RoleData[0] = gru.new RoleDataBlock();
		gru.RoleData[0].RoleID = new UUID();
		gru.RoleData[0].setName(Helpers.StringToBytes(role.Name));
		gru.RoleData[0].setDescription(Helpers.StringToBytes(role.Description));
		gru.RoleData[0].Powers = role.Powers;
		gru.RoleData[0].setTitle(Helpers.StringToBytes(role.Title));
		gru.RoleData[0].UpdateType = GroupRoleUpdate.Create.getValue();
		_Client.Network.sendPacket(gru);
	}

	/**
	 * Delete a group role
	 *
	 * @param group
	 *            Group ID to update
	 * @param roleID
	 *            Role to delete
	 * @throws Exception
	 */
	public final void DeleteRole(UUID group, UUID roleID) throws Exception {
		GroupRoleUpdatePacket gru = new GroupRoleUpdatePacket();
		gru.AgentData.AgentID = _Client.Self.getAgentID();
		gru.AgentData.SessionID = _Client.Self.getSessionID();
		gru.AgentData.GroupID = group;
		gru.RoleData = new GroupRoleUpdatePacket.RoleDataBlock[1];
		gru.RoleData[0] = gru.new RoleDataBlock();
		gru.RoleData[0].RoleID = roleID;
		gru.RoleData[0].setName(Helpers.StringToBytes(Helpers.EmptyString));
		gru.RoleData[0].setDescription(Helpers.StringToBytes(Helpers.EmptyString));
		gru.RoleData[0].Powers = 0;
		gru.RoleData[0].setTitle(Helpers.StringToBytes(Helpers.EmptyString));
		gru.RoleData[0].UpdateType = GroupRoleUpdate.Delete.getValue();
		_Client.Network.sendPacket(gru);
	}

	/**
	 * Remove an avatar from a role
	 *
	 * @param group
	 *            Group ID to update
	 * @param role
	 *            Role ID to be removed from
	 * @param member
	 *            Avatar's Key to remove
	 * @throws Exception
	 */
	public final void RemoveFromRole(UUID group, UUID role, UUID member) throws Exception {
		GroupRoleChangesPacket grc = new GroupRoleChangesPacket();
		grc.AgentData.AgentID = _Client.Self.getAgentID();
		grc.AgentData.SessionID = _Client.Self.getSessionID();
		grc.AgentData.GroupID = group;
		grc.RoleChange = new GroupRoleChangesPacket.RoleChangeBlock[1];
		grc.RoleChange[0] = grc.new RoleChangeBlock();
		// Add to members and role
		grc.RoleChange[0].MemberID = member;
		grc.RoleChange[0].RoleID = role;
		// 1 = Remove From Role TODO: this should be in an enum
		grc.RoleChange[0].Change = 1;
		_Client.Network.sendPacket(grc);
	}

	/**
	 * Assign an avatar to a role
	 *
	 * @param group
	 *            Group ID to update
	 * @param role
	 *            Role ID to assign to
	 * @param member
	 *            Avatar's ID to assign to role
	 * @throws Exception
	 */
	public final void AddToRole(UUID group, UUID role, UUID member) throws Exception {
		GroupRoleChangesPacket grc = new GroupRoleChangesPacket();
		grc.AgentData.AgentID = _Client.Self.getAgentID();
		grc.AgentData.SessionID = _Client.Self.getSessionID();
		grc.AgentData.GroupID = group;
		grc.RoleChange = new GroupRoleChangesPacket.RoleChangeBlock[1];
		grc.RoleChange[0] = grc.new RoleChangeBlock();
		// Add to members and role
		grc.RoleChange[0].MemberID = member;
		grc.RoleChange[0].RoleID = role;
		// 0 = Add to Role TODO: this should be in an enum
		grc.RoleChange[0].Change = 0;
		_Client.Network.sendPacket(grc);
	}

	/**
	 * Request the group notices list
	 *
	 * @param group
	 *            Group ID to fetch notices for
	 * @throws Exception
	 */
	public final void RequestGroupNoticesList(UUID group) throws Exception {
		GroupNoticesListRequestPacket gnl = new GroupNoticesListRequestPacket();
		gnl.AgentData.AgentID = _Client.Self.getAgentID();
		gnl.AgentData.SessionID = _Client.Self.getSessionID();
		gnl.GroupID = group;
		_Client.Network.sendPacket(gnl);
	}

	/**
	 * Request a group notice by key
	 *
	 * @param noticeID
	 *            ID of group notice
	 * @throws Exception
	 */
	public final void RequestGroupNotice(UUID noticeID) throws Exception {
		GroupNoticeRequestPacket gnr = new GroupNoticeRequestPacket();
		gnr.AgentData.AgentID = _Client.Self.getAgentID();
		gnr.AgentData.SessionID = _Client.Self.getSessionID();
		gnr.GroupNoticeID = noticeID;
		_Client.Network.sendPacket(gnr);
	}

	/**
	 * Send out a group notice
	 *
	 * @param group
	 *            Group ID to update
	 * @param notice
	 *            <code>GroupNotice</code> structure containing notice data
	 * @throws Exception
	 */
	public final void SendGroupNotice(UUID group, GroupNotice notice) throws Exception {
		_Client.Self.InstantMessage(_Client.Self.getName(), group, notice.Subject + "|" + notice.Message, UUID.Zero,
				InstantMessageDialog.GroupNotice, InstantMessageOnline.Online, Vector3.Zero, UUID.Zero, 0,
				notice.SerializeAttachment());
	}

	/**
	 * Start a group proposal (vote)
	 *
	 * @param group
	 *            The Group ID to send proposal to
	 * @param prop
	 *            <code>GroupProposal</code> structure containing the proposal
	 * @throws Exception
	 */
	public final void StartProposal(UUID group, GroupProposal prop) throws Exception {
		StartGroupProposalPacket p = new StartGroupProposalPacket();
		p.AgentData.AgentID = _Client.Self.getAgentID();
		p.AgentData.SessionID = _Client.Self.getSessionID();
		p.ProposalData.GroupID = group;
		p.ProposalData.setProposalText(Helpers.StringToBytes(prop.ProposalText));
		p.ProposalData.Quorum = prop.Quorum;
		p.ProposalData.Majority = prop.Majority;
		p.ProposalData.Duration = prop.Duration;
		_Client.Network.sendPacket(p);
	}

	/**
	 * Request to leave a group
	 *
	 * @param groupID
	 *            The group to leave
	 * @throws Exception
	 */
	public final void LeaveGroup(UUID groupID) throws Exception {
		LeaveGroupRequestPacket p = new LeaveGroupRequestPacket();
		p.AgentData.AgentID = _Client.Self.getAgentID();
		p.AgentData.SessionID = _Client.Self.getSessionID();
		p.GroupID = groupID;

		_Client.Network.sendPacket(p);
	}

	/**
	 * Gets the URI of the cpability for handling group bans
	 *
	 * @param groupID
	 *            UUID of the group
	 * @throws URISyntaxException
	 * @returns null, if the feature is not supported, or URI of the capability
	 */
	public URI GetGroupAPIUri(UUID groupID) throws URISyntaxException {
		URI ret = _Client.Network.getCapabilityURI("GroupAPIv1");
		if (ret != null) {
			ret = new URI(String.format("%s?group_id=%s", ret.toString(), groupID.toString()));
		}
		return ret;
	}

	/**
	 * Request a list of residents banned from joining a group
	 *
	 * @param groupID
	 *            UUID of the group
	 * @throws URISyntaxException
	 * @throws IOReactorException
	 */
	public void RequestBannedAgents(UUID groupID) throws IOReactorException, URISyntaxException {
		RequestBannedAgents(groupID, null);
	}

	/**
	 * Request a list of residents banned from joining a group
	 *
	 * @param groupID
	 *            UUID of the group
	 * @param callback
	 *            Callback on request completition
	 * @throws URISyntaxException
	 * @throws IOReactorException
	 */
	public void RequestBannedAgents(final UUID groupID, final Callback<BannedAgentsCallbackArgs> callback)
			throws URISyntaxException, IOReactorException {
		class ClientCallback implements FutureCallback<OSD> {
			@Override
			public void cancelled() {
				BannedAgentsCallbackArgs ret = new BannedAgentsCallbackArgs(groupID, false, null);
				if (callback != null) {
					try {
						callback.callback(ret);
					} catch (Exception ex) {
					}
				}
				OnBannedAgents.dispatch(ret);
			}

			@Override
			public void completed(OSD result) {
				UUID gid = ((OSDMap) result).get("group_id").AsUUID();
				OSDMap banList = (OSDMap) ((OSDMap) result).get("ban_list");
				HashMap<UUID, Date> bannedAgents = new HashMap<UUID, Date>(banList.size());

				for (String id : banList.keySet()) {
					UUID uid = new UUID(id);
					bannedAgents.put(uid, ((OSDMap) banList.get(uid)).get("ban_date").AsDate());
				}
				BannedAgentsCallbackArgs ret = new BannedAgentsCallbackArgs(gid, true, bannedAgents);

				if (callback != null) {
					try {
						callback.callback(ret);
					} catch (Exception ex) {
					}
				}
				OnBannedAgents.dispatch(ret);
			}

			@Override
			public void failed(Exception ex) {
				logger.warn(
						GridClient.Log("Failed to get a list of banned group members: " + ex.getMessage(), _Client));
				BannedAgentsCallbackArgs ret = new BannedAgentsCallbackArgs(groupID, false, null);
				if (callback != null) {
					try {
						callback.callback(ret);
					} catch (Exception ex1) {
					}
				}
				OnBannedAgents.dispatch(ret);
			}
		}

		URI uri = GetGroupAPIUri(groupID);
		if (uri == null)
			return;
		CapsClient req = new CapsClient(_Client, "GroupAPIv1");
		req.executeHttpGet(uri, null, new ClientCallback(), _Client.Settings.CAPS_TIMEOUT);
	}

	/**
	 * Request that group of agents be banned or unbanned from the group
	 *
	 * @param groupID
	 *            UUID of the group
	 * @param action
	 *            Ban/Unban action<
	 * @param agents
	 *            Array of agents UUIDs to ban
	 * @throws URISyntaxException
	 * @throws IOReactorException
	 */
	public void RequestBanAction(UUID groupID, GroupBanAction action, UUID[] agents)
			throws IOReactorException, URISyntaxException {
		RequestBanAction(groupID, action, agents, null);
	}

	/**
	 * Request that group of agents be banned or unbanned from the group
	 *
	 * @param groupID
	 *            UUID of the group
	 * @param action
	 *            Ban/Unban action<
	 * @param agents
	 *            Array of agents UUIDs to ban
	 * @param callback
	 *            Callback on request completition
	 * @throws URISyntaxException
	 * @throws IOReactorException
	 */
	public void RequestBanAction(final UUID groupID, GroupBanAction action, UUID[] agents,
			final Callback<CallbackArgs> callback) throws IOReactorException, URISyntaxException {
		class ClientCallback implements FutureCallback<OSD> {
			@Override
			public void cancelled() {
				if (callback != null) {
					try {
						callback.callback(null);
					} catch (Exception ex) {
					}
				}
			}

			@Override
			public void completed(OSD result) {
				if (callback != null) {
					BannedAgentsCallbackArgs ret = new BannedAgentsCallbackArgs(groupID, true, null);
					try {
						callback.callback(ret);
					} catch (Exception ex) {
					}
				}
			}

			@Override
			public void failed(Exception ex) {
				logger.warn(GridClient.Log("Failed to ban or unban group members: " + ex.getMessage(), _Client));
				if (callback != null) {
					try {
						callback.callback(null);
					} catch (Exception ex1) {
					}
				}
			}
		}

		URI uri = GetGroupAPIUri(groupID);
		if (uri == null)
			return;

		OSDMap request = new OSDMap();
		request.put("ban_action", OSD.FromInteger(action.getValue()));
		OSDArray banIDs = new OSDArray(agents.length);
		for (UUID agent : agents) {
			banIDs.add(OSD.FromUUID(agent));
		}
		request.put("ban_ids", banIDs);
		CapsClient req = new CapsClient(_Client, "GroupAPIv1");
		req.executeHttpPost(uri, request, OSDFormat.Xml, new ClientCallback(), _Client.Settings.CAPS_TIMEOUT);
	}
	// #endregion

	// #region Packet Handlers
	private final void HandleAgentGroupDataUpdate(IMessage message, Simulator simulator) {
		HashMap<UUID, Group> currentGroups = OnCurrentGroups.count() > 0 ? new HashMap<UUID, Group>() : null;
		AgentGroupDataUpdateMessage msg = (AgentGroupDataUpdateMessage) message;

		for (int i = 0; i < msg.groupDataBlock.length; i++) {
			Group group = new Group(msg.groupDataBlock[i].groupID);
			group.InsigniaID = msg.groupDataBlock[i].groupInsigniaID;
			group.Name = msg.groupDataBlock[i].groupName;
			group.Contribution = msg.groupDataBlock[i].contribution;
			group.AcceptNotices = msg.groupDataBlock[i].acceptNotices;
			group.Powers = msg.groupDataBlock[i].groupPowers;
			group.ListInProfile = msg.newGroupDataBlock[i].listInProfile;

			if (currentGroups != null)
				currentGroups.put(group.getID(), group);

			synchronized (GroupList) {
				GroupList.put(group.getID(), group);
			}
			synchronized (GroupNames) {
				GroupNames.put(group.getID(), group.Name);
			}
		}

		if (currentGroups != null)
			OnCurrentGroups.dispatch(new CurrentGroupsCallbackArgs(currentGroups));
	}

	private final void HandleAgentGroupDataUpdate(Packet packet, Simulator simulator) throws Exception {
		HashMap<UUID, Group> currentGroups = OnCurrentGroups.count() > 0 ? new HashMap<UUID, Group>() : null;
		AgentGroupDataUpdatePacket update = (AgentGroupDataUpdatePacket) packet;

		for (AgentGroupDataUpdatePacket.GroupDataBlock block : update.GroupData) {
			Group group = new Group(block.GroupID);

			group.InsigniaID = block.GroupInsigniaID;
			group.Name = Helpers.BytesToString(block.getGroupName());
			group.Powers = block.GroupPowers;
			group.Contribution = block.Contribution;
			group.AcceptNotices = block.AcceptNotices;

			if (currentGroups != null)
				currentGroups.put(block.GroupID, group);

			synchronized (GroupList) {
				GroupList.put(group.getID(), group);
			}
			synchronized (GroupNames) {
				GroupNames.put(group.getID(), group.Name);
			}
		}

		if (currentGroups != null)
			OnCurrentGroups.dispatch(new CurrentGroupsCallbackArgs(currentGroups));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void HandleAgentDropGroup(IMessage message, Simulator simulator) {
		if (OnGroupDropped.count() > 0) {
			AgentDropGroupMessage msg = (AgentDropGroupMessage) message;
			for (int i = 0; i < msg.agentDataBlock.length; i++) {
				OnGroupDropped.dispatch(new GroupDroppedCallbackArgs(msg.agentDataBlock[i].groupID));
			}
		}
	}

	private final void HandleAgentDropGroup(Packet packet, Simulator simulator) {
		OnGroupDropped.dispatch(new GroupDroppedCallbackArgs(((AgentDropGroupPacket) packet).AgentData.GroupID));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupProfileReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (OnGroupProfile.count() > 0) {
			GroupProfileReplyPacket profile = (GroupProfileReplyPacket) packet;
			Group group = new Group(profile.GroupData.GroupID);

			group.AllowPublish = profile.GroupData.AllowPublish;
			group.Charter = Helpers.BytesToString(profile.GroupData.getCharter());
			group.FounderID = profile.GroupData.FounderID;
			group.GroupMembershipCount = profile.GroupData.GroupMembershipCount;
			group.GroupRolesCount = profile.GroupData.GroupRolesCount;
			group.InsigniaID = profile.GroupData.InsigniaID;
			group.MaturePublish = profile.GroupData.MaturePublish;
			group.MembershipFee = profile.GroupData.MembershipFee;
			group.MemberTitle = Helpers.BytesToString(profile.GroupData.getMemberTitle());
			group.Money = profile.GroupData.Money;
			group.Name = Helpers.BytesToString(profile.GroupData.getName());
			group.OpenEnrollment = profile.GroupData.OpenEnrollment;
			group.OwnerRole = profile.GroupData.OwnerRole;
			group.Powers = profile.GroupData.PowersMask;
			group.ShowInList = profile.GroupData.ShowInList;

			OnGroupProfile.dispatch(new GroupProfileCallbackArgs(group));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupNoticesListReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException {
		if (OnGroupNoticesListReply.count() > 0) {
			GroupNoticesListReplyPacket reply = (GroupNoticesListReplyPacket) packet;

			ArrayList<GroupNoticesListEntry> notices = new ArrayList<GroupNoticesListEntry>();

			for (GroupNoticesListReplyPacket.DataBlock entry : reply.Data) {
				GroupNoticesListEntry notice = new GroupNoticesListEntry();
				notice.FromName = Helpers.BytesToString(entry.getFromName());
				notice.Subject = Helpers.BytesToString(entry.getSubject());
				notice.NoticeID = entry.NoticeID;
				notice.Timestamp = entry.Timestamp;
				notice.HasAttachment = entry.HasAttachment;
				notice.AssetType = AssetType.setValue(entry.AssetType);

				notices.add(notice);
			}
			OnGroupNoticesListReply.dispatch(new GroupNoticesListReplyCallbackArgs(reply.AgentData.GroupID, notices));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupTitlesReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (OnGroupTitles.count() > 0) {
			GroupTitlesReplyPacket titles = (GroupTitlesReplyPacket) packet;
			java.util.HashMap<UUID, GroupTitle> groupTitleCache = new java.util.HashMap<UUID, GroupTitle>();

			for (GroupTitlesReplyPacket.GroupDataBlock block : titles.GroupData) {
				GroupTitle groupTitle = new GroupTitle();

				groupTitle.GroupID = titles.AgentData.GroupID;
				groupTitle.RoleID = block.RoleID;
				groupTitle.Title = Helpers.BytesToString(block.getTitle());
				groupTitle.Selected = block.Selected;

				groupTitleCache.put(block.RoleID, groupTitle);
			}
			OnGroupTitles.dispatch(new GroupTitlesReplyCallbackArgs(titles.AgentData.RequestID,
					titles.AgentData.GroupID, groupTitleCache));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupMembers(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		GroupMembersReplyPacket members = (GroupMembersReplyPacket) packet;
		HashMap<UUID, GroupMember> groupMemberCache = null;

		synchronized (GroupMembersRequests) {
			// If nothing is registered to receive this RequestID drop the data
			if (GroupMembersRequests.contains(members.GroupData.RequestID)) {
				synchronized (TempGroupMembers) {
					if (TempGroupMembers.containsKey(members.GroupData.RequestID)) {
						groupMemberCache = TempGroupMembers.get(members.GroupData.RequestID);
					} else {
						groupMemberCache = new java.util.HashMap<UUID, GroupMember>();
						TempGroupMembers.put(members.GroupData.RequestID, groupMemberCache);
					}

					for (GroupMembersReplyPacket.MemberDataBlock block : members.MemberData) {
						GroupMember groupMember = new GroupMember(block.AgentID);

						groupMember.Contribution = block.Contribution;
						groupMember.IsOwner = block.IsOwner;
						groupMember.OnlineStatus = Helpers.BytesToString(block.getOnlineStatus());
						groupMember.Powers = block.AgentPowers;
						groupMember.Title = Helpers.BytesToString(block.getTitle());

						groupMemberCache.put(block.AgentID, groupMember);
					}

					if (groupMemberCache.size() >= members.GroupData.MemberCount) {
						GroupMembersRequests.remove(members.GroupData.RequestID);
						TempGroupMembers.remove(members.GroupData.RequestID);
					}
				}
			}
		}

		if (groupMemberCache != null && groupMemberCache.size() >= members.GroupData.MemberCount) {
			OnGroupMembersReply.dispatch(new GroupMembersReplyCallbackArgs(members.GroupData.RequestID,
					members.GroupData.GroupID, groupMemberCache));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupRoleDataReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException {
		GroupRoleDataReplyPacket roles = (GroupRoleDataReplyPacket) packet;
		HashMap<UUID, GroupRole> groupRoleCache = null;

		synchronized (GroupRolesRequests) {
			// If nothing is registered to receive this RequestID drop the data
			if (GroupRolesRequests.contains(roles.GroupData.RequestID)) {
				synchronized (TempGroupRoles) {
					if (TempGroupRoles.containsKey(roles.GroupData.RequestID)) {
						groupRoleCache = TempGroupRoles.get(roles.GroupData.RequestID);
					} else {
						groupRoleCache = new java.util.HashMap<UUID, GroupRole>();
						TempGroupRoles.put(roles.GroupData.RequestID, groupRoleCache);
					}

					for (GroupRoleDataReplyPacket.RoleDataBlock block : roles.RoleData) {
						GroupRole groupRole = new GroupRole(roles.GroupData.GroupID);

						groupRole.ID = block.RoleID;
						groupRole.Description = Helpers.BytesToString(block.getDescription());
						groupRole.Name = Helpers.BytesToString(block.getName());
						groupRole.Powers = block.Powers;
						groupRole.Title = Helpers.BytesToString(block.getTitle());

						groupRoleCache.put(block.RoleID, groupRole);
					}

					if (groupRoleCache.size() >= roles.GroupData.RoleCount) {
						GroupRolesRequests.remove(roles.GroupData.RequestID);
						TempGroupRoles.remove(roles.GroupData.RequestID);
					}
				}
			}
		}

		if (groupRoleCache != null && groupRoleCache.size() >= roles.GroupData.RoleCount) {
			OnGroupRoleDataReply.dispatch(new GroupRolesDataReplyCallbackArgs(roles.GroupData.RequestID,
					roles.GroupData.GroupID, groupRoleCache));
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
	private final void HandleGroupRoleMembersReply(Packet packet, Simulator simulator) {
		GroupRoleMembersReplyPacket members = (GroupRoleMembersReplyPacket) packet;
		ArrayList<Entry<UUID, UUID>> groupRoleMemberCache = null;

		synchronized (GroupRolesMembersRequests) {
			// If nothing is registered to receive this RequestID drop the data
			if (GroupRolesMembersRequests.contains(members.AgentData.RequestID)) {
				synchronized (TempGroupRolesMembers) {
					if (TempGroupRolesMembers.containsKey(members.AgentData.RequestID)) {
						groupRoleMemberCache = TempGroupRolesMembers.get(members.AgentData.RequestID);
					} else {
						groupRoleMemberCache = new ArrayList<Entry<UUID, UUID>>();
						TempGroupRolesMembers.put(members.AgentData.RequestID, groupRoleMemberCache);
					}

					for (GroupRoleMembersReplyPacket.MemberDataBlock block : members.MemberData) {
						Entry<UUID, UUID> rolemember = new AbstractMap.SimpleEntry<UUID, UUID>(block.RoleID,
								block.MemberID);

						groupRoleMemberCache.add(rolemember);
					}

					if (groupRoleMemberCache.size() >= members.AgentData.TotalPairs) {
						GroupRolesMembersRequests.remove(members.AgentData.RequestID);
						TempGroupRolesMembers.remove(members.AgentData.RequestID);
					}
				}
			}
		}

		if (groupRoleMemberCache != null && groupRoleMemberCache.size() >= members.AgentData.TotalPairs) {
			OnGroupRoleMembers.dispatch(new GroupRolesMembersReplyCallbackArgs(members.AgentData.RequestID,
					members.AgentData.GroupID, groupRoleMemberCache));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupActiveProposalItem(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException {
		GroupActiveProposalItemReplyPacket proposal = (GroupActiveProposalItemReplyPacket) packet;

		// UUID transactionID = proposal.TransactionData.TransactionID;

		ArrayList<GroupProposalItem> array = new ArrayList<GroupProposalItem>(proposal.ProposalData.length);
		for (GroupActiveProposalItemReplyPacket.ProposalDataBlock block : proposal.ProposalData) {
			GroupProposalItem p = new GroupProposalItem();

			p.VoteID = block.VoteID;
			p.VoteInitiator = block.VoteInitiator;
			p.TerseDateID = Helpers.BytesToString(block.getTerseDateID());
			p.StartDateTime = Helpers.StringToDate(Helpers.BytesToString(block.getStartDateTime()));
			p.EndDateTime = Helpers.StringToDate(Helpers.BytesToString(block.getEndDateTime()));
			p.AlreadyVoted = block.AlreadyVoted;
			p.VoteCast = Helpers.BytesToString(block.getVoteCast());
			p.Majority = block.Majority;
			p.Quorum = block.Quorum;
			p.ProposalText = Helpers.BytesToString(block.getProposalText());

			array.add(p);
		}
		// TODO: Create transactionID hashed event queue and dispatch the event
		// there
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void HandleGroupVoteHistoryItem(Packet packet, Simulator simulator) {
		@SuppressWarnings("unused")
		GroupVoteHistoryItemReplyPacket history = (GroupVoteHistoryItemReplyPacket) packet;

		// TODO: This was broken in the official viewer when I was last trying to work
		// on it
		/*
		 * GroupProposalItem proposal = new GroupProposalItem(); proposal.Majority =
		 * history.HistoryItemData.Majority; proposal.Quorum =
		 * history.HistoryItemData.Quorum; proposal.Duration =
		 * history.TransactionData.TotalNumItems; proposal.ProposalText = ;
		 * proposal.TerseDateID = proposal.VoteID = history.HistoryItemData.VoteID;
		 * proposal.VoteInitiator = history.HistoryItemData.VoteInitiator; for (int i =
		 * 0; i < history.VoteItem.length; i++) { history.VoteItem[i].CandidateID =
		 * history.VoteItem.; history.VoteItem[i].NumVotes = ; }
		 */
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupAccountSummaryReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException {
		if (OnGroupAccountSummaryReply.count() > 0) {
			GroupAccountSummaryReplyPacket summary = (GroupAccountSummaryReplyPacket) packet;
			GroupAccountSummary account = new GroupAccountSummary();

			account.Balance = summary.MoneyData.Balance;
			account.CurrentInterval = summary.MoneyData.CurrentInterval;
			account.GroupTaxCurrent = summary.MoneyData.GroupTaxCurrent;
			account.GroupTaxEstimate = summary.MoneyData.GroupTaxEstimate;
			account.IntervalDays = summary.MoneyData.IntervalDays;
			account.LandTaxCurrent = summary.MoneyData.LandTaxCurrent;
			account.LandTaxEstimate = summary.MoneyData.LandTaxEstimate;
			account.LastTaxDate = Helpers.BytesToString(summary.MoneyData.getLastTaxDate());
			account.LightTaxCurrent = summary.MoneyData.LightTaxCurrent;
			account.LightTaxEstimate = summary.MoneyData.LightTaxEstimate;
			account.NonExemptMembers = summary.MoneyData.NonExemptMembers;
			account.ObjectTaxCurrent = summary.MoneyData.ObjectTaxCurrent;
			account.ObjectTaxEstimate = summary.MoneyData.ObjectTaxEstimate;
			account.ParcelDirFeeCurrent = summary.MoneyData.ParcelDirFeeCurrent;
			account.ParcelDirFeeEstimate = summary.MoneyData.ParcelDirFeeEstimate;
			account.StartDate = Helpers.BytesToString(summary.MoneyData.getStartDate());
			account.TaxDate = Helpers.BytesToString(summary.MoneyData.getTaxDate());
			account.TotalCredits = summary.MoneyData.TotalCredits;
			account.TotalDebits = summary.MoneyData.TotalDebits;

			OnGroupAccountSummaryReply
					.dispatch(new GroupAccountSummaryReplyCallbackArgs(summary.AgentData.GroupID, account));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleCreateGroupReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		CreateGroupReplyPacket reply = (CreateGroupReplyPacket) packet;
		String message = Helpers.BytesToString(reply.ReplyData.getMessage());

		OnGroupCreatedReply
				.dispatch(new GroupCreatedReplyCallbackArgs(reply.ReplyData.GroupID, reply.ReplyData.Success, message));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void HandleJoinGroupReply(Packet packet, Simulator simulator) {
		JoinGroupReplyPacket reply = (JoinGroupReplyPacket) packet;

		OnGroupJoinedReply.dispatch(new GroupOperationCallbackArgs(reply.GroupData.GroupID, reply.GroupData.Success));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void HandleLeaveGroupReply(Packet packet, Simulator simulator) {
		LeaveGroupReplyPacket reply = (LeaveGroupReplyPacket) packet;

		OnGroupLeaveReply.dispatch(new GroupOperationCallbackArgs(reply.GroupData.GroupID, reply.GroupData.Success));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private void HandleUUIDGroupNameReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		UUIDGroupNameReplyPacket reply = (UUIDGroupNameReplyPacket) packet;
		UUIDGroupNameReplyPacket.UUIDNameBlockBlock[] blocks = reply.UUIDNameBlock;

		HashMap<UUID, String> groupNames = new HashMap<UUID, String>();

		for (UUIDGroupNameReplyPacket.UUIDNameBlockBlock block : blocks) {
			String name = Helpers.BytesToString(block.getGroupName());
			groupNames.put(block.ID, name);
			synchronized (GroupNames) {
				GroupNames.put(block.ID, name);
			}
			synchronized (GroupList) {
				Group group = (Group) GroupList.get(block.ID);
				if (group != null) {
					group.Name = name;
				}
			}
		}
		OnGroupNamesReply.dispatch(new GroupNamesCallbackArgs(groupNames));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void HandleEjectGroupMemberReply(Packet packet, Simulator simulator) {
		EjectGroupMemberReplyPacket reply = (EjectGroupMemberReplyPacket) packet;

		// TODO: On Success remove the member from the cache(s)

		OnGroupMemberEjected.dispatch(new GroupOperationCallbackArgs(reply.GroupID, reply.Success));
	}

	// #endregion Packet Handlers

	private final void HandleGroupAccountDetails(Packet packet, Simulator simulator) throws Exception {
		GroupAccountDetailsReplyPacket details = (GroupAccountDetailsReplyPacket) packet;

		if (OnGroupAccountDetailsCallbacks.containsKey(details.AgentData.GroupID)) {
			GroupAccountDetails account = new GroupAccountDetails();

			account.CurrentInterval = details.MoneyData.CurrentInterval;
			account.IntervalDays = details.MoneyData.IntervalDays;
			account.StartDate = Helpers.BytesToString(details.MoneyData.getStartDate());

			account.HistoryItems = new HashMapInt<String>();

			for (int i = 0; i < details.HistoryData.length; i++) {
				GroupAccountDetailsReplyPacket.HistoryDataBlock block = details.HistoryData[i];
				account.HistoryItems.put(Helpers.BytesToString(block.getDescription()), block.Amount);
			}
			OnGroupAccountDetailsCallbacks.get(details.AgentData.GroupID).callback(account);
		}
	}

	private final void HandleGroupAccountTransactions(Packet packet, Simulator simulator) throws Exception {
		GroupAccountTransactionsReplyPacket transactions = (GroupAccountTransactionsReplyPacket) packet;

		if (OnGroupAccountTransactionsCallbacks.containsKey(transactions.AgentData.GroupID)) {
			GroupAccountTransactions account = new GroupAccountTransactions();

			account.CurrentInterval = transactions.MoneyData.CurrentInterval;
			account.IntervalDays = transactions.MoneyData.IntervalDays;
			account.StartDate = Helpers.BytesToString(transactions.MoneyData.getStartDate());

			account.Transactions = new TransactionEntry[transactions.HistoryData.length];
			for (int i = 0; i < transactions.HistoryData.length; i++) {
				TransactionEntry entry = account.new TransactionEntry();
				GroupAccountTransactionsReplyPacket.HistoryDataBlock block = transactions.HistoryData[i];

				entry.Type = block.Type;
				entry.Amount = block.Amount;
				entry.Item = Helpers.BytesToString(block.getItem());
				entry.User = Helpers.BytesToString(block.getUser());
				entry.Time = Helpers.BytesToString(block.getTime());
				account.Transactions[i] = entry;
			}
			OnGroupAccountTransactionsCallbacks.get(transactions.AgentData.GroupID).callback(account);
		}
	}

	// #region CallbackArgs

}
