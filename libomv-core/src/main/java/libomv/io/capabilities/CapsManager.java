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
package libomv.io.capabilities;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsToPacket;
import libomv.capabilities.IMessage;
import libomv.io.GridClient;
import libomv.io.SimulatorManager;
import libomv.packets.Packet;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;

/**
 * Capabilities is the name of the bi-directional HTTP REST protocol used to
 * communicate non real-time transactions such as teleporting or group messaging
 */
public class CapsManager extends Thread {
	private static final Logger logger = Logger.getLogger(CapsManager.class);

	public class CapabilitiesReceivedCallbackArgs implements CallbackArgs {
		// The simulator that received a capability
		private SimulatorManager simulator;

		public CapabilitiesReceivedCallbackArgs(SimulatorManager simulator) {
			this.simulator = simulator;
		}

		public SimulatorManager getSimulator() {
			return simulator;
		}

	}

	private enum CapsState {
		Seeding, Running, Closing, Closed;
	}

	public CallbackHandler<CapabilitiesReceivedCallbackArgs> onCapabilitiesReceived = new CallbackHandler<>();

	/* Reference to the simulator this system is connected to */
	private SimulatorManager simulator;

	/*
	 * Asynchronous HTTP Client used for both the seedRequest as well as the
	 * eventQueue
	 */
	private CapsClient client;

	/* Capabilities URI this system was initialized with */
	private String seedCapsURI;

	private Map<String, URI> capabilities = new Hashtable<>();

	/*
	 * Whether the capabilities event queue is connected and listening for incoming
	 * events
	 */
	private CapsState running = CapsState.Closed;

	/**
	 * Default constructor
	 *
	 * @param simulator
	 * @param seedcaps
	 * @throws IOReactorException
	 */
	public CapsManager(SimulatorManager simulator, String seedcaps) throws IOReactorException {
		super("CapsManager");
		this.simulator = simulator;

		this.seedCapsURI = seedcaps;
		this.client = new CapsClient(simulator.getClient(), CapsEventType.EventQueueGet.toString());

		start();
	}

	public final String getSeedCapsURI() {
		return seedCapsURI;
	}

	/**
	 * Request the URI of a named capability
	 *
	 * @param capability
	 *            Name of the capability to request
	 * @return The URI of the requested capability, or String. Empty if the
	 *         capability does not exist
	 */
	public final URI capabilityURI(String capability) {
		synchronized (capabilities) {
			return capabilities.get(capability);
		}
	}

	private void setState(CapsState state) {
		synchronized (running) {
			running = state;
		}
	}

	private final boolean isClosed() {
		synchronized (running) {
			return running == CapsState.Closed;
		}
	}

	private final boolean isActiveAndMakeClosing() {
		synchronized (running) {
			boolean active = running == CapsState.Seeding || running == CapsState.Running;
			if (active)
				running = CapsState.Closing;
			return active;
		}
	}

	private final boolean isSeedingAndMakeRun() {
		synchronized (running) {
			boolean seeding = running == CapsState.Seeding;
			if (seeding)
				running = CapsState.Running;
			return seeding;
		}
	}

	private final boolean isSeeding() {
		synchronized (running) {
			return running == CapsState.Seeding;
		}
	}

	public final boolean isRunning() {
		synchronized (running) {
			return running == CapsState.Running;
		}
	}

	public final boolean isClosing() {
		synchronized (running) {
			return running == CapsState.Closing;
		}
	}

	public final void disconnect(boolean immediate) throws InterruptedException {
		if (isActiveAndMakeClosing()) {
			client.cancel(immediate);
			logger.info(GridClient.Log(
					"Caps system for " + simulator.getName() + " is " + (immediate ? "aborting" : "disconnecting"),
					simulator.getClient()));
		}
	}

	@Override
	public void run() {
		setState(CapsState.Seeding);
		try {
			URI eventQueueGet = null;
			do {
				eventQueueGet = makeSeedRequest();
			} while (isSeeding() && eventQueueGet == null);

			runEventQueueLoop(eventQueueGet);
		} catch (Exception ex) {
			logger.error(GridClient.Log("Couldn't startup capability system", simulator.getClient()), ex);
			setState(CapsState.Closed);
		} finally {
			try {
				client.shutdown(true);
			} catch (InterruptedException | IOException ex) {
			} finally {
				client = null;
			}
		}
	}

