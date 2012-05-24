/**
 * Copyright (C) 2011 aki@akjava.com
 * Copyright (c) 2012, Frederick Martian
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

public class Channels
{
	private boolean Xrotation, Yrotation, Zrotation, Xposition, Yposition, Zposition;

 	public static final int XPOSITION = 0;
	public static final int YPOSITION = 1;
	public static final int ZPOSITION = 2;
	public static final int XROTATION = 3;
	public static final int YROTATION = 4;
	public static final int ZROTATION = 5;

	private int rotOffset;
	
	public Channels(int rotOffset)
	{
		this.rotOffset = -rotOffset;
	}
	
	public int getRotOffset()
	{
		return rotOffset;
	}
	
	private String order = "";
	private String text = "";

	public String getOrder()
	{
		return order;
	}

	public void addOrder(String ch)
	{
		order += ch;
	}

	public boolean isXrotation()
	{
		return Xrotation;
	}

	public void setXrotation(boolean xrotation)
	{
		if (rotOffset < 1)
			rotOffset = -rotOffset;
		Xrotation = xrotation;
		text += "Xrotation ";
	}

	public boolean isYrotation()
	{
		return Yrotation;
	}

	public void setYrotation(boolean yrotation)
	{
		if (rotOffset < 1)
			rotOffset = -rotOffset;
		Yrotation = yrotation;
		text += "Yrotation ";
	}

	public boolean isZrotation()
	{
		return Zrotation;
	}

	public void setZrotation(boolean zrotation)
	{
		if (rotOffset < 1)
			rotOffset = -rotOffset;
		Zrotation = zrotation;
		text += "Zrotation ";
	}

	public boolean isXposition()
	{
		return Xposition;
	}

	public void setXposition(boolean xposition)
	{
		if (rotOffset < 1)
			rotOffset--;
		Xposition = xposition;
		text += "Xposition ";
	}

	public boolean isYposition()
	{
		return Yposition;
	}

	public void setYposition(boolean yposition)
	{
		if (rotOffset < 1)
			rotOffset--;
		Yposition = yposition;
		text += "Yposition ";
	}

	public boolean isZposition()
	{
		return Zposition;
	}

	public void setZposition(boolean zposition)
	{
		if (rotOffset < 1)
			rotOffset--;
		Zposition = zposition;
		text += "Zposition ";
	}
	
	public int getNumChannels()
	{
		int size = 0;
		if (Xposition)
		{
			size++;
		}
		if (Yposition)
		{
			size++;
		}
		if (Zposition)
		{
			size++;
		}
		if (Xrotation)
		{
			size++;
		}
		if (Yrotation)
		{
			size++;
		}
		if (Zrotation)
		{
			size++;
		}
		return size;
	}
	
	@Override
	public String toString()
	{
		if (text.isEmpty())
		{
			return "CHANNELS 0";
		}
		return "CHANNELS " + getNumChannels() + " " + text.substring(0, text.length() - 1);
	}
}