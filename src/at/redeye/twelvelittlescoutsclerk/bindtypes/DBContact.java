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

    public static final DBInteger      IDX = new DBInteger("idx", "Idx");
    public static final DBInteger      BP_IDX = new DBInteger( "bp_idx" );
    public static final DBString       NAME = new DBString("name", "Name", 50 );
    public static final DBString       FORNAME = new DBString("forname", "Forname", 50 );    
    public static final DBHistory      HIST = new DBHistory( "hist" );    
    public static final DBString       NOTE = new DBString("note", "Note", 300);
    public static final DBString       TEL = new DBString("tel", "Phone Number", 50 );
    public static final DBString       EMAIL = new DBString("email", "Email", 200 );
    public static final DBString       BANK_ACCOUNT_IBAN = new DBString("bank_account_iban", "Bank account IBAN", 50 );
    public static final DBString       BANK_ACCOUNT_BIC = new DBString("bank_account_bic", "Bank account BIC", 50 );

    public DBInteger      idx = IDX.getCopy();
    public DBInteger      bp_idx = BP_IDX.getCopy();
    public DBString       name = NAME.getCopy();
    public DBString       forname = FORNAME.getCopy();    
    public DBHistory      hist = (DBHistory) HIST.getCopy();    
    public DBString       note = NOTE.getCopy();
    public DBString       tel = TEL.getCopy();
    public DBString       email = EMAIL.getCopy();
    public DBString       bank_account_iban = BANK_ACCOUNT_IBAN.getCopy();
    public DBString       bank_account_bic = BANK_ACCOUNT_BIC.getCopy();
    
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
