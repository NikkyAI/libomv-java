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
package libomv.primitives;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.types.Color4;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.RefObject;

public class TextureEntry {
	// The type of bump-mapping applied to a face
	public enum Bumpiness {
		None, Brightness, Darkness, Woodgrain, Bark, Bricks, Checker, Concrete, Crustytile, Cutstone, Discs, Gravel, Petridish, Siding, Stonetile, Stucco, Suction, Weave;

		public static Bumpiness setValue(int value) {
			return values()[value];
		}

		public byte getValue() {
			return (byte) ordinal();
		}
	}

	// The level of shininess applied to a face
	public enum Shininess {
		None(0), Low(0x40), Medium(0x80), High(0xC0);

		public static Shininess setValue(int value) {
			for (Shininess e : values()) {
				if ((e._value & value) != 0)
					return e;
			}
			return None;
		}

		public byte getValue() {
			return _value;
		}

		private final byte _value;

		private Shininess(int value) {
			this._value = (byte) value;
		}
	}

	// The texture mapping style used for a face
	public enum MappingType {
		Default(0), Planar(2);

		public static MappingType setValue(int value) {
			for (MappingType e : values()) {
				if (e._value == value)
					return e;
			}
			return Default;
		}

		public byte getValue() {
			return _value;
		}

		private final byte _value;

		private MappingType(int value) {
			this._value = (byte) value;
		}
	}

	// Flags in the TextureEntry block that describe which properties are set
	// [Flags]
	public static class TextureAttributes {
		public static final int None = 0;
		public static final int TextureID = 1 << 0;
		public static final int RGBA = 1 << 1;
		public static final int RepeatU = 1 << 2;
		public static final int RepeatV = 1 << 3;
		public static final int OffsetU = 1 << 4;
		public static final int OffsetV = 1 << 5;
		public static final int Rotation = 1 << 6;
		public static final int Material = 1 << 7;
		public static final int Media = 1 << 8;
		public static final int Glow = 1 << 9;
		public static final int MaterialID = 1 << 10;
		public static final int All = 0xFFFFFFFF;

		public static int setValue(int value) {
			return value & _mask;
		}

		public static int getValue(int value) {
			return value & _mask;
		}

		private static int _mask = All;
	}

	// Texture animation mode
	// [Flags]
	public static class TextureAnimMode {
		// Disable texture animation
		public static final byte ANIM_OFF = 0x00;
		// Enable texture animation
		public static final byte ANIM_ON = 0x01;
		// Loop when animating textures
		public static final byte LOOP = 0x02;
		// Animate in reverse direction
		public static final byte REVERSE = 0x04;
		// Animate forward then reverse
		public static final byte PING_PONG = 0x08;
		// Slide texture smoothly instead of frame-stepping
		public static final byte SMOOTH = 0x10;
		// Rotate texture instead of using frames
		public static final byte ROTATE = 0x20;
		// Scale texture instead of using frames
		public static final byte SCALE = 0x40;

		public static byte setValue(int value) {
			return (byte) (value & _mask);
		}

		public static Byte getValue(byte value) {
			return (byte) (value & _mask);
		}

		private static byte _mask = 0x7F;
	}

	// #endregion Enums

	// #region Subclasses

	// A single textured face. Don't instantiate this class yourself, use the
	// methods in TextureEntry
	public class TextureEntryFace implements Cloneable {
		// +----------+ S = Shiny
		// | SSFBBBBB | F = Fullbright
		// | 76543210 | B = Bumpmap
		// +----------+
		private final byte BUMP_MASK = 0x1F;
		private final byte FULLBRIGHT_MASK = 0x20;
		private final byte SHINY_MASK = (byte) 0xC0;
		// +----------+ M = Media Flags (web page)
		// | .....TTM | T = Texture Mapping
		// | 76543210 | . = Unused
		// +----------+
		private final byte MEDIA_MASK = 0x01;
		private final byte TEX_MAP_MASK = 0x06;

		private Color4 rgba;
		private float repeatU;
		private float repeatV;
		private float offsetU;
		private float offsetV;
		private float rotation;
		private float glow;
		private byte material;
		private byte media;
		private int hasAttribute;
		private UUID textureID;
		private UUID materialID;
		private TextureEntryFace defaultTexture;

		// #region Properties
		public byte getMaterial() {
			if ((hasAttribute & TextureAttributes.Material) != 0)
				return material;
			return defaultTexture.material;
		}

		public void setMaterial(byte value) {
			material = value;
			hasAttribute |= TextureAttributes.Material;
		}

		public byte getMedia() {
			if ((hasAttribute & TextureAttributes.Media) != 0)
				return media;
			return defaultTexture.media;
		}

		public void setMedia(byte value) {
			media = value;
			hasAttribute |= TextureAttributes.Media;
		}

		public Color4 getRGBA() {
			if ((hasAttribute & TextureAttributes.RGBA) != 0)
				return rgba;
			return defaultTexture.rgba;
		}

		public void setRGBA(Color4 value) {
			rgba = value;
			hasAttribute |= TextureAttributes.RGBA;
		}

		public float getRepeatU() {
			if ((hasAttribute & TextureAttributes.RepeatU) != 0)
				return repeatU;
			return defaultTexture.repeatU;
		}

