package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.tagtraum.perf.gcviewer.view.SimpleChartRenderer;
import com.tagtraum.perf.gcviewer.exp.AbstractDataWriter;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.view.model.GCPreferences;

/**
 * PNG data writter
 *
 * @author Angel Olle Blazquez
 *
 */
public class PNGDataWriter extends AbstractDataWriter {
	private final FileOutputStream out;

	/**
	 * Constructor for PNGDataWriter with additional <code>configuration</code> parameter.
	 *
	 * @param outputStream FileOutputStream, file where the image should be written to
	 * @param configuration Configuration for this PNGDataWriter; expected contents of the parameter:
	 * <ul>
	 * <li>String: <code>DataWriterFactory.GC_PREFERENCES</code></li>
	 * <li>Object: Instance of GCPreferences (E.g. current screen selection for chart)
	 * </ul>
	 */
	public PNGDataWriter(OutputStream outputStream, Map<String, Object> configuration) {
		super(outputStream, configuration);
		out = (FileOutputStream)outputStream;
	}

	@Override
	public void write(GCModel model) throws IOException {
		SimpleChartRenderer simpleChartRenderer = new SimpleChartRenderer();

		Object gcPreferences = getConfiguration().get(DataWriterFactory.GC_PREFERENCES);
		if (gcPreferences instanceof GCPreferences) {
			simpleChartRenderer.render(model, out, (GCPreferences)gcPreferences);
		} else {
			simpleChartRenderer.render(model, out);
		}
	}

}
