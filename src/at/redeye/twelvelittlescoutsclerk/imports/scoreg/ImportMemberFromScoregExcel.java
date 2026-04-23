/**
 * TwelveLittleScoutsClerk Scoreg Excel (.xlsx) importer
 * @author Copyright (c) 2023-2025 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.imports.scoreg;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import at.redeye.twelvelittlescoutsclerk.MainWin;

/**
 * Imports Scoreg member data from an Excel (.xlsx) file.
 * The first sheet must contain a header row with the same column names
 * as the Scoreg CSV export.
 *
 * @author martin
 */
public class ImportMemberFromScoregExcel extends ImportMemberFromScoreBase
{
    private static final Logger logger = Logger.getLogger(ImportMemberFromScoregExcel.class);

    public ImportMemberFromScoregExcel( MainWin main, File file )
    {
        super(main, file);
    }

    @Override
    protected List<String[]> readData() throws Exception
    {
        List<String[]> rows = new ArrayList<>();

        try( FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis) )
        {
            Sheet sheet = workbook.getSheetAt(0);

            // Determine number of columns from the header row
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            int lastCol = (headerRow != null) ? headerRow.getLastCellNum() : 0;

            for( Row row : sheet )
            {
                String[] cols = new String[lastCol];
                for( int c = 0; c < lastCol; c++ )
                {
                    cols[c] = cellToString(row.getCell(c));
                }
                rows.add(cols);
            }
        }

        return rows;
    }

    private String cellToString( Cell cell )
    {
        if( cell == null )
            return "";

        CellType type = cell.getCellType();
        if( type == CellType.FORMULA )
            type = cell.getCachedFormulaResultType();

        switch( type )
        {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                if( DateUtil.isCellDateFormatted(cell) )
                    return sdf_date.format(cell.getDateCellValue());
                double d = cell.getNumericCellValue();
                if( d == Math.floor(d) && !Double.isInfinite(d) )
                    return String.valueOf((long) d);
                return String.valueOf(d);

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            default:
                return "";
        }
    }

    /**
     * Opens a file chooser for an Excel (.xlsx) file and imports Scoreg
     * member data. Commit and rollback are handled automatically.
     *
     * @param mainwin the application main window
     */
    public static void importMember( final MainWin mainwin )
    {
        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept( File f ) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xlsx");
            }
            @Override
            public String getDescription() {
                return "*.xlsx";
            }
        });
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
            executeImport(mainwin, new ImportMemberFromScoregExcel(mainwin, file));
        }
    }
}
