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
package libomv.Gui.windows;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import libomv.GridClient;
import libomv.LibSettings;
import libomv.Gui.components.list.FriendList;
import libomv.Gui.components.list.GroupList;

public interface MainControl extends ActionListener
{
	public static final String cmdOnline = "online";
	public static final String cmdLogout = "logout";
	public static final String cmdQuit = "quit";
	public static final String cmdAbout = "about";
	public static final String cmdBugs = "bugs";
	public static final String cmdUpdates = "Updates";
	public static final String cmdDebugCon = "debugCon";
	public static final String cmdSettings = "settings";
	
	public void setAction(AbstractButton comp, ActionListener actionListener, String actionCommand);
	public void setAction(JComboBox comp, ActionListener actionListener, String actionCommand);
	public JMenuItem newMenuItem(String label, ActionListener actionListener, String actionCommand);
	public void setMenuBar(JMenuBar menuBar);
	public JFrame getMainJFrame();
	public LibSettings getAppSettings();
	public GridClient getGridClient();
	public CommWindow getCommWindow();
	public FriendList getFriendList();
	public GroupList getGroupList();
	public void setContentArea(Component pane);
}
