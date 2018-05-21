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
import java.util.Map;

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
import libomv.model.grid.GridInfo;
import libomv.model.login.LastExecStatus;
import libomv.model.login.LoginProgressCallbackArgs;
import libomv.model.login.LoginResponseData;
import libomv.model.login.LoginStatus;
import libomv.packets.EconomyDataRequestPacket;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.TimeoutEvent;
import libomv.utils.TimeoutEventQueue;

public class LoginManager {
	private static final Logger logger = Logger.getLogger(LoginManager.class);

	/** Login Request Parameters */
	public class LoginParams {
		/** The URL of the Login Server */
		public String uri;
		/**
		 * The number of milliseconds to wait before a login is considered failed due to
		 * timeout
		 */
		public int timeout;
		/**
		 * The request method login_to_simulator is currently the only supported method
		 */
		public String methodName;
		/** The Agents First name */
		public String firstName;
		/** The Agents First name */
		public String lastName;
		/**
		 * A md5 hashed password, plaintext password will be automatically hashed
		 */
		public String password;
		/**
		 * The agents starting location once logged in Either "last", "home", or a
		 * string encoded URI containing the simulator name and x/y/z coordinates e.g:
		 * uri:hooper&amp;128&amp;152&amp;17
		 */
		public String start;
		/**
		 * A string containing the client software channel information <example>Second
		 * Life Release</example>
		 */
		public String channel;
		/**
		 * The client software version information The official viewer uses: Second Life
		 * Release n.n.n.n where n is replaced with the current version of the viewer
		 */
		public String version;
		/** A string containing the platform information the agent is running on */
		public String platform;
		/** A string containing version number for OS the agent is running on */
		public String platformVersion;
		/** A string hash of the network cards Mac Address */
		public String mac;
		/** Unknown or deprecated */
		public String viewerDigest;
		/**
		 * A string hash of the first disk drives ID used to identify this clients
		 * uniqueness
		 */
		public String id0;
		/**
		 * A string containing the viewers Software, this is not directly sent to the
		 * login server but is used by the library to generate the Version information
		 */
		public String userAgent;
		/**
		 * A string representing the software creator. This is not directly sent to the
		 * login server but is used by the library to generate the Version information
		 */
		public String author;
		/**
		 * If true, this agent agrees to the Terms of Service of the grid its connecting
		 * to
		 */
		public boolean agreeToTos;
		/** Unknown */
		public boolean readCritical;
		/**
		 * Status of the last application run sent to the grid login server for
		 * statistical purposes
		 */
		public LastExecStatus lastExecEvent = LastExecStatus.Normal;
		/**
		 * An array of string sent to the login server to enable various options
		 */
		public String[] options;

		/**
		 * Default constructor, initializes sane default values
		 */
		public LoginParams() {
			this.options = new String[] { "inventory-root", "inventory-skeleton", "inventory-lib-root",
					"inventory-lib-owner", "inventory-skel-lib", "initial-outfit", "gestures", "event_categories",
					"event_notifications", "classified_categories", "adult_compliant", "buddy-list", "ui-config",
					"map-server-url", "tutorial_settings", "login-flags", "global-textures" };
			this.methodName = "login_to_simulator";
			this.start = "last";
			this.platform = Helpers.getPlatform();
			this.platformVersion = Helpers.getPlatformVersion();
			this.mac = Helpers.getMAC();
			this.viewerDigest = "";
			this.id0 = Helpers.getMAC();
			this.agreeToTos = true;
			this.readCritical = true;
			this.channel = LibSettings.LIBRARY_NAME;
			this.version = LibSettings.LIBRARY_VERSION;
			this.lastExecEvent = LastExecStatus.Normal;
		}

		public LoginParams(GridClient client) {
			this();
			this.timeout = client.settings.LOGIN_TIMEOUT;
			GridInfo gridInfo = client.getDefaultGrid();
			this.uri = gridInfo.loginuri;

			String names[] = gridInfo.username.split("[\\. ]");

			this.firstName = names[0];
			if (names.length >= 2) {
				this.lastName = names[1];
			} else {
				this.lastName = "Resident";
			}

			this.password = gridInfo.getPassword();
			if (gridInfo.startLocation != null)
				this.start = gridInfo.startLocation;
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
			this.uri = client.getDefaultGrid().loginuri;
			this.timeout = client.settings.LOGIN_TIMEOUT;
			this.firstName = firstName;
			this.lastName = lastName;
			this.password = password;
			if (startLocation != null)
				this.start = startLocation;
		}
	}

