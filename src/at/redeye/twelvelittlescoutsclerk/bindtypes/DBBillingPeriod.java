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
    public DBInteger idx = new DBInteger("idx");
    public DBString  title = new DBString("title", "Billing Period", 50 );
    public DBString  comment = new DBString("comment", "Comment", 1000 );
    public DBHistory    hist = new DBHistory("hist");
    public DBFlagInteger locked = new DBFlagInteger("locked");
    
    
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
