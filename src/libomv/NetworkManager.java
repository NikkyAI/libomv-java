/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
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

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import libomv.LoginManager.LoginResponseCallbackArgs;
import libomv.Simulator.RegionFlags;
import libomv.Simulator.SimAccess;
import libomv.capabilities.CapsCallback;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.IMessage;
import libomv.packets.CompletePingCheckPacket;
import libomv.packets.EnableSimulatorPacket;
import libomv.packets.KickUserPacket;
import libomv.packets.LogoutReplyPacket;
import libomv.packets.LogoutRequestPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.RegionHandshakePacket;
import libomv.packets.RegionHandshakeReplyPacket;
import libomv.packets.SimStatsPacket;
import libomv.packets.StartPingCheckPacket;
import libomv.types.UUID;
import libomv.types.PacketCallback;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.CallbackHandlerQueue;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

// NetworkManager is responsible for managing the network layer of
// libsecondlife. It tracks all the server connections, serializes
// outgoing traffic and deserializes incoming traffic, and provides
// instances of delegates for network-related events.

public class NetworkManager implements PacketCallback {
    /** Explains why a simulator or the grid disconnected from us */
    public enum DisconnectType
    {
        /** The client requested the logout or simulator disconnect  */
        ClientInitiated,
        /** The server notified us that it is disconnecting */
        ServerInitiated,
        /** Either a socket was closed or network traffic timed out */
        NetworkTimeout,
        /** The last active simulator shut down */
        SimShutdown
    }

    /** Holds a simulator reference and a decoded packet, these structs are put in
    the packet inbox for event handling */
    public final class IncomingPacket
    {
        /** Reference to the simulator that this packet came from */
        public Simulator Simulator;
        /** Packet that needs to be processed */
        public Packet Packet;

        public IncomingPacket(Simulator simulator, Packet packet)
        {
            Simulator = simulator;
            Packet = packet;
        }
    }

    /** Holds a simulator reference and a serialized packet, these structs are put in
        the packet outbox for sending */
    public class OutgoingPacket
    {
        /** Reference to the simulator this packet is destined for */
        public final Simulator Simulator;
        /** Packet that needs to be sent */
        public final ByteBuffer Buffer;
        public int bytes;
        /** Sequence number of the wrapped packet */
        public int SequenceNumber;
        /** Number of times this packet has been resent */
        public AtomicInteger ResendCount;
        /** Environment.TickCount when this packet was last sent over the wire */
        public long TickCount;

        public OutgoingPacket(Simulator simulator, ByteBuffer buffer)
        {
            Simulator = simulator;
            Buffer = buffer;
        }
    }
    
	// The simulator that the logged in avatar is currently occupying
	private Simulator CurrentSim;
	
	public Simulator getCurrentSim()
	{
		return CurrentSim;
	}
    public final void setCurrentSim(Simulator value)
    {
        CurrentSim = value;
    }

    /** Number of packets in the incoming queue */
    public final int getInboxCount()
    {
        return PacketInbox.size();
    }

    /** Number of packets in the outgoing queue */
    public final int getOutboxCount()
    {
        return PacketOutbox.size();
    }

    /**
     * A list of packets obtained during the login process which NetworkManager will log but not process
     */
    private final ArrayList<PacketType> UDPBlacklist = new ArrayList<PacketType>();

	// Shows whether the network layer is logged in to the grid or not
	public boolean getConnected()
	{
		return connected;
	}
	
	/** Callback arguments classes */
	public class SimConnectingCallbackArgs extends CallbackArgs
	{
		private InetSocketAddress endPoint;
		private boolean cancel = false;
		
		public InetSocketAddress getEndPoint()
		{
			return endPoint;
		}

		public void setCancel(boolean cancel)
		{
		    this.cancel = cancel;	
		}
		
		public boolean getCancel()
		{
			return cancel;
		}
		
		public SimConnectingCallbackArgs(InetSocketAddress endPoint)
		{
			this.endPoint = endPoint;
		}
	}

	public CallbackHandlerQueue<SimConnectingCallbackArgs> OnSimConnecting = new CallbackHandlerQueue<SimConnectingCallbackArgs>();


	public class SimConnectedCallbackArgs extends CallbackArgs
	{
		private final Simulator simulator;
		
		public Simulator getSimulator()
		{
			return simulator;
		}
		
		public SimConnectedCallbackArgs(Simulator simulator)
		{
			this.simulator = simulator;
		}
	}

	public CallbackHandlerQueue<SimConnectedCallbackArgs> OnSimConnected = new CallbackHandlerQueue<SimConnectedCallbackArgs>();

    /**
     * Fire an event when an event queue connects for capabilities
     * 
     * @param simulator Simulator the event queue is attached to
     */
    public void RaiseSimConnectedEvent(Simulator simulator)
	{
		OnSimConnected.dispatch(new SimConnectedCallbackArgs(simulator));
	}

