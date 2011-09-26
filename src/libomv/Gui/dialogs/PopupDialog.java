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

import java.awt.FontMetrics;
import java.text.BreakIterator;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

// Abstract base class for a dialog showing an auto wrapping message
public abstract class PopupDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	// The width of the window
	protected static final int width = 375;

	public PopupDialog(JFrame parent, String title, boolean modal)
	{
		// Super constructor
		super(parent, title, modal);

		// Set the title again
		super.setTitle(title);

		// Do not allow resizing
		setResizable(false);

	}

	/**
	 * Method to determine where to wrap text within a JLabel
	 * 
	 * @param label The label
	 * @param text The text
	 */
	protected void wrapLabelText(JLabel label, String text)
	{
		// Get the font metrics
		FontMetrics fm = label.getFontMetrics(label.getFont());
		// Get the break iterator for word boundaries
		BreakIterator breakIterator = BreakIterator.getWordInstance();
		breakIterator.setText(text);

		// String builders for the current line and final text
		StringBuffer currentLine = new StringBuffer();
		StringBuffer finalText = new StringBuffer("<html>");

		// Get the first boundary
		int start = 0;

		String word = null;
		
		// While there are remaining boundaries
		for (int end = breakIterator.next(); end != BreakIterator.DONE; start = end, end = breakIterator.next())
		{
			boolean wasTag = false;
			String prevWord = word;
			// Get the word.
			word = text.substring(start, end);
			// Append the word
			currentLine.append(word);
			// Get the width of the current line
			int currentLineWidth = SwingUtilities.computeStringWidth(fm, currentLine.toString());

			// If the current line is too wide
			if (currentLineWidth > width - 15 - 32)
			{
				// If we were about to insert a new line...
				if (currentLine.toString().toLowerCase().endsWith("<br"))
				{
					// Remove it
					currentLine.substring(0, currentLine.length() - 3);
					wasTag = true;
				}

				if (!wasTag)
				{
					// Reset the trial (but keep the word in buffer as we will put this on the next line)
					currentLine = new StringBuffer(word);
				}
				else
				{
					currentLine = new StringBuffer();
				}

				// Add a line break
				finalText.append("<br>");
			}

			if (finalText.toString().toLowerCase().endsWith("br>") && word.equals(">") && prevWord != null
					&& prevWord.toLowerCase().equals("br"))
			{
				wasTag = false;
			}
			else
			{
				// Append the word to the final text buffer
				finalText.append(word);
			}
		}

		// Close the result
		finalText.append("</html>");

		// Set the label
		label.setText(finalText.toString());
	}

	/**
	 * Count the number of a instances of a substring within a string
	 */
	protected static int countOf(String text, String search)
	{
		int count = 0;
		for (int fromIndex = 0; fromIndex > -1; count++)
		{
			fromIndex = text.indexOf(search, fromIndex + ((count > 0) ? 1 : 0));
		}

		return count - 1;
	}
}
