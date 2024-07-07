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
    public static DBConfig Prime = new DBConfig("Prime","1000","Ausbezahlte Prämie", new PrmDefaultCheckSuite(PrmDefaultChecksInterface.PRM_IS_DOUBLE));
    public static DBConfig PrimeTrainer = new DBConfig("PrimeTrainer","300","Ausbezahlte Prämie für Trainer", new PrmDefaultCheckSuite(PrmDefaultChecksInterface.PRM_IS_DOUBLE));
    public static DBConfig Anzahlung = new DBConfig("Anzahlung","3000","Anzahlung für neue Kunden", new PrmDefaultCheckSuite(PrmDefaultChecksInterface.PRM_IS_DOUBLE));
    
    public static void registerDefinitions() {

        BaseRegisterDefinitions();

        addLocal(UserName);  
        addLocal(Password);  
        add(Prime);
        add(PrimeTrainer);
        add(Anzahlung);

        GlobalConfigDefinitions.add_help_path("/at/redeye/griesdorn/resources/Help/Params/");
        LocalConfigDefinitions.add_help_path("/at/redeye/griesdorn/resources/Help/Params/");
    }    
}
