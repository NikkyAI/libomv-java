/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
 * Copyright (c) 2009-2011, Frederick Martian
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

import java.util.Hashtable;

import libomv.ParcelManager.Parcel;
import libomv.packets.ParcelDividePacket;
import libomv.packets.ParcelJoinPacket;
import libomv.packets.ParcelPropertiesRequestPacket;
import libomv.primitives.Primitive;
import libomv.types.UUID;
import libomv.types.Vector3;

// Represents a region (also known as a sim) in Second Life.
public class Region
{
	public ParcelCompleteCallback OnParcelCompletion;

	// FIXME: This whole setup is fscked in a really bad way. We can't be
	// locking on a publically accessible container, and we shouldn't have
	// publically accessible containers anyways because external programs
	// might be iterating through them or modifying them when internally
	// we are doing the opposite. The best way to fix this will be
	// privatizing and adding helper functions to access the dictionary
	public Hashtable<Integer, Parcel> Parcels;

    /*  */
//    public int Flags; /* Simulator.RegionFlags */
    /*  */
//    public byte Access; /* Simulator.SimAccess */
    /*  */
//    public float BillableFactor;

//    public UUID RegionID;

//	public long Handle;

//	public String Name;

//	public byte[] ParcelOverlay;

//	public int ParcelOverlaysReceived;

	// 64x64 Array of parcels which have been successfully downloaded
	// (and their LocalID's, 0 = Null)
	public int[][] ParcelMarked;

	// Flag to indicate whether we are downloading a sim's parcels
	public boolean ParcelDownloading;

	// Flag to indicate whether to get Dwell values automatically (NOT USED
	// YET). Call Parcel.GetDwell() instead
	public boolean ParcelDwell;

//	public float TerrainHeightRange00;

//	public float TerrainHeightRange01;

//	public float TerrainHeightRange10;

//	public float TerrainHeightRange11;

//	public float TerrainStartHeight00;

//	public float TerrainStartHeight01;

//	public float TerrainStartHeight10;

//	public float TerrainStartHeight11;

//	public float WaterHeight;

//	public UUID SimOwner;

//	public UUID TerrainBase0;

//	public UUID TerrainBase1;

//	public UUID TerrainBase2;

//	public UUID TerrainBase3;

//	public UUID TerrainDetail0;

//	public UUID TerrainDetail1;

//	public UUID TerrainDetail2;

//	public UUID TerrainDetail3;

//	public boolean IsEstateManager;

	public EstateTools Estate;

//	private Simulator Simulator;

	public Region(Simulator simulator)
	{
		Simulator = simulator;
		Estate = new EstateTools(Simulator.getClient());
		ID = new UUID();
		ParcelOverlay = new byte[4096];
		ParcelMarked = new int[64][64];

		Parcels = new Hashtable<Integer, Parcel>();

		SimOwner = new UUID();
		TerrainBase0 = new UUID();
		TerrainBase1 = new UUID();
		TerrainBase2 = new UUID();
		TerrainBase3 = new UUID();
		TerrainDetail0 = new UUID();
		TerrainDetail1 = new UUID();
		TerrainDetail2 = new UUID();
		TerrainDetail3 = new UUID();
	}

	public Region(Simulator simulator, UUID id, long handle, String name,
			float[] heightList, UUID simOwner, UUID[] terrainImages,
			boolean isEstateManager)
	{
		Simulator = simulator;
		Estate = new EstateTools(Simulator.getClient());
		ID = id;
		Handle = handle;
		Name = name;
		ParcelOverlay = new byte[4096];
		ParcelMarked = new int[64][64];
		ParcelDownloading = false;
		ParcelDwell = false;

		TerrainHeightRange00 = heightList[0];
		TerrainHeightRange01 = heightList[1];
		TerrainHeightRange10 = heightList[2];
		TerrainHeightRange11 = heightList[3];
		TerrainStartHeight00 = heightList[4];
		TerrainStartHeight01 = heightList[5];
		TerrainStartHeight10 = heightList[6];
		TerrainStartHeight11 = heightList[7];
		WaterHeight = heightList[8];

		SimOwner = simOwner;

		TerrainBase0 = terrainImages[0];
		TerrainBase1 = terrainImages[1];
		TerrainBase2 = terrainImages[2];
		TerrainBase3 = terrainImages[3];
		TerrainDetail0 = terrainImages[4];
		TerrainDetail1 = terrainImages[5];
		TerrainDetail2 = terrainImages[6];
		TerrainDetail3 = terrainImages[7];

		IsEstateManager = isEstateManager;
	}

	public void ParcelSubdivide(float west, float south, float east, float north) throws Exception
	{
		ParcelDividePacket divide = new ParcelDividePacket();
		divide.AgentData.AgentID = Simulator.getClient().Self.getAgentID();
		divide.AgentData.SessionID = Simulator.getClient().Self.getSessionID();
		divide.ParcelData.East = east;
		divide.ParcelData.North = north;
		divide.ParcelData.South = south;
		divide.ParcelData.West = west;

		Simulator.SendPacket(divide);
	}

	public void ParcelJoin(float west, float south, float east, float north) throws Exception {
		ParcelJoinPacket join = new ParcelJoinPacket();
		join.AgentData.AgentID = Simulator.getClient().Self.getAgentID();
		join.AgentData.SessionID = Simulator.getClient().Self.getSessionID();
		join.ParcelData.East = east;
		join.ParcelData.North = north;
		join.ParcelData.South = south;
		join.ParcelData.West = west;

		Simulator.SendPacket(join);
	}

	public void RezObject(Primitive prim, Vector3 position, Vector3 avatarPosition)
	{
		// FIXME:
		// byte[] textureEntry = new byte[40];
		// Array.Copy(prim.Texture.Data, textureEntry, 16);
		// textureEntry[35] = 0xe0; // No clue

		// Packet objectAdd = ObjectManager.ObjectAdd(Client.Protocol, Client.Network.AgentID, LLUUID.GenerateUUID(), avatarPosition, position, prim, textureEntry);
		// Simulator.SendPacket(objectAdd);
	}

	public void FillParcels() throws Exception
	{
		// Begins filling parcels
		ParcelDownloading = true;

		ParcelPropertiesRequestPacket tPacket = new ParcelPropertiesRequestPacket();
		tPacket.AgentData.AgentID = Simulator.getClient().Self.getAgentID();
		tPacket.AgentData.SessionID = Simulator.getClient().Self.getSessionID();
		tPacket.ParcelData.SequenceID = -10000;
		tPacket.ParcelData.West = 0.0f;
		tPacket.ParcelData.South = 0.0f;
		tPacket.ParcelData.East = 0.0f;
		tPacket.ParcelData.North = 0.0f;

		Simulator.SendPacket(tPacket);
	}

	public void ResetParcelDownload() {
		Parcels = new Hashtable<Integer, Parcel>();
		ParcelMarked = new int[64][64];
	}

	public void FilledParcels() {
		if (OnParcelCompletion != null) {
			OnParcelCompletion.parcelCompleteCallback(this);
		}
	}

	@Override
	public int hashCode() {
		return ID.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Region)) {
			return false;
		}

		Region region = (Region) o;

		return (region.ID.equals(ID));
	}
}
