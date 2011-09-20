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
package libomv.test;

import libomv.GridClient;
import libomv.LoginManager.LoginParams;
import libomv.LoginManager.LoginResponseCallbackArgs;
import libomv.NetworkManager.DisconnectType;
import libomv.NetworkManager.DisconnectedCallbackArgs;
import libomv.Simulator;
import libomv.mapgenerator.ProtocolManager;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.types.PacketCallback;
import libomv.utils.CallbackHandler;

public class sldump extends CallbackHandler<DisconnectedCallbackArgs> implements PacketCallback
{
	// The main entry point for the application.
	static public void main(String[] args)
	{
		try
		{
			new sldump(args);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public sldump(String[] args) throws Exception
	{
		if (args.length == 0 || (args.length < 3 && !args[0].equals("--printmap")))
		{
			System.out.println("Usage: sldump [--printmap] [--decrypt] [inputfile] [outputfile] [--protocol] [firstname] [lastname] [password]");
			return;
		}

		if (args[0].equals("--decrypt"))
		{
			try {
				ProtocolManager.DecodeMapFile(args[1], args[2]);
			} catch (Exception e) {
				System.out.println(e.toString());
			}

			return;
		}

		if (args[0].equals("--printmap"))
		{
			ProtocolManager protocol;

			try {
				protocol = new ProtocolManager("message_template.msg", false);
			} catch (Exception e) {
				// Error initializing the client, probably missing file(s)
				System.out.println(e.toString());
				return;
			}

			protocol.PrintMap();
			return;
		}

		GridClient client = new GridClient();
		client.setDefaultGrid("secondlife");
		LoginParams loginParams = client.Login.DefaultLoginParams(args[0], args[1], args[2]);
		
		// Setup the packet callback and disconnect event handler
		client.Network.RegisterCallback(PacketType.Default, this);
		client.Network.OnDisconnected.add(this);
		
		// Setup the Login Response handler to print out the result of the login
		client.Login.RegisterLoginResponseCallback(new Network_OnLogin(), loginParams.Options, false);

		// An example of how to pass additional options to the login server
		// loginParams.ID0 = "65e142a8d3c1ee6632259f111cb168c9";
		// loginParams.ViewerDigest = "0e63550f-0991-a092-3158-b4206e728ffa";

		if (!client.Login.Login(loginParams))
		{
			// Login failed
			return;
		}
		
		while (true)
		{
			client.Tick(100);
		}
	}

	public class Network_OnLogin extends CallbackHandler<LoginResponseCallbackArgs>
	{
		@Override
		public void callback(LoginResponseCallbackArgs e)
		{
			if (e.getSuccess())
			{
				// Login was successful
				System.out.println("sldump: Message of the day: " + e.getMessage());
			}
			else if (e.getRedirect())
			{
				// Server requested redirection
				System.out.println("sldump: Server requested redirection: " + e.getReason());
			}
			else
			{
				System.out.println("sldump: Error logging in: " + e.getReason());			
			}
		}
		
	}
	
	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
		    case Default:
				System.out.println(packet.toString());
			    break;
		}
	}

	@Override
	public void callback(DisconnectedCallbackArgs args)
	{
		DisconnectType type = args.getDisconnectType();
		if (type == DisconnectType.NetworkTimeout)
		{
			System.out.println("Network connection timed out, disconnected");
		}
		else if (type == DisconnectType.ServerInitiated)
		{
			System.out.println("Server disconnected us: " + args.getMessage());
		}
	}
}
