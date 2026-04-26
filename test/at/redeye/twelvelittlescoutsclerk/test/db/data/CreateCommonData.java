/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.redeye.twelvelittlescoutsclerk.test.db.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import at.redeye.twelvelittlescoutsclerk.BookingLineHelper;
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

    public static final List<String> LANDESSPIEL_SCOUT_IDS = List.of("4-B60-Z62847", "6-X94-N34508", "9-W38-I30526", "2-C34-K70612", "1-D78-A86412", "3-V48-J47049", "3-Z32-O67037", "8-D27-M31759");
    public static final List<String> THXALOT_SCOUT_IDS = List.of("5-X74-X7484", "1-A99-I39575", "6-G2-X5030", "8-T12-P21833", "3-A84-G5166");

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
                data_inserter.assign_booking_lines_4_landesspiel();
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
        create_thx_alot();
        create_landesspiel();
    }

    public void create_thx_alot() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException 
    {
        Transaction trans = root.getDBConnection().getDefaultTransaction(); 
        DBEvent event = new DBEvent();
        event.bp_idx.loadFromCopy(mainwin.getBPIdx());
        event.name.loadFromCopy("LK Thxalot");
        event.hist.setAnHist(root.getUserName());
        event.idx.loadFromCopy(mainwin.getNewSequenceValue(DBEvent.EVENT_IDX_SEQUENCE));
        event.costs.loadFromCopy(40.0);

        DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans,event);

        HashMap<String,DBMember> members_by_scout_id = MemberHelper.get_members_by_scout_id_map(trans, mainwin.getBPIdx());
        
        for( String scout_id : THXALOT_SCOUT_IDS )
        {
            DBEventMember em = EventHelper.createEventMember(mainwin, trans, members_by_scout_id.get(scout_id), event);
            DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, em, em.hist, "root" );
        }

        trans.commit();
    }

    public void create_landesspiel() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException 
    {
        Transaction trans = root.getDBConnection().getDefaultTransaction(); 
        DBEvent event = new DBEvent();
        event.bp_idx.loadFromCopy(mainwin.getBPIdx());
        event.name.loadFromCopy("Landesspiel");
        event.hist.setAnHist(root.getUserName());
        event.idx.loadFromCopy(mainwin.getNewSequenceValue(DBEvent.EVENT_IDX_SEQUENCE));
        event.costs.loadFromCopy(25.0);

        DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans,event);

        HashMap<String,DBMember> members_by_scout_id = MemberHelper.get_members_by_scout_id_map(trans, mainwin.getBPIdx());

        List<String> scout_ids = CreateCommonData.LANDESSPIEL_SCOUT_IDS;

        for( String scout_id : scout_ids )
        {
            DBEventMember em = EventHelper.createEventMember(mainwin, trans, members_by_scout_id.get(scout_id), event);
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
    
    public void assign_booking_lines_4_landesspiel() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException 
    {
        Transaction trans = root.getDBConnection().getDefaultTransaction(); 

        List<DBBookingLine2Events> bles_to_remove = new java.util.ArrayList<>();

        // fetch Booking Line
        DBBookingLine line = new DBBookingLine();

        List<DBBookingLine> values = trans.fetchTable2( line, 
            " where " + trans.markColumn(line.bp_idx) + " = " + mainwin.getBPIdx() + 
            " and " + trans.markColumn(line.line) + " like '%LaSp%' " +
            " order by " + trans.markColumn(line.idx) );

        if( values.size() != 8) {
            throw new RuntimeException( "Expected 8 booking lines, but found " + values.size() );
        }

        HashSet<Integer> edited_rows = new HashSet<Integer>() {
            {
                for( int i = 0; i < values.size(); i++ ) {
                    add(i);
                }
            }
        };

        // Fetch event
        DBEvent event = new DBEvent();

        List<DBEvent> events = trans.fetchTable2( event, 
            " where " + trans.markColumn(event.bp_idx) + " = " + mainwin.getBPIdx() + 
            " and " + trans.markColumn(event.name) + " = 'Landesspiel' " +
            " order by " + trans.markColumn(event.idx) );

        if( events.size() != 1) {
            throw new RuntimeException( "Expected 1 event, but found " + events.size() );
        }

        event = events.get(0);

        // Fetch members            
        HashMap<String,DBMember> members_by_scout_id = MemberHelper.get_members_by_scout_id_map(trans, mainwin.getBPIdx());
        HashMap<Integer, DBBookingLine2Events> bl2es = BookingLineHelper.fetch_bookingline2events(trans, mainwin);

        int count = 0;

        for( String scout_id : LANDESSPIEL_SCOUT_IDS ) {        
            DBMember member_descr = members_by_scout_id.get(scout_id);

            DBBookingLine current_value = values.get(count);
            count++;

            DBBookingLine2Events bl2e = new DBBookingLine2Events();
            bl2e.bl_idx.loadFromCopy(current_value.idx.getValue());
            bl2e.bp_idx.loadFromCopy(current_value.bp_idx.getValue());
            bl2e.event_idx.loadFromCopy(event.idx.getValue());
            bl2e.member_idx.loadFromCopy(member_descr.idx.getValue());
            bl2e.member_name.loadFromString(member_descr.forname.toString() + " " + member_descr.name.toString() );
            bl2e.event_name.loadFromString(event.name.toString());
                    
            bl2es.put(current_value.idx.getValue(), bl2e);
        }

        BookingLineHelper.save(trans, mainwin, bles_to_remove, values, edited_rows, bl2es);        

        trans.commit();
    }
}
