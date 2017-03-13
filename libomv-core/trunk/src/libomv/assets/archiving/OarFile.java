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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import libomv.assets.AssetPrim;
import libomv.assets.AssetAnimation;
import libomv.assets.AssetBodypart;
import libomv.assets.AssetClothing;
import libomv.assets.AssetGesture;
import libomv.assets.AssetItem;
import libomv.assets.AssetItem.AssetType;
import libomv.assets.AssetLandmark;
import libomv.assets.AssetManager;
import libomv.assets.AssetManager.AssetDownload;
import libomv.assets.AssetManager.ImageDownload;
import libomv.assets.AssetManager.SourceType;
import libomv.assets.AssetMesh;
import libomv.assets.AssetNotecard;
import libomv.assets.AssetPrim.PrimObject;
import libomv.assets.AssetScriptBinary;
import libomv.assets.AssetScriptText;
import libomv.assets.AssetSound;
import libomv.assets.AssetTexture;
import libomv.primitives.Primitive;
import libomv.primitives.Primitive.PrimFlags;
import libomv.primitives.TextureEntry.TextureEntryFace;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.TimeoutEvent;

public class OarFile
{
    //public delegate void AssetLoadedCallback(Asset asset, long bytesRead, long totalBytes);
    //public delegate void TerrainLoadedCallback(float[,] terrain, long bytesRead, long totalBytes);
    //public delegate void SceneObjectLoadedCallback(AssetPrim linkset, long bytesRead, long totalBytes);
    //public delegate void SettingsLoadedCallback(string regionName, RegionSettings settings);

    // #region Archive Loading

