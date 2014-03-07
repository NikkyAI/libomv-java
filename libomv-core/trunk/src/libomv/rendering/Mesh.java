/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2014, Frederick Martian
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
import libomv.primitives.TextureEntry;
import libomv.types.Quaternion;
import libomv.types.Vector2;
import libomv.types.Vector3;

public abstract class Mesh
{
    // #region Enums

	public enum FaceType
	{
		PathBegin(1 << 0),
		PathEnd(1 << 1),
		InnerSide(1 << 2), 
		ProfileBegin(1 << 3), 
		ProfileEnd(1 << 4), 
		OuterSide0(1 << 5), 
		OuterSide1(1 << 6), 
		OuterSide2(1 << 7), 
		OuterSide3(1 << 8);

		public static FaceType setValue(int value)
		{
			for (FaceType e : values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}
		
		public short getValue()
		{
			return _value;
		}

		private short _value;

		private FaceType(int value)
		{
			_value = (short) value;
		}
	}

    // [Flags]
    public enum FaceMask
    {
    	Single(1 << 0),
		Cap(1 << 1),
		End(1 << 2), 
		Side(1 << 3), 
		Inner(1 << 4), 
		Outer(1 << 5), 
		Hollow(1 << 6), 
		Open(1 << 7), 
		Flat(1 << 8),
        Top(1 << 9),
        Bottom(1 << 10);

        public static FaceMask setValue(int value)
		{
			for (FaceMask e : values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}
		
		public short getValue()
		{
			return _value;
		}

		private short _value;

		private FaceMask(int value)
		{
			_value = (short) value;
		}
    }

    public enum DetailLevel
    {
        Low,
        Medium,
        High,
        Highest
    }

    // #endregion Enums

    // #region Structs

    public class Vertex
    {
        public Vector3 Position;
        public Vector3 Normal;
        public Vector2 TexCoord;

        @Override
		public String toString()
        {
            return String.format("P: %s N: %s T: %s", Position, Normal, TexCoord);
        }
    }

    public class ProfileFace
    {
        public int Index;
        public int Count;
        public float ScaleU;
        public boolean Cap;
        public boolean Flat;
        public FaceType Type;

        @Override
		public String toString()
        {
            return Type.toString();
        }
    };

    public class Profile
    {
        public float MinX;
        public float MaxX;
        public boolean Open;
        public boolean Concave;
        public int TotalOutsidePoints;
        public List<Vector3> Positions;
        public List<ProfileFace> Faces;
    }

    public class PathPoint
    {
        public Vector3 Position;
        public Vector2 Scale;
        public Quaternion Rotation;
        public float TexT;
    }

    public class Path
    {
        public List<PathPoint> Points;
        public boolean Open;
    }

    public class Face
    {
        // Only used for Inner/Outer faces
        public int BeginS;
        public int BeginT;
        public int NumS;
        public int NumT;

        public int ID;
        public Vector3 Center;
        public Vector3 MinExtent;
        public Vector3 MaxExtent;
        public List<Vertex> Vertices;
        public List<Integer> Indices;
        public List<Integer> Edge;
        public FaceMask Mask;
        public TextureEntry.TextureEntryFace TextureFace;
        public Object UserData;

        @Override
		public String toString()
        {
            return Mask.toString();
        }
    }

    //#endregion Structs

    public Primitive Prim;
    public Path Path;
    public Profile Profile;

    @Override
	public String toString()
    {
        if (Prim.Properties != null && !Prim.Properties.Name.isEmpty())
        {
            return Prim.Properties.Name;
        }
		return String.format("(%d) (%s)", Prim.LocalID, Prim.PrimData.toString());
    }
}
