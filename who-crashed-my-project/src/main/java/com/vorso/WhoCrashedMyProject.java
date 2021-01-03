package com.vorso;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 *  \\   //
 *   \\ //
 *    \//
 *    /\\
 *   // \\
 *  //   \\
 *  \\   //
 *   \\ //
 *    \//
 *    //\
 *   // \\
 *  //   \\
 *  \\
 *   \\
 *    \\
 *    // __
 *   // //\\
 *  // // //  
 *  \\// //
 *      //
 *     // 
 *    //
 *   //  \\
 *  //    \\
 *  \\    //
 *   \\  //
 *    \\//
 * 
 * Created by Vorso
 * If WhoCrashedMyProject helped you out with a corrupted project 
 * please consider supporting my stuff here:
 * 
 * Soundcloud:  https://soundcloud.com/vorso
 * Spotify:     https://open.spotify.com/artist/5Og6MsfuDPnFYd1asgHXdH
 * Twitter:     https://twitter.com/vorsomusic
 * Facebook:    https://facebook.com/vorsomusic
 * Youtube:     https://www.youtube.com/c/VorsoMusic
 * Bandcamp:    https://vorso.bandcamp.com/releases
 * 
 * Thanks :^)
 */

public class WhoCrashedMyProject {

    public static OS USER_OS;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_PURPLE = "\u001B[35m";

    public static File Vst_Folder;
    public static File Vst3_Folder;
    public static File Au_Folder;
    public static File Ableton;
    public static File Project_File;
    public static File Isolation_Folder;

    public static Plugin currentPlugin;

