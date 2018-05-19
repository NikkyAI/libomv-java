/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Portions Copyright (c) 2006, Lateral Arts Limited
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

import libomv.Animations;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDMap;
import libomv.assets.AssetGesture;
import libomv.assets.AssetGesture.GestureStep;
import libomv.assets.AssetGesture.GestureStepAnimation;
import libomv.assets.AssetGesture.GestureStepChat;
import libomv.assets.AssetGesture.GestureStepWait;
import libomv.capabilities.CapsMessage.AgentStateUpdateMessage;
import libomv.capabilities.CapsMessage.AttachmentResourcesMessage;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.ChatSessionAcceptInvitation;
import libomv.capabilities.CapsMessage.ChatSessionRequestMuteUpdate;
import libomv.capabilities.CapsMessage.ChatSessionRequestStartConference;
import libomv.capabilities.CapsMessage.ChatterBoxInvitationMessage;
import libomv.capabilities.CapsMessage.ChatterBoxSessionAgentListUpdatesMessage;
import libomv.capabilities.CapsMessage.ChatterBoxSessionEventReplyMessage;
import libomv.capabilities.CapsMessage.ChatterBoxSessionStartReplyMessage;
import libomv.capabilities.CapsMessage.CrossedRegionMessage;
import libomv.capabilities.CapsMessage.EstablishAgentCommunicationMessage;
import libomv.capabilities.CapsMessage.SetDisplayNameMessage;
import libomv.capabilities.CapsMessage.SetDisplayNameReplyMessage;
import libomv.capabilities.CapsMessage.TeleportFailedMessage;
import libomv.capabilities.CapsMessage.TeleportFinishMessage;
import libomv.capabilities.CapsMessage.UpdateAgentLanguageMessage;
import libomv.capabilities.IMessage;
import libomv.io.capabilities.CapsCallback;
import libomv.io.capabilities.CapsClient;
import libomv.model.Simulator;
import libomv.model.agent.AgentAccessCallbackArgs;
import libomv.model.agent.AgentDataReplyCallbackArgs;
import libomv.model.agent.AgentFlags;
import libomv.model.agent.AlertMessageCallbackArgs;
import libomv.model.agent.AnimationsChangedCallbackArgs;
import libomv.model.agent.AttachmentResourcesCallbackArgs;
import libomv.model.agent.AvatarSitResponseCallbackArgs;
import libomv.model.agent.BalanceCallbackArgs;
import libomv.model.agent.CameraConstraintCallbackArgs;
import libomv.model.agent.ChatAudibleLevel;
import libomv.model.agent.ChatCallbackArgs;
import libomv.model.agent.ChatSessionMemberCallbackArgs;
import libomv.model.agent.ChatSourceType;
import libomv.model.agent.ChatType;
import libomv.model.agent.ControlFlags;
import libomv.model.agent.EffectType;
import libomv.model.agent.GenericMessageCallbackArgs;
import libomv.model.agent.GroupChatJoinedCallbackArgs;
import libomv.model.agent.InstantMessage;
import libomv.model.agent.InstantMessageCallbackArgs;
import libomv.model.agent.InstantMessageDialog;
import libomv.model.agent.InstantMessageOnline;
import libomv.model.agent.LookAtType;
import libomv.model.agent.LureLocation;
import libomv.model.agent.MeanCollisionCallbackArgs;
import libomv.model.agent.MeanCollisionType;
import libomv.model.agent.MoneyBalanceReplyCallbackArgs;
import libomv.model.agent.MoneyTransactionType;
import libomv.model.agent.MuteEntry;
import libomv.model.agent.MuteFlags;
import libomv.model.agent.MuteType;
import libomv.model.agent.PointAtType;
import libomv.model.agent.RegionCrossedCallbackArgs;
import libomv.model.agent.ScriptControlChange;
import libomv.model.agent.ScriptControlChangeCallbackArgs;
import libomv.model.agent.ScriptSensorReplyCallbackArgs;
import libomv.model.agent.ScriptSensorTypeFlags;
import libomv.model.agent.SetDisplayNameReplyCallbackArgs;
import libomv.model.agent.TeleportCallbackArgs;
import libomv.model.agent.TeleportFlags;
import libomv.model.agent.TeleportLureCallbackArgs;
import libomv.model.agent.TeleportStatus;
import libomv.model.agent.TransactionFlags;
import libomv.model.agent.TransactionInfo;
import libomv.model.asset.AssetDownload;
import libomv.model.asset.AssetType;
import libomv.model.asset.XferDownload;
import libomv.model.directory.ClassifiedCategories;
import libomv.model.directory.ClassifiedFlags;
import libomv.model.grid.GridLayerType;
import libomv.model.grid.GridRegion;
import libomv.model.group.ChatSessionMember;
import libomv.model.group.GroupPowers;
import libomv.model.login.LoginProgressCallbackArgs;
import libomv.model.login.LoginResponseData;
import libomv.model.login.LoginStatus;
import libomv.model.network.DisconnectedCallbackArgs;
import libomv.packets.ActivateGesturesPacket;
import libomv.packets.AgentAnimationPacket;
import libomv.packets.AgentDataUpdatePacket;
import libomv.packets.AgentFOVPacket;
import libomv.packets.AgentHeightWidthPacket;
import libomv.packets.AgentMovementCompletePacket;
import libomv.packets.AgentRequestSitPacket;
import libomv.packets.AgentSitPacket;
import libomv.packets.AgentUpdatePacket;
import libomv.packets.AlertMessagePacket;
import libomv.packets.AvatarAnimationPacket;
import libomv.packets.AvatarInterestsUpdatePacket;
import libomv.packets.AvatarPropertiesUpdatePacket;
import libomv.packets.AvatarSitResponsePacket;
import libomv.packets.CameraConstraintPacket;
import libomv.packets.ChatFromSimulatorPacket;
import libomv.packets.ChatFromViewerPacket;
import libomv.packets.ClassifiedDeletePacket;
import libomv.packets.ClassifiedInfoUpdatePacket;
import libomv.packets.CompleteAgentMovementPacket;
import libomv.packets.CrossedRegionPacket;
import libomv.packets.DeactivateGesturesPacket;
import libomv.packets.GenericMessagePacket;
import libomv.packets.HealthMessagePacket;
import libomv.packets.ImprovedInstantMessagePacket;
import libomv.packets.MeanCollisionAlertPacket;
import libomv.packets.MoneyBalanceReplyPacket;
import libomv.packets.MoneyBalanceRequestPacket;
import libomv.packets.MoneyTransferRequestPacket;
import libomv.packets.MuteListRequestPacket;
import libomv.packets.MuteListUpdatePacket;
import libomv.packets.ObjectDeGrabPacket;
import libomv.packets.ObjectGrabPacket;
import libomv.packets.ObjectGrabUpdatePacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.PickDeletePacket;
import libomv.packets.PickInfoUpdatePacket;
import libomv.packets.RemoveMuteListEntryPacket;
import libomv.packets.RetrieveInstantMessagesPacket;
import libomv.packets.ScriptAnswerYesPacket;
import libomv.packets.ScriptControlChangePacket;
import libomv.packets.ScriptDialogReplyPacket;
import libomv.packets.ScriptSensorReplyPacket;
import libomv.packets.ScriptSensorRequestPacket;
import libomv.packets.SetAlwaysRunPacket;
import libomv.packets.SetStartLocationRequestPacket;
import libomv.packets.StartLurePacket;
import libomv.packets.TeleportFailedPacket;
import libomv.packets.TeleportFinishPacket;
import libomv.packets.TeleportLandmarkRequestPacket;
import libomv.packets.TeleportLocalPacket;
import libomv.packets.TeleportLocationRequestPacket;
import libomv.packets.TeleportLureRequestPacket;
import libomv.packets.TeleportProgressPacket;
import libomv.packets.TeleportStartPacket;
import libomv.packets.UpdateMuteListEntryPacket;
import libomv.packets.ViewerEffectPacket;
import libomv.primitives.Avatar;
import libomv.primitives.Primitive;
import libomv.types.Color4;
import libomv.types.Matrix4;
import libomv.types.PacketCallback;
import libomv.types.Predicate;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;
import libomv.utils.Callback;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Settings.SettingsUpdateCallbackArgs;
import libomv.utils.TimeoutEvent;

// Class to hold Client Avatar's data
public class AgentManager implements PacketCallback, CapsCallback {
	private static final Logger logger = Logger.getLogger(AgentManager.class);

	private static final Vector3 X_AXIS = new Vector3(1f, 0f, 0f);
	private static final Vector3 Y_AXIS = new Vector3(0f, 1f, 0f);
	private static final Vector3 Z_AXIS = new Vector3(0f, 0f, 1f);

	public CallbackHandler<GroupChatJoinedCallbackArgs> OnGroupChatJoined = new CallbackHandler<GroupChatJoinedCallbackArgs>();

	public CallbackHandler<ChatSessionMemberCallbackArgs> OnChatSessionMember = new CallbackHandler<ChatSessionMemberCallbackArgs>();

	public CallbackHandler<ChatSessionMemberCallbackArgs> OnGroupSessionMember = new CallbackHandler<ChatSessionMemberCallbackArgs>();

	public CallbackHandler<ChatCallbackArgs> OnChat = new CallbackHandler<ChatCallbackArgs>();

	public CallbackHandler<InstantMessageCallbackArgs> OnInstantMessage = new CallbackHandler<InstantMessageCallbackArgs>();

	public CallbackHandler<TeleportCallbackArgs> OnTeleport = new CallbackHandler<TeleportCallbackArgs>();

	public CallbackHandler<TeleportLureCallbackArgs> OnTeleportLure = new CallbackHandler<TeleportLureCallbackArgs>();

	public CallbackHandler<RegionCrossedCallbackArgs> OnRegionCrossed = new CallbackHandler<RegionCrossedCallbackArgs>();

	public CallbackHandler<BalanceCallbackArgs> OnBalanceUpdated = new CallbackHandler<BalanceCallbackArgs>();

	public CallbackHandler<SetDisplayNameReplyCallbackArgs> OnSetDisplayNameReply = new CallbackHandler<SetDisplayNameReplyCallbackArgs>();

	public CallbackHandler<ScriptControlChangeCallbackArgs> OnScriptControlChange = new CallbackHandler<ScriptControlChangeCallbackArgs>();

	public CallbackHandler<ScriptSensorReplyCallbackArgs> OnScriptSensorReply = new CallbackHandler<ScriptSensorReplyCallbackArgs>();

	public CallbackHandler<AlertMessageCallbackArgs> OnAlertMessage = new CallbackHandler<AlertMessageCallbackArgs>();

	public CallbackHandler<GenericMessageCallbackArgs> OnGenericMessage = new CallbackHandler<GenericMessageCallbackArgs>();

	public CallbackHandler<CameraConstraintCallbackArgs> OnCameraConstraint = new CallbackHandler<CameraConstraintCallbackArgs>();

	public CallbackHandler<MeanCollisionCallbackArgs> OnMeanCollision = new CallbackHandler<MeanCollisionCallbackArgs>();

	public CallbackHandler<AvatarSitResponseCallbackArgs> OnAvatarSitResponse = new CallbackHandler<AvatarSitResponseCallbackArgs>();

	public CallbackHandler<AnimationsChangedCallbackArgs> OnAnimationsChanged = new CallbackHandler<AnimationsChangedCallbackArgs>();

	public CallbackHandler<AgentDataReplyCallbackArgs> OnAgentData = new CallbackHandler<AgentDataReplyCallbackArgs>();

	public CallbackHandler<MoneyBalanceReplyCallbackArgs> OnMoneyBalanceReply = new CallbackHandler<MoneyBalanceReplyCallbackArgs>();

	public CallbackHandler<CallbackArgs> OnMuteListUpdated = new CallbackHandler<CallbackArgs>();

	private UUID agentID;
	// A temporary UUID assigned to this session, used for secure transactions
	private UUID sessionID;
	private UUID secureSessionID;
	private String startLocation = Helpers.EmptyString;
	private String agentAccess = Helpers.EmptyString;
	// Position avatar client will goto when login to 'home' or during teleport
	// request to 'home' region.
	private long homeRegion;
	private Vector3 homePosition;
	private Vector3 homeLookAt;
	private Vector3 lookAt;

	private void setHomePosRegion(long region, Vector3 pos) {
		homeRegion = region;
		homePosition = pos;
	}

	private String firstName = Helpers.EmptyString;
	private String lastName = Helpers.EmptyString;
	private String fullName;
	private TimeoutEvent<TeleportStatus> teleportTimeout;

	private int heightWidthGenCounter;

	private HashMap<UUID, AssetGesture> gestureCache = new HashMap<UUID, AssetGesture>();

	private boolean isBusy;

	private float health;
	private int balance;
	private boolean firstBalance;
	private UUID activeGroup;
	private long activeGroupPowers;

	// Your (client) Avatar ID, local to Region/sim
	private int localID;
	// Current position of avatar
	private Vector3 relativePosition;
	// Current rotation of avatar
	private Quaternion relativeRotation = Quaternion.Identity;
	private Vector4 collisionPlane;
	private Vector3 velocity;
	private Vector3 acceleration;
	private Vector3 angularVelocity;
	private long sittingOn;

	// Various abilities and preferences sent by the grid
	public AgentStateUpdateMessage AgentStateStatus;

	/*
	 * Your (client) avatars <see cref="UUID"/> "client", "agent", and "avatar" all
	 * represent the same thing
	 */
	public final UUID getAgentID() {
		return agentID;
	}

	/*
	 * Temporary {@link UUID} assigned to this session, used for verifying our
	 * identity in packets
	 */
	public final UUID getSessionID() {
		return sessionID;
	}

	/* Shared secret {@link UUID} that is never sent over the wire */
	public final UUID getSecureSessionID() {
		return secureSessionID;
	}

	/* Your (client) avatar ID, local to the current region/sim */
	public final int getLocalID() {
		return localID;
	}

	// not public
	final void setLocalID(int value) {
		localID = value;
	}

	/*
	 * Where the avatar started at login. Can be "last", "home" or a login {@link
	 * T:OpenMetaverse.URI}
	 */
	public final String getStartLocation() {
		return startLocation;
	}

	/* The access level of this agent, usually M, PG, or A */
	public final String getAgentAccess() {
		return agentAccess;
	}

	/* The CollisionPlane of Agent */
	public final Vector4 getCollisionPlane() {
		return collisionPlane;
	}

	// not public
	final void setCollisionPlane(Vector4 value) {
		collisionPlane = value;
	}

	/* An {@link Vector3} representing the velocity of our agent */
	public final Vector3 getVelocity() {
		return velocity;
	}

	final void setVelocity(Vector3 value) {
		velocity = value;
	}

	/* An {@link Vector3} representing the acceleration of our agent */
	public final Vector3 getAcceleration() {
		return acceleration;
	}

	// not public
	final void setAcceleration(Vector3 value) {
		acceleration = value;
	}

	/*
	 * A {@link Vector3} which specifies the angular speed, and axis about which an
	 * Avatar is rotating.
	 */
	public final Vector3 getAngularVelocity() {
		return angularVelocity;
	}

	// not public
	final void setAngularVelocity(Vector3 value) {
		angularVelocity = value;
	}

	public final Vector3d getGlobalHomePosition() {
		if (homePosition != null) {
			return Helpers.RegionHandleToGlobalPos(homeRegion, homePosition);
		}
		return null;
	}

	/*
	 * Position avatar client will goto when login to 'home' or during teleport
	 * request to 'home' region.
	 */
	public Vector3 getHomePosition() {
		return homePosition;
	}

	/* LookAt point saved/restored with HomePosition */
	public Vector3 getHomeLookAt() {
		return homeLookAt;
	}

	/* Avatar First Name (i.e. Philip) */
	public final String getFirstName() {
		return firstName;
	}

	/* Avatar Last Name (i.e. Linden) */
	public final String getLastName() {
		return lastName;
	}

	public final Vector3 getLookAt() {
		return lookAt;
	}

	/* Avatar Full Name (i.e. Philip Linden) */
	public final String getName() {
		// This is a fairly common request, so assume the name doesn't change
		// mid-session and cache the result
		if (fullName == null || fullName.length() < 2) {
			fullName = String.format("%s %s", firstName, lastName);
		}
		return fullName;
	}

	/* Gets the health of the agent */
	public final float getHealth() {
		return health;
	}

	/*
	 * Gets the current balance of the agent
	 */
	public final int getBalance() {
		return balance;
	}

	/*
	 * Gets the busy status of the agent
	 */
	public final boolean getIsBusy() {
		return isBusy;
	}

	public final void setIsBusy(boolean value) {
		isBusy = value;
	}

	/*
	 * Gets the local ID of the prim the agent is sitting on, zero if the avatar is
	 * not currently sitting
	 */
	public final long getSittingOn() {
		return sittingOn;
	}

	// not public
	final void setSittingOn(long value) {
		sittingOn = value;
	}

	/* Gets the {@link UUID} of the agents active group. */
	public final UUID getActiveGroup() {
		return activeGroup;
	}

	/* Gets the Agents powers in the currently active group */
	public final long getActiveGroupPowers() {
		return activeGroupPowers;
	}

	/*
	 * Current position of the agent as a relative offset from the simulator, or the
	 * parent object if we are sitting on something
	 */
	public final Vector3 getRelativePosition() {
		return relativePosition;
	}

	public final void setRelativePosition(Vector3 value) {
		relativePosition = value;
	}

	/*
	 * Current rotation of the agent as a relative rotation from the simulator, or
	 * the parent object if we are sitting on something
	 */
	public final Quaternion getRelativeRotation() {
		return relativeRotation;
	}

	public final void setRelativeRotation(Quaternion value) {
		relativeRotation = value;
	}

	/* Current position of the agent in the simulator */
	public final Vector3 getAgentPosition() {
		// simple case, agent not seated
		if (sittingOn == 0) {
			return relativePosition;
		}

		// a bit more complicatated, agent sitting on a prim
		Primitive p, t;
		Vector3 fullPosition = relativePosition;

		SimulatorManager sim = _Client.Network.getCurrentSim();
		HashMap<Integer, Primitive> primitives = sim.getObjectsPrimitives();
		synchronized (primitives) {
			p = primitives.get(sittingOn);
			if (p != null) {
				fullPosition.add(Vector3.add(p.position, Vector3.multiply(relativePosition, p.rotation)));
			}
		}

		// go up the hiearchy trying to find the root prim
		while (p != null && p.parentID != 0) {
			synchronized (primitives) {
				t = sim.getObjectsAvatars().get(p.parentID);
				if (t != null) {
					fullPosition.add(t.position);
					t = p;
				} else {
					p = primitives.get(p.parentID);
					if (p != null) {
						fullPosition.add(p.position);
					}
				}
			}
		}

		if (p != null) // we found the root prim
		{
			return fullPosition;
		}

		// Didn't find the seat's root prim, try returning coarse loaction
		if (sim.getAvatarPositions().containsKey(agentID)) {
			return sim.getAvatarPositions().get(agentID);
		}

		logger.warn(GridClient.Log("Failed to determine agents sim position", _Client));
		return relativePosition;
	}

	/**
	 * A {@link Quaternion} representing the agents current rotation
	 */
	public final Quaternion getAgentRotation() {
		if (sittingOn != 0) {
			Primitive parent;
			if (_Client.Network.getCurrentSim() != null) {
				synchronized (_Client.Network.getCurrentSim().getObjectsPrimitives()) {
					if (_Client.Network.getCurrentSim().getObjectsPrimitives().containsKey(sittingOn)) {
						parent = _Client.Network.getCurrentSim().getObjectsPrimitives().get(sittingOn);
						return Quaternion.multiply(relativeRotation, parent.rotation);
					}
				}
			}

			logger.warn(GridClient.Log("Currently sitting on object " + sittingOn
					+ " which is not tracked, SimRotation will be inaccurate", _Client));
			return relativeRotation;

		}
		return relativeRotation;
	}

	/**
	 * Returns the global grid position of the avatar
	 */
	public final Vector3d getGlobalPosition() {
		if (_Client.Network.getCurrentSim() != null) {
			return Helpers.RegionHandleToGlobalPos(_Client.getCurrentRegionHandle(), getAgentPosition());
		}
		return Vector3d.Zero;
	}

	/* Reference to the GridClient instance */
	private final GridClient _Client;
	/* Used for movement and camera tracking */
	private final AgentMovement _Movement;

	public AgentMovement getMovement() {
		return _Movement;
	}

	private ExecutorService _ThreadPool;

	/*
	 * Currently playing animations for the agent. Can be used to check the current
	 * movement status such as walking, hovering, aiming, etc. by checking against
	 * system animations found in the Animations class
	 */
	public HashMap<UUID, Integer> SignaledAnimations = new HashMap<UUID, Integer>();
	// Dictionary containing current Group Chat sessions and members
	public HashMap<UUID, ArrayList<ChatSessionMember>> GroupChatSessions = new HashMap<UUID, ArrayList<ChatSessionMember>>();
	// Dictionary containing mute list keyed on mute name and key
	private HashMap<String, MuteEntry> MuteList = new HashMap<String, MuteEntry>();

	private HashMap<UUID, UUID> ActiveGestures = new HashMap<UUID, UUID>();

	/**
	 * Finds if a MuteEntry exists by using a predicate function that is passed in
	 * as object parameter
	 *
	 * @param predicate
	 *            The Predicate object that implements the test
	 * @return true if a matching MuteEntry was found, or false otherwise
	 */
	public boolean findMuteEntry(Predicate<MuteEntry> predicate) {
		synchronized (MuteList) {
			for (MuteEntry me : MuteList.values()) {
				if (predicate.evaluate(me))
					return true;
			}
		}
		return false;
	}

