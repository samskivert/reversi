//
// Reversi - A reversi implementation built directly atop Narya/Nenya/Vilya
// http://github.com/samskivert/reversi/blob/master/LICENSE

package com.samskivert.reversi.client;

import com.threerings.presents.client.InvocationService;

/**
 * Provides a simple "hello, I'm ready" function for clients to call once they're ready.
 */
public interface SimpleService extends InvocationService
{
    /** Defines the invocation service boot group. */
    String GROUP = "simple";

    /**
     * Lets the server know that this client is ready to be assigned to a game.
     */
    void clientReady ();
}
