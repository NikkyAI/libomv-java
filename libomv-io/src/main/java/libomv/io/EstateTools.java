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
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.LandStatReplyMessage;
import libomv.capabilities.IMessage;
import libomv.io.capabilities.CapsCallback;
import libomv.model.Asset.AssetType;
import libomv.model.Simulator;
import libomv.packets.EjectUserPacket;
import libomv.packets.EstateCovenantReplyPacket;
import libomv.packets.EstateCovenantRequestPacket;
import libomv.packets.EstateOwnerMessagePacket;
import libomv.packets.FreezeUserPacket;
import libomv.packets.LandStatReplyPacket;
import libomv.packets.LandStatRequestPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.SimWideDeletesPacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;

public class EstateTools implements PacketCallback, CapsCallback, libomv.model.Estate
{
	private static final Logger logger = Logger.getLogger(EstateTools.class);

	private GridClient _Client;

    /// Textures for each of the four terrain height levels
    public GroundTextureSettings GroundTextures;

    /// Upper/lower texture boundaries for each corner of the sim
    public GroundTextureHeightSettings GroundTextureLimits;

    // #region Enums
    // Used in the ReportType field of a LandStatRequest
    public enum LandStatReportType
    {
        TopScripts,
        TopColliders;

        public static LandStatReportType setValue(int value)
		{
			return values()[value];
		}
    }

    // Used by EstateOwnerMessage packets
    public enum EstateAccessDelta 
    {
    	None(0),
        BanUser(64),
        BanUserAllEstates(66),
        UnbanUser(128),
        UnbanUserAllEstates(130),
        AddManager(256),
        AddManagerAllEstates(257),
        RemoveManager(512),
        RemoveManagerAllEstates(513),
        AddUserAsAllowed(4),
        AddAllowedAllEstates(6),
        RemoveUserAsAllowed(8),
        RemoveUserAllowedAllEstates(10),
        AddGroupAsAllowed(16),
        AddGroupAllowedAllEstates(18),
        RemoveGroupAsAllowed(32),
        RemoveGroupAllowedAllEstates(34);

        public static EstateAccessDelta setValue(int value)
		{
			for (EstateAccessDelta e : values())
			{
				if (e.val == value)
					return e;
			}
			return None;
		}

		public int getValue()
		{
			return val;
		}

		private int val;

		private EstateAccessDelta(int value)
		{
			val = value;
		}
    }

    // Used by EstateOwnerMessage packets
    public enum EstateAccessReplyDelta
    {
    	None(0),
        AllowedUsers(17),
        AllowedGroups(18),
        EstateBans(20),
        EstateManagers(24);

        public static EstateAccessReplyDelta setValue(int value)
		{
			for (EstateAccessReplyDelta e : values())
			{
				if (e.val == value)
					return e;
			}
			return None;
		}

		public int getValue()
		{
			return val;
		}

		private int val;

		private EstateAccessReplyDelta(int value)
		{
			val = value;
		}
    }

    public enum EstateReturnFlags
    {
        /// <summary>No flags set</summary>
        None(2),
        /// <summary>Only return targets scripted objects</summary>
        ReturnScripted(6),
        /// <summary>Only return targets objects if on others land</summary>
        ReturnOnOthersLand(3),
        /// <summary>Returns target's scripted objects and objects on other parcels</summary>
        ReturnScriptedAndOnOthers(7);

        public static EstateReturnFlags setValue(int value)
		{
			for (EstateReturnFlags e : values())
			{
				if (e.val == value)
					return e;
			}
			return None;
		}

		public int getValue()
		{
			return val;
		}

		private int val;

		private EstateReturnFlags(int value)
		{
			val = value;
		}
    }
    // #endregion

    // #region Structs
    
    /**
     * Ground texture settings for each corner of the region
     * TODO: maybe move this class to the Simulator object and implement it there too   
     */ 
    public class GroundTextureSettings
    {
        public UUID Low;
        public UUID MidLow;
        public UUID MidHigh;
        public UUID High;
    }

    /**
     * Used by GroundTextureHeightSettings
     */
    public class GroundTextureHeight
    {
        public float Low;
        public float High;
    }

    /**
     * The high and low texture thresholds for each corner of the sim
     */
    public class GroundTextureHeightSettings
    {
        public GroundTextureHeight SW;
        public GroundTextureHeight NW;
        public GroundTextureHeight SE;
        public GroundTextureHeight NE;
    }

    /**
     * Describes tasks returned in LandStatReply
     */
    public class EstateTask
    {
        public Vector3 Position;
        public float Score;
        public float MonoScore;
        public UUID TaskID;
        public int TaskLocalID;
        public String TaskName;
        public String OwnerName;
    }
    // #endregion

    // #region EstateTools CallbackArgs Classes
    /**
     * Raised on LandStatReply when the report type is for "top colliders"
     */
    public class TopCollidersReplyCallbackArgs implements CallbackArgs
    {
        private final int m_objectCount;
        private final HashMap<UUID, EstateTask> m_Tasks;

        // The number of returned items in LandStatReply
        public int getObjectCount()
        {
        	return m_objectCount;
        }
        
        // A Dictionary of Object UUIDs to tasks returned in LandStatReply
        public HashMap<UUID, EstateTask> getTasks()
        {
        	return m_Tasks;
        }

        /**
         * Construct a new instance of the TopCollidersReplyEventArgs class
         * 
         * @param objectCount The number of returned items in LandStatReply
         * @param tasks Dictionary of Object UUIDs to tasks returned in LandStatReply
         */
        public TopCollidersReplyCallbackArgs(int objectCount, HashMap<UUID, EstateTask> tasks)
        {
            this.m_objectCount = objectCount;
            this.m_Tasks = tasks;
        }
    }