	/*
    public static void UnpackageArchive(String filename, AssetLoadedCallback assetCallback, TerrainLoadedCallback terrainCallback,
        SceneObjectLoadedCallback objectCallback, SettingsLoadedCallback settingsCallback)
    {
        int successfulAssetRestores = 0;
        int failedAssetRestores = 0;

        try
        {
            InputStream fileStream = new FileInputStream(filename);
            if (fileStream != null)
            {
                GZipInputStream loadStream = new GZipInputStream(fileStream);
                if (loadStream != null)
                {
                    TarArchiveReader archive = new TarArchiveReader(loadStream);

                    String filePath;
                    byte[] data;
                    TarArchiveReader.TarEntryType entryType;

                    while ((data = archive.ReadEntry(out filePath, out entryType)) != null)
                    {
                        if (filePath.startsWith(ArchiveConstants.OBJECTS_PATH))
                        {
                            // Deserialize the XML bytes
                            if (objectCallback != null)
                                LoadObjects(data, objectCallback, fileStream.Position, fileStream.Length);
                        }
                        else if (filePath.startsWith(ArchiveConstants.ASSETS_PATH))
                        {
                            if (assetCallback != null)
                            {
                                if (LoadAsset(filePath, data, assetCallback, fileStream.Position, fileStream.Length))
                                    successfulAssetRestores++;
                                else
                                    failedAssetRestores++;
                            }
                        }
                        else if (filePath.startsWith(ArchiveConstants.TERRAINS_PATH))
                        {
                            if (terrainCallback != null)
                                LoadTerrain(filePath, data, terrainCallback, fileStream.Position, fileStream.Length);
                        }
                        else if (filePath.startsWith(ArchiveConstants.SETTINGS_PATH))
                        {
                            if (settingsCallback != null)
                                LoadRegionSettings(filePath, data, settingsCallback);
                        }
                    }

                    archive.Close();
                }
            }
        }
        catch (Exception e)
        {
            Logger.Log("[OarFile] Error loading OAR file: ", Logger.LogLevel.Error, e);
            return;
        }

        if (failedAssetRestores > 0)
            Logger.Log(String.Format("[OarFile]: Failed to load {0} assets", failedAssetRestores), Helpers.LogLevel.Warning);
    }
*/
    private static boolean LoadAsset(String assetPath, byte[] data, AssetLoadedCallback assetCallback, long bytesRead, long totalBytes)
    {
        // Right now we're nastily obtaining the UUID from the filename
        String filename = assetPath.remove(0, ArchiveConstants.ASSETS_PATH.length());
        int i = filename.lastIndexOf(ArchiveConstants.ASSET_EXTENSION_SEPARATOR);

        if (i == -1)
        {
            Logger.Log(String.format(
                "[OarFile]: Could not find extension information in asset path %s since it's missing the separator %s. Skipping",
                assetPath, ArchiveConstants.ASSET_EXTENSION_SEPARATOR), Helpers.LogLevel.Warning);
            return false;
        }

        String extension = filename.substring(i);
        UUID uuid;
        UUID.TryParse(filename.Remove(filename.length() - extension.length()), out uuid);

        if (ArchiveConstants.EXTENSION_TO_ASSET_TYPE.containsKey(extension))
        {
            AssetType assetType = ArchiveConstants.EXTENSION_TO_ASSET_TYPE.get(extension);
            AssetItem asset = null;

            switch (assetType)
            {
                case Animation:
                    asset = new AssetAnimation(uuid, data);
                    break;
                case Bodypart:
                    asset = new AssetBodypart(uuid, data);
                    break;
                case Clothing:
                    asset = new AssetClothing(uuid, data);
                    break;
                case Gesture:
                    asset = new AssetGesture(uuid, data);
                    break;
                case Landmark:
                    asset = new AssetLandmark(uuid, data);
                    break;
                case LSLBytecode:
                    asset = new AssetScriptBinary(uuid, data);
                    break;
                case LSLText:
                    asset = new AssetScriptText(uuid, data);
                    break;
                case Notecard:
                    asset = new AssetNotecard(uuid, data);
                    break;
                case Object:
                    asset = new AssetPrim(uuid, data);
                    break;
                case Sound:
                    asset = new AssetSound(uuid, data);
                    break;
                case Texture:
                    asset = new AssetTexture(uuid, data);
                    break;
                case Mesh:
                    asset = new AssetMesh(uuid, data);
                    break;
                default:
                    Logger.Log("[OarFile] Unhandled asset type " + assetType, Logger.LogLevel.Error);
                    break;
            }

            if (asset != null)
            {
                assetCallback(asset, bytesRead, totalBytes);
                return true;
            }
        }

        Logger.Log("[OarFile] Failed to load asset", Helpers.LogLevel.Warning);
        return false;
    }

    private static boolean LoadRegionSettings(String filePath, byte[] data, SettingsLoadedCallback settingsCallback)
    {
        RegionSettings settings = null;
        bool loaded = false;

        try
        {
            using (MemoryStream stream = new MemoryStream(data))
                settings = RegionSettings.FromStream(stream);
            loaded = true;
        }
        catch (Exception ex)
        {
            Logger.Log("[OarFile] Failed to parse region settings file " + filePath + ": " + ex.Message, Helpers.LogLevel.Warning);
        }

        // Parse the region name out of the filename
        string regionName = Path.GetFileNameWithoutExtension(filePath);

        if (loaded)
            settingsCallback(regionName, settings);

        return loaded;
    }

    private static bool LoadTerrain(string filePath, byte[] data, TerrainLoadedCallback terrainCallback, long bytesRead, long totalBytes)
    {
        float[,] terrain = new float[256, 256];
        bool loaded = false;

        switch (Path.GetExtension(filePath))
        {
            case ".r32":
            case ".f32":
                // RAW32
                if (data.Length == 256 * 256 * 4)
                {
                    int pos = 0;
                    for (int y = 0; y < 256; y++)
                    {
                        for (int x = 0; x < 256; x++)
                        {
                            terrain[y, x] = Utils.Clamp(Utils.BytesToFloat(data, pos), 0.0f, 255.0f);
                            pos += 4;
                        }
                    }

                    loaded = true;
                }
                else
                {
                    Logger.Log("[OarFile] RAW32 terrain file " + filePath + " has the wrong number of bytes: " + data.Length,
                        Helpers.LogLevel.Warning);
                }
                break;
            case ".ter":
                // Terragen
            case ".raw":
                // LLRAW
            case ".jpg":
            case ".jpeg":
                // JPG
            case ".bmp":
                // BMP
            case ".png":
                // PNG
            case ".gif":
                // GIF
            case ".tif":
            case ".tiff":
                // TIFF
            default:
                Logger.Log("[OarFile] Unrecognized terrain format in " + filePath, Helpers.LogLevel.Warning);
                break;
        }

        if (loaded)
            terrainCallback(terrain, bytesRead, totalBytes);

        return loaded;
    }

