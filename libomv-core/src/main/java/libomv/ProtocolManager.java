/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Portions Copyright (c) 2006, Lateral Arts Limited
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

import static libomv.utils.Helpers.BytesToInt16L;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import libomv.types.PacketFrequency;
import libomv.utils.HashMapInt;

public class ProtocolManager {
	private static final Logger logger = Logger.getLogger(ProtocolManager.class);

	public static class FieldType {
		public static final int U8 = 0;

		public static final int U16 = 1;

		public static final int U32 = 2;

		public static final int U64 = 3;

		public static final int I8 = 4;

		public static final int I16 = 5;

		public static final int I32 = 6;

		public static final int I64 = 7;

		public static final int F32 = 8;

		public static final int F64 = 9;

		public static final int UUID = 10;

		public static final int BOOL = 11;

		public static final int Vector3 = 12;

		public static final int Vector3d = 13;

		public static final int Vector4 = 14;

		public static final int Quaternion = 15;

		public static final int IPADDR = 16;

		public static final int IPPORT = 17;

		public static final int Variable = 18;

		public static final int Fixed = 19;

		public static final int NumTypes = 20;

		public static final String[] TypeNames = { "U8", "U16", "U32", "U64", "S8", "S16", "S32", "S64", "F32", "F64",
				"LLUUID", "BOOL", "LLVector3", "LLVector3d", "LLVector4", "LLQuaternion", "IPADDR", "IPPORT",
				"Variable", "Fixed" };

		public static final short[] TypeSizes = { 1 /* U8 */, 2 /* U16 */, 4 /* U32 */, 8 /* U64 */, 1 /* I8 */,
				2 /* I16 */, 4 /* I32 */, 8 /* I64 */, 4 /* F32 */, 8 /* F64 */, 16 /* UUID */, 1 /* BOOL */,
				12 /* Vector3 */, 24 /* Vector3d */, 16 /* Vector4 */, 16 /* Quaternion */, 4 /* IPADDR */,
				2 /* IPPORT */, 0 /* Variable */, 0 /* Fixed */ };

		public static int getFieldType(String token) {
			int value = 0;
			for (int i = 0; i < TypeNames.length; i++) {
				if (FieldType.TypeNames[i].equals(token)) {
					value = i;
					break;
				}
			}
			return value;
		}
	}

	public class MapField implements Comparable<Object>, Cloneable {
		public int keywordIndex;

		public short offset;

		public int type;

		public short count;

		@Override
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		@Override
		public int compareTo(Object obj) {
			MapField temp = (MapField) obj;
			return keywordPosition(this.keywordIndex).compareTo(keywordPosition(temp.keywordIndex));
		}
	}

	public class MapBlock implements Comparable<Object>, Cloneable {
		public int keywordIndex;

		public short size;

		public short count;

		public List<MapField> fields;

		@Override
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		@Override
		public int compareTo(Object obj) {
			MapBlock temp = (MapBlock) obj;
			return keywordPosition(this.keywordIndex).compareTo(keywordPosition(temp.keywordIndex));
		}
	}

	public class MapPacket {

		public int id;

		public String name;

		public int frequency;

		public boolean trusted;

		public boolean encoded;

		public boolean deprecated;

		public List<MapBlock> blocks;
	}

	public class MapPacketMap {
		public List<MapPacket> mapPackets;

		public Map<Integer, MapPacket> commandMapPacket;

		public Map<String, MapPacket> nameMapPacket;

		public MapPacketMap(int size) {
			mapPackets = new ArrayList<>(size);
			commandMapPacket = new HashMap<>(size);
			nameMapPacket = new Hashtable<>(size);
		}

		public MapPacket getMapPacketByName(String name) {
			return nameMapPacket.get(name);
		}

		public MapPacket getMapPacketByCommand(int command) {
			return commandMapPacket.get(command);
		}

		public void addPacket(int id, MapPacket packet) {
			mapPackets.add(packet);
			commandMapPacket.put(id, packet);
			nameMapPacket.put(packet.name, packet);
		}
	}

	public MapPacketMap lowMaps;

	public MapPacketMap mediumMaps;

	public MapPacketMap highMaps;

	private boolean sort;
	private List<String> keywordList;
	private HashMapInt<String> keywordPositions;

