/**
 * TwelveLittleScoutsClerk list of events
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */

package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBDouble;
import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;
import at.redeye.FrameWork.base.bindtypes.ForeignKeyDefinition;

public class DBEvent extends DBStrukt {
    
    public static final String EVENT_IDX_SEQUENCE = "EVENT_IDX_SEQUENCE";

    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");

    // Column references for Condition API
    public static final DBInteger IDX                   = new DBInteger("idx");
    public static final DBInteger BP_IDX                = new DBInteger("bp_idx");
    public static final DBString  NAME                  = new DBString("name", 1);
    public static final DBHistory HIST                  = new DBHistory("hist");
    public static final DBDouble  COSTS                 = new DBDouble("costs");
    public static final DBDouble  PAID                  = new DBDouble("paid");
    public static final DBDouble  PLANNED_COSTS         = new DBDouble("planned_costs");
    public static final DBString  BILLING_TEMPLATE      = new DBString("billing_template", 1);
    public static final DBString  REGISTRATION_TEMPLATE = new DBString("registration_template", 1);
    public static final DBDouble  REGISTRATION_COSTS    = new DBDouble("registration_costs");

    public DBInteger  idx                    = IDX.getCopy();
    public DBInteger  bp_idx                 = BP_IDX.getCopy();
    public DBString   name                   = NAME.getCopy();
    public DBHistory  hist                   = (DBHistory)HIST.getCopy();
    public DBDouble   costs                  = COSTS.getCopy();
    public DBDouble   paid                   = PAID.getCopy();
    public DBDouble   planned_costs          = PLANNED_COSTS.getCopy();
    public DBString   billing_template       = BILLING_TEMPLATE.getCopy();
    public DBString   registration_template  = REGISTRATION_TEMPLATE.getCopy();
    public DBDouble   registration_costs     = REGISTRATION_COSTS.getCopy();
    
    public DBEvent()
    {
        super("EVENT");
        
        add(idx);
        add(bp_idx);
        add(name);
        add(hist);
        add(costs);
        add(paid, 2);
        add(planned_costs, 3);
        add(billing_template, 4);
        add(registration_template, 5);
        add(registration_costs, 6);
        
        hist.setTitle(" ");
        
        idx.setAsPrimaryKey();

        addForeignKey(FK_BILLING_PERIOD, 7);

        setVersion(7);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBEvent();
    }
    
}