	private URI makeSeedRequest()
			throws InterruptedException, ExecutionException, TimeoutException, IOException, URISyntaxException {
		if (simulator != null && simulator.getClient().network.getConnected()) {
			// Create a request list
			OSDArray req = new OSDArray();
			// This list can be updated by using the following command to obtain a
			// current list of capabilities the official linden viewer supports:
			// wget -q -O -
			// https://bitbucket.org/lindenlab/viewer-release/raw/default/indra/newview/llviewerregion.cpp
			// | grep 'capabilityNames.append' | sed 's/^[
			// \t]*//;s/capabilityNames.append("/req.Add("/'
			req.add(OSD.fromString("AgentPreferences"));
			req.add(OSD.fromString("AgentState"));
			req.add(OSD.fromString("AttachmentResources"));
			req.add(OSD.fromString("AvatarPickerSearch"));
			req.add(OSD.fromString("AvatarRenderInfo"));
			req.add(OSD.fromString("CharacterProperties"));
			req.add(OSD.fromString("ChatSessionRequest"));
			req.add(OSD.fromString("CopyInventoryFromNotecard"));
			req.add(OSD.fromString("CreateInventoryCategory"));
			req.add(OSD.fromString("DispatchRegionInfo"));
			req.add(OSD.fromString("DirectDelivery"));
			req.add(OSD.fromString("EnvironmentSettings"));
			req.add(OSD.fromString("EstateChangeInfo"));
			req.add(OSD.fromString("EventQueueGet"));
			req.add(OSD.fromString("FacebookConnect"));
			req.add(OSD.fromString("FlickrConnect"));
			req.add(OSD.fromString("TwitterConnect"));
			req.add(OSD.fromString("FetchInventory2"));
			req.add(OSD.fromString("FetchInventoryDescendents2"));
			req.add(OSD.fromString("FetchLib2"));
			req.add(OSD.fromString("FetchLibDescendents2"));
			req.add(OSD.fromString("GetDisplayNames"));
			req.add(OSD.fromString("GetExperiences"));
			req.add(OSD.fromString("AgentExperiences"));
			req.add(OSD.fromString("FindExperienceByName"));
			req.add(OSD.fromString("GetExperienceInfo"));
			req.add(OSD.fromString("GetAdminExperiences"));
			req.add(OSD.fromString("GetCreatorExperiences"));
			req.add(OSD.fromString("ExperiencePreferences"));
			req.add(OSD.fromString("GroupExperiences"));
			req.add(OSD.fromString("UpdateExperience"));
			req.add(OSD.fromString("IsExperienceAdmin"));
			req.add(OSD.fromString("IsExperienceContributor"));
			req.add(OSD.fromString("RegionExperiences"));
			req.add(OSD.fromString("GetMesh"));
			req.add(OSD.fromString("GetMesh2"));
			req.add(OSD.fromString("GetMetadata"));
			req.add(OSD.fromString("GetObjectCost"));
			req.add(OSD.fromString("GetObjectPhysicsData"));
			req.add(OSD.fromString("GetTexture"));
			req.add(OSD.fromString("GroupAPIv1"));
			req.add(OSD.fromString("GroupMemberData"));
			req.add(OSD.fromString("GroupProposalBallot"));
			req.add(OSD.fromString("HomeLocation"));
			req.add(OSD.fromString("IncrementCOFVersion"));
			req.add(OSD.fromString("LandResources"));
			req.add(OSD.fromString("LSLSyntax"));
			req.add(OSD.fromString("MapLayer"));
			req.add(OSD.fromString("MapLayerGod"));
			req.add(OSD.fromString("MeshUploadFlags"));
			req.add(OSD.fromString("NavMeshGenerationStatus"));
			req.add(OSD.fromString("NewFileAgentInventory"));
			req.add(OSD.fromString("ObjectMedia"));
			req.add(OSD.fromString("ObjectMediaNavigate"));
			req.add(OSD.fromString("ObjNavMeshProperties"));
			req.add(OSD.fromString("ParcelPropertiesUpdate"));
			req.add(OSD.fromString("ParcelVoiceInfoRequest"));
			req.add(OSD.fromString("ProductInfoRequest"));
			req.add(OSD.fromString("ProvisionVoiceAccountRequest"));
			req.add(OSD.fromString("RemoteParcelRequest"));
			req.add(OSD.fromString("RenderMaterials"));
			req.add(OSD.fromString("RequestTextureDownload"));
			req.add(OSD.fromString("ResourceCostSelected"));
			req.add(OSD.fromString("RetrieveNavMeshSrc"));
			req.add(OSD.fromString("SearchStatRequest"));
			req.add(OSD.fromString("SearchStatTracking"));
			req.add(OSD.fromString("SendPostcard"));
			req.add(OSD.fromString("SendUserReport"));
			req.add(OSD.fromString("SendUserReportWithScreenshot"));
			req.add(OSD.fromString("ServerReleaseNotes"));
			req.add(OSD.fromString("SetDisplayName"));
			req.add(OSD.fromString("SimConsoleAsync"));
			req.add(OSD.fromString("SimulatorFeatures"));
			req.add(OSD.fromString("StartGroupProposal"));
			req.add(OSD.fromString("TerrainNavMeshProperties"));
			req.add(OSD.fromString("TextureStats"));
			req.add(OSD.fromString("UntrustedSimulatorMessage"));
			req.add(OSD.fromString("UpdateAgentInformation"));
			req.add(OSD.fromString("UpdateAgentLanguage"));
			req.add(OSD.fromString("UpdateAvatarAppearance"));
			req.add(OSD.fromString("UpdateGestureAgentInventory"));
			req.add(OSD.fromString("UpdateGestureTaskInventory"));
			req.add(OSD.fromString("UpdateNotecardAgentInventory"));
			req.add(OSD.fromString("UpdateNotecardTaskInventory"));
			req.add(OSD.fromString("UpdateScriptAgent"));
			req.add(OSD.fromString("UpdateScriptTask"));
			req.add(OSD.fromString("UploadBakedTexture"));
			req.add(OSD.fromString("ViewerMetrics"));
			req.add(OSD.fromString("ViewerStartAuction"));
			req.add(OSD.fromString("ViewerStats"));
			req.add(OSD.fromString("WebFetchInventoryDescendents"));
			// AIS3
			req.add(OSD.fromString("InventoryAPIv3"));
			req.add(OSD.fromString("LibraryAPIv3"));

			try {
				OSD result = client.getResponse(new URI(seedCapsURI), req, OSD.OSDFormat.Xml,
						simulator.getClient().settings.CAPS_TIMEOUT);
				if (result != null && result.getType().equals(OSDType.Map)) {
					URI eventQueueGet = null;
					OSDMap respTable = (OSDMap) result;
					synchronized (capabilities) {
						for (Map.Entry<String, OSD> entry : respTable.entrySet()) {
							OSD value = entry.getValue();
							if (value.getType() == OSD.OSDType.String || value.getType() == OSD.OSDType.URI) {
								URI capsURI = value.asUri();
								if (entry.getKey().equals("EventQueueGet"))
									eventQueueGet = capsURI;
								capabilities.put(entry.getKey(), capsURI);
							}
						}
					}

					onCapabilitiesReceived.dispatch(new CapabilitiesReceivedCallbackArgs(simulator));

					if (eventQueueGet == null) {
						logger.warn(GridClient.Log(
								"Caps seed: Returned capabilities does not contain an EventQueueGet caps",
								simulator.getClient()));
					}
					/* when successful: return and startup eventqueue */
					return eventQueueGet;
				}
			} catch (HttpResponseException ex) {
				if (ex.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
					logger.error(GridClient.Log(
							"Caps seed: Seed capability returned a 404 status, capability system is aborting",
							simulator.getClient()));
					throw ex;
				}
				logger.warn(
						GridClient.Log("Caps seed: Seed capability returned an error status", simulator.getClient()),
						ex);
			}
		}
		/* Retry seed request */
		return null;
	}

