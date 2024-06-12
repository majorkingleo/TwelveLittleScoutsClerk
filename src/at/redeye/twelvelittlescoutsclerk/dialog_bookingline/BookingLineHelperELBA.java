/**
 * TwelveLittleScoutsClerk Dialog for Contact table
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk.dialog_bookingline;

import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBookingLine;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;


public class BookingLineHelperELBA 
{       
    public static final String DATA_SOURCE = "ELBA";
    
    static final String KEYWORD_IBAN_AUFTRAGGEBER = "IBAN Auftraggeber:";
    static final String KEYWORD_BIX_AUFTRAGGEBER = "BIC Auftraggeber:";
    static final String KEYWORD_AUFTRAGGEBER = "Auftraggeber:";
    static final String KEYWORD_ZAHLUNGSREFERENZ = "Zahlungsreferenz:";
    static final String KEYWORD_VERWENDUNGSZWECK = "Verwendungszweck:";
    
    static ArrayList<String> splitKeyWords = new ArrayList<String>( Arrays.asList(
        KEYWORD_IBAN_AUFTRAGGEBER,
        KEYWORD_BIX_AUFTRAGGEBER,
        KEYWORD_AUFTRAGGEBER,
        KEYWORD_ZAHLUNGSREFERENZ,
        KEYWORD_VERWENDUNGSZWECK
         ) );   
    
    private static class Index
    {
        public int start = 0;
        public int end = 0;
        public String keyword = "";
        public String text = "";
        
        public Index( int start, int end, String keyword ) {
            this.start = start;
            this.end = end;
            this.keyword = keyword;
        }
    }

    /**
     * 
     * @param line 
     */
    public static void parseBookingLineText( DBBookingLine line )
    {
       // example line:
       // Auftraggeber: Hans Müller Zahlungsreferenz: Müller Franz, GuSp, SoLa 2020 IBAN Auftraggeber: AT235321235511212311 BIC Auftraggeber: KALTEDASXEK
    
       ArrayList<Index> indexes = new ArrayList<>();
       String l = line.line.getValue();
       
       for( int start = 0; start < l.length(); ) {
             Map.Entry<Integer,String> idx = findFirstOf( l, start, splitKeyWords );
            
            if( idx != null ) {
                int start_idx = idx.getKey();
                int end_idx = start_idx + idx.getValue().length();
                
                if( !indexes.isEmpty() ) {                    
                    Index last_index = indexes.get(indexes.size()-1);
                    last_index.text = l.substring(last_index.end, start_idx );                    
                }
                
                indexes.add(new Index( start_idx, end_idx, idx.getValue() ));
                
                start = end_idx + 1;
                
            } else if( !indexes.isEmpty() ) {
                Index last_index = indexes.get(indexes.size()-1);
                last_index.text = l.substring(last_index.end ); 
                break;
            }
       }
       
       
       for( Index idx : indexes ) {
           System.out.println( String.format("idx: %d - %d: '%s' = '%s'", idx.start, idx.end, idx.keyword, idx.text ) );
           switch( idx.keyword )
           {
               case KEYWORD_IBAN_AUFTRAGGEBER:
                   line.from_bank_account_iban.loadFromString(idx.text.trim());
                   break;
                   
               case KEYWORD_BIX_AUFTRAGGEBER:
                   line.from_bank_account_bic.loadFromString(idx.text.trim());
                   break;
                   
               case KEYWORD_AUFTRAGGEBER:
                   line.from_name.loadFromString(idx.text.trim());
                   break;
                   
               case KEYWORD_ZAHLUNGSREFERENZ:
                   line.reference.loadFromString(idx.text.trim());
                   break;
                   
               case KEYWORD_VERWENDUNGSZWECK:
                   line.reference.loadFromString(idx.text.trim());
                   break;                   
           }
       }              
    }
    
    private static Map.Entry<Integer,String> findFirstOf( String line, int start_idx, Collection<String> words )
    {
        SimpleEntry<Integer,String> ret = null;
        
        for( String keyword : words ) {
            int w_idx = line.indexOf( keyword, start_idx );
            if( w_idx >= 0 ) {
                if( ret == null || ret.getKey() > w_idx ) {
                    ret = new SimpleEntry( w_idx, keyword );
                }
            }
        }
        
        return ret;
    }
    
    
    public static void main( String args[] )
    {
        String test1 = "Auftraggeber: Hans Müller Zahlungsreferenz: Müller Franz, GuSp, SoLa 2020 IBAN Auftraggeber: AT235321235511212311 BIC Auftraggeber: KALTEDASXEK";
        String test2 = "Auftraggeber: Hans Müller Verwendungszweck: Müller Franz, GuSp, SoLa 2020 IBAN Auftraggeber: AT235321235511212311 BIC Auftraggeber: KALTEDASXEK";
        
        BookingLineHelperELBA elba = new BookingLineHelperELBA();
        DBBookingLine line = new DBBookingLine();
        line.line.loadFromString(test2);        
        elba.parseBookingLineText(line);
        System.out.println( "from_name: " + line.from_name.toString() );
        
    }
}
