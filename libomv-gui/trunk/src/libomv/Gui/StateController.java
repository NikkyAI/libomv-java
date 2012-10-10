/**
 * Copyright (c) 2009-2012, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the libomv-java project nor the names of its
 *   contributors may be used to endorse or promote products derived from
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
package libomv.Gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserFactory;
import com.teamdev.jxbrowser.BrowserType;

import libomv.AgentManager.BalanceCallbackArgs;
import libomv.Gui.components.LoginPane;
import libomv.Gui.components.OnlinePane;
import libomv.Gui.components.ProgressPane;
import libomv.Gui.windows.CommWindow;
import libomv.Gui.windows.MainControl;
import libomv.Gui.windows.MainWindow;
import libomv.GridClient;
import libomv.LoginManager.LoginProgressCallbackArgs;
import libomv.LoginManager.LoginStatus;
import libomv.core.RLVManager;
import libomv.utils.Callback;
import libomv.utils.Helpers;

/* 
 * This is a UI less intermediate controller that implements an ActionListener to serve as central
 * message dispatcher during a particular state. Currently offline and online state are distinguished
 */
public class StateController implements ActionListener
{
	public static final String cmdFriends = "friends";
	public static final String cmdGroups = "groups";
	public static final String cmdInventory = "inventory";
	public static final String cmdSearch = "search";
	public static final String cmdMaps = "maps";
	public static final String cmdObjects = "objects";
	public static final String cmdMedia = "media";
	public static final String cmdVoice = "voice";
	public static final String cmdDebugCon = "debugCon";

	private MainControl _Main;
	private GridClient _Client;
	private Browser _Browser;
	private ProgressPane _Progress;

	private CommWindow Comm;
	public RLVManager RLV;
	
	private JLabel jMiAmount;

	private Callback<BalanceCallbackArgs> balanceUpdateCallback = new BalanceUpdateCallback();

