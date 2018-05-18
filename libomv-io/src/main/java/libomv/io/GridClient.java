/**
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import libomv.ProtocolManager;
import libomv.Statistics;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.StructuredData.OSDString;
import libomv.capabilities.CapsMessage;
import libomv.io.assets.AssetManager;
import libomv.io.inventory.InventoryManager;
import libomv.model.Grid;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;

/* Main class to expose the functionality of a particular grid to clients. All of the
 * classes needed for sending and receiving data are accessible throug this class.
 */
public class GridClient implements Grid {
	// #region gridlist definitions
	public class GridInfo implements Cloneable {
		public String gridnick; // gridnick
		public String gridname; // gridname
		public String platform; // platform
		public String loginuri; // login, loginuri
		public String loginpage; // welcome, loginpage
		public String helperuri; // economy, helperuri
		public String website; // about, website
		public String support; // help, support
		public String register; // register, account
		public String passworduri; // password
		public String searchurl; // search
		public String currencySym;
		public String realCurrencySym;
		public String directoryFee;
		public int version;

		public transient boolean saveSettings;
		public transient boolean savePassword;
		public transient String username; // first and last name separated by space, or resident name
		public transient String startLocation;

		private transient String password; // password

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			if (password.length() != 35 && !password.startsWith("$1$")) {
				password = Helpers.MD5Password(password);
			}
			this.password = password;
		}

		@Override
		public GridInfo clone() {
			try {
				return (GridInfo) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new Error("This should not occur since we implement Cloneable");
			}
		}

		/**
		 * Merge in the grid info for all null fields in our record
		 *
		 * @param info
		 *            The info to merge in
		 */
		public void merge(GridInfo info) {
			merge(info, false);
		}

		public void merge(GridInfo info, boolean force) {
			saveSettings |= info.saveSettings;
			savePassword = saveSettings && (info.savePassword || savePassword);
			if (username == null)
				username = info.username;
			if (password == null)
				password = info.password;
			if (startLocation == null)
				startLocation = info.startLocation;

			if (force || version < info.version) {
				if (gridnick == null || version >= 0)
					gridnick = info.gridnick;
				if (gridname == null || version >= 0)
					gridname = info.gridname;
				if (platform == null || version >= 0)
					platform = info.platform;
				if (loginuri == null || version >= 0)
					loginuri = info.loginuri;
				if (loginpage == null || version >= 0)
					loginpage = info.loginpage;
				if (helperuri == null || version >= 0)
					helperuri = info.helperuri;
				if (website == null || version >= 0)
					website = info.website;
				if (support == null || version >= 0)
					support = info.support;
				if (register == null || version >= 0)
					register = info.register;
				if (searchurl == null || version >= 0)
					searchurl = info.searchurl;
				if (currencySym == null || version >= 0)
					currencySym = info.currencySym;
				if (realCurrencySym == null || version >= 0)
					realCurrencySym = info.realCurrencySym;
				if (directoryFee == null || version >= 0)
					directoryFee = info.directoryFee;
				version = info.version;
			}
			if (!equals(info))
				version++;
		}

		public String dump() {
			return String.format(
					"Nick: %s, Name: %s, Platform: %s, Ver: %d\n"
							+ "loginuri: %s, loginpage: %s, website: %s, support: %s\n"
							+ "search: %s, currency: %s, real_currency: %s, directory_fee: %s",
					gridnick, gridname, platform, version, loginuri, loginpage, website, support, searchurl,
					currencySym, realCurrencySym, directoryFee);
		}

		@Override
		public int hashCode() {
			int hash = 0;
			String string = null;
			for (int i = 0; i < 13; i++) {
				switch (i) {
				case 0:
					string = gridnick;
					break;
				case 1:
					string = gridname;
					break;
				case 2:
					string = loginuri;
					break;
				case 3:
					string = loginpage;
					break;
				case 4:
					string = helperuri;
					break;
				case 5:
					string = website;
					break;
				case 6:
					string = support;
					break;
				case 7:
					string = register;
					break;
				case 8:
					string = platform;
					break;
				case 9:
					string = searchurl;
					break;
				case 10:
					string = currencySym;
					break;
				case 11:
					string = realCurrencySym;
					break;
				case 12:
					string = directoryFee;
					break;
				default:
					break;
				}
				if (string != null)
					hash ^= string.hashCode();
			}
			return hash;
		}

