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

import java.util.Date;

import libomv.AgentManager.ChatType;
import libomv.Gui.windows.MainControl;
import libomv.types.UUID;

public class IMChannel extends AbstractChannel
{
	private static final long serialVersionUID = 1L;
	
	public IMChannel(MainControl main, String name, UUID id, UUID session)
	{
		super(main, name, id, session);
	}
		
	/**
	 * Receive a message.
	 * 
	 * @param message The message received.
	 */
	@Override
	public void receiveMessage(Date timestamp, UUID fromId, String fromName, String message, boolean offline)
	{
		if(message == null || message.isEmpty())
			return;
		
		// Determine if this is a friend...
		boolean friend = _Main.getGridClient().Friends.getFriendList().containsKey(fromId);
		
		// If this is an action message.
		if(message.startsWith("/me "))
		{
			// Remove the "/me ".
			addMessage(new ChatItem(timestamp, true, fromName, friend ? STYLE_CHATREMOTEFRIEND : STYLE_CHATREMOTE, "* " + message.substring(4), offline ? STYLE_OFFLINE : STYLE_ACTION));
		}
		else
		{
			// This is a normal message.
			addMessage(new ChatItem(timestamp, false, fromName, friend ? STYLE_CHATREMOTEFRIEND : STYLE_CHATREMOTE, message, offline ? STYLE_OFFLINE : STYLE_REGULAR));
		}
	}

	@Override
	public void transmitMessage(String message, ChatType type) throws Exception
	{
        if (message == null || message.trim().isEmpty())
        	return;

		String self = _Main.getGridClient().Self.getName();
		if(getID() != null)
		{
			addHistory(message);	

			// Deal with actions.
			if(message.toLowerCase().startsWith("/me "))
			{
				// Remove the "/me "
				addMessage(new ChatItem(true, self, STYLE_CHATLOCAL, message.substring(4).trim(), STYLE_ACTION));
			}
			else
			{
				addMessage(new ChatItem(false, self, STYLE_CHATLOCAL, message.substring(4), STYLE_REGULAR));
			}
				
			// Send the message.
			_Main.getGridClient().Self.InstantMessage(getID(), message, getSession());
			// Indicate that we're no longer typing.
			_Main.getGridClient().Self.SendTypingState(getID(), getSession(), false);
		}
		else
		{
			addMessage(new ChatItem(true, self, STYLE_CHATLOCAL, "Invlid UUID for this chat channel", STYLE_ERROR));			
		}
	}
}