		public void setRepeatU(float value) {
			repeatU = value;
			hasAttribute |= TextureAttributes.RepeatU;
		}

		public float getRepeatV() {
			if ((hasAttribute & TextureAttributes.RepeatV) != 0)
				return repeatV;
			return defaultTexture.repeatV;
		}

		public void setRepeatV(float value) {
			repeatV = value;
			hasAttribute |= TextureAttributes.RepeatV;
		}

		public float getOffsetU() {
			if ((hasAttribute & TextureAttributes.OffsetU) != 0)
				return offsetU;
			return defaultTexture.offsetU;
		}

		public void setOffsetU(float value) {
			offsetU = value;
			hasAttribute |= TextureAttributes.OffsetU;
		}

		public float getOffsetV() {
			if ((hasAttribute & TextureAttributes.OffsetV) != 0)
				return offsetV;
			return defaultTexture.offsetV;
		}

		public void setOffsetV(float value) {
			offsetV = value;
			hasAttribute |= TextureAttributes.OffsetV;
		}

		public float getRotation() {
			if ((hasAttribute & TextureAttributes.Rotation) != 0)
				return rotation;
			return defaultTexture.rotation;
		}

		public void setRotation(float value) {
			rotation = value;
			hasAttribute |= TextureAttributes.Rotation;
		}

		public float getGlow() {
			if ((hasAttribute & TextureAttributes.Glow) != 0)
				return glow;
			return defaultTexture.glow;
		}

		public void setGlow(float value) {
			glow = value;
			hasAttribute |= TextureAttributes.Glow;
		}

		public Bumpiness getBump() {
			if ((hasAttribute & TextureAttributes.Material) != 0)
				return Bumpiness.setValue(material & BUMP_MASK);
			return defaultTexture.getBump();
		}

		public void setBump(Bumpiness value) {
			// Clear out the old material value
			material &= ~BUMP_MASK;
			// Put the new bump value in the material byte
			material |= value.getValue();
			hasAttribute |= TextureAttributes.Material;
		}

		public Shininess getShiny() {
			if ((hasAttribute & TextureAttributes.Material) != 0)
				return Shininess.setValue(material & SHINY_MASK);
			return defaultTexture.getShiny();
		}

		public void setShiny(Shininess value) {
			// Clear out the old shiny value
			material &= ~SHINY_MASK;
			// Put the new shiny value in the material byte
			material |= value.getValue();
			hasAttribute |= TextureAttributes.Material;
		}

		public boolean getFullbright() {
			if ((hasAttribute & TextureAttributes.Material) != 0)
				return (material & FULLBRIGHT_MASK) != 0;
			return defaultTexture.getFullbright();
		}

		public void setFullbright(boolean value) {
			// Clear out the old fullbright value
			material &= ~FULLBRIGHT_MASK;
			if (value) {
				material |= FULLBRIGHT_MASK;
				hasAttribute |= TextureAttributes.Material;
			}
		}

		// In the future this will specify whether a webpage is attached to this
		// face
		public boolean getMediaFlags() {
			if ((hasAttribute & TextureAttributes.Media) != 0)
				return (media & MEDIA_MASK) != 0;
			return defaultTexture.getMediaFlags();
		}

		public void setMediaFlags(boolean value) {
			// Clear out the old mediaflags value
			media &= ~MEDIA_MASK;
			if (value) {
				media |= MEDIA_MASK;
				hasAttribute |= TextureAttributes.Media;
			}
		}

		public MappingType getTexMapType() {
			if ((hasAttribute & TextureAttributes.Media) != 0)
				return MappingType.setValue(media & TEX_MAP_MASK);
			return defaultTexture.getTexMapType();
		}

		public void setTexMapType(MappingType value) {
			// Clear out the old texmap value
			media &= ~TEX_MAP_MASK;
			// Put the new texmap value in the media byte
			media |= value.getValue();
			hasAttribute |= TextureAttributes.Media;
		}

		public UUID getTextureID() {
			if ((hasAttribute & TextureAttributes.TextureID) != 0)
				return textureID;
			return defaultTexture.textureID;
		}

		public void setTextureID(UUID value) {
			textureID = value;
			hasAttribute |= TextureAttributes.TextureID;
		}

		public UUID getMaterialID() {
			if ((hasAttribute & TextureAttributes.MaterialID) != 0)
				return materialID;
			return defaultTexture.materialID;
		}

		public void setMaterialID(UUID value) {
			materialID = value;
			hasAttribute |= TextureAttributes.MaterialID;
		}

		/**
		 * Contains the definition for individual faces
		 *
		 * @param defaultTexture
		 */
		public TextureEntryFace(TextureEntryFace defaultText) {
			rgba = Color4.White;
			repeatU = 1.0f;
			repeatV = 1.0f;

			defaultTexture = defaultText;
			// FIXME: Is this really correct or should this be reversed?
			if (defaultTexture == null)
				hasAttribute = TextureAttributes.All;
			else
				hasAttribute = TextureAttributes.None;
		}

		public TextureEntryFace(OSD osd, TextureEntryFace defaultText, RefObject<Integer> faceNumber) {
			this(defaultText);
			fromOSD(osd, faceNumber);
		}

