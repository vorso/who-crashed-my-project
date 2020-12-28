package com.vorso;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
 *    //
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
 * Soundcloud:  https://soundcloud.com/vorso
 * Spotify:     https://open.spotify.com/artist/5Og6MsfuDPnFYd1asgHXdH
 * Twitter:     https://twitter.com/vorsomusic
 * Facebook:    https://facebook.com/vorsomusic
 */



public class App {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public final static String VST_FOLDER = "C:/Program Files/VSTPlugins";
    public final static String VST3_FOLDER = "C:/Program Files/Common Files/VST3";
    public final static String ABLETON_PATH = "C:/ProgramData/Ableton/Live 10 Suite/Program/Ableton Live 10 Suite.exe";
    public final static String PROJECT_PATH = "C:/Users/vorso/Documents/Dev/who-crashed-my-project/Moog Sesh Project/Moog Sesh.als";
    public final static String ISOLATION_FOLDER = "C:/Users/vorso/Documents/Dev/who-crashed-my-project/Isolation Folder";
    public static String tempFileNameStore = "";

    public static ArrayList<Plugin> plugins = new ArrayList<>();

    public static String linuxPathToWindows(String path) {
        return path.replace("/", "\\");
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException,
            InterruptedException {
        ArrayList<String> VSTPluginList = new ArrayList<>();
        ArrayList<String> VST3PluginList = new ArrayList<>();

        File isolationFolder = new File(linuxPathToWindows(ISOLATION_FOLDER));
        if (!isolationFolder.exists()){
            isolationFolder.mkdirs();
        }
                          
        System.out.println("Open an Ableton .als file:");

        JFileChooser projectFileChooser = new JFileChooser();
		int returnValue = projectFileChooser.showOpenDialog(null);
        
        if (returnValue == JFileChooser.APPROVE_OPTION) {
			File ALSFile = projectFileChooser.getSelectedFile();
			String extension = ALSFile.getAbsolutePath().split("\\.")[1];

			if (extension.equals("als")) {
                    
                String ALSPath = ALSFile.getAbsolutePath();
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
                    VSTPluginList.add(e.getAttribute("Value"));
                }

                NodeList VST3PluginsList = document.getElementsByTagName("Name");
                for(int i = 0; i < VST3PluginsList.getLength(); i++) {
                    Element e = (Element)VST3PluginsList.item(i);

                    if(e.getParentNode().getNodeName().equals("Vst3PluginInfo")) {
                        VST3PluginList.add(e.getAttribute("Value"));
                    }     
                }

                XMLFile.delete();
		    }
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

        if(isolationFolder.listFiles().length == 0) {
            isolationFolder.delete();
        }      
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
            if(plugins.get(i).vst_type == VST_TYPE.VST3 && firstVst3) {
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
            Plugin thisPlugin = pluginsCopy.get(i);

            System.out.println(ANSI_YELLOW + "----------------- Isolating " + thisPlugin.name + ANSI_RESET);

            String vstPath;

            switch(thisPlugin.vst_type) {
                case VST: vstPath = VST_FOLDER; break;
                case VST3: vstPath = VST3_FOLDER; break;
                default: vstPath = VST_FOLDER; break;
            }

            Path isolatedVstPath = isolate(vstPath, thisPlugin.name);

            System.out.println("Opening Ableton...");
            Process openAbleton = Runtime.getRuntime().exec("\"" + linuxPathToWindows(ABLETON_PATH) + "\" \"" + linuxPathToWindows(PROJECT_PATH) + "\"");
            openAbleton.waitFor();
            openAbleton.destroy();
            undoIsolate(isolatedVstPath);

            if(openAbleton.exitValue() == 1) {
                System.out.println(ANSI_GREEN + thisPlugin.name + " (" + thisPlugin.vst_type + ") did not cause the crash." + ANSI_RESET);

                thisPlugin.working = true;

            }
            else {
                System.out.println(ANSI_RED + thisPlugin.name + " (" + thisPlugin.vst_type +  ") caused an error." + ANSI_RESET); 
                thisPlugin.working = false;
                Thread.sleep(3000);

                System.out.println("Opening Ableton again to redect plugin " + thisPlugin.name + " (" + thisPlugin.vst_type +  ")...");
                Process openAbletonAgain = Runtime.getRuntime().exec("\"" + linuxPathToWindows(ABLETON_PATH) + "\" \"" + linuxPathToWindows(PROJECT_PATH) + "\"");
                openAbletonAgain.waitFor();
                openAbletonAgain.destroy();
            }
 
        }
    }

    public static Path isolate(String folder, String pluginName) throws IOException, InterruptedException {

        File pluginDir = new File(linuxPathToWindows(folder));     
        File thisVst = searchFile(pluginDir, pluginName);
        
        tempFileNameStore = thisVst.getAbsolutePath();

        Path isolatedPath = Paths.get(linuxPathToWindows(ISOLATION_FOLDER) + "\\" + thisVst.getName());
        Files.move(Paths.get(thisVst.getAbsolutePath()), isolatedPath,  StandardCopyOption.REPLACE_EXISTING);
        Thread.sleep(1000);

        return isolatedPath;
    }

    public static void undoIsolate(Path isolatedVstPath) throws IOException, InterruptedException {
        Files.move(isolatedVstPath, Paths.get(tempFileNameStore),  StandardCopyOption.REPLACE_EXISTING);
        Thread.sleep(1000);
    }

    public static File searchFile(File file, String search) {
        if(search.contains("Uhbik")) {  //TODO Uhbik vst3 are all grouped into one file
            search = "Uhbik";
        }

        if (file.isDirectory()) {
            File[] arr = file.listFiles();
            for (File f : arr) {
                File found = searchFile(f, search);
                if (found != null)
                    return found;
            }
        } else {
            if (file.getName().contains(search) && !file.getName().contains("(Mono)")) { //TODO added in for Mono fabfilter plugins
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
