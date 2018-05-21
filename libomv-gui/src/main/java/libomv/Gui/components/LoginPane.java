/**
 * Copyright (c) 2010-2017, Frederick Martian
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
import java.awt.Dimension;
import java.awt.EventQueue;
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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import com.teamdev.jxbrowser.Browser;

import libomv.Gui.dialogs.GridEditor;
import libomv.Gui.windows.MainControl;
import libomv.Gui.windows.MainWindow;
import libomv.io.GridClient;
import libomv.model.grid.GridInfo;
import libomv.model.grid.GridListUpdateCallbackArgs;
import libomv.utils.Callback;

public class LoginPane extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	public static final String cmdGrid = "grid";
	private static final String cmdGrids = "grids";
	private static final String cmdSaveDetails = "saveDetails";

	private MainControl _Main;
	private GridClient _Client;
	private Browser _Browser;
	private String cachedPage;

	private Callback<GridListUpdateCallbackArgs> gridListCallback = new GridListUpdateCallback();

	private JLabel jLblUserName;
	private JTextField jTxtUserName;
	private JLabel jLblPassword;
	private JPasswordField jPwdPassword;
	private JButton jBtnLogin;
	private JLabel jLblGridSelector;
	private JComboBox<GridInfo> jcbGridSelector;
	private JLabel jLblLocation;
	private JComboBox<String> jcbLocation;
	private JButton jBtnGrids;
	private JCheckBox jChkSavePassword;
	private JCheckBox jChkSaveDetails;

	public LoginPane(MainControl main, Browser browser) {
		super();
		_Main = main;
		_Client = _Main.getGridClient();
		_Browser = browser;

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 1, 4, 6, 4, 6, 4, 0, 1, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0 };
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
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
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
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 0;
		add(getChckbxSavePassword(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.gridx = 7;
		gridBagConstraints.gridy = 0;
		add(getJBtnLogin(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		add(getJLblGridSelector(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		add(getJcbGridSelector(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		add(getBtnGrids(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 1;
		add(getChckbxSaveDetails(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		add(getJLblLocation(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 0, 5, 5);
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 2;
		add(getJcbStartLocation(), gridBagConstraints);

		_Client.onGridListUpdate.add(gridListCallback);

		initializePanel((GridInfo) getJcbGridSelector().getSelectedItem());
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension size = super.getMinimumSize();
		size.height = 110;
		return size;
	}

	protected void finalize() throws Throwable {
		_Client.onGridListUpdate.remove(gridListCallback);
		super.finalize();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/* Handle local events */
		if (e.getActionCommand().equals(MainControl.cmdLogin)) {
			GridInfo grid = (GridInfo) getJcbGridSelector().getSelectedItem();
			_Client.setDefaultGrid(grid);

			grid.saveSettings = getChckbxSaveDetails().isSelected();
			grid.savePassword = grid.saveSettings && getChckbxSavePassword().isSelected();
			grid.startLocation = getJcbStartLocation().getSelectedItem().toString().toLowerCase();

			String string = getJTxtUserName().getText();
			if (string != null)
				grid.username = string;
			string = String.valueOf(getJPwdPassword().getPassword());
			if (string != null)
				grid.setPassword(string);
			try {
				_Client.saveList();
			} catch (Exception ex) {
			}
			;

			// Pass this event to the state controller to perform the actual login
			_Main.getStateControl().actionPerformed(e);
		} else if (e.getActionCommand().equals(cmdGrid)) {
			GridInfo grid = (GridInfo) getJcbGridSelector().getSelectedItem();
			if (grid != null)
				initializePanel(grid);
		} else if (e.getActionCommand().equals(cmdGrids)) {
			GridEditor gridEdit = new GridEditor(_Main, "Grid List", true);
			gridEdit.setVisible(true);
		} else if (e.getActionCommand().equals(cmdSaveDetails)) {
			JCheckBox cb = (JCheckBox) e.getSource();
			getChckbxSavePassword().setEnabled(cb.isSelected());
			if (!cb.isSelected()) {
				getChckbxSavePassword().setSelected(false);
			}
		} else {
			/* Pass to main window to be handled */
			_Main.actionPerformed(e);
		}
	}

	private JLabel getJLblUserName() {
		if (jLblUserName == null) {
			jLblUserName = new JLabel("User Name:");
		}
		return jLblUserName;
	}

	private JLabel getJLblPassword() {
		if (jLblPassword == null) {
			jLblPassword = new JLabel("Password:");
		}
		return jLblPassword;
	}

	private JLabel getJLblGridSelector() {
		if (jLblGridSelector == null) {
			jLblGridSelector = new JLabel("Grid:");
		}
		return jLblGridSelector;
	}

	private JLabel getJLblLocation() {
		if (jLblLocation == null) {
			jLblLocation = new JLabel("Location:");
		}
		return jLblLocation;
	}

	/**
	 * This method initializes textFirstName
	 *
	 * @return JTextField
	 */
	private JTextField getJTxtUserName() {
		if (jTxtUserName == null) {
			jTxtUserName = new JTextField(20);
			// Add a caret listener
			jTxtUserName.addCaretListener(new CaretListener() {
				/**
				 * Called when the caret is updated
				 *
				 * @param e
				 *            The CaretEvent
				 */
				@Override
				public void caretUpdate(CaretEvent e) {
					// Validate
					validateSettings();
				}
			});

			// Add a focus listener
			jTxtUserName.addFocusListener(new FocusAdapter() {
				/**
				 * Called when focus is gained
				 *
				 * @param e
				 *            The FocusEvent
				 */
				@Override
				public void focusGained(FocusEvent e) {
					// Select all
					getJTxtUserName().selectAll();
				}
			});

			// Add a key listener
			jTxtUserName.addKeyListener(new KeyAdapter() {
				/**
				 * Called when a key is pressed
				 *
				 * @param e
				 *            The KeyEvent
				 */
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
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
	private JPasswordField getJPwdPassword() {
		if (jPwdPassword == null) {
			jPwdPassword = new JPasswordField(20);
			// Add a caret listener
			jPwdPassword.addCaretListener(new CaretListener() {
				/**
				 * Called when the caret is updated
				 *
				 * @param e
				 *            The CaretEvent
				 */
				@Override
				public void caretUpdate(CaretEvent e) {
					// Validate input
					validateSettings();
				}
			});

			// Add a focus listener
			jPwdPassword.addFocusListener(new FocusAdapter() {
				/**
				 * Called when focus is gained
				 *
				 * @param e
				 *            The FocusEvent
				 */
				@Override
				public void focusGained(FocusEvent e) {
					// Select all
					getJPwdPassword().selectAll();
				}
			});

			// Add a key listener
			jPwdPassword.addKeyListener(new KeyAdapter() {
				/**
				 * Called when a key is pressed
				 *
				 * @param e
				 *            The KeyEvent
				 */
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						if (validateSettings())
							actionPerformed(
									new ActionEvent(jTxtUserName, ActionEvent.ACTION_PERFORMED, MainControl.cmdLogin));
						else
							getJTxtUserName().requestFocus();
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
	private JButton getJBtnLogin() {
		if (jBtnLogin == null) {
			jBtnLogin = new JButton();
			jBtnLogin.setText("Login");
			MainWindow.setAction(jBtnLogin, this, MainControl.cmdLogin);
		}
		return jBtnLogin;
	}

	private JComboBox<GridInfo> getJcbGridSelector() {
		if (jcbGridSelector == null) {
			jcbGridSelector = new JComboBox<GridInfo>(_Client.getGridInfos());
			jcbGridSelector.setSelectedItem(_Client.getDefaultGrid());
			MainWindow.setAction(jcbGridSelector, this, cmdGrid);
		}
		return jcbGridSelector;
	}

	private JComboBox<String> getJcbStartLocation() {
		if (jcbLocation == null) {
			jcbLocation = new JComboBox<String>();
			jcbLocation.setEditable(true);
			jcbLocation.addItem("Last");
			jcbLocation.addItem("Home");

			String start = _Client.getDefaultGrid().startLocation;
			if (start == null || start.isEmpty() || start.equalsIgnoreCase("last")) {
				jcbLocation.setSelectedIndex(0);
			} else if (start.equalsIgnoreCase("home")) {
				jcbLocation.setSelectedIndex(1);
			} else {
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
	private JButton getBtnGrids() {
		if (jBtnGrids == null) {
			jBtnGrids = new JButton("Grids");
			MainWindow.setAction(jBtnGrids, this, cmdGrids);
		}
		return jBtnGrids;
	}

	private JCheckBox getChckbxSaveDetails() {
		if (jChkSaveDetails == null) {
			jChkSaveDetails = new JCheckBox("Save Details");
			MainWindow.setAction(jChkSaveDetails, this, cmdSaveDetails);
		}
		return jChkSaveDetails;
	}

	private JCheckBox getChckbxSavePassword() {
		if (jChkSavePassword == null) {
			jChkSavePassword = new JCheckBox("Save Password");
		}
		return jChkSavePassword;
	}

	private void refreshGridList() {
		GridInfo[] grids = _Client.getGridInfos();
		GridInfo gridInfo = (GridInfo) getJcbGridSelector().getSelectedItem();
		if (gridInfo != null) {
			gridInfo = _Client.getGrid(gridInfo.gridnick);
		}
		if (gridInfo == null) {
			gridInfo = grids[Math.min(getJcbGridSelector().getSelectedIndex(), grids.length - 1)];
		}

		getJcbGridSelector().removeAllItems();
		for (GridInfo entry : grids) {
			getJcbGridSelector().addItem(entry);
		}
		getJcbGridSelector().setSelectedItem(gridInfo);
		initializePanel(gridInfo);
	}

	/**
	 * Validate all settings
	 */
	private boolean validateSettings() {
		boolean valid = true;

		// Validate the first name
		if (!validateField(getJTxtUserName(), getJLblUserName())) {
			valid = false;
		}

		// Validate the password
		if (!validateField(getJPwdPassword(), getJLblPassword())) {
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
	private static boolean validateField(JTextComponent component, JLabel associatedLabel) {
		// If this is a text field..
		if (component instanceof JTextField) {
			// Invalid
			if (component.getText() == null || component.getText().trim().length() <= 0) {
				associatedLabel.setForeground(Color.RED);
				return false;
			}
		}
		// If this is a password field..
		else if (component instanceof JPasswordField) {
			// Invalid
			if (((JPasswordField) component).getPassword().length <= 0) {
				associatedLabel.setForeground(Color.RED);
				return false;
			}
		}

		// Valid
		associatedLabel.setForeground(SystemColor.textText);
		return true;
	}

	private void initializePanel(GridInfo grid) {
		if (grid.startLocation != null) {
			getJcbStartLocation().setSelectedItem(grid.startLocation);
		}
		if (grid.username != null)
			getJTxtUserName().setText(grid.username);
		if (grid.getPassword() != null)
			getJPwdPassword().setText(grid.getPassword());
		getChckbxSaveDetails().setSelected(grid.saveSettings);
		getChckbxSavePassword().setSelected(grid.saveSettings && grid.savePassword);
		getChckbxSavePassword().setEnabled(grid.saveSettings);

		if (cachedPage == null || !cachedPage.equals(grid.loginpage)) {
			cachedPage = grid.loginpage;
			_Browser.navigate(grid.loginpage);
			// _Browser.loadURL(grid.loginpage);
		}
	}

	private class GridListUpdateCallback implements Callback<GridListUpdateCallbackArgs> {
		@Override
		public boolean callback(GridListUpdateCallbackArgs params) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					refreshGridList();
				}
			});
			return false;
		}

	}
}
