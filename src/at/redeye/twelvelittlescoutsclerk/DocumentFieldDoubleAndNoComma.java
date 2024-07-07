/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.widgets.documentfields.DocumentFieldLimit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 *
 * @author martin
 */
public class DocumentFieldDoubleAndNoComma extends DocumentFieldLimit {

    public DocumentFieldDoubleAndNoComma(int limit) {
        super(limit);
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

        if (str.equals(",")) {
            str = ".";
        }
        
        if( str != null )                    
            if( str.matches("[0-9,\\.\\-\\+]+"))
                super.insertString(offs, str, a);                
    } 
    
}
