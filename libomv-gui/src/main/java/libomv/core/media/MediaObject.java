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

import java.util.HashMap;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import libomv.types.UUID;
import libomv.types.Vector3;

public class MediaObject
{
    protected static MediaManager manager;
    protected static HashMap<UUID, BufferSound> allBuffers;
	protected static LineListener lineCallback;

    /* Indicates if this object's resources have already been disposed */
    private boolean disposed = false;
    public boolean getDisposed()
    {
    	return disposed;
    }
    protected boolean finished = false;

    // A SoundSource represents the data (buffer or stream)
    protected SoundSource sound = null;
    public SoundSource getSoundSource()
    {
    	return sound;
    }

    // A SoundChannel represents a playback instance of a sound.
    protected SoundChannel channel = null;
    public SoundChannel getSoundChannel()
    {
    	return channel;
    }

    protected boolean cloned = false;
    public void dispose()
    {
    	if (!cloned && sound != null)
        {
            sound.dispose();
            sound = null;
        }
        disposed = true;
    }

    public boolean getActive()
    {
    	return (sound != null);
    }

    /* Change a playback volume */
    protected float volume = 0.8f;
    public float getVolume()
    {
        return volume;
    }
 
    public void setVolume(float value)
    {
        volume = value;
        if (channel == null)
        	return;

        channel.setVolume(volume);
    }

    /* Update the 3D position of a sound source */
    protected Vector3 position;
    public void setPosition(Vector3 value)
    {
        position = value;
        if (channel == null)
        	return;

        channel.set3DAttributes(position, null);
    }

    /**
     * Control the volume of all inworld sounds
     */
    protected static float allObjectVolume = 0.8f;
    
    public void stop()
    {
        if (channel != null)
        {
            channel.stop();
        }
    }

    /**
     * A callback for asynchronous FMOD calls.
     * 
     * Note: Subclasses override these methods to handle callbacks.
     * 
     * @return true on success, false otherwise
     */
    protected class DispatchEndCallback implements LineListener
    {
		@Override
		public void update(LineEvent event)
		{
			LineEvent.Type type = event.getType();
			if (type == LineEvent.Type.CLOSE)
	        {
                if (allChannels.containsKey(event.getSource()))
                {
                    MediaObject sndobj = allChannels.get(event.getSource());
                    sndobj.endCallbackHandler();
                }
	        }
		}
    }
    
    protected boolean endCallbackHandler()
    {
    	return true;
    }

    protected static HashMap<SoundSource, MediaObject> allSounds;
    protected static HashMap<SoundChannel, MediaObject> allChannels;
    
    protected void registerSound(SoundSource sound)
    {
        allSounds.put(sound, this);
    }

    protected void registerChannel(SoundChannel channel)
    {
        allChannels.put(channel, this);
    }
    
    protected void unregisterSound()
    {
        allSounds.remove(sound);
    }
    
    protected void unregisterChannel()
    {
        allChannels.remove(channel);
    }
}
