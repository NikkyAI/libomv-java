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
package libomv.mapgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import libomv.ProtocolManager;
import libomv.ProtocolManager.FieldType;
import libomv.ProtocolManager.MapBlock;
import libomv.ProtocolManager.MapField;
import libomv.ProtocolManager.MapPacket;
import libomv.types.PacketFrequency;

public class mapgenerator {
	private static final Logger logger = Logger.getLogger(mapgenerator.class);

	static String spaces = new String("                        ");

	static String FieldTypeString(int type) {
		switch (type) {
		case FieldType.BOOL:
			return "boolean";
		case FieldType.F32:
			return "float";
		case FieldType.F64:
			return "double";
		case FieldType.IPPORT:
		case FieldType.U16:
			return "short";
		case FieldType.IPADDR:
		case FieldType.U32:
			return "int";
		case FieldType.Quaternion:
			return "Quaternion";
		case FieldType.UUID:
			return "UUID";
		case FieldType.Vector3:
			return "Vector3";
		case FieldType.Vector3d:
			return "Vector3d";
		case FieldType.Vector4:
			return "Vector4";
		case FieldType.I16:
			return "short";
		case FieldType.I32:
			return "int";
		case FieldType.I8:
			return "byte";
		case FieldType.U64:
			return "long";
		case FieldType.U8:
			return "byte";
		case FieldType.Fixed:
			return "byte[]";
		default:
			break;
		}
		return null;
	}

	static String FieldInitString(int type) {
		switch (type) {
		case FieldType.BOOL:
			return "false";
		case FieldType.I8:
		case FieldType.I16:
		case FieldType.I32:
		case FieldType.U8:
		case FieldType.U16:
		case FieldType.U32:
		case FieldType.U64:
		case FieldType.IPPORT:
		case FieldType.IPADDR:
		case FieldType.F32:
		case FieldType.F64:
			return "0";
		case FieldType.Quaternion:
		case FieldType.UUID:
		case FieldType.Vector3:
		case FieldType.Vector3d:
		case FieldType.Vector4:
		case FieldType.Fixed:
			return "null";
		default:
			break;
		}
		return null;
	}

	static void WriteFieldMember(PrintWriter writer, String fieldName, MapField field) {
		if (field.type != FieldType.Variable) {
			writer.println("        public " + FieldTypeString(field.type) + " " + fieldName + " = "
					+ FieldInitString(field.type) + ";");
		} else {
			writer.println("        private byte[] _" + fieldName.toLowerCase() + ";");
			writer.println("        public byte[] get" + fieldName + "() {");
			writer.println("            return _" + fieldName.toLowerCase() + ";");
			writer.println("        }\n");
			writer.println("        public void set" + fieldName + "(byte[] value) throws Exception {");
			writer.println("            if (value == null) {");
			writer.println("                _" + fieldName.toLowerCase() + " = null;");
			writer.println("            }");
			writer.println("            else if (value.length > " + ((field.count == 1) ? "255" : "1024") + ") {");
			writer.println("                throw new OverflowException(\"Value exceeds "
					+ ((field.count == 1) ? "255" : "1024") + " characters\");");
			writer.println("            }");
			writer.println("            else {");
			writer.println("                _" + fieldName.toLowerCase() + " = new byte[value.length];");
			writer.println(
					"                System.arraycopy(value, 0, _" + fieldName.toLowerCase() + ", 0, value.length);");
			writer.println("            }");
			writer.println("        }\n");
		}
	}

	static void WriteFieldFromBytes(PrintWriter writer, int indent, String fieldName, MapField field, String arrayName,
			String index) {
		String lead = spaces.substring(0, indent);
		writer.write(lead);

		switch (field.type) {
		case FieldType.BOOL:
			writer.println(fieldName + index + " = (" + arrayName + ".get() != 0) ? (boolean)true : (boolean)false;");
			break;
		case FieldType.F32:
			writer.println(fieldName + index + " = " + arrayName + ".getFloat();");
			break;
		case FieldType.F64:
			writer.println(fieldName + index + " = " + arrayName + ".getDouble();");
			break;
		case FieldType.Fixed:
			writer.println(fieldName + index + " = new byte[" + field.count + "];");
			writer.println(lead + "" + arrayName + ".get(" + fieldName + index + ");");
			break;
		case FieldType.IPADDR:
		case FieldType.U32:
			writer.println(fieldName + index + " = " + arrayName + ".getInt();");
			break;
		case FieldType.IPPORT:
			// IPPORT is big endian while U16/S16 are little endian.
			writer.println(fieldName + index + " = (short)((" + arrayName + ".get() << 8) + " + arrayName + ".get());");
			break;
		case FieldType.U16:
			writer.println(fieldName + index + " = " + arrayName + ".getShort();");
			break;
		case FieldType.Quaternion:
			writer.println(fieldName + index + " = new Quaternion(" + arrayName + ", true);");
			break;
		case FieldType.UUID:
			writer.println(fieldName + index + " = new UUID(" + arrayName + ");");
			break;
		case FieldType.Vector3:
			writer.println(fieldName + index + " = new Vector3(" + arrayName + ");");
			break;
		case FieldType.Vector3d:
			writer.println(fieldName + index + " = new Vector3d(" + arrayName + ");");
			break;
		case FieldType.Vector4:
			writer.println(fieldName + index + " = new Vector4(" + arrayName + ");");
			break;
		case FieldType.I16:
			writer.println(fieldName + index + " = " + arrayName + ".getShort();");
			break;
		case FieldType.I32:
			writer.println(fieldName + index + " = " + arrayName + ".getInt();");
			break;
		case FieldType.I8:
			writer.println(fieldName + index + " = " + arrayName + ".get();");
			break;
		case FieldType.U64:
			writer.println(fieldName + index + " = " + arrayName + ".getLong();");
			break;
		case FieldType.U8:
			writer.println(fieldName + index + " = " + arrayName + ".get();");
			break;
		case FieldType.Variable:
			if (field.count == 1) {
				writer.println("length = " + arrayName + ".get() & 0xFF;");
			} else {
				writer.println("length = " + arrayName + ".getShort() & 0xFFFF;");
			}
			writer.println(lead + "_" + fieldName.toLowerCase() + " = new byte[length];");
			writer.println(lead + "" + arrayName + ".get(_" + fieldName.toLowerCase() + ");");
			break;
		default:
			writer.println("!!! ERROR: Unhandled FieldType: " + field.type + " !!!");
			break;
		}
	}

