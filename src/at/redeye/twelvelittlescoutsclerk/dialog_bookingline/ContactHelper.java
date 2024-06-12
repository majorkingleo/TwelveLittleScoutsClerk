/**
 * TwelveLittleScoutsClerk Dialog for Contact table
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_bookingline;

import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author martin
 */
public class ContactHelper 
{
    static final DBContact contact = new DBContact();
    
    private static List<DBContact> findContactsFor_Unsorted( Transaction trans, DBBookingLine line ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {
        List<DBContact> result = new ArrayList<>();
        
        if( !line.from_bank_account_bic.isEmpty() &&
            !line.from_bank_account_iban.isEmpty() ) {        
             List<DBContact> contacts = trans.fetchTable2(contact, 
                        "where " + trans.markColumn(contact.bp_idx) + " = " + line.bp_idx.toString() +
                        " and "  + trans.markColumn(contact.bank_account_bic) + " = '" + line.from_bank_account_bic + "'" +
                        " and "  + trans.markColumn(contact.bank_account_iban) + " = '" + line.from_bank_account_iban + "'" );
             
             result.addAll(contacts);
        }
          
        if( !line.from_bank_account_iban.isEmpty() ) {        
             List<DBContact> contacts = trans.fetchTable2(contact, 
                        "where " + trans.markColumn(contact.bp_idx) + " = " + line.bp_idx.toString() +            
                        " and "  + trans.markColumn(contact.bank_account_iban) + " = '" + line.from_bank_account_iban + "'" );
             
             result.addAll(contacts);
        }

        List<DBContact> contacts = findContactsFor( trans, line.bp_idx.getValue(), line.from_name.getValue() );
        
        result.addAll(contacts);       
        
        return result;
    }
    
    public static List<DBContact> findContactsFor( Transaction trans, int bpidx, String text ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {
        List<DBContact> result = new ArrayList<>();
        String texts[] = text.split("[\t ,\\-]");        
        
        for( String t : texts ) {
            List<DBContact> contacts = trans.fetchTable2(contact, 
                    "where " + trans.markColumn(contact.bp_idx) + " = " + String.valueOf(bpidx) +
                    " and ( " + trans.markColumn(contact.name) + " like '%" + t + "%'" +
                    "   or " + trans.markColumn(contact.forname) + " like '%" + t + "%' ) " );
            
            result.addAll(contacts);
        }
        
        return result;
    }
    
    public static List<DBContact> findContactsFor( Transaction trans, DBBookingLine line ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {
        List<DBContact> result = findContactsFor_Unsorted( trans, line );
        
        HashMap<Integer, ArrayList<DBContact>> weight_map = new HashMap<>();
                
        for( DBContact contact : result ) {
            ArrayList<DBContact> cont = weight_map.get(contact.idx.getValue());
            if( cont == null ) {
                cont = new ArrayList<DBContact>();
                weight_map.put(contact.idx.getValue(), cont);
            }
            
            cont.add(contact);
        }
        
        ArrayList<AbstractMap.SimpleEntry> weighted_list = new ArrayList<HashMap.SimpleEntry>();
        
        for( Map.Entry<Integer, ArrayList<DBContact>> cont : weight_map.entrySet() ) {
            weighted_list.add( new HashMap.SimpleEntry( cont.getValue().size(), cont.getValue().get(0)));
        }
        
        weighted_list.sort(new Comparator<AbstractMap.SimpleEntry>(){
            @Override
            public int compare(AbstractMap.SimpleEntry a, AbstractMap.SimpleEntry b) {
                return Integer.compare((Integer)a.getKey(), (Integer)b.getKey());
            }            
        });
        
        result.clear();
        
        for( AbstractMap.SimpleEntry<Integer,DBContact> entry : weighted_list ) {
            result.add(entry.getValue());
        }
        
        Collections.reverse(result);
        
        for( DBContact contact : result ) {
            System.out.println( "Contact: " + contact.idx.toString() + " " + contact.forname.getValue() + " " + contact.name.getValue() );
        }
        
        return result;
    }
    
}