    public static void LoadObjects(byte[] objectData, SceneObjectLoadedCallback objectCallback, long bytesRead, long totalBytes)
    {
        XmlDocument doc = new XmlDocument();

        using (XmlTextReader reader = new XmlTextReader(new MemoryStream(objectData)))
        {
            reader.WhitespaceHandling = WhitespaceHandling.None;
            doc.Load(reader);
        }

        XmlNode rootNode = doc.FirstChild;

        if (rootNode.LocalName.Equals("scene"))
        {
            foreach (XmlNode node in rootNode.ChildNodes)
            {
                AssetPrim linkset = new AssetPrim(node.OuterXml);
                if (linkset != null)
                    objectCallback(linkset, bytesRead, totalBytes);
            }
        }
        else
        {
            AssetPrim linkset = new AssetPrim(rootNode.OuterXml);
            if (linkset != null)
                objectCallback(linkset, bytesRead, totalBytes);
        }
    }

    #endregion Archive Loading

    #region Archive Saving

    public static void PackageArchive(string directoryName, string filename)
    {
        const string ARCHIVE_XML = "<?xml version=\"1.0\" encoding=\"utf-16\"?>\n<archive major_version=\"0\" minor_version=\"1\" />";

        TarArchiveWriter archive = new TarArchiveWriter(new GZipStream(new FileStream(filename, FileMode.Create), CompressionMode.Compress));

        // Create the archive.xml file
        archive.WriteFile("archive.xml", ARCHIVE_XML);

        // Add the assets
        string[] files = Directory.GetFiles(directoryName + "/" + ArchiveConstants.ASSETS_PATH);
        foreach (string file in files)
            archive.WriteFile(ArchiveConstants.ASSETS_PATH + Path.GetFileName(file), File.ReadAllBytes(file));

        // Add the objects
        files = Directory.GetFiles(directoryName + "/" + ArchiveConstants.OBJECTS_PATH);
        foreach (string file in files)
            archive.WriteFile(ArchiveConstants.OBJECTS_PATH + Path.GetFileName(file), File.ReadAllBytes(file));

        // Add the terrain(s)
        files = Directory.GetFiles(directoryName + "/" + ArchiveConstants.TERRAINS_PATH);
        foreach (string file in files)
            archive.WriteFile(ArchiveConstants.TERRAINS_PATH + Path.GetFileName(file), File.ReadAllBytes(file));

        // Add the parcels(s)
        files = Directory.GetFiles(directoryName + "/" + ArchiveConstants.LANDDATA_PATH);
        foreach (string file in files)
            archive.WriteFile(ArchiveConstants.LANDDATA_PATH + Path.GetFileName(file), File.ReadAllBytes(file));

        // Add the setting(s)
        files = Directory.GetFiles(directoryName + "/" + ArchiveConstants.SETTINGS_PATH);
        foreach (string file in files)
            archive.WriteFile(ArchiveConstants.SETTINGS_PATH + Path.GetFileName(file), File.ReadAllBytes(file));

        archive.Close();
    }

    public static void SaveTerrain(Simulator sim, string terrainPath)
    {
        if (Directory.Exists(terrainPath))
            Directory.Delete(terrainPath, true);
        Thread.Sleep(100);
        Directory.CreateDirectory(terrainPath);
        Thread.Sleep(100);
        FileInfo file = new FileInfo(Path.Combine(terrainPath, sim.Name + ".r32"));
        FileStream s = file.Open(FileMode.Create, FileAccess.Write);
        SaveTerrainStream(s, sim);

        s.Close();
    }

