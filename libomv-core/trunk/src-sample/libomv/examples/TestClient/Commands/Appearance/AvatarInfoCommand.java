package libomv.examples.TestClient.Commands.Appearance;

import libomv.assets.AssetWearable.AvatarTextureIndex;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.primitives.Avatar;
import libomv.primitives.Primitive;
import libomv.primitives.TextureEntry.TextureEntryFace;
import libomv.types.UUID;

public class AvatarInfoCommand extends Command
{
    public AvatarInfoCommand(TestClient testClient)
    {
        Name = "avatarinfo";
        Description = "Print out information on a nearby avatar. Usage: avatarinfo [firstname] [lastname]";
        Category = CommandCategory.Appearance;
    }

	@Override
    public String execute(String[] args, UUID fromAgentID)
    {
        if (args.length != 2)
            return "Usage: avatarinfo [firstname] [lastname]";

        String targetName = args[0] + " " + args[1];

        Avatar foundAv = Client.Network.getCurrentSim().findAvatar(targetName);

        if (foundAv != null)
        {
            StringBuilder output = new StringBuilder();

            output.append(targetName + " (" + foundAv.ID + ")\n");
 
            for (int i = 0; i < foundAv.Textures.faceTextures.length; i++)
            {
                if (foundAv.Textures.faceTextures[i] != null)
                {
                    TextureEntryFace face = foundAv.Textures.faceTextures[i];
                    AvatarTextureIndex type = AvatarTextureIndex.setValue(i);

                    output.append(type + ": " + face.getTextureID() + "\n");
                }
            }

            return output.toString();
        }
        else
        {
            return "No nearby avatar with the name " + targetName;
        }
    }
}
