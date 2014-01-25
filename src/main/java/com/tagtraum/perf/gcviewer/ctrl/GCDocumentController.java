package com.tagtraum.perf.gcviewer.ctrl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingWorker;

import com.tagtraum.perf.gcviewer.view.ChartPanelView;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCModelLoaderView;

/**
 * Controller for {@link GCDocument}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 12.01.2014</p>
 */
public class GCDocumentController implements PropertyChangeListener {

    private GCDocument gcDocument;

    public GCDocumentController(GCDocument gcDocument) {
        super();
        
        this.gcDocument = gcDocument;
        gcDocument.addPropertyChangeListener(this);
    }
    
    public void addModel(GCModelLoader loader) {
        ChartPanelView chartPanelView = new ChartPanelView(gcDocument.getPreferences(), loader.getGcResource());
        gcDocument.addChartPanelView(chartPanelView);
        loader.addPropertyChangeListener(this);
        loader.addPropertyChangeListener(chartPanelView.getModelLoaderView());
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
            // for every model that has finished loading, the document must be laid out again
            gcDocument.relayout();
            
            ((GCModelLoader)evt.getSource()).removePropertyChangeListener(this);
        }

    }

    public void reloadModel(GCModelLoader loader) {
        loader.addPropertyChangeListener(this);
        
        GCModelLoaderView loaderView = gcDocument.getChartPanelView(loader.getGcResource()).getModelLoaderView();
        loaderView.setGCResource(loader.getGcResource());
        loader.addPropertyChangeListener(loaderView);
    }
    
}
