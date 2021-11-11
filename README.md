# idle_codes
Install
============
Download compiled jar from packages or compile it by yourself from sources
 - Put the jar file wherever you want.
 - Make sure you have Java installed ( https://www.oracle.com/java/technologies/downloads/#jdk17-windows )
 - Associate jar files in windows:
> How to run jar files on windows:
> https://stackoverflow.com/questions/394616/running-jar-file-on-windows

Or just use command line from the folder where jar is located 

![image](https://user-images.githubusercontent.com/1526383/141288018-59dd4ec7-2d04-4905-925b-1dd2d52fc63b.png)

and go > **java -jar idlecodes.jar**

![image](https://user-images.githubusercontent.com/1526383/141287974-7b355d66-f26f-4396-bcc3-f825e3dae517.png)

First run creates **codes.db** for saved codes and **settings.cfg** - for settings.
The programm automatically copy\paste codes into the game. If you want to copy them manualy, you may change the config value in the file **settings.cfg**

HOW TO USE
============
For automatic mode leave the game opened(on active adventure or in "Chest open window") and run the script.

![image](https://user-images.githubusercontent.com/1526383/141297842-c0549a87-962b-4743-a797-5ecc24ab08b7.png)

If you need to stop - just close the cmd or use Ctrl+C break in the command line window.

Additional parameter when starting:
- You may use any number from 1 to parse older comments if needed. 
 
example: **java -jar idlecodes.jar 1** - this will load older comments(1 page older)

This project uses 
+ JNA library for window search and focus
+ OPENCV for finding buttons position
+ Robot for emulating key\mouse press
+ Jsoup for getting codes from web.

V1.1
Fully automated code copy-pasting.  
v1.3
Manual\auto mode in config file. 
Codes are case INsensitive now. Also ignore difference between codes with and without dashes(they read the same)






This program comes with no warranty; for details see the file LICENSE.
