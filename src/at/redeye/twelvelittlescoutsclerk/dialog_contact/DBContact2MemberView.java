/**
 * TwelveLittleScoutsClerk Member search dialog
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_contact;
import at.redeye.FrameWork.base.bindtypes.*;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Contacts;

/**
 *
 * @author martin
 */
public class DBContact2MemberView extends DBStrukt
{ 
    DBMembers2Contacts m2c = new DBMembers2Contacts();
    DBMember member = new DBMember();
    
    public DBContact2MemberView()
    {
        super("Contact2MemberView");
        
        add( m2c );
        add( member );
        
        m2c.setTitle(" ");
        member.setTitle(" ");
        
        setVersion(1);
    }

    @Override
    public DBStrukt getNewOne() {
        return new DBContact2MemberView();
    }
    
    
}
