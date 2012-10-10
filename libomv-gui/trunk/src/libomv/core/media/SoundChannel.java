package libomv.core.media;

import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import libomv.types.Vector3;

// A SoundChannel represents a playback instance of a sound.
public class SoundChannel
{
	private SourceDataLine line;
	private SoundSource attachedSource;
	private int loopCount;
	private float minDistance;
	private float maxDistance;
	private float volume;
	private long timestamp;
	private Vector3 position;
	private Vector3 velocity;

	public SoundChannel(SoundSource sound, DataLine.Info info) throws LineUnavailableException
	{
        this.line = (SourceDataLine) AudioSystem.getLine(info);
        this.line.open(sound.getFormat());
		this.attachedSource = sound;
	}
	
	public void setCallback(LineListener listener)
	{
		line.addLineListener(listener);
	}
	
	public void set3DAttributes(Vector3 pos, Vector3 vel)
	{
		this.position = pos;
		this.velocity = vel;
		if (vel != null && !vel.equals(Vector3.Zero))
		{
			timestamp = System.currentTimeMillis();
		}
		updateVolume(true);
	}

	public void setLoopCount(int number)
	{
		loopCount = number;
	}
	
	public int getLoopCount()
	{
		if (loopCount != 0)
			return loopCount;
		return attachedSource.getLoopCount();
	}

	public void set3DMinMaxDistance(float minDistance, float maxDistance)
	{
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		updateVolume(true);
	}
	
	public float get3DMinDistance()
	{
		if (minDistance != 0)
			return this.minDistance;
		return attachedSource.get3DMinDistance();
	}

	public float get3DMaxDistance()
	{
		if (maxDistance != 0)
			return this.maxDistance;
		return attachedSource.get3DMaxDistance();
	}

	public void setVolume(float volume)
	{
		this.volume = volume;
		updateVolume(true);
	}
	
	private void updateVolume(boolean full)
	{
		FloatControl control = (FloatControl)line.getControl(Type.VOLUME);
		float value = 1.0f;
		control.getMaximum();
		
		control.setValue(value);		
	}
	
	public boolean start() throws IOException
	{
       	if (line != null && !line.isActive())
       	{
    		updateVolume(true);
            line.start();
            return keepAlive();
       	}
		return false;
	}
	
	public void pause()
	{
		line.stop();
	}
	
	public void stop()
	{
       	if (line != null)
       	{
       		if (!line.isActive())
        	{
       			line.drain();
       			line.stop();
        	}
        	line.close();
       	}
	}

	public boolean keepAlive() throws IOException
	{
       	if (line != null && line.isActive())
       	{
            int nBytesWritten = attachedSource.write(line, 4096);
            if (nBytesWritten > 0)
            {	
            	return true;
            }
            attachedSource.dispose();
            stop();
       	}
       	return false;
	}
}
