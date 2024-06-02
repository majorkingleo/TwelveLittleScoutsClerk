/**
 * TwelveLittleScoutsClerk Booking line table
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */

package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBDateTime;
import at.redeye.FrameWork.base.bindtypes.DBDouble;
import at.redeye.FrameWork.base.bindtypes.DBFlagInteger;
import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

public class DBBookingLine extends DBStrukt {

    public static final String BOOKING_LINE_IDX_SEQUENCE = "BL_IDX_SEQUENCE";
    
    public DBInteger      idx = new DBInteger("idx", "Idx");
    public DBHistory      hist = new DBHistory("hist");
    public DBInteger      bz_idx = new DBInteger( "bp_idx" );
    public DBString       line = new DBString("line", 500);
    public DBDouble       amount = new DBDouble("amount" );
    public DBString       from_bank_account = new DBString("from_bank_account",255);
    public DBString       from_name = new DBString("from_name",255);
    public DBInteger      contact_idx = new DBInteger("contact_idx");
    public DBFlagInteger  assigned = new DBFlagInteger("assigned","Assigned");
    public DBFlagInteger  splitpos = new DBFlagInteger("splitpos","SplitPos");
    public DBInteger      parent_idx = new DBInteger("parent_idx", "Parent Idx");
    public DBDateTime     date = new DBDateTime( "date", "Booking Date");
    public DBString       data_source = new DBString("data_source",50); // eg elba, cash
    public DBString       comment = new DBString("comment",50);
            
    public DBBookingLine()
    {
        super("BOOKINGLINE");
        
        add( idx );
        add( date );
        add( hist );
        add( bz_idx );
        add( line );
        add( amount );
        add( from_bank_account );        
        add( from_name );
        add( contact_idx );
        add( assigned );        
        add( splitpos );
        add( parent_idx );
        add( data_source );
        add( comment );
        
        idx.setAsPrimaryKey();        
        hist.setTitle(" ");
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBBookingLine();
    }
    
}
