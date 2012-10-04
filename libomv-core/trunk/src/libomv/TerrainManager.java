/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Portions Copyright (c) 2009-2012, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without 
 *   modification, are permitted provided that the following conditions are met:
 *
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

import libomv.packets.LayerDataPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.types.PacketCallback;
import libomv.types.Vector2;
import libomv.utils.BitPack;
import libomv.utils.Callback;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.Settings.SettingsUpdateCallbackArgs;

public class TerrainManager implements PacketCallback
{
	public enum LayerType
	{
		Land(0x4C), Water(0x57), Wind(0x37), Cloud(0x38);

		public static LayerType setValue(int value)
		{
			for (LayerType e : values())
			{
				if (e._value == value)
				{
					return e;
				}
			}
			return Land;
		}

		public byte getValue()
		{
			return _value;
		}

		private final byte _value;

		private LayerType(int value)
		{
			_value = (byte) value;
		}
	}

	public class TerrainHeader
	{
		public float DCOffset;
		public int Range;
		public int QuantWBits;
		public int PatchIDs;
		public int WordBits;

		public int getX()
		{
			return PatchIDs >> 5;
		}

		public void setX(int value)
		{
			PatchIDs += (value << 5);
		}

		public int getY()
		{
			return PatchIDs & 0x1F;
		}

		public void setY(int value)
		{
			PatchIDs |= value & 0x1F;
		}
	}

	public final class GroupHeader
	{
		public int Stride;
		public int PatchSize;
		public LayerType Type;
	}

	public class TerrainPatch
	{
		/* X position of this patch */
		public int X;
		/* Y position of this patch */
		public int Y;
		/* A 16x16 array of floats holding decompressed layer data */
		public float[] Data;
	}

	// #region EventHandling

	// Provides data for LandPatchReceived
	public class LandPatchReceivedCallbackArgs implements CallbackArgs
	{
	    private Simulator m_Simulator;
	    private int m_X;
	    private int m_Y;
	    private int m_PatchSize;
	    private float[] m_HeightMap;

	    // Simulator from that sent the data
	    public Simulator getSimulator()
	    {
	    	return m_Simulator;
	    }
	    
	    // Sim coordinate of the patch
	    public int getX()
	    { 
	    	return m_X;
	    }
	    
	    // Sim coordinate of the patch
	    public int getY()
	    {
	    	return m_Y;
	    }
	    
	    // Size of tha patch</summary>
	    public int getPatchSize()
	    {
	    	return m_PatchSize;
	    }
	    
	    /// <summary>Heightmap for the patch</summary>
	    public float[] getHeightMap()
	    {
	    	return m_HeightMap;
	    }
	
	    public LandPatchReceivedCallbackArgs(Simulator simulator, int x, int y, int patchSize, float[] heightMap)
	    {
	        this.m_Simulator = simulator;
	        this.m_X = x;
	        this.m_Y = y;
	        this.m_PatchSize = patchSize;
	        this.m_HeightMap = heightMap;
	    }
	}
	public CallbackHandler<LandPatchReceivedCallbackArgs> OnLandPatchReceived;

    // #endregion

	private class SettingsUpdate implements Callback<SettingsUpdateCallbackArgs>
	{
		@Override
		public boolean callback(SettingsUpdateCallbackArgs params)
		{
			String key = params.getName();
			if (key == null)
			{
			    storeLandPatches = _Client.Settings.getBool(LibSettings.STORE_LAND_PATCHES);
			}
			else if (key.equals(LibSettings.STORE_LAND_PATCHES))
			{
				storeLandPatches = params.getValue().AsBoolean();
			}
			return false;
		}
	}
	
    private GridClient _Client;
    private boolean storeLandPatches;
    
