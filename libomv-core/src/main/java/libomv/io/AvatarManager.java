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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsMessage.AgentGroupDataUpdateMessage;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.DisplayNameUpdateMessage;
import libomv.capabilities.CapsMessage.GetDisplayNamesMessage;
import libomv.capabilities.IMessage;
import libomv.io.capabilities.CapsCallback;
import libomv.io.capabilities.CapsClient;
import libomv.model.Simulator;
import libomv.model.agent.EffectType;
import libomv.model.appearance.AppearanceFlags;
import libomv.model.avatar.AgentNamesCallbackArgs;
import libomv.model.avatar.Animation;
import libomv.model.avatar.AvatarAnimationCallbackArgs;
import libomv.model.avatar.AvatarAppearanceCallbackArgs;
import libomv.model.avatar.AvatarGroup;
import libomv.model.avatar.AvatarGroupsReplyCallbackArgs;
import libomv.model.avatar.AvatarInterestsReplyCallbackArgs;
import libomv.model.avatar.AvatarPickerReplyCallbackArgs;
import libomv.model.avatar.AvatarPropertiesReplyCallbackArgs;
import libomv.model.avatar.DisplayNameUpdateCallbackArgs;
import libomv.model.avatar.DisplayNamesCallbackArgs;
import libomv.model.avatar.ViewerEffectCallbackArgs;
import libomv.packets.AvatarAnimationPacket;
import libomv.packets.AvatarAppearancePacket;
import libomv.packets.AvatarGroupsReplyPacket;
import libomv.packets.AvatarInterestsReplyPacket;
import libomv.packets.AvatarPickerReplyPacket;
import libomv.packets.AvatarPickerRequestPacket;
import libomv.packets.AvatarPropertiesReplyPacket;
import libomv.packets.AvatarPropertiesRequestPacket;
import libomv.packets.GenericMessagePacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.TrackAgentPacket;
import libomv.packets.UUIDNameReplyPacket;
import libomv.packets.UUIDNameRequestPacket;
import libomv.packets.ViewerEffectPacket;
import libomv.primitives.Avatar;
import libomv.primitives.Avatar.ProfileFlags;
import libomv.primitives.TextureEntry;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3d;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;

public class AvatarManager implements PacketCallback, CapsCallback {
	private static final Logger logger = Logger.getLogger(AvatarManager.class);

	static final int MAX_UUIDS_PER_PACKET = 100;

	private GridClient client;

	/* HashMap containing all known avatars to this client */
	private Map<UUID, Avatar> avatars;

	public CallbackHandler<AgentNamesCallbackArgs> onAgentNames = new CallbackHandler<>();

	public CallbackHandler<DisplayNameUpdateCallbackArgs> onDisplayNameUpdate = new CallbackHandler<>();

	public CallbackHandler<AvatarAnimationCallbackArgs> onAvatarAnimation = new CallbackHandler<>();

	public CallbackHandler<AvatarAppearanceCallbackArgs> onAvatarAppearance = new CallbackHandler<>();

	public CallbackHandler<AvatarInterestsReplyCallbackArgs> onAvatarInterestsReply = new CallbackHandler<>();

	public CallbackHandler<AvatarPropertiesReplyCallbackArgs> onAvatarPropertiesReply = new CallbackHandler<>();

	public CallbackHandler<AvatarGroupsReplyCallbackArgs> onAvatarGroupsReply = new CallbackHandler<>();

	public CallbackHandler<AvatarPickerReplyCallbackArgs> onAvatarPickerReply = new CallbackHandler<>();

	public CallbackHandler<ViewerEffectCallbackArgs> onViewerEffect = new CallbackHandler<>();

