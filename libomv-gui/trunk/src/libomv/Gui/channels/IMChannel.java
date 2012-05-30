/**
 * Copyright (c) 2009-2012, Frederick Martian
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
package libomv.Gui.channels;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.StyledDocument;

import libomv.GridClient;
import libomv.types.UUID;

public class IMChannel extends AbstractChannel
{
	private static final long serialVersionUID = 1L;

	private JTextPane jTxPane;
	private JTextField jTxChat;
	
	public IMChannel(GridClient client, String name, UUID id)
	{
		super(client, name, id);

		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPaneText = new JScrollPane();
		scrollPaneText.setViewportView(getTxPane());
		add(scrollPaneText, BorderLayout.CENTER);

		JPanel panelSouth = new JPanel();
		panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));
		panelSouth.add(getJTxChat());

		JButton btnSay = new JButton("Say");
		btnSay.setHorizontalAlignment(SwingConstants.RIGHT);
		btnSay.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				// TODO send the text from the chatTextField
				getJTxChat().getText();
			}
		});

		panelSouth.add(btnSay);
		add(panelSouth, BorderLayout.SOUTH);
	}

	private JTextPane getTxPane()
	{
		if (jTxPane == null)
		{
			jTxPane = new JTextPane();
		}
		return jTxPane;
	}

	private JTextField getJTxChat()
	{
		if (jTxChat == null)
		{
			jTxChat = new JTextField();
			jTxChat.setHorizontalAlignment(SwingConstants.LEFT);
			jTxChat.setColumns(20);
		}
		return jTxChat;
	}

	@Override
	public StyledDocument getDocument()
	{
		return getTxPane().getStyledDocument();
	}
}
