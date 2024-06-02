/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.widgets.helpwindow.HelpWinModal;

/**
 *
 * @author martin
 */
public class LocalHelpWinModal extends HelpWinModal {

    public LocalHelpWinModal(Root root, String Module) {
        super( root, "/at/redeye/griesdorn/resources/Help/", Module );
    }
    
}