	public StateController(MainControl main)
	{
		_Main = main;
		_Client = _Main.getGridClient();

		actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, MainControl.cmdLogout));
	}

	@Override
	public void finalize() throws Throwable
	{
		dispose();
		super.finalize();
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		/* Handle local events */
		String action = e.getActionCommand();	
		if (action.equals(MainControl.cmdLogin))
		{
			_Client.Self.OnBalanceUpdated.add(balanceUpdateCallback);
			_Progress = new ProgressPane(_Main);
			_Main.setControlPane(_Progress);
			_Main.getJFrame().validate();
			_Progress.updateProgress(0, "Preparing to login...", "");
			try
			{
				_Client.Login.RequestLogin(_Client.Login.new LoginParams(_Client), new LoginProgressHandler());
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else if (e.getActionCommand().equals(MainControl.cmdCancel))
		{
			_Client.Login.AbortLogin();
			// Browser and offline menu should still be assigned
			_Main.setControlPane(new LoginPane(_Main, getBrowser()));
			_Main.getJFrame().validate();
		}
		else if (e.getActionCommand().equals(MainControl.cmdLogout))
		{
			_Main.setJMenuBar(getOfflineMenuBar());
			_Main.setContentPane(getBrowser().getComponent());
			_Main.setControlPane(new LoginPane(_Main, getBrowser()));
			_Main.getJFrame().validate();
		}
		else if (action.equals(cmdFriends) ||
			     action.equals(cmdGroups))
		{
			Comm.setFocus(e.getActionCommand(), null);
			Comm.setVisible(true);
		}
		else if (action.equals(cmdInventory))
		{
			
		}
		else
		{
			/* Pass to main window to be handled */
			_Main.actionPerformed(e);
		}
	}

	public void dispose()
	{
		_Main.setContentPane(null);
		_Main.setControlPane(null);

		Comm = null;
		RLV = null;
		
		/* Make sure to unregister any callbacks */
		_Client.Self.OnBalanceUpdated.remove(balanceUpdateCallback);
		
		/* Dispose of Browser instance if it exists to avoid crash in native code */
		if (_Browser != null)
		{
			_Browser.dispose();
			_Browser = null;
		}		
	}
	
	private JMenuBar getOfflineMenuBar()
	{
		JMenuBar jMbMain = new JMenuBar();

		JMenu file = new JMenu("File");
		
		JMenuItem jMiSettings = MainWindow.newMenuItem("Settings...", this, MainControl.cmdSettings);
		file.add(jMiSettings);
		
		file.addSeparator();

		JMenuItem jMiQuit = MainWindow.newMenuItem("Quit...", this, MainControl.cmdQuit);
		file.add(jMiQuit);

		jMbMain.add(file);
		
		JMenu help = new JMenu("Help");

		JMenuItem jMiBugReports = MainWindow.newMenuItem("Bugs/Feature Request...", this, MainControl.cmdBugs);
		help.add(jMiBugReports);

		JMenuItem jMiUpdates = MainWindow.newMenuItem("Check for Updates...", this, MainControl.cmdUpdates);
		help.add(jMiUpdates);

		help.addSeparator();
		
		JMenuItem jMiAbout = MainWindow.newMenuItem("About Libomv Client...", this, MainControl.cmdAbout);
		help.add(jMiAbout);

		jMbMain.add(help);
//		jMbMain.setHelpMenu(help); // needed for portability (Motif, etc.).
		return jMbMain;
	}

	private JMenuBar getOnlineMenuBar()
	{
		JMenuBar jMbMain = new JMenuBar();

		JMenu file = new JMenu("File");
		
		JMenuItem jMiFileOpen = MainWindow.newMenuItem("Open...", this, "open");
		file.add(jMiFileOpen);
		file.addSeparator();

		JMenuItem jMiSettings = MainWindow.newMenuItem("Settings...", this, MainControl.cmdSettings);
		file.add(jMiSettings);
		file.addSeparator();
		
		JMenuItem jMiFileQuit = MainWindow.newMenuItem("Quit", this, MainControl.cmdQuit);
		file.add(jMiFileQuit);

		jMbMain.add(file);

		JMenu world = new JMenu("World");
		JMenuItem jMiFriends = MainWindow.newMenuItem("Friends", this, cmdFriends);
		world.add(jMiFriends);

		JMenuItem jMiGroups = MainWindow.newMenuItem("Groups", this, cmdGroups);
		world.add(jMiGroups);
		
		JMenuItem jMiInventory = MainWindow.newMenuItem("Inventory", this, cmdInventory);
		world.add(jMiInventory);
		
		JMenuItem jMiSearch = MainWindow.newMenuItem("Search", this, cmdSearch);
		world.add(jMiSearch);
		
		JMenuItem jMiMap = MainWindow.newMenuItem("Map", this, cmdMaps);
		world.add(jMiMap);
		
		JMenuItem jMiObjects = MainWindow.newMenuItem("Objects", this, cmdObjects);
		world.add(jMiObjects);

		JMenuItem jMiMedia = MainWindow.newMenuItem("Media", this, cmdMedia);
		world.add(jMiMedia);

		JMenuItem jMiVoice = MainWindow.newMenuItem("Voice", this, cmdVoice);
		world.add(jMiVoice);

		jMbMain.add(world);

		JMenu help = new JMenu("Help");
		JMenuItem jMiBugReports = MainWindow.newMenuItem("Bugs/Feature Request...", this, MainControl.cmdBugs);
		help.add(jMiBugReports);

		JMenuItem jMiUpdates = MainWindow.newMenuItem("Check for Updates...", this, MainControl.cmdUpdates);
		help.add(jMiUpdates);

		JMenuItem jMiDebugConsole = MainWindow.newMenuItem("Debug Console...", this, cmdDebugCon);
		help.add(jMiDebugConsole);

		help.addSeparator();

		JMenuItem jMiAbout = MainWindow.newMenuItem("About Libomv Client...", this, MainControl.cmdAbout);
		help.add(jMiAbout);
		jMbMain.add(help);
		// jMbMain.setHelpMenu(help); // needed for portability (Motif, etc.).
		
		jMbMain.add(Box.createGlue());

		jMbMain.add(getJAmount());
		jMbMain.add(new JLabel(" "));
		return jMbMain;
	}

	private Browser getBrowser()
	{
		if (_Browser == null)
		{
	        _Browser = BrowserFactory.createBrowser(BrowserType.getCrossPlatformBrowser());		
		}
		return _Browser;
	}	

	private JLabel getJAmount()
	{
		if (jMiAmount == null)
		{
			_Main.getGridClient().Self.getBalance();
			jMiAmount = new JLabel(String.format("%s %s", _Main.getGridClient().getGrid(null).currencySym, _Main.getGridClient().Self.getBalance()));
		}
		return jMiAmount;
	}

	public class LoginProgressHandler implements Callback<LoginProgressCallbackArgs>
	{
		@Override
		public boolean callback(LoginProgressCallbackArgs e)
		{
			if (e.getStatus() == LoginStatus.ConnectingToLogin)
			{
				_Progress.updateProgress(10, "Logging in...", Helpers.EmptyString);
			}
			else if (e.getStatus() == LoginStatus.Redirecting)
			{
				// Server requested redirection
				_Progress.updateProgress(20, "Server requested redirection...", e.getReply().NextUrl);
			}
			else if (e.getStatus() == LoginStatus.ReadingResponse)
			{
				_Progress.updateProgress(30, "Reading response...", null);
			}
			else if (e.getStatus() == LoginStatus.ConnectingToSim)
			{
				_Progress.updateProgress(40, e.getMessage(), e.getReply().Message);
			}
			else if (e.getStatus() == LoginStatus.Failed)
			{
				_Progress.updateProgress(90, "Login failed...", e.getReason());
				actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, MainControl.cmdCancel));
				return true;
			}
			else if (e.getStatus() == LoginStatus.Success)
			{
				// Login was successful
				_Progress.updateProgress(50, "Authentification succeed...", e.getReply().Message);

				// Create the CommWindow as hidden window as it is also our communication manager
				Comm = new CommWindow(_Main);
				// Create the RLV manager
				RLV = new RLVManager(_Main);
				
				try
				{
					_Client.Self.RequestMuteList();
					_Client.Self.RetrieveInstantMessages();
//					_Client.Appearance.RequestSetAppearance();
				}
				catch (Exception ex)
				{
					
				}
				
				_Main.setJMenuBar(getOnlineMenuBar());
				_Main.setContentPane(null);
				_Main.setControlPane(new OnlinePane(_Main));
				_Main.getJFrame().validate();

				_Progress.updateProgress(100, "Finished", e.getReply().Message);
				return true;
			}
			return false;
		}
	}

	private class BalanceUpdateCallback implements Callback<BalanceCallbackArgs>
	{
		@Override
		public boolean callback(final BalanceCallbackArgs params)
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					String symbol = _Client.getGrid(null).currencySym;
					getJAmount().setText(String.format("%s %s", symbol == null || symbol.isEmpty() ? "$" : symbol, params.getBalance()));
					if (!params.getFirst() && params.getDelta() > 50)
					{
						if (params.getDelta() < 0)
						{
							/* Create money gone sound */
						}
						else
						{
							/* Create cash register sound */
						}
					}
				}
			});
			return false;
		}
	}
}