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

import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

/* Represents a sequence of animations, sounds, and chat actions */
public class AssetGesture extends AssetItem
{
	/* Type of gesture step */
	public enum GestureStepType
	{
		Animation, Sound, Chat, Wait, EOF,
	}

	/* Base class for gesture steps */
	public abstract class GestureStep
	{
		/* Returns what kind of gesture step this is */
		public abstract GestureStepType getGestureStepType();
	}

	/* Describes animation step of a gesture */
	public class GestureStepAnimation extends GestureStep
	{
		/* Returns what kind of gesture step this is */
		@Override
		public GestureStepType getGestureStepType()
		{
			return GestureStepType.Animation;
		}

		/*
		 * If true, this step represents start of animation, otherwise animation
		 * stop
		 */
		public boolean AnimationStart = true;

		/* Animation asset <see cref="UUID"/> */
		public UUID ID;

		/* Animation inventory name */
		public String Name;

		@Override
		public String toString()
		{
			if (AnimationStart)
			{
				return "Start animation: " + Name;
			}
			return "Stop animation: " + Name;
		}
	}

	/* Describes sound step of a gesture */
	public class GestureStepSound extends GestureStep
	{
		/* Returns what kind of gesture step this is */
		@Override
		public GestureStepType getGestureStepType()
		{
			return GestureStepType.Sound;
		}

		/* Sound asset <see cref="UUID"/> */
		public UUID ID;

		/* Sound inventory name */
		public String Name;

		@Override
		public String toString()
		{
			return "Sound: " + Name;
		}

	}

	/* Describes sound step of a gesture */
	public class GestureStepChat extends GestureStep
	{
		/* Returns what kind of gesture step this is */
		@Override
		public GestureStepType getGestureStepType()
		{
			return GestureStepType.Chat;
		}

		/* Text to output in chat */
		public String Text;

		@Override
		public String toString()
		{
			return "Chat: " + Text;
		}
	}

	/* Describes sound step of a gesture */
	public class GestureStepWait extends GestureStep
	{
		/* Returns what kind of gesture step this is */
		@Override
		public GestureStepType getGestureStepType()
		{
			return GestureStepType.Wait;
		}

		/* If true in this step we wait for all animations to finish */
		public boolean WaitForAnimation;

		/* If true gesture player should wait for the specified amount of time */
		public boolean WaitForTime;

		/* Time in seconds to wait if WaitForAnimation is false */
		public float WaitTime;

		@Override
		public String toString()
		{
			StringBuilder ret = new StringBuilder("-- Wait for: ");

			if (WaitForAnimation)
			{
				ret.append("(animations to finish) ");
			}

			if (WaitForTime)
			{
				ret.append(String.format("(time {0:0.0}s)", WaitTime));
			}

			return ret.toString();
		}
	}

	/* Describes the final step of a gesture */
	public class GestureStepEOF extends GestureStep
	{
		/* Returns what kind of gesture step this is */
		@Override
		public GestureStepType getGestureStepType()
		{
			return GestureStepType.EOF;
		}

		@Override
		public String toString()
		{
			return "End of guesture sequence";
		}
	}

	/* Returns asset type */
	@Override
	public AssetType getAssetType()
	{
		return AssetType.Gesture;
	}

	/* Keyboard key that triggers the gesture */
	private byte TriggerKey;
	
	public byte getTriggerKey()
	{
		return TriggerKey;
	}

	public void setTriggerKey(byte triggerKey)
	{
		invalidateAssetData();
		TriggerKey = triggerKey;
	}

	/* Modifier to the trigger key */
	private int TriggerKeyMask;

	public int getTriggerKeyMask()
	{
		return TriggerKeyMask;
	}

	public void setTriggerKeyMask(int triggerKeyMask)
	{
		invalidateAssetData();
		TriggerKeyMask = triggerKeyMask;
	}

	/* String that triggers playing of the gesture sequence */
	private String Trigger;

	public String getTrigger()
	{
		return Trigger;
	}

	public void setTrigger(String trigger)
	{
		invalidateAssetData();
		Trigger = trigger;
	}

	/* Text that replaces trigger in chat once gesture is triggered */
	private String ReplaceWith;

	public String getReplaceWith()
	{
		return ReplaceWith;
	}

	public void setReplaceWith(String replaceWith)
	{
		invalidateAssetData();
		ReplaceWith = replaceWith;
	}

	/* Sequence of gesture steps */
	private List<GestureStep> Sequence;

	public List<GestureStep> getSequence()
	{
		return Sequence;
	}

	public void setSequence(List<GestureStep> sequence)
	{
		invalidateAssetData();
		Sequence = sequence;
	}

