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
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEventMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import java.sql.SQLException;
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
}
