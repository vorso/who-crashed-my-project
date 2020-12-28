# who-crashed-my-project

## A VST/VST3 tester for Ableton Live on Windows

I have recently switched OS's to Windows and noticed that I get occasional problems with running external plugins in Ableton Live. I recently created a project which would crash instantly on opening. A solution for this is to disable each VST / VST3 plugin used in the project one by one until the project loads correctly. This search can be time consuming to perform, so I created this software to automate the process. 

## How it works:

An Ableton Live Project file with extension .als is selected via the file chooser. An Ableton Live .als file is a GZIP'd .xml file. The program starts by unzipping this, yielding an xml file that is loaded in and parsed (note: you can unzip a .als file very easily using an unzipper such as 7-zip. Rename the produced file to include the file extension .xml and you can open it in VSCode or equivalent). The names and types (VST / VST3) of all plugins used in the project is analysed and listed to the user. 
The debug phase then begins. An isolation folder is created temporarily. The program moves a single plugin which is used in the project into the isolation folder and attempts to open Ableton Live. The project will be loaded with all but the isolated plugin. The project loading will either crash, indicating that the currently excluded plugin was not responsible for the crash on loading, or will load as expected, indicating the excluded plugin was causing the crash. The program will wait until the user dismisses the error dialogue box or closes Ableton, and then will repeat for the next plugin. Finally, once all plugins have been tested, a crash report is generated and shown on the command line to the user. 

I have included the project which was giving me errors. You can try WhoCrashedMyProject out using Moog Sesh.als.


## How to use it:

Before starting: This program moves VST + VST3 files around on your computer!! PLEASE make sure that you MAKE A BACKUP COPY of your plugin folders before you run this. If you notice any missing plugins at the end, they may be in the Isolation Folder, which is located in the outer program directory (who-crashed-my-project).

1. Run WhoCrashedMyProject. 
  - Command Line
    - Right click the Command Line and click "Run As Administrator".
    - Navigate to the WhoCrashedMyProject directory.
    - Run << mvn compile exec:java -Dexec.mainClass="com.vorso.WhoCrashedMyProject" >>
    
  - VSCode
    - Right click VSCode and click "Run As Administrator". 
    - Open the WhoCrashedMyProject Folder
    - Open the Terminal window (This results will be output).
    - Press Ctrl+F5 or click Run > Run without Debugging
    
    
 2. The VST + VST3 folders can only by edited by adiministrators by default. 
  If you are running in an IDE such as VSCode, run your IDE as Administrator. 
 
3. The program will open a dialogue box. Select the Ableton Live .als project to be analysed. 

4. The VST + VST3 plugins used in the project will be shown in your terminal, accompanyed by the number of instances of each plugin

5. The debug phase begins. Plugins are isolated in random order. The program will open Ableton Live each time. 
  - If Ableton Live crashes, the user needs to click OK on the dialogue box to allow Live to close.
  - If Ableton Live opens the project successfully, PLEASE ENSURE THAT YOU CLOSE ABLETON LIVE BY CLICKING THE CROSS AFTER IT LOADS. This allows the program to continue testing the rest of the plugins. Halting the program early may leave plugin files in the temporary folder!!
    Ableton Live will be opened TWICE if a crash did not occur. This is required because Ableton will not re-detect the missing plugin. 
  
6. A final crash report is generated and shown. Plugins which caused a crash are shown in red.

I hope this helps!
