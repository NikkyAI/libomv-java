/**
 * Copyright (c) 2009-2011, Frederick Martian
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
package libomv.Gui.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

import libomv.GridClient;
import libomv.Gui.components.LoginPanel;
import libomv.Gui.components.OnlinePanel;
import libomv.Gui.dialogs.AboutDialog;

public class MainWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	private JMenuBar jMbMain;
	private JPanel jPSouth;
	GridClient _Client;

	/**
	 * This is the default constructor
	 */
	public MainWindow(GridClient client)
	{
		super();
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			System.out.println("Error setting native LAF: " + e);
		}
		
		_Client = client;
		
		setTitle("Libomv-Java Client");
		setSize(1024, 800);
		setJMenuBar(getJMbMain());
		setPreferredSize(new Dimension(640, 480));
		setMinimumSize(new Dimension(640, 480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initializeLoginPanel();
	}
	
	
	private class PanelActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String command = e.getActionCommand();
			if (command != null && command.equals("success"))
			{
				// Create the online panel and display it
				initializeOnlinePanel();
			}
			else if (command != null && command.equals("logout"))
			{
				// Create the online panel and display it
				initializeLoginPanel();
			}
		}
	}

	public void initializeLoginPanel()
	{
		if (jPSouth != null)
			remove(jPSouth);
		jPSouth = new LoginPanel(_Client, this, new PanelActionListener());
		add(jPSouth, BorderLayout.SOUTH);
		validate();		
	}

	public void initializeOnlinePanel()
	{
		if (jPSouth != null)
			remove(jPSouth);
		jPSouth = new OnlinePanel(_Client, this, new PanelActionListener());
		add(jPSouth, BorderLayout.SOUTH);
		validate();		
	}
	
	private JFrame getJMainFrame()
	{
		return this;
	}
	
	/**
	 * This method initializes mainJMenuBar
	 * 
	 * @return JMenuBar
	 */
	private JMenuBar getJMbMain()
	{
		if (jMbMain == null)
		{
			JMenuItem jMiSettings = new JMenuItem("Settings...");
			jMiSettings.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					PreferenceWindow pref = new PreferenceWindow(getJMainFrame());
					pref.setVisible(true);
				}
			});

			JMenuItem jMiAbout = new JMenuItem("About Libomv Client...");
			jMiAbout.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					AboutDialog about = new AboutDialog(getJMainFrame());
					about.setVisible(true);
				}
			});

			jMbMain = new JMenuBar();

			JMenu pref = new JMenu("Preferences");
			pref.add(jMiSettings);
			jMbMain.add(pref);

			JMenu help = new JMenu("Help");
			help.add(jMiAbout);
			jMbMain.add(help);
		}
		return jMbMain;
	}
}
