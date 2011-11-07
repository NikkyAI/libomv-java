/**
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
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
package libomv.Gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import libomv.GridClient;
import libomv.types.UUID;

// Shows a dialog that allows to accept or deny a teleportation offer
public class TeleportOfferDialog extends PopupQuestionDialog
{
	private static final long serialVersionUID = 1L;
	// The the agent offering the teleportation
	private UUID _AgentID;
	// The the agent offering the teleportation
	private UUID _LureID;
	// Out grid client for the current session
	private GridClient _Client;

	/**
	 * Constructor to create the teleport offered dialog
	 * 
	 * @param client
	 *            The GridClient used to answer the result of the user decision
	 * @param parent
	 *            The parent object of this dialog
	 * @param agentName
	 *            The name of the Avatar offering teleportation
	 * @param agentID
	 *            The UID of the agent offering teleportation
	 * @param lureID
	 *            The session UID of the message
	 * @param message
	 *            The message sent from the Avatar
	 */
	public TeleportOfferDialog(GridClient client, JFrame parent, String agentName, UUID agentID, UUID lureID, String message)
	{
		super(parent, "Teleportation Offer", agentName + " has offered you a teleport with the following message: '"
				+ message + "'. Do you wish to accept?", "Accept", "Decline");
		this._Client = client;
		this._AgentID = agentID;
		this._LureID = lureID;
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
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					// Accept the request
					_Client.Self.TeleportLureRespond(_AgentID, _LureID, true);
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
				try
				{
					// Decline the request
					_Client.Self.TeleportLureRespond(_AgentID, _LureID, false);
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
