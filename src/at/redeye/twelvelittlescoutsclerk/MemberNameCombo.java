/**
 * TwelveLittleScoutsClerk common functions on table Member 
 * @author Copyright (c) 2023-2024 Martin Oberzalek
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
    DBMember selected_member;

    public MemberNameCombo(JComboBox combo, List<DBMember> member_list, DBMember selected_member) {
        this(combo, member_list, selected_member, false );
    }
    
    public MemberNameCombo(JComboBox combo, List<DBMember> member_list, DBMember selected_member, boolean emty_at_begin) {
        this.combo = combo;
        members = new HashMap();
        this.selected_member = selected_member;

        if( emty_at_begin ) {
            combo.addItem("");
        }
        
        for (DBMember kunde : member_list) {
            String name = getName4Member(kunde);
            members.put(name, kunde);
            combo.addItem(name);
        }
    }    

    public static String getName4Member(DBMember member) {
        return member.name.getValue() + " " + member.forname.getValue() + " (" + member.member_registration_number + ")";
    }

    @Override
    public void gui_to_var() {
        String item = (String) combo.getSelectedItem();

        if (item != null) {
            if (members.get(item) != null ) {
                selected_member.loadFromCopy(members.get(item));
            } else {                
                selected_member.loadFromCopy(new DBMember());
            }
        }
    }

    @Override
    public void var_to_gui() {
        for (Entry<String, DBMember> e : members.entrySet()) {
//            logger.trace(e.getValue().idx.getValue() + " " + selected_kunde.idx.getValue());
            if (e.getValue().idx.getValue().equals(selected_member.idx.getValue())) {
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
        return selected_member;
    }
}
