/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.Plugin.AboutPlugins;
import at.redeye.FrameWork.base.*;
import at.redeye.FrameWork.base.bindtypes.DBString;
import at.redeye.FrameWork.base.prm.impl.gui.GlobalConfig;
import at.redeye.FrameWork.base.prm.impl.gui.LocalConfig;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.FrameWork.utilities.StringUtils;
import at.redeye.FrameWork.widgets.documentfields.DocumentFieldLimit;
import at.redeye.Setup.dbexport.ExportDialog;
import at.redeye.Setup.dbexport.ImportDialog;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillingPeriod;
import at.redeye.twelvelittlescoutsclerk.imports.ImportMemberFromScoreg;
import at.redeye.twelvelittlescoutsclerk.reports.audit.ReportAudit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 *
 * @author martin
 */
public class MainWin extends BaseDialog implements MainWinInterface {

    private static final String CONFIG_LAST_AZ = "last_bp";
    
    private String MESSAGE_REALLY_IMPORT_DATABASE;
    private String MESSAGE_REALLY_REALLY_IMPORT_DATABASE;    
    private String MESSAGE_REALLY_RESET_DATABASE;    
    private String MESSAGE_REALLY_REALLY_RESET_DATABASE;    
    
    Main main;
    String last_path;
    DBBillingPeriod bp = new DBBillingPeriod();
    boolean started = false;    
    Audit audit;
    /**
     * Creates new form MainWin
     */
    public MainWin(Main main, Root root) {
        super(root, root.getAppTitle());
        initComponents();       
        initStyle();
        
        this.main = main;
        audit = new Audit(this);
        
        initMessages();
        
        last_path = root.getSetup().getLocalConfig("LastPath", "");
        final String last_az = root.getSetup().getLocalConfig(CONFIG_LAST_AZ,"0");
        
        new AutoLogger(MainWin.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                
                bp.idx.loadFromString(last_az);
                
                if( getTransaction().fetchTableWithPrimkey(bp) )                
                    changeAZ(bp, false);
                else {
                    bp.idx.loadFromCopy(1);
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
                    cal.setTime(new Date(System.currentTimeMillis()));
                    int year = cal.get(Calendar.YEAR);                    
                    bp.title.loadFromCopy(String.format("%d",year));
                    // damit die Combobox befüllt ist
                    changeAZ(bp, false, true);
                }
            }
        };
        
        bindVar(jTComment,bp.comment);
        
        var_to_gui();
        
