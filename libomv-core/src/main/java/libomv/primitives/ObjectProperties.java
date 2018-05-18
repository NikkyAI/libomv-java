/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Copyright (c) 2006, Lateral Arts Limited
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

import java.util.Date;

import libomv.model.LLObject.SaleType;
import libomv.primitives.Primitive.ObjectCategory;
import libomv.types.Permissions;
import libomv.types.UUID;
import libomv.utils.Helpers;

// Extended properties to describe an object
public class ObjectProperties {
	public UUID objectID;
	public Date creationDate;
	public Permissions permissions;
	public int ownershipCost;
	public SaleType saleType;
	public int salePrice;
	public byte aggregatePerms;
	public byte aggregatePermTextures;
	public byte aggregatePermTexturesOwner;
	public ObjectCategory category;
	public short inventorySerial;
	public UUID itemID;
	public UUID folderID;
	public UUID fromTaskID;
	public String name;
	public String description;
	public String touchName;
	public String sitName;
	public UUID[] textureIDs;

	// Default constructor
	public ObjectProperties() {
		name = Helpers.EmptyString;
		description = Helpers.EmptyString;
		touchName = Helpers.EmptyString;
		sitName = Helpers.EmptyString;
	}

	public ObjectProperties(ObjectProperties p) {
		objectID = p.objectID;
		creationDate = p.creationDate;
		permissions = new Permissions(p.permissions);
		ownershipCost = p.ownershipCost;
		saleType = p.saleType;
		salePrice = p.salePrice;
		aggregatePerms = p.aggregatePerms;
		aggregatePermTextures = p.aggregatePermTextures;
		aggregatePermTexturesOwner = p.aggregatePermTexturesOwner;
		category = p.category;
		inventorySerial = p.inventorySerial;
		itemID = p.itemID;
		folderID = p.folderID;
		fromTaskID = p.fromTaskID;
		name = p.name;
		description = p.description;
		touchName = p.touchName;
		sitName = p.sitName;
		textureIDs = new UUID[p.textureIDs.length];
		for (int i = 0; i < p.textureIDs.length; i++)
			textureIDs[i] = p.textureIDs[i];
	}

	/**
	 * Set the properties that are set in an ObjectPropertiesFamily packet
	 *
	 * @param props
	 *            {@link ObjectProperties} that has been partially filled by an
	 *            ObjectPropertiesFamily packet
	 */
	public void setFamilyProperties(ObjectProperties props) {
		objectID = props.objectID;
		permissions = props.permissions;
		ownershipCost = props.ownershipCost;
		saleType = props.saleType;
		salePrice = props.salePrice;
		category = props.category;
		name = props.name;
		description = props.description;
	}

	public byte[] getTextureIDBytes() {
		if (textureIDs == null || textureIDs.length == 0)
			return Helpers.EmptyBytes;

		byte[] bytes = new byte[16 * textureIDs.length];
		for (int i = 0; i < textureIDs.length; i++)
			textureIDs[i].toBytes(bytes, 16 * i);

		return bytes;
	}
}
