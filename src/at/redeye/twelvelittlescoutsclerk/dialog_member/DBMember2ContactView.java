/**
 * TwelveLittleScoutsClerk Member search dialog
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_member;
import at.redeye.FrameWork.base.bindtypes.*;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Contacts;

/**
 *
 * @author martin
 */
public class DBMember2ContactView extends DBStrukt
{ 
    DBMembers2Contacts m2c = new DBMembers2Contacts();
    DBContact contact = new DBContact();
    
    public DBMember2ContactView()
    {
        super("Member2ContactView");
        
        add( m2c );
        add( contact );
        
        setVersion(1);
    }

    @Override
    public DBStrukt getNewOne() {
        return new DBMember2ContactView();
    }
    
    
}
