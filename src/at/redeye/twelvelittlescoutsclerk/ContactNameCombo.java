/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.BindVarInterface.Pair;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JComboBox;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class ContactNameCombo extends Pair {
    
    private static Logger logger = Logger.getLogger(ContactNameCombo.class);

    HashMap<String, DBContact> contacts;
    JComboBox combo;
    DBContact selected_contact;

    public ContactNameCombo(JComboBox combo, List<DBContact> kunden_list, DBContact selected_contact) {
        this(combo, kunden_list, selected_contact, false );
    }
    
    public ContactNameCombo(JComboBox combo, List<DBContact> kunden_list, DBContact selected_contact, boolean emty_at_begin) {
        this.combo = combo;
        contacts = new HashMap();
        this.selected_contact = selected_contact;

        if( emty_at_begin ) {
            combo.addItem("");
        }
        
        for (DBContact contact : kunden_list) {
            String name = getName4Contact(contact);
            contacts.put(name, contact);
            combo.addItem(name);
        }
    }    

    public static String getName4Contact(DBContact contact) {
        return contact.name.getValue() + " " + contact.forname.getValue();
    }

    @Override
    public void gui_to_var() {
        String item = (String) combo.getSelectedItem();

        if (item != null) {
            if (contacts.get(item) != null ) {
                selected_contact.loadFromCopy(contacts.get(item));
            } else {                
                selected_contact.loadFromCopy(new DBContact());
            }
        }
    }

    @Override
    public void var_to_gui() {
        for (Entry<String, DBContact> e : contacts.entrySet()) {
//            logger.trace(e.getValue().idx.getValue() + " " + selected_kunde.idx.getValue());
            if (e.getValue().idx.getValue().equals(selected_contact.idx.getValue())) {
                combo.setSelectedItem(e.getKey());
                break;
            }
        }
    }

    @Override
    public Object get_first() {
        return combo;
    }

    @Override
    public Object get_second() {
        return selected_contact;
    }
}
