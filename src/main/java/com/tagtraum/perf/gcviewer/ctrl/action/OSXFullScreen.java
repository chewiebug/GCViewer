package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Event;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.util.OSXSupport;

/**
 * Used to enter and exit full screen mode on Mac OS X Lion and later.
 * Uses reflection to be compilable on other platforms.
 *
 * @author <a href="mailto:johan@kaving.se">Johan Kaving</a>
 */
public class OSXFullScreen extends AbstractAction {
    private GCViewerGui gcViewer;

    public OSXFullScreen(final GCViewerGui gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_enter_fullscreen"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_enter_fullscreen"));
        putValue(ACTION_COMMAND_KEY, ActionCommands.OSX_FULLSCREEN.toString());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('F', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK));

        try {
            final Class<?> listenerClass = Class.forName("com.apple.eawt.FullScreenListener");

            Object proxy =
                Proxy.newProxyInstance(OSXFullScreen.class.getClassLoader(),
                                       new Class[]{listenerClass},
                                       new InvocationHandler() {
                                           @Override
                                           public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                                               if (method.getName().equals("windowEnteredFullScreen")) {
                                                   putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_leave_fullscreen"));
                                                   putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_leave_fullscreen"));
                                                   gcViewer.getDesktopPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                                                                  "leaveFullScreen");
                                                   gcViewer.getDesktopPane().getActionMap().put("leaveFullScreen", OSXFullScreen.this);
                                               }
                                               if (method.getName().equals("windowExitedFullScreen")) {
                                                   putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_enter_fullscreen"));
                                                   putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_enter_fullscreen"));
                                                   gcViewer.getDesktopPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
                                                   gcViewer.getDesktopPane().getActionMap().remove("leaveFullScreen");
                                               }
                                               return null;
                                           }
                                       });


            Class fullScreenUtilities = Class.forName("com.apple.eawt.FullScreenUtilities");
            Method addFullScreenListenerMethod = fullScreenUtilities.getMethod("addFullScreenListenerTo", Window.class, listenerClass);
            addFullScreenListenerMethod.invoke(fullScreenUtilities, gcViewer, proxy);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(final ActionEvent e) {
        try {
            Object application = OSXSupport.getOSXApplication();
            Method requestToggleFullScreenMethod = application.getClass().getMethod("requestToggleFullScreen", Window.class);
            requestToggleFullScreenMethod.invoke(application, gcViewer);
        } 
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
