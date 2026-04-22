/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.DBDataType;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDB;
import at.redeye.twelvelittlescoutsclerk.test.db.SetupTestDBInterface;

/**
 *
 * @author martin
 */
public class UpdateBPTest {
    
    static SetupTestDBInterface setup_test_db;
    static MainWinInterface mainwin;
    static Root root;
    static Transaction trans;    
    static NewSequenceValueInterface new_seq_values;        
    
    public UpdateBPTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception 
    {
        if( setup_test_db == null )
        {
            SetupTestDB db = new SetupTestDB(); 
            mainwin = db;
            new_seq_values = db;
            setup_test_db = db;
            setup_test_db.invoke();    
            root = setup_test_db.getRoot();        
            trans = root.getDBConnection().getDefaultTransaction();           
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

    private int countRows( String table, int bp_idx ) throws SQLException, UnsupportedDBDataTypeException
    {
            ArrayList<DBDataType> count_list = new ArrayList<>();
            count_list.add(DBDataType.DB_TYPE_INTEGER);
            
            List<List<?>> res = trans.fetchColumnValue("select count(*) from " + trans.markTable(table) + " where " + trans.markColumn("bp_idx") + " = " + bp_idx, 
                    count_list );
            
            int val = (Integer)res.get(0).get(0);
            
            return val;
    }
    
    private int countRows( String table ) throws SQLException, UnsupportedDBDataTypeException
    {
            ArrayList<DBDataType> count_list = new ArrayList<>();
            count_list.add(DBDataType.DB_TYPE_INTEGER);
            
            List<List<?>> res = trans.fetchColumnValue("select count(*) from " + trans.markTable(table), 
                    count_list );
            
            int val = (Integer)res.get(0).get(0);
            
            return val;
    }
    
    /**
     * Test of copyData3NewAZ method, of class UpdateAZ.
     */
    @Test
    public void testCopyData2NewAZ() throws Exception {
        System.out.println("copyData2NewAZ");
/* 
        try {
            DBAZ az = new DBAZ();
            az.idx.loadFromCopy(1);
            
            if( !trans.fetchTableWithPrimkey(az) ) {
                fail( "Cannot fetch AZ with idx 1");
            }
            
            DBAZ az_new = new DBAZ();
            az_new.title.loadFromCopy(new Date().toString());
            az_new.idx.loadFromCopy(mainwin.getNewSequenceValue(az_new.getName()));
            az_new.comment.loadFromCopy("Test");
            
            trans.insertValues(az_new);
            
            DBKunden kunden = new DBKunden();
            DBPrime prime = new DBPrime();
            DBBonus bonus = new DBBonus();           
            DBAnzahlungen anzahlungen = new DBAnzahlungen();    
            DBAudit audit = new DBAudit();    
            
            int before_count_kunden = countRows(kunden.getName(), az.idx.getValue());
            int before_count_prime = countRows(prime.getName(), az.idx.getValue());
            int before_count_bonus = countRows(bonus.getName(), az.idx.getValue());
            int before_count_anzahlungen = countRows(anzahlungen.getName(), az.idx.getValue());
            int before_count_audit = countRows(audit.getName(), az.idx.getValue());
            
            UpdateAZ update_az = new UpdateAZ(trans, new_seq_values);
            update_az.copyData2NewAZ(az_new.idx.getValue(), az);

            int after_count_kunden = countRows(kunden.getName(), az.idx.getValue());
            int after_count_prime = countRows(prime.getName(), az.idx.getValue());
            int after_count_bonus = countRows(bonus.getName(), az.idx.getValue());            
            int after_count_anzahlungen = countRows(anzahlungen.getName(), az.idx.getValue()); 
            int after_count_audit = countRows(audit.getName(), az.idx.getValue());            
            
            assertEquals(before_count_kunden, after_count_kunden);
            assertEquals(before_count_bonus, after_count_bonus);
            assertEquals(before_count_prime, after_count_prime);
            assertEquals(before_count_anzahlungen, after_count_anzahlungen);
            assertEquals(before_count_audit, after_count_audit);
            
            
            int after_count_kunden_new = countRows(kunden.getName(), az_new.idx.getValue());
            int after_count_prime_new = countRows(prime.getName(), az_new.idx.getValue());
            int after_count_bonus_new = countRows(bonus.getName(), az_new.idx.getValue());            
            int after_count_anzahlungen_new = countRows(anzahlungen.getName(), az_new.idx.getValue()); 
            int after_count_audit_new = countRows(audit.getName(), az_new.idx.getValue());            
            
            assertEquals(before_count_kunden, after_count_kunden_new);
            assertEquals(before_count_bonus, after_count_bonus_new);
            assertEquals(before_count_prime, after_count_prime_new);
            assertEquals(before_count_anzahlungen, after_count_anzahlungen_new);
            assertEquals(before_count_audit, after_count_audit_new);            
            
            List<DBKunden> kunden_list = trans.fetchTable2(kunden, "where " + trans.markColumn(kunden.az_idx) + " = " + az_new.idx.toString() );
            HashMap<Integer,DBKunden> kunden_map = new HashMap();
            
            for( DBKunden k : kunden_list )
                kunden_map.put(k.idx.getValue(), k);
            
            List<DBBonus> bonus_list = trans.fetchTable2(bonus, "where " + trans.markColumn(kunden.az_idx) + " = " + az_new.idx.toString() );
            
            for( DBBonus b : bonus_list )
            {                
                assertNotNull(kunden_map.get(b.idx.getValue()));
                
                if( b.gast1_idx.getValue() > 0 )
                    assertNotNull(kunden_map.get(b.gast1_idx.getValue()));
                
                if( b.gast2_idx.getValue() > 0 )
                    assertNotNull(kunden_map.get(b.gast2_idx.getValue()));
                
                if( b.mentor_idx.getValue() > 0 )
                    assertNotNull(kunden_map.get(b.mentor_idx.getValue()));
                
                if( b.seminar_berater_idx.getValue() > 0 )
                    assertNotNull(kunden_map.get(b.seminar_berater_idx.getValue()));     
                
                if( b.gebracht_von_idx.getValue() > 0 )
                    assertNotNull(kunden_map.get(b.gebracht_von_idx.getValue()));                    
            }
            
            
            List<DBPrime> prime_list = trans.fetchTable2(prime, "where " + trans.markColumn(prime.az_idx) + " = " + az_new.idx.toString() );
            
            for( DBPrime p : prime_list )
            { 
                assertNotNull(kunden_map.get(p.bonus_idx.getValue()));
                
                assertNotNull(kunden_map.get(p.gast_idx.getValue()));
                
                if( p.premie_von_idx.getValue() > 0 )
                    assertNotNull(kunden_map.get(p.premie_von_idx.getValue()));                                
            }
            
            trans.commit();
            
        } catch( UnsupportedDBDataTypeException | WrongBindFileFormatException | SQLException | TableBindingNotRegisteredException | IOException ex ) {
            ex.printStackTrace();
            fail( "exception caught: " + ex);
        }              
            */  

        fail( "test not implemented yet");
    }

    /**
     * Test of deleteAZ method, of class UpdateAZ.
     */
    @Test
    public void testDeleteAZ() throws Exception 
    {
        /*
        try {
             DBAZ az = new DBAZ();
            az.idx.loadFromCopy(1);
            
            if( !trans.fetchTableWithPrimkey(az) ) {
                fail( "Cannot fetch AZ with idx 1");
            }
            
            DBAZ az_new = new DBAZ();
            az_new.title.loadFromCopy(new Date().toString());
            az_new.idx.loadFromCopy(mainwin.getNewSequenceValue(az_new.getName()));
            az_new.comment.loadFromCopy("Test");
            
            trans.insertValues(az_new);
            
            DBKunden kunden = new DBKunden();
            DBPrime prime = new DBPrime();
            DBBonus bonus = new DBBonus();           
            DBAnzahlungen anzahlungen = new DBAnzahlungen();    
            DBAudit audit = new DBAudit();    
            
            int before_count_kunden = countRows(kunden.getName(), az.idx.getValue());
            int before_count_prime = countRows(prime.getName(), az.idx.getValue());
            int before_count_bonus = countRows(bonus.getName(), az.idx.getValue());
            int before_count_anzahlungen = countRows(anzahlungen.getName(), az.idx.getValue());
            int before_count_audit = countRows(audit.getName(), az.idx.getValue());            
            
            int before_count_kunden_all = countRows(kunden.getName());
            int before_count_prime_all = countRows(prime.getName());
            int before_count_bonus_all = countRows(bonus.getName());
            int before_count_anzahlungen_all = countRows(anzahlungen.getName());
            int before_count_audit_all = countRows(audit.getName());            
            
            UpdateAZ update_az = new UpdateAZ(trans, new_seq_values);
            update_az.copyData2NewAZ(az_new.idx.getValue(), az);
        
            
            int after_count_kunden_new = countRows(kunden.getName(), az_new.idx.getValue());
            int after_count_prime_new = countRows(prime.getName(), az_new.idx.getValue());
            int after_count_bonus_new = countRows(bonus.getName(), az_new.idx.getValue());            
            int after_count_anzahlungen_new = countRows(anzahlungen.getName(), az_new.idx.getValue()); 
            int after_count_audit_new = countRows(audit.getName(), az_new.idx.getValue());            
            
            assertEquals(before_count_kunden, after_count_kunden_new);
            assertEquals(before_count_bonus, after_count_bonus_new);
            assertEquals(before_count_prime, after_count_prime_new);
            assertEquals(before_count_anzahlungen, after_count_anzahlungen_new);
            assertEquals(before_count_audit, after_count_audit_new);                  

            update_az.deleteAZ(az_new.idx.getValue());
            
            int after_count_kunden_all = countRows(kunden.getName());
            int after_count_prime_all = countRows(prime.getName());
            int after_count_bonus_all = countRows(bonus.getName());
            int after_count_anzahlungen_all = countRows(anzahlungen.getName());
            int after_count_audit_all = countRows(audit.getName());                     
            
            assertEquals(before_count_kunden_all, after_count_kunden_all);
            assertEquals(before_count_bonus_all, after_count_bonus_all);
            assertEquals(before_count_prime_all, after_count_prime_all);
            assertEquals(before_count_anzahlungen_all, after_count_anzahlungen_all);
            assertEquals(before_count_audit_all, after_count_audit_all);                  
            
            trans.commit();
            
        } catch( UnsupportedDBDataTypeException | WrongBindFileFormatException | SQLException | TableBindingNotRegisteredException | IOException ex ) {
            ex.printStackTrace();
            fail( "exception caught: " + ex);
        }    
*/
        fail( "test not implemented yet");
    }
}
