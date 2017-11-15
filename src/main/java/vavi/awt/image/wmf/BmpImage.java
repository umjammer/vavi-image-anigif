/*
 * Copyright 1997, 1998 Carmen Delessio (carmen@blackdirt.com)
 * Black Dirt Software http://www.blackdirt.com/graphics
 * Free for non-commercial use
 */

package vavi.awt.image.wmf;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import vavi.io.LittleEndianDataInputStream;


/**
 * BmpImage.
 *
 * @author <a href="mailto:carmen@blackdirt.com">Carmen Delessio</a>
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070630 nsano initial version <br>
 * @see "http://www.blackdirt.com/graphics/"
 */
class BmpImage {

    /** size of this header in bytes */
    private int bmpSize;

    private int bmpReserved;

    private int bmpImageOffset;

    private int bmpHeaderSize;

    /** image width in pixels */
    private int bmpWidth;

    /** image height in pixels (if < 0, "top-down") */
    private int bmpHeight;

    /** no. of color planes: always 1 */
    private int bmpPlanes;

    /** number of bits per pixel: 1, 4, 8, or 24 (no color map) */
    private int bmpBitsPerPixel;

    /** compression methods used: 0 (none), 1 (8-bit RLE), or 2 (4-bit RLE) */
    private int bmpCompression;

    /** size of bitmap in bytes (may be 0: if so, calculate) */
    private int bmpSizeOfBitmap;

    /** horizontal resolution, pixels/meter (may be 0) */
    private int bmpHorzResolution;

    /** vertical resolution, pixels/meter (may be 0) */
    private int bmpVertResolution;

    /** no. of colors in palette (if 0, calculate) */
    private int bmpColorsUsed;

    /**
     * no. of important colors (appear first in palette)
     * (0 means all are important)
     */
    private int bmpColorsImportant;

    /** array of pixels */
    private int pixels[];

    /** */
    private Image bmpImage;