	private void runEventQueueLoop(URI eventQueueGet) {
		logger.info(GridClient.Log("Starting event queue for " + simulator.getName(), simulator.getClient()));

		OSDMap osdRequest = new OSDMap(2);
		osdRequest.put("ack", new OSD());
		Random random = new Random();
		OSDArray events = null;
		int errorCount = 0;

		while (!isClosed()) {
			osdRequest.put("done", OSD.fromBoolean(isClosing()));

			// Start or resume the connection
			Future<OSD> request = client.executeHttpPost(eventQueueGet, osdRequest, OSD.OSDFormat.Xml, null,
					CapsClient.TIMEOUT_INFINITE);

			// Handle incoming events from previous request
			if (events != null && events.size() > 0) {
				// Fire callbacks for each event received
				ListIterator<OSD> iterator = events.listIterator();
				while (iterator.hasNext()) {
					OSDMap evt = (OSDMap) iterator.next();
					OSD osd = evt.get("body");
					if (osd.getType().equals(OSDType.Map)) {
						OSDMap body = (OSDMap) osd;
						String eventName = evt.get("message").asString();

						IMessage message = simulator.getClient().messages.decodeEvent(eventName, body);
						if (message != null) {
							logger.debug(GridClient.Log("Caps message: " + eventName + ".", simulator.getClient()));
							simulator.getClient().network.distributeCaps(simulator, message);
						} else {
							logger.warn(GridClient.Log(
									"Caps loop: No Message handler exists for event " + eventName
											+ ". Unable to decode. Will try Generic Handler next",
									simulator.getClient()));
							logger.debug(GridClient.Log(
									"Caps loop: Please report this information to http://sourceforge.net/tracker/?group_id=387920&atid=1611745\n"
											+ body,
									simulator.getClient()));

							// try generic decoder next which takes a caps event and tries to match it to an
							// existing packet
							Packet packet = CapsToPacket.buildPacket(eventName, body);
							if (packet != null) {
								logger.debug(GridClient.Log("Caps loop: Serializing " + packet.getType()
										+ " capability with generic handler", simulator.getClient()));
								simulator.getClient().network.distributePacket(simulator, packet);
							} else {
								logger.warn(GridClient.Log(
										"Caps loop: No Packet or Message handler exists for " + eventName,
										simulator.getClient()));
							}
						}
					}
				}
				events = null;
			}

			if (!isClosing()) {
				try {
					OSD result = request.get(simulator.getClient().settings.CAPS_TIMEOUT, TimeUnit.MILLISECONDS);
					if (result == null) {
						++errorCount;
						logger.warn(GridClient.Log("Caps loop: Got an unparseable response from the event queue!",
								simulator.getClient()));
					} else if (result instanceof OSDMap) {
						errorCount = 0;
						if (isSeedingAndMakeRun()) {
							simulator.getClient().network.raiseConnectedEvent(simulator);
						}

						OSDMap map = (OSDMap) result;
						osdRequest.put("ack", map.get("id"));
						events = (OSDArray) ((map.get("events") instanceof OSDArray) ? map.get("events") : null);
					}
				} catch (ExecutionException ex) {
					Throwable cause = ex.getCause();
					if (cause instanceof HttpResponseException) {
						int status = ((HttpResponseException) cause).getStatusCode();
						if (status == HttpStatus.SC_NOT_FOUND || status == HttpStatus.SC_GONE) {
							setState(CapsState.Closed);
							logger.info(GridClient.Log(
									String.format("Closing event queue at %s due to missing caps URI", eventQueueGet),
									simulator.getClient()));
						} else if (status == HttpStatus.SC_BAD_GATEWAY) {
							// This is not good (server) protocol design, but it's normal.
							// The EventQueue server is a proxy that connects to a Squid
							// cache which will time out periodically. The EventQueue
							// server interprets this as a generic error and returns a
							// 502 to us that we ignore
						} else {
							++errorCount;

							// Try to log a meaningful error message
							if (status != HttpStatus.SC_OK) {
								logger.warn(
										GridClient.Log(String.format("Unrecognized caps connection problem from %s: %d",
												eventQueueGet, status), simulator.getClient()));
							} else if (ex.getCause() != null) {
								logger.warn(GridClient.Log("Unrecognized internal caps exception from " + eventQueueGet
										+ ": " + ex.getCause().getMessage(), simulator.getClient()));
							} else {
								logger.warn(GridClient.Log(
										"Unrecognized caps exception from " + eventQueueGet + ": " + ex.getMessage(),
										simulator.getClient()));
							}
						}
					} else {
						++errorCount;

						logger.warn(GridClient.Log("No response from the event queue but no reported HTTP error either",
								simulator.getClient()), ex);
					}
				} catch (Exception ex) {
					++errorCount;
					logger.warn(GridClient.Log("Error retrieving response from the event queue request!",
							simulator.getClient()), ex);
				}
				request = null;

				if (errorCount > 0) {
					// On error backoff in increasing delay to not hammer the server
					try {
						Thread.sleep(random.nextInt(500 + (int) Math.pow(2, errorCount)));
					} catch (InterruptedException e) {
					}
				}
			} else {
				setState(CapsState.Closed);
			}
		}
		logger.info(GridClient.Log("Terminated event queue for " + simulator.getName(), simulator.getClient()));
	}
}
