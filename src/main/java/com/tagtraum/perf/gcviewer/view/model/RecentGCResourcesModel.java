package com.tagtraum.perf.gcviewer.view.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 * Model managing a list of resource names (or groups of {@link GCResource}s).
 * 
 * <p>Date: Sep 25, 2005</p>
 * <p>Time: 10:54:45 PM</p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RecentGCResourcesModel {

    private final static int MAX_ELEMENTS = 10;
    private List<GCResourceGroup> resourceNameGroupList;
    private List<RecentGCResourcesListener> listeners;
    private Set<GCResource> allResources;

    public RecentGCResourcesModel() {
        this.resourceNameGroupList = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.allResources = new HashSet<>();
    }

    /**
     * Add a list of {@link GCResource} to the model.
     * 
     * @param gcResourceList group of GCResources.
     */
    public void add(List<GCResource> gcResourceList) {
        add(new GCResourceGroup(gcResourceList));
    }
    
    /**
     * Add a single resourceName or a list of names separated by ";".
     * 
     * @param resourceName name or group to be added
     */
    public void add(String resourceName) {
        add(new GCResourceGroup(resourceName));
    }
    
    private void add(GCResourceGroup resourceNameGroup) {
        allResources.addAll(resourceNameGroup.getGCResourceList());
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

    public void addRecentResourceNamesListener(RecentGCResourcesListener recentResourceNamesListener) {
        listeners.add(recentResourceNamesListener);
    }

    protected void fireAddEvent(int position, GCResourceGroup urlSet) {
        RecentGCResourcesEvent event = new RecentGCResourcesEvent(this, position, urlSet); 
        for (RecentGCResourcesListener listener : listeners) {
            listener.add(event);
        }
    }

    protected void fireRemoveEvent(int position) {
        RecentGCResourcesEvent event = new RecentGCResourcesEvent(this, position);
        for (RecentGCResourcesListener listener : listeners) {
            listener.remove(event);
        }
    }

    public List<GCResourceGroup> getResourceNameGroups() {
        return resourceNameGroupList;
    }
    
    public List<String> getResourceNamesStartingWith(String start) {
        List<String> result = new ArrayList<>();
        for (GCResource gcResource : allResources) {
            if (gcResource.getResourceName().startsWith(start)) {
                result.add(gcResource.getResourceName());
            }
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public String toString() {
        return "RecentGCResourcesModel{" +
                "resourceNameGroupList=" + resourceNameGroupList +
                '}';
    }

}
