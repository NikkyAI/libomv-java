/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Copyright (c) 2009-2012, Frederick Martian
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
package libomv.capabilities;

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

import libomv.Simulator;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.packets.Packet;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

/**
 * Capabilities is the name of the bi-directional HTTP REST protocol used to
 * communicate non real-time transactions such as teleporting or group messaging
 */
public class CapsManager extends Thread
{
	/* Reference to the simulator this system is connected to */
	private Simulator _Simulator;

	/* Asynchronous HTTP Client used for both the seedRequest as well as the eventQueue */
	private CapsClient _Client;

	/* Capabilities URI this system was initialized with */
	private String _SeedCapsURI;
	public final String getSeedCapsURI()
	{
		return _SeedCapsURI;
	}

	
	private Hashtable<String, URI> _Capabilities = new Hashtable<String, URI>();

	/**
	 * Request the URI of a named capability
	 * 
	 * @param capability
	 *            Name of the capability to request
	 * @return The URI of the requested capability, or String. Empty if the
	 *         capability does not exist
	 */
	public final URI capabilityURI(String capability)
	{
		synchronized (_Capabilities)
		{
			return _Capabilities.get(capability);
		}
	}

	private enum CapsState
	{
		Closed,
		Seeding,
		Running,
		Closing;
	}
	
	/* Whether the capabilities event queue is connected and listening for incoming events */
	private CapsState _Running = CapsState.Closed;

	private void setState(CapsState state)
	{
		synchronized (_Running)
		{
			_Running = state;
		}
	}

	private final boolean isClosed()
	{
		synchronized (_Running)
		{
			return _Running != CapsState.Closed;
		}
	}
	
	private final boolean isActive()
	{
		synchronized (_Running)
		{
			boolean active = _Running != CapsState.Closed && _Running != CapsState.Closing;
			if (active)
				_Running = CapsState.Closing;
			return active;
		}
	}

	private final boolean isSeeding()
	{
		synchronized (_Running)
		{
			boolean seed =  _Running == CapsState.Seeding;
			_Running = CapsState.Running;
			return seed;
		}
	}

	public final boolean isRunning()
	{
		synchronized (_Running)
		{
			return _Running == CapsState.Running;
		}
	}
	
	public final boolean isClosing()
	{
		synchronized (_Running)
		{
			return _Running == CapsState.Closing;
		}
	}

	/**
	 * Default constructor
	 * 
	 * @param simulator
	 * @param seedcaps
	 * @throws IOReactorException
	 */
	public CapsManager(Simulator simulator, String seedcaps) throws IOReactorException
	{
		super("CapsManager");
		_Simulator = simulator;
		_SeedCapsURI = seedcaps;
		_Client = new CapsClient("CapsManager Client");
		start();
	}

	public final void disconnect(boolean immediate) throws InterruptedException
	{
		if (isActive())
		{
			Logger.Log("Caps system for " + _Simulator.getName() + " is " + (immediate ? "aborting" : "disconnecting"), LogLevel.Info, _Simulator.getClient());
			if (_Client != null)
			{
				_Client.shutdown(immediate);
			}
		}
	}

	@Override
	public void run()
	{
		setState(CapsState.Seeding);
		try
		{
			URI eventQueueGet = null;
			do
			{
				eventQueueGet = makeSeedRequest();
			}
			while (isSeeding() && eventQueueGet == null);
			
			runEventQueueLoop(eventQueueGet);
		}
		catch (Exception ex)
		{
			Logger.Log("Couldn't startup capability system", LogLevel.Error, _Simulator.getClient(), ex);
			setState(CapsState.Closed);
		}
		_Client = null;
	}

