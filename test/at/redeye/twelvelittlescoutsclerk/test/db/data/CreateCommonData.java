/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.redeye.twelvelittlescoutsclerk.test.db.data;

import java.awt.Event;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.swing.GroupLayout.Group;

import at.redeye.FrameWork.base.AutoLogger;
import at.redeye.FrameWork.base.DefaultInsertOrUpdater;
import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.UserManagement.bindtypes.DBPb;
import at.redeye.UserManagement.impl.UserDataHandling;
import at.redeye.twelvelittlescoutsclerk.EventHelper;
import at.redeye.twelvelittlescoutsclerk.GroupHelper;
import at.redeye.twelvelittlescoutsclerk.MainWinInterface;
import at.redeye.twelvelittlescoutsclerk.MemberHelper;
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
import at.redeye.twelvelittlescoutsclerk.imports.scoreg.ImportMemberFromScoregExcel;
import at.redeye.twelvelittlescoutsclerk.imports.elba.ImportBookingLineFromElba;
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDB;

/**
 *
 * @author martin
 */
public class CreateCommonData {

    Root root;    
    MainWinInterface mainwin;

    public CreateCommonData(Root root, MainWinInterface mainwin) {
        this.root = root;
        this.mainwin = mainwin;
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

                final CreateCommonData data_inserter = new CreateCommonData(setup.root, setup);
                data_inserter.cleanup();
                data_inserter.create_az();
                data_inserter.init_groups();
                data_inserter.import_scoreg_data();
                data_inserter.create_events();
                data_inserter.import_booking_lines();
            }
        };

    }
    
    public void cleanup() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException 
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
        
        int bp_idx = 1;
        DBBillingPeriod bp = new DBBillingPeriod();
        
        bp.idx.loadFromCopy(1);
        bp.title.loadFromCopy("initial Import");
        bp.hist.setAnHist(root.getUserName());
                
        trans.insertValues(bp);                
    }

    public void import_scoreg_data() throws Exception
    {
        File file = new File( "testdata/scoreg_anonymized.xlsx" );

        if( !file.exists() || !file.canRead() ) {
            throw new RuntimeException("Test file not found: " + file.getAbsolutePath());
        }

        ImportMemberFromScoregExcel instance = new ImportMemberFromScoregExcel(mainwin, file);

        if( !instance.run(mainwin.getBPIdx()) ) {
            throw new RuntimeException("Import failed");
        }
        
        instance.commit();
    }

    public void import_booking_lines() throws Exception
    {
        File file = new File( "testdata/elba_booking_lines_anonymized.csv" );

        if( !file.exists() || !file.canRead() ) {
            throw new RuntimeException("Test file not found: " + file.getAbsolutePath());
        }

        ImportBookingLineFromElba instance = new ImportBookingLineFromElba( mainwin, file );
                
        if( !instance.run(mainwin.getBPIdx()) ) {
            throw new RuntimeException("Import failed");
        }
        
        instance.commit();
        instance.dispose();
    }

    public void create_events() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException 
    {
        Transaction trans = root.getDBConnection().getDefaultTransaction(); 
        DBEvent event = new DBEvent();
        event.bp_idx.loadFromCopy(mainwin.getBPIdx());
        event.name.loadFromCopy("LK Thxalot");
        event.hist.setAnHist(root.getUserName());
        event.idx.loadFromCopy(mainwin.getNewSequenceValue(DBEvent.EVENT_IDX_SEQUENCE));
        event.costs.loadFromCopy(40.0);

        DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans,event);

        List<DBMember> members = MemberHelper.fetch_members(trans, mainwin.getBPIdx());
        HashMap<String,DBMember> members_by_scout_id = new HashMap<>();

        for( DBMember m : members ) {
            members_by_scout_id.put(m.member_registration_number.getValue(), m);
        }

        {
            DBEventMember em = EventHelper.createEventMember(mainwin, trans, members_by_scout_id.get("8-AEJGP-K48289"), event);
            DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, em, em.hist, "root" );
        }

        {
            DBEventMember em = EventHelper.createEventMember(mainwin, trans, members_by_scout_id.get("6-R19-B70917"), event);
            DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, em, em.hist, "root" );
        }

        {
            DBEventMember em = EventHelper.createEventMember(mainwin, trans, members_by_scout_id.get("9-B12-Z74561"), event);
            DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, em, em.hist, "root" );
        }

        {
            DBEventMember em = EventHelper.createEventMember(mainwin, trans, members_by_scout_id.get("6-L36-I68116"), event);
            DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, em, em.hist, "root" );
        }
        
        {
            DBEventMember em = EventHelper.createEventMember(mainwin, trans, members_by_scout_id.get("7-Q47-T80709"), event);
            DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, em, em.hist, "root" );
        }
        
        {
            DBEventMember em = EventHelper.createEventMember(mainwin, trans, members_by_scout_id.get("6-A69-B70161"), event);
            DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, em, em.hist, "root" );
        }

        trans.commit();
    }

    public void init_groups() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException 
    {      
        Transaction trans = root.getDBConnection().getDefaultTransaction(); 
        GroupHelper.init_groups(mainwin, trans);
        trans.commit();
    }
    
}
