package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.WatchDogReloadDelayView;

/**
 *
 * @author <a href="mailto:web@olleb.com">Àngel Ollé Blázquez</a>
 *
 *
 */
public class WatchDogReloadDelay extends AbstractAction {
	private static final long serialVersionUID = -4527644600732531177L;
	private final WatchDogReloadDelayView view;
	private final GCViewerGui gui;

	public WatchDogReloadDelay(final GCViewerGui gui) {
		this.gui = gui;
		int delay = RefreshWatchDog.getRELOAD_DELAY();
		this.view = new WatchDogReloadDelayView(gui, delay);

		putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_watchdog"));
		putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_watchdog"));
		putValue(MNEMONIC_KEY,
				new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_watchdog").charAt(0)));
		putValue(ACTION_COMMAND_KEY, ActionCommands.ARRANGE.toString());
		setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this.view.showDialog()) {
			int delay = this.view.getReloadDelayValue();
			RefreshWatchDog.setRELOAD_DELAY(delay);
			GCDocument gcDocument = gui.getSelectedGCDocument();
			if (gcDocument != null) {
				Watch watch = (Watch) this.gui.getActionMap().get(ActionCommands.WATCH.toString());
				watch.restartIfRunning();
			}
		}
	}
}
