package com.vorso;

/** Plugin
 * 
 * A datastore class for information about a VST/VST3 Plugin
 */
public class Plugin {
    public String name;
    public Boolean working;
    public int numberOfInstances;
    public VST_TYPE vst_type;

    public Plugin(String name, int numberOfInstances, VST_TYPE vst_type, Boolean working) {
        this.name = name;
        this.numberOfInstances = numberOfInstances;
        this.working = working;
        this.vst_type = vst_type;
    }
}
