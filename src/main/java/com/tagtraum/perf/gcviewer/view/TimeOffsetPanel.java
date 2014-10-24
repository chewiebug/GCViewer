package com.tagtraum.perf.gcviewer.view;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.model.PropertyChangeEventConsts;

/**
 * This panel shows a checkbox and a date input field to enter a datetime offset for a gc log.
 * 
 * <p>Date: Sep 24, 2005</p>
 * <p>Time: 5:53:41 PM</p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TimeOffsetPanel extends JPanel {

    private JSpinner timeSpinner;
    private JCheckBox setOffsetCheckBox;
    private static final String OK_ACTION_MAP_KEY = "ok";
    private static final String CANCEL_ACTION_MAP_KEY = "cancel";
    private JPopupMenu popup;

    public TimeOffsetPanel(JPopupMenu popup) {
        this.popup = popup;
        timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, getPattern()));
        setOffsetCheckBox = new JCheckBox(LocalisationHelper.getString("timeoffset_prompt"), true);
        setOffsetCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                timeSpinner.setEnabled(setOffsetCheckBox.isSelected());
                fireTimeOffsetPanelStateChanged(setOffsetCheckBox.isSelected());
            }
        });
        add(setOffsetCheckBox);
        add(timeSpinner);

        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), OK_ACTION_MAP_KEY);
        getActionMap().put(OK_ACTION_MAP_KEY, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TimeOffsetPanel.this.popup.setVisible(false);
                fireTimeOffsetPanelStateChanged(setOffsetCheckBox.isSelected());
            }
        });
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL_ACTION_MAP_KEY);
        getActionMap().put(CANCEL_ACTION_MAP_KEY, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TimeOffsetPanel.this.popup.setVisible(false);
            }
        });
    }

    private void fireTimeOffsetPanelStateChanged(boolean state) {
        firePropertyChange(PropertyChangeEventConsts.TIMEOFFSETPANEL_STATE_CHANGED, 
                !setOffsetCheckBox.isSelected(), 
                setOffsetCheckBox.isSelected());
    }
    
    private String getPattern() {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
        String format = "d. MMM HH:mm.ss a";
        if (dateFormat instanceof SimpleDateFormat) {
            format = ((SimpleDateFormat)dateFormat).toPattern();
        }
        return format;
    }

    public void setDate(Date date) {
        if (date != null) {
            ((SpinnerDateModel)timeSpinner.getModel()).setValue(date);
        }
    }

    public Date getDate() {
        return ((SpinnerDateModel)timeSpinner.getModel()).getDate();
    }

    public boolean isCheckboxSelected() {
        return setOffsetCheckBox.isSelected();
    }

    public void setCheckboxSelected(boolean value) {
        setOffsetCheckBox.setSelected(value);
    }

}
