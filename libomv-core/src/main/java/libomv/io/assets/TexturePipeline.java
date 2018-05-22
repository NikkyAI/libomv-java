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
package libomv.io.assets;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import libomv.imaging.ManagedImage.ImageCodec;
import libomv.io.GridClient;
import libomv.io.LibSettings;
import libomv.io.SimulatorManager;
import libomv.model.Simulator;
import libomv.model.asset.DelayedTransfer;
import libomv.model.asset.ImageDownload;
import libomv.model.asset.ImageType;
import libomv.model.asset.StatusCode;
import libomv.model.login.LoginProgressCallbackArgs;
import libomv.model.login.LoginStatus;
import libomv.model.network.DisconnectedCallbackArgs;
import libomv.model.texture.TextureRequestState;
import libomv.packets.ImageDataPacket;
import libomv.packets.ImageNotInDatabasePacket;
import libomv.packets.ImagePacketPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.RequestImagePacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.TimeoutEvent;

/**
 * Texture request download handler, allows a configurable number of download
 * slots which manage multiple concurrent texture downloads from the
 * {@link SimulatorManager}
 *
 * This class makes full use of the internal {@link TextureCache} system for
 * full texture downloads.
 */
public class TexturePipeline implements PacketCallback {
	private static final Logger logger = Logger.getLogger(TexturePipeline.class);

	// A request task containing information and status of a request as it is
	// processed through the <see cref="TexturePipeline"/>
	private class TaskInfo {
		// The slot this request is occupying in the threadpoolSlots array
		public int requestSlot;

		public SortedMap<Short, Short> packetsSeen;

		public Simulator simulator;
		// The timeout event used for this task
		public TimeoutEvent<Boolean> timeoutEvent;
		// An object that maintains the data of an request thats in-process.
		public ImageDownload request;
	}

	// #if DEBUG_TIMING // Timing globals
	// The combined time it has taken for all textures requested sofar. This
	// includes the amount of
	// time the texture spent waiting for a download slot, and the time spent
	// retrieving the actual
	// texture from the Grid
	public static long totalTime;
	// The amount of time the request spent in the <see
	// cref="TextureRequestState.Progress"/> state
	public static long networkTime;
	// The total number of bytes transferred since the TexturePipeline was started
	public static int totalBytes;

	// #endif

	// A dictionary containing all pending and in-process transfer requests
	// where the Key is both the RequestID
	// and also the Asset Texture ID, and the value is an object containing the
	// current state of the request and also
	// the asset data as it is being re-assembled
	private final Map<UUID, TaskInfo> texTransfers;
	// Holds the reference to the <see cref="GridClient"/> client object
	private final GridClient client;

	private final AssetCache cache;

	private final ExecutorService threadPool;
	// An array of worker slots which shows the availablity status of the slot
	private final Future<?>[] threadRequests;

	// The primary thread which manages the requests.
	private Thread downloadMaster;
	// true if the TexturePipeline is currently running
	private boolean running;
	// A refresh timer used to increase the priority of stalled requests
	private Timer refreshDownloadsTimer;

	/**
	 * Default constructor, Instantiates a new copy of the TexturePipeline class
	 *
	 * @param client
	 *            Reference to the instantiated <see cref="GridClient"/> object
	 */
	public TexturePipeline(GridClient client, AssetCache cache) {
		this.client = client;
		this.cache = cache;

		int maxDownloads = client.settings.MAX_CONCURRENT_TEXTURE_DOWNLOADS;

		this.threadPool = Executors.newFixedThreadPool(maxDownloads);
		this.threadRequests = new Future[maxDownloads];

		this.texTransfers = new HashMap<>();

		// Handle client connected and disconnected events
		client.login.onLoginProgress.add(new Network_LoginProgress());
		client.network.onDisconnected.add(new Network_Disconnected());
	}

