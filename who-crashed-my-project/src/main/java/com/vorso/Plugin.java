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
    public VST_TYPE vstType;
    public File pluginFile;
    public File isolatedFile;
    public boolean currentlyIsolated;

    public Plugin(String name, int numberOfInstances, VST_TYPE vst_type, Boolean working) {
        this.name = name;
        this.numberOfInstances = numberOfInstances;
        this.working = working;
        this.vstType = vst_type;
        currentlyIsolated = false;
    }
}
