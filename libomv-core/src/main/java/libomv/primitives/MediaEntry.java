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
package libomv.primitives;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;

public class MediaEntry {
	// #region enums
	// Permissions for control of object media
	// [Flags]
	public static class MediaPermission {
		public static final byte None = 0;
		public static final byte Owner = 1;
		public static final byte Group = 2;
		public static final byte Anyone = 4;
		public static final byte All = Owner | Group | Anyone;

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value) {
			return (byte) (value & _mask);
		}

		private static final byte _mask = All;
	}

	// Style of cotrols that shold be displayed to the user
	public enum MediaControls {
		Standard, Mini;

		public byte getValue() {
			return (byte) ordinal();
		}

		public static MediaControls setValue(int value) {
			if (value > 0 && value < values().length)
				return values()[value];
			return null;
		}

	}

	// #endregion enums

	// Is display of the alternative image enabled
	public boolean enableAlterntiveImage;

	// Should media auto loop
	public boolean autoLoop;

	// Shoule media be auto played
	public boolean autoPlay;

	// Auto scale media to prim face
	public boolean autoScale;

	// Should viewer automatically zoom in on the face when clicked
	public boolean autoZoom;

	// Should viewer interpret first click as interaction with the media
	// or when false should the first click be treated as zoom in command
	public boolean interactOnFirstClick;

	// Style of controls viewer should display when viewer media on this face
	public MediaControls controls;

	// Starting URL for the media
	public String homeURL;

	// Currently navigated URL
	public String currentURL;

	// Media height in pixes
	public int height;

	// Media width in pixels
	public int width;

	// Who can controls the media, flags MediaPermission
	public byte controlPermissions;

	// Who can interact with the media, flags MediaPermission
	public byte interactPermissions;

	// Is URL whitelist enabled
	public boolean enableWhiteList;

	// Array of URLs that are whitelisted
	public String[] whiteList;

	public MediaEntry() {

	}

	public MediaEntry(OSD osd) {
		fromOSD(osd);
	}

	/**
	 * Serialize to OSD
	 *
	 * @return OSDMap with the serialized data
	 */
	public OSDMap serialize() {
		OSDMap map = new OSDMap();

		map.put("alt_image_enable", OSD.FromBoolean(enableAlterntiveImage));
		map.put("auto_loop", OSD.FromBoolean(autoLoop));
		map.put("auto_play", OSD.FromBoolean(autoPlay));
		map.put("auto_scale", OSD.FromBoolean(autoScale));
		map.put("auto_zoom", OSD.FromBoolean(autoZoom));
		map.put("controls", OSD.FromInteger(controls.getValue()));
		map.put("current_url", OSD.FromString(currentURL));
		map.put("first_click_interact", OSD.FromBoolean(interactOnFirstClick));
		map.put("height_pixels", OSD.FromInteger(height));
		map.put("home_url", OSD.FromString(homeURL));
		map.put("perms_control", OSD.FromInteger(controlPermissions));
		map.put("perms_interact", OSD.FromInteger(interactPermissions));

		OSDArray wl = new OSDArray();
		if (whiteList != null && whiteList.length > 0) {
			for (int i = 0; i < whiteList.length; i++)
				wl.add(OSD.FromString(whiteList[i]));
		}

		map.put("whitelist", wl);
		map.put("whitelist_enable", OSD.FromBoolean(enableWhiteList));
		map.put("width_pixels", OSD.FromInteger(width));

		return map;
	}

	/**
	 * Deserialize from OSD data
	 *
	 * @param osd
	 *            Serialized OSD data
	 * @return Deserialized object
	 */
	public void fromOSD(OSD osd) {
		if (osd instanceof OSDMap) {
			OSDMap map = (OSDMap) osd;

			enableAlterntiveImage = map.get("alt_image_enable").AsBoolean();
			autoLoop = map.get("auto_loop").AsBoolean();
			autoPlay = map.get("auto_play").AsBoolean();
			autoScale = map.get("auto_scale").AsBoolean();
			autoZoom = map.get("auto_zoom").AsBoolean();
			controls = MediaControls.setValue(map.get("controls").AsInteger());
			currentURL = map.get("current_url").AsString();
			interactOnFirstClick = map.get("first_click_interact").AsBoolean();
			height = map.get("height_pixels").AsInteger();
			homeURL = map.get("home_url").AsString();
			controlPermissions = MediaPermission.setValue(map.get("perms_control").AsInteger());
			interactPermissions = MediaPermission.setValue(map.get("perms_interact").AsInteger());

			if (map.get("whitelist").getType() == OSDType.Array) {
				OSDArray wl = (OSDArray) map.get("whitelist");
				if (wl.size() > 0) {
					whiteList = new String[wl.size()];
					for (int i = 0; i < wl.size(); i++) {
						whiteList[i] = wl.get(i).AsString();
					}
				}
			}
			enableWhiteList = map.get("whitelist_enable").AsBoolean();
			width = map.get("width_pixels").AsInteger();
		}
	}
}
