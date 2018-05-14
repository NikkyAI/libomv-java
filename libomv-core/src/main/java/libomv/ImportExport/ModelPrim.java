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
package libomv.ImportExport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.rendering.Mesh.Vertex;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.Helpers;

public class ModelPrim
{
    public List<Vector3> Positions;
    public Vector3 BoundMin = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    public Vector3 BoundMax = new Vector3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
    public Vector3 Position;
    public Vector3 Scale;
    public Quaternion Rotation = Quaternion.Identity;
    public List<ModelFace> Faces = new ArrayList<ModelFace>();
    public String ID;
    public byte[] Asset;

    public void CreateAsset(UUID creator) throws IOException
    {
        OSDMap header = new OSDMap();
        header.put("version", OSD.FromInteger(1));
        header.put("creator", OSD.FromUUID(creator));
        header.put("date", OSD.FromDate(new Date()));

        header.put("rez_position", OSD.FromVector3(Position));
        header.put("rez_scale", OSD.FromVector3(Scale));

        OSDArray faces = new OSDArray();
        for (ModelFace face : Faces)
        {
            OSDMap faceMap = new OSDMap();

            // Find UV min/max
            Vector2 uvMin = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
            Vector2 uvMax = new Vector2(Float.MIN_VALUE, Float.MIN_VALUE);
            for (Vertex v : face.Vertices)
            {
                if (v.TexCoord.X < uvMin.X) uvMin.X = v.TexCoord.X;
                if (v.TexCoord.Y < uvMin.Y) uvMin.Y = v.TexCoord.Y;

                if (v.TexCoord.X > uvMax.X) uvMax.X = v.TexCoord.X;
                if (v.TexCoord.Y > uvMax.Y) uvMax.Y = v.TexCoord.Y;
            }
            OSDMap uvDomain = new OSDMap();
            uvDomain.put("Min",  OSD.FromVector2(uvMin));
            uvDomain.put("Max", OSD.FromVector2(uvMax));
            faceMap.put("TexCoord0Domain", uvDomain);


            OSDMap positionDomain = new OSDMap();
            positionDomain.put("Min", OSD.FromVector3(new Vector3(-0.5f, -0.5f, -0.5f)));
            positionDomain.put("Max", OSD.FromVector3(new Vector3(0.5f, 0.5f, 0.5f)));
            faceMap.put("PositionDomain", positionDomain);

            byte[] posBytes = new byte[face.Vertices.size() * 2 * 3];
            byte[] norBytes = new byte[face.Vertices.size() * 2 * 3];
            byte[] uvBytes = new byte[face.Vertices.size() * 2 * 2];

            int offp = 0, offn = 0, offu = 0;
            for (Vertex v : face.Vertices)
            {
                Helpers.UInt16ToBytesL(Helpers.FloatToUInt16(v.Position.X, -0.5f, 0.5f), posBytes, offp);
                offp += 2;
                Helpers.UInt16ToBytesL(Helpers.FloatToUInt16(v.Position.Y, -0.5f, 0.5f), posBytes, offp);
                offp += 2;
                Helpers.UInt16ToBytesL(Helpers.FloatToUInt16(v.Position.Z, -0.5f, 0.5f), posBytes, offp);
                offp += 2;

                Helpers.UInt16ToBytesL(Helpers.FloatToUInt16(v.Normal.X, -1f, 1f), norBytes, offn);
    	        offn += 2;
                Helpers.UInt16ToBytesL(Helpers.FloatToUInt16(v.Normal.Y, -1f, 1f), norBytes, offn);
                offn += 2;
                Helpers.UInt16ToBytesL(Helpers.FloatToUInt16(v.Normal.Z, -1f, 1f), norBytes, offn);
                offn += 2;

                Helpers.UInt16ToBytesL(Helpers.FloatToUInt16(v.TexCoord.X, uvMin.X, uvMax.X), uvBytes, offu);
                offu += 2;
                Helpers.UInt16ToBytesL(Helpers.FloatToUInt16(v.TexCoord.Y, uvMin.Y, uvMax.Y), uvBytes, offu);
                offu += 2;
            }

            faceMap.put("Position", OSD.FromBinary(posBytes));
            faceMap.put("Normal", OSD.FromBinary(norBytes));
            faceMap.put("TexCoord0", OSD.FromBinary(uvBytes));

            int offi = 0;
            byte[] indexBytes = new byte[face.Indices.size() * 2];
            for (int t : face.Indices)
            {
                Helpers.UInt16ToBytesL(t, indexBytes, offi);
    	        offi += 2;
            }
            faceMap.put("TriangleList", OSD.FromBinary(indexBytes));

            faces.add(faceMap);
        }

        byte[] physicStubBytes = Helpers.ZCompressOSD(Model.PhysicsStub());

        byte[] meshBytes = Helpers.ZCompressOSD(faces);
        int n = 0;

        OSDMap lodParms = new OSDMap();
        lodParms.put("offset", OSD.FromInteger(n));
        lodParms.put("size", OSD.FromInteger(meshBytes.length));
        header.put("high_lod", lodParms);
        n += meshBytes.length;

        lodParms = new OSDMap();
        lodParms.put("offset", OSD.FromInteger(n));
        lodParms.put("size", OSD.FromInteger(physicStubBytes.length));
        header.put("physics_convex", lodParms);
        n += physicStubBytes.length;

        byte[] headerBytes = OSDParser.serializeToBytes(header, OSD.OSDFormat.Binary, false);
        n += headerBytes.length;

        Asset = new byte[n];

        int offset = 0;
        System.arraycopy(headerBytes, 0, Asset, offset, headerBytes.length);
        offset += headerBytes.length;

        System.arraycopy(meshBytes, 0, Asset, offset, meshBytes.length);
        offset += meshBytes.length;

        System.arraycopy(physicStubBytes, 0, Asset, offset, physicStubBytes.length);
        offset += physicStubBytes.length;

    }
}
