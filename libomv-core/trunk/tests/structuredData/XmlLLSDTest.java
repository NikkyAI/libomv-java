/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Portions Copyright (c) 2012, Frederick Martian
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
package structuredData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDBinary;
import libomv.StructuredData.OSDBoolean;
import libomv.StructuredData.OSDDate;
import libomv.StructuredData.OSDInteger;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.StructuredData.OSDReal;
import libomv.StructuredData.OSDString;
import libomv.StructuredData.OSDUUID;
import libomv.StructuredData.OSDUri;
import libomv.types.UUID;
import libomv.utils.Helpers;

/// XmlLLSDTests is a suite of tests for libomv implementation of the LLSD XML format.
public class XmlLLSDTest extends TestCase
{
    /// Test that the sample LLSD supplied by Linden Lab is properly deserialized.
    /// The LLSD string in the test is a pared down version of the sample on the blog.
    /// http://wiki.secondlife.com/wiki/LLSD
    public void testDeserializeLLSDSample() throws IOException, ParseException
    {
        OSD theSD = null;
        OSDMap map = null;
        OSD tempSD = null;
        OSDUUID tempUUID = null;
        OSDString tempStr = null;
        OSDReal tempReal = null;

        String testSD = "<?xml version='1.0' encoding='UTF-8'?>\r\n"+
        "<llsd>\r\n"+
        "    <map>\r\n"+
        "        <key>region_id</key>\r\n" +
        "        <uuid>67153d5b-3659-afb4-8510-adda2c034649</uuid>\r\n" +
        "        <key>scale</key>\r\n" +
        "        <string>one minute</string>\r\n" +
        "        <key>simulator statistics</key>\r\n" +
        "        <map>\r\n" +
	    "            <key>time dilation</key>\r\n" +
	    "            <real>0.9878624</real>\r\n" +
	    "            <key>sim fps</key>\r\n" +
	    "            <real>44.38898</real>\r\n" +
	    "            <key>agent updates per second</key>\r\n" +
	    "            <real>nan</real>\r\n" +
	    "            <key>total task count</key>\r\n" +
	    "            <real>4</real>\r\n" +
	    "            <key>active task count</key>\r\n" +
	    "            <real>0</real>\r\n" +
	    "            <key>pending uploads</key>\r\n" +
	    "            <real>0.0001096525</real>\r\n" +
        "        </map>\r\n" +
        "    </map>\r\n" +
        "</llsd>";

        //Deserialize the string
        byte[] bytes = testSD.getBytes(Helpers.UTF8_ENCODING);
        theSD = OSDParser.deserialize(bytes);

        //Confirm the contents
        assertNotNull(theSD);
        assertTrue(theSD instanceof OSDMap);
        assertEquals(theSD.getType(), OSDType.Map);
        map = (OSDMap)theSD;

        tempSD = map.get("region_id");
        assertNotNull(tempSD);
        assertTrue(tempSD instanceof OSDUUID);
        assertTrue(tempSD.getType() == OSDType.UUID);
        tempUUID = (OSDUUID)tempSD;
        assertEquals(new UUID("67153d5b-3659-afb4-8510-adda2c034649"), tempUUID.AsUUID());

        tempSD = map.get("scale");
        assertNotNull(tempSD);
        assertTrue(tempSD instanceof OSDString);
        assertTrue(tempSD.getType() == OSDType.String);
        tempStr = (OSDString)tempSD;
        assertEquals("one minute", tempStr.AsString());

        tempSD = map.get("simulator statistics");
        assertNotNull(tempSD);
        assertTrue(tempSD instanceof OSDMap);
        assertTrue(tempSD.getType() == OSDType.Map);
        map = (OSDMap)tempSD;

        tempSD = map.get("time dilation");
        assertNotNull(tempSD);
        assertTrue(tempSD instanceof OSDReal);
        assertTrue(tempSD.getType() == OSDType.Real);
        tempReal = (OSDReal)tempSD;
        
        assertEquals(0.9878624d, tempReal.AsReal());
        //TODO - figure out any relevant rounding variability for 64 bit reals
        tempSD = map.get("sim fps");
        assertNotNull(tempSD);
        assertTrue(tempSD instanceof OSDReal);
        assertTrue(tempSD.getType() == OSDType.Real);
        tempReal = (OSDReal)tempSD;
        assertEquals(44.38898d, tempReal.AsReal());

        tempSD = map.get("agent updates per second");
        assertNotNull(tempSD);
        assertTrue(tempSD instanceof OSDReal);
        assertTrue(tempSD.getType() == OSDType.Real);
        tempReal = (OSDReal)tempSD;
        assertEquals(Double.NaN, tempSD.AsReal());

        tempSD = map.get("total task count");
        assertNotNull(tempSD);
        assertTrue(tempSD instanceof OSDReal);
        assertTrue(tempSD.getType() == OSDType.Real);
        tempReal = (OSDReal)tempSD;
        assertEquals(4.0d, tempReal.AsReal());

        tempSD = map.get("active task count");
        assertNotNull(tempSD);
        assertTrue(tempSD instanceof OSDReal);
        assertTrue(tempSD.getType() == OSDType.Real);
        tempReal = (OSDReal)tempSD;
        assertEquals(0.0d, tempReal.AsReal());

        tempSD = map.get("pending uploads");
        assertNotNull(tempSD);
        assertTrue(tempSD instanceof OSDReal);
        assertTrue(tempSD.getType() == OSDType.Real);
        tempReal = (OSDReal)tempSD;
        assertEquals(0.0001096525d, tempReal.AsReal());
    }

