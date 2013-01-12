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

import java.net.URI;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.nio.reactor.IOReactorException;

import libomv.GridClient;
import libomv.Simulator;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSD.OSDType;
import libomv.packets.Packet;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class CapsEventQueue extends CapsClient
{
	public final int REQUEST_TIMEOUT = 1000 * 120;

	private URI address;
	private Simulator _Simulator;
	private GridClient _Client;

	public CapsEventQueue(Simulator sim, URI eventQueueLocation) throws IOReactorException
	{
		super("EventQueue");
		address = eventQueueLocation;
		_Simulator = sim;
	}

	private int errorCount;
	private boolean running;

	public boolean getRunning()
	{
		return running;
	}

	public void start()
	{
		running = true;
		_Client = _Simulator.getClient();
		Thread eventloop = new Thread(new Runnable()
		{
			public void run()
			{
				Random random = new Random();
				boolean first = true;
				OSDArray events = null;
				int ack = 0;

				do
				{
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

					OSDMap osdRequest = new OSDMap();
					osdRequest.put("ack", first ? new OSD() : OSD.FromInteger(ack));
					osdRequest.put("done", OSD.FromBoolean(!running));

					// Start or resume the connection
					Future<OSD> Request = executeHttpPost(address, osdRequest, OSD.OSDFormat.Xml, null, REQUEST_TIMEOUT);
				
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
								IMessage message = _Client.Messages.DecodeEvent(name, body);
								if (message != null)
								{
									Logger.Log("Caps message: " + name + ".", LogLevel.Debug, _Client);
									_Client.Network.DistributeCaps(_Simulator, message);

									// #region Stats Tracking
									if (_Client.Settings.TRACK_UTILIZATION)
									{
										/* TODO add Stats support to Client manager */
										// Simulator.getClient().Stats.Update(eventName,
										// libomv.Stats.Type.Message, 0,
										// body.ToString().Length);
									}
								}
								else
								{
									Logger.Log("No Message handler exists for event " + name
											+ ". Unable to decode. Will try Generic Handler next", LogLevel.Warning, _Client);
									Logger.Log("Please report this information to http://sourceforge.net/tracker/?group_id=387920&atid=1611745\n" + body,
											LogLevel.Debug, _Client);

									// try generic decoder next which takes a caps event and
									// tries to match it to an existing packet
									Packet packet = CapsToPacket.BuildPacket(name, body);
									if (packet != null)
									{
										Logger.DebugLog("Serializing " + packet.getType() + " capability with generic handler", _Client);
										_Client.Network.DistributePacket(_Simulator, packet);
									}
									else
									{
										Logger.Log("No Packet or Message handler exists for " + name, LogLevel.Warning, _Client);
									}
								}
							}
						}
						events = null;
					}

					try
					{
						OSD result = null;
						synchronized (address)
						{
							if (Request != null)
								result = Request.get();
						}
						if (result == null)
						{
							++errorCount;
							Logger.Log("Got an unparseable response from the event queue!", LogLevel.Warning, _Client);
						}
						else if (result instanceof OSDMap)
						{
							errorCount = 0;
							if (first)
							{
								_Client.Network.raiseConnectedEvent(_Simulator);
								first = false;
							}

							OSDMap map = (OSDMap) result;
							ack = map.get("id").AsInteger();
							events = (OSDArray) ((map.get("events") instanceof OSDArray) ? map.get("events") : null);
						}
					}
					catch (ExecutionException e)
					{
						Throwable ex = e.getCause();
						if (ex instanceof HttpResponseException)
						{
							int status = ((HttpResponseException)ex).getStatusCode();
							if (status == HttpStatus.SC_NOT_FOUND || status == HttpStatus.SC_GONE)
							{
								running = false;
								Logger.Log(String.format("Closing event queue at %s due to missing caps URI", address),
										LogLevel.Info, _Client);
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
									Logger.Log(String.format("Unrecognized caps connection problem from %s: %d",
											address, status), LogLevel.Warning, _Client);
								}
								else if (ex.getCause() != null)
								{
									Logger.Log(String.format("Unrecognized internal caps exception from %s: %s",
											address, ex.getCause().getMessage()), LogLevel.Warning, _Client);
								}
								else
								{
									Logger.Log(String.format("Unrecognized caps exception from %s: %s", address,
													ex.getMessage()), LogLevel.Warning, _Client);
								}
							}
						}
						else
						{
							++errorCount;

							Logger.Log("No response from the event queue but no reported error either", LogLevel.Warning, _Client, ex);
						}
					}
					catch (Exception ex)
					{
						++errorCount;
						Logger.Log("Error retrieving response from the event queue request!", LogLevel.Warning, _Client, ex);
					} 
				}
				while (running);
				Logger.DebugLog("Caps Event queue terminated", _Client);
			}
		});
		eventloop.setName("EventLoop");
		// Startup the event queue
		eventloop.start();
	}
}
