/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBAudit;
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDB;
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDBInterface;
import java.util.ArrayList;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author martin
 */
public class AuditTest {
    
    static SetupTestDBInterface setup_test_db;
    static MainWinInterface mainwin;
    static Root root;
    static Transaction trans;
    static Audit audit;    
    
    public AuditTest() {
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
            audit = new Audit(mainwin);
        }            
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of openNewAudit method, of class Audit.
     */
    @Test
    public void testOpenNewAudit() throws Exception {
        System.out.println("openNewAudit");
        
        Audit instance = audit;
        instance.openNewAudit();
    }

    /**
     * Test of addMessage method, of class Audit.
     */
    @Test
    public void testAddMessage() throws Exception {
        System.out.println("addMessage");
                               
        String message = "das ist ein Test";
        Audit instance = audit;
        instance.openNewAudit();
        DBAudit dba = instance.addMessage(trans, message);

        ArrayList<DBAudit> audits = new ArrayList<>();
        
        DBAudit a2 = new DBAudit();
        a2.idx.loadFromCopy(dba.idx.getValue());
        
        if (!trans.fetchTableWithPrimkey(a2)) {
            fail("data not found in db");
        }            
        
        audits.add(a2);
        audits.add(dba);
        
        for (DBAudit a : audits) 
        {
            if (a.audit_idx.getValue() == 0) {
                fail("audit_idx value not set");
            }

            assertEquals(mainwin.getBPIdx(), a.bp_idx.getValue());

            if (a.date.getValue().getTime() == 0) {
                fail("time value not set");
            }


            assertEquals(0, (int) a.member_idx.getValue());
            assertEquals(message, a.message.getValue());        
        }
        
        if (!trans.fetchTableWithPrimkey(dba)) {
            fail("data not found in db");
        }    
        
        trans.commit();
    }  

    /**
     * Test of addMessage4Kunde method, of class Audit.
     */
    @Test
    public void testAddMessage4Kunde_3args_1() throws Exception {
        System.out.println("addMessage4Kunde");


        String message = "das ist ein Test";
        Audit instance = audit;
        instance.openNewAudit();
        DBAudit dba = instance.addMessage4Kunde(trans, 1, message);

        ArrayList<DBAudit> audits = new ArrayList<>();
        
        DBAudit a2 = new DBAudit();
        a2.idx.loadFromCopy(dba.idx.getValue());
        
        if (!trans.fetchTableWithPrimkey(a2)) {
            fail("data not found in db");
        }            
        
        audits.add(a2);
        audits.add(dba);
        
        for (DBAudit a : audits) 
        {
            if (a.audit_idx.getValue() == 0) {
                fail("audit_idx value not set");
            }

            assertEquals(mainwin.getBPIdx(), a.bp_idx.getValue());

            if (a.date.getValue().getTime() == 0) {
                fail("time value not set");
            }


            assertEquals(1, (int) a.member_idx.getValue());
            assertEquals(message, a.message.getValue());        
        }
        
        if (!trans.fetchTableWithPrimkey(dba)) {
            fail("data not found in db");
        }    
        
        trans.commit();        
    }

    /**
     * Test of addMessage4Kunde method, of class Audit.
     */
    @Test
    public void testAddMessage4Kunde_3args_2() throws Exception {
        System.out.println("addMessage4Kunde");
        
        
        ArrayList<String> messages = new ArrayList<>();
        messages.add("das");
        messages.add("ist");
        messages.add("ein");
        messages.add("test");
        
        String message = "das\nist\nein\ntest\n";
        
        Audit instance = audit;
        instance.openNewAudit();
        DBAudit dba = instance.addMessage4Kunde(trans, 1, messages);

        ArrayList<DBAudit> audits = new ArrayList<>();
        
        DBAudit a2 = new DBAudit();
        a2.idx.loadFromCopy(dba.idx.getValue());
        
        if (!trans.fetchTableWithPrimkey(a2)) {
            fail("data not found in db");
        }            
        
        audits.add(a2);
        audits.add(dba);
        
        for (DBAudit a : audits) 
        {
            if (a.audit_idx.getValue() == 0) {
                fail("audit_idx value not set");
            }

            assertEquals(mainwin.getBPIdx(), a.bp_idx.getValue());

            if (a.date.getValue().getTime() == 0) {
                fail("time value not set");
            }


            assertEquals(1, (int) a.member_idx.getValue());
            assertEquals(message.trim(), a.message.getValue().trim());        
        }
        
        if (!trans.fetchTableWithPrimkey(dba)) {
            fail("data not found in db");
        }    
        
        trans.commit();                        
    }
}
