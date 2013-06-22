/**
 * Copyright (c) 2009, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
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
package libomv.examples.TestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import libomv.DirectoryManager.AgentSearchData;
import libomv.GridClient;
import libomv.GridClient.GridInfo;
import libomv.LoginManager;
import libomv.LoginManager.LoginParams;
import libomv.LoginManager.LoginProgressCallbackArgs;
import libomv.LoginManager.LoginStatus;
import libomv.LibSettings;
import libomv.Simulator;
import libomv.examples.TestClient.Commands.Inventory.ScriptCommand;
import libomv.examples.TestClient.Commands.System.WaitForLoginCommand;
import libomv.primitives.Primitive;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class ClientManager
{
	public class LoginDetails
	{
		public String FirstName;
		public String LastName;
		public String Password;
		public String StartLocation;
		public boolean GroupCommands;
		public String MasterName;
		public UUID MasterKey;
		public String URI;
	}

	static final String VERSION = "1.0.0";

	static class Singleton
	{
		private static final ClientManager Instance = new ClientManager();
	}

	public static ClientManager getInstance()
	{
		return Singleton.Instance;
	}

	public HashMap<UUID, TestClient> Clients = new HashMap<UUID, TestClient>();
	public HashMap<Simulator, HashMap<Integer, Primitive>> SimPrims = new HashMap<Simulator, HashMap<Integer, Primitive>>();

	private boolean _Running = true;
	public boolean GetTextures = false;
	public volatile int PendingLogins = 0;
	public String onlyAvatar = Helpers.EmptyString;

    private Scanner in = new Scanner(System.in);
    private ExecutorService _ThreadPool = Executors.newCachedThreadPool();

	public void start(List<LoginDetails> accounts, boolean getTextures) throws Exception
	{
		GetTextures = getTextures;

		for (LoginDetails account : accounts)
			login(account);
	}

	public TestClient login(String[] args) throws Exception
	{
		if (args.length < 3)
		{
			System.out.println("Usage: login <firstname> <lastname> <password> [<loginuri>|grid://<gridnick>] [<simname>[/<x>/<y>/<z>]]");
			return null;
		}
		LoginDetails account = new LoginDetails();
		account.FirstName = args[0];
		account.LastName = args[1];
		account.Password = args[2];

		if (args.length > 3)
		{
			String arg = args[3];
			if (arg.startsWith("http"))
			{
				account.URI = arg;
			}
			else if (arg.startsWith("grid://"))
			{
				GridInfo info = new GridClient().getGrid(arg.substring(7));
				if (info != null)
					account.URI = info.loginuri;
			}
		}
		
		if (args.length > 4)
		{
			String arg = args[4];
			
			// If it looks like a full starting position was specified, parse it
			if (arg.startsWith("http"))
			{
				account.URI = arg;
			}
			else
			{
				if (arg.indexOf('/') >= 0)
				{
					String sep = "/";
					String[] startbits = arg.split(sep);
					try
					{
						account.StartLocation = LoginManager.StartLocation(startbits[0],
								Integer.parseInt(startbits[1]), Integer.parseInt(startbits[2]),
								Integer.parseInt(startbits[3]));
					}
					catch (NumberFormatException ex)
					{
					}
				}
				// Otherwise, use the center of the named region
				if (account.StartLocation == null)
					account.StartLocation = LoginManager.StartLocation(arg, 128, 128, 40);
			}
		}

		if (account.URI == null || account.URI.isEmpty())
			account.URI = Program.LoginURI;
		Logger.Log("Using login URI " + account.URI, LogLevel.Info);

		return login(account);
	}
	
	public TestClient login(final LoginDetails account) throws Exception
	{
		// Check if this client is already logged in
		for (TestClient c : Clients.values())
		{
			if (c.Self.getFirstName().equals(account.FirstName) && c.Self.getLastName().equals(account.LastName))
			{
				Clients.remove(c.Self.getAgentID());
				c.Network.Logout();
				break;
			}
		}

		++PendingLogins;

		LibSettings settings = new LibSettings();
		settings.put(LibSettings.ENABLE_ASSET_MANAGER, true);
		settings.put(LibSettings.ENABLE_OBJECT_MANAGER, true);
//		settings.put(LibSettings.ENABLE_APPEARANCE_MANAGER, true);
		settings.put(LibSettings.ENABLE_DIRECTORY_MANAGER, true);
		settings.put(LibSettings.USE_LLSD_LOGIN, true);
		final TestClient client = new TestClient(this, settings);

		Callback<LoginProgressCallbackArgs> loginCallback = new Callback<LoginProgressCallbackArgs>()
		{
			@Override
			public boolean callback(LoginProgressCallbackArgs e)
			{
				Logger.Log(String.format("Login %s: %s", e.getStatus(), e.getMessage()), LogLevel.Info, client);

				if (e.getStatus() == LoginStatus.Success)
				{
					Clients.put(client.Self.getAgentID(), client);

					/* if the MasterKey hasn't been provided and we do have a MasterName available then try to resolve it */
					if ((client.MasterKey == null || client.MasterKey.equals(UUID.Zero)) && client.MasterName != null && !client.MasterName.isEmpty())
					{
						ArrayList<AgentSearchData> uuids = client.findFromAgentName(client.MasterName, 10000);
						if (uuids != null)
						{
							if (uuids.size() == 1)
							{
	                            Logger.Log("Master key resolved to " + client.MasterKey, LogLevel.Info, client);
	                            client.MasterKey = uuids.get(0).AgentID;
							}
							else
							{
								Logger.Log("Unable to resolve master key from " + client.MasterName, LogLevel.Warning, client);
							}	
						}
					}
					System.out.println("MOTD: " + e.getMessage());
					printPrompt();

					Logger.Log("Logged in " + client.toString(), LogLevel.Debug, client);
					--PendingLogins;
					return true;
				}
				else if (e.getStatus() == LoginStatus.Failed)
				{
					Logger.Log("Failed to login " + account.FirstName + " " + account.LastName + ": " + e.getMessage(), LogLevel.Warning, client);
					--PendingLogins;
					return true;
				}
				return false;
			}
		};
		
		// Optimize the throttle
		client.Throttle.setWind(0);
		client.Throttle.setCloud(0);
		client.Throttle.setLand(1000000);
		client.Throttle.setTask(1000000);

		client.GroupCommands = account.GroupCommands;
		client.MasterName = account.MasterName;
		client.MasterKey = account.MasterKey;
		
		// Require UUID for object master
		client.AllowObjectMaster =  client.MasterKey!= null && !client.MasterKey.equals(UUID.Zero);

		LoginParams loginParams = client.Login.DefaultLoginParams(account.FirstName, account.LastName,
				account.Password, "TestClient", VERSION);

		if (account.StartLocation != null && !account.StartLocation.isEmpty())
			loginParams.Start = account.StartLocation;

		if (account.URI != null && !account.URI.isEmpty())
			loginParams.URI = account.URI;

		client.Login.RequestLogin(loginParams, loginCallback);
		return client;
	}

	/**
	 * 
	 * @param noGUI
	 * @throws Exception
	 */
	public void run(boolean noGUI) throws Exception
	{
		try
		{
			if (noGUI)
			{
				while (_Running)
				{
					Thread.sleep(2 * 1000);
				}
			}
			else
			{
				System.out.println("Type quit to exit. Type help for a command list.");

				while (_Running)
				{
					printPrompt();
					
					String input = in.nextLine();
					doCommandAll(input, UUID.Zero);
				}
			}
		}
		finally
		{
			for (TestClient client : Clients.values())
			{
				client.Network.Logout();
			}
			Clients = null;
		}
	}

	private void printPrompt()
	{
		int online = 0;

		for (GridClient client : Clients.values())
		{
			if (client.Network.getConnected())
				online++;
		}
		System.out.print(online + " avatars online> ");
	}

	/**
	 * 
	 * 
	 * @param cmd
	 * @param fromAgentID
	 * @throws Exception 
	 */
	public void doCommandAll(String cmd, final UUID fromAgentID) throws Exception
    {
        String[] tokens = cmd.trim().split("[ \t]");
        if (tokens.length == 0)
            return;
        
        final String firstToken = tokens[0].toLowerCase();
        if (firstToken == null || firstToken.isEmpty())
            return;

        // Allow for comments when cmdline begins with ';' or '#'
        if (firstToken.startsWith(";") || firstToken.startsWith("#"))
            return;

        if (firstToken.startsWith("@"))
        {
            onlyAvatar = Helpers.EmptyString;
            if (tokens.length == 3)
            {
            	boolean found = false;
                onlyAvatar = tokens[1] + " " + tokens[2];
                for (TestClient client : Clients.values())
                {
                    if (client.toString().equals(onlyAvatar) && client.Network.getConnected())
                    {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    Logger.Log("Commanding only " + onlyAvatar + " now", LogLevel.Info);
                } else {
                    Logger.Log("Commanding nobody now. Avatar " + onlyAvatar + " is offline", LogLevel.Info);
                }
            }
            else
            {
                Logger.Log("Commanding all avatars now", LogLevel.Info);
            }
            return;
        }
        
        final String[] args = new String[tokens.length - 1];
        if (args.length > 0)
            System.arraycopy(tokens, 1, args, 0, args.length);

        if (firstToken.equals("grids"))
        {
        	GridClient client = new GridClient();
        	System.out.println(client.dumpGridlist());
        }
        else if (firstToken.equals("login"))
        {
            login(args);
        }
        else if (firstToken.equals("quit"))
        {
            quit();
            Logger.Log("All clients logged out and program finished running.", LogLevel.Info);
        }
        else if (firstToken.equals("help"))
        {
            if (Clients.size() > 0)
            {
                for (TestClient client : Clients.values())
                {
                    Command command = client.Commands.get("help");
                    String response = command.execute(args, fromAgentID);
                    client.printResponse(response, fromAgentID);
                    break;
                }
            }
            else
            {
            	System.out.println("You must login at least one bot to use the help command");
            }
        }
        else if (firstToken.equals("script"))
        {
            // No reason to pass this to all bots, and we also want to allow it when there are no bots
            ScriptCommand command = new ScriptCommand(null);
            Logger.Log(command.execute(args, UUID.Zero), LogLevel.Info);
        }
        else if (firstToken.equals("waitforlogin"))
        {
            // Special exception to allow this to run before any bots have logged in
            if (ClientManager.getInstance().PendingLogins > 0)
            {
                WaitForLoginCommand command = new WaitForLoginCommand(null);
                Logger.Log(command.execute(args, UUID.Zero), LogLevel.Info);
            }
            else
            {
                Logger.Log("No pending logins", LogLevel.Info);
            }
        }
        else
        {
            // Make an immutable copy of the Clients dictionary to safely iterate over
            HashMap<UUID, TestClient> clientsCopy = new HashMap<UUID, TestClient>(Clients);

            final AtomicInteger completed = new AtomicInteger();
            
            for (final TestClient testClient : clientsCopy.values())
            {
                _ThreadPool.execute(new Runnable()
                { 	
                	@Override
					public void run()
                    {
                        if ((onlyAvatar.isEmpty()) || (testClient.toString().equals(onlyAvatar))) 
                        {
                            try
                            {
                            	String response;
                            	if (testClient.Commands.containsKey(firstToken)) 
                            	{
                            		response = testClient.Commands.get(firstToken).execute(args, fromAgentID);
                                }
                                else
                                {
                                    response = "Unknown command \"" + firstToken + "\"";
                                }
                                testClient.printResponse(response, fromAgentID);
                            }
                            catch(Exception e)
                            {
                                Logger.Log(String.format("%s raised exception %s", firstToken, e), LogLevel.Error, testClient);
                            }
                        }
                        completed.incrementAndGet();
                    }
                });
            }

            while (completed.get() < clientsCopy.size())
                Thread.sleep(50);
        }
    }
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void quit() throws IOException
	{
		_Running = false;
		_ThreadPool.shutdownNow();
		in.close();
	}
}
