package ca.ericw.hnefatafl.client;

import ca.ericw.hnefatafl.util.IsometricSprite;

/**
 * An action selection cursor.
 * 
 * A selection cursor can be in three states:
 * <ul>
 * <li><i>Hovering:</i> Indicating a potential selection. The cursor sprite is
 * drawn partially transparent in this state.</li>
 * <li><i>Selecting:</i> Indicating a fixed selection.</li>
 * <li><i>Invisible:</i> Cursor is not visible. The cursor transitions to this
 * state from Hovering or Selecting by fading out.</li>
 * </ul>
 */
public class SelectorCursor {

    /**
     * Creates a new selection cursor using the given sprite.
     */
    public SelectorCursor(IsometricSprite cursor) {
        // store the reference to the cursor sprite
        _cursor = cursor;
        _cursor.setAlpha(0f);
        _cursor.setIsoLocation(-2, -2);
    }

    /**
     * Hovers the selector over the given board location.
     */
    public void hover(int row, int col) {
        _cursor.cancelFade();
        _cursor.setAlpha(0.75f);
        _cursor.setIsoLocation(row, col);
    }
    
    /**
     * Selects the element at the given board location.
     */
    public void select(int row, int col) {
        _cursor.cancelFade();
        _cursor.setAlpha(1f);
        _cursor.setIsoLocation(row, col);
    }
    
    /**
     * Causes this cursor to disappear when the currently selected piece is
     * deselected.
     */
    public void deselect() {
        // fade this cursor out over a quarter second
        _cursor.animateFade(0f, 250L);
    }
    
    /**
     * Returns the sprite used to render this cursor.
     */
    public IsometricSprite sprite() {
        return _cursor;
    }
    
    /** Sprite used to render this cursor. */
    private IsometricSprite _cursor;
}
