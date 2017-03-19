package archive;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import junit.framework.TestCase;
import libomv.assets.archiving.OarFile;
import libomv.assets.archiving.OarFile.AssetLoadedData;
import libomv.assets.archiving.OarFile.SceneObjectLoadedData;
import libomv.assets.archiving.OarFile.SettingsLoadedData;
import libomv.assets.archiving.OarFile.TerrainLoadedData;
import libomv.assets.archiving.RegionSettings;
import libomv.types.UUID;
import libomv.utils.Callback;

public class OARUnpack extends TestCase
{
	public class AssetLoadedCallback implements Callback<AssetLoadedData>
	{
		@Override
		public boolean callback(AssetLoadedData params)
		{
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	public class TerrainLoadedCallback implements Callback<TerrainLoadedData>
	{
		@Override
		public boolean callback(TerrainLoadedData params)
		{
			// TODO Auto-generated method stub
			return false;
		}
	}

	public class SceneObjectLoadedCallback implements Callback<SceneObjectLoadedData>
	{
		@Override
		public boolean callback(SceneObjectLoadedData params)
		{
			// TODO Auto-generated method stub
			return false;
		}
	}

	public class SettingsLoadedCallback implements Callback<SettingsLoadedData>
	{
		@Override
		public boolean callback(SettingsLoadedData params)
		{
			// TODO Auto-generated method stub
			return false;
		}
	}

	public void testRegionSettings() throws Exception
	{
		String xml = "<?xml version=\"1.0\" encoding=\"utf-16\"?>\n" +
                     "<RegionSettings>\n" +
                     "  <General>\n" +
                     "    <AllowDamage>False</AllowDamage>\n" +
                     "    <AllowLandResell>True</AllowLandResell>\n" +
                     "    <AllowLandJoinDivide>True</AllowLandJoinDivide>\n" +
                     "    <BlockFly>False</BlockFly>\n" +
                     "    <BlockLandShowInSearch>False</BlockLandShowInSearch>\n" +
                     "    <BlockTerraform>False</BlockTerraform>\n" +
                     "    <DisableCollisions>False</DisableCollisions>\n" +
                     "    <DisablePhysics>False</DisablePhysics>\n" +
                     "    <DisableScripts>False</DisableScripts>\n" +
                     "    <MaturityRating>1</MaturityRating>\n" +
                     "    <RestrictPushing>False</RestrictPushing>\n" +
                     "    <AgentLimit>40</AgentLimit>\n" +
                     "    <ObjectBonus>1</ObjectBonus>\n" +
                     "  </General>\n" +
                     "  <GroundTextures>\n" +
                     "    <Texture1>7c2ee836-dc42-4100-be85-ff983385d78b</Texture1>\n" +
                     "    <Texture2>abb783e6-3e93-26c0-248a-247666855da3</Texture2>\n" +
                     "    <Texture3>179cdabd-398a-9b6b-1391-4dc333ba321f</Texture3>\n" +
                     "    <Texture4>beb169c7-11ea-fff2-efe5-0f24dc881df2</Texture4>\n" +
                     "    <ElevationLowSW>8</ElevationLowSW>\n" +
                     "    <ElevationLowNW>8</ElevationLowNW>\n" +
                     "    <ElevationLowSE>18</ElevationLowSE>\n" +
                     "    <ElevationLowNE>8</ElevationLowNE>\n" +
                     "    <ElevationHighSW>40</ElevationHighSW>\n" +
                     "    <ElevationHighNW>40</ElevationHighNW>\n" +
                     "    <ElevationHighSE>40</ElevationHighSE>\n" +
                     "    <ElevationHighNE>40</ElevationHighNE>\n" +
                     "  </GroundTextures>\n" +
                     "  <Terrain>\n" +
                     "    <WaterHeight>20</WaterHeight>\n" +
                     "    <TerrainRaiseLimit>100</TerrainRaiseLimit>\n" +
                     "    <TerrainLowerLimit>-100</TerrainLowerLimit>\n" +
                     "    <UseEstateSun>True</UseEstateSun>\n" +
                     "    <FixedSun>False</FixedSun>\n" +
                     "  </Terrain>\n" +
                     "</RegionSettings>";
		
		Reader reader = new StringReader(xml);
		RegionSettings settings = RegionSettings.fromReader(reader);
		reader.close();
		
		assertNotNull(settings);
		assertTrue(settings.AllowLandResell);
		assertTrue(settings.AllowLandJoinDivide);
		assertTrue(settings.AgentLimit == 40);
		assertTrue(settings.TerrainDetail0.equals(new UUID("7c2ee836-dc42-4100-be85-ff983385d78b")));
		assertTrue(settings.TerrainDetail1.equals(new UUID("abb783e6-3e93-26c0-248a-247666855da3")));
		assertTrue(settings.TerrainDetail2.equals(new UUID("179cdabd-398a-9b6b-1391-4dc333ba321f")));
		assertTrue(settings.TerrainDetail3.equals(new UUID("beb169c7-11ea-fff2-efe5-0f24dc881df2")));
	}
	
	public void testUnpackArchive() throws Exception
	{
		URL fileName = this.getClass().getResource("opensim-openvce-2010-07-07.oar");
		File file = new File(fileName.toURI());
	
        OarFile.UnpackageArchive(file, new AssetLoadedCallback(), new TerrainLoadedCallback(), new SceneObjectLoadedCallback(), new SettingsLoadedCallback());
	}
}
