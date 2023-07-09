/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author martin
 */
public class UpdateMember 
{
    Transaction trans;
    Root root;
    Audit audit;    
        
    public UpdateMember( Root root, Transaction trans, Audit audit )
    {
        this.trans = trans;           
        this.root = root;
        this.audit = audit;
    }
       
    /**
     * Schreibt alle Audit Einträger, wenn sich Kundendaten verändert haben
     * @param kunden_old
     * @param kunden_new 
     */
    public void auditKundenDiffAndUpdate( DBMember kunden_old, DBMember kunden_new ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
         boolean differs = false;
         
        ArrayList<String> messages = new ArrayList();
        
        if( !kunden_old.vorname.getValue().equals(kunden_new.vorname.getValue()) ) {            
            differs = true;
            messages.add(String.format(root.MlM("Änderung des Vornamens von '%s' auf '%s'"),kunden_old.vorname, kunden_new.vorname));
        }
        
        if( !kunden_old.name.getValue().equals(kunden_new.name.getValue()) ) {
            differs = true;
            messages.add(String.format(root.MlM("Änderung des Namens von '%s' auf '%s'"),kunden_old.name, kunden_new.name));
        }
        
        if( !kunden_old.kundennummer.getValue().equals(kunden_new.kundennummer.getValue()) ) {
            differs = true;
            messages.add(String.format(root.MlM("Änderung der Kundennummer von '%s' auf '%s'"),kunden_old.kundennummer, kunden_new.kundennummer));
        }
        
        if( !kunden_old.eintrittsdatum.getDateStr().equals(kunden_new.eintrittsdatum.getDateStr() )) {
            differs = true;
            messages.add(String.format(root.MlM("Änderung des Eintrittsdatums von '%s' auf '%s'"),
                    kunden_old.eintrittsdatum.getDateStr(), kunden_new.eintrittsdatum.getDateStr()));
        }
               
        if( differs )
        {                        
            kunden_new.hist.setAeHist(root.getUserName());
            
            audit.addMessage4Kunde(trans, kunden_new.idx.getValue(), messages);
            
            trans.updateValues(kunden_new);
        }                
    }
    
    
}
