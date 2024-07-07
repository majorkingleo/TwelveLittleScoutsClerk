/**
 * TwelveLittleScoutsClerk common functions on table Member 
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import javax.swing.*;
/**
 *
 * @author martin
 */
public class tableFilter extends javax.swing.JPanel {

    TableFilterHelper helper;
        
    public tableFilter() {
        initComponents();                  
    }           

    @Override
    public void removeNotify() {
        super.removeNotify();
        
        if( helper != null )
            helper.removeNotify();
    }   

    public void setFilter(JTable table) 
    {
        if( helper == null )
            helper = new TableFilterHelper(this, jTFilter, table);
    }
        

    @Override
    public void requestFocus() {
        jTFilter.requestFocus();
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTFilter = new javax.swing.JTextField();

        setMinimumSize(new java.awt.Dimension(233, 21));

        jLabel1.setText("Filter:");

        jTFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTFilterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 2, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTFilterActionPerformed

    }//GEN-LAST:event_jTFilterActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTFilter;
    // End of variables declaration//GEN-END:variables
}
