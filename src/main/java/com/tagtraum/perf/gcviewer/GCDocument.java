package com.tagtraum.perf.gcviewer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.File;
import java.util.*;
import java.net.URL;

/**
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 *         Date: May 5, 2005
 *         Time: 10:25:16 AM
 */
public class GCDocument extends JInternalFrame {

    private java.util.List chartPanelViews = new ArrayList();
    private ModelChart modelChart;
    private boolean showModelPanel = true;
    private boolean watched;
    private RefreshWatchDog refreshWatchDog;


    public GCDocument(final GCViewer gcViewer, String s) {
        super(s, true, true, true, false);
        this.refreshWatchDog = new RefreshWatchDog();
        refreshWatchDog.setGcDocument(this);
        modelChart = new MultiModelChartFacade();
        GridBagLayout layout = new GridBagLayout();
        getContentPane().setLayout(layout);
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
                        java.util.List list = (java.util.List)tr.getTransferData(DataFlavor.javaFileListFlavor);
                        File[] files = (File[])list.toArray(new File[list.size()]);
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
     * @return true, if any of the files has been reloaded
     * @throws IOException
     */
    public boolean reloadModels(boolean background) throws IOException {
        boolean reloaded = false;
        for (int i = 0; i < chartPanelViews.size(); i++) {
            reloaded |= ((ChartPanelView) chartPanelViews.get(i)).reloadModel();
        }
        if (!background) {
            relayout();
        }
        return reloaded;
    }

    public ModelChart getModelChart() {
        return modelChart;
    }

    public void add(final URL url) throws IOException {
        ChartPanelView chartPanelView = new ChartPanelView(this, url);
        chartPanelViews.add(chartPanelView);
        chartPanelView.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if ("minimized".equals(event.getPropertyName())) relayout();
            }
        });
        // make sure all models in one document have the same display properties
        if (chartPanelViews.size() > 1) {
            modelChart.setScaleFactor(modelChart.getScaleFactor());
            modelChart.setShowFullGCLines(modelChart.isShowFullGCLines());
            modelChart.setShowGCTimesLine(modelChart.isShowGCTimesLine());
            modelChart.setShowGCTimesRectangles(modelChart.isShowGCTimesRectangles());
            modelChart.setShowIncGCLines(modelChart.isShowIncGCLines());
            modelChart.setShowTotalMemoryLine(modelChart.isShowTotalMemoryLine());
            modelChart.setShowUsedMemoryLine(modelChart.isShowUsedMemoryLine());
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
                sb.append(((ChartPanelView) chartPanelViews.get(i)).getModel().getURL().getFile());
                if (i + 1 < chartPanelViews.size()) sb.append(", ");
            }
            setTitle(sb.toString());
        } else if (!chartPanelViews.isEmpty())
            setTitle(((ChartPanelView) chartPanelViews.get(0)).getModel().getURL().toString());
        else
            setTitle("");
        int row = 0;
        boolean noMaximizedChartPanelView = true;
        ChartPanelView lastMaximizedChartPanelView = getLastMaximizedChartPanelView();
        MasterViewPortChangeListener masterViewPortChangeListener = new MasterViewPortChangeListener();

        for (int i = 0; i < chartPanelViews.size(); i++) {
            final ChartPanelView chartPanelView = (ChartPanelView) chartPanelViews.get(i);
            final ModelChartImpl modelChart = (ModelChartImpl) chartPanelView.getModelChart();
            final ModelPanel modelPanel = chartPanelView.getModelPanel();
            //final JComponent parseLog = new JScrollPane(chartPanelView.getParseLog());
            modelChart.invalidate();
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
            modelChart.setVisible(!chartPanelView.isMinimized());
            getContentPane().add(modelChart, constraints);

            /*
            constraints.gridheight = 1;
            constraints.gridy = row;
            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.weighty = 4;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.NORTH;
            getContentPane().add(parseLog, constraints);
            parseLog.setVisible(!chartPanelView.isMinimized());

            row++;
            */

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
        revalidate();
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
        return (ChartPanelView) chartPanelViews.get(i);
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
        private java.util.List slaveViewPorts = new ArrayList();

        public void addSlaveViewport(JViewport viewPort) {
            slaveViewPorts.add(viewPort);
        }

        public void stateChanged(ChangeEvent e) {
            JViewport master = (JViewport) e.getSource();
            final int x = master.getViewPosition().x;
            for (int i = 0; i < slaveViewPorts.size(); i++) {
                JViewport slave = (JViewport) slaveViewPorts.get(i);
                slave.setViewPosition(new Point(x, slave.getViewPosition().y));
            }
        }
    }

    public ChartPanelView getLastMaximizedChartPanelView() {
        ChartPanelView lastMaximizedChartPanelView = null;
        for (int i = 0; i < chartPanelViews.size(); i++) {
            final ChartPanelView chartPanelView = (ChartPanelView) chartPanelViews.get(i);
            if (!chartPanelView.isMinimized()) lastMaximizedChartPanelView = chartPanelView;
        }
        return lastMaximizedChartPanelView;
    }

    private void scaleModelChart() {
        for (int i = 0; i < chartPanelViews.size(); i++) {
            final ChartPanelView aChartPanelView = ((ChartPanelView) chartPanelViews.get(i));
            aChartPanelView.getModelChart().setFootprint(getMaxFootprint());
            aChartPanelView.getModelChart().setMaxPause(getMaxMaxPause());
            aChartPanelView.getModelChart().setRunningTime(getMaxRunningTime());
        }
    }

    private double getMaxRunningTime() {
        double max = 0;
        for (int i = 0; i < chartPanelViews.size(); i++) {
            max = Math.max(max, ((ChartPanelView) chartPanelViews.get(i)).getModel().getRunningTime());
        }
        return max;
    }

    private long getMaxFootprint() {
        long max = 0;
        for (int i = 0; i < chartPanelViews.size(); i++) {
            max = Math.max(max, ((ChartPanelView) chartPanelViews.get(i)).getModel().getFootprint());
        }
        return max;
    }

    private double getMaxMaxPause() {
        double max = 0;
        for (int i = 0; i < chartPanelViews.size(); i++) {
            max = Math.max(max, ((ChartPanelView) chartPanelViews.get(i)).getModel().getPause().getMax());
        }
        return max;
    }


    private class MultiModelChartFacade implements ModelChart {

        public boolean isAntiAlias() {
            if (chartPanelViews.isEmpty()) return false;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().isAntiAlias();
        }

        public void setAntiAlias(boolean antiAlias) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setAntiAlias(antiAlias);
            }
        }

        public long getFootprint() {
            if (chartPanelViews.isEmpty()) return 0;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().getFootprint();
        }

        public double getMaxPause() {
            if (chartPanelViews.isEmpty()) return 0;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().getMaxPause();
        }

        public void setRunningTime(double runningTime) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setRunningTime(runningTime);
            }
        }

        public void setFootprint(long footPrint) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setFootprint(footPrint);
            }
        }

        public void setMaxPause(double maxPause) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setMaxPause(maxPause);
            }
        }

        public void setScaleFactor(double scaleFactor) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setScaleFactor(scaleFactor);
            }
        }

        public double getScaleFactor() {
            if (chartPanelViews.isEmpty()) return 1;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().getScaleFactor();
        }

        public boolean isShowGCTimesLine() {
            if (chartPanelViews.isEmpty()) return false;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().isShowGCTimesLine();
        }

        public void setShowGCTimesLine(boolean showGCTimesLine) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setShowGCTimesLine(showGCTimesLine);
            }
        }

        public boolean isShowGCTimesRectangles() {
            if (chartPanelViews.isEmpty()) return false;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().isShowGCTimesRectangles();
        }

        public void setShowGCTimesRectangles(boolean showGCTimesRectangles) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setShowGCTimesRectangles(showGCTimesRectangles);
            }
        }

        public boolean isShowFullGCLines() {
            if (chartPanelViews.isEmpty()) return false;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().isShowFullGCLines();
        }

        public void setShowFullGCLines(boolean showFullGCLines) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setShowFullGCLines(showFullGCLines);
            }
        }

        public boolean isShowIncGCLines() {
            if (chartPanelViews.isEmpty()) return false;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().isShowIncGCLines();
        }

        public void setShowIncGCLines(boolean showIncGCLines) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setShowIncGCLines(showIncGCLines);
            }
        }

        public boolean isShowTotalMemoryLine() {
            if (chartPanelViews.isEmpty()) return false;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().isShowTotalMemoryLine();
        }

        public void setShowTotalMemoryLine(boolean showTotalMemoryLine) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setShowTotalMemoryLine(showTotalMemoryLine);
            }
        }

        public boolean isShowUsedMemoryLine() {
            if (chartPanelViews.isEmpty()) return false;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().isShowUsedMemoryLine();
        }

        public void setShowUsedMemoryLine(boolean showUsedMemoryLine) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setShowUsedMemoryLine(showUsedMemoryLine);
            }
        }

        public void setShowTenured(boolean showTenured) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setShowTenured(showTenured);
            }
        }

        public boolean isShowTenured() {
            if (chartPanelViews.isEmpty()) return false;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().isShowTenured();
        }

        public void setShowYoung(boolean showYoung) {
            for (int i = 0; i < chartPanelViews.size(); i++) {
                ((ChartPanelView) chartPanelViews.get(i)).getModelChart().setShowYoung(showYoung);
            }
        }

        public boolean isShowYoung() {
            if (chartPanelViews.isEmpty()) return false;
            return ((ChartPanelView) chartPanelViews.get(0)).getModelChart().isShowYoung();
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
}
