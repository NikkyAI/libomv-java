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
package libomv.assets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.model.asset.AssetType;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.types.UUID;
import libomv.utils.Helpers;

// Represents Mesh asset
public class AssetMesh extends AssetItem {
	private static final Logger logger = Logger.getLogger(AssetMesh.class);

	// Decoded mesh data
	private OSDMap meshData;

	/**
	 * Initializes a new instance of an AssetMesh object with parameters
	 *
	 * @param assetID
	 *            A unique <see cref="UUID"/> specific to this asset
	 * @param assetData
	 *            A byte array containing the raw asset data
	 */
	public AssetMesh(UUID assetID, byte[] assetData) {
		super(assetID, assetData);
	}

	public OSDMap getMeshData() {
		return meshData;
	}

	public void setMeshData(OSDMap meshData) {
		invalidateAssetData();
		this.meshData = meshData;
	}

	// Override the base classes AssetType
	@Override
	public AssetType getAssetType() {
		return AssetType.Mesh;
	}

	// TODO: Encodes Collada file into LLMesh format
	@Override
	protected void encode() {
	}

	/**
	 * Decodes mesh asset. See <see
	 * cref="OpenMetaverse.Rendering.FacetedMesh.TryDecodeFromAsset" to further
	 * decode it for rendering
	 *
	 * @returns true
	 */
	@Override
	protected boolean decode() {
		meshData = new OSDMap();

		if (assetData == null)
			return false;

		InputStream data = new ByteArrayInputStream(assetData);
		try {
			OSDMap header = (OSDMap) OSDParser.deserialize(data, Helpers.UTF8_ENCODING);
			meshData.put("asset_header", header);
			data.mark(assetData.length);

			for (String partName : header.keySet()) {
				OSD value = header.get(partName);
				if (value.getType() == OSDType.Map) {
					OSDMap partInfo = (OSDMap) value;
					int offset = partInfo.get("offset").asInteger(), size = partInfo.get("size").asInteger();
					if (offset >= 0 && size > 0) {
						data.reset();
						data.skip(offset);
						value = Helpers.ZDecompressOSD(data);
					}
				}
				meshData.put(partName, value);
			}
			return true;
		} catch (Exception ex) {
			logger.error("Failed to decode mesh asset", ex);
			return false;
		} finally {
			try {
				data.close();
			} catch (IOException e) {
				// Ignore this final exception as we can't do anything at this point.
			}
		}
	}
}