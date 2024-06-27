/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.redeye.twelvelittlescoutsclerk.reports.audit;



import at.redeye.FrameWork.base.AutoLogger;
import at.redeye.FrameWork.base.bindtypes.DBDateTime;
import at.redeye.FrameWork.base.reports.BaseReportRenderer;
import at.redeye.FrameWork.base.reports.ReportRenderer;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.twelvelittlescoutsclerk.DateFilter;
import at.redeye.twelvelittlescoutsclerk.MemberNameCombo;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillingPeriod;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBAudit;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import java.util.*;

/**
 *
 * @author martin
 */
public class ReportAuditRenderer extends BaseReportRenderer implements ReportRenderer
{           
    protected DBBillingPeriod bp;
    protected DBDateTime von;
    protected DBDateTime bis;
    protected DBAudit audit;
    protected DBMember member;
    protected Transaction trans;
    protected HashMap<Integer,DBMember> member_map;
    protected List<DBAudit> audit_liste;
    
    public ReportAuditRenderer(Transaction trans, DBBillingPeriod az, DBDateTime von, DBDateTime bis, DBMember member )
    {                 
        this.bp = az;
        this.von = von;
        this.bis = bis;        
        this.member = member;
        audit = new DBAudit();
        this.trans = trans;
        
    }   
    
    private String getKundenFilter()
    {
        if( member.idx.getValue() == 0 )
            return "";
        
        return " and " + trans.markColumn(audit.member_idx) + " = " + member.idx;
    }

    @Override
    public boolean collectData() 
    {
        AutoLogger al = new AutoLogger(ReportAuditRenderer.class.getSimpleName()) {

            @Override
            public void do_stuff() throws Exception {
                result = false;
                
                DBMember member = new DBMember();
                
                audit_liste = trans.fetchTable2(audit, " where " + trans.markColumn(member.bp_idx) + " = " + bp.idx 
                        + DateFilter.getVonBisFilter(trans,von,bis,audit.date) + " " + getKundenFilter()
                        + " order by " + trans.markColumn(audit.date));
                        
                
                List<DBMember> kunden_liste = trans.fetchTable2(member, "where " + trans.markColumn(member.bp_idx) + " = " + bp.idx                       
                                       + " order by " + trans.markColumn(member.name) + ", "  +  trans.markColumn(member.forname));                        

                member_map = new HashMap<>();
                
                for( DBMember k : kunden_liste )
                    member_map.put(k.idx.getValue(),k);
                          
                
                result = true;
            }
        };
        
        return (Boolean)al.result;
    }

    @Override
    public String render() 
    {
        if (member_map == null) {
            if (!collectData()) {
                return "failed";
            }
        }

        clear();

        html_start();

        html_setTitle("Audit Abrechnungszeitraum " + bp.title);

        for (DBAudit a : audit_liste) {
            
            text.append("<i>");
            text.append(a.date.toString());
            text.append("</i> ");
            
            if (a.member_idx.getValue() > 0) {
                html_bold(MemberNameCombo.getName4Member(member_map.get(a.member_idx.getValue())));
                html_normal_text(" ");
            }
            
            html_newline();

            html_normal_text(a.message.getValue());
            

            html_newline();
            html_newline();
        }
        
        html_stop();

        return text.toString();
    }   
}
