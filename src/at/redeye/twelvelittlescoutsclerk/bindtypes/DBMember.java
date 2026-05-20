/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.*;
import at.redeye.FrameWork.base.bindtypes.ForeignKeyDefinition;

/**
 *
 * @author martin
 */
public class DBMember extends DBStrukt
{
    public static final String MEMBERS_IDX_SEQUENCE = "MEMBERS_IDX_SEQUENCE";

    public static final ForeignKeyDefinition FK_BILLING_PERIOD =
        new ForeignKeyDefinition("bp_idx", "BILLING_PERIOD", "idx");

    // Column references for Condition API
    public static final DBInteger     IDX                        = new DBInteger("idx");
    public static final DBString      MEMBER_REGISTRATION_NUMBER = new DBString("member_registration_number", 1);
    public static final DBString      NAME                       = new DBString("name", 1);
    public static final DBString      FORNAME                    = new DBString("forname", 1);
    public static final DBDateTime    ENTRY_DATE                 = new DBDateTime("entry_date");
    public static final DBHistory     HIST                       = new DBHistory("hist");
    public static final DBInteger     BP_IDX                     = new DBInteger("bp_idx");
    public static final DBString      NOTE                       = new DBString("note", 1);
    public static final DBString      TEL                        = new DBString("tel", 1);
    public static final DBFlagInteger INAKTIV                    = new DBFlagInteger("inactiv");
    public static final DBFlagInteger DE_REGISTERED              = new DBFlagInteger("de_registered");
    public static final DBString      GROUP                      = new DBString("group", 1);

    public DBInteger      idx                         = IDX.getCopy();
    public DBString       member_registration_number  = MEMBER_REGISTRATION_NUMBER.getCopy();
    public DBString       name                        = NAME.getCopy();
    public DBString       forname                     = FORNAME.getCopy();
    public DBDateTime     entry_date                  = ENTRY_DATE.getCopy();
    public DBHistory      hist                        = (DBHistory)HIST.getCopy();
    public DBInteger      bp_idx                      = BP_IDX.getCopy();
    public DBString       note                        = NOTE.getCopy();
    public DBString       tel                         = TEL.getCopy();
    public DBFlagInteger  inaktiv                     = new DBFlagInteger(INAKTIV.getName(), "Inactiv");
    public DBFlagInteger  de_registered               = new DBFlagInteger(DE_REGISTERED.getName(), "De-Registered");
    public DBString       group                       = GROUP.getCopy();
    
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
