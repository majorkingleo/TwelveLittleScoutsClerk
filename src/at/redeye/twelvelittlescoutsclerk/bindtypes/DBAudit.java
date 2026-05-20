/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBDateTime;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

/**
 *
 * @author martin
 */
public class DBAudit extends DBStrukt
{
    public static String AUDIT_SEQUENCE = "AUDIT_IDX_SEQ";
    
    public static final DBInteger   IDX         = new DBInteger("idx", "Idx");
    public static final DBInteger   AUDIT_IDX   = new DBInteger("audit_idx", "Idx");
    public static final DBInteger   BP_IDX      = new DBInteger( "bp_idx" );
    public static final DBInteger   MEMBER_IDX  = new DBInteger("member_idx", "Idx");
    public static final DBString    MESSAGE     = new DBString( "message", "Text", 3000 );
    public static final DBDateTime  DATE        = new DBDateTime( "date", "Date" );
    public static final DBString    USER        = new DBString("user", "User", 50);

    
    public DBInteger    idx         = IDX.getCopy();
    public DBInteger    audit_idx   = AUDIT_IDX.getCopy();
    public DBInteger    bp_idx      = BP_IDX.getCopy();
    public DBInteger    member_idx  = MEMBER_IDX.getCopy();
    public DBString     message     = MESSAGE.getCopy();
    public DBDateTime   date        = DATE.getCopy();
    public DBString     user        = USER.getCopy();

    public DBAudit()
    {
        super( "AUDIT" );
        
        add( idx );
        add( bp_idx );
        add( member_idx );
        add( message );
        add( audit_idx );
        add( date );
        add( user );
        
        idx.setAsPrimaryKey();
        bp_idx.shouldHaveIndex();
        member_idx.shouldHaveIndex();        
        audit_idx.shouldHaveIndex();
        date.shouldHaveIndex();
        
        setVersion(1);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBAudit();
    }
    
}
