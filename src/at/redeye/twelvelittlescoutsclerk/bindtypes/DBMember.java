/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    
    public DBInteger      idx = new DBInteger("idx", "Idx");   
    public DBString       member_registration_number = new DBString("member_registration_number", "Member Id", 50);
    public DBString       name = new DBString("name", "Name", 50 );
    public DBString       forname = new DBString("forname", "Forname", 50 );
    public DBDateTime     entry_date = new DBDateTime("entry_date", "Entry Date");
    public DBHistory      hist = new DBHistory( "hist" );
    public DBInteger      az_idx = new DBInteger( "az_idx" );
    public DBString       note = new DBString("note", "Notiz", 300);
    public DBString       tel = new DBString("tel", "Phone Number", 50 );
    public DBFlagInteger  inaktiv = new DBFlagInteger("inactiv","Inaktiv");
    public DBFlagInteger  de_registered = new DBFlagInteger("de_registered","De-Registered");
    
    public DBMember()
    {
        super( "MEMBERS" );
        
        add(idx);
        add(az_idx);
        add(member_registration_number);
        add(name);
        add(forname);
        add(entry_date);
        add(hist);
        add(note);
        add(tel);
        add(inaktiv);
        add(de_registered);
        
        idx.setAsPrimaryKey();
        az_idx.shouldHaveIndex();        
        
        hist.setTitle(" ");
        
        setVersion(1);
    }

    @Override
    public DBStrukt getNewOne() {
        return new DBMember();
    }
    
}
