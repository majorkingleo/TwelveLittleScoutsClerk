/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package at.redeye.twelvelittlescoutsclerk.imports.elba;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.DBDataType;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.MainWinInterface;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDB;
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDBInterface;
import at.redeye.twelvelittlescoutsclerk.test.db.data.CreateCommonData;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author martin
 */
public class ImportBookingLineFromElbaTest {

    static SetupTestDBInterface setup_test_db;
    static MainWinInterface mainwin;
    static Root root;
    static Transaction trans;
    static String TEST_FILE = "testdata/elba_booking_lines_anonymized.csv";
    
    public ImportBookingLineFromElbaTest() {
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
     * Test of clear method, of class ImportBookingLineFromElba.
     */
    @Test
    public void testClear() {
        System.out.println("clear");
        ImportBookingLineFromElba instance = new ImportBookingLineFromElba(mainwin, new File(TEST_FILE));
        instance.clear();
    }

    /**
     * Test of getErrorMessage method, of class ImportBookingLineFromElba.
     */
    @Test
    public void testGetErrorMessage() {
        System.out.println("getErrorMessage");
        ImportBookingLineFromElba instance = new ImportBookingLineFromElba(mainwin, new File(TEST_FILE));
        String expResult = null;
        String result = instance.getErrorMessage();
        assertEquals(expResult, result);
    }

    /**
     * Test of cutBOM method, of class ImportBookingLineFromElba.
     */
    @Test
    public void testCutBOM() {
        System.out.println("cutBOM");

        HashMap<String, String> test_cases = new HashMap<>();

        // UTF-8 BOM: EF BB BF as individual ISO-8859-1 bytes mapped to chars
        test_cases.put("\u00EF\u00BB\u00BFUTF-8", "UTF-8");

        // no BOM
        test_cases.put("no bom", "no bom");

        // UTF-16BE BOM: \uFEFF
        test_cases.put("\uFEFFUTF-16BE", "UTF-16BE");

        // UTF-16LE BOM: \uFFFE
        test_cases.put("\uFFFEUTF-16LE", "UTF-16LE");

        for (Map.Entry<String, String> entry : test_cases.entrySet()) {
            System.out.println( "Testing: " + entry.getValue());
            
            String value = entry.getKey();
            String expResult = entry.getValue();
            String result = ImportBookingLineFromElba.cutBOM(value);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of run method, of class ImportBookingLineFromElba.
     */
    @Test
    public void testRun() throws Exception, SQLException {
        System.out.println("run");
        int bpidx = mainwin.getBPIdx();
        File csv_file = new File(TEST_FILE);

        if( !csv_file.exists() ) {
            fail("Test CSV file not found: " + csv_file.getAbsolutePath());
        }

        ImportBookingLineFromElba instance = new ImportBookingLineFromElba(mainwin, csv_file);
        boolean expResult = true;
        boolean result = instance.run(bpidx);
        assertEquals(expResult, result);

        instance.commit();

        {
            DBBookingLine bl = new DBBookingLine();
            List<Integer> data = (List<Integer>)trans.fetchOneColumnValue("select count(*) from " + trans.markTable(bl) 
                    + " where " + trans.markColumn(bl.bp_idx) + " = " + bpidx, 
                    DBDataType.DB_TYPE_INTEGER);

            assertEquals(240, (int) data.get(0));
        }

        instance.dispose();
    }
}