    /// Test that various Real representations are parsed correctly.
    public void testDeserializeReals() throws IOException, ParseException
    {
        OSD theSD = null;
        OSDArray array = null;
        OSDReal tempReal = null;

        String testSD = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<llsd>" +
        "    <array>" +
        "        <real>44.38898</real>" +
	    "        <real>nan</real>" +
	    "        <real>4</real>" +
        "        <real>-13.333</real>" +
        "        <real/>" +
        "    </array>" +
        "</llsd>";
        //Deserialize the string
        byte[] bytes = testSD.getBytes(Helpers.UTF8_ENCODING);
        theSD = OSDParser.deserialize(bytes);

        assertTrue(theSD instanceof OSDArray);
        array = (OSDArray)theSD;

        assertEquals(OSDType.Real, array.get(0).getType());
        tempReal = (OSDReal)array.get(0);
        assertEquals(44.38898d, tempReal.AsReal());

        assertEquals(OSDType.Real, array.get(1).getType());
        tempReal = (OSDReal)array.get(1);
        assertEquals(Double.NaN, tempReal.AsReal());

        assertEquals(OSDType.Real, array.get(2).getType());
        tempReal = (OSDReal)array.get(2);
        assertEquals(4.0d, tempReal.AsReal());

        assertEquals(OSDType.Real, array.get(3).getType());
        tempReal = (OSDReal)array.get(3);
        assertEquals(-13.333d, tempReal.AsReal());

        assertEquals(OSDType.Real, array.get(4).getType());
        tempReal = (OSDReal)array.get(4);
        assertEquals(0d, tempReal.AsReal());
    }

    /// Test that various String representations are parsed correctly.
    public void testDeserializeStrings() throws IOException, ParseException
    {
        OSD theSD = null;
        OSDArray array = null;
        OSDString tempStr = null;

        String testSD = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<llsd>" +
        "    <array>" +
        "        <string>Kissling</string>" +
        "        <string>Attack ships on fire off the shoulder of Orion</string>" +
        "        <string>&lt; &gt; &amp; &apos; &quot;</string>" +
        "        <string/>" +
        "    </array>" +
        "</llsd>";
        //Deserialize the string
        byte[] bytes = testSD.getBytes(Helpers.UTF8_ENCODING);
        theSD = OSDParser.deserialize(bytes);

        assertTrue(theSD instanceof OSDArray);
        array = (OSDArray)theSD;

        assertEquals(OSDType.String, array.get(0).getType());
        tempStr = (OSDString)array.get(0);
        assertEquals("Kissling", tempStr.AsString());

        assertEquals(OSDType.String, array.get(1).getType());
        tempStr = (OSDString)array.get(1);
        assertEquals("Attack ships on fire off the shoulder of Orion", tempStr.AsString());

        assertEquals(OSDType.String, array.get(2).getType());
        tempStr = (OSDString)array.get(2);
        assertEquals("< > & \' \"", tempStr.AsString());

        assertEquals(OSDType.String, array.get(3).getType());
        tempStr = (OSDString)array.get(3);
        assertEquals("", tempStr.AsString());
    }

