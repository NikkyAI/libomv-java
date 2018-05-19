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

import java.net.URI;
import java.util.HashMap;

import org.apache.http.concurrent.FutureCallback;
import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.io.capabilities.AsyncHTTPClient;
import libomv.io.capabilities.CapsClient;
import libomv.io.capabilities.RpcClient;
import libomv.model.Grid.GridInfo;
import libomv.packets.EconomyDataRequestPacket;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.TimeoutEvent;
import libomv.utils.TimeoutEventQueue;

public class LoginManager implements libomv.model.Login {
	private static final Logger logger = Logger.getLogger(LoginManager.class);

	/** Login Request Parameters */
	public class LoginParams {
		/** The URL of the Login Server */
		public String URI;
		/**
		 * The number of milliseconds to wait before a login is considered failed due to
		 * timeout
		 */
		public int Timeout;
		/**
		 * The request method login_to_simulator is currently the only supported method
		 */
		public String MethodName;
		/** The Agents First name */
		public String FirstName;
		/** The Agents First name */
		public String LastName;
		/**
		 * A md5 hashed password, plaintext password will be automatically hashed
		 */
		public String Password;
		/**
		 * The agents starting location once logged in Either "last", "home", or a
		 * string encoded URI containing the simulator name and x/y/z coordinates e.g:
		 * uri:hooper&amp;128&amp;152&amp;17
		 */
		public String Start;
		/**
		 * A string containing the client software channel information <example>Second
		 * Life Release</example>
		 */
		public String Channel;
		/**
		 * The client software version information The official viewer uses: Second Life
		 * Release n.n.n.n where n is replaced with the current version of the viewer
		 */
		public String Version;
		/** A string containing the platform information the agent is running on */
		public String Platform;
		/** A string containing version number for OS the agent is running on */
		public String PlatformVersion;
		/** A string hash of the network cards Mac Address */
		public String MAC;
		/** Unknown or deprecated */
		public String ViewerDigest;
		/**
		 * A string hash of the first disk drives ID used to identify this clients
		 * uniqueness
		 */
		public String ID0;
		/**
		 * A string containing the viewers Software, this is not directly sent to the
		 * login server but is used by the library to generate the Version information
		 */
		public String UserAgent;
		/**
		 * A string representing the software creator. This is not directly sent to the
		 * login server but is used by the library to generate the Version information
		 */
		public String Author;
		/**
		 * If true, this agent agrees to the Terms of Service of the grid its connecting
		 * to
		 */
		public boolean AgreeToTos;
		/** Unknown */
		public boolean ReadCritical;
		/**
		 * Status of the last application run sent to the grid login server for
		 * statistical purposes
		 */
		public LastExecStatus LastExecEvent = LastExecStatus.Normal;
		/**
		 * An array of string sent to the login server to enable various options
		 */
		public String[] Options;

		/**
		 * Default constructor, initializes sane default values
		 */
		public LoginParams() {
			this.Options = new String[] { "inventory-root", "inventory-skeleton", "inventory-lib-root",
					"inventory-lib-owner", "inventory-skel-lib", "initial-outfit", "gestures", "event_categories",
					"event_notifications", "classified_categories", "adult_compliant", "buddy-list", "ui-config",
					"map-server-url", "tutorial_settings", "login-flags", "global-textures" };
			this.MethodName = "login_to_simulator";
			this.Start = "last";
			this.Platform = Helpers.getPlatform();
			this.PlatformVersion = Helpers.getPlatformVersion();
			this.MAC = Helpers.getMAC();
			this.ViewerDigest = "";
			this.ID0 = Helpers.getMAC();
			this.AgreeToTos = true;
			this.ReadCritical = true;
			this.Channel = LibSettings.LIBRARY_NAME;
			this.Version = LibSettings.LIBRARY_VERSION;
			this.LastExecEvent = LastExecStatus.Normal;
		}

		public LoginParams(GridClient client) {
			this();
			this.Timeout = client.Settings.LOGIN_TIMEOUT;
			GridInfo gridInfo = client.getDefaultGrid();
			this.URI = gridInfo.loginuri;

			String names[] = gridInfo.username.split("[\\. ]");

			this.FirstName = names[0];
			if (names.length >= 2) {
				this.LastName = names[1];
			} else {
				this.LastName = "Resident";
			}

			this.Password = gridInfo.getPassword();
			if (gridInfo.startLocation != null)
				this.Start = gridInfo.startLocation;
		}

