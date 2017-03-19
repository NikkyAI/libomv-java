/**
 * Copyright (c) 2011 aki@akjava.com
 * Copyright (c) 2012-2017, Frederick Martian
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libomv.character;

import java.util.ArrayList;
import java.util.List;

public class BVHMotion
{
	private int frames;
	private float frameTime;
	
	public int getFrames()
	{
		return frames;
	}
	
	public float[] getFrameAt(int index)
	{
		return motions.get(index);
	}
	
	public void setFrames(int frames)
	{
		this.frames = frames;
	}
	
	public void syncFrames()
	{
		setFrames(motions.size());
	}

	public float getFrameTime()
	{
		return frameTime;
	}
	
	public void setFrameTime(float frameTime)
	{
		this.frameTime = frameTime;
	}
	
	public List<float[]> getMotions()
	{
		return motions;
	}
	
	private List<float[]> motions = new ArrayList<float[]>();
	
	public void add(float[] motion)
	{
		motions.add(motion);
	}
	
	public int size()
	{
		return motions.size();
	}
	
	public float getDuration()
	{
		//TODO support ignore first
		return frameTime * motions.size();
	}
}