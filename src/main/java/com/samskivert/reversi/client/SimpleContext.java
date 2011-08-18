//
// Reversi - A reversi implementation built directly atop Narya/Nenya/Vilya
// http://github.com/samskivert/reversi/blob/master/LICENSE

package com.samskivert.reversi.client;

import com.threerings.media.FrameManager;
import com.threerings.util.MessageBundle;
import com.threerings.util.Name;

import com.threerings.crowd.data.BodyObject;

import com.threerings.parlor.util.ParlorContext;

/**
 * Provides various services to clients working with the simple server.
 */
public abstract class SimpleContext implements ParlorContext
{
    /**
     * Returns a reference to our frame manager (used for media services).
     */
    public abstract FrameManager getFrameManager ();

    /**
     * Translates the specified message using the specified message bundle.
     */
    public String xlate (String bundle, String message)
    {
        MessageBundle mb = getMessageManager().getBundle(bundle);
        return (mb == null) ? message : mb.xlate(message);
    }

    /**
     * Convenience method to get the username of the currently logged on
     * user. Returns null when we're not logged on.
     */
    public Name getUsername ()
    {
        BodyObject bobj = (BodyObject)getClient().getClientObject();
        return (bobj == null) ? null : bobj.getVisibleName();
    }
}
