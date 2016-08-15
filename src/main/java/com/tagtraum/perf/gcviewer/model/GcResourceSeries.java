package com.tagtraum.perf.gcviewer.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An (artificial) {@link GCResource} that represents a collection of actual {@link GCResource}s that are to be
 * treated as one consecutive log.
 *
 * @author martin.geldmacher
 */
public class GcResourceSeries extends AbstractGcResource {
    private static final Logger logger = Logger.getLogger(GcResourceSeries.class.getName());
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    private final List<GCResource> resourcesInOrder;

    public GcResourceSeries(List<GCResource> resourcesInOrder) {
        super(buildName(resourcesInOrder), Logger.getLogger("GCResourceSeries".concat(Integer.toString(COUNT.incrementAndGet()))));

        this.resourcesInOrder = resourcesInOrder;
        setLogger(resourcesInOrder);
    }

    private void setLogger(List<GCResource> resourcesInOrder) {
        // Contained GCResource should use the same logger
        for (GCResource resource : resourcesInOrder) {
            resource.setLogger(getLogger());
        }
    }

    public List<GCResource> getResourcesInOrder() {
        return resourcesInOrder;
    }

    @Override
    public boolean hasUnderlyingResourceChanged() {
        // Assumption: Once a logfile has been rotated, it doesn't change anymore.
        // Only the last logfile can be active. Check if it has changed.
        return getLastGcResource().hasUnderlyingResourceChanged();
    }

    @Override
    public void setModel(GCModel model) {
        for (GCResource resource : resourcesInOrder) {
            resource.setModel(model);
        }

        super.setModel(model);
    }

    private GCResource getLastGcResource() {
        return resourcesInOrder.get(resourcesInOrder.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GcResourceSeries that = (GcResourceSeries) o;

        return resourcesInOrder.equals(that.resourcesInOrder);

    }

    @Override
    public int hashCode() {
        return resourcesInOrder.hashCode();
    }

    @Override
    public String toString() {
        return "GCResourceSeries [resourceName=" + getResourceName() + ", isReload=" + isReload() + ", logger=" + getLogger() + ", model=" + getModel() + "]";
    }

    protected static String buildName(List<GCResource> resourcesInOrder) {
        if (resourcesInOrder == null || resourcesInOrder.isEmpty())
            throw new IllegalArgumentException("At least one GCResource expected!");
        try {

            GCResource first = resourcesInOrder.get(0);
            GCResource last = resourcesInOrder.get(resourcesInOrder.size() - 1);
            return buildName(first.getResourceName(), last.getResourceName());
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to build name for GCResources. Reason: " + ex.getMessage());
            logger.log(Level.FINER, "Details: ", ex);
            return resourcesInOrder.get(0).getResourceName();
        }
    }

    protected static String buildName(String first, String last) {
        if (first.equals(last))
            return first;

        String prefix = greatestCommonPrefix(first, last);
        String firstSuffix = getSuffix(first, prefix);
        String lastSuffix = getSuffix(last, prefix);

        return prefix + firstSuffix + "-" + lastSuffix;
    }

    private static String getSuffix(String full, String prefix) {
        int beginIndex = 0;
        int indexOfPrefix = prefix.isEmpty() ? 0 : full.lastIndexOf(prefix);
        if (indexOfPrefix >= 0)
            beginIndex = indexOfPrefix + prefix.length();

        String suffix = full.substring(beginIndex);
        String number = suffix.replaceAll("\\D+", "");
        if (number.isEmpty())
            return suffix;
        return number;
    }

    private static String greatestCommonPrefix(String a, String b) {
        int minLength = Math.min(a.length(), b.length());
        for (int i = 0; i < minLength; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return a.substring(0, i);
            }
        }
        return a.substring(0, minLength);
    }
}
