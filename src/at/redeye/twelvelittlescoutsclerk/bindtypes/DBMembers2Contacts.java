package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

/**
 *
 * @author martin
 */
public class DBMembers2Contacts extends DBStrukt
{
    public static final String MEMBERS2CONTACTS_IDX_SEQUENCE = "MEMBERS2CONTACTS_IDX_SEQUENCE";
    
    public DBInteger      idx = new DBInteger("idx", "Idx");
    public DBInteger      member_idx = new DBInteger("member_idx", "Member Idx");
    public DBInteger      contact_idx = new DBInteger("contact_idx", "Contact Idx");
    public DBHistory      hist = new DBHistory( "hist" );
    
    public DBMembers2Contacts()
    {
        super("MEMBERS2CONTACTS");
        
        add(idx);        
        add(member_idx);
        add(contact_idx);
        add(hist);
                
        idx.setAsPrimaryKey();
        hist.setTitle(" ");
        
        setVersion(1);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBMembers2Contacts();
    }
    
    
}
