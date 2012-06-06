package ca.ericw.hnefatafl.data;

import com.threerings.io.Streamable;
import com.threerings.presents.dobj.DSet;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.turn.data.TurnGameObject;
import com.threerings.util.Name;

/**
 * Maintains the shared state of the game.
 */
public class HnefataflObject extends GameObject implements TurnGameObject {

    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>boardSize</code> field. */
    public static final String BOARD_SIZE = "boardSize";

    /** The field name of the <code>pieces</code> field. */
    public static final String PIECES = "pieces";

    /** The field name of the <code>turnHolder</code> field. */
    public static final String TURN_HOLDER = "turnHolder";

    /** The field name of the <code>gameCreatorIsBlack</code> field. */
    public static final String GAME_CREATOR_IS_BLACK = "gameCreatorIsBlack";
    // AUTO-GENERATED: FIELDS END
    
    
    /** The index into the {@link #players} array of the black player. */
    public static final int BLACK = 0;
    
    /** The index into the {@link #players} array of the white player. */
    public static final int WHITE = 1;
    
    /** Simple class representing a location on the game board. */
    public static class BoardLocation implements Streamable {
        public int row;
        public int col;
        
        public BoardLocation(int r, int c) {
            row = r;
            col = c;
        }
        
        public BoardLocation() {/*deserialization c'tor*/}
    }
    
    /** A single piece on the board. */
    public static class Piece implements DSet.Entry {
        public int pieceId;
        public int colour;
        public boolean isKing;
        public int row, col;

        public Comparable getKey() {
            return pieceId;
        }
    }

    /** Size of the board. Every Hnefatafll board is square and is of odd length. */
    public int boardSize;
    
    /** The set of pieces dropped into the board. */
    public DSet<Piece> pieces = new DSet<Piece>();

    /** Username of the current turn holder or null. */
    public Name turnHolder;
    
    /** True if the game creator player plays black, false for white. */
    public boolean gameCreatorIsBlack;

    /**
     * Places the given piece on the board.
     */
    public void placePiece(Piece piece) {
        piece.pieceId = _nextPieceId++;
        addToPieces(piece);
    }
    
    /**
     * Returns an index into the {@link players} array of the player who is
     * playing the given colour.
     */
    public int getPlayerIdxOfColour(int colour) {
        if (colour == BLACK) {
            return gameCreatorIsBlack ? 0 : 1;
        } else if (colour == WHITE) {
            return gameCreatorIsBlack ? 1 : 0;
        } else {
            return -1;
        }
    }
    
    /**
     * Returns the colour (either {@link BLACK} or {@link WHITE}) that the
     * given player is playing.
     */
    public int getColourOfPlayer(int player) {
        if (player == 0) {
            return gameCreatorIsBlack ? BLACK : WHITE;
        } else if (player == 1) {
            return gameCreatorIsBlack ? WHITE : BLACK;
        } else {
            return -1;
        }
    }
    
    // from interface TurnGameObject
    public String getTurnHolderFieldName() {
        return TURN_HOLDER;
    }

    // from interface TurnGameObject
    public Name getTurnHolder() {
        return turnHolder;
    }

    // from interface TurnGameObject
    public Name[] getPlayers() {
        return players;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>boardSize</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setBoardSize (int value)
    {
        int ovalue = this.boardSize;
        requestAttributeChange(
            BOARD_SIZE, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.boardSize = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>pieces</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToPieces (HnefataflObject.Piece elem)
    {
        requestEntryAdd(PIECES, pieces, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>pieces</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromPieces (Comparable key)
    {
        requestEntryRemove(PIECES, pieces, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>pieces</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updatePieces (HnefataflObject.Piece elem)
    {
        requestEntryUpdate(PIECES, pieces, elem);
    }

    /**
     * Requests that the <code>pieces</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setPieces (DSet<ca.ericw.hnefatafl.data.HnefataflObject.Piece> value)
    {
        requestAttributeChange(PIECES, value, this.pieces);
        @SuppressWarnings("unchecked") DSet<ca.ericw.hnefatafl.data.HnefataflObject.Piece> clone =
            (value == null) ? null : value.typedClone();
        this.pieces = clone;
    }

    /**
     * Requests that the <code>turnHolder</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setTurnHolder (Name value)
    {
        Name ovalue = this.turnHolder;
        requestAttributeChange(
            TURN_HOLDER, value, ovalue);
        this.turnHolder = value;
    }

    /**
     * Requests that the <code>gameCreatorIsBlack</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setGameCreatorIsBlack (boolean value)
    {
        boolean ovalue = this.gameCreatorIsBlack;
        requestAttributeChange(
            GAME_CREATOR_IS_BLACK, Boolean.valueOf(value), Boolean.valueOf(ovalue));
        this.gameCreatorIsBlack = value;
    }
    // AUTO-GENERATED: METHODS END
    
    /** Used to assign ids to pieces. */
    protected transient int _nextPieceId = 0;

}
