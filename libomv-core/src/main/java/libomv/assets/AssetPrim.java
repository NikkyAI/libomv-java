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
package libomv.assets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.primitives.ObjectProperties;
import libomv.primitives.ParticleSystem;
import libomv.primitives.ParticleSystem.SourcePattern;
import libomv.primitives.Primitive;
import libomv.primitives.Primitive.HoleType;
import libomv.primitives.Primitive.PathCurve;
import libomv.primitives.Primitive.PrimFlags;
import libomv.primitives.Primitive.ProfileCurve;
import libomv.primitives.Primitive.SculptType;
import libomv.primitives.TextureEntry;
import libomv.types.Color4;
import libomv.types.Permissions;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.Helpers;

// A linkset asset, containing a parent primitive and zero or more children
public class AssetPrim extends AssetItem {
	private static final Logger logger = Logger.getLogger(AssetPrim.class);

	private PrimObject parent;

	public PrimObject getParent() {
		return parent;
	}

	public void setParent(PrimObject parent) {
		invalidateAssetData();
		this.parent = parent;
	}

	private List<PrimObject> children;

	public List<PrimObject> getChildren() {
		return children;
	}

	public void setChildren(List<PrimObject> children) {
		invalidateAssetData();
		this.children = children;
	}

	// Override the base classes AssetType
	@Override
	public AssetType getAssetType() {
		return AssetType.Object;
	}

	/// Initializes a new instance of an AssetPrim object
	/// <param name="assetID">A unique <see cref="UUID"/> specific to this
	/// asset</param>
	/// <param name="assetData">A byte array containing the raw asset data</param>
	public AssetPrim(UUID assetID, byte[] assetData) {
		super(assetID, assetData);
	}

	public AssetPrim(String xmlData) throws XmlPullParserException, IOException {
		super(null, null);
		decodeXml(xmlData);
	}

	public AssetPrim(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
		super(null, null);
		decodeXml(xmlParser);
	}

	public AssetPrim(PrimObject parent, ArrayList<PrimObject> children) {
		super(null, null);
		this.parent = parent;
		if (children != null)
			this.children = children;
		else
			this.children = new ArrayList<PrimObject>(0);
	}

	@Override
	protected void encode() {
		StringWriter textWriter = new StringWriter();
		try {
			XmlSerializer xmlWriter = XmlPullParserFactory.newInstance().newSerializer();
			xmlWriter.setOutput(textWriter);

			encodeXml(xmlWriter);
			xmlWriter.flush();
			assetData = textWriter.toString().getBytes(Helpers.UTF8_ENCODING);
		} catch (Exception ex) {
			logger.error("XML encoding error", ex);
		} finally {
			try {
				textWriter.close();
			} catch (IOException ex) {
				logger.error("XML encoding error", ex);
			}
		}
	}

	@Override
	protected boolean decode() {
		if (assetData != null) {
			InputStream stream = new ByteArrayInputStream(assetData);
			try {
				XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
				parser.setInput(stream, Helpers.UTF8_ENCODING);
				parser.nextTag();
				return decodeXml(parser);
			} catch (Exception ex) {
				logger.error("XML parse error", ex);
			} finally {
				try {
					stream.close();
				} catch (IOException ex) {
					logger.error("XML parse error", ex);
				}
			}
		}
		return false;
	}

