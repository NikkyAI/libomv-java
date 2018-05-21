/**
 * Copyright (c) 2006-2016, openmetaverse.org
 * Copyright (c) 2016-2017, Frederick Martian
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
package libomv.assets.archiving;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import libomv.types.UUID;
import libomv.utils.Helpers;

public class RegionSettings {
	public boolean allowDamage;
	public boolean allowLandResell;
	public boolean allowLandJoinDivide;
	public boolean blockFly;
	public boolean blockLandShowInSearch;
	public boolean blockTerraform;
	public boolean disableCollisions;
	public boolean disablePhysics;
	public boolean disableScripts;
	public int maturityRating;
	public boolean restrictPushing;
	public int agentLimit;
	public float objectBonus;

	public UUID terrainDetail0;
	public UUID terrainDetail1;
	public UUID terrainDetail2;
	public UUID terrainDetail3;
	public float terrainHeightRange00;
	public float terrainHeightRange01;
	public float terrainHeightRange10;
	public float terrainHeightRange11;
	public float terrainStartHeight00;
	public float terrainStartHeight01;
	public float terrainStartHeight10;
	public float terrainStartHeight11;

	public float waterHeight;
	public float terrainRaiseLimit;
	public float terrainLowerLimit;
	public boolean useEstateSun;
	public boolean fixedSun;

	public static RegionSettings fromReader(Reader reader) throws XmlPullParserException, IOException {
		XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
		parser.setInput(reader);
		return fromXml(parser);
	}

	public static RegionSettings fromStream(InputStream stream, String encoding)
			throws XmlPullParserException, IOException {
		XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
		parser.setInput(stream, encoding);
		return fromXml(parser);
	}

	protected static RegionSettings fromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
		RegionSettings settings = new RegionSettings();
		String name;

		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "RegionSettings");

		while (parser.nextTag() == XmlPullParser.START_TAG) {
			name = parser.getName();
			if (name.equals("General")) {
				while (parser.nextTag() == XmlPullParser.START_TAG) {
					name = parser.getName();
					if (name.equals("AllowDamage"))
						settings.allowDamage = Helpers.tryParseBoolean(parser.nextText().trim());
					else if (name.equals("AllowLandResell"))
						settings.allowLandResell = Helpers.tryParseBoolean(parser.nextText().trim());
					else if (name.equals("AllowLandJoinDivide"))
						settings.allowLandJoinDivide = Helpers.tryParseBoolean(parser.nextText().trim());
					else if (name.equals("BlockFly"))
						settings.blockFly = Helpers.tryParseBoolean(parser.nextText().trim());
					else if (name.equals("BlockLandShowInSearch"))
						settings.blockLandShowInSearch = Helpers.tryParseBoolean(parser.nextText().trim());
					else if (name.equals("BlockTerraform"))
						settings.blockTerraform = Helpers.tryParseBoolean(parser.nextText().trim());
					else if (name.equals("DisableCollisions"))
						settings.disableCollisions = Helpers.tryParseBoolean(parser.nextText().trim());
					else if (name.equals("DisablePhysics"))
						settings.disablePhysics = Helpers.tryParseBoolean(parser.nextText().trim());
					else if (name.equals("DisableScripts"))
						settings.disableScripts = Helpers.tryParseBoolean(parser.nextText().trim());
					else if (name.equals("MaturityRating"))
						settings.maturityRating = Helpers.tryParseInt(parser.nextText().trim());
					else if (name.equals("RestrictPushing"))
						settings.restrictPushing = Helpers.tryParseBoolean(parser.nextText().trim());
					else if (name.equals("AgentLimit"))
						settings.agentLimit = Helpers.tryParseInt(parser.nextText().trim());
					else if (name.equals("ObjectBonus"))
						settings.objectBonus = Helpers.tryParseFloat(parser.nextText());
				}
				// at </General>
			} else if (name.equals("GroundTextures")) {
				while (parser.nextTag() == XmlPullParser.START_TAG) {
					name = parser.getName();
					if (name.equals("Texture1"))
						settings.terrainDetail0 = new UUID(parser);
					else if (name.equals("Texture2"))
						settings.terrainDetail1 = new UUID(parser);
					else if (name.equals("Texture3"))
						settings.terrainDetail2 = new UUID(parser);
					else if (name.equals("Texture4"))
						settings.terrainDetail3 = new UUID(parser);
					else if (name.equals("ElevationLowSW"))
						settings.terrainStartHeight00 = Helpers.tryParseFloat(parser.nextText());
					else if (name.equals("ElevationLowNW"))
						settings.terrainStartHeight01 = Helpers.tryParseFloat(parser.nextText());
					else if (name.equals("ElevationLowSE"))
						settings.terrainStartHeight10 = Helpers.tryParseFloat(parser.nextText());
					else if (name.equals("ElevationLowNE"))
						settings.terrainStartHeight11 = Helpers.tryParseFloat(parser.nextText());
					else if (name.equals("ElevationHighSW"))
						settings.terrainHeightRange00 = Helpers.tryParseFloat(parser.nextText());
					else if (name.equals("ElevationHighNW"))
						settings.terrainHeightRange01 = Helpers.tryParseFloat(parser.nextText());
					else if (name.equals("ElevationHighSE"))
						settings.terrainHeightRange10 = Helpers.tryParseFloat(parser.nextText());
					else if (name.equals("ElevationHighNE"))
						settings.terrainHeightRange11 = Helpers.tryParseFloat(parser.nextText());
				}
				// </GroundTextures>
			} else if (name.equals("Terrain")) {
				while (parser.nextTag() == XmlPullParser.START_TAG) {
					name = parser.getName();
					if (name.equals("WaterHeight"))
						settings.waterHeight = Helpers.tryParseFloat(parser.nextText());
					else if (name.equals("TerrainRaiseLimit"))
						settings.terrainRaiseLimit = Helpers.tryParseFloat(parser.nextText());
					else if (name.equals("TerrainLowerLimit"))
						settings.terrainLowerLimit = Helpers.tryParseFloat(parser.nextText());
					else if (name.equals("UseEstateSun"))
						settings.useEstateSun = Helpers.tryParseBoolean(parser.nextText());
					else if (name.equals("FixedSun"))
						settings.fixedSun = Helpers.tryParseBoolean(parser.nextText());
				}
				// at </Terrain>
			}
		}
		// at </RegionSettings>
		return settings;
	}

	public void toXML(File filename) throws IOException, XmlPullParserException {
		Writer fileWriter = new FileWriter(filename);
		XmlSerializer writer = XmlPullParserFactory.newInstance().newSerializer();
		writer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
		writer.setOutput(fileWriter);
		writer.startDocument(Helpers.UTF8_ENCODING, null);
		writer.startTag(null, "RegionSettings");

		writer.startTag(null, "General");
		writeBoolean(writer, "AllowDamage", allowDamage);
		writeBoolean(writer, "AllowLandResell", allowLandResell);
		writeBoolean(writer, "AllowLandJoinDivide", allowLandJoinDivide);
		writeBoolean(writer, "BlockFly", blockFly);
		writeBoolean(writer, "BlockLandShowInSearch", blockLandShowInSearch);
		writeBoolean(writer, "BlockTerraform", blockTerraform);
		writeBoolean(writer, "DisableCollisions", disableCollisions);
		writeBoolean(writer, "DisablePhysics", disablePhysics);
		writeBoolean(writer, "DisableScripts", disableScripts);
		writeInteger(writer, "MaturityRating", maturityRating);
		writeBoolean(writer, "RestrictPushing", restrictPushing);
		writeInteger(writer, "AgentLimit", agentLimit);
		writeFloat(writer, "ObjectBonus", objectBonus);
		writer.endTag(null, "General");

		writer.startTag(null, "GroundTextures");
		writeString(writer, "Texture1", terrainDetail0.toString());
		writeString(writer, "Texture2", terrainDetail1.toString());
		writeString(writer, "Texture3", terrainDetail2.toString());
		writeString(writer, "Texture4", terrainDetail3.toString());
		writeFloat(writer, "ElevationLowSW", terrainStartHeight00);
		writeFloat(writer, "ElevationLowNW", terrainStartHeight01);
		writeFloat(writer, "ElevationLowSE", terrainStartHeight10);
		writeFloat(writer, "ElevationLowNE", terrainStartHeight11);
		writeFloat(writer, "ElevationHighSW", terrainHeightRange00);
		writeFloat(writer, "ElevationHighNW", terrainHeightRange01);
		writeFloat(writer, "ElevationHighSE", terrainHeightRange10);
		writeFloat(writer, "ElevationHighNE", terrainHeightRange11);
		writer.endTag(null, "GroundTextures");

		writer.startTag(null, "Terrain");
		writeFloat(writer, "WaterHeight", waterHeight);
		writeFloat(writer, "TerrainRaiseLimit", terrainRaiseLimit);
		writeFloat(writer, "TerrainLowerLimit", terrainLowerLimit);
		writeBoolean(writer, "UseEstateSun", useEstateSun);
		writeBoolean(writer, "FixedSun", fixedSun);
		writer.endTag(null, "Terrain");

		writer.endTag(null, "RegionSettings");
		fileWriter.close();
	}

	private void writeBoolean(XmlSerializer writer, String tag, boolean value)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, tag).text(value ? "True" : "False").endTag(null, tag);
	}

	private void writeInteger(XmlSerializer writer, String tag, int value)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, tag).text(Integer.toString(value)).endTag(null, tag);
	}

	private void writeFloat(XmlSerializer writer, String tag, float value)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, tag).text(Float.toString(value)).endTag(null, tag);
	}

	private void writeString(XmlSerializer writer, String tag, String value)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, tag).text(value).endTag(null, tag);
	}
}