	public CallbackHandler<LoginProgressCallbackArgs> onLoginProgress = new CallbackHandler<>();

	private Map<Callback<LoginProgressCallbackArgs>, String[]> callbackOptions = new HashMap<>();

	public final void registerLoginProgressCallback(Callback<LoginProgressCallbackArgs> callback, String[] options,
			boolean autoremove) {
		if (options != null)
			callbackOptions.put(callback, options);
		onLoginProgress.add(callback, autoremove);
	}

	public final void unregisterLoginProgressCallback(Callback<LoginProgressCallbackArgs> callback) {
		callbackOptions.remove(callback);
		onLoginProgress.remove(callback);
	}

	// #region Private Members
	private GridClient client;
	private TimeoutEventQueue<LoginStatus> loginEvents = new TimeoutEventQueue<>();
	private AsyncHTTPClient<OSD> httpClient;

	public LoginManager(GridClient client) {
		this.client = client;
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
	 * @param startLocation
	 *            Location where to start such as "home", "last", or an explicit
	 *            start location
	 * @return A populated {@link LoginParams} struct containing sane defaults
	 */
	public final LoginParams defaultLoginParams(String firstName, String lastName, String password,
			String startLocation) {
		return new LoginParams(client, firstName, lastName, password, startLocation);
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
	public final LoginParams defaultLoginParams(String firstName, String lastName, String password, String channel,
			String version) {
		LoginParams params = new LoginParams(client, firstName, lastName, password, null);
		params.channel = channel;
		params.version = version;
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
	public final boolean login(String firstName, String lastName, String password) throws Exception {
		return login(new LoginParams(client, firstName, lastName, password, null));
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
	public final boolean login(String firstName, String lastName, String password, String startLocation)
			throws Exception {
		return login(new LoginParams(client, firstName, lastName, password, startLocation));
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
	public final boolean login(String firstName, String lastName, String password, String channel, String version)
			throws Exception {
		return login(defaultLoginParams(firstName, lastName, password, channel, version));
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
	public final boolean login(String firstName, String lastName, String password, String start, String channel,
			String version) throws Exception {
		LoginParams loginParams = defaultLoginParams(firstName, lastName, password, channel, version);
		loginParams.start = start;

		return login(loginParams, null);
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
	public final boolean login(LoginParams loginParams) throws Exception {
		return login(loginParams, null);
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
	public final boolean login(LoginParams loginParams, Callback<LoginProgressCallbackArgs> callback) throws Exception {
		// FIXME: Now that we're using CAPS we could cancel the current login and start
		// a new one
		if (loginEvents.size() != 0) {
			throw new Exception("Login already in progress");
		}

		TimeoutEvent<LoginStatus> loginEvent = loginEvents.create();
		requestLogin(loginParams, callback);
		LoginStatus status = loginEvent.waitOne(loginParams.timeout);
		loginEvents.cancel(loginEvent);
		if (status == null) {
			updateLoginStatus(LoginStatus.Failed, "Logon timed out", "timeout", null);
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
	public static String startLocation(String sim, int x, int y, int z) {
		return String.format("uri:%s&%d&%d&%d", sim, x, y, z);
	}

	/**
	 * Abort any ongoing login. If no login is currently ongoing, this function does
	 * nothing
	 */
	public void abortLogin() {
		// FIXME: Now that we're using CAPS we could cancel the current login and start
		// a new one
		if (loginEvents.size() != 0) {
			updateLoginStatus(LoginStatus.Failed, "Abort Requested", " aborted", null);
		}
	}

	public void requestLogin(final LoginParams loginParams, Callback<LoginProgressCallbackArgs> callback)
			throws Exception {
		// #region Sanity Check loginParams
		if (loginParams.options == null)
			loginParams.options = new String[] {};

		if (loginParams.password == null)
			loginParams.password = Helpers.EmptyString;

		// Convert the password to MD5 if it isn't already
		if (loginParams.password.length() != 35 && !loginParams.password.startsWith("$1$"))
			loginParams.password = Helpers.MD5Password(loginParams.password);

		if (loginParams.viewerDigest == null)
			loginParams.viewerDigest = Helpers.EmptyString;

		if (loginParams.userAgent == null)
			loginParams.userAgent = Helpers.EmptyString;

		if (loginParams.version == null)
			loginParams.version = Helpers.EmptyString;

		if (loginParams.platform == null)
			loginParams.platform = Helpers.getPlatform();

		if (loginParams.platformVersion == null)
			loginParams.platformVersion = Helpers.getPlatformVersion();

		if (loginParams.mac == null)
			loginParams.mac = Helpers.getMAC();

		if (loginParams.channel == null || loginParams.channel.isEmpty()) {
			logger.warn("Viewer channel not set. This is a TOS violation on some grids.");
			loginParams.channel = LibSettings.LIBRARY_NAME;
		}

		if (loginParams.author == null)
			loginParams.author = Helpers.EmptyString;
		// #endregion

		if (callback != null)
			registerLoginProgressCallback(callback, loginParams.options, false);

		URI loginUri;
		try {
			loginUri = new URI(loginParams.uri);
		} catch (Exception ex) {
			logger.error(GridClient
					.Log(String.format("Failed to parse login URI %s, %s", loginParams.uri, ex.getMessage()), client));
			throw ex;
		}

		updateLoginStatus(LoginStatus.ConnectingToLogin,
				"Logging in as " + loginParams.firstName + " " + loginParams.lastName + " ...", null, null);

		try {
			// Create the CAPS login structure
			OSDMap loginLLSD = new OSDMap();
			loginLLSD.put("first", OSD.fromString(loginParams.firstName));
			loginLLSD.put("last", OSD.fromString(loginParams.lastName));
			loginLLSD.put("passwd", OSD.fromString(loginParams.password));
			loginLLSD.put("start", OSD.fromString(loginParams.start));
			loginLLSD.put("channel", OSD.fromString(loginParams.channel));
			loginLLSD.put("version", OSD.fromString(loginParams.version));
			loginLLSD.put("platform", OSD.fromString(loginParams.platform));
			loginLLSD.put("platform_version", OSD.fromString(loginParams.platformVersion));
			loginLLSD.put("mac", OSD.fromString(loginParams.mac));
			loginLLSD.put("agree_to_tos", OSD.fromBoolean(loginParams.agreeToTos));
			loginLLSD.put("read_critical", OSD.fromBoolean(loginParams.readCritical));
			loginLLSD.put("viewer_digest", OSD.fromString(loginParams.viewerDigest));
			loginLLSD.put("id0", OSD.fromString(loginParams.id0));
			loginLLSD.put("last_exec_event", OSD.fromInteger(loginParams.lastExecEvent.ordinal()));

			OSDArray optionsOSD;
			// Create the options LLSD array
			if (loginParams.options != null && loginParams.options.length > 0) {
				optionsOSD = new OSDArray(loginParams.options.length);
				for (int i = 0; i < loginParams.options.length; i++) {
					optionsOSD.add(OSD.fromString(loginParams.options[i]));
				}

				for (String[] callbackOpts : callbackOptions.values()) {
					if (callbackOpts != null) {
						for (int i = 0; i < callbackOpts.length; i++) {
							if (!optionsOSD.contains(callbackOpts[i])) {
								optionsOSD.add(OSD.fromString(callbackOpts[i]));
							}
						}
					}
				}
			} else {
				optionsOSD = new OSDArray();
			}
			loginLLSD.put("options", optionsOSD);

			LoginReplyHandler handler = new LoginReplyHandler(loginParams);
			if (client.settings.getBool(LibSettings.USE_LLSD_LOGIN)) {
				// Make the CAPS POST for login
				CapsClient loginRequest = new CapsClient(client, "LoginAgent");
				httpClient = loginRequest;
				loginRequest.executeHttpPost(loginUri, loginLLSD, OSDFormat.Xml, handler, loginParams.timeout);
			} else {
				// Make the RPC call for login
				OSDArray request = new OSDArray(1);
				request.add(loginLLSD);
				RpcClient loginRequest = new RpcClient("LoginAgent");
				httpClient = loginRequest;
				loginRequest.call(loginUri, loginParams.methodName, request, handler, loginParams.timeout);
			}
		} catch (Exception ex) {
			updateLoginStatus(LoginStatus.Failed, ex.toString(), ex.getClass().toString(), null);
			throw ex;
		}
	}

	// #endregion

	// #region Private Methods

	private void updateLoginStatus(LoginStatus status, String message, String reason, LoginResponseData reply) {
		// Fire the login status callback
		onLoginProgress.dispatch(new LoginProgressCallbackArgs(status, message, reason, reply));

		// If we reached a login resolution
		if (status == LoginStatus.Success || status == LoginStatus.Failed) {
			// trigger the event
			loginEvents.set(status);
			// register our client for cleanup
			client.network.addClosableClient(httpClient);
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
				updateLoginStatus(LoginStatus.ReadingResponse, "Parsing Reply data", "parsing", null);

				LoginResponseData reply = new LoginResponseData().parseLoginReply((OSDMap) result);

				if (reply.success) {
					// Remove the quotes around our first name.
					if (reply.firstName.charAt(0) == '"') {
						reply.firstName = reply.firstName.substring(1);
					}
					if (reply.firstName.charAt(reply.firstName.length() - 1) == '"') {
						reply.firstName = reply.firstName.substring(0, reply.firstName.length() - 1);
					}
				}

				try {
					handleLoginResponse(reply, loginParams);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				// No LLSD response
				updateLoginStatus(LoginStatus.Failed, "Empty or unparseable login response", "bad response", null);
			}
		}

		@Override
		public void failed(Exception ex) {
			logger.error(GridClient.Log(String.format("Login exception %s", ex.getMessage()), client), ex);
			// Connection error
			updateLoginStatus(LoginStatus.Failed, ex.getMessage(), ex.getClass().toString(), null);
		}

		@Override
		public void cancelled() {
			// Connection canceled
			updateLoginStatus(LoginStatus.Failed, "connection canceled", "canceled", null);
		}
	}

	private void handleLoginResponse(LoginResponseData reply, LoginParams loginParams) throws Exception {
		if (reply.login.equals("indeterminate")) {
			// Login redirected

			// Make the next login URL jump
			updateLoginStatus(LoginStatus.Redirecting, reply.message, "redirecting", null);
			loginParams.uri = reply.nextUrl;
			loginParams.methodName = reply.nextMethod;
			loginParams.options = reply.nextOptions;

			// Sleep for some amount of time while the servers work
			int seconds = reply.nextDuration;
			logger.info(GridClient.Log("Sleeping for " + seconds + " seconds during a login redirect", client));
			try {
				Thread.sleep(seconds * 1000);
			} catch (InterruptedException ex) {
			}

			requestLogin(loginParams, null);
		} else if (reply.success) {
			// Login succeeded
			client.network.setCircuitCode(reply.circuitCode);
			client.network.setUDPBlacklist(reply.udpBlacklist);
			client.network.setAgentAppearanceServiceURL(reply.agentAppearanceServiceURL);

			updateLoginStatus(LoginStatus.ConnectingToSim, "Connecting to simulator...", "connecting", reply);

			if (reply.simIP != null && reply.simPort != 0) {
				// Connect to the sim given in the login reply
				if (client.network.connect(reply.simIP, reply.simPort, reply.region, true,
						reply.seedCapability) != null) {
					client.setCurrentGrid(reply.grid);

					// Request the economy data right after login
					client.network.sendPacket(new EconomyDataRequestPacket());

					// Update the login message with the MOTD returned from the server
					updateLoginStatus(LoginStatus.Success, reply.message, reply.reason, reply);
				} else {
					updateLoginStatus(LoginStatus.Failed, "Unable to establish a UDP connection to the simulator",
							"connection failed", null);
				}
			} else {
				updateLoginStatus(LoginStatus.Failed, "Login server did not return a valid simulator address", "no sim",
						null);
			}
		} else {
			// Login failed, make sure a usable error key is set
			if (reply.reason == null || reply.reason.isEmpty()) {
				reply.reason = "unknown";
			}
			updateLoginStatus(LoginStatus.Failed, reply.message, reply.reason, reply);
		}
	}
	// #endregion
}