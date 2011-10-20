/**
 * Portions Copyright (c) 2009-2011, Frederick Martian
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
package libomv.capabilities;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.nio.reactor.IOReactorException;

import libomv.StructuredData.OSD;
import libomv.utils.Logger;

public class CapsClient extends AsyncHTTPClient<OSD>
{
	public CapsClient(URI address) throws IOReactorException
	{
		super(address, null);
	}

	public CapsClient(URI address, X509Certificate cert) throws IOReactorException
	{
		super(address, cert);
	}

	/**
	 * Synchronous HTTP Get request from a capability that requires no further
	 * request entity This function returns either after the server responded
	 * with any data or when the timeout expired
	 * 
	 * @param timeout
	 *            The timeout in ms to wait for a request
	 * @return Returns the response parsed into OSD data
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 * @throws ClientProtocolException
	 */
	public OSD GetResponse(String acceptHeader, long timeout) throws InterruptedException, ExecutionException, TimeoutException
	{
		Future<OSD> result = BeginGetResponse(acceptHeader, -1);
		return result.get(timeout, TimeUnit.MILLISECONDS);
	}

	public OSD GetResponse(OSD data, OSD.OSDFormat format, long timeout) throws InterruptedException,
			ExecutionException, TimeoutException, IOException
	{
		Future<OSD> result = BeginGetResponse(data, format, -1);
		return result.get(timeout, TimeUnit.MILLISECONDS);
	}

	public OSD GetResponse(byte[] postData, String contentType, long timeout) throws InterruptedException,
			ExecutionException, TimeoutException
	{
		Future<OSD> result = BeginGetResponse(postData, contentType, -1);
		return result.get(timeout, TimeUnit.MILLISECONDS);
	}

	public Future<OSD> BeginGetResponse(IMessage message, long timeout) throws IOException
	{
		return BeginGetResponse(message.Serialize(), OSD.OSDFormat.Xml, timeout);
	}

	public Future<OSD> BeginGetResponse(OSD data, OSD.OSDFormat format, long timeout) throws IOException
	{
		byte[] postData = null;
		String contentType;

		if (data != null)
			postData = data.serializeToBytes(format);

		switch (format)
		{
			case Xml:
				contentType = "application/llsd+xml";
				break;
			case Binary:
				contentType = "application/llsd+binary";
				break;
			case Json:
			default:
				contentType = "application/llsd+json";
				break;
		}
		return BeginGetResponse(postData, contentType, timeout);
	}

	@Override
	protected OSD convertContent(InputStreamReader in) throws IOException
	{
		try
		{
			return OSD.parse(in);
		}
		catch (ParseException ex)
		{
			Logger.Log("Error converting the HTTP response into structured data at offset " + ex.getErrorOffset(),
					   Logger.LogLevel.Error);
		}
		return null;
	}
}
