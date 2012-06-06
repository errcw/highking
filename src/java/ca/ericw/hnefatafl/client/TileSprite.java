package ca.ericw.hnefatafl.client;

import ca.ericw.hnefatafl.data.HnefataflObject;
import ca.ericw.hnefatafl.util.IsometricSprite;

import com.threerings.media.sprite.action.CommandSprite;
import com.threerings.media.sprite.action.DisableableSprite;
import com.threerings.media.sprite.action.HoverSprite;

/**
 * Graphical representation of a space on the Hnefatafll board.
 */
public class TileSprite extends IsometricSprite 
    implements CommandSprite, HoverSprite, DisableableSprite {
    
    /**
     * Creates a new tile sprite at the given row and column location.
     * 
     * @param view board view responsible for this tile sprite
     * @param row row position of the tile
     * @param col column position of the tile
     */
    public TileSprite(HnefataflBoardView view, int row, int col) {
        _view = view;
        _position = new HnefataflObject.BoardLocation(row, col);
        
        setMirage(_view.getTileImage(_position));
        setIsoLocation(row, col);
        setIsoOffset(0, 0);
        setIsoLevel(0);
    }
    
    // from interface HoverableSprite
    public void setHovered(boolean hovered) {
        _view.setTileHovered(_position, hovered);
    }
    
    /**
     * Returns true if the board location this tile sprite represents is a valid
     * move for the currently selected piece. Upon being enabled this tile will
     * display a movement cursor when hovered over and will generate movement
     * events when clicked.
     */
    public boolean isEnabled() {
        return _view.getController().isValidMove(_position);
    }
    
    /**
     * Returns the command to invoke when this sprite is clicked.
     */
    public String getActionCommand() {
        return HnefataflController.MOVE_ACTION;
    }
    
    /**
     * Returns the board location this tile represents.
     */
    public Object getCommandArgument() {
        return _position;
    }

    /** Row and column position of this tile. */
    protected HnefataflObject.BoardLocation _position;
    
    /** Board view responsible for this tile. */
    protected HnefataflBoardView _view;
    
}
