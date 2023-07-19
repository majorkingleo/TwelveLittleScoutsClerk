/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package at.redeye.twelvelittlescoutsclerk.imports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author martin
 */
public class MatchColumn {

    public MatchColumn() {
        this.columns = new HashMap<>();
    }

    public static class Column
    {
        public String name;
        public Integer idx;
        
        public Column( String name, int idx ) {
            this.name = name;
            this.idx = idx;
        }
    }
        
    private final HashMap<String,ArrayList<Column>> columns;
    
    public void init( String cols[] )
    {
        for( int idx = 0; idx < cols.length; idx++ ) {
            String name = cols[idx];
            
            ArrayList<Column> col_array = columns.get(name);
            
            if( col_array == null ) {
                col_array = new ArrayList<>();
                columns.put(name, col_array);
            }
            
            col_array.add(new Column( cols[idx], idx ));
        }
    }
    
    public String getOrDefault(String name, String[] cols) {
        return getOrDefault(name,cols,0);
    }
    
    public String getOrDefault(String name, String[] cols, int idx) {
        
        ArrayList<Column> col_array = columns.get(name);
        
        if( col_array == null ) {
             return new String();
        }
        
        if( col_array.size() <= idx ) {
             return new String();
        }
        
        Column col = col_array.get(idx);
               
        return cols[col.idx];
    }
}
