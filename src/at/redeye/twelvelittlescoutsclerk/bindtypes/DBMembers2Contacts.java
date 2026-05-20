/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;
import at.redeye.FrameWork.base.bindtypes.ForeignKeyDefinition;

/**
 *
 * @author martin
 */
public class DBMembers2Contacts extends DBStrukt
{
    public static final String MEMBERS2CONTACTS_IDX_SEQUENCE = "M2C_IDX_SEQUENCE";

    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");
    public static final ForeignKeyDefinition FK_MEMBER =
        new ForeignKeyDefinition("member_idx", "MEMBER", "idx");
    public static final ForeignKeyDefinition FK_CONTACT =
        new ForeignKeyDefinition("contact_idx", "CONTACT", "idx");

    public DBInteger      idx = new DBInteger("idx", "Idx");
    public DBInteger      member_idx = new DBInteger("member_idx", "Member Idx");
    public DBInteger      contact_idx = new DBInteger("contact_idx", "Contact Idx");
    public DBInteger      bp_idx = new DBInteger( "bp_idx" );
    public DBHistory      hist = new DBHistory( "hist" );
    
    public DBMembers2Contacts()
    {
        super("MEMBERS2CONTACTS");
        
        add(idx);        
        add(member_idx);
        add(contact_idx);
        add(bp_idx);        
        add(hist);
                
        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();
        hist.setTitle(" ");

        addForeignKey(FK_BILLING_PERIOD, 2);
        addForeignKey(FK_MEMBER, 2);
        addForeignKey(FK_CONTACT, 2);

        setVersion(2);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBMembers2Contacts();
    }
    
    
}
