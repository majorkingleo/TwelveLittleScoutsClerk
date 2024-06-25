package at.redeye.twelvelittlescoutsclerk;

/**
 * TwelveLittleScoutsClerk common functions on table Member 
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */

import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import java.sql.SQLException;
import java.util.List;


public class ContactHelper {
 
    public static List<DBContact> fetch_contacts( Transaction trans, int bpidx ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {
        DBContact contact = new DBContact();
        
        List<DBContact> contacts = trans.fetchTable2(contact,
                        "where " + trans.markColumn(contact.bp_idx) + " = " + bpidx
                      + " order by " + trans.markColumn(contact.name));
                
                
        return contacts;
    }
    
}
