package ca.ericw.hnefatafl.data;

import java.util.HashMap;
import java.util.Map;

/**
 * The game of Hnefatafll has many, many variants which involve varying board
 * sizes and initial piece configurations. This class provides a set of
 * configurations available for play.
 * <p>
 * Each configuration is represented as an array of integers. The first integer
 * represents the board size. Every subsequent group of three numbers represents
 * a piece's row, column, and owner in that order. The very last entry is that
 * of the White King.
 */
public class HnefataflConfigurations {
    
    /** Scottish variant of Ard-Ri, translated as "High King" */
    protected static final int[] ARD_RI =
    {
        7, // Ard-Ri is played on a 7x7 board
        
        /* Attackers */
        6, 2, HnefataflObject.BLACK,
        6, 3, HnefataflObject.BLACK,
        6, 4, HnefataflObject.BLACK,
        5, 3, HnefataflObject.BLACK,
        
        0, 2, HnefataflObject.BLACK,
        0, 3, HnefataflObject.BLACK,
        0, 4, HnefataflObject.BLACK,
        1, 3, HnefataflObject.BLACK,
        
        2, 0, HnefataflObject.BLACK,
        3, 0, HnefataflObject.BLACK,
        4, 0, HnefataflObject.BLACK,
        3, 1, HnefataflObject.BLACK,
        
        2, 6, HnefataflObject.BLACK,
        3, 6, HnefataflObject.BLACK,
        4, 6, HnefataflObject.BLACK,
        3, 5, HnefataflObject.BLACK,
        
        /* Defenders */
        2, 2, HnefataflObject.WHITE,
        2, 3, HnefataflObject.WHITE,
        2, 4, HnefataflObject.WHITE,
        4, 2, HnefataflObject.WHITE,
        4, 3, HnefataflObject.WHITE,
        4, 4, HnefataflObject.WHITE,
        3, 2, HnefataflObject.WHITE,
        3, 4, HnefataflObject.WHITE,
        
        /* King */
        3, 3, HnefataflObject.WHITE,
    };
    
    /** Finnish variant of Tablut */
    protected static final int[] TABLUT =
    {
        9, // Tablut is played on an 9x9 board
        
        /* Attackers */
        8, 3, HnefataflObject.BLACK,
        8, 4, HnefataflObject.BLACK,
        8, 5, HnefataflObject.BLACK,
        7, 4, HnefataflObject.BLACK,
        
        0, 3, HnefataflObject.BLACK,
        0, 4, HnefataflObject.BLACK,
        0, 5, HnefataflObject.BLACK,
        1, 4, HnefataflObject.BLACK,
        
        3, 0, HnefataflObject.BLACK,
        4, 0, HnefataflObject.BLACK,
        5, 0, HnefataflObject.BLACK,
        4, 1, HnefataflObject.BLACK,
        
        3, 8, HnefataflObject.BLACK,
        4, 8, HnefataflObject.BLACK,
        5, 8, HnefataflObject.BLACK,
        4, 7, HnefataflObject.BLACK,
        
        /* Defenders */
        4, 2, HnefataflObject.WHITE,
        4, 3, HnefataflObject.WHITE,
        2, 4, HnefataflObject.WHITE,
        3, 4, HnefataflObject.WHITE,
        4, 5, HnefataflObject.WHITE,
        4, 6, HnefataflObject.WHITE,
        5, 4, HnefataflObject.WHITE,
        6, 4, HnefataflObject.WHITE,
        
        /* King */
        4, 4, HnefataflObject.WHITE,
    };
    
    /** Modern variant of my own devising */
    protected static final int[] ERRK =
    {
        7, // Errk is played on an 7x7 board
        
        /* Attackers */
        1, 1, HnefataflObject.BLACK,
        2, 1, HnefataflObject.BLACK,
        
        1, 4, HnefataflObject.BLACK,
        1, 5, HnefataflObject.BLACK,
        
        5, 5, HnefataflObject.BLACK,
        4, 5, HnefataflObject.BLACK,
        
        5, 1, HnefataflObject.BLACK,
        5, 2, HnefataflObject.BLACK,
        
        /* Defenders */
        2, 3, HnefataflObject.WHITE,
        4, 3, HnefataflObject.WHITE,
        3, 2, HnefataflObject.WHITE,
        3, 4, HnefataflObject.WHITE,
        
        /* King */
        3, 3, HnefataflObject.WHITE,
    };
    
    /**
     * Returns the board configuration matching the given name. If no such
     * configuration exists, this method returns the default Ard-Ri config.
     */
    public static int[] getConfiguration(String name) {
        int[] config = _configs.get(name);
        return config != null ? config : ARD_RI;
    }
    
    /** Mapping from configuration name to configuration. */
    protected static Map<String, int[]> _configs;
    
    static {
        _configs = new HashMap<String, int[]>();
        _configs.put("ardri", ARD_RI);
        _configs.put("tablut", TABLUT);
        _configs.put("errk", ERRK);
    }
    
}
