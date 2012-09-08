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

import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import libomv.GridClient;
import libomv.Gui.windows.MainControl;

public class OnlinePanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private GridClient _Client;
	private MainControl _Main;
	
	private JMenuBar jMbMain;
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

			JMenu pref = new JMenu("File");
			
			JMenuItem jMiFileOpen = _Main.newMenuItem("Open...", this, "open");
			pref.add(jMiFileOpen);
			pref.addSeparator();

			JMenuItem jMiSettings = _Main.newMenuItem("Settings...", this, MainControl.cmdSettings);
			pref.add(jMiSettings);
			jMbMain.add(pref);
			
			JMenu mnNewMenu = new JMenu("New menu");
			jMbMain.add(mnNewMenu);

			JMenu help = new JMenu("Help");
			JMenuItem jMiAbout = _Main.newMenuItem("About Libomv Client...", this, MainControl.cmdAbout);
			help
			.add(jMiAbout);
			jMbMain.add(help);
			jMbMain.setHelpMenu(help); // needed for portability (Motif, etc.).
			
			JLabel filler = new JLabel("");
			jMbMain.add(filler);

			JLabel amount = new JLabel("L$ 2000");
            amount.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			jMbMain.add(amount);

			JPanel panel = new JPanel();
			jMbMain.add(panel);
		}
		return jMbMain;
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

	private void doReturn(boolean logout)
	{
		ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, logout ? "logout" : "failed", 0);
		_Main.actionPerformed(e);
	}

	private void initializePanel()
	{

	}
}
