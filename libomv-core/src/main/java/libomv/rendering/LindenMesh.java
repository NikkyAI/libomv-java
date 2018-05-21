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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.input.SwappedDataInputStream;

import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.Helpers;

public class LindenMesh extends ReferenceMesh {

	// #region Mesh Structs

	public class Vertex {
		public Vector3 position;
		public Vector3 normal;
		public Vector3 biNormal;
		public Vector2 texCoord;
		public Vector2 detailTexCoord;
		public float weight;

		@Override
		public String toString() {
			return String.format("Position: %s Norm: %s BiNorm: %s TexCoord: %s DetailTexCoord: %s", position, normal,
					biNormal, texCoord, detailTexCoord, weight);
		}
	}

	public class MorphVertex {
		public int vertexIndex;
		public Vector3 position;
		public Vector3 normal;
		public Vector3 biNormal;
		public Vector2 texCoord;

		@Override
		public String toString() {
			return String.format("Index: %d Position: %s Norm: %s BiNorm: %s TexCoord: %s", vertexIndex, position,
					normal, biNormal, texCoord);
		}
	}

	public class Morph {
		public String name;
		public int numVertices;
		public MorphVertex[] vertices;

		@Override
		public String toString() {
			return name;
		}
	}

	public class VertexRemap {
		public int remapSource;
		public int remapDestination;

		@Override
		public String toString() {
			return String.format("%d -> %d", remapSource, remapDestination);
		}
	}
	// #endregion Mesh Structs

	public FloatBuffer vertices;
	public FloatBuffer normals;
	public FloatBuffer biNormals;
	public FloatBuffer texCoords;
	public FloatBuffer detailTexCoords;
	public FloatBuffer weights;
	protected String name;
	protected short numVertices;
	protected Vector3 center;
	private LindenSkeleton skeleton;

	// private GridClient _client;
	protected short numSkinJoints;
	protected String[] skinJoints;
	protected Morph[] morphs;
	protected int numRemaps;
	protected VertexRemap[] vertexRemaps;
	protected Map<Integer, ReferenceMesh> meshes;

	public LindenMesh(String name) throws Exception {
		// this(null, name, null);
		this(name, null);
	}

	// public LindenMesh(GridClient client, String name) throws Exception
	// {
	// this(client, name, null);
	// }

	// public LindenMesh(GridClient client, String name, LindenSkeleton skeleton)
	// throws Exception
	public LindenMesh(String name, LindenSkeleton skeleton) throws Exception {
		// this._client = client;
		this.name = name;
		this.skeleton = skeleton;
		this.meshes = new TreeMap<>();

		if (this.skeleton == null) {
			this.skeleton = LindenSkeleton.load(null);
		}
	}

	public String getName() {
		return name;
	}

	public LindenSkeleton getSkeleton() {
		return skeleton;
	}

	public Vector3 getVerticeCoord(int index) {
		if (index >= numVertices)
			return null;
		index *= 3;
		return new Vector3(vertices.get(index), vertices.get(index + 1), vertices.get(index + 2));
	}

	public Vector3 getCenter() {
		return center;
	}

	public Vertex getVertex(int index) {
		if (index >= numVertices)
			return null;

		Vertex vertex = new Vertex();
		int offset = index * 3;
		vertex.position = new Vector3(vertices.get(offset), vertices.get(offset + 1), vertices.get(offset + 2));
		vertex.normal = new Vector3(normals.get(offset), normals.get(offset + 1), normals.get(offset + 2));
		vertex.biNormal = new Vector3(biNormals.get(offset), biNormals.get(offset + 1), biNormals.get(offset + 2));
		offset = index * 2;
		vertex.texCoord = new Vector2(texCoords.get(offset), texCoords.get(offset + 1));
		vertex.detailTexCoord = new Vector2(detailTexCoords.get(offset), detailTexCoords.get(offset + 1));

		vertex.weight = weights.get(index);
		return vertex;
	}

	public short getNumSkinJoints() {
		return numSkinJoints;
	}

	public String[] getSkinJoints() {
		return skinJoints;
	}

