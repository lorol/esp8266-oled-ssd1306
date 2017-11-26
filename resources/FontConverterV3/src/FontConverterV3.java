package ch.squix.esp8266.fontconverter.rest;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://learn.adafruit.com/adafruit-gfx-graphics-library/using-fonts
 * https://github
 * .com/adafruit/Adafruit-GFX-Library/blob/master/fontconvert/fontconvert.c
 * https ://github.com/adafruit/Adafruit-GFX-Library/blob/master/Fonts/
 * FreeMono18pt7b .h
 * 
 * table[j].bitmapOffset = bitmapOffset; table[j].width = bitmap->width;
 * table[j].height = bitmap->rows; table[j].xAdvance = face->glyph->advance.x >>
 * 6; table[j].xOffset = g->left; table[j].yOffset = 1 - g->top;
 * 
 * bitmapOffset += (bitmap->width * bitmap->rows + 7) / 8;
 * 
 * Pad end of char bitmap to next byte boundary if needed
 * 
 * @author deichhor
 * 
 */
public class FontConverterV3 {

    private static char endChar = 256;
    private static char startChar = 32;
    private static char firstChar = 32;
    private static boolean cyr = false;

    private Graphics2D g;
    private FontMetrics fontMetrics;
    private BufferedImage image;


    public FontConverterV3(Font font) {
        image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setFont(font);
        fontMetrics = g.getFontMetrics();
    }


