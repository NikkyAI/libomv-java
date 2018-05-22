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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import libomv.Statistics.Type;
import libomv.io.capabilities.CapsManager;
import libomv.model.Parcel;
import libomv.model.network.OutgoingPacket;
import libomv.model.simulator.IncomingPacketIDCollection;
import libomv.model.simulator.SimAccess;
import libomv.model.simulator.SimStats;
import libomv.model.terrain.TerrainPatch;
import libomv.packets.AgentPausePacket;
import libomv.packets.AgentResumePacket;
import libomv.packets.CloseCircuitPacket;
import libomv.packets.Packet;
import libomv.packets.PacketAckPacket;
import libomv.packets.PacketType;
import libomv.packets.StartPingCheckPacket;
import libomv.packets.UseCircuitCodePacket;
import libomv.primitives.Avatar;
import libomv.primitives.Primitive;
import libomv.types.PacketHeader;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.Settings.SettingsUpdateCallbackArgs;

// Simulator is a wrapper for a network connection to a simulator and the
// Region class representing the block of land in the metaverse.
public class SimulatorManager extends Thread implements libomv.model.Simulator {
	private static final Logger logger = Logger.getLogger(SimulatorManager.class);

	private class BoundedLongArray {
		private long[] array;
		private int index;
		private int size;

		public BoundedLongArray(int capacity) {
			array = new long[capacity];
			index = size = 0;
		}

		public int size() {
			return size;
		}

		public boolean isFull() {
			return size >= array.length;
		}

		/*
		 * public long poll() { if (size > 0) { return array[(index - size--) %
		 * array.length]; } return 0; }
		 */
		/**
		 * Offer a new value to put in the queue
		 *
		 * @param value
		 *            The value to put in the queue
		 * @return The previous value that was in the queue at that position
		 */
		public long offer(long value) {
			long old = array[index];
			array[index] = value;
			++index;
			index %= array.length;
			if (!isFull())
				size++;
			return old;
		}
	}

	private class AckTimer_Elapsed extends TimerTask {
		@Override
		public void run() {
			if (!connected) {
				return;
			}

			sendPendingAcks();
			resendUnacked();

			ackTimer.schedule(new AckTimer_Elapsed(), LibSettings.NETWORK_TICK_INTERVAL);
		}
	}

	private class StatsTimer_Elapsed extends TimerTask {
		@Override
		public void run() {
			boolean full = inBytes.isFull();
			long recv = statistics.recvBytes;
			long sent = statistics.sentBytes;
			long oldIn = inBytes.offer(recv);
			long oldOut = outBytes.offer(sent);

			if (full) {
				statistics.incomingBPS = (int) (recv - oldIn) / inBytes.size();
				statistics.outgoingBPS = (int) (sent - oldOut) / outBytes.size();
				logger.debug(GridClient.Log(getName() + ", Incoming: " + statistics.incomingBPS + " bps, Out: "
						+ statistics.outgoingBPS + " bps, Lag: " + statistics.lastLag + " ms, Pings: "
						+ statistics.receivedPongs + "/" + statistics.sentPings, client));
			}
		}
	}

	private class PingTimer_Elapsed extends TimerTask {
		@Override
		public void run() {
			try {
				sendPing();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			statistics.sentPings++;
		}
	}

	private class SettingsUpdate implements Callback<SettingsUpdateCallbackArgs> {
		@Override
		public boolean callback(SettingsUpdateCallbackArgs params) {
			String key = params.getName();
			if (key == null) {
				trackUtilization = client.settings.getBool(LibSettings.TRACK_UTILIZATION);
				throttleOutgoingPackets = client.settings.getBool(LibSettings.THROTTLE_OUTGOING_PACKETS);
			} else if (key.equals(LibSettings.TRACK_UTILIZATION)) {
				trackUtilization = params.getValue().asBoolean();
			} else if (key.equals(LibSettings.THROTTLE_OUTGOING_PACKETS)) {
				throttleOutgoingPackets = params.getValue().asBoolean();
			}
			return false;
		}
	}

	/* A Unique Cache identifier for this simulator */
	public UUID id = UUID.ZERO;

	public final TerrainPatch[] terrain;

	public final Vector2[] windSpeeds;

	// Provides access to an internal thread-safe dictionary containing parcel
	// information found in this simulator
	public Map<Integer, Parcel> parcels = new HashMap<>();

	// Is it safe to send agent updates to this sim
	// AgentMovementComplete message received
	public boolean agentMovementComplete;

	/*
	 * Statistics information for this simulator and the connection to the
	 * simulator, calculated by the simulator itself and the library
	 */
	public SimStats statistics;

