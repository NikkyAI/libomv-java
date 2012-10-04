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
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import libomv.DirectoryManager.DirPeopleReplyCallbackArgs;
import libomv.GridClient;
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

	public class StartPosition
	{
		public String sim;
		public int x;
		public int y;
		public int z;

		public StartPosition()
		{
			this.sim = null;
			this.x = 0;
			this.y = 0;
			this.z = 0;
		}
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

	public boolean Running = true;
	public boolean GetTextures = false;
	public volatile int PendingLogins = 0;
	public String onlyAvatar = Helpers.EmptyString;

    Scanner in = new Scanner(System.in);

	public void Start(List<LoginDetails> accounts, boolean getTextures) throws Exception
	{
		GetTextures = getTextures;

		for (LoginDetails account : accounts)
			Login(account);
	}

	public TestClient Login(String[] args) throws Exception
	{
		if (args.length < 3)
		{
			System.out.println("Usage: login firstname lastname password [simname] [login server url]");
			return null;
		}
		LoginDetails account = new LoginDetails();
		account.FirstName = args[0];
		account.LastName = args[1];
		account.Password = args[2];

		if (args.length > 3)
		{
			// If it looks like a full starting position was specified, parse it
			if (args[3].startsWith("http"))
			{
				account.URI = args[3];
			}
			else
			{
				if (args[3].indexOf('/') >= 0)
				{
					String sep = "/";
					String[] startbits = args[3].split(sep);
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
					account.StartLocation = LoginManager.StartLocation(args[3], 128, 128, 40);
			}
		}

		if (args.length > 4)
			if (args[4].startsWith("http"))
				account.URI = args[4];

		if (account.URI == null || account.URI.isEmpty())
			account.URI = Program.LoginURI;
		Logger.Log("Using login URI " + account.URI, LogLevel.Info);

		return Login(account);
	}
	
	public TestClient Login(final LoginDetails account) throws Exception
	{
		// Check if this client is already logged in
		for (TestClient c : Clients.values())
		{
			if (c.Self.getFirstName().equals(account.FirstName) && c.Self.getLastName().equals(account.LastName))
			{
				Logout(c);
				break;
			}
		}

		++PendingLogins;

		LibSettings settings = new LibSettings();
		settings.put(LibSettings.ENABLE_OBJECT_MANAGER, true);
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

					/* if the MasterKey hasn't been provided and we do have the directory service
					 * available then try to resolve it for our account name */
					if (client.Directory != null && (client.MasterKey == null || client.MasterKey.equals(UUID.Zero)))
					{
						final UUID query = new UUID();

						Callback<DirPeopleReplyCallbackArgs> peopleDirCallback = new Callback<DirPeopleReplyCallbackArgs>()
						{
							@Override
							public boolean callback(DirPeopleReplyCallbackArgs dpe)
							{
								if (dpe.getQueryID().equals(query))
								{
									if (dpe.getMatchedPeople().size() != 1)
									{
										Logger.Log("Unable to resolve master key from " + client.MasterName,
												LogLevel.Warning);
									}
									else
									{
										client.MasterKey = dpe.getMatchedPeople().get(0).AgentID;
										Logger.Log("Master key resolved to " + client.MasterKey, LogLevel.Info);
									}
								}
								return false;
							}
						};

						client.Directory.OnDirPeople.add(peopleDirCallback);
						try
						{
							client.Directory.StartPeopleSearch(client.MasterName, 0, query);
						}
						catch (Exception ex)
						{
							Logger.Log("Exception when trying to do people search", LogLevel.Error, client, ex);
						}
					}

					Logger.Log("Logged in " + client.toString(), LogLevel.Info);
					--PendingLogins;
					return true;
				}
				else if (e.getStatus() == LoginStatus.Failed)
				{
					Logger.Log("Failed to login " + account.FirstName + " " + account.LastName + ": " + e.getMessage(),
							LogLevel.Warning);
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
	public void Run(boolean noGUI) throws Exception
	{
		if (noGUI)
		{
			while (Running)
			{
				Thread.sleep(2 * 1000);
			}
		}
		else
		{
			System.out.println("Type quit to exit. Type help for a command list.");

			while (Running)
			{
				PrintPrompt();
				
				String input = in.nextLine();
				DoCommandAll(input, UUID.Zero);
			}
		}

		for (GridClient client : Clients.values())
		{
			if (client.Network.getConnected())
				client.Network.Logout();
		}
	}

	private void PrintPrompt()
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
	public void DoCommandAll(String cmd, final UUID fromAgentID) throws Exception
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
                    if ((client.toString() == onlyAvatar) && (client.Network.getConnected()))
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

        if (firstToken.equals("login"))
        {
            Login(args);
        }
        else if (firstToken.equals("quit"))
        {
            Quit();
            Logger.Log("All clients logged out and program finished running.", LogLevel.Info);
        }
        else if (firstToken.equals("help"))
        {
            if (Clients.size() > 0)
            {
                for (TestClient client : Clients.values())
                {
                    System.out.println(client.Commands.get("help").execute(args, UUID.Zero));
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

            ExecutorService threadPool = Executors.newCachedThreadPool();
            
            for (final TestClient testClient : clientsCopy.values())
            {
                threadPool.execute(new Runnable()
                { 	
                	@Override
					public void run()
                    {
                        if ((onlyAvatar.isEmpty()) || (testClient.toString().equals(onlyAvatar))) 
                        {
                            if (testClient.Commands.containsKey(firstToken)) 
                            {
                                String result;
                                try
                                {
                                    result = testClient.Commands.get(firstToken).execute(args, fromAgentID);
                                    Logger.Log(result, LogLevel.Info, testClient);
                                }
                                catch(Exception e)
                                {
                                    Logger.Log(String.format("%s raised exception %s", firstToken, e), LogLevel.Error, testClient);
                                }
                            }
                            else
                            {
                                Logger.Log("Unknown command " + firstToken, LogLevel.Warning);
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
	 * 
	 * @param client
	 * @throws Exception
	 */
	public void Logout(TestClient client) throws Exception
	{
		Clients.remove(client.Self.getAgentID());
		client.Network.Logout();
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void Quit() throws IOException
	{
		Running = false;
		in.close();
	}
}
