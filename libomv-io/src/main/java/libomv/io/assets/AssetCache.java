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
package libomv.io.assets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import libomv.io.GridClient;
import libomv.io.LibSettings;
import libomv.io.LoginManager.LoginProgressCallbackArgs;
import libomv.io.LoginManager.LoginStatus;
import libomv.io.NetworkManager.DisconnectedCallbackArgs;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Settings.SettingsUpdateCallbackArgs;

// Class that handles the local asset cache
public class AssetCache
{
	private static final Logger logger = Logger.getLogger(AssetCache.class);
	
	// User can plug in a routine to compute the asset cache location
	public interface ComputeAssetCacheFilenameDelegate
	{
		public File callback(String cacheDir, UUID assetID, String suffix);
	}

	public ComputeAssetCacheFilenameDelegate ComputeAssetCacheFilename = null;

	private GridClient _Client;
	private Thread cleanerThread;
	private Timer cleanerTimer;
	private long pruneInterval = 1000 * 60 * 5;
	private boolean autoPruneEnabled = true;

	// Allows setting weather to periodically prune the cache if it grows too big
	// Default is enabled, when caching is enabled
	public final void setAutoPruneEnabled(boolean value)
	{
		autoPruneEnabled = value;

		if (autoPruneEnabled)
		{
			SetupTimer();
		}
		else
		{
			DestroyTimer();
		}
	}

	public final boolean getAutoPruneEnabled()
	{
		return autoPruneEnabled;
	}

	// How long (in ms) between cache checks (default is 5 min.)
	public final void setAutoPruneInterval(long value)
	{
		pruneInterval = value;
		SetupTimer();
	}

	public final long getAutoPruneInterval()
	{
		return pruneInterval;
	}

	/* Checks whether caching is enabled */
	private boolean useAssetCache;
	/* Name of the cache directory */
	private String cacheAssetDir;
	/* Maximum asset cache size */
	private long cacheAssetMaxSize;

	private File resourcePath;
	private File settingsPath;
	private File cacheAssetPath;
	private File cacheStaticPath;

	private void setResourcePath(String path)
	{
		resourcePath = new File(path).getAbsoluteFile();
		cacheStaticPath = new File(resourcePath, "static_assets");
		
		settingsPath = new File(System.getProperty("user.home"), "." + path);
		settingsPath.mkdir();
	}

	private void setAssetPath(String path)
	{
		if (path != null)
			cacheAssetDir = path;
		cacheAssetPath = new File(settingsPath, cacheAssetDir); // &(APPDATA)/_libomv/cache
		cacheAssetPath.mkdir();
	}

	private class SettingsUpdate implements Callback<SettingsUpdateCallbackArgs>
	{
		@Override
		public boolean callback(SettingsUpdateCallbackArgs params)
		{
			String key = params.getName();
			if (key == null)
			{
				useAssetCache = _Client.Settings.getBool(LibSettings.USE_ASSET_CACHE);
				cacheAssetMaxSize = _Client.Settings.getLong(LibSettings.ASSET_CACHE_MAX_SIZE);
				setResourcePath(_Client.Settings.getString(LibSettings.RESOURCE_DIR));
				setAssetPath(_Client.Settings.getString(LibSettings.ASSET_CACHE_DIR));
			}
			else if (key.equals(LibSettings.USE_ASSET_CACHE))
			{
				useAssetCache = params.getValue().AsBoolean();
			}
			else if (key.equals(LibSettings.USE_ASSET_CACHE))
			{
				cacheAssetMaxSize = params.getValue().AsLong();
			}
			else if (key.equals(LibSettings.ASSET_CACHE_DIR))
			{
				setAssetPath(params.getValue().AsString());
			}
			else if (key.equals(LibSettings.RESOURCE_DIR))
			{
				setResourcePath(params.getValue().AsString());
			 	setAssetPath(null);
			}
			return false;
		}
	}