	public CallbackHandler<TopCollidersReplyCallbackArgs> OnTopCollidersReply = new CallbackHandler<TopCollidersReplyCallbackArgs>();

    /**
     * Raised on LandStatReply when the report type is for "top Scripts"
     */
    public class TopScriptsReplyCallbackArgs implements CallbackArgs
    {
        private final int m_objectCount;
        private final HashMap<UUID, EstateTask> m_Tasks;

        // The number of scripts returned in LandStatReply
        public int getObjectCount()
        {
        	return m_objectCount;
        }
        
        // A Dictionary of Object UUIDs to tasks returned in LandStatReply
        public HashMap<UUID, EstateTask> getTasks()
        {
        	return m_Tasks;
        }

        /**
         * Construct a new instance of the TopScriptsReplyEventArgs class
         * 
         * @param objectCount The number of returned items in LandStatReply
         * @param tasks Dictionary of Object UUIDs to tasks returned in LandStatReply
         */
        public TopScriptsReplyCallbackArgs(int objectCount, HashMap<UUID, EstateTask> tasks)
        {
            this.m_objectCount = objectCount;
            this.m_Tasks = tasks;
        }
    }

	public CallbackHandler<TopScriptsReplyCallbackArgs> OnTopScriptsReply = new CallbackHandler<TopScriptsReplyCallbackArgs>();

    /**
     * Returned, along with other info, upon a successful .RequestInfo()
     */
    public class EstateBansReplyCallbackArgs implements CallbackArgs
    {
        private final int m_estateID;
        private final int m_count;
        private final ArrayList<UUID> m_banned;

        // The identifier of the estate
        public int getEstateID()
        {
        	return m_estateID;
        }
        
        // The number of returned items
        public int getCount()
        {
        	return m_count;
        }

        // List of UUIDs of Banned Users
        public ArrayList<UUID> getBanned()
        {
        	return m_banned;
        }

        /**
         * Construct a new instance of the EstateBansReplyEventArgs class
         * 
         * @param estateID The estate's identifier on the grid
         * @param count The number of returned items in LandStatReply
         * @param banned User UUIDs banned
         */
        public EstateBansReplyCallbackArgs(int estateID, int count, ArrayList<UUID> banned)
        {
            this.m_estateID = estateID;
            this.m_count = count;
            this.m_banned = banned;
        }
    }

	public CallbackHandler<EstateBansReplyCallbackArgs> OnEstateBansReply = new CallbackHandler<EstateBansReplyCallbackArgs>();

    /**
     * Returned, along with other info, upon a successful .RequestInfo()
     */
    public class EstateUsersReplyCallbackArgs implements CallbackArgs
    {
        private final int m_estateID;
        private final int m_count;
        private final ArrayList<UUID> m_allowedUsers;

        // The identifier of the estate
        public int getEstateID()
        {
        	return m_estateID;
        }
        
        // The number of returned items
        public int getCount()
        {
        	return m_count;
        }

        // List of UUIDs of Allowed Users
        public ArrayList<UUID> getAllowedUsers()
		{
        	return m_allowedUsers;
        }

        /**
         * Construct a new instance of the EstateUsersReplyEventArgs class
         * 
         * @param estateID The estate's identifier on the grid
         * @param count The number of returned users in LandStatReply
         * @param allowedUsers Allowed users UUIDs
         */
        public EstateUsersReplyCallbackArgs(int estateID, int count, ArrayList<UUID> allowedUsers)
        {
            this.m_estateID = estateID;
            this.m_count = count;
            this.m_allowedUsers = allowedUsers;
        }
    }

	public CallbackHandler<EstateUsersReplyCallbackArgs> OnEstateUsersReply = new CallbackHandler<EstateUsersReplyCallbackArgs>();

	/**
     * Returned, along with other info, upon a successful .RequestInfo()
     */
    public class EstateGroupsReplyCallbackArgs implements CallbackArgs
    {
        private final int m_estateID;
        private final int m_count;
        private final ArrayList<UUID> m_allowedGroups;

        // The identifier of the estate
        public int getEstateID()
        {
        	return m_estateID;
        }
        
        // The number of returned items
        public int getCount()
        {
        	return m_count;
        }

        // List of UUIDs of Allowed Groups
        public ArrayList<UUID> getAllowedGroups()
        {
        	return m_allowedGroups;
        }

        /**
         * Construct a new instance of the EstateGroupsReplyEventArgs class
		 *
         * @param estateID The estate's identifier on the grid
         * @param count The number of returned groups in LandStatReply
         * @param allowedGroups Allowed Groups UUIDs
         */
        public EstateGroupsReplyCallbackArgs(int estateID, int count, ArrayList<UUID> allowedGroups)
        {
            this.m_estateID = estateID;
            this.m_count = count;
            this.m_allowedGroups = allowedGroups;
        }
    }

	public CallbackHandler<EstateGroupsReplyCallbackArgs> OnEstateGroupsReply = new CallbackHandler<EstateGroupsReplyCallbackArgs>();

	/**
     * Returned, along with other info, upon a successful .RequestInfo()</summary>
     */
    public class EstateManagersReplyCallbackArgs implements CallbackArgs
    {
        private final int m_estateID;
        private final int m_count;
        private final ArrayList<UUID> m_Managers;

        // The identifier of the estate
        public int getEstateID()
        {
        	return m_estateID;
        }
        
