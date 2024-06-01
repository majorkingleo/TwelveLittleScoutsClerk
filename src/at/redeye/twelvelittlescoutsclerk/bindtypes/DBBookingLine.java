/**
 *
 * @author martin
 */

package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBDouble;
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
            
    public DBBookingLine()
    {
        super("BOOKINGLINE");
        
        add( idx );
        add( hist );
        add( bz_idx );
        add( line );
        add( amount );
        add( from_bank_account );
        add( from_bank_account );
        add( from_name );
        add( contact_idx );
        
        idx.setAsPrimaryKey();        
        hist.setTitle(" ");
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBBookingLine();
    }
    
}
