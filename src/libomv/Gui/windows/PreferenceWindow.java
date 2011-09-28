package libomv.Gui.windows;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class PreferenceWindow extends JDialog
{
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	/**
	 * 
	 */
	public PreferenceWindow(JFrame owner)
	{
		// TODO Auto-generated constructor stub
		super(owner);
		setTitle("About Libomv-Java");
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane()
	{
		if (jContentPane == null)
		{
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
		}
		return jContentPane;
	}
	
	
}
