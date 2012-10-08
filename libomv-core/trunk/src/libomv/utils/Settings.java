/**
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
 * - Neither the name of the libomv-java project nor the names of its
 *   contributors may be used to endorse or promote products derived from
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
package libomv.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.ParseException;
import java.util.Map.Entry;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;

public class Settings
{
	public class DefaultSetting
	{
		String key;
		Object value;
		
		public DefaultSetting(String key, Object value)
		{
			this.key = key;
			this.value = value;
		}
	}
	
	public class SettingsUpdateCallbackArgs implements CallbackArgs
	{
		private String name;
		private OSD value;
		
		public String getName()
		{
			return name;
		}
		
		public OSD getValue()
		{
			return value;
		}
		
		public SettingsUpdateCallbackArgs(String name, OSD value)
		{
			this.name = name;
			this.value = value;
		}
	}
	
	private File settingsPath;
	private OSDMap settings;
	private OSDMap defaults;
	
	public CallbackHandler<SettingsUpdateCallbackArgs> OnSettingsUpdate = new CallbackHandler<SettingsUpdateCallbackArgs>();
		
	public Settings(String settingsPath)
	{
		this.settingsPath = new File(System.getProperty("user.home"), settingsPath);
		settings = new OSDMap();
	}
	
	protected void setDefaults(DefaultSetting[] defaults)
	{
		if (defaults != null)
		{
			if (this.defaults == null)
				this.defaults = new OSDMap();
			
			for (DefaultSetting setting : defaults)
			{
				this.defaults.put(setting.key, OSD.FromObject(setting.value));
				this.settings.put(setting.key, OSD.FromObject(setting.value));
			}
		}
	}
	
	protected void load() throws IOException, ParseException
    {
		try
		{
			Reader reader = new FileReader(settingsPath);
			try
			{
				for (Entry<String, OSD> entry : ((OSDMap)OSDParser.deserialize(reader)).entrySet())
				{
					settings.put(entry.getKey(), entry.getValue());
				}
				OnSettingsUpdate.dispatch(new SettingsUpdateCallbackArgs(null, null));
			}
			finally
			{
				reader.close();
			}
		}
		catch (FileNotFoundException ex)
		{
			// Catch FileNotFoundException and ignore as this happens whenever we startup without a settings file
		}
    }
    
	public void save() throws IOException
	{
		OSDMap temp = null;
		if (defaults != null)
		{
			temp = new OSDMap();
			for (Entry<String, OSD> entry : defaults.entrySet())
			{
				if (!settings.get(entry.getKey()).equals(entry.getValue()))
				{
					temp.put(entry);
				}
			}
		}
		else
		{
			temp = settings;
		}
		
		if (temp.size() > 0)
		{
			Writer writer = new FileWriter(settingsPath);
			try
			{
				OSDParser.serialize(writer, temp, OSDFormat.Notation);
			}
			finally
			{
				writer.close();
			}
		}
	}
	
	public OSD get(String name)
	{
		OSD osd = settings.get(name);
		if (osd == null && defaults != null)
			osd = defaults.get(name);
		return osd;
	}

	public boolean getBool(String name)
	{
		return get(name).AsBoolean();
	}

	public int getInt(String name)
	{
		return get(name).AsInteger();
	}

	public String getString(String name)
	{
		return get(name).AsString();
	}

	public boolean get(String name, boolean defValue)
	{
		OSD osd = get(name);
		if (osd != null)
			return osd.AsBoolean();
		return defValue;
	}

	public int get(String name, int defValue)
	{
		OSD osd = get(name);
		if (osd != null)
			return osd.AsInteger();
		return defValue;
	}

	public String get(String name, String defValue)
	{
		OSD osd = get(name);
		if (osd != null)
			return osd.AsString();
		return defValue;
	}

	public OSD get(String name, OSD defValue)
	{
		OSD osd = get(name);
		if (osd != null)
			return osd;
		return defValue;
	}
	
	public boolean putDefault(String name, boolean value)
	{
		return putDefault(name, OSD.FromBoolean(value)).AsBoolean();
	}

	public int putDefault(String name, int value)
	{
		return putDefault(name, OSD.FromInteger(value)).AsInteger();
	}

	public String putDefault(String name, String value)
	{
		return putDefault(name, OSD.FromString(value)).AsString();
	}

	public OSD putDefault(String name, Object value)
	{
		return putDefault(name, OSD.FromObject(value));
	}

	private OSD putDefault(String name, OSD value)
	{
		if (defaults == null)
			defaults = new OSDMap();
		
		OSD osd = defaults.put(name, value);
		return osd;
	}

	public boolean put(String name, boolean value)
	{
		return put(name, OSD.FromBoolean(value)).AsBoolean();
	}

	public int put(String name, int value)
	{
		return put(name, OSD.FromInteger(value)).AsInteger();
	}

	public String put(String name, String value)
	{
		return put(name, OSD.FromString(value)).AsString();
	}

	public OSD put(String name, Object value)
	{
		return put(name, OSD.FromObject(value));
	}

	private OSD put(String name, OSD value)
	{
		OSD osd = settings.put(name, value);
		OnSettingsUpdate.dispatch(new SettingsUpdateCallbackArgs(name, value));
		return osd;
	}
}
