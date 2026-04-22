/**
 * TwelveLittleScoutsClerk CSV filetype import filter
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.imports;

import java.io.File;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

import at.redeye.FrameWork.utilities.FileExtFilter;

/**
 *
 * @author martin
 */
public class MultipleExtFileFilter extends FileFilter {

    ArrayList<FileExtFilter> filter;
    String description;
    
    public MultipleExtFileFilter( ArrayList<String> filters )
    {
        filter = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filters.size(); i++) {
            FileExtFilter f = new FileExtFilter(filters.get(i));            
            filter.add(f);

            if (i > 0) {
                sb.append(", ");
            }
            sb.append(f);
        }
        description = sb.toString();
    }        
    
    @Override
    public boolean accept(File file) {
        if( file.isDirectory() )
            return true;
        
        for (FileExtFilter f : filter) {
            if (f.accept(file)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return description;
    }
    
}
