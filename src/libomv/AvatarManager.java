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

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

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
import libomv.utils.CallbackHandler;
import libomv.utils.CallbackHandlerQueue;
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
         * @param data Incoming OSD data
         * AgentDisplayName object
         */
        public AgentDisplayName FromOSD(OSD data)
        {
            AgentDisplayName ret = new AgentDisplayName();

            OSDMap map = (OSDMap)data;
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
        // On Linden Grids this number is unique per region, with OpenSim it is per client</remarks>
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
     * Holds group information for Avatars such as those you might find in a profile
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

	
	private GridClient Client;

	private Hashtable<UUID, Avatar> Avatars;

	public class AgentNamesCallbackArgs extends CallbackArgs
	{
		private Hashtable<UUID, String> names;
		
		public Hashtable<UUID, String> getNames()
		{
			return names;
		}
		
		public AgentNamesCallbackArgs(Hashtable<UUID, String> names)
		{
			this.names = names;
		}
	}
	
	public CallbackHandlerQueue<AgentNamesCallbackArgs> OnAgentNames = new CallbackHandlerQueue<AgentNamesCallbackArgs>();

    /**
     * Event args class for display name notification messages
     */
    public class DisplayNameUpdateCallbackArgs extends CallbackArgs
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

    public CallbackHandlerQueue<DisplayNameUpdateCallbackArgs> OnDisplayNameUpdate = new CallbackHandlerQueue<DisplayNameUpdateCallbackArgs>();


    public AvatarManager(GridClient client)
    {
		Client = client;
		Avatars = new Hashtable<UUID, Avatar>();

        // Avatar appearance callback
        Client.Network.RegisterCallback(PacketType.AvatarAppearance, this);

        // Avatar profile callbacks
        Client.Network.RegisterCallback(PacketType.AvatarPropertiesReply, this);
        // Client.Network.RegisterCallback(PacketType.AvatarStatisticsReply, AvatarStatisticsHandler);
        Client.Network.RegisterCallback(PacketType.AvatarInterestsReply, this);

        // Avatar group callback
        Client.Network.RegisterCallback(PacketType.AvatarGroupsReply, this);
        Client.Network.RegisterCallback(CapsEventType.AgentGroupDataUpdate, this);
        Client.Network.RegisterCallback(CapsEventType.AvatarGroupsReply, this);

        // Viewer effect callback
        Client.Network.RegisterCallback(PacketType.ViewerEffect, this);

        // Other callbacks
        Client.Network.RegisterCallback(PacketType.UUIDNameReply, this);
        Client.Network.RegisterCallback(PacketType.AvatarPickerReply, this);
        Client.Network.RegisterCallback(PacketType.AvatarAnimation, this);

        // Picks callbacks
        Client.Network.RegisterCallback(PacketType.AvatarPicksReply, this);
        Client.Network.RegisterCallback(PacketType.PickInfoReply, this);

        // Classifieds callbacks
        Client.Network.RegisterCallback(PacketType.AvatarClassifiedReply, this);
        Client.Network.RegisterCallback(PacketType.ClassifiedInfoReply, this);

        Client.Network.RegisterCallback(CapsEventType.DisplayNameUpdate, this);
    }

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
            case AvatarAppearance:
//            	AvatarAppearanceHandler(packet, simulator);
            	break;
            case AvatarPropertiesReply:
//            	AvatarPropertiesHandler(packet, simulator);
            	break;
            case AvatarInterestsReply:
//            	AvatarInterestsHandler(packet, simulator);
            	break;
            case AvatarGroupsReply:
//            	AvatarGroupsReplyHandler(packet, simulator);
            	break;
            case ViewerEffect:
//            	ViewerEffectHandler(packet, simulator);
            	break;
            case UUIDNameReply:
            	UUIDNameReplyHandler(packet, simulator);
            	break;
            case AvatarPickerReply:
//            	AvatarPickerReplyHandler(packet, simulator);
            	break;
            case AvatarAnimation:
//            	AvatarAnimationHandler(packet, simulator);
            	break;
           case AvatarPicksReply:
//            	AvatarPicksReplyHandler(packet, simulator);
            	break;
            case PickInfoReply:
//            	PickInfoReplyHandler(packet, simulator);
            	break;
            case AvatarClassifiedReply:
//            	AvatarClassifiedReplyHandler(packet, simulator);
            	break;
            case ClassifiedInfoReply:
//            	ClassifiedInfoReplyHandler(packet, simulator);
            	break;
		}
	}
	
	@Override
	public void capsCallback(IMessage message, Simulator simulator)
	{
		switch (message.getType())
		{
			case DisplayNameUpdate:
				DisplayNameUpdateMessageHandler(message, simulator);
				break;
		}
	}	
	
	
	// Add an Avatar into the Avatars Dictionary
	// <param name="avatar">Filled-out Avatar class to insert</param>
	public void AddAvatar(Avatar avatar) {
		synchronized (Avatars) {
			Avatars.put(avatar.ID, avatar);
		}
	}

	public boolean Contains(UUID id) {
		return Avatars.containsKey(id);
	}

	// This function will only check if the avatar name exists locally,
	// it will not do any networking calls to fetch the name
	// <returns>The avatar name, or an empty String if it's not found</returns>
	public String LocalAvatarNameLookup(UUID id)
	{
		String name = Helpers.EmptyString;

		synchronized (Avatars)
		{
			if (Avatars.containsKey(id))
			{
				name = Avatars.get(id).getName();
			}
		}
		return name;
	}

	// 
	// <param name="id"></param>
	public void RequestAvatarName(UUID id, CallbackHandler<AgentNamesCallbackArgs> anc) throws Exception {
		// TODO: BeginGetAvatarNames is pretty bulky, rewrite a simple version
		// here

		Vector<UUID> ids = new Vector<UUID>();
		ids.addElement(id);

		RequestAvatarNames(ids, anc);
	}

	// 
	// <param name="ids"></param>
	public void RequestAvatarNames(Vector<UUID> ids,CallbackHandler<AgentNamesCallbackArgs> anc)
			throws Exception {
		if (anc != null) {
			OnAgentNames.add(anc);
		}

		Hashtable<UUID, String> havenames = new Hashtable<UUID, String>();
		Vector<UUID> neednames = new Vector<UUID>();

		// Fire callbacks for the ones we already have cached
		for (int i = 0; i < ids.size(); i++)
		{
			UUID id = ids.elementAt(i);
			if (Avatars.containsKey(id))
			{
				havenames.put(id, Avatars.get(id).getName());
			}
			else
			{
				neednames.addElement(id);
			}
		}

		if (havenames.size() > 0 && OnAgentNames != null)
		{
			OnAgentNames.dispatch(new AgentNamesCallbackArgs(havenames));
		}

		if (neednames.size() > 0)
		{
			UUIDNameRequestPacket request = new UUIDNameRequestPacket();

			request.UUIDNameBlock = new UUIDNameRequestPacket.UUIDNameBlockBlock[neednames
					.size()];

			for (int i = 0; i < neednames.size(); i++)
			{
				request.UUIDNameBlock[i] = request.createUUIDNameBlockBlock();
				request.UUIDNameBlock[i].ID = neednames.elementAt(i);
			}
			Client.Network.SendPacket(request);
		}
	}

	/** Process an incoming UUIDNameReply Packet and insert Full Names into the
	 * 
	 * @param packet Incoming Packet to process
	 * @param simulator Unused
	 * @throws Exception
	 */
	private void UUIDNameReplyHandler(Packet packet, Simulator simulator)
			throws Exception {
		Hashtable<UUID, String> names = new Hashtable<UUID, String>();
		UUIDNameReplyPacket reply = (UUIDNameReplyPacket) packet;

		synchronized (Avatars) {
			for (UUIDNameReplyPacket.UUIDNameBlockBlock block : reply.UUIDNameBlock) {
				if (!Avatars.containsKey(block.ID)) {
					Avatars.put(block.ID, new Avatar(block.ID));
				}

				Avatars.get(block.ID).setNames(Helpers.BytesToString(block.getFirstName()), Helpers.BytesToString(block.getLastName()));
				names.put(block.ID, Avatars.get(block.ID).getName());
			}
		}

		if (OnAgentNames != null) {
			OnAgentNames.dispatch(new AgentNamesCallbackArgs(names));
		}
	}
	
    /**
     * EQ Message fired when someone nearby changes their display name
     */
    private void DisplayNameUpdateMessageHandler(IMessage message, Simulator simulator)
    {
        DisplayNameUpdateMessage msg = (DisplayNameUpdateMessage) message;
        OnDisplayNameUpdate.dispatch(new DisplayNameUpdateCallbackArgs(msg.OldDisplayName, msg.DisplayName));
    }

}