	static void WriteFieldToBytes(PrintWriter writer, int indent, String fieldName, MapField field, String arrayName,
			String index) {
		String lead = spaces.substring(0, indent);
		writer.write(lead);

		switch (field.type) {
		case FieldType.BOOL:
			writer.println(arrayName + ".put((byte)((" + fieldName + index + ") ? 1 : 0));");
			break;
		case FieldType.F32:
			writer.println(arrayName + ".putFloat(" + fieldName + index + ");");
			break;
		case FieldType.F64:
			writer.println(arrayName + ".putDouble(" + fieldName + index + ");");
			break;
		case FieldType.Fixed:
			writer.println(arrayName + ".put(" + fieldName + index + ");");
			break;
		case FieldType.IPPORT:
			// IPPORT is big endian while U16/S16 is little endian.
			writer.println(arrayName + ".put((byte)((" + fieldName + index + " >> 8) % 256));");
			writer.println(lead + arrayName + ".put((byte)(" + fieldName + index + " % 256));");
			break;
		case FieldType.U16:
		case FieldType.I16:
			writer.println(arrayName + ".putShort(" + fieldName + index + ");");
			break;
		case FieldType.UUID:
		case FieldType.Vector4:
		case FieldType.Quaternion:
		case FieldType.Vector3:
		case FieldType.Vector3d:
			writer.println(fieldName + index + ".write(" + arrayName + ");");
			break;
		case FieldType.U8:
		case FieldType.I8:
			writer.println(arrayName + ".put(" + fieldName + index + ");");
			break;
		case FieldType.IPADDR:
		case FieldType.U32:
		case FieldType.I32:
			writer.println(arrayName + ".putInt(" + fieldName + index + ");");
			break;
		case FieldType.U64:
			writer.println(arrayName + ".putLong(" + fieldName + index + ");");
			break;
		case FieldType.Variable:
			if (field.count == 1) {
				writer.print(arrayName + ".put((byte)_");
			} else {
				writer.print(arrayName + ".putShort((short)_");
			}
			String varName = fieldName.toLowerCase();
			writer.println(varName + ".length);\n" + lead + arrayName + ".put(_" + varName + ");");
			break;
		default:
			writer.println("!!! ERROR: Unhandled FieldType: " + field.type + " !!!");
			break;
		}
	}

	static int GetFieldLength(PrintWriter writer, ProtocolManager protocol, MapField field) {
		short len = FieldType.TypeSizes[field.type];
		if (len < 0)
			writer.println("!!! ERROR: Unhandled FieldType " + field.type + " !!!");
		return len;
	}

	static void WriteFieldToString(PrintWriter writer, int indent, String fieldName, MapField field, String index) {
		String lead = spaces.substring(0, indent);
		String index1 = new String(), index2 = new String();
		if (!index.isEmpty()) {
			index1 = "[\" + " + index + " + \"]";
			index2 = "[" + index + "]";
		}
		writer.write(lead);

		switch (field.type) {
		case FieldType.Variable:
			writer.println("output += Helpers.FieldToString(_" + fieldName.toLowerCase() + ", \"" + fieldName
					+ "\") + \"\\n\";");
			break;
		case FieldType.Fixed:
			writer.println(
					"output += Helpers.FieldToString(" + fieldName + ", \"" + fieldName + index2 + "\") + \"\\n\";");
			break;
		case FieldType.BOOL:
			writer.println("output += \"" + fieldName + index1 + ": \" + Boolean.toString(" + fieldName + index2
					+ ") + \"\\n\";");
			break;
		case FieldType.F32:
			writer.println("output += \"" + fieldName + index1 + ": \" + Float.toString(" + fieldName + index2
					+ ") + \"\\n\";");
			break;
		case FieldType.F64:
			writer.println("output += \"" + fieldName + index1 + ": \" + Double.toString(" + fieldName + index2
					+ ") + \"\\n\";");
			break;
		case FieldType.I8:
		case FieldType.U8:
			writer.println("output += \"" + fieldName + index1 + ": \" + Byte.toString(" + fieldName + index2
					+ ") + \"\\n\";");
			break;
		case FieldType.I16:
		case FieldType.U16:
		case FieldType.IPPORT:
			writer.println("output += \"" + fieldName + index1 + ": \" + Short.toString(" + fieldName + index2
					+ ") + \"\\n\";");
			break;
		case FieldType.I32:
		case FieldType.U32:
		case FieldType.IPADDR:
			writer.println("output += \"" + fieldName + index1 + ": \" + Integer.toString(" + fieldName + index2
					+ ") + \"\\n\";");
			break;
		case FieldType.I64:
		case FieldType.U64:
			writer.println("output += \"" + fieldName + index1 + ": \" + Long.toString(" + fieldName + index2
					+ ") + \"\\n\";");
			break;
		case FieldType.UUID:
		case FieldType.Vector3:
		case FieldType.Vector3d:
		case FieldType.Vector4:
		case FieldType.Quaternion:
			writer.println(
					"output += \"" + fieldName + index1 + ": \" + " + fieldName + index2 + ".toString() + \"\\n\";");
			break;
		default:
			writer.println("output += \"" + fieldName + index1 + ": \" + Helpers.toString(" + fieldName + index2
					+ ") + \"\\n\";");
			break;
		}
	}