    private static void SaveTerrainStream(Stream s, Simulator sim)
    {
        BinaryWriter bs = new BinaryWriter(s);
        
        int y;
        for (y = 0; y < 256; y++)
        {
            int x;
            for (x = 0; x < 256; x++)
            {
                float height;
                sim.TerrainHeightAtPoint(x, y, out height);
                bs.Write(height);
            }
        }

        bs.Close();
    }

    public static void SaveParcels(Simulator sim, String parcelPath)
    {
        if (Directory.Exists(parcelPath))
            Directory.Delete(parcelPath, true);
        Thread.Sleep(100);
        Directory.CreateDirectory(parcelPath);
        Thread.Sleep(100);
        sim.Parcels.ForEach((Parcel parcel) =>
        {
                UUID globalID = UUID.Random();
                SerializeParcel(parcel, globalID, Path.Combine(parcelPath, globalID + ".xml"));
        });
    }

    private static void SerializeParcel(Parcel parcel, UUID globalID, string filename)
    {
        StringWriter sw = new StringWriter();
        XmlTextWriter xtw = new XmlTextWriter(sw) { Formatting = Formatting.Indented };

        xtw.WriteStartDocument();
        xtw.startTag(null, "LandData");

        xtw.WriteElementString("Area", Convert.ToString(parcel.Area));
        xtw.WriteElementString("AuctionID", Convert.ToString(parcel.AuctionID));
        xtw.WriteElementString("AuthBuyerID", parcel.AuthBuyerID.ToString());
        xtw.WriteElementString("Category", Convert.ToString((sbyte)parcel.Category));
        TimeSpan t = parcel.ClaimDate.ToUniversalTime() - Utils.Epoch;
        xtw.WriteElementString("ClaimDate", Convert.ToString((int)t.TotalSeconds));
        xtw.WriteElementString("ClaimPrice", Convert.ToString(parcel.ClaimPrice));
        xtw.WriteElementString("GlobalID", globalID.ToString());
        xtw.WriteElementString("GroupID", parcel.GroupID.ToString());
        xtw.WriteElementString("IsGroupOwned", Convert.ToString(parcel.IsGroupOwned));
        xtw.WriteElementString("Bitmap", Convert.ToBase64String(parcel.Bitmap));
        xtw.WriteElementString("Description", parcel.Desc);
        xtw.WriteElementString("Flags", Convert.ToString((uint)parcel.Flags));
        xtw.WriteElementString("LandingType", Convert.ToString((byte)parcel.Landing));
        xtw.WriteElementString("Name", parcel.Name);
        xtw.WriteElementString("Status", Convert.ToString((sbyte)parcel.Status));
        xtw.WriteElementString("LocalID", parcel.LocalID.ToString());
        xtw.WriteElementString("MediaAutoScale", Convert.ToString(parcel.Media.MediaAutoScale ? 1 : 0));
        xtw.WriteElementString("MediaID", parcel.Media.MediaID.ToString());
        xtw.WriteElementString("MediaURL", parcel.Media.MediaURL);
        xtw.WriteElementString("MusicURL", parcel.MusicURL);
        xtw.WriteElementString("OwnerID", parcel.OwnerID.ToString());

        xtw.startTag(null, "ParcelAccessList");
        foreach (ParcelManager.ParcelAccessEntry pal in parcel.AccessBlackList)
        {
            xtw.startTag(null, "ParcelAccessEntry");
            xtw.WriteElementString("AgentID", pal.AgentID.ToString());
            xtw.WriteElementString("Time", pal.Time.ToString("s"));
            xtw.WriteElementString("AccessList", Convert.ToString((uint)pal.Flags));
            xtw.WriteEndElement();
        }
        foreach (ParcelManager.ParcelAccessEntry pal in parcel.AccessWhiteList)
        {
            xtw.startTag(null, "ParcelAccessEntry");
            xtw.WriteElementString("AgentID", pal.AgentID.ToString());
            xtw.WriteElementString("Time", pal.Time.ToString("s"));
            xtw.WriteElementString("AccessList", Convert.ToString((uint)pal.Flags));
            xtw.WriteEndElement();
        }
        xtw.WriteEndElement();

        xtw.WriteElementString("PassHours", Convert.ToString(parcel.PassHours));
        xtw.WriteElementString("PassPrice", Convert.ToString(parcel.PassPrice));
        xtw.WriteElementString("SalePrice", Convert.ToString(parcel.SalePrice));
        xtw.WriteElementString("SnapshotID", parcel.SnapshotID.ToString());
        xtw.WriteElementString("UserLocation", parcel.UserLocation.ToString());
        xtw.WriteElementString("UserLookAt", parcel.UserLookAt.ToString());
        xtw.WriteElementString("Dwell", "0");
        xtw.WriteElementString("OtherCleanTime", Convert.ToString(parcel.OtherCleanTime));

        xtw.WriteEndElement();

        xtw.Close();
        sw.Close();
        File.WriteAllText(filename, sw.ToString());
    }

