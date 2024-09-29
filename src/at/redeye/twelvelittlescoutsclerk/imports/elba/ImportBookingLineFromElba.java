/**
 * TwelveLittleScoutsClerk ELBA csv file importer
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.imports.elba;

import at.redeye.FrameWork.base.AutoMBox;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.MainWin;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillingPeriod;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import at.redeye.twelvelittlescoutsclerk.imports.CSVFileFilter;
import at.redeye.twelvelittlescoutsclerk.imports.InfoWin;
import au.com.bytecode.opencsv.CSVReader;
import java.awt.Dialog;
import java.io.*;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class ImportBookingLineFromElba
{
    private static final Logger logger = Logger.getLogger(ImportBookingLineFromElba.class);
    
    MainWin main;
    Transaction trans;
    File csv_file;
    SimpleDateFormat sdf_date;    
    int azidx;
    
    public ImportBookingLineFromElba( MainWin main, File csv_file )
    {
        this.main = main;
        this.csv_file = csv_file;       
        sdf_date = new SimpleDateFormat("dd.MM.yyyy");
        trans = main.getNewTransaction();
    }
    
    public void clear()
    {
    }

    public String getErrorMessage()
    {                
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }
    
    public static String cutBOM(String value) {
        // UTF-8 BOM is EF BB BF, see https://en.wikipedia.org/wiki/Byte_order_mark
        String bom = String.format("%x", new BigInteger(1, value.substring(0,3).getBytes()));
        if (bom.equals("efbbbf"))
            // UTF-8
            return value.substring(3, value.length());
        else if (bom.substring(0, 2).equals("feff") || bom.substring(0, 2).equals("ffe"))
            // UTF-16BE or UTF16-LE
            return value.substring(2, value.length());
        else
            return value;
    }
          
    
    public boolean run( int azidx ) throws FileNotFoundException, IOException, ParseException, SQLException, UnsupportedDBDataTypeException, WrongBindFileFormatException, TableBindingNotRegisteredException
    {
        this.azidx = azidx;
        clear();
        
        if( trans == null ) {
            trans = main.getNewTransaction();
        }               
                        
        FileInputStream in = new FileInputStream(csv_file);
        BOMInputStream bomIn = BOMInputStream.builder()
            .setInputStream(in)
            .setByteOrderMarks(ByteOrderMark.UTF_8,ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE)
            .get();
        
        //CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csv_file),"UTF-8"), ';', '\"');
        CSVReader reader = new CSVReader( new InputStreamReader(bomIn), ';', '\"' );
        
        List list = reader.readAll();
        
        for( int i = 0; i < list.size(); i++ )
        {
            String cols[] = (String[]) list.get(i);
            
            if( i == 0 ) {
                cols[0] = cutBOM(cols[0]);
            }
            
            logLine(cols);
            
            boolean ignore_line = false;
            
            DBBookingLine line = new DBBookingLine();            
            
            line.date.loadFromCopy(sdf_date.parse(cols[0]));           
            line.bp_idx.loadFromCopy(azidx);            
            line.data_source.loadFromString("ELBA");
            line.line.loadFromCopy(cols[1]);
            line.amount.loadFromCopy(Double.valueOf(cols[3].replaceFirst(",", ".")));
                        
            var lines = trans.fetchTable2(line, " where "
                    + trans.markColumn(line,line.line) + " = '" + line.line.toString() + "' "
                    + " and " + trans.markColumn(line,line.bp_idx) + " = " + azidx
                    + " and " + trans.markColumn(line,line.data_source) + " = 'ELBA' "
            );
            
            if( lines == null || lines.isEmpty() ) {
                line.idx.loadFromCopy(main.getNewSequenceValue(DBBookingLine.BOOKING_LINE_IDX_SEQUENCE));
                line.hist.setAnHist(main.getRoot().getLogin());
                trans.insertValues(line);
            }
        }
        
        if( getErrorMessage() != null ) {
            return false;
        }
        
        return true;
    }
    
    
           
    private void logLine( String cols[] )
    {
        if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();

            for (String col : cols) {
                sb.append(col);
                sb.append(";");
            }

            logger.trace(sb);
        }
    }        
    
    public void dispose() throws SQLException
    {
        if( trans != null ) {
            trans.rollback();
            trans = null;
        }
    }
    

    private void commit() throws SQLException {
        trans.commit();
    }    
    
    /**
     * Function that starts the import of memebrs, by opening the filechooser
     * as the first step. Will do commit and rollback on fail automatically
     * @param mainwin 
     */
    public static void importBookingLine( final MainWin mainwin )
    {
        JFileChooser fc = new JFileChooser();

        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new CSVFileFilter());
        fc.setMultiSelectionEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        String last_path = mainwin.getLastOpenPath();

        logger.info("last path: " + last_path);

        if (last_path != null) {
            fc.setCurrentDirectory(new File(last_path));
        }

        int retval = fc.showOpenDialog(mainwin);

        if (retval != 0) {
            return;
        }

        File file = fc.getSelectedFile();

        if (file.canRead()) {
            mainwin.setLastOpenPath(file.getParent());

            logger.debug("importiere " + file);
            
            final ImportBookingLineFromElba importer = new ImportBookingLineFromElba(mainwin, file );            
                    
            new AutoMBox(ImportBookingLineFromElba.class.getName()) {

                @Override
                public void do_stuff() throws Exception {
                    
                    mainwin.setWaitCursor();
                    
                    DBBillingPeriod bp = new DBBillingPeriod();
                    
                    Transaction trans = mainwin.getTransaction();                   
                    
                    StringBuilder sb = new StringBuilder();                                       

                    boolean error_happened = false;
                    
                    if( !importer.run(mainwin.getBPIdx()) )
                    {                         
                        sb.append(importer.getErrorMessage() + "\n\n");
                        error_happened = true;
                    }                        

                    
                    boolean do_commit = false;
                    
                    mainwin.setWaitCursor(false);
                    
                    if( error_happened )
                    {
                        InfoWin infowin = new InfoWin(mainwin.getRoot(), "Warnung", sb.toString() );
                        infowin.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                        infowin.setVisible(true);
                        infowin.toFront();
                        infowin.requestFocus();
                        
                        if( infowin.pressedYes() )
                            do_commit = true;
                    } else {
                        JOptionPane.showMessageDialog(mainwin, "Der Datenimport war erfolgreich");
                        do_commit = true;
                    }
                    
                                        
                    if( do_commit )
                    {
                        importer.commit();
                    }                                        
                }
            };
            
            try {
                importer.dispose();
            } catch (SQLException ex) {
                logger.error(ex,ex);
            }
        }        
    }
}