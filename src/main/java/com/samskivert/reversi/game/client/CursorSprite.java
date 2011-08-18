//
// Reversi - A reversi implementation built directly atop Narya/Nenya/Vilya
// http://github.com/samskivert/reversi/blob/master/LICENSE

package com.samskivert.reversi.game.client;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

import com.samskivert.reversi.game.data.ReversiLogic;
import com.samskivert.reversi.game.data.ReversiObject;

/**
 * Displays a "potential move" cursor to the player.
 */
public class CursorSprite extends PieceSprite
{
    public CursorSprite ()
    {
        super(new ReversiObject.Piece());
        _comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
    }

    public void setColor (int color)
    {
        _piece.owner = color;
    }

    public void setPosition (int x, int y, ReversiLogic logic)
    {
        _piece.x = x;
        _piece.y = y;
        _legal = logic.isLegalMove(_piece);
        updatePiece(_piece);
    }

    public ReversiObject.Piece getPiece ()
    {
        return _piece;
    }

    @Override // from PieceSprite
    public void paint (Graphics2D gfx)
    {
        if (_legal) {
            Composite ocomp = gfx.getComposite();
            gfx.setComposite(_comp);
            super.paint(gfx);
            gfx.setComposite(ocomp);
        }
    }

    protected AlphaComposite _comp;
    protected boolean _legal = false;
}
