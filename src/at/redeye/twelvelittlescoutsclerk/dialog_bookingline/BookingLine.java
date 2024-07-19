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
import at.redeye.twelvelittlescoutsclerk.ContactHelper;
import at.redeye.twelvelittlescoutsclerk.EventHelper;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.MemberHelper;
import at.redeye.twelvelittlescoutsclerk.NewSequenceValueInterface;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine2Events;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.dialog_contact.EditContact;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
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
    
    // Key = DBBookingLine.idx
    HashMap<Integer,DBBookingLine2Events> bl2es = new HashMap<>();
    TableManipulator tm;
    Audit audit;    

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
        tm.hide(bookinglines.idx);        
        
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
        
        jTContent.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
        @Override
        public void valueChanged(ListSelectionEvent event) {
            onBookingLineSelectionChanged();
        }
    });
    }
    
    private void feed_table() {
        feed_table(true);
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
                values = trans.fetchTable2(bookingline,
                        "where " + trans.markColumn(bookingline.bp_idx) + " = " + mainwin.getBPIdx()
                        + " order by " + trans.markColumn(bookingline.date));
                
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
        tableFilter1 = new at.redeye.twelvelittlescoutsclerk.tableFilter();
        jLabel1 = new javax.swing.JLabel();
        jLInfo = new javax.swing.JLabel();

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

        jLabel9.setText("Event:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jBAutoDetect)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBApplyBookingLine))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
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
                            .addComponent(jTDataSource)
                            .addComponent(jTDate)
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
                                        .addComponent(jCContact, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jCMember, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jCEvent, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBApplyBookingLine)
                    .addComponent(jBAutoDetect))
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
                        .addComponent(jLInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
        
        for (Integer i : tm.getEditedRows()) {

            if( i < 0 ) {
                continue;
            }
            
            DBBookingLine entry = values.get(i);

            entry.hist.setAeHist(root.getUserName());
            getTransaction().updateValues(entry);                       
        }
        
        if( bl2es.size() > 0 ) {
            
            for( DBBookingLine2Events bl2e : bl2es.values() ) {
                if( bl2e.idx.getValue() != 0 ) {
                    bl2e.idx.loadFromCopy(getNewSequenceValue(DBBookingLine2Events.BOOKINGLINE2EVENTS_IDX_SEQUENCE));
                }

                DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, bl2e);
            }
        }


        trans.commit();
    }
    
    private void jBSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBSaveActionPerformed

       final BaseDialog parent = this;
        
       new AutoMBox(getTitle()) {

            @Override
            public void do_stuff() throws Exception {
                
                save();
                close();
                //feed_table();
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
            
        int row = tm.getSelectedRow();
        
        if( row < 0 ) {
            return;
        }
        
        current_value.loadFromCopy(values.get(row));
        
        var l2e = bl2es.get(current_value.idx.getValue());
        
        if( l2e != null ) {
            
        }
        
        var_to_gui();    
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
        
        bl2es.remove(current_value.idx.getValue());
        
        EventDescr event_descr = (EventDescr) jCEvent.getSelectedItem();
        if( event_descr == null ) {
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
        
        if( contact_descr != null ) {
            bl2e.contact_idx.loadFromCopy(contact_descr.contact.idx.getValue());
        }
        
        bl2es.put(current_value.idx.getValue(), bl2e);
        
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
                for( DBEvent event : events ) {
                    jCEvent.addItem(new EventDescr( event ));
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBApplyBookingLine;
    private javax.swing.JButton jBAutoDetect;
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBCreateNewContact;
    private javax.swing.JButton jBDel;
    private javax.swing.JButton jBEdit;
    private javax.swing.JButton jBNew;
    private javax.swing.JButton jBSave;
    private javax.swing.JComboBox<ContactDescr> jCContact;
    private javax.swing.JComboBox<EventDescr> jCEvent;
    private javax.swing.JComboBox<MemberDescr> jCMember;
    private javax.swing.JLabel jLInfo;
    private javax.swing.JLabel jLabel1;
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
    private javax.swing.JTextField jTBankAccountBIC;
    private javax.swing.JTextField jTBankAccountIBAN;
    private javax.swing.JTextField jTComment;
    private javax.swing.JTable jTContent;
    private javax.swing.JTextField jTDataSource;
    private javax.swing.JTextField jTDate;
    private javax.swing.JTextField jTFrom;
    private javax.swing.JTextArea jTLine;
    private javax.swing.JTextField jTReference;
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
