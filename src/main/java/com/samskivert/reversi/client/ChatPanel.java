//
// Reversi - A reversi implementation built directly atop Narya/Nenya/Vilya
// http://github.com/samskivert/reversi/blob/master/LICENSE

package com.samskivert.reversi.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.event.AncestorAdapter;

import com.samskivert.util.StringUtil;
import com.threerings.util.MessageBundle;

import com.threerings.crowd.chat.client.ChatDisplay;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.ChatMessage;
import com.threerings.crowd.chat.data.SystemMessage;
import com.threerings.crowd.chat.data.TellFeedbackMessage;
import com.threerings.crowd.chat.data.UserMessage;

import com.threerings.crowd.client.OccupantObserver;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;

import static com.samskivert.reversi.Log.log;

public class ChatPanel extends JPanel
    implements ActionListener, ChatDisplay, OccupantObserver, PlaceView
{
    /** The message bundle identifier for chat translations. */
    public static final String CHAT_MSGS = "chat";

    public ChatPanel (SimpleContext ctx)
    {
        this(ctx, false);
    }

    public ChatPanel (SimpleContext ctx, boolean horizontal)
    {
        // keep this around for later
        _ctx = ctx;

        // create our chat director and register ourselves with it
        _ctx.getChatDirector().addChatDisplay(this);

        // register as an occupant observer
        _ctx.getOccupantDirector().addOccupantObserver(this);

        GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        setLayout(gl);
        setOpaque(false);

        // create our scrolling chat text display
        _text = new JTextPane();
        _text.setOpaque(false);
        _text.setEditable(false);

        // we need to create a custom scroll pane so that we work properly on a ManagedJFrame
        add(new JScrollPane(_text) {
            @Override
            protected JViewport createViewport () {
                JViewport vp = new JViewport() {
                    @Override
                    public void setViewPosition (Point p) {
                        super.setViewPosition(p);
                        // simple scroll mode results in setViewPosition causing our view to become
                        // invalid, but nothing ever happens to queue up a revalidate for said
                        // view, so we have to do it here
                        Component c = getView();
                        if (c instanceof JComponent) {
                            ((JComponent)c).revalidate();
                        }
                    }
                };
                vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
                return vp;
            }
        });

        // create our styles and add those to the text pane
        createStyles(_text);

        // add a label for the text entry stuff
        String htext = _ctx.xlate(CHAT_MSGS, "m.chat_label");
        if (!horizontal) {
            add(new JLabel(htext), GroupLayout.FIXED);
        }

        // create a horizontal group for the text entry bar
        gl = new HGroupLayout(GroupLayout.STRETCH);
        JPanel epanel = new JPanel(gl);
        if (horizontal) {
            epanel.add(new JLabel(htext), GroupLayout.FIXED);
        }
        epanel.setOpaque(false);
        epanel.add(_entry = new JTextField());
        _entry.setActionCommand("send");
        _entry.addActionListener(this);
        _entry.setEnabled(false);

        _send = new JButton(_ctx.xlate(CHAT_MSGS, "m.send"));
        _send.setEnabled(false);
        _send.addActionListener(this);
        _send.setActionCommand("send");
        if (horizontal) {
            epanel.add(_send, GroupLayout.FIXED);
        }
        add(epanel, GroupLayout.FIXED);

        // listen to ancestor events to request focus when added
        addAncestorListener(new AncestorAdapter() {
            @Override
            public void ancestorAdded (AncestorEvent e) {
                if (_focus) {
                    _entry.requestFocus();
                }
            }
        });
    }

    /**
     * Sets whether the chat box text entry field requests the keyboard focus when the panel
     * receives {@link AncestorListener#ancestorAdded} or {@link PlaceView#willEnterPlace} events.
     */
    public void setRequestFocus (boolean focus)
    {
        _focus = focus;
    }

    protected void createStyles (JTextPane text)
    {
        StyleContext sctx = StyleContext.getDefaultStyleContext();
        Style defstyle = sctx.getStyle(StyleContext.DEFAULT_STYLE);

        _nameStyle = text.addStyle("name", defstyle);
        StyleConstants.setForeground(_nameStyle, Color.blue);

        _msgStyle = text.addStyle("msg", defstyle);
        StyleConstants.setForeground(_msgStyle, Color.black);

        _errStyle = text.addStyle("err", defstyle);
        StyleConstants.setForeground(_errStyle, Color.red);

        _noticeStyle = text.addStyle("notice", defstyle);
        StyleConstants.setForeground(_noticeStyle, Color.magenta.darker());

        _feedbackStyle = text.addStyle("feedback", defstyle);
        StyleConstants.setForeground(_feedbackStyle, Color.green.darker());
    }

    // documentation inherited
    public void actionPerformed (ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if (cmd.equals("send")) {
            sendText();

        } else {
            System.out.println("Unknown action event: " + cmd);
        }
    }

    // documentation inherited
    public void occupantEntered (OccupantInfo info)
    {
        displayOccupantMessage("*** " + info.username + " entered.");
    }

    // documentation inherited
    public void occupantLeft (OccupantInfo info)
    {
        displayOccupantMessage("*** " + info.username + " left.");
    }

    // documentation inherited
    public void occupantUpdated (OccupantInfo oinfo, OccupantInfo info)
    {
    }

    protected void displayOccupantMessage (String message)
    {
        appendAndScroll(message, _noticeStyle);
    }

    protected void sendText ()
    {
        String text = _entry.getText().trim();
        if (!StringUtil.isBlank(text)) {
            String rv = _ctx.getChatDirector().requestChat(_room.speakService, text, true);
            if (rv != ChatCodes.SUCCESS) {
                displayError(rv);
            } else {
                _entry.setText("");
            }
        }
    }

    // documentation inherited from interface ChatDisplay
    public void clear ()
    {
        _text.setText("");
    }

    // documentation inherited from interface ChatDisplay
    public boolean displayMessage (ChatMessage message, boolean alreadyShown)
    {
        if (message instanceof UserMessage) {
            UserMessage msg = (UserMessage) message;
            String type = "m.chat_prefix_" + msg.mode;
            Style msgStyle = _msgStyle;
            if (msg.localtype == ChatCodes.USER_CHAT_TYPE) {
                type = "m.chat_prefix_tell";
            }
            if (msg.mode == ChatCodes.BROADCAST_MODE) {
                msgStyle = _noticeStyle;
            }

            String speaker = MessageBundle.tcompose(type, msg.speaker);
            speaker = _ctx.xlate(CHAT_MSGS, speaker);
            appendAndScroll(speaker, msg.message, msgStyle);
            return true;

        } else if (message instanceof SystemMessage) {
            appendAndScroll(message.message, _noticeStyle);
            return true;

        } else if (message instanceof TellFeedbackMessage) {
            appendAndScroll(message.message, _feedbackStyle);
            return true;

        } else {
            log.warning("Received unknown message type", "message", message);
            return false;
        }
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject place)
    {
        // enable our chat input elements since we're now somewhere that we can chat
        _entry.setEnabled(true);
        _send.setEnabled(true);
        if (_focus) {
            _entry.requestFocus();
        }
        _room = place;
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject place)
    {
        _room = null;
    }

    @Override
    public Dimension getPreferredSize ()
    {
        Dimension size = super.getPreferredSize();
        // always prefer a sensible but not overly large width. This also prevents us from
        // inheriting a foolishly large preferred width from the JTextPane which sometimes decides
        // it wants to be as wide as its widest line of text rather than wrap that line of text.
        size.width = PREFERRED_WIDTH;
        return size;
    }

    protected void displayFeedback (String message)
    {
        appendAndScroll(_ctx.xlate(CHAT_MSGS, message), _feedbackStyle);
    }

    protected void displayError (String message)
    {
        appendAndScroll(_ctx.xlate(CHAT_MSGS, message), _errStyle);
    }

    protected void appendAndScroll (String message, Style style)
    {
        if (_text.getDocument().getLength() > 0) {
            message = "\n" + message;
        }
        append(message, style);
        _text.scrollRectToVisible(new Rectangle(0, _text.getHeight(), _text.getWidth(), 1));
    }

    protected void appendAndScroll (String speaker, String message, Style style)
    {
        if (_text.getDocument().getLength() > 0) {
            speaker = "\n" + speaker;
        }
        append(speaker + " ", _nameStyle);
        append(message, style);

        _text.scrollRectToVisible(
            new Rectangle(0, _text.getHeight(), _text.getWidth(), 1));
    }

    /**
     * Append the specified text in the specified style.
     */
    protected void append (String text, Style style)
    {
        Document doc = _text.getDocument();
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ble) {
            log.warning("Unable to insert text!?", "error", ble);
        }
    }

    protected SimpleContext _ctx;
    protected PlaceObject _room;

    protected boolean _focus = true;

    protected JComboBox _roombox;
    protected JTextPane _text;
    protected JButton _send;
    protected JTextField _entry;

    protected Style _nameStyle, _msgStyle, _errStyle, _noticeStyle, _feedbackStyle;

    /** A width that isn't so skinny that the text is teeny. */
    protected static final int PREFERRED_WIDTH = 200;
}
