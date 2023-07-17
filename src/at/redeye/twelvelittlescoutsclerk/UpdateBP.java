/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillingPeriod;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class UpdateBP {
    
    private static final Logger logger = Logger.getLogger(UpdateBP.class);
    
    Transaction trans;
    NewSequenceValueInterface dialog;
    
    public UpdateBP( Transaction trans, NewSequenceValueInterface dialog )
    {
        this.trans = trans;
        this.dialog = dialog;
    }
    
    /**
     * Kopiert alle Daten vom az_to_copy_from in den neuen AZ.
     * @param new_az_idx
     * @param az_to_copy_from
     * @throws SQLException
     * @throws UnsupportedDBDataTypeException
     * @throws WrongBindFileFormatException
     * @throws TableBindingNotRegisteredException
     * @throws IOException 
     */
    public void copyData2NewAZ( int new_az_idx, DBBillingPeriod az_to_copy_from ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        if( new_az_idx == az_to_copy_from.idx.getValue() )
        {
            throw new RuntimeException("Neuer und alter az_idx sind gleich: " + new_az_idx);
        }
        /*
        DBBonus bonus = new DBBonus();
        DBKunden kunden = new DBKunden();
        DBPrime prime = new DBPrime();
        DBAudit audit = new DBAudit();
        DBAnzahlungen anzahlungen = new DBAnzahlungen();
        
        List<DBBonus> bonus_list;
        List<DBKunden> kunden_list;
        List<DBPrime> prime_list;
        List<DBAudit> audit_list;
        List<DBAnzahlungen> anzahlungen_list;
        
        if( az_to_copy_from.idx.getValue() > 0 )
        {
            bonus_list = trans.fetchTable2(bonus, " where " + trans.markColumn(bonus.az_idx) + " = " + az_to_copy_from.idx.getValue() );
            kunden_list = trans.fetchTable2(kunden, " where " + trans.markColumn(kunden.az_idx) + " = " + az_to_copy_from.idx.getValue() );       
            prime_list = trans.fetchTable2(prime, " where " + trans.markColumn(prime.az_idx) + " = " + az_to_copy_from.idx.getValue() );    
            audit_list = trans.fetchTable2(audit, " where " + trans.markColumn(audit.az_idx) + " = " + az_to_copy_from.idx.getValue() );    
            anzahlungen_list = trans.fetchTable2(anzahlungen, " where " + trans.markColumn(audit.az_idx) + " = " + az_to_copy_from.idx.getValue() );    
            
        } else {
            bonus_list = trans.fetchTable2(bonus);
            kunden_list = trans.fetchTable2(kunden);
            prime_list = trans.fetchTable2(prime);
            audit_list = trans.fetchTable2(audit);
            anzahlungen_list = trans.fetchTable2(anzahlungen);
        }
        
        if( bonus_list == null || bonus_list.isEmpty() )
            return;

        
        int bonus_idx_counter = 0;
        int prime_idx_counter = 0;
        int audit_idx_counter = 0;
        
        if( !bonus_list.isEmpty() )
            bonus_idx_counter = dialog.getNewSequenceValues(DBBonus.KUNR_IDX_SEQUENCE, bonus_list.size());
        
        if( !prime_list.isEmpty() )
            prime_idx_counter = dialog.getNewSequenceValues(prime.getName(), prime_list.size());
        
        if( !audit_list.isEmpty() )
            audit_idx_counter = dialog.getNewSequenceValues(audit.getName(), audit_list.size());
                
        // Map mit alt, und neuem bonus_idx 
        HashMap<Integer,Integer> gast_map = new HashMap();
        
        // change idx
        for( DBBonus entry : bonus_list )
        {
            entry.az_idx.loadFromCopy(new_az_idx);
            int old_idx = entry.idx.getValue();
            
            for( DBBonus other : bonus_list )
            {                
                if( other.gast1_idx.getValue().equals(old_idx) ) {
                    other.gast1_idx.loadFromCopy(bonus_idx_counter);
                }    
                if( other.gast2_idx.getValue().equals(old_idx) ) {
                    other.gast2_idx.loadFromCopy(bonus_idx_counter);
                }   
                if( other.seminar_berater_idx.getValue().equals(old_idx) ) {
                    other.seminar_berater_idx.loadFromCopy(bonus_idx_counter);
                }   
                if( other.mentor_idx.getValue().equals(old_idx) ) {
                    other.mentor_idx.loadFromCopy(bonus_idx_counter);
                }    
                if( other.gebracht_von_idx.getValue().equals(old_idx) ) {
                    other.gebracht_von_idx.loadFromCopy(bonus_idx_counter);
                }        
                if( other.trainer_idx.getValue().equals(old_idx) ) {
                    other.trainer_idx.loadFromCopy(bonus_idx_counter);
                }                                   
            }
            
            entry.idx.loadFromCopy(bonus_idx_counter);       
            
            logger.debug("Kunden Map: " + old_idx + " => " + bonus_idx_counter );
            gast_map.put(old_idx, bonus_idx_counter);
            
            trans.insertValues(entry);
            
            for( DBKunden kunde : kunden_list )
            {
                if( kunde.idx.getValue()== old_idx )
                {
                    kunde.az_idx.loadFromCopy(new_az_idx);
                    kunde.idx.loadFromCopy(bonus_idx_counter);
                    trans.insertValues(kunde);
                    break;
                }
            }                     
            
            bonus_idx_counter++;
        }                
        
        // so, wir haben oben alle idx geändert.
        // aber die gast_idx, mentor_idx, sembarberater_idx wurde eventuell
        // nach dem insert nochmal verändert. Daher müssen wir für
        // alle Einträge noch ein Update machen
        for( DBBonus entry : bonus_list )
        {
            trans.updateValues(entry);
        }        
        
        for (DBPrime p : prime_list) {
            p.bonus_idx.loadFromCopy(gast_map.get(p.bonus_idx.getValue()));
            p.az_idx.loadFromCopy(new_az_idx);
            p.idx.loadFromCopy(prime_idx_counter++);
            p.gast_idx.loadFromCopy(gast_map.get(p.gast_idx.getValue()));           
            
            if(  p.premie_von_idx.getValue() != 0 )
                p.premie_von_idx.loadFromCopy(gast_map.get(p.premie_von_idx.getValue()));
            
            trans.insertValues(p);
        }  
        
        for (DBAudit a : audit_list) {
            if( a.kunden_idx.getValue() > 0 ) {
                
                Integer new_kunden_idx = gast_map.get(a.kunden_idx.getValue());
                
                if( new_kunden_idx == null ) {
                    throw new NullPointerException("Kunden idx " + 
                            a.kunden_idx.getValue() +                             
                            " nicht in Kundenmap gefunden\n" + 
                            "Nachrichtentext: " + a.message.toString() );
                }
                
                a.kunden_idx.loadFromCopy(new_kunden_idx);            
            }
            
            a.idx.loadFromCopy(audit_idx_counter++);
            a.az_idx.loadFromCopy(new_az_idx);
            trans.insertValues(a);
        }     
        
        for (DBAnzahlungen a : anzahlungen_list) {
            if( a.kunden_idx.getValue() > 0 ) {
                
                Integer new_kunden_idx = gast_map.get(a.kunden_idx.getValue());
                
                if( new_kunden_idx == null ) {
                    throw new NullPointerException("Kunden idx " + 
                            a.kunden_idx.getValue() +                             
                            " nicht in Kundenmap gefunden\n" + 
                            "Nachrichtentext: " + a.betrag.toString() );
                }
                
                a.kunden_idx.loadFromCopy(new_kunden_idx);            
            }
            
            a.idx.loadFromCopy(audit_idx_counter++);
            a.az_idx.loadFromCopy(new_az_idx);
            trans.insertValues(a);
        }             
*/
    }

    void deleteAZ(int idx) throws SQLException 
    {
        /*
        DBBonus bonus = new DBBonus();
        DBKunden kunden = new DBKunden();
        DBPrime prime = new DBPrime();
        DBAudit audit = new DBAudit();
        DBAZ az = new DBAZ();
        DBAnzahlungen anzahlungen = new DBAnzahlungen();
        
        trans.updateValues("delete from " + trans.markTable(bonus) + " where " + trans.markColumn(bonus.az_idx) + " = " + idx );
        trans.updateValues("delete from " + trans.markTable(kunden) + " where " + trans.markColumn(kunden.az_idx) + " = " + idx );
        trans.updateValues("delete from " + trans.markTable(prime) + " where " + trans.markColumn(prime.az_idx) + " = " + idx );
        trans.updateValues("delete from " + trans.markTable(audit) + " where " + trans.markColumn(audit.az_idx) + " = " + idx );
        trans.updateValues("delete from " + trans.markTable(anzahlungen) + " where " + trans.markColumn(anzahlungen.az_idx) + " = " + idx );
        trans.updateValues("delete from " + trans.markTable(az) + " where " + trans.markColumn(az.idx) + " = " + idx );
*/
    }
    
}
