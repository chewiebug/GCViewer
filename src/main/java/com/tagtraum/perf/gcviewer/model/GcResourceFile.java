package com.tagtraum.perf.gcviewer.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Identifies a GC resource: a file or URL resource containing GC info.
 *
 * @author Hans Bausewein
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>Date: November 8, 2013</p>
 */
public class GcResourceFile extends AbstractGcResource
{
    public static final String PROPERTY_MODEL = "model";
    private static final AtomicInteger COUNT = new AtomicInteger(0);

    public GcResourceFile(File file) {
        this(file.getAbsolutePath());
    }

    public GcResourceFile(String resourceName) {
        super(resourceName, Logger.getLogger("GCResourceFile".concat(Integer.toString(COUNT.incrementAndGet()))));

        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName cannot be null");
        }

    }

    public URL getResourceNameAsUrl() throws MalformedURLException {
        URL url = null;
        if (getResourceName().startsWith("http") || getResourceName().startsWith("file")) {
            url = new URL(getResourceName());
        }
        else {
            url = new File(getResourceName()).toURI().toURL();
        }

        return url;
    }

    /**
     * Same as {@link #getResourceNameAsUrl()}, but still returns a string, if MalFormedURLException occurred.
     *
     * @return same as getResourceNameAsUrl(), but without Exception
     */
    public String getResourceNameAsUrlString() {
        try {
            return getResourceNameAsUrl().toString();
        }
        catch (MalformedURLException e) {
            return "malformed url: " + getResourceName();
        }
    }

    @Override
    public boolean hasUnderlyingResourceChanged() {
        if (getModel().getURL() == null) {
            return true;
        }

        return getModel().isDifferent(getModel().getURL());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GcResourceFile other = (GcResourceFile) obj;
        if (getResourceName() == null) {
            if (other.getResourceName() != null) {
                return false;
            }
        }
        else if (!getResourceNameAsUrlString().equals(other.getResourceNameAsUrlString())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((getResourceName() == null) ? 0 : getResourceNameAsUrlString().hashCode());

        return result;
    }

    @Override
    public String toString() {
        return "GCResourceFile [resourceNameAsUrlString=" + getResourceNameAsUrlString() + ", isReload=" + isReload() + ", logger=" + getLogger() + ", model="
                + getModel() + "]";
    }
}
