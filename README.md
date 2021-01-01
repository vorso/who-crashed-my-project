# who-crashed-my-project

## A VST/VST3 tester for Ableton Live on Windows and Mac OS

I have recently switched OS's to Windows and noticed that I get occasional problems with running external plugins in Ableton Live. One of the projects I was working on would crash immediately on opening. A common way of solving this problem manually is to move each plugin used in the project out of your plugins folder repeatedly, opening the project each time. When the plugin which causes the crash is removed, the project will open correctly. However, as Ableton Live does not provide tools to list what plugins are in a project without opening it, there is no way to narrow down which plugins might be causing the crash. This search can be time consuming to perform for every plugin in your folde. 
WhoCrashedMyProject is a Java program which automates this isolating and testing process for each plugin used in a project. It allows a way for a user to scan the project file for plugins used without opening it with Ableton Live. I created this project using Java and VSCode with Maven package management. 


## How it works:

An Ableton Live Project file with the extension .als is actually a zipped .xml file. You can unzip a project file easily with a zip utility such as 7-zip on Windows or Keka on Mac, yielding a large .xml file containing all information stored about the live project.WhoCrashedMyProject takes advantage of this by reading this .xml file and locating the list of plugins used in the project file. As the actual .als file is not edited in the process this is will not affect the contents of the live project, so the project file will not changed in any way by running this program. You can actually try this process yourself manually -  after adding the file extension .xml to the end of the yielded file you get from unzipping a .als file you can open it in an IDE such as VSCode.  
Once a list of plugins is produced WhoCrashedMyProject creates a temporary isolation folder and each individual plugin into it, opening the Ableton Live project each time. If the project is unaffected by the move and continues to crash then the isolated plugin is likely not contributing to causing the problem. If Ableton Live opens the project with no crash then this indicates that the isolated plugin was the cause. 
After all plugins in the project have been tested, WhoCrashedMyProject generates and displays a crash report, showing each plugin used in the project, the number of instances of each plugin and which plugins caused a crash. 

I have included a sample Ableton Project (Moog Sesh.als) which would crash on opening. You can use it to try out WhoCrashedMyProject, however to re-create the crash you would need to have all the same plugins + plugins versions as I did. The report for this project was:
    
    ---------------- VST Plugins Used: ----------------
    FabFilter Pro-Q 3 ................................. 4
    FabFilter Pro-R ................................... 1
    FabFilter Pro-C 2 ................................. 1
    Serum ............................................. 1
    OTT ............................................... 1

    ---------------- VST3 Plugins Used: ---------------
    MSED .............................................. 1
    Vital ............................................. 1
    Uhbik-G ........................................... 1
    Disperser ......................................... 1
    Pro-Q 3 ........................................... 2
    Pro-R ............................................. 2

...so if you have copies of these plugins you may be able to recreate the crash on Windows. 


## How to use it:

### Before starting: 

IMPORTANT: This program moves VST + VST3 files around on your computer!! PLEASE make sure that you MAKE A BACKUP COPY of your plugin folders before you run this. If WhoCrashedMyProject hits an exception while running it should move the current plugin file back from the isolation folder. However, if the run is cancelled by the user then there is a chance that the currently tested plugin will remain in the isolation folder, which will not be deleted if it is not empty. You can find this folder at /who-crashed-my-project/who-crashed-my-project/Isolation Folder if you notice a plugin is missing. 

Make sure that Ableton is closed before starting.

Open the file paths.properties which is at /who-crashed-my-project/who-crashed-my-project/paths.properties - This file needs to contain:

    VST_FOLDER:{<<Absolute Path to your VST Folder>>},
    VST3_FOLDER:{<<Absolute Path to your VST3 Folder>>},
    PROJECT_PATH:{<<Absolute Path to the .als file of your project>>},
    
    On WINDOWS:
        ABLETON_PATH:{<<Absolute Path to the "Ableton Live 10 Suite.exe" file this is inside the Ableton package Ableton/Live 10 Suite/Program/Ableton Live 10 Suite.exe>>}
    - On MAC:
         ABLETON_PATH:{<<Absolute Path to the "Live" file this is inside the Ableton package Ableton Live 10 Suite/contents/MacOS/Live>>}
         
Run WhoCrashedMyProject as administrator (Plugins folders normally can only be edited by administrators by default on Windows and Mac OS)...
  - ... using Command Line
    - Right click the Command Line icon and click "Run As Administrator".
    - Navigate to the WhoCrashedMyProject directory.
    - Run << mvn compile exec:java -Dexec.mainClass="com.vorso.WhoCrashedMyProject" >> (note: you will need Maven installed to use this command, useful links - https://www.baeldung.com/install-maven-on-windows-linux-mac , https://maven.apache.org/install.html )
    
  - ... using VSCode
    - Right click VSCode and click "Run As Administrator". 
    - Open the WhoCrashedMyProject Folder
    - Open the Terminal window (The results will be output here).
    - Press Ctrl+F5 or click Run > Run without Debugging
    
 
 WhoCrashedMyProject will open a file selection window. Navigate to and select the Ableton Live .als project to be analysed. 

The plugins used in the project will be shown in your terminal, accompanied by the number of instances of each plugin

The debug phase begins. Plugins are isolated in random order. The program will open Ableton Live each time. 
  - If Ableton Live crashes, the user needs to click OK on the dialogue box to allow Live to close.
  - If Ableton Live opens the project successfully, PLEASE ENSURE THAT YOU CLOSE ABLETON LIVE BY CLICKING THE CROSS AFTER IT LOADS. This allows the program to continue testing the rest of the plugins. Halting the program early may leave plugin files in the isolation folder!!
  - Ableton Live will be opened TWICE if a crash did not occur. This is required because Ableton will not re-detect the missing plugin until it is run a second time. 
    
Repeat step 5 until all plugins used in the project have been tested.
  
A final crash report is generated and shown. Plugins which caused a crash are shown in red.

I hope this helps!
