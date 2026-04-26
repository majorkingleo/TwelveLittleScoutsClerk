/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine2Events;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEventMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDB;
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDBInterface;
import at.redeye.twelvelittlescoutsclerk.test.db.data.CreateCommonData;
import java.io.IOException;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author martin
 */
public class BookingLineHelperTest {
    
    static SetupTestDBInterface setup_test_db;
    static MainWinInterface mainwin;
    static Root root;
    static Transaction trans;

    public BookingLineHelperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception 
    {
        if( setup_test_db == null )
        {
            SetupTestDB db = new SetupTestDB(); 
            mainwin = db;
            setup_test_db = db;
            setup_test_db.invoke();    
            root = setup_test_db.getRoot();        
            trans = root.getDBConnection().getDefaultTransaction();

            CreateCommonData ccd = new CreateCommonData( root, mainwin );
            ccd.cleanup();
            ccd.init_groups();
            ccd.import_scoreg_data();
            ccd.import_booking_lines();
            ccd.create_events();
            ccd.assign_booking_lines_4_landesspiel();
        }      
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
         
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of save method, of class BookingLineHelper.
     */
    @Test
    public void testSave() throws Exception {
        System.out.println("save");
        testSaveAssignEvent();
        testRemoveAssignEvent();
    }

    void testSaveAssignEvent() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        List<DBBookingLine2Events> bles_to_remove = new java.util.ArrayList<>();

        // fetch Booking Line
        DBBookingLine line = new DBBookingLine();

        List<DBBookingLine> values = trans.fetchTable2( line, 
            " where " + trans.markColumn(line.bp_idx) + " = " + mainwin.getBPIdx() + 
            " and " + trans.markColumn(line.line) + " like '%LK Thxalot%' " +
            " order by " + trans.markColumn(line.idx) );

        if( values.size() != 6) {
            fail( "Expected 6 booking lines, but found " + values.size() );
        }

        HashSet<Integer> edited_rows = new HashSet<Integer>() {
            {
                add(0);
            }
        };

        // Fetch event
        DBEvent event = new DBEvent();

        List<DBEvent> events = trans.fetchTable2( event, 
            " where " + trans.markColumn(event.bp_idx) + " = " + mainwin.getBPIdx() + 
            " and " + trans.markColumn(event.name) + " like '%LK Thxalot%' " +
            " order by " + trans.markColumn(event.idx) );

        if( events.size() != 1) {
            fail( "Expected 1 event, but found " + events.size() );
        }

        event = events.get(0);

        // Fetch members            
        HashMap<String,DBMember> members_by_scout_id = MemberHelper.get_members_by_scout_id_map(trans, mainwin.getBPIdx());
        DBMember member_descr = members_by_scout_id.get("8-AEJGP-K48289");

        DBBookingLine current_value = values.get(0);

        HashMap<Integer, DBBookingLine2Events> bl2es = BookingLineHelper.fetch_bookingline2events(trans, mainwin);

        DBBookingLine2Events bl2e = new DBBookingLine2Events();
        bl2e.bl_idx.loadFromCopy(current_value.idx.getValue());
        bl2e.bp_idx.loadFromCopy(current_value.bp_idx.getValue());
        bl2e.event_idx.loadFromCopy(event.idx.getValue());
        bl2e.member_idx.loadFromCopy(member_descr.idx.getValue());
        bl2e.member_name.loadFromString(member_descr.forname.toString() + " " + member_descr.name.toString() );
        bl2e.event_name.loadFromString(event.name.toString());
                
        bl2es.put(current_value.idx.getValue(), bl2e);
        

        BookingLineHelper.save(trans, mainwin, bles_to_remove, values, edited_rows, bl2es);

        {
            HashMap<Integer, DBBookingLine2Events> bl2es_new = BookingLineHelper.fetch_bookingline2events(trans, mainwin);
            DBBookingLine2Events bl2e_new = bl2es_new.get(current_value.idx.getValue());

            assertNotNull(bl2e_new);
            assertEquals(bl2e.bl_idx.getValue(), bl2e_new.bl_idx.getValue());
            assertEquals(bl2e.bp_idx.getValue(), bl2e_new.bp_idx.getValue());
            assertEquals(bl2e.event_idx.getValue(), bl2e_new.event_idx.getValue());
            assertEquals(bl2e.member_idx.getValue(), bl2e_new.member_idx.getValue());
            assertEquals(bl2e.member_name.getValue(), bl2e_new.member_name.getValue());
            assertEquals(bl2e.event_name.getValue(), bl2e_new.event_name.getValue());
        }

        trans.commit();
    }

