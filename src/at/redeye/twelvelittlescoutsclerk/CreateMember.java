/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.BaseDialog;
import at.redeye.FrameWork.base.BaseDialogBase;
import at.redeye.FrameWork.base.BaseDialogDialog;
import at.redeye.FrameWork.base.bindtypes.DBDouble;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.FrameWork.widgets.documentfields.DocumentFieldDouble;
import at.redeye.FrameWork.widgets.documentfields.DocumentFieldLimit;
import at.redeye.SqlDBInterface.SqlDBIO.impl.DBDataType;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author martin
 */
public class CreateMember extends BaseDialogDialog implements NewSequenceValueInterface {

     DBMember member;
     boolean saved  = false;
     boolean wantTrainer = false;
     MainWin mainwin;
    
     
    /**
     * Creates new form CreateKunde
     */
    public CreateMember(MainWin mainwin, Member members, final DBMember member) {
        super( members.getRoot(), "Neuer Kunde");
        initComponents();
        
        this.member = member;      
        this.mainwin = mainwin;
        
        if( member.eintrittsdatum.getValue().getTime() < 10000 && 
            member.idx.getValue() == 0) {
            member.eintrittsdatum.loadFromCopy(new Date());
        }
        
        
        registerHelpWin(
                new Runnable() {

                    @Override
                    public void run() {
                        invokeDialogModal(new LocalHelpWinModal(root, "CreateKunde"));
                    }
                });     
                       
                            
        bindVar(jTKundennummer,kunde.kundennummer);
        bindVar(jTVorname,kunde.vorname);
        bindVar(jTName,kunde.name);
        bindVar(jCTermin,kunde.termin);
        bindVar(jDEintrittsdatum,kunde.eintrittsdatum);
        bindVar(jCPrimeEarnsCompany,kunde.prime_earns_company);
        bindVar(jTLifepartnerName,kunde.lifepartner_name);
        bindVar(jTLifepartnerForename,kunde.lifepartner_forename);
        bindVar(jTAnzahlung,kunde.anzahlung);
        bindVar(jTBereitsBezahlt,kunde.bereits_bezahlt);

        kunde.anzahlung.loadFromString(root.getSetup().getConfig(AppConfigDefinitions.Anzahlung));
        
        var_to_gui();
    }
    
    final public void bindVar( JTextField field, DBDouble var )
    {
        super.bindVar(field, var);
        field.setDocument(new DocumentFieldDoubleAndNoComma(7));
    }      
    
    final public void bindVar( JTextField field, DBString var )
    {
        super.bindVar(field, var);
        field.setDocument(new DocumentFieldLimit(var.getMaxLen()));
    }

