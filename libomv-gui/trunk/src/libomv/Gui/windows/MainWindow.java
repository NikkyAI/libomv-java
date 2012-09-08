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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

import libomv.GridClient;
import libomv.Gui.components.LoginPanel;
import libomv.Gui.components.OnlinePanel;
import libomv.Gui.dialogs.AboutDialog;

public class MainWindow extends JFrame implements MainControl
{
	private static final long serialVersionUID = 1L;

	private JPanel jPSouth;
	private Component jPContent;
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
		setPreferredSize(new Dimension(640, 480));
		setMinimumSize(new Dimension(640, 480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initializeLoginPanel();
	}
	
	@Override
	public JFrame getMainJFrame()
	{
		return this;
	}
	
	public JMenuItem newMenuItem(String label, ActionListener actionListener, String actionCommand)
	{
		JMenuItem item = new JMenuItem(label);
		if (actionCommand != null)
		{
			item.setActionCommand(actionCommand);
		}
		item.addActionListener(actionListener);
		return item;
	}
	
	public void setMenuBar(JMenuBar menuBar)
	{
		this.setJMenuBar(menuBar);
	}

	public void setContentArea(Component component)
	{
		jPContent = component;
		getContentPane().add(component, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String action = e.getActionCommand();
		if (action.equals(MainControl.cmdAbout))
		{
			AboutDialog about = new AboutDialog(getMainJFrame());
			about.setVisible(true);			
		}
		else if (action.equals(MainControl.cmdSettings))
		{
			PreferenceWindow pref = new PreferenceWindow(getMainJFrame());
			pref.setVisible(true);			
		}
		else if (action.equals(MainControl.cmdLogout))
		{
			initializeLoginPanel();			
		}
		else if (action.equals(MainControl.cmdQuit))
		{
		      System.exit(0);			
		}
	}

	public void initializeLoginPanel()
	{
		if (jPSouth == null || !(jPSouth instanceof LoginPanel))
		{
			if (jPSouth != null)
				remove(jPSouth);
			if (jPContent != null)
				remove(jPContent);
			jPSouth = new LoginPanel(_Client, this);
			getContentPane().add(jPSouth, BorderLayout.SOUTH);
			validate();		
		}
	}

	public void initializeOnlinePanel()
	{
		if (jPSouth == null || !(jPSouth instanceof OnlinePanel))
		{
			if (jPSouth != null)
				remove(jPSouth);
			if (jPContent != null)
				remove(jPContent);
			jPSouth = new OnlinePanel(_Client, this);
			getContentPane().add(jPSouth, BorderLayout.SOUTH);
			validate();
		}
	}	
}
