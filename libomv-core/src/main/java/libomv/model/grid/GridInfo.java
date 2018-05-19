package libomv.model.grid;

import libomv.utils.Helpers;

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