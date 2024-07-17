/**
 * TwelveLittleScoutsClerk Dialog for Contact table
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_bookingline;

import at.redeye.FrameWork.base.Root;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.dialog_bookingline.BookingLine.EventDescr;
import at.redeye.twelvelittlescoutsclerk.dialog_event.EditEvent;
import at.redeye.twelvelittlescoutsclerk.dialog_event.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;

public class ActionPopupEvents  extends JPopupMenu
{
    private static final Logger logger = Logger.getLogger(ActionPopupEvents.class.getName());
    
    Root root;
    MainWin  mainwin;
    
    public ActionPopupEvents( final MainWin mainwin, final EventDescr descr )
    {
        this.root = mainwin.getRoot();
        this.mainwin = mainwin;
        
        {
            var menuItem = new JMenuItem( mainwin.MESSAGE_GOTO_EVENTS_DIALOG );        
            add( menuItem );        
            menuItem.addActionListener(new ActionListener() {

                   @Override
                   public void actionPerformed(ActionEvent e) {                    
                       mainwin.invokeDialog(new Event(mainwin));
                   }
               });
        }
        
        
        if( descr != null ) 
        {
            var menuItem = new JMenuItem( String.format(mainwin.MESSAGE_EDIT_EVENT_S,descr.toString()));
            add( menuItem );        
            menuItem.addActionListener(new ActionListener() {

                   @Override
                   public void actionPerformed(ActionEvent e) {                    
                       mainwin.invokeDialog(new EditEvent(mainwin, descr.event));
                   }
               });
        }        
    }
    
}
