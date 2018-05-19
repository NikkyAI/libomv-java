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
package libomv;

import org.apache.log4j.Logger;

import libomv.model.terrain.GroupHeader;
import libomv.model.terrain.LayerType;
import libomv.model.terrain.TerrainHeader;
import libomv.model.terrain.TerrainPatch;
import libomv.packets.LayerDataPacket;
import libomv.utils.BitPack;
import libomv.utils.Helpers;

public class TerrainCompressor {
	private static final Logger logger = Logger.getLogger(TerrainCompressor.class);

	// TODO:FIXME
	// Shouldn't all these values be final???
	public static byte PATCHES_PER_EDGE = 16;
	public static byte END_OF_PATCHES = 97;

	private static float OO_SQRT2 = 0.7071067811865475244008443621049f;
	private static int STRIDE = 264;

	private static byte ZERO_CODE = 0x0;
	private static byte ZERO_EOB = 0x2;
	private static byte POSITIVE_VALUE = 0x6;
	private static byte NEGATIVE_VALUE = 0x7;

	private static float[] DequantizeTable16 = new float[16 * 16];
	private static float[] DequantizeTable32 = new float[16 * 16];
	private static float[] CosineTable16 = new float[16 * 16];
	// private static readonly float[] CosineTable32 = new float[16 * 16];
	private static int[] CopyMatrix16 = new int[16 * 16];
	private static int[] CopyMatrix32 = new int[16 * 16];
	private static float[] QuantizeTable16 = new float[16 * 16];

	static {
		// Initialize the decompression tables
		buildDequantizeTable16();
		setupCosines16();
		buildCopyMatrix16();
		buildQuantizeTable16();
	}

	public static LayerDataPacket createLayerDataPacket(TerrainPatch[] patches, LayerType type) throws Exception {
		LayerDataPacket layer = new LayerDataPacket();
		layer.Type = type.getValue();

		GroupHeader header = new GroupHeader();
		header.stride = STRIDE;
		header.patchSize = 16;
		header.type = type;

		// Should be enough to fit even the most poorly packed data
		byte[] data = new byte[patches.length * 16 * 16 * 2];
		BitPack bitpack = new BitPack(data, 0);
		bitpack.packBits(Helpers.UInt16ToBytesL(header.stride), 16);
		bitpack.packBits(header.patchSize, 8);
		bitpack.packBits(header.type.getValue(), 8);

		for (int j = 0; j < patches.length; j++)
			createPatch(bitpack, patches[j].Data, patches[j].X, patches[j].Y);

		bitpack.packBits(END_OF_PATCHES, 8);

		layer.LayerData.setData(bitpack.getData());
		return layer;
	}

	/**
	 * Creates a LayerData packet for compressed land data given a full simulator
	 * heightmap and an array of indices of patches to compress
	 *
	 * @param heightmap
	 *            A 256 * 256 array of floating point values specifying the height
	 *            at each meter in the simulator</param>
	 * @param patches
	 *            Array of indexes in the 16x16 grid of patches for this simulator.
	 *            For example if 1 and 17 are specified, patches x=1,y=0 and x=1,y=1
	 *            are sent</param>
	 * @throws Exception
	 * @returns the layer data packet
	 */
	public static LayerDataPacket createLandPacket(float[] heightmap, int[] patches) throws Exception {
		LayerDataPacket layer = new LayerDataPacket();
		layer.Type = LayerType.Land.getValue();

		GroupHeader header = new GroupHeader();
		header.stride = STRIDE;
		header.patchSize = 16;
		header.type = LayerType.Land;

		byte[] data = new byte[1536];
		BitPack bitpack = new BitPack(data, 0);
		bitpack.packBits(Helpers.UInt16ToBytesL(header.stride), 16);
		bitpack.packBits(header.patchSize, 8);
		bitpack.packBits(header.type.getValue(), 8);

		for (int j = 0; j < patches.length; j++)
			createPatchFromHeightmap(bitpack, heightmap, patches[j] % 16, (patches[j] - (patches[j] % 16)) / 16);

		bitpack.packBits(END_OF_PATCHES, 8);

		layer.LayerData.setData(bitpack.getData());
		return layer;
	}

	public static LayerDataPacket createLandPacket(float[] patchData, int x, int y) throws Exception {
		LayerDataPacket layer = new LayerDataPacket();
		layer.Type = LayerType.Land.getValue();

		GroupHeader header = new GroupHeader();
		header.stride = STRIDE;
		header.patchSize = 16;
		header.type = LayerType.Land;

		byte[] data = new byte[1536];
		BitPack bitpack = new BitPack(data, 0);
		bitpack.packBits(Helpers.UInt16ToBytesL(header.stride), 16);
		bitpack.packBits(header.patchSize, 8);
		bitpack.packBits(header.type.getValue(), 8);

		createPatch(bitpack, patchData, x, y);

		bitpack.packBits(END_OF_PATCHES, 8);

		layer.LayerData.setData(bitpack.getData());
		return layer;
	}

