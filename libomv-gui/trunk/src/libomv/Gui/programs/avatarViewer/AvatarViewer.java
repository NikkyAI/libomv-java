package libomv.Gui.programs.avatarViewer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import libomv.Gui.components.AvatarPanel;

public class AvatarViewer
{
    static JFrame frame = new AvatarPanel();

    static public void main(String[] args)
	{
        frame.setSize(800, 480);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
			public void windowClosing(WindowEvent e)
            {
                exit();
            }
        });
        frame.setVisible(true);
	}

    public static void exit() {
        frame.dispose();
        System.exit(0);
    } 
}
