package ca.ericw.hnefatafl.util;

import javax.swing.UIDefaults;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

/**
 * High-contrast white-black Swing theme.
 */
public class WhiteTheme extends DefaultMetalTheme {

    @Override // from DefaultMetalTheme
    public String getName() {
        return "White";
    }

    private final ColorUIResource primary1 = new ColorUIResource(0, 0, 0);
    private final ColorUIResource primary2 = new ColorUIResource(0, 0, 0);
    private final ColorUIResource primary3 = new ColorUIResource(0, 0, 0);
    private final ColorUIResource highlight = new ColorUIResource(119, 158, 211);

    private final ColorUIResource secondary1 = new ColorUIResource(255, 255, 255);
    private final ColorUIResource secondary2 = new ColorUIResource(119, 158, 211);
    private final ColorUIResource secondary3 = new ColorUIResource(255, 255, 255);

    protected ColorUIResource getPrimary1() { return primary1; } 
    protected ColorUIResource getPrimary2() { return primary2; }
    protected ColorUIResource getPrimary3() { return primary3; }

    protected ColorUIResource getSecondary1() { return secondary1; }
    protected ColorUIResource getSecondary2() { return secondary2; }
    protected ColorUIResource getSecondary3() { return secondary3; }
    
    public ColorUIResource getPrimaryControlHighlight() { return highlight; }
    public ColorUIResource getPrimaryControlShadow() { return getWhite(); }  
    public ColorUIResource getPrimaryControlDarkShadow() { return getWhite(); }  

    public ColorUIResource getFocusColor() { return getBlack(); }
    public ColorUIResource getTextHighlightColor() { return highlight; }
    public ColorUIResource getHighlightedTextColor() { return getWhite(); }
    
    public void addCustomEntriesToTable(UIDefaults table) {
        Border blackLineBorder = new BorderUIResource(new LineBorder( getBlack() ));

        table.put("TextField.border", blackLineBorder);
        table.put("Button.border", blackLineBorder);
        table.put("TabbedPane.border", blackLineBorder);
    }

}
