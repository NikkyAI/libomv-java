/**
 * Copyright (C) 2011 aki@akjava.com
 * Copyright (c) 2012 Frederick Martian
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

import libomv.types.Vector3;

public class BVHNode
{
	private Vector3 offset;
	
	public Vector3 getOffset()
	{
		return offset;
	}
	
	public void setOffset(Vector3 offset)
	{
		this.offset = offset;
	}
	
	public Vector3 getEndSite()
	{
		if (endSites.size() > 0)
		{
			return endSites.get(0);
		}
		return null;
	}

	public List<Vector3> getEndSites()
	{
		return endSites;
	}

	public void addEndSite(Vector3 endSite)
	{
		this.endSites.add(endSite);
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public Channels getChannels()
	{
		return channels;
	}
	
	public void setChannels(Channels channels)
	{
		this.channels = channels;
	}
	
	private List<Vector3> endSites = new ArrayList<Vector3>();
	private String name;
	private Channels channels;
	private List<BVHNode> joints = new ArrayList<BVHNode>();

	public List<BVHNode> getJoints()
	{
		return joints;
	}
	
	public void add(BVHNode joint)
	{
		joints.add(joint);
	}

	//usually null for special purpose
	private String parentName;
	public String getParentName()
	{
		return parentName;
	}
	
	public void setParentName(String parentName)
	{
		this.parentName = parentName;
	}
	
	private BVHTranslation translation;
	public BVHTranslation getTranslation()
	{
		return translation;
	}
	
	public void setTranslation(BVHTranslation translation)
	{
		this.translation = translation;
	}
	
}