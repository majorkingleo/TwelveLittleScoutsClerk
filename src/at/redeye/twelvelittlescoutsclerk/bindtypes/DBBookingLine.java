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
import at.redeye.FrameWork.base.bindtypes.ForeignKeyDefinition;

public class DBBookingLine extends DBStrukt {

    public static final String BOOKING_LINE_IDX_SEQUENCE = "BL_IDX_SEQUENCE";
    
    public DBInteger      idx = new DBInteger("idx", "Idx");
    public DBHistory      hist = new DBHistory("hist");
    public DBInteger      bp_idx = new DBInteger( "bp_idx", "BP Idx" );
    public DBString       line = new DBString("line", "Line", 500);
    public DBString       reference = new DBString("reference", "Reference", 500);
    public DBDouble       amount = new DBDouble("amount", "Amount");
    public DBString       from_bank_account_iban = new DBString("from_bank_account_iban", "From Bank Account IBAN", 50);
    public DBString       from_bank_account_bic = new DBString("from_bank_account_bic", "From Bank Account BIC", 50);    
    public DBString       from_name = new DBString("from_name", "From Name", 255);
    public DBInteger      contact_idx = new DBInteger("contact_idx", "Contact Idx");
    public DBFlagInteger  assigned = new DBFlagInteger("assigned","Assigned");
    public DBFlagInteger  splitpos = new DBFlagInteger("splitpos","SplitPos");
    public DBInteger      parent_idx = new DBInteger("parent_idx", "Parent Idx");
    public DBDateTime     date = new DBDateTime( "date", "Booking Date");
    public DBString       data_source = new DBString("data_source", "Data Source", 50); // eg elba, cash
    public DBString       comment = new DBString("comment", "Comment", 50);
            
    public DBBookingLine()
    {
        super("BOOKINGLINE");
        
        add( idx );
        add( date );
        add( hist );
        add( bp_idx );
        add( line );
        add( reference );
        add( amount );
        add( from_bank_account_iban );
        add( from_bank_account_bic );
        add( from_name );
        add( contact_idx );
        add( assigned );        
        add( splitpos );
        add( parent_idx );
        add( data_source );
        add( comment );
        
        idx.setAsPrimaryKey();
        hist.setTitle(" ");

        addForeignKey(new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx"), 2);
        addForeignKey(new ForeignKeyDefinition("contact_idx", "CONTACT", "idx"), 2);

        setVersion(2);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBBookingLine();
    }
    
}
