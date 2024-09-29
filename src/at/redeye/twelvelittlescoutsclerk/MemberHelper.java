/**
 * TwelveLittleScoutsClerk common functions on table Member 
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.DefaultInsertOrUpdater;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.DBDataType;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBGroup;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Contacts;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Groups;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;


public class MemberHelper {
    
    private static final Logger logger = Logger.getLogger(MemberHelper.class.getName());
    
    
    public static HashMap<Integer,DBGroup> fetch_groups( Transaction trans, int bpidx ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {
        DBGroup group = new DBGroup();                
        List<DBGroup> groups = trans.fetchTable2(group);        
        HashMap<Integer,DBGroup> map_g = new HashMap<Integer,DBGroup>();
        
        for( DBGroup g : groups ) {
            map_g.put( g.idx.getValue(), g);
        }
        
        DBMembers2Groups m2g = new DBMembers2Groups();
        List<DBMembers2Groups> m2gs = trans.fetchTable2(m2g,
                "where " + trans.markColumn(m2g.bp_idx) + " = " + bpidx );
                
        HashMap<Integer,DBGroup> map_m2g = new HashMap<Integer,DBGroup>();
        
        for( DBMembers2Groups m : m2gs ) {
            DBGroup g = map_g.get(m.group_idx.getValue());
            map_m2g.put( m.member_idx.getValue(), g);            
        }
        
        return map_m2g;
    }
    
    public static List<DBMember> fetch_members( Transaction trans, int bpidx ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {
        DBMember member = new DBMember();
        
        List<DBMember> members = trans.fetchTable2(member,
                        "where " + trans.markColumn(member.bp_idx) + " = " + bpidx
                      + " order by " + trans.markColumn(member.name));
                
        HashMap<Integer,DBGroup> m2g = fetch_groups(trans, bpidx);

        for (DBMember m : members) {
            DBGroup group = m2g.get(m.idx.getValue());
            
            if( group != null ) {
                m.group.loadFromString(group.name.getValue());
            }                    
        }
        
        return members;
    }
    
    public static Integer fetch_group_idx( Transaction trans, DBMember member) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {
        DBGroup group = new DBGroup();
        DBMembers2Groups m2g = new DBMembers2Groups();
        
        String stmt = "select " + trans.markColumn(group,group.idx)
                + " from " + trans.markTable(group) + ", " + trans.markTable(m2g)
                + " where " +  trans.markColumn(group,group.idx) + " = " + trans.markColumn(m2g,m2g.group_idx)
                + " and " + trans.markColumn(m2g,m2g.member_idx) + " = " + member.idx.toString();
        
        
        List<Integer> data = (List<Integer>) trans.fetchOneColumnValue(stmt, DBDataType.DB_TYPE_INTEGER);
        
        if( data.isEmpty() ) {
            return null;
        }
        
        return data.get(0);
    }
    
    public static List<DBMember> findMembersFor( Transaction trans, DBContact contact ) throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException
    {        
        DBMember member = new DBMember();
        DBMembers2Contacts m2c = new DBMembers2Contacts();
                
        
        List<DBMember> members = trans.fetchTable2(member, 
            "where " + trans.markColumn(member.bp_idx) + " = " + contact.bp_idx.toString() +
            " and exists ( select 1 from " + trans.markTable(m2c) + " where " + trans.markColumn(m2c.bp_idx) + " = " + trans.markColumn(member,member.bp_idx) +
            "       and " + trans.markColumn(m2c.contact_idx) + " = " + contact.idx.toString() +
            "       and " + trans.markColumn(m2c.member_idx) + " = " + trans.markColumn(member,member.idx) + " ) " );              
        
        return members;
    }
    
    public static void deleteMember( Transaction trans, DBMember member )  throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        trans.deleteWithPrimaryKey(member);
    }
    
    public static void updateMemberMissingFields( Transaction trans, List<DBMember> members )  throws SQLException, TableBindingNotRegisteredException, UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException
    {
        for( DBMember member : members ) {
            trans.updateValues(member);
        }
    }
    
    
}
