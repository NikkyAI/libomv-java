/**
 * Copyright (c) 2009, openmetaverse.org
 * Copyright (c) 2009-2013, Frederick Martian
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
package libomv.examples.TestClient.Commands.Inventory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import libomv.Simulator;
import libomv.assets.AssetManager.ImageType;
import libomv.assets.AssetTexture;
import libomv.assets.AssetWearable.AvatarTextureIndex;
import libomv.assets.TexturePipeline.TextureDownloadCallback;
import libomv.assets.TexturePipeline.TextureRequestState;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.imaging.J2KImage;
import libomv.imaging.ManagedImage;
import libomv.imaging.TGAImage;
import libomv.primitives.Avatar;
import libomv.primitives.TextureEntry;
import libomv.types.UUID;

public class DumpOutfitCommand extends Command
{
    private static final String usage = "Usage: dumpoutfit <avatar-uuid>";

    ArrayList<UUID> OutfitAssets = new ArrayList<UUID>();

    public DumpOutfitCommand(TestClient testClient)
    {
        Name = "dumpoutfit";
        Description = "Dumps all of the textures from an avatars outfit to the hard drive. " + usage;
        Category = CommandCategory.Inventory;

    }

    public String execute(String[] args, UUID fromAgentID)
    {
        if (args.length < 1)
            return usage;

        Avatar targetAv = null;
        ArrayList<Simulator> sims = Client.Network.getSimulators();
        UUID target = UUID.parse(args[0]);
        if (target != null)
        {
            synchronized (sims)
            {
                for (int i = 0; i < sims.size(); i++)
                {
                    targetAv = sims.get(i).findAvatar(target);
                    if (targetAv != null)
                    	break;
                }
            }
            if (targetAv == null)
            	return "Couldn't find avatar " + target.toString();
        }
        else if (args.length >= 2)
        {
        	String name = args[0] + " " + args[1];
            if (name != null)
            {
                synchronized (sims)
                {
                    for (int i = 0; i < sims.size(); i++)
                    {
                        targetAv = sims.get(i).findAvatar(name);
                        if (targetAv != null)
                        	break;
                    }
                }
                if (targetAv == null)
                	return "Couldn't find avatar " + name;
            }
        }
        
        StringBuilder output = new StringBuilder("Downloading ");

        synchronized (OutfitAssets)
        {
        	OutfitAssets.clear();

            for (int j = 0; j < targetAv.Textures.getNumTextures(); j++)
            {
                TextureEntry.TextureEntryFace face = targetAv.Textures.faceTextures[j];

                if (face != null)
                {
                    ImageType type = ImageType.Normal;

                    switch (AvatarTextureIndex.setValue(j))
                    {
                        case HeadBaked:
                        case EyesBaked:
                        case UpperBaked:
                        case LowerBaked:
                        case SkirtBaked:
                            type = ImageType.Baked;
                            break;
                    }

                    OutfitAssets.add(face.getTextureID());
                    Client.Assets.RequestImage(face.getTextureID(), type, new Assets_OnImageReceived());
                    output.append(AvatarTextureIndex.setValue(j).toString());
                    output.append(" ");
                }
        	}
        }
        return output.toString();
    }

    private class Assets_OnImageReceived implements TextureDownloadCallback
    {
		@Override
    	public void callback(TextureRequestState state, AssetTexture assetTexture)
    	{
	        synchronized (OutfitAssets)
	        {
	            if (OutfitAssets.contains(assetTexture.getAssetID()))
	            {
	                if (state == TextureRequestState.Finished)
	                {
	                    try
	                    {
	                    	FileOutputStream os = new FileOutputStream(new File(assetTexture.getAssetID() + ".jp2"));
	                        os.write(assetTexture.AssetData);
	                        os.close();
	                        System.out.println("Wrote JPEG2000 image " + assetTexture.getAssetID() + ".jp2");

	                        ManagedImage imgData = new J2KImage(new ByteArrayInputStream(assetTexture.AssetData));
	                        TGAImage tgaImage = new TGAImage(imgData);
	                        os = new FileOutputStream(new File(assetTexture.getAssetID() + ".tga"));
	                        tgaImage.encode(os);
	                        os.close();
	                        System.out.println("Wrote TGA image " + assetTexture.getAssetID() + ".tga");
	                    }
	                    catch (Exception e)
	                    {
	                        System.out.println(e.toString());
	                    }
	                }
	                else
	                {
	                	System.out.println("Failed to download image " + assetTexture.getAssetID());
	                }
	                OutfitAssets.remove(assetTexture.getAssetID());
	            }
	        }
    	}
    }
}
