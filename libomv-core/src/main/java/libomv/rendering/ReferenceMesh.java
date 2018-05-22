/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2012-2017, Frederick Martian
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ShortBuffer;

import libomv.types.Vector3;
import libomv.utils.Helpers;

import org.apache.commons.io.input.SwappedDataInputStream;

/**
 * A reference mesh is one way to implement level of detail
 *
 * @remarks Reference meshes are supplemental meshes to full meshes. For all
 *          practical purposes almost all lod meshes are implemented as
 *          reference meshes, except for 'avatar_eye_1.llm' which for some
 *          reason is implemented as a full mesh.
 */
public class ReferenceMesh {
	protected static final String MESH_HEADER = "Linden Binary Mesh 1.0";
	protected static final String MORPH_FOOTER = "End Morphs";

	public class Face {
		public short indices1;
		public short indices2;
		public short indices3;

		public Face(ShortBuffer indices, int idx) {
			indices1 = indices.get(idx++);
			indices2 = indices.get(idx++);
			indices3 = indices.get(idx++);
		}
	}

	public float minPixelWidth;

	protected String header;
	protected boolean hasWeights;
	protected boolean hasDetailTexCoords;
	protected Vector3 position;
	protected Vector3 rotationAngles;
	protected byte rotationOrder;
	protected Vector3 scale;
	protected short numFaces;
	public ShortBuffer indices;

	public String getHeader() {
		return header;
	}

	public boolean getHasWeights() {
		return hasWeights;
	}

	public boolean getHasDetailTexCoords() {
		return hasDetailTexCoords;
	}

	public Vector3 getPosition() {
		return position;
	}

	public Vector3 getRotationAngles() {
		return rotationAngles;
	}

	public byte getRotationOrder() {
		return rotationOrder;
	}

	public Vector3 getScale() {
		return scale;
	}

	public short getNumFaces() {
		return numFaces;
	}

	public Face getFace(int index) {
		if (index >= numFaces)
			return null;
		return new Face(indices, index * 3);
	}

	public void load(String filename) throws IOException {
		InputStream stream = new FileInputStream(filename);
		try {
			load(stream);
		} finally {
			stream.close();
		}
	}

	public void load(InputStream stream) throws IOException {
		SwappedDataInputStream fis = new SwappedDataInputStream(stream);

		load(fis);

		numFaces = fis.readShort();
		indices = ShortBuffer.allocate(3 * numFaces);
		for (int i = 0; i < numFaces; i++) {
			indices.put(fis.readShort());
			indices.put(fis.readShort());
			indices.put(fis.readShort());
		}
	}

	protected void load(SwappedDataInputStream fis) throws IOException {
		header = Helpers.readString(fis, 24);
		if (!header.equals(MESH_HEADER))
			throw new IOException("Unrecognized mesh format");

		// Populate base mesh variables
		hasWeights = fis.readByte() != 1;
		hasDetailTexCoords = fis.readByte() != 1;
		position = new Vector3(fis);
		rotationAngles = new Vector3(fis);
		rotationOrder = fis.readByte();
		scale = new Vector3(fis);
	}
}
