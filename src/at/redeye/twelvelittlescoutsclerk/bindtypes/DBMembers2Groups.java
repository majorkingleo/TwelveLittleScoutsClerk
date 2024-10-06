/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
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
    public DBInteger      bp_idx = new DBInteger( "bp_idx" );
    public DBHistory      hist = new DBHistory( "hist" );
    public DBString       group = new DBString("group", "Group", 50 );
    public DBString       member_name = new DBString("member_name", "Member Name", 50 );
    
    public DBMembers2Groups()
    {
        super("MEMBERS2GROUPS");
        
        add(idx);        
        add(member_idx);
        add(group_idx);
        add(bp_idx);        
        add(hist);
        add(group,2);
        add(member_name,2);
                
        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();
        hist.setTitle(" ");
        
        setVersion(2);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBMembers2Groups();
    }
    
    
}
