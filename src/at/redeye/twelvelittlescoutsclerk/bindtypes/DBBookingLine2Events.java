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


public class DBBookingLine2Events extends DBStrukt
{
    public static final String BOOKINGLINE2EVENTS_IDX_SEQUENCE = "B2E_IDX_SEQUENCE";

    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");
    public static final ForeignKeyDefinition FK_BOOKING_LINE =
        new ForeignKeyDefinition("bl_idx", "BOOKINGLINE", "idx");
    public static final ForeignKeyDefinition FK_EVENT =
        new ForeignKeyDefinition("event_idx", "EVENT", "idx");
    public static final ForeignKeyDefinition FK_MEMBER =
        new ForeignKeyDefinition("member_idx", "MEMBER", "idx");
    public static final ForeignKeyDefinition FK_CONTACT =
        new ForeignKeyDefinition("contact_idx", "CONTACT", "idx");


    public static final DBInteger      IDX = new DBInteger("idx", "Idx");
    public static final DBInteger      BL_IDX = new DBInteger("bl_idx", "Booking line Idx");
    public static final DBInteger      EVENT_IDX = new DBInteger("event_idx", "Event Idx");
    public static final DBInteger      MEMBER_IDX = new DBInteger("member_idx", "Member Idx");
    public static final DBInteger      CONTACT_IDX = new DBInteger("contact_idx", "Contact Idx");
    public static final DBInteger      BP_IDX = new DBInteger( "bp_idx" );
    public static final DBString       MEMBER_NAME = new DBString( "member_name", "Member Name", 50 );
    public static final DBString       EVENT_NAME = new DBString( "event_name", "Event Name", 50 );
    public static final DBHistory      HIST = new DBHistory( "hist" );

    public DBInteger      idx = IDX.getCopy();
    public DBInteger      bl_idx = BL_IDX.getCopy();
    public DBInteger      event_idx = EVENT_IDX.getCopy();
    public DBInteger      member_idx = MEMBER_IDX.getCopy();
    public DBInteger      contact_idx = CONTACT_IDX.getCopy();
    public DBInteger      bp_idx = BP_IDX.getCopy();
    public DBString       member_name = MEMBER_NAME.getCopy();
    public DBString       event_name = EVENT_NAME.getCopy();
    public DBHistory      hist = (DBHistory) HIST.getCopy();
    
    public DBBookingLine2Events()
    {
        super("BOOKINGLINE2EVENTS");
        
        add(idx);        
        add(bl_idx);
        add(event_idx);
        add(bp_idx);
        add(member_idx);
        add(contact_idx);
        add(member_name,2);
        add(event_name,2);
        add(hist);
                
        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();
        hist.setTitle(" ");

        // Mark nullable FKs (default is NOT NULL, so explicitly allow null for these)
        member_idx.setCanBeNull(true);
        contact_idx.setCanBeNull(true);
        // bl_idx, event_idx, bp_idx remain NOT NULL (default: canBeNull = false)

        addForeignKey(FK_BILLING_PERIOD, 3);
        addForeignKey(FK_BOOKING_LINE, 3);
        addForeignKey(FK_EVENT, 3);
        addForeignKey(FK_MEMBER, 3);
        addForeignKey(FK_CONTACT, 3);

        setVersion(4);  // Bump version due to NULL constraint changes
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBBookingLine2Events();
    }
    
    
}
