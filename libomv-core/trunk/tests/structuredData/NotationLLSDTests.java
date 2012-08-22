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

/* 
 * This tests are based upon the description at
 * 
 * http://wiki.secondlife.com/wiki/SD
 * 
 * and (partially) generated by the (supposed) reference implementation at
 * 
 * http://svn.secondlife.com/svn/linden/release/indra/lib/python/indra/base/llsd.py
 */

package structuredData;

import java.io.IOException;
import java.text.ParseException;

import junit.framework.Assert;
import junit.framework.TestCase;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;

public class NotationLLSDTests extends TestCase
{
    public void testDeserializeUndef() throws IOException, ParseException
    {
        String s = "!";
        OSD llsd = OSD.parse(s, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Unknown, llsd.getType());
    }

    public void testSerializeUndef() throws IOException, ParseException
    {
        OSD llsd = new OSD();
        String s = OSD.serializeToString(llsd, OSDFormat.Notation);

        OSD llsdDS = OSD.parse(s);
        Assert.assertEquals(OSDType.Unknown, llsdDS.getType());
    }

    public void testDeserializeBoolean() throws IOException, ParseException
    {
        String t = "true";
        OSD llsdT = OSD.parse(t, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Boolean, llsdT.getType());
        Assert.assertEquals(true, llsdT.AsBoolean());

        String tTwo = "t";
        OSD llsdTTwo = OSD.parse(tTwo, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Boolean, llsdTTwo.getType());
        Assert.assertEquals(true, llsdTTwo.AsBoolean());

        String tThree = "TRUE";
        OSD llsdTThree = OSD.parse(tThree, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Boolean, llsdTThree.getType());
        Assert.assertEquals(true, llsdTThree.AsBoolean());

        String tFour = "T";
        OSD llsdTFour = OSD.parse(tFour, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Boolean, llsdTFour.getType());
        Assert.assertEquals(true, llsdTFour.AsBoolean());

        String tFive = "1";
        OSD llsdTFive = OSD.parse(tFive, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Boolean, llsdTFive.getType());
        Assert.assertEquals(true, llsdTFive.AsBoolean());

        String f = "false";
        OSD llsdF = OSD.parse(f, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Boolean, llsdF.getType());
        Assert.assertEquals(false, llsdF.AsBoolean());

        String fTwo = "f";
        OSD llsdFTwo = OSD.parse(fTwo, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Boolean, llsdFTwo.getType());
        Assert.assertEquals(false, llsdFTwo.AsBoolean());

        String fThree = "FALSE";
        OSD llsdFThree = OSD.parse(fThree, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Boolean, llsdFThree.getType());
        Assert.assertEquals(false, llsdFThree.AsBoolean());

        String fFour = "F";
        OSD llsdFFour = OSD.parse(fFour, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Boolean, llsdFFour.getType());
        Assert.assertEquals(false, llsdFFour.AsBoolean());

        String fFive = "0";
        OSD llsdFFive = OSD.parse(fFive, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Boolean, llsdFFive.getType());
        Assert.assertEquals(false, llsdFFive.AsBoolean());
    }

    public void testSerializeBoolean() throws IOException, ParseException
    {
        OSD llsdTrue = OSD.FromBoolean(true);
        String sTrue = OSD.serializeToString(llsdTrue, OSDFormat.Notation);
        OSD llsdTrueDS = OSD.parse(sTrue);
        Assert.assertEquals(OSDType.Boolean, llsdTrueDS.getType());
        Assert.assertEquals(true, llsdTrueDS.AsBoolean());

        OSD llsdFalse = OSD.FromBoolean(false);
        String sFalse = OSD.serializeToString(llsdFalse, OSDFormat.Notation);
        OSD llsdFalseDS = OSD.parse(sFalse);
        Assert.assertEquals(OSDType.Boolean, llsdFalseDS.getType());
        Assert.assertEquals(false, llsdFalseDS.AsBoolean());
    }

    public void testDeserializeInteger() throws IOException, ParseException
    {
        String integerOne = "i12319423";
        OSD llsdOne = OSD.parse(integerOne, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Integer, llsdOne.getType());
        Assert.assertEquals(12319423, llsdOne.AsInteger());

        String integerTwo = "i-489234";
        OSD llsdTwo = OSD.parse(integerTwo, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Integer, llsdTwo.getType());
        Assert.assertEquals(-489234, llsdTwo.AsInteger());
    }

