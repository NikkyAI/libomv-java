/**
 * Copyright (c) 2009-2011, Frederick Martian
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
package libomv.capabilities;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncRequestProducer;
import org.apache.http.nio.client.HttpAsyncResponseConsumer;
import org.apache.http.nio.concurrent.BasicFuture;
import org.apache.http.nio.concurrent.FutureCallback;
import org.apache.http.nio.conn.scheme.Scheme;
import org.apache.http.nio.conn.ssl.SSLLayeringStrategy;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NHttpEntityWrapper;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.entity.ProducingNHttpEntity;
import org.apache.http.nio.reactor.IOReactorException;

public abstract class AsyncHTTPClient<T>
{
	public static final long TIMEOUT_INFINITE = -1;

	public interface ProgressCallback
	{
		public void progress(long bytesTransceived, long totalBytes);
	}
	
	private final DefaultHttpAsyncClient client;
	private X509Certificate certificate;
	private Timer timeout;
	private Future<T> resultFuture;
	private FutureCallback<T> resultCb;
	private ProgressCallback progressCb;

	public void setProgressCallback(ProgressCallback callback)
	{
		this.progressCb = callback;
	}
	
	public void setCertificate(X509Certificate cert)
	{
		this.certificate = cert;
	}
	
	/**
	 * Sets basic authentication on web request using plain credentials
	 *
	 * @param uri The uri for which the authentication credentials should be applied
	 * @param username The plain text username
	 * @param password The plain text password
	 */
	public void setBasicAuthentication(URI uri, String username, String password)
	{
		client.getCredentialsProvider().setCredentials(
				new AuthScope(uri.getHost(), uri.getPort(), AuthScope.ANY_REALM),
				new UsernamePasswordCredentials(username, password));
	}

	/**
	 * Registers a new scheme for this client. Useful to provide a scheme with
	 * custom security provider such as for certificate verification for the
	 * HTTPS scheme
	 *
	 * @param scheme The scheme to add to the connection manager for this connection
	 * @return The scheme registered
	 */
	public Scheme register(Scheme scheme)
	{
		return client.getConnectionManager().getSchemeRegistry().register(scheme);
	}

	private void cancel(boolean mayInterruptIfRunning)
	{
		synchronized (this)
		{
			if (timeout != null)
			{
				timeout.cancel();
				timeout = null;
			}
			if (resultFuture != null)
			{
				if (!resultFuture.isDone())
				{
					resultFuture.cancel(mayInterruptIfRunning);
				}
				resultFuture = null;
			}
		}
	}
	
	public void shutdown() throws InterruptedException
	{
		cancel(true);
		client.shutdown();
	}

	public AsyncHTTPClient() throws IOReactorException
	{
		client = new DefaultHttpAsyncClient();
		client.start();
	}

	/**
	 * Do a HTTP Get Request from the server without any timeout
	 * 
	 * @param address The document uri to fetch
	 * @return A Future that can be used to retrieve the data or cancel the request
	 */
	public Future<T> executeHttpGet(URI address)
	{
		return executeHttp(new HttpGet(address), null, TIMEOUT_INFINITE);
	}
	
	/**
	 * Do a HTTP Get Request from the server without any timeout
	 * 
	 * @param address The document uri to fetch
	 * @param acceptHeader The content type to add as Accept: header or null
	 * @return A Future that can be used to retrieve the data or cancel the request
	 */
	public Future<T> executeHttpGet(URI address, String acceptHeader)
	{
		HttpGet request = new HttpGet(address);
		if (acceptHeader != null && !acceptHeader.isEmpty())
			request.addHeader("Accept", acceptHeader);
		return executeHttp(request, null, TIMEOUT_INFINITE);
	}

	/**
	 * Do a HTTP Get Request from the server
	 * 
	 * @param address The document uri to fetch
	 * @param acceptHeader The content type to add as Accept: header or null
	 * @param callback The result callback to be called on success, exception or failure
	 * @param millisecondTimeout The timeout to wait for a response or -1 if no timeout should be used
	 *                The request can still be aborted through the returned future.
	 * @return A Future that can be used to cancel the request
	 */
	public Future<T> executeHttpGet(URI address, String acceptHeader, FutureCallback<T> callback, long millisecondTimeout)
	{
		HttpGet request = new HttpGet(address);
		if (acceptHeader != null && !acceptHeader.isEmpty())
			request.addHeader("Accept", acceptHeader);
		return executeHttp(request, callback, millisecondTimeout);
	}

	/**
	 * Do a HTTP Post Request from the server from string data
	 * 
	 * @param address The uri to post
	 * @param data The string data to add as entity content
	 * @param contentType The content type to add as ContentType: header or null
	 * @param encoding The encoding to use to stream the data
	 * @return A Future that can be used to retrieve the data or cancel the request
	 * @throws UnsupportedEncodingException 
	 */
	public Future<T> executeHttpPost(URI address, String data, String contentType, String encoding) throws UnsupportedEncodingException
	{
		AbstractHttpEntity entity = new NStringEntity(data);
		if (contentType != null && !contentType.isEmpty())
			entity.setContentType(contentType);
		if (encoding != null)
			entity.setContentEncoding(encoding);
		return executeHttpPost(address, entity, null, TIMEOUT_INFINITE);
	}

	/**
	 * Do a HTTP Post Request from the server from string data
	 * 
	 * @param address The uri to post
	 * @param data The string data to add as entity content
	 * @param contentType The content type to add as ContentType: header or null
	 * @param encoding The encoding to use to stream the data
	 * @param callback The result callback to be called on success, exception or failure
	 * @param millisecondTimeout The timeout to wait for a response or -1 if no timeout should be used
	 *                The request can still be aborted through the returned future.
	 * @return A Future that can be used to retrieve the data or cancel the request
	 * @throws UnsupportedEncodingException 
	 */
	public Future<T> executeHttpPost(URI address, String data, String contentType, String encoding,
							FutureCallback<T> callback, long millisecondTimeout) throws UnsupportedEncodingException
	{
		AbstractHttpEntity entity = new NStringEntity(data);
		if (contentType != null && !contentType.isEmpty())
			entity.setContentType(contentType);
		if (encoding != null)
			entity.setContentEncoding(encoding);
		return executeHttpPost(address, entity, callback, millisecondTimeout);
	}

	/**
	 * Do a HTTP Post Request from the server from binary data
	 * 
	 * @param address The uri to post
	 * @param data The binary data to add as entity content
	 * @param contentType The content type to add as ContentType: header or null
	 * @param encoding The encoding to use in the header
	 * @return A Future that can be used to retrieve the data or cancel the request
	 */
	public Future<T> executeHttpPost(URI address, byte[] data, String contentType, String encoding)
	{
		AbstractHttpEntity entity = new NByteArrayEntity(data);
		if (contentType != null && !contentType.isEmpty())
			entity.setContentType(contentType);
		if (encoding != null)
			entity.setContentEncoding(encoding);
		return executeHttpPost(address, entity, null, TIMEOUT_INFINITE);
	}

	/**
	 * Do a HTTP Post Request from the server from binary data
	 * 
	 * @param address The uri to post the data to
	 * @param data The binary data to add as entity content
	 * @param contentType The content type to add as ContentType: header or null
	 * @param encoding The encoding to use in the header
	 * @param callback The result callback to be called on success, exception or failure
	 * @param millisecondTimeout The timeout to wait for a response or -1 if no timeout should be used
	 *                The request can still be aborted through the returned future.
	 * @return A Future that can be used to retrieve the data or cancel the request
	 */
	public Future<T> executeHttpPost(URI address, byte[] data, String contentType, String encoding,
							         FutureCallback<T> callback, long millisecondTimeout)
	{
		AbstractHttpEntity entity = new NByteArrayEntity(data);
		if (contentType != null && !contentType.isEmpty())
			entity.setContentType(contentType);
		if (encoding != null)
			entity.setContentEncoding(encoding);
		return executeHttpPost(address, entity, callback, millisecondTimeout);
	}
	
	/**
	 * Do a HTTP Put Request from the server from file data
	 * 
	 * @param address The uri to post the data to
	 * @param file The binary data to add as entity content
	 * @param contentType The content type to add as ContentType: header or null
	 * @param encoding The encoding to use in the header
	 * @param callback The result callback to be called on success, exception or failure
	 * @param millisecondTimeout The timeout to wait for a response or -1 if no timeout should be used
	 *                The request can still be aborted through the returned future.
	 * @return A Future that can be used to retrieve the data or cancel the request
	 */
	public Future<T> executeHttpPut(URI address, File file, String contentType, String encoding,
	         FutureCallback<T> callback, long millisecondTimeout)
	{
		AbstractHttpEntity entity = new NFileEntity(file, contentType);
		if (encoding != null)
			entity.setContentEncoding(encoding);
		HttpPut request = new HttpPut(address);
		request.setEntity(entity);
		return executeHttp(request, callback, millisecondTimeout);
	}
	
	/**
	 * Do a HTTP Post Request from the server from binary data
	 * 
	 * @param address The uri to post the data to
	 * @param entity The content entity to send
	 * @param callback The result callback to be called on success, exception or failure
	 * @param millisecondTimeout The timeout to wait for a response or -1 if no timeout should be used
	 *                The request can still be aborted through the returned future.
	 * @return A Future that can be used to retrieve the data or cancel the request
	 */
	public Future<T> executeHttpPost(URI address, HttpEntity entity,
			                         FutureCallback<T> callback, long millisecondTimeout)
	{
		// Create the request
		HttpPost request = new HttpPost(address);
		// set POST body
		request.setEntity(entity);
		return executeHttp(request, callback, millisecondTimeout);
	}

	private HttpHost determineTarget(URI address)
	{
		if (address.getScheme().equals("https"))
		{
			try
			{
				String host = address.getHost();
				if (certificate == null)
				{
					certificate = Helpers.getCertificate(host);
				}

				if (certificate != null)
				{
					KeyStore store = Helpers.getExtendedKeyStore();
					store.setCertificateEntry(host, certificate);
					register(new Scheme("https", 443, new SSLLayeringStrategy(store)));
				}
			}
			catch (Exception ex)
			{
				// Ignore exceptions that happen while trying to add extra certificates to keystore
			}
		}
		return URIUtils.extractHost(address);
	}

	private Future<T> executeHttp(HttpRequestBase request, FutureCallback<T> callback, long millisecondTimeout)
	{
		synchronized (this)
		{
			if (timeout != null)
			{
				Logger.Log("This Capability Client is already waiting for a response", Logger.LogLevel.Error);
				return null;
			}

			if (callback != null)
				resultCb = callback;

			try
			{
				resultFuture = client.execute(new AsyncHttpRequestProducer(determineTarget(request.getURI()), request),
					                                	new AsyncHttpResponseConsumer(), new AsyncHttpResultCallback());

				if (millisecondTimeout >= 0)
				{
					timeout = new Timer();
					timeout.schedule(new TimerTask()
					{
						@Override
						public void run()
						{
							AsyncHTTPClient.this.cancel(true);
						}
					}, millisecondTimeout);
				}
				return resultFuture;
			}
			catch (Exception ex)
			{
				BasicFuture<T> failed = new BasicFuture<T>(resultCb);
				failed.failed(ex);
				return failed;
			}
		}
	}

	private class AsyncHttpResultCallback implements FutureCallback<T>
	{
		@Override
		public void completed(T result)
		{
			if (resultCb != null)
				resultCb.completed(result);
			cancel(false);
		}

		@Override
		public void failed(Exception ex)
		{
			if (resultCb != null)
				resultCb.failed(ex);
			cancel(false);
		}

		@Override
		public void cancelled()
		{
			if (resultCb != null)
				resultCb.cancelled();
			cancel(false);
		}
	}

	private class AsyncHttpRequestProducer implements HttpAsyncRequestProducer, Closeable
	{
		private final HttpHost target;
		private final HttpRequestBase request;
		private final ProducingNHttpEntity producer;

		public AsyncHttpRequestProducer(final HttpHost target, final HttpRequestBase request) throws IOException
		{
			super();
			if (target == null)
			{
				throw new IllegalArgumentException("Target host may not be null");
			}
			if (request == null)
			{
				throw new IllegalArgumentException("HTTP request may not be null");
			}
			this.target = target;
			this.request = request;
			HttpEntity entity = null;
			if (request instanceof HttpEntityEnclosingRequest)
			{
				entity = ((HttpEntityEnclosingRequest) request).getEntity();
			}
			if (entity != null)
			{
				if (entity instanceof ProducingNHttpEntity)
				{
					producer = (ProducingNHttpEntity) entity;
				}
				else
				{
					producer = new NHttpEntityWrapper(entity);
				}
			}
			else
			{
				producer = null;
			}
		}

		@Override
		public HttpRequestBase generateRequest()
		{
			return request;
		}

		@Override
		public HttpHost getTarget()
		{
			return target;
		}

		@Override
		public synchronized void produceContent(final ContentEncoder encoder, final IOControl ioctrl)
				throws IOException
		{
			producer.produceContent(encoder, ioctrl);
			if (encoder.isCompleted())
			{
				producer.finish();
			}
		}

		@Override
		public synchronized boolean isRepeatable()
		{
			return producer.isRepeatable();
		}

		@Override
		public synchronized void resetRequest()
		{
			try
			{
				producer.finish();
			}
			catch (IOException ignore)
			{
			}
		}

		public synchronized void close() throws IOException
		{
			this.producer.finish();
		}
	}

	protected abstract T convertContent(InputStream in, String encoding) throws IOException;
	
	private class AsyncHttpResponseConsumer implements HttpAsyncResponseConsumer<T>
	{
		private volatile String encoding;
		private volatile long length;
		private volatile T result;
		private volatile Exception ex;
		private volatile boolean completed;

		private ByteBuffer buffer;
		
		@Override
		public synchronized void responseReceived(final HttpResponse response) throws IOException
		{
			StatusLine status = response.getStatusLine();
			Logger.Log("HTTP response: " + status, LogLevel.Debug);

			if (status.getStatusCode() != HttpStatus.SC_OK)
			{
				throw new HttpResponseException(status.getStatusCode(), status.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				length = entity.getContentLength();
				if (entity.getContentEncoding() != null)
				{
					encoding = entity.getContentEncoding().getValue();
				}
				else if (entity.getContentType() != null)
				{
					HeaderElement values[] = entity.getContentType().getElements();
					if (values.length > 0)
					{
						NameValuePair param = values[0].getParameterByName("charset");
						if (param != null)
						{
							encoding = param.getValue();
						}
					}
				}
			}
		}

		/**
		 * Notification that content is available to be read from the decoder.
		 * {@link IOControl} instance passed as a parameter to the method can be
		 * used to suspend input events if the entity is temporarily unable to
		 * allocate more storage to accommodate all incoming content.
		 * 
		 * @param decoder
		 *            content decoder.
		 * @param ioctrl
		 *            I/O control of the underlying connection.
		 */
		@Override
		public synchronized void consumeContent(final ContentDecoder decoder, final IOControl ioctrl)
				throws IOException
		{
			int toRead;

			if (buffer == null)
			{
				if (length < 0)
					buffer = ByteBuffer.allocate(8 * 1024);
				else 
					buffer = ByteBuffer.allocate((int)length);
			}
			
			do
			{
				if (length < 0 && buffer.capacity() - buffer.position() < 1024)
				{
					buffer.flip();
					buffer = ByteBuffer.allocate(buffer.capacity() * 2).put(buffer);
				}
				toRead = decoder.read(buffer);
			}
			while (toRead > 0);

			if (progressCb != null)
				progressCb.progress(buffer.position(), length);	

			if (decoder.isCompleted() || toRead < 0)
			{
				if (length < 0 && progressCb != null)
					progressCb.progress(buffer.position(), buffer.position());	

				buffer.flip();
				InputStream in = new ByteArrayInputStream(buffer.array(), 0, buffer.limit());
				try
				{
					result = convertContent(in, encoding);
				}
				finally
				{
					in.close();
				}
			}
		}

		/**
		 * Notification that any resources allocated for reading can be released.
		 */
		@Override
		public synchronized void responseCompleted()
		{
			if (completed)
			{
				return;
			}
			completed = true;
		}

		@Override
		public synchronized void cancel()
		{
			if (completed)
			{
				return;
			}
			completed = true;
			result = null;
		}

		@Override
		public synchronized void failed(final Exception exc)
		{
			if (completed)
			{
				return;
			}
			ex = exc;
			completed = true;
		}

		@Override
		public T getResult()
		{
			return result;
		}

		@Override
		public Exception getException()
		{
			return ex;
		}
	}
}
