/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
 * Portions Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
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
package libomv;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsCallback;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.DisplayNameUpdateMessage;
import libomv.capabilities.IMessage;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.UUIDNameReplyPacket;
import libomv.packets.UUIDNameRequestPacket;
import libomv.primitives.Avatar;
import libomv.types.UUID;
import libomv.types.PacketCallback;
import libomv.types.Vector3d;
import libomv.utils.CallbackArgs;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;

public class AvatarManager implements PacketCallback, CapsCallback
{
	// Information about agents display name
	public class AgentDisplayName
	{
		// Agent UUID
		public UUID ID;
		// Username
		public String UserName;
		// Display name
		public String DisplayName;
		// First name (legacy)
		public String LegacyFirstName;
		// Last name (legacy)
		public String LegacyLastName;
		// Full name (legacy)
		public String LegacyFullName;
		// Is display name default display name </summary>
		public boolean IsDefaultDisplayName;
		// Cache display name until
		public Date NextUpdate;

		public String getLegacyFullName()
		{
			return String.format("%s %s", LegacyFirstName, LegacyLastName);
		}

		/**
		 * Creates AgentDisplayName object from OSD
		 * 
		 * @param data
		 *            Incoming OSD data AgentDisplayName object
		 */
		public AgentDisplayName FromOSD(OSD data)
		{
			AgentDisplayName ret = new AgentDisplayName();

			OSDMap map = (OSDMap) data;
			ret.ID = map.get("id").AsUUID();
			ret.UserName = map.get("username").AsString();
			ret.DisplayName = map.get("display_name").AsString();
			ret.LegacyFirstName = map.get("legacy_first_name").AsString();
			ret.LegacyLastName = map.get("legacy_last_name").AsString();
			ret.IsDefaultDisplayName = map.get("is_display_name_default").AsBoolean();
			ret.NextUpdate = map.get("display_name_next_update").AsDate();

			return ret;
		}

		/**
		 * Return object as OSD map
		 * 
		 * @returns OSD containing agent's display name data
		 */
		public OSD GetOSD()
		{
			OSDMap map = new OSDMap();

			map.put("id", OSD.FromUUID(ID));
			map.put("username", OSD.FromString(UserName));
			map.put("display_name", OSD.FromString(DisplayName));
			map.put("legacy_first_name", OSD.FromString(LegacyFirstName));
			map.put("legacy_last_name", OSD.FromString(LegacyLastName));
			map.put("is_display_name_default", OSD.FromBoolean(IsDefaultDisplayName));
			map.put("display_name_next_update", OSD.FromDate(NextUpdate));

			return map;
		}

		@Override
		public String toString()
		{
			return Helpers.StructToString(this);
		}
	}

	/**
	 * Contains an animation currently being played by an agent
	 */
	public class Animation
	{
		// The ID of the animation asset
		public UUID AnimationID;
		// A number to indicate start order of currently playing animations
		// On Linden Grids this number is unique per region, with OpenSim it is
		// per client</remarks>
		public int AnimationSequence;
		//
		public UUID AnimationSourceObjectID;
	}

	/**
	 * Holds group information on an individual profile pick
	 */
	public class ProfilePick
	{
		public UUID PickID;
		public UUID CreatorID;
		public boolean TopPick;
		public UUID ParcelID;
		public String Name;
		public String Desc;
		public UUID SnapshotID;
		public String User;
		public String OriginalName;
		public String SimName;
		public Vector3d PosGlobal;
		public int SortOrder;
		public boolean Enabled;
	}

	public class ClassifiedAd
	{
		public UUID ClassifiedID;
		public int Catagory;
		public UUID ParcelID;
		public int ParentEstate;
		public UUID SnapShotID;
		public Vector3d Position;
		public byte ClassifiedFlags;
		public int Price;
		public String Name;
		public String Desc;
	}

	/**
	 * Holds group information for Avatars such as those you might find in a
	 * profile
	 */
	public final class AvatarGroup
	{
		/* true of Avatar accepts group notices */
		public boolean AcceptNotices;
		/* Groups Key */
		public UUID GroupID;
		/* Texture Key for groups insignia */
		public UUID GroupInsigniaID;
		/* Name of the group */
		public String GroupName;
		/* Powers avatar has in the group */
		public long GroupPowers;
		/* Avatars Currently selected title */
		public String GroupTitle;
		/* true of Avatar has chosen to list this in their profile */
		public boolean ListInProfile;
	}

