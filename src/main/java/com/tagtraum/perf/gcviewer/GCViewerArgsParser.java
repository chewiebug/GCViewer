package com.tagtraum.perf.gcviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tagtraum.perf.gcviewer.exp.DataWriterType;

/**
 * Parser for commandline arguments. 
 * 
 * @author engineersamuel
 */
public class GCViewerArgsParser {
    private static final int ARG_POS_GCFILE = 0;
    private static final int ARG_POS_SUMMARY_FILE = 1;
    private static final int ARG_POS_CHART_FILE = 2;
    
    private int argumentCount;
    private String chartFilePath;
    private String gcfile;
    private String summaryFilePath;
    private DataWriterType type = DataWriterType.SUMMARY;
    
    public int getArgumentCount() {
        return argumentCount;
    }
    
    public String getChartFilePath() {
        return chartFilePath;
    }
    
    public String getGcfile() {
        return gcfile;
    }
    
    public String getSummaryFilePath() {
        return summaryFilePath;
    }

    public DataWriterType getType() { 
        return type; 
    }

    /**
     * Parse arguments given in parameter. If an illegal argument is given, an exception is thrown.
     * 
     * @param args command line arguments to be parsed
     * @throws GCViewerArgsParserException notify about illegal argument
     */
    public void parseArguments(String[] args) throws GCViewerArgsParserException {
        List<String> argsList = new ArrayList<String>(Arrays.asList(args));
        int typeIdx = argsList.indexOf("-t");

        // If there is a -t and there is a string after, set the type
        if (typeIdx != -1 && argsList.size() > (typeIdx + 1)) {
            type = parseType(argsList.get(typeIdx + 1));
            // Chomp these two from the array to prevent any order issues
            argsList.remove(typeIdx);
            argsList.remove(typeIdx);
        } 
        else if (typeIdx != -1) {
            // No specific type set, just keep the default
            argsList.remove(typeIdx);
        }

        argumentCount = argsList.size();
        gcfile = safeGetArgument(argsList, ARG_POS_GCFILE);
        summaryFilePath = safeGetArgument(argsList, ARG_POS_SUMMARY_FILE);
        chartFilePath = safeGetArgument(argsList, ARG_POS_CHART_FILE);
    }

    private DataWriterType parseType(String type) throws GCViewerArgsParserException {
        try {
            return DataWriterType.valueOf(type);
        }
        catch (IllegalArgumentException e) {
            throw new GCViewerArgsParserException(type);
        }
    }
    
    private String safeGetArgument(List<String> arguments, int index) {
        if (arguments.size() > index) {
            return arguments.get(index);
        }
        else {
            return null;
        }
    }

}
