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

public class BVHWriter
{
	public String writeToString(BVH bvh)
	{
		StringBuilder buffer = new StringBuilder();
		//hierachy
		buffer.append("HIERARCHY\n");
		writeTo(bvh.getHiearchy(),buffer,0);

		//MOTION
		int frames = bvh.getFrames();
		buffer.append("MOTION\n");
		buffer.append("Frames: " + frames + "\n");
		buffer.append("Frame Time: " + bvh.getFrameTime() + "\n");

		for(int i = 0; i < frames; i++)
		{
			double[] values = bvh.getFrameAt(i);
			String v = "";
			for(int j = 0; j < values.length; j++)
			{
				v += values[j];
				if (j != values.length - 1)
				{
					v += " ";
				}
			}
			buffer.append(v+"\n");
		}
		//return text;
		return buffer.toString();
	}
	
	private void writeTo(BVHNode node, StringBuilder buffer, int indent)
	{
		String indentText = "";
		for (int i = 0; i < indent; i++)
		{
			indentText += "\t";
		}
		if (indent == 0)
		{
			buffer.append("ROOT " + node.getName() + "\n");
		}
		else
		{
			buffer.append(indentText + "JOINT " + node.getName() + "\n");
		}
		buffer.append(indentText + "{" + "\n");
		//offset
		buffer.append("\t" + indentText + node.getOffset().toString() + "\n");
		//channel
		buffer.append("\t" + indentText + node.getChannels().toString() + "\n");
		//joint
		for (int i = 0; i < node.getJoints().size(); i++)
		{
			writeTo(node.getJoints().get(i), buffer, indent + 1);
		}
		//endsite
		if (node.getEndSite() != null)
		{
			buffer.append("\t" + indentText + "End Site" + "\n");
			buffer.append("\t" + indentText + "{" + "\n");
			buffer.append("\t" + indentText + "\t" + node.getEndSite().toString() + "\n");
			buffer.append("\t" + indentText + "}" + "\n");
		}
		buffer.append(indentText + "}" + "\n");
	}
}