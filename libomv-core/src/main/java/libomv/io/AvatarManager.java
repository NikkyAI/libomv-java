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
import libomv.model.Agent.EffectType;
import libomv.model.Simulator;
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

public class AvatarManager implements PacketCallback, CapsCallback, libomv.model.Avatar {
	private static final Logger logger = Logger.getLogger(AvatarManager.class);

	static final int MAX_UUIDS_PER_PACKET = 100;

	private GridClient _Client;

	/* HashMap containing all known avatars to this client */
	private HashMap<UUID, Avatar> _Avatars;

	public CallbackHandler<AgentNamesCallbackArgs> OnAgentNames = new CallbackHandler<AgentNamesCallbackArgs>();

	public CallbackHandler<DisplayNameUpdateCallbackArgs> OnDisplayNameUpdate = new CallbackHandler<DisplayNameUpdateCallbackArgs>();

	public CallbackHandler<AvatarAnimationCallbackArgs> OnAvatarAnimation = new CallbackHandler<AvatarAnimationCallbackArgs>();

	public CallbackHandler<AvatarAppearanceCallbackArgs> OnAvatarAppearance = new CallbackHandler<AvatarAppearanceCallbackArgs>();

	public CallbackHandler<AvatarInterestsReplyCallbackArgs> OnAvatarInterestsReply = new CallbackHandler<AvatarInterestsReplyCallbackArgs>();

	public CallbackHandler<AvatarPropertiesReplyCallbackArgs> OnAvatarPropertiesReply = new CallbackHandler<AvatarPropertiesReplyCallbackArgs>();

	public CallbackHandler<AvatarGroupsReplyCallbackArgs> OnAvatarGroupsReply = new CallbackHandler<AvatarGroupsReplyCallbackArgs>();

	public CallbackHandler<AvatarPickerReplyCallbackArgs> OnAvatarPickerReply = new CallbackHandler<AvatarPickerReplyCallbackArgs>();

	public CallbackHandler<ViewerEffectCallbackArgs> OnViewerEffect = new CallbackHandler<ViewerEffectCallbackArgs>();

