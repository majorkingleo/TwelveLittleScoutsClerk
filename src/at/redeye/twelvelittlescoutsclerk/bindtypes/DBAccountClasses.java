/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBEnumAsInteger;
import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;
import at.redeye.FrameWork.base.bindtypes.ForeignKeyDefinition;


import java.util.Vector;

/**
 *
 * @author martin
 */
public class DBAccountClasses extends DBStrukt 
{
    public enum Category { INCOME, EXPENSE }

    public static class CategoryHandler extends DBEnumAsInteger.EnumAsIntegerHandler {

        private int value = Category.INCOME.ordinal();

        @Override
        public int getMaxSize() {
            return Category.values().length;
        }

        @Override
        public boolean setValue(String val) {
            for (Category t : Category.values()) {
                if (t.name().equalsIgnoreCase(val)) {
                    value = t.ordinal();
                    return true;
                }
            }
            try {
                int i = Integer.parseInt(val);
                if (i >= 0 && i < Category.values().length) {
                    value = i;
                    return true;
                }
            } catch (NumberFormatException ignored) {
            }
            return false;
        }

        @Override
        public boolean setValue(Integer val) {
            if (val >= 0 && val < Category.values().length) {
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
            return Category.values()[value].name();
        }

        @Override
        public DBEnumAsInteger.EnumAsIntegerHandler getNewOne() {
            return new CategoryHandler();
        }

        @Override
        public Vector<String> getPossibleValues() {
            Vector<String> res = new Vector<>();
            for (Category t : Category.values()) {
                res.add(t.name());
            }
            return res;
        }

        @Override
        public void refresh() {
            // nothing to do
        }
    }

    public static final String ACCLASS_IDX_SEQUENCE = "ACCLASS_IDX_SEQU";
    
    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");

    public static final DBInteger       IDX      = new DBInteger("idx", "Idx");   
    public static final DBInteger       BP_IDX   = new DBInteger("bp_idx", "BP Idx");
    public static final DBEnumAsInteger CATEGORY = new DBEnumAsInteger("category", "Category", new CategoryHandler());
    public static final DBString        NAME     = new DBString("name", "Name", 50 );    
    public static final DBHistory       HIST     = new DBHistory( "hist" );

    public DBInteger        idx       = IDX.getCopy();   
    public DBInteger        bp_idx    = BP_IDX.getCopy();
    public DBEnumAsInteger  category = (DBEnumAsInteger) CATEGORY.getCopy();
    public DBString         name      = NAME.getCopy();    
    public DBHistory        hist      = (DBHistory) HIST.getCopy();
    
    public DBAccountClasses()
    {
        super("ACCOUNT_CLASSES");
        
        add( idx );
        add( bp_idx );
        add( category );
        add( name );
        add( hist );
        
        hist.setTitle(" ");  

        idx.setAsPrimaryKey();    
        
        addForeignKey(FK_BILLING_PERIOD, 1);

        setVersion(1);
    }

    
    @Override
    public DBStrukt getNewOne() {
        return new DBAccountClasses();
    }
    
}