	private GridClient _Client;

	/* HashMap containing all known avatars to this client */ 
	private HashMap<UUID, Avatar> _Avatars;

	public class AgentNamesCallbackArgs implements CallbackArgs
	{
		private HashMap<UUID, String> names;

		public HashMap<UUID, String> getNames()
		{
			return names;
		}

		public AgentNamesCallbackArgs(HashMap<UUID, String> names)
		{
			this.names = names;
		}
	}

	public CallbackHandler<AgentNamesCallbackArgs> OnAgentNames = new CallbackHandler<AgentNamesCallbackArgs>();

	/**
	 * Event args class for display name notification messages
	 */
	public class DisplayNameUpdateCallbackArgs implements CallbackArgs
	{
		private String oldDisplayName;
		private AgentDisplayName displayName;

		public String getOldDisplayName()
		{
			return oldDisplayName;
		}

		public AgentDisplayName getDisplayName()
		{
			return displayName;
		}

		public DisplayNameUpdateCallbackArgs(String oldDisplayName, AgentDisplayName displayName)
		{
			this.oldDisplayName = oldDisplayName;
			this.displayName = displayName;
		}
	}

	public CallbackHandler<DisplayNameUpdateCallbackArgs> OnDisplayNameUpdate = new CallbackHandler<DisplayNameUpdateCallbackArgs>();

	public AvatarManager(GridClient client)
	{
		_Client = client;
		_Avatars = new HashMap<UUID, Avatar>();

		// Avatar appearance callback
		_Client.Network.RegisterCallback(PacketType.AvatarAppearance, this);

		// Avatar profile callbacks
		_Client.Network.RegisterCallback(PacketType.AvatarPropertiesReply, this);
		// Client.Network.RegisterCallback(PacketType.AvatarStatisticsReply,
		// AvatarStatisticsHandler);
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

		// Picks callbacks
		_Client.Network.RegisterCallback(PacketType.AvatarPicksReply, this);
		_Client.Network.RegisterCallback(PacketType.PickInfoReply, this);

		// Classifieds callbacks
		_Client.Network.RegisterCallback(PacketType.AvatarClassifiedReply, this);
		_Client.Network.RegisterCallback(PacketType.ClassifiedInfoReply, this);

		_Client.Network.RegisterCallback(CapsEventType.DisplayNameUpdate, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case AvatarAppearance:
				// HandleAvatarAppearance(packet, simulator);
				break;
			case AvatarPropertiesReply:
				// HandleAvatarProperties(packet, simulator);
				break;
			case AvatarInterestsReply:
				// HandleAvatarInterests(packet, simulator);
				break;
			case AvatarGroupsReply:
				// HandleAvatarGroupsReply(packet, simulator);
				break;
			case ViewerEffect:
				// HandleViewerEffect(packet, simulator);
				break;
			case UUIDNameReply:
				HandleUUIDNameReply(packet, simulator);
				break;
			case AvatarPickerReply:
				// HandleAvatarPickerReply(packet, simulator);
				break;
			case AvatarAnimation:
				// HandleAvatarAnimation(packet, simulator);
				break;
			case AvatarPicksReply:
				// HandleAvatarPicksReply(packet, simulator);
				break;
			case PickInfoReply:
				// HandlePickInfoReply(packet, simulator);
				break;
			case AvatarClassifiedReply:
				// HandleAvatarClassifiedReply(packet, simulator);
				break;
			case ClassifiedInfoReply:
				// HandleClassifiedInfoReply(packet, simulator);
				break;
		}
	}

	@Override
	public void capsCallback(IMessage message, Simulator simulator)
	{
		switch (message.getType())
		{
			case DisplayNameUpdate:
				HandleDisplayNameUpdate(message, simulator);
				break;
			case AgentGroupDataUpdate:
				// HandleAgentGroupDataUpdate(message, simulator);
				break;
			case AvatarGroupsReply:
				// HandleAvatarGroupsReply(message, simulator);
				break;
		}
	}