	/* The current version of software this simulator is running */
	public String simVersion = "";

	/*
	 * A 64x64 grid of parcel coloring values. The values stored in this array are
	 * of the {@link ParcelArrayType} type
	 */
	public byte[] parcelOverlay = new byte[4096];
	/*  */
	public int parcelOverlaysReceived;
	/*  */
	public float terrainHeightRange00;
	/*  */
	public float terrainHeightRange01;
	/*  */
	public float terrainHeightRange10;
	/*  */
	public float terrainHeightRange11;
	/*  */
	public float terrainStartHeight00;
	/*  */
	public float terrainStartHeight01;
	/*  */
	public float terrainStartHeight10;
	/*  */
	public float terrainStartHeight11;
	/*  */
	public float waterHeight;
	/*  */
	public UUID simOwner = UUID.ZERO;
	/*  */
	public UUID terrainBase0 = UUID.ZERO;
	/*  */
	public UUID terrainBase1 = UUID.ZERO;
	/*  */
	public UUID terrainBase2 = UUID.ZERO;
	/*  */
	public UUID terrainBase3 = UUID.ZERO;
	/*  */
	public UUID terrainDetail0 = UUID.ZERO;
	/*  */
	public UUID terrainDetail1 = UUID.ZERO;
	/*  */
	public UUID terrainDetail2 = UUID.ZERO;
	/*  */
	public UUID terrainDetail3 = UUID.ZERO;
	/* true if your agent has Estate Manager rights on this region */
	public boolean isEstateManager;
	/*  */
	public long flags; /* Simulator.RegionFlags */
	/*  */
	public SimAccess access;
	/*  */
	public float billableFactor;
	/* The regions Unique ID */
	public UUID regionID = UUID.ZERO;

	/*
	 * The physical data center the simulator is located Known values are: Dallas,
	 * SF
	 */
	public String coLocation;
	/*
	 * The CPU Class of the simulator Most full mainland/estate sims appear to be 5,
	 * Homesteads and Openspace appear to be 501
	 */
	public int cpuClass;
	/*
	 * The number of regions sharing the same CPU as this one "Full Sims" appear to
	 * be 1, Homesteads appear to be 4
	 */
	public int cpuRatio;
	/*
	 * The billing product name Known values are: Mainland / Full Region (Sku: 023)
	 * Estate / Full Region (Sku: 024) Estate / Openspace (Sku: 027) Estate /
	 * Homestead (Sku: 029) Mainland / Homestead (Sku: 129) (Linden Owned) Mainland
	 * / Linden Homes (Sku: 131)
	 */
	public String productName;
	/*
	 * The billing product SKU Known values are: 023 Mainland / Full Region 024
	 * Estate / Full Region 027 Estate / Openspace 029 Estate / Homestead 129
	 * Mainland / Homestead (Linden Owned) 131 Linden Homes / Full Region
	 */
	public String productSku;

	/* Flags indicating which protocols this region supports */
	public long protocols;

	/* The reference to the client that this Simulator object is attached to */
	private GridClient client;
	/* The capabilities for this simulator */
	private CapsManager caps = null;

	private long handle;

	/*
	 * The current sequence number for packets sent to this simulator. Must be
	 * interlocked before modifying. Only useful for applications manipulating
	 * sequence numbers
	 */
	private AtomicInteger sequence;

	/* Sequence numbers of packets we've received (for duplicate checking) */
	private IncomingPacketIDCollection packetArchive;
	/* ACKs that are queued up to be sent to the simulator */
	private TreeSet<Integer> pendingAcks;
	/* Packets we sent out that need ACKs from the simulator */

	private TreeMap<Integer, OutgoingPacket> needAck; // int -> Packet
	/* Sequence number for pause/resume */
	private AtomicInteger pauseSerial;

	// simulator <> parcel LocalID Map
	private int[] parcelMap = new int[4096];
	private boolean downloadingParcelMap = false;

	private DatagramSocket connection;
	// The IP address and port of the server.
	private InetSocketAddress ipEndPoint;

	private String simName;

	/* A non thread-safe dictionary containing avatars in a simulator */
	private Map<Integer, Avatar> objectsAvatars = new HashMap<>();

	/* A non thread-safe dictionary containing primitives in a simulator */
	private Map<Integer, Primitive> objectsPrimitives = new HashMap<>();

	/* Coarse locations of avatars in this simulator */
	private Map<UUID, Vector3> avatarPositions = new HashMap<>();

	/* AvatarPositions key representing TrackAgent target */
	private UUID preyID = UUID.ZERO;

	// A boolean representing whether there is a working connection to the
	// simulator or not.
	private boolean connected;

