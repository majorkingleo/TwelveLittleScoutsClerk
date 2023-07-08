/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk.bindtypes;

import at.redeye.FrameWork.base.bindtypes.DBFlagInteger;
import at.redeye.FrameWork.base.bindtypes.DBHistory;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;

/**
 *
 * @author martin
 */
public class DBAZ extends DBStrukt 
{
    public DBInteger idx = new DBInteger("idx");
    public DBString  title = new DBString("title", "Abrechnunszeitraum", 50 );
    public DBString  comment = new DBString("comment", "Kommentar", 1000 );
    public DBHistory    hist = new DBHistory("hist");
    public DBFlagInteger locked = new DBFlagInteger("locked");
    
    
    public DBAZ()
    {
        super( "AZ" );
        
        add(idx);
        add(title);
        add(comment);
        add(hist);
        add(locked,2);
        
        idx.setAsPrimaryKey();       
        
        hist.setTitle(" ");
        
        setVersion(2);
    }

    @Override
    public DBStrukt getNewOne() {
        return new DBAZ();
    }
    
    public boolean isLocked()
    {
        return locked.getValue() > 0;
    }
    
}
