package libomv.Gui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JProgressBar;
import javax.swing.JButton;

import libomv.Gui.windows.MainControl;
import libomv.Gui.windows.MainWindow;
import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.UIManager;

public class ProgressPane extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;

	public static final String cmdUpdateMessage = "updateMessage";
	public static final String cmdUpdateProgress = "updateProgress";
	public static final String cmdUpdateInfo = "updateInfo";
	
	private MainControl _Main;
	
	private JLabel jLblProgressText;
	private JLabel jLblInfoText;
	private JProgressBar jProgressBar;
	private JButton jBtnCancel;
	
	public ProgressPane(MainControl main)
	{
		super();
		_Main = main;

		setBorder(new EmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout(20, 0));
		
		add(getJLblProgressText(), BorderLayout.NORTH);
		add(getJProgressBar(), BorderLayout.CENTER);
		add(getJLblInfoText(), BorderLayout.SOUTH);
		add(getJBtnCancel(), BorderLayout.EAST);
	}

	@Override
	public Dimension getMinimumSize()
	{
		Dimension size = super.getMinimumSize();
		size.height = 110;
		return size;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals(MainControl.cmdCancel))
		{
			
		}
		else if (e.getActionCommand().equals(cmdUpdateMessage))
		{
			getJLblProgressText().setText((String)e.getSource());
		}
		else if (e.getActionCommand().equals(cmdUpdateProgress))
		{
			getJProgressBar().setValue((Integer)e.getSource());
		}
		else if (e.getActionCommand().equals(cmdUpdateInfo))
		{
			getJLblInfoText().setText((String)e.getSource());
		}
		else
		{
			_Main.actionPerformed(e);
		}
	}
	
	public void updateProgress(int value, String status, String info)
	{
		getJProgressBar().setValue(value);
		getJLblProgressText().setText(status);
		if (info != null)
			getJLblInfoText().setText(info.isEmpty() ? " " : info);
	}

	private JLabel getJLblProgressText()
	{
		if (jLblProgressText == null)
		{
			jLblProgressText = new JLabel();
			jLblProgressText.setMinimumSize(new Dimension(200, 10));
			jLblProgressText.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return jLblProgressText;
	}

	private JLabel getJLblInfoText()
	{
		if (jLblInfoText == null)
		{
			jLblInfoText = new JLabel();
			jLblInfoText.setMinimumSize(new Dimension(200, 10));
			jLblInfoText.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return jLblInfoText;
	}
	
	private JProgressBar getJProgressBar()
	{
		if (jProgressBar == null)
		{
			jProgressBar = new JProgressBar();
			jProgressBar.setStringPainted(true);
		}
		return jProgressBar;
	}
	
	private JButton getJBtnCancel()
	{
		if (jBtnCancel == null)
		{
			jBtnCancel = new JButton("Cancel");
			MainWindow.setAction(jBtnCancel, this, MainControl.cmdCancel);
		}
		return jBtnCancel;
	}
}
