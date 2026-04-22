/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.redeye.twelvelittlescoutsclerk.test.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import at.redeye.FrameWork.base.BaseModuleLauncher;
import at.redeye.FrameWork.base.FrameWorkConfigDefinitions;
import at.redeye.FrameWork.base.LocalRoot;
import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.Setup;
import at.redeye.FrameWork.base.dbmanager.DBBindtypeManager;
import at.redeye.FrameWork.base.prm.bindtypes.DBConfig;
import at.redeye.FrameWork.base.sequence.bindtypes.DBSequences;
import at.redeye.FrameWork.base.transaction.MySQLTransaction;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBConnection.DbConnectionInterface;
import at.redeye.SqlDBInterface.SqlDBConnection.impl.ConnectionDefinition;
import at.redeye.SqlDBInterface.SqlDBConnection.impl.DBConnector;
import at.redeye.SqlDBInterface.SqlDBConnection.impl.MissingConnectionParamException;
import at.redeye.SqlDBInterface.SqlDBConnection.impl.SupportedDBMSTypes;
import at.redeye.SqlDBInterface.SqlDBConnection.impl.UnSupportedDatabaseException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.UserManagement.bindtypes.DBPb;
import at.redeye.twelvelittlescoutsclerk.AppConfigDefinitions;
import at.redeye.twelvelittlescoutsclerk.Audit;
import at.redeye.twelvelittlescoutsclerk.MainWinInterface;
import at.redeye.twelvelittlescoutsclerk.NewSequenceValueInterface;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBAudit;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillingPeriod;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine2Events;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEventMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBGroup;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Contacts;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Groups;

/**
 *
 * @author martin
 */
public class SetupTestDB extends BaseModuleLauncher implements SetupTestDBInterface, MainWinInterface, NewSequenceValueInterface
{
    private Transaction seq_transaction;
    private Audit audit;
    private static String DB_HOST = "192.168.1.200";
    private static String DB_NAME = "scouts_unit_test";
    
    public SetupTestDB()
    {
        root = new LocalRoot("TwelveLittleScoutsClerk-TEST","TwelveLittleScoutsClerk-TEST",true,false);

        this.configureLogging();
    }

    @Override
    public void invoke() throws ClassNotFoundException, UnSupportedDatabaseException, SQLException, MissingConnectionParamException
    {
        AppConfigDefinitions.registerDefinitions();
        FrameWorkConfigDefinitions.registerDefinitions();
        root.getBindtypeManager().register(new DBPb());
        root.getBindtypeManager().register(new DBSequences());
        root.getBindtypeManager().register(new DBConfig());
        root.getBindtypeManager().register(new DBBillingPeriod());
        root.getBindtypeManager().register(new DBMember());            
        root.getBindtypeManager().register(new DBAudit());
        root.getBindtypeManager().register(new DBContact());
        root.getBindtypeManager().register(new DBMembers2Contacts());
        root.getBindtypeManager().register(new DBMembers2Groups());
        root.getBindtypeManager().register(new DBGroup());
        root.getBindtypeManager().register(new DBEvent());
        root.getBindtypeManager().register(new DBEventMember());
        root.getBindtypeManager().register(new DBBookingLine2Events());
        root.getBindtypeManager().register(new DBBookingLine());

        autocreateInternalDB();
    }

    private boolean autocreateInternalDB() throws ClassNotFoundException, UnSupportedDatabaseException, SQLException, MissingConnectionParamException
    {
        ConnectionDefinition connparams = new ConnectionDefinition(
               DB_HOST,
               0,
               DB_NAME,
               DB_NAME,
               DB_NAME,
               SupportedDBMSTypes.DB_MYSQL
               );

        DbConnectionInterface connint = new DBConnector(connparams);

        Connection my_db_conn = connint.connectToDatabase();

        if( my_db_conn.isClosed() )
        {
            logger.error("Erzeugen der Datenbank " + DB_NAME + " nicht mögglich");
            return false;
        }

        Transaction t = new MySQLTransaction(connparams);

        DBBindtypeManager bindtypeManager = root.getBindtypeManager();

        bindtypeManager.setTransaction(t);

        if( bindtypeManager.autocreate() )
        {
             logger.info("Datenbank erfolgreich eingerichtet");
             t.commit();
        }
        else
        {
             logger.error( "Fehler beim Einrichten der Datenbank!" );
             t.rollback();
        }

        t.close();
        my_db_conn.close();

        root.getSetup().setLocalConfig(Setup.DBDatabase, DB_NAME);
        root.getSetup().setLocalConfig(Setup.DBHost, DB_HOST);
        root.getSetup().setLocalConfig(Setup.DBUser, DB_NAME);
        root.getSetup().setLocalConfig(Setup.DBPasswd, DB_NAME);
        root.getSetup().setLocalConfig(Setup.DBPort, "");
        root.getSetup().setLocalConfig(Setup.DBInstance, DB_NAME);
        root.getSetup().setLocalConfig(Setup.DBType, SupportedDBMSTypes.DB_MYSQL.toString());

         if( !root.loadDBConnectionFromSetup() )
         {
             logger.error("Fehler beim Laden der Datenbankverbindung vom Setup");
             return false;
         }

        root.saveSetup();

        return true;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public Root getRoot() {
        return root;
    }

    @Override
    public void close() {
        root.getDBConnection().close();
    }

    @Override
    public Integer getBPIdx() {
        return 1;
    }

    @Override
    public int getNewSequenceValue(String seqName) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException {
        
        if( seq_transaction == null ) {
            seq_transaction = root.getDBConnection().getNewTransaction();
        }
        
        int value = seq_transaction.getNewSequenceValue(seqName, 1234567 );
        
        seq_transaction.commit();        
        
        return value;
    }
    
    @Override
    public int getNewSequenceValues(String seqName, int count) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException {
        
        if( seq_transaction == null ) {
            seq_transaction = root.getDBConnection().getNewTransaction();
        }
        
        int value = seq_transaction.getNewSequenceValues(seqName, count, 1234567 );
        
        seq_transaction.commit();        
        
        return value;        
    }    

    @Override
    public Audit getAudit() {
       
        if ( audit == null )
        {
            audit = new Audit(this);
        }
        
        return audit;        
    }

}
