/**
 * TwelveLittleScoutsClerk Dialog for Contact table
 * @author Copyright (c) 2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.DefaultInsertOrUpdater;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine2Events;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author martin
 */
public class BookingLineHelper 
{
    public static void delete_bookingline( Transaction trans, DBBookingLine line )  throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        DBBookingLine2Events b2l = new DBBookingLine2Events();
        DBEvent event = new DBEvent();
        
        List<DBBookingLine2Events> b2ls = trans.fetchTable2(b2l, " where " + trans.markColumn(b2l,b2l.bl_idx) + " = " + line.idx.toString() );
        
        if( !b2ls.isEmpty() ) {
            event.idx.loadFromCopy(b2ls.get(0).event_idx.getValue());
            
            for( var bl : b2ls ) {
                trans.deleteWithPrimaryKey(bl);
            }
            
            EventHelper.calc_paid_values_4_event(trans, event);
        }
        
        trans.deleteWithPrimaryKey(line);
    }

    public static void save( Transaction trans, 
                             MainWinInterface mainwin,
                             List<DBBookingLine2Events> bles_to_remove, 
                             List<DBBookingLine> values, 
                             Set<Integer> edited_rows,
                             HashMap<Integer,DBBookingLine2Events> bl2es ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        for( var le : bles_to_remove ) {
            trans.deleteWithPrimaryKey(le);
        }
        
        Set<Integer> events2update = new HashSet<>();
        
        for (Integer i : edited_rows) {

            if( i < 0 ) {
                continue;
            }
            
            DBBookingLine entry = values.get(i);            
            DBBookingLine2Events bl2e = bl2es.get(entry.idx.getValue());
            
            if( bl2e == null ) {
                DBBookingLine2Events bl2 = new DBBookingLine2Events();
                List<DBBookingLine2Events> to_delete = trans.fetchTable2(bl2," where " + trans.markColumn(bl2,bl2.bl_idx) + " = " + entry.idx.toString() );
                
                for( var td : to_delete ) {
                    events2update.add(td.event_idx.getValue());
                    trans.deleteWithPrimaryKey(td);
                }
                
                entry.assigned.loadFromCopy(0);
            } else {
                entry.assigned.loadFromCopy(1);
            }

            entry.hist.setAeHist(mainwin.getRoot().getUserName());
            trans.updateValues(entry);         
        }
        
        if( bl2es.size() > 0 ) {
            
            for( DBBookingLine2Events bl2e : bl2es.values() ) {
                if( bl2e.idx.getValue() == 0 ) {
                    bl2e.idx.loadFromCopy(mainwin.getNewSequenceValue(DBBookingLine2Events.BOOKINGLINE2EVENTS_IDX_SEQUENCE));
                }

                DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, bl2e);
            }            
        }
        
        List<DBEvent> events = getEvents4Update(trans, events2update, bl2es); 

        for( var event : events ) {
            EventHelper.calc_paid_values_4_event( trans, event );
        }
    }

    public static HashMap<Integer,DBBookingLine2Events> fetch_bookingline2events( Transaction trans, MainWinInterface mainwin ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        HashMap<Integer,DBBookingLine2Events> bl2es = new HashMap<>();
        DBBookingLine2Events bl2e = new DBBookingLine2Events();
        List<DBBookingLine2Events> l_ble = trans.fetchTable2(bl2e,
                "where " + trans.markColumn(bl2e.bp_idx) + " = " + mainwin.getBPIdx());
                    
        for( var bl2 : l_ble ) {
            bl2es.put(bl2.bl_idx.getValue(), bl2);
        }

        return bl2es;
    }

    /** returns an empty list if empty */
    private static List<DBEvent> getEvents4Update( Transaction trans, Set<Integer> events2update, HashMap<Integer,DBBookingLine2Events> bl2es ) throws UnsupportedDBDataTypeException, WrongBindFileFormatException, SQLException, TableBindingNotRegisteredException, IOException
    {
        HashSet<Integer> events = new HashSet<>();
        events.addAll(events2update);
        
        for( var bl2 : bl2es.values() ) {
            events.add(bl2.event_idx.getValue());
        }
        
        List<DBEvent> ret = new ArrayList<>();
        
        for( var event_idx : events ) {
            DBEvent ev = new DBEvent();
            ev.idx.loadFromCopy(event_idx);
            trans.fetchTableWithPrimkey(ev);
            ret.add(ev);
        }
        
        return ret;
    }
}
