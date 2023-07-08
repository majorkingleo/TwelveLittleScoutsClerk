/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    
    public DBInteger idx = new DBInteger("idx", "Idx");
    public DBInteger audit_idx = new DBInteger("audit_idx", "Idx");
    public DBInteger az_idx = new DBInteger( "az_idx" );
    public DBInteger member_idx = new DBInteger("member_idx", "Idx");
    public DBString  message = new DBString( "message", "Text", 3000 );
    public DBDateTime date = new DBDateTime( "datum", "Datum" );
    public DBString  user = new DBString("user", "Benutzer", 50);

    
    public DBAudit()
    {
        super( "AUDIT" );
        
        add( idx );
        add( az_idx );
        add( member_idx );
        add( message );
        add( audit_idx );
        add( date );
        add( user, 2);
        
        idx.setAsPrimaryKey();
        az_idx.shouldHaveIndex();
        member_idx.shouldHaveIndex();        
        audit_idx.shouldHaveIndex();
        date.shouldHaveIndex();
        
        setVersion(2);
    }
    
    @Override
    public DBStrukt getNewOne() {
        return new DBAudit();
    }
    
}
