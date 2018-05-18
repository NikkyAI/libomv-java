/**
 * Copyright (c) 2006-2014, openmetaverse.org
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
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
package libomv.imaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import libomv.types.Vector3;
import libomv.utils.Helpers;

public class ManagedImage implements Cloneable {
	public enum ImageCodec {
		Invalid, RGB, J2K, BMP, TGA, JPEG, DXT, PNG;

		public static ImageCodec setValue(int value) {
			return values()[value];
		}

		public static byte getValue(ImageCodec value) {
			return (byte) value.ordinal();
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	// [Flags]
	public class ImageChannels {
		public static final byte Gray = 1;
		public static final byte Color = 2;
		public static final byte Alpha = 4;
		public static final byte Bump = 8;

		public void setValue(int value) {
			this.value = (byte) value;
		}

		public byte getValue() {
			return value;
		}

		private byte value;
	};

	public enum ImageResizeAlgorithm {
		NearestNeighbor
	}

	// Image width
	private int width;

	public int getWidth() {
		return width;
	}

	// Image height
	private int height;

	public int getHeight() {
		return height;
	}

	// Image channel flags
	private byte channels;

	public byte getChannels() {
		return channels;
	}

	// BitDepth per channel
	private int bitDepth;

	public int getBitDepth() {
		return bitDepth;
	}

	// Red channel data
	protected byte[] red;

	public byte[] getRed() {
		return red;
	}

	public byte getRed(int index) {
		return red[index];
	}

	public void setRed(int pixelIdx, byte val) {
		if (red != null)
			red[pixelIdx] = val;
	}

	// Green channel data
	protected byte[] green;

	public byte[] getGreen() {
		return green;
	}

	public byte getGreen(int index) {
		return green[index];
	}

	public void setGreen(int pixelIdx, byte val) {
		if (green != null)
			green[pixelIdx] = val;
	}

	// Blue channel data
	protected byte[] blue;

	public byte[] getBlue() {
		return blue;
	}

	public byte getBlue(int index) {
		return blue[index];
	}

	public void setBlue(int pixelIdx, byte val) {
		if (blue != null)
			blue[pixelIdx] = val;
	}

	// Alpha channel data
	protected byte[] alpha;

	public byte[] getAlpha() {
		return alpha;
	}

	public byte getAlpha(int index) {
		return alpha[index];
	}

	public void setAlpha(int pixelIdx, byte val) {
		if (alpha != null)
			alpha[pixelIdx] = val;
	}

	// Bump channel data
	protected byte[] bump;

	public byte[] getBump() {
		return bump;
	}

	public void setBump(byte[] array) {
		bump = array;
	}

	public void setBump(int pixelIdx, byte val) {
		if (bump != null)
			bump[pixelIdx] = val;
	}

	public ManagedImage() {

	}

	/* Only do a shallow copy of the input image */
	public ManagedImage(ManagedImage image) {
		this.height = image.getHeight();
		this.width = image.getWidth();
		this.channels = image.getChannels();
		this.bitDepth = image.getBitDepth();
		this.alpha = image.getAlpha();
		this.bump = image.getBump();
		this.red = image.getRed();
		this.green = image.getGreen();
		this.blue = image.getBlue();
	}

	/**
	 * Create a new blank image
	 *
	 * @param width
	 *            width
	 * @param height
	 *            height
	 * @param channels
	 *            channel flags
	 */
	public ManagedImage(int width, int height, byte channels) {
		this.width = width;
		this.height = height;
		this.channels = channels;
		initialize();
	}

	public void clear() {
		Arrays.fill(red, (byte) 0);
		Arrays.fill(green, (byte) 0);
		Arrays.fill(blue, (byte) 0);
		Arrays.fill(alpha, (byte) 0);
		Arrays.fill(bump, (byte) 0);
	}

	protected int initialize() {
		int n = width * height;

		if ((channels & ImageChannels.Gray) != 0) {
			red = new byte[n];
			green = null;
			blue = null;
		} else if ((channels & ImageChannels.Color) != 0) {
			red = new byte[n];
			green = new byte[n];
			blue = new byte[n];
		}

		if ((channels & ImageChannels.Alpha) != 0)
			alpha = new byte[n];
		else
			alpha = null;

		if ((channels & ImageChannels.Bump) != 0)
			bump = new byte[n];
		else
			bump = null;

		return n;
	}

	protected void deepCopy(ManagedImage src) {
		// Deep copy member fields here
		if (src.getAlpha() != null) {
			alpha = src.getAlpha().clone();
		} else {
			alpha = null;
		}
		if (src.getRed() != null) {
			red = src.getRed().clone();
		} else {
			red = null;
		}
		if (src.getGreen() != null) {
			green = src.getGreen().clone();
		} else {
			green = null;
		}
		if (src.getBlue() != null) {
			blue = src.getBlue().clone();
		} else {
			blue = null;
		}
		if (src.getBump() != null) {
			bump = src.getBump().clone();
		} else {
			bump = null;
		}
	}

	/**
	 * Convert the channels in the image. Channels are created or destroyed as
	 * required.
	 *
	 * @param channels
	 *            new channel flags
	 */
	public void convertChannels(byte channels) {
		if (this.channels == channels)
			return;

		int n = this.width * this.height;
		byte add = (byte) (this.channels ^ channels & channels);
		byte del = (byte) (this.channels ^ channels & this.channels);

		if ((add & ImageChannels.Color) != 0) {
			red = new byte[n];
			green = new byte[n];
			blue = new byte[n];
		} else if ((del & ImageChannels.Color) != 0) {
			red = null;
			green = null;
			blue = null;
		}

		if ((add & ImageChannels.Alpha) != 0) {
			alpha = new byte[n];
			Arrays.fill(this.getAlpha(), (byte) 255);
		} else if ((del & ImageChannels.Alpha) != 0)
			alpha = null;

		if ((add & ImageChannels.Bump) != 0) {
			bump = new byte[n];
		} else if ((del & ImageChannels.Bump) != 0) {
			bump = null;
		}

		this.channels = channels;
	}

	public ArrayList<ArrayList<Vector3>> toRows(boolean mirror) {

		ArrayList<ArrayList<Vector3>> rows = new ArrayList<ArrayList<Vector3>>(height);

		float pixScale = 1.0f / 255;

		int rowNdx, colNdx;
		int smNdx = 0;

		for (rowNdx = 0; rowNdx < height; rowNdx++) {
			ArrayList<Vector3> row = new ArrayList<Vector3>(width);
			for (colNdx = 0; colNdx < width; colNdx++) {
				if (mirror)
					row.add(new Vector3(-(red[smNdx] * pixScale - 0.5f), (green[smNdx] * pixScale - 0.5f),
							blue[smNdx] * pixScale - 0.5f));
				else
					row.add(new Vector3(red[smNdx] * pixScale - 0.5f, green[smNdx] * pixScale - 0.5f,
							blue[smNdx] * pixScale - 0.5f));

				++smNdx;
			}
			rows.add(row);
		}
		return rows;
	}

	/**
	 * Resize or stretch the image using nearest neighbor (ugly) resampling
	 *
	 * @param width
	 *            widt new width
	 * @param height
	 *            new height
	 */
	public void resizeNearestNeighbor(int width, int height) {
		if (this.width == width && this.height == height)
			return;

		byte[] red = null, green = null, blue = null, alpha = null, bump = null;
		int n = width * height;
		int di = 0, si;

		if (this.red != null)
			red = new byte[n];
		if (this.green != null)
			green = new byte[n];
		if (this.blue != null)
			blue = new byte[n];
		if (this.alpha != null)
			alpha = new byte[n];
		if (this.bump != null)
			bump = new byte[n];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				si = (y * height / height) * width + (x * width / width);
				if (this.red != null)
					red[di] = this.red[si];
				if (this.green != null)
					green[di] = this.green[si];
				if (this.blue != null)
					blue[di] = this.blue[si];
				if (this.alpha != null)
					alpha[di] = this.alpha[si];
				if (this.bump != null)
					bump[di] = this.bump[si];
				di++;
			}
		}

		this.width = width;
		this.height = height;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		this.bump = bump;
	}

	@Override
	public ManagedImage clone() throws CloneNotSupportedException {
		ManagedImage clone;
		clone = (ManagedImage) super.clone();

		// Deep copy member fields here
		deepCopy(clone);
		return clone;
	}

	/**
	 * Saves the image data into an output stream with whatever encoding this object
	 * supports
	 *
	 * Note: This method does currently nothing as it does not support a native raw
	 * image format This method should be overwritten by derived classes to save the
	 * image data with whatever default options makes most sense for the image
	 * format.
	 *
	 * @param os
	 *            Stream in which to write the image data
	 * @return number of bytes written into the stream or -1 on error
	 * @throws Exception
	 */
	protected int encode(OutputStream os) throws Exception {
		return 0;
	}

	/**
	 * Saves the image data into an output stream with whatever encoding this object
	 * supports
	 *
	 * Note: This method does currently nothing as it does not support a native raw
	 * image format This method should be overwritten by derived classes to save the
	 * image data with whatever default options makes most sense for the image
	 * format.
	 *
	 * @param os
	 *            Stream in which to write the image data
	 * @return number of bytes written into the stream or -1 on error
	 * @throws Exception
	 */
	public int encode(OutputStream os, ImageCodec codec) throws Exception {
		switch (codec) {
		case J2K:
			return J2KImage.encode(os, this, false);
		case TGA:
			return TGAImage.encode(os, this);
		default:
			return encode(os);
		}
	}

	public static ManagedImage decode(File file) throws Exception {
		ManagedImage image = null;
		String ext = Helpers.getFileExtension(file.getName());
		FileInputStream is = new FileInputStream(file);
		try {
			if (ext.equals("j2k") || ext.equals("jp2")) {
				image = decode(is, ImageCodec.J2K);
			} else if (ext.equals("tga")) {
				image = decode(is, ImageCodec.TGA);
			} else {
				byte[] data = new byte[10];
				is.read(data);
				is.close();
			}
		} finally {
			is.close();
		}
		return image;
	}

	public static ManagedImage decode(InputStream input, ImageCodec codec) throws Exception {
		switch (codec) {
		case J2K:
			return J2KImage.decode(input);
		case TGA:
			return TGAImage.decode(input);
		default:
			return null;
		}
	}
}