	/* Used internally to track sim disconnections, do not modify this variable. */
	private boolean disconnectCandidate = false;

	private boolean trackUtilization;
	private boolean throttleOutgoingPackets;

	private BoundedLongArray inBytes;
	private BoundedLongArray outBytes;
	private Timer statsTimer;
	private Timer ackTimer;
	private Timer pingTimer;

	public SimulatorManager(GridClient client, InetAddress ip, short port, long handle) throws Exception {
		// Create an endpoint that we will be communicating with
		this(client, new InetSocketAddress(ip, port), handle);
	}

	public SimulatorManager(GridClient client, InetSocketAddress endPoint, long handle) throws Exception {
		super("Simulator: " + endPoint.getHostName());
		this.client = client;

		this.client.settings.onSettingsUpdate.add(new SettingsUpdate());
		this.trackUtilization = client.settings.getBool(LibSettings.TRACK_UTILIZATION);
		this.throttleOutgoingPackets = client.settings.getBool(LibSettings.THROTTLE_OUTGOING_PACKETS);

		this.ipEndPoint = endPoint;
		this.connection = new DatagramSocket();
		this.connected = false;
		this.disconnectCandidate = false;

		this.handle = handle;

		this.sequence = new AtomicInteger();
		this.pauseSerial = new AtomicInteger();

		this.statistics = new SimStats();
		this.inBytes = new BoundedLongArray(client.settings.STATS_QUEUE_SIZE);
		this.outBytes = new BoundedLongArray(client.settings.STATS_QUEUE_SIZE);

		// Initialize the dictionary for reliable packets waiting on ACKs from the
		// server
		this.needAck = new TreeMap<>();

		// Initialize the lists of sequence numbers we've received so far
		this.packetArchive = new IncomingPacketIDCollection(client.settings.getInt(LibSettings.PACKET_ARCHIVE_SIZE));
		this.pendingAcks = new TreeSet<>();

		if (client.settings.getBool(LibSettings.STORE_LAND_PATCHES)) {
			this.terrain = new TerrainPatch[16 * 16];
			this.windSpeeds = new Vector2[16 * 16];
		} else {
			this.terrain = null;
			this.windSpeeds = null;
		}
	}

	public GridClient getClient() {
		return client;
	}

	public long getHandle() {
		return handle;
	}

	public void setHandle(long handle) {
		this.handle = handle;
	}

	@Override
	public Map<Integer, Parcel> getParcels() {
		return parcels;
	}

	// Provides access to an internal thread-safe multidimensional array
	// containing a x,y grid mapped to each 64x64 parcel's LocalID.
	public final synchronized int[] getParcelMap() {
		return parcelMap;
	}

	public final synchronized int getParcelMap(int x, int y) {
		if (x < 0 || x >= 64 || y < 0 || y >= 64)
			throw new IllegalArgumentException(
					"Simulator.getParcelMap() parameters need to be in the range 0 - 63. x = " + x + "; y = " + y);
		return parcelMap[y * 64 + x];
	}

	public final synchronized void setParcelMap(int x, int y, int value) {
		parcelMap[y * 64 + x] = value;
	}

	public final synchronized void clearParcelMap() {
		for (int y = 0; y < 64; y++) {
			for (int x = 0; x < 64; x++) {
				parcelMap[x * 64 + y] = 0;
			}
		}
	}

	// Provides access to an internal thread-safe multidimensional array
	// containing a x,y grid mapped to each 64x64 parcel's LocalID.
	public final synchronized boolean getDownloadingParcelMap() {
		return downloadingParcelMap;
	}

	public final synchronized void setDownloadingParcelMap(boolean value) {
		downloadingParcelMap = value;
	}

	/**
	 * Checks simulator parcel map to make sure it has downloaded all data
	 * successfully
	 *
	 * @return true if map is full (contains no 0's)
	 */
	public final boolean isParcelMapFull() {
		for (int y = 0; y < 64; y++) {
			for (int x = 0; x < 64; x++) {
				if (getParcelMap(y, x) == 0) {
					return false;
				}
			}
		}
		return true;
	}

	public void setSimName(String name) {
		simName = name;
		setName(name + " (" + ipEndPoint.getHostName() + ")");
	}

	@Override
	public String getSimName() {
		return simName;
	}

	public Map<Integer, Avatar> getObjectsAvatars() {
		return objectsAvatars;
	}

	public Avatar findAvatar(UUID id) {
		return findAvatar(id, false);
	}

