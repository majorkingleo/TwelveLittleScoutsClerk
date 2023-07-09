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
    public static final String KUNR_IDX_SEQUENCE = "KUNR_IDX_SEQUENCE";
    
    public DBInteger      idx = new DBInteger("idx", "Idx");   
    public DBString       kundennummer = new DBString("kundennummer", "Kundennummmer", 50);
    public DBString       name = new DBString("name", "Name", 50 );
    public DBString       vorname = new DBString("vorname", "Vorname", 50 );
    public DBDateTime     eintrittsdatum = new DBDateTime("eintrittsdatum", "Eintrittsdatum");    
    public DBHistory      hist = new DBHistory( "hist" );
    public DBInteger      az_idx = new DBInteger( "az_idx" );    
    public DBString       note = new DBString("note", "Notiz", 300);
    public DBString       tel = new DBString("tel", "Telfonnummer", 50 );
    public DBFlagInteger  inaktiv = new DBFlagInteger("inaktiv","Inaktiv");
    public DBFlagInteger  gekuendigt = new DBFlagInteger("gekuendigt","gekündigt");
    
    public DBMember()
    {
        super( "KUNDEN" );
        
        add(idx);
        add(az_idx);
        add(kundennummer);
        add(name);
        add(vorname);
        add(eintrittsdatum);
        add(hist);
        add(note);
        add(tel);
        add(inaktiv);
        add(gekuendigt);
        
        idx.setAsPrimaryKey();
        az_idx.shouldHaveIndex();        
        
        hist.setTitle(" ");
        
        setVersion(8);
    }

    @Override
    public DBStrukt getNewOne() {
        return new DBMember();
    }
    
}