	public CallbackHandlerQueue<SimChangedCallbackArgs> OnSimChanged = new CallbackHandlerQueue<SimChangedCallbackArgs>();

	
	// An event for the connection to a simulator other than the currently occupied one disconnecting
	public class SimDisconnectedCallbackArgs extends CallbackArgs
	{
		private final Simulator simulator;
		private final DisconnectType type;
		
		public Simulator getSimulator()
		{
			return simulator;
		}
		
		public DisconnectType getDisconnectType()
		{
			return type;
		}

		public SimDisconnectedCallbackArgs(Simulator simulator, DisconnectType type)
	    {
			this.simulator = simulator;
	    	this.type = type;
	    }
	}

	public CallbackHandlerQueue<SimDisconnectedCallbackArgs> OnSimDisconnected = new CallbackHandlerQueue<SimDisconnectedCallbackArgs>();
	

	// An event for being logged out either through client request, server forced, or network error
	public class DisconnectedCallbackArgs  extends CallbackArgs
	{
		private final DisconnectType type;
		private final String message;
		
		public DisconnectType getDisconnectType()
		{
			return type;	
		}
		
		public String getMessage()
		{
			return message;
		}
		
		public DisconnectedCallbackArgs(DisconnectType type, String message)
		{
		    this.type = type;
		    this.message = message;
		}
	}

	public CallbackHandlerQueue<DisconnectedCallbackArgs> OnDisconnected = new CallbackHandlerQueue<DisconnectedCallbackArgs>();


    public class PacketSentCallbackArgs extends CallbackArgs
    {
        private final byte[] m_Data;
        private final int m_SentBytes;
        private final Simulator m_Simulator;

        public final byte[] getData()
        {
            return m_Data;
        }
        public final int getSentBytes()
        {
            return m_SentBytes;
        }
        public final Simulator getSimulator()
        {
            return m_Simulator;
        }

        public PacketSentCallbackArgs(byte[] data, int bytesSent, Simulator simulator)
        {
            this.m_Data = data;
            this.m_SentBytes = bytesSent;
            this.m_Simulator = simulator;
        }
    }

    public CallbackHandlerQueue<PacketSentCallbackArgs> OnPacketSent = new CallbackHandlerQueue<PacketSentCallbackArgs>();

    public void RaisePacketSentCallback(byte[] data, int bytes, Simulator sim)
    {
    	_Client.Network.OnPacketSent.dispatch(new NetworkManager.PacketSentCallbackArgs(data, bytes, sim));
    }

    
    public class EventQueueRunningCallbackArgs extends CallbackArgs
    {
        private final Simulator m_Simulator;

        public final Simulator getSimulator()
        {
            return m_Simulator;
        }

        public EventQueueRunningCallbackArgs(Simulator simulator)
        {
            this.m_Simulator = simulator;
        }
    }

    public CallbackHandlerQueue<EventQueueRunningCallbackArgs> OnEventQueueRunning = new CallbackHandlerQueue<EventQueueRunningCallbackArgs>();
	
    public final void RaiseConnectedEvent(Simulator simulator)
    {
        OnEventQueueRunning.dispatch(new EventQueueRunningCallbackArgs(simulator));
    }

    
	// An event triggered when the logout is confirmed
	public class LoggedOutCallbackArgs  extends CallbackArgs
	{
		private final Vector<UUID> itemIDs;
	
		public Vector<UUID> getItemIDs()
		{
			return itemIDs;
		}
		
		public LoggedOutCallbackArgs(Vector<UUID> itemIDs)
		{
			this.itemIDs = itemIDs;
		}
	}

	public class SimChangedCallbackArgs extends CallbackArgs
	{
		private final Simulator simulator;
		
		public Simulator getSimulator()
		{
			return simulator;
		}
		
		public SimChangedCallbackArgs(Simulator simulator)
		{
			this.simulator = simulator;
		}
	}

	public CallbackHandlerQueue<LoggedOutCallbackArgs> OnLoggedOut = new CallbackHandlerQueue<LoggedOutCallbackArgs>();
     
	private HashMap<PacketType, ArrayList<PacketCallback>> simCallbacks;
	private HashMap<CapsEventType, ArrayList<CapsCallback>> capCallbacks;

	private GridClient _Client;

	// The ID number associated with this particular connection to the simulator, used to emulate
	// TCP connections. This is used internally for packets that have a CircuitCode field.
    private int _CircuitCode;

	public int getCircuitCode() {
		return _CircuitCode;
	}
	public void setCircuitCode(int code) {
		_CircuitCode = code;
	}

	private ArrayList<Simulator> Simulators;

	/**
	 * Get the array with all currently known simulators. This list must be protected with a synchronization
	 * lock on itself if you do anything with it.
	 * 
	 * @return array of simulator objects known to this client
	 */
	public ArrayList<Simulator> getSimulators()
	{
		return Simulators;
	}

