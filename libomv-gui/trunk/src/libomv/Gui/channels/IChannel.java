package libomv.Gui.channels;

import java.util.Date;

import javax.swing.JPanel;

import libomv.types.UUID;

public interface IChannel
{
	public UUID getUUID();
	public String getName();
	public JPanel getPanel();
	public void receiveMessage(Date timestamp, UUID fromId, String from, String message, String style);
}
