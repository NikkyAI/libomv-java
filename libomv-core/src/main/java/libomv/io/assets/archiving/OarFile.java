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
package libomv.io.assets.archiving;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import libomv.assets.AssetItem;
import libomv.assets.AssetPrim;
import libomv.assets.AssetPrim.PrimObject;
import libomv.assets.archiving.ArchiveConstants;
import libomv.assets.archiving.RegionSettings;
import libomv.assets.archiving.TarArchiveReader;
import libomv.assets.archiving.TarArchiveWriter;
import libomv.io.SimulatorManager;
import libomv.io.assets.AssetManager;
import libomv.model.Parcel;
import libomv.model.asset.AssetDownload;
import libomv.model.asset.AssetType;
import libomv.model.asset.ImageDownload;
import libomv.model.asset.SourceType;
import libomv.model.parcel.ParcelAccessEntry;
import libomv.model.simulator.RegionFlags;
import libomv.primitives.TextureEntry.TextureEntryFace;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.PushbackInputStream;
import libomv.utils.RefObject;
import libomv.utils.TimeoutEvent;

public class OarFile {
	private static final Logger logger = Logger.getLogger(OarFile.class);

	public class AssetLoadedData {
		public AssetItem asset;
		public long bytesRead;
		public long totalBytes;

		public AssetLoadedData(AssetItem asset, long bytesRead, long totalBytes) {
			this.asset = asset;
			this.bytesRead = bytesRead;
			this.totalBytes = totalBytes;
		}
	};

	public class TerrainLoadedData {
		public float[][] terrain;
		public long bytesRead;
		public long totalBytes;

		public TerrainLoadedData(float[][] terrain, long bytesRead, long totalBytes) {
			this.terrain = terrain;
			this.bytesRead = bytesRead;
			this.totalBytes = totalBytes;
		}
	}

	public class SceneObjectLoadedData {
		public AssetPrim linkset;
		public long bytesRead;
		public long totalBytes;

		public SceneObjectLoadedData(AssetPrim linkset, long bytesRead, long totalBytes) {
			this.linkset = linkset;
			this.bytesRead = bytesRead;
			this.totalBytes = totalBytes;
		}
	}

	public class SettingsLoadedData {
		public String regionName;
		public RegionSettings settings;

		public SettingsLoadedData(String regionName, RegionSettings settings) {
			this.regionName = regionName;
			this.settings = settings;
		}
	}
	// #region Archive Loading

