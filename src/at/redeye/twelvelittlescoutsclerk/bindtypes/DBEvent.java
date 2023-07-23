package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBDouble;
import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

/**
 *
 * @author martin
 */
public class DBEvent extends DBStrukt {
    
    public static final String EVENT_IDX_SEQUENCE = "EVENT_IDX_SEQUENCE";

    public DBInteger      idx = new DBInteger("idx", "Idx");
    public DBInteger      bz_idx = new DBInteger( "bp_idx" );
    public DBString       name = new DBString("name", "Name", 50 );
    public DBHistory      hist = new DBHistory( "hist" );
    public DBDouble       costs = new DBDouble( "costs" );
    
    public DBEvent()
    {
        super("EVENT");
        
        add(idx);
        add(bz_idx);
        add(name);
        add(hist);
        add(costs);
        
        hist.setTitle(" ");
        
        idx.setAsPrimaryKey();
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBEvent();
    }
    
}
