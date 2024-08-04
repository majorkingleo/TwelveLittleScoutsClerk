/**
 * TwelveLittleScoutsClerk Member search dialog
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_member;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.BaseDialog;
import at.redeye.FrameWork.base.DefaultInsertOrUpdater;
import at.redeye.FrameWork.base.UniqueDialogHelper;
import at.redeye.FrameWork.base.bindtypes.DBDouble;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.tablemanipulator.TableManipulator;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.FrameWork.widgets.documentfields.DocumentFieldLimit;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.ContactSearch;
import at.redeye.twelvelittlescoutsclerk.DocumentFieldDoubleAndNoComma;
import at.redeye.twelvelittlescoutsclerk.LocalHelpWinModal;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.MemberNameCombo;
import at.redeye.twelvelittlescoutsclerk.NewSequenceValueInterface;
import at.redeye.twelvelittlescoutsclerk.UpdateMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Contacts;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author martin
 */
public class EditMember extends BaseDialog implements NewSequenceValueInterface {

    DBMember member;
    DBMember member_old;
    boolean saved = false;
    MainWin mainwin;
    boolean started = false;
    List<DBMember2ContactView> m2csv = new ArrayList<>();
    List<DBMember2ContactView> m2csv_to_remove = new ArrayList<>();
    TableManipulator tm;
    
    /**
     * Creates new form EditKunde
     */
    public EditMember(MainWin mainwin, DBMember member) 
    {
        super(mainwin.root,  getTitle(member ));
        initComponents();
        
        
        registerHelpWin(
                new Runnable() {

                    @Override
                    public void run() {
                        invokeDialogModal(new LocalHelpWinModal(root, "EditKunde"));
                    }
                });          
        
        this.member = member;
        this.mainwin = mainwin;
        
        member_old = new DBMember();
        member_old.loadFromCopy(member);
        
        bindVar(jTKundennummer, member.member_registration_number);
        bindVar(jTName, member.name);
        bindVar(jTVorname, member.forname);
        bindVar(jDEintrittsdatum, member.entry_date);
        bindVar(jTtel,member.tel);
        bindVar(jCinaktiv,member.inaktiv);
        bindVar(jCgekuendigt,member.de_registered);
        
        
        DBMember2ContactView m2c = new DBMember2ContactView();
        
        tm = new TableManipulator(root, jTM2C, m2c);

         
         
        tm.hide(m2c.m2c.hist.lo_user);        
        tm.hide(m2c.m2c.hist.lo_zeit);        
        tm.hide(m2c.m2c.hist.ae_user);
        tm.hide(m2c.m2c.hist.an_user);
        tm.hide(m2c.m2c.hist.an_zeit);
        tm.hide(m2c.m2c.hist.ae_zeit);
        tm.hide(m2c.m2c.bp_idx);
        tm.hide(m2c.m2c.idx);
        tm.hide(m2c.m2c.contact_idx);
        tm.hide(m2c.m2c.member_idx);
        
        tm.hide(m2c.contact.hist.lo_user);        
        tm.hide(m2c.contact.hist.lo_zeit);        
        tm.hide(m2c.contact.hist.ae_user);
        tm.hide(m2c.contact.hist.an_user);
        tm.hide(m2c.contact.hist.an_zeit);
        tm.hide(m2c.contact.hist.ae_zeit);
        tm.hide(m2c.contact.bp_idx);
        tm.hide(m2c.contact.idx);        

        tm.prepareTable();
        
        feed_m2c_table(false);   
        
        tm.autoResize();
               
        jCGroup.insertItemAt(null, 0);
        
        var_to_gui();
        
        started = true;               
        
        if( mainwin.isAzLocked() )
            jBSave.setEnabled(false);
        

    }        

    @Override
    public boolean openWithLastWidthAndHeight() {
        return false;
    }        
    
    @Override
    public void var_to_gui()
    {
        jCGroup.setSelectedIndex(0);
        
        for( int i = 0; i < jCGroup.getItemCount(); i++ ) {
            String item = jCGroup.getItemAt(i);
            
            if( item != null && item.equals(member.group.getValue()) ) {
                jCGroup.setSelectedIndex(i);
                break;
            }
        }                            
        
        super.var_to_gui();
    }
    
