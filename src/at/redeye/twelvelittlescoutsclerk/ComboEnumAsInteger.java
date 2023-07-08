/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.BindVarInterface.ComboStringPair;
import at.redeye.FrameWork.base.bindtypes.DBEnumAsInteger;
import at.redeye.FrameWork.base.bindtypes.DBValue;
import javax.swing.JComboBox;

/**
 *
 * @author martin
 */
class ComboEnumAsInteger extends ComboStringPair {

    JComboBox combo;
    DBValue value;

    public ComboEnumAsInteger(JComboBox combo, DBEnumAsInteger value) {
        super(combo, value);

        for (String label : value.getPossibleValues()) {
            combo.addItem(label);
        }
    }
} 