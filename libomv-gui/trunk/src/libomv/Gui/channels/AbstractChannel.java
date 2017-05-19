/**
 * Copyright (c) 2009-2017, Frederick Martian
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
package libomv.Gui.channels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import libomv.AgentManager.ChatType;
import libomv.GridClient;
import libomv.Gui.windows.MainControl;
import libomv.types.UUID;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.TimeoutEvent;

public abstract class AbstractChannel extends JPanel
{
	private static final long serialVersionUID = 1L;

	/** Identifier for the "regular" style of text. */
	public static final String STYLE_REGULAR = "regular";
	/** Identifier for the "action" style of text. */
	public static final String STYLE_ACTION = "action";
	/** Identifier for the "error" style of text. */
	public static final String STYLE_ERROR = "error";
	/** Identifier for the "system" style of text. */
	public static final String STYLE_SYSTEM = "system";
	/** Identifier for the "remote chat" style of text (message from a third party). */
	public static final String STYLE_CHATREMOTE = "remote";
	/** Identifier for the "remote chat from a friend" style of text (message from a third party). */
	public static final String STYLE_CHATREMOTEFRIEND = "remoteFriend";
	/** Identifier for the "local chat" style of text (message from the user). */
	public static final String STYLE_CHATLOCAL = "local";
	/** Identifier for the "informational" style of text. */
	public static final String STYLE_INFORMATIONAL = "informational";
	/** Identifier for the "offline" style of text. */
	public static final String STYLE_OFFLINE = "offline";
	/** Identifier for the "object" style of text. */
	public static final String STYLE_OBJECT = "object";
	/** Identifier for the "faint" style of text. */
	public static final String STYLE_FAINT = "faint";
	/** Identifier for the "faint" style of text. */
	public static final String STYLE_ACTION_FAINT = "faintAction";

	/** Identifier for a URL. */
	private static final Integer IDENTIFIER_URL = new Integer(0);

    protected static final String NewlineMarker = String.valueOf('\u00b6');

	private long typingEndTime;
	private TimeoutEvent<Integer> isTyping = new TimeoutEvent<Integer>();
    protected static int AGENT_TYPING_TIMEOUT = 5000;	// milliseconds

	/** Regex used for testing URLs. */
	private static String URLRegex =
			// protocol identifier
		    "([A-Za-z]+://)" +
		    // user:pass authentication
		    "(?:\\S+(?::\\S*)?@)?" +
		    "(?:" +
		      // IP address exclusion
		      // private & local networks
		      "(?!10(?:\\.\\d{1,3}){3})" +
		      "(?!127(?:\\.\\d{1,3}){3})" +
		      "(?!169\\.254(?:\\.\\d{1,3}){2})" +
		      "(?!192\\.168(?:\\.\\d{1,3}){2})" +
		      "(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})" +
		      // IP address dotted notation octets
		      // excludes loopback network 0.0.0.0
		      // excludes reserved space >= 224.0.0.0
		      // excludes network & broacast addresses
		      // (first & last IP address of each class)
		      "(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])" +
		      "(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}" +
		      "(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))" +
		    "|" +
		      // host name
		      "(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)" +
		      // domain name
		      "(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*" +
		      // TLD identifier
		      "(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))" +
		    ")" +
		    // port number
		    "(?::\\d{2,5})?" +
		    // resource path
		    "(?:/[^\\s]*)?" +
		  "$";
	
	public class ChatItem
	{
		Date timestamp;
		String from;
		String fromStyle;
		String message;
		String messageStyle;
		
		public ChatItem(String from, String fromStyle, String message, String messageStyle)
		{
			this.timestamp = new Date();
			this.from = from;
			this.fromStyle = fromStyle;
			this.message = message;
			this.messageStyle = messageStyle;
		}

		public ChatItem(Date timestamp, String from, String fromStyle, String message, String messageStyle)
		{
			this.timestamp = timestamp != null ? timestamp : new Date();
			this.from = from;
			this.fromStyle = fromStyle;
			this.message = message;
			this.messageStyle = messageStyle;
		}
	}

	private UUID _UUID;
	private UUID _Session;
	protected MainControl _Main;
	protected GridClient _Client;

	private JTextPane jTextPane;
	private JTextField jTextChat;

	private int tempStart = 0;
	private int tempLen = 0;

	private int chatPointer;
	private List<String> chatHistory;
	private List<ChatItem> chatBuffer;
	private Pattern URL;
	
	private boolean printTimestamp = true; 
	protected static DateFormat df = DateFormat.getTimeInstance();

	public AbstractChannel(MainControl main, String name, UUID id, UUID session)
	{
		super();
		_UUID = id;
		_Session = session;
		_Main = main;
		_Client = main.getGridClient();

		setName(name);
		setLayout(new BorderLayout(0, 0));

		chatBuffer = new ArrayList<ChatItem>(); 
		chatHistory = new ArrayList<String>();
		chatPointer = 0;
		isTyping.reset(0);

		// Setup an URL matcher from an URL regex.
		URL = Pattern.compile(URLRegex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

		JScrollPane scrollPaneText = new JScrollPane();
		scrollPaneText.setViewportView(getTextPane());
		scrollPaneText.getVerticalScrollBar().setValue(scrollPaneText.getVerticalScrollBar().getMaximum()); 
		add(scrollPaneText, BorderLayout.CENTER);

		JPanel panelSouth = new JPanel();
		panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));
		panelSouth.add(getJTxChat());

		JButton btnSay = new JButton("Say");
		btnSay.setHorizontalAlignment(SwingConstants.RIGHT);
		btnSay.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					transmitMessage(getJTxChat().getText(), ChatType.Normal);
				}
				catch (Exception ex)
				{
					Logger.Log("Failed to send local chat text", LogLevel.Error, _Main.getGridClient(), ex);
				}
				getJTxChat().grabFocus();
			}
		});

