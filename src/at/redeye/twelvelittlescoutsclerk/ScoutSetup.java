/**
 * TwelveLittleScoutsClerk common functions on table Member 
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.bindtypes.DBStrukt;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBGroup;
import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author martin
 */
public class ScoutSetup extends SetupDatabase {
    
    public ScoutSetup( MainWinInterface mainwin )
    {
        super(mainwin);
        
        tables_to_init.add( new DBGroup() );
    }
    
    @Override
    public void init( DBStrukt table ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        if( table instanceof DBGroup ) {
            GroupHelper.init_groups(mainwin, mainwin.getRoot().getDBConnection().getDefaultTransaction());
        }
    }    
    
  
    
}
