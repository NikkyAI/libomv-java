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
package libomv.io;

import org.apache.log4j.Logger;

import libomv.TerrainCompressor;
import libomv.model.Simulator;
import libomv.model.terrain.GroupHeader;
import libomv.model.terrain.LandPatchReceivedCallbackArgs;
import libomv.model.terrain.LayerType;
import libomv.model.terrain.TerrainHeader;
import libomv.model.terrain.TerrainPatch;
import libomv.packets.LayerDataPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.types.PacketCallback;
import libomv.types.Vector2;
import libomv.utils.BitPack;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Settings.SettingsUpdateCallbackArgs;

public class TerrainManager implements PacketCallback {
	private static final Logger logger = Logger.getLogger(TerrainManager.class);

	public CallbackHandler<LandPatchReceivedCallbackArgs> OnLandPatchReceived;

	// #endregion

	private class SettingsUpdate implements Callback<SettingsUpdateCallbackArgs> {
		@Override
		public boolean callback(SettingsUpdateCallbackArgs params) {
			String key = params.getName();
			if (key == null) {
				storeLandPatches = _Client.Settings.getBool(LibSettings.STORE_LAND_PATCHES);
			} else if (key.equals(LibSettings.STORE_LAND_PATCHES)) {
				storeLandPatches = params.getValue().AsBoolean();
			}
			return false;
		}
	}

	private GridClient _Client;
	private boolean storeLandPatches;

	public TerrainManager(GridClient client) {
		_Client = client;

		storeLandPatches = _Client.Settings.getBool(LibSettings.STORE_LAND_PATCHES);
		_Client.Settings.onSettingsUpdate.add(new SettingsUpdate());

		_Client.Network.RegisterCallback(PacketType.LayerData, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case LayerData:
			LayerDataHandler(packet, simulator);
		default:
			break;
		}
	}

	private void DecompressLand(SimulatorManager simulator, BitPack bitpack, GroupHeader group) {
		int x, y;
		int[] patches = new int[32 * 32];
		int count = 0;

		while (true) {
			TerrainHeader header = TerrainCompressor.decodePatchHeader(bitpack);

			if (header.quantWBits == TerrainCompressor.END_OF_PATCHES)
				break;

			x = header.getX();
			y = header.getY();

			if (x >= TerrainCompressor.PATCHES_PER_EDGE || y >= TerrainCompressor.PATCHES_PER_EDGE) {
				logger.warn(GridClient.Log(String.format(
						"Invalid LayerData land packet, x=%d, y=%d, dc_offset=%f, range=%d, quant_wbits=%d, patchids=%d, count=%d",
						x, y, header.dcOffset, header.range, header.quantWBits, header.patchIDs, count), _Client));
				return;
			}

			// Decode this patch
			TerrainCompressor.decodePatch(patches, bitpack, header, group.patchSize);

			// Decompress this patch
			float[] heightmap = TerrainCompressor.decompressPatch(patches, header, group);

			count++;

			try {
				OnLandPatchReceived
						.dispatch(new LandPatchReceivedCallbackArgs(simulator, x, y, group.patchSize, heightmap));
			} catch (Exception e) {
				logger.error(GridClient.Log(e.getMessage(), _Client), e);
			}

			if (storeLandPatches) {
				TerrainPatch patch = new TerrainPatch();
				patch.data = heightmap;
				patch.x = x;
				patch.y = y;
				simulator.Terrain[y * 16 + x] = patch;
			}
		}
	}

	private void DecompressWind(SimulatorManager simulator, BitPack bitpack, GroupHeader group) {
		int[] patches = new int[32 * 32];

		// Ignore the simulator stride value
		group.stride = group.patchSize;

		// Each wind packet contains the wind speeds and direction for the entire
		// simulator
		// stored as two float arrays. The first array is the X value of the wind speed
		// at
		// each 16x16m block, second is the Y value.
		// wind_speed = distance(x,y to 0,0)
		// wind_direction = vec2(x,y)

		// X values
		TerrainHeader header = TerrainCompressor.decodePatchHeader(bitpack);
		TerrainCompressor.decodePatch(patches, bitpack, header, group.patchSize);
		float[] xvalues = TerrainCompressor.decompressPatch(patches, header, group);

		// Y values
		header = TerrainCompressor.decodePatchHeader(bitpack);
		TerrainCompressor.decodePatch(patches, bitpack, header, group.patchSize);
		float[] yvalues = TerrainCompressor.decompressPatch(patches, header, group);

		if (storeLandPatches) {
			for (int i = 0; i < 256; i++)
				simulator.WindSpeeds[i] = new Vector2(xvalues[i], yvalues[i]);
		}
	}

	private void DecompressCloud(Simulator simulator, BitPack bitpack, GroupHeader group) {
		// FIXME:
	}

	private void LayerDataHandler(Packet packet, Simulator sim) {
		SimulatorManager simulator = (SimulatorManager) sim;
		LayerDataPacket layer = (LayerDataPacket) packet;
		BitPack bitpack = new BitPack(layer.LayerData.getData());
		GroupHeader header = new GroupHeader();
		LayerType type = LayerType.setValue(layer.Type);

		// Stride
		header.stride = bitpack.unpackBits(16);
		// Patch size
		header.patchSize = bitpack.unpackBits(8);
		// Layer type
		header.type = LayerType.setValue(bitpack.unpackBits(8));

		switch (type) {
		case Land:
			if (OnLandPatchReceived.count() > 0 || storeLandPatches)
				DecompressLand(simulator, bitpack, header);
			break;
		case Water:
			logger.error(GridClient.Log("Got a Water LayerData packet, implement me!", _Client));
			break;
		case Wind:
			DecompressWind(simulator, bitpack, header);
			break;
		case Cloud:
			DecompressCloud(simulator, bitpack, header);
			break;
		default:
			logger.warn(GridClient.Log("Unrecognized LayerData type " + type.toString(), _Client));
			break;
		}
	}
}
