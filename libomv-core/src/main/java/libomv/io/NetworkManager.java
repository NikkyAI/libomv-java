/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Portions Copyright (c) 2006, Lateral Arts Limited
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

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.EnableSimulatorMessage;
import libomv.capabilities.IMessage;
import libomv.io.capabilities.AsyncHTTPClient;
import libomv.io.capabilities.CapsCallback;
import libomv.model.Simulator;
import libomv.model.network.DisconnectType;
import libomv.model.network.DisconnectedCallbackArgs;
import libomv.model.network.EventQueueRunningCallbackArgs;
import libomv.model.network.IncomingPacket;
import libomv.model.network.LoggedOutCallbackArgs;
import libomv.model.network.OutgoingPacket;
import libomv.model.network.PacketSentCallbackArgs;
import libomv.model.network.SimChangedCallbackArgs;
import libomv.model.network.SimConnectedCallbackArgs;
import libomv.model.network.SimConnectingCallbackArgs;
import libomv.model.network.SimDisconnectedCallbackArgs;
import libomv.model.simulator.RegionFlags;
import libomv.model.simulator.RegionProtocols;
import libomv.model.simulator.SimAccess;
import libomv.model.simulator.SimStatType;
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
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Settings.SettingsUpdateCallbackArgs;
import libomv.utils.TimeoutEvent;

// NetworkManager is responsible for managing the network layer of
// libsecondlife. It tracks all the server connections, serializes
// outgoing traffic and deserializes incoming traffic, and provides
// instances of delegates for network-related events.

public class NetworkManager implements PacketCallback, CapsCallback {
	private static final Logger logger = Logger.getLogger(NetworkManager.class);

	private class OutgoingPacketHandler implements Runnable {
		@Override
		public void run() {
			long lastTime = System.currentTimeMillis();
			int count = 0;

			while (connected) {
				try {
					OutgoingPacket outgoingPacket = packetOutbox.poll(100, TimeUnit.MILLISECONDS);
					if (outgoingPacket != null) {
						// Very primitive rate limiting, keeps a fixed minimum buffer of time between
						// each packet
						long newTime = System.currentTimeMillis();
						long remains = 10 + lastTime - newTime;
						lastTime = newTime;

						if (remains > 0) {
							// logger.debug(String.format("Rate limiting, last packet was %d ms ago",
							// remains), _Client);
							Thread.sleep(remains);
						}
						outgoingPacket.simulator.sendPacketFinal(outgoingPacket);
						count++;
					} else {
						count += 10;
					}

					if (count > 200) {
						cleanClosableClients();
						count = 0;
					}
				} catch (InterruptedException | IOException ex) {
					logger.debug(GridClient.Log("Call interrupted", client), ex);
				}
			}
		}
	}

	private class PacketCallbackExecutor implements Runnable {
		private final IncomingPacket packet;

		public PacketCallbackExecutor(IncomingPacket packet) {
			this.packet = packet;
		}

		@Override
		public void run() {
			if (packet.packet != null)
				firePacketCallbacks(packet.packet, packet.simulator);
			else
				fireCapsCallbacks(packet.message, (SimulatorManager) packet.simulator);
		}
	}

	private class IncomingPacketHandler implements Runnable {
		ExecutorService threadPool = Executors.newCachedThreadPool();

		public void shutdown() {
			threadPool.shutdown();
		}