	/**
	 * Default constructor
	 * 
	 * @param client A reference to the GridClient object
	 * @param manager A reference to the AssetManager
	 */
	public AssetCache(GridClient client)
	{
		_Client = client;

		_Client.Settings.OnSettingsUpdate.add(new SettingsUpdate());
		useAssetCache = _Client.Settings.getBool(LibSettings.USE_ASSET_CACHE);
		cacheAssetMaxSize = _Client.Settings.getLong(LibSettings.ASSET_CACHE_MAX_SIZE);
		setResourcePath(_Client.Settings.getString(LibSettings.RESOURCE_DIR));
		setAssetPath(_Client.Settings.getString(LibSettings.ASSET_CACHE_DIR));

		_Client.Login.OnLoginProgress.add(new Network_LoginProgress(), false);
		_Client.Network.OnDisconnected.add(new Network_Disconnected(), true);
	}

	private class Network_LoginProgress implements Callback<LoginProgressCallbackArgs>
	{
		@Override
		public boolean callback(LoginProgressCallbackArgs e)
		{
			if (e.getStatus() == LoginStatus.Success)
			{
				SetupTimer();
			}
			return false;
		}
	}

	private class Network_Disconnected implements Callback<DisconnectedCallbackArgs>
	{
		@Override
		public boolean callback(DisconnectedCallbackArgs e)
		{
			DestroyTimer();
			return false;
		}
	}

	// Disposes cleanup timer
	private void DestroyTimer()
	{
		if (cleanerTimer != null)
		{
			cleanerTimer.cancel();
			cleanerTimer = null;
		}
	}

