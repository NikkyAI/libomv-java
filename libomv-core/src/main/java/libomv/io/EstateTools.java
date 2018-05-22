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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.LandStatReplyMessage;
import libomv.capabilities.IMessage;
import libomv.io.capabilities.CapsCallback;
import libomv.model.Simulator;
import libomv.model.asset.AssetType;
import libomv.model.estate.EstateAccessDelta;
import libomv.model.estate.EstateAccessReplyDelta;
import libomv.model.estate.EstateBansReplyCallbackArgs;
import libomv.model.estate.EstateCovenantReplyCallbackArgs;
import libomv.model.estate.EstateGroupsReplyCallbackArgs;
import libomv.model.estate.EstateManagersReplyCallbackArgs;
import libomv.model.estate.EstateReturnFlags;
import libomv.model.estate.EstateTask;
import libomv.model.estate.EstateUpdateInfoReplyCallbackArgs;
import libomv.model.estate.EstateUsersReplyCallbackArgs;
import libomv.model.estate.GroundTextureHeightSettings;
import libomv.model.estate.GroundTextureSettings;
import libomv.model.estate.LandStatReportType;
import libomv.model.estate.TopCollidersReplyCallbackArgs;
import libomv.model.estate.TopScriptsReplyCallbackArgs;
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
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;

public class EstateTools implements PacketCallback, CapsCallback {
	private static final Logger logger = Logger.getLogger(EstateTools.class);

	private GridClient client;

	/// Textures for each of the four terrain height levels
	public GroundTextureSettings groundTextures;

	/// Upper/lower texture boundaries for each corner of the sim
	public GroundTextureHeightSettings groundTextureLimits;

	public CallbackHandler<TopCollidersReplyCallbackArgs> onTopCollidersReply = new CallbackHandler<>();

	public CallbackHandler<TopScriptsReplyCallbackArgs> onTopScriptsReply = new CallbackHandler<>();

	public CallbackHandler<EstateBansReplyCallbackArgs> onEstateBansReply = new CallbackHandler<>();

	public CallbackHandler<EstateUsersReplyCallbackArgs> onEstateUsersReply = new CallbackHandler<>();

	public CallbackHandler<EstateGroupsReplyCallbackArgs> onEstateGroupsReply = new CallbackHandler<>();

	public CallbackHandler<EstateManagersReplyCallbackArgs> onEstateManagersReply = new CallbackHandler<>();

	public CallbackHandler<EstateCovenantReplyCallbackArgs> onEstateCovenantReply = new CallbackHandler<>();

	public CallbackHandler<EstateUpdateInfoReplyCallbackArgs> onEstateUpdateInfoReply = new CallbackHandler<>();