		@Override
		public void run() {
			while (connected) {
				try {
					IncomingPacket incomingPacket = packetInbox.poll(100, TimeUnit.MILLISECONDS);
					if (incomingPacket != null) {
						if (incomingPacket.packet != null) {
							// skip blacklisted packets
							if (udpBlacklist.contains(incomingPacket.packet.getType())) {
								logger.warn(GridClient.Log(
										String.format("Discarding Blacklisted packet %s from %s",
												incomingPacket.packet.getType(),
												((SimulatorManager) incomingPacket.simulator).getIPEndPoint()),
										client));
							} else if (syncPacketCallbacks) {
								firePacketCallbacks(incomingPacket.packet, incomingPacket.simulator);
							} else {
								if (!threadPool.isShutdown())
									threadPool.submit(new PacketCallbackExecutor(incomingPacket));
							}
						} else if (incomingPacket.message != null) {
							if (syncPacketCallbacks) {
								fireCapsCallbacks(incomingPacket.message, (SimulatorManager) incomingPacket.simulator);
							} else {
								if (!threadPool.isShutdown())
									threadPool.submit(new PacketCallbackExecutor(incomingPacket));
							}
						}
					}
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private class SettingsUpdate implements Callback<SettingsUpdateCallbackArgs> {
		@Override
		public boolean callback(SettingsUpdateCallbackArgs params) {
			String key = params.getName();
			if (key == null) {
				syncPacketCallbacks = client.settings.getBool(LibSettings.SYNC_PACKETCALLBACKS);
				sendAgentUpdates = client.settings.getBool(LibSettings.SEND_AGENT_UPDATES);
				enableSimStats = client.settings.getBool(LibSettings.ENABLE_SIMSTATS);
			} else if (key.equals(LibSettings.SYNC_PACKETCALLBACKS)) {
				syncPacketCallbacks = params.getValue().asBoolean();
			} else if (key.equals(LibSettings.SEND_AGENT_UPDATES)) {
				sendAgentUpdates = params.getValue().asBoolean();
			} else if (key.equals(LibSettings.ENABLE_SIMSTATS)) {
				enableSimStats = params.getValue().asBoolean();
			}
			return false;
		}
	}

	private class DisconnectTimer_Elapsed extends TimerTask {
		@Override
		public void run() {
			// If the current simulator is disconnected, shutdown + callback + return
			if (!connected || currentSim == null) {
				if (disconnectTimer != null) {
					disconnectTimer.cancel();
					disconnectTimer = null;
				}
				connected = false;
			} else if (currentSim.getDisconnectCandidate()) {
				// The currently occupied simulator hasn't sent us any traffic in a while,
				// shutdown
				logger.warn("Network timeout for the current simulator (" + currentSim.getName() + "), logging out");

				if (disconnectTimer != null) {
					disconnectTimer.cancel();
					disconnectTimer = null;
				}
				connected = false;

				// Shutdown the network layer
				try {
					shutdown(DisconnectType.NetworkTimeout, "DisconnectTimer elapsed");
				} catch (Exception ex) {
				}

				// We're completely logged out and shut down, leave this function
				return;
			}

			List<SimulatorManager> disconnectedSims = null;

			// Check all of the connected sims for disconnects
			synchronized (simulators) {
				for (SimulatorManager simulator : simulators) {
					if (simulator.getDisconnectCandidate()) {
						if (disconnectedSims == null) {
							disconnectedSims = new ArrayList<>();
						}
						disconnectedSims.add(simulator);
					} else {
						simulator.setDisconnectCandidate(true);
					}
				}
			}

			// Actually disconnect each sim we detected as disconnected
			if (disconnectedSims != null) {
				for (SimulatorManager simulator : disconnectedSims) {
					// This sim hasn't received any network traffic since the timer last elapsed,
					// consider it disconnected
					logger.warn("Network timeout for simulator " + simulator.getName() + ", disconnecting");

					try {
						disconnectSim(simulator, false);
					} catch (Exception ex) {
					}
				}
			}
		}
	}

	public CallbackHandler<SimConnectingCallbackArgs> onSimConnecting = new CallbackHandler<>();
	public CallbackHandler<SimConnectedCallbackArgs> onSimConnected = new CallbackHandler<>();
	public CallbackHandler<SimChangedCallbackArgs> onSimChanged = new CallbackHandler<>();
	public CallbackHandler<SimDisconnectedCallbackArgs> onSimDisconnected = new CallbackHandler<>();
	public CallbackHandler<DisconnectedCallbackArgs> onDisconnected = new CallbackHandler<>();
	public CallbackHandler<PacketSentCallbackArgs> onPacketSent = new CallbackHandler<>();
	public CallbackHandler<EventQueueRunningCallbackArgs> onEventQueueRunning = new CallbackHandler<>();
	public CallbackHandler<LoggedOutCallbackArgs> onLoggedOut = new CallbackHandler<>();

	private Map<PacketType, List<PacketCallback>> simCallbacks;
	private Map<CapsEventType, List<CapsCallback>> capCallbacks;

	private GridClient client;

	/**
	 * The ID number associated with this particular connection to the simulator,
	 * used to emulate TCP connections. This is used internally for packets that
	 * have a CircuitCode field.
	 */
	private int circuitCode;

	/**
	 * A list of packets obtained during the login process which NetworkManager will
	 * log but not process
	 */
	private final List<PacketType> udpBlacklist = new ArrayList<>();

	// Server side baking service URL
	private String agentAppearanceServiceURL;
	private List<AsyncHTTPClient<OSD>> closableClients = new ArrayList<>();

	private List<SimulatorManager> simulators;

	/** Incoming packets that are awaiting handling */
	private BlockingQueue<IncomingPacket> packetInbox = new LinkedBlockingQueue<>(LibSettings.PACKET_INBOX_SIZE);
	/** Outgoing packets that are awaiting handling */
	private BlockingQueue<OutgoingPacket> packetOutbox = new LinkedBlockingQueue<>(LibSettings.PACKET_OUTBOX_SIZE);

	private IncomingPacketHandler packetHandlerThread;

	private Timer disconnectTimer;
	private Timer logoutTimer;

	// The simulator that the logged in avatar is currently occupying
	private SimulatorManager currentSim;
	// Shows whether the network layer is logged in to the grid or not
	private boolean connected;

	private boolean syncPacketCallbacks;
	private boolean sendAgentUpdates;
	private boolean enableSimStats;

	/**
	 * Constructor for this manager
	 *
	 * @param client
	 *            The GridClient which controls this manager
	 * @throws Exception
	 */
	// <param name="client"></param>
	public NetworkManager(GridClient client) throws Exception {
		this.client = client;
		simulators = new ArrayList<>();
		simCallbacks = new HashMap<>();
		capCallbacks = new HashMap<>();
		logoutTimer = new Timer("LogoutTimer");
		currentSim = null;

		syncPacketCallbacks = client.settings.getBool(LibSettings.SYNC_PACKETCALLBACKS);
		sendAgentUpdates = client.settings.getBool(LibSettings.SEND_AGENT_UPDATES);
		enableSimStats = client.settings.getBool(LibSettings.ENABLE_SIMSTATS);
		client.settings.onSettingsUpdate.add(new SettingsUpdate());

		// Register internal CAPS callbacks
		registerCallback(CapsEventType.EnableSimulator, this);

		// Register the internal callbacks
		registerCallback(PacketType.RegionHandshake, this);
		registerCallback(PacketType.StartPingCheck, this);
		registerCallback(PacketType.DisableSimulator, this);
		registerCallback(PacketType.EnableSimulator, this);
		registerCallback(PacketType.KickUser, this);
		registerCallback(PacketType.LogoutReply, this);
		registerCallback(PacketType.CompletePingCheck, this);
		registerCallback(PacketType.SimStats, this);
	}

	/**
	 * Fire an event when an event queue connects for capabilities
	 *
	 * @param simulator
	 *            Simulator the event queue is attached to
	 */
	public void raiseSimConnectedEvent(Simulator simulator) {
		onSimConnected.dispatch(new SimConnectedCallbackArgs(simulator));
	}

	public void raisePacketSentCallback(byte[] data, int bytes, Simulator sim) {
		client.network.onPacketSent.dispatch(new PacketSentCallbackArgs(data, bytes, sim));
	}

	public final void raiseConnectedEvent(SimulatorManager simulator) {
		onEventQueueRunning.dispatch(new EventQueueRunningCallbackArgs(simulator));
	}

	public int getCircuitCode() {
		return circuitCode;
	}

	public void setCircuitCode(int code) {
		circuitCode = code;
	}

	public void setUDPBlacklist(String blacklist) {
		if (blacklist != null) {
			synchronized (udpBlacklist) {
				for (String s : blacklist.split(","))
					udpBlacklist.add(PacketType.valueOf(s));
				logger.debug(GridClient.Log("UDP blacklisted packets: " + udpBlacklist.toString(), client));
			}
		}
	}

	public void setAgentAppearanceServiceURL(String url) {
		agentAppearanceServiceURL = url;
	}

	public String getAgentAppearanceServiceURL() {
		return agentAppearanceServiceURL;
	}

	public void addClosableClient(AsyncHTTPClient<OSD> client) {
		synchronized (closableClients) {
			closableClients.add(client);
		}
	}

	private void cleanClosableClients() throws InterruptedException, IOException {
		synchronized (closableClients) {
			long time = System.currentTimeMillis();
			for (AsyncHTTPClient<OSD> client : closableClients) {
				client.shutdown(false);
			}

			if (closableClients.size() > 0) {
				logger.info(GridClient.Log("Closing " + closableClients.size() + " clients in "
						+ (System.currentTimeMillis() - time) + " ms.", client));
				closableClients.clear();
			}
		}
	}

	/**
	 * Get the array with all currently known simulators. This list must be
	 * protected with a synchronization lock on itself if you do anything with it.
	 *
	 * @return array of simulator objects known to this client
	 */
	public List<SimulatorManager> getSimulators() {
		return simulators;
	}

	/** Number of packets in the incoming queue */
	public final int getInboxCount() {
		return packetInbox.size();
	}

	/** Number of packets in the outgoing queue */
	public final int getOutboxCount() {
		return packetOutbox.size();
	}

	private void firePacketCallbacks(Packet packet, Simulator simulator) {
		boolean specialHandler = false;
		PacketType type = packet.getType();

		synchronized (simCallbacks) {
			// Fire any default callbacks
			List<PacketCallback> callbackArray = simCallbacks.get(PacketType.Default);
			if (callbackArray != null) {
				for (PacketCallback callback : callbackArray) {
					try {
						callback.packetCallback(packet, simulator);
					} catch (Exception ex) {
						logger.error(GridClient.Log("Default packet event handler: " + type, client), ex);
					}
				}
			}
			// Fire any registered callbacks
			callbackArray = simCallbacks.get(type);
			if (callbackArray != null) {
				for (PacketCallback callback : callbackArray) {
					try {
						callback.packetCallback(packet, simulator);
					} catch (Exception ex) {
						logger.error(GridClient.Log("Packet event handler: " + type, client), ex);
					}
					specialHandler = true;
				}
			}
		}

		if (!specialHandler && type != PacketType.Default && type != PacketType.PacketAck) {
			// logger.test("No handler registered for packet event " + type,
			// LogLevel.Warning, _Client);
		}
	}

	private void fireCapsCallbacks(IMessage message, SimulatorManager simulator) {
		boolean specialHandler = false;

		synchronized (capCallbacks) {
			// Fire any default callbacks
			List<CapsCallback> callbackArray = capCallbacks.get(CapsEventType.Default);
			if (callbackArray != null) {
				for (CapsCallback callback : callbackArray) {
					try {
						callback.capsCallback(message, simulator);
					} catch (Exception ex) {
						logger.error(GridClient.Log("CAPS event handler: " + message.getType(), client), ex);
					}
				}
			}
			// Fire any registered callbacks
			callbackArray = capCallbacks.get(message.getType());
			if (callbackArray != null) {
				for (CapsCallback callback : callbackArray) {
					try {
						callback.capsCallback(message, simulator);
					} catch (Exception ex) {
						logger.error(GridClient.Log("CAPS event handler: " + message.getType(), client), ex);
					}
					specialHandler = true;
				}
			}
		}
		if (!specialHandler) {
			logger.warn(GridClient.Log("Unhandled CAPS event " + message.getType(), client));
		}
	}

	public SimulatorManager getCurrentSim() {
		return currentSim;
	}

	public final void setCurrentSim(SimulatorManager value) {
		currentSim = value;
	}

	public boolean getConnected() {
		return connected;
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case RegionHandshake:
			handleRegionHandshake(packet, simulator);
			break;
		case StartPingCheck:
			handleStartPingCheck(packet, simulator);
			break;
		case CompletePingCheck:
			handleCompletePingCheck(packet, simulator);
			break;
		case EnableSimulator:
			handleEnableSimulator(packet, simulator);
			break;
		case DisableSimulator:
			handleDisableSimulator(packet, simulator);
			break;
		case LogoutReply:
			handleLogoutReply(packet, simulator);
			break;
		case SimStats:
			handleSimStats(packet, simulator);
			break;
		case KickUser:
			handleKickUser(packet, simulator);
			break;
		default:
			break;
		}
	}

	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) throws Exception {
		switch (message.getType()) {
		case EnableSimulator:
			handleEnableSimulator(message, simulator);
			break;
		default:
			break;
		}

	}

	/**
	 * Get the capability URL from the current simulator
	 *
	 * @param capability
	 *            The name of the capability to retrieve the URL from
	 * @return The URI for the capability or null if it doesn't exist
	 */
	public URI getCapabilityURI(String capability) {
		return getCapabilityURI(capability, currentSim);
	}

	/**
	 * Get the capability URL from the currenta specific simulator
	 *
	 * @param capability
	 *            The name of the capability to retrieve the URL from
	 * @param simulator
	 *            The simulator for which the capability URL should be returned If
	 *            "simulator" is null, this function uses the current simulator
	 * @return The URI for the capability or null if it doesn't exist
	 */
	public URI getCapabilityURI(String capability, SimulatorManager simulator) {
		synchronized (simulators) {
			if (simulator == null)
				simulator = currentSim;

			if (simulator != null) {
				return simulator.getCapabilityURI(capability);
			}
		}
		return null;
	}

	public boolean getIsEventQueueRunning() {
		synchronized (simulators) {
			return currentSim != null && currentSim.getIsEventQueueRunning();
		}
	}

	public void registerCallback(CapsEventType capability, CapsCallback callback) {
		/* Don't accept null callbacks */
		if (callback == null)
			return;

		synchronized (capCallbacks) {
			List<CapsCallback> callbacks = capCallbacks.get(capability);
			if (callbacks == null) {
				callbacks = new ArrayList<>();
				capCallbacks.put(capability, callbacks);
			} else {
				callbacks.remove(callback);
			}
			callbacks.add(callback);
		}
	}

	public void unregisterCallback(CapsEventType capability, CapsCallback callback) {
		synchronized (capCallbacks) {
			if (!capCallbacks.containsKey(capability)) {
				logger.info(GridClient.Log("Trying to unregister a callback for capability " + capability
						+ " when no callbacks are setup for that capability", client));
				return;
			}

			List<CapsCallback> callbackArray = capCallbacks.get(capability);

			if (callbackArray.contains(callback)) {
				callbackArray.remove(callback);
				if (callbackArray.isEmpty()) {
					capCallbacks.remove(capability);
				}
			} else {
				logger.info(GridClient.Log("Trying to unregister a non-existant callback for capability " + capability,
						client));
			}
		}
	}

	public void registerCallback(PacketType type, PacketCallback callback) {
		/* Don't accept null callbacks */
		if (callback == null)
			return;

		synchronized (simCallbacks) {
			List<PacketCallback> callbacks = simCallbacks.get(type);
			if (callbacks == null) {
				callbacks = new ArrayList<>();
				simCallbacks.put(type, callbacks);
			} else {
				callbacks.remove(callback);
			}
			callbacks.add(callback);
		}
	}

	public void unregisterCallback(PacketType type, PacketCallback callback) {
		synchronized (simCallbacks) {
			if (!simCallbacks.containsKey(type)) {
				logger.info(GridClient.Log("Trying to unregister a callback for packet " + type
						+ " when no callbacks are setup for that packet", client));
				return;
			}

			List<PacketCallback> callbackArray = simCallbacks.get(type);
			if (callbackArray.contains(callback)) {
				callbackArray.remove(callback);
				if (callbackArray.isEmpty()) {
					simCallbacks.remove(type);
				}
			} else {
				logger.info(GridClient.Log("Trying to unregister a non-existant callback for packet " + type, client));
			}
		}
	}

	/**
	 * Send an UDP packet to the current simulator
	 *
	 * @param packet
	 *            The packet to send
	 * @throws Exception
	 */
	public void sendPacket(Packet packet) throws Exception {
		// try CurrentSim, however directly after login this will be null, so if it is,
		// we'll
		// try to find the first simulator we're connected to in order to send the
		// packet.
		SimulatorManager simulator = currentSim;
		if (simulator == null) {
			synchronized (simulators) {
				if (simulators.size() >= 1) {
					logger.debug(GridClient.Log("CurrentSim object was null, using first found connected simulator",
							client));
					simulator = simulators.get(0);
				}
			}
		}

		if (simulator != null && simulator.getConnected()) {
			simulator.sendPacket(packet);
		} else {
			ConnectException ex = new ConnectException(
					"Packet received before simulator packet processing threads running, make certain you are completely logged in");
			logger.error(GridClient.Log(ex.getMessage(), client), ex);
			throw ex;
		}
	}

	public void queuePacket(OutgoingPacket packet) throws InterruptedException {
		packetOutbox.put(packet);
	}

	public void distributePacket(Simulator simulator, Packet packet) {
		try {
			packetInbox.add(new IncomingPacket(simulator, packet));
		} catch (Exception ex) {
			logger.warn(GridClient.Log("Suppressing packet " + packet.toString(), client), ex);
		}
	}

	public void distributeCaps(Simulator simulator, IMessage message) {
		try {
			packetInbox.add(new IncomingPacket(simulator, message));
		} catch (Exception ex) {
			logger.warn(GridClient.Log("Suppressing message " + message.toString(), client), ex);
		}
	}

	public SimulatorManager connect(InetAddress ip, short port, long handle, boolean setDefault, String seedcaps)
			throws Exception {
		return connect(new InetSocketAddress(ip, port), handle, setDefault, seedcaps);
	}

	/**
	 * Connect to a simulator
	 *
	 * @param endPoint
	 *            IP address and port to connect to
	 * @param handle
	 *            Handle for this simulator, to identify its location in the grid
	 * @param setDefault
	 *            Whether to set CurrentSim to this new connection, use this if the
	 *            avatar is moving in to this simulator
	 * @param seedcaps
	 *            URL of the capabilities server to use for this sim connection
	 * @return A Simulator object on success, otherwise null
	 */
	public SimulatorManager connect(InetSocketAddress endPoint, long handle, boolean setDefault, String seedcaps)
			throws Exception {
		SimulatorManager simulator = findSimulator(endPoint);

		if (simulator == null) {
			// We're not tracking this sim, create a new Simulator object
			simulator = new SimulatorManager(client, endPoint, handle);

			synchronized (simulators) {
				// Immediately add this simulator to the list of current sims.
				// It will be removed if the connection fails
				simulators.add(simulator);
			}
		}

		if (!simulator.getConnected()) {
			if (!connected) {
				// Mark that we are connecting/connected to the grid
				connected = true;

				// Start the packet decoding thread
				packetHandlerThread = new IncomingPacketHandler();
				Thread decodeThread = new Thread(packetHandlerThread);
				decodeThread.setName("Incoming UDP packet dispatcher");
				decodeThread.start();

				// Start the packet sending thread
				Thread sendThread = new Thread(new OutgoingPacketHandler());
				sendThread.setName("Outgoing UDP packet dispatcher");
				sendThread.start();
			}

			if (onSimConnecting.count() > 0) {
				SimConnectingCallbackArgs args = new SimConnectingCallbackArgs(endPoint);
				onSimConnecting.dispatch(args);
				if (args.getCancel()) {
					synchronized (simulators) {
						// Callback is requesting that we abort this connection
						simulators.remove(simulator);
					}
					return null;
				}
			}

			// Attempt to establish a connection to the simulator
			if (simulator.connect(setDefault)) {
				if (disconnectTimer == null) {
					// Start a timer that checks if we've been disconnected
					disconnectTimer = new Timer("_DisconnectTimer");
					disconnectTimer.scheduleAtFixedRate(new DisconnectTimer_Elapsed(),
							client.settings.SIMULATOR_TIMEOUT, client.settings.SIMULATOR_TIMEOUT);
				}

				if (setDefault) {
					setCurrentSim(simulator, seedcaps);
				}

				// Raise the SimConnected event
				onSimConnected.dispatch(new SimConnectedCallbackArgs(simulator));

				// If enabled, send an AgentThrottle packet to the server to
				// increase our bandwidth
				if (client.throttle != null) {
					client.throttle.set(simulator);
				}
			} else {
				synchronized (simulators) {
					// Connection failed, remove this simulator from our list
					// and destroy it
					simulators.remove(simulator);
				}
				return null;
			}
		} else if (setDefault) {
			// Move in to this simulator
			simulator.useCircuitCode();
			client.agent.completeAgentMovement(simulator);

			// We're already connected to this server, but need to set it to the default
			setCurrentSim(simulator, seedcaps);

			// Send an initial AgentUpdate to complete our movement in to the sim
			if (sendAgentUpdates) {
				client.agent.sendMovementUpdate(true, simulator);
			}
		} else {
			// Already connected to this simulator and wasn't asked to set it as
			// the default, just return a reference to the existing object
		}
		return simulator;
	}

	/**
	 * Begins the non-blocking logout. Makes sure that the LoggedOut event is called
	 * even if the server does not send a logout reply, and shutdown() is properly
	 * called.
	 *
	 * @throws Exception
	 */
	public void beginLogout() throws Exception {
		// Wait for a logout response (by way of the LoggedOut event. If the response is
		// received,
		// shutdown will be fired in the callback itself that caused this event to be
		// triggered.
		// Otherwise we fire it manually with a NetworkTimeout type after LOGOUT_TIMEOUT
		class LoggedOutHandler extends TimerTask implements Callback<LoggedOutCallbackArgs> {
			// Executed when the timer times out
			@Override
			public void run() {
				try {
					shutdown(DisconnectType.NetworkTimeout, "User logged out");
				} catch (Exception e) {
				}
				/* Remove ourself from the event dispatcher */
				onLoggedOut.remove(this);
				onLoggedOut.dispatch(new LoggedOutCallbackArgs(new Vector<>()));
			}

			// Executed when the log out resulted in an acknowledgement from the server
			@Override
			public boolean callback(LoggedOutCallbackArgs params) {
				this.cancel();
				/* Remove ourself from the event dispatcher */
				return true;
			}

		}

		LoggedOutHandler timeoutTask = new LoggedOutHandler();

		onLoggedOut.add(timeoutTask);

		// Send the packet requesting a clean logout
		requestLogout();
		logoutTimer.schedule(timeoutTask, client.settings.LOGOUT_TIMEOUT);
	}

	/**
	 * Initiate a blocking logout request. This will return when the logout
	 * handshake has completed or when <code>Settings.LOGOUT_TIMEOUT</code> has
	 * expired and the network layer is manually shut down
	 */
	public void logout() throws Exception {
		final TimeoutEvent<Boolean> timeout = new TimeoutEvent<>();

		Callback<LoggedOutCallbackArgs> loggedOut = new Callback<LoggedOutCallbackArgs>() {
			@Override
			public boolean callback(LoggedOutCallbackArgs params) {
				timeout.set(true);
				/* Remove ourself from the event dispatcher */
				return true;
			}
		};

		onLoggedOut.add(loggedOut);

		// Send the packet requesting a clean logout
		requestLogout();

		// Wait for a logout response. If the response is received, shutdown() will
		// be fired in the callback. Otherwise we fire it manually with a NetworkTimeout
		// type
		Boolean success = timeout.waitOne(client.settings.LOGOUT_TIMEOUT);
		if (success == null || !success) {
			// Shutdown the network layer
			shutdown(DisconnectType.NetworkTimeout, "User logged out");
		}
		onLoggedOut.remove(loggedOut);
	}

	/**
	 * Initiate the logout process. The <code>Shutdown()</code> function needs to be
	 * manually called.
	 *
	 * @throws Exception
	 */
	public void requestLogout() throws Exception {
		if (disconnectTimer == null) {
			disconnectTimer.cancel();
			disconnectTimer = null;
		}

		// This will catch a Logout when the client is not logged in
		if (currentSim == null || !connected) {
			return;
		}

		connected = false;
		client.setCurrentGrid((String) null);
		packetHandlerThread.shutdown();

		logger.info(GridClient.Log("Logging out", client));

		// Send a logout request to the current sim
		LogoutRequestPacket logout = new LogoutRequestPacket();
		logout.AgentData.AgentID = client.agent.getAgentID();
		logout.AgentData.SessionID = client.agent.getSessionID();

		currentSim.sendPacket(logout);
	}

	private void setCurrentSim(SimulatorManager simulator, String seedcaps) throws InterruptedException, IOException {
		if (!simulator.equals(getCurrentSim())) {
			Simulator oldSim = getCurrentSim();
			synchronized (simulators) // CurrentSim is synchronized against
										// Simulators
			{
				setCurrentSim(simulator);
			}
			simulator.setSeedCaps(seedcaps);

			// If the current simulator changed fire the callback
			if (!simulator.equals(oldSim)) {
				onSimChanged.dispatch(new SimChangedCallbackArgs(oldSim));
			}
		}
	}

	public void disconnectSim(Runnable sim, boolean sendCloseCircuit) throws Exception {
		if (sim != null) {
			SimulatorManager simulator = (SimulatorManager) sim;
			simulator.disconnect(sendCloseCircuit);

			// Fire the SimDisconnected event if a handler is registered
			onSimDisconnected.dispatch(new SimDisconnectedCallbackArgs(simulator, DisconnectType.NetworkTimeout));

			synchronized (simulators) {
				simulators.remove(simulator);
				if (simulators.isEmpty()) {
					shutdown(DisconnectType.SimShutdown, "Last simulator disconnected");
				}
			}
		} else {
			logger.warn("DisconnectSim() called with a null Simulator reference");
		}
	}

	/**
	 * Shutdown will disconnect all the sims except for the current sim first, and
	 * then kill the connection to CurrentSim. This should only be called if the
	 * logout process times out on <code>RequestLogout</code>
	 *
	 * @param type
	 *            Type of shutdown
	 * @throws Exception
	 */
	public final void shutdown(DisconnectType type) throws Exception {
		shutdown(type, type.toString());
	}

	private void shutdown(DisconnectType type, String message) throws Exception {
		logger.info(GridClient.Log("NetworkManager shutdown initiated", client));

		// Send a CloseCircuit packet to simulators if we are initiating the
		// disconnect
		boolean sendCloseCircuit = type == DisconnectType.ClientInitiated || type == DisconnectType.NetworkTimeout;

		synchronized (simulators) {
			// Disconnect all simulators except the current one
			for (int i = 0; i < simulators.size(); i++) {
				SimulatorManager simulator = simulators.get(i);
				// Don't disconnect the current sim, we'll use LogoutRequest for
				// that
				if (simulator != null && !simulator.equals(currentSim)) {
					simulator.disconnect(sendCloseCircuit);

					// Fire the SimDisconnected event if a handler is registered
					onSimDisconnected
							.dispatch(new SimDisconnectedCallbackArgs(simulator, DisconnectType.NetworkTimeout));
				}

			}
			simulators.clear();

			if (currentSim != null) {
				currentSim.disconnect(sendCloseCircuit);

				// Fire the SimDisconnected event if a handler is registered
				onSimDisconnected.dispatch(new SimDisconnectedCallbackArgs(currentSim, DisconnectType.NetworkTimeout));
			}

		}
		connected = false;
		logoutTimer.cancel();
		logoutTimer = null;

		if (onDisconnected.count() > 0) {
			onDisconnected.dispatch(new DisconnectedCallbackArgs(type, message));
		}
	}

	/**
	 * Searches through the list of currently connected simulators to find one
	 * attached to the given IPEndPoint
	 *
	 * @param endPoint
	 *            InetSocketAddress of the Simulator to search for
	 * @return A Simulator reference on success, otherwise null
	 */
	public final SimulatorManager findSimulator(InetSocketAddress endPoint) {
		synchronized (simulators) {
			for (SimulatorManager simulator : simulators) {
				if (simulator.getIPEndPoint().equals(endPoint)) {
					return simulator;
				}
			}
		}
		return null;
	}

	private void handleRegionHandshake(Packet packet, Simulator sim) throws Exception {
		RegionHandshakePacket handshake = (RegionHandshakePacket) packet;
		SimulatorManager simulator = (SimulatorManager) sim;
		simulator.id = handshake.RegionInfo.CacheID;

		simulator.isEstateManager = handshake.RegionInfo.IsEstateManager;
		simulator.setSimName(Helpers.bytesToString(handshake.RegionInfo.getSimName()));
		simulator.simOwner = handshake.RegionInfo.SimOwner;
		simulator.terrainBase0 = handshake.RegionInfo.TerrainBase0;
		simulator.terrainBase1 = handshake.RegionInfo.TerrainBase1;
		simulator.terrainBase2 = handshake.RegionInfo.TerrainBase2;
		simulator.terrainBase3 = handshake.RegionInfo.TerrainBase3;
		simulator.terrainDetail0 = handshake.RegionInfo.TerrainDetail0;
		simulator.terrainDetail1 = handshake.RegionInfo.TerrainDetail1;
		simulator.terrainDetail2 = handshake.RegionInfo.TerrainDetail2;
		simulator.terrainDetail3 = handshake.RegionInfo.TerrainDetail3;
		simulator.terrainHeightRange00 = handshake.RegionInfo.TerrainHeightRange00;
		simulator.terrainHeightRange01 = handshake.RegionInfo.TerrainHeightRange01;
		simulator.terrainHeightRange10 = handshake.RegionInfo.TerrainHeightRange10;
		simulator.terrainHeightRange11 = handshake.RegionInfo.TerrainHeightRange11;
		simulator.terrainStartHeight00 = handshake.RegionInfo.TerrainStartHeight00;
		simulator.terrainStartHeight01 = handshake.RegionInfo.TerrainStartHeight01;
		simulator.terrainStartHeight10 = handshake.RegionInfo.TerrainStartHeight10;
		simulator.terrainStartHeight11 = handshake.RegionInfo.TerrainStartHeight11;

		simulator.waterHeight = handshake.RegionInfo.WaterHeight;
		simulator.flags = RegionFlags.setValue(handshake.RegionInfo.RegionFlags);
		simulator.billableFactor = handshake.RegionInfo.BillableFactor;
		simulator.access = SimAccess.setValue(handshake.RegionInfo.SimAccess);

		simulator.regionID = handshake./* RegionInfo2. */RegionID;

		simulator.coLocation = Helpers.bytesToString(handshake.RegionInfo3.getColoName());
		simulator.cpuClass = handshake.RegionInfo3.CPUClassID;
		simulator.cpuRatio = handshake.RegionInfo3.CPURatio;
		simulator.productName = Helpers.bytesToString(handshake.RegionInfo3.getProductName());
		simulator.productSku = Helpers.bytesToString(handshake.RegionInfo3.getProductSKU());

		if (handshake.RegionInfo4 != null && handshake.RegionInfo4.length > 0) {
			simulator.protocols = RegionProtocols.setValue(handshake.RegionInfo4[0].RegionProtocols);
			// Yes, overwrite region flags if we have extended version of them
			simulator.flags = RegionFlags.setValue(handshake.RegionInfo4[0].RegionFlagsExtended);
		}

		// Send a RegionHandshakeReply
		RegionHandshakeReplyPacket reply = new RegionHandshakeReplyPacket();
		reply.AgentData.AgentID = client.agent.getAgentID();
		reply.AgentData.SessionID = client.agent.getSessionID();
		reply.Flags = (int) RegionProtocols.SelfAppearanceSupport;
		simulator.sendPacket(reply);

		logger.debug(GridClient.Log("Received a region handshake for " + simulator.getName(), client));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The packet data
	 * @param simulator
	 *            The sender
	 * @throws Exception
	 */
	private void handleStartPingCheck(Packet packet, Simulator simulator) throws Exception {
		StartPingCheckPacket incomingPing = (StartPingCheckPacket) packet;
		CompletePingCheckPacket ping = new CompletePingCheckPacket();
		ping.PingID = incomingPing.PingID.PingID;
		ping.getHeader().setReliable(false);
		// TODO: We can use OldestUnacked to correct transmission errors
		// I don't think that's right. As far as I can tell, the Viewer
		// only uses this to prune its duplicate-checking buffer. -bushing
		simulator.sendPacket(ping);
	}

	/**
	 * Process a ping answer
	 */
	private final void handleCompletePingCheck(Packet packet, Simulator sim) {
		CompletePingCheckPacket pong = (CompletePingCheckPacket) packet;
		long timeMilli = System.currentTimeMillis();

		SimulatorManager simulator = (SimulatorManager) sim;
		simulator.statistics.lastLag = timeMilli - simulator.statistics.lastPingSent;
		simulator.statistics.receivedPongs++;

		if (LibSettings.OUTPUT_TIMING_STATS) {
			String retval = "Pong2: " + simulator.getName() + " lag : " + simulator.statistics.lastLag + "ms";

			if ((pong.PingID - simulator.statistics.lastPingID + 1) != 0) {
				retval += " (gap of " + (pong.PingID - simulator.statistics.lastPingID + 1) + ")";
			}

			logger.debug(GridClient.Log(retval, client));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The packet data
	 * @param simulator
	 *            The sender
	 */
	private final void handleSimStats(Packet packet, Simulator sim) {
		if (enableSimStats) {
			SimulatorManager simulator = (SimulatorManager) sim;
			SimStatsPacket stats = (SimStatsPacket) packet;
			for (int i = 0; i < stats.Stat.length; i++) {
				SimStatsPacket.StatBlock s = stats.Stat[i];

				switch (SimStatType.setValue(s.StatID)) {
				case TimeDilation:
					simulator.statistics.dilation = s.StatValue;
					break;
				case SimFPS:
					simulator.statistics.fps = Helpers.bytesToInt32L(Helpers.floatToBytesL(s.StatValue));
					break;
				case PhysicsFPS:
					simulator.statistics.physicsFPS = s.StatValue;
					break;
				case AgentUpdates:
					simulator.statistics.agentUpdates = s.StatValue;
					break;
				case FrameMS:
					simulator.statistics.frameTime = s.StatValue;
					break;
				case NetMS:
					simulator.statistics.netTime = s.StatValue;
					break;
				case OtherMS:
					simulator.statistics.otherTime = s.StatValue;
					break;
				case PhysicsMS:
					simulator.statistics.physicsTime = s.StatValue;
					break;
				case AgentMS:
					simulator.statistics.agentTime = s.StatValue;
					break;
				case ImageMS:
					simulator.statistics.imageTime = s.StatValue;
					break;
				case ScriptMS:
					simulator.statistics.scriptTime = s.StatValue;
					break;
				case TotalPrim:
					simulator.statistics.objects = (int) s.StatValue;
					break;
				case ActivePrim:
					simulator.statistics.scriptedObjects = (int) s.StatValue;
					break;
				case Agents:
					simulator.statistics.agents = (int) s.StatValue;
					break;
				case ChildAgents:
					simulator.statistics.childAgents = (int) s.StatValue;
					break;
				case ActiveScripts:
					simulator.statistics.activeScripts = (int) s.StatValue;
					break;
				case ScriptInstructionsPerSecond:
					simulator.statistics.lslIPS = (int) s.StatValue;
					break;
				case InPacketsPerSecond:
					simulator.statistics.inPPS = (int) s.StatValue;
					break;
				case OutPacketsPerSecond:
					simulator.statistics.outPPS = (int) s.StatValue;
					break;
				case PendingDownloads:
					simulator.statistics.pendingDownloads = (int) s.StatValue;
					break;
				case PendingUploads:
					simulator.statistics.pendingUploads = (int) s.StatValue;
					break;
				case VirtualSizeKB:
					simulator.statistics.virtualSize = (int) s.StatValue;
					break;
				case ResidentSizeKB:
					simulator.statistics.residentSize = (int) s.StatValue;
					break;
				case PendingLocalUploads:
					simulator.statistics.pendingLocalUploads = (int) s.StatValue;
					break;
				case UnAckedBytes:
					simulator.statistics.unackedBytes = (int) s.StatValue;
					break;
				case PhysicsPinnedTasks:
					simulator.statistics.physicsPinnedTasks = (int) s.StatValue;
					break;
				case PhysicsLODTasks:
					simulator.statistics.physicsLODTasks = (int) s.StatValue;
					break;
				case PhysicsStepMS:
					simulator.statistics.physicsStepMS = (int) s.StatValue;
					break;
				case PhysicsShapeMS:
					simulator.statistics.physicsShapeMS = (int) s.StatValue;
					break;
				case PhysicsOtherMS:
					simulator.statistics.physicsOtherMS = (int) s.StatValue;
					break;
				case PhysicsMemory:
					simulator.statistics.physicsMemory = (int) s.StatValue;
					break;
				case ScriptEPS:
					simulator.statistics.scriptEPS = (int) s.StatValue;
					break;
				case SimSpareTime:
					simulator.statistics.simSpareTime = (int) s.StatValue;
					break;
				case SimSleepTime:
					simulator.statistics.simSleepTime = (int) s.StatValue;
					break;
				case SimIOPumpTime:
					simulator.statistics.simIOPumpTime = (int) s.StatValue;
					break;
				case SimPctScriptsRun:
					simulator.statistics.simPctScriptsRun = (int) s.StatValue;
					break;
				case SimAIStepMsec:
					simulator.statistics.simAIStepMsec = (int) s.StatValue;
					break;
				case SimSkippedSilhouetteSteps:
					simulator.statistics.simSkippedSilhouetteSteps = (int) s.StatValue;
					break;
				case SimPctSteppedCharacters:
					simulator.statistics.simPctSteppedCharacters = (int) s.StatValue;
					break;
				default:
					logger.debug(GridClient.Log("Unknown stat id: " + s.StatID, client));
					break;
				}
			}
		}
	}

	private void handleEnableSimulator(IMessage message, Simulator simulator) throws Exception {
		if (client.settings.getBool(LibSettings.MULTIPLE_SIMS)) {
			EnableSimulatorMessage msg = (EnableSimulatorMessage) message;

			for (int i = 0; i < msg.simulators.length; i++) {
				InetAddress ip = msg.simulators[i].ip;
				InetSocketAddress endPoint = new InetSocketAddress(ip, msg.simulators[i].port);

				if (findSimulator(endPoint) != null)
					return;

				if (connect(endPoint, msg.simulators[i].regionHandle, false, null) == null) {
					logger.error(GridClient.Log("Unable to connect to new sim " + ip + ":" + msg.simulators[i].port,
							client));
				}
			}
		}
	}

	private void handleEnableSimulator(Packet packet, Simulator simulator) throws Exception {
		if (client.settings.getBool(LibSettings.MULTIPLE_SIMS)) {
			EnableSimulatorPacket msg = (EnableSimulatorPacket) packet;

			InetAddress ip = InetAddress.getByAddress(Helpers.int32ToBytesB(msg.SimulatorInfo.IP));
			InetSocketAddress endPoint = new InetSocketAddress(ip, msg.SimulatorInfo.Port);

			if (findSimulator(endPoint) != null)
				return;

			if (connect(endPoint, msg.SimulatorInfo.Handle, false, null) == null) {
				logger.error(
						GridClient.Log("Unable to connect to new sim " + ip + ":" + msg.SimulatorInfo.Port, client));
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param simulator
	 *            The sender
	 * @param packet
	 *            The packet data
	 * @throws Exception
	 */
	private final void handleDisableSimulator(Packet packet, Simulator simulator) throws Exception {
		disconnectSim(simulator, false);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param packet
	 *            The packet data
	 * @param simulator
	 *            The sender
	 * @throws Exception
	 */
	private void handleLogoutReply(Packet packet, Simulator simulator) throws Exception {
		LogoutReplyPacket logout = (LogoutReplyPacket) packet;

		if ((logout.AgentData.SessionID.equals(client.agent.getSessionID()))
				&& (logout.AgentData.AgentID.equals(client.agent.getAgentID()))) {
			logger.debug(GridClient.Log("Logout reply received", client));

			// Deal with callbacks, if any
			if (onLoggedOut.count() > 0) {
				Vector<UUID> itemIDs = new Vector<>();

				for (UUID inventoryID : logout.ItemID) {
					itemIDs.add(inventoryID);
				}
				onLoggedOut.dispatch(new LoggedOutCallbackArgs(itemIDs));
			}

			// If we are receiving a LogoutReply packet assume this is a client
			// initiated shutdown
			shutdown(DisconnectType.ClientInitiated, "Logout from simulator");
		} else {
			logger.warn(GridClient.Log("Invalid Session or Agent ID received in Logout Reply... ignoring", client));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 * @param simulator
	 *            The sender
	 * @param packet
	 *            The packet data
	 */
	private void handleKickUser(Packet packet, Simulator simulator) throws Exception {
		String message = Helpers.bytesToString(((KickUserPacket) packet).UserInfo.getReason());

		// Shutdown the network layer
		shutdown(DisconnectType.ServerInitiated, message);
	}
}
