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

import libomv.Gui.Resources;
import libomv.Gui.components.ImagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

// Shows a dialog with a auto wrapping message and an icon indication the popup type
public class PopupInfoDialog extends PopupDialog
{
	private static final long serialVersionUID = 1L;

	public enum Severity
	{
		ALERT, ERROR, INFORMATIONAL
	}

	// The width of the window
	protected static final int width = 375;

	public PopupInfoDialog(JFrame parent, String title, String message, Severity severity)
	{
		// Super constructor
		super(parent, title, true);

		// The icon to display with the popup
		String name;

		// No severity, assume informational
		if (severity == null)
		{
			name = Resources.IMAGE_INFORMATIONAL;
		}
		// Alert
		else if (severity.equals(Severity.ALERT))
		{
			name = Resources.IMAGE_ALERT;
		}
		// Error
		else if (severity.equals(Severity.ERROR))
		{
			name = Resources.IMAGE_ERROR;
		}
		// Informational
		else if (severity.equals(Severity.INFORMATIONAL))
		{
			name = Resources.IMAGE_INFORMATIONAL;
		}
		// Something else...
		else
		{
			name = Resources.IMAGE_INFORMATIONAL;
		}

		// Create a panel to hold the icon
		JPanel jpWest = new JPanel(new FlowLayout());

		// Create the icon
		ImagePanel img = new ImagePanel(name);
		img.setSize(new Dimension(32, 32));
		img.setPreferredSize(new Dimension(32, 32));
		// Add the icon
		jpWest.add(img);

		// Create a label for the message
		JLabel jlMessage = new JLabel(message);

		// Wrap the text
		wrapLabelText(jlMessage, message);
		int lines = countOf(jlMessage.getText().toLowerCase(), "<br>") + 1;

		// Create a panel for the dialog
		JPanel jpMain = new JPanel(new BorderLayout());
		// Set the content pane.
		setContentPane(jpMain);

		// Add the west panel
		jpMain.add(jpWest, BorderLayout.WEST);

		// Create a center panel
		JPanel jpCenter = new JPanel();
		// Add the message
		jpCenter.add(jlMessage);
		// Add the center panel
		jpMain.add(jpCenter, BorderLayout.CENTER);

		// Create a south panel for the button
		JPanel jpSouth = new JPanel();
		// Create the button
		JButton jbOkay = new JButton("OK");
		// Add the button to the south panel
		jpSouth.add(jbOkay);
		// Add the button to the south
		jpMain.add(jpSouth, BorderLayout.SOUTH);

		// Set the size of the dialog based on the number of line wraps
		FontMetrics fm = jlMessage.getFontMetrics(jlMessage.getFont());
		setSize(width, (fm.getHeight() * lines) + 100);

		// Open in the center of the screen
		setLocationRelativeTo(null);

		// Add an action listener to the button
		jbOkay.addActionListener(new ActionListener()
		{
			/**
			 * Called when an action is performed
			 */
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				// Hide the dialog
				setVisible(false);
			}
		});
	}
}