		public OSD serialize(int faceNumber) {
			OSDMap tex = new OSDMap(10);
			if (faceNumber >= 0)
				tex.put("face_number", OSD.FromInteger(faceNumber));
			tex.put("colors", OSD.FromColor4(getRGBA()));
			tex.put("scales", OSD.FromReal(getRepeatU()));
			tex.put("scalet", OSD.FromReal(getRepeatV()));
			tex.put("offsets", OSD.FromReal(getOffsetU()));
			tex.put("offsett", OSD.FromReal(getOffsetV()));
			tex.put("imagerot", OSD.FromReal(getRotation()));
			tex.put("bump", OSD.FromInteger(getBump().getValue()));
			// tex.put("shiny", OSD.FromInteger(getShiny().getValue()));
			tex.put("fullbright", OSD.FromBoolean(getFullbright()));
			tex.put("media_flags", OSD.FromInteger(getMediaFlags() ? 1 : 0));
			// tex.put("mapping", OSD.FromInteger(getTexMapType().getValue()));
			tex.put("glow", OSD.FromReal(getGlow()));

			if (getTextureID().equals(TextureEntry.WHITE_TEXTURE))
				tex.put("imageid", OSD.FromUUID(UUID.Zero));
			else
				tex.put("imageid", OSD.FromUUID(getTextureID()));

			tex.put("materialid", OSD.FromUUID(getMaterialID()));

			return tex;
		}

		public void fromOSD(OSD osd, RefObject<Integer> faceNumber) {
			if (osd instanceof OSDMap) {
				OSDMap map = (OSDMap) osd;

				faceNumber.argvalue = map.containsKey("face_number") ? map.get("face_number").AsInteger() : -1;
				setRGBA(map.get("colors").AsColor4());
				setRepeatU((float) map.get("scales").AsReal());
				setRepeatV((float) map.get("scalet").AsReal());
				setOffsetU((float) map.get("offsets").AsReal());
				setOffsetV((float) map.get("offsett").AsReal());
				setRotation((float) map.get("imagerot").AsReal());
				setBump(Bumpiness.setValue(map.get("bump").AsInteger()));
				// setShiny(Shininess.setValue(map.get("shiny").AsInteger()));
				setFullbright(map.get("fullbright").AsBoolean());
				setMediaFlags(map.get("media_flags").AsBoolean());
				// setTexMapType(MappingType.setValue(map.get("mapping").AsInteger()));
				setGlow((float) map.get("glow").AsReal());
				setTextureID(map.get("imageid").AsUUID());
				setMaterialID(map.get("materialid").AsUUID());
			}
		}

		@Override
		public TextureEntryFace clone() {
			TextureEntryFace ret = new TextureEntryFace(
					this.defaultTexture == null ? null : (TextureEntryFace) this.defaultTexture.clone());
			ret.rgba = rgba;
			ret.repeatU = repeatU;
			ret.repeatV = repeatV;
			ret.offsetU = offsetU;
			ret.offsetV = offsetV;
			ret.rotation = rotation;
			ret.glow = glow;
			ret.material = material;
			ret.media = media;
			ret.hasAttribute = hasAttribute;
			ret.textureID = textureID;
			ret.materialID = materialID;
			return ret;
		}

		public boolean equals(TextureEntryFace obj) {
			return obj != null && getRGBA().equals(obj.getRGBA()) && getRepeatU() == obj.getRepeatU()
					&& getRepeatV() == obj.getRepeatV() && getOffsetU() == obj.getOffsetU()
					&& getOffsetV() == obj.getOffsetV() && getRotation() == obj.getRotation()
					&& getGlow() == obj.getGlow() && getBump().equals(obj.getBump())
					&& getShiny().equals(obj.getShiny()) && getFullbright() == obj.getFullbright()
					&& getMediaFlags() == obj.getMediaFlags() && getTexMapType().equals(obj.getTexMapType())
					&& getTextureID().equals(obj.getTextureID()) && getMaterialID().equals(obj.getMaterialID());
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null && obj instanceof TextureEntryFace && equals((TextureEntryFace) obj);
		}

		@Override
		public int hashCode() {
			return getRGBA().hashCode() ^ (int) getRepeatU() ^ (int) getRepeatV() ^ (int) getOffsetU()
					^ (int) getOffsetV() ^ (int) getRotation() ^ (int) getGlow() ^ getBump().getValue()
					^ getShiny().getValue() ^ (getFullbright() ? 1 : 0) ^ (getMediaFlags() ? 1 : 0)
					^ getTexMapType().getValue() ^ getTextureID().hashCode() ^ getMaterialID().hashCode();
		}

		@Override
		public String toString() {
			return String.format("Color: %s RepeatU: %f RepeatV: %f OffsetU: %f OffsetV: %f "
					+ "Rotation: %f Bump: %s Shiny: %s Fullbright: %s Mapping: %s Media: %s Glow: %f ID: %s MaterialID: %s",
					getRGBA(), getRepeatU(), getRepeatV(), getOffsetU(), getOffsetV(), getRotation(), getBump(),
					getShiny(), getFullbright(), getTexMapType(), getMediaFlags(), getGlow(), getTextureID(),
					getMaterialID());
		}
	}

	// Controls the texture animation of a particular prim
	public class TextureAnimation {
		public byte Flags;
		public int Face;
		public int SizeX;
		public int SizeY;
		public float Start;
		public float Length;
		public float Rate;

