@echo off
mode con: cols=112 lines=32
set _JAVA_OPTIONS=
prompt $S
echo This is a runner for FontConverterV3.jar 
echo Build from FontConverterV3.java source on NetBeans IDE JDK8, see the link 
echo A standalone tool that creates bitmap arrays from your system font, based on:
echo https://github.com/squix78/esp8266-display-tools
echo https://github.com/squix78/minigrafx/issues/9
echo Java JRE8 is required to run
echo.
echo Command line options:
echo.
echo java -jar FontConverterV3.jar [name [size [stylex] [startchar endchar] [outfirstchar]]]
echo.
echo  without parameters: prints the available font names list to console
echo.
echo  with parameter(s): creates a C code text file and binary file in current directory
echo 	name: font name from the list, use quotes if the name has spaces, example "Courrier New"
echo 	size: font size(height) in px, default 12, converter may output higher size
echo 	stylex: a combination of b=bold i=italic and c=add Cyrillic from char 192 like Windows-1251
echo 	startchar endchar: character set to be parsed, default 32 256, 
echo 	note endchar is last desired +1 and maximum set is 255 chars 
echo 	outfirstchar: specify the output startchar if the previous selection points beyond 255th
echo.
echo.
echo Example 1: save a list of system font names to fonts.txt
echo on
java -jar "%~dp0dist\FontConverterV3.jar" > fonts.txt
@echo off
echo.
echo Example 2: generate C code and binary files for Arial normal Cyrillic
echo on
java -jar "%~dp0dist\FontConverterV3.jar" "Arial" 10 c
java -jar "%~dp0dist\FontConverterV3.jar" "Arial" 16 c
java -jar "%~dp0dist\FontConverterV3.jar" "Arial" 24 c
@echo off
echo.
echo.
pause