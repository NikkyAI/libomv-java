package libomv.Gui.windows;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.GridLayout;

import libomv.GridClient;
import libomv.Gui.components.list.FriendList;

public class CommunicationFrame extends JFrame
{
	private static final long serialVersionUID = 1L;

	public CommunicationFrame(GridClient client)
	{
		setTitle("Communication");
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		JTabbedPane jTpComm = new JTabbedPane(JTabbedPane.BOTTOM);
		jTpComm.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		jTpComm.setBorder(new LineBorder(new Color(0, 0, 0)));
		getContentPane().add(jTpComm);
		
		JTabbedPane jTpContacts = new JTabbedPane(JTabbedPane.TOP);
		jTpContacts.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		jTpContacts.setBorder(new LineBorder(new Color(0, 0, 0)));
		jTpComm.add("Contacts", jTpContacts);
		
		JList jLFriends = new FriendList(client); 
		jTpContacts.add("Friends", jLFriends);

		JList jLGroups = new JList(); 
		jTpContacts.add("Groups", jLGroups);
		
		JPanel jPLocal = new JPanel();
		jTpComm.add("Local Chat", jPLocal);
	}
}
