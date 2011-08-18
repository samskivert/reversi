//
// Reversi - A reversi implementation built directly atop Narya/Nenya/Vilya
// http://github.com/samskivert/reversi/blob/master/LICENSE

package com.samskivert.reversi.client;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.samskivert.servlet.user.Password;
import com.samskivert.util.StringUtil;
import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.MultiLineLabel;
import com.samskivert.swing.VGroupLayout;

import com.threerings.util.MessageBundle;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.client.LogonException;

import static com.samskivert.reversi.Log.log;

public class LogonPanel extends JPanel
    implements ActionListener, ClientObserver
{
    public LogonPanel (SimpleContext ctx, SimpleClient client)
    {
        _ctx = ctx;
        _client = client;
        _msgs = _ctx.getMessageManager().getBundle("logon");

        setLayout(new VGroupLayout());
        // setBackground(ToyBoxUI.LIGHT_BLUE);

        // stick the logon components into a panel that will stretch them to a sensible width
        JPanel box = new JPanel(
            new VGroupLayout(VGroupLayout.NONE, VGroupLayout.NONE, 5, VGroupLayout.CENTER)) {
            @Override
            public Dimension getPreferredSize () {
                Dimension psize = super.getPreferredSize();
                psize.width = Math.max(psize.width, 300);
                return psize;
            }
        };
        box.setOpaque(false);
        add(box);

        // create a big fat title label
        MultiLineLabel title = new MultiLineLabel("Simple Game", MultiLineLabel.CENTER);
        // title.setFont(ToyBoxUI.fancyFont);
        box.add(title);

        // float the logon bits side-by-side inside the wider panel
        HGroupLayout hlay = new HGroupLayout();
        hlay.setOffAxisJustification(HGroupLayout.BOTTOM);
        JPanel hbox = new JPanel(hlay);
        hbox.setOpaque(false);
        box.add(hbox);

        // this contains the username and password stuff
        VGroupLayout vlay = new VGroupLayout();
        vlay.setOffAxisJustification(VGroupLayout.RIGHT);
        JPanel subbox = new JPanel(vlay);
        subbox.setOpaque(false);
        hbox.add(subbox);
        JPanel bar = new JPanel(new HGroupLayout(GroupLayout.NONE));
        bar.add(new JLabel(_msgs.get("m.username")));
        bar.setOpaque(false);
        _username = new JTextField();
        // _username.setText(ToyBoxPrefs.getUsername());
        _username.setPreferredSize(new Dimension(100, 20));
        _username.setActionCommand("skipToPassword");
        _username.addActionListener(this);
        bar.add(_username);
        subbox.add(bar);
        bar = new JPanel(new HGroupLayout(GroupLayout.NONE));
        bar.setOpaque(false);
        bar.add(new JLabel(_msgs.get("m.password")));
        _password = new JPasswordField();
        _password.setPreferredSize(new Dimension(100, 20));
        _password.setActionCommand("logon");
        _password.addActionListener(this);
        // _password.setText(StringUtil.fill('*', ToyBoxPrefs.getPasswordLength()));
        bar.add(_password);
        subbox.add(bar);

        // create the logon button bar
        _logon = new JButton(_msgs.get("m.logon"));
        _logon.setActionCommand("logon");
        _logon.addActionListener(this);
        hbox.add(_logon);

        _status = new MultiLineLabel(_msgs.get("m.please_logon"));
        box.add(_status);

        // we'll want to listen for logon failure
        _ctx.getClient().addClientObserver(this);

        // start with focus in the username field
        _username.requestFocus();
    }

    /**
     * Informs the logon panel that we're auto-logging on. This clears out any authentication info
     * that was slurped in from prefs and disables the interface.
     */
    public void setAutoLoggingOn ()
    {
        _username.setText("");
        _password.setText("");
        setLogonEnabled(false);
    }

    public void actionPerformed (ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if (cmd.equals("skipToPassword")) {
            _password.requestFocus();

        } else if (cmd.equals("logon")) {
            logon();

        } else {
            System.out.println("Unknown action event: " + cmd);
        }
    }

    // documentation inherited from interface
    public void clientWillLogon (Client client)
    {
        // nada
    }

    // documentation inherited from interface
    public void clientDidLogon (Client client)
    {
        _status.setText(_msgs.get("m.logon_success") + "\n");
    }

    // documentation inherited from interface
    public void clientDidLogoff (Client client)
    {
        _status.setText(_msgs.get("m.logged_off") + "\n");
        setLogonEnabled(true);
    }

    // documentation inherited from interface
    public void clientDidClear (Client client)
    {
        // nada
    }

    // documentation inherited from interface
    public void clientFailedToLogon (Client client, Exception cause)
    {
        String msg;
        if (cause instanceof LogonException) {
            msg = MessageBundle.compose("m.logon_failed", cause.getMessage());
        } else {
            msg = MessageBundle.tcompose("m.logon_failed", cause.getMessage());
            cause.printStackTrace(System.err);
        }
        _status.setText(_msgs.xlate(msg) + "\n");
        setLogonEnabled(true);
    }

    // documentation inherited from interface
    public void clientObjectDidChange (Client client)
    {
        // nothing we can do here...
    }

    // documentation inherited from interface
    public void clientConnectionFailed (Client client, Exception cause)
    {
        String msg = MessageBundle.tcompose("m.connection_failed",
                                            cause.getMessage());
        _status.setText(_msgs.xlate(msg) + "\n");
        setLogonEnabled(true);
    }

    // documentation inherited from interface
    public boolean clientWillLogoff (Client client)
    {
        // no vetoing here
        return true;
    }

    protected void logon ()
    {
        // disable further logon attempts until we hear back
        setLogonEnabled(false);

        String username = _username.getText().trim();
        String password = new String(_password.getPassword()).trim();

        String server = _ctx.getClient().getHostname();
        int port = _ctx.getClient().getPorts()[0];
        String msg = MessageBundle.tcompose("m.logging_on", server, String.valueOf(port));
        _status.setText(_msgs.xlate(msg) + "\n");

        // // take care of the password stuff
        // int pwLen = ToyBoxPrefs.getPasswordLength();
        // Password encPw;
        // if (StringUtil.fill('*', pwLen).equals(password)) {
        //     // use their stored value
        //     encPw = Password.makeFromCrypto(ToyBoxPrefs.getPassword());
        // } else {
        //     // use what they typed
        //     pwLen = password.length();
        //     encPw = Password.makeFromClear(password);
        // }

        // // update the logon-related config values
        // ToyBoxPrefs.setUsername(username);
        // boolean remember = _remember.isSelected();
        // ToyBoxPrefs.setPassword(remember ? encPw.getEncrypted() : "");
        // ToyBoxPrefs.setPasswordLength(remember ? pwLen : 0);
        // ToyBoxPrefs.setRememberPassword(remember);

        // configure the client with some credentials and logon
        Client client = _ctx.getClient();
        client.setCredentials(_client.createCredentials(username, password));
        client.logon();
    }

    protected void setLogonEnabled (boolean enabled)
    {
        _username.setEnabled(enabled);
        _password.setEnabled(enabled);
        _logon.setEnabled(enabled);
    }

    protected SimpleContext _ctx;
    protected SimpleClient _client;
    protected MessageBundle _msgs;

    protected JTextField _username;
    protected JPasswordField _password;
    protected JButton _logon;
    protected MultiLineLabel _status;
}
