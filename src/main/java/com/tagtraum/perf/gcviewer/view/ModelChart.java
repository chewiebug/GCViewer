package com.tagtraum.perf.gcviewer.view;

/**
 * Interface with all important methods for the model chart showing the graphs of the gc file.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 5, 2005
 * Time: 2:43:39 PM
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

    boolean isShowUsedYoungMemoryLine();

    void setShowUsedYoungMemoryLine(boolean showUsedYoungMemoryLine);

    boolean isShowUsedTenuredMemoryLine();

    void setShowUsedTenuredMemoryLine(boolean showUsedTenuredMemoryLine);

    void setShowTenured(boolean showTenured);

    boolean isShowTenured();

    void setShowYoung(boolean showYoung);

    boolean isShowYoung();
    
    void setShowInitialMarkLevel(boolean showInitialMarkLevel);
    
    boolean isShowInitialMarkLevel();
    
    void setShowConcurrentCollectionBeginEnd(boolean showConcurrentCollectionBeginEnd);
    
    boolean isShowConcurrentCollectionBeginEnd();

    void setRunningTime(double runningTime);

    void setFootprint(long footPrint);

    void setMaxPause(double maxPause);

    long getFootprint();

    double getMaxPause();

    boolean isAntiAlias();

    void setAntiAlias(boolean antiAlias);
    
    void resetPolygonCache();

    void setShowDateStamp(boolean showDateStamp);

    boolean isShowDateStamp();

}
