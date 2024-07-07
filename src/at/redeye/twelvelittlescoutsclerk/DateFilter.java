/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.bindtypes.DBDateTime;
import at.redeye.FrameWork.base.transaction.Transaction;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.joda.time.LocalDate;

/**
 *
 * @author martin
 */
public class DateFilter 
{    
    private static final Calendar cal_70_er = new GregorianCalendar(1970,1,3);
 
    
    public static String getVonBisFilter( Transaction trans, DBDateTime von , DBDateTime bis, DBDateTime column )            
    {
        if( von.getValue().getTime() < cal_70_er.getTimeInMillis() && bis.getValue().getTime() < cal_70_er.getTimeInMillis() )
        {
            return "";
        }
        else if( von.getValue().getTime() < cal_70_er.getTimeInMillis() ) 
        {
            return " and " + trans.getLowerDate(column, new LocalDate(bis.getValue()));
        } 
        else if(  bis.getValue().getTime() < cal_70_er.getTimeInMillis() ) 
        {
            return " and " + trans.getHigherDate(column, new LocalDate(von.getValue()));
        } else {
            Date dvon = von.getValue();
            Date dbis = bis.getValue();
            
            if( dbis.getTime() < dvon.getTime() ) {
                Date dtemp = dvon;
                dvon = dbis;
                dbis = dtemp;                
            }
            
            return " and " + trans.getPeriodStmt(column, new LocalDate(dvon), new LocalDate(dbis));
        }
    }    
}
