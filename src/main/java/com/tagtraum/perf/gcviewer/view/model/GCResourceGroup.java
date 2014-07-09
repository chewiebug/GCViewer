package com.tagtraum.perf.gcviewer.view.model;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 * <p>Holds a group of resource names (those displayed in the same GCDocument).</p>
 * 
 * <p>This class was refactored from "URLSet".</p>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 05.03.2014</p>
 */
public class GCResourceGroup {

    private List<GCResource> gcResourceList;

    public GCResourceGroup(List<GCResource> gcResourceList) {
        this.gcResourceList = gcResourceList;
    }
    
    /**
     * Initialise a group from a single string consisting of resource names separated by ";"
     * 
     * @param resourceNameGroup resource names separated by ";"
     */
    public GCResourceGroup(String resourceNameGroup) {
        if (resourceNameGroup.indexOf(";") >= 0) {
            setGCResourceList(resourceNameGroup.split(";"));
        }
        else {
            setGCResourceList(new String[]{ resourceNameGroup });
        }
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
        } else if (!gcResourceList.equals(other.gcResourceList))
            return false;
        return true;
    }

    /**
     * Get all resources names as an array of strings.
     * 
     * @return resource names as array of strings
     */
    public List<GCResource> getGCResourceList() {
        return Collections.unmodifiableList(gcResourceList);
    }

    /**
     * Get all resource names of the group formatted as URLs separated by a ";"
     * 
     * @return single string with all resource names separated by a ";"
     */
    public String getUrlGroupString() {
        StringBuilder sb = new StringBuilder();
        for (GCResource gcResource : gcResourceList) {
            try {
                sb.append(gcResource.getResourceNameAsUrl().toString()).append(";");
            }
            catch (MalformedURLException e) {
                // ignore it
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Get short version of resource names (only file name without path), if more than one
     * resource is in this group.
     * 
     * @return get short group name (only file name without path), if there is more than one
     * resource
     */
    public String getGroupStringShort() {
        if (gcResourceList.size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (GCResource gcResource : gcResourceList) {
                // test for "/" and "\\" because in Windows you have a "/" in a http url
                // but "\\" in file strings
                String resourceName = gcResource.getResourceName();
                int lastIndexOfPathSeparator = resourceName.lastIndexOf("/");
                if (lastIndexOfPathSeparator < 0) {
                    lastIndexOfPathSeparator = resourceName.lastIndexOf("\\");
                }
                sb.append(resourceName.substring(lastIndexOfPathSeparator + 1)).append(";");
            }
            return sb.toString();
        }
        else {
            return gcResourceList.get(0).getResourceName();
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((gcResourceList == null) ? 0 : gcResourceList.hashCode());
        return result;
    }

    private void setGCResourceList(String[] resourceNames) {
        gcResourceList = new ArrayList<>();
        for (String resourceName : resourceNames) {
            gcResourceList.add(new GCResource(resourceName));
        }
    }

    @Override
    public String toString() {
        return "RecentGCResourceGroup [gcResourceList=" + gcResourceList + "]";
    }

}