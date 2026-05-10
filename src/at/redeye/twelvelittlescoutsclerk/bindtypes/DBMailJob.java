/**
 * TwelveLittleScoutsClerk mail dispatch jobs
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

public class DBMailJob extends DBStrukt {

    public static final String MAIL_JOB_IDX_SEQUENCE = "MAIL_JOB_IDX_SEQ";

    public enum State { PENDING, SENDING, SENT, FAILED }

    public static class StateHandler extends DBEnumAsInteger.EnumAsIntegerHandler {

        private int value = State.PENDING.ordinal();

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

    public DBInteger       idx             = new DBInteger("idx");
    public DBInteger       bp_idx          = new DBInteger("bp_idx");
    public DBInteger       bill_idx        = new DBInteger("bill_idx");
    public DBString        recipient_email = new DBString("recipient_email", "E-Mail", 200);
    public DBString        recipient_name  = new DBString("recipient_name", "Recipient", 200);
    public DBString        subject         = new DBString("subject", "Subject", 500);
    public DBBlob          body            = new DBBlob("body");
    public DBBlob          pdf_data        = new DBBlob("pdf_data");
    public DBEnumAsInteger state           = new DBEnumAsInteger("state", new StateHandler());
    public DBInteger       retry_count     = new DBInteger("retry_count");
    public DBString        error_message   = new DBString("error_message", "Error", 2000);
    public DBHistory       hist            = new DBHistory("hist");

    public DBMailJob() {
        super("MAIL_JOBS");

        add(idx);
        add(bp_idx);
        add(bill_idx);
        add(recipient_email);
        add(recipient_name);
        add(subject);
        add(body);
        add(pdf_data);
        add(state);
        add(retry_count);
        add(error_message);
        add(hist);

        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();
        hist.setTitle(" ");

        setVersion(1);
    }

    @Override
    public DBStrukt getNewOne() {
        return new DBMailJob();
    }

    public boolean isPending() {
        return State.PENDING.ordinal() == state.getValue();
    }

    public boolean isFailed() {
        return State.FAILED.ordinal() == state.getValue();
    }
}
