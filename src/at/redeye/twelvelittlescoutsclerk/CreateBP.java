/**
 * TwelveLittleScoutsClerk common functions on table Member 
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.BaseDialogDialog;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.FrameWork.widgets.documentfields.DocumentFieldLimit;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillingPeriod;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author martin
 */
public class CreateBP extends BaseDialogDialog implements NewSequenceValueInterface {
    
    static class BPNameWrapper
    {
        DBBillingPeriod az = null;
        
        public BPNameWrapper()
        {
            
        }
        
        public BPNameWrapper( DBBillingPeriod az )
        {
            this.az = az;
        }
        
        @Override
        public String toString()
        {
            if( az == null ) {
                return "";
            }
            
            if( az.locked.getValue() > 0 )
                return az.title.toString() + " X";
            
            return az.title.toString();
        }
    }              
    
    DBBillingPeriod az = new DBBillingPeriod();
    DBBillingPeriod bp_other = new DBBillingPeriod();
    MainWin mainwin;
    private String MESSAGE_PLEASE_SELECT_NAME;
    /**
     * Creates new form CreateAZ
     */
    public CreateBP(MainWin mainwin) {
        super( mainwin.getRoot(), "Billing Period" );
        initComponents();
        initMessages();
        
        this.mainwin = mainwin;
        
        jTextArea1.setCaretPosition(0);
        
        final Transaction trans = getTransaction();

        new AutoMBox(CreateBP.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                List<DBBillingPeriod> jt = trans.fetchTable2(az, "order by " + trans.markColumn("hist_anzeit") + " desc");

                int preselect = -1;
                int count = 0;
                
                // add empty one first
                jCold.addItem(new BPNameWrapper());
                
                for( DBBillingPeriod j : jt )
                {
                    jCold.addItem(new BPNameWrapper(j));
                    if( j.idx.getValue().equals(az.idx.getValue()) )
                    {
                        preselect = count;
                    }
                    
                    count++;
                }

                if( preselect >= 0 ) {
                    jCold.setSelectedIndex(preselect);
                }
            }
        };           
        
        bindVar(jTName, az.title);
                
        var_to_gui();
    }
    
    final public void bindVar( JTextField field, DBString var )
    {
        super.bindVar(field, var);
        field.setDocument(new DocumentFieldLimit(var.getMaxLen()));
    }
    
        

    @Override
    public boolean openWithLastWidthAndHeight() {
        return false;
    }

    private void initMessages() {
        MESSAGE_PLEASE_SELECT_NAME = MlM("Please select a name.");
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jTName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jCold = new javax.swing.JComboBox();
        jBSave = new javax.swing.JButton();
        jBClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("A new billing period will be created. All data from a previous billing period will be copied into the new one. This makes it always possible to review the state of the data at any previous point in time.\n\nChoose a name, e.g. \"2012 Jan\", and the previous billing period from which the data should be copied.\n");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel1.setText("New Name:");

        jLabel2.setText("Previous Period:");

        jBSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/button_ok.gif"))); // NOI18N
        jBSave.setText("Save");
        jBSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBSaveActionPerformed(evt);
            }
        });

        jBClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/fileclose.gif"))); // NOI18N
        jBClose.setText("Close");
        jBClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCloseActionPerformed(evt);
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
                        .addComponent(jLabel1)
                        .addGap(111, 111, 111)
                        .addComponent(jTName, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCold, 0, 224, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jBSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 152, Short.MAX_VALUE)
                        .addComponent(jBClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jCold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBSave)
                    .addComponent(jBClose))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    void copyDate2NewBp( int idx ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        Transaction trans = getTransaction();
        
       UpdateBP updateBP = new UpdateBP(trans, this );
       
       // no copy selected
       if( bp_other != null ) {
        updateBP.copyData2NewAZ(idx, bp_other);
       }
    }    
    
    void createNewAZ() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        if( az.title.isEmptyTrimmed() )
        {
            JOptionPane.showMessageDialog(this, MESSAGE_PLEASE_SELECT_NAME);
            jTName.requestFocus();
            return;
        }
        
        Object obj = jCold.getSelectedItem();
        
        if( obj != null ) {            
            bp_other = ((BPNameWrapper) obj).az;
        }
        
        az.hist.setAnHist(root.getUserName());
        
        az.idx.loadFromCopy( getNewSequenceValue(az.getName()) );
        
        getTransaction().insertValues(az);
                
        copyDate2NewBp( az.idx.getValue() );
    }
    
    private void jBSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBSaveActionPerformed

        gui_to_var();                               
        
        new AutoMBox(CreateBP.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                
                createNewAZ();
                
                getTransaction().commit();      
                close();
                mainwin.changeAZ(az);
            }
        };
        
        try {
            getTransaction().rollback();
        } catch( Exception ex ) {
            logger.error(ex,ex);
        }
      
    }//GEN-LAST:event_jBSaveActionPerformed

    private void jBCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCloseActionPerformed

        close();
    }//GEN-LAST:event_jBCloseActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBSave;
    private javax.swing.JComboBox jCold;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTName;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
