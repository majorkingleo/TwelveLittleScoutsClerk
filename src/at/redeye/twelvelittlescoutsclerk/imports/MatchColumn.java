/**
 * TwelveLittleScoutsClerk Helper class for importing CSV file colums by column name
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.imports;

import java.util.ArrayList;
import java.util.HashMap;

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
    
    /**
     * initialize the class with the first line, to know all columns by name
     * @param cols 
     */
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
    
    /**
     * Get the column by name (init() has to be called before).
     * 
     * @param name
     * @param cols
     * @return 
     */
    public String getOrDefault(String name, String[] cols) {
        return getOrDefault(name,cols,0);
    }
    
    /**
     * Get the column by name (init() has to be called before)
     * If there a multiple columns with the same name, the idx indicates which one you wan't to get.
     * 
     * @param name
     * @param cols
     * @param idx
     * @return 
     */
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