    public void testSerializeInteger() throws IOException, ParseException
    {
        OSD llsdOne = OSD.FromInteger(12319423);
        String sOne = OSD.serializeToString(llsdOne, OSDFormat.Notation);
        OSD llsdOneDS = OSD.parse(sOne);
        Assert.assertEquals(OSDType.Integer, llsdOneDS.getType());
        Assert.assertEquals(12319423, llsdOne.AsInteger());

        OSD llsdTwo = OSD.FromInteger(-71892034);
        String sTwo = OSD.serializeToString(llsdTwo, OSDFormat.Notation);
        OSD llsdTwoDS = OSD.parse(sTwo);
        Assert.assertEquals(OSDType.Integer, llsdTwoDS.getType());
        Assert.assertEquals(-71892034, llsdTwoDS.AsInteger());
    }

    public void testDeserializeReal() throws IOException, ParseException
    {
        String realOne = "r1123412345.465711";
        OSD llsdOne = OSD.parse(realOne, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Real, llsdOne.getType());
        Assert.assertEquals(1123412345.465711d, llsdOne.AsReal());

        String realTwo = "r-11234684.923411";
        OSD llsdTwo = OSD.parse(realTwo, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Real, llsdTwo.getType());
        Assert.assertEquals(-11234684.923411d, llsdTwo.AsReal());

        String realThree = "r1";
        OSD llsdThree = OSD.parse(realThree, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Real, llsdThree.getType());
        Assert.assertEquals(1d, llsdThree.AsReal());

        String realFour = "r2.0193899999999998204e-06";
        OSD llsdFour = OSD.parse(realFour, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Real, llsdFour.getType());
        Assert.assertEquals(2.0193899999999998204e-06d, llsdFour.AsReal());

        String realFive = "r0";
        OSD llsdFive = OSD.parse(realFive, null, OSDFormat.Notation);
        Assert.assertEquals(OSDType.Real, llsdFive.getType());
        Assert.assertEquals(0d, llsdFive.AsReal());
    }

    public void testSerializeReal() throws IOException, ParseException
    {
        OSD llsdOne = OSD.FromReal(12987234.723847d);
        String sOne = OSD.serializeToString(llsdOne, OSDFormat.Notation);
        OSD llsdOneDS = OSD.parse(sOne);
        Assert.assertEquals(OSDType.Real, llsdOneDS.getType());
        Assert.assertEquals(12987234.723847d, llsdOneDS.AsReal());

        OSD llsdTwo = OSD.FromReal(-32347892.234234d);
        String sTwo = OSD.serializeToString(llsdTwo, OSDFormat.Notation);
        OSD llsdTwoDS = OSD.parse(sTwo);
        Assert.assertEquals(OSDType.Real, llsdTwoDS.getType());
        Assert.assertEquals(-32347892.234234d, llsdTwoDS.AsReal());

        OSD llsdThree = OSD.FromReal(Double.MAX_VALUE);
        String sThree = OSD.serializeToString(llsdThree, OSDFormat.Notation);
        OSD llsdThreeDS = OSD.parse( sThree );
        Assert.assertEquals( OSDType.Real, llsdThreeDS.getType() );
        Assert.assertEquals( Double.MAX_VALUE, llsdThreeDS.AsReal());
    
        OSD llsdFour = OSD.FromReal(Double.MIN_VALUE);
        String sFour = OSD.serializeToString(llsdFour, OSDFormat.Notation);
        OSD llsdFourDS = OSD.parse(sFour);
        Assert.assertEquals(OSDType.Real, llsdFourDS.getType());
        Assert.assertEquals(Double.MIN_VALUE, llsdFourDS.AsReal());

        OSD llsdFive = OSD.FromReal(-1.1123123E+50d);
        String sFive = OSD.serializeToString(llsdFive, OSDFormat.Notation);
        OSD llsdFiveDS = OSD.parse(sFive);
        Assert.assertEquals(OSDType.Real, llsdFiveDS.getType());
        Assert.assertEquals(-1.1123123E+50d, llsdFiveDS.AsReal());

        OSD llsdSix = OSD.FromReal(2.0193899999999998204e-06);
        String sSix = OSD.serializeToString(llsdSix, OSDFormat.Notation);
        OSD llsdSixDS = OSD.parse(sSix);
        Assert.assertEquals(OSDType.Real, llsdSixDS.getType());
        Assert.assertEquals(2.0193899999999998204e-06, llsdSixDS.AsReal());
    }

}
