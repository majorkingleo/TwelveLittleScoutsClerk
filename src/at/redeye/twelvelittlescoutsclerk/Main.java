/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.*;
import at.redeye.FrameWork.base.dbmanager.DBBindtypeManager;
import at.redeye.FrameWork.base.prm.bindtypes.DBConfig;
import at.redeye.FrameWork.base.sequence.bindtypes.DBSequences;
import at.redeye.FrameWork.base.transaction.DerbyTransaction;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.FrameWork.widgets.StartupWindow;
import at.redeye.SqlDBInterface.SqlDBConnection.DbConnectionInterface;
import at.redeye.SqlDBInterface.SqlDBConnection.impl.*;
import at.redeye.UserManagement.bindtypes.DBPb;
import at.redeye.twelvelittlescoutsclerk.bindtypes.*;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.log4j.Level;

/**
 *
 * @author martin
 */
public class Main extends BaseModuleLauncher 
{
    public Main( String args[] )
    {
        super( args );
        
        BaseConfigureLogging(Level.DEBUG);
        
        root = new LocalRoot( getAppName(), getAppTitle(), true, false);

        root.setBaseLanguage("de");
        root.setDefaultLanguage("en");
          
        BaseAppConfigDefinitions.LoggingLevel.setConfigValue("DEBUG");
        BaseAppConfigDefinitions.DoLogging.setConfigValue("TRUE");
        
        root.setLanguageTranslationResourcePath("/at/redeye/twelvelittlescoutsclerk/resources/translations");
    }
    
    final String getAppName()
    {
        return getStartupParam("appname", "appname", "appname", "TwelveLittleScoutsClerk");
    }

    final String getAppTitle()
    {
        return getStartupParam("apptitle", "apptitle", "apptitle", "Test" );
    }
    
    public void run()
    {
         if (splashEnabled()) {
            splash = new StartupWindow(
                    "/at/redeye/twelvelittlescoutsclerk/resources/icons/logo.png");
        }                 
         
        AppConfigDefinitions.registerDefinitions();
	FrameWorkConfigDefinitions.registerDefinitions();
        
        root.registerPlugin(new at.redeye.Plugins.JDatePicker.Plugin() );
        
        // this sets the default value only
        FrameWorkConfigDefinitions.LookAndFeel.value.loadFromString("nimbus");
        
        // AutoMBox.addShowAdvancedExceptionHandle(new ExceptionHandler());

        try {
            boolean dbconnection_loaded = false;

            try {
                dbconnection_loaded = root.loadDBConnectionFromSetup();
            } catch (NoClassDefFoundError ex) {
                System.out.println(ex);
            }
            
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

            if( !dbconnection_loaded )
                autocreateInternalDB();
                       
             runGui();
             
        } catch (Exception ex) {
         logger.error(ex);
         ex.printStackTrace();
        }         
               
    }    

   void runGui()
    {
        setLookAndFeel(root);
         
        configureLogging();
               
        silentCheckTableVersions();        
        
        final MainWin mainwin = new MainWin(this,root);     
        
        new AutoMBox(this.getClass().getCanonicalName()) {
            @Override
            public void do_stuff() throws Exception {
                SetupDatabase setup = new ScoutSetup(mainwin);

                if (setup.isEmpty()) {
                    setup.init();
                }

            }
        }; 
        
        closeSplash();
        
        mainwin.setVisible(true);      
        mainwin.toFront();
    }
    
    public void silentCheckTableVersions() {
        new AutoLogger(BaseModuleLauncher.class.getCanonicalName()) {

            @Override
            public void do_stuff() throws Exception {

                Transaction trans = root.getDBConnection().getDefaultTransaction();

                if (trans.isOpen()) {
                    root.getBindtypeManager().setTransaction(trans);
                    if( !root.getBindtypeManager().check_table_versions() )
                    {
                        if (root.getBindtypeManager().autocreate()) {
                            logger.info("Datenbank erfolgreich eingerichtet");
                            trans.commit();
                        } else {
                            logger.error("Fehler beim Einrichten der Datenbank!");
                            trans.rollback();
                        }                  
                    }
                }
            }
        };
    }    
    
    private boolean autocreateInternalDB() throws ClassNotFoundException, UnSupportedDatabaseException, SQLException, MissingConnectionParamException
    {
        String db_name = Setup.getHiddenUserHomeFileName("TwelveLittleScoutsClerk.db");

        File f = new File( db_name );
        
        // Migration ins .Disnstplan Verzeichnis        
        if( !f.isDirectory() )
            db_name = Setup.getAppConfigFile(root.getAppName(),"TwelveLittleScoutsClerk.db");
        
        ConnectionDefinition connparams = new ConnectionDefinition(
               "",
               0,
               "",
               "",
               db_name,
               SupportedDBMSTypes.DB_JAVADB
               );

        DbConnectionInterface connint = new DBConnector(connparams);

        Connection my_db_conn = connint.connectToDatabase();

        if( my_db_conn.isClosed() )
        {
            logger.error("Erzeugen der Datenbank " + db_name + " nicht mögglich");
            return false;
        }

        Transaction t = new DerbyTransaction(connparams);

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

        root.getSetup().setLocalConfig(Setup.DBDatabase, db_name);
        root.getSetup().setLocalConfig(Setup.DBHost, "");
        root.getSetup().setLocalConfig(Setup.DBUser, "");
        root.getSetup().setLocalConfig(Setup.DBPasswd, "");
        root.getSetup().setLocalConfig(Setup.DBPort, "");
        root.getSetup().setLocalConfig(Setup.DBInstance, "");
        root.getSetup().setLocalConfig(Setup.DBType, SupportedDBMSTypes.DB_JAVADB.toString());

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
        return Version.getVersion();
    }
    
    
    void reopen() {
        root.closeAllWindowsNoAppExit();
        run();
    }
    
    public static void main( String args[] )
    {
        Main main = new Main( args );
        main.run();
    }        
    
}
