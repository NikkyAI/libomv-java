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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import libomv.VisualParams;
import libomv.VisualParams.VisualAlphaParam;
import libomv.VisualParams.VisualParam;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDMap;
import libomv.assets.AssetTexture;
import libomv.assets.AssetWearable;
import libomv.assets.AssetWearable.AvatarTextureIndex;
import libomv.assets.AssetWearable.WearableType;
import libomv.inventory.InventoryAttachment;
import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryFolder.FolderType;
import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryNode;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.inventory.InventoryObject;
import libomv.inventory.InventoryWearable;
import libomv.io.assets.AssetManager;
import libomv.io.capabilities.CapsClient;
import libomv.io.imaging.Baker;
import libomv.model.Simulator;
import libomv.model.appearance.AgentCachedBakesReplyCallbackArgs;
import libomv.model.appearance.AgentWearablesReplyCallbackArgs;
import libomv.model.appearance.AppearanceSetCallbackArgs;
import libomv.model.appearance.BakeType;
import libomv.model.appearance.ColorParamInfo;
import libomv.model.appearance.RebakeAvatarTexturesCallbackArgs;
import libomv.model.appearance.TextureData;
import libomv.model.appearance.WearableData;
import libomv.model.asset.AssetDownload;
import libomv.model.asset.AssetType;
import libomv.model.asset.BakedTextureUploadedCallback;
import libomv.model.asset.ImageDownload;
import libomv.model.inventory.InventorySortOrder;
import libomv.model.network.DisconnectedCallbackArgs;
import libomv.model.network.EventQueueRunningCallbackArgs;
import libomv.model.simulator.RegionProtocols;
import libomv.model.texture.TextureRequestState;
import libomv.packets.AgentCachedTexturePacket;
import libomv.packets.AgentCachedTexturePacket.WearableDataBlock;
import libomv.packets.AgentCachedTextureResponsePacket;
import libomv.packets.AgentIsNowWearingPacket;
import libomv.packets.AgentSetAppearancePacket;
import libomv.packets.AgentWearablesRequestPacket;
import libomv.packets.AgentWearablesUpdatePacket;
import libomv.packets.DetachAttachmentIntoInvPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.RebakeAvatarTexturesPacket;
import libomv.packets.RezMultipleAttachmentsFromInvPacket;
import libomv.packets.RezSingleAttachmentFromInvPacket;
import libomv.primitives.Avatar;
import libomv.primitives.Primitive.AttachmentPoint;
import libomv.primitives.TextureEntry;
import libomv.types.Color4;
import libomv.types.PacketCallback;
import libomv.types.Permissions;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.MultiMap;
import libomv.utils.Settings.SettingsUpdateCallbackArgs;
import libomv.utils.TimeoutEvent;

public class AppearanceManager implements PacketCallback {
	private static final Logger logger = Logger.getLogger(AppearanceManager.class);

	private class SettingsUpdate implements Callback<SettingsUpdateCallbackArgs> {
		@Override
		public boolean callback(SettingsUpdateCallbackArgs params) {
			String key = params.getName();
			if (key == null) {
				sendAppearanceUpdates = client.settings.getBool(LibSettings.SEND_AGENT_APPEARANCE);
			} else if (key.equals(LibSettings.SEND_AGENT_APPEARANCE)) {
				sendAppearanceUpdates = params.getValue().asBoolean();
			}
			return false;
		}
	}

	// Maximum number of concurrent downloads for wearable assets and textures
	private static final int MAX_CONCURRENT_DOWNLOADS = 5;
	// Maximum number of concurrent uploads for baked textures
	private static final int MAX_CONCURRENT_UPLOADS = 6;
	// Timeout for fetching inventory listings
	private static final int INVENTORY_TIMEOUT = 1000 * 30;
	// Timeout for fetching a single wearable, or receiving a single packet response
	private static final int WEARABLE_TIMEOUT = 1000 * 30;
	// Timeout for fetching a single texture
	private static final int TEXTURE_TIMEOUT = 1000 * 120;
	// Timeout for uploading a single baked texture
	private static final int UPLOAD_TIMEOUT = 1000 * 90;
	// Number of times to retry bake upload
	private static final int UPLOAD_RETRIES = 2;
	// When changing outfit, kick off rebake after 20 seconds has passed since the
	// last change
	private static final int REBAKE_DELAY = 1000 * 20;

	// Total number of wearables allowed for each avatar
	public static final int WEARABLE_COUNT_MAX = 60;
	// Total number of wearables for each avatar
	public static final int WEARABLE_COUNT = 16;
	// Total number of baked textures on each avatar
	public static final int BAKED_TEXTURE_COUNT = 6;
	// Total number of wearables per bake layer
	public static final int WEARABLES_PER_LAYER = 9;
	// Map of what wearables are included in each bake
	public static final WearableType[][] WEARABLE_BAKE_MAP = new WearableType[][] {
			new WearableType[] { WearableType.Shape, WearableType.Skin, WearableType.Tattoo, WearableType.Hair,
					WearableType.Alpha, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid },
			new WearableType[] { WearableType.Shape, WearableType.Skin, WearableType.Tattoo, WearableType.Shirt,
					WearableType.Jacket, WearableType.Gloves, WearableType.Undershirt, WearableType.Alpha,
					WearableType.Invalid },
			new WearableType[] { WearableType.Shape, WearableType.Skin, WearableType.Tattoo, WearableType.Pants,
					WearableType.Shoes, WearableType.Socks, WearableType.Jacket, WearableType.Underpants,
					WearableType.Alpha },
			new WearableType[] { WearableType.Eyes, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid },
			new WearableType[] { WearableType.Skirt, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid },
			new WearableType[] { WearableType.Hair, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,
					WearableType.Invalid } };

	// Magic values to finalize the cache check hashes for each bake
	public static final UUID[] BAKED_TEXTURE_HASH = new UUID[] { new UUID("18ded8d6-bcfc-e415-8539-944c0f5ea7a6"),
			new UUID("338c29e3-3024-4dbb-998d-7c04cf4fa88f"), new UUID("91b4a2c7-1b1a-ba16-9a16-1f8f8dcc1c3f"),
			new UUID("b2cf28af-b840-1071-3c6a-78085d8128b5"), new UUID("ea800387-ea1a-14e0-56cb-24f2022f969a"),
			new UUID("0af1ef7c-ad24-11dd-8790-001f5bf833e8") };
	// Default avatar texture, used to detect when a custom texture is not set for a
	// face
	public static final UUID DEFAULT_AVATAR_TEXTURE = new UUID("c228d1cf-4b5d-4ba8-84f4-899a0796aa97");

	public CallbackHandler<AgentWearablesReplyCallbackArgs> onAgentWearablesReply = new CallbackHandler<>();

	public CallbackHandler<AgentCachedBakesReplyCallbackArgs> onAgentCachedBakesReply = new CallbackHandler<>();

	public CallbackHandler<AppearanceSetCallbackArgs> onAppearanceSet = new CallbackHandler<>();

	public CallbackHandler<RebakeAvatarTexturesCallbackArgs> onRebakeAvatarReply = new CallbackHandler<>();

	// Visual parameters last sent to the sim
	public byte[] myVisualParameters = null;

	// Textures about this client sent to the sim
	public TextureEntry myTextures = null;

	// A cache of wearables currently being worn
	private MultiMap<WearableType, WearableData> wearables = new MultiMap<>();
	// A cache of textures currently being worn
	private TextureData[] textures = new TextureData[AvatarTextureIndex.getNumValues()];
	// Incrementing serial number for AgentCachedTexture packets
	private AtomicInteger cacheCheckSerialNum = new AtomicInteger(-1);
	// Incrementing serial number for AgentSetAppearance packets
	private AtomicInteger setAppearanceSerialNum = new AtomicInteger();
	// Indicates if WearablesRequest succeeded
	private boolean gotWearables = false;
	// Indicates whether or not the appearance thread is currently running, to
	// prevent multiple
	// appearance threads from running simultaneously
	// private AtomicBoolean AppearanceThreadRunning = new AtomicBoolean(false);
	// Reference to our agent
	private GridClient client;
	// Timer used for delaying rebake on changing outfit
	private Timer rebakeScheduleTimer;
	// Main appearance thread
	private Thread appearanceThread;
	// Is server baking complete. It needs doing only once
	private boolean serverBakingDone = false;

	private boolean sendAppearanceUpdates;