    public TerrainManager(GridClient client)
    {
        _Client = client;

        storeLandPatches = _Client.Settings.getBool(LibSettings.STORE_LAND_PATCHES);
		_Client.Settings.OnSettingsUpdate.add(new SettingsUpdate());
		
		_Client.Network.RegisterCallback(PacketType.LayerData, this);
    }

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case LayerData:
				LayerDataHandler(packet,simulator);
		}
	}

	private void DecompressLand(Simulator simulator, BitPack bitpack, GroupHeader group)
    {
        int x, y;
        int[] patches = new int[32 * 32];
        int count = 0;

        while (true)
        {
        	TerrainHeader header = TerrainCompressor.DecodePatchHeader(this, bitpack);

            if (header.QuantWBits == TerrainCompressor.END_OF_PATCHES)
                break;

            x = header.getX();
            y = header.getY();

            if (x >= TerrainCompressor.PATCHES_PER_EDGE || y >= TerrainCompressor.PATCHES_PER_EDGE)
            {
                Logger.Log(String.format(
                    "Invalid LayerData land packet, x=%d, y=%d, dc_offset=%f, range=%d, quant_wbits=%d, patchids=%d, count=%d",
                    x, y, header.DCOffset, header.Range, header.QuantWBits, header.PatchIDs, count),
                    LogLevel.Warning, _Client);
                return;
            }

            // Decode this patch
            TerrainCompressor.DecodePatch(patches, bitpack, header, group.PatchSize);

            // Decompress this patch
            float[] heightmap = TerrainCompressor.DecompressPatch(patches, header, group);

            count++;

            try
            { 
            	OnLandPatchReceived.dispatch(new LandPatchReceivedCallbackArgs(simulator, x, y, group.PatchSize, heightmap));
            }
            catch (Exception e)
            { 
            	Logger.Log(e.getMessage(), LogLevel.Error, _Client, e);
            }

            if (storeLandPatches)
            {
                TerrainPatch patch = new TerrainPatch();
                patch.Data = heightmap;
                patch.X = x;
                patch.Y = y;
                simulator.Terrain[y * 16 + x] = patch;
            }
        }
    }

    private void DecompressWind(Simulator simulator, BitPack bitpack, GroupHeader group)
    {
        int[] patches = new int[32 * 32];

        // Ignore the simulator stride value
        group.Stride = group.PatchSize;

        // Each wind packet contains the wind speeds and direction for the entire simulator
        // stored as two float arrays. The first array is the X value of the wind speed at
        // each 16x16m block, second is the Y value.
        // wind_speed = distance(x,y to 0,0)
        // wind_direction = vec2(x,y)

        // X values
        TerrainHeader header = TerrainCompressor.DecodePatchHeader(this, bitpack);
        TerrainCompressor.DecodePatch(patches, bitpack, header, group.PatchSize);
        float[] xvalues = TerrainCompressor.DecompressPatch(patches, header, group);

        // Y values
        header = TerrainCompressor.DecodePatchHeader(this, bitpack);
        TerrainCompressor.DecodePatch(patches, bitpack, header, group.PatchSize);
        float[] yvalues = TerrainCompressor.DecompressPatch(patches, header, group);

        if (storeLandPatches)
        {
            for (int i = 0; i < 256; i++)
                simulator.WindSpeeds[i] = new Vector2(xvalues[i], yvalues[i]);
        }
    }

    private void DecompressCloud(Simulator simulator, BitPack bitpack, GroupHeader group)
    {
        // FIXME:
    }

    private void LayerDataHandler(Packet packet, Simulator simulator)
    {
        LayerDataPacket layer = (LayerDataPacket)packet;
        BitPack bitpack = new BitPack(layer.LayerData.getData());
        GroupHeader header = new GroupHeader();
        LayerType type = LayerType.setValue(layer.Type);

        // Stride
        header.Stride = bitpack.UnpackBits(16);
        // Patch size
        header.PatchSize = bitpack.UnpackBits(8);
        // Layer type
        header.Type =  LayerType.setValue(bitpack.UnpackBits(8));

        switch (type)
        {
            case Land:
                if (OnLandPatchReceived.count() > 0 || storeLandPatches)
                    DecompressLand(simulator, bitpack, header);
                break;
            case Water:
                Logger.Log("Got a Water LayerData packet, implement me!", LogLevel.Error, _Client);
                break;
            case Wind:
                DecompressWind(simulator, bitpack, header);
                break;
            case Cloud:
                DecompressCloud(simulator, bitpack, header);
                break;
            default:
                Logger.Log("Unrecognized LayerData type " + type.toString(), LogLevel.Warning, _Client);
                break;
        }
    }
}
