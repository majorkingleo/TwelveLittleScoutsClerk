/**
 * TwelveLittleScoutsClerk Dialog for Contact table
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_bookingline;

import at.redeye.FrameWork.base.Root;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.dialog_bookingline.BookingLine.ContactDescr;
import at.redeye.twelvelittlescoutsclerk.dialog_contact.Contact;
import at.redeye.twelvelittlescoutsclerk.dialog_contact.EditContact;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;

public class ActionPopupContacts  extends JPopupMenu
{
    private static final Logger logger = Logger.getLogger(ActionPopupContacts.class.getName());
    
    Root root;
    MainWin  mainwin;
    
    public ActionPopupContacts( final MainWin mainwin, final ContactDescr descr )
    {
        this.root = mainwin.getRoot();
        this.mainwin = mainwin;
        
        {
            var menuItem = new JMenuItem( mainwin.MESSAGE_GOTO_CONTACTS_DIALOG );        
            add( menuItem );        
            menuItem.addActionListener(new ActionListener() {

                   @Override
                   public void actionPerformed(ActionEvent e) {                    
                       mainwin.invokeDialog(new Contact(mainwin));
                   }
               });
        }
        
        
        if( descr != null ) 
        {
            var menuItem = new JMenuItem( String.format(mainwin.MESSAGE_EDIT_CONTACT_S,descr.toString()));
            add( menuItem );        
            menuItem.addActionListener(new ActionListener() {

                   @Override
                   public void actionPerformed(ActionEvent e) {                    
                       mainwin.invokeDialog(new EditContact(mainwin, descr.contact));
                   }
               });
        }        
    }
    
}
