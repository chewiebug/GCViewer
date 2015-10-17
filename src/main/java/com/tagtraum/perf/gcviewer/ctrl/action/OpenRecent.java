package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.model.GCResourceGroup;

/**
 * <p>Action to open an entry of the recent urls menu.</p>
 * <ul><li>Date: Sep 25, 2005</li>
 * <li>Time: 11:16:49 PM</li></ul>
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