	public ProtocolManager(String mapFile, boolean sort) throws Exception {
		this.sort = sort;

		// Initialize the map arrays
		lowMaps = new MapPacketMap(256);
		mediumMaps = new MapPacketMap(256);
		highMaps = new MapPacketMap(256);

		keywordPositions = new HashMapInt<>();
		keywordList = new ArrayList<>();
		loadMapFile(mapFile);
	}

	public MapPacket command(String command) throws Exception {
		// TODO: Get a hashtable in here quick!

		MapPacket map = highMaps.getMapPacketByName(command);
		if (map == null) {
			map = mediumMaps.getMapPacketByName(command);
			if (map == null) {
				map = lowMaps.getMapPacketByName(command);
			} else {
				throw new Exception("Cannot find map for command \"" + command + "\"");
			}
		}
		return map;
		/**
		 * TO BE PORTED // This will speed things up for now if
		 * (command.equals(LowMaps[65531].Name)) { return LowMaps[65531]; }
		 */
	}

	public MapPacket command(byte[] data) throws Exception {
		int command;

		if (data.length < 7) {
			return null;
		}

		if (data[6] == (byte) 0xFF) {
			if (data[7] == (byte) 0xFF) {
				// Low frequency
				command = (data[8] * 256 + data[9]);
				return command(command, PacketFrequency.Low);
			}

			// Medium frequency
			command = data[7];
			return command(command, PacketFrequency.Medium);
		}

		// High frequency
		command = data[6];
		return command(command, PacketFrequency.High);
	}

	public MapPacket command(int command, int frequency) throws Exception {
		switch (frequency) {
		case PacketFrequency.High:
			return highMaps.getMapPacketByCommand(command);
		case PacketFrequency.Medium:
			return mediumMaps.getMapPacketByCommand(command);
		case PacketFrequency.Low:
			return lowMaps.getMapPacketByCommand(command);
		default:
			break;
		}

		throw new Exception("Cannot find map for command \"" + command + "\" with frequency \"" + frequency + "\"");
	}

	public void printMap() {
		printOneMap(lowMaps, "Low   ");
		printOneMap(mediumMaps, "Medium");
		printOneMap(highMaps, "High  ");
	}

	private void printOneMap(MapPacketMap map, String frequency) {
		int i;

		for (i = 0; i < map.mapPackets.size(); ++i) {
			MapPacket map_packet = map.mapPackets.get(i);
			if (map_packet != null) {
				System.out.format("%s %d %d %4x - %s - %s - %s\n", frequency, i, map_packet.id, map_packet.frequency,
						map_packet.name, map_packet.trusted ? "Trusted" : "Untrusted",
						map_packet.encoded ? "Unencoded" : "Zerocoded");

				for (int j = 0; j < map_packet.blocks.size(); j++) {
					MapBlock block = map_packet.blocks.get(j);
					if (block.count == -1) {
						System.out.format("\t%4d %s (Variable)\n", block.keywordIndex,
								keywordPosition(block.keywordIndex));
					} else {
						System.out.format("\t%4d %s (%d)\n", block.keywordIndex, keywordPosition(block.keywordIndex),
								block.count);
					}

					for (int k = 0; k < block.fields.size(); k++) {
						MapField field = block.fields.get(k);
						System.out.format("\t\t%4d %s (%d / %d)", field.keywordIndex,
								keywordPosition(block.keywordIndex), field.type, field.count);
					}
				}
			}
		}
	}

	public static void decodeMapFile(String mapFile, String outputFile) throws Exception {
		byte magicKey = 0;
		byte[] buffer = new byte[2048];
		int nread;
		InputStream map;
		OutputStream output;

		try {
			map = new FileInputStream(mapFile);
			try {
				output = new FileOutputStream(outputFile);
			} catch (Exception e) {
				map.close();
				throw new Exception("Map file error, can't create output file", e);
			}
		} catch (Exception e) {
			throw new Exception("Map file error, can't open map file", e);
		}

		while ((nread = map.read(buffer, 0, 2048)) != 0) {
			for (int i = 0; i < nread; ++i) {
				buffer[i] ^= magicKey;
				magicKey += 43;
			}

			output.write(buffer, 0, nread);
		}

		map.close();
		output.close();
	}

