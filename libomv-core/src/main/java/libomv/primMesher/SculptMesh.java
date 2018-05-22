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
package libomv.primMesher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import libomv.primMesher.types.Face;
import libomv.primMesher.types.ViewerFace;
import libomv.types.Quaternion;
import libomv.types.Vector2;
import libomv.types.Vector3;

public class SculptMesh implements Cloneable {
	public List<Vector3> coords;
	public List<Face> faces;

	public List<ViewerFace> viewerFaces;
	public List<Vector3> normals;
	public List<Vector2> uvs;

	public enum SculptType {
		sphere, torus, plane, cylinder;

		public static SculptType valueOf(int value) {
			return values()[value];
		}
	};

	/**
	 * ** Experimental ** May disappear from future versions ** not recommeneded for
	 * use in applications Construct a sculpt mesh from a 2D array of floats
	 *
	 * @param zMap
	 * @param xBegin
	 * @param xEnd
	 * @param yBegin
	 * @param yEnd
	 * @param viewerMode
	 */
	public SculptMesh(float[][] zMap, float xBegin, float xEnd, float yBegin, float yEnd, boolean viewerMode) {
		float xStep;
		float yStep;
		float uStep;
		float vStep;

		int numYElements = zMap.length;
		int numXElements = zMap[0].length;

		try {
			xStep = (xEnd - xBegin) / (numXElements - 1);
			yStep = (yEnd - yBegin) / (numYElements - 1);

			uStep = 1.0f / (numXElements - 1);
			vStep = 1.0f / (numYElements - 1);
		} catch (Exception ex) {
			return;
		}

		coords = new ArrayList<>();
		faces = new ArrayList<>();
		normals = new ArrayList<>();
		uvs = new ArrayList<>();

		viewerFaces = new ArrayList<>();

		int p1;
		int p2;
		int p3;
		int p4;

		int x;
		int y;
		int xStart = 0;
		int yStart = 0;

		for (y = yStart; y < numYElements; y++) {
			int rowOffset = y * numXElements;

			for (x = xStart; x < numXElements; x++) {
				/*
				 * p1-----p2 | \ f2 | | \ | | f1 \| p3-----p4
				 */

				p4 = rowOffset + x;
				p3 = p4 - 1;

				p2 = p4 - numXElements;
				p1 = p3 - numXElements;

				Vector3 c = new Vector3(xBegin + x * xStep, yBegin + y * yStep, zMap[y][x]);
				this.coords.add(c);
				if (viewerMode) {
					this.normals.add(new Vector3(0f));
					this.uvs.add(new Vector2(uStep * x, 1.0f - vStep * y));
				}

				if (y > 0 && x > 0) {
					Face f1;
					Face f2;

					if (viewerMode) {
						f1 = new Face(p1, p4, p3, p1, p4, p3);
						f1.uv1 = p1;
						f1.uv2 = p4;
						f1.uv3 = p3;

						f2 = new Face(p1, p2, p4, p1, p2, p4);
						f2.uv1 = p1;
						f2.uv2 = p2;
						f2.uv3 = p4;
					} else {
						f1 = new Face(p1, p4, p3);
						f2 = new Face(p1, p2, p4);
					}

					this.faces.add(f1);
					this.faces.add(f2);
				}
			}
		}

		if (viewerMode)
			calcVertexNormals(SculptType.plane, numXElements, numYElements);
	}

