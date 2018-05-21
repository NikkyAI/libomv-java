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

import java.util.Date;

import javax.swing.text.BadLocationException;

import libomv.Gui.windows.MainControl;
import libomv.model.agent.ChatType;
import libomv.types.UUID;

public class PrivateChannel extends AbstractChannel {
	private static final long serialVersionUID = 1L;

	public PrivateChannel(MainControl main, String name, UUID id, UUID session) {
		super(main, name, id, session);
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
		// Determine if this is a friend...
		boolean friend = _Client.friends.getFriendList().containsKey(fromId);
		String localMessage = message, localStyle = style;

		// If this is an action message.
		if (message.startsWith("/me ")) {
			localStyle = STYLE_ACTION;
			// Remove the "/me".
			localMessage = message.substring(3);
		} else if (style == null) {
			localStyle = STYLE_REGULAR;
			localMessage = ": " + message;
		}
		// This is a normal message.
		addMessage(new ChatItem(timestamp, fromName, friend ? STYLE_CHATREMOTEFRIEND : STYLE_CHATREMOTE, localMessage,
				localStyle));
	}

	@Override
	public void transmitMessage(String message, ChatType chatType) throws Exception {
		if (message == null || message.trim().isEmpty())
			return;

		// Indicate that we're no longer typing.
		super.transmitMessage(message, chatType);

		String localMessage = message, self = _Client.agent.getName();
		if (getUUID() != null) {
			if (message.length() >= 1000) {
				localMessage = message.substring(0, 1000);
			}
			addHistory(message);

			if (_Main.getStateControl().RLV.restrictionActive("sendim", getUUID())) {
				localMessage = "*** IM blocked by sender's viewer";
			}

			// Deal with actions.
			if (localMessage.toLowerCase().startsWith("/me ")) {
				// Remove the "/me "
				addMessage(new ChatItem(self, STYLE_CHATLOCAL, " " + localMessage.substring(4).trim(), STYLE_ACTION));
			} else {
				addMessage(new ChatItem(self, STYLE_CHATLOCAL, ": " + localMessage, STYLE_REGULAR));
			}

			// Send the message.
			_Client.agent.instantMessage(getUUID(), localMessage, getSession());
		} else {
			addMessage(new ChatItem(self, STYLE_CHATLOCAL, ": Invalid UUID for this chat channel", STYLE_ERROR));
		}
	}

	protected void triggerTyping(boolean start) throws Exception {
		_Client.agent.sendTypingState(getUUID(), getSession(), start);
	}
}
