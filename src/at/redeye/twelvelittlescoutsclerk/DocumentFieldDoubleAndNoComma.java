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
public class DocumentFieldDoubleAndNoComma extends DocumentFieldLimit {

    public DocumentFieldDoubleAndNoComma(int limit) {
        super(limit);
    }

    // AI modification start (Claude Sonnet 4.6, 2026-09-03):
    // Paste support. Typing inserts single chars, paste inserts the whole
    // string at once -> validate the pasted string as one number.
    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (str == null) {
            return;
        }

        // replace German decimal comma by dot and drop space / narrow
        // no-break space thousands separators (e.g. from Excel cells)
        String cleaned = normalize(str);

        if (cleaned.isEmpty()) {
            return;
        }

        // single character: allow number building blocks while typing
        if (cleaned.length() == 1) {
            if (cleaned.matches("[0-9.,\\-+]")) {
                super.insertString(offs, cleaned, a);
            }
            return;
        }

        // whole string (paste): only accept if it is a valid number
        if (isValidDouble(cleaned)) {
            super.insertString(offs, cleaned, a);
        }
    }

    private String normalize(String str) {
        return str.replace(',', '.').replace("\u202F", "").replace(" ", "").replace("\t", "");
    }

    private boolean isValidDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    // AI modification end

}
