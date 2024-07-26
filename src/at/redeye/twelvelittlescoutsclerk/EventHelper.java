/**
 * TwelveLittleScoutsClerk common functions on table Member 
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.FrameWork.utilities.StringUtils;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine2Events;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEventMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import java.io.IOException;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class EventHelper {
           
    public static List<DBEvent> findEventsFor( Transaction trans, DBMember member ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {        
        DBEvent event = new DBEvent();
        DBEventMember em = new DBEventMember();
                
        
        List<DBEvent> events = trans.fetchTable2(event, 
            "where " + trans.markColumn(event.bp_idx) + " = " + member.bp_idx.toString() +
            " and exists ( select 1 from " + trans.markTable(em) + " where " + trans.markColumn(em.bp_idx) + " = " + trans.markColumn(event,event.bp_idx) +
            "       and " + trans.markColumn(em.member_idx) + " = " + member.idx.toString() +
            "       and " + trans.markColumn(em.event_idx) + " = " + trans.markColumn(event,event.idx) + " ) " );
        
        System.out.println(StringUtils.autoLineBreak(trans.getSql()));
        
        return events;
    }
    
        public static void delete_event( Transaction trans, DBEvent event )  throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        List<DBEventMember> values = get_members_4_event( trans, event );

        for (DBEventMember entry : values) {
            trans.deleteWithPrimaryKey(entry);
        }
        
        trans.deleteWithPrimaryKey(event);
    }
    
    public static List<DBEventMember> get_members_4_event( Transaction trans, DBEvent event )  throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        DBEventMember em = new DBEventMember();

        List<DBEventMember> values = trans.fetchTable2(em,
                "where " + trans.markColumn(em.bp_idx) + " = " + event.bp_idx.toString()
                + " and " + trans.markColumn(em.event_idx) + " = " + event.idx.toString()
                + " order by " + trans.markColumn(em.name));
        
        return values;
    }
    
    public static List<DBBookingLine> get_bookinglines_4_event( Transaction trans, DBEvent event )  throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        DBBookingLine line = new DBBookingLine();
        DBEventMember em = new DBEventMember();
        DBBookingLine2Events l2e = new DBBookingLine2Events();
        
        List<DBBookingLine> lines = trans.fetchTable2(line,
                " where " + trans.markColumn(line,line.idx) + 
                " in ( select " + trans.markColumn(l2e, l2e.bl_idx) + " from " + trans.markTable(l2e) +
                "      where " + trans.markColumn(l2e, l2e.event_idx) + " = " + event.idx.toString() + " ) " );
        
        return lines;
    }    
    
    public static List<HashMap.Entry<DBBookingLine,DBBookingLine2Events>> get_bookinglines_and_mapping_4_event( Transaction trans, DBEvent event )  throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        DBBookingLine line = new DBBookingLine();
        DBEventMember em = new DBEventMember();
        DBBookingLine2Events l2e = new DBBookingLine2Events();
        
        List<DBBookingLine> lines = get_bookinglines_4_event( trans, event );
        
        List<DBBookingLine2Events> l2es = trans.fetchTable2(l2e, 
                " where " + trans.markColumn(l2e, l2e.event_idx) + " = " + event.idx.toString() );
                       
        List<HashMap.Entry<DBBookingLine,DBBookingLine2Events>> ret = new ArrayList<>();
        
        for( var l : lines ) {
            for( var le : l2es ) {
                if( l.idx.getValue().equals(le.bl_idx.getValue()) ) {
                    ret.add( new SimpleEntry(l,le) );
                    break;
                }
            }
        }
        
        return ret;
    } 
    
    public static void calc_paid_values_4_event( Transaction trans, DBEvent event ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        List<DBEventMember> event_members = get_members_4_event( trans, event );
        List<HashMap.Entry<DBBookingLine,DBBookingLine2Events>> lines = get_bookinglines_and_mapping_4_event( trans, event );
        
        HashMap<Integer,Double> member2amount = new HashMap<>();
        
        for( var em : event_members ) {
             member2amount.put( em.member_idx.getValue(), 0.0 );
        }
        
        for( var em : event_members ) {
            for( var e : lines ) {
                if( em.member_idx.getValue().equals(e.getValue().member_idx.getValue()) ) {
                    var m2e = member2amount.get(em.member_idx.getValue());
                    member2amount.put( em.member_idx.getValue(), m2e + e.getKey().amount.getValue() );                    
                } // if
            } // for
        } // for
        
        for( var em : event_members ) {
            em.paid.loadFromCopy(member2amount.get(em.member_idx.getValue()));
            trans.updateValues(em);
        }
    }
    
    /**
     * 
     * @param trans
     * @param member
     * @param event
     * @return null if not found
     * @throws SQLException
     * @throws TableBindingNotRegisteredException
     * @throws UnsupportedDBDataTypeException
     * @throws WrongBindFileFormatException
     * @throws IOException 
     */
    public static DBEventMember get_event_member( Transaction trans, DBMember member, DBEvent event ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        DBEventMember em = new DBEventMember();
        
        List<DBEventMember> ems = trans.fetchTable2(em, 
                " where " + trans.markColumn(em, em.member_idx) + " = " + member.idx.toString() +
                " and " + trans.markColumn(em, em.event_idx) + " = " + event.idx.toString() );
                
        if( ems.isEmpty() ) {
            return null;
        }
        
        return ems.get(0);
    }
}
