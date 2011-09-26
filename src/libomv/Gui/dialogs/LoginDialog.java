/**
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
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
package libomv.Gui.dialogs;

import libomv.GridClient;
import libomv.GridClient.GridInfo;
import libomv.Gui.Resources;
import libomv.Gui.components.LogoPanel;
import libomv.LoginManager.LoginParams;
import libomv.LoginManager.LoginProgressCallbackArgs;
import libomv.LoginManager.LoginStatus;
import libomv.Settings;
import libomv.utils.Callback;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import java.awt.Insets;
import java.io.IOException;

public class LoginDialog extends JFrame
{
	private static final long serialVersionUID = 1L;
	// The content panel
	private JPanel jContentPane = null;
	// The center panel
	private JPanel jpCenter = null;
	// The south panel
	private JPanel jpSouth = null;
	// The north panel
	private LogoPanel jpNorth = null;

	// The first name label
	private JLabel jlFirstName = null;
	// The last name label
	private JLabel jlLastName = null;
	// The password label
	private JLabel jlPassword = null;
	// The location label
	private JLabel jlLocation = null;
	// The grid label
	private JLabel jlGrid = null;
	// The save details label
	private JLabel jlSaveDetails = null;
	// The save password label
	private JLabel jlSavePassword = null;
	// The error label
	private JLabel jlError = null;

	// The first name text field
	private JTextField jtfFirstName = null;
	// The last name text field
	private JTextField jtfLastName = null;
	// The password field
	private JPasswordField jpPassword = null;

	// The grid combo box
	private JComboBox jcbGrid = null;

	private JButton btnGrids;

	// The location combo box
	private JComboBox jcbLocation = null;

	// The save details checkbox
	private JCheckBox jchkSaveDetails = null;
	// The save password checkbox. */
	private JCheckBox jchkSavePassword = null;

	// The login button
	private JButton jbLogin = null;
	// The exit button
	private JButton jbExit = null;

	private GridClient _Client;

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	public LoginDialog(GridClient client)
	{
		super();

		_Client = client;

		// Initialise
		initialize();
		// Set the icon for the window
		Image icon;
		try
		{
			icon = ImageIO.read(getClass().getResource(Resources.ICON_APPLICATION));
			this.setIconImage(icon);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Initialise the window
	 */
	private void initialize()
	{
		// Set the initial and minimum sizes
		this.setSize(306, 420);
		this.setMinimumSize(new Dimension(300, 420));
		this.setMaximumSize(new Dimension(300, 420));
		this.setResizable(false);
		// Add the content pane
		this.setContentPane(getJContentPane());
		// Set the title
		this.setTitle("libomv chat - Login (" + Settings.APPLICATION_VERSION + ")");
		// Set the default close option
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Open in the center of the screen
		setLocationRelativeTo(null);
		validateSettings();
	}

	/**
	 * Get the content pane. If it has not been initialised, it is initialised
	 * upon first call
	 */
	private JPanel getJContentPane()
	{
		// Initialise
		if (jContentPane == null)
		{
			jContentPane = new JPanel();
			// Use a border layout
			jContentPane.setLayout(new BorderLayout());
			// Create a border around the edge
			jContentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			// Add the center panel
			jContentPane.add(getJpCenter(), BorderLayout.CENTER);

			// Add the south pane
			jContentPane.add(getJpSouth(), BorderLayout.SOUTH);

			// Create a north panel
			JPanel northPanel = new JPanel(new FlowLayout());
			// Add the logo
			northPanel.add(getLogoPanel());
			// Add the north panel
			jContentPane.add(northPanel, BorderLayout.NORTH);
		}

		return jContentPane;
	}

	/**
	 * Get the center content pane. If it is not constructed, it is constructed
	 * on the first call
	 * 
	 * @return The center content pane
	 */
	private JPanel getJpCenter()
	{
		if (jpCenter == null)
		{
			jpCenter = new JPanel();

			// Use a gridbag layout
			jpCenter.setLayout(new GridBagLayout());

			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the first name label
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.ipady = 2;
			jpCenter.add(getJlFirstName(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the last name label
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 1;
			jpCenter.add(getJlLastName(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the password label
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 2;
			jpCenter.add(getJlPassword(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the location label
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 3;
			jpCenter.add(getJlLocation(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the grid label
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 4;
			jpCenter.add(getJlGrid(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the save details label
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 6;
			jpCenter.add(getJlSaveDetails(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the save password label
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 7;
			jpCenter.add(getJlSavePassword(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the first name text field
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 0;
			jpCenter.add(getJtfFirstName(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the last name text field
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 1;
			jpCenter.add(getJtfLastName(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the password box
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 2;
			jpCenter.add(getJpPassword(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the location combo box
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 3;
			jpCenter.add(getJcbLocation(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the grid combo box
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 4;
			jpCenter.add(getJcbGrid(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the grids button
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 5;
			jpCenter.add(getJbGrids(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the save details checkbox
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 6;
			jpCenter.add(getJchkSaveDetails(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the save password checkbox
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 7;
			jpCenter.add(getJchkSavePassword(), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			// Add the error label
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 8;
			gridBagConstraints.gridwidth = 2;
			jpCenter.add(getJlError(), gridBagConstraints);
		}
		return jpCenter;
	}

	/**
	 * Get the south content pane. If it is not constructed, it is constructed
	 * on the first call
	 * 
	 * @return The south content pane
	 */
	private JPanel getJpSouth()
	{
		if (jpSouth == null)
		{
			jpSouth = new JPanel();
			jpSouth.setLayout(new GridLayout());

			// Add the login button
			jpSouth.add(getJbLogin());
			// Add the exit button
			jpSouth.add(getJbExit());
		}

		return jpSouth;
	}

	/**
	 * Get the logo panel. If it is not constructed, it is constructed on the
	 * first call
	 * 
	 * @return The logo panel
	 */
	private LogoPanel getLogoPanel()
	{
		if (jpNorth == null)
		{
			jpNorth = new LogoPanel();
			jpNorth.setPreferredSize(new Dimension(128, 128));
		}

		return jpNorth;
	}

	/**
	 * Get the first name label. If it is not constructed, it is constructed on
	 * the first call
	 * 
	 * @return The first name label
	 */
	private JLabel getJlFirstName()
	{
		if (jlFirstName == null)
		{
			jlFirstName = new JLabel("First name:");
		}

		return jlFirstName;
	}

	/**
	 * Get the last name label. If it is not constructed, it is constructed on
	 * the first call
	 * 
	 * @return The last name label
	 */
	private JLabel getJlLastName()
	{
		if (jlLastName == null)
		{
			jlLastName = new JLabel("Last name:");
		}

		return jlLastName;
	}

	/**
	 * Get the password label. If it is not constructed, it is constructed on
	 * the first call
	 * 
	 * @return The password label
	 */
	private JLabel getJlPassword()
	{
		if (jlPassword == null)
		{
			jlPassword = new JLabel("Password:");
		}

		return jlPassword;
	}

	/**
	 * Get the location label. If it is not constructed, it is constructed on
	 * the first call
	 * 
	 * @return The location label
	 */
	private JLabel getJlLocation()
	{
		if (jlLocation == null)
		{
			jlLocation = new JLabel("Location:");
		}

		return jlLocation;
	}

	/**
	 * Get the grid label. If it is not constructed, it is constructed on the
	 * first call
	 * 
	 * @return The grid label
	 */
	private JLabel getJlGrid()
	{
		if (jlGrid == null)
		{
			jlGrid = new JLabel("Grid:");
		}

		return jlGrid;
	}

	/**
	 * Get the save details label. If it is not constructed, it is constructed
	 * on the first call
	 * 
	 * @return The save details label
	 */
	private JLabel getJlSaveDetails()
	{
		if (jlSaveDetails == null)
		{
			jlSaveDetails = new JLabel("Save Details:");
		}

		return jlSaveDetails;
	}

	/**
	 * Get the save password label. If it is not constructed, it is constructed
	 * on the first call
	 * 
	 * @return The save password label
	 */
	private JLabel getJlSavePassword()
	{
		if (jlSavePassword == null)
		{
			jlSavePassword = new JLabel("Save Password:");
		}

		return jlSavePassword;
	}

	/**
	 * Get the first name text field. If it is not constructed, it is
	 * constructed on the first call
	 * 
	 * @return The fist name text field
	 */
	private JTextField getJtfFirstName()
	{
		if (jtfFirstName == null)
		{
			jtfFirstName = new JTextField(_Client.getDefaultGrid().firstname);
			jtfFirstName.setPreferredSize(new Dimension(150, 20));
			// Add a caret listener
			jtfFirstName.addCaretListener(new CaretListener()
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
			jtfFirstName.addFocusListener(new FocusAdapter()
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
					getJtfFirstName().selectAll();
				}
			});

			// Add a key listener
			jtfFirstName.addKeyListener(new KeyAdapter()
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
						getJtfLastName().requestFocus();
					}
				}
			});
		}
		return jtfFirstName;
	}

	/**
	 * Get the last name text field. If it is not constructed, it is constructed
	 * on the first call
	 * 
	 * @return The last name text field
	 */
	private JTextField getJtfLastName()
	{
		if (jtfLastName == null)
		{
			jtfLastName = new JTextField(_Client.getDefaultGrid().lastname);
			jtfLastName.setPreferredSize(new Dimension(150, 20));
			// Add a caret listener
			jtfLastName.addCaretListener(new CaretListener()
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
			jtfLastName.addFocusListener(new FocusAdapter()
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
					getJtfLastName().selectAll();
				}
			});

			// Add a key listener
			jtfLastName.addKeyListener(new KeyAdapter()
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
						getJpPassword().requestFocus();
					}
				}
			});
		}

		return jtfLastName;
	}

	/**
	 * Get the grid combo box. If it is not constructed, it is constructed on
	 * the first call
	 * 
	 * @return The host text field
	 */
	private JComboBox getJcbGrid()
	{
		if (jcbGrid == null)
		{
			jcbGrid = new JComboBox(_Client.getGridNames().toArray());
			jcbGrid.setPreferredSize(new Dimension(150, 20));
			jcbGrid.setSelectedItem(_Client.getDefaultGrid());

			jcbGrid.addActionListener(new ActionListener()
			{
				/**
				 * Called when an action is performed on the combo box
				 * 
				 * @param e
				 *            The action events
				 */
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					_Client.setDefaultGrid((String) jcbGrid.getSelectedItem());
					String start = getSelectedGrid().startLocation.toLowerCase();
					if (start == null || start.isEmpty() || start.equals("last"))
					{
						jcbLocation.setSelectedIndex(0);
					}
					else if (start.equals("home"))
					{
						jcbLocation.setSelectedIndex(1);
					}
					else
					{
						int i;
						for (i = 2; i < jcbLocation.getItemCount(); i++)
						{
							if (((String) jcbLocation.getItemAt(i)).equals(start))
							{
								jcbLocation.setSelectedIndex(i);
							}
						}
						if (i == jcbLocation.getItemCount())
						{
							jcbLocation.addItem(start);
							jcbLocation.setSelectedIndex(i);
						}
					}
					if (!getSelectedGrid().lastname.isEmpty())
					{
						getJtfLastName().setText(getSelectedGrid().lastname);
					}
					if (!getSelectedGrid().lastname.isEmpty())
					{
						getJtfLastName().setText(getSelectedGrid().lastname);
					}
					if (!getSelectedGrid().getPassword().isEmpty())
					{
						getJpPassword().setText(getSelectedGrid().getPassword());
					}
					else
					{
						getJpPassword().setText("");
					}
				}
			});
		}
		return jcbGrid;
	}

	/**
	 * Get the grids button. If it is not constructed, it is constructed on the
	 * first call
	 * 
	 * @return The grids button
	 */
	private JButton getJbGrids()
	{
		if (btnGrids == null)
		{
			btnGrids = new JButton("Grids");
			btnGrids.addActionListener(new ActionListener()
			{
				/**
				 * Called when an action is performed on the button
				 * 
				 * @param e
				 *            The action events
				 */
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					// TODO: Show the Gridlist panel
				}
			});
		}
		return btnGrids;
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

	/**
	 * Convinience function to return the grid info structure for the currently
	 * selected grid
	 * 
	 * @return The GridInfo structure corresponding to the currently selected
	 *         grid
	 */
	private GridInfo getSelectedGrid()
	{
		return _Client.getGrid((String) getJcbGrid().getSelectedItem());
	}

	/**
	 * Get the password field. If it is not constructed, it is constructed on
	 * the first call
	 * 
	 * @return The password field
	 */
	private JPasswordField getJpPassword()
	{
		if (jpPassword == null)
		{
			jpPassword = new JPasswordField(_Client.getDefaultGrid().getPassword());
			jpPassword.setPreferredSize(new Dimension(150, 20));
			// Add a caret listener
			jpPassword.addCaretListener(new CaretListener()
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
			jpPassword.addFocusListener(new FocusAdapter()
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
					getJpPassword().selectAll();
				}
			});

			// Add a key listener
			jpPassword.addKeyListener(new KeyAdapter()
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

		return jpPassword;
	}

	/**
	 * Get the location combo box. If it is not constructed, it is constructed
	 * on the first call
	 * 
	 * @return The location combo box
	 */
	private JComboBox getJcbLocation()
	{
		if (jcbLocation == null)
		{
			jcbLocation = new JComboBox();
			jcbLocation.setPreferredSize(new Dimension(150, 20));
			jcbLocation.setEditable(true);
			jcbLocation.addItem("Last");
			jcbLocation.addItem("Home");
			String start = _Client.getDefaultGrid().startLocation.toLowerCase();
			if (start == null || start.isEmpty() || start.equals("last"))
			{
				jcbLocation.setSelectedIndex(0);
			}
			else if (start.equals("home"))
			{
				jcbLocation.setSelectedIndex(1);
			}
			else
			{
				jcbLocation.addItem(start);
				jcbLocation.setSelectedIndex(2);
			}
		}
		return jcbLocation;
	}

	/**
	 * Get the save details checkbox. If it is not constructed, it is
	 * constructed on the first call
	 * 
	 * @return The save details checkbox
	 */
	private JCheckBox getJchkSaveDetails()
	{
		if (jchkSaveDetails == null)
		{
			jchkSaveDetails = new JCheckBox("", getSelectedGrid().saveSettings);
			jchkSaveDetails.setPreferredSize(new Dimension(150, 20));
			// Add an item listener
			jchkSaveDetails.addItemListener(new ItemListener()
			{
				/**
				 * Called whenever the item state is changed
				 * 
				 * @param e
				 *            The ItemEvent
				 */
				@Override
				public void itemStateChanged(ItemEvent e)
				{
					// Update the settings
					getSelectedGrid().saveSettings = e.getStateChange() == ItemEvent.SELECTED;
					// Disable save password if we disabled to save settings
					if (e.getStateChange() != ItemEvent.SELECTED)
					{
						getJchkSavePassword().setSelected(false);
					}
				}
			});
		}
		return jchkSaveDetails;
	}

	/**
	 * Get the save password checkbox. If it is not constructed, it is
	 * constructed on the first call
	 * 
	 * @return The save password checkbox
	 */
	private JCheckBox getJchkSavePassword()
	{
		if (jchkSavePassword == null)
		{
			jchkSavePassword = new JCheckBox("", getSelectedGrid().savePassword);
			jchkSavePassword.setPreferredSize(new Dimension(150, 20));
			// Add an item listener
			jchkSavePassword.addItemListener(new ItemListener()
			{
				/**
				 * Called whenever the item state is changed
				 * 
				 * @param e
				 *            The ItemEvent
				 */
				@Override
				public void itemStateChanged(ItemEvent e)
				{
					// Update the settings
					getSelectedGrid().savePassword = e.getStateChange() == ItemEvent.SELECTED;
				}
			});
		}
		return jchkSavePassword;
	}

	/**
	 * Get the login button. If it is not constructed, it is constructed on the
	 * first call
	 * 
	 * @return The login button
	 */
	private JButton getJbLogin()
	{
		if (jbLogin == null)
		{
			jbLogin = new JButton("Login");
			// Add an action listener
			jbLogin.addActionListener(new ActionListener()
			{
				/**
				 * Called when an action is performed on the button
				 * 
				 * @param e
				 *            The action events
				 */
				@Override
				public void actionPerformed(ActionEvent e)
				{
					doLogin();
					setVisible(false);
					dispose();
				}
			});
		}

		return jbLogin;
	}

	/**
	 * Get the exit button. If it is not constructed, it is constructed on the
	 * first call
	 * 
	 * @return The exit button
	 */
	private JButton getJbExit()
	{
		if (jbExit == null)
		{
			jbExit = new JButton("Exit");
			// Add an action listener
			jbExit.addActionListener(new ActionListener()
			{
				/**
				 * Called when an action is performed
				 * 
				 * @param e
				 *            The ActionEvent
				 */
				@Override
				public void actionPerformed(ActionEvent e)
				{
					// Exit the application
					try
					{
						_Client.Network.Logout();
					}
					catch (Exception ex)
					{
					}
					setVisible(false);
					dispose();
				}
			});
		}
		return jbExit;
	}

	/**
	 * Get the error label. If it is not constructed, it is constructed on the
	 * first call
	 * 
	 * @return The error
	 */
	private JLabel getJlError()
	{
		if (jlError == null)
		{
			jlError = new JLabel();
			// Red is a good colour for errors
			jlError.setForeground(Color.RED);
			// Set a preferred size so that the GUI doesn't jump when an error
			// is displayed for the first time
			jlError.setPreferredSize(new Dimension(10, 20));
		}

		return jlError;
	}

	/**
	 * Set the enabled state of all controls
	 * 
	 * @param enabled
	 *            The enabled state to set
	 */
	private void setControlState(final boolean enabled)
	{
		// If we're not running in the UI thread..
		if (!SwingUtilities.isEventDispatchThread())
		{
			try
			{
				// Run from the UI thread
				SwingUtilities.invokeAndWait(new Runnable()
				{
					/**
					 * Called when the thread is run
					 */
					@Override
					public void run()
					{
						setControlState(enabled);
					}
				});
			}
			catch (Exception ex)
			{
				Logger.Log("", LogLevel.Error, _Client, ex);
			}
		}
		// If we're running from the UI thread..
		else
		{
			getJbExit().setEnabled(enabled);
			getJbLogin().setEnabled(enabled);
			getJtfFirstName().setEnabled(enabled);
			getJtfLastName().setEnabled(enabled);
			getJcbLocation().setEnabled(enabled);
			getJcbGrid().setEnabled(enabled);
			getJchkSaveDetails().setEnabled(enabled);
			getJchkSavePassword().setEnabled(enabled);
			getJpPassword().setEnabled(enabled);
		}
	}

	/**
	 * Validate all settings
	 */
	private boolean validateSettings()
	{
		boolean valid = true;

		// Validate the first name
		if (!validateField(getJtfFirstName(), getJlFirstName()))
		{
			valid = false;
		}

		// Validate the last name
		if (!validateField(getJtfLastName(), getJlLastName()))
		{
			valid = false;
		}

		// Validate the password
		if (!validateField(getJpPassword(), getJlPassword()))
		{
			valid = false;
		}

		// Set the login button enabled state based on the result (and if we're
		// not connecting)
		getJbLogin().setEnabled(valid && !getLogoPanel().getIsConnecting());

		return valid;
	}

	/**
	 * Set the visibility of the form
	 * 
	 * @param visible
	 *            True to be visible, false to be invisible
	 */
	@Override
	public void setVisible(boolean visible)
	{
		// Stop the connecting animation (if it was running)
		getLogoPanel().setConnecting(false);
		// Call to the super class
		super.setVisible(visible);

		// If we're not saving the password, make sure we clear it (in the case
		// of re-logging in)
		if (!getSelectedGrid().savePassword)
			getJpPassword().setText("");

		// If the first name and last name are complete, focus the password
		if (getJtfFirstName().getText().trim().length() > 0 && getJtfLastName().getText().trim().length() > 0)
			getJpPassword().requestFocus();
	}

	private class LoginProgress implements Callback<LoginProgressCallbackArgs>
	{
		@Override
		public void callback(LoginProgressCallbackArgs params)
		{
			if (params.getStatus() == LoginStatus.Success)
			{
				// Close this dialog
				setVisible(false);
			}
			else if (params.getStatus() == LoginStatus.Failed)
			{
				if (params.getReason().equals("tos"))
				{
					// Set an error in the error label
					getJlError().setText("Acceptance of Terms of Service required");
					// TODO: Show ToS dialog

				}
				else
				{
					// Set an error in the error label
					getJlError().setText("Error logging in to the Grid");
					// Close this dialog
					setVisible(false);
					// Re-enable controls
					setControlState(true);
				}
			}
		}
	}

	/**
	 * Perform the login
	 */
	private void doLogin()
	{
		// Start the connecting animation
		getLogoPanel().setConnecting(true);
		// Disable all controls
		setControlState(false);
		// Clear error text
		getJlError().setText("");

		// Store our settings
		_Client.setDefaultGrid((String) getJcbGrid().getSelectedItem());
		getSelectedGrid().saveSettings = getJchkSaveDetails().isSelected();
		getSelectedGrid().firstname = getJtfFirstName().getText();
		getSelectedGrid().lastname = getJtfLastName().getText();
		getSelectedGrid().startLocation = (String) getJcbLocation().getSelectedItem();
		getSelectedGrid().savePassword = getJchkSavePassword().isSelected();
		getSelectedGrid().setPassword(new String(getJpPassword().getPassword()));

		// Connect the grid
		LoginParams params = _Client.Login.DefaultLoginParams(getJtfFirstName().getText().trim(), getJtfLastName()
				.getText().trim(), new String(getJpPassword().getPassword()), (String) getJcbLocation()
				.getSelectedItem());
		try
		{
			_Client.Login.RequestLogin(params, new LoginProgress());
		}
		catch (Exception ex)
		{
			Logger.Log("Failed to login", LogLevel.Error, _Client, ex);
		}
	}
}
