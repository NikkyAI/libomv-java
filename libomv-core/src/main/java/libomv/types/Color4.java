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
package libomv.types;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.IllegalArgumentException;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.utils.Helpers;

/** An 8-bit color structure including an alpha channel */
public final class Color4 {

	/** A Color4 with zero RGB values and fully opaque (alpha 1.0) */
	public static final Color4 BLACK = new Color4(0f, 0f, 0f, 1f);

	/** A Color4 with full RGB values (1.0) and fully opaque (alpha 1.0) */
	public static final Color4 WHITE = new Color4(1f, 1f, 1f, 1f);

	private static final float HUE_MAX = 360f;

	/** Red */
	public float r;
	/** Green */
	public float g;
	/** Blue */
	public float b;
	/** Alpha */
	public float a;

	/**
	 * Builds a color from four values
	 *
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public Color4(byte r, byte g, byte b, byte a) {
		final float quanta = 1.0f / 255.0f;

		this.r = r * quanta;
		this.g = g * quanta;
		this.b = b * quanta;
		this.a = a * quanta;
	}

	/**
	 * Builds a color from four values
	 *
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 *
	 * @throws IllegalArgumentException
	 *             if one of the values is outside 0 .. 1.0
	 */
	public Color4(float r, float g, float b, float a) {
		// Quick check to see if someone is doing something obviously wrong
		// like using float values from 0.0 - 255.0
		if (r > 1f || g > 1f || b > 1f || a > 1f) {
			throw new IllegalArgumentException(String
					.format("Attempting to initialize Color4 with out of range values <%f,%f,%f,%f>", r, g, b, a));
		}

		// Valid range is from 0.0 to 1.0
		this.r = Helpers.clamp(r, 0f, 1f);
		this.g = Helpers.clamp(g, 0f, 1f);
		this.b = Helpers.clamp(b, 0f, 1f);
		this.a = Helpers.clamp(a, 0f, 1f);
	}

	/**
	 * Constructor, builds a Color4 from an XML reader
	 *
	 * @param parser
	 *            XML pull parser reader
	 */
	public Color4(XmlPullParser parser) throws XmlPullParserException, IOException {
		// entering with event on START_TAG for the tag name identifying the Vector3
		int eventType = parser.getEventType();
		if (eventType != XmlPullParser.START_TAG)
			throw new XmlPullParserException("Unexpected Tag event " + eventType + " for tag name " + parser.getName(),
					parser, null);

		a = 1f;
		while (parser.nextTag() == XmlPullParser.START_TAG) {
			String name = parser.getName();
			if (name.equalsIgnoreCase("R")) {
				r = Helpers.tryParseFloat(parser.nextText().trim());
			} else if (name.equalsIgnoreCase("G")) {
				g = Helpers.tryParseFloat(parser.nextText().trim());
			} else if (name.equalsIgnoreCase("B")) {
				b = Helpers.tryParseFloat(parser.nextText().trim());
			} else if (name.equalsIgnoreCase("A")) {
				String element = parser.nextText().trim();
				if (!Helpers.isEmpty(element))
					a = Helpers.tryParseFloat(element);
			} else {
				Helpers.skipElement(parser);
			}
		}
	}

	/**
	 * Builds a color from a byte array
	 *
	 * @param byteArray
	 *            Byte array containing a 16 byte color
	 * @param pos
	 *            Beginning position in the byte array
	 * @param inverted
	 *            True if the byte array stores inverted values, otherwise false.
	 *            For example the color black (fully opaque) inverted would be 0xFF
	 *            0xFF 0xFF 0x00
	 */
	public Color4(byte[] byteArray, int pos, boolean inverted) {
		fromBytes(byteArray, pos, inverted);
	}

	/**
	 * Returns the raw bytes for this vector
	 *
	 * @param byteArray
	 *            Byte array containing a 16 byte color
	 * @param pos
	 *            Beginning position in the byte array
	 * @param inverted
	 *            True if the byte array stores inverted values, otherwise false.
	 *            For example the color black (fully opaque) inverted would be 0xFF
	 *            0xFF 0xFF 0x00
	 * @param alphaInverted
	 *            True if the alpha value is inverted in addition to whatever the
	 *            inverted parameter is. Setting inverted true and alphaInverted
	 *            true will flip the alpha value back to non-inverted, but keep the
	 *            other color bytes inverted
	 * @return A 16 byte array containing R, G, B, and A
	 */
	public Color4(byte[] byteArray, int pos, boolean inverted, boolean alphaInverted) {
		fromBytes(byteArray, pos, inverted, alphaInverted);
	}

