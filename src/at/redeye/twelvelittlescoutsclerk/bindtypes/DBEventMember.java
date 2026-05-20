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
import at.redeye.FrameWork.base.bindtypes.ForeignKeyDefinition;

public class DBEventMember extends DBStrukt {
    
    public static final String EVENTMEMBER_IDX_SEQUENCE = "EVENTMEMBER_IDX_SEQ";

    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");
    public static final ForeignKeyDefinition FK_EVENT =
        new ForeignKeyDefinition("event_idx", "EVENT", "idx");
    public static final ForeignKeyDefinition FK_MEMBER =
        new ForeignKeyDefinition("member_idx", "MEMBER", "idx");
    public static final ForeignKeyDefinition FK_GROUP =
        new ForeignKeyDefinition("group_idx", "GROUP", "idx");
    public static final ForeignKeyDefinition FK_BILL =
        new ForeignKeyDefinition("bill_idx", "BILLS", "idx");
    public static final ForeignKeyDefinition FK_REGISTRATION_BILL =
        new ForeignKeyDefinition("registration_bill_idx", "BILLS", "idx");

    // Column references for Condition API
    public static final DBInteger IDX                   = new DBInteger("idx");
    public static final DBInteger BP_IDX                = new DBInteger("bp_idx");
    public static final DBInteger EVENT_IDX             = new DBInteger("event_idx");
    public static final DBInteger MEMBER_IDX            = new DBInteger("member_idx");
    public static final DBInteger GROUP_IDX             = new DBInteger("group_idx");
    public static final DBString  NAME                  = new DBString("name", 1);
    public static final DBString  FORNAME               = new DBString("forname", 1);
    public static final DBString  GROUP                 = new DBString("group", 1);
    public static final DBHistory HIST                  = new DBHistory("hist");
    public static final DBDouble  COSTS                 = new DBDouble("costs");
    public static final DBDouble  PAID                  = new DBDouble("piad");        // DB column: "piad" (sic)
    public static final DBDouble  PAID_CASH             = new DBDouble("piad_cash");   // DB column: "piad_cash" (sic)
    public static final DBString  COMMENT               = new DBString("comment", 1);
    public static final DBString  BILL                  = new DBString("bill", 1);
    public static final DBInteger BILL_IDX              = new DBInteger("bill_idx");
    public static final DBInteger REGISTRATION_BILL_IDX = new DBInteger("registration_bill_idx");

    public DBInteger  idx                    = IDX.getCopy();
    public DBInteger  bp_idx                 = BP_IDX.getCopy();
    public DBInteger  event_idx              = EVENT_IDX.getCopy();
    public DBInteger  member_idx             = MEMBER_IDX.getCopy();
    public DBInteger  group_idx              = GROUP_IDX.getCopy();
    public DBString   name                   = NAME.getCopy();  // copy of member
    public DBString   forname                = FORNAME.getCopy();  // copy of member
    public DBString   group                  = GROUP.getCopy();  // copy of group name
    public DBHistory  hist                   = (DBHistory)HIST.getCopy();
    public DBDouble   costs                  = COSTS.getCopy();  // got a discount? So this value can vary
    public DBDouble   paid                   = PAID.getCopy();
    public DBDouble   paid_cash              = PAID_CASH.getCopy();
    public DBString   comment                = COMMENT.getCopy();
    public DBString   bill                   = BILL.getCopy();
    public DBInteger  bill_idx               = BILL_IDX.getCopy();  // FK to BILLS.idx
    public DBInteger  registration_bill_idx  = REGISTRATION_BILL_IDX.getCopy();  // FK to BILLS.idx (registration)

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
        add(bill, 3);
        add(bill_idx, 4);
        add(registration_bill_idx, 5);
        
        hist.setTitle(" ");
        
        bp_idx.shouldHaveIndex();
        idx.setAsPrimaryKey();

        addForeignKey(FK_BILLING_PERIOD, 6);
        addForeignKey(FK_EVENT, 6);
        addForeignKey(FK_MEMBER, 6);
        addForeignKey(FK_GROUP, 6);
        addForeignKey(FK_BILL, 6);
        addForeignKey(FK_REGISTRATION_BILL, 6);

        setVersion(6);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBEventMember();
    }
    
}
