/**
 * TwelveLittleScoutsClerk mail dispatch jobs
 * @author Copyright (c) 2023-2026 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBBlob;
import at.redeye.FrameWork.base.bindtypes.DBEnumAsInteger;
import at.redeye.FrameWork.base.bindtypes.DBFlagInteger;
import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;
import at.redeye.FrameWork.base.bindtypes.ForeignKeyDefinition;
import java.util.Vector;

public class DBMailJob extends DBStrukt {

    public static final String MAIL_JOB_IDX_SEQUENCE = "MAIL_JOB_IDX_SEQ";

    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");
    public static final ForeignKeyDefinition FK_BILL =
        new ForeignKeyDefinition("bill_idx", "BILLS", "idx");

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

    public static final DBInteger       IDX             = new DBInteger("idx");
    public static final DBInteger       BP_IDX          = new DBInteger("bp_idx");
    public static final DBInteger       BILL_IDX        = new DBInteger("bill_idx");
    public static final DBString        RECIPIENT_EMAIL = new DBString("recipient_email", "E-Mail", 200);
    public static final DBString        RECIPIENT_NAME  = new DBString("recipient_name", "Recipient", 200);
    public static final DBString        SUBJECT         = new DBString("subject", "Subject", 500);
    public static final DBBlob          BODY            = new DBBlob("body");
    public static final DBBlob          PDF_DATA        = new DBBlob("pdf_data");
    public static final DBEnumAsInteger STATE           = new DBEnumAsInteger("state", new StateHandler());
    // AI modification start (GitHub Copilot / GPT-5.3-Codex)
    public static final DBFlagInteger   ACKNOWLEDGED    = new DBFlagInteger("acknowledged", "Acknowledged");
    // AI modification end
    public static final DBInteger       RETRY_COUNT     = new DBInteger("retry_count");
    public static final DBString        ERROR_MESSAGE   = new DBString("error_message", "Error", 2000);
    public static final DBHistory       HIST            = new DBHistory("hist");

    public DBInteger       idx             = IDX.getCopy();
    public DBInteger       bp_idx          = BP_IDX.getCopy();
    public DBInteger       bill_idx        = BILL_IDX.getCopy();
    public DBString        recipient_email = RECIPIENT_EMAIL.getCopy();
    public DBString        recipient_name  = RECIPIENT_NAME.getCopy();
    public DBString        subject         = SUBJECT.getCopy();
    public DBBlob          body            = BODY.getCopy();
    public DBBlob          pdf_data        = PDF_DATA.getCopy();
    public DBEnumAsInteger state           = (DBEnumAsInteger) STATE.getCopy();
    // AI modification start (GitHub Copilot / GPT-5.3-Codex)
    public DBFlagInteger   acknowledged    = ACKNOWLEDGED.getCopy();
    // AI modification end
    public DBInteger       retry_count     = RETRY_COUNT.getCopy();
    public DBString        error_message   = ERROR_MESSAGE.getCopy();
    public DBHistory       hist            = (DBHistory) HIST.getCopy();

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
        // AI modification start (GitHub Copilot / GPT-5.3-Codex)
        add(acknowledged, 3);
        // AI modification end
        add(retry_count);
        add(error_message);
        add(hist);

        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();
        hist.setTitle(" ");

        addForeignKey(FK_BILLING_PERIOD, 2);
        addForeignKey(FK_BILL, 2);

        // AI modification start (GitHub Copilot / GPT-5.3-Codex)
        setVersion(3);
        // AI modification end
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

    // AI modification start (GitHub Copilot / GPT-5.3-Codex)
    // generated by AI (GitHub Copilot / GPT-5.3-Codex)
    public boolean isAcknowledged() {
        return acknowledged.getValue() != null && acknowledged.getValue() > 0;
    }

    // generated by AI (GitHub Copilot / GPT-5.3-Codex)
    public void setAcknowledged(boolean val) {
        acknowledged.handler.setValue(val ? 1 : 0);
    }
    // AI modification end
}