	static PrintWriter WriteHeader(File file, String template) throws IOException {
		file.getParentFile().mkdirs(); // Useful if we want this during the build, and the target directory does not
										// exist yet ;)
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		BufferedReader reader = new BufferedReader(new FileReader(template));
		while (reader.ready()) {
			String line = reader.readLine();
			if (line != null) {
				writer.println(line);
			} else {
				break;
			}
		}
		reader.close();
		return writer;
	}

	static void WriteBlockClass(PrintWriter writer, ProtocolManager protocol, MapBlock block) {
		boolean variableFields = false;
		String blockName = protocol.keywordPosition(block.keywordIndex);

		writer.println("    public class " + blockName + "Block\n    {");

		for (int k = 0; k < block.fields.size(); k++) {
			MapField field = block.fields.get(k);
			WriteFieldMember(writer, protocol.keywordPosition(field.keywordIndex), field);

			if (field.type == FieldType.Variable) {
				variableFields = true;
			}
		}

		// Length property
		writer.println("");
		writer.println("        public int getLength(){");
		int length = 0;
		for (int k = 0; k < block.fields.size(); k++) {
			MapField field = block.fields.get(k);
			length += GetFieldLength(writer, protocol, field);
		}

		if (!variableFields) {
			writer.println("            return " + length + ";");
		} else {
			writer.println("            int length = " + length + ";");

			for (int k = 0; k < block.fields.size(); k++) {
				MapField field = block.fields.get(k);
				if (field.type == FieldType.Variable) {
					String fieldName = protocol.keywordPosition(field.keywordIndex);
					writer.println("            if (_" + fieldName.toLowerCase() + " != null) { length += "
							+ field.count + " + _" + fieldName.toLowerCase() + ".length; }");
				}
			}
			writer.println("            return length;");
		}
		writer.println("        }\n");

		// Default constructor
		writer.println("        public " + blockName + "Block() { }");

		// Constructor for building the class from bytes
		writer.println("        public " + blockName + "Block(ByteBuffer bytes)\n        {");

		// Declare a length variable if we need it for variable fields in this
		// constructor
		if (variableFields) {
			writer.println("            int length;");
		}

		for (int k = 0; k < block.fields.size(); k++) {
			MapField field = block.fields.get(k);
			String fieldName = protocol.keywordPosition(field.keywordIndex);
			WriteFieldFromBytes(writer, 12, fieldName, field, "bytes", new String());
		}

		writer.println("        }\n");

		// ToBytes() function
		writer.println("        public void ToBytes(ByteBuffer bytes) throws Exception\n        {");

		for (int k = 0; k < block.fields.size(); k++) {
			MapField field = block.fields.get(k);
			String fieldName = protocol.keywordPosition(field.keywordIndex);
			WriteFieldToBytes(writer, 12, fieldName, field, "bytes", new String());
		}

		writer.println("        }\n");

		// toString() function
		writer.println("        @Override\n        public String toString()\n        {");
		writer.println("            String output = \"-- " + blockName + " --\\n\";");
		writer.println("            try {");

		for (int k = 0; k < block.fields.size(); k++) {
			MapField field = block.fields.get(k);
			String fieldName = protocol.keywordPosition(field.keywordIndex);
			WriteFieldToString(writer, 16, fieldName, field, new String());
		}
		writer.println("                output = output.trim();");
		writer.println("            }");
		writer.println("            catch(Exception e){}");
		writer.println("            return output;");
		writer.println("        }");
		writer.println("    }\n");
		writer.println("    public " + blockName + "Block create" + blockName + "Block() {");
		writer.println("         return new " + blockName + "Block();");
		writer.println("    }\n");
	}

	static void WriteHelpersImport(PrintWriter writer, MapPacket packet) {
		for (MapBlock block : packet.blocks) {
			for (int k = 0; k < block.fields.size(); k++) {
				MapField field = block.fields.get(k);

				if (field.type == FieldType.Variable || field.type == FieldType.Fixed) {
					writer.println("import libomv.utils.Helpers;");
					return;
				}
			}
		}
	}

