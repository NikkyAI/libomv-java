/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Copyright (c) 2011-2012, Frederick Martian
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
package libomv.utils;

import java.net.URI;

import org.apache.http.nio.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorException;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsClient;

public class UpdateChecker
{
	public class UpdateInfo
	{
	    private boolean Error;
	    private String ErrMessage;
	    private String CurrentVersion;
	    private String DownloadSite;
	    private boolean DisplayMOTD;
	    private String MOTD;
	    private boolean UpdateAvailable;

	    public boolean getError() { return Error; };
	    public void setError(boolean value) { Error = value; };
	    public String getErrMessage() { return ErrMessage; };
	    public void setErrMessage(String value) { ErrMessage = value; };
	    public String getCurrentVersion() { return CurrentVersion; };
	    public void setCurrentVersion(String value) { CurrentVersion = value; };
	    public String getDownloadSite() { return DownloadSite; };
	    public void setDownloadSite(String value) { DownloadSite = value; };
	    public boolean getDisplayMOTD() { return DisplayMOTD; };
	    public void setDisplayMOTD(boolean value) { DisplayMOTD = value; };
	    public String getMOTD() { return MOTD; };
	    public void setMOTD(String value) { MOTD = value; };
	    public boolean getUpdateAvailable() { return UpdateAvailable; };
	    public void setUpdateAvailable(boolean value) { UpdateAvailable = value; };
	}

	public class UpdateCheckerArgs implements CallbackArgs
	{
	    public boolean Success;
	    public UpdateInfo Info;

	    public boolean getSuccess() { return Success; };
	    public void setSuccess(boolean value) { Success = value; };
	    public UpdateInfo getInfo() { return Info; };
	    public void setInfo(UpdateInfo value) { Info = value; };
	}

	private Package Package;

	public CallbackHandler<UpdateCheckerArgs> OnUpdateInfoReceived = new CallbackHandler<UpdateCheckerArgs>(); 
	
    private CapsClient client;

    public UpdateChecker(Class<?> clazz)
    {
   		Package = clazz.getPackage();
    }

    public void dispose() throws InterruptedException
    {
        if (client != null)
        {
            client.shutdown(true);
            client = null;
        }
    }

    /**
     * Compare a new version with the one from this package
     * 
     * @param version Version string in the form <major>.<minor>.<bugfix>.<build>
     * @return true if the version is higher than the current version
     * @throws NumberFormatException
     */
    private boolean isNewerVersion(String version) throws NumberFormatException
    {
    	String[] verss = version.split("[\\.\\-]");
    	String[] impls = Package.getImplementationVersion().split("[\\.\\-]");
    	int impl, vers;
    	for (int i = 0; i < verss.length && i < impls.length; i++)
    	{
    		impl = Integer.parseInt(impls[i].trim());
    		vers = Integer.parseInt(verss[i].trim());
    		if (impl != vers)
    			return vers > impl;
    	}
    	return verss.length > impls.length;
    }
    
	private class OnDownloadCallback implements FutureCallback<OSD>
    {
		private UpdateCheckerArgs checkArgs;

		public OnDownloadCallback()
		{
			checkArgs = new UpdateCheckerArgs();
			checkArgs.setSuccess(false);
		}
		
		@Override
		public void cancelled()
		{
            Logger.Log("Failed fetching updatede information", Logger.LogLevel.Warning);
            OnUpdateInfoReceived.dispatch(checkArgs);
		}

		@Override
		public void completed(OSD arg)
		{
			try
	        {
				OSDMap upd = (OSDMap)arg;
                UpdateInfo inf = new UpdateInfo();
                inf.setError(upd.get("Error").AsBoolean());
                inf.setErrMessage(upd.get("ErrMessage").AsString());
                inf.setCurrentVersion(upd.get("CurrentVersion").AsString());
                inf.setDownloadSite(upd.get("DownloadSite").AsString());
                inf.setDisplayMOTD(upd.get("DisplayMOTD").AsBoolean());
                inf.setMOTD(upd.get("MOTD").AsString());
                inf.UpdateAvailable = isNewerVersion(inf.getCurrentVersion());
				checkArgs.Success = !inf.Error;
				checkArgs.Info = inf;
            }
            catch (Exception ex)
            {
                Logger.Log("Failed decoding updatede information: ", Logger.LogLevel.Warning, ex);
            }
            OnUpdateInfoReceived.dispatch(checkArgs);
		}

		@Override
		public void failed(Exception ex)
		{
            Logger.Log("Failed fetching updated information: ", Logger.LogLevel.Warning, ex);
            OnUpdateInfoReceived.dispatch(checkArgs);
		}
    }
    
    public void startCheck(URI updateCheckUri) throws IOReactorException
    {
        if (client == null)
        {
            client = new CapsClient(null, "startCheck");
            client.executeHttpGet(updateCheckUri, null, new OnDownloadCallback(), 60000);
        }
    }
}