	public AvatarManager(GridClient client) {
		this.client = client;
		this.avatars = new HashMap<>();

		// Avatar appearance callback
		this.client.network.registerCallback(PacketType.AvatarAppearance, this);

		// Avatar profile callbacks
		this.client.network.registerCallback(PacketType.AvatarPropertiesReply, this);
		// Client.Network.RegisterCallback(PacketType.AvatarStatisticsReply, this);
		this.client.network.registerCallback(PacketType.AvatarInterestsReply, this);

		// Avatar group callback
		this.client.network.registerCallback(PacketType.AvatarGroupsReply, this);
		this.client.network.registerCallback(CapsEventType.AgentGroupDataUpdate, this);
		this.client.network.registerCallback(CapsEventType.AvatarGroupsReply, this);

		// Viewer effect callback
		this.client.network.registerCallback(PacketType.ViewerEffect, this);

		// Other callbacks
		this.client.network.registerCallback(PacketType.UUIDNameReply, this);
		this.client.network.registerCallback(PacketType.AvatarPickerReply, this);
		this.client.network.registerCallback(PacketType.AvatarAnimation, this);
		this.client.network.registerCallback(CapsEventType.DisplayNameUpdate, this);

		// Picks callbacks
		this.client.network.registerCallback(PacketType.AvatarPicksReply, this);
		this.client.network.registerCallback(PacketType.PickInfoReply, this);

		// Classifieds callbacks
		this.client.network.registerCallback(PacketType.AvatarClassifiedReply, this);
		this.client.network.registerCallback(PacketType.ClassifiedInfoReply, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case AvatarAppearance:
			handleAvatarAppearance(packet, simulator);
			break;
		case AvatarPropertiesReply:
			handleAvatarProperties(packet, simulator);
			break;
		case AvatarInterestsReply:
			handleAvatarInterests(packet, simulator);
			break;
		case AvatarGroupsReply:
			handleAvatarGroupsReply(packet, simulator);
			break;
		case ViewerEffect:
			handleViewerEffect(packet, simulator);
			break;
		case UUIDNameReply:
			handleUUIDNameReply(packet, simulator);
			break;
		case AvatarPickerReply:
			handleAvatarPickerReply(packet, simulator);
			break;
		case AvatarAnimation:
			handleAvatarAnimation(packet, simulator);
			break;
		// case AvatarPicksReply:
		// // HandleAvatarPicksReply(packet, simulator);
		// break;
		// case PickInfoReply:
		// // HandlePickInfoReply(packet, simulator);
		// break;
		// case AvatarClassifiedReply:
		// // HandleAvatarClassifiedReply(packet, simulator);
		// break;
		// case ClassifiedInfoReply:
		// // HandleClassifiedInfoReply(packet, simulator);
		// break;
		default:
			logger.warn(GridClient.Log("AvatarManager: Unhandled packet" + packet.getType().toString(), client));
		}
	}

	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) {
		switch (message.getType()) {
		case DisplayNameUpdate:
			handleDisplayNameUpdate(message, simulator);
			break;
		case AgentGroupDataUpdate:
		case AvatarGroupsReply:
			handleAvatarGroupsReply(message, simulator);
			break;
		default:
			logger.warn(GridClient.Log("AvatarManager: Unhandled message " + message.getType().toString(), client));
		}
	}

	/**
	 * Add an Avatar into the Avatars Dictionary
	 *
	 * @param avatar
	 *            Filled-out Avatar class to insert
	 */
	public void add(Avatar avatar) {
		synchronized (avatars) {
			avatars.put(avatar.id, avatar);
		}
	}

	public boolean contains(UUID id) {
		synchronized (avatars) {
			return avatars.containsKey(id);
		}
	}

	/**
	 * This function will only check if the avatar name exists locally, it will not
	 * do any networking calls to fetch the name
	 *
	 * @param id
	 *            The uuid of the avatar to get the name for
	 * @return The avatar name, or an empty String if it's not found
	 */
	public String localAvatarNameLookup(UUID id) {
		synchronized (avatars) {
			Avatar avatar = avatars.get(id);
			if (avatar != null)
				return avatar.getName();
		}
		return Helpers.EmptyString;
	}

	/**
	 * Request retrieval of display names (max 90 names per request)
	 *
	 * @param ids
	 *            List of UUIDs to lookup
	 * @param callback
	 *            Callback to report result of the operation
	 * @throws IOReactorException
	 * @throws URISyntaxException
	 */
	public void getDisplayNames(List<UUID> ids, final Callback<DisplayNamesCallbackArgs> callback)
			throws IOReactorException, URISyntaxException {
		URI uri = client.network.getCapabilityURI(CapsEventType.GetDisplayNames.toString());
		if (uri == null || ids.size() == 0) {
			callback.callback(new DisplayNamesCallbackArgs(false, null, null));
		}

		StringBuilder query = new StringBuilder();
		for (int i = 0; i < ids.size() && i < 90; i++) {
			query.append("ids=" + ids.get(i));
			if (i < ids.size() - 1) {
				query.append("&");
			}
		}

		class DisplayCapsCallback implements FutureCallback<OSD> {
			@Override
			public void completed(OSD result) {
				GetDisplayNamesMessage msg = client.messages.new GetDisplayNamesMessage();
				msg.deserialize((OSDMap) result);
				callback.callback(new DisplayNamesCallbackArgs(true, msg.agents, msg.badIDs));
			}

			@Override
			public void failed(Exception ex) {
				callback.callback(new DisplayNamesCallbackArgs(false, null, null));
			}

			@Override
			public void cancelled() {
				callback.callback(new DisplayNamesCallbackArgs(false, null, null));
			}
		}
		CapsClient cap = new CapsClient(client, CapsEventType.GetDisplayNames.toString());
		cap.executeHttpGet(new URI(uri.toString() + "/?" + query), null, new DisplayCapsCallback(),
				client.settings.CAPS_TIMEOUT);
	}

	/**
	 * Tracks the specified avatar on your map
	 *
	 * @param preyID
	 *            Avatar ID to track
	 * @throws Exception
	 */
	public void requestTrackAgent(UUID preyID) throws Exception {
		TrackAgentPacket p = new TrackAgentPacket();
		p.AgentData.AgentID = client.agent.getAgentID();
		p.AgentData.SessionID = client.agent.getSessionID();
		p.PreyID = preyID;
		client.network.sendPacket(p);
	}

	/**
	 * Start a request for Avatar Properties
	 *
	 * @param avatarid
	 * @throws Exception
	 */
	public void requestAvatarProperties(UUID avatarid) throws Exception {
		AvatarPropertiesRequestPacket aprp = new AvatarPropertiesRequestPacket();

		aprp.AgentData.AgentID = client.agent.getAgentID();
		aprp.AgentData.SessionID = client.agent.getSessionID();
		aprp.AgentData.AvatarID = avatarid;

		client.network.sendPacket(aprp);
	}

	/**
	 * Search for an avatar (first name, last name)
	 *
	 * @param name
	 *            The name to search for
	 * @param queryID
	 *            An ID to associate with this query
	 * @throws Exception
	 */
	public void requestAvatarNameSearch(String name, UUID queryID) throws Exception {
		AvatarPickerRequestPacket aprp = new AvatarPickerRequestPacket();

		aprp.AgentData.AgentID = client.agent.getAgentID();
		aprp.AgentData.SessionID = client.agent.getSessionID();
		aprp.AgentData.QueryID = queryID;
		aprp.Data.setName(Helpers.stringToBytes(name));

		client.network.sendPacket(aprp);
	}

	/**
	 * Start a request for Avatar Picks
	 *
	 * @param avatarid
	 *            UUID of the avatar
	 * @throws Exception
	 */
	public void requestAvatarPicks(UUID avatarid) throws Exception {
		GenericMessagePacket gmp = new GenericMessagePacket();

		gmp.AgentData.AgentID = client.agent.getAgentID();
		gmp.AgentData.SessionID = client.agent.getSessionID();
		gmp.AgentData.TransactionID = UUID.ZERO;

		gmp.MethodData.setMethod(Helpers.stringToBytes("avatarpicksrequest"));
		gmp.MethodData.Invoice = UUID.ZERO;
		gmp.ParamList = new GenericMessagePacket.ParamListBlock[1];
		gmp.ParamList[0] = gmp.new ParamListBlock();
		gmp.ParamList[0].setParameter(Helpers.stringToBytes(avatarid.toString()));

		client.network.sendPacket(gmp);
	}

	/**
	 * Start a request for Avatar Classifieds
	 *
	 * @param avatarid
	 *            UUID of the avatar
	 * @throws Exception
	 */
	public void requestAvatarClassified(UUID avatarid) throws Exception {
		GenericMessagePacket gmp = new GenericMessagePacket();

		gmp.AgentData.AgentID = client.agent.getAgentID();
		gmp.AgentData.SessionID = client.agent.getSessionID();
		gmp.AgentData.TransactionID = UUID.ZERO;

		gmp.MethodData.setMethod(Helpers.stringToBytes("avatarclassifiedsrequest"));
		gmp.MethodData.Invoice = UUID.ZERO;
		gmp.ParamList = new GenericMessagePacket.ParamListBlock[1];
		gmp.ParamList[0] = gmp.new ParamListBlock();
		gmp.ParamList[0].setParameter(Helpers.stringToBytes(avatarid.toString()));

		client.network.sendPacket(gmp);
	}

	/**
	 * Start a request for details of a specific profile pick
	 *
	 * @param avatarid
	 *            UUID of the avatar
	 * @param pickid
	 *            UUID of the profile pick
	 * @throws Exception
	 */
	public void requestPickInfo(UUID avatarid, UUID pickid) throws Exception {
		GenericMessagePacket gmp = new GenericMessagePacket();

		gmp.AgentData.AgentID = client.agent.getAgentID();
		gmp.AgentData.SessionID = client.agent.getSessionID();
		gmp.AgentData.TransactionID = UUID.ZERO;

		gmp.MethodData.setMethod(Helpers.stringToBytes("pickinforequest"));
		gmp.MethodData.Invoice = UUID.ZERO;
		gmp.ParamList = new GenericMessagePacket.ParamListBlock[2];
		gmp.ParamList[0] = gmp.new ParamListBlock();
		gmp.ParamList[0].setParameter(Helpers.stringToBytes(avatarid.toString()));
		gmp.ParamList[1] = gmp.new ParamListBlock();
		gmp.ParamList[1].setParameter(Helpers.stringToBytes(pickid.toString()));

		client.network.sendPacket(gmp);
	}

	/**
	 * Start a request for details of a specific profile classified
	 *
	 * @param avatarid
	 *            UUID of the avatar
	 * @param UUID
	 *            of the profile classified
	 * @throws Exception
	 */
	public void requestClassifiedInfo(UUID avatarid, UUID classifiedid) throws Exception {
		GenericMessagePacket gmp = new GenericMessagePacket();

		gmp.AgentData.AgentID = client.agent.getAgentID();
		gmp.AgentData.SessionID = client.agent.getSessionID();
		gmp.AgentData.TransactionID = UUID.ZERO;

		gmp.MethodData.setMethod(Helpers.stringToBytes("classifiedinforequest"));
		gmp.MethodData.Invoice = UUID.ZERO;
		gmp.ParamList = new GenericMessagePacket.ParamListBlock[2];
		gmp.ParamList[0] = gmp.new ParamListBlock();
		gmp.ParamList[0].setParameter(Helpers.stringToBytes(avatarid.toString()));
		gmp.ParamList[1] = gmp.new ParamListBlock();
		gmp.ParamList[1].setParameter(Helpers.stringToBytes(classifiedid.toString()));

		client.network.sendPacket(gmp);
	}

	/**
	 * Request a name update for an avatar
	 *
	 * @param id
	 *            The uuid of the avatar to get the name for
	 * @param anc
	 *            A callback being called when a name request is answered
	 * @throws Exception
	 */
	public void requestAvatarName(UUID id, Callback<AgentNamesCallbackArgs> anc) throws Exception {
		synchronized (avatars) {
			// Fire callbacks for the ones we already have cached
			if (avatars.containsKey(id)) {
				Map<UUID, String> map = new HashMap<>(1);
				map.put(id, avatars.get(id).getName());
				anc.callback(new AgentNamesCallbackArgs(map));
				return;
			}
		}

		if (anc != null) {
			onAgentNames.add(anc, true);
		}

		UUIDNameRequestPacket request = new UUIDNameRequestPacket();
		request.ID = new UUID[1];
		request.ID[0] = id;
		client.network.sendPacket(request);
	}

	/**
	 * Request several name updates for a list of avatar uuids
	 *
	 * @param ids
	 *            The list of uuids of the avatars to get the names for
	 * @param anc
	 *            A callback being called when a name request is answered
	 * @throws Exception
	 */
	public void requestAvatarNames(List<UUID> ids, Callback<AgentNamesCallbackArgs> anc) throws Exception {
		Map<UUID, String> havenames = new HashMap<>();
		List<UUID> neednames = new ArrayList<>();

		synchronized (avatars) {
			Iterator<UUID> iter = ids.listIterator();
			while (iter.hasNext()) {
				UUID id = iter.next();
				if (avatars.containsKey(id)) {
					havenames.put(id, avatars.get(id).getName());
				} else {
					neednames.add(id);
				}
			}
		}

		// Fire callbacks for the ones we already have cached
		if (havenames.size() > 0) {
			if (anc != null) {
				anc.callback(new AgentNamesCallbackArgs(havenames));
			} else {
				onAgentNames.dispatch(new AgentNamesCallbackArgs(havenames));
			}
		}

		if (neednames.size() > 0) {
			if (anc != null) {
				onAgentNames.add(anc, true);
			}

			int m = MAX_UUIDS_PER_PACKET;
			int n = neednames.size() / m; // Number of full requests to make
			int i = 0;
			UUIDNameRequestPacket request = new UUIDNameRequestPacket();

			for (int j = 0; j < n; j++) {
				request.ID = new UUID[m];
				for (; i < neednames.size(); i++) {
					request.ID[i % m] = neednames.get(i);
				}
				client.network.sendPacket(request);
			}

			// Get any remaining names after left after the full requests
			if (neednames.size() > n * m) {
				request.ID = new UUID[neednames.size() - n * m];
				for (; i < neednames.size(); i++) {
					request.ID[i % m] = neednames.get(i);
				}
				client.network.sendPacket(request);
			}
		}
	}

	private Avatar findAvatar(Simulator simulator, UUID uuid) {
		Avatar av = simulator.findAvatar(uuid);
		synchronized (avatars) {
			if (av == null) {
				av = avatars.get(uuid);
			}
			if (av == null) {
				av = new Avatar(uuid);
				avatars.put(uuid, av);
			}
		}
		return av;
	}

	/**
	 * Process an incoming UUIDNameReply Packet and insert Full Names into the
	 *
	 * @param packet
	 *            Incoming Packet to process
	 * @param simulator
	 *            Unused
	 * @throws Exception
	 */
	private void handleUUIDNameReply(Packet packet, Simulator simulator) throws Exception {
		Map<UUID, String> names = new HashMap<>();
		UUIDNameReplyPacket reply = (UUIDNameReplyPacket) packet;

		synchronized (avatars) {
			for (UUIDNameReplyPacket.UUIDNameBlockBlock block : reply.UUIDNameBlock) {
				if (!avatars.containsKey(block.ID)) {
					avatars.put(block.ID, new Avatar(block.ID));
				}

				avatars.get(block.ID).setNames(Helpers.bytesToString(block.getFirstName()),
						Helpers.bytesToString(block.getLastName()));
				names.put(block.ID, avatars.get(block.ID).getName());
			}
		}
		onAgentNames.dispatch(new AgentNamesCallbackArgs(names));
	}

	private void handleAvatarAnimation(Packet packet, Simulator simulator) throws Exception {
		AvatarAnimationPacket data = (AvatarAnimationPacket) packet;
		List<Animation> signaledAnimations = new ArrayList<>(data.AnimationList.length);

		for (int i = 0; i < data.AnimationList.length; i++) {
			Animation animation = new Animation();
			animation.animationID = data.AnimationList[i].AnimID;
			animation.animationSequence = data.AnimationList[i].AnimSequenceID;
			if (i < data.ObjectID.length) {
				animation.animationSourceObjectID = data.ObjectID[i];
			}
			signaledAnimations.add(animation);
		}

		Avatar avatar = simulator.findAvatar(data.ID);
		if (avatar != null) {
			avatar.animations = signaledAnimations;
		}

		onAvatarAnimation.dispatch(new AvatarAnimationCallbackArgs(data.ID, signaledAnimations));
	}

	private void handleAvatarAppearance(Packet packet, Simulator simulator) throws Exception {
		if (onAvatarAppearance.count() > 0 || client.settings.getBool(LibSettings.AVATAR_TRACKING)) {
			AvatarAppearancePacket appearance = (AvatarAppearancePacket) packet;

			TextureEntry textureEntry = new TextureEntry(appearance.ObjectData.getTextureEntry());

			TextureEntry.TextureEntryFace defaultTexture = textureEntry.defaultTexture;
			TextureEntry.TextureEntryFace[] faceTextures = textureEntry.faceTextures;

			byte appearanceVersion = 0;
			int cofVersion = 0;
			AppearanceFlags appearanceFlags = AppearanceFlags.None;

			if (appearance.AppearanceData != null && appearance.AppearanceData.length > 0) {
				appearanceVersion = appearance.AppearanceData[0].AppearanceVersion;
				cofVersion = appearance.AppearanceData[0].CofVersion;
				// used in the future
				// appearanceFlags =
				// AppearanceManager.AppearanceFlags.setValue(appearance.AppearanceData[0].Flags);
			}

			Avatar av = simulator.findAvatar(appearance.Sender.ID);
			if (av != null) {
				av.textures = textureEntry;
				av.visualParameters = appearance.ParamValue;
				av.appearanceVersion = appearanceVersion;
				av.cofVersion = cofVersion;
				av.appearanceFlags = appearanceFlags;
			}

			onAvatarAppearance.dispatch(new AvatarAppearanceCallbackArgs(simulator, appearance.Sender.ID,
					appearance.Sender.IsTrial, defaultTexture, faceTextures, appearance.ParamValue, appearanceVersion,
					cofVersion, appearanceFlags));
		}
	}

	private void handleAvatarProperties(Packet packet, Simulator simulator) throws Exception {
		if (onAvatarPropertiesReply.count() > 0) {
			AvatarPropertiesReplyPacket reply = (AvatarPropertiesReplyPacket) packet;
			Avatar av = findAvatar(simulator, reply.AgentData.AvatarID);
			av.profileProperties = av.new AvatarProperties();

			av.profileProperties.profileImage = reply.PropertiesData.ImageID;
			av.profileProperties.firstLifeImage = reply.PropertiesData.FLImageID;
			av.profileProperties.partner = reply.PropertiesData.PartnerID;
			av.profileProperties.aboutText = Helpers.bytesToString(reply.PropertiesData.getAboutText());
			av.profileProperties.firstLifeText = Helpers.bytesToString(reply.PropertiesData.getFLAboutText());
			av.profileProperties.bornOn = Helpers.bytesToString(reply.PropertiesData.getBornOn());
			long charter = Helpers.bytesToUInt32L(reply.PropertiesData.getCharterMember());
			if (charter == 0) {
				av.profileProperties.charterMember = "Resident";
			} else if (charter == 2) {
				av.profileProperties.charterMember = "Charter";
			} else if (charter == 3) {
				av.profileProperties.charterMember = "Linden";
			} else {
				av.profileProperties.charterMember = Helpers.bytesToString(reply.PropertiesData.getCharterMember());
			}
			av.profileProperties.flags = ProfileFlags.setValue(reply.PropertiesData.Flags);
			av.profileProperties.profileURL = Helpers.bytesToString(reply.PropertiesData.getProfileURL());

			onAvatarPropertiesReply.dispatch(new AvatarPropertiesReplyCallbackArgs(av));
		}
	}

	private void handleAvatarInterests(Packet packet, Simulator simulator) throws Exception {
		if (onAvatarInterestsReply.count() > 0) {
			AvatarInterestsReplyPacket airp = (AvatarInterestsReplyPacket) packet;
			Avatar av = findAvatar(simulator, airp.AgentData.AvatarID);
			av.profileInterests = av.new Interests();

			av.profileInterests.wantToMask = airp.PropertiesData.WantToMask;
			av.profileInterests.wantToText = Helpers.bytesToString(airp.PropertiesData.getWantToText());
			av.profileInterests.skillsMask = airp.PropertiesData.SkillsMask;
			av.profileInterests.skillsText = Helpers.bytesToString(airp.PropertiesData.getSkillsText());
			av.profileInterests.languagesText = Helpers.bytesToString(airp.PropertiesData.getLanguagesText());

			onAvatarInterestsReply.dispatch(new AvatarInterestsReplyCallbackArgs(av));
		}
	}

	/**
	 * EQ Message fired when someone nearby changes their display name
	 */
	private void handleDisplayNameUpdate(IMessage message, SimulatorManager simulator) {
		DisplayNameUpdateMessage msg = (DisplayNameUpdateMessage) message;
		synchronized (avatars) {
			UUID id = msg.displayName.id;
			if (!avatars.containsKey(id)) {
				avatars.put(id, new Avatar(id));
			}
			avatars.get(id).setDisplayName(msg.displayName.displayName);
		}
		onDisplayNameUpdate.dispatch(new DisplayNameUpdateCallbackArgs(msg.oldDisplayName, msg.displayName));
	}

	private void handleAvatarGroupsReply(IMessage message, SimulatorManager simulator) {
		if (onAvatarGroupsReply.count() > 0) {
			AgentGroupDataUpdateMessage msg = (AgentGroupDataUpdateMessage) message;
			List<AvatarGroup> avatarGroups = new ArrayList<>(msg.groupDataBlock.length);
			for (int i = 0; i < msg.groupDataBlock.length; i++) {
				AvatarGroup avatarGroup = new AvatarGroup();
				avatarGroup.acceptNotices = msg.groupDataBlock[i].acceptNotices;
				avatarGroup.groupID = msg.groupDataBlock[i].groupID;
				avatarGroup.groupInsigniaID = msg.groupDataBlock[i].groupInsigniaID;
				avatarGroup.groupName = msg.groupDataBlock[i].groupName;
				avatarGroup.groupTitle = msg.groupDataBlock[i].groupTitle;
				avatarGroup.groupPowers = msg.groupDataBlock[i].groupPowers;
				avatarGroup.listInProfile = msg.newGroupDataBlock[i].listInProfile;

				avatarGroups.add(avatarGroup);
			}

			onAvatarGroupsReply.dispatch(new AvatarGroupsReplyCallbackArgs(msg.agentID, avatarGroups));
		}
	}

	private void handleAvatarGroupsReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (onAvatarGroupsReply.count() > 0) {
			AvatarGroupsReplyPacket groups = (AvatarGroupsReplyPacket) packet;
			List<AvatarGroup> avatarGroups = new ArrayList<>(groups.GroupData.length);

			for (int i = 0; i < groups.GroupData.length; i++) {
				AvatarGroup avatarGroup = new AvatarGroup();

				avatarGroup.acceptNotices = groups.GroupData[i].AcceptNotices;
				avatarGroup.groupID = groups.GroupData[i].GroupID;
				avatarGroup.groupInsigniaID = groups.GroupData[i].GroupInsigniaID;
				avatarGroup.groupName = Helpers.bytesToString(groups.GroupData[i].getGroupName());
				avatarGroup.groupPowers = groups.GroupData[i].GroupPowers;
				avatarGroup.groupTitle = Helpers.bytesToString(groups.GroupData[i].getGroupTitle());
				avatarGroup.listInProfile = groups.ListInProfile;

				avatarGroups.add(avatarGroup);
			}
			onAvatarGroupsReply.dispatch(new AvatarGroupsReplyCallbackArgs(groups.AgentData.AvatarID, avatarGroups));
		}
	}

	private void handleAvatarPickerReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (onAvatarPickerReply.count() > 0) {
			AvatarPickerReplyPacket reply = (AvatarPickerReplyPacket) packet;
			Map<UUID, String> avatars = new HashMap<>();

			for (AvatarPickerReplyPacket.DataBlock block : reply.Data) {
				avatars.put(block.AvatarID,
						Helpers.bytesToString(block.getFirstName()) + " " + Helpers.bytesToString(block.getLastName()));
			}
			onAvatarPickerReply.dispatch(new AvatarPickerReplyCallbackArgs(reply.AgentData.QueryID, avatars));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events</summary>
	 */
	private void handleViewerEffect(Packet packet, Simulator simulator) {
		ViewerEffectPacket effect = (ViewerEffectPacket) packet;

		for (ViewerEffectPacket.EffectBlock block : effect.Effect) {
			EffectType type = EffectType.setValue(block.Type);

			// Each ViewerEffect type uses it's own custom binary format for additional
			// data. Fun eh?
			switch (type) {
			case Beam:
			case Point:
			case Trail:
			case Sphere:
			case Spiral:
			case Edit:
				if (block.getTypeData().length == 56) {
					UUID sourceAvatar = new UUID(block.getTypeData(), 0);
					UUID targetObject = new UUID(block.getTypeData(), 16);
					Vector3d targetPos = new Vector3d(block.getTypeData(), 32);
					onViewerEffect.dispatch(new ViewerEffectCallbackArgs(type, simulator, sourceAvatar, targetObject,
							targetPos, (byte) 0, block.Duration, block.ID));
				} else {
					logger.warn(GridClient
							.Log("Received a " + type.toString() + " ViewerEffect with an incorrect TypeData size of "
									+ block.getTypeData().length + " bytes", client));
				}
				break;
			case LookAt:
			case PointAt:
				if (block.getTypeData().length == 57) {
					UUID sourceAvatar = new UUID(block.getTypeData(), 0);
					UUID targetObject = new UUID(block.getTypeData(), 16);
					Vector3d targetPos = new Vector3d(block.getTypeData(), 32);

					onViewerEffect.dispatch(new ViewerEffectCallbackArgs(type, simulator, sourceAvatar, targetObject,
							targetPos, block.getTypeData()[56], block.Duration, block.ID));
				} else {
					logger.warn(GridClient.Log(
							"Received a LookAt " + type.toString() + " ViewerEffect with an incorrect TypeData size of "
									+ block.getTypeData().length + " bytes",
							client));
				}
				break;
			case Text:
			case Icon:
			case Connector:
			case FlexibleObject:
			case AnimalControls:
			case AnimationObject:
			case Cloth:
			case Glow:
			default:
				logger.warn(GridClient.Log("Received a ViewerEffect with an unknown type " + type.toString()
						+ " and length " + block.getTypeData().length + " bytes", client));
				break;
			}
		}
	}
}
