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
package libomv.examples.TestClient.Commands.Movement;

import libomv.AgentManager.AgentMovement;
import libomv.ObjectManager.TerseObjectUpdateCallbackArgs;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.Callback;

public class FlyToCommand extends Command
{
    Vector3 myPos;
    Vector2 myPos0;
    Vector3 target;
    Vector2 target0;
    float diff, olddiff, saveolddiff;
    long startTime;
    int duration;
    boolean running = false;
    Callback<TerseObjectUpdateCallbackArgs> OnTerseUpdateCallback = new TerseUpdateCallback();		
    		
    private class TerseUpdateCallback implements Callback<TerseObjectUpdateCallbackArgs>
    {
    	public boolean callback(TerseObjectUpdateCallbackArgs e)
    	{
	        if (startTime != 0)
	        {
	        	try
	        	{
			        if (e.getUpdate().LocalID == Client.Self.getLocalID())
			        {
			            XYMovement();
			            ZMovement();
			            if (Client.Self.getMovement().getAtPos() || Client.Self.getMovement().getAtNeg())
			            {
			                Client.Self.getMovement().TurnToward(target);
			                Debug("Flyxy ");
			            }
			            else if (Client.Self.getMovement().getUpPos() || Client.Self.getMovement().getUpNeg())
			            {
			                Client.Self.getMovement().TurnToward(target);
			                //Client.Self.Movement.SendUpdate(false);
			                Debug("Fly z ");
			            }
			            else if (Vector3.distance(target, Client.Self.getAgentPosition()) <= 2.0)
			            {
			                EndFlyto();
			                Debug("At Target");
			            }
			        }
			        if (System.currentTimeMillis() - startTime > duration)
			        {
			            EndFlyto();
			            Debug("End Flyto");
			        }
	        	}
	        	catch (Exception ex)
	        	{}
	        }
        	return false;
	    }
    }
    
    public FlyToCommand(TestClient Client)
    {
        Name = "FlyTo";
        Description = "Fly the avatar toward the specified position for a maximum of seconds. Usage: FlyTo x y z [seconds]";
        Category = CommandCategory.Movement;
    }

    @Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (args.length > 4 || args.length < 3)
            return "Usage: FlyTo x y z [seconds]";

        if (running)
            return "Already in progress, wait for the previous FlyTo to finish";

        try
        {
            running = true;
        	target = new Vector3(Float.valueOf(args[0]), Float.valueOf(args[1]), Float.valueOf(args[2]));

        	// Subscribe to terse update events while this command is running
            Client.Objects.OnTerseObjectUpdate.add(OnTerseUpdateCallback);

            target0.X = target.X;
            target0.Y = target.Y;

            try
            {
            	duration = 10000;
            	if (args.length == 4)
            	{
                    duration = 1000 * Integer.valueOf(args[3]);
            	}
            }
            catch (NumberFormatException ex)
            {
            }

            startTime = System.currentTimeMillis();
            Client.Self.getMovement().setFly(true);
            Client.Self.getMovement().setAtPos(true);
            Client.Self.getMovement().setAtNeg(false);
            ZMovement();
            Client.Self.getMovement().TurnToward(target);

            return String.format("flying to %s in %f seconds", target.toString(), duration / 1000);
        }
        catch (NumberFormatException ex)
        {
            return "Usage: FlyTo x y z [seconds]";
        }
    }

    private boolean XYMovement()
    {
        boolean res = false;

        myPos = Client.Self.getAgentPosition();
        myPos0.X = myPos.X;
        myPos0.Y = myPos.Y;
        diff = Vector2.Distance(target0, myPos0);
        Vector2 vvel = new Vector2(Client.Self.getVelocity().X, Client.Self.getVelocity().Y);
        float vel = vvel.Length();
        if (diff >= 10.0)
        {
            Client.Self.getMovement().setAtPos(true);
            
            res = true;
        }
        else if (diff >= 2 && vel < 5)
        {
            Client.Self.getMovement().setAtPos(true);
        }
        else
        {
            Client.Self.getMovement().setAtPos(false);
            Client.Self.getMovement().setAtNeg(false);
        }
        saveolddiff = olddiff;
        olddiff = diff;
        return res;
    }

    private void ZMovement()
    {
        Client.Self.getMovement().setUpPos(false);
        Client.Self.getMovement().setUpNeg(false);
        float diffz = (target.Z - Client.Self.getAgentPosition().Z);
        if (diffz >= 20.0)
            Client.Self.getMovement().setUpPos(true);
        else if (diffz <= -20.0)
            Client.Self.getMovement().setUpNeg(true);
        else if (diffz >= +5.0 && Client.Self.getVelocity().Z < +4.0)
            Client.Self.getMovement().setUpPos(true);
        else if (diffz <= -5.0 && Client.Self.getVelocity().Z > -4.0)
            Client.Self.getMovement().setUpNeg(true);
        else if (diffz >= +2.0 && Client.Self.getVelocity().Z < +1.0)
            Client.Self.getMovement().setUpPos(true);
        else if (diffz <= -2.0 && Client.Self.getVelocity().Z > -1.0)
            Client.Self.getMovement().setUpNeg(true);
    }

    private void EndFlyto() throws Exception
    {
        // Unsubscribe from terse update events
        Client.Objects.OnTerseObjectUpdate.remove(OnTerseUpdateCallback);

        startTime = 0;
        Client.Self.getMovement().setAtPos(false);
        Client.Self.getMovement().setAtNeg(false);
        Client.Self.getMovement().setUpPos(false);
        Client.Self.getMovement().setUpNeg(false);
        Client.Self.getMovement().SendUpdate(false);

        running = false;
    }

    private void Debug(String x)
    {
    	AgentMovement mov = Client.Self.getMovement();
        System.out.format(x + " %.3f %.3f %.3f diff %.5f olddiff %.5f  At:%.5f %.5f  Up:%.5f %.5f  v: %s w: %s\n",
            myPos.X, myPos.Y, myPos.Z, diff, saveolddiff,
            mov.getAtPos(), mov.getAtNeg(), mov.getUpPos(), mov.getUpNeg(),
            Client.Self.getVelocity().toString(), Client.Self.getAngularVelocity().toString());
    }
}
