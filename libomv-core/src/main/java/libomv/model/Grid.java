package libomv.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;
import libomv.utils.Helpers;

public interface Grid {

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

		// TODO:FIXME Changing several fields to public, they need getters instead!
		public transient String password; // password, private

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

	/* Map layer request type */
	public enum GridLayerType {
		/* Objects and terrain are shown */
		Objects,
		/* Only the terrain is shown, no objects */
		Terrain,
		/* Overlay showing land for sale and for auction */
		LandForSale
	}

	/* Type of grid item, such as telehub, event, populator location, etc. */
	public enum GridItemType {
		Nothing, Telehub, PgEvent, MatureEvent, Popular, Unused1, AgentLocations, LandForSale, Classified, AdultEvent, AdultLandForSale;

		public static GridItemType convert(int value) {
			GridItemType values[] = GridItemType.values();

			for (int i = 0; i < values.length; i++)
				if (values[i].ordinal() == value)
					return values[i];
			return null;
		}
	}

	public final class GridLayer {
		public int Bottom;
		public int Left;
		public int Top;
		public int Right;
		public UUID ImageID;

		public boolean ContainsRegion(int x, int y) {
			return (x >= Left && x <= Right && y >= Bottom && y <= Top);
		}
	}

	/* Class for regions on the world map */
	public class GridRegion {
		// Sim X position on World Map
		public int X;
		// Sim Y position on World Map
		public int Y;
		// Sim Name (NOTE: In lowercase!)
		public String Name;
		//
		public byte Access;
		// Various flags for the region (presumably things like PG/Mature)
		public int RegionFlags;
		// Sim's defined Water Height
		public byte WaterHeight;
		//
		public byte Agents;
		// UUID of the World Map image
		public UUID MapImageID;
		// Used for teleporting
		public long RegionHandle;

		// Constructor
		public GridRegion() {
		}

		public GridRegion(String name) {
			Name = name;
		}

		@Override
		public String toString() {
			return String.format("%s (%d/%d), Handle: %d, MapImage: %s, Access: %d, Flags: 0x%8x", Name, X, Y,
					RegionHandle, MapImageID.toString(), Access, RegionFlags);
		}

		@Override
		public int hashCode() {
			return ((Integer) X).hashCode() ^ ((Integer) Y).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof GridRegion) {
				return equals((GridRegion) obj);
			}
			return false;
		}

		private boolean equals(GridRegion region) {
			return (this.X == region.X && this.Y == region.Y);
		}
	}

	/** Map Items */
	public class MapItem {
		private UUID ID;
		private String Name;
		private Vector3 GlobalPos;

		/* Represents an agent or group of agents location */
		public int AvatarCount;

		/* For adult and normal land for sale */
		public int Size;
		public int Price;

		public boolean isInfoHub;

		/* For evnts */
		public Date DateTime;

		public final UUID getUUID() {
			return ID;
		}

		public final String getName() {
			return Name;
		}

		/* Get the Local X position of the item */
		public final float getLocalX() {
			return GlobalPos.X % 256;
		}

		/* Get the Local Y position of the item */
		public final float getLocalY() {
			return GlobalPos.Y % 256;
		}

		public final Vector3 getGlobalPosition() {
			return GlobalPos;
		}

		public final void setEvelation(float z) {
			GlobalPos.Z = z;
		}

		/* Get the Handle of the region */
		public final long getRegionHandle() {
			return Helpers.IntsToLong((int) (GlobalPos.X - (GlobalPos.X % 256)),
					(int) (GlobalPos.Y - (GlobalPos.Y % 256)));
		}

		public MapItem(float x, float y, UUID id, String name) {
			GlobalPos = new Vector3(x, y, 40);
			ID = id;
			Name = name;
		}
	}

	// /#region EventArgs classes

	public class CoarseLocationUpdateCallbackArgs implements CallbackArgs {
		private final Simulator m_Simulator;
		private final ArrayList<UUID> m_NewEntries;
		private final ArrayList<UUID> m_RemovedEntries;

		public final Simulator getSimulator() {
			return m_Simulator;
		}

		public final ArrayList<UUID> getNewEntries() {
			return m_NewEntries;
		}

		public final ArrayList<UUID> getRemovedEntries() {
			return m_RemovedEntries;
		}

		public CoarseLocationUpdateCallbackArgs(Simulator simulator, ArrayList<UUID> newEntries,
				ArrayList<UUID> removedEntries) {
			this.m_Simulator = simulator;
			this.m_NewEntries = newEntries;
			this.m_RemovedEntries = removedEntries;
		}
	}

	public class GridRegionCallbackArgs implements CallbackArgs {
		private final GridRegion m_Region;

		public final GridRegion getRegion() {
			return m_Region;
		}

		public GridRegionCallbackArgs(GridRegion region) {
			this.m_Region = region;
		}
	}

	public class GridLayerCallbackArgs implements CallbackArgs {
		private final GridLayer m_Layer;

		public final GridLayer getLayer() {
			return m_Layer;
		}

		public GridLayerCallbackArgs(GridLayer layer) {
			this.m_Layer = layer;
		}
	}

	public class GridItemsCallbackArgs implements CallbackArgs {
		private final GridItemType m_Type;
		private final List<MapItem> m_Items;

		public final GridItemType getType() {
			return m_Type;
		}

		public final List<MapItem> getItems() {
			return m_Items;
		}

		public GridItemsCallbackArgs(GridItemType type, List<MapItem> items) {
			this.m_Type = type;
			this.m_Items = items;
		}
	}

	public class RegionHandleReplyCallbackArgs implements CallbackArgs {
		private final UUID m_RegionID;
		// TODO was: private readonly ulong m_RegionHandle;
		private final long m_RegionHandle;

		public final UUID getRegionID() {
			return m_RegionID;
		}

		// TODO was: public ulong getRegionHandle()
		public final long getRegionHandle() {
			return m_RegionHandle;
		}

		// TODO was: public RegionHandleReplyEventArgs(UUID regionID, ulong
		// regionHandle)
		public RegionHandleReplyCallbackArgs(UUID regionID, long regionHandle) {
			this.m_RegionID = regionID;
			this.m_RegionHandle = regionHandle;
		}
	}
}