	// Only create timer when needed
	private void SetupTimer()
	{
		if (useAssetCache && autoPruneEnabled && _Client.Network.getConnected())
		{
			cleanerTimer = new Timer("AssetCleaner");
			cleanerTimer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					BeginPrune();
				}
			}, pruneInterval, pruneInterval);
		}
	}

	/**
	 * Return bytes read from the local asset cache, null if it does not exist
	 * 
	 * @param assetID
	 *            UUID of the asset we want to get
	 * @return Raw bytes of the asset, or null on failure
	 */
	public final byte[] get(UUID assetID, String suffix)
	{
		if (useAssetCache)
		{
			try
			{
				File file = cachedAssetFile(assetID, suffix);
				boolean exists = file.exists() && file.length() > 0;
				if (!exists)
				{
					file = getStaticAssetFile(assetID);
					exists = file.exists() && file.length() > 0;
					if (exists)
						logger.debug(GridClient.Log("Reading " + file + " from static asset cache.", _Client));
				}
				else
				{
					logger.debug(GridClient.Log("Reading " + file + " from asset cache.", _Client));
				}
				
				if (exists)
				{
					byte[] assetData = new byte[(int) file.length()];
					FileInputStream fis = new FileInputStream(file);
					try
					{
						fis.read(assetData);
					}
					finally
					{
						fis.close();
					}
					return assetData;
				}
			}
			catch (Throwable ex)
			{
				logger.warn(GridClient.Log("Failed reading asset from cache (" + ex.getMessage() + ")", _Client), ex);
			}
		}
		return null;
	}

	/**
	 * Constructs a file name of the cached asset
	 * 
	 * @param assetID UUID of the asset
	 * @return String with the file name of the cached asset
	 */
	public File cachedAssetFile(UUID assetID, String suffix)
	{
		if (ComputeAssetCacheFilename != null)
		{
			return ComputeAssetCacheFilename.callback(cacheAssetDir, assetID, suffix);
		}
		String filename = assetID.toString();
		if (suffix != null)
		{
			filename = filename + "." + suffix;
		}
		return new File(cacheAssetPath, filename);
	}

	/**
	 * Constructs a file name of the static cached asset
	 *
	 * @param assetID
	 *            UUID of the asset
	 * @return String with the file name of the static cached asset
	 */
	private File getStaticAssetFile(UUID assetID)
	{
		return new File(cacheStaticPath, assetID.toString());
	}

	/**
	 * Saves an asset to the local cache
	 * 
	 * @param assetID
	 *            UUID of the asset
	 * @param assetData
	 *            Raw bytes the asset consists of
	 * @return Weather the operation was successfull
	 */
	public final boolean put(UUID assetID, byte[] assetData, String suffix)
	{
		if (useAssetCache)
		{
			try
			{
				File file = cachedAssetFile(assetID, suffix);
				logger.debug(GridClient.Log("Saving " + file + " to asset cache.", _Client));
				FileOutputStream fos = new FileOutputStream(file);
				try
				{
					fos.write(assetData);
				}
				finally
				{
					fos.close();
				}
				return true;
			}
			catch (Throwable ex)
			{
				logger.warn(GridClient.Log("Failed saving asset to cache (" + ex.getMessage() + ")", _Client), ex);
			}
		}
		return false;
	}

	/**
	 * Checks if the asset exists in the local cache
	 * Note: libOpenMetaverse: HasAsset()
	 * 
	 * @param assetID
	 *            UUID of the asset
	 * @return True is the asset is stored in the cache, otherwise false
	 */
	public final boolean containsKey(UUID assetID, String suffix)
	{
		if (useAssetCache)
		{
			File file = cachedAssetFile(assetID, suffix);
			if (!file.exists())
			{
				file = getStaticAssetFile(assetID);
			}
			return file.exists();
		}
		return false;
	}

	private File[] ListCacheFiles()
	{
		if (!cacheAssetPath.exists() || !cacheAssetPath.isDirectory())
		{
			return null;
		}

		class CacheNameFilter implements FilenameFilter
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
			}
		}

		// We save file with UUID as file name, only count those
		return cacheAssetPath.listFiles(new CacheNameFilter());
	}

	/**
	 * Wipes out entire cache
	 * 
	 */
	public final void clear()
	{
		File[] files = ListCacheFiles();
		if (files != null)
		{
			int num = 0;
			for (File file : files)
			{
				file.delete();
				++num;
			}
			logger.debug(GridClient.Log("Wiped out " + num + " files from the cache directory.", _Client));
		}
	}

	/**
	 * Brings cache size to the 90% of the max size
	 * 
	 */
	public final void Prune()
	{
		File[] files = ListCacheFiles();
		long size = GetFileSize(files);

		if (size > cacheAssetMaxSize)
		{
			Arrays.sort(files, new SortFilesByModTimeHelper());
			long targetSize = (long)(cacheAssetMaxSize * 0.9);
			int num = 0;
			for (File file : files)
			{
				++num;
				size -= file.length();
				file.delete();
				if (size < targetSize)
				{
					break;
				}
			}
			logger.debug(GridClient.Log(num + " files deleted from the cache, cache size now: " + NiceFileSize(size), _Client));
		}
		else
		{
			logger.debug(GridClient.Log("Cache size is " + NiceFileSize(size) + ", file deletion not needed", _Client));
		}

	}

	/**
	 * Asynchronously brings cache size to the 90% of the max size
	 * 
	 */
	public final void BeginPrune()
	{
		// Check if the background cache cleaning thread is active first
		if (cleanerThread != null && cleanerThread.isAlive())
		{
			return;
		}

		synchronized (this)
		{
			cleanerThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					Prune();
				}
			});
			cleanerThread.setDaemon(true);
			cleanerThread.start();
		}
	}

	/**
	 * Adds up file sizes passed in a File array
	 */
	private long GetFileSize(File[] files)
	{
		long ret = 0;
		for (File file : files)
		{
			ret += file.length();
		}
		return ret;
	}

	/**
	 * Nicely formats file sizes
	 * 
	 * @param byteCount
	 *            Byte size we want to output
	 * @return String with humanly readable file size
	 */
	private String NiceFileSize(long byteCount)
	{
		String size = "0 Bytes";
		if (byteCount >= 1073741824)
		{
			size = String.format("%d", (int) (byteCount / 1073741824)) + " GB";
		}
		else if (byteCount >= 1048576)
		{
			size = String.format("%d", (int) (byteCount / 1048576)) + " MB";
		}
		else if (byteCount >= 1024)
		{
			size = String.format("%d", (int) (byteCount / 1024)) + " KB";
		}
		else if (byteCount > 0 && byteCount < 1024)
		{
			size = ((Long) byteCount).toString() + " Bytes";
		}

		return size;
	}

	/**
	 * Helper class for sorting files by their last accessed time
	 * 
	 */
	private class SortFilesByModTimeHelper implements Comparator<File>
	{
		@Override
		public int compare(File f1, File f2)
		{
			if (f1.lastModified() > f2.lastModified())
			{
				return 1;
			}
			if (f1.lastModified() < f2.lastModified())
			{
				return -1;
			}
			return 0;
		}
	}
}
