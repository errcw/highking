package ca.ericw.hnefatafl.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import ca.ericw.hnefatafl.util.WhiteTheme;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.MultiLineLabel;

import com.threerings.crowd.client.PlacePanel;
import com.threerings.util.MessageBundle;

import com.threerings.toybox.client.ToyBoxUI;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Contains the primary client interface for the game.
 */
public class HnefataflPanel extends PlacePanel
{
    /** The board view. */
    public HnefataflBoardView boardView;
    
    /**
     * Creates a Hnefatafl panel and its associated interface components.
     */
    public HnefataflPanel(ToyBoxContext ctx, HnefataflController ctrl)
    {
        super(ctrl);
        _ctx = ctx;
        
        // this is used to look up localized strings
        MessageBundle msgs = _ctx.getMessageManager().getBundle("hnefatafl");
        
        // read the image files
        _tiledBack = ctx.loadImage("media/background.png");
        _cornerBack = ctx.loadImage("media/background_corner.png");
        
        // set ourselves up
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // create and add our board view
        add(boardView = new HnefataflBoardView(ctx, ctrl), BorderLayout.CENTER);

        // create a side panel to hold our title and other interfaces
        JPanel sidePanel = GroupLayout.makeVStretchBox(5);
        sidePanel.setOpaque(false);

        // add a huge label for the game name
        MultiLineLabel vlabel = new MultiLineLabel(msgs.get("m.title"));
        vlabel.setFont(ToyBoxUI.fancyFont);
        vlabel.setForeground(Color.WHITE);
        vlabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        vlabel.setAntiAliased(true);
        sidePanel.add(vlabel, GroupLayout.FIXED);
        
        // add the chat, occupant, and rules interfaces
        HnefataflSidePanel side = new HnefataflSidePanel(ctx, ctrl);
        sidePanel.add(side);

        // add our side panel to the main display
        add(sidePanel, BorderLayout.EAST);
    }
    
    /**
     * Sets the current Swing theme to better match the artwork in the game.
     * This change is permanent; any Swing components created after displayed
     * following <code>initPanel</code> will use the new UI style. To undo the
     * damage call {@link #deinitPanel()}.
     */
    public void initPanel() {
        // save the old theme to be reset later
        _oldTheme = MetalLookAndFeel.getCurrentTheme();
        _oldLookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        
        // use our fancy new whiteness
        MetalLookAndFeel.setCurrentTheme(new WhiteTheme());
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) { System.err.println("Borked on UI switch"); }
        
        // and force the changes to propogate
        SwingUtilities.updateComponentTreeUI(this);
    }
    
    /**
     * Undoes the UI changes executed during {@link #initPanel}.
     */
    public void deinitPanel() {
        if (_oldTheme != null) {
            MetalLookAndFeel.setCurrentTheme(_oldTheme);
        }
        if (_oldLookAndFeel != null) {
            try {
                UIManager.setLookAndFeel(_oldLookAndFeel);
            } catch (Exception e) { System.err.println("Borked on UI revert"); }
        }
        SwingUtilities.updateComponentTreeUI(this);
    }
    
    @Override // from JComponent
    public void paintComponent(Graphics g) {
        Rectangle cbounds = getBounds();
        
        // tile the background image over the bounds of the component
        for (int x = 0; x < cbounds.getWidth(); x += _tiledBack.getWidth()) {
            for (int y = 0; y < cbounds.getHeight(); y += _tiledBack.getHeight()) {
                g.drawImage(_tiledBack, x, y, null);
            }
        }
    }
    
    @Override // from JComponent
    public void paint(Graphics g) {
        super.paint(g);
        
        // ensure that the corner image is drawn on top of absolutely everything
        g.drawImage(_cornerBack, 0, 0, null);
    }
    
    /** Provides access to various client services. */
    protected ToyBoxContext _ctx;
    
    /** Tiled background image. */
    private BufferedImage _tiledBack;
    
    /** Corner "flair" background image. NB: must exceed minimum flair requirements. */
    private BufferedImage _cornerBack;
    
    /** References to the old UI theme information so we can reset everything properly. */
    protected MetalTheme _oldTheme = null;
    protected String _oldLookAndFeel = null;
}
