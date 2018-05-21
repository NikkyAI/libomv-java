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
package libomv.io.utils;

import java.io.IOException;
import java.net.URI;

import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.io.capabilities.CapsClient;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;

public class UpdateChecker {
	private static final Logger logger = Logger.getLogger(UpdateChecker.class);

	public class UpdateInfo {
		private boolean error;
		private String errMessage;
		private String currentVersion;
		private String downloadSite;
		private boolean displayMOTD;
		private String motd;
		private boolean updateAvailable;

		public boolean getError() {
			return error;
		};

		public void setError(boolean value) {
			error = value;
		};

		public String getErrMessage() {
			return errMessage;
		};

		public void setErrMessage(String value) {
			errMessage = value;
		};

		public String getCurrentVersion() {
			return currentVersion;
		};

		public void setCurrentVersion(String value) {
			currentVersion = value;
		};

		public String getDownloadSite() {
			return downloadSite;
		};

		public void setDownloadSite(String value) {
			downloadSite = value;
		};

		public boolean getDisplayMOTD() {
			return displayMOTD;
		};

		public void setDisplayMOTD(boolean value) {
			displayMOTD = value;
		};

		public String getMOTD() {
			return motd;
		};

		public void setMOTD(String value) {
			motd = value;
		};

		public boolean getUpdateAvailable() {
			return updateAvailable;
		};

		public void setUpdateAvailable(boolean value) {
			updateAvailable = value;
		};
	}

	public class UpdateCheckerArgs implements CallbackArgs {
		public boolean success;
		public UpdateInfo info;

		public boolean getSuccess() {
			return success;
		};

		public void setSuccess(boolean value) {
			success = value;
		};

		public UpdateInfo getInfo() {
			return info;
		};

		public void setInfo(UpdateInfo value) {
			info = value;
		};
	}

	private Package pkg;

	public CallbackHandler<UpdateCheckerArgs> onUpdateInfoReceived = new CallbackHandler<>();

	private CapsClient client;

	public UpdateChecker(Class<?> clazz) {
		pkg = clazz.getPackage();
	}

	public void dispose() throws InterruptedException, IOException {
		if (client != null) {
			client.shutdown(true);
			client = null;
		}
	}

	/**
	 * Compare a new version with the one from this package
	 *
	 * @param version
	 *            Version string in the form <major>.<minor>.<bugfix>.<build>
	 * @return true if the version is higher than the current version
	 * @throws NumberFormatException
	 */
	private boolean isNewerVersion(String version) throws NumberFormatException {
		String[] verss = version.split("[\\.\\-]");
		String[] impls = pkg.getImplementationVersion().split("[\\.\\-]");
		int impl;
		int vers;
		for (int i = 0; i < verss.length && i < impls.length; i++) {
			impl = Integer.parseInt(impls[i].trim());
			vers = Integer.parseInt(verss[i].trim());
			if (impl != vers)
				return vers > impl;
		}
		return verss.length > impls.length;
	}

	private class OnDownloadCallback implements FutureCallback<OSD> {
		private UpdateCheckerArgs checkArgs;

		public OnDownloadCallback() {
			checkArgs = new UpdateCheckerArgs();
			checkArgs.setSuccess(false);
		}

		@Override
		public void cancelled() {
			logger.warn("Failed fetching updatede information");
			onUpdateInfoReceived.dispatch(checkArgs);
		}

		@Override
		public void completed(OSD arg) {
			try {
				OSDMap upd = (OSDMap) arg;
				UpdateInfo inf = new UpdateInfo();
				inf.setError(upd.get("Error").asBoolean());
				inf.setErrMessage(upd.get("ErrMessage").asString());
				inf.setCurrentVersion(upd.get("CurrentVersion").asString());
				inf.setDownloadSite(upd.get("DownloadSite").asString());
				inf.setDisplayMOTD(upd.get("DisplayMOTD").asBoolean());
				inf.setMOTD(upd.get("MOTD").asString());
				inf.updateAvailable = isNewerVersion(inf.getCurrentVersion());
				checkArgs.success = !inf.error;
				checkArgs.info = inf;
			} catch (Exception ex) {
				logger.warn("Failed decoding updatede information: ", ex);
			}
			onUpdateInfoReceived.dispatch(checkArgs);
		}

		@Override
		public void failed(Exception ex) {
			logger.warn("Failed fetching updated information: ", ex);
			onUpdateInfoReceived.dispatch(checkArgs);
		}
	}

	public void startCheck(URI updateCheckUri) throws IOReactorException {
		if (client == null) {
			client = new CapsClient(null, "startCheck");
			client.executeHttpGet(updateCheckUri, null, new OnDownloadCallback(), 60000);
		}
	}
}
