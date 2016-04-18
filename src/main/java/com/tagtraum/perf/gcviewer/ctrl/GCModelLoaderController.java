package com.tagtraum.perf.gcviewer.ctrl;

import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.view.GCDocument;

import java.io.File;
import java.util.List;

/**
 * Controller class for {@link GCModelLoader}.
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 16.08.2014</p>
 */
public interface GCModelLoaderController {
    void add(File[] files);

    void add(GCResource gcResource);

    void add(List<GCResource> gcResourceList);

    void open(File[] files);

    /**
     * Open a gc log resource from a filename or URL.
     *
     * @param gcResource filename or URL name.
     */
    void open(GCResource gcResource);

    void open(List<GCResource> gcResourceList);

    /**
     * Opens the given {@link GCResource}s as a series of rotated logfiles.
     *
     * @param gcResourceList a list of rotated gc logfiles. Ordering is not required.
     */
    void openAsSeries(List<GCResource> gcResourceList);

    /**
     * Reload all models of <code>gcDocument</code> and provide tracker. The tracker will
     * fire a propertyChangeEvent, as soon as all GCModelLoaders have finished loading.
     *
     * @param gcDocument document of which models should be reloaded
     * @return tracker to track finish state of all models being loaded
     */
    GCModelLoaderGroupTracker reload(GCDocument gcDocument);
}
