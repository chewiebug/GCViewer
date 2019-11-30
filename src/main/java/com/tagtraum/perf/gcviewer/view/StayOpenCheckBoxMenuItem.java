package com.tagtraum.perf.gcviewer.view;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * An extension of JCheckBoxMenuItem that doesn't close the menu when selected.
 *
 * This code was taken from the blog found at https://tips4java.wordpress.com/2010/09/12/keeping-menus-open/.
 *
 * @author Darryl
 */
public class StayOpenCheckBoxMenuItem extends JCheckBoxMenuItem {

    private static MenuElement[] path;

    {
        getModel().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (getModel().isArmed() && isShowing()) {
                    path = MenuSelectionManager.defaultManager().getSelectedPath();
                }
            }
        });
    }

    /**
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem()
     */
    public StayOpenCheckBoxMenuItem() {
        super();
    }

    /**
     * @param a action, where properties are taken from to create MenuItem
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(Action)
     */
    public StayOpenCheckBoxMenuItem(Action a) {
        super(a);
    }

    /**
     * @param icon icon for menu item
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(Icon)
     */
    public StayOpenCheckBoxMenuItem(Icon icon) {
        super(icon);
    }

    /**
     * @param text text for menu item
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String)
     */
    public StayOpenCheckBoxMenuItem(String text) {
        super(text);
    }

    /**
     * @param text text for menu item
     * @param selected initial state of checkbox
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String, boolean)
     */
    public StayOpenCheckBoxMenuItem(String text, boolean selected) {
        super(text, selected);
    }

    /**
     * @param text text for menu item
     * @param icon icon for menu item
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String, Icon)
     */
    public StayOpenCheckBoxMenuItem(String text, Icon icon) {
        super(text, icon);
    }

    /**
     * @param text text for menu item
     * @param icon icon for the menu item
     * @param selected initial state for checkbox
     * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String, Icon, boolean)
     */
    public StayOpenCheckBoxMenuItem(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
    }

    /**
     * Overridden to reopen the menu.
     *
     * @param pressTime the time to "hold down" the button, in milliseconds
     */
    @Override
    public void doClick(int pressTime) {
        super.doClick(pressTime);
        MenuSelectionManager.defaultManager().setSelectedPath(path);
    }
}
