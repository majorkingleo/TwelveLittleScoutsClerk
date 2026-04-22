/**
 * TwelveLittleScoutsClerk Scoreg importer - abstract base class
 * @author Copyright (c) 2023-2025 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.imports.scoreg;

import java.awt.Dialog;
import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.DefaultInsertOrUpdater;
import at.redeye.FrameWork.base.bindtypes.DBValue;
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
import at.redeye.twelvelittlescoutsclerk.imports.InfoWin;
import at.redeye.twelvelittlescoutsclerk.imports.MatchColumn;

/**
 * Abstract base class for importing Scoreg member data.
 * Subclasses implement {@link #readData()} to supply rows as String arrays
 * (first row is the header row).
 *
 * @author martin
 */
public abstract class ImportMemberFromScoreBase
{
    private static final Logger logger = Logger.getLogger(ImportMemberFromScoreBase.class);

    protected MainWin main;
    protected Transaction trans;
    protected File file;
    protected final SimpleDateFormat sdf_date;
    protected ArrayList<String> scoutids;
    protected ArrayList<String> double_scoutids;
    protected ArrayList<String> unknown_members;
    protected int azidx;

    public ImportMemberFromScoreBase( MainWin main, File file )
    {
        this.main = main;
        this.file = file;
        sdf_date = new SimpleDateFormat("dd.MM.yy");
        scoutids = new ArrayList<>();
        double_scoutids = new ArrayList<>();
        unknown_members = new ArrayList<>();
        trans = main.getNewTransaction();
    }

    public void clear()
    {
        scoutids.clear();
        double_scoutids.clear();
        unknown_members.clear();
    }

