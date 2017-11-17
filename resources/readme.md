<center>

#### Glyph Font Editor, compatible with  
[Squix78' OLED SD1306 screen library V3 format](https://github.com/squix78/esp8266-oled-ssd1306)

This stanalone JavaScript tool was originally developed by Xavier Grosjean ([reivaxy](https://github.com/reivaxy)), see [here](https://github.com/reivaxy/esp8266-oled-ssd1306/blob/master/resources/glyphEditor.html)

#### Tested on Mozilla and Chrome browser (Win10 64)

</center>

1.  **Startup alternatives**

*   Use provided example code: Click "Demo", then "Parse" button
*   Create own font from scratch: Specify font parameters and click "Create" button
*   Load a font from an extranal file (.c .h): click "Browse" button
*   Use "binary input" checkbox to load a binary file
*   If you modify the code in text area, use "Parse" button to apply changes
*   If code contains more fonts, the next can be parsed too, except on binary input

3.  **Edit chars**

*   Draw / erase char pixels by mouse click and drag
*   ✗ Delete this char
*    +Add new char above
*   ←↓↑→ Move all pixels
*   ( Extend occupied width
*   ) Reduce occupied width
*   I Import all pixels from another char
*   C Clean all pixels of current char
*   "Add a character" button : Add new char at the end

5.  **Generate code**

*   Click "Generate" button to preview code

7.  **Save to file**

*   Click "Save To .." button to save generated code of current font to .h file
*   Check "binary" checkbox to save the bytearray to .bin file