package libomv.examples.TestClient.Commands.System;

import libomv.AvatarManager.ViewerEffectCallbackArgs;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Callback;

public class ShowEffectsCommand extends Command
{
    boolean ShowEffects = false;

    public ShowEffectsCommand(TestClient testClient)
    {
        Name = "showeffects";
        Description = "Prints out information for every viewer effect that is received. Usage: showeffects [on/off]";
        Category = CommandCategory.Other;

        testClient.Avatars.OnViewerEffect.add(new Avatars_ViewerEffect());
    }

    private class Avatars_ViewerEffect implements Callback<ViewerEffectCallbackArgs>
    {
    	public boolean callback(ViewerEffectCallbackArgs e)
        {
            if (ShowEffects)
            {
            	switch (e.getType())
            	{
            		case LookAt:
                        System.out.println(String.format("ViewerEffect [LookAt]: SourceID: %s TargetID: %s TargetPos: %s Type: %d Duration: %f ID: %s",
                                e.getSourceAvatar(), e.getTargetObject(), e.getTargetPos(), e.getTarget(), e.getDuration(), e.getDataID()));
            			break;
            		case PointAt:
                        System.out.println(String.format("ViewerEffect [PointAt]: SourceID: %s TargetID: %s TargetPos: %s Type: %d Duration: %f ID: %s",
                                e.getSourceAvatar(), e.getTargetObject(), e.getTargetPos(), e.getTarget(), e.getDuration(), e.getDataID()));
            			break;
            		default:
            			System.out.println(String.format("ViewerEffect [%s]: SourceID: %s TargetID: %s TargetPos: %s Duration: %f ID: %s",
            					e.getType(), e.getSourceAvatar(), e.getTargetObject(), e.getTargetPos(), e.getDuration(), e.getDataID()));
            	}
            }
            return false;
        }
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length == 0)
        {
            ShowEffects = true;
            return "Viewer effects will be shown on the console";
        }
        else if (args.length == 1)
        {
            if (args[0] == "on")
            {
                ShowEffects = true;
                return "Viewer effects will be shown on the console";
            }
            else
            {
                ShowEffects = false;
                return "Viewer effects will not be shown";
            }
        }
        else
        {
            return "Usage: showeffects [on/off]";
        }
    }        
}