//		getRootPane().setDefaultButton(btnSay);
		panelSouth.add(btnSay);
		add(panelSouth, BorderLayout.SOUTH);
	}

	public UUID getUUID()
	{
		return _UUID;
	}

	public String getName()
	{
		return super.getName();
	}

	protected UUID getSession()
	{
		return _Session;
	}

//	public JPanel getPanel()
//	{
//		return this;
//	}

	protected void transmitMessage(String message, ChatType chatType) throws Exception
	{
		isTyping.reset(-1);		
	}

	/* Overwrite this for channels who send a typing indication */
	abstract protected void triggerTyping(boolean start) throws Exception;
	
	/* this must be implemented by all channels to receive messages */
	abstract public void receiveMessage(Date timestamp, UUID fromId, String fromName, String message, String style) throws BadLocationException;

	public void receiveStatus(UUID sourceID, String message, String style) throws BadLocationException
	{
		// Get the StyledDocument.
		StyledDocument styledDocument = getTextPane().getStyledDocument();

		if (tempLen > 0)
		{
			styledDocument.remove(tempStart, tempLen);
			tempLen = 0;
		}

		if (message != null && !message.isEmpty())
		{
			tempStart = styledDocument.getLength();
			styledDocument.insertString(styledDocument.getLength(), message, styledDocument.getStyle(style == null ? STYLE_SYSTEM : style));
			tempLen = message.length();
		}
	}
	
	/**
	 * Call to trigger that typing has begun.
	 * @throws Exception 
	 */
	protected void triggerTyping() throws Exception
	{
		// Update the time at which typing started
		typingEndTime = System.currentTimeMillis() + AGENT_TYPING_TIMEOUT;

		// If this is the beginning of typing.
		if (isTyping.get() <= 0)
		{
			// Set the flag
			isTyping.reset(1);
			// Inform the server of the typing status
			triggerTyping(true);

			// Start a thread that checks if we paused typing
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						// While the channel is flagged as typing...
						do
						{
							// Check for timeout.
							if (System.currentTimeMillis() > typingEndTime)
							{
								// Clear the flag
								isTyping.set(0);
							}
							// give up time
						}
						while (isTyping.waitOne(200) > 0);

						// Send the "stopped typing" message
						if (isTyping.get() == 0)
							triggerTyping(false);
					}
					catch (Exception e) { }
				}
			}).start();
		}
	}	
	
	protected void addMessage(ChatItem chatItem) throws BadLocationException
	{
		if (chatItem == null || chatItem.message == null || chatItem.message.trim().isEmpty())
			return;

		chatBuffer.add(chatItem);

		printMessage(chatItem);

		getJTxChat().setText(null);
		getJTxChat().grabFocus();
	}

	protected void addHistory(String message)
	{
		chatHistory.add(message);
		chatPointer = chatHistory.size();
	}

	protected void printMessage(ChatItem chatItem) throws BadLocationException
	{
		// Get the StyledDocument.
		StyledDocument styledDocument = jTextPane.getStyledDocument();

		if (tempLen > 0)
		{
			styledDocument.remove(tempStart, tempLen);
			tempLen = 0;
		}

		// First, if timestamps are enabled add that one
		if (printTimestamp)
		{
			styledDocument.insertString(styledDocument.getLength(), "[" + df.format(chatItem.timestamp) + "] ", styledDocument.getStyle(STYLE_INFORMATIONAL));
		}

		// Second, we insert the name.
		styledDocument.insertString(styledDocument.getLength(), chatItem.from, styledDocument.getStyle(chatItem.fromStyle));

		// Third, we insert all of the text plus a line feed with the desired style.
		styledDocument.insertString(styledDocument.getLength(), chatItem.message + "\n", styledDocument.getStyle(chatItem.messageStyle));

		// Find the start of the insert point for this message (-1 is for the additional line feed)
		int offset = styledDocument.getLength() - chatItem.message.length() - 1;
		Matcher matcher = URL.matcher(chatItem.message);
		
		// This style will be used to create a style identifying each link.
		Style urlStyle;
		// Whilst there are still links to be found...
		while (matcher.find())
		{
			// Create a style to identify the link (so that it can be clicked).
			urlStyle = styledDocument.addStyle("link" + matcher.start(), null);				
			urlStyle.addAttribute(IDENTIFIER_URL, matcher.group());
			StyleConstants.setForeground(urlStyle, Color.BLUE);
			StyleConstants.setBold(urlStyle, true);
			StyleConstants.setUnderline(urlStyle, true);
			// Update the substring of the URL within the document with the link style.
			styledDocument.setCharacterAttributes(offset + matcher.start(), matcher.end() - matcher.start(), urlStyle, true );
		}
	}

	protected void chatHistoryPrev()
	{
		if (chatPointer == 0)
			return;
		chatPointer--;
		if (chatHistory.size() > chatPointer)
		{
			String text = chatHistory.get(chatPointer);
			getJTxChat().setText(text);
			getJTxChat().setSelectionStart(text.length());
			getJTxChat().setSelectionEnd(text.length());
		}
	}

	protected void chatHistoryNext()
	{
		if (chatPointer == chatHistory.size())
			return;
		chatPointer++;
		if (chatPointer == chatHistory.size())
		{
			getJTxChat().setText(null);
			return;
		}
		String text = chatHistory.get(chatPointer);
		getJTxChat().setText(text);
		getJTxChat().setSelectionStart(text.length());
		getJTxChat().setSelectionEnd(text.length());
	}

	/** Clear the text pane. */
	public void clearText()
	{
		getTextPane().setText(null);
	}

	protected JTextPane getTextPane()
	{
		if (jTextPane == null)
		{
			jTextPane = new JTextPane();
			// Automatically scroll.
			jTextPane.setAutoscrolls(true);
			// Text is immutable.
			jTextPane.setEditable(false);

			// Get the StyledDocument.
			StyledDocument styledDocument = jTextPane.getStyledDocument();

			// Set up the regular style (using the default style context).
			Style regular = styledDocument.addStyle(STYLE_REGULAR, StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
			StyleConstants.setFontFamily(regular, "SansSerif");

			// Set up the action style.
			Style action = styledDocument.addStyle(STYLE_ACTION, regular);
			StyleConstants.setForeground(action, Color.orange);

			// Set up the error style.
			Style error = styledDocument.addStyle(STYLE_ERROR, regular);
			StyleConstants.setForeground(error, Color.red);
			StyleConstants.setBold(error, true);

			// Set up the system style.
			Style system = styledDocument.addStyle(STYLE_SYSTEM, regular);
			StyleConstants.setForeground(system, new Color(170, 0, 170)); // lighterMagenta
			StyleConstants.setBold(system, true);

			// Set up the remote chat style.
			Style chatRemote = styledDocument.addStyle(STYLE_CHATREMOTE, regular);
			StyleConstants.setForeground(chatRemote, new Color(230, 150, 0)); // darkOrange
			StyleConstants.setBold(chatRemote, true);

			// Set up the remote chat friend style.
			Style chatRemoteFriend = styledDocument.addStyle(STYLE_CHATREMOTEFRIEND, regular);
			StyleConstants.setForeground(chatRemoteFriend, Color.green);
			StyleConstants.setBold(chatRemoteFriend, true);

			// Set up the local chat style.
			Style chatLocal = styledDocument.addStyle(STYLE_CHATLOCAL, regular);
			StyleConstants.setForeground(chatLocal, Color.blue);
			StyleConstants.setBold(chatLocal, true);

			// Set up the informational style.
			Style informational = styledDocument.addStyle(STYLE_INFORMATIONAL, regular);
			StyleConstants.setForeground(informational, Color.darkGray);

			// Set up the offline style.
			Style offline = styledDocument.addStyle(STYLE_OFFLINE, regular);
			StyleConstants.setForeground(offline, Color.darkGray);
			StyleConstants.setItalic(offline, true);

			// Set up the object style.
			Style object = styledDocument.addStyle(STYLE_OBJECT, regular);
			StyleConstants.setForeground(object, new Color(0, 127, 0));  // lightGreen
			StyleConstants.setBold(object, true);

			// Set up the faint style.
			Style faint = styledDocument.addStyle(STYLE_FAINT, regular);
			StyleConstants.setForeground(faint, Color.lightGray);

			// Set up the faint action style.
			Style faintAction = styledDocument.addStyle(STYLE_ACTION_FAINT, regular);
			StyleConstants.setForeground(faintAction, Color.lightGray);
		}
		return jTextPane;
	}

	protected JTextField getJTxChat()
	{
		if (jTextChat == null)
		{
			jTextChat = new JTextField();
			jTextChat.setHorizontalAlignment(SwingConstants.LEFT);
			jTextChat.setColumns(20);
			jTextChat.addKeyListener(new KeyAdapter()
			{
				@Override
				public void keyTyped(KeyEvent e)
				{
					if (e.getKeyChar() != '\n')
					{
						try
						{
							triggerTyping();
						}
						catch (Exception ex)
						{
							Logger.Log("Failed to send typing indication", LogLevel.Error, _Main.getGridClient(), ex);
						}
						return;
					}
					e.consume();

					try
					{
						if (e.isShiftDown())
							transmitMessage(getJTxChat().getText(), ChatType.Whisper);
						else if (e.isControlDown())
							transmitMessage(getJTxChat().getText(), ChatType.Shout);
						else
							transmitMessage(getJTxChat().getText(), ChatType.Normal);
					}
					catch (Exception ex)
					{
						Logger.Log("Failed to send local chat text", LogLevel.Error, _Client, ex);
					}
				}

				@Override
				public void keyReleased(KeyEvent e)
				{
					if (e.isAltDown())
					{
						if (e.getKeyCode() == KeyEvent.VK_UP)
						{
							e.consume();
							chatHistoryPrev();
							return;
						}
						else if (e.getKeyCode() == KeyEvent.VK_DOWN)
						{
							e.consume();
							chatHistoryNext();
							return;
						}
					}
				}
			});
		}
		return jTextChat;
	}
}
