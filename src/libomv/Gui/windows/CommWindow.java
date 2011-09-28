/**
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
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
package libomv.Gui.windows;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;

import libomv.GridClient;
import libomv.Gui.channels.AbstractChannel;
import libomv.Gui.channels.LocalChannel;
import libomv.Gui.components.list.FriendList;
import libomv.Gui.components.list.GroupList;

public class CommWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	private JTabbedPane jTpComm;
	private JTabbedPane jTpContacts;
	
	private GridClient _Client;
	
	public CommWindow(GridClient client)
	{
		super();
		
		_Client = client;
		
		setTitle("Communication");
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel jPnl = new JPanel();
		jPnl.add(getJTpComm());
		setContentPane(jPnl);				
	}
	
	private JTabbedPane getJTpComm()
	{
		if (jTpComm == null)
		{
			jTpComm = new JTabbedPane(JTabbedPane.BOTTOM);
			jTpComm.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			jTpComm.setBorder(new LineBorder(new Color(0, 0, 0)));

			jTpComm.add("Contacts", getJTpContacts());
			jTpComm.add("Local Chat", new LocalChannel(_Client));
		}
		return jTpComm;
	}
	
	private JTabbedPane getJTpContacts()
	{
		if (jTpContacts == null)
		{
			jTpContacts = new JTabbedPane(JTabbedPane.TOP);
			jTpContacts.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			jTpContacts.setBorder(new LineBorder(new Color(0, 0, 0)));

			jTpContacts.add("Friends", new FriendList(_Client));
			jTpContacts.add("Groups", new GroupList(_Client));
		}
		return jTpContacts;
	}

	public void addChannel(AbstractChannel channel)
	{
		getJTpComm().add(channel.getName(), channel);
	}
	
	public void removeChannel(AbstractChannel channel)
	{
		getJTpComm().remove(channel);
	}
}