	/**
	 * Copy constructor
	 *
	 * @param color
	 *            Color to copy
	 */
	public Color4(Color4 color) {
		r = color.r;
		g = color.g;
		b = color.b;
		a = color.a;
	}

	/**
	 * CompareTo implementation
	 *
	 * Sorting ends up like this: |--Grayscale--||--Color--|. Alpha is only used
	 * when the colors are otherwise equivalent
	 */
	public int compareTo(Color4 color) {
		float thisHue = getHue();
		float thatHue = color.getHue();

		if (thisHue < 0f && thatHue < 0f) {
			// Both monochromatic
			if (r == color.r) {
				// Monochromatic and equal, compare alpha
				return ((Float) a).compareTo(color.a);
			}
			// Compare lightness
			return ((Float) r).compareTo(r);
		}

		if (thisHue == thatHue) {
			// RGB is equal, compare alpha
			return ((Float) a).compareTo(color.a);
		}
		// Compare hues
		return ((Float) thisHue).compareTo(thatHue);
	}

	public void fromBytes(byte[] byteArray, int pos, boolean inverted) {
		final float quanta = 1.0f / 255.0f;

		if (inverted) {
			r = (255 - (byteArray[pos] & 0xFF)) * quanta;
			g = (255 - (byteArray[pos + 1] & 0xFF)) * quanta;
			b = (255 - (byteArray[pos + 2] & 0xFF)) * quanta;
			a = (255 - (byteArray[pos + 3] & 0xFF)) * quanta;
		} else {
			r = (byteArray[pos] & 0xFF) * quanta;
			g = (byteArray[pos + 1] & 0xFF) * quanta;
			b = (byteArray[pos + 2] & 0xFF) * quanta;
			a = (byteArray[pos + 3] & 0xFF) * quanta;
		}
	}

	/**
	 * Builds a color from a byte array
	 *
	 * @param byteArray
	 *            Byte array containing a 16 byte color
	 * @param pos
	 *            Beginning position in the byte array
	 * @param inverted
	 *            True if the byte array stores inverted values, otherwise false.
	 *            For example the color black (fully opaque) inverted would be 0xFF
	 *            0xFF 0xFF 0x00
	 * @param alphaInverted
	 *            True if the alpha value is inverted in addition to whatever the
	 *            inverted parameter is. Setting inverted true and alphaInverted
	 *            true will flip the alpha value back to non-inverted, but keep the
	 *            other color bytes inverted
	 */
	public void fromBytes(byte[] byteArray, int pos, boolean inverted, boolean alphaInverted) {
		fromBytes(byteArray, pos, inverted);

		if (alphaInverted) {
			a = 1.0f - a;
		}
	}

	public byte[] getBytes() {
		byte[] byteArray = new byte[4];
		toBytes(byteArray, 0, false);
		return byteArray;
	}

	public byte[] getBytes(boolean inverted) {
		byte[] byteArray = new byte[4];
		toBytes(byteArray, 0, inverted);
		return byteArray;
	}

	public void write(OutputStream stream, boolean inverted) throws IOException {
		byte r = Helpers.floatToByte(this.r, 0f, 1f);
		byte g = Helpers.floatToByte(this.g, 0f, 1f);
		byte b = Helpers.floatToByte(this.b, 0f, 1f);
		byte a = Helpers.floatToByte(this.a, 0f, 1f);

		if (inverted) {
			stream.write((byte) (255 - (r & 0xFF)));
			stream.write((byte) (255 - (g & 0xFF)));
			stream.write((byte) (255 - (b & 0xFF)));
			stream.write((byte) (255 - (a & 0xFF)));
		} else {
			stream.write(r);
			stream.write(g);
			stream.write(b);
			stream.write(a);
		}
	}

