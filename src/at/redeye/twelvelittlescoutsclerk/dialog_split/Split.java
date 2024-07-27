/**
 * TwelveLittleScoutsClerk Dialog for Contact table
 * @author Copyright (c) 2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_split;

import at.redeye.FrameWork.base.BaseDialogDialog;
import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.tablemanipulator.TableManipulator;
import at.redeye.FrameWork.base.tablemanipulator.validators.DateValidator;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author martin
 */
public class Split extends BaseDialogDialog {

    ArrayList<DBBookingLine> lines = new ArrayList<>();
    TableManipulator tm;
    DBBookingLine line;
     
    /**
     * Creates new form Split
     */
    public Split( JFrame owner, Root root, DBBookingLine line )
    {
        super( owner, root, "Split booking line" );
        
        initComponents();
        
        this.line = line;
        
        init();
        
        add_new_line();
    }
    
    public Split( JFrame owner, Root root, DBBookingLine parent, List<DBBookingLine> childs )
    {
        super( owner, root, "Split booking line" );
        
        initComponents();
        
        this.line = parent;
        lines.addAll(childs);
        
        init();
        
        tm.addAll(lines);
    }    

    void init()
    {
        tm = new TableManipulator(root, jTContent, line);       

        tm.hide(line.hist.lo_user);
        tm.hide(line.hist.lo_zeit);        
        tm.hide(line.hist.ae_user);
        tm.hide(line.hist.an_user);
        tm.hide(line.hist.an_zeit);
        tm.hide(line.hist.ae_zeit);
        tm.hide(line.bp_idx);
        tm.hide(line.idx);
        tm.hide(line.line);
        tm.hide(line.reference);
        tm.hide(line.from_bank_account_bic);
        tm.hide(line.from_bank_account_iban);
        tm.hide(line.splitpos);
        tm.hide(line.contact_idx);
        tm.hide(line.data_source);
        tm.hide(line.parent_idx);
        tm.hide(line.assigned);
        
        tm.setEditable(line.amount);
        
        tm.prepareTable();           
        
        tm.autoResize();
        
        bindVar(jTAmount, line.amount);
        bindVar(jTReference, line.reference);
        
        var_to_gui();
    }
    
    public List<DBBookingLine> getLines()
    {
        return lines;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTContent = new javax.swing.JTable();
        jTReference = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTAmount = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jBNew = new javax.swing.JButton();
        jBDelete = new javax.swing.JButton();
        jBClose = new javax.swing.JButton();
        jBSave = new javax.swing.JButton();

        jButton1.setText("jButton1");

        jLabel1.setText("jLabel1");

        jTextField1.setText("jTextField1");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

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
        jScrollPane1.setViewportView(jTContent);

        jTReference.setEditable(false);

        jLabel2.setText("Amount:");

        jLabel3.setText("Reference:");

        jTAmount.setEditable(false);

        jBNew.setText("New");
        jBNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/bookmark.png"))); // NOI18N
        jBNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBNewActionPerformed(evt);
            }
        });

        jBDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/edittrash.gif"))); // NOI18N
        jBDelete.setText("Delete");
        jBDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBDeleteActionPerformed(evt);
            }
        });

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBNew)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jBClose)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 6, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBSave)
                    .addComponent(jBNew)
                    .addComponent(jBDelete)
                    .addComponent(jBClose)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTReference)
                    .addComponent(jTAmount))
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jBNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBNewActionPerformed
        
        add_new_line();
        
    }//GEN-LAST:event_jBNewActionPerformed

    private void jBCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCloseActionPerformed

        lines.clear();
        close();
        
    }//GEN-LAST:event_jBCloseActionPerformed

    private void jBDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBDeleteActionPerformed
        
        int row = tm.getSelectedRow();
        
        if( row < 0 ) {
            return;
        }
        
        lines.remove(row);
        tm.remove(row);
        
    }//GEN-LAST:event_jBDeleteActionPerformed

    private void jBSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBSaveActionPerformed
        close();
    }//GEN-LAST:event_jBSaveActionPerformed

    void add_new_line()
    {
        DBBookingLine new_line = new DBBookingLine();
        new_line.loadFromCopy(line);
        new_line.amount.loadFromCopy(0.0);
        new_line.idx.loadFromCopy(0);
        new_line.splitpos.loadFromCopy(0);
        
        lines.add(new_line);
        tm.add(new_line);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBDelete;
    private javax.swing.JButton jBNew;
    private javax.swing.JButton jBSave;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTAmount;
    private javax.swing.JTable jTContent;
    private javax.swing.JTextField jTReference;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
