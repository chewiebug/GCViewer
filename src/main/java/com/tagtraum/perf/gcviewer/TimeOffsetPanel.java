package com.tagtraum.perf.gcviewer;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * TimeOffsetPanel.
 * <p/>
 * Date: Sep 24, 2005
 * Time: 5:53:41 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TimeOffsetPanel extends JPanel {
    private static ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.perf.gcviewer.localStrings");

    private JSpinner timeSpinner;
    private JCheckBox setOffsetCheckBox;
    private int result;
    private static final String OK_ACTION_MAP_KEY = "ok";
    private static final String CANCEL_ACTION_MAP_KEY = "cancel";
    private JPopupMenu popup;
    private Action okAction;

    public TimeOffsetPanel(JPopupMenu popup) {
        this.popup = popup;
        timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, getPattern()));
        //.setLayout(new GridLayout(1, 2, 10, 10));
        setOffsetCheckBox = new JCheckBox(localStrings.getString("timeoffset_prompt"), true);
        setOffsetCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                timeSpinner.setEnabled(setOffsetCheckBox.isSelected());
                okAction.actionPerformed(new ActionEvent(this, 0, null));
            }
        });
        add(setOffsetCheckBox);
        add(timeSpinner);

        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), OK_ACTION_MAP_KEY);
        getActionMap().put(OK_ACTION_MAP_KEY, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                result = JOptionPane.OK_OPTION;
                TimeOffsetPanel.this.popup.setVisible(false);
                if (okAction != null) {
                    okAction.actionPerformed(e);
                }
            }
        });
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL_ACTION_MAP_KEY);
        getActionMap().put(CANCEL_ACTION_MAP_KEY, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                result = JOptionPane.OK_CANCEL_OPTION;
                TimeOffsetPanel.this.popup.setVisible(false);
            }
        });
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
        final SpinnerDateModel spinnerDateModel = ((SpinnerDateModel)timeSpinner.getModel());
        spinnerDateModel.setValue(date);
    }

    public Date getDate() {
        final SpinnerDateModel spinnerDateModel = ((SpinnerDateModel)timeSpinner.getModel());
        return spinnerDateModel.getDate();
    }

    public boolean isOffsetSet() {
        return setOffsetCheckBox.isSelected();
    }

    public void setOffsetSet(boolean value) {
        setOffsetCheckBox.setSelected(value);
    }

    public int getResult() {
        return result;
    }

    public void setOkAction(Action okAction) {
        this.okAction = okAction;
    }
}
