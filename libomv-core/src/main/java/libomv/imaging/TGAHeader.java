/**
 * Copyright (c) 2010-2017, Frederick Martian
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
import java.io.OutputStream;

import org.apache.commons.io.input.SwappedDataInputStream;

import libomv.imaging.ManagedImage.ImageChannels;

public class TGAHeader {
	protected class TGAColorMap {
		public int offset;
		public int length;
		public byte bits;
		int alphaBits;
		int colorBits;

		public int rMask;
		public int gMask;
		public int bMask;
		public int aMask;
		public int rShift;
		public int gShift;
		public int bShift;
		public int aShift;

		byte[] redM;
		byte[] greenM;
		byte[] blueM;
		byte[] alphaM;

		public TGAColorMap() {
		}

		public TGAColorMap(SwappedDataInputStream is) throws IOException {
			offset = is.readUnsignedShort();
			length = is.readUnsignedShort();
			bits = is.readByte();
		}

		public void writeHeader(OutputStream os) throws IOException {
			os.write((byte) (offset & 0xFF));
			os.write((byte) ((offset >> 8) & 0xFF));
			os.write((byte) (length & 0xFF));
			os.write((byte) ((length >> 8) & 0xFF));
			os.write((byte) bits);
		}

		public void readMap(SwappedDataInputStream is, boolean gray) throws IOException {
			if (bits == 8 || bits == 32) {
				alphaBits = 8;
			} else if (bits == 16) {
				alphaBits = 1;
			}

			colorBits = bits - alphaBits;
			if (!gray) {
				colorBits /= 3;

				bMask = (2 ^ colorBits) - 1;
				gShift = bShift + colorBits;
				gMask = (2 ^ colorBits) - 1;
				rShift = gShift + colorBits;

				if (gMask > 0)
					greenM = new byte[length];
				if (bMask > 0)
					blueM = new byte[length];
			}

			rMask = (2 ^ colorBits) - 1;
			aShift = rShift + colorBits;
			aMask = (2 ^ alphaBits) - 1;

			if (rMask > 0)
				redM = new byte[length];
			if (aMask > 0)
				alphaM = new byte[length];

			for (int i = 0; i < length; i++) {
				long x = 0;
				for (int k = 0; k < bits; k += 8) {
					x |= is.readUnsignedByte() << k;
				}

				if (redM != null)
					redM[i] = (byte) ((x >> rShift) & rMask);
				if (greenM != null)
					greenM[i] = (byte) ((x >> gShift) & gMask);
				if (blueM != null)
					blueM[i] = (byte) ((x >> bShift) & bMask);
				if (alphaM != null)
					alphaM[i] = (byte) ((x >> aShift) & aMask);
			}
		}

		public void setBits(TGAImageSpec spec, boolean gray) {
			// Treat 8 bit images as alpha channel
			if (alphaBits == 0 && (spec.pixelDepth == 8 || spec.pixelDepth == 32)) {
				alphaBits = 8;
			}

			length = 0;
			bits = spec.pixelDepth;
			colorBits = bits - alphaBits;
			if (!gray) {
				colorBits /= 3;

				bMask = (2 ^ Math.round(colorBits)) - 1;
				gShift = bShift + Math.round(colorBits);
				gMask = (2 ^ (int) Math.ceil(colorBits)) - 1;
				rShift = gShift + (int) Math.ceil(colorBits);
			}
			rMask = (2 ^ (int) Math.floor(colorBits)) - 1;
			aShift = rShift + (int) Math.floor(colorBits);
			aMask = (2 ^ alphaBits) - 1;
		}
	}

	protected class TGAImageSpec {
		public int xOrigin;
		public int yOrigin;
		public int width;
		public int height;
		public byte pixelDepth;
		public byte descriptor;

		public TGAImageSpec(ManagedImage image) {
			width = image.getWidth();
			height = image.getHeight();

			if (image.getChannels() == ImageChannels.Gray) {
				pixelDepth = 8;
			} else if (image.getChannels() == ImageChannels.Color) {
				pixelDepth = 24;
			}
			if (image.getChannels() == ImageChannels.Alpha) {
				descriptor = pixelDepth > 0 ? (byte) 0x28 : (byte) 0x20;
				pixelDepth += 8;
			}
		}

		public TGAImageSpec(SwappedDataInputStream is) throws IOException {
			xOrigin = is.readUnsignedShort();
			yOrigin = is.readUnsignedShort();
			width = is.readUnsignedShort();
			height = is.readUnsignedShort();
			pixelDepth = is.readByte();
			descriptor = is.readByte();
		}

		public void write(OutputStream os) throws IOException {
			os.write((byte) (xOrigin & 0xFF));
			os.write((byte) ((xOrigin >> 8) & 0xFF));
			os.write((byte) (yOrigin & 0xFF));
			os.write((byte) ((yOrigin >> 8) & 0xFF));
			os.write((byte) (width & 0xFF));
			os.write((byte) ((width >> 8) & 0xFF));
			os.write((byte) (height & 0xFF));
			os.write((byte) ((height >> 8) & 0xFF));
			os.write(pixelDepth);
			os.write(descriptor);
		}

		public byte getAlphaBits() {
			return (byte) (descriptor & 0xF);
		}

		public void setAlphaBits(int value) {
			descriptor = (byte) ((descriptor & ~0xF) | (value & 0xF));
		}

		public boolean getBottomUp() {
			return (descriptor & 0x20) == 0x20;
		}

		public void setBottomUp(boolean value) {
			descriptor = (byte) ((descriptor & ~0x20) | (value ? 0x0 : 0x20));
		}
	}

	public byte idLength;
	public byte colorMapType;
	public byte imageType;

	public TGAColorMap colorMap;
	public TGAImageSpec imageSpec;

	public TGAHeader(ManagedImage image) {
		colorMap = new TGAColorMap();
		imageSpec = new TGAImageSpec(image);
	}

	public TGAHeader(SwappedDataInputStream is) throws IOException {
		idLength = is.readByte();
		colorMapType = is.readByte();
		imageType = is.readByte();
		colorMap = new TGAColorMap(is);
		imageSpec = new TGAImageSpec(is);
		colorMap.alphaBits = imageSpec.getAlphaBits();

		is.skipBytes(idLength); // Skip any ID Length data

		if (colorMapType != 0) {
			if (colorMap.bits != 8 && colorMap.bits != 16 && colorMap.bits != 24 && colorMap.bits != 24)
				throw new IllegalArgumentException("Not a supported tga file.");

			colorMap.readMap(is, imageType % 8 == 3);
		} else {
			colorMap.setBits(imageSpec, imageType % 8 == 3);
		}
	}

	public void write(OutputStream os) throws IOException {
		os.write(idLength);
		os.write(colorMapType);
		os.write(imageType);
		colorMap.writeHeader(os);
		imageSpec.write(os);
	}

	public boolean getRleEncoded() {
		return imageType >= 8;
	}
}
