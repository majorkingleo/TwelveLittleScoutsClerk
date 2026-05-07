/**
 * TwelveLittleScoutsClerk generated bills
 * @author Copyright (c) 2023-2026 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBBlob;
import at.redeye.FrameWork.base.bindtypes.DBEnumAsInteger;
import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;
import java.util.Vector;

public class DBBill extends DBStrukt {

    public static final String BILL_IDX_SEQUENCE = "BILL_IDX_SEQ";

    public enum State { NORMAL, CANCELED }

    public static class StateHandler extends DBEnumAsInteger.EnumAsIntegerHandler {

        private int value = State.NORMAL.ordinal();

        @Override
        public int getMaxSize() {
            return State.values().length;
        }

        @Override
        public boolean setValue(String val) {
            for (State s : State.values()) {
                if (s.name().equalsIgnoreCase(val)) {
                    value = s.ordinal();
                    return true;
                }
            }
            try {
                int i = Integer.parseInt(val);
                if (i >= 0 && i < State.values().length) {
                    value = i;
                    return true;
                }
            } catch (NumberFormatException ignored) {
            }
            return false;
        }

        @Override
        public boolean setValue(Integer val) {
            if (val >= 0 && val < State.values().length) {
                value = val;
                return true;
            }
            return false;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getValueAsString() {
            return State.values()[value].name();
        }

        @Override
        public DBEnumAsInteger.EnumAsIntegerHandler getNewOne() {
            return new StateHandler();
        }

        @Override
        public Vector<String> getPossibleValues() {
            Vector<String> res = new Vector<>();
            for (State s : State.values()) {
                res.add(s.name());
            }
            return res;
        }

        @Override
        public void refresh() {
            // nothing to do
        }
    }

    public DBInteger       idx        = new DBInteger("idx");
    public DBInteger       bp_idx     = new DBInteger("bp_idx");
    public DBString        billingnr  = new DBString("billingnr", "Billing Nr", 50);
    public DBString        file_name  = new DBString("file_name", "File Name", 255);
    public DBBlob          odt_data   = new DBBlob("odt_data");
    public DBBlob          pdf_data   = new DBBlob("pdf_data");
    public DBEnumAsInteger state      = new DBEnumAsInteger("state", new StateHandler());
    public DBHistory       hist       = new DBHistory("hist");

    public DBBill() {
        super("BILLS");

        add(idx);
        add(bp_idx);
        add(billingnr);
        add(file_name);
        add(odt_data);
        add(pdf_data);
        add(state);
        add(hist);

        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();
        hist.setTitle(" ");

        setVersion(1);
    }

    @Override
    public DBStrukt getNewOne() {
        return new DBBill();
    }

    public boolean isCanceled() {
        return State.CANCELED.ordinal() == state.getValue();
    }
}
