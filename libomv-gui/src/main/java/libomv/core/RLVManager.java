/**
 * Copyright (c) 2009-2011, Radegast Development Team
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
package libomv.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import libomv.Gui.AppSettings;
import libomv.Gui.windows.MainControl;
import libomv.model.agent.ChatCallbackArgs;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Settings.SettingsUpdateCallbackArgs;

public class RLVManager {
	public class RLVRule {
		public String Behaviour;
		public String Option;
		public String Param;
		public UUID Sender;
		public String SenderName;

		public String SoString() {
			return String.format("%s: %s:%s=%s [%s]", SenderName, Behaviour, Option, Param, Sender);
		}
	}

	private MainControl _Main;

	private List<RLVRule> rules = new ArrayList<RLVRule>();

	Pattern pattern = Pattern.compile("");
	/*
	 * syntax:
	 *
	 * <behaviour>[:<option>]=<param>
	 */
	private Pattern rlv_regex = Pattern.compile("([^:=]+)(:([^=]+))?=(\\w+)");

	private Callback<SettingsUpdateCallbackArgs> settingsUpdate = new SettingsUpdateCallback();

	private class SettingsUpdateCallback implements Callback<SettingsUpdateCallbackArgs> {
		@Override
		public boolean callback(SettingsUpdateCallbackArgs params) {
			if (params.getName() == null) {
				enabled = _Main.getAppSettings().getBool(AppSettings.ENABLE_RLV_MANAGER);
			} else {
				enabled = params.getValue().asBoolean();
			}
			return false;
		}
	}

	public RLVManager(MainControl main) {
		_Main = main;

		_Main.getAppSettings().onSettingsUpdate.add(settingsUpdate);
		enabled = _Main.getAppSettings().get(AppSettings.ENABLE_RLV_MANAGER, false);
	}

	protected void finalized() throws Throwable {
		_Main.getAppSettings().onSettingsUpdate.remove(settingsUpdate);
		super.finalize();
	}

	private boolean enabled;

	public boolean isEnabled() {
		return enabled;
	}

	public boolean restrictionActive(String name, UUID value) {
		if (isEnabled()) {

		}
		return false;
	}

	public boolean restrictionActive(String name, String value) {
		if (isEnabled()) {

		}
		return false;
	}

	public boolean tryProcessCommand(ChatCallbackArgs cmd) {
		if (isEnabled()) {

		}
		return false;
	}

	public boolean autoAcceptTP(UUID fromID) {
		if (isEnabled()) {

		}
		return false;
	}
}
