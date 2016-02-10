package com.tagtraum.perf.gcviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

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
    private static final String ACTION_HOMEPAGE = "homepage";

    private static final String[] CONTRIBUTORS = {
        "Hans Bausewein",
        "Peter Bilstein",
        "Steve Boardwell",
        "Cka3o4Huk",
        "Frank Dietrich",
        "Bernd Eckenfels",
        "Ryan Gardner",
        "Neil Gentleman",
        "Michi Gysel",
        "Johan Kaving",
        "Maciej Kwiecien",
        "James Livingston",
        "Tony Mancill",
        "Samuel Mendenhall",
        "Carl Meyer",
        "Reinhard Nägele",
        "Robert Nibali",
        "Àngel Ollé Blázquez",
        "Rupesh Ramachandran",
        "Heiko W. Rupp",
        "Stephan Schroevers",
        "François Secherre",
        "Serafín Sedano",
        "Jugal Shah",
        "Andrey Skripalschikov",
        "Kamil Szymanski",
        "Pierre Viret",
        "Yin Xunjun",
        "Eugene Zimichev"};

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

        JLabel copyright = new JLabel("\u00A9" + " 2011-2015: Joerg Wuethrich and contributors", JLabel.CENTER);

        JLabel contributorsLabel = new JLabel("contributors (alphabetically ordered):", JLabel.CENTER);
        contributorsLabel.setForeground(Color.GRAY);
        JLabel contributors = new JLabel(formatContributors(CONTRIBUTORS), JLabel.CENTER);
        contributors.setPreferredSize(calculatePreferredSize(contributors, true, logoIcon.getIconWidth()));

        JLabel version = new JLabel("<html><font color=\"gray\">version:</font> " + BuildInfoReader.getVersion() + "</html>", JLabel.CENTER);
        JLabel buildDate = new JLabel("<html><font color=\"gray\">build date:</font> " + BuildInfoReader.getBuildDate() + "</html>", JLabel.CENTER);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.gridx = 0;

        versionPanel.add(copyright, gridBagConstraints);

        Insets insetsGapOnTop = new Insets(10, 0, 0, 0);
        GridBagConstraints gridBagConstraintsGapOnTop = new GridBagConstraints();
        gridBagConstraintsGapOnTop.gridy = 1;
        gridBagConstraintsGapOnTop.insets = insetsGapOnTop;
        versionPanel.add(contributorsLabel, gridBagConstraintsGapOnTop);

        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        versionPanel.add(contributors, gridBagConstraints);
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;

        gridBagConstraintsGapOnTop.gridy = 3;
        versionPanel.add(version, gridBagConstraintsGapOnTop);

        gridBagConstraints.gridy = 4;
        versionPanel.add(buildDate, gridBagConstraints);

        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        if (UrlDisplayHelper.displayUrlIsSupported()) {
        JButton homePageButton = new JButton("Homepage");
            homePageButton.setActionCommand(ACTION_HOMEPAGE);
            homePageButton.addActionListener(this);
            buttonPanel.add(homePageButton);
        }

        JButton okButton = new JButton(LocalisationHelper.getString("button_ok"));
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

    private String formatContributors(String[] contributors) {
        StringBuilder sb = new StringBuilder("<html><center>");
        for (String contributor : contributors) {
            sb.append(contributor).append(" | ");
        }

        sb.delete(sb.length() - 3, sb.length());

        sb.append("</center></html>");

        return sb.toString();
    }

    /**
     * Returns the preferred size to set a component at in order to render
     * an html string.  You can specify the size of one dimension.
     *
     * @see http://blog.nobel-joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/
     */
    private Dimension calculatePreferredSize(JLabel labelWithHtmlText, boolean width, int preferredSize) {

        View view = (View) labelWithHtmlText.getClientProperty(BasicHTML.propertyKey);

        view.setSize(width ? preferredSize : 0,
                     width ? 0 : preferredSize);

        float w = view.getPreferredSpan(View.X_AXIS);
        float h = view.getPreferredSpan(View.Y_AXIS);

        return new Dimension((int) Math.ceil(w),
                (int) Math.ceil(h));
    }

    @Override
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