		/**
		 * Instantiates new LoginParams object and fills in the values
		 *
		 * @param client
		 *            Instance of GridClient to read settings from
		 * @param firstName
		 *            Login first name
		 * @param lastName
		 *            Login last name
		 * @param password
		 *            Password
		 * @param startLocation
		 *            location to start in, if null, "last" is used
		 */
		public LoginParams(GridClient client, String firstName, String lastName, String password,
				String startLocation) {
			this();
			this.URI = client.getDefaultGrid().loginuri;
			this.Timeout = client.Settings.LOGIN_TIMEOUT;
			this.FirstName = firstName;
			this.LastName = lastName;
			this.Password = password;
			if (startLocation != null)
				this.Start = startLocation;
		}
	}

	public CallbackHandler<LoginProgressCallbackArgs> OnLoginProgress = new CallbackHandler<LoginProgressCallbackArgs>();

	private HashMap<Callback<LoginProgressCallbackArgs>, String[]> CallbackOptions = new HashMap<Callback<LoginProgressCallbackArgs>, String[]>();

	public final void RegisterLoginProgressCallback(Callback<LoginProgressCallbackArgs> callback, String[] options,
			boolean autoremove) {
		if (options != null)
			CallbackOptions.put(callback, options);
		OnLoginProgress.add(callback, autoremove);
	}

	public final void UnregisterLoginProgressCallback(Callback<LoginProgressCallbackArgs> callback) {
		CallbackOptions.remove(callback);
		OnLoginProgress.remove(callback);
	}

	// #endregion Callback handlers

	// #region Private Members
	private GridClient _Client;
	private TimeoutEventQueue<LoginStatus> LoginEvents = new TimeoutEventQueue<LoginStatus>();
	private AsyncHTTPClient<OSD> httpClient;

	// #endregion

	public LoginManager(GridClient client) {
		this._Client = client;
	}

	// #region Public Methods

	// #region Login Routines

	/**
	 * Generate sane default values for a login request
	 *
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password
	 * @param startLocation
	 *            Location where to start such as "home", "last", or an explicit
	 *            start location
	 * @return A populated {@link LoginParams} struct containing sane defaults
	 */
	public final LoginParams DefaultLoginParams(String firstName, String lastName, String password,
			String startLocation) {
		return new LoginParams(_Client, firstName, lastName, password, startLocation);
	}

	/**
	 * Generate sane default values for a login request
	 *
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password
	 * @param channel
	 *            Client application name
	 * @param version
	 *            Client application version
	 * @return A populated {@link LoginParams} struct containing sane defaults
	 */
	public final LoginParams DefaultLoginParams(String firstName, String lastName, String password, String channel,
			String version) {
		LoginParams params = new LoginParams(_Client, firstName, lastName, password, null);
		params.Channel = channel;
		params.Version = version;
		return params;
	}

	/**
	 * Simplified login that takes the most common and required fields to receive
	 * Logs in to the last known position the avatar was in
	 *
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password
	 * @return Whether the login was successful or not. Register to the
	 *         OnLoginResponse callback to receive more detailed information about
	 *         the errors that have occurred
	 * @throws Exception
	 */
	public final boolean Login(String firstName, String lastName, String password) throws Exception {
		return Login(new LoginParams(_Client, firstName, lastName, password, null));
	}

	/**
	 * Simplified login that takes the most common and required fields to receive
	 *
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password
	 * @param startLocation
	 *            The location to login too, such as "last", "home", or an explicit
	 *            start location
	 * @return Whether the login was successful or not. Register to the
	 *         OnLoginResponse callback to receive more detailed information about
	 *         the errors that have occurred
	 * @throws Exception
	 */
	public final boolean Login(String firstName, String lastName, String password, String startLocation)
			throws Exception {
		return Login(new LoginParams(_Client, firstName, lastName, password, startLocation));
	}

	/**
	 * Simplified login that takes the most common and required fields To receive
	 *
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password
	 * @param channel
	 *            Client application name
	 * @param version
	 *            Client application version
	 * @return Whether the login was successful or not. Register to the
	 *         OnLoginResponse callback to receive more detailed information about
	 *         the errors that have occurred
	 * @throws Exception
	 */
	public final boolean Login(String firstName, String lastName, String password, String channel, String version)
			throws Exception {
		return Login(DefaultLoginParams(firstName, lastName, password, channel, version));
	}