        started = true;
        /*
        AutoMBox ab = new AutoMBox(MainWin.class.getName()) {

                     @Override
                     public void do_stuff() throws Exception {                         
                         {
                             FixPrimeVonBug fix_bug = new FixPrimeVonBug(getTransaction());

                             fix_bug.run();

                             getTransaction().commit();
                         }

                         {
                             FixPrimeGebrachtVon fix_bug = new FixPrimeGebrachtVon(getTransaction());

                             fix_bug.run();

                             getTransaction().commit();
                         }                                                  
                     }
                 };
        
        if( ab.isFailed() ) {
            try {
                getTransaction().rollback();
            } catch( Exception ex ) {
                logger.error(ex,ex);
            }
        }        
        */
    }

    private void initMessages() {
        if (MESSAGE_REALLY_IMPORT_DATABASE != null) {
            return;
        }

        MESSAGE_REALLY_IMPORT_DATABASE = MlM("Wollen Sie tasächlich eine andere Datenbank importieren und die existierende Löschen?");
        MESSAGE_REALLY_REALLY_IMPORT_DATABASE = MlM("Die existierende Datenbank wird tatsächlich gelöscht! Wollen Sie trotzdem weitermachen?");
        MESSAGE_REALLY_RESET_DATABASE = MlM("Wollen Sie tasächlich die Datenbank in den Ursprungszustand zurücksetztn und dabei alle Daten,"
                + "die sie nicht exportiert haben verlieren?");
        MESSAGE_REALLY_REALLY_RESET_DATABASE = MlM("Sind Sie sicher?");        
    }    
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupDesign = new javax.swing.ButtonGroup();
        jMenuItem4 = new javax.swing.JMenuItem();
        jCAZ = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jBNew = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTComment = new javax.swing.JTextArea();
        jBChange = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMDatabase = new javax.swing.JMenuItem();
        jMDBExport = new javax.swing.JMenuItem();
        jMDBImport = new javax.swing.JMenuItem();
        jMResetDB = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMScoregMemberImport = new javax.swing.JMenuItem();
        jMQuit = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItemMembers = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jRMetal = new javax.swing.JRadioButtonMenuItem();
        jRSystem = new javax.swing.JRadioButtonMenuItem();
        jRMotif = new javax.swing.JRadioButtonMenuItem();
        jRNimbus = new javax.swing.JRadioButtonMenuItem();
        jMSettings = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMAbout = new javax.swing.JMenuItem();
        jMChangeLog = new javax.swing.JMenuItem();
        jMPlugin = new javax.swing.JMenuItem();

        jMenuItem4.setText("jMenuItem4");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jCAZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCAZActionPerformed(evt);
            }
        });

        jLabel1.setText("Abrechnungszeitraum");

        jBNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/bookmark.png"))); // NOI18N
        jBNew.setToolTipText("Neuer Abrechnungszeitraum");
        jBNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBNewActionPerformed(evt);
            }
        });

        jTComment.setColumns(20);
        jTComment.setRows(5);
        jScrollPane1.setViewportView(jTComment);

        jBChange.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/edit.png"))); // NOI18N
        jBChange.setToolTipText("Neuer Abrechnungszeitraum");
        jBChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBChangeActionPerformed(evt);
            }
        });

        jMenu1.setText("Programm");
        jMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu1ActionPerformed(evt);
            }
        });

        jMDatabase.setText("Datenbankverbindung");
        jMDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMDatabaseActionPerformed(evt);
            }
        });
        jMenu1.add(jMDatabase);

        jMDBExport.setText("Datenbank exportieren");
        jMDBExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMDBExportActionPerformed(evt);
            }
        });
        jMenu1.add(jMDBExport);

        jMDBImport.setText("Datenbank importieren");
        jMDBImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMDBImportActionPerformed(evt);
            }
        });
        jMenu1.add(jMDBImport);

        jMResetDB.setText("Datenbank zurücksetzten");
        jMResetDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMResetDBActionPerformed(evt);
            }
        });
        jMenu1.add(jMResetDB);
        jMenu1.add(jSeparator1);

        jMenuItem12.setText("Abrechnungszeitraum löschen");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem12);

        jMScoregMemberImport.setText("Mitgliederdaten von Scoreg importieren");
        jMScoregMemberImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMScoregMemberImportActionPerformed(evt);
            }
        });
        jMenu1.add(jMScoregMemberImport);

        jMQuit.setText("Beenden");
        jMQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMQuitActionPerformed(evt);
            }
        });
        jMenu1.add(jMQuit);

        jMenuBar1.add(jMenu1);

        jMenu5.setText("Daten");

        jMenuItemMembers.setText("Mitglieder");
        jMenuItemMembers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMembersActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItemMembers);
        jMenu5.add(jSeparator3);

        jMenuBar1.add(jMenu5);

        jMenu6.setText("Berichte");

        jMenuItem7.setText("Audit");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem7);

        jMenuBar1.add(jMenu6);

        jMenu4.setText("Einstellungen");

        jMenu3.setText("Design");

        jRMetal.setSelected(true);
        jRMetal.setText("Metal");
        jMenu3.add(jRMetal);

        jRSystem.setSelected(true);
        jRSystem.setText("System");
        jMenu3.add(jRSystem);

        jRMotif.setSelected(true);
        jRMotif.setText("Motif");
        jRMotif.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRMotifActionPerformed(evt);
            }
        });
        jMenu3.add(jRMotif);

        jRNimbus.setSelected(true);
        jRNimbus.setText("Nimbus");
        jMenu3.add(jRNimbus);

        jMenu4.add(jMenu3);

        jMSettings.setText("Einstellungen");
        jMSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMSettingsActionPerformed(evt);
            }
        });
        jMenu4.add(jMSettings);

        jMenuItem1.setText("Globale Einstellungen");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem1);

        jMenuBar1.add(jMenu4);

        jMenu2.setText("Info");

        jMAbout.setText("Über");
        jMAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMAboutActionPerformed(evt);
            }
        });
        jMenu2.add(jMAbout);

        jMChangeLog.setText("Änderungsprotokoll");
        jMChangeLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMChangeLogActionPerformed(evt);
            }
        });
        jMenu2.add(jMChangeLog);

        jMPlugin.setText("Plugins");
        jMPlugin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMPluginActionPerformed(evt);
            }
        });
        jMenu2.add(jMPlugin);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCAZ, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jBChange)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCAZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addComponent(jBNew)
                    .addComponent(jBChange))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMDatabaseActionPerformed

        invokeDialogUnique(new ConnectionDialog(root));
    }//GEN-LAST:event_jMDatabaseActionPerformed

    private void jMDBExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMDBExportActionPerformed

        ExportDialog exporter = new ExportDialog(root);

        invokeDialogUnique(exporter);

        exporter.doExport();
    }//GEN-LAST:event_jMDBExportActionPerformed

    private void jMDBImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMDBImportActionPerformed

        int ret = JOptionPane.showConfirmDialog(
                this,
                StringUtils.autoLineBreak(MESSAGE_REALLY_IMPORT_DATABASE),
                MlM("Datenbankimport"), JOptionPane.OK_CANCEL_OPTION);

        if (ret != JOptionPane.OK_OPTION) {
            return;
        }

        logger.error("User "
                + root.getUserName()
                + " will einen Datenbankimport Starten und hat die erste Frage mit Ja beantwortet");

        ret = JOptionPane.showConfirmDialog(
                this,
                StringUtils.autoLineBreak(MESSAGE_REALLY_REALLY_IMPORT_DATABASE),
                "Datenbankimport", JOptionPane.OK_CANCEL_OPTION);

        logger.error("User "
                + root.getUserName()
                + " will einen Datenbankimport Starten und hat die zweite Frage auch mit Ja beantwortet");

        if (ret != JOptionPane.OK_OPTION) {
            return;
        }

        ImportDialog importer = new ImportDialog(root);

        importer.setFinishedListener(new Runnable() {

            @Override
            public void run() {
                main.reopen();
            }
        });

        invokeDialogModal(importer);
    }//GEN-LAST:event_jMDBImportActionPerformed

    private void jMQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMQuitActionPerformed

        close();
    }//GEN-LAST:event_jMQuitActionPerformed

    private void jMAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMAboutActionPerformed
        invokeDialogUnique(new About(root));
    }//GEN-LAST:event_jMAboutActionPerformed

    private void jMChangeLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMChangeLogActionPerformed

        invokeDialogUnique(new LocalHelpWin(root, "ChangeLog"));
    }//GEN-LAST:event_jMChangeLogActionPerformed

    private void jMPluginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMPluginActionPerformed

        invokeDialogUnique(new AboutPlugins(root));
    }//GEN-LAST:event_jMPluginActionPerformed

    private void jRMotifActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRMotifActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRMotifActionPerformed

    private void jMSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMSettingsActionPerformed
        invokeDialogUnique(new LocalConfig(root));
    }//GEN-LAST:event_jMSettingsActionPerformed

    private void jBNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBNewActionPerformed
               
        invokeDialogModal(new CreateBP(this));
        
    }//GEN-LAST:event_jBNewActionPerformed

    void saveCurrentAZ() throws SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException, IOException
    {
        gui_to_var();

        if( bp.idx.getValue() > 0 ) {
            
            DBBillingPeriod other = new DBBillingPeriod();
            other.loadFromCopy(bp);
            getTransaction().fetchTableWithPrimkey(other);
            
            if (!other.comment.getValue().equals(bp.comment.getValue()))
            {
                bp.hist.setAeHist(root.getUserName());
                getTransaction().updateValues(bp);      
                getTransaction().commit();
            }
        }
    }
    
    private void jCAZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCAZActionPerformed
        
        if (started) {
            logger.debug(evt.getActionCommand());

            final MainWin mainwin = this;

            new AutoMBox(MainWin.class.getName()) {

                @Override
                public void do_stuff() throws Exception {

                    root.closeAllWindowsExceptThisOne(mainwin);

                    CreateBP.BPNameWrapper wrapper = (CreateBP.BPNameWrapper) jCAZ.getSelectedItem();

                    if (wrapper != null) {
                       changeAZ(wrapper.az);                        
                    }
                }
            };
        }
        
    }//GEN-LAST:event_jCAZActionPerformed

    private void jMenuItemMembersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMembersActionPerformed
       
        if( checkAz() )
            invokeDialogUnique(new Member(this));
       
    }//GEN-LAST:event_jMenuItemMembersActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        
        invokeDialogUnique(new GlobalConfig(root));
        
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMResetDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMResetDBActionPerformed
        
        int ret = JOptionPane.showConfirmDialog(
                this,
                StringUtils.autoLineBreak(MESSAGE_REALLY_RESET_DATABASE),
                MlM("Datenbank zurücksetzten"), JOptionPane.OK_CANCEL_OPTION);

        if (ret != JOptionPane.OK_OPTION) {
            return;
        }

        logger.error("User "
                + root.getUserName()
                + " will die Datenbank zurücksetzten und hat die erste Frage mit Ja beantwortet");        

        ret = JOptionPane.showConfirmDialog(
                this,
                StringUtils.autoLineBreak(MESSAGE_REALLY_REALLY_RESET_DATABASE),
                MlM("Datenbank zurücksetzten"), JOptionPane.OK_CANCEL_OPTION);

        if (ret != JOptionPane.OK_OPTION) {
            return;
        }        
        
        logger.error("User "
                + root.getUserName()
                + " will die Datenbank zurücksetzten und hat die zweite Frage mit Ja beantwortet");          
        
        final MainWin mainwin = this;
        
        new AutoMBox(MainWin.class.getName()) {

            @Override
            public void do_stuff() throws Exception {                                
                
                root.closeAllWindowsExceptThisOne(mainwin);
                
                Collection<String> tables = root.getDBManager().getTables();
                
                ArrayList<String> tables_to_drop = new ArrayList();
                
                for (String table : tables ) 
                {
                    if( root.getDBManager().tableExists(table) )
                    {
                        tables_to_drop.add(table);                        
                    }
                }
                
                
                root.getDBConnection().close();
                Transaction trans = root.getDBConnection().getNewTransaction();                
                
                for (String table : tables_to_drop) 
                {
                    logger.debug("dropping table " + table);
                    trans.updateValues("drop table " + getTransaction().markTable(table));
                }
                
                trans.commit();
                
                root.appExit();
            }
        };
                
        
    }//GEN-LAST:event_jMResetDBActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        invokeDialogUnique(new ReportAudit(this));
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jBChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBChangeActionPerformed
                
        gui_to_var();               
        /*
        final String newName = (String) JOptionPane.showInputDialog(this,
                "Neuer Name",
                "Neuer Name",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                az.title.getValue());

        new AutoMBox(MainWin.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                if (newName != null && !newName.trim().isEmpty()) {
                    az.title.loadFromCopy(newName);
                                        
                    DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(getTransaction(), az, az.hist, root.getUserName());
                    
                    getTransaction().commit();
                    
                    DBAZ az_new = new DBAZ();
                    az_new.loadFromCopy(az);
                    
                    started = false;
                    changeAZ(az_new, false);                                        
                    started = true;
                }
            }
        };
        */
        BPEdit az_edit = new BPEdit(root, this, bp);
        
        invokeDialogModal(az_edit);          
        
        if (az_edit.hasSomethingChanged()) 
        {
            new AutoMBox(MainWin.class.getName()) {

                @Override
                public void do_stuff() throws Exception {
                    DBBillingPeriod az_new = new DBBillingPeriod();
                    az_new.loadFromCopy(bp);

                    started = false;
                    changeAZ(az_new, false);
                    started = true;
                }
            };
        }
        
        started = true;
    }//GEN-LAST:event_jBChangeActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        
        invokeDialogModal(new DeleteBP(this));
        
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu1ActionPerformed

    private void jMScoregMemberImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMScoregMemberImportActionPerformed
        
        ImportMemberFromScoreg.importMember(this);
        
    }//GEN-LAST:event_jMScoregMemberImportActionPerformed
    

    public String getLastOpenPath() {
        return last_path;
    }

    public void setLastOpenPath(String path) {
        last_path = path;
    }    
    
    @Override
    public void close()
    {        
        new AutoLogger(MainWin.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                saveCurrentAZ();
            }
        };
        
        root.getSetup().setLocalConfig("LastPath", last_path);
        root.getSetup().setLocalConfig(CONFIG_LAST_AZ, String.valueOf(bp.idx.getValue()));
        super.close();
    }
 
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupDesign;
    private javax.swing.JButton jBChange;
    private javax.swing.JButton jBNew;
    private javax.swing.JComboBox jCAZ;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuItem jMAbout;
    private javax.swing.JMenuItem jMChangeLog;
    private javax.swing.JMenuItem jMDBExport;
    private javax.swing.JMenuItem jMDBImport;
    private javax.swing.JMenuItem jMDatabase;
    private javax.swing.JMenuItem jMPlugin;
    private javax.swing.JMenuItem jMQuit;
    private javax.swing.JMenuItem jMResetDB;
    private javax.swing.JMenuItem jMScoregMemberImport;
    private javax.swing.JMenuItem jMSettings;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItemMembers;
    private javax.swing.JRadioButtonMenuItem jRMetal;
    private javax.swing.JRadioButtonMenuItem jRMotif;
    private javax.swing.JRadioButtonMenuItem jRNimbus;
    private javax.swing.JRadioButtonMenuItem jRSystem;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JTextArea jTComment;
    // End of variables declaration//GEN-END:variables

 void changeStyle()
    {
        if (jRMetal.isSelected()) {
            changeStyle("metal");
        }
        else if (jRSystem.isSelected()) {
            changeStyle("system");
        }
        else if (jRNimbus.isSelected()) {
            changeStyle("nimbus");
        }
        else if (jRMotif.isSelected()) {
            changeStyle("motif");
        }
    }
    
    void changeStyle( final String name )
    {        
        new AutoMBox(MainWin.class.getName())
        {
            @Override
            public void do_stuff() throws Exception {
                root.getSetup().setLocalConfig(FrameWorkConfigDefinitions.LookAndFeel.getConfigName(), name);
                
                UIManager.setLookAndFeel(BaseModuleLauncher.getLookAndFeelStrByName(name));
                root.closeAllWindowsNoAppExit();
                new MainWin(main,root).setVisible(true);
            }
        };
    }    

    private void initStyle()
    {
        buttonGroupDesign.add(jRMetal);
        buttonGroupDesign.add(jRSystem);
        buttonGroupDesign.add(jRNimbus);
        buttonGroupDesign.add(jRMotif);
        
        String style = root.getSetup().getLocalConfig(FrameWorkConfigDefinitions.LookAndFeel);
        
        if( style.equals("motif") ) {
            jRMotif.setSelected(true);
        } else if( style.equals("metal")) {
            jRMetal.setSelected(true);            
        } else if( style.equals("nimbus")) {
            jRNimbus.setSelected(true);
        } else {
            jRSystem.setSelected(true);
        }
        
        ActionListener styleChanged = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                changeStyle();
            }
        };
        

        jRMetal.addActionListener(styleChanged);
        jRSystem.addActionListener(styleChanged);
        jRNimbus.addActionListener(styleChanged);
        jRMotif.addActionListener(styleChanged);
    }       

    @Override
    public boolean openWithLastWidthAndHeight() {
        return false;
    }        
    
    void updateAZList() throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException 
    {
        changeAZ(bp,true);
    }    

    void changeAZ(DBBillingPeriod az_other) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException 
    {
        changeAZ(az_other,true);
    }    
    
    void changeAZ(DBBillingPeriod az_other,boolean save_current) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException 
    {
        changeAZ(az_other,save_current, false);
    }        
    
    void changeAZ(DBBillingPeriod az_other,  boolean save_current, boolean load_first) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException 
    {
        if( save_current )
            saveCurrentAZ();
        
        bp.loadFromCopy(az_other);
        Transaction trans = getTransaction();
        
        trans.fetchTableWithPrimkey(bp);

        List<DBBillingPeriod> jt = getTransaction().fetchTable2(new DBBillingPeriod(), "order by " + getTransaction().markColumn("hist_anzeit") + " desc");

        if( jt.isEmpty() ) {
            trans.insertValues(bp);
            trans.commit();
            jt.add(bp);
        }
        
        jCAZ.removeAllItems();
        
        int preselect = -1;
        int count = 0;

        for (DBBillingPeriod j : jt) {
            
            if (load_first && preselect < 0) {
                preselect = count;
                az_other.loadFromCopy(j);
            }
            
            jCAZ.addItem(new CreateBP.BPNameWrapper(j));
            if (j.idx.getValue().equals(bp.idx.getValue())) {                                   
                preselect = count;
            }

            count++;
        }

        if (preselect >= 0) {
            jCAZ.setSelectedIndex(preselect);
        }

        var_to_gui();
    }

    public DBBillingPeriod getAZ() {
        return bp;
    }

    @Override
    public Integer getAZIdx() {
        return bp.idx.getValue();
    }
    
    public boolean isAzLocked()
    {
        return bp.isLocked();
    }
    
    public boolean checkAz()
    {
        if( getAZIdx() == 0 )
        {
            JOptionPane.showMessageDialog(this, MlM("Bitte legen sie einen Abrechnungszeitraum fest."));
            jBNewActionPerformed(null);
            return false;
        }
                
        return true;
    }

    @Override
    public Audit getAudit() {
        return audit;
    }
    
    final public void bindVar( JTextField field, DBString var )
    {
        super.bindVar(field, var);
        field.setDocument(new DocumentFieldLimit(var.getMaxLen()));
    }
    
    final public void bindVar( JTextArea field, DBString var )
    {
        super.bindVar(field, var);
        field.setDocument(new DocumentFieldLimit(var.getMaxLen()));
    }
    
    
}