		@Override
		public boolean equals(Object info) {
			return (info != null && info instanceof GridInfo) ? equals((GridInfo) info) : false;
		}

		public boolean equals(GridInfo info) {
			String string1 = null, string2 = null;
			for (int i = 0; i < 13; i++) {
				switch (i) {
				case 0:
					string1 = gridnick;
					string2 = info.gridnick;
					break;
				case 1:
					string1 = gridname;
					string2 = info.gridname;
					break;
				case 2:
					string1 = loginuri;
					string2 = info.loginuri;
					break;
				case 3:
					string1 = loginpage;
					string2 = info.loginpage;
					break;
				case 4:
					string1 = helperuri;
					string2 = info.helperuri;
					break;
				case 5:
					string1 = website;
					string2 = info.website;
					break;
				case 6:
					string1 = support;
					string2 = info.support;
					break;
				case 7:
					string1 = register;
					string2 = info.register;
					break;
				case 8:
					string1 = platform;
					string2 = info.platform;
					break;
				case 9:
					string1 = searchurl;
					string2 = info.searchurl;
					break;
				case 10:
					string1 = currencySym;
					string2 = info.currencySym;
					break;
				case 11:
					string1 = realCurrencySym;
					string2 = info.realCurrencySym;
					break;
				case 12:
					string1 = directoryFee;
					string2 = info.directoryFee;
					break;
				default:
					break;
				}
				if (string1 == null || !string1.equals(string2)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			return gridname + " (" + gridnick + ")";
		}
	}

	public enum GridListUpdate {
		Add, Modify, Remove;
	}

	public class GridListUpdateCallbackArgs implements CallbackArgs {
		private GridListUpdate operation;
		private GridInfo info;

		public GridListUpdate getOperation() {
			return operation;
		}

		public GridInfo getGridInfo() {
			return info;
		}

		public GridListUpdateCallbackArgs(GridListUpdate operation, GridInfo info) {
			this.operation = operation;
			this.info = info;
		}
	}

	public CallbackHandler<GridListUpdateCallbackArgs> OnGridListUpdate = new CallbackHandler<GridListUpdateCallbackArgs>();

	private static final String listUri = "http://www.coolview.nl/grid/default_grids.xml";
	private static HashMap<String, GridInfo> gridlist = new HashMap<String, GridInfo>();
	private static int listversion = 0;

	private String defaultGrid = null;
	private transient String currentGrid = null;

	private static final String NUMGRIDS = "numgrids";
	private static final String GRIDINFO = "gridinfo";
	private static final String DEFAULT_GRID = "defaultGrid";
	private static final String DEFAULT_GRID_NAME1 = "osgrid";
	private static final String DEFAULT_GRID_NAME2 = "secondlife";
	private static final String GRID_INFO_PROTOCOL = "get_grid_info";
	private static final String DEFAULT_GRIDS_VERSION = "default_grids_version";
	private static final String DEFAULT_GRIDS_LIST = "/res/default_grids.xml";
	// #endregion

	// Networking Subsystem
	public NetworkManager Network;
	// Login Subsystem of Network handler
	public LoginManager Login;
	// Protocol Manager
	public ProtocolManager Protocol;
	// Caps Messages
	public CapsMessage Messages;
	// AgentThrottle
	public AgentThrottle Throttle;
	// Settings class including constant values and changeable parameters for
	// everything
	public LibSettings Settings;
	// 'Client's Avatar' Subsystem
	public AgentManager Self;
	// Other Avatars Subsystem
	public AvatarManager Avatars;
	// Friend Avatars Subsystem
	public FriendsManager Friends;
	// Group Subsystem
	public GroupManager Groups;
	// Grid (aka simulator group) Subsystem
	public GridManager Grid;
	/* Asset subsystem */
	public AssetManager Assets;
	/* Inventory subsystem */
	public InventoryManager Inventory;
	/* Handles sound-related networking */
	public SoundManager Sound;
	/* Appearance subsystem */
	public AppearanceManager Appearance;
	/* Parcel (subdivided simulator lots) Subsystem */
	public ParcelManager Parcels;
	/* Object Subsystem */
	public ObjectManager Objects;
	/* Directory searches including classifieds, people, land sales, etc */
	public DirectoryManager Directory;
	/* Handles land, wind, and cloud heightmaps */
	public TerrainManager Terrain;

	// Packet Statistics
	public Statistics Stats;

	//
	// Constructor.
	//
	public GridClient() throws Exception {
		this(new LibSettings());
	}

	public GridClient(LibSettings settings) throws Exception {
		initializeGridList();

		Settings = settings.initialize();
		Login = new LoginManager(this);
		Network = new NetworkManager(this);
		Messages = new CapsMessage();

		Self = new AgentManager(this);
		Friends = new FriendsManager(this);
		Groups = new GroupManager(this);
		Grid = new GridManager(this);

		/*
		 * This needs to come after the creation of the Network manager as it registers
		 * a packetCallback
		 */
		settings.startup(this);

		if (Settings.getBool(LibSettings.ENABLE_ASSET_MANAGER))
			Assets = new AssetManager(this);

		if (Settings.getBool(LibSettings.ENABLE_APPEARANCE_MANAGER))
			Appearance = new AppearanceManager(this);

		if (Settings.getBool(LibSettings.SEND_AGENT_THROTTLE))
			Throttle = new AgentThrottle(this);

		if (Settings.getBool(LibSettings.ENABLE_AVATAR_MANAGER))
			Avatars = new AvatarManager(this);

		if (Settings.getBool(LibSettings.ENABLE_INVENTORY_MANAGER))
			Inventory = new InventoryManager(this);

		if (Settings.getBool(LibSettings.ENABLE_SOUND_MANAGER))
			Sound = new SoundManager(this);

		if (Settings.getBool(LibSettings.ENABLE_PARCEL_MANAGER))
			Parcels = new ParcelManager(this);

		if (Settings.getBool(LibSettings.ENABLE_OBJECT_MANAGER))
			Objects = new ObjectManager(this);

		if (Settings.getBool(LibSettings.ENABLE_DIRECTORY_MANAGER))
			Directory = new DirectoryManager(this);

		if (Settings.getBool(LibSettings.ENABLE_TERRAIN_MANAGER))
			Terrain = new TerrainManager(this);

		Stats = new Statistics();
	}

	public long getCurrentRegionHandle() {
		return Network.getCurrentSim().getHandle();
	}

	public GridInfo[] getGridInfos() {
		synchronized (gridlist) {
			GridInfo[] grids = new GridInfo[gridlist.size()];
			return gridlist.values().toArray(grids);
		}
	}

	public Set<String> getGridNames() {
		synchronized (gridlist) {
			return gridlist.keySet();
		}
	}

	/**
	 * Set the current grid
	 *
	 * The function uses the info in the passed in grid to both set the currently
	 * active grid based on the gridnick, as well as merging the information in the
	 * GridInfo to the already stored Gridinfo.
	 *
	 * @param grid
	 *            The grid info to use to set the current grid. If the GridInfo is
	 *            null, the currentGrid is set to the current default grid
	 *
	 */
	public boolean setCurrentGrid(GridInfo grid) {
		if (grid != null) {
			synchronized (gridlist) {
				GridInfo temp = gridlist.get(grid.gridnick);
				if (temp != null) {
					temp.merge(grid, true);
					currentGrid = grid.gridnick;
					return true;
				}
			}
		} else {
			currentGrid = defaultGrid;
		}
		return false;
	}

	public void setCurrentGrid(String grid) {
		if (grid != null) {
			currentGrid = grid;
		} else {
			currentGrid = defaultGrid;
		}
	}

	public GridInfo getGrid(String grid) {
		if (grid == null)
			grid = currentGrid;
		synchronized (gridlist) {
			return gridlist.get(grid);
		}
	}

	public GridInfo getDefaultGrid() {
		if (defaultGrid == null || defaultGrid.isEmpty())
			setDefaultGrid((String) null);
		synchronized (gridlist) {
			return gridlist.get(defaultGrid);
		}
	}

	public void setDefaultGrid(GridInfo grid) {
		setDefaultGrid(grid != null ? grid.gridnick : null);
	}

	public void setDefaultGrid(String gridnick) {
		synchronized (gridlist) {
			if (gridnick != null && gridlist.containsKey(gridnick)) {
				defaultGrid = gridnick;
			} else if (gridlist.containsKey(DEFAULT_GRID_NAME1)) {
				defaultGrid = DEFAULT_GRID_NAME1;
			} else if (gridlist.containsKey(DEFAULT_GRID_NAME2)) {
				defaultGrid = DEFAULT_GRID_NAME2;
			} else if (!gridlist.isEmpty()) {
				defaultGrid = gridlist.keySet().iterator().next();
			} else {
				defaultGrid = Helpers.EmptyString;
			}
		}
	}

	public GridInfo addGrid(GridInfo info, boolean sendEvent) {
		synchronized (gridlist) {
			GridInfo old = gridlist.put(info.gridnick, info);
			if (sendEvent)
				OnGridListUpdate.dispatch(new GridListUpdateCallbackArgs(GridListUpdate.Add, info));
			return old;
		}
	}

	public GridInfo removeGrid(String grid, boolean sendEvent) {
		synchronized (gridlist) {
			GridInfo info = gridlist.remove(grid);
			if (info != null) {
				if (sendEvent)
					OnGridListUpdate.dispatch(new GridListUpdateCallbackArgs(GridListUpdate.Remove, info));
				if (grid.equals(defaultGrid)) {
					// sets first grid if map is not empty
					setDefaultGrid((String) null);
				}
			}
			return info;
		}
	}

	public void clearGrids(boolean sendEvent) {
		synchronized (gridlist) {
			gridlist.clear();
		}
		if (sendEvent)
			OnGridListUpdate.dispatch(new GridListUpdateCallbackArgs(GridListUpdate.Remove, null));
	}

	/**
	 * Retrieves the GridInfo settings from the grid user server, when the server
	 * supports the GridInfo protocol.
	 *
	 * @param loginuri
	 *            The HTTP address of the user server
	 * @return a filled in GridInfo if the call was successful, null otherwise
	 * @throws Exception
	 */
	public GridInfo queryGridInfo(GridInfo grid) throws Exception {
		GridInfo info = null;
		HttpClient client = getDefaultHttpClient();
		HttpGet getMethod = new HttpGet(new URI(grid.loginuri + GRID_INFO_PROTOCOL));
		try {
			HttpResponse response = client.execute(getMethod);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream stream = entity.getContent();
					String charset = null;
					if (entity.getContentType() != null) {
						HeaderElement values[] = entity.getContentType().getElements();
						if (values.length > 0) {
							NameValuePair param = values[0].getParameterByName("charset");
							if (param != null) {
								charset = param.getValue();
							}
						}
					}
					if (charset == null) {
						// charset = HTTP.DEFAULT_CONTENT_CHARSET;
						charset = StandardCharsets.ISO_8859_1.displayName();
					}
					XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
					parser.setInput(stream, charset);
					parser.nextTag();
					parser.require(XmlPullParser.START_TAG, null, GRIDINFO);
					if (!parser.isEmptyElementTag()) {
						parser.nextTag();
						info = parseRecord(parser);
					}
				}
			}
		} finally {
			getMethod.abort();
		}

