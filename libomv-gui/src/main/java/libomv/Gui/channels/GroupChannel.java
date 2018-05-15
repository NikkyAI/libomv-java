/**
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
package libomv.Gui.channels;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import libomv.Gui.windows.MainControl;
import libomv.io.GridClient;
import libomv.io.AgentManager.ChatType;
import libomv.types.UUID;

public class GroupChannel extends AbstractChannel
{
	private static final Logger logger = Logger.getLogger(GroupChannel.class);
	private static final long serialVersionUID = 1L;

	private JScrollPane jScrpAttendents; 

	/**
	 * This is the default constructor
	 */
	public GroupChannel(MainControl main, String name, UUID id, UUID session)
	{
		super(main, name, id, session);
		
		JPanel panelNorth = new JPanel();
		panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.X_AXIS));
		
		JTextField textField = new JTextField();
		textField.setColumns(10);
		panelNorth.add(textField);

		JToggleButton jtbExpandAttendents = new JToggleButton("Show Attendents");
		jtbExpandAttendents.setHorizontalAlignment(SwingConstants.RIGHT);
		jtbExpandAttendents.setSelected(false);
		getJScrpAttendents().setVisible(false);
		jtbExpandAttendents.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				getJScrpAttendents().setVisible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
	
		panelNorth.add(jtbExpandAttendents);
		add(panelNorth, BorderLayout.NORTH);

		add(getJScrpAttendents(), BorderLayout.EAST);
		
    	try
    	{
    		if (!_Client.Self.GroupChatSessions.containsKey(session))
    		{
				_Client.Self.ChatterBoxAcceptInvite(session);
    		}
		}
    	catch (Exception ex)
    	{
			logger.error(GridClient.Log("Failed to join group chat", _Client), ex);
		}
	}

	/**
	 * Receive a message.
	 * 
	 * @param message The message received.
	 * @throws BadLocationException 
	 */
	@Override
	public void receiveMessage(Date timestamp, UUID fromId, String fromName, String message, String style) throws BadLocationException
	{
		// Determine if this is a friend...
		boolean friend = _Client.Friends.getFriendList().containsKey(fromId);
		String localStyle = style, localMessage = message;
		
		// If this is an action message.
		if (message.startsWith("/me "))
		{
			localStyle = STYLE_ACTION;
			// Remove the "/me".
			localMessage = message.substring(3);
		}
		else if (style == null)
		{
			localStyle = STYLE_REGULAR;
			// This is a normal message.
			localMessage = ": " + message;
		}
		addMessage(new ChatItem(timestamp, fromName, friend ? STYLE_CHATREMOTEFRIEND : STYLE_CHATREMOTE, localMessage, localStyle));
	}

	@Override
	public void receiveStatus(UUID sourceID, final String message, final String style) throws BadLocationException
	{
		
	}

	@Override
	public void transmitMessage(String message, ChatType type) throws UnsupportedEncodingException, Exception
	{
		String localMessage = message, self = _Client.Self.getName();

		if (message.length() >= 1000)
        {
        	localMessage = message.substring(0, 1000);
        }
		addHistory(localMessage);	
		
		// Do we have an action command?
		if (localMessage.charAt(0) == '/')
		{
			String firstWord = "";
			try
			{
				firstWord = localMessage.split("\\s")[0].toLowerCase();
			}
			catch (Exception ex) { }
			localMessage = localMessage.substring(firstWord.length()).trim();

			// Deal with actions.
			if (firstWord.equals("/me"))
			{
				addMessage(new ChatItem(self, STYLE_CHATLOCAL, localMessage, STYLE_ACTION));
			}
		}
		else
		{
			// Normal
			addMessage(new ChatItem(self, STYLE_CHATLOCAL, ": " + localMessage, STYLE_REGULAR));
		}
		// Indicate that we're no longer typing.
		super.transmitMessage(localMessage, type);

		// Send the message.
		_Client.Self.InstantMessageGroup(getUUID(), localMessage);
	}
				
	protected void triggerTyping(boolean start) throws Exception
	{
		_Client.Self.SendTypingState(getUUID(), getSession(), start);		
	}

	private JScrollPane getJScrpAttendents()
	{
		if (jScrpAttendents == null)
		{
			jScrpAttendents = new JScrollPane();
			add(jScrpAttendents, BorderLayout.EAST);

			JList<String> listAttendents = new JList<String>();
			listAttendents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jScrpAttendents.setViewportView(listAttendents);
		}
		return jScrpAttendents;
	}
}
