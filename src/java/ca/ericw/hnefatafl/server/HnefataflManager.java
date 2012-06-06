package ca.ericw.hnefatafl.server;

import ca.ericw.hnefatafl.data.HnefataflConfigurations;
import ca.ericw.hnefatafl.data.HnefataflMechanics;
import ca.ericw.hnefatafl.data.HnefataflObject;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.turn.server.TurnGameManager;
import com.threerings.parlor.turn.server.TurnGameManagerDelegate;

import com.threerings.toybox.data.ToyBoxGameConfig;

/**
 * Handles the server side of the game.
 */
public class HnefataflManager extends GameManager implements TurnGameManager {

    /**
     * Creates a new Hnefatafll Manager to manage a game ofHnefataflfl.
     */
    public HnefataflManager() {
        addDelegate(_turndel = getTurnGameManagerDelegate());
    }
    
    @Override // from PlaceManager
    public void didInit() {
        super.didInit();

        // get a casted reference to our game configuration
        _gameconf = (ToyBoxGameConfig)_config;
    }

    @Override // from PlaceManager
    public void didStartup() {
        super.didStartup();

        // grab our own casted game object reference
        _gameobj = (HnefataflObject)super._gameobj;

        // create our game mechanics helper
        _logic = new HnefataflMechanics(_gameobj);
    }

    @Override // from PlaceManager
    public void didShutdown() {
        super.didShutdown();

        // null out the two critical references
        _gameobj = null;
        _logic = null;
    }

    @Override // from PlaceManager
    protected PlaceObject createPlaceObject() {
        return new HnefataflObject();
    }

    @Override // from GameManager
    protected void gameWillStart() {
        super.gameWillStart();

        // set up game configuration
        boolean creatorIsBlack = (Boolean)_gameconf.params.get("blackcreator");
        String boardConfig = (String)_gameconf.params.get("boardsetup");
        
        _gameobj.setGameCreatorIsBlack(creatorIsBlack);
        _logic.setupGame(HnefataflConfigurations.getConfiguration(boardConfig));
    }

    @Override // from GameManager
    protected void gameDidEnd() {
        super.gameDidEnd();

        // nothing to do, and, err, nothing good that rhymes with do
    }
    
    // from interface TurnGameManager
    public void turnWillStart () {
        // nothing to see here; move along
    }

    // from interface TurnGameManager
    public void turnDidStart () {
        int turnHolderColour = _gameobj.getColourOfPlayer(_turndel.getTurnHolderIndex());
        
        // if the current player has no valid moves, the game is over
        if (!_logic.hasValidMoves(turnHolderColour)) {
            endGame();
        }
    }

    // from interface TurnGameManager
    public void turnDidEnd () {
        if (_logic.getWinningPlayer() != -1) {
            endGame();
        }
    }

    /**
     * Invoked when a player moves a piece. Ensures the move is valid,
     * determines the new game state, then reports the changes back to the
     * players.
     * 
     * @param player player who moved the piece
     * @param piece piece that was moved
     * @param destination new position of the piece
     */
    public void movePiece(BodyObject player, HnefataflObject.Piece piece,
                                             HnefataflObject.BoardLocation destination) {       
        // make sure it's the turn of the player moving the piece
        int pidx = _turndel.getTurnHolderIndex();
        if (_playerOids[pidx] != player.getOid()) {
            System.err.println("Requested to move piece by non-turn holder " +
                               "[who=" + player.who() +
                               ", turnHolder=" + _gameobj.turnHolder + "].");
            return;
        }
        
        // make sure this is a legal move
        if (!_logic.isValidMove(piece, destination)) {
            System.err.println("Received illegal move request " +
                               "[who=" + player.who() +
                               ", piece=" + piece + ", dest=" + destination + "].");
            return;
        }
        
        // everything's OK, actually carry out the move
        try {
            _gameobj.startTransaction();
            _logic.movePiece(piece, destination);
        } finally {
            _gameobj.commitTransaction();
        }
        
        // end the current turn
        _turndel.endTurn();
    }
    
    /**
     * Returns a new turn game manager delegate to handle turn transitions.
     */
    protected TurnGameManagerDelegate getTurnGameManagerDelegate() {
        return new TurnGameManagerDelegate(this) {
            protected void setFirstTurnHolder() {
                _turnIdx = _logic.getFirstTurnHolderIndex();
            }
            protected void setNextTurnHolder() {
                _logic.updateState();
                _turnIdx = _logic.getNextTurnHolderIndex(_turnIdx);
            }
        };
    }
    
    @Override // from GameManager
    protected void assignWinners (boolean[] winners) {
        super.assignWinners(winners);
        
        // ask the game logic who won; -1 indicates a draw
        int winner = _logic.getWinningPlayer();
       
        winners[0] = (winner == 0 || winner == -1);
        winners[1] = (winner == 1 || winner == -1);
    }

    /** Our game object. */
    protected HnefataflObject _gameobj;

    /** Our game configuration. */
    protected ToyBoxGameConfig _gameconf;
    
    /** Handles our turn based game flow. */
    protected TurnGameManagerDelegate _turndel;
    
    /** Handles our game logic. */
    protected HnefataflMechanics _logic;
    
}
