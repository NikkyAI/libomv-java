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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import libomv.imaging.TGAHeader.TGAColorMap;

import org.apache.commons.io.input.SwappedDataInputStream;

public class TGAImage extends ManagedImage {
	public TGAImage(ManagedImage image) throws IOException {
		super(image);
	}

	protected TGAImage(int width, int height, byte type) throws Exception {
		super(width, height, type);
	}

	private void unpackColor(int[] values, int pixel, TGAColorMap cd) {
		for (int x = 0; x < getWidth(); x++, pixel++) {
			int val = values[x];
			if (cd.rMask == 0 && cd.gMask == 0 && cd.bMask == 0 && cd.aMask == 0xFF) {
				// Special case to deal with 8-bit TGA files that we treat as alpha masks
				setAlpha(pixel, (byte) val);
			} else if (cd.length > 0) {
				setRed(pixel, cd.redM[val]);
				setGreen(pixel, cd.greenM[val]);
				setBlue(pixel, cd.blueM[val]);
				setAlpha(pixel, cd.alphaM[val]);
			} else {
				setRed(pixel, (byte) ((val >> cd.rShift) & cd.rMask));
				setGreen(pixel, (byte) ((val >> cd.gShift) & cd.gMask));
				setBlue(pixel, (byte) ((val >> cd.bShift) & cd.bMask));
				setAlpha(pixel, (byte) ((val >> cd.aShift) & cd.aMask));
			}
		}
	}

	/**
	 * Reads the pixmap as RLE encode stream
	 * 
	 * @param is
	 *            the DataInputStream in little endian format
	 * @param byp
	 *            the number of bytes to read per pixel value
	 * @param cd
	 *            the color map structure that contains the information how to
	 *            interpret the color value of the pixel entry
	 * @param bottomUp
	 *            indicates if the bitmap is stored in bottemUp format
	 * @throws IOException
	 */
	private void decodeRle(SwappedDataInputStream is, int byp, TGAColorMap cd, boolean bottomUp) throws IOException {
		int vals[] = new int[getWidth() + 128];
		int x = 0, pixel = bottomUp ? (getHeight() - 1) * getWidth() : 0;

		// RLE compressed
		for (int y = 0; y < getHeight(); y++) {
			while (x < getWidth()) {
				int nb = is.readUnsignedByte(); // num of pixels
				if ((nb & 0x80) == 0) { // 0x80 = dec 128, bits 10000000
					for (int i = 0; i <= nb; i++, x++) {
						for (int k = 0; k < byp; k++) {
							vals[x] |= is.readUnsignedByte() << (k << 3);
						}
					}
				} else {
					int val = 0;
					for (int k = 0; k < byp; k++) {
						val |= is.readUnsignedByte() << (k << 3);
					}
					nb &= 0x7f;
					for (int j = 0; j <= nb; j++, x++) {
						vals[x] = val;
					}
				}
			}
			unpackColor(vals, pixel, cd);
			if (x > getWidth()) {
				System.arraycopy(vals, getWidth(), vals, 0, x - getWidth());
				x -= getWidth();
			} else {
				x = 0;
			}
			pixel += bottomUp ? -getWidth() : getWidth();
		}
	}

	/**
	 * Reads the pixmap as unencoded stream
	 * 
	 * @param is
	 *            the DataInputStream in little endian format
	 * @param byp
	 *            the number of bytes to read per pixel value
	 * @param cd
	 *            the color map structure that contains the information how to
	 *            interpret the color value of the pixel entry
	 * @param bottomUp
	 *            indicates if the bitmap is stored in bottemUp format
	 * @throws IOException
	 */
	private void decodePlain(SwappedDataInputStream is, int byp, TGAColorMap cd, boolean bottomUp) throws IOException {
		int vals[] = new int[getWidth()];
		int pixel = bottomUp ? (getHeight() - 1) * getWidth() : 0;
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				for (int k = 0; k < byp; k++) {
					vals[x] |= is.readUnsignedByte() << (k << 3);
				}
			}
			unpackColor(vals, pixel, cd);
			pixel += bottomUp ? -getWidth() : getWidth();
		}
	}

	public static TGAImage decode(InputStream is) throws Exception {
		SwappedDataInputStream sis = is instanceof SwappedDataInputStream ? (SwappedDataInputStream) is
				: new SwappedDataInputStream(is);
		TGAHeader header = new TGAHeader(sis);
		byte channels = 0;

		if (header.imageSpec.width > 4096 || header.imageSpec.height > 4096)
			throw new IllegalArgumentException("Image too large.");

		if (header.imageSpec.pixelDepth != 8 && header.imageSpec.pixelDepth != 16 && header.imageSpec.pixelDepth != 24
				&& header.imageSpec.pixelDepth != 32)
			throw new IllegalArgumentException("Not a supported tga file.");

		if (header.colorMap.alphaBits > 0) {
			channels = ImageChannels.Alpha;
		}
		if (header.colorMap.colorBits > 0) {
			channels += ImageChannels.Color;
		} else if (header.colorMap.bits > header.colorMap.alphaBits) {
			channels += ImageChannels.Gray;
		}

		TGAImage image = new TGAImage(header.imageSpec.width, header.imageSpec.height, channels);

		if (header.getRleEncoded())
			image.decodeRle(sis, header.imageSpec.pixelDepth / 8, header.colorMap, header.imageSpec.getBottomUp());
		else
			image.decodePlain(sis, header.imageSpec.pixelDepth / 8, header.colorMap, header.imageSpec.getBottomUp());
		return image;
	}

	@Override
	public int encode(OutputStream os) throws Exception {
		return TGAImage.encode(os, this);
	}

	public static int encode(OutputStream os, ManagedImage image) throws Exception {
		TGAHeader header = new TGAHeader(image);
		header.write(os);

		int len = 18, n = image.getWidth() * image.getHeight();

		if ((image.getChannels() & ImageChannels.Alpha) != 0) {
			if ((image.getChannels() & ImageChannels.Color) != 0) {
				// RGBA
				for (int i = 0; i < n; i++) {
					os.write(image.getBlue()[i]);
					os.write(image.getGreen()[i]);
					os.write(image.getRed()[i]);
					os.write(image.getAlpha()[i]);
				}
			} else if ((image.getChannels() & ImageChannels.Gray) != 0) {
				for (int i = 0; i < n; i++) {
					os.write(image.getRed()[i]);
					os.write(image.getAlpha()[i]);
				}
			} else {
				// Alpha only
				for (int i = 0; i < n; i++) {
					os.write(image.getAlpha()[i]);
				}
			}
			len += n * 4;
		} else {
			if ((image.getChannels() & ImageChannels.Color) != 0) {
				// RGB
				for (int i = 0; i < n; i++) {
					os.write(image.getBlue()[i]);
					os.write(image.getGreen()[i]);
					os.write(image.getRed()[i]);
				}
			} else if ((image.getChannels() & ImageChannels.Gray) != 0) {
				for (int i = 0; i < n; i++) {
					os.write(image.getRed()[i]);
				}
			}
			len += n * 3;
		}
		return len;
	}
}
