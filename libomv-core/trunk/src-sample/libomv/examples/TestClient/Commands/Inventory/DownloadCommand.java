/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2014, Frederick Martian
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
package libomv.examples.TestClient.Commands.Inventory;

import java.io.File;
import java.io.FileOutputStream;

import libomv.assets.AssetItem.AssetType;
import libomv.assets.AssetManager.AssetDownload;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Logger;
import libomv.utils.TimeoutEvent;

public class DownloadCommand extends Command
{
    private UUID _AssetID;
    private AssetType _AssetType;
    TimeoutEvent<Boolean> DownloadHandle = new TimeoutEvent<Boolean>();
    String usage = "Usage: download [uuid] [assetType]";

    public DownloadCommand(TestClient testClient)
    {
        Name = "download";
        Description = "Downloads the specified asset. " + usage;
        Category = CommandCategory.Inventory;
    }

	@Override
	public String execute(String[] args, UUID fromAgentID) throws Exception
	{
        if (args.length != 2)
            return usage;

        _AssetID = UUID.parse(args[0]);
        _AssetType = AssetType.Unknown;
 
        if (_AssetID == null)
            return usage;

        DownloadHandle.reset();
       	_AssetType = AssetType.setValue(args[1]);
       	if (_AssetType == AssetType.Unknown)
        {
            return usage;
        }

        // Start the asset download
        Client.Assets.RequestAsset(_AssetID, _AssetType, true, new Assets_OnAssetReceived());

        Boolean success = DownloadHandle.waitOne(120 * 1000, false);
        if (success != null)
        {
            if (success)
            	return String.format("Saved %s.%s", _AssetID, _AssetType.toString().toLowerCase());
            return String.format("Failed to download asset %s, perhaps %s is the incorrect asset type?", _AssetID, _AssetType);
        }
        return "Timed out waiting for texture download";
	}
	
	private class Assets_OnAssetReceived implements Callback<AssetDownload>
	{
		@Override
		public boolean callback(AssetDownload params)
		{
	        if (params.ItemID.equals(_AssetID))
	        {
	            if (params.Success)
	            {
	            	FileOutputStream os = null;
	                try
	                {
	                	File file = new File(_AssetID.toString() + "." + _AssetType.toString().toLowerCase());
	                	os = new FileOutputStream(file);
	                	os.write(params.AssetData);
	    	            DownloadHandle.set(false);
	                }
	                catch (Exception ex)
	                {
	                    Logger.Log(ex.getMessage(), Logger.LogLevel.Error, ex);
	                }
	                finally
	                {
	                	try
	                	{
	                		if (os != null)
	   	                		os.close();
	                	}
	                	catch (Exception ex) {}
	                }
	            }
	            DownloadHandle.set(false);
	        }
            return true;
		}
    }
}
