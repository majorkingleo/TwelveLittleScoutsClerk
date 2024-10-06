/**
 * TwelveLittleScoutsClerk Dialog for Contact table
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_bookingline;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.BaseDialog;
import at.redeye.FrameWork.base.DefaultInsertOrUpdater;
import at.redeye.FrameWork.base.Setup;
import at.redeye.FrameWork.base.tablemanipulator.TableManipulator;
import at.redeye.FrameWork.base.tablemanipulator.validators.DateValidator;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.Audit;
import at.redeye.twelvelittlescoutsclerk.BookingLineHelper;
import at.redeye.twelvelittlescoutsclerk.ContactHelper;
import at.redeye.twelvelittlescoutsclerk.EventHelper;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.MemberHelper;
import at.redeye.twelvelittlescoutsclerk.NewSequenceValueInterface;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine2Events;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEventMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.dialog_contact.EditContact;
import at.redeye.twelvelittlescoutsclerk.dialog_split.Split;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;

public class BookingLine extends BaseDialog implements NewSequenceValueInterface {

    private static final Logger logger = Logger.getLogger(BookingLine.class.getName());
    
    static class ContactDescr
    {
        public DBContact contact;        
        
        public ContactDescr( DBContact contact )
        {
            this.contact = contact;
        }
        
        @Override
        public String toString()
        {
            return contact.forname.toString() + " " + contact.name.toString();
        }
    }
    
    static class MemberDescr
    {
        public DBMember member;
        
        public MemberDescr( DBMember member )
        {
            this.member = member;
        }
        
        @Override
        public String toString()
        {
            return member.forname.toString() + " " + member.name.toString();
        }
    }
    
    
    static class EventDescr
    {
        public DBEvent event;
                
        public EventDescr( DBEvent event )
        {
            this.event = event;
        }
        
        @Override
        public String toString()
        {
            return event.name.toString() + " " + event.costs.toString() + "€";
        }
    }    
        
    
    MainWin mainwin;
    List<DBBookingLine> values;
    DBBookingLine current_value = new DBBookingLine();
    DBEventMember current_event_member = new DBEventMember();
    
    // Key = DBBookingLine.idx
    HashMap<Integer,DBBookingLine2Events> bl2es = new HashMap<>();
    List<DBBookingLine2Events> bles_to_remove = new ArrayList<>();
    TableManipulator tm;
    Audit audit;    

    ArrayList<JCheckBox> filters = new ArrayList<>();
    
    public BookingLine(MainWin mainwin) {
        super( mainwin.getRoot(), "BookingLines");
        
        initComponents();
            
        this.mainwin = mainwin;
        
        DBBookingLine bookinglines = new DBBookingLine();
        
        tm = new TableManipulator(root, jTContent, bookinglines);

        tm.hide(bookinglines.hist.lo_user);
        tm.hide(bookinglines.hist.lo_zeit);        
        tm.hide(bookinglines.hist.ae_user);
        tm.hide(bookinglines.hist.an_user);
        tm.hide(bookinglines.hist.an_zeit);
        tm.hide(bookinglines.hist.ae_zeit);
        tm.hide(bookinglines.bp_idx);
        //tm.hide(bookinglines.idx);        
        
        tm.setValidator(bookinglines.hist.ae_zeit, new DateValidator());
        tm.setValidator(bookinglines.hist.an_zeit, new DateValidator());               
        
        tm.prepareTable();
        
        feed_table(false);   
        
        tm.autoResize();
        
        tableFilter1.setFilter(jTContent);
                                
        if( mainwin.isAzLocked() ) {
            jBSave.setEnabled(false);
            jBDel.setEnabled(false);
            jBNew.setEnabled(false);
        }
        
        bindVar(jTFrom, current_value.from_name);
        bindVar(jTBankAccountIBAN, current_value.from_bank_account_iban);
        bindVar(jTBankAccountBIC, current_value.from_bank_account_bic);
        bindVar(jTDate, current_value.date);
        bindVar(jTLine, current_value.line);
        bindVar(jTComment, current_value.comment);                
        bindVar(jTDataSource, current_value.data_source);
        bindVar(jTReference, current_value.reference);
        bindVar(jTAmount, current_value.amount);
        bindVar(jtAlreadyPaid, current_event_member.paid);
        bindVar(jTAlreadyPaidInCash, current_event_member.paid_cash);
        
        jTContent.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent event) {
                onBookingLineSelectionChanged();
            }
        });  
        
        filters.add(jCIn);
        filters.add(jCOut);
        filters.add(jCAssigned);
        filters.add(jCUnassigned);
        
        for( int i = 0; i < filters.size(); i++ )
        {
            String check_filter = getUniqueDialogIdentifier("Filter").concat(String.format(".Filter[%d]",i));
            if( Boolean.parseBoolean(root.getSetup().getLocalConfig(check_filter,"False")) ) {
                filters.get(i).setSelected(true);
            }
        }        
    }
    
    @Override
    public void close()
    {
        for( int i = 0; i < filters.size(); i++ )
        {
            String check_filter = getUniqueDialogIdentifier("Filter").concat(String.format(".Filter[%d]",i));
            root.getSetup().setLocalConfig(check_filter,Boolean.toString(filters.get(i).isSelected()));
        }       

        super.close();
    }
    
    private void feed_table() {
        feed_table(true);
    }
    
    @Override
    public void var_to_gui() {
        super.var_to_gui();
        
        jtAlreadyPaid.setBackground(null);
        jTAlreadyPaidInCash.setBackground(null);
        
        final Color WARNING_COLOR = new Color(255,200,200);
        final Color OK_COLOR = new Color(200,255,200);
                
        if( current_event_member.idx.getValue() > 0 ) {
            
            double already_paid = current_event_member.paid.getValue() + current_event_member.paid_cash.getValue() + current_value.amount.getValue();

            if( already_paid > current_event_member.costs.getValue() ) {
                jtAlreadyPaid.setBackground(WARNING_COLOR);
                jTAlreadyPaidInCash.setBackground(WARNING_COLOR);
            }
            else if( Math.abs(already_paid - current_event_member.costs.getValue()) <= 0.01 ) {                               
                jtAlreadyPaid.setBackground(OK_COLOR);
                jTAlreadyPaidInCash.setBackground(OK_COLOR);
            }
        }
    }

    private void feed_table(boolean autombox) {
        new AutoMBox(getTitle(), autombox) {

            @Override
            public void do_stuff() throws Exception {

                tm.clear();
                tm.resetAllCellColors();
                clearEdited();

                DBBookingLine bookingline = new DBBookingLine();

                Transaction trans = getTransaction();
                
                StringBuilder sql = new StringBuilder();
                
                sql.append( "where " + trans.markColumn(bookingline.bp_idx) + " = " + mainwin.getBPIdx());
                sql.append( " and ").append(trans.markColumn(bookingline.splitpos)).append(" = 0 " );
                
                if( jCIn.isSelected() ) {
                    sql.append(" and ").append(trans.markColumn(bookingline.amount)).append(" >= 0 ");
                }
                
                if( jCOut.isSelected() ) {
                    sql.append(" and ").append(trans.markColumn(bookingline.amount)).append(" <= 0 ");
                }
                
                if( jCUnassigned.isSelected() ) {
                    sql.append(" and ").append(trans.markColumn(bookingline.assigned)).append(" = 0 ");
                }
                
                if( jCAssigned.isSelected() ) {
                    sql.append(" and ").append(trans.markColumn(bookingline.assigned)).append(" = 1 ");
                }                
                
                sql.append( " order by " + trans.markColumn(bookingline.date));
                                
                values = trans.fetchTable2(bookingline, sql.toString() );
                
                for (DBBookingLine entry : values) {
                    tm.add(entry);
                }
                
                DBBookingLine2Events bl2e = new DBBookingLine2Events();
                
                List<DBBookingLine2Events> l_ble = trans.fetchTable2(bl2e,
                        "where " + trans.markColumn(bl2e.bp_idx) + " = " + mainwin.getBPIdx());
                
                for( var l2e : l_ble ) {                    
                    bl2es.put(l2e.bl_idx.getValue(),l2e);                                        
                }
                
                // color lines, with an assigned event
                for( int idx = 0; idx < values.size(); idx++ ) {
                    final var value = values.get(idx);
                    var l2e = bl2es.get(value.idx.getValue());

                    if( l2e != null ) {
                        for( var col : value.getAllValues() ) {
                            tm.setCellColor(col, idx, idx % 2 == 0 ? Color.YELLOW : Color.ORANGE);
                        }
                    }
                }
            }
        };
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTContent = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jBClose = new javax.swing.JButton();
        jBDel = new javax.swing.JButton();
        jBNew = new javax.swing.JButton();
        jBSave = new javax.swing.JButton();
        jBEdit = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTFrom = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTBankAccountIBAN = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTDate = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTComment = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTLine = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        jTDataSource = new javax.swing.JTextField();
        jBApplyBookingLine = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jTBankAccountBIC = new javax.swing.JTextField();
        jBAutoDetect = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jTReference = new javax.swing.JTextField();
        jCContact = new javax.swing.JComboBox<>();
        jCMember = new javax.swing.JComboBox<>();
        jBCreateNewContact = new javax.swing.JButton();
        jCEvent = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTAmount = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jtAlreadyPaid = new javax.swing.JTextField();
        jBSplit = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jTAlreadyPaidInCash = new javax.swing.JTextField();
        tableFilter1 = new at.redeye.twelvelittlescoutsclerk.tableFilter();
        jLabel1 = new javax.swing.JLabel();
        jLInfo = new javax.swing.JLabel();
        jCOut = new javax.swing.JCheckBox();
        jCIn = new javax.swing.JCheckBox();
        jCUnassigned = new javax.swing.JCheckBox();
        jCAssigned = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        jTContent.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTContent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTContentMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTContent);

        jBClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/fileclose.gif"))); // NOI18N
        jBClose.setText("Close");
        jBClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCloseActionPerformed(evt);
            }
        });

        jBDel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/edittrash.gif"))); // NOI18N
        jBDel.setText("Delete");
        jBDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBDelActionPerformed(evt);
            }
        });

        jBNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/bookmark.png"))); // NOI18N
        jBNew.setText("New");
        jBNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBNewActionPerformed(evt);
            }
        });

        jBSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/button_ok.gif"))); // NOI18N
        jBSave.setText("Save");
        jBSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBSaveActionPerformed(evt);
            }
        });

        jBEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/edit.png"))); // NOI18N
        jBEdit.setText("Edit");
        jBEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBEditActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("From:");

        jLabel3.setText("Bank account:");

        jLabel4.setText("Date:");

        jLabel5.setText("Comment:");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Booking Line"));

        jTLine.setEditable(false);
        jTLine.setColumns(20);
        jTLine.setLineWrap(true);
        jTLine.setRows(5);
        jTLine.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTLine);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jLabel6.setText("Data source:");

        jTDataSource.setEditable(false);

        jBApplyBookingLine.setText("Apply");
        jBApplyBookingLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBApplyBookingLineActionPerformed(evt);
            }
        });

        jLabel7.setText("BIC:");

        jBAutoDetect.setText("Auto Detect");
        jBAutoDetect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBAutoDetectActionPerformed(evt);
            }
        });

        jLabel8.setText("Reference:");

        jCContact.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jCContactMousePressed(evt);
            }
        });
        jCContact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCContactActionPerformed(evt);
            }
        });

        jCMember.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jCMemberMousePressed(evt);
            }
        });
        jCMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCMemberActionPerformed(evt);
            }
        });

        jBCreateNewContact.setText("create new Contact");
        jBCreateNewContact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCreateNewContactActionPerformed(evt);
            }
        });

        jCEvent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jCEventMousePressed(evt);
            }
        });
        jCEvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCEventActionPerformed(evt);
            }
        });

        jLabel9.setText("Event:");

        jLabel10.setText("Amount:");

        jTAmount.setEditable(false);

        jLabel11.setText("Already paid:");

        jtAlreadyPaid.setEditable(false);

        jBSplit.setText("Split ...");
        jBSplit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBSplitActionPerformed(evt);
            }
        });

        jLabel12.setText("Cash:");

        jTAlreadyPaidInCash.setEditable(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jBSplit)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jBAutoDetect)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBApplyBookingLine))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel9))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTComment)
                                    .addComponent(jTReference)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jTBankAccountIBAN, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jTFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jBCreateNewContact)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel7)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTBankAccountBIC))
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jCContact, 0, 267, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jCMember, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(jCEvent, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTDate)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jTDataSource, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jtAlreadyPaid, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTAlreadyPaidInCash)))))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCContact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCMember, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBCreateNewContact))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTBankAccountIBAN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jTBankAccountBIC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTComment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jTReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTDataSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel10)
                    .addComponent(jTAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jtAlreadyPaid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(jTAlreadyPaidInCash, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBApplyBookingLine)
                    .addComponent(jBAutoDetect)
                    .addComponent(jBSplit))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jBSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBEdit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBDel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jBClose))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBSave)
                    .addComponent(jBNew)
                    .addComponent(jBClose)
                    .addComponent(jBDel)
                    .addComponent(jBEdit))
                .addContainerGap())
        );

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setText("Bookinglines");

        jLInfo.setText(" ");
        jLInfo.setAutoscrolls(true);

        jCOut.setText("Outgoing");
        jCOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCOutActionPerformed(evt);
            }
        });

        jCIn.setText("Incoming");
        jCIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCInActionPerformed(evt);
            }
        });

        jCUnassigned.setText("unassigned");
        jCUnassigned.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCUnassignedActionPerformed(evt);
            }
        });

        jCAssigned.setText("assigned");
        jCAssigned.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCAssignedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCAssigned)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCUnassigned)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCIn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCOut)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableFilter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tableFilter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCOut)
                        .addComponent(jCIn)
                        .addComponent(jCUnassigned)
                        .addComponent(jCAssigned)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCloseActionPerformed

        if (canClose()) {
            close();
        }
    }//GEN-LAST:event_jBCloseActionPerformed

    private void jBDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBDelActionPerformed

        if (!checkAnyAndSingleSelection(jTContent)) {
            return;
        }

        final int i = tm.getSelectedRow();

        if (i < 0 || i >= values.size()) {
            return;
        }
        
        final JFrame parent = this;
        AutoMBox al = new AutoMBox(getTitle()) {

                     @Override
                     public void do_stuff() throws Exception {
                                                  
                         values.remove(i);
                         tm.remove(i);
                         setEdited();
                         save();
                         feed_table();
                     }
                 };
        
        if( al.isFailed() )
        {
            try {
                getTransaction().rollback();                
            } catch( Exception ex ) {
              logger.error(ex,ex);
            }
        }
    }//GEN-LAST:event_jBDelActionPerformed

    private void jBNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBNewActionPerformed
        AutoMBox al = new AutoMBox(BookingLine.class.getName()) {

                     @Override
                     public void do_stuff() throws Exception {
                         newEntry();                
                     }
                 };
        
        if( al.isFailed() ) {
            try {
                getTransaction().rollback();
            } catch( Exception ex ) {
                logger.error(ex,ex);
            }
        }
        
    }//GEN-LAST:event_jBNewActionPerformed

    private void save() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        Transaction trans = getTransaction();
        
        for( var le : bles_to_remove ) {
            trans.deleteWithPrimaryKey(le);
        }
        
        Set<Integer> events2update = new HashSet<>();
        
        for (Integer i : tm.getEditedRows()) {

            if( i < 0 ) {
                continue;
            }
            
            DBBookingLine entry = values.get(i);            
            DBBookingLine2Events bl2e = bl2es.get(entry.idx.getValue());
            
            if( bl2e == null ) {
                DBBookingLine2Events bl2 = new DBBookingLine2Events();
                List<DBBookingLine2Events> to_delete = trans.fetchTable2(bl2," where " + trans.markColumn(bl2,bl2.bl_idx) + " = " + entry.idx.toString() );
                
                for( var td : to_delete ) {
                    events2update.add(td.event_idx.getValue());
                    trans.deleteWithPrimaryKey(td);
                }
                
                entry.assigned.loadFromCopy(0);
            } else {
                entry.assigned.loadFromCopy(1);
            }

            entry.hist.setAeHist(root.getUserName());
            trans.updateValues(entry);         
        }
        
        if( bl2es.size() > 0 ) {
            
            for( DBBookingLine2Events bl2e : bl2es.values() ) {
                if( bl2e.idx.getValue() == 0 ) {
                    bl2e.idx.loadFromCopy(getNewSequenceValue(DBBookingLine2Events.BOOKINGLINE2EVENTS_IDX_SEQUENCE));
                }

                DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, bl2e);
            }            
        }
        
        List<DBEvent> events = getEvents4Update(events2update); 

        for( var event : events ) {
            EventHelper.calc_paid_values_4_event( trans, event );
        }


        trans.commit();
    }
    
    /** returns an empty list if empty */
    private List<DBEvent> getEvents4Update( Set<Integer> events2update ) throws UnsupportedDBDataTypeException, WrongBindFileFormatException, SQLException, TableBindingNotRegisteredException, IOException
    {
        HashSet<Integer> events = new HashSet<>();
        events.addAll(events2update);
        
        for( var bl2 : bl2es.values() ) {
            events.add(bl2.event_idx.getValue());
        }
        
        List<DBEvent> ret = new ArrayList<>();
        
        for( var event_idx : events ) {
            DBEvent ev = new DBEvent();
            ev.idx.loadFromCopy(event_idx);
            getTransaction().fetchTableWithPrimkey(ev);
            ret.add(ev);
        }
        
        return ret;
    }
    
    private void jBSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBSaveActionPerformed

       final BaseDialog parent = this;
        
       new AutoMBox(getTitle()) {

            @Override
            public void do_stuff() throws Exception {
                
                save();
                //close();
                feed_table();
            }
        };
    }//GEN-LAST:event_jBSaveActionPerformed

    private void jBEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBEditActionPerformed

        if (!checkAnyAndSingleSelection(jTContent)) {
            System.out.println("nothing is selected");
            return;
        }

        final int i = tm.getSelectedRow();

        if (i < 0 || i >= values.size()) {
            return;
        }
