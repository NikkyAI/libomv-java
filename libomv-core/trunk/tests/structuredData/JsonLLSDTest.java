package structuredData;

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

import junit.framework.Assert;
import junit.framework.TestCase;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDInteger;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class JsonLLSDTest extends TestCase
{
    public void testDeserializeUndef() throws IOException, ParseException
    {
        String s = "null";
        OSD llsd = OSDParser.deserialize(s, OSDFormat.Json);
        Assert.assertEquals(OSDType.Unknown, llsd.getType());
    }

    public void testSerializeUndef() throws IOException, ParseException
    {
        OSD llsd = new OSD();
        String s = OSDParser.serializeToString(llsd, OSDFormat.Json, false);
        OSD llsdDS = OSDParser.deserialize(s);
        Assert.assertEquals(OSDType.Unknown, llsdDS.getType());
    }

    public void testDeserializeBoolean() throws IOException, ParseException
    {
        String t = "true";
        OSD llsdT = OSDParser.deserialize(t, OSDFormat.Json);
        Assert.assertEquals(OSDType.Boolean, llsdT.getType());
        Assert.assertEquals(true, llsdT.AsBoolean());

        String f = "false";
        OSD llsdF = OSDParser.deserialize(f, OSDFormat.Json);
        Assert.assertEquals(OSDType.Boolean, llsdF.getType());
        Assert.assertEquals(false, llsdF.AsBoolean());
    }

    public void testSerializeBoolean() throws IOException, ParseException
    {
        OSD llsdTrue = OSD.FromBoolean(true);
        String sTrue = OSDParser.serializeToString(llsdTrue, OSDFormat.Json, false);
        OSD llsdTrueDS = OSDParser.deserialize(sTrue);
        Assert.assertEquals(OSDType.Boolean, llsdTrueDS.getType());
        Assert.assertEquals(true, llsdTrueDS.AsBoolean());

        OSD llsdFalse = OSD.FromBoolean(false);
        String sFalse = OSDParser.serializeToString(llsdFalse, OSDFormat.Json, false);
        OSD llsdFalseDS = OSDParser.deserialize(sFalse);
        Assert.assertEquals(OSDType.Boolean, llsdFalseDS.getType());
        Assert.assertEquals(false, llsdFalseDS.AsBoolean());
    }

    public void testDeserializeInteger() throws IOException, ParseException
    {
        String integerOne = "12319423";
        OSD llsdOne = OSDParser.deserialize(integerOne, OSDFormat.Json);
        Assert.assertEquals(OSDType.Integer, llsdOne.getType());
        Assert.assertEquals(12319423, llsdOne.AsInteger());

        String integerTwo = "-489234";
        OSD llsdTwo = OSDParser.deserialize(integerTwo, OSDFormat.Json);
        Assert.assertEquals(OSDType.Integer, llsdTwo.getType());
        Assert.assertEquals(-489234, llsdTwo.AsInteger());
    }

    public void testSerializeInteger() throws IOException, ParseException
    {
        OSD llsdOne = OSD.FromInteger(12319423);
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Json);
        OSD llsdOneDS = OSDParser.deserialize(sOne);
        Assert.assertEquals(OSDType.Integer, llsdOneDS.getType());
        Assert.assertEquals(12319423, llsdOne.AsInteger());

        OSD llsdTwo = OSD.FromInteger(-71892034);
        String sTwo = OSDParser.serializeToString(llsdTwo, OSDFormat.Json);
        OSD llsdTwoDS = OSDParser.deserialize(sTwo);
        Assert.assertEquals(OSDType.Integer, llsdTwoDS.getType());
        Assert.assertEquals(-71892034, llsdTwoDS.AsInteger());
    }

    public void testDeserializeReal() throws IOException, ParseException
    {
        String realOne = "1123412345.465711";
        OSD llsdOne = OSDParser.deserialize(realOne, OSDFormat.Json);
        Assert.assertEquals(OSDType.Real, llsdOne.getType());
        Assert.assertEquals(1123412345.465711d, llsdOne.AsReal());

        String realTwo = "-11234684.923411";
        OSD llsdTwo = OSDParser.deserialize(realTwo, OSDFormat.Json);
        Assert.assertEquals(OSDType.Real, llsdTwo.getType());
        Assert.assertEquals(-11234684.923411d, llsdTwo.AsReal());

        String realThree = "1";
        OSD llsdThree = OSDParser.deserialize(realThree, OSDFormat.Json);
        // Assert.assertEquals(OSDType.Real, llsdThree.getType());
        Assert.assertEquals(1d, llsdThree.AsReal());

        String realFour = "2.0193899999999998204e-06";
        OSD llsdFour = OSDParser.deserialize(realFour, OSDFormat.Json);
        Assert.assertEquals(OSDType.Real, llsdFour.getType());
        Assert.assertEquals(2.0193899999999998204e-06d, llsdFour.AsReal());

        String realFive = "0";
        OSD llsdFive = OSDParser.deserialize(realFive, OSDFormat.Json);
        // Assert.assertEquals(OSDType.Real, llsdFive.getType());
        Assert.assertEquals(0d, llsdFive.AsReal());
    }

    public void testSerializeReal() throws IOException, ParseException
    {
        OSD llsdOne = OSD.FromReal(12987234.723847d);
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Json);
        OSD llsdOneDS = OSDParser.deserialize(sOne);
        Assert.assertEquals(OSDType.Real, llsdOneDS.getType());
        Assert.assertEquals(12987234.723847d, llsdOneDS.AsReal());

        OSD llsdTwo = OSD.FromReal(-32347892.234234d);
        String sTwo = OSDParser.serializeToString(llsdTwo, OSDFormat.Json);
        OSD llsdTwoDS = OSDParser.deserialize(sTwo);
        Assert.assertEquals(OSDType.Real, llsdTwoDS.getType());
        Assert.assertEquals(-32347892.234234d, llsdTwoDS.AsReal());

        OSD llsdThree = OSD.FromReal(Double.MAX_VALUE);
        String sThree = OSDParser.serializeToString(llsdThree, OSDFormat.Json);
        OSD llsdThreeDS = OSDParser.deserialize( sThree );
        Assert.assertEquals( OSDType.Real, llsdThreeDS.getType() );
        Assert.assertEquals( Double.MAX_VALUE, llsdThreeDS.AsReal());
    
        OSD llsdFour = OSD.FromReal(Double.MIN_VALUE);
        String sFour = OSDParser.serializeToString(llsdFour, OSDFormat.Json);
        OSD llsdFourDS = OSDParser.deserialize(sFour);
        Assert.assertEquals(OSDType.Real, llsdFourDS.getType());
        Assert.assertEquals(Double.MIN_VALUE, llsdFourDS.AsReal());

        OSD llsdFive = OSD.FromReal(-1.1123123E+50d);
        String sFive = OSDParser.serializeToString(llsdFive, OSDFormat.Json);
        OSD llsdFiveDS = OSDParser.deserialize(sFive);
        Assert.assertEquals(OSDType.Real, llsdFiveDS.getType());
        Assert.assertEquals(-1.1123123E+50d, llsdFiveDS.AsReal());

        OSD llsdSix = OSD.FromReal(2.0193899999999998204e-06);
        String sSix = OSDParser.serializeToString(llsdSix, OSDFormat.Json);
        OSD llsdSixDS = OSDParser.deserialize(sSix);
        Assert.assertEquals(OSDType.Real, llsdSixDS.getType());
        Assert.assertEquals(2.0193899999999998204e-06, llsdSixDS.AsReal());
    }

    public void testDeserializeUUID() throws IOException, ParseException
    {
        String uuidOne = "\"97f4aeca-88a1-42a1-b385-b97b18abb255\"";
        OSD llsdOne = OSDParser.deserialize(uuidOne, OSDFormat.Json);
        Assert.assertEquals(OSDType.UUID, llsdOne.getType());
        Assert.assertEquals("97f4aeca-88a1-42a1-b385-b97b18abb255", llsdOne.AsString());

        String uuidTwo = "\"00000000-0000-0000-0000-000000000000\"";
        OSD llsdTwo = OSDParser.deserialize(uuidTwo, OSDFormat.Json);
        Assert.assertEquals(OSDType.UUID, llsdTwo.getType());
        Assert.assertEquals("00000000-0000-0000-0000-000000000000", llsdTwo.AsString());
    }

    public void testSerializeUUID() throws IOException, ParseException
    {
        OSD llsdOne = OSD.FromUUID(new UUID("97f4aeca-88a1-42a1-b385-b97b18abb255"));
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Json);
        OSD llsdOneDS = OSDParser.deserialize(sOne);
        Assert.assertEquals(OSDType.UUID, llsdOneDS.getType());
        Assert.assertEquals("97f4aeca-88a1-42a1-b385-b97b18abb255", llsdOneDS.AsString());

        OSD llsdTwo = OSD.FromUUID(new UUID("00000000-0000-0000-0000-000000000000"));
        String sTwo = OSDParser.serializeToString(llsdTwo, OSDFormat.Json);
        OSD llsdTwoDS = OSDParser.deserialize(sTwo);
        Assert.assertEquals(OSDType.UUID, llsdTwoDS.getType());
        Assert.assertEquals("00000000-0000-0000-0000-000000000000", llsdTwoDS.AsString());
    }

    public void testDeserializeString() throws IOException, ParseException
    {
        String sOne = "\"\"";
        OSD llsdOne = OSDParser.deserialize(sOne, OSDFormat.Json);
        Assert.assertEquals(OSDType.String, llsdOne.getType());
        Assert.assertEquals("", llsdOne.AsString());

        // This is double escaping. Once for the encoding, and once for Java.  
        String sTwo = "\"test\\\\'\\\"test\"";
        OSD llsdTwo = OSDParser.deserialize(sTwo, OSDFormat.Json);
        Assert.assertEquals(OSDType.String, llsdTwo.getType());
        Assert.assertEquals("test\\'\"test", llsdTwo.AsString());

        // "test \lest"
        char[] cThree = { (char)0x22, (char)0x74, (char)0x65, (char)0x73, (char)0x74, (char)0x20, (char)0x5c,
                            (char)0x5c, (char)0x6c, (char)0x65, (char)0x73, (char)0x74, (char)0x22 };
        String sThree = new String(cThree);

        OSD llsdThree = OSDParser.deserialize(sThree, OSDFormat.Json);
        Assert.assertEquals(OSDType.String, llsdThree.getType());
        Assert.assertEquals("test \\lest", llsdThree.AsString());

        String sFour = "\"aa\t la\"";
        OSD llsdFour = OSDParser.deserialize(sFour, OSDFormat.Json);
        Assert.assertEquals(OSDType.String, llsdFour.getType());
        Assert.assertEquals("aa\t la", llsdFour.AsString());

        char[] cFive = { (char)0x22, (char)0x5c, (char)0x5c, (char)0x22 };
        String sFive = new String(cFive);
        OSD llsdFive = OSDParser.deserialize(sFive, OSDFormat.Json);
        Assert.assertEquals(OSDType.String, llsdFive.getType());
        Assert.assertEquals("\\", llsdFive.AsString());


        String sSix = "\"1234567890\"";
        OSD llsdSix = OSDParser.deserialize(sSix, OSDFormat.Json);
        Assert.assertEquals(OSDType.String, llsdSix.getType());
        Assert.assertEquals("1234567890", llsdSix.AsString());

        String sSeven = "\"\\\\ \\\\ \\\\\"";
        OSD llsdSeven = OSDParser.deserialize(sSeven, OSDFormat.Json);
        Assert.assertEquals(OSDType.String, llsdSeven.getType());
        Assert.assertEquals("\\ \\ \\", llsdSeven.AsString());

        String sEight = "\"aouAOUhsdjklfghskldjfghqeiurtzwieortzaslxfjkgh\"";
        OSD llsdEight = OSDParser.deserialize(sEight, OSDFormat.Json);
        Assert.assertEquals(OSDType.String, llsdEight.getType());
        Assert.assertEquals("aouAOUhsdjklfghskldjfghqeiurtzwieortzaslxfjkgh", llsdEight.AsString());
    }

    private void DoSomeStringSerializingActionsAndAsserts(String s) throws IOException, ParseException
    {
        OSD llsdOne = OSD.FromString(s);
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Json);
        OSD llsdOneDS = OSDParser.deserialize(sOne);
        Assert.assertEquals(OSDType.String, llsdOne.getType());
        Assert.assertEquals(s, llsdOneDS.AsString());
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
        String sUriTwo = "\"test/test/test?test=1&toast=2#data\"";
        OSD llsdTwo = OSDParser.deserialize(sUriTwo, OSDFormat.Json);
        // Assert.assertEquals(OSDType.URI, llsdTwo.getType());
        Assert.assertEquals("test/test/test?test=1&toast=2#data", llsdTwo.AsString());

        String sUriOne = "\"http://username:password@example.com:8042/over/there/index.dtb\"";
        OSD llsdOne = OSDParser.deserialize(sUriOne, OSDFormat.Json);
        // Assert.assertEquals(OSDType.URI, llsdOne.getType());
        Assert.assertEquals("http://username:password@example.com:8042/over/there/index.dtb", llsdOne.AsString());
    }

    public void testSerializeURI() throws IOException, ParseException, URISyntaxException
    {
        URI uriTwo = new URI("test/test/near/the/end?test=1&toast=2#data");
        OSD llsdTwo = OSD.FromUri(uriTwo);
        String sUriTwo = OSDParser.serializeToString(llsdTwo, OSDFormat.Json);
        OSD llsdTwoDS = OSDParser.deserialize(sUriTwo);
        // Json does serialize uri data to a string
        Assert.assertEquals(OSDType.String, llsdTwoDS.getType());
        Assert.assertEquals(uriTwo, llsdTwoDS.AsUri());

        URI uriOne = new URI("http://username:password@example.com:8042/over/there/index.dtb");
        OSD llsdOne = OSD.FromUri(uriOne);
        String sUriOne = OSDParser.serializeToString(llsdOne, OSDFormat.Json);
        OSD llsdOneDS = OSDParser.deserialize(sUriOne);
        // Json does serialize uri data to a string
        Assert.assertEquals(OSDType.String, llsdOneDS.getType());
        Assert.assertEquals(uriOne, llsdOneDS.AsUri());
    }

    public void testDeserializeDate() throws IOException, ParseException
    {
        Calendar dt = Calendar.getInstance(TimeZone.getTimeZone(Helpers.UTF8_ENCODING));
        dt.clear();
        dt.set(2007, 12 - 1, 31, 20, 49, 10);

        String sDateOne = "\"2007-12-31T20:49:10Z\"";
        OSD llsdOne = OSDParser.deserialize(sDateOne, OSDFormat.Json);
        Assert.assertEquals(OSDType.Date, llsdOne.getType());
        
        Date dtDS = llsdOne.AsDate();
        Assert.assertEquals(dt.getTime(), dtDS);
    }

    public void testSerializeDate() throws IOException, ParseException
    {
        Calendar dtOne = Calendar.getInstance();
        dtOne.clear();
        dtOne.set(2005, 8 - 1, 10, 11, 23, 4);
        OSD llsdOne = OSD.FromDate(dtOne.getTime());
        String sDtOne = OSDParser.serializeToString(llsdOne, OSDFormat.Json);
        OSD llsdOneDS = OSDParser.deserialize(sDtOne);
        Assert.assertEquals(OSDType.Date, llsdOneDS.getType());
        Date dtOneDS = llsdOneDS.AsDate();
        Assert.assertEquals(dtOne.getTime(), dtOneDS);

        Calendar dtTwo = Calendar.getInstance();
        dtTwo.clear();
        dtTwo.set(2010, 10 - 1, 11, 23, 0, 10);
        dtTwo.set(Calendar.MILLISECOND, 100);
        OSD llsdTwo = OSD.FromDate(dtTwo.getTime());
        String sDtTwo = OSDParser.serializeToString(llsdTwo, OSDFormat.Json);
        OSD llsdTwoDS = OSDParser.deserialize(sDtTwo);
        Assert.assertEquals(OSDType.Date, llsdTwoDS.getType());
        Date dtTwoDS = llsdTwoDS.AsDate();
        Assert.assertEquals(dtTwo.getTime(), dtTwoDS);

        // check if a *local* time can be serialized and deserialized
        Calendar dtThree = Calendar.getInstance();
        dtThree.clear();
        dtThree.set(2009, 12 - 1, 30, 8, 25, 10);
        OSD llsdDateThree = OSD.FromDate(dtThree.getTime());
        String sDateThreeSerialized = OSDParser.serializeToString(llsdDateThree, OSDFormat.Json);
        OSD llsdDateThreeDS = OSDParser.deserialize(sDateThreeSerialized);
        Assert.assertEquals(OSDType.Date, llsdDateThreeDS.getType());
        Assert.assertEquals(dtThree.getTime(), llsdDateThreeDS.AsDate());
    }

    byte[] binary = { 0x0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a,
            0x0b, 0x0c, 0x0d, 0x0e, 0x0f };

    public void testDeserializeBinary() throws IOException, ParseException
    {
    	String sBinarySerialized = "[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15]";
        OSD llsdBinaryDS = OSDParser.deserialize(sBinarySerialized, OSDFormat.Json);
        // Json does serialize binary data to an array of numerics
        Assert.assertEquals(OSDType.Array, llsdBinaryDS.getType());
        Assert.assertTrue(Arrays.equals(binary, llsdBinaryDS.AsBinary()));
    }
    
    public void testSerializeBinary() throws IOException, ParseException
    {
        OSD llsdBinary = OSD.FromBinary(binary);
        String sBinarySerialized = OSDParser.serializeToString(llsdBinary, OSDFormat.Json);
        OSD llsdBinaryDS = OSDParser.deserialize(sBinarySerialized);
        // Json does serialize binary data to an array of numerics
        Assert.assertEquals(OSDType.Array, llsdBinaryDS.getType());
        Assert.assertTrue(Arrays.equals(binary, llsdBinaryDS.AsBinary()));
    }

    public void testDeserializeArray() throws IOException, ParseException
    {
        String sArrayOne = "[]";
        OSDArray llsdArrayOne = (OSDArray)OSDParser.deserialize(sArrayOne, OSDFormat.Json);
        Assert.assertEquals(OSDType.Array, llsdArrayOne.getType());
        Assert.assertEquals(0, llsdArrayOne.size());

        String sArrayTwo = "[ 0 ]";
        OSDArray llsdArrayTwo = (OSDArray)OSDParser.deserialize(sArrayTwo, OSDFormat.Json);
        Assert.assertEquals(OSDType.Array, llsdArrayTwo.getType());
        Assert.assertEquals(1, llsdArrayTwo.size());
        OSDInteger llsdIntOne = (OSDInteger)llsdArrayTwo.get(0);
        Assert.assertEquals(OSDType.Integer, llsdIntOne.getType());
        Assert.assertEquals(0, llsdIntOne.AsInteger());

        String sArrayThree = "[ 0, 1 ]";
        OSDArray llsdArrayThree = (OSDArray)OSDParser.deserialize(sArrayThree, OSDFormat.Json);
        Assert.assertEquals(OSDType.Array, llsdArrayThree.getType());
        Assert.assertEquals(2, llsdArrayThree.size());
        OSDInteger llsdIntTwo = (OSDInteger)llsdArrayThree.get(0);
        Assert.assertEquals(OSDType.Integer, llsdIntTwo.getType());
        Assert.assertEquals(0, llsdIntTwo.AsInteger());
        OSDInteger llsdIntThree = (OSDInteger)llsdArrayThree.get(1);
        Assert.assertEquals(OSDType.Integer, llsdIntThree.getType());
        Assert.assertEquals(1, llsdIntThree.AsInteger());

        String sArrayFour = " [ \"testtest\", \"aha\",true,false,1, 1.2, [ 1] ] ";
        OSDArray llsdArrayFour = (OSDArray)OSDParser.deserialize(sArrayFour, OSDFormat.Json);
        Assert.assertEquals(OSDType.Array, llsdArrayFour.getType());
        Assert.assertEquals(7, llsdArrayFour.size());
        Assert.assertEquals("testtest", llsdArrayFour.get(0).AsString());
        Assert.assertEquals("aha", llsdArrayFour.get(1).AsString());
        Assert.assertEquals(true, llsdArrayFour.get(2).AsBoolean());
        Assert.assertEquals(false, llsdArrayFour.get(3).AsBoolean());
        Assert.assertEquals(1, llsdArrayFour.get(4).AsInteger());
        Assert.assertEquals(1.2d, llsdArrayFour.get(5).AsReal());
        Assert.assertEquals(OSDType.Array, llsdArrayFour.get(6).getType());
        OSDArray llsdArrayFive = (OSDArray)llsdArrayFour.get(6);
        Assert.assertEquals(1, llsdArrayFive.get(0).AsInteger());
    }

    public void testSerializeArray() throws IOException, ParseException
    {
        OSDArray llsdOne = new OSDArray();
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Json);
        OSDArray llsdOneDS = (OSDArray)OSDParser.deserialize(sOne);
        Assert.assertEquals(OSDType.Array, llsdOneDS.getType());
        Assert.assertEquals(0, llsdOneDS.size());

        OSD llsdTwo = OSD.FromInteger(123234);
        OSD llsdThree = OSD.FromString("asedkfjhaqweiurohzasdf");
        OSDArray llsdFour = new OSDArray();
        llsdFour.add(llsdTwo);
        llsdFour.add(llsdThree);

        llsdOne.add(llsdTwo);
        llsdOne.add(llsdThree);
        llsdOne.add(llsdFour);

        String sFive = OSDParser.serializeToString(llsdOne, OSDFormat.Json);
        OSDArray llsdFive = (OSDArray)OSDParser.deserialize(sFive);
        Assert.assertEquals(OSDType.Array, llsdFive.getType());
        Assert.assertEquals(3, llsdFive.size());
        Assert.assertEquals(OSDType.Integer, llsdFive.get(0).getType());
        Assert.assertEquals(123234, llsdFive.get(0).AsInteger());
        Assert.assertEquals(OSDType.String, llsdFive.get(1).getType());
        Assert.assertEquals("asedkfjhaqweiurohzasdf", llsdFive.get(1).AsString());

        OSDArray llsdSix = (OSDArray)llsdFive.get(2);
        Assert.assertEquals(OSDType.Array, llsdSix.getType());
        Assert.assertEquals(2, llsdSix.size());
        Assert.assertEquals(OSDType.Integer, llsdSix.get(0).getType());
        Assert.assertEquals(123234, llsdSix.get(0).AsInteger());
        Assert.assertEquals(OSDType.String, llsdSix.get(1).getType());
        Assert.assertEquals("asedkfjhaqweiurohzasdf", llsdSix.get(1).AsString());
    }

    public void testDeserializeMap() throws IOException, ParseException
    {
        String sMapOne = " { } ";
        OSDMap llsdMapOne = (OSDMap)OSDParser.deserialize(sMapOne, OSDFormat.Json);
        Assert.assertEquals(OSDType.Map, llsdMapOne.getType());
        Assert.assertEquals(0, llsdMapOne.size());

        String sMapTwo = " { \"test\":2 } ";
        OSDMap llsdMapTwo = (OSDMap)OSDParser.deserialize(sMapTwo, OSDFormat.Json);
        Assert.assertEquals(OSDType.Map, llsdMapTwo.getType());
        Assert.assertEquals(1, llsdMapTwo.size());
        Assert.assertEquals(OSDType.Integer, llsdMapTwo.get("test").getType());
        Assert.assertEquals(2, llsdMapTwo.get("test").AsInteger());

        String sMapThree = " { \"test\":\"testtesttest\", \"aha\" :\"muahahaha\" , \"anywhere\": null } ";
        OSDMap llsdMapThree = (OSDMap)OSDParser.deserialize(sMapThree, OSDFormat.Json);
        Assert.assertEquals(OSDType.Map, llsdMapThree.getType());
        Assert.assertEquals(3, llsdMapThree.size());
        Assert.assertEquals(OSDType.String, llsdMapThree.get("test").getType());
        Assert.assertEquals("testtesttest", llsdMapThree.get("test").AsString());
        Assert.assertEquals(OSDType.String, llsdMapThree.get("test").getType());
        Assert.assertEquals("muahahaha", llsdMapThree.get("aha").AsString());
        Assert.assertEquals(OSDType.Unknown, llsdMapThree.get("self").getType());

        String sMapFour = " { \"test\" : { \"test\" : 1, \"t0st\" : 2.5 }, \"tist\" : \"hello world!\", \"tast\" : \"last\" } ";
        OSDMap llsdMapFour = (OSDMap)OSDParser.deserialize(sMapFour, OSDFormat.Json);
        Assert.assertEquals(OSDType.Map, llsdMapFour.getType());
        Assert.assertEquals(3, llsdMapFour.size());
        Assert.assertEquals("hello world!", llsdMapFour.get("tist").AsString());
        Assert.assertEquals("last", llsdMapFour.get("tast").AsString());
        OSDMap llsdMapFive = (OSDMap)llsdMapFour.get("test");
        Assert.assertEquals(OSDType.Map, llsdMapFive.getType());
        Assert.assertEquals(2, llsdMapFive.size());
        Assert.assertEquals(OSDType.Integer, llsdMapFive.get("test").getType());
        Assert.assertEquals(1, llsdMapFive.get("test").AsInteger());
        Assert.assertEquals(OSDType.Real, llsdMapFive.get("t0st").getType());
        Assert.assertEquals(2.5d, llsdMapFive.get("t0st").AsReal());
    }

    public void testSerializeMap() throws IOException, ParseException, XmlPullParserException
    {
        OSDMap llsdOne = new OSDMap();
        String sOne = OSDParser.serializeToString(llsdOne, OSDFormat.Json);
        OSDMap llsdOneDS = (OSDMap)OSDParser.deserialize(sOne);
        Assert.assertEquals(OSDType.Map, llsdOneDS.getType());
        Assert.assertEquals(0, llsdOneDS.size());

        OSD llsdTwo = OSD.FromInteger(123234);
        OSD llsdThree = OSD.FromString("asedkfjhaqweiurohzasdf");
        OSDMap llsdFour = new OSDMap();
        llsdFour.put("test0", llsdTwo);
        llsdFour.put("test1", llsdThree);

        llsdOne.put("test0", llsdTwo);
        llsdOne.put("test1", llsdThree);
        llsdOne.put("test2", llsdFour);

        String sFive = OSDParser.serializeToString(llsdOne, OSDFormat.Json);
        OSDMap llsdFive = (OSDMap)OSDParser.deserialize(sFive);
        Assert.assertEquals(OSDType.Map, llsdFive.getType());
        Assert.assertEquals(3, llsdFive.size());
        Assert.assertEquals(OSDType.Integer, llsdFive.get("test0").getType());
        Assert.assertEquals(123234, llsdFive.get("test0").AsInteger());
        Assert.assertEquals(OSDType.String, llsdFive.get("test1").getType());
        Assert.assertEquals("asedkfjhaqweiurohzasdf", llsdFive.get("test1").AsString());

        OSDMap llsdSix = (OSDMap)llsdFive.get("test2");
        Assert.assertEquals(OSDType.Map, llsdSix.getType());
        Assert.assertEquals(2, llsdSix.size());
        Assert.assertEquals(OSDType.Integer, llsdSix.get("test0").getType());
        Assert.assertEquals(123234, llsdSix.get("test0").AsInteger());
        Assert.assertEquals(OSDType.String, llsdSix.get("test1").getType());
        Assert.assertEquals("asedkfjhaqweiurohzasdf", llsdSix.get("test1").AsString());

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
        String sSeven = OSDParser.serializeToString(llsdSeven, OSDFormat.Json);
        OSDMap llsdSevenDS = (OSDMap)OSDParser.deserialize(sSeven);
        Assert.assertEquals(OSDType.Map, llsdSevenDS.getType());
        Assert.assertEquals(1, llsdSevenDS.size());
        Assert.assertEquals(content, llsdSevenDS.get(content).AsString());
    }

    public void testDeserializeRealWorldExamples() throws IOException, ParseException
    {
        String realWorldExample = "[" +
        		"{\"destination\":\"http://secondlife.com\"}," + 
        		"{\"version\":1}," +
        		"{" +
        		"\"agent_id\":\"3c115e51-04f4-523c-9fa6-98aff1034730\"," +
        		"\"session_id\":\"2c585cec-038c-40b0-b42e-a25ebab4d132\"," +
        		"\"circuit_code\":1075," +
        		"\"first_name\":\"Phoenix\"," +
        		"\"last_name\":\"Linden\"," +
        		"\"position\":[70.9247,254.378,38.7304]," +
        		"\"look_at\":[-0.043753,-0.999042,0]," +
        		"\"granters\":[\"a2e76fcd-9360-4f6d-a924-000000000003\"]," +
        		"\"attachment_data\":" +
        		"[" +
        		"  {" +
        		"    \"attachment_point\":2," +
        		"    \"item_id\":\"d6852c11-a74e-309a-0462-50533f1ef9b3\"," +
        		"    \"asset_id\":\"c69b29b1-8944-58ae-a7c5-2ca7b23e22fb\"" +
        		"  }," +
        		"  {" +
        		"    \"attachment_point\":10," +
        		"    \"item_id\":\"ff852c22-a74e-309a-0462-50533f1ef900\"," +
        		"    \"asset_id\":\"5868dd20-c25a-47bd-8b4c-dedc99ef9479\"" +
        		"  }" +
        		"]" +
        		"}" +
        		"]";
        // We dont do full testing here. We are fine if a few values are right
        // and the parser doesnt throw an exception
        OSDArray llsdArray = (OSDArray)OSDParser.deserialize(realWorldExample, OSDFormat.Json);
        Assert.assertEquals(OSDType.Array, llsdArray.getType());
        Assert.assertEquals(3, llsdArray.size());

        OSDMap llsdMapOne = (OSDMap)llsdArray.get(0);
        Assert.assertEquals(OSDType.Map, llsdMapOne.getType());
        Assert.assertEquals("http://secondlife.com", llsdMapOne.get("destination").AsString());

        OSDMap llsdMapTwo = (OSDMap)llsdArray.get(1);
        Assert.assertEquals(OSDType.Map, llsdMapTwo.getType());
        Assert.assertEquals(OSDType.Integer, llsdMapTwo.get("version").getType());
        Assert.assertEquals(1, llsdMapTwo.get("version").AsInteger());

        OSDMap llsdMapThree = (OSDMap)llsdArray.get(2);
        Assert.assertEquals(OSDType.UUID, llsdMapThree.get("session_id").getType());
        Assert.assertEquals("2c585cec-038c-40b0-b42e-a25ebab4d132", llsdMapThree.get("session_id").AsString());
        Assert.assertEquals(OSDType.UUID, llsdMapThree.get("agent_id").getType());
        Assert.assertEquals("3c115e51-04f4-523c-9fa6-98aff1034730", llsdMapThree.get("agent_id").AsString());
    }
}
