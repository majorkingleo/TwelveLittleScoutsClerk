/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author martin
 */
public interface NewSequenceValueInterface {
        public int getNewSequenceValue(String seqName) throws SQLException,
            UnsupportedDBDataTypeException, WrongBindFileFormatException,
            TableBindingNotRegisteredException, IOException;
        
        public int getNewSequenceValues(String seqName, int cound) throws SQLException,
            UnsupportedDBDataTypeException, WrongBindFileFormatException,
            TableBindingNotRegisteredException, IOException;        
}
