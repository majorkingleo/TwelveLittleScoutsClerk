/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.redeye.twelvelittlescoutsclerk.test.db;

import at.redeye.FrameWork.base.Root;
import at.redeye.SqlDBInterface.SqlDBConnection.impl.MissingConnectionParamException;
import at.redeye.SqlDBInterface.SqlDBConnection.impl.UnSupportedDatabaseException;
import java.sql.SQLException;

/**
 *
 * @author martin
 */
public interface SetupTestDBInterface
{
    Root getRoot();
    void invoke() throws ClassNotFoundException, UnSupportedDatabaseException, SQLException, MissingConnectionParamException;

    public void close();

}
