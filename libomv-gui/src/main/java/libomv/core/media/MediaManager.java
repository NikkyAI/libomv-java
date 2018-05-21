/**
 * Copyright (c) 2011-2012, Frederick Martian
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
 * - Neither the name of the libomv-java project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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
package libomv.core.media;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import libomv.Sounds;
import libomv.Gui.AppSettings;
import libomv.Gui.windows.MainControl;
import libomv.io.AgentManager;
import libomv.io.GridClient;
import libomv.io.SimulatorManager;
import libomv.model.Simulator;
import libomv.model.agent.ChatCallbackArgs;
import libomv.model.agent.ChatType;
import libomv.model.asset.AssetDownload;
import libomv.model.asset.AssetType;
import libomv.model.network.SimChangedCallbackArgs;
import libomv.model.object.KillObjectsCallbackArgs;
import libomv.model.object.PrimCallbackArgs;
import libomv.model.sound.AttachedSoundCallbackArgs;
import libomv.model.sound.PreloadSoundCallbackArgs;
import libomv.model.sound.SoundTriggerCallbackArgs;
import libomv.primitives.Primitive;
import libomv.primitives.Primitive.SoundFlags;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.TimeoutEvent;

public class MediaManager extends MediaObject {
	private static final Logger logger = Logger.getLogger(MediaManager.class);

	private MainControl _Main;
	private Thread listenerThread;
	private TimeoutEvent<Boolean> listenerActive = new TimeoutEvent<Boolean>();

	private SoundSystem system;

	private List<MediaObject> sounds = new ArrayList<>();

	// Vectors used for orienting spatial axes.
	protected static Vector3 upVector;

	/**
	 * Indicates whether sound system is available and ready for use
	 */
	private boolean soundSystemAvailable;

	public boolean getSoundSystemAvailable() {
		return soundSystemAvailable;
	}

	/**
	 * Control the volume of all inworld sounds
	 */
	public void setObjectVolume(float value) {
		allObjectVolume = value;
		BufferSound.adjustVolumes();
	}

	public float getObjectVolume() {
		return allObjectVolume;
	}

	/**
	 * Enable and Disable inworld sounds
	 */
	private boolean objectEnabled = true;

	public boolean getObjectEnable() {
		return objectEnabled;
	}

	public void setObjectEnable(boolean value) {
		if (value) {
			// Subscribe to events about inworld sounds
			RegisterClientEvents(_Main.getGridClient());
			logger.info("Inworld sound enabled");
		} else {
			// Unsubscribe to events about inworld sounds
			UnregisterClientEvents(_Main.getGridClient());
			// Stop all running sounds
			BufferSound.killAll();
			logger.info("Inworld sound disabled");
		}
		objectEnabled = value;
	}

	/**
	 * UI sounds volume
	 */
	public float UIVolume = 0.5f;

	public MediaManager(MainControl main) throws InterruptedException {
		_Main = main;
		manager = this;
		RegisterClientEvents(_Main.getGridClient());

		soundSystemAvailable = _Main.getAppSettings().get(AppSettings.disableSound, false);
		if (soundSystemAvailable) {
			return;
		}

		upVector = new Vector3(0.0f, 1.0f, 0.0f);

		lineCallback = new DispatchEndCallback();
		allBuffers = new HashMap<UUID, BufferSound>();
		allSounds = new HashMap<SoundSource, MediaObject>();
		allChannels = new HashMap<SoundChannel, MediaObject>();

		system = new SoundSystem();

		// Start the background thread that updates listener position.
		listenerThread = new Thread(new ListenerUpdate(), "ListenerThread");
		listenerThread.setDaemon(true);
		listenerThread.start();

		// _client.ClientChanged += new
		// EventHandler<ClientChangedEventArgs>(Instance_ClientChanged);

		// Wait for init to complete
		listenerActive.waitOne(10000);
	}

	public void dispose() {
		super.dispose();
		if (_Main != null)
			UnregisterClientEvents(_Main.getGridClient());

		synchronized (sounds) {
			for (int i = 0; i < sounds.size(); i++) {
				if (!sounds.get(i).getDisposed())
					sounds.get(i).dispose();
			}
			sounds.clear();
		}

		sounds = null;

		if (listenerThread != null) {
			listenerActive.set(false);
			listenerThread = null;
		}
		super.dispose();
	}

	public void RequestAsset(UUID soundID, AssetType type, boolean priority, Callback<AssetDownload> callback)
			throws Exception {
		_Main.getGridClient().assets.requestAsset(soundID, type, priority, callback);
	}

	/**
	 * Thread to update listener position and generally keep SoundSystem up to date.
	 */
	private class ListenerUpdate implements Runnable {
		public void run() {
			// Notice changes in position or direction.
			Vector3 lastpos = new Vector3(0.0f, 0.0f, 0.0f);
			float lastface = 0.0f;

			listenerActive.set(true);

			try {
				// Two updates per second.
				while (listenerActive.waitOne(500)) {
					if (system == null)
						continue;

					AgentManager my = _Main.getGridClient().agent;
					Vector3 newPosition = new Vector3(my.getAgentPosition());
					float newFace = my.getAgentRotation().W;

					// If we are standing still, nothing to update now, but
					// FMOD needs a 'tick' anyway for callbacks, etc. In looping
					// 'game' programs, the loop is the 'tick'. Since Radegast
					// uses events and has no loop, we use this position update
					// thread to drive the FMOD tick. Have to move more than
					// 500mm or turn more than 10 desgrees to bother with.
					//
					if (!newPosition.approxEquals(lastpos, 0.5f) || Math.abs(newFace - lastface) >= 0.2) {

						// We have moved or turned. Remember new position.
						lastpos = newPosition;
						lastface = newFace;

						// Get azimuth from the facing Quaternion. Note we assume the
						// avatar is standing upright. Avatars in unusual positions
						// hear things from unpredictable directions.
						// By definition, facing.W = Cos( angle/2 )
						// With angle=0 meaning East.
						double angle = 2.0 * Math.acos(newFace);

						// Construct facing unit vector in FMOD coordinates. Z is East, X is South, Y is
						// up.
						Vector3 forward = new Vector3((float) Math.sin(angle), // South
								0.0f, (float) Math.cos(angle)); // East

						system.set3DListenerAttributes(newPosition, // Position
								null, // Velocity
								forward, // Facing direction
								upVector); // Top of head
					}
					system.update();
				}
			} catch (InterruptedException ex) {
				logger.error("Interrupt in ListenerThread", ex);
			}
		}
	}

	/*
	 * void Instance_ClientChanged(object sender, ClientChangedEventArgs e) {
	 * UnregisterClientEvents(e.OldClient); if (getObjectEnable())
	 * RegisterClientEvents(e.Client); }
	 */
	/**
	 * Handle request to play a sound, which might (or might not) have been
	 * preloaded.
	 */
	private class Sound_SoundTrigger implements Callback<SoundTriggerCallbackArgs> {
		public boolean callback(SoundTriggerCallbackArgs args) {
			if (!args.getSoundID().equals(UUID.Zero)) {

				logger.debug("Trigger sound " + args.getSoundID() + " in object " + args.getObjectID());

				new BufferSound(args.getObjectID(), args.getSoundID(), false, true, args.getPosition(),
						args.getGain() * getObjectVolume());
			}
			return false;
		}
	}

	private Callback<SoundTriggerCallbackArgs> soundTriggerCallback = new Sound_SoundTrigger();

	/**
	 * Handle sound attached to an object
	 */
	private class Sound_AttachedSound implements Callback<AttachedSoundCallbackArgs> {
		// This event tells us the Object ID, but not the Prim info directly.
		public boolean callback(AttachedSoundCallbackArgs args) {
			// So we look it up in our internal Object memory.
			SimulatorManager simulator = (SimulatorManager) args.getSimulator();
			Primitive prim = null;
			synchronized (simulator.getObjectsPrimitives()) {
				for (Primitive p : simulator.getObjectsPrimitives().values()) {
					if (p.id.equals(args.getObjectID())) {
						prim = p;
						break;
					}
				}
			}
			if (prim == null)
				return false;

			// Only one attached sound per prim, so we kill any previous
			BufferSound.kill(prim.id);

			// If this is stop sound, we're done since we've already killed sound for this
			// object
			if ((args.getFlags() & SoundFlags.Stop) == SoundFlags.Stop)
				return false;

			// We seem to get a lot of these zero sounds.
			if (args.getSoundID().equals(UUID.Zero))
				return false;

			// If this is a child prim, its position is relative to the root.
			Vector3 fullPosition = prim.position;

			while (prim != null && prim.parentID != 0) {
				if (simulator.getObjectsAvatars().containsKey(prim.parentID)) {
					prim = simulator.getObjectsAvatars().get(prim.parentID);
					fullPosition.add(prim.position);
				} else {
					if (simulator.getObjectsPrimitives().containsKey(prim.parentID)) {
						fullPosition.add(prim.position);
					}
				}
			}

			// Did find root prim?
			if (prim != null)
				new BufferSound(args.getObjectID(), args.getSoundID(),
						(args.getFlags() & SoundFlags.Loop) == SoundFlags.Loop, true, fullPosition,
						args.getGain() * getObjectVolume());
			return false;
		}
	}

	private Callback<AttachedSoundCallbackArgs> soundAttachedCallback = new Sound_AttachedSound();

	/**
	 * Handle request to preload a sound for playing later.
	 */
	private class Sound_PreloadSound implements Callback<PreloadSoundCallbackArgs> {
		public boolean callback(PreloadSoundCallbackArgs args) {
			if (!args.getSoundID().equals(UUID.Zero)) {
				if (!_Main.getGridClient().assets.getCache().containsKey(args.getSoundID(), null))
					new BufferSound(args.getSoundID());
			}
			return false;
		}
	}

	private Callback<PreloadSoundCallbackArgs> soundPreloadCallback = new Sound_PreloadSound();

	/**
	 * Common object sound processing for various Update events
	 *
	 * @param prim
	 * @param sim
	 */
	private void HandleObjectSound(Primitive prim, Simulator sim) {
		SimulatorManager simulator = (SimulatorManager) sim;
		// Objects without sounds are not interesting.
		if (prim.soundID == null || prim.soundID.equals(UUID.Zero))
			return;

		if ((prim.soundFlags & SoundFlags.Stop) == SoundFlags.Stop) {
			BufferSound.kill(prim.soundID);
			return;
		}

		// If this is a child prim, its position is relative to the root prim.
		Vector3 fullPosition = prim.position;
		if (prim.parentID != 0) {
			synchronized (simulator.getObjectsPrimitives()) {
				if (!simulator.getObjectsPrimitives().containsKey(prim.parentID))
					return;
				fullPosition.add(simulator.getObjectsPrimitives().get(prim.parentID).position);
			}
		}

		// See if this is an update to something we already know about.
		if (allBuffers.containsKey(prim.soundID)) {
			// Exists already, so modify existing sound.
			BufferSound snd = allBuffers.get(prim.soundID);
			snd.volume = prim.soundGain * getObjectVolume();
			snd.position = fullPosition;
		} else {
			// Does not exist, so create a new one.
			new BufferSound(prim.id, prim.soundID, (prim.soundFlags & SoundFlags.Loop) == SoundFlags.Loop, true,
					fullPosition, prim.soundGain * getObjectVolume());
		}
	}

	/**
	 * Handle object updates, looking for sound events
	 */
	private class Objects_ObjectUpdate implements Callback<PrimCallbackArgs> {
		public boolean callback(PrimCallbackArgs args) {
			HandleObjectSound(args.getPrim(), args.getSimulator());
			return false;
		}
	}

	private Callback<PrimCallbackArgs> primCallback = new Objects_ObjectUpdate();

	/**
	 * Handle deletion of a noise-making object
	 */
	private class Objects_KillObject implements Callback<KillObjectsCallbackArgs> {
		public boolean callback(KillObjectsCallbackArgs args) {
			final SimulatorManager simulator = (SimulatorManager) args.getSimulator();
			synchronized (simulator.getObjectsPrimitives()) {
				Map<Integer, Primitive> prims = simulator.getObjectsPrimitives();
				for (int obj : args.getObjectLocalIDs()) {
					Primitive p = prims.get(obj);
					// Objects without sounds are not interesting.
					if (p != null && !UUID.isZeroOrNull(p.soundID))
						BufferSound.kill(p.id);
				}
			}
			return false;
		}
	}

	private Callback<KillObjectsCallbackArgs> objectKillCallback = new Objects_KillObject();

	/**
	 * Watch for Teleports to cancel all the old sounds
	 */
	private class Network_SimChanged implements Callback<SimChangedCallbackArgs> {
		public boolean callback(SimChangedCallbackArgs args) {
			BufferSound.killAll();
			return false;
		}
	}

	private Callback<SimChangedCallbackArgs> simChangedCallback = new Network_SimChanged();

	private class Self_ChatFromSimulator implements Callback<ChatCallbackArgs> {
		public boolean callback(ChatCallbackArgs args) {
			if (args.getType() == ChatType.StartTyping) {
				new BufferSound(new UUID(), Sounds.KEYBOARD_LOOP, false, true, args.getPosition(),
						getObjectVolume() / 2f);
			}
			return false;
		}
	}

	private Callback<ChatCallbackArgs> chatCallback = new Self_ChatFromSimulator();

	void RegisterClientEvents(GridClient client) {
		if (client != null) {
			client.sound.onSoundTrigger.add(soundTriggerCallback);
			client.sound.onAttachedSound.add(soundAttachedCallback);
			client.sound.onPreloadSound.add(soundPreloadCallback);
			client.objects.onObjectUpdate.add(primCallback);
			client.objects.onKillObject.add(objectKillCallback);
			client.network.onSimChanged.add(simChangedCallback);
			client.agent.onChat.add(chatCallback);
		}
	}

	void UnregisterClientEvents(GridClient client) {
		if (client != null) {
			client.sound.onSoundTrigger.remove(soundTriggerCallback);
			client.sound.onAttachedSound.remove(soundAttachedCallback);
			client.sound.onPreloadSound.remove(soundPreloadCallback);
			client.objects.onObjectUpdate.remove(primCallback);
			client.objects.onKillObject.remove(objectKillCallback);
			client.network.onSimChanged.remove(simChangedCallback);
			client.agent.onChat.remove(chatCallback);
		}
	}

	/**
	 * Plays a sound
	 *
	 * @param sound
	 *            The UUID of the sound to play
	 */
	public void playUISound(UUID sound) {
		if (!soundSystemAvailable)
			return;

		new BufferSound(new UUID(), sound, false, true, _Main.getGridClient().agent.getAgentPosition(), UIVolume);
	}
}
