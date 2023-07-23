package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

/**
 *
 * @author martin
 */
public class DBMembers2Groups extends DBStrukt
{
    public static final String MEMBERS2GROUPS_IDX_SEQUENCE = "M2G_IDX_SEQUENCE";
    
    public DBInteger      idx = new DBInteger("idx", "Idx");
    public DBInteger      member_idx = new DBInteger("member_idx", "Member Idx");
    public DBInteger      group_idx = new DBInteger("group_idx", "Contact Idx");
    public DBHistory      hist = new DBHistory( "hist" );
    
    public DBMembers2Groups()
    {
        super("MEMBERS2GROUPS");
        
        add(idx);        
        add(member_idx);
        add(group_idx);
        add(hist);
                
        idx.setAsPrimaryKey();
        hist.setTitle(" ");
        
        setVersion(1);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBMembers2Groups();
    }
    
    
}
