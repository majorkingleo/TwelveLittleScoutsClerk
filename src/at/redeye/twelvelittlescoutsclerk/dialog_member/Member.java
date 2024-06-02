/**
 * TwelveLittleScoutsClerk Dialog for Member table
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_member;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.BaseDialog;
import at.redeye.FrameWork.base.tablemanipulator.TableManipulator;
import at.redeye.FrameWork.base.tablemanipulator.validators.DateValidator;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.Audit;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.MemberHelper;
import at.redeye.twelvelittlescoutsclerk.NewSequenceValueInterface;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBGroup;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author martin
 */
public class Member extends BaseDialog implements NewSequenceValueInterface {

    MainWin mainwin;
    List<DBMember> values;
    TableManipulator tm;
    Audit audit;

    public Member(MainWin mainwin) {
        super( mainwin.getRoot(), "Members");
        
        initComponents();
        
        this.mainwin = mainwin;
        
        DBMember members = new DBMember();
        
        tm = new TableManipulator(root, jTContent, members);

        // tm.hide(kunden.idx);
        tm.hide(members.hist.lo_user);
        tm.hide(members.hist.lo_zeit);        
        tm.hide(members.hist.ae_user);
        tm.hide(members.hist.an_user);
        tm.hide(members.hist.an_zeit);
        tm.hide(members.hist.ae_zeit);
        tm.hide(members.bp_idx);
        tm.hide(members.idx);

        /*
        tm.setEditable(kunden.name);
        tm.setEditable(kunden.vorname);
        tm.setEditable(kunden.kundennummer);     
        tm.setEditable(kunden.termin);
        tm.setEditable(kunden.eintrittsdatum);                
        */
        
        tm.setValidator(members.entry_date, new DateValidator());
        tm.setValidator(members.hist.ae_zeit, new DateValidator());
        tm.setValidator(members.hist.an_zeit, new DateValidator());               
        
        tm.prepareTable();
        
        feed_table(false);   
        
        tm.autoResize();
        
        tableFilter1.setFilter(jTContent);        
        
        if( mainwin.isAzLocked() ) {
            jBSave.setEnabled(false);
            jBDel.setEnabled(false);
            jBNew.setEnabled(false);
        }
    }
    
    private void feed_table() {
        feed_table(true);
    }
    


    private void feed_table(boolean autombox) {
        new AutoMBox(getTitle(), autombox) {

            @Override
            public void do_stuff() throws Exception {

                tm.clear();
                clearEdited();

                Transaction trans = getTransaction();
                values = MemberHelper.fetch_members( trans, mainwin.getBPIdx() );               
                
                for (DBMember entry : values) {
                    tm.add(entry);
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
        jBClose.setText("Schließen");
        jBClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCloseActionPerformed(evt);
            }
        });

        jBDel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/edittrash.gif"))); // NOI18N
        jBDel.setText("Löschen");
        jBDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBDelActionPerformed(evt);
            }
        });

        jBNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/bookmark.png"))); // NOI18N
        jBNew.setText("Neu");
        jBNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBNewActionPerformed(evt);
            }
        });

        jBSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/button_ok.gif"))); // NOI18N
        jBSave.setText("Speichern");
        jBSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBSaveActionPerformed(evt);
            }
        });

        jBEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/edit.png"))); // NOI18N
        jBEdit.setText("Bearbeiten");
        jBEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBEditActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBNew)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBEdit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBDel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addComponent(jBClose)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBSave)
                    .addComponent(jBNew)
                    .addComponent(jBClose)
                    .addComponent(jBDel)
                    .addComponent(jBEdit))
                .addContainerGap())
        );

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setText("Members");

        jLInfo.setText(" ");
        jLInfo.setAutoscrolls(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 631, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
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
        AutoMBox al = new AutoMBox(Member.class.getName()) {

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
        for (Integer i : tm.getEditedRows()) {

            if( i < 0 )
                continue;
            
            DBMember entry = values.get(i);

            entry.hist.setAeHist(root.getUserName());
            getTransaction().updateValues(entry);
        }

        getTransaction().commit();
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
            System.out.println("i: " + i + " values " + values.size());
            return;
        }

        final EditMember editkunde = new EditMember(mainwin, values.get(i));
        editkunde.registerOnCloseListener(new Runnable() {

            @Override
            public void run() {

                new AutoMBox(Member.class.getName()) {

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

    }//GEN-LAST:event_jBEditActionPerformed

    private void jTContentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTContentMouseClicked
        
        if (evt.getClickCount() == 2) {
            /*
             JTable target = (JTable)evt.getSource();
             int row = target.getSelectedRow();
             int column = target.getSelectedColumn();
             */
            jBEditActionPerformed(null);
        }
    }//GEN-LAST:event_jTContentMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBDel;
    private javax.swing.JButton jBEdit;
    private javax.swing.JButton jBNew;
    private javax.swing.JButton jBSave;
    private javax.swing.JLabel jLInfo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTContent;
    private at.redeye.twelvelittlescoutsclerk.tableFilter tableFilter1;
    // End of variables declaration//GEN-END:variables

    private void newEntry() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException {
                       
        DBMember member = new DBMember();
        member.bp_idx.loadFromCopy(mainwin.getBPIdx());
        
        CreateMember create_kunde = new CreateMember(mainwin, this, member);
        
        invokeDialogModal(create_kunde);
        
        if( create_kunde.pressedSave() )
        {        
            mainwin.getAudit().openNewAudit();            
                                                                     
            tm.add(member, true, true);
            values.add(member);
            setEdited();
            save();
            feed_table();
            toFront();
        }        
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
