/**
 * TwelveLittleScoutsClerk Edit an entry in event table
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_event;

import at.redeye.FrameWork.base.AutoMBox;
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
import at.redeye.twelvelittlescoutsclerk.DocumentFieldDoubleAndNoComma;
import at.redeye.twelvelittlescoutsclerk.LocalHelpWinModal;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.MemberHelper;
import at.redeye.twelvelittlescoutsclerk.MemberSearch;
import at.redeye.twelvelittlescoutsclerk.NewSequenceValueInterface;
import at.redeye.twelvelittlescoutsclerk.UpdateEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEventMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.dialog_member.EditMember;
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
public class EditEvent extends BaseDialogDialog implements NewSequenceValueInterface {

    DBEvent event;
    DBEvent event_old;
    boolean saved = false;
    MainWin mainwin;
    boolean started = false;
    TableManipulator tm;
    List<DBEventMember> values = new ArrayList<>();
    List<DBEventMember> values_to_remove = new ArrayList<>();
    
    /**
     * Creates new form EditKunde
     */
    public EditEvent(MainWin mainwin, DBEvent event) 
    {
        super( mainwin.root,  event.name.getValue() );
        initComponents();
        
        
        registerHelpWin(
                new Runnable() {

                    @Override
                    public void run() {
                        invokeDialogModal(new LocalHelpWinModal(root, "EditEvent"));
                    }
                });          
        
        this.event = event;
        this.mainwin = mainwin;
        
        event_old = new DBEvent();
        event_old.loadFromCopy(event);
        
        bindVar(jTName, event.name);
        bindVar(jTCosts, event.costs);
               
        var_to_gui();
        
        started = true;
        
        if( mainwin.isAzLocked() ) {
            jBSave.setEnabled(false);
        }
        
        
        DBEventMember members = new DBEventMember();
        
        tm = new TableManipulator(root, jTMembers, members);
     
        tm.hide(members.hist.lo_user);
        tm.hide(members.hist.lo_zeit);        
        tm.hide(members.hist.ae_user);
        tm.hide(members.hist.an_user);
        tm.hide(members.hist.an_zeit);
        tm.hide(members.hist.ae_zeit);
        tm.hide(members.bp_idx);
        tm.hide(members.idx);
        tm.hide(members.event_idx);
        tm.hide(members.member_idx);
        tm.hide(members.group_idx);
        
        tm.setEditable(members.costs);
        tm.setEditable(members.comment);
        
        tm.prepareTable();
        
        feed_table(false);
        
        tm.autoResize();
        
        tableFilter1.setFilter(jTMembers);
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

                DBEventMember em = new DBEventMember();

                Transaction trans = getTransaction();
                values = trans.fetchTable2(em,
                        "where " + trans.markColumn(em.bp_idx) + " = " + mainwin.getBPIdx().toString()
                        + " and " + trans.markColumn(em.event_idx) + " = " + event.idx.toString()
                        + " order by " + trans.markColumn(em.name));
                
                for (DBEventMember entry : values) {
                    tm.add(entry);
                }
            }
        };
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
        
        if( event.name.isEmptyTrimmed() ) {
            JOptionPane.showMessageDialog(this, MlM("The event name is mandatory."));
            jTName.requestFocus();
            return false;
        }
        
        List<DBEvent> event_list = trans.fetchTable2(event, "where " + trans.markColumn(event.bp_idx) + "  = " + event.bp_idx
                + " and " + trans.markColumn(event.name) + " = '" + event.name + "'"
                + " and " + trans.markColumn(event.idx) + " != " + event.idx.toString() );
        
        if( !event_list.isEmpty() ) {
            JOptionPane.showMessageDialog(this, MlM("There exists already an event with this name."));
            jTName.requestFocus();
            return false;
        }
                
        return true;
    }
    
    public boolean pressedSave()
    {
        return saved;
    }
        
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jLabel2 = new javax.swing.JLabel();
        jTName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTCosts = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jBClose = new javax.swing.JButton();
        jBSave = new javax.swing.JButton();
        jBClose1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTMembers = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jBAddMember = new javax.swing.JButton();
        jBRemoveMember = new javax.swing.JButton();
        jBEditMember = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        tableFilter1 = new at.redeye.twelvelittlescoutsclerk.tableFilter();

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(548, 360));

        jLabel2.setText("Name");

        jLabel3.setText("Costs");

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
                .addContainerGap()
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
                    .addComponent(jBSave)
                    .addComponent(jBClose))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTMembers.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTMembers);

        jBAddMember.setText("add Member");
        jBAddMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBAddMemberActionPerformed(evt);
            }
        });

        jBRemoveMember.setText("remove Member");
        jBRemoveMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBRemoveMemberActionPerformed(evt);
            }
        });

        jBEditMember.setText("edit Member");
        jBEditMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBEditMemberActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBAddMember, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBRemoveMember, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                    .addComponent(jBEditMember, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBAddMember)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBRemoveMember)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBEditMember)
                .addContainerGap(193, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        jLabel1.setText("Members");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 40, Short.MAX_VALUE)
                .addComponent(tableFilter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(tableFilter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(133, 133, 133)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTCosts)
                            .addComponent(jTName))))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTCosts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        final EditEvent parent = this;
        
        new AutoMBox(EditEvent.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                if (check()) {                                                                                         
                    
                    Transaction trans = getTransaction();
                    
                    UpdateEvent updaterEvent = new UpdateEvent(root, getTransaction(), mainwin.getAudit());
                    updaterEvent.auditEventDiffAndUpdate(event_old, event);
                    
                    for( DBEventMember em : values_to_remove ) {
                        trans.deleteWithPrimaryKey(em);
                    }
                    
                    for( DBEventMember em : values ) {
                        DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(trans, em, em.hist, "root" );
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
        
        for( DBEventMember member : values ) {
            ret.add(member.member_idx.getValue());
        }
        
        return ret;
    }
    
    DBEventMember createEntry( DBMember member ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        Transaction trans = getTransaction();
        
        DBEventMember em = new DBEventMember();
        em.idx.loadFromCopy(mainwin.getNewSequenceValue(DBEventMember.EVENTMEMBER_IDX_SEQUENCE));        
        em.bp_idx.loadFromCopy(mainwin.getBPIdx());
        em.event_idx.loadFromCopy(event.idx.getValue());
        em.member_idx.loadFromCopy(member.idx.getValue());
        em.group_idx.loadFromCopy(MemberHelper.fetch_group_idx(trans,member));
        em.hist.setAnHist("root");
        em.costs.loadFromCopy(event.costs.getValue());
        em.name.loadFromCopy(member.name.getValue());
        em.forname.loadFromCopy(member.forname.getValue());
        em.group.loadFromCopy(member.group.getValue());       
        
        return em;
    }
    
    private void jBAddMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBAddMemberActionPerformed
        
        MemberSearch ms = new MemberSearch( mainwin, "Add a new member");
        Set<Integer> ids = getMemberIds();
               
        ms.addFilter( new MemberSearch.Filter() {
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
        
        new AutoMBox(EditEvent.class.getName()) {

            @Override
            public void do_stuff() throws Exception {

                for (DBMember member : selected_members) {

                    DBEventMember entry = createEntry(member);

                    values.add(entry);
                    tm.add(entry);
                }
            }
        };
        
    }//GEN-LAST:event_jBAddMemberActionPerformed

    private void jBRemoveMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBRemoveMemberActionPerformed
        
        Set<Integer> rows = tm.getSelectedRows();
        
        if( rows == null ) {
            return;
        }
        
        List<Integer> rows_up_to_down = new ArrayList<>(rows);
        Collections.sort(rows_up_to_down, Collections.reverseOrder());
        
        for( int row : rows_up_to_down ) {
            values_to_remove.add(values.get(row));
            
            tm.remove(row);
            values.remove(row);            
        }
        
    }//GEN-LAST:event_jBRemoveMemberActionPerformed

    private void jBEditMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBEditMemberActionPerformed
                
        new AutoMBox(this.getClass().getCanonicalName()) {
            @Override
            public void do_stuff() throws Exception {
                Set<Integer> rows = tm.getSelectedRows();

                if( rows == null ) {
                    return;
                }

                for( int row : rows ) {            
                    DBEventMember em = values.get(row);
                    DBMember member = new DBMember();
                    member.idx.loadFromCopy(em.member_idx.getValue());

                    getTransaction().fetchTableWithPrimkey(member);
                    var dialog = new EditMember( mainwin, member );
                    mainwin.invokeDialogModal(dialog);
                    if( dialog.pressedSave() ) {
                        
                    }
                }                
            }
        };
               
    }//GEN-LAST:event_jBEditMemberActionPerformed

  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBAddMember;
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBClose1;
    private javax.swing.JButton jBEditMember;
    private javax.swing.JButton jBRemoveMember;
    private javax.swing.JButton jBSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTCosts;
    private javax.swing.JTable jTMembers;
    private javax.swing.JTextField jTName;
    private at.redeye.twelvelittlescoutsclerk.tableFilter tableFilter1;
    // End of variables declaration//GEN-END:variables


}
