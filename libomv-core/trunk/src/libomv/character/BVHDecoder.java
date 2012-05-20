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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import libomv.types.Matrix4;
import libomv.types.Vector3;

public class BVHDecoder
{

/*
	public class Joint
	{
		// Include aligned members first
		Matrix4		mFrameMatrix;
		Matrix4		mOffsetMatrix;
*		Vector3		mRelativePosition;
		//
*		String		mName;
*		boolean		mIgnore;
*		boolean		mIgnorePositions;
*		boolean		mRelativePositionKey;
*		boolean		mRelativeRotationKey;
*		String		mOutName;
*		String		mMergeParentName;
*		String		mMergeChildName;
*		char		mOrder[4];			/* Flawfinder: ignore
*		KeyVector	mKeys;
*		int			mNumPosKeys;
*		int			mNumRotKeys;
		int			mChildTreeMaxDepth;
		int			mPriority;
	}
*/

	enum EConstraintType
	{
		CONSTRAINT_TYPE_POINT,
		CONSTRAINT_TYPE_PLANE,
		NUM_CONSTRAINT_TYPES
	}

	enum EConstraintTargetType
	{
		CONSTRAINT_TARGET_TYPE_BODY,
		CONSTRAINT_TARGET_TYPE_GROUND,
		NUM_CONSTRAINT_TARGET_TYPES
	}
	
	public class Constraint
	{
		String		mSourceJointName;
		String		mTargetJointName;
		int			mChainLength;
		Vector3		mSourceOffset;
		Vector3		mTargetOffset;
		Vector3		mTargetDir;
		float		mEaseInStart;
		float		mEaseInStop;
		float		mEaseOutStart;
		float		mEaseOutStop;
		EConstraintType mConstraintType;
	};

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
	
	int			mLineNumber;

	// Translation values
	int			mNumFrames;
	float		mFrameTime;

	ArrayList<Constraint>	mConstraints = new ArrayList<Constraint>();
	HashMap<String, BVHTranslation> mTranslations = new HashMap<String, BVHTranslation>();

	int		mPriority;
	boolean	mLoop;
	float	mLoopInPoint;
	float	mLoopOutPoint;
	float	mEaseIn;
	float	mEaseOut;
	int		mHand;
	String	mEmoteName;

	boolean	mInitialized;
//	Status	mStatus;
	// computed values
	float	mDuration;

	public BVHDecoder(BufferedReader reader) throws IOException, InvalidLineException
	{
		mode = HIERARCHY;
		loadTranslationTable("anim.ini");
		loadBVHFile(reader);
		optimize();
	}
	