	/**
	 * Simplified login that takes the most common fields along with a starting
	 * location URI, and can accept an MD5 string instead of a plaintext password
	 *
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password or MD5 hash of the password such as
	 *            $1$1682a1e45e9f957dcdf0bb56eb43319c
	 * @param start
	 *            Starting location URI that can be built with StartLocation()
	 * @param channel
	 *            Client application name
	 * @param version
	 *            Client application version
	 * @return Whether the login was successful or not. Register to the
	 *         OnLoginResponse callback to receive more detailed information about
	 *         the errors that have occurred
	 * @throws Exception
	 */
	public final boolean Login(String firstName, String lastName, String password, String start, String channel,
			String version) throws Exception {
		LoginParams loginParams = DefaultLoginParams(firstName, lastName, password, channel, version);
		loginParams.Start = start;

		return Login(loginParams, null);
	}

	/**
	 * Login that takes a struct of all the values that will be passed to the login
	 * server
	 *
	 * @param loginParams
	 *            The values that will be passed to the login server, all fields
	 *            must be set even if they are ""
	 * @return Whether the login was successful or not. Register to the
	 *         OnLoginResponse callback to receive more detailed information about
	 *         the errors that have occurred
	 * @throws Exception
	 */
	public final boolean Login(LoginParams loginParams) throws Exception {
		return Login(loginParams, null);
	}

	/**
	 * Login that takes a struct of all the values that will be passed to the login
	 * server
	 *
	 * @param loginParams
	 *            The values that will be passed to the login server, all fields
	 *            must be set even if they are ""
	 * @param callback
	 *            the progress callback to invoke with login progress updates
	 * @return Whether the login was successful or not. Register to the
	 *         OnLoginResponse callback to receive more detailed information about
	 *         the errors that have occurred
	 * @throws Exception
	 */
	public final boolean Login(LoginParams loginParams, Callback<LoginProgressCallbackArgs> callback) throws Exception {
		// FIXME: Now that we're using CAPS we could cancel the current login and start
		// a new one
		if (LoginEvents.size() != 0) {
			throw new Exception("Login already in progress");
		}

		TimeoutEvent<LoginStatus> loginEvent = LoginEvents.create();
		RequestLogin(loginParams, callback);
		LoginStatus status = loginEvent.waitOne(loginParams.Timeout);
		LoginEvents.cancel(loginEvent);
		if (status == null) {
			UpdateLoginStatus(LoginStatus.Failed, "Logon timed out", "timeout", null);
			return false;
		}
		return (status == LoginStatus.Success);
	}

	/**
	 * Build a start location URI for passing to the Login function
	 *
	 * @param sim
	 *            Name of the simulator to start in
	 * @param x
	 *            X coordinate to start at
	 * @param y
	 *            Y coordinate to start at
	 * @param z
	 *            Z coordinate to start at
	 * @return String with a URI that can be used to login to a specified location
	 */
	public static String StartLocation(String sim, int x, int y, int z) {
		return String.format("uri:%s&%d&%d&%d", sim, x, y, z);
	}

	/**
	 * Abort any ongoing login. If no login is currently ongoing, this function does
	 * nothing
	 */
	public void AbortLogin() {
		// FIXME: Now that we're using CAPS we could cancel the current login and start
		// a new one
		if (LoginEvents.size() != 0) {
			UpdateLoginStatus(LoginStatus.Failed, "Abort Requested", " aborted", null);
		}
	}

