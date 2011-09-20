/**
 * Copyright (c) 2007-2008, openmetaverse.org
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
package libomv;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.nio.concurrent.FutureCallback;

import net.xmlrpc.XMLRPCClient;

import libomv.GridClient;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.LLSD.LLSDNotation;
import libomv.assets.AssetItem.AssetType;
import libomv.capabilities.CapsClient;
import libomv.inventory.InventoryFolder;
import libomv.packets.EconomyDataRequestPacket;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.CallbackHandlerQueue;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.TimeoutEvent;
import libomv.utils.TimeoutEventQueue;

public class LoginManager
{
    // #region Enums
    public enum LoginStatus
    {
        Failed,
        None,
        ConnectingToLogin,
        ReadingResponse,
        ConnectingToSim,
        Redirecting,
        Success;
    }
    // #endregion Enums

    // #region Structs
    /** Login Request Parameters */
    public class LoginParams
    {
        /** The URL of the Login Server        */
        public String URI;
        /** The number of milliseconds to wait before a login is considered failed due to timeout */
        public int Timeout;
        /** The request method login_to_simulator is currently the only supported method */
        public String MethodName;
        /** The Agents First name */
        public String FirstName;
        /** The Agents Last name */
        public String LastName;
        /** A md5 hashed password, plaintext password will be automatically hashed */
        public String Password;
        /** The agents starting location once logged in
            Either "last", "home", or a string encoded URI 
            containing the simulator name and x/y/z coordinates e.g: uri:hooper&amp;128&amp;152&amp;17 */
        public String Start;
        /** A string containing the client software channel information <example>Second Life Release</example> */
        public String Channel;
        /** The client software version information
            The official viewer uses: Second Life Release n.n.n.n 
            where n is replaced with the current version of the viewer */
        public String Version;
        /** A string containing the platform information the agent is running on */
        public String Platform;
        /** A string hash of the network cards Mac Address */
        public String MAC;
        /** Unknown or deprecated */
        public String ViewerDigest;
        /** A string hash of the first disk drives ID used to identify this clients uniqueness */
        public String ID0;
        /** A string containing the viewers Software, this is not directly sent to the login server but
            is used by the library to generate the Version information */
        public String UserAgent;
        /** A string representing the software creator. This is not directly sent to the login server but
            is used by the library to generate the Version information */
        public String Author;
        /** If true, this agent agrees to the Terms of Service of the grid its connecting to */
        public boolean AgreeToTos;
        /** Unknown */
        public boolean ReadCritical;
        /** An array of string sent to the login server to enable various options */
        public String[] Options;

        /**
         * Default constructor, initializes sane default values
         */
        public LoginParams()
        {
            this.Options = new String[]
                {"inventory-root", "inventory-skeleton", "inventory-lib-root", "inventory-lib-owner", "inventory-skel-lib",
                 "initial-outfit", "gestures", "event_categories", "event_notifications", "classified_categories", "buddy-list",
                 "ui-config", "tutorial_settings", "login-flags", "global-textures", "adult_compliant"};
            this.MethodName = "login_to_simulator";
            this.Start = "last";
            this.Platform = Helpers.GetPlatform();
            this.MAC = Helpers.GetMAC();
            this.ViewerDigest = "";
            this.ID0 = Helpers.GetMAC();
            this.AgreeToTos = true;
            this.ReadCritical = true;
        }

        /**
         * Instantiates new LoginParams object and fills in the values
         * 
         * @param client Instance of GridClient to read settings from
         * @param firstName Login first name
         * @param lastName Login last name
         * @param password Password
         * @param channel Login channnel (application name)
         * @param version Client version, should be application name + version number
         */
        public LoginParams(GridClient client, String firstName, String lastName, String password, String channel, String version)
        {
            this();
            this.URI = client.getDefaultGrid().loginuri;
            this.Timeout = client.Settings.LOGIN_TIMEOUT;
            this.FirstName = firstName;
            this.LastName = lastName;
            this.Password = password;
            this.Channel = channel;
            this.Version = version;
        }
    }

    public final class BuddyListEntry
    {
        public int buddy_rights_given;
        public String buddy_id;
        public int buddy_rights_has;
    }

    /** The decoded data returned from the login server after a successful login */
    public final class LoginResponseData
    {
        /**
         * true, false, indeterminate
         * [XmlRpcMember("login")]
         */
        public String Login;
        public boolean Success;
        public String Reason;
        /** Login message of the day */
        public String Message;
        public UUID AgentID;
        public UUID SessionID;
        public UUID SecureSessionID;
        public String FirstName;
        public String LastName;
        public String StartLocation;
        /** M or PG, also agent_region_access and agent_access_max */
        public String AgentAccess;
        public Vector3 LookAt;
        public long HomeRegion;
        public Vector3 HomePosition;
        public Vector3 HomeLookAt;
        public int CircuitCode;
        public int RegionX;
        public int RegionY;
        public short SimPort;
        public InetAddress SimIP;
        public String SeedCapability;
        public BuddyListEntry[] BuddyList;
        public int SecondsSinceEpoch;
        public String UDPBlacklist;

        // #region Inventory
        public UUID InventoryRoot;
        public UUID LibraryRoot;
        public InventoryFolder[] InventorySkeleton;
        public InventoryFolder[] LibrarySkeleton;
        public UUID LibraryOwner;
        // #endregion

        // #region Redirection
        public String NextMethod;
        public String NextUrl;
        public String[] NextOptions;
        public int NextDuration;
        // #endregion

        // These aren't currently being utilized by the library
        public String AgentAccessMax;
        public String AgentRegionAccess;
        public int AOTransition;
        public String InventoryHost;

        // Unhandled:
        // reply.gestures
        // reply.event_categories
        // reply.classified_categories
        // reply.event_notifications
        // reply.ui_config
        // reply.login_flags
        // reply.global_textures
        // reply.initial_outfit

        /**
         * Parse LLSD Login Reply Data
         *
         * @param reply An {@link OSDMap} containing the login response data.
         *              XML-RPC logins do not require this as XML-RPC.NET 
         *              automatically populates the struct properly using attributes 
         * @throws IOException 
         */
        private void ParseLoginReply(OSDMap reply)
        {
            AgentID = reply.get("agent_id").AsUUID();
            SessionID = reply.get("session_id").AsUUID();
            SecureSessionID = reply.get("secure_session_id").AsUUID();
            FirstName = reply.get("first_name").AsString();
            LastName = reply.get("last_name").AsString();
            StartLocation = reply.get("start_location").AsString();
            AgentAccess = reply.get("agent_access").AsString();
            LookAt = reply.get("look_at").AsVector3();
            Reason = reply.get("reason").AsString();
            Message = reply.get("message").AsString();

            Login = reply.get("login").AsString();
            Success = reply.get("login").AsBoolean();

            // Home
            OSD osdHome = null;
			try
			{
				osdHome = LLSDNotation.parse(reply.get("home").AsString());
			}
			catch (Exception ex)
			{
                Logger.Log("Login server returned (some) invalid data: " + ex.getMessage(), LogLevel.Warning, ex);
			}
			
			if (osdHome != null && osdHome.getType().equals(OSDType.Map))
            {
            	OSDMap home = (OSDMap)osdHome;

                OSD homeRegion = home.get("region_handle");
                if (homeRegion != null && homeRegion.getType().equals(OSDType.Array))
                {
                    OSDArray homeArray = (OSDArray)homeRegion;
                    if (homeArray.size() == 2)
                    {
                        HomeRegion = Helpers.UIntsToLong(homeArray.get(0).AsInteger(), homeArray.get(1).AsInteger());
                    }
                    else
                    {
                        HomeRegion = 0;
                    }
                }
                HomePosition = home.get("position").AsVector3();
                HomeLookAt = home.get("look_at").AsVector3();
            }
            else
            {
                HomeRegion = 0;
                HomePosition = Vector3.Zero;
                HomeLookAt = Vector3.Zero;
            }

            CircuitCode = reply.get("circuit_code").AsUInteger();
            RegionX = reply.get("region_x").AsUInteger();
            RegionY = reply.get("region_y").AsUInteger();
            SimPort = (short)reply.get("sim_port").AsUInteger();
            SimIP = reply.get("sim_ip").AsInetAddress();
            SeedCapability = reply.get("seed_capability").AsString();

            // Buddy list
            OSD buddyLLSD = reply.get("buddy-list");
            if (buddyLLSD != null && buddyLLSD.getType().equals(OSDType.Array))
            {
                OSDArray buddyArray = (OSDArray)buddyLLSD;
                BuddyList = new BuddyListEntry[buddyArray.size()];
                for (int i = 0; i < buddyArray.size(); i++)
                {
                    if (buddyArray.get(i).getType().equals(OSDType.Map))
                    {
                        BuddyListEntry bud = new BuddyListEntry();
                        OSDMap buddy = (OSDMap)buddyArray.get(i);

                        bud.buddy_id = buddy.get("buddy_id").AsString();
                        bud.buddy_rights_given = buddy.get("buddy_rights_given").AsUInteger();
                        bud.buddy_rights_has = buddy.get("buddy_rights_has").AsUInteger();

                        BuddyList[i] = bud;
                    }
                }
            }

            SecondsSinceEpoch = reply.get("seconds_since_epoch").AsUInteger();

            InventoryRoot = ParseMappedUUID("inventory-root", "folder_id", reply);
            InventorySkeleton = ParseInventorySkeleton("inventory-skeleton", reply);

            LibraryOwner = ParseMappedUUID("inventory-lib-owner", "agent_id", reply);
            LibraryRoot = ParseMappedUUID("inventory-lib-root", "folder_id", reply);
            LibrarySkeleton = ParseInventorySkeleton("inventory-skel-lib", reply);
        }

        private void ParseLoginReply(HashMap<String, Object> reply)
        {
            try
            {
                AgentID = ParseUUID("agent_id", reply);
                SessionID = ParseUUID("session_id", reply);
                SecureSessionID = ParseUUID("secure_session_id", reply);
                FirstName = ParseString("first_name", reply);
                if (FirstName.startsWith("\"")) {
                	FirstName.substring(1);
                }
                if (FirstName.endsWith("\"")) {
                	FirstName.substring(0, FirstName.length() - 1);
                }
                LastName = ParseString("last_name", reply);
                if (LastName.startsWith("\"")) {
                	LastName.substring(1);
                }
                if (LastName.endsWith("\"")) {
                	LastName.substring(0, LastName.length() - 1);
                }
                // "first_login" for brand new accounts
                StartLocation = ParseString("start_location", reply);
                AgentAccess = ParseString("agent_access", reply);
                LookAt = ParseVector3("look_at", reply);
                Reason = ParseString("reason", reply);
                Message = ParseString("message", reply);

                if (reply.containsKey("login"))
                {
                    Login = ParseString("login", reply);
                    Success = Login.equals("true");

                    // Parse redirect options
                    if (Login.equals("indeterminate"))
                    {
                        NextUrl = ParseString("next_url", reply);
                        NextDuration = ParseUInt("next_duration", reply);
                        NextMethod = ParseString("next_method", reply);
                        NextOptions = ParseArray("next_options", reply);
                    }
                }
            }
            catch (Exception ex)
            {
                Logger.Log("Login server returned (some) invalid data: " + ex.getMessage(), LogLevel.Warning, ex);
            }

            if (!Success)
            {
                return;
            }

            // Home
            if (reply.containsKey("home"))
            {
                OSD osdHome = null;
				try
				{
					osdHome = LLSDNotation.parse(reply.get("home").toString());
				}
				catch (Exception ex)
				{
	                Logger.Log("Login server returned (some) invalid data: " + ex.getMessage(), LogLevel.Warning, ex);
				}

                if (osdHome != null && osdHome.getType().equals(OSDType.Map))
                {
                	OSDMap home = (OSDMap)osdHome;

                    OSD homeRegion = home.get("region_handle");
                    if (homeRegion != null && homeRegion.getType().equals(OSDType.Array))
                    {
                        OSDArray homeArray = (OSDArray)homeRegion;
                        if (homeArray.size() == 2)
                        {
                            HomeRegion = Helpers.UIntsToLong(homeArray.get(0).AsUInteger(), homeArray.get(1).AsUInteger());
                        }
                        else
                        {
                            HomeRegion = 0;
                        }
                    }
                    HomePosition = home.get("position").AsVector3();
                    HomeLookAt = home.get("look_at").AsVector3();
                }
            }
            else
            {
                HomeRegion = 0;
                HomePosition = Vector3.Zero;
                HomeLookAt = Vector3.Zero;
            }

            CircuitCode = ParseUInt("circuit_code", reply);
            RegionX = ParseUInt("region_x", reply);
            RegionY = ParseUInt("region_y", reply);
            SimPort = (short)ParseUInt("sim_port", reply);
            try
			{
				SimIP = InetAddress.getByName(ParseString("sim_ip", reply));
			}
            catch (UnknownHostException e)
			{
				SimIP = null;
			}
            SeedCapability = ParseString("seed_capability", reply);

            // Buddy list
            if (reply.containsKey("buddy-list") && reply.get("buddy-list") instanceof ArrayList)
            {
                ArrayList<BuddyListEntry> buddys = new ArrayList<BuddyListEntry>();

                @SuppressWarnings("unchecked")
				ArrayList<Object> buddyArray = (ArrayList<Object>)reply.get("buddy-list");
                for (int i = 0; i < buddyArray.size(); i++)
                {
                    if (buddyArray.get(i) instanceof Hashtable)
                    {
                        @SuppressWarnings("unchecked")
						HashMap<String, Object> buddy = (HashMap<String, Object>)buddyArray.get(i);
                        BuddyListEntry bud = new BuddyListEntry();

                        bud.buddy_id = ParseString("buddy_id", buddy);
                        bud.buddy_rights_given = ParseUInt("buddy_rights_given", buddy);
                        bud.buddy_rights_has = ParseUInt("buddy_rights_has", buddy);

                        buddys.add(bud);
                    }
                }
                BuddyList = buddys.toArray(BuddyList);
            }

            SecondsSinceEpoch = ParseUInt("seconds_since_epoch", reply);

            InventoryRoot = ParseMappedUUID("inventory-root", "folder_id", reply);
            InventorySkeleton = ParseInventorySkeleton("inventory-skeleton", reply);

            LibraryOwner = ParseMappedUUID("inventory-lib-owner", "agent_id", reply);
            LibraryRoot = ParseMappedUUID("inventory-lib-root", "folder_id", reply);
            LibrarySkeleton = ParseInventorySkeleton("inventory-skel-lib", reply);

            // UDP Blacklist
            if (reply.containsKey("udp_blacklist"))
            {
                UDPBlacklist = ParseString("udp_blacklist", reply);
            }
        }

        public InventoryFolder[] ParseInventoryFolders(String key, UUID owner, OSDMap reply)
        {
            OSD skeleton = reply.get(key);
            if (skeleton != null && skeleton.getType().equals(OSDType.Array))
            {
                OSDArray array = (OSDArray)skeleton;
                InventoryFolder[] folders = new InventoryFolder[array.size()];
                for (int i = 0; i < array.size(); i++)
                {
                    if (array.get(i).getType().equals(OSDType.Map))
                    {
                        OSDMap map = (OSDMap)array.get(i);
                        InventoryFolder folder = new InventoryFolder(map.get("folder_id").AsUUID());
                        folder.Name = map.get("name").AsString();
                        folder.ParentUUID = map.get("parent_id").AsUUID();
                        folder.preferredType = AssetType.setValue(map.get("type_default").AsInteger());
                        folder.version = map.get("version").AsInteger();
                        folder.OwnerID = owner;

                        folders[i] = folder;
                    }
                }
                return folders;
            }
            return null;
        }

        public InventoryFolder[] ParseInventorySkeleton(String key, OSDMap reply)
        {
            OSD skeleton = reply.get(key);
            if (skeleton != null && skeleton.getType().equals(OSDType.Array))
            {
                OSDArray array = (OSDArray)skeleton;
                InventoryFolder[] folders = new InventoryFolder[array.size()];
                for (int i = 0; i < array.size(); i++)
                {
                    if (array.get(i).getType().equals(OSDType.Map))
                    {
                        OSDMap map = (OSDMap)array.get(i);
                        InventoryFolder folder = new InventoryFolder(map.get("folder_id").AsUUID());
                        folder.Name = map.get("name").AsString();
                        folder.ParentUUID = map.get("parent_id").AsUUID();
                        folder.preferredType = AssetType.setValue(map.get("type_default").AsInteger());
                        folder.version = map.get("version").AsInteger();
                        folders[i] = folder;
                    }
                }
                return folders;
            }
            return null;
        }

        public InventoryFolder[] ParseInventorySkeleton(String key, HashMap<String, Object> reply)
        {
            UUID ownerID;
            if (key.equals("inventory-skel-lib"))
            {
                ownerID = LibraryOwner;
            }
            else
            {
                ownerID = AgentID;
            }

            if (reply.containsKey(key) && reply.get(key) instanceof ArrayList)
            {
                @SuppressWarnings("unchecked")
				ArrayList<Object> array = (ArrayList<Object>)reply.get(key);
                InventoryFolder[] folders = new InventoryFolder[array.size()];
                for (int i = 0; i < array.size(); i++)
                {
                    if (array.get(i) instanceof Hashtable)
                    {
                        @SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>)array.get(i);
                        InventoryFolder folder = new InventoryFolder(ParseUUID("folder_id", map));
                        folder.Name = ParseString("name", map);
                        folder.ParentUUID = ParseUUID("parent_id", map);
                        folder.preferredType = AssetType.setValue(ParseUInt("type_default", map));
                        folder.version = ParseUInt("version", map);
                        folder.OwnerID = ownerID;

                        folders[i] = folder;
                    }
                }
                return folders;
            }
            return null;
        }
    }
    // #endregion Structs

    // #region Callback handlers

    // An event for being logged out either through client request, server forced, or network error
	public class LoginProgressCallbackArgs  extends CallbackArgs
	{
        private final LoginStatus m_Status;
        private final String m_Message;
        private final String m_FailReason;

        public final LoginStatus getStatus()
        {
            return m_Status;
        }
        public final String getMessage()
        {
            return m_Message;
        }
        public final String getFailReason()
        {
            return m_FailReason;
        }

        public LoginProgressCallbackArgs(LoginStatus login, String message, String failReason)
        {
            this.m_Status = login;
            this.m_Message = message;
            this.m_FailReason = failReason;
        }
	}

	public CallbackHandlerQueue<LoginProgressCallbackArgs> OnLoginProgress = new CallbackHandlerQueue<LoginProgressCallbackArgs>();

    private void LoginProgressCallback(LoginStatus status, String message, String InternalErrorKey)
	{
		OnLoginProgress.dispatch(new LoginProgressCallbackArgs(status, message, InternalErrorKey));
	}


	// Called when a reply is received from the login server, the login sequence will block until this event returns
    public class LoginResponseCallbackArgs extends CallbackArgs
    {
    	private boolean success;
    	private boolean redirect;
    	private String message;
    	private String reason;
    	private LoginResponseData reply;
		
    	public boolean getSuccess()
    	{
    		return success;
    	}
    	
    	public boolean getRedirect()
    	{
    		return redirect;
    	}
    	
    	public String getMessage()
    	{
    		return message;
    	}
    	
    	public String getReason()
    	{
    		return reason;
    	}
    	
    	public LoginResponseData getReply()
    	{
    		return reply;
    	}
		
		public LoginResponseCallbackArgs(boolean success, boolean redirect,
				String message, String reason, LoginResponseData reply) {
			this.success = success;
			this.redirect = redirect;
			this.message = message;
			this.reason = reason;
			this.reply = reply;
		}

    }

    private CallbackHandlerQueue<LoginResponseCallbackArgs> OnLoginResponse = new CallbackHandlerQueue<LoginResponseCallbackArgs>();

    private HashMap<CallbackHandler<LoginResponseCallbackArgs>, String[]> CallbackOptions = new HashMap<CallbackHandler<LoginResponseCallbackArgs>, String[]>();

	private void LoginResponseCallback(boolean loginSuccess, boolean redirect, String message, String reason, LoginResponseData replyData)
	{
		OnLoginResponse.dispatch(new LoginResponseCallbackArgs(loginSuccess, redirect, message, reason, replyData));
	}

	public final void RegisterLoginResponseCallback(CallbackHandler<LoginResponseCallbackArgs> callback, String[] options, boolean autoremove)
    {
        CallbackOptions.put(callback, options);
        OnLoginResponse.add(callback, autoremove);
    }
    
    public final void UnregisterLoginResponseCallback(CallbackHandler<LoginResponseCallbackArgs> callback)
    {
        CallbackOptions.remove(callback);
        OnLoginResponse.remove(callback);
    }
    // #endregion Callback handlers

	
    // #region Public Members
    /** Seed CAPS URL returned from the login server */
    public String LoginSeedCapability = "";

    // #endregion

    // #region Private Members
    private GridClient _Client;

    private TimeoutEventQueue<LoginStatus> LoginEvents = new TimeoutEventQueue<LoginStatus>();
    // #endregion

    
	public LoginManager(GridClient client)
	{
		this._Client = client;
	}

    // #region Public Methods

    /** Login Routines */

	public final LoginParams DefaultLoginParams(String firstName, String lastName, String password) throws Exception
	{
		return new LoginParams(_Client, firstName, lastName, password, Settings.APPLICATION_NAME, Settings.APPLICATION_VERSION);
	}

	/**
     * Generate sane default values for a login request
     * 
     * @param firstName Account first name
     * @param lastName Account last name
     * @param password Account password
     * @param userAgent Client application name
     * @param userVersion Client application version
     * @return A populated {@link LoginParams} struct containing sane defaults
     */
    public final LoginParams DefaultLoginParams(String firstName, String lastName, String password, String userAgent, String userVersion)
    {
        return new LoginParams(_Client, firstName, lastName, password, userAgent, userVersion);
    }

    /**
     * Simplified login that takes the most common and required fields
     * To receive
     * 
     * @param firstName Account first name
     * @param lastName Account last name
     * @param password Account password
     * @param userAgent Client application name
     * @param userVersion Client application version
     * @return Whether the login was successful or not. On failure the
     *           LoginErrorKey string will contain the error code and LoginMessage
     *           will contain a description of the error
     * @throws Exception 
     */
    public final boolean Login(String firstName, String lastName, String password, String userAgent, String userVersion) throws Exception
    {
        LoginParams loginParams = new LoginParams(_Client, firstName, lastName, password, userAgent, userVersion);
        return Login(loginParams);
    }

    /**
     * Simplified login that takes the most common fields along with a
     * starting location URI, and can accept an MD5 string instead of a plaintext password
     *  
     * @param firstName Account first name
     * @param lastName Account last name
     * @param password Account password or MD5 hash of the password
     *          such as $1$1682a1e45e9f957dcdf0bb56eb43319c
     * @param userAgent Client application name
     * @param start Starting location URI that can be built with StartLocation()
     * @param userVersion Client application version
     * @return Whether the login was successful or not. On failure the
     *           LoginErrorKey string will contain the error code and LoginMessage
     *           will contain a description of the error
     * @throws Exception 
     */
    public final boolean Login(String firstName, String lastName, String password, String userAgent, String start, String userVersion) throws Exception
    {
        LoginParams loginParams = new LoginParams(_Client, firstName, lastName, password, userAgent, userVersion);
        loginParams.Start = start;

        return Login(loginParams);
    }

    /**
     * Login that takes a struct of all the values that will be passed to the login server
     * 
     * @param loginParams The values that will be passed to the login
     *          server, all fields must be set even if they are ""
     * @return Whether the login was successful or not. On failure the
     *           LoginErrorKey string will contain the error code and LoginMessage
     *           will contain a description of the error
     * @throws Throwable 
     */
    public final boolean Login(LoginParams loginParams) throws Exception
    {
        // FIXME: Now that we're using CAPS we could cancel the current login and start a new one
        if (LoginEvents.size() != 0)
        {
            throw new Exception("Login already in progress");
        }

        TimeoutEvent<LoginStatus> loginEvent = LoginEvents.create();
        RequestLogin(loginParams);
        LoginStatus status = loginEvent.waitOne(loginParams.Timeout);
        LoginEvents.cancel(loginEvent);
        if (status == null)
        {
            UpdateLoginStatus(LoginStatus.Failed, "Timed out", null);
            return false;
        }
        return (status == LoginStatus.Success);
    }

    /**
     * Build a start location URI for passing to the Login function
     * 
     * @param sim Name of the simulator to start in
     * @param x X coordinate to start at
     * @param y Y coordinate to start at
     * @param z Z coordinate to start at
     * @return String with a URI that can be used to login to a specified location
     */
    public static String StartLocation(String sim, int x, int y, int z)
    {
        return String.format("uri:%s&%d&%d&%d", sim, x, y, z);
    }

    public void RequestLogin(final LoginParams loginParams) throws Exception
    {
        // #region Sanity Check loginParams

        if (loginParams.Options == null)
        {
            loginParams.Options = new String[] {};
        }

        // Convert the password to MD5 if it isn't already
        if (loginParams.Password.length() != 35 && !loginParams.Password.startsWith("$1$"))
        {
            loginParams.Password = Helpers.MD5Password(loginParams.Password);
        }

        if (loginParams.ViewerDigest == null)
        {
            loginParams.ViewerDigest = "";
        }

        if (loginParams.Version == null)
        {
            loginParams.Version = "";
        }

        if (loginParams.UserAgent == null)
        {
            loginParams.UserAgent = "";
        }

        if (loginParams.Platform == null)
        {
            loginParams.Platform = "";
        }

        if (loginParams.MAC == null)
        {
            loginParams.MAC = "";
        }

        if (loginParams.Channel == null)
        {
            loginParams.Channel = "";
        }

        if (loginParams.Author == null)
        {
            loginParams.Author = "";
        }
        // #endregion

        // TODO: Allow a user callback to be defined for handling the cert
        // ServicePointManager.CertificatePolicy = new TrustAllCertificatePolicy();
        // Even though this will compile on Mono 2.4, it throws a runtime exception
        //ServicePointManager.ServerCertificateValidationCallback = TrustAllCertificatePolicy.TrustAllCertificateHandler;

        URI loginUri;
        try
        {
            loginUri = new URI(loginParams.URI);
        }
        catch (Exception ex)
        {
            Logger.Log(String.format("Failed to parse login URI %s, %s", loginParams.URI, ex.getMessage()), LogLevel.Error, _Client);
            return;
        }

        if (_Client.Settings.USE_LLSD_LOGIN)
        {
            // #region LLSD Based Login

            // Create the CAPS login structure
            OSDMap loginLLSD = new OSDMap();
            loginLLSD.put("first", OSD.FromString(loginParams.FirstName));
            loginLLSD.put("last", OSD.FromString(loginParams.LastName));
            loginLLSD.put("passwd", OSD.FromString(loginParams.Password));
            loginLLSD.put("start", OSD.FromString(loginParams.Start));
            loginLLSD.put("channel", OSD.FromString(loginParams.Channel));
            loginLLSD.put("version", OSD.FromString(loginParams.Version));
            loginLLSD.put("platform", OSD.FromString(loginParams.Platform));
            loginLLSD.put("mac", OSD.FromString(loginParams.MAC));
            loginLLSD.put("agree_to_tos", OSD.FromBoolean(loginParams.AgreeToTos));
            loginLLSD.put("read_critical", OSD.FromBoolean(loginParams.ReadCritical));
            loginLLSD.put("viewer_digest", OSD.FromString(loginParams.ViewerDigest));
            loginLLSD.put("id0", OSD.FromString(loginParams.ID0));

            // Create the options LLSD array
            OSDArray optionsOSD = new OSDArray(loginParams.Options.length);
            for (int i = 0; i < loginParams.Options.length; i++)
            {
                optionsOSD.add(OSD.FromString(loginParams.Options[i]));
            }

            for (String[] callbackOpts : CallbackOptions.values())
            {
                if (callbackOpts != null)
                {
                    for (int i = 0; i < callbackOpts.length; i++)
                    {
                        if (!optionsOSD.contains(callbackOpts[i]))
                        {
                            optionsOSD.add(OSD.FromString(callbackOpts[i]));
                        }
                    }
                }
            }
            loginLLSD.put("options", optionsOSD);

            // Make the CAPS POST for login

            CapsClient loginRequest = new CapsClient(loginUri);
            UpdateLoginStatus(LoginStatus.ConnectingToLogin, "Logging in as " + loginParams.FirstName + " " + loginParams.LastName + " ...", null);
            loginRequest.BeginGetResponse(loginLLSD, OSDFormat.Xml, loginParams.Timeout, new LoginReplyLLSDHandler(loginParams));
            // #endregion
        }
        else
        {
            // #region XML-RPC Based Login Code

            // Create the Hashtable for XmlRpcCs
            HashMap<String, Object> loginXmlRpc = new HashMap<String, Object>();
            loginXmlRpc.put("first", loginParams.FirstName);
            loginXmlRpc.put("last", loginParams.LastName);
            loginXmlRpc.put("passwd", loginParams.Password);
            loginXmlRpc.put("start", loginParams.Start);
            loginXmlRpc.put("channel", loginParams.Channel);
            loginXmlRpc.put("version", loginParams.Version);
            loginXmlRpc.put("platform", loginParams.Platform);
            loginXmlRpc.put("mac", loginParams.MAC);
            if (loginParams.AgreeToTos)
            {
                loginXmlRpc.put("agree_to_tos", "true");
            }
            if (loginParams.ReadCritical)
            {
                loginXmlRpc.put("read_critical", "true");
            }
            loginXmlRpc.put("id0", loginParams.ID0);
            loginXmlRpc.put("last_exec_event", 0);

            // Create the options array
            ArrayList<String> options = new ArrayList<String>();
            for (int i = 0; i < loginParams.Options.length; i++)
            {
                options.add(loginParams.Options[i]);
            }

            for (String[] callbackOpts : CallbackOptions.values())
            {
                if (callbackOpts != null)
                {
                    for (int i = 0; i < callbackOpts.length; i++)
                    {
                        if (!options.contains(callbackOpts[i]))
                        {
                            options.add(callbackOpts[i]);
                        }
                    }
                }
            }
            loginXmlRpc.put("options", options);

            try
            {
                final XMLRPCClient client = new XMLRPCClient(loginUri);
                final Object[] request = new Object[] { loginXmlRpc };
                
                if (loginUri.getScheme().equals("https") && loginUri.getHost().contains("linden"))
                {
                    client.register(new Scheme("https", 443, new SSLSocketFactory(Helpers.GetExtendedKeyStore(null))));
                }

                // Start the request
                Thread requestThread = new Thread()
                {
                	@Override
					public void run()
                	{
                		try
                        {
                			Object data = client.callEx(loginParams.MethodName, request);
                            LoginReplyXmlRpcHandler(data, loginParams);
                        }
						catch (Exception ex)
						{
                            UpdateLoginStatus(LoginStatus.Failed, "Error opening the login server connection", ex.getMessage());
						}
                    }
                };
                requestThread.setName("XML-RPC Login");
                requestThread.start();
            }
            catch (Exception ex)
            {
                UpdateLoginStatus(LoginStatus.Failed, "Error connecting to the login server", ex.getMessage());
                throw ex;
            }
            // #endregion
        }
    }
    // #endregion

    // #region Private Methods

    private void UpdateLoginStatus(LoginStatus status, String message, String reason)
    {
        Logger.DebugLog("Login status: " + status.toString() + ", Message: " + message + (reason != null ? " Reason: " + reason : ""), _Client);

        // If we reached a login resolution trigger the event
        if (status == LoginStatus.Success || status == LoginStatus.Failed)
        {
            LoginEvents.set(status);
        }
        // Fire the login status callback
        LoginProgressCallback(status, message, reason);
    }

    
    /**
     * Handles response from XML-RPC login replies 
     * 
     * @param response the response object
     * @param the login params, used to start the next round on redirects
     * @throws Exception
     */
	private void LoginReplyXmlRpcHandler(Object response, LoginParams loginParams)
    {
        LoginResponseData reply = new LoginResponseData();

        // Fetch the login response
        if (response == null || !(response instanceof HashMap))
        {
            UpdateLoginStatus(LoginStatus.Failed, "Invalid or missing login response from the server", null);
        }

        @SuppressWarnings("unchecked")
        HashMap<String, Object> result = (HashMap<String, Object>)response;
        reply.ParseLoginReply(result);

        if (reply.Success)
        {
            // Remove the quotes around our first name.
            if (reply.FirstName.charAt(0) == '"')
            {
                reply.FirstName = reply.FirstName.substring(1);
            }
            if (reply.FirstName.charAt(reply.FirstName.length() - 1) == '"')
            {
                reply.FirstName = reply.FirstName.substring(0, reply.FirstName.length() - 1);
            }
        }
	    try
		{
			HandleLoginResponse(reply, loginParams);
		}
		catch (Exception ex)
		{
            UpdateLoginStatus(LoginStatus.Failed, ex.getMessage(), ex.getStackTrace().toString());
		}
    }

    /**
     * Handle response from LLSD login replies
     * 
     * @param client
     * @param result
     * @param error
     */
    private class LoginReplyLLSDHandler implements FutureCallback<OSD>
    {
    	private final LoginParams loginParams;
    	
    	public LoginReplyLLSDHandler(LoginParams loginParams)
    	{
    		this.loginParams = loginParams;
    	}
    	
		@Override
		public void completed(OSD result)
		{
            if (result != null && result.getType().equals(OSDType.Map))
            {
                OSDMap map = (OSDMap)result;

                LoginResponseData reply = new LoginResponseData();
			    reply.ParseLoginReply(map);

			    try
			    {
			    	HandleLoginResponse(reply, loginParams);
			    }
			    catch (Exception ex)
			    {
		            UpdateLoginStatus(LoginStatus.Failed, ex.getMessage(), ex.getStackTrace().toString());
			    }
            }
            else
            {
                // No LLSD response
                 UpdateLoginStatus(LoginStatus.Failed, "Empty or unparseable login response", "bad response");
            }
		}

		@Override
		public void failed(Exception ex)
		{
            // Connection error
            UpdateLoginStatus(LoginStatus.Failed, ex.getMessage(), ex.getStackTrace().toString());
		}

		@Override
		public void cancelled()
		{
            // Connection canceled
            UpdateLoginStatus(LoginStatus.Failed, "connection canceled", "connection canceled");
		}
    }
    
    private void HandleLoginResponse(LoginResponseData reply, LoginParams loginParams) throws Exception
    {
        boolean redirect = reply.Login.equals("indeterminate");
        LoginResponseCallback(reply.Success, redirect, reply.Message, reply.Reason, reply);

        if (redirect)
        {
            // Login redirected

            // Make the next login URL jump
            UpdateLoginStatus(LoginStatus.Redirecting, reply.Message, null);
            loginParams.URI = reply.NextUrl;
            loginParams.MethodName = reply.NextMethod;
            loginParams.Options = reply.NextOptions;

            // Sleep for some amount of time while the servers work
            int seconds = reply.NextDuration;
            Logger.Log("Sleeping for " + seconds + " seconds during a login redirect", LogLevel.Info, _Client);
			try
			{
				Thread.sleep(seconds * 1000);
			}
			catch (InterruptedException ex) { }

            RequestLogin(loginParams);
        }
        else if (reply.Success)
        {
            // Login succeeded
            _Client.Network.setCircuitCode(reply.CircuitCode);
            _Client.Network.setUDPBlackList(reply.UDPBlacklist);
            LoginSeedCapability = reply.SeedCapability;

            UpdateLoginStatus(LoginStatus.ConnectingToSim, "Connecting to simulator...", null);

            if (reply.SimIP != null && reply.SimPort != 0)
            {
				// Connect to the sim given in the login reply
				if (_Client.Network.Connect(reply.SimIP, reply.SimPort, Helpers.UIntsToLong(reply.RegionX, reply.RegionY), true, LoginSeedCapability) != null)
				{
					// Request the economy data right after login
					_Client.Network.SendPacket(new EconomyDataRequestPacket());

					// Update the login message with the MOTD returned from the server
					UpdateLoginStatus(LoginStatus.Success, reply.Message, null);
				}
				else
				{
					UpdateLoginStatus(LoginStatus.Failed, "Unable to establish a UDP connection to the simulator", null);
				}
            }
            else
            {
                UpdateLoginStatus(LoginStatus.Failed, "Login server did not return a simulator address", null);
            }
        }
        else
        {
            // Login failed

        	// Make sure a usable error key is set
            if (Helpers.isEmpty(reply.Reason))
            {
            	reply.Reason = "unknown";
            }
            UpdateLoginStatus(LoginStatus.Failed, reply.Message, reply.Reason);
        }
    }
    // #endregion

    // #region Parsing Helpers
    private static int ParseUInt(String key, HashMap<String, Object> reply)
    {
        if (reply.containsKey(key))
        {
            Object value = reply.get(key);
            if (value instanceof Integer)
            {
                return (Integer)value;
            }
        }
        return 0;
    }

    private static UUID ParseUUID(String key, HashMap<String, Object> reply)
    {
        if (reply.containsKey(key))
        {
            return new UUID(reply.get(key).toString());
        }
        return UUID.Zero;
    }

    private static String ParseString(String key, HashMap<String, Object> reply)
    {
        if (reply.containsKey(key))
        {
            return reply.get(key).toString();
        }
        return Helpers.EmptyString;
    }

    private static Vector3 ParseVector3(String key, HashMap<String, Object> reply) throws ParseException, IOException
    {
        if (reply.containsKey(key))
        {
            Object value = reply.get(key);

            if (value instanceof List)
            {
                @SuppressWarnings("unchecked")
				List<String> list = (List<String>)value;
                if (list.size() == 3)
                {
                    float x, y, z;
                    x = Helpers.TryParseFloat(list.get(0));
                    y = Helpers.TryParseFloat(list.get(1));
                    z = Helpers.TryParseFloat(list.get(2));

                    return new Vector3(x, y, z);
                }
            }
            else if (value instanceof String)
            {
            	OSD osd = LLSDNotation.parse((String)value);
            	if (osd != null && osd.getType().equals(OSDType.Array))
                    return ((OSDArray)osd).AsVector3();
            }
        }
        return Vector3.Zero;
    }

    private static UUID ParseMappedUUID(String key, String key2, OSDMap reply)
    {
        OSD folderOSD = reply.get(key);
        if (folderOSD != null && folderOSD.getType().equals(OSDType.Array))
        {
            OSDArray array = (OSDArray)folderOSD;
            if (array.size() == 1 && array.get(0).getType().equals(OSDType.Map))
            {
                OSDMap map = (OSDMap)array.get(0);
                OSD folder = map.get(key2);
                if (folder != null)
                {
                    return folder.AsUUID();
                }
            }
        }
        return UUID.Zero;
    }

    private static UUID ParseMappedUUID(String key, String key2, HashMap<String, Object> reply)
    {
        if (reply.containsKey(key) && reply.get(key) instanceof ArrayList)
        {
            @SuppressWarnings("unchecked")
			ArrayList<Object> array = (ArrayList<Object>)reply.get(key);
            if (array.size() == 1 && array.get(0) instanceof Hashtable)
            {
                @SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>)array.get(0);
                return ParseUUID(key2, map);
            }
        }
        return UUID.Zero;
    }

    private static String[] ParseArray(String key, HashMap<String, Object> reply)
    {
        Object o = reply.get(key);
        if (o instanceof List)
        {
            @SuppressWarnings("unchecked")
			List<Object>array = (List<Object>)o;
            String[] strings = new String[array.size()];
            for (int i = 0; i < array.size(); i++)
            {
            	strings[i] = array.get(i).toString();
            }
        }
        return null;
    }
    // #endregion Parsing Helpers

    // #region CallbackArgs

}