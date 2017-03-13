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

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import libomv.types.UUID;
import libomv.utils.Helpers;

public class RegionSettings
{
    public boolean AllowDamage;
    public boolean AllowLandResell;
    public boolean AllowLandJoinDivide;
    public boolean BlockFly;
    public boolean BlockLandShowInSearch;
    public boolean BlockTerraform;
    public boolean DisableCollisions;
    public boolean DisablePhysics;
    public boolean DisableScripts;
    public int MaturityRating;
    public boolean RestrictPushing;
    public int AgentLimit;
    public float ObjectBonus;

    public UUID TerrainDetail0;
    public UUID TerrainDetail1;
    public UUID TerrainDetail2;
    public UUID TerrainDetail3;
    public float TerrainHeightRange00;
    public float TerrainHeightRange01;
    public float TerrainHeightRange10;
    public float TerrainHeightRange11;
    public float TerrainStartHeight00;
    public float TerrainStartHeight01;
    public float TerrainStartHeight10;
    public float TerrainStartHeight11;

    public float WaterHeight;
    public float TerrainRaiseLimit;
    public float TerrainLowerLimit;
    public boolean UseEstateSun;
    public boolean FixedSun;

    public static RegionSettings FromStream(InputStream stream) throws XmlPullParserException, IOException
    {
        RegionSettings settings = new RegionSettings();
        String name;
        
		XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
		parser.setInput(stream, Helpers.UTF8_ENCODING);
        
		parser.require(XmlPullParser.START_TAG, null, "RegionSettings");
		parser.nextTag();

		parser.require(XmlPullParser.START_TAG, null, "General");
		
		do
		{
			name = parser.getName();
            if (name.equals("AllowDamage"))
            	settings.AllowDamage = Helpers.TryParseBoolean(parser.nextText().trim());
            else if (name.equals("AllowLandResell"))
                settings.AllowLandResell = Helpers.TryParseBoolean(parser.nextText().trim());
            else if (name.equals("AllowLandJoinDivide"))
                    settings.AllowLandJoinDivide = Helpers.TryParseBoolean(parser.nextText().trim());
            else if (name.equals("BlockFly"))
                    settings.BlockFly = Helpers.TryParseBoolean(parser.nextText().trim());
            else if (name.equals("BlockLandShowInSearch"))
                    settings.BlockLandShowInSearch = Helpers.TryParseBoolean(parser.nextText().trim());
            else if (name.equals("BlockTerraform"))
                    settings.BlockTerraform = Helpers.TryParseBoolean(parser.nextText().trim());
            else if (name.equals("DisableCollisions"))
                    settings.DisableCollisions = Helpers.TryParseBoolean(parser.nextText().trim());
            else if (name.equals("DisablePhysics"))
                    settings.DisablePhysics = Helpers.TryParseBoolean(parser.nextText().trim());
            else if (name.equals("DisableScripts"))
                    settings.DisableScripts = Helpers.TryParseBoolean(parser.nextText().trim());
            else if (name.equals("MaturityRating"))
                    settings.MaturityRating = Helpers.TryParseInt(parser.nextText().trim());
            else if (name.equals("RestrictPushing"))
                    settings.RestrictPushing = Helpers.TryParseBoolean(parser.nextText().trim());
            else if (name.equals("AgentLimit"))
                    settings.AgentLimit = Helpers.TryParseInt(parser.nextText().trim());
            else if (name.equals("ObjectBonus"))
                    settings.ObjectBonus = Helpers.TryParseFloat(parser.getText());
		} while (parser.nextTag() == XmlPullParser.START_TAG);

		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "GroundTextures");
		do
		{
			name = parser.getName();
            if (name.equals("Texture1"))
                settings.TerrainDetail0 = new UUID(parser);
            else if (name.equals("Texture2"))
                settings.TerrainDetail1 = new UUID(parser);
            else if (name.equals("Texture3"))
                settings.TerrainDetail2 = new UUID(parser);
            else if (name.equals("Texture4"))
                settings.TerrainDetail3 = new UUID(parser);
            else if (name.equals("ElevationLowSW"))
                settings.TerrainStartHeight00 = Helpers.TryParseFloat(parser.nextText());
            else if (name.equals("ElevationLowNW"))
                settings.TerrainStartHeight01 = Helpers.TryParseFloat(parser.nextText());
            else if (name.equals("ElevationLowSE"))
                settings.TerrainStartHeight10 = Helpers.TryParseFloat(parser.nextText());
            else if (name.equals("ElevationLowNE"))
                settings.TerrainStartHeight11 = Helpers.TryParseFloat(parser.nextText());
            else if (name.equals("ElevationHighSW"))
                settings.TerrainHeightRange00 = Helpers.TryParseFloat(parser.nextText());
            else if (name.equals("ElevationHighNW"))
                settings.TerrainHeightRange01 = Helpers.TryParseFloat(parser.nextText());
            else if (name.equals("ElevationHighSE"))
                settings.TerrainHeightRange10 = Helpers.TryParseFloat(parser.nextText());
            else if (name.equals("ElevationHighNE"))
                settings.TerrainHeightRange11 = Helpers.TryParseFloat(parser.nextText());
        }
		while (parser.nextTag() == XmlPullParser.START_TAG);

		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "Terrain");
 
		do
		{
			name = parser.getName();
            if (name.equals("WaterHeight"))
                settings.WaterHeight = Helpers.TryParseFloat(parser.nextText());
            else if (name.equals("TerrainRaiseLimit"))
                settings.TerrainRaiseLimit = Helpers.TryParseFloat(parser.nextText());
            else if (name.equals("TerrainLowerLimit"))
                settings.TerrainLowerLimit = Helpers.TryParseFloat(parser.nextText());
            else if (name.equals("UseEstateSun"))
                settings.UseEstateSun = Helpers.TryParseBoolean(parser.nextText());
            else if (name.equals("FixedSun"))
                settings.FixedSun = Helpers.TryParseBoolean(parser.nextText());
        }
		while (parser.nextTag() == XmlPullParser.START_TAG);
        return settings;
    }

    public void ToXML(String filename) throws IOException, XmlPullParserException
    {
        Writer fileWriter = new FileWriter(filename);
		XmlSerializer writer = XmlPullParserFactory.newInstance().newSerializer();
   		writer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
		writer.setOutput(fileWriter);
        writer.startDocument(Helpers.UTF8_ENCODING, null);
        writer.startTag(null, "RegionSettings");

        writer.startTag(null, "General");
        WriteBoolean(writer, "AllowDamage", AllowDamage);
        WriteBoolean(writer, "AllowLandResell", AllowLandResell);
        WriteBoolean(writer, "AllowLandJoinDivide", AllowLandJoinDivide);
        WriteBoolean(writer, "BlockFly", BlockFly);
        WriteBoolean(writer, "BlockLandShowInSearch", BlockLandShowInSearch);
        WriteBoolean(writer, "BlockTerraform", BlockTerraform);
        WriteBoolean(writer, "DisableCollisions", DisableCollisions);
        WriteBoolean(writer, "DisablePhysics", DisablePhysics);
        WriteBoolean(writer, "DisableScripts", DisableScripts);
        WriteInteger(writer, "MaturityRating", MaturityRating);
        WriteBoolean(writer, "RestrictPushing", RestrictPushing);
        WriteInteger(writer, "AgentLimit", AgentLimit);
        WriteFloat(writer, "ObjectBonus", ObjectBonus);
        writer.endTag(null, "General");

        writer.startTag(null, "GroundTextures");
        WriteString(writer, "Texture1", TerrainDetail0.toString());
        WriteString(writer, "Texture2", TerrainDetail1.toString());
        WriteString(writer, "Texture3", TerrainDetail2.toString());
        WriteString(writer, "Texture4", TerrainDetail3.toString());
        WriteFloat(writer, "ElevationLowSW", TerrainStartHeight00);
        WriteFloat(writer, "ElevationLowNW", TerrainStartHeight01);
        WriteFloat(writer, "ElevationLowSE", TerrainStartHeight10);
        WriteFloat(writer, "ElevationLowNE", TerrainStartHeight11);
        WriteFloat(writer, "ElevationHighSW", TerrainHeightRange00);
        WriteFloat(writer, "ElevationHighNW", TerrainHeightRange01);
        WriteFloat(writer, "ElevationHighSE", TerrainHeightRange10);
        WriteFloat(writer, "ElevationHighNE", TerrainHeightRange11);
        writer.endTag(null, "GroundTextures");
            
        writer.startTag(null, "Terrain");
        WriteFloat(writer, "WaterHeight", WaterHeight);
        WriteFloat(writer, "TerrainRaiseLimit", TerrainRaiseLimit);
        WriteFloat(writer, "TerrainLowerLimit", TerrainLowerLimit);
        WriteBoolean(writer, "UseEstateSun", UseEstateSun);
        WriteBoolean(writer, "FixedSun", FixedSun);
        writer.endTag(null, "Terrain");

        writer.endTag(null, "RegionSettings");
        fileWriter.close();
    }

    private void WriteBoolean(XmlSerializer writer, String name, boolean value) throws IllegalArgumentException, IllegalStateException, IOException
    {
        writer.startTag(null, name).text(value ? "True" : "False").endTag(null, name);
    }
    
    private void WriteInteger(XmlSerializer writer, String name, int value) throws IllegalArgumentException, IllegalStateException, IOException
    {
        writer.startTag(null, name).text(Integer.toString(value)).endTag(null, name);
    }

    private void WriteFloat(XmlSerializer writer, String name, float value) throws IllegalArgumentException, IllegalStateException, IOException
    {
        writer.startTag(null, name).text(Float.toString(value)).endTag(null, name);
    }

    private void WriteString(XmlSerializer writer, String name, String value) throws IllegalArgumentException, IllegalStateException, IOException
    {
        writer.startTag(null, name).text(value).endTag(null, name);
    }
}