	private void loadMapFile(String mapFile) throws Exception {
		FileReader map;
		int low = 1;
		int medium = 1;
		int high = 1;

		// Load the protocol map file
		try {
			map = new FileReader(mapFile);
		} catch (Exception e) {
			throw new Exception("Map file error", e);
		}

		try {
			BufferedReader r = new BufferedReader(map);
			String newline;
			String trimmedline;
			boolean inPacket = false;
			boolean inBlock = false;
			MapPacket currentPacket = null;
			MapBlock currentBlock = null;
			short fieldOffset = 0;

			while (r.ready()) {
				newline = r.readLine();
				trimmedline = newline.trim();

				if (!inPacket) {
					// Outside of all packet blocks

					if (trimmedline.equals("{")) {
						inPacket = true;
					}
				} else {
					// Inside of a packet block

					if (!inBlock) {
						// Inside a packet block, outside of the blocks

						if (trimmedline.equals("{")) {
							inBlock = true;
							fieldOffset = 0;
						} else if (trimmedline.equals("}")) {
							// Reached the end of the packet
							if (sort)
								Collections.sort(currentPacket.blocks);
							inPacket = false;
						} else if (trimmedline.startsWith("//")) {
							// ignore comment lines
						} else {
							// The packet header
							// #region ParsePacketHeader

							// Splice the String in to tokens
							String[] tokens = trimmedline.split("\\s+");

							if (tokens.length > 3) {
								if (tokens[1].equals("Fixed")) {
									// Remove the leading "0x"
									if (tokens[2].substring(0, 2).equals("0x")) {
										tokens[2] = tokens[2].substring(2, tokens[2].length());
									}

									long l_fixedID = Long.parseLong(tokens[2], 16);
									// Truncate the id to a short
									int fixedID = (int) (l_fixedID ^ 0xFFFF0000);
									currentPacket = new MapPacket();
									currentPacket.id = fixedID;
									currentPacket.frequency = PacketFrequency.Low;
									currentPacket.name = tokens[0];
									currentPacket.trusted = tokens[3].equals("Trusted");
									currentPacket.encoded = tokens[4].equals("Zerocoded");
									currentPacket.deprecated = tokens.length > 5 ? tokens[5].contains("Deprecated")
											: false;
									currentPacket.blocks = new ArrayList<>();
									lowMaps.addPacket(fixedID, currentPacket);
								} else if (tokens[1].equals("Low")) {
									currentPacket = new MapPacket();
									currentPacket.id = low;
									currentPacket.frequency = PacketFrequency.Low;
									currentPacket.name = tokens[0];
									currentPacket.trusted = tokens[2].equals("Trusted");
									currentPacket.encoded = tokens[3].equals("Zerocoded");
									currentPacket.deprecated = tokens.length > 4 ? tokens[4].contains("Deprecated")
											: false;
									currentPacket.blocks = new ArrayList<>();
									lowMaps.addPacket(low, currentPacket);
									low++;
								} else if (tokens[1].equals("Medium")) {
									currentPacket = new MapPacket();
									currentPacket.id = medium;
									currentPacket.frequency = PacketFrequency.Medium;
									currentPacket.name = tokens[0];
									currentPacket.trusted = tokens[2].equals("Trusted");
									currentPacket.encoded = tokens[3].equals("Zerocoded");
									currentPacket.deprecated = tokens.length > 4 ? tokens[4].contains("Deprecated")
											: false;
									currentPacket.blocks = new ArrayList<>();
									mediumMaps.addPacket(medium, currentPacket);

									medium++;
								} else if (tokens[1].equals("High")) {
									currentPacket = new MapPacket();
									currentPacket.id = high;
									currentPacket.frequency = PacketFrequency.High;
									currentPacket.name = tokens[0];
									currentPacket.trusted = tokens[2].equals("Trusted");
									currentPacket.encoded = tokens[3].equals("Zerocoded");
									currentPacket.deprecated = tokens.length > 4 ? tokens[4].contains("Deprecated")
											: false;
									currentPacket.blocks = new ArrayList<>();
									highMaps.addPacket(high, currentPacket);

									high++;
								} else {
									logger.error("Unknown packet frequency : " + tokens[1]);
								}
							}
						}
					} else {
						if (trimmedline.length() > 0 && trimmedline.substring(0, 1).equals("{")) {
							// A field
							MapField field = new MapField();

							// Splice the String in to tokens
							String[] tokens = trimmedline.split("\\s+");

							field.keywordIndex = keywordPosition(tokens[1]);
							field.type = FieldType.getFieldType(tokens[2]);
							field.offset = fieldOffset;

							if (tokens[3].equals("}")) {
								field.count = 1;
								if (fieldOffset >= 0) {
									fieldOffset = getFieldSize(field, null, (short) 0);
								}
							} else {
								field.count = Short.parseShort(tokens[3]);
								if (fieldOffset >= 0) {
									if (field.type == FieldType.Variable)
										fieldOffset = -1;
									else if (field.type == FieldType.Fixed)
										fieldOffset += field.count;
								}
							}

							// Save this field to the current block
							currentBlock.fields.add(field);
						} else if (trimmedline.equals("}")) {
							if (sort)
								Collections.sort(currentBlock.fields);
							currentBlock.size = fieldOffset;
							inBlock = false;
						} else if (trimmedline.length() != 0 && trimmedline.substring(0, 2).equals("//") == false) {
							// The block header
							// #region ParseBlockHeader

							currentBlock = new MapBlock();

							// Splice the String in to tokens
							String[] tokens = trimmedline.split("\\s+");

							currentBlock.keywordIndex = keywordPosition(tokens[0]);
							currentBlock.fields = new ArrayList<>();
							currentPacket.blocks.add(currentBlock);

							if (tokens[1].equals("Single")) {
								currentBlock.count = 1;
							} else if (tokens[1].equals("Multiple")) {
								currentBlock.count = Short.parseShort(tokens[2]);
							} else if (tokens[1].equals("Variable")) {
								currentBlock.count = -1;
							} else {
								logger.error("Unknown block frequency");
							}
							// #endregion
						}
					}
				}

				// #endregion
			}

			r.close();
			map.close();
		} catch (Exception e) {
			throw e;
		}
	}