    public static void main(String[] args) throws InterruptedException, IOException {
         
        String FontName;
        String FontSize;
        String FontStyle = "";
        Integer fstyle = Font.PLAIN;
       
        if (args.length < 1) {
            GraphicsEnvironment graphicEnvironment =
            GraphicsEnvironment.getLocalGraphicsEnvironment(); for (Font font :
            graphicEnvironment.getAllFonts()) {
            System.out.println(font.getFontName()); }
        } else {
            FontName = args[0];
            if (args.length < 2) FontSize = "12";
            else FontSize = args[1];
            
            if (args.length < 3) fstyle = Font.PLAIN;
            else {
                FontStyle = args[2];
                if (FontStyle.toLowerCase().contains("b")) fstyle |= Font.BOLD;
                if (FontStyle.toLowerCase().contains("i")) fstyle |= Font.ITALIC;
                if (FontStyle.toLowerCase().contains("c")) cyr = true;
            }
            
            if (args.length >= 5) {
                startChar = (char) Integer.parseInt(args[3]);
                endChar = (char) Integer.parseInt(args[4]);
                firstChar = startChar;
            }
            
            if (args.length >= 6) firstChar = (char) Integer.parseInt(args[5]);
            firstChar &= 0xFF;
            
            String fileName = (FontName  + "_" + FontStyle + "_" + FontSize).replaceAll("[\\s\\-\\.]", "_");

            StringBuilder builder = new StringBuilder();
            FontConverterV3 app = new FontConverterV3(new Font(FontName, fstyle, Integer.parseInt(FontSize)));
            
            //app.writeBinaryFontFile("TimesRegular30.mxf");
            //app = new FontConverterV3(new Font("Meteocons", Font.PLAIN, 21));
            //app.printLetterData(builder);
            //app = new FontConverterV3(new Font("Meteocons", Font.PLAIN, 10));
            //app.printLetterData(builder);  
            
           byte[] bytes = app.getBinaryOutputStream().toByteArray();
           try (FileOutputStream fos = new FileOutputStream(fileName + ".V3.bin"))  {
            fos.write(bytes);
            fos.close();
           } catch (IOException e) {
                  System.out.println(e);
           }
           //System.out.println(bytes.length); 
            
            app.printLetterData(builder);
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName + ".V3.h"))) {
                bw.write(builder.toString());
                bw.flush();
            } catch (IOException e) {
                  System.out.println(e);
            }
            //System.out.println(builder);
 
        }
    }


    public ByteArrayOutputStream getBinaryOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        List<LetterData> letterList = produceLetterDataList();
        os.write(getMaxCharWidth());
        os.write(getMaxCharHeight());
        os.write(firstChar);
        os.write(endChar - startChar);

        int lastJumpPoint = 0;
        for (LetterData letter : letterList) {
            int letterWidth = letter.getWidth();
            int size = letter.getByteSize();
            String code = "" + ((int) letter.getCode());
            if (letter.isVisable()) {
                writeBinaryJumpTable(os, code, lastJumpPoint, size, letterWidth);
                lastJumpPoint += size;
            } else {
                writeBinaryJumpTable(os, code, 0xFFFF, size, letterWidth);
            }
        }

        for (LetterData letter : letterList) {
            if (letter.isVisable()) {
                for (int data : letter.getBytes()) {
                    os.write((byte) data);
                }

            }
        }
        return os;
    }


    public void printLetterData(StringBuilder builder) {
        List<LetterData> letterList = produceLetterDataList();

        String fontName = g.getFont().getFontName().replaceAll("[\\s\\-\\.]", "_") + "_"
                + g.getFont().getSize();
        builder.append("// Created by http://oleddisplay.squix.ch/ Consider a donation\n");
        builder.append("// In case of problems make sure that you are using the font file with the correct version!\n");
        builder.append("const char " + fontName + "[] PROGMEM = {\n");
        writeHexValue(builder, "Width", getMaxCharWidth());
        writeHexValue(builder, "Height", getMaxCharHeight());
        writeHexValue(builder, "First Char", firstChar);
        writeHexValue(builder, "Numbers of Chars", endChar - startChar);
        builder.append("\n");
        builder.append("\t// Jump Table:\n");

        int lastJumpPoint = 0;
        for (LetterData letter : letterList) {
            int letterWidth = letter.getWidth();
            int size = letter.getByteSize();
            String code = "" + ((int) letter.getCode());
            if (letter.isVisable()) {
                writeJumpTable(builder, code, lastJumpPoint, size, letterWidth);
                lastJumpPoint += size;
            } else {
                writeJumpTable(builder, code, 0xFFFF, size, letterWidth);
            }
        }

        builder.append("\n");
        builder.append("\t// Font Data:\n");

        for (LetterData letter : letterList) {
            if (letter.isVisable()) {
                builder.append("\t");
                builder.append(letter.toString());
                if ((int) letter.getCode() != endChar - 1) {
                    builder.append(",");
                }
                builder.append("\t// " + (int) letter.getCode() + "\n");
            }
        }

        builder.append("};\n");
    }

    public List<LetterData> produceLetterDataList() {
        ArrayList<LetterData> letterDataList = new ArrayList<>(endChar - startChar);
        for (char i = startChar; i < endChar; i++) {
            if (cyr) {
                if (i == 168) letterDataList.add(createLetterData((char)(1025))); //Ё
                else if (i == 184) letterDataList.add(createLetterData((char) (1105))); //ё   
                else if ((i > 126)&&(i < 160)) letterDataList.add(createLetterData((char) (32))); // clean unused symbols to space
                else if (i < 192) letterDataList.add(createLetterData(i));
                else letterDataList.add(createLetterData((char) (i + 848)));
            }
            else letterDataList.add(createLetterData(i));
        }
        return letterDataList;
    }

    public LetterData createLetterData(char code) {
        BufferedImage letterImage = drawLetter(code);

        int width = fontMetrics.charWidth(code);
        int height = fontMetrics.getHeight();

        int arrayHeight = (int) Math.ceil((double) height / 8.0);
        int arraySize = width * arrayHeight;

        int character[] = new int[arraySize];

        boolean isVisableChar = false;

        if (width > 0) {
            for (int i = 0; i < arraySize; i++) {
                int xImg = (i / arrayHeight);
                int yImg = (i % arrayHeight) * 8;
                int currentByte = 0;
                for (int b = 0; b < 8; b++) {
                    if (yImg + b <= height) {
                        if (letterImage.getRGB(xImg, yImg + b) == Color.BLACK.getRGB()) {
                            isVisableChar = true;
                            currentByte = currentByte | (1 << b);
                        } else {
                            currentByte = currentByte & ~(1 << b);
                        }
                    }
                }

                character[i] = (byte) currentByte;
            }
        }

        // Remove rightmost zeros to save bytes
        int lastByteNotNull = -1;
        for (int i = 0; i < character.length; i++) {
            if (character[i] != 0)
                lastByteNotNull = i;
        }

        character = Arrays.copyOf(character, lastByteNotNull + 1);

        return new LetterData(code, character, width, height, isVisableChar);
    }

    public BufferedImage drawLetter(char code) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 450, 250);
        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(code), 0, fontMetrics.getAscent() + fontMetrics.getLeading());
        return image;
    }


    private String getFontStyle() {
        Font font = g.getFont();
        if (font.isPlain()) {
            return "Plain";
        } else if (font.isItalic() && font.isBold()) {
            return "ItalicBold";
        } else if (font.isBold()) {
            return "Bold";
        } else if (font.isItalic()) {
            return "Italic";
        }
        return "";
    }

    private void writeBinaryJumpTable(OutputStream fos, String label, int jump, int size, int width) throws IOException {
        fos.write((jump >> 8) & 0xFF); // MSB
        fos.write((jump & 0xFF)); // LSB
        fos.write((size)); // byteSize
        fos.write((width)); // WIDTH
        //builder.append(String.format(" // %s:%s", label, jump) + "\n");
    }

    private void writeJumpTable(StringBuilder builder, String label, int jump, int size, int width) {
        builder.append(String.format("\t0x%02X, ", (jump >> 8) & 0xFF)); // MSB
        builder.append(String.format("0x%02X, ", jump & 0xFF)); // LSB
        builder.append(String.format("0x%02X, ", size)); // byteSize
        builder.append(String.format("0x%02X, ", width)); // WIDTH
        builder.append(String.format(" // %s:%s", label, jump) + "\n");
    }

    private void writeHexValue(StringBuilder builder, String label, int value) {
        builder.append(String.format("\t0x%02X, // %s: %d", value, label, value));
        builder.append("\n");
    }

    public int getMaxCharWidth() {
        int maxWidth = 0;
        for (int i = startChar; i < endChar; i++) {
            maxWidth = Math.max(maxWidth, fontMetrics.charWidth((char) i));
        }
        return maxWidth;
    }

    public int getMaxCharHeight() {
        return fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent() + fontMetrics.getLeading();
    }

    public class LetterData {

        private char code;

        private int[] bytes;
        private int width;
        private int height;

        private boolean visable;

        public LetterData(char code, int[] bytes, int width, int height, boolean visable) {
            this.code = code;
            this.bytes = bytes;
            this.width = width;
            this.height = height;
            this.visable = visable;
        }

        public char getCode() {
            return code;
        }

        public int[] getBytes() {
            return bytes;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isVisable() {
            return visable;
        }

        public int getByteSize() {
            return bytes.length;
        }

        public String toString() {
            if (bytes.length <= 0 || !visable) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(String.format("0x%02X", (byte) bytes[i]));
            }
            return builder.toString();

        }
    }

}
