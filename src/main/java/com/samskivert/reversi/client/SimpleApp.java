//
// Reversi - A reversi implementation built directly atop Narya/Nenya/Vilya
// http://github.com/samskivert/reversi/blob/master/LICENSE

package com.samskivert.reversi.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.FrameManager;
import com.threerings.media.ManagedJFrame;

import com.threerings.presents.client.Client;

import static com.samskivert.reversi.Log.log;

/**
 * The main entry point for the simple client.
 */
public class SimpleApp
{
    public static void main (String[] args)
    {
        String server = "localhost";
        if (args.length > 0) {
            server = args[0];
        }

        int port = Client.DEFAULT_SERVER_PORTS[0];
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                System.err.println("Invalid port specification '" + args[1] + "'.");
            }
        }

        String username = (args.length > 2) ? args[2] : null;
        String password = (args.length > 3) ? args[3] : null;
        start(new SimpleApp(), server, port, username, password);
    }

    /**
     * Performs the standard setup and starts the client.
     */
    public static void start (SimpleApp app, String server, int port,
                              String username, String password)
    {
        // initialize the app and run it
        try {
            app.init(username);
            app.run(server, port, username, password);
        } catch (IOException ioe) {
            log.warning("Error initializing application.", ioe);
        }
    }

    public void init (String username)
        throws IOException
    {
        // create a frame
        _frame = new GameFrame("Simple Client: " + username);
        _framemgr = FrameManager.newInstance(_frame);

        // create and initialize our client instance
        _client = new SimpleClient();
        _client.init(_frame, _framemgr);
    }

    public void run (String server, int port, String username, String password)
    {
        // show the frame and start our active renderer
        _frame.setVisible(true);
        _framemgr.start();

        // configure the client with server and port
        Client client = _client.getContext().getClient();
        log.info("Using [server=" + server + ", port=" + port + "].");
        client.setServer(server, new int[] { port });

        // configure the client with some credentials and logon
        if (username != null && password != null) {
            // create and set our credentials
            client.setCredentials(_client.createCredentials(username, password));
            client.logon();
        }
    }

    protected class GameFrame extends ManagedJFrame {
        public GameFrame (String title) {
            super(title);

            // we'll handle shutting things down ourselves
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

            // log off when they close the window
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing (WindowEvent evt) {
                    // if we're logged on, log off
                    if (_client.getContext().getClient().isLoggedOn()) {
                        _client.getContext().getClient().logoff(true);
                    }
                    // and get the heck out
                    System.exit(0);
                }
            });

            // set up our initial frame size and position
            setSize(800, 600);
            SwingUtil.centerWindow(this);
        }
    }

    protected SimpleClient _client;
    protected GameFrame _frame;
    protected FrameManager _framemgr;
}
