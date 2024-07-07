/**
 * TwelveLittleScoutsClerk common functions on table Member 
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;
import at.redeye.FrameWork.base.dbmanager.impl.ShowTablesMSSql;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.DBDataType;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author martin
 */
public abstract class SetupDatabase {
    
    protected MainWinInterface mainwin;
    protected ArrayList<DBStrukt> tables_to_init = new ArrayList<>();
    protected ArrayList<DBStrukt> tables_to_check = new ArrayList<>();
    protected Transaction trans;
    
    public SetupDatabase( MainWinInterface mainwin )
    {
        this.mainwin = mainwin;
        trans = mainwin.getRoot().getDBConnection().getDefaultTransaction();
    }
    
    public boolean isEmpty() throws SQLException, UnsupportedDBDataTypeException
    {
        for( DBStrukt table : tables_to_init ) {
            String sql = "SELECT count(*) FROM " + trans.markTable(table);

            List<DBDataType> args = new Vector<DBDataType>();
            args.add(DBDataType.DB_TYPE_INTEGER);
            List<List<?>> res;

            res = trans.getStmtExecInterface().fetchColumnValue(sql, args);
            
            int count = (Integer)res.get(0).get(0);
            
            if( count == 0 ) {
                tables_to_check.add(table);
            }
        }
        
        return !tables_to_check.isEmpty();
    }
    
    public void init() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        for( DBStrukt table : tables_to_check ) {
            init( table );
        }
        
        trans.commit();
    }
    
    public abstract void init( DBStrukt table ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException;    
}
