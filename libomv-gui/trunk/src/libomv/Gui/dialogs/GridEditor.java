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
package libomv.Gui.dialogs;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

import java.awt.GridLayout;
import java.awt.FlowLayout;
import javax.swing.border.MatteBorder;
import java.awt.SystemColor;
import javax.swing.border.EmptyBorder;

public class GridEditor extends JDialog
{
	private static final long serialVersionUID = 1L;

	private GridClient _Client;

	private JButton jBtnSetup;
	private JButton jBtnCancel;
	private JButton jBtnOk;

	private DefaultListModel jLsModel;
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
	private JTextField jTxtPasswordUrl;
	private JTextField jTxtSearchUrl;
	private JTextField jTxtCurrencySym;
	private JPanel jPaneGridBtnPanel;
	private JButton jBtnClearGridInfo;
	private JPanel jBtnPanel;
	private JButton jBtnApply;
	private JButton jBtnAdd;
	private JButton jBtnRemove;
	private JButton jBtnSetDefault;
	private GridBagConstraints gbConstraints_1;

	public GridEditor(GridClient client, JFrame parent, String title, boolean modal)
	{
		// Super constructor
		super(parent, title, modal);

		// Set the title again
		super.setTitle(title);

		_Client = client;

		// Do not allow resizing
		setResizable(false);
		setSize(new Dimension(680, 480));
		setLocationByPlatform(true);

		getContentPane().add(getJSpGridNames(), BorderLayout.WEST);

		JPanel jEditPanel = new JPanel();
		jEditPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(jEditPanel, BorderLayout.CENTER);

		getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 1, 2, 3, 1, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
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

		JLabel jLblLoginUrl = new JLabel("Login URL");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 3;
		jEditPanel.add(jLblLoginUrl, gbConstraints);

		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 3;
		jEditPanel.add(getJTxtLoginUrl(), gbConstraints);

		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.BOTH;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 4;
		jEditPanel.add(getGridBtnPanel(), gbConstraints);

		JLabel jLblStartUrl = new JLabel("Login Page:");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 5;
		jEditPanel.add(jLblStartUrl, gbConstraints);

		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 5;
		jEditPanel.add(getJTxtStartUrl(), gbConstraints);

		JLabel jLblHelperUrl = new JLabel("Helper URI:");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 6;
		jEditPanel.add(jLblHelperUrl, gbConstraints);

		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 6;
		jEditPanel.add(getJTxtHelperUrl(), gbConstraints);

		JLabel jLblWebsite = new JLabel("Website:");
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 7;
		jEditPanel.add(jLblWebsite, gbConstraints);

		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 7;
		jEditPanel.add(getJTxtWebsiteUrl(), gbConstraints);

		JLabel jLblSupportUrl = new JLabel("Support:");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 8;
		jEditPanel.add(jLblSupportUrl, gbConstraints);

		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 8;
		jEditPanel.add(getJTxtSupportUrl(), gbConstraints);

		JLabel jLblRegisterUrl = new JLabel("Account:");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 9;
		jEditPanel.add(jLblRegisterUrl, gbConstraints);

		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 9;
		jEditPanel.add(getJTxtRegisterUrl(), gbConstraints);

		JLabel jLblPasswrodUrl = new JLabel("Password:");
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 10;
		jEditPanel.add(jLblPasswrodUrl, gbConstraints);

		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 10;
		jEditPanel.add(getJTxtPasswordUrl(), gbConstraints);

		JLabel jLblSearchUri = new JLabel("Search URI");
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 11;
		jEditPanel.add(jLblSearchUri, gbConstraints);

		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 0, 5, 5);
		gbConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints.gridx = 2;
		gbConstraints.gridy = 11;
		jEditPanel.add(getJTxtSearchUrl(), gbConstraints);

		JLabel jLblCurrencySymbol = new JLabel("Currency");
		gbConstraints = new GridBagConstraints();
		gbConstraints.insets = new Insets(5, 5, 5, 5);
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.gridx = 1;
		gbConstraints.gridy = 12;
		jEditPanel.add(jLblCurrencySymbol, gbConstraints);

