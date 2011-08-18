//
// Reversi - A reversi implementation built directly atop Narya/Nenya/Vilya
// http://github.com/samskivert/reversi/blob/master/LICENSE

package com.samskivert.reversi.client;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JPanel;

import com.samskivert.servlet.user.Password;
import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.util.Config;
import com.samskivert.util.RunQueue;

import com.threerings.media.FrameManager;
import com.threerings.media.ManagedJFrame;

import com.threerings.util.IdleTracker;
import com.threerings.util.KeyDispatcher;
import com.threerings.util.MessageManager;
import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientAdapter;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.client.BodyService;
import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.OccupantDirector;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.client.ParlorDirector;

import static com.samskivert.reversi.Log.log;

/**
 * Does something extraordinary.
 */
public class SimpleClient
{
    /**
     * Initializes a new client and provides it with a frame in which to display everything.
     */
    public void init (ManagedJFrame frame, FrameManager fmgr)
        throws IOException
    {
        // keep this for later
        _fmgr = fmgr;

        // create our context
        _ctx = new SimpleContext() {
            public Client getClient () {
                return _client;
            }
            public DObjectManager getDObjectManager () {
                return _client.getDObjectManager();
            }
            public Config getConfig () {
                return _config;
            }
            public LocationDirector getLocationDirector () {
                return _locdir;
            }
            public OccupantDirector getOccupantDirector () {
                return _occdir;
            }
            public ChatDirector getChatDirector () {
                return _chatdir;
            }
            public MessageManager getMessageManager () {
                return _msgmgr;
            }
            public ParlorDirector getParlorDirector () {
                return _pardtr;
            }
            public void setPlaceView (PlaceView view) {
                setMainPanel((JPanel)view);
            }
            public void clearPlaceView (PlaceView view) {
                // we'll just let the next place view replace our old one
            }
            public FrameManager getFrameManager () {
                return _fmgr;
            }
            public KeyDispatcher getKeyDispatcher () {
                return _keydisp;
            }
        };

        // create the directors/managers/etc. provided by the context
        _client = new Client(null, RunQueue.AWT);
        _client.addServiceGroup(SimpleService.GROUP);
        _msgmgr = new MessageManager("i18n");
        _keydisp = new KeyDispatcher(frame);
        _locdir = new LocationDirector(_ctx);
        _occdir = new OccupantDirector(_ctx);
        _chatdir = new ChatDirector(_ctx, ChatPanel.CHAT_MSGS);
        _pardtr = new ParlorDirector(_ctx);

        // // load up our user interface bits
        // ToyBoxUI.init(_ctx);

        // create our client controller
        _cctrl = new ClientController(_ctx, this);

        // stuff our top-level pane into the top-level of our shell
        frame.setContentPane(_root);

        // start our idle tracker
        new IdleTracker(ChatCodes.DEFAULT_IDLE_TIME, LOGOFF_DELAY) {
            @Override
            protected long getTimeStamp () {
                return _fmgr.getTimeStamp();
            }
            @Override
            protected void idledIn () {
                updateIdle(false);
            }
            @Override
            protected void idledOut () {
                updateIdle(true);
            }
            protected void updateIdle (boolean isIdle) {
                if (_ctx.getClient().isLoggedOn()) {
                    log.info("Setting idle " + isIdle + ".");
                    BodyService bsvc = _ctx.getClient().requireService(BodyService.class);
                    bsvc.setIdle(isIdle);
                }
            }
            @Override
            protected void abandonedShip () {
                if (_client.isLoggedOn()) {
                    _client.logoff(true);
                }
            }
        }.start(null, frame, _ctx.getClient().getRunQueue());

        // once we're logged on, let the server know that we're good to go
        _client.addClientObserver(new ClientAdapter() {
            public void clientDidLogon (Client client) {
                client.requireService(SimpleService.class).clientReady();
            }
        });
    }

    /**
     * Returns a reference to the context in effect for this client. This reference is valid for
     * the lifetime of the application.
     */
    public SimpleContext getContext ()
    {
        return _ctx;
    }

    // /**
    //  * Returns a reference to the main client controller.
    //  */
    // public ClientController getClientController ()
    // {
    //     return _cctrl;
    // }

    /**
     * Creates the appropriate type of credentials from the supplied username and plaintext
     * password.
     */
    public Credentials createCredentials (String username, String pw)
    {
        return new UsernamePasswordCreds(
            new Name(username), Password.makeFromClear(pw).getEncrypted());
    }

    /**
     * Sets the main user interface panel.
     */
    public void setMainPanel (JPanel panel)
    {
        // remove the old panel
        _root.removeAll();
        // add the new one
        _root.add(panel, BorderLayout.CENTER);
        // swing doesn't properly repaint after adding/removing children
        _root.revalidate();
        _root.repaint();
    }

    /** Makes our client controller visible to the dispatch system. */
    protected class RootPanel extends JPanel
        implements ControllerProvider
    {
        public RootPanel () {
            super(new BorderLayout());
        }

        public Controller getController () {
            return _cctrl;
        }
    }

    protected SimpleContext _ctx;
    protected FrameManager _fmgr;
    protected RootPanel _root = new RootPanel();
    protected Config _config = new Config("simple");

    protected Client _client;
    protected ClientController _cctrl;
    protected MessageManager _msgmgr;
    protected KeyDispatcher _keydisp;

    protected LocationDirector _locdir;
    protected OccupantDirector _occdir;
    protected ChatDirector _chatdir;
    protected ParlorDirector _pardtr;

    /** The time in milliseconds after which we log off an idle user. */
    protected static final long LOGOFF_DELAY = 8L * 60L * 1000L;
}
