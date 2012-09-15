/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
 * Portions Copyright (c) 2009-2011, Frederick Martian
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
package libomv.mapgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import libomv.ProtocolManager;
import libomv.ProtocolManager.FieldType;
import libomv.ProtocolManager.MapBlock;
import libomv.ProtocolManager.MapField;
import libomv.ProtocolManager.MapPacket;
import libomv.types.PacketFrequency;
import libomv.utils.Helpers;

public class mapgenerator
{
	static String spaces = new String("                    ");

	static String FieldTypeString(int type)
	{
		switch (type)
		{
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
		}
		return null;
	}
	
	static String FieldInitString(int type)
	{
		switch (type)
		{
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
		}
		return null;
	}
 
	
	static void WriteFieldMember(PrintWriter writer, String fieldName, MapField field)
	{
		if (field.type != FieldType.Variable)
		{
			writer.println("        public " + FieldTypeString(field.type) + " " + fieldName + " = " + FieldInitString(field.type) + ";");
		}
		else
		{
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
			writer.println("                System.arraycopy(value, 0, _" + fieldName.toLowerCase()
					+ ", 0, value.length);");
			writer.println("            }");
			writer.println("        }\n");
		}
	}

	static void WriteFieldFromBytes(PrintWriter writer, int indent, String fieldName, MapField field, String index)
	{
		String lead = spaces.substring(0, indent);
		writer.write(lead);

		switch (field.type)
		{
			case FieldType.BOOL:
				writer.println(fieldName + index + " = (bytes.get() != 0) ? (boolean)true : (boolean)false;");
				break;
			case FieldType.F32:
				writer.println(fieldName + index + " = bytes.getFloat();");
				break;
			case FieldType.F64:
				writer.println(fieldName + index + " = bytes.getDouble();");
				break;
			case FieldType.Fixed:
				writer.println(fieldName + index + " = new byte[" + field.count + "];");
				writer.println(lead + "bytes.get(" + fieldName + index + ");");
				break;
			case FieldType.IPADDR:
			case FieldType.U32:
				writer.println(fieldName + index + " = bytes.getInt();");
				break;
			case FieldType.IPPORT:
				// IPPORT is big endian while U16/S16 are little endian.
				writer.println(fieldName + index + " = (short)((bytes.get() << 8) + bytes.get());");
				break;
			case FieldType.U16:
				writer.println(fieldName + index + " = bytes.getShort();");
				break;
			case FieldType.Quaternion:
				writer.println(fieldName + index + " = new Quaternion(bytes, true);");
				break;
			case FieldType.UUID:
				writer.println(fieldName + index + " = new UUID(bytes);");
				break;
			case FieldType.Vector3:
				writer.println(fieldName + index + " = new Vector3(bytes);");
				break;
			case FieldType.Vector3d:
				writer.println(fieldName + index + " = new Vector3d(bytes);");
				break;
			case FieldType.Vector4:
				writer.println(fieldName + index + " = new Vector4(bytes);");
				break;
			case FieldType.I16:
				writer.println(fieldName + index + " = bytes.getShort();");
				break;
			case FieldType.I32:
				writer.println(fieldName + index + " = bytes.getInt();");
				break;
			case FieldType.I8:
				writer.println(fieldName + index + " = bytes.get();");
				break;
			case FieldType.U64:
				writer.println(fieldName + index + " = bytes.getLong();");
				break;
			case FieldType.U8:
				writer.println(fieldName + index + " = bytes.get();");
				break;
			case FieldType.Variable:
				if (field.count == 1)
				{
					writer.println("length = bytes.get() & 0xFF;");
				}
				else
				{
					writer.println("length = bytes.getShort() & 0xFFFF;");
				}
				writer.println(lead + "_" + fieldName.toLowerCase() + " = new byte[length];");
				writer.println(lead + "bytes.get(_" + fieldName.toLowerCase() + ");");
				break;
			default:
				writer.println("!!! ERROR: Unhandled FieldType: " + field.type + " !!!");
				break;
		}
	}

