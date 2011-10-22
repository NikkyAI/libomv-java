package libomv.Gui.dialogs;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import libomv.GridClient;
import libomv.GridClient.GridInfo;

public class GridEditor extends JDialog
{
	private static final long serialVersionUID = 1L;

	private GridClient _Client;

	private JList jLsGridNames;
	private JScrollPane jSpGridNames;
	private JTextField jTxtName;
	private JTextField jTxtNick;
	private JTextField jTxtStartUrl;
	private JTextField jTxtLoginUrl;
	private JTextField jTxtHelperUrl;
	private JTextField jTxtWebsiteUrl;
	private JTextField jTxtSupportUrl;
	private JTextField jTxtRegisterUrl;

	public GridEditor(GridClient client, JFrame parent, String title, boolean modal)
	{
		// Super constructor
		super(parent, title, modal);

		// Set the title again
		super.setTitle(title);

		_Client = client;
		
		// Do not allow resizing
		setResizable(true);
		
		getContentPane().add(getJSpGridNames(), BorderLayout.WEST);
		
		JPanel jEditPanel = new JPanel();
		getContentPane().add(jEditPanel, BorderLayout.CENTER);

		GridBagLayout gbl_jEditPanel = new GridBagLayout();
		gbl_jEditPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_jEditPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_jEditPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_jEditPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		jEditPanel.setLayout(gbl_jEditPanel);
		
		JLabel jLblName = new JLabel("Name");
		GridBagConstraints gbc_jLblName = new GridBagConstraints();
		gbc_jLblName.anchor = GridBagConstraints.WEST;
		gbc_jLblName.insets = new Insets(0, 0, 5, 5);
		gbc_jLblName.gridx = 1;
		gbc_jLblName.gridy = 1;
		jEditPanel.add(jLblName, gbc_jLblName);
		
		GridBagConstraints gbc_jTxtName = new GridBagConstraints();
		gbc_jTxtName.insets = new Insets(0, 0, 5, 5);
		gbc_jTxtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTxtName.gridx = 2;
		gbc_jTxtName.gridy = 1;
		jEditPanel.add(getJTxtName(), gbc_jTxtName);
		
		JLabel jLblNick = new JLabel("Nick");
		GridBagConstraints gbc_jLblNick = new GridBagConstraints();
		gbc_jLblNick.anchor = GridBagConstraints.WEST;
		gbc_jLblNick.insets = new Insets(0, 0, 5, 5);
		gbc_jLblNick.gridx = 1;
		gbc_jLblNick.gridy = 2;
		jEditPanel.add(jLblNick, gbc_jLblNick);
		
		GridBagConstraints gbc_jTxtNick = new GridBagConstraints();
		gbc_jTxtNick.insets = new Insets(0, 0, 5, 5);
		gbc_jTxtNick.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTxtNick.gridx = 2;
		gbc_jTxtNick.gridy = 2;
		jEditPanel.add(getJTxtNick(), gbc_jTxtNick);
		
		JLabel jLblStartUrl = new JLabel("Start URL");
		GridBagConstraints gbc_jLblStartUrl = new GridBagConstraints();
		gbc_jLblStartUrl.anchor = GridBagConstraints.WEST;
		gbc_jLblStartUrl.insets = new Insets(0, 0, 5, 5);
		gbc_jLblStartUrl.gridx = 1;
		gbc_jLblStartUrl.gridy = 3;
		jEditPanel.add(jLblStartUrl, gbc_jLblStartUrl);
		
		GridBagConstraints gbc_jTxtStartUrl = new GridBagConstraints();
		gbc_jTxtStartUrl.insets = new Insets(0, 0, 5, 5);
		gbc_jTxtStartUrl.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTxtStartUrl.gridx = 2;
		gbc_jTxtStartUrl.gridy = 3;
		jEditPanel.add(getJTxtStartUrl(), gbc_jTxtStartUrl);
		
		JLabel jLblLoginUrl = new JLabel("Login URL");
		GridBagConstraints gbc_jLblLoginUrl = new GridBagConstraints();
		gbc_jLblLoginUrl.anchor = GridBagConstraints.WEST;
		gbc_jLblLoginUrl.insets = new Insets(0, 0, 5, 5);
		gbc_jLblLoginUrl.gridx = 1;
		gbc_jLblLoginUrl.gridy = 4;
		jEditPanel.add(jLblLoginUrl, gbc_jLblLoginUrl);
		
		GridBagConstraints gbc_jTxtLoginUrl = new GridBagConstraints();
		gbc_jTxtLoginUrl.insets = new Insets(0, 0, 5, 5);
		gbc_jTxtLoginUrl.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTxtLoginUrl.gridx = 2;
		gbc_jTxtLoginUrl.gridy = 4;
		jEditPanel.add(getJTxtLoginUrl(), gbc_jTxtLoginUrl);
		
		JButton btnSetup = new JButton("Setup");
		GridBagConstraints gbc_btnSetup = new GridBagConstraints();
		gbc_btnSetup.insets = new Insets(0, 0, 5, 0);
		gbc_btnSetup.gridx = 3;
		gbc_btnSetup.gridy = 4;
		jEditPanel.add(btnSetup, gbc_btnSetup);
		
		JLabel jLblHelperUrl = new JLabel("Helper URL");
		GridBagConstraints gbc_jLblHelperUrl = new GridBagConstraints();
		gbc_jLblHelperUrl.anchor = GridBagConstraints.WEST;
		gbc_jLblHelperUrl.insets = new Insets(0, 0, 5, 5);
		gbc_jLblHelperUrl.gridx = 1;
		gbc_jLblHelperUrl.gridy = 5;
		jEditPanel.add(jLblHelperUrl, gbc_jLblHelperUrl);
		
		GridBagConstraints gbc_jTxtHelperUrl = new GridBagConstraints();
		gbc_jTxtHelperUrl.insets = new Insets(0, 0, 5, 5);
		gbc_jTxtHelperUrl.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTxtHelperUrl.gridx = 2;
		gbc_jTxtHelperUrl.gridy = 5;
		jEditPanel.add(getJTxtHelperUrl(), gbc_jTxtHelperUrl);
		
		JLabel jLblWebsite = new JLabel("Website");
		GridBagConstraints gbc_jLblWebsite = new GridBagConstraints();
		gbc_jLblWebsite.insets = new Insets(0, 0, 5, 5);
		gbc_jLblWebsite.anchor = GridBagConstraints.WEST;
		gbc_jLblWebsite.gridx = 1;
		gbc_jLblWebsite.gridy = 6;
		jEditPanel.add(jLblWebsite, gbc_jLblWebsite);
		
		GridBagConstraints gbc_jTxtWebsiteUrl = new GridBagConstraints();
		gbc_jTxtWebsiteUrl.insets = new Insets(0, 0, 5, 5);
		gbc_jTxtWebsiteUrl.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTxtWebsiteUrl.gridx = 2;
		gbc_jTxtWebsiteUrl.gridy = 6;
		jEditPanel.add(getJTxtWebsiteUrl(), gbc_jTxtWebsiteUrl);
		
		JLabel jLblSupportUrl = new JLabel("Support URL");
		GridBagConstraints gbc_jLblSupportUrl = new GridBagConstraints();
		gbc_jLblSupportUrl.anchor = GridBagConstraints.WEST;
		gbc_jLblSupportUrl.insets = new Insets(0, 0, 5, 5);
		gbc_jLblSupportUrl.gridx = 1;
		gbc_jLblSupportUrl.gridy = 7;
		jEditPanel.add(jLblSupportUrl, gbc_jLblSupportUrl);
		
		GridBagConstraints gbc_jTxtSupportUrl = new GridBagConstraints();
		gbc_jTxtSupportUrl.insets = new Insets(0, 0, 5, 5);
		gbc_jTxtSupportUrl.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTxtSupportUrl.gridx = 2;
		gbc_jTxtSupportUrl.gridy = 7;
		jEditPanel.add(getJTxtSupportUrl(), gbc_jTxtSupportUrl);
		
		JLabel jLblRegisterUrl = new JLabel("Register URL");
		GridBagConstraints gbc_jLblRegisterUrl = new GridBagConstraints();
		gbc_jLblRegisterUrl.anchor = GridBagConstraints.WEST;
		gbc_jLblRegisterUrl.insets = new Insets(0, 0, 5, 5);
		gbc_jLblRegisterUrl.gridx = 1;
		gbc_jLblRegisterUrl.gridy = 8;
		jEditPanel.add(jLblRegisterUrl, gbc_jLblRegisterUrl);
		
		GridBagConstraints gbc_jTxtRegisterUrl = new GridBagConstraints();
		gbc_jTxtRegisterUrl.insets = new Insets(0, 0, 5, 5);
		gbc_jTxtRegisterUrl.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTxtRegisterUrl.gridx = 2;
		gbc_jTxtRegisterUrl.gridy = 8;
		jEditPanel.add(getJTxtRegisterUrl(), gbc_jTxtRegisterUrl);
		
		JButton btnOk = new JButton("OK");
		GridBagConstraints gbc_btnOk = new GridBagConstraints();
		gbc_btnOk.insets = new Insets(0, 0, 0, 5);
		gbc_btnOk.gridx = 2;
		gbc_btnOk.gridy = 12;
		jEditPanel.add(btnOk, gbc_btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 3;
		gbc_btnCancel.gridy = 12;
		jEditPanel.add(btnCancel, gbc_btnCancel);
		
		updateGridProperties(_Client.getDefaultGrid());
	}
	
	private JTextField getJTxtName()
	{
		if (jTxtName == null)
		{
			jTxtName = new JTextField();
			jTxtName.setColumns(10);
			// Add a focus listener
			jTxtName.addFocusListener(new FocusAdapter()
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
					getJTxtName().selectAll();
				}
			});

			// Add a key listener
			jTxtName.addKeyListener(new KeyAdapter()
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
						getJTxtNick().requestFocus();
					}
				}
			});
		}
		return jTxtName;
	}
	
	private JTextField getJTxtNick()
	{
		if (jTxtNick == null)
		{
			jTxtNick = new JTextField();
			jTxtNick.setColumns(10);
			// Add a focus listener
			jTxtNick.addFocusListener(new FocusAdapter()
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
					getJTxtNick().selectAll();
				}
			});

			// Add a key listener
			jTxtNick.addKeyListener(new KeyAdapter()
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
						getJTxtStartUrl().requestFocus();
					}
				}
			});
		}
		return jTxtNick;
	}

	private JTextField getJTxtStartUrl()
	{
		if (jTxtStartUrl == null)
		{
			jTxtStartUrl = new JTextField();
			jTxtStartUrl.setColumns(10);
			// Add a focus listener
			jTxtStartUrl.addFocusListener(new FocusAdapter()
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
					getJTxtStartUrl().selectAll();
				}
			});

			// Add a key listener
			jTxtStartUrl.addKeyListener(new KeyAdapter()
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
						getJTxtLoginUrl().requestFocus();
					}
				}
			});
		}
		return jTxtStartUrl;
	}

	private JTextField getJTxtLoginUrl()
	{
		if (jTxtLoginUrl == null)
		{
			jTxtLoginUrl = new JTextField();
			jTxtLoginUrl.setColumns(10);
			// Add a focus listener
			jTxtLoginUrl.addFocusListener(new FocusAdapter()
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
					getJTxtLoginUrl().selectAll();
				}
			});

			// Add a key listener
			jTxtLoginUrl.addKeyListener(new KeyAdapter()
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
						getJTxtHelperUrl().requestFocus();
					}
				}
			});
		}
		return jTxtLoginUrl;
	}

	private JTextField getJTxtHelperUrl()
	{
		if (jTxtHelperUrl == null)
		{
			jTxtHelperUrl = new JTextField();
			jTxtHelperUrl.setColumns(10);
			// Add a focus listener
			jTxtHelperUrl.addFocusListener(new FocusAdapter()
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
					getJTxtHelperUrl().selectAll();
				}
			});

			// Add a key listener
			jTxtHelperUrl.addKeyListener(new KeyAdapter()
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
						getJTxtWebsiteUrl().requestFocus();
					}
				}
			});
		}
		return jTxtHelperUrl;
	}

	private JTextField getJTxtWebsiteUrl()
	{
		if (jTxtWebsiteUrl == null)
		{
			jTxtWebsiteUrl = new JTextField();
			jTxtWebsiteUrl.setColumns(10);
			// Add a focus listener
			jTxtWebsiteUrl.addFocusListener(new FocusAdapter()
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
					getJTxtWebsiteUrl().selectAll();
				}
			});

			// Add a key listener
			jTxtWebsiteUrl.addKeyListener(new KeyAdapter()
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
						getJTxtSupportUrl().requestFocus();
					}
				}
			});
		}
		return jTxtWebsiteUrl;
	}

	private JTextField getJTxtSupportUrl()
	{
		if (jTxtSupportUrl == null)
		{
			jTxtSupportUrl = new JTextField();
			jTxtSupportUrl.setColumns(10);
			// Add a focus listener
			jTxtSupportUrl.addFocusListener(new FocusAdapter()
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
					getJTxtSupportUrl().selectAll();
				}
			});

			// Add a key listener
			jTxtSupportUrl.addKeyListener(new KeyAdapter()
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
						getJTxtRegisterUrl().requestFocus();
					}
				}
			});
		}
		return jTxtSupportUrl;
	}

	private JTextField getJTxtRegisterUrl()
	{
		if (jTxtRegisterUrl == null)
		{
			jTxtRegisterUrl = new JTextField();
			jTxtRegisterUrl.setColumns(10);
			// Add a focus listener
			jTxtRegisterUrl.addFocusListener(new FocusAdapter()
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
					getJTxtRegisterUrl().selectAll();
				}
			});

			// Add a key listener
			jTxtRegisterUrl.addKeyListener(new KeyAdapter()
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
						getJTxtRegisterUrl().requestFocus();
					}
				}
			});
		}
		return jTxtRegisterUrl;
	}

	private JList getJLsGridNames()
	{
		if (jLsGridNames == null)
		{
			int i = 0;
			Set<String> nicks = _Client.getGridNames();
			GridInfo[] grids = new GridInfo[nicks.size()];
			for (String nick : nicks)
			{
				grids[i++] = _Client.getGrid(nick);
			}
			jLsGridNames = new JList(grids);
			jLsGridNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jLsGridNames.setLayoutOrientation(JList.VERTICAL);
			jLsGridNames.setVisibleRowCount(-1);
			jLsGridNames.setSelectedValue(_Client.getDefaultGrid(), true);
			jLsGridNames.addListSelectionListener(new ListSelectionListener()
			{
				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					updateGridProperties((GridInfo)((JList)e.getSource()).getSelectedValue());				
				}
			});
		}
		return jLsGridNames;
	}
	
	private JScrollPane getJSpGridNames()
	{
		if (jSpGridNames == null)
		{
			jSpGridNames = new JScrollPane(getJLsGridNames());
			jSpGridNames.setPreferredSize(new Dimension(150, 100));
		}
		return jSpGridNames;
	}

	private void updateGridProperties(GridInfo grid)
	{
		getJTxtName().setText(grid.gridname);
		getJTxtNick().setText(grid.gridnick);
		getJTxtStartUrl().setText(grid.loginpage);
		getJTxtLoginUrl().setText(grid.loginuri);
		getJTxtHelperUrl().setText(grid.helperuri);
		getJTxtWebsiteUrl().setText(grid.website);
		getJTxtSupportUrl().setText(grid.support);
		getJTxtRegisterUrl().setText(grid.register);
	}
}
