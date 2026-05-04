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
    public static DBConfig Organisation = new DBConfig("Organisation","Pfadfindergruppe XY","Name der Organisation");
    public static DBConfig OrganisationAddressStreet = new DBConfig("OrganisationAddressStreet","Musterstraße 1","Adresse der Organisation");
    public static DBConfig OrganisationAddressPostalCode = new DBConfig("OrganisationAddressPostalCode","12345","Adresse der Organisation");
    public static DBConfig OrganisationAddressCity = new DBConfig("OrganisationAddressCity","Musterstadt","Adresse der Organisation");
    public static DBConfig OrganisaiontIBAN = new DBConfig("OrganisaiontIBAN","AT00 0000 0000 0000 0000","IBAN der Organisation");    
    
    public static void registerDefinitions() {

        BaseRegisterDefinitions();

        addLocal(UserName);  
        addLocal(Password);  
        add(Organisation);
        add(OrganisationAddressStreet);
        add(OrganisationAddressPostalCode);
        add(OrganisationAddressCity);
        add(OrganisaiontIBAN);
    
        GlobalConfigDefinitions.add_help_path("/at/redeye/twelvelittlescoutsclerk/resources/Help/Params/");
        LocalConfigDefinitions.add_help_path("/at/redeye/twelvelittlescoutsclerk/resources/Help/Params/");
    }    
}