		public TextureAnimation() {
			init();
		}

		public TextureAnimation(byte[] data, int pos) {
			if (data.length >= (16 + pos)) {
				Flags = TextureAnimMode.setValue(data[pos++]);
				Face = data[pos++];
				SizeX = data[pos++];
				SizeY = data[pos++];

				Start = Helpers.BytesToFloatL(data, pos);
				Length = Helpers.BytesToFloatL(data, pos + 4);
				Rate = Helpers.BytesToFloatL(data, pos + 8);
			} else {
				init();
			}
		}

		public TextureAnimation(byte[] data, int pos, int length) {
			if (length >= 16 && data.length >= (length + pos)) {
				Flags = TextureAnimMode.setValue(data[pos++]);
				Face = data[pos++];
				SizeX = data[pos++];
				SizeY = data[pos++];

				Start = Helpers.BytesToFloatL(data, pos);
				Length = Helpers.BytesToFloatL(data, pos + 4);
				Rate = Helpers.BytesToFloatL(data, pos + 8);
			} else {
				init();
			}
		}

		public TextureAnimation(OSD osd) {
			fromOSD(osd);
		}

		public TextureAnimation(TextureAnimation textureAnim) {
			Flags = textureAnim.Flags;
			Face = textureAnim.Face;
			SizeX = textureAnim.SizeX;
			SizeY = textureAnim.SizeY;
			Start = textureAnim.Start;
			Length = textureAnim.Length;
			Rate = textureAnim.Rate;
		}

		private void init() {
			Flags = TextureAnimMode.ANIM_OFF;
			Face = 0;
			SizeX = 0;
			SizeY = 0;

			Start = 0.0f;
			Length = 0.0f;
			Rate = 0.0f;
		}

		public byte[] getBytes() {
			byte[] data = new byte[16];
			int pos = 0;

			data[pos++] = TextureAnimMode.getValue(Flags);
			data[pos++] = (byte) Face;
			data[pos++] = (byte) SizeX;
			data[pos++] = (byte) SizeY;

			Helpers.FloatToBytesL(Start, data, pos);
			Helpers.FloatToBytesL(Length, data, pos + 4);
			Helpers.FloatToBytesL(Rate, data, pos + 8);

			return data;
		}

		public OSD serialize() {
			OSDMap map = new OSDMap();

			map.put("face", OSD.FromInteger(Face));
			map.put("flags", OSD.FromInteger(Flags));
			map.put("length", OSD.FromReal(Length));
			map.put("rate", OSD.FromReal(Rate));
			map.put("size_x", OSD.FromInteger(SizeX));
			map.put("size_y", OSD.FromInteger(SizeY));
			map.put("start", OSD.FromReal(Start));

			return map;
		}

		public void fromOSD(OSD osd) {
			if (osd instanceof OSDMap) {
				OSDMap map = (OSDMap) osd;

				Face = map.get("face").AsUInteger();
				Flags = TextureAnimMode.setValue(map.get("flags").AsUInteger());
				Length = (float) map.get("length").AsReal();
				Rate = (float) map.get("rate").AsReal();
				SizeX = map.get("size_x").AsUInteger();
				SizeY = map.get("size_y").AsUInteger();
				Start = (float) map.get("start").AsReal();
			} else {
				init();
			}
		}
	}

	/**
	 * Represents all of the texturable faces for an object
	 *
	 * Grid objects have infinite faces, with each face using the properties of the
	 * default face unless set otherwise. So if you have a TextureEntry with a
	 * default texture uuid of X, and face 18 has a texture UUID of Y, every face
	 * would be textured with X except for face 18 that uses Y. In practice however,
	 * primitives utilize a maximum of nine faces
	 */
	public static final int MAX_FACES = 32;
	public static final UUID WHITE_TEXTURE = new UUID("5748decc-f629-461c-9a36-a35a221fe21f");

	public TextureEntryFace defaultTexture;
	public TextureEntryFace[] faceTextures = new TextureEntryFace[MAX_FACES];

	private int numTextures = MAX_FACES;

	public int getNumTextures() {
		return numTextures;
	}

	public void setNumTextures(int value) {
		numTextures = value <= MAX_FACES ? value : MAX_FACES;
	}

	/**
	 * Constructor that takes a default texture UUID
	 *
	 * @param defaultTextureID
	 *            Texture UUID to use as the default texture
	 */
	public TextureEntry(UUID defaultTextureID) {
		defaultTexture = new TextureEntryFace(null);
		defaultTexture.setTextureID(defaultTextureID);
	}

	/**
	 * Constructor that takes a <code>TextureEntryFace</code> for the default face
	 *
	 * @param defaultFace
	 *            Face to use as the default face
	 */
	public TextureEntry(TextureEntryFace defaultFace) {
		defaultTexture = new TextureEntryFace(null);
		defaultTexture.setBump(defaultFace.getBump());
		defaultTexture.setFullbright(defaultFace.getFullbright());
		defaultTexture.setMediaFlags(defaultFace.getMediaFlags());
		defaultTexture.setOffsetU(defaultFace.getOffsetU());
		defaultTexture.setOffsetV(defaultFace.getOffsetV());
		defaultTexture.setRepeatU(defaultFace.getRepeatU());
		defaultTexture.setRepeatV(defaultFace.getRepeatV());
		defaultTexture.setRGBA(defaultFace.getRGBA());
		defaultTexture.setRotation(defaultFace.getRotation());
		defaultTexture.setGlow(defaultFace.getGlow());
		defaultTexture.setShiny(defaultFace.getShiny());
		defaultTexture.setTexMapType(defaultFace.getTexMapType());
		defaultTexture.setTextureID(defaultFace.getTextureID());
	}

