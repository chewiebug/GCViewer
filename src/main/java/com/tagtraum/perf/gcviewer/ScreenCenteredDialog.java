package com.tagtraum.perf.gcviewer;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 * Base class for modal dialogs that are centered on screen.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.04.2013</p>
 */
public class ScreenCenteredDialog extends JDialog implements ActionListener {

    protected static final String ACTION_OK = "ok";
    
    private Frame frame;

    public ScreenCenteredDialog(Frame f, String title) {
        super(f, title, true);
        
        frame = f;
    }

    protected JRootPane createRootPane() {
        KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        
        JRootPane rootPane = new JRootPane();
        rootPane.registerKeyboardAction(this, escapeStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(this, enterStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        return rootPane;
    }
    
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
        int taskBarSize = scnMax.bottom;
        
        int usableScreenHeight = screenSize.height - taskBarSize - 50;
        
        if (size.height > (usableScreenHeight)) {
            size.height = usableScreenHeight;
        }
        
        return size;
    }

    protected void initComponents() {
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton(LocalisationHelper.getString("button_ok"));
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        buttonPanel.add(okButton);
        
        getContentPane().add("South", buttonPanel);
    }
    
    public void setVisible(boolean visible) {
        if (visible) {
            int x = (int) frame.getLocation().getX() + (frame.getWidth() / 2) - (getWidth() / 2);
            int y = (int) frame.getLocation().getY() + (frame.getHeight() / 2) - (getHeight() / 2);
            setLocation(adjustX(x), adjustY(y));
        }
        
        super.setVisible(visible);
    }

    private int adjustX(int x) {
        if (x < 0) {
            x = 0;
        }
        else if (x + getWidth() > Toolkit.getDefaultToolkit().getScreenSize().getWidth()) {
            x = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - getWidth();
        }
        return x;
    }

    private int adjustY(int y) {
        if (y < 0) {
            y = 0;
        }
        else if (y + getHeight() > Toolkit.getDefaultToolkit().getScreenSize().getHeight()) {
            y = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - getHeight();
        }

        return y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
    }

}
