//
// Reversi - A reversi implementation built directly atop Narya/Nenya/Vilya
// http://github.com/samskivert/reversi/blob/master/LICENSE

package com.samskivert.reversi.data;

import javax.annotation.Generated;

import com.samskivert.reversi.client.SimpleService;
import com.threerings.presents.data.InvocationMarshaller;

/**
 * Provides the implementation of the {@link SimpleService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from SimpleService.java.")
public class SimpleMarshaller extends InvocationMarshaller
    implements SimpleService
{
    /** The method id used to dispatch {@link #clientReady} requests. */
    public static final int CLIENT_READY = 1;

    // from interface SimpleService
    public void clientReady ()
    {
        sendRequest(CLIENT_READY, new Object[] {
        });
    }
}
