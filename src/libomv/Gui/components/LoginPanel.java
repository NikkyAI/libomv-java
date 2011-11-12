package libomv.Gui.components;

import java.awt.BorderLayout;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
import libomv.utils.Callback;

public class LoginPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private Browser browser;

	private GridClient _Client;
	private JFrame _Parent;
	private ActionListener _Action;
	
	private JLabel jLblFirstName;
	private JTextField jTxtFirstName;
	private JLabel jLblLastName;
	private JTextField jTxtLastName;
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
	
	public LoginPanel(GridClient client, JFrame parent, ActionListener action)
	{
		_Client = client;
		_Parent = parent;
		_Action = action;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 1, 4, 6, 4, 6, 4, 6, 1, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 2.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };

		setLayout(gridBagLayout);
		// Create a border around the edge
		setBorder(BorderFactory.createLineBorder(Color.BLACK));

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		add(getJLblFirstName(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		add(getJTxtFirstName(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		add(getJLblLastName(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 0;
		add(getJTxtLastName(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 0;
		add(getJLblPassword(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.gridx = 6;
		gridBagConstraints.gridy = 0;
		add(getJPwdPassword(), gridBagConstraints);

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
		jLblGridSelector = new JLabel("Grid:");
		add(jLblGridSelector, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 0, 5, 5);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		add(getJcbGridSelector(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 0, 5, 5);
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		add(getBtnGrids(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 5, 5, 5);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		jLblLocation = new JLabel("Location:");
		add(jLblLocation, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 0, 5, 5);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 2;
		add(getJcbStartLocation(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 0, 5, 5);
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 2;
		add(getChckbxSaveDetails(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 0, 5, 5);
		gridBagConstraints.gridx = 6;
		gridBagConstraints.gridy = 2;
		add(getChckbxSavePassword(), gridBagConstraints);
		
		parent.add(getJBrowser().getComponent(), BorderLayout.CENTER);

		initializeLoginPanel(_Client.getDefaultGrid());	
	}

	private JLabel getJLblFirstName()
	{
		if (jLblFirstName == null)
		{
			jLblFirstName = new JLabel("First Name:");
		}
		return jLblFirstName;
	}

	private JLabel getJLblLastName()
	{
		if (jLblLastName == null)
		{
			jLblLastName = new JLabel("Last Name:");
		}
		return jLblLastName;
	}

	private JLabel getJLblPassword()
	{
		if (jLblPassword == null)
		{
			jLblPassword = new JLabel("Password:");			
		}
		return jLblPassword;
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
			// Add a caret listener
			jTxtFirstName.addCaretListener(new CaretListener()
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
			jTxtFirstName.addFocusListener(new FocusAdapter()
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
					getJTxtFirstName().selectAll();
				}
			});

			// Add a key listener
			jTxtFirstName.addKeyListener(new KeyAdapter()
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
						getJTxtLastName().requestFocus();
					}
				}
			});
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
			// Add a caret listener
			jTxtLastName.addCaretListener(new CaretListener()
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
			jTxtLastName.addFocusListener(new FocusAdapter()
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
					getJTxtLastName().selectAll();
				}
			});

			// Add a key listener
			jTxtLastName.addKeyListener(new KeyAdapter()
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
			jBtnLogin.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					doLogin();
				}
			});
		}
		return jBtnLogin;
	}

	private JComboBox getJcbGridSelector()
	{
		if (jcbGridSelector == null)
		{
			jcbGridSelector = new JComboBox(_Client.getGridInfos());
			jcbGridSelector.setSelectedItem(_Client.getDefaultGrid());
			jcbGridSelector.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent evt)
				{
					initializeLoginPanel((GridInfo)((JComboBox)evt.getSource()).getSelectedItem());
				}
			});
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
			
			String start = _Client.getDefaultGrid().startLocation;
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
			jBtnGrids.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					GridEditor gridEdit = new GridEditor(_Client, _Parent, "Grid List", true);
					gridEdit.setVisible(true);
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

	public class LoginProgressHandler implements Callback<LoginProgressCallbackArgs>
	{
		@Override
		public void callback(LoginProgressCallbackArgs e)
		{
			if (e.getStatus() == LoginStatus.Success)
			{
				// Login was successful
				System.out.println("sldump: Message of the day: " + e.getMessage());
				doReturn(true);
			}
			else if (e.getStatus() == LoginStatus.Redirecting)
			{
				// Server requested redirection
				System.out.println("sldump: Server requested redirection: " + e.getReason());
			}
			else if (e.getStatus() == LoginStatus.Failed)
			{
				System.out.println("sldump: Error logging in: " + e.getReason());
			}
		}
	}

	private void doLogin()
	{
		GridInfo grid = (GridInfo)getJcbGridSelector().getSelectedItem();
		_Client.setDefaultGrid(grid);

		grid.saveSettings = getChckbxSaveDetails().isSelected();
		grid.savePassword = grid.saveSettings && getChckbxSavePassword().isSelected();
		grid.startLocation = getJcbStartLocation().getSelectedItem().toString().toLowerCase();

		String string = getJTxtFirstName().getText(); 
		if (string != null)
			grid.firstname = string;
		string = getJTxtLastName().getText();
		if (string != null)
			grid.lastname = string;
		string = String.valueOf(getJPwdPassword().getPassword());
		if (string != null)
			grid.setPassword(string);
		
		_Client.Login.OnLoginProgress.add(new LoginProgressHandler(), false);
		try
		{
			_Client.Login.Login(_Client.Login.new LoginParams(_Client));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void doReturn(boolean success)
	{
		_Parent.remove(getJBrowser().getComponent());
		_Parent.remove(this);
		_Action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, success ? "success" : "failed"));
	}
	
	/**
	 * Validate all settings
	 */
	private boolean validateSettings()
	{
		boolean valid = true;

		// Validate the first name
		if (!validateField(getJTxtFirstName(), getJLblFirstName()))
		{
			valid = false;
		}

		// Validate the last name
		if (!validateField(getJTxtLastName(), getJLblLastName()))
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
			if (component.getText() == null || component.getText().trim().length() <= 0
					|| component.getText().contains(" "))
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

	private void initializeLoginPanel(GridInfo grid)
	{
		getJBrowser().navigate(grid.loginpage);
		
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
	
	private Browser getJBrowser()
	{
		if (browser == null)
		{
	        browser = BrowserFactory.createBrowser(BrowserType.getCrossPlatformBrowser());		
		}
		return browser;
	}
}
