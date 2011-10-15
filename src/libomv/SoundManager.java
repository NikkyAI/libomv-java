/**
 * Copyright (c) 2006-2009, openmetaverse.org
 * Portions Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
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
package libomv;

import libomv.packets.AttachedSoundGainChangePacket;
import libomv.packets.AttachedSoundPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.PreloadSoundPacket;
import libomv.packets.SoundTriggerPacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;

public class SoundManager implements PacketCallback
{
    // #region Private Members
    private final GridClient _Client;
    // #endregion

    // #region Event Callback Handling
    
    /**
     * Provides data for the <see cref="SoundManager.AttachedSound"/> event
     *
     * The <see cref="SoundManager.AttachedSound"/> event occurs when the simulator sends
     * the sound data which emits from an agents attachment
     */
    public class AttachedSoundCallbackArgs implements CallbackArgs
    {
        private final Simulator m_Simulator;
        private final UUID m_SoundID;
        private final UUID m_OwnerID;
        private final UUID m_ObjectID;
        private final float m_Gain;
        private final byte m_Flags;

        // Simulator where the event originated
        public final Simulator getSimulator()
        {
            return m_Simulator;
        }
        // Get the sound asset id
        public final UUID getSoundID()
        {
            return m_SoundID;
        }
        // Get the ID of the owner
        public final UUID getOwnerID()
        {
            return m_OwnerID;
        }
        // Get the ID of the Object
        public final UUID getObjectID()
        {
            return m_ObjectID;
        }
        // Get the volume level
        public final float getGain()
        {
            return m_Gain;
        }
        // Get the <see cref="SoundFlags"/>
        public final byte getFlags()
        {
            return m_Flags;
        }
        
        public AttachedSoundCallbackArgs(Simulator sim, UUID soundID, UUID ownerID, UUID objectID, float gain, byte flags)
        {
            this.m_Simulator = sim;
            this.m_SoundID = soundID;
            this.m_OwnerID = ownerID;
            this.m_ObjectID = objectID;
            this.m_Gain = gain;
            this.m_Flags = flags;
        }
    }
    
    public CallbackHandler<AttachedSoundCallbackArgs> OnAttachedSound = new CallbackHandler<AttachedSoundCallbackArgs>();

    /**
     * Provides data for the <see cref="SoundManager.SoundTrigger"/> event
     * <p>The <see cref="SoundManager.SoundTrigger"/> event occurs when the simulator forwards
     * a request made by yourself or another agent to play either an asset sound or a built in sound</p>
     *
     * <p>Requests to play sounds where the <see cref="SoundTriggerEventArgs.SoundID"/> is not one of the built-in
     * <see cref="Sounds"/> will require sending a request to download the sound asset before it can be played</p>
     */
    public class SoundTriggerCallbackArgs implements CallbackArgs
    {
        private final Simulator m_Simulator;
        private final UUID m_SoundID;
        private final UUID m_OwnerID;
        private final UUID m_ObjectID;
        private final UUID m_ParentID;
        private final float m_Gain;
        private final long m_RegionHandle;
        private final Vector3 m_Position;

        // Simulator where the event originated
        public final Simulator getSimulator()
        {
            return m_Simulator;
        }
        // Get the sound asset id
        public final UUID getSoundID()
        {
            return m_SoundID;
        }
        // Get the ID of the owner
        public final UUID getOwnerID()
        {
            return m_OwnerID;
        }
        // Get the ID of the Object
        public final UUID getObjectID()
        {
            return m_ObjectID;
        }
        // Get the ID of the objects parent
        public final UUID getParentID()
        {
            return m_ParentID;
        }
        // Get the volume level
        public final float getGain()
        {
            return m_Gain;
        }
        // Get the regionhandle
        public final long getRegionHandle()
        {
            return m_RegionHandle;
        }
        // Get the source position
        public final Vector3 getPosition()
        {
            return m_Position;
        }

        /** 
         * Construct a new instance of the SoundTriggerEventArgs class
         * 
         * @param sim Simulator where the event originated
         * @param soundID The sound asset id
         * @param ownerID The ID of the owner
         * @param objectID The ID of the object
         * @param parentID The ID of the objects parent
         * @param gain The volume level
         * @param regionHandle The regionhandle
         * @param position The source position
         */
        public SoundTriggerCallbackArgs(Simulator sim, UUID soundID, UUID ownerID, UUID objectID, UUID parentID, float gain, long regionHandle, Vector3 position)
        {
            this.m_Simulator = sim;
            this.m_SoundID = soundID;
            this.m_OwnerID = ownerID;
            this.m_ObjectID = objectID;
            this.m_ParentID = parentID;
            this.m_Gain = gain;
            this.m_RegionHandle = regionHandle;
            this.m_Position = position;
        }
    }
    
    public CallbackHandler<SoundTriggerCallbackArgs> OnSoundTrigger = new CallbackHandler<SoundTriggerCallbackArgs>();
    
    
    /**
     * Provides data for the <see cref="SoundManager.AttachedSoundGainChange"/> event
     *
     * The <see cref="SoundManager.AttachedSoundGainChange"/> event occurs when an attached sound
     * changes its volume level
     */
    public class AttachedSoundGainChangeCallbackArgs implements CallbackArgs
    {
        private final Simulator m_Simulator;
        private final UUID m_ObjectID;
        private final float m_Gain;

        // Simulator where the event originated
        //Tangible_doc_comment_end
        public final Simulator getSimulator()
        {
            return m_Simulator;
        }
        // Get the ID of the Object
        //Tangible_doc_comment_end
        public final UUID getObjectID()
        {
            return m_ObjectID;
        }
        // Get the volume level
        //Tangible_doc_comment_end
        public final float getGain()
        {
            return m_Gain;
        }

        /** 
         * Construct a new instance of the AttachedSoundGainChangedEventArgs class
         * 
         * @param sim Simulator where the event originated
         * @param objectID The ID of the Object
         * @param gain The new volume level
         */
        public AttachedSoundGainChangeCallbackArgs(Simulator sim, UUID objectID, float gain)
        {
            this.m_Simulator = sim;
            this.m_ObjectID = objectID;
            this.m_Gain = gain;
        }
    }

   
    public CallbackHandler<AttachedSoundGainChangeCallbackArgs> OnAttachedSoundGainChange = new CallbackHandler<AttachedSoundGainChangeCallbackArgs>();


    public class PreloadSoundCallbackArgs implements CallbackArgs
    {
        private final Simulator m_Simulator;
        private final UUID m_SoundID;
        private final UUID m_OwnerID;
        private final UUID m_ObjectID;

        // Simulator where the event originated
        public final Simulator getSimulator()
        {
            return m_Simulator;
        }
        // Get the sound asset id
        public final UUID getSoundID()
        {
            return m_SoundID;
        }
        // Get the ID of the owner
        public final UUID getOwnerID()
        {
            return m_OwnerID;
        }
        // Get the ID of the Object
        public final UUID getObjectID()
        {
            return m_ObjectID;
        }

        /** 
         * Construct a new instance of the PreloadSoundEventArgs class
         * 
         * @param sim Simulator where the event originated
         * @param soundID The sound asset id
         * @param ownerID The ID of the owner
         * @param objectID The ID of the object
         */
        public PreloadSoundCallbackArgs(Simulator sim, UUID soundID, UUID ownerID, UUID objectID)
        {
            this.m_Simulator = sim;
            this.m_SoundID = soundID;
            this.m_OwnerID = ownerID;
            this.m_ObjectID = objectID;
        }
    }

    public CallbackHandler<PreloadSoundCallbackArgs> OnPreloadSound = new CallbackHandler<PreloadSoundCallbackArgs>();

    // #endregion

    /** 
     * Construct a new instance of the SoundManager class, used for playing and receiving
     * sound assets
     * 
     * @param client A reference to the current GridClient instance
     */
    public SoundManager(GridClient client)
    {
        _Client = client;

        _Client.Network.RegisterCallback(PacketType.AttachedSound, this);
        _Client.Network.RegisterCallback(PacketType.AttachedSoundGainChange, this);
        _Client.Network.RegisterCallback(PacketType.PreloadSound, this);
        _Client.Network.RegisterCallback(PacketType.SoundTrigger, this);
    }

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case AttachedSound:
				HandleAttachedSound(packet, simulator);
				break;
			case AttachedSoundGainChange:
				HandleAttachedSoundGainChange(packet, simulator);
				break;
			case PreloadSound:
				HandlePreloadSound(packet, simulator);
				break;
			case SoundTrigger:
				HandleSoundTrigger(packet, simulator);
				break;
		}
	}

    // #region public methods

    /**
     * Plays a sound in the current region at full volume from avatar position
     *
     * @param soundID UUID of the sound to be played
     * @param position position for the sound to be played at. Normally the avatar
     * @throws Exception 
     */
    public final void PlaySound(UUID soundID) throws Exception
    {
        SendSoundTrigger(soundID, _Client.Network.getCurrentSim().getHandle(), _Client.Self.getSimPosition(), 1.0f);
    }

    /**
     * Plays a sound in the current region at full volume
     *
     * @param soundID UUID of the sound to be played
     * @param position position for the sound to be played at. Normally the avatar
     * @throws Exception 
     */
    public final void SendSoundTrigger(UUID soundID, Vector3 position) throws Exception
    {
        SendSoundTrigger(soundID, _Client.Network.getCurrentSim().getHandle(), position, 1.0f);
    }

    /**
     * Plays a sound in the current region
     *
     * @param soundID UUID of the sound to be played
     * @param position position for the sound to be played at. Normally the avatar
     * @param gain volume of the sound, from 0.0 to 1.0
     * @throws Exception 
     */
    public final void SendSoundTrigger(UUID soundID, Vector3 position, float gain) throws Exception
    {
        SendSoundTrigger(soundID, _Client.Network.getCurrentSim().getHandle(), position, gain);
    }
    
    /**
     * Plays a sound in the specified sim
     *
     * @param soundID UUID of the sound to be played
     * @param sim simulator where to play the sound in
     * @param position position for the sound to be played at. Normally the avatar
     * @param gain volume of the sound, from 0.0 to 1.0
     * @throws Exception 
     */
    public final void SendSoundTrigger(UUID soundID, Simulator sim, Vector3 position, float gain) throws Exception
    {
        SendSoundTrigger(soundID, sim.getHandle(), position, gain);
    }

    /**
     * Play a sound asset
     *
     * @param soundID UUID of the sound to be played
     * @param handle handle id for the sim to be played in
     * @param position position for the sound to be played at. Normally the avatar
     * @param gain volume of the sound, from 0.0 to 1.0
     * @throws Exception 
     */
    public final void SendSoundTrigger(UUID soundID, long handle, Vector3 position, float gain) throws Exception
    {
        SoundTriggerPacket soundtrigger = new SoundTriggerPacket();
        soundtrigger.SoundData = soundtrigger.new SoundDataBlock();
        soundtrigger.SoundData.SoundID = soundID;
        soundtrigger.SoundData.ObjectID = UUID.Zero;
        soundtrigger.SoundData.OwnerID = UUID.Zero;
        soundtrigger.SoundData.ParentID = UUID.Zero;
        soundtrigger.SoundData.Handle = handle;
        soundtrigger.SoundData.Position = position;
        soundtrigger.SoundData.Gain = gain;

        _Client.Network.SendPacket(soundtrigger);
    }
    // #endregion

    // #region Packet Handlers
    /**
     * Process an incoming packet and raise the appropriate events
     */
    private final void HandleAttachedSound(Packet packet, Simulator simulator)
    {
        AttachedSoundPacket sound = (AttachedSoundPacket)packet;

        OnAttachedSound.dispatch(new AttachedSoundCallbackArgs(simulator, sound.DataBlock.SoundID, sound.DataBlock.OwnerID, sound.DataBlock.ObjectID, sound.DataBlock.Gain, sound.DataBlock.Flags));
    }

    /**
     * Process an incoming packet and raise the appropriate events
     */
    private final void HandleAttachedSoundGainChange(Packet packet, Simulator simulator)
    {
        AttachedSoundGainChangePacket change = (AttachedSoundGainChangePacket)packet;
        OnAttachedSoundGainChange.dispatch(new AttachedSoundGainChangeCallbackArgs(simulator, change.DataBlock.ObjectID, change.DataBlock.Gain));
    }

    /**
     * Process an incoming packet and raise the appropriate events
     */
    private final void HandlePreloadSound(Packet packet, Simulator simulator)
    {
        PreloadSoundPacket preload = (PreloadSoundPacket)packet;

        for (PreloadSoundPacket.DataBlockBlock data : preload.DataBlock)
        {
            OnPreloadSound.dispatch(new PreloadSoundCallbackArgs(simulator, data.SoundID, data.OwnerID, data.ObjectID));
        }
    }

    /**
     * Process an incoming packet and raise the appropriate events
     */
    private final void HandleSoundTrigger(Packet packet, Simulator simulator)
    {
        SoundTriggerPacket trigger = (SoundTriggerPacket)packet;
        OnSoundTrigger.dispatch(new SoundTriggerCallbackArgs(simulator, trigger.SoundData.SoundID, trigger.SoundData.OwnerID, trigger.SoundData.ObjectID, trigger.SoundData.ParentID, trigger.SoundData.Gain, trigger.SoundData.Handle, trigger.SoundData.Position));
    }
    // #endregion
}

