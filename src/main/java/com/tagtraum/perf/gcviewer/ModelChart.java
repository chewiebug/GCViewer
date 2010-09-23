package com.tagtraum.perf.gcviewer;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 5, 2005
 * Time: 2:43:39 PM
 *
 */
public interface ModelChart {
    void setScaleFactor(double scaleFactor);

    double getScaleFactor();

    boolean isShowGCTimesLine();

    void setShowGCTimesLine(boolean showGCTimesLine);

    boolean isShowGCTimesRectangles();

    void setShowGCTimesRectangles(boolean showGCTimesRectangles);

    boolean isShowFullGCLines();

    void setShowFullGCLines(boolean showFullGCLines);

    boolean isShowIncGCLines();

    void setShowIncGCLines(boolean showIncGCLines);

    boolean isShowTotalMemoryLine();

    void setShowTotalMemoryLine(boolean showTotalMemoryLine);

    boolean isShowUsedMemoryLine();

    void setShowUsedMemoryLine(boolean showUsedMemoryLine);

    void setShowTenured(boolean showTenured);

    boolean isShowTenured();

    void setShowYoung(boolean showYoung);

    boolean isShowYoung();

    void setRunningTime(double runningTime);

    void setFootprint(long footPrint);

    void setMaxPause(double maxPause);

    long getFootprint();

    double getMaxPause();

    boolean isAntiAlias();

    void setAntiAlias(boolean antiAlias);
}