	public byte[] getFloatBytes() {
		byte[] bytes = new byte[16];
		toFloatBytesL(bytes, 0);
		return bytes;
	}

	/**
	 * Writes the raw bytes for this color to a byte array
	 *
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 16 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes(byte[] dest, int pos) {
		return toBytes(dest, pos, false);
	}

	/**
	 * Serializes this color into four bytes in a byte array
	 *
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @param inverted
	 *            True to invert the output (1.0 becomes 0 instead of 255)
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes(byte[] dest, int pos, boolean inverted) {
		dest[pos + 0] = Helpers.floatToByte(r, 0f, 1f);
		dest[pos + 1] = Helpers.floatToByte(g, 0f, 1f);
		dest[pos + 2] = Helpers.floatToByte(b, 0f, 1f);
		dest[pos + 3] = Helpers.floatToByte(a, 0f, 1f);

		if (inverted) {
			dest[pos + 0] = (byte) (255 - (dest[pos + 0] & 0xFF));
			dest[pos + 1] = (byte) (255 - (dest[pos + 1] & 0xFF));
			dest[pos + 2] = (byte) (255 - (dest[pos + 2] & 0xFF));
			dest[pos + 3] = (byte) (255 - (dest[pos + 3] & 0xFF));
		}
		return 4;
	}

	/**
	 * Writes the raw bytes for this color to a byte array in little endian format
	 *
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 16 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toFloatBytesL(byte[] dest, int pos) {
		Helpers.floatToBytesL(r, dest, pos + 0);
		Helpers.floatToBytesL(g, dest, pos + 4);
		Helpers.floatToBytesL(b, dest, pos + 8);
		Helpers.floatToBytesL(a, dest, pos + 12);
		return 4;
	}

	public float getHue() {

		float max = Math.max(Math.max(r, g), b);
		float min = Math.min(Math.min(r, b), b);

		if (max == min) {
			// Achromatic, hue is undefined
			return -1f;
		} else if (r == max) {
			float bDelta = (((max - b) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			float gDelta = (((max - g) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			return bDelta - gDelta;
		} else if (g == max) {
			float rDelta = (((max - r) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			float bDelta = (((max - b) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			return (HUE_MAX / 3f) + rDelta - bDelta;
		} else
		// B == max
		{
			float gDelta = (((max - g) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			float rDelta = (((max - r) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			return ((2f * HUE_MAX) / 3f) + gDelta - rDelta;
		}
	}

	/** Ensures that values are in range 0-1 */
	public void clampValues() {
		if (r < 0f) {
			r = 0f;
		}
		if (g < 0f) {
			g = 0f;
		}
		if (b < 0f) {
			b = 0f;
		}
		if (a < 0f) {
			a = 0f;
		}
		if (r > 1f) {
			r = 1f;
		}
		if (g > 1f) {
			g = 1f;
		}
		if (b > 1f) {
			b = 1f;
		}
		if (a > 1f) {
			a = 1f;
		}
	}

	/**
	 * Create an RGB color from a hue, saturation, value combination
	 *
	 * @param hue
	 *            Hue
	 * @param saturation
	 *            Saturation
	 * @param value
	 *            Value
	 * @return An fully opaque RGB color (alpha is 1.0)
	 */
	public static Color4 fromHSV(double hue, double saturation, double value) {
		double r = 0d;
		double g = 0d;
		double b = 0d;

		if (saturation == 0d) {
			// If s is 0, all colors are the same.
			// This is some flavor of gray.
			r = value;
			g = value;
			b = value;
		} else {
			double p;
			double q;
			double t;

			double fractionalSector;
			int sectorNumber;
			double sectorPos;

			// The color wheel consists of 6 sectors.
			// Figure out which sector we're in.
			sectorPos = hue / 60d;
			sectorNumber = (int) (Math.floor(sectorPos));

			// get the fractional part of the sector.
			// That is, how many degrees into the sector
			// are you?
			fractionalSector = sectorPos - sectorNumber;

			// Calculate values for the three axes
			// of the color.
			p = value * (1d - saturation);
			q = value * (1d - (saturation * fractionalSector));
			t = value * (1d - (saturation * (1d - fractionalSector)));

			// Assign the fractional colors to r, g, and b
			// based on the sector the angle is in.
			switch (sectorNumber) {
			case 0:
				r = value;
				g = t;
				b = p;
				break;
			case 1:
				r = q;
				g = value;
				b = p;
				break;
			case 2:
				r = p;
				g = value;
				b = t;
				break;
			case 3:
				r = p;
				g = q;
				b = value;
				break;
			case 4:
				r = t;
				g = p;
				b = value;
				break;
			case 5:
				r = value;
				g = p;
				b = q;
				break;
			default:
				break;
			}
		}

		return new Color4((float) r, (float) g, (float) b, 1f);
	}

