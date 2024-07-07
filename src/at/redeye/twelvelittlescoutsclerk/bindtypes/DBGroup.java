/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

/**
 *
 * @author martin
 */
public class DBGroup extends DBStrukt 
{
    public static final String GROUP_IDX_SEQUENCE = "GROUP_IDX_SEQUENCE";
    
    public DBInteger      idx = new DBInteger("idx", "Idx");   
    public DBString       name = new DBString("name", "Name", 50 );    
    public DBHistory      hist = new DBHistory( "hist" );
    
    public DBGroup()
    {
        super("GROUP");
        
        add( idx );
        add( name );
        add( hist );
        
        hist.setTitle(" ");
        
        idx.setAsPrimaryKey();        
    }

    
    @Override
    public DBStrukt getNewOne() {
        return new DBGroup();
    }
    
}
