/**
 * Copyright (c) 2009, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
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
package libomv.examples.TestClient;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.LineIterator;

import libomv.LoginManager;
import libomv.Settings;
import libomv.examples.TestClient.ClientManager.LoginDetails;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class Program
{
    public static String LoginURI;

    private static void Usage()
    {
        System.out.println("Usage: \n" +
                "TestClient.exe [--first firstname --last lastname --pass password] [--file userlist.txt] [--loginuri=\"uri\"] [--startpos \"sim/x/y/z\"] [--master \"master name\"] [--masterkey \"master uuid\"] [--gettextures] [--scriptfile \"filename\"]");
    }

    static public void main(String[] args) throws Exception
    {
        Arguments arguments = new Arguments(args);

        List<LoginDetails> accounts = new ArrayList<LoginDetails>();
        LoginDetails account;
        boolean groupCommands = false;
        String masterName = Helpers.EmptyString;
        UUID masterKey = UUID.Zero;
        File file = null;
        boolean getTextures = false;
        boolean noGUI = false; // true if to not prompt for input
        File scriptFile = null;

        if (arguments.get("groupcommands") != null)
            groupCommands = true;

        if (arguments.get("masterkey") != null)
            masterKey = UUID.Parse(arguments.get("masterkey"));

        if (arguments.get("master") != null)
            masterName = arguments.get("master");

        if (arguments.get("loginuri") != null)
            LoginURI = arguments.get("loginuri");
        if (LoginURI == null || LoginURI.isEmpty())
            LoginURI = Settings.AGNI_LOGIN_SERVER;
        Logger.Log("Using login URI " + LoginURI, LogLevel.Info);

        if (arguments.get("gettextures") != null)
            getTextures = true;

        if (arguments.get("nogui") != null)
            noGUI = true;

        if (arguments.get("scriptfile") != null)
        {
            scriptFile = new File(arguments.get("scriptfile"));
            if (!scriptFile.exists())
            {
                Logger.Log(String.format("File %s Does not exist", scriptFile), LogLevel.Error);
                return;
            }
        }

        if (arguments.get("file") != null)
        {
            file = new File(arguments.get("file"));

            if (!file.exists())
            {
                Logger.Log(String.format("File %s Does not exist", file), LogLevel.Error);
                return;
            }

            // Loading names from a file
            try
            {
                LineIterator reader = new LineIterator(new FileReader(file));
                String line;
                int lineNumber = 0;

                while (reader.hasNext())
                {
                	line = reader.nextLine();
                    lineNumber++;
                    String[] tokens = line.trim().split("[ ,]");

                    if (tokens.length >= 3)
                    {
                        account = ClientManager.getInstance().new LoginDetails();
                        account.FirstName = tokens[0];
                        account.LastName = tokens[1];
                        account.Password = tokens[2];

                        if (tokens.length >= 4) // Optional starting position
                        {
                            String[] startbits = tokens[3].split("/");
                            account.StartLocation = LoginManager.StartLocation(startbits[0], Integer.parseInt(startbits[1]),
                                Integer.parseInt(startbits[2]), Integer.parseInt(startbits[3]));
                        }

                        accounts.add(account);
                    }
                    else
                    {
                        Logger.Log("Invalid data on line " + lineNumber +
                            ", must be in the format of: FirstName LastName Password [Sim/StartX/StartY/StartZ]",
                            LogLevel.Warning);
                    }
                }
            }

            catch (Exception ex)
            {
                Logger.Log("Error reading from " + args[1], LogLevel.Error, ex);
                return;
            }
        }
        else if (arguments.get("first") != null && arguments.get("last") != null && arguments.get("pass") != null)
        {
            // Taking a single login off the command-line
            account = ClientManager.getInstance().new LoginDetails();
            account.FirstName = arguments.get("first");
            account.LastName = arguments.get("last");
            account.Password = arguments.get("pass");

            accounts.add(account);
        }
        else if (arguments.get("help") != null)
        {
            Usage();
            return;
        }

        for (LoginDetails a : accounts)
        {
            a.GroupCommands = groupCommands;
            a.MasterName = masterName;
            a.MasterKey = masterKey;
            a.URI = LoginURI;

            if (arguments.get("startpos") != null)
            {
                String[] startbits = arguments.get("startpos").split("/");
                a.StartLocation = LoginManager.StartLocation(startbits[0], Integer.parseInt(startbits[1]),
                		Integer.parseInt(startbits[2]), Integer.parseInt(startbits[3]));
            }
        }

        // Login the accounts and run the input loop
        ClientManager.getInstance().Start(accounts, getTextures);

        if (scriptFile != null)
            ClientManager.getInstance().DoCommandAll("script " + scriptFile.getAbsolutePath(), UUID.Zero);

        // Then Run the ClientManager normally
        ClientManager.getInstance().Run(noGUI);
    }
}