	/**
	 * Add an Avatar into the Avatars Dictionary
	 * 
	 * @param avatar
	 *            Filled-out Avatar class to insert
	 */
	public void add(Avatar avatar)
	{ 
		synchronized (_Avatars)
		{
			_Avatars.put(avatar.ID, avatar);
		}
	}

	public boolean contains(UUID id)
	{
		synchronized (_Avatars)
		{
			return _Avatars.containsKey(id);
		}
	}

	/**
	 * This function will only check if the avatar name exists locally, it will
	 * not do any networking calls to fetch the name
	 * 
	 * @param id
	 *            The uuid of the avatar to get the name for
	 * @return The avatar name, or an empty String if it's not found
	 */
	public String LocalAvatarNameLookup(UUID id)
	{
		String name = Helpers.EmptyString;

		synchronized (_Avatars)
		{
			if (_Avatars.containsKey(id))
			{
				name = _Avatars.get(id).getName();
			}
		}
		return name;
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
	public void RequestAvatarName(UUID id, Callback<AgentNamesCallbackArgs> anc) throws Exception
	{
		// TODO: BeginGetAvatarNames is pretty bulky, rewrite a simple version
		// here
		ArrayList<UUID> ids = new ArrayList<UUID>();
		ids.add(id);

		RequestAvatarNames(ids, anc);
	}

	//
	// <param name="ids"></param>
	public void RequestAvatarNames(ArrayList<UUID> ids, Callback<AgentNamesCallbackArgs> anc) throws Exception
	{
		if (anc != null)
		{
			OnAgentNames.add(anc);
		}

		HashMap<UUID, String> havenames = new HashMap<UUID, String>();
		ArrayList<UUID> neednames = new ArrayList<UUID>();

		synchronized (_Avatars)
		{
			// Fire callbacks for the ones we already have cached
			Iterator<UUID> iter = ids.listIterator();
			while (iter.hasNext())
			{
				UUID id = iter.next();
				if (_Avatars.containsKey(id))
				{
					havenames.put(id, _Avatars.get(id).getName());
				}
				else
				{
					neednames.add(id);
				}
			}
		}
		
		if (havenames.size() > 0)
		{
			OnAgentNames.dispatch(new AgentNamesCallbackArgs(havenames));
		}

		if (neednames.size() > 0)
		{
			UUIDNameRequestPacket request = new UUIDNameRequestPacket();

			request.UUIDNameBlock = new UUIDNameRequestPacket.UUIDNameBlockBlock[neednames.size()];

			for (int i = 0; i < neednames.size(); i++)
			{
				request.UUIDNameBlock[i] = request.createUUIDNameBlockBlock();
				request.UUIDNameBlock[i].ID = neednames.get(i);
			}
			_Client.Network.SendPacket(request);
		}
		else
		{
			OnAgentNames.remove(anc);			
		}
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
	private void HandleUUIDNameReply(Packet packet, Simulator simulator) throws Exception
	{
		HashMap<UUID, String> names = new HashMap<UUID, String>();
		UUIDNameReplyPacket reply = (UUIDNameReplyPacket) packet;

		synchronized (_Avatars)
		{
			for (UUIDNameReplyPacket.UUIDNameBlockBlock block : reply.UUIDNameBlock)
			{
				if (!_Avatars.containsKey(block.ID))
				{
					_Avatars.put(block.ID, new Avatar(block.ID));
				}

				_Avatars.get(block.ID).setNames(Helpers.BytesToString(block.getFirstName()),
						Helpers.BytesToString(block.getLastName()));
				names.put(block.ID, _Avatars.get(block.ID).getName());
			}
		}
		OnAgentNames.dispatch(new AgentNamesCallbackArgs(names));
	}

	/**
	 * EQ Message fired when someone nearby changes their display name
	 */
	private void HandleDisplayNameUpdate(IMessage message, Simulator simulator)
	{
		DisplayNameUpdateMessage msg = (DisplayNameUpdateMessage) message;
		synchronized (_Avatars)
		{
			UUID id = msg.DisplayName.ID;
			if (!_Avatars.containsKey(id))
			{
				_Avatars.put(id, new Avatar(id));
			}
			_Avatars.get(id).setDisplayName(msg.DisplayName.DisplayName);
		}
		OnDisplayNameUpdate.dispatch(new DisplayNameUpdateCallbackArgs(msg.OldDisplayName, msg.DisplayName));
	}

}