    /// Test that various Integer representations are parsed correctly.
    /// These tests currently only test for values within the range of a
    /// 32 bit signed integer, even though the SD specification says
    /// the type is a 64 bit signed integer, because LLSInteger is currently
    /// implemented using int, a.k.a. Int32.  Not testing Int64 range until
    /// it's understood if there was a design reason for the Int32.
    public void testDeserializeIntegers() throws IOException, ParseException
    {
        OSD theSD = null;
        OSDArray array = null;
        OSDInteger tempInt = null;

        String testSD = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<llsd>" +
        "    <array>" +
        "        <integer>2147483647</integer>" +
	    "        <integer>-2147483648</integer>" +
	    "        <integer>0</integer>" +
        "        <integer>013</integer>" +
        "        <integer/>" +
        "    </array>" +
        "</llsd>";
        //Deserialize the string
        byte[] bytes = testSD.getBytes(Helpers.UTF8_ENCODING);
        theSD = OSDParser.deserialize(bytes);

        assertTrue(theSD instanceof OSDArray);
        array = (OSDArray)theSD;

        assertEquals(OSDType.Integer, array.get(0).getType());
        tempInt = (OSDInteger)array.get(0);
        assertEquals(2147483647, tempInt.AsInteger());

        assertEquals(OSDType.Integer, array.get(1).getType());
        tempInt = (OSDInteger)array.get(1);
        assertEquals(-2147483648, tempInt.AsInteger());

        assertEquals(OSDType.Integer, array.get(2).getType());
        tempInt = (OSDInteger)array.get(2);
        assertEquals(0, tempInt.AsInteger());

        assertEquals(OSDType.Integer, array.get(3).getType());
        tempInt = (OSDInteger)array.get(3);
        assertEquals(13, tempInt.AsInteger());

        assertEquals(OSDType.Integer, array.get(4).getType());
        tempInt = (OSDInteger)array.get(4);
        assertEquals(0, tempInt.AsInteger());
    }

    /// Test that various UUID representations are parsed correctly.
    public void testDeserializeUUID() throws IOException, ParseException
    {
        OSD theSD = null;
        OSDArray array = null;
        OSDUUID tempUUID = null;

        String testSD = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<llsd>" +
        "    <array>" +
        "        <uuid>d7f4aeca-88f1-42a1-b385-b9db18abb255</uuid>" +
        "        <uuid/>" +
        "    </array>" +
        "</llsd>";
        //Deserialize the string
        byte[] bytes = testSD.getBytes(Helpers.UTF8_ENCODING);
        theSD = OSDParser.deserialize(bytes);

        assertTrue(theSD instanceof OSDArray);
        array = (OSDArray)theSD;

        assertEquals(OSDType.UUID, array.get(0).getType());
        tempUUID = (OSDUUID)array.get(0);
        assertEquals(new UUID("d7f4aeca-88f1-42a1-b385-b9db18abb255"), tempUUID.AsUUID());

        assertEquals(OSDType.UUID, array.get(1).getType());
        tempUUID = (OSDUUID)array.get(1);
        assertEquals(UUID.Zero, tempUUID.AsUUID());
    }

    /// Test that various date representations are parsed correctly.
    public void testDeserializeDates() throws IOException, ParseException
    {
        OSD theSD = null;
        OSDArray array = null;
        OSDDate tempDate = null;
        Calendar dt = Calendar.getInstance(TimeZone.getTimeZone(Helpers.UTF8_ENCODING));

        String testSD = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<llsd>" +
        "    <array>" +
        "        <date>2006-02-01T14:29:53Z</date>" +
        "        <date>1999-01-01T00:00:00Z</date>" +
        "        <date/>" +
        "    </array>" +
        "</llsd>";
        //Deserialize the string
        byte[] bytes = testSD.getBytes(Helpers.UTF8_ENCODING);
        theSD = OSDParser.deserialize(bytes);

        assertTrue(theSD instanceof OSDArray);
        array = (OSDArray)theSD;

        assertEquals(OSDType.Date, array.get(0).getType());
        tempDate = (OSDDate)array.get(0);
        dt.clear();
        dt.set(2006, 2 - 1, 1, 14, 29, 53);
        assertEquals(dt.getTime(), tempDate.AsDate());

        assertEquals(OSDType.Date, array.get(1).getType());
        tempDate = (OSDDate)array.get(1);
        dt.clear();
        dt.set(1999, 1 - 1, 1, 0, 0, 0);
        assertEquals(dt.getTime(), tempDate.AsDate());

        assertEquals(OSDType.Date, array.get(2).getType());
        tempDate = (OSDDate)array.get(2);
        assertEquals(Helpers.Epoch, tempDate.AsDate());
    }

