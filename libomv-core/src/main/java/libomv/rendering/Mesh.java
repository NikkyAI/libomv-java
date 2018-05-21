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

import java.util.List;

import libomv.primitives.Primitive;
import libomv.primitives.TextureEntry.TextureEntryFace;
import libomv.types.Quaternion;
import libomv.types.Vector2;
import libomv.types.Vector3;

public abstract class Mesh {

	public enum FaceType {
		PathBegin(1 << 0), PathEnd(1 << 1), InnerSide(1 << 2), ProfileBegin(1 << 3), ProfileEnd(1 << 4), OuterSide0(
				1 << 5), OuterSide1(1 << 6), OuterSide2(1 << 7), OuterSide3(1 << 8);

		private short value;

		private FaceType(int value) {
			this.value = (short) value;
		}

		public static FaceType setValue(int value) {
			for (FaceType e : values()) {
				if (e.value == value)
					return e;
			}
			return null;
		}

		public short getValue() {
			return value;
		}

	}

	// [Flags]
	public enum FaceMask {
		Single(1 << 0), Cap(1 << 1), End(1 << 2), Side(1 << 3), Inner(1 << 4), Outer(1 << 5), Hollow(1 << 6), Open(
				1 << 7), Flat(1 << 8), Top(1 << 9), Bottom(1 << 10);

		private short value;

		private FaceMask(int value) {
			this.value = (short) value;
		}

		public static FaceMask setValue(int value) {
			for (FaceMask e : values()) {
				if (e.value == value)
					return e;
			}
			return null;
		}

		public short getValue() {
			return value;
		}

	}

	public enum DetailLevel {
		Low, Medium, High, Highest
	}

	// #endregion Enums

	// #region Structs

	public class Vertex {
		public Vector3 position;
		public Vector3 normal;
		public Vector2 texCoord;

		@Override
		public String toString() {
			return String.format("P: %s N: %s T: %s", position, normal, texCoord);
		}
	}

	public class ProfileFace {
		public int index;
		public int count;
		public float scaleU;
		public boolean cap;
		public boolean flat;
		public FaceType type;

		@Override
		public String toString() {
			return type.toString();
		}
	};

	public class Profile {
		public float minX;
		public float maxX;
		public boolean open;
		public boolean concave;
		public int totalOutsidePoints;
		public List<Vector3> positions;
		public List<ProfileFace> faces;
	}

	public class PathPoint {
		public Vector3 position;
		public Vector2 scale;
		public Quaternion rotation;
		public float texT;
	}

	public class Path {
		public List<PathPoint> points;
		public boolean open;
	}

	public class Face {
		// Only used for Inner/Outer faces
		public int beginS;
		public int beginT;
		public int numS;
		public int numT;

		public int id;
		public Vector3 center;
		public Vector3 minExtent;
		public Vector3 maxExtent;
		public List<Vertex> vertices;
		public List<Integer> indices;
		public List<Integer> edge;
		public FaceMask mask;
		public TextureEntryFace textureFace;
		public Object userData;

		@Override
		public String toString() {
			return mask.toString();
		}
	}

	// #endregion Structs

	public Primitive prim;
	public Path path;
	public Profile profile;

	@Override
	public String toString() {
		if (prim.properties != null && !prim.properties.name.isEmpty()) {
			return prim.properties.name;
		}
		return String.format("(%d) (%s)", prim.localID, prim.primData.toString());
	}
}