	public AvatarManager(GridClient client) {
		_Client = client;
		_Avatars = new HashMap<UUID, Avatar>();

		// Avatar appearance callback
		_Client.Network.RegisterCallback(PacketType.AvatarAppearance, this);

		// Avatar profile callbacks
		_Client.Network.RegisterCallback(PacketType.AvatarPropertiesReply, this);
		// Client.Network.RegisterCallback(PacketType.AvatarStatisticsReply, this);
		_Client.Network.RegisterCallback(PacketType.AvatarInterestsReply, this);

		// Avatar group callback
		_Client.Network.RegisterCallback(PacketType.AvatarGroupsReply, this);
		_Client.Network.RegisterCallback(CapsEventType.AgentGroupDataUpdate, this);
		_Client.Network.RegisterCallback(CapsEventType.AvatarGroupsReply, this);

		// Viewer effect callback
		_Client.Network.RegisterCallback(PacketType.ViewerEffect, this);

		// Other callbacks
		_Client.Network.RegisterCallback(PacketType.UUIDNameReply, this);
		_Client.Network.RegisterCallback(PacketType.AvatarPickerReply, this);
		_Client.Network.RegisterCallback(PacketType.AvatarAnimation, this);
		_Client.Network.RegisterCallback(CapsEventType.DisplayNameUpdate, this);

		// Picks callbacks
		_Client.Network.RegisterCallback(PacketType.AvatarPicksReply, this);
		_Client.Network.RegisterCallback(PacketType.PickInfoReply, this);

		// Classifieds callbacks
		_Client.Network.RegisterCallback(PacketType.AvatarClassifiedReply, this);
		_Client.Network.RegisterCallback(PacketType.ClassifiedInfoReply, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case AvatarAppearance:
			HandleAvatarAppearance(packet, simulator);
			break;
		case AvatarPropertiesReply:
			HandleAvatarProperties(packet, simulator);
			break;
		case AvatarInterestsReply:
			HandleAvatarInterests(packet, simulator);
			break;
		case AvatarGroupsReply:
			HandleAvatarGroupsReply(packet, simulator);
			break;
		case ViewerEffect:
			HandleViewerEffect(packet, simulator);
			break;
		case UUIDNameReply:
			HandleUUIDNameReply(packet, simulator);
			break;
		case AvatarPickerReply:
			HandleAvatarPickerReply(packet, simulator);
			break;
		case AvatarAnimation:
			HandleAvatarAnimation(packet, simulator);
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
			logger.warn(GridClient.Log("AvatarManager: Unhandled packet" + packet.getType().toString(), _Client));
		}
	}

	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) {
		switch (message.getType()) {
		case DisplayNameUpdate:
			HandleDisplayNameUpdate(message, simulator);
			break;
		case AgentGroupDataUpdate:
		case AvatarGroupsReply:
			HandleAvatarGroupsReply(message, simulator);
			break;
		default:
			logger.warn(GridClient.Log("AvatarManager: Unhandled message " + message.getType().toString(), _Client));
		}
	}

	/**
	 * Add an Avatar into the Avatars Dictionary
	 *
	 * @param avatar
	 *            Filled-out Avatar class to insert
	 */
	public void add(Avatar avatar) {
		synchronized (_Avatars) {
			_Avatars.put(avatar.id, avatar);
		}
	}

	public boolean contains(UUID id) {
		synchronized (_Avatars) {
			return _Avatars.containsKey(id);
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
	public String LocalAvatarNameLookup(UUID id) {
		synchronized (_Avatars) {
			Avatar avatar = _Avatars.get(id);
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
	public void GetDisplayNames(ArrayList<UUID> ids, final Callback<DisplayNamesCallbackArgs> callback)
			throws IOReactorException, URISyntaxException {
		URI uri = _Client.Network.getCapabilityURI(CapsEventType.GetDisplayNames.toString());
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
				GetDisplayNamesMessage msg = _Client.Messages.new GetDisplayNamesMessage();
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
		CapsClient cap = new CapsClient(_Client, CapsEventType.GetDisplayNames.toString());
		cap.executeHttpGet(new URI(uri.toString() + "/?" + query), null, new DisplayCapsCallback(),
				_Client.Settings.CAPS_TIMEOUT);
	}

	/**
	 * Tracks the specified avatar on your map
	 *
	 * @param preyID
	 *            Avatar ID to track
	 * @throws Exception
	 */
	public void RequestTrackAgent(UUID preyID) throws Exception {
		TrackAgentPacket p = new TrackAgentPacket();
		p.AgentData.AgentID = _Client.Self.getAgentID();
		p.AgentData.SessionID = _Client.Self.getSessionID();
		p.PreyID = preyID;
		_Client.Network.sendPacket(p);
	}

	/**
	 * Start a request for Avatar Properties
	 *
	 * @param avatarid
	 * @throws Exception
	 */
	public void RequestAvatarProperties(UUID avatarid) throws Exception {
		AvatarPropertiesRequestPacket aprp = new AvatarPropertiesRequestPacket();

		aprp.AgentData.AgentID = _Client.Self.getAgentID();
		aprp.AgentData.SessionID = _Client.Self.getSessionID();
		aprp.AgentData.AvatarID = avatarid;

		_Client.Network.sendPacket(aprp);
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
	public void RequestAvatarNameSearch(String name, UUID queryID) throws Exception {
		AvatarPickerRequestPacket aprp = new AvatarPickerRequestPacket();

		aprp.AgentData.AgentID = _Client.Self.getAgentID();
		aprp.AgentData.SessionID = _Client.Self.getSessionID();
		aprp.AgentData.QueryID = queryID;
		aprp.Data.setName(Helpers.StringToBytes(name));

		_Client.Network.sendPacket(aprp);
	}

	/**
	 * Start a request for Avatar Picks
	 *
	 * @param avatarid
	 *            UUID of the avatar
	 * @throws Exception
	 */
	public void RequestAvatarPicks(UUID avatarid) throws Exception {
		GenericMessagePacket gmp = new GenericMessagePacket();

		gmp.AgentData.AgentID = _Client.Self.getAgentID();
		gmp.AgentData.SessionID = _Client.Self.getSessionID();
		gmp.AgentData.TransactionID = UUID.Zero;

		gmp.MethodData.setMethod(Helpers.StringToBytes("avatarpicksrequest"));
		gmp.MethodData.Invoice = UUID.Zero;
		gmp.ParamList = new GenericMessagePacket.ParamListBlock[1];
		gmp.ParamList[0] = gmp.new ParamListBlock();
		gmp.ParamList[0].setParameter(Helpers.StringToBytes(avatarid.toString()));

		_Client.Network.sendPacket(gmp);
	}

	/**
	 * Start a request for Avatar Classifieds
	 *
	 * @param avatarid
	 *            UUID of the avatar
	 * @throws Exception
	 */
	public void RequestAvatarClassified(UUID avatarid) throws Exception {
		GenericMessagePacket gmp = new GenericMessagePacket();

		gmp.AgentData.AgentID = _Client.Self.getAgentID();
		gmp.AgentData.SessionID = _Client.Self.getSessionID();
		gmp.AgentData.TransactionID = UUID.Zero;

		gmp.MethodData.setMethod(Helpers.StringToBytes("avatarclassifiedsrequest"));
		gmp.MethodData.Invoice = UUID.Zero;
		gmp.ParamList = new GenericMessagePacket.ParamListBlock[1];
		gmp.ParamList[0] = gmp.new ParamListBlock();
		gmp.ParamList[0].setParameter(Helpers.StringToBytes(avatarid.toString()));

		_Client.Network.sendPacket(gmp);
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
	public void RequestPickInfo(UUID avatarid, UUID pickid) throws Exception {
		GenericMessagePacket gmp = new GenericMessagePacket();

		gmp.AgentData.AgentID = _Client.Self.getAgentID();
		gmp.AgentData.SessionID = _Client.Self.getSessionID();
		gmp.AgentData.TransactionID = UUID.Zero;

		gmp.MethodData.setMethod(Helpers.StringToBytes("pickinforequest"));
		gmp.MethodData.Invoice = UUID.Zero;
		gmp.ParamList = new GenericMessagePacket.ParamListBlock[2];
		gmp.ParamList[0] = gmp.new ParamListBlock();
		gmp.ParamList[0].setParameter(Helpers.StringToBytes(avatarid.toString()));
		gmp.ParamList[1] = gmp.new ParamListBlock();
		gmp.ParamList[1].setParameter(Helpers.StringToBytes(pickid.toString()));

		_Client.Network.sendPacket(gmp);
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
	public void RequestClassifiedInfo(UUID avatarid, UUID classifiedid) throws Exception {
		GenericMessagePacket gmp = new GenericMessagePacket();

		gmp.AgentData.AgentID = _Client.Self.getAgentID();
		gmp.AgentData.SessionID = _Client.Self.getSessionID();
		gmp.AgentData.TransactionID = UUID.Zero;

		gmp.MethodData.setMethod(Helpers.StringToBytes("classifiedinforequest"));
		gmp.MethodData.Invoice = UUID.Zero;
		gmp.ParamList = new GenericMessagePacket.ParamListBlock[2];
		gmp.ParamList[0] = gmp.new ParamListBlock();
		gmp.ParamList[0].setParameter(Helpers.StringToBytes(avatarid.toString()));
		gmp.ParamList[1] = gmp.new ParamListBlock();
		gmp.ParamList[1].setParameter(Helpers.StringToBytes(classifiedid.toString()));

		_Client.Network.sendPacket(gmp);
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
	public void RequestAvatarName(UUID id, Callback<AgentNamesCallbackArgs> anc) throws Exception {
		synchronized (_Avatars) {
			// Fire callbacks for the ones we already have cached
			if (_Avatars.containsKey(id)) {
				HashMap<UUID, String> map = new HashMap<UUID, String>(1);
				map.put(id, _Avatars.get(id).getName());
				anc.callback(new AgentNamesCallbackArgs(map));
				return;
			}
		}

		if (anc != null) {
			OnAgentNames.add(anc, true);
		}

		UUIDNameRequestPacket request = new UUIDNameRequestPacket();
		request.ID = new UUID[1];
		request.ID[0] = id;
		_Client.Network.sendPacket(request);
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
	public void RequestAvatarNames(ArrayList<UUID> ids, Callback<AgentNamesCallbackArgs> anc) throws Exception {
		HashMap<UUID, String> havenames = new HashMap<UUID, String>();
		ArrayList<UUID> neednames = new ArrayList<UUID>();

		synchronized (_Avatars) {
			Iterator<UUID> iter = ids.listIterator();
			while (iter.hasNext()) {
				UUID id = iter.next();
				if (_Avatars.containsKey(id)) {
					havenames.put(id, _Avatars.get(id).getName());
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
				OnAgentNames.dispatch(new AgentNamesCallbackArgs(havenames));
			}
		}

		if (neednames.size() > 0) {
			if (anc != null) {
				OnAgentNames.add(anc, true);
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
				_Client.Network.sendPacket(request);
			}

			// Get any remaining names after left after the full requests
			if (neednames.size() > n * m) {
				request.ID = new UUID[neednames.size() - n * m];
				for (; i < neednames.size(); i++) {
					request.ID[i % m] = neednames.get(i);
				}
				_Client.Network.sendPacket(request);
			}
		}
	}

	private Avatar findAvatar(Simulator simulator, UUID uuid) {
		Avatar av = simulator.findAvatar(uuid);
		synchronized (_Avatars) {
			if (av == null) {
				av = _Avatars.get(uuid);
			}
			if (av == null) {
				av = new Avatar(uuid);
				_Avatars.put(uuid, av);
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
	private void HandleUUIDNameReply(Packet packet, Simulator simulator) throws Exception {
		HashMap<UUID, String> names = new HashMap<UUID, String>();
		UUIDNameReplyPacket reply = (UUIDNameReplyPacket) packet;

		synchronized (_Avatars) {
			for (UUIDNameReplyPacket.UUIDNameBlockBlock block : reply.UUIDNameBlock) {
				if (!_Avatars.containsKey(block.ID)) {
					_Avatars.put(block.ID, new Avatar(block.ID));
				}

				_Avatars.get(block.ID).setNames(Helpers.BytesToString(block.getFirstName()),
						Helpers.BytesToString(block.getLastName()));
				names.put(block.ID, _Avatars.get(block.ID).getName());
			}
		}
		OnAgentNames.dispatch(new AgentNamesCallbackArgs(names));
	}

	private void HandleAvatarAnimation(Packet packet, Simulator simulator) throws Exception {
		AvatarAnimationPacket data = (AvatarAnimationPacket) packet;
		ArrayList<Animation> signaledAnimations = new ArrayList<Animation>(data.AnimationList.length);

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

		OnAvatarAnimation.dispatch(new AvatarAnimationCallbackArgs(data.ID, signaledAnimations));
	}

	private void HandleAvatarAppearance(Packet packet, Simulator simulator) throws Exception {
		if (OnAvatarAppearance.count() > 0 || _Client.Settings.getBool(LibSettings.AVATAR_TRACKING)) {
			AvatarAppearancePacket appearance = (AvatarAppearancePacket) packet;

			TextureEntry textureEntry = new TextureEntry(appearance.ObjectData.getTextureEntry());

			TextureEntry.TextureEntryFace defaultTexture = textureEntry.defaultTexture;
			TextureEntry.TextureEntryFace[] faceTextures = textureEntry.faceTextures;

			byte appearanceVersion = 0;
			int COFVersion = 0;
			AppearanceManager.AppearanceFlags appearanceFlags = AppearanceManager.AppearanceFlags.None;

			if (appearance.AppearanceData != null && appearance.AppearanceData.length > 0) {
				appearanceVersion = appearance.AppearanceData[0].AppearanceVersion;
				COFVersion = appearance.AppearanceData[0].CofVersion;
				// used in the future
				// appearanceFlags =
				// AppearanceManager.AppearanceFlags.setValue(appearance.AppearanceData[0].Flags);
			}

			Avatar av = simulator.findAvatar(appearance.Sender.ID);
			if (av != null) {
				av.textures = textureEntry;
				av.visualParameters = appearance.ParamValue;
				av.appearanceVersion = appearanceVersion;
				av.cofVersion = COFVersion;
				av.appearanceFlags = appearanceFlags;
			}

			OnAvatarAppearance.dispatch(new AvatarAppearanceCallbackArgs(simulator, appearance.Sender.ID,
					appearance.Sender.IsTrial, defaultTexture, faceTextures, appearance.ParamValue, appearanceVersion,
					COFVersion, appearanceFlags));
		}
	}

	private void HandleAvatarProperties(Packet packet, Simulator simulator) throws Exception {
		if (OnAvatarPropertiesReply.count() > 0) {
			AvatarPropertiesReplyPacket reply = (AvatarPropertiesReplyPacket) packet;
			Avatar av = findAvatar(simulator, reply.AgentData.AvatarID);
			av.profileProperties = av.new AvatarProperties();

			av.profileProperties.profileImage = reply.PropertiesData.ImageID;
			av.profileProperties.firstLifeImage = reply.PropertiesData.FLImageID;
			av.profileProperties.partner = reply.PropertiesData.PartnerID;
			av.profileProperties.aboutText = Helpers.BytesToString(reply.PropertiesData.getAboutText());
			av.profileProperties.firstLifeText = Helpers.BytesToString(reply.PropertiesData.getFLAboutText());
			av.profileProperties.bornOn = Helpers.BytesToString(reply.PropertiesData.getBornOn());
			long charter = Helpers.BytesToUInt32L(reply.PropertiesData.getCharterMember());
			if (charter == 0) {
				av.profileProperties.charterMember = "Resident";
			} else if (charter == 2) {
				av.profileProperties.charterMember = "Charter";
			} else if (charter == 3) {
				av.profileProperties.charterMember = "Linden";
			} else {
				av.profileProperties.charterMember = Helpers.BytesToString(reply.PropertiesData.getCharterMember());
			}
			av.profileProperties.flags = ProfileFlags.setValue(reply.PropertiesData.Flags);
			av.profileProperties.profileURL = Helpers.BytesToString(reply.PropertiesData.getProfileURL());

			OnAvatarPropertiesReply.dispatch(new AvatarPropertiesReplyCallbackArgs(av));
		}
	}

	private void HandleAvatarInterests(Packet packet, Simulator simulator) throws Exception {
		if (OnAvatarInterestsReply.count() > 0) {
			AvatarInterestsReplyPacket airp = (AvatarInterestsReplyPacket) packet;
			Avatar av = findAvatar(simulator, airp.AgentData.AvatarID);
			av.profileInterests = av.new Interests();

			av.profileInterests.wantToMask = airp.PropertiesData.WantToMask;
			av.profileInterests.wantToText = Helpers.BytesToString(airp.PropertiesData.getWantToText());
			av.profileInterests.skillsMask = airp.PropertiesData.SkillsMask;
			av.profileInterests.skillsText = Helpers.BytesToString(airp.PropertiesData.getSkillsText());
			av.profileInterests.languagesText = Helpers.BytesToString(airp.PropertiesData.getLanguagesText());

			OnAvatarInterestsReply.dispatch(new AvatarInterestsReplyCallbackArgs(av));
		}
	}

	/**
	 * EQ Message fired when someone nearby changes their display name
	 */
	private void HandleDisplayNameUpdate(IMessage message, SimulatorManager simulator) {
		DisplayNameUpdateMessage msg = (DisplayNameUpdateMessage) message;
		synchronized (_Avatars) {
			UUID id = msg.displayName.id;
			if (!_Avatars.containsKey(id)) {
				_Avatars.put(id, new Avatar(id));
			}
			_Avatars.get(id).setDisplayName(msg.displayName.displayName);
		}
		OnDisplayNameUpdate.dispatch(new DisplayNameUpdateCallbackArgs(msg.oldDisplayName, msg.displayName));
	}

	private void HandleAvatarGroupsReply(IMessage message, SimulatorManager simulator) {
		if (OnAvatarGroupsReply.count() > 0) {
			AgentGroupDataUpdateMessage msg = (AgentGroupDataUpdateMessage) message;
			ArrayList<AvatarGroup> avatarGroups = new ArrayList<AvatarGroup>(msg.groupDataBlock.length);
			for (int i = 0; i < msg.groupDataBlock.length; i++) {
				AvatarGroup avatarGroup = new AvatarGroup();
				avatarGroup.AcceptNotices = msg.groupDataBlock[i].acceptNotices;
				avatarGroup.GroupID = msg.groupDataBlock[i].groupID;
				avatarGroup.GroupInsigniaID = msg.groupDataBlock[i].groupInsigniaID;
				avatarGroup.GroupName = msg.groupDataBlock[i].groupName;
				avatarGroup.GroupTitle = msg.groupDataBlock[i].groupTitle;
				avatarGroup.GroupPowers = msg.groupDataBlock[i].groupPowers;
				avatarGroup.ListInProfile = msg.newGroupDataBlock[i].listInProfile;

				avatarGroups.add(avatarGroup);
			}

			OnAvatarGroupsReply.dispatch(new AvatarGroupsReplyCallbackArgs(msg.agentID, avatarGroups));
		}
	}

	private void HandleAvatarGroupsReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (OnAvatarGroupsReply.count() > 0) {
			AvatarGroupsReplyPacket groups = (AvatarGroupsReplyPacket) packet;
			ArrayList<AvatarGroup> avatarGroups = new ArrayList<AvatarGroup>(groups.GroupData.length);

			for (int i = 0; i < groups.GroupData.length; i++) {
				AvatarGroup avatarGroup = new AvatarGroup();

				avatarGroup.AcceptNotices = groups.GroupData[i].AcceptNotices;
				avatarGroup.GroupID = groups.GroupData[i].GroupID;
				avatarGroup.GroupInsigniaID = groups.GroupData[i].GroupInsigniaID;
				avatarGroup.GroupName = Helpers.BytesToString(groups.GroupData[i].getGroupName());
				avatarGroup.GroupPowers = groups.GroupData[i].GroupPowers;
				avatarGroup.GroupTitle = Helpers.BytesToString(groups.GroupData[i].getGroupTitle());
				avatarGroup.ListInProfile = groups.ListInProfile;

				avatarGroups.add(avatarGroup);
			}
			OnAvatarGroupsReply.dispatch(new AvatarGroupsReplyCallbackArgs(groups.AgentData.AvatarID, avatarGroups));
		}
	}

	private void HandleAvatarPickerReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		if (OnAvatarPickerReply.count() > 0) {
			AvatarPickerReplyPacket reply = (AvatarPickerReplyPacket) packet;
			HashMap<UUID, String> avatars = new HashMap<UUID, String>();

			for (AvatarPickerReplyPacket.DataBlock block : reply.Data) {
				avatars.put(block.AvatarID,
						Helpers.BytesToString(block.getFirstName()) + " " + Helpers.BytesToString(block.getLastName()));
			}
			OnAvatarPickerReply.dispatch(new AvatarPickerReplyCallbackArgs(reply.AgentData.QueryID, avatars));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events</summary>
	 */
	private void HandleViewerEffect(Packet packet, Simulator simulator) {
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
					OnViewerEffect.dispatch(new ViewerEffectCallbackArgs(type, simulator, sourceAvatar, targetObject,
							targetPos, (byte) 0, block.Duration, block.ID));
				} else {
					logger.warn(GridClient
							.Log("Received a " + type.toString() + " ViewerEffect with an incorrect TypeData size of "
									+ block.getTypeData().length + " bytes", _Client));
				}
				break;
			case LookAt:
			case PointAt:
				if (block.getTypeData().length == 57) {
					UUID sourceAvatar = new UUID(block.getTypeData(), 0);
					UUID targetObject = new UUID(block.getTypeData(), 16);
					Vector3d targetPos = new Vector3d(block.getTypeData(), 32);

					OnViewerEffect.dispatch(new ViewerEffectCallbackArgs(type, simulator, sourceAvatar, targetObject,
							targetPos, block.getTypeData()[56], block.Duration, block.ID));
				} else {
					logger.warn(GridClient.Log(
							"Received a LookAt " + type.toString() + " ViewerEffect with an incorrect TypeData size of "
									+ block.getTypeData().length + " bytes",
							_Client));
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
						+ " and length " + block.getTypeData().length + " bytes", _Client));
				break;
			}
		}
	}
}