	public Morph[] getMorphs() {
		return morphs;
	}

	public int getNumRemaps() {
		return numRemaps;
	}

	public VertexRemap[] getVertexRemaps() {
		return vertexRemaps;
	}

	public Map<Integer, ReferenceMesh> getMeshes() {
		return meshes;
	}

	/**
	 * Load the mesh from a file
	 *
	 * @param "filename"
	 *            The filename and path of the file containing the mesh data
	 */
	public void load(String filename) throws IOException {
		FileInputStream fis = new FileInputStream(filename);
		try {
			load(fis);
		} catch (IOException ex) {
			System.out.append(filename + "\r\n");
		} finally {
			fis.close();
		}
	}

	public void load(InputStream stream) throws IOException {
		SwappedDataInputStream fis = new SwappedDataInputStream(stream);
		super.load(fis);

		numVertices = fis.readShort();

		float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE, val, maxX = Float.MIN_VALUE,
				maxY = Float.MIN_VALUE, maxZ = Float.MIN_VALUE;
		// Populate the vertex array
		vertices = FloatBuffer.allocate(3 * numVertices);
		for (int i = 0; i < numVertices; i++) {
			val = fis.readFloat();
			if (val < minX)
				minX = val;
			else if (val > maxX)
				maxX = val;
			vertices.put(val);

			val = fis.readFloat();
			if (val < minY)
				minY = val;
			else if (val > maxY)
				maxY = val;
			vertices.put(val);

			val = fis.readFloat();
			if (val < minZ)
				minX = val;
			else if (val > maxZ)
				maxZ = val;
			vertices.put(val);
		}

		// Store the Center vector
		center = new Vector3((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);

		normals = FloatBuffer.allocate(3 * numVertices);
		for (int i = 0; i < numVertices; i++) {
			normals.put(fis.readFloat());
			normals.put(fis.readFloat());
			normals.put(fis.readFloat());
		}

		biNormals = FloatBuffer.allocate(3 * numVertices);
		for (int i = 0; i < numVertices; i++) {
			biNormals.put(fis.readFloat());
			biNormals.put(fis.readFloat());
			biNormals.put(fis.readFloat());
		}

		texCoords = FloatBuffer.allocate(2 * numVertices);
		for (int i = 0; i < numVertices; i++) {
			texCoords.put(fis.readFloat());
			texCoords.put(fis.readFloat());
		}

		if (hasDetailTexCoords) {
			detailTexCoords = FloatBuffer.allocate(2 * numVertices);
			for (int i = 0; i < numVertices; i++) {
				detailTexCoords.put(fis.readFloat());
				detailTexCoords.put(fis.readFloat());
			}
		}

		if (hasWeights) {
			weights = FloatBuffer.allocate(numVertices);
			for (int i = 0; i < numVertices; i++) {
				weights.put(fis.readFloat());
			}
		}

		numFaces = fis.readShort();

		indices = ShortBuffer.allocate(3 * numFaces);
		for (int i = 0; i < numFaces; i++) {
			indices.put(fis.readShort());
			indices.put(fis.readShort());
			indices.put(fis.readShort());
		}

		if (hasWeights) {
			numSkinJoints = fis.readShort();
			skinJoints = new String[numSkinJoints];

			for (int i = 0; i < numSkinJoints; i++) {
				skinJoints[i] = Helpers.readString(fis, 64);
			}
		} else {
			numSkinJoints = 0;
			skinJoints = new String[0];
		}

		// Grab morphs
		List<Morph> morphsList = new ArrayList<>();
		String morphName = Helpers.readString(fis, 64);

		while (!morphName.equals(MORPH_FOOTER)) {
			// TODO:FIXME
			// There is a missing if statement in the original source code
			// which needs to be translated

			// if (reader.BaseStream.Position + 48 >= reader.BaseStream.Length)
			// throw new FileLoadException("Encountered end of file while parsing morphs");

			Morph morph = new Morph();
			morph.name = morphName;
			morph.numVertices = fis.readInt();
			morph.vertices = new MorphVertex[morph.numVertices];

			for (int i = 0; i < morph.numVertices; i++) {
				morph.vertices[i] = new MorphVertex();
				morph.vertices[i].vertexIndex = fis.readInt();
				morph.vertices[i].position = new Vector3(fis);
				morph.vertices[i].normal = new Vector3(fis);
				morph.vertices[i].biNormal = new Vector3(fis);
				morph.vertices[i].texCoord = new Vector2(fis);
			}

			morphsList.add(morph);

			// Grab the next name
			morphName = Helpers.readString(fis, 64);
		}

		morphs = morphsList.toArray(morphs);

		// Check if there are remaps or if we're at the end of the file
		try {
			numRemaps = fis.readInt();
			vertexRemaps = new VertexRemap[numRemaps];

			for (int i = 0; i < numRemaps; i++) {
				vertexRemaps[i].remapSource = fis.readInt();
				vertexRemaps[i].remapDestination = fis.readInt();
			}
		} catch (IOException ex) {
			numRemaps = 0;
			vertexRemaps = new VertexRemap[0];
		}

		// uncompress the skin weights
		if (skeleton != null) {
			// some meshes aren't weighted, which doesn't make much sense. We check for
			// left and right eyeballs, and assign them a 100% to their respective bone.
			List<String> expandedJointList = skeleton.buildExpandedJointList(skinJoints);
			if (expandedJointList.size() == 0) {
				if (name.equals("eyeBallLeftMesh")) {
					expandedJointList.add("mEyeLeft");
					expandedJointList.add("mSkull");
				} else if (name.equals("eyeBallRightMesh")) {
					expandedJointList.add("mEyeRight");
					expandedJointList.add("mSkull");
				}
			}

			if (expandedJointList.size() > 0)
				expandCompressedSkinWeights(expandedJointList);
		}
	}

	public ReferenceMesh loadLod(int level, String filename) throws Exception {
		ReferenceMesh mesh;
		if (filename.equalsIgnoreCase("avatar_eye_1.llm")) {
			mesh = new ReferenceMesh();
		} else {
			mesh = new LindenMesh("");
		}
		mesh.load(filename);
		meshes.put(level, mesh);
		return mesh;
	}

	// region Skin weight

	/**
	 * Layout of one skinweight element
	 */
	public class SkinWeightElement {
		public String bone1; // Name of the first bone that influences the vertex
		public String bone2; // Name of the second bone that influences the vertex
		public float weight1; // Weight with whitch the first bone influences the vertex
		public float weight2; // Weight with whitch the second bone influences the vertex
	}

	// List of skinweights, in the same order as the mesh vertices
	public List<SkinWeightElement> skinWeights = new ArrayList<>();

	/**
	 * Decompress the skinweights
	 *
	 * @param expandedJointList
	 *            The expanded joint list, used to index which bones should influece
	 *            the vertex
	 */
	private void expandCompressedSkinWeights(List<String> expandedJointList) {
		for (int i = 0; i < numVertices; i++) {
			int boneIndex = (int) Math.floor(weights.get(i)); // Whole number part is the index
			float boneWeight = (weights.get(i) - boneIndex); // fractional part is the weight
			SkinWeightElement elm = new SkinWeightElement();

			if (boneIndex == 0) // Special case for dealing with eye meshes, which doesn't have any weights
			{
				elm.bone1 = expandedJointList.get(0);
				elm.weight1 = 1;
				elm.bone2 = expandedJointList.get(1);
				elm.weight2 = 0;
			} else if (boneIndex < expandedJointList.size()) {
				elm.bone1 = expandedJointList.get(boneIndex - 1);
				elm.weight1 = 1 - boneWeight;
				elm.bone2 = expandedJointList.get(boneIndex);
				elm.weight2 = boneWeight;
			} else { // this should add a weight where the "invalid" Joint has a weight of zero
				elm.bone1 = expandedJointList.get(boneIndex - 1);
				elm.weight1 = 1 - boneWeight;
				elm.bone2 = "mPelvis";
				elm.weight2 = boneWeight;
			}
			skinWeights.add(elm);
		}
	}
	// #endregion Skin weight
}