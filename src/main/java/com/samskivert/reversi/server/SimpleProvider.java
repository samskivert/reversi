//
// Reversi - A reversi implementation built directly atop Narya/Nenya/Vilya
// http://github.com/samskivert/reversi/blob/master/LICENSE

package com.samskivert.reversi.server;

import javax.annotation.Generated;

import com.samskivert.reversi.client.SimpleService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationProvider;

/**
 * Defines the server-side of the {@link SimpleService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from SimpleService.java.")
public interface SimpleProvider extends InvocationProvider
{
    /**
     * Handles a {@link SimpleService#clientReady} request.
     */
    void clientReady (ClientObject caller);
}
