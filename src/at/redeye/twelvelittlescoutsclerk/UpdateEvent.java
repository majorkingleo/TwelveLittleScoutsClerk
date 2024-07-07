/**
 * TwelveLittleScoutsClerk common functions on table Member 
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author martin
 */
public class UpdateEvent 
{
    Transaction trans;
    Root root;
    Audit audit;    
        
    public UpdateEvent( Root root, Transaction trans, Audit audit )
    {
        this.trans = trans;           
        this.root = root;
        this.audit = audit;
    }
       
    /**
     * Schreibt alle Audit Einträger, wenn sich Kundendaten verändert haben
     * @param event_old
     * @param event_new 
     */
    public void auditEventDiffAndUpdate( DBEvent event_old, DBEvent event_new ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
         boolean differs = false;
         
        ArrayList<String> messages = new ArrayList();
        
        if( !event_old.name.getValue().equals(event_new.name.getValue()) ) {            
            differs = true;
            messages.add(String.format(root.MlM("Change of the event name from '%s' to '%s'"),event_old.name, event_new.name));
        }
        
        if( !event_old.costs.getValue().equals(event_new.costs.getValue()) ) {
            differs = true;
            messages.add(String.format(root.MlM("Change of costs from '%f' to '%f'"),event_old.costs.getValue(), event_new.costs.getValue()));
        }
        
               
        if( differs )
        {                        
            event_new.hist.setAeHist(root.getUserName());
            
            audit.addMessages(trans, messages);
            
            trans.updateValues(event_new);
        }
    }
    
    
}