	public static void createPatch(BitPack bitpack, float[] patchData, int x, int y) {
		if (patchData.length != 16 * 16)
			throw new IllegalArgumentException("Patch data must be a 16x16 array");

		TerrainHeader header = prescanPatch(patchData);
		header.QuantWBits = 136;
		header.PatchIDs = (y & 0x1F);
		header.PatchIDs += (x << 5);

		// NOTE: No idea what prequant and postquant should be or what they do
		int[] patch = compressPatch(patchData, header, 10);
		int wbits = encodePatchHeader(bitpack, header, patch);
		encodePatch(bitpack, patch, 0, wbits);
	}

	/**
	 * Add a patch of terrain to a BitPacker
	 *
	 * @param output
	 *            BitPacker to write the patch to
	 * @param heightmap
	 *            Heightmap of the simulator, must be a 256 * 256 float array
	 * @param x
	 *            X offset of the patch to create, valid values are from 0 to 15
	 * @param y
	 *            Y offset of the patch to create, valid values are from 0 to 15
	 */
	public static void createPatchFromHeightmap(BitPack bitpack, float[] heightmap, int x, int y) {
		if (heightmap.length != 256 * 256)
			throw new IllegalArgumentException("Heightmap data must be 256x256");

		if (x < 0 || x > 15 || y < 0 || y > 15)
			throw new IllegalArgumentException("X and Y patch offsets must be from 0 to 15");

		TerrainHeader header = prescanPatch(heightmap, x, y);
		header.QuantWBits = 136;
		header.PatchIDs = (y & 0x1F);
		header.PatchIDs += (x << 5);

		// NOTE: No idea what prequant and postquant should be or what they do
		int[] patch = compressPatch(heightmap, x, y, header, 10);
		int wbits = encodePatchHeader(bitpack, header, patch);
		encodePatch(bitpack, patch, 0, wbits);
	}

	private static TerrainHeader prescanPatch(float[] patch) {
		TerrainHeader header = new TerrainHeader();
		float zmax = -99999999.0f;
		float zmin = 99999999.0f;

		for (int j = 0; j < 16; j++) {
			for (int i = 0; i < 16; i++) {
				float val = patch[j * 16 + i];
				if (val > zmax)
					zmax = val;
				if (val < zmin)
					zmin = val;
			}
		}

		header.DCOffset = zmin;
		header.Range = (int) ((zmax - zmin) + 1.0f);

		return header;
	}

	private static TerrainHeader prescanPatch(float[] heightmap, int patchX, int patchY) {
		TerrainHeader header = new TerrainHeader();
		float zmax = -99999999.0f;
		float zmin = 99999999.0f;

		for (int j = patchY * 16; j < (patchY + 1) * 16; j++) {
			for (int i = patchX * 16; i < (patchX + 1) * 16; i++) {
				float val = heightmap[j * 256 + i];
				if (val > zmax)
					zmax = val;
				if (val < zmin)
					zmin = val;
			}
		}

		header.DCOffset = zmin;
		header.Range = (int) ((zmax - zmin) + 1.0f);

		return header;
	}

	public static TerrainHeader decodePatchHeader(BitPack bitpack) {
		TerrainHeader header = new TerrainHeader();

		// Quantized word bits
		header.QuantWBits = bitpack.unpackBits(8);
		if (header.QuantWBits == END_OF_PATCHES)
			return header;

		// DC offset
		header.DCOffset = bitpack.unpackFloat();

		// Range
		header.Range = bitpack.unpackBits(16);

		// Patch IDs (10 bits)
		header.PatchIDs = bitpack.unpackBits(10);

		// Word bits
		header.WordBits = ((header.QuantWBits & 0x0f) + 2);

		return header;
	}

	private static int encodePatchHeader(BitPack bitpack, TerrainHeader header, int[] patch) {
		int temp;
		int wbits = (header.QuantWBits & 0x0f) + 2;
		long maxWbits = wbits + 5;
		long minWbits = wbits >> 1;

		wbits = (int) minWbits;

		for (int j = 0; j < patch.length; j++) {
			temp = patch[j];

			if (temp != 0) {
				// Get the absolute value
				if (temp < 0)
					temp *= -1;

				for (int k = (int) maxWbits; k > (int) minWbits; k--) {
					if ((temp & (1 << k)) != 0) {
						if (k > wbits)
							wbits = k;
						break;
					}
				}
			}
		}

		wbits += 1;

		header.QuantWBits &= 0xf0;

		if (wbits > 17 || wbits < 2) {
			logger.error("Bits needed per word in EncodePatchHeader() are outside the allowed range");
		}

		header.QuantWBits |= (wbits - 2);

		bitpack.packBits(header.QuantWBits, 8);
		bitpack.packFloat(header.DCOffset);
		bitpack.packBits(header.Range, 16);
		bitpack.packBits(header.PatchIDs, 10);

		return wbits;
	}

