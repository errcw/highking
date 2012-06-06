package ca.ericw.hnefatafl.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sits on top of the game object (i.e., raw game data) to provide the mechanics
 * of the game. Governs game setup, turn sequence, move validity, piece attack
 * and removal, and end game conditions.
 */
public class HnefataflMechanics {

    /**
     * Creates a new Hnefatafll mechanics object to control the game represented
     * by the given game object.
     */
    public HnefataflMechanics(HnefataflObject gameobj) {
        _gameobj = gameobj;
        _size = 0;
    }
    
    /**
     * Updates the mechanics' internal representation of the game state to match
     * that available from the game object.
     */
    public void updateState() {
        if (_size != _gameobj.boardSize) {
            _size = _gameobj.boardSize;
            _board = new int[_size * _size];
        }
        Arrays.fill(_board, -1);
        for(HnefataflObject.Piece p : _gameobj.pieces) {
            _board[p.row * _size + p.col] = p.colour;
        }
    }
    
    /**
     * Returns the index into the {@link HnefataflObject#players} array of the
     * player to take the first turn.
     */
    public int getFirstTurnHolderIndex() {
        return _gameobj.getPlayerIdxOfColour(HnefataflObject.BLACK);
    }
    
    /**
     * Returns the index into the {@link HnefataflObject#players} array of the
     * player to whom control should transition.
     */
    public int getNextTurnHolderIndex(int curTurnHolderIdx) {
        return 1 - curTurnHolderIdx;
    }
    
    /**
     * Returns an index into the {@link HnefataflObject#players} array for the
     * winner of the game. Returns -1 if the game has not yet been won.
     */
    public int getWinningPlayer() {
        return _gameobj.getPlayerIdxOfColour(_winner);
    }
    
    /**
     * Sets up a game ofHnefataflfl. Sets the board size and places all the pieces
     * on the board.
     * @param configuration configuration array from {@link HnefataflConfigurations}
     */
    public void setupGame(int[] config) {
        // set up the board size
        _gameobj.setBoardSize(config[0]);
        
        // set up the pieces
        for (int i = 1; i < config.length; i += 3) {
            HnefataflObject.Piece piece = new HnefataflObject.Piece();
            piece.row = config[i];
            piece.col = config[i+1];
            piece.colour = config[i+2];
            piece.isKing = (i == config.length-3);
            
            _gameobj.placePiece(piece);
        }
        
        // update our internal state
        updateState();
    }
    
