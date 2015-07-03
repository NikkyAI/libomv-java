
/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2012-2014, Frederick Martian
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
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map.Entry;

import junit.framework.TestCase;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.types.Matrix4;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.utils.BitPack;
import libomv.utils.Helpers;

public class TypeTests extends TestCase
{
    public void testUUIDs()
    {
        // Creation
        UUID a = new UUID();
        byte[] bytes = a.getBytes();
        for (int i = 0; i < 16; i++)
            assertFalse(bytes[i] == 0x00);

        // Comparison
        a = new UUID(new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A,
            0x0B, 0x0C, 0x0D, 0x0E, 0x0F, (byte)0xFF, (byte)0xFF }, 0);
        UUID b = new UUID(new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A,
            0x0B, 0x0C, 0x0D, 0x0E, 0x0F }, 0);

        assertTrue("UUID comparison operator failed, " + a.toString() + " should equal " + b.toString(), a.equals(b));

        // From string
        a = new UUID(new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A,
            0x0B, 0x0C, 0x0D, 0x0E, 0x0F }, 0);
        String zeroonetwo = "00010203-0405-0607-0809-0a0b0c0d0e0f";
        b = new UUID(zeroonetwo);

        assertTrue("UUID hyphenated string constructor failed, should have " + a.toString() + " but we got " + b.toString(), a.equals(b));

        // ToString()            
        assertTrue(a.equals(b));                        
        assertTrue(a.toString().equals(zeroonetwo));

        // TODO: CRC test
    }

    public void testVector3ApproxEquals()
    {
        Vector3 a = new Vector3(1f, 0f, 0f);
        Vector3 b = new Vector3(0f, 0f, 0f);

        assertFalse("ApproxEquals failed (1)", a.approxEquals(b, 0.9f));
        assertTrue("ApproxEquals failed (2)", a.approxEquals(b, 1.1f));

        a = new Vector3(-1f, 0f, 0f);
        b = new Vector3(1f, 0f, 0f);

        assertFalse("ApproxEquals failed (3)", a.approxEquals(b, 1.9f));
        assertTrue("ApproxEquals failed (4)", a.approxEquals(b, 2.1f));

        a = new Vector3(0f, -1f, 0f);
        b = new Vector3(0f, -1.1f, 0f);

        assertFalse("ApproxEquals failed (5)", a.approxEquals(b, 0.09f));
        assertTrue("ApproxEquals failed (6)", a.approxEquals(b, 0.11f));

        a = new Vector3(0f, 0f, 0.00001f);
        b = new Vector3(0f, 0f, 0f);

        assertFalse("ApproxEquals failed (6)", b.approxEquals(a, 0.000001f));
        assertTrue("ApproxEquals failed (7)", b.approxEquals(a, 0.0001f));
    }

    public void testVectorCasting()
    {
        HashMap<String, Double> testNumbers;
        testNumbers = new HashMap<String, Double>();
        testNumbers.put("1.1", 1.1);
        testNumbers.put("1.01", 1.01);
        testNumbers.put("1.001", 1.001);
        testNumbers.put("1.0001", 1.0001);
        testNumbers.put("1.00001", 1.00001);
        testNumbers.put("1.000001", 1.000001);
        testNumbers.put("1.0000001", 1.0000001);
        testNumbers.put("1.00000001", 1.00000001);

        for (Entry<String, Double> kvp : testNumbers.entrySet())
        {
            double testNumber = kvp.getValue();
            double testNumber2 = ((float)testNumber);
            boolean noPrecisionLoss = testNumber == testNumber2;

            Vector3 a = new Vector3(
                    (float)testNumber,
                    (float)testNumber, (float)testNumber);
            Vector3d b = new Vector3d(testNumber, testNumber, testNumber);

            Vector3 c = new Vector3(b);
            Vector3d d = new Vector3d(a);

            if (noPrecisionLoss)
            {
                System.err.println("Unsuitable test value used-" +
                        " test number should have precision loss when" +
                        " cast to float (" + kvp.getKey() + ").");
            }
            else
            {
                assertFalse(String.format("Vector casting failed, precision loss should" +
                        " have occurred. %s: %f, %f", kvp.getKey(), a.X, b.X), a.equals(b));
                assertFalse(String.format("Vector casting failed, explicit cast of double" +
                        " to float should result in precision loss whichwas should not magically disappear when" +
                        " Vector3 is implicitly cast to Vector3d. %s: %f, %f", kvp.getKey(), b.X, d.X), b.equals(d));
            }
            assertTrue(String.format("Vector casting failed, Vector3 compared to" +
                    " explicit cast of Vector3d to Vector3 should result in identical precision loss." +
                    " %s: %f, %f", kvp.getKey(), a.X, c.X), a.equals(c));
            assertTrue(String.format("Vector casting failed, implicit cast of Vector3" +
                    " to Vector3d should not result in precision loss. %s: %f, %f", kvp.getKey(), a.X, d.X), a.equals(d));
        }
    }

    public void testQuaternions()
    {
        Quaternion a = new Quaternion(1, 0, 0, 0);
        Quaternion b = new Quaternion(1, 0, 0, 0);

        assertTrue("Quaternion comparison operator failed", a.equals(b));

        Quaternion expected = new Quaternion(0, 0, 0, -1);
        Quaternion result = a.multiply(b);

        assertTrue(a.toString() + " * " + b.toString() + " produced " + result.toString() +
            " instead of " + expected.toString(), result.equals(expected));

        a = new Quaternion(1, 0, 0, 0);
        b = new Quaternion(0, 1, 0, 0);
        expected = new Quaternion(0, 0, 1, 0);
        result = a.multiply(b);

        assertTrue(a.toString() + " * " + b.toString() + " produced " + result.toString() +
            " instead of " + expected.toString(), result.equals(expected));

        a = new Quaternion(0, 0, 1, 0);
        b = new Quaternion(0, 1, 0, 0);
        expected = new Quaternion(-1, 0, 0, 0);
        result = a.multiply(b);

        assertTrue(a.toString() + " * " + b.toString() + " produced " + result.toString() +
            " instead of " + expected.toString(), result.equals(expected));
    }
    
    
    public void testMatrix() throws Exception
    {
    	Matrix4 matrix = new Matrix4(0, 0, 74, 1,
				                     0, 435, 0, 1,
				                     345, 0, 34, 1,
				                     0, 0, 0, 0);
    	
    	/* determinant of singular matrix returns zero */
    	assertEquals("Determinant of singular matrix is not 0", 0d, matrix.determinant(), 0.001d);
    	
    	/* inverse of identity matrix is the identity matrix */
       	assertTrue("Inverse of identity matrix should be the identity matrix", Matrix4.Identity.equals(Matrix4.inverse(Matrix4.Identity)));
  	 
    	/* inverse of non-singular matrix test */
        matrix = new Matrix4(1, 1, 0, 0,
    			    		 1, 1, 1, 0,
    					     0, 1, 1, 0,
    					     0, 0, 0, 1);
    	Matrix4 expectedInverse = new Matrix4(0, 1,-1, 0,
    						                  1,-1, 1, 0,
    						                 -1, 1, 0, 0,
    						                  0, 0, 0, 1);
    	assertEquals("inverse of simple non singular matrix failed", expectedInverse, Matrix4.inverse(matrix));
    }

    //public void testVectorQuaternionMath()
    //{
    //    // Convert a vector to a quaternion and back
    //    Vector3 a = new Vector3(1f, 0.5f, 0.75f);
    //    Quaternion b = a.ToQuaternion();
    //    Vector3 c;
    //    b.GetEulerAngles(out c.X, out c.Y, out c.Z);

    //    IsTrue(a == c, c.ToString() + " does not equal " + a.ToString());
    //}

    public void testFloatsToTerseStrings()
    {
        float f = 1.20f;
        String a, b = "1.2";
        
        a = Helpers.FloatToTerseString(f);
        boolean e =  a.equals(b);
        assertTrue(String.format("%f converted to %s, expecting %s", f, a, b), e);

        f = 24.00f;
        b = "24";

        a = Helpers.FloatToTerseString(f);
        assertTrue(String.format("%f converted to %s, expecting %s", f, a, b), a.equals(b));

        f = -0.59f;
        b = "-.59";

        a = Helpers.FloatToTerseString(f);
        assertTrue(String.format("%f converted to %s, expecting %s", f, a, b), a.equals(b));

        f = 0.59f;
        b = ".59";

        a = Helpers.FloatToTerseString(f);
        assertTrue(String.format("%f converted to %s, expecting %s", f, a, b), a.equals(b));
    }

    public void testBitUnpacking()
    {
        byte[] data = new byte[] { (byte)0x80, 0x00, 0x0F, 0x50, (byte)0x83, 0x7D };
        BitPack bitpacker = new BitPack(data, 0);

        int b = bitpacker.UnpackBits(1);
        assertTrue("Unpacked " + b + " instead of 1", b == 1);

        b = bitpacker.UnpackBits(1);
        assertTrue("Unpacked " + b + " instead of 0", b == 0);

        bitpacker = new BitPack(data, 2);

        b = bitpacker.UnpackBits(4);
        assertTrue("Unpacked " + b + " instead of 0", b == 0);

        b = bitpacker.UnpackBits(8);
        assertTrue("Unpacked " + b + " instead of 0xF5", b == 0xF5);

        b = bitpacker.UnpackBits(4);
        assertTrue("Unpacked " + b + " instead of 0", b == 0);

        b = bitpacker.UnpackBits(10);
        assertTrue("Unpacked " + b + " instead of 0x0183", b == 0x0183);
    }

    public void testBitPacking()
    {
        byte[] packedBytes = new byte[12];
        BitPack bitpacker = new BitPack(packedBytes, 0);

        bitpacker.PackBits(0x0ABBCCDD, 32);
        bitpacker.PackBits(25, 5);
        bitpacker.PackFloat(123.321f);
        bitpacker.PackBits(1000, 16);

        bitpacker = new BitPack(packedBytes, 0);

        int b = bitpacker.UnpackBits(32);
        assertTrue("Unpacked " + b + " instead of 2864434397", b == 0x0ABBCCDD);

        b = bitpacker.UnpackBits(5);
        assertTrue("Unpacked " + b + " instead of 25", b == 25);

        float f = bitpacker.UnpackFloat();
        assertTrue("Unpacked " + f + " instead of 123.321", f == 123.321f);

        b = bitpacker.UnpackBits(16);
        assertTrue("Unpacked " + b + " instead of 1000", b == 1000);

        packedBytes = new byte[1];
        bitpacker = new BitPack(packedBytes, 0);
        bitpacker.PackBit(true);

        bitpacker = new BitPack(packedBytes, 0);
        b = bitpacker.UnpackBits(1);
        assertTrue("Unpacked " + b + " instead of 1", b == 1);

        packedBytes = new byte[1];
        packedBytes[0] = Byte.MAX_VALUE;
        bitpacker = new BitPack(packedBytes, 0);
        bitpacker.PackBit(false);

        bitpacker = new BitPack(packedBytes, 0);
        b = bitpacker.UnpackBits(1);
        assertTrue("Unpacked " + b + " instead of 0", b == 0);
    }

    public void testLLSDTerseParsing() throws IOException, ParseException
    {
        String testOne = "[r0.99967899999999998428,r-0.025334599999999998787,r0]";
        String testTwo = "[[r1,r1,r1],r0]";
        String testThree = "{'region_handle':[r255232, r256512], 'position':[r33.6, r33.71, r43.13], 'look_at':[r34.6, r33.71, r43.13]}";

        OSD obj = OSDParser.deserialize(testOne, OSDFormat.Notation);
        assertTrue("Expected OSDArray, got " + obj.getType().toString(), obj instanceof OSDArray);
        OSDArray array = (OSDArray)obj;
        assertTrue("Expected three contained objects, got " + array.size(), array.size() == 3);
        assertTrue("Unexpected value for first real " + array.get(0).AsReal(), array.get(0).AsReal() > 0.9d && array.get(0).AsReal() < 1.0d);
        assertTrue("Unexpected value for second real " + array.get(1).AsReal(), array.get(1).AsReal() < 0.0d && array.get(1).AsReal() > -0.03d);
        assertTrue("Unexpected value for third real " + array.get(2).AsReal(), array.get(2).AsReal() == 0.0d);

        obj = OSDParser.deserialize(testTwo, OSDFormat.Notation);
        assertTrue("Expected OSDArray, got " + obj.getType().toString(), obj instanceof OSDArray);
        array = (OSDArray)obj;
        assertTrue("Expected two contained objects, got " + array.size(), array.size() == 2);
        assertTrue("Unexpected value for real " + array.get(1).AsReal(), array.get(1).AsReal() == 0.0d);
        obj = array.get(0);
        assertTrue("Expected ArrayList, got " + obj.getType().toString(), obj instanceof OSDArray);
        array = (OSDArray)obj;
        assertTrue("Unexpected value(s) for nested array: " + array.get(0).AsReal() + ", " + array.get(1).AsReal() + ", " + array.get(2).AsReal(),
        		          array.get(0).AsReal() == 1.0d && array.get(1).AsReal() == 1.0d && array.get(2).AsReal() == 1.0d);

        obj = OSDParser.deserialize(testThree, OSDFormat.Notation);
        assertTrue("Expected OSDMap, got " + obj.getType().toString(), obj instanceof OSDMap);
        OSDMap hashtable = (OSDMap)obj;
        assertTrue("Expected three contained objects, got " + hashtable.size(), hashtable.size() == 3);
        assertTrue(hashtable.get("region_handle") instanceof OSDArray);
        assertTrue(((OSDArray)hashtable.get("region_handle")).size() == 2);
        assertTrue(hashtable.get("position") instanceof OSDArray);
        assertTrue(((OSDArray)hashtable.get("position")).size() == 3);
        assertTrue(hashtable.get("look_at") instanceof OSDArray);
        assertTrue(((OSDArray)hashtable.get("look_at")).size() == 3);
    }
}