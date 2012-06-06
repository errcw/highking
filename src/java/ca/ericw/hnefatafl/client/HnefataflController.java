package ca.ericw.hnefatafl.client;

import ca.ericw.hnefatafl.data.HnefataflMechanics;
import ca.ericw.hnefatafl.data.HnefataflObject;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.client.GameController;
import com.threerings.parlor.turn.client.TurnGameController;
import com.threerings.parlor.turn.client.TurnGameControllerDelegate;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;
import com.threerings.toybox.util.ToyBoxContext;
import com.threerings.util.Name;

/**
 * Manages the client side mechanics of the game.
 */
public class HnefataflController extends GameController
    implements TurnGameController, SetListener
{
    /** Action performed when a piece is selected. */
    public static final String SELECT_ACTION = "PieceSelected";
    
    /** Action performed when a piece is deselected. */
    public static final String DESELECT_ACTION = "PieceDeselected";
    
    /** Action performed when a piece is moved. */
    public static final String MOVE_ACTION = "PieceMoved";
    
    /** Game mechanics object governing this game. Valid only after game has started. */
    public HnefataflMechanics logic;
    
    /**
     * Creates a new controller to manage a game of Hnefatafll on the client.
     */
    public HnefataflController() {
        addDelegate(_turndel = new TurnGameControllerDelegate(this));
    }
    
	/**
	 * Requests that we leave the game and return to the lobby.
	 */
	public void backToLobby() {
		_ctx.getLocationDirector().moveBack();
	}

	@Override // from PlaceController
	public void willEnterPlace(PlaceObject plobj) {
		super.willEnterPlace(plobj);
        
		// get a casted reference to our game object
		_gameobj = (HnefataflObject)plobj;
        
        // setup the UI
        if (_panel != null) {
            _panel.initPanel();
        }
        
        // watching a game in progress, so sync up
        if (_gameobj.isInPlay()) {
            startGame();
        }
	}

	@Override // from PlaceController
	public void didLeavePlace(PlaceObject plobj) {
		super.didLeavePlace(plobj);

        // tear down the UI
        if (_panel != null) {
            _panel.deinitPanel();
        }
        
		// clear out our game object reference
		_gameobj = null;
	}

	@Override // from PlaceController
	protected PlaceView createPlaceView(CrowdContext ctx) {
        _ctx = (ToyBoxContext)ctx;
                
		_panel = new HnefataflPanel(_ctx, this);
		return _panel;
	}

	@Override // from GameController
	protected void gameDidStart() {
		super.gameDidStart();
        
        startGame();
	}

	@Override // from GameController
	protected void gameDidEnd() {
		super.gameDidEnd();
        
        // find an appropriate end of game message
        boolean draw = _gameobj.isDraw();
        boolean winner = _gameobj.isWinner(_gameobj.getPlayerIndex(_ctx.getUsername()));
        String message = draw ? "m.tie" : (winner ? "m.win" : "m.loss");
        
        // and display it
        _panel.boardView.displayFloatingText(_ctx.xlate("hnefatafl", message));
	}
    
    // from interface TurnGameController
    public void turnDidChange(Name turnHolder) {
        // nothing to see here, move along
    }
    
    // from interface SetListener
    public void entryAdded(EntryAddedEvent event) {
        // ignore the pieces being added
    }
    
    // from interface SetListener
    public void entryUpdated(EntryUpdatedEvent event) {
        if (event.getName().equals(HnefataflObject.PIECES)) {
            // update the game mechanics
            logic.updateState();
            
            // animate the piece moving
            HnefataflObject.Piece moved = (HnefataflObject.Piece)event.getEntry();
            _panel.boardView.movePiece(moved);
        }
    }
    
    // fromInterface SetListener
    public void entryRemoved(EntryRemovedEvent event) {
        if (event.getName().equals(HnefataflObject.PIECES)) {
            // update the game mechanics
            logic.updateState();
            
            // animate the piece being removed
            HnefataflObject.Piece dead = (HnefataflObject.Piece)event.getOldEntry();
            _panel.boardView.removePiece(dead);
        }
    }
    
    /**
     * Invoked when a piece has been selected by the player to be moved.
     */
    public void handlePieceSelected(Object source, HnefataflObject.Piece piece) {
        _panel.boardView.setPieceSelected(piece, true);
        _selectedPiece = piece;
    }
    
    /**
     * Invoked when a piece has been deselected by the player.
     */
    public void handlePieceDeselected(Object source, HnefataflObject.Piece piece) {
        _panel.boardView.setPieceSelected(piece, false);
        _selectedPiece = null;
    }
    
    /**
     * Invoked when a player wants to move the currently selected piece to
     * another point on the board.
     */
    public void handlePieceMoved(Object source, HnefataflObject.BoardLocation destination) {
        if (_selectedPiece != null &&
            logic.isValidMove(_selectedPiece, destination))
        {
            // forward the move to the server for processing
            Object[] moveParams = new Object[]{_selectedPiece, destination};
            _gameobj.manager.invoke("movePiece", moveParams);
            
            // deselect the piece on the board
            _panel.boardView.setPieceSelected(_selectedPiece, false);
        }
    }
    
    /**
     * Initializes this client to being playing or watching a game.
     */
    protected void startGame() {
        // setup the logic
        logic = new HnefataflMechanics(_gameobj);
        logic.updateState();
        
        // ask the board view to initialize itself
        _panel.boardView.initView();
    }
    
    /**
     * Returns true if the given piece is currently controllable.
     */
    protected boolean isControllable(HnefataflObject.Piece piece) {
        return _turndel.isOurTurn() &&
               _gameobj.getPlayerIndex(_ctx.getUsername()) == _gameobj.getPlayerIdxOfColour(piece.colour) &&
               logic.hasValidMove(piece);
    }
    
    /**
     * Returns true if the given board location is a valid move for the
     * currently selected piece. If no piece is selected, this method always
     * returns false.
     */
    protected boolean isValidMove(HnefataflObject.BoardLocation boardLocation) {
        return _selectedPiece != null &&
               logic.isValidMove(_selectedPiece, boardLocation);
    }
    
    /** Provides access to client services. */
    protected ToyBoxContext _ctx;
    
    /** Our game panel. */
    protected HnefataflPanel _panel;

    /** Our game distributed object. */
    protected HnefataflObject _gameobj;
    
    /** Handles our turn based game flow. */
    protected TurnGameControllerDelegate _turndel;
    
    /** Currently selected piece. */
    protected HnefataflObject.Piece _selectedPiece;

}
