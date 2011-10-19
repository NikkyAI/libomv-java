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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import libomv.GridClient;
import libomv.GridClient.GridInfo;
import libomv.Gui.components.InfoPanel;

public class MainWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane;

	private JPanel jLoginPanel;
	private JLabel jLblFirstName;
	private JTextField jTxtFirstName;
	private JLabel jLblLastName;
	private JTextField jTxtLastName;
	private JLabel jLblPassword;
	private JPasswordField jPwdPassword;
	private JButton jBtnLogin;
	private JComboBox jcbGridSelector;
	private JComboBox jcbLocation;
	private JButton jBtnGrids;
	private JCheckBox jChkSavePassword;
	private JCheckBox jChkSaveDetails;

	private JPanel jInfoPanel;

	private JMenuBar jMbMain;
	
	GridClient _Client;

	/**
	 * This is the default constructor
	 */
	public MainWindow(GridClient client)
	{
		super();
		
		_Client = client;
		
		setSize(800, 640);
		setJMenuBar(getJMbMain());
		setPreferredSize(new Dimension(640, 480));
		setMinimumSize(new Dimension(640, 480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(getJContentPane());
		setTitle("Libomv-Java Client");
	}

	private JFrame getJMainFrame()
	{
		return this;
	}
	
	/**
	 * This method initializes jContentPane
	 * 
	 * @return the content pane JPanel
	 */
	private JPanel getJContentPane()
	{
		if (jContentPane == null)
		{
			jContentPane = (JPanel)getContentPane();
			BorderLayout bl_jContentPane = new BorderLayout();
			bl_jContentPane.setHgap(10);
			jContentPane.setLayout(bl_jContentPane);

			jContentPane.add(getJLoginPanel(), BorderLayout.SOUTH);
			jContentPane.add(getJInfoPanel(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jLoginInformation
	 * 
	 * @return Login JPanel
	 */
	private JPanel getJLoginPanel()
	{
		if (jLoginPanel == null)
		{
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[] { 1, 4, 6, 4, 6, 4, 6, 1, 0 };
			gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
			gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 2.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0 };
			gridBagLayout.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };

			jLoginPanel = new JPanel();
			jLoginPanel.setLayout(gridBagLayout);

			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 0;
			jLblFirstName = new JLabel("First Name:");
			jLoginPanel.add(jLblFirstName, gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.gridx = 2;
			gridBagConstraints.gridy = 0;
			jLoginPanel.add(getJTxtFirstName(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.gridx = 3;
			gridBagConstraints.gridy = 0;
			jLblLastName = new JLabel("Last Name:");
			jLoginPanel.add(jLblLastName, gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.gridx = 4;
			gridBagConstraints.gridy = 0;
			jLoginPanel.add(getJTxtLastName(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.gridx = 5;
			gridBagConstraints.gridy = 0;
			jLblPassword = new JLabel("Password:");
			jLoginPanel.add(jLblPassword, gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.gridx = 6;
			gridBagConstraints.gridy = 0;
			jLoginPanel.add(getJPwdPassword(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.gridx = 7;
			gridBagConstraints.gridy = 0;
			jLoginPanel.add(getJbtnLogin(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridx = 8;
			gridBagConstraints.gridy = 0;
			jLoginPanel.add(new JPanel(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 1;
			jLoginPanel.add(new JLabel("Grid:"), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridx = 2;
			gridBagConstraints.gridy = 1;
			jLoginPanel.add(getJcbGridSelector(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.gridx = 3;
			gridBagConstraints.gridy = 1;
			jLoginPanel.add(getBtnGrids(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.gridx = 4;
			gridBagConstraints.gridy = 1;
			jLoginPanel.add(getChckbxSaveDetails(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.gridx = 6;
			gridBagConstraints.gridy = 1;
			jLoginPanel.add(getChckbxSavePassword(), gridBagConstraints);
			
			initializeLoginPanel(_Client.getDefaultGrid());
		}
		return jLoginPanel;
	}

	/**
	 * This method initializes textFirstName
	 * 
	 * @return JTextField
	 */
	private JTextField getJTxtFirstName()
	{
		if (jTxtFirstName == null)
		{
			jTxtFirstName = new JTextField(20);
		}
		return jTxtFirstName;
	}

	/**
	 * This method initializes textLastName
	 * 
	 * @return JTextField
	 */
	private JTextField getJTxtLastName()
	{
		if (jTxtLastName == null)
		{
			jTxtLastName = new JTextField(20);
		}
		return jTxtLastName;
	}

	/**
	 * This method initializes textPassword
	 * 
	 * @return java.awt.TextField
	 */
	private JPasswordField getJPwdPassword()
	{
		if (jPwdPassword == null)
		{
			jPwdPassword = new JPasswordField(20);
		}
		return jPwdPassword;
	}

	/**
	 * This method initializes our Login button
	 * 
	 * @return Login JButton
	 */
	private JButton getJbtnLogin()
	{
		if (jBtnLogin == null)
		{
			jBtnLogin = new JButton();
			jBtnLogin.setText("Login");
			jBtnLogin.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					String string = (String) getJcbGridSelector().getSelectedItem();
					_Client.setDefaultGrid(string);
					GridInfo grid = _Client.getDefaultGrid();

					grid.saveSettings = getChckbxSaveDetails().isSelected();
					grid.savePassword = grid.saveSettings && getChckbxSavePassword().isSelected();
					grid.startLocation = (String) getJcbStartLocation().getSelectedItem();

					string = getJTxtFirstName().getText(); 
					if (string != null)
						grid.firstname = string;
					string = getJTxtLastName().getText();
					if (string != null)
						grid.lastname = string;
					string = getJPwdPassword().getPassword().toString();
					if (string != null)
						grid.setPassword(string);
					
					try
					{
						_Client.Login.Login(_Client.Login.new LoginParams(_Client));
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
		return jBtnLogin;
	}

	private JComboBox getJcbGridSelector()
	{
		if (jcbGridSelector == null)
		{
			jcbGridSelector = new JComboBox();
			Set<String> set = _Client.getGridNames();
			for (String grid : set)
			{
				jcbGridSelector.addItem(grid);
			}
			jcbGridSelector.setSelectedItem(_Client.getDefaultGrid().gridnick);
			jcbGridSelector.addActionListener(new GridSelectionListener());
		}
		return jcbGridSelector;
	}

	private JComboBox getJcbStartLocation()
	{
		if (jcbLocation == null)
		{
			jcbLocation = new JComboBox();
			jcbLocation.setEditable(true);
			jcbLocation.addItem("Last");
			jcbLocation.addItem("Home");
			String start = _Client.getDefaultGrid().startLocation.toLowerCase();
			if (start == null || start.isEmpty() || start.equalsIgnoreCase("last"))
			{
				jcbLocation.setSelectedIndex(0);
			}
			else if (start.equalsIgnoreCase("home"))
			{
				jcbLocation.setSelectedIndex(1);
			}
			else
			{
				jcbLocation.addItem(start);
				jcbLocation.setSelectedItem(start);
			}

		}
		return jcbLocation;
	}
	private class GridSelectionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent evt)
		{
			String selection = (String)((JComboBox)evt.getSource()).getSelectedItem();
			initializeLoginPanel(_Client.getGrid(selection));
		}
	}
	
	/**
	 * This method initializes our Grid button
	 * 
	 * @return Grid JButton
	 */
	private JButton getBtnGrids()
	{
		if (jBtnGrids == null)
		{
			jBtnGrids = new JButton("Grids");
			jBtnGrids.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					// TODO Open the Grid Selection overview panel

				}
			});
		}
		return jBtnGrids;
	}

	private JCheckBox getChckbxSaveDetails()
	{
		if (jChkSaveDetails == null)
		{
			jChkSaveDetails = new JCheckBox("Save Details");
			jChkSaveDetails.setHorizontalAlignment(SwingConstants.CENTER);
			
			jChkSaveDetails.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent evt)
				{
					JCheckBox cb = (JCheckBox)evt.getSource();
					getChckbxSavePassword().setEnabled(cb.isSelected());
					if (!cb.isSelected())
					{
						getChckbxSavePassword().setSelected(false);
					}
				}	
			});
		}
		return jChkSaveDetails;
	}

	private JCheckBox getChckbxSavePassword()
	{
		if (jChkSavePassword == null)
		{
			jChkSavePassword = new JCheckBox("Save Password");
		}
		return jChkSavePassword;
	}

	private JPanel getJInfoPanel()
	{
		if (jInfoPanel == null)
		{
			jInfoPanel = new InfoPanel(_Client);
		}
		return jInfoPanel;
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
					AboutWindow about = new AboutWindow(getJMainFrame());
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
	
	private void initializeLoginPanel(GridInfo grid)
	{
		if (grid.startLocation != null)
		{
			getJcbStartLocation().setSelectedItem(grid.startLocation);
		}
		if (grid.firstname != null)
			getJTxtFirstName().setText(grid.firstname);
		if (grid.lastname != null)
			getJTxtLastName().setText(grid.lastname);
		if (grid.getPassword() != null)
			getJPwdPassword().setText(grid.getPassword());
		getChckbxSaveDetails().setSelected(grid.saveSettings);
		getChckbxSavePassword().setSelected(grid.saveSettings && grid.savePassword);				
		getChckbxSavePassword().setEnabled(grid.saveSettings);				
	}
}
