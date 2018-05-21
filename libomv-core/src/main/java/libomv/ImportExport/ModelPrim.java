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

public class ModelPrim {
	public List<Vector3> positions;
	public Vector3 boundMin = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	public Vector3 boundMax = new Vector3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
	public Vector3 position;
	public Vector3 scale;
	public Quaternion rotation = Quaternion.IDENTITY;
	public List<ModelFace> faces = new ArrayList<>();
	public String id;
	public byte[] asset;

	public void createAsset(UUID creator) throws IOException {
		OSDMap header = new OSDMap();
		header.put("version", OSD.fromInteger(1));
		header.put("creator", OSD.fromUUID(creator));
		header.put("date", OSD.fromDate(new Date()));

		header.put("rez_position", OSD.fromVector3(position));
		header.put("rez_scale", OSD.fromVector3(scale));

		OSDArray facesOSD = new OSDArray();
		for (ModelFace face : faces) {
			OSDMap faceMap = new OSDMap();

			// Find UV min/max
			Vector2 uvMin = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
			Vector2 uvMax = new Vector2(Float.MIN_VALUE, Float.MIN_VALUE);
			for (Vertex v : face.vertices) {
				if (v.texCoord.x < uvMin.x)
					uvMin.x = v.texCoord.x;
				if (v.texCoord.y < uvMin.y)
					uvMin.y = v.texCoord.y;

				if (v.texCoord.x > uvMax.x)
					uvMax.x = v.texCoord.x;
				if (v.texCoord.y > uvMax.y)
					uvMax.y = v.texCoord.y;
			}
			OSDMap uvDomain = new OSDMap();
			uvDomain.put("Min", OSD.fromVector2(uvMin));
			uvDomain.put("Max", OSD.fromVector2(uvMax));
			faceMap.put("TexCoord0Domain", uvDomain);

			OSDMap positionDomain = new OSDMap();
			positionDomain.put("Min", OSD.fromVector3(new Vector3(-0.5f, -0.5f, -0.5f)));
			positionDomain.put("Max", OSD.fromVector3(new Vector3(0.5f, 0.5f, 0.5f)));
			faceMap.put("PositionDomain", positionDomain);

			byte[] posBytes = new byte[face.vertices.size() * 2 * 3];
			byte[] norBytes = new byte[face.vertices.size() * 2 * 3];
			byte[] uvBytes = new byte[face.vertices.size() * 2 * 2];

			int offp = 0;
			int offn = 0;
			int offu = 0;
			for (Vertex v : face.vertices) {
				Helpers.uint16ToBytesL(Helpers.floatToUInt16(v.position.x, -0.5f, 0.5f), posBytes, offp);
				offp += 2;
				Helpers.uint16ToBytesL(Helpers.floatToUInt16(v.position.y, -0.5f, 0.5f), posBytes, offp);
				offp += 2;
				Helpers.uint16ToBytesL(Helpers.floatToUInt16(v.position.z, -0.5f, 0.5f), posBytes, offp);
				offp += 2;

				Helpers.uint16ToBytesL(Helpers.floatToUInt16(v.normal.x, -1f, 1f), norBytes, offn);
				offn += 2;
				Helpers.uint16ToBytesL(Helpers.floatToUInt16(v.normal.y, -1f, 1f), norBytes, offn);
				offn += 2;
				Helpers.uint16ToBytesL(Helpers.floatToUInt16(v.normal.z, -1f, 1f), norBytes, offn);
				offn += 2;

				Helpers.uint16ToBytesL(Helpers.floatToUInt16(v.texCoord.x, uvMin.x, uvMax.x), uvBytes, offu);
				offu += 2;
				Helpers.uint16ToBytesL(Helpers.floatToUInt16(v.texCoord.y, uvMin.y, uvMax.y), uvBytes, offu);
				offu += 2;
			}

			faceMap.put("Position", OSD.fromBinary(posBytes));
			faceMap.put("Normal", OSD.fromBinary(norBytes));
			faceMap.put("TexCoord0", OSD.fromBinary(uvBytes));

			int offi = 0;
			byte[] indexBytes = new byte[face.indices.size() * 2];
			for (int t : face.indices) {
				Helpers.uint16ToBytesL(t, indexBytes, offi);
				offi += 2;
			}
			faceMap.put("TriangleList", OSD.fromBinary(indexBytes));

			facesOSD.add(faceMap);
		}

		byte[] physicStubBytes = Helpers.zCompressOSD(Model.physicsStub());

		byte[] meshBytes = Helpers.zCompressOSD(facesOSD);
		int n = 0;

		OSDMap lodParms = new OSDMap();
		lodParms.put("offset", OSD.fromInteger(n));
		lodParms.put("size", OSD.fromInteger(meshBytes.length));
		header.put("high_lod", lodParms);
		n += meshBytes.length;

		lodParms = new OSDMap();
		lodParms.put("offset", OSD.fromInteger(n));
		lodParms.put("size", OSD.fromInteger(physicStubBytes.length));
		header.put("physics_convex", lodParms);
		n += physicStubBytes.length;

		byte[] headerBytes = OSDParser.serializeToBytes(header, OSD.OSDFormat.Binary, false);
		n += headerBytes.length;

		asset = new byte[n];

		int offset = 0;
		System.arraycopy(headerBytes, 0, asset, offset, headerBytes.length);
		offset += headerBytes.length;

		System.arraycopy(meshBytes, 0, asset, offset, meshBytes.length);
		offset += meshBytes.length;

		System.arraycopy(physicStubBytes, 0, asset, offset, physicStubBytes.length);
		offset += physicStubBytes.length;

	}
}
