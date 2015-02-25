package com.tagtraum.perf.gcviewer.view;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.SwingPropertyChangeSupport;

import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.model.GCPreferences;
import com.tagtraum.perf.gcviewer.view.util.ImageHelper;


/**
 * This class holds all chart and model data panels and provides them to {@link GCDocument}
 * for layouting.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ChartPanelView implements PropertyChangeListener {

    public static final String EVENT_MINIMIZED = "minimized";
    public static final String EVENT_CLOSED = "closed";
    
    static Logger LOG = Logger.getLogger(ChartPanelView.class.getName());
    
    private GCPreferences preferences;
    
    private final ModelChartImpl modelChart;
    private final ModelMetricsPanel modelMetricsPanel;
    private final ModelDetailsPanel modelDetailsPanel;
    private GCModelLoaderView modelLoaderView;
    private final JTabbedPane modelChartAndDetailsPanel;
    private final ViewBar viewBar;
    private final SwingPropertyChangeSupport propertyChangeSupport;
    private GCResource gcResource;
    private boolean viewBarVisible;
    private boolean minimized;
    
    public ChartPanelView(final GCPreferences preferences, final GCResource gcResource) {
    	this.gcResource = gcResource;
        this.modelDetailsPanel = new ModelDetailsPanel();
        this.modelChart = new ModelChartImpl();
        this.preferences = preferences;
        this.modelMetricsPanel = new ModelMetricsPanel();
        this.modelLoaderView = new GCModelLoaderView(gcResource);
        
        JScrollPane modelDetailsScrollPane = new JScrollPane(modelDetailsPanel, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JScrollBar hScrollBar = modelDetailsScrollPane.getHorizontalScrollBar();
        hScrollBar.setUnitIncrement(10);
        JScrollBar vScrollBar = modelDetailsScrollPane.getVerticalScrollBar();
        vScrollBar.setUnitIncrement(10);

        this.modelChartAndDetailsPanel = new JTabbedPane();
        this.modelChartAndDetailsPanel.addTab(LocalisationHelper.getString("data_panel_tab_chart"), modelChart);
        this.modelChartAndDetailsPanel.addTab(LocalisationHelper.getString("data_panel_tab_details"), modelDetailsScrollPane);
        this.modelChartAndDetailsPanel.addTab(LocalisationHelper.getString("data_panel_tab_parser"), modelLoaderView);
        
        this.viewBar = new ViewBar(this);
        this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
        
        setGcResource(gcResource);
        updateTabDisplay(gcResource);
    }

    public void invalidate() {
        modelChart.invalidate();
        modelMetricsPanel.invalidate();
        modelDetailsPanel.invalidate();
        modelLoaderView.invalidate();
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
    }

    public ViewBar getViewBar() {
        return viewBar;
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
            propertyChangeSupport.firePropertyChange(EVENT_MINIMIZED, oldValue, minimized);
        }
    }

    public JTabbedPane getModelChartAndDetails() {
        return modelChartAndDetailsPanel;
    }

    public ModelChart getModelChart() {
        return modelChart;
    }

    public ModelMetricsPanel getModelMetricsPanel() {
        return modelMetricsPanel;
    }

    public GCModelLoaderView getModelLoaderView() {
        return modelLoaderView;
    }
    
    public GCResource getGCResource() {
        return gcResource;
    }

    public void setGcResource(GCResource gcResource) {
        if (this.gcResource != null) {
            this.gcResource.removePropertyChangeListener(this);
        }
        
        this.gcResource = gcResource;
        this.gcResource.addPropertyChangeListener(this);

        updateModel(gcResource);
    }

    public void close() {
        propertyChangeSupport.firePropertyChange(EVENT_CLOSED, false, true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO if there were parser warnings, change color of parser tab
        if (evt.getSource() instanceof GCResource
                && GCResource.PROPERTY_MODEL.equals(evt.getPropertyName())) {
            
            GCResource gcResource = (GCResource) evt.getSource();
            updateModel(gcResource);
            updateTabDisplay(gcResource);
        }
    }
    
    private void updateTabDisplay(GCResource gcResource) {
        // enable only "parser" panel, as long as model contains no data
        boolean modelHasData = gcResource.getModel() != null && gcResource.getModel().size() > 0;
        for (int i = 0; i < modelChartAndDetailsPanel.getTabCount(); ++i) {
            modelChartAndDetailsPanel.setEnabledAt(i, 
                    modelHasData
                    || modelChartAndDetailsPanel.getTitleAt(i).equals(
                            LocalisationHelper.getString("data_panel_tab_parser")));
        }
        
        if (!gcResource.isReload()) {
            if (modelHasData) {
                modelChartAndDetailsPanel.setSelectedIndex(0);
            }
            else {
                modelChartAndDetailsPanel.setSelectedIndex(modelChartAndDetailsPanel.getTabCount()-1);
            }
        }
    }
    
    private void updateModel(GCResource gcResource) {
        this.modelMetricsPanel.setModel(gcResource.getModel());
        this.modelChart.setModel(gcResource.getModel(), preferences);
        this.modelDetailsPanel.setModel(gcResource.getModel());
        this.viewBar.setTitle(gcResource.getResourceName());
    }

    private static class ViewBar extends JPanel {
        private JLabel title = new JLabel();
        private ViewBarButton closeButton = new ViewBarButton("close.png", "close_selected.png");
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
                final ImageIcon imageIcon1 = ImageHelper.loadImageIcon(image1);
                final ImageIcon imageIcon2 = ImageHelper.loadImageIcon(image2);
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
            private final ImageIcon min1 = ImageHelper.loadImageIcon("minimize.png");
            private final ImageIcon min2 = ImageHelper.loadImageIcon("minimize_selected.png");
            private final ImageIcon max1 = ImageHelper.loadImageIcon("maximize.png");
            private final ImageIcon max2 = ImageHelper.loadImageIcon("maximize_selected.png");
            private boolean minimize = true;
            
            public MinMaxButton() {
                setIcons(min1, min2);
                setMargin(new Insets(0,2,0,2));
                setRolloverEnabled(true);
                setBorderPainted(false);
                setOpaque(false);
                
                addActionListener(new ActionListener() {
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
