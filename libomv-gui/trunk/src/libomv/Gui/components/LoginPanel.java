/**
 * Copyright (c) 2010-2011, Frederick Martian
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
package libomv.Gui.components;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserFactory;
import com.teamdev.jxbrowser.BrowserType;

import libomv.GridClient;
import libomv.GridClient.GridInfo;
import libomv.LoginManager.LoginProgressCallbackArgs;
import libomv.LoginManager.LoginStatus;
import libomv.Gui.dialogs.GridEditor;
import libomv.Gui.windows.MainControl;
import libomv.utils.Callback;

public class LoginPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private static final String cmdLogin = "login";
	private static final String cmdGrids = "grids";
	private static final String cmdGrid = "grid";
	private static final String cmdSaveDetails = "saveDetails";

	private Browser browser;

	private MainControl _Main;
	
	private JMenuBar jMbMain;
	private JLabel jLblUserName;
	private JTextField jTxtUserName;
	private JLabel jLblPassword;
	private JPasswordField jPwdPassword;
	private JButton jBtnLogin;
	private JLabel jLblGridSelector;
	private JComboBox jcbGridSelector;
	private JLabel jLblLocation;
	private JComboBox jcbLocation;
	private JButton jBtnGrids;
	private JCheckBox jChkSavePassword;
	private JCheckBox jChkSaveDetails;
	
	public LoginPanel(MainControl main)
	{
		_Main = main;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 1, 4, 6, 4, 6, 4, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 2.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };

		setLayout(gridBagLayout);
		// Create a border around the edge
		setBorder(BorderFactory.createLineBorder(Color.BLACK));

		GridBagConstraints gridBagConstraints;
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		add(getJLblUserName(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		add(getJTxtUserName(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		add(getJLblPassword(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 0;
		add(getJPwdPassword(), gridBagConstraints);
				
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(0, 0, 5, 5);
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 0;
		add(getChckbxSavePassword(), gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.gridx = 7;
		gridBagConstraints.gridy = 0;
		add(getJBtnLogin(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 5, 5, 5);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		add(getJLblGridSelector(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 0, 5, 5);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		add(getJcbGridSelector(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(0, 0, 5, 5);
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		add(getBtnGrids(), gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(0, 0, 5, 5);
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 1;
		add(getChckbxSaveDetails(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 5, 0, 5);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		add(getJLblLocation(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 0, 0, 5);
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 2;
		add(getJcbStartLocation(), gridBagConstraints);
		
		main.setContentArea(getJBrowser().getComponent());
		main.setMenuBar(getJMBar());

		initializePanel(_Main.getGridClient().getDefaultGrid());	
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
			
			JMenuItem jMiSettings = _Main.newMenuItem("Settings...", this, MainControl.cmdSettings);
			file.add(jMiSettings);
			
			file.addSeparator();

			JMenuItem jMiQuit = _Main.newMenuItem("Quit...", this, MainControl.cmdQuit);
			file.add(jMiQuit);

			jMbMain.add(file);
			
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
//			jMbMain.setHelpMenu(help); // needed for portability (Motif, etc.).

			JPanel panel = new JPanel();
			jMbMain.add(panel);
		}
		return jMbMain;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		/* Handle local events */
		if (e.getActionCommand().equals(cmdLogin))
		{
			doLogin();
		}
		else if (e.getActionCommand().equals(cmdGrid))
		{
			initializePanel((GridInfo)((JComboBox)e.getSource()).getSelectedItem());
		}
		else if (e.getActionCommand().equals(cmdGrids))
		{
			GridEditor gridEdit = new GridEditor(_Main.getGridClient(), _Main.getMainJFrame(), "Grid List", true);
			gridEdit.setVisible(true);
		}
		else if (e.getActionCommand().equals(cmdSaveDetails))
		{
			JCheckBox cb = (JCheckBox)e.getSource();
			getChckbxSavePassword().setEnabled(cb.isSelected());
			if (!cb.isSelected())
			{
				getChckbxSavePassword().setSelected(false);
			}
		}	
		else
		{
			/* Pass to main window to be handled */
			_Main.actionPerformed(e);
		}
	}

	private JLabel getJLblUserName()
	{
		if (jLblUserName == null)
		{
			jLblUserName = new JLabel("User Name:");
		}
		return jLblUserName;
	}

	private JLabel getJLblPassword()
	{
		if (jLblPassword == null)
		{
			jLblPassword = new JLabel("Password:");			
		}
		return jLblPassword;
	}
	
	private JLabel getJLblGridSelector()
	{
		if (jLblGridSelector == null)
		{
			jLblGridSelector = new JLabel("Grid:");
		}
		return jLblGridSelector;
	}
	private JLabel getJLblLocation()
	{
		if (jLblLocation == null)
		{
			jLblLocation = new JLabel("Location:");
		}
		return jLblLocation;
	}
	
	/**
	 * This method initializes textFirstName
	 * 
	 * @return JTextField
	 */
	private JTextField getJTxtUserName()
	{
		if (jTxtUserName == null)
		{
			jTxtUserName = new JTextField(20);
			// Add a caret listener
			jTxtUserName.addCaretListener(new CaretListener()
			{
				/**
				 * Called when the caret is updated
				 * 
				 * @param e
				 *            The CaretEvent
				 */
				@Override
				public void caretUpdate(CaretEvent e)
				{
					// Validate
					validateSettings();
				}
			});

			// Add a focus listener
			jTxtUserName.addFocusListener(new FocusAdapter()
			{
				/**
				 * Called when focus is gained
				 * 
				 * @param e
				 *            The FocusEvent
				 */
				@Override
				public void focusGained(FocusEvent e)
				{
					// Select all
					getJTxtUserName().selectAll();
				}
			});

			// Add a key listener
			jTxtUserName.addKeyListener(new KeyAdapter()
			{
				/**
				 * Called when a key is pressed
				 * 
				 * @param e
				 *            The KeyEvent
				 */
				@Override
				public void keyPressed(KeyEvent e)
				{
					if (e.getKeyCode() == KeyEvent.VK_ENTER)
					{
						getJPwdPassword().requestFocus();
					}
				}
			});
		}
		return jTxtUserName;
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
			// Add a caret listener
			jPwdPassword.addCaretListener(new CaretListener()
			{
				/**
				 * Called when the caret is updated
				 * 
				 * @param e
				 *            The CaretEvent
				 */
				@Override
				public void caretUpdate(CaretEvent e)
				{
					// Validate input
					validateSettings();
				}
			});

			// Add a focus listener
			jPwdPassword.addFocusListener(new FocusAdapter()
			{
				/**
				 * Called when focus is gained
				 * 
				 * @param e
				 *            The FocusEvent
				 */
				@Override
				public void focusGained(FocusEvent e)
				{
					// Select all
					getJPwdPassword().selectAll();
				}
			});

			// Add a key listener
			jPwdPassword.addKeyListener(new KeyAdapter()
			{
				/**
				 * Called when a key is pressed
				 * 
				 * @param e
				 *            The KeyEvent
				 */
				@Override
				public void keyPressed(KeyEvent e)
				{
					if (e.getKeyCode() == KeyEvent.VK_ENTER)
					{
						doLogin();
					}
				}
			});
		}
		return jPwdPassword;
	}

	/**
	 * This method initializes our Login button
	 * 
	 * @return Login JButton
	 */
	private JButton getJBtnLogin()
	{
		if (jBtnLogin == null)
		{
			jBtnLogin = new JButton();
			jBtnLogin.setText("Login");
			_Main.setAction(jBtnLogin, this, cmdLogin);
		}
		return jBtnLogin;
	}

	private JComboBox getJcbGridSelector()
	{
		if (jcbGridSelector == null)
		{
			jcbGridSelector = new JComboBox(_Main.getGridClient().getGridInfos());
			jcbGridSelector.setSelectedItem(_Main.getGridClient().getDefaultGrid());
			_Main.setAction(jcbGridSelector, this, cmdGrid);
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
			
			String start = _Main.getGridClient().getDefaultGrid().startLocation;
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
				jcbLocation.setSelectedItem(start);
			}

		}
		return jcbLocation;
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
			_Main.setAction(jBtnGrids, this, cmdGrids);
		}
		return jBtnGrids;
	}

	private JCheckBox getChckbxSaveDetails()
	{
		if (jChkSaveDetails == null)
		{
			jChkSaveDetails = new JCheckBox("Save Details");
			_Main.setAction(jChkSaveDetails, this, cmdSaveDetails);
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

	public class LoginProgressHandler implements Callback<LoginProgressCallbackArgs>
	{
		@Override
		public boolean callback(LoginProgressCallbackArgs e)
		{
			if (e.getStatus() == LoginStatus.Success)
			{
				// Login was successful
				System.out.println("JOMV: Message of the day: " + e.getMessage());
				_Main.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, MainControl.cmdOnline));
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

	private boolean doLogin()
	{
		GridClient client = _Main.getGridClient();
		GridInfo grid = (GridInfo)getJcbGridSelector().getSelectedItem();
		client.setDefaultGrid(grid);

		grid.saveSettings = getChckbxSaveDetails().isSelected();
		grid.savePassword = grid.saveSettings && getChckbxSavePassword().isSelected();
		grid.startLocation = getJcbStartLocation().getSelectedItem().toString().toLowerCase();

		String string = getJTxtUserName().getText(); 
		if (string != null)
			grid.username = string;
		string = String.valueOf(getJPwdPassword().getPassword());
		if (string != null)
			grid.setPassword(string);
		
		client.Login.OnLoginProgress.add(new LoginProgressHandler(), false);
		try
		{
			return client.Login.Login(client.Login.new LoginParams(client));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Validate all settings
	 */
	private boolean validateSettings()
	{
		boolean valid = true;

		// Validate the first name
		if (!validateField(getJTxtUserName(), getJLblUserName()))
		{
			valid = false;
		}

		// Validate the password
		if (!validateField(getJPwdPassword(), getJLblPassword()))
		{
			valid = false;
		}

		// Set the login button enabled state based on the result (and if we're
		// not connecting)
		getJBtnLogin().setEnabled(valid);

		return valid;
	}

	/**
	 * Validate a text component
	 * 
	 * @param component
	 *            The component to validate
	 * @param associatedLabel
	 *            The label associated with the component
	 * @return True if valid, otherwise false
	 */
	private boolean validateField(JTextComponent component, JLabel associatedLabel)
	{
		// If this is a text field..
		if (component instanceof JTextField)
		{
			// Invalid
			if (component.getText() == null || component.getText().trim().length() <= 0)
			{
				associatedLabel.setForeground(Color.RED);
				return false;
			}
		}
		// If this is a password field..
		else if (component instanceof JPasswordField)
		{
			// Invalid
			if (((JPasswordField) component).getPassword().length <= 0)
			{
				associatedLabel.setForeground(Color.RED);
				return false;
			}
		}

		// Valid
		associatedLabel.setForeground(SystemColor.textText);
		return true;
	}

	private void initializePanel(GridInfo grid)
	{
		getJBrowser().navigate(grid.loginpage);
		
		if (grid.startLocation != null)
		{
			getJcbStartLocation().setSelectedItem(grid.startLocation);
		}
		if (grid.username != null)
			getJTxtUserName().setText(grid.username);
		if (grid.getPassword() != null)
			getJPwdPassword().setText(grid.getPassword());
		getChckbxSaveDetails().setSelected(grid.saveSettings);
		getChckbxSavePassword().setSelected(grid.saveSettings && grid.savePassword);				
		getChckbxSavePassword().setEnabled(grid.saveSettings);
	}
	
	private Browser getJBrowser()
	{
		if (browser == null)
		{
	        browser = BrowserFactory.createBrowser(BrowserType.getCrossPlatformBrowser());		
		}
		return browser;
	}
}
