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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import libomv.AgentManager.InstantMessageCallbackArgs;
import libomv.AgentManager.InstantMessageDialog;
import libomv.DirectoryManager.AgentSearchData;
import libomv.DirectoryManager.DirPeopleReplyCallbackArgs;
import libomv.GridClient;
import libomv.GroupManager.CurrentGroupsCallbackArgs;
import libomv.GroupManager.Group;
import libomv.GroupManager.GroupMember;
import libomv.GroupManager.GroupMembersReplyCallbackArgs;
import libomv.LoginManager.LoginProgressCallbackArgs;
import libomv.LoginManager.LoginStatus;
import libomv.NetworkManager.DisconnectedCallbackArgs;
import libomv.LibSettings;
import libomv.NetworkManager.SimChangedCallbackArgs;
import libomv.ObjectManager.AvatarUpdateCallbackArgs;
import libomv.ObjectManager.TerseObjectUpdateCallbackArgs;
import libomv.Simulator;
import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryManager.InventoryObjectOfferedCallbackArgs;
import libomv.packets.AgentDataUpdatePacket;
import libomv.packets.AgentFOVPacket;
import libomv.packets.AlertMessagePacket;
import libomv.packets.AvatarAppearancePacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.TimeoutEvent;

public class TestClient extends GridClient implements PacketCallback
{
	public UUID GroupID = UUID.Zero;
	public HashMap<UUID, GroupMember> GroupMembers;
	public HashMap<UUID, AvatarAppearancePacket> Appearances = new HashMap<UUID, AvatarAppearancePacket>();
	public HashMap<String, Command> Commands = new HashMap<String, Command>();
	public boolean GroupCommands = false;
	public String MasterName;
	public UUID MasterKey;
	public boolean AllowObjectMaster = false;
	public ClientManager _ClientManager;
//	public VoiceManager _VoiceManager;

	// Shell-like inventory commands need to be aware of the 'current' inventory folder.
	public InventoryFolder CurrentDirectory = null;

	private Timer updateTimer;
	private UUID GroupMembersRequestID;
	private HashMap<UUID, Group> GroupsCache;
	private TimeoutEvent<HashMap<UUID, Group>> GroupsEvent = new TimeoutEvent<HashMap<UUID, Group>>();

