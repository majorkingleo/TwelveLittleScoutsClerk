/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package at.redeye.twelvelittlescoutsclerk.imports.scoreg;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.DBDataType;
import at.redeye.twelvelittlescoutsclerk.MainWinInterface;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Contacts;
import at.redeye.twelvelittlescoutsclerk.imports.MatchColumn;
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDB;
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDBInterface;
import at.redeye.twelvelittlescoutsclerk.test.db.data.CreateCommonData;

/**
 *
 * @author martin
 */
public class ImportMemberFromScoregExcelTest {
    
    static SetupTestDBInterface setup_test_db;
    static MainWinInterface mainwin;
    static Root root;
    static Transaction trans;
    static DBMember member;

    public ImportMemberFromScoregExcelTest() 
    {
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
            member = new DBMember();
            
            CreateCommonData ccd = new CreateCommonData( root );
            ccd.cleanup();
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
     * Test of readData method, of class ImportMemberFromScoregExcel.
     */
    @Test
    public void testReadData() throws Exception 
    {
        System.out.println("readData");
        File file = new File( "testdata/scoreg_anonymized.xlsx" );

        if( !file.exists() || !file.canRead() ) {
            fail("Test file not found: " + file.getAbsolutePath());
        }

        int bp_idx = mainwin.getBPIdx();

        ImportMemberFromScoregExcel instance = new ImportMemberFromScoregExcel(mainwin, file);          

        List<String[]> result = instance.readData();

        if( !instance.run(bp_idx) ) {
            fail("Import failed");
        }
        
        instance.commit();

        List<DBMember> members = trans.fetchTable2(member, "where " + trans.markColumn(member.bp_idx) + " = " + bp_idx);

        HashMap<String, DBMember> members_by_scout_id = new HashMap<>();
        for( DBMember m : members ) {
            members_by_scout_id.put(m.member_registration_number.toString(), m);
        }        

        assertEquals(members.size(), result.size() - 1); // -1 for header row

        MatchColumn match = new MatchColumn();

        for ( int i = 0; i < result.size(); i++ ) {

            String[] cols = result.get(i);
            
            if( i == 0 ) {
                match.init( cols );
                continue;
            }

            DBMember m = members_by_scout_id.get(match.getOrDefault("Scout-Id",cols));

            if( m == null ) {
                fail("No member found with Scout-Id: " + match.getOrDefault("Scout-Id",cols));
            }
        
            assertEquals(m.forname.toString(), match.getOrDefault("Vorname", cols));
            assertEquals(m.name.toString(), match.getOrDefault("Nachname", cols));
        }

        {
            DBContact contact = new DBContact();
            List<Integer> data = (List<Integer>)trans.fetchOneColumnValue("select count(*) from " + trans.markTable(contact) 
                    + " where " + trans.markColumn(contact.bp_idx) + " = " + bp_idx, 
                    DBDataType.DB_TYPE_INTEGER);

            assertEquals(128, (int) data.get(0));
        }

        {
            DBMembers2Contacts m2c = new DBMembers2Contacts();
            List<Integer> data = (List<Integer>)trans.fetchOneColumnValue("select count(*) from " + trans.markTable(m2c) 
                    + " where " + trans.markColumn(m2c.bp_idx) + " = " + bp_idx, 
                    DBDataType.DB_TYPE_INTEGER);

            assertEquals(149, (int) data.get(0));
        }

    }

}
