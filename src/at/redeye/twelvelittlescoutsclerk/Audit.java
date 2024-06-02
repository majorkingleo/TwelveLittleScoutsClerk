/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBAudit;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class Audit {        
    
    private static final Logger logger = Logger.getLogger(Audit.class);
        
    MainWinInterface mainwin;
    int audit_idx;    
    
    public Audit( MainWinInterface mainwin )
    {
        this.mainwin = mainwin;
        this.audit_idx = 0;
    }
    
    public void openNewAudit() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        audit_idx = mainwin.getNewSequenceValue(DBAudit.AUDIT_SEQUENCE);
    }
    
    public DBAudit addMessage( Transaction trans, String message ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {        
        return addMessage4Kunde(trans, 0, message);
    }
    
    public void addMessages( Transaction trans, Collection<String> messages ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {    
        for( String message : messages ) {
         addMessage4Kunde(trans, 0, message);
        }
    }    
    
    public DBAudit addMessage4Kunde( Transaction trans, int kunden_idx, String message ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        DBAudit audit = new DBAudit();
        audit.bp_idx.loadFromCopy(mainwin.getBPIdx());
        audit.idx.loadFromCopy(mainwin.getNewSequenceValue(audit.getName()));
        audit.audit_idx.loadFromCopy(audit_idx);
        audit.message.loadFromCopy(message);
        audit.date.loadFromCopy(new Date());
        audit.member_idx.loadFromCopy(kunden_idx);
        audit.user.loadFromCopy(mainwin.getRoot().getUserName());
        
        trans.insertValues(audit);        
        if( kunden_idx > 0 ) {        
            logger.debug(String.format("AUDIT Kunden idx: %d: %s", kunden_idx, message ));
        } else {
            logger.debug(String.format("AUDIT %s", message ));
        }            
        
        return audit;
    }

    DBAudit addMessage4Kunde(Transaction trans, int kunden_idx, ArrayList<String> messages) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        StringBuilder sb = new StringBuilder();
        
        for( String msg : messages ) {
            sb.append(msg);
            sb.append("\n");
        }
        
        return addMessage4Kunde(trans, kunden_idx, sb.toString());
    }
    
}
