package libomv.examples.TestClient.Commands.Communication;

import java.util.HashMap;
import java.util.Map.Entry;

import libomv.AvatarManager.AvatarPickerReplyCallbackArgs;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.TimeoutEvent;

public class IMCommand extends Command
{
    private static final String usage = "Usage: im [firstname] [lastname] [message]";
    String ToAvatarName = Helpers.EmptyString;
    TimeoutEvent<UUID> NameSearchEvent = new TimeoutEvent<UUID>();
    HashMap<String, UUID> Name2Key = new HashMap<String, UUID>();

    public IMCommand(TestClient testClient)
    {
        testClient.Avatars.OnAvatarPickerReply.add(new Avatars_AvatarPickerReply());

        Name = "im";
        Description = "Instant message someone. " + usage;
        Category = CommandCategory.Communication;
    }
    
    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length < 3)
            return usage;

        ToAvatarName = args[0] + " " + args[1];

        // Build the message
        String message = Helpers.EmptyString;
        for (int ct = 2; ct < args.length; ct++)
            message += args[ct] + " ";
        message = message.trim();
        if (message.length() > 1023) message = message.substring(0, 1023);

        if (!Name2Key.containsKey(ToAvatarName.toLowerCase()))
        {
            // Send the Query
            Client.Avatars.RequestAvatarNameSearch(ToAvatarName, new UUID());

            UUID uuid = NameSearchEvent.waitOne(6000, true);
            if (uuid != null)
            {
                Name2Key.put(ToAvatarName.toLowerCase(), uuid);            	
            }
        }

        if (Name2Key.containsKey(ToAvatarName.toLowerCase()))
        {
            UUID id = Name2Key.get(ToAvatarName.toLowerCase());

            Client.Self.InstantMessage(id, message);
            return "Instant messaged " + ToAvatarName + " {" + id + "} with message: " + message;
        }
        else
        {
            return "Name lookup for " + ToAvatarName + " failed";
        }
    }

    private class Avatars_AvatarPickerReply implements Callback<AvatarPickerReplyCallbackArgs>
    {
    	public boolean callback(AvatarPickerReplyCallbackArgs e)
    	{
            for (Entry<UUID, String> kvp : e.getAvatars().entrySet())
            {
                if (kvp.getValue().equalsIgnoreCase(ToAvatarName))
                {
                    NameSearchEvent.set(kvp.getKey());
                }
            }
            return false;
    	}
    }
}