    public static void SaveRegionSettings(Simulator sim, string settingsPath)
    {
        if (Directory.Exists(settingsPath))
            Directory.Delete(settingsPath, true);
        Thread.Sleep(100);
        Directory.CreateDirectory(settingsPath);
        Thread.Sleep(100);

        RegionSettings settings = new RegionSettings();
        //settings.AgentLimit;
        settings.AllowDamage = (sim.Flags & RegionFlags.AllowDamage) == RegionFlags.AllowDamage;
        //settings.AllowLandJoinDivide;
        settings.AllowLandResell = (sim.Flags & RegionFlags.BlockLandResell) != RegionFlags.BlockLandResell;
        settings.BlockFly = (sim.Flags & RegionFlags.NoFly) == RegionFlags.NoFly;
        settings.BlockLandShowInSearch = (sim.Flags & RegionFlags.BlockParcelSearch) == RegionFlags.BlockParcelSearch;
        settings.BlockTerraform = (sim.Flags & RegionFlags.BlockTerraform) == RegionFlags.BlockTerraform;
        settings.DisableCollisions = (sim.Flags & RegionFlags.SkipCollisions) == RegionFlags.SkipCollisions;
        settings.DisablePhysics = (sim.Flags & RegionFlags.SkipPhysics) == RegionFlags.SkipPhysics;
        settings.DisableScripts = (sim.Flags & RegionFlags.SkipScripts) == RegionFlags.SkipScripts;
        settings.FixedSun = (sim.Flags & RegionFlags.SunFixed) == RegionFlags.SunFixed;
        settings.MaturityRating = (int)(sim.Access & SimAccess.Mature & SimAccess.Adult & SimAccess.PG);
        //settings.ObjectBonus;
        settings.RestrictPushing = (sim.Flags & RegionFlags.RestrictPushObject) == RegionFlags.RestrictPushObject;
        settings.TerrainDetail0 = sim.TerrainDetail0;
        settings.TerrainDetail1 = sim.TerrainDetail1;
        settings.TerrainDetail2 = sim.TerrainDetail2;
        settings.TerrainDetail3 = sim.TerrainDetail3;
        settings.TerrainHeightRange00 = sim.TerrainHeightRange00;
        settings.TerrainHeightRange01 = sim.TerrainHeightRange01;
        settings.TerrainHeightRange10 = sim.TerrainHeightRange10;
        settings.TerrainHeightRange11 = sim.TerrainHeightRange11;
        settings.TerrainStartHeight00 = sim.TerrainStartHeight00;
        settings.TerrainStartHeight01 = sim.TerrainStartHeight01;
        settings.TerrainStartHeight10 = sim.TerrainStartHeight10;
        settings.TerrainStartHeight11 = sim.TerrainStartHeight11;
        //settings.UseEstateSun;
        settings.WaterHeight = sim.WaterHeight;

        settings.ToXML(Path.Combine(settingsPath, sim.Name + ".xml"));
    }

