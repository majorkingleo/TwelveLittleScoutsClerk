/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShowException.java
 *
 * Created on 26.02.2013, 10:30:58
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.utilities.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class ShowException extends javax.swing.JFrame {    

    private static final Logger logger = Logger.getLogger(ShowException.class);
    
    /** Creates new form ShowException */
    public ShowException(String title, Exception ex ) {
        initComponents();
        setTitle( title );
       
        StringBuilder sb = new StringBuilder();
        
        sb.append(System.getProperty("java.vendor"));
        sb.append("\n");
        sb.append(System.getProperty("java.version"));
        sb.append("\n");
        sb.append(exceptionToString(ex));
        
        jTBacktrace.setText(sb.toString());
        jTBacktrace.setCaretPosition(0);
        
        Root root = Root.getLastRoot();
        
        jLMessage.setText(StringUtils.autoLineBreak(
                root.MlM("Es ist ein Fehler aufgetreten:") + " "
                + ex.getLocalizedMessage()));      
        
        
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTBacktrace = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLMessage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTBacktrace.setColumns(20);
        jTBacktrace.setRows(5);
        jScrollPane1.setViewportView(jTBacktrace);

        jButton1.setText("Ok");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Fehlertext Kopieren");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLMessage.setText("jLabel1");
        jLMessage.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 288, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        
        dispose();
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        
        jTBacktrace.selectAll();
        jTBacktrace.copy();
        
    }//GEN-LAST:event_jButton2ActionPerformed

    public String exceptionToString(Exception ex) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        PrintStream s = new PrintStream(bos);
        ex.printStackTrace(s);
        s.flush();

        return bos.toString();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
     
        try {
            throw new RuntimeException("test");
        } catch( Exception ex ) {
            showException("Error", ex);
        }
        
    }
    
    public static void showException( final String title, final Exception ex )
    {
        logger.error(ex,ex);
        
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new ShowException(title,ex).setVisible(true);
            }
        });        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLMessage;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTBacktrace;
    // End of variables declaration//GEN-END:variables

}
