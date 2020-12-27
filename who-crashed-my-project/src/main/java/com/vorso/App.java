package com.vorso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class App {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        ArrayList<String> VSTPluginList = new ArrayList<>();
        ArrayList<String> VST3PluginList = new ArrayList<>();
             
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

                NodeList VSTPlugins = document.getElementsByTagName("PlugName");
                System.out.println(VSTPlugins.toString());
                System.out.println(VSTPlugins.getLength());

                for(int i = 0; i < VSTPlugins.getLength(); i++) {
                    Element e = (Element)VSTPlugins.item(i);
                    VSTPluginList.add(e.getAttribute("Value"));
                }

                NodeList VST3Plugins = document.getElementsByTagName("Vst3PluginInfo");

                for(int i = 0; i < VST3Plugins.getLength(); i++) {
                    //Element e = (Element)VST3Plugins.item(i);
                    Node n = VST3Plugins.item(i);
                    
                    NodeList childNodes = n.getChildNodes();

                    for(int j = 0; j < childNodes.getLength(); j++){
                        Node item = childNodes.item(i);
                        
                        if(item.getNodeType() != Node.ELEMENT_NODE){
                            continue;
                        }
                        
                        NamedNodeMap attributes = item.getAttributes();
                        System.out.print("     Name : " + attributes.getNamedItem("Name"));
                    }







/*
                    Element e = (Element)VST3Plugins.item(i);
                    NodeList nameList = e.getElementsByTagName("Name");
                    Element ee = (Element)nameList.item(0);
                    String name =  ee.getAttribute("Value");

                     
                    System.out.println(name);*/
                    
                   // NodeList Name = e.getElementsByTagName("Name");
                   // Element n = (Element)Name.item(0);
                   // String stringname = n.getAttribute("Value");

                    


                   // String Name = ((Element)e.getElementsByTagName("Name").item(0)).getAttribute("Value");
                    //System.out.println(stringname);

                    //VSTPluginList.add(e.getAttribute("Value"));
                }
                
                


		    }
        }	

        System.out.println("Open the location of your VST Folder: ");




        //TODO need a list of all the different plugins with tickboxes next to them



        for(int i = 0; i < VSTPluginList.size(); i++) {
            System.out.println(VSTPluginList.get(i));
        }
    }
}
