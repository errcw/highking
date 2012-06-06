package ca.ericw.hnefatafl.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.threerings.media.SafeScrollPane;
import com.threerings.parlor.turn.client.TurnDisplay;
import com.threerings.toybox.client.ChatPanel;
import com.threerings.toybox.client.OccupantList;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;
import com.threerings.util.MessageBundle;

/**
 * Contains standard components for a side panel: chat, occupancy list, and game
 * information.
 */
public class HnefataflSidePanel extends JPanel {

    public HnefataflSidePanel(ToyBoxContext ctx, HnefataflController ctrl) {
        // set ourselves up
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setOpaque(true);
        setBackground(Color.WHITE);
        
        MessageBundle msgs = ctx.getMessageManager().getBundle("hnefatafl");
        ToyBoxGameConfig conf = (ToyBoxGameConfig)ctrl.getPlaceConfig();
        
        // add a turn display
        TurnDisplay turnDisplay = new TurnDisplay();
        turnDisplay.setOpaque(false);
        turnDisplay.setForeground(Color.WHITE);
        turnDisplay.setWinnerText(msgs.get("m.winner"));
        turnDisplay.setDrawText(msgs.get("m.draw"));
        
        Icon black = new ImageIcon(ctx.loadImage("media/black_small.png"));
        Icon white = new ImageIcon(ctx.loadImage("media/white_small.png"));
        Icon turn = new ImageIcon(ctx.loadImage("media/turn_selector.png"));
        
        turnDisplay.setPlayerIcons(
                (Boolean)conf.params.get("blackcreator")
                    ? new Icon[]{black, white}
                    : new Icon[]{white, black});
        turnDisplay.setTurnIcon(turn);
        
        // add tabs
        JTabbedPane pane = new JTabbedPane();
        
        // add a chat panel
        if ((Boolean)conf.params.get("chatenabled")) {
            ChatPanel chat = new ChatPanel(ctx);
            chat.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            pane.addTab(msgs.get("m.chat_header"), chat);
        }
        
        // add a list of people playing and watching the game
        OccupantList occs = new OccupantList(ctx);
        occs.setOpaque(false);
        occs.setBorder(null);
        pane.addTab(msgs.get("m.occupants_header"), occs);
        
        // add a rules page
        JEditorPane rules = new JEditorPane();
        rules.setOpaque(false);
        rules.setEditable(false);
        rules.setContentType("text/html");
        rules.setText(msgs.get("m.help"));
        
        SafeScrollPane scroll = new SafeScrollPane(rules, 50, 0);
        scroll.setBorder(null);
        pane.addTab(msgs.get("m.help_header"), scroll);

        // add a "back to lobby" button
        JButton backBtn = HnefataflController.createActionButton(msgs.get("m.back_to_lobby"), "backToLobby");
        
        // finally, compose the side panel
        add(turnDisplay, BorderLayout.NORTH);
        add(pane, BorderLayout.CENTER);
        add(backBtn, BorderLayout.SOUTH);
    }
    
    @Override // from JComponent
    public Dimension getPreferredSize() {
        return new Dimension(225, 0);
    }
    
}
