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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import libomv.GridClient;
import libomv.assets.AssetTexture;
import libomv.assets.TexturePipeline.TextureDownloadCallback;
import libomv.assets.TexturePipeline.TextureRequestState;
import libomv.types.UUID;

// This class represents a texture received from the server
public class TextureImage extends JPanel
{
	private static final long serialVersionUID = 1L;
	// The image to draw
	private Image _Image = null;
	// The UUID of the image
	private UUID _TextureID;
	// The image dimensions
	private Dimension _Dimension;
	// Flag to determine if the image is resolved
	private boolean _Resolved = false;
	// Our grid client for the current session
	private GridClient _Client;

	/**
	 * Constructor
	 * 
	 * @param textureID
	 *            The UUID of the image
	 * @param dimension
	 *            The desired dimension
	 * @param initialImage
	 *            The initial image to use
	 */
	public TextureImage(GridClient client, UUID textureID, Dimension dimension, Image initialImage)
	{
		this._Client = client;
		this._TextureID = textureID;
		this.setImage(initialImage, false);
		forceSize(dimension);
	}

	/**
	 * Force the size to the specified dimension.
	 * 
	 * @param dimension
	 *            The dimension to force.
	 */
	public void forceSize(Dimension dimension)
	{
		this._Dimension = dimension;
		this.setSize(dimension);
		this.setMinimumSize(dimension);
		this.setMaximumSize(dimension);
		this.setPreferredSize(dimension);
	}

	/**
	 * Called to paint the component.
	 * 
	 * @param g
	 *            The graphics to paint.
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		// Call the super class
		super.paintComponent(g);

		// Draw the image image
		g.drawImage(_Image, 0, 0, _Dimension.width, _Dimension.height, this);
	}

	/**
	 * Called to update the component
	 * 
	 * @param g
	 *            The graphics to update
	 */
	@Override
	public void update(Graphics g)
	{
		// Call to the super class
		super.update(g);

		// Draw the image image
		g.drawImage(_Image, 0, 0, _Dimension.width, _Dimension.height, this);
	}

	/**
	 * Determine whether the image has been resolved
	 * 
	 * @return True if resolved, otherwise false
	 */
	public boolean isResolved()
	{
		return _Resolved;
	}

	/**
	 * Request the image from the server
	 */
	public void request()
	{
		_Client.Assets.RequestImage(_TextureID, new ImageDownloadCallback());
	}

	/**
	 * Get the image (used if just the raw image is required
	 * 
	 * @return The image contained within this control
	 */
	public Image getImage()
	{
		return _Image;
	}

	/**
	 * Set the image manually
	 * 
	 * @param image
	 *            The image to set.
	 * @param resolve
	 *            True if the image is to be indicated as resolved, otherwise
	 *            false.
	 */
	public void setImage(Image image, boolean resolve)
	{
		this._Image = image;
		_Resolved = resolve;
		this.repaint();
	}

	/**
	 * Callback receiving the texture download result
	 * 
	 */
	private class ImageDownloadCallback implements TextureDownloadCallback
	{
		@Override
		public void callback(TextureRequestState state, AssetTexture assetTexture)
		{
			if (state == TextureRequestState.Finished)
			{
				// TODO: Convert the Texture into an AWT Image
				Image image = null /* assetTexture.Image */;
				setImage(image, true);
			}
		}
	}
}