	// Current number of pending and in-process transfers
	public final int getTransferCount() {
		return texTransfers.size();
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case ImageData:
			handleImageData(packet, simulator);
			break;
		case ImagePacket:
			handleImagePacket(packet, simulator);
			break;
		case ImageNotInDatabase:
			handleImageNotInDatabase(packet, simulator);
			break;
		default:
			break;
		}
	}

	private class Network_LoginProgress implements Callback<LoginProgressCallbackArgs> {
		@Override
		public boolean callback(LoginProgressCallbackArgs e) {
			if (e.getStatus() == LoginStatus.Success) {
				startup();
			}
			return false;
		}
	}

	private class Network_Disconnected implements Callback<DisconnectedCallbackArgs> {
		@Override
		public boolean callback(DisconnectedCallbackArgs e) {
			shutdown();
			return false;
		}
	}

	/**
	 * Initialize callbacks required for the TexturePipeline to operate
	 *
	 */
	public final void startup() {
		if (running) {
			return;
		}

		if (downloadMaster == null) {
			// Instantiate master thread that manages the request pool
			downloadMaster = new Thread(new DownloadThread());
			downloadMaster.setName("TexturePipeline");
			downloadMaster.setDaemon(true);
		}
		running = true;

		client.network.registerCallback(PacketType.ImageData, this);
		client.network.registerCallback(PacketType.ImagePacket, this);
		client.network.registerCallback(PacketType.ImageNotInDatabase, this);
		downloadMaster.start();

		if (refreshDownloadsTimer == null) {
			refreshDownloadsTimer = new Timer();
			refreshDownloadsTimer.schedule(new RefreshDownloadsTimer_Elapsed(), LibSettings.PIPELINE_REFRESH_INTERVAL,
					LibSettings.PIPELINE_REFRESH_INTERVAL);
		}
	}

	/**
	 * Shutdown the TexturePipeline and cleanup any callbacks or transfers
	 */
	public final void shutdown() {
		if (!running) {
			return;
		}
		// #if DEBUG_TIMING

		logger.debug(GridClient.Log(
				String.format("Combined Execution Time: %d, Network Execution Time %d, Network %d k/sec, Image Size %d",
						totalTime, networkTime, getNetworkThroughput(totalBytes, networkTime), totalBytes),
				client));
		// #endif
		if (null != refreshDownloadsTimer) {
			refreshDownloadsTimer.cancel();
		}
		refreshDownloadsTimer = null;

		if (downloadMaster != null && downloadMaster.isAlive()) {
			running = false;
		}
		downloadMaster = null;

		client.network.unregisterCallback(PacketType.ImageNotInDatabase, this);
		client.network.unregisterCallback(PacketType.ImageData, this);
		client.network.unregisterCallback(PacketType.ImagePacket, this);

		synchronized (texTransfers) {
			texTransfers.clear();
		}

		synchronized (threadRequests) {
			for (int i = 0; i < threadRequests.length; i++) {
				if (threadRequests[i] != null) {
					threadRequests[i].cancel(true);
					threadRequests[i] = null;
				}
			}
		}
	}

	private class RefreshDownloadsTimer_Elapsed extends TimerTask {
		@Override
		public void run() {
			synchronized (texTransfers) {
				for (TaskInfo task : texTransfers.values()) {
					if (task.request.state.equals(TextureRequestState.Progress)) {
						// Find the first missing packet in the download
						short packet = 0;
						synchronized (task) {
							if (task.packetsSeen != null && task.packetsSeen.size() > 0) {
								packet = getFirstMissingPacket(task.packetsSeen);
							}
						}

						if (task.request.timeSinceLastPacket > 5000) {
							// We're not receiving data for this texture fast
							// enough, bump up the priority by 5%
							task.request.priority *= 1.05f;
							task.request.timeSinceLastPacket = 0;
							try {
								requestImage(task.request.itemID, task.request.imageType, task.request.priority,
										task.request.discardLevel, packet);
							} catch (Exception e) {
							}
						}

						if (task.request.timeSinceLastPacket > client.settings.PIPELINE_REQUEST_TIMEOUT) {
							task.timeoutEvent.set(true);
						}
					}
				}
			}
		}
	}

	/**
	 * Request a texture asset from the simulator using the
	 * <see cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 *
	 * @param textureID
	 *            The <see cref="UUID"/> of the texture asset to download
	 * @param imageType
	 *            The <see cref="ImageType"/> of the texture asset. Use
	 *            <see cref="ImageType.Normal"/> for most textures, or
	 *            <see cref="ImageType.Baked"/> for baked layer texture assets
	 * @param priority
	 *            A float indicating the requested priority for the transfer. Higher
	 *            priority values tell the simulator to prioritize the request
	 *            before lower valued requests. An image already being transferred
	 *            using the <see cref="TexturePipeline"/> can have its priority
	 *            changed by resending the request with the new priority value
	 * @param discardLevel
	 *            Number of quality layers to discard. This controls the end marker
	 *            of the data sent
	 * @param packetStart
	 *            The packet number to begin the request at. A value of 0 begins the
	 *            request from the start of the asset texture
	 * @param callback
	 *            The <see cref="TextureDownloadCallback"/> callback to fire when
	 *            the image is retrieved. The callback will contain the result of
	 *            the request and the texture asset data
	 * @param progressive
	 *            If true, the callback will be fired for each chunk of the
	 *            downloaded image. The callback asset parameter will contain all
	 *            previously received chunks of the texture asset starting from the
	 *            beginning of the request
	 */
	public boolean requestTexture(ImageDownload request) {
		TaskInfo task = getTransferValue(request.itemID);
		if (task == null) {
			request.state = TextureRequestState.Pending;
			task = new TaskInfo();
			task.timeoutEvent = new TimeoutEvent<>();
			task.requestSlot = -1;
			task.request = request;

			synchronized (texTransfers) {
				texTransfers.put(request.itemID, task);
			}
		}
		return true;
	}

	/**
	 * Sends the actual request packet to the simulator
	 *
	 * @Note Sending a priority of 0 and a discardlevel of -1 aborts download
	 *
	 * @param imageID
	 *            The image to download
	 * @param type
	 *            Type of the image to download, either a baked avatar texture or a
	 *            normal texture
	 * @param priority
	 *            Priority level of the download. Default is <c>1,013,000.0f</c>
	 * @param discardLevel
	 *            Number of quality layers to discard. This controls the end marker
	 *            of the data sent
	 * @param packetNum
	 *            Packet number to start the download at. This controls the start
	 *            marker of the data sent
	 * @throws Exception
	 */
	private void requestImage(UUID imageID, ImageType type, float priority, int discardLevel, int packetNum)
			throws Exception {
		// Priority == 0 && DiscardLevel == -1 means cancel the transfer
		if (priority == 0 && discardLevel == -1) {
			abortTextureRequest(imageID);
		} else {
			TaskInfo task = getTransferValue(imageID);
			if (task != null) {
				if (task.simulator != null) {
					// Already downloading, just updating the priority
					float percentComplete = ((float) task.request.transferred / (float) task.request.size) * 100f;
					if (Float.isNaN(percentComplete)) {
						percentComplete = 0f;
					}

					if (percentComplete > 0f) {
						logger.debug(String.format("Updating priority on image transfer %s to %d, %d% complete",
								imageID.toString(), task.request.priority, Math.round(percentComplete)));
					}
				} else {
					task.simulator = client.network.getCurrentSim();
				}

				// Build and send the request packet
				RequestImagePacket request = new RequestImagePacket();
				request.AgentData.AgentID = client.agent.getAgentID();
				request.AgentData.SessionID = client.agent.getSessionID();
				request.RequestImage = new RequestImagePacket.RequestImageBlock[1];
				request.RequestImage[0] = request.new RequestImageBlock();
				request.RequestImage[0].DiscardLevel = (byte) discardLevel;
				request.RequestImage[0].DownloadPriority = priority;
				request.RequestImage[0].Packet = packetNum;
				request.RequestImage[0].Image = imageID;
				request.RequestImage[0].Type = type.getValue();

				client.network.sendPacket(request);
			} else {
				logger.warn(
						"Received texture download request for a texture that isn't in the download queue: " + imageID);
			}
		}
	}

	/**
	 * Cancel a pending or in process texture request
	 *
	 * @param textureID
	 *            The texture assets unique ID
	 * @throws Exception
	 */
	public final void abortTextureRequest(UUID textureID) throws Exception {
		TaskInfo task = getTransferValue(textureID);
		if (task != null) {
			// this means we've actually got the request assigned to the threadpool
			if (task.request.state == TextureRequestState.Progress) {
				RequestImagePacket request = new RequestImagePacket();
				request.AgentData.AgentID = client.agent.getAgentID();
				request.AgentData.SessionID = client.agent.getSessionID();
				request.RequestImage = new RequestImagePacket.RequestImageBlock[1];
				request.RequestImage[0] = request.new RequestImageBlock();
				request.RequestImage[0].DiscardLevel = -1;
				request.RequestImage[0].DownloadPriority = 0;
				request.RequestImage[0].Packet = 0;
				request.RequestImage[0].Image = textureID;
				request.RequestImage[0].Type = task.request.imageType.getValue();
				client.network.sendPacket(request);

				removeTransfer(textureID);

				task.timeoutEvent.set(false);
				task.request.state = TextureRequestState.Aborted;
				task.request.callbacks.dispatch(task.request);
				client.assets.fireImageProgressEvent(task.request.itemID, task.request.transferred, task.request.size);
			} else {
				removeTransfer(textureID);

				task.request.state = TextureRequestState.Aborted;
				task.request.callbacks.dispatch(task.request);
				client.assets.fireImageProgressEvent(task.request.itemID, task.request.transferred, task.request.size);
			}
		}
	}

	/**
	 * Master Download Thread, Queues up downloads in the threadpool
	 */
	private class DownloadThread implements Runnable {
		@Override
		public void run() {
			while (running) {
				// find free slots
				int active = 0;
				int slot;

				TaskInfo nextTask = null;

				synchronized (texTransfers) {
					for (TaskInfo request : texTransfers.values()) {
						if (request.request.state == TextureRequestState.Pending) {
							nextTask = request;
						} else if (request.request.state == TextureRequestState.Started
								|| request.request.state == TextureRequestState.Progress) {
							++active;
						}
					}
				}

				if (nextTask != null && active <= threadRequests.length) {
					slot = -1;
					// find available slot for reset event
					synchronized (threadRequests) {
						for (int i = 0; i < threadRequests.length; i++) {
							if (threadRequests[i] == null) {
								// found a free slot
								slot = i;
								break;
							}
						}
					}

					// -1 = slot not available
					if (slot != -1) {
						nextTask.request.state = TextureRequestState.Started;
						nextTask.requestSlot = slot;

						logger.debug(String.format("Sending Worker thread new download request %d", slot));
						Future<?> request = threadPool.submit(new TextureRequestDoWork(nextTask));
						synchronized (threadRequests) {
							threadRequests[slot] = request;
						}
						continue;
					}
				}
				// Queue was empty or all download slots are in use, let's give
				// up some CPU time
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}
			logger.info(GridClient.Log("Texture pipeline download thread shutting down", client));
		}
	}

	/**
	 * The worker thread that sends the request and handles timeouts
	 *
	 * @param threadContext
	 *            A <see cref="TaskInfo"/> object containing the request details
	 */
	private class TextureRequestDoWork implements Runnable {
		private final TaskInfo task;

		public TextureRequestDoWork(TaskInfo task) {
			this.task = task;
		}

		@Override
		public void run() {

			task.request.state = TextureRequestState.Progress;
			// Find the first missing packet in the download
			short packet = 0;
			synchronized (task.request) {
				if (task.packetsSeen != null && task.packetsSeen.size() > 0) {
					packet = getFirstMissingPacket(task.packetsSeen);
				}
			}

			// Request the texture
			try {
				requestImage(task.request.itemID, task.request.imageType, task.request.priority,
						task.request.discardLevel, packet);
			} catch (Exception e) {
			}

			// Set starting time
			task.request.timeSinceLastPacket = 0;

			// Don't release this worker slot until texture is downloaded or
			// timeout occurs
			Boolean timeout;
			try {
				timeout = task.timeoutEvent.waitOne(-1);
				if (timeout == null || !timeout) {
					// Timed out
					logger.warn("Worker " + task.requestSlot + " timeout waiting for texture " + task.request.itemID
							+ " to download got " + task.request.transferred + " of " + task.request.size);

					removeTransfer(task.request.itemID);

					task.request.state = TextureRequestState.Timeout;
					task.request.callbacks.dispatch(task.request);
					client.assets.fireImageProgressEvent(task.request.itemID, task.request.transferred,
							task.request.size);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Free up this download slot
			synchronized (threadRequests) {
				threadRequests[task.requestSlot] = null;
			}
		}
	}

	private short getFirstMissingPacket(SortedMap<Short, Short> packetsSeen) {
		short packet = 0;

		synchronized (packetsSeen) {
			boolean first = true;
			for (short packetSeen : packetsSeen.values()) {
				if (first) {
					// Initially set this to the earliest packet received in the
					// transfer
					packet = packetSeen;
					first = false;
				} else {
					++packet;

					// If there is a missing packet in the list, break and
					// request the download
					// resume here
					if (packetSeen != packet) {
						--packet;
						break;
					}
				}
			}
			++packet;
		}
		return packet;
	}

	/**
	 * Handle responses from the simulator that tell us a texture we have requested
	 * is unable to be located or no longer exists. This will remove the request
	 * from the pipeline and free up a slot if one is in use
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void handleImageNotInDatabase(Packet packet, Simulator simulator) {
		ImageNotInDatabasePacket imageNotFoundData = (ImageNotInDatabasePacket) packet;
		TaskInfo task = getTransferValue(imageNotFoundData.ID);
		if (task != null) {
			// cancel active request and free up the threadpool slot
			if (task.request.state.equals(TextureRequestState.Progress)) {
				task.timeoutEvent.set(true);
			}

			removeTransfer(imageNotFoundData.ID);

			// fire callback to inform the caller
			task.request.state = TextureRequestState.NotFound;
			task.request.callbacks.dispatch(task.request);
			task.timeoutEvent.set(true);
		} else {
			logger.warn("Received an ImageNotFound packet for an image we did not request: " + imageNotFoundData.ID);
		}
	}

	private boolean processDelayedData(ImageDownload download, DelayedTransfer data) {
		while (data != null) {
			System.arraycopy(data.data, 0, download.assetData, download.transferred, data.data.length);
			download.transferred += data.data.length;
			if (data.status == StatusCode.OK && download.transferred >= download.size) {
				download.state = TextureRequestState.Finished;
			} else if (data.status == StatusCode.Error) {
				download.state = TextureRequestState.Aborted;
			}

			if (download.state != TextureRequestState.Progress) {
				synchronized (texTransfers) {
					texTransfers.remove(download.itemID);
				}
				download.delayed.clear();

				download.success = download.state == TextureRequestState.Finished;
				if (download.success) {
					logger.debug(
							GridClient.Log("Transfer for asset " + download.itemID.toString() + " completed", client));

					// Cache successful asset download
					cache.put(download.itemID, download.assetData, download.suffix);
				} else {
					logger.warn(GridClient.Log("Transfer failed with status code " + download.state, client));
				}

				download.callbacks.dispatch(download);
				return true;
			}

			download.packetNum++;
			data = download.delayed.remove(download.packetNum);
		}

		if (download.reportProgress) {
			download.callbacks.dispatch(download);
			client.assets.fireImageProgressEvent(download.itemID, download.transferred, download.size);
		}
		return false;
	}

	/**
	 * Handle the initial ImageDataPacket sent from the simulator
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void handleImageData(Packet packet, Simulator simulator) {
		ImageDataPacket data = (ImageDataPacket) packet;
		TaskInfo task = getTransferValue(data.ImageID.ID);
		if (task == null) {
			logger.warn(GridClient.Log(
					"Received a ImageData packet for a texture we didn't request, Image ID: " + data.ImageID.ID,
					client));
			return;
		}

		// reset the timeout interval since we got data
		task.request.timeSinceLastPacket = 0;

		if (task.request.size == 0) {
			task.request.codec = ImageCodec.setValue(data.ImageID.Codec);
			// task.Request.PacketCount = data.ImageID.Packets;
			task.request.size = data.ImageID.Size;
			task.request.assetData = new byte[task.request.size];

			processDelayedData(task.request, new DelayedTransfer(StatusCode.OK, data.ImageData.getData()));
		}
	}

	/**
	 * Handles the remaining Image data that did not fit in the initial ImageData
	 * packet
	 *
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws InterruptedException
	 */
	private final void handleImagePacket(Packet packet, Simulator simulator) throws InterruptedException {
		ImagePacketPacket image = (ImagePacketPacket) packet;
		TaskInfo task = getTransferValue(image.ImageID.ID);
		if (task != null) {
			StatusCode status = StatusCode.OK;
			DelayedTransfer info = new DelayedTransfer(status, image.ImageData.getData());

			if (!task.request.gotInfo() || image.ImageID.Packet != task.request.packetNum) {
				/*
				 * We haven't received the header yet, or the packet number is higher than the
				 * currently expected packet. Put it in the out of order hashlist
				 */
				task.request.delayed.put((int) image.ImageID.Packet, info);
			} else {
				processDelayedData(task.request, info);
			}
			task.request.timeSinceLastPacket = 0;
		}
	}

	private int getNetworkThroughput(long bytes, long duration) {
		return duration != 0 ? Math.round(bytes / duration) : 0;
	}

	private TaskInfo getTransferValue(UUID textureID) {
		synchronized (texTransfers) {
			return texTransfers.get(textureID);
		}
	}

	private boolean removeTransfer(UUID textureID) {
		synchronized (texTransfers) {
			return texTransfers.remove(textureID) != null;
		}
	}
}
