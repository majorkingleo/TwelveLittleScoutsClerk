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

    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");
    public static final ForeignKeyDefinition FK_CONTACT =
        new ForeignKeyDefinition("contact_idx", "CONTACT", "idx");


    public static final DBInteger      IDX = new DBInteger("idx", "Idx");
    public static final DBHistory      HIST = new DBHistory("hist");
    public static final DBInteger      BP_IDX = new DBInteger( "bp_idx", "BP Idx" );
    public static final DBString       LINE = new DBString("line", "Line", 500);
    public static final DBString       REFERENCE = new DBString("reference", "Reference", 500);
    public static final DBDouble       AMOUNT = new DBDouble("amount", "Amount");
    public static final DBString       FROM_BANK_ACCOUNT_IBAN = new DBString("from_bank_account_iban", "From Bank Account IBAN", 50);
    public static final DBString       FROM_BANK_ACCOUNT_BIC = new DBString("from_bank_account_bic", "From Bank Account BIC", 50);    
    public static final DBString       FROM_NAME = new DBString("from_name", "From Name", 255);
    public static final DBInteger      CONTACT_IDX = new DBInteger("contact_idx", "Contact Idx");
    public static final DBFlagInteger  ASSIGNED = new DBFlagInteger("assigned","Assigned");
    public static final DBFlagInteger  SPLITPOS = new DBFlagInteger("splitpos","SplitPos");
    public static final DBInteger      PARENT_IDX = new DBInteger("parent_idx", "Parent Idx");
    public static final DBDateTime     DATE = new DBDateTime( "date", "Booking Date");
    public static final DBString       DATA_SOURCE = new DBString("data_source", "Data Source", 50); // eg elba, cash
    public static final DBString       COMMENT = new DBString("comment", "Comment", 50);

    public DBInteger      idx = IDX.getCopy();
    public DBHistory      hist = (DBHistory) HIST.getCopy();
    public DBInteger      bp_idx = BP_IDX.getCopy();
    public DBString       line = LINE.getCopy();
    public DBString       reference = REFERENCE.getCopy();
    public DBDouble       amount = AMOUNT.getCopy();
    public DBString       from_bank_account_iban = FROM_BANK_ACCOUNT_IBAN.getCopy();
    public DBString       from_bank_account_bic = FROM_BANK_ACCOUNT_BIC.getCopy();    
    public DBString       from_name = FROM_NAME.getCopy();
    public DBInteger      contact_idx = CONTACT_IDX.getCopy();
    public DBFlagInteger  assigned = (DBFlagInteger) ASSIGNED.getCopy();
    public DBFlagInteger  splitpos = (DBFlagInteger) SPLITPOS.getCopy();
    public DBInteger      parent_idx = PARENT_IDX.getCopy();
    public DBDateTime     date = DATE.getCopy();
    public DBString       data_source = DATA_SOURCE.getCopy(); // eg elba, cash
    public DBString       comment = COMMENT.getCopy();
            
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

        addForeignKey(FK_BILLING_PERIOD, 2);
        addForeignKey(FK_CONTACT, 2);

        setVersion(2);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBBookingLine();
    }
    
}
