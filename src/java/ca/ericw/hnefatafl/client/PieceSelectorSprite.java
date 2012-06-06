package ca.ericw.hnefatafl.client;

import java.awt.Point;

import ca.ericw.hnefatafl.util.IsometricSprite;

/**
 * Sprite to display the piece selector cursor.
 */
public class PieceSelectorSprite extends IsometricSprite {

    /**
     * Creates a new piece selector sprite.
     * @param view board view responsible for this selector sprite
     */
    public PieceSelectorSprite(HnefataflBoardView view) {
        super( view.getSelectorImage() );
        
        setIsoOffset(IMG_OFFSET.x, IMG_OFFSET.y);
        setIsoLocation(1, 1);
        setIsoLevel(3);
    }
    
    /** Offsets from image origin to tile origin */
    protected static final Point IMG_OFFSET = new Point(-29, 35);

}
