/**
 * Copyright (c) 2009, openmetaverse.org
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
package libomv.assets;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.zip.InflaterInputStream;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

// Represents Mesh asset
public class AssetMesh extends AssetItem
{
    // Decoded mesh data
    public OSDMap MeshData;

    // Override the base classes AssetType
	@Override
	public AssetItem.AssetType getAssetType()
	{
		return AssetItem.AssetType.Mesh;
	}

	// Initializes a new instance of an AssetMesh object
	public AssetMesh() { }

    /**
     * Initializes a new instance of an AssetMesh object with parameters
     *
     * @param assetID A unique <see cref="UUID"/> specific to this asset
     * @param assetData A byte array containing the raw asset data
     */
    public AssetMesh(UUID assetID, byte[] assetData)
    {
        super(assetID, assetData);
    }

    // TODO: Encodes Collada file into LLMesh format
	@Override
	public void Encode()
	{
	}

    /**
     * Decodes mesh asset. See <see cref="OpenMetaverse.Rendering.FacetedMesh.TryDecodeFromAsset"
     * to further decode it for rendering
     * 
     * @returns true
     */
	@Override
	public boolean Decode()
    {
        MeshData = new OSDMap();
        InputStream data = new ByteArrayInputStream(AssetData);
        try
        {
            OSDMap header = (OSDMap)OSDParser.deserialize(data, Helpers.UTF8_ENCODING);
            data.mark(AssetData.length);

            for (String partName : header.keySet())
            {
              	OSD value = header.get(partName);
                if (value.getType() != OSDType.Map)
                {
                    MeshData.put(partName, value);
                    continue;
                }

                OSDMap partInfo = (OSDMap)value;
                if (partInfo.get("offset").AsInteger() < 0 || partInfo.get("size").AsInteger() == 0)
                {
                    MeshData.put(partName, partInfo);
                    continue;
                }
                data.reset();
                data.skip(partInfo.get("offset").AsInteger());
                InflaterInputStream inflate = new InflaterInputStream(data);
                try
                {
                	MeshData.put(partName, OSDParser.deserialize(inflate, Helpers.UTF8_ENCODING));
                }
                finally
                {
                	inflate.close();
                }
            }
            return true;
        }
        catch (Exception ex)
        {
        	try
        	{
				data.close();
			}
        	catch (IOException e) {}
            Logger.Log("Failed to decode mesh asset", LogLevel.Error, ex);
            return false;
        }
    }
}
