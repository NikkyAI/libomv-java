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
import java.util.List;
import java.util.Map;
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

	private GridClient client;

	// Currently-active group members requests
	private List<UUID> groupMembersRequests;
	// Currently-active group roles requests
	private List<UUID> groupRolesRequests;
	// Currently-active group role-member requests
	private List<UUID> groupRolesMembersRequests;
	// Dictionary keeping group members while request is in progress
	private Map<UUID, Map<UUID, GroupMember>> tempGroupMembers;
	// Dictionary keeping member/role mapping while request is in progress
	private Map<UUID, List<Entry<UUID, UUID>>> tempGroupRolesMembers;
	// Dictionary keeping GroupRole information while request is in progress
	private Map<UUID, Map<UUID, GroupRole>> tempGroupRoles;
	// Caches groups this avatar is member of
	public Map<UUID, Group> groupList;
	// Caches group names of all groups known to us
	public Map<UUID, String> groupNames;

	public CallbackHandler<CurrentGroupsCallbackArgs> onCurrentGroups = new CallbackHandler<>();

	public CallbackHandler<GroupNamesCallbackArgs> onGroupNamesReply = new CallbackHandler<>();

	public CallbackHandler<GroupProfileCallbackArgs> onGroupProfile = new CallbackHandler<>();

	public CallbackHandler<GroupMembersReplyCallbackArgs> onGroupMembersReply = new CallbackHandler<>();

	public CallbackHandler<GroupRolesDataReplyCallbackArgs> onGroupRoleDataReply = new CallbackHandler<>();

	public CallbackHandler<GroupRolesMembersReplyCallbackArgs> onGroupRoleMembers = new CallbackHandler<>();

	public CallbackHandler<GroupTitlesReplyCallbackArgs> onGroupTitles = new CallbackHandler<>();

	public CallbackHandler<GroupAccountSummaryReplyCallbackArgs> onGroupAccountSummaryReply = new CallbackHandler<>();

	public CallbackHandler<GroupCreatedReplyCallbackArgs> onGroupCreatedReply = new CallbackHandler<>();

	public CallbackHandler<GroupOperationCallbackArgs> onGroupJoinedReply = new CallbackHandler<>();

	public CallbackHandler<GroupOperationCallbackArgs> onGroupLeaveReply = new CallbackHandler<>();

	public CallbackHandler<GroupDroppedCallbackArgs> onGroupDropped = new CallbackHandler<>();

	public CallbackHandler<GroupOperationCallbackArgs> onGroupMemberEjected = new CallbackHandler<>();

	public CallbackHandler<GroupNoticesListReplyCallbackArgs> onGroupNoticesListReply = new CallbackHandler<>();

	public CallbackHandler<GroupInvitationCallbackArgs> onGroupInvitation = new CallbackHandler<>();

	public CallbackHandler<BannedAgentsCallbackArgs> onBannedAgents = new CallbackHandler<>();

	public Map<UUID, Callback<GroupAccountDetails>> onGroupAccountDetailsCallbacks = new HashMap<>();

	public Map<UUID, Callback<GroupAccountTransactions>> onGroupAccountTransactionsCallbacks = new HashMap<>();

	private class InstantMessageCallback implements Callback<InstantMessageCallbackArgs> {
		@Override
		public boolean callback(InstantMessageCallbackArgs e) {
			if (onGroupInvitation.count() > 0 && e.getIM().dialog == InstantMessageDialog.GroupInvitation) {
				byte[] bucket = e.getIM().binaryBucket;
				int fee = -1;
				UUID roleID = null;
				if (bucket.length == 20) {
					fee = Helpers.BytesToInt32B(bucket);
					roleID = new UUID(bucket, 4);
				}

				GroupInvitationCallbackArgs args = new GroupInvitationCallbackArgs(e.getSimulator(),
						e.getIM().imSessionID, e.getIM().fromAgentID, roleID, e.getIM().fromAgentName,
						e.getIM().message, fee);
				onGroupInvitation.dispatch(args);
			}
			return false;
		}
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
					titles[i] = titlesOSD.get(i).asString();
				}
				UUID groupID = res.get("group_id").asUUID();
				long defaultPowers = ((OSDMap) res.get("defaults")).get("default_powers").asULong();
				OSDMap membersOSD = (OSDMap) res.get("members");
				HashMap<UUID, GroupMember> groupMembers = new HashMap<UUID, GroupMember>(membersOSD.size());
				for (String memberID : membersOSD.keySet()) {
					OSDMap member = (OSDMap) membersOSD.get(memberID);

					GroupMember groupMember = new GroupMember(UUID.parse(memberID));
					groupMember.contribution = member.get("donated_square_meters").asInteger();
					groupMember.isOwner = "Y" == member.get("owner").asString();
					groupMember.onlineStatus = member.get("last_login").asString();
					groupMember.powers = defaultPowers;
					if (member.containsKey("powers")) {
						groupMember.powers = member.get("powers").asULong();
					}
					groupMember.title = titles[member.get("title").asInteger()];
					groupMembers.put(groupMember.id, groupMember);
				}
				onGroupMembersReply.dispatch(new GroupMembersReplyCallbackArgs(requestID, groupID, groupMembers));
			} catch (Exception ex) {
				logger.error(GridClient.Log("Failed to decode result of GroupMemberData capability: ", client), ex);
			}
		}

		@Override
		public void failed(Exception ex) {
			logger.error(GridClient.Log("Failed to request GroupMemberData capability: ", client), ex);
		}

		@Override
		public void cancelled() {
			logger.error(GridClient.Log("GroupMemberData capability request canceled!", client));
		}
	}

	public GroupManager(GridClient client) {
		this.client = client;

		this.tempGroupMembers = new HashMap<>();
		this.groupMembersRequests = new ArrayList<>();
		this.tempGroupRoles = new HashMap<>();
		this.groupRolesRequests = new ArrayList<>();
		this.tempGroupRolesMembers = new HashMap<>();
		this.groupRolesMembersRequests = new ArrayList<>();
		this.groupList = new HashList<>();
		this.groupNames = new HashMap<>();

		this.client.agent.onInstantMessage.add(new InstantMessageCallback());

		this.client.network.registerCallback(CapsEventType.AgentGroupDataUpdate, this);
		// deprecated in simulator v1.27
		this.client.network.registerCallback(PacketType.AgentGroupDataUpdate, this);

		this.client.network.registerCallback(CapsEventType.AgentDropGroup, this);
		// deprecated in simulator v1.27
		this.client.network.registerCallback(PacketType.AgentDropGroup, this);

		this.client.network.registerCallback(PacketType.GroupTitlesReply, this);
		this.client.network.registerCallback(PacketType.GroupProfileReply, this);
		this.client.network.registerCallback(PacketType.GroupMembersReply, this);
		this.client.network.registerCallback(PacketType.GroupRoleDataReply, this);
		this.client.network.registerCallback(PacketType.GroupRoleMembersReply, this);
		this.client.network.registerCallback(PacketType.GroupActiveProposalItemReply, this);
		this.client.network.registerCallback(PacketType.GroupVoteHistoryItemReply, this);
		this.client.network.registerCallback(PacketType.GroupAccountSummaryReply, this);
		this.client.network.registerCallback(PacketType.GroupAccountDetailsReply, this);
		this.client.network.registerCallback(PacketType.GroupAccountTransactionsReply, this);
		this.client.network.registerCallback(PacketType.CreateGroupReply, this);
		this.client.network.registerCallback(PacketType.JoinGroupReply, this);
		this.client.network.registerCallback(PacketType.LeaveGroupReply, this);
		this.client.network.registerCallback(PacketType.UUIDGroupNameReply, this);
		this.client.network.registerCallback(PacketType.EjectGroupMemberReply, this);
		this.client.network.registerCallback(PacketType.GroupNoticesListReply, this);
	}

	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) throws Exception {
		switch (message.getType()) {
		case AgentGroupDataUpdate:
			handleAgentGroupDataUpdate(message, simulator);
		case AgentDropGroup:
			handleAgentDropGroup(message, simulator);
		default:
			break;
		}
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case AgentGroupDataUpdate:
			handleAgentGroupDataUpdate(packet, simulator);
			break;
		case AgentDropGroup:
			handleAgentDropGroup(packet, simulator);
			break;
		case GroupTitlesReply:
			handleGroupTitlesReply(packet, simulator);
			break;
		case GroupProfileReply:
			handleGroupProfileReply(packet, simulator);
			break;
		case GroupMembersReply:
			handleGroupMembers(packet, simulator);
			break;
		case GroupRoleDataReply:
			handleGroupRoleDataReply(packet, simulator);
			break;
		case GroupRoleMembersReply:
			handleGroupRoleMembersReply(packet, simulator);
			break;
		case GroupActiveProposalItemReply:
			handleGroupActiveProposalItem(packet, simulator);
			break;
		case GroupVoteHistoryItemReply:
			handleGroupVoteHistoryItem(packet, simulator);
			break;
		case GroupAccountSummaryReply:
			handleGroupAccountSummaryReply(packet, simulator);
			break;
		case GroupAccountDetailsReply:
			handleGroupAccountDetails(packet, simulator);
			break;
		case GroupAccountTransactionsReply:
			handleGroupAccountTransactions(packet, simulator);
			break;
		case CreateGroupReply:
			handleCreateGroupReply(packet, simulator);
			break;
		case JoinGroupReply:
			handleJoinGroupReply(packet, simulator);
			break;
		case LeaveGroupReply:
			handleLeaveGroupReply(packet, simulator);
			break;
		case EjectGroupMemberReply:
			handleEjectGroupMemberReply(packet, simulator);
			break;
		case GroupNoticesListReply:
			handleGroupNoticesListReply(packet, simulator);
			break;
		case UUIDGroupNameReply:
			handleUUIDGroupNameReply(packet, simulator);
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
	public final void requestCurrentGroups() throws Exception {
		AgentDataUpdateRequestPacket request = new AgentDataUpdateRequestPacket();

		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();

		client.network.sendPacket(request);
	}

	/**
	 * Lookup name of group based on groupID
	 *
	 * @param groupID
	 *            groupID of group to lookup name for.
	 * @throws Exception
	 */
	public final void requestGroupName(UUID groupID) throws Exception {
		// if we already have this in the cache, return from cache instead of
		// making a request
		synchronized (groupNames) {
			if (groupNames.containsKey(groupID)) {
				HashMap<UUID, String> groupNamesList = new HashMap<UUID, String>();
				groupNamesList.put(groupID, groupNames.get(groupID));

				onGroupNamesReply.dispatch(new GroupNamesCallbackArgs(groupNamesList));
				return;
			}
		}

		UUIDGroupNameRequestPacket req = new UUIDGroupNameRequestPacket();
		req.ID = new UUID[1];
		req.ID[0] = groupID;
		client.network.sendPacket(req);
	}

	/**
	 * Request lookup of multiple group names
	 *
	 * @param groupIDs
	 *            List of group IDs to request.
	 * @throws Exception
	 */
	public final void requestGroupNames(List<UUID> groupIDs) throws Exception {
		Map<UUID, String> groupNamesList = new HashMap<>();
		List<UUID> tempIDs = new ArrayList<>();
		synchronized (groupNames) {
			for (UUID groupID : groupIDs) {
				if (groupNames.containsKey(groupID)) {
					groupNamesList.put(groupID, groupNames.get(groupID));
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
			client.network.sendPacket(req);
		}

		// fire handler from cache
		if (groupNamesList.size() > 0)
			onGroupNamesReply.dispatch(new GroupNamesCallbackArgs(groupNamesList));
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
	public final void requestGroupProfile(UUID group) throws Exception {
		GroupProfileRequestPacket request = new GroupProfileRequestPacket();

		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();
		request.GroupID = group;

		client.network.sendPacket(request);
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
	public final UUID requestGroupMembers(UUID group) throws Exception {
		UUID requestID = new UUID();
		URI url = client.network.getCapabilityURI("GroupMemberData");
		if (url != null) {
			CapsClient req = new CapsClient(client, "GroupMemberData");
			OSDMap requestData = new OSDMap(1);
			requestData.put("group_id", OSD.fromUUID(group));
			req.executeHttpPost(url, requestData, OSDFormat.Xml, new GroupMembersHandlerCaps(requestID),
					client.settings.CAPS_TIMEOUT * 4);
		} else {
			synchronized (groupMembersRequests) {
				groupMembersRequests.add(requestID);
			}

			GroupMembersRequestPacket request = new GroupMembersRequestPacket();

			request.AgentData.AgentID = client.agent.getAgentID();
			request.AgentData.SessionID = client.agent.getSessionID();
			request.GroupData.GroupID = group;
			request.GroupData.RequestID = requestID;

			client.network.sendPacket(request);
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
	public final UUID requestGroupRoles(UUID group) throws Exception {
		UUID requestID = new UUID();
		synchronized (groupRolesRequests) {
			groupRolesRequests.add(requestID);
		}

		GroupRoleDataRequestPacket request = new GroupRoleDataRequestPacket();

		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();
		request.GroupData.GroupID = group;
		request.GroupData.RequestID = requestID;

		client.network.sendPacket(request);
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
	public final UUID requestGroupRolesMembers(UUID group) throws Exception {
		UUID requestID = new UUID();
		synchronized (groupRolesRequests) {
			groupRolesMembersRequests.add(requestID);
		}

		GroupRoleMembersRequestPacket request = new GroupRoleMembersRequestPacket();
		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();
		request.GroupData.GroupID = group;
		request.GroupData.RequestID = requestID;
		client.network.sendPacket(request);
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
	public final UUID requestGroupTitles(UUID group) throws Exception {
		UUID requestID = new UUID();

		GroupTitlesRequestPacket request = new GroupTitlesRequestPacket();

		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();
		request.AgentData.GroupID = group;
		request.AgentData.RequestID = requestID;

		client.network.sendPacket(request);
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
	public final void requestGroupAccountSummary(UUID group, int intervalDays, int currentInterval) throws Exception {
		GroupAccountSummaryRequestPacket p = new GroupAccountSummaryRequestPacket();
		p.AgentData.AgentID = client.agent.getAgentID();
		p.AgentData.SessionID = client.agent.getSessionID();
		p.AgentData.GroupID = group;
		// TODO: Store request ID to identify the callback
		p.MoneyData.RequestID = new UUID();
		p.MoneyData.CurrentInterval = currentInterval;
		p.MoneyData.IntervalDays = intervalDays;
		client.network.sendPacket(p);
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
	public final void requestGroupAccountDetails(UUID group, int intervalDays, int currentInterval) throws Exception {
		GroupAccountDetailsRequestPacket p = new GroupAccountDetailsRequestPacket();
		p.AgentData.AgentID = client.agent.getAgentID();
		p.AgentData.SessionID = client.agent.getSessionID();
		p.AgentData.GroupID = group;
		// TODO: Store request ID to identify the callback
		p.MoneyData.RequestID = new UUID();
		p.MoneyData.CurrentInterval = currentInterval;
		p.MoneyData.IntervalDays = intervalDays;
		client.network.sendPacket(p);
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
	public final void invite(UUID group, List<UUID> roles, UUID personkey) throws Exception {
		InviteGroupRequestPacket igp = new InviteGroupRequestPacket();

		igp.AgentData = igp.new AgentDataBlock();
		igp.AgentData.AgentID = client.agent.getAgentID();
		igp.AgentData.SessionID = client.agent.getSessionID();

		igp.GroupID = group;

		igp.InviteData = new InviteGroupRequestPacket.InviteDataBlock[roles.size()];

		for (int i = 0; i < roles.size(); i++) {
			igp.InviteData[i] = igp.new InviteDataBlock();
			igp.InviteData[i].InviteeID = personkey;
			igp.InviteData[i].RoleID = roles.get(i);
		}

		client.network.sendPacket(igp);
	}

	/**
	 * Set a group as the current active group
	 *
	 * @param id
	 *            group ID (UUID)
	 * @throws Exception
	 */
	public final void activateGroup(UUID id) throws Exception {
		ActivateGroupPacket activate = new ActivateGroupPacket();
		activate.AgentData.AgentID = client.agent.getAgentID();
		activate.AgentData.SessionID = client.agent.getSessionID();
		activate.AgentData.GroupID = id;

		client.network.sendPacket(activate);
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
	public final void activateTitle(UUID group, UUID role) throws Exception {
		GroupTitleUpdatePacket gtu = new GroupTitleUpdatePacket();
		gtu.AgentData.AgentID = client.agent.getAgentID();
		gtu.AgentData.SessionID = client.agent.getSessionID();
		gtu.AgentData.TitleRoleID = role;
		gtu.AgentData.GroupID = group;

		client.network.sendPacket(gtu);
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
	public final void setGroupContribution(UUID group, int contribution) throws Exception {
		SetGroupContributionPacket sgp = new SetGroupContributionPacket();
		sgp.AgentData.AgentID = client.agent.getAgentID();
		sgp.AgentData.SessionID = client.agent.getSessionID();
		sgp.Data.GroupID = group;
		sgp.Data.Contribution = contribution;

		client.network.sendPacket(sgp);
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
	public final void setGroupAcceptNotices(UUID groupID, boolean acceptNotices, boolean listInProfile)
			throws Exception {
		SetGroupAcceptNoticesPacket p = new SetGroupAcceptNoticesPacket();
		p.AgentData.AgentID = client.agent.getAgentID();
		p.AgentData.SessionID = client.agent.getSessionID();
		p.Data.GroupID = groupID;
		p.Data.AcceptNotices = acceptNotices;
		p.ListInProfile = listInProfile;

		client.network.sendPacket(p);
	}

	/**
	 * Request to join a group
	 *
	 * @param id
	 *            group ID (UUID) to join.
	 * @throws Exception
	 */
	public final void requestJoinGroup(UUID id) throws Exception {
		JoinGroupRequestPacket join = new JoinGroupRequestPacket();
		join.AgentData.AgentID = client.agent.getAgentID();
		join.AgentData.SessionID = client.agent.getSessionID();

		join.GroupID = id;

		client.network.sendPacket(join);
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
	public final void requestCreateGroup(Group group) throws Exception {
		CreateGroupRequestPacket cgrp = new CreateGroupRequestPacket();
		cgrp.AgentData = cgrp.new AgentDataBlock();
		cgrp.AgentData.AgentID = client.agent.getAgentID();
		cgrp.AgentData.SessionID = client.agent.getSessionID();

		cgrp.GroupData = cgrp.new GroupDataBlock();
		cgrp.GroupData.AllowPublish = group.allowPublish;
		cgrp.GroupData.setCharter(Helpers.stringToBytes(group.charter));
		cgrp.GroupData.InsigniaID = group.insigniaID;
		cgrp.GroupData.MaturePublish = group.maturePublish;
		cgrp.GroupData.MembershipFee = group.membershipFee;
		cgrp.GroupData.setName(Helpers.stringToBytes(group.getName()));
		cgrp.GroupData.OpenEnrollment = group.openEnrollment;
		cgrp.GroupData.ShowInList = group.showInList;

		client.network.sendPacket(cgrp);
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
	public final void updateGroup(UUID id, Group group) throws Exception {
		UpdateGroupInfoPacket cgrp = new UpdateGroupInfoPacket();
		cgrp.AgentData = cgrp.new AgentDataBlock();
		cgrp.AgentData.AgentID = client.agent.getAgentID();
		cgrp.AgentData.SessionID = client.agent.getSessionID();

		cgrp.GroupData = cgrp.new GroupDataBlock();
		cgrp.GroupData.GroupID = id;
		cgrp.GroupData.AllowPublish = group.allowPublish;
		cgrp.GroupData.setCharter(Helpers.stringToBytes(group.charter));
		cgrp.GroupData.InsigniaID = group.insigniaID;
		cgrp.GroupData.MaturePublish = group.maturePublish;
		cgrp.GroupData.MembershipFee = group.membershipFee;
		cgrp.GroupData.OpenEnrollment = group.openEnrollment;
		cgrp.GroupData.ShowInList = group.showInList;

		client.network.sendPacket(cgrp);
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
	public final void ejectUser(UUID group, UUID member) throws Exception {
		EjectGroupMemberRequestPacket eject = new EjectGroupMemberRequestPacket();
		eject.AgentData = eject.new AgentDataBlock();
		eject.AgentData.AgentID = client.agent.getAgentID();
		eject.AgentData.SessionID = client.agent.getSessionID();

		eject.GroupID = group;

		eject.EjecteeID = new UUID[1];
		eject.EjecteeID[0] = member;

		client.network.sendPacket(eject);
	}

	/**
	 * Update role information
	 *
	 * @param role
	 *            Modified role to be updated
	 * @throws Exception
	 */
	public final void updateRole(GroupRole role) throws Exception {
		GroupRoleUpdatePacket gru = new GroupRoleUpdatePacket();
		gru.AgentData.AgentID = client.agent.getAgentID();
		gru.AgentData.SessionID = client.agent.getSessionID();
		gru.AgentData.GroupID = role.groupID;
		gru.RoleData = new GroupRoleUpdatePacket.RoleDataBlock[1];
		gru.RoleData[0] = gru.new RoleDataBlock();
		gru.RoleData[0].setName(Helpers.stringToBytes(role.name));
		gru.RoleData[0].setDescription(Helpers.stringToBytes(role.description));
		gru.RoleData[0].Powers = role.powers;
		gru.RoleData[0].RoleID = role.id;
		gru.RoleData[0].setTitle(Helpers.stringToBytes(role.title));
		gru.RoleData[0].UpdateType = GroupRoleUpdate.UpdateAll.getValue();
		client.network.sendPacket(gru);
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
	public final void createRole(UUID group, GroupRole role) throws Exception {
		GroupRoleUpdatePacket gru = new GroupRoleUpdatePacket();
		gru.AgentData.AgentID = client.agent.getAgentID();
		gru.AgentData.SessionID = client.agent.getSessionID();
		gru.AgentData.GroupID = group;
		gru.RoleData = new GroupRoleUpdatePacket.RoleDataBlock[1];
		gru.RoleData[0] = gru.new RoleDataBlock();
		gru.RoleData[0].RoleID = new UUID();
		gru.RoleData[0].setName(Helpers.stringToBytes(role.name));
		gru.RoleData[0].setDescription(Helpers.stringToBytes(role.description));
		gru.RoleData[0].Powers = role.powers;
		gru.RoleData[0].setTitle(Helpers.stringToBytes(role.title));
		gru.RoleData[0].UpdateType = GroupRoleUpdate.Create.getValue();
		client.network.sendPacket(gru);
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
	public final void deleteRole(UUID group, UUID roleID) throws Exception {
		GroupRoleUpdatePacket gru = new GroupRoleUpdatePacket();
		gru.AgentData.AgentID = client.agent.getAgentID();
		gru.AgentData.SessionID = client.agent.getSessionID();
		gru.AgentData.GroupID = group;
		gru.RoleData = new GroupRoleUpdatePacket.RoleDataBlock[1];
		gru.RoleData[0] = gru.new RoleDataBlock();
		gru.RoleData[0].RoleID = roleID;
		gru.RoleData[0].setName(Helpers.stringToBytes(Helpers.EmptyString));
		gru.RoleData[0].setDescription(Helpers.stringToBytes(Helpers.EmptyString));
		gru.RoleData[0].Powers = 0;
		gru.RoleData[0].setTitle(Helpers.stringToBytes(Helpers.EmptyString));
		gru.RoleData[0].UpdateType = GroupRoleUpdate.Delete.getValue();
		client.network.sendPacket(gru);
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
	public final void removeFromRole(UUID group, UUID role, UUID member) throws Exception {
		GroupRoleChangesPacket grc = new GroupRoleChangesPacket();
		grc.AgentData.AgentID = client.agent.getAgentID();
		grc.AgentData.SessionID = client.agent.getSessionID();
		grc.AgentData.GroupID = group;
		grc.RoleChange = new GroupRoleChangesPacket.RoleChangeBlock[1];
		grc.RoleChange[0] = grc.new RoleChangeBlock();
		// Add to members and role
		grc.RoleChange[0].MemberID = member;
		grc.RoleChange[0].RoleID = role;
		// 1 = Remove From Role TODO: this should be in an enum
		grc.RoleChange[0].Change = 1;
		client.network.sendPacket(grc);
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
	public final void addToRole(UUID group, UUID role, UUID member) throws Exception {
		GroupRoleChangesPacket grc = new GroupRoleChangesPacket();
		grc.AgentData.AgentID = client.agent.getAgentID();
		grc.AgentData.SessionID = client.agent.getSessionID();
		grc.AgentData.GroupID = group;
		grc.RoleChange = new GroupRoleChangesPacket.RoleChangeBlock[1];
		grc.RoleChange[0] = grc.new RoleChangeBlock();
		// Add to members and role
		grc.RoleChange[0].MemberID = member;
		grc.RoleChange[0].RoleID = role;
		// 0 = Add to Role TODO: this should be in an enum
		grc.RoleChange[0].Change = 0;
		client.network.sendPacket(grc);
	}

	/**
	 * Request the group notices list
	 *
	 * @param group
	 *            Group ID to fetch notices for
	 * @throws Exception
	 */
	public final void requestGroupNoticesList(UUID group) throws Exception {
		GroupNoticesListRequestPacket gnl = new GroupNoticesListRequestPacket();
		gnl.AgentData.AgentID = client.agent.getAgentID();
		gnl.AgentData.SessionID = client.agent.getSessionID();
		gnl.GroupID = group;
		client.network.sendPacket(gnl);
	}

	/**
	 * Request a group notice by key
	 *
	 * @param noticeID
	 *            ID of group notice
	 * @throws Exception
	 */
	public final void requestGroupNotice(UUID noticeID) throws Exception {
		GroupNoticeRequestPacket gnr = new GroupNoticeRequestPacket();
		gnr.AgentData.AgentID = client.agent.getAgentID();
		gnr.AgentData.SessionID = client.agent.getSessionID();
		gnr.GroupNoticeID = noticeID;
		client.network.sendPacket(gnr);
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
	public final void sendGroupNotice(UUID group, GroupNotice notice) throws Exception {
		client.agent.instantMessage(client.agent.getName(), group, notice.subject + "|" + notice.message, UUID.Zero,
				InstantMessageDialog.GroupNotice, InstantMessageOnline.Online, Vector3.Zero, UUID.Zero, 0,
				notice.serializeAttachment());
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
	public final void startProposal(UUID group, GroupProposal prop) throws Exception {
		StartGroupProposalPacket p = new StartGroupProposalPacket();
		p.AgentData.AgentID = client.agent.getAgentID();
		p.AgentData.SessionID = client.agent.getSessionID();
		p.ProposalData.GroupID = group;
		p.ProposalData.setProposalText(Helpers.stringToBytes(prop.proposalText));
		p.ProposalData.Quorum = prop.quorum;
		p.ProposalData.Majority = prop.majority;
		p.ProposalData.Duration = prop.duration;
		client.network.sendPacket(p);
	}

	/**
	 * Request to leave a group
	 *
	 * @param groupID
	 *            The group to leave
	 * @throws Exception
	 */
	public final void leaveGroup(UUID groupID) throws Exception {
		LeaveGroupRequestPacket p = new LeaveGroupRequestPacket();
		p.AgentData.AgentID = client.agent.getAgentID();
		p.AgentData.SessionID = client.agent.getSessionID();
		p.GroupID = groupID;

		client.network.sendPacket(p);
	}

	/**
	 * Gets the URI of the cpability for handling group bans
	 *
	 * @param groupID
	 *            UUID of the group
	 * @throws URISyntaxException
	 * @returns null, if the feature is not supported, or URI of the capability
	 */
	public URI getGroupAPIUri(UUID groupID) throws URISyntaxException {
		URI ret = client.network.getCapabilityURI("GroupAPIv1");
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
	public void requestBannedAgents(UUID groupID) throws IOReactorException, URISyntaxException {
		requestBannedAgents(groupID, null);
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
	public void requestBannedAgents(final UUID groupID, final Callback<BannedAgentsCallbackArgs> callback)
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
				onBannedAgents.dispatch(ret);
			}

			@SuppressWarnings("unlikely-arg-type")
			@Override
			public void completed(OSD result) {
				UUID gid = ((OSDMap) result).get("group_id").asUUID();
				OSDMap banList = (OSDMap) ((OSDMap) result).get("ban_list");
				HashMap<UUID, Date> bannedAgents = new HashMap<UUID, Date>(banList.size());

				for (String id : banList.keySet()) {
					UUID uid = new UUID(id);
					bannedAgents.put(uid, ((OSDMap) banList.get(uid)).get("ban_date").asDate());
				}
				BannedAgentsCallbackArgs ret = new BannedAgentsCallbackArgs(gid, true, bannedAgents);

				if (callback != null) {
					try {
						callback.callback(ret);
					} catch (Exception ex) {
					}
				}
				onBannedAgents.dispatch(ret);
			}

			@Override
			public void failed(Exception ex) {
				logger.warn(GridClient.Log("Failed to get a list of banned group members: " + ex.getMessage(), client));
				BannedAgentsCallbackArgs ret = new BannedAgentsCallbackArgs(groupID, false, null);
				if (callback != null) {
					try {
						callback.callback(ret);
					} catch (Exception ex1) {
					}
				}
				onBannedAgents.dispatch(ret);
			}
		}

		URI uri = getGroupAPIUri(groupID);
		if (uri == null)
			return;
		CapsClient req = new CapsClient(client, "GroupAPIv1");
		req.executeHttpGet(uri, null, new ClientCallback(), client.settings.CAPS_TIMEOUT);
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
	public void requestBanAction(UUID groupID, GroupBanAction action, UUID[] agents)
			throws IOReactorException, URISyntaxException {
		requestBanAction(groupID, action, agents, null);
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
	public void requestBanAction(final UUID groupID, GroupBanAction action, UUID[] agents,
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
				logger.warn(GridClient.Log("Failed to ban or unban group members: " + ex.getMessage(), client));
				if (callback != null) {
					try {
						callback.callback(null);
					} catch (Exception ex1) {
					}
				}
			}
		}

		URI uri = getGroupAPIUri(groupID);
		if (uri == null)
			return;

		OSDMap request = new OSDMap();
		request.put("ban_action", OSD.fromInteger(action.getValue()));
		OSDArray banIDs = new OSDArray(agents.length);
		for (UUID agent : agents) {
			banIDs.add(OSD.fromUUID(agent));
		}
		request.put("ban_ids", banIDs);
		CapsClient req = new CapsClient(client, "GroupAPIv1");
		req.executeHttpPost(uri, request, OSDFormat.Xml, new ClientCallback(), client.settings.CAPS_TIMEOUT);
	}

	private final void handleAgentGroupDataUpdate(IMessage message, Simulator simulator) {
		HashMap<UUID, Group> currentGroups = onCurrentGroups.count() > 0 ? new HashMap<UUID, Group>() : null;
		AgentGroupDataUpdateMessage msg = (AgentGroupDataUpdateMessage) message;

		for (int i = 0; i < msg.groupDataBlock.length; i++) {
			Group group = new Group(msg.groupDataBlock[i].groupID);
			group.insigniaID = msg.groupDataBlock[i].groupInsigniaID;
			group.name = msg.groupDataBlock[i].groupName;
			group.contribution = msg.groupDataBlock[i].contribution;
			group.acceptNotices = msg.groupDataBlock[i].acceptNotices;
			group.powers = msg.groupDataBlock[i].groupPowers;
			group.listInProfile = msg.newGroupDataBlock[i].listInProfile;

			if (currentGroups != null)
				currentGroups.put(group.getID(), group);

			synchronized (groupList) {
				groupList.put(group.getID(), group);
			}
			synchronized (groupNames) {
				groupNames.put(group.getID(), group.name);
			}
		}

		if (currentGroups != null)
			onCurrentGroups.dispatch(new CurrentGroupsCallbackArgs(currentGroups));
	}

	private final void handleAgentGroupDataUpdate(Packet packet, Simulator simulator) throws Exception {
		HashMap<UUID, Group> currentGroups = onCurrentGroups.count() > 0 ? new HashMap<UUID, Group>() : null;
		AgentGroupDataUpdatePacket update = (AgentGroupDataUpdatePacket) packet;

		for (AgentGroupDataUpdatePacket.GroupDataBlock block : update.GroupData) {
			Group group = new Group(block.GroupID);

			group.insigniaID = block.GroupInsigniaID;
			group.name = Helpers.BytesToString(block.getGroupName());
			group.powers = block.GroupPowers;
			group.contribution = block.Contribution;
			group.acceptNotices = block.AcceptNotices;

			if (currentGroups != null)
				currentGroups.put(block.GroupID, group);

			synchronized (groupList) {
				groupList.put(group.getID(), group);
			}
			synchronized (groupNames) {
				groupNames.put(group.getID(), group.name);
			}
		}

		if (currentGroups != null)
			onCurrentGroups.dispatch(new CurrentGroupsCallbackArgs(currentGroups));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void handleAgentDropGroup(IMessage message, Simulator simulator) {
		if (onGroupDropped.count() > 0) {
			AgentDropGroupMessage msg = (AgentDropGroupMessage) message;
			for (int i = 0; i < msg.agentDataBlock.length; i++) {
				onGroupDropped.dispatch(new GroupDroppedCallbackArgs(msg.agentDataBlock[i].groupID));
			}
		}
	}

	private final void handleAgentDropGroup(Packet packet, Simulator simulator) {
		onGroupDropped.dispatch(new GroupDroppedCallbackArgs(((AgentDropGroupPacket) packet).AgentData.GroupID));
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
	private final void handleGroupProfileReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (onGroupProfile.count() > 0) {
			GroupProfileReplyPacket profile = (GroupProfileReplyPacket) packet;
			Group group = new Group(profile.GroupData.GroupID);

			group.allowPublish = profile.GroupData.AllowPublish;
			group.charter = Helpers.BytesToString(profile.GroupData.getCharter());
			group.founderID = profile.GroupData.FounderID;
			group.groupMembershipCount = profile.GroupData.GroupMembershipCount;
			group.groupRolesCount = profile.GroupData.GroupRolesCount;
			group.insigniaID = profile.GroupData.InsigniaID;
			group.maturePublish = profile.GroupData.MaturePublish;
			group.membershipFee = profile.GroupData.MembershipFee;
			group.memberTitle = Helpers.BytesToString(profile.GroupData.getMemberTitle());
			group.money = profile.GroupData.Money;
			group.name = Helpers.BytesToString(profile.GroupData.getName());
			group.openEnrollment = profile.GroupData.OpenEnrollment;
			group.ownerRole = profile.GroupData.OwnerRole;
			group.powers = profile.GroupData.PowersMask;
			group.showInList = profile.GroupData.ShowInList;

			onGroupProfile.dispatch(new GroupProfileCallbackArgs(group));
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
	private final void handleGroupNoticesListReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException {
		if (onGroupNoticesListReply.count() > 0) {
			GroupNoticesListReplyPacket reply = (GroupNoticesListReplyPacket) packet;

			List<GroupNoticesListEntry> notices = new ArrayList<>();

			for (GroupNoticesListReplyPacket.DataBlock entry : reply.Data) {
				GroupNoticesListEntry notice = new GroupNoticesListEntry();
				notice.fromName = Helpers.BytesToString(entry.getFromName());
				notice.subject = Helpers.BytesToString(entry.getSubject());
				notice.noticeID = entry.NoticeID;
				notice.timestamp = entry.Timestamp;
				notice.hasAttachment = entry.HasAttachment;
				notice.assetType = AssetType.setValue(entry.AssetType);

				notices.add(notice);
			}
			onGroupNoticesListReply.dispatch(new GroupNoticesListReplyCallbackArgs(reply.AgentData.GroupID, notices));
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
	private final void handleGroupTitlesReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (onGroupTitles.count() > 0) {
			GroupTitlesReplyPacket titles = (GroupTitlesReplyPacket) packet;
			Map<UUID, GroupTitle> groupTitleCache = new HashMap<>();

			for (GroupTitlesReplyPacket.GroupDataBlock block : titles.GroupData) {
				GroupTitle groupTitle = new GroupTitle();

				groupTitle.groupID = titles.AgentData.GroupID;
				groupTitle.roleID = block.RoleID;
				groupTitle.title = Helpers.BytesToString(block.getTitle());
				groupTitle.selected = block.Selected;

				groupTitleCache.put(block.RoleID, groupTitle);
			}
			onGroupTitles.dispatch(new GroupTitlesReplyCallbackArgs(titles.AgentData.RequestID,
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
	private final void handleGroupMembers(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		GroupMembersReplyPacket members = (GroupMembersReplyPacket) packet;
		Map<UUID, GroupMember> groupMemberCache = null;

		synchronized (groupMembersRequests) {
			// If nothing is registered to receive this RequestID drop the data
			if (groupMembersRequests.contains(members.GroupData.RequestID)) {
				synchronized (tempGroupMembers) {
					if (tempGroupMembers.containsKey(members.GroupData.RequestID)) {
						groupMemberCache = tempGroupMembers.get(members.GroupData.RequestID);
					} else {
						groupMemberCache = new HashMap<>();
						tempGroupMembers.put(members.GroupData.RequestID, groupMemberCache);
					}

					for (GroupMembersReplyPacket.MemberDataBlock block : members.MemberData) {
						GroupMember groupMember = new GroupMember(block.AgentID);

						groupMember.contribution = block.Contribution;
						groupMember.isOwner = block.IsOwner;
						groupMember.onlineStatus = Helpers.BytesToString(block.getOnlineStatus());
						groupMember.powers = block.AgentPowers;
						groupMember.title = Helpers.BytesToString(block.getTitle());

						groupMemberCache.put(block.AgentID, groupMember);
					}

					if (groupMemberCache.size() >= members.GroupData.MemberCount) {
						groupMembersRequests.remove(members.GroupData.RequestID);
						tempGroupMembers.remove(members.GroupData.RequestID);
					}
				}
			}
		}

		if (groupMemberCache != null && groupMemberCache.size() >= members.GroupData.MemberCount) {
			onGroupMembersReply.dispatch(new GroupMembersReplyCallbackArgs(members.GroupData.RequestID,
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
	private final void handleGroupRoleDataReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException {
		GroupRoleDataReplyPacket roles = (GroupRoleDataReplyPacket) packet;
		Map<UUID, GroupRole> groupRoleCache = null;

		synchronized (groupRolesRequests) {
			// If nothing is registered to receive this RequestID drop the data
			if (groupRolesRequests.contains(roles.GroupData.RequestID)) {
				synchronized (tempGroupRoles) {
					if (tempGroupRoles.containsKey(roles.GroupData.RequestID)) {
						groupRoleCache = tempGroupRoles.get(roles.GroupData.RequestID);
					} else {
						groupRoleCache = new HashMap<>();
						tempGroupRoles.put(roles.GroupData.RequestID, groupRoleCache);
					}

					for (GroupRoleDataReplyPacket.RoleDataBlock block : roles.RoleData) {
						GroupRole groupRole = new GroupRole(roles.GroupData.GroupID);

						groupRole.id = block.RoleID;
						groupRole.description = Helpers.BytesToString(block.getDescription());
						groupRole.name = Helpers.BytesToString(block.getName());
						groupRole.powers = block.Powers;
						groupRole.title = Helpers.BytesToString(block.getTitle());

						groupRoleCache.put(block.RoleID, groupRole);
					}

					if (groupRoleCache.size() >= roles.GroupData.RoleCount) {
						groupRolesRequests.remove(roles.GroupData.RequestID);
						tempGroupRoles.remove(roles.GroupData.RequestID);
					}
				}
			}
		}

		if (groupRoleCache != null && groupRoleCache.size() >= roles.GroupData.RoleCount) {
			onGroupRoleDataReply.dispatch(new GroupRolesDataReplyCallbackArgs(roles.GroupData.RequestID,
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
	private final void handleGroupRoleMembersReply(Packet packet, Simulator simulator) {
		GroupRoleMembersReplyPacket members = (GroupRoleMembersReplyPacket) packet;
		List<Entry<UUID, UUID>> groupRoleMemberCache = null;

		synchronized (groupRolesMembersRequests) {
			// If nothing is registered to receive this RequestID drop the data
			if (groupRolesMembersRequests.contains(members.AgentData.RequestID)) {
				synchronized (tempGroupRolesMembers) {
					if (tempGroupRolesMembers.containsKey(members.AgentData.RequestID)) {
						groupRoleMemberCache = tempGroupRolesMembers.get(members.AgentData.RequestID);
					} else {
						groupRoleMemberCache = new ArrayList<>();
						tempGroupRolesMembers.put(members.AgentData.RequestID, groupRoleMemberCache);
					}

					for (GroupRoleMembersReplyPacket.MemberDataBlock block : members.MemberData) {
						Entry<UUID, UUID> rolemember = new AbstractMap.SimpleEntry<UUID, UUID>(block.RoleID,
								block.MemberID);

						groupRoleMemberCache.add(rolemember);
					}

					if (groupRoleMemberCache.size() >= members.AgentData.TotalPairs) {
						groupRolesMembersRequests.remove(members.AgentData.RequestID);
						tempGroupRolesMembers.remove(members.AgentData.RequestID);
					}
				}
			}
		}

		if (groupRoleMemberCache != null && groupRoleMemberCache.size() >= members.AgentData.TotalPairs) {
			onGroupRoleMembers.dispatch(new GroupRolesMembersReplyCallbackArgs(members.AgentData.RequestID,
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
	private final void handleGroupActiveProposalItem(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException {
		GroupActiveProposalItemReplyPacket proposal = (GroupActiveProposalItemReplyPacket) packet;

		// UUID transactionID = proposal.TransactionData.TransactionID;

		List<GroupProposalItem> array = new ArrayList<>(proposal.ProposalData.length);
		for (GroupActiveProposalItemReplyPacket.ProposalDataBlock block : proposal.ProposalData) {
			GroupProposalItem p = new GroupProposalItem();

			p.voteID = block.VoteID;
			p.voteInitiator = block.VoteInitiator;
			p.terseDateID = Helpers.BytesToString(block.getTerseDateID());
			p.startDateTime = Helpers.stringToDate(Helpers.BytesToString(block.getStartDateTime()));
			p.endDateTime = Helpers.stringToDate(Helpers.BytesToString(block.getEndDateTime()));
			p.alreadyVoted = block.AlreadyVoted;
			p.voteCast = Helpers.BytesToString(block.getVoteCast());
			p.majority = block.Majority;
			p.quorum = block.Quorum;
			p.proposalText = Helpers.BytesToString(block.getProposalText());

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
	private final void handleGroupVoteHistoryItem(Packet packet, Simulator simulator) {
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
	private final void handleGroupAccountSummaryReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException {
		if (onGroupAccountSummaryReply.count() > 0) {
			GroupAccountSummaryReplyPacket summary = (GroupAccountSummaryReplyPacket) packet;
			GroupAccountSummary account = new GroupAccountSummary();

			account.balance = summary.MoneyData.Balance;
			account.currentInterval = summary.MoneyData.CurrentInterval;
			account.groupTaxCurrent = summary.MoneyData.GroupTaxCurrent;
			account.groupTaxEstimate = summary.MoneyData.GroupTaxEstimate;
			account.intervalDays = summary.MoneyData.IntervalDays;
			account.landTaxCurrent = summary.MoneyData.LandTaxCurrent;
			account.landTaxEstimate = summary.MoneyData.LandTaxEstimate;
			account.lastTaxDate = Helpers.BytesToString(summary.MoneyData.getLastTaxDate());
			account.lightTaxCurrent = summary.MoneyData.LightTaxCurrent;
			account.lightTaxEstimate = summary.MoneyData.LightTaxEstimate;
			account.nonExemptMembers = summary.MoneyData.NonExemptMembers;
			account.objectTaxCurrent = summary.MoneyData.ObjectTaxCurrent;
			account.objectTaxEstimate = summary.MoneyData.ObjectTaxEstimate;
			account.parcelDirFeeCurrent = summary.MoneyData.ParcelDirFeeCurrent;
			account.parcelDirFeeEstimate = summary.MoneyData.ParcelDirFeeEstimate;
			account.startDate = Helpers.BytesToString(summary.MoneyData.getStartDate());
			account.taxDate = Helpers.BytesToString(summary.MoneyData.getTaxDate());
			account.totalCredits = summary.MoneyData.TotalCredits;
			account.totalDebits = summary.MoneyData.TotalDebits;

			onGroupAccountSummaryReply
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
	private final void handleCreateGroupReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		CreateGroupReplyPacket reply = (CreateGroupReplyPacket) packet;
		String message = Helpers.BytesToString(reply.ReplyData.getMessage());

		onGroupCreatedReply
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
	private final void handleJoinGroupReply(Packet packet, Simulator simulator) {
		JoinGroupReplyPacket reply = (JoinGroupReplyPacket) packet;

		onGroupJoinedReply.dispatch(new GroupOperationCallbackArgs(reply.GroupData.GroupID, reply.GroupData.Success));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void handleLeaveGroupReply(Packet packet, Simulator simulator) {
		LeaveGroupReplyPacket reply = (LeaveGroupReplyPacket) packet;

		onGroupLeaveReply.dispatch(new GroupOperationCallbackArgs(reply.GroupData.GroupID, reply.GroupData.Success));
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
	private void handleUUIDGroupNameReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		UUIDGroupNameReplyPacket reply = (UUIDGroupNameReplyPacket) packet;
		UUIDGroupNameReplyPacket.UUIDNameBlockBlock[] blocks = reply.UUIDNameBlock;

		Map<UUID, String> groupNamesList = new HashMap<>();

		for (UUIDGroupNameReplyPacket.UUIDNameBlockBlock block : blocks) {
			String name = Helpers.BytesToString(block.getGroupName());
			groupNamesList.put(block.ID, name);
			synchronized (groupNames) {
				groupNames.put(block.ID, name);
			}
			synchronized (groupList) {
				Group group = (Group) groupList.get(block.ID);
				if (group != null) {
					group.name = name;
				}
			}
		}
		onGroupNamesReply.dispatch(new GroupNamesCallbackArgs(groupNamesList));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void handleEjectGroupMemberReply(Packet packet, Simulator simulator) {
		EjectGroupMemberReplyPacket reply = (EjectGroupMemberReplyPacket) packet;

		// TODO: On Success remove the member from the cache(s)

		onGroupMemberEjected.dispatch(new GroupOperationCallbackArgs(reply.GroupID, reply.Success));
	}

	// #endregion Packet Handlers

	private final void handleGroupAccountDetails(Packet packet, Simulator simulator) throws Exception {
		GroupAccountDetailsReplyPacket details = (GroupAccountDetailsReplyPacket) packet;

		if (onGroupAccountDetailsCallbacks.containsKey(details.AgentData.GroupID)) {
			GroupAccountDetails account = new GroupAccountDetails();

			account.currentInterval = details.MoneyData.CurrentInterval;
			account.intervalDays = details.MoneyData.IntervalDays;
			account.startDate = Helpers.BytesToString(details.MoneyData.getStartDate());

			account.historyItems = new HashMapInt<String>();

			for (int i = 0; i < details.HistoryData.length; i++) {
				GroupAccountDetailsReplyPacket.HistoryDataBlock block = details.HistoryData[i];
				account.historyItems.put(Helpers.BytesToString(block.getDescription()), block.Amount);
			}
			onGroupAccountDetailsCallbacks.get(details.AgentData.GroupID).callback(account);
		}
	}

	private final void handleGroupAccountTransactions(Packet packet, Simulator simulator) throws Exception {
		GroupAccountTransactionsReplyPacket transactions = (GroupAccountTransactionsReplyPacket) packet;

		if (onGroupAccountTransactionsCallbacks.containsKey(transactions.AgentData.GroupID)) {
			GroupAccountTransactions account = new GroupAccountTransactions();

			account.currentInterval = transactions.MoneyData.CurrentInterval;
			account.intervalDays = transactions.MoneyData.IntervalDays;
			account.startDate = Helpers.BytesToString(transactions.MoneyData.getStartDate());

			account.transactions = new TransactionEntry[transactions.HistoryData.length];
			for (int i = 0; i < transactions.HistoryData.length; i++) {
				TransactionEntry entry = account.new TransactionEntry();
				GroupAccountTransactionsReplyPacket.HistoryDataBlock block = transactions.HistoryData[i];

				entry.type = block.Type;
				entry.amount = block.Amount;
				entry.item = Helpers.BytesToString(block.getItem());
				entry.user = Helpers.BytesToString(block.getUser());
				entry.time = Helpers.BytesToString(block.getTime());
				account.transactions[i] = entry;
			}
			onGroupAccountTransactionsCallbacks.get(transactions.AgentData.GroupID).callback(account);
		}
	}

}