    /// Test that various Boolean representations are parsed correctly.
    public void testDeserializeBoolean() throws IOException, ParseException
    {
        OSD theSD = null;
        OSDArray array = null;
        OSDBoolean tempBool = null;
        OSDString tempStr = null;

        String testSD = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<llsd>" +
        "    <array>" +
        "        <boolean>1</boolean>" +
        "        <boolean>true</boolean>" +
        "        <boolean>0</boolean>" +
        "        <boolean>false</boolean>" +
        "        <boolean/>" +
        "        <string>true</string>" +
        "        <string>false</string>" +
        "    </array>" +
        "</llsd>";
        //Deserialize the string
        byte[] bytes = testSD.getBytes(Helpers.UTF8_ENCODING);
        theSD = OSDParser.deserialize(bytes);

        assertTrue(theSD instanceof OSDArray);
        array = (OSDArray)theSD;

        assertEquals(OSDType.Boolean, array.get(0).getType());
        tempBool = (OSDBoolean)array.get(0);
        assertEquals(true, tempBool.AsBoolean());

        assertEquals(OSDType.Boolean, array.get(1).getType());
        tempBool = (OSDBoolean)array.get(1);
        assertEquals(true, tempBool.AsBoolean());

        assertEquals(OSDType.Boolean, array.get(2).getType());
        tempBool = (OSDBoolean)array.get(2);
        assertEquals(false, tempBool.AsBoolean());

        assertEquals(OSDType.Boolean, array.get(3).getType());
        tempBool = (OSDBoolean)array.get(3);
        assertEquals(false, tempBool.AsBoolean());

        assertEquals(OSDType.Boolean, array.get(4).getType());
        tempBool = (OSDBoolean)array.get(4);
        assertEquals(false, tempBool.AsBoolean());

        assertEquals(OSDType.String, array.get(5).getType());
        tempStr = (OSDString)array.get(5);
        assertEquals(true, tempStr.AsBoolean());

        assertEquals(OSDType.String, array.get(6).getType());
        tempStr = (OSDString)array.get(6);
        assertEquals(false, tempStr.AsBoolean());
    }

    byte[] binary = {116, 104, 101, 32, 113, 117, 105, 99, 107, 32, 98, 
                     114, 111, 119, 110, 32, 102, 111, 120, 91, 20, 18,
                     116, 104, 101, 32, 113, 117, 105, 99, 107, 32, 98, 
                     114, 111, 119, 110, 32, 102, 111, 120, 91, 0, 18,
                     116, 104, 101, 32, 113, 117, 105, 99, 107, 32, 98, 
                     114, 111, 119, 110, 32, 102, 111, 120, 91, 3, 18,
                     116, 104, 101, 32, 113, 117, 105, 99, 107, 32, 98, 
                     114, 111, 119, 110, 32, 102, 111, 120, 91, 5, 18};
            
    /// Test that binary elements are parsed correctly.
    public void testDeserializeBinary() throws IOException, ParseException
    {
        OSD theSD = null;
        OSDArray array = null;
        OSDBinary tempBinary = null;

        String testSD = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<llsd>" +
        "    <array>" +
        "        <binary encoding='base64'>cmFuZG9t</binary>" +
        "        <binary>dGhlIHF1aWNrIGJyb3duIGZveA==</binary>" +
        "        <binary/>" +
        "    </array>" +
        "</llsd>";

        //Deserialize the string
        byte[] bytes = testSD.getBytes(Helpers.UTF8_ENCODING);
        theSD = OSDParser.deserialize(bytes);

        assertTrue(theSD instanceof OSDArray);
        array = (OSDArray)theSD;

        assertEquals(OSDType.Binary, array.get(0).getType());
        tempBinary = (OSDBinary)array.get(0);
        byte[] testData1 = {114, 97, 110, 100, 111, 109};
        assertTrue(Arrays.equals(tempBinary.AsBinary(), testData1));

        assertEquals(OSDType.Binary, array.get(1).getType());
        tempBinary = (OSDBinary)array.get(1);
        byte[] testData2 = {116, 104, 101, 32, 113, 117, 105, 99, 107, 32, 98, 
                            114, 111, 119, 110, 32, 102, 111, 120};
        assertTrue(Arrays.equals(tempBinary.AsBinary(), testData2));

        assertEquals(OSDType.Binary, array.get(1).getType());
        tempBinary = (OSDBinary)array.get(2);
        assertEquals(0, tempBinary.AsBinary().length);
    }

