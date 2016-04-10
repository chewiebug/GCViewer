package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.tagtraum.perf.gcviewer.view.SimpleChartRenderer;
import com.tagtraum.perf.gcviewer.exp.AbstractDataWriter;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * PNG data writter
 *
 * @author Angel Olle Blazquez
 *
 */
public class PNGDataWriter extends AbstractDataWriter {
	private FileOutputStream out;

	public PNGDataWriter(OutputStream outputStream) {
		super(outputStream);
		out = (FileOutputStream)outputStream;
	}

	@Override
	public void write(GCModel model) throws IOException {
		new SimpleChartRenderer().render(model, out);
	}

}
