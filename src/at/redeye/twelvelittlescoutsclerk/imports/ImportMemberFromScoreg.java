/**
 * TwelveLittleScoutsClerk Scoreg csv file importer
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.imports;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillingPeriod;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBGroup;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Contacts;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Groups;
import au.com.bytecode.opencsv.CSVReader;
import java.awt.Dialog;
import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class ImportMemberFromScoreg
{
    private static final Logger logger = Logger.getLogger(ImportMemberFromScoreg.class);
    
    MainWin main;
    Transaction trans;
    File csv_file;
    SimpleDateFormat sdf_date;
    ArrayList<String> kundennummern ;
    ArrayList<String> doppelte_kundennummern;
    ArrayList<String> unbekannte_kunden;
    int azidx;
    
    public ImportMemberFromScoreg( MainWin main, File csv_file )
    {
        this.main = main;
        this.csv_file = csv_file;       
        sdf_date = new SimpleDateFormat("dd.MM.yy");
        kundennummern = new ArrayList();
        doppelte_kundennummern = new ArrayList();       
        unbekannte_kunden = new ArrayList();
        trans = main.getNewTransaction();
    }
    
    public void clear()
    {
        kundennummern.clear();
        doppelte_kundennummern.clear();
        unbekannte_kunden.clear();
    }

    public String getErrorMessage()
    {        
        if( doppelte_kundennummern.isEmpty() && unbekannte_kunden.isEmpty() )
            return null;
        
        StringBuilder sb = new StringBuilder();

        if (!doppelte_kundennummern.isEmpty()) 
        {
            sb.append("Folgende Kundennummern wurden doppelt vergeben:\n");

            for (String kunr : doppelte_kundennummern) {
                sb.append(kunr);
                sb.append("\n");
            }
        }
        
        
        if (!unbekannte_kunden.isEmpty()) 
        {
            if( sb.length() != 0 )
                sb.append("\n");
            
            sb.append("Folgende Kundennummern sind unbekannt:\n");

            for (String kunr : unbekannte_kunden) {
                sb.append(kunr);
                sb.append("\n");
            }
        }     
        
        return sb.toString();
    }
    
    private boolean checkKundenNummer( String kundennummer, String message_text )
    {
        if( kundennummern.contains(kundennummer) )
        {
            // doppelte_kundennummern.add(message_text);
            return true;
        } else {
            kundennummern.add(kundennummer);
        }              
        
        return false;
    }
    
    
    private HashMap<String,DBMember> fetchMemberNr() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException
    {
        HashMap<String,DBMember> member_by_membernr = new HashMap();
        
        DBMember member = new DBMember();
        List<DBMember> members_list = trans.fetchTable2(member, "where " + trans.markColumn(member.bp_idx) + " = " + azidx );
        
        for( DBMember m : members_list )        
        {
            member_by_membernr.put(m.member_registration_number.getValue(), m);
        }
        
        return member_by_membernr;
    }
    
    private HashMap<String,DBGroup> fetchGroups() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException
    {
        HashMap<String,DBGroup> group_by_abbrv = new HashMap();
        
        List<DBGroup> group_list = trans.fetchTable2( new DBGroup() );
        
        for( DBGroup g : group_list )        
        {
            String name = g.name.getValue();
            group_by_abbrv.put(name, g);
            
            switch (name) {
                case "WiWö":
                    group_by_abbrv.put("WI",g);
                    group_by_abbrv.put("WOE",g);
                    break;
                case "GuSp":
                    group_by_abbrv.put("GU",g);
                    group_by_abbrv.put("SP",g);
                    break;
                case "CaEx":
                    group_by_abbrv.put("CA",g);
                    group_by_abbrv.put("EX",g);
                    break;
                case "RaRo":
                    group_by_abbrv.put("RA",g);
                    group_by_abbrv.put("RO",g);
                    break;
                default:
                    break;
            }
        }
        
        return group_by_abbrv;
    }
    
    public boolean run( int azidx ) throws FileNotFoundException, IOException, ParseException, SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException
    {
        this.azidx = azidx;
        clear();
        
        if( trans == null ) {
            trans = main.getNewTransaction();
        }                
                        
        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csv_file),"ISO-8859-15"), ';', '\"');
        
        List list = reader.readAll();     
        
        MatchColumn match = new MatchColumn();
        
        HashMap<String, DBMember> kunden_by_kundennr = fetchMemberNr();        
        HashMap<String,DBGroup> groups_by_abbrv = fetchGroups();
        
        for( int i = 0; i < list.size(); i++ )
        {
            String cols[] = (String[]) list.get(i);
            
            logLine(cols);
            
            if( i == 0 ) {
                match.init(cols);
                continue;
            }
            
            boolean ignore_line = false;
            
            DBMember member = new DBMember();            
            
            member.member_registration_number.loadFromString(match.getOrDefault( "Scout-Id", cols ));
            member.forname.loadFromString(match.getOrDefault( "Vorname",cols));
            member.name.loadFromString(match.getOrDefault("Nachname", cols));
            
            String date = match.getOrDefault("Eintrittsdatum",cols);
            if( !date.isEmpty() ) {
                member.entry_date.loadFromCopy(readDate(date));
            }
            member.tel.loadFromString(match.getOrDefault("Handy",cols));
                       
            if( ignore_line ) {
                continue;
            }
            
            member.bp_idx.loadFromCopy(main.getAZIdx());
            member.idx.loadFromCopy(main.getNewSequenceValue(DBMember.MEMBERS_IDX_SEQUENCE));
            member.hist.setAnHist(main.getRoot().getLogin());
            
            trans.insertValues(member);
            
            for( int j = 0; j < 2; j++ ) {
                DBContact contact = new DBContact();
                
                contact.bp_idx.loadFromCopy(main.getAZIdx());                
                contact.name.loadFromCopy(match.getOrDefault("Kontakt",cols,j));
                contact.tel.loadFromCopy(match.getOrDefault("Kontakt Telefon",cols,j));
                contact.email.loadFromCopy(match.getOrDefault("Kontakt E-Mail",cols,j));
                        
                if( !contact.name.isEmptyTrimmed() ) {
                    contact.idx.loadFromCopy(main.getNewSequenceValue(DBContact.CONTACT_IDX_SEQUENCE));
                    logger.debug("email " + contact.email.toString());
                    contact.hist.setAnHist(main.getRoot().getLogin());
                    trans.insertValues(contact);
                    
                    DBMembers2Contacts m2c = new DBMembers2Contacts();
                    m2c.idx.loadFromCopy(main.getNewSequenceValue(DBMembers2Contacts.MEMBERS2CONTACTS_IDX_SEQUENCE));
                    m2c.contact_idx.loadFromCopy(contact.idx.getValue());
                    m2c.member_idx.loadFromCopy(member.idx.getValue());
                    m2c.hist.setAnHist(main.getRoot().getLogin());
                    trans.insertValues(m2c);
                }
            }
            
            DBGroup group = groups_by_abbrv.get(match.getOrDefault("Stufe",cols));
            
            if( group == null ) {
                group = groups_by_abbrv.get("Leiter");
            }
            
            if( group != null ) {
                DBMembers2Groups m2g = new DBMembers2Groups();
                m2g.idx.loadFromCopy(main.getNewSequenceValue(DBMembers2Groups.MEMBERS2GROUPS_IDX_SEQUENCE));
                m2g.hist.setAnHist(main.getRoot().getLogin());
                m2g.member_idx.loadFromCopy(member.idx.getValue());
                m2g.group_idx.loadFromCopy(group.idx.getValue());
                trans.insertValues(m2g);
            } 
        }
        
        if( getErrorMessage() != null ) {
            return false;
        }
        
        return true;
    }
    
    
    private Date readDate( String value ) throws ParseException
    {
        return sdf_date.parse(value);
    }
    
    private void logLine( String cols[] )
    {
        if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();

            for (String col : cols) {
                sb.append(col);
                sb.append(";");
            }

            logger.trace(sb);
        }
    }        
    
    public void dispose() throws SQLException
    {
        if( trans != null ) {
            trans.rollback();
            trans = null;
        }
    }
    

    private void commit() throws SQLException {
        trans.commit();
    }    
    
    /**
     * Function that starts the import of memebrs, by opening the filechooser
     * as the first step. Will do commit and rollback on fail automatically
     * @param mainwin 
     */
    public static void importMember( final MainWin mainwin )
    {
         JFileChooser fc = new JFileChooser();

        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new CSVFileFilter());
        fc.setMultiSelectionEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        String last_path = mainwin.getLastOpenPath();

        logger.info("last path: " + last_path);

        if (last_path != null) {
            fc.setCurrentDirectory(new File(last_path));
        }

        int retval = fc.showOpenDialog(mainwin);

        if (retval != 0) {
            return;
        }

        File file = fc.getSelectedFile();

        if (file.canRead()) {
            mainwin.setLastOpenPath(file.getParent());

            logger.debug("importiere " + file);
            
            final ImportMemberFromScoreg importer = new ImportMemberFromScoreg(mainwin, file );            
                    
            new AutoMBox(ImportMemberFromScoreg.class.getName()) {

                @Override
                public void do_stuff() throws Exception {
                    
                    mainwin.setWaitCursor();
                    
                    DBBillingPeriod bp = new DBBillingPeriod();
                    
                    Transaction trans = mainwin.getTransaction();
                    
                    List<DBBillingPeriod> bp_list = trans.fetchTable2(bp, "order by " + trans.markColumn("hist_anzeit") + " desc" );
                    
                    StringBuilder sb = new StringBuilder();                                       

                    boolean error_happened = false;
                    
                    for( DBBillingPeriod current_bp : bp_list )
                    {                                                                    
                        if( !importer.run(current_bp.idx.getValue()) )
                        {                         
                            sb.append(current_bp.title.getValue() + "\n");
                            sb.append(importer.getErrorMessage() + "\n\n");
                            error_happened = true;
                        }                        
                    }
                    
                    boolean do_commit = false;
                    
                    mainwin.setWaitCursor(false);
                    
                    if( error_happened )
                    {
                        InfoWin infowin = new InfoWin(mainwin.getRoot(), "Warnung", sb.toString() );
                        infowin.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                        infowin.setVisible(true);
                        infowin.toFront();
                        infowin.requestFocus();
                        
                        if( infowin.pressedYes() )
                            do_commit = true;
                    } else {
                        JOptionPane.showMessageDialog(mainwin, "Der Datenimport war erfolgreich");
                        do_commit = true;
                    }
                    
                                        
                    if( do_commit )
                    {
                        importer.commit();
                    }                                        
                }
            };
            
            try {
                importer.dispose();
            } catch (SQLException ex) {
                logger.error(ex,ex);
            }
        }        
    }
}