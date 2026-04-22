/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.redeye.twelvelittlescoutsclerk.test.db.data;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import at.redeye.FrameWork.base.AutoLogger;
import at.redeye.FrameWork.base.DefaultInsertOrUpdater;
import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.UserManagement.bindtypes.DBPb;
import at.redeye.UserManagement.impl.UserDataHandling;
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
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDB;

/**
 *
 * @author martin
 */
public class CreateCommonData {

    Root root;
    Integer normal_job_type_id = null;
    Integer holiday_job_type_id = null;

    private CreateCommonData(Root root) {
        this.root = root;
    }

    private DBPb create_userdata() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        Transaction trans = root.getDBConnection().getDefaultTransaction();

        DBPb pb = new DBPb();

        List<DBPb> pbs = trans.fetchTable2(pb, "where " + trans.markColumn(pb.login) + "=" + "'martin.test'");

        if( !pbs.isEmpty() )
        {
            pb = pbs.get(0);
        }

        pb.locked.loadFromString("NEIN");
        pb.login.loadFromString("martin.test");
        pb.name.loadFromString("Martin");
        pb.surname.loadFromString("Schema 1 38.5");
        pb.plevel.loadFromCopy(3);
        pb.title.loadFromCopy("Ing");
        pb.pwd.loadFromString(UserDataHandling.getEncryptedPwd("test"));

        DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, pb);

        trans.commit();

        return pb;
    }

   
    public static void main( String[] args )
    {
        new AutoLogger(CreateCommonData.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                SetupTestDB setup = new SetupTestDB();


                setup.invoke();

                final CreateCommonData data_inserter = new CreateCommonData(setup.root);      
                data_inserter.cleanup();
                data_inserter.create_az();
            }
        };

    }
    
    private void cleanup() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException 
    {
        Transaction trans = root.getDBConnection().getDefaultTransaction();

        DBBillingPeriod         az              = new DBBillingPeriod();
        DBAudit                 audit           = new DBAudit();
        DBMember                member          = new DBMember();
        DBContact               contact         = new DBContact();
        DBMembers2Contacts      m2c             = new DBMembers2Contacts();
        DBMembers2Groups        m2g             = new DBMembers2Groups();
        DBGroup                 group           = new DBGroup();
        DBEvent                 event           = new DBEvent();
        DBEventMember           event_member    = new DBEventMember();
        DBBookingLine2Events    bl2events       = new DBBookingLine2Events();
        DBBookingLine           bl              = new DBBookingLine();
        
        trans.updateValues("delete from " + trans.markTable(az));
        trans.updateValues("delete from " + trans.markTable(audit));
        trans.updateValues("delete from " + trans.markTable(member));
        trans.updateValues("delete from " + trans.markTable(contact));
        trans.updateValues("delete from " + trans.markTable(m2c));
        trans.updateValues("delete from " + trans.markTable(m2g));
        trans.updateValues("delete from " + trans.markTable(group));
        trans.updateValues("delete from " + trans.markTable(event));
        trans.updateValues("delete from " + trans.markTable(event_member));
        trans.updateValues("delete from " + trans.markTable(bl2events));
        trans.updateValues("delete from " + trans.markTable(bl));
        
        trans.commit();
    }
    
    private void create_az() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException 
    {                
        Transaction trans = root.getDBConnection().getDefaultTransaction();
        
        int az_idx = 1;
        DBBillingPeriod az = new DBBillingPeriod();
        
        az.idx.loadFromCopy(1);
        az.title.loadFromCopy("initial Import");
        az.hist.setAnHist(root.getUserName());
                
        trans.insertValues(az);                
    }
    
}