	// / <summary>
	// /
	// / </summary>
	public TestClient(ClientManager manager, LibSettings settings) throws Exception
	{
		super(settings);
		_ClientManager = manager;

		RegisterAllCommands(this.getClass());

 		Logger.LOG_LEVEL = LogLevel.Info;

		Settings.LOG_RESENDS = false;
		Settings.put(LibSettings.STORE_LAND_PATCHES, true);
		Settings.put(LibSettings.SEND_AGENT_UPDATES, true);
		Settings.ALWAYS_DECODE_OBJECTS = true;
		Settings.ALWAYS_REQUEST_OBJECTS = true;

		Network.OnDisconnected.add(new Network_OnDisconnected(), true);
		Login.OnLoginProgress.add(new Network_OnLoginProgress(), false);
		Objects.OnAvatarUpdate.add(new Objects_AvatarUpdate(), false);
		Objects.OnTerseObjectUpdate.add(new Objects_TerseObjectUpdate(), false);
        Network.OnSimChanged.add(new Network_SimChanged(), false);
		Self.OnInstantMessage.add(new Self_IM(), false);
		Groups.OnGroupMembersReply.add(new GroupMembersHandler(), false);
		Inventory.OnInventoryObjectOffered.add(new Inventory_OnInventoryObjectReceived(), false);
		
		Network.RegisterCallback(PacketType.AgentDataUpdate, this);
		Network.RegisterCallback(PacketType.AvatarAppearance, this);
		Network.RegisterCallback(PacketType.AlertMessage, this);
		
//		_VoiceManager = new VoiceManager(this);

		updateTimer = new Timer("TestClient UpdateTimer");
		updateTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
		        for (Command c : Commands.values())
		        {
		            if (c.Active)
		                c.think();
		        }
			}
		}, 500, 500);
	}

	public HashMap<UUID, Group> getCurrentGroups(boolean reload) throws Exception
	{
		if (GroupsCache == null || reload)
			GroupsCache = ReloadCurrentGroups();
		return GroupsCache;
	}

	public ArrayList<AgentSearchData> findFromAgentName(final String name, long timeout)
	{
	    final TimeoutEvent<ArrayList<AgentSearchData> > keyResolution = new TimeoutEvent<ArrayList<AgentSearchData>>();
		final UUID query = new UUID();
		
		Callback<DirPeopleReplyCallbackArgs> peopleDirCallback = new Callback<DirPeopleReplyCallbackArgs>()
		{
			@Override
			public boolean callback(DirPeopleReplyCallbackArgs dpe)
			{
				if (dpe.getQueryID().equals(query))
				{
					keyResolution.set(dpe.getMatchedPeople());
				}
				return false;
			}
		};

		ArrayList<AgentSearchData> temp, agents = null;
		Directory.OnDirPeople.add(peopleDirCallback);
		try
		{
			Directory.StartPeopleSearch(name, 0, query);
			agents = keyResolution.waitOne(timeout, true);
	        while (agents != null)
	        {
	        	temp = keyResolution.waitOne(2000, true);
	        	if (temp == null)
	        		break;
	       		agents.addAll(temp);
	        }
		}
		catch (Exception ex)
		{
			Logger.Log("Exception when trying to do people search", LogLevel.Error, this, ex);
		}
		Directory.OnDirPeople.remove(peopleDirCallback);
		return agents;
	}

	public UUID groupName2UUID(String groupName) throws Exception
    {
        UUID tryUUID = UUID.parse(groupName);
        if (tryUUID == null)
        {
            if (GroupsCache == null)
            {
            	GroupsCache = ReloadCurrentGroups();
                if (null == GroupsCache)
                    return null;
            }

            synchronized (GroupsCache)
            {
                for (Group currentGroup : GroupsCache.values())
                    if (currentGroup.getName().equalsIgnoreCase(groupName))
                        return currentGroup.getID();
            }
        }
        return tryUUID;
    }

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case AgentDataUpdate:
				AgentDataUpdateHandler(packet, simulator);
				break;
			case AvatarAppearance:
				AvatarAppearanceHandler(packet, simulator);
				break;
			case AlertMessage:
				AlertMessageHandler(packet, simulator);
				break;
		}
	}
	
	public void printResponse(String response, UUID toAgent) throws Exception
	{
		int length = response.length() - 1;

		if (toAgent != null && toAgent.equals(MasterKey))
		{
			if (response.charAt(length) == '\n')
				response = response.substring(0, length);
			Self.InstantMessage(toAgent, response);
		}
		else
		{
			if (response.charAt(length) != '\n')
				response += "\n";
			System.out.println(response);
		}
	}
	
	private boolean cancel()
	{
		updateTimer.cancel();
		updateTimer = null;
		return true;
	}

	private class Network_OnDisconnected implements Callback<DisconnectedCallbackArgs>
	{
		@Override
		public boolean callback(DisconnectedCallbackArgs e)
		{
			return cancel();
		}
	}

	/**
	 * Initialize everything that needs to be initialized once we're logged in.
	 *
	 * @param login The status of the login
	 * @param message Error message on failure, MOTD on success.
	 */
	private class Network_OnLoginProgress implements Callback<LoginProgressCallbackArgs>
	{
		@Override
		public boolean callback(LoginProgressCallbackArgs e)
		{
			if (e.getStatus() == LoginStatus.Success)
			{
				// Start in the inventory root folder.
				CurrentDirectory = Inventory.getRootNode(false);
				return true;				
			}
			else if (e.getStatus() == LoginStatus.Failed)
			{
				return cancel();
			}
			return false;
		}
	}
			
	private class Objects_TerseObjectUpdate implements Callback<TerseObjectUpdateCallbackArgs>
	{
		@Override
		public boolean callback(TerseObjectUpdateCallbackArgs e)
		{
			if (e.getPrim().LocalID == Self.getLocalID())
			{
				SetDefaultCamera();
			}
			return false;
		}
	}

	private class Objects_AvatarUpdate implements Callback<AvatarUpdateCallbackArgs>
	{
		@Override
		public boolean callback(AvatarUpdateCallbackArgs e)
		{
			if (e.getAvatar().LocalID == Self.getLocalID())
			{
				SetDefaultCamera();
			}
			return false;
		}
    }

	private class Network_SimChanged implements Callback<SimChangedCallbackArgs>
	{
		@Override
		public boolean callback(SimChangedCallbackArgs e)
		{
			AgentFOVPacket msg = new AgentFOVPacket();
			msg.AgentData.AgentID = Self.getAgentID();
			msg.AgentData.SessionID = Self.getSessionID();
			msg.AgentData.CircuitCode = Network.getCircuitCode();
			msg.FOVBlock.GenCounter = 0;
			msg.FOVBlock.VerticalAngle = Helpers.TWO_PI - 0.05f;
			try
			{
				Network.sendPacket(msg);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return false;
		}
    }

    public void SetDefaultCamera()
    {
    	/* Set camera 5m behind the avatar */
        Self.getMovement().Camera.LookAt(new Vector3(-5, 0, 0).multiply(Self.getMovement().BodyRotation).add(Self.getAgentPosition()), Self.getAgentPosition());
    }
	
	private class Self_IM implements Callback<InstantMessageCallbackArgs>
	{
		@Override
		public boolean callback(InstantMessageCallbackArgs e)
		{
			boolean groupIM = e.getIM().GroupIM && GroupMembers != null && GroupMembers.containsKey(e.getIM().FromAgentID) ? true
					: false;

			if (e.getIM().FromAgentID.equals(MasterKey) || (GroupCommands && groupIM))
			{
				// Received an IM from someone that is authenticated
				System.out.println(String.format("<%s (%s)> %s: %s (@%s:%s)", e.getIM().GroupIM ? "GroupIM" : "IM", e.getIM().Dialog,
						e.getIM().FromAgentName, e.getIM().Message, e.getIM().RegionID, e.getIM().Position));

				try
				{
					if (e.getIM().Dialog == InstantMessageDialog.RequestTeleport)
					{
						System.out.println("Accepting teleport lure.");
						Self.TeleportLureRespond(e.getIM().FromAgentID, e.getIM().IMSessionID, true);
					}
					else if (e.getIM().Dialog == InstantMessageDialog.MessageFromAgent
							|| e.getIM().Dialog == InstantMessageDialog.MessageFromObject)
					{
						ClientManager.getInstance().doCommandAll(e.getIM().Message, e.getIM().FromAgentID);
					}
				}
				catch (Exception ex)
				{
					
				}
			}
			else
			{
				// Received an IM from someone that is not the bot's master, ignore
				System.out.println(String.format("<%s (%s)> %s (not master): %s (@%s:%s)", e.getIM().GroupIM ? "GroupIM" : "IM",
						e.getIM().Dialog, e.getIM().FromAgentName, e.getIM().Message, e.getIM().RegionID, e.getIM().Position));
			}
			return false;
		}
	}
	
	private HashMap<UUID, Group> ReloadCurrentGroups() throws Exception
	{
		Callback<CurrentGroupsCallbackArgs> currentGroups = new Groups_CurrentGroups();
		Groups.OnCurrentGroups.add(currentGroups);
		Groups.RequestCurrentGroups();
		HashMap<UUID, Group> groups = GroupsEvent.waitOne(10000);
		Groups.OnCurrentGroups.remove(currentGroups);
		GroupsEvent.reset();
		return groups;
	}
	
	private class Groups_CurrentGroups implements Callback<CurrentGroupsCallbackArgs>
	{
		@Override
		public boolean callback(CurrentGroupsCallbackArgs e)
		{
			GroupsEvent.set(e.getGroups());
			return false;
		}
	}

	private class GroupMembersHandler implements Callback<GroupMembersReplyCallbackArgs>
	{
		@Override
		public boolean callback(GroupMembersReplyCallbackArgs e)
		{
			if (e.getRequestID().equals(GroupMembersRequestID))
			{
				GroupMembers = e.getMembers();
			}
			return false;
		}
	}
	
	private class Inventory_OnInventoryObjectReceived implements Callback<InventoryObjectOfferedCallbackArgs>
	{
		@Override
		public boolean callback(InventoryObjectOfferedCallbackArgs e)
		{
			if (MasterKey == null || !MasterKey.equals(UUID.Zero))
			{
				if (!MasterKey.equals(e.getOffer().FromAgentID))
					return false;
			}
			else if (GroupMembers != null && !GroupMembers.containsKey(e.getOffer().FromAgentID))
			{
				return false;
			}
			e.setAccept(true);
			return false;
		}
	}
	
	private void AgentDataUpdateHandler(Packet packet, Simulator simulator) throws Exception
	{
		AgentDataUpdatePacket p = (AgentDataUpdatePacket) packet;
		if (p.AgentData.AgentID.equals(simulator.getClient().Self.getAgentID()))
		{
			GroupID = p.AgentData.ActiveGroupID;

			GroupMembersRequestID = simulator.getClient().Groups.RequestGroupMembers(GroupID);
		}
	}

	private void AvatarAppearanceHandler(Packet packet, Simulator simulator)
    {
        AvatarAppearancePacket appearance = (AvatarAppearancePacket)packet;

        synchronized (Appearances)
        {
        	Appearances.put(appearance.Sender.ID, appearance);
        }
    }

	private void AlertMessageHandler(Packet packet, Simulator simulator) throws UnsupportedEncodingException
	{
		AlertMessagePacket message = (AlertMessagePacket) packet;

		Logger.Log("[AlertMessage] " + Helpers.BytesToString(message.AlertData.getMessage()), LogLevel.Info, this);
	}
	
	private void RegisterAllCommands(Class<?> assembly)
    {
		ArrayList<Class<?>> classes = getDerivedClasses(assembly.getPackage(), Command.class);
        for (Class<?> clazz : classes)
        {
        	Class<?> supClass = clazz.getSuperclass();
            try
            {
                if (supClass != null && supClass.equals(Command.class))
                {
                    @SuppressWarnings("unchecked")
					Constructor<Command> ctor = (Constructor<Command>) clazz.getDeclaredConstructor(new Class[] {TestClient.class});
                    Command command = ctor.newInstance(new Object[] {this});
                    RegisterCommand(command);
                }
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
    }

	private void RegisterCommand(Command command)
	{
		command.Client = this;
		if (!Commands.containsKey(command.Name.toLowerCase()))
		{
			Commands.put(command.Name.toLowerCase(), command);
		}
	}

	private static ArrayList<Class<?>> getDerivedClasses(Package pkg, Class<?> superClazz)
	{
	    String pkgname = pkg.getName();
	    ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

	    // Get a File object for the package
	    File directory = null;
	    String relPath = pkgname.replace('.', '/');
//	    System.out.println("ClassDiscovery: Package: " + pkgname + " becomes Path:" + relPath);
	    URL resource = ClassLoader.getSystemClassLoader().getResource(relPath);
//	    System.out.println("ClassDiscovery: Resource = " + resource);
	    if (resource == null) {
	        throw new RuntimeException("No resource for " + relPath);
	    }

	    try {
	        directory = new File(resource.toURI());
	    } catch (URISyntaxException e) {
	        throw new RuntimeException(pkgname + " (" + resource + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...", e);
	    } catch (IllegalArgumentException e) {
	        directory = null;
	    }
//	    System.out.println("ClassDiscovery: Directory = " + directory);

	    if (directory != null && directory.exists())
	    {
	    	enumerateAllDerivedClasses(classes, pkgname, directory, superClazz);
	    }
	    else
	    {
	        try
	        {
	    	    String fullPath = resource.getFile();
//	    	    System.out.println("ClassDiscovery: FullPath = " + resource);
	            String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
	            JarFile jarFile = new JarFile(jarPath);         
	            Enumeration<JarEntry> entries = jarFile.entries();
	            while(entries.hasMoreElements())
	            {
	                JarEntry entry = entries.nextElement();
	                String entryName = entry.getName();
	                if(entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length()))
	                {
//	                    System.out.println("ClassDiscovery: JarEntry: " + entryName);
	                    String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
//	                    System.out.println("ClassDiscovery: className = " + className);
	                    try
	                    {
	                    	Class<?> clazz = Class.forName(className);
	                    	if (superClazz.equals(clazz.getSuperclass()))
	                    	    classes.add(clazz);
	                    } 
	                    catch (ClassNotFoundException e)
	                    {
	                        throw new RuntimeException("ClassNotFoundException loading " + className);
	                    }
	                }
	            }
	        }
	        catch (IOException e)
	        {
	            throw new RuntimeException(pkgname + " (" + directory + ") does not appear to be a valid package", e);
	        }
	    }
	    return classes;
	}
	
	private static void enumerateAllDerivedClasses(ArrayList<Class<?>> classes, String pkgname, File directory, Class<?> superClazz)
	{
        // Get the list of the files contained in the package
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isDirectory())
            {
        	    enumerateAllDerivedClasses(classes, pkgname + "." + files[i].getName(), files[i], superClazz);
            }	
            // we are only interested in .class files
            else 
            {
            	String fileName = files[i].getName();
            	if (fileName.endsWith(".class"))
            	{
	                // removes the .class extension
	                String className = pkgname + '.' + fileName.substring(0, fileName.length() - 6);
//	                System.out.println("ClassDiscovery: className = " + className);
	                try
	                {
                    	Class<?> clazz = Class.forName(className);
                    	if (superClazz.equals(clazz.getSuperclass()))
                    	    classes.add(clazz);
	                } 
	                catch (ClassNotFoundException e)
	                {
	                    throw new RuntimeException("ClassNotFoundException loading " + className);
	                }
            	}
            }
        }
	}
}
