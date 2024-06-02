/**
 * TwelveLittleScoutsClerk CSV filetype import filter
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.imports;

import at.redeye.FrameWork.utilities.FileExtFilter;
import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author martin
 */
public class CSVFileFilter extends FileFilter {

    FileExtFilter filter;
    
    public CSVFileFilter()
    {
        filter = new FileExtFilter("*.csv");
    }        
    
    @Override
    public boolean accept(File f) {
        if( f.isDirectory() )
            return true;
        
        return filter.accept(f);
    }

    @Override
    public String getDescription() {
        return "*.csv";
    }
    
}
