/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.*;

/**
 *
 * @author martin
 */
public class DBMember extends DBStrukt
{
    public static final String MEMBERS_IDX_SEQUENCE = "MEMBERS_IDX_SEQUENCE";

    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");

    public static final DBInteger      IDX = new DBInteger("idx", "Idx");   
    public static final DBString       MEMBER_REGISTRATION_NUMBER = new DBString("member_registration_number", "Member Id", 50);
    public static final DBString       NAME = new DBString("name", "Name", 50 );
    public static final DBString       FORNAME = new DBString("forname", "Forname", 50 );
    public static final DBDateTime     ENTRY_DATE = new DBDateTime("entry_date", "Entry Date");
    public static final DBHistory      HIST = new DBHistory( "hist" );
    public static final DBInteger      BP_IDX = new DBInteger( "bp_idx" );
    public static final DBString       NOTE = new DBString("note", "Note", 300);
    public static final DBString       TEL = new DBString("tel", "Phone Number", 50 );
    public static final DBFlagInteger  INAKTIV = new DBFlagInteger("inactiv","Inactiv");
    public static final DBFlagInteger  DE_REGISTERED = new DBFlagInteger("de_registered","De-Registered");
    public static final DBString       GROUP = new DBString("group", "Group", 50 );

    public DBInteger      idx = IDX.getCopy();   
    public DBString       member_registration_number = MEMBER_REGISTRATION_NUMBER.getCopy();
    public DBString       name = NAME.getCopy();
    public DBString       forname = FORNAME.getCopy();
    public DBDateTime     entry_date = ENTRY_DATE.getCopy();
    public DBHistory      hist = (DBHistory) HIST.getCopy();
    public DBInteger      bp_idx = BP_IDX.getCopy();
    public DBString       note = NOTE.getCopy();
    public DBString       tel = TEL.getCopy();
    public DBFlagInteger  inaktiv = (DBFlagInteger) INAKTIV.getCopy();
    public DBFlagInteger  de_registered = (DBFlagInteger) DE_REGISTERED.getCopy();
    public DBString       group = GROUP.getCopy();
    
    public DBMember()
    {
        super( "MEMBER" );
        
        add(idx);
        add(bp_idx);
        add(member_registration_number);
        add(name);
        add(forname);
        add(entry_date);
        add(hist);
        add(note);
        add(tel);
        add(inaktiv);
        add(de_registered);
        add(group);
        
        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();

        addForeignKey(FK_BILLING_PERIOD, 2);

        hist.setTitle(" ");
        
        setVersion(2);
    }

    @Override
    public DBStrukt getNewOne() {
        return new DBMember();
    }
    
}
