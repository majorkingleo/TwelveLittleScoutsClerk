/**
 * TwelveLittleScoutsClerk bill templates
 * @author Copyright (c) 2023-2026 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBBlob;
import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

public class DBBillTemplate extends DBStrukt {

    public static final String BILL_TEMPLATE_IDX_SEQUENCE = "BILL_TEMPLATE_IDX_SEQ";

    public DBInteger idx       = new DBInteger("idx");
    public DBInteger bp_idx    = new DBInteger("bp_idx");
    public DBString  name      = new DBString("name", "Name", 255);
    public DBBlob    odt_data  = new DBBlob("odt_data");
    public DBHistory hist      = new DBHistory("hist");

    public DBBillTemplate() {
        super("BILL_TEMPLATES");

        add(idx);
        add(bp_idx);
        add(name);
        add(odt_data);
        add(hist);

        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();
        hist.setTitle(" ");

        setVersion(1);
    }

    @Override
    public DBStrukt getNewOne() {
        return new DBBillTemplate();
    }
}
