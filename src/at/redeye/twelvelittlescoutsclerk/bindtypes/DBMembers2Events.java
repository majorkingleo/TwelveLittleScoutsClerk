package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

/**
 *
 * @author martin
 */
public class DBMembers2Events extends DBStrukt
{
    public static final String MEMBERS2EVENTS_IDX_SEQUENCE = "M2E_IDX_SEQUENCE";
    
    public DBInteger      idx = new DBInteger("idx", "Idx");
    public DBInteger      member_idx = new DBInteger("member_idx", "Member Idx");
    public DBInteger      event_idx = new DBInteger("event_idx", "Event Idx");
    public DBInteger      bp_idx = new DBInteger( "bp_idx" );
    public DBHistory      hist = new DBHistory( "hist" );
    
    public DBMembers2Events()
    {
        super("MEMBERS2EVENTS");
        
        add(idx);        
        add(member_idx);
        add(event_idx);
        add(bp_idx);        
        add(hist);
                
        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();
        hist.setTitle(" ");
        
        setVersion(1);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBMembers2Events();
    }
    
    
}
