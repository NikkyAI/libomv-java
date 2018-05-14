package libomv.inventory;

import java.net.URI;

import libomv.GridClient;
import libomv.StructuredData.OSD;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class InventoryAISClient
{

    private String INVENTORY_CAP_NAME = "InventoryAPIv3";
    private String LIBRARY_CAP_NAME   = "LibraryAPIv3";

    enum CommandType {
	           COPYINVENTORY,
	           SLAMFOLDER,
	           REMOVECATEGORY,
	           REMOVEITEM,
	           PURGEDESCENDENTS,
	           UPDATECATEGORY,
	           UPDATEITEM,
	           COPYLIBRARYCATEGORY
	}
    
    private GridClient _Client;

    public InventoryAISClient(GridClient client)
    {
        _Client = client;
    }

    public boolean IsAvailable()
    {
    	return _Client.Network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME) != null;
    }

    public void CreateInventory(UUID parentUuid, OSD newInventory, Callback<OSD> callback)
    {
        URI cap = _Client.Network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
        if (cap == null)
        {
            Logger.Log("No AIS3 Capability!", LogLevel.Warning, _Client);
            return;
        }

        UUID tid = new UUID();
        String url = String.format("%s/category/%s?tid=%s", cap.toString(), parentUuid.toString(), tid.toString());
        Logger.Log("url: " + url, LogLevel.Debug, _Client);
       // Enqueue
    }

    public void SlamFolder(UUID folderUuid, OSD newInventory, Callback<OSD> callback)
    {
        URI cap = _Client.Network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
        if (cap == null)
        {
            Logger.Log("No AIS3 Capability!", LogLevel.Warning, _Client);
            return;
        }

        UUID tid = new UUID();
        String url = String.format("%s/category/%s/links?tid=%s", folderUuid.toString(), tid.toString());
        Logger.Log("url: " + url,  LogLevel.Debug, _Client);
        // Enqueue
    }

    public void RemoveCategory(UUID categoryUuid, Callback<OSD> callback)
    {
        URI cap = _Client.Network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
        if (cap == null)
        {
            Logger.Log("No AIS3 Capability!",  LogLevel.Warning, _Client);
            return;
        }

        String url = String.format("%s/category/%s", categoryUuid.toString());
        Logger.Log("url: " + url,  LogLevel.Debug, _Client);
        // Enqueue
    }

    public void RemoveItem(UUID itemUuid, Callback<OSD> callback)
    {
        URI cap = _Client.Network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
        if (cap == null)
        {
            Logger.Log("No AIS3 Capability!", LogLevel.Warning, _Client);
            return;
        }

        String url = String.format("%s/item/%s", itemUuid.toString());
        Logger.Log("url: " + url,  LogLevel.Debug, _Client);
        // Enqueue
    }

    public void CopyLibraryCategory(UUID sourceUuid, UUID destUuid, boolean copySubfolders, Callback<OSD> callback)
    {
        URI cap = _Client.Network.getCurrentSim().getCapabilityURI(LIBRARY_CAP_NAME);
        if (cap == null)
        {
            Logger.Log("No AIS3 Capability!", LogLevel.Warning, _Client);
            return;
        }

        UUID tid = new UUID();
        String url = String.format("%s/category/%s?tid=%s", sourceUuid.toString(), tid.toString());
        if (copySubfolders)
            url += ",depth=0";
        Logger.Log("url: " + url, LogLevel.Debug, _Client);
        // Enqueue
    }

    public void PurgeDescendents(UUID categoryUuid, Callback<OSD> callback)
    {
    	URI cap = _Client.Network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
        if (cap == null)
        {
            Logger.Log("No AIS3 Capability!",  LogLevel.Warning, _Client);
            return;
        }

        String url = String.format("%s/category/%s/children", categoryUuid.toString());
        Logger.Log("url: " + url,  LogLevel.Debug, _Client);
        // Enqueue
    }

    public void UpdateCategory(UUID categoryUuid, OSD updates, Callback<OSD> callback)
    {
    	URI cap = _Client.Network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
        if (cap == null)
        {
            Logger.Log("No AIS3 Capability!",  LogLevel.Warning, _Client);
            return;
        }

        String url = String.format("%s/category/%s", categoryUuid.toString());
        Logger.Log("url: " + url,  LogLevel.Debug, _Client);
        // Enqueue
    }

    public void UpdateItem(UUID itemUuid, OSD updates, Callback<OSD> callback)
    {
    	URI cap = _Client.Network.getCurrentSim().getCapabilityURI(INVENTORY_CAP_NAME);
        if (cap == null)
        {
            Logger.Log("No AIS3 Capability!",  LogLevel.Warning, _Client);
            return;
        }

        String url = String.format("%s/item/%s", itemUuid.toString());
        Logger.Log("url: " + url, LogLevel.Debug, _Client);
        // Enqueue
    }
}
