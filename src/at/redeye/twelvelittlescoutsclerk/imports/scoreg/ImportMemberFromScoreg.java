/**
 * TwelveLittleScoutsClerk Scoreg CSV file importer
 * @author Copyright (c) 2023-2025 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.imports.scoreg;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.imports.MultipleExtFileFilter;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Imports Scoreg member data from a semicolon-separated CSV file
 * (ISO-8859-15 encoding).
 *
 * @author martin
 */
public class ImportMemberFromScoreg extends ImportMemberFromScoreBase
{
    private static final Logger logger = Logger.getLogger(ImportMemberFromScoreg.class);

    public ImportMemberFromScoreg( MainWin main, File csv_file )
    {
        super(main, csv_file);
    }

    @Override
    protected List<String[]> readData() throws Exception
    {
        CSVReader reader = new CSVReader(
                new InputStreamReader(new FileInputStream(file), "ISO-8859-15"), ';', '\"');
        List<String[]> rows = new ArrayList<>();
        for( Object row : reader.readAll() ) {
            rows.add((String[]) row);
        }
        return rows;
    }

    /**
     * Opens a file chooser for a CSV file and imports Scoreg member data.
     * Commit and rollback are handled automatically.
     *
     * @param mainwin the application main window
     */
    public static void importMember( final MainWin mainwin )
    {
        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new MultipleExtFileFilter( new ArrayList<String>(){{ add("*.csv"); add("*.xlsx"); }}));
        fc.setMultiSelectionEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        String last_path = mainwin.getLastOpenPath();
        logger.info("last path: " + last_path);

        if( last_path != null ) {
            fc.setCurrentDirectory(new File(last_path));
        }

        if( fc.showOpenDialog(mainwin) != 0 ) {
            return;
        }

        File file = fc.getSelectedFile();
        if( file.canRead() ) {

            mainwin.setLastOpenPath(file.getParent());
            logger.debug("importing " + file);

            if( file.getName().toLowerCase().endsWith(".csv") ) {                        
                executeImport(mainwin, new ImportMemberFromScoreg(mainwin, file));
            } else {
                executeImport(mainwin, new ImportMemberFromScoregExcel(mainwin, file));
            }
        }
    }
}