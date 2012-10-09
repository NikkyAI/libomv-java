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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import libomv.AgentManager.BalanceCallbackArgs;
import libomv.Gui.components.OnlinePane;
import libomv.Gui.windows.CommWindow;
import libomv.Gui.windows.MainControl;
import libomv.Gui.windows.MainWindow;
import libomv.utils.Callback;

public class OnlineController implements StateController, ActionListener
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
	private JMenuBar jMbMain;
	private JLabel jMiAmount;
	private CommWindow _Comm;
	
	private Callback<BalanceCallbackArgs> balanceUpdateCallback = new BalanceUpdateCallback();

	public OnlineController(MainControl main)
	{
		_Main = main;
		_Main.getGridClient().Self.OnBalanceUpdated.add(balanceUpdateCallback);

		// Create the CommWindow right away as hidden window as it is also our communication manager
		_Comm = new CommWindow(_Main);

		main.setJMenuBar(getJMenuBar());
//		main.setContentPane(new SceneViewer());
		main.setControlPane(new OnlinePane(_Main));
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
		/* Handle local events */
		if (e.getActionCommand().equals(cmdFriends) ||
			e.getActionCommand().equals(cmdGroups))
		{
			_Comm.setFocus(e.getActionCommand(), null);
			_Comm.setVisible(true);
		}
		else if (e.getActionCommand().equals(cmdInventory))
		{
			
		}
		/* Pass to main window to be handled */
		_Main.actionPerformed(e);
	}

	public void dispose()
	{
		_Main.getGridClient().Self.OnBalanceUpdated.remove(balanceUpdateCallback);
		_Comm.dispose();
	}
	
	private JMenuBar getJMenuBar()
	{
		if (jMbMain == null)
		{
			jMbMain = new JMenuBar();

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
		}
		return jMbMain;
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

	private class BalanceUpdateCallback implements Callback<BalanceCallbackArgs>
	{
		@Override
		public boolean callback(final BalanceCallbackArgs params)
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					String symbol = _Main.getGridClient().getGrid(null).currencySym;
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