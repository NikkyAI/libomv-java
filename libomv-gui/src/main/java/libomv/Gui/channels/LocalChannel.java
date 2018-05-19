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

import libomv.Gui.windows.MainControl;
import libomv.model.agent.ChatType;
import libomv.types.UUID;

public class LocalChannel extends AbstractChannel {
	private static final long serialVersionUID = 1L;

	private boolean printLocalMessage = false;

	private JScrollPane jScrpAttendents;
	private int chatChannel = 0;

	/**
	 * This is the default constructor
	 */
	public LocalChannel(MainControl main) {
		super(main, "Local Chat", UUID.Zero, UUID.Zero);

		JPanel panelNorth = new JPanel();
		panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.X_AXIS));

		JTextField textField = new JTextField();
		textField.setColumns(10);
		panelNorth.add(textField);

		JToggleButton jtbExpandAttendents = new JToggleButton("Show Attendents");
		jtbExpandAttendents.setHorizontalAlignment(SwingConstants.RIGHT);
		jtbExpandAttendents.setSelected(false);
		getJScrpAttendents().setVisible(false);
		add(getJScrpAttendents(), BorderLayout.EAST);
		jtbExpandAttendents.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				getJScrpAttendents().setVisible(e.getStateChange() == ItemEvent.SELECTED);
				validate();
			}
		});

		panelNorth.add(jtbExpandAttendents);
		add(panelNorth, BorderLayout.NORTH);
	}

	/**
	 * Receive a message.
	 *
	 * @param message
	 *            The message received.
	 * @throws BadLocationException
	 */
	@Override
	public void receiveMessage(Date timestamp, UUID fromId, String fromName, String message, String style)
			throws BadLocationException {
		boolean self = fromId != null && fromId.equals(_Client.Self.getAgentID());
		// Did we already print that message?
		if (!(self && printLocalMessage)) {
			String localStyle = style, localMessage = message, chatStyle = STYLE_CHATREMOTE;
			if (self) {
				chatStyle = STYLE_CHATLOCAL;
			}
			// Determine if this is a friend...
			else if (_Client.Friends.getFriendList().containsKey(fromId)) {
				chatStyle = STYLE_CHATREMOTEFRIEND;
			}

			// If this is an action message
			if (message.startsWith("/me ")) {
				localStyle = style != null ? style : STYLE_ACTION;
				// Remove the "/me"
				localMessage = message.substring(3);
			} else if (style == null) {
				localStyle = STYLE_REGULAR;
				localMessage = ": " + message;
			}
			addMessage(new ChatItem(timestamp, fromName, chatStyle, localMessage, localStyle));
		}
	}

	protected void transmitMessage(String message, ChatType chatType) throws Exception {
		if (message == null || message.trim().isEmpty())
			return;

		// Indicate that we're no longer typing.
		super.transmitMessage(message, chatType);

		int channel = 0;
		String shortMessage, localMessage = message, style = STYLE_REGULAR, self = _Client.Self.getName();
		ChatType localType = chatType;

		if (message.length() >= 1000) {
			localMessage = message.substring(0, 1000);
		}
		addHistory(localMessage);

		// Do we have a command?
		if (localMessage.charAt(0) == '/') {
			String firstWord = "";
			try {
				firstWord = localMessage.split("\\s")[0].toLowerCase();
			} catch (Exception ex) {
			}

			shortMessage = localMessage.substring(firstWord.length()).trim();

			// Deal with actions.
			if (firstWord.equals("/me")) {
				style = STYLE_ACTION;
				// Send the message as is
			}
			// Shout
			else if ((firstWord.startsWith("/shout")) || (firstWord.equals("/s"))) {
				localType = ChatType.Shout;
				style = STYLE_ACTION;
				// Send the message without the /shout command
				localMessage = shortMessage;
				shortMessage = " shouts: " + shortMessage;
			}
			// Whisper
			else if ((firstWord.startsWith("/whisper")) || (firstWord.equals("/w"))) {
				localType = ChatType.Whisper;
				style = STYLE_ACTION;
				// Send the message without the /whisper command
				localMessage = shortMessage;
				shortMessage = " whispers: " + shortMessage;
			} else if (firstWord.length() > 1) {
				// Is there a channel request?
				if (firstWord.equals("//")) {
					// Use previous channel
					channel = chatChannel;
				} else {
					try {
						channel = Integer.parseInt(firstWord.substring(1));
						chatChannel = channel;
					} catch (Exception ex) {
					}
				}

				if (channel != 0) {
					localMessage = shortMessage;
					// Remove the channel command from the message
					shortMessage = "(" + channel + ") " + shortMessage;
				} else {
					shortMessage = ": " + shortMessage;
				}
			}
		} else {
			shortMessage = ": " + message;
		}

		if (printLocalMessage) {
			addMessage(new ChatItem(self, STYLE_CHATLOCAL, shortMessage, style));
		}

		// Send the message.
		_Client.Self.Chat(localMessage, channel, localType);
	}

	protected void triggerTyping(boolean start) throws Exception {
		_Client.Self.Chat("typing", 0, start ? ChatType.StartTyping : ChatType.StopTyping);
	}

	private JScrollPane getJScrpAttendents() {
		if (jScrpAttendents == null) {
			jScrpAttendents = new JScrollPane();
			add(jScrpAttendents, BorderLayout.EAST);

			JList<String> listAttendents = new JList<String>();
			listAttendents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jScrpAttendents.setViewportView(listAttendents);
		}
		return jScrpAttendents;
	}
}
