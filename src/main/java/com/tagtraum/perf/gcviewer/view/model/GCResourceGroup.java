package com.tagtraum.perf.gcviewer.view.model;

import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * <p>Holds a group of resource names (those displayed in the same GCDocument).</p>
 * <p>
 * <p>This class was refactored from "URLSet".</p>
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 *         <p>created on: 05.03.2014</p>
 */
public class GCResourceGroup {
    private static final Logger logger = Logger.getLogger(GCResourceGroup.class.getName());
    public static final String RESOURCE_SEPARATOR = ";";
    public static final String SERIES_SEPARATOR = ">";
    private List<String> gcResourceList;

    public GCResourceGroup(List<GCResource> gcResourceList) {
        this.gcResourceList = gcResourceList.stream().map(this::getResourceUrlString).collect(Collectors.toList());
    }

    /**
     * Initialise a group from a single string consisting of {@link GcResourceFile}s separated by "{@value RESOURCE_SEPARATOR}"
     * and contents of a {@link GcResourceSeries} separated by {@value SERIES_SEPARATOR}.
     *
     * @param resourceNameGroup resource names separated by ";"
     */
    public GCResourceGroup(String resourceNameGroup) {
        String[] resources = resourceNameGroup.split(RESOURCE_SEPARATOR);
        gcResourceList = getResourceUrlString(resources);
    }

    private List<String> getResourceUrlString(String[] resources) {
        List<String> urls = new ArrayList<>();
        for (String resource : resources) {
            String url = getResourceUrlString(resource);
            if (url != null) {
                urls.add(url);
            }
        }
        return urls;
    }

    private String getResourceUrlString(String resource) {
        URL url = null;
        try {
            if (resource.startsWith("http") || resource.startsWith("file")) {
                url = new URL(resource);
            }
            else {
                url = new File(resource).toURI().toURL();
            }

        }
        catch (MalformedURLException ex) {
            logger.log(Level.WARNING, "Failed to determine URL of " + resource + ". Reason: " + ex.getMessage());
            logger.log(Level.FINER, "Details: ", ex);
        }
        return url != null ? url.toString() : null;
    }

    private String getResourceUrlString(GCResource gcResource) {
        if (gcResource instanceof GcResourceFile)
            return ((GcResourceFile) gcResource).getResourceNameAsUrlString();
        else if (gcResource instanceof GcResourceSeries) {
            StringJoiner joiner = new StringJoiner(SERIES_SEPARATOR);
            for (GCResource inner : ((GcResourceSeries) gcResource).getResourcesInOrder()) {
                joiner.add(((GcResourceFile) inner).getResourceNameAsUrlString());
            }
            return joiner.toString();
        }
        else
            throw new IllegalArgumentException("Unknown GCResource type!");
    }

    /**
     * Get all resources names as an array of strings.
     *
     * @return resource names as array of strings
     */
    public List<GCResource> getGCResourceList() {
        List<GCResource> resources = new ArrayList<>();
        for (String entry : gcResourceList) {
            GCResource resource = getGcResource(entry);
            resources.add(resource);
        }

        return resources;
    }

    private GCResource getGcResource(String entry) {
        GCResource resource;
        if (entry.contains(SERIES_SEPARATOR)) {
            resource = getGcResourceSeries(entry);
        }
        else {
            resource = new GcResourceFile(entry);
        }
        return resource;
    }

    private GCResource getGcResourceSeries(String entry) {
        GCResource resource;
        List<GCResource> series = new ArrayList<>();
        for (String s : entry.split(SERIES_SEPARATOR)) {
            series.add(new GcResourceFile(s));
        }

        resource = new GcResourceSeries(series);
        return resource;
    }

    /**
     * Get all resource names of the group formatted as URLs separated by a ";"
     *
     * @return single string with all resource names separated by a ";"
     */
    public String getUrlGroupString() {
        StringBuilder sb = new StringBuilder();
        for (String resource : gcResourceList) {
            sb.append(resource).append(RESOURCE_SEPARATOR);
        }

        return sb.toString();
    }

    /**
     * Get short version of resource names.<br>
     * If more than one resource is in this group, returns only file name without path.
     * {@link GcResourceSeries} are abbreviated by showing the name of the first file and how many files are following.
     *
     * @return get short group name (only file name without path), if there is more than one
     * resource
     */
    public String getGroupStringShort() {
        if (gcResourceList.size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (String resourceName : gcResourceList) {
                resourceName = shortenGroupStringForSeries(resourceName);
                // test for "/" and "\\" because in Windows you have a "/" in a http url
                // but "\\" in file strings
                int lastIndexOfPathSeparator = resourceName.lastIndexOf("/");
                if (lastIndexOfPathSeparator < 0) {
                    lastIndexOfPathSeparator = resourceName.lastIndexOf("\\");
                }
                sb.append(resourceName.substring(lastIndexOfPathSeparator + 1)).append(";");
            }
            return sb.toString();
        }
        else {
            String resourceName = gcResourceList.get(0);
            return shortenGroupStringForSeries(resourceName);
        }
    }

    private String shortenGroupStringForSeries(String resourceName) {
        String[] splitBySeriesSeparator = resourceName.split(SERIES_SEPARATOR);
        if(splitBySeriesSeparator.length > 1)
        {
            // Series: Shorten description by showing first entry only + number of remaining files
            resourceName = splitBySeriesSeparator[0] + " (series, " + (splitBySeriesSeparator.length -1) +" more files)";
        }
        return resourceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GCResourceGroup other = (GCResourceGroup) obj;
        if (gcResourceList == null) {
            if (other.gcResourceList != null)
                return false;
        }
        else if (!gcResourceList.equals(other.gcResourceList))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gcResourceList == null) ? 0 : gcResourceList.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "RecentGCResourceGroup [gcResourceList=" + gcResourceList + "]";
    }
}