	public static void unpackageArchive(File filename, Callback<AssetLoadedData> assetCallback,
			Callback<TerrainLoadedData> terrainCallback, Callback<SceneObjectLoadedData> objectCallback,
			Callback<SettingsLoadedData> settingsCallback) throws FileNotFoundException {
		int successfulAssetRestores = 0;
		int failedAssetRestores = 0;

		long fileLength = filename.length();
		TarArchiveReader archive = null;
		GZIPInputStream loadStream = null;
		PushbackInputStream pushStream = null;
		InputStream fileStream = new FileInputStream(filename);
		try {
			loadStream = new GZIPInputStream(fileStream);
			if (loadStream != null) {
				pushStream = new PushbackInputStream(loadStream);
				if (pushStream != null) {
					archive = new TarArchiveReader(pushStream);

					TarArchiveReader.TarHeader header = archive.new TarHeader();
					byte[] data;

					while ((data = archive.readEntry(header)) != null) {
						if (header.FilePath.startsWith(ArchiveConstants.OBJECTS_PATH)) {
							// Deserialize the XML bytes
							if (objectCallback != null)
								loadObjects(data, objectCallback, pushStream.getBytePosition(), fileLength);
						} else if (header.FilePath.startsWith(ArchiveConstants.ASSETS_PATH)) {
							if (assetCallback != null) {
								if (loadAsset(header.FilePath, data, assetCallback, pushStream.getBytePosition(),
										fileLength))
									successfulAssetRestores++;
								else
									failedAssetRestores++;
							}
						} else if (header.FilePath.startsWith(ArchiveConstants.TERRAINS_PATH)) {
							if (terrainCallback != null)
								loadTerrain(header.FilePath, data, terrainCallback, pushStream.getBytePosition(),
										fileLength);
						} else if (header.FilePath.startsWith(ArchiveConstants.SETTINGS_PATH)) {
							if (settingsCallback != null)
								loadRegionSettings(header.FilePath, data, settingsCallback);
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("[OarFile] Error loading OAR file: ", ex);
			return;
		} finally {
			try {
				if (archive != null)
					archive.close();
				if (pushStream != null)
					pushStream.close();
				if (loadStream != null)
					pushStream.close();
				if (fileStream != null)
					fileStream.close();
			} catch (IOException ex) {
			}
		}

		logger.debug(String.format("[OarFile]: Restored " + successfulAssetRestores + " assets"));
		if (failedAssetRestores > 0)
			logger.warn(String.format("[OarFile]: Failed to load " + failedAssetRestores + " assets"));
	}

	private static boolean loadAsset(String assetPath, byte[] data, Callback<AssetLoadedData> assetCallback,
			long bytesRead, long totalBytes) {
		// Right now we're nastily obtaining the UUID from the filename
		if (!assetPath.startsWith(ArchiveConstants.ASSETS_PATH))
			return false;

		String fileName = assetPath.substring(ArchiveConstants.ASSETS_PATH.length());
		String extension = Helpers.getFileExtension(fileName, ArchiveConstants.ASSET_EXTENSION_SEPARATOR);

		if (extension == null) {
			logger.warn(String.format(
					"[OarFile]: Could not find extension information in asset path %s since it's missing the separator %c. Skipping",
					assetPath, ArchiveConstants.ASSET_EXTENSION_SEPARATOR));
			return false;
		}

		RefObject<UUID> uuid = new RefObject<>(null);
		UUID.TryParse(fileName, uuid);
		AssetType assetType = ArchiveConstants.getAssetTypeForExtenstion(extension);
		if (assetType != null) {
			AssetItem asset = AssetManager.createAssetItem(assetType, uuid.argvalue, data);
			if (asset != null) {
				assetCallback.callback(new OarFile().new AssetLoadedData(asset, bytesRead, totalBytes));
				return true;
			}
		}
		logger.warn("[OarFile] Failed to load asset");
		return false;
	}

	private static boolean loadRegionSettings(String filePath, byte[] data,
			Callback<SettingsLoadedData> settingsCallback) {
		RegionSettings settings = null;
		boolean loaded = false;

		try {
			InputStream stream = new ByteArrayInputStream(data);
			settings = RegionSettings.fromStream(stream, Helpers.ASCII_ENCODING);
			loaded = true;
		} catch (Exception ex) {
			logger.warn("[OarFile] Failed to parse region settings file " + filePath + ": ", ex);
		}

		// Parse the region name out of the filename
		String regionName = FilenameUtils.removeExtension(FilenameUtils.getName(filePath.toString()));
		if (loaded)
			settingsCallback.callback(new OarFile().new SettingsLoadedData(regionName, settings));

		return loaded;
	}

	private static boolean loadTerrain(String filePath, byte[] data, Callback<TerrainLoadedData> terrainCallback,
			long bytesRead, long totalBytes) {
		float[][] terrain = new float[256][256];
		boolean loaded = false;
		String extension = FilenameUtils.getExtension(filePath);

		if (extension.equals("r32") || extension.equals("f32")) {
			// RAW32
			if (data.length == 256 * 256 * 4) {
				int pos = 0;
				for (int y = 0; y < 256; y++) {
					for (int x = 0; x < 256; x++) {
						terrain[y][x] = Helpers.clamp(Helpers.BytesToFloatL(data, pos), 0.0f, 255.0f);
						pos += 4;
					}
				}

				loaded = true;
			} else {
				logger.warn(
						"[OarFile] RAW32 terrain file " + filePath + " has the wrong number of bytes: " + data.length);
			}
		} else if (extension.equals("ter"))
			; // Terragen
		else if (extension.equals("raw"))
			; // LLRAW
		else if (extension.equals("jpg") || extension.equals("jpeg"))
			; // JPG
		else if (extension.equals("bmp"))
			; // BMP
		else if (extension.equals("png"))
			; // PNG
		else if (extension.equals("gif"))
			; // GIF
		else if (extension.equals("tif") || extension.equals("tiff"))
			; // TIFF
		else
			logger.warn("[OarFile] Unrecognized terrain format in " + filePath);

		if (loaded)
			terrainCallback.callback(new OarFile().new TerrainLoadedData(terrain, bytesRead, totalBytes));

		return loaded;
	}

	public static void loadObjects(byte[] objectData, Callback<SceneObjectLoadedData> objectCallback, long bytesRead,
			long totalBytes) throws XmlPullParserException, IOException {
		InputStream stream = new ByteArrayInputStream(objectData);
		XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
		parser.setInput(stream, Helpers.UTF8_ENCODING);

		try {
			parser.nextTag();
		} catch (XmlPullParserException ex) {
			logger.debug("What the heck");
		}

		if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("scene")) {
			parser.nextTag();
			while (parser.getEventType() == XmlPullParser.START_TAG) {
				AssetPrim linkset = new AssetPrim(parser);
				if (linkset != null)
					objectCallback.callback(new OarFile().new SceneObjectLoadedData(linkset, bytesRead, totalBytes));
				parser.nextTag();
			}
		} else {
			AssetPrim linkset = new AssetPrim(parser);
			if (linkset != null)
				objectCallback.callback(new OarFile().new SceneObjectLoadedData(linkset, bytesRead, totalBytes));
		}
		stream.close();
	}

	// #endregion Archive Loading

	// #region Archive Saving

	public static void packageArchive(File directoryName, File fileName) throws IOException {
		final String ARCHIVE_XML = "<?xml version=\"1.0\" encoding=\"utf-16\"?>\n<archive major_version=\"0\" minor_version=\"1\" />";

		TarArchiveWriter archive = new TarArchiveWriter(new GZIPOutputStream(new FileOutputStream(fileName)));

		// Create the archive.xml file
		archive.writeFile("archive.xml", ARCHIVE_XML);

		// Add the assets
		File dir = new File(directoryName, ArchiveConstants.ASSETS_PATH);
		String[] files = dir.list();
		for (String file : files)
			archive.writeFile(ArchiveConstants.ASSETS_PATH + FilenameUtils.getName(file),
					FileUtils.readFileToByteArray(new File(dir, file)));

		// Add the objects
		dir = new File(directoryName, ArchiveConstants.OBJECTS_PATH);
		files = dir.list();
		for (String file : files)
			archive.writeFile(ArchiveConstants.OBJECTS_PATH + FilenameUtils.getName(file),
					FileUtils.readFileToByteArray(new File(dir, file)));

		// Add the terrain(s)
		dir = new File(directoryName, ArchiveConstants.TERRAINS_PATH);
		files = dir.list();
		for (String file : files)
			archive.writeFile(ArchiveConstants.TERRAINS_PATH + FilenameUtils.getName(file),
					FileUtils.readFileToByteArray(new File(dir, file)));

		// Add the parcels(s)
		dir = new File(directoryName, ArchiveConstants.LANDDATA_PATH);
		files = dir.list();
		for (String file : files)
			archive.writeFile(ArchiveConstants.LANDDATA_PATH + FilenameUtils.getName(file),
					FileUtils.readFileToByteArray(new File(dir, file)));

		// Add the setting(s)
		dir = new File(directoryName, ArchiveConstants.SETTINGS_PATH);
		files = dir.list();
		for (String file : files)
			archive.writeFile(ArchiveConstants.SETTINGS_PATH + FilenameUtils.getName(file),
					FileUtils.readFileToByteArray(new File(dir, file)));

		archive.close();
	}

	public static void saveTerrain(SimulatorManager sim, File terrainPath) throws IOException, InterruptedException {
		if (terrainPath.exists())
			FileUtils.deleteDirectory(terrainPath);
		Thread.sleep(100);
		terrainPath.mkdir();
		Thread.sleep(100);
		OutputStream stream = new FileOutputStream(new File(terrainPath, sim.getName() + ".r32"));
		saveTerrainStream(stream, sim);
		stream.close();
	}

	private static void saveTerrainStream(OutputStream stream, SimulatorManager sim) throws IOException {
		int x, y;
		for (y = 0; y < 256; y++) {
			for (x = 0; x < 256; x++) {
				float height = sim.terrainHeightAtPoint(x, y);
				stream.write(Helpers.floatToBytesL(height));
			}
		}
	}

	public static void saveParcels(SimulatorManager sim, File parcelPath) throws IOException, InterruptedException,
			IllegalArgumentException, IllegalStateException, XmlPullParserException {
		if (parcelPath.exists())
			FileUtils.deleteDirectory(parcelPath);
		Thread.sleep(100);
		parcelPath.mkdir();
		Thread.sleep(100);

		for (Parcel parcel : sim.parcels.values()) {
			UUID globalID = UUID.GenerateUUID();
			serializeParcel(parcel, globalID, new File(parcelPath, globalID + ".xml"));
		}
	}

	private static void serializeParcel(Parcel parcel, UUID globalID, File fileName)
			throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException {

		Writer fileWriter = new FileWriter(fileName);
		XmlSerializer writer = XmlPullParserFactory.newInstance().newSerializer();
		writer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
		writer.setOutput(fileWriter);
		writer.startDocument(Helpers.UTF8_ENCODING, null);
		writer.startTag(null, "LandData");

		writeInt(writer, "Area", parcel.area);
		writeInt(writer, "AuctionID", parcel.auctionID);
		parcel.authBuyerID.serializeXml(writer, null, "AuthBuyerID");
		writeInt(writer, "Category", parcel.category.getValue());
		writeLong(writer, "ClaimDate", (long) Helpers.dateTimeToUnixTime(parcel.claimDate));
		writeInt(writer, "ClaimPrice", parcel.claimPrice);
		globalID.serializeXml(writer, null, "GlobalID");
		parcel.groupID.serializeXml(writer, null, "GroupID");
		writeBoolean(writer, "IsGroupOwned", parcel.isGroupOwned);
		writeString(writer, "Bitmap",
				Base64.encodeBase64String(parcel.bitmap != null ? parcel.bitmap : Helpers.EmptyBytes));
		writeString(writer, "Description", parcel.desc);
		writeInt(writer, "Flags", parcel.flags);
		writeInt(writer, "LandingType", parcel.landing.getValue());
		writeString(writer, "Name", parcel.name);
		writeInt(writer, "Status", parcel.status.getValue());
		writeInt(writer, "LocalID", parcel.localID);
		writeInt(writer, "MediaAutoScale", parcel.media.mediaAutoScale ? 1 : 0);
		parcel.media.mediaID.serializeXml(writer, null, "MediaID");
		writeString(writer, "MediaURL", parcel.media.mediaURL);
		writeString(writer, "MusicURL", parcel.musicURL);
		parcel.ownerID.serializeXml(writer, null, "OwnerID");

		writer.startTag(null, "ParcelAccessList");
		for (ParcelAccessEntry pal : parcel.accessBlackList) {
			writer.startTag(null, "ParcelAccessEntry");
			pal.agentID.serializeXml(writer, null, "AgentID");
			writeString(writer, "Time", String.format("%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS", pal.time)); // 2008-06-15T21:15:07
			writeInt(writer, "AccessList", pal.flags);
			writer.endTag(null, "ParcelAccessEntry");
		}
		for (ParcelAccessEntry pal : parcel.accessWhiteList) {
			writer.startTag(null, "ParcelAccessEntry");
			pal.agentID.serializeXml(writer, null, "AgentID");
			writeString(writer, "Time", String.format("%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS", pal.time)); // 2008-06-15T21:15:07
			writeInt(writer, "AccessList", pal.flags);
			writer.endTag(null, "ParcelAccessEntry");
		}
		writer.endTag(null, "ParcelAccessList");

		writeFloat(writer, "PassHours", parcel.passHours);
		writeInt(writer, "PassPrice", parcel.passPrice);
		writeInt(writer, "SalePrice", parcel.salePrice);
		parcel.snapshotID.serializeXml(writer, null, "SnapshotID");
		parcel.userLocation.serializeXml(writer, null, "UserLocation");
		parcel.userLookAt.serializeXml(writer, null, "UserLookAt");
		writeString(writer, "Dwell", "0");
		writeInt(writer, "OtherCleanTime", parcel.otherCleanTime);

		writer.endTag(null, "LandData");

		fileWriter.close();
	}

	public static void saveRegionSettings(SimulatorManager sim, File settingsPath)
			throws IOException, InterruptedException, XmlPullParserException {
		if (settingsPath.exists())
			FileUtils.deleteDirectory(settingsPath);
		Thread.sleep(100);
		settingsPath.mkdir();
		Thread.sleep(100);

		RegionSettings settings = new RegionSettings();
		// settings.AgentLimit;
		settings.allowDamage = (sim.flags & RegionFlags.AllowDamage) == RegionFlags.AllowDamage;
		// settings.AllowLandJoinDivide;
		settings.allowLandResell = (sim.flags & RegionFlags.BlockLandResell) != RegionFlags.BlockLandResell;
		settings.blockFly = (sim.flags & RegionFlags.NoFly) == RegionFlags.NoFly;
		settings.blockLandShowInSearch = (sim.flags & RegionFlags.BlockParcelSearch) == RegionFlags.BlockParcelSearch;
		settings.blockTerraform = (sim.flags & RegionFlags.BlockTerraform) == RegionFlags.BlockTerraform;
		settings.disableCollisions = (sim.flags & RegionFlags.SkipCollisions) == RegionFlags.SkipCollisions;
		settings.disablePhysics = (sim.flags & RegionFlags.SkipPhysics) == RegionFlags.SkipPhysics;
		settings.disableScripts = (sim.flags & RegionFlags.SkipScripts) == RegionFlags.SkipScripts;
		settings.fixedSun = (sim.flags & RegionFlags.SunFixed) == RegionFlags.SunFixed;
		settings.maturityRating = sim.access.getValue();
		// settings.ObjectBonus;
		settings.restrictPushing = (sim.flags & RegionFlags.RestrictPushObject) == RegionFlags.RestrictPushObject;
		settings.terrainDetail0 = sim.terrainDetail0;
		settings.terrainDetail1 = sim.terrainDetail1;
		settings.terrainDetail2 = sim.terrainDetail2;
		settings.terrainDetail3 = sim.terrainDetail3;
		settings.terrainHeightRange00 = sim.terrainHeightRange00;
		settings.terrainHeightRange01 = sim.terrainHeightRange01;
		settings.terrainHeightRange10 = sim.terrainHeightRange10;
		settings.terrainHeightRange11 = sim.terrainHeightRange11;
		settings.terrainStartHeight00 = sim.terrainStartHeight00;
		settings.terrainStartHeight01 = sim.terrainStartHeight01;
		settings.terrainStartHeight10 = sim.terrainStartHeight10;
		settings.terrainStartHeight11 = sim.terrainStartHeight11;
		// settings.UseEstateSun;
		settings.waterHeight = sim.waterHeight;

		settings.toXML(new File(settingsPath, sim.getName() + ".xml"));
	}

	public static void savePrims(AssetManager manager, List<AssetPrim> prims, File primsDir, File assetsPath)
			throws InterruptedException {
		Map<UUID, UUID> textureList = new HashMap<>();

		// Delete all of the old linkset files
		try {
			FileUtils.deleteDirectory(primsDir);
		} catch (Exception ex) {
		}

		Thread.sleep(100);
		// Create a new folder for the linkset files
		try {
			primsDir.mkdir();
		} catch (Exception ex) {
			logger.error("Failed saving prims: ", ex);
			return;
		}
		Thread.sleep(100);
		try {
			for (AssetPrim assetPrim : prims) {
				savePrim(assetPrim, new File(primsDir, "Primitive_" + assetPrim.getParent().id + ".xml"));

				collectTextures(assetPrim.getParent(), textureList);
				List<PrimObject> children = assetPrim.getChildren();
				if (children != null) {
					for (PrimObject child : children)
						collectTextures(child, textureList);
				}
			}
			saveAssets(manager, AssetType.Texture, textureList.keySet(), assetsPath);
		} catch (Exception ex) {
		}
	}

	static void collectTextures(PrimObject prim, Map<UUID, UUID> textureList) {
		if (prim.textures != null) {
			// Add all of the textures on this prim to the save list
			if (prim.textures.defaultTexture != null)
				textureList.put(prim.textures.defaultTexture.getTextureID(),
						prim.textures.defaultTexture.getTextureID());

			if (prim.textures.faceTextures != null) {
				for (int i = 0; i < prim.textures.faceTextures.length; i++) {
					TextureEntryFace face = prim.textures.faceTextures[i];
					if (face != null)
						textureList.put(face.getTextureID(), face.getTextureID());
				}
			}
			if (prim.sculpt != null && !prim.sculpt.texture.equals(UUID.Zero))
				textureList.put(prim.sculpt.texture, prim.sculpt.texture);
		}
	}

	public static void clearAssetFolder(File directory) throws InterruptedException {
		// Delete the assets folder
		try {

			FileUtils.deleteDirectory(directory);
		} catch (Exception ex) {
		}
		Thread.sleep(100);

		// Create a new assets folder
		try {
			directory.mkdir();
		} catch (Exception ex) {
			logger.error("Failed saving assets: ", ex);
			return;
		}
		Thread.sleep(100);
	}

	public static void saveAssets(AssetManager assetManager, AssetType assetType, Set<UUID> assets, File assetsPath)
			throws Exception {
		List<UUID> remainingTextures = new ArrayList<>(assets);
		TimeoutEvent<Boolean> allReceived = new TimeoutEvent<>();
		assets.forEach(new Consumer<UUID>() {
			@Override
			public void accept(UUID texture) {
				if (assetType.equals(AssetType.Texture)) {
					assetManager.requestImage(texture, new Callback<ImageDownload>() {
						@Override
						public boolean callback(ImageDownload transfer) {
							if (transfer.success) {
								if (transfer.assetData != null) {
									String extension = ArchiveConstants.getExtensionForType(assetType);
									try {
										OutputStream stream = new FileOutputStream(
												new File(assetsPath, texture.toString() + extension));
										stream.write(transfer.assetData);
										stream.close();
										remainingTextures.remove(texture);
										if (remainingTextures.size() == 0)
											allReceived.set(true);
									} catch (IOException ex) {
									}
								} else {
									System.out.println("Missing asset " + texture);
								}
							}
							return true;
						}
					});
				} else {
					try {
						assetManager.requestAsset(texture, assetType, false, new Callback<AssetDownload>() {
							@Override
							public boolean callback(AssetDownload transfer) {
								if (transfer.success) {
									if (transfer.assetData != null) {
										String extension = ArchiveConstants.getExtensionForType(assetType);
										try {
											OutputStream stream = new FileOutputStream(
													new File(assetsPath, texture.toString() + extension));
											stream.write(transfer.assetData);
											stream.close();
											remainingTextures.remove(texture);
											if (remainingTextures.size() == 0)
												allReceived.set(true);
										} catch (IOException ex) {
										}
									} else {
										System.out.println("Missing asset " + texture);
									}
								}
								return true;
							}
						});
					} catch (Exception ex) {

					}
				}
				try {
					Thread.sleep(200);
					if (remainingTextures.size() > 0 && remainingTextures.size() % 5 == 0)
						Thread.sleep(250);
				} catch (Exception ex) {

				}
			}
		});

		allReceived.waitOne(5000 + 350 * assets.size());

		logger.info("Copied " + (assets.size() - remainingTextures.size()) + " textures to the asset archive folder");
	}

	public static void saveSimAssets(AssetManager assetManager, AssetType assetType, UUID assetID, UUID itemID,
			UUID primID, File assetsPath) throws Exception {
		TimeoutEvent<Integer> allReceived = new TimeoutEvent<>();
		assetManager.requestAsset(assetID, itemID, primID, assetType, false, SourceType.SimInventoryItem,
				UUID.GenerateUUID(), new Callback<AssetDownload>() {
					@Override
					public boolean callback(AssetDownload transfer) {
						if (transfer.success) {
							if (transfer.assetData != null) {
								String extension = ArchiveConstants.getExtensionForType(assetType);
								try {
									OutputStream stream = new FileOutputStream(
											new File(assetsPath, assetID.toString() + extension));
									stream.write(transfer.assetData);
									stream.close();
									allReceived.set(1);
								} catch (Exception ex) {
								}
								;
							} else {
								allReceived.set(0);
								System.out.println("Missing asset " + assetID);
							}
						}
						return true;
					}
				});
		Integer count = allReceived.waitOne(5000);
		if (count != null)
			logger.info("Copied " + count + " textures to the asset archive folder");
	}

	static void savePrim(AssetPrim prim, File filename) throws IOException {
		Writer writer = new FileWriter(filename);
		try {
			prim.writeXml(writer, 4);
		} catch (Exception ex) {
			logger.error("Failed saving linkset: ", ex);
			throw new IOException("failed saving linkset", ex);
		} finally {
			writer.close();
		}
	}

	static void writeBoolean(XmlSerializer writer, String tag, boolean value)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, tag).text(value ? "True" : "False").endTag(null, tag);
	}

	static void writeInt(XmlSerializer writer, String tag, int value)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, tag).text(Integer.toString(value)).endTag(null, tag);
	}

	static void writeLong(XmlSerializer writer, String tag, long value)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, tag).text(Long.toString(value)).endTag(null, tag);
	}

	static void writeFloat(XmlSerializer writer, String tag, float value)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, tag).text(Float.toString(value)).endTag(null, tag);
	}

	static void writeString(XmlSerializer writer, String tag, String value)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, tag).text(value).endTag(null, tag);
	}
	// #endregion Archive Saving
}
