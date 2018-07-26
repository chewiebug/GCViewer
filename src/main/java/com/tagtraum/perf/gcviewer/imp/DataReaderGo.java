package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.ParseInformation;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses GC log output from Go 1.9.
 *
 * @author <a href="mailto:roland.illig@gmx.de">Roland Illig</a>
 * @see <a href="https://golang.org/pkg/runtime/#hdr-Environment_Variables">Go documentation</a>
 */
public class DataReaderGo extends AbstractDataReader {

    private static final Pattern GCLINE = Pattern.compile(""
            + "gc "
            + "(\\d+) "
            + "@(\\d+\\.\\d+)s "
            + "(\\d+)%: "
            + "(\\d+(?:\\.\\d+)?)\\+"
            + "(\\d+(?:\\.\\d+)?)\\+"
            + "(\\d+(?:\\.\\d+)?) ms clock, "
            + "(\\d+(?:\\.\\d+)?)\\+"
            + "(\\d+(?:\\.\\d+)?)/"
            + "(\\d+(?:\\.\\d+)?)/"
            + "(\\d+(?:\\.\\d+)?)\\+"
            + "(\\d+(?:\\.\\d+)?) ms cpu, "
            + "(\\d+)->"
            + "(\\d+)->"
            + "(\\d+) MB, "
            + "(\\d+) MB goal, "
            + "(\\d+) P");

    public DataReaderGo(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    public GCModel read() throws IOException {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading Go format...");

        try (LineNumberReader in = this.in) {
            GCModel model = new GCModel();
            model.setFormat(GCModel.Format.GO);
            ParseInformation parsePosition = new ParseInformation(0);

            Matcher matcher = GCLINE.matcher("");
            String line;
            while ((line = in.readLine()) != null && shouldContinue()) {
                parsePosition.setIndex(0);
                parsePosition.setLineNumber(in.getLineNumber());
                if (!matcher.reset(line).matches()) {
                    continue;
                }

                try {
                    AbstractGCEvent<?> gcEvent = parseMatch(matcher);
                    model.add(gcEvent);
                } catch (Exception pe) {
                    if (getLogger().isLoggable(Level.WARNING)) getLogger().warning(pe.toString());
                    if (getLogger().isLoggable(Level.FINE)) getLogger().log(Level.FINE, pe.getMessage(), pe);
                }
            }
            return model;
        } finally {
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Done reading.");
        }
    }

    private AbstractGCEvent<?> parseMatch(Matcher matcher) {
        double relativeTime = Double.parseDouble(matcher.group(2));
        double stopTheWorld1Time = Double.parseDouble(matcher.group(4)) / 1000.0;
        double stopTheWorld2Time = Double.parseDouble(matcher.group(6)) / 1000.0;
        int preUsed = Integer.parseInt(matcher.group(12)) * 1024;
        int alive = Integer.parseInt(matcher.group(13)) * 1024;
        int postUsed = Integer.parseInt(matcher.group(14)) * 1024;

        double pause = stopTheWorld1Time + stopTheWorld2Time;
        return new GCEvent(relativeTime, preUsed, postUsed, alive, pause, AbstractGCEvent.Type.GC);
    }
}