	/**
	 * Default constructor
	 *
	 * @param client
	 *            A reference to our agent
	 */
	public AppearanceManager(GridClient client) {
		this.client = client;

		this.client.settings.onSettingsUpdate.add(new SettingsUpdate());
		this.sendAppearanceUpdates = this.client.settings.getBool(LibSettings.SEND_AGENT_APPEARANCE);

		for (int i = 0; i < this.textures.length; i++) {
			this.textures[i] = new TextureData();
			this.textures[i].textureIndex = AvatarTextureIndex.setValue(i);
		}

		if (this.client.assets == null)
			logger.error(GridClient.Log("AppearanceManager requires a working AssetManager!", this.client));

		this.client.network.registerCallback(PacketType.AgentWearablesUpdate, this);
		this.client.network.registerCallback(PacketType.AgentCachedTextureResponse, this);
		this.client.network.registerCallback(PacketType.RebakeAvatarTextures, this);

		this.client.network.onEventQueueRunning.add(new Network_OnEventQueueRunning());
		this.client.network.onDisconnected.add(new Network_OnDisconnected(), true);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case AgentWearablesUpdate:
			handleAgentWearablesUpdate(packet, simulator);
			break;
		case AgentCachedTextureResponse:
			handleAgentCachedTextureResponse(packet, simulator);
			break;
		case RebakeAvatarTextures:
			handleRebakeAvatarTextures(packet, simulator);
			break;
		default:
			break;
		}
	}

	/**
	 * Returns true if AppearanceManager is busy and trying to set or change
	 * appearance will fail
	 */
	public boolean getManagerBusy() {
		return appearanceThread.isAlive();
	}

	/**
	 * Check if current region supports server side baking
	 *
	 * @returns True if server side baking support is detected
	 */
	public boolean isServerBakingRegion() {
		return client.network.getCurrentSim() != null
				&& ((client.network.getCurrentSim().protocols & RegionProtocols.AgentAppearanceService) != 0);
	}

	/**
	 * Starts the appearance setting thread
	 */
	public void requestSetAppearance() {
		requestSetAppearance(false);
	}

	/**
	 * Starts the appearance setting thread
	 *
	 * @param forceRebake
	 *            True to force rebaking, otherwise false
	 */
	public void requestSetAppearance(final boolean forceRebake) {
		if (appearanceThread != null && appearanceThread.isAlive()) {
			logger.warn(GridClient.Log("Appearance thread is already running, skipping", client));
			return;
		}

		// If we have an active delayed scheduled appearance bake, we dispose of it
		if (rebakeScheduleTimer != null) {
			rebakeScheduleTimer.cancel();
			rebakeScheduleTimer = null;
		}

		// This is the first time setting appearance, run through the entire sequence
		appearanceThread = new Thread("AppearenceThread") {
			@Override
			public void run() {
				boolean success = true;
				try {
					if (forceRebake) {
						// Set all of the baked textures to UUID.Zero to force rebaking
						for (BakeType type : BakeType.values()) {
							if (type != BakeType.Unknown)
								textures[bakeTypeToAgentTextureIndex(type).getValue()].textureID = UUID.ZERO;
						}
					}

					// Is this server side baking enabled sim
					if (isServerBakingRegion()) {
						if (!gotWearables) {
							// Fetch a list of the current agent wearables
							if (getAgentWearables()) {
								gotWearables = true;
							}
						}

						if (!serverBakingDone || forceRebake) {
							success = updateAvatarAppearance();
							if (success) {
								serverBakingDone = true;
							}
						}
					} else // Classic client side baking
					{
						if (!gotWearables) {
							// Fetch a list of the current agent wearables
							if (!getAgentWearables()) {
								logger.error(GridClient.Log(
										"Failed to retrieve a list of current agent wearables, appearance cannot be set",
										client));
								throw new Exception(
										"Failed to retrieve a list of current agent wearables, appearance cannot be set");
							}
							gotWearables = true;
						}

						// If we get back to server side baking region re-request server bake
						serverBakingDone = false;

						// Download and parse all of the agent wearables
						success = downloadWearables();
						if (!success) {
							logger.warn(GridClient.Log(
									"One or more agent wearables failed to download, appearance will be incomplete",
									client));
						}

						// If this is the first time setting appearance and we're not forcing rebakes,
						// check the server
						// for cached bakes
						if (setAppearanceSerialNum.get() == 0 && !forceRebake) {
							// Compute hashes for each bake layer and compare against what the simulator
							// currently has
							if (!getCachedBakes()) {
								logger.warn(GridClient.Log(
										"Failed to get a list of cached bakes from the simulator, appearance will be rebaked",
										client));
							}
						}

						// Download textures, compute bakes, and upload for any cache misses
						if (!createBakes()) {
							success = false;
							logger.warn(GridClient.Log(
									"Failed to create or upload one or more bakes, appearance will be incomplete",
									client));
						}

						// Send the appearance packet
						requestAgentSetAppearance();
					}
				} catch (Exception ex) {
					success = false;
					logger.warn(GridClient.Log(
							"Failed to get cached bakes from the simulator, appearance will be rebaked", client), ex);
				} finally {
					onAppearanceSet.dispatch(new AppearanceSetCallbackArgs(success));
				}
			}
		};
		appearanceThread.setDaemon(true);
		appearanceThread.start();
	}

	/**
	 * Check if current region supports server side baking
	 *
	 * @return True if server side baking support is detected
	 */
	public boolean serverBakingRegion() {
		return client.network.getCurrentSim() != null
				&& ((client.network.getCurrentSim().protocols & RegionProtocols.AgentAppearanceService) != 0);
	}

	/**
	 * Ask the server what textures our agent is currently wearing
	 *
	 * @throws Exception
	 */
	public void requestAgentWearables() throws Exception {
		AgentWearablesRequestPacket request = new AgentWearablesRequestPacket();
		request.AgentData.AgentID = client.agent.getAgentID();
		request.AgentData.SessionID = client.agent.getSessionID();

		client.network.sendPacket(request);
	}

	/**
	 * Build hashes out of the texture assetIDs for each baking layer to ask the
	 * simulator whether it has cached copies of each baked texture
	 *
	 * @throws Exception
	 */
	public void requestCachedBakes() throws Exception {
		List<AgentCachedTexturePacket.WearableDataBlock> hashes = new ArrayList<>();
		AgentCachedTexturePacket cache = new AgentCachedTexturePacket();

		// Build hashes for each of the bake layers from the individual components
		synchronized (wearables) {
			for (BakeType bakeType : BakeType.values()) {
				// Don't do a cache request for a skirt bake if we're not wearing a skirt
				if (bakeType == BakeType.Unknown
						|| (bakeType == BakeType.Skirt && !wearables.containsKey(WearableType.Skirt)))
					continue;

				// Build a hash of all the texture asset IDs in this baking layer
				UUID hash = UUID.ZERO;
				for (int wearableIndex = 0; wearableIndex < WEARABLES_PER_LAYER; wearableIndex++) {
					WearableType type = WEARABLE_BAKE_MAP[bakeType.getValue()][wearableIndex];

					if (type != WearableType.Invalid) {
						if (wearables.containsKey(type)) {
							for (WearableData data : wearables.get(type)) {
								hash = UUID.xor(hash, data.assetID);
							}
						}
					}
				}

				if (!hash.equals(UUID.ZERO)) {
					// Hash with our secret value for this baked layer
					hash = UUID.xor(hash, BAKED_TEXTURE_HASH[bakeType.getValue()]);

					// Add this to the list of hashes to send out
					AgentCachedTexturePacket.WearableDataBlock block = cache.new WearableDataBlock();
					block.ID = hash;
					block.TextureIndex = bakeTypeToAgentTextureIndex(bakeType).getValue();
					hashes.add(block);

					logger.debug(GridClient.Log("Checking cache for " + bakeType + ", hash = " + block.ID, client));
				}
			}
		}

		// Only send the packet out if there's something to check
		if (hashes.size() > 0) {
			cache.AgentData.AgentID = client.agent.getAgentID();
			cache.AgentData.SessionID = client.agent.getSessionID();
			cache.AgentData.SerialNum = cacheCheckSerialNum.incrementAndGet();

			cache.WearableData = new WearableDataBlock[hashes.size()];
			for (int i = 0; i < hashes.size(); i++) {
				cache.WearableData[i] = hashes.get(i);
			}
			client.network.sendPacket(cache);
		}
	}

	/// <summary>
	/// OBSOLETE! Returns the AssetID of the first asset that is currently
	/// being worn in a given WearableType slot
	/// </summary>
	/// <param name="type">WearableType slot to get the AssetID for</param>
	/// <returns>The UUID of the asset being worn in the given slot, or
	/// UUID.Zero if no wearable is attached to the given slot or wearables
	/// have not been downloaded yet
	public UUID getWearableAsset(WearableType type) {
		synchronized (wearables) {
			if (wearables.containsKey(type) && wearables.get(type).get(0) != null) {
				return wearables.get(type).get(0).assetID;
			}
		}
		return UUID.ZERO;
	}

	/**
	 * Returns the AssetID of the asset that is currently being worn in a given
	 * WearableType slot
	 *
	 * @param type
	 *            WearableType slot to get the AssetID for
	 * @returns A list of UUIDs of the assets being worn in the given slot, or an
	 *          empty list if no wearable is attached to the given slot or wearables
	 *          have not been downloaded yet
	 */
	public List<UUID> getWearableAssets(WearableType type) {
		List<UUID> list = new ArrayList<>();
		synchronized (wearables) {
			if (wearables.containsKey(type)) {
				for (WearableData data : wearables.get(type))
					list.add(data.assetID);
			}
		}
		return list;
	}

	/**
	 * Add a wearable to the current outfit and set appearance
	 *
	 * @param wearableItem
	 *            Wearable to be added to the outfit
	 * @param replace
	 *            Should existing item on the same point or of the same type be
	 *            replaced
	 * @throws Exception
	 */
	public void addToOutfit(InventoryItem wearableItem, boolean replace) throws Exception {
		List<InventoryItem> wearableItems = new ArrayList<>();
		wearableItems.add(wearableItem);
		addToOutfit(wearableItems, replace);
	}

	/**
	 * Add a list of wearables to the current outfit and set appearance
	 *
	 * @param wearableItems
	 *            List of wearable inventory items to be added to the outfit
	 * @throws Exception
	 */
	public void addToOutfit(List<InventoryItem> wearableItems) throws Exception {
		addToOutfit(wearableItems, true);
	}

	/**
	 * Add a list of wearables to the current outfit and set appearance
	 *
	 * @param wearableItems
	 *            List of wearable inventory items to be added to the outfit
	 * @param replace
	 *            Should existing item on the same point or of the same type be
	 *            replaced
	 * @throws Exception
	 */
	public void addToOutfit(List<InventoryItem> wearableItems, boolean replace) throws Exception {
		List<InventoryWearable> wearablesList = new ArrayList<>();
		List<InventoryItem> attachments = new ArrayList<>();

		for (InventoryItem item : wearableItems) {
			if (item instanceof InventoryWearable)
				wearablesList.add((InventoryWearable) item);
			else if (item instanceof InventoryAttachment || item instanceof InventoryObject)
				attachments.add(item);
		}

		synchronized (wearables) {
			// Add the given wearables to the wearables collection
			for (InventoryWearable wearableItem : wearablesList) {
				WearableData wd = new WearableData();
				wd.assetID = wearableItem.assetID;
				wd.assetType = wearableItem.assetType;
				wd.itemID = wearableItem.itemID;
				wd.wearableType = wearableItem.getWearableType();

				if (replace) // Dump everything from the key
					wearables.remove(wearableItem.getWearableType());
				wearables.put(wearableItem.getWearableType(), wd);
			}
		}

		if (attachments.size() > 0) {
			addAttachments(attachments, false, replace);
		}

		if (wearablesList.size() > 0) {
			sendAgentIsNowWearing();
			delayedRequestSetAppearance();
		}
	}

	/**
	 * Remove a wearable from the current outfit and set appearance
	 *
	 * @param wearableItem
	 *            Wearable to be removed from the outfit
	 * @throws Exception
	 */
	public void removeFromOutfit(InventoryItem wearableItem) throws Exception {
		List<InventoryItem> wearableItems = new ArrayList<>();
		wearableItems.add(wearableItem);
		removeFromOutfit(wearableItems);
	}

	/**
	 * Removes a list of wearables from the current outfit and set appearance
	 *
	 * @param wearableItems
	 *            List of wearable inventory items to be removed from the outfit
	 * @throws Exception
	 */
	public void removeFromOutfit(List<InventoryItem> wearableItems) throws Exception {
		List<InventoryWearable> wearablesList = new ArrayList<>();
		List<InventoryItem> attachments = new ArrayList<>();

		for (InventoryItem item : wearableItems) {
			if (item instanceof InventoryWearable)
				wearablesList.add((InventoryWearable) item);
			else if (item instanceof InventoryAttachment || item instanceof InventoryObject)
				attachments.add(item);
		}

		boolean needSetAppearance = false;
		synchronized (wearables) {
			// Remove the given wearables from the wearables collection
			for (InventoryWearable wearableItem : wearablesList) {
				if (wearableItem.assetType != AssetType.Bodypart // Remove if it's not a body part
						&& wearables.containsKey(wearableItem.getWearableType())) // And we have that wearabe type
				{
					Collection<WearableData> worn = wearables.get(wearableItem.getWearableType());
					if (worn != null) {
						for (WearableData wearable : worn) {
							if (wearable.itemID.equals(wearableItem.itemID)) {
								wearables.remove(wearableItem.getWearableType(), wearable);
								needSetAppearance = true;
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < attachments.size(); i++) {
			detach(attachments.get(i).itemID);
		}

		if (needSetAppearance) {
			sendAgentIsNowWearing();
			delayedRequestSetAppearance();
		}
	}

	/**
	 * Replace the current outfit with a list of wearables and set appearance
	 *
	 * @param wearableItems
	 *            List of wearable inventory items that define a new outfit
	 * @throws Exception
	 */
	public void replaceOutfit(List<InventoryItem> wearableItems) throws Exception {
		replaceOutfit(wearableItems, true);
	}

	/**
	 * Replace the current outfit with a list of wearables and set appearance
	 *
	 * @param wearableItems
	 *            List of wearable inventory items that define a new outfit
	 * @param safe
	 *            Check if we have all body parts, set this to false only if you
	 *            know what you're doing
	 * @throws Exception
	 */
	public void replaceOutfit(List<InventoryItem> wearableItems, boolean safe) throws Exception {
		List<InventoryWearable> wearablesList = new ArrayList<>();
		List<InventoryItem> attachments = new ArrayList<>();

		for (int i = 0; i < wearableItems.size(); i++) {
			InventoryItem item = wearableItems.get(i);

			if (item instanceof InventoryWearable)
				wearablesList.add((InventoryWearable) item);
			else if (item instanceof InventoryAttachment || item instanceof InventoryObject)
				attachments.add(item);
		}

		if (safe) {
			// If we don't already have a the current agent wearables downloaded, updating
			// to a
			// new set of wearables that doesn't have all of the bodyparts can leave the
			// avatar
			// in an inconsistent state. If any bodypart entries are empty, we need to fetch
			// the
			// current wearables first
			boolean needsCurrentWearables = false;
			synchronized (wearables) {
				for (WearableType wearableType : WearableType.values()) {
					if (wearableType != WearableType.Invalid
							&& wearableTypeToAssetType(wearableType) == AssetType.Bodypart
							&& !wearables.containsKey(wearableType)) {
						needsCurrentWearables = true;
						break;
					}
				}
			}

			if (needsCurrentWearables && !getAgentWearables()) {
				logger.error(GridClient.Log("Failed to fetch the current agent wearables, cannot safely replace outfit",
						client));
				return;
			}
		}

		// Replace our local Wearables collection, send the packet(s) to update our
		// attachments, tell sim what we are wearing now, and start the baking process
		if (!safe) {
			setAppearanceSerialNum.incrementAndGet();
		}
		replaceWearables(wearablesList);
		addAttachments(attachments, true, false);
		sendAgentIsNowWearing();
		delayedRequestSetAppearance();
	}

	/**
	 * Checks if an inventory item is currently being worn
	 *
	 * @param item
	 *            The inventory item to check against the agent wearables
	 * @returnsThe WearableType slot that the item is being worn in, or
	 *             WearableType.Invalid if it is not currently being worn
	 */
	public WearableType isItemWorn(InventoryItem item) {
		synchronized (wearables) {
			for (Entry<WearableType, List<WearableData>> entry : wearables.entrySet()) {
				for (WearableData data : entry.getValue()) {
					if (data.itemID.equals(item.itemID))
						return entry.getKey();
				}
			}
		}
		return WearableType.Invalid;
	}

	/**
	 * Returns a copy of the agents currently worn wearables Avoid calling this
	 * function multiple times as it will make a copy of all of the wearable data
	 * each time
	 *
	 * @returnsA copy of the agents currently worn wearables
	 */
	public Collection<WearableData> getWearables() {
		synchronized (wearables) {
			return new ArrayList<>(wearables.values());
		}
	}

	public MultiMap<WearableType, WearableData> getWearablesByType() {
		synchronized (wearables) {
			return new MultiMap<>(wearables);
		}
	}

	/**
	 * Calls either <see cref="ReplaceOutfit"/> orb<see cref="AddToOutfit"/>
	 * depending on the value of replaceItems
	 *
	 * @param wearables
	 *            List of wearable inventory items to add to the outfit or become a
	 *            new outfit
	 * @param replaceItems
	 *            True to replace existing items with the new list of items, false
	 *            to add these items to the existing outfit
	 * @throws Exception
	 */
	public void wearOutfit(List<InventoryItem> wearables, boolean replaceItems) throws Exception {
		List<InventoryItem> wearableItems = new ArrayList<>(wearables.size());
		Iterator<InventoryItem> iter = wearables.iterator();
		while (iter.hasNext()) {
			wearableItems.add(iter.next());
		}

		if (replaceItems)
			replaceOutfit(wearableItems);
		else
			addToOutfit(wearableItems);
	}

	/**
	 * Adds a list of attachments to our agent
	 *
	 * @param attachments
	 *            A List containing the attachments to add
	 * @param removeExistingFirst
	 *            If true, tells simulator to remove existing attachment first
	 * @throws Exception
	 */
	public void addAttachments(List<InventoryItem> attachments, boolean removeExistingFirst) throws Exception {
		addAttachments(attachments, removeExistingFirst, true);
	}

	/**
	 * Adds a list of attachments to our agent
	 *
	 * @param attachments
	 *            A List containing the attachments to add
	 * @param removeExistingFirst
	 *            If true, tells simulator to remove existing attachment first
	 * @param replace
	 *            If true replace existing attachment on this attachment point,
	 *            otherwise add to it (multi-attachments)
	 * @throws Exception
	 */
	public void addAttachments(List<InventoryItem> attachments, boolean removeExistingFirst, boolean replace)
			throws Exception {
		// Use RezMultipleAttachmentsFromInv to clear out current attachments, and
		// attach new ones
		RezMultipleAttachmentsFromInvPacket attachmentsPacket = new RezMultipleAttachmentsFromInvPacket();
		attachmentsPacket.AgentData.AgentID = client.agent.getAgentID();
		attachmentsPacket.AgentData.SessionID = client.agent.getSessionID();

		attachmentsPacket.HeaderData.CompoundMsgID = new UUID();
		attachmentsPacket.HeaderData.FirstDetachAll = removeExistingFirst;
		attachmentsPacket.HeaderData.TotalObjects = (byte) attachments.size();

		attachmentsPacket.ObjectData = new RezMultipleAttachmentsFromInvPacket.ObjectDataBlock[attachments.size()];
		for (int i = 0; i < attachments.size(); i++) {
			InventoryAttachment attachment = (InventoryAttachment) attachments.get(i);
			if (attachments.get(i) instanceof InventoryAttachment) {
				attachmentsPacket.ObjectData[i] = attachmentsPacket.new ObjectDataBlock();
				attachmentsPacket.ObjectData[i].AttachmentPt = attachment.getAttachmentPoint().getValue(replace);
				attachmentsPacket.ObjectData[i].EveryoneMask = attachment.permissions.everyoneMask;
				attachmentsPacket.ObjectData[i].GroupMask = attachment.permissions.groupMask;
				attachmentsPacket.ObjectData[i].ItemFlags = attachment.itemFlags;
				attachmentsPacket.ObjectData[i].ItemID = attachment.itemID;
				attachmentsPacket.ObjectData[i].setName(Helpers.stringToBytes(attachment.name));
				attachmentsPacket.ObjectData[i].setDescription(Helpers.stringToBytes(attachment.description));
				attachmentsPacket.ObjectData[i].NextOwnerMask = attachment.permissions.nextOwnerMask;
				attachmentsPacket.ObjectData[i].OwnerID = attachment.getOwnerID();
			} else if (attachments.get(i) instanceof InventoryObject) {
				attachmentsPacket.ObjectData[i] = attachmentsPacket.new ObjectDataBlock();
				attachmentsPacket.ObjectData[i].AttachmentPt = AttachmentPoint.Default.getValue(replace);
				attachmentsPacket.ObjectData[i].EveryoneMask = attachment.permissions.everyoneMask;
				attachmentsPacket.ObjectData[i].GroupMask = attachment.permissions.groupMask;
				attachmentsPacket.ObjectData[i].ItemFlags = attachment.itemFlags;
				attachmentsPacket.ObjectData[i].ItemID = attachment.itemID;
				attachmentsPacket.ObjectData[i].setName(Helpers.stringToBytes(attachment.name));
				attachmentsPacket.ObjectData[i].setDescription(Helpers.stringToBytes(attachment.description));
				attachmentsPacket.ObjectData[i].NextOwnerMask = attachment.permissions.nextOwnerMask;
				attachmentsPacket.ObjectData[i].OwnerID = attachment.getOwnerID();
			} else {
				logger.warn(GridClient.Log("Cannot attach inventory item " + attachment.name, client));
			}
		}
		client.network.sendPacket(attachmentsPacket);
	}

	/**
	 * Attach an item to our agent at a specific attach point
	 *
	 * @param item
	 *            A <seealso cref="OpenMetaverse.InventoryItem"/> to attach
	 * @param attachPoint
	 *            the <seealso cref="OpenMetaverse.AttachmentPoint"/> on the avatar
	 *            to attach the item to
	 * @throws Exception
	 */
	public void attach(InventoryItem item, AttachmentPoint attachPoint) throws Exception {
		attach(item.itemID, item.getOwnerID(), item.name, item.description, item.permissions, item.itemFlags,
				attachPoint, true);
	}

	/**
	 * Attach an item to our agent at a specific attach point
	 *
	 * @param item
	 *            A <seealso cref="OpenMetaverse.InventoryItem"/> to attach
	 * @param attachPoint
	 *            the <seealso cref="OpenMetaverse.AttachmentPoint"/> on the avatar
	 *            to attach the item to
	 * @param replace
	 *            If true replace existing attachment on this attachment point,
	 *            otherwise add to it (multi-attachments)
	 * @throws Exception
	 */
	public void attach(InventoryItem item, AttachmentPoint attachPoint, boolean replace) throws Exception {
		attach(item.itemID, item.getOwnerID(), item.name, item.description, item.permissions, item.itemFlags,
				attachPoint, replace);
	}

	/**
	 * Attach an item to our agent specifying attachment details
	 *
	 * @param itemID
	 *            The <seealso cref="OpenMetaverse.UUID"/> of the item to attach
	 * @param ownerID
	 *            The <seealso cref="OpenMetaverse.UUID"/> attachments owner
	 * @param name
	 *            The name of the attachment
	 * @param description
	 *            The description of the attahment
	 * @param perms
	 *            The <seealso cref="OpenMetaverse.Permissions"/> to apply when
	 *            attached
	 * @param itemFlags
	 *            The <seealso cref="OpenMetaverse.InventoryItemFlags"/> of the
	 *            attachment
	 * @param attachPoint
	 *            The <seealso cref="OpenMetaverse.AttachmentPoint"/> on the agent
	 *            to attach the item to
	 * @throws Exception
	 */
	public void attach(UUID itemID, UUID ownerID, String name, String description, Permissions perms, int itemFlags,
			AttachmentPoint attachPoint) throws Exception {
		attach(itemID, ownerID, name, description, perms, itemFlags, attachPoint, true);
	}

	/**
	 * Attach an item to our agent specifying attachment details
	 *
	 * @param itemID
	 *            The <seealso cref="OpenMetaverse.UUID"/> of the item to attach
	 * @param ownerID
	 *            The <seealso cref="OpenMetaverse.UUID"/> attachments owner
	 * @param name
	 *            The name of the attachment
	 * @param description
	 *            The description of the attahment
	 * @param perms
	 *            The <seealso cref="OpenMetaverse.Permissions"/> to apply when
	 *            attached
	 * @param itemFlags
	 *            The <seealso cref="OpenMetaverse.InventoryItemFlags"/> of the
	 *            attachment
	 * @param attachPoint
	 *            The <seealso cref="OpenMetaverse.AttachmentPoint"/> on the agent
	 *            to attach the item to
	 * @param replace
	 *            If true replace existing attachment on this attachment point,
	 *            otherwise add to it (multi-attachments)
	 * @throws Exception
	 */
	public void attach(UUID itemID, UUID ownerID, String name, String description, Permissions perms, int itemFlags,
			AttachmentPoint attachPoint, boolean replace) throws Exception {
		// TODO: At some point it might be beneficial to have AppearanceManager track
		// what we
		// are currently wearing for attachments to make enumeration and detachment
		// easier
		RezSingleAttachmentFromInvPacket attach = new RezSingleAttachmentFromInvPacket();

		attach.AgentData.AgentID = client.agent.getAgentID();
		attach.AgentData.SessionID = client.agent.getSessionID();

		attach.ObjectData.AttachmentPt = attachPoint.getValue(replace);
		attach.ObjectData.setDescription(Helpers.stringToBytes(description));
		attach.ObjectData.EveryoneMask = perms.everyoneMask;
		attach.ObjectData.GroupMask = perms.groupMask;
		attach.ObjectData.ItemFlags = itemFlags;
		attach.ObjectData.ItemID = itemID;
		attach.ObjectData.setName(Helpers.stringToBytes(name));
		attach.ObjectData.NextOwnerMask = perms.nextOwnerMask;
		attach.ObjectData.OwnerID = ownerID;

		client.network.sendPacket(attach);
	}

	/**
	 * Detach an item from our agent using an
	 * <seealso cref="OpenMetaverse.InventoryItem"/> object
	 *
	 * @param item
	 *            An <see cref="OpenMetaverse.InventoryItem"/> object
	 * @throws Exception
	 */
	public void detach(InventoryItem item) throws Exception {
		detach(item.itemID);
	}

	/**
	 * Detach an item from our agent
	 *
	 * @param itemID
	 *            The inventory itemID of the item to detach
	 * @throws Exception
	 */
	public void detach(UUID itemID) throws Exception {
		DetachAttachmentIntoInvPacket detach = new DetachAttachmentIntoInvPacket();
		detach.ObjectData.AgentID = client.agent.getAgentID();
		detach.ObjectData.ItemID = itemID;

		client.network.sendPacket(detach);
	}

	/**
	 * Inform the sim which wearables are part of our current outfit
	 *
	 * @throws Exception
	 */
	private void sendAgentIsNowWearing() throws Exception {
		AgentIsNowWearingPacket wearing = new AgentIsNowWearingPacket();
		wearing.AgentData.AgentID = client.agent.getAgentID();
		wearing.AgentData.SessionID = client.agent.getSessionID();
		wearing.WearableData = new AgentIsNowWearingPacket.WearableDataBlock[WearableType.getNumValues()];

		synchronized (wearables) {
			for (WearableType type : WearableType.values()) {
				if (type != WearableType.Invalid) {
					AgentIsNowWearingPacket.WearableDataBlock block = wearing.new WearableDataBlock();
					block.WearableType = type.getValue();

					// This appears to be hacked on SL server side to support multi-layers
					if (wearables.containsKey(type) && wearables.get(type).get(0) != null)
						block.ItemID = wearables.get(type).get(0).itemID;
					else
						block.ItemID = UUID.ZERO;
					wearing.WearableData[type.getValue()] = block;
				}
			}
		}
		client.network.sendPacket(wearing);
	}

	/**
	 * Replaces the Wearables collection with a list of new wearable items
	 *
	 * @param wearableItems
	 *            Wearable items to replace the Wearables collection with
	 */
	private void replaceWearables(List<InventoryWearable> wearableItems) {
		MultiMap<WearableType, WearableData> newWearables = new MultiMap<>();

		synchronized (wearables) {
			// Preserve body parts from the previous set of wearables. They may be
			// overwritten,
			// but cannot be missing in the new set
			for (Entry<WearableType, List<WearableData>> entry : wearables.entrySet()) {
				for (WearableData data : entry.getValue()) {
					if (data.assetType == AssetType.Bodypart)
						newWearables.put(entry.getKey(), data);
				}
			}

			// Add the given wearables to the new wearables collection
			for (InventoryWearable wearableItem : wearableItems) {
				WearableData data = new WearableData();
				data.assetID = wearableItem.assetID;
				data.assetType = wearableItem.assetType;
				data.itemID = wearableItem.itemID;
				data.wearableType = wearableItem.getWearableType();

				newWearables.put(data.wearableType, data);
			}

			// Replace the Wearables collection
			wearables = newWearables;
		}
	}

	/**
	 * Calculates base color/tint for a specific wearable based on its params
	 *
	 * @param param
	 *            All the color info gathered from wearable's VisualParams passed as
	 *            list of ColorParamInfo tuples
	 * @returns Base color/tint for the wearable
	 */
	public Color4 getColorFromParams(List<ColorParamInfo> param) {
		// Start off with a blank slate, black, fully transparent
		Color4 res = new Color4(0, 0, 0, 0);

		// Apply color modification from each color parameter
		Iterator<ColorParamInfo> iter = param.iterator();
		while (iter.hasNext()) {
			ColorParamInfo p = iter.next();
			int n = p.visualColorParam.colors.length;

			Color4 paramColor = new Color4(0, 0, 0, 0);

			if (n == 1) {
				// We got only one color in this param, use it for application
				// to the final color
				paramColor = p.visualColorParam.colors[0];
			} else if (n > 1) {
				// We have an array of colors in this parameter
				// First, we need to find out, based on param value
				// between which two elements of the array our value lands

				// Size of the step using which we iterate from Min to Max
				float step = (p.visualParam.maxValue - p.visualParam.minValue) / ((float) n - 1);

				// Our color should land inbetween colors in the array with index a and b
				int indexa = 0;
				int indexb = 0;

				int i = 0;

				for (float a = p.visualParam.minValue; a <= p.visualParam.maxValue; a += step) {
					if (a <= p.value) {
						indexa = i;
					} else {
						break;
					}

					i++;
				}

				// Sanity check that we don't go outside bounds of the array
				if (indexa > n - 1)
					indexa = n - 1;

				indexb = (indexa == n - 1) ? indexa : indexa + 1;

				// How far is our value from Index A on the
				// line from Index A to Index B
				float distance = p.value - indexa * step;

				// We are at Index A (allowing for some floating point math fuzz),
				// use the color on that index
				if (distance < 0.00001f || indexa == indexb) {
					paramColor = p.visualColorParam.colors[indexa];
				} else {
					// Not so simple as being precisely on the index eh? No problem.
					// We take the two colors that our param value places us between
					// and then find the value for each ARGB element that is
					// somewhere on the line between color1 and color2 at some
					// distance from the first color
					Color4 c1 = paramColor = p.visualColorParam.colors[indexa];
					Color4 c2 = paramColor = p.visualColorParam.colors[indexb];

					// Distance is some fraction of the step, use that fraction
					// to find the value in the range from color1 to color2
					paramColor = Color4.lerp(c1, c2, distance / step);
				}

				// Please leave this fragment even if its commented out
				// might prove useful should ($deity forbid) there be bugs in this code
				// string carray = "";
				// foreach (Color c in p.VisualColorParam.Colors)
				// {
				// carray += c.ToString() + " - ";
				// }
				// logger.debug("Calculating color for " + p.WearableType + " from " +
				// p.VisualParam.Name + ", value is " + p.Value + " in range " +
				// p.VisualParam.MinValue + " - " + p.VisualParam.MaxValue + " step " + step + "
				// with " + n + " elements " + carray + " A: " + indexa + " B: " + indexb + " at
				// distance " + distance);
			}

			// Now that we have calculated color from the scale of colors
			// that visual params provided, lets apply it to the result
			switch (p.visualColorParam.operation) {
			case Add:
				res = Color4.add(res, paramColor);
				break;
			case Multiply:
				res = Color4.multiply(res, paramColor);
				break;
			case Blend:
				res = Color4.lerp(res, paramColor, p.value);
				break;
			default:
				break;
			}
		}
		return res;
	}

	/**
	 * Blocking method to populate the Wearables dictionary
	 *
	 * @returns True on success, otherwise false
	 * @throws Exception
	 */
	boolean getAgentWearables() throws Exception {
		final TimeoutEvent<Boolean> wearablesEvent = new TimeoutEvent<>();
		Callback<AgentWearablesReplyCallbackArgs> wearablesCallback = new Callback<AgentWearablesReplyCallbackArgs>() {
			@Override
			public boolean callback(AgentWearablesReplyCallbackArgs e) {
				wearablesEvent.set(true);
				return false;
			}
		};

		onAgentWearablesReply.add(wearablesCallback);

		requestAgentWearables();

		boolean success = wearablesEvent.waitOne(WEARABLE_TIMEOUT);

		onAgentWearablesReply.remove(wearablesCallback);

		return success;
	}

	/**
	 * Blocking method to populate the Textures array with cached bakes
	 *
	 * @returns True on success, otherwise false
	 * @throws Exception
	 */
	boolean getCachedBakes() throws Exception {
		final TimeoutEvent<Boolean> cacheCheckEvent = new TimeoutEvent<>();
		Callback<AgentCachedBakesReplyCallbackArgs> cacheCallback = new Callback<AgentCachedBakesReplyCallbackArgs>() {
			@Override
			public boolean callback(AgentCachedBakesReplyCallbackArgs e) {
				cacheCheckEvent.set(true);
				return false;
			}
		};

		onAgentCachedBakesReply.add(cacheCallback);

		requestCachedBakes();

		Boolean success = cacheCheckEvent.waitOne(WEARABLE_TIMEOUT);

		onAgentCachedBakesReply.remove(cacheCallback);

		return success != null ? success : false;
	}

	/**
	 * Populates textures and visual params from a decoded asset
	 *
	 * @param wearable
	 *            Wearable to decode
	 * @param textures
	 *            Texture data
	 */
	public void decodeWearableParams(WearableData wearable, TextureData[] textures) {
		Map<VisualAlphaParam, Float> alphaMasks = new HashMap<>();
		List<ColorParamInfo> colorParams = new ArrayList<>();

		// Populate collection of alpha masks from visual params
		// also add color tinting information
		for (Entry<Integer, Float> kvp : wearable.asset.params.entrySet()) {
			if (!VisualParams.params.containsKey(kvp.getKey()))
				continue;

			VisualParam p = VisualParams.params.get(kvp.getKey());

			ColorParamInfo colorInfo = new ColorParamInfo();
			colorInfo.visualParam = p;
			colorInfo.value = kvp.getValue();

			// Color params
			if (p.colorParams != null) {
				colorInfo.visualColorParam = p.colorParams;
				int key = kvp.getKey();

				if (wearable.wearableType == WearableType.Tattoo) {
					if (key == 1062 || key == 1063 || key == 1064) {
						colorParams.add(colorInfo);
					}
				} else if (wearable.wearableType == WearableType.Jacket) {
					if (key == 809 || key == 810 || key == 811) {
						colorParams.add(colorInfo);
					}
				} else if (wearable.wearableType == WearableType.Hair) {
					// Param 112 - Rainbow
					// Param 113 - Red
					// Param 114 - Blonde
					// Param 115 - White
					if (key == 112 || key == 113 || key == 114 || key == 115) {
						colorParams.add(colorInfo);
					}
				} else if (wearable.wearableType == WearableType.Skin) {
					// For skin we skip makeup params for now and use only the 3
					// that are used to determine base skin tone
					// Param 108 - Rainbow Color
					// Param 110 - Red Skin (Ruddiness)
					// Param 111 - Pigment
					if (kvp.getKey() == 108 || kvp.getKey() == 110 || kvp.getKey() == 111) {
						colorParams.add(colorInfo);
					}
				} else {
					colorParams.add(colorInfo);
				}
			}

			// Add alpha mask
			if (p.alphaParams != null && !p.alphaParams.tgaFile.isEmpty() && !p.isBumpAttribute
					&& !alphaMasks.containsKey(p.alphaParams)) {
				alphaMasks.put(p.alphaParams, kvp.getValue() == 0 ? 0.01f : kvp.getValue());
			}

			// Alhpa masks can also be specified in sub "driver" params
			if (p.drivers != null) {
				for (int i = 0; i < p.drivers.length; i++) {
					if (VisualParams.params.containsKey(p.drivers[i])) {
						VisualParam driver = VisualParams.params.get(p.drivers[i]);
						if (driver.alphaParams != null && !driver.alphaParams.tgaFile.isEmpty()
								&& !driver.isBumpAttribute && !alphaMasks.containsKey(driver.alphaParams)) {
							alphaMasks.put(driver.alphaParams, kvp.getValue() == 0 ? 0.01f : kvp.getValue());
						}
					}
				}
			}
		}

		Color4 wearableColor = Color4.WHITE; // Never actually used
		if (colorParams.size() > 0) {
			wearableColor = getColorFromParams(colorParams);
			logger.debug("Setting tint " + wearableColor + " for " + wearable.wearableType);
		}

		// Loop through all of the texture IDs in this decoded asset and put them in our
		// cache of worn textures
		for (Entry<AvatarTextureIndex, UUID> entry : wearable.asset.textures.entrySet()) {
			int i = AvatarTextureIndex.getValue(entry.getKey());

			// Update information about color and alpha masks for this texture
			textures[i].alphaMasks = alphaMasks;
			textures[i].color = wearableColor;

			// If this texture changed, update the TextureID and clear out the old cached
			// texture asset
			if (textures[i].textureID == null || !textures[i].textureID.equals(entry.getValue())) {
				// Treat DEFAULT_AVATAR_TEXTURE as null
				if (entry.getValue().equals(DEFAULT_AVATAR_TEXTURE))
					textures[i].textureID = UUID.ZERO;
				else
					textures[i].textureID = entry.getValue();
				logger.debug(GridClient.Log("Set " + entry.getKey() + " to " + textures[i].textureID, client));

				textures[i].texture = null;
			}
		}
	}

	private class WearablesReceived implements Callback<AssetDownload> {
		private final WearableData wearable;
		private final CountDownLatch latch;

		public WearablesReceived(CountDownLatch latch, WearableData wearable) {
			this.latch = latch;
			this.wearable = wearable;
		}

		public boolean callback(AssetDownload transfer) {
			if (transfer.success) {
				wearable.asset = (AssetWearable) AssetManager.createAssetItem(transfer.assetType, transfer.itemID,
						transfer.assetData);
				if (wearable.asset != null) {
					decodeWearableParams(wearable, textures);
					logger.debug(GridClient.Log("Downloaded wearable asset " + wearable.wearableType + " with "
							+ wearable.asset.params.size() + " visual params and " + wearable.asset.textures.size()
							+ " textures", client));

				} else {
					logger.error(GridClient.Log("Failed to decode wearable asset: " + transfer.itemID, client));
				}
			} else {
				logger.warn(GridClient.Log("Wearable " + wearable.wearableType + " {" + wearable.assetID
						+ "} failed to download, status:  " + transfer.status, client));
			}
			latch.countDown();
			return true;
		}
	}

	/**
	 * Blocking method to download and parse currently worn wearable assets
	 *
	 * @returns True on success, otherwise false
	 */
	private boolean downloadWearables() {
		boolean success = true;

		// Make a copy of the wearables dictionary to enumerate over
		MultiMap<WearableType, WearableData> wearablesMap;
		synchronized (wearables) {
			wearablesMap = new MultiMap<>(wearables);
		}

		// We will refresh the textures (zero out all non bake textures)
		for (int i = 0; i < textures.length; i++) {
			boolean isBake = false;
			for (BakeType type : BakeType.values()) {
				if (bakeTypeToAgentTextureIndex(type).getValue() == i) {
					isBake = true;
					break;
				}
			}
			if (!isBake) {
				textures[i].texture = null;
				textures[i].textureID = null;
				textures[i].color = null;
			}
		}

		final CountDownLatch latch = new CountDownLatch(wearablesMap.size());
		for (WearableData wearable : wearablesMap.values()) {
			if (wearable.asset != null) {
				decodeWearableParams(wearable, textures);
				latch.countDown();
			}
		}

		int pendingWearables = (int) latch.getCount();
		if (pendingWearables == 0)
			return true;

		logger.debug("Downloading " + pendingWearables + " wearable assets");

		ExecutorService executor = Executors.newFixedThreadPool(Math.min(pendingWearables, MAX_CONCURRENT_DOWNLOADS));
		for (final WearableData wearable : wearablesMap.values()) {
			if (wearable.asset == null) {
				executor.submit(new Runnable() {
					@Override
					public void run() {
						// Fetch this wearable asset
						try {
							client.assets.requestAsset(wearable.assetID, wearable.assetType, true,
									new WearablesReceived(latch, wearable));
						} catch (Exception ex) {
						}
					}
				});
			}
		}

		try {
			success = latch.await(TEXTURE_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
		executor.shutdown();
		return success;
	}

	/**
	 * Get a list of all of the textures that need to be downloaded for a single
	 * bake layer
	 *
	 * @param bakeType
	 *            Bake layer to get texture AssetIDs for
	 * @returns A list of texture AssetIDs to download
	 */
	private List<UUID> getTextureDownloadList(BakeType bakeType) {
		List<AvatarTextureIndex> indices = bakeTypeToTextures(bakeType);
		List<UUID> textures = new ArrayList<>();

		for (AvatarTextureIndex index : indices) {
			// If this is not the skirt layer or we're wearing a skirt then add it
			if (index != AvatarTextureIndex.Skirt || wearables.containsKey(WearableType.Skirt))
				addTextureDownload(index, textures);
		}
		return textures;
	}

	/**
	 * Helper method to lookup the TextureID for a single layer and add it to the
	 * texture list if it is not already present
	 *
	 * @param index
	 * @param textures
	 */
	private void addTextureDownload(AvatarTextureIndex index, List<UUID> textures) {
		TextureData textureData = this.textures[index.getValue()];
		// Add the textureID to the list if this layer has a valid textureID set, it has
		// not already
		// been downloaded, and it is not already in the download list
		if (!UUID.isZeroOrNull(textureData.textureID) && textureData.texture == null
				&& !textures.contains(textureData.textureID))
			textures.add(textureData.textureID);
	}

	/**
	 * Blocking method to download all of the textures needed for baking the given
	 * bake layers No return value is given because the baking will happen whether
	 * or not all textures are successfully downloaded
	 *
	 * @param bakeLayers
	 *            A list of layers that need baking
	 */
	private void downloadTextures(List<BakeType> bakeLayers) {
		List<UUID> textureIDs = new ArrayList<>();

		for (int i = 0; i < bakeLayers.size(); i++) {
			List<UUID> layerTextureIDs = getTextureDownloadList(bakeLayers.get(i));

			for (int j = 0; j < layerTextureIDs.size(); j++) {
				UUID uuid = layerTextureIDs.get(j);
				if (!textureIDs.contains(uuid))
					textureIDs.add(uuid);
			}
		}

		logger.debug("Downloading " + textureIDs.size() + " textures for baking");

		final CountDownLatch latch = new CountDownLatch(textureIDs.size());
		for (UUID textureID : textureIDs) {
			client.assets.requestImage(textureID, new Callback<ImageDownload>() {
				@Override
				public boolean callback(ImageDownload download) {
					if (download.state == TextureRequestState.Finished && download.assetData != null) {
						AssetTexture texture = (AssetTexture) AssetManager.createAssetItem(AssetType.Texture,
								download.itemID, download.assetData);
						if (texture == null) {
							logger.error(GridClient.Log("Failed to decode texture: " + textureID, client));
						}

						for (int i = 0; i < textures.length; i++) {
							if (textures[i].textureID != null && textures[i].textureID.equals(download.itemID))
								textures[i].texture = texture;
						}
					} else {
						logger.warn(GridClient.Log("Texture " + download.itemID
								+ " failed to download, one or more bakes will be incomplete", client));
					}
					latch.countDown();
					return true;
				}
			});
		}

		try {
			latch.await(TEXTURE_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Blocking method to create and upload baked textures for all of the missing
	 * bakes
	 *
	 * @returns True on success, otherwise false
	 */
	private boolean createBakes() {
		List<BakeType> pendingBakes = new ArrayList<>();

		// Check each bake layer in the Textures array for missing bakes
		for (BakeType type : BakeType.values()) {
			if (type != BakeType.Unknown) {
				UUID uuid = textures[bakeTypeToAgentTextureIndex(type).getValue()].textureID;
				if (UUID.isZeroOrNull(uuid)) {
					// If this is the skirt layer and we're not wearing a skirt then skip it
					if (type == BakeType.Skirt && !wearables.containsKey(WearableType.Skirt))
						continue;

					pendingBakes.add(type);
				}
			}
		}

		final AtomicBoolean success = new AtomicBoolean(true);
		if (pendingBakes.size() > 0) {
			downloadTextures(pendingBakes);

			ExecutorService executor = Executors
					.newFixedThreadPool(Math.min(pendingBakes.size(), MAX_CONCURRENT_UPLOADS));
			for (final BakeType bakeType : pendingBakes) {
				executor.submit(new Runnable() {
					@Override
					public void run() {
						try {
							if (!createBake(bakeType))
								success.set(false);
						} catch (Exception e) {
							success.set(false);
						}
					}
				});
			}
		}

		// Free up all the textures we're holding on to
		for (int i = 0; i < textures.length; i++) {
			textures[i].texture = null;
		}

		// We just allocated and freed a ridiculous amount of memory while baking.
		// Signal to the GC to clean up
		Runtime.getRuntime().gc();

		return success.get();
	}

	/**
	 * Blocking method to create and upload a baked texture for a single bake layer
	 *
	 * @param bakeType
	 *            Layer to bake
	 * @throws URISyntaxException
	 * @throws CloneNotSupportedException
	 * @returns True on success, otherwise false
	 */
	private boolean createBake(BakeType bakeType) throws URISyntaxException, CloneNotSupportedException {
		List<AvatarTextureIndex> textureIndices = bakeTypeToTextures(bakeType);
		Baker oven = new Baker(client, bakeType);

		for (int i = 0; i < textureIndices.size(); i++) {
			AvatarTextureIndex textureIndex = textureIndices.get(i);
			TextureData texture = textures[AvatarTextureIndex.getValue(textureIndex)];

			oven.addTexture(texture);
		}

		long start = System.currentTimeMillis();
		;
		oven.bake();
		logger.debug("Baking " + bakeType + " took " + (System.currentTimeMillis() - start) + "ms");

		UUID newAssetID = UUID.ZERO;
		int retries = UPLOAD_RETRIES;

		do {
			try {
				newAssetID = uploadBake(new AssetTexture(oven.getBakedTexture()).getAssetData());
			} catch (IOException e) {
				return false;
			} catch (InterruptedException e) {
				return false;
			}
			--retries;
		} while (UUID.isZeroOrNull(newAssetID) && retries > 0);

		textures[bakeTypeToAgentTextureIndex(bakeType).getValue()].textureID = newAssetID;

		if (UUID.isZeroOrNull(newAssetID)) {
			logger.warn(GridClient.Log("Failed uploading bake " + bakeType, client));
			return false;
		}
		return true;
	}

	/**
	 * Blocking method to upload a baked texture
	 *
	 * @param textureData
	 *            Five channel JPEG2000 texture data to upload
	 * @returns UUID of the newly created asset on success, otherwise UUID.Zero
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private UUID uploadBake(byte[] textureData) throws IOException, InterruptedException {
		final TimeoutEvent<UUID> uploadEvent = new TimeoutEvent<>();

		client.assets.requestUploadBakedTexture(textureData, new BakedTextureUploadedCallback() {
			@Override
			public void callback(UUID newAssetID) {
				uploadEvent.set(newAssetID);
			}
		});

		// FIXME: evaluate the need for timeout here, RequestUploadBakedTexture() will
		// timeout either on Client.Settings.TRANSFER_TIMEOUT or
		// Client.Settings.CAPS_TIMEOUT
		// depending on which upload method is used.
		UUID bakeID = uploadEvent.waitOne(UPLOAD_TIMEOUT);
		return bakeID != null ? bakeID : UUID.ZERO;
	}

	/**
	 * Initate server baking process
	 *
	 * @throws Exception
	 *
	 * @returns True if the server baking was successful
	 */
	private boolean updateAvatarAppearance() throws Exception {
		URI url = client.network.getCapabilityURI("UpdateAvatarAppearance");
		if (url == null) {
			return false;
		}

		InventoryFolder cof = client.inventory.findFolderForType(FolderType.CurrentOutfit);
		if (cof == null) {
			client.inventory.folderContents(client.inventory.getRootNode(false).itemID, client.agent.getAgentID(), true,
					true, InventorySortOrder.ByDate, true, client.settings.CAPS_TIMEOUT);
			cof = client.inventory.findFolderForType(FolderType.CurrentOutfit);
		}

		if (cof == null) {
			// TODO: create Current Outfit Folder
			return false;
		}

		CapsClient capsRequest = new CapsClient(client, "UpdateAvatarAppearance");
		OSDMap request = new OSDMap(1);
		request.put("cof_version", OSD.fromInteger(cof.version));

		String msg = "Setting server side baking failed";
		OSD res = capsRequest.getResponse(url, request, OSDFormat.Xml, client.settings.CAPS_TIMEOUT * 2);
		if (res != null && res instanceof OSDMap) {
			OSDMap result = (OSDMap) res;
			if (result.get("success").asBoolean()) {
				logger.info(GridClient.Log("Successfully set appearance", client));
				// TODO: Set local visual params and baked textures based on the result here
				return true;
			}
			if (result.containsKey("error")) {
				msg += ": " + result.get("error").asString();
			}
		}
		capsRequest.shutdown(true);
		logger.error(GridClient.Log(msg, client));

		return false;
	}

	/**
	 * Create an AgentSetAppearance packet from Wearables data and the Textures
	 * array and send it
	 *
	 * @throws Exception
	 */
	private void requestAgentSetAppearance() throws Exception {
		AgentSetAppearancePacket set = new AgentSetAppearancePacket();
		set.AgentData.AgentID = client.agent.getAgentID();
		set.AgentData.SessionID = client.agent.getSessionID();
		set.AgentData.SerialNum = setAppearanceSerialNum.incrementAndGet();

		// Visual params used in the agent height calculation
		float agentSizeVPHeight = 0.0f;
		float agentSizeVPHeelHeight = 0.0f;
		float agentSizeVPPlatformHeight = 0.0f;
		float agentSizeVPHeadSize = 0.5f;
		float agentSizeVPLegLength = 0.0f;
		float agentSizeVPNeckLength = 0.0f;
		float agentSizeVPHipLength = 0.0f;

		synchronized (wearables) {

			int vpIndex = 0;
			int nrParams;
			boolean wearingPhysics = wearables.containsKey(WearableType.Physics);

			if (wearingPhysics) {
				nrParams = 251;
			} else {
				nrParams = 218;
			}

			set.ParamValue = new byte[nrParams];

			for (Entry<Integer, VisualParam> kvp : VisualParams.params.entrySet()) {
				VisualParam vp = kvp.getValue();
				float paramValue = 0f;
				boolean found = false;

				// Try and find this value in our collection of downloaded wearables
				for (Entry<WearableType, List<WearableData>> entry : wearables.entrySet()) {
					for (WearableData data : entry.getValue()) {
						if (data.asset != null && data.asset.params.containsKey(vp.paramID)) {
							paramValue = data.asset.params.get(vp.paramID);
							found = true;
							break;
						}
					}
					if (found)
						break;
				}

				// Use a default value if we don't have one set for it
				if (!found)
					paramValue = vp.defaultValue;

				// Only Group-0 parameters are sent in AgentSetAppearance packets
				if (kvp.getValue().group == 0) {
					set.ParamValue[vpIndex] = Helpers.floatToByte(paramValue, vp.minValue, vp.maxValue);
					++vpIndex;
				}

				// Check if this is one of the visual params used in the agent height
				// calculation
				switch (vp.paramID) {
				case 33:
					agentSizeVPHeight = paramValue;
					break;
				case 198:
					agentSizeVPHeelHeight = paramValue;
					break;
				case 503:
					agentSizeVPPlatformHeight = paramValue;
					break;
				case 682:
					agentSizeVPHeadSize = paramValue;
					break;
				case 692:
					agentSizeVPLegLength = paramValue;
					break;
				case 756:
					agentSizeVPNeckLength = paramValue;
					break;
				case 842:
					agentSizeVPHipLength = paramValue;
					break;
				default:
					break;
				}

				if (vpIndex == nrParams)
					break;
			}

			myVisualParameters = new byte[set.ParamValue.length];
			System.arraycopy(myVisualParameters, 0, set.ParamValue, 0, set.ParamValue.length);

			TextureEntry te = new TextureEntry(DEFAULT_AVATAR_TEXTURE);

			for (int i = 0; i < textures.length; i++) {
				TextureEntry.TextureEntryFace face = te.createFace(i);
				if ((i == 0 || i == 5 || i == 6) && !client.settings.CLIENT_IDENTIFICATION_TAG.equals(UUID.ZERO)) {
					face.setTextureID(client.settings.CLIENT_IDENTIFICATION_TAG);
					logger.debug(GridClient.Log(
							"Sending client identification tag: " + client.settings.CLIENT_IDENTIFICATION_TAG, client));
				} else if (textures[i].textureID != UUID.ZERO) {
					face.setTextureID(textures[i].textureID);
					logger.debug(
							GridClient.Log("Sending texture entry for " + i + " to " + textures[i].textureID, client));
				}
			}

			set.ObjectData.setTextureEntry(te.getBytes());
			myTextures = te;

			set.WearableData = new AgentSetAppearancePacket.WearableDataBlock[BakeType.getNumValues()];

			// Build hashes for each of the bake layers from the individual components
			for (BakeType bakeType : BakeType.values()) {
				if (bakeType == BakeType.Unknown)
					continue;

				UUID hash = UUID.ZERO;

				for (int wearableIndex = 0; wearableIndex < WEARABLES_PER_LAYER; wearableIndex++) {
					WearableType type = WEARABLE_BAKE_MAP[bakeType.getValue()][wearableIndex];

					if (type != WearableType.Invalid && wearables.containsKey(type)) {
						for (WearableData wearable : wearables.get(type)) {
							hash = UUID.xor(hash, wearable.assetID);
						}
					}
				}

				if (!hash.equals(UUID.ZERO)) {
					// Hash with our magic value for this baked layer
					hash = UUID.xor(hash, BAKED_TEXTURE_HASH[bakeType.getValue()]);
				}

				// Tell the server what cached texture assetID to use for each bake layer
				AgentSetAppearancePacket.WearableDataBlock block = set.new WearableDataBlock();
				block.TextureIndex = bakeTypeToAgentTextureIndex(bakeType).getValue();
				block.CacheID = hash;
				set.WearableData[bakeType.getValue()] = block;
				logger.debug(GridClient.Log("Sending TextureIndex " + bakeType + " with CacheID " + hash, client));
			}

			// Takes into account the Shoe Heel/Platform offsets but not the HeadSize
			// offset. Seems to work.
			double agentSizeBase = 1.706;

			// The calculation for the HeadSize scalar may be incorrect, but it seems to
			// work
			double agentHeight = agentSizeBase + (agentSizeVPLegLength * .1918) + (agentSizeVPHipLength * .0375)
					+ (agentSizeVPHeight * .12022) + (agentSizeVPHeadSize * .01117) + (agentSizeVPNeckLength * .038)
					+ (agentSizeVPHeelHeight * .08) + (agentSizeVPPlatformHeight * .07);

			set.AgentData.Size = new Vector3(0.45f, 0.6f, (float) agentHeight);

			if (client.settings.getBool(LibSettings.AVATAR_TRACKING)) {
				Avatar me = client.network.getCurrentSim().getObjectsAvatars().get(client.agent.getLocalID());
				if (me != null) {
					me.textures = myTextures;
					me.visualParameters = myVisualParameters;
				}
			}
		}
		client.network.sendPacket(set);
		logger.debug(GridClient.Log("Sent AgentSetAppearance packet", client));
	}

	private void delayedRequestSetAppearance() {
		if (rebakeScheduleTimer == null) {
			rebakeScheduleTimer = new Timer("DelayedRequestSetAppearance");
		}
		rebakeScheduleTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				requestSetAppearance(true);
			}
		}, REBAKE_DELAY);
	}

	public boolean getFolderWearables(String[] folderPath, List<InventoryWearable> wearables,
			List<InventoryItem> attachments) throws Exception {
		UUID folder = client.inventory.findObjectByPath(client.inventory.getRootNode(false).itemID,
				client.agent.getAgentID(), String.join("/", folderPath), INVENTORY_TIMEOUT);

		if (folder != UUID.ZERO) {
			return getFolderWearables(folder, wearables, attachments);
		}
		logger.error(GridClient.Log("Failed to resolve outfit folder path " + folderPath, client));
		return false;
	}

	private boolean getFolderWearables(UUID folder, List<InventoryWearable> wearables, List<InventoryItem> attachments)
			throws Exception {
		List<InventoryNode> objects = client.inventory.folderContents(folder, client.agent.getAgentID(), false, true,
				InventorySortOrder.ByName, INVENTORY_TIMEOUT);

		if (objects != null) {
			for (InventoryNode ib : objects) {
				if (ib.getType() == InventoryType.Wearable) {
					logger.debug(GridClient.Log("Adding wearable " + ib.name, client));
					wearables.add((InventoryWearable) ib);
				} else if (ib.getType() == InventoryType.Attachment) {
					logger.debug(GridClient.Log("Adding attachment (attachment) " + ib.name, client));
					attachments.add((InventoryItem) ib);
				} else if (ib.getType() == InventoryType.Object) {
					logger.debug(GridClient.Log("Adding attachment (object) " + ib.name, client));
					attachments.add((InventoryItem) ib);
				} else {
					logger.debug(GridClient.Log("Ignoring inventory item " + ib.name, client));
				}
			}
		} else {
			logger.error(GridClient.Log("Failed to download folder contents of + " + folder, client));
			return false;
		}

		return true;
	}

	private void handleAgentWearablesUpdate(Packet packet, Simulator simulator) {
		boolean changed = false;
		AgentWearablesUpdatePacket update = (AgentWearablesUpdatePacket) packet;

		synchronized (wearables) {

			for (AgentWearablesUpdatePacket.WearableDataBlock block : update.WearableData) {
				WearableType type = WearableType.setValue(block.WearableType);

				if (!block.AssetID.equals(UUID.ZERO)) {
					if (wearables.containsKey(type)) {
						boolean match = false;
						for (WearableData wearable : wearables.get(type)) {
							if (wearable != null && wearable.assetID.equals(block.AssetID)
									&& wearable.itemID.equals(block.ItemID)) {
								// Same wearable as before
								match = true;
								break;
							}
						}
						changed = !match;
						if (changed)
							break;
					} else {
						// A wearable is now set for this index
						changed = true;
						break;
					}
				} else if (wearables.containsKey(type)) {
					// This index is now empty
					changed = true;
					break;
				}
			}
		}

		if (changed) {
			logger.debug(GridClient.Log("New wearables received in AgentWearablesUpdate", client));
			synchronized (wearables) {
				wearables.clear();

				for (int i = 0; i < update.WearableData.length; i++) {
					AgentWearablesUpdatePacket.WearableDataBlock block = update.WearableData[i];

					if (!block.AssetID.equals(UUID.ZERO)) {
						WearableType type = WearableType.setValue(block.WearableType);

						WearableData data = new WearableData();
						data.asset = null;
						data.assetID = block.AssetID;
						data.assetType = wearableTypeToAssetType(type);
						data.itemID = block.ItemID;
						data.wearableType = type;

						// Add this wearable to our collection
						wearables.put(type, data);
					}
				}
			}
			// Fire the callback
			onAgentWearablesReply.dispatch(new AgentWearablesReplyCallbackArgs());
		} else {
			logger.debug(GridClient.Log("Duplicate AgentWearablesUpdate received, discarding", client));
		}
	}

	private void handleRebakeAvatarTextures(Packet packet, Simulator simulator) {
		RebakeAvatarTexturesPacket rebake = (RebakeAvatarTexturesPacket) packet;

		// allow the library to do the rebake
		if (sendAppearanceUpdates) {
			requestSetAppearance(true);
		}
		onRebakeAvatarReply.dispatch(new RebakeAvatarTexturesCallbackArgs(rebake.TextureID));
	}

	private void handleAgentCachedTextureResponse(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException {
		AgentCachedTextureResponsePacket response = (AgentCachedTextureResponsePacket) packet;

		for (AgentCachedTextureResponsePacket.WearableDataBlock block : response.WearableData) {
			AvatarTextureIndex index = AvatarTextureIndex.setValue(block.TextureIndex);

			logger.debug(GridClient.Log("Cache response for " + index + ", TextureID = " + block.TextureID, client));

			TextureData tex = textures[index.getValue()];
			if (!block.TextureID.equals(UUID.ZERO)) {
				// A simulator has a cache of this bake layer
				tex.textureID = block.TextureID;
				tex.host = Helpers.bytesToString(block.getHostName());
			} else {
				// TODO:FIXME The server does not have a cache of this bake layer, request
				// upload
			}
		}
		if (onAgentCachedBakesReply.count() > 0)
			onAgentCachedBakesReply.dispatch(
					new AgentCachedBakesReplyCallbackArgs(response.AgentData.SerialNum, response.WearableData.length));
	}

	private class Network_OnEventQueueRunning implements Callback<EventQueueRunningCallbackArgs> {
		@Override
		public boolean callback(EventQueueRunningCallbackArgs e) {
			if (sendAppearanceUpdates && e.getSimulator().equals(client.network.getCurrentSim())) {
				// Update appearance each time we enter a new sim and capabilities have been
				// retrieved
				logger.warn(GridClient.Log("Starting AppearanceRequest from server " + e.getSimulator().getSimName(),
						client));
				requestSetAppearance(false);
			}
			return false;
		}
	}

	private class Network_OnDisconnected implements Callback<DisconnectedCallbackArgs> {
		@SuppressWarnings("deprecation")
		@Override
		public boolean callback(DisconnectedCallbackArgs e) {
			if (rebakeScheduleTimer != null) {
				rebakeScheduleTimer.cancel();
				rebakeScheduleTimer = null;
			}

			if (appearanceThread != null) {
				if (appearanceThread.isAlive()) {
					appearanceThread.stop();
				}
				appearanceThread = null;
			}
			return true;
		}
	}

	/**
	 * Converts a WearableType to a bodypart or clothing WearableType
	 *
	 * @param type
	 *            A WearableType
	 * @returns AssetType.Bodypart or AssetType.Clothing or AssetType.Unknown
	 */
	public static AssetType wearableTypeToAssetType(WearableType type) {
		switch (type) {
		case Shape:
		case Skin:
		case Hair:
		case Eyes:
			return AssetType.Bodypart;
		case Shirt:
		case Pants:
		case Shoes:
		case Socks:
		case Jacket:
		case Gloves:
		case Undershirt:
		case Underpants:
		case Skirt:
		case Tattoo:
		case Alpha:
		case Physics:
			return AssetType.Clothing;
		default:
			return AssetType.Unknown;
		}
	}

	/**
	 * Converts a BakeType to the corresponding baked texture slot in
	 * AvatarTextureIndex
	 *
	 * @param index
	 *            A BakeType
	 * @returns The AvatarTextureIndex slot that holds the given BakeType
	 */
	public static AvatarTextureIndex bakeTypeToAgentTextureIndex(BakeType index) {
		switch (index) {
		case Head:
			return AvatarTextureIndex.HeadBaked;
		case UpperBody:
			return AvatarTextureIndex.UpperBaked;
		case LowerBody:
			return AvatarTextureIndex.LowerBaked;
		case Eyes:
			return AvatarTextureIndex.EyesBaked;
		case Skirt:
			return AvatarTextureIndex.SkirtBaked;
		case Hair:
			return AvatarTextureIndex.HairBaked;
		default:
			return AvatarTextureIndex.Unknown;
		}
	}

	/**
	 * Gives the layer number that is used for morph mask
	 *
	 * @param bakeType
	 *            A BakeType
	 * @returns Which layer number as defined in BakeTypeToTextures is used for
	 *          morph mask
	 */
	public static AvatarTextureIndex morphLayerForBakeType(BakeType bakeType) {
		// Indexes return here correspond to those returned
		// in BakeTypeToTextures(), those two need to be in sync.
		// Which wearable layer is used for morph is defined in avatar_lad.xml
		// by looking for <layer> that has <morph_mask> defined in it, and
		// looking up which wearable is defined in that layer. Morph mask
		// is never combined, it's always a straight copy of one single clothing
		// item's alpha channel per bake.
		switch (bakeType) {
		case Head:
			return AvatarTextureIndex.Hair; // hair
		case UpperBody:
			return AvatarTextureIndex.UpperShirt; // shirt
		case LowerBody:
			return AvatarTextureIndex.LowerPants; // lower pants
		case Skirt:
			return AvatarTextureIndex.Skirt; // skirt
		case Hair:
			return AvatarTextureIndex.Hair; // hair
		default:
			return AvatarTextureIndex.Unknown;
		}
	}

	/**
	 * Converts a BakeType to a list of the texture slots that make up that bake
	 *
	 * @param bakeType
	 *            A BakeType
	 * @returns A list of texture slots that are inputs for the given bake
	 */
	public static List<AvatarTextureIndex> bakeTypeToTextures(BakeType bakeType) {
		List<AvatarTextureIndex> textures = new ArrayList<>();

		switch (bakeType) {
		case Head:
			textures.add(AvatarTextureIndex.HeadBodypaint);
			textures.add(AvatarTextureIndex.HeadTattoo);
			// textures.add(AvatarTextureIndex.Hair);
			textures.add(AvatarTextureIndex.HeadAlpha);
			break;
		case UpperBody:
			textures.add(AvatarTextureIndex.UpperBodypaint);
			textures.add(AvatarTextureIndex.UpperTattoo);
			textures.add(AvatarTextureIndex.UpperGloves);
			textures.add(AvatarTextureIndex.UpperUndershirt);
			textures.add(AvatarTextureIndex.UpperShirt);
			textures.add(AvatarTextureIndex.UpperJacket);
			textures.add(AvatarTextureIndex.UpperAlpha);
			break;
		case LowerBody:
			textures.add(AvatarTextureIndex.LowerBodypaint);
			textures.add(AvatarTextureIndex.LowerTattoo);
			textures.add(AvatarTextureIndex.LowerUnderpants);
			textures.add(AvatarTextureIndex.LowerSocks);
			textures.add(AvatarTextureIndex.LowerShoes);
			textures.add(AvatarTextureIndex.LowerPants);
			textures.add(AvatarTextureIndex.LowerJacket);
			textures.add(AvatarTextureIndex.LowerAlpha);
			break;
		case Eyes:
			textures.add(AvatarTextureIndex.EyesIris);
			textures.add(AvatarTextureIndex.EyesAlpha);
			break;
		case Skirt:
			textures.add(AvatarTextureIndex.Skirt);
			break;
		case Hair:
			textures.add(AvatarTextureIndex.Hair);
			textures.add(AvatarTextureIndex.HairAlpha);
			break;
		default:
			break;
		}
		return textures;
	}

}