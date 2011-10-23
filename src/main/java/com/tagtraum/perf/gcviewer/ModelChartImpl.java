package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.renderer.*;
import com.tagtraum.perf.gcviewer.util.TimeFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * Graphical chart of the gc file.
 *
 * Date: Jan 30, 2002
 * Time: 7:50:42 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class ModelChartImpl extends JScrollPane implements ModelChart {

    private GCModel model;
    private Chart chart;
    private Ruler timestampRuler;
    private Ruler memoryRuler;
    private Ruler pauseRuler;
    private double scaleFactor = 1;
    private double runningTime;
    private double maxPause;
    private long footprint;
    private TotalYoungRenderer totalYoungRenderer;
    private TotalHeapRenderer totalHeapRenderer;
    private TotalTenuredRenderer totalTenuredRenderer;
    private IncLineRenderer incLineRenderer;
    private GCRectanglesRenderer gcRectanglesRenderer;
    private FullGCLineRenderer fullGCLineRenderer;
    private GCTimesRenderer gcTimesRenderer;
    private UsedHeapRenderer usedHeapRenderer;
    private boolean antiAlias;
    private TimeOffsetPanel timeOffsetPanel;

    public ModelChartImpl() {
        super();
        this.model = new GCModel(true);
        this.chart = new Chart();
        this.chart.setPreferredSize(new Dimension(0, 0));
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        // order of the renderers determines what is painted first and last
        // we start with what's painted last
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2;
        gridBagConstraints.weighty = 2;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;

        usedHeapRenderer = new UsedHeapRenderer(this);
        chart.add(usedHeapRenderer, gridBagConstraints);
        gcTimesRenderer = new GCTimesRenderer(this);
        chart.add(gcTimesRenderer, gridBagConstraints);
        fullGCLineRenderer = new FullGCLineRenderer(this);
        chart.add(fullGCLineRenderer, gridBagConstraints);
        gcRectanglesRenderer = new GCRectanglesRenderer(this);
        chart.add(gcRectanglesRenderer, gridBagConstraints);
        incLineRenderer = new IncLineRenderer(this);
        chart.add(incLineRenderer, gridBagConstraints);
        totalTenuredRenderer = new TotalTenuredRenderer(this);
        chart.add(totalTenuredRenderer, gridBagConstraints);
        totalYoungRenderer = new TotalYoungRenderer(this);
        chart.add(totalYoungRenderer, gridBagConstraints);
        totalHeapRenderer = new TotalHeapRenderer(this);
        chart.add(totalHeapRenderer, gridBagConstraints);

        setViewportView(chart);
        // This would make scrolling slower, but eliminates flickering...
        //getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        JPanel rowHeaderPanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        rowHeaderPanel.setLayout(layout);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.weightx = 2;
        constraints.weighty = 1;
        constraints.gridheight = 2;
        constraints.gridx = 0;
        constraints.gridy = 1;
        this.memoryRuler = new Ruler(true, 0, model.getFootprint(), "K");
        this.pauseRuler = new Ruler(true, 0, model.getPause().getMax(), "s");
        layout.setConstraints(memoryRuler, constraints);
        rowHeaderPanel.add(memoryRuler);
        constraints.gridx = 1;
        layout.setConstraints(pauseRuler, constraints);
        rowHeaderPanel.add(pauseRuler);
        setRowHeaderView(rowHeaderPanel);
        setCorner(JScrollPane.UPPER_LEFT_CORNER, new JPanel());
        setCorner(JScrollPane.LOWER_LEFT_CORNER, new JPanel());

        DateFormat dateFormatter = new TimeFormat();
        this.timestampRuler = new Ruler(false, 0, model.getRunningTime(), "", dateFormatter);
        setColumnHeaderView(timestampRuler);

        getViewport().addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                chart.setSize(chart.getPreferredSize());
                memoryRuler.setSize((int)memoryRuler.getPreferredSize().getWidth(), e.getComponent().getHeight());
                pauseRuler.setSize((int)pauseRuler.getPreferredSize().getWidth(), e.getComponent().getHeight());
                timestampRuler.setSize((int)chart.getPreferredSize().getWidth(), (int)timestampRuler.getPreferredSize().getHeight());
            }
            public void componentMoved(ComponentEvent e) {}
            public void componentShown(ComponentEvent e) {}
            public void componentHidden(ComponentEvent e) {}
        });
        // timestamp menu
        final JPopupMenu popupMenu = new JPopupMenu();
        timeOffsetPanel = new TimeOffsetPanel(popupMenu);
        popupMenu.add(timeOffsetPanel);
        final JPopupMenu timestampRulerPopup = popupMenu;
        Action setOffsetAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (timeOffsetPanel.isOffsetSet()) timestampRuler.setOffset(timeOffsetPanel.getDate().getTime()/1000);
                else timestampRuler.setOffset(0);
                timestampRuler.revalidate();
                timestampRuler.repaint();
            }
        };
        timeOffsetPanel.setOkAction(setOffsetAction);
        this.timestampRuler.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e) {
                maybePopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybePopup(e);
            }

            public void maybePopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    if (timestampRuler.getOffset() != 0) {
                        timeOffsetPanel.setDate(new Date((long)timestampRuler.getOffset()*1000));
                        timeOffsetPanel.setOffsetSet(true);
                    }
                    else {
                        long suggestedStartDate = model.getLastModified();
                        if (model.hasDateStamp()) {
                        	suggestedStartDate = (long)(model.getFirstDateStamp().getTime());
                        }
                        else if (model.hasCorrectTimestamp()) {
                            suggestedStartDate -= (long)(model.getRunningTime() * 1000.0d);
                        }
                        timeOffsetPanel.setDate(new Date(suggestedStartDate));
                        timeOffsetPanel.setOffsetSet(false);
                    }
                    timestampRulerPopup.show(e.getComponent(), e.getX(),  e.getY());
                    timeOffsetPanel.requestFocus();
                }
            }
        });

    }

    public void invalidate() {
        super.invalidate();
        chart.invalidate();
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        chart.setSize(chart.getPreferredSize());
        memoryRuler.setSize((int)memoryRuler.getPreferredSize().getWidth(), getViewport().getHeight());
        pauseRuler.setSize((int)pauseRuler.getPreferredSize().getWidth(), getViewport().getHeight());
        timestampRuler.setSize((int)(getViewport().getWidth()*getScaleFactor()), (int)timestampRuler.getPreferredSize().getHeight());
        repaint();
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public void setAntiAlias(boolean antiAlias) {
        this.antiAlias = antiAlias;
    }

    public boolean isShowTenured() {
        return totalTenuredRenderer.isVisible();
    }

    public void setShowTenured(boolean showTenured) {
        totalTenuredRenderer.setVisible(showTenured);
    }

    public boolean isShowYoung() {
        return totalYoungRenderer.isVisible();
    }

    public void setShowYoung(boolean showYoung) {
        totalYoungRenderer.setVisible(showYoung);
    }

    public boolean isShowGCTimesLine() {
        return gcTimesRenderer.isVisible();
    }

    public void setShowGCTimesLine(boolean showGCTimesLine) {
        gcTimesRenderer.setVisible(showGCTimesLine);
    }

    public boolean isShowGCTimesRectangles() {
        return gcRectanglesRenderer.isVisible();
    }

    public void setShowGCTimesRectangles(boolean showGCTimesRectangles) {
        gcRectanglesRenderer.setVisible(showGCTimesRectangles);
    }

    public boolean isShowFullGCLines() {
        return fullGCLineRenderer.isVisible();
    }

    public void setShowFullGCLines(boolean showFullGCLines) {
        fullGCLineRenderer.setVisible(showFullGCLines);
    }

    public boolean isShowIncGCLines() {
        return incLineRenderer.isVisible();
    }

    public void setShowIncGCLines(boolean showIncGCLines) {
        incLineRenderer.setVisible(showIncGCLines);
    }

    public boolean isShowTotalMemoryLine() {
        return totalHeapRenderer.isVisible();
    }

    public void setShowTotalMemoryLine(boolean showTotalMemoryLine) {
        totalHeapRenderer.setVisible(showTotalMemoryLine);
    }

    public boolean isShowUsedMemoryLine() {
        return usedHeapRenderer.isVisible();
    }

    public void setShowUsedMemoryLine(boolean showUsedMemoryLine) {
        usedHeapRenderer.setVisible(showUsedMemoryLine);
    }

    public void setModel(GCModel model) {
        this.model = model;
    }

    public GCModel getModel() {
        return model;
    }

    public void setRunningTime(double runningTime) {
        this.timestampRuler.setMaxUnit(runningTime);
        this.runningTime = runningTime;
        getRowHeader().revalidate();
        usedHeapRenderer.invalidate();
        chart.revalidate();
    }

    public void setFootprint(long footprint) {
        this.memoryRuler.setMaxUnit(footprint);
        this.footprint = footprint;
        getColumnHeader().revalidate();
        chart.revalidate();
    }

    public void setMaxPause(double maxPause) {
        this.pauseRuler.setMaxUnit(maxPause);
        this.maxPause = maxPause;
        getColumnHeader().revalidate();
        chart.revalidate();
    }

    public long getFootprint() {
        return footprint;
    }

    public double getMaxPause() {
        return maxPause;
    }

    private class Chart extends JPanel {
        private double yMemScaleFactor = 1;

        public Chart() {
            setBackground(Color.white);
            setLayout(new GridBagLayout());
        }

        public Dimension getPreferredSize() {
            return new Dimension(scaleX(runningTime), getViewport().getHeight());
        }

        public void setSize(int width, int height) {
            super.setSize(width, height);
            computeScaleFactors();
        }

        public void computeScaleFactors() {
            yMemScaleFactor = (double) getViewport().getHeight() / (double) footprint;
        }

        private void drawUsedTenuredPolygon(Graphics g) {
            Polygon tenured = computeUsedTenuredPolygon();
            g.setColor(Color.ORANGE.darker());
            g.fillPolygon(tenured);
        }

        private Polygon computeUsedTenuredPolygon() {
            Polygon polygon = new Polygon();
            final int zeroY = memScaleY(0);
            polygon.addPoint(0, zeroY);
            for (Iterator i = model.getGCEvents(); i.hasNext();) {
                AbstractGCEvent event = (AbstractGCEvent) i.next();
                for (Iterator iterator=event.details(); iterator.hasNext();) {
                    GCEvent detailEvent = (GCEvent)iterator.next();
                    if (detailEvent.getType().getGeneration() == GCEvent.Generation.TENURED) {
                        polygon.addPoint(scaleX(detailEvent.getTimestamp()), memScaleY(detailEvent.getPreUsed()));
                        polygon.addPoint(scaleX(detailEvent.getTimestamp()+detailEvent.getPause()), memScaleY(detailEvent.getPostUsed()));
                    }
                }
            }
            polygon.addPoint(scaleX(model.getRunningTime()), zeroY);
            return polygon;
        }

        private int scaleX(double d) {
            return (int) (d * getScaleFactor());
        }

        private int memScaleY(long l) {
            return (int) getHeight() - (int) ((double) l * yMemScaleFactor);
        }

    }

    private class Ruler extends JPanel {
        private boolean vertical;
        private double minUnit;
        private double maxUnit;
        private final double log10 = Math.log(10);
        private Font font;
        private Format formatter;
        private String longestString;
        private String unitName;
        private int minHalfDistance;
        private double offset;

        public Ruler(boolean vertical, double minUnit, double maxUnit, String unitName) {
            this(vertical,  minUnit, maxUnit, unitName, NumberFormat.getInstance());
        }

        public Ruler(boolean vertical, double minUnit, double maxUnit, String unitName, Format formatter) {
            setUnitName(unitName);
            this.formatter = formatter;
            setVertical(vertical);
            setMinUnit(minUnit);
            setMaxUnit(maxUnit);
            font = new Font("sans-serif", Font.PLAIN, 10);
        }

        public void setSize(int width, int height) {
            super.setSize(width, height);
            configureFormatter();
        }

        public Dimension getPreferredSize() {
            FontMetrics fm = getToolkit().getFontMetrics(font);
            //System.out.println("longest: " + longestString);
            configureFormatter();
            int minWidth = fm.stringWidth(longestString) + 5;
            Dimension bestSize = null;
            if (isVertical()) {
                bestSize = new Dimension(minWidth, getHeight());
            } else {
                bestSize = new Dimension((int) (runningTime * getScaleFactor()), fm.getHeight());
                minHalfDistance = minWidth;
            }
            //System.out.println("pref: " + bestSize);
            return bestSize;
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponents(g);
            configureFormatter();
            double lineDistance = getLineDistance();
            Rectangle clip = g.getClipBounds();
            g.clearRect(clip.x, clip.y, clip.width, clip.height);
            g.setColor(Color.black);
            if (isVertical()) {
                double halfLineDistance = lineDistance / 2.0d;
                for (double line = (double) getHeight() - (minUnit % lineDistance); line > 0; line -= lineDistance) {
                    g.drawLine(0, (int) line, getWidth(), (int) line);
                }
                for (double line = (double) getHeight() - ((minUnit - halfLineDistance) % lineDistance); line > 0; line -= lineDistance) {
                    int inset = 3;
                    g.drawLine(inset, (int) line, getWidth() - inset, (int) line);
                }
                String number = null;
                for (double line = (double) getHeight() - (minUnit % lineDistance); line > 0; line -= lineDistance) {
                    g.setFont(font);
                    String newNumber = format((getHeight() - line) / getPixelsPerUnit()) + getUnitName();
                    if (!newNumber.equals(number))
                        g.drawString(newNumber, 2, (int) line - 2);
                    number = newNumber;
                }
            } else {
                double halfLineDistance = lineDistance / 2.0d;
                for (double line = (minUnit % lineDistance); line < getWidth(); line += lineDistance) {
                    g.drawLine((int) line, 0, (int) line, getHeight());
                }
                for (double line = (minUnit - halfLineDistance) % lineDistance; line < getWidth(); line += lineDistance) {
                    int inset = 3;
                    g.drawLine((int) line, inset, (int) line, getHeight() - inset);
                }
                String number = null;
                for (double line = (minUnit % lineDistance); line < getWidth(); line += lineDistance) {
                //for (double line = unitStart; line < unitEnd; line += lineDistance) {
                    g.setFont(font);
                    String newNumber = format(line / getPixelsPerUnit()) + getUnitName();
                    if (!newNumber.equals(number))
                        g.drawString(newNumber, ((int) line) + 3, getHeight() - 2);
                    number = newNumber;
                }
            }
        }

        public double getOffset() {
            return offset;
        }

        public void setOffset(double offset) {
            this.offset = offset;
        }

        public void setFormatter(Format formatter) {
            this.formatter = formatter;
        }

        private String format(final double val) {
            final double offsetValue = val + offset;
            String s = null;
            if (formatter instanceof NumberFormat) {
                s = ((NumberFormat)formatter).format(offsetValue);
            }
            else if (formatter instanceof DateFormat) {
                final Date date = new Date(((long)offsetValue) * 1000l);
                //final Date date = new Date((long)Math.ceil(val * 1000.0d));
                s = ((DateFormat)formatter).format(date);
            }
            return s;
        }

        private double getLineDistance() {
            if (formatter instanceof NumberFormat) return getNumberLineDistance();
            else if (formatter instanceof DateFormat) return getDateLineDistance();
            return 0.0d;
        }

        private double getDateLineDistance() {
            double lineDistance = getPixelsPerUnit();
            if (isVertical()) {
                if (lineDistance < 20) lineDistance *= 10.0d; // 10sec
                if (lineDistance < 20) lineDistance *= 3.0d; // 30sec
                if (lineDistance < 20) lineDistance *= 2.0d; // 1min
                if (lineDistance < 20) lineDistance *= 2.0d; // 2min
                if (lineDistance < 20) lineDistance *= 5.0d; // 10min
                if (lineDistance < 20) lineDistance *= 2.0d; // 20min
                if (lineDistance < 20) lineDistance *= 3.0d; // 1h
                if (lineDistance < 20) {
                    double oneHourDistance = lineDistance;
                    while (lineDistance < 20) lineDistance += oneHourDistance;
                }
            } else {
                if (lineDistance < minHalfDistance * 2) lineDistance *= 10.0d; // 10sec
                if (lineDistance < minHalfDistance * 2) lineDistance *= 3.0d; // 30sec
                if (lineDistance < minHalfDistance * 2) lineDistance *= 2.0d; // 1min
                if (lineDistance < minHalfDistance * 2) lineDistance *= 2.0d; // 2min
                if (lineDistance < minHalfDistance * 2) lineDistance *= 5.0d; // 10min
                if (lineDistance < minHalfDistance * 2) lineDistance *= 2.0d; // 20min
                if (lineDistance < minHalfDistance * 2) lineDistance *= 3.0d; // 1h
                if (lineDistance < minHalfDistance * 2) {
                    double oneHourDistance = lineDistance;
                    while (lineDistance < minHalfDistance * 2) lineDistance += oneHourDistance;
                }
            }
            return lineDistance;
        }

        private double getNumberLineDistance() {
            double log10PixelPerUnit = Math.log(getPixelsPerUnit()) / log10;
            double lineDistance = getPixelsPerUnit() * Math.pow(10, Math.ceil(-log10PixelPerUnit) + 1);
            if (isVertical()) {
                while (lineDistance < 20) lineDistance *= 10.0d;
            } else {
                while (lineDistance < minHalfDistance * 2) lineDistance *= 10.0d;
            }
            return lineDistance;
        }

        private double getPixelsPerUnit() {
            double pixelPerUnit = (isVertical()?(double) getHeight()/(maxUnit - minUnit):(runningTime * getScaleFactor() / (maxUnit - minUnit)));
            return pixelPerUnit;
        }

        public double getMinUnit() {
            return minUnit;
        }

        public void setMinUnit(double minUnit) {
            this.minUnit = minUnit;
            configureFormatter();
        }

        public double getMaxUnit() {
            return maxUnit;
        }

        public void setMaxUnit(double maxUnit) {
            this.maxUnit = maxUnit;
            configureFormatter();
        }

        public void configureFormatter() {
            if (formatter instanceof NumberFormat) {
                double digits = Math.log(maxUnit) / log10;
                if (digits < 1) {
                    ((NumberFormat)formatter).setMaximumFractionDigits((int) Math.abs(digits) + 2);
                    ((NumberFormat)formatter).setMinimumFractionDigits((int) Math.abs(digits) + 2);
                } else {
                    ((NumberFormat)formatter).setMaximumFractionDigits(0);
                    ((NumberFormat)formatter).setMinimumFractionDigits(0);
                }
            }
            longestString = format(maxUnit);
            if (unitName != null) longestString += unitName;
            invalidate();
        }

        public boolean isVertical() {
            return vertical;
        }

        public void setVertical(boolean vertical) {
            this.vertical = vertical;
        }

        public String getUnitName() {
            return unitName;
        }

        public void setUnitName(String unitName) {
            this.unitName = unitName;
        }
    }
}
