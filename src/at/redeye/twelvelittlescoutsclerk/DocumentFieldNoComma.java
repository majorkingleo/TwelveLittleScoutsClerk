/**
 * TwelveLittleScoutsClerk connection table from bookingline to event
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */

package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.widgets.documentfields.DocumentFieldLimit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

/**
 *
 * @author martin
 */
public class DocumentFieldNoComma extends DocumentFieldLimit
{
    DocumentFieldNoComma( int limit )
    {
        super( limit );
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

        if( str.equals(",") )
                str = ".";

           super.insertString(offs, str.toUpperCase(), a );
    }
}
