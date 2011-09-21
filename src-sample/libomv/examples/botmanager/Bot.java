/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
 * Portions Copyright (c) 2009-2011, Frederick Martian
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

import libomv.GridClient;
import libomv.LoginManager.LoginParams;
import libomv.NetworkManager.DisconnectedCallbackArgs;
import libomv.utils.Callback;

public class Bot extends Callback<DisconnectedCallbackArgs>
{
	public final static String LOGIN_SERVER = "https://login.agni.lindenlab.com/cgi-bin/login.cgi";

	public GridClient Client;

	public String FirstName;

	public String LastName;

	public String Password;

	protected boolean loggedIn = false;

	public boolean getLoggedIn() {
		return loggedIn;
	}

	protected BotManager Manager;

	protected BotKilledHandler KillHandler;

	public Bot(BotManager parent, BotKilledHandler killHandler, String firstName, String lastName, String password) throws Exception
	{
		Manager = parent;
		KillHandler = killHandler;
		FirstName = firstName;
		LastName = lastName;
		Password = password;

		Client = new GridClient();
		Client.Network.OnDisconnected.add(this);
	}

	public boolean Spawn() throws Exception
	{
		if (!loggedIn)
		{
			Kill();
		}

		LoginParams loginParams = Client.Login.DefaultLoginParams(FirstName, LastName, Password);
		loginParams.URI = LOGIN_SERVER;
		loggedIn = Client.Login.Login(loginParams);
		return loggedIn;
	}

	public void Kill() throws Exception {
		if (loggedIn) {
			Client.Network.Logout();
		}
	}

	@Override
	public void callback(DisconnectedCallbackArgs params) {
		KillHandler.botKilledHandler(this);
	}
}
