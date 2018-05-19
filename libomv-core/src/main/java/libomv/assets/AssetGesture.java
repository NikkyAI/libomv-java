/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
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
package libomv.assets;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import libomv.model.asset.AssetType;
import libomv.types.UUID;
import libomv.utils.Helpers;

/* Represents a sequence of animations, sounds, and chat actions */
public class AssetGesture extends AssetItem {
	private static final Logger logger = Logger.getLogger(AssetGesture.class);

	/* Type of gesture step */
	public enum GestureStepType {
		Animation, Sound, Chat, Wait, EOF,
	}

	/* Base class for gesture steps */
	public abstract class GestureStep {
		/* Returns what kind of gesture step this is */
		public abstract GestureStepType getGestureStepType();
	}

	/* Describes animation step of a gesture */
	public class GestureStepAnimation extends GestureStep {
		/* Returns what kind of gesture step this is */
		@Override
		public GestureStepType getGestureStepType() {
			return GestureStepType.Animation;
		}

		/*
		 * If true, this step represents start of animation, otherwise animation stop
		 */
		public boolean animationStart = true;

		/* Animation asset <see cref="UUID"/> */
		public UUID id;

		/* Animation inventory name */
		public String name;

		@Override
		public String toString() {
			if (animationStart) {
				return "Start animation: " + name;
			}
			return "Stop animation: " + name;
		}
	}

	/* Describes sound step of a gesture */
	public class GestureStepSound extends GestureStep {
		/* Returns what kind of gesture step this is */
		@Override
		public GestureStepType getGestureStepType() {
			return GestureStepType.Sound;
		}

		/* Sound asset <see cref="UUID"/> */
		public UUID id;

		/* Sound inventory name */
		public String name;

		@Override
		public String toString() {
			return "Sound: " + name;
		}

	}

	/* Describes sound step of a gesture */
	public class GestureStepChat extends GestureStep {
		/* Returns what kind of gesture step this is */
		@Override
		public GestureStepType getGestureStepType() {
			return GestureStepType.Chat;
		}

		/* Text to output in chat */
		public String text;

		@Override
		public String toString() {
			return "Chat: " + text;
		}
	}

	/* Describes sound step of a gesture */
	public class GestureStepWait extends GestureStep {
		/* Returns what kind of gesture step this is */
		@Override
		public GestureStepType getGestureStepType() {
			return GestureStepType.Wait;
		}

		/* If true in this step we wait for all animations to finish */
		public boolean waitForAnimation;

		/* If true gesture player should wait for the specified amount of time */
		public boolean waitForTime;

		/* Time in seconds to wait if WaitForAnimation is false */
		public float waitTime;

		@Override
		public String toString() {
			StringBuilder ret = new StringBuilder("-- Wait for: ");

			if (waitForAnimation) {
				ret.append("(animations to finish) ");
			}

			if (waitForTime) {
				ret.append(String.format("(time {0:0.0}s)", waitTime));
			}

			return ret.toString();
		}
	}

	/* Describes the final step of a gesture */
	public class GestureStepEOF extends GestureStep {
		/* Returns what kind of gesture step this is */
		@Override
		public GestureStepType getGestureStepType() {
			return GestureStepType.EOF;
		}

		@Override
		public String toString() {
			return "End of guesture sequence";
		}
	}

	/* Returns asset type */
	@Override
	public AssetType getAssetType() {
		return AssetType.Gesture;
	}

	/* Keyboard key that triggers the gesture */
	private byte triggerKey;

	public byte getTriggerKey() {
		return triggerKey;
	}

	public void setTriggerKey(byte triggerKey) {
		invalidateAssetData();
		this.triggerKey = triggerKey;
	}

	/* Modifier to the trigger key */
	private int triggerKeyMask;

	public int getTriggerKeyMask() {
		return triggerKeyMask;
	}

	public void setTriggerKeyMask(int triggerKeyMask) {
		invalidateAssetData();
		this.triggerKeyMask = triggerKeyMask;
	}

	/* String that triggers playing of the gesture sequence */
	private String trigger;

	public String getTrigger() {
		return trigger;
	}

	public void setTrigger(String trigger) {
		invalidateAssetData();
		this.trigger = trigger;
	}

	/* Text that replaces trigger in chat once gesture is triggered */
	private String replaceWith;

	public String getReplaceWith() {
		return replaceWith;
	}

	public void setReplaceWith(String replaceWith) {
		invalidateAssetData();
		this.replaceWith = replaceWith;
	}

	/* Sequence of gesture steps */
	private List<GestureStep> sequence;

	public List<GestureStep> getSequence() {
		return sequence;
	}