/*
        final EditMember editkunde = new EditMember(mainwin, values.get(i));
        editkunde.registerOnCloseListener(new Runnable() {

            @Override
            public void run() {

                new AutoMBox(Contact.class.getName()) {

                    @Override
                    public void do_stuff() throws Exception {
                        setEdited();
                        save();
                        feed_table();
                        toFront();
                    }
                };

            }
        });

        invokeDialogUnique(editkunde);
*/
    }//GEN-LAST:event_jBEditActionPerformed

    private void onBookingLineSelectionChanged()
    {
        jCContact.removeAllItems();
        jCMember.removeAllItems();
        jCEvent.removeAllItems();        
        
            
        int row = tm.getSelectedRow();
        
        if( row < 0 ) {
            return;
        }
        
        current_value.loadFromCopy(values.get(row));
        current_event_member.loadFromCopy(new DBEventMember());
        
        var l2e = bl2es.get(current_value.idx.getValue());
        
        new AutoMBox(BookingLine.class.getName()) {
            @Override
            public void do_stuff() throws Exception {
                
                if( l2e != null ) {
                    fillJCContact( l2e.contact_idx.getValue() );
                    fillJCMember( l2e.member_idx.getValue() );
                    fillJCEvent( l2e.event_idx.getValue() );
                } else {
                    fillJCContact( -1 );
                    fillJCMember(-1);
                    fillJCEvent(-1);
                }               
            }
        };
        
        
        var_to_gui();    
    }
        
    private void fillJCContact( int selected_contact_idx ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException 
    {
        jCContact.removeAllItems();
        
        Transaction trans = getTransaction();
        DBContact contact = new DBContact();
        
        var contacts = trans.fetchTable2(contact, "where " + trans.markColumn(contact, contact.bp_idx) + " = " + mainwin.getBPIdx() + 
                " order by " + trans.markColumn(contact,contact.name) + ", " + trans.markColumn(contact, contact.forname) );
        
        int idx = -1 ;
        
        for( int i = 0; i < contacts.size(); i++ ) {
            var c = contacts.get(i);
            
            jCContact.addItem( new ContactDescr(c) );
            if( c.idx.getValue() == selected_contact_idx ) {
                idx = i;
            }
        }
        
        if( idx >= 0 ) {
            jCContact.setSelectedIndex(idx);
        } else {
            jCContact.insertItemAt(null,0);
            jCContact.setSelectedIndex(0);
        }
    }
    
    private void fillJCMember( int selected_member_idx ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException 
    {
        jCMember.removeAllItems();
        
        Transaction trans = getTransaction();
        DBMember member = new DBMember();
        
        var members = trans.fetchTable2(member, "where " + trans.markColumn(member, member.bp_idx) + " = " + mainwin.getBPIdx() + 
                " order by " + trans.markColumn(member,member.name) + ", " + trans.markColumn(member, member.forname) );
        
        int idx = -1 ;
        
        for( int i = 0; i < members.size(); i++ ) {
            var c = members.get(i);
            
            jCMember.addItem( new MemberDescr(c) );
            if( c.idx.getValue() == selected_member_idx ) {
                idx = i;
            }
        }
        
        if( idx >= 0 ) {
            jCMember.setSelectedIndex(idx);
        } else {
            jCMember.insertItemAt(null,0);
            jCMember.setSelectedIndex(0);
        }
    }    
    
    private void fillJCEvent( int selected_event_idx ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException 
    {
        jCEvent.removeAllItems();
        
        Transaction trans = getTransaction();
        DBEvent event = new DBEvent();
        
        var events = trans.fetchTable2(event, "where " + trans.markColumn(event, event.bp_idx) + " = " + mainwin.getBPIdx() + 
                " order by " + trans.markColumn(event,event.name) );
        
        int idx = -1 ;
        
        for( int i = 0; i < events.size(); i++ ) {
            var c = events.get(i);
            
            jCEvent.addItem( new EventDescr(c) );
            if( c.idx.getValue() == selected_event_idx ) {
                idx = i;
            }
        }
        
        if( idx >= 0 ) {
            jCEvent.setSelectedIndex(idx);
            jCEvent.insertItemAt(null,0);
        } else {
            jCEvent.insertItemAt(null,0);            
            jCEvent.setSelectedIndex(0);
        }
    }        
    
    private void jTContentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTContentMouseClicked
        
        /*
        if (evt.getClickCount() == 2) {
            
             JTable target = (JTable)evt.getSource();
             int row = target.getSelectedRow();
             int column = target.getSelectedColumn();
             
            jBEditActionPerformed(null);
        }*/
        onBookingLineSelectionChanged();
    }//GEN-LAST:event_jTContentMouseClicked

    private void jBAutoDetectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBAutoDetectActionPerformed
        
        new AutoMBox(this.getClass().getCanonicalName()) {
            @Override
            public void do_stuff() throws Exception {
                if( current_value.data_source.getValue().equals(BookingLineHelperELBA.DATA_SOURCE) )
                {
                    BookingLineHelperELBA.parseBookingLineText(current_value);
                }
                
                List<DBContact> contacts = ContactHelper.findContactsFor(getTransaction(), current_value);
                
                jCContact.removeAllItems();
                for( DBContact contact : contacts ) {
                    jCContact.addItem(new ContactDescr( contact ));
                }               

               var_to_gui();       
            }
        };                        
    }//GEN-LAST:event_jBAutoDetectActionPerformed

    private void jBApplyBookingLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBApplyBookingLineActionPerformed
        
        gui_to_var();       
        
        int row = tm.getSelectedRow();
        
        if( row < 0 ) {
            return;
        }
        
        values.get(row).loadFromCopy(current_value);
        
        {
            var l2e = bl2es.get(current_value.idx.getValue());
            if( l2e != null ) {
                bles_to_remove.add(l2e);
                bl2es.remove(current_value.idx.getValue());
            }
        }
        
        EventDescr event_descr = (EventDescr) jCEvent.getSelectedItem();
        if( event_descr == null ) {
            tm.setEdited(row);        
            setEdited();
            return;
        }

        MemberDescr member_descr = (MemberDescr) jCMember.getSelectedItem();
        if( member_descr == null ) {
            return;
        }

        ContactDescr contact_descr = (ContactDescr) jCContact.getSelectedItem();
        if( contact_descr == null ) {
            return;
        }
        
        DBBookingLine2Events bl2e = new DBBookingLine2Events();
        bl2e.bl_idx.loadFromCopy(current_value.idx.getValue());
        bl2e.bp_idx.loadFromCopy(current_value.bp_idx.getValue());
        bl2e.event_idx.loadFromCopy(event_descr.event.idx.getValue());
        bl2e.member_idx.loadFromCopy(member_descr.member.idx.getValue());
        bl2e.member_name.loadFromString(member_descr.member.forname.toString() + " " + member_descr.member.name.toString() );
        bl2e.event_name.loadFromString(event_descr.event.name.toString());
        
        if( contact_descr != null ) {
            bl2e.contact_idx.loadFromCopy(contact_descr.contact.idx.getValue());
        }
        
        bl2es.put(current_value.idx.getValue(), bl2e);
        
        tm.setEdited(row);        
        setEdited();
    }//GEN-LAST:event_jBApplyBookingLineActionPerformed

    private void jBCreateNewContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCreateNewContactActionPerformed
        
       gui_to_var();
       Transaction trans = getTransaction();
        
       new AutoMBox(this.getClass().getSimpleName(), true) {
           @Override
           public void do_stuff() throws Exception {

               DBContact contact = new DBContact();
               contact.bp_idx.loadFromCopy(mainwin.getBPIdx());
               contact.bank_account_bic.loadFromString(current_value.from_bank_account_bic.toString());
               contact.bank_account_iban.loadFromString(current_value.from_bank_account_iban.toString());
               contact.name.loadFromString(current_value.from_name.toString());
               contact.idx.loadFromCopy(getNewSequenceValue(DBContact.CONTACT_IDX_SEQUENCE));
               
               EditContact ec = new EditContact(mainwin, contact);
               invokeDialogModal(ec);                              
           }
       };
        
    }//GEN-LAST:event_jBCreateNewContactActionPerformed

    private void jCContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCContactActionPerformed
                  
        new AutoMBox(this.getClass().getCanonicalName()) {
            @Override
            public void do_stuff() throws Exception {
                ContactDescr descr = (ContactDescr)jCContact.getSelectedItem();

                if( descr == null ) {
                    return;
                }
                
                List<DBMember> members = MemberHelper.findMembersFor(getTransaction(), descr.contact);
                
                jCMember.removeAllItems();
                for( DBMember member : members ) {
                    jCMember.addItem(new MemberDescr( member ));
                }

            }
        };
        
    }//GEN-LAST:event_jCContactActionPerformed

    private void jCMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCMemberActionPerformed
        
        new AutoMBox(this.getClass().getCanonicalName()) {
            @Override
            public void do_stuff() throws Exception {
                MemberDescr descr = (MemberDescr)jCMember.getSelectedItem();

                if( descr == null ) {
                    return;
                }
                
                List<DBEvent> events = EventHelper.findEventsFor(getTransaction(), descr.member);
                
                jCEvent.removeAllItems();
                jCEvent.addItem(null);
                
                int selected_index = 0;
                int index = 0;
                
                for( DBEvent event : events ) {
                    index++;
                    jCEvent.addItem(new EventDescr( event ));
                    
                    if( Math.abs(event.costs.getValue() - current_value.amount.getValue()) <= 0.01 ) {
                        selected_index = index;
                    }
                    
                    if( selected_index == 0 &&
                        event.costs.getValue() > current_value.amount.getValue() ) {
                        selected_index = index;
                    }
                }
               
                if( !events.isEmpty() ) {                                                           
                    jCEvent.setSelectedIndex(selected_index);
                }
            }
        };
        
    }//GEN-LAST:event_jCMemberActionPerformed

    private void jCContactMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCContactMousePressed
        
        boolean do_popup = evt.isPopupTrigger();

        if (!do_popup && Setup.is_win_system()) {
            if (evt.getButton() == MouseEvent.BUTTON3) {
                do_popup = true;
            }
        }
        
        if( !do_popup ) {
            return;
        }
        
        ContactDescr descr = (ContactDescr) jCContact.getSelectedItem();
        
        JPopupMenu popup = new ActionPopupContacts(mainwin, descr);
        popup.show(evt.getComponent(), evt.getX(), evt.getY());                
    }//GEN-LAST:event_jCContactMousePressed

    private void jCMemberMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCMemberMousePressed
        
        boolean do_popup = evt.isPopupTrigger();

        if (!do_popup && Setup.is_win_system()) {
            if (evt.getButton() == MouseEvent.BUTTON3) {
                do_popup = true;
            }
        }
        
        if( !do_popup ) {
            return;
        }
        
        MemberDescr descr = (MemberDescr) jCMember.getSelectedItem();       
        
        JPopupMenu popup = new ActionPopupMembers(mainwin, descr);
        popup.show(evt.getComponent(), evt.getX(), evt.getY());
    }//GEN-LAST:event_jCMemberMousePressed

    private void jCEventMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCEventMousePressed
                
        boolean do_popup = evt.isPopupTrigger();

        if (!do_popup && Setup.is_win_system()) {
            if (evt.getButton() == MouseEvent.BUTTON3) {
                do_popup = true;
            }
        }
        
        if( !do_popup ) {
            return;
        }
        
        EventDescr descr = (EventDescr) jCEvent.getSelectedItem();        
        
        JPopupMenu popup = new ActionPopupEvents(mainwin, descr);
        popup.show(evt.getComponent(), evt.getX(), evt.getY());
        
    }//GEN-LAST:event_jCEventMousePressed

    private void jCEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCEventActionPerformed
                    
        var event_descr = (EventDescr)jCEvent.getSelectedItem();
        
        if( event_descr == null ) {
            if( current_event_member.idx.getValue() != 0 ) {
                current_event_member.loadFromCopy(new DBEventMember());            
                var_to_gui();
            }
            return;
        }
        
        var member_descr = (MemberDescr)jCMember.getSelectedItem();
        
        if( member_descr == null ) {
            if( current_event_member.idx.getValue() != 0 ) {
                current_event_member.loadFromCopy(new DBEventMember());
                var_to_gui();
            }
            return;
        }
        
        final Transaction trans = getTransaction();
        
        new AutoMBox(BookingLine.class.getName()) {
            @Override
            public void do_stuff() throws Exception {
                var event_member = EventHelper.get_event_member(trans, member_descr.member, event_descr.event);
        
                if( event_member != null ) {
                    current_event_member.loadFromCopy(event_member);
                    var_to_gui();
                } else {
                     if( current_event_member.idx.getValue() != 0 ) {
                        current_event_member.loadFromCopy(new DBEventMember());
                        var_to_gui();
                      }
                }
            }
        };
                     
    }//GEN-LAST:event_jCEventActionPerformed

    private void jCOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCOutActionPerformed
        feed_table(true);
    }//GEN-LAST:event_jCOutActionPerformed

    private void jCInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCInActionPerformed
        feed_table(true);
    }//GEN-LAST:event_jCInActionPerformed

    private void jBSplitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBSplitActionPerformed
        
        final var dlg = this;
        Transaction trans = getTransaction();
        
        new AutoMBox(BookingLine.class.getName()) {
            @Override
            public void do_stuff() throws Exception {          
                gui_to_var();

                if( current_value == null ) {
                    return;
                }
                
                save();
                
                Split dlg_split = null;
                List<DBBookingLine> childs = null;
                DBBookingLine parent = current_value;
                
                if( current_value.parent_idx.getValue() > 0 ) {
                    // fetch all split positions as the original one
                    parent = new DBBookingLine();
                    parent.idx.loadFromCopy(current_value.parent_idx.getValue());
                    trans.fetchTableWithPrimkey(parent);
                    
                    childs = trans.fetchTable2(parent,
                            " where " + trans.markColumn(parent,parent.parent_idx) + " = " + parent.idx.toString() );
                    
                
                    dlg_split = new Split(dlg,root,parent,childs);
                    
                } else {
                    dlg_split = new Split(dlg,root,parent);
                }
                
                invokeDialogModal(dlg_split);

                var lines = dlg_split.getLines();

                if( lines == null ) {
                    return;
                }
                  
                HashSet<Integer> sLineIdx = new HashSet<>();
                
                parent.splitpos.loadFromCopy(lines.isEmpty() ? 0 : 1);
                
                trans.updateValues(parent);
                
                for( DBBookingLine line : lines )
                {
                   line.parent_idx.loadFromCopy(parent.idx.getValue());                  
                   
                   if( line.idx.getValue() == 0 ) {
                    line.idx.loadFromCopy(getNewSequenceValue(DBBookingLine.BOOKING_LINE_IDX_SEQUENCE));
                    getTransaction().insertValues(line);
                   }
                   
                   DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, line, line.hist, root.getUserName());
                
                   sLineIdx.add(line.idx.getValue());
                }
                
                if( childs != null ) 
                {
                    for( DBBookingLine child : childs )
                    {
                        if( !sLineIdx.contains(child.idx.getValue()) ) {
                            BookingLineHelper.delete_bookingline(trans, child);
                        }
                    }
                }                               
                
                save();
                
                feed_table(false);
            }
        }; 
        
    }//GEN-LAST:event_jBSplitActionPerformed

    private void jCUnassignedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCUnassignedActionPerformed
        feed_table(true);
    }//GEN-LAST:event_jCUnassignedActionPerformed

    private void jCAssignedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCAssignedActionPerformed
        feed_table(true);
    }//GEN-LAST:event_jCAssignedActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBApplyBookingLine;
    private javax.swing.JButton jBAutoDetect;
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBCreateNewContact;
    private javax.swing.JButton jBDel;
    private javax.swing.JButton jBEdit;
    private javax.swing.JButton jBNew;
    private javax.swing.JButton jBSave;
    private javax.swing.JButton jBSplit;
    private javax.swing.JCheckBox jCAssigned;
    private javax.swing.JComboBox<ContactDescr> jCContact;
    private javax.swing.JComboBox<EventDescr> jCEvent;
    private javax.swing.JCheckBox jCIn;
    private javax.swing.JComboBox<MemberDescr> jCMember;
    private javax.swing.JCheckBox jCOut;
    private javax.swing.JCheckBox jCUnassigned;
    private javax.swing.JLabel jLInfo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTAlreadyPaidInCash;
    private javax.swing.JTextField jTAmount;
    private javax.swing.JTextField jTBankAccountBIC;
    private javax.swing.JTextField jTBankAccountIBAN;
    private javax.swing.JTextField jTComment;
    private javax.swing.JTable jTContent;
    private javax.swing.JTextField jTDataSource;
    private javax.swing.JTextField jTDate;
    private javax.swing.JTextField jTFrom;
    private javax.swing.JTextArea jTLine;
    private javax.swing.JTextField jTReference;
    private javax.swing.JTextField jtAlreadyPaid;
    private at.redeye.twelvelittlescoutsclerk.tableFilter tableFilter1;
    // End of variables declaration//GEN-END:variables

    private void newEntry() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException {
                       
        DBBookingLine bookingline = new DBBookingLine();
        bookingline.bp_idx.loadFromCopy(mainwin.getBPIdx());
/*        
        CreateContact create_contact = new CreateContact(mainwin, this, contact);
        
        invokeDialogModal(create_contact);
        
        if( create_contact.pressedSave() )
        {        
            mainwin.getAudit().openNewAudit();            
                                                                     
            tm.add(contact, true, true);
            values.add(contact);
            setEdited();
            save();
            feed_table();
            toFront();
        }
*/
    }    
    
    @Override
    public boolean canClose() {
        int ret = checkSave(tm);

        if (ret == 1) {
            jBSaveActionPerformed(null);
        } else if (ret == -1) {
            return false;
        }
        return true;
    } 
}