	static void WriteFieldToBytes(PrintWriter writer, int indent, String fieldName, MapField field, String index)
	{
		String lead = spaces.substring(0, indent);
		writer.write(lead);

		switch (field.type)
		{
			case FieldType.BOOL:
				writer.println("bytes.put((byte)((" + fieldName + index + ") ? 1 : 0));");
				break;
			case FieldType.F32:
				writer.println("bytes.putFloat(" + fieldName + index + ");");
				break;
			case FieldType.F64:
				writer.println("bytes.putDouble(" + fieldName + index + ");");
				break;
			case FieldType.Fixed:
				writer.println("bytes.put(" + fieldName + index + ");");
				break;
			case FieldType.IPPORT:
				// IPPORT is big endian while U16/S16 is little endian.
				writer.println("bytes.put((byte)((" + fieldName + index + " >> 8) % 256));");
				writer.println(lead + "bytes.put((byte)(" + fieldName + index + " % 256));");
				break;
			case FieldType.U16:
			case FieldType.I16:
				writer.println("bytes.putShort(" + fieldName + index + ");");
				break;
			case FieldType.UUID:
			case FieldType.Vector4:
			case FieldType.Quaternion:
			case FieldType.Vector3:
			case FieldType.Vector3d:
				writer.println(fieldName + index + ".GetBytes(bytes);");
				break;
			case FieldType.U8:
			case FieldType.I8:
				writer.println("bytes.put(" + fieldName + index + ");");
				break;
			case FieldType.IPADDR:
			case FieldType.U32:
			case FieldType.I32:
				writer.println("bytes.putInt(" + fieldName + index + ");");
				break;
			case FieldType.U64:
				writer.println("bytes.putLong(" + fieldName + index + ");");
				break;
			case FieldType.Variable:
				if (field.count == 1)
				{
					writer.println("bytes.put((byte)_" + fieldName.toLowerCase() + ".length);");
				}
				else
				{
					writer.println("bytes.putShort((short)_" + fieldName.toLowerCase() + ".length);");
				}
				writer.println(lead + "bytes.put(_" + fieldName.toLowerCase() + ");");
				break;
			default:
				writer.println("!!! ERROR: Unhandled FieldType: " + field.type + " !!!");
				break;
		}
	}

	static int GetFieldLength(PrintWriter writer, ProtocolManager protocol, MapField field)
	{
		short len = FieldType.TypeSizes[field.type];
		if (len < 0)
			writer.println("!!! ERROR: Unhandled FieldType " + field.type + " !!!");
		return len;
	}

	static void WriteFieldToString(PrintWriter writer, int indent, String fieldName, MapField field, String index)
	{
		String lead = spaces.substring(0, indent);
		String index1 = Helpers.EmptyString, index2 = Helpers.EmptyString;
		if (!index.isEmpty())
		{
			index1 = "[\" + " + index + " + \"]";
			index2 = "[" + index + "]";
		}
		writer.write(lead);

		switch (field.type)
		{
			case FieldType.Variable:
				writer.println("output += Helpers.FieldToString(_" + fieldName.toLowerCase() + ", \"" + fieldName + "\") + \"\\n\";");
				break;
			case FieldType.Fixed:
				writer.println("output += Helpers.FieldToString(" + fieldName + ", \"" + fieldName + index2 + "\") + \"\\n\";");
				break;
			case FieldType.BOOL:
				writer.println("output += \"" + fieldName + index1 + ": \" + Boolean.toString(" + fieldName + index2 + ") + \"\\n\";");
				break;
			case FieldType.F32:
				writer.println("output += \"" + fieldName + index1 + ": \" + Float.toString(" + fieldName + index2 + ") + \"\\n\";");
				break;
			case FieldType.F64:
				writer.println("output += \"" + fieldName + index1 + ": \" + Double.toString(" + fieldName + index2 + ") + \"\\n\";");
				break;
			case FieldType.I8:
			case FieldType.U8:
				writer.println("output += \"" + fieldName + index1 + ": \" + Byte.toString(" + fieldName + index2 + ") + \"\\n\";");
				break;
			case FieldType.I16:
			case FieldType.U16:
			case FieldType.IPPORT:
				writer.println("output += \"" + fieldName + index1 + ": \" + Short.toString(" + fieldName + index2 + ") + \"\\n\";");
				break;
			case FieldType.I32:
			case FieldType.U32:
			case FieldType.IPADDR:
				writer.println("output += \"" + fieldName + index1 + ": \" + Integer.toString(" + fieldName + index2 + ") + \"\\n\";");
				break;
			case FieldType.I64:
			case FieldType.U64:
				writer.println("output += \"" + fieldName + index1 + ": \" + Long.toString(" + fieldName + index2 + ") + \"\\n\";");
				break;
			case FieldType.UUID:
			case FieldType.Vector3:
			case FieldType.Vector3d:
			case FieldType.Vector4:
			case FieldType.Quaternion:
				writer.println("output += \"" + fieldName + index1 + ": \" + " + fieldName + index2 + ".toString() + \"\\n\";");
				break;
			default:
				writer.println("output += \"" + fieldName + index1 + ": \" + Helpers.toString(" + fieldName + index2 + ") + \"\\n\";");
				break;
		}
	}
	
