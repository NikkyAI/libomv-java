/**
 * Copyright (c) 2009-2014, Frederick Martian
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import libomv.GridClient;
import libomv.Gui.AppSettings;
import libomv.Gui.StateController;
import libomv.Gui.dialogs.AboutDialog;
import libomv.utils.Logger;
import libomv.utils.Settings;
import libomv.utils.Logger.LogLevel;

public class MainWindow extends JFrame implements MainControl
{
	private static final long serialVersionUID = 1L;

	private JPanel jPSouth;
	private Component jPContent;
	private JMenuBar jMenuBar;

	private GridClient _Client;
	private AppSettings _Settings;
	private StateController _State;

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
        addWindowListener(new WindowAdapter()
        {
			@Override
            public void windowClosing(WindowEvent e)
            {
               	actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, cmdQuit));
            }
        });
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		_State = new StateController(this);
	}
	
	protected void finalize() throws Throwable
	{
		_State.dispose();
		_State = null;
		super.finalize();
	}
	
	public JFrame getJFrame()
	{
		return this;
	}
	
	public GridClient getGridClient()
	{
		return _Client;
	}
	
	public Settings getAppSettings()
	{
		return _Settings;
	}
	
	public StateController getStateControl()
	{
		return _State;
	}
	
	/* Convinience methods */
	static public void setAction(AbstractButton comp, ActionListener actionListener, String actionCommand)
	{
		if (actionCommand != null)
		{
			comp.setActionCommand(actionCommand);
		}
		comp.addActionListener(actionListener);
	}

	static public void setAction(JTextField comp, ActionListener actionListener, String actionCommand)
	{
		if (actionCommand != null)
		{
			comp.setActionCommand(actionCommand);
		}
		comp.addActionListener(actionListener);
	}

	static public void setAction(JComboBox<?> comp, ActionListener actionListener, String actionCommand)
	{
		if (actionCommand != null)
		{
			comp.setActionCommand(actionCommand);
		}
		comp.addActionListener(actionListener);
	}

	static public JMenuItem newMenuItem(String label, ActionListener actionListener, String actionCommand)
	{
		JMenuItem item = new JMenuItem(label);
		setAction(item, actionListener, actionCommand);
		return item;
	}

	public void setJMenuBar(JMenuBar menuBar)
	{
		if (jMenuBar != null)
		{
			remove(jMenuBar);
		}
		if (menuBar != null)
		{
			super.setJMenuBar(menuBar);
		}
		jMenuBar = menuBar;
	}

	public void setContentPane(Component component)
	{
		if (jPContent != null)
		{
			getContentPane().remove(jPContent);
		}
		if (component != null)
		{
			getContentPane().add(component, BorderLayout.CENTER);
		}
		jPContent = component;
	}

	public void setControlPane(JPanel panel)
	{
		if (jPSouth != null)
		{
			getContentPane().remove(jPSouth);			
		}
		if (panel != null)
		{
			getContentPane().add(panel, BorderLayout.SOUTH);
		}
		jPSouth = panel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		String action = e.getActionCommand();
		if (action.equals(MainControl.cmdAbout))
		{
			AboutDialog about = new AboutDialog(this);
			about.setVisible(true);			
		}
		else if (action.equals(MainControl.cmdSettings))
		{
			PreferenceWindow pref = new PreferenceWindow(this);
			pref.setVisible(true);			
		}
		else if (action.equals(MainControl.cmdQuit))
		{
			if (_Client.Network.getConnected())
			{
                int confirm = JOptionPane.showOptionDialog(MainWindow.this,
                        "Are You sure you want to Logout from the network and close this Application?",
                        "Quit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == JOptionPane.YES_OPTION)
                {
         		    try
        		    {
        				_Client.Network.Logout();
        			}
        		    catch (Exception ex)
        			{
						Logger.Log("Response to teleportation invite failed", LogLevel.Error, _Client, ex);
        			}
                }
                else
                {
                	return;
                }
			}
			_State.dispose();
			System.exit(0);			
		}
	}
}