	public void RequestLogin(final LoginParams loginParams, Callback<LoginProgressCallbackArgs> callback)
			throws Exception {
		// #region Sanity Check loginParams
		if (loginParams.Options == null)
			loginParams.Options = new String[] {};

		if (loginParams.Password == null)
			loginParams.Password = Helpers.EmptyString;

		// Convert the password to MD5 if it isn't already
		if (loginParams.Password.length() != 35 && !loginParams.Password.startsWith("$1$"))
			loginParams.Password = Helpers.MD5Password(loginParams.Password);

		if (loginParams.ViewerDigest == null)
			loginParams.ViewerDigest = Helpers.EmptyString;

		if (loginParams.UserAgent == null)
			loginParams.UserAgent = Helpers.EmptyString;

		if (loginParams.Version == null)
			loginParams.Version = Helpers.EmptyString;

		if (loginParams.Platform == null)
			loginParams.Platform = Helpers.getPlatform();

		if (loginParams.PlatformVersion == null)
			loginParams.PlatformVersion = Helpers.getPlatformVersion();

		if (loginParams.MAC == null)
			loginParams.MAC = Helpers.getMAC();

		if (loginParams.Channel == null || loginParams.Channel.isEmpty()) {
			logger.warn("Viewer channel not set. This is a TOS violation on some grids.");
			loginParams.Channel = LibSettings.LIBRARY_NAME;
		}

		if (loginParams.Author == null)
			loginParams.Author = Helpers.EmptyString;
		// #endregion

		if (callback != null)
			RegisterLoginProgressCallback(callback, loginParams.Options, false);

		URI loginUri;
		try {
			loginUri = new URI(loginParams.URI);
		} catch (Exception ex) {
			logger.error(GridClient
					.Log(String.format("Failed to parse login URI %s, %s", loginParams.URI, ex.getMessage()), _Client));
			throw ex;
		}

		UpdateLoginStatus(LoginStatus.ConnectingToLogin,
				"Logging in as " + loginParams.FirstName + " " + loginParams.LastName + " ...", null, null);

		try {
			// Create the CAPS login structure
			OSDMap loginLLSD = new OSDMap();
			loginLLSD.put("first", OSD.FromString(loginParams.FirstName));
			loginLLSD.put("last", OSD.FromString(loginParams.LastName));
			loginLLSD.put("passwd", OSD.FromString(loginParams.Password));
			loginLLSD.put("start", OSD.FromString(loginParams.Start));
			loginLLSD.put("channel", OSD.FromString(loginParams.Channel));
			loginLLSD.put("version", OSD.FromString(loginParams.Version));
			loginLLSD.put("platform", OSD.FromString(loginParams.Platform));
			loginLLSD.put("platform_version", OSD.FromString(loginParams.PlatformVersion));
			loginLLSD.put("mac", OSD.FromString(loginParams.MAC));
			loginLLSD.put("agree_to_tos", OSD.FromBoolean(loginParams.AgreeToTos));
			loginLLSD.put("read_critical", OSD.FromBoolean(loginParams.ReadCritical));
			loginLLSD.put("viewer_digest", OSD.FromString(loginParams.ViewerDigest));
			loginLLSD.put("id0", OSD.FromString(loginParams.ID0));
			loginLLSD.put("last_exec_event", OSD.FromInteger(loginParams.LastExecEvent.ordinal()));

			OSDArray optionsOSD;
			// Create the options LLSD array
			if (loginParams.Options != null && loginParams.Options.length > 0) {
				optionsOSD = new OSDArray(loginParams.Options.length);
				for (int i = 0; i < loginParams.Options.length; i++) {
					optionsOSD.add(OSD.FromString(loginParams.Options[i]));
				}

				for (String[] callbackOpts : CallbackOptions.values()) {
					if (callbackOpts != null) {
						for (int i = 0; i < callbackOpts.length; i++) {
							if (!optionsOSD.contains(callbackOpts[i])) {
								optionsOSD.add(OSD.FromString(callbackOpts[i]));
							}
						}
					}
				}
			} else {
				optionsOSD = new OSDArray();
			}
			loginLLSD.put("options", optionsOSD);

			LoginReplyHandler handler = new LoginReplyHandler(loginParams);
			if (_Client.Settings.getBool(LibSettings.USE_LLSD_LOGIN)) {
				// Make the CAPS POST for login
				CapsClient loginRequest = new CapsClient(_Client, "LoginAgent");
				httpClient = loginRequest;
				loginRequest.executeHttpPost(loginUri, loginLLSD, OSDFormat.Xml, handler, loginParams.Timeout);
			} else {
				// Make the RPC call for login
				OSDArray request = new OSDArray(1);
				request.add(loginLLSD);
				RpcClient loginRequest = new RpcClient("LoginAgent");
				httpClient = loginRequest;
				loginRequest.call(loginUri, loginParams.MethodName, request, handler, loginParams.Timeout);
			}
		} catch (Exception ex) {
			UpdateLoginStatus(LoginStatus.Failed, ex.toString(), ex.getClass().toString(), null);
			throw ex;
		}
	}

	// #endregion

	// #region Private Methods

