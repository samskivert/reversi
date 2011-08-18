//
// Reversi - A reversi implementation built directly atop Narya/Nenya/Vilya
// http://github.com/samskivert/reversi/blob/master/LICENSE

package com.samskivert.reversi.server;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.server.ParlorManager;

import com.samskivert.reversi.client.SimpleService;
import com.samskivert.reversi.data.SimpleMarshaller;
import com.samskivert.reversi.game.client.ReversiController;

import static com.samskivert.reversi.Log.log;

/**
 * The main entry point for the simple server.
 */
@Singleton
public class SimpleServer extends CrowdServer
{
    /** Configures dependencies needed by the Simple services. */
    public static class SimpleModule extends CrowdServer.CrowdModule
    {
        @Override protected void configure () {
            super.configure();
        }
    }

    /**
     * The main entry point for the simple server.
     */
    public static void main (String[] args)
    {
        runServer(new SimpleModule(), new PresentsServerModule(SimpleServer.class));
    }

    @Override // from CrowdServer
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // register ourselves as providing the toybox service
        _invmgr.registerProvider(new SimpleProvider() {
            public void clientReady (ClientObject caller) {
                // if we have no waiter, or our waiter logged off, make this player wait (note:
                // this doesn't handle disconnected players, a real match making service should be
                // more robust)
                if (_waiter == null || !_waiter.isActive()) {
                    _waiter = (BodyObject)caller;
                } else {
                    final Name[] players = new Name[] {
                        _waiter.getVisibleName(), ((BodyObject)caller).getVisibleName()
                    };
                    try {
                        // create the game location and it will take over from here
                        _plreg.createPlace(new SimpleGameConfig(players));
                    } catch (Exception e) {
                        log.warning("Failed to create game", "players", players, e);
                    }
                }
            }
            protected BodyObject _waiter;
        }, SimpleMarshaller.class, SimpleService.GROUP);

        log.info("Simple server initialized.");
    }

    protected static class SimpleGameConfig extends GameConfig {
        public SimpleGameConfig (Name[] players) {
            this.players = players;
        }
        public int getGameId () {
            return 1;
        }
        public String getGameIdent () {
            return "reversi";
        }
        public PlaceController createController () {
            return new ReversiController();
        }
        public String getManagerClassName () {
            // don't use ReversiManager.class.getName() here, as this class will be loaded into the
            // client, which won't have ReversiManager in its jar file
            return "com.samskivert.reversi.game.server.ReversiManager";
        }
    }

    // inject the parlor manager to cause it to register with the system
    @Inject protected ParlorManager _parmgr;
}
