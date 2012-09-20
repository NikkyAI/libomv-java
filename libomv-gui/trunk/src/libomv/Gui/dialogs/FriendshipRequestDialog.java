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
package libomv.Gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import libomv.GridClient;
import libomv.types.UUID;

// Shows the dialog that allows to accept or deny a friendship offer 
public class FriendshipRequestDialog extends PopupQuestionDialog
{
	private static final long serialVersionUID = 1L;
	// The agent offering friendship
	private UUID _AgentID;
	// The session in which the friendship was offered
	private UUID _SessionID;
	// Our grid client for the current session
	private GridClient _Client;

	/**
	 * Constructor to create the friendship offered dialog
	 * 
	 * @param client
	 *            The GridClient used to answer the result of the user decision
	 * @param parent
	 *            The parent object of this dialog
	 * @param agentName
	 *            The name of the Avatar offering friendship
	 * @param agentID
	 *            The UID of the agent offering friendship
	 * @param sessionID
	 *            The UID of the session to use when answering the request
	 */
	public FriendshipRequestDialog(GridClient client, JFrame parent, String agentName, UUID agentID, UUID sessionID)
	{
		super(parent, "Friendship Request", agentName + " wants to be your friend. Do you accept this request?",
				"Accept", "Decline");
		this._Client = client;
		this._AgentID = agentID;
		this._SessionID = sessionID;
	}

	/**
	 * Get the {@link ActionListener} to be called when the accept button is
	 * activated
	 * 
	 * @return The {@link ActionListener} to be called when the accept button is
	 *         activated
	 */
	@Override
	protected ActionListener getAcceptButtonActionListener()
	{
		return new ActionListener()
		{
			/**
			 * Called when an action is performed.
			 */
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Accept the request
				try
				{
					_Client.Friends.AcceptFriendship(_AgentID, _SessionID);
				}
				catch (Exception ex)
				{
				}
				// Destroy the form
				setVisible(false);
				dispose();
			}
		};
	}

	/**
	 * Get the {@link ActionListener} to be called when the decline button is
	 * activated
	 * 
	 * @return The {@link ActionListener} to be called when the decline button
	 *         is activated
	 */
	@Override
	protected ActionListener getDeclineButtonActionListener()
	{
		return new ActionListener()
		{
			/**
			 * Called when an action is performed.
			 */
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Decline the request
				try
				{
					_Client.Friends.DeclineFriendship(_AgentID, _SessionID);
				}
				catch (Exception ex)
				{
				}
				// Destroy the form
				setVisible(false);
				dispose();
			}
		};
	}
}
