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
package libomv.rendering;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.assets.AssetMesh;
import libomv.primitives.Primitive;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.Helpers;

public class FacetedMesh extends Mesh {
	private static final Logger logger = Logger.getLogger(FacetedMesh.class);

	/// List of primitive faces
	public List<Face> faces;

	/**
	 * Decodes mesh asset into FacetedMesh
	 *
	 * @param prim
	 *            Mesh primitive
	 * @param meshAsset
	 *            Asset retrieved from the asset server
	 * @param lod
	 *            Level of detail
	 * @param mesh
	 *            Resulting decoded FacetedMesh
	 * @returns True if mesh asset decoding was successful
	 */
	public static FacetedMesh tryDecodeFromAsset(Primitive prim, AssetMesh meshAsset, DetailLevel lod) {
		try {
			OSDMap meshData = meshAsset.getMeshData();
			OSD facesOSD = null;

			switch (lod) {
			default:
			case Highest:
				facesOSD = meshData.get("high_lod");
				break;

			case High:
				facesOSD = meshData.get("medium_lod");
				break;

			case Medium:
				facesOSD = meshData.get("low_lod");
				break;

			case Low:
				facesOSD = meshData.get("lowest_lod");
				break;
			}

			if (facesOSD == null || !(facesOSD instanceof OSDArray)) {
				return null;
			}

			FacetedMesh mesh = new FacetedMesh();
			mesh.faces = new ArrayList<>();
			mesh.prim = prim;
			mesh.profile.faces = new ArrayList<>();
			mesh.profile.positions = new ArrayList<>();
			mesh.path.points = new ArrayList<>();

			OSDArray decodedMeshOsdArray = (OSDArray) facesOSD;

			for (int faceNr = 0; faceNr < decodedMeshOsdArray.size(); faceNr++) {
				OSD subMeshOsd = decodedMeshOsdArray.get(faceNr);

				// Decode each individual face
				if (subMeshOsd instanceof OSDMap) {
					OSDMap subMeshMap = (OSDMap) subMeshOsd;

					// As per http://wiki.secondlife.com/wiki/Mesh/Mesh_Asset_Format, some Mesh
					// Level
					// of Detail Blocks (maps) contain just a NoGeometry key to signal there is no
					// geometry for this submesh.
					if (subMeshMap.containsKey("NoGeometry") && (subMeshMap.get("NoGeometry").asBoolean()))
						continue;

					Face oface = mesh.new Face();
					oface.id = faceNr;
					oface.vertices = new ArrayList<>();
					oface.indices = new ArrayList<>();
					oface.textureFace = prim.textures.getFace(faceNr);

					Vector3 posMax;
					Vector3 posMin;

					// If PositionDomain is not specified, the default is from -0.5 to 0.5
					OSD osd = subMeshMap.get("PositionDomain");
					if (osd != null) {
						posMax = ((OSDMap) osd).get("Max").asVector3();
						posMin = ((OSDMap) osd).get("Min").asVector3();
					} else {
						posMax = new Vector3(0.5f, 0.5f, 0.5f);
						posMin = new Vector3(-0.5f, -0.5f, -0.5f);
					}

					// Vertex positions
					byte[] posBytes = subMeshMap.get("Position").asBinary();

					// Normals
					byte[] norBytes = null;
					if (subMeshMap.containsKey("Normal")) {
						norBytes = subMeshMap.get("Normal").asBinary();
					}

					// UV texture map
					Vector2 texPosMax = Vector2.ZERO;
					Vector2 texPosMin = Vector2.ZERO;
					byte[] texBytes = null;
					osd = subMeshMap.get("TexCoord0");
					if (osd != null) {
						texBytes = osd.asBinary();
						osd = subMeshMap.get("TexCoord0Domain");
						texPosMax = ((OSDMap) osd).get("Max").asVector2();
						texPosMin = ((OSDMap) osd).get("Min").asVector2();
					}

					// Extract the vertex position data
					// If present normals and texture coordinates too
					for (int i = 0; i < posBytes.length; i += 6) {
						Vertex vx = mesh.new Vertex();

						vx.position = new Vector3(Helpers.uint16ToFloatL(posBytes, i, posMin.x, posMax.x),
								Helpers.uint16ToFloatL(posBytes, i + 2, posMin.y, posMax.y),
								Helpers.uint16ToFloatL(posBytes, i + 4, posMin.z, posMax.z));

						if (norBytes != null && norBytes.length >= i + 6) {
							vx.normal = new Vector3(Helpers.uint16ToFloatL(norBytes, i, posMin.x, posMax.x),
									Helpers.uint16ToFloatL(norBytes, i + 2, posMin.y, posMax.y),
									Helpers.uint16ToFloatL(norBytes, i + 4, posMin.z, posMax.z));
						}

						int vertexIndexOffset = oface.vertices.size() * 4;

						if (texBytes != null && texBytes.length >= vertexIndexOffset + 4) {
							vx.texCoord = new Vector2(
									Helpers.uint16ToFloatL(texBytes, vertexIndexOffset, texPosMin.x, texPosMax.x),
									Helpers.uint16ToFloatL(texBytes, vertexIndexOffset + 2, texPosMin.y, texPosMax.y));
						}

						oface.vertices.add(vx);
					}

					byte[] triangleBytes = subMeshMap.get("TriangleList").asBinary();
					for (int i = 0; i < triangleBytes.length; i += 6) {
						oface.indices.add(Helpers.bytesToUInt16L(triangleBytes, i));
						oface.indices.add(Helpers.bytesToUInt16L(triangleBytes, i + 2));
						oface.indices.add(Helpers.bytesToUInt16L(triangleBytes, i + 4));
					}

					mesh.faces.add(oface);
				}
			}
			return mesh;
		} catch (Exception ex) {
			logger.warn("Failed to decode mesh asset: " + ex.getMessage());
		}
		return null;
	}
}
