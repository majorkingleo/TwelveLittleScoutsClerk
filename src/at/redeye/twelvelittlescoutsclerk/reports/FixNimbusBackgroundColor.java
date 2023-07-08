/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.griesdorn.reports;

import java.awt.Color;
import javax.swing.JEditorPane;
import javax.swing.UIDefaults;

/**
 *
 * @author kingleo
 */
public class FixNimbusBackgroundColor {
 
    public static void fixNimbusBackgroundColor( JEditorPane jReport )
    {
        Color bgColor = new Color(0xffffff);
        UIDefaults defaults = new UIDefaults();
        defaults.put("EditorPane[Enabled].backgroundPainter", bgColor);
        jReport.putClientProperty("Nimbus.Overrides", defaults);
        jReport.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
        jReport.setBackground(bgColor);      
    }
    
}
