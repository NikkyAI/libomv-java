/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2012-2017, Frederick Martian
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
package libomv.structuredData;

/* 
 * This tests are based upon the description at
 * 
 * http://wiki.secondlife.com/wiki/LLSD
 * 
 * and (partially) generated by the (supposed) reference implementation at
 * 
 * http://svn.secondlife.com/svn/linden/release/indra/lib/python/indra/base/llsd.py
 */

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import junit.framework.TestCase;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDInteger;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.StructuredData.LLSD.LLSDNotation;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class NotationLLSDTests extends TestCase
{
    public void testDeserializeUndef() throws IOException, ParseException
    {
        String s = "!";
        OSD llsd = OSDParser.deserialize(s, OSDFormat.Notation);
        assertEquals(OSDType.Unknown, llsd.getType());
    }

    public void testSerializeUndef() throws IOException, ParseException
    {
        OSD llsd = new OSD();
        String s = OSDParser.serializeToString(llsd, OSDFormat.Notation);

        OSD llsdDS = OSDParser.deserialize(s);
        assertEquals(OSDType.Unknown, llsdDS.getType());
    }

    public void testDeserializeBoolean() throws IOException, ParseException
    {
        String t = "true";
        OSD llsdT = OSDParser.deserialize(t, OSDFormat.Notation);
        assertEquals(OSDType.Boolean, llsdT.getType());
        assertEquals(true, llsdT.AsBoolean());

        String tTwo = "t";
        OSD llsdTTwo = OSDParser.deserialize(tTwo, OSDFormat.Notation);
        assertEquals(OSDType.Boolean, llsdTTwo.getType());
        assertEquals(true, llsdTTwo.AsBoolean());

        String tThree = "TRUE";
        OSD llsdTThree = OSDParser.deserialize(tThree, OSDFormat.Notation);
        assertEquals(OSDType.Boolean, llsdTThree.getType());
        assertEquals(true, llsdTThree.AsBoolean());

        String tFour = "T";
        OSD llsdTFour = OSDParser.deserialize(tFour, OSDFormat.Notation);
        assertEquals(OSDType.Boolean, llsdTFour.getType());
        assertEquals(true, llsdTFour.AsBoolean());

        String tFive = "1";
        OSD llsdTFive = OSDParser.deserialize(tFive, OSDFormat.Notation);
        assertEquals(OSDType.Boolean, llsdTFive.getType());
        assertEquals(true, llsdTFive.AsBoolean());

        String f = "false";
        OSD llsdF = OSDParser.deserialize(f, OSDFormat.Notation);
        assertEquals(OSDType.Boolean, llsdF.getType());
        assertEquals(false, llsdF.AsBoolean());

        String fTwo = "f";
        OSD llsdFTwo = OSDParser.deserialize(fTwo, OSDFormat.Notation);
        assertEquals(OSDType.Boolean, llsdFTwo.getType());
        assertEquals(false, llsdFTwo.AsBoolean());

        String fThree = "FALSE";
        OSD llsdFThree = OSDParser.deserialize(fThree, OSDFormat.Notation);
        assertEquals(OSDType.Boolean, llsdFThree.getType());
        assertEquals(false, llsdFThree.AsBoolean());

        String fFour = "F";
        OSD llsdFFour = OSDParser.deserialize(fFour, OSDFormat.Notation);
        assertEquals(OSDType.Boolean, llsdFFour.getType());
        assertEquals(false, llsdFFour.AsBoolean());

        String fFive = "0";
        OSD llsdFFive = OSDParser.deserialize(fFive, OSDFormat.Notation);
        assertEquals(OSDType.Boolean, llsdFFive.getType());
        assertEquals(false, llsdFFive.AsBoolean());
    }

    public void testSerializeBoolean() throws IOException, ParseException
    {
        OSD llsdTrue = OSD.FromBoolean(true);
        String sTrue = OSDParser.serializeToString(llsdTrue, OSDFormat.Notation);
        OSD llsdTrueDS = OSDParser.deserialize(sTrue);
        assertEquals(OSDType.Boolean, llsdTrueDS.getType());
        assertEquals(true, llsdTrueDS.AsBoolean());

        OSD llsdFalse = OSD.FromBoolean(false);
        String sFalse = OSDParser.serializeToString(llsdFalse, OSDFormat.Notation);
        OSD llsdFalseDS = OSDParser.deserialize(sFalse);
        assertEquals(OSDType.Boolean, llsdFalseDS.getType());
        assertEquals(false, llsdFalseDS.AsBoolean());
    }

    public void testDeserializeInteger() throws IOException, ParseException
    {
        String integerOne = "i12319423";
        OSD llsdOne = OSDParser.deserialize(integerOne, OSDFormat.Notation);
        assertEquals(OSDType.Integer, llsdOne.getType());
        assertEquals(12319423, llsdOne.AsInteger());

        String integerTwo = "i-489234";
        OSD llsdTwo = OSDParser.deserialize(integerTwo, OSDFormat.Notation);
        assertEquals(OSDType.Integer, llsdTwo.getType());
        assertEquals(-489234, llsdTwo.AsInteger());
    }

    public void testSerializeInteger() throws IOException, ParseException
    {
        OSD llsdOne = OSD.FromInteger(12319423);
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Notation);
        OSD llsdOneDS = OSDParser.deserialize(sOne);
        assertEquals(OSDType.Integer, llsdOneDS.getType());
        assertEquals(12319423, llsdOne.AsInteger());

        OSD llsdTwo = OSD.FromInteger(-71892034);
        String sTwo = OSDParser.serializeToString(llsdTwo, OSDFormat.Notation);
        OSD llsdTwoDS = OSDParser.deserialize(sTwo);
        assertEquals(OSDType.Integer, llsdTwoDS.getType());
        assertEquals(-71892034, llsdTwoDS.AsInteger());
    }

    public void testDeserializeReal() throws IOException, ParseException
    {
        String realOne = "r1123412345.465711";
        OSD llsdOne = OSDParser.deserialize(realOne, OSDFormat.Notation);
        assertEquals(OSDType.Real, llsdOne.getType());
        assertEquals(1123412345.465711d, llsdOne.AsReal());

        String realTwo = "r-11234684.923411";
        OSD llsdTwo = OSDParser.deserialize(realTwo, OSDFormat.Notation);
        assertEquals(OSDType.Real, llsdTwo.getType());
        assertEquals(-11234684.923411d, llsdTwo.AsReal());

        String realThree = "r1";
        OSD llsdThree = OSDParser.deserialize(realThree, OSDFormat.Notation);
        assertEquals(OSDType.Real, llsdThree.getType());
        assertEquals(1d, llsdThree.AsReal());

        String realFour = "r2.0193899999999998204e-06";
        OSD llsdFour = OSDParser.deserialize(realFour, OSDFormat.Notation);
        assertEquals(OSDType.Real, llsdFour.getType());
        assertEquals(2.0193899999999998204e-06d, llsdFour.AsReal());

        String realFive = "r0";
        OSD llsdFive = OSDParser.deserialize(realFive, OSDFormat.Notation);
        assertEquals(OSDType.Real, llsdFive.getType());
        assertEquals(0d, llsdFive.AsReal());
    }

    public void testSerializeReal() throws IOException, ParseException
    {
        OSD llsdOne = OSD.FromReal(12987234.723847d);
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Notation);
        OSD llsdOneDS = OSDParser.deserialize(sOne);
        assertEquals(OSDType.Real, llsdOneDS.getType());
        assertEquals(12987234.723847d, llsdOneDS.AsReal());

        OSD llsdTwo = OSD.FromReal(-32347892.234234d);
        String sTwo = OSDParser.serializeToString(llsdTwo, OSDFormat.Notation);
        OSD llsdTwoDS = OSDParser.deserialize(sTwo);
        assertEquals(OSDType.Real, llsdTwoDS.getType());
        assertEquals(-32347892.234234d, llsdTwoDS.AsReal());

        OSD llsdThree = OSD.FromReal(Double.MAX_VALUE);
        String sThree = OSDParser.serializeToString(llsdThree, OSDFormat.Notation);
        OSD llsdThreeDS = OSDParser.deserialize( sThree );
        assertEquals( OSDType.Real, llsdThreeDS.getType() );
        assertEquals( Double.MAX_VALUE, llsdThreeDS.AsReal());
    
        OSD llsdFour = OSD.FromReal(Double.MIN_VALUE);
        String sFour = OSDParser.serializeToString(llsdFour, OSDFormat.Notation);
        OSD llsdFourDS = OSDParser.deserialize(sFour);
        assertEquals(OSDType.Real, llsdFourDS.getType());
        assertEquals(Double.MIN_VALUE, llsdFourDS.AsReal());

        OSD llsdFive = OSD.FromReal(-1.1123123E+50d);
        String sFive = OSDParser.serializeToString(llsdFive, OSDFormat.Notation);
        OSD llsdFiveDS = OSDParser.deserialize(sFive);
        assertEquals(OSDType.Real, llsdFiveDS.getType());
        assertEquals(-1.1123123E+50d, llsdFiveDS.AsReal());

        OSD llsdSix = OSD.FromReal(2.0193899999999998204e-06);
        String sSix = OSDParser.serializeToString(llsdSix, OSDFormat.Notation);
        OSD llsdSixDS = OSDParser.deserialize(sSix);
        assertEquals(OSDType.Real, llsdSixDS.getType());
        assertEquals(2.0193899999999998204e-06, llsdSixDS.AsReal());
    }

    public void testDeserializeUUID() throws IOException, ParseException
    {
        String uuidOne = "u97f4aeca-88a1-42a1-b385-b97b18abb255";
        OSD llsdOne = OSDParser.deserialize(uuidOne, OSDFormat.Notation);
        assertEquals(OSDType.UUID, llsdOne.getType());
        assertEquals("97f4aeca-88a1-42a1-b385-b97b18abb255", llsdOne.AsString());

        String uuidTwo = "u00000000-0000-0000-0000-000000000000";
        OSD llsdTwo = OSDParser.deserialize(uuidTwo, OSDFormat.Notation);
        assertEquals(OSDType.UUID, llsdTwo.getType());
        assertEquals("00000000-0000-0000-0000-000000000000", llsdTwo.AsString());
    }

    public void testSerializeUUID() throws IOException, ParseException
    {
        OSD llsdOne = OSD.FromUUID(new UUID("97f4aeca-88a1-42a1-b385-b97b18abb255"));
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Notation);
        OSD llsdOneDS = OSDParser.deserialize(sOne);
        assertEquals(OSDType.UUID, llsdOneDS.getType());
        assertEquals("97f4aeca-88a1-42a1-b385-b97b18abb255", llsdOneDS.AsString());

        OSD llsdTwo = OSD.FromUUID(new UUID("00000000-0000-0000-0000-000000000000"));
        String sTwo = OSDParser.serializeToString(llsdTwo, OSDFormat.Notation);
        OSD llsdTwoDS = OSDParser.deserialize(sTwo);
        assertEquals(OSDType.UUID, llsdTwoDS.getType());
        assertEquals("00000000-0000-0000-0000-000000000000", llsdTwoDS.AsString());
    }

    public void testDeserializeString() throws IOException, ParseException
    {
        String sOne = "''";
        OSD llsdOne = OSDParser.deserialize(sOne, OSDFormat.Notation);
        assertEquals(OSDType.String, llsdOne.getType());
        assertEquals("", llsdOne.AsString());

        // This is double escaping. Once for the encoding, and once for java.  
        String sTwo = "'test\\'\"test'";
        OSD llsdTwo = OSDParser.deserialize(sTwo, OSDFormat.Notation);
        assertEquals(OSDType.String, llsdTwo.getType());
        assertEquals("test'\"test", llsdTwo.AsString());

        // "test \\lest"
        char[] cThree = { (char)0x27, (char)0x74, (char)0x65, (char)0x73, (char)0x74, (char)0x20, (char)0x5c,
                            (char)0x5c, (char)0x6c, (char)0x65, (char)0x73, (char)0x74, (char)0x27 };
        String sThree = new String(cThree);

        OSD llsdThree = OSDParser.deserialize(sThree, OSDFormat.Notation);
        assertEquals(OSDType.String, llsdThree.getType());
        assertEquals("test \\lest", llsdThree.AsString());

        String sFour = "'aa\t la'";
        OSD llsdFour = OSDParser.deserialize(sFour, OSDFormat.Notation);
        assertEquals(OSDType.String, llsdFour.getType());
        assertEquals("aa\t la", llsdFour.AsString());

        char[] cFive = { (char)0x27, (char)0x5c, (char)0x5c, (char)0x27 };
        String sFive = new String(cFive);
        OSD llsdFive = OSDParser.deserialize(sFive, OSDFormat.Notation);
        assertEquals(OSDType.String, llsdFive.getType());
        assertEquals("\\", llsdFive.AsString());


        String sSix = "s(10)\"1234567890\"";
        OSD llsdSix = OSDParser.deserialize(sSix, OSDFormat.Notation);
        assertEquals(OSDType.String, llsdSix.getType());
        assertEquals("1234567890", llsdSix.AsString());

        String sSeven = "s(5)\"\\\\\\\\\\\"";
        OSD llsdSeven = OSDParser.deserialize(sSeven, OSDFormat.Notation);
        assertEquals(OSDType.String, llsdSeven.getType());
        assertEquals("\\\\\\\\\\", llsdSeven.AsString());

        String sEight = "\"aouAOUhsdjklfghskldjfghqeiurtzwieortzaslxfjkgh\"";
        OSD llsdEight = OSDParser.deserialize(sEight, OSDFormat.Notation);
        assertEquals(OSDType.String, llsdEight.getType());
        assertEquals("aouAOUhsdjklfghskldjfghqeiurtzwieortzaslxfjkgh", llsdEight.AsString());

        String sNine = "\"A" + "\u00ea" + "\u00f1" + "\u00fc" + "C\"";
        byte[] bytes = sNine.getBytes(Helpers.UTF8_ENCODING);
        OSD llsdNine = OSDParser.deserialize(bytes, OSDFormat.Notation);
        assertEquals(OSDType.String, llsdNine.getType());
        assertEquals("A" + "\u00ea" + "\u00f1" + "\u00fc" + "C", llsdNine.AsString());
    }

    static private void DoSomeStringSerializingActionsAndAsserts(String s) throws IOException, ParseException
    {
        OSD llsdOne = OSD.FromString(s);
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Notation);
        OSD llsdOneDS = OSDParser.deserialize(sOne);
        assertEquals(OSDType.String, llsdOne.getType());
        assertEquals(s, llsdOneDS.AsString());
    }

    public void testSerializeString() throws IOException, ParseException, XmlPullParserException
    {
        DoSomeStringSerializingActionsAndAsserts("");

        DoSomeStringSerializingActionsAndAsserts("\\");

        DoSomeStringSerializingActionsAndAsserts("\"\"");

        DoSomeStringSerializingActionsAndAsserts("������-these-should-be-some-german-umlauts");

        DoSomeStringSerializingActionsAndAsserts("\t\n\r");

        DoSomeStringSerializingActionsAndAsserts("asdkjfhaksldjfhalskdjfhaklsjdfhaklsjdhjgzqeuiowrtzserghsldfg" +
                                                  "asdlkfhqeiortzsdkfjghslkdrjtzsoidklghuisoehiguhsierughaishdl" +
                                                  "asdfkjhueiorthsgsdkfughaslkdfjshldkfjghsldkjghsldkfghsdklghs" +
                                                  "wopeighisdjfghklasdfjghsdklfgjhsdklfgjshdlfkgjshdlfkgjshdlfk");

        DoSomeStringSerializingActionsAndAsserts("all is N\"\\'othing and n'oting is all");

        DoSomeStringSerializingActionsAndAsserts("very\"british is this.");

        // We test here also for 4byte characters
        String xml = "<x>&#x10137;</x>";
        Reader reader = new StringReader(xml);
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setInput(reader);
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "x");
        String content = parser.nextText();
        DoSomeStringSerializingActionsAndAsserts(content);
    }

    public void testDeserializeURI() throws IOException, ParseException
    {
        String sUriTwo = "l\"test/test/test?test=1&toast=2#data\"";
        OSD llsdTwo = OSDParser.deserialize(sUriTwo, OSDFormat.Notation);
        assertEquals(OSDType.URI, llsdTwo.getType());
        assertEquals("test/test/test?test=1&toast=2#data", llsdTwo.AsString());

        String sUriOne = "l\"http://username:password@example.com:8042/over/there/index.dtb\"";
        OSD llsdOne = OSDParser.deserialize(sUriOne, OSDFormat.Notation);
        assertEquals(OSDType.URI, llsdOne.getType());
        assertEquals("http://username:password@example.com:8042/over/there/index.dtb", llsdOne.AsString());
    }

    public void testSerializeURI() throws IOException, ParseException, URISyntaxException
    {
        URI uriTwo = new URI("test/test/near/the/end?test=1&toast=2#data");
        OSD llsdTwo = OSD.FromUri(uriTwo);
        String sUriTwo = OSDParser.serializeToString(llsdTwo, OSDFormat.Notation);
        OSD llsdTwoDS = OSDParser.deserialize(sUriTwo);
        assertEquals(OSDType.URI, llsdTwoDS.getType());
        assertEquals(uriTwo, llsdTwoDS.AsUri());

        URI uriOne = new URI("http://username:password@example.com:8042/over/there/index.dtb");
        OSD llsdOne = OSD.FromUri(uriOne);
        String sUriOne = OSDParser.serializeToString(llsdOne, OSDFormat.Notation);
        OSD llsdOneDS = OSDParser.deserialize(sUriOne);
        assertEquals(OSDType.URI, llsdOneDS.getType());
        assertEquals(uriOne, llsdOneDS.AsUri());
    }

    public void testDeserializeDate() throws IOException, ParseException
    {
        Calendar dt = Calendar.getInstance(TimeZone.getTimeZone(Helpers.UTF8_ENCODING));
        dt.clear();
        dt.set(2007, 12 - 1, 31, 20, 49, 10);

        String sDateOne = "d\"2007-12-31T20:49:10Z\"";
        OSD llsdOne = OSDParser.deserialize(sDateOne, OSDFormat.Notation);
        assertEquals(OSDType.Date, llsdOne.getType());
        
        Date dtDS = llsdOne.AsDate();
        assertEquals(dt.getTime(), dtDS);
    }

    public void testSerializeDate() throws IOException, ParseException
    {
        Calendar dtOne = Calendar.getInstance();
        dtOne.clear();
        dtOne.set(2005, 8 - 1, 10, 11, 23, 4);
        OSD llsdOne = OSD.FromDate(dtOne.getTime());
        String sDtOne = OSDParser.serializeToString(llsdOne, OSDFormat.Notation);
        OSD llsdOneDS = OSDParser.deserialize(sDtOne);
        assertEquals(OSDType.Date, llsdOneDS.getType());
        Date dtOneDS = llsdOneDS.AsDate();
        assertEquals(dtOne.getTime(), dtOneDS);

        Calendar dtTwo = Calendar.getInstance();
        dtTwo.clear();
        dtTwo.set(2010, 10 - 1, 11, 23, 0, 10);
        dtTwo.set(Calendar.MILLISECOND, 100);
        OSD llsdTwo = OSD.FromDate(dtTwo.getTime());
        String sDtTwo = OSDParser.serializeToString(llsdTwo, OSDFormat.Notation);
        OSD llsdTwoDS = OSDParser.deserialize(sDtTwo);
        assertEquals(OSDType.Date, llsdTwoDS.getType());
        Date dtTwoDS = llsdTwoDS.AsDate();
        assertEquals(dtTwo.getTime(), dtTwoDS);

        // check if a *local* time can be serialized and deserialized
        Calendar dtThree = Calendar.getInstance();
        dtThree.clear();
        dtThree.set(2009, 12 - 1, 30, 8, 25, 10);
        OSD llsdDateThree = OSD.FromDate(dtThree.getTime());
        String sDateThreeSerialized = OSDParser.serializeToString(llsdDateThree, OSDFormat.Notation);
        OSD llsdDateThreeDS = OSDParser.deserialize(sDateThreeSerialized);
        assertEquals(OSDType.Date, llsdDateThreeDS.getType());
        assertEquals(dtThree.getTime(), llsdDateThreeDS.AsDate());
    }

    byte[] binary = { 0x0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0b,
            0x0b, 0x0c, 0x0d, 0x0e, 0x0f };

    public void testDeserializeBinary() throws IOException, ParseException
    {
    	String sBinarySerialized = "b64\"AAECAwQFBgcICQsLDA0ODw==\"";
        OSD llsdBinaryDS = OSDParser.deserialize(sBinarySerialized, OSDFormat.Notation);
        assertEquals(OSDType.Binary, llsdBinaryDS.getType());
        assertTrue(Arrays.equals(binary, llsdBinaryDS.AsBinary()));
    }
    
    public void testSerializeBinary() throws IOException, ParseException
    {
        OSD llsdBinary = OSD.FromBinary(binary);
        String sBinarySerialized = OSDParser.serializeToString(llsdBinary, OSDFormat.Notation);
        OSD llsdBinaryDS = OSDParser.deserialize(sBinarySerialized);
        assertEquals(OSDType.Binary, llsdBinaryDS.getType());
        assertTrue(Arrays.equals(binary, llsdBinaryDS.AsBinary()));
    }

    public void testDeserializeArray() throws IOException, ParseException
    {
        String sArrayOne = "[]";
        OSDArray llsdArrayOne = (OSDArray)OSDParser.deserialize(sArrayOne, OSDFormat.Notation);
        assertEquals(OSDType.Array, llsdArrayOne.getType());
        assertEquals(0, llsdArrayOne.size());

        String sArrayTwo = "[ i0 ]";
        OSDArray llsdArrayTwo = (OSDArray)OSDParser.deserialize(sArrayTwo, OSDFormat.Notation);
        assertEquals(OSDType.Array, llsdArrayTwo.getType());
        assertEquals(1, llsdArrayTwo.size());
        OSDInteger llsdIntOne = (OSDInteger)llsdArrayTwo.get(0);
        assertEquals(OSDType.Integer, llsdIntOne.getType());
        assertEquals(0, llsdIntOne.AsInteger());

        String sArrayThree = "[ i0, i1 ]";
        OSDArray llsdArrayThree = (OSDArray)OSDParser.deserialize(sArrayThree, OSDFormat.Notation);
        assertEquals(OSDType.Array, llsdArrayThree.getType());
        assertEquals(2, llsdArrayThree.size());
        OSDInteger llsdIntTwo = (OSDInteger)llsdArrayThree.get(0);
        assertEquals(OSDType.Integer, llsdIntTwo.getType());
        assertEquals(0, llsdIntTwo.AsInteger());
        OSDInteger llsdIntThree = (OSDInteger)llsdArrayThree.get(1);
        assertEquals(OSDType.Integer, llsdIntThree.getType());
        assertEquals(1, llsdIntThree.AsInteger());

        String sArrayFour = " [ \"testtest\", \"aha\",t,f,i1, r1.2, [ i1] ] ";
        OSDArray llsdArrayFour = (OSDArray)OSDParser.deserialize(sArrayFour, OSDFormat.Notation);
        assertEquals(OSDType.Array, llsdArrayFour.getType());
        assertEquals(7, llsdArrayFour.size());
        assertEquals("testtest", llsdArrayFour.get(0).AsString());
        assertEquals("aha", llsdArrayFour.get(1).AsString());
        assertEquals(true, llsdArrayFour.get(2).AsBoolean());
        assertEquals(false, llsdArrayFour.get(3).AsBoolean());
        assertEquals(1, llsdArrayFour.get(4).AsInteger());
        assertEquals(1.2d, llsdArrayFour.get(5).AsReal());
        assertEquals(OSDType.Array, llsdArrayFour.get(6).getType());
        OSDArray llsdArrayFive = (OSDArray)llsdArrayFour.get(6);
        assertEquals(1, llsdArrayFive.get(0).AsInteger());
    }

    public void testSerializeArray() throws IOException, ParseException
    {
        OSDArray llsdOne = new OSDArray();
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Notation);
        OSDArray llsdOneDS = (OSDArray)OSDParser.deserialize(sOne);
        assertEquals(OSDType.Array, llsdOneDS.getType());
        assertEquals(0, llsdOneDS.size());

        OSD llsdTwo = OSD.FromInteger(123234);
        OSD llsdThree = OSD.FromString("asedkfjhaqweiurohzasdf");
        OSDArray llsdFour = new OSDArray();
        llsdFour.add(llsdTwo);
        llsdFour.add(llsdThree);

        llsdOne.add(llsdTwo);
        llsdOne.add(llsdThree);
        llsdOne.add(llsdFour);

        String sFive = OSDParser.serializeToString(llsdOne, OSDFormat.Notation);
        OSDArray llsdFive = (OSDArray)OSDParser.deserialize(sFive);
        assertEquals(OSDType.Array, llsdFive.getType());
        assertEquals(3, llsdFive.size());
        assertEquals(OSDType.Integer, llsdFive.get(0).getType());
        assertEquals(123234, llsdFive.get(0).AsInteger());
        assertEquals(OSDType.String, llsdFive.get(1).getType());
        assertEquals("asedkfjhaqweiurohzasdf", llsdFive.get(1).AsString());

        OSDArray llsdSix = (OSDArray)llsdFive.get(2);
        assertEquals(OSDType.Array, llsdSix.getType());
        assertEquals(2, llsdSix.size());
        assertEquals(OSDType.Integer, llsdSix.get(0).getType());
        assertEquals(123234, llsdSix.get(0).AsInteger());
        assertEquals(OSDType.String, llsdSix.get(1).getType());
        assertEquals("asedkfjhaqweiurohzasdf", llsdSix.get(1).AsString());
    }

    public void testDeserializeMap() throws IOException, ParseException
    {
        String sMapOne = " { } ";
        OSDMap llsdMapOne = (OSDMap)OSDParser.deserialize(sMapOne, OSDFormat.Notation);
        assertEquals(OSDType.Map, llsdMapOne.getType());
        assertEquals(0, llsdMapOne.size());

        String sMapTwo = " { \"test\":i2 } ";
        OSDMap llsdMapTwo = (OSDMap)OSDParser.deserialize(sMapTwo, OSDFormat.Notation);
        assertEquals(OSDType.Map, llsdMapTwo.getType());
        assertEquals(1, llsdMapTwo.size());
        assertEquals(OSDType.Integer, llsdMapTwo.get("test").getType());
        assertEquals(2, llsdMapTwo.get("test").AsInteger());

        String sMapThree = " { 'test':\"testtesttest\", 'aha':\"muahahaha\" , \"anywhere\":! } ";
        OSDMap llsdMapThree = (OSDMap)OSDParser.deserialize(sMapThree, OSDFormat.Notation);
        assertEquals(OSDType.Map, llsdMapThree.getType());
        assertEquals(3, llsdMapThree.size());
        assertEquals(OSDType.String, llsdMapThree.get("test").getType());
        assertEquals("testtesttest", llsdMapThree.get("test").AsString());
        assertEquals(OSDType.String, llsdMapThree.get("test").getType());
        assertEquals("muahahaha", llsdMapThree.get("aha").AsString());
        assertEquals(OSDType.Unknown, llsdMapThree.get("self").getType());

        String sMapFour = " { 'test' : { 'test' : i1, 't0st' : r2.5 }, 'tist' : \"hello world!\", 'tast' : \"last\" } ";
        OSDMap llsdMapFour = (OSDMap)OSDParser.deserialize(sMapFour, OSDFormat.Notation);
        assertEquals(OSDType.Map, llsdMapFour.getType());
        assertEquals(3, llsdMapFour.size());
        assertEquals("hello world!", llsdMapFour.get("tist").AsString());
        assertEquals("last", llsdMapFour.get("tast").AsString());
        OSDMap llsdMapFive = (OSDMap)llsdMapFour.get("test");
        assertEquals(OSDType.Map, llsdMapFive.getType());
        assertEquals(2, llsdMapFive.size());
        assertEquals(OSDType.Integer, llsdMapFive.get("test").getType());
        assertEquals(1, llsdMapFive.get("test").AsInteger());
        assertEquals(OSDType.Real, llsdMapFive.get("t0st").getType());
        assertEquals(2.5d, llsdMapFive.get("t0st").AsReal());
    }

    public void testSerializeMap() throws IOException, ParseException, XmlPullParserException
    {
        OSDMap llsdOne = new OSDMap();
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Notation);
        OSDMap llsdOneDS = (OSDMap)OSDParser.deserialize(sOne);
        assertEquals(OSDType.Map, llsdOneDS.getType());
        assertEquals(0, llsdOneDS.size());

        OSD llsdTwo = OSD.FromInteger(123234);
        OSD llsdThree = OSD.FromString("asedkfjhaqweiurohzasdf");
        OSDMap llsdFour = new OSDMap();
        llsdFour.put("test0", llsdTwo);
        llsdFour.put("test1", llsdThree);

        llsdOne.put("test0", llsdTwo);
        llsdOne.put("test1", llsdThree);
        llsdOne.put("test2", llsdFour);

        String sFive = OSDParser.serializeToString(llsdOne, OSDFormat.Notation);
        OSDMap llsdFive = (OSDMap)OSDParser.deserialize(sFive);
        assertEquals(OSDType.Map, llsdFive.getType());
        assertEquals(3, llsdFive.size());
        assertEquals(OSDType.Integer, llsdFive.get("test0").getType());
        assertEquals(123234, llsdFive.get("test0").AsInteger());
        assertEquals(OSDType.String, llsdFive.get("test1").getType());
        assertEquals("asedkfjhaqweiurohzasdf", llsdFive.get("test1").AsString());

        OSDMap llsdSix = (OSDMap)llsdFive.get("test2");
        assertEquals(OSDType.Map, llsdSix.getType());
        assertEquals(2, llsdSix.size());
        assertEquals(OSDType.Integer, llsdSix.get("test0").getType());
        assertEquals(123234, llsdSix.get("test0").AsInteger());
        assertEquals(OSDType.String, llsdSix.get("test1").getType());
        assertEquals("asedkfjhaqweiurohzasdf", llsdSix.get("test1").AsString());

        // We test here also for 4byte characters as map keys
        String xml = "<x>&#x10137;</x>";
        Reader reader = new StringReader(xml);
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setInput(reader);
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "x");
        String content = parser.nextText();

        OSDMap llsdSeven = new OSDMap();
        llsdSeven.put(content, OSD.FromString(content));
        String sSeven = OSDParser.serializeToString(llsdSeven, OSDFormat.Notation);
        OSDMap llsdSevenDS = (OSDMap)OSDParser.deserialize(sSeven);
        assertEquals(OSDType.Map, llsdSevenDS.getType());
        assertEquals(1, llsdSevenDS.size());
        assertEquals(content, llsdSevenDS.get(content).AsString());
    }

    public void testDeserializeRealWorldExamples() throws IOException, ParseException
    {
        String realWorldExample = "[" +
        		"{'destination':'http://secondlife.com'}," + 
        		"{'version':i1}," +
        		"{" +
        		"'agent_id':u3c115e51-04f4-523c-9fa6-98aff1034730," +
        		"'session_id':u2c585cec-038c-40b0-b42e-a25ebab4d132," +
        		"'circuit_code':i1075," +
        		"'first_name':'Phoenix'," +
        		"'last_name':'Linden'," +
        		"'position':[r70.9247,r254.378,r38.7304]," +
        		"'look_at':[r-0.043753,r-0.999042,r0]," +
        		"'granters':[ua2e76fcd-9360-4f6d-a924-000000000003]," +
        		"'attachment_data':" +
        		"[" +
        		"  {" +
        		"    'attachment_point':i2," +
        		"    'item_id':ud6852c11-a74e-309a-0462-50533f1ef9b3," +
        		"    'asset_id':uc69b29b1-8944-58ae-a7c5-2ca7b23e22fb" +
        		"  }," +
        		"  {" +
        		"    'attachment_point':i10," +
        		"    'item_id':uff852c22-a74e-309a-0462-50533f1ef900," +
        		"    'asset_id':u5868dd20-c25a-47bd-8b4c-dedc99ef9479" +
        		"  }" +
        		"]" +
        		"}" +
        		"]";
        // We dont do full testing here. We are fine if a few values are right
        // and the parser doesnt throw an exception
        OSDArray llsdArray = (OSDArray)OSDParser.deserialize(realWorldExample, OSDFormat.Notation);
        assertEquals(OSDType.Array, llsdArray.getType());
        assertEquals(3, llsdArray.size());

        OSDMap llsdMapOne = (OSDMap)llsdArray.get(0);
        assertEquals(OSDType.Map, llsdMapOne.getType());
        assertEquals("http://secondlife.com", llsdMapOne.get("destination").AsString());

        OSDMap llsdMapTwo = (OSDMap)llsdArray.get(1);
        assertEquals(OSDType.Map, llsdMapTwo.getType());
        assertEquals(OSDType.Integer, llsdMapTwo.get("version").getType());
        assertEquals(1, llsdMapTwo.get("version").AsInteger());

        OSDMap llsdMapThree = (OSDMap)llsdArray.get(2);
        assertEquals(OSDType.UUID, llsdMapThree.get("session_id").getType());
        assertEquals("2c585cec-038c-40b0-b42e-a25ebab4d132", llsdMapThree.get("session_id").AsString());
        assertEquals(OSDType.UUID, llsdMapThree.get("agent_id").getType());
        assertEquals("3c115e51-04f4-523c-9fa6-98aff1034730", llsdMapThree.get("agent_id").AsString());

    }

    public void testSerializeFormattedTest() throws IOException, ParseException
    {
        // This is not a real test. Instead look at the console.out tab for how formatted notation looks like.
        OSDArray llsdArray = new OSDArray();
        OSD llsdOne = OSD.FromInteger(1);
        OSD llsdTwo = OSD.FromInteger(1);
        llsdArray.add(llsdOne);
        llsdArray.add(llsdTwo);
                    
        OSDMap llsdMap = new OSDMap();
        OSD llsdThree = OSD.FromInteger(2);
        llsdMap.put("test1", llsdThree);
        OSD llsdFour = OSD.FromInteger(2);
        llsdMap.put("test2", llsdFour);

        llsdArray.add(llsdMap);
        
        OSDArray llsdArrayTwo = new OSDArray();
        OSD llsdFive = OSD.FromString("asdflkhjasdhj");
        OSD llsdSix = OSD.FromString("asdkfhasjkldfghsd");
        llsdArrayTwo.add(llsdFive);
        llsdArrayTwo.add(llsdSix);

        llsdMap.put("test3", llsdArrayTwo);

        String sThree = LLSDNotation.serializeToStringFormatted(llsdArray);

        // we also try to parse this... and look a little at the results 
        OSDArray llsdSeven = (OSDArray)OSDParser.deserialize(sThree, OSDFormat.Notation);
        assertEquals(OSDType.Array, llsdSeven.getType());
        assertEquals(3, llsdSeven.size());
        assertEquals(OSDType.Integer, llsdSeven.get(0).getType());
        assertEquals(1, llsdSeven.get(0).AsInteger());
        assertEquals(OSDType.Integer, llsdSeven.get(1).getType());
        assertEquals(1, llsdSeven.get(1).AsInteger());

        assertEquals(OSDType.Map, llsdSeven.get(2).getType());
        // thats enough for now.            
    }
}