    void testRemoveAssignEvent() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        List<DBBookingLine2Events> bles_to_remove = new java.util.ArrayList<>();

        // fetch Booking Line
        DBBookingLine line = new DBBookingLine();

        List<DBBookingLine> values = trans.fetchTable2( line, 
            " where " + trans.markColumn(line.bp_idx) + " = " + mainwin.getBPIdx() + 
            " and " + trans.markColumn(line.line) + " like '%LK Thxalot%' " +
            " order by " + trans.markColumn(line.idx) );

        if( values.size() != 6) {
            fail( "Expected 6 booking lines, but found " + values.size() );
        }

        HashSet<Integer> edited_rows = new HashSet<Integer>() {
            {
                add(0);
            }
        };

        // Fetch event
        DBEvent event = new DBEvent();

        List<DBEvent> events = trans.fetchTable2( event, 
            " where " + trans.markColumn(event.bp_idx) + " = " + mainwin.getBPIdx() + 
            " and " + trans.markColumn(event.name) + " like '%LK Thxalot%' " +
            " order by " + trans.markColumn(event.idx) );

        if( events.size() != 1) {
            fail( "Expected 1 event, but found " + events.size() );
        }

        event = events.get(0);

        DBBookingLine current_value = values.get(0);

        HashMap<Integer, DBBookingLine2Events> bl2es = BookingLineHelper.fetch_bookingline2events(trans, mainwin);
        DBBookingLine2Events ble = bl2es.get(current_value.idx.getValue());

        DBEventMember event_member = new DBEventMember();

        List<DBEventMember> event_members = trans.fetchTable2( event_member, " where " + trans.markColumn(ble.bp_idx) + " = " + ble.bp_idx.getValue() +
            " and " + trans.markColumn(ble.event_idx) + " = " + ble.event_idx.getValue() +
            " and " + trans.markColumn(ble.member_idx) + " = " + ble.member_idx.getValue() );

        if( event_members.size() != 1) {
            fail( "Expected 1 event member, but found " + event_members.size() );
        }

        event_member = event_members.get(0);

        bles_to_remove.add(ble);
        bl2es.remove(current_value.idx.getValue());
    

        BookingLineHelper.save(trans, mainwin, bles_to_remove, values, edited_rows, bl2es);
        
        {
            HashMap<Integer, DBBookingLine2Events> bl2es_new = BookingLineHelper.fetch_bookingline2events(trans, mainwin);
            assertFalse(bl2es_new.containsKey(current_value.idx.getValue()));
        }

        {   
            DBEventMember current_event_member = new DBEventMember();
            current_event_member.idx.loadFromCopy(event_member.idx.getValue());

            if( !trans.fetchTableWithPrimkey(current_value) ) {
                fail("Booking line not found after save");
            }

            if( current_value.amount.getValue() != 0 ) {
                fail("Amount should not be 0 after save but is " + current_value.amount.getValue());
            }
        }

        trans.commit();
    }

    /**
     * Test of fetch_bookingline2events method, of class BookingLineHelper.
     */
    @Test
    public void testFetch_bookingline2events() throws Exception {
        System.out.println("fetch_bookingline2events");
        
        HashMap<Integer, DBBookingLine2Events> result = BookingLineHelper.fetch_bookingline2events(trans, mainwin);

        HashMap<Integer, DBBookingLine2Events> results4landesspiel = new HashMap<>();

        for( var e : result.entrySet() ) {
            if( e.getValue().event_name.getValue().equals("Landesspiel") ) {
                results4landesspiel.put(e.getKey(), e.getValue());
            }
        }        

        assertEquals(results4landesspiel.size(), CreateCommonData.LANDESSPIEL_SCOUT_IDS.size());
    }
    
}
