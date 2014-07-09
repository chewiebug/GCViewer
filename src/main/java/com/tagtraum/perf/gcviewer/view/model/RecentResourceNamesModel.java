package com.tagtraum.perf.gcviewer.view.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Model managing a list of resource names (or groups of resource names).
 * 
 * <p>Date: Sep 25, 2005</p>
 * <p>Time: 10:54:45 PM</p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RecentResourceNamesModel {

    private final static int MAX_ELEMENTS = 10;
    private List<ResourceNameGroup> resourceNameGroupList;
    private List<RecentResourceNamesListener> listeners;
    private Set<String> allResources;

    public RecentResourceNamesModel() {
        this.resourceNameGroupList = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.allResources = new HashSet<>();
    }

    /**
     * Add the resourceNames as a group.
     * 
     * @param resourceNames group of resource names.
     */
    public void add(String[] resourceNames) {
        add(new ResourceNameGroup(resourceNames));
    }
    
    /**
     * Add a single resourceName or a list of names separated by ";".
     * 
     * @param resourceName name or group to be added
     */
    public void add(String resourceName) {
        add(new ResourceNameGroup(resourceName));
    }
    
    private void add(ResourceNameGroup resourceNameGroup) {
        allResources.addAll(Arrays.asList(resourceNameGroup.getResourceNames()));
        int index = resourceNameGroupList.indexOf(resourceNameGroup);
        if (index < 0) {
            resourceNameGroupList.add(0, resourceNameGroup);
            fireAddEvent(0, resourceNameGroup);
            if (resourceNameGroupList.size() > MAX_ELEMENTS) {
                // if list size is too big remove last element
                resourceNameGroupList.remove(MAX_ELEMENTS - 1);
                fireRemoveEvent(MAX_ELEMENTS - 1);
            }
        }
        else {
            resourceNameGroupList.remove(index);
            fireRemoveEvent(index);
            resourceNameGroupList.add(0, resourceNameGroup);
            fireAddEvent(0, resourceNameGroup);
        }
    }

    public void addRecentResourceNamesListener(RecentResourceNamesListener recentResourceNamesListener) {
        listeners.add(recentResourceNamesListener);
    }

    protected void fireAddEvent(int position, ResourceNameGroup urlSet) {
        RecentResourceNamesEvent event = new RecentResourceNamesEvent(this, position, urlSet); 
        for (RecentResourceNamesListener listener : listeners) {
            listener.add(event);
        }
    }

    protected void fireRemoveEvent(int position) {
        RecentResourceNamesEvent event = new RecentResourceNamesEvent(this, position);
        for (RecentResourceNamesListener listener : listeners) {
            listener.remove(event);
        }
    }

    public List<ResourceNameGroup> getResourceNameGroups() {
        return resourceNameGroupList;
    }
    
    public List<String> getResourceNamesStartingWith(String start) {
        List<String> result = new ArrayList<>();
        for (String url : allResources) {
            String urlString = url.toString();
            if (urlString.startsWith(start)) {
                result.add(urlString);
            }
        }
        Collections.sort(result);
        return result;
    }

}
