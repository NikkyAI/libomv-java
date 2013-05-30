/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
 * Copyright (c) 2009-2012, Frederick Martian
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
package libomv;

import java.util.ArrayList;

import libomv.packets.EjectUserPacket;
import libomv.packets.EstateOwnerMessagePacket;
import libomv.packets.FreezeUserPacket;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class EstateTools
{
	private GridClient Client;

    // #region Enums
    // Used in the ReportType field of a LandStatRequest
    public enum LandStatReportType
    {
        TopScripts,
        TopColliders
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

        public static EstateAccessDelta setValue(byte value)
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

        public static EstateAccessReplyDelta setValue(byte value)
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

        public static EstateReturnFlags setValue(byte value)
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

    // @param client
	public EstateTools(GridClient client)
	{
		Client = client;
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
        estate.AgentData.AgentID = Client.Self.getAgentID();
        estate.AgentData.SessionID = Client.Self.getSessionID();
        estate.AgentData.TransactionID = UUID.Zero;
        estate.MethodData.Invoice = new UUID();
        estate.MethodData.setMethod(Helpers.StringToBytes(method));
        estate.ParamList = new EstateOwnerMessagePacket.ParamListBlock[listParams.size()];
        for (int i = 0; i < listParams.size(); i++)
        {
            estate.ParamList[i] = estate.new ParamListBlock();
            estate.ParamList[i].setParameter(Helpers.StringToBytes(listParams.get(i)));
        }
        Client.Network.sendPacket(estate);
    }

    /**
     * Kick an avatar from an estate
     *
     * @param userID Key of Agent to remove
     */
	public void KickUser(UUID userID) throws Exception
	{
        EstateOwnerMessage("kickestate", userID.toString());
	}

    public void EjectUser(UUID targetID, boolean ban) throws Exception
    {
        EjectUserPacket eject = new EjectUserPacket();
        eject.AgentData.AgentID = Client.Self.getAgentID();
        eject.AgentData.SessionID = Client.Self.getSessionID();
        eject.Data.TargetID = targetID;
        if (ban)
        	eject.Data.Flags = 1;
        else
        	eject.Data.Flags = 0;

        Client.Network.sendPacket(eject);
    }
    
    /**
     * Freeze or unfreeze an avatar over your land
     *
     * @param targetID target key to freeze
     * @param freeze true to freeze, false to unfreeze
     * @throws Exception 
     * */
    public void FreezeUser(UUID targetID, boolean freeze) throws Exception
    {
        FreezeUserPacket frz = new FreezeUserPacket();
        frz.AgentData.AgentID = Client.Self.getAgentID();
        frz.AgentData.SessionID = Client.Self.getSessionID();
        frz.Data.TargetID = targetID;
        if (freeze)
        	frz.Data.Flags = 0;
        else
        	frz.Data.Flags = 1;

        Client.Network.sendPacket(frz);
    }

    /** 
     * Ban an avatar from an estate
     *
     * @param userID Key of Agent to remove
     * @param allEstates allEstates Ban user from this estate and all others owned by the estate owner
     * @throws Exception 
     */
	public void BanUser(UUID userID, boolean allEstates) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>();
        Integer flag = allEstates ? EstateAccessDelta.BanUserAllEstates.getValue() : EstateAccessDelta.BanUser.getValue();
        listParams.add(Client.Self.getAgentID().toString());
        listParams.add(flag.toString());
        listParams.add(userID.toString());
        EstateOwnerMessage("estateaccessdelta", listParams);
    }

    /** 
     * Unban an avatar from an estate
     * 
     * @param userID Key of Agent to remove
     * @param allEstates allEstates Unban user from this estate and all others owned by the estate owner
     * @throws Exception 
     */
    public void UnbanUser(UUID userID, boolean allEstates) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>();
        Integer flag = allEstates ? EstateAccessDelta.UnbanUserAllEstates.getValue() : EstateAccessDelta.UnbanUser.getValue();
        listParams.add(Client.Self.getAgentID().toString());
        listParams.add(flag.toString());
        listParams.add(userID.toString());
        EstateOwnerMessage("estateaccessdelta", listParams);
    }

    /**
	 * Send a message dialog to everyone in an entire estate
     *
     * @param message Message to send all users in the estate
     * @throws Exception 
     */
    public void EstateMessage(String message) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>();
        listParams.add(Client.Self.getName());
        listParams.add(message);
        EstateOwnerMessage("instantmessage", listParams);
    }

    /**
	 * Send a message dialog to everyone in a simulator
     *
     * @param message Message to send all users in the simulator
     * @throws Exception 
     */
    public void SimulatorMessage(String message) throws Exception
    {
        ArrayList<String> listParams = new ArrayList<String>();
        listParams.add("-1");
        listParams.add("-1");
        listParams.add(Client.Self.getAgentID().toString());
        listParams.add(Client.Self.getName());
        listParams.add(message);
        EstateOwnerMessage("simulatormessage", listParams);
    }

    /**
	 * Send an avatar back to their home location
	 *
	 * @param userID Key of avatar to send home
     * @throws Exception 
	 */
	public void TeleportHomeUser(UUID userID) throws Exception
	{
        ArrayList<String> listParams = new ArrayList<String>();
        listParams.add(Client.Self.getAgentID().toString());
        listParams.add(userID.toString());
        EstateOwnerMessage("teleporthomeuser", listParams);
	}
	
    /**
	 * Send all avatars back to their home location
	 *
	 * @param userID Key of avatar to send home
     * @throws Exception 
	 */
	public void TeleportHomeAllUsers(UUID userID) throws Exception
	{
        ArrayList<String> listParams = new ArrayList<String>();
        listParams.add(Client.Self.getAgentID().toString());
        EstateOwnerMessage("teleporthomeallusers", listParams);
	}

	/**
	 * Begin the region restart process
     * @throws Exception 
    */
     public void RestartRegion() throws Exception
    {
        EstateOwnerMessage("restart", "120");
    }

    /**
	 * Cancels a region restart
     * @throws Exception 
     */
    public void CancelRestart() throws Exception
    {
        EstateOwnerMessage("restart", "-1");
    }
}
