/**
 * TwelveLittleScoutsClerk Member search dialog
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.BaseDialogDialog;
import at.redeye.FrameWork.base.tablemanipulator.TableManipulator;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author martin
 */
public class MemberSearch extends BaseDialogDialog  {

    MainWin mainwin;
    List<DBMember> values;
    TableManipulator tm;
    boolean abort = false;
    /**
     * Creates new form MemberSearch
     */
    public MemberSearch( MainWin mainwin, String title )
    {
        super( mainwin.getRoot(), title );               
        
        initComponents();
        
        this.mainwin = mainwin;
        
        DBMember members = new DBMember();
        
        tm = new TableManipulator(root, jTMember, members);
     
        tm.hide(members.hist.lo_user);
        tm.hide(members.hist.lo_zeit);        
        tm.hide(members.hist.ae_user);
        tm.hide(members.hist.an_user);
        tm.hide(members.hist.an_zeit);
        tm.hide(members.hist.ae_zeit);
        tm.hide(members.bp_idx);
        tm.hide(members.idx);
        
        tm.prepareTable();
        
        feed_table(false);
        
        tm.autoResize();
        
        tableFilter1.setFilter(jTMember);
    }
    
    private void feed_table() {
        feed_table(true);
    }

    private void feed_table(boolean autombox) {
        new AutoMBox(getTitle(), autombox) {            
            
            @Override
            public void do_stuff() throws Exception {
                
                tm.clear();
                
                Transaction trans = getTransaction();
                values = MemberHelper.fetch_members( trans, mainwin.getBPIdx() );               
                
                for (DBMember entry : values) {
                    tm.add(entry);
                }                

            }
        };
    }
    
    /**     
     * @return null if nothing was selected
     */
    public List<DBMember> getSelectedEntries()
    {
        if( abort ) {
            return null;
        }
        
        Set<Integer> rows = tm.getSelectedRows();
        
        if( rows == null ) {
            return null;
        }
        
        List<DBMember> selected_members = new ArrayList<DBMember>();
        
        for( int row : rows ) {
            selected_members.add(values.get(row));
        }
        
        return selected_members;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        tableFilter1 = new at.redeye.twelvelittlescoutsclerk.tableFilter();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTMember = new javax.swing.JTable();
        jBClose = new javax.swing.JButton();
        jBAccept = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        jLabel1.setText("Member search");

        jTMember.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTMember);

        jBClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/button_cancel.gif"))); // NOI18N
        jBClose.setText("Abort");
        jBClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCloseActionPerformed(evt);
            }
        });

        jBAccept.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/button_ok.gif"))); // NOI18N
        jBAccept.setText("Ok");
        jBAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBAcceptActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jBAccept)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jBClose))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                        .addComponent(tableFilter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tableFilter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jBClose)
                    .addComponent(jBAccept))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCloseActionPerformed
        
        abort = true;
        close();
        
    }//GEN-LAST:event_jBCloseActionPerformed

    private void jBAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBAcceptActionPerformed
        
        close();
        
    }//GEN-LAST:event_jBAcceptActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBAccept;
    private javax.swing.JButton jBClose;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTMember;
    private at.redeye.twelvelittlescoutsclerk.tableFilter tableFilter1;
    // End of variables declaration//GEN-END:variables
}