		gbConstraints_1 = new GridBagConstraints();
		gbConstraints_1.anchor = GridBagConstraints.WEST;
		gbConstraints_1.insets = new Insets(5, 0, 5, 5);
		gbConstraints_1.gridx = 2;
		gbConstraints_1.gridy = 12;
		jEditPanel.add(getJTxtCurrencySym(), gbConstraints_1);

		getRootPane().setDefaultButton(getJBtnOk());
		updateGridProperties(_Client.getDefaultGrid(), false);
	}

	private JButton getJBtnSetup()
	{
		if (jBtnSetup == null)
		{
			jBtnSetup = new JButton("Get Grid Info");
			jBtnSetup.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					try
					{
						updateGridProperties(
								_Client.queryGridInfo(((GridInfo) (getJLsGridNames().getSelectedValue()))), false);
					}
					catch (Exception e)
					{
					}
				}
			});
		}
		return jBtnSetup;
	}

	private JButton getJBtnClearGridInfo()
	{
		if (jBtnClearGridInfo == null)
		{
			jBtnClearGridInfo = new JButton("Clear Grid Info");
			jBtnClearGridInfo.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					updateGridProperties(null, false);
				}
			});
		}
		return jBtnClearGridInfo;
	}

	private JButton getJBtnApply()
	{
		if (jBtnApply == null)
		{
			jBtnApply = new JButton("Apply");
			jBtnApply.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					updateGridProperties((GridInfo) (getJLsGridNames().getSelectedValue()), true);
				}
			});
		}
		return jBtnApply;
	}

	private JPanel getGridBtnPanel()
	{
		if (jPaneGridBtnPanel == null)
		{
			jPaneGridBtnPanel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) jPaneGridBtnPanel.getLayout();
			flowLayout.setVgap(0);
			flowLayout.setHgap(0);
			jPaneGridBtnPanel.add(getJBtnSetup());
			jPaneGridBtnPanel.add(getJBtnClearGridInfo());
			jPaneGridBtnPanel.add(getJBtnApply());
		}
		return jPaneGridBtnPanel;
	}

	private JButton getJBtnAdd()
	{
		if (jBtnAdd == null)
		{
			jBtnAdd = new JButton("Add Grid");
			jBtnAdd.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					getJLsModel().add(getJLsGridNames().getSelectedIndex(),
							updateGridProperties(_Client.new GridInfo(), true));
				}
			});
		}
		return jBtnAdd;
	}

	private JButton getJBtnRemove()
	{
		if (jBtnRemove == null)
		{
			jBtnRemove = new JButton("Remove Grid");
			jBtnRemove.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					getJLsModel().remove(getJLsGridNames().getSelectedIndex());
				}
			});
		}
		return jBtnRemove;
	}

	private JButton getJBtnSetDefault()
	{
		if (jBtnSetDefault == null)
		{
			jBtnSetDefault = new JButton("Set Default");
			jBtnSetDefault.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					_Client.setDefaultGrid((GridInfo)getJLsGridNames().getSelectedValue());
					jLsGridNames.setCellRenderer(new MyListCellRenderer(_Client.getDefaultGrid()));
				}
			});
		}
		return jBtnSetDefault;
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
					_Client.clearGrids(false);
					// Get number of items in the list
					int size = getJLsGridNames().getModel().getSize();
					// Get all item objects
					for (int i = 0; i < size; i++)
					{
						_Client.addGrid((GridInfo)getJLsGridNames().getModel().getElementAt(i), false);
					}
					try
					{
						_Client.saveList(true);
					}
					catch (Exception ex)
					{
						Logger.Log("Saving GridInfo list failed", LogLevel.Error, _Client, ex);
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

	private JPanel getButtonPanel()
	{
		if (jBtnPanel == null)
		{
			jBtnPanel = new JPanel();
			jBtnPanel.setBorder(new MatteBorder(5, 5, 5, 5, SystemColor.control));
			jBtnPanel.setLayout(new GridLayout(1, 6, 10, 0));
			jBtnPanel.add(getJBtnRemove());
			jBtnPanel.add(getJBtnAdd());
			jBtnPanel.add(getJBtnSetDefault());
			jBtnPanel.add(new JLabel(""));
			jBtnPanel.add(getJBtnOk());
			jBtnPanel.add(getJBtnCancel());
		}
		return jBtnPanel;
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

	private JTextField getJTxtPasswordUrl()
	{
		if (jTxtPasswordUrl == null)
		{
			jTxtPasswordUrl = new JTextField();
			jTxtPasswordUrl.setColumns(10);
		}
		return jTxtPasswordUrl;
	}

	private JTextField getJTxtSearchUrl()
	{
		if (jTxtSearchUrl == null)
		{
			jTxtSearchUrl = new JTextField();
			jTxtSearchUrl.setColumns(10);
		}
		return jTxtSearchUrl;
	}

	private JTextField getJTxtCurrencySym()
	{
		if (jTxtCurrencySym == null)
		{
			jTxtCurrencySym = new JTextField();
			jTxtCurrencySym.setColumns(10);
		}
		return jTxtCurrencySym;
	}

	private DefaultListModel getJLsModel()
	{
		if (jLsModel == null)
		{
			int i = 0;
			jLsModel = new DefaultListModel();
			Set<String> nicks = _Client.getGridNames();
			for (String nick : nicks)
			{
				jLsModel.add(i, _Client.getGrid(nick).clone());
			}
		}
		return jLsModel;
	}

	private class MyListCellRenderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 1L;
		private GridInfo defaultItem;

		MyListCellRenderer(GridInfo defaultItem)
		{
			this.defaultItem = defaultItem;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
		{
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (defaultItem.equals(value))
			{
				label.setFont(list.getFont().deriveFont(Font.BOLD));
			}
			else
			{
				label.setFont(list.getFont());
			}
			return label;
		}
	}

	private JList getJLsGridNames()
	{
		if (jLsGridNames == null)
		{
			jLsGridNames = new JList(getJLsModel());
			jLsGridNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jLsGridNames.setLayoutOrientation(JList.VERTICAL);
			jLsGridNames.setVisibleRowCount(-1);
			jLsGridNames.setCellRenderer(new MyListCellRenderer(_Client.getDefaultGrid()));
			jLsGridNames.setSelectedValue(_Client.getDefaultGrid(), true);
			jLsGridNames.addListSelectionListener(new ListSelectionListener()
			{
				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					updateGridProperties((GridInfo) getJLsGridNames().getSelectedValue(), false);
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
			jSpGridNames.setPreferredSize(new Dimension(250, 100));
		}
		return jSpGridNames;
	}

	private GridInfo updateGridProperties(GridInfo grid, boolean set)
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
			grid.passworduri = getJTxtPasswordUrl().getText();
			grid.searchurl = getJTxtSearchUrl().getText();
			grid.currencySym = getJTxtCurrencySym().getText();
		}
		else
		{
			if (grid != null)
			{
				getJTxtName().setText(grid.gridname);
				getJTxtNick().setText(grid.gridnick);
				getJTxtStartUrl().setText(grid.loginpage);
			}
			getJTxtLoginUrl().setText(grid == null ? Helpers.EmptyString : grid.loginuri);
			getJTxtHelperUrl().setText(grid == null ? Helpers.EmptyString : grid.helperuri);
			getJTxtWebsiteUrl().setText(grid == null ? Helpers.EmptyString : grid.website);
			getJTxtSupportUrl().setText(grid == null ? Helpers.EmptyString : grid.support);
			getJTxtRegisterUrl().setText(grid == null ? Helpers.EmptyString : grid.register);
			getJTxtPasswordUrl().setText(grid == null ? Helpers.EmptyString : grid.passworduri);
			getJTxtSearchUrl().setText(grid == null ? Helpers.EmptyString : grid.searchurl);
			getJTxtCurrencySym().setText(grid == null ? Helpers.EmptyString : grid.currencySym);
		}
		return grid;
	}
}
