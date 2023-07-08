/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.AutoMBox;
import javax.swing.JFrame;

/**
 *
 * @author martin
 */
public class ExceptionHandler implements AutoMBox.ShowAdvancedException 
{

    @Override
    public boolean wantShowAdvancedException(Exception ex) {
        return true;
    }

    @Override
    public void showAdvancedException(Exception ex) {
        JFrame dialog = new ShowException("Fehler", ex);
        dialog.setVisible(true);
    }
    
}
