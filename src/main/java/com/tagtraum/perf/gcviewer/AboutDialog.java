package com.tagtraum.perf.gcviewer;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

import com.tagtraum.perf.gcviewer.util.BuildInfoReader;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.util.UrlDisplayHelper;

/**
 * About dialog showing version and contributors information.
 * 
 * @author Hendrik Schreiber
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public class AboutDialog extends ScreenCenteredDialog implements ActionListener {

    private static final String GCVIEWER_HOMEPAGE = "https://github.com/chewiebug/gcviewer/wiki";

    private static final String ACTION_OK = "ok";
    private static final String ACTION_HOMEPAGE = "homepage";
    
    private JButton homePageButton;
    private JButton okButton;

    public AboutDialog(Frame f) {
        super(f, LocalisationHelper.getString("about_dialog_title"));
        Panel logoPanel = new Panel();
        ImageIcon logoIcon = new ImageIcon(getClass().getResource(LocalisationHelper.getString("about_dialog_image")));
        JLabel la_icon = new JLabel(logoIcon);
        la_icon.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
        logoPanel.add(la_icon);

        JPanel versionPanel = new JPanel();
        versionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        versionPanel.setLayout(new GridBagLayout());
        
        JLabel copyright = new JLabel("\u00A9" + " 2011-2013: Joerg Wuethrich", JLabel.CENTER);
        
        JLabel contributorsLabel = new JLabel("contributors (alphabetically ordered):", JLabel.CENTER);
        contributorsLabel.setForeground(Color.GRAY);
        JLabel contributors = new JLabel(
                "<html><center>Peter Bilstein | Cka3o4Huk | Bernd Eckenfels<br>" +
        		"Neil Gentleman | Johan Kaving | Carl Meyer<br>" +
        		"Reinhard Nägele | Rupesh Ramachandran<br>" +
        		"Serafín Sedano | Andrey Skripalschikov | Yin Xunjun</center><html>",
        		JLabel.CENTER);
        
        JLabel version = new JLabel("<html><font color=\"gray\">version:</font> " + BuildInfoReader.getVersion() + "</html>", JLabel.CENTER);
        JLabel buildDate = new JLabel("<html><font color=\"gray\">build date:</font> " + BuildInfoReader.getBuildDate() + "</html>", JLabel.CENTER);
        
        JLabel spacer1 = new JLabel("spacer");
        spacer1.setForeground(getBackground());
        JLabel spacer2 = new JLabel("spacer");
        spacer2.setForeground(getBackground());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.gridx = 0;

        versionPanel.add(copyright, gridBagConstraints);
        gridBagConstraints.gridy = 1;
        versionPanel.add(spacer1, gridBagConstraints);
        gridBagConstraints.gridy = 2;
        versionPanel.add(contributorsLabel, gridBagConstraints);
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        versionPanel.add(contributors, gridBagConstraints);
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        versionPanel.add(spacer2, gridBagConstraints);
        gridBagConstraints.gridy = 5;
        versionPanel.add(version, gridBagConstraints);
        gridBagConstraints.gridy = 6;
        versionPanel.add(buildDate, gridBagConstraints);
        
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        if (UrlDisplayHelper.displayUrlIsSupported()) {
            homePageButton = new JButton("Homepage");
            homePageButton.setActionCommand(ACTION_HOMEPAGE);
            homePageButton.addActionListener(this);
            buttonPanel.add(homePageButton);
        }

        okButton = new JButton(LocalisationHelper.getString("button_ok"));
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        buttonPanel.add(okButton);
        getContentPane().add("North", logoPanel);
        getContentPane().add("Center", versionPanel);
        getContentPane().add("South", buttonPanel);
        pack();
        setResizable(false);
        setVisible(false);
    }

    public void actionPerformed(ActionEvent e) {
        if (ACTION_HOMEPAGE.equals(e.getActionCommand())) {
            UrlDisplayHelper.displayUrl(this, GCVIEWER_HOMEPAGE);
        }
        else {
            // default action
            super.actionPerformed(e);
        }
    }

}