	private class Network_OnLoginProgress implements Callback<LoginProgressCallbackArgs> {
		@Override
		public boolean callback(LoginProgressCallbackArgs e) {
			if (e.getStatus() == LoginStatus.ConnectingToSim) {
				_Movement.ResetTimer();

				LoginResponseData reply = e.getReply();
				agentID = reply.AgentID;
				sessionID = reply.SessionID;
				secureSessionID = reply.SecureSessionID;
				firstName = reply.FirstName;
				lastName = reply.LastName;
				startLocation = reply.StartLocation;
				agentAccess = reply.AgentAccessMax;
				_Movement.Camera.LookDirection(reply.LookAt);
				homeRegion = reply.HomeRegion;
				homePosition = reply.HomePosition;
				homeLookAt = reply.HomeLookAt;
				lookAt = reply.LookAt;

				for (Entry<UUID, UUID> gesture : reply.Gestures.entrySet()) {
					ActiveGestures.put(gesture.getKey(), gesture.getValue());
				}
			} else if (e.getStatus() == LoginStatus.Success) {
				try {
					RequestBalance();
				} catch (Exception ex) {
					logger.error(GridClient.Log("", _Client), ex);
				}
			}
			return false;
		}
	}

	private class Network_OnDisconnected implements Callback<DisconnectedCallbackArgs> {
		@Override
		public boolean callback(DisconnectedCallbackArgs e) {
			// Null out the cached fullName since it can change after logging
			// in again (with a different account name or different login
			// server but using the same GridClient object
			fullName = null;
			return true;
		}
	}

	private boolean sendAgentUpdates;

	private class SettingsUpdate implements Callback<SettingsUpdateCallbackArgs> {
		@Override
		public boolean callback(SettingsUpdateCallbackArgs params) {
			String key = params.getName();
			if (key == null) {
				sendAgentUpdates = _Client.Settings.getBool(LibSettings.SEND_AGENT_UPDATES);
			} else if (key.equals(LibSettings.SEND_AGENT_UPDATES)) {
				sendAgentUpdates = params.getValue().AsBoolean();
			}
			return false;
		}
	}

