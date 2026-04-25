/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBGroup;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author martin
 */
public class GroupHelper {
    
    public static HashMap<String,DBGroup> fetch_groups_by_name(Transaction trans, int bpidx ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {
        DBGroup group = new DBGroup();                
        List<DBGroup> groups = trans.fetchTable2(group);        
        HashMap<String,DBGroup> map_g = new HashMap<String,DBGroup>();
        
        for( DBGroup g : groups ) {
            map_g.put( g.name.getValue(), g);
        }
        
        return map_g;
    }

    public static void init_groups(MainWinInterface mainwin, Transaction trans) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException 
    {      
        {
            DBGroup group = new DBGroup();
            group.idx.loadFromCopy(mainwin.getNewSequenceValue(DBGroup.GROUP_IDX_SEQUENCE));
            group.hist.setAnHist(mainwin.getRoot().getLogin());
            group.name.loadFromCopy("WiWö");
            trans.insertValues(group);
        }
        
        {
            DBGroup group = new DBGroup();
            group.idx.loadFromCopy(mainwin.getNewSequenceValue(DBGroup.GROUP_IDX_SEQUENCE));
            group.hist.setAnHist(mainwin.getRoot().getLogin());
            group.name.loadFromCopy("GuSp");
            trans.insertValues(group);
        }
        
        {
            DBGroup group = new DBGroup();
            group.idx.loadFromCopy(mainwin.getNewSequenceValue(DBGroup.GROUP_IDX_SEQUENCE));
            group.hist.setAnHist(mainwin.getRoot().getLogin());
            group.name.loadFromCopy("CaEx");
            trans.insertValues(group);
        }
        
        {
            DBGroup group = new DBGroup();
            group.idx.loadFromCopy(mainwin.getNewSequenceValue(DBGroup.GROUP_IDX_SEQUENCE));
            group.hist.setAnHist(mainwin.getRoot().getLogin());
            group.name.loadFromCopy("RaRo");
            trans.insertValues(group);
        }
        
        {
            DBGroup group = new DBGroup();
            group.idx.loadFromCopy(mainwin.getNewSequenceValue(DBGroup.GROUP_IDX_SEQUENCE));
            group.hist.setAnHist(mainwin.getRoot().getLogin());
            group.name.loadFromCopy("Leiter");
            trans.insertValues(group);
        }
        
        {
            DBGroup group = new DBGroup();
            group.idx.loadFromCopy(mainwin.getNewSequenceValue(DBGroup.GROUP_IDX_SEQUENCE));
            group.hist.setAnHist(mainwin.getRoot().getLogin());
            group.name.loadFromCopy("Funktionär");
            trans.insertValues(group);
        }        
    }
}
