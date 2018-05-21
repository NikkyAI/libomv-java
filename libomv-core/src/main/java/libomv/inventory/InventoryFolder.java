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
package libomv.inventory;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.types.UUID;
import libomv.utils.Helpers;

/**
 * A folder contains {@link libomv.inventory.InventoryNode}s and has certain
 * attributes specific to itself
 */
public class InventoryFolder extends InventoryNode {
	/** The different types of folder */
	public enum FolderType {
		/** None folder type */
		None(-1),
		/** Texture folder type */
		Texture(0),
		/** Sound folder type */
		Sound(1),
		/** Calling card folder type */
		CallingCard(2),
		/** Landmark folder type */
		Landmark(3),
		/** Clothing folder type */
		Clothing(5),
		/** Object folder type */
		Object(6),
		/** Notecard folder type */
		Notecard(7),
		/** The root folder type */
		Root(8),
		/**
		 * Non-conformant OpenSim root folder type, obsolete("No longer used, please use
		 * FolderType.Root")
		 */
		OldRoot(9),
		/** LSLText folder */
		LSLText(10),
		/** Bodyparts folder */
		BodyPart(13),
		/** Trash folder */
		Trash(14),
		/** Snapshot folder */
		Snapshot(15),
		/** Lost And Found folder */
		LostAndFound(16),
		/** Animation folder */
		Animation(20),
		/** Gesture folder */
		Gesture(21),
		/** Favorites folder */
		Favorites(23),
		/** Ensemble beginning range */
		EnsembleStart(26),
		/** Ensemble ending range */
		EnsembleEnd(45),
		/** Current outfit folder */
		CurrentOutfit(46),
		/** Outfit folder */
		Outfit(47),
		/** My outfits folder */
		MyOutfits(48),
		/** Mesh folder */
		Mesh(49),
		/** Marketplace direct delivery inbox ("Received Items") */
		Inbox(50),
		/** Marketplace direct delivery outbox */
		Outbox(51),
		/** Basic root folder */
		BasicRoot(52),
		/** Marketplace listings folder */
		MarketplaceListings(53),
		/** Marketplace stock folder */
		MarkplaceStock(54),
		/** Hypergrid Suitcase folder */
		Suitcase(100);

		private static final String[] _FolderTypeNames = new String[] { "texture", // 0
				"sound", // 1
				"callcard", // 2
				"landmark", // 3
				Helpers.EmptyString, // 4
				"clothing", // 5
				"object", // 6
				"notecard", // 7
				"root_inv", // 8
				Helpers.EmptyString, // 9
				"lsltext", // 10
				Helpers.EmptyString, // 11
				Helpers.EmptyString, // 12
				"bodypart", // 13
				"trash", // 14
				"snapshot", // 15
				"lstndfnd", // 16
				Helpers.EmptyString, // 17
				Helpers.EmptyString, // 18
				Helpers.EmptyString, // 19
				"animatn", // 20
				"gesture", // 21
				Helpers.EmptyString, // 22
				"favorite", // 23
				Helpers.EmptyString, // 24
				Helpers.EmptyString, // 25
				"ensemble", // 26
				"ensemble", // 27
				"ensemble", // 28
				"ensemble", // 29
				"ensemble", // 30
				"ensemble", // 31
				"ensemble", // 32
				"ensemble", // 33
				"ensemble", // 34
				"ensemble", // 35
				"ensemble", // 36
				"ensemble", // 37
				"ensemble", // 38
				"ensemble", // 39
				"ensemble", // 40
				"ensemble", // 41
				"ensemble", // 42
				"ensemble", // 43
				"ensemble", // 44
				"ensemble", // 45
				"current", // 46
				"outfit", // 47
				"my_otfts", // 48
				"mesh", // 49
				"inbox", // 50
				"outbox", // 51
				"basic_rt", // 52
				"merchant", // 53
				"stock", // 54
		};

		/**
		 * Translate a string name of an FolderType into the proper Type
		 *
		 * @param type
		 *            A string containing the AssetType name
		 * @return The FolderType which matches the string name, or FolderType.None if
		 *         no match was found
		 */
		public static FolderType setValue(String value) {
			if (value != null) {
				try {
					return setValue(Integer.parseInt(value, 10));
				} catch (NumberFormatException ex) {
				}

				int i = 0;
				for (String name : _FolderTypeNames) {
					i++;
					if (name.compareToIgnoreCase(value) == 0) {
						return setValue(i);
					}
				}
			}
			return None;
		}

		public static FolderType setValue(int value) {
			for (FolderType e : values()) {
				if (e._value == value)
					return e;
			}
			return None;
		}

		public byte getValue() {
			return _value;
		}

		private final byte _value;