	private static void IDCTColumn16(float[] linein, float[] lineout, int column) {
		float total;
		int usize;

		for (int n = 0; n < 16; n++) {
			total = OO_SQRT2 * linein[column];

			for (int u = 1; u < 16; u++) {
				usize = u * 16;
				total += linein[usize + column] * CosineTable16[usize + n];
			}

			lineout[16 * n + column] = total;
		}
	}

	private static void IDCTLine16(float[] linein, float[] lineout, int line) {
		float oosob = 2.0f / 16.0f;
		int lineSize = line * 16;
		float total;

		for (int n = 0; n < 16; n++) {
			total = OO_SQRT2 * linein[lineSize];

			for (int u = 1; u < 16; u++) {
				total += linein[lineSize + u] * CosineTable16[u * 16 + n];
			}

			lineout[lineSize + n] = total * oosob;
		}
	}

	private static void DCTLine16(float[] linein, float[] lineout, int line) {
		float total = 0.0f;
		int lineSize = line * 16;

		for (int n = 0; n < 16; n++) {
			total += linein[lineSize + n];
		}

		lineout[lineSize] = OO_SQRT2 * total;

		for (int u = 1; u < 16; u++) {
			total = 0.0f;

			for (int n = 0; n < 16; n++) {
				total += linein[lineSize + n] * CosineTable16[u * 16 + n];
			}

			lineout[lineSize + u] = total;
		}
	}

	private static void DCTColumn16(float[] linein, int[] lineout, int column) {
		float total = 0.0f;
		float oosob = 2.0f / 16.0f;

		for (int n = 0; n < 16; n++) {
			total += linein[16 * n + column];
		}

		lineout[CopyMatrix16[column]] = (int) (OO_SQRT2 * total * oosob * QuantizeTable16[column]);

		for (int u = 1; u < 16; u++) {
			total = 0.0f;

			for (int n = 0; n < 16; n++) {
				total += linein[16 * n + column] * CosineTable16[u * 16 + n];
			}

			lineout[CopyMatrix16[16 * u + column]] = (int) (total * oosob * QuantizeTable16[16 * u + column]);
		}
	}

	public static void decodePatch(int[] patches, BitPack bitpack, TerrainHeader header, int size) {
		int temp;
		for (int n = 0; n < size * size; n++) {
			// ?
			temp = bitpack.unpackBits(1);
			if (temp != 0) {
				// Value or EOB
				temp = bitpack.unpackBits(1);
				if (temp != 0) {
					// Value
					temp = bitpack.unpackBits(1);
					if (temp != 0) {
						// Negative
						temp = bitpack.unpackBits(header.WordBits);
						patches[n] = temp * -1;
					} else {
						// Positive
						temp = bitpack.unpackBits(header.WordBits);
						patches[n] = temp;
					}
				} else {
					// Set the rest to zero
					// TODO: This might not be necessary
					for (int o = n; o < size * size; o++) {
						patches[o] = 0;
					}
					break;
				}
			} else {
				patches[n] = 0;
			}
		}
	}

	private static void encodePatch(BitPack output, int[] patch, int postquant, int wbits) {
		int temp;
		boolean eob;

		if (postquant > 16 * 16 || postquant < 0) {
			logger.error("Postquant is outside the range of allowed values in EncodePatch()");
			return;
		}

		if (postquant != 0)
			patch[16 * 16 - postquant] = 0;

		for (int i = 0; i < 16 * 16; i++) {
			eob = false;
			temp = patch[i];

			if (temp == 0) {
				eob = true;

				for (int j = i; j < 16 * 16 - postquant; j++) {
					if (patch[j] != 0) {
						eob = false;
						break;
					}
				}

				if (eob) {
					output.packBits(ZERO_EOB, 2);
					return;
				}
				output.packBits(ZERO_CODE, 1);
			} else {
				if (temp < 0) {
					temp *= -1;

					if (temp > (1 << wbits))
						temp = (1 << wbits);

					output.packBits(NEGATIVE_VALUE, 3);
					output.packBits(temp, wbits);
				} else {
					if (temp > (1 << wbits))
						temp = (1 << wbits);

					output.packBits(POSITIVE_VALUE, 3);
					output.packBits(temp, wbits);
				}
			}
		}
	}

