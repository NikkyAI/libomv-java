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

import java.util.List;

import libomv.types.Vector3;

public class Face {
	public int primFace;

	// vertices
	public int v1;
	public int v2;
	public int v3;

	// normals
	public int n1;
	public int n2;
	public int n3;

	// uvs
	public int uv1;
	public int uv2;
	public int uv3;

	public Face() {
		this(0, 0, 0);
	}

	public Face(int v1, int v2, int v3) {
		this(v1, v2, v3, 0, 0, 0);
	}

	public Face(int v1, int v2, int v3, int n1, int n2, int n3) {
		primFace = 0;

		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;

		this.n1 = n1;
		this.n2 = n2;
		this.n3 = n3;

		this.uv1 = 0;
		this.uv2 = 0;
		this.uv3 = 0;
	}

	public Face(Face face) {
		primFace = face.primFace;

		this.v1 = face.v1;
		this.v2 = face.v2;
		this.v3 = face.v3;

		this.n1 = face.n1;
		this.n2 = face.n2;
		this.n3 = face.n3;

		this.uv1 = face.uv1;
		this.uv2 = face.uv2;
		this.uv3 = face.uv3;
	}

	public Vector3 surfaceNormal(List<Vector3> coordList) {
		Vector3 c1 = coordList.get(this.v1);
		Vector3 c2 = coordList.get(this.v2);
		Vector3 c3 = coordList.get(this.v3);

		Vector3 edge1 = new Vector3(c2.x - c1.x, c2.y - c1.y, c2.z - c1.z);
		Vector3 edge2 = new Vector3(c3.x - c1.x, c3.y - c1.y, c3.z - c1.z);

		return edge1.cross(edge2).normalize();
	}
}