	public void setSequence(List<GestureStep> sequence) {
		invalidateAssetData();
		this.sequence = sequence;
	}

	/**
	 * Constructs gesture asset
	 *
	 * @param assetID
	 *            A unique <see cref="UUID"/> specific to this asset
	 * @param assetData
	 *            A byte array containing the raw asset data
	 */
	public AssetGesture(UUID assetID, byte[] assetData) {
		super(assetID, assetData);
	}

	/**
	 * Encodes gesture asset suitable for upload
	 */
	@Override
	protected void encode() {
		StringBuilder sb = new StringBuilder();
		sb.append("2\n");
		sb.append(triggerKey + "\n");
		sb.append(triggerKeyMask + "\n");
		sb.append(trigger + "\n");
		sb.append(replaceWith + "\n");

		int count = 0;
		if (sequence != null) {
			count = sequence.size();
		}

		sb.append(count + "\n");

		for (int i = 0; i < count; i++) {
			GestureStep step = sequence.get(i);
			sb.append(step.getGestureStepType() + "\n");

			switch (step.getGestureStepType()) {
			case EOF:
				break;

			case Animation:
				GestureStepAnimation animstep = (GestureStepAnimation) step;
				sb.append(animstep.name + "\n");
				sb.append(animstep.id + "\n");

				if (animstep.animationStart) {
					sb.append("0\n");
				} else {
					sb.append("1\n");
				}
				break;

			case Sound:
				GestureStepSound soundstep = (GestureStepSound) step;
				sb.append(soundstep.name + "\n");
				sb.append(soundstep.id + "\n");
				sb.append("0\n");
				break;

			case Chat:
				GestureStepChat chatstep = (GestureStepChat) step;
				sb.append(chatstep.text + "\n");
				sb.append("0\n");
				break;

			case Wait:
				GestureStepWait waitstep = (GestureStepWait) step;
				sb.append(String.format("{0:0.000000}\n", waitstep.waitTime));
				int waitflags = 0;

				if (waitstep.waitForTime) {
					waitflags |= 0x01;
				}

				if (waitstep.waitForAnimation) {
					waitflags |= 0x02;
				}

				sb.append(waitflags + "\n");
				break;
			default:
				break;
			}
		}
		assetData = Helpers.StringToBytes(sb.toString());
	}

	/**
	 * Decodes gesture asset into play sequence
	 *
	 * @return true if the asset data was decoded successfully
	 */
	@Override
	protected boolean decode() {
		if (assetData == null)
			return false;

		try {
			String[] lines = Helpers.BytesToString(assetData).split("\n");
			sequence = new ArrayList<GestureStep>();

			int i = 0;

			// version
			int version = Integer.parseInt(lines[i++]);
			if (version != 2) {
				throw new Exception("Only know how to decode version 2 of gesture asset");
			}

			triggerKey = Byte.parseByte(lines[i++]);
			triggerKeyMask = Integer.parseInt(lines[i++]);
			trigger = lines[i++];
			replaceWith = lines[i++];

			int count = Integer.parseInt(lines[i++]);

			if (count < 0) {
				throw new Exception("Wrong number of gesture steps");
			}

			for (int n = 0; n < count; n++) {
				GestureStepType type = GestureStepType.values()[Integer.parseInt(lines[i++])];
				int flags;

				switch (type) {
				case EOF:
					break;
				case Animation:
					GestureStepAnimation ani = new GestureStepAnimation();
					ani.name = lines[i++];
					ani.id = new UUID(lines[i++]);
					flags = Integer.parseInt(lines[i++]);

					if (flags == 0) {
						ani.animationStart = true;
					} else {
						ani.animationStart = false;
					}

					sequence.add(ani);
					break;
				case Sound:
					GestureStepSound snd = new GestureStepSound();
					snd.name = lines[i++].replace("\r", "");
					snd.id = new UUID(lines[i++]);
					flags = Integer.parseInt(lines[i++]);

					sequence.add(snd);
					break;
				case Chat:
					GestureStepChat chat = new GestureStepChat();
					chat.text = lines[i++];
					flags = Integer.parseInt(lines[i++]);

					sequence.add(chat);
					break;
				case Wait:
					GestureStepWait wait = new GestureStepWait();
					wait.waitTime = Float.parseFloat(lines[i++]);
					flags = Integer.parseInt(lines[i++]);

					wait.waitForTime = (flags & 0x01) != 0;
					wait.waitForAnimation = (flags & 0x02) != 0;
					sequence.add(wait);
					break;
				default:
					break;
				}
			}
			return true;
		} catch (Exception ex) {
			logger.error("Decoding gesture asset failed:", ex);
			return false;
		}
	}
}