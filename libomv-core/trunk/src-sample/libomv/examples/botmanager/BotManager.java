/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Portions Copyright (c) 2006, Lateral Arts Limited
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
package libomv.examples.botmanager;

import java.util.Vector;

public class BotManager implements BotKilledHandler
{
	protected Vector<Bot> Bots;

	public BotManager() {
		Bots = new Vector<Bot>();
	}

	public void AddBot(String firstName, String lastName, String password) throws Exception
	{
		Bot bot = new Bot(this, this, firstName, lastName, password);
		Bots.addElement(bot);
		AddBotToDB(bot);
	}

	protected void AddBotToDB(Bot bot) {
		;
	}

	@Override
	public void botKilledHandler(Bot bot) {
		System.out.println(bot.toString() + " was killed");

		if (Bots.contains(bot)) {
			Bots.remove(bot);
		}
	}

	public static void main(String args) {
		BotManager manager = new BotManager();
		try
		{
		    manager.AddBot("Frederick", "Martian", "");
		}
		catch (Exception ex)
		{
			
		}
	}
}
