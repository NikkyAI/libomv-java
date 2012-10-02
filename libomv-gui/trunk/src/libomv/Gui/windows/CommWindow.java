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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;

import libomv.AgentManager.ChatAudibleLevel;
import libomv.AgentManager.ChatSourceType;
import libomv.AgentManager.ChatType;
import libomv.AgentManager.InstantMessage;
import libomv.AgentManager.InstantMessageOnline;
import libomv.Gui.channels.AbstractChannel;
import libomv.Gui.channels.GroupChannel;
import libomv.Gui.channels.IMChannel;
import libomv.Gui.channels.LocalChannel;
import libomv.Gui.components.OnlinePanel;
import libomv.types.UUID;

public class CommWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	private JTabbedPane jTpComm;
	private JTabbedPane jTpContacts;
	
	private MainControl _Main;
	private LocalChannel localChat;
	private HashMap<UUID, IMChannel> imChannels;
	private HashMap<UUID, GroupChannel> groupChannels;
	
	public CommWindow(MainControl main)
	{
		super();
		
		_Main = main;
		
		setTitle("Communication");
		
		// Choose a sensible minimum size.
		setPreferredSize(new Dimension(280, 400));
		getContentPane().setLayout(new BorderLayout(0, 0));

		getContentPane().add(getJTpComm());

		//Display the window.
        pack();
        setVisible(true);
	}
	
	public void setFocus(String focus)
	{
		if (focus.equals("chat"))
		{
			getJTpComm().setSelectedIndex(1);
		}
		else
		{
			getJTpComm().setSelectedIndex(0);
			if (focus.equals(OnlinePanel.cmdFriends))
			{
				getJTpContacts().setSelectedIndex(0);
			}
			else if (focus.equals(OnlinePanel.cmdGroups))
			{
				getJTpContacts().setSelectedIndex(1);
			}
		}
	}
	
	public void printAlertMessage(String alertMessage)
	{
        if (alertMessage.toLowerCase().contains("autopilot canceled"))
        	return; //workaround the stupid autopilot alerts

        getLocalChannel().receiveMessage(null, null, "Alert message", alertMessage, true);
	}
	
	public void printMessage(ChatSourceType source, String from, String message, ChatAudibleLevel level, ChatType type)
	{
        getLocalChannel().receiveMessage(null, null, from, message, false);		
	}
	
	public void printInstantMessage(InstantMessage message)
	{
		AbstractChannel channel;
		if (message.GroupIM)
		{
			channel = groupChannels.get(message.FromAgentID);
			if (channel == null)
			{
				channel = new GroupChannel(_Main, message.FromAgentName, message.FromAgentID, message.IMSessionID);
				getJTpComm().add(message.FromAgentName, channel);
			}
		}
		else
		{
			channel = imChannels.get(message.FromAgentID);
			if (channel == null)
			{
				channel = new IMChannel(_Main, message.FromAgentName, message.FromAgentID, message.IMSessionID);
				getJTpComm().add(message.FromAgentName, channel);
			}
		}
		channel.receiveMessage(message.Timestamp, message.FromAgentID, message.FromAgentName, message.Message, message.Offline == InstantMessageOnline.Offline);
	}
	
	private LocalChannel getLocalChannel()
	{
		if (localChat == null)
		{
			localChat = new LocalChannel(_Main);
		}
		return localChat;
	}
	
	private JTabbedPane getJTpComm()
	{
		if (jTpComm == null)
		{
			jTpComm = new JTabbedPane(JTabbedPane.BOTTOM);
			jTpComm.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			jTpComm.setBorder(new LineBorder(new Color(0, 0, 0)));

			jTpComm.add("Contacts", getJTpContacts());
			jTpComm.add("Local Chat", getLocalChannel());
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

			jTpContacts.add("Friends", _Main.getFriendList());
			jTpContacts.add("Groups", _Main.getGroupList());
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
