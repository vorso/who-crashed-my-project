package com.vorso;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

//TODO What if the vst file is not on the computer
//TODO Clip _x64

public class WhoCrashedMyProject {

    public static OS USER_OS;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public static String VST_FOLDER = "";
    public static String VST3_FOLDER = "";
    public static String AU_FOLDER = "";
    public static String ABLETON_PATH = "";
    public static String PROJECT_PATH = "";

    public static File Vst_Folder;
    public static File Vst3_Folder;
    public static File Au_Folder;
    public static File Ableton_Path;
    public static File Project_File;


    //public final static String ISOLATION_FOLDER = "./Isolation Folder";
    public static File Isolation_Folder;

    public static String tempFileNameStore = "";
    public static boolean testing = false;

    public static Plugin currentPlugin;

    public static ArrayList<Plugin> plugins = new ArrayList<>();

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException,
            InterruptedException {
        
                try {
                    USER_OS = checkOS();

                    ArrayList<String> VSTPluginList = new ArrayList<>();
                    ArrayList<String> VST3PluginList = new ArrayList<>();

                    if(!checkPropertiesFileExists()) {
                        return;
                    }

                    if(!parseProperties()){
                        return;
                    }

                    System.out.println(Paths.get("Isolation Folder"));
                    Isolation_Folder = new File(Paths.get("Isolation Folder").toString());
                    if (!Isolation_Folder.exists()){
                        Isolation_Folder.mkdirs();
                    }

                    System.out.println(Isolation_Folder.getAbsolutePath());
                    
                    //File isolationFolder = new File(parsePath(ISOLATION_FOLDER));
                    //if (!isolationFolder.exists()){
                    //   isolationFolder.mkdirs();
                // }
            
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

                            NodeList VSTPlugins = document.getElementsByTagName("PlugName");

                            for(int i = 0; i < VSTPlugins.getLength(); i++) {
                                Element e = (Element)VSTPlugins.item(i);

                                String name;
                                if(USER_OS == OS.MAC) {
                                    name = e.getAttribute("Value").replace("_x64", "");
                                } else {
                                    name = e.getAttribute("Value");
                                }
                                VSTPluginList.add(name);
                            }

                            NodeList VST3PluginsList = document.getElementsByTagName("Name");
                            for(int i = 0; i < VST3PluginsList.getLength(); i++) {
                                Element e = (Element)VST3PluginsList.item(i);

                                if(e.getParentNode().getNodeName().equals("Vst3PluginInfo")) {
                                    VST3PluginList.add(e.getAttribute("Value"));
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
                        plugins.add(new Plugin(entry.getKey(), entry.getValue(), VST_TYPE.VST, false));
                    }
                    for (HashMap.Entry<String, Integer> entry : vst3Map.entrySet()) {
                        plugins.add(new Plugin(entry.getKey(), entry.getValue(), VST_TYPE.VST3, false));
                    }
                    
                    System.out.println();
                    System.out.println("---------------- VST Plugins Used: ----------------");
                    printPlugins(vstMap);

                    System.out.println();
                    System.out.println("---------------- VST3 Plugins Used: ---------------");      
                    printPlugins(vst3Map);

                    System.out.println();
                    System.out.println("----------------- Beginning Debug: ----------------");
                                    
                    isolateAndTest(vstMap);

                    System.out.println();
                    System.out.println("----------------- Crash Report: -------------------");

                    printFinalReport(plugins);

                    System.out.println();
                    System.out.println("Thank you for using WhoCrashedMyProject. I hope it helps. Click here to support me - https://soundcloud.com/vorso");
                    System.out.println("Cheers :-)");

                    if(Isolation_Folder.listFiles().length == 0) {
                        Isolation_Folder.delete();
                    }   
                }
                catch (Exception e) {
                    System.out.println(ANSI_RED + "Encountered an error. Cleaning up Isolation Folder." + ANSI_RESET);
                    if(testing) {
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
            if(searchFile(new File(AU_FOLDER), ".component") == null) {
                System.out.println(ANSI_RED + "No AU files found in supplied Audio Unit Folder" + ANSI_RESET);
                return false;
            }
        }

        if(searchFile(new File(VST_FOLDER), vstFileExtension) == null) {
            System.out.println(ANSI_RED + "No " + vstFileExtension + " files found in supplied VST Folder" + ANSI_RESET);
            return false;
        }

        if(searchFile(new File(VST3_FOLDER), ".vst3") == null) {
            System.out.println(ANSI_RED + "No .vst3 files found in supplied VST3 Folder" + ANSI_RESET);
            return false;
        }

        File abletonFile = new File(ABLETON_PATH);
        if(!abletonFile.isFile()) {
            System.out.println(ANSI_RED + "Ableton application project file found in supplied Ableton file" + ANSI_RESET);
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
        Ableton_Path    = new File(parsePath(abletonPath));

        VST_FOLDER      = parsePath(vstFolder);
        VST3_FOLDER     = parsePath(vst3Folder);
        AU_FOLDER       = parsePath(auFolder);
        ABLETON_PATH    = parsePath(abletonPath);

        return true;
    }

    public static void printFinalReport(ArrayList<Plugin> plugins) {
        Boolean firstVst = true;
        Boolean firstVst3 = true;      
        System.out.println();

        for(int i = 0; i < plugins.size(); i++) {
            if(firstVst) {
                System.out.println("----------------- VSTs: ---------------------------");
                firstVst = false;
            }
            if(plugins.get(i).vstType == VST_TYPE.VST3 && firstVst3) {
                System.out.println();
                System.out.println("----------------- VST3s: --------------------------");
                firstVst3 = false;
            }

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

    public static void isolateAndTest(HashMap<String, Integer> vstMap) throws IOException, InterruptedException {

        ArrayList<Plugin> pluginsCopy = new ArrayList<Plugin>(plugins);

        Collections.shuffle(pluginsCopy);
    
        for(int i = 0; i < plugins.size(); i++) {
            currentPlugin = pluginsCopy.get(i);
            
            System.out.println(ANSI_YELLOW + "----------------- Isolating " + currentPlugin.name + ANSI_RESET);

            File pluginFolder;

            switch(currentPlugin.vstType) { //TODO Add AU
                case VST:   pluginFolder = Vst_Folder; break;
                case VST3:  pluginFolder = Vst3_Folder; break;
                default:    pluginFolder = Vst_Folder; break;
            } 

            Path isolatedVstPath = isolate(pluginFolder, currentPlugin.name);

            testing = true;
            
            if(isolatedVstPath == null) {
                System.out.println("The " + currentPlugin.vstType.name() + " file " + currentPlugin.name + " could not be found. Skipping test...");
            } else {
                System.out.println("Opening Ableton...");

                Process openAbleton = OpenAbleton(Project_File);
                openAbleton.waitFor();
                openAbleton.destroy();

                undoIsolate();
                testing = false;

                if(openAbleton.exitValue() == 1) {
                    System.out.println(ANSI_GREEN + currentPlugin.name + " (" + currentPlugin.vstType + ") did not cause the crash." + ANSI_RESET);
                    currentPlugin.working = true;
                }
                else {
                    System.out.println(ANSI_RED + "The project opens correctly without " + 
                    currentPlugin.name + " (" + currentPlugin.vstType +  "). This may indicate that this plugin" + 
                    " is causing the crash." + ANSI_RESET); 
                    currentPlugin.working = false;
                    Thread.sleep(3000);

                    System.out.println("Opening Ableton again to redect plugin " + currentPlugin.name + " (" + currentPlugin.vstType +  ")...");
                    Process openAbletonAgain = OpenAbleton(Project_File);
                    openAbletonAgain.waitFor();
                    openAbletonAgain.destroy();
                }
            }
        }
    }

    public static Process OpenAbleton(File projectFile) throws IOException {
        String[] params = {Ableton_Path.getAbsolutePath(), projectFile.getAbsolutePath()};
        return Runtime.getRuntime().exec(params);
    }

    public static Path isolate(File pluginDir, String pluginName) throws IOException, InterruptedException {

        currentPlugin.pluginFile = searchFile(pluginDir, pluginName);
 
        if(currentPlugin.pluginFile == null) {
            return null;
        }

        currentPlugin.isolatedFile = Paths.get(Isolation_Folder.getAbsolutePath(), currentPlugin.pluginFile.getName()).toFile();
        FileUtils.moveDirectory(currentPlugin.pluginFile, currentPlugin.isolatedFile);

        /*
        try {
            Files.move(Paths.get(thisVst.getAbsolutePath()), isolatedPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.DirectoryNotEmptyException e) {
            System.out.println(ANSI_RED + "Cannot move files between volumes" + ANSI_RESET);
        }*/

        //String path1 = Paths.get(thisVst.getAbsolutePath()).toString();
       // String path2 = isolatedPath.toString();

        //Boolean didItWork = thisVst.renameTo(new File(path2));
        Thread.sleep(1000);


        return currentPlugin.isolatedFile.toPath();
    }

    public static void undoIsolate() throws IOException, InterruptedException {

        FileUtils.moveDirectory(currentPlugin.isolatedFile, currentPlugin.pluginFile);

        //Files.move(isolatedVstPath, Paths.get(tempFileNameStore),  StandardCopyOption.REPLACE_EXISTING);
        Thread.sleep(1000);
    }

    public static File searchFile(File file, String search) {
        if(search.contains("Uhbik")) {  //U-He Uhbik vst3 are all grouped into one file
            search = "Uhbik";
        }
 
    /*

    This doesnt work!!!!!!!!!! It only works if you want the FIRST thing you find thats a vst / au. 
    You need to be looking for a SPECIFIC thing
    */
    /*
        if(file.getName().contains(".component")){
            return file;
        }
        if(file.getName().contains(".vst")){
            return file;
        }
        if(file.getName().contains(".vst3")){
            return file;
        }*/

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
            if (file.getName().contains(search) && !file.getName().contains("(Mono)")) { //Fabfilter plugins also have (Mono) Duplicates
                return file;
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
