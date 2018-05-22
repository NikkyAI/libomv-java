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
package libomv.types;

import libomv.utils.Helpers;
import libomv.utils.RefObject;

public final class NameValue {
	/* Type of the value */
	public enum ValueType {
		// Unknown
		Unknown(-1),
		// String value
		String(0),

		F32(1),

		S32(2),

		VEC3(3),

		U32(4),
		// Deprecated
		CAMERA(5),
		// String value, but designated as an asset
		Asset(6),

		U64(7);

		public int val;

		private ValueType(int val) {
			this.val = val;
		}
	}

	public enum ClassType {
		Unknown(-1), ReadOnly(0), ReadWrite(1), Callback(2);

		public int val;

		private ClassType(int val) {
			this.val = val;
		}
	}

	public enum SendtoType {
		Unknown(-1), Sim(0), DataSim(1), SimViewer(2), DataSimViewer(3);

		public int val;

		private SendtoType(int val) {
			this.val = val;
		}
	}

	private static final String[] TYPE_STRINGS = new String[] { "STRING", "F32", "S32", "VEC3", "U32", "ASSET", "U64" };
	private static final String[] CLASS_STRINGS = new String[] { "R", "RW", "CB" };
	private static final String[] SENDTO_STRINGS = new String[] { "S", "DS", "SV", "DSV" };
	private static final char[] SEPARATORS = new char[] { ' ', '\n', '\t', '\r' };

	public String name;
	public ValueType type;
	public ClassType classType;
	public SendtoType sendto;
	public Object valueObject;

	/**
	 * Constructor that takes all the fields as parameters
	 *
	 * @param name
	 * @param valueType
	 * @param classType
	 * @param sendtoType
	 * @param value
	 */
	public NameValue(String name, ValueType valueType, ClassType classType, SendtoType sendtoType, Object value) {
		this.name = name;
		this.type = valueType;
		this.classType = classType;
		this.sendto = sendtoType;
		this.valueObject = value;
	}

	/**
	 * Constructor that takes a single line from a NameValue field
	 *
	 * @param data
	 */
	public NameValue(String data) {
		int i;

		// Name
		i = Helpers.indexOfAny(data, SEPARATORS);
		if (i < 1) {
			name = Helpers.EmptyString;
			type = ValueType.Unknown;
			classType = ClassType.Unknown;
			sendto = SendtoType.Unknown;
			valueObject = null;
			return;
		}
		name = data.substring(0, i);
		data = data.substring(i + 1);

		// Type
		i = Helpers.indexOfAny(data, SEPARATORS);
		if (i > 0) {
			type = getValueType(data.substring(0, i));
			data = data.substring(i + 1);

			// Class
			i = Helpers.indexOfAny(data, SEPARATORS);
			if (i > 0) {
				classType = getClassType(data.substring(0, i));
				data = data.substring(i + 1);

				// Sendto
				i = Helpers.indexOfAny(data, SEPARATORS);
				if (i > 0) {
					sendto = getSendtoType(data.substring(0, 1));
					data = data.substring(i + 1);
				}
			}
		}

		// Value
		type = ValueType.String;
		classType = ClassType.ReadOnly;
		sendto = SendtoType.Sim;
		valueObject = null;
		setValue(data);
	}

	public static String nameValuesToString(NameValue[] values) {
		if (values == null || values.length == 0) {
			return "";
		}

		StringBuilder output = new StringBuilder();

		for (int i = 0; i < values.length; i++) {
			NameValue value = values[i];

			if (value.valueObject != null) {
				String newLine = (i < values.length - 1) ? "\n" : "";
				output.append(String.format("%s %s %s %s %s%s", value.name, TYPE_STRINGS[value.type.val],
						CLASS_STRINGS[value.classType.val], SENDTO_STRINGS[value.sendto.val], value.valueObject,
						newLine));
			}
		}

		return output.toString();
	}

	private void setValue(String value) {
		switch (type) {
		case Asset:
		case String:
			valueObject = value;
			break;
		case F32: {
			float temp = Helpers.tryParseFloat(value);
			valueObject = temp;
			break;
		}
		case S32: {
			int temp;
			temp = Helpers.tryParseInt(value);
			valueObject = temp;
			break;
		}
		case U32: {
			int temp = Helpers.tryParseInt(value);
			valueObject = temp;
			break;
		}
		case U64: {
			long temp = Helpers.tryParseLong(value);
			valueObject = temp;
			break;
		}
		case VEC3: {
			RefObject<Vector3> temp = new RefObject<>((Vector3) valueObject);
			Vector3.tryParse(value, temp);
			break;
		}
		default:
			valueObject = null;
			break;
		}
	}

	private static ValueType getValueType(String value) {
		ValueType type = ValueType.Unknown;
		int i = 1;
		for (String s : TYPE_STRINGS) {
			if (s.equals(value)) {
				type = ValueType.values()[i];
			}
		}

		if (type == ValueType.Unknown) {
			type = ValueType.String;
		}

		return type;
	}

	private static ClassType getClassType(String value) {
		ClassType type = ClassType.Unknown;
		int i = 1;
		for (String s : CLASS_STRINGS) {
			if (s.equals(value)) {
				type = ClassType.values()[i];
			}
		}

		if (type == ClassType.Unknown) {
			type = ClassType.ReadOnly;
		}

		return type;
	}

	private static SendtoType getSendtoType(String value) {
		SendtoType type = SendtoType.Unknown;
		int i = 1;
		for (String s : SENDTO_STRINGS) {
			if (s.equals(value)) {
				type = SendtoType.values()[i];
			}
		}

		if (type == SendtoType.Unknown) {
			type = SendtoType.Sim;
		}

		return type;
	}
}