	/**
	 * 'CallBack Central' - Setup callbacks for packets related to our avatar
	 *
	 * @param client
	 */
	public AgentManager(GridClient client) {
		_Client = client;

		_Client.Settings.onSettingsUpdate.add(new SettingsUpdate());
		sendAgentUpdates = _Client.Settings.getBool(LibSettings.SEND_AGENT_UPDATES);

		_Movement = new AgentMovement(client);
		teleportTimeout = new TimeoutEvent<TeleportStatus>();
		_ThreadPool = Executors.newCachedThreadPool();

		homePosition = null;
		firstBalance = true;

		_Client.Network.OnDisconnected.add(new Network_OnDisconnected(), true);
		// Login
		_Client.Login.OnLoginProgress.add(new Network_OnLoginProgress(), false);

		// Coarse location callback
		_Client.Network.RegisterCallback(PacketType.CoarseLocationUpdate, this);

		// Teleport callbacks
		_Client.Network.RegisterCallback(PacketType.TeleportStart, this);
		_Client.Network.RegisterCallback(PacketType.TeleportProgress, this);
		_Client.Network.RegisterCallback(PacketType.TeleportFailed, this);
		_Client.Network.RegisterCallback(PacketType.TeleportFinish, this);
		_Client.Network.RegisterCallback(PacketType.TeleportCancel, this);
		_Client.Network.RegisterCallback(PacketType.TeleportLocal, this);

		// these come in via the CapsEventQueue
		_Client.Network.RegisterCallback(CapsEventType.TeleportFailed, this);
		_Client.Network.RegisterCallback(CapsEventType.TeleportFinish, this);

		// Instant Message callback
		_Client.Network.RegisterCallback(PacketType.ImprovedInstantMessage, this);
		// Chat callback
		_Client.Network.RegisterCallback(PacketType.ChatFromSimulator, this);

		_Client.Network.RegisterCallback(PacketType.MuteListUpdate, this);
		// Script dialog callback
		_Client.Network.RegisterCallback(PacketType.ScriptDialog, this);
		// Script question callback
		_Client.Network.RegisterCallback(PacketType.ScriptQuestion, this);
		// Script URL callback
		_Client.Network.RegisterCallback(PacketType.LoadURL, this);
		// script control change messages, ie: when an in-world LSL script wants
		// to take control of your agent.
		_Client.Network.RegisterCallback(PacketType.ScriptControlChange, this);

		_Client.Network.RegisterCallback(PacketType.ScriptSensorReply, this);
		// Movement complete callback
		_Client.Network.RegisterCallback(PacketType.AgentMovementComplete, this);
		// Health callback
		_Client.Network.RegisterCallback(PacketType.HealthMessage, this);
		// Money callback
		_Client.Network.RegisterCallback(PacketType.MoneyBalanceReply, this);
		// Agent update callback
		_Client.Network.RegisterCallback(PacketType.AgentDataUpdate, this);
		// Animation callback
		_Client.Network.RegisterCallback(PacketType.AvatarAnimation, this);

		_Client.Network.RegisterCallback(PacketType.AvatarSitResponse, this);
		// Object colliding into our agent callback
		_Client.Network.RegisterCallback(PacketType.MeanCollisionAlert, this);
		// Region Crossing
		_Client.Network.RegisterCallback(PacketType.CrossedRegion, this);
		_Client.Network.RegisterCallback(CapsEventType.CrossedRegion, this);
		// CAPS callbacks
		_Client.Network.RegisterCallback(CapsEventType.EstablishAgentCommunication, this);
		_Client.Network.RegisterCallback(CapsEventType.SetDisplayNameReply, this);
		_Client.Network.RegisterCallback(CapsEventType.AgentStateUpdate, this);
		// Incoming Group Chat
		_Client.Network.RegisterCallback(CapsEventType.ChatterBoxInvitation, this);
		// Outgoing Group Chat Reply
		_Client.Network.RegisterCallback(CapsEventType.ChatterBoxSessionEventReply, this);
		_Client.Network.RegisterCallback(CapsEventType.ChatterBoxSessionStartReply, this);
		_Client.Network.RegisterCallback(CapsEventType.ChatterBoxSessionAgentListUpdates, this);
		// Alert Messages
		_Client.Network.RegisterCallback(PacketType.AlertMessage, this);
		// Generic Messages
		_Client.Network.RegisterCallback(PacketType.GenericMessage, this);
		// Camera Constraint (probably needs to move to AgentManagerCamera TODO:
		_Client.Network.RegisterCallback(PacketType.CameraConstraint, this);
	}

	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) throws Exception {
		switch (message.getType()) {
		case TeleportFailed:
			HandleTeleportFailed(message, simulator);
			break;
		case TeleportFinish:
			HandleTeleportFinish(message, simulator);
			break;
		case CrossedRegion:
			HandleCrossedRegion(message, simulator);
			break;
		case EstablishAgentCommunication:
			HandleEstablishAgentComm(message, simulator);
			break;
		case ChatterBoxInvitation:
			HandleChatterBoxInvitation(message, simulator);
			break;
		case ChatterBoxSessionEventReply:
			HandleChatterBoxSessionEventReply(message, simulator);
			break;
		case ChatterBoxSessionStartReply:
			HandleChatterBoxSessionStartReply(message, simulator);
			break;
		case ChatterBoxSessionAgentListUpdates:
			HandleChatterBoxSessionAgentListUpdates(message, simulator);
			break;
		case SetDisplayNameReply:
			HandleSetDisplayNameReply(message, simulator);
			break;
		case AgentStateUpdate:
			HandleAgentStateUpdate(message, simulator);
			break;
		default:
			logger.warn(GridClient.Log("AgentManager: Unhandled message " + message.getType().toString(), _Client));
		}
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case TeleportStart:
		case TeleportProgress:
		case TeleportFailed:
		case TeleportFinish:
		case TeleportCancel:
		case TeleportLocal:
			HandleTeleport(packet, simulator);
			break;
		case MoneyBalanceReply:
			HandleMoneyBalanceReply(packet, simulator);
			break;
		case ImprovedInstantMessage:
			HandleInstantMessage(packet, simulator);
			break;
		case ChatFromSimulator:
			HandleChat(packet, simulator);
			break;
		case MuteListUpdate:
			HandleMuteListUpdate(packet, simulator);
			break;
		case CoarseLocationUpdate:
			HandleCoarseLocation(packet, simulator);
			break;
		case MeanCollisionAlert:
			HandleMeanCollisionAlert(packet, simulator);
			break;
		case AgentMovementComplete:
			HandleAgentMovementComplete(packet, simulator);
			break;
		case AgentDataUpdate:
			HandleAgentDataUpdate(packet, simulator);
			break;
		case CrossedRegion:
			HandleCrossedRegion(packet, simulator);
			break;
		case HealthMessage:
			HandleHealthMessage(packet, simulator);
			break;
		case ScriptControlChange:
			HandleScriptControlChange(packet, simulator);
			break;
		case ScriptSensorReply:
			HandleScriptSensorReply(packet, simulator);
			break;
		case GenericMessage:
			HandleGenericMessage(packet, simulator);
			break;
		case AlertMessage:
			HandleAlertMessage(packet, simulator);
			break;
		case AvatarAnimation:
			HandleAvatarAnimation(packet, simulator);
			break;
		case AvatarSitResponse:
			HandleAvatarSitResponse(packet, simulator);
			break;
		case CameraConstraint:
			HandleCameraConstraint(packet, simulator);
			break;
		default:
			logger.warn(GridClient.Log("AgentManager: Unhandled packet " + packet.getType().toString(), _Client));
		}
	}

	// /#region Chat and instant messages

	/**
	 * Send a text message from the Agent to the Simulator
	 *
	 * @param message
	 *            A <see cref="string"/> containing the message
	 * @param channel
	 *            The channel to send the message on, 0 is the public channel.
	 *            Channels above 0 can be used however only scripts listening on the
	 *            specified channel will see the message
	 * @param type
	 *            Denotes the type of message being sent, shout, whisper, etc.
	 */
	public void Chat(String message, int channel, ChatType type) throws Exception {
		ChatFromViewerPacket chat = new ChatFromViewerPacket();
		chat.AgentData.AgentID = this.agentID;
		chat.AgentData.SessionID = this.sessionID;
		chat.ChatData.Channel = channel;
		chat.ChatData.setMessage(Helpers.StringToField(message));
		chat.ChatData.Type = (byte) type.getValue();

		_Client.Network.sendPacket(chat);
	}

	/**
	 * Request any instant messages sent while the client was offline to be resent.
	 *
	 * @throws Exception
	 */
	public final void RetrieveInstantMessages() throws Exception {
		RetrieveInstantMessagesPacket p = new RetrieveInstantMessagesPacket();
		p.AgentData.AgentID = _Client.Self.getAgentID();
		p.AgentData.SessionID = _Client.Self.getSessionID();
		_Client.Network.sendPacket(p);
	}

	/**
	 * Send an Instant Message to another Avatar
	 *
	 * @param target
	 *            The recipients <see cref="UUID"/>
	 * @param message
	 *            A <see cref="string"/> containing the message to send
	 */
	public void InstantMessage(UUID target, String message) throws Exception {
		InstantMessage(getName(), target, message, UUID.Zero, InstantMessageDialog.MessageFromAgent,
				InstantMessageOnline.Online, null, null, 0, null);
	}

	/**
	 * Send an Instant Message to an existing group chat or conference chat
	 *
	 * @param target
	 *            The recipients <see cref="UUID"/>
	 * @param message
	 *            A <see cref="string"/> containing the message to send
	 * @param imSessionID
	 *            IM session ID (to differentiate between IM windows)
	 */
	public void InstantMessage(UUID target, String message, UUID imSessionID) throws Exception {
		InstantMessage(getName(), target, message, imSessionID, InstantMessageDialog.MessageFromAgent,
				InstantMessageOnline.Online, null, null, 0, null);
	}

	public final void InstantMessage(String fromName, UUID target, String message, UUID imSessionID,
			InstantMessageDialog dialog, InstantMessageOnline offline) throws Exception {
		InstantMessage(getName(), target, message, imSessionID, dialog, offline, null, null, 0, null);
	}

	/**
	 * Send an Instant Message
	 *
	 * @param fromName
	 *            The name this IM will show up as being from
	 * @param target
	 *            Key of Avatar
	 * @param message
	 *            Text message being sent
	 * @param imSessionID
	 *            IM session ID (to differentiate between IM windows)
	 * @param conferenceIDs
	 *            IDs of sessions for a conference
	 */
	public void InstantMessage(String fromName, UUID target, String message, UUID imSessionID, UUID[] conferenceIDs)
			throws Exception {
		byte[] binaryBucket = null;

		if (conferenceIDs != null && conferenceIDs.length > 0) {
			binaryBucket = new byte[16 * conferenceIDs.length];

			for (int i = 0; i < conferenceIDs.length; ++i) {
				conferenceIDs[i].toBytes(binaryBucket, i * 16);
			}
		}

		// Send the message
		InstantMessage(fromName, target, message, imSessionID, InstantMessageDialog.MessageFromAgent,
				InstantMessageOnline.Online, null, null, 0, binaryBucket);
	}

	/**
	 * Send an Instant Message
	 *
	 * @param fromName
	 *            The name this IM will show up as being from
	 * @param target
	 *            Key of Avatar
	 * @param message
	 *            Text message being sent
	 * @param imSessionID
	 *            IM session ID (to differentiate between IM windows)
	 * @param dialog
	 *            Type of instant message to send
	 * @param offline
	 *            Whether to IM offline avatars as well
	 * @param position
	 *            Senders Position, if null use the current agent location
	 * @param regionID
	 *            RegionID Sender is In, if null use the current simulator ID
	 * @param timestamp
	 *            timestamp of message or 0
	 * @param binaryBucket
	 *            Packed binary data that is specific to the dialog type
	 *
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public final void InstantMessage(String fromName, UUID target, String message, UUID imSessionID,
			InstantMessageDialog dialog, InstantMessageOnline offline, Vector3 position, UUID regionID, long timestamp,
			byte[] binaryBucket) throws Exception {
		if (!target.equals(UUID.Zero)) {
			ImprovedInstantMessagePacket im = new ImprovedInstantMessagePacket();

			if (imSessionID == null || imSessionID.equals(UUID.Zero) || imSessionID.equals(getAgentID())) {
				imSessionID = getAgentID().equals(target) ? getAgentID() : UUID.XOr(target, getAgentID());
			}

			im.AgentData.AgentID = getAgentID();
			im.AgentData.SessionID = getSessionID();

			im.MessageBlock.FromGroup = false;
			im.MessageBlock.ToAgentID = target;
			im.MessageBlock.setFromAgentName(Helpers.StringToBytes(fromName));
			im.MessageBlock.setMessage(Helpers.StringToBytes(message));
			im.MessageBlock.Offline = offline.getValue();
			im.MessageBlock.Dialog = dialog.getValue();
			im.MessageBlock.ID = imSessionID;
			im.MessageBlock.ParentEstateID = 0;

			im.MessageBlock.Timestamp = (int) ((timestamp % 1000) & 0xFFFFFFFF);

			// These fields are mandatory, even if we don't have valid values
			// Allow region id to be correctly set by caller or fetched from _Client.
			if (regionID == null)
				regionID = _Client.Network.getCurrentSim().RegionID;
			im.MessageBlock.RegionID = regionID;

			// for them
			if (position == null)
				position = getAgentPosition();
			im.MessageBlock.Position = position;

			if (binaryBucket != null) {
				im.MessageBlock.setBinaryBucket(binaryBucket);
			} else {
				im.MessageBlock.setBinaryBucket(Helpers.EmptyBytes);
			}

			// Send the message
			_Client.Network.sendPacket(im);
		} else {
			logger.error(
					GridClient.Log(String.format("Suppressing instant message \"%s\" to UUID.Zero", message), _Client));
		}
	}

	/**
	 * Send an Instant Message to a group
	 *
	 * @param groupID
	 *            {@link UUID} of the group to send message to
	 * @param message
	 *            Text Message being sent.
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public final void InstantMessageGroup(UUID groupID, String message) throws UnsupportedEncodingException, Exception {
		InstantMessageGroup(getName(), groupID, message);
	}

	/**
	 * Send an Instant Message to a group the agent is a member of
	 *
	 * @param fromName
	 *            The name this IM will show up as being from
	 * @param groupID
	 *            {@link UUID} of the group to send message to
	 * @param message
	 *            Text message being sent
	 * @throws Exception
	 */
	public final void InstantMessageGroup(String fromName, UUID groupID, String message) throws Exception {
		synchronized (GroupChatSessions) {
			if (GroupChatSessions.containsKey(groupID)) {
				InstantMessage(fromName, groupID, message, groupID, InstantMessageDialog.SessionSend,
						InstantMessageOnline.Online);
			} else {
				throw new Exception(
						"No Active group chat session appears to exist, use RequestJoinGroupChat() to join one");
			}
		}
	}

	/**
	 * Send a typing status update
	 *
	 * @param otherID
	 *            {@link UUID} of the group to send the status update to
	 * @param sessionID
	 *            {@link UUID} of the communication session this status is for
	 * @param typing
	 *            typing status to send
	 * @throws Exception
	 */
	public final void SendTypingState(UUID otherID, UUID sessionID, boolean typing) throws Exception {
		InstantMessage(getName(), otherID, "typing", sessionID,
				typing ? InstantMessageDialog.StartTyping : InstantMessageDialog.StopTyping,
				InstantMessageOnline.Online);
	}

	/**
	 * Send a request to join a group chat session
	 *
	 * @param groupID
	 *            {@link UUID} of Group to leave
	 */
	public final void RequestJoinGroupChat(UUID groupID) throws Exception {
		InstantMessage(getName(), groupID, Helpers.EmptyString, groupID, InstantMessageDialog.SessionGroupStart,
				InstantMessageOnline.Online);
	}

	/**
	 * Exit a group chat session. This will stop further Group chat messages from
	 * being sent until session is rejoined.
	 *
	 * @param groupID
	 *            {@link UUID} of Group chat session to leave
	 * @throws Exception
	 */
	public final void RequestLeaveGroupChat(UUID groupID) throws Exception {
		InstantMessage(getName(), groupID, Helpers.EmptyString, groupID, InstantMessageDialog.SessionDrop,
				InstantMessageOnline.Online);

		synchronized (GroupChatSessions) {
			if (GroupChatSessions.containsKey(groupID)) {
				GroupChatSessions.remove(groupID);
			}
		}
	}

	/**
	 * Reply to script dialog questions.
	 *
	 * @param channel
	 *            Channel initial request came on
	 * @param buttonIndex
	 *            Index of button you're "clicking"
	 * @param buttonlabel
	 *            Label of button you're "clicking"
	 * @param objectID
	 *            {@link UUID} of Object that sent the dialog request
	 *            {@link OnScriptDialog}
	 *
	 * @throws Exception
	 */
	public final void ReplyToScriptDialog(int channel, int buttonIndex, String buttonlabel, UUID objectID)
			throws Exception {
		ScriptDialogReplyPacket reply = new ScriptDialogReplyPacket();

		reply.AgentData.AgentID = _Client.Self.getAgentID();
		reply.AgentData.SessionID = _Client.Self.getSessionID();

		reply.Data.ButtonIndex = buttonIndex;
		reply.Data.setButtonLabel(Helpers.StringToBytes(buttonlabel));
		reply.Data.ChatChannel = channel;
		reply.Data.ObjectID = objectID;

		_Client.Network.sendPacket(reply);
	}

	/**
	 * Accept invite for a chatterbox session
	 *
	 * @param session_id
	 *            {@link UUID} of session to accept invite to
	 * @throws Exception
	 */
	public final void ChatterBoxAcceptInvite(UUID session_id) throws Exception {
		URI uri = _Client.Network.getCapabilityURI(CapsEventType.ChatSessionRequest.toString());
		if (uri != null) {
			ChatSessionAcceptInvitation acceptInvite = _Client.Messages.new ChatSessionAcceptInvitation();
			acceptInvite.sessionID = session_id;
			new CapsClient(_Client, CapsEventType.ChatSessionAcceptInvitation.toString()).executeHttpPost(uri,
					acceptInvite, null, _Client.Settings.CAPS_TIMEOUT);

			synchronized (GroupChatSessions) {
				if (!GroupChatSessions.containsKey(session_id)) {
					GroupChatSessions.put(session_id, new ArrayList<ChatSessionMember>());
				}
			}
		} else {
			throw new Exception("ChatSessionRequest capability is not currently available");
		}

	}

	/**
	 * Start a friends conference
	 *
	 * @param participants
	 *            {@link UUID} List of UUIDs to start a conference with
	 * @param tmp_session_id
	 *            the temporary session ID returned in the {see
	 *            cref="OnJoinedGroupChat" callback
	 * @throws Exception
	 */
	public final void StartIMConference(UUID[] participants, UUID tmp_session_id) throws Exception {
		URI url = _Client.Network.getCapabilityURI(CapsEventType.ChatSessionRequest.toString());
		if (url != null) {
			ChatSessionRequestStartConference startConference = _Client.Messages.new ChatSessionRequestStartConference();

			startConference.agentsBlock = new UUID[participants.length];
			for (int i = 0; i < participants.length; i++) {
				startConference.agentsBlock[i] = participants[i];
			}
			startConference.sessionID = tmp_session_id;
			new CapsClient(_Client, CapsEventType.ChatSessionRequestStartConference.toString()).executeHttpPost(url,
					startConference, null, _Client.Settings.CAPS_TIMEOUT);
		} else {
			throw new Exception("ChatSessionRequest capability is not currently available");
		}
	}

	// #endregion

	// /#region Viewer Effects

	/**
	 * Start a particle stream between an agent and an object
	 *
	 * @param sourceAvatar
	 *            {@link UUID} Key of the source agent
	 * @param targetObject
	 *            {@link UUID} Key of the target object
	 * @param globalOffset
	 * @param type
	 *            The type from the {@link T:PointAtType} enum
	 * @param effectID
	 *            A unique {@link UUID} for this effect
	 * @throws Exception
	 */
	public final void PointAtEffect(UUID sourceAvatar, UUID targetObject, Vector3d globalOffset, PointAtType type,
			UUID effectID) throws Exception {
		ViewerEffectPacket effect = new ViewerEffectPacket();

		effect.AgentData.AgentID = _Client.Self.getAgentID();
		effect.AgentData.SessionID = _Client.Self.getSessionID();

		effect.Effect = new ViewerEffectPacket.EffectBlock[1];
		effect.Effect[0] = effect.new EffectBlock();
		effect.Effect[0].AgentID = _Client.Self.getAgentID();
		effect.Effect[0].Color = new byte[4];
		effect.Effect[0].Duration = (type == PointAtType.Clear) ? 0.0f : Float.MAX_VALUE / 4.0f;
		effect.Effect[0].ID = effectID;
		effect.Effect[0].Type = EffectType.PointAt.getValue();

		byte[] typeData = new byte[57];
		if (!sourceAvatar.equals(UUID.Zero)) {
			sourceAvatar.toBytes(typeData, 0);
		}
		if (!targetObject.equals(UUID.Zero)) {
			targetObject.toBytes(typeData, 16);
		}
		globalOffset.toBytes(typeData, 32);
		typeData[56] = type.getValue();

		effect.Effect[0].setTypeData(typeData);

		_Client.Network.sendPacket(effect);
	}

	/**
	 * Start a particle stream between an agent and an object
	 *
	 * @param sourceAvatar
	 *            {@link UUID} Key of the source agent
	 * @param targetObject
	 *            {@link UUID} Key of the target object
	 * @param globalOffset
	 *            A {@link Vector3d} representing the beams offset from the source
	 * @param type
	 *            A {@link T:PointAtType} which sets the avatars lookat animation
	 * @param effectID
	 *            {@link UUID} of the Effect
	 * @throws Exception
	 */
	public final void LookAtEffect(UUID sourceAvatar, UUID targetObject, Vector3d globalOffset, LookAtType type,
			UUID effectID) throws Exception {
		ViewerEffectPacket effect = new ViewerEffectPacket();

		effect.AgentData.AgentID = _Client.Self.getAgentID();
		effect.AgentData.SessionID = _Client.Self.getSessionID();

		float duration;

		switch (type) {
		case Clear:
			duration = 2.0f;
			break;
		case Hover:
			duration = 1.0f;
			break;
		case FreeLook:
			duration = 2.0f;
			break;
		case Idle:
			duration = 3.0f;
			break;
		case AutoListen:
		case Respond:
			duration = 4.0f;
			break;
		case None:
		case Select:
		case Focus:
		case Mouselook:
			duration = Float.MAX_VALUE / 2.0f;
			break;
		default:
			duration = 0.0f;
			break;
		}

		effect.Effect = new ViewerEffectPacket.EffectBlock[1];
		effect.Effect[0] = effect.new EffectBlock();
		effect.Effect[0].AgentID = _Client.Self.getAgentID();
		effect.Effect[0].Color = new byte[4];
		effect.Effect[0].Duration = duration;
		effect.Effect[0].ID = effectID;
		effect.Effect[0].Type = EffectType.LookAt.getValue();

		byte[] typeData = new byte[57];
		sourceAvatar.toBytes(typeData, 0);
		targetObject.toBytes(typeData, 16);
		globalOffset.toBytes(typeData, 32);
		typeData[56] = type.getValue();

		effect.Effect[0].setTypeData(typeData);

		_Client.Network.sendPacket(effect);
	}

	/**
	 * Create a particle beam between an avatar and an primitive
	 *
	 * @param sourceAvatar
	 *            The ID of source avatar
	 * @param targetObject
	 *            The ID of the target primitive
	 * @param globalOffset
	 *            global offset
	 * @param color
	 *            A <see cref="Color4"/> object containing the combined red, green,
	 *            blue and alpha color values of particle beam
	 * @param duration
	 *            a float representing the duration the parcicle beam will last
	 * @param effectID
	 *            A Unique ID for the beam {@link ViewerEffectPacket}
	 * @throws Exception
	 */
	public final void BeamEffect(UUID sourceAvatar, UUID targetObject, Vector3d globalOffset, Color4 color,
			float duration, UUID effectID) throws Exception {
		ViewerEffectPacket effect = new ViewerEffectPacket();

		effect.AgentData.AgentID = _Client.Self.getAgentID();
		effect.AgentData.SessionID = _Client.Self.getSessionID();

		effect.Effect = new ViewerEffectPacket.EffectBlock[1];
		effect.Effect[0] = effect.new EffectBlock();
		effect.Effect[0].AgentID = _Client.Self.getAgentID();
		effect.Effect[0].Color = color.getBytes();
		effect.Effect[0].Duration = duration;
		effect.Effect[0].ID = effectID;
		effect.Effect[0].Type = EffectType.Beam.getValue();

		byte[] typeData = new byte[56];
		sourceAvatar.toBytes(typeData, 0);
		targetObject.toBytes(typeData, 16);
		globalOffset.toBytes(typeData, 32);

		effect.Effect[0].setTypeData(typeData);

		_Client.Network.sendPacket(effect);
	}

	/**
	 * Create a particle swirl around a target position using a
	 * {@link ViewerEffectPacket} packet
	 *
	 * @param globalOffset
	 *            global offset
	 * @param color
	 *            A <see cref="Color4"/> object containing the combined red, green,
	 *            blue and alpha color values of particle beam
	 * @param duration
	 *            a float representing the duration the parcicle beam will last
	 * @param effectID
	 *            A Unique ID for the beam
	 * @throws Exception
	 */
	public final void SphereEffect(Vector3d globalOffset, Color4 color, float duration, UUID effectID)
			throws Exception {
		ViewerEffectPacket effect = new ViewerEffectPacket();

		effect.AgentData.AgentID = _Client.Self.getAgentID();
		effect.AgentData.SessionID = _Client.Self.getSessionID();

		effect.Effect = new ViewerEffectPacket.EffectBlock[1];
		effect.Effect[0] = effect.new EffectBlock();
		effect.Effect[0].AgentID = _Client.Self.getAgentID();
		effect.Effect[0].Color = color.getBytes();
		effect.Effect[0].Duration = duration;
		effect.Effect[0].ID = effectID;
		effect.Effect[0].Type = EffectType.Sphere.getValue();

		byte[] typeData = new byte[56];
		UUID.Zero.toBytes(typeData, 0);
		UUID.Zero.toBytes(typeData, 16);
		globalOffset.toBytes(typeData, 32);

		effect.Effect[0].setTypeData(typeData);

		_Client.Network.sendPacket(effect);
	}

	// #endregion Viewer Effects

	// #region Movement Actions

	/**
	 * Sends a request to sit on the specified object
	 *
	 * @param targetID
	 *            {@link UUID} of the object to sit on
	 * @param offset
	 *            Sit at offset
	 * @throws Exception
	 */
	public final void RequestSit(UUID targetID, Vector3 offset) throws Exception {
		AgentRequestSitPacket requestSit = new AgentRequestSitPacket();
		requestSit.AgentData.AgentID = _Client.Self.getAgentID();
		requestSit.AgentData.SessionID = _Client.Self.getSessionID();
		requestSit.TargetObject.TargetID = targetID;
		requestSit.TargetObject.Offset = offset;
		_Client.Network.sendPacket(requestSit);
	}

	/**
	 * Follows a call to {@link RequestSit} to actually sit on the object
	 *
	 * @throws Exception
	 *
	 */
	public final void Sit() throws Exception {
		AgentSitPacket sit = new AgentSitPacket();
		sit.AgentData.AgentID = _Client.Self.getAgentID();
		sit.AgentData.SessionID = _Client.Self.getSessionID();
		_Client.Network.sendPacket(sit);
	}

	/**
	 * Stands up from sitting on a prim or the ground
	 *
	 * @return true of AgentUpdate was sent
	 * @throws Exception
	 */
	public final boolean Stand() throws Exception {
		if (sendAgentUpdates) {
			_Movement.setSitOnGround(false);
			_Movement.setStandUp(true);
			_Movement.SendUpdate();
			_Movement.setStandUp(false);
			_Movement.SendUpdate();
			return true;
		}
		logger.warn(GridClient.Log("Attempted to Stand() but agent updates are disabled", _Client));
		return false;
	}

	/**
	 * Does a "ground sit" at the avatar's current position
	 *
	 * @throws Exception
	 */
	public final void SitOnGround() throws Exception {
		_Movement.setSitOnGround(true);
		_Movement.SendUpdate(true);
	}

	/**
	 * Starts or stops flying
	 *
	 * @param start
	 *            True to start flying, false to stop flying
	 * @throws Exception
	 */
	public final void Fly(boolean start) throws Exception {
		if (start) {
			_Movement.setFly(true);
		} else {
			_Movement.setFly(false);
		}
		_Movement.SendUpdate(true);
	}

	/**
	 * Starts or stops crouching
	 *
	 * @param crouching
	 *            True to start crouching, false to stop crouching
	 * @throws Exception
	 */
	public final void Crouch(boolean crouching) throws Exception {
		_Movement.setUpNeg(crouching);
		_Movement.SendUpdate(true);
	}

	/*
	 * Starts a jump (begin holding the jump key)
	 */
	public final void Jump(boolean jumping) throws Exception {
		_Movement.setUpPos(jumping);
		_Movement.setFastUp(jumping);
		_Movement.SendUpdate(true);
	}

	/**
	 * Use the autopilot sim function to move the avatar to a new position. Uses
	 * double precision to get precise movements
	 *
	 * The z value is currently not handled properly by the simulator
	 *
	 * @param globalX
	 *            Global X coordinate to move to
	 * @param globalY
	 *            Global Y coordinate to move to
	 * @param z
	 *            Z coordinate to move to
	 * @throws Exception
	 */
	public final void AutoPilot(double globalX, double globalY, double z) throws Exception {
		GenericMessagePacket autopilot = new GenericMessagePacket();

		autopilot.AgentData.AgentID = _Client.Self.getAgentID();
		autopilot.AgentData.SessionID = _Client.Self.getSessionID();
		autopilot.AgentData.TransactionID = UUID.Zero;
		autopilot.MethodData.Invoice = UUID.Zero;
		autopilot.MethodData.setMethod(Helpers.StringToBytes("autopilot"));
		autopilot.ParamList = new GenericMessagePacket.ParamListBlock[3];
		autopilot.ParamList[0] = autopilot.new ParamListBlock();
		autopilot.ParamList[0].setParameter(Helpers.StringToBytes(((Double) globalX).toString()));
		autopilot.ParamList[1] = autopilot.new ParamListBlock();
		autopilot.ParamList[1].setParameter(Helpers.StringToBytes(((Double) globalY).toString()));
		autopilot.ParamList[2] = autopilot.new ParamListBlock();
		autopilot.ParamList[2].setParameter(Helpers.StringToBytes(((Double) z).toString()));

		_Client.Network.sendPacket(autopilot);
	}

	/**
	 * Use the autopilot sim function to move the avatar to a new position
	 *
	 * The z value is currently not handled properly by the simulator
	 *
	 * @param globalX
	 *            Long integer value for the global X coordinate to move to
	 * @param globalY
	 *            Long integer value for the global Y coordinate to move to
	 * @param z
	 *            Floating-point value for the Z coordinate to move to
	 * @throws Exception
	 */
	public final void AutoPilot(long globalX, long globalY, float z) throws Exception {
		GenericMessagePacket autopilot = new GenericMessagePacket();

		autopilot.AgentData.AgentID = _Client.Self.getAgentID();
		autopilot.AgentData.SessionID = _Client.Self.getSessionID();
		autopilot.AgentData.TransactionID = UUID.Zero;
		autopilot.MethodData.Invoice = UUID.Zero;
		autopilot.MethodData.setMethod(Helpers.StringToBytes("autopilot"));
		autopilot.ParamList = new GenericMessagePacket.ParamListBlock[3];
		autopilot.ParamList[0] = autopilot.new ParamListBlock();
		autopilot.ParamList[0].setParameter(Helpers.StringToBytes(((Long) globalX).toString()));
		autopilot.ParamList[1] = autopilot.new ParamListBlock();
		autopilot.ParamList[1].setParameter(Helpers.StringToBytes(((Long) globalY).toString()));
		autopilot.ParamList[2] = autopilot.new ParamListBlock();
		autopilot.ParamList[2].setParameter(Helpers.StringToBytes(((Float) z).toString()));

		_Client.Network.sendPacket(autopilot);
	}

	/**
	 * Use the autopilot sim function to move the avatar to a new position
	 *
	 * The z value is currently not handled properly by the simulator
	 *
	 * @param localX
	 *            Integer value for the local X coordinate to move to
	 * @param localY
	 *            Integer value for the local Y coordinate to move to
	 * @param z
	 *            Floating-point value for the Z coordinate to move to
	 * @throws Exception
	 */
	public final void AutoPilotLocal(int localX, int localY, float z) throws Exception {
		int[] coord = new int[2];
		Helpers.LongToUInts(_Client.getCurrentRegionHandle(), coord);
		AutoPilot((coord[0] + localX), (coord[1] + localY), z);
	}

	/**
	 * Macro to cancel autopilot sim function Not certain if this is how it is
	 * really done
	 *
	 * @return true if control flags were set and AgentUpdate was sent to the
	 *         simulator
	 * @throws Exception
	 */
	public final boolean AutoPilotCancel() throws Exception {
		if (sendAgentUpdates) {
			_Movement.setAtPos(true);
			_Movement.SendUpdate();
			_Movement.setAtPos(false);
			_Movement.SendUpdate();
			return true;
		}
		logger.warn(GridClient.Log("Attempted to AutoPilotCancel() but agent updates are disabled", _Client));
		return false;
	}

	// #endregion Movement actions

	// #region Touch and grab

	/**
	 * Grabs an object
	 *
	 * @param objectLocalID
	 *            an integer of the objects ID within the simulator
	 * @see cref="Simulator.ObjectsPrimitives
	 * @throws Exception
	 */
	public void Grab(int objectLocalID) throws Exception {
		Grab(objectLocalID, Vector3.Zero, Vector3.Zero, Vector3.Zero, 0, Vector3.Zero, Vector3.Zero, Vector3.Zero);
	}

	/**
	 * Overload: Grab a simulated object
	 *
	 * @param objectLocalID
	 *            an unsigned integer of the objects ID within the simulator
	 * @param grabOffset
	 * @param uvCoord
	 *            The texture coordinates to grab
	 * @param stCoord
	 *            The surface coordinates to grab
	 * @param faceIndex
	 *            The face of the position to grab
	 * @param position
	 *            The region coordinates of the position to grab
	 * @param normal
	 *            The surface normal of the position to grab (A normal is a vector
	 *            perpindicular to the surface)
	 * @param binormal
	 *            The surface binormal of the position to grab (A binormal is a
	 *            vector tangen to the surface pointing along the U direction of the
	 *            tangent space
	 * @throws Exception
	 */
	public void Grab(int objectLocalID, Vector3 grabOffset, Vector3 uvCoord, Vector3 stCoord, int faceIndex,
			Vector3 position, Vector3 normal, Vector3 binormal) throws Exception {
		ObjectGrabPacket grab = new ObjectGrabPacket();

		grab.AgentData.AgentID = agentID;
		grab.AgentData.SessionID = sessionID;

		grab.ObjectData.LocalID = objectLocalID;
		grab.ObjectData.GrabOffset = grabOffset;

		grab.SurfaceInfo = new ObjectGrabPacket.SurfaceInfoBlock[1];
		grab.SurfaceInfo[0] = grab.new SurfaceInfoBlock();
		grab.SurfaceInfo[0].UVCoord = uvCoord;
		grab.SurfaceInfo[0].STCoord = stCoord;
		grab.SurfaceInfo[0].FaceIndex = faceIndex;
		grab.SurfaceInfo[0].Position = position;
		grab.SurfaceInfo[0].Normal = normal;
		grab.SurfaceInfo[0].Binormal = binormal;

		_Client.Network.sendPacket(grab);
	}

	/**
	 * Drag an object
	 *
	 * @param objectID
	 * @see cref="UUID" of the object to drag
	 * @param grabPosition
	 *            Drag target in region coordinates
	 * @throws Exception
	 */
	public void GrabUpdate(UUID objectID, Vector3 grabPosition) throws Exception {
		GrabUpdate(objectID, grabPosition, Vector3.Zero, Vector3.Zero, Vector3.Zero, 0, Vector3.Zero, Vector3.Zero,
				Vector3.Zero);
	}

	/**
	 * Overload: Drag an object
	 *
	 * @param objectID
	 * @see cref="UUID" of the object to drag
	 * @param grabPosition
	 *            Drag target in region coordinates
	 * @param grabOffset
	 * @param uvCoord
	 *            The texture coordinates to grab
	 * @param stCoord
	 *            The surface coordinates to grab
	 * @param faceIndex
	 *            The face of the position to grab
	 * @param position
	 *            The region coordinates of the position to grab
	 * @param normal
	 *            The surface normal of the position to grab (A normal is a vector
	 *            perpindicular to the surface)
	 * @param binormal
	 *            The surface binormal of the position to grab (A binormal is a
	 *            vector tangen to the surface pointing along the U direction of the
	 *            tangent space
	 * @throws Exception
	 */
	public void GrabUpdate(UUID objectID, Vector3 grabPosition, Vector3 grabOffset, Vector3 uvCoord, Vector3 stCoord,
			int faceIndex, Vector3 position, Vector3 normal, Vector3 binormal) throws Exception {
		ObjectGrabUpdatePacket grab = new ObjectGrabUpdatePacket();
		grab.AgentData.AgentID = agentID;
		grab.AgentData.SessionID = sessionID;

		grab.ObjectData.ObjectID = objectID;
		grab.ObjectData.GrabOffsetInitial = grabOffset;
		grab.ObjectData.GrabPosition = grabPosition;
		grab.ObjectData.TimeSinceLast = 0;

		grab.SurfaceInfo = new ObjectGrabUpdatePacket.SurfaceInfoBlock[1];
		grab.SurfaceInfo[0] = grab.new SurfaceInfoBlock();
		grab.SurfaceInfo[0].UVCoord = uvCoord;
		grab.SurfaceInfo[0].STCoord = stCoord;
		grab.SurfaceInfo[0].FaceIndex = faceIndex;
		grab.SurfaceInfo[0].Position = position;
		grab.SurfaceInfo[0].Normal = normal;
		grab.SurfaceInfo[0].Binormal = binormal;

		_Client.Network.sendPacket(grab);
	}

	/**
	 * Release a grabbed object
	 *
	 * @param objectLocalID">The
	 *            Objects Simulator Local ID</param>
	 * @see cref="Simulator.ObjectsPrimitives"
	 * @see cref="Grab"
	 * @see cref="GrabUpdate"
	 * @throws Exception
	 */
	public void DeGrab(int objectLocalID) throws Exception {
		DeGrab(objectLocalID, Vector3.Zero, Vector3.Zero, 0, Vector3.Zero, Vector3.Zero, Vector3.Zero);
	}

	/**
	 * Release a grabbed object
	 *
	 * @param objectLocalID
	 *            The Objects Simulator Local ID
	 * @param uvCoord
	 *            The texture coordinates to grab
	 * @param stCoord
	 *            The surface coordinates to grab
	 * @param faceIndex
	 *            The face of the position to grab
	 * @param position
	 *            The region coordinates of the position to grab
	 * @param normal
	 *            The surface normal of the position to grab (A normal is a vector
	 *            perpindicular to the surface)
	 * @param binormal
	 *            The surface binormal of the position to grab (A binormal is a
	 *            vector tangen to the surface pointing along the U direction of the
	 *            tangent space
	 * @throws Exception
	 */
	public void DeGrab(int objectLocalID, Vector3 uvCoord, Vector3 stCoord, int faceIndex, Vector3 position,
			Vector3 normal, Vector3 binormal) throws Exception {
		ObjectDeGrabPacket degrab = new ObjectDeGrabPacket();
		degrab.AgentData.AgentID = agentID;
		degrab.AgentData.SessionID = sessionID;

		degrab.LocalID = objectLocalID;

		degrab.SurfaceInfo = new ObjectDeGrabPacket.SurfaceInfoBlock[1];
		degrab.SurfaceInfo[0] = degrab.new SurfaceInfoBlock();
		degrab.SurfaceInfo[0].UVCoord = uvCoord;
		degrab.SurfaceInfo[0].STCoord = stCoord;
		degrab.SurfaceInfo[0].FaceIndex = faceIndex;
		degrab.SurfaceInfo[0].Position = position;
		degrab.SurfaceInfo[0].Normal = normal;
		degrab.SurfaceInfo[0].Binormal = binormal;

		_Client.Network.sendPacket(degrab);
	}

	/**
	 * Touches an object
	 *
	 * @param objectLocalID
	 *            an integer of the objects ID within the simulator
	 * @see cref="Simulator.ObjectsPrimitives"
	 * @throws Exception
	 */
	public void Touch(int objectLocalID) throws Exception {
		_Client.Self.Grab(objectLocalID);
		_Client.Self.DeGrab(objectLocalID);
	}
	// #endregion Touch and grab

	/**
	 * Update agent profile
	 *
	 * @param profile
	 *            <seealso cref="libomv.Avatar.AvatarProperties"/> struct containing
	 *            updated profile information
	 * @throws Exception
	 */
	public void UpdateProfile(Avatar.AvatarProperties profile) throws Exception {
		AvatarPropertiesUpdatePacket apup = new AvatarPropertiesUpdatePacket();
		apup.AgentData.AgentID = this.agentID;
		apup.AgentData.SessionID = this.sessionID;
		apup.PropertiesData.setAboutText(Helpers.StringToBytes(profile.aboutText));
		apup.PropertiesData.AllowPublish = profile.getAllowPublish();
		apup.PropertiesData.setFLAboutText(Helpers.StringToBytes(profile.firstLifeText));
		apup.PropertiesData.FLImageID = profile.firstLifeImage;
		apup.PropertiesData.ImageID = profile.profileImage;
		apup.PropertiesData.MaturePublish = profile.getMaturePublish();
		apup.PropertiesData.setProfileURL(Helpers.StringToBytes(profile.profileURL));

		_Client.Network.sendPacket(apup);
	}

	/**
	 * Update agents profile interests
	 *
	 * @param interests
	 *            selection of interests from
	 *            <seealso cref="libomv.Avatar.Interests"/> struct
	 */
	public void UpdateInterests(Avatar.Interests interests) throws Exception {
		AvatarInterestsUpdatePacket aiup = new AvatarInterestsUpdatePacket();
		aiup.AgentData.AgentID = this.agentID;
		aiup.AgentData.SessionID = this.sessionID;
		aiup.PropertiesData.setLanguagesText(Helpers.StringToBytes(interests.languagesText));
		aiup.PropertiesData.SkillsMask = interests.skillsMask;
		aiup.PropertiesData.setSkillsText(Helpers.StringToBytes(interests.skillsText));
		aiup.PropertiesData.WantToMask = interests.wantToMask;
		aiup.PropertiesData.setWantToText(Helpers.StringToBytes(interests.wantToText));

		_Client.Network.sendPacket(aiup);
	}

	/**
	 * Set the height and the width of your avatar. This is used to scale
	 *
	 * @param height
	 *            New height of the avatar
	 * @param width
	 *            >New width of the avatar
	 * @throws Exception
	 */
	public void SetHeightWidth(short height, short width) throws Exception {
		AgentHeightWidthPacket heightwidth = new AgentHeightWidthPacket();
		heightwidth.AgentData.AgentID = this.agentID;
		heightwidth.AgentData.SessionID = this.sessionID;
		heightwidth.AgentData.CircuitCode = _Client.Network.getCircuitCode();
		heightwidth.HeightWidthBlock.Height = height;
		heightwidth.HeightWidthBlock.Width = width;
		heightwidth.HeightWidthBlock.GenCounter = heightWidthGenCounter++;

		_Client.Network.sendPacket(heightwidth);
	}

	// #region Money

	/**
	 * Request the current L$ balance
	 *
	 * @throws Exception
	 */
	public void RequestBalance() throws Exception {
		MoneyBalanceRequestPacket money = new MoneyBalanceRequestPacket();
		money.AgentData.AgentID = this.agentID;
		money.AgentData.SessionID = this.sessionID;
		money.TransactionID = UUID.Zero;

		_Client.Network.sendPacket(money);
	}

	/**
	 * Give Money to destination Avatar
	 *
	 * @param target
	 *            UUID of the Target Avatar
	 * @param amount
	 *            Amount in L$
	 * @throws Exception
	 */
	public final void GiveAvatarMoney(UUID target, int amount) throws Exception {
		GiveMoney(target, amount, Helpers.EmptyString, MoneyTransactionType.Gift, TransactionFlags.None);
	}

	/**
	 * Give Money to destination Avatar
	 *
	 * @param target
	 *            UUID of the Target Avatar
	 * @param amount
	 *            Amount in L$
	 * @param description
	 *            Description that will show up in the recipients transaction
	 *            history
	 * @throws Exception
	 */
	public final void GiveAvatarMoney(UUID target, int amount, String description) throws Exception {
		GiveMoney(target, amount, description, MoneyTransactionType.Gift, TransactionFlags.None);
	}

	/**
	 * Give L$ to an object
	 *
	 * @param target
	 *            object {@link UUID} to give money to
	 * @param amount
	 *            amount of L$ to give
	 * @param objectName
	 *            name of object
	 * @throws Exception
	 */
	public final void GiveObjectMoney(UUID target, int amount, String objectName) throws Exception {
		GiveMoney(target, amount, objectName, MoneyTransactionType.PayObject, TransactionFlags.None);
	}

	/**
	 * Give L$ to a group
	 *
	 * @param target
	 *            group {@link UUID} to give money to
	 * @param amount
	 *            amount of L$ to give
	 * @throws Exception
	 */
	public final void GiveGroupMoney(UUID target, int amount) throws Exception {
		GiveMoney(target, amount, Helpers.EmptyString, MoneyTransactionType.Gift, TransactionFlags.DestGroup);
	}

	/**
	 * Give L$ to a group
	 *
	 * @param target
	 *            group {@link UUID} to give money to
	 * @param amount
	 *            amount of L$ to give
	 * @param description
	 *            description of transaction
	 * @throws Exception
	 */
	public final void GiveGroupMoney(UUID target, int amount, String description) throws Exception {
		GiveMoney(target, amount, description, MoneyTransactionType.Gift, TransactionFlags.DestGroup);
	}

	/**
	 * Pay texture/animation upload fee
	 *
	 * @throws Exception
	 *
	 */
	public final void PayUploadFee() throws Exception {
		GiveMoney(UUID.Zero, _Client.Settings.getUploadPrice(), Helpers.EmptyString, MoneyTransactionType.UploadCharge,
				TransactionFlags.None);
	}

	/**
	 * Pay texture/animation upload fee
	 *
	 * @param description
	 *            description of the transaction
	 * @throws Exception
	 */
	public final void PayUploadFee(String description) throws Exception {
		GiveMoney(UUID.Zero, _Client.Settings.getUploadPrice(), description, MoneyTransactionType.UploadCharge,
				TransactionFlags.None);
	}

	/**
	 * Give Money to destination Object or Avatar
	 *
	 * @param target
	 *            UUID of the Target Object/Avatar
	 * @param amount
	 *            Amount in L$
	 * @param description
	 *            Reason (Optional normally)
	 * @param type
	 *            The type of transaction
	 * @param flags
	 *            Transaction flags, mostly for identifying group transactions
	 * @throws Exception
	 */
	public final void GiveMoney(UUID target, int amount, String description, MoneyTransactionType type, byte flags)
			throws Exception {
		MoneyTransferRequestPacket money = new MoneyTransferRequestPacket();
		money.AgentData.AgentID = this.agentID;
		money.AgentData.SessionID = this.sessionID;
		money.MoneyData.setDescription(Helpers.StringToBytes(description));
		money.MoneyData.DestID = target;
		money.MoneyData.SourceID = this.agentID;
		money.MoneyData.TransactionType = type.getValue();
		money.MoneyData.AggregatePermInventory = 0; // This is weird, apparently
													// always set to zero though
		money.MoneyData.AggregatePermNextOwner = 0; // This is weird, apparently
													// always set to zero though
		money.MoneyData.Flags = flags;
		money.MoneyData.Amount = amount;

		_Client.Network.sendPacket(money);
	}
	// #endregion Money

	// #region Gestures
	/**
	 * Plays a gesture
	 *
	 * @param gestureID
	 *            Asset {@link UUID} of the gesture
	 */
	public final void PlayGesture(final UUID gestureID) {
		if (_Client.Assets == null)
			throw new RuntimeException("Can't play a gesture without the asset manager being instantiated.");

		// First fetch the guesture
		// TODO: implement waiting for all animations to end that were triggered
		// during playing of this guesture sequence
		Thread thread = new Thread() {
			@Override
			public void run() {
				AssetGesture gesture = null;
				synchronized (gestureCache) {
					if (gestureCache.containsKey(gestureID)) {
						gesture = gestureCache.get(gestureID);
					}
				}

				if (gesture == null) {
					final TimeoutEvent<AssetGesture> gotAsset = new TimeoutEvent<AssetGesture>();

					class AssetDownloadCallback implements Callback<AssetDownload> {
						@Override
						public boolean callback(AssetDownload transfer) {
							if (transfer.Success) {
								gotAsset.set(new AssetGesture(transfer.ItemID, transfer.AssetData));
							} else {
								gotAsset.set(null);
							}
							return true;
						}
					}

					try {
						_Client.Assets.RequestAsset(gestureID, AssetType.Gesture, true, new AssetDownloadCallback());
						gesture = gotAsset.waitOne(30 * 1000);
						if (gesture != null) {
							synchronized (gestureCache) {
								if (!gestureCache.containsKey(gestureID)) {
									gestureCache.put(gestureID, gesture);
								}
							}
						}
					} catch (Exception ex) {
					}
				}

				// We got it, now we play it
				if (gesture != null) {
					List<GestureStep> sequence = gesture.getSequence();
					for (int i = 0; i < sequence.size(); i++) {
						GestureStep step = sequence.get(i);
						try {
							switch (step.getGestureStepType()) {
							case Chat:
								String text = ((GestureStepChat) step).text;
								int channel = 0;
								Pattern p = Pattern.compile("^/(?<channel>-?[0-9]+) *(?<text>.*)");
								Matcher m = p.matcher(text);

								String val = m.group("channel");
								if (Helpers.isEmpty(val)) {
									try {
										channel = Integer.decode(val);
										text = m.group("text");
									} catch (Exception ex) {
									}
								}
								Chat(text, channel, ChatType.Normal);
								break;
							case Animation:
								GestureStepAnimation anim = (GestureStepAnimation) step;
								if (anim.animationStart) {
									if (SignaledAnimations.containsKey(anim.id)) {
										AnimationStop(anim.id);
									}
									AnimationStart(anim.id);
								} else {
									AnimationStop(anim.id);
								}
								break;
							case Sound:
								// TODO: Add Sound Manager
								// _Client.Sound.PlaySound(((GestureStepSound)step).ID);
								break;
							case Wait:
								GestureStepWait wait = (GestureStepWait) step;
								if (wait.waitForTime) {
									Thread.sleep((int) (1000f * wait.waitTime));
								}
								if (wait.waitForAnimation) {

								}
								break;
							default:
								break;
							}
						} catch (Exception ex) {
						}
					}
				}
			}
		};

		thread.setDaemon(true);
		thread.setName("Gesture thread: " + gestureID);
		thread.start();
	}

	/*
	 * Mark gesture active
	 *
	 * @param invID Inventory {@link UUID} of the gesture
	 *
	 * @param assetID Asset {@link UUID} of the gesture
	 */
	public final void ActivateGesture(UUID invID, UUID assetID) throws Exception {
		ActivateGesturesPacket p = new ActivateGesturesPacket();

		p.AgentData.AgentID = this.agentID;
		p.AgentData.SessionID = this.sessionID;
		p.AgentData.Flags = 0x00;

		ActivateGesturesPacket.DataBlock b = p.new DataBlock();
		b.ItemID = invID;
		b.AssetID = assetID;
		b.GestureFlags = 0x00;

		p.Data = new ActivateGesturesPacket.DataBlock[1];
		p.Data[0] = b;

		_Client.Network.sendPacket(p);

		ActiveGestures.put(invID, assetID);
	}

	/**
	 * Mark gesture inactive
	 *
	 * @param invID
	 *            Inventory {@link UUID} of the gesture
	 */
	public final void DeactivateGesture(UUID invID) throws Exception {
		DeactivateGesturesPacket p = new DeactivateGesturesPacket();

		p.AgentData.AgentID = this.agentID;
		p.AgentData.SessionID = this.sessionID;
		p.AgentData.Flags = 0x00;

		DeactivateGesturesPacket.DataBlock b = p.new DataBlock();
		b.ItemID = invID;
		b.GestureFlags = 0x00;

		p.Data = new DeactivateGesturesPacket.DataBlock[1];
		p.Data[0] = b;

		_Client.Network.sendPacket(p);

		ActiveGestures.remove(invID);
	}

	// #endregion

	// #region Animations

	/**
	 * Send an AgentAnimation packet that toggles a single animation on
	 *
	 * @param animation
	 *            The {@link UUID} of the animation to start playing
	 * @throws Exception
	 */
	public final void AnimationStart(UUID animation) throws Exception {
		Animate(animation, true);
	}

	/**
	 * Send an AgentAnimation packet that toggles a single animation off
	 *
	 * @param animation
	 *            The {@link UUID} of a currently playing animation to stop playing
	 * @throws Exception
	 */
	public final void AnimationStop(UUID animation) throws Exception {
		Animate(animation, false);
	}

	/**
	 * Send an AgentAnimation packet that will toggle an animations on or off
	 *
	 * @param uuid
	 *            The animation {@link UUID} s, and whether to turn that animation
	 *            on or off
	 * @param start
	 *            Wether the animation should be started or stopped
	 * @throws Exception
	 */
	public final void Animate(UUID uuid, Boolean start) throws Exception {
		AgentAnimationPacket animate = new AgentAnimationPacket();
		animate.getHeader().setReliable(false);

		animate.AgentData.AgentID = _Client.Self.getAgentID();
		animate.AgentData.SessionID = _Client.Self.getSessionID();
		animate.AnimationList = new AgentAnimationPacket.AnimationListBlock[1];

		animate.AnimationList[0] = animate.new AnimationListBlock();
		animate.AnimationList[0].AnimID = uuid;
		animate.AnimationList[0].StartAnim = start;

		_Client.Network.sendPacket(animate);
	}

	/**
	 * Send an AgentAnimation packet that will toggle animations on or off
	 *
	 * @param animations
	 *            A list of animation {@link UUID} s, and whether to turn that
	 *            animation on or off
	 * @throws Exception
	 */
	public final void Animate(HashMap<UUID, Boolean> animations) throws Exception {
		AgentAnimationPacket animate = new AgentAnimationPacket();
		animate.getHeader().setReliable(false);

		animate.AgentData.AgentID = _Client.Self.getAgentID();
		animate.AgentData.SessionID = _Client.Self.getSessionID();
		animate.AnimationList = new AgentAnimationPacket.AnimationListBlock[animations.size()];
		int i = 0;

		for (Entry<UUID, Boolean> animation : animations.entrySet()) {
			animate.AnimationList[i] = animate.new AnimationListBlock();
			animate.AnimationList[i].AnimID = animation.getKey();
			animate.AnimationList[i].StartAnim = animation.getValue();
			i++;
		}

		_Client.Network.sendPacket(animate);
	}

	// #endregion Animations

	// #region Teleport

	/**
	 * Teleports agent to their stored home location
	 *
	 * @return true on successful teleport to home location
	 * @throws Exception
	 */
	public final boolean GoHome() throws Exception {
		return Teleport(UUID.Zero, _Client.Settings.TELEPORT_TIMEOUT);
	}

	/**
	 * Teleport agent to a landmark
	 *
	 * @param landmark
	 *            {@link UUID} of the landmark to teleport agent to
	 * @return true on success, false on failure
	 * @throws Exception
	 */
	public final boolean Teleport(UUID landmark) throws Exception {
		return Teleport(landmark, _Client.Settings.TELEPORT_TIMEOUT);
	}

	public final boolean Teleport(UUID landmark, long timeout) throws Exception {
		// Start the timeout check
		teleportTimeout.reset();
		RequestTeleport(landmark);
		TeleportStatus teleportStat = teleportTimeout.waitOne(timeout);
		if (teleportStat == null) {
			teleportStat = TeleportStatus.Failed;
			OnTeleport.dispatch(new TeleportCallbackArgs("Teleport timed out.", teleportStat, 0));
		}
		return (teleportStat == TeleportStatus.Finished);
	}

	/**
	 * Teleport agent to a landmark
	 *
	 * @param landmark
	 *            {@link UUID} of the landmark to teleport agent to
	 * @throws Exception
	 */
	public final void RequestTeleport(UUID landmark) throws Exception {
		if (_Client.Network.getIsEventQueueRunning()) {
			TeleportLandmarkRequestPacket p = new TeleportLandmarkRequestPacket();
			p.Info.AgentID = _Client.Self.getAgentID();
			p.Info.SessionID = _Client.Self.getSessionID();
			p.Info.LandmarkID = landmark;

			logger.info("Requesting teleport to simulator " + landmark.toString());

			_Client.Network.sendPacket(p);
		} else {
			TeleportStatus teleportStat = TeleportStatus.Failed;
			teleportTimeout.set(teleportStat);
			OnTeleport.dispatch(new TeleportCallbackArgs("CAPS event queue is not running", teleportStat, 0));
		}
	}

	/**
	 * Start a Teleport request asynchronously. You can either use the callback
	 * handler to wait for any message or the returned timeoutEvent to abort the
	 * request prematurely if desired.
	 *
	 * <example> // Using a callback handler final Callback<TeleportCallbackArgs>
	 * handler = new Callback<TeleportCallbackArgs>() { public void
	 * callback(TeleportCallbackArgs args) { // Do something with the callback args:
	 * args.status, args.message, args.flags switch (args.status) { case Start: case
	 * Progress: break; case Canceled: case Failed: case Finished: break; } } }
	 * BeginTeleport(handle, pos, handler);
	 *
	 * // Using the timeout event TimeoutEvent<TeleportStatus> timo =
	 * BeginTeleport(handle, pos, null); TeleportStatus stat = timo.waitms(timeout);
	 * if (stat == null) { // The timeout occurred } </example>
	 *
	 * @param regionHandle
	 *            The region handle of the region to teleport to
	 * @param position
	 *            The position inside the region to teleport to
	 * @param tc
	 *            The callback handler that will be invoked with progress and final
	 *            status information
	 * @return A timout event that can be used to wait for the
	 * @throws Exception
	 */
	public TimeoutEvent<TeleportStatus> BeginTeleport(long regionHandle, Vector3 position,
			Callback<TeleportCallbackArgs> tc) throws Exception {
		return BeginTeleport(regionHandle, position, new Vector3(position.X + 1.0f, position.Y, position.Z), tc);
	}

	/**
	 * Start a Teleport request asynchronously. You can either use the callback
	 * handler to wait for any message or the returned timeoutEvent to abort the
	 * request prematurely if desired.
	 *
	 * @param regionHandle
	 *            The region handle of the region to teleport to
	 * @param position
	 *            The position inside the region to teleport to
	 * @param lookAt
	 *            The direction in which to look at when arriving
	 * @param tc
	 *            The callback handler that will be invoked with progress and final
	 *            status information
	 * @return A timout event that can be used to wait for the
	 * @throws Exception
	 */
	public TimeoutEvent<TeleportStatus> BeginTeleport(long regionHandle, Vector3 position, Vector3 lookAt,
			Callback<TeleportCallbackArgs> tc) throws Exception {
		if (tc != null)
			OnTeleport.add(tc, true);

		// Start the timeout check
		teleportTimeout.reset();
		RequestTeleport(regionHandle, position, lookAt);
		return teleportTimeout;
	}

	/**
	 * Request teleport to a another simulator
	 *
	 * @param regionHandle
	 *            handle of region to teleport agent to
	 * @param position
	 *            {@link Vector3} position in destination sim to teleport to
	 * @throws Exception
	 */
	public final void RequestTeleport(long regionHandle, Vector3 position) throws Exception {
		RequestTeleport(regionHandle, position, Vector3.UnitY);
	}

	/**
	 * Request teleport to a another simulator
	 *
	 * @param regionHandle
	 *            handle of region to teleport agent to
	 * @param position
	 *            {@link Vector3} position in destination sim to teleport to
	 * @param lookAt
	 *            {@link Vector3} direction in destination sim agent will look at
	 * @throws Exception
	 */
	public final void RequestTeleport(long regionHandle, Vector3 position, Vector3 lookAt) throws Exception {
		if (_Client.Network.getIsEventQueueRunning()) {
			TeleportLocationRequestPacket teleport = new TeleportLocationRequestPacket();
			teleport.AgentData.AgentID = _Client.Self.getAgentID();
			teleport.AgentData.SessionID = _Client.Self.getSessionID();
			teleport.Info.LookAt = lookAt;
			teleport.Info.Position = position;
			teleport.Info.RegionHandle = regionHandle;

			logger.info(GridClient.Log("Requesting teleport to region handle " + ((Long) regionHandle).toString(),
					_Client));

			_Client.Network.sendPacket(teleport);
		} else {
			TeleportStatus teleportStat = TeleportStatus.Failed;
			teleportTimeout.set(teleportStat);
			OnTeleport.dispatch(new TeleportCallbackArgs("CAPS event queue is not running", teleportStat, 0));
		}
	}

	/**
	 * Teleport agent to another region
	 *
	 * @param regionHandle
	 *            handle of region to teleport agent to
	 * @param position
	 *            {@link Vector3} position in destination sim to teleport to
	 * @return true on success, false on failure
	 */
	public boolean Teleport(long regionHandle, Vector3 position) throws Exception {
		return Teleport(regionHandle, position, Vector3.UnitY, _Client.Settings.TELEPORT_TIMEOUT);
	}

	/**
	 * Teleport agent to another region
	 *
	 * @param regionHandle
	 *            handle of region to teleport agent to
	 * @param position
	 *            {@link Vector3} position in destination sim to teleport to
	 * @param timeout
	 *            The maximum time to wait for the teleport to succeed
	 * @return true on success, false on failure
	 */
	public boolean Teleport(long regionHandle, Vector3 position, long timeout) throws Exception {
		return Teleport(regionHandle, position, Vector3.UnitY, timeout);
	}

	/**
	 * Teleport agent to another region
	 *
	 * @param regionHandle
	 *            handle of region to teleport agent to
	 * @param position
	 *            {@link Vector3} position in destination sim to teleport to
	 * @param lookAt
	 *            {@link Vector3} direction in destination sim agent will look at
	 * @param timeout
	 *            The maximum time to wait for the teleport to succeed
	 * @return true on success, false on failure
	 */
	public boolean Teleport(long regionHandle, Vector3 position, Vector3 lookAt, long timeout) throws Exception {
		// Start the timeout check
		teleportTimeout.reset();
		RequestTeleport(regionHandle, position, lookAt);

		TeleportStatus teleportStat = teleportTimeout.waitOne(timeout);
		if (teleportStat == null) {
			teleportStat = TeleportStatus.Failed;
			OnTeleport.dispatch(new TeleportCallbackArgs("Teleport timed out.", teleportStat, 0));
		}
		return (teleportStat == TeleportStatus.Finished);
	}

	/**
	 * Attempt to look up a simulator name and teleport to the discovered
	 * destination
	 *
	 * @param simName
	 *            Region name to look up
	 * @param position
	 *            {@link Vector3} position to teleport to
	 * @return True if the lookup and teleport were successful, otherwise
	 */
	public boolean Teleport(String simName, Vector3 position) throws Exception {
		return Teleport(simName, position, Vector3.UnitY, _Client.Settings.TELEPORT_TIMEOUT);
	}

	/**
	 * Attempt to look up a simulator name and teleport to the discovered
	 * destination
	 *
	 * @param simName
	 *            Region name to look up
	 * @param position
	 *            {@link Vector3} position to teleport to
	 * @param lookAt
	 *            {@link Vector3} direction in destination sim agent will look at
	 * @param timeout
	 *            The maximum time to wait for the teleport to succeed
	 * @return True if the lookup and teleport were successful, false otherwise
	 * @throws Exception
	 */
	public boolean Teleport(String simName, Vector3 position, Vector3 lookAt, long timeout) throws Exception {
		if (_Client.Network.getCurrentSim() == null) {
			return false;
		}

		if (!simName.equals(_Client.Network.getCurrentSim().getName())) {
			// Teleporting to a foreign sim
			GridRegion region = _Client.Grid.GetGridRegion(simName, GridLayerType.Objects);
			if (region != null) {
				return Teleport(region.RegionHandle, position, lookAt, timeout);
			}

			TeleportStatus teleportStat = TeleportStatus.Failed;
			OnTeleport.dispatch(new TeleportCallbackArgs("Unable to resolve name: " + simName, teleportStat, 0));
			return false;
		}

		// Teleporting to the sim we're already in
		return Teleport(_Client.getCurrentRegionHandle(), position, lookAt, timeout);
	}

	/**
	 * Send a teleport lure to another avatar with default "Join me in ..."
	 * invitation message
	 *
	 * @param targetID
	 *            Target avatars {@link UUID} to lure
	 * @throws Exception
	 */
	public final void SendTeleportLure(UUID targetID) throws Exception {
		SendTeleportLure(targetID, "Join me in " + _Client.Network.getCurrentSim().getSimName() + "!");
	}

	/**
	 * Send a teleport lure to another avatar with custom invitation message
	 *
	 * @param targetID
	 *            target avatars {@link UUID} to lure
	 * @param message
	 *            custom message to send with invitation
	 * @throws Exception
	 */
	public final void SendTeleportLure(UUID targetID, String message) throws Exception {
		StartLurePacket p = new StartLurePacket();
		p.AgentData.AgentID = _Client.Self.getAgentID();
		p.AgentData.SessionID = _Client.Self.getSessionID();
		p.Info.LureType = 0;
		p.Info.setMessage(Helpers.StringToBytes(message));
		p.TargetID = new UUID[1];
		p.TargetID[0] = targetID;
		_Client.Network.sendPacket(p);
	}

	/**
	 * Respond to a teleport lure by either accepting it and initiating the
	 * teleport, or denying it
	 *
	 * @param requesterID
	 *            {@link UUID} of the avatar sending the lure
	 * @param lureID
	 *            {@link UUID} of the lure ID received on the
	 * @param accept
	 *            true to accept the lure, false to decline it
	 * @throws Exception
	 */
	public final void TeleportLureRespond(UUID requesterID, UUID lureID, boolean accept) throws Exception {
		TeleportLureRespond(requesterID, lureID, accept, false);
	}

	private final void TeleportLureRespond(UUID requesterID, UUID lureID, boolean accept, boolean godlike)
			throws Exception {
		if (accept) {
			TeleportLureRequestPacket lure = new TeleportLureRequestPacket();

			lure.Info.AgentID = getAgentID();
			lure.Info.SessionID = getSessionID();
			lure.Info.LureID = lureID;
			if (godlike) {
				lure.Info.TeleportFlags = TeleportFlags.ViaGodlikeLure | TeleportFlags.DisableCancel;
			} else {
				lure.Info.TeleportFlags = TeleportFlags.ViaLure;
			}
			_Client.Network.sendPacket(lure);
		} else {
			InstantMessage(getName(), requesterID, Helpers.EmptyString, lureID, InstantMessageDialog.DenyTeleport,
					InstantMessageOnline.Online);
		}
	}

	/**
	 * Request the list of muted objects and avatars for this agent
	 *
	 * @throws Exception
	 */
	public void RequestMuteList() throws Exception {
		MuteListRequestPacket mute = new MuteListRequestPacket();
		mute.AgentData.AgentID = _Client.Self.getAgentID();
		mute.AgentData.SessionID = _Client.Self.getSessionID();
		mute.MuteCRC = 0;

		_Client.Network.sendPacket(mute);
	}

	/**
	 * Mute an object, resident, etc.
	 *
	 * @param type
	 *            Mute type
	 * @param id
	 *            Mute UUID
	 * @param name
	 *            Mute name
	 * @throws Exception
	 */
	public void UpdateMuteListEntry(MuteType type, UUID id, String name) throws Exception {
		UpdateMuteListEntry(type, id, name, MuteFlags.Default);
	}

	/**
	 * Mute an object, resident, etc.
	 *
	 * @param type
	 *            Mute type
	 * @param id
	 *            Mute UUID
	 * @param name
	 *            Mute name
	 * @param flags
	 *            Mute flags
	 * @throws Exception
	 */
	public void UpdateMuteListEntry(MuteType type, UUID id, String name, byte flags) throws Exception {
		UpdateMuteListEntryPacket p = new UpdateMuteListEntryPacket();
		p.AgentData.AgentID = _Client.Self.getAgentID();
		p.AgentData.SessionID = _Client.Self.getSessionID();

		p.MuteData.MuteType = type.getValue();
		p.MuteData.MuteID = id;
		p.MuteData.setMuteName(Helpers.StringToBytes(name));
		p.MuteData.MuteFlags = flags;

		_Client.Network.sendPacket(p);

		MuteEntry me = new MuteEntry();
		me.Type = type;
		me.ID = id;
		me.Name = name;
		me.Flags = flags;
		synchronized (MuteList) {
			MuteList.put(String.format("%s|%s", me.ID, me.Name), me);
		}
		OnMuteListUpdated.dispatch(null);

	}

	/**
	 * Unmute an object, resident, etc.
	 *
	 * @param id
	 *            Mute UUID
	 * @param name
	 *            Mute name
	 * @throws Exception
	 */
	public void RemoveMuteListEntry(UUID id, String name) throws Exception {
		RemoveMuteListEntryPacket p = new RemoveMuteListEntryPacket();
		p.AgentData.AgentID = _Client.Self.getAgentID();
		p.AgentData.SessionID = _Client.Self.getSessionID();

		p.MuteData.MuteID = id;
		p.MuteData.setMuteName(Helpers.StringToBytes(name));

		_Client.Network.sendPacket(p);

		String listKey = String.format("%s|%s", id, name);
		if (MuteList.containsKey(listKey)) {
			synchronized (MuteList) {
				MuteList.remove(listKey);
			}
			OnMuteListUpdated.dispatch(null);
		}
	}

	/**
	 * Sets home location to agents current position Will fire an AlertMessage
	 * (<seealso cref="E:OpenMetaverse.AgentManager.OnAlertMessage"/>) with success
	 * or failure message
	 *
	 * @throws Exception
	 */
	public void SetHome() throws Exception {
		SetHome(1, getAgentPosition(), _Movement.Camera.getAtAxis());
	}

	/**
	 * Sets home location to the provided position and lookat parameters Will fire
	 * an AlertMessage
	 * (<seealso cref="E:OpenMetaverse.AgentManager.OnAlertMessage"/>) with success
	 * or failure message
	 *
	 * @throws Exception
	 */
	public void SetHome(int id, Vector3 pos, Vector3 lookAt) throws Exception {
		final class HomeLocationResponse implements FutureCallback<OSD> {
			@Override
			public void completed(OSD result) {
				if (result instanceof OSDMap) {
					OSDMap map = (OSDMap) result;
					if (!map.containsKey("success") || !map.get("success").AsBoolean())
						return;

					if (map.containsKey("HomeLocation")) {
						result = map.get("HomeLocation");
						if (result instanceof OSDMap) {
							map = (OSDMap) result;
							if (map.containsKey("LocationPos")) {
								result = map.get("LocationPos");
								if (result instanceof OSDMap) {
									map = (OSDMap) result;
									setHomePosRegion(_Client.getCurrentRegionHandle(), map.AsVector3());
								}
							}
						}
					}
				}
			}

			@Override
			public void failed(Exception ex) {
			}

			@Override
			public void cancelled() {
			}
		}

		URI url = _Client.Network.getCapabilityURI(CapsEventType.HomeLocation.toString());
		if (url != null) {
			CapsClient request = new CapsClient(_Client, CapsEventType.HomeLocation.toString());
			OSDMap map = new OSDMap(2);
			map.put("LocationId", OSD.FromInteger(id));
			map.put("LocationPos", OSD.FromVector3(pos));
			map.put("LocationLookAt", OSD.FromVector3(lookAt));
			request.executeHttpPost(url, map, OSDFormat.Xml, new HomeLocationResponse(), _Client.Settings.CAPS_TIMEOUT);
		} else {
			SetStartLocationRequestPacket s = new SetStartLocationRequestPacket();
			s.AgentData = s.new AgentDataBlock();
			s.AgentData.AgentID = getAgentID();
			s.AgentData.SessionID = getSessionID();
			s.StartLocationData = s.new StartLocationDataBlock();
			s.StartLocationData.LocationPos = pos;
			s.StartLocationData.LocationID = id;
			s.StartLocationData.setSimName(Helpers.StringToBytes(Helpers.EmptyString));
			s.StartLocationData.LocationLookAt = lookAt;
			_Client.Network.sendPacket(s);

			if (id == 1) {
				setHomePosRegion(_Client.getCurrentRegionHandle(), pos);
			}
		}
	}

	/**
	 * Acknowledge agent movement complete
	 *
	 * @param simulator
	 *            {@link T:OpenMetaverse.Simulator} Object
	 * @throws Exception
	 */
	public void CompleteAgentMovement(SimulatorManager simulator) throws Exception {
		CompleteAgentMovementPacket move = new CompleteAgentMovementPacket();

		move.AgentData.AgentID = this.agentID;
		move.AgentData.SessionID = this.sessionID;
		move.AgentData.CircuitCode = _Client.Network.getCircuitCode();

		simulator.sendPacket(move);
	}

	public void SendMovementUpdate(boolean reliable, SimulatorManager simulator) throws Exception {
		_Movement.SendUpdate(reliable, simulator);
	}

	/**
	 * Reply to script permissions request
	 *
	 * @param simulator
	 *            {@link T:OpenMetaverse.Simulator} Object
	 * @param itemID
	 *            {@link UUID} of the itemID requesting permissions
	 * @param taskID
	 *            {@link UUID} of the taskID requesting permissions
	 * @param permissions
	 *            {@link OpenMetaverse.ScriptPermission} list of permissions to
	 *            allow
	 * @throws Exception
	 */
	public final void ScriptQuestionReply(SimulatorManager simulator, UUID itemID, UUID taskID, int permissions)
			throws Exception {
		ScriptAnswerYesPacket yes = new ScriptAnswerYesPacket();
		yes.AgentData.AgentID = _Client.Self.getAgentID();
		yes.AgentData.SessionID = _Client.Self.getSessionID();
		yes.Data.ItemID = itemID;
		yes.Data.TaskID = taskID;
		yes.Data.Questions = permissions;

		simulator.sendPacket(yes);
	}

	/**
	 * Respond to a group invitation by either accepting or denying it
	 *
	 * @param groupID
	 *            UUID of the group (sent in the AgentID field of the invite
	 *            message)
	 * @param imSessionID
	 *            IM Session ID from the group invitation message
	 * @param accept
	 *            Accept the group invitation or deny it
	 * @throws Exception
	 */
	public final void GroupInviteRespond(UUID groupID, UUID imSessionID, boolean accept) throws Exception {
		InstantMessage(getName(), groupID, Helpers.EmptyString, imSessionID,
				accept ? InstantMessageDialog.GroupInvitationAccept : InstantMessageDialog.GroupInvitationDecline,
				InstantMessageOnline.Offline);
	}

	/**
	 * Requests script detection of objects and avatars
	 *
	 * @param name
	 *            name of the object/avatar to search for
	 * @param searchID
	 *            UUID of the object or avatar to search for
	 * @param type
	 *            Type of search from ScriptSensorTypeFlags
	 * @param range
	 *            range of scan (96 max?)
	 * @param arc
	 *            the arc in radians to search within
	 * @param requestID
	 *            an user generated ID to correlate replies with
	 * @param sim
	 *            Simulator to perform search in
	 * @throws Exception
	 */
	public final void RequestScriptSensor(String name, UUID searchID, byte type, float range, float arc, UUID requestID,
			SimulatorManager simulator) throws Exception {
		ScriptSensorRequestPacket request = new ScriptSensorRequestPacket();
		request.Requester.Arc = arc;
		request.Requester.Range = range;
		request.Requester.RegionHandle = simulator.getHandle();
		request.Requester.RequestID = requestID;
		request.Requester.SearchDir = Quaternion.Identity; // TODO: this needs
															// to be tested
		request.Requester.SearchID = searchID;
		request.Requester.setSearchName(Helpers.StringToBytes(name));
		request.Requester.SearchPos = Vector3.Zero;
		request.Requester.SearchRegions = 0; // TODO: ?
		request.Requester.SourceID = _Client.Self.getAgentID();
		request.Requester.Type = type;

		simulator.sendPacket(request);
	}

	/**
	 * Create or update profile pick
	 *
	 * @param pickID
	 *            UUID of the pick to update, or random UUID to create a new pick
	 * @param topPick
	 *            Is this a top pick? (typically false)
	 * @param parcelID
	 *            UUID of the parcel (UUID.Zero for the current parcel)
	 * @param name
	 *            Name of the pick
	 * @param globalPosition
	 *            Global position of the pick landmark
	 * @param textureID
	 *            UUID of the image displayed with the pick
	 * @param description
	 *            Long description of the pick
	 * @throws Exception
	 */
	public final void PickInfoUpdate(UUID pickID, boolean topPick, UUID parcelID, String name, Vector3d globalPosition,
			UUID textureID, String description) throws Exception {
		PickInfoUpdatePacket pick = new PickInfoUpdatePacket();
		pick.AgentData.AgentID = _Client.Self.getAgentID();
		pick.AgentData.SessionID = _Client.Self.getSessionID();
		pick.Data.PickID = pickID;
		pick.Data.setDesc(Helpers.StringToBytes(description));
		pick.Data.CreatorID = _Client.Self.getAgentID();
		pick.Data.TopPick = topPick;
		pick.Data.ParcelID = parcelID;
		pick.Data.setName(Helpers.StringToBytes(name));
		pick.Data.SnapshotID = textureID;
		pick.Data.PosGlobal = globalPosition;
		pick.Data.SortOrder = 0;
		pick.Data.Enabled = false;

		_Client.Network.sendPacket(pick);
	}

	/**
	 * Delete profile pick
	 *
	 * @param pickID
	 *            UUID of the pick to delete
	 */
	public final void PickDelete(UUID pickID) throws Exception {
		PickDeletePacket delete = new PickDeletePacket();
		delete.AgentData.AgentID = _Client.Self.getAgentID();
		delete.AgentData.SessionID = _Client.Self.getSessionID();
		delete.PickID = pickID;

		_Client.Network.sendPacket(delete);
	}

	/**
	 * Create or update profile Classified
	 *
	 * @param classifiedID
	 *            UUID of the classified to update, or random UUID to create a new
	 *            classified
	 * @param category
	 *            Defines what catagory the classified is in
	 * @param snapshotID
	 *            UUID of the image displayed with the classified
	 * @param price
	 *            Price that the classified will cost to place for a week
	 * @param position
	 *            Global position of the classified landmark
	 * @param name
	 *            Name of the classified
	 * @param desc
	 *            Long description of the classified
	 * @param autoRenew
	 *            if true, auto renew classified after expiration
	 * @throws Exception
	 */
	public final void UpdateClassifiedInfo(UUID classifiedID, ClassifiedCategories category, UUID snapshotID, int price,
			Vector3d position, String name, String desc, boolean autoRenew) throws Exception {
		ClassifiedInfoUpdatePacket classified = new ClassifiedInfoUpdatePacket();
		classified.AgentData.AgentID = _Client.Self.getAgentID();
		classified.AgentData.SessionID = _Client.Self.getSessionID();

		classified.Data.ClassifiedID = classifiedID;
		classified.Data.Category = ClassifiedCategories.getValue(category);

		classified.Data.ParcelID = UUID.Zero;
		// TODO: verify/fix ^
		classified.Data.ParentEstate = 0;
		// TODO: verify/fix ^

		classified.Data.SnapshotID = snapshotID;
		classified.Data.PosGlobal = position;

		classified.Data.ClassifiedFlags = autoRenew ? ClassifiedFlags.AutoRenew : ClassifiedFlags.None;
		// TODO: verify/fix ^

		classified.Data.PriceForListing = price;
		classified.Data.setName(Helpers.StringToBytes(name));
		classified.Data.setDesc(Helpers.StringToBytes(desc));
		_Client.Network.sendPacket(classified);
	}

	/**
	 * Create or update profile Classified
	 *
	 * @param classifiedID
	 *            UUID of the classified to update, or random UUID to create a new
	 *            classified
	 * @param category
	 *            Defines what catagory the classified is in
	 * @param snapshotID
	 *            UUID of the image displayed with the classified
	 * @param price
	 *            Price that the classified will cost to place for a week
	 * @param name
	 *            Name of the classified
	 * @param desc
	 *            Long description of the classified
	 * @param autoRenew
	 *            if true, auto renew classified after expiration
	 * @throws Exception
	 */
	public final void UpdateClassifiedInfo(UUID classifiedID, ClassifiedCategories category, UUID snapshotID, int price,
			String name, String desc, boolean autoRenew) throws Exception {
		UpdateClassifiedInfo(classifiedID, category, snapshotID, price, _Client.Self.getGlobalPosition(), name, desc,
				autoRenew);
	}

	/**
	 * Delete a classified ad
	 *
	 * @param classifiedID
	 *            The classified ads ID
	 * @throws Exception
	 */
	public final void DeleteClassfied(UUID classifiedID) throws Exception {
		ClassifiedDeletePacket classified = new ClassifiedDeletePacket();
		classified.AgentData.AgentID = _Client.Self.getAgentID();
		classified.AgentData.SessionID = _Client.Self.getSessionID();

		classified.ClassifiedID = classifiedID;
		_Client.Network.sendPacket(classified);
	}

	/**
	 * Fetches resource usage by agents attachmetns
	 *
	 * @param callback
	 *            Called when the requested information is collected
	 */
	private class AttachmentResourceReplyHandler implements FutureCallback<OSD> {
		private final Callback<AttachmentResourcesCallbackArgs> callback;

		public AttachmentResourceReplyHandler(Callback<AttachmentResourcesCallbackArgs> callback) {
			this.callback = callback;
		}

		@Override
		public void completed(OSD result) {
			if (result == null) {
				callback.callback(new AttachmentResourcesCallbackArgs(false, null));
			}
			AttachmentResourcesMessage info = (AttachmentResourcesMessage) _Client.Messages
					.decodeEvent(CapsEventType.AttachmentResources, (OSDMap) result);
			callback.callback(new AttachmentResourcesCallbackArgs(true, info));
		}

		@Override
		public void failed(Exception ex) {
			callback.callback(new AttachmentResourcesCallbackArgs(false, null));
		}

		@Override
		public void cancelled() {
			callback.callback(new AttachmentResourcesCallbackArgs(false, null));
		}
	}

	public final void GetAttachmentResources(final Callback<AttachmentResourcesCallbackArgs> callback)
			throws IOException {
		URI url = _Client.Network.getCapabilityURI(CapsEventType.AttachmentResources.toString());
		if (url != null) {
			CapsClient request = new CapsClient(_Client, CapsEventType.AttachmentResources.toString());
			request.executeHttpGet(url, Helpers.EmptyString, new AttachmentResourceReplyHandler(callback),
					_Client.Settings.CAPS_TIMEOUT);
		}
	}

	/**
	 * Initates request to set a new display name
	 *
	 * @param oldName
	 *            Previous display name
	 * @param newName
	 *            Desired new display name
	 * @throws IOException
	 */
	public void SetDisplayName(String oldName, String newName) throws IOException {
		URI url = _Client.Network.getCapabilityURI(CapsEventType.SetDisplayName.toString());
		if (url == null) {
			logger.warn(GridClient.Log("Unable to invoke SetDisplyName capability at this time", _Client));
			throw new IOException("Unable to retrieve SetDisplayName capability");
		}

		SetDisplayNameMessage msg = _Client.Messages.new SetDisplayNameMessage();
		msg.oldDisplayName = oldName;
		msg.newDisplayName = newName;

		new CapsClient(_Client, CapsEventType.SetDisplayName.toString()).executeHttpPost(url, msg, null,
				_Client.Settings.CAPS_TIMEOUT);
	}

	/**
	 * Tells the sim what UI language is used, and if it's ok to share that with
	 * scripts
	 *
	 * @param language
	 *            Two letter language code
	 * @param isPublic
	 *            Share language info with scripts
	 */
	public void UpdateAgentLanguage(String language, boolean isPublic) {
		try {
			UpdateAgentLanguageMessage msg = _Client.Messages.new UpdateAgentLanguageMessage();
			msg.language = language;
			msg.languagePublic = isPublic;

			URI url = _Client.Network.getCapabilityURI(CapsEventType.UpdateAgentLanguage.toString());
			if (url != null) {
				new CapsClient(_Client, CapsEventType.UpdateAgentLanguage.toString()).executeHttpPost(url, msg, null,
						_Client.Settings.CAPS_TIMEOUT);
			}
		} catch (Exception ex) {
			logger.error(GridClient.Log("Failes to update agent language", _Client), ex);
		}
	}

	/**
	 * Sets agents maturity access level
	 *
	 * @param access
	 *            PG, M or A
	 * @throws IOReactorException
	 */
	public void SetAgentAccess(String access) throws IOReactorException {
		SetAgentAccess(access, null);
	}

	/**
	 * Sets agents maturity access level
	 *
	 * @param access
	 *            PG, M or A
	 * @param callback
	 *            Callback function
	 * @throws IOReactorException
	 */
	public void SetAgentAccess(String access, final Callback<AgentAccessCallbackArgs> callback)
			throws IOReactorException {
		if (_Client == null)
			return;

		URI url = _Client.Network.getCurrentSim().getCapabilityURI("UpdateAgentInformation");
		if (url == null)
			return;

		CapsClient request = new CapsClient(_Client, "UpdateAgentInformation");

		final class AccessCallback implements FutureCallback<OSD> {

			@Override
			public void cancelled() {
				logger.info(GridClient.Log("Max maturity unchanged at " + agentAccess + ".", _Client));
			}

			@Override
			public void completed(OSD result) {
				OSDMap map = (OSDMap) ((OSDMap) result).get("access_prefs");
				agentAccess = map.get("max").AsString();
				logger.info(GridClient.Log("Max maturity access set to " + agentAccess + ".", _Client));
			}

			@Override
			public void failed(Exception ex) {
				logger.warn(GridClient.Log("Failed setting max maturity access.", _Client), ex);
				callback.callback(new AgentAccessCallbackArgs(agentAccess, false));
			}
		}

		OSDMap req = new OSDMap();
		OSDMap prefs = new OSDMap();
		prefs.put("max", OSD.FromString(access));
		req.put("access_prefs", prefs);

		request.executeHttpPost(url, req, OSDFormat.Xml, new AccessCallback(), _Client.Settings.CAPS_TIMEOUT);
	}

	/**
	 * Sets agents hover height.
	 *
	 * @param hoverHeight
	 *            : Hover height [-2.0, 2.0]
	 */
	public void SetHoverHeight(double hoverHeight) {
		if (_Client == null)
			return;

		URI url = _Client.Network.getCurrentSim().getCapabilityURI("AgentPreferences");
		if (url == null)
			return;

		try {
			CapsClient request = new CapsClient(_Client, "AgentPreferences");

			final class HoverHeightCallback implements FutureCallback<OSD> {

				@Override
				public void cancelled() {
					logger.info(GridClient.Log("Hover height unchanged.", _Client));
				}

				@Override
				public void completed(OSD result) {
					double confirmedHeight = ((OSDMap) result).get("hover_height").AsReal();
					logger.info(GridClient.Log("Hover height set to " + confirmedHeight + ".", _Client));
				}

				@Override
				public void failed(Exception ex) {
					logger.warn(GridClient.Log("Failed to set hover height.", _Client), ex);
				}
			}

			OSDMap postData = new OSDMap(1);
			postData.put("hover_height", OSD.FromReal(hoverHeight));
			request.executeHttpPost(url, postData, OSDFormat.Xml, new HoverHeightCallback(),
					_Client.Settings.CAPS_TIMEOUT);
		} catch (IOReactorException e) {
			logger.error(e);
		}

	}
	// #endregion Misc

	public void UpdateCamera(boolean reliable) throws Exception {
		AgentUpdatePacket update = new AgentUpdatePacket();
		update.AgentData.AgentID = this.agentID;
		update.AgentData.SessionID = this.sessionID;
		update.AgentData.State = 0;
		update.AgentData.BodyRotation = new Quaternion(0, 0.6519076f, 0, 0);
		update.AgentData.HeadRotation = new Quaternion();
		// Semi-sane default values
		update.AgentData.CameraCenter = new Vector3(9.549901f, 7.033957f, 11.75f);
		update.AgentData.CameraAtAxis = new Vector3(0.7f, 0.7f, 0);
		update.AgentData.CameraLeftAxis = new Vector3(-0.7f, 0.7f, 0);
		update.AgentData.CameraUpAxis = new Vector3(0.1822026f, 0.9828722f, 0);
		update.AgentData.Far = 384;
		update.AgentData.ControlFlags = 0; // TODO: What is this?
		update.AgentData.Flags = 0;
		update.getHeader().setReliable(reliable);

		_Client.Network.sendPacket(update);

		// Send an AgentFOV packet widening our field of vision
		/*
		 * AgentFOVPacket fovPacket = new AgentFOVPacket(); fovPacket.AgentData.AgentID
		 * = this.ID; fovPacket.AgentData.SessionID = _Client.Network.SessionID;
		 * fovPacket.AgentData.CircuitCode = simulator.CircuitCode;
		 * fovPacket.FOVBlock.GenCounter = 0; fovPacket.FOVBlock.VerticalAngle =
		 * 6.28318531f; fovPacket.Header.Reliable = true;
		 * _Client.Network.sendPacket(fovPacket);
		 */
	}

	private void HandleCoarseLocation(Packet packet, Simulator simulator) throws Exception {
		// TODO: This will be useful one day
	}

	private UUID computeSessionID(InstantMessageDialog dialog, UUID fromID) {
		UUID sessionID = fromID;
		switch (dialog) {
		case RequestTeleport:
		case GroupInvitation:
		default:
			break;
		}
		return sessionID;
	}

	private void HandleInstantMessage(Packet packet, Simulator sim) throws Exception {
		ImprovedInstantMessagePacket im = (ImprovedInstantMessagePacket) packet;
		SimulatorManager simulator = (SimulatorManager) sim;

		InstantMessageDialog dialog = InstantMessageDialog.setValue(im.MessageBlock.Dialog);
		String fromName = Helpers.BytesToString(im.MessageBlock.getFromAgentName());
		String message = Helpers.BytesToString(im.MessageBlock.getMessage());
		boolean isMuted = MuteList.containsKey(String.format("%s|%s", im.AgentData.AgentID, fromName)); // MuteFlags.TextChat
		UUID sessionID = computeSessionID(dialog, im.AgentData.AgentID);

		if (dialog == InstantMessageDialog.RequestTeleport) {
			/* A user sent us a teleport lure */
			if (isMuted) {
				return;
			} else if (isBusy) {
				// busyMessage(im.AgentData.AgentID);
			} else if (OnTeleportLure.count() > 0) {
				String[] strings = Helpers.BytesToString(im.MessageBlock.getBinaryBucket()).split("|");
				LureLocation info = new LureLocation();
				info.regionHandle = Helpers.GlobalPosToRegionHandle(Float.valueOf(strings[0]),
						Float.valueOf(strings[1]), null);
				info.position = new Vector3(Float.valueOf(strings[2]), Float.valueOf(strings[3]),
						Float.valueOf(strings[4]));
				info.lookAt = null;
				if (strings.length >= 8)
					info.lookAt = new Vector3(Float.valueOf(strings[5]), Float.valueOf(strings[6]),
							Float.valueOf(strings[7]));
				info.maturity = Helpers.EmptyString;
				if (strings.length >= 9)
					info.maturity = strings[8];

				if (OnTeleportLure.count() > 0) {
					OnTeleportLure.dispatch(
							new TeleportLureCallbackArgs(im.AgentData.AgentID, fromName, sessionID, message, info));
				} else {
					// Nobody to handle this lure request
					TeleportLureRespond(im.AgentData.AgentID, sessionID, false);
				}
				/* We dispatched the lure request, so return */
				return;
			}
		}
		if (dialog == InstantMessageDialog.GodLikeRequestTeleport) {
			/*
			 * A godlike teleport lure. Typically just teleport but we pass it to the
			 * callback anyhow, but ignore getAccepted()
			 */
			OnTeleportLure
					.dispatch(new TeleportLureCallbackArgs(im.AgentData.AgentID, fromName, sessionID, message, null));
			TeleportLureRespond(im.AgentData.AgentID, sessionID, true, true);
			return;
		}
		if (dialog == InstantMessageDialog.GotoUrl) {
			/* An URL sent form the system, not a script */
			String url = Helpers.BytesToString(im.MessageBlock.getBinaryBucket());
			if (url.length() <= 0)
				logger.warn(GridClient.Log("No URL in binary bucket for GotoURL IM", _Client));
			return;
			/*
			 * TODO: if (OnGotoURL.count() > 0) { OnGotoURL.dispatch(new
			 * GotoURLCallbackArgs(message, url); return; }
			 */
		} else if (dialog == InstantMessageDialog.GroupInvitation) {
			/*
			 * A user sent us a group invite, Handled by GroupManager in standard callback
			 * below
			 */
		}

		InstantMessage mess = new InstantMessage();
		mess.Dialog = dialog;
		mess.Offline = InstantMessageOnline.setValue(im.MessageBlock.Offline);
		mess.FromAgentID = im.AgentData.AgentID;
		mess.FromAgentName = fromName;
		mess.ToAgentID = im.MessageBlock.ToAgentID;
		mess.ParentEstateID = im.MessageBlock.ParentEstateID;
		mess.RegionID = im.MessageBlock.RegionID;
		mess.Position = im.MessageBlock.Position;
		mess.GroupIM = im.MessageBlock.FromGroup;
		mess.IMSessionID = im.MessageBlock.ID;
		mess.Timestamp = im.MessageBlock.Timestamp == 0 ? new Date()
				: new Date(im.MessageBlock.Timestamp & 0xFFFFFFFFL);
		mess.Message = message;
		mess.BinaryBucket = im.MessageBlock.getBinaryBucket();
		OnInstantMessage.dispatch(new InstantMessageCallbackArgs(mess, simulator));
	}

	private void HandleChat(Packet packet, Simulator simulator) throws Exception {
		ChatFromSimulatorPacket chat = (ChatFromSimulatorPacket) packet;
		try {
			String message = Helpers.BytesToString(chat.ChatData.getMessage());
			String from = Helpers.BytesToString(chat.ChatData.getFromName());
			logger.debug(GridClient.Log("ChatFromSimulator: Type: " + ChatType.setValue(chat.ChatData.ChatType)
					+ " From: " + from + " Message: " + message, _Client));

			OnChat.dispatch(new ChatCallbackArgs(ChatAudibleLevel.setValue(chat.ChatData.Audible),
					ChatType.setValue(chat.ChatData.ChatType), ChatSourceType.setValue(chat.ChatData.SourceType), from,
					message, chat.ChatData.SourceID, chat.ChatData.OwnerID, chat.ChatData.Position));
		} catch (Exception ex) {
			logger.debug(GridClient.Log("Exception in ChatFromSimulator", _Client), ex);
		}
	}

	private void HandleAgentMovementComplete(Packet packet, Simulator sim) throws UnsupportedEncodingException {
		AgentMovementCompletePacket movement = (AgentMovementCompletePacket) packet;
		SimulatorManager simulator = (SimulatorManager) sim;

		relativePosition = movement.Data.Position;
		_Movement.Camera.LookDirection(movement.Data.LookAt);
		simulator.setHandle(movement.Data.RegionHandle);
		simulator.SimVersion = Helpers.BytesToString(movement.SimData.getChannelVersion());
		simulator.AgentMovementComplete = true;
	}

	private void HandleHealthMessage(Packet packet, Simulator simulator) {
		health = ((HealthMessagePacket) packet).Health;
	}

	private void HandleTeleport(Packet packet, Simulator simulator) throws Exception {
		int flags = 0;
		TeleportStatus teleportStatus = TeleportStatus.None;
		String teleportMessage = Helpers.EmptyString;
		boolean finished = false;

		if (packet.getType() == PacketType.TeleportStart) {
			TeleportStartPacket start = (TeleportStartPacket) packet;

			teleportStatus = TeleportStatus.Start;
			teleportMessage = "Teleport started";
			flags = start.TeleportFlags;

			logger.debug(GridClient.Log("TeleportStart received, Flags: " + flags, _Client));
		} else if (packet.getType() == PacketType.TeleportProgress) {
			TeleportProgressPacket progress = (TeleportProgressPacket) packet;

			teleportStatus = TeleportStatus.Progress;
			teleportMessage = Helpers.BytesToString(progress.Info.getMessage());
			flags = progress.Info.TeleportFlags;

			logger.debug(GridClient.Log("TeleportProgress received, Message: " + teleportMessage + ", Flags: " + flags,
					_Client));
		} else if (packet.getType() == PacketType.TeleportFailed) {
			TeleportFailedPacket failed = (TeleportFailedPacket) packet;

			teleportMessage = Helpers.BytesToString(failed.Info.getReason());
			teleportStatus = TeleportStatus.Failed;
			finished = true;

			logger.debug(GridClient.Log("TeleportFailed received, Reason: " + teleportMessage, _Client));
		} else if (packet.getType() == PacketType.TeleportCancel) {
			// TeleportCancelPacket cancel = (TeleportCancelPacket)packet;

			teleportMessage = "Cancelled";
			teleportStatus = TeleportStatus.Cancelled;
			finished = true;

			logger.debug(GridClient.Log("TeleportCancel received from " + simulator.toString(), _Client));
		} else if (packet.getType() == PacketType.TeleportFinish) {
			TeleportFinishPacket finish = (TeleportFinishPacket) packet;

			flags = finish.Info.TeleportFlags;
			String seedcaps = Helpers.BytesToString(finish.Info.getSeedCapability());
			finished = true;

			logger.debug(GridClient.Log("TeleportFinish received, Flags: " + flags, _Client));

			// update home location if we are teleporting out of prelude - specific to
			// teleporting to welcome area
			if ((flags & TeleportFlags.SetHomeToTarget) != 0) {
				setHomePosRegion(finish.Info.RegionHandle, getAgentPosition());
			}

			// Connect to the new sim
			_Client.Network.getCurrentSim().AgentMovementComplete = false; // we're not there anymore
			InetAddress addr = InetAddress.getByAddress(Helpers.Int32ToBytesB(finish.Info.SimIP));
			SimulatorManager newSimulator = _Client.Network.connect(addr, finish.Info.SimPort, finish.Info.RegionHandle,
					true, seedcaps);

			if (newSimulator != null) {
				teleportMessage = "Teleport finished";
				teleportStatus = TeleportStatus.Finished;

				logger.info(GridClient.Log("Moved to new sim " + _Client.Network.getCurrentSim().getName() + " ("
						+ _Client.Network.getCurrentSim().getIPEndPoint().toString() + ")", _Client));
			} else {
				teleportMessage = "Failed to connect to the new sim after a teleport";
				teleportStatus = TeleportStatus.Failed;

				logger.error(GridClient.Log(teleportMessage, _Client));
			}
		} else if (packet.getType() == PacketType.TeleportLocal) {
			TeleportLocalPacket local = (TeleportLocalPacket) packet;

			teleportMessage = "Teleport finished";
			flags = local.Info.TeleportFlags;
			teleportStatus = TeleportStatus.Finished;
			relativePosition = local.Info.Position;
			_Movement.Camera.LookDirection(local.Info.LookAt);
			// This field is apparently not used for anything
			// local.Info.LocationID;
			finished = true;

			logger.debug(GridClient.Log("TeleportLocal received, Flags: " + flags, _Client));
		}
		OnTeleport.dispatch(new TeleportCallbackArgs(teleportMessage, teleportStatus, flags));
		if (finished) {
			teleportTimeout.set(teleportStatus);
		}
	}

	/**
	 * Process TeleportFailed message sent via CapsEventQueue, informs agent its
	 * last teleport has failed and why.
	 */
	private void HandleTeleportFailed(IMessage message, SimulatorManager simulator) {
		TeleportFailedMessage failed = (TeleportFailedMessage) message;
		OnTeleport.dispatch(new TeleportCallbackArgs(failed.reason, TeleportStatus.Failed, 0));
		teleportTimeout.set(TeleportStatus.Failed);
	}

	private void HandleTeleportFinish(IMessage message, SimulatorManager simulator) throws Exception {
		TeleportStatus teleportStatus = TeleportStatus.None;
		String teleportMessage = Helpers.EmptyString;
		TeleportFinishMessage msg = (TeleportFinishMessage) message;

		logger.debug(GridClient.Log("TeleportFinish received, Flags: " + msg.flags, _Client));

		// Connect to the new sim
		SimulatorManager newSimulator = _Client.Network.connect(msg.ip, (short) msg.port, msg.regionHandle, true,
				msg.seedCapability.toString());
		if (newSimulator != null) {
			teleportMessage = "Teleport finished";
			teleportStatus = TeleportStatus.Finished;

			logger.info(GridClient.Log("Moved to new sim " + _Client.Network.getCurrentSim().getName() + " ("
					+ _Client.Network.getCurrentSim().getIPEndPoint().toString() + ")", _Client));
		} else {
			teleportMessage = "Failed to connect to the new sim after a teleport";
			teleportStatus = TeleportStatus.Failed;

			logger.error(GridClient.Log(teleportMessage, _Client));
		}
		OnTeleport.dispatch(new TeleportCallbackArgs(teleportMessage, teleportStatus, msg.flags));
		teleportTimeout.set(teleportStatus);
	}

	private void HandleEstablishAgentComm(IMessage message, SimulatorManager simulator)
			throws InterruptedException, IOException {
		EstablishAgentCommunicationMessage msg = (EstablishAgentCommunicationMessage) message;

		if (_Client.Settings.getBool(LibSettings.MULTIPLE_SIMS)) {
			InetSocketAddress endPoint = new InetSocketAddress(msg.address, msg.port);
			SimulatorManager sim = _Client.Network.FindSimulator(endPoint);

			if (sim == null) {
				logger.error(GridClient.Log(
						"Got EstablishAgentCommunication for unknown sim " + msg.address + ":" + msg.port, _Client));

				// FIXME: Should we use this opportunity to connect to the simulator?
			} else {
				logger.debug(GridClient.Log("Got EstablishAgentCommunication for " + sim.getName(), _Client));

				sim.setSeedCaps(msg.seedCapability.toString());
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @throws UnsupportedEncodingException
	 */
	private void HandleAgentDataUpdate(Packet packet, Simulator sim) throws UnsupportedEncodingException {
		AgentDataUpdatePacket p = (AgentDataUpdatePacket) packet;
		SimulatorManager simulator = (SimulatorManager) sim;

		if (p.AgentData.AgentID.equals(simulator.getClient().Self.getAgentID())) {
			firstName = Helpers.BytesToString(p.AgentData.getFirstName());
			lastName = Helpers.BytesToString(p.AgentData.getLastName());
			activeGroup = p.AgentData.ActiveGroupID;
			activeGroupPowers = GroupPowers.setValue(p.AgentData.GroupPowers);

			if (OnAgentData.count() > 0) {
				String groupTitle = Helpers.BytesToString(p.AgentData.getGroupTitle());
				String groupName = Helpers.BytesToString(p.AgentData.getGroupName());

				OnAgentData.dispatch(new AgentDataReplyCallbackArgs(firstName, lastName, activeGroup, groupTitle,
						activeGroupPowers, groupName));
			}
		} else {
			logger.error(GridClient.Log("Got an AgentDataUpdate packet for avatar " + p.AgentData.AgentID.toString()
					+ " instead of " + _Client.Self.getAgentID().toString() + ", this shouldn't happen", _Client));
		}
	}

	private void HandleAgentStateUpdate(IMessage message, SimulatorManager simulator) {
		AgentStateStatus = (AgentStateUpdateMessage) message;
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @throws Exception
	 */
	private void HandleMoneyBalanceReply(Packet packet, Simulator simulator) throws Exception {
		if (packet.getType() == PacketType.MoneyBalanceReply) {
			MoneyBalanceReplyPacket reply = (MoneyBalanceReplyPacket) packet;
			int delta = balance - reply.MoneyData.MoneyBalance;
			balance = reply.MoneyData.MoneyBalance;
			OnBalanceUpdated.dispatch(new BalanceCallbackArgs(balance, delta, firstBalance));
			firstBalance = false;

			if (OnMoneyBalanceReply.count() > 0 && reply.TransactionInfo != null
					&& reply.TransactionInfo.TransactionType != 0) {
				TransactionInfo transactionInfo = new TransactionInfo();
				transactionInfo.TransactionType = reply.TransactionInfo.TransactionType;
				transactionInfo.SourceID = reply.TransactionInfo.SourceID;
				transactionInfo.IsSourceGroup = reply.TransactionInfo.IsSourceGroup;
				transactionInfo.DestID = reply.TransactionInfo.DestID;
				transactionInfo.IsDestGroup = reply.TransactionInfo.IsDestGroup;
				transactionInfo.Amount = reply.TransactionInfo.Amount;
				transactionInfo.ItemDescription = Helpers.BytesToString(reply.TransactionInfo.getItemDescription());

				OnMoneyBalanceReply.dispatch(new MoneyBalanceReplyCallbackArgs(reply.MoneyData.TransactionID,
						reply.MoneyData.TransactionSuccess, reply.MoneyData.MoneyBalance,
						reply.MoneyData.SquareMetersCredit, reply.MoneyData.SquareMetersCommitted,
						Helpers.BytesToString(reply.MoneyData.getDescription()), transactionInfo));
			}
		}
	}

	/**
	 * EQ Message fired with the result of SetDisplayName request
	 */
	private void HandleSetDisplayNameReply(IMessage message, SimulatorManager simulator) {
		SetDisplayNameReplyMessage msg = (SetDisplayNameReplyMessage) message;
		OnSetDisplayNameReply.dispatch(new SetDisplayNameReplyCallbackArgs(msg.status, msg.reason, msg.displayName));
	}

	private void HandleAvatarAnimation(Packet packet, Simulator simulator) throws Exception {
		AvatarAnimationPacket animation = (AvatarAnimationPacket) packet;

		if (animation.ID.equals(_Client.Self.getAgentID())) {
			synchronized (SignaledAnimations) {
				// Reset the signaled animation list
				SignaledAnimations.clear();

				for (int i = 0; i < animation.AnimationList.length; i++) {
					UUID animID = animation.AnimationList[i].AnimID;
					int sequenceID = animation.AnimationList[i].AnimSequenceID;

					// Add this animation to the list of currently signaled animations
					SignaledAnimations.put(animID, sequenceID);

					if (i < animation.ObjectID.length) {
						// FIXME: The server tells us which objects triggered our animations,
						// we should store this info

						// animation.ObjectID[i]
					}

					if (i < animation.PhysicalAvatarEventList.length) {
						AvatarAnimationPacket.PhysicalAvatarEventListBlock block = animation.PhysicalAvatarEventList[i];
						block.getTypeData();
						// FIXME: What is this?
					}

					if (sendAgentUpdates) {
						// We have to manually tell the server to stop playing some animations
						if (animID.equals(Animations.STANDUP) || animID.equals(Animations.PRE_JUMP)
								|| animID.equals(Animations.LAND) || animID.equals(Animations.MEDIUM_LAND)) {
							_Movement.setFinishAnim(true);
							_Movement.SendUpdate(true);
							_Movement.setFinishAnim(false);
						}
					}
				}
			}
		}

		if (OnAnimationsChanged.count() > 0) {
			_ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					OnAnimationsChanged.dispatch(new AnimationsChangedCallbackArgs(SignaledAnimations));
				}
			});
		}

	}

	private void HandleMeanCollisionAlert(Packet packet, Simulator simulator) {
		if (OnMeanCollision.count() > 0) {
			MeanCollisionAlertPacket collision = (MeanCollisionAlertPacket) packet;

			for (int i = 0; i < collision.MeanCollision.length; i++) {
				MeanCollisionAlertPacket.MeanCollisionBlock block = collision.MeanCollision[i];

				Date time = Helpers.UnixTimeToDateTime(block.Time);
				MeanCollisionType type = MeanCollisionType.setValue(block.Type);

				OnMeanCollision
						.dispatch(new MeanCollisionCallbackArgs(type, block.Perp, block.Victim, block.Mag, time));
			}
		}
	}

	/**
	 * Crossed region handler for message that comes across the CapsEventQueue. Sent
	 * to an agent when the agent crosses a sim border into a new region.
	 *
	 * @param message
	 *            IMessage object containing the deserialized data sent from the
	 *            simulator
	 * @param simulator
	 *            The <see cref="Simulator"/> which originated the packet
	 * @throws Exception
	 */
	private void HandleCrossedRegion(IMessage message, SimulatorManager simulator) throws Exception {
		CrossedRegionMessage crossed = (CrossedRegionMessage) message;
		HandleCrossedRegion(new InetSocketAddress(crossed.ip, crossed.port), crossed.regionHandle,
				crossed.seedCapability.toString());
	}

	private void HandleCrossedRegion(Packet packet, Simulator simulator) throws Exception {
		CrossedRegionPacket crossing = (CrossedRegionPacket) packet;
		InetSocketAddress endPoint = new InetSocketAddress(
				InetAddress.getByAddress(Helpers.Int32ToBytesB(crossing.RegionData.SimIP)),
				crossing.RegionData.SimPort);
		HandleCrossedRegion(endPoint, crossing.RegionData.RegionHandle,
				Helpers.BytesToString(crossing.RegionData.getSeedCapability()));
	}

	private void HandleCrossedRegion(InetSocketAddress endPoint, long regionHandle, String seedCap) throws Exception {
		logger.debug(GridClient.Log("Crossed in to new region area, attempting to connect to " + endPoint.toString(),
				_Client));

		SimulatorManager oldSim = _Client.Network.getCurrentSim();
		SimulatorManager newSim = _Client.Network.connect(endPoint, regionHandle, true, seedCap);

		if (newSim != null) {
			logger.info(GridClient.Log("Finished crossing over in to region " + newSim.toString(), _Client));
			oldSim.AgentMovementComplete = false; // We're no longer there
			OnRegionCrossed.dispatch(new RegionCrossedCallbackArgs(oldSim, newSim));
		} else {
			// The old simulator will still (poorly) handle our movement, so the connection
			// isn't completely shot yet
			logger.warn(GridClient
					.Log("Failed to connect to new region " + endPoint.toString() + " after crossing over", _Client));
		}
	}

	/**
	 * Group Chat event handler
	 *
	 * @param message
	 *            IMessage object containing the deserialized data sent from the
	 *            simulator
	 * @param simulator
	 *            The <see cref="Simulator"/> which originated the packet
	 * @throws Exception
	 */
	private void HandleChatterBoxSessionEventReply(IMessage message, SimulatorManager simulator) throws Exception {
		ChatterBoxSessionEventReplyMessage msg = (ChatterBoxSessionEventReplyMessage) message;

		if (!msg.success) {
			RequestJoinGroupChat(msg.sessionID);
			logger.info(GridClient.Log("Attempt to send group chat to non-existant session for group " + msg.sessionID,
					_Client));
		}
	}

	/**
	 * Response from request to join a group chat
	 *
	 * @param message
	 *            IMessage object containing the deserialized data sent from the
	 *            simulator
	 * @param simulator
	 *            The <see cref="Simulator"/> which originated the packet
	 */
	private void HandleChatterBoxSessionStartReply(IMessage message, SimulatorManager simulator) {
		ChatterBoxSessionStartReplyMessage msg = (ChatterBoxSessionStartReplyMessage) message;

		if (msg.success) {
			synchronized (GroupChatSessions) {
				if (!GroupChatSessions.containsKey(msg.sessionID))
					GroupChatSessions.put(msg.sessionID, new ArrayList<ChatSessionMember>());
			}
		}
		OnGroupChatJoined.dispatch(
				new GroupChatJoinedCallbackArgs(msg.sessionID, msg.sessionName, msg.tempSessionID, msg.success));
	}

	/**
	 * Someone joined or left group chat
	 *
	 * @param message
	 *            IMessage object containing the deserialized data sent from the
	 *            simulator
	 * @param simulator
	 *            The <see cref="Simulator"/> which originated the packet
	 */
	private void HandleChatterBoxSessionAgentListUpdates(IMessage message, SimulatorManager simulator) {
		ChatterBoxSessionAgentListUpdatesMessage msg = (ChatterBoxSessionAgentListUpdatesMessage) message;

		synchronized (GroupChatSessions) {
			if (!GroupChatSessions.containsKey(msg.sessionID))
				GroupChatSessions.put(msg.sessionID, new ArrayList<ChatSessionMember>());
		}

		for (int i = 0; i < msg.updates.length; i++) {
			ChatSessionMember fndMbr = null;
			synchronized (GroupChatSessions) {
				for (ChatSessionMember member : GroupChatSessions.get(msg.sessionID)) {
					if (member.AvatarKey.equals(msg.updates[i].agentID)) {
						fndMbr = member;
						break;
					}
				}
			}

			if (msg.updates[i].transition != null) {
				if (msg.updates[i].transition.equals("ENTER")) {
					if (fndMbr == null || fndMbr.AvatarKey.equals(UUID.Zero)) {
						fndMbr = new ChatSessionMember();
						fndMbr.AvatarKey = msg.updates[i].agentID;

						synchronized (GroupChatSessions) {
							GroupChatSessions.get(msg.sessionID).add(fndMbr);
						}
						OnChatSessionMember.dispatch(
								new ChatSessionMemberCallbackArgs(msg.sessionID, msg.updates[i].agentID, true));
					}
				} else if (msg.updates[i].transition.equals("LEAVE")) {
					if (fndMbr != null && !fndMbr.AvatarKey.equals(UUID.Zero)) {
						synchronized (GroupChatSessions) {
							GroupChatSessions.get(msg.sessionID).remove(fndMbr);
						}
						fndMbr = null;
					}
					OnChatSessionMember
							.dispatch(new ChatSessionMemberCallbackArgs(msg.sessionID, msg.updates[i].agentID, false));
				}
			}

			if (fndMbr != null) {
				// update existing member record
				synchronized (GroupChatSessions) {
					fndMbr.MuteText = msg.updates[i].muteText;
					fndMbr.MuteVoice = msg.updates[i].muteVoice;

					fndMbr.CanVoiceChat = msg.updates[i].canVoiceChat;
					fndMbr.IsModerator = msg.updates[i].isModerator;
				}
			}
		}
	}

	/**
	 * Handle a group chat Invitation
	 *
	 * @param message
	 *            IMessage object containing the deserialized data sent from the
	 *            simulator
	 * @param simulator
	 *            The <see cref="Simulator"/> which originated the packet
	 */
	private void HandleChatterBoxInvitation(IMessage message, SimulatorManager simulator) {
		if (OnInstantMessage.count() > 0) {
			ChatterBoxInvitationMessage msg = (ChatterBoxInvitationMessage) message;

			// TODO: do something about invitations to voice group chat/friends conference
			// Skip for now
			if (msg.voice)
				return;

			InstantMessage im = new InstantMessage();

			im.FromAgentID = msg.fromAgentID;
			im.FromAgentName = msg.fromAgentName;
			im.ToAgentID = msg.toAgentID;
			im.ParentEstateID = msg.parentEstateID;
			im.RegionID = msg.regionID;
			im.Position = msg.position;
			im.Dialog = msg.dialog;
			im.GroupIM = msg.groupIM;
			im.IMSessionID = msg.imSessionID;
			im.Timestamp = msg.timestamp;
			im.Message = msg.message;
			im.Offline = msg.offline;
			im.BinaryBucket = msg.binaryBucket;
			try {
				ChatterBoxAcceptInvite(msg.imSessionID);
			} catch (Exception ex) {
				logger.warn(GridClient.Log("Failed joining IM:", _Client), ex);
			}
			OnInstantMessage.dispatch(new InstantMessageCallbackArgs(im, simulator));
		}
	}

	/**
	 * Moderate a chat session
	 *
	 * @param sessionID
	 *            the <see cref="UUID"/> of the session to moderate, for group chats
	 *            this will be the groups UUID
	 * @param memberID
	 *            the <see cref="UUID"/> of the avatar to moderate
	 * @param key
	 *            Either "voice" to moderate users voice, or "text" to moderate
	 *            users text session
	 * @param moderate
	 *            true to moderate (silence user), false to allow avatar to speak
	 * @throws Exception
	 */
	public void ModerateChatSessions(UUID sessionID, UUID memberID, String key, boolean moderate) throws Exception {
		URI url = _Client.Network.getCapabilityURI(CapsEventType.ChatSessionRequest.toString());
		if (url != null) {
			ChatSessionRequestMuteUpdate req = _Client.Messages.new ChatSessionRequestMuteUpdate();

			req.requestKey = key;
			req.requestValue = moderate;
			req.sessionID = sessionID;
			req.agentID = memberID;

			CapsClient request = new CapsClient(_Client, CapsEventType.ChatSessionRequest.toString());
			request.getResponse(url, req, null, _Client.Settings.CAPS_TIMEOUT);
		} else {
			throw new Exception("ChatSessionRequest capability is not currently available");
		}
	}

	private void HandleAlertMessage(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		AlertMessagePacket alert = (AlertMessagePacket) packet;
		OnAlertMessage.dispatch(new AlertMessageCallbackArgs(Helpers.BytesToString(alert.AlertData.getMessage())));
	}

	private void HandleGenericMessage(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		GenericMessagePacket message = (GenericMessagePacket) packet;

		if (message.AgentData.AgentID.equals(agentID)) {
			String method = Helpers.BytesToString(message.MethodData.getMethod());
			if (method.equals("emptymutelist")) {
				synchronized (MuteList) {
					MuteList.clear();
				}
			} else {
				logger.info(GridClient
						.Log("GenericMessage: " + method + ", " + message.ParamList.length + " parameter(s)", _Client));

				List<String> parameters = new ArrayList<String>(message.ParamList.length);
				for (GenericMessagePacket.ParamListBlock block : message.ParamList) {
					parameters.add(Helpers.BytesToString(block.getParameter()));
				}
				OnGenericMessage.dispatch(new GenericMessageCallbackArgs(message.AgentData.SessionID,
						message.AgentData.TransactionID, Helpers.BytesToString(message.MethodData.getMethod()),
						message.MethodData.Invoice, parameters));
			}
		}
	}

	private void HandleCameraConstraint(Packet packet, Simulator simulator) {
		if (OnCameraConstraint.count() > 0) {
			CameraConstraintPacket camera = (CameraConstraintPacket) packet;
			OnCameraConstraint.dispatch(new CameraConstraintCallbackArgs(camera.Plane));
		}
	}

	private void HandleScriptControlChange(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (OnScriptControlChange.count() > 0) {
			ScriptControlChangePacket reply = (ScriptControlChangePacket) packet;

			for (int i = 0; i < reply.Data.length; i++) {
				ScriptControlChangePacket.DataBlock block = reply.Data[i];

				OnScriptControlChange.dispatch(new ScriptControlChangeCallbackArgs(
						ScriptControlChange.getValue(block.Controls), block.PassToAgent, block.TakeControls));
			}
		}
	}

	private void HandleScriptSensorReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (OnScriptSensorReply.count() > 0) {
			ScriptSensorReplyPacket reply = (ScriptSensorReplyPacket) packet;

			for (int i = 0; i < reply.SensedData.length; i++) {
				ScriptSensorReplyPacket.SensedDataBlock block = reply.SensedData[i];

				OnScriptSensorReply.dispatch(new ScriptSensorReplyCallbackArgs(reply.SourceID, block.GroupID,
						Helpers.BytesToString(block.getName()), block.ObjectID, block.OwnerID, block.Position,
						block.Range, block.Rotation, ScriptSensorTypeFlags.setValue(block.Type), block.Velocity));
			}
		}
	}

	private void HandleAvatarSitResponse(Packet packet, Simulator simulator) {
		if (OnAvatarSitResponse.count() > 0) {
			AvatarSitResponsePacket sit = (AvatarSitResponsePacket) packet;

			OnAvatarSitResponse.dispatch(new AvatarSitResponseCallbackArgs(sit.ID, sit.SitTransform.AutoPilot,
					sit.SitTransform.CameraAtOffset, sit.SitTransform.CameraEyeOffset, sit.SitTransform.ForceMouselook,
					sit.SitTransform.SitPosition, sit.SitTransform.SitRotation));
		}
	}

	private void HandleMuteListUpdate(Packet packet, Simulator simulator) throws Exception {
		MuteListUpdatePacket data = (MuteListUpdatePacket) packet;
		if (data.MuteData.AgentID.equals(_Client.Self.getAgentID())) {
			return;
		}

		String fileName = Helpers.BytesToString(data.MuteData.getFilename());
		final TimeoutEvent<byte[]> gotMuteList = new TimeoutEvent<byte[]>();
		final AtomicLong xferID = new AtomicLong();

		Callback<XferDownload> xferCallback = new Callback<XferDownload>() {
			@Override
			public boolean callback(XferDownload download) {
				if (download.XferID == xferID.get()) {
					gotMuteList.set(download.AssetData);
				}
				return false;
			}
		};

		_Client.Assets.OnXferReceived.add(xferCallback, true);
		xferID.set(_Client.Assets.RequestAssetXfer(fileName, true, false, UUID.Zero, AssetType.Unknown, true));

		byte[] assetData = gotMuteList.waitOne(60 * 1000);
		if (assetData != null) {
			String muteList = Helpers.BytesToString(assetData);

			synchronized (MuteList) {
				MuteList.clear();
				for (String line : muteList.split("\n")) {
					if (line.trim().isEmpty())
						continue;

					try {
						Matcher m;
						if ((m = Pattern
								.compile("(?<MyteType>\\d+)\\s+(?<Key>[a-zA-Z0-9-]+)\\s+(?<Name>[^|]+)|(?<Flags>.+)")
								.matcher(line)).matches()) {
							MuteEntry me = new MuteEntry();
							me.Type = MuteType.setValue(Integer.valueOf(m.group(1)));
							me.ID = new UUID(m.group(2));
							me.Name = m.group(3);
							me.Flags = MuteFlags.setValue(Helpers.TryParseInt(m.group(4)));
							MuteList.put(String.format("%s|%s", me.ID, me.Name), me);
						} else {
							throw new IllegalArgumentException("Invalid mutelist entry line");
						}
					} catch (Exception ex) {
						logger.warn(GridClient.Log("Failed to parse the mute list line: " + line, _Client), ex);
					}
				}
			}
			OnMuteListUpdated.dispatch(null);
		} else {
			logger.warn(GridClient.Log("Timed out waiting for mute list download", _Client));
		}
		_Client.Assets.OnXferReceived.remove(xferCallback);
	}

	/*
	 * Agent movement and camera control
	 *
	 * Agent movement is controlled by setting specific {@link
	 * T:AgentManager.ControlFlags} After the control flags are set, An AgentUpdate
	 * is required to update the simulator of the specified flags This is most
	 * easily accomplished by setting one or more of the AgentMovement properties
	 *
	 * Movement of an avatar is always based on a compass direction, for example
	 * AtPos will move the agent from West to East or forward on the X Axis, AtNeg
	 * will of course move agent from East to West or backward on the X Axis,
	 * LeftPos will be South to North or forward on the Y Axis The Z axis is Up,
	 * finer grained control of movements can be done using the Nudge properties
	 */
	public class AgentMovement {
		public class CoordinateFrame {
			/* Origin position of this coordinate frame */
			public final Vector3 getOrigin() {
				return origin;
			}

			public final void setOrigin(Vector3 value) throws Exception {
				if (!value.isFinite()) {
					throw new Exception("Non-finite in CoordinateFrame.Origin assignment");
				}
				origin = value;
			}

			/* X axis of this coordinate frame, or Forward/At in grid terms */
			public final Vector3 getXAxis() {
				return xAxis;
			}

			public final void setXAxis(Vector3 value) throws Exception {
				if (!value.isFinite()) {
					throw new Exception("Non-finite in CoordinateFrame.XAxis assignment");
				}
				xAxis = value;
			}

			/* Y axis of this coordinate frame, or Left in grid terms */
			public final Vector3 getYAxis() {
				return yAxis;
			}

			public final void setYAxis(Vector3 value) throws Exception {
				if (!value.isFinite()) {
					throw new Exception("Non-finite in CoordinateFrame.YAxis assignment");
				}
				yAxis = value;
			}

			/* Z axis of this coordinate frame, or Up in grid terms */
			public final Vector3 getZAxis() {
				return zAxis;
			}

			public final void setZAxis(Vector3 value) throws Exception {
				if (!value.isFinite()) {
					throw new Exception("Non-finite in CoordinateFrame.ZAxis assignment");
				}
				zAxis = value;
			}

			protected Vector3 origin;
			protected Vector3 xAxis;
			protected Vector3 yAxis;
			protected Vector3 zAxis;

			public CoordinateFrame(Vector3 origin) throws Exception {
				this.origin = origin;
				xAxis = X_AXIS;
				yAxis = Y_AXIS;
				zAxis = Z_AXIS;

				if (!this.origin.isFinite()) {
					throw new Exception("Non-finite in CoordinateFrame constructor");
				}
			}

			public CoordinateFrame(Vector3 origin, Vector3 direction) throws Exception {
				this.origin = origin;
				LookDirection(direction);

				if (!IsFinite()) {
					throw new Exception("Non-finite in CoordinateFrame constructor");
				}
			}

			public CoordinateFrame(Vector3 origin, Vector3 xAxis, Vector3 yAxis, Vector3 zAxis) throws Exception {
				this.origin = origin;
				this.xAxis = xAxis;
				this.yAxis = yAxis;
				this.zAxis = zAxis;

				if (!IsFinite()) {
					throw new Exception("Non-finite in CoordinateFrame constructor");
				}
			}

			public CoordinateFrame(Vector3 origin, Matrix4 rotation) throws Exception {
				this.origin = origin;
				xAxis = rotation.getAtAxis();
				yAxis = rotation.getLeftAxis();
				zAxis = rotation.getUpAxis();

				if (!IsFinite()) {
					throw new Exception("Non-finite in CoordinateFrame constructor");
				}
			}

			public CoordinateFrame(Vector3 origin, Quaternion rotation) throws Exception {
				Matrix4 m = Matrix4.createFromQuaternion(rotation);

				this.origin = origin;
				xAxis = m.getAtAxis();
				yAxis = m.getLeftAxis();
				zAxis = m.getUpAxis();

				if (!IsFinite()) {
					throw new Exception("Non-finite in CoordinateFrame constructor");
				}
			}

			public final void ResetAxes() {
				xAxis = X_AXIS;
				yAxis = Y_AXIS;
				zAxis = Z_AXIS;
			}

			public final void Rotate(float angle, Vector3 rotationAxis) throws Exception {
				Quaternion q = Quaternion.createFromAxisAngle(rotationAxis, angle);
				Rotate(q);
			}

			public final void Rotate(Quaternion q) throws Exception {
				Matrix4 m = Matrix4.createFromQuaternion(q);
				Rotate(m);
			}

			public final void Rotate(Matrix4 m) throws Exception {
				xAxis = Vector3.transform(xAxis, m);
				yAxis = Vector3.transform(yAxis, m);

				Orthonormalize();

				if (!IsFinite()) {
					throw new Exception("Non-finite in CoordinateFrame.Rotate()");
				}
			}

			public final void Roll(float angle) throws Exception {
				Quaternion q = Quaternion.createFromAxisAngle(xAxis, angle);
				Matrix4 m = Matrix4.createFromQuaternion(q);
				Rotate(m);

				if (!yAxis.isFinite() || !zAxis.isFinite()) {
					throw new Exception("Non-finite in CoordinateFrame.Roll()");
				}
			}

			public final void Pitch(float angle) throws Throwable {
				Quaternion q = Quaternion.createFromAxisAngle(yAxis, angle);
				Matrix4 m = Matrix4.createFromQuaternion(q);
				Rotate(m);

				if (!xAxis.isFinite() || !zAxis.isFinite()) {
					throw new Throwable("Non-finite in CoordinateFrame.Pitch()");
				}
			}

			public final void Yaw(float angle) throws Throwable {
				Quaternion q = Quaternion.createFromAxisAngle(zAxis, angle);
				Matrix4 m = Matrix4.createFromQuaternion(q);
				Rotate(m);

				if (!xAxis.isFinite() || !yAxis.isFinite()) {
					throw new Throwable("Non-finite in CoordinateFrame.Yaw()");
				}
			}

			public final void LookDirection(Vector3 at) {
				LookDirection(at, Z_AXIS);
			}

			/**
			 * @param at
			 *            Looking direction, must be a normalized vector
			 * @param upDirection
			 *            Up direction, must be a normalized vector
			 */
			public final void LookDirection(Vector3 at, Vector3 upDirection) {
				// The two parameters cannot be parallel
				Vector3 left = Vector3.cross(upDirection, at);
				if (left == Vector3.Zero) {
					// Prevent left from being zero
					at.X += 0.01f;
					at.normalize();
					left = Vector3.cross(upDirection, at);
				}
				left.normalize();

				xAxis = at;
				yAxis = left;
				zAxis = Vector3.cross(at, left);
			}

			/**
			 * Align the coordinate frame X and Y axis with a given rotation around the Z
			 * axis in radians
			 *
			 * @param heading
			 *            Absolute rotation around the Z axis in radians
			 */
			public final void LookDirection(double heading) {
				yAxis.X = (float) Math.cos(heading);
				yAxis.Y = (float) Math.sin(heading);
				xAxis.X = (float) -Math.sin(heading);
				xAxis.Y = (float) Math.cos(heading);
			}

			public final void LookAt(Vector3 origin, Vector3 target) {
				LookAt(origin, target, new Vector3(0f, 0f, 1f));
			}

			public final void LookAt(Vector3 origin, Vector3 target, Vector3 upDirection) {
				this.origin = origin;
				Vector3 at = target.subtract(origin);
				at.normalize();

				LookDirection(at, upDirection);
			}

			protected final boolean IsFinite() {
				if (xAxis.isFinite() && yAxis.isFinite() && zAxis.isFinite()) {
					return true;
				}
				return false;
			}

			protected final void Orthonormalize() {
				// Make sure the axis are orthagonal and normalized
				xAxis.normalize();
				yAxis.subtract(Vector3.multiply(xAxis, Vector3.multiply(xAxis, yAxis)));
				yAxis.normalize();
				zAxis = Vector3.cross(xAxis, yAxis);
			}
		}

		/* Move agent positive along the X axis */
		public final boolean getAtPos() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_AT_POS);
		}

		public final void setAtPos(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_AT_POS, value);
		}

		/* Move agent negative along the X axis */
		public final boolean getAtNeg() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_AT_NEG);
		}

		public final void setAtNeg(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_AT_NEG, value);
		}

		/* Move agent positive along the Y axis */
		public final boolean getLeftPos() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_LEFT_POS);
		}

		public final void setLeftPos(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_LEFT_POS, value);
		}

		/* Move agent negative along the Y axis */
		public final boolean getLeftNeg() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_LEFT_NEG);
		}

		public final void setLeftNeg(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_LEFT_NEG, value);
		}

		/* Move agent positive along the Z axis */
		public final boolean getUpPos() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_UP_POS);
		}

		public final void setUpPos(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_UP_POS, value);
		}

		/* Move agent negative along the Z axis */
		public final boolean getUpNeg() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_UP_NEG);
		}

		public final void setUpNeg(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_UP_NEG, value);
		}

		public final boolean getPitchPos() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_PITCH_POS);
		}

		public final void setPitchPos(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_PITCH_POS, value);
		}

		public final boolean getPitchNeg() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_PITCH_NEG);
		}

		public final void setPitchNeg(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_PITCH_NEG, value);
		}

		public final boolean getYawPos() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_YAW_POS);
		}

		public final void setYawPos(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_YAW_POS, value);
		}

		public final boolean getYawNeg() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_YAW_NEG);
		}

		public final void setYawNeg(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_YAW_NEG, value);
		}

		public final boolean getFastAt() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_FAST_AT);
		}

		public final void setFastAt(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_FAST_AT, value);
		}

		public final boolean getFastLeft() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_FAST_LEFT);
		}

		public final void setFastLeft(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_FAST_LEFT, value);
		}

		public final boolean getFastUp() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_FAST_UP);
		}

		public final void setFastUp(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_FAST_UP, value);
		}

		/* Causes simulator to make agent fly */
		public final boolean getFly() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_FLY);
		}

		public final void setFly(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_FLY, value);
		}

		/* Stop movement */
		public final boolean getStop() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_STOP);
		}

		public final void setStop(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_STOP, value);
		}

		/* Finish animation */
		public final boolean getFinishAnim() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_FINISH_ANIM);
		}

		public final void setFinishAnim(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_FINISH_ANIM, value);
		}

		/* Stand up from a sit */
		public final boolean getStandUp() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_STAND_UP);
		}

		public final void setStandUp(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_STAND_UP, value);
		}

		/* Tells simulator to sit agent on ground */
		public final boolean getSitOnGround() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_SIT_ON_GROUND);
		}

		public final void setSitOnGround(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_SIT_ON_GROUND, value);
		}

		/* Place agent into mouselook mode */
		public final boolean getMouselook() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_MOUSELOOK);
		}

		public final void setMouselook(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_MOUSELOOK, value);
		}

		/* Nudge agent positive along the X axis */
		public final boolean getNudgeAtPos() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_AT_POS);
		}

		public final void setNudgeAtPos(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_AT_POS, value);
		}

		/* Nudge agent negative along the X axis */
		public final boolean getNudgeAtNeg() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_AT_NEG);
		}

		public final void setNudgeAtNeg(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_AT_NEG, value);
		}

		/* Nudge agent positive along the Y axis */
		public final boolean getNudgeLeftPos() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_LEFT_POS);
		}

		public final void setNudgeLeftPos(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_LEFT_POS, value);
		}

		/* Nudge agent negative along the Y axis */
		public final boolean getNudgeLeftNeg() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_LEFT_NEG);
		}

		public final void setNudgeLeftNeg(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_LEFT_NEG, value);
		}

		/* Nudge agent positive along the Z axis */
		public final boolean getNudgeUpPos() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_UP_POS);
		}

		public final void setNudgeUpPos(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_UP_POS, value);
		}

		/* Nudge agent negative along the Z axis */
		public final boolean getNudgeUpNeg() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_UP_NEG);
		}

		public final void setNudgeUpNeg(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_NUDGE_UP_NEG, value);
		}

		public final boolean getTurnLeft() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_TURN_LEFT);
		}

		public final void setTurnLeft(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_TURN_LEFT, value);
		}

		public final boolean getTurnRight() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_TURN_RIGHT);
		}

		public final void setTurnRight(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_TURN_RIGHT, value);
		}

		/* Tell simulator to mark agent as away */
		public final boolean getAway() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_AWAY);
		}

		public final void setAway(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_AWAY, value);
		}

		public final boolean getLButtonDown() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_LBUTTON_DOWN);
		}

		public final void setLButtonDown(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_LBUTTON_DOWN, value);
		}

		public final boolean getLButtonUp() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_LBUTTON_UP);
		}

		public final void setLButtonUp(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_LBUTTON_UP, value);
		}

		public final boolean getMLButtonDown() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_ML_LBUTTON_DOWN);
		}

		public final void setMLButtonDown(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_ML_LBUTTON_DOWN, value);
		}

		public final boolean getMLButtonUp() {
			return GetControlFlag(ControlFlags.AGENT_CONTROL_ML_LBUTTON_UP);
		}

		public final void setMLButtonUp(boolean value) {
			SetControlFlag(ControlFlags.AGENT_CONTROL_ML_LBUTTON_UP, value);
		}

		/*
		 * Returns "always run" value, or changes it by sending a SetAlwaysRunPacket
		 */
		public final boolean getAlwaysRun() {
			return alwaysRun;
		}

		public final void setAlwaysRun(boolean value) throws Exception {
			alwaysRun = value;
			SetAlwaysRunPacket run = new SetAlwaysRunPacket();
			run.AgentData.AgentID = _Client.Self.getAgentID();
			run.AgentData.SessionID = _Client.Self.getSessionID();
			run.AgentData.AlwaysRun = alwaysRun;
			_Client.Network.sendPacket(run);
		}

		/* The current value of the agent control flags */
		public final int getAgentControls() {
			return ControlFlags.getValue(agentControls);
		}

		/*
		 * Gets or sets the interval in milliseconds at which AgentUpdate packets are
		 * sent to the current simulator. Setting this to a non-zero value will also
		 * enable the packet sending if it was previously off, and setting it to zero
		 * will disable
		 */
		public final int getUpdateInterval() {
			return updateInterval;
		}

		public final void setUpdateInterval(int value) {
			if (value > 0) {
				if (updateTimer != null) {
					updateTask.cancel();
					updateTask = new UpdateTimerTask();
					updateTimer.scheduleAtFixedRate(updateTask, updateInterval, updateInterval);
				}
				updateInterval = value;
			} else {
				if (updateTimer != null) {
					updateTask.cancel();
					updateTask = null;
				}
				updateInterval = 0;
			}
		}

		/*
		 * Gets or sets whether AgentUpdate packets are sent to the current simulator
		 */
		public final boolean getUpdateEnabled() {
			return (updateInterval != 0);
		}

		/* Reset movement controls every time we send an update */
		public final boolean getAutoResetControls() {
			return autoResetControls;
		}

		public final void setAutoResetControls(boolean value) {
			autoResetControls = value;
		}

		// #endregion Properties

		// Agent camera controls
		public AgentCamera Camera;
		// Currently only used for hiding your group title
		public AgentFlags Flags = AgentFlags.None;
		// Action state of the avatar, which can currently be typing and editing
		public byte State;
		public Quaternion BodyRotation = Quaternion.Identity;
		public Quaternion HeadRotation = Quaternion.Identity;

		// /#region Change tracking
		private Quaternion LastBodyRotation;
		private Quaternion LastHeadRotation;
		private Vector3 LastCameraCenter;
		private Vector3 LastCameraXAxis;
		private Vector3 LastCameraYAxis;
		private Vector3 LastCameraZAxis;
		private float LastFar;

		private boolean alwaysRun;
		private GridClient Client;

		private int agentControls;
		private int duplicateCount;
		private int lastState;
		/* Timer for sending AgentUpdate packets */
		private Timer updateTimer;
		private TimerTask updateTask;
		private int updateInterval;
		private boolean autoResetControls;

		/* Default constructor */
		public AgentMovement(GridClient client) {
			Client = client;
			Camera = new AgentCamera();
			_Client.Network.OnDisconnected.add(new Network_OnDisconnected());
			updateInterval = LibSettings.DEFAULT_AGENT_UPDATE_INTERVAL;
		}

		private void CleanupTimer() {
			if (updateTimer != null) {
				updateTimer.cancel();
				updateTimer = null;
				updateTask = null;
			}
		}

		private class Network_OnDisconnected implements Callback<DisconnectedCallbackArgs> {
			@Override
			public boolean callback(DisconnectedCallbackArgs e) {
				CleanupTimer();
				return true;
			}
		}

		private class UpdateTimerTask extends TimerTask {
			@Override
			public void run() {
				if (_Client.Network.getConnected() && sendAgentUpdates && _Client.Network.getCurrentSim() != null) {
					// Send an AgentUpdate packet
					try {
						SendUpdate(false, _Client.Network.getCurrentSim());
					} catch (Exception e) {
					}
				}
			}
		}

		public void ResetTimer() {
			CleanupTimer();
			updateTimer = new Timer("UpdateTimer");
			updateTask = new UpdateTimerTask();
			updateTimer.scheduleAtFixedRate(updateTask, updateInterval, updateInterval);
		}

		/**
		 * Send an AgentUpdate with the camera set at the current agent
		 *
		 * @param heading
		 *            Camera rotation in radians
		 * @param reliable
		 *            Whether to send the AgentUpdate reliable or not
		 * @throws Exception
		 */
		public final void UpdateFromHeading(double heading, boolean reliable) throws Exception {
			Camera.setPosition(getAgentPosition());
			Camera.LookDirection(heading);

			BodyRotation.Z = (float) Math.sin(heading / 2.0d);
			BodyRotation.W = (float) Math.cos(heading / 2.0d);
			HeadRotation = BodyRotation;

			SendUpdate(reliable, _Client.Network.getCurrentSim());
		}

		/**
		 * Rotates the avatar body and camera toward a target position. This This will
		 * also anchor the camera position on the avatar
		 *
		 * @param target
		 *            Region coordinates to turn toward
		 * @return True for success, false otherwise
		 * @throws Exception
		 */
		public final boolean TurnToward(Vector3 target) throws Exception {
			return TurnToward(target, true);
		}

		/**
		 * Rotates the avatar body and camera toward a target position. This will also
		 * anchor the camera position on the avatar
		 *
		 * @param target
		 *            Region coordinates to turn toward
		 * @param sendUpdate
		 *            wether to send update or not
		 * @return True for success, false otherwise
		 * @throws Exception
		 */
		public boolean TurnToward(Vector3 target, boolean sendUpdate) throws Exception {
			if (sendAgentUpdates) {
				Quaternion parentRot = Quaternion.Identity;

				if (_Client.Self.sittingOn > 0) {
					synchronized (_Client.Network.getCurrentSim().getObjectsPrimitives()) {
						if (_Client.Network.getCurrentSim().getObjectsPrimitives().containsKey(sittingOn)) {
							parentRot = _Client.Network.getCurrentSim().getObjectsPrimitives().get(sittingOn).rotation;
						} else {
							logger.warn(GridClient.Log("Attempted TurnToward but parent prim is not in dictionary",
									Client));
							return false;
						}
					}
				}

				Quaternion between = Vector3.rotationBetween(Vector3.UnitX,
						Vector3.normalize(target.subtract(getAgentPosition())));
				Quaternion rot = Quaternion.multiply(between, Quaternion.divide(Quaternion.Identity, parentRot));

				BodyRotation = rot;
				HeadRotation = rot;
				Camera.LookAt(getAgentPosition(), target);

				if (sendUpdate)
					SendUpdate(false, _Client.Network.getCurrentSim());

				return true;
			}

			logger.warn(GridClient.Log("Attempted TurnToward but agent updates are disabled", Client));
			return false;
		}

		/**
		 * Send new AgentUpdate packet to update our current camera position and
		 * rotation
		 *
		 * @throws Exception
		 */
		public final void SendUpdate() throws Exception {
			SendUpdate(false, Client.Network.getCurrentSim());
		}

		/**
		 * Send new AgentUpdate packet to update our current camera position and
		 * rotation
		 *
		 * @param reliable
		 *            Whether to require server acknowledgement of this packet
		 * @throws Exception
		 */
		public final void SendUpdate(boolean reliable) throws Exception {
			SendUpdate(reliable, Client.Network.getCurrentSim());
		}

		/**
		 * Send new AgentUpdate packet to update our current camera position and
		 * rotation
		 *
		 * @param reliable
		 *            Whether to require server acknowledgement of this packet
		 * @param simulator
		 *            Simulator to send the update to
		 * @throws Exception
		 */
		public final void SendUpdate(boolean reliable, SimulatorManager simulator) throws Exception {
			// Since version 1.40.4 of the Linden simulator, sending this update
			// causes corruption of the agent position in the simulator
			if (simulator != null && (!simulator.AgentMovementComplete))
				return;

			Vector3 origin = Camera.getPosition();
			Vector3 xAxis = Camera.getAtAxis();
			Vector3 yAxis = Camera.getLeftAxis();
			Vector3 zAxis = Camera.getUpAxis();

			// Attempted to sort these in a rough order of how often they might change
			if (agentControls == 0 && yAxis.equals(LastCameraYAxis) && origin.equals(LastCameraCenter)
					&& State == lastState && HeadRotation.equals(LastHeadRotation)
					&& BodyRotation.equals(LastBodyRotation) && xAxis.equals(LastCameraXAxis) && Camera.Far == LastFar
					&& zAxis.equals(LastCameraZAxis)) {
				++duplicateCount;
			} else {
				duplicateCount = 0;
			}

			if (_Client.Settings.DISABLE_AGENT_UPDATE_DUPLICATE_CHECK || duplicateCount < 10) {
				// Store the current state to do duplicate checking
				LastHeadRotation = HeadRotation;
				LastBodyRotation = BodyRotation;
				LastCameraCenter = origin;
				LastCameraXAxis = xAxis;
				LastCameraYAxis = yAxis;
				LastCameraZAxis = zAxis;
				LastFar = Camera.Far;
				lastState = State;

				SendUpdate(simulator, agentControls, origin, xAxis, yAxis, zAxis, BodyRotation, HeadRotation,
						Camera.Far, Flags, State, reliable);

				if (autoResetControls) {
					ResetControlFlags();
				}
			}
		}

		/**
		 * Builds an AgentUpdate packet entirely from parameters. This will not touch
		 * the state of Self.Movement or Self.Movement.Camera in any way
		 *
		 * @param controlFlags
		 * @param origin
		 * @param forwardAxis
		 * @param leftAxis
		 * @param upAxis
		 * @param bodyRotation
		 * @param headRotation
		 * @param farClip
		 * @param flags
		 * @param state
		 * @param reliable
		 * @throws Exception
		 */
		public final void SendManualUpdate(int controlFlags, Vector3 origin, Vector3 forwardAxis, Vector3 leftAxis,
				Vector3 upAxis, Quaternion bodyRotation, Quaternion headRotation, float farClip, AgentFlags flags,
				byte state, boolean reliable) throws Exception {
			SimulatorManager simulator = _Client.Network.getCurrentSim();

			// Since version 1.40.4 of the Linden simulator, sending this update
			// causes corruption of the agent position in the simulator
			if (simulator == null || (!simulator.AgentMovementComplete)) {
				return;
			}
			SendUpdate(simulator, controlFlags, origin, forwardAxis, leftAxis, upAxis, bodyRotation, headRotation,
					farClip, flags, state, reliable);
		}

		private final void SendUpdate(SimulatorManager simulator, int controlFlags, Vector3 origin, Vector3 forwardAxis,
				Vector3 leftAxis, Vector3 upAxis, Quaternion bodyRotation, Quaternion headRotation, float farClip,
				AgentFlags flags, byte state, boolean reliable) throws Exception {
			AgentUpdatePacket update = new AgentUpdatePacket();

			update.AgentData.AgentID = _Client.Self.getAgentID();
			update.AgentData.SessionID = _Client.Self.getSessionID();
			update.AgentData.BodyRotation = bodyRotation;
			update.AgentData.HeadRotation = headRotation;
			update.AgentData.CameraCenter = origin;
			update.AgentData.CameraAtAxis = forwardAxis;
			update.AgentData.CameraLeftAxis = leftAxis;
			update.AgentData.CameraUpAxis = upAxis;
			update.AgentData.Far = farClip;
			update.AgentData.ControlFlags = controlFlags;
			update.AgentData.Flags = flags.getValue();
			update.AgentData.State = state;

			update.getHeader().setReliable(reliable);

			simulator.sendPacket(update);
		}

		private boolean GetControlFlag(int flag) {
			return ((agentControls & flag) != 0);
		}

		private void SetControlFlag(int flag, boolean value) {
			if (value) {
				agentControls |= flag;
			} else {
				agentControls &= ~flag;
			}
		}

		public void ResetControlFlags() {
			// Reset all of the flags except for persistent settings like
			// away, fly, mouselook, and crouching
			agentControls &= (ControlFlags.AGENT_CONTROL_AWAY & ControlFlags.AGENT_CONTROL_FLY
					& ControlFlags.AGENT_CONTROL_MOUSELOOK & ControlFlags.AGENT_CONTROL_UP_NEG);
		}

		/**
		 * Sends update of Field of Vision vertical angle to the simulator
		 *
		 * @param angle
		 *            Angle in radians
		 * @throws Exception
		 */
		public void setFOVVerticalAngle(float angle) throws Exception {
			AgentFOVPacket msg = new AgentFOVPacket();
			msg.AgentData.AgentID = Client.Self.getAgentID();
			msg.AgentData.SessionID = Client.Self.getSessionID();
			msg.AgentData.CircuitCode = Client.Network.getCircuitCode();
			msg.FOVBlock.GenCounter = 0;
			msg.FOVBlock.VerticalAngle = angle;
			Client.Network.sendPacket(msg);
		}

		/*
		 * Camera controls for the agent, mostly a thin wrapper around CoordinateFrame.
		 * This class is only responsible for state tracking and math, it does not send
		 * any packets
		 */
		public class AgentCamera {
			public float Far;

			// The camera is a local frame of reference inside of
			// the larger grid space. This is where the math happens
			private CoordinateFrame Frame;

			public final Vector3 getPosition() {
				return Frame.getOrigin();
			}

			public final void setPosition(Vector3 value) throws Exception {
				Frame.setOrigin(value);
			}

			public final Vector3 getAtAxis() {
				return Frame.getYAxis();
			}

			public final void setAtAxis(Vector3 value) throws Exception {
				Frame.setYAxis(value);
			}

			public final Vector3 getLeftAxis() {
				return Frame.getXAxis();
			}

			public final void setLeftAxis(Vector3 value) throws Exception {
				Frame.setXAxis(value);
			}

			public final Vector3 getUpAxis() {
				return Frame.getZAxis();
			}

			public final void setUpAxis(Vector3 value) throws Exception {
				Frame.setZAxis(value);
			}

			// Default constructor
			public AgentCamera() {
				try {
					Frame = new CoordinateFrame(new Vector3(128f, 128f, 20f));
				} catch (Exception e) {
				}
				Far = 128f;
			}

			public final void Roll(float angle) throws Exception {
				Frame.Roll(angle);
			}

			public final void Pitch(float angle) throws Throwable {
				Frame.Pitch(angle);
			}

			public final void Yaw(float angle) throws Throwable {
				Frame.Yaw(angle);
			}

			public final void LookDirection(Vector3 target) {
				Frame.LookDirection(target);
			}

			public final void LookDirection(Vector3 target, Vector3 upDirection) {
				Frame.LookDirection(target, upDirection);
			}

			public final void LookDirection(double heading) {
				Frame.LookDirection(heading);
			}

			public final void LookAt(Vector3 position, Vector3 target) {
				Frame.LookAt(position, target);
			}

			public final void LookAt(Vector3 position, Vector3 target, Vector3 upDirection) {
				Frame.LookAt(position, target, upDirection);
			}

			public final void SetPositionOrientation(Vector3 position, float roll, float pitch, float yaw)
					throws Throwable {
				Frame.setOrigin(position);

				Frame.ResetAxes();

				Frame.Roll(roll);
				Frame.Pitch(pitch);
				Frame.Yaw(yaw);
			}
		}
	}
}
