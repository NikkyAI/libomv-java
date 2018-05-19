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

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.concurrent.FutureCallback;
import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.LandResourcesInfo;
import libomv.capabilities.CapsMessage.LandResourcesMessage;
import libomv.capabilities.CapsMessage.LandResourcesRequest;
import libomv.capabilities.CapsMessage.ParcelObjectOwnersReplyMessage;
import libomv.capabilities.CapsMessage.ParcelPropertiesMessage;
import libomv.capabilities.CapsMessage.RemoteParcelRequestMessage;
import libomv.capabilities.CapsMessage.RemoteParcelRequestReply;
import libomv.capabilities.CapsMessage.RemoteParcelRequestRequest;
import libomv.capabilities.IMessage;
import libomv.io.capabilities.CapsCallback;
import libomv.io.capabilities.CapsClient;
import libomv.model.Parcel;
import libomv.model.Simulator;
import libomv.model.parcel.AccessList;
import libomv.model.parcel.ForceSelectObjectsReplyCallbackArgs;
import libomv.model.parcel.ObjectReturnType;
import libomv.model.parcel.ParcelAccessEntry;
import libomv.model.parcel.ParcelAccessListReplyCallbackArgs;
import libomv.model.parcel.ParcelDwellReplyCallbackArgs;
import libomv.model.parcel.ParcelInfo;
import libomv.model.parcel.ParcelInfoReplyCallbackArgs;
import libomv.model.parcel.ParcelMedia;
import libomv.model.parcel.ParcelMediaCommand;
import libomv.model.parcel.ParcelMediaCommandCallbackArgs;
import libomv.model.parcel.ParcelMediaUpdateReplyCallbackArgs;
import libomv.model.parcel.ParcelObjectOwnersReplyCallbackArgs;
import libomv.model.parcel.ParcelPrimOwners;
import libomv.model.parcel.ParcelPropertiesCallbackArgs;
import libomv.model.parcel.ParcelResult;
import libomv.model.parcel.SimParcelsDownloadedCallbackArgs;
import libomv.model.parcel.TerraformAction;
import libomv.model.parcel.TerraformBrushSize;
import libomv.packets.EjectUserPacket;
import libomv.packets.ForceObjectSelectPacket;
import libomv.packets.FreezeUserPacket;
import libomv.packets.ModifyLandPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.ParcelAccessListReplyPacket;
import libomv.packets.ParcelAccessListRequestPacket;
import libomv.packets.ParcelBuyPacket;
import libomv.packets.ParcelDeedToGroupPacket;
import libomv.packets.ParcelDividePacket;
import libomv.packets.ParcelDwellReplyPacket;
import libomv.packets.ParcelDwellRequestPacket;
import libomv.packets.ParcelInfoReplyPacket;
import libomv.packets.ParcelInfoRequestPacket;
import libomv.packets.ParcelJoinPacket;
import libomv.packets.ParcelMediaCommandMessagePacket;
import libomv.packets.ParcelMediaUpdatePacket;
import libomv.packets.ParcelObjectOwnersReplyPacket;
import libomv.packets.ParcelObjectOwnersRequestPacket;
import libomv.packets.ParcelOverlayPacket;
import libomv.packets.ParcelPropertiesRequestByIDPacket;
import libomv.packets.ParcelPropertiesRequestPacket;
import libomv.packets.ParcelReclaimPacket;
import libomv.packets.ParcelReleasePacket;
import libomv.packets.ParcelReturnObjectsPacket;
import libomv.packets.ParcelSelectObjectsPacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.RefObject;
import libomv.utils.TimeoutEvent;

public class ParcelManager implements PacketCallback, CapsCallback {
	private static final Logger logger = Logger.getLogger(ParcelManager.class);

	public CallbackHandler<ParcelDwellReplyCallbackArgs> OnParcelDwellReply = new CallbackHandler<ParcelDwellReplyCallbackArgs>();

	public CallbackHandler<ParcelInfoReplyCallbackArgs> OnParcelInfoReply = new CallbackHandler<ParcelInfoReplyCallbackArgs>();

	public CallbackHandler<ParcelPropertiesCallbackArgs> OnParcelProperties = new CallbackHandler<ParcelPropertiesCallbackArgs>();

	public CallbackHandler<ParcelAccessListReplyCallbackArgs> OnParcelAccessListReply = new CallbackHandler<ParcelAccessListReplyCallbackArgs>();

	public CallbackHandler<ParcelObjectOwnersReplyCallbackArgs> OnParcelObjectOwnersReply = new CallbackHandler<ParcelObjectOwnersReplyCallbackArgs>();

	public CallbackHandler<SimParcelsDownloadedCallbackArgs> OnSimParcelsDownloaded = new CallbackHandler<SimParcelsDownloadedCallbackArgs>();

	public CallbackHandler<ForceSelectObjectsReplyCallbackArgs> OnForceSelectObjectsReply = new CallbackHandler<ForceSelectObjectsReplyCallbackArgs>();

	public CallbackHandler<ParcelMediaUpdateReplyCallbackArgs> OnParcelMediaUpdateReply = new CallbackHandler<ParcelMediaUpdateReplyCallbackArgs>();

	public CallbackHandler<ParcelMediaCommandCallbackArgs> OnParcelMediaCommand = new CallbackHandler<ParcelMediaCommandCallbackArgs>();
	// #endregion

	private GridClient _Client;
	private TimeoutEvent<Boolean> WaitForSimParcel;

