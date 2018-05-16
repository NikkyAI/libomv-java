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

import libomv.types.Quaternion;
import libomv.types.Vector2;
import libomv.types.Vector3;

public class ViewerFace {
	public int primFaceNumber;

	public Vector3 v1;
	public Vector3 v2;
	public Vector3 v3;

	public int coordIndex1;
	public int coordIndex2;
	public int coordIndex3;

	public Vector3 n1;
	public Vector3 n2;
	public Vector3 n3;

	public Vector2 uv1;
	public Vector2 uv2;
	public Vector2 uv3;

	public ViewerFace(int primFaceNumber) {
		this.primFaceNumber = primFaceNumber;

		this.v1 = new Vector3(0);
		this.v2 = new Vector3(0);
		this.v3 = new Vector3(0);

		this.coordIndex1 = this.coordIndex2 = this.coordIndex3 = -1; // -1 means not assigned yet

		this.n1 = new Vector3(0);
		this.n2 = new Vector3(0);
		this.n3 = new Vector3(0);

		this.uv1 = new Vector2();
		this.uv2 = new Vector2();
		this.uv3 = new Vector2();
	}

	public void scale(float x, float y, float z) {
		this.v1.X *= x;
		this.v1.Y *= y;
		this.v1.Z *= z;

		this.v2.X *= x;
		this.v2.Y *= y;
		this.v2.Z *= z;

		this.v3.X *= x;
		this.v3.Y *= y;
		this.v3.Z *= z;
	}

	public void addPos(float x, float y, float z) {
		this.v1.X += x;
		this.v2.X += x;
		this.v3.X += x;

		this.v1.Y += y;
		this.v2.Y += y;
		this.v3.Y += y;

		this.v1.Z += z;
		this.v2.Z += z;
		this.v3.Z += z;
	}

	public void addRot(Quaternion rot) {
		this.v1.multiply(rot);
		this.v2.multiply(rot);
		this.v3.multiply(rot);

		this.n1.multiply(rot);
		this.n2.multiply(rot);
		this.n3.multiply(rot);
	}

	public void calcSurfaceNormal() {

		Vector3 edge1 = new Vector3(this.v2.X - this.v1.X, this.v2.Y - this.v1.Y, this.v2.Z - this.v1.Z);
		Vector3 edge2 = new Vector3(this.v3.X - this.v1.X, this.v3.Y - this.v1.Y, this.v3.Z - this.v1.Z);

		this.n1 = edge1.cross(edge2).normalize();
		this.n2 = new Vector3(this.n1);
		this.n3 = new Vector3(this.n1);
	}
}
