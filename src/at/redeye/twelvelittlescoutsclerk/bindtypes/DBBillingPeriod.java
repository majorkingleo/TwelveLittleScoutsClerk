/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBFlagInteger;
import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

/**
 *
 * @author martin
 */
public class DBBillingPeriod extends DBStrukt 
{
    // Column references for Condition API
    public static final DBInteger     IDX     = new DBInteger("idx");
    public static final DBString      TITLE   = new DBString("title", 1);
    public static final DBString      COMMENT = new DBString("comment", 1);
    public static final DBHistory     HIST    = new DBHistory("hist");
    public static final DBFlagInteger LOCKED  = new DBFlagInteger("locked");

    public DBInteger      idx      = IDX.getCopy();
    public DBString       title    = TITLE.getCopy();
    public DBString       comment  = COMMENT.getCopy();
    public DBHistory      hist     = (DBHistory)HIST.getCopy();
    public DBFlagInteger  locked   = new DBFlagInteger(LOCKED.getName());
    
    
    public DBBillingPeriod()
    {
        super( "BILLING_PERIOD" );
        
        add(idx);
        add(title);
        add(comment);
        add(hist);
        add(locked);
        
        idx.setAsPrimaryKey();       
        
        hist.setTitle(" ");
        
        setVersion(1);
    }

    @Override
    public DBStrukt getNewOne() {
        return new DBBillingPeriod();
    }
    
    public boolean isLocked()
    {
        return locked.getValue() > 0;
    }
    
}
