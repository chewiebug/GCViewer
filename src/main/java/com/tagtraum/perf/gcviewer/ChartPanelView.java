package com.tagtraum.perf.gcviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.SwingPropertyChangeSupport;

import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.imp.MonitoredBufferedInputStream;
import com.tagtraum.perf.gcviewer.log.TextAreaLogHandler;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;


/**
 * This class holds all chart and model data panels and provides them to {@link GCDocument}
 * for layouting.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * <p>Date: May 5, 2005<br/>
 * Time: 2:14:36 PM</p>
 */
public class ChartPanelView {

    public static final String EVENT_MINIMIZED = "minimized";
    private static Logger LOG = Logger.getLogger(ChartPanelView.class.getName());
    
    /**
     * Loads the model in a background thread.
     *
     * @author Hans Bausewein
     * <p>Date: November 8, 2013</p>
     */
    private class GCModelLoader extends SwingWorker<GCModel, Object> implements MonitoredBufferedInputStream.ProgressCallback {
    	
        private final DataReaderFacade dataReaderFacade;
    	private final GCDocument gcDocument;
    	private final URL url;		
		private boolean showParserErrors;
		private AtomicReference<GCModel> modelRef = new AtomicReference<GCModel>();
	    private final Logger parserLogger;
    	
        public GCModelLoader(final GCDocument gcDocument, 
        				 	 final URL url, 
        				 	 final boolean showParserErrors) {
			super();

			this.gcDocument = gcDocument;
			this.dataReaderFacade = gcDocument.getGcViewer().getDataReaderFacade();
			this.url = url;
			this.showParserErrors = showParserErrors;
			GCModel model = new GCModel(true);
			model.setURL(url);
			this.modelRef.set(model);

			if (url != null) {
				// A static logger would not be reliable, when concurrently used
				// Solution: create a logger per url instance, or better: per GCModel	        	
				parserLogger = Logger.getLogger(DataReaderFacade.class.getSimpleName() + "_" + url.hashCode());
		        parserLogger.addHandler(textAreaLogHandler);
			start();
			} else {
				parserLogger = null;
			}
		}
        
        public String getLoggerName() {
			return parserLogger == null ? "NONE" : parserLogger.getName();
		}

        public GCModelLoader(final GCModelLoader prevModel,
        					 final URL url, 
			 	 			 final boolean showParserErrors) {
			this(prevModel.getGcDocument(), url, showParserErrors);
        }
        
		@Override
		protected GCModel doInBackground() throws Exception {
			setProgress(0);
	        final GCModel model = dataReaderFacade.loadModel(url, this);
	        model.setURL(url);
			return model;
		}

		protected void done() {
			GCModel model = null;
            // remove special handler after we are done with reading.
            parserLogger.removeHandler(textAreaLogHandler);

			try {
				model = get();
			} catch (InterruptedException e) {
				LOG.log(Level.FINE, "model get() interrupted", e);
			} catch (ExecutionException e) {
				LOG.log(Level.WARNING, "Failed to create ChartPanelView GCModel for " + url.toExternalForm(), e);			
			}
			setProgress(100);
			
			if (model != null) {
				modelRef.set(model);
				setModel(model);
				
				// TODO delete
				model.printDetailedInformation();				
			} else {
				final int nChartPanelViews = gcDocument.removeChartPanelView(ChartPanelView.this);
				
		        if (textAreaLogHandler.hasErrors() && showParserErrors) {
		        	final Component parent = nChartPanelViews == 0 ? gcDocument.getGcViewer().getDesktopPane() : gcDocument;
		        	if (LOG.isLoggable(Level.FINE)) 
		        		LOG.fine("Show error for " + url);
		            showErrorDialog(url, textAreaLogHandler, parent);
		        }
			}
		}
		
		public void start() {
            Executor executor = Executors.newSingleThreadExecutor(dataReaderFacade.getThreadFactory());
            executor.execute(this);			
		}

		public GCModel getModel() {
			return modelRef.get();
		}

		public GCDocument getGcDocument() {
			return gcDocument;
		}

		@Override
		public void publishP(Integer... chunks) {			
			if (LOG.isLoggable(Level.FINE))
				LOG.fine("Received " + Arrays.asList(chunks));
		}

		@Override
		public void updateProgress(int progress) {
			setProgress(progress);			
		}    	
    }
    
