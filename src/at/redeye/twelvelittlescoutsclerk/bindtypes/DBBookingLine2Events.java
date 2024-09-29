/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */

package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;


public class DBBookingLine2Events extends DBStrukt
{
    public static final String BOOKINGLINE2EVENTS_IDX_SEQUENCE = "B2E_IDX_SEQUENCE";
    
    public DBInteger      idx = new DBInteger("idx", "Idx");
    public DBInteger      bl_idx = new DBInteger("bl_idx", "Booking line Idx");
    public DBInteger      event_idx = new DBInteger("event_idx", "Event Idx");
    public DBInteger      member_idx = new DBInteger("member_idx", "Member Idx");
    public DBInteger      contact_idx = new DBInteger("contact_idx", "Contact Idx");
    public DBInteger      bp_idx = new DBInteger( "bp_idx" );
    public DBString       member_name = new DBString( "member_name", 50 );
    public DBString       event_name = new DBString( "event_name", 50 );
    public DBHistory      hist = new DBHistory( "hist" );
    
    public DBBookingLine2Events()
    {
        super("BOOKINGLINE2EVENTS");
        
        add(idx);        
        add(bl_idx);
        add(event_idx);
        add(bp_idx);
        add(member_idx);
        add(contact_idx);
        add(member_name,2);
        add(event_name,2);
        add(hist);
                
        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();
        hist.setTitle(" ");
        
        setVersion(2);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBBookingLine2Events();
    }
    
    
}