	/**
	 * Constructor that takes a <code>TextureEntry</code> for the default face
	 *
	 * @param texture
	 *            Texture to copy
	 */
	public TextureEntry(TextureEntry texture) {
		defaultTexture = new TextureEntryFace(null);
		numTextures = texture.getNumTextures();
		for (int i = 0; i < numTextures; i++) {
			faceTextures[i] = texture.faceTextures[i];
			if (faceTextures[i] != null) {
				faceTextures[i] = new TextureEntryFace(texture.defaultTexture);
				faceTextures[i].setRGBA(texture.faceTextures[i].getRGBA());
				faceTextures[i].setRepeatU(texture.faceTextures[i].getRepeatU());
				faceTextures[i].setRepeatV(texture.faceTextures[i].getRepeatV());
				faceTextures[i].setOffsetU(texture.faceTextures[i].getOffsetU());
				faceTextures[i].setOffsetV(texture.faceTextures[i].getOffsetV());
				faceTextures[i].setRotation(texture.faceTextures[i].getRotation());
				faceTextures[i].setBump(texture.faceTextures[i].getBump());
				faceTextures[i].setShiny(texture.faceTextures[i].getShiny());
				faceTextures[i].setFullbright(texture.faceTextures[i].getFullbright());
				faceTextures[i].setMediaFlags(texture.faceTextures[i].getMediaFlags());
				faceTextures[i].setTexMapType(texture.faceTextures[i].getTexMapType());
				faceTextures[i].setGlow(texture.faceTextures[i].getGlow());
				faceTextures[i].setTextureID(texture.faceTextures[i].getTextureID());
			}
		}
	}

	/**
	 * Constructor that creates the TextureEntry class from a byte array
	 *
	 * @param data
	 *            Byte array containing the TextureEntry field
	 * @param pos
	 *            Starting position of the TextureEntry field in the byte array
	 * @param length
	 *            Length of the TextureEntry field, in bytes
	 * @throws Exception
	 */
	public TextureEntry(byte[] data, int pos, int length) {
		fromBytes(data, pos, length);
	}

	public TextureEntry(byte[] data) {
		fromBytes(data, 0, data.length);
	}

	public TextureEntry(OSD osd) {
		fromOSD(osd);
	}

	/**
	 * This will either create a new face if a custom face for the given index is
	 * not defined, or return the custom face for that index if it already exists
	 *
	 * @param index
	 *            The index number of the face to create or retrieve
	 * @return A TextureEntryFace containing all the properties for that
	 * @throws Exception
	 */
	public TextureEntryFace createFace(int index) {
		if (index >= numTextures)
			return null;

		if (faceTextures[index] == null)
			faceTextures[index] = new TextureEntryFace(this.defaultTexture);

		return faceTextures[index];
	}

	public TextureEntryFace getFace(int index) throws Exception {
		if (index >= numTextures)
			throw new Exception(index + " is outside the range of MAX_FACES");

		if (faceTextures[index] != null)
			return faceTextures[index];
		return defaultTexture;
	}

	public OSD serialize() {
		OSDArray array = new OSDArray();

		// If DefaultTexture is null, assume the whole TextureEntry is empty
		if (defaultTexture == null)
			return array;

		// Otherwise, always add default texture
		array.add(defaultTexture.serialize(-1));

		for (int i = 0; i < MAX_FACES; i++) {
			if (faceTextures[i] != null)
				array.add(faceTextures[i].serialize(i));
		}
		return array;
	}

	public void fromOSD(OSD osd) {
		if (osd.getType() == OSDType.Array) {
			OSDArray array = (OSDArray) osd;

			if (array.size() > 0) {
				RefObject<Integer> faceNumber = new RefObject<Integer>(0);
				OSDMap faceSD = (OSDMap) array.get(0);
				defaultTexture = new TextureEntryFace(faceSD, null, faceNumber);

				for (int i = 1; i < array.size(); i++) {
					TextureEntryFace tex = new TextureEntryFace(array.get(i), defaultTexture, faceNumber);
					if (faceNumber.argvalue >= 0 && faceNumber.argvalue < faceTextures.length)
						faceTextures[faceNumber.argvalue] = tex;
				}
			}
		}
	}

