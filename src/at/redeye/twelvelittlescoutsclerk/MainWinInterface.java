/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.Root;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author martin
 */
public interface MainWinInterface 
{
    public Integer getBPIdx();

    /**
     * Ermittelt den nächsten Wert für eine gegebene Sequenz
     *
     * @param seqName
     * @return den nächsten Wert der Sequenz
     * @throws java.sql.SQLException
     * @throws
     * at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException
     * @throws WrongBindFileFormatException
     * @throws TableBindingNotRegisteredException
     * @throws IOException
     */
    public int getNewSequenceValue(String seqName) throws SQLException,
            UnsupportedDBDataTypeException, WrongBindFileFormatException,
            TableBindingNotRegisteredException, IOException;

    public Audit getAudit();
    
    public Root getRoot();
}
