/**
 * TwelveLittleScoutsClerk Edit an entry in event table
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_event;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.BaseDialog;
import at.redeye.FrameWork.base.UniqueDialogHelper;
import at.redeye.FrameWork.base.bindtypes.DBDouble;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.FrameWork.widgets.documentfields.DocumentFieldLimit;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.DocumentFieldDoubleAndNoComma;
import at.redeye.twelvelittlescoutsclerk.LocalHelpWinModal;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.NewSequenceValueInterface;
import at.redeye.twelvelittlescoutsclerk.UpdateEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author martin
 */
public class EditEvent extends BaseDialog implements NewSequenceValueInterface {

    DBEvent event;
    DBEvent event_old;
    boolean saved = false;
    MainWin mainwin;
    boolean started = false;
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

        jLabel2 = new javax.swing.JLabel();
        jTName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTCosts = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jBClose = new javax.swing.JButton();
        jBSave = new javax.swing.JButton();
        jBClose1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(548, 360));

        jLabel2.setText("Name");

        jLabel3.setText("Costs");

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 362, Short.MAX_VALUE)
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 247, Short.MAX_VALUE)
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
                    
                    UpdateEvent updaterEvent = new UpdateEvent(root, getTransaction(), mainwin.getAudit());
                    updaterEvent.auditEventDiffAndUpdate(event_old, event);
                    
                    getTransaction().commit();
                    
                    saved = true;
                    close();
                }
            }
        };

    }//GEN-LAST:event_jBSaveActionPerformed

    private void jBClose1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBClose1ActionPerformed
        callHelpWin();
    }//GEN-LAST:event_jBClose1ActionPerformed

  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBClose1;
    private javax.swing.JButton jBSave;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTCosts;
    private javax.swing.JTextField jTName;
    // End of variables declaration//GEN-END:variables


}