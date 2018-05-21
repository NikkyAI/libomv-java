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
import java.util.Map;
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
import libomv.model.grid.GridInfo;
import libomv.model.grid.GridListUpdate;
import libomv.model.grid.GridListUpdateCallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;

/* Main class to expose the functionality of a particular grid to clients. All of the
 * classes needed for sending and receiving data are accessible throug this class.
 */
public class GridClient {

	private static final String listUri = "http://www.coolview.nl/grid/default_grids.xml";
	private static Map<String, GridInfo> gridlist = new HashMap<>();
	private static int listversion = 0;

	private static final String NUMGRIDS = "numgrids";
	private static final String GRIDINFO = "gridinfo";
	private static final String DEFAULT_GRID = "defaultGrid";
	private static final String DEFAULT_GRID_NAME1 = "osgrid";
	private static final String DEFAULT_GRID_NAME2 = "secondlife";
	private static final String GRID_INFO_PROTOCOL = "get_grid_info";
	private static final String DEFAULT_GRIDS_VERSION = "default_grids_version";
	private static final String DEFAULT_GRIDS_LIST = "/res/default_grids.xml";

	public CallbackHandler<GridListUpdateCallbackArgs> onGridListUpdate = new CallbackHandler<>();

	private String defaultGrid = null;
	private transient String currentGrid = null;

	// Networking Subsystem
	public NetworkManager network;
	// Login Subsystem of Network handler
	public LoginManager login;
	// Protocol Manager
	public ProtocolManager protocol;
	// Caps Messages
	public CapsMessage messages;
	// AgentThrottle
	public AgentThrottle throttle;
	// Settings class including constant values and changeable parameters for
	// everything
	public LibSettings settings;
	// 'Client's Avatar' Subsystem
	public AgentManager agent;
	// Other Avatars Subsystem
	public AvatarManager avatars;
	// Friend Avatars Subsystem
	public FriendsManager friends;
	// Group Subsystem
	public GroupManager groups;
	// Grid (aka simulator group) Subsystem
	public GridManager grid;
	/* Asset subsystem */
	public AssetManager assets;
	/* Inventory subsystem */
	public InventoryManager inventory;
	/* Handles sound-related networking */
	public SoundManager sound;
	/* Appearance subsystem */
	public AppearanceManager appearance;
	/* Parcel (subdivided simulator lots) Subsystem */
	public ParcelManager parcels;
	/* Object Subsystem */
	public ObjectManager objects;
	/* Directory searches including classifieds, people, land sales, etc */
	public DirectoryManager directory;
	/* Handles land, wind, and cloud heightmaps */
	public TerrainManager terrain;

	// Packet Statistics
	public Statistics stats;

	//
	// Constructor.
	//
	public GridClient() throws Exception {
		this(new LibSettings());
	}

	public GridClient(LibSettings settings) throws Exception {
		initializeGridList();

		this.settings = settings.initialize();
		this.login = new LoginManager(this);
		this.network = new NetworkManager(this);
		this.messages = new CapsMessage();

		this.agent = new AgentManager(this);
		this.friends = new FriendsManager(this);
		this.groups = new GroupManager(this);
		this.grid = new GridManager(this);

		/*
		 * This needs to come after the creation of the Network manager as it registers
		 * a packetCallback
		 */
		settings.startup(this);

		if (this.settings.getBool(LibSettings.ENABLE_ASSET_MANAGER))
			this.assets = new AssetManager(this);

		if (this.settings.getBool(LibSettings.ENABLE_APPEARANCE_MANAGER))
			this.appearance = new AppearanceManager(this);

		if (this.settings.getBool(LibSettings.SEND_AGENT_THROTTLE))
			this.throttle = new AgentThrottle(this);

		if (this.settings.getBool(LibSettings.ENABLE_AVATAR_MANAGER))
			this.avatars = new AvatarManager(this);

		if (this.settings.getBool(LibSettings.ENABLE_INVENTORY_MANAGER))
			this.inventory = new InventoryManager(this);

		if (this.settings.getBool(LibSettings.ENABLE_SOUND_MANAGER))
			this.sound = new SoundManager(this);

		if (this.settings.getBool(LibSettings.ENABLE_PARCEL_MANAGER))
			this.parcels = new ParcelManager(this);

		if (this.settings.getBool(LibSettings.ENABLE_OBJECT_MANAGER))
			this.objects = new ObjectManager(this);

		if (this.settings.getBool(LibSettings.ENABLE_DIRECTORY_MANAGER))
			this.directory = new DirectoryManager(this);

		if (this.settings.getBool(LibSettings.ENABLE_TERRAIN_MANAGER))
			this.terrain = new TerrainManager(this);

		this.stats = new Statistics();
	}

	public long getCurrentRegionHandle() {
		return network.getCurrentSim().getHandle();
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
				onGridListUpdate.dispatch(new GridListUpdateCallbackArgs(GridListUpdate.Add, info));
			return old;
		}
	}

	public GridInfo removeGrid(String grid, boolean sendEvent) {
		synchronized (gridlist) {
			GridInfo info = gridlist.remove(grid);
			if (info != null) {
				if (sendEvent)
					onGridListUpdate.dispatch(new GridListUpdateCallbackArgs(GridListUpdate.Remove, info));
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
			onGridListUpdate.dispatch(new GridListUpdateCallbackArgs(GridListUpdate.Remove, null));
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
				version = map.get(DEFAULT_GRIDS_VERSION).asInteger();
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
					members.put("username", OSDString.fromString(info.username));
					members.put("startLocation", OSDString.fromString(info.startLocation));
					if (info.savePassword) {
						members.put("userpassword", OSDString.fromString(info.password));
					}
				}
				prefs.put(GRIDINFO + Integer.toString(++i), OSDParser.serializeToString(members, OSDFormat.Xml));
			}
		}
		if (sendEvent)
			onGridListUpdate.dispatch(new GridListUpdateCallbackArgs(GridListUpdate.Add, null));
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
		return agent.getName();
	}

	// A simple sleep function that will allow pending threads to run
	public void tick(long millis) throws Exception {
		Thread.sleep(millis);
	}

	// A simple sleep function that will allow pending threads to run
	public void tick() throws Exception {
		Thread.sleep(0);
	}

	public static String Log(String message, GridClient client) {
		if (client != null && client.settings.LOG_NAMES) {
			return String.format("<%s>: {%s}", client.agent.getName(), message);
		}
		return message;
	}

}
