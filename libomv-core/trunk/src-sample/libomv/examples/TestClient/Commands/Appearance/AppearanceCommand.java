package libomv.examples.TestClient.Commands.Appearance;

import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

/**
 * Set avatars current appearance to appearance last stored on simulator
 */
public class AppearanceCommand extends Command
{
 	public AppearanceCommand(TestClient testClient)
    {
        Name = "appearance";
        Description = "Set your current appearance to your last saved appearance. Usage: appearance [rebake]";
        Category = CommandCategory.Appearance;
    }

	@Override
    public String execute(String[] args, UUID fromAgentID)
    {
        Client.Appearance.RequestSetAppearance((args.length > 0 && args[0].equals("rebake")));
        return "Appearance sequence started";
    }
}