    public static void SavePrims(AssetManager manager, List<AssetPrim> prims, String primsPath, String assetsPath)
    {
        Map<UUID, UUID> textureList = new HashMap<UUID, UUID>();

        // Delete all of the old linkset files
        try
        {
        	Directory.Delete(primsPath, true);
        }
        catch (Exception ex) { }

        Thread.Sleep(100);
        // Create a new folder for the linkset files
        try { Directory.CreateDirectory(primsPath); }
        catch (Exception ex)
        {
            Logger.Log("Failed saving prims: " + ex.Message, Helpers.LogLevel.Error);
            return;
        }
        Thread.Sleep(100);
        try
        {
            foreach (AssetPrim assetPrim in prims)
            {
                SavePrim(assetPrim, Path.Combine(primsPath, "Primitive_" + assetPrim.Parent.ID + ".xml"));

                CollectTextures(assetPrim.Parent, textureList);
                if (assetPrim.Children != null)
                {
                    foreach (PrimObject child in assetPrim.Children)
                        CollectTextures(child, textureList);
                }
            }

            SaveAssets(manager, AssetType.Texture, new List<UUID>(textureList.Keys), assetsPath);
        }
        catch
        {
        }
    }

    static void CollectTextures(PrimObject prim, Map<UUID, UUID> textureList)
    {
        if (prim.Textures != null)
        {
            // Add all of the textures on this prim to the save list
            if (prim.Textures.defaultTexture != null)
                textureList.put(prim.Textures.defaultTexture.getTextureID(), prim.Textures.defaultTexture.getTextureID());

            if (prim.Textures.faceTextures != null)
            {
                for (int i = 0; i < prim.Textures.faceTextures.length; i++)
                {
                    TextureEntryFace face = prim.Textures.faceTextures[i];
                    if (face != null)
                        textureList. put(face.getTextureID(), face.getTextureID());
                }
            }
            if (prim.Sculpt != null && !prim.Sculpt.Texture.equals(UUID.Zero))
                textureList.put(prim.Sculpt.Texture, prim.Sculpt.Texture);
        }
    }

    public static void ClearAssetFolder(File directory)
    {
        // Delete the assets folder
        try
        {
        	
        	Helpers.deleteDirectory(directory);
        }
        catch (Exception ex) { }
        Thread.sleep(100);

        // Create a new assets folder
        try 
        {
        	directory.mkdir();
        }
        catch (Exception ex)
        {
            Logger.Log("Failed saving assets: ", Logger.LogLevel.Error, ex);
            return;
        }
        Thread.sleep(100);
    }

