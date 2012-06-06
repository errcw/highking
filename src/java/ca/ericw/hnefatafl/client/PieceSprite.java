package ca.ericw.hnefatafl.client;

import java.awt.Point;

import com.threerings.media.sprite.action.CommandSprite;
import com.threerings.media.sprite.action.DisableableSprite;
import com.threerings.media.sprite.action.HoverSprite;

import ca.ericw.hnefatafl.data.HnefataflObject;
import ca.ericw.hnefatafl.util.IsometricSprite;

/**
 * Graphical representation of a piece on the Hnefatafll board.
 */
public class PieceSprite extends IsometricSprite
    implements CommandSprite, HoverSprite, DisableableSprite {
    
    /**
     * Piece sprite state. The selctable state means this sprite may be selected 
     * by the appropriate player. The selected state means the piece this sprite
     * represents has been selected to be moved. The unselectable state means
     * this piece sprite cannot respond to user input.
     */
    protected enum State {
        SELECTABLE(HnefataflController.SELECT_ACTION),
        SELECTED(HnefataflController.DESELECT_ACTION),
        UNSELECTABLE("NullAction");
        
        State(String cmd) {
            command = cmd;
        }
        
        public final String command;
    };
    
    /**
     * Creates a piece sprite.
     * 
     * @param view board view responsible for this piece sprite
     * @param pieceHnefataflfl piece this sprite represents
     */
    public PieceSprite(HnefataflBoardView view, HnefataflObject.Piece piece) {
        _view = view;
        _piece = piece;
        
        // get the image offset
        Point offset = (_piece.isKing) ? KING_OFFSET : KNIGHT_OFFSET;
        
        setMirage(_view.getPieceImage(_piece));
        setIsoLocation(_piece.row, _piece.col);
        setIsoOffset(offset.x, offset.y);
        setIsoLevel(2);
    }
    
    /**
     * Updates this sprite to reflect the state of the given piece. If this
     * sprite is in a different location than the piece, it will animate itself
     * moving to its new position.
     */
    public void updatePiece(HnefataflObject.Piece piece) {
        if (_piece.row != piece.row || _piece.col != piece.col) {
            move( getIsoPath(piece.row, piece.col) );
        }
        _piece = piece;
    }
    
    /**
     * Animates the piece this sprite represents being captured.
     */
    public void setCaptured() {
        _state = State.UNSELECTABLE;
        animateFade(0f, 1500L);
    }
    
    /**
     * Invoked to notify this sprite of its hovered status. If the sprite is
     * selectable, it notifies its parent board view that it is hovered and
     * should likely display something to reflect this fact.
     */
    public void setHovered(boolean hovered) {
        if (_state == State.SELECTABLE) {
            _view.setPieceHovered(_piece, hovered);
        }
    }
    
    /**
     * Returns true if this piece is controllable.
     */
    public boolean isEnabled() {
        return _view.getController().isControllable(_piece) &&
               !isMoving() &&
               _state != State.UNSELECTABLE;
    }
    
    /**
     * Returns the command to invoke when this sprite is clicked.
     */
    public String getActionCommand() {
        return _state.command;
    }
    
    /**
     * Returns the piece this sprite represents as an argument to the controller.
     */
    public Object getCommandArgument() {
        return _piece;
    }
    
    /*Hnefataflafl piece we're representing */
    protected HnefataflObject.Piece _piece;
    
    /** Board view responsible for this piece */
    protected HnefataflBoardView _view;
    
    /** Current state for rendering and actions */
    protected State _state = State.SELECTABLE;
    
    /** Offsets from image origin to tile origin */
    protected static final Point KNIGHT_OFFSET = new Point(-13, 20);
    protected static final Point KING_OFFSET = new Point(-13, 35);
    
}