    /** */
    public BmpImage(InputStream is) throws IOException {
        parse(is);
        bmpImage = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(bmpWidth, bmpHeight, pixels, 0, bmpWidth));
    }

    /** */
    public BmpImage(String bmpString, int typeFlag) throws IOException {
        // add string error check, length, BM, etc

        byte[] bytePicture = bmpString.getBytes();
        ByteArrayInputStream byteInput = new ByteArrayInputStream(bytePicture);
        parse(byteInput, typeFlag);
        bmpImage = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(bmpWidth, bmpHeight, pixels, 0, bmpWidth));
    }

    /** */
    public BmpImage(String bmpString) throws IOException {

        byte[] bytePicture = bmpString.getBytes();
        ByteArrayInputStream byteInput = new ByteArrayInputStream(bytePicture);
        parse(byteInput);
        bmpImage = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(bmpWidth, bmpHeight, pixels, 0, bmpWidth));
    }

    /** */
    public Image getImage() {
        return bmpImage;
    }

    /** */
    private void parse(InputStream is) throws IOException {
        parse(is, 0);
    }

    /** */
    private void parse(InputStream is, int typeFlag) throws IOException {

        byte[] tempBuffer;

        // begin bitmap header
        @SuppressWarnings("resource")
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        if (typeFlag == 0) { // read header - bitmapfile
            tempBuffer = null;
            tempBuffer = new byte[2]; // get BM

            dis.read(tempBuffer);
System.err.println("tempBuffer " + new String(tempBuffer));
            bmpSize = dis.readInt(); // key 4 bytes
System.err.println("size " + bmpSize);

            bmpReserved = dis.readInt();
System.err.println("reservede " + bmpReserved);

            bmpImageOffset = dis.readInt();
System.err.println("offset " + bmpImageOffset);
        }

System.err.println("in bmpstream");
        bmpHeaderSize = dis.readInt();
System.err.println("BMPheadersize " + bmpHeaderSize);

        bmpWidth = dis.readInt();
System.err.println("BMPwidth " + bmpWidth);

        bmpHeight = dis.readInt();
System.err.println("BMPheight " + bmpHeight);

        bmpPlanes = dis.readShort();
System.err.println("BMPplanes " + bmpPlanes);

        bmpBitsPerPixel = dis.readShort();
System.err.println("BMPbitsPerPixel " + bmpBitsPerPixel);

        bmpCompression = dis.readInt();
System.err.println("BMPcompression " + bmpCompression);

        bmpSizeOfBitmap = dis.readInt();
System.err.println("BMPsizeOfBitmap " + bmpSizeOfBitmap);

        bmpHorzResolution = dis.readInt();
System.err.println("BMPhorzResolution " + bmpHorzResolution);

        bmpVertResolution = dis.readInt();
System.err.println("BMPvertResolution " + bmpVertResolution);

        bmpColorsUsed = dis.readInt();
System.err.println("BMPcolorsUsed " + bmpColorsUsed);

        bmpColorsImportant = dis.readInt();
System.err.println("BMPcolorsImportant " + bmpColorsImportant);

        pixels = new int[bmpWidth * (bmpHeight + 1)];

        if (bmpBitsPerPixel == 1) {
            Color[] colorTable = new Color[2];
            for (int i = 0; i < 2; i++) {
                colorTable[i] = win2Color(dis.readInt());
            }
            // width is # of pixels, twice as many bytes as pixles
            int bytesPerLine = bmpWidth / 8;
            // only used to read in scan lines
            if (bytesPerLine * 8 < bmpWidth) { // if pixel is on odd boundary
                bytesPerLine++;
            }

            while (bytesPerLine % 4 != 0) {
                bytesPerLine++; // get even boundary, DWORD boundary
            }

            // declare a buffer sufficient for 1 line
            byte[] scanline = new byte[bytesPerLine];

            // bottom up, start with last line
            for (int i = bmpHeight - 1; i >= 0; i--) {
                dis.readFully(scanline, 0, bytesPerLine); // read in a line

                for (int j = 0; j < bmpWidth; j += 8) {
                    // 1st 4 bits of byte shifted and masked
                    int colorIndex = (scanline[j / 8]) >> 7 & 0x01;
                    pixels[i * bmpWidth + j] = colorTable[colorIndex].getRGB();

                    // 1st 4 bits of byte shifted and masked
                    colorIndex = (scanline[j / 8]) >> 6 & 0x01;
                    pixels[i * bmpWidth + j + 1] = colorTable[colorIndex].getRGB();

                    // 1st 4 bits of byte shifted and masked
                    colorIndex = (scanline[j / 8]) >> 5 & 0x01;
                    pixels[i * bmpWidth + j + 2] = colorTable[colorIndex].getRGB();

                    // 1st 4 bits of byte shifted and masked
                    colorIndex = (scanline[j / 8]) >> 4 & 0x01;
                    pixels[i * bmpWidth + j + 3] = colorTable[colorIndex].getRGB();

                    // 1st 4 bits of byte shifted and masked
                    colorIndex = (scanline[j / 8]) >> 3 & 0x01;
                    pixels[i * bmpWidth + j + 4] = colorTable[colorIndex].getRGB();

                    // 1st 4 bits of byte shifted and masked
                    colorIndex = (scanline[j / 8]) >> 2 & 0x01;
                    pixels[i * bmpWidth + j + 5] = colorTable[colorIndex].getRGB();

                    // 1st 4 bits of byte shifted and masked
                    colorIndex = (scanline[j / 8]) >> 1 & 0x01;
                    pixels[i * bmpWidth + j + 6] = colorTable[colorIndex].getRGB();

                    // 1st 4 bits of byte shifted and masked
                    colorIndex = (scanline[j / 8]) & 0x01;
                    pixels[i * bmpWidth + j + 7] = colorTable[colorIndex].getRGB();

                }
            }
        } // if bpp = 1

        if (bmpBitsPerPixel == 4) {
            Color[] colorTable = new Color[16];
            for (int i = 0; i < 16; i++) {
                colorTable[i] = win2Color(dis.readInt());
            }
            // width is # of pixels, twice as many bytes as pixles
            int bytesPerLine = bmpWidth / 2;
            // only used to read in scan lines
            if (bytesPerLine * 2 < bmpWidth) { // if pixel is on odd boundary
                bytesPerLine++;
            }

            while (bytesPerLine % 4 != 0) {
                bytesPerLine++; // get even boundary, DWORD boundary
            }

            // declare a buffer sufficient for 1 line
            byte[] scanline = new byte[bytesPerLine];
            // bottom up, start with last line
            for (int i = bmpHeight - 1; i >= 0; i--) {
                dis.readFully(scanline, 0, bytesPerLine); // read in a line

                for (int j = 0; j < bmpWidth; j += 2) {
                    // 1st 4 bits of byte shifted and masked
                    int colorIndex = (scanline[j / 2] >> 4) & 0x0F;
                    pixels[i * bmpWidth + j] = colorTable[colorIndex].getRGB();
                    colorIndex = (scanline[j / 2]) & 0x0F; // 2nd 4 bits masked
                    pixels[i * bmpWidth + j + 1] = colorTable[colorIndex].getRGB();
                }
            }
        } // if bpp = 4

        if (bmpBitsPerPixel == 8) {
            Color[] colorTable = new Color[256];
            for (int i = 0; i < 256; i++) {
                colorTable[i] = win2Color(dis.readInt());
            }
            // width is # of pixels, 1 pixels for each byte
            int bytesPerLine = bmpWidth;
            while (bytesPerLine % 4 != 0) {
                bytesPerLine++; // get even boundary
            }
            // declare a buffer sufficient for 1 line
            byte[] scanline = new byte[bytesPerLine];

System.err.println("bytesPerLine " + bytesPerLine);

            // bottom up, start with last line
            for (int i = bmpHeight - 1; i >= 0; i--) {
                dis.readFully(scanline); // read in a line
                for (int j = 0; j < bmpWidth; j++) {
                    int colorIndex = scanline[j];
                    if (colorIndex < 0)
                        colorIndex += 256;
                    pixels[i * bmpWidth + j] = colorTable[colorIndex].getRGB();
                }
            }
        } // if bpp = 8

        if (bmpBitsPerPixel == 24) {
System.err.println("in bmpstream bpp =24 ");

            int winBlue;
            int winGreen;
            int winRed;
            // width is # of pixels, 3 bytes for each pixel
            int bytesPerLine = 3 * bmpWidth;
            while (bytesPerLine % 4 != 0)
                bytesPerLine++; // get even boundary
            // declare a buffer sufficient for 1 line
            byte[] scanline = new byte[bytesPerLine + 4];

            // bottom up, start with last line
            for (int i = bmpHeight - 1; i >= 0; i--) {
                dis.readFully(scanline, 0, bytesPerLine); // read in a line
                // work with 3 bytes a time
                for (int j = 0; j < bytesPerLine; j += 3) {
                    // j
                    // j+1
                    // j+2
                    winBlue = (scanline[j]) & 0xff;
                    winGreen = ((scanline[j + 1]) & 0xff) << 8;
                    winRed = ((scanline[j + 2]) & 0xff) << 16;
                    pixels[i * bmpWidth + j / 3] = 0xff000000;
                    pixels[i * bmpWidth + j / 3] |= winRed;
                    pixels[i * bmpWidth + j / 3] |= winGreen;
                    pixels[i * bmpWidth + j / 3] |= winBlue;
                }
            }
        } // if bpp = 24
    }

    private Color win2Color(int colorValue) {

        // windows does it backwards
        final int rgbBlue = 16711680; // ff0000
        final int rgbGreen = 65280; // 00ff00
        final int rgbRed = 255; // 0000ff

        int javaRed = (colorValue & rgbBlue) / 65536;
        int javaGreen = (colorValue & rgbGreen) / 256;
        int javaBlue = (colorValue & rgbRed);

        return new Color(javaRed, javaGreen, javaBlue);
    }
}

/* */
