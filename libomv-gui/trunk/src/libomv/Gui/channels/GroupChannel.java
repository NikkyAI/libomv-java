/**
 * Copyright (c) 2009-2012, Frederick Martian
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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.BoxLayout;
import javax.swing.text.BadLocationException;

import libomv.AgentManager.ChatType;
import libomv.Gui.windows.MainControl;
import libomv.types.UUID;

public class GroupChannel extends AbstractChannel
{
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
		if(message == null || message.isEmpty())
			return;
		
		// Determine if this is a friend...
		boolean friend = _Main.getGridClient().Friends.getFriendList().containsKey(fromId);
		
		// If this is an action message.
		if(message.startsWith("/me "))
		{
			// Remove the "/me ".
			addMessage(new ChatItem(timestamp, true, fromName, friend ? STYLE_CHATREMOTEFRIEND : STYLE_CHATREMOTE, message.substring(4), STYLE_ACTION));
		}
		else
		{
			// This is a normal message.
			addMessage(new ChatItem(timestamp, false, fromName, friend ? STYLE_CHATREMOTEFRIEND : STYLE_CHATREMOTE, message, STYLE_REGULAR));
		}
	}

	@Override
	public void transmitMessage(String message, ChatType type) throws UnsupportedEncodingException, Exception
	{
        if (message == null || message.trim().isEmpty())
        	return;

		String self = _Main.getGridClient().Self.getName();
		addHistory(message);	

		// Do we have an action command?
		if(message.charAt(0) == '/')
		{
			String firstWord = "";
			try
			{
				firstWord = message.split("\\s")[0].toLowerCase();
			}
			catch(Exception ex) { }
			String localMessage = message.substring(firstWord.length()).trim();

			// Deal with actions.
			if(firstWord.equals("/me"))
			{
				addMessage(new ChatItem(true, self, STYLE_CHATLOCAL, localMessage, STYLE_ACTION));
			}
		}
		else
		{
			// Normal
			addMessage(new ChatItem(false, self, STYLE_CHATLOCAL, message, STYLE_REGULAR));
		}
		// Indicate that we're no longer typing.
		super.transmitMessage(message, type);

		// Send the message.
		_Main.getGridClient().Self.InstantMessageGroup(getUUID(), message);
	}
				
	protected void triggerTyping(boolean start) throws Exception
	{
		_Main.getGridClient().Self.SendTypingState(getUUID(), getSession(), start);		
	}

	private JScrollPane getJScrpAttendents()
	{
		if (jScrpAttendents == null)
		{
			jScrpAttendents = new JScrollPane();
			add(jScrpAttendents, BorderLayout.EAST);

			JList listAttendents = new JList();
			listAttendents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jScrpAttendents.setViewportView(listAttendents);
		}
		return jScrpAttendents;
	}
}
