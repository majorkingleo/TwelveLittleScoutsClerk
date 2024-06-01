package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

/**
 *
 * @author martin
 */
public class DBContact extends DBStrukt 
{
     public static final String CONTACT_IDX_SEQUENCE = "CONTACT_IDX_SEQUENCE";
    
    public DBInteger      idx = new DBInteger("idx", "Idx");
    public DBInteger      bz_idx = new DBInteger( "bp_idx" );
    public DBString       name = new DBString("name", "Name", 50 );
    public DBString       forname = new DBString("forname", "Forname", 50 );    
    public DBHistory      hist = new DBHistory( "hist" );    
    public DBString       note = new DBString("note", "Notiz", 300);
    public DBString       tel = new DBString("tel", "Phone Number", 50 );
    public DBString       email = new DBString("email", "Email", 200 );
    public DBString       bank_account = new DBString("bank_account", "Bank account", 50 );
    
    public DBContact()
    {
        super("CONTACT");
        
        add(idx);
        add(bz_idx);
        add(name);
        add(forname);
        add(hist);
        add(note);
        add(tel);
        add(email);
        add(bank_account);
        
        setVersion(1);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBContact();
    }
    
}
