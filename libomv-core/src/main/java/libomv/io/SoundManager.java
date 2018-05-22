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
package libomv.io;

import libomv.model.Simulator;
import libomv.model.sound.AttachedSoundCallbackArgs;
import libomv.model.sound.AttachedSoundGainChangeCallbackArgs;
import libomv.model.sound.PreloadSoundCallbackArgs;
import libomv.model.sound.SoundTriggerCallbackArgs;
import libomv.packets.AttachedSoundGainChangePacket;
import libomv.packets.AttachedSoundPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.PreloadSoundPacket;
import libomv.packets.SoundTriggerPacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackHandler;

public class SoundManager implements PacketCallback {

	public CallbackHandler<AttachedSoundCallbackArgs> onAttachedSound = new CallbackHandler<>();
	public CallbackHandler<SoundTriggerCallbackArgs> onSoundTrigger = new CallbackHandler<>();
	public CallbackHandler<AttachedSoundGainChangeCallbackArgs> onAttachedSoundGainChange = new CallbackHandler<>();
	public CallbackHandler<PreloadSoundCallbackArgs> onPreloadSound = new CallbackHandler<>();

	private final GridClient client;

	/**
	 * Construct a new instance of the SoundManager class, used for playing and
	 * receiving sound assets
	 *
	 * @param client
	 *            A reference to the current GridClient instance
	 */
	public SoundManager(GridClient client) {
		this.client = client;

		this.client.network.registerCallback(PacketType.AttachedSound, this);
		this.client.network.registerCallback(PacketType.AttachedSoundGainChange, this);
		this.client.network.registerCallback(PacketType.PreloadSound, this);
		this.client.network.registerCallback(PacketType.SoundTrigger, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case AttachedSound:
			handleAttachedSound(packet, simulator);
			break;
		case AttachedSoundGainChange:
			handleAttachedSoundGainChange(packet, simulator);
			break;
		case PreloadSound:
			handlePreloadSound(packet, simulator);
			break;
		case SoundTrigger:
			handleSoundTrigger(packet, simulator);
			break;
		default:
			break;
		}
	}

	/**
	 * Plays a sound in the current region at full volume from avatar position
	 *
	 * @param soundID
	 *            UUID of the sound to be played
	 * @param position
	 *            position for the sound to be played at. Normally the avatar
	 * @throws Exception
	 */
	public final void playSound(UUID soundID) throws Exception {
		sendSoundTrigger(soundID, client.getCurrentRegionHandle(), client.agent.getAgentPosition(), 1.0f);
	}

	/**
	 * Plays a sound in the current region at full volume
	 *
	 * @param soundID
	 *            UUID of the sound to be played
	 * @param position
	 *            position for the sound to be played at. Normally the avatar
	 * @throws Exception
	 */
	public final void sendSoundTrigger(UUID soundID, Vector3 position) throws Exception {
		sendSoundTrigger(soundID, client.getCurrentRegionHandle(), position, 1.0f);
	}

	/**
	 * Plays a sound in the current region
	 *
	 * @param soundID
	 *            UUID of the sound to be played
	 * @param position
	 *            position for the sound to be played at. Normally the avatar
	 * @param gain
	 *            volume of the sound, from 0.0 to 1.0
	 * @throws Exception
	 */
	public final void sendSoundTrigger(UUID soundID, Vector3 position, float gain) throws Exception {
		sendSoundTrigger(soundID, client.getCurrentRegionHandle(), position, gain);
	}

	/**
	 * Plays a sound in the specified sim
	 *
	 * @param soundID
	 *            UUID of the sound to be played
	 * @param sim
	 *            simulator where to play the sound in
	 * @param position
	 *            position for the sound to be played at. Normally the avatar
	 * @param gain
	 *            volume of the sound, from 0.0 to 1.0
	 * @throws Exception
	 */
	public final void sendSoundTrigger(UUID soundID, Simulator sim, Vector3 position, float gain) throws Exception {
		sendSoundTrigger(soundID, sim.getHandle(), position, gain);
	}

	/**
	 * Play a sound asset
	 *
	 * @param soundID
	 *            UUID of the sound to be played
	 * @param handle
	 *            handle id for the sim to be played in
	 * @param position
	 *            position for the sound to be played at. Normally the avatar
	 * @param gain
	 *            volume of the sound, from 0.0 to 1.0
	 * @throws Exception
	 */
	public final void sendSoundTrigger(UUID soundID, long handle, Vector3 position, float gain) throws Exception {
		SoundTriggerPacket soundtrigger = new SoundTriggerPacket();
		soundtrigger.SoundData = soundtrigger.new SoundDataBlock();
		soundtrigger.SoundData.SoundID = soundID;
		soundtrigger.SoundData.ObjectID = UUID.ZERO;
		soundtrigger.SoundData.OwnerID = UUID.ZERO;
		soundtrigger.SoundData.ParentID = UUID.ZERO;
		soundtrigger.SoundData.Handle = handle;
		soundtrigger.SoundData.Position = position;
		soundtrigger.SoundData.Gain = gain;

		client.network.sendPacket(soundtrigger);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void handleAttachedSound(Packet packet, Simulator simulator) {
		AttachedSoundPacket sound = (AttachedSoundPacket) packet;

		onAttachedSound.dispatch(new AttachedSoundCallbackArgs(simulator, sound.DataBlock.SoundID,
				sound.DataBlock.OwnerID, sound.DataBlock.ObjectID, sound.DataBlock.Gain, sound.DataBlock.Flags));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void handleAttachedSoundGainChange(Packet packet, Simulator simulator) {
		AttachedSoundGainChangePacket change = (AttachedSoundGainChangePacket) packet;
		onAttachedSoundGainChange.dispatch(
				new AttachedSoundGainChangeCallbackArgs(simulator, change.DataBlock.ObjectID, change.DataBlock.Gain));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void handlePreloadSound(Packet packet, Simulator simulator) {
		PreloadSoundPacket preload = (PreloadSoundPacket) packet;

		for (PreloadSoundPacket.DataBlockBlock data : preload.DataBlock) {
			onPreloadSound.dispatch(new PreloadSoundCallbackArgs(simulator, data.SoundID, data.OwnerID, data.ObjectID));
		}
	}

	/**
	 * Process an incoming <see cref="SoundTriggerPacket"/> packet
	 *
	 * @param packet
	 *            The <see cref="SoundTriggerPacket"/> packet containing the data
	 * @param simulator
	 *            The simulator the packet originated from
	 */
	private final void handleSoundTrigger(Packet packet, Simulator simulator) {
		SoundTriggerPacket trigger = (SoundTriggerPacket) packet;
		onSoundTrigger.dispatch(new SoundTriggerCallbackArgs(simulator, trigger.SoundData.SoundID,
				trigger.SoundData.OwnerID, trigger.SoundData.ObjectID, trigger.SoundData.ParentID,
				trigger.SoundData.Gain, trigger.SoundData.Handle, trigger.SoundData.Position));
	}

}