		private FolderType(int value) {
			this._value = (byte) value;
		}
	}

	private static final long serialVersionUID = 1L;
	// The Preferred {@link T:libomv.assets.AssetItem.AssetType} for a folder.
	public FolderType preferredType;
	// The Version of this folder
	public int version;
	// The number of descendents in this folder. This value can be different to the
	// actual
	// number of children, if the contents of the folder hasn't been completely
	// fetched yet.
	public int descendentCount;
	// The list of children this folder contains
	public List<InventoryNode> children;

	/**
	 * Constructor
	 *
	 * @param itemID
	 *            UUID of the folder
	 */
	public InventoryFolder(UUID itemID) {
		super(itemID);
		preferredType = FolderType.None;
		version = 1;
	}

	public InventoryFolder(UUID itemID, UUID ownerID) {
		this(itemID);
		this.ownerID = ownerID;
	}

	public InventoryFolder(UUID itemID, UUID parentID, UUID ownerID) {
		this(itemID);
		this.parentID = parentID;
		this.ownerID = ownerID;
	}

	public InventoryFolder(OSDMap map) {
		super();
		fromOSD(map);
	}

	@Override
	public InventoryType getType() {
		return InventoryType.Folder;
	}

	@Override
	public Date getModifyTime() {
		Date newest = new Date(); // .MinValue;
		for (InventoryNode node : children) {
			Date t = node.getModifyTime();
			if (t.after(newest))
				newest = t;
		}
		return newest;
	}

	/**
	 * Returns a copy of the arraylist of children. We return a copy so nobody can
	 * mess with our tree structure.
	 *
	 * @return an arraylist containing the children nodes of this folder or null if
	 *         there is no children list yet
	 */
	public List<InventoryNode> getContents() {
		if (children != null)
			return new ArrayList<>(children);
		return null;
	}

	@Override
	protected OSDMap toOSD() {
		return toOSD(false);
	}

	protected OSDMap toOSD(boolean descendentRoot) {
		OSDMap map = super.toOSD();
		map.put(descendentRoot ? "folder_id" : "category_id", OSD.fromUUID(itemID));
		map.put("type_default", OSD.fromInteger(preferredType.getValue()));
		map.put("version", OSD.fromInteger(version));
		if (descendentRoot) {
			map.put("descendents", OSD.fromInteger(descendentCount));
		}
		return map;
	}

	protected void fromOSD(OSDMap map) {
		super.fromOSD(map);
		if (ownerID == null)
			ownerID = map.get("owner_id").asUUID();
		UUID folderID = map.get("category_id").asUUID();
		if (folderID == null)
			folderID = map.get("folder_id").asUUID();
		version = map.get("version").asInteger();
		preferredType = FolderType.setValue(map.get("type_default").asInteger());
		if (map.containsKey("descendents")) {
			descendentCount = map.get("descendents").asInteger();
		}
	}

	/**
	 * Initializes an InventoryFolder object from a serialization stream
	 *
	 * @param info
	 *            serialization stream
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Override
	protected void readObject(ObjectInputStream info) throws IOException, ClassNotFoundException {
		super.readObject(info);
		if (serialVersionUID != info.readLong())
			throw new InvalidObjectException("InventoryItem serial version mismatch");

		preferredType = FolderType.setValue(info.readByte());
		version = info.readInt();

		int num = info.readInt();
		if (num >= 0) {
			children = new ArrayList<>(num);
			for (int i = 0; i < num; i++)
				children.add((InventoryNode) info.readObject());
		}
	}

	/**
	 * Write Serilization data for this InventoryFolder object to the stream
	 *
	 * @param info
	 *            serialization stream
	 * @throws IOException
	 */
	@Override
	protected void writeObject(ObjectOutputStream info) throws IOException {
		super.writeObject(info);
		info.writeLong(serialVersionUID);
		info.writeByte(preferredType.getValue());
		info.writeInt(version);
		if (children == null) {
			info.writeInt(-1);
		} else {
			info.writeInt(children.size());
			Iterator<InventoryNode> iter = children.iterator();
			while (iter.hasNext())
				info.writeObject(iter.next());
		}
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ preferredType.hashCode() ^ version ^ children.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		InventoryFolder folder = (InventoryFolder) ((o instanceof InventoryFolder) ? o : null);
		return folder != null && equals(folder);
	}

	@Override
	public boolean equals(InventoryNode o) {
		InventoryFolder folder = (InventoryFolder) ((o instanceof InventoryFolder) ? o : null);
		return folder != null && equals(folder);
	}

	public final boolean equals(InventoryFolder o) {
		return super.equals(o) && o.preferredType.equals(preferredType) && o.version == version;
	}
}
