package libomv;

/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2012-2017, Frederick Martian
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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import junit.framework.TestCase;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.types.Color4;
import libomv.types.Matrix4;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.utils.BitPack;
import libomv.utils.Helpers;

public class TypeTest extends TestCase {
	public void testUUIDs() {
		// Creation
		UUID a = new UUID();
		byte[] bytes = a.getBytes();
		for (int i = 0; i < 16; i++)
			assertFalse(bytes[i] == 0x00);

		// Comparison
		a = new UUID(new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D,
				0x0E, 0x0F, (byte) 0xFF, (byte) 0xFF }, 0);
		UUID b = new UUID(new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C,
				0x0D, 0x0E, 0x0F }, 0);

		assertTrue("UUID comparison operator failed, " + a.toString() + " should equal " + b.toString(), a.equals(b));

		// From string
		String zeroonetwo1 = "000102030405060708090a0b0c0d0e0f";
		b = new UUID(zeroonetwo1);
		assertTrue("UUID compact string constructor failed, should have been " + a.toString() + " but we got "
				+ b.toString(), a.equals(b));

		String zeroonetwo2 = "00010203-0405-0607-0809-0a0b0c0d0e0f";
		b = new UUID(zeroonetwo2);
		assertTrue("UUID hyphenated string constructor failed, should have been " + a.toString() + " but we got "
				+ b.toString(), a.equals(b));

		String zeroonetwo3 = "{00010203-0405-0607-0809-0a0b0c0d0e0f}";
		b = new UUID(zeroonetwo3);
		assertTrue("UUID hyphenated curly bracket string constructor failed, should have been " + a.toString()
				+ " but we got " + b.toString(), a.equals(b));

		// ToString()
		assertTrue(a.toString().equals(zeroonetwo2));

		// CRC test
		long crc = b.CRC();
		assertTrue("CRC calculatoin error", crc == 606084120);

		// From XML
		String xml = "<Test>\n" + " <TextureID>00010203-0405-0607-0809-0a0b0c0d0e0f</TextureID>"
				+ " <AvatarID><UUID>00010203-0405-0607-0809-0a0b0c0d0e0f</UUID></AvatarID>\n"
				+ " <AvatarID><Guid>00010203-0405-0607-0809-0a0b0c0d0e0f</Guid></AvatarID>\n"
				+ " <AvatarID><Guid></Guid></AvatarID>\n" + " <AvatarID><Guid /></AvatarID>\n"
				+ " <AvatarID></AvatarID>\n" + " <AvatarID />\n" + "</Test>";
		Reader reader = new StringReader(xml);
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(reader);
			parser.nextTag(); // skip <Test>

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("TextureID"));
			b = new UUID(parser);
			assertTrue("UUID xml constructor without inner name failed, should have been " + a.toString()
					+ " but we got " + b.toString(), a.equals(b));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("AvatarID"));
			b = new UUID(parser);
			assertTrue("UUID xml constructor with inner name <UUID> failed, should have been " + a.toString()
					+ " but we got " + b.toString(), a.equals(b));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("AvatarID"));
			b = new UUID(parser);
			assertTrue("UUID xml constructor with inner name <Guid> failed, should have been " + a.toString()
					+ " but we got " + b.toString(), a.equals(b));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("AvatarID"));
			b = new UUID(parser);
			assertTrue("UUID xml constructor with empty inner tag failed, should have been UUID.Zero, but we got "
					+ b.toString(), UUID.isZero(b));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("AvatarID"));
			b = new UUID(parser);
			assertTrue(
					"UUID xml constructor with short-form empty inner tag failed, should have been UUID.Zero, but we got "
							+ b.toString(),
					UUID.isZero(b));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("AvatarID"));
			b = new UUID(parser);
			assertTrue("UUID xml constructor with empty outer tag failed, should have been UUID.Zero, but we got "
					+ b.toString(), UUID.isZero(b));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("AvatarID"));
			b = new UUID(parser);
			assertTrue(
					"UUID xml constructor with short-form empty outer tag failed, should have been UUID.Zero, but we got "
							+ b.toString(),
					UUID.isZero(b));

			assertTrue("Should be END_TAG",
					parser.nextTag() == XmlPullParser.END_TAG && parser.getName().equals("Test"));
		} catch (Exception ex) {
			assertTrue("Received exception: " + ex.getMessage(), false);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}

		b = new UUID(new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D,
				0x0E, 0x0F }, 0);

		Writer writer = new StringWriter();
		try {
			XmlSerializer serializer = XmlPullParserFactory.newInstance().newSerializer();
			serializer.setOutput(writer);
			serializer.startTag(null, "Test");
			b.serializeXml(serializer, null, "AvatarID");
			serializer.endTag(null, "Test");
			serializer.endDocument();
			String text = writer.toString();
			assertTrue(
					text.equals("<Test><AvatarID><UUID>00010203-0405-0607-0809-0a0b0c0d0e0f</UUID></AvatarID></Test>"));
		} catch (Exception ex) {
			assertTrue("Received exception: " + ex.getMessage(), false);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	public void testVector3ApproxEquals() {
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

	public void testVector3Casting() {
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

		for (Entry<String, Double> kvp : testNumbers.entrySet()) {
			double testNumber = kvp.getValue();
			double testNumber2 = ((float) testNumber);
			boolean noPrecisionLoss = testNumber == testNumber2;

			Vector3 a = new Vector3((float) testNumber, (float) testNumber, (float) testNumber);
			Vector3d b = new Vector3d(testNumber, testNumber, testNumber);

			Vector3 c = new Vector3(b);
			Vector3d d = new Vector3d(a);

			if (noPrecisionLoss) {
				System.err.println("Unsuitable test value used-" + " test number should have precision loss when"
						+ " cast to float (" + kvp.getKey() + ").");
			} else {
				assertFalse(String.format("Vector casting failed, precision loss should" + " have occurred. %s: %f, %f",
						kvp.getKey(), a.X, b.X), a.equals(b));
				assertFalse(String.format("Vector casting failed, explicit cast of double"
						+ " to float should result in precision loss whichwas should not magically disappear when"
						+ " Vector3 is implicitly cast to Vector3d. %s: %f, %f", kvp.getKey(), b.X, d.X), b.equals(d));
			}
			assertTrue(String.format("Vector casting failed, Vector3 compared to"
					+ " explicit cast of Vector3d to Vector3 should result in identical precision loss."
					+ " %s: %f, %f", kvp.getKey(), a.X, c.X), a.equals(c));
			assertTrue(
					String.format(
							"Vector casting failed, implicit cast of Vector3"
									+ " to Vector3d should not result in precision loss. %s: %f, %f",
							kvp.getKey(), a.X, d.X),
					a.equals(d));
		}
	}

	public void testVector3Xml() {
		// From XML
		Vector3 test, onetwothree = new Vector3(1.5f, 2.5f, 3.5f), onezerothree = new Vector3(1.5f, 0.0f, 3.5f);

		String xml = "<Test>\n" + " <Offset><Trash>100</Trash><X>1.5</X><Y>2.5</Y><Z>3.5</Z></Offset>\n"
				+ " <Offset><X>1.5</X><Y>2.5</Y><Z>3.5</Z></Offset>\n"
				+ " <Offset><X>1.5</X><Y>0.0</Y><Z>3.5</Z></Offset>\n"
				+ " <Offset><X>1.5</X><Y></Y><Z>3.5</Z></Offset>\n" + " <Offset><X>1.5</X><Y /><Z>3.5</Z></Offset>\n"
				+ " <Offset><X></X><Y></Y><Z></Z></Offset>\n" + " <Offset><X /><Y /><Z /></Offset>\n"
				+ " <Offset></Offset>\n" + " <Offset />\n" + "</Test>";
		Reader reader = new StringReader(xml);
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(reader);
			parser.nextTag(); // skip <Test>
			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Offset"));
			test = new Vector3(parser);
			assertTrue(
					"Vector3 xml constructor with superfluous element failed, should have been "
							+ onetwothree.toString() + " but we got " + test.toString(),
					test.approxEquals(onetwothree, 0.1f));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Offset"));
			test = new Vector3(parser);
			assertTrue("Vector3 xml constructor failed, should have been " + onetwothree.toString() + " but we got "
					+ test.toString(), test.approxEquals(onetwothree, 0.1f));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Offset"));
			test = new Vector3(parser);
			assertTrue(
					"Vector3 xml constructor with inner element set to 0.0 failed, should have been "
							+ onezerothree.toString() + " but we got " + test.toString(),
					test.approxEquals(onezerothree, 0.1f));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Offset"));
			test = new Vector3(parser);
			assertTrue(
					"Vector3 xml constructor with empty inner element failed, should have been "
							+ onezerothree.toString() + " but we got " + test.toString(),
					test.approxEquals(onezerothree, 0.1f));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Offset"));
			test = new Vector3(parser);
			assertTrue(
					"Vector3 xml constructor with short-form empty inner element failed, should have been "
							+ onezerothree.toString() + " but we got " + test.toString(),
					test.approxEquals(onezerothree, 0.1f));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Offset"));
			test = new Vector3(parser);
			assertTrue(
					"Vector3 xml constructor with empty inner tags failed, should have been Vector3.Zero, but we got "
							+ test.toString(),
					test.isZero());

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Offset"));
			test = new Vector3(parser);
			assertTrue(
					"Vector3 xml constructor with short-form empty inner tags failed, should have been Vector3.Zero, but we got "
							+ test.toString(),
					test.isZero());

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Offset"));
			test = new Vector3(parser);
			assertTrue("Vector3 xml constructor with empty outer tag failed, should have been Vector3.Zero, but we got "
					+ test.toString(), test.isZero());

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Offset"));
			test = new Vector3(parser);
			assertTrue(
					"Vector3 xml constructor with short-form empty outer tag failed, should have been Vector3.Zero, but we got "
							+ test.toString(),
					test.isZero());

			assertTrue("Should be END_TAG",
					parser.nextTag() == XmlPullParser.END_TAG && parser.getName().equals("Test"));
		} catch (Exception ex) {
			assertTrue("Received exception: " + ex.getMessage(), false);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}

		Writer writer = new StringWriter();
		try {
			XmlSerializer serializer = XmlPullParserFactory.newInstance().newSerializer();
			serializer.setOutput(writer);
			serializer.startTag(null, "Test");
			onetwothree.serializeXml(serializer, null, "Offset");
			serializer.endTag(null, "Test");
			serializer.endDocument();
			String text = writer.toString();
			assertTrue(text.equals("<Test><Offset><X>1.5</X><Y>2.5</Y><Z>3.5</Z></Offset></Test>"));
		} catch (Exception ex) {
			assertTrue("Received exception: " + ex.getMessage(), false);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	public void testQuaternions() {
		Quaternion a = new Quaternion(1, 0, 0, 0);
		Quaternion b = new Quaternion(1, 0, 0, 0);

		assertTrue("Quaternion comparison operator failed", a.equals(b));

		Quaternion expected = new Quaternion(0, 0, 0, -1);
		Quaternion result = a.multiply(b);

		assertTrue(a.toString() + " * " + b.toString() + " produced " + result.toString() + " instead of "
				+ expected.toString(), result.equals(expected));

		a = new Quaternion(1, 0, 0, 0);
		b = new Quaternion(0, 1, 0, 0);
		expected = new Quaternion(0, 0, 1, 0);
		result = a.multiply(b);

		assertTrue(a.toString() + " * " + b.toString() + " produced " + result.toString() + " instead of "
				+ expected.toString(), result.equals(expected));

		a = new Quaternion(0, 0, 1, 0);
		b = new Quaternion(0, 1, 0, 0);
		expected = new Quaternion(-1, 0, 0, 0);
		result = a.multiply(b);

		assertTrue(a.toString() + " * " + b.toString() + " produced " + result.toString() + " instead of "
				+ expected.toString(), result.equals(expected));
	}

	public void testQuaternionXml() {
		// From XML
		Quaternion test, onetwothree = new Quaternion(1.5f, 2.5f, 3.5f, 4.5f),
				onezerothree = new Quaternion(1.5f, 0.0f, 3.5f, 4.5f);

		String xml = "<Test>\n" + " <Rotation><Trash>100</Trash><X>1.5</X><Y>2.5</Y><Z>3.5</Z><W>4.5</W></Rotation>\n"
				+ " <Rotation><X>1.5</X><Y>2.5</Y><Z>3.5</Z><W>4.5</W></Rotation>\n"
				+ " <Rotation><X>1.5</X><Y>0.0</Y><Z>3.5</Z><W>4.5</W></Rotation>\n"
				+ " <Rotation><X>1.5</X><Y></Y><Z>3.5</Z><W>4.5</W></Rotation>\n"
				+ " <Rotation><X>1.5</X><Y /><Z>3.5</Z><W>4.5</W></Rotation>\n"
				+ " <Rotation><X></X><Y></Y><Z></Z><W></W></Rotation>\n"
				+ " <Rotation><X /><Y /><Z /><W /></Rotation>\n" + " <Rotation></Rotation>\n" + " <Rotation />\n"
				+ "</Test>";
		Reader reader = new StringReader(xml);
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(reader);
			parser.nextTag(); // skip <Test>
			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Rotation"));
			test = new Quaternion(parser);
			assertTrue(
					"Quaternion xml constructor with superfluous element failed, should have been "
							+ onetwothree.toString() + " but we got " + test.toString(),
					test.approxEquals(onetwothree, 0.1f));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Rotation"));
			test = new Quaternion(parser);
			assertTrue("Quaternion xml constructor failed, should have been " + onetwothree.toString() + " but we got "
					+ test.toString(), test.approxEquals(onetwothree, 0.1f));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Rotation"));
			test = new Quaternion(parser);
			assertTrue(
					"Quaternion xml constructor with inner element set to 0.0 failed, should have been "
							+ onezerothree.toString() + " but we got " + test.toString(),
					test.approxEquals(onezerothree, 0.1f));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Rotation"));
			test = new Quaternion(parser);
			assertTrue(
					"Vector3 xml constructor with empty inner element failed, should have been "
							+ onezerothree.toString() + " but we got " + test.toString(),
					test.approxEquals(onezerothree, 0.1f));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Rotation"));
			test = new Quaternion(parser);
			assertTrue(
					"Quaternion xml constructor with short-form empty inner element failed, should have been "
							+ onezerothree.toString() + " but we got " + test.toString(),
					test.approxEquals(onezerothree, 0.1f));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Rotation"));
			test = new Quaternion(parser);
			assertTrue(
					"Quaternion xml constructor with empty inner tags failed, should have been Quaternion.Zero, but we got "
							+ test.toString(),
					test.isZero());

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Rotation"));
			test = new Quaternion(parser);
			assertTrue(
					"Quaternion xml constructor with short-form empty inner tags failed, should have been Quaternion.Zero, but we got "
							+ test.toString(),
					test.isZero());

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Rotation"));
			test = new Quaternion(parser);
			assertTrue(
					"Quaternion xml constructor with empty outer tag failed, should have been Quaternion.Zero, but we got "
							+ test.toString(),
					test.isZero());

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Rotation"));
			test = new Quaternion(parser);
			assertTrue(
					"Quaternion xml constructor with short-form empty outer tag failed, should have been Quaternion.Zero, but we got "
							+ test.toString(),
					test.isZero());

			assertTrue("Should be END_TAG",
					parser.nextTag() == XmlPullParser.END_TAG && parser.getName().equals("Test"));
		} catch (Exception ex) {
			assertTrue("Received exception: " + ex.getMessage(), false);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}

		Writer writer = new StringWriter();
		try {
			XmlSerializer serializer = XmlPullParserFactory.newInstance().newSerializer();
			serializer.setOutput(writer);
			serializer.startTag(null, "Test");
			onetwothree.serializeXml(serializer, null, "Rotation");
			serializer.endTag(null, "Test");
			serializer.endDocument();
			String text = writer.toString();
			assertTrue(text.equals("<Test><Rotation><X>1.5</X><Y>2.5</Y><Z>3.5</Z><W>4.5</W></Rotation></Test>"));
		} catch (Exception ex) {
			assertTrue("Received exception: " + ex.getMessage(), false);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	public void testColor4Xml() {
		// From XML
		Color4 test, onetwothree = new Color4(0.12f, 0.23f, 0.34f, 0.45f),
				onezerothree = new Color4(0.12f, 0f, 0.34f, 0.45f);

		String xml = "<Test>\n" + " <Color><Trash>100</Trash><R>0.12</R><G>0.23</G><B>0.34</B><A>0.45</A></Color>\n"
				+ " <Color><R>0.12</R><G>0.23</G><B>0.34</B><A>0.45</A></Color>\n"
				+ " <Color><R>0.12</R><G>0.0</G><B>0.34</B><A>0.45</A></Color>\n"
				+ " <Color><R>0.12</R><G></G><B>0.34</B><A>0.45</A></Color>\n"
				+ " <Color><R>0.12</R><G /><B>0.34</B><A>0.45</A></Color>\n"
				+ " <Color><R></R><G></G><B></B><A></A></Color>\n" + " <Color><R /><G /><B /><A /></Color>\n"
				+ " <Color></Color>\n" + " <Color />\n" + "</Test>";
		Reader reader = new StringReader(xml);
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(reader);
			parser.nextTag(); // skip <Test>
			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Color"));
			test = new Color4(parser);
			assertTrue("Color4 xml constructor with superfluous element failed, should have been "
					+ onetwothree.toString() + " but we got " + test.toString(), test.equals(onetwothree));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Color"));
			test = new Color4(parser);
			assertTrue("Color4 xml constructor failed, should have been " + onetwothree.toString() + " but we got "
					+ test.toString(), test.equals(onetwothree));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Color"));
			test = new Color4(parser);
			assertTrue("Color4 xml constructor with inner element set to 0.0 failed, should have been "
					+ onezerothree.toString() + " but we got " + test.toString(), test.equals(onezerothree));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Color"));
			test = new Color4(parser);
			assertTrue("Color4 xml constructor with empty inner element failed, should have been "
					+ onezerothree.toString() + " but we got " + test.toString(), test.equals(onezerothree));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Color"));
			test = new Color4(parser);
			assertTrue("Color4 xml constructor with short-form empty inner element failed, should have been "
					+ onezerothree.toString() + " but we got " + test.toString(), test.equals(onezerothree));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Color"));
			test = new Color4(parser);
			assertTrue("Color4 xml constructor with empty inner tags failed, should have been Color4.Black, but we got "
					+ test.toString(), test.equals(Color4.Black));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Color"));
			test = new Color4(parser);
			assertTrue(
					"Color4 xml constructor with short-form empty inner tags failed, should have been Color4.Black, but we got "
							+ test.toString(),
					test.equals(Color4.Black));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Color"));
			test = new Color4(parser);
			assertTrue("Color4 xml constructor with empty outer tag failed, should have been Color4.Black, but we got "
					+ test.toString(), test.equals(Color4.Black));

			assertTrue("Unexpected tag type", parser.nextTag() == XmlPullParser.START_TAG);
			assertTrue("Unexpected tag name", parser.getName().equals("Color"));
			test = new Color4(parser);
			assertTrue(
					"Color4 xml constructor with short-form empty outer tag failed, should have been Color4.Black, but we got "
							+ test.toString(),
					test.equals(Color4.Black));

			assertTrue("Should be END_TAG",
					parser.nextTag() == XmlPullParser.END_TAG && parser.getName().equals("Test"));
		} catch (Exception ex) {
			assertTrue("Received exception: " + ex.getMessage(), false);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}

		Writer writer = new StringWriter();
		try {
			XmlSerializer serializer = XmlPullParserFactory.newInstance().newSerializer();
			serializer.setOutput(writer);
			serializer.startTag(null, "Test");
			onetwothree.serializeXml(serializer, null, "Color");
			serializer.endTag(null, "Test");
			serializer.endDocument();
			String text = writer.toString();
			assertTrue(text.equals("<Test><Color><R>0.12</R><G>0.23</G><B>0.34</B><A>0.45</A></Color></Test>"));
		} catch (Exception ex) {
			assertTrue("Received exception: " + ex.getMessage(), false);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	public void testMatrix() throws Exception {
		Matrix4 matrix = new Matrix4(0, 0, 74, 1, 0, 435, 0, 1, 345, 0, 34, 1, 0, 0, 0, 0);

		/* determinant of singular matrix returns zero */
		assertEquals("Determinant of singular matrix is not 0", 0d, matrix.determinant(), 0.001d);

		/* inverse of identity matrix is the identity matrix */
		assertTrue("Inverse of identity matrix should be the identity matrix",
				Matrix4.Identity.equals(Matrix4.inverse(Matrix4.Identity)));

		/* inverse of non-singular matrix test */
		matrix = new Matrix4(1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 1);
		Matrix4 expectedInverse = new Matrix4(0, 1, -1, 0, 1, -1, 1, 0, -1, 1, 0, 0, 0, 0, 0, 1);
		assertEquals("inverse of simple non singular matrix failed", expectedInverse, Matrix4.inverse(matrix));
	}

	// public void testVectorQuaternionMath()
	// {
	// // Convert a vector to a quaternion and back
	// Vector3 a = new Vector3(1f, 0.5f, 0.75f);
	// Quaternion b = a.ToQuaternion();
	// Vector3 c;
	// b.GetEulerAngles(out c.X, out c.Y, out c.Z);

	// IsTrue(a == c, c.ToString() + " does not equal " + a.ToString());
	// }

	public void testFloatsToTerseStrings() {
		float f = 1.20f;
		String a, b = "1.2";

		a = Helpers.FloatToTerseString(f);
		boolean e = a.equals(b);
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

	public void testBitUnpacking() {
		byte[] data = new byte[] { (byte) 0x80, 0x00, 0x0F, 0x50, (byte) 0x83, 0x7D };
		BitPack bitpacker = new BitPack(data, 0);

		int b = bitpacker.unpackBits(1);
		assertTrue("Unpacked " + b + " instead of 1", b == 1);

		b = bitpacker.unpackBits(1);
		assertTrue("Unpacked " + b + " instead of 0", b == 0);

		bitpacker = new BitPack(data, 2);

		b = bitpacker.unpackBits(4);
		assertTrue("Unpacked " + b + " instead of 0", b == 0);

		b = bitpacker.unpackBits(8);
		assertTrue("Unpacked " + b + " instead of 0xF5", b == 0xF5);

		b = bitpacker.unpackBits(4);
		assertTrue("Unpacked " + b + " instead of 0", b == 0);

		b = bitpacker.unpackBits(10);
		assertTrue("Unpacked " + b + " instead of 0x0183", b == 0x0183);
	}

	public void testBitPacking() {
		byte[] packedBytes = new byte[12];
		BitPack bitpacker = new BitPack(packedBytes, 0);

		bitpacker.packBits(0x0ABBCCDD, 32);
		bitpacker.packBits(25, 5);
		bitpacker.packFloat(123.321f);
		bitpacker.packBits(1000, 16);

		bitpacker = new BitPack(packedBytes, 0);

		int b = bitpacker.unpackBits(32);
		assertTrue("Unpacked " + b + " instead of 2864434397", b == 0x0ABBCCDD);

		b = bitpacker.unpackBits(5);
		assertTrue("Unpacked " + b + " instead of 25", b == 25);

		float f = bitpacker.unpackFloat();
		assertTrue("Unpacked " + f + " instead of 123.321", f == 123.321f);

		b = bitpacker.unpackBits(16);
		assertTrue("Unpacked " + b + " instead of 1000", b == 1000);

		packedBytes = new byte[1];
		bitpacker = new BitPack(packedBytes, 0);
		bitpacker.packBit(true);

		bitpacker = new BitPack(packedBytes, 0);
		b = bitpacker.unpackBits(1);
		assertTrue("Unpacked " + b + " instead of 1", b == 1);

		packedBytes = new byte[1];
		packedBytes[0] = Byte.MAX_VALUE;
		bitpacker = new BitPack(packedBytes, 0);
		bitpacker.packBit(false);

		bitpacker = new BitPack(packedBytes, 0);
		b = bitpacker.unpackBits(1);
		assertTrue("Unpacked " + b + " instead of 0", b == 0);
	}

	public void testLLSDTerseParsing() throws IOException, ParseException {
		String testOne = "[r0.99967899999999998428,r-0.025334599999999998787,r0]";
		String testTwo = "[[r1,r1,r1],r0]";
		String testThree = "{'region_handle':[r255232, r256512], 'position':[r33.6, r33.71, r43.13], 'look_at':[r34.6, r33.71, r43.13]}";

		OSD obj = OSDParser.deserialize(testOne, OSDFormat.Notation);
		assertTrue("Expected OSDArray, got " + obj.getType().toString(), obj instanceof OSDArray);
		OSDArray array = (OSDArray) obj;
		assertTrue("Expected three contained objects, got " + array.size(), array.size() == 3);
		assertTrue("Unexpected value for first real " + array.get(0).AsReal(),
				array.get(0).AsReal() > 0.9d && array.get(0).AsReal() < 1.0d);
		assertTrue("Unexpected value for second real " + array.get(1).AsReal(),
				array.get(1).AsReal() < 0.0d && array.get(1).AsReal() > -0.03d);
		assertTrue("Unexpected value for third real " + array.get(2).AsReal(), array.get(2).AsReal() == 0.0d);

		obj = OSDParser.deserialize(testTwo, OSDFormat.Notation);
		assertTrue("Expected OSDArray, got " + obj.getType().toString(), obj instanceof OSDArray);
		array = (OSDArray) obj;
		assertTrue("Expected two contained objects, got " + array.size(), array.size() == 2);
		assertTrue("Unexpected value for real " + array.get(1).AsReal(), array.get(1).AsReal() == 0.0d);
		obj = array.get(0);
		assertTrue("Expected ArrayList, got " + obj.getType().toString(), obj instanceof OSDArray);
		array = (OSDArray) obj;
		assertTrue(
				"Unexpected value(s) for nested array: " + array.get(0).AsReal() + ", " + array.get(1).AsReal() + ", "
						+ array.get(2).AsReal(),
				array.get(0).AsReal() == 1.0d && array.get(1).AsReal() == 1.0d && array.get(2).AsReal() == 1.0d);

		obj = OSDParser.deserialize(testThree, OSDFormat.Notation);
		assertTrue("Expected OSDMap, got " + obj.getType().toString(), obj instanceof OSDMap);
		OSDMap hashtable = (OSDMap) obj;
		assertTrue("Expected three contained objects, got " + hashtable.size(), hashtable.size() == 3);
		assertTrue(hashtable.get("region_handle") instanceof OSDArray);
		assertTrue(((OSDArray) hashtable.get("region_handle")).size() == 2);
		assertTrue(hashtable.get("position") instanceof OSDArray);
		assertTrue(((OSDArray) hashtable.get("position")).size() == 3);
		assertTrue(hashtable.get("look_at") instanceof OSDArray);
		assertTrue(((OSDArray) hashtable.get("look_at")).size() == 3);
	}
}