/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.BindVarInterface.Pair;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JComboBox;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class MemberNameCombo extends Pair {
    
    private static Logger logger = Logger.getLogger(MemberNameCombo.class);

    HashMap<String, DBMember> members;
    JComboBox combo;
    DBMember selected_kunde;

    public MemberNameCombo(JComboBox combo, List<DBMember> kunden_list, DBMember selected_kunde) {
        this( combo, kunden_list, selected_kunde, false );
    }
    
    public MemberNameCombo(JComboBox combo, List<DBMember> kunden_list, DBMember selected_kunde, boolean emty_at_begin) {
        this.combo = combo;
        members = new HashMap();
        this.selected_kunde = selected_kunde;

        if( emty_at_begin ) {
            combo.addItem("");
        }
        
        for (DBMember kunde : kunden_list) {
            String name = getName4Kunde(kunde);
            members.put(name, kunde);
            combo.addItem(name);
        }
    }    

    public static String getName4Kunde(DBMember kunde) {
        return kunde.name.getValue() + " " + kunde.forname.getValue() + " (" + kunde.member_registration_number + ")";
    }

    @Override
    public void gui_to_var() {
        String item = (String) combo.getSelectedItem();

        if (item != null) {
            if (members.get(item) != null ) {
                selected_kunde.loadFromCopy(members.get(item));
            } else {                
                selected_kunde.loadFromCopy(new DBMember());
            }
        }
    }

    @Override
    public void var_to_gui() {
        for (Entry<String, DBMember> e : members.entrySet()) {
//            logger.trace(e.getValue().idx.getValue() + " " + selected_kunde.idx.getValue());
            if (e.getValue().idx.getValue().equals(selected_kunde.idx.getValue())) {
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
        return selected_kunde;
    }
}