    /**
     * Returns true if the given colour has valid moves available, false if not.
     */
    public boolean hasValidMoves(int colour) {
        // check every piece of the given colour
        for (HnefataflObject.Piece piece : _gameobj.pieces) {
            if (piece.colour == colour && hasValidMove(piece)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if the given piece has a valid move available to it.
     */
    public boolean hasValidMove(HnefataflObject.Piece piece) {
        // check validity of movement to every orthogonal space
        for (int loc = 0; loc < _size; loc++) {
            HnefataflObject.BoardLocation locR = new HnefataflObject.BoardLocation(loc, piece.col);
            HnefataflObject.BoardLocation locC = new HnefataflObject.BoardLocation(piece.row, loc);
            
            if (isValidMove(piece, locR) || isValidMove(piece, locC)) {
                // as soon as we've found a valid move we're done
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if a board location is a valid move for the given piece.
     */
    public boolean isValidMove(HnefataflObject.Piece piece, HnefataflObject.BoardLocation destination) {
        // check that the location is valid and no piece is at the destination
        if (getColourAt(destination.row, destination.col) == -2 ||
            getColourAt(destination.row, destination.col) != -1) {
            return false;
        }
        
        // check for orthogonal movement
        if (piece.row != destination.row && piece.col != destination.col) {
            return false;
        }
        
        // only the King may sit on the throne or move to a corner (and rightfully so! =P)
        if ((isThrone(destination) || isCorner(destination)) && !piece.isKing) {
            return false;
        }
        
        // check for a clear path
        int dr = destination.row > piece.row ? 1 : (destination.row < piece.row ? -1 : 0);
        int dc = destination.col > piece.col ? 1 : (destination.col < piece.col ? -1 : 0); 
        
        if (dr != 0) {
            // check along the rows between this piece and its destination
            for (int row = piece.row + dr; row != destination.row; row += dr) {
                if (getColourAt(row, piece.col) != -1) {
                    return false;
                }
            }
        } else if (dc != 0) {
            // check along the columns between this piece and its destination
            for (int col = piece.col + dc; col != destination.col; col += dc) {
                if (getColourAt(piece.row, col) != -1) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Moves a game piece to a new location. The new state, along with any
     * pieces destroyed by the move, is reflected in the game object.
     * <em>Note:</em> This method does not check that the move is valid.
     */
    public void movePiece(HnefataflObject.Piece piece, HnefataflObject.BoardLocation destination) {
        List<HnefataflObject.Piece> capturedPieces = new ArrayList<HnefataflObject.Piece>();
        
        // white wins if the King successfully escapes to a corner tile
        if (piece.isKing && isCorner(destination)) {
            _winner = HnefataflObject.WHITE;
        }
        
        // look for captured pieces
        for (int dir = 0; dir < 4; dir++) {
            int r = destination.row + CAPTURE_CHECK_DR[dir];
            int c = destination.col + CAPTURE_CHECK_DC[dir];
            int farr = destination.row + 2 * CAPTURE_CHECK_DR[dir];
            int farc = destination.col + 2 * CAPTURE_CHECK_DC[dir];
            
            // find the neighbouring piece in this direction
            HnefataflObject.Piece neighbour = getPieceAt(r, c);
            
            if (neighbour != null) {
                if (neighbour.getKey() == piece.getKey()) {
                    // no sense comparing with ourselves; this space will be empty
                    continue;
                    
                } else if (neighbour.isKing && piece.colour == HnefataflObject.BLACK) {
                    // Kings require far more elaborate capturing moves
                    if (isKingCapturable(neighbour, destination)) {
                        capturedPieces.add(neighbour);
                        _winner = HnefataflObject.BLACK;
                    }
                    
                } else if (neighbour.colour == getOpposingColour(piece.colour)) {
                    // we are beside an opposing piece
                    int farneighbour = getColourAt(farr, farc);
                    HnefataflObject.BoardLocation farloc = new HnefataflObject.BoardLocation(farr, farc);
                    
                    // and have captured it if we have bracketed it with an ally or a corner
                    if (farneighbour == piece.colour || isCorner(farloc)) {
                        capturedPieces.add(neighbour);
                    }
                    
                }
            }
        }
        
        // move the piece to its new location
        piece.row = destination.row;
        piece.col = destination.col;
        _gameobj.updatePieces(piece);
        
        // remove any pieces we captured
        for (HnefataflObject.Piece capd : capturedPieces) {
            _gameobj.removeFromPieces(capd.getKey());
        }
    }
    
    /**
     * Returns true if the given board position is a corner.
     */
    public boolean isCorner(HnefataflObject.BoardLocation loc) {
        return (loc.row == 0 && loc.col == 0) ||
               (loc.row == 0 && loc.col == _size - 1) ||
               (loc.row == _size - 1 && loc.col == 0) || 
               (loc.row == _size - 1 && loc.col == _size - 1);
    }
    
    /**
     * Returns true if the given board position is the throne.
     */
    public boolean isThrone(HnefataflObject.BoardLocation loc) {
        return (loc.row == _size / 2) &&
               (loc.col == _size / 2);
    }
    
    /**
     * Returns the colour of the opponent to the colour given.
     */
    public int getOpposingColour(int colour) {
        return (colour == HnefataflObject.BLACK) ? HnefataflObject.WHITE : HnefataflObject.BLACK;
    }
    
    /**
     * Returns the colour of the piece at the given row and column location, -1
     * if the space is unoccupied, or -2 if the location is invalid.
     */
    protected int getColourAt(int row, int col) {
        if (row < 0 || row >= _size || col < 0 || col >= _size) {
            return -2;
        } else {
            return _board[row * _size + col];
        }
    }
    
    /**
     * Returns the piece object for the piece occupying the given location, or
     * null if the space is empty or invalid.
     */
    protected HnefataflObject.Piece getPieceAt(int row, int col) {
        for (HnefataflObject.Piece p : _gameobj.pieces) {
            if (row == p.row && col == p.col) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Returns true if the given king piece is in a capturable position when
     * a black piece is moved to the given blocker position.
     * <p>
     * A king may only be captured by being surrounded. For the purposes of
     * capture, we consider the throne, corners, and edges to contibute in
     * the same way as an opposing piece.
     */
    protected boolean isKingCapturable(HnefataflObject.Piece king, HnefataflObject.BoardLocation blocker) {
        for (int dir = 0; dir < 4; dir++) {
            int r = king.row + CAPTURE_CHECK_DR[dir];
            int c = king.col + CAPTURE_CHECK_DC[dir];
            HnefataflObject.BoardLocation loc = new HnefataflObject.BoardLocation(r, c);
            
            if (!isCorner(loc) &&
                !isThrone(loc) &&
                !(blocker.row == r && blocker.col == c) &&
                getColourAt(r, c) != HnefataflObject.BLACK &&
                getColourAt(r, c) != -2)
            {
                // the king is free on this side, and so is not capturable
                return false;
            }
        }
        
        return true;
    }
    
    /** Game object on which we run the game logic. */
    protected HnefataflObject _gameobj;
    
    /** Size of the game board. */
    protected int _size;
    
    /** Board state as a row-major array. */
    protected int[] _board;
    
    /** Colour of the winner of the game, or -1 if no winner has yet been determined. */
    protected int _winner = -1;
    
    /** Direcions in which to check for captured pieces on after a piece is moved. */
    protected static final int[] CAPTURE_CHECK_DR = {1, 0, -1, 0};
    protected static final int[] CAPTURE_CHECK_DC = {0, 1, 0, -1};
}
