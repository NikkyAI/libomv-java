/**
 * Copyright (c) 2008, openmetaverse.org
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
package libomv.rendering;

import java.util.ArrayList;
import java.util.List;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.assets.AssetMesh;
import libomv.primitives.Primitive;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;;

public class FacetedMesh extends Mesh
{
    /// List of primitive faces
    public List<Mesh.Face> Faces;

    /**
     * Decodes mesh asset into FacetedMesh
     *
     * @param prim Mesh primitive
     * @param meshAsset Asset retrieved from the asset server
     * @param LOD Level of detail
     * @param mesh Resulting decoded FacetedMesh
     * @returns True if mesh asset decoding was successful
     */
    public static FacetedMesh TryDecodeFromAsset(Primitive prim, AssetMesh meshAsset, DetailLevel LOD)
    {
        try
        {
            if (!meshAsset.Decode())
            {
                return null;
            }

            OSDMap meshData = meshAsset.MeshData;
            OSD facesOSD = null;

            switch (LOD)
            {
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

            if (facesOSD == null || !(facesOSD instanceof OSDArray))
            {
                return null;
            }

            FacetedMesh mesh = new FacetedMesh();
            mesh.Faces = new ArrayList<Face>();
            mesh.Prim = prim;
            mesh.Profile.Faces = new ArrayList<ProfileFace>();
            mesh.Profile.Positions = new ArrayList<Vector3>();
            mesh.Path.Points = new ArrayList<PathPoint>();

            OSDArray decodedMeshOsdArray = (OSDArray)facesOSD;

            for (int faceNr = 0; faceNr < decodedMeshOsdArray.size(); faceNr++)
            {
                OSD subMeshOsd = decodedMeshOsdArray.get(faceNr);

                // Decode each individual face
                if (subMeshOsd instanceof OSDMap)
                {
                    Face oface = mesh.new Face();
                    oface.ID = faceNr;
                    oface.Vertices = new ArrayList<Vertex>();
                    oface.Indices = new ArrayList<Integer>();
                    oface.TextureFace = prim.Textures.GetFace(faceNr);

                    OSDMap subMeshMap = (OSDMap)subMeshOsd;

                    Vector3 posMax;
                    Vector3 posMin;

                    // If PositionDomain is not specified, the default is from -0.5 to 0.5
                    if (subMeshMap.containsKey("PositionDomain"))
                    {
                        posMax = ((OSDMap)subMeshMap.get("PositionDomain")).get("Max").AsVector3();
                        posMin = ((OSDMap)subMeshMap.get("PositionDomain")).get("Min").AsVector3();
                    }
                    else
                    {
                        posMax = new Vector3(0.5f, 0.5f, 0.5f);
                        posMin = new Vector3(-0.5f, -0.5f, -0.5f);
                    }

                    // Vertex positions
                    byte[] posBytes = subMeshMap.get("Position").AsBinary();

                    // Normals
                    byte[] norBytes = null;
                    if (subMeshMap.containsKey("Normal"))
                    {
                        norBytes = subMeshMap.get("Normal").AsBinary();
                    }

                    // UV texture map
                    Vector2 texPosMax = Vector2.Zero;
                    Vector2 texPosMin = Vector2.Zero;
                    byte[] texBytes = null;
                    if (subMeshMap.containsKey("TexCoord0"))
                    {
                        texBytes = subMeshMap.get("TexCoord0").AsBinary();
                        texPosMax = ((OSDMap)subMeshMap.get("TexCoord0Domain")).get("Max").AsVector2();
                        texPosMin = ((OSDMap)subMeshMap.get("TexCoord0Domain")).get("Min").AsVector2();
                    }

                    // Extract the vertex position data
                    // If present normals and texture coordinates too
                    for (int i = 0; i < posBytes.length; i += 6)
                    {
                        Vertex vx = mesh.new Vertex();

                        vx.Position = new Vector3(
                        		Helpers.UInt16ToFloatL(posBytes, i, posMin.X, posMax.X),
                        		Helpers.UInt16ToFloatL(posBytes, i + 2, posMin.Y, posMax.Y),
                        		Helpers.UInt16ToFloatL(posBytes, i + 4, posMin.Z, posMax.Z));

                        if (norBytes != null && norBytes.length >= i + 4)
                        {
                            vx.Normal = new Vector3(
                            		Helpers.UInt16ToFloatL(norBytes, i, posMin.X, posMax.X),
                            		Helpers.UInt16ToFloatL(norBytes, i + 2, posMin.Y, posMax.Y),
                            		Helpers.UInt16ToFloatL(norBytes, i + 4, posMin.Z, posMax.Z));
                        }

                        int vertexIndexOffset = oface.Vertices.size() * 4;

                        if (texBytes != null && texBytes.length >= vertexIndexOffset + 4)
                        {
                            vx.TexCoord = new Vector2(
                            		Helpers.UInt16ToFloatL(texBytes, vertexIndexOffset, texPosMin.X, texPosMax.X),
                            		Helpers.UInt16ToFloatL(texBytes, vertexIndexOffset + 2, texPosMin.Y, texPosMax.Y));
                        }

                        oface.Vertices.add(vx);
                    }

                    byte[] triangleBytes = subMeshMap.get("TriangleList").AsBinary();
                    for (int i = 0; i < triangleBytes.length; i += 6)
                    {
                        oface.Indices.add(Helpers.BytesToUInt16L(triangleBytes, i));
                        oface.Indices.add(Helpers.BytesToUInt16L(triangleBytes, i + 2));
                        oface.Indices.add(Helpers.BytesToUInt16L(triangleBytes, i + 4));
                    }

                    mesh.Faces.add(oface);
                }
            }
            return mesh;
        }
        catch (Exception ex)
        {
            Logger.Log("Failed to decode mesh asset: " + ex.getMessage(), LogLevel.Warning);
        }
        return null;
    }
}