	static void WritePacketClass(File packets_dir, String template, ProtocolManager protocol, MapPacket packet)
			throws IOException {
		boolean[] variableField = new boolean[FieldType.NumTypes];
		boolean cannotMultiple = false, hasVariableBlocks = false;
		PrintWriter writer = WriteHeader(new File(packets_dir, packet.name + "Packet.java"), template);

		// Check if there are any variable blocks
		for (MapBlock block : packet.blocks) {
			if (block.count == -1) {
				hasVariableBlocks = true;
			} else if (hasVariableBlocks) {
				// A fixed or single block showed up after a variable count block.
				// Our automatic splitting algorithm won't work for this packet
				cannotMultiple = true;
			}
		}

		if (hasVariableBlocks && !cannotMultiple)
			writer.println("import java.util.ArrayList;");

		writer.println();
		writer.println("import libomv.types.PacketHeader;");
		writer.println("import libomv.types.PacketFrequency;");

		for (MapBlock block : packet.blocks) {
			for (MapField field : block.fields) {
				if (!variableField[field.type]) {
					switch (field.type) {
					case FieldType.Variable:
						writer.println("import libomv.types.OverflowException;");
						variableField[field.type] = true;
						break;
					case FieldType.UUID:
						writer.println("import libomv.types.UUID;");
						variableField[field.type] = true;
						break;

					case FieldType.Vector3:
						writer.println("import libomv.types.Vector3;");
						variableField[field.type] = true;
						break;
					case FieldType.Vector3d:
						writer.println("import libomv.types.Vector3d;");
						variableField[field.type] = true;
						break;
					case FieldType.Vector4:
						writer.println("import libomv.types.Vector4;");
						variableField[field.type] = true;
						break;
					case FieldType.Quaternion:
						writer.println("import libomv.types.Quaternion;");
						variableField[field.type] = true;
						break;
					default:
						break;
					}
				}
			}
		}

		WriteHelpersImport(writer, packet);
		writer.println("\npublic class " + packet.name + "Packet extends Packet\n{");

		// Write out each block class
		for (MapBlock block : packet.blocks) {
			if (block.fields.size() > 1 || block.fields.get(0).type == FieldType.Variable) {
				WriteBlockClass(writer, protocol, block);
			}
		}

		// PacketType member
		writer.println("    @Override");
		writer.println("    public PacketType getType() { return PacketType." + packet.name + "; }");

		// Block members
		for (MapBlock block : packet.blocks) {
			String blockName = protocol.keywordPosition(block.keywordIndex);

			// TODO: More thorough name blacklisting

			MapField field = block.fields.get(0);
			if (block.fields.size() > 1 || field.type == FieldType.Variable) {
				writer.println(
						"    public " + blockName + "Block" + ((block.count != 1) ? "[] " : " ") + blockName + ";");
			} else {
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				writer.println("    public " + FieldTypeString(field.type) + ((block.count != 1) ? "[] " : " ")
						+ fieldName + ";");
			}
		}

		writer.println("");

		// Default constructor
		writer.println("    public " + packet.name + "Packet()\n    {");
		writer.println("        hasVariableBlocks = " + (hasVariableBlocks ? "true" : "false") + ";");
		writer.println(
				"        _header = new PacketHeader(PacketFrequency." + PacketFrequency.Names[packet.frequency] + ");");
		writer.println("        _header.setID((short)" + packet.id + ");");
		// Turn the reliable flag on by default
		writer.println("        _header.setReliable(true);");
		if (packet.encoded) {
			writer.println("        _header.setZerocoded(true);");
		}
		for (MapBlock block : packet.blocks) {
			String blockName = protocol.keywordPosition(block.keywordIndex);

			MapField field = block.fields.get(0);
			if (block.fields.size() > 1 || field.type == FieldType.Variable) {
				if (block.count == 1) {
					// Single count block
					writer.println("        " + blockName + " = new " + blockName + "Block();");
				} else if (block.count == -1) {
					// Variable count block
					writer.println("        " + blockName + " = new " + blockName + "Block[0];");
				} else {
					// Multiple count block
					writer.println("        " + blockName + " = new " + blockName + "Block[" + block.count + "];");
				}
			} else {
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == 1) {
					// Single count block
					if (field.type == FieldType.UUID
							|| field.type >= FieldType.Vector3 && field.type <= FieldType.Quaternion)
						writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "();");
				} else if (block.count == -1) {
					// Variable count block
					writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "[0];");
				} else {
					// Multiple count block
					writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "[" + block.count
							+ "];");
				}
			}
		}
		writer.println("    }\n");

		// Constructor that takes a byte array and beginning position only (no prebuilt
		// header)
		writer.println("    public " + packet.name + "Packet(ByteBuffer bytes) throws Exception");
		writer.println("    {");
		writer.println("        hasVariableBlocks = " + (hasVariableBlocks ? "true" : "false") + ";");
		writer.println("        _header = new PacketHeader(bytes, PacketFrequency."
				+ PacketFrequency.Names[packet.frequency] + ");");
		for (MapBlock block : packet.blocks) {
			String blockName = protocol.keywordPosition(block.keywordIndex);
			MapField field = block.fields.get(0);
			if (block.fields.size() > 1 || field.type == FieldType.Variable) {
				if (block.count == 1) {
					// Single count block
					writer.println("        " + blockName + " = new " + blockName + "Block(bytes);");
				} else if (block.count == -1) {
					// Variable count block
					writer.println("        if (bytes.hasRemaining())\n        {");
					writer.println("            int count = bytes.get() & 0xFF;");
					writer.println("            " + blockName + " = new " + blockName + "Block[count];");
					writer.println("            for (int j = 0; j < count; j++)\n            {");
					writer.println("                " + blockName + "[j] = new " + blockName + "Block(bytes);");
					writer.println("            }\n        }\n        else\n        {");
					writer.println("            " + blockName + " = new " + blockName + "Block[0];\n        }");
				} else {
					// Multiple count block
					writer.println("        " + blockName + " = new " + blockName + "Block[" + block.count + "];");
					writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
					writer.println("            " + blockName + "[j] = new " + blockName + "Block(bytes);");
					writer.println("        }");
				}
			} else {
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == 1) {
					// Single count block
					WriteFieldFromBytes(writer, 8, fieldName, field, "bytes", new String());
				} else if (block.count == -1) {
					// Variable count block
					writer.println("        if (bytes.hasRemaining())\n        {");
					writer.println("            int count = bytes.get() & 0xFF;");
					writer.println("            " + fieldName + " = new " + FieldTypeString(field.type) + "[count];");
					if (field.type == FieldType.I8 || field.type == FieldType.U8) {
						writer.println("            bytes.get(" + fieldName + ");");
					} else {
						writer.println("            for (int j = 0; j < count; j++)\n            {");
						WriteFieldFromBytes(writer, 16, fieldName, field, "bytes", "[j]");
						writer.println("            }");
					}
					writer.println("        }\n        else\n        {");
					writer.println(
							"            " + fieldName + " = new " + FieldTypeString(field.type) + "[0];\n        }");
				} else {
					// Multiple count block
					writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "[" + block.count
							+ "];");
					if (field.type == FieldType.I8 || field.type == FieldType.U8) {
						writer.println("        bytes.get(" + fieldName + ");");
					} else {
						writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
						WriteFieldFromBytes(writer, 12, fieldName, field, "bytes", "[j]");
						writer.println("        }");
					}
				}
			}
		}
		writer.println("     }\n");

		// Constructor that takes a byte array and a prebuilt header
		writer.println("    public " + packet.name + "Packet(PacketHeader head, ByteBuffer bytes)");
		writer.println("    {");
		writer.println("        hasVariableBlocks = " + (hasVariableBlocks ? "true" : "false") + ";");
		writer.println("        _header = head;");
		for (MapBlock block : packet.blocks) {
			String blockName = protocol.keywordPosition(block.keywordIndex);
			MapField field = block.fields.get(0);
			if (block.fields.size() > 1 || field.type == FieldType.Variable) {
				if (block.count == 1) {
					// Single count block
					writer.println("        " + blockName + " = new " + blockName + "Block(bytes);");
				} else if (block.count == -1) {
					// Variable count block
					writer.println("        if (bytes.hasRemaining())\n        {");
					writer.println("            int count = bytes.get() & 0xFF;");
					writer.println("            " + blockName + " = new " + blockName + "Block[count];");
					writer.println("            for (int j = 0; j < count; j++)\n            {");
					writer.println("                " + blockName + "[j] = new " + blockName + "Block(bytes);");
					writer.println("            }\n        }\n        else\n        {");
					writer.println("            " + blockName + " = new " + blockName + "Block[0];\n        }");
				} else {
					// Multiple count block
					writer.println("        " + blockName + " = new " + blockName + "Block[" + block.count + "];");
					writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
					writer.println("            " + blockName + "[j] = new " + blockName + "Block(bytes);");
					writer.println("        }");
				}
			} else {
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == 1) {
					// Single count block
					WriteFieldFromBytes(writer, 8, fieldName, field, "bytes", new String());
				} else if (block.count == -1) {
					// Variable count block
					writer.println("        if (bytes.hasRemaining())\n        {");
					writer.println("            int count = bytes.get() & 0xFF;");
					writer.println("            " + fieldName + " = new " + FieldTypeString(field.type) + "[count];");
					if (field.type == FieldType.I8 || field.type == FieldType.U8) {
						writer.println("            bytes.get(" + fieldName + ");");
					} else {
						writer.println("            for (int j = 0; j < count; j++)\n            {");
						WriteFieldFromBytes(writer, 16, fieldName, field, "bytes", "[j]");
						writer.println("            }");
					}
					writer.println("        }\n        else\n        {");
					writer.println(
							"            " + fieldName + " = new " + FieldTypeString(field.type) + "[0];\n        }");
				} else {
					// Multiple count block
					writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "[" + block.count
							+ "];");
					if (field.type == FieldType.I8 || field.type == FieldType.U8) {
						writer.println("        bytes.get(" + fieldName + ");");
					} else {
						writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
						WriteFieldFromBytes(writer, 12, fieldName, field, "bytes", "[j]");
						writer.println("        }");
					}
				}
			}
		}
		writer.println("    }\n");

		// getLength() function
		writer.println("    @Override");
		writer.println("    public int getLength()");
		writer.println("    {");

		writer.println("        int length = _header.getLength();");

		for (MapBlock block : packet.blocks) {
			String blockName = protocol.keywordPosition(block.keywordIndex);
			MapField field = block.fields.get(0);
			if (block.fields.size() > 1 || field.type == FieldType.Variable) {
				if (block.count == 1) {
					// Single count block
					writer.println("        length += " + blockName + ".getLength();");
				}
			} else {
				if (block.count == 1) {
					writer.println("        length += " + GetFieldLength(writer, protocol, field) + ";");
				}
			}
		}

		for (MapBlock block : packet.blocks) {
			String blockName = protocol.keywordPosition(block.keywordIndex);
			MapField field = block.fields.get(0);
			if (block.fields.size() > 1 || field.type == FieldType.Variable) {
				if (block.count == -1) {
					writer.println("        length++;");
					writer.println("        if (" + blockName + " != null)\n        {");
					writer.println("            for (int j = 0; j < " + blockName + ".length; j++) { length += "
							+ blockName + "[j].getLength(); }");
					writer.println("        }");
				} else if (block.count > 1) {
					writer.println("        for (int j = 0; j < " + block.count + "; j++) { length += " + blockName
							+ "[j].getLength(); }");
				}
			} else {
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == -1) {
					writer.println("        length++;");
					writer.println("        if (" + fieldName + " != null)\n        {");
					writer.println("            length += " + fieldName + ".length * "
							+ GetFieldLength(writer, protocol, field) + ";");
					writer.println("        }");
				} else if (block.count > 1) {
					writer.println(
							"        length += " + block.count + " * " + GetFieldLength(writer, protocol, field) + ";");
				}
			}
		}

		writer.println("        return length;\n    }\n");

		WriteToBytes(writer, protocol, packet);
		if (hasVariableBlocks && !cannotMultiple)
			WriteToBytesMultiple(writer, protocol, packet);
		WriteToString(writer, protocol, packet);

		// Closing function bracket
		writer.println("}");
		writer.close();
	}

	static void WriteToBytes(PrintWriter writer, ProtocolManager protocol, MapPacket packet) {
		String blockName;

		writer.println("    @Override");
		writer.println("    public ByteBuffer ToBytes() throws Exception");
		writer.println("    {");
		writer.println("        ByteBuffer bytes = ByteBuffer.allocate(getLength());");
		writer.println("        _header.ToBytes(bytes);");
		writer.println("        bytes.order(ByteOrder.LITTLE_ENDIAN);");
		for (MapBlock block : packet.blocks) {
			MapField field = block.fields.get(0);
			blockName = protocol.keywordPosition(block.keywordIndex);
			if (block.fields.size() > 1 || field.type == FieldType.Variable) {
				if (block.count == -1) {
					// Variable count block
					writer.println("        if (" + blockName + " != null)\n        {");
					writer.println("            bytes.put((byte)" + blockName + ".length);");
					writer.println("            for (int j = 0; j < " + blockName + ".length; j++)");
					writer.println("            {");
					writer.println("                " + blockName + "[j].ToBytes(bytes);");
					writer.println("            }");
					writer.println("        }\n        else\n        {");
					writer.println("            bytes.put((byte)0);");
					writer.println("        }");
				} else if (block.count == 1) {
					writer.println("        " + blockName + ".ToBytes(bytes);");
				} else {
					// Multiple count block
					writer.println("        for (int j = 0; j < " + block.count + "; j++) { " + blockName
							+ "[j].ToBytes(bytes); }");
				}
			} else {
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == 1) {
					WriteFieldToBytes(writer, 8, fieldName, field, "bytes", new String());
				} else if (block.count == -1) {
					// Variable count block
					writer.println("        if (" + fieldName + " != null)\n        {");
					writer.println("            bytes.put((byte)" + fieldName + ".length);");
					if (field.type == FieldType.I8 || field.type == FieldType.U8) {
						writer.println("            bytes.put(" + fieldName + ");");
					} else {
						writer.println("            for (int j = 0; j < " + fieldName + ".length; j++)\n            {");
						WriteFieldToBytes(writer, 16, fieldName, field, "bytes", "[j]");
						writer.println("            }");
					}
					writer.println("        }\n        else\n        {");
					writer.println("            bytes.put((byte)0);");
					writer.println("        }");
				} else {
					// Multiple count block
					if (field.type == FieldType.I8 || field.type == FieldType.U8) {
						writer.println("        bytes.put(" + fieldName + ");");
					} else {
						writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
						WriteFieldToBytes(writer, 12, fieldName, field, "bytes", "[j]");
						writer.println("        }");
					}
				}
			}
		}
		writer.println("        return bytes;\n    }\n");
	}

	static void WriteToBytesMultiple(PrintWriter writer, ProtocolManager protocol, MapPacket packet) {
		String blockName;
		boolean isVariable = false;
		int variableCountBlock = 0;

		writer.println("    @Override");
		writer.println("    public ByteBuffer[] ToBytesMultiple() throws Exception\n    {");
		writer.println("        int maxLength = Math.min(Packet.MTU, getLength());");
		writer.println("        ArrayList<ByteBuffer> packets = new ArrayList<ByteBuffer>();");
		writer.println("        ByteBuffer fixedBytes = ByteBuffer.allocate(maxLength);");
		writer.println("        _header.ToBytes(fixedBytes);");
		writer.println("        fixedBytes.order(ByteOrder.LITTLE_ENDIAN);");

		// Serialize fixed blocks
		for (MapBlock block : packet.blocks) {
			MapField field = block.fields.get(0);
			blockName = protocol.keywordPosition(block.keywordIndex);
			if (block.count == -1) {
				// Variable count block
				if (block.fields.size() > 1 || field.type == FieldType.Variable)
					isVariable = true;
				if (variableCountBlock == 0)
					writer.println();
				writer.println("        int " + blockName + "Start, " + blockName + "Count = 0;");
				++variableCountBlock;
			} else {
				if (block.fields.size() > 1 || field.type == FieldType.Variable) {
					if (block.count == 1) {
						writer.println("        " + blockName + ".ToBytes(fixedBytes);");
					} else {
						// Multiple count block
						writer.println("        for (int j = 0; j < " + block.count + "; j++) { " + blockName
								+ "[j].ToBytes(fixedBytes); }");
					}
				} else {
					String fieldName = protocol.keywordPosition(field.keywordIndex);
					if (block.count == 1) {
						WriteFieldToBytes(writer, 8, fieldName, field, "fixedBytes", new String());
					} else {
						// Multiple count block
						if (field.type == FieldType.I8 || field.type == FieldType.U8) {
							writer.println("        fixedBytes.put(" + fieldName + ");");
						} else {
							writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
							WriteFieldToBytes(writer, 12, fieldName, field, "fixedBytes", "[j]");
							writer.println("        }");
						}
					}
				}
			}
		}

		if (variableCountBlock > 0) {
			if (isVariable)
				writer.println("        int index, blockLength;");
		}
		writer.println("        do");
		writer.println("        {");
		writer.println("            ByteBuffer bytes = ByteBuffer.allocate(maxLength);");
		writer.println("            bytes.put(fixedBytes.array(), 0, fixedBytes.position());");
		writer.println("            bytes.order(ByteOrder.LITTLE_ENDIAN);");

		// Count how many variable blocks can go in this packet
		for (MapBlock block : packet.blocks) {
			blockName = protocol.keywordPosition(block.keywordIndex);
			if (block.count == -1) {
				// Variable count block
				writer.println("            " + blockName + "Start = " + blockName + "Count;");
			}
		}

		String fieldName = "";
		blockName = "";
		for (MapBlock block : packet.blocks) {
			if (block.count == -1) {
				blockName = protocol.keywordPosition(block.keywordIndex);
				writer.println();
				// Variable count block
				MapField field = block.fields.get(0);
				if (block.fields.size() > 1 || field.type == FieldType.Variable) {
					fieldName = blockName;
					writer.println("            index = bytes.position();");
					writer.println("            bytes.put((byte)0);");
					writer.println("            if (" + fieldName + " != null)");
					writer.println("            {");
					writer.println("                while (" + blockName + "Count < " + blockName + ".length)");
					writer.println("                {");
					writer.println(
							"                    blockLength = " + blockName + "[" + blockName + "Count].getLength();");
					writer.println("                    if (bytes.position() + blockLength > Packet.MTU)");
					writer.println("                        break;");
					writer.println("                    " + fieldName + "[" + blockName + "Count++].ToBytes(bytes);");
					writer.println("                }");
					writer.println("                bytes.put(index, (byte)(" + blockName + "Count - " + blockName
							+ "Start));");
				} else {
					fieldName = protocol.keywordPosition(field.keywordIndex);

					writer.println("            if (" + fieldName + " != null)");
					writer.println("            {");
					if (field.type == FieldType.I8 || field.type == FieldType.U8) {
						writer.println("                " + blockName + "Count = Packet.MTU - bytes.position() - 1;");
						writer.println("                if (" + blockName + "Count > " + fieldName + ".length - "
								+ blockName + "Start)");
						writer.println("            	    " + blockName + "Count = " + fieldName + ".length - "
								+ blockName + "Start;");
						writer.println("                bytes.put((byte)(" + blockName + "Count));");
						writer.println("                bytes.put(" + fieldName + ", " + blockName + "Start, "
								+ blockName + "Count);");
						writer.println("            	" + blockName + "Count += " + blockName + "Start;");
					} else {
						writer.println("                " + blockName + "Count = (Packet.MTU - bytes.position() - 1) / "
								+ GetFieldLength(writer, protocol, field) + ";");
						writer.println("                if (" + blockName + "Count + " + blockName + "Start >= "
								+ fieldName + ".length)");
						writer.println("            	    " + blockName + "Count = " + fieldName + ".length;");
						writer.println("            	else");
						writer.println("            	    " + blockName + "Count += " + blockName + "Start;");
						writer.println(
								"                bytes.put((byte)(" + blockName + "Count - " + blockName + "Start));");
						writer.println("                for (int j = " + blockName + "Start; j < " + blockName
								+ "Count; j++)");
						writer.println("                {");
						WriteFieldToBytes(writer, 20, fieldName, field, "bytes", "[j]");
						writer.println("                }");
					}
					writer.println("            }");
					writer.println("            else");
					writer.println("            {");
					writer.println("                bytes.put((byte)0);");
				}
				writer.println("            }");
			}
		}
		writer.println("            packets.add(bytes);");
		writer.print("        } while (");

		variableCountBlock = 0;
		for (MapBlock block : packet.blocks) {
			if (block.count == -1) {
				if (variableCountBlock > 0)
					writer.print(" ||\n                 ");
				variableCountBlock++;
				blockName = protocol.keywordPosition(block.keywordIndex);
				MapField field = block.fields.get(0);
				if (block.fields.size() > 1 || field.type == FieldType.Variable) {
					fieldName = blockName;
				} else {
					fieldName = protocol.keywordPosition(field.keywordIndex);
				}
				writer.print(blockName + "Count < " + fieldName + ".length");
			}
		}
		writer.println(");");
		writer.println("        return packets.toArray(new ByteBuffer[0]);");
		writer.println("    }\n");
	}

	static public void WriteToString(PrintWriter writer, ProtocolManager protocol, MapPacket packet) {
		String blockName, sanitizedName;

		writer.println("    @Override");
		writer.println("    public String toString()\n    {");
		writer.println("        String output = \"--- " + packet.name + " ---\\n\";");

		for (int k = 0; k < packet.blocks.size(); k++) {
			MapBlock block = packet.blocks.get(k);
			blockName = protocol.keywordPosition(block.keywordIndex);
			MapField field = block.fields.get(0);
			if (block.fields.size() > 1 || field.type == FieldType.Variable) {
				if (blockName.equals("Header")) {
					sanitizedName = "_" + blockName;
				} else {
					sanitizedName = blockName;
				}

				if (block.count == -1) {
					// Variable count block
					writer.println("        if (" + sanitizedName + " != null)\n        {");
					writer.println("            for (int j = 0; j < " + sanitizedName + ".length; j++)\n            {");
					writer.println(
							"                output += " + sanitizedName + "[j].toString() + \"\\n\";\n            }");
					writer.println("        }");

				} else if (block.count == 1) {
					writer.println("        output += " + sanitizedName + ".toString() + \"\\n\";");
				} else {
					// Multiple count block
					writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
					writer.println("            output += " + sanitizedName + "[j].toString() + \"\\n\";\n        }");
				}
			} else {
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == 1) {
					WriteFieldToString(writer, 8, fieldName, field, new String());
				} else if (block.count == -1) {
					// Variable count block
					writer.println("        if (" + fieldName + " != null)\n        {");
					writer.println("            for (int j = 0; j < " + fieldName + ".length; j++)\n            {");
					WriteFieldToString(writer, 16, fieldName, field, "j");
					writer.println("            }\n        }");
				} else {
					// Multiple count block
					writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
					WriteFieldToString(writer, 12, fieldName, field, "j");
					writer.println("        }");
				}

			}
		}

		writer.println("        return output;\n    }");
	}

	static public void main(String[] args) {
		ProtocolManager protocol = null;
		PrintWriter writer = null;

		try {
			if (args.length < 3) {
				logger.error(
						"Invalid arguments, using default values for [message_template.msg] [template.java.txt] [Packet.java]");
				args = new String[] { "src/main/java/libomv/mapgenerator/message_template.msg",
						"src/main/java/libomv/mapgenerator/template.java.txt",
						"src/main/java/libomv/packets/Packet.java" };
			}

			File packets_dir = new File(args[2]).getParentFile();
			protocol = new ProtocolManager(args[0], false);

			/* Open Packet.java file and copy file header from template */
			writer = WriteHeader(new File(args[2]), args[1]);

			PrintWriter packettype_writer = new PrintWriter(new FileWriter(new File(packets_dir, "PacketType.java")));

			// Write the PacketType enum
			packettype_writer.println("package libomv.packets;\npublic enum PacketType\n{\n    Default,");
			for (int k = 0; k < protocol.lowMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.lowMaps.mapPackets.get(k);
				if (packet != null) {
					packettype_writer.println("    " + packet.name + ",");
				}
			}
			for (int k = 0; k < protocol.mediumMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.mediumMaps.mapPackets.get(k);
				if (packet != null) {
					packettype_writer.println("    " + packet.name + ",");
				}
			}
			for (int k = 0; k < protocol.highMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.highMaps.mapPackets.get(k);
				if (packet != null) {
					packettype_writer.println("    " + packet.name + ",");
				}
			}
			packettype_writer.println("}\n");
			packettype_writer.close();

			// Write the base Packet class
			writer.println("import libomv.StructuredData.OSDMap;\n"
					+ "import libomv.capabilities.CapsMessage.CapsEventType;\n" + "import libomv.types.PacketHeader;\n"
					+ "import libomv.types.PacketFrequency;\n\n" + "public abstract class Packet\n" + "{\n"
					+ "    public static final int MTU = 1200;\n\n" + "    public boolean hasVariableBlocks;\n"
					+ "    protected PacketHeader _header;\n"
					+ "    public PacketHeader getHeader() { return _header; }\n"
					+ "    public void setHeader(PacketHeader value) { _header = value; };\n"
					+ "    public abstract PacketType getType();\n" + "    public abstract int getLength();\n"
					+ "    // Serializes the packet in to a byte array\n"
					+ "    // return A byte array containing the serialized packet payload, ready to be sent across the wire\n"
					+ "    public abstract ByteBuffer ToBytes() throws Exception;\n\n"
					+ "    public ByteBuffer[] ToBytesMultiple() throws Exception\n    {\n"
					+ "        if (getLength() > MTU)\n"
					+ "            throw new UnsupportedOperationException(\"ToBytesMultiple()\");\n\n"
					+ "        ByteBuffer[] buffer = new ByteBuffer[1];\n" + "        buffer[0] = ToBytes();\n"
					+ "        return buffer;\n" + "    }\n\n"
					+ "    //Get the PacketType for a given packet id and packet frequency\n"
					+ "    //<param name=\"id\">The packet ID from the header</param>\n"
					+ "    //<param name=\"frequency\">Frequency of this packet</param>\n"
					+ "    //<returns>The packet type, or PacketType.Default</returns>\n"
					+ "    public static PacketType getType(short id, byte frequency)\n    {\n"
					+ "        switch (frequency)\n        {\n            case PacketFrequency.Low:\n"
					+ "                switch (id)\n                {");

			for (int k = 0; k < protocol.lowMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.lowMaps.mapPackets.get(k);
				if (packet != null) {
					writer.println("                        case (short)" + packet.id + ": return PacketType."
							+ packet.name + ";");
				}
			}

			writer.println("                        default:\n                            break;");
			writer.println("                    }\n                    break;\n"
					+ "                case PacketFrequency.Medium:\n                    switch (id)\n                    {");

			for (int k = 0; k < protocol.mediumMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.mediumMaps.mapPackets.get(k);
				if (packet != null) {
					writer.println(
							"                        case " + packet.id + ": return PacketType." + packet.name + ";");
				}
			}

			writer.println("                        default:\n                            break;");
			writer.println("                    }\n                    break;\n"
					+ "                case PacketFrequency.High:\n                    switch (id)\n"
					+ "                    {");

			for (int k = 0; k < protocol.highMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.highMaps.mapPackets.get(k);
				if (packet != null) {
					writer.println(
							"                        case " + packet.id + ": return PacketType." + packet.name + ";");
				}
			}

			writer.println("                        default:\n                            break;");
			writer.println("                    }\n                    break;");
			writer.println("                default:\n                    break;\n            }");
			writer.println("            return PacketType.Default;\n        }\n");

			writer.println("        /**\n"
					+ "         * Construct a packet in it's native class from a capability OSD structure\n"
					+ "         *\n"
					+ "         * @param bytes Byte array containing the packet, starting at position 0\n"
					+ "         * @param packetEnd The last byte of the packet. If the packet was 76 bytes long, packetEnd would be 75\n"
					+ "         * @returns The native packet class for this type of packet, typecasted to the generic Packet\n"
					+ "         */\n"
					+ "        public static Packet BuildPacket(CapsEventType capsKey,  OSDMap map) throws Exception\n"
					+ "        {\n            return null;\n        }\n\n");

			writer.println("        /**\n" + "         * Construct a packet in it's native class from a byte array\n"
					+ "         *\n"
					+ "         * @param bytes Byte array containing the packet, starting at position 0\n"
					+ "         * @param packetEnd The last byte of the packet. If the packet was 76 bytes long, packetEnd would be 75\n"
					+ "         * @returns The native packet class for this type of packet, typecasted to the generic Packet\n"
					+ "         */\n        public static Packet BuildPacket(ByteBuffer bytes) throws Exception\n"
					+ "        {\n            PacketHeader _header = new PacketHeader(bytes);\n"
					+ "            bytes.order(ByteOrder.LITTLE_ENDIAN);\n"
					+ "            bytes.position(_header.getLength());\n\n"
					+ "            switch (_header.getFrequency())            {\n"
					+ "                case PacketFrequency.Low:\n                    switch (_header.getID())\n"
					+ "                    {");
			for (int k = 0; k < protocol.lowMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.lowMaps.mapPackets.get(k);
				if (packet != null) {
					writer.println("                        case (short)" + packet.id + ": return new " + packet.name
							+ "Packet(_header,bytes);");
				}
			}
			writer.println("                        default:\n                            break;");
			writer.println(
					"                    }\n                    break;\n                case PacketFrequency.Medium:\n"
							+ "                    switch (_header.getID())\n                    {");
			for (int k = 0; k < protocol.mediumMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.mediumMaps.mapPackets.get(k);
				if (packet != null) {
					writer.println("                        case " + packet.id + ": return new " + packet.name
							+ "Packet(_header, bytes);");
				}
			}
			writer.println("                        default:\n                            break;");
			writer.println(
					"                    }\n                    break;\n                case PacketFrequency.High:\n"
							+ "                    switch (_header.getID())\n                    {");
			for (int k = 0; k < protocol.highMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.highMaps.mapPackets.get(k);
				if (packet != null) {
					writer.println("                        case " + packet.id + ": return new " + packet.name
							+ "Packet(_header, bytes);");
				}
			}
			writer.println("                        default:\n                            break;");
			writer.println(
					"                    }\n                    break;\n                default:\n                    break;\n            }\n"
							+ "            throw new Exception(\"Unknown packet ID\");\n        }\n");

			// Write the packet classes
			for (int k = 0; k < protocol.lowMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.lowMaps.mapPackets.get(k);
				if (packet != null) {
					WritePacketClass(packets_dir, args[1], protocol, packet);
				}
			}

			for (int k = 0; k < protocol.mediumMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.mediumMaps.mapPackets.get(k);
				if (packet != null) {
					WritePacketClass(packets_dir, args[1], protocol, packet);
				}
			}

			for (int k = 0; k < protocol.highMaps.mapPackets.size(); k++) {
				MapPacket packet = protocol.highMaps.mapPackets.get(k);
				if (packet != null) {
					WritePacketClass(packets_dir, args[1], protocol, packet);
				}
			}
			writer.println("}");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