	private void fromBytes(byte[] data, int pos, int length) {
		Values off = new Values();

		if (length < 16 + pos) {
			// No TextureEntry to process
			defaultTexture = null;
			return;
		}
		defaultTexture = new TextureEntryFace(null);

		off.i = pos;

		// #region Texture
		defaultTexture.setTextureID(new UUID(data, off.i));
		off.i += 16;

		while (readFaceBitfield(data, off)) {
			UUID tmpUUID = new UUID(data, off.i);
			off.i += 16;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if ((off.faceBits & bit) != 0)
					createFace(face).setTextureID(tmpUUID);
		}
		// #endregion Texture

		// #region Color
		defaultTexture.setRGBA(new Color4(data, off.i, true));
		off.i += 4;

		while (readFaceBitfield(data, off)) {
			Color4 tmpColor = new Color4(data, off.i, true);
			off.i += 4;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if ((off.faceBits & bit) != 0)
					createFace(face).setRGBA(tmpColor);
		}
		// #endregion Color

		// #region RepeatU
		defaultTexture.setRepeatU(Helpers.BytesToFloatL(data, off.i));
		off.i += 4;

		while (readFaceBitfield(data, off)) {
			float tmpFloat = Helpers.BytesToFloatL(data, off.i);
			off.i += 4;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if ((off.faceBits & bit) != 0)
					createFace(face).setRepeatU(tmpFloat);
		}
		// #endregion RepeatU

		// #region RepeatV
		defaultTexture.setRepeatV(Helpers.BytesToFloatL(data, off.i));
		off.i += 4;

		while (readFaceBitfield(data, off)) {
			float tmpFloat = Helpers.BytesToFloatL(data, off.i);
			off.i += 4;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if ((off.faceBits & bit) != 0)
					createFace(face).setRepeatV(tmpFloat);
		}
		// #endregion RepeatV

		// #region OffsetU
		defaultTexture.setOffsetU(Helpers.TEOffsetFloat(data, off.i));
		off.i += 2;

		while (readFaceBitfield(data, off)) {
			float tmpFloat = Helpers.TEOffsetFloat(data, off.i);
			off.i += 2;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if ((off.faceBits & bit) != 0)
					createFace(face).setOffsetU(tmpFloat);
		}
		// #endregion OffsetU

		// #region OffsetV
		defaultTexture.setOffsetV(Helpers.TEOffsetFloat(data, off.i));
		off.i += 2;

		while (readFaceBitfield(data, off)) {
			float tmpFloat = Helpers.TEOffsetFloat(data, off.i);
			off.i += 2;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if ((off.faceBits & bit) != 0)
					createFace(face).setOffsetV(tmpFloat);
		}
		// #endregion OffsetV

		// #region Rotation
		defaultTexture.setRotation(Helpers.TERotationFloat(data, off.i));
		off.i += 2;

		while (readFaceBitfield(data, off)) {
			float tmpFloat = Helpers.TERotationFloat(data, off.i);
			off.i += 2;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if ((off.faceBits & bit) != 0)
					createFace(face).setRotation(tmpFloat);
		}
		// #endregion Rotation

		// #region Material
		defaultTexture.material = data[off.i];
		off.i++;

		while (readFaceBitfield(data, off)) {
			byte tmpByte = data[off.i];
			off.i++;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if ((off.faceBits & bit) != 0)
					createFace(face).material = tmpByte;
		}
		// #endregion Material

		// #region Media
		defaultTexture.media = data[off.i];
		off.i++;

		while (off.i - pos + 1 < length && readFaceBitfield(data, off)) {
			byte tmpByte = data[off.i];
			off.i++;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if ((off.faceBits & bit) != 0)
					createFace(face).media = tmpByte;
		}
		// #endregion Media

		// #region Glow
		defaultTexture.setGlow(Helpers.TEGlowFloat(data, off.i));
		off.i++;

		while (off.i - pos + 4 < length && readFaceBitfield(data, off)) {
			float tmpFloat = Helpers.TEGlowFloat(data, off.i);
			off.i++;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if ((off.faceBits & bit) != 0)
					createFace(face).setGlow(tmpFloat);
		}
		// #endregion Glow

		// #region MaterialID
		if (off.i - pos + 16 <= length) {
			defaultTexture.setMaterialID(new UUID(data, off.i));
			off.i += 16;

			while (off.i - pos + 16 < length && readFaceBitfield(data, off)) {
				UUID tmpUUID = new UUID(data, off.i);
				off.i += 16;

				for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
					if ((off.faceBits & bit) != 0)
						createFace(face).setMaterialID(tmpUUID);
			}
		}
		// #endregion MaterialID
	}

	public byte[] getBytes() throws IOException {
		if (defaultTexture == null)
			return Helpers.EmptyBytes;

		ByteArrayOutputStream memStream = new ByteArrayOutputStream();
		boolean alreadySent;
		int i, j;
		long bitfield;

		// #region Texture
		UUID tempID, defID = defaultTexture.getTextureID();
		defID.write(memStream);
		for (i = 0; i < numTextures; i++) {
			if (faceTextures[i] != null) {
				tempID = faceTextures[i].getTextureID();
				if (tempID != null && !tempID.equals(defID)) {
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i) {
						alreadySent = faceTextures[j] != null && tempID.equals(faceTextures[j].getTextureID());
						j++;
					}
					if (!alreadySent) {
						bitfield = 1 << j++;
						while (j < numTextures) {
							if (faceTextures[j] != null && tempID.equals(faceTextures[j].getTextureID()))
								bitfield = 1 << j;
							j++;
						}
						writeFaceBitfieldBytes(memStream, bitfield);
						tempID.write(memStream);
					}
				}
			}
		}
		memStream.write(0);
		// #endregion Texture