		if (info != null) {
			info.merge(grid);
		}
		return info;
	}

	private GridInfo parseRecord(XmlPullParser parser) throws XmlPullParserException, IOException {
		GridInfo info = new GridInfo();
		info.version = -1;
		parser.require(XmlPullParser.START_TAG, null, null);
		do {
			if (parser.isEmptyElementTag()) {
				/* forward to end_tag */
				parser.nextTag();
			} else {
				String name = parser.getName();

				if (name.equals("gridnick")) {
					info.gridnick = parser.nextText().trim();
				} else if (name.equals("gridname")) {
					info.gridname = parser.nextText().trim();
				} else if (name.equals("platform")) {
					info.platform = parser.nextText().trim();
				} else if (name.equals("login") || name.equals("loginuri")) {
					info.loginuri = parser.nextText().trim();
				} else if (name.equals("welcome") || name.equals("loginpage")) {
					info.loginpage = parser.nextText().trim();
				} else if (name.equals("economy") || name.equals("helperuri")) {
					info.helperuri = parser.nextText().trim();
				} else if (name.equals("about") || name.equals("website")) {
					info.website = parser.nextText().trim();
				} else if (name.equals("help") || name.equals("support")) {
					info.support = parser.nextText().trim();
				} else if (name.equals("register") || name.equals("account")) {
					info.register = parser.nextText().trim();
				} else if (name.equals("password")) {
					info.passworduri = parser.nextText().trim();
				} else if (name.equals("searchurl")) {
					info.searchurl = parser.nextText().trim();
				} else if (name.equals("currency")) {
					info.currencySym = parser.nextText().trim();
				} else if (name.equals("real_currency")) {
					info.realCurrencySym = parser.nextText().trim();
				} else if (name.equals("directoryFee")) {
					info.directoryFee = parser.nextText().trim();
				} else if (name.equals("username")) {
					info.username = parser.nextText().trim();
					info.saveSettings = true;
				} else if (name.equals("startLocation")) {
					info.startLocation = parser.nextText().trim();
				} else if (name.equals("userpassword")) {
					info.password = parser.nextText().trim();
					info.savePassword = true;
				} else {
					/* forward to end_tag */
					parser.nextTag();
				}
			}
		} while (parser.nextTag() == XmlPullParser.START_TAG);
		return info;
	}

	private void initializeGridList() throws IOException, IllegalStateException, URISyntaxException,
			IllegalArgumentException, IllegalAccessException {
		boolean modified = setList(loadSettings(), false);
		modified |= setList(loadDefaults(), true);
		modified |= setList(downloadList(), true);
		if (modified)
			saveList(true);
	}

	private boolean setList(OSD list, boolean merge) throws IllegalArgumentException, IllegalAccessException {
		if (list == null || list.getType() != OSDType.Array)
			return false;

		if (!merge) {
			synchronized (gridlist) {
				gridlist.clear();
			}
			listversion = 0;
		}

		boolean modified = false;
		int version = 0;
		OSDArray array = (OSDArray) list;
		for (int i = 0; i < array.size(); i++) {
			OSDMap map = (OSDMap) array.get(i);
			if (map.containsKey(DEFAULT_GRIDS_VERSION)) {
				version = map.get(DEFAULT_GRIDS_VERSION).AsInteger();
				if (version <= listversion) {
					return false;
				}
			} else {
				GridInfo newinfo = new GridInfo();
				map.deserializeMembers(newinfo);
				synchronized (gridlist) {
					GridInfo oldinfo = gridlist.get(newinfo.gridname);
					if (!merge || oldinfo == null || oldinfo.version < newinfo.version) {
						gridlist.put(newinfo.gridnick, newinfo);
						modified = true;
					}
				}
			}
			if (modified)
				listversion = version;
		}
		return modified;
	}

	public void saveList() throws IllegalArgumentException, IllegalAccessException, IOException {
		saveList(true);
	}

	protected void saveList(boolean sendEvent) throws IllegalArgumentException, IllegalAccessException, IOException {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putInt(DEFAULT_GRIDS_VERSION, listversion);
		prefs.put(DEFAULT_GRID, defaultGrid);
		synchronized (gridlist) {
			prefs.putInt(NUMGRIDS, gridlist.size());
			int i = 0;
			for (GridInfo info : gridlist.values()) {
				// This doesn't save the transient fields
				OSDMap members = OSD.serializeMembers(info);
				if (info.saveSettings) {
					members.put("username", OSDString.FromString(info.username));
					members.put("startLocation", OSDString.FromString(info.startLocation));
					if (info.savePassword) {
						members.put("userpassword", OSDString.FromString(info.password));
					}
				}
				prefs.put(GRIDINFO + Integer.toString(++i), OSDParser.serializeToString(members, OSDFormat.Xml));
			}
		}
		if (sendEvent)
			OnGridListUpdate.dispatch(new GridListUpdateCallbackArgs(GridListUpdate.Add, null));
	}

	private OSD loadDefaults() throws IOException {
		OSD osd = null;
		InputStream stream = getClass().getResourceAsStream(DEFAULT_GRIDS_LIST);
		if (stream != null) {
			try {
				osd = OSDParser.deserialize(stream, OSDFormat.Xml, Helpers.UTF8_ENCODING);
			} catch (ParseException ex) {
			} finally {
				stream.close();
			}
		}
		return osd;
	}

	private OSD loadSettings() throws IOException {
		OSDArray osd = new OSDArray();
		try {
			Preferences prefs = Preferences.userNodeForPackage(this.getClass());
			defaultGrid = prefs.get(DEFAULT_GRID, Helpers.EmptyString);
			int length = prefs.getInt(NUMGRIDS, 0);
			for (int i = 1; i <= length; i++) {
				osd.add(OSDParser.deserialize(prefs.get(GRIDINFO + Integer.toString(i), Helpers.EmptyString)));
			}
		} catch (ParseException ex) {
		} catch (NumberFormatException ex) {
		}
		return osd;
	}

	private OSD downloadList() throws IOException, IllegalStateException, URISyntaxException {
		OSD osd = null;
		HttpClient client = getDefaultHttpClient();
		HttpGet getMethod = new HttpGet(new URI(listUri));
		try {
			HttpResponse response = client.execute(getMethod);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new HttpResponseException(response.getStatusLine().getStatusCode(),
						response.getStatusLine().getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream stream = entity.getContent();
				String charset = null;
				if (entity.getContentType() != null) {
					HeaderElement values[] = entity.getContentType().getElements();
					if (values.length > 0) {
						NameValuePair param = values[0].getParameterByName("charset");
						if (param != null) {
							charset = param.getValue();
						}
					}
				}
				if (charset == null) {
					charset = StandardCharsets.ISO_8859_1.displayName();
				}
				osd = OSDParser.deserialize(stream, OSDFormat.Xml, charset);
			}
		} catch (ParseException ex) {
		} finally {
			getMethod.abort();
		}
		return osd;
	}

	private CloseableHttpClient getDefaultHttpClient() {
		return HttpClientBuilder.create().build();
	}

	public String dumpGridlist() {
		String string = String.format("Version: %d, Default: %s\n", listversion, defaultGrid);
		synchronized (gridlist) {
			for (GridInfo info : gridlist.values()) {
				string += info.dump() + "\n";
			}

		}
		return string;
	}

	// <returns>Client Avatar's Full Name</returns>
	@Override
	public String toString() {
		return Self.getName();
	}

	// A simple sleep function that will allow pending threads to run
	public void Tick(long millis) throws Exception {
		Thread.sleep(millis);
	}

	// A simple sleep function that will allow pending threads to run
	public void Tick() throws Exception {
		Thread.sleep(0);
	}

	public static String Log(String message, GridClient client) {
		if (client != null && client.Settings.LOG_NAMES) {
			return String.format("<%s>: {%s}", client.Self.getName(), message);
		}
		return message;
	}

}
