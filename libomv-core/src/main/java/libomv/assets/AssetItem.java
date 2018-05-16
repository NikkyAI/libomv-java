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

import libomv.model.Asset;
import libomv.types.UUID;

/** Base class for all Asset types */
public abstract class AssetItem implements Asset {

	/** True if the asset is only stored on the server temporarily */
	public boolean Temporary;
	/** The assets unique unique ID */
	public UUID AssetID;
	/** A byte array containing the raw asset data */
	public byte[] AssetData;

	public UUID getAssetID() {
		return AssetID;
	}

	public void setAssetID(UUID value) {
		AssetID = value;
	}

	/**
	 * retrieve the binary asset data byte stream and create it if it is not
	 * available
	 */
	public byte[] getAssetData() {
		if (AssetData == null)
			encode();
		return AssetData;
	}

	/**
	 * invalidate the binary asset data byte stream. This should be called whenever
	 * something in a derived class is changed that would change the binary asset
	 * data
	 */
	public void invalidateAssetData() {
		AssetData = null;
	}

	/**
	 * Construct a new Asset object
	 *
	 * @param assetID
	 *            A unique <see cref="UUID"/> specific to this asset
	 * @param assetData
	 *            A byte array containing the raw asset data
	 */
	public AssetItem(UUID assetID, byte[] assetData) {
		AssetID = assetID;
		AssetData = assetData;
		decode();
	}

	/**
	 * Regenerates the <code>AssetData</code> byte array from the properties of the
	 * derived class.
	 */
	protected abstract void encode();

	/**
	 * Decodes the AssetData, placing it in appropriate properties of the derived
	 * class.
	 *
	 * @return True if the asset decoding succeeded, otherwise false
	 */
	protected abstract boolean decode();
}
