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
package libomv.core.state;

import java.awt.event.ActionEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserFactory;
import com.teamdev.jxbrowser.BrowserType;

import libomv.GridClient;
import libomv.LoginManager.LoginProgressCallbackArgs;
import libomv.LoginManager.LoginStatus;
import libomv.Gui.components.LoginPane;
import libomv.Gui.components.ProgressPane;
import libomv.Gui.windows.MainControl;
import libomv.Gui.windows.MainWindow;
import libomv.utils.Callback;

public class OfflineController implements StateController
{
	private MainControl _Main;
	private GridClient _Client;
	private Browser _Browser;
	private JMenuBar jMbMain;
	
	public OfflineController(MainControl main)
	{
		_Main = main;
		_Client = _Main.getGridClient();

		main.setJMenuBar(getJMenuBar());
		main.setContentPane(getJBrowser().getComponent());
		main.setControlPane(new LoginPane(_Main, getJBrowser()));
		main.getJFrame().validate();
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
		if (e.getActionCommand().equals(MainControl.cmdLogin))
		{
			try
			{
				_Main.setControlPane(new ProgressPane(_Main));
				_Client.Login.RequestLogin(_Client.Login.new LoginParams(_Client), new LoginProgressHandler());
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (e.getActionCommand().equals(MainControl.cmdCancel))
		{
			_Client.Login.AbortLogin();			
			_Main.setControlPane(new LoginPane(_Main, getJBrowser()));
		}
		else
		{
			_Main.actionPerformed(e);
		}
	}

	public void dispose()
	{
		_Main.setContentPane(null);
		_Main.setControlPane(null);
		if (_Browser != null)
		{
			_Browser.dispose();
			_Browser = null;
		}		
	}

	public class LoginProgressHandler implements Callback<LoginProgressCallbackArgs>
	{
		@Override
		public boolean callback(LoginProgressCallbackArgs e)
		{
			if (e.getStatus() == LoginStatus.Success)
			{
				// Login was successful
				System.out.println("JOMV: Message of the day: " + e.getMessage());
				_Main.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, MainControl.cmdLogin));
				return true;
			}
			else if (e.getStatus() == LoginStatus.Redirecting)
			{
				// Server requested redirection
				System.out.println("JOMV: Server requested redirection: " + e.getReason());
			}
			else if (e.getStatus() == LoginStatus.Failed)
			{
				System.out.println("JOMV: Error logging in: " + e.getReason() + " : " + e.getMessage());
				_Main.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, MainControl.cmdLogout));
				return true;
			}
			return false;
		}
	}

	private JMenuBar getJMenuBar()
	{
		if (jMbMain == null)
		{
			jMbMain = new JMenuBar();

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
//			jMbMain.setHelpMenu(help); // needed for portability (Motif, etc.).

			JPanel panel = new JPanel();
			jMbMain.add(panel);
		}
		return jMbMain;
	}
	
	private Browser getJBrowser()
	{
		if (_Browser == null)
		{
			BrowserType type = BrowserType.getCrossPlatformBrowser();
			String os = System.getProperty("os.name");
			String arch = System.getProperty("os.arch");
			if (os.contains("Windows") && (arch.indexOf("64") != -1))
				type = BrowserType.IE;
			_Browser = BrowserFactory.createBrowser(type);		
		}
		return _Browser;
	}	
}