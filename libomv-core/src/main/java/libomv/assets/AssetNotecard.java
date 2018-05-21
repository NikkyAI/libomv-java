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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.model.Inventory;
import libomv.model.asset.AssetType;
import libomv.model.object.SaleType;
import libomv.types.Permissions;
import libomv.types.Permissions.PermissionMask;
import libomv.types.UUID;
import libomv.utils.Helpers;

/**
 * Represents a string of characters encoded with specific formatting properties
 */
public class AssetNotecard extends AssetItem {
	private static final Logger logger = Logger.getLogger(AssetNotecard.class);

	/** A text string containing main text of the notecard */
	private String bodyText = null;
	/**
	 * List of <see cref="OpenMetaverse.InventoryItem"/>s embedded on the notecard
	 */
	private List<InventoryItem> embeddedItems = null;

	/**
	 * Construct an Asset object of type Notecard
	 *
	 * @param assetID
	 *            A unique <see cref="UUID"/> specific to this asset
	 * @param assetData
	 *            A byte array containing the raw asset data
	 */
	public AssetNotecard(UUID assetID, byte[] assetData) {
		super(assetID, assetData);
	}

	/**
	 * Construct an Asset object of type Notecard
	 *
	 * @param text
	 *            A text string containing the main body text of the notecard
	 */
	public AssetNotecard(String text) {
		super(null, null);
		bodyText = text;
		encode();
	}

	/* Override the base classes getAssetType */
	@Override
	public AssetType getAssetType() {
		return AssetType.Notecard;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		invalidateAssetData();
		this.bodyText = bodyText;
	}

	public List<InventoryItem> geEmbeddedItems() {
		return embeddedItems;
	}

	public void setEmbeddedItems(List<InventoryItem> embeddedItems) {
		invalidateAssetData();
		this.embeddedItems = embeddedItems;
	}

	/*
	 * Encode the raw contents of a string with the specific Linden Text properties
	 */
	@Override
	protected void encode() {
		String body = bodyText;

		StringBuilder output = new StringBuilder();
		output.append("Linden text version 2\n");
		output.append("{\n");
		output.append("LLEmbeddedItems version 1\n");
		output.append("{\n");

		int count = 0;

		if (embeddedItems != null) {
			count = embeddedItems.size();
		}

		output.append("count " + count + "\n");

		if (count > 0) {
			output.append("{\n");

			for (int i = 0; i < embeddedItems.size(); i++) {
				InventoryItem item = embeddedItems.get(i);

				output.append("ext char index " + i + "\n");

				output.append("\tinv_item\t0\n");
				output.append("\t{\n");

				output.append("\t\titem_id\t" + item.itemID + "\n");
				output.append("\t\tparent_id\t" + item.getParentID() + "\n");

				output.append("\tpermissions 0\n");
				output.append("\t{\n");
				output.append("\t\tbase_mask\t" + String.format("08x", item.permissions.baseMask) + "\n");
				output.append("\t\towner_mask\t" + String.format("08x", item.permissions.ownerMask) + "\n");
				output.append("\t\tgroup_mask\t" + String.format("08x", item.permissions.groupMask) + "\n");
				output.append("\t\teveryone_mask\t" + String.format("08x", item.permissions.everyoneMask) + "\n");
				output.append("\t\tnext_owner_mask\t" + String.format("08x", item.permissions.nextOwnerMask) + "\n");
				output.append("\t\tcreator_id\t" + item.permissions.creatorID.toString() + "\n");
				output.append("\t\towner_id\t" + item.permissions.ownerID.toString() + "\n");
				output.append("\t\tlast_owner_id\t" + item.permissions.lastOwnerID.toString() + "\n");
				output.append("\t\tgroup_id\t" + item.permissions.groupID.toString() + "\n");
				if (item.permissions.isGroupOwned)
					output.append("\t\tgroup_owned\t1\n");
				output.append("\t}\n");

				if (Permissions.hasPermissions(item.permissions.baseMask,
						PermissionMask.Modify | PermissionMask.Copy | PermissionMask.Transfer)
						|| item.assetID == UUID.ZERO) {
					output.append("\t\tasset_id\t" + item.assetID + "\n");
				} else {
					output.append("\t\tshadow_id\t" + Inventory.encryptAssetID(item.assetID) + "\n");
				}

				output.append("\t\ttype\t" + item.assetType.toString() + "\n");
				output.append("\t\tinv_type\t" + item.getType().toString() + "\n");
				output.append("\t\tflags\t" + String.format("08x", item.itemFlags) + "\n");

				output.append("\tsale_info\t0\n");
				output.append("\t{\n");
				output.append("\t\tsale_type\t" + item.saleType.toString() + "\n");
				output.append("\t\tsale_price\t" + item.salePrice + "\n");
				output.append("\t}\n");

				output.append("\t\tname\t" + item.name.replace('|', '_') + "|\n");
				output.append("\t\tdesc\t" + item.description.replace('|', '_') + "|\n");
				output.append("\t\tcreation_date\t" + Helpers.dateTimeToUnixTime(item.creationDate) + "\n");

				output.append("\t}\n");

				if (i != embeddedItems.size() - 1) {
					output.append("}\n{\n");
				}
			}

			output.append("}\n");
		}

		output.append("}\n");
		output.append("Text length " + String.format("%d", Helpers.stringToBytes(body).length - 1) + "\n");
		output.append(body + "}\n");

		assetData = Helpers.stringToBytes(output.toString());
	}

