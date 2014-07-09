package com.tagtraum.perf.gcviewer.view.model;

import java.util.Arrays;

/**
 * <p>Holds a group of resource names (those displayed in the same GCDocument).</p>
 * 
 * <p>This class was refactored from "URLSet" inside RecentResourcNamesModel.</p>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 05.03.2014</p>
 */
public class ResourceNameGroup {

    private String[] resourceNames;

    public ResourceNameGroup(String[] resourceNames) {
        this.resourceNames = resourceNames;
        Arrays.sort(this.resourceNames);
    }
    
    public ResourceNameGroup(String resourceNameGroup) {
        if (resourceNameGroup.indexOf(";") >= 0) {
            this.resourceNames = resourceNameGroup.split(";");
        }
        else {
            this.resourceNames = new String[]{ resourceNameGroup };
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResourceNameGroup other = (ResourceNameGroup) obj;
        if (!Arrays.equals(resourceNames, other.resourceNames)) {
            return false;
        }
        return true;
    }

    public String getGroupString() {
        StringBuilder sb = new StringBuilder();
        for (String resourceName : resourceNames) {
            sb.append(resourceName).append(";");
        }
        
        return sb.toString();
    }
    
    public String[] getResourceNames() {
        return resourceNames;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(resourceNames);
        return result;
    }

}