	public SculptMesh(List<List<Vector3>> rows, SculptType sculptType, boolean viewerMode, boolean mirror,
			boolean invert) {
		coords = new ArrayList<>();
		faces = new ArrayList<>();
		normals = new ArrayList<>();
		uvs = new ArrayList<>();

		if (mirror)
			invert = !invert;

		viewerFaces = new ArrayList<>();

		int height = rows.size();
		int width = rows.get(0).size();

		int p1;
		int p2;
		int p3;
		int p4;

		int imageX;
		int imageY;

		if (sculptType != SculptType.plane) {
			if (height % 2 == 0) {
				for (int i = 0; i < rows.size(); i++)
					rows.get(i).add(rows.get(i).get(0));
			} else {
				int lastIndex = width - 1;

				for (int i = 0; i < rows.size(); i++)
					rows.get(i).set(0, rows.get(i).get(lastIndex));
			}
		}

		Vector3 topPole = rows.get(0).get(width / 2);
		Vector3 bottomPole = rows.get(height - 1).get(width / 2);
		if (sculptType == SculptType.sphere) {
			if (height % 2 == 0) {
				List<Vector3> topPoleRow = new ArrayList<>(width);
				List<Vector3> bottomPoleRow = new ArrayList<>(width);

				for (int i = 0; i < height; i++) {
					topPoleRow.add(topPole);
					bottomPoleRow.add(bottomPole);
				}
				rows.add(0, topPoleRow);
				rows.add(bottomPoleRow);
			} else {
				List<Vector3> topPoleRow = rows.get(0);
				List<Vector3> bottomPoleRow = rows.get(height - 1);

				for (int i = 0; i < height; i++) {
					topPoleRow.set(i, topPole);
					bottomPoleRow.set(i, bottomPole);
				}
			}
		}

		if (sculptType == SculptType.torus)
			rows.add(rows.get(0));

		int coordsDown = rows.size();
		int coordsAcross = rows.get(0).size();

		float widthUnit = 1.0f / (coordsAcross - 1);
		float heightUnit = 1.0f / (coordsDown - 1);

		for (imageY = 0; imageY < coordsDown; imageY++) {
			int rowOffset = imageY * coordsAcross;

			for (imageX = 0; imageX < coordsAcross; imageX++) {
				/*
				 * p1-----p2 | \ f2 | | \ | | f1 \| p3-----p4
				 */

				p4 = rowOffset + imageX;
				p3 = p4 - 1;

				p2 = p4 - coordsAcross;
				p1 = p3 - coordsAcross;

				this.coords.add(rows.get(imageY).get(imageX));
				if (viewerMode) {
					this.normals.add(new Vector3(0f));
					this.uvs.add(new Vector2(widthUnit * imageX, heightUnit * imageY));
				}

				if (imageY > 0 && imageX > 0) {
					Face f1;
					Face f2;

					if (viewerMode) {
						if (invert) {
							f1 = new Face(p1, p4, p3, p1, p4, p3);
							f1.uv1 = p1;
							f1.uv2 = p4;
							f1.uv3 = p3;

							f2 = new Face(p1, p2, p4, p1, p2, p4);
							f2.uv1 = p1;
							f2.uv2 = p2;
							f2.uv3 = p4;
						} else {
							f1 = new Face(p1, p3, p4, p1, p3, p4);
							f1.uv1 = p1;
							f1.uv2 = p3;
							f1.uv3 = p4;

							f2 = new Face(p1, p4, p2, p1, p4, p2);
							f2.uv1 = p1;
							f2.uv2 = p4;
							f2.uv3 = p2;
						}
					} else {
						if (invert) {
							f1 = new Face(p1, p4, p3);
							f2 = new Face(p1, p2, p4);
						} else {
							f1 = new Face(p1, p3, p4);
							f2 = new Face(p1, p4, p2);
						}
					}

					this.faces.add(f1);
					this.faces.add(f2);
				}
			}
		}

		if (viewerMode)
			calcVertexNormals(sculptType, coordsAcross, coordsDown);
	}

	public SculptMesh(SculptMesh sm) {
		coords = new ArrayList<>(sm.coords);
		faces = new ArrayList<>(sm.faces);
		viewerFaces = new ArrayList<>(sm.viewerFaces);
		normals = new ArrayList<>(sm.normals);
		uvs = new ArrayList<>(sm.uvs);
	}

	/**
	 * Duplicates a SculptMesh object. All object properties are copied by value,
	 * including lists.
	 */
	public SculptMesh copy() {
		return new SculptMesh(this);
	}