    public String getErrorMessage()
    {
        if( double_scoutids.isEmpty() && unknown_members.isEmpty() )
            return null;

        StringBuilder sb = new StringBuilder();

        if( !double_scoutids.isEmpty() )
        {
            sb.append("Following ScoutIds are already in use:\n");
            for( String scoutid : double_scoutids ) {
                sb.append(scoutid);
                sb.append("\n");
            }
        }

        if( !unknown_members.isEmpty() )
        {
            if( sb.length() != 0 )
                sb.append("\n");
            sb.append("Following ScoutIds are unknown:\n");
            for( String scoutid : unknown_members ) {
                sb.append(scoutid);
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private boolean checkScoutID( String scoutid, String message_text )
    {
        if( scoutids.contains(scoutid) ) {
            return true;
        } else {
            scoutids.add(scoutid);
        }
        return false;
    }

    private HashMap<String,DBMember> fetchMemberNr() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException
    {
        HashMap<String,DBMember> member_by_membernr = new HashMap<>();

        DBMember member = new DBMember();
        List<DBMember> members_list = trans.fetchTable2(member, "where " + trans.markColumn(member.bp_idx) + " = " + azidx);

        for( DBMember m : members_list )
        {
            member_by_membernr.put(m.member_registration_number.getValue(), m);
        }

        return member_by_membernr;
    }

    private HashMap<String,DBGroup> fetchGroups() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException
    {
        HashMap<String,DBGroup> group_by_abbrv = new HashMap<>();

        List<DBGroup> group_list = trans.fetchTable2(new DBGroup());

        for( DBGroup g : group_list )
        {
            String name = g.name.getValue();
            group_by_abbrv.put(name, g);

            switch( name ) {
                case "WiWö":
                    group_by_abbrv.put("WI", g);
                    group_by_abbrv.put("WOE", g);
                    break;
                case "GuSp":
                    group_by_abbrv.put("GU", g);
                    group_by_abbrv.put("SP", g);
                    break;
                case "CaEx":
                    group_by_abbrv.put("CA", g);
                    group_by_abbrv.put("EX", g);
                    break;
                case "RaRo":
                    group_by_abbrv.put("RA", g);
                    group_by_abbrv.put("RO", g);
                    break;
                default:
                    break;
            }
        }

        return group_by_abbrv;
    }

    private void merge( DBValue val1, DBValue val2 )
    {
        if( val1.toString().isEmpty() ) {
            val1.loadFromCopy(val2.getValue());
        }
    }

    private boolean findContact( DBContact contact ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {
        List<DBContact> contacts;

        if( contact.forname.isEmptyTrimmed() ) {
            contacts = trans.fetchTable2(contact,
                    "where " + trans.markColumn(contact.name) + " = '" + contact.name.toString() + "' "
                    + " and " + trans.markColumn(contact.bp_idx) + " = " + contact.bp_idx.getValue());
        } else {
            contacts = trans.fetchTable2(contact,
                    "where " + trans.markColumn(contact.name) + " = '" + contact.name.toString() + "' "
                    + trans.markColumn(contact.forname) + " = '" + contact.forname.toString() + "' "
                    + " and " + trans.markColumn(contact.bp_idx) + " = " + contact.bp_idx.getValue());
        }

        if( contacts.isEmpty() ) {
            return false;
        }

        for( DBContact contact_in_db : contacts ) {
            contact.loadFromCopy(contact_in_db);
            return true;
        }

        return false;
    }

    /**
     * Reads the source file and returns all rows as String arrays.
     * The first array must be the header row containing column names.
     * Date values must be formatted as {@code dd.MM.yy}.
     *
     * @return list of rows; row 0 is the header
     * @throws Exception on any read error
     */
    protected abstract List<String[]> readData() throws Exception;

    public boolean run( int azidx ) throws Exception
    {
        this.azidx = azidx;
        clear();

        if( trans == null ) {
            trans = main.getNewTransaction();
        }

        List<String[]> rows = readData();
        MatchColumn match = new MatchColumn();

        HashMap<String, DBMember> kunden_by_kundennr = fetchMemberNr();
        HashMap<String,DBGroup> groups_by_abbrv = fetchGroups();

        for( int i = 0; i < rows.size(); i++ )
        {
            String[] cols = rows.get(i);

            logLine(cols);

            if( i == 0 ) {
                match.init(cols);
                continue;
            }

            DBMember member = new DBMember();

            member.member_registration_number.loadFromString(match.getOrDefault("Scout-Id", cols));

            var members = trans.fetchTable2(member, " where " + trans.markColumn(member, member.member_registration_number) + " = '" + member.member_registration_number.toString() + "'"
                    + " and " + trans.markColumn(member, member.bp_idx) + " = " + main.getBPIdx());

            if( members != null && !members.isEmpty() ) {
                member.loadFromCopy(members.get(0));
            }

            member.forname.loadFromString(match.getOrDefault("Vorname", cols));
            member.name.loadFromString(match.getOrDefault("Nachname", cols));

            String date = match.getOrDefault("Eintrittsdatum", cols);
            if( !date.isEmpty() ) {
                member.entry_date.loadFromCopy(readDate(date));
            }
            member.tel.loadFromString(match.getOrDefault("Handy", cols));

            member.bp_idx.loadFromCopy(main.getBPIdx());

            boolean insert = false;

            if( member.idx.getValue() == 0 ) {
                insert = true;
                member.idx.loadFromCopy(main.getNewSequenceValue(DBMember.MEMBERS_IDX_SEQUENCE));
            }

            member.hist.setAnHist(main.getRoot().getLogin());

            if( insert ) {
                trans.insertValues(member);
            } else {
                trans.updateValues(member);
            }

            for( int j = 0; j < 2; j++ ) {
                DBContact contact = new DBContact();

                contact.bp_idx.loadFromCopy(main.getBPIdx());
                contact.name.loadFromCopy(match.getOrDefault(String.format("Kontakt %d", j + 1), cols));
                contact.tel.loadFromCopy(match.getOrDefault(String.format("Kontakt %d Telefon", j + 1), cols));
                contact.email.loadFromCopy(match.getOrDefault(String.format("Kontakt %d E-Mail", j + 1), cols));

                if( !findContact(contact) ) {
                    contact.idx.loadFromCopy(main.getNewSequenceValue(DBContact.CONTACT_IDX_SEQUENCE));
                }

                if( !contact.name.isEmptyTrimmed() ) {
                    logger.debug("email " + contact.email.toString());
                    DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, contact, contact.hist, ImportMemberFromScoreBase.class.getSimpleName());

                    DBMembers2Contacts m2c = new DBMembers2Contacts();
                    m2c.idx.loadFromCopy(main.getNewSequenceValue(DBMembers2Contacts.MEMBERS2CONTACTS_IDX_SEQUENCE));
                    m2c.contact_idx.loadFromCopy(contact.idx.getValue());
                    m2c.member_idx.loadFromCopy(member.idx.getValue());
                    m2c.bp_idx.loadFromCopy(main.getBPIdx());
                    m2c.hist.setAnHist(main.getRoot().getLogin());
                    trans.insertValues(m2c);
                }
            }

            DBGroup group = groups_by_abbrv.get(match.getOrDefault("Stufe", cols));

            if( group == null ) {
                group = groups_by_abbrv.get("Leiter");
            }

            if( group != null ) {
                DBMembers2Groups m2g = new DBMembers2Groups();
                m2g.idx.loadFromCopy(main.getNewSequenceValue(DBMembers2Groups.MEMBERS2GROUPS_IDX_SEQUENCE));
                m2g.hist.setAnHist(main.getRoot().getLogin());
                m2g.member_idx.loadFromCopy(member.idx.getValue());
                m2g.group_idx.loadFromCopy(group.idx.getValue());
                m2g.bp_idx.loadFromCopy(main.getBPIdx());
                m2g.member_name.loadFromString(member.forname.toString() + " " + member.name.toString());
                m2g.group.loadFromString(group.name.toString());
                trans.insertValues(m2g);
            }
        }

        return getErrorMessage() == null;
    }

    protected Date readDate( String value ) throws ParseException
    {
        return sdf_date.parse(value);
    }

    private void logLine( String[] cols )
    {
        if( logger.isTraceEnabled() ) {
            StringBuilder sb = new StringBuilder();
            for( String col : cols ) {
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

    protected void commit() throws SQLException
    {
        trans.commit();
    }

    /**
     * Shared execution logic: iterates all billing periods, handles error
     * reporting, and commits or rolls back the transaction.
     *
     * @param mainwin  the application main window
     * @param importer the concrete importer instance to use
     */
    static void executeImport( final MainWin mainwin, final ImportMemberFromScoreBase importer )
    {
        new AutoMBox(ImportMemberFromScoreBase.class.getName()) {

            @Override
            public void do_stuff() throws Exception
            {
                mainwin.setWaitCursor();

                DBBillingPeriod bp = new DBBillingPeriod();
                Transaction trans = mainwin.getTransaction();
                List<DBBillingPeriod> bp_list = trans.fetchTable2(bp, "order by " + trans.markColumn("hist_anzeit") + " desc");

                StringBuilder sb = new StringBuilder();
                boolean error_happened = false;

                for( DBBillingPeriod current_bp : bp_list )
                {
                    if( !importer.run(current_bp.idx.getValue()) )
                    {
                        sb.append(current_bp.title.getValue()).append("\n");
                        sb.append(importer.getErrorMessage()).append("\n\n");
                        error_happened = true;
                    }
                }

                boolean do_commit = false;

                mainwin.setWaitCursor(false);

                if( error_happened )
                {
                    InfoWin infowin = new InfoWin(mainwin.getRoot(), "Warning", sb.toString());
                    infowin.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    infowin.setVisible(true);
                    infowin.toFront();
                    infowin.requestFocus();

                    if( infowin.pressedYes() )
                        do_commit = true;
                } else {
                    JOptionPane.showMessageDialog(mainwin, "Dataimport was successful!");
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
        } catch( SQLException ex ) {
            logger.error(ex, ex);
        }
    }
}