	private void loadTranslationTable(String filename) throws IOException
	{
		mTranslations.clear();
		mConstraints.clear();
		InputStream st = getClass().getResourceAsStream("/res/" + filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(st));
		try
		{
			String line = br.readLine();
			
			if (!line.startsWith("Translations 1.0"))
			{
				throw new IOException("Invalid Translation file header");
			}
			boolean loadingGlobals = false;
			BVHTranslation trans = null;
			while ((line = br.readLine().trim()) != null)
			{
				/* if the line is empty or a comment ignore it */
				if (line.isEmpty() || line.startsWith("#"))
					continue;
				
				if (line.startsWith("["))
				{
					if (line.startsWith("[GLOBALS]"))
					{
						loadingGlobals = true;
					}
					else
					{
						loadingGlobals = false;
						trans = new BVHTranslation();
						mTranslations.put(line.substring(1, line.indexOf("]") - 1), trans);
					}
					continue;
				}
				
				int offset = line.indexOf("=");
				String vals[], token = line.substring(0,  offset - 1).trim();
				line = line.substring(offset + 1).trim();
				
				if (loadingGlobals)
				{
					if (token.compareToIgnoreCase("emote") == 0)
					{
						mEmoteName = line;
					}
					else if (token.compareToIgnoreCase("priority") == 0)
					{
						mPriority = Integer.parseInt(line);
					}
					else if (token.compareToIgnoreCase("loop") == 0)
					{
						vals = line.split(" ");
						float loop_in = 0.f;
						float loop_out = 1.f;
						if (vals.length >= 2)
						{
							mLoop = true;
							loop_in = Float.parseFloat(vals[0]);
							loop_out = Float.parseFloat(vals[1]);
						}
						else if (vals.length == 1)
						{
						    mLoop = vals[0].compareToIgnoreCase("true") == 0;
						}
						mLoopInPoint = loop_in * mDuration;
						mLoopOutPoint = loop_out * mDuration;
					}
					else if (token.compareToIgnoreCase("easein") == 0)
					{
						mEaseIn = Float.parseFloat(line);
					}
					else if (token.compareToIgnoreCase("easeout") == 0)
					{
						mEaseOut = Float.parseFloat(line);
					}
					else if (token.compareToIgnoreCase("hand") == 0)
					{
						mHand = Integer.parseInt(line);
					}
					else if (token.compareToIgnoreCase("constraint") == 0)
					{
						Constraint constraint = new Constraint();
						vals = line.split(" ");
						if (vals.length >= 13)
						{
							constraint.mChainLength = Integer.parseInt(vals[0]);
							constraint.mEaseInStart = Float.parseFloat(vals[1]);
							constraint.mEaseInStop = Float.parseFloat(vals[2]);
							constraint.mEaseOutStart = Float.parseFloat(vals[3]);
							constraint.mEaseOutStop = Float.parseFloat(vals[4]);
							constraint.mSourceJointName = vals[5];
							constraint.mSourceOffset.X = Float.parseFloat(vals[6]);
							constraint.mSourceOffset.Y = Float.parseFloat(vals[7]);
							constraint.mSourceOffset.Z = Float.parseFloat(vals[8]);
							constraint.mTargetJointName = vals[9];
							constraint.mTargetOffset.X = Float.parseFloat(vals[10]);
							constraint.mTargetOffset.Y = Float.parseFloat(vals[11]);
							constraint.mTargetOffset.Z = Float.parseFloat(vals[12]);
							if (vals.length >= 16)
							{
								constraint.mTargetDir.X = Float.parseFloat(vals[13]);
								constraint.mTargetDir.Y = Float.parseFloat(vals[14]);
								constraint.mTargetDir.Z = Float.parseFloat(vals[15]);
								constraint.mTargetDir.Normalize();
							}
						}
						else
						{
							throw new IOException("Invalid constraint entry");							
						}
						constraint.mConstraintType = EConstraintType.CONSTRAINT_TYPE_POINT;
						mConstraints.add(constraint);
					}
					else if (token.compareToIgnoreCase("planar_constraint") == 0)
					{
						Constraint constraint = new Constraint();
						vals = line.split(" ");
						if (vals.length >= 13)
						{
							constraint.mChainLength = Integer.parseInt(vals[0]);
							constraint.mEaseInStart = Float.parseFloat(vals[1]);
							constraint.mEaseInStop = Float.parseFloat(vals[2]);
							constraint.mEaseOutStart = Float.parseFloat(vals[3]);
							constraint.mEaseOutStop = Float.parseFloat(vals[4]);
							constraint.mSourceJointName = vals[5];
							constraint.mSourceOffset.X = Float.parseFloat(vals[6]);
							constraint.mSourceOffset.Y = Float.parseFloat(vals[7]);
							constraint.mSourceOffset.Z = Float.parseFloat(vals[8]);
							constraint.mTargetJointName = vals[9];
							constraint.mTargetOffset.X = Float.parseFloat(vals[10]);
							constraint.mTargetOffset.Y = Float.parseFloat(vals[11]);
							constraint.mTargetOffset.Z = Float.parseFloat(vals[12]);
							if (vals.length >= 16)
							{
								constraint.mTargetDir.X = Float.parseFloat(vals[13]);
								constraint.mTargetDir.Y = Float.parseFloat(vals[14]);
								constraint.mTargetDir.Z = Float.parseFloat(vals[15]);
								constraint.mTargetDir.Normalize();
							}
						}
						else
						{
							throw new IOException("Invalid constraint entry");							
						}
						constraint.mConstraintType = EConstraintType.CONSTRAINT_TYPE_PLANE;
						mConstraints.add(constraint);
					}
				}
				else if (trans == null)
				{
					throw new IOException("Invalid Translation file format");
				}
				else if (token.compareToIgnoreCase("ignore") == 0)
				{
					trans.mIgnore = line.compareToIgnoreCase("true") == 0;
				}
				else if (token.compareToIgnoreCase("relativepos") == 0)
				{
					vals = line.split(" ");
					if (vals.length >= 3)
					{
						trans.mRelativePosition = new Vector3(
								Float.parseFloat(vals[0]), Float.parseFloat(vals[1]), Float.parseFloat(vals[2]));
					}
					else if (vals.length >= 1 && vals[0].compareToIgnoreCase("firstkey") == 0)
					{
						trans.mRelativePositionKey = true;
					}
					else
					{
						throw new IOException("No relative key");
					}
				}
				else if (token.compareToIgnoreCase("relativerot") == 0)
				{
					if (line.compareToIgnoreCase("firstkey") == 0)
					{
						trans.mRelativePositionKey = true;
					}
					else
					{
						throw new IOException("No relative key");
					}
				}
				else if (token.compareToIgnoreCase("outname") == 0)
				{
					if (!line.isEmpty())
					{
						trans.mOutName = line;
					}
					else
					{
						throw new IOException("No valid outname");
					}
				}
				else if (token.compareToIgnoreCase("frame") == 0)
				{
					vals = line.split("[, ]+");
					if (vals.length >= 9)
					{
						trans.mFrameMatrix = new Matrix4(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]), Float.parseFloat(vals[2]), 0,
								                         Float.parseFloat(vals[3]), Float.parseFloat(vals[4]), Float.parseFloat(vals[5]), 0,
								                         Float.parseFloat(vals[6]), Float.parseFloat(vals[7]), Float.parseFloat(vals[8]), 0,
								                         0, 0, 0, 0);
					}
					else
					{
						throw new IOException("No valid matrix");
					}
				}
				else if (token.compareToIgnoreCase("offset") == 0)
				{
					vals = line.split("[, ]+");
					if (vals.length >= 9)
					{
						trans.mOffsetMatrix = new Matrix4(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]), Float.parseFloat(vals[2]), 0,
								                          Float.parseFloat(vals[3]), Float.parseFloat(vals[4]), Float.parseFloat(vals[5]), 0,
								                          Float.parseFloat(vals[6]), Float.parseFloat(vals[7]), Float.parseFloat(vals[8]), 0,
								                          0, 0, 0, 0);
					}
					else
					{
						throw new IOException("No valid matrix");
					}
				}
				else if (token.compareToIgnoreCase("mergeparent") == 0)
				{
					if (!line.isEmpty())
					{
						trans.mMergeParentName = line;
					}
					else
					{
						throw new IOException("No valid merge parent");
					}
				}
				else if (token.compareToIgnoreCase("mergechild") == 0)
				{
					if (!line.isEmpty())
					{
						trans.mMergeChildName = line;
					}
					else
					{
						throw new IOException("No valid merge parent");
					}
				}
				else if (token.compareToIgnoreCase("priority") == 0)
				{
					if (!line.isEmpty())
					{
						trans.mPriorityModifier = Integer.parseInt(line);
					}
					else
					{
						throw new IOException("No valid priority");
					}
				}
			}
		}
		finally
		{
			br.close();
			st.close();
		}
	}
	
	private void loadBVHFile(BufferedReader reader) throws InvalidLineException, IOException
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
		    			node.setTranslation(mTranslations.get(name));
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
		    			float x = toFloat(i, line, values[1]);
		    			float y = toFloat(i, line, values[2]);
		    			float z = toFloat(i, line, values[3]);
		    			getLast().setOffset(new Vector3(x, y, z));
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
		    			if (values.length != 2)
		    			{
		    				throw new InvalidLineException(i, line, " Invalid Joint name:" + line);
		    			}
		    			String name = values[1];
		    			BVHNode node = new BVHNode();
		    			node.setName(name);
		    			node.setTranslation(mTranslations.get(name));
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
		    			float x = toFloat(i, line, values[1]);
		    			float y = toFloat(i, line, values[2]);
		    			float z = toFloat(i, line, values[3]);
		    			getLast().addEndSite(new Vector3(x, y, z));
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
		    			BVHMotion motion = new BVHMotion();
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
		    			int frames = toInt(i, line, values[1]);
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
		    			float frameTime = toFloat(i, line, values[1].trim());
		    			bvh.getMotion().setFrameTime(frameTime);
		    			mode = MOTION_DATA;
		    		}
		    		else
		    		{
		    			throw new InvalidLineException(i,line, "Expected Frame Time");
		    		}
		    		break;
		    	case MOTION_DATA:
		    		float vs[] = toFloat(i, line);
		    		bvh.getMotion().getMotions().add(vs);
		    		break;
		    }
		}	
	}
	
	public void optimize()
	{
		
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

	protected float[] toFloat(int index,String line) throws InvalidLineException
	{
		String values[] = line.split(" ");
		float vs[] = new float[values.length];
		try
		{
			for (int i = 0; i < values.length; i++)
			{
				vs[i] = Float.parseFloat(values[i]);
			}
		}
		catch (Exception e)
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

	protected float toFloat(int index, String line, String v) throws InvalidLineException
	{
		float d = 0;
		try
		{
			d = Float.parseFloat(v);
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