    private class OutgoingPacketHandler extends Thread
    {
    	@Override
    	public void run()
        {
            long lastTime = System.currentTimeMillis();
            OutgoingPacket outgoingPacket = null;

            while (connected)
            {
            	try
				{
					outgoingPacket = PacketOutbox.poll(100, TimeUnit.MILLISECONDS);
	                if (outgoingPacket != null)
	                {
	                    // Very primitive rate limiting, keeps a fixed buffer of time between each packet
	                	long newTime = System.currentTimeMillis();
	                	long remains = 10 + lastTime - newTime;
	                	lastTime = newTime;
	                	
	                    if (remains > 0)
	                    {
	                        // Logger.DebugLog(String.format("Rate limiting, last packet was %d ms ago", remains));
	                        Thread.sleep(remains);
	                    }
	                    outgoingPacket.Simulator.SendPacketFinal(outgoingPacket);
					}
				}
				catch (InterruptedException ex) { }
            }
        }
    }

    private class IncomingPacketHandler extends Thread
    {
    	@Override
    	public void run()
        {
            IncomingPacket incomingPacket;
            Packet packet = null;
            Simulator simulator = null;

            while (connected)
            {
                try
				{
					incomingPacket = PacketInbox.poll(100, TimeUnit.MILLISECONDS);
	                if (incomingPacket != null)
	                {
	                    packet = incomingPacket.Packet;
	                    simulator = incomingPacket.Simulator;
	                    if (packet != null)
	                    {
	    	            	synchronized (UDPBlacklist)
	    	            	{
	    	            		// skip blacklisted packets
	    	            		if (UDPBlacklist.contains(packet.getType()))
	    	            		{
	    	            			Logger.Log(String.format("Discarding Blacklisted packet %s from %s", packet.getType(), simulator.getIPEndPoint()), LogLevel.Warning);
	    	            			return;
	    	            		}
	                        }
	    					// Let the network manager distribute the packets to the callbacks
	                        DistributePacket(simulator, packet);
	                    }
	                }
				}
				catch (InterruptedException e) {}
            }
        }
    }
	
    /** Incoming packets that are awaiting handling */
    public BlockingQueue<IncomingPacket> PacketInbox = new LinkedBlockingQueue<IncomingPacket>(Settings.PACKET_INBOX_SIZE);
    /** Outgoing packets that are awaiting handling */
    public BlockingQueue<OutgoingPacket> PacketOutbox = new LinkedBlockingQueue<OutgoingPacket>(Settings.PACKET_INBOX_SIZE);

	private Timer DisconnectTimer;