    public static ArrayList<Plugin> plugins = new ArrayList<>();

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException,
            InterruptedException {
        
                try {
                    USER_OS = checkOS();

                    ArrayList<String> VSTPluginList = new ArrayList<>();
                    ArrayList<String> VST3PluginList = new ArrayList<>();
                    ArrayList<String> AUPluginList = new ArrayList<>();

                    if(!checkPropertiesFileExists()) {
                        return;
                    }

                    if(!parseProperties()){
                        return;
                    }

                    Isolation_Folder = new File(Paths.get("Isolation Folder").toString());
                    if (!Isolation_Folder.exists()){
                        Isolation_Folder.mkdirs();
                    }

                    if(!checkFolders()) {
                        return;
                    }

                    System.out.println("Open an Ableton .als file:");

                    JFileChooser projectFileChooser = new JFileChooser();
                    int returnValue = projectFileChooser.showOpenDialog(null);
                    
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        Project_File = projectFileChooser.getSelectedFile();
                        String extension = Project_File.getAbsolutePath().split("\\.")[1];

                        if (extension.equals("als")) {
                                
                            String ALSPath = Project_File.getAbsolutePath();
                            String XMLPath = ALSPath.split("\\.")[0].concat(".xml");
                            GZipper zipper = new GZipper();
                            zipper.decompressGzipFile(ALSPath, XMLPath);
                            File XMLFile = new File(XMLPath);
                            
                            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();  
                            Document document = documentBuilder.parse(XMLFile);  
                            document.getDocumentElement().normalize(); 

                            NodeList VSTPluginsNodeList = document.getElementsByTagName("PlugName");

                            for(int i = 0; i < VSTPluginsNodeList.getLength(); i++) {
                                Element e = (Element)VSTPluginsNodeList.item(i);

                                String name;
                                if(USER_OS == OS.MAC) {
                                    name = e.getAttribute("Value").replace("_x64", "");
                                } else {
                                    name = e.getAttribute("Value");
                                }
                                VSTPluginList.add(name);
                            }

                            NodeList VST3PluginsNodeList = document.getElementsByTagName("Name");
                            for(int i = 0; i < VST3PluginsNodeList.getLength(); i++) {
                                Element e = (Element)VST3PluginsNodeList.item(i);

                                if(e.getParentNode().getNodeName().equals("Vst3PluginInfo")) {
                                    VST3PluginList.add(e.getAttribute("Value"));
                                }     
                            }
                            
                            if(USER_OS == OS.MAC){ 
                                NodeList AUPluginsNodeList = document.getElementsByTagName("Name");
                                for(int i = 0; i < AUPluginsNodeList.getLength(); i++) {
                                    Element e = (Element)AUPluginsNodeList.item(i);
    
                                    if(e.getParentNode().getNodeName().equals("AuPluginInfo")) {
                                        AUPluginList.add(e.getAttribute("Value"));
                                    }     
                                }
                            }

                            XMLFile.delete();
                        } else {
                            System.out.println(ANSI_RED + "Selected file was not a .als file, operation cancelled" + ANSI_RESET);
                            return;
                        }
                    } else {
                        System.out.println("Operation Cancelled");
                        return;
                    }

                    HashMap<String, Integer> vstMap = createVstMap(VSTPluginList);
                    HashMap<String, Integer> vst3Map = createVstMap(VST3PluginList);

                    for (HashMap.Entry<String, Integer> entry : vstMap.entrySet()) {
                        plugins.add(new Plugin(entry.getKey(), entry.getValue(), PLUGIN_TYPE.VST, false));
                    }
                    for (HashMap.Entry<String, Integer> entry : vst3Map.entrySet()) {
                        plugins.add(new Plugin(entry.getKey(), entry.getValue(), PLUGIN_TYPE.VST3, false));
                    }

                    if(USER_OS == OS.MAC){ 
                        HashMap<String, Integer> auMap = createVstMap(AUPluginList);
                        for (HashMap.Entry<String, Integer> entry : auMap.entrySet()) {
                            plugins.add(new Plugin(entry.getKey(), entry.getValue(), PLUGIN_TYPE.AU, false));
                        }
                        System.out.println();
                        System.out.println("---------------- AU Plugins Used: -----------------");
                        printPlugins(auMap);
                    }

                    System.out.println();
                    System.out.println("---------------- VST Plugins Used: ----------------");
                    printPlugins(vstMap);

                    System.out.println();
                    System.out.println("---------------- VST3 Plugins Used: ---------------");      
                    printPlugins(vst3Map);

                    System.out.println();
                    System.out.println("----------------- Beginning Debug: ----------------");
                    System.out.println("If Ableton Live hits an error, please click OK on the error message to continue debug.");           
                    System.out.println("If Ableton Live opens correctly during debug, " + ANSI_RED + "please immediately close ");
                    System.out.println("Ableton Live without changing your project" + ANSI_RESET + " to allow debug to continue.");              
   
                    isolateAndTest();

                    System.out.println();
                    System.out.println("----------------- Crash Report: -------------------");

                    printFinalReport(plugins);

                    System.out.println();
                    System.out.println("Thank you for using WhoCrashedMyProject. I hope it helps!" + System.lineSeparator()
                    + "Here is a link to my music if you want to support me: " 
                    + ANSI_PURPLE + "https://soundcloud.com/vorso" + ANSI_RESET);
                    System.out.println("Cheers :-)");

                    if(Isolation_Folder.listFiles().length == 0) {
                        Isolation_Folder.delete();
                    }   
                }
                catch (Exception e) {
                    System.out.println(ANSI_RED + "Encountered an error. Cleaning up Isolation Folder." + ANSI_RESET);
                    if(currentPlugin.currentlyIsolated) {
                        undoIsolate();
                    }
                    if(Isolation_Folder.listFiles().length == 0) {
                        Isolation_Folder.delete();
                    }   

                    e.printStackTrace();
                }   
    }

    public static OS checkOS(){
        String OSString = System.getProperty("os.name").toLowerCase();
        switch(OSString) {
            case "mac os x": return OS.MAC;
            case "windows": return OS.WINDOWS;
        }
        return OS.WINDOWS;
    }

    public static String parsePath(String path) {
        switch(USER_OS) {
            case MAC: return path;
            case WINDOWS: return path.replace("/", "\\");
            default: return path;
        }
    }

    public static Boolean checkFolders() {
        
        String vstFileExtension = "";
        if(USER_OS == OS.WINDOWS) {
            vstFileExtension = ".dll";
        } else {
            vstFileExtension = ".vst";
        }

        if(USER_OS == OS.MAC) {
            if(searchFile(Au_Folder, ".component") == null) {
                System.out.println(ANSI_RED + "No AU files found in supplied Audio Unit Folder" + ANSI_RESET);
                return false;
            }
        }

        if(searchFile(Vst_Folder, vstFileExtension) == null) {
            System.out.println(ANSI_RED + "No " + vstFileExtension + " files found in supplied VST Folder" + ANSI_RESET);
            return false;
        }

        if(searchFile(Vst3_Folder, ".vst3") == null) {
            System.out.println(ANSI_RED + "No .vst3 files found in supplied VST3 Folder" + ANSI_RESET);
            return false;
        }

        if(!Ableton.isFile()) {
            System.out.println(ANSI_RED + "Ableton application not found in supplied Ableton Folder" + ANSI_RESET);
            return false;
        }
        return true;
    }

    public static Boolean checkPropertiesFileExists() {
        File properties = new File("paths.properties");
        if(!properties.isFile()) {
            System.out.println(ANSI_RED + "Properties file not found." + ANSI_RESET);
            return false;
        }
        return true;
    }

    /** parseProperties
     * 
     * Sets the VST, VST3, AU (Mac Only) and Ableton Paths
     *
     * 
     * @return TRUE if set correctly, FALSE if not
     * @throws IOException
     */
    private static Boolean parseProperties() throws IOException {
           
        List<String> lines = Files.readAllLines(Paths.get("paths.properties"), StandardCharsets.UTF_8);
        String vstFolder = "";
        String vst3Folder = "";
        String auFolder = "";
        String abletonPath = "";

        for(int i = 0; i < lines.size(); i++) {
            if(lines.get(i).contains("VST_FOLDER")) {
                vstFolder = lines.get(i).replace("VST_FOLDER:{", "").replace("}", "").replace(",", "");
            } 
            if(lines.get(i).contains("VST3_FOLDER")) {
                vst3Folder = lines.get(i).replace("VST3_FOLDER:{", "").replace("}", "").replace(",", "");
            } 
            if(lines.get(i).contains("ABLETON_PATH")) {
                abletonPath = lines.get(i).replace("ABLETON_PATH:{", "").replace("}", "").replace(",", "");
            } 
            if(lines.get(i).contains("AU_FOLDER")) {
                auFolder = lines.get(i).replace("AU_FOLDER:{", "").replace("}", "").replace(",", "");
            }
        }

        if("".equals(vstFolder)) {
            System.out.println("VST Folder path not set correctly.");
            return false;
        }
        if("".equals(vst3Folder)) {
            System.out.println("VST3 Folder path not set correctly.");
            return false;
        }
        if("".equals(abletonPath)) {
            System.out.println("Ableton Live 10 path not set correctly.");
            return false;
        }
        if(USER_OS == OS.MAC) {
            if("".equals(auFolder)) {
                System.out.println("Audio Unit Folder path not set correctly.");
                return false;
            }
        }

        Vst_Folder      = new File(parsePath(vstFolder));
        Vst3_Folder     = new File(parsePath(vst3Folder));
        Au_Folder       = new File(parsePath(auFolder));
        Ableton         = new File(parsePath(abletonPath));

        return true;
    }

    public static void printFinalReport(ArrayList<Plugin> plugins) {
 
        System.out.println();

        ArrayList<Plugin> aus = new ArrayList<>();
        ArrayList<Plugin> vsts = new ArrayList<>();
        ArrayList<Plugin> vst3s = new ArrayList<>();

        for(int i = 0; i < plugins.size(); i++) {
            if(plugins.get(i).pluginType == PLUGIN_TYPE.AU) {
                aus.add(plugins.get(i));
            }
            if(plugins.get(i).pluginType == PLUGIN_TYPE.VST) {
                vsts.add(plugins.get(i));
            }
            if(plugins.get(i).pluginType == PLUGIN_TYPE.VST3) {
                vst3s.add(plugins.get(i));
            }
        }

        printPlugins(aus, PLUGIN_TYPE.AU);
        printPlugins(vsts, PLUGIN_TYPE.VST);
        printPlugins(vst3s, PLUGIN_TYPE.VST3);

    }

    public static void printPlugins(ArrayList<Plugin> plugins, PLUGIN_TYPE pluginType) {
        if(USER_OS == OS.MAC){
            if(pluginType == PLUGIN_TYPE.AU) {
                System.out.println("----------------- AUs: ---------------------------");
            }
        }

        else if(pluginType == PLUGIN_TYPE.VST) {
            System.out.println("----------------- VSTs: ---------------------------");
        }
        else if(pluginType == PLUGIN_TYPE.VST3) {
            System.out.println("----------------- VST3s: ---------------------------");
        }

        for(int i = 0; i < plugins.size(); i++) {
           
            Plugin thisPlugin = plugins.get(i);

            if(thisPlugin.working) {
                System.out.print(ANSI_GREEN);
            }
            else { 
                System.out.print(ANSI_RED);
            }

            System.out.print(thisPlugin.name + " ");
            int numberOfDots = 50 - thisPlugin.name.length();
            for(int j = 0; j < numberOfDots; j++) {
                System.out.print(".");
            }
            System.out.print(" " + thisPlugin.numberOfInstances);
            System.out.print(ANSI_RESET);
            System.out.println();
        }
    } 



    public static void isolateAndTest() throws IOException, InterruptedException {

        ArrayList<Plugin> pluginsCopy = new ArrayList<Plugin>(plugins);

        Collections.shuffle(pluginsCopy);
    
        for(int i = 0; i < plugins.size(); i++) {
            currentPlugin = pluginsCopy.get(i);
            
            System.out.println(ANSI_YELLOW + "----------------- Isolating " + currentPlugin.name + ANSI_RESET);

            Path isolatedVstPath = isolate();
            
            if(isolatedVstPath == null) {
                System.out.println(ANSI_PURPLE + "The " + currentPlugin.pluginType.name() + " file " 
                + currentPlugin.name + " could not be found. Skipping test..." + ANSI_RESET);
            } else {
                System.out.println("Opening Ableton...");

                Process openAbleton = OpenAbleton(Project_File);
                openAbleton.waitFor();
                openAbleton.destroy();

                undoIsolate();

                if(openAbleton.exitValue() == 1) {
                    System.out.println(ANSI_GREEN + currentPlugin.name + " (" + currentPlugin.pluginType + ") did not cause the crash." + ANSI_RESET);
                    currentPlugin.working = true;
                }
                else {
                    System.out.println(ANSI_RED + "The project opens correctly without " + 
                    currentPlugin.name + " (" + currentPlugin.pluginType +  ")." + System.lineSeparator() +
                    "This may indicate that this plugin" + 
                    " is causing Ableton Live to crash." + ANSI_RESET); 
                    currentPlugin.working = false;
                    Thread.sleep(3000);

                    System.out.println("Opening Ableton again to redect plugin " + currentPlugin.name + " (" + currentPlugin.pluginType +  ")...");
                    Process openAbletonAgain = OpenAbleton(Project_File);
                    openAbletonAgain.waitFor();
                    openAbletonAgain.destroy();
                }
            }
        }
    }

    public static Process OpenAbleton(File projectFile) throws IOException {
        String[] params = {Ableton.getAbsolutePath(), projectFile.getAbsolutePath()};
        return Runtime.getRuntime().exec(params);
    }

    public static Path isolate() throws IOException, InterruptedException {
        File pluginDir;

        switch(currentPlugin.pluginType) { 
            case AU:    pluginDir = Au_Folder;   break;
            case VST:   pluginDir = Vst_Folder;  break;
            case VST3:  pluginDir = Vst3_Folder; break;
            default:    pluginDir = Vst_Folder;  break;
        } 

        currentPlugin.pluginFile = searchFile(pluginDir, fixPluginSpecificName(currentPlugin.name));
 
        if(currentPlugin.pluginFile == null) {
            return null;
        }

        currentPlugin.isolatedFile = Paths.get(Isolation_Folder.getAbsolutePath(), currentPlugin.pluginFile.getName()).toFile();
        
        if(USER_OS == OS.MAC){ //Mac OS treats plugins as directories, Windows treats them as files
            FileUtils.moveDirectory(currentPlugin.pluginFile, currentPlugin.isolatedFile);
        } 
        else if(USER_OS == OS.WINDOWS) {
            Files.move(currentPlugin.pluginFile.toPath(), currentPlugin.isolatedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        currentPlugin.currentlyIsolated = true;

        Thread.sleep(1000);

        return currentPlugin.isolatedFile.toPath();
    }

    public static void undoIsolate() throws IOException, InterruptedException {

        if(USER_OS == OS.MAC){ //Mac OS treats plugins as directories, Windows treats them as files
            FileUtils.moveDirectory(currentPlugin.isolatedFile, currentPlugin.pluginFile);
        } 
        else if(USER_OS == OS.WINDOWS) {
            Files.move(currentPlugin.isolatedFile.toPath(), currentPlugin.pluginFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        currentPlugin.currentlyIsolated = false;
        Thread.sleep(1000);
    }

    public static String fixPluginSpecificName(String name) {
        if(name.contains("Uhbik")) {  //U-He Uhbik vst3 are all grouped into one file
            return "Uhbik";
        }
        if(name.contains("(Mono)")) { //Fabfilter plugins also have (Mono) Duplicates
            return name.replace("(Mono)", "");
        }
        if(name.contains("FF Pro-")){ //Audio units abreviate the plugin name but not the plugin file name
            return name.replace("FF Pro-", "FabFilter Pro-");
        } 
        return name;
    }

    public static File searchFile(File file, String search) {

        boolean isPluginFile = file.getName().contains(".component") || file.getName().contains(".vst") || file.getName().contains(".vst3");

        if (file.isDirectory() && !isPluginFile) {
            File[] arr = file.listFiles();
            for (File f : arr) { 
                if(!f.getName().contains(".DS_Store")){
                    File found = searchFile(f, search);
                    if (found != null){
                        return found;
                    }
                }
            }
        } else {
            if (file.getName().contains(search) && !file.getName().contains(".DS_Store")) {
                if(file.getName().contains("FX")) { //Some plugins come with an included "FX" version
                    if(search.contains("FX")){
                        return file;
                    }
                } else {
                    return file;
                }
            }
        }
        return null;
    }

    public static HashMap<String, Integer> createVstMap(ArrayList<String> vstList) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        for(int i = 0; i < vstList.size(); i++) {
            String name = vstList.get(i);

            if(map.get(name) == null) {
                map.put(name, 1);
            }
            else {
                map.put(name, map.get(name).intValue() + 1);
            } 
        }
        return map;
    }

    public static void printPlugins(HashMap<String, Integer> map) {
        for (HashMap.Entry<String, Integer> entry : map.entrySet()) {
            int numberOfVsts = entry.getValue();
            String name = entry.getKey();
            
            int numberOfDots = 50 - name.length();

            System.out.print(name +  " ");
            
            for(int i = 0; i < numberOfDots; i++) {
                System.out.print(".");
            }
            
            System.out.println(" " + numberOfVsts);
        }
    }
}
