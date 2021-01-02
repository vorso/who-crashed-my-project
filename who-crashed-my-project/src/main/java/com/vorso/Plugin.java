package com.vorso;

import java.io.File;

/**
 * Plugin
 * 
 * A datastore class for information about a VST/VST3 Plugin
 */
public class Plugin {
    public String name;
    public Boolean working;
    public int numberOfInstances;
    public PLUGIN_TYPE pluginType;
    public File pluginFile;
    public File isolatedFile;
    public boolean currentlyIsolated;

    public Plugin(String name, int numberOfInstances, PLUGIN_TYPE pluginType, Boolean working) {
        this.name = name;
        this.numberOfInstances = numberOfInstances;
        this.working = working;
        this.pluginType = pluginType;
        currentlyIsolated = false;
    }
}