	private boolean decodeXml(String xmlData) throws XmlPullParserException, IOException {
		StringReader reader = new StringReader(xmlData);
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(reader);
			parser.nextTag();
			return decodeXml(parser);
		} finally {
			reader.close();
		}
	}

	public void writeXml(Writer writer, int indentation)
			throws XmlPullParserException, IllegalArgumentException, IllegalStateException, IOException {
		XmlSerializer xmlWriter = XmlPullParserFactory.newInstance().newSerializer();

		if (indentation > 0) {
			String indent = new String(new char[indentation]).replace('\0', ' ');
			xmlWriter.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", indent);
		}
		xmlWriter.setOutput(writer);
		xmlWriter.startDocument(Helpers.UTF8_ENCODING, null);
		encodeXml(xmlWriter);
		xmlWriter.flush();
	}

	private void encodeXml(XmlSerializer writer) throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, "SceneObjectGroup");
		writePrim(writer, parent, null);

		writer.startTag(null, "OtherParts");
		for (PrimObject child : children)
			writePrim(writer, child, parent);
		writer.endTag(null, "OtherParts");
		writer.endTag(null, "SceneObjectGroup");
		writer.endDocument();
	}

	private void writePrim(XmlSerializer writer, PrimObject prim, PrimObject parent)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, "SceneObjectPart");
		writer.attribute(null, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		writer.attribute(null, "xmlns:xsd", "http://www.w3.org/2001/XMLSchema");

		prim.creatorID.serializeXml(writer, null, "CreatorID");
		prim.folderID.serializeXml(writer, null, "FolderID");
		writeText(writer, "InventorySerial", (prim.inventory != null) ? Integer.toString(prim.inventory.serial) : "0");

		// FIXME: Task inventory
		writer.startTag(null, "TaskInventory");
		if (prim.inventory != null) {
			for (PrimObject.InventoryBlock.ItemBlock item : prim.inventory.items) {
				writer.startTag(null, "TaskInventoryItem");

				item.assetID.serializeXml(writer, null, "AssetID");
				writeInt(writer, "BasePermissions", item.permsBase);
				writeLong(writer, "CreationDate", (long) Helpers.DateTimeToUnixTime(item.creationDate));
				item.creatorID.serializeXml(writer, null, "CreatorID");
				writeText(writer, "Description", item.description);
				writeInt(writer, "EveryonePermissions", item.permsEveryone);
				writeInt(writer, "Flags", item.flags);
				item.groupID.serializeXml(writer, null, "GroupID");
				writeInt(writer, "GroupPermissions", item.permsGroup);
				writeInt(writer, "InvType", item.inventoryType.getValue());
				item.id.serializeXml(writer, null, "ItemID");
				UUID.Zero.serializeXml(writer, null, "OldItemID");
				item.lastOwnerID.serializeXml(writer, null, "LastOwnerID");
				writeText(writer, "Name", item.name);
				writeInt(writer, "NextPermissions", item.permsNextOwner);
				item.ownerID.serializeXml(writer, null, "OwnerID");
				writeInt(writer, "CurrentPermissions", item.permsOwner);
				prim.id.serializeXml(writer, null, "ParentID");
				prim.id.serializeXml(writer, null, "ParentPartID");
				item.permsGranterID.serializeXml(writer, null, "PermsGranter");
				writeInt(writer, "PermsMask", 0);
				writeInt(writer, "Type", item.assetType.getValue());
				writeText(writer, "OwnerChanged", "false");

				writer.endTag(null, "TaskInventoryItem");
			}
		}
		writer.endTag(null, "TaskInventory");

		int flags = PrimFlags.None;
		if (prim.usePhysics)
			flags |= PrimFlags.Physics;
		if (prim.phantom)
			flags |= PrimFlags.Phantom;
		if (prim.dieAtEdge)
			flags |= PrimFlags.DieAtEdge;
		if (prim.returnAtEdge)
			flags |= PrimFlags.ReturnAtEdge;
		if (prim.temporary)
			flags |= PrimFlags.Temporary;
		if (prim.sandbox)
			flags |= PrimFlags.Sandbox;
		writeInt(writer, "ObjectFlags", flags);

		prim.id.serializeXml(writer, null, "UUID");
		writeInt(writer, "LocalId", prim.localID);
		writeText(writer, "Name", prim.name);
		writeInt(writer, "Material", prim.material);
		writeLong(writer, "RegionHandle", prim.regionHandle);
		writeInt(writer, "ScriptAccessPin", prim.remoteScriptAccessPIN);

		Vector3 groupPosition;
		if (parent == null)
			groupPosition = prim.position;
		else
			groupPosition = parent.position;

		groupPosition.serializeXml(writer, null, "GroupPosition", Helpers.EnUsCulture);
		if (prim.parentID == 0)
			Vector3.Zero.serializeXml(writer, null, "OffsetPosition");
		else
			prim.position.serializeXml(writer, null, "OffsetPosition");
		prim.rotation.serializeXml(writer, null, "RotationOffset");
		prim.velocity.serializeXml(writer, null, "Velocity");
		Vector3.Zero.serializeXml(writer, null, "RotationalVelocity");
		prim.angularVelocity.serializeXml(writer, null, "AngularVelocity");
		prim.acceleration.serializeXml(writer, null, "Acceleration");
		writeText(writer, "Description", prim.description);

		prim.textColor.serializeXml(writer, null, "Color");

		writeText(writer, "Text", prim.text);
		writeText(writer, "SitName", prim.sitName);
		writeText(writer, "TouchName", prim.touchName);

		writeInt(writer, "LinkNum", prim.linkNumber);
		writeInt(writer, "ClickAction", prim.clickAction);

		writer.startTag(null, "Shape");

		writeInt(writer, "PathBegin", Primitive.packBeginCut(prim.shape.pathBegin));
		writeInt(writer, "PathCurve", prim.shape.pathCurve);
		writeInt(writer, "PathEnd", Primitive.packEndCut(prim.shape.pathEnd));
		writeInt(writer, "PathRadiusOffset", Primitive.packPathTwist(prim.shape.pathRadiusOffset));
		writeInt(writer, "PathRevolutions", Primitive.packPathRevolutions(prim.shape.pathRevolutions));
		writeInt(writer, "PathScaleX", Primitive.packPathScale(prim.shape.pathScaleX));
		writeInt(writer, "PathScaleY", Primitive.packPathScale(prim.shape.pathScaleY));
		writeInt(writer, "PathShearX", Primitive.packPathShear(prim.shape.pathShearX));
		writeInt(writer, "PathShearY", Primitive.packPathShear(prim.shape.pathShearY));
		writeInt(writer, "PathSkew", Primitive.packPathTwist(prim.shape.pathSkew));
		writeInt(writer, "PathTaperX", Primitive.packPathTaper(prim.shape.pathTaperX));
		writeInt(writer, "PathTaperY", Primitive.packPathTaper(prim.shape.pathTaperY));
		writeInt(writer, "PathTwist", Primitive.packPathTwist(prim.shape.pathTwist));
		writeInt(writer, "PathTwistBegin", Primitive.packPathTwist(prim.shape.pathTwistBegin));
		writeInt(writer, "PCode", prim.primCode);
		writeInt(writer, "ProfileBegin", Primitive.packBeginCut(prim.shape.profileBegin));
		writeInt(writer, "ProfileEnd", Primitive.packEndCut(prim.shape.profileEnd));
		writeInt(writer, "ProfileHollow", Primitive.packProfileHollow(prim.shape.profileHollow));
		prim.scale.serializeXml(writer, null, "Scale");
		writeInt(writer, "State", prim.state);
		writeText(writer, "ProfileShape", ProfileCurve.setValue(prim.shape.profileCurve & 0x0F).toString());
		writeText(writer, "HollowShape", HoleType.setValue((prim.shape.profileCurve & 0xF0) >> 4).toString());
		writeInt(writer, "ProfileCurve", prim.shape.profileCurve);

		writeText(writer, "TextureEntry",
				Base64.encodeBase64String(prim.textures != null ? prim.textures.getBytes() : Helpers.EmptyBytes));

		// FIXME: ExtraParams
		writeText(writer, "ExtraParams", Helpers.EmptyString);

		// FIXME: write sculpt, flexy and light data

		writer.endTag(null, "Shape");

		prim.scale.serializeXml(writer, null, "Scale"); // FIXME: again?
		writeInt(writer, "UpdateFlag", 0);
		Quaternion.Identity.serializeXml(writer, null, "SitTargetOrientation");
		prim.sitOffset.serializeXml(writer, null, "SitTargetPosition");
		prim.sitOffset.serializeXml(writer, null, "SitTargetPositionLL");
		prim.sitRotation.serializeXml(writer, null, "SitTargetOrientationLL");
		writeInt(writer, "ParentID", prim.parentID);
		writeLong(writer, "CreationDate", (long) Helpers.DateTimeToUnixTime(prim.creationDate));
		writeInt(writer, "Category", 0);
		writeInt(writer, "SalePrice", prim.salePrice);
		writeInt(writer, "ObjectSaleType", prim.saleType);
		writeInt(writer, "OwnershipCost", 0);
		prim.groupID.serializeXml(writer, null, "GroupID");
		prim.ownerID.serializeXml(writer, null, "OwnerID");
		prim.lastOwnerID.serializeXml(writer, null, "LastOwnerID");
		writeInt(writer, "BaseMask", prim.permsBase);
		writeInt(writer, "OwnerMask", prim.permsOwner);
		writeInt(writer, "GroupMask", prim.permsGroup);
		writeInt(writer, "EveryoneMask", prim.permsEveryone);
		writeInt(writer, "NextOwnerMask", prim.permsNextOwner);
		writeText(writer, "Flags", "None");
		prim.collisionSound.serializeXml(writer, null, "CollisionSound");
		writeFloat(writer, "CollisionSoundVolume", prim.collisionSoundVolume);
		Vector3.Zero.serializeXml(writer, null, "SitTargetAvatar");
		writer.endTag(null, "SceneObjectPart");
	}

	private static void writeText(XmlSerializer writer, String name, String text)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, name).text(text).endTag(null, name);
	}

	private static void writeInt(XmlSerializer writer, String name, int number)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, name).text(Integer.toString(number)).endTag(null, name);
	}

	private static void writeLong(XmlSerializer writer, String name, long number)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, name).text(Long.toString(number)).endTag(null, name);
	}

	private static void writeFloat(XmlSerializer writer, String name, float number)
			throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag(null, name).text(Float.toString(number)).endTag(null, name);
	}

	private boolean decodeXml(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, "SceneObjectGroup");
		parser.nextTag(); // Advance to <RootPart> (or sometimes just <SceneObjectPart>
		parent = loadPrim(parser);
		if (parent != null) {
			parser.nextTag(); // Advance to <OtherParths>
			if (parser.getEventType() == XmlPullParser.END_TAG)
				logger.error("Unexpected event type");

			if (!parser.isEmptyElementTag()) {
				parser.require(XmlPullParser.START_TAG, null, "OtherParts");

				ArrayList<PrimObject> children = new ArrayList<PrimObject>();
				while (parser.nextTag() == XmlPullParser.START_TAG) {
					PrimObject child = loadPrim(parser);
					if (child != null)
						children.add(child);
				}
				this.children = children;
			}
			return true;
		}
		logger.error("Failed to load root linkset prim");
		return false;
	}

	private PrimObject loadPrim(XmlPullParser parser) throws XmlPullParserException, IOException {
		PrimObject obj = new PrimObject();
		Vector3 groupPosition = null, offsetPosition = null;
		boolean gotExtraPartTag = false;

		obj.inventory = obj.new InventoryBlock();

		// Enter with eventType == XmlPullParser.START_TAG
		if (parser.getEventType() == XmlPullParser.START_TAG
				&& (parser.getName().equals("RootPart") || parser.getName().equals("Part"))) {
			gotExtraPartTag = true;
			parser.nextTag(); // Advance to SceneObjectPart tag
		}
		if (!parser.getName().equals("SceneObjectPart"))
			System.out.println("Something went wrong");
		parser.require(XmlPullParser.START_TAG, null, "SceneObjectPart");

		obj.allowedDrop = true;
		// obj.PassTouches = false;

		while (parser.nextTag() == XmlPullParser.START_TAG) {
			String name = parser.getName();
			if (name.equals("AllowedDrop")) {
				obj.allowedDrop = Helpers.TryParseBoolean(parser.nextText().trim());
			} else if (name.equals("CreatorID")) {
				obj.creatorID = new UUID(parser);
			} else if (name.equals("FolderID")) {
				obj.folderID = new UUID(parser);
			} else if (name.equals("InventorySerial")) {
				obj.inventory.serial = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("TaskInventory")) {
				// FIXME: Parse TaskInventory obj.Inventory.Items = new
				// PrimObject.InventoryBlock.ItemBlock[0];
				Helpers.skipElement(parser);
			} else if (name.equals("ObjectFlags")) {
				int flags = Helpers.TryParseInt(parser.nextText().trim());
				obj.usePhysics = (flags & PrimFlags.Physics) != 0;
				obj.phantom = (flags & PrimFlags.Phantom) != 0;
				obj.dieAtEdge = (flags & PrimFlags.DieAtEdge) != 0;
				obj.returnAtEdge = (flags & PrimFlags.ReturnAtEdge) != 0;
				obj.temporary = (flags & PrimFlags.Temporary) != 0;
				obj.sandbox = (flags & PrimFlags.Sandbox) != 0;
			} else if (name.equals("UUID")) {
				obj.id = new UUID(parser);
			} else if (name.equals("LocalId")) {
				obj.localID = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("Name")) {
				obj.name = parser.nextText().trim();
			} else if (name.equals("Material")) {
				obj.material = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("PassTouches")) {
				obj.passTouches = Helpers.TryParseBoolean(parser.nextText().trim());
			} else if (name.equals("RegionHandle")) {
				obj.regionHandle = Helpers.TryParseLong(parser.nextText().trim());
			} else if (name.equals("ScriptAccessPin")) {
				obj.remoteScriptAccessPIN = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("GroupPosition")) {
				groupPosition = new Vector3(parser);
			} else if (name.equals("OffsetPosition")) {
				offsetPosition = new Vector3(parser);
			} else if (name.equals("RotationOffset")) {
				obj.rotation = new Quaternion(parser);
			} else if (name.equals("Velocity")) {
				obj.velocity = new Vector3(parser);
			} else if (name.equals("RotationalVelocity")) {
				new Vector3(parser);
			} else if (name.equals("AngularVelocity")) {
				obj.angularVelocity = new Vector3(parser);
			} else if (name.equals("Acceleration")) {
				obj.acceleration = new Vector3(parser);
			} else if (name.equals("Description")) {
				obj.description = parser.nextText().trim();
			} else if (name.equals("Color")) {
				obj.textColor = new Color4(parser);
			} else if (name.equals("Text")) {
				obj.text = parser.nextText().trim();
			} else if (name.equals("SitName")) {
				obj.sitName = parser.nextText().trim();
			} else if (name.equals("TouchName")) {
				obj.touchName = parser.nextText().trim();
			} else if (name.equals("LinkNum")) {
				obj.linkNumber = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("ClickAction")) {
				obj.clickAction = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("Shape")) {
				obj.shape = loadShape(parser, obj);
			} else if (name.equals("Scale")) {
				obj.scale = new Vector3(parser); // Yes, again
			} else if (name.equals("UpdateFlag")) {
				parser.nextText(); // Skip
			} else if (name.equals("SitTargetOrientation")) {
				Quaternion.parse(parser); // Skip
			} else if (name.equals("SitTargetPosition")) {
				Vector3.parse(parser); // Skip
			} else if (name.equals("SitTargetPositionLL")) {
				obj.sitOffset = new Vector3(parser);
			} else if (name.equals("SitTargetOrientationLL")) {
				obj.sitRotation = new Quaternion(parser);
			} else if (name.equals("ParentID")) {
				obj.parentID = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("CreationDate")) {
				obj.creationDate = Helpers.UnixTimeToDateTime(Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("Category")) {
				Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("SalePrice")) {
				obj.salePrice = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("ObjectSaleType")) {
				obj.saleType = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("OwnershipCost")) {
				Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("GroupID")) {
				obj.groupID = new UUID(parser);
			} else if (name.equals("OwnerID")) {
				obj.ownerID = new UUID(parser);
			} else if (name.equals("LastOwnerID")) {
				obj.lastOwnerID = new UUID(parser);
			} else if (name.equals("BaseMask")) {
				obj.permsBase = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("OwnerMask")) {
				obj.permsOwner = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("GroupMask")) {
				obj.permsGroup = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("EveryoneMask")) {
				obj.permsEveryone = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("NextOwnerMask")) {
				obj.permsNextOwner = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("Flags")) {
				parser.nextText();
			} else if (name.equals("CollisionSound")) {
				obj.collisionSound = new UUID(parser);
			} else if (name.equals("CollisionSoundVolume")) {
				obj.collisionSoundVolume = Helpers.TryParseFloat(parser.nextText().trim());
			} else {
				if (parser.isEmptyElementTag())
					Helpers.skipElement(parser);
				else
					logger.debug("Received unrecocognized asset primitive element " + name + " \""
							+ Helpers.skipElementDebug(parser) + "\"");
			}
		}
		// currently at </SceneObjectPart>
		if (gotExtraPartTag)
			parser.nextTag(); // Advance to </RootPart> or </Part>

		if (obj.parentID == 0)
			obj.position = groupPosition;
		else
			obj.position = offsetPosition;

		return obj;
	}

	private static PrimObject.ShapeBlock loadShape(XmlPullParser parser, PrimObject obj)
			throws XmlPullParserException, IOException {
		obj.shape = obj.new ShapeBlock();
		PrimObject.LightBlock light = obj.new LightBlock();
		light.color = new Color4(0f, 0f, 0f, 1f);
		PrimObject.FlexibleBlock flexible = obj.new FlexibleBlock();
		flexible.force = new Vector3(0.0f);

		parser.require(XmlPullParser.START_TAG, null, "Shape");

		while (parser.nextTag() == XmlPullParser.START_TAG) {
			String name = parser.getName();
			if (name.equals("ProfileCurve")) {
				obj.shape.profileCurve = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("TextureEntry")) {
				byte[] teData = Base64.decodeBase64(parser.nextText());
				obj.textures = new TextureEntry(teData, 0, teData.length);
			} else if (name.equals("ExtraParams")) {
				parser.nextText(); // Skip Extra Params
			} else if (name.equals("PathBegin")) {
				obj.shape.pathBegin = Primitive.unpackBeginCut((short) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathCurve")) {
				obj.shape.pathCurve = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("PathEnd")) {
				obj.shape.pathEnd = Primitive.unpackEndCut((short) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathRadiusOffset")) {
				obj.shape.pathRadiusOffset = Primitive
						.unpackPathTwist((byte) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathRevolutions")) {
				obj.shape.pathRevolutions = Primitive
						.unpackPathRevolutions((byte) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathScaleX")) {
				obj.shape.pathScaleX = Primitive.unpackPathScale((byte) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathScaleY")) {
				obj.shape.pathScaleY = Primitive.unpackPathScale((byte) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathShearX")) {
				obj.shape.pathShearX = Primitive.unpackPathShear((byte) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathShearY")) {
				obj.shape.pathShearY = Primitive.unpackPathShear((byte) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathSkew")) {
				obj.shape.pathSkew = Primitive.unpackPathTwist((byte) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathTaperX")) {
				obj.shape.pathTaperX = Primitive.unpackPathTaper((byte) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathTaperY")) {
				obj.shape.pathTaperY = Primitive.unpackPathShear((byte) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathTwist")) {
				obj.shape.pathTwist = Primitive.unpackPathTwist((byte) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PathTwistBegin")) {
				obj.shape.pathTwistBegin = Primitive
						.unpackPathTwist((byte) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("PCode")) {
				obj.primCode = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("ProfileBegin")) {
				obj.shape.profileBegin = Primitive
						.unpackBeginCut((short) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("ProfileEnd")) {
				obj.shape.profileEnd = Primitive.unpackEndCut((short) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("ProfileHollow")) {
				obj.shape.profileHollow = Primitive
						.unpackProfileHollow((short) Helpers.TryParseInt(parser.nextText().trim()));
			} else if (name.equals("Scale")) {
				obj.scale = new Vector3(parser);
			} else if (name.equals("State")) {
				obj.state = (byte) Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("ProfileShape")) {
				obj.shape.profileCurve |= ProfileCurve.setValue(Helpers.TryParseInt(parser.nextText())).getValue();
			} else if (name.equals("HollowShape")) {
				obj.shape.profileCurve |= HoleType.setValue(Helpers.TryParseInt(parser.nextText())).getValue() << 4;
			} else if (name.equals("SculptTexture")) {
				UUID sculptTexture = new UUID(parser);
				if (!sculptTexture.equals(UUID.Zero)) {
					if (obj.sculpt == null)
						obj.sculpt = obj.new SculptBlock();
					obj.sculpt.texture = sculptTexture;
				}
			} else if (name.equals("SculptType")) {
				if (obj.sculpt == null)
					obj.sculpt = obj.new SculptBlock();
				obj.sculpt.type = SculptType.setValue(Helpers.TryParseInt(parser.nextText())).getValue();
			} else if (name.equals("SculptData")) {
				parser.nextText();
			} else if (name.equals("FlexiSoftness")) {
				flexible.softness = Helpers.TryParseInt(parser.nextText().trim());
			} else if (name.equals("FlexiTension")) {
				flexible.tension = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("FlexiDrag")) {
				flexible.drag = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("FlexiGravity")) {
				flexible.gravity = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("FlexiWind")) {
				flexible.wind = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("FlexiForceX")) {
				flexible.force.X = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("FlexiForceY")) {
				flexible.force.Y = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("FlexiForceZ")) {
				flexible.force.Z = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("LightColorR")) {
				light.color.R = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("LightColorG")) {
				light.color.G = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("LightColorB")) {
				light.color.B = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("LightColorA")) {
				light.color.A = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("LightRadius")) {
				light.radius = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("LightCutoff")) {
				light.cutoff = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("LightFalloff")) {
				light.falloff = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("LightIntensity")) {
				light.intensity = Helpers.TryParseFloat(parser.nextText().trim());
			} else if (name.equals("FlexiEntry")) {
				if (Helpers.TryParseBoolean(parser.nextText().trim()))
					obj.flexible = flexible;
			} else if (name.equals("LightEntry")) {
				if (Helpers.TryParseBoolean(parser.nextText().trim()))
					obj.light = light;
			} else if (name.equals("SculptEntry")) {
				parser.nextText(); // Skip
			} else {
				Helpers.skipElement(parser);
			}
		}
		return obj.shape;
	}

	/** The deserialized form of a single primitive in a linkset asset */
	public class PrimObject {
		public class FlexibleBlock {
			public int softness;
			public float gravity;
			public float drag;
			public float wind;
			public float tension;
			public Vector3 force;

			public OSDMap serialize() {
				OSDMap map = new OSDMap();
				map.put("softness", OSD.FromInteger(softness));
				map.put("gravity", OSD.FromReal(gravity));
				map.put("drag", OSD.FromReal(drag));
				map.put("wind", OSD.FromReal(wind));
				map.put("tension", OSD.FromReal(tension));
				map.put("force", OSD.FromVector3(force));
				return map;
			}

			public void deserialize(OSDMap map) {
				softness = map.get("softness").AsInteger();
				gravity = (float) map.get("gravity").AsReal();
				drag = (float) map.get("drag").AsReal();
				wind = (float) map.get("wind").AsReal();
				tension = (float) map.get("tension").AsReal();
				force = map.get("force").AsVector3();
			}
		}

		public class LightBlock {
			public Color4 color;
			public float intensity;
			public float radius;
			public float falloff;
			public float cutoff;

			public OSDMap serialize() {
				OSDMap map = new OSDMap();
				map.put("color", OSD.FromColor4(color));
				map.put("intensity", OSD.FromReal(intensity));
				map.put("radius", OSD.FromReal(radius));
				map.put("falloff", OSD.FromReal(falloff));
				map.put("cutoff", OSD.FromReal(cutoff));
				return map;
			}

			public void deserialize(OSDMap map) {
				color = map.get("color").AsColor4();
				intensity = (float) map.get("intensity").AsReal();
				radius = (float) map.get("radius").AsReal();
				falloff = (float) map.get("falloff").AsReal();
				cutoff = (float) map.get("cutoff").AsReal();
			}
		}

		public class SculptBlock {
			public UUID texture;
			public byte type;

			public OSDMap serialize() {
				OSDMap map = new OSDMap();
				map.put("texture", OSD.FromUUID(texture));
				map.put("type", OSD.FromInteger(type));
				return map;
			}

			public void deserialize(OSDMap map) {
				texture = map.get("texture").AsUUID();
				type = (byte) map.get("type").AsInteger();
			}
		}

		public class ParticlesBlock {
			public int flags;
			public int pattern;
			public float maxAge;
			public float startAge;
			public float innerAngle;
			public float outerAngle;
			public float burstRate;
			public float burstRadius;
			public float burstSpeedMin;
			public float burstSpeedMax;
			public int burstParticleCount;
			public Vector3 angularVelocity;
			public Vector3 acceleration;
			public UUID textureID;
			public UUID targetID;
			public int dataFlags;
			public float particleMaxAge;
			public Color4 particleStartColor;
			public Color4 particleEndColor;
			public Vector2 particleStartScale;
			public Vector2 particleEndScale;

			public OSDMap serialize() {
				OSDMap map = new OSDMap();
				map.put("flags", OSD.FromInteger(flags));
				map.put("pattern", OSD.FromInteger(pattern));
				map.put("max_age", OSD.FromReal(maxAge));
				map.put("start_age", OSD.FromReal(startAge));
				map.put("inner_angle", OSD.FromReal(innerAngle));
				map.put("outer_angle", OSD.FromReal(outerAngle));
				map.put("burst_rate", OSD.FromReal(burstRate));
				map.put("burst_radius", OSD.FromReal(burstRadius));
				map.put("burst_speed_min", OSD.FromReal(burstSpeedMin));
				map.put("burst_speed_max", OSD.FromReal(burstSpeedMax));
				map.put("burst_particle_count", OSD.FromInteger(burstParticleCount));
				map.put("angular_velocity", OSD.FromVector3(angularVelocity));
				map.put("acceleration", OSD.FromVector3(acceleration));
				map.put("texture_id", OSD.FromUUID(textureID));
				map.put("target_id", OSD.FromUUID(targetID));
				map.put("data_flags", OSD.FromInteger(dataFlags));
				map.put("particle_max_age", OSD.FromReal(particleMaxAge));
				map.put("particle_start_color", OSD.FromColor4(particleStartColor));
				map.put("particle_end_color", OSD.FromColor4(particleEndColor));
				map.put("particle_start_scale", OSD.FromVector2(particleStartScale));
				map.put("particle_end_scale", OSD.FromVector2(particleEndScale));
				return map;
			}

			public void deserialize(OSDMap map) {
				flags = map.get("flags").AsInteger();
				pattern = map.get("pattern").AsInteger();
				maxAge = (float) map.get("max_age").AsReal();
				startAge = (float) map.get("start_age").AsReal();
				innerAngle = (float) map.get("inner_angle").AsReal();
				outerAngle = (float) map.get("outer_angle").AsReal();
				burstRate = (float) map.get("burst_rate").AsReal();
				burstRadius = (float) map.get("burst_radius").AsReal();
				burstSpeedMin = (float) map.get("burst_speed_min").AsReal();
				burstSpeedMax = (float) map.get("burst_speed_max").AsReal();
				burstParticleCount = map.get("burst_particle_count").AsInteger();
				angularVelocity = map.get("angular_velocity").AsVector3();
				acceleration = map.get("acceleration").AsVector3();
				textureID = map.get("texture_id").AsUUID();
				dataFlags = map.get("data_flags").AsInteger();
				particleMaxAge = (float) map.get("particle_max_age").AsReal();
				particleStartColor = map.get("particle_start_color").AsColor4();
				particleEndColor = map.get("particle_end_color").AsColor4();
				particleStartScale = map.get("particle_start_scale").AsVector2();
				particleEndScale = map.get("particle_end_scale").AsVector2();
			}
		}

		public class ShapeBlock {
			public int pathCurve;
			public float pathBegin;
			public float pathEnd;
			public float pathScaleX;
			public float pathScaleY;
			public float pathShearX;
			public float pathShearY;
			public float pathTwist;
			public float pathTwistBegin;
			public float pathRadiusOffset;
			public float pathTaperX;
			public float pathTaperY;
			public float pathRevolutions;
			public float pathSkew;
			public int profileCurve;
			public float profileBegin;
			public float profileEnd;
			public float profileHollow;

			public OSDMap serialize() {
				OSDMap map = new OSDMap();
				map.put("path_curve", OSD.FromInteger(pathCurve));
				map.put("path_begin", OSD.FromReal(pathBegin));
				map.put("path_end", OSD.FromReal(pathEnd));
				map.put("path_scale_x", OSD.FromReal(pathScaleX));
				map.put("path_scale_y", OSD.FromReal(pathScaleY));
				map.put("path_shear_x", OSD.FromReal(pathShearX));
				map.put("path_shear_y", OSD.FromReal(pathShearY));
				map.put("path_twist", OSD.FromReal(pathTwist));
				map.put("path_twist_begin", OSD.FromReal(pathTwistBegin));
				map.put("path_radius_offset", OSD.FromReal(pathRadiusOffset));
				map.put("path_taper_x", OSD.FromReal(pathTaperX));
				map.put("path_taper_y", OSD.FromReal(pathTaperY));
				map.put("path_revolutions", OSD.FromReal(pathRevolutions));
				map.put("path_skew", OSD.FromReal(pathSkew));
				map.put("profile_curve", OSD.FromInteger(profileCurve));
				map.put("profile_begin", OSD.FromReal(profileBegin));
				map.put("profile_end", OSD.FromReal(profileEnd));
				map.put("profile_hollow", OSD.FromReal(profileHollow));
				return map;
			}

			public void deserialize(OSDMap map) {
				pathCurve = map.get("path_curve").AsInteger();
				pathBegin = (float) map.get("path_begin").AsReal();
				pathEnd = (float) map.get("path_end").AsReal();
				pathScaleX = (float) map.get("path_scale_x").AsReal();
				pathScaleY = (float) map.get("path_scale_y").AsReal();
				pathShearX = (float) map.get("path_shear_x").AsReal();
				pathShearY = (float) map.get("path_shear_y").AsReal();
				pathTwist = (float) map.get("path_twist").AsReal();
				pathTwistBegin = (float) map.get("path_twist_begin").AsReal();
				pathRadiusOffset = (float) map.get("path_radius_offset").AsReal();
				pathTaperX = (float) map.get("path_taper_x").AsReal();
				pathTaperY = (float) map.get("path_taper_y").AsReal();
				pathRevolutions = (float) map.get("path_revolutions").AsReal();
				pathSkew = (float) map.get("path_skew").AsReal();
				profileCurve = map.get("profile_curve").AsInteger();
				profileBegin = (float) map.get("profile_begin").AsReal();
				profileEnd = (float) map.get("profile_end").AsReal();
				profileHollow = (float) map.get("profile_hollow").AsReal();
			}
		}

		public class InventoryBlock {
			public class ItemBlock {
				public UUID id;
				public String name;
				public UUID ownerID;
				public UUID creatorID;
				public UUID groupID;
				public UUID lastOwnerID;
				public UUID permsGranterID;
				public UUID assetID;
				public AssetType assetType;
				public InventoryType inventoryType;
				public String description;
				public int permsBase;
				public int permsOwner;
				public int permsGroup;
				public int permsEveryone;
				public int permsNextOwner;
				public int salePrice;
				public int saleType;
				public int flags;
				public Date creationDate;

				public OSDMap serialize() {
					OSDMap map = new OSDMap();
					map.put("id", OSD.FromUUID(id));
					map.put("name", OSD.FromString(name));
					map.put("owner_id", OSD.FromUUID(ownerID));
					map.put("creator_id", OSD.FromUUID(creatorID));
					map.put("group_id", OSD.FromUUID(groupID));
					map.put("last_owner_id", OSD.FromUUID(lastOwnerID));
					map.put("perms_granter_id", OSD.FromUUID(permsGranterID));
					map.put("asset_id", OSD.FromUUID(assetID));
					map.put("asset_type", OSD.FromInteger(assetType.getValue()));
					map.put("inv_type", OSD.FromInteger(inventoryType.getValue()));
					map.put("description", OSD.FromString(description));
					map.put("perms_base", OSD.FromInteger(permsBase));
					map.put("perms_owner", OSD.FromInteger(permsOwner));
					map.put("perms_group", OSD.FromInteger(permsGroup));
					map.put("perms_everyone", OSD.FromInteger(permsEveryone));
					map.put("perms_next_owner", OSD.FromInteger(permsNextOwner));
					map.put("sale_price", OSD.FromInteger(salePrice));
					map.put("sale_type", OSD.FromInteger(saleType));
					map.put("flags", OSD.FromInteger(flags));
					map.put("creation_date", OSD.FromDate(creationDate));
					return map;
				}

				public void deserialize(OSDMap map) {
					id = map.get("id").AsUUID();
					name = map.get("name").AsString();
					ownerID = map.get("owner_id").AsUUID();
					creatorID = map.get("creator_id").AsUUID();
					groupID = map.get("group_id").AsUUID();
					assetID = map.get("asset_id").AsUUID();
					lastOwnerID = map.get("last_owner_id").AsUUID();
					permsGranterID = map.get("perms_granter_id").AsUUID();
					assetType = AssetType.setValue(map.get("asset_type").AsInteger());
					inventoryType = InventoryType.setValue(map.get("inv_type").AsInteger());
					description = map.get("description").AsString();
					permsBase = map.get("perms_base").AsInteger();
					permsOwner = map.get("perms_owner").AsInteger();
					permsGroup = map.get("perms_group").AsInteger();
					permsEveryone = map.get("perms_everyone").AsInteger();
					permsNextOwner = map.get("perms_next_owner").AsInteger();
					salePrice = map.get("sale_price").AsInteger();
					saleType = map.get("sale_type").AsInteger();
					flags = map.get("flags").AsInteger();
					creationDate = map.get("creation_date").AsDate();
				}

				public ItemBlock() {
				}

				public ItemBlock(InventoryItem item) {
					assetID = item.assetID;
					creationDate = item.creationDate;
					creatorID = item.permissions.creatorID;
					description = item.description;
					flags = item.itemFlags;
					groupID = item.permissions.groupID;
					id = item.itemID;
					inventoryType = item.getType() == InventoryType.Unknown && item.assetType == AssetType.LSLText
							? InventoryType.LSL
							: item.getType();
					;
					lastOwnerID = item.permissions.lastOwnerID;
					name = item.name;
					ownerID = item.getOwnerID();
					permsBase = item.permissions.BaseMask;
					permsEveryone = item.permissions.EveryoneMask;
					permsGroup = item.permissions.GroupMask;
					permsNextOwner = item.permissions.NextOwnerMask;
					permsOwner = item.permissions.OwnerMask;
					permsGranterID = UUID.Zero;
					assetType = item.assetType;
				}
			}

			public int serial;
			public ItemBlock[] items;

			public OSDMap serialize() {
				OSDMap map = new OSDMap();
				map.put("serial", OSD.FromInteger(serial));

				if (items != null) {
					OSDArray array = new OSDArray(items.length);
					for (int i = 0; i < items.length; i++)
						array.add(items[i].serialize());
					map.put("items", array);
				}

				return map;
			}

			public void deserialize(OSDMap map) {
				serial = map.get("serial").AsInteger();

				if (map.containsKey("items")) {
					OSDArray array = (OSDArray) map.get("items");
					items = new ItemBlock[array.size()];

					for (int i = 0; i < array.size(); i++) {
						ItemBlock item = new ItemBlock();
						item.deserialize((OSDMap) array.get(i));
						items[i] = item;
					}
				} else {
					items = new ItemBlock[0];
				}
			}
		}

		public UUID id;
		public boolean allowedDrop;
		public Vector3 attachmentPosition;
		public Quaternion attachmentRotation;
		public Quaternion beforeAttachmentRotation;
		public String name;
		public String description;
		public int permsBase;
		public int permsOwner;
		public int permsGroup;
		public int permsEveryone;
		public int permsNextOwner;
		public UUID creatorID;
		public UUID ownerID;
		public UUID lastOwnerID;
		public UUID groupID;
		public UUID folderID;
		public long regionHandle;
		public int clickAction;
		public int lastAttachmentPoint;
		public int linkNumber;
		public int localID;
		public int parentID;
		public Vector3 position;
		public Quaternion rotation;
		public Vector3 velocity;
		public Vector3 angularVelocity;
		public Vector3 acceleration;
		public Vector3 scale;
		public Vector3 sitOffset;
		public Quaternion sitRotation;
		public Vector3 cameraEyeOffset;
		public Vector3 cameraAtOffset;
		public int state;
		public int primCode;
		public int material;
		public boolean passTouches;
		public UUID soundID;
		public float soundGain;
		public float soundRadius;
		public byte soundFlags;
		public Color4 textColor;
		public String text;
		public String sitName;
		public String touchName;
		public boolean selected;
		public UUID selectorID;
		public boolean usePhysics;
		public boolean phantom;
		public int remoteScriptAccessPIN;
		public boolean volumeDetect;
		public boolean dieAtEdge;
		public boolean returnAtEdge;
		public boolean temporary;
		public boolean sandbox;
		public Date creationDate;
		public Date rezDate;
		public int salePrice;
		public int saleType;
		public byte[] scriptState;
		public UUID collisionSound;
		public float collisionSoundVolume;
		public FlexibleBlock flexible;
		public LightBlock light;
		public SculptBlock sculpt;
		public ParticlesBlock particles;
		public ShapeBlock shape;
		public TextureEntry textures;
		public InventoryBlock inventory;

		public OSDMap Serialize() {
			OSDMap map = new OSDMap();
			map.put("id", OSD.FromUUID(id));
			map.put("attachment_position", OSD.FromVector3(attachmentPosition));
			map.put("attachment_rotation", OSD.FromQuaternion(attachmentRotation));
			map.put("before_attachment_rotation", OSD.FromQuaternion(beforeAttachmentRotation));
			map.put("name", OSD.FromString(name));
			map.put("description", OSD.FromString(description));
			map.put("perms_base", OSD.FromInteger(permsBase));
			map.put("perms_owner", OSD.FromInteger(permsOwner));
			map.put("perms_group", OSD.FromInteger(permsGroup));
			map.put("perms_everyone", OSD.FromInteger(permsEveryone));
			map.put("perms_next_owner", OSD.FromInteger(permsNextOwner));
			map.put("creator_identity", OSD.FromUUID(creatorID));
			map.put("owner_identity", OSD.FromUUID(ownerID));
			map.put("last_owner_identity", OSD.FromUUID(lastOwnerID));
			map.put("group_identity", OSD.FromUUID(groupID));
			map.put("folder_id", OSD.FromUUID(folderID));
			map.put("region_handle", OSD.FromULong(regionHandle));
			map.put("click_action", OSD.FromInteger(clickAction));
			map.put("last_attachment_point", OSD.FromInteger(lastAttachmentPoint));
			map.put("link_number", OSD.FromInteger(linkNumber));
			map.put("local_id", OSD.FromInteger(localID));
			map.put("parent_id", OSD.FromInteger(parentID));
			map.put("position", OSD.FromVector3(position));
			map.put("rotation", OSD.FromQuaternion(rotation));
			map.put("velocity", OSD.FromVector3(velocity));
			map.put("angular_velocity", OSD.FromVector3(angularVelocity));
			map.put("acceleration", OSD.FromVector3(acceleration));
			map.put("scale", OSD.FromVector3(scale));
			map.put("sit_offset", OSD.FromVector3(sitOffset));
			map.put("sit_rotation", OSD.FromQuaternion(sitRotation));
			map.put("camera_eye_offset", OSD.FromVector3(cameraEyeOffset));
			map.put("camera_at_offset", OSD.FromVector3(cameraAtOffset));
			map.put("state", OSD.FromInteger(state));
			map.put("prim_code", OSD.FromInteger(primCode));
			map.put("material", OSD.FromInteger(material));
			map.put("pass_touches", OSD.FromBoolean(passTouches));
			map.put("sound_id", OSD.FromUUID(soundID));
			map.put("sound_gain", OSD.FromReal(soundGain));
			map.put("sound_radius", OSD.FromReal(soundRadius));
			map.put("sound_flags", OSD.FromInteger(soundFlags));
			map.put("text_color", OSD.FromColor4(textColor));
			map.put("text", OSD.FromString(text));
			map.put("sit_name", OSD.FromString(sitName));
			map.put("touch_name", OSD.FromString(touchName));
			map.put("selected", OSD.FromBoolean(selected));
			map.put("selector_id", OSD.FromUUID(selectorID));
			map.put("use_physics", OSD.FromBoolean(usePhysics));
			map.put("phantom", OSD.FromBoolean(phantom));
			map.put("remote_script_access_pin", OSD.FromInteger(remoteScriptAccessPIN));
			map.put("volume_detect", OSD.FromBoolean(volumeDetect));
			map.put("die_at_edge", OSD.FromBoolean(dieAtEdge));
			map.put("return_at_edge", OSD.FromBoolean(returnAtEdge));
			map.put("temporary", OSD.FromBoolean(temporary));
			map.put("sandbox", OSD.FromBoolean(sandbox));
			map.put("creation_date", OSD.FromDate(creationDate));
			map.put("rez_date", OSD.FromDate(rezDate));
			map.put("sale_price", OSD.FromInteger(salePrice));
			map.put("sale_type", OSD.FromInteger(saleType));

			if (flexible != null)
				map.put("flexible", flexible.serialize());
			if (light != null)
				map.put("light", light.serialize());
			if (sculpt != null)
				map.put("sculpt", sculpt.serialize());
			if (particles != null)
				map.put("particles", particles.serialize());
			if (shape != null)
				map.put("shape", shape.serialize());
			if (textures != null)
				map.put("textures", textures.serialize());
			if (inventory != null)
				map.put("inventory", inventory.serialize());

			return map;
		}

		public void deserialize(OSDMap map) {
			id = map.get("id").AsUUID();
			attachmentPosition = map.get("attachment_position").AsVector3();
			attachmentRotation = map.get("attachment_rotation").AsQuaternion();
			beforeAttachmentRotation = map.get("before_attachment_rotation").AsQuaternion();
			name = map.get("name").AsString();
			description = map.get("description").AsString();
			permsBase = map.get("perms_base").AsInteger();
			permsOwner = map.get("perms_owner").AsInteger();
			permsGroup = map.get("perms_group").AsInteger();
			permsEveryone = map.get("perms_everyone").AsInteger();
			permsNextOwner = map.get("perms_next_owner").AsInteger();
			creatorID = map.get("creator_identity").AsUUID();
			ownerID = map.get("owner_identity").AsUUID();
			lastOwnerID = map.get("last_owner_identity").AsUUID();
			groupID = map.get("group_identity").AsUUID();
			folderID = map.get("folder_id").AsUUID();
			regionHandle = map.get("region_handle").AsULong();
			clickAction = map.get("click_action").AsInteger();
			lastAttachmentPoint = map.get("last_attachment_point").AsInteger();
			linkNumber = map.get("link_number").AsInteger();
			localID = map.get("local_id").AsInteger();
			parentID = map.get("parent_id").AsInteger();
			position = map.get("position").AsVector3();
			rotation = map.get("rotation").AsQuaternion();
			velocity = map.get("velocity").AsVector3();
			angularVelocity = map.get("angular_velocity").AsVector3();
			acceleration = map.get("acceleration").AsVector3();
			scale = map.get("scale").AsVector3();
			sitOffset = map.get("sit_offset").AsVector3();
			sitRotation = map.get("sit_rotation").AsQuaternion();
			cameraEyeOffset = map.get("camera_eye_offset").AsVector3();
			cameraAtOffset = map.get("camera_at_offset").AsVector3();
			state = map.get("state").AsInteger();
			primCode = map.get("prim_code").AsInteger();
			material = map.get("material").AsInteger();
			passTouches = map.get("pass_touches").AsBoolean();
			soundID = map.get("sound_id").AsUUID();
			soundGain = (float) map.get("sound_gain").AsReal();
			soundRadius = (float) map.get("sound_radius").AsReal();
			soundFlags = (byte) map.get("sound_flags").AsInteger();
			textColor = map.get("text_color").AsColor4();
			text = map.get("text").AsString();
			sitName = map.get("sit_name").AsString();
			touchName = map.get("touch_name").AsString();
			selected = map.get("selected").AsBoolean();
			selectorID = map.get("selector_id").AsUUID();
			usePhysics = map.get("use_physics").AsBoolean();
			phantom = map.get("phantom").AsBoolean();
			remoteScriptAccessPIN = map.get("remote_script_access_pin").AsInteger();
			volumeDetect = map.get("volume_detect").AsBoolean();
			dieAtEdge = map.get("die_at_edge").AsBoolean();
			returnAtEdge = map.get("return_at_edge").AsBoolean();
			temporary = map.get("temporary").AsBoolean();
			sandbox = map.get("sandbox").AsBoolean();
			creationDate = map.get("creation_date").AsDate();
			rezDate = map.get("rez_date").AsDate();
			salePrice = map.get("sale_price").AsInteger();
			saleType = map.get("sale_type").AsInteger();
		}

		public PrimObject() {

		}

		public PrimObject(Primitive obj) {
			acceleration = obj.acceleration;
			allowedDrop = (obj.flags & PrimFlags.AllowInventoryDrop) == PrimFlags.AllowInventoryDrop;
			angularVelocity = obj.angularVelocity;
			// AttachmentPosition
			// AttachmentRotation
			// BeforeAttachmentRotation
			// CameraAtOffset
			// CameraEyeOffset
			clickAction = obj.clickAction.getValue();
			// CollisionSound
			// CollisionSoundVolume;
			creationDate = obj.properties.creationDate;
			creatorID = obj.properties.permissions.creatorID;
			description = obj.properties.description;
			dieAtEdge = (obj.flags & PrimFlags.DieAtEdge) == PrimFlags.DieAtEdge;
			if (obj.flexible != null) {
				flexible = new FlexibleBlock();
				flexible.drag = obj.flexible.drag;
				flexible.force = obj.flexible.force;
				flexible.gravity = obj.flexible.gravity;
				flexible.softness = obj.flexible.softness;
				flexible.tension = obj.flexible.tension;
				flexible.wind = obj.flexible.wind;
			}
			folderID = obj.properties.folderID;
			groupID = obj.properties.permissions.groupID;
			id = obj.properties.objectID;
			// Inventory;
			// LastAttachmentPoint;
			lastOwnerID = obj.properties.permissions.lastOwnerID;
			if (obj.light != null) {
				light = new LightBlock();
				light.color = obj.light.color;
				light.cutoff = obj.light.cutoff;
				light.falloff = obj.light.falloff;
				light.intensity = obj.light.intensity;
				light.radius = obj.light.radius;
			}

			// LinkNumber;
			localID = obj.localID;
			material = obj.primData.material.getValue();
			name = obj.properties.name;
			ownerID = obj.properties.permissions.ownerID;
			parentID = obj.parentID;

			particles = new ParticlesBlock();
			particles.angularVelocity = obj.particleSys.angularVelocity;
			particles.acceleration = obj.particleSys.partAcceleration;
			particles.burstParticleCount = obj.particleSys.burstPartCount;
			particles.burstRate = obj.particleSys.burstRadius;
			particles.burstRate = obj.particleSys.burstRate;
			particles.burstSpeedMax = obj.particleSys.burstSpeedMax;
			particles.burstSpeedMin = obj.particleSys.burstSpeedMin;
			particles.dataFlags = obj.particleSys.partDataFlags;
			particles.flags = obj.particleSys.partFlags;
			particles.innerAngle = obj.particleSys.innerAngle;
			particles.maxAge = obj.particleSys.maxAge;
			particles.outerAngle = obj.particleSys.outerAngle;
			particles.particleEndColor = obj.particleSys.partEndColor;
			particles.particleEndScale = new Vector2(obj.particleSys.partEndScaleX, obj.particleSys.partEndScaleY);
			particles.particleMaxAge = obj.particleSys.maxAge;
			particles.particleStartColor = obj.particleSys.partStartColor;
			particles.particleStartScale = new Vector2(obj.particleSys.partStartScaleX,
					obj.particleSys.partStartScaleY);
			particles.pattern = obj.particleSys.pattern;
			particles.startAge = obj.particleSys.startAge;
			particles.targetID = obj.particleSys.target;
			particles.textureID = obj.particleSys.texture;

			// PassTouches;
			primCode = obj.primData.primCode.getValue();
			permsBase = obj.properties.permissions.BaseMask;
			permsEveryone = obj.properties.permissions.EveryoneMask;
			permsGroup = obj.properties.permissions.GroupMask;
			permsNextOwner = obj.properties.permissions.NextOwnerMask;
			permsOwner = obj.properties.permissions.OwnerMask;
			phantom = (obj.flags & PrimFlags.Phantom) == PrimFlags.Phantom;
			position = obj.position;
			regionHandle = obj.regionHandle;
			// RemoteScriptAccessPIN;
			returnAtEdge = (obj.flags & PrimFlags.ReturnAtEdge) == PrimFlags.ReturnAtEdge;
			// RezDate;
			rotation = obj.rotation;
			salePrice = obj.properties.salePrice;
			saleType = obj.properties.saleType.getValue();
			sandbox = (obj.flags & PrimFlags.Sandbox) == PrimFlags.Sandbox;
			scale = obj.scale;
			// ScriptState;
			if (obj.sculpt != null) {
				sculpt = new SculptBlock();
				sculpt.texture = obj.sculpt.sculptTexture;
				sculpt.type = obj.sculpt.getType().getValue();
			}
			shape = new ShapeBlock();
			shape.pathBegin = obj.primData.pathBegin;
			shape.pathCurve = obj.primData.pathCurve.getValue();
			shape.pathEnd = obj.primData.pathEnd;
			shape.pathRadiusOffset = obj.primData.pathRadiusOffset;
			shape.pathRevolutions = obj.primData.pathRevolutions;
			shape.pathScaleX = obj.primData.pathScaleX;
			shape.pathScaleY = obj.primData.pathScaleY;
			shape.pathShearX = obj.primData.pathShearX;
			shape.pathShearY = obj.primData.pathShearY;
			shape.pathSkew = obj.primData.pathSkew;
			shape.pathTaperX = obj.primData.pathTaperX;
			shape.pathTaperY = obj.primData.pathTaperY;

			shape.pathTwist = obj.primData.pathTwist;
			shape.pathTwistBegin = obj.primData.pathTwistBegin;
			shape.profileBegin = obj.primData.profileBegin;
			shape.profileCurve = obj.primData.profileCurve.getValue();
			shape.profileEnd = obj.primData.profileEnd;
			shape.profileHollow = obj.primData.profileHollow;

			sitName = obj.properties.sitName;
			// SitOffset;
			// SitRotation;
			soundFlags = obj.soundFlags;
			soundGain = obj.soundGain;
			soundID = obj.soundID;
			soundRadius = obj.soundRadius;
			state = obj.primData.state;
			temporary = (obj.flags & PrimFlags.Temporary) == PrimFlags.Temporary;
			text = obj.text;
			textColor = obj.textColor;
			textures = obj.textures;
			// TouchName;
			usePhysics = (obj.flags & PrimFlags.Physics) == PrimFlags.Physics;
			velocity = obj.velocity;
		}

		public Primitive toPrimitive() {
			Primitive prim = new Primitive();
			prim.properties = new ObjectProperties();
			prim.primData = prim.new ConstructionData();

			prim.acceleration = this.acceleration;
			prim.angularVelocity = this.angularVelocity;
			prim.clickAction = Primitive.ClickAction.setValue(this.clickAction);
			prim.properties.creationDate = this.creationDate;
			prim.properties.description = this.description;
			if (this.dieAtEdge)
				prim.flags |= PrimFlags.DieAtEdge;
			prim.properties.folderID = this.folderID;
			prim.id = this.id;
			prim.localID = this.localID;
			prim.primData.material = Primitive.Material.setValue(this.material);
			prim.properties.name = this.name;
			prim.ownerID = this.ownerID;
			prim.parentID = this.parentID;
			prim.primData.primCode = Primitive.PCode.setValue(this.primCode);
			prim.properties.permissions = new Permissions(this.creatorID, this.ownerID, this.lastOwnerID, this.groupID,
					this.permsBase, this.permsEveryone, this.permsGroup, this.permsNextOwner, this.permsOwner);
			if (this.phantom)
				prim.flags |= PrimFlags.Phantom;
			prim.position = this.position;
			if (this.returnAtEdge)
				prim.flags |= PrimFlags.ReturnAtEdge;
			prim.rotation = this.rotation;
			prim.properties.salePrice = this.salePrice;
			prim.properties.saleType = libomv.model.LLObject.SaleType.setValue(this.saleType);
			if (this.sandbox)
				prim.flags |= PrimFlags.Sandbox;
			prim.scale = this.scale;
			prim.soundFlags = this.soundFlags;
			prim.soundGain = this.soundGain;
			prim.soundID = this.soundID;
			prim.soundRadius = this.soundRadius;
			prim.primData.state = (byte) this.state;
			if (this.temporary)
				prim.flags |= PrimFlags.Temporary;
			prim.text = this.text;
			prim.textColor = this.textColor;
			prim.textures = new TextureEntry(this.textures);
			if (this.usePhysics)
				prim.flags |= PrimFlags.Physics;
			prim.velocity = this.velocity;

			prim.primData.pathBegin = this.shape.pathBegin;
			prim.primData.pathCurve = PathCurve.setValue(this.shape.pathCurve);
			prim.primData.pathEnd = this.shape.pathEnd;
			prim.primData.pathRadiusOffset = this.shape.pathRadiusOffset;
			prim.primData.pathRevolutions = this.shape.pathRevolutions;
			prim.primData.pathScaleX = this.shape.pathScaleX;
			prim.primData.pathScaleY = this.shape.pathScaleY;
			prim.primData.pathShearX = this.shape.pathShearX;
			prim.primData.pathShearY = this.shape.pathShearY;
			prim.primData.pathSkew = this.shape.pathSkew;
			prim.primData.pathTaperX = this.shape.pathTaperX;
			prim.primData.pathTaperY = this.shape.pathTaperY;
			prim.primData.pathTwist = this.shape.pathTwist;
			prim.primData.pathTwistBegin = this.shape.pathTwistBegin;
			prim.primData.profileBegin = this.shape.profileBegin;
			prim.primData.profileCurve = ProfileCurve.setValue(this.shape.profileCurve);
			prim.primData.profileEnd = this.shape.profileEnd;
			prim.primData.profileHollow = this.shape.profileHollow;

			if (this.flexible != null) {
				prim.flexible = prim.new FlexibleData();
				prim.flexible.drag = this.flexible.drag;
				prim.flexible.force = this.flexible.force;
				prim.flexible.gravity = this.flexible.gravity;
				prim.flexible.softness = this.flexible.softness;
				prim.flexible.tension = this.flexible.tension;
				prim.flexible.wind = this.flexible.wind;
			}

			if (this.light != null) {
				prim.light = prim.new LightData();
				prim.light.color = this.light.color;
				prim.light.cutoff = this.light.cutoff;
				prim.light.falloff = this.light.falloff;
				prim.light.intensity = this.light.intensity;
				prim.light.radius = this.light.radius;
			}

			if (this.particles != null) {
				prim.particleSys = new ParticleSystem();
				prim.particleSys.angularVelocity = this.particles.angularVelocity;
				prim.particleSys.partAcceleration = this.particles.acceleration;
				prim.particleSys.burstPartCount = (byte) this.particles.burstParticleCount;
				prim.particleSys.burstRate = this.particles.burstRadius;
				prim.particleSys.burstRate = this.particles.burstRate;
				prim.particleSys.burstSpeedMax = this.particles.burstSpeedMax;
				prim.particleSys.burstSpeedMin = this.particles.burstSpeedMin;
				prim.particleSys.partDataFlags = this.particles.dataFlags;
				prim.particleSys.partFlags = this.particles.flags;
				prim.particleSys.innerAngle = this.particles.innerAngle;
				prim.particleSys.maxAge = this.particles.maxAge;
				prim.particleSys.outerAngle = this.particles.outerAngle;
				prim.particleSys.partEndColor = this.particles.particleEndColor;
				prim.particleSys.partEndScaleX = this.particles.particleEndScale.X;
				prim.particleSys.partEndScaleY = this.particles.particleEndScale.Y;
				prim.particleSys.maxAge = this.particles.particleMaxAge;
				prim.particleSys.partStartColor = this.particles.particleStartColor;
				prim.particleSys.partStartScaleX = this.particles.particleStartScale.X;
				prim.particleSys.partStartScaleY = this.particles.particleStartScale.Y;
				prim.particleSys.pattern = SourcePattern.setValue(this.particles.pattern);
				prim.particleSys.startAge = this.particles.startAge;
				prim.particleSys.target = this.particles.targetID;
				prim.particleSys.texture = this.particles.textureID;
			}

			if (this.sculpt != null) {
				prim.sculpt = prim.new SculptData();
				prim.sculpt.sculptTexture = this.sculpt.texture;
				prim.sculpt.setType(this.sculpt.type);
			}

			return prim;
		}
	}
}