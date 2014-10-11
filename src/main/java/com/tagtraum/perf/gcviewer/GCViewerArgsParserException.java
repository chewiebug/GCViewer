package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.exp.DataWriterType;

/**
 * Exception to indicate illegal argument in {@link GCViewerArgsParser}.
 *  
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.06.2014</p>
 */
public class GCViewerArgsParserException extends Exception {
    
    public GCViewerArgsParserException(String type) {
        super("Illegal type '" + type + "'; must be one of " + formatLegalDataWriterTypes());
    }
    
    private static String formatLegalDataWriterTypes() {
        StringBuilder sb = new StringBuilder();
        for (DataWriterType type : DataWriterType.values()) {
            sb.append(type.name()).append(", ");
        }
        
        // delete last ", "
        sb.delete(sb.length()-2, sb.length()-1);
        
        return sb.toString();
    }
}