    private void feedTermin() throws SQLException, UnsupportedDBDataTypeException 
    {
        Transaction trans = getTransaction();
        
        List<DBDataType> list = new ArrayList();
        list.add(DBDataType.DB_TYPE_STRING);
        
        List<List<?>> values = trans.fetchColumnValue("select distinct(" + trans.markColumn(kunde.termin) + ") from "  + trans.markTable(kunde), 
                             list );
        
        for( int i = 0; i < values.size(); i++ )
        {
            List<String> row = (List<String>) values.get(i);
            
            for( String item  : row )
            {
                jCTermin.addItem(item);
            }
        }
    }    

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTKundennummer = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTName = new javax.swing.JTextField();
        jTVorname = new javax.swing.JTextField();
        jDEintrittsdatum = new at.redeye.Plugins.JDatePicker.JDatePicker(root);
        jPanel1 = new javax.swing.JPanel();
        jBClose = new javax.swing.JButton();
        jBSave = new javax.swing.JButton();
        jBClose1 = new javax.swing.JButton();
        jBVorschau = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jCGebrachtVon = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jCPrimeEarnsCompany = new javax.swing.JCheckBox();
        jCTermin = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jTLifepartnerName = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTLifepartnerForename = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTAnzahlung = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTBereitsBezahlt = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(456, 336));

        jLabel1.setText("Kundennummer");

        jLabel2.setText("Name");

        jLabel3.setText("Eintrittsdatum");

        jLabel4.setText("Vorname");

        jDEintrittsdatum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDEintrittsdatumActionPerformed(evt);
            }
        });

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

        jBVorschau.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/history.png"))); // NOI18N
        jBVorschau.setText("Vorschau");
        jBVorschau.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBVorschauActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jBSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jBVorschau)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBClose1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBClose))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBSave)
                    .addComponent(jBClose)
                    .addComponent(jBClose1))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBVorschau)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel6.setText("Gebracht von");

        jLabel5.setText("Termin");

        jLabel7.setText("Ohne Provision");

        jCPrimeEarnsCompany.setText("Provisionen werden nicht ausbezahlt");
        jCPrimeEarnsCompany.setToolTipText("zB: bei LebenspartnerInnen");

        jCTermin.setEditable(true);

        jLabel8.setText("Lebenspartner Name");

        jTLifepartnerName.setToolTipText("Wird im System nicht mitgeführt, aber sobel dieses feld ausgefüllt ist bekommt der Trainer ebenso für den Lebenspartner eine Sonderprämie.");

        jLabel9.setText("Lebenspartner Vorname");

        jLabel10.setText("Anzahlung");

        jLabel11.setText("gleich bezahlt");

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
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel6))
                        .addGap(59, 59, 59)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addComponent(jTVorname, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addComponent(jDEintrittsdatum, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addComponent(jCGebrachtVon, 0, 367, Short.MAX_VALUE)
                            .addComponent(jTKundennummer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel5)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTLifepartnerForename, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addComponent(jCTermin, 0, 367, Short.MAX_VALUE)
                            .addComponent(jTLifepartnerName, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addComponent(jCPrimeEarnsCompany, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTAnzahlung, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTBereitsBezahlt, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)))))
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
                    .addComponent(jLabel4)
                    .addComponent(jTVorname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jDEintrittsdatum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCGebrachtVon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCTermin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jCPrimeEarnsCompany))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTLifepartnerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTLifepartnerForename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTAnzahlung, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jTBereitsBezahlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jDEintrittsdatumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDEintrittsdatumActionPerformed

    }//GEN-LAST:event_jDEintrittsdatumActionPerformed

    private void jBCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCloseActionPerformed

        close();
    }//GEN-LAST:event_jBCloseActionPerformed

    private boolean check() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {
        Transaction trans = getTransaction();
        
        if( kunde.kundennummer.isEmptyTrimmed() ) {
            JOptionPane.showMessageDialog(this, MlM("Die Kundennummer darf nicht leer sein"));
            jTKundennummer.requestFocus();
            return false;
        }
        
        List<DBKunden> kunden_list = trans.fetchTable2(kunde, "where " + trans.markColumn(kunde.az_idx) + "  = " + kunde.az_idx
                + " and " + trans.markColumn(kunde.kundennummer) + " = '" + kunde.kundennummer + "'" );
        
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

        if( kunde.vorname.isEmptyTrimmed() ) {
            JOptionPane.showMessageDialog(this, MlM("Bitte einen Vornamen eingeben"));
            jTName.requestFocus();
            return false;
        }        
        
        kunden_list = trans.fetchTable2(kunde, "where " + trans.markColumn(kunde.az_idx) + "  = " + kunde.az_idx
                + " and " + trans.markColumn(kunde.name) + " = '" + kunde.name + "' "
                + " and " + trans.markColumn(kunde.vorname) + " = '" + kunde.vorname + "' ");
        
        if( !kunden_list.isEmpty() ) {
            JOptionPane.showMessageDialog(this, MlM("Es existiert bereits ein Kunde mit diesem Namen"));
            jTName.requestFocus();
            return false;
        }                        

        if( mentor.idx.getValue() == 0 ) {
            
            if( JOptionPane.showConfirmDialog(this, MlM("Es wurde kein Mentor angegeben.\n Wollen Sie einen Trainer anlegen?"),
                    MlM("Kein Mentor wurde angeben."), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION )
            {                                
                JOptionPane.showMessageDialog(this, MlM("Bitte einen Mentor angeben"));
                jCGebrachtVon.requestFocus();
                return false;
            } else {
                wantTrainer = true;
            }
        }             
        
        if( kunde.anzahlung.getValue() > kunde.bereits_bezahlt.getValue() && 
            jCPrimeEarnsCompany.isSelected() == false )
        {
 
            int ret =  JOptionPane.showConfirmDialog(this, MlM("Die Anzahlung wurde nicht vollständig geleistet.\n"
                    + "Soll \"Provisionen werden nicht ausbezahlt\" wird gesetzt werden?"),
                    MlM("Anzahlung kleiner Bereits bezahlt"), JOptionPane.YES_NO_CANCEL_OPTION);
            
            if( ret == JOptionPane.CANCEL_OPTION ) {                                               
                jTBereitsBezahlt.requestFocus();
                return false;
            } else if( ret == JOptionPane.YES_OPTION ) {
                kunde.prime_earns_company.loadFromCopy(1);
            } 
        }
        
        return true;
    }
    
    /**
     * gibt den ausgewählten Mentor zurück
     * @return 
     */
    public DBKunden getMentor()
    {
        return mentor;
    }
    
    private void jBSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBSaveActionPerformed

        gui_to_var();

        new AutoMBox(CreateMember.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                if (check()) {
                    saved = true;
                    close();
                }
            }
        };
                

    }//GEN-LAST:event_jBSaveActionPerformed

    private void jBClose1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBClose1ActionPerformed
        callHelpWin();
    }//GEN-LAST:event_jBClose1ActionPerformed

    
    public DBBonus createNewKunde( Transaction trans,                                    
                                   DBKunden kunde,
                                   DBKunden mentor, 
                                   boolean wantTrainer ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        kunde.idx.loadFromCopy(getNewSequenceValue(DBBonus.KUNR_IDX_SEQUENCE));

        UpdateBonusSystem updaterBonus = new UpdateBonusSystem(root, trans, mainwin.getAZIdx(), mainwin.getAudit());

        DBBonus bonus = updaterBonus.insertKundenDatenIfNotExists(kunde);

        if (bonus == null) {
            throw new RuntimeException("Kunde bereits vorhanden");
        }

        if (!wantTrainer) {
            if (mentor == null) {
                throw new RuntimeException("Kein Mentor angegeben");
            }
        }

        kunde.hist.setAnHist(root.getUserName());
        trans.insertValues(kunde);

        if (!wantTrainer) {
            UpdateBonusSystem.addMentor2Bonus(bonus, mentor);
            UpdateBonusSystem.addGebrachtVon2Bonus(bonus, mentor);
        } else {
            bonus.dienstgrad.loadFromCopy(DBDienstgrad.DIENSTGRAD.Trainer.ordinal());
        }

        trans.updateValues(bonus);

        if (!wantTrainer) {
            updaterBonus.addKunde2Mentor(this, mentor, kunde);
        }

        updaterBonus.addExtraPrime2Trainer(this, kunde);
        updaterBonus.addAnzahlung4newKunde(this, kunde);

        return bonus;        
    }
    
    private void do_vorschau( Transaction trans ) throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        DBBonus bonus = createNewKunde ( trans , kunde, getMentor(), isTrainer() );
        invokeDialogModal(new PrimesCreatedByOneUser(root, mainwin, bonus, trans));       
    }
    
    private void jBVorschauActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBVorschauActionPerformed
        
        final Transaction trans = getTransaction();
        
        new AutoMBox(CreateMember.class.getName()) {
            
            @Override
            public void do_stuff() throws Exception {
                               
                gui_to_var();
                do_vorschau(trans);
                trans.rollback();
           
            }
        };
             
                                            
        new AutoMBox(CreateMember.class.getName()) {
            
            @Override
            public void do_stuff() throws Exception {
                                         
                trans.rollback();
           
            }
        };
        
    }//GEN-LAST:event_jBVorschauActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBClose1;
    private javax.swing.JButton jBSave;
    private javax.swing.JButton jBVorschau;
    private javax.swing.JComboBox jCGebrachtVon;
    private javax.swing.JCheckBox jCPrimeEarnsCompany;
    private javax.swing.JComboBox jCTermin;
    private at.redeye.Plugins.JDatePicker.JDatePicker jDEintrittsdatum;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTAnzahlung;
    private javax.swing.JTextField jTBereitsBezahlt;
    private javax.swing.JTextField jTKundennummer;
    private javax.swing.JTextField jTLifepartnerForename;
    private javax.swing.JTextField jTLifepartnerName;
    private javax.swing.JTextField jTName;
    private javax.swing.JTextField jTVorname;
    // End of variables declaration//GEN-END:variables

    boolean pressedSave() {
        return saved;
    }
    
    boolean isTrainer() {
        return wantTrainer;
    }    
}
