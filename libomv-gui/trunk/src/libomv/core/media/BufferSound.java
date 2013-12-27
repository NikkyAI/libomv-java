/**
 * Copyright (c) 2011-2012, Frederick Martian
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
 * - Neither the name of the libomv-java project nor the names of its
 *   contributors may be used to endorse or promote products derived from
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
package libomv.core.media;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import libomv.assets.AssetItem.AssetType;
import libomv.assets.AssetManager;
import libomv.assets.AssetManager.AssetDownload;
import libomv.assets.AssetSound;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.TimeoutEvent;

public class BufferSound extends MediaObject
{
    private UUID containerId;
    private boolean prefetchOnly;
    private boolean looping;

    /**
     * The individual volume setting for THIS object
     */
    private float volumeSetting = 0.5f;
    public void setVolumeSetting(float value)
    {
    	volumeSetting = value;
    }
    
    public float getVolumeSetting()
    {
    	return volumeSetting;
    }
  
    /**
     * Handle arrival of a sound resource.
     */
    public class Assets_OnSoundReceived implements Callback<AssetDownload>
    {
		@Override
		public boolean callback(AssetDownload transfer)
		{
	        if (transfer.Success)
	        {
	            // If this was a Prefetch, just stop here.
	            if (prefetchOnly)
	                return false;

	            Logger.Log("Opening sound " + transfer.ItemID.toString(), LogLevel.Debug);

	            // Decode the Ogg Vorbis buffer.
	            AssetSound s = (AssetSound)AssetManager.CreateAssetItem(AssetType.Sound, transfer.ItemID, transfer.AssetData);
	            s.decode();

	            try
	            {
	            	sound = SoundSystem.createSound(new ByteArrayInputStream(s.AssetData), null);
                    registerSound(sound);

                    // If looping is requested, loop the entire thing.
                    if (looping)
                    {
                        int soundlen = sound.getFrameLength();
                        sound.setLoopPoints(0, soundlen - 1);
                        sound.setLoopCount(-1);
                    }
                    
                    channel = SoundSystem.playSound(-1, sound, true);
                    registerChannel(channel);

                    channel.setVolume(volumeSetting * allObjectVolume);

                    // Take note of when the sound is finished playing.
                    channel.setCallback(lineCallback);

                    // Set attenuation limits.
                    sound.set3DMinMaxDistance(1.2f,       // Any closer than this gets no louder
                                              100.0f);    // Further than this gets no softer.

                    // Set the sound point of origin. This is in SIM coordinates.
                    channel.set3DAttributes(position, null);

                    // Turn off pause mode.  The sound will start playing now.
                    channel.start();
	            }
	            catch (Exception ex)
	            {
	            	Logger.Log("Exception when trying to queue sound data", LogLevel.Error, ex);
	            }
	        }
	        else
	        {
	            Logger.Log("Failed to download sound: " + transfer.Status.toString(), LogLevel.Error);
	        }
	        return false;
		}
    }
 
    // A simpler constructor used by PreFetchSound.
    public BufferSound(UUID soundID)
    {
        if (manager == null || !manager.getSoundSystemAvailable()) return;

        this.prefetchOnly = true;
        this.containerId = UUID.Zero;

        try
        {
			manager.RequestAsset(soundID, AssetType.Sound, false, new  Assets_OnSoundReceived());
		}
        catch (Exception ex)
        {
            Logger.Log("Failed to request sound download: " + soundID.toString(), LogLevel.Error, ex);
		}
    }

    public BufferSound(UUID objectID, UUID soundID, boolean looping, boolean global, Vector3 worldpos, float volume)
    {
        if (manager == null || !manager.getSoundSystemAvailable()) return;

        // Do not let this get garbage-collected.
        synchronized (allBuffers)
        {
            allBuffers.put(objectID, this);
		}
        
        this.containerId = objectID;
        this.position = worldpos;
        this.volumeSetting = volume;
        this.looping = looping;
        
        Logger.Log(String.format("Playing sound at <%0.0f,%0.0f,%0.0f> ID %s}",
                                 position.X, position.Y, position.Z, soundID.toString()), LogLevel.Debug);

         // Fetch the sound data.
        try
        {
        	manager.RequestAsset(soundID, AssetType.Sound, false, new Assets_OnSoundReceived());
		}
		catch (Exception ex)
		{
			Logger.Log("Failure requestin sound buffere for sound " + soundID, LogLevel.Error, ex);
		}
    }
    
    /**
     * Handles stop sound even from FMOD
     *
     * @returns true on success, false otherwise
     */
    protected boolean EndCallbackHandler()
    {
        stopSound(false);
        return true;
    }

    public static void killAll()
    {
        // Make a list from the dictionary so we do not get a deadlock
        // on it when removing entries.
        ArrayList<BufferSound> list = new ArrayList<BufferSound>(allBuffers.values());

        for (BufferSound s : list)
        {
            s.stopSound();
        }

        ArrayList<MediaObject> objs = new ArrayList<MediaObject>(allChannels.values());
        for (MediaObject obj : objs)
        {
            if (obj instanceof BufferSound)
                ((BufferSound)obj).stopSound();
        }
    }

    public static void kill(UUID uuid)
    {
        if (allBuffers.containsKey(uuid))
        {
            BufferSound bs = allBuffers.get(uuid);
            bs.stopSound(true);
        }   	
    }
    
    /**
     * Adjust volumes of all playing sounds to observe the new global sound volume
     */
    public static void adjustVolumes()
    {
        // Make a list from the dictionary so we do not get a deadlock
        ArrayList<BufferSound> list = new ArrayList<BufferSound>(allBuffers.values());

        for (BufferSound s : list)
        {
            s.adjustVolume();
        }
    }

    public void adjustVolume()
    {
        volume = volumeSetting * allObjectVolume;    	
    }
    
    protected void stopSound()
    {
        stopSound(false);
    }

    protected void stopSound(final boolean blocking)
    {
        final TimeoutEvent<Boolean> stopped = blocking ? new TimeoutEvent<Boolean>() : null;
 
        finished = true;

        new Thread(new Runnable()
        {
        	public void run()
        	{
                // Release the buffer to avoid a big memory leak.
                if (channel != null)
                {
                	synchronized(allChannels)
                	{
                        allChannels.remove(channel);
                	}
                	channel.stop();
                    channel = null;
                }

                if (sound != null)
                {
                    sound.dispose();
                    sound = null;
                }

                synchronized(allBuffers)
                {
                    allBuffers.remove(containerId);
                }

                if (blocking)
                {
                    stopped.set(true);
                }
        	}
        }).run();
        
        if (blocking)
        {
            try
            {
				stopped.waitOne(10000);
			}
            catch (InterruptedException ex) { }
        }
    }
}
