/**
 * Copyright (c) 2010-2012, Dahlia Trimble
 * Copyright (c) 2011-2017, Frederick Martian
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
package libomv.primMesher.types;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import libomv.types.Quaternion;
import libomv.types.Vector2;
import libomv.types.Vector3;

// generates a profile for extrusion
public class Profile {
	private final float twoPi = 2.0f * (float) Math.PI;

	public String errorMessage = null;

	public List<Vector3> coords;
	public List<Face> faces;
	public List<Vector3> vertexNormals;
	public List<Float> us;
	public List<Vector2> faceUVs;
	public List<Integer> faceNumbers;

	// use these for making individual meshes for each prim face
	public List<Integer> outerCoordIndices = null;
	public List<Integer> hollowCoordIndices = null;
	public List<Integer> cut1CoordIndices = null;
	public List<Integer> cut2CoordIndices = null;

	public Vector3 faceNormal = new Vector3(0.0f, 0.0f, 1.0f);
	public Vector3 cutNormal1 = new Vector3(0f);
	public Vector3 cutNormal2 = new Vector3(0f);

	public int numOuterVerts = 0;
	public int numHollowVerts = 0;

	public int outerFaceNumber = -1;
	public int hollowFaceNumber = -1;

	public boolean calcVertexNormals = false;
	public int bottomFaceNumber = 0;
	public int numPrimFaces = 0;

	public Profile() {
		this.coords = new ArrayList<>();
		this.faces = new ArrayList<>();
		this.vertexNormals = new ArrayList<>();
		this.us = new ArrayList<>();
		this.faceUVs = new ArrayList<>();
		this.faceNumbers = new ArrayList<>();
	}

	public Profile(int sides, float profileStart, float profileEnd, float hollow, int hollowSides, boolean createFaces,
			boolean calcVertexNormals) {
		this.calcVertexNormals = calcVertexNormals;
		this.coords = new ArrayList<>();
		this.faces = new ArrayList<>();
		this.vertexNormals = new ArrayList<>();
		this.us = new ArrayList<>();
		this.faceUVs = new ArrayList<>();
		this.faceNumbers = new ArrayList<>();

		Vector3 center = new Vector3(0.0f, 0.0f, 0.0f);

		List<Vector3> hollowCoords = new ArrayList<>();
		List<Vector3> hollowNormals = new ArrayList<>();
		List<Float> hollowUs = new ArrayList<>();

		if (calcVertexNormals) {
			this.outerCoordIndices = new ArrayList<>();
			this.hollowCoordIndices = new ArrayList<>();
			this.cut1CoordIndices = new ArrayList<>();
			this.cut2CoordIndices = new ArrayList<>();
		}

		boolean hasHollow = (hollow > 0.0f);

		boolean hasProfileCut = (profileStart > 0.0f || profileEnd < 1.0f);

		AngleList angles = new AngleList();
		AngleList hollowAngles = new AngleList();

		float xScale = 0.5f;
		float yScale = 0.5f;
		if (sides == 4) // corners of a square are sqrt(2) from center
		{
			xScale = 0.707107f;
			yScale = 0.707107f;
		}

		float startAngle = profileStart * twoPi;
		float stopAngle = profileEnd * twoPi;

		try {
			angles.makeAngles(sides, startAngle, stopAngle);
		} catch (Exception ex) {
			errorMessage = String.format("makeAngles failed: Exception: %s\nsides: %f startAngle: %f stopAngle: %f",
					ex.toString(), sides, startAngle, stopAngle);
			return;
		}

		this.numOuterVerts = angles.angles.size();

		// flag to create as few triangles as possible for 3 or 4 side profile
		boolean simpleFace = (sides < 5 && !hasHollow && !hasProfileCut);

		if (hasHollow) {
			if (sides == hollowSides)
				hollowAngles = angles;
			else {
				try {
					hollowAngles.makeAngles(hollowSides, startAngle, stopAngle);
				} catch (Exception ex) {
					errorMessage = String.format(
							"makeAngles failed: Exception: %s\nsides: %f startAngle: %f stopAngle: %f", ex.toString(),
							sides, startAngle, stopAngle);
					return;
				}
			}
			this.numHollowVerts = hollowAngles.angles.size();
		} else if (!simpleFace) {
			this.coords.add(center);
			if (this.calcVertexNormals)
				this.vertexNormals.add(new Vector3(0.0f, 0.0f, 1.0f));
			this.us.add(0.0f);
		}

		float z = 0.0f;

		Angle angle;
		if (hasHollow && hollowSides != sides) {
			int numHollowAngles = hollowAngles.angles.size();
			for (int i = 0; i < numHollowAngles; i++) {
				angle = hollowAngles.angles.get(i);
				hollowCoords.add(new Vector3(hollow * xScale * angle.x, hollow * yScale * angle.y, z));
				if (this.calcVertexNormals) {
					if (hollowSides < 5)
						hollowNormals.add(Vector3.negate(hollowAngles.normals.get(i)));
					else
						hollowNormals.add(new Vector3(-angle.x, -angle.y, 0.0f));

					if (hollowSides == 4)
						hollowUs.add(angle.angle * hollow * 0.707107f);
					else
						hollowUs.add(angle.angle * hollow);
				}
			}
		}

		int index = 0;
		int numAngles = angles.angles.size();
		Vector3 newVert = new Vector3(0f);
		for (int i = 0; i < numAngles; i++) {
			angle = angles.angles.get(i);
			newVert.x = angle.x * xScale;
			newVert.y = angle.y * yScale;
			newVert.z = z;
			this.coords.add(new Vector3(newVert));
			if (this.calcVertexNormals) {
				this.outerCoordIndices.add(this.coords.size() - 1);

				if (sides < 5) {
					this.vertexNormals.add(angles.normals.get(i));
					float u = angle.angle;
					this.us.add(u);
				} else {
					this.vertexNormals.add(new Vector3(angle.x, angle.y, 0.0f));
					this.us.add(angle.angle);
				}
			}

			if (hasHollow) {
				if (hollowSides == sides) {
					newVert.x *= hollow;
					newVert.y *= hollow;
					newVert.z = z;
					hollowCoords.add(new Vector3(newVert));
					if (this.calcVertexNormals) {
						if (sides < 5) {
							hollowNormals.add(angles.normals.get(i).negate());
						}

						else
							hollowNormals.add(new Vector3(-angle.x, -angle.y, 0.0f));

						hollowUs.add(angle.angle * hollow);
					}
				}
			} else if (!simpleFace && createFaces && angle.angle > 0.0001f) {
				Face newFace = new Face();
				newFace.v1 = 0;
				newFace.v2 = index;
				newFace.v3 = index + 1;

				this.faces.add(newFace);
			}
			index += 1;
		}

		if (hasHollow) {
			Collections.reverse(hollowCoords);
			if (this.calcVertexNormals) {
				Collections.reverse(hollowNormals);
				Collections.reverse(hollowUs);
			}

			if (createFaces) {
				int numTotalVerts = this.numOuterVerts + this.numHollowVerts;
				Face newFace = new Face();

				if (this.numOuterVerts == this.numHollowVerts) {

					for (int coordIndex = 0; coordIndex < this.numOuterVerts - 1; coordIndex++) {
						newFace.v1 = coordIndex;
						newFace.v2 = coordIndex + 1;
						newFace.v3 = numTotalVerts - coordIndex - 1;
						this.faces.add(new Face(newFace));

						newFace.v1 = coordIndex + 1;
						newFace.v2 = numTotalVerts - coordIndex - 2;
						newFace.v3 = numTotalVerts - coordIndex - 1;
						this.faces.add(new Face(newFace));
					}
				} else {
					if (this.numOuterVerts < this.numHollowVerts) {
						int j = 0; // j is the index for outer vertices
						int maxJ = this.numOuterVerts - 1;
						for (int i = 0; i < this.numHollowVerts; i++) // i is the index for inner vertices
						{
							if (j < maxJ)
								if (angles.angles.get(j + 1).angle
										- hollowAngles.angles.get(i).angle < hollowAngles.angles.get(i).angle
												- angles.angles.get(j).angle + 0.000001f) {
									newFace.v1 = numTotalVerts - i - 1;
									newFace.v2 = j;
									newFace.v3 = j + 1;

									this.faces.add(new Face(newFace));
									j += 1;
								}

							newFace.v1 = j;
							newFace.v2 = numTotalVerts - i - 2;
							newFace.v3 = numTotalVerts - i - 1;

							this.faces.add(new Face(newFace));
						}
					} else // numHollowVerts < numOuterVerts
					{
						int j = 0; // j is the index for inner vertices
						int maxJ = this.numHollowVerts - 1;
						for (int i = 0; i < this.numOuterVerts; i++) {
							if (j < maxJ)
								if (hollowAngles.angles.get(j + 1).angle
										- angles.angles.get(i).angle < angles.angles.get(i).angle
												- hollowAngles.angles.get(j).angle + 0.000001f) {
									newFace.v1 = i;
									newFace.v2 = numTotalVerts - j - 2;
									newFace.v3 = numTotalVerts - j - 1;

									this.faces.add(new Face(newFace));
									j += 1;
								}

							newFace.v1 = numTotalVerts - j - 1;
							newFace.v2 = i;
							newFace.v3 = i + 1;

							this.faces.add(new Face(newFace));
						}
					}
				}
			}

			if (calcVertexNormals) {
				for (Vector3 hc : hollowCoords) {
					this.coords.add(hc);
					hollowCoordIndices.add(this.coords.size() - 1);
				}
			} else
				this.coords.addAll(hollowCoords);

			if (this.calcVertexNormals) {
				this.vertexNormals.addAll(hollowNormals);
				this.us.addAll(hollowUs);

			}
		}

		if (simpleFace && createFaces) {
			if (sides == 3)
				this.faces.add(new Face(0, 1, 2));
			else if (sides == 4) {
				this.faces.add(new Face(0, 1, 2));
				this.faces.add(new Face(0, 2, 3));
			}
		}

		if (calcVertexNormals && hasProfileCut) {
			int lastOuterVertIndex = this.numOuterVerts - 1;

			if (hasHollow) {
				this.cut1CoordIndices.add(0);
				this.cut1CoordIndices.add(this.coords.size() - 1);

				this.cut2CoordIndices.add(lastOuterVertIndex + 1);
				this.cut2CoordIndices.add(lastOuterVertIndex);

				this.cutNormal1.x = this.coords.get(0).y - this.coords.get(this.coords.size() - 1).y;
				this.cutNormal1.y = -(this.coords.get(0).x - this.coords.get(this.coords.size() - 1).x);

				this.cutNormal2.x = this.coords.get(lastOuterVertIndex + 1).y - this.coords.get(lastOuterVertIndex).y;
				this.cutNormal2.y = -(this.coords.get(lastOuterVertIndex + 1).x
						- this.coords.get(lastOuterVertIndex).x);
			}

			else {
				this.cut1CoordIndices.add(0);
				this.cut1CoordIndices.add(1);

				this.cut2CoordIndices.add(lastOuterVertIndex);
				this.cut2CoordIndices.add(0);

				this.cutNormal1.x = this.vertexNormals.get(1).y;
				this.cutNormal1.y = -this.vertexNormals.get(1).x;

				this.cutNormal2.x = -this.vertexNormals.get(this.vertexNormals.size() - 2).y;
				this.cutNormal2.y = this.vertexNormals.get(this.vertexNormals.size() - 2).x;

			}
			this.cutNormal1.normalize();
			this.cutNormal2.normalize();
		}

		makeFaceUVs();

		hollowCoords = null;
		hollowNormals = null;
		hollowUs = null;

		if (calcVertexNormals) { // calculate prim face numbers

			// face number order is top, outer, hollow, bottom, start cut, end cut
			// I know it's ugly but so is the whole concept of prim face numbers

			int faceNum = 1; // start with outer faces
			this.outerFaceNumber = faceNum;

			int startVert = hasProfileCut && !hasHollow ? 1 : 0;
			if (startVert > 0)
				this.faceNumbers.add(-1);
			for (int i = 0; i < this.numOuterVerts - 1; i++)
				this.faceNumbers.add(sides < 5 && i <= sides ? faceNum++ : faceNum);

			this.faceNumbers.add(hasProfileCut ? -1 : faceNum++);

			if (sides > 4 && (hasHollow || hasProfileCut))
				faceNum++;

			if (sides < 5 && (hasHollow || hasProfileCut) && this.numOuterVerts < sides)
				faceNum++;

			if (hasHollow) {
				for (int i = 0; i < this.numHollowVerts; i++)
					this.faceNumbers.add(faceNum);

				this.hollowFaceNumber = faceNum++;
			}

			this.bottomFaceNumber = faceNum++;

			if (hasHollow && hasProfileCut)
				this.faceNumbers.add(faceNum++);

			for (int i = 0; i < this.faceNumbers.size(); i++)
				if (this.faceNumbers.get(i) == -1)
					this.faceNumbers.set(i, faceNum++);

			this.numPrimFaces = faceNum;
		}

	}

	public void makeFaceUVs() {
		this.faceUVs = new ArrayList<Vector2>();
		for (Vector3 c : this.coords)
			this.faceUVs.add(new Vector2(1.0f - (0.5f + c.x), 1.0f - (0.5f - c.y)));
	}

	public Profile copy() {
		return this.copy(true);
	}

	public Profile copy(boolean needFaces) {
		Profile copy = new Profile();

		copy.coords.addAll(this.coords);
		copy.faceUVs.addAll(this.faceUVs);

		if (needFaces)
			copy.faces.addAll(this.faces);
		if ((copy.calcVertexNormals = this.calcVertexNormals) == true) {
			copy.vertexNormals.addAll(this.vertexNormals);
			copy.faceNormal = this.faceNormal;
			copy.cutNormal1 = this.cutNormal1;
			copy.cutNormal2 = this.cutNormal2;
			copy.us.addAll(this.us);
			copy.faceNumbers.addAll(this.faceNumbers);

			copy.cut1CoordIndices = new ArrayList<Integer>(this.cut1CoordIndices);
			copy.cut2CoordIndices = new ArrayList<Integer>(this.cut2CoordIndices);
			copy.hollowCoordIndices = new ArrayList<Integer>(this.hollowCoordIndices);
			copy.outerCoordIndices = new ArrayList<Integer>(this.outerCoordIndices);
		}
		copy.numOuterVerts = this.numOuterVerts;
		copy.numHollowVerts = this.numHollowVerts;

		return copy;
	}

	public void addPos(Vector3 v) {
		this.addPos(v.x, v.y, v.z);
	}

	public void addPos(float x, float y, float z) {
		int i;
		int numVerts = this.coords.size();
		Vector3 vert;

		for (i = 0; i < numVerts; i++) {
			vert = this.coords.get(i);
			vert.x += x;
			vert.y += y;
			vert.z += z;
			this.coords.set(i, vert);
		}
	}

	public void addRot(Quaternion rot) {
		int i;
		int numVerts = this.coords.size();

		for (i = 0; i < numVerts; i++)
			this.coords.set(i, Vector3.multiply(this.coords.get(i), rot));

		if (this.calcVertexNormals) {
			int numNormals = this.vertexNormals.size();
			for (i = 0; i < numNormals; i++)
				this.vertexNormals.set(i, Vector3.multiply(this.vertexNormals.get(i), rot));

			this.faceNormal = Vector3.multiply(this.faceNormal, rot);
			this.cutNormal1 = Vector3.multiply(this.cutNormal1, rot);
			this.cutNormal2 = Vector3.multiply(this.cutNormal2, rot);
		}
	}

	public void scale(float x, float y) {
		int i;
		int numVerts = this.coords.size();
		Vector3 vert;

		for (i = 0; i < numVerts; i++) {
			vert = this.coords.get(i);
			vert.x *= x;
			vert.y *= y;
			this.coords.set(i, vert);
		}
	}

	/// <summary>
	/// Changes order of the vertex indices and negates the center vertex normal.
	/// Does not alter vertex normals of radial vertices
	/// </summary>
	public void flipNormals() {
		int i;
		int numFaces = this.faces.size();
		Face tmpFace;
		int tmp;

		for (i = 0; i < numFaces; i++) {
			tmpFace = this.faces.get(i);
			tmp = tmpFace.v3;
			tmpFace.v3 = tmpFace.v1;
			tmpFace.v1 = tmp;
			this.faces.set(i, tmpFace);
		}

		if (this.calcVertexNormals) {
			int normalCount = this.vertexNormals.size();
			if (normalCount > 0) {
				Vector3 n = this.vertexNormals.get(normalCount - 1);
				n.z = -n.z;
				this.vertexNormals.set(normalCount - 1, n);
			}
		}

		this.faceNormal.x = -this.faceNormal.x;
		this.faceNormal.y = -this.faceNormal.y;
		this.faceNormal.z = -this.faceNormal.z;

		int numfaceUVs = this.faceUVs.size();
		for (i = 0; i < numfaceUVs; i++) {
			Vector2 uv = this.faceUVs.get(i);
			uv.y = 1.0f - uv.y;
			this.faceUVs.set(i, uv);
		}
	}

	public void addValue2FaceVertexIndices(int num) {
		int numFaces = this.faces.size();
		Face tmpFace;
		for (int i = 0; i < numFaces; i++) {
			tmpFace = this.faces.get(i);
			tmpFace.v1 += num;
			tmpFace.v2 += num;
			tmpFace.v3 += num;

			this.faces.set(i, tmpFace);
		}
	}

	public void addValue2FaceNormalIndices(int num) {
		if (this.calcVertexNormals) {
			int numFaces = this.faces.size();
			Face tmpFace;
			for (int i = 0; i < numFaces; i++) {
				tmpFace = this.faces.get(i);
				tmpFace.n1 += num;
				tmpFace.n2 += num;
				tmpFace.n3 += num;

				this.faces.set(i, tmpFace);
			}
		}
	}

	public void dumpRaw(String path, String name, String title) throws IOException {
		if (path == null)
			return;
		String fileName = name + "_" + title + ".raw";
		File completePath = new File(path, fileName);
		FileWriter sw = new FileWriter(completePath);

		for (int i = 0; i < this.faces.size(); i++) {
			sw.append(this.coords.get(this.faces.get(i).v1).toString() + " ");
			sw.append(this.coords.get(this.faces.get(i).v2).toString() + " ");
			sw.append(this.coords.get(this.faces.get(i).v3).toString() + "\n");
		}
		sw.close();
	}
}