    @Override
    public void gui_to_var()
    {
        Object ogroup = jCGroup.getSelectedItem();
        if( ogroup == null ) {
            member.group.loadFromString("");
        } else {
            String group = (String)ogroup;
            member.group.loadFromString(group);
        }
        
        super.gui_to_var();
    }
       
    final public void bindVar( JTextField field, DBString var )
    {
        super.bindVar(field, var);
        field.setDocument(new DocumentFieldLimit(var.getMaxLen()));
    }    
    
    final public void bindVar( JTextField field, DBDouble var )
    {
        super.bindVar(field, var);
        field.setDocument(new DocumentFieldDoubleAndNoComma(7));
    }         
    
    static String getTitle( DBMember kunde )
    {
        return MemberNameCombo.getName4Member(kunde);
    }
    
    @Override
    public String getUniqueDialogIdentifier(Object requester) {
        if (requester instanceof String) {
            String reason = (String) requester;

            // der is dafür zuständig, dass ein dialog nur einmal geöffnet
            // werden kann,
            // aber in unserem Fall er den Dialog mehrfach öffnen, aber die
            // größen sollen
            // alle gleich sein.
            if (reason.equals(UniqueDialogHelper.ID_STRING)) {
                return super.getUniqueDialogIdentifier(requester);
            }
        }

        return this.getClass().getName();
    }    
    
    private boolean check() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {
        Transaction trans = getTransaction();
        
        if( member.member_registration_number.isEmptyTrimmed() ) {
            JOptionPane.showMessageDialog(this, MlM("Die Kundennummer darf nicht leer sein"));
            jTKundennummer.requestFocus();
            return false;
        }
        
        List<DBMember> member_list = trans.fetchTable2(member, "where " + trans.markColumn(member.bp_idx) + "  = " + member.bp_idx
                + " and " + trans.markColumn(member.member_registration_number) + " = '" + member.member_registration_number + "'" 
                + " and " + trans.markColumn(member.idx) + " != " + member.idx.getValue());
        
        if( !member_list.isEmpty() ) {
            JOptionPane.showMessageDialog(this, MlM("Diese Kundennummer ist bereits vorhanden"));
            jTKundennummer.requestFocus();
            return false;
        }
        
        if( member.name.isEmptyTrimmed() ) {
            JOptionPane.showMessageDialog(this, MlM("Bitte einen Namen eingeben"));
            jTName.requestFocus();
            return false;
        }

        if( member.forname.isEmptyTrimmed() ) {
            JOptionPane.showMessageDialog(this, MlM("Bitte einen Vornamen eingeben"));
            jTName.requestFocus();
            return false;
        }        
        
        member_list = trans.fetchTable2(member, "where " + trans.markColumn(member.bp_idx) + "  = " + member.bp_idx
                + " and " + trans.markColumn(member.name) + " = '" + member.name + "' "
                + " and " + trans.markColumn(member.forname) + " = '" + member.forname + "' "
                + " and " + trans.markColumn(member.idx) + " != " + member.idx.getValue());
        
        if( !member_list.isEmpty() ) {
            JOptionPane.showMessageDialog(this, MlM("Es existiert bereits ein Kunde mit diesem Namen"));
            jTName.requestFocus();
            return false;
        }                        
        
        return true;
    }
    
    public boolean pressedSave()
    {
        return saved;
    }

    private void feed_m2c_table() {
        feed_m2c_table(true);
    }