	/**
	 * Constructs gesture asset
	 * 
	 * @param assetID
	 *            A unique <see cref="UUID"/> specific to this asset
	 * @param assetData
	 *            A byte array containing the raw asset data
	 */
	public AssetGesture(UUID assetID, byte[] assetData)
	{
		super(assetID, assetData);
	}

	/**
	 * Encodes gesture asset suitable for upload
	 */
	@Override
	protected void encode()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("2\n");
		sb.append(TriggerKey + "\n");
		sb.append(TriggerKeyMask + "\n");
		sb.append(Trigger + "\n");
		sb.append(ReplaceWith + "\n");

		int count = 0;
		if (Sequence != null)
		{
			count = Sequence.size();
		}

		sb.append(count + "\n");

		for (int i = 0; i < count; i++)
		{
			GestureStep step = Sequence.get(i);
			sb.append(step.getGestureStepType() + "\n");

			switch (step.getGestureStepType())
			{
				case EOF:
					break;

				case Animation:
					GestureStepAnimation animstep = (GestureStepAnimation) step;
					sb.append(animstep.Name + "\n");
					sb.append(animstep.ID + "\n");

					if (animstep.AnimationStart)
					{
						sb.append("0\n");
					}
					else
					{
						sb.append("1\n");
					}
					break;

				case Sound:
					GestureStepSound soundstep = (GestureStepSound) step;
					sb.append(soundstep.Name + "\n");
					sb.append(soundstep.ID + "\n");
					sb.append("0\n");
					break;

				case Chat:
					GestureStepChat chatstep = (GestureStepChat) step;
					sb.append(chatstep.Text + "\n");
					sb.append("0\n");
					break;

				case Wait:
					GestureStepWait waitstep = (GestureStepWait) step;
					sb.append(String.format("{0:0.000000}\n", waitstep.WaitTime));
					int waitflags = 0;

					if (waitstep.WaitForTime)
					{
						waitflags |= 0x01;
					}

					if (waitstep.WaitForAnimation)
					{
						waitflags |= 0x02;
					}

					sb.append(waitflags + "\n");
					break;
                default:
                	break;
			}
		}
		AssetData = Helpers.StringToBytes(sb.toString());
	}

	/**
	 * Decodes gesture asset into play sequence
	 * 
	 * @return true if the asset data was decoded successfully
	 */
	@Override
	protected boolean decode()
	{
		if (AssetData == null)
			return false;
		
		try
		{
			String[] lines = Helpers.BytesToString(AssetData).split("\n");
			Sequence = new ArrayList<GestureStep>();

			int i = 0;

			// version
			int version = Integer.parseInt(lines[i++]);
			if (version != 2)
			{
				throw new Exception("Only know how to decode version 2 of gesture asset");
			}

			TriggerKey = Byte.parseByte(lines[i++]);
			TriggerKeyMask = Integer.parseInt(lines[i++]);
			Trigger = lines[i++];
			ReplaceWith = lines[i++];

			int count = Integer.parseInt(lines[i++]);

			if (count < 0)
			{
				throw new Exception("Wrong number of gesture steps");
			}

			for (int n = 0; n < count; n++)
			{
				GestureStepType type = GestureStepType.values()[Integer.parseInt(lines[i++])];
				int flags;

				switch (type)
				{
					case EOF:
						break;
					case Animation:
						GestureStepAnimation ani = new GestureStepAnimation();
						ani.Name = lines[i++];
						ani.ID = new UUID(lines[i++]);
						flags = Integer.parseInt(lines[i++]);

						if (flags == 0)
						{
							ani.AnimationStart = true;
						}
						else
						{
							ani.AnimationStart = false;
						}

						Sequence.add(ani);
						break;
					case Sound:
						GestureStepSound snd = new GestureStepSound();
						snd.Name = lines[i++].replace("\r", "");
						snd.ID = new UUID(lines[i++]);
						flags = Integer.parseInt(lines[i++]);

						Sequence.add(snd);
						break;
					case Chat:
						GestureStepChat chat = new GestureStepChat();
						chat.Text = lines[i++];
						flags = Integer.parseInt(lines[i++]);

						Sequence.add(chat);
						break;
					case Wait:
						GestureStepWait wait = new GestureStepWait();
						wait.WaitTime = Float.parseFloat(lines[i++]);
						flags = Integer.parseInt(lines[i++]);

						wait.WaitForTime = (flags & 0x01) != 0;
						wait.WaitForAnimation = (flags & 0x02) != 0;
						Sequence.add(wait);
						break;
	                default:
	                	break;
				}
			}
			return true;
		}
		catch (Exception ex)
		{
			Logger.Log("Decoding gesture asset failed:", LogLevel.Error, ex);
			return false;
		}
	}
}