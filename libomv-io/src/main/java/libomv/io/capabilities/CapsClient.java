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
package libomv.io.capabilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

import libomv.Statistics.Type;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDParser;
import libomv.capabilities.IMessage;
import libomv.io.GridClient;
import libomv.io.LibSettings;
import libomv.utils.Callback;
import libomv.utils.Settings.SettingsUpdateCallbackArgs;

public class CapsClient extends AsyncHTTPClient<OSD>
{
	private static final Logger logger = Logger.getLogger(CapsClient.class);
	private GridClient _Client;
	
	private boolean trackUtilization;
	
	private class SettingsUpdate implements Callback<SettingsUpdateCallbackArgs>
	{
		@Override
		public boolean callback(SettingsUpdateCallbackArgs params)
		{
			String key = params.getName();
			if (key == null)
			{
				trackUtilization = _Client.Settings.getBool(LibSettings.TRACK_UTILIZATION);
			}
			else if (key.equals(LibSettings.TRACK_UTILIZATION))
			{
				trackUtilization = params.getValue().AsBoolean();
			}
			return false;
		}
	}

	public CapsClient(GridClient client, String name) throws IOReactorException
	{
		super(name);
		_Client = client;

		if (client != null)
		{
			client.Settings.OnSettingsUpdate.add(new SettingsUpdate());
			trackUtilization = client.Settings.getBool(LibSettings.TRACK_UTILIZATION);
		}
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		try
		{
			shutdown(true);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		finally
		{
			super.finalize();
		}
	}

	/**
	 * Synchronous HTTP Get request from a capability that requires no further
	 * request entity. This function returns either after the server responded
	 * with any data or when the timeout expired
	 * 
	 * @param address The timeout in ms to wait for a request
	 * @param acceptHeader The content type to add as Accept: header or null
	 * @param timeout The timeout in ms to wait for a request
	 * @return Returns the response parsed into OSD data
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public OSD getResponse(URI address, String acceptHeader, long timeout)
			throws InterruptedException, ExecutionException, TimeoutException
	{
		Future<OSD> result = executeHttpGet(address, acceptHeader);
		return result.get(timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Synchronous HTTP Get request from a capability that requires no further
	 * request entity. This function returns either after the server responded
	 * with any data or when the timeout expired
	 * 
	 * @param address The timeout in ms to wait for a request
	 * @param acceptHeader The content type to add as Accept: header or null
	 * @param timeout The timeout in ms to wait for a request
	 * @return Returns the response parsed into OSD data
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public OSD getResponse(URI address, IMessage message, FutureCallback<OSD> callback, long timeout)
			throws InterruptedException, ExecutionException, TimeoutException
	{
		Future<OSD> result = executeHttpPost(address, message.Serialize(), OSD.OSDFormat.Xml, null, null, TIMEOUT_INFINITE);
		return result.get(timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Synchronous HTTP Post request from a capability that requires a OSD formated
	 * request entity. This function returns either after the server responded
	 * with any data or when the timeout expired
	 * 
	 * @param address The timeout in ms to wait for a request
	 * @param data The OSD data
	 * @param format The OSD data format to serialize the data into
	 * @param timeout The timeout in ms to wait for a request
	 * @return Returns the response parsed into OSD data
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public OSD getResponse(URI address, OSD data, OSD.OSDFormat format, long timeout)
				throws InterruptedException, ExecutionException, TimeoutException, IOException
	{
		Future<OSD> result = executeHttpPost(address, data, format, null, null, TIMEOUT_INFINITE);
		return result.get(timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Synchronous HTTP Post request from a capability that requires a OSD formated
	 * request entity. This function returns either after the server responded
	 * with any data or when the timeout expired
	 * 
	 * @param address The timeout in ms to wait for a request
	 * @param data The OSD data
	 * @param format The OSD data format to serialize the data into
	 * @param encoding The encoding to use in the header
	 * @param timeout The timeout in ms to wait for a request
	 * @return Returns the response parsed into OSD data
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public OSD getResponse(URI address, byte[] postData, String contentType, String encoding, long timeout)
				throws InterruptedException, ExecutionException, TimeoutException
	{
		Future<OSD> result = executeHttpPost(address, postData, contentType, encoding);
		return result.get(timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Asynchronous HTTP Post request from a capability that requires a OSD formated
	 * request entity. This function returns either after the server responded
	 * with any data or when the timeout expired
	 * 
	 * @param address The uri to post the data to
	 * @param message The Caps message to send
	 * @param callback The callback to call for reporting of failure, success or cancel and returning a response to
	 * @param timeout The timeout in ms to wait for a request
	 * @return A Future that can be used to retrieve the data as OSD or to cancel the request
	 * @throws IOException 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public Future<OSD> executeHttpPost(URI address, IMessage message, FutureCallback<OSD> callback, long timeout)
	{
		return executeHttpPost(address, message.Serialize(), OSD.OSDFormat.Xml, null, callback, timeout);
	}

	/**
	 * Asynchronous HTTP Post request from a capability that requires a OSD formated
	 * request entity. This function returns either after the server responded
	 * with any data or when the timeout expired
	 * 
	 * @param address The uri to post the data to
	 * @param data The OSD data
	 * @param format The OSD data format to serialize the data into
	 * @return A Future that can be used to retrieve the data as OSD or to cancel the request
	 * @throws IOException 
	 */
	public Future<OSD> executeHttpPost(URI address, OSD data, OSD.OSDFormat format)
	{
		return executeHttpPost(address, data, format, null, null, TIMEOUT_INFINITE);
	}

	/**
	 * Asynchronous HTTP Post request from a capability that requires a OSD formated
	 * request entity. This function returns either after the server responded
	 * with any data or when the timeout expired
	 * 
	 * @param address The uri to post the data to
	 * @param data The OSD data
	 * @param format The OSD data format to serialize the data into
	 * @param callback The callback to call for reporting of failure or success or null
	 * @param timeout The timeout in ms to wait for a request
	 * @return A Future that can be used to retrieve the data as OSD or to cancel the request
	 * @throws IOException 
	 */
	public Future<OSD> executeHttpPost(URI address, OSD data, OSD.OSDFormat format, FutureCallback<OSD> callback, long timeout)
	{
		return executeHttpPost(address, data, format, null, callback, timeout);
	}

	/**
	 * Asynchronous HTTP Post request from a capability that requires an OSD formated
	 * request entity. This function returns either after the server responded
	 * with any data or when the timeout expired
	 * 
	 * @param address The uri to post the data to
	 * @param data The OSD data
	 * @param format The OSD data format to serialize the data into
	 * @param encoding The encoding to use to stream the data
	 * @param callback The callback to call for reporting of failure or success or null
	 * @param timeout The timeout in ms to wait for a request
	 * @return A Future that can be used to retrieve the data as OSD or to cancel the request
	 */
	public Future<OSD> executeHttpPost(URI address, OSD data, OSD.OSDFormat format, String encoding,
			                           FutureCallback<OSD> callback, long timeout)
	{
		AbstractHttpEntity entity = new OSDEntity(data, format);
		if (encoding != null)
			entity.setContentEncoding(encoding);

		// #region Stats Tracking
		if (_Client != null && trackUtilization)
		{
			_Client.Stats.updateNetStats(name, Type.Message, entity.getContentLength(), 0);
		}
		// #endregion
		return executeHttpPost(address, entity, callback, timeout);
	}
	
	private class OSDEntity extends AbstractHttpEntity
	{
		private byte[] bytes;
		private OSD osd;
		private OSDFormat format;
		
		public OSDEntity(OSD osd, OSDFormat format)
		{
			super();
			this.osd = osd;
			this.format = format;
			setContentType(OSDFormat.contentType(format));
			setContentEncoding(OSDFormat.contentEncodingDefault(format));
		}

		private byte[] getBytes() throws IOException
		{
			if (bytes == null)
			{
				bytes = OSDParser.serializeToBytes(osd, format, false, getContentEncoding().getValue());
			}
			return bytes;
		}
		
		@Override
		public boolean isRepeatable()
		{
			return true;
		}

		@Override
		public long getContentLength()
		{
			try
			{
				return getBytes().length;
			} 
			catch (IOException e)
			{
			}
			return -1;
		}

		@Override
		public InputStream getContent() throws IOException
		{
			return new ByteArrayInputStream(getBytes());
		}

		@Override
		public void writeTo(OutputStream outstream) throws IOException, IllegalArgumentException
		{
			if (outstream == null)
			{
				throw new IllegalArgumentException("Output stream may not be null");
			}
			outstream.write(getBytes());
		}

		@Override
		public boolean isStreaming()
		{
			return false;
		}
	}

	@Override
	protected OSD convertContent(InputStream in, String encoding) throws IOException
	{
		try
		{
			return OSDParser.deserialize(in, OSDFormat.Xml, encoding);
		}
		catch (ParseException ex)
		{
			logger.error("Error converting the HTTP response into structured data at offset " + ex.getErrorOffset());
		}
		return null;
	}
}
