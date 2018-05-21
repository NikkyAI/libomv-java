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
package libomv.capabilities;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.inventory.InventoryFolder.FolderType;
import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.model.agent.AgentDisplayName;
import libomv.model.agent.InstantMessageDialog;
import libomv.model.agent.InstantMessageOnline;
import libomv.model.agent.TeleportFlags;
import libomv.model.asset.AssetType;
import libomv.model.object.SaleType;
import libomv.model.parcel.LandingTypeEnum;
import libomv.model.parcel.ParcelCategory;
import libomv.model.parcel.ParcelFlags;
import libomv.model.parcel.ParcelResult;
import libomv.model.parcel.ParcelStatus;
import libomv.model.simulator.SimAccess;
import libomv.primitives.MediaEntry;
import libomv.primitives.PhysicsProperties;
import libomv.primitives.Primitive.AttachmentPoint;
import libomv.primitives.Primitive.ExtraParamType;
import libomv.primitives.Primitive.Material;
import libomv.primitives.Primitive.SculptType;
import libomv.primitives.TextureEntry.Bumpiness;
import libomv.types.Color4;
import libomv.types.Permissions.PermissionMask;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.utils.Helpers;

public class CapsMessage implements IMessage {
	private static final Logger logger = Logger.getLogger(CapsMessage.class);

	public enum CapsEventType {
		Default, AgentGroupDataUpdate, AgentStateUpdate, AvatarGroupsReply, ParcelProperties, ParcelObjectOwnersReply, TeleportFinish, EnableSimulator, ParcelPropertiesUpdate, EstablishAgentCommunication, ChatterBoxInvitation, ChatterBoxSessionEventReply, ChatterBoxSessionStartReply, ChatterBoxSessionAgentListUpdates, HomeLocation, RequiredVoiceVersion, MapLayer, ChatSessionRequest, ChatSessionRequestMuteUpdate, ChatSessionRequestStartConference, ChatSessionAcceptInvitation, CopyInventoryFromNotecard, ProvisionVoiceAccountRequest, Viewerstats, UpdateAgentLanguage, RemoteParcelRequest, RemoteParcelRequestRequest, RemoteParcelRequestReply, UpdateScriptTask, UpdateScriptTaskUpdateMessage, UploadScriptTask, UpdateScriptAgent, UploaderScriptRequestError, SendPostcard, UpdateGestureAgentInventory, UpdateNotecardAgentInventory, LandStatReply, ParcelVoiceInfoRequest, ViewerStats, EventQueueGet, CrossedRegion, TeleportFailed, PlacesReply, UpdateAgentInformation, DirLandReply, ScriptRunningReply, SearchStatRequest, AgentDropGroup, ForceCloseChatterBoxSession, UploadBakedTexture, WebFetchInventoryDescendents, RegionInfo, UploadObjectAsset, ObjectPhysicsProperties, ObjectMediaNavigate, ObjectMedia, GetObjectCost, GetObjectPhysicsData, AttachmentResources, LandResources, ProductInfoRequest, DispatchRegionInfo, EstateChangeInfo, FetchInventoryDescendents, GroupProposalBallot, GroupAPIv1, MapLayerGod, NewFileAgentInventory, BulkUpdateInventory, RenderMaterials, RequestTextureDownload, SearchStatTracking, SendUserReport, SendUserReportWithScreenshot, ServerReleaseNotes, StartGroupProposal, UpdateGestureTaskInventory, UpdateNotecardTaskInventory, ViewerStartAuction, UntrustedSimulatorMessage, GetDisplayNames, SetDisplayName, SetDisplayNameReply, DisplayNameUpdate,
	}

	@Override
	public CapsEventType getType() {
		return CapsEventType.Default;
	}

	// #region Teleport/Region/Movement Messages

	/* Sent to the client to indicate a teleport request has completed */
	public class TeleportFinishMessage implements IMessage {
		// The <see cref="UUID"/> of the agent
		public UUID agentID;
		//
		public int locationID;
		// The simulators handle the agent teleported to
		public long regionHandle;
		// A URI which contains a list of Capabilities the simulator supports
		public URI seedCapability;
		// Indicates the level of access required to access the simulator, or
		// the content rating, or the simulators map status
		public SimAccess simAccess;
		// The IP Address of the simulator
		// TODO:FIXME
		// Let's keep everything the same, so rename ip to address
		public InetAddress ip;
		// The UDP Port the simulator will listen for UDP traffic on
		public int port;
		// Status flags indicating the state of the Agent upon arrival, Flying,
		// etc.
		public int flags;

		@Override
		public CapsEventType getType() {
			return CapsEventType.TeleportFinish;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);

			OSDArray infoArray = new OSDArray(1);

			OSDMap info = new OSDMap(8);
			info.put("AgentID", OSD.fromUUID(agentID));
			info.put("LocationID", OSD.fromInteger(locationID)); // Unused by
																	// the
																	// client
			info.put("RegionHandle", OSD.fromULong(regionHandle));
			info.put("SeedCapability", OSD.fromUri(seedCapability));
			info.put("SimAccess", OSD.fromInteger(simAccess.getValue()));
			info.put("SimIP", OSD.fromBinary(ip.getAddress()));
			info.put("SimPort", OSD.fromInteger(port));
			info.put("TeleportFlags", OSD.fromInteger(TeleportFlags.getValue(flags)));

			infoArray.add(info);

			map.put("Info", infoArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDArray array = (OSDArray) map.get("Info");
			OSDMap blockMap = (OSDMap) array.get(0);

			agentID = blockMap.get("AgentID").asUUID();
			locationID = blockMap.get("LocationID").asInteger();
			regionHandle = blockMap.get("RegionHandle").asULong();
			seedCapability = blockMap.get("SeedCapability").asUri();
			simAccess = libomv.model.simulator.SimAccess.setValue(blockMap.get("SimAccess").asInteger());
			ip = blockMap.get("SimIP").asInetAddress();
			port = blockMap.get("SimPort").asInteger();
			flags = TeleportFlags.setValue(blockMap.get("TeleportFlags").asUInteger());
		}
	}

	// Sent to the viewer when a neighboring simulator is requesting the agent
	// make a connection to it.
	public class EstablishAgentCommunicationMessage implements IMessage {
		public UUID agentID;
		public InetAddress address;
		public int port;
		public URI seedCapability;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.EstablishAgentCommunication;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);
			map.put("agent-id", OSD.fromUUID(agentID));
			map.put("sim-ip-and-port", OSD.fromString(String.format("%s:%d", address.getHostAddress(), port)));
			map.put("seed-capability", OSD.fromUri(seedCapability));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			String ipAndPort = map.get("sim-ip-and-port").asString();
			int i = ipAndPort.indexOf(':');