		// #region Color
		// Serialize the color bytes inverted to optimize for zerocoding
		Color4 tempCol, defCol = defaultTexture.getRGBA();
		defCol.write(memStream, true);
		for (i = 0; i < numTextures; i++) {
			if (faceTextures[i] != null) {
				tempCol = faceTextures[i].getRGBA();
				if (tempCol != null && !tempCol.equals(defCol)) {
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i) {
						alreadySent = faceTextures[j] != null && tempCol.equals(faceTextures[j].getRGBA());
						j++;
					}
					if (!alreadySent) {
						bitfield = 1 << j++;
						while (j < numTextures) {
							if (faceTextures[j] != null && tempCol.equals(faceTextures[j].getRGBA()))
								bitfield = 1 << j;
							j++;
						}
						writeFaceBitfieldBytes(memStream, bitfield);
						// Serialize the color bytes inverted to optimize for zerocoding
						tempCol.write(memStream, true);
					}
				}
			}
		}
		memStream.write(0);
		// #endregion Color

		// #region RepeatU
		float tempFloat, defFloat = defaultTexture.getRepeatU();
		memStream.write(Helpers.FloatToBytesL(defFloat));
		for (i = 0; i < numTextures; i++) {
			if (faceTextures[i] != null) {
				tempFloat = faceTextures[i].getRepeatU();
				if (tempFloat != defFloat) {
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i) {
						alreadySent = faceTextures[j] != null && tempFloat == faceTextures[j].getRepeatU();
						j++;
					}
					if (!alreadySent) {
						bitfield = 1 << j++;
						while (j < numTextures) {
							if (faceTextures[j] != null && tempFloat == faceTextures[j].getRepeatU())
								bitfield = 1 << j;
							j++;
						}
						writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(Helpers.FloatToBytesL(tempFloat));
					}
				}
			}
		}
		memStream.write(0);
		// #endregion RepeatU

		// #region RepeatV
		defFloat = defaultTexture.getRepeatV();
		memStream.write(Helpers.FloatToBytesL(defFloat));
		for (i = 0; i < numTextures; i++) {
			if (faceTextures[i] != null) {
				tempFloat = faceTextures[i].getRepeatV();
				if (tempFloat != defFloat) {
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i) {
						alreadySent = faceTextures[j] != null && tempFloat == faceTextures[j].getRepeatV();
						j++;
					}
					if (!alreadySent) {
						bitfield = 1 << j++;
						while (j < numTextures) {
							if (faceTextures[j] != null && tempFloat == faceTextures[j].getRepeatV())
								bitfield = 1 << j;
							j++;
						}
						writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(Helpers.FloatToBytesL(tempFloat));
					}
				}
			}
		}
		memStream.write((byte) 0);
		// #endregion RepeatV

		// #region OffsetU
		defFloat = defaultTexture.getOffsetU();
		memStream.write(Helpers.TEOffsetShort(defFloat));
		for (i = 0; i < numTextures; i++) {
			if (faceTextures[i] != null) {
				tempFloat = faceTextures[i].getOffsetU();
				if (tempFloat != defFloat) {
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i) {
						alreadySent = faceTextures[j] != null && tempFloat == faceTextures[j].getOffsetU();
						j++;
					}
					if (!alreadySent) {
						bitfield = 1 << j++;
						while (j < numTextures) {
							if (faceTextures[j] != null && tempFloat == faceTextures[j].getOffsetU())
								bitfield = 1 << j;
							j++;
						}
						writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(Helpers.TEOffsetShort(tempFloat));
					}
				}
			}
		}
		memStream.write(0);
		// #endregion OffsetU

		// #region OffsetV
		defFloat = defaultTexture.getOffsetV();
		memStream.write(Helpers.TEOffsetShort(defFloat));
		for (i = 0; i < numTextures; i++) {
			if (faceTextures[i] != null) {
				tempFloat = faceTextures[i].getOffsetV();
				if (tempFloat != defFloat) {
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i) {
						alreadySent = faceTextures[j] != null && tempFloat == faceTextures[j].getOffsetV();
						j++;
					}
					if (!alreadySent) {
						bitfield = 1 << j++;
						while (j < numTextures) {
							if (faceTextures[j] != null && tempFloat == faceTextures[j].getOffsetV())
								bitfield = 1 << j;
							j++;
						}
						writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(Helpers.TEOffsetShort(tempFloat));
					}
				}
			}
		}
		memStream.write(0);
		// #endregion OffsetV

		// #region Rotation
		defFloat = defaultTexture.getRotation();
		memStream.write(Helpers.TERotationShort(defFloat));
		for (i = 0; i < numTextures; i++) {
			if (faceTextures[i] != null) {
				tempFloat = faceTextures[i].getRotation();
				if (tempFloat != defFloat) {
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i) {
						alreadySent = faceTextures[j] != null && tempFloat == faceTextures[j].getRotation();
						j++;
					}
					if (!alreadySent) {
						bitfield = 1 << j++;
						while (j < numTextures) {
							if (faceTextures[j] != null && tempFloat == faceTextures[j].getRotation())
								bitfield = 1 << j;
							j++;
						}
						writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(Helpers.TERotationShort(tempFloat));
					}
				}
			}
		}
		memStream.write(0);
		// #endregion Rotation

		// #region Material
		memStream.write(defaultTexture.material);
		for (i = 0; i < numTextures; i++) {
			if (faceTextures[i] != null && faceTextures[i].material != defaultTexture.material) {
				alreadySent = false;
				j = 0;
				while (!alreadySent && j < i) {
					alreadySent = faceTextures[j] != null && faceTextures[i].material == faceTextures[j].material;
					j++;
				}
				if (!alreadySent) {
					bitfield = 1 << j++;
					while (j < numTextures) {
						if (faceTextures[j] != null && faceTextures[i].material == faceTextures[j].material)
							bitfield = 1 << j;
						j++;
					}
					writeFaceBitfieldBytes(memStream, bitfield);
					memStream.write(faceTextures[i].material);
				}
			}
		}
		memStream.write(0);
		// #endregion Material

		// #region Media
		memStream.write(defaultTexture.media);
		for (i = 0; i < numTextures; i++) {
			if (faceTextures[i] != null && faceTextures[i].media != defaultTexture.media) {
				alreadySent = false;
				j = 0;
				while (!alreadySent && j < i) {
					alreadySent = faceTextures[j] != null && faceTextures[i].media == faceTextures[j].media;
					j++;
				}
				if (!alreadySent) {
					bitfield = 1 << j++;
					while (j < numTextures) {
						if (faceTextures[j] != null && faceTextures[i].media == faceTextures[j].media)
							bitfield = 1 << j;
						j++;
					}
					writeFaceBitfieldBytes(memStream, bitfield);
					memStream.write(faceTextures[i].media);
				}
			}
		}
		memStream.write(0);
		// #endregion Media

		// #region Glow
		byte tempByte, defByte = Helpers.TEGlowByte(defaultTexture.getGlow());
		memStream.write(defByte);
		for (i = 0; i < numTextures; i++) {
			if (faceTextures[i] != null) {
				tempByte = Helpers.TEGlowByte(faceTextures[i].getGlow());
				if (tempByte != defByte) {
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i) {
						alreadySent = faceTextures[j] != null
								&& tempByte == Helpers.TEGlowByte(faceTextures[j].getGlow());
						j++;
					}
					if (!alreadySent) {
						bitfield = 1 << j++;
						while (j < numTextures) {
							if (faceTextures[j] != null && tempByte == Helpers.TEGlowByte(faceTextures[j].getGlow()))
								bitfield = 1 << j;
							j++;
						}
						writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(tempByte);
					}
				}
			}
		}
		// #endregion Glow

		// #region MaterialID
		defID = defaultTexture.getMaterialID();
		if (defID != null) {
			memStream.write(0);

			defID.write(memStream);
			for (i = 0; i < numTextures; i++) {
				if (faceTextures[i] != null) {
					tempID = faceTextures[i].getMaterialID();
					if (tempID != null && !tempID.equals(defID)) {
						alreadySent = false;
						j = 0;
						while (!alreadySent && j < i) {
							alreadySent = faceTextures[j] != null && tempID.equals(faceTextures[j].getMaterialID());
							j++;
						}
						if (!alreadySent) {
							bitfield = 1 << j++;
							while (j < numTextures) {
								if (faceTextures[j] != null && tempID.equals(faceTextures[j].getMaterialID()))
									bitfield = 1 << j;
								j++;
							}
							writeFaceBitfieldBytes(memStream, bitfield);
							tempID.write(memStream);
						}
					}
				}
			}
		}
		// #endregion MaterialID
		return memStream.toByteArray();
	}

	@Override
	public int hashCode() {
		int hashCode = defaultTexture != null ? defaultTexture.hashCode() : 0;
		for (int i = 0; i < numTextures; i++) {
			if (faceTextures[i] != null)
				hashCode ^= faceTextures[i].hashCode();
		}
		return hashCode;
	}

	@Override
	public String toString() {
		String output = Helpers.EmptyString;

		output += "Default Face: " + defaultTexture.toString() + Helpers.NewLine;

		for (int i = 0; i < faceTextures.length; i++) {
			if (faceTextures[i] != null)
				output += "Face " + i + ": " + faceTextures[i].toString() + Helpers.NewLine;
		}

		return output;
	}

	// #region Helpers
	class Values {
		int bitfieldSize;
		int i;
		long faceBits;
	}

	private boolean readFaceBitfield(byte[] data, Values pos) {
		pos.faceBits = 0;
		pos.bitfieldSize = 0;

		if (pos.i >= data.length)
			return false;

		byte b = 0;
		do {
			b = data[pos.i++];
			pos.faceBits = (pos.faceBits << 7) | (b & 0x7FL);
			pos.bitfieldSize += 7;
		} while ((b & 0x80) != 0);

		return (pos.faceBits != 0);
	}

	private int writeFaceBitfieldBytes(ByteArrayOutputStream memStream, long bitfield) {
		int byteLength = 0;
		long tmpBitfield = bitfield;
		while (tmpBitfield != 0) {
			tmpBitfield >>= 7;
			byteLength++;
		}

		if (byteLength > 0) {
			int value;
			for (int i = 1; i <= byteLength; i++) {
				value = (int) ((bitfield >> (7 * (byteLength - i))) & 0x7F);
				if (i < byteLength)
					value |= 0x80;
				memStream.write(value);
			}
			return byteLength;
		}
		memStream.write(0);
		return 1;
	}
}
