/*
 * Copyright (c) 2007-2008, openmetaverse.org
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
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
package objects;

import java.io.IOException;

import junit.framework.Assert;
import junit.framework.TestCase;
import libomv.primitives.Primitive;
import libomv.primitives.TextureEntry;
import libomv.primitives.TextureEntry.Bumpiness;
import libomv.primitives.TextureEntry.MappingType;
import libomv.primitives.TextureEntry.Shininess;
import libomv.primitives.TextureEntry.TextureEntryFace;
import libomv.types.Color4;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class PrimObjectTests extends TestCase
{
        public void testPathBegin()
        {
            for (byte i = 0; i < Byte.MAX_VALUE; i++)
            {
                float floatValue = Primitive.UnpackBeginCut(i);
                short result = Primitive.PackBeginCut(floatValue);

                Assert.assertTrue("Started with " + i + ", float value was " + floatValue +
                    ", and ended up with " + result, result == i);
            }
        }

        public void testPathEnd()
        {
            for (byte i = 0; i < Byte.MAX_VALUE; i++)
            {
                float floatValue = Primitive.UnpackEndCut(i);
                short result = Primitive.PackEndCut(floatValue);

                Assert.assertTrue("Started with " + i + ", float value was " + floatValue +
                    ", and ended up with " + result, result == i);
            }
        }

        public void testPathRevolutions()
        {
            for (byte i = 0; i < Byte.MAX_VALUE; i++)
            {
                float floatValue = Primitive.UnpackPathRevolutions(i);
                byte result = Primitive.PackPathRevolutions(floatValue);

                Assert.assertTrue("Started with " + i + ", float value was " + floatValue +
                    ", and ended up with " + result, result == i);
            }
        }

        public void testPathScale()
        {
            for (byte i = 0; i < Byte.MAX_VALUE; i++)
            {
                float floatValue = Primitive.UnpackPathScale(i);
                byte result = Primitive.PackPathScale(floatValue);

                Assert.assertTrue("Started with " + i + ", float value was " + floatValue +
                    ", and ended up with " + result, result == i);
            }
        }

        public void testPathShear()
        {
            for (byte i = 0; i < Byte.MAX_VALUE; i++)
            {
                float floatValue = Primitive.UnpackPathShear(i);
                byte result = Primitive.PackPathShear(floatValue);

                Assert.assertTrue("Started with " + i + ", float value was " + floatValue +
                ", and ended up with " + result, result == i);
            }
        }

        public void testPathTaper()
        {
            for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++)
            {
                float floatValue = Primitive.UnpackPathTaper(i);
                byte result = Primitive.PackPathTaper(floatValue);

                Assert.assertTrue("Started with " + i + ", float value was " + floatValue +
                ", and ended up with " + result, result == i);
            }
        }

        public void testTextureEntryOffsets()
        {
            for (int i = -1000; i <= 1000; i++)
            {
                float f = (float)i / 1000;

                byte[] offset = Helpers.TEOffsetShort(f);
                float foffset = Helpers.TEOffsetFloat(offset, 0);

                Assert.assertTrue(foffset + " is not equal to " + f, foffset - f < 0.0001);
            }
        }

        public void testTextureEntry() throws IOException
        {
        	byte[] teTest = {-38, -78, 80, 125, 122, -42, 66, 54, -100, -40, 1, -66, 106, -116, 25, -39,
        			          1,
        	/* 17: */         94, -61, 38, -36, 26, -123, 75, 14, -109, -124, 111, 77, 106, 44, 59, -68,
        			          0,
            /* 34: Color */   0, 0, 0, 0,
        			          1,
        			          -1, -64, 64, 0,
        			          0,
        	/* 44: RepeatU */ 0, 0, -128, 63,
        			          1,
        			          0, 0, 64, 64,
        			          0,
        	/* 54: RepeatV */ 0, 0, -128, 63,
        			          1,
        			          0, 0, -128, 64,		         
        			          0,
        	/* 64: OffsetU */ 0, 0,
        			          1,
        			          0, 64,
        			          0,
            /* 70: OffsetV */ 0, 0,
        			          1,
        			          0, -64,
        			          0,
            /* 76: Rotation */ 0, 0,
        			          1,
        			          -113, 30,
        			          0,
        	/* 82: Material */ 0,
        			          1,
        			          -89,
        			          0,
        	/* 86: Media */   0,
        			          1,
        			          3,
        			          0,
        	/* 90: Glow */    0};

            TextureEntry te = new TextureEntry(new UUID("dab2507d-7ad6-4236-9cd8-01be6a8c19d9"));
            TextureEntryFace face = te.CreateFace(0);
            face.setBump(Bumpiness.Concrete);
            face.setFullbright(true);
            face.setMediaFlags(true);
            face.setOffsetU(0.5f);
            face.setOffsetV(-0.5f);
            face.setRepeatU(3.0f);
            face.setRepeatV(4.0f);
            face.setRGBA(new Color4(0f, 0.25f, 0.75f, 1f));
            face.setRotation(1.5f);
            face.setShiny(Shininess.Medium);
            face.setTexMapType(MappingType.Planar);
            face.setTextureID(new UUID("5ec326dc-1a85-4b0e-9384-6f4d6a2c3bbc"));

            byte[] teBytes = te.getBytes();

            Assert.assertTrue(teBytes.length == teTest.length);

            for (int i = 0; i < teBytes.length; i++)
            {
                Assert.assertTrue("Byte " + i + " is not equal", teBytes[i] == teTest[i]);
            }

            TextureEntry te2 = new TextureEntry(teBytes, 0, teBytes.length);

            byte[] teBytes2 = te2.getBytes();

            Assert.assertTrue(teBytes.length == teBytes2.length);

            for (int i = 0; i < teBytes.length; i++)
            {
                Assert.assertTrue("Byte " + i + " is not equal", teBytes[i] == teBytes2[i]);
            }
        }
    }