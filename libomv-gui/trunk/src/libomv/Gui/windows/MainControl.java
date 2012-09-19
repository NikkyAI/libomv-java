package libomv.Gui.windows;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public interface MainControl extends ActionListener
{
	public static final String cmdOnline = "online";
	public static final String cmdLogout = "logout";
	public static final String cmdQuit = "quit";
	public static final String cmdAbout = "about";
	public static final String cmdBugs = "bugs";
	public static final String cmdUpdates = "Updates";
	public static final String cmdDebugCon = "debugCon";
	public static final String cmdSettings = "settings";
	
	public JMenuItem newMenuItem(String label, ActionListener actionListener, String actionCommand);
	public void setMenuBar(JMenuBar menuBar);
	public JFrame getMainJFrame();
	public void setContentArea(Component pane);
}
