/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.BaseDialogDialog;
import at.redeye.FrameWork.base.Setup;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.FrameWork.utilities.StringUtils;
import at.redeye.FrameWork.widgets.documentfields.DocumentFieldLimit;
import at.redeye.Setup.dbexport.ExportDialog;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.CreateBP.BPNameWrapper;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillingPeriod;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author martin
 */
public class DeleteBP extends BaseDialogDialog implements NewSequenceValueInterface {
         
    
    private DBBillingPeriod az = new DBBillingPeriod();
    private MainWin mainwin;
    private String MESSAGE_REALLY_DELETE_AZ;
    
    /**
     * Creates new form CreateAZ
     */
    public DeleteBP(MainWin mainwin) {
        super( mainwin.getRoot(), "Abrechnungszeitraum" );
        initComponents();        
                
        this.mainwin = mainwin;               
        
        final Integer current_az = mainwin.getBPIdx();
        
        final Transaction trans = getTransaction();

        new AutoMBox(DeleteBP.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                List<DBBillingPeriod> jt = trans.fetchTable2(az, 
                        " where " + trans.markColumn(az.idx) + " != " + current_az + " order by " + trans.markColumn("hist_anzeit") + " desc");

                int preselect = -1;
                int count = 0;

                for( DBBillingPeriod j : jt )
                {
                    jCold.addItem(new CreateBP.BPNameWrapper(j));
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
        
        MESSAGE_REALLY_DELETE_AZ = MlM("Den Abrechnungszeitraum '%s' wirklich löschen?");
                
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

        jLabel2 = new javax.swing.JLabel();
        jCold = new javax.swing.JComboBox();
        jBDel = new javax.swing.JButton();
        jBClose = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel2.setText("zu löschender AZ");

        jBDel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/messagebox_critical.gif"))); // NOI18N
        jBDel.setText("Löschen");
        jBDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBDelActionPerformed(evt);
            }
        });

        jBClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/fileclose.gif"))); // NOI18N
        jBClose.setText("Abbrechen");
        jBClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCloseActionPerformed(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("Hinweis:\nDer aktuell im Hauptfenster ausgewählte Abrechnungszeitraum kann nicht gelöscht werden und steht daher hier nicht zur Auswahl.");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jBDel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                        .addComponent(jBClose))
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCold, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBDel)
                    .addComponent(jBClose, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    void deleteAZ4Bonus( int idx ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        Transaction trans = getTransaction();
        
       UpdateBP updateAz = new UpdateBP(trans, this );
       updateAz.deleteAZ(idx);
    }    
    
    boolean deleteAZ() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {        
        Object obj = jCold.getSelectedItem();
        
        if( obj == null ) {            
            return false;
        }
            
        az = ((BPNameWrapper) obj).az;        
     
        if( az.title.isEmptyTrimmed() )
        {
            JOptionPane.showMessageDialog(this,"Bitte wählen Sie einen Namen");
            jCold.requestFocus();
            return false;
        }        
        
        int ret = JOptionPane.showConfirmDialog(
                this,
                StringUtils.autoLineBreak( String.format(MESSAGE_REALLY_DELETE_AZ, az.title.getValue())),
                MlM("Abrechnugszeitraum löschen"), JOptionPane.OK_CANCEL_OPTION);

        if (ret != JOptionPane.OK_OPTION) {
            return false;
        }

        logger.error("User "
                + root.getUserName()
                + " will den Abrechnugszeitraum '" + az.title.getValue() + "' löschen und hat die erste Frage mit Ja beantwortet");        
                
        ExportDialog exporter = new ExportDialog(root, "Automatische Sicherung");  
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss"); 
        
        String file_name = "db_sich " + df.format(new Date(System.currentTimeMillis())) + ".zip";
                
        exporter.doExport(new File(Setup.getAppConfigFile(root.getAppName(), file_name)), false);        
        
        invokeDialogModal(exporter);
        
        if( exporter.wasSuccessful() )
        {
            deleteAZ4Bonus( az.idx.getValue() );
            return true;
        }
        
        return false;
    }
    
    private void jBDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBDelActionPerformed

        gui_to_var();                                      

        AutoMBox am = new AutoMBox(DeleteBP.class.getName()) {
            @Override
            public void do_stuff() throws Exception {

                if (deleteAZ()) {
                    getTransaction().commit();
                    close();
                    mainwin.updateAZList();
                }
            }
        };

        if (am.isFailed()) {
            try {
                getTransaction().rollback();
            } catch (SQLException ex) {
                logger.error(ex, ex);
            }
        }      
      
    }//GEN-LAST:event_jBDelActionPerformed

    private void jBCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCloseActionPerformed

        close();
    }//GEN-LAST:event_jBCloseActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBDel;
    private javax.swing.JComboBox jCold;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
