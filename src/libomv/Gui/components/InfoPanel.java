package libomv.Gui.components;

import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Image;

import javax.swing.JPanel;

import libomv.GridClient;

public class InfoPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private Image image;
	private GridClient _Client;
	
	/**
	 * This is the default constructor
	 */
	public InfoPanel(GridClient client)
	{
		super();
		_Client = client;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		this.setLayout(new GridBagLayout());
	}

	@Override
	public void paintComponent(Graphics g)
	{
	    g.drawImage(image, 0, 0, null);
	    super.paintComponents(g);
	}
}
