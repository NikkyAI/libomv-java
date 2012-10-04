/**
 * Copyright (c) 2009-2011, Frederick Martian
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
package libomv.Gui.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

import libomv.GridClient;
import libomv.Gui.AppSettings;
import libomv.Gui.components.LoginPanel;
import libomv.Gui.components.OnlinePanel;
import libomv.Gui.components.list.FriendList;
import libomv.Gui.components.list.GroupList;
import libomv.Gui.dialogs.AboutDialog;
import libomv.utils.Settings;

public class MainWindow extends JFrame implements MainControl
{
	private static final long serialVersionUID = 1L;

	private JPanel jPSouth;
	private Component jPContent;
	private JMenuBar jMenuBar;
	private GridClient _Client;
	private AppSettings _Settings;

	/**
	 * This is the default constructor
	 */
	public MainWindow(GridClient client)
	{
		super();
/*
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ex)
		{
			System.out.println("Error setting native LAF: " + ex);
		}
 */
		
		_Client = client;
		_Settings = new AppSettings(client);
		
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
	
	@Override
	public GridClient getGridClient()
	{
		return _Client;
	}
	
	@Override
	public Settings getAppSettings()
	{
		// TODO Auto-generated method stub
		return _Settings;
	}	

	public CommWindow getCommWindow()
	{
		if (jPSouth != null && jPSouth instanceof OnlinePanel)
		{
			return ((OnlinePanel)jPSouth).getCommWindow();
		}
		return null;
	}

	public FriendList getFriendList()
	{
		if (jPSouth != null && jPSouth instanceof OnlinePanel)
		{
			return ((OnlinePanel)jPSouth).getFriendList();
		}
		return null;
	}
	
	public GroupList getGroupList()
	{
		if (jPSouth != null && jPSouth instanceof OnlinePanel)
		{
			return ((OnlinePanel)jPSouth).getGroupList();
		}
		return null;
	}

	public void setAction(AbstractButton comp, ActionListener actionListener, String actionCommand)
	{
		if (actionCommand != null)
		{
			comp.setActionCommand(actionCommand);
		}
		comp.addActionListener(actionListener);
	}

	public void setAction(JComboBox comp, ActionListener actionListener, String actionCommand)
	{
		if (actionCommand != null)
		{
			comp.setActionCommand(actionCommand);
		}
		comp.addActionListener(actionListener);
	}

	public JMenuItem newMenuItem(String label, ActionListener actionListener, String actionCommand)
	{
		JMenuItem item = new JMenuItem(label);
		setAction(item, actionListener, actionCommand);
		return item;
	}

	public void setMenuBar(JMenuBar menuBar)
	{
		jMenuBar = menuBar;
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
			PreferenceWindow pref = new PreferenceWindow(this);
			pref.setVisible(true);			
		}
		else if (action.equals(MainControl.cmdOnline))
		{
			initializeOnlinePanel();			
		}
		else if (action.equals(MainControl.cmdLogout))
		{
			initializeLoginPanel();			
		}
		else if (action.equals(MainControl.cmdQuit))
		{
		    try
		    {
				_Client.Network.Logout();
			}
		    catch (Exception e1)
			{
				e1.printStackTrace();
			}
			System.exit(0);			
		}
	}

	public void initializeLoginPanel()
	{
		if (jPSouth == null || !(jPSouth instanceof LoginPanel))
		{
			if (jMenuBar != null)
				remove(jMenuBar);
			if (jPSouth != null)
				remove(jPSouth);
			if (jPContent != null)
				remove(jPContent);
			jPSouth = new LoginPanel(this);
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
			jPSouth = new OnlinePanel(this);
			getContentPane().add(jPSouth, BorderLayout.SOUTH);
			validate();
		}
	}
}