	private boolean connected;

	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
		case RegionHandshake:
			RegionHandshakeHandler(packet, simulator);
			break;
		case StartPingCheck:
			StartPingCheckHandler(packet, simulator);
			break;
	    case CompletePingCheck:
			CompletePingCheckHandler(packet, simulator);
			break;
		case EnableSimulator:
			EnableSimulatorHandler(packet, simulator);
			break;
		case DisableSimulator:
			DisableSimulatorHandler(packet, simulator);
			break;
		case LogoutReply:
			LogoutReplyHandler(packet, simulator);
			break;
		case SimStats:
			SimStatsHandler(packet, simulator);
			break;
		case KickUser:
			KickUserHandler(packet, simulator);
			break;
		}
	}

	// 
	// <param name="client"></param>
	public NetworkManager(GridClient client) throws Exception
	{
		_Client = client;
		Simulators = new ArrayList<Simulator>();
		simCallbacks = new HashMap<PacketType, ArrayList<PacketCallback>>();
		capCallbacks = new HashMap<CapsEventType, ArrayList<CapsCallback>>();
		CurrentSim = null;
		
		_Client.Login.RegisterLoginResponseCallback(new Network_OnLogin(), null, false);
		
		// Register the internal callbacks
		RegisterCallback(PacketType.RegionHandshake, this);
		RegisterCallback(PacketType.StartPingCheck, this);
        RegisterCallback(PacketType.DisableSimulator, this);
		RegisterCallback(PacketType.EnableSimulator, this);
		RegisterCallback(PacketType.KickUser, this);
        RegisterCallback(PacketType.LogoutReply, this);
        RegisterCallback(PacketType.CompletePingCheck, this);
        RegisterCallback(PacketType.SimStats, this);

		// Disconnect a sim if no network traffic has been received for 15 seconds
		DisconnectTimer = new Timer();
		DisconnectTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				try {
					DisconnectTimer_Elapsed();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 60000, 60000);
	}

	private class Network_OnLogin extends CallbackHandler<LoginResponseCallbackArgs>
	{
		@Override
		public void callback(LoginResponseCallbackArgs e)
		{
			if (e.getSuccess())
			{
	            // Add any blacklisted UDP packets to the blacklist for exclusion from packet processing
				String blacklist = e.getReply().UDPBlacklist;
	            if (blacklist != null)
	            {
	            	synchronized (UDPBlacklist)
	            	{
	            		for (String s : blacklist.split(","))
	            			UDPBlacklist.add(PacketType.valueOf(s));
	            	}
	            }
			}
		}
	}
	
	
	public URI getCapabilityURI(String capability)
	{
		if (CurrentSim != null)
		{
			return CurrentSim.getCapabilityURI(capability);
		}
		return null;
	}

	public URI getCapabilityURI(String capability, Simulator simulator)
	{
		if (simulator == null)
			simulator = CurrentSim;

		if (simulator != null)
		{
			return simulator.getCapabilityURI(capability);
		}
		return null;
	}

	public boolean getIsEventQueueRunning()
	{
		return (CurrentSim != null && CurrentSim.getIsEventQueueRunning());
	}
	
	public void RegisterCallback(CapsEventType capability, CapsCallback callback)
	{
		/* Don't accept null callbacks */
		if (callback == null)
			return;

		if (!capCallbacks.containsKey(capability))
		{
			capCallbacks.put(capability, new ArrayList<CapsCallback>());
		}
		capCallbacks.get(capability).add(callback);
    }
	
	public void UnregisterCallback(CapsEventType capability, CapsCallback callback)
	{
		if (!capCallbacks.containsKey(capability))
		{
			Logger.Log("Trying to unregister a callback for capability " + capability
					 + " when no callbacks are setup for that capability",
					 LogLevel.Info);
			return;
		}

		ArrayList<CapsCallback> callbackArray = capCallbacks.get(capability);

		if (callbackArray.contains(callback))
		{
			callbackArray.remove(callback);
			if (callbackArray.isEmpty())
			{
			    capCallbacks.remove(capability);
			}
		}
		else
		{
			Logger.Log("Trying to unregister a non-existant callback for capability " + capability,
					   LogLevel.Info);
		}
	}

	public void RegisterCallback(PacketType type, PacketCallback callback)
	{
		/* Don't accept null callbacks */
		if (callback == null)
			return;
		
		if (!simCallbacks.containsKey(type))
		{
			simCallbacks.put(type, new ArrayList<PacketCallback>());
		}
		simCallbacks.get(type).add(callback);
	}

	public void UnregisterCallback(PacketType type, PacketCallback callback)
	{
		if (!simCallbacks.containsKey(type))
		{
			Logger.Log("Trying to unregister a callback for packet " + type
					+ " when no callbacks are setup for that packet",
					LogLevel.Info);
			return;
		}

		ArrayList<PacketCallback> callbackArray = simCallbacks.get(type);

		if (callbackArray.contains(callback))
		{
			callbackArray.remove(callback);
			if (callbackArray.isEmpty())
			{
			    simCallbacks.remove(type);
			}
		}
		else
		{
			Logger.Log("Trying to unregister a non-existant callback for packet " + type, LogLevel.Info);
		}
	}

	/**
	 * Send an UDP packet to the current simulator
	 * 
	 * @param packet The packet to send
	 * @throws Exception
	 */
	public void SendPacket(Packet packet) throws Exception
	{
        // try CurrentSim, however directly after login this will be null, so if it is, we'll try to
		// find the first simulator we're connected to in order to send the packet.
        Simulator simulator = CurrentSim;

        if (simulator == null && Simulators.size() >= 1)
        {
            Logger.DebugLog("CurrentSim object was null, using first found connected simulator", _Client);
            simulator = _Client.Network.Simulators.get(0);
        }

        if (simulator != null && simulator.getConnected())
        {
            simulator.SendPacket(packet);
        }
        else
        {
            Logger.Log("Packet received before simulator packet processing threads running, make certain you are completely logged in.", LogLevel.Error);
            throw new ConnectException("Packet received before simulator packet processing threads running, make certain you are completely logged in");
        }
	}

	public void DistributePacket(Simulator simulator, Packet packet)
	{
		// Fire the registered packet events
	    try
	    {
	        if (_Client.Settings.SYNC_PACKETCALLBACKS)
	        {
		    	ArrayList<PacketCallback> callbackArray = simCallbacks.get(packet.getType());
	     	    // Fire any registered callbacks
			    for (PacketCallback callback : callbackArray)
			    {
				    callback.packetCallback(packet, simulator);
			    }

	            callbackArray = (ArrayList<PacketCallback>) simCallbacks.get(PacketType.Default);
				// Fire any registered callbacks
			    for (PacketCallback callback : callbackArray)
			    {
					callback.packetCallback(packet, simulator);
			    }
		    }
	        else
	        {
	        }
	    }
	    catch (Exception e)
	    {
		    e.printStackTrace();
		    Logger.Log("Caught an exception in a packet callback: " + e.toString(), LogLevel.Warning);
	    }
	}

	public void DistributeCaps(IMessage message, Simulator simulator)
	{
	    try
	    {
	    	if (simulator == null)
	    		simulator = CurrentSim;

			// Fire the registered capability callbacks
	    	ArrayList<CapsCallback> callbackArray = capCallbacks.get(message.getType());
			// Fire any registered callbacks
			for (CapsCallback callback : callbackArray)
			{
			    callback.capsCallback(message, simulator);
			}

			// Fire any default capability callbacks
		    callbackArray = capCallbacks.get(CapsEventType.Default);
			// Fire any registered callbacks
			for (CapsCallback callback : callbackArray)
			{
			    callback.capsCallback(message, simulator);
			}
	    }
	    catch (Exception e)
	    {
		    e.printStackTrace();
		    Logger.Log("Caught an exception in a packet callback: "
				+ e.toString(), LogLevel.Warning, e);
	    }
	}

	public Simulator Connect(InetAddress ip, short port, long handle, boolean setDefault, String seedcaps) throws Exception
	{
	    return Connect(new InetSocketAddress(ip, port), handle, setDefault, seedcaps);
	}
    /** Connect to a simulator
     * 
     *  @param endPoint IP address and port to connect to
     *  @param handle Handle for this simulator, to identify its location in the grid
     *  @param setDefault Whether to set CurrentSim to this new
     *                    connection, use this if the avatar is moving in to this simulator
     *  @param seedcaps URL of the capabilities server to use for this sim connection
     *  @return A Simulator object on success, otherwise null
     *  */
	public Simulator Connect(InetSocketAddress endPoint, long handle, boolean setDefault, String seedcaps) throws Exception
	{
        Simulator simulator = FindSimulator(endPoint);

        if (simulator == null)
        {
            // We're not tracking this sim, create a new Simulator object
		    simulator = new Simulator(_Client, endPoint, handle);

		    synchronized (Simulators)
		    {
		    	// Immediately add this simulator to the list of current sims. It will be removed if the connection fails
		    	Simulators.add(simulator);
		    }
        }

		if (!simulator.getConnected())
		{
            if (!connected)
            {
                // Mark that we are connecting/connected to the grid
                connected = true;

                // Start the packet decoding thread
                Thread decodeThread = new Thread(new IncomingPacketHandler());
                decodeThread.setName("Incoming UDP packet dispatcher");
                decodeThread.start();

                // Start the packet sending thread
                Thread sendThread = new Thread(new OutgoingPacketHandler());
                sendThread.setName("Outgoing UDP packet dispatcher");
                sendThread.start();
            }
            
            if (OnSimConnecting.count() > 0)
            {
            	SimConnectingCallbackArgs args = new SimConnectingCallbackArgs(endPoint);
            	OnSimConnecting.dispatch(args);
            	if (args.getCancel())
            	{
                    synchronized (Simulators)
                    {
            	        // Callback is requesting that we abort this connection
                        Simulators.remove(simulator);
                    }
                    return null;
            	}
		    }
            
            // Attempt to establish a connection to the simulator
            if (simulator.Connect(setDefault))
            {
                if (DisconnectTimer == null)
                {
                    // Start a timer that checks if we've been disconnected
                    DisconnectTimer = new Timer();
                    DisconnectTimer.scheduleAtFixedRate(new TimerTask()
            		{
            			public void run() {
            				try {
            					DisconnectTimer_Elapsed();
            				} catch (Exception e) {
            					e.printStackTrace();
            				}
            			}
            		}, _Client.Settings.SIMULATOR_TIMEOUT, _Client.Settings.SIMULATOR_TIMEOUT);
                }

                if (setDefault)
                {
                    SetCurrentSim(simulator, seedcaps);
                }

                // Raise the SimConnected event
                OnSimConnected.dispatch( new SimConnectedCallbackArgs(simulator));

                // If enabled, send an AgentThrottle packet to the server to increase our bandwidth
                if (_Client.Throttle != null)
                {
                    _Client.Throttle.Set(simulator);
                }
            }
            else
            {
                synchronized (Simulators)
                {
                    // Connection failed, remove this simulator from our list and destroy it
                    Simulators.remove(simulator);
                }
                return null;
            }
		}
        else if (setDefault)
        {
            // Move in to this simulator
            simulator.handshakeComplete = false;
            simulator.UseCircuitCode();
            _Client.Self.CompleteAgentMovement(simulator);

            // We're already connected to this server, but need to set it to the default
            SetCurrentSim(simulator, seedcaps);

            // Send an initial AgentUpdate to complete our movement in to the sim
            if (_Client.Settings.SEND_AGENT_UPDATES)
            {
            	/*  TODO: implement AgentManager.Movement class
                _Client.Self.Movement.SendUpdate(true, simulator); */
            }
        }
        else
        {
            // Already connected to this simulator and wasn't asked to set it as the default,
            // just return a reference to the existing object
        }
        return simulator;
 	}

	public void Logout() throws Exception {
		// This will catch a Logout when the client is not logged in
		if (CurrentSim == null || !connected) {
			return;
		}

		Logger.Log("Logging out", LogLevel.Info);

		DisconnectTimer.cancel();
		connected = false;

		// Send a logout request to the current sim
		LogoutRequestPacket logout = new LogoutRequestPacket();
		logout.AgentData.AgentID = _Client.Self.getAgentID();
		logout.AgentData.SessionID = _Client.Self.getSessionID();

		CurrentSim.SendPacket(logout);

		// TODO: We should probably check if the server actually received the
		// logout request

		// Shutdown the network layer
		Shutdown(DisconnectType.ClientInitiated, "");
	}

    private void SetCurrentSim(Simulator simulator, String seedcaps)
    {
        if (simulator != getCurrentSim())
        {
            Simulator oldSim = getCurrentSim();
            synchronized (Simulators) // CurrentSim is synchronized against Simulators
            {
                setCurrentSim(simulator);
            }
            simulator.SetSeedCaps(seedcaps);

            // If the current simulator changed fire the callback
            if (simulator != oldSim)
            {
                OnSimChanged.dispatch(new SimChangedCallbackArgs(oldSim));
            }
        }
    }

	public void DisconnectSim(Simulator simulator, boolean sendCloseCircuit) throws Exception
	{
        if (simulator != null)
        {
        	simulator.Disconnect(sendCloseCircuit);

		    // Fire the SimDisconnected event if a handler is registered
		    OnSimDisconnected.dispatch(new SimDisconnectedCallbackArgs(simulator, DisconnectType.NetworkTimeout));

            synchronized (Simulators)
            {
            	Simulators.remove(simulator);

		        if (Simulators.isEmpty())
                {
                    Shutdown(DisconnectType.SimShutdown);
                }
            }
        }
        else
        {
        	Logger.Log("DisconnectSim() called with a null Simulator reference", LogLevel.Warning);        
        }
	}

    /** Shutdown will disconnect all the sims except for the current sim
     *  first, and then kill the connection to CurrentSim. This should only
     *  be called if the logout process times out on <code>RequestLogout</code>
     *
     *  @param type Type of shutdown 
     *  @throws Exception
     */
    public final void Shutdown(DisconnectType type) throws Exception
    {
        Shutdown(type, type.toString());
    }

	private void Shutdown(DisconnectType type, String message) throws Exception
	{
        Logger.Log("NetworkManager shutdown initiated", LogLevel.Info, _Client);

        // Send a CloseCircuit packet to simulators if we are initiating the disconnect
        boolean sendCloseCircuit = (type == DisconnectType.ClientInitiated || type == DisconnectType.NetworkTimeout);

        synchronized (Simulators)
        {
			// Disconnect all simulators except the current one
			for (int i = 0; i < Simulators.size(); i++)
			{
				Simulator simulator = Simulators.get(i);
				// Don't disconnect the current sim, we'll use LogoutRequest for
				// that
				if (simulator != null && simulator != CurrentSim)
				{
					simulator.Disconnect(sendCloseCircuit);

					// Fire the SimDisconnected event if a handler is registered
					OnSimDisconnected.dispatch(new SimDisconnectedCallbackArgs(simulator, DisconnectType.NetworkTimeout));
				}

			}
			Simulators.clear();

			if (CurrentSim != null)
			{
				CurrentSim.Disconnect(sendCloseCircuit);

				// Fire the SimDisconnected event if a handler is registered
				OnSimDisconnected.dispatch(new SimDisconnectedCallbackArgs(CurrentSim, DisconnectType.NetworkTimeout));
			}
		}
        connected = false;
        if (OnDisconnected != null) {
            OnDisconnected.dispatch(new DisconnectedCallbackArgs(type, message));
	    }
	}


	private void DisconnectTimer_Elapsed() throws Exception {
		// If the current simulator is disconnected, shutdown + callback + return
        if (!connected || CurrentSim == null)
        {
            if (DisconnectTimer != null)
            {
			    DisconnectTimer.cancel();
			    DisconnectTimer = null;
            }
            connected = false;
        }
        else if (CurrentSim.getDisconnectCandidate())
        {
            // The currently occupied simulator hasn't sent us any traffic in a while, shutdown
        	Logger.Log("Network timeout for the current simulator ("
					  + CurrentSim.Name + "), logging out", LogLevel.Warning);

            if (DisconnectTimer != null)
            {
			    DisconnectTimer.cancel();
			    DisconnectTimer = null;
            }
            connected = false;

			// Shutdown the network layer
			Shutdown(DisconnectType.NetworkTimeout);

			// We're completely logged out and shut down, leave this function
			return;
		}

		ArrayList<Simulator> disconnectedSims = null;

		// Check all of the connected sims for disconnects
		synchronized (Simulators)
		{
			for (Simulator simulator : Simulators)
			{
				if (simulator.getDisconnectCandidate())
				{
					if (disconnectedSims == null)
					{
						disconnectedSims = new ArrayList<Simulator>();
					}
					disconnectedSims.add(simulator);
				} 
				else
				{
					simulator.setDisconnectCandidate(true);
				}
			}
		}

		// Actually disconnect each sim we detected as disconnected
		if (disconnectedSims != null)
		{
			for (Simulator simulator : disconnectedSims)
			{
				// This sim hasn't received any network traffic since the
				// timer last elapsed, consider it disconnected
				Logger.Log("Network timeout for simulator " + simulator.Name + ", disconnecting", LogLevel.Warning);

				DisconnectSim(simulator, false);
			}
		}
	}

    /** Searches through the list of currently connected simulators to find
     *  one attached to the given IPEndPoint
     *  
     *  @param endPoint InetSocketAddress of the Simulator to search for
     *  @return A Simulator reference on success, otherwise null
     */
    private final Simulator FindSimulator(InetSocketAddress endPoint)
    {
        synchronized (Simulators)
        {
            for (Simulator simulator : Simulators) 
            {
                if (simulator.getIPEndPoint().equals(endPoint))
                {
                    return simulator;
                }
            }
        }
        return null;
    }

	private void RegionHandshakeHandler(Packet packet, Simulator simulator) throws Exception
	{
		RegionHandshakePacket handshake = (RegionHandshakePacket)packet;

        simulator.ID = handshake.RegionInfo.CacheID;

		simulator.IsEstateManager = handshake.RegionInfo.IsEstateManager;
		simulator.Name = Helpers.BytesToString(handshake.RegionInfo.getSimName());
		simulator.SimOwner = handshake.RegionInfo.SimOwner;
		simulator.TerrainBase0 = handshake.RegionInfo.TerrainBase0;
		simulator.TerrainBase1 = handshake.RegionInfo.TerrainBase1;
		simulator.TerrainBase2 = handshake.RegionInfo.TerrainBase2;
		simulator.TerrainBase3 = handshake.RegionInfo.TerrainBase3;
		simulator.TerrainDetail0 = handshake.RegionInfo.TerrainDetail0;
		simulator.TerrainDetail1 = handshake.RegionInfo.TerrainDetail1;
		simulator.TerrainDetail2 = handshake.RegionInfo.TerrainDetail2;
		simulator.TerrainDetail3 = handshake.RegionInfo.TerrainDetail3;
		simulator.TerrainHeightRange00 = handshake.RegionInfo.TerrainHeightRange00;
		simulator.TerrainHeightRange01 = handshake.RegionInfo.TerrainHeightRange01;
		simulator.TerrainHeightRange10 = handshake.RegionInfo.TerrainHeightRange10;
		simulator.TerrainHeightRange11 = handshake.RegionInfo.TerrainHeightRange11;
		simulator.TerrainStartHeight00 = handshake.RegionInfo.TerrainStartHeight00;
		simulator.TerrainStartHeight01 = handshake.RegionInfo.TerrainStartHeight01;
		simulator.TerrainStartHeight10 = handshake.RegionInfo.TerrainStartHeight10;
		simulator.TerrainStartHeight11 = handshake.RegionInfo.TerrainStartHeight11;
        
        simulator.WaterHeight = handshake.RegionInfo.WaterHeight;
        simulator.Flags = RegionFlags.setValue(handshake.RegionInfo.RegionFlags);
        simulator.BillableFactor = handshake.RegionInfo.BillableFactor;
        simulator.Access = SimAccess.setValue(handshake.RegionInfo.SimAccess);

        simulator.RegionID = handshake.RegionInfo2.RegionID;
        simulator.ColoLocation = Helpers.BytesToString(handshake.RegionInfo3.getColoName());
        simulator.CPUClass = handshake.RegionInfo3.CPUClassID;
        simulator.CPURatio = handshake.RegionInfo3.CPURatio;
        simulator.ProductName = Helpers.BytesToString(handshake.RegionInfo3.getProductName());
        simulator.ProductSku = Helpers.BytesToString(handshake.RegionInfo3.getProductSKU());

		// Send a RegionHandshakeReply
		RegionHandshakeReplyPacket reply = new RegionHandshakeReplyPacket();
		reply.AgentData.AgentID = _Client.Self.getAgentID();
		reply.AgentData.SessionID = _Client.Self.getSessionID();
		reply.RegionInfo.Flags = 0;
		simulator.SendPacket(reply);

		Logger.Log("Received a region handshake for " + simulator.Name, LogLevel.Info);
	}

    /** Process an incoming packet and raise the appropriate events
     * 
     *  @param packet The packet data
     *  @param simulator The sender
     *  @throws Exception 
     */
    private void StartPingCheckHandler(Packet packet, Simulator simulator) throws Exception
    {
        StartPingCheckPacket incomingPing = (StartPingCheckPacket)packet;
        CompletePingCheckPacket ping = new CompletePingCheckPacket();
        ping.PingID.PingID = incomingPing.PingID.PingID;
        ping.getHeader().setReliable(false);
        // TODO: We can use OldestUnacked to correct transmission errors
        //   I don't think that's right.  As far as I can tell, the Viewer
        //   only uses this to prune its duplicate-checking buffer. -bushing
        simulator.SendPacket(ping);
    }

    /** Process an incoming packet and raise the appropriate events
     * 
     *  @param packet The packet data
     *  @param simulator The sender
     */
    protected final void CompletePingCheckHandler(Packet packet, Simulator simulator)
    {
        CompletePingCheckPacket pong = (CompletePingCheckPacket)packet;
        long timeMilli = System.currentTimeMillis();
        String retval = "Pong2: " + (timeMilli - simulator.Stats.LastPingSent);
        if ((pong.PingID.PingID - simulator.Stats.LastPingID + 1) != 0)
        {
            retval += " (gap of " + (pong.PingID.PingID - simulator.Stats.LastPingID + 1) + ")";
        }

        simulator.Stats.LastLag = timeMilli - simulator.Stats.LastPingSent;
        simulator.Stats.ReceivedPongs.incrementAndGet();
        Logger.Log(retval, LogLevel.Info);
    }

    /** Process an incoming packet and raise the appropriate events
     * 
     *  @param packet The packet data
     *  @param simulator The sender
     */
    protected final void SimStatsHandler(Packet packet, Simulator simulator)
    {
        if (!_Client.Settings.ENABLE_SIMSTATS)
        {
            return;
        }
        SimStatsPacket stats = (SimStatsPacket)packet;
        for (int i = 0; i < stats.Stat.length; i++)
        {
            SimStatsPacket.StatBlock s = stats.Stat[i];
            switch (s.StatID)
            {
                case 0:
                    simulator.Stats.Dilation = s.StatValue;
                    break;
                case 1:
                    simulator.Stats.FPS = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 2:
                    simulator.Stats.PhysicsFPS = s.StatValue;
                    break;
                case 3:
                    simulator.Stats.AgentUpdates = s.StatValue;
                    break;
                case 4:
                    simulator.Stats.FrameTime = s.StatValue;
                    break;
                case 5:
                    simulator.Stats.NetTime = s.StatValue;
                    break;
                case 6:
                    simulator.Stats.OtherTime = s.StatValue;
                    break;
                case 7:
                    simulator.Stats.PhysicsTime = s.StatValue;
                    break;
                case 8:
                    simulator.Stats.AgentTime = s.StatValue;
                    break;
                case 9:
                    simulator.Stats.ImageTime = s.StatValue;
                    break;
                case 10:
                    simulator.Stats.ScriptTime = s.StatValue;
                    break;
                case 11:
                    simulator.Stats.Objects = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 12:
                    simulator.Stats.ScriptedObjects = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 13:
                    simulator.Stats.Agents = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 14:
                    simulator.Stats.ChildAgents = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 15:
                    simulator.Stats.ActiveScripts = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 16:
                    simulator.Stats.LSLIPS = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 17:
                    simulator.Stats.INPPS = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 18:
                    simulator.Stats.OUTPPS = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 19:
                    simulator.Stats.PendingDownloads = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 20:
                    simulator.Stats.PendingUploads = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 21:
                    simulator.Stats.VirtualSize = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 22:
                    simulator.Stats.ResidentSize = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 23:
                    simulator.Stats.PendingLocalUploads = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
                case 24:
                    simulator.Stats.UnackedBytes = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
                    break;
            }
        }
    }

	private void EnableSimulatorHandler(Packet packet, Simulator simulator) throws Exception
	{
        if (!_Client.Settings.MULTIPLE_SIMS)
        {
            return;
        }

        EnableSimulatorPacket msg = (EnableSimulatorPacket)packet;

        InetAddress ip = InetAddress.getByAddress(Helpers.Int32ToBytesB(msg.SimulatorInfo.IP));
        InetSocketAddress endPoint = new InetSocketAddress(ip, msg.SimulatorInfo.Port);

        if (FindSimulator(endPoint) != null)
        {
            return;
        }

        if (Connect(endPoint, msg.SimulatorInfo.Handle, false, null) == null)
        {
        	Logger.Log("Unabled to connect to new sim " + ip + ":" + msg.SimulatorInfo.Port, LogLevel.Error);
        }
	}

    /** Process an incoming packet and raise the appropriate events
     * 
     *  @param simulator The sender
     *  @param packet The packet data
     *  @throws Exception 
     */
    protected final void DisableSimulatorHandler(Packet packet, Simulator simulator) throws Exception
    {
         DisconnectSim(simulator, false);
    }

    /** Process an incoming packet and raise the appropriate events
     * 
     *  @param packet The packet data
     *  @param simulator The sender
     *  @throws Exception 
     */
    private void LogoutReplyHandler(Packet packet, Simulator simulator) throws Exception
    {
        LogoutReplyPacket logout = (LogoutReplyPacket)packet;

        if ((logout.AgentData.SessionID == _Client.Self.getSessionID()) && 
            (logout.AgentData.AgentID == _Client.Self.getAgentID()))
        {
        	Logger.DebugLog("Logout reply received");

            // Deal with callbacks, if any
            if (OnLoggedOut.count() > 0)
            {
                Vector<UUID> itemIDs = new Vector<UUID>();

                for (LogoutReplyPacket.InventoryDataBlock InventoryData : logout.InventoryData)
                {
                    itemIDs.add(InventoryData.ItemID);
                }
                OnLoggedOut.dispatch(new LoggedOutCallbackArgs(itemIDs));
            }

            // If we are receiving a LogoutReply packet assume this is a client initiated shutdown
            Shutdown(DisconnectType.ClientInitiated);
        }
        else
        {
        	Logger.Log("Invalid Session or Agent ID received in Logout Reply... ignoring", LogLevel.Warning);
        }
    }

    /**
     * Process an incoming packet and raise the appropriate events
     * 
     * @param simulator The sender
     * @param packet The packet data
     */
	private void KickUserHandler(Packet packet, Simulator simulator) throws Exception {
		String message = Helpers.BytesToString(((KickUserPacket) packet).UserInfo.getReason());

		// Shutdown the network layer
		Shutdown(DisconnectType.ServerInitiated, message);
	}
}
