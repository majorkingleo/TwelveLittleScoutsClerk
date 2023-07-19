/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

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
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author martin
 */
public class EditMember extends BaseDialog implements NewSequenceValueInterface {

    DBMember kunde;
    DBMember kunde_old;
    boolean saved = false;
    MainWin mainwin;
    boolean started = false;
    /**
     * Creates new form EditKunde
     */
    public EditMember(MainWin mainwin, DBMember kunde) 
    {
        super( mainwin.root,  getTitle( kunde ));
        initComponents();
        
        
        registerHelpWin(
                new Runnable() {

                    @Override
                    public void run() {
                        invokeDialogModal(new LocalHelpWinModal(root, "EditKunde"));
                    }
                });          
        
        this.kunde = kunde;
        this.mainwin = mainwin;
        
        kunde_old = new DBMember();
        kunde_old.loadFromCopy(kunde);
        
        bindVar(jTKundennummer, kunde.member_registration_number);
        bindVar(jTName, kunde.name);
        bindVar(jTVorname, kunde.forname);
        bindVar(jDEintrittsdatum, kunde.entry_date);
        bindVar(jTtel,kunde.tel);
        bindVar(jCinaktiv,kunde.inaktiv);
        bindVar(jCgekuendigt,kunde.de_registered);
               
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
    
    static String getTitle( DBMember kunde )
    {
        return MemberNameCombo.getName4Kunde(kunde);
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
        
        if( kunde.member_registration_number.isEmptyTrimmed() ) {
            JOptionPane.showMessageDialog(this, MlM("Die Kundennummer darf nicht leer sein"));
            jTKundennummer.requestFocus();
            return false;
        }
        
        List<DBMember> kunden_list = trans.fetchTable2(kunde, "where " + trans.markColumn(kunde.bp_idx) + "  = " + kunde.bp_idx
                + " and " + trans.markColumn(kunde.member_registration_number) + " = '" + kunde.member_registration_number + "'" 
                + " and " + trans.markColumn(kunde.idx) + " != " + kunde.idx.getValue());
        
        if( !kunden_list.isEmpty() ) {
            JOptionPane.showMessageDialog(this, MlM("Diese Kundennummer ist bereits vorhanden"));
            jTKundennummer.requestFocus();
            return false;
        }
        
        if( kunde.name.isEmptyTrimmed() ) {
            JOptionPane.showMessageDialog(this, MlM("Bitte einen Namen eingeben"));
            jTName.requestFocus();
            return false;
        }

        if( kunde.forname.isEmptyTrimmed() ) {
            JOptionPane.showMessageDialog(this, MlM("Bitte einen Vornamen eingeben"));
            jTName.requestFocus();
            return false;
        }        
        
        kunden_list = trans.fetchTable2(kunde, "where " + trans.markColumn(kunde.bp_idx) + "  = " + kunde.bp_idx
                + " and " + trans.markColumn(kunde.name) + " = '" + kunde.name + "' "
                + " and " + trans.markColumn(kunde.forname) + " = '" + kunde.forname + "' "
                + " and " + trans.markColumn(kunde.idx) + " != " + kunde.idx.getValue());
        
        if( !kunden_list.isEmpty() ) {
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(548, 360));

        jLabel1.setText("Kundennummer");

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
                .addContainerGap(50, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel13))
                        .addGap(63, 63, 63)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTName, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTVorname, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jDEintrittsdatum, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTKundennummer)
                            .addComponent(jTtel)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(105, 105, 105)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                    .addComponent(jLabel6)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
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
                    
                    UpdateMember updaterKunden = new UpdateMember(root, getTransaction(), mainwin.getAudit());
                    updaterKunden.auditKundenDiffAndUpdate(kunde_old, kunde);                                        
                    
                    getTransaction().commit();
                    
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

  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBClose1;
    private javax.swing.JButton jBSave;
    private javax.swing.JCheckBox jCgekuendigt;
    private javax.swing.JCheckBox jCinaktiv;
    private at.redeye.Plugins.JDatePicker.JDatePicker jDEintrittsdatum;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTKundennummer;
    private javax.swing.JTextField jTName;
    private javax.swing.JTextField jTVorname;
    private javax.swing.JTextField jTtel;
    // End of variables declaration//GEN-END:variables


}
