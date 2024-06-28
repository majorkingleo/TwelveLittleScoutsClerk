/**
 * TwelveLittleScoutsClerk Member search dialog
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_contact;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.BaseDialog;
import at.redeye.FrameWork.base.BaseDialogDialog;
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
import at.redeye.twelvelittlescoutsclerk.ContactNameCombo;
import at.redeye.twelvelittlescoutsclerk.DocumentFieldDoubleAndNoComma;
import at.redeye.twelvelittlescoutsclerk.LocalHelpWinModal;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.MemberSearch;
import at.redeye.twelvelittlescoutsclerk.NewSequenceValueInterface;
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
public class EditContact extends BaseDialogDialog implements NewSequenceValueInterface {

    DBContact contact;
    DBContact contact_old;
    boolean saved = false;
    MainWin mainwin;
    boolean started = false;
    List<DBContact2MemberView> c2msv = new ArrayList<>();
    List<DBContact2MemberView> c2msv_to_remove = new ArrayList<>();
    TableManipulator tm;
    
    /**
     * Creates new form EditKunde
     */
    public EditContact(MainWin mainwin, DBContact contact) 
    {
        super(mainwin.root,  getTitle(contact ));
        initComponents();
        
        
        registerHelpWin(
                new Runnable() {

                    @Override
                    public void run() {
                        invokeDialogModal(new LocalHelpWinModal(root, "EditKunde"));
                    }
                });          
        
        this.contact = contact;
        this.mainwin = mainwin;
        
        contact_old = new DBContact();
        contact_old.loadFromCopy(contact);
        
        bindVar(jTName, contact.name);
        bindVar(jTVorname, contact.forname);
        bindVar(jTtel,contact.tel);
        bindVar(jTEmail,contact.email);
        bindVar(jTBIC,contact.bank_account_bic);
        bindVar(jTIBAN,contact.bank_account_iban);
        bindVar(jTNote,contact.note);
        
        
        DBContact2MemberView c2m = new DBContact2MemberView();
        
        tm = new TableManipulator(root, jTC2M, c2m);

         
         
        tm.hide(c2m.m2c.hist.lo_user);        
        tm.hide(c2m.m2c.hist.lo_zeit);        
        tm.hide(c2m.m2c.hist.ae_user);
        tm.hide(c2m.m2c.hist.an_user);
        tm.hide(c2m.m2c.hist.an_zeit);
        tm.hide(c2m.m2c.hist.ae_zeit);
        tm.hide(c2m.m2c.bp_idx);
        tm.hide(c2m.m2c.idx);
        tm.hide(c2m.m2c.contact_idx);
        tm.hide(c2m.m2c.member_idx);
        
        tm.hide(c2m.member.hist.lo_user);        
        tm.hide(c2m.member.hist.lo_zeit);        
        tm.hide(c2m.member.hist.ae_user);
        tm.hide(c2m.member.hist.an_user);
        tm.hide(c2m.member.hist.an_zeit);
        tm.hide(c2m.member.hist.ae_zeit);
        tm.hide(c2m.member.bp_idx);
        tm.hide(c2m.member.idx);        

        tm.prepareTable();
        
        feed_c2m_table(false);   
        
        tm.autoResize();
               
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
        super.var_to_gui();
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
    
    static String getTitle( DBContact contact )
    {
        return ContactNameCombo.getName4Contact(contact);
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
        
        if( contact.name.isEmptyTrimmed() ) {
            JOptionPane.showMessageDialog(this, MlM("Bitte einen Namen eingeben"));
            jTName.requestFocus();
            return false;
       }
     
        
        List<DBContact> contact_list = trans.fetchTable2(contact, "where " + trans.markColumn(contact.bp_idx) + "  = " + contact.bp_idx
                + " and " + trans.markColumn(contact.name) + " = '" + contact.name + "' "
                + " and " + trans.markColumn(contact.forname) + " = '" + contact.forname + "' "
                + " and " + trans.markColumn(contact.idx) + " != " + contact.idx.getValue());
        
        if( !contact_list.isEmpty() ) {
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

    private void feed_c2m_table() {
        feed_c2m_table(true);
    }

    private void feed_c2m_table(boolean autombox) {
        new AutoMBox(getTitle(), autombox) {

            @Override
            public void do_stuff() throws Exception {

                tm.clear();
                c2msv.clear();
                clearEdited();

                DBMember member = new DBMember();
                DBMembers2Contacts m2cs = new DBMembers2Contacts();

                Transaction trans = getTransaction();
                List<DBMember> members = trans.fetchTable2(member,
                        "where " + trans.markColumn(member.bp_idx) + " = " + mainwin.getBPIdx()
                        + " and " + trans.markColumn(member.idx)
                        + " in ( select " + trans.markColumn(m2cs.member_idx) + " from " + trans.markTable(m2cs) + " where "
                                + trans.markColumn(m2cs.contact_idx) + " = " + contact.idx.toString() + " ) "
                        + " order by " + trans.markColumn(member.name));
                
                for (DBMember entry : members) {
                    DBContact2MemberView v = new DBContact2MemberView();
                    v.member.loadFromCopy(entry);
                    c2msv.add(v);
                } // for
                
                List<DBMembers2Contacts> m2css = trans.fetchTable2(m2cs,
                        "where " + trans.markColumn(m2cs.bp_idx) + " = " + mainwin.getBPIdx()                        
                        + " and " + trans.markColumn(m2cs.contact_idx) + " = " + contact.idx.toString());
                
                for (DBContact2MemberView entry : c2msv) {
                    for (DBMembers2Contacts m2c : m2css) {
                        if( m2c.member_idx.getValue().equals(entry.member.idx.getValue()) ) {
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

        jLabel2 = new javax.swing.JLabel();
        jTName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTVorname = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jBClose = new javax.swing.JButton();
        jBSave = new javax.swing.JButton();
        jBClose1 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jTtel = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTC2M = new javax.swing.JTable();
        jBAddContact = new javax.swing.JButton();
        jBRemoveMember = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTEmail = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTBIC = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTIBAN = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTNote = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(548, 360));

        jLabel2.setText("Name");

        jLabel3.setText("Vorname");

        jBClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/fileclose.gif"))); // NOI18N
        jBClose.setText("Close");
        jBClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCloseActionPerformed(evt);
            }
        });

        jBSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/button_ok.gif"))); // NOI18N
        jBSave.setText("Save");
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        jLabel13.setText("Telefonnummer");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Members"));

        jTC2M.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTC2M);

        jBAddContact.setText("add Member");
        jBAddContact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBAddContactActionPerformed(evt);
            }
        });

        jBRemoveMember.setText("remove Member");
        jBRemoveMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBRemoveMemberActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 605, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBAddContact, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBRemoveMember, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jBAddContact)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBRemoveMember)
                .addContainerGap(115, Short.MAX_VALUE))
        );

        jLabel1.setText("Email");

        jLabel4.setText("BIC");

        jLabel5.setText("IBAN");

        jLabel6.setText("Note");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel13)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4)
                            .addComponent(jLabel6))
                        .addGap(68, 68, 68)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTName)
                            .addComponent(jTVorname)
                            .addComponent(jTtel)
                            .addComponent(jTEmail)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTBIC, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTIBAN))
                            .addComponent(jTNote))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jTVorname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jTtel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jTEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTBIC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5)
                        .addComponent(jTIBAN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jTNote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        final EditContact parent = this;
        
        new AutoMBox(EditContact.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                if (check()) {                                                                                         
                    
                    Transaction trans = getTransaction();
                    
                    DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, contact, contact.hist, "root" );
                    
                    for( DBContact2MemberView m2csv : c2msv_to_remove ) {
                        trans.deleteWithPrimaryKey(m2csv.m2c);
                    }
                    
                    for( DBContact2MemberView m2 : c2msv ) {
                        DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, m2.m2c, m2.m2c.hist, "root" );
                    }
                    
                    trans.commit();
                    
                    saved = true;
                    close();
                }
            }
        };

    }//GEN-LAST:event_jBSaveActionPerformed

    private void jBClose1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBClose1ActionPerformed
        callHelpWin();
    }//GEN-LAST:event_jBClose1ActionPerformed

    private Set<Integer> getMemberIds()
    {
        Set<Integer> ret = new HashSet<>();
        
        for( DBContact2MemberView m : c2msv ) {
            ret.add(m.member.idx.getValue());
        }
        
        return ret;
    }
    
    
    private void jBAddContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBAddContactActionPerformed
        
        MemberSearch ms = new MemberSearch( mainwin, "Add a new member");
        Set<Integer> ids = getMemberIds();
        
        ms.addFilter(new MemberSearch.Filter() {
            @Override
            public boolean accept(DBMember member) {
                return !ids.contains(member.idx.getValue());
            }
        });
        
        invokeDialogModal(ms);
        
        List<DBMember> selected_members = ms.getSelectedEntries();
        
        if( selected_members == null ) {
            return;
        }
        
        new AutoMBox(EditContact.class.getName()) {

            @Override
            public void do_stuff() throws Exception {

                for (DBMember member : selected_members) {

                    DBContact2MemberView entry = createEntry(member);

                    c2msv.add(entry);
                    tm.add(entry);
                }
            }
        };
    }//GEN-LAST:event_jBAddContactActionPerformed

    private void jBRemoveMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBRemoveMemberActionPerformed
        
        Set<Integer> rows = tm.getSelectedRows();
        
        if( rows == null ) {
            return;
        }
        
        List<Integer> rows_up_to_down = new ArrayList<>(rows);
        Collections.sort(rows_up_to_down, Collections.reverseOrder());
        
        for( int row : rows_up_to_down ) {
            c2msv_to_remove.add(c2msv.get(row));
            
            tm.remove(row);
            c2msv.remove(row);            
        }
        
    }//GEN-LAST:event_jBRemoveMemberActionPerformed

  
    DBContact2MemberView createEntry( DBMember member ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        Transaction trans = getTransaction();
        
        DBContact2MemberView c2ms = new DBContact2MemberView();
                       
        c2ms.m2c.idx.loadFromCopy(mainwin.getNewSequenceValue(DBMembers2Contacts.MEMBERS2CONTACTS_IDX_SEQUENCE));
        c2ms.m2c.bp_idx.loadFromCopy(mainwin.getBPIdx());
        c2ms.m2c.contact_idx.loadFromCopy(contact.idx.getValue());
        c2ms.m2c.member_idx.loadFromCopy(member.idx.getValue());        
        c2ms.m2c.hist.setAnHist("root");
        c2ms.member.loadFromCopy(member);       
        
        return c2ms;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBAddContact;
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBClose1;
    private javax.swing.JButton jBRemoveMember;
    private javax.swing.JButton jBSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTBIC;
    private javax.swing.JTable jTC2M;
    private javax.swing.JTextField jTEmail;
    private javax.swing.JTextField jTIBAN;
    private javax.swing.JTextField jTName;
    private javax.swing.JTextField jTNote;
    private javax.swing.JTextField jTVorname;
    private javax.swing.JTextField jTtel;
    // End of variables declaration//GEN-END:variables


}
