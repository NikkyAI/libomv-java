/**
 * Copyright (c) 2009, openmetaverse.org
 * Copyright (c) 2009-2012, Frederick Martian
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
package libomv.examples.TestClient.Commands.System;

import libomv.AvatarManager.ViewerEffectCallbackArgs;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Callback;

public class ShowEffectsCommand extends Command
{
    private static final String usage = "Usage: showeffects [on/off]";
    private boolean ShowEffects = false;

    public ShowEffectsCommand(TestClient testClient)
    {
        Name = "showeffects";
        Description = "Prints out information for every viewer effect that is received. " + usage;
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
        }
        else if (args.length == 1)
        {
            ShowEffects = args[0].equalsIgnoreCase("on");
        }
        else
        {
            return usage;
        }

        if (ShowEffects)
        {
            return "Viewer effects will be shown on the console";
        }
        return "Viewer effects will not be shown";        
    }        
}