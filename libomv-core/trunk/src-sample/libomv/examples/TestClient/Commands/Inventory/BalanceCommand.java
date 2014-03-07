/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2014, Frederick Martian
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

import libomv.AgentManager.BalanceCallbackArgs;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.TimeoutEvent;

public class BalanceCommand extends Command
{
    public BalanceCommand(TestClient testClient)
	{
		Name = "balance";
		Description = "Shows the amount of L$. Usage: balance";
        Category = CommandCategory.Other;
	}

    @Override
	public String execute(String[] args, UUID fromAgentID) throws Exception
	{
        final TimeoutEvent<Integer> waitBalance = new TimeoutEvent<Integer>();
        
        Callback<BalanceCallbackArgs> handler = new Callback<BalanceCallbackArgs>()
        {
        	@Override
			public boolean callback(BalanceCallbackArgs args)
        	{
        		waitBalance.set(args.getBalance());
        		return true;
        	}
        };
                
        Client.Self.OnBalanceUpdated.add(handler, true);            
        Client.Self.RequestBalance();
        String result = "Timeout waiting for balance reply";
        Integer balance = waitBalance.waitOne(10000);
        if (balance != null)
        {
            result = Client.toString() + " has L$: " + balance;
        }
        return result;            
	}
}
