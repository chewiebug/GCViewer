package com.tagtraum.perf.gcviewer.imp;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.ExtendedType;

/**
 * Tools useful for (most) DataReader implementations.
 */
public class DataReaderTools {

    private static Pattern parenthesesPattern = Pattern.compile("\\([^)]*(\\))?\\) ?");

    private Logger logger;

    public DataReaderTools(Logger logger) {
        this.logger = logger;
    }

    /**
     * Returns the amount of memory in kilobyte. Depending on <code>memUnit</code>, input is
     * converted to kilobyte.
     * @param memoryValue amount of memory
     * @param memUnit memory unit
     * @param line line that is parsed
     * @return amount of memory in kilobyte
     */
    public int getMemoryInKiloByte(double memoryValue, char memUnit, String line) {
        if ('B' == memUnit) {
            return (int) Math.rint(memoryValue / 1024);
        }
        else if ('K' == memUnit) {
            return (int) Math.rint(memoryValue);
        }
        else if ('M' == memUnit) {
            return (int) Math.rint(memoryValue * 1024);
        }
        else if ('G' == memUnit) {
            return (int) Math.rint(memoryValue * 1024*1024);
        }
        else {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("unknown memoryunit '" + memUnit + "' in line " + line);
            }
            return 1;
        }
    }

    /**
     * Returns the <code>ExtendedType</code> for <code>typeString</code>, if it can find one. If there is a type name
     * including gc cause ("ParNew (promotion failed)", where (promotion failed) is the gc cause), the cause is removed
     * while trying to find the type.
     *
     * @param typeString string representation of the gc event
     * @return <code>ExtendedType</code> representing <code>typeString</code>
     * @throws UnknownGcTypeException If <code>typeString</code> can't be converted to an <code>ExtendedType</code>
     */
    public ExtendedType parseType(String typeString) throws UnknownGcTypeException {
        ExtendedType gcType = parseTypeWithCause(typeString.trim());
        if (gcType == null) {
            throw new UnknownGcTypeException(typeString);
        }

        return gcType;
    }

    /**
     * Same as @{link {@link #parseType(String)}}, but returns <code>null</code> instead of exception, if no type could
     * be found.
     *
     * @param typeName string representation of the gc event
     * @return <code>ExtendedType</code> representing <code>typeString</code>, or <code>null</code> if none could be found
     */
    public ExtendedType parseTypeWithCause(String typeName) {
        typeName = typeName.trim();
        ExtendedType extendedType = null;
        String lookupTypeName = getLookupTypeName(typeName);
        
        AbstractGCEvent.Type gcType = AbstractGCEvent.Type.lookup(lookupTypeName);
        // the gcType may be null because there was a PrintGCCause flag enabled - if so, reparse it with the first parentheses set stripped
        while (gcType == null && (lookupTypeName.contains("(") && lookupTypeName.contains(")"))) {
            // try to parse it again with the parentheses removed
            Matcher parenthesesMatcher = parenthesesPattern.matcher(lookupTypeName);
            if (parenthesesMatcher.find()) {
                lookupTypeName = parenthesesMatcher.replaceFirst("");
                gcType = AbstractGCEvent.Type.lookup(lookupTypeName);
            } else {
                // is expected to never happen...
                logger.warning("parenthesisMatcher does not match for '" + lookupTypeName + "', allthough string contains '(' + ')'");
            }
        }

        if (gcType != null) {
            extendedType = ExtendedType.lookup(gcType, typeName);
        }

        return extendedType;
    }

    private String getLookupTypeName(String typeName) {
        typeName = typeName.endsWith(":")
                ? typeName.substring(0, typeName.length()-1)
                : typeName;
        return typeName.endsWith("--")
                    ? typeName.substring(0, typeName.length()-2)
                    : typeName;
    }


}