	public static float[] decompressPatch(int[] patches, TerrainHeader header, GroupHeader group) {
		float[] block = new float[group.patchSize * group.patchSize];
		float[] output = new float[group.patchSize * group.patchSize];
		int prequant = (header.QuantWBits >> 4) + 2;
		int quantize = 1 << prequant;
		float ooq = 1.0f / quantize;
		float mult = ooq * header.Range;
		float addval = mult * (1 << (prequant - 1)) + header.DCOffset;

		if (group.patchSize == 16) {
			for (int n = 0; n < 16 * 16; n++) {
				block[n] = patches[CopyMatrix16[n]] * DequantizeTable16[n];
			}

			float[] ftemp = new float[16 * 16];

			for (int o = 0; o < 16; o++)
				IDCTColumn16(block, ftemp, o);
			for (int o = 0; o < 16; o++)
				IDCTLine16(ftemp, block, o);
		} else {
			for (int n = 0; n < 32 * 32; n++) {
				block[n] = patches[CopyMatrix32[n]] * DequantizeTable32[n];
			}

			logger.error("Implement IDCTPatchLarge");
		}

		for (int j = 0; j < block.length; j++) {
			output[j] = block[j] * mult + addval;
		}

		return output;
	}

	private static int[] compressPatch(float[] patchData, TerrainHeader header, int prequant) {
		float[] block = new float[16 * 16];
		int wordsize = prequant;
		float oozrange = 1.0f / header.Range;
		float range = (1 << prequant);
		float premult = oozrange * range;
		float sub = (1 << (prequant - 1)) + header.DCOffset * premult;

		header.QuantWBits = wordsize - 2;
		header.QuantWBits |= (prequant - 2) << 4;

		int k = 0;
		for (int j = 0; j < 16; j++) {
			for (int i = 0; i < 16; i++)
				block[k++] = patchData[j * 16 + i] * premult - sub;
		}

		float[] ftemp = new float[16 * 16];
		int[] itemp = new int[16 * 16];

		for (int o = 0; o < 16; o++)
			DCTLine16(block, ftemp, o);
		for (int o = 0; o < 16; o++)
			DCTColumn16(ftemp, itemp, o);

		return itemp;
	}

	private static int[] compressPatch(float[] heightmap, int patchX, int patchY, TerrainHeader header, int prequant) {
		float[] block = new float[16 * 16];
		int wordsize = prequant;
		float oozrange = 1.0f / header.Range;
		float range = (1 << prequant);
		float premult = oozrange * range;
		float sub = (1 << (prequant - 1)) + header.DCOffset * premult;

		header.QuantWBits = wordsize - 2;
		header.QuantWBits |= (prequant - 2) << 4;

		int k = 0;
		for (int j = patchY * 16; j < (patchY + 1) * 16; j++) {
			for (int i = patchX * 16; i < (patchX + 1) * 16; i++)
				block[k++] = heightmap[j * 256 + i] * premult - sub;
		}

		float[] ftemp = new float[16 * 16];
		int[] itemp = new int[16 * 16];

		for (int o = 0; o < 16; o++)
			DCTLine16(block, ftemp, o);
		for (int o = 0; o < 16; o++)
			DCTColumn16(ftemp, itemp, o);

		return itemp;
	}

	// #region Initialization

	private static void buildDequantizeTable16() {
		for (int j = 0; j < 16; j++) {
			for (int i = 0; i < 16; i++) {
				DequantizeTable16[j * 16 + i] = 1.0f + 2.0f * (i + j);
			}
		}
	}

	private static void buildQuantizeTable16() {
		for (int j = 0; j < 16; j++) {
			for (int i = 0; i < 16; i++) {
				QuantizeTable16[j * 16 + i] = 1.0f / (1.0f + 2.0f * ((float) i + (float) j));
			}
		}
	}

	private static void setupCosines16() {
		float hposz = (float) Math.PI * 0.5f / 16.0f;

		for (int u = 0; u < 16; u++) {
			for (int n = 0; n < 16; n++) {
				CosineTable16[u * 16 + n] = (float) Math.cos((2.0f * n + 1.0f) * u * hposz);
			}
		}
	}

	private static void buildCopyMatrix16() {
		boolean diag = false;
		boolean right = true;
		int i = 0;
		int j = 0;
		int count = 0;

		while (i < 16 && j < 16) {
			CopyMatrix16[j * 16 + i] = count++;

			if (!diag) {
				if (right) {
					if (i < 16 - 1)
						i++;
					else
						j++;

					right = false;
					diag = true;
				} else {
					if (j < 16 - 1)
						j++;
					else
						i++;

					right = true;
					diag = true;
				}
			} else {
				if (right) {
					i++;
					j--;
					if (i == 16 - 1 || j == 0)
						diag = false;
				} else {
					i--;
					j++;
					if (j == 16 - 1 || i == 0)
						diag = false;
				}
			}
		}
	}

	// #endregion Initialization
}
