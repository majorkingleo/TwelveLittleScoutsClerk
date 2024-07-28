/**
 * TwelveLittleScoutsClerk Dialog for Contact table
 * @author Copyright (c) 2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine2Events;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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
}