	private URI makeSeedRequest() throws InterruptedException, ExecutionException, TimeoutException, IOException, URISyntaxException
	{
		if (_Simulator != null && _Simulator.getClient().Network.getConnected())
		{
			// Create a request list
			OSDArray req = new OSDArray();
			// This list can be updated by using the following command to obtain a
			// current list of capabilities the official linden viewer supports:
			// wget -q -O - https://bitbucket.org/lindenlab/viewer-development/raw/default/indra/newview/llviewerregion.cpp
			// | grep 'capabilityNames.append' | sed 's/^[ \t]*//;s/capabilityNames.append("/req.Add("/'
			req.add(OSD.FromString("AgentState"));
			req.add(OSD.FromString("AttachmentResources"));
			req.add(OSD.FromString("AvatarPickerSearch"));
			req.add(OSD.FromString("CharacterProperties"));
			req.add(OSD.FromString("ChatSessionRequest"));
			req.add(OSD.FromString("CopyInventoryFromNotecard"));
			req.add(OSD.FromString("CreateInventoryCategory"));
			req.add(OSD.FromString("DispatchRegionInfo"));
			req.add(OSD.FromString("EnvironmentSettings"));
			req.add(OSD.FromString("EstateChangeInfo"));
			req.add(OSD.FromString("EventQueueGet"));
			req.add(OSD.FromString("FetchInventory2"));
			req.add(OSD.FromString("FetchInventoryDescendents2"));
			req.add(OSD.FromString("FetchLib2"));
			req.add(OSD.FromString("FetchLibDescendents2"));
			req.add(OSD.FromString("GetDisplayNames"));
			req.add(OSD.FromString("GetMesh"));
			req.add(OSD.FromString("GetObjectCost"));
			req.add(OSD.FromString("GetObjectPhysicsData"));
			req.add(OSD.FromString("GetTexture"));
			req.add(OSD.FromString("GroupMemberData"));
			req.add(OSD.FromString("GroupProposalBallot"));
			req.add(OSD.FromString("HomeLocation"));
			req.add(OSD.FromString("LandResources"));
			req.add(OSD.FromString("MapLayer"));
			req.add(OSD.FromString("MapLayerGod"));
			req.add(OSD.FromString("MeshUploadFlags"));
			req.add(OSD.FromString("NavMeshGenerationStatus"));
			req.add(OSD.FromString("NewFileAgentInventory"));
			req.add(OSD.FromString("ObjectMedia"));
			req.add(OSD.FromString("ObjectMediaNavigate"));
			req.add(OSD.FromString("ObjNavMeshProperties"));
			req.add(OSD.FromString("ParcelPropertiesUpdate"));
			req.add(OSD.FromString("ParcelVoiceInfoRequest"));
			req.add(OSD.FromString("ProductInfoRequest"));
			req.add(OSD.FromString("ProvisionVoiceAccountRequest"));
			req.add(OSD.FromString("RemoteParcelRequest"));
			req.add(OSD.FromString("RequestTextureDownload"));
			req.add(OSD.FromString("ResourceCostSelected"));
			req.add(OSD.FromString("RetrieveNavMeshSrc"));
			req.add(OSD.FromString("SearchStatRequest"));
			req.add(OSD.FromString("SearchStatTracking"));
			req.add(OSD.FromString("SendPostcard"));
			req.add(OSD.FromString("SendUserReport"));
			req.add(OSD.FromString("SendUserReportWithScreenshot"));
			req.add(OSD.FromString("ServerReleaseNotes"));
			req.add(OSD.FromString("SetDisplayName"));
			req.add(OSD.FromString("SimConsoleAsync"));
			req.add(OSD.FromString("SimulatorFeatures"));
			req.add(OSD.FromString("StartGroupProposal"));
			req.add(OSD.FromString("TerrainNavMeshProperties"));
			req.add(OSD.FromString("TextureStats"));
			req.add(OSD.FromString("UntrustedSimulatorMessage"));
			req.add(OSD.FromString("UpdateAgentInformation"));
			req.add(OSD.FromString("UpdateAgentLanguage"));
			req.add(OSD.FromString("UpdateAvatarAppearance"));
			req.add(OSD.FromString("UpdateGestureAgentInventory"));
			req.add(OSD.FromString("UpdateGestureTaskInventory"));
			req.add(OSD.FromString("UpdateNotecardAgentInventory"));
			req.add(OSD.FromString("UpdateNotecardTaskInventory"));
			req.add(OSD.FromString("UpdateScriptAgent"));
			req.add(OSD.FromString("UpdateScriptTask"));
			req.add(OSD.FromString("UploadBakedTexture"));
			req.add(OSD.FromString("ViewerMetrics"));
			req.add(OSD.FromString("ViewerStartAuction"));
			req.add(OSD.FromString("ViewerStats"));
			req.add(OSD.FromString("WebFetchInventoryDescendents"));

			try
			{
				OSD result = _Client.getResponse(new URI(_SeedCapsURI), req, OSD.OSDFormat.Xml, _Simulator.getClient().Settings.CAPS_TIMEOUT);
				if (result != null && result.getType().equals(OSDType.Map))
				{
					URI eventQueueGet = null;
					OSDMap respTable = (OSDMap) result;
//					OSDMap meta = (OSDMap) respTable.remove("Metadata");
					synchronized (_Capabilities)
					{
						for (Map.Entry<String, OSD> entry : respTable.entrySet())
						{
							OSD value = entry.getValue();
							if (value.getType() == OSD.OSDType.String || value.getType() == OSD.OSDType.URI)
							{
								URI capsURI = value.AsUri();
								if (entry.getKey().equals("EventQueueGet"))
									eventQueueGet = capsURI; 
								_Capabilities.put(entry.getKey(), capsURI);
							}
						}
					}

					if (eventQueueGet == null)
					{
						Logger.Log("Returned capabilities does not contain an EventQueueGet caps", LogLevel.Warning, _Simulator.getClient());
					}
					/* when successful: return and startup eventqueue */
					return eventQueueGet;
				}
			}
			catch (HttpResponseException ex)
			{
				if (ex.getStatusCode() == HttpStatus.SC_NOT_FOUND)
				{
					Logger.Log("Seed capability returned a 404 status, capability system is aborting", LogLevel.Error, _Simulator.getClient());
					throw ex;
				}
				Logger.Log("Seed capability returned an error status", LogLevel.Warning, _Simulator.getClient(), ex);
			}
		}
		/* Retry seed request */
		return null;
	}
	
