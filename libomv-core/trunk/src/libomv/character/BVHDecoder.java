/**
 * Copyright (C) 2011 aki@akjava.com
 * Copyright (c) 2011, 2012 Frederick Martian
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import libomv.types.Vector3d;

public class BVHDecoder
{
	private final int HIERARCHY = 0;
	private final int ROOT = 1;
	private final int JOINT_OPEN = 2;
	private final int JOINT_CLOSE = 3;
	private final int JOINT_INSIDE = 4;
	private final int ENDSITES_OPEN = 5;
	private final int ENDSITES_INSIDE = 6;
	private final int ENDSITES_CLOSE = 7;

	private final int MOTION = 8;
	private final int MOTION_FRAMES = 9;
	private final int MOTION_FRAME_TIME = 10;
	private final int MOTION_DATA = 11;

	private int mode;
	List<BVHNode> nodes = new ArrayList<BVHNode>();
	private BVH bvh;

	public BVHDecoder(BufferedReader reader) throws IOException, InvalidLineException
	{
		int status = loadTranslationTable("anim.ini");
		if (status == 0)
		{
			mode = HIERARCHY;
			status = loadBVHFile(reader);
		}
	}
	
	private int loadTranslationTable(String filename) throws IOException
	{
		InputStream st = getClass().getResourceAsStream("/res/" + filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(st));
		try
		{
			
		}
		catch (Exception ex)
		{
			
		}
		finally
		{
			br.close();
			st.close();
		}
		return 0;
	}
	
	private int loadBVHFile(BufferedReader reader) throws InvalidLineException, IOException
	{
		int i = -1;
		String line;
		String[] values;
		while ((line = reader.readLine()) != null) 
		{
			line = line.trim();
			i++;
		    if (line.isEmpty())
		    {
			    continue; // skip any empty line
		    }
		    switch (mode)
		    {
		    	case HIERARCHY:
		    		if (!line.equals("HIERARCHY"))
		    		{
		    			throw new InvalidLineException(i, line, "Expected HIERARCHY");
		    		}
		    		mode = ROOT;
		    		break;
		    	case ROOT:
		    		if (line.startsWith("ROOT"))
		    		{
		    			BVHNode node = new BVHNode();
		    			String name = line.substring("ROOT".length()).trim();
		    			node.setName(name);
		    			bvh.setHiearchy(node);
		    			nodes.add(node);

		    			mode = JOINT_OPEN;
		    		}
		    		else
		    		{
		    			throw new InvalidLineException(i,line, "Expected ROOT");
		    		}
		    		break;
		    	case JOINT_OPEN:
		    		if (line.equals("{"))
		    		{
		    			mode = JOINT_INSIDE;
		    		}
		    		else
		    		{
		    			throw new InvalidLineException(i,line, "Expected {");
		    		}
		    		break;
		    	case JOINT_INSIDE:
		    		values = line.split(" ");
		    		if (values[0].equals("OFFSET"))
		    		{
		    			if (values.length!=4)
		    			{
		    				throw new InvalidLineException(i,line, "OFFSET value need 3 points");
		    			}
		    			double x = toDouble(i, line, values[1]);
		    			double y = toDouble(i, line, values[2]);
		    			double z = toDouble(i, line, values[3]);
		    			getLast().setOffset(new Vector3d(x, y, z));
		    		}
		    		else if (values[0].equals("CHANNELS"))
		    		{
		    			if (values.length < 2)
		    			{
		    				throw new InvalidLineException(i, line, "CHANNEL must have 3 values");
		    			}
		    			int channelSize = toInt(i, line, values[1]);
		    			
		    			if (channelSize < 1)
		    			{
		    				throw new InvalidLineException(i, line, "CHANNEL size must be larger than 0");
		    			}

		    			if (channelSize != values.length - 2)
		    			{
		    				throw new InvalidLineException(i, line, " Invalid CHANNEL size:" + channelSize);
		    			}
		    			Channels channel = new Channels();
		    			for (int j = 2; j < values.length; j++)
		    			{
		    				if (values[j].equals("Xposition"))
		    				{
		    					channel.setXposition(true);
		    					bvh.add(new NameAndChannel(getLast().getName(), Channels.XPOSITION, channel));
		    				}
		    				else if(values[j].equals("Yposition"))
		    				{
		    					channel.setYposition(true);
		    					bvh.add(new NameAndChannel(getLast().getName(), Channels.YPOSITION, channel));
		    				}
		    				else if(values[j].equals("Zposition"))
		    				{
		    					channel.setZposition(true);
		    					bvh.add(new NameAndChannel(getLast().getName(), Channels.ZPOSITION, channel));
		    				}
		    				else if(values[j].equals("Xrotation"))
		    				{
		    					channel.setXrotation(true);
		    					channel.addOrder("X");
		    					bvh.add(new NameAndChannel(getLast().getName(), Channels.XROTATION, channel));
		    				}
		    				else if(values[j].equals("Yrotation"))
		    				{
		    					channel.setYrotation(true);
		    					channel.addOrder("Y");
		    					bvh.add(new NameAndChannel(getLast().getName(), Channels.YROTATION, channel));
		    				}
		    				else if(values[j].equals("Zrotation"))
		    				{
		    					channel.setZrotation(true);
		    					channel.addOrder("Z");
		    					bvh.add(new NameAndChannel(getLast().getName(), Channels.ZROTATION, channel));
		    				}
		    				else
		    				{
		    					throw new InvalidLineException(i, line, " Invalid CHANNEL value:" + values[j]);
		    				}
		    			}
		    			getLast().setChannels(channel);
		    		}
		    		else if(line.equals("End Site"))
		    		{
		    			mode = ENDSITES_OPEN;
		    		}
		    		else if(line.equals("}"))
		    		{
		    			mode = JOINT_INSIDE;
		    			nodes.remove(getLast()); //pop up

		    			if(nodes.size()==0)
		    			{
		    				mode = MOTION;
		    			}
		    		}
		    		else if(values[0].equals("JOINT"))
		    		{
		    			if (values.length!=2)
		    			{
		    				throw new InvalidLineException(i, line, " Invalid Joint name:" + line);
		    			}
		    			String name = values[1];
		    			BVHNode node = new BVHNode();
		    			node.setName(name);
		    			getLast().add(node);
		    			nodes.add(node);
		    			mode = JOINT_OPEN;
		    		}
		    		else
		    		{
		    			throw new InvalidLineException(i, line, " Invalid Joint inside:" + values[0]);
		    		}
		    		break;
		    	case ENDSITES_OPEN:
		    		if (line.equals("{"))
		    		{
		    			mode = ENDSITES_INSIDE;
		    		}
		    		else
		    		{
		    			throw new InvalidLineException(i, line, "Expected {");
		    		}
		    		break;
		    	case ENDSITES_INSIDE:
		    		values = line.split(" ");
		    		if (values[0].equals("OFFSET"))
		    		{
		    			if (values.length!=4)
		    			{
		    				throw new InvalidLineException(i, line, "OFFSET value need 3 points");
		    			}
		    			double x = toDouble(i, line, values[1]);
		    			double y = toDouble(i, line, values[2]);
		    			double z = toDouble(i, line, values[3]);
		    			getLast().addEndSite(new Vector3d(x, y, z));
		    			mode = ENDSITES_CLOSE;
		    		}
		    		else
		    		{
		    			throw new InvalidLineException(i,line, "Endsite only support offset");
		    		}
		    		break;
		    	case ENDSITES_CLOSE:
		    		if (line.equals("}"))
		    		{
		    			mode = JOINT_CLOSE;
		    		}
		    		else
		    		{
		    			throw new InvalidLineException(i, line, "Expected {");
		    		}
		    		break;
		   		case JOINT_CLOSE:
		    		if (line.equals("}"))
		    		{
		    			mode = JOINT_INSIDE;//maybe joint again or close
		    		    nodes.remove(getLast());//pop up

		    		    if (nodes.size()==0)
		    		    {
		    		    	mode = MOTION;
		    		    }
		    		}
		    		else if (line.equals("End Site"))
		    		{
		    			mode = ENDSITES_OPEN;
		    		}
		    		else
		    		{
		    			throw new InvalidLineException(i, line, "Expected {");
		    		}
		    		break;
		    	case MOTION:
		    		if (line.equals("MOTION"))
		    		{
		    			BVHMotion motion=new BVHMotion();
		    			bvh.setMotion(motion);
		    			mode = MOTION_FRAMES;
		    		}
		    		else
		    		{
		    			throw new InvalidLineException(i,line, "Expected MOTION");
		    		}
		    		break;
		    	case MOTION_FRAMES:
		    		values = line.split(" ");
		    		if (values[0].equals("Frames:") && values.length == 2)
		    		{
		    			int frames=toInt(i, line, values[1]);
		    			bvh.getMotion().setFrames(frames);
		    			mode = MOTION_FRAME_TIME;
		    		}
		    		else
		    		{
		    			throw new InvalidLineException(i, line, "Expected Frames:");
		    		}
		    		break;
		    	case MOTION_FRAME_TIME:
		    		values = line.split(":"); //not space
		    		if (values[0].equals("Frame Time") && values.length == 2)
		    		{
		    			double frameTime = toDouble(i, line, values[1].trim());
		    			bvh.getMotion().setFrameTime(frameTime);
		    			mode = MOTION_DATA;
		    		}
		    		else
		    		{
		    			throw new InvalidLineException(i,line, "Expected Frame Time");
		    		}
		    		break;
		    	case MOTION_DATA:
		    		double vs[] = toDouble(i, line);
		    		bvh.getMotion().getMotions().add(vs);
		    		break;
		    }
		}
		return 0;		
	}
	
	public boolean validate()
	{
		if (bvh.getMotion().getFrames() != bvh.getMotion().getMotions().size())
		{
			return false;
		}
		return true;
	}

	protected BVHNode getLast()
	{
		return nodes.get(nodes.size()-1);
	}


	protected double[] toDouble(int index,String line) throws InvalidLineException
	{
		String values[] = line.split(" ");
		double vs[] = new double[values.length];
		try
		{
			for(int i = 0; i < values.length; i++)
			{
				vs[i] = Double.parseDouble(values[i]);
			}
		}
		catch(Exception e)
		{
			throw new InvalidLineException(index, line, "has invalid double");
		}
		return vs;
	}


	protected int toInt(int index, String line, String v) throws InvalidLineException
	{
		int d = 0;
		try
		{
			d = Integer.parseInt(v);
		}
		catch (Exception e)
		{
			throw new InvalidLineException(index, line, "invalid integer value:" + v);
		}
		return d;
	}

	protected double toDouble(int index, String line, String v) throws InvalidLineException
	{
		double d = 0;
		try
		{
			d = Double.parseDouble(v);
		}
		catch (Exception e)
		{
			throw new InvalidLineException(index, line, "invalid double value:" + v);
		}
		return d;
	}

	public class InvalidLineException extends Exception
	{
		private static final long serialVersionUID = 1L;
		public InvalidLineException(int index,String line,String message)
		{
			super("line " + index + ":" + line + ":message:" + message);
		}
	}

	public LLAnimation parse()
	{

		
		
		return null;
	}
}