	public Avatar findAvatar(UUID id, boolean remove) {
		if (!UUID.isZeroOrNull(id)) {
			synchronized (objectsAvatars) {
				Iterator<Entry<Integer, Avatar>> iter = objectsAvatars.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Integer, Avatar> e = iter.next();
					if (id.equals(e.getValue().id)) {
						if (remove)
							iter.remove();
						return e.getValue();
					}
				}
			}
		}
		return null;
	}

	public Avatar findAvatar(String name) {
		if (name != null && !name.isEmpty()) {
			synchronized (objectsAvatars) {
				for (Entry<Integer, Avatar> e : objectsAvatars.entrySet()) {
					if (name.equals(e.getValue().getName())) {
						return e.getValue();
					}
				}
			}
		}
		return null;
	}

	public Map<Integer, Primitive> getObjectsPrimitives() {
		return objectsPrimitives;
	}

	public Primitive findPrimitive(UUID id, boolean remove) {
		if (!UUID.isZeroOrNull(id)) {
			synchronized (objectsPrimitives) {
				Iterator<Entry<Integer, Primitive>> iter = objectsPrimitives.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Integer, Primitive> e = iter.next();
					if (id.equals(e.getValue().id)) {
						if (remove)
							iter.remove();
						return e.getValue();
					}
				}
			}
		}
		return null;
	}

	public Map<UUID, Vector3> getAvatarPositions() {
		return avatarPositions;
	}

	public final UUID getPreyID() {
		return preyID;
	}

	public final void setPreyID(UUID id) {
		preyID = id;
	}

	public InetSocketAddress getIPEndPoint() {
		return ipEndPoint;
	}

	public boolean getConnected() {
		return connected;
	}

	public boolean getDisconnectCandidate() {
		return disconnectCandidate;
	}

	public void setDisconnectCandidate(boolean val) {
		disconnectCandidate = val;
	}

	@Override
	public int hashCode() {
		return ((Long) getHandle()).hashCode();
	}

	/**
	 * Attempt to connect to this simulator
	 *
	 * @param moveToSim
	 *            Whether to move our agent in to this sim or not
	 * @return True if the connection succeeded or connection status is unknown,
	 *         false if there was a failure
	 * @throws Exception
	 */
	public final boolean connect(boolean moveToSim) throws Exception {
		if (connected) {
			useCircuitCode();
			if (moveToSim) {
				client.agent.completeAgentMovement(this);
			}
			return true;
		}

		if (ackTimer == null) {
			ackTimer = new Timer("Simulator Acknowledge");
		}
		ackTimer.schedule(new AckTimer_Elapsed(), LibSettings.NETWORK_TICK_INTERVAL);

		// Timer for recording simulator connection statistics
		if (LibSettings.OUTPUT_TIMING_STATS && statsTimer == null) {
			statsTimer = new Timer("Simulator Statistics");
			statsTimer.scheduleAtFixedRate(new StatsTimer_Elapsed(), 1000, 1000);
		}

		// Timer for periodically pinging the simulator
		if (pingTimer == null && client.settings.SEND_PINGS) {
			pingTimer = new Timer("Simulator Pings");
			pingTimer.scheduleAtFixedRate(new PingTimer_Elapsed(), LibSettings.PING_INTERVAL,
					LibSettings.PING_INTERVAL);
		}

		logger.info(GridClient.Log("Connecting to " + ipEndPoint.toString(), client));

		// runs background thread to read from DatagramSocket
		start();

		statistics.connectTime = System.currentTimeMillis();

		logger.debug(GridClient.Log("Waiting for connection", client));
		while (true) {
			if (connected) {
				logger.debug(GridClient.Log(
						String.format("Connected! Waited %d ms", System.currentTimeMillis() - statistics.connectTime),
						client));
				break;
			} else if (System.currentTimeMillis() - statistics.connectTime > client.settings.LOGIN_TIMEOUT) {
				logger.error(GridClient.Log("Giving up on waiting for RegionHandshake for " + this.toString(), client));

				// Remove the simulator from the list, not useful if we haven't received the
				// RegionHandshake
				synchronized (client.network.getSimulators()) {
					client.network.getSimulators().remove(this);
				}
				return false;
			}
			Thread.sleep(10);
		}

		try {
			// Initiate connection
			useCircuitCode();

			// Move our agent in to the sim to complete the connection
			if (moveToSim) {
				client.agent.completeAgentMovement(this);
			}

			if (client.settings.getBool(LibSettings.SEND_AGENT_THROTTLE)) {
				client.throttle.set(this);
			}

			if (client.settings.getBool(LibSettings.SEND_AGENT_UPDATES)) {
				client.agent.sendMovementUpdate(true, this);
			}
			return true;
		} catch (Exception ex) {
			logger.error(GridClient.Log("Failed to update our status", client), ex);
		}
		return false;
	}

	/* Initiates connection to the simulator */
	public final void useCircuitCode() throws Exception {
		// Send the UseCircuitCode packet to initiate the connection
		UseCircuitCodePacket use = new UseCircuitCodePacket();
		use.CircuitCode.Code = client.network.getCircuitCode();
		use.CircuitCode.ID = client.agent.getAgentID();
		use.CircuitCode.SessionID = client.agent.getSessionID();

		// Send the initial packet out
		sendPacket(use);
	}

	public final void setSeedCaps(String seedcaps) throws InterruptedException, IOException {
		if (caps != null) {
			if (caps.getSeedCapsURI().equals(seedcaps))
				return;

			logger.warn(GridClient.Log("Unexpected change of seed capability", client));
			caps.disconnect(true);
			caps = null;
		}

		if (client.settings.getBool(LibSettings.ENABLE_CAPS)) {
			// Connect to the new CAPS system
			if (seedcaps == null || seedcaps.isEmpty()) {
				logger.error(GridClient.Log("Setting up a sim without a valid capabilities server!", client));
			} else {
				caps = new CapsManager(this, seedcaps);
			}
		}
	}

	public void disconnect(boolean sendCloseCircuit) throws Exception {
		if (connected) {
			connected = false;

			// Destroy the timers
			if (ackTimer != null) {
				ackTimer.cancel();
				ackTimer = null;
			}

			if (statsTimer != null) {
				statsTimer.cancel();
				statsTimer = null;
			}

			if (pingTimer != null) {
				pingTimer.cancel();
				pingTimer = null;
			}

			// Kill the current CAPS system
			if (caps != null) {
				caps.disconnect(true);
				caps = null;
			}

			if (sendCloseCircuit) {
				// Send the CloseCircuit notice
				CloseCircuitPacket close = new CloseCircuitPacket();

				try {
					ByteBuffer data = close.toBytes();
					connection.send(new DatagramPacket(data.array(), data.position()));
					Thread.sleep(50);
				} catch (IOException ex) {
					// There's a high probability of this failing if the network
					// is disconnected, so don't even bother logging the error
				}
			}

			try {
				// Shut the socket communication down
				connection.close();
			} catch (Exception ex) {
				logger.error(GridClient.Log(ex.toString(), client), ex);
			}
		}
	}

	/*
	 * Instructs the simulator to stop sending update (and possibly other) packets
	 */
	public final void pauseUpdates() throws Exception {
		AgentPausePacket pause = new AgentPausePacket();
		pause.AgentData.AgentID = client.agent.getAgentID();
		pause.AgentData.SessionID = client.agent.getSessionID();
		pause.AgentData.SerialNum = pauseSerial.getAndIncrement();

		sendPacket(pause);
	}

	/* Instructs the simulator to resume sending update packets (unpause) */
	public final void resumeUpdates() throws Exception {
		AgentResumePacket resume = new AgentResumePacket();
		resume.AgentData.AgentID = client.agent.getAgentID();
		resume.AgentData.SessionID = client.agent.getSessionID();
		resume.AgentData.SerialNum = pauseSerial.get();

		sendPacket(resume);
	}

	/**
	 * Retrieve the terrain height at a given coordinate
	 *
	 * @param x
	 *            Sim X coordinate, valid range is from 0 to 255
	 * @param y
	 *            Sim Y coordinate, valid range is from 0 to 255
	 * @param height
	 *            The terrain height at the given point if the lookup was
	 *            successful, otherwise 0.0f
	 * @return True if the lookup was successful, otherwise false
	 */
	public final float terrainHeightAtPoint(int x, int y) {
		if (terrain != null && x >= 0 && x < 256 && y >= 0 && y < 256) {
			int patchX = x / 16;
			int patchY = y / 16;
			x = x % 16;
			y = y % 16;

			TerrainPatch patch = terrain[patchY * 16 + patchX];
			if (patch != null) {
				return patch.data[y * 16 + x];
			}
		}
		return Float.NaN;
	}

	private final void sendPing() throws Exception {
		int oldestUnacked = 0;

		// Get the oldest NeedAck value, the first entry in the sorted dictionary
		synchronized (needAck) {
			if (!needAck.isEmpty()) {
				oldestUnacked = needAck.firstKey();
			}
		}

		// if (oldestUnacked != 0)
		// logger.debug("Sending ping with oldestUnacked=" + oldestUnacked);

		StartPingCheckPacket ping = new StartPingCheckPacket();
		ping.PingID.PingID = statistics.lastPingID++;
		ping.PingID.OldestUnacked = oldestUnacked;
		ping.getHeader().setReliable(false);
		sendPacket(ping);
		statistics.lastPingSent = System.currentTimeMillis();
	}

	public URI getCapabilityURI(String capability) {
		if (caps != null)
			return caps.capabilityURI(capability);
		return null;
	}

	public boolean getIsEventQueueRunning() {
		if (caps != null)
			return caps.isRunning();
		return false;
	}

	private void dumpBuffer(byte[] byteBuffer, int numBytes, String head) {
		logger.debug(GridClient.Log(head + numBytes, client));
		StringBuffer dump = new StringBuffer(numBytes * 2);
		for (int i = 0; i < numBytes; i++) {
			byte value = byteBuffer[i];
			dump.append(Integer.toHexString(value & 0xFF));
			dump.append(" ");
		}
		logger.debug(GridClient.Log(dump.toString(), client));

	}

	@Override
	public void run() {
		try {
			connection.connect(ipEndPoint);
		} catch (SocketException e) {
			logger.error(GridClient.Log("Failed to startup the UDP socket", client));
			return;
		}
		byte[] recvBuffer = new byte[4096];
		DatagramPacket p = new DatagramPacket(recvBuffer, recvBuffer.length);
		boolean logRawPackets = client.settings.getBool(LibSettings.LOG_RAW_PACKET_BYTES);
		connected = true;

		while (true) {
			try {
				connection.receive(p);
				int numBytes = p.getLength();
				Packet packet = null;

				// Update the disconnect flag so this sim doesn't time out
				disconnectCandidate = false;

				synchronized (recvBuffer) {
					byte[] byteBuffer = recvBuffer;

					// Retrieve the incoming packet
					try {
						if (logRawPackets) {
							dumpBuffer(byteBuffer, numBytes, "<=============== Received packet, length = ");
						}

						if ((recvBuffer[0] & PacketHeader.MSG_ZEROCODED) != 0) {
							int bodylen = numBytes;
							if ((recvBuffer[0] & PacketHeader.MSG_APPENDED_ACKS) != 0) {
								bodylen -= (recvBuffer[numBytes - 1] * 4 + 1);
							}
							byteBuffer = new byte[numBytes <= 1000 ? 4000 : numBytes * 4];
							numBytes = zeroDecode(recvBuffer, numBytes, bodylen, byteBuffer);
							if (logRawPackets) {
								dumpBuffer(byteBuffer, numBytes, "<==========Zero-Decoded packet, length=");
							}
						}

						packet = Packet.buildPacket(ByteBuffer.wrap(byteBuffer, 0, numBytes));
						if (logRawPackets) {
							logger.debug(GridClient.Log("Decoded packet " + packet.getClass().getName(), client));
						}
					} catch (IOException ex) {
						logger.info(GridClient.Log(
								ipEndPoint.toString() + " socket is closed, shutting down " + getName(), client), ex);

						connected = false;
						client.network.disconnectSim(this, true);
						return;
					} catch (BufferUnderflowException ex) {
						dumpBuffer(byteBuffer, numBytes, "<=========== Buffer Underflow in packet, length = ");
					}
				}

				if (packet == null) {
					// TODO:FIXME
					// This used to be a warning
					dumpBuffer(recvBuffer, numBytes,
							"<=========== Couldn't build a message from the incoming data, length = ");
					continue;
				}

				statistics.recvBytes += numBytes;
				statistics.recvPackets++;

				if (packet.getHeader().getResent()) {
					statistics.receivedResends++;
				}

				// Handle appended ACKs
				if (packet.getHeader().getAppendedAcks() && packet.getHeader().ackList != null) {
					synchronized (needAck) {
						for (int ack : packet.getHeader().ackList) {
							if (needAck.remove(ack) == null) {
								logger.warn(GridClient
										.Log(String.format("Appended ACK for a packet (%d) we didn't send: %s", ack,
												packet.getClass().getName()), client));
							}
						}
					}
				}
				// Handle PacketAck packets
				if (packet.getType() == PacketType.PacketAck) {
					PacketAckPacket ackPacket = (PacketAckPacket) packet;

					synchronized (needAck) {
						for (int ID : ackPacket.ID) {
							if (needAck.remove(ID) == null) {
								logger.warn(GridClient.Log(String.format("ACK for a packet (%d) we didn't send: %s", ID,
										packet.getClass().getName()), client));
							}
						}
					}
				}

				// Add this packet to the list of ACKs that need to be sent out
				int sequence = packet.getHeader().getSequence();
				synchronized (pendingAcks) {
					pendingAcks.add(sequence);
				}

				// Send out ACKs if we have a lot of them
				if (pendingAcks.size() >= client.settings.MAX_PENDING_ACKS) {
					sendPendingAcks();
				}

				/*
				 * Track the sequence number for this packet if it's marked as reliable
				 */
				if (packet.getHeader().getReliable() && !packetArchive.tryEnqueue(sequence)) {
					if (packet.getHeader().getResent()) {
						logger.debug(GridClient.Log(
								String.format("Received a resend of already processed packet #%d, type: %s, from %s",
										sequence, packet.getType(), getName()),
								client));
					} else {
						logger.warn(GridClient.Log(String.format(
								"Received a duplicate (not marked as resend) of packet #%d, type: %s for %s from %s",
								sequence, packet.getType(), client.agent.getName(), getName()), client));
					}
					// Avoid firing a callback twice for the same packet
					continue;
				}

				// Let the network manager distribute the packet to the callbacks
				client.network.distributePacket(this, packet);

				if (trackUtilization) {
					client.stats.updateNetStats(packet.getType().toString(), Type.Packet, 0, numBytes);
				}
			} catch (IOException ex) {
				logger.info(GridClient.Log(ipEndPoint.toString() + " socket is closed, shutting down " + getName(),
						client));

				connected = false;
				return;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void sendPacket(Packet packet) throws Exception {
		if (packet.hasVariableBlocks && packet.getLength() > Packet.MTU) {
			ByteBuffer[] datas;
			try {
				datas = packet.toBytesMultiple();
			} catch (NullPointerException ex) {
				logger.error("Failed to serialize " + packet.getType()
						+ " packet to one or more payloads due to a missing block or field. StackTrace: "
						+ ex.getStackTrace());
				return;
			}
			int packetCount = datas.length;

			if (packetCount > 1) {
				logger.debug("Split " + packet.getType() + " packet into " + packetCount + " packets");
			}

			for (int i = 0; i < packetCount; i++) {
				sendPacketData(datas[i], packet.getType(), packet.getHeader().getZerocoded());
			}
		} else {
			ByteBuffer data = packet.toBytes();
			sendPacketData(data, packet.getType(), packet.getHeader().getZerocoded());
		}
	}

	private final void sendPacketData(ByteBuffer data, PacketType type, boolean doZerocode)
			throws InterruptedException {
		// Zerocode if needed
		if (doZerocode) {
			byte[] zeroBuffer = new byte[2000];
			int bytes = PacketHeader.zeroEncode(data, zeroBuffer);
			if (bytes <= data.capacity()) {
				data = ByteBuffer.wrap(zeroBuffer, 0, bytes);
				data.order(ByteOrder.LITTLE_ENDIAN);
			} else {
				// Zero encoding actually grew the buffer beyond the original size
				data.put(0, (byte) (data.get(0) & PacketHeader.MSG_ZEROCODED));
				data.position(0);
			}
		}

		OutgoingPacket outgoingPacket = new OutgoingPacket(this, type, data);

		// Send ACK and logout packets directly, everything else goes through
		// the queue
		if (!throttleOutgoingPackets || type == PacketType.PacketAck || type == PacketType.LogoutRequest) {
			sendPacketFinal(outgoingPacket);
		} else {
			client.network.queuePacket(outgoingPacket);
		}

		if (trackUtilization) {
			client.stats.updateNetStats(type.toString(), Type.Packet, data.capacity(), 0);
		}
	}

	/* Sends out pending acknowledgements */
	private void sendPendingAcks() {
		synchronized (pendingAcks) {
			if (pendingAcks.size() > 0) {
				PacketAckPacket acks = new PacketAckPacket();
				acks.ID = new int[pendingAcks.size()];
				acks.getHeader().setReliable(false);
				int i = 0;
				Iterator<Integer> iter = pendingAcks.iterator();
				while (iter.hasNext()) {
					acks.ID[i++] = iter.next();
				}
				pendingAcks.clear();

				try {
					sendPacket(acks);
				} catch (Exception ex) {
					logger.error(GridClient.Log("Exception when sending Ack packet", client), ex);
				}
			}
		}
	}

	/**
	 * Resend unacknowledged packets
	 */
	private void resendUnacked() {
		if (needAck.size() > 0) {
			List<OutgoingPacket> array;

			synchronized (needAck) {
				// Create a temporary copy of the outgoing packets array to iterate over
				array = new ArrayList<>(needAck.size());
				array.addAll(needAck.values());
			}

			long now = System.currentTimeMillis();

			// Resend packets
			for (int i = 0; i < array.size(); i++) {
				OutgoingPacket outgoing = array.get(i);

				if (outgoing.tickCount != 0 && now - outgoing.tickCount > client.settings.RESEND_TIMEOUT) {
					if (outgoing.resendCount < client.settings.MAX_RESEND_COUNT) {
						if (client.settings.LOG_RESENDS) {
							logger.debug(GridClient.Log(String.format("Resending %s packet #%d, %d ms have passed",
									outgoing.type, outgoing.sequenceNumber, now - outgoing.tickCount), client));
						}

						// The TickCount will be set to the current time when
						// the packet is actually sent out again
						outgoing.tickCount = 0;

						// Set the resent flag
						outgoing.buffer.array()[0] |= PacketHeader.MSG_RESENT;

						// Stats tracking
						outgoing.resendCount++;
						statistics.resentPackets++;

						sendPacketFinal(outgoing);
					} else {
						logger.debug(String.format("Dropping packet #%d after %d failed attempts",
								outgoing.sequenceNumber, outgoing.resendCount, client));

						synchronized (needAck) {
							needAck.remove(outgoing.sequenceNumber);
						}
					}
				}
			}
		}
	}

	public final void sendPacketFinal(OutgoingPacket outgoingPacket) {
		ByteBuffer buffer = outgoingPacket.buffer;
		byte[] bytes = buffer.array();
		byte flags = buffer.get(0);
		boolean isResend = (flags & PacketHeader.MSG_RESENT) != 0;
		boolean isReliable = (flags & PacketHeader.MSG_RELIABLE) != 0;

		// Keep track of when this packet was sent out (right now)
		outgoingPacket.tickCount = System.currentTimeMillis();

		int dataLength = buffer.limit();

		// Keep appending ACKs until there is no room left in the packet or
		// there are no more ACKs to append
		int ackCount = 0;
		synchronized (pendingAcks) {
			while (dataLength + 5 < buffer.capacity() && !pendingAcks.isEmpty()) {
				dataLength += Helpers.uint32ToBytesB(pendingAcks.pollFirst(), bytes, dataLength);
				++ackCount;
			}
		}

		if (ackCount > 0) {
			// Set the last byte of the packet equal to the number of appended
			// ACKs
			bytes[dataLength++] = (byte) ackCount;
			// Set the appended ACKs flag on this packet
			bytes[0] = (byte) (flags | PacketHeader.MSG_APPENDED_ACKS);
			// Increase the byte buffer limit to the new length
			buffer.limit(dataLength);
		}

		if (!isResend) {
			// Not a resend, assign a new sequence number
			outgoingPacket.sequenceNumber = sequence.incrementAndGet();
			Helpers.uint32ToBytesB(outgoingPacket.sequenceNumber, bytes, 1);

			if (isReliable) {
				// Add this packet to the list of ACK responses we are waiting
				// on from the server
				synchronized (needAck) {
					needAck.put(outgoingPacket.sequenceNumber, outgoingPacket);
				}
			}
		}

		try {
			connection.send(new DatagramPacket(buffer.array(), dataLength));
		} catch (IOException ex) {
			logger.error(GridClient.Log(ex.toString(), client), ex);
		}

		// Stats tracking
		statistics.sentBytes += dataLength;
		statistics.sentPackets++;
		client.network.raisePacketSentCallback(buffer.array(), dataLength, this);
	}

	/* #region timer callbacks */

	/**
	 * Decode a zerocoded byte array, used to decompress packets marked with the
	 * zerocoded flag Any time a zero is encountered, the next byte is a count of
	 * how many zeroes to expand. One zero is encoded with 0x00 0x01, two zeroes is
	 * 0x00 0x02, three zeroes is 0x00 0x03, etc. The first six bytes puls and extra
	 * bytes are copied directly to the output buffer.
	 *
	 * @param src
	 *            The byte array to decode
	 * @param srclen
	 *            The length of the input byte array
	 * @param bodylen
	 *            The length of the byte array to decode
	 * @param dest
	 *            The output byte array to decode to
	 * @return The length of the output buffer
	 * @throws Exception
	 */
	private static int zeroDecode(byte[] src, int srclen, int bodylen, byte[] dest) throws Exception {
		int i;
		int destlen = 6 + src[5];

		/* Copy the first 6 + extra header bytes as they are never compressed */
		System.arraycopy(src, 0, dest, 0, destlen);
		for (i = destlen; i < bodylen; i++) {
			if (src[i] == 0x00) {
				for (byte j = 0; j < src[i + 1]; j++) {
					dest[destlen++] = 0x00;
				}
				i++;
			} else {
				dest[destlen++] = src[i];
			}
		}

		if (srclen > bodylen) {
			/* Copy the appended ack data as they are never compressed */
			System.arraycopy(src, bodylen, dest, destlen, srclen - bodylen);
			destlen += srclen - bodylen;
		}
		return destlen;
	}
}
