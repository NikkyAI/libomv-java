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
package libomv.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.UnknownServiceException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;

import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;

/// Manages HTTP texture downloads with a limit on maximum concurrent downloads
public class DownloadManager {
	private static final Logger logger = Logger.getLogger(DownloadManager.class);

	private static int num = 0;

	public class DownloadResult {
		public boolean finished;
		public int current;
		public int full;
		public byte[] data;

		public DownloadResult(int current, int full) {
			finished = false;
			this.current = current;
			this.full = full;
		}

		public DownloadResult(byte[] data) {
			finished = true;
			this.data = data;
		}
	}

	private class ActiveDownload implements Runnable {
		// URI of the item to fetch
		private URI address;
		// Timout specified in milliseconds
		private int millisecondsTimeout;
		// Accept the following content type
		private String acceptType;
		// How many times will this request be retried
		private int retries;
		// Current fetch attempt
		private int attempt;
		// The cache location to store this resources data after successful download if
		// any
		private File cacheFile;

		private CallbackHandler<DownloadResult> callbacks = new CallbackHandler<DownloadResult>();

		// Default constructor
		public ActiveDownload() {
			this.retries = 5;
			this.attempt = 0;
		}

		public ActiveDownload(URI address, int millisecondsTimeout, String acceptType, File cacheFile,
				Callback<DownloadResult> callback) {
			this();
			this.address = address;
			this.millisecondsTimeout = millisecondsTimeout;
			this.acceptType = acceptType;
			this.cacheFile = cacheFile;
			this.callbacks.add(callback, false);
		}

		public void addCallback(Callback<DownloadResult> callback) {
			this.callbacks.add(callback, false);
		}

		@Override
		public void run() {
			while (attempt++ < retries) {
				try {
					HttpURLConnection con = (HttpURLConnection) address.toURL().openConnection();
					try {
						con.setRequestProperty("Accept-Encoding", "identity");
						if (acceptType != null)
							con.setRequestProperty("Accept", acceptType);
						con.setReadTimeout(millisecondsTimeout > 0 ? millisecondsTimeout : 0);
						if (address.getScheme().equals("https")) {
							try {
								String hostname = address.getHost();
								X509Certificate certificate = Helpers.getCertificate(hostname);
								if (certificate != null) {
									KeyStore keyStore = Helpers.getExtendedKeyStore();
									keyStore.setCertificateEntry(hostname, certificate);
									KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
									kmf.init(keyStore, null);
									SSLContext context = SSLContext.getInstance("TLS");
									context.init(kmf.getKeyManagers(), null, null);
									((HttpsURLConnection) con).setSSLSocketFactory(context.getSocketFactory());
								}
							} catch (Exception e) {
								/*
								 * Ignore any exceptions here and let the connection fail if the security
								 * manager has a problem
								 */
							}
						}
						con.connect();
						int len, total = con.getContentLength();
						ByteArrayOutputStream bos = new ByteArrayOutputStream(total > 0 ? total : 10000);
						InputStream is = con.getInputStream();
						byte b[] = new byte[10000];
						while ((len = is.read(b)) >= 0) {
							callbacks.dispatch(new DownloadResult(len, total));
							bos.write(b, 0, len);
						}
						if (cacheFile != null) {
							try {
								FileOutputStream fos = new FileOutputStream(cacheFile);
								try {
									fos.write(bos.toByteArray());
								} finally {
									fos.close();
								}
							} catch (Exception ex) {
							}
						}
						callbacks.dispatch(new DownloadResult(bos.toByteArray()));
						return;
					} catch (MalformedURLException ex) {
						logger.debug("HTTP Texture download failed, attempt " + attempt + " from " + retries, ex);
					} catch (UnknownServiceException ex) {
						logger.debug("HTTP Texture download failed, attempt " + attempt + " from " + retries, ex);
					} catch (ProtocolException ex) {
						logger.debug("HTTP Texture download failed, attempt " + attempt + " from " + retries, ex);
					} catch (IOException ex) {
						logger.debug("HTTP Texture download failed, attempt " + attempt + " from " + retries, ex);
					} finally {
						con.disconnect();
					}
				} catch (Exception ex) {
					logger.debug("HTTP Texture download failed, attempt " + attempt + " from " + retries, ex);
				}
			}
			callbacks.dispatch(new DownloadResult(null));
		}
	}

	class SimpleThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			return new Thread(r, "DownloadManager" + ++num);
		}
	}

	private ExecutorService execPool = Executors.newFixedThreadPool(8, new SimpleThreadFactory());
	private Hashtable<URI, ActiveDownload> requests = new Hashtable<URI, ActiveDownload>();

	// Enqueue a new HTPP download
	public void enque(URI address, int millisecondsTimeout, String acceptType, File cacheFile,
			Callback<DownloadResult> callback) {
		synchronized (requests) {
			ActiveDownload download = requests.get(address);
			if (download == null) {
				download = new ActiveDownload(address, millisecondsTimeout, acceptType, cacheFile, callback);
				requests.put(address, download);
				execPool.submit(download);
			} else if (callback != null) {
				download.addCallback(callback);
			}
		}
	}

	// Cleanup method
	public void shutdown() {
		execPool.shutdownNow();
		requests.clear();
	}
}
