/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
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
