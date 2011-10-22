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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

	private JButton jBtnSetup;
	private JButton jBtnCancel;
	private JButton jBtnOk;
	
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
		setResizable(false);
		setSize(new Dimension(640, 400));
		setLocationByPlatform(true);
		
		getContentPane().add(getJSpGridNames(), BorderLayout.WEST);
		
		JPanel jEditPanel = new JPanel();
		getContentPane().add(jEditPanel, BorderLayout.CENTER);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{1, 2, 3, 1, 0};
		gridBagLayout.rowHeights = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		jEditPanel.setLayout(gridBagLayout);
		
		JLabel jLblName = new JLabel("Name");
		GridBagConstraints gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 1;
		jEditPanel.add(jLblName, gbConstraints);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 1;
		jEditPanel.add(getJTxtName(), gbConstraints);
		
		JLabel jLblNick = new JLabel("Nick");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 2;
		jEditPanel.add(jLblNick, gbConstraints);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 2;
		jEditPanel.add(getJTxtNick(), gbConstraints);
		
		JLabel jLblStartUrl = new JLabel("Start URL");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 3;
		jEditPanel.add(jLblStartUrl, gbConstraints);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 3;
		jEditPanel.add(getJTxtStartUrl(), gbConstraints);
		
		JLabel jLblLoginUrl = new JLabel("Login URL");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 4;
		jEditPanel.add(jLblLoginUrl, gbConstraints);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 4;
		jEditPanel.add(getJTxtLoginUrl(), gbConstraints);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(0, 0, 5, 0);
		gbConstraints.gridx = 3;
		gbConstraints.gridy = 4;
		jEditPanel.add(getJBtnSetup(), gbConstraints);
		
		JLabel jLblHelperUrl = new JLabel("Helper URL");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 5;
		jEditPanel.add(jLblHelperUrl, gbConstraints);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 5;
		jEditPanel.add(getJTxtHelperUrl(), gbConstraints);
		
		JLabel jLblWebsite = new JLabel("Website");
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 6;
		jEditPanel.add(jLblWebsite, gbConstraints);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 6;
		jEditPanel.add(getJTxtWebsiteUrl(), gbConstraints);
		
		JLabel jLblSupportUrl = new JLabel("Support URL");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 7;
		jEditPanel.add(jLblSupportUrl, gbConstraints);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 7;
		jEditPanel.add(getJTxtSupportUrl(), gbConstraints);
		
		JLabel jLblRegisterUrl = new JLabel("Register URL");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 8;
		jEditPanel.add(jLblRegisterUrl, gbConstraints);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 8;
		jEditPanel.add(getJTxtRegisterUrl(), gbConstraints);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 0, 5);
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 12;
		jEditPanel.add(getJBtnOk(), gbConstraints);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 0, 5);
		gbConstraints.gridx = 3;
		gbConstraints.gridy = 12;
		jEditPanel.add(getJBtnCancel(), gbConstraints);
		
		getRootPane().setDefaultButton(getJBtnOk());
		updateGridProperties(_Client.getDefaultGrid(), false);
	}
	
	private JButton getJBtnSetup()
	{
		if (jBtnSetup == null)
		{
			jBtnSetup = new JButton("Setup");
			jBtnSetup.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					try
					{
						updateGridProperties(_Client.queryGridInfo(((GridInfo)(getJLsGridNames().getSelectedValue())).loginuri), false);
					}
					catch (Exception e)	{ }
				}
			});
		}
		return jBtnSetup;
	}
	
	private JButton getJBtnOk()
	{
		if (jBtnOk == null)
		{
			jBtnOk = new JButton("Ok");
			jBtnOk.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					// Get number of items in the list
					int size = getJLsGridNames().getModel().getSize(); // 4
					// Get all item objects
					for (int i=0; i<size; i++)
					{
						_Client.addGrid((GridInfo)getJLsGridNames().getModel().getElementAt(i));
					}					
					dispose();
				}
			});
		}
		return jBtnOk;
	}

	private JButton getJBtnCancel()
	{
		if (jBtnCancel == null)
		{
			jBtnCancel = new JButton("Cancel");
			jBtnCancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					dispose();
				}
			});
		}
		return jBtnCancel;
	}

	private JTextField getJTxtName()
	{
		if (jTxtName == null)
		{
			jTxtName = new JTextField();
			jTxtName.setColumns(20);
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
			jTxtNick.setColumns(20);
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
			jTxtStartUrl.setColumns(20);
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
			jTxtLoginUrl.setColumns(20);
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
			jTxtHelperUrl.setColumns(20);
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
			jTxtWebsiteUrl.setColumns(20);
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
			jTxtSupportUrl.setColumns(20);
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
			jTxtRegisterUrl.setColumns(20);
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

	private GridInfo lastSelection;
	
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
			
			lastSelection = _Client.getDefaultGrid();
			jLsGridNames = new JList(grids);
			jLsGridNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jLsGridNames.setLayoutOrientation(JList.VERTICAL);
			jLsGridNames.setVisibleRowCount(-1);
			jLsGridNames.setSelectedValue(lastSelection, true);
			jLsGridNames.addListSelectionListener(new ListSelectionListener()
			{
				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					updateGridProperties(lastSelection, true);
					lastSelection = (GridInfo)((JList)e.getSource()).getSelectedValue();
					updateGridProperties(lastSelection, false);				
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
			jSpGridNames.setPreferredSize(new Dimension(200, 100));
		}
		return jSpGridNames;
	}

	private void updateGridProperties(GridInfo grid, boolean set)
	{
		if (set)
		{
			grid.gridname = getJTxtName().getText();
			grid.gridnick = getJTxtNick().getText();
			grid.loginpage = getJTxtStartUrl().getText();
			grid.loginuri = getJTxtLoginUrl().getText();
			grid.helperuri = getJTxtHelperUrl().getText();
			grid.website = getJTxtWebsiteUrl().getText();
			grid.support = getJTxtSupportUrl().getText();
			grid.register = getJTxtRegisterUrl().getText();
		}
		else
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
}