	private void runEventQueueLoop(URI eventQueueGet)
	{
		Logger.Log("Starting event queue for " + _Simulator.getName(), LogLevel.Info, _Simulator.getClient());

		OSDMap osdRequest = new OSDMap(2);
		osdRequest.put("ack", new OSD());
		Random random = new Random();
		OSDArray events = null;
		int errorCount = 0;

		while (isClosed())
		{
			osdRequest.put("done", OSD.FromBoolean(isClosing()));

			// Start or resume the connection
			Future<OSD> request = _Client.executeHttpPost(eventQueueGet, osdRequest, OSD.OSDFormat.Xml, null, CapsClient.TIMEOUT_INFINITE);
		
			// Handle incoming events from previous request
			if (events != null && events.size() > 0)
			{
				// Fire callbacks for each event received
				ListIterator<OSD> iterator = events.listIterator();
				while (iterator.hasNext())
				{
					OSDMap evt = (OSDMap) iterator.next();
					OSD osd = evt.get("body");
					if (osd.getType().equals(OSDType.Map))
					{
						OSDMap body = (OSDMap) osd;
						String name = evt.get("message").AsString();
						IMessage message = _Simulator.getClient().Messages.DecodeEvent(name, body);
						if (message != null)
						{
							Logger.Log("Caps message: " + name + ".", LogLevel.Debug, _Simulator.getClient());
							_Simulator.getClient().Network.DistributeCaps(_Simulator, message);

							// #region Stats Tracking
							if (_Simulator.getClient().Settings.TRACK_UTILIZATION)
							{
								/* TODO add Stats support to Client manager */
								// Simulator.getClient().Stats.Update(eventName,
								// libomv.Stats.Type.Message, 0,
								// body.ToString().Length);
							}
						}
						else
						{
							Logger.Log("No Message handler exists for event " + name + ". Unable to decode. Will try Generic Handler next",
									   LogLevel.Warning, _Simulator.getClient());
							Logger.Log("Please report this information to http://sourceforge.net/tracker/?group_id=387920&atid=1611745\n" + body,
									   LogLevel.Debug, _Simulator.getClient());

							// try generic decoder next which takes a caps event and tries to match it to an existing packet
							Packet packet = CapsToPacket.BuildPacket(name, body);
							if (packet != null)
							{
								Logger.Log("Serializing " + packet.getType() + " capability with generic handler", LogLevel.Debug, _Simulator.getClient());
								_Simulator.getClient().Network.DistributePacket(_Simulator, packet);
							}
							else
							{
								Logger.Log("No Packet or Message handler exists for " + name, LogLevel.Warning, _Simulator.getClient());
							}
						}
					}
				}
				events = null;
			}

			if (!isClosing())
			{
				try
				{
					OSD result = request.get(_Simulator.getClient().Settings.CAPS_TIMEOUT, TimeUnit.MILLISECONDS);
					if (result == null)
					{
						++errorCount;
						Logger.Log("Got an unparseable response from the event queue!", LogLevel.Warning, _Simulator.getClient());
					}
					else if (result instanceof OSDMap)
					{
						errorCount = 0;
						if (isSeeding())
						{
							_Simulator.getClient().Network.raiseConnectedEvent(_Simulator);
						}

						OSDMap map = (OSDMap) result;
						osdRequest.put("ack", map.get("id"));
						events = (OSDArray) ((map.get("events") instanceof OSDArray) ? map.get("events") : null);
					}
				}
				catch (ExecutionException ex)
				{
					Throwable cause = ex.getCause();
					if (cause instanceof HttpResponseException)
					{
						int status = ((HttpResponseException)cause).getStatusCode();
						if (status == HttpStatus.SC_NOT_FOUND || status == HttpStatus.SC_GONE)
						{
							setState(CapsState.Closed);
							Logger.Log(String.format("Closing event queue at %s due to missing caps URI", eventQueueGet), LogLevel.Info, _Simulator.getClient());
						}
						else if (status == HttpStatus.SC_BAD_GATEWAY)
						{
							// This is not good (server) protocol design, but it's normal.
							// The EventQueue server is a proxy that connects to a Squid
							// cache which will time out periodically. The EventQueue
							// server interprets this as a generic error and returns a
							// 502 to us that we ignore
						}
						else
						{
							++errorCount;

							// Try to log a meaningful error message
							if (status != HttpStatus.SC_OK)
							{
								Logger.Log(String.format("Unrecognized caps connection problem from %s: %d", eventQueueGet, status),
										   LogLevel.Warning, _Simulator.getClient());
							}
							else if (ex.getCause() != null)
							{
								Logger.Log("Unrecognized internal caps exception from " + eventQueueGet + ": " + ex.getCause().getMessage(),
										    LogLevel.Warning, _Simulator.getClient());
							}
							else
							{
								Logger.Log("Unrecognized caps exception from " + eventQueueGet + ": " + ex.getMessage(),
										    LogLevel.Warning, _Simulator.getClient());
							}
						}
					}
					else
					{
						++errorCount;

						Logger.Log("No response from the event queue but no reported HTTP error either", LogLevel.Warning, _Simulator.getClient(), ex);
					}
				}
				catch (Exception ex)
				{
					++errorCount;
					Logger.Log("Error retrieving response from the event queue request!", LogLevel.Warning, _Simulator.getClient(), ex);
				} 

				if (errorCount > 0)
				{
					// On error backoff in increasing delay to not hammer the server
					try
					{
						Thread.sleep(random.nextInt(500 + (int) Math.pow(2, errorCount)));
					}
					catch (InterruptedException e)
					{
					}
				}
			}
			else
			{
				setState(CapsState.Closed);
			}
		}
		Logger.Log("Terminated event queue for " + _Simulator.getName(), LogLevel.Info, _Simulator.getClient());
	}
}
