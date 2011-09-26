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
package libomv.Gui.components;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import libomv.Gui.Resources;

// Panel to display an image loaded from a resource or other buffered image 
public class ImagePanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	/** The image to draw. */
	private Image image = null;

	/**
	 * Constructor to intialize the contained image from a resource
	 * 
	 * @param name Resource name to load the image from
	 * @wbp.parser.constructor
	 */
	public ImagePanel(String name)
	{
		this.image = Resources.loadImage(name);
	}

	/**
	 * Constructor to intialize the contained image from an image
	 * 
	 * @param image to use when drawing this object
	 */
	public ImagePanel(Image image)
	{
		this.image = image;
	}

	public void setImagePanel(Image image)
	{
		this.image = image;
	}

	@Override
	public void paintComponent(Graphics g)
	{
		// Call the super class.
		super.paintComponent(g);

		// Draw the image image.
		g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
	}

	/**
	 * Get the image (used if just the raw image is required).
	 * 
	 * @return The image contained within this control.
	 */
	public Image getImage()
	{
		return image;
	}
}