    public void testSerializeBinary() throws IOException, ParseException
    {
       OSD llsdBinary = OSD.FromBinary(binary);
       String sBinarySerialized = OSDParser.serializeToString(llsdBinary, OSDFormat.Xml);
       OSD llsdBinaryDS = OSDParser.deserialize(sBinarySerialized);
       assertEquals(OSDType.Binary, llsdBinaryDS.getType());
       assertTrue(Arrays.equals(binary, llsdBinaryDS.AsBinary()));
   }

    /// Test that undefened elements are parsed correctly.
    /// Currently this just checks that there is no error since undefined has no
    /// value and there is no SD child class for Undefined elements - the
    /// current implementation generates an instance of SD
    public void testDeserializeUndef() throws IOException, ParseException
    {
        OSD theSD = null;

        String testSD = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<llsd>" +
        "    <undef/>" +
        "</llsd>";
        //Deserialize the string
        byte[] bytes = testSD.getBytes(Helpers.UTF8_ENCODING);
        theSD = OSDParser.deserialize(bytes);

        assertTrue(theSD instanceof OSD);
    }

    /// Test that various URI representations are parsed correctly.
    public void testDeserializeURI() throws IOException, ParseException, URISyntaxException
    {
        OSD theSD = null;
        OSDArray array = null;
        OSDUri tempURI = null;

        String testSD = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<llsd>" +
        "    <array>" +
        "        <uri>http://sim956.agni.lindenlab.com:12035/runtime/agents</uri>" +
        "        <uri/>" +
        "    </array>" +
        "</llsd>";
        //Deserialize the string
        byte[] bytes = testSD.getBytes(Helpers.UTF8_ENCODING);
        theSD = OSDParser.deserialize(bytes);

        assertTrue(theSD instanceof OSDArray);
        array = (OSDArray)theSD;

        assertEquals(OSDType.URI, array.get(0).getType());
        tempURI = (OSDUri)array.get(0);
        URI testURI = new URI("http://sim956.agni.lindenlab.com:12035/runtime/agents");
        assertEquals(testURI, tempURI.AsUri());

        assertEquals(OSDType.URI, array.get(1).getType());
        tempURI = (OSDUri)array.get(1);
        assertEquals("", tempURI.AsUri().toString());
    }

    /// Test some nested containers.  This is not a very deep or complicated SD graph
    /// but it should reveal basic nesting issues.
    public void testDeserializeNestedContainers() throws IOException, ParseException
    {
        OSD theSD = null;
        OSDArray array = null;
        OSDMap map = null;
        OSD tempSD = null;

        String testSD = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<llsd>" +
        "    <array>" +
        "        <map>" +
        "            <key>Map One</key>" +
        "            <map>" +
        "                <key>Array One</key>" +
        "                <array>" +
        "                    <integer>1</integer>" +
        "                    <integer>2</integer>" +
        "                </array>" +
        "            </map>" +
        "        </map>" +
        "        <array>" +
        "            <string>A</string>" +
        "            <string>B</string>" +
        "            <array>" +
        "                <integer>1</integer>" +
        "                <integer>4</integer>" +
        "                <integer>9</integer>" +
        "            </array>" +
        "        </array>" +
        "    </array>" +
        "</llsd>";
        //Deserialize the string
        byte[] bytes = testSD.getBytes(Helpers.UTF8_ENCODING);
        theSD = OSDParser.deserialize(bytes);

        assertTrue(theSD instanceof OSDArray);
        array = (OSDArray)theSD;
        assertEquals(2, array.size());

        //The first element of top level array, a map
        assertEquals(OSDType.Map, array.get(0).getType());
        map = (OSDMap)array.get(0);
        //First nested map
        tempSD = map.get("Map One");
        assertNotNull(tempSD);
        assertEquals(OSDType.Map, tempSD.getType());
        map = (OSDMap)tempSD;
        //First nested array
        tempSD = map.get("Array One");
        assertNotNull(tempSD);
        assertEquals(OSDType.Array, tempSD.getType());
        array = (OSDArray)tempSD;
        assertEquals(2, array.size());

        array = (OSDArray)theSD;
        //Second element of top level array, an array
        tempSD = array.get(1);
        assertEquals(OSDType.Array, tempSD.getType());
        array = (OSDArray)tempSD;
        assertEquals(3, array.size());
        //Nested array
        tempSD = array.get(2);
        assertEquals(OSDType.Array, tempSD.getType());
        array = (OSDArray)tempSD;
        assertEquals(3, array.size());
    }
}
