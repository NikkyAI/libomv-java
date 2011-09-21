/**
 * Copyright (c) 2006-2008, openmetaverse.org
 * Portions Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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
package libomv;

public class AppearenceManager
{
    // Index of TextureEntry slots for avatar appearances
    public enum AvatarTextureIndex
    {
        Unknown,
        HeadBodypaint,
        UpperShirt,
        LowerPants,
        EyesIris,
        Hair,
        UpperBodypaint,
        LowerBodypaint,
        LowerShoes,
        HeadBaked,
        UpperBaked,
        LowerBaked,
        EyesBaked,
        LowerSocks,
        UpperJacket,
        LowerJacket,
        UpperGloves,
        UpperUndershirt,
        LowerUnderpants,
        Skirt,
        SkirtBaked,
        HairBaked;

        public static AvatarTextureIndex setValue(int value)
        {
        	return values()[value + 1];
        }

        public static byte getValue(AvatarTextureIndex value)
        {
        	return (byte)(value.ordinal() - 1);
        }

        public byte getValue()
        {
        	return (byte)(ordinal() - 1);
        }
    }

    // Bake layers for avatar appearance
    public enum BakeType
    {
        Unknown,
        Head,
        UpperBody,
        LowerBody,
        Eyes,
        Skirt,
        Hair;

        public static BakeType setValue(int value)
        {
        	return values()[value + 1];
        }

        public static byte getValue(BakeType value)
        {
        	return (byte)(value.ordinal() - 1);
        }
    }

}
