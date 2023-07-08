/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author martin
 */
public class TableFilterHelper {
    
    JTextField jTFilter;
    JPanel panel;
    long lastChange = 0;
    Timer timer;
    String lastValue = "";
    JTable table;
    TableRowSorter<TableModel> sorter;
            
    public TableFilterHelper(  JPanel panel, JTextField jTFilter_, JTable table_ )
    {
        this.panel = panel;
        this.jTFilter = jTFilter_;
        this.table = table_;
        
        jTFilter.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                lastChange = System.currentTimeMillis();
                //System.out.println("XXXXX");
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                lastChange = System.currentTimeMillis();
                //System.out.println("XXXXX");
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lastChange = System.currentTimeMillis();                
                //System.out.println("XXXXX");
            }
        });
        
        timer = new Timer(1000, new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {                          
                                
                if( table == null )
                    return;
                
                if( lastChange + 3000 > System.currentTimeMillis() ) {
                    if( !lastValue.equals(jTFilter.getText())) {
                        java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                jTFilterActionPerformed(null);
                            }
                        });
                    }
                }
            }
        });                
        
        timer.start();    
        
        setFilter(table);
    }
    
    private void jTFilterActionPerformed(java.awt.event.ActionEvent evt) {                                         
               
        if (table != null) 
        {
            String expr = jTFilter.getText();
            lastValue = expr;
            lastChange = System.currentTimeMillis();
            
            //System.out.println("filter");
            
            if (expr.trim().isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + expr));
            }
        }

    }       
    
    public void setFilter(JTable table) 
    {
        this.table = table;
        sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        //addCloseListener();
    }    
    
       
    public void removeNotify() {        
        if( timer != null )
        {
            timer.stop();
            timer = null;
        }
    }       

}