	public ParcelManager(GridClient client) {
		_Client = client;

		// Setup the callbacks
		_Client.Network.RegisterCallback(CapsEventType.ParcelObjectOwnersReply, this);
		_Client.Network.RegisterCallback(CapsEventType.ParcelProperties, this);

		_Client.Network.RegisterCallback(PacketType.ParcelInfoReply, this);
		_Client.Network.RegisterCallback(PacketType.ParcelDwellReply, this);
		_Client.Network.RegisterCallback(PacketType.ParcelAccessListReply, this);
		_Client.Network.RegisterCallback(PacketType.ForceObjectSelect, this);
		_Client.Network.RegisterCallback(PacketType.ParcelMediaUpdate, this);
		_Client.Network.RegisterCallback(PacketType.ParcelOverlay, this);
		_Client.Network.RegisterCallback(PacketType.ParcelMediaCommandMessage, this);
		_Client.Network.RegisterCallback(PacketType.ParcelObjectOwnersReply, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case ParcelInfoReply:
			HandleParcelInfoReply(packet, simulator);
			break;
		case ParcelDwellReply:
			HandleParcelDwellReply(packet, simulator);
			break;
		case ParcelAccessListReply:
			HandleParcelAccessListReply(packet, simulator);
			break;
		case ForceObjectSelect:
			HandleSelectParcelObjectsReply(packet, simulator);
			break;
		case ParcelMediaUpdate:
			HandleParcelMediaUpdate(packet, simulator);
			break;
		case ParcelOverlay:
			HandleParcelOverlay(packet, simulator);
			break;
		case ParcelMediaCommandMessage:
			HandleParcelMediaCommandMessagePacket(packet, simulator);
			break;
		case ParcelObjectOwnersReply:
			HandleParcelObjectOwnersReply(packet, simulator);
			break;
		default:
			break;
		}
	}

	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) throws Exception {
		switch (message.getType()) {
		case ParcelObjectOwnersReply:
			HandleParcelObjectOwnersReply(message, simulator);
			break;
		case ParcelProperties:
			HandleParcelPropertiesReply(message, simulator);
			break;
		default:
			break;
		}
	}

	// #region Public Methods

	/**
	 * Request basic information for a single parcel
	 *
	 * @param parcelID
	 *            Simulator-local ID of the parcel
	 * @throws Exception
	 */
	public final void RequestParcelInfo(UUID parcelID) throws Exception {
		ParcelInfoRequestPacket request = new ParcelInfoRequestPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.ParcelID = parcelID;

		_Client.Network.sendPacket(request);
	}

	/**
	 * Request properties of a single parcel
	 *
	 * @param simulator
	 *            Simulator containing the parcel
	 * @param localID
	 *            Simulator-local ID of the parcel
	 * @param sequenceID
	 *            An arbitrary integer that will be returned with the
	 *            ParcelProperties reply, useful for distinguishing between multiple
	 *            simultaneous requests
	 * @throws Exception
	 */
	public final void RequestParcelProperties(Simulator simulator, int localID, int sequenceID) throws Exception {
		ParcelPropertiesRequestByIDPacket request = new ParcelPropertiesRequestByIDPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.ParcelData.LocalID = localID;
		request.ParcelData.SequenceID = sequenceID;

		simulator.sendPacket(request);
	}

	/**
	 * Request the access list for a single parcel
	 *
	 * @param simulator
	 *            Simulator containing the parcel
	 * @param localID
	 *            Simulator-local ID of the parcel
	 * @param sequenceID
	 *            An arbitrary integer that will be returned with the
	 *            ParcelAccessList reply, useful for distinguishing between multiple
	 *            simultaneous requests
	 * @param flags
	 * @throws Exception
	 */
	public final void RequestParcelAccessList(Simulator simulator, int localID, byte flags, int sequenceID)
			throws Exception {
		ParcelAccessListRequestPacket request = new ParcelAccessListRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.Data.LocalID = localID;
		request.Data.Flags = AccessList.setValue(flags);
		request.Data.SequenceID = sequenceID;

		simulator.sendPacket(request);
	}

	/**
	 * Request properties of parcels using a bounding box selection
	 *
	 * @param simulator
	 *            Simulator containing the parcel
	 * @param north
	 *            Northern boundary of the parcel selection
	 * @param east
	 *            Eastern boundary of the parcel selection
	 * @param south
	 *            Southern boundary of the parcel selection
	 * @param west
	 *            Western boundary of the parcel selection
	 * @param sequenceID
	 *            An arbitrary integer that will be returned with the
	 *            ParcelProperties reply, useful for distinguishing between
	 *            different types of parcel property requests
	 * @param snapSelection
	 *            A boolean that is returned with the ParcelProperties reply, useful
	 *            for snapping focus to a single parcel
	 * @throws Exception
	 */
	public final void RequestParcelProperties(Simulator simulator, float north, float east, float south, float west,
			int sequenceID, boolean snapSelection) throws Exception {
		ParcelPropertiesRequestPacket request = new ParcelPropertiesRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.ParcelData.North = north;
		request.ParcelData.East = east;
		request.ParcelData.South = south;
		request.ParcelData.West = west;
		request.ParcelData.SequenceID = sequenceID;
		request.ParcelData.SnapSelection = snapSelection;

		simulator.sendPacket(request);
	}

	/**
	 * Request all simulator parcel properties (used for populating the
	 * <code>Simulator.Parcels</code> dictionary)
	 *
	 * @param simulator
	 *            Simulator to request parcels from (must be connected)
	 * @throws Exception
	 */
	public final void RequestAllSimParcels(SimulatorManager simulator) throws Exception {
		RequestAllSimParcels(simulator, false, 750);
	}

	/**
	 * Request all simulator parcel properties (used for populating the
	 * <code>Simulator.Parcels</code> dictionary)
	 *
	 * @param simulator
	 *            Simulator to request parcels from (must be connected)
	 * @param refresh
	 *            If TRUE, will force a full refresh
	 * @param msDelay
	 *            Number of milliseconds to pause in between each request
	 * @throws Exception
	 */
	public final void RequestAllSimParcels(final SimulatorManager simulator, boolean refresh, final int msDelay)
			throws Exception {
		if (simulator.getDownloadingParcelMap()) {
			logger.info(GridClient.Log("Already downloading parcels in " + simulator.getName(), _Client));
			return;
		}
		simulator.setDownloadingParcelMap(true);
		WaitForSimParcel = new TimeoutEvent<Boolean>();

		if (refresh) {
			simulator.clearParcelMap();
		}

		// Wait the given amount of time for a reply before sending the next
		// request
		Thread thread = new Thread() {
			@Override
			public void run() {
				if (!_Client.Network.getConnected()) {
					return;
				}

				int count = 0, timeouts = 0, y, x;
				for (y = 0; y < 64; y++) {
					for (x = 0; x < 64; x++) {

						if (simulator.getParcelMap(y, x) == 0) {
							try {
								RequestParcelProperties(simulator, (y + 1) * 4.0f, (x + 1) * 4.0f, y * 4.0f, x * 4.0f,
										Integer.MAX_VALUE, false);
								if (WaitForSimParcel.waitOne(msDelay) == null) {
									++timeouts;
								}
							} catch (Exception e) {
							}
							++count;
						}
					}
				}
				logger.info(GridClient.Log(String.format(
						"Full simulator parcel information retrieved. Sent %d parcel requests. Current outgoing queue: %d, Retry Count %d",
						count, _Client.Network.getOutboxCount(), timeouts), _Client));
				WaitForSimParcel = null;
				simulator.setDownloadingParcelMap(false);
			}
		};
		thread.start();
	}

	/**
	 * Request the dwell value for a parcel
	 *
	 * @param simulator
	 *            Simulator containing the parcel
	 * @param localID
	 *            Simulator-local ID of the parcel
	 * @throws Exception
	 */
	public final void RequestDwell(Simulator simulator, int localID) throws Exception {
		ParcelDwellRequestPacket request = new ParcelDwellRequestPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.Data.LocalID = localID;
		request.Data.ParcelID = UUID.Zero; // Not used by clients

		simulator.sendPacket(request);
	}

	/**
	 * Send a request to Purchase a parcel of land
	 *
	 * @param simulator
	 *            The Simulator the parcel is located in
	 * @param localID
	 *            The parcels region specific local ID
	 * @param forGroup
	 *            true if this parcel is being purchased by a group
	 * @param groupID
	 *            The groups {@link T:OpenMetaverse.UUID}
	 * @param removeContribution
	 *            true to remove tier contribution if purchase is successful
	 * @param parcelArea
	 *            The parcels size
	 * @param parcelPrice
	 *            The purchase price of the parcel
	 * @throws Exception
	 */
	public final void Buy(Simulator simulator, int localID, boolean forGroup, UUID groupID, boolean removeContribution,
			int parcelArea, int parcelPrice) throws Exception {
		ParcelBuyPacket request = new ParcelBuyPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.Data.Final = true;
		request.Data.GroupID = groupID;
		request.Data.LocalID = localID;
		request.Data.IsGroupOwned = forGroup;
		request.Data.RemoveContribution = removeContribution;

		request.ParcelData.Area = parcelArea;
		request.ParcelData.Price = parcelPrice;

		simulator.sendPacket(request);
	}

	/**
	 * Reclaim a parcel of land
	 *
	 * @param simulator
	 *            The simulator the parcel is in
	 * @param localID
	 *            The parcels region specific local ID
	 * @throws Exception
	 */
	public final void Reclaim(Simulator simulator, int localID) throws Exception {
		ParcelReclaimPacket request = new ParcelReclaimPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.LocalID = localID;

		simulator.sendPacket(request);
	}

	/**
	 * Deed a parcel to a group
	 *
	 * @param simulator
	 *            The simulator the parcel is in
	 * @param localID
	 *            The parcels region specific local ID
	 * @param groupID
	 *            The groups {@link T:OpenMetaverse.UUID}
	 * @throws Exception
	 */
	public final void DeedToGroup(Simulator simulator, int localID, UUID groupID) throws Exception {
		ParcelDeedToGroupPacket request = new ParcelDeedToGroupPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.Data.LocalID = localID;
		request.Data.GroupID = groupID;

		simulator.sendPacket(request);
	}

	/**
	 * Request prim owners of a parcel of land.
	 *
	 * @param simulator
	 *            Simulator parcel is in
	 * @param localID
	 *            The parcels region specific local ID
	 * @throws Exception
	 */
	public final void RequestObjectOwners(Simulator simulator, int localID) throws Exception {
		ParcelObjectOwnersRequestPacket request = new ParcelObjectOwnersRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.LocalID = localID;
		simulator.sendPacket(request);
	}

	/**
	 * Return objects from a parcel
	 *
	 * @param simulator
	 *            Simulator parcel is in
	 * @param localID
	 *            The parcels region specific local ID
	 * @param type
	 *            the type of objects to return, {@link T
	 *            :OpenMetaverse.ObjectReturnType}
	 * @param ownerIDs
	 *            A list containing object owners {@link OpenMetaverse.UUID} s to
	 *            return
	 * @throws Exception
	 */
	public final void ReturnObjects(Simulator simulator, int localID, byte type, UUID[] ownerIDs) throws Exception {
		ParcelReturnObjectsPacket request = new ParcelReturnObjectsPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.ParcelData.LocalID = localID;
		request.ParcelData.ReturnType = ObjectReturnType.setValue(type);

		// A single null TaskID is (not) used for parcel object returns
		request.TaskID = new UUID[1];
		request.TaskID[0] = UUID.Zero;

		// Convert the list of owner UUIDs to packet blocks if a list is given
		if (ownerIDs != null) {
			request.OwnerID = new UUID[ownerIDs.length];

			for (int i = 0; i < ownerIDs.length; i++) {
				request.OwnerID[i] = ownerIDs[i];
			}
		} else {
			request.OwnerID = new UUID[0];
		}

		simulator.sendPacket(request);
	}

	/**
	 * Subdivide (split) a parcel
	 *
	 * @param simulator
	 * @param west
	 * @param south
	 * @param east
	 * @param north
	 * @throws Exception
	 */
	public final void ParcelSubdivide(Simulator simulator, float west, float south, float east, float north)
			throws Exception {
		ParcelDividePacket divide = new ParcelDividePacket();
		divide.AgentData.AgentID = _Client.Self.getAgentID();
		divide.AgentData.SessionID = _Client.Self.getSessionID();
		divide.ParcelData.East = east;
		divide.ParcelData.North = north;
		divide.ParcelData.South = south;
		divide.ParcelData.West = west;

		simulator.sendPacket(divide);
	}

	/**
	 * Join two parcels of land creating a single parcel
	 *
	 * @param simulator
	 * @param west
	 * @param south
	 * @param east
	 * @param north
	 * @throws Exception
	 */
	public final void ParcelJoin(Simulator simulator, float west, float south, float east, float north)
			throws Exception {
		ParcelJoinPacket join = new ParcelJoinPacket();
		join.AgentData.AgentID = _Client.Self.getAgentID();
		join.AgentData.SessionID = _Client.Self.getSessionID();
		join.ParcelData.East = east;
		join.ParcelData.North = north;
		join.ParcelData.South = south;
		join.ParcelData.West = west;

		simulator.sendPacket(join);
	}

	/**
	 * Get a parcels LocalID
	 *
	 * @param simulator
	 *            Simulator parcel is in
	 * @param position
	 *            Vector3 position in simulator (Z not used)
	 * @return 0 on failure, or parcel LocalID on success. A call to
	 *         <code>Parcels.RequestAllSimParcels</code> is required to populate map
	 *         and dictionary.
	 */
	public final int GetParcelLocalID(SimulatorManager simulator, Vector3 position) {
		int value = simulator.getParcelMap((int) position.X / 4, (int) position.Y / 4);
		if (value > 0) {
			return value;
		}

		logger.warn(String.format(
				"ParcelMap returned an default/invalid value for location %d/%d Did you use RequestAllSimParcels() to populate the dictionaries?",
				(int) position.X / 4, (int) position.Y / 4));
		return 0;
	}

	/**
	 * Terraform (raise, lower, etc) an area or whole parcel of land
	 *
	 * @param simulator
	 *            Simulator land area is in.
	 * @param localID
	 *            LocalID of parcel, or -1 if using bounding box
	 * @param action
	 *            From Enum, Raise, Lower, Level, Smooth, Etc.
	 * @param brushSize
	 *            Size of area to modify
	 * @return true on successful request sent. Settings.STORE_LAND_PATCHES must be
	 *         true, Parcel information must be downloaded using
	 *         <code>RequestAllSimParcels()</code>
	 * @throws Exception
	 */
	public final boolean Terraform(SimulatorManager simulator, int localID, TerraformAction action, float brushSize)
			throws Exception {
		return Terraform(simulator, localID, 0f, 0f, 0f, 0f, action, brushSize, 1);
	}

	/**
	 * Terraform (raise, lower, etc) an area or whole parcel of land
	 *
	 * @param simulator
	 *            Simulator land area is in.
	 * @param west
	 *            west border of area to modify
	 * @param south
	 *            south border of area to modify
	 * @param east
	 *            east border of area to modify
	 * @param north
	 *            north border of area to modify
	 * @param action
	 *            From Enum, Raise, Lower, Level, Smooth, Etc.
	 * @param brushSize
	 *            Size of area to modify
	 * @return true on successful request sent. Settings.STORE_LAND_PATCHES must be
	 *         true, Parcel information must be downloaded using
	 *         <code>RequestAllSimParcels()</code>
	 * @throws Exception
	 */
	public final boolean Terraform(SimulatorManager simulator, float west, float south, float east, float north,
			TerraformAction action, float brushSize) throws Exception {
		return Terraform(simulator, -1, west, south, east, north, action, brushSize, 1);
	}

	/**
	 * Terraform (raise, lower, etc) an area or whole parcel of land
	 *
	 * @param simulator
	 *            Simulator land area is in.
	 * @param localID
	 *            LocalID of parcel, or -1 if using bounding box
	 * @param west
	 *            west border of area to modify
	 * @param south
	 *            south border of area to modify
	 * @param east
	 *            east border of area to modify
	 * @param north
	 *            north border of area to modify
	 * @param action
	 *            From Enum, Raise, Lower, Level, Smooth, Etc.
	 * @param brushSize
	 *            Size of area to modify
	 * @param seconds
	 *            How many meters + or - to lower, 1 = 1 meter
	 * @return true on successful request sent. Settings.STORE_LAND_PATCHES must be
	 *         true, Parcel information must be downloaded using
	 *         <code>RequestAllSimParcels()</code>
	 * @throws Exception
	 */
	public final boolean Terraform(SimulatorManager simulator, int localID, float west, float south, float east,
			float north, TerraformAction action, float brushSize, int seconds) throws Exception {
		float height = 0f;
		int x, y;
		if (localID == -1) {
			x = (int) east - (int) west / 2;
			y = (int) north - (int) south / 2;
		} else {
			Parcel p;
			if (!simulator.Parcels.containsKey(localID)) {
				logger.warn(GridClient.Log(String.format("Can't find parcel %d in simulator %s", localID, simulator),
						_Client));
				return false;
			}
			p = simulator.getParcels().get(localID);
			x = (int) p.aabbMax.X - (int) p.aabbMin.X / 2;
			y = (int) p.aabbMax.Y - (int) p.aabbMin.Y / 2;
		}
		RefObject<Float> ref = new RefObject<Float>(height);
		if (Float.isNaN(simulator.TerrainHeightAtPoint(x, y))) {
			logger.warn(GridClient.Log("Land Patch not stored for location", _Client));
			return false;
		}

		Terraform(simulator, localID, west, south, east, north, action, brushSize, seconds, ref.argvalue);
		return true;
	}

	/**
	 * Terraform (raise, lower, etc) an area or whole parcel of land
	 *
	 * @param simulator
	 *            Simulator land area is in.
	 * @param localID
	 *            LocalID of parcel, or -1 if using bounding box
	 * @param west
	 *            west border of area to modify
	 * @param south
	 *            south border of area to modify
	 * @param east
	 *            east border of area to modify
	 * @param north
	 *            north border of area to modify
	 * @param action
	 *            From Enum, Raise, Lower, Level, Smooth, Etc.
	 * @param brushSize
	 *            Size of area to modify
	 * @param seconds
	 *            How many meters + or - to lower, 1 = 1 meter
	 * @param height
	 *            Height at which the terraform operation is acting at
	 * @throws Exception
	 */
	public final void Terraform(Simulator simulator, int localID, float west, float south, float east, float north,
			TerraformAction action, float brushSize, int seconds, float height) throws Exception {
		ModifyLandPacket land = new ModifyLandPacket();
		land.AgentData.AgentID = _Client.Self.getAgentID();
		land.AgentData.SessionID = _Client.Self.getSessionID();

		land.ModifyBlock.Action = action.getValue();
		land.ModifyBlock.BrushSize = TerraformBrushSize.getIndex(brushSize);
		land.ModifyBlock.Seconds = seconds;
		land.ModifyBlock.Height = height;

		land.ParcelData = new ModifyLandPacket.ParcelDataBlock[1];
		land.ParcelData[0] = land.new ParcelDataBlock();
		land.ParcelData[0].LocalID = localID;
		land.ParcelData[0].West = west;
		land.ParcelData[0].South = south;
		land.ParcelData[0].East = east;
		land.ParcelData[0].North = north;

		land.BrushSize = new float[1];
		land.BrushSize[0] = brushSize;

		simulator.sendPacket(land);
	}

	/**
	 * Sends a request to the simulator to return a list of objects owned by
	 * specific owners
	 *
	 * @param localID
	 *            Simulator local ID of parcel
	 * @param selectType
	 *            Owners, Others, Etc
	 * @param ownerID
	 *            List containing keys of avatars objects to select; if List is null
	 *            will return Objects of type <c>selectType</c> Response data is
	 *            returned in the event {@link E :OnParcelSelectedObjects}
	 */
	public final void RequestSelectObjects(int localID, byte selectType, UUID ownerID) throws Exception {
		ParcelSelectObjectsPacket select = new ParcelSelectObjectsPacket();
		select.AgentData.AgentID = _Client.Self.getAgentID();
		select.AgentData.SessionID = _Client.Self.getSessionID();

		select.ParcelData.LocalID = localID;
		select.ParcelData.ReturnType = selectType;

		select.ReturnID = new UUID[1];
		select.ReturnID[0] = ownerID;

		_Client.Network.sendPacket(select);
	}

	/**
	 * Eject and optionally ban a user from a parcel
	 *
	 * @param targetID
	 *            target key of avatar to eject
	 * @param ban
	 *            true to also ban target
	 */
	public final void EjectUser(UUID targetID, boolean ban) throws Exception {
		EjectUserPacket eject = new EjectUserPacket();
		eject.AgentData.AgentID = _Client.Self.getAgentID();
		eject.AgentData.SessionID = _Client.Self.getSessionID();
		eject.Data.TargetID = targetID;
		if (ban) {
			eject.Data.Flags = 1;
		} else {
			eject.Data.Flags = 0;
		}
		_Client.Network.sendPacket(eject);
	}

	/**
	 * Freeze or unfreeze an avatar over your land
	 *
	 * @param targetID
	 *            target key to freeze
	 * @param freeze
	 *            true to freeze, false to unfreeze
	 */
	public final void FreezeUser(UUID targetID, boolean freeze) throws Exception {
		FreezeUserPacket frz = new FreezeUserPacket();
		frz.AgentData.AgentID = _Client.Self.getAgentID();
		frz.AgentData.SessionID = _Client.Self.getSessionID();
		frz.Data.TargetID = targetID;
		if (freeze) {
			frz.Data.Flags = 0;
		} else {
			frz.Data.Flags = 1;
		}

		_Client.Network.sendPacket(frz);
	}

	/**
	 * Abandon a parcel of land
	 *
	 * @param simulator
	 *            Simulator parcel is in
	 * @param localID
	 *            Simulator local ID of parcel
	 */
	public final void ReleaseParcel(Simulator simulator, int localID) throws Exception {
		ParcelReleasePacket abandon = new ParcelReleasePacket();
		abandon.AgentData.AgentID = _Client.Self.getAgentID();
		abandon.AgentData.SessionID = _Client.Self.getSessionID();
		abandon.LocalID = localID;

		simulator.sendPacket(abandon);
	}

	/**
	 * Requests the UUID of the parcel in a remote region at a specified location
	 *
	 * @param location
	 *            Location of the parcel in the remote region
	 * @param regionHandle
	 *            Remote region handle
	 * @param regionID
	 *            Remote region UUID
	 * @return If successful UUID of the remote parcel, UUID.Zero otherwise
	 */
	public final UUID RequestRemoteParcelID(Vector3 location, long regionHandle, UUID regionID) {
		URI url = _Client.Network.getCapabilityURI("RemoteParcelRequest");
		if (url != null) {
			RemoteParcelRequestRequest req = _Client.Messages.new RemoteParcelRequestRequest();
			req.location = location;
			req.regionHandle = regionHandle;
			req.regionID = regionID;

			try {
				OSD result = new CapsClient(_Client, "RequestRemoteParcelID").getResponse(url, req, null,
						_Client.Settings.CAPS_TIMEOUT);
				RemoteParcelRequestMessage response = (RemoteParcelRequestMessage) _Client.Messages
						.decodeEvent(CapsEventType.RemoteParcelRequest, (OSDMap) result);
				return ((RemoteParcelRequestReply) response.request).parcelID;
			} catch (Throwable t) {
				logger.debug(GridClient.Log("Failed to fetch remote parcel ID", _Client));
			}
		}

		return UUID.Zero;

	}

	public interface LandResourcesInfoCallback {
		public void callback(boolean success, LandResourcesInfo info);
	}

	/**
	 * Retrieves information on resources used by the parcel
	 *
	 * @param parcelID
	 *            UUID of the parcel
	 * @param getDetails
	 *            Should per object resource usage be requested
	 * @param callback
	 *            Callback invoked when the request failed or is complete
	 */
	public final void GetParcelResouces(UUID parcelID, boolean getDetails, LandResourcesInfoCallback callback) {
		try {
			URI url = _Client.Network.getCapabilityURI(CapsEventType.LandResources.toString());
			CapsClient request = new CapsClient(_Client, CapsEventType.LandResources.toString());
			LandResourcesRequest req = _Client.Messages.new LandResourcesRequest();
			req.parcelID = parcelID;
			request.executeHttpPost(url, req, new LandResourcesMessageHandler(getDetails, callback),
					_Client.Settings.CAPS_TIMEOUT);

		} catch (Exception ex) {
			logger.error(GridClient.Log("Failed fetching land resources:", _Client), ex);
			callback.callback(false, null);
		}
	}

	// #endregion Public Methods

	private class LandResourcesMessageHandler implements FutureCallback<OSD> {
		private final LandResourcesInfoCallback callback;
		private final boolean getDetails;

		public LandResourcesMessageHandler(boolean getDetails, LandResourcesInfoCallback callback) {
			this.getDetails = getDetails;
			this.callback = callback;
		}

		@Override
		public void completed(OSD result) {
			try {
				if (result == null) {
					callback.callback(false, null);
				}
				LandResourcesMessage response = _Client.Messages.new LandResourcesMessage();
				response.deserialize((OSDMap) result);
				OSD osd = new CapsClient(_Client, CapsEventType.LandResources.toString()).getResponse(
						response.scriptResourceSummary, Helpers.EmptyString, _Client.Settings.CAPS_TIMEOUT);

				LandResourcesInfo info = _Client.Messages.new LandResourcesInfo();
				info.deserialize((OSDMap) osd);
				if (response.scriptResourceDetails != null && getDetails) {
					osd = new CapsClient(_Client, CapsEventType.LandResources.toString()).getResponse(
							response.scriptResourceDetails, Helpers.EmptyString, _Client.Settings.CAPS_TIMEOUT);
					info.deserialize((OSDMap) osd);
				}
				callback.callback(true, info);
			} catch (Exception ex) {
				failed(ex);
			}
		}

		@Override
		public void cancelled() {
			logger.error(GridClient.Log("Fetching land resources was cancelled", _Client));
			callback.callback(false, null);
		}

		@Override
		public void failed(Exception ex) {
			logger.error(GridClient.Log("Failed fetching land resources", _Client), ex);
			callback.callback(false, null);
		}

	}

	private final void HandleParcelDwellReply(Packet packet, Simulator simulator) {
		ParcelDwellReplyPacket dwell = (ParcelDwellReplyPacket) packet;

		synchronized (simulator.getParcels()) {
			if (dwell.Data.Dwell != 0.0F && simulator.getParcels().containsKey(dwell.Data.LocalID)) {
				simulator.getParcels().get(dwell.Data.LocalID).dwell = dwell.Data.Dwell;
			}
		}
		OnParcelDwellReply
				.dispatch(new ParcelDwellReplyCallbackArgs(dwell.Data.ParcelID, dwell.Data.LocalID, dwell.Data.Dwell));
	}

	private final void HandleParcelPropertiesReply(IMessage message, SimulatorManager simulator) throws Exception {
		if (OnParcelProperties.count() > 0 || _Client.Settings.PARCEL_TRACKING == true) {
			ParcelPropertiesMessage msg = (ParcelPropertiesMessage) message;

			Parcel parcel = new Parcel(msg.localID);

			parcel.aabbMax = msg.aabbMax;
			parcel.aabbMin = msg.aabbMin;
			parcel.area = msg.area;
			parcel.auctionID = msg.auctionID;
			parcel.authBuyerID = msg.authBuyerID;
			parcel.bitmap = msg.bitmap;
			parcel.category = msg.category;
			parcel.claimDate = msg.claimDate;
			parcel.claimPrice = msg.claimPrice;
			parcel.desc = msg.desc;
			parcel.flags = msg.parcelFlags;
			parcel.groupID = msg.groupID;
			parcel.groupPrims = msg.groupPrims;
			parcel.isGroupOwned = msg.isGroupOwned;
			parcel.landing = msg.landingType;
			parcel.maxPrims = msg.maxPrims;
			parcel.media.mediaAutoScale = msg.mediaAutoScale;
			parcel.media.mediaID = msg.mediaID;
			parcel.media.mediaURL = msg.mediaURL;
			parcel.musicURL = msg.musicURL;
			parcel.name = msg.name;
			parcel.otherCleanTime = msg.otherCleanTime;
			parcel.otherCount = msg.otherCount;
			parcel.otherPrims = msg.otherPrims;
			parcel.ownerID = msg.ownerID;
			parcel.ownerPrims = msg.ownerPrims;
			parcel.parcelPrimBonus = msg.parcelPrimBonus;
			parcel.passHours = msg.passHours;
			parcel.passPrice = msg.passPrice;
			parcel.publicCount = msg.publicCount;
			parcel.regionDenyAgeUnverified = msg.regionDenyAgeUnverified;
			parcel.regionDenyAnonymous = msg.regionDenyAnonymous;
			parcel.regionPushOverride = msg.regionPushOverride;
			parcel.rentPrice = msg.rentPrice;
			ParcelResult result = msg.requestResult;
			parcel.salePrice = msg.salePrice;
			int selectedPrims = msg.selectedPrims;
			parcel.selfCount = msg.selfCount;
			int sequenceID = msg.sequenceID;
			parcel.simWideMaxPrims = msg.simWideMaxPrims;
			parcel.simWideTotalPrims = msg.simWideTotalPrims;
			boolean snapSelection = msg.snapSelection;
			parcel.snapshotID = msg.snapshotID;
			parcel.status = msg.status;
			parcel.totalPrims = msg.totalPrims;
			parcel.userLocation = msg.userLocation;
			parcel.userLookAt = msg.userLookAt;
			parcel.media.mediaDesc = msg.mediaDesc;
			parcel.media.mediaHeight = msg.mediaHeight;
			parcel.media.mediaWidth = msg.mediaWidth;
			parcel.media.mediaLoop = msg.mediaLoop;
			parcel.media.mediaType = msg.mediaType;
			parcel.obscureMedia = msg.obscureMedia;
			parcel.obscureMusic = msg.obscureMusic;
			parcel.seeAVs = msg.seeAVs;
			parcel.anyAVSounds = msg.anyAVSounds;
			parcel.groupAVSounds = msg.groupAVSounds;

			if (_Client.Settings.PARCEL_TRACKING) {
				synchronized (simulator.getParcels()) {
					simulator.getParcels().put(parcel.localID, parcel);
				}

				boolean set = false;
				int y, x, index, bit;
				for (y = 0; y < 64; y++) {
					for (x = 0; x < 64; x++) {
						index = (y * 64) + x;
						bit = index % 8;
						index >>= 3;

						if ((parcel.bitmap[index] & (1 << bit)) != 0) {
							simulator.setParcelMap(y, x, parcel.localID);
							set = true;
						}
					}
				}

				if (!set) {
					logger.warn("Received a parcel with a bitmap that did not map to any locations");
				}
			}

			if (((Integer) sequenceID).equals(Integer.MAX_VALUE) && WaitForSimParcel != null) {
				WaitForSimParcel.set(true);
			}

			// auto request acl, will be stored in parcel tracking dictionary if
			// enabled
			if (_Client.Settings.ALWAYS_REQUEST_PARCEL_ACL) {
				RequestParcelAccessList(simulator, parcel.localID, AccessList.Both, sequenceID);
			}

			// auto request dwell, will be stored in parcel tracking dictionary
			// if enables
			if (_Client.Settings.ALWAYS_REQUEST_PARCEL_DWELL) {
				RequestDwell(simulator, parcel.localID);
			}

			// Fire the callback for parcel properties being received
			if (OnParcelProperties != null) {
				OnParcelProperties.dispatch(new ParcelPropertiesCallbackArgs(simulator, parcel, result, selectedPrims,
						sequenceID, snapSelection));
			}

			// Check if all of the simulator parcels have been retrieved, if so
			// fire another callback
			if (simulator.IsParcelMapFull() && OnSimParcelsDownloaded.count() > 0) {
				OnSimParcelsDownloaded.dispatch(
						new SimParcelsDownloadedCallbackArgs(simulator, simulator.Parcels, simulator.getParcelMap()));
			}
		}
	}

	private final void HandleParcelInfoReply(Packet packet, Simulator simulator) throws Exception {
		ParcelInfoReplyPacket info = (ParcelInfoReplyPacket) packet;

		ParcelInfo parcelInfo = new ParcelInfo();

		parcelInfo.actualArea = info.Data.ActualArea;
		parcelInfo.auctionID = info.Data.AuctionID;
		parcelInfo.billableArea = info.Data.BillableArea;
		parcelInfo.description = Helpers.BytesToString(info.Data.getDesc());
		parcelInfo.dwell = info.Data.Dwell;
		parcelInfo.globalX = info.Data.GlobalX;
		parcelInfo.globalY = info.Data.GlobalY;
		parcelInfo.globalZ = info.Data.GlobalZ;
		parcelInfo.id = info.Data.ParcelID;
		parcelInfo.mature = ((info.Data.Flags & 1) != 0) ? true : false;
		parcelInfo.name = Helpers.BytesToString(info.Data.getName());
		parcelInfo.ownerID = info.Data.OwnerID;
		parcelInfo.salePrice = info.Data.SalePrice;
		parcelInfo.simName = Helpers.BytesToString(info.Data.getSimName());
		parcelInfo.snapshotID = info.Data.SnapshotID;

		OnParcelInfoReply.dispatch(new ParcelInfoReplyCallbackArgs(parcelInfo));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * Raises the <see cref="ParcelAccessListReply"/> event
	 */
	private final void HandleParcelAccessListReply(Packet packet, Simulator simulator) {
		if (OnParcelAccessListReply.count() > 0 || _Client.Settings.ALWAYS_REQUEST_PARCEL_ACL) {
			ParcelAccessListReplyPacket reply = (ParcelAccessListReplyPacket) packet;

			ArrayList<ParcelAccessEntry> accessList = new ArrayList<ParcelAccessEntry>(reply.List.length);

			for (int i = 0; i < reply.List.length; i++) {
				ParcelAccessEntry pae = new ParcelAccessEntry();
				pae.agentID = reply.List[i].ID;
				pae.time = Helpers.UnixTimeToDateTime(reply.List[i].Time);
				pae.flags = AccessList.setValue(reply.List[i].Flags);

				accessList.add(pae);
			}

			synchronized (simulator.getParcels()) {
				if (simulator.getParcels().containsKey(reply.Data.LocalID)) {
					Parcel parcel = simulator.getParcels().get(reply.Data.LocalID);
					if (reply.Data.Flags == AccessList.Ban) {
						parcel.accessBlackList = accessList;
					} else {
						parcel.accessWhiteList = accessList;
					}

					simulator.getParcels().put(reply.Data.LocalID, parcel);
				}
			}
			OnParcelAccessListReply.dispatch(new ParcelAccessListReplyCallbackArgs(simulator, reply.Data.SequenceID,
					reply.Data.LocalID, reply.Data.Flags, accessList));
		}
	}

	private final void HandleParcelObjectOwnersReply(Packet packet, Simulator simulator) {
		if (OnParcelObjectOwnersReply.count() > 0) {
			ArrayList<ParcelPrimOwners> primOwners = new ArrayList<ParcelPrimOwners>();

			ParcelObjectOwnersReplyPacket msg = (ParcelObjectOwnersReplyPacket) packet;

			for (int i = 0; i < msg.Data.length; i++) {
				ParcelPrimOwners primOwner = new ParcelPrimOwners();
				primOwner.ownerID = msg.Data[i].OwnerID;
				primOwner.count = msg.Data[i].Count;
				primOwner.isGroupOwned = msg.Data[i].IsGroupOwned;
				primOwner.onlineStatus = msg.Data[i].OnlineStatus;
				if (msg.TimeStamp != null && i < msg.TimeStamp.length)
					primOwner.newestPrim = new Date(msg.TimeStamp[i] & 0xFFFF);

				primOwners.add(primOwner);
			}
			OnParcelObjectOwnersReply.dispatch(new ParcelObjectOwnersReplyCallbackArgs(simulator, primOwners));
		}
	}

	private final void HandleParcelObjectOwnersReply(IMessage message, Simulator simulator) {
		if (OnParcelObjectOwnersReply.count() > 0) {
			ArrayList<ParcelPrimOwners> primOwners = new ArrayList<ParcelPrimOwners>();

			ParcelObjectOwnersReplyMessage msg = (ParcelObjectOwnersReplyMessage) message;

			for (int i = 0; i < msg.PrimOwnersBlock.length; i++) {
				ParcelPrimOwners primOwner = new ParcelPrimOwners();
				primOwner.ownerID = msg.PrimOwnersBlock[i].ownerID;
				primOwner.count = msg.PrimOwnersBlock[i].count;
				primOwner.isGroupOwned = msg.PrimOwnersBlock[i].isGroupOwned;
				primOwner.onlineStatus = msg.PrimOwnersBlock[i].onlineStatus;
				primOwner.newestPrim = msg.PrimOwnersBlock[i].timeStamp;

				primOwners.add(primOwner);
			}
			OnParcelObjectOwnersReply.dispatch(new ParcelObjectOwnersReplyCallbackArgs(simulator, primOwners));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * Raises the <see cref="ForceSelectObjectsReply"/> event
	 */
	private final void HandleSelectParcelObjectsReply(Packet packet, Simulator simulator) {
		if (OnForceSelectObjectsReply.count() > 0) {
			ForceObjectSelectPacket reply = (ForceObjectSelectPacket) packet;
			int[] objectIDs = new int[reply.LocalID.length];

			for (int i = 0; i < reply.LocalID.length; i++) {
				objectIDs[i] = reply.LocalID[i];
			}
			OnForceSelectObjectsReply
					.dispatch(new ForceSelectObjectsReplyCallbackArgs(simulator, objectIDs, reply.ResetList));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * Raises the <see cref="ParcelMediaUpdateReply"/> event
	 *
	 * @throws Exception
	 */
	private final void HandleParcelMediaUpdate(Packet packet, Simulator simulator) throws Exception {
		if (OnParcelMediaUpdateReply != null) {
			ParcelMediaUpdatePacket reply = (ParcelMediaUpdatePacket) packet;
			ParcelMedia media = new ParcelMedia();

			media.mediaAutoScale = (reply.DataBlock.MediaAutoScale == (byte) 0x1) ? true : false;
			media.mediaID = reply.DataBlock.MediaID;
			media.mediaDesc = Helpers.BytesToString(reply.DataBlockExtended.getMediaDesc());
			media.mediaHeight = reply.DataBlockExtended.MediaHeight;
			media.mediaLoop = ((reply.DataBlockExtended.MediaLoop & 1) != 0) ? true : false;
			media.mediaType = Helpers.BytesToString(reply.DataBlockExtended.getMediaType());
			media.mediaWidth = reply.DataBlockExtended.MediaWidth;
			media.mediaURL = Helpers.BytesToString(reply.DataBlock.getMediaURL());

			OnParcelMediaUpdateReply.dispatch(new ParcelMediaUpdateReplyCallbackArgs(simulator, media));
		}
	}

	private final void HandleParcelOverlay(Packet packet, Simulator sim) {
		final int OVERLAY_COUNT = 4;
		ParcelOverlayPacket overlay = (ParcelOverlayPacket) packet;
		SimulatorManager simulator = (SimulatorManager) sim;

		if (overlay.ParcelData.SequenceID >= 0 && overlay.ParcelData.SequenceID < OVERLAY_COUNT) {
			int length = overlay.ParcelData.getData().length;

			System.arraycopy(overlay.ParcelData.getData(), 0, simulator.ParcelOverlay,
					overlay.ParcelData.SequenceID * length, length);
			simulator.ParcelOverlaysReceived++;

			if (simulator.ParcelOverlaysReceived >= OVERLAY_COUNT) {
				// TODO: ParcelOverlaysReceived should become internal, and
				// reset to zero every time it hits four. Also need a callback
				// here
				logger.info("Finished building the " + simulator.getName() + " parcel overlay");
			}
		} else {
			logger.warn(GridClient.Log("Parcel overlay with sequence ID of " + overlay.ParcelData.SequenceID
					+ " received from " + simulator.toString(), _Client));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * Raises the <see cref="ParcelMediaCommand"/> event
	 */
	private final void HandleParcelMediaCommandMessagePacket(Packet packet, Simulator simulator) {
		if (OnParcelMediaCommand != null) {
			ParcelMediaCommandMessagePacket pmc = (ParcelMediaCommandMessagePacket) packet;
			ParcelMediaCommandMessagePacket.CommandBlockBlock block = pmc.CommandBlock;

			OnParcelMediaCommand.dispatch(new ParcelMediaCommandCallbackArgs(simulator, pmc.getHeader().getSequence(),
					block.Flags, ParcelMediaCommand.setValue(block.Command), block.Time));
		}
	}
}
