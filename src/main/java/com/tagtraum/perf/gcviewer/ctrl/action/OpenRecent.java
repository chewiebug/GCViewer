package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.model.GCResourceGroup;

/**
 * Action to open an entry of the recent urls menu.
 * <p/>
 * Date: Sep 25, 2005
 * Time: 11:16:49 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class OpenRecent extends AbstractAction {

    private GCResourceGroup gcResourceGroup;
    private GCModelLoaderController controller;

    public OpenRecent(GCModelLoaderController controller, GCResourceGroup gcResourceGroup) {
        this.gcResourceGroup = gcResourceGroup;
        this.controller = controller;
        
        putValue(ACTION_COMMAND_KEY, ActionCommands.OPEN_RECENT.toString());
        putValue(NAME, gcResourceGroup.getGroupStringShort());
    }

    public void actionPerformed(ActionEvent e) {
        controller.open(gcResourceGroup.getGCResourceList());
    }

}
