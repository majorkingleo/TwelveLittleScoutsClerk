/**
 * TwelveLittleScoutsClerk common functions on table Member 
 * @author Copyright (c) 2023-2024 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.BaseAppConfigDefinitions;
import at.redeye.FrameWork.base.prm.PrmDefaultChecksInterface;
import at.redeye.FrameWork.base.prm.bindtypes.DBConfig;
import at.redeye.FrameWork.base.prm.impl.GlobalConfigDefinitions;
import at.redeye.FrameWork.base.prm.impl.LocalConfigDefinitions;
import at.redeye.FrameWork.base.prm.impl.PrmDefaultCheckSuite;

/**
 *
 * @author martin
 */
public class AppConfigDefinitions extends BaseAppConfigDefinitions {
    
    public static DBConfig UserName = new DBConfig("UserName","");
    public static DBConfig Password = new DBConfig("Password","");    
    public static DBConfig Organisation = new DBConfig("Organisation","Pfadfindergruppe XY","Name of the organisation");
    public static DBConfig OrganisationAddressStreet = new DBConfig("OrganisationAddressStreet","Musterstraße 1","Address of the organisation");
    public static DBConfig OrganisationAddressPostalCode = new DBConfig("OrganisationAddressPostalCode","12345","Address of the organisation");
    public static DBConfig OrganisationAddressCity = new DBConfig("OrganisationAddressCity","Musterstadt","Address of the organisation");
    public static DBConfig OrganisaiontIBAN = new DBConfig("OrganisaiontIBAN","AT00 0000 0000 0000 0000","IBAN of the organisation");    

    public static DBConfig MailBodyTemplateName = new DBConfig("MailBodyTemplateName","","Name of the billing template (DBBillTemplate) for the email body");
    public static DBConfig MailSmtpHost = new DBConfig("MailSmtpHost","localhost","Hostname / IP of the SMTP server");
    public static DBConfig MailSmtpPort = new DBConfig("MailSmtpPort","587","SMTP port (587 = STARTTLS)");
    public static DBConfig MailSmtpStartTls = new DBConfig("MailSmtpStartTls","true","Enable STARTTLS (true/false)");
    public static DBConfig MailFrom = new DBConfig("MailFrom","","Sender email address");
    public static DBConfig MailFromName = new DBConfig("MailFromName","","Sender display name");
    public static DBConfig MailSmtpUser = new DBConfig("MailSmtpUser","","SMTP username");
    public static DBConfig MailSmtpPassword = new DBConfig("MailSmtpPassword","","SMTP password");

    public static DBConfig OpenCommand = new DBConfig("OpenCommand","","Command to open files (e.g. PDF viewer). Use %s as placeholder for the file path. If empty, java.awt.Desktop.open() is used.");

    public static void registerDefinitions() {

        BaseRegisterDefinitions();

        addLocal(UserName);  
        addLocal(Password);  
        add(Organisation);
        add(OrganisationAddressStreet);
        add(OrganisationAddressPostalCode);
        add(OrganisationAddressCity);
        add(OrganisaiontIBAN);
        add(MailBodyTemplateName);
        add(MailSmtpHost);
        add(MailSmtpPort);
        add(MailSmtpStartTls);
        add(MailFrom);
        add(MailFromName);
        addLocal(MailSmtpUser);
        addLocal(MailSmtpPassword);
        add(OpenCommand);
    
        GlobalConfigDefinitions.add_help_path("/at/redeye/twelvelittlescoutsclerk/resources/Help/Params/");
        LocalConfigDefinitions.add_help_path("/at/redeye/twelvelittlescoutsclerk/resources/Help/Params/");
    }    
}
