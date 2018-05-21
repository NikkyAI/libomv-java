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

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import libomv.model.asset.AssetType;
import libomv.types.UUID;
import libomv.utils.Helpers;

/** Represents a Callingcard with AvatarID and Position vector */
public class AssetCallingCard extends AssetItem {
	private static final Logger logger = Logger.getLogger(AssetCallingCard.class);

	/**
	 * Construct an Asset object of type Callingcard
	 *
	 * @param assetID
	 *            A unique @see UUID specific to this asset
	 * @param assetData
	 *            A byte array containing the raw asset data
	 */
	public AssetCallingCard(UUID assetID, byte[] assetData) {
		super(assetID, assetData);
	}

	/**
	 * Construct an Asset object of type Callingcard
	 *
	 * @param avatarID
	 *            UUID of the target avatar
	 */
	public AssetCallingCard(UUID avatarID) {
		super(avatarID, null);
	}

	/* Returns asset type */
	@Override
	public AssetType getAssetType() {
		return AssetType.CallingCard;
	}

	/** UUID of the Callingcard target avatar */
	public UUID getAvatarID() {
		return assetID;
	}

	/**
	 * Encode the raw contents of a string with the specific Callingcard format
	 */
	@Override
	protected void encode() {
		String temp = "Callingcard version 2\n" + "avatar_id " + assetID.toString() + "\n";
		assetData = Helpers.stringToBytes(temp);
	}

	/**
	 * Decode the raw asset data, populating the AvatarID and Position
	 *
	 * @return True if the AssetData was successfully decoded to a UUID and Vector
	 */
	@Override
	protected boolean decode() {
		if (assetData == null)
			return false;

		try {
			String text = Helpers.bytesToString(assetData);
			if (text != null && text.toLowerCase().contains("callingcard version 2")) {
				assetID = new UUID(text.substring(text.indexOf("avatar_id") + 9, 38).trim());
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
		return false;
	}

}
