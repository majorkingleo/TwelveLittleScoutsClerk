/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;
import at.redeye.FrameWork.base.bindtypes.ForeignKeyDefinition;

/**
 *
 * @author martin
 */
public class DBContact extends DBStrukt 
{
     public static final String CONTACT_IDX_SEQUENCE = "CONTACT_IDX_SEQUENCE";

    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");

    public DBInteger      idx = new DBInteger("idx", "Idx");
    public DBInteger      bp_idx = new DBInteger( "bp_idx" );
    public DBString       name = new DBString("name", "Name", 50 );
    public DBString       forname = new DBString("forname", "Forname", 50 );    
    public DBHistory      hist = new DBHistory( "hist" );    
    public DBString       note = new DBString("note", "Note", 300);
    public DBString       tel = new DBString("tel", "Phone Number", 50 );
    public DBString       email = new DBString("email", "Email", 200 );
    public DBString       bank_account_iban = new DBString("bank_account_iban", "Bank account IBAN", 50 );
    public DBString       bank_account_bic = new DBString("bank_account_bic", "Bank account BIC", 50 );
    
    public DBContact()
    {
        super("CONTACT");
        
        add(idx);
        add(bp_idx);
        add(name);
        add(forname);
        add(hist);
        add(note);
        add(tel);
        add(email);
        add(bank_account_iban);
        add(bank_account_bic);
        
        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();

        addForeignKey(FK_BILLING_PERIOD, 2);

        hist.setTitle(" ");
        
        setVersion(2);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBContact();
    }
    
}
