package libomv.io.inventory;

import java.net.URI;

import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.io.GridClient;
import libomv.types.UUID;
import libomv.utils.Callback;

public class InventoryAISClient {
	private static final Logger logger = Logger.getLogger(InventoryAISClient.class);

	private String INVENTORY_CAP_NAME = "InventoryAPIv3";
	private String LIBRARY_CAP_NAME = "LibraryAPIv3";

	enum CommandType {
		COPYINVENTORY, SLAMFOLDER, REMOVECATEGORY, REMOVEITEM, PURGEDESCENDENTS, UPDATECATEGORY, UPDATEITEM, COPYLIBRARYCATEGORY
	}

	private GridClient _Client;

	public InventoryAISClient(GridClient client) {
		_Client = client;
	}

	public boolean IsAvailable() {
		return _Client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME) != null;
	}

	public void CreateInventory(UUID parentUuid, OSD newInventory, Callback<OSD> callback) {
		URI cap = _Client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", _Client));
			return;
		}

		UUID tid = new UUID();
		String url = String.format("%s/category/%s?tid=%s", cap.toString(), parentUuid.toString(), tid.toString());
		logger.debug(GridClient.Log("url: " + url, _Client));
		// Enqueue
	}

	public void SlamFolder(UUID folderUuid, OSD newInventory, Callback<OSD> callback) {
		URI cap = _Client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", _Client));
			return;
		}

		UUID tid = new UUID();
		String url = String.format("%s/category/%s/links?tid=%s", folderUuid.toString(), tid.toString());
		logger.debug(GridClient.Log("url: " + url, _Client));
		// Enqueue
	}

	public void RemoveCategory(UUID categoryUuid, Callback<OSD> callback) {
		URI cap = _Client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", _Client));
			return;
		}

		String url = String.format("%s/category/%s", categoryUuid.toString());
		logger.debug(GridClient.Log("url: " + url, _Client));
		// Enqueue
	}

	public void RemoveItem(UUID itemUuid, Callback<OSD> callback) {
		URI cap = _Client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", _Client));
			return;
		}

		String url = String.format("%s/item/%s", itemUuid.toString());
		logger.debug(GridClient.Log("url: " + url, _Client));
		// Enqueue
	}

	public void CopyLibraryCategory(UUID sourceUuid, UUID destUuid, boolean copySubfolders, Callback<OSD> callback) {
		URI cap = _Client.network.getCurrentSim().getCapabilityURI(LIBRARY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", _Client));
			return;
		}

		UUID tid = new UUID();
		String url = String.format("%s/category/%s?tid=%s", sourceUuid.toString(), tid.toString());
		if (copySubfolders)
			url += ",depth=0";
		logger.debug(GridClient.Log("url: " + url, _Client));
		// Enqueue
	}

	public void PurgeDescendents(UUID categoryUuid, Callback<OSD> callback) {
		URI cap = _Client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", _Client));
			return;
		}

		String url = String.format("%s/category/%s/children", categoryUuid.toString());
		logger.debug(GridClient.Log("url: " + url, _Client));
		// Enqueue
	}

	public void UpdateCategory(UUID categoryUuid, OSD updates, Callback<OSD> callback) {
		URI cap = _Client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", _Client));
			return;
		}

		String url = String.format("%s/category/%s", categoryUuid.toString());
		logger.debug(GridClient.Log("url: " + url, _Client));
		// Enqueue
	}

	public void UpdateItem(UUID itemUuid, OSD updates, Callback<OSD> callback) {
		URI cap = _Client.network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
		if (cap == null) {
			logger.warn(GridClient.Log("No AIS3 Capability!", _Client));
			return;
		}

		String url = String.format("%s/item/%s", itemUuid.toString());
		logger.debug(GridClient.Log("url: " + url, _Client));
		// Enqueue
	}
}
