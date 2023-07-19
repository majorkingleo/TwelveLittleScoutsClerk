/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MonthlyReportOverview.java
 *
 * Created on 19.07.2010, 22:39:53
 */

package at.redeye.twelvelittlescoutsclerk.reports.audit;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.BaseDialog;
import at.redeye.FrameWork.base.bindtypes.DBDateTime;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.griesdorn.reports.FixNimbusBackgroundColor;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.MemberNameCombo;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import java.util.List;

/**
 *
 * @author martin
 */
public class ReportAudit extends BaseDialog {

    ReportAuditRenderer renderer;
    MainWin mainwin;
    DBDateTime von;
    DBDateTime bis;
    DBMember member;
    
    /** Creates new form MonthlyReportOverview */
    public ReportAudit(final MainWin mainwin) {
        super(mainwin.getRoot(),"Audit");
        initComponents();

        von = new DBDateTime("von");
        bis = new DBDateTime("bis");
        member = new DBMember();     
        
        FixNimbusBackgroundColor.fixNimbusBackgroundColor(jReport);
        
        String svon = root.getSetup().getLocalConfig(ReportAudit.class.getName() + "last_von", "");
        String sbis = root.getSetup().getLocalConfig(ReportAudit.class.getName() + "last_bis", "");
        String last_member = root.getSetup().getLocalConfig(ReportAudit.class.getName() + "last_member", "0");
        
        try {
            if( !svon.trim().isEmpty() )
                von.loadFromString(svon);
        } catch( Exception ex ) {
            logger.error(ex,ex);
        }
        
        try {
            if( !sbis.trim().isEmpty() )
                bis.loadFromString(sbis);
        } catch( Exception ex ) {
            logger.error(ex,ex);
        }
        
        try {
            member.idx.loadFromCopy(Integer.valueOf(last_member));
        } catch( NumberFormatException ex ) {
            logger.error(ex,ex);
        }
        
        new AutoMBox(ReportAudit.class.getName()) {

            @Override
            public void do_stuff() throws Exception {
                Transaction trans = getTransaction();                
                                
                List<DBMember> members = trans.fetchTable2(member, 
                        "where " + trans.markColumn(member.bp_idx) + " = " + mainwin.getAZIdx()
                        + " order by " + trans.markColumn(member.name));                
                                
                for( DBMember m : members ) {
                    if( m.idx.getValue().equals(member.idx.getValue()) ) {
                        member = m;
                        break;
                    }
                }
                
                addBindVarPair(new MemberNameCombo(jCKunde, members, member, true));
            }
        };               
        
        bindVar(jDVon, von);
        bindVar(jDBis, bis);        
        
        renderer = new ReportAuditRenderer(getTransaction(),mainwin.getAZ(), von, bis, member);    
        
        var_to_gui();
        
        jBSearch.requestFocus();
    }
    
 @Override
    public void close()
    {
        gui_to_var();
        
        root.getSetup().setLocalConfig(ReportAudit.class.getName() + "last_von", von.toString());
        root.getSetup().setLocalConfig(ReportAudit.class.getName() + "last_bis", bis.toString());
        root.getSetup().setLocalConfig(ReportAudit.class.getName() + "letzte_kundennummer", member.idx.toString());
        
        super.close();
    }
    

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jBPrint = new javax.swing.JButton();
        jBClose = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jDVon = new at.redeye.Plugins.JDatePicker.JDatePicker(root);
        jLabel1 = new javax.swing.JLabel();
        jDBis = new at.redeye.Plugins.JDatePicker.JDatePicker(root);
        jCKunde = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jBSearch = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jReport = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jBPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/print.png"))); // NOI18N
        jBPrint.setText("Drucken");
        jBPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBPrintActionPerformed(evt);
            }
        });

        jBClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/fileclose.gif"))); // NOI18N
        jBClose.setText("Schließen");
        jBClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCloseActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter"));

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        jLabel2.setText(" Von ");
        jPanel2.add(jLabel2);

        jDVon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDVonActionPerformed(evt);
            }
        });
        jPanel2.add(jDVon);

        jLabel1.setText(" Bis ");
        jPanel2.add(jLabel1);

        jDBis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDBisActionPerformed(evt);
            }
        });
        jPanel2.add(jDBis);

        jLabel3.setText("Name");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCKunde, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCKunde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        jBSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/redeye/FrameWork/base/resources/icons/reload.png"))); // NOI18N
        jBSearch.setText("Suchen");
        jBSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBSearchActionPerformed(evt);
            }
        });

        jReport.setContentType("text/html");
        jReport.setEditable(false);
        jScrollPane2.setViewportView(jReport);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jBSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 262, Short.MAX_VALUE)
                        .addComponent(jBPrint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBClose)
                    .addComponent(jBPrint)
                    .addComponent(jBSearch))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBPrintActionPerformed


        new AutoMBox(ReportAudit.class.getCanonicalName()) {
            @Override
            public void do_stuff() throws Exception {
                jReport.print();
            }
        };
    }//GEN-LAST:event_jBPrintActionPerformed

    private void jBCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCloseActionPerformed

        new AutoMBox(getTitle()) {

            @Override
            public void do_stuff() throws Exception {
                if( canClose() ) {
                    getTransaction().rollback();
                    close();
                }
            }
        };
}//GEN-LAST:event_jBCloseActionPerformed

    private void jBSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBSearchActionPerformed
        
        gui_to_var();
        update();
        
    }//GEN-LAST:event_jBSearchActionPerformed

    private void jDVonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDVonActionPerformed

   }//GEN-LAST:event_jDVonActionPerformed

    private void jDBisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDBisActionPerformed

   }//GEN-LAST:event_jDBisActionPerformed


    private void update()
    {          
        new AutoMBox(getTitle()) {

            @Override
            public void do_stuff() throws Exception {
                setWaitCursor();
                renderer.collectData();
                jReport.setText(renderer.render());
                jReport.setCaretPosition(0);
                setWaitCursor(false);
            }
        };
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBClose;
    private javax.swing.JButton jBPrint;
    private javax.swing.JButton jBSearch;
    private javax.swing.JComboBox jCKunde;
    private at.redeye.Plugins.JDatePicker.JDatePicker jDBis;
    private at.redeye.Plugins.JDatePicker.JDatePicker jDVon;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextPane jReport;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

}