	private void calcVertexNormals(SculptType sculptType, int xSize, int ySize) {
		// compute vertex normals by summing all the surface normals of all the
		// triangles sharing
		// each vertex and then normalizing
		int numFaces = this.faces.size();
		for (int i = 0; i < numFaces; i++) {
			Face face = this.faces.get(i);
			Vector3 surfaceNormal = face.surfaceNormal(this.coords);
			this.normals.set(face.n1, Vector3.add(this.normals.get(face.n1), surfaceNormal));
			this.normals.set(face.n2, Vector3.add(this.normals.get(face.n2), surfaceNormal));
			this.normals.set(face.n3, Vector3.add(this.normals.get(face.n3), surfaceNormal));
		}

		int numNormals = this.normals.size();
		for (int i = 0; i < numNormals; i++)
			this.normals.set(i, Vector3.normalize(this.normals.get(i)));

		if (sculptType != SculptType.plane) { // blend the vertex normals at the cylinder seam
			for (int y = 0; y < ySize; y++) {
				int rowOffset = y * xSize;
				Vector3 vect = Vector3.add(this.normals.get(rowOffset), this.normals.get(rowOffset + xSize - 1))
						.normalize();

				this.normals.set(rowOffset, vect);
				this.normals.set(rowOffset + xSize - 1, vect);
			}
		}

		for (Face face : this.faces) {
			ViewerFace vf = new ViewerFace(0);
			vf.v1 = this.coords.get(face.v1);
			vf.v2 = this.coords.get(face.v2);
			vf.v3 = this.coords.get(face.v3);

			vf.coordIndex1 = face.v1;
			vf.coordIndex2 = face.v2;
			vf.coordIndex3 = face.v3;

			vf.n1 = this.normals.get(face.n1);
			vf.n2 = this.normals.get(face.n2);
			vf.n3 = this.normals.get(face.n3);

			vf.uv1 = this.uvs.get(face.uv1);
			vf.uv2 = this.uvs.get(face.uv2);
			vf.uv3 = this.uvs.get(face.uv3);

			this.viewerFaces.add(vf);
		}
	}

	/**
	 * Adds a value to each XYZ vertex coordinate in the mesh
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
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

		if (this.viewerFaces != null) {
			int numViewerFaces = this.viewerFaces.size();

			for (i = 0; i < numViewerFaces; i++) {
				ViewerFace v = this.viewerFaces.get(i);
				v.addPos(x, y, z);
				this.viewerFaces.set(i, v);
			}
		}
	}

	/**
	 * Rotates the mesh
	 *
	 * @param q
	 */
	public void addRot(Quaternion q) {
		int i;
		int numVerts = this.coords.size();

		for (i = 0; i < numVerts; i++)
			this.coords.set(i, Vector3.multiply(this.coords.get(i), q));

		int numNormals = this.normals.size();
		for (i = 0; i < numNormals; i++)
			this.normals.set(i, Vector3.multiply(this.normals.get(i), q));

		if (this.viewerFaces != null) {
			int numViewerFaces = this.viewerFaces.size();

			for (i = 0; i < numViewerFaces; i++) {
				ViewerFace v = this.viewerFaces.get(i);
				v.v1 = Vector3.multiply(v.v1, q);
				v.v2 = Vector3.multiply(v.v2, q);
				v.v3 = Vector3.multiply(v.v3, q);

				v.n1 = Vector3.multiply(v.n1, q);
				v.n2 = Vector3.multiply(v.n2, q);
				v.n3 = Vector3.multiply(v.n3, q);
				this.viewerFaces.set(i, v);
			}
		}
	}

	public void scale(float x, float y, float z) {
		int i;
		int numVerts = this.coords.size();

		Vector3 m = new Vector3(x, y, z);
		for (i = 0; i < numVerts; i++)
			this.coords.set(i, Vector3.multiply(this.coords.get(i), m));

		if (this.viewerFaces != null) {
			int numViewerFaces = this.viewerFaces.size();
			for (i = 0; i < numViewerFaces; i++) {
				ViewerFace v = this.viewerFaces.get(i);
				v.v1 = Vector3.multiply(v.v1, m);
				v.v2 = Vector3.multiply(v.v2, m);
				v.v3 = Vector3.multiply(v.v3, m);
				this.viewerFaces.set(i, v);
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