	/**
	 * Performs linear interpolation between two colors
	 *
	 * @param value1
	 *            Color to start at
	 * @param value2
	 *            Color to end at
	 * @param amount
	 *            Amount to interpolate
	 * @return The interpolated color
	 */
	public static Color4 lerp(Color4 value1, Color4 value2, float amount) {
		return new Color4(Helpers.lerp(value1.r, value2.r, amount), Helpers.lerp(value1.g, value2.g, amount),
				Helpers.lerp(value1.b, value2.b, amount), Helpers.lerp(value1.a, value2.a, amount));
	}

	static public Color4 parse(XmlPullParser parser) throws XmlPullParserException, IOException {
		return new Color4(parser);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(namespace, name);
		writer.startTag(namespace, "R").text(Float.toString(r)).endTag(namespace, "R");
		writer.startTag(namespace, "G").text(Float.toString(g)).endTag(namespace, "G");
		writer.startTag(namespace, "B").text(Float.toString(b)).endTag(namespace, "B");
		writer.startTag(namespace, "A").text(Float.toString(a)).endTag(namespace, "A");
		writer.endTag(namespace, name);
	}

	public void serializeXml(XmlSerializer writer, String namespace, String name, Locale locale)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(namespace, name);
		writer.startTag(namespace, "R").text(String.format(locale, "%f", r)).endTag(namespace, "R");
		writer.startTag(namespace, "G").text(String.format(locale, "%f", g)).endTag(namespace, "G");
		writer.startTag(namespace, "B").text(String.format(locale, "%f", b)).endTag(namespace, "B");
		writer.startTag(namespace, "A").text(String.format(locale, "%f", a)).endTag(namespace, "A");
		writer.endTag(namespace, name);
	}

	@Override
	public String toString() {
		return String.format(Helpers.EnUsCulture, "<%f, %f, %f, %f>", r, g, b, a);
	}

	public String toRGBString() {
		return String.format(Helpers.EnUsCulture, "<%f, %f, %f>", r, g, b);
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof Color4 && equals((Color4) obj);
	}

	public boolean equals(Color4 other) {
		return other != null && r == other.r && g == other.g && b == other.b && a == other.a;
	}

	@Override
	public int hashCode() {
		int hashCode = ((Float) r).hashCode();
		hashCode = hashCode * 31 + ((Float) g).hashCode();
		hashCode = hashCode * 31 + ((Float) b).hashCode();
		hashCode = hashCode * 31 + ((Float) a).hashCode();
		return hashCode;
	}

	public static boolean equals(Color4 lhs, Color4 rhs) {
		return lhs == null ? lhs == rhs : lhs.equals(rhs);
	}

	public static Color4 add(Color4 lhs, Color4 rhs) {
		lhs.r += rhs.r;
		lhs.g += rhs.g;
		lhs.b += rhs.b;
		lhs.a += rhs.a;
		lhs.clampValues();

		return lhs;
	}

	public static Color4 minus(Color4 lhs, Color4 rhs) {
		lhs.r -= rhs.r;
		lhs.g -= rhs.g;
		lhs.b -= rhs.b;
		lhs.a -= rhs.a;
		lhs.clampValues();

		return lhs;
	}

	public static Color4 multiply(Color4 lhs, Color4 rhs) {
		lhs.r *= rhs.r;
		lhs.g *= rhs.g;
		lhs.b *= rhs.b;
		lhs.a *= rhs.a;
		lhs.clampValues();

		return lhs;
	}

}
