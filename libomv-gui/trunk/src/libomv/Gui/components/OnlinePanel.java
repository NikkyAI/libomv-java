/**
 * Copyright (c) 2010-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
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
package libomv.Gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import libomv.AgentManager.BalanceCallbackArgs;
import libomv.GridClient;
import libomv.Gui.windows.MainControl;
import libomv.utils.Callback;

public class OnlinePanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private static final String cmdFriends = "friends";
	private static final String cmdGroups = "groups";
	private static final String cmdInventory = "inventory";
	private static final String cmdSearch = "search";
	private static final String cmdMaps = "maps";
	private static final String cmdObjects = "objects";
	private static final String cmdMedia = "media";
	private static final String cmdVoice = "voice";
	
	private GridClient _Client;
	private MainControl _Main;
	
	private JMenuBar jMbMain;
	private JLabel jMiAmount;
	private JPanel jSceneViewer;

	public OnlinePanel(GridClient client, MainControl main)
	{
		_Client = client;
		_Main = main;

		main.setContentArea(getSceneViewer());
		main.setMenuBar(getJMBar());

		initializePanel();	
	}
	
	/**
	 * This method initializes mainJMenuBar
	 * 
	 * @return JMenuBar
	 */
	private JMenuBar getJMBar()
	{
		if (jMbMain == null)
		{
			jMbMain = new JMenuBar();

			JMenu file = new JMenu("File");
			
			JMenuItem jMiFileOpen = _Main.newMenuItem("Open...", this, "open");
			file.add(jMiFileOpen);
			file.addSeparator();

			JMenuItem jMiSettings = _Main.newMenuItem("Settings...", this, MainControl.cmdSettings);
			file.add(jMiSettings);
			file.addSeparator();
			
			JMenuItem jMiFileQuit = _Main.newMenuItem("Quit", this, MainControl.cmdQuit);
			file.add(jMiFileQuit);

			jMbMain.add(file);

			JMenu world = new JMenu("World");
			JMenuItem jMiFriends = _Main.newMenuItem("Friends", this, cmdFriends);
			world.add(jMiFriends);

			JMenuItem jMiGroups = _Main.newMenuItem("Groups", this, cmdGroups);
			world.add(jMiGroups);
			
			JMenuItem jMiInventory = _Main.newMenuItem("Inventory", this, cmdInventory);
			world.add(jMiInventory);
			
			JMenuItem jMiSearch = _Main.newMenuItem("Search", this, cmdSearch);
			world.add(jMiSearch);
			
			JMenuItem jMiMap = _Main.newMenuItem("Map", this, cmdMaps);
			world.add(jMiMap);
			
			JMenuItem jMiObjects = _Main.newMenuItem("Objects", this, cmdObjects);
			world.add(jMiObjects);

			JMenuItem jMiMedia = _Main.newMenuItem("Media", this, cmdMedia);
			world.add(jMiMedia);

			JMenuItem jMiVoice = _Main.newMenuItem("Voice", this, cmdVoice);
			world.add(jMiVoice);

			jMbMain.add(world);

			JMenu help = new JMenu("Help");
			JMenuItem jMiBugReports = _Main.newMenuItem("Bugs/Feature Request...", this, MainControl.cmdBugs);
			help.add(jMiBugReports);

			JMenuItem jMiUpdates = _Main.newMenuItem("Check for Updates...", this, MainControl.cmdUpdates);
			help.add(jMiUpdates);

			JMenuItem jMiDebugConsole = _Main.newMenuItem("Debug Console...", this, MainControl.cmdDebugCon);
			help.add(jMiDebugConsole);

			help.addSeparator();

			JMenuItem jMiAbout = _Main.newMenuItem("About Libomv Client...", this, MainControl.cmdAbout);
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
			_Client.Self.getBalance();
			jMiAmount = new JLabel(String.format("%s %s", _Client.getGrid(null).currencySym, _Client.Self.getBalance()));
		}
		return jMiAmount;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		/* Handle local events */
		
		/* Pass to main window to be handled */
		_Main.actionPerformed(e);
	}

	private JPanel getSceneViewer()
	{
		if (jSceneViewer == null)
		{
			jSceneViewer = new JPanel();
		}
		return jSceneViewer;
	}

	private void initializePanel()
	{
		_Client.Self.OnBalanceUpdated.add(new BalanceUpdate());
	}
	
	private class BalanceUpdate implements Callback<BalanceCallbackArgs>
	{
		@Override
		public boolean callback(BalanceCallbackArgs params)
		{
			getJAmount().setText(String.format("%s %s", _Client.getGrid(null).currencySym, params.getBalance()));
			return false;
		}
		
	}
}
