/**
 * Copyright (c) 2009-2012, Frederick Martian
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.concurrent.Future;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.nio.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDString;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class RpcClient extends AsyncHTTPClient<OSD>
{
	private static final String METHOD_CALL = "methodCall";
	private static final String METHOD_NAME = "methodName";
	private static final String METHOD_RESPONSE = "methodResponse";
	private static final String PARAMS = "params";
	private static final String PARAM = "param";
	private static final String FAULT = "fault";
	private static final String FAULT_CODE = "faultCode";
	private static final String FAULT_STRING = "faultString";

	private static final String TAG_NAME = "name";
	private static final String TAG_MEMBER = "member";
	private static final String TAG_VALUE = "value";
	private static final String TAG_DATA = "data";

	private static final String TYPE_NIL = "nil";
	private static final String TYPE_INT = "int";
	private static final String TYPE_I4 = "i4";
	private static final String TYPE_I8 = "i8";
	private static final String TYPE_DOUBLE = "double";
	private static final String TYPE_BOOLEAN = "boolean";
	private static final String TYPE_STRING = "string";
	private static final String TYPE_DATE_TIME_ISO8601 = "dateTime.iso8601";
	private static final String TYPE_BASE64 = "base64";
	private static final String TYPE_ARRAY = "array";
	private static final String TYPE_STRUCT = "struct";

	private static final String DATETIME_FORMAT = "yyyyMMdd'T'HH:mm:ss";

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(DATETIME_FORMAT);
	public RpcClient() throws IOReactorException
	{
		super();
	}


	public Future<OSD> call(URI address, String method, OSDArray params, FutureCallback<OSD> callback, long timeout) throws XmlPullParserException
	{
		AbstractHttpEntity entity = new OSDEntity(method, params);
		
		return executeHttpPost(address, entity, null, callback, timeout);
	}
	

	private void methodCall(XmlSerializer serializer, String method, OSDArray params) throws IllegalArgumentException, IllegalStateException, IOException
	{
		serializer.startDocument(null, null);
		serializer.startTag(null, METHOD_CALL);
		// set method name
		serializer.startTag(null, METHOD_NAME).text(method).endTag(null, METHOD_NAME);

		serializeParams(serializer, params);

		serializer.endTag(null, METHOD_CALL);
		serializer.endDocument();
	}

	private void serializeParams(XmlSerializer serializer, OSDArray params) throws IllegalArgumentException, IllegalStateException, IOException
	{
		if (params != null && params.size() != 0)
		{
			// set method params
			serializer.startTag(null, PARAMS);
			for (int i = 0; i < params.size(); i++)
			{
				serializer.startTag(null, PARAM).startTag(null, TAG_VALUE);
				serialize(serializer, params.get(i));
				serializer.endTag(null, TAG_VALUE).endTag(null, PARAM);
			}
			serializer.endTag(null, PARAMS);
		}
	}

	private class OSDEntity extends AbstractHttpEntity
	{
		private byte[] bytes;
		private OSDArray params;
		private String method;
		private XmlSerializer serializer;

		
		public OSDEntity(String method, OSDArray params) throws XmlPullParserException
		{
	        super();
	        bytes = null;
			this.method = method;
			this.params = params;
			serializer = XmlPullParserFactory.newInstance().newSerializer();
			setContentType("text/xml");
		}

		@Override
		public boolean isRepeatable()
		{
			return true;
		}

		@Override
		public long getContentLength()
		{
			return -1;
		}

		@Override
		public InputStream getContent() throws IOException, IllegalStateException
		{
			if (bytes == null)
			{
				ByteArrayOutputStream outstream = new ByteArrayOutputStream();
				serializer.setOutput(outstream, getContentEncoding().getValue());
				methodCall(serializer, method, params);
				bytes = outstream.toByteArray();
			}
			return new ByteArrayInputStream(bytes);
		}

		@Override
		public void writeTo(OutputStream outstream) throws IOException
		{
	        if (outstream == null)
	        {
	            throw new IllegalArgumentException("Output stream may not be null");
	        }
			serializer.setOutput(outstream, getContentEncoding().getValue());
			methodCall(serializer, method, params);
		}

		@Override
		public boolean isStreaming()
		{
			return false;
		}
	}

	@Override
	protected OSD convertContent(InputStreamReader inStream) throws IOException
	{
		try
		{
			// parse response stuff
			//
			// setup pull parser
			XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();
			pullParser.setInput(inStream);

			// lets start pulling...
			pullParser.nextTag();
			pullParser.require(XmlPullParser.START_TAG, null, METHOD_RESPONSE);

			pullParser.nextTag(); // either Tag.PARAMS (<params>) or
									// Tag.FAULT (<fault>)
			String tag = pullParser.getName();
			if (tag.equals(PARAMS))
			{
				// normal response
				pullParser.nextTag(); // Tag.PARAM (<param>)
				pullParser.require(XmlPullParser.START_TAG, null, PARAM);
				pullParser.nextTag(); // Tag.VALUE (<value>)
				// no parser.require() here since its called in
				// XMLRPCSerializer.deserialize() below

				// deserialize result
				return deserialize(pullParser);
			}
			else if (tag.equals(FAULT))
			{
				// fault response
				pullParser.nextTag(); // Tag.VALUE (<value>)
				// no parser.require() here since its called in
				// XMLRPCSerializer.deserialize() below

				// deserialize fault result
				return deserialize(pullParser);
			}
			else
			{
				throw new IOException("Bad tag <" + tag + "> in XMLRPC response - neither <params> nor <fault>");
			}
		}
		catch (XmlPullParserException ex)
		{
			throw new IOException("PullParserExeception in XMLRPC response: " + ex.getMessage());			
		}
	}
	

	private void serialize(XmlSerializer serializer, OSD data) throws IOException
	{
		// check for scalar types:
		switch (data.getType())
		{
		    case Unknown:
				serializer.startTag(null, TYPE_NIL).endTag(null, TYPE_NIL);
			    break;
		    case Boolean:
				serializer.startTag(null, TYPE_BOOLEAN).text(data.AsString()).endTag(null, TYPE_BOOLEAN);
		    	break;
			case Integer:
				serializer.startTag(null, TYPE_I4).text(data.AsString()).endTag(null, TYPE_I4);
				break;
//			case Long:
//			    serializer.startTag(null, TYPE_I8).text(data.AsString()).endTag(null, TYPE_I8);
//				break;
			case Real:
			    serializer.startTag(null, TYPE_DOUBLE).text(data.AsString()).endTag(null, TYPE_DOUBLE);
			    break;
			case String:
			case URI:
			case UUID:
				serializer.startTag(null, TYPE_STRING).text(data.AsString()).endTag(null, TYPE_STRING);
			case Date:
				String dateStr = dateFormat.format(data.AsDate());
				serializer.startTag(null, TYPE_DATE_TIME_ISO8601).text(dateStr).endTag(null, TYPE_DATE_TIME_ISO8601);
			case Binary:
				String value = new String(Base64.encodeBase64(data.AsBinary()));
				serializer.startTag(null, TYPE_BASE64).text(value).endTag(null, TYPE_BASE64);
			case Array:
				serializer.startTag(null, TYPE_ARRAY).startTag(null, TAG_DATA);
				OSDArray array = (OSDArray)data;
				for (int i = 0; i < array.size(); i++)
				{
					serializer.startTag(null, TAG_VALUE);
					serialize(serializer, array.get(i));
					serializer.endTag(null, TAG_VALUE);
				}
				serializer.endTag(null, TAG_DATA).endTag(null, TYPE_ARRAY);
			case Map:
				serializer.startTag(null, TYPE_STRUCT);
				OSDMap map = (OSDMap) data;
				Iterator<String> iter = map.keySet().iterator();
				while (iter.hasNext())
				{
					String key = iter.next();
					serializer.startTag(null, TAG_MEMBER);
					serializer.startTag(null, TAG_NAME).text(key).endTag(null, TAG_NAME);
					serializer.startTag(null, TAG_VALUE);
					serialize(serializer, map.get(key));
					serializer.endTag(null, TAG_VALUE);
					serializer.endTag(null, TAG_MEMBER);
				}
				serializer.endTag(null, TYPE_STRUCT);
		}
	}

	private OSD deserialize(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		parser.require(XmlPullParser.START_TAG, null, TAG_VALUE);

		if (parser.isEmptyElementTag())
		{
			// degenerated <value />, return empty string
			return new OSDString("");
		}

		OSD ret = null;
		try
		{
			parser.nextTag();
			String value = null, name = parser.getName();
			if (name.equals(TAG_VALUE) && parser.getEventType() == XmlPullParser.END_TAG)
			{
				// empty <value></value>, return empty string
				return new OSDString("");
			}
			else if (name.equals(TYPE_NIL))
			{
				return new OSD();
			}
			
			boolean notEmpty = !parser.isEmptyElementTag();
			if (name.equals(TYPE_INT) || name.equals(TYPE_I4))
			{
				int num = 0;
				if (notEmpty)
				{
					num = Helpers.TryParseInt(parser.nextText());
				}
				ret = OSD.FromInteger(num);
			}
			else if (name.equals(TYPE_I8))
			{
				long num = 0;
				if (notEmpty)
				{
					num = Helpers.TryParseLong(parser.nextText());
				}
				ret = OSD.FromLong(num);
			}
			else if (name.equals(TYPE_DOUBLE))
			{
				double num = 0d;
				if (notEmpty)
				{
					num = Helpers.TryParseDouble(parser.nextText());
				}
				ret = OSD.FromReal(num);
			}
			else if (name.equals(TYPE_BOOLEAN))
			{
				boolean bool = false;
				if (notEmpty)
				{
					value = parser.nextText().trim();
					bool = (value != null && !value.isEmpty() && value.equals("1"));
				}
				ret = OSD.FromBoolean(bool);
			}
			else if (name.equals(TYPE_STRING))
			{
				ret = parseString(parser.nextText());
			}
			else if (name.equals(TYPE_DATE_TIME_ISO8601))
			{
				value = parser.nextText();
				try
				{
					ret = OSD.FromDate(dateFormat.parse(value));
				}
				catch (ParseException e)
				{
					throw new IOException("Cannot deserialize dateTime " + value);
				}
			}
			else if (name.equals(TYPE_BASE64))
			{
				byte[] data = Helpers.EmptyBytes;
				if (notEmpty)
				{
					data = Base64.decodeBase64(parser.nextText());
				}
				ret = OSD.FromBinary(data);
			}
			else if (name.equals(TYPE_ARRAY))
			{
				parser.nextTag(); // TAG_DATA (<data>)
				parser.require(XmlPullParser.START_TAG, null, TAG_DATA);

				parser.nextTag();
				OSDArray list = new OSDArray();
				while (parser.getName().equals(TAG_VALUE))
				{
					list.add(deserialize(parser));
					parser.nextTag();
				}
				parser.require(XmlPullParser.END_TAG, null, TAG_DATA);
				parser.nextTag(); // TAG_ARRAY (</array>)
				parser.require(XmlPullParser.END_TAG, null, TYPE_ARRAY);
				ret = list;
			}
			else if (name.equals(TYPE_STRUCT))
			{
				parser.nextTag();
				OSDMap map = new OSDMap();
				while (parser.getName().equals(TAG_MEMBER))
				{
					String memberName = null;
					OSD memberValue = null;
					while (true)
					{
						parser.nextTag();
						String key = parser.getName();
						if (key.equals(TAG_NAME))
						{
							memberName = parser.nextText();
						}
						else if (name.equals(TAG_VALUE))
						{
							memberValue = deserialize(parser);
						}
						else
						{
							break;
						}
					}
					if (memberName != null && memberValue != null)
					{
						map.put(memberName, memberValue);
					}
					parser.require(XmlPullParser.END_TAG, null, TAG_MEMBER);
					parser.nextTag();
				}
				parser.require(XmlPullParser.END_TAG, null, TYPE_STRUCT);
				ret = map;
			}
			else
			{
				throw new IOException("Cannot deserialize " + name);
			}
		}
		catch (XmlPullParserException e)
		{
			// TYPE_STRING (<string>) is not required
			ret = new OSDString(parser.getText());
		}
		parser.nextTag(); // TAG_VALUE (</value>)
		parser.require(XmlPullParser.END_TAG, null, TAG_VALUE);
		return ret;
	}
	
	private static OSD parseString(String string)
	{
		if (string.length() >=36)
		{
			try
			{
				UUID uuid = new UUID();
				if (uuid.FromString(string))
				    return OSD.FromUUID(uuid);
			}
			catch (Exception ex) {}
		}
		return OSD.FromString(string);
	}
}
