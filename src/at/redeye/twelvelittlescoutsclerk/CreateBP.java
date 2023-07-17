/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    
    static class AZNameWrapper
    {
        DBBillingPeriod az;
        
        public AZNameWrapper( DBBillingPeriod az )
        {
            this.az = az;
        }
        
        @Override
        public String toString()
        {
            if( az.locked.getValue() > 0 )
                return az.title.toString() + " X";
            
            return az.title.toString();
        }
    }              
    
    DBBillingPeriod az = new DBBillingPeriod();
    DBBillingPeriod az_other = new DBBillingPeriod();
    MainWin mainwin;
    /**
     * Creates new form CreateAZ
     */
    public CreateBP(MainWin mainwin) {
        super( mainwin.getRoot(), "Abrechnungszeitraum" );
        initComponents();        
                
        this.mainwin = mainwin;
        
        jTextArea1.setCaretPosition(0);
        
        final Transaction trans = getTransaction();

        new AutoMBox(CreateBP.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                List<DBBillingPeriod> jt = trans.fetchTable2(az, "order by " + trans.markColumn("hist_anzeit") + " desc");

                int preselect = -1;
                int count = 0;

                for( DBBillingPeriod j : jt )
                {
                    jCold.addItem(new AZNameWrapper(j));
                    if( j.idx.getValue().equals(az.idx.getValue()) )
                    {
                        preselect = count;
                    }
                    
                    count++;
                }

                if( preselect >= 0 )
                    jCold.setSelectedIndex(preselect);
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
        jTextArea1.setText("Es wird ein neuer Abrechnungzeitraum erstellt. Dabei werden alle Daten von einem vorhergehend Abrechnungszeitraum in den neuen kopiert. Somit ist es immer möglich den Zustand der Daten zu einem beliebigen anderen Zeitpunkt erneut einzusehen.\n\nWählen Sie einen Namen zb: \"2012 Jan\" und den vorhergehenden Abrechungszeitraum von dem die Daten kopiert werden sollen.\n");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel1.setText("Neuer Name:");

        jLabel2.setText("Vorhergehender Zeitraum:");

        jBSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/button_ok.gif"))); // NOI18N
        jBSave.setText("Speichern");
        jBSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBSaveActionPerformed(evt);
            }
        });

        jBClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/fileclose.gif"))); // NOI18N
        jBClose.setText("Schließen");
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

    void createNewAZ4Bonus( int idx ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        Transaction trans = getTransaction();
        
       UpdateBP updateAz = new UpdateBP(trans, this );
       updateAz.copyData2NewAZ(idx, az_other);
    }    
    
    void createNewAZ() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        if( az.title.isEmptyTrimmed() )
        {
            JOptionPane.showMessageDialog(this,"Bitte wählen Sie einen Namen");
            jTName.requestFocus();
            return;
        }
        
        Object obj = jCold.getSelectedItem();
        
        if( obj != null ) {            
            az_other = ((AZNameWrapper) obj).az;
        }
        
        az.hist.setAnHist(root.getUserName());
        
        az.idx.loadFromCopy( getNewSequenceValue(az.getName()) );
        
        getTransaction().insertValues(az);
                
        createNewAZ4Bonus( az.idx.getValue() );
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
