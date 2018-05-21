package libomv.io.inventory;

import java.net.URI;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.io.GridClient;
import libomv.types.UUID;
import libomv.utils.Callback;

public class InventoryAISClient {
	private static final Logger logger = Logger.getLogger(InventoryAISClient.class);

	private static final String INVENTORY_CAP_NAME = "InventoryAPIv3";
	private static final String LIBRARY_CAP_NAME = "LibraryAPIv3";

	enum CommandType {
		COPYINVENTORY, SLAMFOLDER, REMOVECATEGORY, REMOVEITEM, PURGEDESCENDENTS, UPDATECATEGORY, UPDATEITEM, COPYLIBRARYCATEGORY
	}

	private GridClient client;

	public InventoryAISClient(GridClient client) {
		this.client = client;
	}

	public boolean isAvailable() {
		return client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME) != null;
	}

	public void createInventory(UUID parentUuid, OSD newInventory, Callback<OSD> callback) {
		URI cap = client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", client));
			return;
		}

		UUID tid = new UUID();
		String url = String.format("%s/category/%s?tid=%s", cap.toString(), parentUuid.toString(), tid.toString());
		logger.debug(GridClient.Log("url: " + url, client));
		// Enqueue
	}

	public void slamFolder(UUID folderUuid, OSD newInventory, Callback<OSD> callback) {
		URI cap = client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", client));
			return;
		}

		UUID tid = new UUID();
		String url = String.format("%s/category/%s/links?tid=%s", folderUuid.toString(), tid.toString());
		logger.debug(GridClient.Log("url: " + url, client));
		// Enqueue
	}

	public void removeCategory(UUID categoryUuid, Callback<OSD> callback) {
		URI cap = client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", client));
			return;
		}

		String url = String.format("%s/category/%s", categoryUuid.toString());
		logger.debug(GridClient.Log("url: " + url, client));
		// Enqueue
	}

	public void removeItem(UUID itemUuid, Callback<OSD> callback) {
		URI cap = client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", client));
			return;
		}

		String url = String.format("%s/item/%s", itemUuid.toString());
		logger.debug(GridClient.Log("url: " + url, client));
		// Enqueue
	}

	public void copyLibraryCategory(UUID sourceUuid, UUID destUuid, boolean copySubfolders, Callback<OSD> callback) {
		URI cap = client.network.getCurrentSim().getCapabilityURI(LIBRARY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", client));
			return;
		}

		UUID tid = new UUID();
		String url = String.format("%s/category/%s?tid=%s", sourceUuid.toString(), tid.toString());
		if (copySubfolders)
			url += ",depth=0";
		logger.debug(GridClient.Log("url: " + url, client));
		// Enqueue
	}

	public void purgeDescendents(UUID categoryUuid, Callback<OSD> callback) {
		URI cap = client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", client));
			return;
		}

		String url = String.format("%s/category/%s/children", categoryUuid.toString());
		logger.debug(GridClient.Log("url: " + url, client));
		// Enqueue
	}

	public void updateCategory(UUID categoryUuid, OSD updates, Callback<OSD> callback) {
		URI cap = client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", client));
			return;
		}

		String url = String.format("%s/category/%s", categoryUuid.toString());
		logger.debug(GridClient.Log("url: " + url, client));
		// Enqueue
	}

	public void updateItem(UUID itemUuid, OSD updates, Callback<OSD> callback) {
		URI cap = client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", client));
			return;
		}

		String url = String.format("%s/item/%s", itemUuid.toString());
		logger.debug(GridClient.Log("url: " + url, client));
		// Enqueue
	}
}