			agentID = map.get("agent-id").asUUID();
			port = 2345; // FIXME: What default port should we use?
			try {
				if (i >= 0) {
					address = InetAddress.getByName(ipAndPort.substring(0, i));
					port = Integer.valueOf(ipAndPort.substring(i + 1));
				} else {
					address = InetAddress.getByName(ipAndPort);
				}
			} catch (UnknownHostException e) {
				address = null;
			}
			seedCapability = map.get("seed-capability").asUri();
		}
	}

	public class CrossedRegionMessage implements IMessage {
		public Vector3 lookAt;
		public Vector3 position;
		public UUID agentID;
		public UUID sessionID;
		public long regionHandle;
		public URI seedCapability;
		public InetAddress ip;
		public int port;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.CrossedRegion;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);

			OSDArray infoArray = new OSDArray(1);
			OSDMap infoMap = new OSDMap(2);
			infoMap.put("LookAt", OSD.fromVector3(lookAt));
			infoMap.put("Position", OSD.fromVector3(position));
			infoArray.add(infoMap);
			map.put("Info", infoArray);

			OSDArray agentDataArray = new OSDArray(1);
			OSDMap agentDataMap = new OSDMap(2);
			agentDataMap.put("AgentID", OSD.fromUUID(agentID));
			agentDataMap.put("SessionID", OSD.fromUUID(sessionID));
			agentDataArray.add(agentDataMap);
			map.put("AgentData", agentDataArray);

			OSDArray regionDataArray = new OSDArray(1);
			OSDMap regionDataMap = new OSDMap(4);
			regionDataMap.put("RegionHandle", OSD.fromULong(regionHandle));
			regionDataMap.put("SeedCapability", OSD.fromUri(seedCapability));
			regionDataMap.put("SimIP", OSD.fromBinary(ip.getAddress()));
			regionDataMap.put("SimPort", OSD.fromInteger(port));
			regionDataArray.add(regionDataMap);
			map.put("RegionData", regionDataArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDMap infoMap = (OSDMap) ((OSDArray) map.get("Info")).get(0);
			lookAt = infoMap.get("LookAt").asVector3();
			position = infoMap.get("Position").asVector3();

			OSDMap agentDataMap = (OSDMap) ((OSDArray) map.get("AgentData")).get(0);
			agentID = agentDataMap.get("AgentID").asUUID();
			sessionID = agentDataMap.get("SessionID").asUUID();

			OSDMap regionDataMap = (OSDMap) ((OSDArray) map.get("RegionData")).get(0);
			regionHandle = regionDataMap.get("RegionHandle").asULong();
			seedCapability = regionDataMap.get("SeedCapability").asUri();
			ip = regionDataMap.get("SimIP").asInetAddress();
			port = regionDataMap.get("SimPort").asInteger();
		}
	}

	public class EnableSimulatorMessage implements IMessage {
		public class SimulatorInfoBlock {
			public long regionHandle;
			public InetAddress ip;
			public int port;
		}

		public SimulatorInfoBlock[] simulators;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.EnableSimulator;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);

			OSDArray array = new OSDArray(simulators.length);
			for (int i = 0; i < simulators.length; i++) {
				SimulatorInfoBlock block = simulators[i];

				OSDMap blockMap = new OSDMap(3);
				blockMap.put("Handle", OSD.fromULong(block.regionHandle));
				blockMap.put("IP", OSD.fromBinary(block.ip));
				blockMap.put("Port", OSD.fromInteger(block.port));
				array.add(blockMap);
			}

			map.put("SimulatorInfo", array);
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDArray array = (OSDArray) map.get("SimulatorInfo");
			simulators = new SimulatorInfoBlock[array.size()];

			for (int i = 0; i < array.size(); i++) {
				OSDMap blockMap = (OSDMap) array.get(i);

				SimulatorInfoBlock block = new SimulatorInfoBlock();
				block.regionHandle = blockMap.get("Handle").asULong();
				block.ip = blockMap.get("IP").asInetAddress();
				block.port = blockMap.get("Port").asInteger();
				simulators[i] = block;
			}
		}
	}

	// A message sent to the client which indicates a teleport request has
	// failed
	// and contains some information on why it failed
	public class TeleportFailedMessage implements IMessage {
		//
		public String extraParams;
		// A string key of the reason the teleport failed e.g. CouldntTPCloser
		// Which could be used to look up a value in a dictionary or enum
		public String messageKey;
		// The <see cref="UUID"/> of the Agent
		public UUID agentID;
		// A string human readable message containing the reason
		// An example: Could not teleport closer to destination
		public String reason;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.TeleportFailed;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);

			OSDMap alertInfoMap = new OSDMap(2);

			alertInfoMap.put("ExtraParams", OSD.fromString(extraParams));
			alertInfoMap.put("Message", OSD.fromString(messageKey));
			OSDArray alertArray = new OSDArray();
			alertArray.add(alertInfoMap);
			map.put("AlertInfo", alertArray);

			OSDMap infoMap = new OSDMap(2);
			infoMap.put("AgentID", OSD.fromUUID(agentID));
			infoMap.put("Reason", OSD.fromString(reason));
			OSDArray infoArray = new OSDArray();
			infoArray.add(infoMap);
			map.put("Info", infoArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {

			OSDArray alertInfoArray = (OSDArray) map.get("AlertInfo");

			OSDMap alertInfoMap = (OSDMap) alertInfoArray.get(0);
			extraParams = alertInfoMap.get("ExtraParams").asString();
			messageKey = alertInfoMap.get("Message").asString();

			OSDArray infoArray = (OSDArray) map.get("Info");
			OSDMap infoMap = (OSDMap) infoArray.get(0);
			agentID = infoMap.get("AgentID").asUUID();
			reason = infoMap.get("Reason").asString();
		}
	}

	public class LandStatReplyMessage implements IMessage {
		public int reportType;
		public int requestFlags;
		public int totalObjectCount;

		public class ReportDataBlock {
			public Vector3 location;
			public String ownerName;
			public float score;
			public UUID taskID;
			public int taskLocalID;
			public String taskName;
			public float monoScore;
			public Date timeStamp;
		}

		public ReportDataBlock[] reportDataBlocks;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.LandStatReply;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);

			OSDMap requestDataMap = new OSDMap(3);
			requestDataMap.put("ReportType", OSD.fromUInteger(this.reportType));
			requestDataMap.put("RequestFlags", OSD.fromUInteger(this.requestFlags));
			requestDataMap.put("TotalObjectCount", OSD.fromUInteger(this.totalObjectCount));

			OSDArray requestDatArray = new OSDArray();
			requestDatArray.add(requestDataMap);
			map.put("RequestData", requestDatArray);

			OSDArray reportDataArray = new OSDArray();
			OSDArray dataExtendedArray = new OSDArray();
			for (int i = 0; i < reportDataBlocks.length; i++) {
				OSDMap reportMap = new OSDMap(8);
				reportMap.put("LocationX", OSD.fromReal(reportDataBlocks[i].location.x));
				reportMap.put("LocationY", OSD.fromReal(reportDataBlocks[i].location.y));
				reportMap.put("LocationZ", OSD.fromReal(reportDataBlocks[i].location.z));
				reportMap.put("OwnerName", OSD.fromString(reportDataBlocks[i].ownerName));
				reportMap.put("Score", OSD.fromReal(reportDataBlocks[i].score));
				reportMap.put("TaskID", OSD.fromUUID(reportDataBlocks[i].taskID));
				reportMap.put("TaskLocalID", OSD.fromReal(reportDataBlocks[i].taskLocalID));
				reportMap.put("TaskName", OSD.fromString(reportDataBlocks[i].taskName));
				reportDataArray.add(reportMap);

				OSDMap extendedMap = new OSDMap(2);
				extendedMap.put("MonoScore", OSD.fromReal(reportDataBlocks[i].monoScore));
				extendedMap.put("TimeStamp", OSD.fromDate(reportDataBlocks[i].timeStamp));
				dataExtendedArray.add(extendedMap);
			}

			map.put("ReportData", reportDataArray);
			map.put("DataExtended", dataExtendedArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {

			OSDArray requestDataArray = (OSDArray) map.get("RequestData");
			OSDMap requestMap = (OSDMap) requestDataArray.get(0);

			this.reportType = requestMap.get("ReportType").asUInteger();
			this.requestFlags = requestMap.get("RequestFlags").asUInteger();
			this.totalObjectCount = requestMap.get("TotalObjectCount").asUInteger();

			if (totalObjectCount < 1) {
				reportDataBlocks = new ReportDataBlock[0];
				return;
			}

			OSDArray dataArray = (OSDArray) map.get("ReportData");
			OSDArray dataExtendedArray = (OSDArray) map.get("DataExtended");

			reportDataBlocks = new ReportDataBlock[dataArray.size()];
			for (int i = 0; i < dataArray.size(); i++) {
				OSDMap blockMap = (OSDMap) dataArray.get(i);
				OSDMap extMap = (OSDMap) dataExtendedArray.get(i);
				ReportDataBlock block = new ReportDataBlock();
				block.location = new Vector3((float) blockMap.get("LocationX").asReal(),
						(float) blockMap.get("LocationY").asReal(), (float) blockMap.get("LocationZ").asReal());
				block.ownerName = blockMap.get("OwnerName").asString();
				block.score = (float) blockMap.get("Score").asReal();
				block.taskID = blockMap.get("TaskID").asUUID();
				block.taskLocalID = blockMap.get("TaskLocalID").asUInteger();
				block.taskName = blockMap.get("TaskName").asString();
				block.monoScore = (float) extMap.get("MonoScore").asReal();
				block.timeStamp = Helpers.unixTimeToDateTime(extMap.get("TimeStamp").asUInteger());

				reportDataBlocks[i] = block;
			}
		}
	}

	// #region Parcel Messages

	/**
	 * Contains a list of prim owner information for a specific parcel in a
	 * simulator
	 *
	 * A Simulator will always return at least 1 entry If agent does not have proper
	 * permission the OwnerID will be UUID.Zero If agent does not have proper
	 * permission OR there are no primitives on parcel the DataBlocksExtended map
	 * will not be sent from the simulator
	 */
	public class ParcelObjectOwnersReplyMessage implements IMessage {
		// Prim ownership information for a specified owner on a single parcel
		public class PrimOwner {
			// The <see cref="UUID"/> of the prim owner,
			// UUID.Zero if agent has no permission to view prim owner
			// information
			public UUID ownerID;
			// The total number of prims
			public int count;
			// True if the OwnerID is a <see cref="Group"/>
			public boolean isGroupOwned;
			// True if the owner is online
			// This is no longer used by the LL Simulators
			public boolean onlineStatus;
			// The date the most recent prim was rezzed
			public Date timeStamp;
		}

		// An Array of <see cref="PrimOwner"/> objects
		public PrimOwner[] primOwnersBlock;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ParcelObjectOwnersReply;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDArray dataArray = new OSDArray(primOwnersBlock.length);
			OSDArray dataExtendedArray = new OSDArray();

			for (int i = 0; i < primOwnersBlock.length; i++) {
				OSDMap dataMap = new OSDMap(4);
				dataMap.put("OwnerID", OSD.fromUUID(primOwnersBlock[i].ownerID));
				dataMap.put("Count", OSD.fromInteger(primOwnersBlock[i].count));
				dataMap.put("IsGroupOwned", OSD.fromBoolean(primOwnersBlock[i].isGroupOwned));
				dataMap.put("OnlineStatus", OSD.fromBoolean(primOwnersBlock[i].onlineStatus));
				dataArray.add(dataMap);

				OSDMap dataExtendedMap = new OSDMap(1);
				dataExtendedMap.put("TimeStamp", OSD.fromDate(primOwnersBlock[i].timeStamp));
				dataExtendedArray.add(dataExtendedMap);
			}

			OSDMap map = new OSDMap();
			map.put("Data", dataArray);
			if (dataExtendedArray.size() > 0)
				map.put("DataExtended", dataExtendedArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDArray dataArray = (OSDArray) map.get("Data");

			// DataExtended is optional, will not exist of parcel contains zero
			// prims
			OSDArray dataExtendedArray;
			if (map.containsKey("DataExtended")) {
				dataExtendedArray = (OSDArray) map.get("DataExtended");
			} else {
				dataExtendedArray = new OSDArray();
			}

			primOwnersBlock = new PrimOwner[dataArray.size()];

			for (int i = 0; i < dataArray.size(); i++) {
				OSDMap dataMap = (OSDMap) dataArray.get(i);
				PrimOwner block = new PrimOwner();
				block.ownerID = dataMap.get("OwnerID").asUUID();
				block.count = dataMap.get("Count").asInteger();
				block.isGroupOwned = dataMap.get("IsGroupOwned").asBoolean();
				block.onlineStatus = dataMap.get("OnlineStatus").asBoolean(); // deprecated

				// if the agent has no permissions, or there are no prims, the
				// counts
				// should not match up, so we don't decode the DataExtended map
				if (dataExtendedArray.size() == dataArray.size()) {
					OSDMap dataExtendedMap = (OSDMap) dataExtendedArray.get(i);
					block.timeStamp = Helpers.unixTimeToDateTime(dataExtendedMap.get("TimeStamp").asUInteger());
				}
				primOwnersBlock[i] = block;
			}
		}
	}

	// The details of a single parcel in a region, also contains some regionwide
	// globals
	public class ParcelPropertiesMessage implements IMessage {
		// Simulator-local ID of this parcel
		public int localID;
		// Maximum corner of the axis-aligned bounding box for this parcel
		public Vector3 aabbMax;
		// Minimum corner of the axis-aligned bounding box for this parcel
		public Vector3 aabbMin;
		// Total parcel land area
		public int area;
		//
		public int auctionID;
		// Key of authorized buyer
		public UUID authBuyerID;
		// Bitmap describing land layout in 4x4m squares across the entire
		// region
		public byte[] bitmap;
		//
		public ParcelCategory category;
		// Date land was claimed
		public Date claimDate;
		// Appears to always be zero
		public int claimPrice;
		// Parcel Description
		public String desc;
		//
		public int parcelFlags;
		//
		public UUID groupID;
		// Total number of primitives owned by the parcel group on this parcel
		public int groupPrims;
		// Whether the land is deeded to a group or not
		public boolean isGroupOwned;
		//
		public LandingTypeEnum landingType;
		// Maximum number of primitives this parcel supports
		public int maxPrims;
		// The Asset UUID of the Texture which when applied to a primitive will
		// display the media
		public UUID mediaID;
		// A URL which points to any Quicktime supported media type
		public String mediaURL;
		// A byte, if 0x1 viewer should auto scale media to fit object
		public boolean mediaAutoScale;
		// URL For Music Stream
		public String musicURL;
		// Parcel Name
		public String name;
		// Autoreturn value in minutes for others' objects
		public int otherCleanTime;
		//
		public int otherCount;
		// Total number of other primitives on this parcel
		public int otherPrims;
		// UUID of the owner of this parcel
		public UUID ownerID;
		// Total number of primitives owned by the parcel owner on this parcel
		public int ownerPrims;
		//
		public float parcelPrimBonus;
		// How long is pass valid for
		public float passHours;
		// Price for a temporary pass
		public int passPrice;
		//
		public int publicCount;
		// Disallows people outside the parcel from being able to see in
		public boolean privacy;
		//
		public boolean regionDenyAnonymous;
		//
		public boolean regionPushOverride;
		// This field is no longer used
		public int rentPrice;
		// The result of a request for parcel properties
		public ParcelResult requestResult;
		// Sale price of the parcel, only useful if ForSale is set
		// The SalePrice will remain the same after an ownership transfer
		// (sale), so it can be used to see the purchase
		// price after a sale if the new owner has not changed it
		public int salePrice;
		// Number of primitives your avatar is currently selecting and sitting
		// on in this parcel
		public int selectedPrims;
		//
		public int selfCount;
		// A number which increments by 1, starting at 0 for each
		// ParcelProperties request.
		// Can be overriden by specifying the sequenceID with the
		// ParcelPropertiesRequest being sent.
		// a Negative number indicates the action in {@link
		// ParcelPropertiesStatus} has occurred.
		public int sequenceID;
		// Maximum primitives across the entire simulator
		public int simWideMaxPrims;
		// Total primitives across the entire simulator
		public int simWideTotalPrims;
		//
		public boolean snapSelection;
		// Key of parcel snapshot
		public UUID snapshotID;
		// Parcel ownership status
		public ParcelStatus status;
		// Total number of primitives on this parcel
		public int totalPrims;
		//
		public Vector3 userLocation;
		//
		public Vector3 userLookAt;
		// TRUE of region denies access to age unverified users
		public boolean regionDenyAgeUnverified;
		// A description of the media
		public String mediaDesc;
		// An Integer which represents the height of the media
		public int mediaHeight;
		// An integer which represents the width of the media
		public int mediaWidth;
		// A boolean, if true the viewer should loop the media
		public boolean mediaLoop;
		// A string which contains the mime type of the media
		public String mediaType;
		// true to obscure (hide) media url
		public boolean obscureMedia;
		// true to obscure (hide) music url
		public boolean obscureMusic;
		// true if avatars in this parcel should be invisible to people outside
		public boolean seeAVs;
		// true if avatars outside can hear any sounds avatars inside play
		public boolean anyAVSounds;
		// true if group members outside can hear any sounds avatars inside play
		public boolean groupAVSounds;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ParcelProperties;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);

			OSDArray dataArray = new OSDArray(1);
			OSDMap parcelDataMap = new OSDMap(47);
			parcelDataMap.put("LocalID", OSD.fromInteger(localID));
			parcelDataMap.put("AABBMax", OSD.fromVector3(aabbMax));
			parcelDataMap.put("AABBMin", OSD.fromVector3(aabbMin));
			parcelDataMap.put("Area", OSD.fromInteger(area));
			parcelDataMap.put("AuctionID", OSD.fromInteger(auctionID));
			parcelDataMap.put("AuthBuyerID", OSD.fromUUID(authBuyerID));
			parcelDataMap.put("Bitmap", OSD.fromBinary(bitmap));
			parcelDataMap.put("Category", OSD.fromInteger(category.getValue()));
			parcelDataMap.put("ClaimDate", OSD.fromDate(claimDate));
			parcelDataMap.put("ClaimPrice", OSD.fromInteger(claimPrice));
			parcelDataMap.put("Desc", OSD.fromString(desc));
			parcelDataMap.put("ParcelFlags", OSD.fromUInteger(parcelFlags));
			parcelDataMap.put("GroupID", OSD.fromUUID(groupID));
			parcelDataMap.put("GroupPrims", OSD.fromInteger(groupPrims));
			parcelDataMap.put("IsGroupOwned", OSD.fromBoolean(isGroupOwned));
			parcelDataMap.put("LandingType", OSD.fromInteger(landingType.getValue()));
			parcelDataMap.put("MaxPrims", OSD.fromInteger(maxPrims));
			parcelDataMap.put("MediaID", OSD.fromUUID(mediaID));
			parcelDataMap.put("MediaURL", OSD.fromString(mediaURL));
			parcelDataMap.put("MediaAutoScale", OSD.fromBoolean(mediaAutoScale));
			parcelDataMap.put("MusicURL", OSD.fromString(musicURL));
			parcelDataMap.put("Name", OSD.fromString(name));
			parcelDataMap.put("OtherCleanTime", OSD.fromInteger(otherCleanTime));
			parcelDataMap.put("OtherCount", OSD.fromInteger(otherCount));
			parcelDataMap.put("OtherPrims", OSD.fromInteger(otherPrims));
			parcelDataMap.put("OwnerID", OSD.fromUUID(ownerID));
			parcelDataMap.put("OwnerPrims", OSD.fromInteger(ownerPrims));
			parcelDataMap.put("ParcelPrimBonus", OSD.fromReal(parcelPrimBonus));
			parcelDataMap.put("PassHours", OSD.fromReal(passHours));
			parcelDataMap.put("PassPrice", OSD.fromInteger(passPrice));
			parcelDataMap.put("PublicCount", OSD.fromInteger(publicCount));
			parcelDataMap.put("Privacy", OSD.fromBoolean(privacy));
			parcelDataMap.put("RegionDenyAnonymous", OSD.fromBoolean(regionDenyAnonymous));
			parcelDataMap.put("RegionPushOverride", OSD.fromBoolean(regionPushOverride));
			parcelDataMap.put("RentPrice", OSD.fromInteger(rentPrice));
			parcelDataMap.put("RequestResult", OSD.fromInteger(requestResult.getValue()));
			parcelDataMap.put("SalePrice", OSD.fromInteger(salePrice));
			parcelDataMap.put("SelectedPrims", OSD.fromInteger(selectedPrims));
			parcelDataMap.put("SelfCount", OSD.fromInteger(selfCount));
			parcelDataMap.put("SequenceID", OSD.fromInteger(sequenceID));
			parcelDataMap.put("SimWideMaxPrims", OSD.fromInteger(simWideMaxPrims));
			parcelDataMap.put("SimWideTotalPrims", OSD.fromInteger(simWideTotalPrims));
			parcelDataMap.put("SnapSelection", OSD.fromBoolean(snapSelection));
			parcelDataMap.put("SnapshotID", OSD.fromUUID(snapshotID));
			parcelDataMap.put("Status", OSD.fromInteger(status.getValue()));
			parcelDataMap.put("TotalPrims", OSD.fromInteger(totalPrims));
			parcelDataMap.put("UserLocation", OSD.fromVector3(userLocation));
			parcelDataMap.put("UserLookAt", OSD.fromVector3(userLookAt));
			parcelDataMap.put("SeeAVs", OSD.fromBoolean(seeAVs));
			parcelDataMap.put("AnyAVSounds", OSD.fromBoolean(anyAVSounds));
			parcelDataMap.put("GroupAVSounds", OSD.fromBoolean(groupAVSounds));
			dataArray.add(parcelDataMap);
			map.put("ParcelData", dataArray);

			OSDArray mediaDataArray = new OSDArray(1);
			OSDMap mediaDataMap = new OSDMap(7);
			mediaDataMap.put("MediaDesc", OSD.fromString(mediaDesc));
			mediaDataMap.put("MediaHeight", OSD.fromInteger(mediaHeight));
			mediaDataMap.put("MediaWidth", OSD.fromInteger(mediaWidth));
			mediaDataMap.put("MediaLoop", OSD.fromBoolean(mediaLoop));
			mediaDataMap.put("MediaType", OSD.fromString(mediaType));
			mediaDataMap.put("ObscureMedia", OSD.fromBoolean(obscureMedia));
			mediaDataMap.put("ObscureMusic", OSD.fromBoolean(obscureMusic));
			mediaDataArray.add(mediaDataMap);
			map.put("MediaData", mediaDataArray);

			OSDArray ageVerificationBlockArray = new OSDArray(1);
			OSDMap ageVerificationBlockMap = new OSDMap(1);
			ageVerificationBlockMap.put("RegionDenyAgeUnverified", OSD.fromBoolean(regionDenyAgeUnverified));
			ageVerificationBlockArray.add(ageVerificationBlockMap);
			map.put("AgeVerificationBlock", ageVerificationBlockArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDMap parcelDataMap = (OSDMap) ((OSDArray) map.get("ParcelData")).get(0);
			localID = parcelDataMap.get("LocalID").asInteger();
			aabbMax = parcelDataMap.get("AABBMax").asVector3();
			aabbMin = parcelDataMap.get("AABBMin").asVector3();
			area = parcelDataMap.get("Area").asInteger();
			auctionID = parcelDataMap.get("AuctionID").asInteger();
			authBuyerID = parcelDataMap.get("AuthBuyerID").asUUID();
			bitmap = parcelDataMap.get("Bitmap").asBinary();
			category = ParcelCategory.setValue(parcelDataMap.get("Category").asInteger());
			claimDate = Helpers.unixTimeToDateTime(parcelDataMap.get("ClaimDate").asInteger());
			claimPrice = parcelDataMap.get("ClaimPrice").asInteger();
			desc = parcelDataMap.get("Desc").asString();

			// LL sends this as binary, we'll convert it here
			if (parcelDataMap.get("ParcelFlags").getType() == OSDType.Binary) {
				byte[] bytes = parcelDataMap.get("ParcelFlags").asBinary();
				parcelFlags = ParcelFlags.getValue((int) Helpers.bytesToUInt32B(bytes));
			} else {
				parcelFlags = ParcelFlags.getValue(parcelDataMap.get("ParcelFlags").asUInteger());
			}
			groupID = parcelDataMap.get("GroupID").asUUID();
			groupPrims = parcelDataMap.get("GroupPrims").asInteger();
			isGroupOwned = parcelDataMap.get("IsGroupOwned").asBoolean();
			landingType = LandingTypeEnum.setValue(parcelDataMap.get("LandingType").asInteger());
			maxPrims = parcelDataMap.get("MaxPrims").asInteger();
			mediaID = parcelDataMap.get("MediaID").asUUID();
			mediaURL = parcelDataMap.get("MediaURL").asString();
			mediaAutoScale = parcelDataMap.get("MediaAutoScale").asBoolean(); // 0x1
																				// =
																				// yes
			musicURL = parcelDataMap.get("MusicURL").asString();
			name = parcelDataMap.get("Name").asString();
			otherCleanTime = parcelDataMap.get("OtherCleanTime").asInteger();
			otherCount = parcelDataMap.get("OtherCount").asInteger();
			otherPrims = parcelDataMap.get("OtherPrims").asInteger();
			ownerID = parcelDataMap.get("OwnerID").asUUID();
			ownerPrims = parcelDataMap.get("OwnerPrims").asInteger();
			parcelPrimBonus = (float) parcelDataMap.get("ParcelPrimBonus").asReal();
			passHours = (float) parcelDataMap.get("PassHours").asReal();
			passPrice = parcelDataMap.get("PassPrice").asInteger();
			publicCount = parcelDataMap.get("PublicCount").asInteger();
			privacy = parcelDataMap.get("Privacy").asBoolean();
			regionDenyAnonymous = parcelDataMap.get("RegionDenyAnonymous").asBoolean();
			regionPushOverride = parcelDataMap.get("RegionPushOverride").asBoolean();
			rentPrice = parcelDataMap.get("RentPrice").asInteger();
			requestResult = ParcelResult.setValue(parcelDataMap.get("RequestResult").asInteger());
			salePrice = parcelDataMap.get("SalePrice").asInteger();
			selectedPrims = parcelDataMap.get("SelectedPrims").asInteger();
			selfCount = parcelDataMap.get("SelfCount").asInteger();
			sequenceID = parcelDataMap.get("SequenceID").asInteger();
			simWideMaxPrims = parcelDataMap.get("SimWideMaxPrims").asInteger();
			simWideTotalPrims = parcelDataMap.get("SimWideTotalPrims").asInteger();
			snapSelection = parcelDataMap.get("SnapSelection").asBoolean();
			snapshotID = parcelDataMap.get("SnapshotID").asUUID();
			status = ParcelStatus.setValue(parcelDataMap.get("Status").asInteger());
			totalPrims = parcelDataMap.get("TotalPrims").asInteger();
			userLocation = parcelDataMap.get("UserLocation").asVector3();
			userLookAt = parcelDataMap.get("UserLookAt").asVector3();
			seeAVs = parcelDataMap.get("SeeAVs").asBoolean();
			anyAVSounds = parcelDataMap.get("AnyAVSounds").asBoolean();
			groupAVSounds = parcelDataMap.get("GroupAVSounds").asBoolean();

			if (map.containsKey("MediaData")) // temporary, OpenSim doesn't send
												// this block
			{
				OSDMap mediaDataMap = (OSDMap) ((OSDArray) map.get("MediaData")).get(0);
				mediaDesc = mediaDataMap.get("MediaDesc").asString();
				mediaHeight = mediaDataMap.get("MediaHeight").asInteger();
				mediaWidth = mediaDataMap.get("MediaWidth").asInteger();
				mediaLoop = mediaDataMap.get("MediaLoop").asBoolean();
				mediaType = mediaDataMap.get("MediaType").asString();
				obscureMedia = mediaDataMap.get("ObscureMedia").asBoolean();
				obscureMusic = mediaDataMap.get("ObscureMusic").asBoolean();
			}

			OSDMap ageVerificationBlockMap = (OSDMap) ((OSDArray) map.get("AgeVerificationBlock")).get(0);
			regionDenyAgeUnverified = ageVerificationBlockMap.get("RegionDenyAgeUnverified").asBoolean();
		}
	}

	// A message sent from the viewer to the simulator to updated a specific
	// parcels settings
	public class ParcelPropertiesUpdateMessage implements IMessage {
		// The {@link UUID} of the agent authorized to purchase this parcel of
		// land or
		// a NULL {@link UUID} if the sale is authorized to anyone
		public UUID authBuyerID;
		// true to enable auto scaling of the parcel media
		public boolean mediaAutoScale;
		// The category of this parcel used when search is enabled to restrict
		// search results
		public ParcelCategory category;
		// A string containing the description to set
		public String desc;
		// The {@link UUID} of the {@link Group} which allows for additional
		// powers and restrictions.
		public UUID groupID;
		// The {@link LandingType} which specifies how avatars which teleport to
		// this parcel are handled
		public LandingTypeEnum landingType;
		// The LocalID of the parcel to update settings on
		public int localID;
		// A string containing the description of the media which can be played
		// to visitors
		public String mediaDesc;
		//
		public int mediaHeight;
		//
		public boolean mediaLoop;
		//
		public UUID mediaID;
		//
		public String mediaType;
		//
		public String mediaURL;
		//
		public int mediaWidth;
		//
		public String musicURL;
		//
		public String name;
		//
		public boolean obscureMedia;
		//
		public boolean obscureMusic;
		//
		public int parcelFlags;
		//
		public float passHours;
		//
		public int passPrice;
		//
		public boolean privacy;
		//
		public int salePrice;
		//
		public UUID snapshotID;
		//
		public Vector3 userLocation;
		//
		public Vector3 userLookAt;
		// true if avatars in this parcel should be invisible to people outside
		public boolean seeAVs;
		// true if avatars outside can hear any sounds avatars inside play
		public boolean anyAVSounds;
		// true if group members outside can hear any sounds avatars inside play
		public boolean groupAVSounds;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ParcelPropertiesUpdate;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap();
			map.put("auth_buyer_id", OSD.fromUUID(authBuyerID));
			map.put("auto_scale", OSD.fromBoolean(mediaAutoScale));
			map.put("category", OSD.fromInteger(category.getValue()));
			map.put("description", OSD.fromString(desc));
			map.put("flags", OSD.fromBinary(Helpers.EmptyBytes));
			map.put("group_id", OSD.fromUUID(groupID));
			map.put("landing_type", OSD.fromInteger(landingType.getValue()));
			map.put("local_id", OSD.fromInteger(localID));
			map.put("media_desc", OSD.fromString(mediaDesc));
			map.put("media_height", OSD.fromInteger(mediaHeight));
			map.put("media_id", OSD.fromUUID(mediaID));
			map.put("media_loop", OSD.fromBoolean(mediaLoop));
			map.put("media_type", OSD.fromString(mediaType));
			map.put("media_url", OSD.fromString(mediaURL));
			map.put("media_width", OSD.fromInteger(mediaWidth));
			map.put("music_url", OSD.fromString(musicURL));
			map.put("name", OSD.fromString(name));
			map.put("obscure_media", OSD.fromBoolean(obscureMedia));
			map.put("obscure_music", OSD.fromBoolean(obscureMusic));
			map.put("parcel_flags", OSD.fromUInteger(parcelFlags));
			map.put("pass_hours", OSD.fromReal(passHours));
			map.put("pass_price", OSD.fromInteger(passPrice));
			map.put("privacy", OSD.fromBoolean(privacy));
			map.put("sale_price", OSD.fromInteger(salePrice));
			map.put("snapshot_id", OSD.fromUUID(snapshotID));
			map.put("user_location", OSD.fromVector3(userLocation));
			map.put("user_look_at", OSD.fromVector3(userLookAt));
			map.put("see_avs", OSD.fromBoolean(seeAVs));
			map.put("any_av_sounds", OSD.fromBoolean(anyAVSounds));
			map.put("group_av_sounds", OSD.fromBoolean(groupAVSounds));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			authBuyerID = map.get("auth_buyer_id").asUUID();
			mediaAutoScale = map.get("auto_scale").asBoolean();
			category = ParcelCategory.setValue(map.get("category").asInteger());
			desc = map.get("description").asString();
			groupID = map.get("group_id").asUUID();
			landingType = LandingTypeEnum.setValue(map.get("landing_type").asUInteger());
			localID = map.get("local_id").asInteger();
			mediaDesc = map.get("media_desc").asString();
			mediaHeight = map.get("media_height").asInteger();
			mediaLoop = map.get("media_loop").asBoolean();
			mediaID = map.get("media_id").asUUID();
			mediaType = map.get("media_type").asString();
			mediaURL = map.get("media_url").asString();
			mediaWidth = map.get("media_width").asInteger();
			musicURL = map.get("music_url").asString();
			name = map.get("name").asString();
			obscureMedia = map.get("obscure_media").asBoolean();
			obscureMusic = map.get("obscure_music").asBoolean();
			parcelFlags = ParcelFlags.setValue((map.get("parcel_flags").asUInteger()));
			passHours = (float) map.get("pass_hours").asReal();
			passPrice = map.get("pass_price").asUInteger();
			privacy = map.get("privacy").asBoolean();
			salePrice = map.get("sale_price").asUInteger();
			snapshotID = map.get("snapshot_id").asUUID();
			userLocation = map.get("user_location").asVector3();
			userLookAt = map.get("user_look_at").asVector3();
			if (map.containsKey("see_avs")) {
				seeAVs = map.get("see_avs").asBoolean();
				anyAVSounds = map.get("any_av_sounds").asBoolean();
				groupAVSounds = map.get("group_av_sounds").asBoolean();
			} else {
				seeAVs = true;
				anyAVSounds = true;
				groupAVSounds = true;
			}
		}
	}

	// TODO: FIXME
	// Why does this class exist? It doesn't seem to be used or very useful
	// Base class used for the RemoteParcelRequest message
	public abstract class RemoteParcelRequestBlock implements IMessage {
		public abstract OSDMap serialize();

		public abstract void deserialize(OSDMap map);
	}

	// A message sent from the viewer to the simulator to request information on
	// a remote parcel
	public class RemoteParcelRequestRequest extends RemoteParcelRequestBlock {
		// Local sim position of the parcel we are looking up
		public Vector3 location;
		// Region handle of the parcel we are looking up
		public long regionHandle;
		// Region <see cref="UUID"/> of the parcel we are looking up
		public UUID regionID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.RemoteParcelRequestRequest;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);
			map.put("location", OSD.fromVector3(location));
			map.put("region_handle", OSD.fromULong(regionHandle));
			map.put("region_id", OSD.fromUUID(regionID));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			location = map.get("location").asVector3();
			regionHandle = map.get("region_handle").asULong();
			regionID = map.get("region_id").asUUID();
		}
	}

	// A message sent from the simulator to the viewer in response to a <see
	// cref="RemoteParcelRequestRequest"/>
	public class RemoteParcelRequestReply extends RemoteParcelRequestBlock {
		// The grid-wide unique parcel ID
		public UUID parcelID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.RemoteParcelRequestReply;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);
			map.put("parcel_id", OSD.fromUUID(parcelID));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map == null || !map.containsKey("parcel_id"))
				parcelID = UUID.ZERO;
			else
				parcelID = map.get("parcel_id").asUUID();
		}
	}

	// A message containing a request for a remote parcel from a viewer, or a
	// response from the simulator to that request
	public class RemoteParcelRequestMessage implements IMessage {
		// The request or response details block
		public RemoteParcelRequestBlock request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.RemoteParcelRequest;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return request.serialize();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("parcel_id")) {
				request = new RemoteParcelRequestReply();
				request.deserialize(map);
			} else if (map.containsKey("location")) {
				request = new RemoteParcelRequestRequest();
				request.deserialize(map);
			} else
				logger.warn("Unable to deserialize RemoteParcelRequest: No message handler exists for method: "
						+ map.asString());
		}
	}

	// #endregion

	// #region Inventory Messages

	public class NewFileAgentInventoryMessage implements IMessage {
		public UUID folderID;
		public AssetType assetType;
		public InventoryType inventoryType;
		public String name;
		public String description;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.NewFileAgentInventory;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(5);
			map.put("folder_id", OSD.fromUUID(folderID));
			map.put("asset_type", OSD.fromString(assetType.toString()));
			map.put("inventory_type", OSD.fromString(inventoryType.toString()));
			map.put("name", OSD.fromString(name));
			map.put("description", OSD.fromString(description));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			folderID = map.get("folder_id").asUUID();
			assetType = AssetType.setValue(map.get("asset_type").asString());
			inventoryType = InventoryItem.InventoryType.setValue(map.get("inventory_type").asString());
			name = map.get("name").asString();
			description = map.get("description").asString();
		}
	}

	public class BulkUpdateInventoryMessage implements IMessage {
		public class FolderDataInfo {
			public UUID folderID;
			public UUID parentID;
			public String name;
			public FolderType type;

			public FolderDataInfo(OSDMap map) {
				folderID = map.get("FolderID").asUUID();
				parentID = map.get("ParentID").asUUID();
				name = map.get("Name").asString();
				type = FolderType.setValue(map.get("Type").asInteger());
			}
		}

		public class ItemDataInfo {
			public UUID itemID;
			public int callbackID;
			public UUID folderID;
			public UUID creatorID;
			public UUID ownerID;
			public UUID groupID;
			public int baseMask;
			public int ownerMask;
			public int groupMask;
			public int everyoneMask;
			public int nextOwnerMask;
			public boolean groupOwned;
			public UUID assetID;
			public AssetType assetType;
			public InventoryType inventoryType;
			public int flags;
			public SaleType saleType;
			public int salePrice;
			public String name;
			public String description;
			public Date creationDate;
			public int crc;

			public ItemDataInfo(OSDMap map) {
				itemID = map.get("ItemID").asUUID();
				callbackID = map.get("CallbackID").asUInteger();
				folderID = map.get("FolderID").asUUID();
				creatorID = map.get("CreatorID").asUUID();
				ownerID = map.get("OwnerID").asUUID();
				groupID = map.get("GroupID").asUUID();
				baseMask = PermissionMask.setValue(map.get("BaseMask").asUInteger());
				ownerMask = PermissionMask.setValue(map.get("OwnerMask").asUInteger());
				groupMask = PermissionMask.setValue(map.get("GroupMask").asUInteger());
				everyoneMask = PermissionMask.setValue(map.get("EveryoneMask").asUInteger());
				nextOwnerMask = PermissionMask.setValue(map.get("NextOwnerMask").asUInteger());
				groupOwned = map.get("GroupOwned").asBoolean();
				assetID = map.get("AssetID").asUUID();
				assetType = AssetType.setValue(map.get("Type").asInteger());
				inventoryType = InventoryType.setValue(map.get("InvType").asInteger());
				flags = map.get("Flags").asUInteger();
				saleType = SaleType.setValue(map.get("SaleType").asInteger());
				salePrice = map.get("SaleType").asInteger();
				name = map.get("Name").asString();
				description = map.get("Description").asString();
				creationDate = Helpers.unixTimeToDateTime(map.get("CreationDate").asReal());
				crc = map.get("CRC").asUInteger();
			}
		}

		public UUID agentID;
		public UUID transactionID;
		public FolderDataInfo[] folderData;
		public ItemDataInfo[] itemData;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.BulkUpdateInventory;
		}

		@Override
		public OSDMap serialize() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void deserialize(OSDMap map) {
			if (map.get("AgentData") instanceof OSDArray) {
				OSDArray array = (OSDArray) map.get("AgentData");
				if (array.size() > 0) {
					OSDMap adata = (OSDMap) array.get(0);
					agentID = adata.get("AgentID").asUUID();
					transactionID = adata.get("TransactionID").asUUID();
				}
			}

			if (map.get("FolderData") instanceof OSDArray) {
				OSDArray array = (OSDArray) map.get("FolderData");
				folderData = new FolderDataInfo[array.size()];
				for (int i = 0; i < array.size(); i++) {
					folderData[i] = new FolderDataInfo((OSDMap) array.get(i));
				}
			} else {
				folderData = new FolderDataInfo[0];
			}

			if (map.get("ItemData") instanceof OSDArray) {
				OSDArray array = (OSDArray) map.get("ItemData");
				itemData = new ItemDataInfo[array.size()];
				for (int i = 0; i < array.size(); i++) {
					itemData[i] = new ItemDataInfo((OSDMap) array.get(i));
				}
			} else {
				itemData = new ItemDataInfo[0];
			}
		}
	}

	public class WebFetchInventoryDescendentsMessage implements IMessage {

		// public class Folder implements InventoryBase
		// {

		// }

		// public class Item implements InventoryBase
		// {

		// }
		// #region CapsMessage Members

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.WebFetchInventoryDescendents;
		}

		@Override
		public OSDMap serialize() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void deserialize(OSDMap map) {
			throw new UnsupportedOperationException();
		}
	}

	// #endregion

	// #region Agent Messages

	// A message sent from the simulator to an agent which contains the groups
	// the agent is in
	public class AgentGroupDataUpdateMessage implements IMessage {
		// The Agent receiving the message

		public UUID agentID;

		// Group Details specific to the agent
		public class GroupData {
			// true of the agent accepts group notices
			public boolean acceptNotices;
			// The agents tier contribution to the group
			public int contribution;
			// The Groups {@link UUID}
			public UUID groupID;
			// The {@link UUID} of the groups insignia
			public UUID groupInsigniaID;
			// The name of the group
			public String groupName;
			// The Active Title
			public String groupTitle;
			// The aggregate permissions the agent has in the group for all
			// roles the agent is assigned
			public long groupPowers;
		}

		// An optional block containing additional agent specific information
		public class NewGroupData {
			// true of the agent allows this group to be listed in their profile
			public boolean listInProfile;
		}

		// An array containing {@link GroupData} information
		// for each <see cref="Group"/> the agent is a member of
		public GroupData[] groupDataBlock;
		// An array containing {@link NewGroupData} information
		// for each <see cref="Group"/> the agent is a member of
		public NewGroupData[] newGroupDataBlock;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.AgentGroupDataUpdate;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);

			OSDMap agent = new OSDMap(1);
			agent.put("AgentID", OSD.fromUUID(agentID));

			OSDArray agentArray = new OSDArray();
			agentArray.add(agent);

			map.put("AgentData", agentArray);

			OSDArray groupDataArray = new OSDArray(groupDataBlock.length);

			for (int i = 0; i < groupDataBlock.length; i++) {
				OSDMap group = new OSDMap(7);
				group.put("AcceptNotices", OSD.fromBoolean(groupDataBlock[i].acceptNotices));
				group.put("Contribution", OSD.fromInteger(groupDataBlock[i].contribution));
				group.put("GroupID", OSD.fromUUID(groupDataBlock[i].groupID));
				group.put("GroupInsigniaID", OSD.fromUUID(groupDataBlock[i].groupInsigniaID));
				group.put("GroupName", OSD.fromString(groupDataBlock[i].groupName));
				group.put("GroupTitle", OSD.fromString(groupDataBlock[i].groupTitle));
				group.put("GroupPowers", OSD.fromLong(groupDataBlock[i].groupPowers));
				groupDataArray.add(group);
			}

			map.put("GroupData", groupDataArray);

			OSDArray newGroupDataArray = new OSDArray(newGroupDataBlock.length);

			for (int i = 0; i < newGroupDataBlock.length; i++) {
				OSDMap group = new OSDMap(1);
				group.put("ListInProfile", OSD.fromBoolean(newGroupDataBlock[i].listInProfile));
				newGroupDataArray.add(group);
			}

			map.put("NewGroupData", newGroupDataArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDArray agentArray = (OSDArray) map.get("AgentData");
			OSDMap agentMap = (OSDMap) agentArray.get(0);
			agentID = agentMap.get("AgentID").asUUID();

			OSDArray groupArray = (OSDArray) map.get("GroupData");

			groupDataBlock = new GroupData[groupArray.size()];

			for (int i = 0; i < groupArray.size(); i++) {
				OSDMap groupMap = (OSDMap) groupArray.get(i);

				GroupData groupData = new GroupData();

				groupData.groupID = groupMap.get("GroupID").asUUID();
				groupData.contribution = groupMap.get("Contribution").asInteger();
				groupData.groupInsigniaID = groupMap.get("GroupInsigniaID").asUUID();
				groupData.groupName = groupMap.get("GroupName").asString();
				groupData.groupPowers = groupMap.get("GroupPowers").asLong();
				groupData.acceptNotices = groupMap.get("AcceptNotices").asBoolean();
				if (groupMap.containsKey("GroupTitle"))
					groupData.groupTitle = groupMap.get("GroupTitle").asString();
				groupDataBlock[i] = groupData;
			}

			// If request for current groups came very close to login
			// the Linden sim will not include the NewGroupData block, but
			// it will instead set all ListInProfile fields to false
			if (map.containsKey("NewGroupData")) {
				OSDArray newGroupArray = (OSDArray) map.get("NewGroupData");

				newGroupDataBlock = new NewGroupData[newGroupArray.size()];

				for (int i = 0; i < newGroupArray.size(); i++) {
					OSDMap newGroupMap = (OSDMap) newGroupArray.get(i);
					NewGroupData newGroupData = new NewGroupData();
					newGroupData.listInProfile = newGroupMap.get("ListInProfile").asBoolean();
					newGroupDataBlock[i] = newGroupData;
				}
			} else {
				newGroupDataBlock = new NewGroupData[groupDataBlock.length];
				for (int i = 0; i < newGroupDataBlock.length; i++) {
					NewGroupData newGroupData = new NewGroupData();
					newGroupData.listInProfile = false;
					newGroupDataBlock[i] = newGroupData;
				}
			}
		}
	}

	// A message sent from the viewer to the simulator which
	// specifies the language and permissions for others to detect the language
	// specified
	public class UpdateAgentLanguageMessage implements IMessage {
		// A string containng the default language to use for the agent
		public String language;
		// true of others are allowed to know the language setting
		public boolean languagePublic;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UpdateAgentLanguage;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);

			map.put("language", OSD.fromString(language));
			map.put("language_is_public", OSD.fromBoolean(languagePublic));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			languagePublic = map.get("language_is_public").asBoolean();
			language = map.get("language").asString();
		}
	}

	// An CapsEventQueue message sent from the simulator to an agent when the agent
	// leaves a group
	public class AgentDropGroupMessage implements IMessage {
		// An object containing the Agents UUID, and the Groups UUID
		public class AgentData {
			// The ID of the Agent leaving the group
			public UUID agentID;
			// The GroupID the Agent is leaving
			public UUID groupID;
		}

		// An Array containing the AgentID and GroupID

		public AgentData[] agentDataBlock;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.AgentDropGroup;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);

			OSDArray agentDataArray = new OSDArray(agentDataBlock.length);

			for (int i = 0; i < agentDataBlock.length; i++) {
				OSDMap agentMap = new OSDMap(2);
				agentMap.put("AgentID", OSD.fromUUID(agentDataBlock[i].agentID));
				agentMap.put("GroupID", OSD.fromUUID(agentDataBlock[i].groupID));
				agentDataArray.add(agentMap);
			}
			map.put("AgentData", agentDataArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDArray agentDataArray = (OSDArray) map.get("AgentData");

			agentDataBlock = new AgentData[agentDataArray.size()];

			for (int i = 0; i < agentDataArray.size(); i++) {
				OSDMap agentMap = (OSDMap) agentDataArray.get(i);
				AgentData agentData = new AgentData();

				agentData.agentID = agentMap.get("AgentID").asUUID();
				agentData.groupID = agentMap.get("GroupID").asUUID();

				agentDataBlock[i] = agentData;
			}
		}
	}

	// An CapsEventQueue message sent from the simulator to an agent when the agent
	// state changes
	public class AgentStateUpdateMessage implements IMessage {
		public class Preferences {
			public int godLevel;
			public boolean languageIsPublic;
			public String maxAccess;
			public String language;
			public boolean alterPermanentObjects;
			public boolean alterNavmeshObjects;
		}

		public boolean hasModifiedNavmesh;
		public boolean canModifyNavmesh;

		public Preferences preferences;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.AgentStateUpdate;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap" containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap access = new OSDMap(1);
			access.put("max", OSD.fromString(preferences.maxAccess));

			OSDMap prefs = new OSDMap(6);
			prefs.put("god_level", OSD.fromInteger(preferences.godLevel));
			prefs.put("language_is_public", OSD.fromBoolean(preferences.languageIsPublic));
			prefs.put("language", OSD.fromString(preferences.language));
			prefs.put("alter_permanent_objects", OSD.fromBoolean(preferences.alterPermanentObjects));
			prefs.put("alter_navmesh_objects", OSD.fromBoolean(preferences.alterNavmeshObjects));
			prefs.put("access_prefs", access);

			OSDMap map = new OSDMap(3);
			map.put("has_modified_navmesh", OSD.fromBoolean(hasModifiedNavmesh));
			map.put("can_modify_navmesh", OSD.fromBoolean(canModifyNavmesh));
			map.put("preferences", prefs);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap" containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			hasModifiedNavmesh = map.get("has_modified_navmesh").asBoolean();
			canModifyNavmesh = map.get("can_modify_navmesh").asBoolean();
			preferences = new Preferences();

			OSDMap prefs = (OSDMap) map.get("preferences");
			if (prefs != null) {
				preferences.godLevel = prefs.get("god_level").asInteger();
				preferences.languageIsPublic = prefs.get("language_is_public").asBoolean();
				preferences.language = prefs.get("language").asString();
				preferences.alterPermanentObjects = prefs.get("alter_permanent_objects").asBoolean();
				preferences.alterNavmeshObjects = prefs.get("alter_navmesh_objects").asBoolean();

				OSDMap access = (OSDMap) prefs.get("access_prefs");
				preferences.maxAccess = access.get("max").asString();
			}
		}
	}

	// Base class for Asset uploads/results via Capabilities
	public abstract class AssetUploaderBlock {
		// The request state
		public String state;

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		public abstract OSDMap serialize();

		public abstract void deserialize(OSDMap map);
	}

	// A message sent from the viewer to the simulator to request a temporary
	// upload capability
	// which allows an asset to be uploaded
	public class UploaderRequestUpload extends AssetUploaderBlock {
		// The Capability URL sent by the simulator to upload the baked texture
		// to
		public URI url;

		public UploaderRequestUpload() {
			state = "upload";
		}

		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);
			map.put("state", OSD.fromString(state));
			map.put("uploader", OSD.fromUri(url));

			return map;
		}

		@Override
		public void deserialize(OSDMap map) {
			url = map.get("uploader").asUri();
			state = map.get("state").asString();
		}
	}

	// A message sent from the simulator that will inform the agent the upload
	// is complete, and the UUID of the uploaded asset
	public class UploaderRequestComplete extends AssetUploaderBlock {
		// The uploaded texture asset ID
		public UUID assetID;

		public UploaderRequestComplete() {
			state = "complete";
		}

		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);
			map.put("state", OSD.fromString(state));
			map.put("new_asset", OSD.fromUUID(assetID));

			return map;
		}

		@Override
		public void deserialize(OSDMap map) {
			assetID = map.get("new_asset").asUUID();
			state = map.get("state").asString();
		}
	}

	// A message sent from the viewer to the simulator to request a temporary
	// capability URI which is used to upload an agents baked appearance
	// textures
	public class UploadBakedTextureMessage implements IMessage {
		// Object containing request or response
		public AssetUploaderBlock request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UploadBakedTexture;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return request.serialize();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("state")) {
				String value = map.get("state").asString();
				if (value.equals("upload")) {
					request = new UploaderRequestUpload();
					request.deserialize(map);
				} else if (value.equals("complete")) {
					request = new UploaderRequestComplete();
					request.deserialize(map);
				} else
					logger.warn(
							"Unable to deserialize UploadBakedTexture: No message handler exists for state " + value);
			}
		}
	}

	// #endregion

	// #region Voice Messages

	// A message sent from the simulator which indicates the minimum version
	// required for using voice chat
	public class RequiredVoiceVersionMessage implements IMessage {
		// Major Version Required
		public int majorVersion;
		// Minor version required
		public int minorVersion;
		// The name of the region sending the version requrements
		public String regionName;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.RequiredVoiceVersion;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(4);
			map.put("major_version", OSD.fromInteger(majorVersion));
			map.put("minor_version", OSD.fromInteger(minorVersion));
			map.put("region_name", OSD.fromString(regionName));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			majorVersion = map.get("major_version").asInteger();
			minorVersion = map.get("minor_version").asInteger();
			regionName = map.get("region_name").asString();
		}
	}

	// A message sent from the simulator to the viewer containing the voice
	// server URI
	public class ParcelVoiceInfoRequestMessage implements IMessage {
		// The Parcel ID which the voice server URI applies
		public int parcelID;
		// The name of the region
		public String regionName;
		// A uri containing the server/channel information which the viewer can
		// utilize to participate in voice conversations
		public URI sipChannelUri;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ParcelVoiceInfoRequest;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);
			map.put("parcel_local_id", OSD.fromInteger(parcelID));
			map.put("region_name", OSD.fromString(regionName));

			OSDMap vcMap = new OSDMap(1);
			vcMap.put("channel_uri", OSD.fromUri(sipChannelUri));

			map.put("voice_credentials", vcMap);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			parcelID = map.get("parcel_local_id").asInteger();
			regionName = map.get("region_name").asString();

			OSDMap vcMap = (OSDMap) map.get("voice_credentials");
			sipChannelUri = vcMap.get("channel_uri").asUri();
		}
	}

	public class ProvisionVoiceAccountRequestMessage implements IMessage {
		//
		public String password;
		//
		public String username;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ProvisionVoiceAccountRequest;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);

			map.put("username", OSD.fromString(username));
			map.put("password", OSD.fromString(password));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			username = map.get("username").asString();
			password = map.get("password").asString();
		}
	}

	// #endregion

	// #region Script/Notecards Messages

	// A message sent by the viewer to the simulator to request a temporary
	// capability for a script contained with in a Tasks inventory to be updated
	public class UploadScriptTaskMessage implements IMessage {
		// Object containing request or response
		public AssetUploaderBlock request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UploadScriptTask;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return request.serialize();
		}

		public UploadScriptTaskMessage(String value) {
			if (value.equals("upload"))
				request = new UploaderRequestUpload();
			else if (value.equals("complete"))
				request = new UploaderRequestComplete();
			else
				logger.warn("Unable to deserialize UploadScriptTask: No message handler exists for state " + value);
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("state")) {
				String value = map.get("state").asString();
				if (value.equals("upload")) {
					request = new UploaderRequestUpload();
					request.deserialize(map);
				} else if (value.equals("complete")) {
					request = new UploaderRequestComplete();
					request.deserialize(map);
				} else
					logger.warn("Unable to deserialize UploadScriptTask: No message handler exists for state " + value);
			}
		}
	}

	// A message sent from the simulator to the viewer to indicate a Tasks
	// scripts status.
	public class ScriptRunningReplyMessage implements IMessage {
		// The Asset ID of the script
		public UUID itemID;
		// True of the script is compiled/ran using the mono interpreter, false
		// indicates it
		// uses the older less efficient lsl2 interprter
		public boolean mono;
		// The Task containing the scripts {@link UUID}
		public UUID objectID;
		// true of the script is in a running state
		public boolean running;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ScriptRunningReply;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);

			OSDMap scriptMap = new OSDMap(4);
			scriptMap.put("ItemID", OSD.fromUUID(itemID));
			scriptMap.put("Mono", OSD.fromBoolean(mono));
			scriptMap.put("ObjectID", OSD.fromUUID(objectID));
			scriptMap.put("Running", OSD.fromBoolean(running));

			OSDArray scriptArray = new OSDArray(1);
			scriptArray.add(scriptMap);

			map.put("Script", scriptArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDArray scriptArray = (OSDArray) map.get("Script");

			OSDMap scriptMap = (OSDMap) scriptArray.get(0);

			itemID = scriptMap.get("ItemID").asUUID();
			mono = scriptMap.get("Mono").asBoolean();
			objectID = scriptMap.get("ObjectID").asUUID();
			running = scriptMap.get("Running").asBoolean();
		}
	}

	// A message containing the request/response used for updating a gesture
	// contained with an agents inventory
	public class UpdateGestureAgentInventoryMessage implements IMessage {
		// Object containing request or response
		public AssetUploaderBlock request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UpdateGestureAgentInventory;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return request.serialize();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("item_id")) {
				request = new UpdateAgentInventoryRequestMessage();
				request.deserialize(map);
			} else if (map.containsKey("state")) {
				String value = map.get("state").asString();
				if (value.equals("upload")) {
					request = new UploaderRequestUpload();
					request.deserialize(map);
				} else if (value.equals("complete")) {
					request = new UploaderRequestComplete();
					request.deserialize(map);
				} else
					logger.warn(
							"Unable to deserialize UpdateGestureAgentInventory: No message handler exists for state "
									+ value);
			} else
				logger.warn("Unable to deserialize UpdateGestureAgentInventory: No message handler exists for message "
						+ map.asString());
		}
	}

	// A message request/response which is used to update a notecard contained
	// within a tasks inventory
	public class UpdateNotecardTaskInventoryMessage implements IMessage {
		// The {@link UUID} of the Task containing the notecard asset to update
		public UUID taskID;
		// The notecard assets {@link UUID} contained in the tasks inventory
		public UUID itemID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UpdateNotecardTaskInventory;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);
			map.put("task_id", OSD.fromUUID(taskID));
			map.put("item_id", OSD.fromUUID(itemID));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			taskID = map.get("task_id").asUUID();
			itemID = map.get("item_id").asUUID();
		}
	}

	// TODO: Add Test
	//
	// A reusable class containing a message sent from the viewer to the
	// simulator to request a temporary uploader capability
	// which is used to update an asset in an agents inventory
	public class UpdateAgentInventoryRequestMessage extends AssetUploaderBlock {
		// The Notecard AssetID to replace
		public UUID itemID;

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);
			map.put("item_id", OSD.fromUUID(itemID));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			itemID = map.get("item_id").asUUID();
		}
	}

	// A message containing the request/response used for updating a notecard
	// contained with an agents inventory
	public class UpdateNotecardAgentInventoryMessage implements IMessage {
		// Object containing request or response
		public AssetUploaderBlock request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UpdateNotecardAgentInventory;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return request.serialize();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("item_id")) {
				request = new UpdateAgentInventoryRequestMessage();
				request.deserialize(map);
			} else if (map.containsKey("state")) {
				String value = map.get("state").asString();
				if (value.equals("upload")) {
					request = new UploaderRequestUpload();
					request.deserialize(map);
				} else if (value.equals("complete")) {
					request = new UploaderRequestComplete();
					request.deserialize(map);
				} else
					logger.warn(
							"Unable to deserialize UpdateNotecardAgentInventory: No message handler exists for state "
									+ value);
			} else
				logger.warn("Unable to deserialize UpdateNotecardAgentInventory: No message handler exists for message "
						+ map.toString());
		}
	}

	public class CopyInventoryFromNotecardMessage implements IMessage {
		public int callbackID;
		public UUID folderID;
		public UUID itemID;
		public UUID notecardID;
		public UUID objectID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.CopyInventoryFromNotecard;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(5);
			map.put("callback-id", OSD.fromInteger(callbackID));
			map.put("folder-id", OSD.fromUUID(folderID));
			map.put("item-id", OSD.fromUUID(itemID));
			map.put("notecard-id", OSD.fromUUID(notecardID));
			map.put("object-id", OSD.fromUUID(objectID));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			callbackID = map.get("callback-id").asInteger();
			folderID = map.get("folder-id").asUUID();
			itemID = map.get("item-id").asUUID();
			notecardID = map.get("notecard-id").asUUID();
			objectID = map.get("object-id").asUUID();
		}
	}

	// A message sent from the simulator to the viewer which indicates an error
	// occurred while attempting
	// to update a script in an agents or tasks inventory
	public class UploaderScriptRequestError extends AssetUploaderBlock implements IMessage {
		// true of the script was successfully compiled by the simulator
		public boolean compiled;
		// A String containing the error which occurred while trying to update
		// the script
		public String error;
		// A new AssetID assigned to the script
		public UUID assetID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UploaderScriptRequestError;
		}

		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(4);
			map.put("state", OSD.fromString(state));
			map.put("new_asset", OSD.fromUUID(assetID));
			map.put("compiled", OSD.fromBoolean(compiled));

			OSDArray errorsArray = new OSDArray();
			errorsArray.add(OSD.fromString(error));
			map.put("errors", errorsArray);
			return map;
		}

		@Override
		public void deserialize(OSDMap map) {
			assetID = map.get("new_asset").asUUID();
			compiled = map.get("compiled").asBoolean();
			state = map.get("state").asString();

			OSDArray errorsArray = (OSDArray) map.get("errors");
			error = errorsArray.get(0).asString();
		}
	}

	// A message sent from the viewer to the simulator requesting the update of
	// an existing script contained
	// within a tasks inventory
	public class UpdateScriptTaskUpdateMessage extends AssetUploaderBlock implements IMessage {
		// if true, set the script mode to running
		public boolean scriptRunning;
		// The scripts InventoryItem ItemID to update
		public UUID itemID;
		// A lowercase string containing either "mono" or "lsl2" which specifies
		// the script is compiled
		// and ran on the mono runtime, or the older lsl runtime
		public String target; // mono or lsl2
		// The tasks <see cref="UUID"/> which contains the script to update
		public UUID taskID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UpdateScriptTaskUpdateMessage;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(4);
			map.put("is_script_running", OSD.fromBoolean(scriptRunning));
			map.put("item_id", OSD.fromUUID(itemID));
			map.put("target", OSD.fromString(target));
			map.put("task_id", OSD.fromUUID(taskID));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			scriptRunning = map.get("is_script_running").asBoolean();
			itemID = map.get("item_id").asUUID();
			target = map.get("target").asString();
			taskID = map.get("task_id").asUUID();
		}
	}

	// A message containing either the request or response used in updating a
	// script inside a tasks inventory
	public class UpdateScriptTaskMessage implements IMessage {
		// Object containing request or response
		public AssetUploaderBlock request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UpdateScriptTask;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return request.serialize();
		}

		public void getMessageHandler(OSDMap map) {
			String value = map.get("method").asString();
			if (value.equals("task_id")) {
				request = new UpdateScriptTaskUpdateMessage();
			} else if (value.equals("upload")) {
				request = new UploaderRequestUpload();
			} else if (value.equals("errors")) {
				request = new UploaderScriptRequestError();
			} else if (value.equals("complete")) {
				request = new UploaderRequestScriptComplete();
			} else
				logger.warn(
						"Unable to deserialize UpdateScriptTaskMessage: No message handler exists for state " + value);
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("task_id")) {
				request = new UpdateScriptTaskUpdateMessage();
				request.deserialize(map);
			} else if (map.containsKey("state")) {
				String value = map.get("state").asString();
				if (value.equals("upload")) {
					request = new UploaderRequestUpload();
					request.deserialize(map);
				} else if (value.equals("complete") && map.containsKey("errors")) {
					request = new UploaderScriptRequestError();
					request.deserialize(map);
				} else if (value.equals("complete")) {
					request = new UploaderRequestScriptComplete();
					request.deserialize(map);
				} else
					logger.warn("Unable to deserialize UpdateScriptTaskMessage: No message handler exists for state "
							+ value);
			} else
				logger.warn("Unable to deserialize UpdateScriptTaskMessage: No message handler exists for message "
						+ map.asString());
		}
	}

	// Response from the simulator to notify the viewer the upload is completed,
	// and the UUID of the script asset and its compiled status
	public class UploaderRequestScriptComplete extends AssetUploaderBlock {
		// The uploaded texture asset ID
		public UUID assetID;
		// true of the script was compiled successfully
		public boolean compiled;

		public UploaderRequestScriptComplete() {
			state = "complete";
		}

		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);
			map.put("state", OSD.fromString(state));
			map.put("new_asset", OSD.fromUUID(assetID));
			map.put("compiled", OSD.fromBoolean(compiled));
			return map;
		}

		@Override
		public void deserialize(OSDMap map) {
			assetID = map.get("new_asset").asUUID();
			compiled = map.get("compiled").asBoolean();
		}
	}

	// A message sent from a viewer to the simulator requesting a temporary
	// uploader capability used to update a script contained in an agents
	// inventory
	public class UpdateScriptAgentRequestMessage extends AssetUploaderBlock {
		// The existing asset if of the script in the agents inventory to
		// replace
		public UUID itemID;
		// The language of the script
		// Defaults to lsl version 2, "mono" might be another possible option
		public String target = "lsl2"; // lsl2

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);
			map.put("item_id", OSD.fromUUID(itemID));
			map.put("target", OSD.fromString(target));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			itemID = map.get("item_id").asUUID();
			target = map.get("target").asString();
		}
	}

	// A message containing either the request or response used in updating a
	// script inside an agents inventory
	public class UpdateScriptAgentMessage implements IMessage {
		// Object containing request or response
		public AssetUploaderBlock request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UpdateScriptAgent;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return request.serialize();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("item_id")) {
				request = new UpdateScriptAgentRequestMessage();
				request.deserialize(map);
			} else if (map.containsKey("errors")) {
				request = new UploaderScriptRequestError();
				request.deserialize(map);
			} else if (map.containsKey("state")) {
				String value = map.get("state").asString();
				if (value.equals("upload")) {
					request = new UploaderRequestUpload();
					request.deserialize(map);
				} else if (value.equals("complete")) {
					request = new UploaderRequestScriptComplete();
					request.deserialize(map);
				} else
					logger.warn(
							"Unable to deserialize UpdateScriptAgent: No message handler exists for state " + value);
			} else
				logger.warn("Unable to deserialize UpdateScriptAgent: No message handler exists for message "
						+ map.asString());
		}
	}

	public class SendPostcardMessage implements IMessage {
		public String fromEmail;
		public String message;
		public String fromName;
		public Vector3 globalPosition;
		public String subject;
		public String toEmail;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.SendPostcard;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(6);
			map.put("from", OSD.fromString(fromEmail));
			map.put("msg", OSD.fromString(message));
			map.put("name", OSD.fromString(fromName));
			map.put("pos-global", OSD.fromVector3(globalPosition));
			map.put("subject", OSD.fromString(subject));
			map.put("to", OSD.fromString(toEmail));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			fromEmail = map.get("from").asString();
			message = map.get("msg").asString();
			fromName = map.get("name").asString();
			globalPosition = map.get("pos-global").asVector3();
			subject = map.get("subject").asString();
			toEmail = map.get("to").asString();
		}
	}

	// #endregion

	// #region Grid/Maps

	// Base class for Map Layers via Capabilities
	// TODO:FIXME
	// Why does this class exist?
	public abstract class MapLayerMessageBase {
		//
		public int flags;

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		public abstract OSDMap serialize();

		public abstract void deserialize(OSDMap map);
	}

	// Sent by an agent to the capabilities server to request map layers
	public class MapLayerRequestVariant extends MapLayerMessageBase {
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);
			map.put("Flags", OSD.fromInteger(flags));
			return map;
		}

		@Override
		public void deserialize(OSDMap map) {
			flags = map.get("Flags").asInteger();
		}
	}

	// A message sent from the simulator to the viewer which contains an array
	// of map images and their grid coordinates
	public class MapLayerReplyVariant extends MapLayerMessageBase {
		// An object containing map location details
		public class LayerData {
			// The Asset ID of the regions tile overlay
			public UUID imageID;
			// The grid location of the southern border of the map tile
			public int bottom;
			// The grid location of the western border of the map tile
			public int left;
			// The grid location of the eastern border of the map tile
			public int right;
			// The grid location of the northern border of the map tile
			public int top;
		}

		// An array containing LayerData items
		public LayerData[] layerDataBlocks;

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);
			OSDMap agentMap = new OSDMap(1);
			agentMap.put("Flags", OSD.fromInteger(flags));
			map.put("AgentData", agentMap);

			OSDArray layerArray = new OSDArray(layerDataBlocks.length);

			for (int i = 0; i < layerDataBlocks.length; i++) {
				OSDMap layerMap = new OSDMap(5);
				layerMap.put("ImageID", OSD.fromUUID(layerDataBlocks[i].imageID));
				layerMap.put("Bottom", OSD.fromInteger(layerDataBlocks[i].bottom));
				layerMap.put("Left", OSD.fromInteger(layerDataBlocks[i].left));
				layerMap.put("Top", OSD.fromInteger(layerDataBlocks[i].top));
				layerMap.put("Right", OSD.fromInteger(layerDataBlocks[i].right));

				layerArray.add(layerMap);
			}

			map.put("LayerData", layerArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDMap agentMap = (OSDMap) map.get("AgentData");
			flags = agentMap.get("Flags").asInteger();

			OSDArray layerArray = (OSDArray) map.get("LayerData");

			layerDataBlocks = new LayerData[layerArray.size()];

			for (int i = 0; i < layerDataBlocks.length; i++) {
				OSDMap layerMap = (OSDMap) layerArray.get(i);

				LayerData layer = new LayerData();
				layer.imageID = layerMap.get("ImageID").asUUID();
				layer.top = layerMap.get("Top").asInteger();
				layer.right = layerMap.get("Right").asInteger();
				layer.left = layerMap.get("Left").asInteger();
				layer.bottom = layerMap.get("Bottom").asInteger();

				layerDataBlocks[i] = layer;
			}
		}
	}

	public class MapLayerMessage implements IMessage {
		// Object containing request or response
		public MapLayerMessageBase request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.MapLayer;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return request.serialize();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("LayerData")) {
				request = new MapLayerReplyVariant();
				request.deserialize(map);
			} else if (map.containsKey("Flags")) {
				request = new MapLayerRequestVariant();
				request.deserialize(map);
			} else
				logger.warn("Unable to deserialize MapLayerMessage: No message handler exists");
		}
	}

	// #endregion

	// #region Session/Communication

	// New as of 1.23 RC1, no details yet.
	public class ProductInfoRequestMessage implements IMessage {
		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ProductInfoRequest;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			throw new UnsupportedOperationException();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			throw new UnsupportedOperationException();
		}
	}

	// #region ChatSessionRequestMessage
	// TODO:FIXME
	// Why does this class exist?
	public abstract class SearchStatRequestBlock {
		public abstract OSDMap serialize();

		public abstract void deserialize(OSDMap map);
	}

	// variant A - the request to the simulator
	public class SearchStatRequestRequest extends SearchStatRequestBlock {
		public UUID classifiedID;

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);
			map.put("classified_id", OSD.fromUUID(classifiedID));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			classifiedID = map.get("classified_id").asUUID();
		}
	}

	public class SearchStatRequestReply extends SearchStatRequestBlock {
		public int mapClicks;
		public int profileClicks;
		public int searchMapClicks;
		public int searchProfileClicks;
		public int searchTeleportClicks;
		public int teleportClicks;

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(6);
			map.put("map_clicks", OSD.fromInteger(mapClicks));
			map.put("profile_clicks", OSD.fromInteger(profileClicks));
			map.put("search_map_clicks", OSD.fromInteger(searchMapClicks));
			map.put("search_profile_clicks", OSD.fromInteger(searchProfileClicks));
			map.put("search_teleport_clicks", OSD.fromInteger(searchTeleportClicks));
			map.put("teleport_clicks", OSD.fromInteger(teleportClicks));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			mapClicks = map.get("map_clicks").asInteger();
			profileClicks = map.get("profile_clicks").asInteger();
			searchMapClicks = map.get("search_map_clicks").asInteger();
			searchProfileClicks = map.get("search_profile_clicks").asInteger();
			searchTeleportClicks = map.get("search_teleport_clicks").asInteger();
			teleportClicks = map.get("teleport_clicks").asInteger();
		}
	}

	public class SearchStatRequestMessage implements IMessage {
		public SearchStatRequestBlock request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.SearchStatRequest;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return request.serialize();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("map_clicks")) {
				request = new SearchStatRequestReply();
				request.deserialize(map);
			} else if (map.containsKey("classified_id")) {
				request = new SearchStatRequestRequest();
				request.deserialize(map);
			} else
				logger.warn("Unable to deserialize SearchStatRequest: No message handler exists for method "
						+ map.get("method").asString());
		}
	}

	public abstract class ChatSessionRequestBlock implements IMessage {
		// A string containing the method used
		public String method;

		public abstract OSDMap serialize();

		public abstract void deserialize(OSDMap map);
	}

	/*
	 * A request sent from an agent to the Simulator to begin a new conference.
	 * Contains a list of Agents which will be included in the conference
	 */
	public class ChatSessionRequestStartConference extends ChatSessionRequestBlock {
		// An array containing the <see cref="UUID"/> of the agents invited to
		// this conference
		public UUID[] agentsBlock;
		// The conferences Session ID
		public UUID sessionID;

		public ChatSessionRequestStartConference() {
			method = "start conference";
		}

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ChatSessionRequestStartConference;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);
			map.put("method", OSD.fromString(method));
			OSDArray agentsArray = new OSDArray();
			for (int i = 0; i < agentsBlock.length; i++) {
				agentsArray.add(OSD.fromUUID(agentsBlock[i]));
			}
			map.put("params", agentsArray);
			map.put("session-id", OSD.fromUUID(sessionID));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			method = map.get("method").asString();
			OSDArray agentsArray = (OSDArray) map.get("params");

			agentsBlock = new UUID[agentsArray.size()];

			for (int i = 0; i < agentsArray.size(); i++) {
				agentsBlock[i] = agentsArray.get(i).asUUID();
			}

			sessionID = map.get("session-id").asUUID();
		}
	}

	/*
	 * A moderation request sent from a conference moderator Contains an agent and
	 * an optional action to take
	 */
	public class ChatSessionRequestMuteUpdate extends ChatSessionRequestBlock {
		// The Session ID
		public UUID sessionID;
		public UUID agentID;
		/*
		 * A list containing Key/Value pairs, known valid values: key: text value:
		 * true/false - allow/disallow specified agents ability to use text in session
		 * key: voice value: true/false - allow/disallow specified agents ability to use
		 * voice in session
		 *
		 * "text" or "voice"
		 */
		public String requestKey;
		public boolean requestValue;

		public ChatSessionRequestMuteUpdate() {
			method = "mute update";
		}

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ChatSessionRequestMuteUpdate;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);
			map.put("method", OSD.fromString(method));

			OSDMap muteMap = new OSDMap(1);
			muteMap.put(requestKey, OSD.fromBoolean(requestValue));

			OSDMap paramMap = new OSDMap(2);
			paramMap.put("agent_id", OSD.fromUUID(agentID));
			paramMap.put("mute_info", muteMap);

			map.put("params", paramMap);
			map.put("session-id", OSD.fromUUID(sessionID));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			method = map.get("method").asString();
			sessionID = map.get("session-id").asUUID();

			OSDMap paramsMap = (OSDMap) map.get("params");
			OSDMap muteMap = (OSDMap) paramsMap.get("mute_info");

			agentID = paramsMap.get("agent_id").asUUID();

			if (muteMap.containsKey("text"))
				requestKey = "text";
			else if (muteMap.containsKey("voice"))
				requestKey = "voice";

			requestValue = muteMap.get(requestKey).asBoolean();
		}
	}

	// A message sent from the agent to the simulator which tells the simulator
	// we've accepted a conference invitation
	public class ChatSessionAcceptInvitation extends ChatSessionRequestBlock {
		// The conference SessionID
		public UUID sessionID;

		public ChatSessionAcceptInvitation() {
			method = "accept invitation";
		}

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ChatSessionAcceptInvitation;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);
			map.put("method", OSD.fromString(method));
			map.put("session-id", OSD.fromUUID(sessionID));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			method = map.get("method").asString();
			sessionID = map.get("session-id").asUUID();
		}
	}

	public class ChatSessionRequestMessage implements IMessage {
		public ChatSessionRequestBlock request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ChatSessionRequest;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return request.serialize();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("method")) {
				String value = map.get("method").asString();
				if (value.equals("start conference")) {
					request = new ChatSessionRequestStartConference();
					request.deserialize(map);
				} else if (value.equals("mute update")) {
					request = new ChatSessionRequestMuteUpdate();
					request.deserialize(map);
				} else if (value.equals("accept invitation")) {
					request = new ChatSessionAcceptInvitation();
					request.deserialize(map);
				} else
					logger.warn(
							"Unable to deserialize ChatSessionRequest: No message handler exists for method " + value);
			} else
				logger.warn("Unable to deserialize ChatSessionRequest: No message handler exists for message "
						+ map.asString());
		}
	}

	// #endregion

	public class ChatterBoxSessionEventReplyMessage implements IMessage {
		public UUID sessionID;
		public boolean success;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ChatterBoxSessionEventReply;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);
			map.put("success", OSD.fromBoolean(success));
			map.put("session_id", OSD.fromUUID(sessionID)); // FIXME: Verify
															// this is correct
															// map name

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			success = map.get("success").asBoolean();
			sessionID = map.get("session_id").asUUID();
		}
	}

	public class ChatterBoxSessionStartReplyMessage implements IMessage {
		public UUID sessionID;
		public UUID tempSessionID;
		public boolean success;

		public String sessionName;
		// FIXME: Replace int with an enum
		public int type;
		public boolean voiceEnabled;
		public boolean moderatedVoice;

		// Is Text moderation possible?

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ChatterBoxSessionStartReply;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap moderatedMap = new OSDMap(1);
			moderatedMap.put("voice", OSD.fromBoolean(moderatedVoice));

			OSDMap sessionMap = new OSDMap(4);
			sessionMap.put("type", OSD.fromInteger(type));
			sessionMap.put("session_name", OSD.fromString(sessionName));
			sessionMap.put("voice_enabled", OSD.fromBoolean(voiceEnabled));
			sessionMap.put("moderated_mode", moderatedMap);

			OSDMap map = new OSDMap(4);
			map.put("session_id", OSD.fromUUID(sessionID));
			map.put("temp_session_id", OSD.fromUUID(tempSessionID));
			map.put("success", OSD.fromBoolean(success));
			map.put("session_info", sessionMap);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			sessionID = map.get("session_id").asUUID();
			tempSessionID = map.get("temp_session_id").asUUID();
			success = map.get("success").asBoolean();

			if (success) {
				OSDMap sessionMap = (OSDMap) map.get("session_info");
				sessionName = sessionMap.get("session_name").asString();
				type = sessionMap.get("type").asInteger();
				voiceEnabled = sessionMap.get("voice_enabled").asBoolean();

				OSDMap moderatedModeMap = (OSDMap) sessionMap.get("moderated_mode");
				moderatedVoice = moderatedModeMap.get("voice").asBoolean();
			}
		}
	}

	public class ChatterBoxInvitationMessage implements IMessage {
		// Key of sender
		public UUID fromAgentID;
		// Name of sender
		public String fromAgentName;
		// Key of destination avatar
		public UUID toAgentID;
		// ID of originating estate
		public int parentEstateID;
		// Key of originating region
		public UUID regionID;
		// Coordinates in originating region
		public Vector3 position;
		// Instant message type
		public InstantMessageDialog dialog;
		// Group IM session toggle
		public boolean groupIM;
		// Key of IM session, for Group Messages, the groups UUID
		public UUID imSessionID;
		// Timestamp of the instant message
		public Date timestamp;
		// Instant message text
		public String message;
		// Whether this message is held for offline avatars
		public InstantMessageOnline offline;
		// Context specific packed data
		public byte[] binaryBucket;
		// Is this invitation for voice group/conference chat
		public boolean voice;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ChatterBoxInvitation;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap dataMap = new OSDMap(3);
			dataMap.put("timestamp", OSD.fromDate(timestamp));
			dataMap.put("type", OSD.fromInteger(dialog.getValue()));
			dataMap.put("binary_bucket", OSD.fromBinary(binaryBucket));

			OSDMap paramsMap = new OSDMap(11);
			paramsMap.put("from_id", OSD.fromUUID(fromAgentID));
			paramsMap.put("from_name", OSD.fromString(fromAgentName));
			paramsMap.put("to_id", OSD.fromUUID(toAgentID));
			paramsMap.put("parent_estate_id", OSD.fromInteger(parentEstateID));
			paramsMap.put("region_id", OSD.fromUUID(regionID));
			paramsMap.put("position", OSD.fromVector3(position));
			paramsMap.put("from_group", OSD.fromBoolean(groupIM));
			paramsMap.put("id", OSD.fromUUID(imSessionID));
			paramsMap.put("message", OSD.fromString(message));
			paramsMap.put("offline", OSD.fromInteger(offline.getValue()));

			paramsMap.put("data", dataMap);

			OSDMap imMap = new OSDMap(1);
			imMap.put("message_params", paramsMap);

			OSDMap map = new OSDMap(1);
			map.put("instantmessage", imMap);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("voice")) {
				fromAgentID = map.get("from_id").asUUID();
				fromAgentName = map.get("from_name").asString();
				imSessionID = map.get("session_id").asUUID();
				binaryBucket = Helpers.stringToBytes(map.get("session_name").asString());
				voice = true;
			} else {
				OSDMap im = (OSDMap) map.get("instantmessage");
				OSDMap msg = (OSDMap) im.get("message_params");
				OSDMap msgdata = (OSDMap) msg.get("data");

				fromAgentID = msg.get("from_id").asUUID();
				fromAgentName = msg.get("from_name").asString();
				toAgentID = msg.get("to_id").asUUID();
				parentEstateID = msg.get("parent_estate_id").asInteger();
				regionID = msg.get("region_id").asUUID();
				position = msg.get("position").asVector3();
				groupIM = msg.get("from_group").asBoolean();
				imSessionID = msg.get("id").asUUID();
				message = msg.get("message").asString();
				offline = InstantMessageOnline.setValue(msg.get("offline").asInteger());
				dialog = InstantMessageDialog.setValue(msgdata.get("type").asInteger());
				binaryBucket = msgdata.get("binary_bucket").asBinary();
				timestamp = msgdata.get("timestamp").asDate();
				voice = false;
			}
		}
	}

	public class RegionInfoMessage implements IMessage {
		public int parcelLocalID;
		public String regionName;
		public String channelUri;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.RegionInfo;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);
			map.put("parcel_local_id", OSD.fromInteger(parcelLocalID));
			map.put("region_name", OSD.fromString(regionName));
			OSDMap voiceMap = new OSDMap(1);
			voiceMap.put("channel_uri", OSD.fromString(channelUri));
			map.put("voice_credentials", voiceMap);
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			this.parcelLocalID = map.get("parcel_local_id").asInteger();
			this.regionName = map.get("region_name").asString();
			OSDMap voiceMap = (OSDMap) map.get("voice_credentials");
			this.channelUri = voiceMap.get("channel_uri").asString();
		}
	}

	// Sent from the simulator to the viewer.
	//
	// When an agent initially joins a session the AgentUpdatesBlock object will
	// contain a list of session members including
	// a boolean indicating they can use voice chat in this session, a boolean
	// indicating they are allowed to moderate
	// this session, and lastly a string which indicates another agent is
	// entering the session with the Transition set to "ENTER"
	//
	// During the session lifetime updates on individuals are sent. During the
	// update the booleans sent during the initial join are
	// excluded with the exception of the Transition field. This indicates a new
	// user entering or exiting the session with
	// the string "ENTER" or "LEAVE" respectively.
	public class ChatterBoxSessionAgentListUpdatesMessage implements IMessage {
		/*
		 * initial when agent joins session <llsd> <map> <key>events</key> <array> <map>
		 * <key>body</key> <map> <key>agent_updates</key> <map>
		 * <key>32939971-a520-4b52-8ca5-6085d0e39933</key> <map> <key>info</key> <map>
		 * <key>can_voice_chat</key> <boolean>1</boolean> <key>is_moderator</key>
		 * <boolean>1</boolean> </map> <key>transition</key> <string>ENTER</string>
		 * </map> <key>ca00e3e1-0fdb-4136-8ed4-0aab739b29e8</key> <map> <key>info</key>
		 * <map> <key>can_voice_chat</key> <boolean>1</boolean> <key>is_moderator</key>
		 * <boolean>0</boolean> </map> <key>transition</key> <string>ENTER</string>
		 * </map> </map> <key>session_id</key>
		 * <string>be7a1def-bd8a-5043-5d5b-49e3805adf6b</string> <key>updates</key>
		 * <map> <key>32939971-a520-4b52-8ca5-6085d0e39933</key> <string>ENTER</string>
		 * <key>ca00e3e1-0fdb-4136-8ed4-0aab739b29e8</key> <string>ENTER</string> </map>
		 * </map> <key>message</key> <string>ChatterBoxSessionAgentListUpdates</string>
		 * </map> <map> <key>body</key> <map> <key>agent_updates</key> <map>
		 * <key>32939971-a520-4b52-8ca5-6085d0e39933</key> <map> <key>info</key> <map>
		 * <key>can_voice_chat</key> <boolean>1</boolean> <key>is_moderator</key>
		 * <boolean>1</boolean> </map> </map> </map> <key>session_id</key>
		 * <string>be7a1def-bd8a-5043-5d5b-49e3805adf6b</string> <key>updates</key>
		 * <map> </map> </map > <key>message</key>
		 * <string>ChatterBoxSessionAgentListUpdates</string> </map> </array>
		 * <key>id</key> <integer>5</integer> </map> </llsd>
		 *
		 * // a message containing only moderator updates //
		 * <llsd><map><key>events</key><array><map><key>body</key><map><key>
		 * agent_updates</key><map><key>ca00e3e1-0fdb-4136-8ed4-0aab739b29e8</key><map><
		 * key>info</key><map><key>mutes</key><map><key>text</key><boolean>1</boolean></
		 * map></map></map></map><key>session_id</key><string>be7a1def-bd8a-5043-5d5b-
		 * 49e3805adf6b</string><key>updates</key><map //
		 * /></map><key>message</key><string>ChatterBoxSessionAgentListUpdates</string><
		 * /map></array><key>id</key><integer>7</integer></map></llsd>
		 */
		public UUID sessionID;

		public class AgentUpdatesBlock {
			public UUID agentID;

			public boolean canVoiceChat;
			public boolean isModerator;
			// transition "transition" = "ENTER" or "LEAVE"
			public String transition; // TODO: switch to an enum "ENTER" or
										// "LEAVE"

			public boolean muteText;
			public boolean muteVoice;
		}

		public AgentUpdatesBlock[] updates;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ChatterBoxSessionAgentListUpdates;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap();

			OSDMap agent_updatesMap = new OSDMap(1);
			for (int i = 0; i < updates.length; i++) {
				OSDMap mutesMap = new OSDMap(2);
				mutesMap.put("text", OSD.fromBoolean(updates[i].muteText));
				mutesMap.put("voice", OSD.fromBoolean(updates[i].muteVoice));

				OSDMap infoMap = new OSDMap(3);
				infoMap.put("can_voice_chat", OSD.fromBoolean(updates[i].canVoiceChat));
				infoMap.put("is_moderator", OSD.fromBoolean(updates[i].isModerator));
				infoMap.put("mutes", mutesMap);

				OSDMap imap = new OSDMap(2);
				imap.put("info", infoMap);
				imap.put("transition", OSD.fromString(updates[i].transition));

				agent_updatesMap.put(updates[i].agentID.toString(), imap);
			}
			map.put("agent_updates", agent_updatesMap);
			map.put("session_id", OSD.fromUUID(sessionID));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {

			OSDMap agent_updates = (OSDMap) map.get("agent_updates");
			sessionID = map.get("session_id").asUUID();

			List<AgentUpdatesBlock> updatesList = new ArrayList<>();

			for (Entry<String, OSD> kvp : agent_updates.entrySet()) {

				if (kvp.getKey().equals("updates")) {
					// This appears to be redundant and duplicated by the info
					// block, more dumps will confirm this
					// <key>32939971-a520-4b52-8ca5-6085d0e39933</key>
					// <string>ENTER</string>
				} else if (kvp.getKey().equals("session_id")) {
					// I am making the assumption that each osdmap will contain
					// the information for a
					// single session. This is how the map appears to read
					// however more dumps should be taken
					// to confirm this.
					// <key>session_id</key>
					// <string>984f6a1e-4ceb-6366-8d5e-a18c6819c6f7</string>

				} else
				// key is an agent uuid (we hope!)
				{
					// should be the agents uuid as the key, and "info" as the
					// datablock
					// <key>32939971-a520-4b52-8ca5-6085d0e39933</key>
					// <map>
					// <key>info</key>
					// <map>
					// <key>can_voice_chat</key>
					// <boolean>1</boolean>
					// <key>is_moderator</key>
					// <boolean>1</boolean>
					// </map>
					// <key>transition</key>
					// <string>ENTER</string>
					// </map>
					AgentUpdatesBlock block = new AgentUpdatesBlock();
					block.agentID = UUID.parse(kvp.getKey());

					OSDMap infoMap = (OSDMap) kvp.getValue();

					OSDMap agentPermsMap = (OSDMap) infoMap.get("info");

					block.canVoiceChat = agentPermsMap.get("can_voice_chat").asBoolean();
					block.isModerator = agentPermsMap.get("is_moderator").asBoolean();

					block.transition = infoMap.get("transition").asString();

					if (agentPermsMap.containsKey("mutes")) {
						OSDMap mutesMap = (OSDMap) agentPermsMap.get("mutes");
						block.muteText = mutesMap.get("text").asBoolean();
						block.muteVoice = mutesMap.get("voice").asBoolean();
					}
					updatesList.add(block);
				}
			}

			updates = new AgentUpdatesBlock[updatesList.size()];

			for (int i = 0; i < updatesList.size(); i++) {
				AgentUpdatesBlock block = new AgentUpdatesBlock();
				block.agentID = updatesList.get(i).agentID;
				block.canVoiceChat = updatesList.get(i).canVoiceChat;
				block.isModerator = updatesList.get(i).isModerator;
				block.muteText = updatesList.get(i).muteText;
				block.muteVoice = updatesList.get(i).muteVoice;
				block.transition = updatesList.get(i).transition;
				updates[i] = block;
			}
		}
	}

	// An CapsEventQueue message sent when the agent is forcibly removed from a
	// chatterbox session
	public class ForceCloseChatterBoxSessionMessage implements IMessage {
		// A string containing the reason the agent was removed
		public String reason;
		// The ChatterBoxSession's SessionID
		public UUID sessionID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ForceCloseChatterBoxSession;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);
			map.put("reason", OSD.fromString(reason));
			map.put("session_id", OSD.fromUUID(sessionID));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			reason = map.get("reason").asString();
			sessionID = map.get("session_id").asUUID();
		}
	}

	// #endregion

	// #region CapsEventQueue
	// TODO: FIXME
	// Why does this class exist?
	public abstract class EventMessageBlock {
		public abstract OSDMap serialize();

		public abstract void deserialize(OSDMap map);
	}

	public class EventQueueAck extends EventMessageBlock {
		public int ackID;
		public boolean done;

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap();
			map.put("ack", OSD.fromInteger(ackID));
			map.put("done", OSD.fromBoolean(done));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			ackID = map.get("ack").asInteger();
			done = map.get("done").asBoolean();
		}
	}

	public class EventQueueEvent extends EventMessageBlock {
		public class QueueEvent {
			public CapsMessage eventMessage;
			public CapsEventType messageKey;
		}

		public int sequence;
		public QueueEvent[] messageEvents;

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);

			OSDArray eventsArray = new OSDArray();

			for (int i = 0; i < messageEvents.length; i++) {
				OSDMap eventMap = new OSDMap(2);
				eventMap.put("body", messageEvents[i].eventMessage.serialize());
				eventMap.put("message", OSD.fromString(messageEvents[i].messageKey.toString()));
				eventsArray.add(eventMap);
			}

			map.put("events", eventsArray);
			map.put("id", OSD.fromInteger(sequence));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			sequence = map.get("id").asInteger();
			OSDArray arrayEvents = (OSDArray) map.get("events");

			messageEvents = new QueueEvent[arrayEvents.size()];

			for (int i = 0; i < arrayEvents.size(); i++) {
				OSDMap eventMap = (OSDMap) arrayEvents.get(i);
				QueueEvent ev = new QueueEvent();

				ev.messageKey = CapsEventType.valueOf(eventMap.get("message").asString());
				ev.eventMessage = (CapsMessage) decodeEvent(ev.messageKey, (OSDMap) eventMap.get("body"));
				messageEvents[i] = ev;
			}
		}
	}

	public class EventQueueGetMessage implements IMessage {
		public EventMessageBlock messages;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.EventQueueGet;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return messages.serialize();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("ack")) {
				messages = new EventQueueAck();
				messages.deserialize(map);
			} else if (map.containsKey("events")) {
				messages = new EventQueueEvent();
				messages.deserialize(map);
			} else
				logger.warn("Unable to deserialize EventQueueGetMessage: No message handler exists for event");
		}
	}

	// #endregion

	// #region Stats Messages

	public class ViewerStatsMessage implements IMessage {
		public int agentsInView;
		public float agentFPS;
		public String agentLanguage;
		public float agentMemoryUsed;
		public float metersTraveled;
		public float agentPing;
		public int regionsVisited;
		public float agentRuntime;
		public float simulatorFPS;
		public Date agentStartTime;
		public String agentVersion;

		public float objectKbytes;
		public float textureKbytes;
		public float worldKbytes;

		public float miscVersion;
		public boolean vertexBuffersEnabled;

		public UUID sessionID;

		public int statsDropped;
		public int statsFailedResends;
		public int failuresInvalid;
		public int failuresOffCircuit;
		public int failuresResent;
		public int failuresSendPacket;

		public int miscInt1;
		public int miscInt2;
		public String miscString1;

		public int inCompressedPackets;
		public float inKbytes;
		public float inPackets;
		public float inSavings;

		public int outCompressedPackets;
		public float outKbytes;
		public float outPackets;
		public float outSavings;

		public String systemCPU;
		public String systemGPU;
		public int systemGPUClass;
		public String systemGPUVendor;
		public String systemGPUVersion;
		public String systemOS;
		public int systemInstalledRam;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ViewerStats;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(5);
			map.put("session_id", OSD.fromUUID(sessionID));

			OSDMap agentMap = new OSDMap(11);
			agentMap.put("agents_in_view", OSD.fromInteger(agentsInView));
			agentMap.put("fps", OSD.fromReal(agentFPS));
			agentMap.put("language", OSD.fromString(agentLanguage));
			agentMap.put("mem_use", OSD.fromReal(agentMemoryUsed));
			agentMap.put("meters_traveled", OSD.fromReal(metersTraveled));
			agentMap.put("ping", OSD.fromReal(agentPing));
			agentMap.put("regions_visited", OSD.fromInteger(regionsVisited));
			agentMap.put("run_time", OSD.fromReal(agentRuntime));
			agentMap.put("sim_fps", OSD.fromReal(simulatorFPS));
			agentMap.put("start_time", OSD.fromUInteger((int) (long) Helpers.dateTimeToUnixTime(agentStartTime)));
			agentMap.put("version", OSD.fromString(agentVersion));
			map.put("agent", agentMap);

			OSDMap downloadsMap = new OSDMap(3); // downloads
			downloadsMap.put("object_kbytes", OSD.fromReal(objectKbytes));
			downloadsMap.put("texture_kbytes", OSD.fromReal(textureKbytes));
			downloadsMap.put("world_kbytes", OSD.fromReal(worldKbytes));
			map.put("downloads", downloadsMap);

			OSDMap miscMap = new OSDMap(2);
			miscMap.put("Version", OSD.fromReal(miscVersion));
			miscMap.put("Vertex Buffers Enabled", OSD.fromBoolean(vertexBuffersEnabled));
			map.put("misc", miscMap);

			OSDMap statsMap = new OSDMap(2);

			OSDMap failuresMap = new OSDMap(6);
			failuresMap.put("dropped", OSD.fromInteger(statsDropped));
			failuresMap.put("failed_resends", OSD.fromInteger(statsFailedResends));
			failuresMap.put("invalid", OSD.fromInteger(failuresInvalid));
			failuresMap.put("off_circuit", OSD.fromInteger(failuresOffCircuit));
			failuresMap.put("resent", OSD.fromInteger(failuresResent));
			failuresMap.put("send_packet", OSD.fromInteger(failuresSendPacket));
			statsMap.put("failures", failuresMap);

			OSDMap statsMiscMap = new OSDMap(3);
			statsMiscMap.put("int_1", OSD.fromInteger(miscInt1));
			statsMiscMap.put("int_2", OSD.fromInteger(miscInt2));
			statsMiscMap.put("string_1", OSD.fromString(miscString1));
			statsMap.put("misc", statsMiscMap);

			OSDMap netMap = new OSDMap(3);

			// in
			OSDMap netInMap = new OSDMap(4);
			netInMap.put("compressed_packets", OSD.fromInteger(inCompressedPackets));
			netInMap.put("kbytes", OSD.fromReal(inKbytes));
			netInMap.put("packets", OSD.fromReal(inPackets));
			netInMap.put("savings", OSD.fromReal(inSavings));
			netMap.put("in", netInMap);
			// out
			OSDMap netOutMap = new OSDMap(4);
			netOutMap.put("compressed_packets", OSD.fromInteger(outCompressedPackets));
			netOutMap.put("kbytes", OSD.fromReal(outKbytes));
			netOutMap.put("packets", OSD.fromReal(outPackets));
			netOutMap.put("savings", OSD.fromReal(outSavings));
			netMap.put("out", netOutMap);

			statsMap.put("net", netMap);

			// system
			OSDMap systemStatsMap = new OSDMap(7);
			systemStatsMap.put("cpu", OSD.fromString(systemCPU));
			systemStatsMap.put("gpu", OSD.fromString(systemGPU));
			systemStatsMap.put("gpu_class", OSD.fromInteger(systemGPUClass));
			systemStatsMap.put("gpu_vendor", OSD.fromString(systemGPUVendor));
			systemStatsMap.put("gpu_version", OSD.fromString(systemGPUVersion));
			systemStatsMap.put("os", OSD.fromString(systemOS));
			systemStatsMap.put("ram", OSD.fromInteger(systemInstalledRam));
			map.put("system", systemStatsMap);

			map.put("stats", statsMap);
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			sessionID = map.get("session_id").asUUID();

			OSDMap agentMap = (OSDMap) map.get("agent");
			agentsInView = agentMap.get("agents_in_view").asInteger();
			agentFPS = (float) agentMap.get("fps").asReal();
			agentLanguage = agentMap.get("language").asString();
			agentMemoryUsed = (float) agentMap.get("mem_use").asReal();
			metersTraveled = agentMap.get("meters_traveled").asInteger();
			agentPing = (float) agentMap.get("ping").asReal();
			regionsVisited = agentMap.get("regions_visited").asInteger();
			agentRuntime = (float) agentMap.get("run_time").asReal();
			simulatorFPS = (float) agentMap.get("sim_fps").asReal();
			agentStartTime = Helpers.unixTimeToDateTime(agentMap.get("start_time").asUInteger());
			agentVersion = agentMap.get("version").asString();

			OSDMap downloadsMap = (OSDMap) map.get("downloads");
			objectKbytes = (float) downloadsMap.get("object_kbytes").asReal();
			textureKbytes = (float) downloadsMap.get("texture_kbytes").asReal();
			worldKbytes = (float) downloadsMap.get("world_kbytes").asReal();

			OSDMap miscMap = (OSDMap) map.get("misc");
			miscVersion = (float) miscMap.get("Version").asReal();
			vertexBuffersEnabled = miscMap.get("Vertex Buffers Enabled").asBoolean();

			OSDMap statsMap = (OSDMap) map.get("stats");
			OSDMap failuresMap = (OSDMap) statsMap.get("failures");
			statsDropped = failuresMap.get("dropped").asInteger();
			statsFailedResends = failuresMap.get("failed_resends").asInteger();
			failuresInvalid = failuresMap.get("invalid").asInteger();
			failuresOffCircuit = failuresMap.get("off_circuit").asInteger();
			failuresResent = failuresMap.get("resent").asInteger();
			failuresSendPacket = failuresMap.get("send_packet").asInteger();

			OSDMap statsMiscMap = (OSDMap) statsMap.get("misc");
			miscInt1 = statsMiscMap.get("int_1").asInteger();
			miscInt2 = statsMiscMap.get("int_2").asInteger();
			miscString1 = statsMiscMap.get("string_1").asString();
			OSDMap netMap = (OSDMap) statsMap.get("net");
			// in
			OSDMap netInMap = (OSDMap) netMap.get("in");
			inCompressedPackets = netInMap.get("compressed_packets").asInteger();
			inKbytes = netInMap.get("kbytes").asInteger();
			inPackets = netInMap.get("packets").asInteger();
			inSavings = netInMap.get("savings").asInteger();
			// out
			OSDMap netOutMap = (OSDMap) netMap.get("out");
			outCompressedPackets = netOutMap.get("compressed_packets").asInteger();
			outKbytes = netOutMap.get("kbytes").asInteger();
			outPackets = netOutMap.get("packets").asInteger();
			outSavings = netOutMap.get("savings").asInteger();

			// system
			OSDMap systemStatsMap = (OSDMap) map.get("system");
			systemCPU = systemStatsMap.get("cpu").asString();
			systemGPU = systemStatsMap.get("gpu").asString();
			systemGPUClass = systemStatsMap.get("gpu_class").asInteger();
			systemGPUVendor = systemStatsMap.get("gpu_vendor").asString();
			systemGPUVersion = systemStatsMap.get("gpu_version").asString();
			systemOS = systemStatsMap.get("os").asString();
			systemInstalledRam = systemStatsMap.get("ram").asInteger();
		}
	}

	//
	public class PlacesReplyMessage implements IMessage {
		public UUID agentID;
		public UUID queryID;
		public UUID transactionID;

		public class QueryData {
			public int actualArea;
			public int billableArea;
			public String description;
			public float dwell;
			public int flags;
			public float globalX;
			public float globalY;
			public float globalZ;
			public String name;
			public UUID ownerID;
			public String simName;
			public UUID snapShotID;
			public String productSku;
			public int price;
		}

		public QueryData[] queryDataBlocks;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.PlacesReply;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);

			// add the AgentData map
			OSDMap agentIDmap = new OSDMap(2);
			agentIDmap.put("AgentID", OSD.fromUUID(agentID));
			agentIDmap.put("QueryID", OSD.fromUUID(queryID));

			OSDArray agentDataArray = new OSDArray();
			agentDataArray.add(agentIDmap);

			map.put("AgentData", agentDataArray);

			// add the QueryData map
			OSDArray dataBlocksArray = new OSDArray(queryDataBlocks.length);
			for (int i = 0; i < queryDataBlocks.length; i++) {
				OSDMap queryDataMap = new OSDMap(14);
				queryDataMap.put("ActualArea", OSD.fromInteger(queryDataBlocks[i].actualArea));
				queryDataMap.put("BillableArea", OSD.fromInteger(queryDataBlocks[i].billableArea));
				queryDataMap.put("Desc", OSD.fromString(queryDataBlocks[i].description));
				queryDataMap.put("Dwell", OSD.fromReal(queryDataBlocks[i].dwell));
				queryDataMap.put("Flags", OSD.fromInteger(queryDataBlocks[i].flags));
				queryDataMap.put("GlobalX", OSD.fromReal(queryDataBlocks[i].globalX));
				queryDataMap.put("GlobalY", OSD.fromReal(queryDataBlocks[i].globalY));
				queryDataMap.put("GlobalZ", OSD.fromReal(queryDataBlocks[i].globalZ));
				queryDataMap.put("Name", OSD.fromString(queryDataBlocks[i].name));
				queryDataMap.put("OwnerID", OSD.fromUUID(queryDataBlocks[i].ownerID));
				queryDataMap.put("Price", OSD.fromInteger(queryDataBlocks[i].price));
				queryDataMap.put("SimName", OSD.fromString(queryDataBlocks[i].simName));
				queryDataMap.put("SnapshotID", OSD.fromUUID(queryDataBlocks[i].snapShotID));
				queryDataMap.put("ProductSKU", OSD.fromString(queryDataBlocks[i].productSku));
				dataBlocksArray.add(queryDataMap);
			}

			map.put("QueryData", dataBlocksArray);

			// add the TransactionData map
			OSDMap transMap = new OSDMap(1);
			transMap.put("TransactionID", OSD.fromUUID(transactionID));
			OSDArray transArray = new OSDArray();
			transArray.add(transMap);
			map.put("TransactionData", transArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDArray agentDataArray = (OSDArray) map.get("AgentData");

			OSDMap agentDataMap = (OSDMap) agentDataArray.get(0);
			agentID = agentDataMap.get("AgentID").asUUID();
			queryID = agentDataMap.get("QueryID").asUUID();

			OSDArray dataBlocksArray = (OSDArray) map.get("QueryData");
			queryDataBlocks = new QueryData[dataBlocksArray.size()];
			for (int i = 0; i < dataBlocksArray.size(); i++) {
				OSDMap dataMap = (OSDMap) dataBlocksArray.get(i);
				QueryData data = new QueryData();
				data.actualArea = dataMap.get("ActualArea").asInteger();
				data.billableArea = dataMap.get("BillableArea").asInteger();
				data.description = dataMap.get("Desc").asString();
				data.dwell = (float) dataMap.get("Dwell").asReal();
				data.flags = dataMap.get("Flags").asInteger();
				data.globalX = (float) dataMap.get("GlobalX").asReal();
				data.globalY = (float) dataMap.get("GlobalY").asReal();
				data.globalZ = (float) dataMap.get("GlobalZ").asReal();
				data.name = dataMap.get("Name").asString();
				data.ownerID = dataMap.get("OwnerID").asUUID();
				data.price = dataMap.get("Price").asInteger();
				data.simName = dataMap.get("SimName").asString();
				data.snapShotID = dataMap.get("SnapshotID").asUUID();
				data.productSku = dataMap.get("ProductSKU").asString();
				queryDataBlocks[i] = data;
			}

			OSDArray transactionArray = (OSDArray) map.get("TransactionData");
			OSDMap transactionDataMap = (OSDMap) transactionArray.get(0);
			transactionID = transactionDataMap.get("TransactionID").asUUID();
		}
	}

	public class UpdateAgentInformationMessage implements IMessage {
		public String maxAccess; // PG, A, or M

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UpdateAgentInformation;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);
			OSDMap prefsMap = new OSDMap(1);
			prefsMap.put("max", OSD.fromString(maxAccess));
			map.put("access_prefs", prefsMap);
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDMap prefsMap = (OSDMap) map.get("access_prefs");
			maxAccess = prefsMap.get("max").asString();
		}
	}

	public class DirLandReplyMessage implements IMessage {
		public UUID agentID;
		public UUID queryID;

		public class QueryReply {
			public int actualArea;
			public boolean auction;
			public boolean forSale;
			public String name;
			public UUID parcelID;
			public String productSku;
			public int salePrice;
		}

		public QueryReply[] queryReplies;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.DirLandReply;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);

			OSDMap agentMap = new OSDMap(1);
			agentMap.put("AgentID", OSD.fromUUID(agentID));
			OSDArray agentDataArray = new OSDArray(1);
			agentDataArray.add(agentMap);
			map.put("AgentData", agentDataArray);

			OSDMap queryMap = new OSDMap(1);
			queryMap.put("QueryID", OSD.fromUUID(queryID));
			OSDArray queryDataArray = new OSDArray(1);
			queryDataArray.add(queryMap);
			map.put("QueryData", queryDataArray);

			OSDArray queryReplyArray = new OSDArray();
			for (int i = 0; i < queryReplies.length; i++) {
				OSDMap queryReply = new OSDMap(100);
				queryReply.put("ActualArea", OSD.fromInteger(queryReplies[i].actualArea));
				queryReply.put("Auction", OSD.fromBoolean(queryReplies[i].auction));
				queryReply.put("ForSale", OSD.fromBoolean(queryReplies[i].forSale));
				queryReply.put("Name", OSD.fromString(queryReplies[i].name));
				queryReply.put("ParcelID", OSD.fromUUID(queryReplies[i].parcelID));
				queryReply.put("ProductSKU", OSD.fromString(queryReplies[i].productSku));
				queryReply.put("SalePrice", OSD.fromInteger(queryReplies[i].salePrice));

				queryReplyArray.add(queryReply);
			}
			map.put("QueryReplies", queryReplyArray);

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDArray agentDataArray = (OSDArray) map.get("AgentData");
			OSDMap agentDataMap = (OSDMap) agentDataArray.get(0);
			agentID = agentDataMap.get("AgentID").asUUID();

			OSDArray queryDataArray = (OSDArray) map.get("QueryData");
			OSDMap queryDataMap = (OSDMap) queryDataArray.get(0);
			queryID = queryDataMap.get("QueryID").asUUID();

			OSDArray queryRepliesArray = (OSDArray) map.get("QueryReplies");

			queryReplies = new QueryReply[queryRepliesArray.size()];
			for (int i = 0; i < queryRepliesArray.size(); i++) {
				QueryReply reply = new QueryReply();
				OSDMap replyMap = (OSDMap) queryRepliesArray.get(i);
				reply.actualArea = replyMap.get("ActualArea").asInteger();
				reply.auction = replyMap.get("Auction").asBoolean();
				reply.forSale = replyMap.get("ForSale").asBoolean();
				reply.name = replyMap.get("Name").asString();
				reply.parcelID = replyMap.get("ParcelID").asUUID();
				reply.productSku = replyMap.get("ProductSKU").asString();
				reply.salePrice = replyMap.get("SalePrice").asInteger();

				queryReplies[i] = reply;
			}
		}
	}

	// #endregion

	// #region Object Messages

	public class UploadObjectAssetMessage implements IMessage {
		public class Object {
			public class Face {
				public Bumpiness bump;
				public Color4 color;
				public boolean fullbright;
				public float glow;
				public UUID imageID;
				public float imageRot;
				public int mediaFlags;
				public float offsetS;
				public float offsetT;
				public float scaleS;
				public float scaleT;

				public OSDMap serialize() {
					OSDMap map = new OSDMap();
					map.put("bump", OSD.fromInteger(bump.getValue()));
					map.put("colors", OSD.fromColor4(color));
					map.put("fullbright", OSD.fromBoolean(fullbright));
					map.put("glow", OSD.fromReal(glow));
					map.put("imageid", OSD.fromUUID(imageID));
					map.put("imagerot", OSD.fromReal(imageRot));
					map.put("media_flags", OSD.fromInteger(mediaFlags));
					map.put("offsets", OSD.fromReal(offsetS));
					map.put("offsett", OSD.fromReal(offsetT));
					map.put("scales", OSD.fromReal(scaleS));
					map.put("scalet", OSD.fromReal(scaleT));

					return map;
				}

				public Face(OSDMap map) {
					bump = Bumpiness.setValue(map.get("bump").asInteger());
					color = map.get("colors").asColor4();
					fullbright = map.get("fullbright").asBoolean();
					glow = (float) map.get("glow").asReal();
					imageID = map.get("imageid").asUUID();
					imageRot = (float) map.get("imagerot").asReal();
					mediaFlags = map.get("media_flags").asInteger();
					offsetS = (float) map.get("offsets").asReal();
					offsetT = (float) map.get("offsett").asReal();
					scaleS = (float) map.get("scales").asReal();
					scaleT = (float) map.get("scalet").asReal();
				}
			}

			// TODO:FIXME
			// Why is this class not using the interfaces?
			public class ExtraParam {
				public ExtraParamType type;
				public byte[] extraParamData;

				public OSDMap serialize() {
					OSDMap map = new OSDMap();
					map.put("extra_parameter", OSD.fromInteger(type.getValue()));
					map.put("param_data", OSD.fromBinary(extraParamData));

					return map;
				}

				public ExtraParam(OSDMap map) {
					type = ExtraParamType.setValue(map.get("extra_parameter").asInteger());
					extraParamData = map.get("param_data").asBinary();
				}
			}

			public Face[] faces;
			public ExtraParam[] extraParams;
			public UUID groupID;
			public Material material;
			public String name;
			public Vector3 position;
			public Quaternion rotation;
			public Vector3 scale;
			public float pathBegin;
			public int pathCurve;
			public float pathEnd;
			public float radiusOffset;
			public float revolutions;
			public float scaleX;
			public float scaleY;
			public float shearX;
			public float shearY;
			public float skew;
			public float taperX;
			public float taperY;
			public float twist;
			public float twistBegin;
			public float profileBegin;
			public int profileCurve;
			public float profileEnd;
			public float profileHollow;
			public UUID sculptID;
			public SculptType sculptType;

			public OSDMap serialize() {
				OSDMap map = new OSDMap();

				map.put("group-id", OSD.fromUUID(groupID));
				map.put("material", OSD.fromInteger(material.getValue()));
				map.put("name", OSD.fromString(name));
				map.put("pos", OSD.fromVector3(position));
				map.put("rotation", OSD.fromQuaternion(rotation));
				map.put("scale", OSD.fromVector3(scale));

				// Extra params
				OSDArray extraParameters = new OSDArray();
				if (extraParams != null) {
					for (int i = 0; i < extraParams.length; i++)
						extraParameters.add(extraParams[i].serialize());
				}
				map.put("extra_parameters", extraParameters);

				// Faces
				OSDArray facelist = new OSDArray();
				if (faces != null) {
					for (int i = 0; i < faces.length; i++)
						facelist.add(faces[i].serialize());
				}
				map.put("facelist", facelist);

				// Shape
				OSDMap shape = new OSDMap();
				OSDMap path = new OSDMap();
				path.put("begin", OSD.fromReal(pathBegin));
				path.put("curve", OSD.fromInteger(pathCurve));
				path.put("end", OSD.fromReal(pathEnd));
				path.put("radius_offset", OSD.fromReal(radiusOffset));
				path.put("revolutions", OSD.fromReal(revolutions));
				path.put("scale_x", OSD.fromReal(scaleX));
				path.put("scale_y", OSD.fromReal(scaleY));
				path.put("shear_x", OSD.fromReal(shearX));
				path.put("shear_y", OSD.fromReal(shearY));
				path.put("skew", OSD.fromReal(skew));
				path.put("taper_x", OSD.fromReal(taperX));
				path.put("taper_y", OSD.fromReal(taperY));
				path.put("twist", OSD.fromReal(twist));
				path.put("twist_begin", OSD.fromReal(twistBegin));
				shape.put("path", path);
				OSDMap profile = new OSDMap();
				profile.put("begin", OSD.fromReal(profileBegin));
				profile.put("curve", OSD.fromInteger(profileCurve));
				profile.put("end", OSD.fromReal(profileEnd));
				profile.put("hollow", OSD.fromReal(profileHollow));
				shape.put("profile", profile);
				OSDMap sculpt = new OSDMap();
				sculpt.put("id", OSD.fromUUID(sculptID));
				sculpt.put("type", OSD.fromInteger(sculptType.getValue()));
				shape.put("sculpt", sculpt);
				map.put("shape", shape);

				return map;
			}

			public Object(OSDMap map) {
				if (map != null) {
					groupID = map.get("group-id").asUUID();
					material = libomv.primitives.Primitive.Material.setValue(map.get("material").asInteger());
					name = map.get("name").asString();
					position = map.get("pos").asVector3();
					rotation = map.get("rotation").asQuaternion();
					scale = map.get("scale").asVector3();

					// Extra params
					OSDArray extraParameters = (OSDArray) map.get("extra_parameters");
					if (extraParameters != null) {
						extraParams = new ExtraParam[extraParameters.size()];
						for (int i = 0; i < extraParameters.size(); i++) {
							extraParams[i] = new ExtraParam((OSDMap) extraParameters.get(i));
							;
						}
					} else {
						extraParams = new ExtraParam[0];
					}

					// Faces
					OSDArray facelist = (OSDArray) map.get("facelist");
					if (facelist != null) {
						faces = new Face[facelist.size()];
						for (int i = 0; i < facelist.size(); i++) {
							faces[i] = new Face((OSDMap) facelist.get(i));
						}
					} else {
						faces = new Face[0];
					}

					// Shape
					OSDMap shape = (OSDMap) map.get("shape");
					OSDMap path = (OSDMap) shape.get("path");
					pathBegin = (float) path.get("begin").asReal();
					pathCurve = path.get("curve").asInteger();
					pathEnd = (float) path.get("end").asReal();
					radiusOffset = (float) path.get("radius_offset").asReal();
					revolutions = (float) path.get("revolutions").asReal();
					scaleX = (float) path.get("scale_x").asReal();
					scaleY = (float) path.get("scale_y").asReal();
					shearX = (float) path.get("shear_x").asReal();
					shearY = (float) path.get("shear_y").asReal();
					skew = (float) path.get("skew").asReal();
					taperX = (float) path.get("taper_x").asReal();
					taperY = (float) path.get("taper_y").asReal();
					twist = (float) path.get("twist").asReal();
					twistBegin = (float) path.get("twist_begin").asReal();

					OSDMap profile = (OSDMap) shape.get("profile");
					profileBegin = (float) profile.get("begin").asReal();
					profileCurve = profile.get("curve").asInteger();
					profileEnd = (float) profile.get("end").asReal();
					profileHollow = (float) profile.get("hollow").asReal();

					OSDMap sculpt = (OSDMap) shape.get("sculpt");
					if (sculpt != null) {
						sculptID = sculpt.get("id").asUUID();
						sculptType = libomv.primitives.Primitive.SculptType.setValue(sculpt.get("type").asInteger());
					} else {
						sculptID = UUID.ZERO;
						sculptType = libomv.primitives.Primitive.SculptType.None;
					}
				}
			}
		}

		public Object[] objects;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.UploadObjectAsset;
		}

		/**
		 * Serializes the message
		 *
		 * @returns Serialized OSD
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap();
			OSDArray array = new OSDArray();

			if (objects != null) {
				for (int i = 0; i < objects.length; i++)
					array.add(objects[i].serialize());
			}

			map.put("objects", array);
			return map;
		}

		@Override
		public void deserialize(OSDMap map) {
			OSDArray array = (OSDArray) map.get("objects");

			if (array != null) {
				objects = new Object[array.size()];

				for (int i = 0; i < array.size(); i++) {
					objects[i] = new Object((OSDMap) array.get(i));
				}
			} else {
				objects = new Object[0];
			}
		}
	}

	// Event Queue message describing physics engine attributes of a list of
	// objects
	// Sim sends these when object is selected
	public class ObjectPhysicsPropertiesMessage implements IMessage {
		// Array with the list of physics properties
		public PhysicsProperties[] objectPhysicsProperties;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ObjectPhysicsProperties;
		}

		/**
		 * Serializes the message
		 *
		 * @returns Serialized OSD
		 */
		@Override
		public OSDMap serialize() {
			OSDMap ret = new OSDMap(1);
			OSDArray array = new OSDArray(objectPhysicsProperties.length);

			for (int i = 0; i < objectPhysicsProperties.length; i++) {
				array.add(objectPhysicsProperties[i].getOSD());
			}
			ret.put("ObjectData", array);
			return ret;

		}

		/**
		 * Deseializes the message
		 *
		 * @param map
		 *            Incoming data to deserialize
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDArray array = (OSDArray) map.get("ObjectData");
			if (array != null) {
				objectPhysicsProperties = new PhysicsProperties[array.size()];

				for (int i = 0; i < array.size(); i++) {
					objectPhysicsProperties[i] = new PhysicsProperties(array.get(i));
				}
			} else {
				objectPhysicsProperties = new PhysicsProperties[0];
			}
		}
	}

	// #endregion Object Messages

	// #region Object Media Messages
	// A message sent from the viewer to the simulator which specifies that the
	// user has changed current URL
	// of the specific media on a prim face
	public class ObjectMediaNavigateMessage implements IMessage {
		// New URL
		public String url;

		// Prim UUID where navigation occurred
		public UUID primID;

		// Face index
		public int face;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ObjectMediaNavigate;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(3);

			map.put("current_url", OSD.fromString(url));
			map.put("object_id", OSD.fromUUID(primID));
			map.put("texture_index", OSD.fromInteger(face));

			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			url = map.get("current_url").asString();
			primID = map.get("object_id").asUUID();
			face = map.get("texture_index").asInteger();
		}
	}

	// Base class used for the ObjectMedia message
	// TODO: FIXME Why does this class exist?
	public abstract class ObjectMediaBlock {
		public abstract OSDMap serialize();

		public abstract void deserialize(OSDMap map);
	}

	// Message used to retrive prim media data
	public class ObjectMediaRequest extends ObjectMediaBlock {
		// Prim UUID
		public UUID primID;

		//
		// Requested operation, either GET or UPDATE
		public String verb = "GET"; // "GET" or "UPDATE"

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);
			map.put("object_id", OSD.fromUUID(primID));
			map.put("verb", OSD.fromString(verb));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			primID = map.get("object_id").asUUID();
			verb = map.get("verb").asString();
		}
	}

	// Message used to update prim media data
	public class ObjectMediaResponse extends ObjectMediaBlock {
		// Prim UUID
		public UUID primID;

		// Array of media entries indexed by face number
		public MediaEntry[] faceMedia;

		// Media version string
		public String version; // String in this format:
								// x-mv:0000000016/00000000-0000-0000-0000-000000000000

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);
			map.put("object_id", OSD.fromUUID(primID));

			if (faceMedia == null) {
				map.put("object_media_data", new OSDArray());
			} else {
				OSDArray mediaData = new OSDArray(faceMedia.length);

				for (int i = 0; i < faceMedia.length; i++) {
					if (faceMedia[i] == null)
						mediaData.add(new OSD());
					else
						mediaData.add(faceMedia[i].serialize());
				}

				map.put("object_media_data", mediaData);
			}

			map.put("object_media_version", OSD.fromString(version));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			primID = map.get("object_id").asUUID();

			if (map.get("object_media_data").getType() == OSDType.Array) {
				OSDArray mediaData = (OSDArray) map.get("object_media_data");
				if (mediaData.size() > 0) {
					faceMedia = new MediaEntry[mediaData.size()];
					for (int i = 0; i < mediaData.size(); i++) {
						if (mediaData.get(i).getType() == OSDType.Map) {
							faceMedia[i] = new MediaEntry(mediaData.get(i));
						}
					}
				}
			}
			version = map.get("object_media_version").asString();
		}
	}

	// Message used to update prim media data
	public class ObjectMediaUpdate extends ObjectMediaBlock {
		// Prim UUID
		public UUID primID;

		// Array of media entries indexed by face number
		public MediaEntry[] faceMedia;

		// Requested operation, either GET or UPDATE
		public String verb = "UPDATE"; // "GET" or "UPDATE"

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(2);
			map.put("object_id", OSD.fromUUID(primID));

			if (faceMedia == null) {
				map.put("object_media_data", new OSDArray());
			} else {
				OSDArray mediaData = new OSDArray(faceMedia.length);

				for (int i = 0; i < faceMedia.length; i++) {
					if (faceMedia[i] == null)
						mediaData.add(new OSD());
					else
						mediaData.add(faceMedia[i].serialize());
				}

				map.put("object_media_data", mediaData);
			}

			map.put("verb", OSD.fromString(verb));
			return map;
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			primID = map.get("object_id").asUUID();

			if (map.get("object_media_data").getType() == OSDType.Array) {
				OSDArray mediaData = (OSDArray) map.get("object_media_data");
				if (mediaData.size() > 0) {
					faceMedia = new MediaEntry[mediaData.size()];
					for (int i = 0; i < mediaData.size(); i++) {
						if (mediaData.get(i).getType() == OSDType.Map) {
							faceMedia[i] = new MediaEntry(mediaData.get(i));
						}
					}
				}
			}
			verb = map.get("verb").asString();
		}
	}

	// Message for setting or getting per face MediaEntry
	public class ObjectMediaMessage implements IMessage {
		// The request or response details block
		public ObjectMediaBlock request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ObjectMedia;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return request.serialize();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("verb")) {
				if (map.get("verb").asString() == "GET") {
					request = new ObjectMediaRequest();
					request.deserialize(map);
				} else if (map.get("verb").asString() == "UPDATE") {
					request = new ObjectMediaUpdate();
					request.deserialize(map);
				}
			} else if (map.containsKey("object_media_version")) {
				request = new ObjectMediaResponse();
				request.deserialize(map);
			} else
				logger.warn(
						"Unable to deserialize ObjectMedia: No message handler exists for method: " + map.asString());
		}
	}

	public class RenderMaterialsMessage implements IMessage {
		public OSD materialData;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.ObjectMedia;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			return new OSDMap();
		}

		/**
		 * Deserialize the message
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			try {
				materialData = Helpers.zDecompressOSD(new ByteArrayInputStream(map.get("Zipped").asBinary()));
			} catch (Exception ex) {
				logger.warn("Failed to decode RenderMaterials message:", ex);
				materialData = new OSDMap();
			}
		}
	}

	abstract class GetObjectInfoRequest implements IMessage {
		// Object IDs for which to request cost information
		public UUID[] objectIDs;

		/**
		 * Deserializes the message
		 *
		 * @param map
		 *            Incoming data to deserialize
		 */
		@Override
		public void deserialize(OSDMap map) {
			OSDArray array = (OSDArray) map.get("object_ids");
			if (array != null) {
				objectIDs = new UUID[array.size()];

				for (int i = 0; i < array.size(); i++) {
					objectIDs[i] = array.get(i).asUUID();
				}
			} else {
				objectIDs = new UUID[0];
			}
		}

		/**
		 * Serializes the message
		 *
		 * @returns Serialized OSD
		 */
		@Override
		public OSDMap serialize() {
			OSDMap ret = new OSDMap();
			OSDArray array = new OSDArray();

			for (int i = 0; i < objectIDs.length; i++) {
				array.add(OSD.fromUUID(objectIDs[i]));
			}

			ret.put("object_ids", array);
			return ret;
		}
	}

	public class GetObjectCostRequest extends GetObjectInfoRequest {
		@Override
		public CapsEventType getType() {
			return CapsEventType.GetObjectCost;
		}
	}

	public class GetObjectCostMessage implements IMessage {
		class ObjectCost {
			public UUID objectID;
			public double linkCost;
			public double objectCost;
			public double physicsCost;
			public double linkPhysicsCost;
		}

		public ObjectCost[] objectCosts;

		@Override
		public CapsEventType getType() {
			return CapsEventType.GetObjectCost;
		}

		/**
		 * Deserializes the message
		 *
		 * @param map
		 *            Incoming data to deserialize
		 */
		public void deserialize(OSDMap map) {
			int i = 0;
			objectCosts = new ObjectCost[map.size()];

			for (String key : map.keySet()) {
				ObjectCost cost = new ObjectCost();
				OSDMap values = (OSDMap) map.get(key);
				cost.objectID = UUID.parse(key);

				cost.linkCost = values.get("linked_set_resource_cost").asReal();
				cost.objectCost = values.get("resource_cost").asReal();
				cost.physicsCost = values.get("physics_cost").asReal();
				cost.linkPhysicsCost = values.get("linked_set_physics_cost").asReal();
				// value["resource_limiting_type"].AsString();
				objectCosts[i++] = cost;
			}
		}

		/**
		 * Serializes the message
		 *
		 * @returns Serialized OSD
		 */
		public OSDMap serialize() {
			OSDMap map = new OSDMap(objectCosts.length);
			for (ObjectCost cost : objectCosts) {
				OSDMap values = new OSDMap(4);
				values.put("linked_set_resource_cost", OSD.fromReal(cost.linkCost));
				values.put("resource_cost", OSD.fromReal(cost.objectCost));
				values.put("physics_cost", OSD.fromReal(cost.physicsCost));
				values.put("linked_set_physics_cost", OSD.fromReal(cost.linkPhysicsCost));

				map.put(cost.objectID.toString(), values);
			}
			return map;
		}

	}

	public class GetObjectPhysicsDataRequest extends GetObjectInfoRequest {
		@Override
		public CapsEventType getType() {
			return CapsEventType.GetObjectPhysicsData;
		}
	}

	public class GetObjectPhysicsDataMessage implements IMessage {
		class ObjectPhysics {
			public UUID objectID;
			public int shapeType;
			public double density;
			public double friction;
			public double restitution;
			public double gravityMultiplier;
		}

		public ObjectPhysics[] objectPhysics;

		@Override
		public CapsEventType getType() {
			return CapsEventType.GetObjectPhysicsData;
		}

		/**
		 * Deserializes the message
		 *
		 * @param map
		 *            Incoming data to deserialize
		 */
		public void deserialize(OSDMap map) {
			int i = 0;
			objectPhysics = new ObjectPhysics[map.size()];

			for (String key : map.keySet()) {
				ObjectPhysics physics = new ObjectPhysics();
				OSDMap values = (OSDMap) map.get(key);
				physics.objectID = UUID.parse(key);

				physics.shapeType = values.get("PhysicsShapeType").asInteger();
				if (values.containsKey("Density")) {
					physics.density = values.get("Density").asReal();
					physics.friction = values.get("Friction").asReal();
					physics.restitution = values.get("Restitution").asReal();
					physics.gravityMultiplier = values.get("GravityMultiplier").asReal();
					objectPhysics[i++] = physics;
				}
			}
		}

		/**
		 * Serializes the message
		 *
		 * @returns Serialized OSD
		 */
		public OSDMap serialize() {
			OSDMap map = new OSDMap(objectPhysics.length);
			for (ObjectPhysics physics : objectPhysics) {
				OSDMap values = new OSDMap(4);
				values.put("PhysicsShapeType", OSD.fromReal(physics.shapeType));
				values.put("Density", OSD.fromReal(physics.density));
				values.put("Friction", OSD.fromReal(physics.friction));
				values.put("Restitution", OSD.fromReal(physics.restitution));
				values.put("GravityMultiplier", OSD.fromReal(physics.gravityMultiplier));

				map.put(physics.objectID.toString(), values);
			}
			return map;
		}
	}

	// #endregion Object Media Messages

	// #region Resource usage

	// Details about object resource usage
	// TODO:FIXME
	// No interface, but has the methods ?!?
	public class ObjectResourcesDetail {
		// Object UUID
		public UUID objectID;
		// Object name
		public String name;
		// Indicates if object is group owned
		public boolean groupOwned;
		// Locatio of the object
		public Vector3d location;
		// Object owner
		public UUID ownerID;
		// Resource usage, keys are resource names, values are resource usage
		// for that specific resource
		public Map<String, Integer> resources;

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		public OSDMap serialize() {
			throw new UnsupportedOperationException();
		}

		/**
		 * Deserializes object from OSD
		 *
		 * @param obj
		 *            An <see cref="OSDMap"/> containing the data
		 */
		public void deserialize(OSDMap obj) {
			objectID = obj.get("id").asUUID();
			name = obj.get("name").asString();
			location = obj.get("location").asVector3d();
			groupOwned = obj.get("is_group_owned").asBoolean();
			ownerID = obj.get("owner_id").asUUID();

			OSDMap resourcesOSD = (OSDMap) obj.get("resources");
			resources = new HashMap<>(resourcesOSD.size());
			for (Entry<String, OSD> kvp : resourcesOSD.entrySet()) {
				resources.put(kvp.getKey(), kvp.getValue().asInteger());
			}
		}
	}

	// Details about parcel resource usage
	// TODO:FIXME
	// No interface, but has the methods ?!?
	public class ParcelResourcesDetail {
		// Parcel UUID
		public UUID parcelID;
		// Parcel local ID
		public int localID;
		// Parcel name
		public String name;
		// Indicates if parcel is group owned
		public boolean groupOwned;
		// Parcel owner
		public UUID ownerID;
		// Array of <see cref="ObjectResourcesDetail"/> containing per object
		// resource usage
		public ObjectResourcesDetail[] objects;

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		public OSDMap serialize() {
			throw new UnsupportedOperationException();
		}

		/**
		 * Deserializes object from OSD
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		public void deserialize(OSDMap map) {
			parcelID = map.get("id").asUUID();
			localID = map.get("local_id").asInteger();
			name = map.get("name").asString();
			groupOwned = map.get("is_group_owned").asBoolean();
			ownerID = map.get("owner_id").asUUID();

			OSDArray objectsOSD = (OSDArray) map.get("objects");
			objects = new ObjectResourcesDetail[objectsOSD.size()];

			for (int i = 0; i < objectsOSD.size(); i++) {
				objects[i] = new ObjectResourcesDetail();
				objects[i].deserialize((OSDMap) objectsOSD.get(i));
			}
		}
	}

	// Resource usage base class, both agent and parcel resource usage contains
	// summary information
	public abstract class BaseResourcesInfo implements IMessage {
		// Summary of available resources, keys are resource names, values are
		// resource usage for that specific resource
		public Map<String, Integer> summaryAvailable;
		// Summary resource usage, keys are resource names, values are resource
		// usage for that specific resource
		public Map<String, Integer> summaryUsed;

		@Override
		public OSDMap serialize() {
			throw new UnsupportedOperationException();
		}

		/**
		 * Deserializes object from OSD
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			summaryAvailable = new HashMap<>();
			summaryUsed = new HashMap<>();

			OSDMap summary = (OSDMap) map.get("summary");
			OSDArray available = (OSDArray) summary.get("available");
			OSDArray used = (OSDArray) summary.get("used");

			for (int i = 0; i < available.size(); i++) {
				OSDMap limit = (OSDMap) available.get(i);
				summaryAvailable.put(limit.get("type").asString(), limit.get("amount").asInteger());
			}

			for (int i = 0; i < used.size(); i++) {
				OSDMap limit = (OSDMap) used.get(i);
				summaryUsed.put(limit.get("type").asString(), limit.get("amount").asInteger());
			}
		}
	}

	public class AttachmentResourcesMessage extends BaseResourcesInfo {
		BaseResourcesInfo summaryInfoBlock;

		// Per attachment point object resource usage
		public Map<AttachmentPoint, ObjectResourcesDetail[]> attachments;

		@Override
		public CapsEventType getType() {
			return CapsEventType.AttachmentResources;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = super.serialize();

			return map;
		}

		/**
		 * Deserializes object from OSD
		 *
		 * @param osd
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map != null) {
				super.deserialize(map);
				OSDArray attachmentsOSD = (OSDArray) map.get("attachments");
				attachments = new HashMap<>();

				for (int i = 0; i < attachmentsOSD.size(); i++) {
					OSDMap attachment = (OSDMap) attachmentsOSD.get(i);
					AttachmentPoint pt = AttachmentPoint.setValue(attachment.get("location").asString());

					OSDArray objectsOSD = (OSDArray) attachment.get("objects");
					ObjectResourcesDetail[] objects = new ObjectResourcesDetail[objectsOSD.size()];

					for (int j = 0; j < objects.length; j++) {
						objects[j] = new ObjectResourcesDetail();
						objects[j].deserialize((OSDMap) objectsOSD.get(j));
					}

					attachments.put(pt, objects);
				}
			}
		}
	}

	// Request message for parcel resource usage
	public class LandResourcesRequest implements IMessage {
		// UUID of the parel to request resource usage info
		public UUID parcelID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.LandResources;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);
			map.put("parcel_id", OSD.fromUUID(parcelID));
			return map;
		}

		/**
		 * Deserializes object from OSD
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			parcelID = map.get("parcel_id").asUUID();
		}
	}

	public class LandResourcesMessage implements IMessage {
		// URL where parcel resource usage details can be retrieved
		public URI scriptResourceDetails;
		// URL where parcel resource usage summary can be retrieved
		public URI scriptResourceSummary;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType() {
			return CapsEventType.LandResources;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap();
			if (scriptResourceSummary != null) {
				map.put("ScriptResourceSummary", OSD.fromString(scriptResourceSummary.toString()));
			}

			if (scriptResourceDetails != null) {
				map.put("ScriptResourceDetails", OSD.fromString(scriptResourceDetails.toString()));
			}
			return map;
		}

		/**
		 * Deserializes object from OSD
		 *
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			try {
				if (map.containsKey("ScriptResourceSummary")) {
					scriptResourceSummary = new URI(map.get("ScriptResourceSummary").asString());
				}
				if (map.containsKey("ScriptResourceDetails")) {
					scriptResourceDetails = new URI(map.get("ScriptResourceDetails").asString());
				}
			} catch (URISyntaxException e) {
			}
		}
	}

	// Parcel resource usage
	public class LandResourcesInfo extends BaseResourcesInfo {
		// Array of <see cref="ParcelResourcesDetail"/> containing per percal
		// resource usage
		public ParcelResourcesDetail[] parcels;

		@Override
		public CapsEventType getType() {
			// TODO Auto-generated method stub
			return CapsEventType.LandResources;
		}

		/**
		 * Serialize the object
		 *
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap serialize() {
			OSDMap map = new OSDMap(1);
			if (parcels != null) {
				OSDArray parcelsOSD = new OSDArray(parcels.length);
				for (int i = 0; i < parcels.length; i++) {
					parcelsOSD.add(parcels[i].serialize());
				}
				map.put("parcels", parcelsOSD);
			}
			return map;
		}

		/**
		 * Deserializes object from OSD
		 *
		 * @param osd
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void deserialize(OSDMap map) {
			if (map.containsKey("summary")) {
				super.deserialize(map);
			} else if (map.containsKey("parcels")) {
				OSDArray parcelsOSD = (OSDArray) map.get("parcels");
				parcels = new ParcelResourcesDetail[parcelsOSD.size()];
				for (int i = 0; i < parcelsOSD.size(); i++) {
					parcels[i] = new ParcelResourcesDetail();
					parcels[i].deserialize((OSDMap) parcelsOSD.get(i));
				}
			}
		}
	}

	// #endregion Resource usage

	// #region Display names

	// Reply to request for bunch if display names
	public class GetDisplayNamesMessage implements IMessage {
		// Current display name
		public AgentDisplayName[] agents = new AgentDisplayName[0];

		// Following UUIDs failed to return a valid display name
		public UUID[] badIDs = new UUID[0];

		@Override
		public CapsEventType getType() {
			return CapsEventType.GetDisplayNames;
		}

		/**
		 * Serializes the message
		 *
		 * @returns OSD containting the messaage
		 */
		@Override
		public OSDMap serialize() {
			OSDArray agentsOSD = new OSDArray();

			if (agents != null && agents.length > 0) {
				for (int i = 0; i < agents.length; i++) {
					agentsOSD.add(agents[i].toOSD());
				}
			}

			OSDArray badIDsOSD = new OSDArray();
			if (badIDs != null && badIDs.length > 0) {
				for (int i = 0; i < badIDs.length; i++) {
					badIDsOSD.add(OSD.fromUUID(badIDs[i]));
				}
			}

			OSDMap ret = new OSDMap();
			ret.put("agents", agentsOSD);
			ret.put("bad_ids", badIDsOSD);
			return ret;
		}

		@Override
		public void deserialize(OSDMap map) {
			if (map.get("agents").getType() == OSDType.Array) {
				OSDArray agentsOSD = (OSDArray) map.get("agents");

				if (agentsOSD.size() > 0) {
					agents = new AgentDisplayName[agentsOSD.size()];

					for (int i = 0; i < agentsOSD.size(); i++) {
						agents[i].fromOSD(agentsOSD.get(i));
					}
				}
			}

			if (map.get("bad_ids").getType() == OSDType.Array) {
				OSDArray badIDsOSD = (OSDArray) map.get("bad_ids");
				if (badIDsOSD.size() > 0) {
					badIDs = new UUID[badIDsOSD.size()];

					for (int i = 0; i < badIDsOSD.size(); i++) {
						badIDs[i] = badIDsOSD.get(i).asUUID();
					}
				}
			}
		}
	}

	// Message sent when requesting change of the display name
	public class SetDisplayNameMessage implements IMessage {
		// Current display name
		public String oldDisplayName;

		// Desired new display name
		public String newDisplayName;

		@Override
		public CapsEventType getType() {
			return CapsEventType.SetDisplayName;
		}

		/**
		 * Serializes the message
		 *
		 * @returns OSD containting the messaage
		 */
		@Override
		public OSDMap serialize() {
			OSDArray names = new OSDArray(2);
			names.add(OSD.fromString(oldDisplayName));
			names.add(OSD.fromString(newDisplayName));

			OSDMap name = new OSDMap();
			name.put("display_name", names);
			return name;
		}

		@Override
		public void deserialize(OSDMap map) {
			OSDArray names = (OSDArray) map.get("display_name");
			oldDisplayName = names.get(0).asString();
			newDisplayName = names.get(1).asString();
		}
	}

	// Message recieved in response to request to change display name
	public class SetDisplayNameReplyMessage implements IMessage {
		// New display name
		public AgentDisplayName displayName;

		// String message indicating the result of the operation
		public String reason;

		// Numerical code of the result, 200 indicates success
		public int status;

		@Override
		public CapsEventType getType() {
			return CapsEventType.SetDisplayNameReply;
		}

		/**
		 * Serializes the message
		 *
		 * @returns OSD containting the messaage
		 */
		@Override
		public OSDMap serialize() {
			OSDMap ret = new OSDMap(3);
			ret.put("content", displayName.toOSD());
			ret.put("reason", OSD.fromString(reason));
			ret.put("status", OSD.fromInteger(status));
			return ret;
		}

		@Override
		public void deserialize(OSDMap map) {
			displayName.fromOSD(map.get("content"));
			reason = map.get("reason").asString();
			status = map.get("status").asInteger();
		}
	}

	// Message recieved when someone nearby changes their display name
	public class DisplayNameUpdateMessage implements IMessage {
		// Previous display name, empty string if default
		public String oldDisplayName;

		// New display name
		public AgentDisplayName displayName;

		@Override
		public CapsEventType getType() {
			return CapsEventType.DisplayNameUpdate;
		}

		/**
		 * Serializes the message
		 *
		 * @returns OSD containting the messaage
		 */
		@Override
		public OSDMap serialize() {
			OSDMap agent = (OSDMap) displayName.toOSD();
			agent.put("old_display_name", OSD.fromString(oldDisplayName));
			OSDMap ret = new OSDMap();
			ret.put("agent", agent);
			return ret;
		}

		@Override
		public void deserialize(OSDMap map) {
			OSDMap agent = (OSDMap) map.get("agent");
			displayName.fromOSD(agent);
			oldDisplayName = agent.get("old_display_name").asString();
		}
	}

	// #endregion Display names

	public IMessage decodeEvent(String eventName, OSDMap map) {
		try {
			CapsEventType eventType = CapsEventType.valueOf(eventName);
			return decodeEvent(eventType, map);
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Return a decoded capabilities message as a strongly typed object
	 *
	 * @param eventType
	 *            The event type enumeration key of the capabilities message
	 * @param map
	 *            An <see cref="OSDMap"/> OSDMap to decode
	 * @return A strongly typed object containing the decoded information from the
	 *         capabilities message, or null if no existing Message object exists
	 *         for the specified event
	 */
	public IMessage decodeEvent(CapsEventType eventType, OSDMap map) {
		IMessage message = null;
		if (map == null)
			return message;

		switch (eventType) {
		case AgentGroupDataUpdate:
			message = new AgentGroupDataUpdateMessage();
			break;
		case AvatarGroupsReply: // OpenSim sends the above with the wrong key
			message = new AgentGroupDataUpdateMessage();
			break;
		case AgentStateUpdate:
			message = new AgentStateUpdateMessage();
			break;
		case ParcelProperties:
			message = new ParcelPropertiesMessage();
			break;
		case ParcelObjectOwnersReply:
			message = new ParcelObjectOwnersReplyMessage();
			break;
		case TeleportFinish:
			message = new TeleportFinishMessage();
			break;
		case EnableSimulator:
			message = new EnableSimulatorMessage();
			break;
		case ParcelPropertiesUpdate:
			message = new ParcelPropertiesUpdateMessage();
			break;
		case EstablishAgentCommunication:
			message = new EstablishAgentCommunicationMessage();
			break;
		case ChatterBoxInvitation:
			message = new ChatterBoxInvitationMessage();
			break;
		case ChatterBoxSessionEventReply:
			message = new ChatterBoxSessionEventReplyMessage();
			break;
		case ChatterBoxSessionStartReply:
			message = new ChatterBoxSessionStartReplyMessage();
			break;
		case ChatterBoxSessionAgentListUpdates:
			message = new ChatterBoxSessionAgentListUpdatesMessage();
			break;
		case RequiredVoiceVersion:
			message = new RequiredVoiceVersionMessage();
			break;
		case MapLayer:
			message = new MapLayerMessage();
			break;
		case ChatSessionRequest:
			message = new ChatSessionRequestMessage();
			break;
		case CopyInventoryFromNotecard:
			message = new CopyInventoryFromNotecardMessage();
			break;
		case ProvisionVoiceAccountRequest:
			message = new ProvisionVoiceAccountRequestMessage();
			break;
		case Viewerstats:
			message = new ViewerStatsMessage();
			break;
		case UpdateAgentLanguage:
			message = new UpdateAgentLanguageMessage();
			break;
		case RemoteParcelRequest:
			message = new RemoteParcelRequestMessage();
			break;
		case UpdateScriptTask:
			message = new UpdateScriptTaskMessage();
			break;
		case UpdateScriptAgent:
			message = new UpdateScriptAgentMessage();
			break;
		case SendPostcard:
			message = new SendPostcardMessage();
			break;
		case UpdateGestureAgentInventory:
			message = new UpdateGestureAgentInventoryMessage();
			break;
		case UpdateNotecardAgentInventory:
			message = new UpdateNotecardAgentInventoryMessage();
			break;
		case LandStatReply:
			message = new LandStatReplyMessage();
			break;
		case ParcelVoiceInfoRequest:
			message = new ParcelVoiceInfoRequestMessage();
			break;
		case ViewerStats:
			message = new ViewerStatsMessage();
			break;
		case EventQueueGet:
			message = new EventQueueGetMessage();
			break;
		case CrossedRegion:
			message = new CrossedRegionMessage();
			break;
		case TeleportFailed:
			message = new TeleportFailedMessage();
			break;
		case PlacesReply:
			message = new PlacesReplyMessage();
			break;
		case UpdateAgentInformation:
			message = new UpdateAgentInformationMessage();
			break;
		case DirLandReply:
			message = new DirLandReplyMessage();
			break;
		case ScriptRunningReply:
			message = new ScriptRunningReplyMessage();
			break;
		case SearchStatRequest:
			message = new SearchStatRequestMessage();
			break;
		case AgentDropGroup:
			message = new AgentDropGroupMessage();
			break;
		case ForceCloseChatterBoxSession:
			message = new ForceCloseChatterBoxSessionMessage();
			break;
		case UploadBakedTexture:
			message = new UploadBakedTextureMessage();
			break;
		case WebFetchInventoryDescendents:
			message = new WebFetchInventoryDescendentsMessage();
			break;
		case RegionInfo:
			message = new RegionInfoMessage();
			break;
		case UploadObjectAsset:
			message = new UploadObjectAssetMessage();
			break;
		case ObjectPhysicsProperties:
			message = new ObjectPhysicsPropertiesMessage();
			break;
		case ObjectMediaNavigate:
			message = new ObjectMediaNavigateMessage();
			break;
		case ObjectMedia:
			message = new ObjectMediaMessage();
			break;
		case AttachmentResources:
			message = new AttachmentResourcesMessage();
			break;
		case LandResources:
			if (map.containsKey("parcel_id")) {
				message = new LandResourcesRequest();
			} else if (map.containsKey("ScriptResourceSummary")) {
				message = new LandResourcesMessage();
			} else if (map.containsKey("summary")) {
				message = new LandResourcesInfo();
			}
			break;
		case ProductInfoRequest:
			message = new ProductInfoRequestMessage();
			break;
		case GetDisplayNames:
			message = new GetDisplayNamesMessage();
			break;
		case SetDisplayName:
			message = new SetDisplayNameMessage();
			break;
		case SetDisplayNameReply:
			message = new SetDisplayNameReplyMessage();
			break;
		case DisplayNameUpdate:
			message = new DisplayNameUpdateMessage();
			break;
		case BulkUpdateInventory:
			message = new BulkUpdateInventoryMessage();
			break;
		case RenderMaterials:
			message = new RenderMaterialsMessage();
			break;
		case GetObjectCost:
			if (map.containsKey("object_ids")) {
				message = new GetObjectCostRequest();
			} else {
				message = new GetObjectCostMessage();
			}
			break;
		case GetObjectPhysicsData:
			if (map.containsKey("object_ids")) {
				message = new GetObjectPhysicsDataRequest();
			} else {
				message = new GetObjectPhysicsDataMessage();
			}
			break;

		// Capabilities TODO:
		case GroupAPIv1:
		case DispatchRegionInfo:
		case EstateChangeInfo:
		case FetchInventoryDescendents:
		case GroupProposalBallot:
		case MapLayerGod:
		case NewFileAgentInventory:
		case RequestTextureDownload:
		case SearchStatTracking:
		case SendUserReport:
		case SendUserReportWithScreenshot:
		case ServerReleaseNotes:
		case StartGroupProposal:
		case UpdateGestureTaskInventory:
		case UpdateNotecardTaskInventory:
		case ViewerStartAuction:
		case UntrustedSimulatorMessage:
		default:
			logger.error("Unimplemented event " + eventType.toString());
		}
		if (message != null)
			message.deserialize(map);
		return message;
	}

	@Override
	public OSDMap serialize() {
		return null;
	}

	@Override
	public void deserialize(OSDMap map) {
	}
}