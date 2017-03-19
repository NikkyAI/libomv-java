/**
 * Copyright (c) 2006-2016, openmetaverse.org
 * Copyright (c) 2016-2017, Frederick Martian
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
package libomv.assets.archiving;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import libomv.assets.AssetItem;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.Logger;

public class AssetsArchiver
{
    ///// Post a message to the log every x assets as a progress bar
    //static int LOG_ASSET_LOAD_NOTIFICATION_INTERVAL = 50;

    /// Archive assets
    protected Map<UUID, AssetItem> m_assets;

    public AssetsArchiver(Map<UUID, AssetItem> assets)
    {
        m_assets = assets;
    }

    /// Archive the assets given to this archiver to the given archive.
    /// <param name="archive"></param>
    public void Archive(TarArchiveWriter archive) throws IOException
    {
        //WriteMetadata(archive);
        writeData(archive);
    }

    /// Write an assets metadata file to the given archive
    /// <param name="archive"></param>
    protected void writeMetadata(TarArchiveWriter archive) throws XmlPullParserException, IllegalArgumentException, IllegalStateException, IOException
    {
        StringWriter sw = new StringWriter();
		XmlSerializer writer = XmlPullParserFactory.newInstance().newSerializer();
		writer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
		writer.setOutput(sw);
		writer.startDocument(Helpers.ASCII_ENCODING, null);
		writer.startTag(null, "assets");

        for (UUID uuid : m_assets.keySet())
        {
            AssetItem asset = m_assets.get(uuid);

            if (asset != null)
            {
            	writer.startTag(null, "asset");

				String extension = ArchiveConstants.getExtensionForType(asset.getAssetType());

                writeString(writer, "filename", uuid.toString() + extension);

                writeString(writer, "name", uuid.toString());
                writeString(writer, "description", Helpers.EmptyString);
                writeString(writer, "asset-type", asset.getAssetType().toString());
                writer.endTag(null, "asset");
            }
        }

        writer.endTag(null,  "assets");
        writer.endDocument();
        archive.writeFile("assets.xml", sw.toString());
    }

    /// <summary>
    /// Write asset data files to the given archive
    /// </summary>
    /// <param name="archive"></param>
    protected void writeData(TarArchiveWriter archive) throws IOException
    {
        // It appears that gtar, at least, doesn't need the intermediate directory entries in the tar
        //archive.AddDir("assets");
        for (UUID uuid : m_assets.keySet())
        {
            AssetItem asset = m_assets.get(uuid);

			String extension = ArchiveConstants.getExtensionForType(asset.getAssetType());
            if (extension == null);
            {
                Logger.Log(String.format(
                    "Unrecognized asset type %s with uuid %s. This asset will be saved but unable to be reloaded",
                    asset.getAssetType(), asset.getAssetID()), Logger.LogLevel.Warning);
            }

            asset.encode();

            archive.writeFile(ArchiveConstants.ASSETS_PATH + uuid.toString() + extension, asset.AssetData);
        }
    }
    
	protected void writeString(XmlSerializer writer, String tag, String text) throws IllegalArgumentException, IllegalStateException, IOException
	{
        writer.startTag(null, tag).text(text).endTag(null, tag);	
	} 
}