    private GCPreferences preferences;
    
    private final ModelChartImpl modelChart;
    private final ModelPanel modelPanel;
    private final ModelDetailsPanel modelDetailsPanel;
    private final JTabbedPane modelChartAndDetailsPanel;
    private final ViewBar viewBar;
    private final SwingPropertyChangeSupport propertyChangeSupport;
    private final TextAreaLogHandler textAreaLogHandler;
    private GCModelLoader modelLoader;
    private boolean viewBarVisible;
    private boolean minimized;
    
    public ChartPanelView(GCDocument gcDocument, URL url) throws DataReaderException {    	
        this.textAreaLogHandler = new TextAreaLogHandler();
        this.modelLoader = new GCModelLoader(gcDocument, url, true);
        this.modelDetailsPanel = new ModelDetailsPanel();
        this.modelChart = new ModelChartImpl();
        modelLoader.addPropertyChangeListener(modelChart);
        this.preferences = gcDocument.getPreferences();
        this.modelPanel = new ModelPanel();
        
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
        
        this.viewBar = new ViewBar(this);
        this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
    }

    /**
     * Show error dialog containing all information related to the error.
     * @param url url where data should have been read from
     * @param textAreaLogHandler handler where all logging information was gathered
     * @param parent parent component for the dialog
     */
    private void showErrorDialog(final URL url, final TextAreaLogHandler textAreaLogHandler, final Component parent) {
        final JPanel panel = new JPanel(new BorderLayout());
        final JLabel messageLabel = new JLabel(new MessageFormat(LocalisationHelper.getString("datareader_parseerror_dialog_message")).format(new Object[]{textAreaLogHandler.getErrorCount(), url}));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(messageLabel, BorderLayout.NORTH);
        final JScrollPane textAreaScrollPane = new JScrollPane(textAreaLogHandler.getTextArea());
        textAreaScrollPane.setPreferredSize(new Dimension(700, 500));
        panel.add(textAreaScrollPane, BorderLayout.CENTER);
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                JOptionPane.showMessageDialog(parent, panel, new MessageFormat(LocalisationHelper.getString("datareader_parseerror_dialog_title")).format(new Object[]{url}), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Reloads the model displayed in this chart panel if it has changed. Using the parameter
     * the parser error dialog can be suppressed.
     * 
     * @param showParserErrors if <code>true</code> parser errors will be shown
     * @return <code>true</code>, if the file has been reloaded
     * @throws DataReaderException if something went wrong reading the file
     */
    public boolean reloadModel(boolean showParserErrors) throws DataReaderException {
    	final GCModel model = getModel();

    	if (modelLoader.getState() != SwingWorker.StateValue.DONE) {
    		// do not start another reload, while loading (or should we cancel the current load operation?)
    		LOG.info("Ignored \"reloadModel\" request, because modelLoader is busy loading \"" + model.getURL() + "\" (at " + modelLoader.getProgress() + " %)");
    		return false; // re-layout will be done, when loading completed
    	}
    	
    	final URL newURL = (model == null) ? null : model.getURL();
        
        if ((newURL != null) && model.isDifferent(newURL)) {
        	modelLoader = new GCModelLoader(modelLoader, newURL, showParserErrors);
            modelLoader.addPropertyChangeListener(modelChart);
            return true;
		}
        return false;
    }

    public void invalidate() {
        modelChart.invalidate();
        modelPanel.invalidate();
        modelDetailsPanel.invalidate();
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
            propertyChangeSupport.firePropertyChange(EVENT_MINIMIZED, oldValue, minimized);
        }
    }

    public JTabbedPane getModelChartAndDetails() {
        return modelChartAndDetailsPanel;
    }
    
    public ModelChart getModelChart() {
        return modelChart;
    }

    public ModelPanel getModelPanel() {
        return modelPanel;
    }

    public GCModel getModel() {
        return modelLoader.getModel();
    }

    public void setModel(GCModel model) {
        this.modelPanel.setModel(model);
        this.modelChart.setModel(model, preferences);
        this.modelDetailsPanel.setModel(model);
        this.viewBar.setTitle(model.getURL().toString());
        this.modelLoader.getGcDocument().relayout();
    }

    public void close() {
    	modelLoader.getGcDocument().removeChartPanelView(this);
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
