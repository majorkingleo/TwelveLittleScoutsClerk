/**
 * TwelveLittleScoutsClerk Dialog for Contact table
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_bookingline;

import at.redeye.FrameWork.base.Root;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.dialog_bookingline.BookingLine.MemberDescr;
import at.redeye.twelvelittlescoutsclerk.dialog_member.EditMember;
import at.redeye.twelvelittlescoutsclerk.dialog_member.Member;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;

public class ActionPopupMembers  extends JPopupMenu
{
    private static final Logger logger = Logger.getLogger(ActionPopupMembers.class.getName());
    
    Root root;
    MainWin  mainwin;
    
    public ActionPopupMembers( final MainWin mainwin, final MemberDescr descr )
    {
        this.root = mainwin.getRoot();
        this.mainwin = mainwin;
        
        {
            var menuItem = new JMenuItem( mainwin.MESSAGE_GOTO_MEMBERS_DIALOG );        
            add( menuItem );        
            menuItem.addActionListener(new ActionListener() {

                   @Override
                   public void actionPerformed(ActionEvent e) {                    
                       mainwin.invokeDialog(new Member(mainwin));
                   }
               });
        }
        
        
        if( descr != null ) 
        {
            var menuItem = new JMenuItem( String.format(mainwin.MESSAGE_EDIT_MEMBER_S,descr.toString()));
            add( menuItem );        
            menuItem.addActionListener(new ActionListener() {

                   @Override
                   public void actionPerformed(ActionEvent e) {                    
                       mainwin.invokeDialog(new EditMember(mainwin, descr.member));
                   }
               });
        }        
    }
    
}