	/**
	 * Decode the raw asset data including the Linden Text properties
	 *
	 * @return true if the AssetData was successfully decoded
	 */

	private Matcher match(String string, String pattern) {
		return Pattern.compile(pattern).matcher(string);
	}

	@Override
	protected boolean decode() {
		embeddedItems = new ArrayList<>();
		bodyText = Helpers.EmptyString;

		if (assetData == null)
			return false;

		try {
			String data = Helpers.bytesToString(assetData);
			String[] lines = data.split("\n");
			int i = 0;
			Matcher m;

			// Version
			if (!(m = match(lines[i++], "Linden text version\\s+(\\d+)")).matches())
				throw new Exception("could not determine version");
			int version = Helpers.tryParseInt(m.group(1));
			if (version < 1 || version > 2)
				throw new Exception("unsupported version");
			m = match(lines[i++], "^\\s*\\{\\s*$");
			if (!m.matches())
				throw new Exception("wrong format");

			// Embedded items header
			if (!(m = match(lines[i++], "LLEmbeddedItems version\\s+(\\d+)")).matches())
				throw new Exception("could not determine embedded items version version");
			version = Helpers.tryParseInt(m.group(1));
			if (version != 1)
				throw new Exception("unsuported embedded item version");
			if (!(m = match(lines[i++], "^\\s*\\{\\s*$")).matches())
				throw new Exception("wrong format");

			// Item count
			if (!(m = match(lines[i++], "count\\s+(\\d+)")).matches())
				throw new Exception("wrong format");
			int count = Helpers.tryParseInt(m.group(1));

			// Decode individual items
			for (int n = 0; n < count; n++) {
				if (!(m = match(lines[i++], "^\\s*\\{\\s*$")).matches())
					throw new Exception("wrong format");

				// Index
				if (!(m = match(lines[i++], "ext char index\\s+(\\d+)")).matches())
					throw new Exception("missing ext char index");
				// warning CS0219: The variable `index' is assigned but its
				// value is never used
				// int index = int.Parse(m.group(1).Value);

				// Inventory item
				if (!(m = match(lines[i++], "inv_item\\s+0")).matches())
					throw new Exception("missing inv item");

				// Item itself
				UUID uuid = UUID.ZERO;
				UUID creatorID = UUID.ZERO;
				UUID ownerID = UUID.ZERO;
				UUID lastOwnerID = UUID.ZERO;
				UUID groupID = UUID.ZERO;
				Permissions permissions = Permissions.NoPermissions;
				int salePrice = 0;
				SaleType saleType = SaleType.Not;
				UUID parentID = UUID.ZERO;
				UUID assetID = UUID.ZERO;
				AssetType assetType = AssetType.Unknown;
				InventoryType inventoryType = InventoryType.Unknown;
				int flags = 0;
				String name = Helpers.EmptyString;
				String description = Helpers.EmptyString;
				Date creationDate = Helpers.Epoch;

				while (true) {
					if (!(m = match(lines[i++], "([^\\s]+)(\\s+)?(.*)?")).matches())
						throw new Exception("wrong format");
					String key = m.group(1);
					String val = m.group(3);
					if (key == "{")
						continue;
					if (key == "}")
						break;
					else if (key == "permissions") {
						int baseMask = 0;
						int ownerMask = 0;
						int groupMask = 0;
						int everyoneMask = 0;
						int nextOwnerMask = 0;

						while (true) {
							if (!(m = match(lines[i++], "([^\\s]+)(\\s+)?([^\\s]+)?")).matches())
								throw new Exception("wrong format");
							String pkey = m.group(1);
							String pval = m.group(3);

							if (pkey == "{")
								continue;
							if (pkey == "}")
								break;
							else if (pkey == "creator_id") {
								creatorID = new UUID(pval);
							} else if (pkey == "owner_id") {
								ownerID = new UUID(pval);
							} else if (pkey == "last_owner_id") {
								lastOwnerID = new UUID(pval);
							} else if (pkey == "group_id") {
								groupID = new UUID(pval);
							} else if (pkey == "base_mask") {
								baseMask = (int) Helpers.tryParseHex(pval);
							} else if (pkey == "owner_mask") {
								ownerMask = (int) Helpers.tryParseHex(pval);
							} else if (pkey == "group_mask") {
								groupMask = (int) Helpers.tryParseHex(pval);
							} else if (pkey == "everyone_mask") {
								everyoneMask = (int) Helpers.tryParseHex(pval);
							} else if (pkey == "next_owner_mask") {
								nextOwnerMask = (int) Helpers.tryParseHex(pval);
							}
						}
						permissions = new Permissions(creatorID, ownerID, lastOwnerID, groupID, baseMask, everyoneMask,
								groupMask, nextOwnerMask, ownerMask);
					} else if (key == "sale_info") {
						while (true) {
							if (!(m = match(lines[i++], "([^\\s]+)(\\s+)?([^\\s]+)?")).matches())
								throw new Exception("wrong format");
							String pkey = m.group(1);
							String pval = m.group(3);

							if (pkey == "{")
								continue;
							if (pkey == "}")
								break;
							else if (pkey == "sale_price") {
								salePrice = Helpers.tryParseInt(pval);
							} else if (pkey == "sale_type") {
								saleType = SaleType.setValue(pval);
							}
						}
					} else if (key == "item_id") {
						uuid = new UUID(val);
					} else if (key == "parent_id") {
						parentID = new UUID(val);
					} else if (key == "asset_id") {
						assetID = new UUID(val);
					} else if (key == "type") {
						assetType = AssetType.setValue(val);
					} else if (key == "inv_type") {
						inventoryType = InventoryType.setValue(val);
					} else if (key == "flags") {
						flags = (int) Helpers.tryParseHex(val);
					} else if (key == "name") {
						name = val.substring(0, val.lastIndexOf("|"));
					} else if (key == "desc") {
						description = val.substring(0, val.lastIndexOf("|"));
					} else if (key == "creation_date") {
						creationDate = Helpers.unixTimeToDateTime(Helpers.tryParseInt(val));
					}
				}
				InventoryItem finalEmbedded = InventoryItem.create(inventoryType, uuid, parentID, ownerID);

				finalEmbedded.permissions = permissions;
				finalEmbedded.salePrice = salePrice;
				finalEmbedded.saleType = saleType;
				finalEmbedded.assetID = assetID;
				finalEmbedded.assetType = assetType;
				finalEmbedded.itemFlags = flags;
				finalEmbedded.name = name;
				finalEmbedded.description = description;
				finalEmbedded.creationDate = creationDate;

				embeddedItems.add(finalEmbedded);

				if (!(m = match(lines[i++], "^\\s*\\}\\s*$")).matches())
					throw new Exception("wrong format");
			}

			// Text size
			if (!(m = match(lines[i++], "^\\s*\\}\\s*$")).matches())
				throw new Exception("wrong format");
			if (!(m = match(lines[i++], "Text length\\s+(\\d+)")).matches())
				throw new Exception("could not determine text length");

			// Read the rest of the notecard
			while (i < lines.length) {
				bodyText += lines[i++] + "\n";
			}
			bodyText = bodyText.substring(0, bodyText.lastIndexOf("}"));
			return true;
		} catch (Exception ex) {
			logger.error("Decoding notecard asset failed: " + ex.getMessage());
			return false;
		}
	}
}
