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
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.inventory.InventoryFolder.FolderType;
import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.model.LLObject.SaleType;
import libomv.model.agent.AgentDisplayName;
import libomv.model.agent.InstantMessageDialog;
import libomv.model.agent.InstantMessageOnline;
import libomv.model.agent.TeleportFlags;
import libomv.model.asset.AssetType;
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
			info.put("AgentID", OSD.FromUUID(agentID));
			info.put("LocationID", OSD.FromInteger(locationID)); // Unused by
																	// the
																	// client
			info.put("RegionHandle", OSD.FromULong(regionHandle));
			info.put("SeedCapability", OSD.FromUri(seedCapability));
			info.put("SimAccess", OSD.FromInteger(simAccess.getValue()));
			info.put("SimIP", OSD.FromBinary(ip.getAddress()));
			info.put("SimPort", OSD.FromInteger(port));
			info.put("TeleportFlags", OSD.FromInteger(TeleportFlags.getValue(flags)));

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

			agentID = blockMap.get("AgentID").AsUUID();
			locationID = blockMap.get("LocationID").AsInteger();
			regionHandle = blockMap.get("RegionHandle").AsULong();
			seedCapability = blockMap.get("SeedCapability").AsUri();
			simAccess = libomv.model.simulator.SimAccess.setValue(blockMap.get("SimAccess").AsInteger());
			ip = blockMap.get("SimIP").AsInetAddress();
			port = blockMap.get("SimPort").AsInteger();
			flags = TeleportFlags.setValue(blockMap.get("TeleportFlags").AsUInteger());
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
			map.put("agent-id", OSD.FromUUID(agentID));
			map.put("sim-ip-and-port", OSD.FromString(String.format("%s:%d", address.getHostAddress(), port)));
			map.put("seed-capability", OSD.FromUri(seedCapability));
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
			String ipAndPort = map.get("sim-ip-and-port").AsString();
			int i = ipAndPort.indexOf(':');

			agentID = map.get("agent-id").AsUUID();
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
			seedCapability = map.get("seed-capability").AsUri();
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
			infoMap.put("LookAt", OSD.FromVector3(lookAt));
			infoMap.put("Position", OSD.FromVector3(position));
			infoArray.add(infoMap);
			map.put("Info", infoArray);

			OSDArray agentDataArray = new OSDArray(1);
			OSDMap agentDataMap = new OSDMap(2);
			agentDataMap.put("AgentID", OSD.FromUUID(agentID));
			agentDataMap.put("SessionID", OSD.FromUUID(sessionID));
			agentDataArray.add(agentDataMap);
			map.put("AgentData", agentDataArray);

			OSDArray regionDataArray = new OSDArray(1);
			OSDMap regionDataMap = new OSDMap(4);
			regionDataMap.put("RegionHandle", OSD.FromULong(regionHandle));
			regionDataMap.put("SeedCapability", OSD.FromUri(seedCapability));
			regionDataMap.put("SimIP", OSD.FromBinary(ip.getAddress()));
			regionDataMap.put("SimPort", OSD.FromInteger(port));
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
			lookAt = infoMap.get("LookAt").AsVector3();
			position = infoMap.get("Position").AsVector3();

			OSDMap agentDataMap = (OSDMap) ((OSDArray) map.get("AgentData")).get(0);
			agentID = agentDataMap.get("AgentID").AsUUID();
			sessionID = agentDataMap.get("SessionID").AsUUID();

			OSDMap regionDataMap = (OSDMap) ((OSDArray) map.get("RegionData")).get(0);
			regionHandle = regionDataMap.get("RegionHandle").AsULong();
			seedCapability = regionDataMap.get("SeedCapability").AsUri();
			ip = regionDataMap.get("SimIP").AsInetAddress();
			port = regionDataMap.get("SimPort").AsInteger();
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
				blockMap.put("Handle", OSD.FromULong(block.regionHandle));
				blockMap.put("IP", OSD.FromBinary(block.ip));
				blockMap.put("Port", OSD.FromInteger(block.port));
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
				block.regionHandle = blockMap.get("Handle").AsULong();
				block.ip = blockMap.get("IP").AsInetAddress();
				block.port = blockMap.get("Port").AsInteger();
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

			alertInfoMap.put("ExtraParams", OSD.FromString(extraParams));
			alertInfoMap.put("Message", OSD.FromString(messageKey));
			OSDArray alertArray = new OSDArray();
			alertArray.add(alertInfoMap);
			map.put("AlertInfo", alertArray);

			OSDMap infoMap = new OSDMap(2);
			infoMap.put("AgentID", OSD.FromUUID(agentID));
			infoMap.put("Reason", OSD.FromString(reason));
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
			extraParams = alertInfoMap.get("ExtraParams").AsString();
			messageKey = alertInfoMap.get("Message").AsString();

			OSDArray infoArray = (OSDArray) map.get("Info");
			OSDMap infoMap = (OSDMap) infoArray.get(0);
			agentID = infoMap.get("AgentID").AsUUID();
			reason = infoMap.get("Reason").AsString();
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
			requestDataMap.put("ReportType", OSD.FromUInteger(this.reportType));
			requestDataMap.put("RequestFlags", OSD.FromUInteger(this.requestFlags));
			requestDataMap.put("TotalObjectCount", OSD.FromUInteger(this.totalObjectCount));

			OSDArray requestDatArray = new OSDArray();
			requestDatArray.add(requestDataMap);
			map.put("RequestData", requestDatArray);

			OSDArray reportDataArray = new OSDArray();
			OSDArray dataExtendedArray = new OSDArray();
			for (int i = 0; i < reportDataBlocks.length; i++) {
				OSDMap reportMap = new OSDMap(8);
				reportMap.put("LocationX", OSD.FromReal(reportDataBlocks[i].location.X));
				reportMap.put("LocationY", OSD.FromReal(reportDataBlocks[i].location.Y));
				reportMap.put("LocationZ", OSD.FromReal(reportDataBlocks[i].location.Z));
				reportMap.put("OwnerName", OSD.FromString(reportDataBlocks[i].ownerName));
				reportMap.put("Score", OSD.FromReal(reportDataBlocks[i].score));
				reportMap.put("TaskID", OSD.FromUUID(reportDataBlocks[i].taskID));
				reportMap.put("TaskLocalID", OSD.FromReal(reportDataBlocks[i].taskLocalID));
				reportMap.put("TaskName", OSD.FromString(reportDataBlocks[i].taskName));
				reportDataArray.add(reportMap);

				OSDMap extendedMap = new OSDMap(2);
				extendedMap.put("MonoScore", OSD.FromReal(reportDataBlocks[i].monoScore));
				extendedMap.put("TimeStamp", OSD.FromDate(reportDataBlocks[i].timeStamp));
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

			this.reportType = requestMap.get("ReportType").AsUInteger();
			this.requestFlags = requestMap.get("RequestFlags").AsUInteger();
			this.totalObjectCount = requestMap.get("TotalObjectCount").AsUInteger();

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
				block.location = new Vector3((float) blockMap.get("LocationX").AsReal(),
						(float) blockMap.get("LocationY").AsReal(), (float) blockMap.get("LocationZ").AsReal());
				block.ownerName = blockMap.get("OwnerName").AsString();
				block.score = (float) blockMap.get("Score").AsReal();
				block.taskID = blockMap.get("TaskID").AsUUID();
				block.taskLocalID = blockMap.get("TaskLocalID").AsUInteger();
				block.taskName = blockMap.get("TaskName").AsString();
				block.monoScore = (float) extMap.get("MonoScore").AsReal();
				block.timeStamp = Helpers.UnixTimeToDateTime(extMap.get("TimeStamp").AsUInteger());

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
		public PrimOwner[] PrimOwnersBlock;

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
			OSDArray dataArray = new OSDArray(PrimOwnersBlock.length);
			OSDArray dataExtendedArray = new OSDArray();

			for (int i = 0; i < PrimOwnersBlock.length; i++) {
				OSDMap dataMap = new OSDMap(4);
				dataMap.put("OwnerID", OSD.FromUUID(PrimOwnersBlock[i].ownerID));
				dataMap.put("Count", OSD.FromInteger(PrimOwnersBlock[i].count));
				dataMap.put("IsGroupOwned", OSD.FromBoolean(PrimOwnersBlock[i].isGroupOwned));
				dataMap.put("OnlineStatus", OSD.FromBoolean(PrimOwnersBlock[i].onlineStatus));
				dataArray.add(dataMap);

				OSDMap dataExtendedMap = new OSDMap(1);
				dataExtendedMap.put("TimeStamp", OSD.FromDate(PrimOwnersBlock[i].timeStamp));
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

			PrimOwnersBlock = new PrimOwner[dataArray.size()];

			for (int i = 0; i < dataArray.size(); i++) {
				OSDMap dataMap = (OSDMap) dataArray.get(i);
				PrimOwner block = new PrimOwner();
				block.ownerID = dataMap.get("OwnerID").AsUUID();
				block.count = dataMap.get("Count").AsInteger();
				block.isGroupOwned = dataMap.get("IsGroupOwned").AsBoolean();
				block.onlineStatus = dataMap.get("OnlineStatus").AsBoolean(); // deprecated

				// if the agent has no permissions, or there are no prims, the
				// counts
				// should not match up, so we don't decode the DataExtended map
				if (dataExtendedArray.size() == dataArray.size()) {
					OSDMap dataExtendedMap = (OSDMap) dataExtendedArray.get(i);
					block.timeStamp = Helpers.UnixTimeToDateTime(dataExtendedMap.get("TimeStamp").AsUInteger());
				}
				PrimOwnersBlock[i] = block;
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
			parcelDataMap.put("LocalID", OSD.FromInteger(localID));
			parcelDataMap.put("AABBMax", OSD.FromVector3(aabbMax));
			parcelDataMap.put("AABBMin", OSD.FromVector3(aabbMin));
			parcelDataMap.put("Area", OSD.FromInteger(area));
			parcelDataMap.put("AuctionID", OSD.FromInteger(auctionID));
			parcelDataMap.put("AuthBuyerID", OSD.FromUUID(authBuyerID));
			parcelDataMap.put("Bitmap", OSD.FromBinary(bitmap));
			parcelDataMap.put("Category", OSD.FromInteger(category.getValue()));
			parcelDataMap.put("ClaimDate", OSD.FromDate(claimDate));
			parcelDataMap.put("ClaimPrice", OSD.FromInteger(claimPrice));
			parcelDataMap.put("Desc", OSD.FromString(desc));
			parcelDataMap.put("ParcelFlags", OSD.FromUInteger(parcelFlags));
			parcelDataMap.put("GroupID", OSD.FromUUID(groupID));
			parcelDataMap.put("GroupPrims", OSD.FromInteger(groupPrims));
			parcelDataMap.put("IsGroupOwned", OSD.FromBoolean(isGroupOwned));
			parcelDataMap.put("LandingType", OSD.FromInteger(landingType.getValue()));
			parcelDataMap.put("MaxPrims", OSD.FromInteger(maxPrims));
			parcelDataMap.put("MediaID", OSD.FromUUID(mediaID));
			parcelDataMap.put("MediaURL", OSD.FromString(mediaURL));
			parcelDataMap.put("MediaAutoScale", OSD.FromBoolean(mediaAutoScale));
			parcelDataMap.put("MusicURL", OSD.FromString(musicURL));
			parcelDataMap.put("Name", OSD.FromString(name));
			parcelDataMap.put("OtherCleanTime", OSD.FromInteger(otherCleanTime));
			parcelDataMap.put("OtherCount", OSD.FromInteger(otherCount));
			parcelDataMap.put("OtherPrims", OSD.FromInteger(otherPrims));
			parcelDataMap.put("OwnerID", OSD.FromUUID(ownerID));
			parcelDataMap.put("OwnerPrims", OSD.FromInteger(ownerPrims));
			parcelDataMap.put("ParcelPrimBonus", OSD.FromReal(parcelPrimBonus));
			parcelDataMap.put("PassHours", OSD.FromReal(passHours));
			parcelDataMap.put("PassPrice", OSD.FromInteger(passPrice));
			parcelDataMap.put("PublicCount", OSD.FromInteger(publicCount));
			parcelDataMap.put("Privacy", OSD.FromBoolean(privacy));
			parcelDataMap.put("RegionDenyAnonymous", OSD.FromBoolean(regionDenyAnonymous));
			parcelDataMap.put("RegionPushOverride", OSD.FromBoolean(regionPushOverride));
			parcelDataMap.put("RentPrice", OSD.FromInteger(rentPrice));
			parcelDataMap.put("RequestResult", OSD.FromInteger(requestResult.getValue()));
			parcelDataMap.put("SalePrice", OSD.FromInteger(salePrice));
			parcelDataMap.put("SelectedPrims", OSD.FromInteger(selectedPrims));
			parcelDataMap.put("SelfCount", OSD.FromInteger(selfCount));
			parcelDataMap.put("SequenceID", OSD.FromInteger(sequenceID));
			parcelDataMap.put("SimWideMaxPrims", OSD.FromInteger(simWideMaxPrims));
			parcelDataMap.put("SimWideTotalPrims", OSD.FromInteger(simWideTotalPrims));
			parcelDataMap.put("SnapSelection", OSD.FromBoolean(snapSelection));
			parcelDataMap.put("SnapshotID", OSD.FromUUID(snapshotID));
			parcelDataMap.put("Status", OSD.FromInteger(status.getValue()));
			parcelDataMap.put("TotalPrims", OSD.FromInteger(totalPrims));
			parcelDataMap.put("UserLocation", OSD.FromVector3(userLocation));
			parcelDataMap.put("UserLookAt", OSD.FromVector3(userLookAt));
			parcelDataMap.put("SeeAVs", OSD.FromBoolean(seeAVs));
			parcelDataMap.put("AnyAVSounds", OSD.FromBoolean(anyAVSounds));
			parcelDataMap.put("GroupAVSounds", OSD.FromBoolean(groupAVSounds));
			dataArray.add(parcelDataMap);
			map.put("ParcelData", dataArray);

			OSDArray mediaDataArray = new OSDArray(1);
			OSDMap mediaDataMap = new OSDMap(7);
			mediaDataMap.put("MediaDesc", OSD.FromString(mediaDesc));
			mediaDataMap.put("MediaHeight", OSD.FromInteger(mediaHeight));
			mediaDataMap.put("MediaWidth", OSD.FromInteger(mediaWidth));
			mediaDataMap.put("MediaLoop", OSD.FromBoolean(mediaLoop));
			mediaDataMap.put("MediaType", OSD.FromString(mediaType));
			mediaDataMap.put("ObscureMedia", OSD.FromBoolean(obscureMedia));
			mediaDataMap.put("ObscureMusic", OSD.FromBoolean(obscureMusic));
			mediaDataArray.add(mediaDataMap);
			map.put("MediaData", mediaDataArray);

			OSDArray ageVerificationBlockArray = new OSDArray(1);
			OSDMap ageVerificationBlockMap = new OSDMap(1);
			ageVerificationBlockMap.put("RegionDenyAgeUnverified", OSD.FromBoolean(regionDenyAgeUnverified));
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
			localID = parcelDataMap.get("LocalID").AsInteger();
			aabbMax = parcelDataMap.get("AABBMax").AsVector3();
			aabbMin = parcelDataMap.get("AABBMin").AsVector3();
			area = parcelDataMap.get("Area").AsInteger();
			auctionID = parcelDataMap.get("AuctionID").AsInteger();
			authBuyerID = parcelDataMap.get("AuthBuyerID").AsUUID();
			bitmap = parcelDataMap.get("Bitmap").AsBinary();
			category = ParcelCategory.setValue(parcelDataMap.get("Category").AsInteger());
			claimDate = Helpers.UnixTimeToDateTime(parcelDataMap.get("ClaimDate").AsInteger());
			claimPrice = parcelDataMap.get("ClaimPrice").AsInteger();
			desc = parcelDataMap.get("Desc").AsString();

			// LL sends this as binary, we'll convert it here
			if (parcelDataMap.get("ParcelFlags").getType() == OSDType.Binary) {
				byte[] bytes = parcelDataMap.get("ParcelFlags").AsBinary();
				parcelFlags = ParcelFlags.getValue((int) Helpers.BytesToUInt32B(bytes));
			} else {
				parcelFlags = ParcelFlags.getValue(parcelDataMap.get("ParcelFlags").AsUInteger());
			}
			groupID = parcelDataMap.get("GroupID").AsUUID();
			groupPrims = parcelDataMap.get("GroupPrims").AsInteger();
			isGroupOwned = parcelDataMap.get("IsGroupOwned").AsBoolean();
			landingType = LandingTypeEnum.setValue(parcelDataMap.get("LandingType").AsInteger());
			maxPrims = parcelDataMap.get("MaxPrims").AsInteger();
			mediaID = parcelDataMap.get("MediaID").AsUUID();
			mediaURL = parcelDataMap.get("MediaURL").AsString();
			mediaAutoScale = parcelDataMap.get("MediaAutoScale").AsBoolean(); // 0x1
																				// =
																				// yes
			musicURL = parcelDataMap.get("MusicURL").AsString();
			name = parcelDataMap.get("Name").AsString();
			otherCleanTime = parcelDataMap.get("OtherCleanTime").AsInteger();
			otherCount = parcelDataMap.get("OtherCount").AsInteger();
			otherPrims = parcelDataMap.get("OtherPrims").AsInteger();
			ownerID = parcelDataMap.get("OwnerID").AsUUID();
			ownerPrims = parcelDataMap.get("OwnerPrims").AsInteger();
			parcelPrimBonus = (float) parcelDataMap.get("ParcelPrimBonus").AsReal();
			passHours = (float) parcelDataMap.get("PassHours").AsReal();
			passPrice = parcelDataMap.get("PassPrice").AsInteger();
			publicCount = parcelDataMap.get("PublicCount").AsInteger();
			privacy = parcelDataMap.get("Privacy").AsBoolean();
			regionDenyAnonymous = parcelDataMap.get("RegionDenyAnonymous").AsBoolean();
			regionPushOverride = parcelDataMap.get("RegionPushOverride").AsBoolean();
			rentPrice = parcelDataMap.get("RentPrice").AsInteger();
			requestResult = ParcelResult.setValue(parcelDataMap.get("RequestResult").AsInteger());
			salePrice = parcelDataMap.get("SalePrice").AsInteger();
			selectedPrims = parcelDataMap.get("SelectedPrims").AsInteger();
			selfCount = parcelDataMap.get("SelfCount").AsInteger();
			sequenceID = parcelDataMap.get("SequenceID").AsInteger();
			simWideMaxPrims = parcelDataMap.get("SimWideMaxPrims").AsInteger();
			simWideTotalPrims = parcelDataMap.get("SimWideTotalPrims").AsInteger();
			snapSelection = parcelDataMap.get("SnapSelection").AsBoolean();
			snapshotID = parcelDataMap.get("SnapshotID").AsUUID();
			status = ParcelStatus.setValue(parcelDataMap.get("Status").AsInteger());
			totalPrims = parcelDataMap.get("TotalPrims").AsInteger();
			userLocation = parcelDataMap.get("UserLocation").AsVector3();
			userLookAt = parcelDataMap.get("UserLookAt").AsVector3();
			seeAVs = parcelDataMap.get("SeeAVs").AsBoolean();
			anyAVSounds = parcelDataMap.get("AnyAVSounds").AsBoolean();
			groupAVSounds = parcelDataMap.get("GroupAVSounds").AsBoolean();

			if (map.containsKey("MediaData")) // temporary, OpenSim doesn't send
												// this block
			{
				OSDMap mediaDataMap = (OSDMap) ((OSDArray) map.get("MediaData")).get(0);
				mediaDesc = mediaDataMap.get("MediaDesc").AsString();
				mediaHeight = mediaDataMap.get("MediaHeight").AsInteger();
				mediaWidth = mediaDataMap.get("MediaWidth").AsInteger();
				mediaLoop = mediaDataMap.get("MediaLoop").AsBoolean();
				mediaType = mediaDataMap.get("MediaType").AsString();
				obscureMedia = mediaDataMap.get("ObscureMedia").AsBoolean();
				obscureMusic = mediaDataMap.get("ObscureMusic").AsBoolean();
			}

			OSDMap ageVerificationBlockMap = (OSDMap) ((OSDArray) map.get("AgeVerificationBlock")).get(0);
			regionDenyAgeUnverified = ageVerificationBlockMap.get("RegionDenyAgeUnverified").AsBoolean();
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
			map.put("auth_buyer_id", OSD.FromUUID(authBuyerID));
			map.put("auto_scale", OSD.FromBoolean(mediaAutoScale));
			map.put("category", OSD.FromInteger(category.getValue()));
			map.put("description", OSD.FromString(desc));
			map.put("flags", OSD.FromBinary(Helpers.EmptyBytes));
			map.put("group_id", OSD.FromUUID(groupID));
			map.put("landing_type", OSD.FromInteger(landingType.getValue()));
			map.put("local_id", OSD.FromInteger(localID));
			map.put("media_desc", OSD.FromString(mediaDesc));
			map.put("media_height", OSD.FromInteger(mediaHeight));
			map.put("media_id", OSD.FromUUID(mediaID));
			map.put("media_loop", OSD.FromBoolean(mediaLoop));
			map.put("media_type", OSD.FromString(mediaType));
			map.put("media_url", OSD.FromString(mediaURL));
			map.put("media_width", OSD.FromInteger(mediaWidth));
			map.put("music_url", OSD.FromString(musicURL));
			map.put("name", OSD.FromString(name));
			map.put("obscure_media", OSD.FromBoolean(obscureMedia));
			map.put("obscure_music", OSD.FromBoolean(obscureMusic));
			map.put("parcel_flags", OSD.FromUInteger(parcelFlags));
			map.put("pass_hours", OSD.FromReal(passHours));
			map.put("pass_price", OSD.FromInteger(passPrice));
			map.put("privacy", OSD.FromBoolean(privacy));
			map.put("sale_price", OSD.FromInteger(salePrice));
			map.put("snapshot_id", OSD.FromUUID(snapshotID));
			map.put("user_location", OSD.FromVector3(userLocation));
			map.put("user_look_at", OSD.FromVector3(userLookAt));
			map.put("see_avs", OSD.FromBoolean(seeAVs));
			map.put("any_av_sounds", OSD.FromBoolean(anyAVSounds));
			map.put("group_av_sounds", OSD.FromBoolean(groupAVSounds));

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
			authBuyerID = map.get("auth_buyer_id").AsUUID();
			mediaAutoScale = map.get("auto_scale").AsBoolean();
			category = ParcelCategory.setValue(map.get("category").AsInteger());
			desc = map.get("description").AsString();
			groupID = map.get("group_id").AsUUID();
			landingType = LandingTypeEnum.setValue(map.get("landing_type").AsUInteger());
			localID = map.get("local_id").AsInteger();
			mediaDesc = map.get("media_desc").AsString();
			mediaHeight = map.get("media_height").AsInteger();
			mediaLoop = map.get("media_loop").AsBoolean();
			mediaID = map.get("media_id").AsUUID();
			mediaType = map.get("media_type").AsString();
			mediaURL = map.get("media_url").AsString();
			mediaWidth = map.get("media_width").AsInteger();
			musicURL = map.get("music_url").AsString();
			name = map.get("name").AsString();
			obscureMedia = map.get("obscure_media").AsBoolean();
			obscureMusic = map.get("obscure_music").AsBoolean();
			parcelFlags = ParcelFlags.setValue((map.get("parcel_flags").AsUInteger()));
			passHours = (float) map.get("pass_hours").AsReal();
			passPrice = map.get("pass_price").AsUInteger();
			privacy = map.get("privacy").AsBoolean();
			salePrice = map.get("sale_price").AsUInteger();
			snapshotID = map.get("snapshot_id").AsUUID();
			userLocation = map.get("user_location").AsVector3();
			userLookAt = map.get("user_look_at").AsVector3();
			if (map.containsKey("see_avs")) {
				seeAVs = map.get("see_avs").AsBoolean();
				anyAVSounds = map.get("any_av_sounds").AsBoolean();
				groupAVSounds = map.get("group_av_sounds").AsBoolean();
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
			map.put("location", OSD.FromVector3(location));
			map.put("region_handle", OSD.FromULong(regionHandle));
			map.put("region_id", OSD.FromUUID(regionID));
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
			location = map.get("location").AsVector3();
			regionHandle = map.get("region_handle").AsULong();
			regionID = map.get("region_id").AsUUID();
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
			map.put("parcel_id", OSD.FromUUID(parcelID));
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
				parcelID = UUID.Zero;
			else
				parcelID = map.get("parcel_id").AsUUID();
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
						+ map.AsString());
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
			map.put("folder_id", OSD.FromUUID(folderID));
			map.put("asset_type", OSD.FromString(assetType.toString()));
			map.put("inventory_type", OSD.FromString(inventoryType.toString()));
			map.put("name", OSD.FromString(name));
			map.put("description", OSD.FromString(description));

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
			folderID = map.get("folder_id").AsUUID();
			assetType = AssetType.setValue(map.get("asset_type").AsString());
			inventoryType = InventoryItem.InventoryType.setValue(map.get("inventory_type").AsString());
			name = map.get("name").AsString();
			description = map.get("description").AsString();
		}
	}

	public class BulkUpdateInventoryMessage implements IMessage {
		public class FolderDataInfo {
			public UUID folderID;
			public UUID parentID;
			public String name;
			public FolderType type;

			public FolderDataInfo(OSDMap map) {
				folderID = map.get("FolderID").AsUUID();
				parentID = map.get("ParentID").AsUUID();
				name = map.get("Name").AsString();
				type = FolderType.setValue(map.get("Type").AsInteger());
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
				itemID = map.get("ItemID").AsUUID();
				callbackID = map.get("CallbackID").AsUInteger();
				folderID = map.get("FolderID").AsUUID();
				creatorID = map.get("CreatorID").AsUUID();
				ownerID = map.get("OwnerID").AsUUID();
				groupID = map.get("GroupID").AsUUID();
				baseMask = PermissionMask.setValue(map.get("BaseMask").AsUInteger());
				ownerMask = PermissionMask.setValue(map.get("OwnerMask").AsUInteger());
				groupMask = PermissionMask.setValue(map.get("GroupMask").AsUInteger());
				everyoneMask = PermissionMask.setValue(map.get("EveryoneMask").AsUInteger());
				nextOwnerMask = PermissionMask.setValue(map.get("NextOwnerMask").AsUInteger());
				groupOwned = map.get("GroupOwned").AsBoolean();
				assetID = map.get("AssetID").AsUUID();
				assetType = AssetType.setValue(map.get("Type").AsInteger());
				inventoryType = InventoryType.setValue(map.get("InvType").AsInteger());
				flags = map.get("Flags").AsUInteger();
				saleType = SaleType.setValue(map.get("SaleType").AsInteger());
				salePrice = map.get("SaleType").AsInteger();
				name = map.get("Name").AsString();
				description = map.get("Description").AsString();
				creationDate = Helpers.UnixTimeToDateTime(map.get("CreationDate").AsReal());
				crc = map.get("CRC").AsUInteger();
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
					agentID = adata.get("AgentID").AsUUID();
					transactionID = adata.get("TransactionID").AsUUID();
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
			agent.put("AgentID", OSD.FromUUID(agentID));

			OSDArray agentArray = new OSDArray();
			agentArray.add(agent);

			map.put("AgentData", agentArray);

			OSDArray groupDataArray = new OSDArray(groupDataBlock.length);

			for (int i = 0; i < groupDataBlock.length; i++) {
				OSDMap group = new OSDMap(7);
				group.put("AcceptNotices", OSD.FromBoolean(groupDataBlock[i].acceptNotices));
				group.put("Contribution", OSD.FromInteger(groupDataBlock[i].contribution));
				group.put("GroupID", OSD.FromUUID(groupDataBlock[i].groupID));
				group.put("GroupInsigniaID", OSD.FromUUID(groupDataBlock[i].groupInsigniaID));
				group.put("GroupName", OSD.FromString(groupDataBlock[i].groupName));
				group.put("GroupTitle", OSD.FromString(groupDataBlock[i].groupTitle));
				group.put("GroupPowers", OSD.FromLong(groupDataBlock[i].groupPowers));
				groupDataArray.add(group);
			}

			map.put("GroupData", groupDataArray);

			OSDArray newGroupDataArray = new OSDArray(newGroupDataBlock.length);

			for (int i = 0; i < newGroupDataBlock.length; i++) {
				OSDMap group = new OSDMap(1);
				group.put("ListInProfile", OSD.FromBoolean(newGroupDataBlock[i].listInProfile));
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
			agentID = agentMap.get("AgentID").AsUUID();

			OSDArray groupArray = (OSDArray) map.get("GroupData");

			groupDataBlock = new GroupData[groupArray.size()];

			for (int i = 0; i < groupArray.size(); i++) {
				OSDMap groupMap = (OSDMap) groupArray.get(i);

				GroupData groupData = new GroupData();

				groupData.groupID = groupMap.get("GroupID").AsUUID();
				groupData.contribution = groupMap.get("Contribution").AsInteger();
				groupData.groupInsigniaID = groupMap.get("GroupInsigniaID").AsUUID();
				groupData.groupName = groupMap.get("GroupName").AsString();
				groupData.groupPowers = groupMap.get("GroupPowers").AsLong();
				groupData.acceptNotices = groupMap.get("AcceptNotices").AsBoolean();
				if (groupMap.containsKey("GroupTitle"))
					groupData.groupTitle = groupMap.get("GroupTitle").AsString();
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
					newGroupData.listInProfile = newGroupMap.get("ListInProfile").AsBoolean();
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

			map.put("language", OSD.FromString(language));
			map.put("language_is_public", OSD.FromBoolean(languagePublic));

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
			languagePublic = map.get("language_is_public").AsBoolean();
			language = map.get("language").AsString();
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
				agentMap.put("AgentID", OSD.FromUUID(agentDataBlock[i].agentID));
				agentMap.put("GroupID", OSD.FromUUID(agentDataBlock[i].groupID));
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

				agentData.agentID = agentMap.get("AgentID").AsUUID();
				agentData.groupID = agentMap.get("GroupID").AsUUID();

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
			access.put("max", OSD.FromString(preferences.maxAccess));

			OSDMap prefs = new OSDMap(6);
			prefs.put("god_level", OSD.FromInteger(preferences.godLevel));
			prefs.put("language_is_public", OSD.FromBoolean(preferences.languageIsPublic));
			prefs.put("language", OSD.FromString(preferences.language));
			prefs.put("alter_permanent_objects", OSD.FromBoolean(preferences.alterPermanentObjects));
			prefs.put("alter_navmesh_objects", OSD.FromBoolean(preferences.alterNavmeshObjects));
			prefs.put("access_prefs", access);

			OSDMap map = new OSDMap(3);
			map.put("has_modified_navmesh", OSD.FromBoolean(hasModifiedNavmesh));
			map.put("can_modify_navmesh", OSD.FromBoolean(canModifyNavmesh));
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
			hasModifiedNavmesh = map.get("has_modified_navmesh").AsBoolean();
			canModifyNavmesh = map.get("can_modify_navmesh").AsBoolean();
			preferences = new Preferences();

			OSDMap prefs = (OSDMap) map.get("preferences");
			if (prefs != null) {
				preferences.godLevel = prefs.get("god_level").AsInteger();
				preferences.languageIsPublic = prefs.get("language_is_public").AsBoolean();
				preferences.language = prefs.get("language").AsString();
				preferences.alterPermanentObjects = prefs.get("alter_permanent_objects").AsBoolean();
				preferences.alterNavmeshObjects = prefs.get("alter_navmesh_objects").AsBoolean();

				OSDMap access = (OSDMap) prefs.get("access_prefs");
				preferences.maxAccess = access.get("max").AsString();
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
			map.put("state", OSD.FromString(state));
			map.put("uploader", OSD.FromUri(url));

			return map;
		}

		@Override
		public void deserialize(OSDMap map) {
			url = map.get("uploader").AsUri();
			state = map.get("state").AsString();
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
			map.put("state", OSD.FromString(state));
			map.put("new_asset", OSD.FromUUID(assetID));

			return map;
		}

		@Override
		public void deserialize(OSDMap map) {
			assetID = map.get("new_asset").AsUUID();
			state = map.get("state").AsString();
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
				String value = map.get("state").AsString();
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
			map.put("major_version", OSD.FromInteger(majorVersion));
			map.put("minor_version", OSD.FromInteger(minorVersion));
			map.put("region_name", OSD.FromString(regionName));

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
			majorVersion = map.get("major_version").AsInteger();
			minorVersion = map.get("minor_version").AsInteger();
			regionName = map.get("region_name").AsString();
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
			map.put("parcel_local_id", OSD.FromInteger(parcelID));
			map.put("region_name", OSD.FromString(regionName));

			OSDMap vcMap = new OSDMap(1);
			vcMap.put("channel_uri", OSD.FromUri(sipChannelUri));

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
			parcelID = map.get("parcel_local_id").AsInteger();
			regionName = map.get("region_name").AsString();

			OSDMap vcMap = (OSDMap) map.get("voice_credentials");
			sipChannelUri = vcMap.get("channel_uri").AsUri();
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

			map.put("username", OSD.FromString(username));
			map.put("password", OSD.FromString(password));

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
			username = map.get("username").AsString();
			password = map.get("password").AsString();
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
				String value = map.get("state").AsString();
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
			scriptMap.put("ItemID", OSD.FromUUID(itemID));
			scriptMap.put("Mono", OSD.FromBoolean(mono));
			scriptMap.put("ObjectID", OSD.FromUUID(objectID));
			scriptMap.put("Running", OSD.FromBoolean(running));

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

			itemID = scriptMap.get("ItemID").AsUUID();
			mono = scriptMap.get("Mono").AsBoolean();
			objectID = scriptMap.get("ObjectID").AsUUID();
			running = scriptMap.get("Running").AsBoolean();
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
				String value = map.get("state").AsString();
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
						+ map.AsString());
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
			map.put("task_id", OSD.FromUUID(taskID));
			map.put("item_id", OSD.FromUUID(itemID));

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
			taskID = map.get("task_id").AsUUID();
			itemID = map.get("item_id").AsUUID();
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
			map.put("item_id", OSD.FromUUID(itemID));

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
			itemID = map.get("item_id").AsUUID();
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
				String value = map.get("state").AsString();
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
			map.put("callback-id", OSD.FromInteger(callbackID));
			map.put("folder-id", OSD.FromUUID(folderID));
			map.put("item-id", OSD.FromUUID(itemID));
			map.put("notecard-id", OSD.FromUUID(notecardID));
			map.put("object-id", OSD.FromUUID(objectID));

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
			callbackID = map.get("callback-id").AsInteger();
			folderID = map.get("folder-id").AsUUID();
			itemID = map.get("item-id").AsUUID();
			notecardID = map.get("notecard-id").AsUUID();
			objectID = map.get("object-id").AsUUID();
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
			map.put("state", OSD.FromString(state));
			map.put("new_asset", OSD.FromUUID(assetID));
			map.put("compiled", OSD.FromBoolean(compiled));

			OSDArray errorsArray = new OSDArray();
			errorsArray.add(OSD.FromString(error));
			map.put("errors", errorsArray);
			return map;
		}

		@Override
		public void deserialize(OSDMap map) {
			assetID = map.get("new_asset").AsUUID();
			compiled = map.get("compiled").AsBoolean();
			state = map.get("state").AsString();

			OSDArray errorsArray = (OSDArray) map.get("errors");
			error = errorsArray.get(0).AsString();
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
			map.put("is_script_running", OSD.FromBoolean(scriptRunning));
			map.put("item_id", OSD.FromUUID(itemID));
			map.put("target", OSD.FromString(target));
			map.put("task_id", OSD.FromUUID(taskID));
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
			scriptRunning = map.get("is_script_running").AsBoolean();
			itemID = map.get("item_id").AsUUID();
			target = map.get("target").AsString();
			taskID = map.get("task_id").AsUUID();
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
			String value = map.get("method").AsString();
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
				String value = map.get("state").AsString();
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
						+ map.AsString());
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
			map.put("state", OSD.FromString(state));
			map.put("new_asset", OSD.FromUUID(assetID));
			map.put("compiled", OSD.FromBoolean(compiled));
			return map;
		}

		@Override
		public void deserialize(OSDMap map) {
			assetID = map.get("new_asset").AsUUID();
			compiled = map.get("compiled").AsBoolean();
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
			map.put("item_id", OSD.FromUUID(itemID));
			map.put("target", OSD.FromString(target));
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
			itemID = map.get("item_id").AsUUID();
			target = map.get("target").AsString();
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
				String value = map.get("state").AsString();
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
						+ map.AsString());
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
			map.put("from", OSD.FromString(fromEmail));
			map.put("msg", OSD.FromString(message));
			map.put("name", OSD.FromString(fromName));
			map.put("pos-global", OSD.FromVector3(globalPosition));
			map.put("subject", OSD.FromString(subject));
			map.put("to", OSD.FromString(toEmail));
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
			fromEmail = map.get("from").AsString();
			message = map.get("msg").AsString();
			fromName = map.get("name").AsString();
			globalPosition = map.get("pos-global").AsVector3();
			subject = map.get("subject").AsString();
			toEmail = map.get("to").AsString();
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
			map.put("Flags", OSD.FromInteger(flags));
			return map;
		}

		@Override
		public void deserialize(OSDMap map) {
			flags = map.get("Flags").AsInteger();
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
			agentMap.put("Flags", OSD.FromInteger(flags));
			map.put("AgentData", agentMap);

			OSDArray layerArray = new OSDArray(layerDataBlocks.length);

			for (int i = 0; i < layerDataBlocks.length; i++) {
				OSDMap layerMap = new OSDMap(5);
				layerMap.put("ImageID", OSD.FromUUID(layerDataBlocks[i].imageID));
				layerMap.put("Bottom", OSD.FromInteger(layerDataBlocks[i].bottom));
				layerMap.put("Left", OSD.FromInteger(layerDataBlocks[i].left));
				layerMap.put("Top", OSD.FromInteger(layerDataBlocks[i].top));
				layerMap.put("Right", OSD.FromInteger(layerDataBlocks[i].right));

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
			flags = agentMap.get("Flags").AsInteger();

			OSDArray layerArray = (OSDArray) map.get("LayerData");

			layerDataBlocks = new LayerData[layerArray.size()];

			for (int i = 0; i < layerDataBlocks.length; i++) {
				OSDMap layerMap = (OSDMap) layerArray.get(i);

				LayerData layer = new LayerData();
				layer.imageID = layerMap.get("ImageID").AsUUID();
				layer.top = layerMap.get("Top").AsInteger();
				layer.right = layerMap.get("Right").AsInteger();
				layer.left = layerMap.get("Left").AsInteger();
				layer.bottom = layerMap.get("Bottom").AsInteger();

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
			map.put("classified_id", OSD.FromUUID(classifiedID));
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
			classifiedID = map.get("classified_id").AsUUID();
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
			map.put("map_clicks", OSD.FromInteger(mapClicks));
			map.put("profile_clicks", OSD.FromInteger(profileClicks));
			map.put("search_map_clicks", OSD.FromInteger(searchMapClicks));
			map.put("search_profile_clicks", OSD.FromInteger(searchProfileClicks));
			map.put("search_teleport_clicks", OSD.FromInteger(searchTeleportClicks));
			map.put("teleport_clicks", OSD.FromInteger(teleportClicks));
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
			mapClicks = map.get("map_clicks").AsInteger();
			profileClicks = map.get("profile_clicks").AsInteger();
			searchMapClicks = map.get("search_map_clicks").AsInteger();
			searchProfileClicks = map.get("search_profile_clicks").AsInteger();
			searchTeleportClicks = map.get("search_teleport_clicks").AsInteger();
			teleportClicks = map.get("teleport_clicks").AsInteger();
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
						+ map.get("method").AsString());
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
			map.put("method", OSD.FromString(method));
			OSDArray agentsArray = new OSDArray();
			for (int i = 0; i < agentsBlock.length; i++) {
				agentsArray.add(OSD.FromUUID(agentsBlock[i]));
			}
			map.put("params", agentsArray);
			map.put("session-id", OSD.FromUUID(sessionID));

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
			method = map.get("method").AsString();
			OSDArray agentsArray = (OSDArray) map.get("params");

			agentsBlock = new UUID[agentsArray.size()];

			for (int i = 0; i < agentsArray.size(); i++) {
				agentsBlock[i] = agentsArray.get(i).AsUUID();
			}

			sessionID = map.get("session-id").AsUUID();
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
			map.put("method", OSD.FromString(method));

			OSDMap muteMap = new OSDMap(1);
			muteMap.put(requestKey, OSD.FromBoolean(requestValue));

			OSDMap paramMap = new OSDMap(2);
			paramMap.put("agent_id", OSD.FromUUID(agentID));
			paramMap.put("mute_info", muteMap);

			map.put("params", paramMap);
			map.put("session-id", OSD.FromUUID(sessionID));

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
			method = map.get("method").AsString();
			sessionID = map.get("session-id").AsUUID();

			OSDMap paramsMap = (OSDMap) map.get("params");
			OSDMap muteMap = (OSDMap) paramsMap.get("mute_info");

			agentID = paramsMap.get("agent_id").AsUUID();

			if (muteMap.containsKey("text"))
				requestKey = "text";
			else if (muteMap.containsKey("voice"))
				requestKey = "voice";

			requestValue = muteMap.get(requestKey).AsBoolean();
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
			map.put("method", OSD.FromString(method));
			map.put("session-id", OSD.FromUUID(sessionID));
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
			method = map.get("method").AsString();
			sessionID = map.get("session-id").AsUUID();
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
				String value = map.get("method").AsString();
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
						+ map.AsString());
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
			map.put("success", OSD.FromBoolean(success));
			map.put("session_id", OSD.FromUUID(sessionID)); // FIXME: Verify
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
			success = map.get("success").AsBoolean();
			sessionID = map.get("session_id").AsUUID();
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
			moderatedMap.put("voice", OSD.FromBoolean(moderatedVoice));

			OSDMap sessionMap = new OSDMap(4);
			sessionMap.put("type", OSD.FromInteger(type));
			sessionMap.put("session_name", OSD.FromString(sessionName));
			sessionMap.put("voice_enabled", OSD.FromBoolean(voiceEnabled));
			sessionMap.put("moderated_mode", moderatedMap);

			OSDMap map = new OSDMap(4);
			map.put("session_id", OSD.FromUUID(sessionID));
			map.put("temp_session_id", OSD.FromUUID(tempSessionID));
			map.put("success", OSD.FromBoolean(success));
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
			sessionID = map.get("session_id").AsUUID();
			tempSessionID = map.get("temp_session_id").AsUUID();
			success = map.get("success").AsBoolean();

			if (success) {
				OSDMap sessionMap = (OSDMap) map.get("session_info");
				sessionName = sessionMap.get("session_name").AsString();
				type = sessionMap.get("type").AsInteger();
				voiceEnabled = sessionMap.get("voice_enabled").AsBoolean();

				OSDMap moderatedModeMap = (OSDMap) sessionMap.get("moderated_mode");
				moderatedVoice = moderatedModeMap.get("voice").AsBoolean();
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
			dataMap.put("timestamp", OSD.FromDate(timestamp));
			dataMap.put("type", OSD.FromInteger(dialog.getValue()));
			dataMap.put("binary_bucket", OSD.FromBinary(binaryBucket));

			OSDMap paramsMap = new OSDMap(11);
			paramsMap.put("from_id", OSD.FromUUID(fromAgentID));
			paramsMap.put("from_name", OSD.FromString(fromAgentName));
			paramsMap.put("to_id", OSD.FromUUID(toAgentID));
			paramsMap.put("parent_estate_id", OSD.FromInteger(parentEstateID));
			paramsMap.put("region_id", OSD.FromUUID(regionID));
			paramsMap.put("position", OSD.FromVector3(position));
			paramsMap.put("from_group", OSD.FromBoolean(groupIM));
			paramsMap.put("id", OSD.FromUUID(imSessionID));
			paramsMap.put("message", OSD.FromString(message));
			paramsMap.put("offline", OSD.FromInteger(offline.getValue()));

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
				fromAgentID = map.get("from_id").AsUUID();
				fromAgentName = map.get("from_name").AsString();
				imSessionID = map.get("session_id").AsUUID();
				binaryBucket = Helpers.StringToBytes(map.get("session_name").AsString());
				voice = true;
			} else {
				OSDMap im = (OSDMap) map.get("instantmessage");
				OSDMap msg = (OSDMap) im.get("message_params");
				OSDMap msgdata = (OSDMap) msg.get("data");

				fromAgentID = msg.get("from_id").AsUUID();
				fromAgentName = msg.get("from_name").AsString();
				toAgentID = msg.get("to_id").AsUUID();
				parentEstateID = msg.get("parent_estate_id").AsInteger();
				regionID = msg.get("region_id").AsUUID();
				position = msg.get("position").AsVector3();
				groupIM = msg.get("from_group").AsBoolean();
				imSessionID = msg.get("id").AsUUID();
				message = msg.get("message").AsString();
				offline = InstantMessageOnline.setValue(msg.get("offline").AsInteger());
				dialog = InstantMessageDialog.setValue(msgdata.get("type").AsInteger());
				binaryBucket = msgdata.get("binary_bucket").AsBinary();
				timestamp = msgdata.get("timestamp").AsDate();
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
			map.put("parcel_local_id", OSD.FromInteger(parcelLocalID));
			map.put("region_name", OSD.FromString(regionName));
			OSDMap voiceMap = new OSDMap(1);
			voiceMap.put("channel_uri", OSD.FromString(channelUri));
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
			this.parcelLocalID = map.get("parcel_local_id").AsInteger();
			this.regionName = map.get("region_name").AsString();
			OSDMap voiceMap = (OSDMap) map.get("voice_credentials");
			this.channelUri = voiceMap.get("channel_uri").AsString();
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
				mutesMap.put("text", OSD.FromBoolean(updates[i].muteText));
				mutesMap.put("voice", OSD.FromBoolean(updates[i].muteVoice));

				OSDMap infoMap = new OSDMap(3);
				infoMap.put("can_voice_chat", OSD.FromBoolean(updates[i].canVoiceChat));
				infoMap.put("is_moderator", OSD.FromBoolean(updates[i].isModerator));
				infoMap.put("mutes", mutesMap);

				OSDMap imap = new OSDMap(2);
				imap.put("info", infoMap);
				imap.put("transition", OSD.FromString(updates[i].transition));

				agent_updatesMap.put(updates[i].agentID.toString(), imap);
			}
			map.put("agent_updates", agent_updatesMap);
			map.put("session_id", OSD.FromUUID(sessionID));

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
			sessionID = map.get("session_id").AsUUID();

			ArrayList<AgentUpdatesBlock> updatesList = new ArrayList<AgentUpdatesBlock>();

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

					block.canVoiceChat = agentPermsMap.get("can_voice_chat").AsBoolean();
					block.isModerator = agentPermsMap.get("is_moderator").AsBoolean();

					block.transition = infoMap.get("transition").AsString();

					if (agentPermsMap.containsKey("mutes")) {
						OSDMap mutesMap = (OSDMap) agentPermsMap.get("mutes");
						block.muteText = mutesMap.get("text").AsBoolean();
						block.muteVoice = mutesMap.get("voice").AsBoolean();
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
			map.put("reason", OSD.FromString(reason));
			map.put("session_id", OSD.FromUUID(sessionID));

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
			reason = map.get("reason").AsString();
			sessionID = map.get("session_id").AsUUID();
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
			map.put("ack", OSD.FromInteger(ackID));
			map.put("done", OSD.FromBoolean(done));
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
			ackID = map.get("ack").AsInteger();
			done = map.get("done").AsBoolean();
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
				eventMap.put("message", OSD.FromString(messageEvents[i].messageKey.toString()));
				eventsArray.add(eventMap);
			}

			map.put("events", eventsArray);
			map.put("id", OSD.FromInteger(sequence));

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
			sequence = map.get("id").AsInteger();
			OSDArray arrayEvents = (OSDArray) map.get("events");

			messageEvents = new QueueEvent[arrayEvents.size()];

			for (int i = 0; i < arrayEvents.size(); i++) {
				OSDMap eventMap = (OSDMap) arrayEvents.get(i);
				QueueEvent ev = new QueueEvent();

				ev.messageKey = CapsEventType.valueOf(eventMap.get("message").AsString());
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

		public float object_kbytes;
		public float texture_kbytes;
		public float world_kbytes;

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
			map.put("session_id", OSD.FromUUID(sessionID));

			OSDMap agentMap = new OSDMap(11);
			agentMap.put("agents_in_view", OSD.FromInteger(agentsInView));
			agentMap.put("fps", OSD.FromReal(agentFPS));
			agentMap.put("language", OSD.FromString(agentLanguage));
			agentMap.put("mem_use", OSD.FromReal(agentMemoryUsed));
			agentMap.put("meters_traveled", OSD.FromReal(metersTraveled));
			agentMap.put("ping", OSD.FromReal(agentPing));
			agentMap.put("regions_visited", OSD.FromInteger(regionsVisited));
			agentMap.put("run_time", OSD.FromReal(agentRuntime));
			agentMap.put("sim_fps", OSD.FromReal(simulatorFPS));
			agentMap.put("start_time", OSD.FromUInteger((int) (long) Helpers.DateTimeToUnixTime(agentStartTime)));
			agentMap.put("version", OSD.FromString(agentVersion));
			map.put("agent", agentMap);

			OSDMap downloadsMap = new OSDMap(3); // downloads
			downloadsMap.put("object_kbytes", OSD.FromReal(object_kbytes));
			downloadsMap.put("texture_kbytes", OSD.FromReal(texture_kbytes));
			downloadsMap.put("world_kbytes", OSD.FromReal(world_kbytes));
			map.put("downloads", downloadsMap);

			OSDMap miscMap = new OSDMap(2);
			miscMap.put("Version", OSD.FromReal(miscVersion));
			miscMap.put("Vertex Buffers Enabled", OSD.FromBoolean(vertexBuffersEnabled));
			map.put("misc", miscMap);

			OSDMap statsMap = new OSDMap(2);

			OSDMap failuresMap = new OSDMap(6);
			failuresMap.put("dropped", OSD.FromInteger(statsDropped));
			failuresMap.put("failed_resends", OSD.FromInteger(statsFailedResends));
			failuresMap.put("invalid", OSD.FromInteger(failuresInvalid));
			failuresMap.put("off_circuit", OSD.FromInteger(failuresOffCircuit));
			failuresMap.put("resent", OSD.FromInteger(failuresResent));
			failuresMap.put("send_packet", OSD.FromInteger(failuresSendPacket));
			statsMap.put("failures", failuresMap);

			OSDMap statsMiscMap = new OSDMap(3);
			statsMiscMap.put("int_1", OSD.FromInteger(miscInt1));
			statsMiscMap.put("int_2", OSD.FromInteger(miscInt2));
			statsMiscMap.put("string_1", OSD.FromString(miscString1));
			statsMap.put("misc", statsMiscMap);

			OSDMap netMap = new OSDMap(3);

			// in
			OSDMap netInMap = new OSDMap(4);
			netInMap.put("compressed_packets", OSD.FromInteger(inCompressedPackets));
			netInMap.put("kbytes", OSD.FromReal(inKbytes));
			netInMap.put("packets", OSD.FromReal(inPackets));
			netInMap.put("savings", OSD.FromReal(inSavings));
			netMap.put("in", netInMap);
			// out
			OSDMap netOutMap = new OSDMap(4);
			netOutMap.put("compressed_packets", OSD.FromInteger(outCompressedPackets));
			netOutMap.put("kbytes", OSD.FromReal(outKbytes));
			netOutMap.put("packets", OSD.FromReal(outPackets));
			netOutMap.put("savings", OSD.FromReal(outSavings));
			netMap.put("out", netOutMap);

			statsMap.put("net", netMap);

			// system
			OSDMap systemStatsMap = new OSDMap(7);
			systemStatsMap.put("cpu", OSD.FromString(systemCPU));
			systemStatsMap.put("gpu", OSD.FromString(systemGPU));
			systemStatsMap.put("gpu_class", OSD.FromInteger(systemGPUClass));
			systemStatsMap.put("gpu_vendor", OSD.FromString(systemGPUVendor));
			systemStatsMap.put("gpu_version", OSD.FromString(systemGPUVersion));
			systemStatsMap.put("os", OSD.FromString(systemOS));
			systemStatsMap.put("ram", OSD.FromInteger(systemInstalledRam));
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
			sessionID = map.get("session_id").AsUUID();

			OSDMap agentMap = (OSDMap) map.get("agent");
			agentsInView = agentMap.get("agents_in_view").AsInteger();
			agentFPS = (float) agentMap.get("fps").AsReal();
			agentLanguage = agentMap.get("language").AsString();
			agentMemoryUsed = (float) agentMap.get("mem_use").AsReal();
			metersTraveled = agentMap.get("meters_traveled").AsInteger();
			agentPing = (float) agentMap.get("ping").AsReal();
			regionsVisited = agentMap.get("regions_visited").AsInteger();
			agentRuntime = (float) agentMap.get("run_time").AsReal();
			simulatorFPS = (float) agentMap.get("sim_fps").AsReal();
			agentStartTime = Helpers.UnixTimeToDateTime(agentMap.get("start_time").AsUInteger());
			agentVersion = agentMap.get("version").AsString();

			OSDMap downloadsMap = (OSDMap) map.get("downloads");
			object_kbytes = (float) downloadsMap.get("object_kbytes").AsReal();
			texture_kbytes = (float) downloadsMap.get("texture_kbytes").AsReal();
			world_kbytes = (float) downloadsMap.get("world_kbytes").AsReal();

			OSDMap miscMap = (OSDMap) map.get("misc");
			miscVersion = (float) miscMap.get("Version").AsReal();
			vertexBuffersEnabled = miscMap.get("Vertex Buffers Enabled").AsBoolean();

			OSDMap statsMap = (OSDMap) map.get("stats");
			OSDMap failuresMap = (OSDMap) statsMap.get("failures");
			statsDropped = failuresMap.get("dropped").AsInteger();
			statsFailedResends = failuresMap.get("failed_resends").AsInteger();
			failuresInvalid = failuresMap.get("invalid").AsInteger();
			failuresOffCircuit = failuresMap.get("off_circuit").AsInteger();
			failuresResent = failuresMap.get("resent").AsInteger();
			failuresSendPacket = failuresMap.get("send_packet").AsInteger();

			OSDMap statsMiscMap = (OSDMap) statsMap.get("misc");
			miscInt1 = statsMiscMap.get("int_1").AsInteger();
			miscInt2 = statsMiscMap.get("int_2").AsInteger();
			miscString1 = statsMiscMap.get("string_1").AsString();
			OSDMap netMap = (OSDMap) statsMap.get("net");
			// in
			OSDMap netInMap = (OSDMap) netMap.get("in");
			inCompressedPackets = netInMap.get("compressed_packets").AsInteger();
			inKbytes = netInMap.get("kbytes").AsInteger();
			inPackets = netInMap.get("packets").AsInteger();
			inSavings = netInMap.get("savings").AsInteger();
			// out
			OSDMap netOutMap = (OSDMap) netMap.get("out");
			outCompressedPackets = netOutMap.get("compressed_packets").AsInteger();
			outKbytes = netOutMap.get("kbytes").AsInteger();
			outPackets = netOutMap.get("packets").AsInteger();
			outSavings = netOutMap.get("savings").AsInteger();

			// system
			OSDMap systemStatsMap = (OSDMap) map.get("system");
			systemCPU = systemStatsMap.get("cpu").AsString();
			systemGPU = systemStatsMap.get("gpu").AsString();
			systemGPUClass = systemStatsMap.get("gpu_class").AsInteger();
			systemGPUVendor = systemStatsMap.get("gpu_vendor").AsString();
			systemGPUVersion = systemStatsMap.get("gpu_version").AsString();
			systemOS = systemStatsMap.get("os").AsString();
			systemInstalledRam = systemStatsMap.get("ram").AsInteger();
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
			agentIDmap.put("AgentID", OSD.FromUUID(agentID));
			agentIDmap.put("QueryID", OSD.FromUUID(queryID));

			OSDArray agentDataArray = new OSDArray();
			agentDataArray.add(agentIDmap);

			map.put("AgentData", agentDataArray);

			// add the QueryData map
			OSDArray dataBlocksArray = new OSDArray(queryDataBlocks.length);
			for (int i = 0; i < queryDataBlocks.length; i++) {
				OSDMap queryDataMap = new OSDMap(14);
				queryDataMap.put("ActualArea", OSD.FromInteger(queryDataBlocks[i].actualArea));
				queryDataMap.put("BillableArea", OSD.FromInteger(queryDataBlocks[i].billableArea));
				queryDataMap.put("Desc", OSD.FromString(queryDataBlocks[i].description));
				queryDataMap.put("Dwell", OSD.FromReal(queryDataBlocks[i].dwell));
				queryDataMap.put("Flags", OSD.FromInteger(queryDataBlocks[i].flags));
				queryDataMap.put("GlobalX", OSD.FromReal(queryDataBlocks[i].globalX));
				queryDataMap.put("GlobalY", OSD.FromReal(queryDataBlocks[i].globalY));
				queryDataMap.put("GlobalZ", OSD.FromReal(queryDataBlocks[i].globalZ));
				queryDataMap.put("Name", OSD.FromString(queryDataBlocks[i].name));
				queryDataMap.put("OwnerID", OSD.FromUUID(queryDataBlocks[i].ownerID));
				queryDataMap.put("Price", OSD.FromInteger(queryDataBlocks[i].price));
				queryDataMap.put("SimName", OSD.FromString(queryDataBlocks[i].simName));
				queryDataMap.put("SnapshotID", OSD.FromUUID(queryDataBlocks[i].snapShotID));
				queryDataMap.put("ProductSKU", OSD.FromString(queryDataBlocks[i].productSku));
				dataBlocksArray.add(queryDataMap);
			}

			map.put("QueryData", dataBlocksArray);

			// add the TransactionData map
			OSDMap transMap = new OSDMap(1);
			transMap.put("TransactionID", OSD.FromUUID(transactionID));
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
			agentID = agentDataMap.get("AgentID").AsUUID();
			queryID = agentDataMap.get("QueryID").AsUUID();

			OSDArray dataBlocksArray = (OSDArray) map.get("QueryData");
			queryDataBlocks = new QueryData[dataBlocksArray.size()];
			for (int i = 0; i < dataBlocksArray.size(); i++) {
				OSDMap dataMap = (OSDMap) dataBlocksArray.get(i);
				QueryData data = new QueryData();
				data.actualArea = dataMap.get("ActualArea").AsInteger();
				data.billableArea = dataMap.get("BillableArea").AsInteger();
				data.description = dataMap.get("Desc").AsString();
				data.dwell = (float) dataMap.get("Dwell").AsReal();
				data.flags = dataMap.get("Flags").AsInteger();
				data.globalX = (float) dataMap.get("GlobalX").AsReal();
				data.globalY = (float) dataMap.get("GlobalY").AsReal();
				data.globalZ = (float) dataMap.get("GlobalZ").AsReal();
				data.name = dataMap.get("Name").AsString();
				data.ownerID = dataMap.get("OwnerID").AsUUID();
				data.price = dataMap.get("Price").AsInteger();
				data.simName = dataMap.get("SimName").AsString();
				data.snapShotID = dataMap.get("SnapshotID").AsUUID();
				data.productSku = dataMap.get("ProductSKU").AsString();
				queryDataBlocks[i] = data;
			}

			OSDArray transactionArray = (OSDArray) map.get("TransactionData");
			OSDMap transactionDataMap = (OSDMap) transactionArray.get(0);
			transactionID = transactionDataMap.get("TransactionID").AsUUID();
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
			prefsMap.put("max", OSD.FromString(maxAccess));
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
			maxAccess = prefsMap.get("max").AsString();
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
			agentMap.put("AgentID", OSD.FromUUID(agentID));
			OSDArray agentDataArray = new OSDArray(1);
			agentDataArray.add(agentMap);
			map.put("AgentData", agentDataArray);

			OSDMap queryMap = new OSDMap(1);
			queryMap.put("QueryID", OSD.FromUUID(queryID));
			OSDArray queryDataArray = new OSDArray(1);
			queryDataArray.add(queryMap);
			map.put("QueryData", queryDataArray);

			OSDArray queryReplyArray = new OSDArray();
			for (int i = 0; i < queryReplies.length; i++) {
				OSDMap queryReply = new OSDMap(100);
				queryReply.put("ActualArea", OSD.FromInteger(queryReplies[i].actualArea));
				queryReply.put("Auction", OSD.FromBoolean(queryReplies[i].auction));
				queryReply.put("ForSale", OSD.FromBoolean(queryReplies[i].forSale));
				queryReply.put("Name", OSD.FromString(queryReplies[i].name));
				queryReply.put("ParcelID", OSD.FromUUID(queryReplies[i].parcelID));
				queryReply.put("ProductSKU", OSD.FromString(queryReplies[i].productSku));
				queryReply.put("SalePrice", OSD.FromInteger(queryReplies[i].salePrice));

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
			agentID = agentDataMap.get("AgentID").AsUUID();

			OSDArray queryDataArray = (OSDArray) map.get("QueryData");
			OSDMap queryDataMap = (OSDMap) queryDataArray.get(0);
			queryID = queryDataMap.get("QueryID").AsUUID();

			OSDArray queryRepliesArray = (OSDArray) map.get("QueryReplies");

			queryReplies = new QueryReply[queryRepliesArray.size()];
			for (int i = 0; i < queryRepliesArray.size(); i++) {
				QueryReply reply = new QueryReply();
				OSDMap replyMap = (OSDMap) queryRepliesArray.get(i);
				reply.actualArea = replyMap.get("ActualArea").AsInteger();
				reply.auction = replyMap.get("Auction").AsBoolean();
				reply.forSale = replyMap.get("ForSale").AsBoolean();
				reply.name = replyMap.get("Name").AsString();
				reply.parcelID = replyMap.get("ParcelID").AsUUID();
				reply.productSku = replyMap.get("ProductSKU").AsString();
				reply.salePrice = replyMap.get("SalePrice").AsInteger();

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
					map.put("bump", OSD.FromInteger(bump.getValue()));
					map.put("colors", OSD.FromColor4(color));
					map.put("fullbright", OSD.FromBoolean(fullbright));
					map.put("glow", OSD.FromReal(glow));
					map.put("imageid", OSD.FromUUID(imageID));
					map.put("imagerot", OSD.FromReal(imageRot));
					map.put("media_flags", OSD.FromInteger(mediaFlags));
					map.put("offsets", OSD.FromReal(offsetS));
					map.put("offsett", OSD.FromReal(offsetT));
					map.put("scales", OSD.FromReal(scaleS));
					map.put("scalet", OSD.FromReal(scaleT));

					return map;
				}

				public Face(OSDMap map) {
					bump = Bumpiness.setValue(map.get("bump").AsInteger());
					color = map.get("colors").AsColor4();
					fullbright = map.get("fullbright").AsBoolean();
					glow = (float) map.get("glow").AsReal();
					imageID = map.get("imageid").AsUUID();
					imageRot = (float) map.get("imagerot").AsReal();
					mediaFlags = map.get("media_flags").AsInteger();
					offsetS = (float) map.get("offsets").AsReal();
					offsetT = (float) map.get("offsett").AsReal();
					scaleS = (float) map.get("scales").AsReal();
					scaleT = (float) map.get("scalet").AsReal();
				}
			}

			// TODO:FIXME
			// Why is this class not using the interfaces?
			public class ExtraParam {
				public ExtraParamType type;
				public byte[] extraParamData;

				public OSDMap serialize() {
					OSDMap map = new OSDMap();
					map.put("extra_parameter", OSD.FromInteger(type.getValue()));
					map.put("param_data", OSD.FromBinary(extraParamData));

					return map;
				}

				public ExtraParam(OSDMap map) {
					type = ExtraParamType.setValue(map.get("extra_parameter").AsInteger());
					extraParamData = map.get("param_data").AsBinary();
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

				map.put("group-id", OSD.FromUUID(groupID));
				map.put("material", OSD.FromInteger(material.getValue()));
				map.put("name", OSD.FromString(name));
				map.put("pos", OSD.FromVector3(position));
				map.put("rotation", OSD.FromQuaternion(rotation));
				map.put("scale", OSD.FromVector3(scale));

				// Extra params
				OSDArray extra_parameters = new OSDArray();
				if (extraParams != null) {
					for (int i = 0; i < extraParams.length; i++)
						extra_parameters.add(extraParams[i].serialize());
				}
				map.put("extra_parameters", extra_parameters);

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
				path.put("begin", OSD.FromReal(pathBegin));
				path.put("curve", OSD.FromInteger(pathCurve));
				path.put("end", OSD.FromReal(pathEnd));
				path.put("radius_offset", OSD.FromReal(radiusOffset));
				path.put("revolutions", OSD.FromReal(revolutions));
				path.put("scale_x", OSD.FromReal(scaleX));
				path.put("scale_y", OSD.FromReal(scaleY));
				path.put("shear_x", OSD.FromReal(shearX));
				path.put("shear_y", OSD.FromReal(shearY));
				path.put("skew", OSD.FromReal(skew));
				path.put("taper_x", OSD.FromReal(taperX));
				path.put("taper_y", OSD.FromReal(taperY));
				path.put("twist", OSD.FromReal(twist));
				path.put("twist_begin", OSD.FromReal(twistBegin));
				shape.put("path", path);
				OSDMap profile = new OSDMap();
				profile.put("begin", OSD.FromReal(profileBegin));
				profile.put("curve", OSD.FromInteger(profileCurve));
				profile.put("end", OSD.FromReal(profileEnd));
				profile.put("hollow", OSD.FromReal(profileHollow));
				shape.put("profile", profile);
				OSDMap sculpt = new OSDMap();
				sculpt.put("id", OSD.FromUUID(sculptID));
				sculpt.put("type", OSD.FromInteger(sculptType.getValue()));
				shape.put("sculpt", sculpt);
				map.put("shape", shape);

				return map;
			}

			public Object(OSDMap map) {
				if (map != null) {
					groupID = map.get("group-id").AsUUID();
					material = libomv.primitives.Primitive.Material.setValue(map.get("material").AsInteger());
					name = map.get("name").AsString();
					position = map.get("pos").AsVector3();
					rotation = map.get("rotation").AsQuaternion();
					scale = map.get("scale").AsVector3();

					// Extra params
					OSDArray extra_parameters = (OSDArray) map.get("extra_parameters");
					if (extra_parameters != null) {
						extraParams = new ExtraParam[extra_parameters.size()];
						for (int i = 0; i < extra_parameters.size(); i++) {
							extraParams[i] = new ExtraParam((OSDMap) extra_parameters.get(i));
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
					pathBegin = (float) path.get("begin").AsReal();
					pathCurve = path.get("curve").AsInteger();
					pathEnd = (float) path.get("end").AsReal();
					radiusOffset = (float) path.get("radius_offset").AsReal();
					revolutions = (float) path.get("revolutions").AsReal();
					scaleX = (float) path.get("scale_x").AsReal();
					scaleY = (float) path.get("scale_y").AsReal();
					shearX = (float) path.get("shear_x").AsReal();
					shearY = (float) path.get("shear_y").AsReal();
					skew = (float) path.get("skew").AsReal();
					taperX = (float) path.get("taper_x").AsReal();
					taperY = (float) path.get("taper_y").AsReal();
					twist = (float) path.get("twist").AsReal();
					twistBegin = (float) path.get("twist_begin").AsReal();

					OSDMap profile = (OSDMap) shape.get("profile");
					profileBegin = (float) profile.get("begin").AsReal();
					profileCurve = profile.get("curve").AsInteger();
					profileEnd = (float) profile.get("end").AsReal();
					profileHollow = (float) profile.get("hollow").AsReal();

					OSDMap sculpt = (OSDMap) shape.get("sculpt");
					if (sculpt != null) {
						sculptID = sculpt.get("id").AsUUID();
						sculptType = libomv.primitives.Primitive.SculptType.setValue(sculpt.get("type").AsInteger());
					} else {
						sculptID = UUID.Zero;
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

			map.put("current_url", OSD.FromString(url));
			map.put("object_id", OSD.FromUUID(primID));
			map.put("texture_index", OSD.FromInteger(face));

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
			url = map.get("current_url").AsString();
			primID = map.get("object_id").AsUUID();
			face = map.get("texture_index").AsInteger();
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
			map.put("object_id", OSD.FromUUID(primID));
			map.put("verb", OSD.FromString(verb));
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
			primID = map.get("object_id").AsUUID();
			verb = map.get("verb").AsString();
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
			map.put("object_id", OSD.FromUUID(primID));

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

			map.put("object_media_version", OSD.FromString(version));
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
			primID = map.get("object_id").AsUUID();

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
			version = map.get("object_media_version").AsString();
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
			map.put("object_id", OSD.FromUUID(primID));

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

			map.put("verb", OSD.FromString(verb));
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
			primID = map.get("object_id").AsUUID();

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
			verb = map.get("verb").AsString();
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
				if (map.get("verb").AsString() == "GET") {
					request = new ObjectMediaRequest();
					request.deserialize(map);
				} else if (map.get("verb").AsString() == "UPDATE") {
					request = new ObjectMediaUpdate();
					request.deserialize(map);
				}
			} else if (map.containsKey("object_media_version")) {
				request = new ObjectMediaResponse();
				request.deserialize(map);
			} else
				logger.warn(
						"Unable to deserialize ObjectMedia: No message handler exists for method: " + map.AsString());
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
				materialData = Helpers.ZDecompressOSD(new ByteArrayInputStream(map.get("Zipped").AsBinary()));
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
					objectIDs[i] = array.get(i).AsUUID();
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
				array.add(OSD.FromUUID(objectIDs[i]));
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

				cost.linkCost = values.get("linked_set_resource_cost").AsReal();
				cost.objectCost = values.get("resource_cost").AsReal();
				cost.physicsCost = values.get("physics_cost").AsReal();
				cost.linkPhysicsCost = values.get("linked_set_physics_cost").AsReal();
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
				values.put("linked_set_resource_cost", OSD.FromReal(cost.linkCost));
				values.put("resource_cost", OSD.FromReal(cost.objectCost));
				values.put("physics_cost", OSD.FromReal(cost.physicsCost));
				values.put("linked_set_physics_cost", OSD.FromReal(cost.linkPhysicsCost));

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

				physics.shapeType = values.get("PhysicsShapeType").AsInteger();
				if (values.containsKey("Density")) {
					physics.density = values.get("Density").AsReal();
					physics.friction = values.get("Friction").AsReal();
					physics.restitution = values.get("Restitution").AsReal();
					physics.gravityMultiplier = values.get("GravityMultiplier").AsReal();
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
				values.put("PhysicsShapeType", OSD.FromReal(physics.shapeType));
				values.put("Density", OSD.FromReal(physics.density));
				values.put("Friction", OSD.FromReal(physics.friction));
				values.put("Restitution", OSD.FromReal(physics.restitution));
				values.put("GravityMultiplier", OSD.FromReal(physics.gravityMultiplier));

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
		public HashMap<String, Integer> resources;

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
			objectID = obj.get("id").AsUUID();
			name = obj.get("name").AsString();
			location = obj.get("location").AsVector3d();
			groupOwned = obj.get("is_group_owned").AsBoolean();
			ownerID = obj.get("owner_id").AsUUID();

			OSDMap resourcesOSD = (OSDMap) obj.get("resources");
			resources = new HashMap<String, Integer>(resourcesOSD.size());
			for (Entry<String, OSD> kvp : resourcesOSD.entrySet()) {
				resources.put(kvp.getKey(), kvp.getValue().AsInteger());
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
			parcelID = map.get("id").AsUUID();
			localID = map.get("local_id").AsInteger();
			name = map.get("name").AsString();
			groupOwned = map.get("is_group_owned").AsBoolean();
			ownerID = map.get("owner_id").AsUUID();

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
		public HashMap<String, Integer> summaryAvailable;
		// Summary resource usage, keys are resource names, values are resource
		// usage for that specific resource
		public HashMap<String, Integer> summaryUsed;

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
			summaryAvailable = new HashMap<String, Integer>();
			summaryUsed = new HashMap<String, Integer>();

			OSDMap summary = (OSDMap) map.get("summary");
			OSDArray available = (OSDArray) summary.get("available");
			OSDArray used = (OSDArray) summary.get("used");

			for (int i = 0; i < available.size(); i++) {
				OSDMap limit = (OSDMap) available.get(i);
				summaryAvailable.put(limit.get("type").AsString(), limit.get("amount").AsInteger());
			}

			for (int i = 0; i < used.size(); i++) {
				OSDMap limit = (OSDMap) used.get(i);
				summaryUsed.put(limit.get("type").AsString(), limit.get("amount").AsInteger());
			}
		}
	}

	public class AttachmentResourcesMessage extends BaseResourcesInfo {
		BaseResourcesInfo summaryInfoBlock;

		// Per attachment point object resource usage
		public HashMap<AttachmentPoint, ObjectResourcesDetail[]> attachments;

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
				attachments = new HashMap<AttachmentPoint, ObjectResourcesDetail[]>();

				for (int i = 0; i < attachmentsOSD.size(); i++) {
					OSDMap attachment = (OSDMap) attachmentsOSD.get(i);
					AttachmentPoint pt = AttachmentPoint.setValue(attachment.get("location").AsString());

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
			map.put("parcel_id", OSD.FromUUID(parcelID));
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
			parcelID = map.get("parcel_id").AsUUID();
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
				map.put("ScriptResourceSummary", OSD.FromString(scriptResourceSummary.toString()));
			}

			if (scriptResourceDetails != null) {
				map.put("ScriptResourceDetails", OSD.FromString(scriptResourceDetails.toString()));
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
					scriptResourceSummary = new URI(map.get("ScriptResourceSummary").AsString());
				}
				if (map.containsKey("ScriptResourceDetails")) {
					scriptResourceDetails = new URI(map.get("ScriptResourceDetails").AsString());
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
					agentsOSD.add(agents[i].GetOSD());
				}
			}

			OSDArray badIDsOSD = new OSDArray();
			if (badIDs != null && badIDs.length > 0) {
				for (int i = 0; i < badIDs.length; i++) {
					badIDsOSD.add(OSD.FromUUID(badIDs[i]));
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
						agents[i].FromOSD(agentsOSD.get(i));
					}
				}
			}

			if (map.get("bad_ids").getType() == OSDType.Array) {
				OSDArray badIDsOSD = (OSDArray) map.get("bad_ids");
				if (badIDsOSD.size() > 0) {
					badIDs = new UUID[badIDsOSD.size()];

					for (int i = 0; i < badIDsOSD.size(); i++) {
						badIDs[i] = badIDsOSD.get(i).AsUUID();
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
			names.add(OSD.FromString(oldDisplayName));
			names.add(OSD.FromString(newDisplayName));

			OSDMap name = new OSDMap();
			name.put("display_name", names);
			return name;
		}

		@Override
		public void deserialize(OSDMap map) {
			OSDArray names = (OSDArray) map.get("display_name");
			oldDisplayName = names.get(0).AsString();
			newDisplayName = names.get(1).AsString();
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
			ret.put("content", displayName.GetOSD());
			ret.put("reason", OSD.FromString(reason));
			ret.put("status", OSD.FromInteger(status));
			return ret;
		}

		@Override
		public void deserialize(OSDMap map) {
			displayName.FromOSD(map.get("content"));
			reason = map.get("reason").AsString();
			status = map.get("status").AsInteger();
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
			OSDMap agent = (OSDMap) displayName.GetOSD();
			agent.put("old_display_name", OSD.FromString(oldDisplayName));
			OSDMap ret = new OSDMap();
			ret.put("agent", agent);
			return ret;
		}

		@Override
		public void deserialize(OSDMap map) {
			OSDMap agent = (OSDMap) map.get("agent");
			displayName.FromOSD(agent);
			oldDisplayName = agent.get("old_display_name").AsString();
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