        // The number of returned items
        public int getCount()
        {
        	return m_count;
        }

        // List of UUIDs of the Estate's Managers
        public ArrayList<UUID> getManagers()
        {
    	    return m_Managers;
        }
       
        /**
         * Construct a new instance of the EstateManagersReplyEventArgs class
         * 
         * @param estateID The estate's identifier on the grid
         * @param count The number of returned managers in LandStatReply
         * @param managers Managers UUIDs
         */
        public EstateManagersReplyCallbackArgs(int estateID, int count, ArrayList<UUID> managers)
        {
            this.m_estateID = estateID;
            this.m_count = count;
            this.m_Managers = managers;
        }
    }

	public CallbackHandler<EstateManagersReplyCallbackArgs> OnEstateManagersReply = new CallbackHandler<EstateManagersReplyCallbackArgs>();

	/**
     * Returned, along with other info, upon a successful .RequestInfo()</summary>
     */
    public class EstateCovenantReplyCallbackArgs implements CallbackArgs
    {
        private final UUID m_covenantID;
        private final long m_timestamp;
        private final String m_estateName;
        private final UUID m_estateOwnerID;

        // The Covenant
        public UUID getCovenantID()
        {
        	return m_covenantID;
        }

        // The timestamp
        public long getTimestamp()
        {
        	return m_timestamp;
        }

        // The Estate name
        public String getEstateName()
        {
        	return m_estateName;
        }

        // The Estate Owner's ID (can be a GroupID)
        public UUID getEstateOwnerID()
        {
        	return m_estateOwnerID;
        }

        /**
         * Construct a new instance of the EstateCovenantReplyEventArgs class
         * 
         * @param covenantID The Covenant ID
         * @param timestamp The timestamp
         * @param estateName The estate's name
         * @param estateOwnerID The Estate Owner's ID (can be a GroupID)
         */
        public EstateCovenantReplyCallbackArgs(UUID covenantID, long timestamp, String estateName, UUID estateOwnerID)
        {
            this.m_covenantID = covenantID;
            this.m_timestamp = timestamp;
            this.m_estateName = estateName;
            this.m_estateOwnerID = estateOwnerID;
        }
    }

	public CallbackHandler<EstateCovenantReplyCallbackArgs> OnEstateCovenantReply = new CallbackHandler<EstateCovenantReplyCallbackArgs>();

    /**
     * Returned, along with other info, upon a successful .RequestInfo()
     */
    public class EstateUpdateInfoReplyCallbackArgs implements CallbackArgs
    {
        private final int m_estateID;
        private final boolean m_denyNoPaymentInfo;
        private final String m_estateName;
        private final UUID m_estateOwner;

        // The estate's name
        public String getEstateName()
        {
        	return m_estateName;
        }

        // The Estate Owner's ID (can be a GroupID)
        public UUID getEstateOwner()
        {
        	return m_estateOwner;
        }

        // The identifier of the estate on the grid
        public int getEstateID()
        {
        	return m_estateID;
        }

        public boolean getDenyNoPaymentInfo()
        {
        	return m_denyNoPaymentInfo;
        }

        /**
         * Construct a new instance of the EstateUpdateInfoReplyEventArgs class
         * 
         * @param estateName The estate's name
         * @param estateOwner The Estate Owners ID (can be a GroupID)
         * @param estateID The estate's identifier on the grid
         * @param denyNoPaymentInfo
         */
        public EstateUpdateInfoReplyCallbackArgs(String estateName, UUID estateOwner, int estateID, boolean denyNoPaymentInfo)
        {
            this.m_estateName = estateName;
            this.m_estateOwner = estateOwner;
            this.m_estateID = estateID;
            this.m_denyNoPaymentInfo = denyNoPaymentInfo;

        }
    }

	public CallbackHandler<EstateUpdateInfoReplyCallbackArgs> OnEstateUpdateInfoReply = new CallbackHandler<EstateUpdateInfoReplyCallbackArgs>();
    // #endregion