	private short getBlockSize(List<MapField> fields, byte[] message, short offset) throws Exception {
		short start = offset;
		for (MapField field : fields) {
			offset += getFieldSize(field, message, offset);
		}
		return (short) (offset - start);
	}

	public short getBlockNum(MapPacket packet, byte[] message, int blockIndex) throws Exception {
		short blocks, offset = 0;
		for (MapBlock block : packet.blocks) {
			if (block.count >= 0) {
				blocks = block.count;
			} else {
				blocks = message[offset++];
			}

			if (block.keywordIndex == blockIndex) {
				return blocks;
			}

			if (block.size >= 0) {
				offset += block.size * blocks;
			} else {
				for (int j = 0; j < blocks; j++) {
					offset += getBlockSize(block.fields, message, offset);
				}
			}
		}
		throw new Exception("Block index not found!");
	}

	public MapField getFieldOffset(MapPacket packet, byte[] message, int blockIndex, int fieldIndex, short blockNumber)
			throws Exception {
		short blocks, offset = 0;
		for (MapBlock block : packet.blocks) {
			if (block.count >= 0) {
				blocks = block.count;
			} else {
				blocks = message[offset++];
			}

			if (block.keywordIndex == blockIndex && blockNumber < blocks) {
				blocks = blockNumber;
			}

			if (blocks > 0) {
				if (block.size >= 0) {
					offset += block.size * blocks;
				} else {
					for (int j = 0; j < blocks; j++) {
						offset += getBlockSize(block.fields, message, offset);
					}
				}
			}

			if (block.keywordIndex == blockIndex) {
				MapField result = null;
				for (MapField field : block.fields) {
					if (field.keywordIndex == fieldIndex) {
						result = (MapField) field.clone();
						result.offset = offset;
						break;
					}
					offset += getFieldSize(field, message, offset);
				}
				return result;
			}
		}
		throw new Exception("Block and or Field index not found!");
	}

	public short getFieldSize(MapField field, byte[] message, short offset) throws Exception {
		if (field.type == FieldType.Fixed) {
			return field.count;
		} else if (field.type == FieldType.Variable) {
			if (field.count == 1)
				return (short) (message[offset] + 1);
			else if (field.count == 2)
				// TODO:FIXME
				// Kinda awkward, this is the only reason Helpers is included :(
				return (short) (BytesToInt16L(message, offset) + 2);
			else
				throw new Exception("Invalid count for variable sized field!");
		}
		return FieldType.TypeSizes[field.type];
	}

	public String keywordPosition(int position) {
		if (position >= 0 && position < keywordList.size()) {
			return keywordList.get(position);
		}
		return null;
	}

	public int keywordPosition(String keyword) {
		if (keywordPositions.containsKey(keyword)) {
			return keywordPositions.get(keyword);
		}

		int position = keywordList.size();
		keywordList.add(keyword);
		keywordPositions.put(keyword, position);
		return position;
	}

}
