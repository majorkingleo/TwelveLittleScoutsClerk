/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package at.redeye.twelvelittlescoutsclerk.imports;

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
        
    private final HashMap<String,Column> columns;
    
    public void init( String cols[] )
    {
        for( int idx = 0; idx < cols.length; idx++ ) {
            columns.put(cols[idx],new Column( cols[idx], idx ));
        }
    }
    
    public String getOrDefault(String name, String[] cols) {
        Column col = columns.get(name);
        
        if( col == null ) {
            return new String();
        }               
        
        return cols[col.idx];
    }
}
