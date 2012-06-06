package ca.ericw.hnefatafl.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.ericw.hnefatafl.client.PieceSprite.State;
import ca.ericw.hnefatafl.data.HnefataflObject;
import ca.ericw.hnefatafl.util.AlphaImageSprite;

import com.samskivert.swing.Label;
import com.threerings.media.VirtualMediaPanel;
import com.threerings.media.animation.FloatingTextAnimation;
import com.threerings.media.image.BufferedMirage;
import com.threerings.media.image.Mirage;
import com.threerings.media.sprite.PathObserver;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.Path;
import com.threerings.parlor.media.ScoreAnimation;
import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

/**
 * Displays the main game interface (the board and pieces).
 */
public class HnefataflBoardView extends VirtualMediaPanel implements PlaceView {

    /**
     * Constructs a view which will initialize itself and prepare to display the game board.
     */
    public HnefataflBoardView(ToyBoxContext ctx, HnefataflController ctrl) {
        super(ctx.getFrameManager());
        _ctx = ctx;
        _ctrl = ctrl;
        loadResources();
    }

    // from interface PlaceView
    public void willEnterPlace(PlaceObject plobj) {
        _gameobj = (HnefataflObject)plobj;
        
        // hook up component resize listener
        addComponentListener(_resizeListener = getComponentListener());
    }

    // from interface PlaceView
    public void didLeavePlace(PlaceObject plobj) {
        // unhook the resize listener
        removeComponentListener(_resizeListener);
        
        _gameobj = null;
    }

    /**
     * Sets up the view after the game has started.
     */
    public void initView() {
        // remove any existing sprites
        clearSprites();
        
        // ensure the sprites are centred when we add them
        centreBoardView();
        generateBackground();
        
        // add the board tiles
        for (int r = 0; r < _gameobj.boardSize; r++) {
            for (int c = 0; c < _gameobj.boardSize; c++) {
                addSprite(new TileSprite(this, r, c));
            }
        }
        
        // add piece selector sprites
        PieceSelectorSprite pss1;
        addSprite(pss1 = new PieceSelectorSprite(this));
        _pieceSelector = new SelectorCursor(pss1);
        
        PieceSelectorSprite pss2;
        addSprite(pss2 = new PieceSelectorSprite(this));
        _pieceSelectorAlt = new SelectorCursor(pss2);
        
        // add the pieces
        for (HnefataflObject.Piece piece : _gameobj.pieces) {
            addPieceSprite(piece);
        }
        
        // force a repaint to allow our parent container to redraw anything overlapping
        // this board view, otherwise the sprites may corrupt the parent panel
        getParent().repaint();
    }
    
    /**
     * Indicates visually that the given piece has a selection cursor hovered
     * over to show a piece that may be selected.
     */
    public void setPieceHovered(HnefataflObject.Piece piece, boolean hovered) {
        PieceSprite sprite = _pieceSprites.get(piece.getKey());
        SelectorCursor cursor = (_pieceSelected == null) ? _pieceSelector : _pieceSelectorAlt;
        if (hovered) {
            cursor.hover(sprite._piece.row, sprite._piece.col);
        } else {
            cursor.deselect();
        }
    }
    
    /**
     * Indicates visually that the given piece is selected. The selected piece
     * will also be used to setup the piece movement cursor.
     */
    public void setPieceSelected(HnefataflObject.Piece piece, boolean selected) {
        // clear the previously selected sprites's state
        if (_pieceSelected != null) {
            _pieceSelected._state = State.SELECTABLE;
            _pieceSelected = null;
        }
        
        // clear any previous tile selector
        if (_tileSelector != null) {
            removeSprite(_tileSelector.sprite());
            _tileSelector = null;
        }
        
        if (selected) {
            PieceSprite sprite = _pieceSprites.get(piece.getKey());
            
            // let the sprite know it was selected
            sprite._state = State.SELECTED;
            _pieceSelected = sprite;
            
            // and have the piece selection cursor reflect that fact
            _pieceSelector.select(sprite._piece.row, sprite._piece.col);
            _pieceSelectorAlt.deselect();
            
            // now generate the appropriate tile selector
            PieceSprite selector = new PieceSprite(this, piece);
            selector._state = State.UNSELECTABLE;
            addSprite(selector);
            
            _tileSelector = new SelectorCursor(selector);
            _tileSelector.deselect();
        } else {
            // clear all the selection mechanisms
            _pieceSelector.deselect();
            _pieceSelectorAlt.deselect();
        }
    }
    
