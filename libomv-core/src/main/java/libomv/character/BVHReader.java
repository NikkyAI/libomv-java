/**
 * Copyright (c) 2011 aki@akjava.com
 * Copyright (c) 2011-2017, Frederick Martian
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
import java.util.List;
import java.util.Map;

import libomv.types.Matrix4;
import libomv.types.Quaternion;
import libomv.types.Vector3;

public class BVHReader extends KeyFrameMotion {
	private float INCHES_TO_METERS = 0.02540005f;

	private float POSITION_KEYFRAME_THRESHOLD = 0.03f;
	private float ROTATION_KEYFRAME_THRESHOLD = 0.01f;

	private float POSITION_MOTION_THRESHOLD = 0.001f;
	private float ROTATION_MOTION_THRESHOLD = 0.001f;

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
	private BVH bvh;
	private List<BVHNode> nodes = new ArrayList<>();
	private List<Constraint> constraintList = new ArrayList<>();

	// Translation values
	Map<String, BVHTranslation> translations = new HashMap<>();

	public BVHReader(BufferedReader reader) throws InvalidLineException, IOException {
		super();
		mode = HIERARCHY;
		loadTranslationTable("anim.ini");
		loadBVHFile(reader);
		optimize();
		translate();
		constraintList.clear();
	}

	private void loadTranslationTable(String filename) throws InvalidLineException, IOException {
		translations.clear();
		constraintList.clear();
		InputStream st = getClass().getResourceAsStream("/res/" + filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(st));
		try {
			int i = 1;
			String line = br.readLine();

			if (!line.startsWith("Translations 1.0")) {
				throw new InvalidLineException(i, line, "Invalid Translation file header");
			}
			boolean loadingGlobals = false;
			BVHTranslation trans = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				i++;

				/* if the line is empty or a comment ignore it */
				if (line.isEmpty() || line.startsWith("#"))
					continue;

				if (line.startsWith("[")) {
					if (line.startsWith("[GLOBALS]")) {
						loadingGlobals = true;
					} else {
						loadingGlobals = false;
						trans = new BVHTranslation();
						translations.put(line.substring(1, line.indexOf("]") - 1), trans);
					}
					continue;
				}

				int offset = line.indexOf("=");
				String vals[], token = line.substring(0, offset - 1).trim();
				line = line.substring(offset + 1).trim();

				if (loadingGlobals) {
					if (token.compareToIgnoreCase("emote") == 0) {
						expressionName = line;
					} else if (token.compareToIgnoreCase("priority") == 0) {
						priority = Integer.parseInt(line);
					} else if (token.compareToIgnoreCase("loop") == 0) {
						vals = line.split(" ");
						float loop_in = 0.f;
						float loop_out = 1.f;
						if (vals.length >= 2) {
							loop = true;
							loop_in = Float.parseFloat(vals[0]);
							loop_out = Float.parseFloat(vals[1]);
						} else if (vals.length == 1) {
							loop = vals[0].compareToIgnoreCase("true") == 0;
						}
						inPoint = loop_in * length;
						outPoint = loop_out * length;
					} else if (token.compareToIgnoreCase("easein") == 0) {
						easeInTime = Float.parseFloat(line);
					} else if (token.compareToIgnoreCase("easeout") == 0) {
						easeOutTime = Float.parseFloat(line);
					} else if (token.compareToIgnoreCase("hand") == 0) {
						handPose = Integer.parseInt(line);
					} else if (token.compareToIgnoreCase("constraint") == 0
							|| token.compareToIgnoreCase("planar_constraint") == 0) {
						Constraint constraint = new Constraint();
						vals = line.split(" ");
						if (vals.length >= 13) {
							constraint.chainLength = Integer.parseInt(vals[0]);
							constraint.easeInStart = Float.parseFloat(vals[1]);
							constraint.easeInStop = Float.parseFloat(vals[2]);
							constraint.easeOutStart = Float.parseFloat(vals[3]);
							constraint.easeOutStop = Float.parseFloat(vals[4]);
							constraint.sourceJointName = vals[5];
							constraint.sourceOffset = new Vector3(Float.parseFloat(vals[6]), Float.parseFloat(vals[7]),
									Float.parseFloat(vals[8]));
							constraint.targetJointName = vals[9];
							constraint.targetOffset = new Vector3(Float.parseFloat(vals[10]),
									Float.parseFloat(vals[11]), Float.parseFloat(vals[12]));
							if (vals.length >= 16) {
								constraint.targetDir = new Vector3(Float.parseFloat(vals[13]),
										Float.parseFloat(vals[14]), Float.parseFloat(vals[15])).normalize();
							}
						} else {
							throw new InvalidLineException(i, line, "Invalid constraint entry");
						}
						if (token.compareToIgnoreCase("constraint") == 0)
							constraint.constraintType = EConstraintType.CONSTRAINT_TYPE_POINT;
						else
							constraint.constraintType = EConstraintType.CONSTRAINT_TYPE_PLANE;
						constraintList.add(constraint);
					}
				} else if (trans == null) {
					throw new InvalidLineException(i, line, "Invalid Translation file format");
				} else if (token.compareToIgnoreCase("ignore") == 0) {
					trans.mIgnore = line.compareToIgnoreCase("true") == 0;
				} else if (token.compareToIgnoreCase("relativepos") == 0) {
					vals = line.split(" ");
					if (vals.length >= 3) {
						trans.mRelativePosition = new Vector3(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]),
								Float.parseFloat(vals[2]));
					} else if (vals.length >= 1 && vals[0].compareToIgnoreCase("firstkey") == 0) {
						trans.mRelativePositionKey = true;
					} else {
						throw new InvalidLineException(i, line, "No relative key");
					}
				} else if (token.compareToIgnoreCase("relativerot") == 0) {
					if (line.compareToIgnoreCase("firstkey") == 0) {
						trans.mRelativePositionKey = true;
					} else {
						throw new InvalidLineException(i, line, "No relative key");
					}
				} else if (token.compareToIgnoreCase("outname") == 0) {
					if (!line.isEmpty()) {
						trans.mOutName = line;
					} else {
						throw new InvalidLineException(i, line, "No valid outname");
					}
				} else if (token.compareToIgnoreCase("frame") == 0) {
					vals = line.split("[, ]+");
					if (vals.length >= 9) {
						trans.mFrameMatrix = new Matrix4(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]),
								Float.parseFloat(vals[2]), 0, Float.parseFloat(vals[3]), Float.parseFloat(vals[4]),
								Float.parseFloat(vals[5]), 0, Float.parseFloat(vals[6]), Float.parseFloat(vals[7]),
								Float.parseFloat(vals[8]), 0, 0, 0, 0, 0);
					} else {
						throw new InvalidLineException(i, line, "No valid matrix");
					}
				} else if (token.compareToIgnoreCase("offset") == 0) {
					vals = line.split("[, ]+");
					if (vals.length >= 9) {
						trans.mOffsetMatrix = new Matrix4(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]),
								Float.parseFloat(vals[2]), 0, Float.parseFloat(vals[3]), Float.parseFloat(vals[4]),
								Float.parseFloat(vals[5]), 0, Float.parseFloat(vals[6]), Float.parseFloat(vals[7]),
								Float.parseFloat(vals[8]), 0, 0, 0, 0, 0);
					} else {
						throw new InvalidLineException(i, line, "No valid matrix");
					}
				} else if (token.compareToIgnoreCase("mergeparent") == 0) {
					if (!line.isEmpty()) {
						trans.mMergeParentName = line;
					} else {
						throw new InvalidLineException(i, line, "No valid merge parent");
					}
				} else if (token.compareToIgnoreCase("mergechild") == 0) {
					if (!line.isEmpty()) {
						trans.mMergeChildName = line;
					} else {
						throw new InvalidLineException(i, line, "No valid merge parent");
					}
				} else if (token.compareToIgnoreCase("priority") == 0) {
					if (!line.isEmpty()) {
						trans.mPriorityModifier = Integer.parseInt(line);
					} else {
						throw new InvalidLineException(i, line, "No valid priority");
					}
				}
			}
		} finally {
			br.close();
			st.close();
		}
	}

	private void loadBVHFile(BufferedReader reader) throws InvalidLineException, IOException {
		int i = 0, rotOffset = 0;
		String line;
		String[] values;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			i++;
			if (line.isEmpty()) {
				continue; // skip any empty line
			}
			switch (mode) {
			case HIERARCHY:
				if (!line.equals("HIERARCHY")) {
					throw new InvalidLineException(i, line, "Expected HIERARCHY");
				}
				mode = ROOT;
				break;
			case ROOT:
				if (line.startsWith("ROOT")) {
					BVHNode node = new BVHNode();
					String name = line.substring("ROOT".length()).trim();
					node.setName(name);
					node.setTranslation(translations.get(name));
					bvh.setHiearchy(node);
					nodes.add(node);

					mode = JOINT_OPEN;
				} else {
					throw new InvalidLineException(i, line, "Expected ROOT");
				}
				break;
			case JOINT_OPEN:
				if (line.equals("{")) {
					mode = JOINT_INSIDE;
				} else {
					throw new InvalidLineException(i, line, "Expected {");
				}
				break;
			case JOINT_INSIDE:
				values = line.split(" ");
				if (values[0].equals("OFFSET")) {
					if (values.length != 4) {
						throw new InvalidLineException(i, line, "OFFSET value need 3 points");
					}
					float x = toFloat(i, line, values[1]);
					float y = toFloat(i, line, values[2]);
					float z = toFloat(i, line, values[3]);
					getLast().setOffset(new Vector3(x, y, z));
				} else if (values[0].equals("CHANNELS")) {
					if (values.length < 2) {
						throw new InvalidLineException(i, line, "CHANNEL must have 3 values");
					}
					int channelSize = toInt(i, line, values[1]);

					if (channelSize < 1) {
						throw new InvalidLineException(i, line, "CHANNEL size must be larger than 0");
					}

					if (channelSize != values.length - 2) {
						throw new InvalidLineException(i, line, " Invalid CHANNEL size:" + channelSize);
					}
					Channels channel = new Channels(rotOffset);
					for (int j = 2; j < values.length; j++) {
						if (values[j].equals("Xposition")) {
							channel.setXposition(true);
							bvh.add(new NameAndChannel(getLast().getName(), Channels.XPOSITION, channel));
						} else if (values[j].equals("Yposition")) {
							channel.setYposition(true);
							bvh.add(new NameAndChannel(getLast().getName(), Channels.YPOSITION, channel));
						} else if (values[j].equals("Zposition")) {
							channel.setZposition(true);
							bvh.add(new NameAndChannel(getLast().getName(), Channels.ZPOSITION, channel));
						} else if (values[j].equals("Xrotation")) {
							channel.setXrotation(true);
							channel.addOrder("X");
							bvh.add(new NameAndChannel(getLast().getName(), Channels.XROTATION, channel));
						} else if (values[j].equals("Yrotation")) {
							channel.setYrotation(true);
							channel.addOrder("Y");
							bvh.add(new NameAndChannel(getLast().getName(), Channels.YROTATION, channel));
						} else if (values[j].equals("Zrotation")) {
							channel.setZrotation(true);
							channel.addOrder("Z");
							bvh.add(new NameAndChannel(getLast().getName(), Channels.ZROTATION, channel));
						} else {
							throw new InvalidLineException(i, line, " Invalid CHANNEL value:" + values[j]);
						}
					}
					rotOffset += values.length - 2;
					getLast().setChannels(channel);
				} else if (line.equals("End Site")) {
					mode = ENDSITES_OPEN;
				} else if (line.equals("}")) {
					mode = JOINT_INSIDE;
					nodes.remove(getLast()); // pop up

					if (nodes.size() == 0) {
						mode = MOTION;
					}
				} else if (values[0].equals("JOINT")) {
					if (values.length != 2) {
						throw new InvalidLineException(i, line, " Invalid Joint name:" + line);
					}
					String name = values[1];
					BVHNode node = new BVHNode();
					node.setName(name);
					node.setTranslation(translations.get(name));
					getLast().add(node);
					nodes.add(node);
					mode = JOINT_OPEN;
				} else {
					throw new InvalidLineException(i, line, " Invalid Joint inside:" + values[0]);
				}
				break;
			case ENDSITES_OPEN:
				if (line.equals("{")) {
					mode = ENDSITES_INSIDE;
				} else {
					throw new InvalidLineException(i, line, "Expected {");
				}
				break;
			case ENDSITES_INSIDE:
				values = line.split(" ");
				if (values[0].equals("OFFSET")) {
					if (values.length != 4) {
						throw new InvalidLineException(i, line, "OFFSET value need 3 points");
					}
					float x = toFloat(i, line, values[1]);
					float y = toFloat(i, line, values[2]);
					float z = toFloat(i, line, values[3]);
					getLast().addEndSite(new Vector3(x, y, z));
					mode = ENDSITES_CLOSE;
				} else {
					throw new InvalidLineException(i, line, "Endsite only support offset");
				}
				break;
			case ENDSITES_CLOSE:
				if (line.equals("}")) {
					mode = JOINT_CLOSE;
				} else {
					throw new InvalidLineException(i, line, "Expected {");
				}
				break;
			case JOINT_CLOSE:
				if (line.equals("}")) {
					mode = JOINT_INSIDE;// maybe joint again or close
					nodes.remove(getLast());// pop up

					if (nodes.size() == 0) {
						mode = MOTION;
					}
				} else if (line.equals("End Site")) {
					mode = ENDSITES_OPEN;
				} else {
					throw new InvalidLineException(i, line, "Expected {");
				}
				break;
			case MOTION:
				if (line.equals("MOTION")) {
					BVHMotion motion = new BVHMotion();
					bvh.setMotion(motion);
					mode = MOTION_FRAMES;
				} else {
					throw new InvalidLineException(i, line, "Expected MOTION");
				}
				break;
			case MOTION_FRAMES:
				values = line.split(" ");
				if (values[0].equals("Frames:") && values.length == 2) {
					int frames = toInt(i, line, values[1]);
					bvh.getMotion().setFrames(frames);
					mode = MOTION_FRAME_TIME;
				} else {
					throw new InvalidLineException(i, line, "Expected Frames:");
				}
				break;
			case MOTION_FRAME_TIME:
				values = line.split(":"); // not space
				if (values[0].equals("Frame Time") && values.length == 2) {
					float frameTime = toFloat(i, line, values[1].trim());
					bvh.getMotion().setFrameTime(frameTime);
					mode = MOTION_DATA;
				} else {
					throw new InvalidLineException(i, line, "Expected Frame Time");
				}
				break;
			case MOTION_DATA:
				float vs[] = toFloat(i, line);
				bvh.getMotion().getMotions().add(vs);
				break;
			default:
				break;
			}
		}
	}

	private void optimize() throws IOException {
		if (!loop && easeInTime + easeOutTime > length && length != 0.f) {
			float factor = length / (easeInTime + easeOutTime);
			easeInTime *= factor;
			easeOutTime *= factor;
		}

		if (bvh.getMotion().size() == 0) {
			throw new IOException("No motion frames");
		}

		float[] first_frame = bvh.getMotion().getFrameAt(0);
		Vector3 first_frame_pos = new Vector3(first_frame);

		for (BVHNode node : bvh.getNodeList()) {
			boolean pos_changed = false;
			boolean rot_changed = false;

			if (!(node.getTranslation().mIgnore)) {
				int ki = 0, size = bvh.getMotion().size(), rotOffset = node.getChannels().getRotOffset();

				// We need to reverse the channel order, so use the ..Rev function
				Quaternion.Order order = Quaternion.StringToOrderRev(node.getChannels().getOrder());
				Quaternion first_frame_rot = Quaternion.mayaQ(first_frame, rotOffset, order);

				node.ignorePos = new boolean[bvh.getMotion().size()];
				node.ignoreRot = new boolean[bvh.getMotion().size()];

				if (size == 1) {
					// FIXME: use single frame to move pelvis
					// if we have only one keyframe force output for this joint
					rot_changed = true;
				} else {
					// if more than one keyframe, use first frame as reference and skip to second
					// keyframe
					ki++;
				}

				int ki_prev = ki, ki_last_good_pos = ki, ki_last_good_rot = ki;
				int numPosFramesConsidered = 2;
				int numRotFramesConsidered = 2;

				double diff_max = 0;
				float rot_threshold = ROTATION_KEYFRAME_THRESHOLD / Math.max(node.getJoints().size() * 0.33f, 1.f);

				for (; ki < size; ki++) {
					if (ki_prev == ki_last_good_pos) {
						node.numPosKeys++;
						if (Vector3.distance(new Vector3(bvh.getMotion().getFrameAt(ki_prev), rotOffset),
								first_frame_pos) > POSITION_MOTION_THRESHOLD) {
							pos_changed = true;
						}
					} else {
						// check position for noticeable effect
						Vector3 test_pos = new Vector3(bvh.getMotion().getFrameAt(ki_prev), rotOffset);
						Vector3 last_good_pos = new Vector3(bvh.getMotion().getFrameAt(ki_last_good_pos), rotOffset);
						Vector3 current_pos = new Vector3(bvh.getMotion().getFrameAt(ki), rotOffset);
						Vector3 interp_pos = Vector3.lerp(current_pos, last_good_pos, 1.f / numPosFramesConsidered);

						if (Vector3.distance(current_pos, first_frame_pos) > POSITION_MOTION_THRESHOLD) {
							pos_changed = true;
						}

						if (Vector3.distance(interp_pos, test_pos) < POSITION_KEYFRAME_THRESHOLD) {
							node.ignorePos[ki] = true;
							numPosFramesConsidered++;
						} else {
							numPosFramesConsidered = 2;
							ki_last_good_pos = ki_prev;
							node.numPosKeys++;
						}
					}

					Quaternion test_rot = Quaternion.mayaQ(bvh.getMotion().getFrameAt(ki_prev), rotOffset, order);
					float x_delta = Vector3.distance(first_frame_rot.multiply(Vector3.UnitX),
							test_rot.multiply(Vector3.UnitX));
					float y_delta = Vector3.distance(first_frame_rot.multiply(Vector3.UnitY),
							test_rot.multiply(Vector3.UnitY));
					float rot_test = x_delta + y_delta;

					if (ki_prev == ki_last_good_rot) {
						node.numRotKeys++;

						if (rot_test > ROTATION_MOTION_THRESHOLD) {
							rot_changed = true;
						}
					} else {
						// check rotation for noticeable effect
						Quaternion last_good_rot = Quaternion.mayaQ(bvh.getMotion().getFrameAt(ki_last_good_rot),
								rotOffset, order);
						Quaternion current_rot = Quaternion.mayaQ(bvh.getMotion().getFrameAt(ki), rotOffset, order);
						Quaternion interp_rot = Quaternion.lerp(current_rot, last_good_rot,
								1f / numRotFramesConsidered);

						// Test if the rotation has changed significantly since the very first frame. If
						// false
						// for all frames, then we'll just throw out this joint's rotation entirely.
						if (rot_test > ROTATION_MOTION_THRESHOLD) {
							rot_changed = true;
						}
						x_delta = Vector3.distance(interp_rot.multiply(Vector3.UnitX),
								test_rot.multiply(Vector3.UnitX));
						y_delta = Vector3.distance(interp_rot.multiply(Vector3.UnitY),
								test_rot.multiply(Vector3.UnitY));
						rot_test = x_delta + y_delta;

						// Draw a line between the last good keyframe and current. Test the distance
						// between the last
						// frame (current - 1, i.e. ki_prev) and the line. If it's greater than some
						// threshold, then it
						// represents a significant frame and we want to include it.
						if (rot_test >= rot_threshold || (ki + 1 == size && numRotFramesConsidered > 2)) {
							// Add the current test keyframe (which is technically the previous key, i.e.
							// ki_prev).
							numRotFramesConsidered = 2;
							ki_last_good_rot = ki_prev;
							node.numRotKeys++;

							// Add another keyframe between the last good keyframe and current, at whatever
							// point was
							// the most "significant" (i.e. had the largest deviation from the earlier
							// tests).
							// Note that a more robust approach would be test all intermediate keyframes
							// against the
							// line between the last good keyframe and current, but we're settling for this
							// other method
							// because it's significantly faster.
							if (diff_max > 0) {
								if (node.ignoreRot[ki] == true) {
									node.ignoreRot[ki] = false;
									node.numRotKeys++;
								}
								diff_max = 0;
							}
						} else {
							// This keyframe isn't significant enough, throw it away.
							node.ignoreRot[ki] = true;
							numRotFramesConsidered++;
							// Store away the keyframe that has the largest deviation from the interpolated
							// line, for insertion later.
							if (rot_test > diff_max) {
								diff_max = rot_test;
							}
						}
					}
					ki_prev = ki;
				}
			}

			// don't output joints with no motion
			if (!(pos_changed || rot_changed)) {
				node.getTranslation().mIgnore = true;
			}
		}
	}

	// converts BVH contents to our KeyFrameMotion format
	protected void translate() {
		// count number of non-ignored joints
		int j = 0, numJoints = 0;
		for (BVHNode node : bvh.getNodeList()) {
			if (!node.getTranslation().mIgnore)
				numJoints++;
		}

		// fill in header
		joints = new Joint[numJoints];

		Quaternion first_frame_rot = new Quaternion();

		for (BVHNode node : bvh.getNodeList()) {
			if (node.getTranslation().mIgnore)
				continue;

			Joint joint = joints[j] = new Joint();

			joint.name = node.getTranslation().mOutName;
			joint.priority = node.getTranslation().mPriorityModifier;

			// compute coordinate frame rotation
			Quaternion frameRot = new Quaternion(node.getTranslation().mFrameMatrix);
			Quaternion frameRotInv = Quaternion.inverse(frameRot);

			Quaternion offsetRot = new Quaternion(node.getTranslation().mOffsetMatrix);

			// find mergechild and mergeparent nodes, if specified
			Quaternion mergeParentRot, mergeChildRot;
			BVHNode mergeParent = null, mergeChild = null;

			for (BVHNode mnode : bvh.getNodeList()) {
				String name = mnode.getTranslation().mMergeParentName;
				if (!name.isEmpty() && (name.equals(mnode.getName()))) {
					mergeParent = mnode;
				}
				name = mnode.getTranslation().mMergeChildName;
				if (!name.isEmpty() && (name.equals(mnode.getName()))) {
					mergeChild = mnode;
				}
			}

			joint.rotationkeys = new KeyFrameMotion.JointKey[node.numRotKeys];

			Quaternion.Order order = Quaternion.StringToOrderRev(node.getChannels().getOrder());
			int frame = 0, rotOffset = node.getChannels().getRotOffset();
			for (float[] keyFrame : bvh.getMotion().getMotions()) {
				if (frame == 0 && node.getTranslation().mRelativeRotationKey) {
					first_frame_rot = Quaternion.mayaQ(keyFrame, rotOffset, order);
				}

				if (node.ignoreRot[frame]) {
					frame++;
					continue;
				}

				if (mergeParent != null) {
					mergeParentRot = Quaternion.mayaQ(keyFrame, mergeParent.getChannels().getRotOffset(),
							Quaternion.StringToOrderRev(mergeParent.getChannels().getOrder()));
					Quaternion parentFrameRot = new Quaternion(mergeParent.getTranslation().mFrameMatrix);
					Quaternion parentOffsetRot = new Quaternion(mergeParent.getTranslation().mOffsetMatrix);
					mergeParentRot = parentFrameRot.inverse().multiply(mergeParentRot).multiply(parentFrameRot)
							.multiply(parentOffsetRot);
				} else {
					mergeParentRot = Quaternion.Identity;
				}

				if (mergeChild != null) {
					mergeChildRot = Quaternion.mayaQ(keyFrame, mergeChild.getChannels().getRotOffset(),
							Quaternion.StringToOrderRev(mergeChild.getChannels().getOrder()));
					Quaternion childFrameRot = new Quaternion(mergeChild.getTranslation().mFrameMatrix);
					Quaternion childOffsetRot = new Quaternion(mergeChild.getTranslation().mOffsetMatrix);
					mergeChildRot = childFrameRot.inverse().multiply(mergeChildRot).multiply(childFrameRot)
							.multiply(childOffsetRot);
				} else {
					mergeChildRot = Quaternion.Identity;
				}

				Quaternion inRot = Quaternion.mayaQ(keyFrame, rotOffset, order);
				Quaternion outRot = frameRotInv.multiply(mergeChildRot).multiply(inRot).multiply(mergeParentRot)
						.multiply(first_frame_rot.inverse()).multiply(frameRot).multiply(offsetRot);

				joint.rotationkeys[frame] = new JointKey();
				joint.rotationkeys[frame].time = (frame + 1) * bvh.getMotion().getFrameTime();
				joint.rotationkeys[frame].keyElement = outRot.toVector3();

				frame++;
			}

			// output position keys (only for 1st joint)
			if (j == 0 && !node.getTranslation().mIgnorePositions) {
				joint.positionkeys = new KeyFrameMotion.JointKey[node.numPosKeys];

				Vector3 relPos = node.getTranslation().mRelativePosition;
				Vector3 relKey = Vector3.Zero;

				frame = 0;
				for (float[] keyFrame : bvh.getMotion().getMotions()) {
					if ((frame == 0) && node.getTranslation().mRelativePositionKey) {
						relKey = new Vector3(keyFrame, 0);
					}

					if (node.ignorePos[frame]) {
						frame++;
						continue;
					}

					Vector3 inPos = new Vector3(keyFrame, 0).subtract(relKey).multiply(first_frame_rot.inverse());
					Vector3 outPos = inPos.multiply(frameRot).multiply(offsetRot);

					outPos = outPos.multiply(INCHES_TO_METERS).subtract(relPos);

					joint.positionkeys[frame] = new JointKey();
					joint.positionkeys[frame].time = (frame + 1) * bvh.getMotion().getFrameTime();
					joint.positionkeys[frame].keyElement = outPos.clamp(-KeyFrameMotion.MAX_PELVIS_OFFSET,
							KeyFrameMotion.MAX_PELVIS_OFFSET);
					frame++;
				}
				j++;
			} else {
				joint.positionkeys = new KeyFrameMotion.JointKey[0];
				;
			}
		}
		constraints = constraintList.toArray(constraints);
	}

	public boolean validate() {
		if (bvh.getMotion().getFrames() != bvh.getMotion().getMotions().size()) {
			return false;
		}
		return true;
	}

	protected BVHNode getLast() {
		return nodes.get(nodes.size() - 1);
	}

	protected float[] toFloat(int index, String line) throws InvalidLineException {
		String values[] = line.split(" ");
		float vs[] = new float[values.length];
		try {
			for (int i = 0; i < values.length; i++) {
				vs[i] = Float.parseFloat(values[i]);
			}
		} catch (Exception e) {
			throw new InvalidLineException(index, line, "has invalid double");
		}
		return vs;
	}

	protected int toInt(int index, String line, String v) throws InvalidLineException {
		int d = 0;
		try {
			d = Integer.parseInt(v);
		} catch (Exception e) {
			throw new InvalidLineException(index, line, "invalid integer value:" + v);
		}
		return d;
	}

	protected float toFloat(int index, String line, String v) throws InvalidLineException {
		float d = 0;
		try {
			d = Float.parseFloat(v);
		} catch (Exception e) {
			throw new InvalidLineException(index, line, "invalid double value:" + v);
		}
		return d;
	}

	public class InvalidLineException extends Exception {
		private static final long serialVersionUID = 1L;

		public InvalidLineException(int index, String line, String message) {
			super("line " + index + ":" + line + ":message:" + message);
		}
	}
}
