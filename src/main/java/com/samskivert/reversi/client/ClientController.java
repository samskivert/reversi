//
// Reversi - A reversi implementation built directly atop Narya/Nenya/Vilya
// http://github.com/samskivert/reversi/blob/master/LICENSE

package com.samskivert.reversi.client;

import java.awt.event.ActionEvent;

import com.samskivert.swing.Controller;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientAdapter;

import static com.samskivert.reversi.Log.log;

/**
 * Responsible for top-level control of the client user interface.
 */
public class ClientController extends Controller
{
    public ClientController (SimpleContext ctx, final SimpleClient sclient)
    {
        // we'll want to keep these around
        _ctx = ctx;

        // we want to know about logon/logoff
        _ctx.getClient().addClientObserver(new ClientAdapter() {
            @Override public void clientDidLogoff (Client client) {
                sclient.setMainPanel(_logonPanel);
            }
        });

        // create the logon panel and display it
        _logonPanel = new LogonPanel(_ctx, sclient);
        sclient.setMainPanel(_logonPanel);
    }

    public LogonPanel getLogonPanel ()
    {
        return _logonPanel;
    }

    @Override
    public boolean handleAction (ActionEvent action)
    {
        String cmd = action.getActionCommand();
        if (cmd.equals("logoff")) {
            _ctx.getClient().logoff(true);
            return true;
        }
        log.info("Unhandled action: " + action);
        return false;
    }

    protected SimpleContext _ctx;
    protected LogonPanel _logonPanel;
}
