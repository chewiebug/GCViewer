package com.tagtraum.perf.gcviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.SwingPropertyChangeSupport;

import com.tagtraum.perf.gcviewer.log.TextAreaLogHandler;


/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * <p>Date: May 5, 2005<br/>
 * Time: 2:14:36 PM</p>
 *
 */
public class ChartPanelView {

    private static final ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.perf.gcviewer.localStrings");
    private static final DataReaderFactory factory = new DataReaderFactory();

    private ModelChartImpl modelChart;
    private ModelPanel modelPanel;
    private GCModel model;
    private ViewBar viewBar;
    private boolean viewBarVisible;
    private boolean minimized;
    private SwingPropertyChangeSupport propertyChangeSupport;
    private GCDocument gcDocument;
    private TextAreaLogHandler textAreaLogHandler;
    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");


    public ChartPanelView(GCDocument gcDocument, URL url) throws IOException {
        this.gcDocument = gcDocument;
        this.modelChart = new ModelChartImpl();
        this.modelPanel = new ModelPanel();
        this.viewBar = new ViewBar(this);
        this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
        this.textAreaLogHandler = new TextAreaLogHandler();
        final GCModel model = loadModel(url);
        setModel(model);
        // TODO delete
        model.printPauseMaps();
    }

    /**
     * @return true, if the files has been reloaded
     * @throws IOException
     */
    public boolean reloadModel() throws IOException {
        if (model.getURL() == null) return false;
        if (model.isDifferent(model.getURL())) {
            setModel(loadModel(this.model.getURL()));
            return true;
        }
        return false;
    }

    private GCModel loadModel(final URL url) throws IOException {
        // set up special handler
        textAreaLogHandler = new TextAreaLogHandler();
        IMP_LOGGER.addHandler(textAreaLogHandler);
        DATA_READER_FACTORY_LOGGER.addHandler(textAreaLogHandler);
        try {
            final InputStream in = url.openStream();
            final DataReader reader = factory.getDataReader(in);
            final GCModel model = reader.read();
            model.setURL(url);
            if (textAreaLogHandler.hasErrors()) {
                // show error dialog
                final JPanel panel = new JPanel(new BorderLayout());
                final JLabel messageLabel = new JLabel(new MessageFormat(localStrings.getString("datareader_parseerror_dialog_message")).format(new Object[]{textAreaLogHandler.getErrorCount(), url}));
                messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                panel.add(messageLabel, BorderLayout.NORTH);
                final JScrollPane textAreaScrollPane = new JScrollPane(textAreaLogHandler.getTextArea());
                textAreaScrollPane.setPreferredSize(new Dimension(700, 500));
                panel.add(textAreaScrollPane, BorderLayout.CENTER);
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        JOptionPane.showMessageDialog(null, panel, new MessageFormat(localStrings.getString("datareader_parseerror_dialog_title")).format(new Object[]{url}), JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
            return model;
        }
        finally {
            // remove special handler after we are done with reading.
            IMP_LOGGER.removeHandler(textAreaLogHandler);
            DATA_READER_FACTORY_LOGGER.removeHandler(textAreaLogHandler);
        }
    }

    public void invalidate() {
        modelChart.invalidate();
        modelPanel.invalidate();
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
    }

    public ViewBar getViewBar() {
        return viewBar;
    }

    public JTextArea getParseLog() {
        return textAreaLogHandler.getTextArea();
    }

    public boolean isViewBarVisible() {
        return viewBarVisible;
    }

    public void setViewBarVisible(boolean viewBarVisible) {
        this.viewBarVisible = viewBarVisible;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void setMinimized(boolean minimized) {
        boolean oldValue = this.minimized;
        if (minimized != this.minimized) {
            this.minimized = minimized;
            propertyChangeSupport.firePropertyChange("minimized", oldValue, minimized);
        }
    }

    public ModelChart getModelChart() {
        return modelChart;
    }

    public ModelPanel getModelPanel() {
        return modelPanel;
    }

    public GCModel getModel() {
        return model;
    }

    public void setModel(GCModel model) {
        this.model = model;
        this.modelPanel.setModel(model);
        this.modelChart.setModel(model);
        this.viewBar.setTitle(model.getURL().toString());
    }

    public void close() {
        gcDocument.removeChartPanelView(this);
    }

    private static class ViewBar extends JPanel {
        private JLabel title = new JLabel();
        private ViewBarButton closeButton = new ViewBarButton("images/close.png", "images/close_selected.png");
        private MinMaxButton minimizeButton = new MinMaxButton();
        private ChartPanelView chartPanelView;

        public ViewBar(ChartPanelView chartPanelView) {
            this.chartPanelView = chartPanelView;
            setLayout(new GridBagLayout());
            this.title.setOpaque(false);
            this.title.setHorizontalAlignment(SwingConstants.LEFT);
            this.title.setFont(this.title.getFont().deriveFont(this.title.getFont().getSize2D()*0.8f));
            //minimize.set
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.weightx = 2.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            add(this.title, gridBagConstraints);
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.fill = GridBagConstraints.VERTICAL;
            add(minimizeButton, gridBagConstraints);
            gridBagConstraints.gridx = 2;
            add(closeButton, gridBagConstraints);

            minimizeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    ViewBar.this.chartPanelView.setMinimized(!ViewBar.this.chartPanelView.isMinimized());
                }
            });
            closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    ViewBar.this.chartPanelView.close();
                }
            });
        }

        public void setTitle(String title) {
            this.title.setText(title);
        }

        protected void paintComponent(Graphics graphics) {
            //super.paintComponent(graphics);
            // paint background
            GradientPaint gradientPaint = new GradientPaint(0, 0, getBackground().darker(), 0, getHeight()/2.0f, getBackground().brighter(), true);
            Color color = graphics.getColor();
            final Graphics2D graphics2D = (Graphics2D)graphics;
            graphics2D.setPaint(gradientPaint);
            graphics2D.fillRect(0, 0, getWidth(), getHeight());
            graphics2D.setColor(color);
        }

        private static class ViewBarButton extends JButton {
            public ViewBarButton(String image1, String image2) {
                final ImageIcon imageIcon1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(image1)));
                final ImageIcon imageIcon2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(image2)));
                setIcons(imageIcon1, imageIcon2);
                setMargin(new Insets(0,2,0,2));
                setRolloverEnabled(true);
                setBorderPainted(false);
                setOpaque(false);
            }

            public void setIcons(final ImageIcon imageIcon1, final ImageIcon imageIcon2) {
                setIcon(imageIcon1);
                setRolloverIcon(imageIcon2);
                setSelectedIcon(imageIcon2);
            }

        }
        private static class MinMaxButton extends JButton {
            private final ImageIcon min1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/minimize.png")));
            private final ImageIcon min2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/minimize_selected.png")));
            private final ImageIcon max1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/maximize.png")));
            private final ImageIcon max2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/maximize_selected.png")));
            private boolean minimize = true;
            public MinMaxButton() {
                setIcons(min1, min2);
                setMargin(new Insets(0,2,0,2));
                setRolloverEnabled(true);
                setBorderPainted(false);
                setOpaque(false);
                addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent event) {
                        if (minimize) {
                            setIcons(max1, max2);
                        }
                        else {
                            setIcons(min1, min2);
                        }
                        minimize = !minimize;
                    }
                });
            }

            public void setIcons(final ImageIcon imageIcon1, final ImageIcon imageIcon2) {
                setIcon(imageIcon1);
                setRolloverIcon(imageIcon2);
                setSelectedIcon(imageIcon2);
            }

        }
    }

}