	static PrintWriter WriteHeader(File file, String template) throws IOException
	{
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		BufferedReader reader = new BufferedReader(new FileReader(template));
		while (reader.ready())
		{
			String line = reader.readLine();
			if (line != null)
			{
				writer.println(line);
			}
			else
			{
				break;
			}
		}
		reader.close();
		return writer;
	}

	static void WriteBlockClass(PrintWriter writer, ProtocolManager protocol, MapBlock block)
	{
		boolean variableFields = false;
		String blockName = protocol.keywordPosition(block.keywordIndex);

		writer.println("    public class " + blockName + "Block\n    {");

		for (int k = 0; k < block.Fields.size(); k++)
		{
			MapField field = block.Fields.get(k);
			WriteFieldMember(writer, protocol.keywordPosition(field.keywordIndex), field);

			if (field.type == FieldType.Variable)
			{
				variableFields = true;
			}
		}

		// Length property
		writer.println("");
		writer.println("        public int getLength(){");
		int length = 0;
		for (int k = 0; k < block.Fields.size(); k++)
		{
			MapField field = block.Fields.get(k);
			length += GetFieldLength(writer, protocol, field);
		}

		if (!variableFields)
		{
			writer.println("            return " + length + ";");
		}
		else
		{
			writer.println("            int length = " + length + ";");

			for (int k = 0; k < block.Fields.size(); k++)
			{
				MapField field = block.Fields.get(k);
				if (field.type == FieldType.Variable)
				{
					String fieldName = protocol.keywordPosition(field.keywordIndex);
					writer.println("            if (get" + fieldName + "() != null) { length += " + field.count
							+ " + get" + fieldName + "().length; }");
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
		if (variableFields)
		{
			writer.println("            int length;");
		}

		for (int k = 0; k < block.Fields.size(); k++)
		{
			MapField field = block.Fields.get(k);
			String fieldName = protocol.keywordPosition(field.keywordIndex);
			WriteFieldFromBytes(writer, 12, fieldName, field, Helpers.EmptyString);
		}

		writer.println("        }\n");

		// ToBytes() function
		writer.println("        public void ToBytes(ByteBuffer bytes) throws Exception\n        {");

		for (int k = 0; k < block.Fields.size(); k++)
		{
			MapField field = block.Fields.get(k);
			String fieldName = protocol.keywordPosition(field.keywordIndex);
			WriteFieldToBytes(writer, 12, fieldName, field, Helpers.EmptyString);
		}

		writer.println("        }\n");

		// toString() function
		writer.println("        @Override\n        public String toString()\n        {");
		writer.println("            String output = \"-- " + blockName + " --\\n\";");
		writer.println("            try {");

		for (int k = 0; k < block.Fields.size(); k++)
		{
			MapField field = block.Fields.get(k);
			String fieldName = protocol.keywordPosition(field.keywordIndex);
			WriteFieldToString(writer, 16, fieldName, field, Helpers.EmptyString);
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

	static void WritePacketClass(File packets_dir, String template, ProtocolManager protocol, MapPacket packet) throws IOException
	{
		String sanitizedName;
		boolean[] variableField = new boolean[FieldType.NumTypes];
		boolean hasVariableBlocks = false;
		PrintWriter writer = WriteHeader(new File(packets_dir, packet.Name + "Packet.java"), template);

		for (int i = 0; i < packet.Blocks.size(); i++)
		{
			MapBlock block = packet.Blocks.get(i);
			for (int k = 0; k < block.Fields.size(); k++)
			{
				MapField field = block.Fields.get(k);

				if (field.type == FieldType.Variable || field.type == FieldType.Fixed)
				{
					writer.println("import libomv.utils.Helpers;");
					k = block.Fields.size();
					i = packet.Blocks.size();
				}
			}
		}

		writer.println("import libomv.types.PacketHeader;");
		writer.println("import libomv.types.PacketFrequency;");

		for (int i = 0; i < packet.Blocks.size(); i++)
		{
			MapBlock block = packet.Blocks.get(i);

			if (block.count == -1)
				hasVariableBlocks = true;

			for (int k = 0; k < block.Fields.size(); k++)
			{
				MapField field = block.Fields.get(k);
				if (!variableField[field.type])
				{
					switch (field.type)
					{
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
					}
				}
			}
		}

		writer.println("\npublic class " + packet.Name + "Packet extends Packet\n{");

		// Write out each block class
		for (int k = 0; k < packet.Blocks.size(); k++)
		{
			MapBlock block = packet.Blocks.get(k);
			if (block.Fields.size() > 1 || block.Fields.get(0).type == FieldType.Variable)
			{
				WriteBlockClass(writer, protocol, block);
			}
		}

		// Header member
		writer.println("    private PacketHeader header;");
		writer.println("    @Override");
		writer.println("    public PacketHeader getHeader() { return header; }");
		writer.println("    @Override");
		writer.println("    public void setHeader(PacketHeader value) { header = value; }");

		// PacketType member
		writer.println("    @Override");
		writer.println("    public PacketType getType() { return PacketType." + packet.Name + "; }");

		// Block members
		for (int k = 0; k < packet.Blocks.size(); k++)
		{
			MapBlock block = packet.Blocks.get(k);
			String blockName = protocol.keywordPosition(block.keywordIndex);

			// TODO: More thorough name blacklisting

			if (block.Fields.size() > 1 || block.Fields.get(0).type == FieldType.Variable)
			{
				if (blockName.equals("Header"))
				{
					sanitizedName = "_" + blockName;
				}
				else
				{
					sanitizedName = blockName;
				}

				writer.println("    public " + blockName + "Block" + ((block.count != 1) ? "[] " : " ") + sanitizedName + ";");
			}
			else
			{
				MapField field = block.Fields.get(0);
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				writer.println("    public " + FieldTypeString(field.type) + ((block.count != 1) ? "[] " : " ") + fieldName + ";");
			}
		}

		writer.println("");

		// Default constructor
		writer.println("    public " + packet.Name + "Packet()\n    {");
		writer.println("        hasVariableBlocks = " + (hasVariableBlocks ? "true" : "false") + ";");
		writer.println("        header = new PacketHeader(PacketFrequency." + PacketFrequency.Names[packet.Frequency]
				+ ");");
		writer.println("        header.setID((short)" + packet.ID + ");");
		// Turn the reliable flag on by default
		writer.println("        header.setReliable(true);");
		if (packet.Encoded)
		{
			writer.println("        header.setZerocoded(true);");
		}
		for (int k = 0; k < packet.Blocks.size(); k++)
		{
			MapBlock block = packet.Blocks.get(k);
			String blockName = protocol.keywordPosition(block.keywordIndex);

			if (block.Fields.size() > 1 || block.Fields.get(0).type == FieldType.Variable)
			{
				if (blockName.equals("Header"))
				{
					sanitizedName = "_" + blockName;
				}
				else
				{
					sanitizedName = blockName;
				}

				if (block.count == 1)
				{
					// Single count block
					writer.println("        " + sanitizedName + " = new " + blockName + "Block();");
				}
				else if (block.count == -1)
				{
					// Variable count block
					writer.println("        " + sanitizedName + " = new " + blockName + "Block[0];");
				}
				else
				{
					// Multiple count block
					writer.println("        " + sanitizedName + " = new " + blockName + "Block[" + block.count + "];");
				}
			}
			else
			{
				MapField field = block.Fields.get(0);
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == 1)
				{
					// Single count block
					if (field.type == FieldType.UUID || field.type >= FieldType.Vector3 && field.type <= FieldType.Quaternion)
						writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "();");
				}
				else if (block.count == -1)
				{
					// Variable count block
					writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "[0];");
				}
				else
				{
					// Multiple count block
					writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "[" + block.count + "];");
				}
			}
		}
		writer.println("    }\n");

		// Constructor that takes a byte array and beginning position only (no prebuilt header)
		boolean seenVariable = false;
		writer.println("    public " + packet.Name + "Packet(ByteBuffer bytes) throws Exception");
		writer.println("    {");
		writer.println("        header = new PacketHeader(bytes, PacketFrequency." + PacketFrequency.Names[packet.Frequency] + ");");
		for (int k = 0; k < packet.Blocks.size(); k++)
		{
			MapBlock block = packet.Blocks.get(k);
			String blockName = protocol.keywordPosition(block.keywordIndex);
			if (block.Fields.size() > 1 || block.Fields.get(0).type == FieldType.Variable)
			{
				if (blockName.equals("Header"))
				{
					sanitizedName = "_" + blockName;
				}
				else
				{
					sanitizedName = blockName;
				}

				if (block.count == 1)
				{
					// Single count block
					writer.println("        " + sanitizedName + " = new " + blockName + "Block(bytes);");
				}
				else if (block.count == -1)
				{
					// Variable count block
					if (!seenVariable)
					{
						writer.println("        int count = bytes.get() & 0xFF;");
						seenVariable = true;
					}
					else
					{
						writer.println("        count = bytes.get() & 0xFF;");
					}
					writer.println("        " + sanitizedName + " = new " + blockName + "Block[count];");
					writer.println("        for (int j = 0; j < count; j++)\n        {");
					writer.println("            " + sanitizedName + "[j] = new " + blockName + "Block(bytes);");
					writer.println("        }");
				}
				else
				{
					// Multiple count block
					writer.println("        " + sanitizedName + " = new " + blockName + "Block[" + block.count + "];");
					writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
					writer.println("            " + sanitizedName + "[j] = new " + blockName + "Block(bytes);");
					writer.println("        }");
				}
			}
			else
			{
				MapField field = block.Fields.get(0);
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == 1)
				{
					// Single count block
					WriteFieldFromBytes(writer, 8, fieldName, field, Helpers.EmptyString);
				}
				else if (block.count == -1)
				{
					// Variable count block
					if (!seenVariable)
					{
						writer.println("        int count = bytes.get() & 0xFF;");
						seenVariable = true;
					}
					else
					{
						writer.println("        count = bytes.get() & 0xFF;");
					}
					writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "[count];");
					if (field.type == FieldType.I8 || field.type == FieldType.U8)
					{
						writer.println("        bytes.get(" + fieldName + ");");						
					}
					else
					{
						writer.println("        for (int j = 0; j < count; j++)\n        {");
						WriteFieldFromBytes(writer, 12, fieldName, field, "[j]");
						writer.println("        }");
					}
				}
				else
				{
					// Multiple count block
					writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "[" + block.count + "];");
					if (field.type == FieldType.I8 || field.type == FieldType.U8)
					{
						writer.println("        bytes.get(" + fieldName + ");");						
					}
					else
					{
						writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
						WriteFieldFromBytes(writer, 12, fieldName, field, "[j]");
						writer.println("        }");
					}
				}
			}
		}
		writer.println("     }\n");

		seenVariable = false;

		// Constructor that takes a byte array and a prebuilt header
		writer.println("    public " + packet.Name + "Packet(PacketHeader head, ByteBuffer bytes)");
		writer.println("    {");
		writer.println("        header = head;");
		for (int k = 0; k < packet.Blocks.size(); k++)
		{
			MapBlock block = packet.Blocks.get(k);
			String blockName = protocol.keywordPosition(block.keywordIndex);
			if (block.Fields.size() > 1 || block.Fields.get(0).type == FieldType.Variable)
			{
				if (blockName.equals("Header"))
				{
					sanitizedName = "_" + blockName;
				}
				else
				{
					sanitizedName = blockName;
				}

				if (block.count == 1)
				{
					// Single count block
					writer.println("        " + sanitizedName + " = new " + blockName + "Block(bytes);");
				}
				else if (block.count == -1)
				{
					// Variable count block
					if (!seenVariable)
					{
						writer.println("        int count = bytes.get() & 0xFF;");
						seenVariable = true;
					}
					else
					{
						writer.println("        count = bytes.get() & 0xFF;");
					}
					writer.println("        " + sanitizedName + " = new " + blockName + "Block[count];");
					writer.println("        for (int j = 0; j < count; j++)\n        {");
					writer.println("            " + sanitizedName + "[j] = new " + blockName + "Block(bytes);");
					writer.println("        }");
				}
				else
				{
					// Multiple count block
					writer.println("        " + sanitizedName + " = new " + blockName + "Block[" + block.count + "];");
					writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
					writer.println("            " + sanitizedName + "[j] = new " + blockName + "Block(bytes);");
					writer.println("        }");
				}
			}
			else
			{
				MapField field = block.Fields.get(0);
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == 1)
				{
					// Single count block
					WriteFieldFromBytes(writer, 8, fieldName, field, Helpers.EmptyString);
				}
				else if (block.count == -1)
				{
					// Variable count block
					if (!seenVariable)
					{
						writer.println("        int count = bytes.get() & 0xFF;");
						seenVariable = true;
					}
					else
					{
						writer.println("        count = bytes.get() & 0xFF;");
					}
					writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "[count];");
					if (field.type == FieldType.I8 || field.type == FieldType.U8)
					{
						writer.println("        bytes.get(" + fieldName + ");");						
					}
					else
					{
						writer.println("        for (int j = 0; j < count; j++)\n        {");
						WriteFieldFromBytes(writer, 12, fieldName, field, "[j]");
						writer.println("        }");
					}
				}
				else
				{
					// Multiple count block
					writer.println("        " + fieldName + " = new " + FieldTypeString(field.type) + "[" + block.count + "];");
					if (field.type == FieldType.I8 || field.type == FieldType.U8)
					{
						writer.println("        bytes.get(" + fieldName + ");");						
					}
					else
					{
						writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
						WriteFieldFromBytes(writer, 12, fieldName, field, "[j]");
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

		writer.println("        int length = header.getLength();");

		for (int k = 0; k < packet.Blocks.size(); k++)
		{
			MapBlock block = packet.Blocks.get(k);
			String blockName = protocol.keywordPosition(block.keywordIndex);
			if (block.Fields.size() > 1 || block.Fields.get(0).type == FieldType.Variable)
			{
				if (blockName.equals("Header"))
				{
					sanitizedName = "_" + blockName;
				}
				else
				{
					sanitizedName = blockName;
				}

				if (block.count == 1)
				{
					// Single count block
					writer.println("        length += " + sanitizedName + ".getLength();");
				}
			}
			else
			{
				MapField field = block.Fields.get(0);
				if (block.count == 1)
				{
					writer.println("        length += " + GetFieldLength(writer, protocol, field) + ";");
				}
			}
		}

		for (int k = 0; k < packet.Blocks.size(); k++)
		{
			MapBlock block = packet.Blocks.get(k);
			String blockName = protocol.keywordPosition(block.keywordIndex);
			if (block.Fields.size() > 1 || block.Fields.get(0).type == FieldType.Variable)
			{
				if (blockName.equals("Header"))
				{
					sanitizedName = "_" + blockName;
				}
				else
				{
					sanitizedName = blockName;
				}

				if (block.count == -1)
				{
					writer.println("        length++;");
					writer.println("        for (int j = 0; j < " + sanitizedName + ".length; j++) { length += "
							+ sanitizedName + "[j].getLength(); }");
				}
				else if (block.count > 1)
				{
					writer.println("        for (int j = 0; j < " + block.count + "; j++) { length += " + sanitizedName
							+ "[j].getLength(); }");
				}
			}
			else
			{
				MapField field = block.Fields.get(0);
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == -1)
				{
					writer.println("        length++;");
					writer.println("        length += " + fieldName + ".length * " + GetFieldLength(writer, protocol, field) + ";");
				}
				else if (block.count > 1)
				{
					writer.println("        length += " + block.count + " * " + GetFieldLength(writer, protocol, field) + ";");
				}
			}
		}

		writer.println("        return length;\n    }\n");

		// ToBytes() function
		writer.println("    @Override");
		writer.println("    public ByteBuffer ToBytes() throws Exception");
		writer.println("    {");
		writer.println("        ByteBuffer bytes = ByteBuffer.allocate(getLength());");
		writer.println("        header.ToBytes(bytes);");
		writer.println("        bytes.order(ByteOrder.LITTLE_ENDIAN);");
		for (int k = 0; k < packet.Blocks.size(); k++)
		{
			MapBlock block = packet.Blocks.get(k);
			String blockName = protocol.keywordPosition(block.keywordIndex);
			if (block.Fields.size() > 1 || block.Fields.get(0).type == FieldType.Variable)
			{
				if (blockName.equals("Header"))
				{
					sanitizedName = "_" + blockName;
				}
				else
				{
					sanitizedName = blockName;
				}

				if (block.count == -1)
				{
					// Variable count block
					writer.println("        bytes.put((byte)" + sanitizedName + ".length);");
					writer.println("        for (int j = 0; j < " + sanitizedName + ".length; j++) { " + sanitizedName
							+ "[j].ToBytes(bytes); }");
				}
				else if (block.count == 1)
				{
					writer.println("        " + sanitizedName + ".ToBytes(bytes);");
				}
				else
				{
					// Multiple count block
					writer.println("        for (int j = 0; j < " + block.count + "; j++) { " + sanitizedName
							+ "[j].ToBytes(bytes); }");
				}
			}
			else
			{
				MapField field = block.Fields.get(0);
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == 1)
				{
					WriteFieldToBytes(writer, 8, fieldName, field, Helpers.EmptyString);
				}
				else if (block.count == -1)
				{
					// Variable count block
					writer.println("        bytes.put((byte)" + fieldName + ".length);");
					if (field.type == FieldType.I8 || field.type == FieldType.U8)
					{
						writer.println("        bytes.put(" + fieldName + ");");						
					}
					else
					{
						writer.println("        for (int j = 0; j < " + fieldName + ".length; j++)\n        {");
						WriteFieldToBytes(writer, 12, fieldName, field, "[j]");
						writer.println("        }");
					}
				}
				else
				{
					// Multiple count block
					if (field.type == FieldType.I8 || field.type == FieldType.U8)
					{
						writer.println("        bytes.put(" + fieldName + ");");						
					}
					else
					{
						writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
						WriteFieldToBytes(writer, 12, fieldName, field, "[j]");
						writer.println("        }");
					}
				}
			}
		}

		writer.println("        return bytes;\n    }\n");

		// toString() function
		writer.println("    @Override");
		writer.println("    public String toString()\n    {");
		writer.println("        String output = \"--- " + packet.Name + " ---\\n\";");

		for (int k = 0; k < packet.Blocks.size(); k++)
		{
			MapBlock block = packet.Blocks.get(k);
			String blockName = protocol.keywordPosition(block.keywordIndex);
			if (block.Fields.size() > 1 || block.Fields.get(0).type == FieldType.Variable)
			{
				if (blockName.equals("Header"))
				{
					sanitizedName = "_" + blockName;
				}
				else
				{
					sanitizedName = blockName;
				}

				if (block.count == -1)
				{
					// Variable count block
					writer.println("        for (int j = 0; j < " + sanitizedName + ".length; j++)\n        {");
					writer.println("            output += " + sanitizedName + "[j].toString() + \"\\n\";\n        }");
				}
				else if (block.count == 1)
				{
					writer.println("        output += " + sanitizedName + ".toString() + \"\\n\";");
				}
				else
				{
					// Multiple count block
					writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
					writer.println("            output += " + sanitizedName + "[j].toString() + \"\\n\";\n        }");
				}
			}
			else
			{
				MapField field = block.Fields.get(0);
				String fieldName = protocol.keywordPosition(field.keywordIndex);
				if (block.count == 1)
				{
					WriteFieldToString(writer, 8, fieldName, field, Helpers.EmptyString);
				}
				else if (block.count == -1)
				{
					// Variable count block
					writer.println("        for (int j = 0; j < " + fieldName + ".length; j++)\n        {");
					WriteFieldToString(writer, 12, fieldName, field, "j");
					writer.println("        }");
				}
				else
				{
					// Multiple count block
					writer.println("        for (int j = 0; j < " + block.count + "; j++)\n        {");
					WriteFieldToString(writer, 12, fieldName, field, "j");
					writer.println("        }");
				}
				
			}
		}

		writer.println("        return output;\n    }");

		// Closing function bracket
		writer.println("}");
		writer.close();
	}

	static public void main(String[] args)
	{
		ProtocolManager protocol = null;
		PrintWriter writer = null;

		try
		{
			if (args.length < 3)
			{
				System.out
						.println("Invalid arguments, using default values for [message_template.msg] [template.java.txt] [Packet.java]");
				args = new String[] { "src/libomv/mapgenerator/message_template.msg",
						"src/libomv/mapgenerator/template.java.txt", "src/libomv/packets/Packet.java" };
			}

			File packets_dir = new File(args[2]).getParentFile();
			protocol = new ProtocolManager(args[0], false);

			/* Open Packet.java file and copy file header from template */
			writer = WriteHeader(new File(args[2]), args[1]);

			PrintWriter packettype_writer = new PrintWriter(new FileWriter(new File(packets_dir, "PacketType.java")));

			// Write the PacketType enum
			packettype_writer.println("package libomv.packets;\npublic enum PacketType\n{\n    Default,");
			for (int k = 0; k < protocol.LowMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.LowMaps.mapPackets.get(k);
				if (packet != null)
				{
					packettype_writer.println("    " + packet.Name + ",");
				}
			}
			for (int k = 0; k < protocol.MediumMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.MediumMaps.mapPackets.get(k);
				if (packet != null)
				{
					packettype_writer.println("    " + packet.Name + ",");
				}
			}
			for (int k = 0; k < protocol.HighMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.HighMaps.mapPackets.get(k);
				if (packet != null)
				{
					packettype_writer.println("    " + packet.Name + ",");
				}
			}
			packettype_writer.println("}\n");
			packettype_writer.close();

			// Write the base Packet class
			writer.println("import libomv.StructuredData.OSDMap;\n"
					+ "import libomv.capabilities.CapsMessage.CapsEventType;\n"
					+ "import libomv.types.PacketHeader;\n"
					+ "import libomv.types.PacketFrequency;\n\n"
					+ "public abstract class Packet\n"
					+ "{\n"
					+ "    public static final int MTU = 1200;\n\n"
					+ "    public boolean hasVariableBlocks;\n"
					+ "    public abstract PacketHeader getHeader();\n"
					+ "    public abstract void setHeader(PacketHeader value);\n"
					+ "    public abstract PacketType getType();\n"
					+ "    public abstract int getLength();\n"
					+ "    // Serializes the packet in to a byte array\n"
					+ "    // return A byte array containing the serialized packet payload, ready to be sent across the wire\n"
					+ "    public abstract ByteBuffer ToBytes() throws Exception;\n\n"
					+ "    public ByteBuffer[] ToBytesMultiple()\n    {\n"
					+ "         throw new UnsupportedOperationException(\"ToBytesMultiple()\");\n    }\n"
					+ "    //Get the PacketType for a given packet id and packet frequency\n"
					+ "    //<param name=\"id\">The packet ID from the header</param>\n"
					+ "    //<param name=\"frequency\">Frequency of this packet</param>\n"
					+ "    //<returns>The packet type, or PacketType.Default</returns>\n"
					+ "    public static PacketType getType(short id, byte frequency)\n    {\n"
					+ "        switch (frequency)\n        {\n            case PacketFrequency.Low:\n"
					+ "                switch (id)\n                {");

			for (int k = 0; k < protocol.LowMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.LowMaps.mapPackets.get(k);
				if (packet != null)
				{
					writer.println("                        case (short)" + packet.ID + ": return PacketType."
							+ packet.Name + ";");
				}
			}

			writer.println("                    }\n                    break;\n"
					+ "                case PacketFrequency.Medium:\n                    switch (id)\n                    {");

			for (int k = 0; k < protocol.MediumMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.MediumMaps.mapPackets.get(k);
				if (packet != null)
				{
					writer.println("                        case " + packet.ID + ": return PacketType."
							+ packet.Name + ";");
				}
			}

			writer.println("                    }\n                    break;\n"
					+ "                case PacketFrequency.High:\n                    switch (id)\n"
					+ "                    {");

			for (int k = 0; k < protocol.HighMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.HighMaps.mapPackets.get(k);
				if (packet != null)
				{
					writer.println("                        case " + packet.ID + ": return PacketType."
							+ packet.Name + ";");
				}
			}

			writer.println("                    }\n                    break;\n            }\n\n"
					+ "            return PacketType.Default;\n        }\n");

			writer.println("        /**\n"
					+ "         * Construct a packet in it's native class from a capability OSD structure\n"
					+ "         *\n"
					+ "         * @param bytes Byte array containing the packet, starting at position 0\n"
					+ "         * @param packetEnd The last byte of the packet. If the packet was 76 bytes long, packetEnd would be 75\n"
					+ "         * @returns The native packet class for this type of packet, typecasted to the generic Packet\n"
					+ "         */\n"
					+ "        public static Packet BuildPacket(CapsEventType capsKey,  OSDMap map) throws Exception\n"
					+ "        {\n            return null;\n        }\n\n");

			writer.println("        /**\n"
					+ "         * Construct a packet in it's native class from a byte array\n"
					+ "         *\n"
					+ "         * @param bytes Byte array containing the packet, starting at position 0\n"
					+ "         * @param packetEnd The last byte of the packet. If the packet was 76 bytes long, packetEnd would be 75\n"
					+ "         * @returns The native packet class for this type of packet, typecasted to the generic Packet\n"
					+ "         */\n        public static Packet BuildPacket(ByteBuffer bytes) throws Exception\n"
					+ "        {\n            PacketHeader header = new PacketHeader(bytes);\n"
					+ "            bytes.order(ByteOrder.LITTLE_ENDIAN);\n"
					+ "            bytes.position(header.getLength());\n\n"
					+ "            switch (header.getFrequency())            {\n"
					+ "                case PacketFrequency.Low:\n                    switch (header.getID())\n"
					+ "                    {");
			for (int k = 0; k < protocol.LowMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.LowMaps.mapPackets.get(k);
				if (packet != null)
				{
					writer.println("                        case (short)" + packet.ID + ": return new " + packet.Name
							+ "Packet(header,bytes);");
				}
			}
			writer.println("                    }\n                case PacketFrequency.Medium:\n"
					+ "                    switch (header.getID())\n                    {");
			for (int k = 0; k < protocol.MediumMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.MediumMaps.mapPackets.get(k);
				if (packet != null)
				{
					writer.println("                        case " + packet.ID + ": return new " + packet.Name
							+ "Packet(header, bytes);");
				}
			}
			writer.println("                    }\n                case PacketFrequency.High:\n"
					+ "                    switch (header.getID())\n                    {");
			for (int k = 0; k < protocol.HighMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.HighMaps.mapPackets.get(k);
				if (packet != null)
				{
					writer.println("                        case " + packet.ID + ": return new " + packet.Name
							+ "Packet(header, bytes);");
				}
			}
			writer.println("                    }\n            }\n"
					+ "            throw new Exception(\"Unknown packet ID\");\n        }\n");

			// Write the packet classes
			for (int k = 0; k < protocol.LowMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.LowMaps.mapPackets.get(k);
				if (packet != null)
				{
					WritePacketClass(packets_dir, args[1], protocol, packet);
				}
			}

			for (int k = 0; k < protocol.MediumMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.MediumMaps.mapPackets.get(k);
				if (packet != null)
				{
					WritePacketClass(packets_dir, args[1], protocol, packet);
				}
			}

			for (int k = 0; k < protocol.HighMaps.mapPackets.size(); k++)
			{
				MapPacket packet = protocol.HighMaps.mapPackets.get(k);
				if (packet != null)
				{
					WritePacketClass(packets_dir, args[1], protocol, packet);
				}
			}
			writer.println("}");
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