    public static void SaveAssets(AssetManager assetManager, AssetType assetType, List<UUID> assets, String assetsPath)
    {
        int count = 0;
        List<UUID> remainingTextures = new ArrayList<UUID>(assets);
        TimeoutEvent<Boolean> AllPropertiesReceived = new TimeoutEvent<Boolean>();
        for (int i = 0; i < assets.size(); i++)
        {
            UUID texture = assets.get(i);
            if (assetType.equals(AssetType.Texture))
            {
                assetManager.RequestImage(texture, new Callback<ImageDownload>()
                {
        			@Override
        			public boolean callback(ImageDownload transfer)
        			{
        				if (transfer.Success)
        				{
        					String extension = Helpers.EmptyString;

        					if (transfer.AssetData != null)
        					{
        						if (ArchiveConstants.ASSET_TYPE_TO_EXTENSION.containsKey(assetType))
        							extension = ArchiveConstants.ASSET_TYPE_TO_EXTENSION.get(assetType);

        						OutputStream stream = new FileOutputStream(new File(assetsPath, texture.toString() + extension));
        						stream.write(transfer.AssetData);
        						stream.close();
        						remainingTextures.remove(texture);
        						if (remainingTextures.size() == 0)
        							AllPropertiesReceived.set(true);
        						++count;
        					}
        					else
        					{
        						System.out.println("Missing asset " + texture);
        					}
        				}
        				return true;
        			}
                });
            }
            else
            {
                assetManager.RequestAsset(texture, assetType, false, new Callback<AssetDownload>()
                {
           			@Override
        			public boolean callback(AssetDownload transfer)
        			{
        				if (transfer.Success)
        				{
        					String extension = Helpers.EmptyString;

        					if (asset != null)
        					{
        						if (ArchiveConstants.ASSET_TYPE_TO_EXTENSION.ContainsKey(assetType))
        							extension = ArchiveConstants.ASSET_TYPE_TO_EXTENSION.get(assetType);

        						OutputStream stream = new FileOutputStream(new File(assetsPath, texture.toString() + extension));
        						stream.write(transfer.AssetData);
        						stream.close();
        						remainingTextures.remove(asset.AssetID);
        						if (remainingTextures.size() == 0)
        							AllPropertiesReceived.set(true);
        						++count;
        					}
        					else
        					{
           						System.out.println("Missing asset " + texture);
        					}
        				}
        				return true;
        			}
                });
            }

//            Thread.sleep(200);
 //           if (i % 5 == 0)
//                Thread.sleep(250);
        }
        AllPropertiesReceived.wait(5000 + 350 * assets.count);

        Logger.Log("Copied " + count + " textures to the asset archive folder", Logger.LogLevel.Info);
    }

    public static void SaveSimAssets(AssetManager assetManager, AssetType assetType, UUID assetID, UUID itemID, UUID primID, String assetsPath)
    {
        int count = 0;
        TimeoutEvent<Boolean> AllPropertiesReceived = new TimeoutEvent<Boolean>();

        assetManager.RequestAsset(assetID, itemID, primID, assetType, false, SourceType.SimInventoryItem, UUID.GenerateUUID(), new Callback<AssetDownload>()
        {
			@Override
			public boolean callback(AssetDownload transfer)
			{
				if (transfer.Success)
				{
					String extension = Helpers.EmptyString;

					if (ArchiveConstants.ASSET_TYPE_TO_EXTENSION.containsKey(assetType))
						extension = ArchiveConstants.ASSET_TYPE_TO_EXTENSION.get(assetType);

					if (transfer.AssetData != null)
					{
						OutputStream stream = new FileOutputStream(new File(assetsPath, assetID.toString() + extension));
						stream.write(transfer.AssetData);
						stream.close();
						++count;
					}
					AllPropertiesReceived.set(transfer.AssetData != null);
					return true;
				}
			}
        });
        AllPropertiesReceived.wait(5000);

        Logger.Log("Copied " + count + " textures to the asset archive folder", Logger.LogLevel.Info);
    }

    static void SavePrim(AssetPrim prim, String filename)
    {
        try
        {
            Writer writer = new FileWriter(filename);
            {
        		XmlSerializer xmlWriter = XmlPullParserFactory.newInstance().newSerializer();
        		xmlWriter.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "    ");
        		xmlWriter.setOutput(writer);
        		xmlWriter.startDocument(Helpers.UTF8_ENCODING, null);
                prim.encodeXml(xmlWriter);
                xmlWriter.flush();
                writer.close();
            }
        }
        catch (Exception ex)
        {
            Logger.Log("Failed saving linkset: ", Logger.LogLevel.Error, ex);
        }
    }

    static void WriteUUID(XmlSerializer writer, String name, UUID id)
    {
        writer.startTag(null, name);
        WriteElement(writer, "UUID", id.toString());
        writer.endTag(null, name);
    }

   
    static void WriteElement(XmlSerializer writer, String name, String value)
    {
    	writer.startTag(null,  name).text(value).endTag(null,  value);
    }
    // #endregion Archive Saving
}