    // @param client
	public EstateTools(GridClient client)
	{
		_Client = client;

		GroundTextures = new GroundTextureSettings();
        GroundTextureLimits = new GroundTextureHeightSettings();

        _Client.Network.RegisterCallback(PacketType.LandStatReply, this);
        _Client.Network.RegisterCallback(PacketType.EstateOwnerMessage, this);
        _Client.Network.RegisterCallback(PacketType.EstateCovenantReply, this);

        _Client.Network.RegisterCallback(CapsEventType.LandStatReply, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case LandStatReply:
				HandleLandStatReply(packet, simulator);
				break;
			case EstateOwnerMessage:
				HandleEstateOwnerMessage(packet, simulator);
				break;
			case EstateCovenantReply:
				HandleEstateCovenantReply(packet, simulator);
				break;
			default:
				break;
		}
	}


	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) throws Exception
	{
		switch (message.getType())
		{
		    case LandStatReply:
			    HandleLandStatReply(message, simulator);
			    break;
			default:
				break;
		}
	}

	/**
     * Requests estate information such as top scripts and colliders
     *
     * @param parcelLocalID
     * @param reportType
     * @param requestFlags
     * @param filter
     * @throws Exception 
     */
    public void LandStatRequest(int parcelLocalID, LandStatReportType reportType, int requestFlags, String filter) throws Exception
    {
        LandStatRequestPacket p = new LandStatRequestPacket();
        p.AgentData.AgentID = _Client.Self.getAgentID();
        p.AgentData.SessionID = _Client.Self.getSessionID();
        p.RequestData.setFilter(Helpers.StringToBytes(filter));
        p.RequestData.ParcelLocalID = parcelLocalID;
        p.RequestData.ReportType = reportType.ordinal();
        p.RequestData.RequestFlags = requestFlags;
        _Client.Network.sendPacket(p);
    }

    /// <summary>Requests estate settings, including estate manager and access/ban lists</summary>
    public void RequestInfo() throws Exception
    {
        EstateOwnerMessage("getinfo", "");
    }

    /// <summary>Requests the "Top Scripts" list for the current region</summary>
    public void RequestTopScripts() throws Exception
    {
        //EstateOwnerMessage("scripts", "");
        LandStatRequest(0, LandStatReportType.TopScripts, 0, "");
    }

    /// <summary>Requests the "Top Colliders" list for the current region</summary>
    public void RequestTopColliders() throws Exception
    {
        //EstateOwnerMessage("colliders", "");
        LandStatRequest(0, LandStatReportType.TopColliders, 0, "");
    }

    /**
     * Set several estate specific configuration variables
     *
     * @param waterHeight The Height of the water level over the entire estate. Defaults to 20
     * @param terrainRaiseLimit The maximum height change allowed above the baked terrain. Defaults to 4
     * @param terrainLowerLimit The minimum height change allowed below the baked terrain. Defaults to -4
     * @param useEstateSun True to use
     * @param fixedSun if True forces the sun position to the position in SunPosition
     * @param sunPosition The current position of the sun on the estate, or when FixedSun is true the static
     *                    position the sun will remain. 
     * @remarks >6.0 = Sunrise, 30.0 = Sunset
     */
    public void SetTerrainVariables(float waterHeight, float terrainRaiseLimit,
        float terrainLowerLimit, boolean useEstateSun, boolean fixedSun, float sunPosition) throws Exception
    {
        ArrayList<String> simVariables = new ArrayList<String>();
        simVariables.add(String.format(Helpers.EnUsCulture, "%f", waterHeight));
        simVariables.add(String.format(Helpers.EnUsCulture, "%f", terrainRaiseLimit));
        simVariables.add(String.format(Helpers.EnUsCulture, "%f", terrainLowerLimit));
        simVariables.add(useEstateSun ? "Y" : "N");
        simVariables.add(fixedSun ? "Y" : "N");
        simVariables.add(String.format(Helpers.EnUsCulture, "%f", sunPosition));
        simVariables.add("Y"); // Not used?
        simVariables.add("N"); // Not used?
        simVariables.add("0.00"); // Also not used?
        EstateOwnerMessage("setregionterrain", simVariables);
    }

    /**
     * Request return of objects owned by specified avatar 
     *
     * @param target The Agents <see cref="UUID"/> owning the primitives to return
     * @param flag specify the coverage and type of objects to be included in the return
     * @param estateWide true to perform return on entire estate
     */
    public void SimWideReturn(UUID target, EstateReturnFlags flag, boolean estateWide) throws Exception
    {
        if (estateWide)
        {
            ArrayList<String> param = new ArrayList<String>();
            param.add(flag.toString());
            param.add(target.toString());
            EstateOwnerMessage("estateobjectreturn", param);
        }
        else
        {
            SimWideDeletesPacket simDelete = new SimWideDeletesPacket();
            simDelete.AgentData.AgentID = _Client.Self.getAgentID();
            simDelete.AgentData.SessionID = _Client.Self.getSessionID();
            simDelete.DataBlock.TargetID = target;
            simDelete.DataBlock.Flags = flag.getValue();
            _Client.Network.sendPacket(simDelete);
        }
    }

    /**
     * Used for setting and retrieving various estate panel settings
     *
     * @param method EstateOwnerMessage Method field
     * @param param Single parameter to include
     * @throws Exception 
     */
    public void EstateOwnerMessage(String method, String param) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>();
        listParams.add(param);
        EstateOwnerMessage(method, listParams);
    }

    /**
     * Used for setting and retrieving various estate panel settings
     *
     * @param method EstateOwnerMessage Method field
     * @param listParams List of parameters to include
     * @throws Exception 
     */
    public void EstateOwnerMessage(String method, ArrayList<String> listParams) throws Exception
    {
        EstateOwnerMessagePacket estate = new EstateOwnerMessagePacket();
        estate.AgentData.AgentID = _Client.Self.getAgentID();
        estate.AgentData.SessionID = _Client.Self.getSessionID();
        estate.AgentData.TransactionID = UUID.Zero;
        estate.MethodData.Invoice = new UUID();
        estate.MethodData.setMethod(Helpers.StringToBytes(method));
        estate.ParamList = new EstateOwnerMessagePacket.ParamListBlock[listParams.size()];
        for (int i = 0; i < listParams.size(); i++)
        {
            estate.ParamList[i] = estate.new ParamListBlock();
            estate.ParamList[i].setParameter(Helpers.StringToBytes(listParams.get(i)));
        }
        _Client.Network.sendPacket(estate);
    }

    /**
     * Kick an avatar from an estate
     *
     * @param userID Key of Agent to remove
     * 
     * @throws Exception 
     */
	public void KickUser(UUID userID) throws Exception
	{
        EstateOwnerMessage("kickestate", userID.toString());
	}

    /**
     * Eject an avatar from an estate
     *
     * @param userID Key of Agent to remove
     * @param ban also ban user from estate if true
     * 
     * @throws Exception 
     */
    public void EjectUser(UUID targetID, boolean ban) throws Exception
    {
        EjectUserPacket eject = new EjectUserPacket();
        eject.AgentData.AgentID = _Client.Self.getAgentID();
        eject.AgentData.SessionID = _Client.Self.getSessionID();
        eject.Data.TargetID = targetID;
        if (ban)
        	eject.Data.Flags = 1;
        else
        	eject.Data.Flags = 0;

        _Client.Network.sendPacket(eject);
    }
    
    /**
     * Freeze or unfreeze an avatar over your land
     *
     * @param targetID target key to freeze
     * @param freeze true to freeze, false to unfreeze
     * 
     * @throws Exception 
     */
    public void FreezeUser(UUID targetID, boolean freeze) throws Exception
    {
        FreezeUserPacket frz = new FreezeUserPacket();
        frz.AgentData.AgentID = _Client.Self.getAgentID();
        frz.AgentData.SessionID = _Client.Self.getSessionID();
        frz.Data.TargetID = targetID;
        if (freeze)
        	frz.Data.Flags = 0;
        else
        	frz.Data.Flags = 1;

        _Client.Network.sendPacket(frz);
    }

    /** 
     * Ban an avatar from an estate
     *
     * @param userID Key of Agent to remove
     * @param allEstates allEstates Ban user from this estate and all others owned by the estate owner
     * 
     * @throws Exception 
     */
	public void BanUser(UUID userID, boolean allEstates) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>();
        Integer flag = allEstates ? EstateAccessDelta.BanUserAllEstates.getValue() : EstateAccessDelta.BanUser.getValue();
        listParams.add(_Client.Self.getAgentID().toString());
        listParams.add(flag.toString());
        listParams.add(userID.toString());
        EstateOwnerMessage("estateaccessdelta", listParams);
    }

    /** 
     * Unban an avatar from an estate
     * 
     * @param userID Key of Agent to remove
     * @param allEstates allEstates Unban user from this estate and all others owned by the estate owner
     * 
     * @throws Exception 
     */
    public void UnbanUser(UUID userID, boolean allEstates) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>();
        Integer flag = allEstates ? EstateAccessDelta.UnbanUserAllEstates.getValue() : EstateAccessDelta.UnbanUser.getValue();
        listParams.add(_Client.Self.getAgentID().toString());
        listParams.add(flag.toString());
        listParams.add(userID.toString());
        EstateOwnerMessage("estateaccessdelta", listParams);
    }

    /**
	 * Send a message dialog to everyone in an entire estate
     *
     * @param message Message to send all users in the estate
     * 
     * @throws Exception 
     */
    public void EstateMessage(String message) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>(2);
        listParams.add(_Client.Self.getName());
        listParams.add(message);
        EstateOwnerMessage("instantmessage", listParams);
    }

    /**
	 * Send a message dialog to everyone in a simulator
     *
     * @param message Message to send all users in the simulator
     * 
     * @throws Exception 
     */
    public void SimulatorMessage(String message) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>(5);
        listParams.add("-1");
        listParams.add("-1");
        listParams.add(_Client.Self.getAgentID().toString());
        listParams.add(_Client.Self.getName());
        listParams.add(message);
        EstateOwnerMessage("simulatormessage", listParams);
    }

    /**
	 * Send an avatar back to their home location
	 *
	 * @param userID Key of avatar to send home
	 * 
     * @throws Exception 
	 */
	public void TeleportHomeUser(UUID userID) throws Exception
	{
        ArrayList<String> listParams = new ArrayList<String>(2);
        listParams.add(_Client.Self.getAgentID().toString());
        listParams.add(userID.toString());
        EstateOwnerMessage("teleporthomeuser", listParams);
	}
	
    /**
	 * Send all avatars back to their home location
	 *
	 * @param userID Key of avatar to send home
	 * 
     * @throws Exception 
	 */
	public void TeleportHomeAllUsers(UUID userID) throws Exception
	{
        ArrayList<String> listParams = new ArrayList<String>();
        listParams.add(_Client.Self.getAgentID().toString());
        EstateOwnerMessage("teleporthomeallusers", listParams);
	}

	/**
	 * Begin the region restart process
	 * 
     * @throws Exception 
    */
     public void RestartRegion() throws Exception
    {
        EstateOwnerMessage("restart", "120");
    }

    /**
	 * Cancels a region restart
	 * 
     * @throws Exception 
     */
    public void CancelRestart() throws Exception
    {
        EstateOwnerMessage("restart", "-1");
    }
    
	/**
     * Estate panel "Region" tab settings
	 * 
	 * @throws Exception 
     */
    public void SetRegionInfo(boolean blockTerraform, boolean blockFly, boolean allowDamage, boolean allowLandResell, boolean restrictPushing, boolean allowParcelJoinDivide, float agentLimit, float objectBonus, boolean mature) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>(9);
        if (blockTerraform) listParams.add("Y"); else listParams.add("N");
        if (blockFly) listParams.add("Y"); else listParams.add("N");
        if (allowDamage) listParams.add("Y"); else listParams.add("N");
        if (allowLandResell) listParams.add("Y"); else listParams.add("N");
        listParams.add(String.format(Helpers.EnUsCulture, "%f", agentLimit));
        listParams.add(String.format(Helpers.EnUsCulture, "%f", objectBonus));
        if (mature) listParams.add("21"); else listParams.add("13"); //FIXME - enumerate these settings
        if (restrictPushing) listParams.add("Y"); else listParams.add("N");
        if (allowParcelJoinDivide) listParams.add("Y"); else listParams.add("N");
        EstateOwnerMessage("setregioninfo", listParams);
    }

	/**
     * Estate panel "Debug" tab settings
	 * 
	 * @throws Exception 
     */
    public void SetRegionDebug(boolean disableScripts, boolean disableCollisions, boolean disablePhysics) throws Exception
    {
    	ArrayList<String> listParams = new ArrayList<String>(3);
    	if (disableScripts) listParams.add("Y"); else listParams.add("N");
        if (disableCollisions) listParams.add("Y"); else listParams.add("N");
        if (disablePhysics) listParams.add("Y"); else listParams.add("N");
        EstateOwnerMessage("setregiondebug", listParams);
    }

	/**
     * Used for setting the region's terrain textures for its four height levels
     * 
     * @param low
     * @param midLow
     * @param midHigh
     * @param high
	 * 
	 * @throws Exception 
     */
    public void SetRegionTerrain(UUID low, UUID midLow, UUID midHigh, UUID high) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>(4);
        listParams.add("0 " + low.toString());
        listParams.add("1 " + midLow.toString());
        listParams.add("2 " + midHigh.toString());
        listParams.add("3 " + high.toString());
        EstateOwnerMessage("texturedetail", listParams);
        EstateOwnerMessage("texturecommit", "");
    }

	/**
     * Used for setting sim terrain texture heights
	 * 
	 * @throws Exception 
     */
    public void SetRegionTerrainHeights(float lowSW, float highSW, float lowNW, float highNW, float lowSE, float highSE, float lowNE, float highNE) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>(4);
        listParams.add(String.format(Helpers.EnUsCulture, "0 %f %f", lowSW, highSW)); //SW low-high 
        listParams.add(String.format(Helpers.EnUsCulture, "1 %f %f", lowNW, highNW)); //NW low-high 
        listParams.add(String.format(Helpers.EnUsCulture, "2 %f %f", lowSE, highSE)); //SE low-high 
        listParams.add(String.format(Helpers.EnUsCulture, "3 %f %f", lowNE, highNE)); //NE low-high 
        EstateOwnerMessage("textureheights", listParams);
        EstateOwnerMessage("texturecommit", "");
    }

	/**
     * Requests the estate covenant
	 * 
	 * @throws Exception 
     */
    public void RequestCovenant() throws Exception
    {
        EstateCovenantRequestPacket req = new EstateCovenantRequestPacket();
        req.AgentData.AgentID = _Client.Self.getAgentID();
        req.AgentData.SessionID = _Client.Self.getSessionID();
        _Client.Network.sendPacket(req);
    }

	/**
     * Upload a terrain RAW file
     *
     * @param fileData A byte array containing the encoded terrain data
     * @param fileName The name of the file being uploaded
	 * @throws Exception 
     * @returns The Id of the transfer request
     */
    public UUID UploadTerrain(byte[] fileData, String fileName) throws Exception
    {
        // Tell the library we have a pending file to upload
        UUID transactionID = _Client.Assets.RequestUpload(AssetType.Unknown, fileData, false);

        // Create and populate a list with commands specific to uploading a raw terrain file
        ArrayList<String> paramList = new ArrayList<String>();
        paramList.add("upload filename");
        paramList.add(fileName);

        // Tell the simulator we have a new raw file to upload
        EstateOwnerMessage("terrain", paramList);

        return transactionID;
    }

	/**
     * Teleports all users home in current Estate
	 * 
	 * @throws Exception 
     */
    public void TeleportHomeAllUsers() throws Exception
    {
        ArrayList<String> params = new ArrayList<String>(1);
        params.add(_Client.Self.getAgentID().toString());
        EstateOwnerMessage("teleporthomeallusers", params);
    }

    /**
     * Remove estate manager
     * 
     * @param userID Key of Agent to Remove
     * @param allEstates removes manager to this estate and all others owned by the estate owner
     * 
     * @throws Exception 
     */
    public void RemoveEstateManager(UUID userID, boolean allEstates) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>(3);
        Integer flag = allEstates ? EstateAccessDelta.RemoveManagerAllEstates.getValue() : EstateAccessDelta.RemoveManager.getValue();
        listParams.add(_Client.Self.getAgentID().toString());
        listParams.add(flag.toString());
        listParams.add(userID.toString());
        EstateOwnerMessage("estateaccessdelta", listParams);
    }

    /**
     * Add estate manager
     *
     * @param userID Key of agent to add
     * @param allEstates Add agent as manager to this estate and all others owned by the estate owner
     * 
     * @throws Exception 
     */
    public void AddEstateManager(UUID userID, boolean allEstates) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>(3);
        Integer flag = allEstates ? EstateAccessDelta.AddManagerAllEstates.getValue() : EstateAccessDelta.AddManager.getValue();
        listParams.add(_Client.Self.getAgentID().toString());
        listParams.add(flag.toString());
        listParams.add(userID.toString());
        EstateOwnerMessage("estateaccessdelta", listParams);
    }

    /**
     * Add's an agent to the estate allowed list
     * 
     * @param userID Key of Agent to add
     * @param allEstates Add agent as an allowed resident to all estates if true\
     * 
     * @throws Exception 
     */
    public void AddAllowedUser(UUID userID, boolean allEstates) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>(3);
        Integer flag = allEstates ? EstateAccessDelta.AddAllowedAllEstates.getValue() : EstateAccessDelta.AddUserAsAllowed.getValue();
        listParams.add(_Client.Self.getAgentID().toString());
        listParams.add(flag.toString());
        listParams.add(userID.toString());
        EstateOwnerMessage("estateaccessdelta", listParams);
    }

    /**
     * Removes an agent from the estate Allowed list
     * 
     * @param userID Key of Agent to Remove
     * @param allEstates Removes agent as an allowed resident from all estates if true
     * @throws Exception 
     */
    public void RemoveAllowedUser(UUID userID, boolean allEstates) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>(3);
        Integer flag = allEstates ? EstateAccessDelta.RemoveUserAllowedAllEstates.getValue() : EstateAccessDelta.RemoveUserAsAllowed.getValue();
        listParams.add(_Client.Self.getAgentID().toString());
        listParams.add(flag.toString());
        listParams.add(userID.toString());
        EstateOwnerMessage("estateaccessdelta", listParams);
    }
    
    /**
     * Add's a group to the estate Allowed list
     * 
     * @param groupID Key of group to add
     * @param allEstates Add group as an allowed group to All estates if true
     * 
     * @throws Exception 
     */
    public void AddAllowedGroup(UUID groupID, boolean allEstates) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>(3);
        Integer flag = allEstates ? EstateAccessDelta.AddGroupAllowedAllEstates.getValue() : EstateAccessDelta.AddGroupAsAllowed.getValue();

        listParams.add(_Client.Self.getAgentID().toString());
        listParams.add(flag.toString());
        listParams.add(groupID.toString());
        EstateOwnerMessage("estateaccessdelta", listParams);
    }
    
    /**
     * Removes a group from the estate Allowed list
     * 
     * @param groupID Key of group to remove</param>
     * @param allEstates Removes group as an allowed group from all estates if true
     * 
     * @throws Exception 
     */
    public void RemoveAllowedGroup(UUID groupID, boolean allEstates) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>(3);
        Integer flag = allEstates ? EstateAccessDelta.RemoveGroupAllowedAllEstates.getValue() : EstateAccessDelta.RemoveGroupAsAllowed.getValue();
        listParams.add(_Client.Self.getAgentID().toString());
        listParams.add(flag.toString());
        listParams.add(groupID.toString());
        EstateOwnerMessage("estateaccessdelta", listParams);
    }
    // #endregion


    // #region Packet Handlers
    private void HandleEstateCovenantReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException
    {
        EstateCovenantReplyPacket reply = (EstateCovenantReplyPacket)packet;
        OnEstateCovenantReply.dispatch(new EstateCovenantReplyCallbackArgs(
        reply.Data.CovenantID,
        reply.Data.CovenantTimestamp,
        Helpers.BytesToString(reply.Data.getEstateName()),
        reply.Data.EstateOwnerID));
    }

    private void HandleEstateOwnerMessage(Packet packet, Simulator simulator) throws UnsupportedEncodingException
    {
        EstateOwnerMessagePacket message = (EstateOwnerMessagePacket)packet;
        int estateID;
        String method = Helpers.BytesToString(message.MethodData.getMethod());
        //List<string> parameters = new List<string>();

        if (method == "estateupdateinfo")
        {
            String estateName = Helpers.BytesToString(message.ParamList[0].getParameter());
            UUID estateOwner = new UUID(Helpers.BytesToString(message.ParamList[1].getParameter()));
            estateID = Helpers.BytesToInt32L(message.ParamList[2].getParameter());
            /*
            for (EstateOwnerMessagePacket.ParamListBlock param : message.ParamList)
            {
                parameters.add(Helpers.BytesToString(param.getParameter()));
            }
            */
            boolean denyNoPaymentInfo;
            if (Helpers.BytesToInt32L(message.ParamList[8].getParameter()) == 0)
            	denyNoPaymentInfo = true;
            else
            	denyNoPaymentInfo = false;

            OnEstateUpdateInfoReply.dispatch(new EstateUpdateInfoReplyCallbackArgs(estateName, estateOwner, estateID, denyNoPaymentInfo));
        }

        else if (method == "setaccess")
        {
            estateID = Helpers.BytesToInt32L(message.ParamList[0].getParameter());
            if (message.ParamList.length > 1)
            {
                //param comes in as a string for some reason
                int param;
                try
    			{
    				param = Integer.parseInt(Helpers.BytesToString(message.ParamList[1].getParameter()));
    			}
    			catch (Throwable t)
    			{
    				return;
    			}
                EstateAccessReplyDelta accessType = EstateAccessReplyDelta.setValue(param);

                switch (accessType)
                {
                    case EstateManagers:
                        //if (OnGetEstateManagers != null)
                        {
                            if (message.ParamList.length > 5)
                            {
                                try
                    			{
                    				param = Integer.parseInt(Helpers.BytesToString(message.ParamList[5].getParameter()));
                    			}
                    			catch (Throwable t)
                    			{
                    				return;
                    			}
                                ArrayList<UUID> managers = new ArrayList<UUID>();
                                for (int i = 6; i < message.ParamList.length; i++)
                                {
                                    try
                                    {
                                        UUID managerID = new UUID(message.ParamList[i].getParameter(), 0);
                                        managers.add(managerID);
                                    }
                                    catch (Exception ex)
                                    {
                                    	logger.error(GridClient.Log(ex.getMessage(), _Client), ex);
                                    }
                                }
                                OnEstateManagersReply.dispatch(new EstateManagersReplyCallbackArgs(estateID, param, managers));
                            }
                        }
                        break;
                    case EstateBans:
                        //if (OnGetEstateBans != null)
                        {
                            if (message.ParamList.length > 5)
                            {
                                try
                    			{
                    				param = Integer.parseInt(Helpers.BytesToString(message.ParamList[4].getParameter()));
                    			}
                    			catch (Throwable t)
                    			{
                    				return;
                    			}
                                ArrayList<UUID> bannedUsers = new ArrayList<UUID>();
                                for (int i = 6; i < message.ParamList.length; i++)
                                {
                                    try
                                    {
                                        UUID bannedID = new UUID(message.ParamList[i].getParameter(), 0);
                                        bannedUsers.add(bannedID);
                                    }
                                    catch (Exception ex)
                                    {
                                    	logger.error(GridClient.Log(ex.getMessage(),  _Client), ex);
                                    }
                                }
                                OnEstateBansReply.dispatch(new EstateBansReplyCallbackArgs(estateID, param, bannedUsers));
                            }
                        }
                        break;
                    case AllowedUsers:
                        //if (OnGetAllowedUsers != null)
                        {
                            if (message.ParamList.length > 5)
                            {
                                try
                    			{
                    				param = Integer.parseInt(Helpers.BytesToString(message.ParamList[2].getParameter()));
                    			}
                    			catch (Throwable t)
                    			{
                    				return;
                    			}
                                ArrayList<UUID> allowedUsers = new ArrayList<UUID>();
                                for (int i = 6; i < message.ParamList.length; i++)
                                {
                                    try
                                    {
                                        UUID allowedID = new UUID(message.ParamList[i].getParameter(), 0);
                                        allowedUsers.add(allowedID);
                                    }
                                    catch (Exception ex)
                                    {
                                    	logger.error(GridClient.Log(ex.getMessage(), _Client), ex);
                                    }
                                }
                                OnEstateUsersReply.dispatch(new EstateUsersReplyCallbackArgs(estateID, param, allowedUsers));
                            }
                        }
                        break;
                    case AllowedGroups:
                        //if (OnGetAllowedGroups != null)
                        {
                            if (message.ParamList.length > 5)
                            {
                                try
                    			{
                    				param = Integer.parseInt(Helpers.BytesToString(message.ParamList[3].getParameter()));
                    			}
                    			catch (Throwable t)
                    			{
                    				return;
                    			}
                                ArrayList<UUID> allowedGroups = new ArrayList<UUID>();
                                for (int i = 6; i < message.ParamList.length; i++)
                                {
                                    try
                                    {
                                        UUID groupID = new UUID(message.ParamList[i].getParameter(), 0);
                                        allowedGroups.add(groupID);
                                    }
                                    catch (Exception ex)
                                    {
                                    	logger.error(GridClient.Log(ex.getMessage(), _Client), ex);
                                    }
                                }
                                OnEstateGroupsReply.dispatch(new EstateGroupsReplyCallbackArgs(estateID, param, allowedGroups));
                            }
                        }
                        break;
					default:
						break;
                }
            }
        }
    }

    private void HandleLandStatReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException
    {
        //if (OnLandStatReply != null || OnGetTopScripts != null || OnGetTopColliders != null)
        //if (OnGetTopScripts != null || OnGetTopColliders != null)
        {
            LandStatReplyPacket p = (LandStatReplyPacket)packet;
            HashMap<UUID, EstateTask> Tasks = new HashMap<UUID, EstateTask>(p.ReportData.length);

            for (LandStatReplyPacket.ReportDataBlock rep : p.ReportData)
            {
                EstateTask task = new EstateTask();
                task.Position = new Vector3(rep.LocationX, rep.LocationY, rep.LocationZ);
                task.Score = rep.Score;
                task.TaskID = rep.TaskID;
                task.TaskLocalID = rep.TaskLocalID;
                task.TaskName = Helpers.BytesToString(rep.getTaskName());
                task.OwnerName = Helpers.BytesToString(rep.getOwnerName());
                Tasks.put(task.TaskID, task);
            }

            LandStatReportType type = LandStatReportType.setValue(p.RequestData.ReportType);

            if (type == LandStatReportType.TopScripts)
            {
                OnTopScriptsReply.dispatch(new TopScriptsReplyCallbackArgs(p.RequestData.TotalObjectCount, Tasks)); 
            }
            else if (type == LandStatReportType.TopColliders)
            {
                OnTopCollidersReply.dispatch(new TopCollidersReplyCallbackArgs(p.RequestData.TotalObjectCount, Tasks)); 
            }

            /*
            if (OnGetTopColliders != null)
            {
                //FIXME - System.UnhandledExceptionEventArgs
                OnLandStatReply(
                    type,
                    p.RequestData.RequestFlags,
                    (int)p.RequestData.TotalObjectCount,
                    Tasks
                );
            }
            */
        }
    }

    private void HandleLandStatReply(IMessage message, Simulator simulator)
    {
        LandStatReplyMessage m = (LandStatReplyMessage)message;
        HashMap<UUID, EstateTask> Tasks = new HashMap<UUID, EstateTask>(m.ReportDataBlocks.length);

        for (LandStatReplyMessage.ReportDataBlock rep : m.ReportDataBlocks)
        {
            EstateTask task = new EstateTask();
            task.Position = rep.Location;
            task.Score = rep.Score;
            task.MonoScore = rep.MonoScore;
            task.TaskID = rep.TaskID;
            task.TaskLocalID = rep.TaskLocalID;
            task.TaskName = rep.TaskName;
            task.OwnerName = rep.OwnerName;
            Tasks.put(task.TaskID, task);
        }

        LandStatReportType type = LandStatReportType.setValue(m.ReportType);

        if (type == LandStatReportType.TopScripts)
        {
            OnTopScriptsReply.dispatch(new TopScriptsReplyCallbackArgs(m.TotalObjectCount, Tasks)); 
        }
        else if (type == LandStatReportType.TopColliders)
        {
            OnTopCollidersReply.dispatch(new TopCollidersReplyCallbackArgs(m.TotalObjectCount, Tasks)); 
        }
    }
    // #endregion
}
