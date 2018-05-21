/**
 * Copyright (c) 2006-2015, openmetaverse.org
 * Copyright (c) 2012-2017, Frederick Martian
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
package libomv.rendering;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LindenSkeleton {

	public class JointBase {
		String name;
		float[] pos;
		float[] rot;
		float[] scale;

		public String getName() {
			return this.name;
		}

		public void setName(String value) {
			this.name = value;
		}

		public float[] getPos() {
			return this.pos;
		}

		public void setPos(float[] value) {
			this.pos = value;
		}

		public float[] getRot() {
			return this.rot;
		}

		public void setRot(float[] value) {
			this.rot = value;
		}

		public float[] getScale() {
			return this.scale;
		}

		public void setScale(float[] value) {
			this.scale = value;
		}
	}

	public class CollisionVolume extends JointBase {
	}

	public class Joint extends JointBase {
		float[] pivot;
		CollisionVolume collision_volume;
		Joint[] bone;

		public CollisionVolume getCollisionVolume() {
			return this.collision_volume;
		}

		public Joint[] getBone() {
			return this.bone;
		}

		public float[] getPivot() {
			return this.pivot;
		}
	}

	private float version;
	private boolean versionSpecified;
	private int num_bones; // totals in this skeleton
	private int num_collision_volumes; // totals in this skeleton

	private Joint bone;

	public Joint getBone() {
		return this.bone;
	}

	protected void setBone(Joint value) {
		this.bone = value;
	}

	public float getVersion() {
		return this.version;
	}

	public boolean getVersionSpecified() {
		return this.versionSpecified;
	}

	public int getNumBones() {
		return this.num_bones;
	}

	public int getNumCollisionVolumes() {
		return this.num_collision_volumes;
	}

	private class SkeletonHandler extends DefaultHandler {
		private LindenSkeleton skeleton;
		private List<List<Joint>> stack;
		private List<Joint> joints;
		private Joint joint = null;

		public SkeletonHandler(LindenSkeleton skeleton) {
			this.skeleton = skeleton;
		}

		public void startDocument() throws SAXException {
			this.stack = new ArrayList<>();
			this.joints = null;
		}

		private float[] createVector(String attribute) {
			float[] values = new float[3];
			String[] numbers = attribute.split(" ");
			if (numbers.length == 3) {
				int i = 0;
				for (String number : numbers) {
					values[i++] = Float.parseFloat(number);
				}
			}
			return values;
		}

		public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
				throws SAXException {
			if (qName.equalsIgnoreCase("bone")) {
				if (joint != null) {
					if (joints == null)
						joints = new ArrayList<>();
					joints.add(joint); // parent into joints array
					stack.add(joints); // joints array onto stack
					joints = null;
				}
				joint = new Joint();
				for (int i = 0; i < atts.getLength(); i++) {
					String name = atts.getQName(i);
					if (name.equalsIgnoreCase("name")) {
						joint.name = atts.getValue(i);
					} else if (name.equalsIgnoreCase("rot")) {
						joint.rot = createVector(atts.getValue(i));
					} else if (name.equalsIgnoreCase("pos")) {
						joint.pos = createVector(atts.getValue(i));
					} else if (name.equalsIgnoreCase("scale")) {
						joint.scale = createVector(atts.getValue(i));
					} else if (name.equalsIgnoreCase("pivot")) {
						joint.pivot = createVector(atts.getValue(i));
					}
				}
			} else if (qName.equalsIgnoreCase("collision_volume")) {
				if (joint == null)
					throw new SAXException("Can't have a collision volume without a parent bone");
				CollisionVolume collision = new CollisionVolume();
				for (int i = 0; i < atts.getLength(); i++) {
					String name = atts.getQName(i);
					if (name.equalsIgnoreCase("name")) {
						collision.name = atts.getValue(i);
					} else if (name.equalsIgnoreCase("rot")) {
						collision.rot = createVector(atts.getValue(i));
					} else if (name.equalsIgnoreCase("pos")) {
						collision.pos = createVector(atts.getValue(i));
					} else if (name.equalsIgnoreCase("scale")) {
						collision.scale = createVector(atts.getValue(i));
					}
				}
				joint.collision_volume = collision;
			} else if (qName.equalsIgnoreCase("linden_skeleton")) {
				for (int i = 0; i < atts.getLength(); i++) {
					String name = atts.getQName(i);
					if (name.equalsIgnoreCase("version")) {
						versionSpecified = true;
						version = Float.parseFloat(atts.getValue(i));
					} else if (name.equalsIgnoreCase("num_bones")) {
						num_bones = Integer.parseInt(atts.getValue(i));
					} else if (name.equalsIgnoreCase("num_collision_volumes")) {
						num_collision_volumes = Integer.parseInt(atts.getValue(i));
					}
				}
			}
		}

		public void endElement(String namespaceURI, String localName, String qName) {
			if (qName.equalsIgnoreCase("bone")) {
				if (joints != null) {
					joint.bone = joints.toArray(new Joint[joints.size()]);
				}
				if (stack.size() > 0) {
					joints = stack.remove(stack.size() - 1);
					joint = joints.get(joints.size() - 1);
				}
			} else if (qName.equalsIgnoreCase("linden_skeleton")) {
				skeleton.bone = joint;
			}
		}
	}

	/**
	 * Load the default skeleton file "character/avatar_skeleton.xml"
	 *
	 * @return the loaded skeleton object or null
	 * @throws Exception
	 */
	static public LindenSkeleton load() throws Exception {
		return load(null);
	}

	/**
	 * Load the default skeleton file "character/avatar_skeleton.xml"
	 *
	 * @param client
	 *            GridClient used to locate the code base as well as the settings
	 *            for the character directory
	 * @param fileName
	 *            the skeleton XML file name to load or if null the default
	 *            "avatar_skeleton.xml"
	 * @return the loaded skeleton object or null
	 * @throws Exception
	 */
	static public LindenSkeleton load(String fileName) throws Exception {
		File charFile = null;
		FileInputStream skeletonData = null;
		LindenSkeleton skeleton = null;

		if (fileName == null) {
			// TODO:FIXME
			// Fix this by reading the loaded client Settings as before.
			// String characterDir = client != null ?
			// client.Settings.getString(LibSettings.CHARACTER_DIR) : "character";
			ClassLoader classLoader = LindenSkeleton.class.getClassLoader();
			charFile = new File(classLoader.getResource("character/avatar_skeleton.xml").getFile());
		} else {
			charFile = new File(fileName);
		}

		try {
			skeletonData = new FileInputStream(charFile);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(false);
			SAXParser parser = factory.newSAXParser();
			skeleton = new LindenSkeleton();
			parser.parse(skeletonData, skeleton.new SkeletonHandler(skeleton));
		} catch (Exception ex) {
			skeleton = null;
			throw ex;
		} finally {
			skeletonData.close();
		}
		return skeleton;
	}

	/**
	 * Build and "expanded" list of joints
	 *
	 * The algorithm is based on this description:
	 *
	 * An "expanded" list of joints, not just a linear array of the joints as
	 * defined in the skeleton file. In particular, any joint that has more than one
	 * child will be repeated in the list for each of its children.
	 *
	 * @param jointsFilter
	 *            The list should only take these joint names in consideration
	 * @returns An "expanded" joints list as a flat list of bone names
	 */
	public List<String> buildExpandedJointList(String[] jointsFilter) {
		List<String> expandedJointList = new ArrayList<>();

		// not really sure about this algorithm, but it seems to fit the bill:
		// and the mesh doesn't seem to be overly distorted
		if (getBone().getBone() != null)
			for (Joint child : getBone().getBone())
				expandJoint(getBone(), child, expandedJointList, jointsFilter);

		return expandedJointList;
	}

	/**
	 * Expand one joint
	 *
	 * @param parentJoint
	 *            The parent of the joint we are operating on
	 * @param currentJoint
	 *            The joint we are supposed to expand
	 * @param expandedJointList
	 *            Joint list that we will extend upon
	 * @param jointsFilter
	 *            The expanded list should only contain these joints
	 */
	private void expandJoint(Joint parentJoint, Joint currentJoint, List<String> expandedJointList,
			String[] jointsFilter) {
		for (String joint : jointsFilter) {
			// does the mesh reference this joint
			if (joint.equals(currentJoint.getName())) {
				if (expandedJointList.size() > 0 && parentJoint != null
						&& parentJoint.getName().equals(expandedJointList.get(expandedJointList.size() - 1))) {
					expandedJointList.add(currentJoint.getName());
				} else {
					if (parentJoint != null) {
						expandedJointList.add(parentJoint.getName());
					} else {
						expandedJointList.add(currentJoint.getName()); // only happens on the root joint
					}

					expandedJointList.add(currentJoint.getName());
				}
			}
		}
		// recurse the joint hierarchy
		if (currentJoint.getBone() != null) {
			for (Joint child : currentJoint.getBone()) {
				expandJoint(currentJoint, child, expandedJointList, jointsFilter);
			}
		}
	}
}