	private void UpdateLoginStatus(LoginStatus status, String message, String reason, LoginResponseData reply) {
		// Fire the login status callback
		OnLoginProgress.dispatch(new LoginProgressCallbackArgs(status, message, reason, reply));

		// If we reached a login resolution
		if (status == LoginStatus.Success || status == LoginStatus.Failed) {
			// trigger the event
			LoginEvents.set(status);
			// register our client for cleanup
			_Client.Network.addClosableClient(httpClient);
			httpClient = null;
		}
	}

	/**
	 * Handle response from LLSD login replies
	 *
	 * @param client
	 * @param result
	 * @param error
	 */
	private class LoginReplyHandler implements FutureCallback<OSD> {
		private final LoginParams loginParams;

		public LoginReplyHandler(LoginParams loginParams) {
			this.loginParams = loginParams;
		}

		@Override
		public void completed(OSD result) {
			if (result != null && result.getType().equals(OSDType.Map)) {
				UpdateLoginStatus(LoginStatus.ReadingResponse, "Parsing Reply data", "parsing", null);

				LoginResponseData reply = new LoginResponseData().ParseLoginReply((OSDMap) result);

				if (reply.Success) {
					// Remove the quotes around our first name.
					if (reply.FirstName.charAt(0) == '"') {
						reply.FirstName = reply.FirstName.substring(1);
					}
					if (reply.FirstName.charAt(reply.FirstName.length() - 1) == '"') {
						reply.FirstName = reply.FirstName.substring(0, reply.FirstName.length() - 1);
					}
				}

				try {
					HandleLoginResponse(reply, loginParams);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				// No LLSD response
				UpdateLoginStatus(LoginStatus.Failed, "Empty or unparseable login response", "bad response", null);
			}
		}

		@Override
		public void failed(Exception ex) {
			logger.error(GridClient.Log(String.format("Login exception %s", ex.getMessage()), _Client), ex);
			// Connection error
			UpdateLoginStatus(LoginStatus.Failed, ex.getMessage(), ex.getClass().toString(), null);
		}

		@Override
		public void cancelled() {
			// Connection canceled
			UpdateLoginStatus(LoginStatus.Failed, "connection canceled", "canceled", null);
		}
	}

	private void HandleLoginResponse(LoginResponseData reply, LoginParams loginParams) throws Exception {
		if (reply.Login.equals("indeterminate")) {
			// Login redirected

			// Make the next login URL jump
			UpdateLoginStatus(LoginStatus.Redirecting, reply.Message, "redirecting", null);
			loginParams.URI = reply.NextUrl;
			loginParams.MethodName = reply.NextMethod;
			loginParams.Options = reply.NextOptions;

			// Sleep for some amount of time while the servers work
			int seconds = reply.NextDuration;
			logger.info(GridClient.Log("Sleeping for " + seconds + " seconds during a login redirect", _Client));
			try {
				Thread.sleep(seconds * 1000);
			} catch (InterruptedException ex) {
			}

			RequestLogin(loginParams, null);
		} else if (reply.Success) {
			// Login succeeded
			_Client.Network.setCircuitCode(reply.CircuitCode);
			_Client.Network.setUDPBlacklist(reply.UDPBlacklist);
			_Client.Network.setAgentAppearanceServiceURL(reply.AgentAppearanceServiceURL);

			UpdateLoginStatus(LoginStatus.ConnectingToSim, "Connecting to simulator...", "connecting", reply);

			if (reply.SimIP != null && reply.SimPort != 0) {
				// Connect to the sim given in the login reply
				if (_Client.Network.connect(reply.SimIP, reply.SimPort, reply.Region, true,
						reply.SeedCapability) != null) {
					_Client.setCurrentGrid(reply.Grid);

					// Request the economy data right after login
					_Client.Network.sendPacket(new EconomyDataRequestPacket());

					// Update the login message with the MOTD returned from the server
					UpdateLoginStatus(LoginStatus.Success, reply.Message, reply.Reason, reply);
				} else {
					UpdateLoginStatus(LoginStatus.Failed, "Unable to establish a UDP connection to the simulator",
							"connection failed", null);
				}
			} else {
				UpdateLoginStatus(LoginStatus.Failed, "Login server did not return a valid simulator address", "no sim",
						null);
			}
		} else {
			// Login failed, make sure a usable error key is set
			if (reply.Reason == null || reply.Reason.isEmpty()) {
				reply.Reason = "unknown";
			}
			UpdateLoginStatus(LoginStatus.Failed, reply.Message, reply.Reason, reply);
		}
	}
	// #endregion
}