    private void feed_m2c_table(boolean autombox) {
        new AutoMBox(getTitle(), autombox) {

            @Override
            public void do_stuff() throws Exception {

                tm.clear();
                m2csv.clear();
                clearEdited();

                DBContact contact = new DBContact();
                DBMembers2Contacts m2cs = new DBMembers2Contacts();

                Transaction trans = getTransaction();
                List<DBContact> contacts = trans.fetchTable2(contact,
                        "where " + trans.markColumn(contact.bp_idx) + " = " + mainwin.getBPIdx()
                        + " and " + trans.markColumn(contact.idx)
                        + " in ( select " + trans.markColumn(m2cs.contact_idx) + " from " + trans.markTable(m2cs) + " where "
                                + trans.markColumn(m2cs.member_idx) + " = " + member.idx.toString() + " ) "
                        + " order by " + trans.markColumn(contact.name));
                
                for (DBContact entry : contacts) {
                    DBMember2ContactView v = new DBMember2ContactView();
                    v.contact.loadFromCopy(entry);
                    m2csv.add(v);
                } // for
                
                List<DBMembers2Contacts> m2css = trans.fetchTable2(m2cs,
                        "where " + trans.markColumn(m2cs.bp_idx) + " = " + mainwin.getBPIdx()                        
                        + " and " + trans.markColumn(m2cs.member_idx) + " = " + member.idx.toString());
                
                for (DBMember2ContactView entry : m2csv) {
                    for (DBMembers2Contacts m2c : m2css) {
                        if( m2c.contact_idx.getValue().equals(entry.contact.idx.getValue()) ) {
                            entry.m2c.loadFromCopy(m2c);
                            break;
                        }
                    }
                    
                    tm.add(entry);
                } // for
                                
            }
        };
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTKundennummer = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTVorname = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jBClose = new javax.swing.JButton();
        jBSave = new javax.swing.JButton();
        jBClose1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jDEintrittsdatum = new at.redeye.Plugins.JDatePicker.JDatePicker(root);
        jLabel6 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jTtel = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jCinaktiv = new javax.swing.JCheckBox();
        jCgekuendigt = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTM2C = new javax.swing.JTable();
        jBAddContact = new javax.swing.JButton();
        jBRemoveContact = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jCGroup = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(548, 360));

        jLabel1.setText("Scout ID");

        jLabel2.setText("Name");

        jLabel3.setText("Vorname");

        jBClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/fileclose.gif"))); // NOI18N
        jBClose.setText("Schließen");
        jBClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCloseActionPerformed(evt);
            }
        });

        jBSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/button_ok.gif"))); // NOI18N
        jBSave.setText("Speichern");
        jBSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBSaveActionPerformed(evt);
            }
        });

        jBClose1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/help.png"))); // NOI18N
        jBClose1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBClose1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jBSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 585, Short.MAX_VALUE)
                .addComponent(jBClose1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBClose))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBClose1)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jBSave)
                        .addComponent(jBClose)))
                .addContainerGap())
        );

        jLabel4.setText("Eintrittsdatum");

        jDEintrittsdatum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDEintrittsdatumActionPerformed(evt);
            }
        });

        jLabel6.setText("Attribute");

        jLabel13.setText("Telefonnummer");

        jCinaktiv.setText("inaktiv");

        jCgekuendigt.setText("gekündigt");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCinaktiv)
                    .addComponent(jCgekuendigt))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCinaktiv)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCgekuendigt)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Contacts"));

        jTM2C.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTM2C);

        jBAddContact.setText("add Contact");
        jBAddContact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBAddContactActionPerformed(evt);
            }
        });

        jBRemoveContact.setText("remove Contact");
        jBRemoveContact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBRemoveContactActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBAddContact, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBRemoveContact, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jBAddContact)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBRemoveContact)
                .addContainerGap(97, Short.MAX_VALUE))
        );

        jLabel5.setText("Group");

        jCGroup.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "WiWö", "GuSp", "CaEx", "RaRo", "Leiter" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel13)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5))
                        .addGap(63, 63, 63)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTName, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTVorname, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jDEintrittsdatum, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTKundennummer)
                            .addComponent(jTtel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTKundennummer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTVorname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jDEintrittsdatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTtel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)))
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCloseActionPerformed

        close();
    }//GEN-LAST:event_jBCloseActionPerformed

    private void jBSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBSaveActionPerformed

        gui_to_var();

        final EditMember parent = this;
        
        new AutoMBox(EditMember.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                if (check()) {                                                                                         
                    
                    Transaction trans = getTransaction();
                    
                    UpdateMember updaterKunden = new UpdateMember(root, trans, mainwin.getAudit());
                    updaterKunden.auditKundenDiffAndUpdate(member_old, member);                                        
                    
                    for( DBMember2ContactView m2csv : m2csv_to_remove ) {
                        trans.deleteWithPrimaryKey(m2csv.m2c);
                    }
                    
                    for( DBMember2ContactView m2 : m2csv ) {
                        DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, m2.m2c, m2.m2c.hist, "root" );
                    }
                    
                    trans.commit();
                    
                    saved = true;
                    close();
                }
            }
        };

    }//GEN-LAST:event_jBSaveActionPerformed

    private void jDEintrittsdatumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDEintrittsdatumActionPerformed

   }//GEN-LAST:event_jDEintrittsdatumActionPerformed

    private void jBClose1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBClose1ActionPerformed
        callHelpWin();
    }//GEN-LAST:event_jBClose1ActionPerformed

    private Set<Integer> getContactIds()
    {
        Set<Integer> ret = new HashSet<>();
        
        for( DBMember2ContactView m : m2csv ) {
            ret.add(m.contact.idx.getValue());
        }
        
        return ret;
    }
    
    
    private void jBAddContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBAddContactActionPerformed
        
        ContactSearch cs = new ContactSearch( mainwin, "Add a new contact");
        Set<Integer> ids = getContactIds();
        
        cs.addFilter( new ContactSearch.Filter() {
            @Override
            public boolean accept(DBContact contact) {
                return !ids.contains(member.idx.getValue());
            }
        });
        
        invokeDialogModal(cs);
        
        List<DBContact> selected_contacts = cs.getSelectedEntries();
        
        if( selected_contacts == null ) {
            return;
        }
        
        new AutoMBox(EditMember.class.getName()) {

            @Override
            public void do_stuff() throws Exception {

                for (DBContact contact : selected_contacts) {

                    DBMember2ContactView entry = createEntry(contact);

                    m2csv.add(entry);
                    tm.add(entry);                                       
                }
            }
        };
    }//GEN-LAST:event_jBAddContactActionPerformed

    private void jBRemoveContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBRemoveContactActionPerformed
        
        Set<Integer> rows = tm.getSelectedRows();
        
        if( rows == null ) {
            return;
        }
        
        List<Integer> rows_up_to_down = new ArrayList<>(rows);
        Collections.sort(rows_up_to_down, Collections.reverseOrder());
        
        for( int row : rows_up_to_down ) {
            m2csv_to_remove.add(m2csv.get(row));
            
            tm.remove(row);
            m2csv.remove(row);            
        }
        
    }//GEN-LAST:event_jBRemoveContactActionPerformed

  
    DBMember2ContactView createEntry( DBContact contact ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        Transaction trans = getTransaction();
        
        DBMember2ContactView m2cs = new DBMember2ContactView();
                       
        m2cs.m2c.idx.loadFromCopy(mainwin.getNewSequenceValue(DBMembers2Contacts.MEMBERS2CONTACTS_IDX_SEQUENCE));
        m2cs.m2c.bp_idx.loadFromCopy(mainwin.getBPIdx());
        m2cs.m2c.contact_idx.loadFromCopy(contact.idx.getValue());
        m2cs.m2c.member_idx.loadFromCopy(member.idx.getValue());        
        m2cs.m2c.hist.setAnHist("root");
        m2cs.contact.loadFromCopy(contact);
                      
        return m2cs;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBAddContact;
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBClose1;
    private javax.swing.JButton jBRemoveContact;
    private javax.swing.JButton jBSave;
    private javax.swing.JComboBox<String> jCGroup;
    private javax.swing.JCheckBox jCgekuendigt;
    private javax.swing.JCheckBox jCinaktiv;
    private at.redeye.Plugins.JDatePicker.JDatePicker jDEintrittsdatum;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTKundennummer;
    private javax.swing.JTable jTM2C;
    private javax.swing.JTextField jTName;
    private javax.swing.JTextField jTVorname;
    private javax.swing.JTextField jTtel;
    // End of variables declaration//GEN-END:variables


}
