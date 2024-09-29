/**
 * TwelveLittleScoutsClerk members of an event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBDouble;
import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

public class DBEventMember extends DBStrukt {
    
    public static final String EVENTMEMBER_IDX_SEQUENCE = "EVENTMEMBER_IDX_SEQ";

    public DBInteger      idx = new DBInteger("idx", "Idx");
    public DBInteger      bp_idx = new DBInteger( "bp_idx" );
    public DBInteger      event_idx = new DBInteger( "event_idx" );
    public DBInteger      member_idx = new DBInteger( "member_idx" );
    public DBInteger      group_idx = new DBInteger( "group_idx" );
    public DBString       name = new DBString("name", "Name", 50 );          // copy of member
    public DBString       forname = new DBString("forname", "Forname", 50 ); // copy of member
    public DBString       group = new DBString("group", "Group", 50 );       // copy of group name
    public DBHistory      hist = new DBHistory( "hist" );
    public DBDouble       costs = new DBDouble( "costs" );                   // got a discount? So this value can vary
    public DBDouble       paid = new DBDouble( "piad" );
    public DBDouble       paid_cash = new DBDouble( "piad_cash", "Cash" );
    public DBString       comment = new DBString("comment", "Comment", 255 );
    
    public DBEventMember()
    {
        super("EVENTMEMBERS");
        
        add(idx);
        add(bp_idx);
        add(event_idx);
        add(member_idx);
        add(group_idx);        
        add(name);
        add(forname);
        add(group);        
        add(hist);
        add(costs);
        add(paid);
        add(paid_cash,2);
        add(comment);
        
        hist.setTitle(" ");
        
        bp_idx.shouldHaveIndex();
        idx.setAsPrimaryKey();
        
        setVersion(2);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBEventMember();
    }
    
}
