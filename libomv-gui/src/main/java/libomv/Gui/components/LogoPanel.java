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
package libomv.Gui.components;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import libomv.Gui.Resources;

// Displays a panel with our logo or a working indicator
public class LogoPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	// Are we currently connected
	private boolean isConnecting = false;
	// The active subimage to draw
	private Image activeImage;

	public LogoPanel()
	{
	}

	@Override
	public void paintComponent(Graphics g)
	{
		// Call the super class
		super.paintComponent(g);

		// If we're not connecting
		if (!isConnecting)
		{
			activeImage = Resources.loadImage(Resources.IMAGE_LOGO);
		}
		// Draw the active image
		g.drawImage(activeImage, 0, 0, this);
	}

	/**
	 * Set the connecting state and start the connection spinner if we're
	 * connecting.
	 * 
	 * @param connecting
	 *            True if connecting, false if not.
	 */
	public void setConnecting(boolean connecting)
	{
		// Set the flag
		this.isConnecting = connecting;
		// Repaint (we might need to repaint the logo)
		repaint();

		// If we're connecting
		if (connecting)
		{
			// Start a new thread to display the connection spinner
			new Thread(new Runnable()
			{
				/**
				 * Called when the thread starts.
				 */
				@Override
				public void run()
				{
					// The image frame index
					int x, y, imgIndex = 0;

					BufferedImage progress = Resources.loadImage(Resources.IMAGE_WORKING);

					// While we're still connecting
					while (isConnecting)
					{
						// Overflow of frame.
						if (imgIndex > 31)
						{
							imgIndex = 1;
						}

						x = (imgIndex % 8) * 32;
						y = (imgIndex / 8) * 32;

						activeImage = progress.getSubimage(x, y, 32, 32);

						// Increment the frame
						imgIndex++;
						// Repaint the image
						repaint();

						try
						{
							Thread.sleep(50);
						}
						catch (InterruptedException e)
						{
							// Ignore
						}
					}
				}
			}).start();
		}
	}

	/**
	 * Get whether we're connecting or not
	 * 
	 * @return True if connecting, false if not
	 */
	public boolean getIsConnecting()
	{
		return isConnecting;
	}
}
