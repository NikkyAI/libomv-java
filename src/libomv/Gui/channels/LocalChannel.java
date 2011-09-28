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
package libomv.Gui.channels;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.BoxLayout;
import javax.swing.text.StyledDocument;

import libomv.types.UUID;

public class LocalChannel extends AbstractChannel
{
	private static final long serialVersionUID = 1L;
	private JTextField chatText;
	private JTextField textField;

	/**
	 * This is the default constructor
	 */
	public LocalChannel()
	{
		super();
		setLayout(new BorderLayout(0, 0));

		JPanel panelNorth = new JPanel();
		add(panelNorth, BorderLayout.NORTH);
		panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.X_AXIS));
		
		textField = new JTextField();
		panelNorth.add(textField);
		textField.setColumns(10);

		JToggleButton jtbExpandAttendents = new JToggleButton("Show Attendents");
		jtbExpandAttendents.setHorizontalAlignment(SwingConstants.RIGHT);
		panelNorth.add(jtbExpandAttendents);

		JPanel panelSouth = new JPanel();
		add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));

		chatText = new JTextField();
		chatText.setHorizontalAlignment(SwingConstants.LEFT);
		panelSouth.add(chatText);
		chatText.setColumns(20);

		JButton btnSay = new JButton("Say");
		btnSay.setHorizontalAlignment(SwingConstants.RIGHT);
		btnSay.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// TODO add chat routing
			}
		});
		panelSouth.add(btnSay);

		JScrollPane scrollPaneText = new JScrollPane();
		add(scrollPaneText, BorderLayout.CENTER);
		
		JTextPane textPane = new JTextPane();
		scrollPaneText.setViewportView(textPane);

		JScrollPane scrollPaneAttendents = new JScrollPane();
		add(scrollPaneAttendents, BorderLayout.EAST);

		JList listAttendents = new JList();
		listAttendents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPaneAttendents.setViewportView(listAttendents);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		this.setSize(548, 587);
	}

	@Override
	public UUID getID()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StyledDocument getDocument()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