	// @param client
	public EstateTools(GridClient client) {
		this.client = client;

		this.groundTextures = new GroundTextureSettings();
		this.groundTextureLimits = new GroundTextureHeightSettings();

		this.client.network.registerCallback(PacketType.LandStatReply, this);
		this.client.network.registerCallback(PacketType.EstateOwnerMessage, this);
		this.client.network.registerCallback(PacketType.EstateCovenantReply, this);

		this.client.network.registerCallback(CapsEventType.LandStatReply, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case LandStatReply:
			handleLandStatReply(packet, simulator);
			break;
		case EstateOwnerMessage:
			handleEstateOwnerMessage(packet, simulator);
			break;
		case EstateCovenantReply:
			handleEstateCovenantReply(packet, simulator);
			break;
		default:
			break;
		}
	}

	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) throws Exception {
		switch (message.getType()) {
		case LandStatReply:
			handleLandStatReply(message, simulator);
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
	public void landStatRequest(int parcelLocalID, LandStatReportType reportType, int requestFlags, String filter)
			throws Exception {
		LandStatRequestPacket p = new LandStatRequestPacket();
		p.AgentData.AgentID = client.agent.getAgentID();
		p.AgentData.SessionID = client.agent.getSessionID();
		p.RequestData.setFilter(Helpers.stringToBytes(filter));
		p.RequestData.ParcelLocalID = parcelLocalID;
		p.RequestData.ReportType = reportType.ordinal();
		p.RequestData.RequestFlags = requestFlags;
		client.network.sendPacket(p);
	}

	/// <summary>Requests estate settings, including estate manager and access/ban
	/// lists</summary>
	public void requestInfo() throws Exception {
		estateOwnerMessage("getinfo", "");
	}

	/// <summary>Requests the "Top Scripts" list for the current region</summary>
	public void requestTopScripts() throws Exception {
		// EstateOwnerMessage("scripts", "");
		landStatRequest(0, LandStatReportType.TopScripts, 0, "");
	}

	/// <summary>Requests the "Top Colliders" list for the current region</summary>
	public void requestTopColliders() throws Exception {
		// EstateOwnerMessage("colliders", "");
		landStatRequest(0, LandStatReportType.TopColliders, 0, "");
	}

	/**
	 * Set several estate specific configuration variables
	 *
	 * @param waterHeight
	 *            The Height of the water level over the entire estate. Defaults to
	 *            20
	 * @param terrainRaiseLimit
	 *            The maximum height change allowed above the baked terrain.
	 *            Defaults to 4
	 * @param terrainLowerLimit
	 *            The minimum height change allowed below the baked terrain.
	 *            Defaults to -4
	 * @param useEstateSun
	 *            True to use
	 * @param fixedSun
	 *            if True forces the sun position to the position in SunPosition
	 * @param sunPosition
	 *            The current position of the sun on the estate, or when FixedSun is
	 *            true the static position the sun will remain.
	 * @remarks >6.0 = Sunrise, 30.0 = Sunset
	 */
	public void setTerrainVariables(float waterHeight, float terrainRaiseLimit, float terrainLowerLimit,
			boolean useEstateSun, boolean fixedSun, float sunPosition) throws Exception {
		List<String> simVariables = new ArrayList<>();
		simVariables.add(String.format(Helpers.EnUsCulture, "%f", waterHeight));
		simVariables.add(String.format(Helpers.EnUsCulture, "%f", terrainRaiseLimit));
		simVariables.add(String.format(Helpers.EnUsCulture, "%f", terrainLowerLimit));
		simVariables.add(useEstateSun ? "Y" : "N");
		simVariables.add(fixedSun ? "Y" : "N");
		simVariables.add(String.format(Helpers.EnUsCulture, "%f", sunPosition));
		simVariables.add("Y"); // Not used?
		simVariables.add("N"); // Not used?
		simVariables.add("0.00"); // Also not used?
		estateOwnerMessage("setregionterrain", simVariables);
	}

	/**
	 * Request return of objects owned by specified avatar
	 *
	 * @param target
	 *            The Agents <see cref="UUID"/> owning the primitives to return
	 * @param flag
	 *            specify the coverage and type of objects to be included in the
	 *            return
	 * @param estateWide
	 *            true to perform return on entire estate
	 */
	public void simWideReturn(UUID target, EstateReturnFlags flag, boolean estateWide) throws Exception {
		if (estateWide) {
			List<String> param = new ArrayList<>();
			param.add(flag.toString());
			param.add(target.toString());
			estateOwnerMessage("estateobjectreturn", param);
		} else {
			SimWideDeletesPacket simDelete = new SimWideDeletesPacket();
			simDelete.AgentData.AgentID = client.agent.getAgentID();
			simDelete.AgentData.SessionID = client.agent.getSessionID();
			simDelete.DataBlock.TargetID = target;
			simDelete.DataBlock.Flags = flag.getValue();
			client.network.sendPacket(simDelete);
		}
	}

	/**
	 * Used for setting and retrieving various estate panel settings
	 *
	 * @param method
	 *            EstateOwnerMessage Method field
	 * @param param
	 *            Single parameter to include
	 * @throws Exception
	 */
	public void estateOwnerMessage(String method, String param) throws Exception {
		List<String> listParams = new ArrayList<>();
		listParams.add(param);
		estateOwnerMessage(method, listParams);
	}

	/**
	 * Used for setting and retrieving various estate panel settings
	 *
	 * @param method
	 *            EstateOwnerMessage Method field
	 * @param listParams
	 *            List of parameters to include
	 * @throws Exception
	 */
	public void estateOwnerMessage(String method, List<String> listParams) throws Exception {
		EstateOwnerMessagePacket estate = new EstateOwnerMessagePacket();
		estate.AgentData.AgentID = client.agent.getAgentID();
		estate.AgentData.SessionID = client.agent.getSessionID();
		estate.AgentData.TransactionID = UUID.ZERO;
		estate.MethodData.Invoice = new UUID();
		estate.MethodData.setMethod(Helpers.stringToBytes(method));
		estate.ParamList = new EstateOwnerMessagePacket.ParamListBlock[listParams.size()];
		for (int i = 0; i < listParams.size(); i++) {
			estate.ParamList[i] = estate.new ParamListBlock();
			estate.ParamList[i].setParameter(Helpers.stringToBytes(listParams.get(i)));
		}
		client.network.sendPacket(estate);
	}

	/**
	 * Kick an avatar from an estate
	 *
	 * @param userID
	 *            Key of Agent to remove
	 *
	 * @throws Exception
	 */
	public void kickUser(UUID userID) throws Exception {
		estateOwnerMessage("kickestate", userID.toString());
	}

	/**
	 * Eject an avatar from an estate
	 *
	 * @param userID
	 *            Key of Agent to remove
	 * @param ban
	 *            also ban user from estate if true
	 *
	 * @throws Exception
	 */
	public void ejectUser(UUID targetID, boolean ban) throws Exception {
		EjectUserPacket eject = new EjectUserPacket();
		eject.AgentData.AgentID = client.agent.getAgentID();
		eject.AgentData.SessionID = client.agent.getSessionID();
		eject.Data.TargetID = targetID;
		if (ban)
			eject.Data.Flags = 1;
		else
			eject.Data.Flags = 0;

		client.network.sendPacket(eject);
	}

	/**
	 * Freeze or unfreeze an avatar over your land
	 *
	 * @param targetID
	 *            target key to freeze
	 * @param freeze
	 *            true to freeze, false to unfreeze
	 *
	 * @throws Exception
	 */
	public void freezeUser(UUID targetID, boolean freeze) throws Exception {
		FreezeUserPacket frz = new FreezeUserPacket();
		frz.AgentData.AgentID = client.agent.getAgentID();
		frz.AgentData.SessionID = client.agent.getSessionID();
		frz.Data.TargetID = targetID;
		if (freeze)
			frz.Data.Flags = 0;
		else
			frz.Data.Flags = 1;

		client.network.sendPacket(frz);
	}

	/**
	 * Ban an avatar from an estate
	 *
	 * @param userID
	 *            Key of Agent to remove
	 * @param allEstates
	 *            allEstates Ban user from this estate and all others owned by the
	 *            estate owner
	 *
	 * @throws Exception
	 */
	public void banUser(UUID userID, boolean allEstates) throws Exception {
		List<String> listParams = new ArrayList<>();
		Integer flag = allEstates ? EstateAccessDelta.BanUserAllEstates.getValue()
				: EstateAccessDelta.BanUser.getValue();
		listParams.add(client.agent.getAgentID().toString());
		listParams.add(flag.toString());
		listParams.add(userID.toString());
		estateOwnerMessage("estateaccessdelta", listParams);
	}

	/**
	 * Unban an avatar from an estate
	 *
	 * @param userID
	 *            Key of Agent to remove
	 * @param allEstates
	 *            allEstates Unban user from this estate and all others owned by the
	 *            estate owner
	 *
	 * @throws Exception
	 */
	public void unbanUser(UUID userID, boolean allEstates) throws Exception {
		List<String> listParams = new ArrayList<>();
		Integer flag = allEstates ? EstateAccessDelta.UnbanUserAllEstates.getValue()
				: EstateAccessDelta.UnbanUser.getValue();
		listParams.add(client.agent.getAgentID().toString());
		listParams.add(flag.toString());
		listParams.add(userID.toString());
		estateOwnerMessage("estateaccessdelta", listParams);
	}

	/**
	 * Send a message dialog to everyone in an entire estate
	 *
	 * @param message
	 *            Message to send all users in the estate
	 *
	 * @throws Exception
	 */
	public void estateMessage(String message) throws Exception {
		List<String> listParams = new ArrayList<>(2);
		listParams.add(client.agent.getName());
		listParams.add(message);
		estateOwnerMessage("instantmessage", listParams);
	}

	/**
	 * Send a message dialog to everyone in a simulator
	 *
	 * @param message
	 *            Message to send all users in the simulator
	 *
	 * @throws Exception
	 */
	public void simulatorMessage(String message) throws Exception {
		List<String> listParams = new ArrayList<>(5);
		listParams.add("-1");
		listParams.add("-1");
		listParams.add(client.agent.getAgentID().toString());
		listParams.add(client.agent.getName());
		listParams.add(message);
		estateOwnerMessage("simulatormessage", listParams);
	}

	/**
	 * Send an avatar back to their home location
	 *
	 * @param userID
	 *            Key of avatar to send home
	 *
	 * @throws Exception
	 */
	public void teleportHomeUser(UUID userID) throws Exception {
		List<String> listParams = new ArrayList<>(2);
		listParams.add(client.agent.getAgentID().toString());
		listParams.add(userID.toString());
		estateOwnerMessage("teleporthomeuser", listParams);
	}

	/**
	 * Send all avatars back to their home location
	 *
	 * @param userID
	 *            Key of avatar to send home
	 *
	 * @throws Exception
	 */
	public void teleportHomeAllUsers(UUID userID) throws Exception {
		List<String> listParams = new ArrayList<>();
		listParams.add(client.agent.getAgentID().toString());
		estateOwnerMessage("teleporthomeallusers", listParams);
	}

	/**
	 * Begin the region restart process
	 *
	 * @throws Exception
	 */
	public void restartRegion() throws Exception {
		estateOwnerMessage("restart", "120");
	}

	/**
	 * Cancels a region restart
	 *
	 * @throws Exception
	 */
	public void cancelRestart() throws Exception {
		estateOwnerMessage("restart", "-1");
	}

	/**
	 * Estate panel "Region" tab settings
	 *
	 * @throws Exception
	 */
	public void setRegionInfo(boolean blockTerraform, boolean blockFly, boolean allowDamage, boolean allowLandResell,
			boolean restrictPushing, boolean allowParcelJoinDivide, float agentLimit, float objectBonus, boolean mature)
			throws Exception {
		List<String> listParams = new ArrayList<>(9);
		if (blockTerraform)
			listParams.add("Y");
		else
			listParams.add("N");
		if (blockFly)
			listParams.add("Y");
		else
			listParams.add("N");
		if (allowDamage)
			listParams.add("Y");
		else
			listParams.add("N");
		if (allowLandResell)
			listParams.add("Y");
		else
			listParams.add("N");
		listParams.add(String.format(Helpers.EnUsCulture, "%f", agentLimit));
		listParams.add(String.format(Helpers.EnUsCulture, "%f", objectBonus));
		if (mature)
			listParams.add("21");
		else
			listParams.add("13"); // FIXME - enumerate these settings
		if (restrictPushing)
			listParams.add("Y");
		else
			listParams.add("N");
		if (allowParcelJoinDivide)
			listParams.add("Y");
		else
			listParams.add("N");
		estateOwnerMessage("setregioninfo", listParams);
	}

	/**
	 * Estate panel "Debug" tab settings
	 *
	 * @throws Exception
	 */
	public void setRegionDebug(boolean disableScripts, boolean disableCollisions, boolean disablePhysics)
			throws Exception {
		List<String> listParams = new ArrayList<>(3);
		if (disableScripts)
			listParams.add("Y");
		else
			listParams.add("N");
		if (disableCollisions)
			listParams.add("Y");
		else
			listParams.add("N");
		if (disablePhysics)
			listParams.add("Y");
		else
			listParams.add("N");
		estateOwnerMessage("setregiondebug", listParams);
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
	public void setRegionTerrain(UUID low, UUID midLow, UUID midHigh, UUID high) throws Exception {
		List<String> listParams = new ArrayList<>(4);
		listParams.add("0 " + low.toString());
		listParams.add("1 " + midLow.toString());
		listParams.add("2 " + midHigh.toString());
		listParams.add("3 " + high.toString());
		estateOwnerMessage("texturedetail", listParams);
		estateOwnerMessage("texturecommit", "");
	}

	/**
	 * Used for setting sim terrain texture heights
	 *
	 * @throws Exception
	 */
	public void setRegionTerrainHeights(float lowSW, float highSW, float lowNW, float highNW, float lowSE, float highSE,
			float lowNE, float highNE) throws Exception {
		List<String> listParams = new ArrayList<>(4);
		listParams.add(String.format(Helpers.EnUsCulture, "0 %f %f", lowSW, highSW)); // SW low-high
		listParams.add(String.format(Helpers.EnUsCulture, "1 %f %f", lowNW, highNW)); // NW low-high
		listParams.add(String.format(Helpers.EnUsCulture, "2 %f %f", lowSE, highSE)); // SE low-high
		listParams.add(String.format(Helpers.EnUsCulture, "3 %f %f", lowNE, highNE)); // NE low-high
		estateOwnerMessage("textureheights", listParams);
		estateOwnerMessage("texturecommit", "");
	}

	/**
	 * Requests the estate covenant
	 *
	 * @throws Exception
	 */
	public void requestCovenant() throws Exception {
		EstateCovenantRequestPacket req = new EstateCovenantRequestPacket();
		req.AgentData.AgentID = client.agent.getAgentID();
		req.AgentData.SessionID = client.agent.getSessionID();
		client.network.sendPacket(req);
	}

	/**
	 * Upload a terrain RAW file
	 *
	 * @param fileData
	 *            A byte array containing the encoded terrain data
	 * @param fileName
	 *            The name of the file being uploaded
	 * @throws Exception
	 * @returns The Id of the transfer request
	 */
	public UUID uploadTerrain(byte[] fileData, String fileName) throws Exception {
		// Tell the library we have a pending file to upload
		UUID transactionID = client.assets.requestUpload(AssetType.Unknown, fileData, false);

		// Create and populate a list with commands specific to uploading a raw terrain
		// file
		List<String> paramList = new ArrayList<>();
		paramList.add("upload filename");
		paramList.add(fileName);

		// Tell the simulator we have a new raw file to upload
		estateOwnerMessage("terrain", paramList);

		return transactionID;
	}

	/**
	 * Teleports all users home in current Estate
	 *
	 * @throws Exception
	 */
	public void teleportHomeAllUsers() throws Exception {
		List<String> params = new ArrayList<>(1);
		params.add(client.agent.getAgentID().toString());
		estateOwnerMessage("teleporthomeallusers", params);
	}

	/**
	 * Remove estate manager
	 *
	 * @param userID
	 *            Key of Agent to Remove
	 * @param allEstates
	 *            removes manager to this estate and all others owned by the estate
	 *            owner
	 *
	 * @throws Exception
	 */
	public void removeEstateManager(UUID userID, boolean allEstates) throws Exception {
		List<String> listParams = new ArrayList<>(3);
		Integer flag = allEstates ? EstateAccessDelta.RemoveManagerAllEstates.getValue()
				: EstateAccessDelta.RemoveManager.getValue();
		listParams.add(client.agent.getAgentID().toString());
		listParams.add(flag.toString());
		listParams.add(userID.toString());
		estateOwnerMessage("estateaccessdelta", listParams);
	}

	/**
	 * Add estate manager
	 *
	 * @param userID
	 *            Key of agent to add
	 * @param allEstates
	 *            Add agent as manager to this estate and all others owned by the
	 *            estate owner
	 *
	 * @throws Exception
	 */
	public void addEstateManager(UUID userID, boolean allEstates) throws Exception {
		List<String> listParams = new ArrayList<>(3);
		Integer flag = allEstates ? EstateAccessDelta.AddManagerAllEstates.getValue()
				: EstateAccessDelta.AddManager.getValue();
		listParams.add(client.agent.getAgentID().toString());
		listParams.add(flag.toString());
		listParams.add(userID.toString());
		estateOwnerMessage("estateaccessdelta", listParams);
	}

	/**
	 * Add's an agent to the estate allowed list
	 *
	 * @param userID
	 *            Key of Agent to add
	 * @param allEstates
	 *            Add agent as an allowed resident to all estates if true\
	 *
	 * @throws Exception
	 */
	public void addAllowedUser(UUID userID, boolean allEstates) throws Exception {
		List<String> listParams = new ArrayList<>(3);
		Integer flag = allEstates ? EstateAccessDelta.AddAllowedAllEstates.getValue()
				: EstateAccessDelta.AddUserAsAllowed.getValue();
		listParams.add(client.agent.getAgentID().toString());
		listParams.add(flag.toString());
		listParams.add(userID.toString());
		estateOwnerMessage("estateaccessdelta", listParams);
	}

	/**
	 * Removes an agent from the estate Allowed list
	 *
	 * @param userID
	 *            Key of Agent to Remove
	 * @param allEstates
	 *            Removes agent as an allowed resident from all estates if true
	 * @throws Exception
	 */
	public void removeAllowedUser(UUID userID, boolean allEstates) throws Exception {
		List<String> listParams = new ArrayList<>(3);
		Integer flag = allEstates ? EstateAccessDelta.RemoveUserAllowedAllEstates.getValue()
				: EstateAccessDelta.RemoveUserAsAllowed.getValue();
		listParams.add(client.agent.getAgentID().toString());
		listParams.add(flag.toString());
		listParams.add(userID.toString());
		estateOwnerMessage("estateaccessdelta", listParams);
	}

	/**
	 * Add's a group to the estate Allowed list
	 *
	 * @param groupID
	 *            Key of group to add
	 * @param allEstates
	 *            Add group as an allowed group to All estates if true
	 *
	 * @throws Exception
	 */
	public void addAllowedGroup(UUID groupID, boolean allEstates) throws Exception {
		List<String> listParams = new ArrayList<>(3);
		Integer flag = allEstates ? EstateAccessDelta.AddGroupAllowedAllEstates.getValue()
				: EstateAccessDelta.AddGroupAsAllowed.getValue();

		listParams.add(client.agent.getAgentID().toString());
		listParams.add(flag.toString());
		listParams.add(groupID.toString());
		estateOwnerMessage("estateaccessdelta", listParams);
	}

	/**
	 * Removes a group from the estate Allowed list
	 *
	 * @param groupID
	 *            Key of group to remove</param>
	 * @param allEstates
	 *            Removes group as an allowed group from all estates if true
	 *
	 * @throws Exception
	 */
	public void removeAllowedGroup(UUID groupID, boolean allEstates) throws Exception {
		List<String> listParams = new ArrayList<>(3);
		Integer flag = allEstates ? EstateAccessDelta.RemoveGroupAllowedAllEstates.getValue()
				: EstateAccessDelta.RemoveGroupAsAllowed.getValue();
		listParams.add(client.agent.getAgentID().toString());
		listParams.add(flag.toString());
		listParams.add(groupID.toString());
		estateOwnerMessage("estateaccessdelta", listParams);
	}

	private void handleEstateCovenantReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		EstateCovenantReplyPacket reply = (EstateCovenantReplyPacket) packet;
		onEstateCovenantReply
				.dispatch(new EstateCovenantReplyCallbackArgs(reply.Data.CovenantID, reply.Data.CovenantTimestamp,
						Helpers.bytesToString(reply.Data.getEstateName()), reply.Data.EstateOwnerID));
	}

	private void handleEstateOwnerMessage(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		EstateOwnerMessagePacket message = (EstateOwnerMessagePacket) packet;
		int estateID;
		String method = Helpers.bytesToString(message.MethodData.getMethod());
		// List<string> parameters = new List<>();

		if (method == "estateupdateinfo") {
			String estateName = Helpers.bytesToString(message.ParamList[0].getParameter());
			UUID estateOwner = new UUID(Helpers.bytesToString(message.ParamList[1].getParameter()));
			estateID = Helpers.bytesToInt32L(message.ParamList[2].getParameter());
			/*
			 * for (EstateOwnerMessagePacket.ParamListBlock param : message.ParamList) {
			 * parameters.add(Helpers.BytesToString(param.getParameter())); }
			 */
			boolean denyNoPaymentInfo;
			if (Helpers.bytesToInt32L(message.ParamList[8].getParameter()) == 0)
				denyNoPaymentInfo = true;
			else
				denyNoPaymentInfo = false;

			onEstateUpdateInfoReply.dispatch(
					new EstateUpdateInfoReplyCallbackArgs(estateName, estateOwner, estateID, denyNoPaymentInfo));
		}

		else if (method == "setaccess") {
			estateID = Helpers.bytesToInt32L(message.ParamList[0].getParameter());
			if (message.ParamList.length > 1) {
				// param comes in as a string for some reason
				int param;
				try {
					param = Integer.parseInt(Helpers.bytesToString(message.ParamList[1].getParameter()));
				} catch (Throwable t) {
					return;
				}
				EstateAccessReplyDelta accessType = EstateAccessReplyDelta.setValue(param);

				switch (accessType) {
				case EstateManagers:
				// if (OnGetEstateManagers != null)
				{
					if (message.ParamList.length > 5) {
						try {
							param = Integer.parseInt(Helpers.bytesToString(message.ParamList[5].getParameter()));
						} catch (Throwable t) {
							return;
						}
						List<UUID> managers = new ArrayList<>();
						for (int i = 6; i < message.ParamList.length; i++) {
							try {
								UUID managerID = new UUID(message.ParamList[i].getParameter(), 0);
								managers.add(managerID);
							} catch (Exception ex) {
								logger.error(GridClient.Log(ex.getMessage(), client), ex);
							}
						}
						onEstateManagersReply.dispatch(new EstateManagersReplyCallbackArgs(estateID, param, managers));
					}
				}
					break;
				case EstateBans:
				// if (OnGetEstateBans != null)
				{
					if (message.ParamList.length > 5) {
						try {
							param = Integer.parseInt(Helpers.bytesToString(message.ParamList[4].getParameter()));
						} catch (Throwable t) {
							return;
						}
						List<UUID> bannedUsers = new ArrayList<>();
						for (int i = 6; i < message.ParamList.length; i++) {
							try {
								UUID bannedID = new UUID(message.ParamList[i].getParameter(), 0);
								bannedUsers.add(bannedID);
							} catch (Exception ex) {
								logger.error(GridClient.Log(ex.getMessage(), client), ex);
							}
						}
						onEstateBansReply.dispatch(new EstateBansReplyCallbackArgs(estateID, param, bannedUsers));
					}
				}
					break;
				case AllowedUsers:
				// if (OnGetAllowedUsers != null)
				{
					if (message.ParamList.length > 5) {
						try {
							param = Integer.parseInt(Helpers.bytesToString(message.ParamList[2].getParameter()));
						} catch (Throwable t) {
							return;
						}
						List<UUID> allowedUsers = new ArrayList<>();
						for (int i = 6; i < message.ParamList.length; i++) {
							try {
								UUID allowedID = new UUID(message.ParamList[i].getParameter(), 0);
								allowedUsers.add(allowedID);
							} catch (Exception ex) {
								logger.error(GridClient.Log(ex.getMessage(), client), ex);
							}
						}
						onEstateUsersReply.dispatch(new EstateUsersReplyCallbackArgs(estateID, param, allowedUsers));
					}
				}
					break;
				case AllowedGroups:
				// if (OnGetAllowedGroups != null)
				{
					if (message.ParamList.length > 5) {
						try {
							param = Integer.parseInt(Helpers.bytesToString(message.ParamList[3].getParameter()));
						} catch (Throwable t) {
							return;
						}
						List<UUID> allowedGroups = new ArrayList<>();
						for (int i = 6; i < message.ParamList.length; i++) {
							try {
								UUID groupID = new UUID(message.ParamList[i].getParameter(), 0);
								allowedGroups.add(groupID);
							} catch (Exception ex) {
								logger.error(GridClient.Log(ex.getMessage(), client), ex);
							}
						}
						onEstateGroupsReply.dispatch(new EstateGroupsReplyCallbackArgs(estateID, param, allowedGroups));
					}
				}
					break;
				default:
					break;
				}
			}
		}
	}

	private void handleLandStatReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException {
		// if (OnLandStatReply != null || OnGetTopScripts != null || OnGetTopColliders
		// != null)
		// if (OnGetTopScripts != null || OnGetTopColliders != null)
		{
			LandStatReplyPacket p = (LandStatReplyPacket) packet;
			Map<UUID, EstateTask> tasks = new HashMap<>(p.ReportData.length);

			for (LandStatReplyPacket.ReportDataBlock rep : p.ReportData) {
				EstateTask task = new EstateTask();
				task.position = new Vector3(rep.LocationX, rep.LocationY, rep.LocationZ);
				task.score = rep.Score;
				task.taskID = rep.TaskID;
				task.taskLocalID = rep.TaskLocalID;
				task.taskName = Helpers.bytesToString(rep.getTaskName());
				task.ownerName = Helpers.bytesToString(rep.getOwnerName());
				tasks.put(task.taskID, task);
			}

			LandStatReportType type = LandStatReportType.setValue(p.RequestData.ReportType);

			if (type == LandStatReportType.TopScripts) {
				onTopScriptsReply.dispatch(new TopScriptsReplyCallbackArgs(p.RequestData.TotalObjectCount, tasks));
			} else if (type == LandStatReportType.TopColliders) {
				onTopCollidersReply.dispatch(new TopCollidersReplyCallbackArgs(p.RequestData.TotalObjectCount, tasks));
			}

			/*
			 * if (OnGetTopColliders != null) { //FIXME - System.UnhandledExceptionEventArgs
			 * OnLandStatReply( type, p.RequestData.RequestFlags,
			 * (int)p.RequestData.TotalObjectCount, Tasks ); }
			 */
		}
	}

	private void handleLandStatReply(IMessage message, Simulator simulator) {
		LandStatReplyMessage m = (LandStatReplyMessage) message;
		Map<UUID, EstateTask> tasks = new HashMap<>(m.reportDataBlocks.length);

		for (LandStatReplyMessage.ReportDataBlock rep : m.reportDataBlocks) {
			EstateTask task = new EstateTask();
			task.position = rep.location;
			task.score = rep.score;
			task.monoScore = rep.monoScore;
			task.taskID = rep.taskID;
			task.taskLocalID = rep.taskLocalID;
			task.taskName = rep.taskName;
			task.ownerName = rep.ownerName;
			tasks.put(task.taskID, task);
		}

		LandStatReportType type = LandStatReportType.setValue(m.reportType);

		if (type == LandStatReportType.TopScripts) {
			onTopScriptsReply.dispatch(new TopScriptsReplyCallbackArgs(m.totalObjectCount, tasks));
		} else if (type == LandStatReportType.TopColliders) {
			onTopCollidersReply.dispatch(new TopCollidersReplyCallbackArgs(m.totalObjectCount, tasks));
		}
	}

}
