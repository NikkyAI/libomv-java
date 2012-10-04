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
	
	public CallbackHandler<SettingsUpdateCallbackArgs> OnSettingsUpdate = new CallbackHandler<SettingsUpdateCallbackArgs>();
		
	public Settings(String settingsPath)
	{
		this.settingsPath = new File(System.getProperty("user.home"), settingsPath);
		settings = new OSDMap();
	}
	
	public void setDefaults(DefaultSetting[] defaults)
	{
		for (DefaultSetting setting : defaults)
		{
			settings.put(setting.key, OSD.FromObject(setting.value));
		}
		
	}
	
    public void load() throws IOException, ParseException
    {
		Reader reader = new FileReader(settingsPath);
		try
		{
			for (Entry<String, OSD> entry : ((OSDMap)OSDParser.deserialize(reader)).entrySet())
			{
				settings.put(entry.getKey(), entry.getValue());
			}
		}
		finally
		{
				reader.close();
		}
    }
    
	public void save() throws IOException
	{
		Writer writer = new FileWriter(settingsPath);
		try
		{
			OSDParser.serialize(writer, settings, OSDFormat.Notation);
		}
		finally
		{
			writer.close();
		}
	}
	
	public OSD get(String name)
	{
		return settings.get(name);
	}

	public OSD get(String name, OSD defValue)
	{
		if (settings.containsKey(name))
			return settings.get(name);
		return defValue;
	}
	
	public boolean getBool(String name, boolean defValue)
	{
		if (settings.containsKey(name))
			return settings.get(name).AsBoolean();
		return defValue;
	}

	public int getInt(String name, int defValue)
	{
		if (settings.containsKey(name))
			return settings.get(name).AsInteger();
		return defValue;
	}

	public String getString(String name, String defValue)
	{
		if (settings.containsKey(name))
			return settings.get(name).AsString();
		return defValue;
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
