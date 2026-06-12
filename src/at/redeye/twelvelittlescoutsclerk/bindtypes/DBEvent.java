/**
 * TwelveLittleScoutsClerk list of events
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */

package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBDouble;
import at.redeye.FrameWork.base.bindtypes.DBFlagInteger;
import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;
import at.redeye.FrameWork.base.bindtypes.ForeignKeyDefinition;

public class DBEvent extends DBStrukt {
    
    public static final String EVENT_IDX_SEQUENCE = "EVENT_IDX_SEQUENCE";

    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");

    public static final DBInteger      IDX = new DBInteger("idx", "Idx");
    public static final DBInteger      BP_IDX = new DBInteger( "bp_idx" );
    public static final DBString       NAME = new DBString("name", "Name", 50 );
    public static final DBHistory      HIST = new DBHistory( "hist" );
    public static final DBDouble       COSTS = new DBDouble( "costs", "Costs per Person" );
    public static final DBDouble       PAID = new DBDouble( "paid", "Paid" );
    public static final DBDouble       PLANNED_COSTS = new DBDouble( "planned_costs", "Planned Costs" );
    public static final DBString       BILLING_TEMPLATE      = new DBString( "billing_template", "Billing Template", 512 );
    public static final DBString       REGISTRATION_TEMPLATE = new DBString( "registration_template", "Registration Template", 512 );
    public static final DBDouble       REGISTRATION_COSTS    = new DBDouble( "registration_costs", "Registration Costs" );
    public static final DBInteger      ACCOUNT_CLASS_IDX = new DBInteger("account_class_idx", "Account Class Idx");
    public static final DBString       ACCOUNT_CLASS = new DBString("account_class", "Account Class", 50);

        // AI modification start (GPT-5.4)
        public static final DBFlagInteger  COUNTS_TO_AVAILABLE_CASH_AMOUNT = new DBFlagInteger(
            "counts_to_available_cash_amount", "Counts to Available Cash Amount" );
        // AI modification end

    public DBInteger      idx = IDX.getCopy();
    public DBInteger      bp_idx = BP_IDX.getCopy();
    public DBString       name = NAME.getCopy();
    public DBHistory      hist = (DBHistory) HIST.getCopy();
    public DBDouble       costs = COSTS.getCopy();
    public DBDouble       paid = PAID.getCopy();
    public DBDouble       planned_costs = PLANNED_COSTS.getCopy();
    public DBString       billing_template      = BILLING_TEMPLATE.getCopy();
    public DBString       registration_template = REGISTRATION_TEMPLATE.getCopy();
    public DBDouble       registration_costs    = REGISTRATION_COSTS.getCopy();
    // AI modification start (GPT-5.4)
    public DBFlagInteger  counts_to_available_cash_amount = COUNTS_TO_AVAILABLE_CASH_AMOUNT.getCopy();
    // AI modification end
    public DBInteger      account_class_idx = ACCOUNT_CLASS_IDX.getCopy();
    public DBString       account_class = ACCOUNT_CLASS.getCopy();


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
        // AI modification start (GPT-5.4)
        add(counts_to_available_cash_amount, 8);
        // AI modification end
        add( account_class_idx, 9 );
        add( account_class, 9 );
        
        hist.setTitle(" ");
        
        idx.setAsPrimaryKey();

        addForeignKey(FK_BILLING_PERIOD, 7);

        setVersion(9);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBEvent();
    }
    
}
