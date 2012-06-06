package ca.ericw.hnefatafl.util;

import java.awt.Graphics2D;
import java.awt.Point;


import com.threerings.media.image.Mirage;
import com.threerings.media.util.LinePath;
import com.threerings.media.util.Path;

/**
 * A sprite on a tiled board rendered from an isometric viewpoint.
 * <p>
 * This is class is favoured over the Narya miso package for the simple reason
 * that employing the entire set of isometric scene classes would be all kinds
 * of overkill (to the point where the setup work would exceed the benefit from
 * using the package!).
 */
public abstract class IsometricSprite extends AlphaImageSprite {
    
    /**
     * Creates a new isometric sprite with no image.
     */
    public IsometricSprite() {
    }
    
    /**
     * Creates a new isometric sprite from the specified image.
     */
    public IsometricSprite(Mirage image) {
        setMirage(image);
    }
    
    /**
     * Sets the board location of this sprite. Translates the row and column
     * address into the appropriate cartesian coordinates for rendering.
     */
    public void setIsoLocation(int row, int col) {
        int x = row * ROW_OFFSET.x + col * COL_OFFSET.x;
        int y = row * ROW_OFFSET.y + col * COL_OFFSET.y;
        setLocation(x, y);
    }
    
    /**
     * Sets the offset of the sprite image origin from the tile origin.
     */
    public void setIsoOffset(int ox, int oy) {
        _oxoff = ox;
        _oyoff = oy;
        updateRenderOrigin();
    }
    
    /**
     * Sets the layer on which this isometric sprite sits.
     * @param level isometric layer; positive or zero
     */
    public void setIsoLevel(int level) {
        _level = level;
        setRenderOrder(_oy << _level);
        invalidate();
    }
    
    /**
     * Returns a path from this sprite's current location to the new isometric
     * space location. The path returned will be a {@link LinePath}.
     */
    public Path getIsoPath(int row, int col) {
        int x = _ox, y = _oy;
        int nx = row * ROW_OFFSET.x + col * COL_OFFSET.x;
        int ny = row * ROW_OFFSET.y + col * COL_OFFSET.y;
        long duration = (Math.abs(x - nx) + Math.abs(y - ny)) * PATH_ANIMATION_PIXEL_MS;
        
        return new LinePath(x, y, nx, ny, duration);
    }

    @Override // from Sprite    
    public void paint(Graphics2D gfx) {
        /* To render isometric sprites in the right order we need to move down
         * rows then across columns in isometric space. This, conveniently,
         * corresponds to simple y order cartesian coordinates so we use this
         * to control the render order for the sprite manager.
         */
        if (_renderOrder != _oy << _level) {
            setRenderOrder(_oy << _level);
        }
        super.paint(gfx);
    }
    
    /** Isometric layer */
    protected int _level;
    
    /** Cartesian delta corresponding to moving across columns in isometric space. */
    protected static final Point COL_OFFSET = new Point(40, 20);
    
    /** Cartesian delta corresponding to moving across rows in isometric space. */
    protected static final Point ROW_OFFSET = new Point(-38, 19);
    
    /** Time in millseconds for every pixel to traverse while constructing a path. */
    protected static final int PATH_ANIMATION_PIXEL_MS = 6;
    
}
