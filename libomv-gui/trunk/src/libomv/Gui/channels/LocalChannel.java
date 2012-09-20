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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.BoxLayout;

import libomv.GridClient;
import libomv.types.UUID;

public class LocalChannel extends AbstractChannel
{
	private static final long serialVersionUID = 1L;

	private JScrollPane jScrpAttendents; 

	/**
	 * This is the default constructor
	 */
	public LocalChannel(GridClient client)
	{
		super(client, "Local Chat", UUID.Zero);
		
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

		JScrollPane scrollPaneText = new JScrollPane();
		scrollPaneText.setViewportView(getTxPane());
		add(scrollPaneText, BorderLayout.CENTER);

		add(getJScrpAttendents(), BorderLayout.EAST);
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