    /**
     * Indicates visually that the given board tile has a piece cursor hovered
     * over to show a tile to which the selected piece may be moved.
     */
    public void setTileHovered(HnefataflObject.BoardLocation tilePos, boolean hovered) {
        if (_tileSelector != null) {
            if (hovered) {
                _tileSelector.hover(tilePos.row, tilePos.col);
            } else {
                _tileSelector.deselect();
            }
        }
    }
    
    /**
     * Animates the given piece moving from its current location on the board to
     * a new location. For the purposes of move validity checking the piece is
     * considered to be moved immediately.
     */
    public void movePiece(HnefataflObject.Piece piece) {
        PieceSprite sprite = _pieceSprites.get(piece.getKey());
        
        // have all subsequent captures wait until this move is complete
        _pathTrigger = new PathTrigger(sprite);
        
        // update the sprite to reflect the new piece state
        sprite.updatePiece(piece);
    }
    
    /**
     * Removes the given piece from the board. If there are any piece movement
     * animations in progress, the removal animation is queued and executed upon
     * completion.
     */
    public void removePiece(HnefataflObject.Piece piece) {
        final PieceSprite sprite = _pieceSprites.get(piece.getKey());
        if (_pathTrigger != null) {
            _pathTrigger.addEvent(new Runnable() {
                public void run() { sprite.setCaptured(); }
            });
        } else {
            sprite.setCaptured();
        }
    }
    
    /**
     * Floats the supplied text over the board.
     */
    public void displayFloatingText(String text) {
        Label label = ScoreAnimation.createLabel(text, Color.WHITE, _floatingTextFont, this);
        int halfheight = (_stdTile.getHeight() * _gameobj.boardSize) / 2 - 20;
        final FloatingTextAnimation floater = new FloatingTextAnimation(label, 0, halfheight, 2500L);
        
        if (_pathTrigger != null) {
            _pathTrigger.addEvent(new Runnable() {
                public void run() { addAnimation(floater); }
            });
        } else {
            addAnimation(floater);
        }
    }
    
