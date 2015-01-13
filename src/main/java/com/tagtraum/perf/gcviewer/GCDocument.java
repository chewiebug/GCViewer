package com.tagtraum.perf.gcviewer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.tagtraum.perf.gcviewer.imp.DataReaderException;

/**
 * GCDocument.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class GCDocument extends JInternalFrame {

    private List<ChartPanelView> chartPanelViews = new ArrayList<ChartPanelView>();
    private ModelChart modelChartListFacade;
    private boolean showModelPanel = true;
    private boolean watched;
    private RefreshWatchDog refreshWatchDog;
    private GCPreferences preferences;

    public GCDocument(final GCViewerGui gcViewer, String s) {
        super(s, true, true, true, false);
        this.refreshWatchDog = new RefreshWatchDog();
        refreshWatchDog.setGcDocument(this);
        preferences = gcViewer.getPreferences();
        showModelPanel = preferences.isShowDataPanel();
        modelChartListFacade = new MultiModelChartFacade();
        addComponentListener(new ResizeListener());
        GridBagLayout layout = new GridBagLayout();
        getContentPane().setLayout(layout);
        // TODO refactor; looks very similar to DesktopPane implementation
        getContentPane().setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetListener(){
            public void dragEnter(DropTargetDragEvent e) {
                if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    e.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    e.rejectDrag();
                }
            }

            public void dragOver(DropTargetDragEvent dtde) {
            }

            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            public void dragExit(DropTargetEvent dte) {
            }

            public void drop(DropTargetDropEvent e) {
                try {
                    Transferable tr = e.getTransferable();
                    if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        e.acceptDrop(DnDConstants.ACTION_COPY);
                        List<Object> list = (List<Object>)tr.getTransferData(DataFlavor.javaFileListFlavor);
                        File[] files = list.toArray(new File[list.size()]);
                        gcViewer.add(files);
                        GCDocument.this.getContentPane().invalidate();
                        e.dropComplete(true);
                    } else {
                        e.rejectDrop();
                    }
                } catch (IOException ioe) {
                    e.rejectDrop();
                    ioe.printStackTrace();
                } catch (UnsupportedFlavorException ufe) {
                    e.rejectDrop();
                    ufe.printStackTrace();
                }
            }
        }));

    }

    public RefreshWatchDog getRefreshWatchDog() {
        return refreshWatchDog;
    }

    public boolean isShowModelPanel() {
        return showModelPanel;
    }

    public void setShowModelPanel(boolean showModelPanel) {
        boolean mustRelayout = this.showModelPanel != showModelPanel;
        this.showModelPanel = showModelPanel;
        if (mustRelayout) {
            relayout();
        }
    }

    /**
     * Reload Models.
     *
     * @return true, if any of the files has been reloaded
     * @param background true if relayout should be skipped.
     * @throws DataReaderException if something went wrong reading the data
     */
    public boolean reloadModels(boolean background) throws DataReaderException {
        boolean reloaded = false;
        for (ChartPanelView chartPanelView : chartPanelViews) {
            reloaded |= chartPanelView.reloadModel(!refreshWatchDog.isRunning());
        }
        if (!background) {
            relayout();
        }
        return reloaded;
    }

    public ModelChart getModelChart() {
        return modelChartListFacade;
    }

    public GCPreferences getPreferences() {
        return preferences;
    }

    public void add(final URL url) throws DataReaderException {
        ChartPanelView chartPanelView = new ChartPanelView(this, url);
        chartPanelViews.add(chartPanelView);
        chartPanelView.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (ChartPanelView.EVENT_MINIMIZED.equals(event.getPropertyName())) {
                    relayout();
                }
            }
        });
        // make sure all models in one document have the same display properties
        if (chartPanelViews.size() > 1) {
            modelChartListFacade.setScaleFactor(modelChartListFacade.getScaleFactor());
            modelChartListFacade.setShowFullGCLines(modelChartListFacade.isShowFullGCLines());
            modelChartListFacade.setShowGCTimesLine(modelChartListFacade.isShowGCTimesLine());
            modelChartListFacade.setShowGCTimesRectangles(modelChartListFacade.isShowGCTimesRectangles());
            modelChartListFacade.setShowIncGCLines(modelChartListFacade.isShowIncGCLines());
            modelChartListFacade.setShowTotalMemoryLine(modelChartListFacade.isShowTotalMemoryLine());
            modelChartListFacade.setShowUsedMemoryLine(modelChartListFacade.isShowUsedMemoryLine());
        }
        relayout();
    }

    public void removeChartPanelView(ChartPanelView chartPanelView) {
        chartPanelViews.remove(chartPanelView);
        relayout();
    }

    public void relayout() {
        getContentPane().removeAll();
        if (chartPanelViews.size() > 1) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < chartPanelViews.size(); i++) {
                sb.append(chartPanelViews.get(i).getModel().getURL().getFile());
                if (i + 1 < chartPanelViews.size()) sb.append(", ");
            }
            setTitle(sb.toString());
        } else if (!chartPanelViews.isEmpty())
            setTitle(chartPanelViews.get(0).getModel().getURL().toString());
        else
            setTitle("");
        int row = 0;
        boolean noMaximizedChartPanelView = true;
        ChartPanelView lastMaximizedChartPanelView = getLastMaximizedChartPanelView();
        MasterViewPortChangeListener masterViewPortChangeListener = new MasterViewPortChangeListener();

        for (int i = 0; i < chartPanelViews.size(); i++) {
            final ChartPanelView chartPanelView = chartPanelViews.get(i);
            final ModelChartImpl modelChart = (ModelChartImpl) chartPanelView.getModelChart();
            final ModelPanel modelPanel = chartPanelView.getModelPanel();
            final JTabbedPane modelChartAndDetails = chartPanelView.getModelChartAndDetails();
            modelChart.resetPolygonCache();
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.NORTH;

            constraints.gridy = row;
            if (chartPanelViews.size() > 1 || (chartPanelView.isMinimized() && chartPanelViews.size() == 1)) {
                constraints.gridwidth = 2;
                constraints.weightx = 2;
                //constraints.weighty = 1;
                getContentPane().add(chartPanelView.getViewBar(), constraints);
                row++;
            }
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridy = row;
            constraints.gridwidth = 1;
            constraints.gridheight = 1; //2
            constraints.gridx = 0;
            constraints.weightx = 2;
            constraints.weighty = 2;
            modelChart.setPreferredSize(new Dimension(800, 600));
            modelChartAndDetails.setVisible(!chartPanelView.isMinimized());
            getContentPane().add(modelChartAndDetails, constraints);

            constraints.gridy = row;
            constraints.gridheight = 1;
            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.weighty = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.SOUTH;
            getContentPane().add(modelPanel, constraints);
            modelPanel.setVisible(showModelPanel && (!chartPanelView.isMinimized()));

            if (!chartPanelView.isMinimized()) {
                noMaximizedChartPanelView = false;
                final boolean isLastMaximizedChartPanelView = lastMaximizedChartPanelView == chartPanelView;
                // lock viewports with each other...
                // remove old master listeners
                final JViewport viewport = modelChart.getViewport();
                lockChartsToOneScrollbar(viewport, isLastMaximizedChartPanelView, modelChart, masterViewPortChangeListener);

                final JScrollBar horizontalScrollBar = modelChart.getHorizontalScrollBar();
                // clean old change listeners
                ChangeListener[] changeListeners = ((DefaultBoundedRangeModel) horizontalScrollBar.getModel()).getChangeListeners();
                for (int j = 0; j < changeListeners.length; j++) {
                    if (changeListeners[j] instanceof ScrollBarMaximumChangeListener) {
                        horizontalScrollBar.getModel().removeChangeListener(changeListeners[j]);
                    }
                }
                if (isLastMaximizedChartPanelView && isWatched()) {
                    horizontalScrollBar.getModel().addChangeListener(new ScrollBarMaximumChangeListener());
                }
                if (isLastMaximizedChartPanelView) {
                    horizontalScrollBar.setEnabled(!isWatched());
                }
            }
            row++;
        }
        if (noMaximizedChartPanelView) {
            // add dummy panel
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridy = row;
            constraints.weightx = 3;
            constraints.weighty = 3;
            getContentPane().add(new JPanel(), constraints);
        }
        scaleModelChart();
        invalidate();
    }

    private void lockChartsToOneScrollbar(final JViewport viewport, final boolean lastMaximizedChartPanelView, final ModelChartImpl modelChart, MasterViewPortChangeListener masterViewPortChangeListener) {
        ChangeListener[] changeListeners = viewport.getChangeListeners();
        for (int j = 0; j < changeListeners.length; j++) {
            if (changeListeners[j] instanceof MasterViewPortChangeListener) {
                viewport.removeChangeListener(changeListeners[j]);
            }
        }
        if (lastMaximizedChartPanelView) {
            modelChart.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            // add scrollbar listener
            viewport.addChangeListener(masterViewPortChangeListener);
        } else {
            modelChart.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            masterViewPortChangeListener.addSlaveViewport(viewport);
        }
    }

    public int getChartPanelViewCount() {
        return chartPanelViews.size();
    }

    public ChartPanelView getChartPanelView(int i) {
        return chartPanelViews.get(i);
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
        final JScrollBar horizontalScrollBar = ((ModelChartImpl) getLastMaximizedChartPanelView().getModelChart()).getHorizontalScrollBar();
        if (watched) {
            horizontalScrollBar.setValue(horizontalScrollBar.getMaximum());
        }
        horizontalScrollBar.setEnabled(!watched);
    }

    public boolean isWatched() {
        return watched;
    }

    private static class MasterViewPortChangeListener implements ChangeListener {
        private List<JViewport> slaveViewPorts = new ArrayList<JViewport>();

        public void addSlaveViewport(JViewport viewPort) {
            slaveViewPorts.add(viewPort);
        }

        public void stateChanged(ChangeEvent e) {
            JViewport master = (JViewport) e.getSource();
            final int x = master.getViewPosition().x;
            for (JViewport slave : slaveViewPorts) {
                slave.setViewPosition(new Point(x, slave.getViewPosition().y));
            }
        }
    }

    public ChartPanelView getLastMaximizedChartPanelView() {
        ChartPanelView lastMaximizedChartPanelView = null;
        for (int i = 0; i < chartPanelViews.size(); i++) {
            final ChartPanelView chartPanelView = chartPanelViews.get(i);
            if (!chartPanelView.isMinimized()) lastMaximizedChartPanelView = chartPanelView;
        }
        return lastMaximizedChartPanelView;
    }

    private void scaleModelChart() {
        for (int i = 0; i < chartPanelViews.size(); i++) {
            final ChartPanelView aChartPanelView = (chartPanelViews.get(i));
            aChartPanelView.getModelChart().setFootprint(getMaxFootprint());
            aChartPanelView.getModelChart().setMaxPause(getMaxMaxPause());
            aChartPanelView.getModelChart().setRunningTime(getMaxRunningTime());
        }
    }

    private double getMaxRunningTime() {
        double max = 0;
        for (int i = 0; i < chartPanelViews.size(); i++) {
            max = Math.max(max, chartPanelViews.get(i).getModel().getRunningTime());
        }
        return max;
    }

    private long getMaxFootprint() {
        long max = 0;
        for (int i = 0; i < chartPanelViews.size(); i++) {
            max = Math.max(max, chartPanelViews.get(i).getModel().getFootprint());
        }
        return max;
    }

    private double getMaxMaxPause() {
        double max = 0;
        for (int i = 0; i < chartPanelViews.size(); i++) {
            max = Math.max(max, chartPanelViews.get(i).getModel().getPause().getMax());
        }
        return max;
    }


    private class MultiModelChartFacade implements ModelChart {

        @Override
        public boolean isAntiAlias() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isAntiAlias();
        }

        @Override
        public void setAntiAlias(boolean antiAlias) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setAntiAlias(antiAlias);
            }
        }

        @Override
        public long getFootprint() {
            if (chartPanelViews.isEmpty()) return 0;
            return chartPanelViews.get(0).getModelChart().getFootprint();
        }

        @Override
        public double getMaxPause() {
            if (chartPanelViews.isEmpty()) return 0;
            return chartPanelViews.get(0).getModelChart().getMaxPause();
        }

        @Override
        public void setRunningTime(double runningTime) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setRunningTime(runningTime);
            }
        }

        @Override
        public void setFootprint(long footPrint) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setFootprint(footPrint);
            }
        }

        @Override
        public void setMaxPause(double maxPause) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setMaxPause(maxPause);
            }
        }

        @Override
        public void setScaleFactor(double scaleFactor) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setScaleFactor(scaleFactor);
            }
        }

        @Override
        public double getScaleFactor() {
            if (chartPanelViews.isEmpty()) return 1;
            return chartPanelViews.get(0).getModelChart().getScaleFactor();
        }

        @Override
        public boolean isShowGCTimesLine() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowGCTimesLine();
        }

        @Override
        public void setShowGCTimesLine(boolean showGCTimesLine) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowGCTimesLine(showGCTimesLine);
            }
        }

        @Override
        public boolean isShowGCTimesRectangles() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowGCTimesRectangles();
        }

        @Override
        public void setShowGCTimesRectangles(boolean showGCTimesRectangles) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowGCTimesRectangles(showGCTimesRectangles);
            }
        }

        @Override
        public boolean isShowFullGCLines() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowFullGCLines();
        }

        @Override
        public void setShowFullGCLines(boolean showFullGCLines) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowFullGCLines(showFullGCLines);
            }
        }

        @Override
        public boolean isShowIncGCLines() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowIncGCLines();
        }

        @Override
        public void setShowIncGCLines(boolean showIncGCLines) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowIncGCLines(showIncGCLines);
            }
        }

        @Override
        public boolean isShowTotalMemoryLine() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowTotalMemoryLine();
        }

        @Override
        public void setShowTotalMemoryLine(boolean showTotalMemoryLine) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowTotalMemoryLine(showTotalMemoryLine);
            }
        }

        @Override
        public void setShowTenured(boolean showTenured) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowTenured(showTenured);
            }
        }

        @Override
        public boolean isShowTenured() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowTenured();
        }

        @Override
        public void setShowYoung(boolean showYoung) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowYoung(showYoung);
            }
        }

        @Override
        public boolean isShowYoung() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowYoung();
        }

        @Override
        public boolean isShowUsedMemoryLine() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowUsedMemoryLine();
        }

        @Override
        public void setShowUsedMemoryLine(boolean showUsedMemoryLine) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowUsedMemoryLine(showUsedMemoryLine);
            }
        }

        @Override
        public boolean isShowUsedTenuredMemoryLine() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowUsedTenuredMemoryLine();
        }

        @Override
        public void setShowUsedTenuredMemoryLine(boolean showUsedTenuredMemoryLine) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowUsedTenuredMemoryLine(showUsedTenuredMemoryLine);
            }
        }

        @Override
        public boolean isShowUsedYoungMemoryLine() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowUsedYoungMemoryLine();
        }

        @Override
        public void setShowUsedYoungMemoryLine(boolean showUsedYoungMemoryLine) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowUsedYoungMemoryLine(showUsedYoungMemoryLine);
            }
        }

        @Override
        public void setShowInitialMarkLevel(boolean showInitialMarkLevel) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowInitialMarkLevel(showInitialMarkLevel);
            }
        }

        @Override
        public boolean isShowInitialMarkLevel() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowInitialMarkLevel();
        }

        @Override
        public void setShowConcurrentCollectionBeginEnd(boolean showConcurrentCollectionBeginEnd) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowConcurrentCollectionBeginEnd(showConcurrentCollectionBeginEnd);
            }
        }

        @Override
        public boolean isShowConcurrentCollectionBeginEnd() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowConcurrentCollectionBeginEnd();
        }

        @Override
        public void resetPolygonCache() {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().resetPolygonCache();
            }
        }

        @Override
        public void setShowDateStamp(boolean showDateStamp) {
            for (ChartPanelView chartPanelView : chartPanelViews) {
                chartPanelView.getModelChart().setShowDateStamp(showDateStamp);
            }
        }

        @Override
        public boolean isShowDateStamp() {
            if (chartPanelViews.isEmpty()) return false;
            return chartPanelViews.get(0).getModelChart().isShowDateStamp();

        }
    }

    private class ScrollBarMaximumChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (isWatched()) {
                BoundedRangeModel model = (BoundedRangeModel) e.getSource();
                model.setValue(model.getMaximum());
            }
        }

    }

    /**
     * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
     */
    private class ResizeListener extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            modelChartListFacade.resetPolygonCache();
        }

    }
}
