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
public class DBMembers2Groups extends DBStrukt
{
    public static final String MEMBERS2GROUPS_IDX_SEQUENCE = "M2G_IDX_SEQUENCE";

    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");
    public static final ForeignKeyDefinition FK_MEMBER =
        new ForeignKeyDefinition("member_idx", "MEMBER", "idx");
    public static final ForeignKeyDefinition FK_GROUP =
        new ForeignKeyDefinition("group_idx", "GROUP", "idx");

    // Column references for Condition API
    public static final DBInteger IDX         = new DBInteger("idx");
    public static final DBInteger MEMBER_IDX  = new DBInteger("member_idx");
    public static final DBInteger GROUP_IDX   = new DBInteger("group_idx");
    public static final DBInteger BP_IDX      = new DBInteger("bp_idx");
    public static final DBHistory HIST        = new DBHistory("hist");
    public static final DBString  GROUP       = new DBString("group", 1);
    public static final DBString  MEMBER_NAME = new DBString("member_name", 1);

    public DBInteger  idx          = IDX.getCopy();
    public DBInteger  member_idx   = MEMBER_IDX.getCopy();
    public DBInteger  group_idx    = GROUP_IDX.getCopy();
    public DBInteger  bp_idx       = BP_IDX.getCopy();
    public DBHistory  hist         = (DBHistory)HIST.getCopy();
    public DBString   group        = GROUP.getCopy();
    public DBString   member_name  = MEMBER_NAME.getCopy();
    
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

        addForeignKey(FK_BILLING_PERIOD, 3);
        addForeignKey(FK_MEMBER, 3);
        addForeignKey(FK_GROUP, 3);

        setVersion(3);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBMembers2Groups();
    }
    
    
}