    /**
     * Returns a component listener that will adapt this board view based
     * on the size of the component displaying it.
     */
    protected ComponentListener getComponentListener() {
        return new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                centreBoardView();
            }
        };
    }
    
    /**
     * Centres the board inside this board view component using the virtual
     * coordinate system.
     * <b>Note:</b> The calculations assume the board dimensions are square. 
     */
    protected void centreBoardView() {
        // only bother to recentre the view when we have things to centre
        if (_gameobj != null) {
            Dimension size = getSize();
            int ox, oy;
            
            ox = (size.width / 2) - (_stdTile.getWidth() / 2) - 10/*fudge factor*/;
            oy = (size.height / 2) - (_stdTile.getHeight() * _gameobj.boardSize / 2);
            setViewLocation(-ox, -oy);
        }
    }
    
    /**
     * Generates and adds the sprites to paint the board's background.
     */
    protected void generateBackground() {
        int halfwidth = _stdTile.getWidth() * _gameobj.boardSize / 2;
        int halfheight = (_stdTile.getHeight() * _gameobj.boardSize) / 2;
        
        // generate background sprites underneath the game board
        for (int x = -halfwidth; x < 2*halfwidth; x += _background.getWidth()) {
            for (int y = -50; y < halfheight; y += _background.getHeight()) {
                AlphaImageSprite b = new AlphaImageSprite();
                b.setMirage(_background);
                b.setLocation(x, y);
                b.setRenderOrder(-1);
                addSprite(b);
            }
        }
    }
    
    /**
     * Gets the preferred size of this board view..
     */
    public Dimension getPreferredSize() {
        return new Dimension(400, 200);
    }
    
    /**
     * Returns the controller managing this board view.
     */
    public HnefataflController getController() {
        return _ctrl;
    }

    /**
     * Returns the tile image appropriate for the given board position.
     */
    protected Mirage getTileImage(HnefataflObject.BoardLocation loc) {
        if (_ctrl.logic.isCorner(loc)) {
            return _cornerTile;
        } else if (_ctrl.logic.isThrone(loc)) {
            return _throneTile;
        } else {
            return _stdTile;
        }
    }
    
    /**
     * Returns the piece image appropriate for the given piece object.
     */
    protected Mirage getPieceImage(HnefataflObject.Piece piece) {
        if (piece.isKing) {
            return _kingPiece;
        } else if (piece.colour == HnefataflObject.WHITE) {
            return _whitePiece;
        } else {
            return _blackPiece;
        }
    }
    
    /**
     * Returns the piece selector image.
     */
    protected Mirage getSelectorImage() {
        return _selector;
    }
    
    /**
     * Loads the image resources required by this board view.
     */
    protected void loadResources() {
        _stdTile = new BufferedMirage(_ctx.loadImage("media/tile.png"));
        _cornerTile = new BufferedMirage(_ctx.loadImage("media/tile_corner.png"));
        _throneTile = new BufferedMirage(_ctx.loadImage("media/tile_throne.png"));
        _blackPiece = new BufferedMirage(_ctx.loadImage("media/black.png"));
        _whitePiece = new BufferedMirage(_ctx.loadImage("media/white.png"));
        _kingPiece = new BufferedMirage(_ctx.loadImage("media/king.png"));
        _selector = new BufferedMirage(_ctx.loadImage("media/selector.png"));
        _background = new BufferedMirage(_ctx.loadImage("media/background.png"));
    }
    
    /**
     * Adds a sprite representation of the given piece to the board.
     */
    protected void addPieceSprite(HnefataflObject.Piece piece) {
        PieceSprite sprite = new PieceSprite(this, piece);
        _pieceSprites.put(piece.getKey(), sprite);
        addSprite(sprite);
    }
    
    /**
     * Triggers a series of events when an observed sprite finishes its path.
     * Events are executed in the order they are queued.
     */
    protected class PathTrigger implements PathObserver {
        
        public PathTrigger(Sprite sprite) {
            sprite.addSpriteObserver(this);
        }
        
        /**
         * Queues the given Runnable to be executed as soon as the path of the
         * observed sprite is completed.
         */
        public void addEvent(Runnable runnable) {
            _torun.add(runnable);
        }
        
        // from interface PathObserver
        public void pathCancelled (Sprite sprite, Path path) {
            movementComplete(sprite);
        }

        // from interface PathObserver
        public void pathCompleted (Sprite sprite, Path path, long when) {
            movementComplete(sprite);
        }
        
        protected void movementComplete(Sprite sprite) {
            // unhook the path observer
            sprite.removeSpriteObserver(this);
            
            // remove all the pieces
            for (Runnable run : _torun) {
                run.run();
            }
            
            // remove ourselves from the view
            _pathTrigger = null;
        }
        
        /** List of sprites to capture once the movement animation is complete. */
        protected List<Runnable> _torun = new ArrayList<Runnable>();
    }
    
    /** Provides access to client services. */
    protected ToyBoxContext _ctx;
    
    /** Game controller for sending user actions to the server. */
    protected HnefataflController _ctrl;

    /** A reference to our game object. */
    protected HnefataflObject _gameobj;
    
    /** Mapping from piece id to piece sprite */
    protected Map<Comparable, PieceSprite> _pieceSprites = new HashMap<Comparable, PieceSprite>();
    
    /** Piece currently selected by the user. */
    protected PieceSprite _pieceSelected;
    
    /** Cursors to indicate user selections. */
    protected SelectorCursor _pieceSelector;
    protected SelectorCursor _pieceSelectorAlt;
    protected SelectorCursor _tileSelector;
    
    /** Image resources for manifold board elements. */
    protected Mirage _stdTile;
    protected Mirage _cornerTile;
    protected Mirage _throneTile;
    protected Mirage _blackPiece;
    protected Mirage _whitePiece;
    protected Mirage _kingPiece;
    protected Mirage _selector;
    protected Mirage _background;
    protected Font _floatingTextFont = new Font("Helvetica", Font.BOLD, 48);
    
    /** User interface resize listener. */
    protected ComponentListener _resizeListener;
    
    /** Capture blocker to help with piece removal. */
    protected PathTrigger _pathTrigger;
}
