/**
 * TwelveLittleScoutsClerk Dialog for Event table
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_event;

import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEventMember;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author martin
 */
public class EventHelper 
{
    public static void delete_event( Transaction trans, DBEvent event )  throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        DBEventMember em = new DBEventMember();

        List<DBEventMember> values = trans.fetchTable2(em,
                "where " + trans.markColumn(em.bp_idx) + " = " + event.bp_idx.toString()
                + " and " + trans.markColumn(em.event_idx) + " = " + event.idx.toString()
                + " order by " + trans.markColumn(em.name));

        for (DBEventMember entry : values) {
            trans.deleteWithPrimaryKey(entry);
        }
        
        trans.deleteWithPrimaryKey(event);
    }
            
}
