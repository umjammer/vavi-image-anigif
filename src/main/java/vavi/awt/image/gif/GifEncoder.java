/*
 * GifEncoder - write out an image as a GIF
 *
 * Transparency handling and variable bit size courtesy of Jack Palevich.
 *
 * Copyright (C)1996,1998 by Jef Poskanzer <jef@mail.acme.com>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * Visit the ACME Labs Java page for up-to-date versions of this and other
 * fine Java utilities: http://www.acme.com/java/
 */

package vavi.awt.image.gif;

import java.awt.Image;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import vavi.io.LittleEndianDataOutputStream;


/**
 * Write out an image as a GIF.
 * <P>
 * <A HREF="/resources/classes/Acme/JPM/Encoders/GifEncoder.java">Fetch the
 * software.</A><BR>
 * <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>
 * <P>
 *
 * @author Jef Poskanzer
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class GifEncoder extends ImageEncoder {

    /** DisposalMethodの定数を持つインターフェースです。 */
    public enum DisposalMethod {
        /** 特に指定なし。特別な処理は何もしません。 */
        Unspecified,
        /** 画像を残す。画像を表示したままにします。 */
        DoNotDispose,
        /** 背景色を回復。画像を表示していた領域を背景色で塗りつぶします。 */
        RestoreToBackground,
        /** 以前のものを回復。画像が表示される前にその領域に表示されていたものを回復します。 */
        RestoreToPrevious
    }

    /** */
    private boolean interlace = false;

    /** */
    private int delayTime = 0;

    /** */
    private DisposalMethod disposalMethod = DisposalMethod.Unspecified;

    /** */
    private int drawX = 0;

    /** */
    private int drawY = 0;

    /**
     * Constructor from Image.
     *
     * @param image The image to encode.
     * @param os The stream to write the GIF to.
     */
    public GifEncoder(Image image, OutputStream os) throws IOException {
        this(image, os, false);
    }

    /**
     * Constructor from Image with interlace setting.
     *
     * @param image The image to encode.
     * @param os The stream to write the GIF to.
     * @param interlace Whether to interlace.
     */
    public GifEncoder(Image image, OutputStream os, boolean interlace) throws IOException {
        super(image, os);
        this.interlace = interlace;
    }

    /**
     * Constructor from ImageProducer.
     *
     * @param producer The ImageProducer to encode.
     * @param os The stream to write the GIF to.
     */
    public GifEncoder(ImageProducer producer, OutputStream os) throws IOException {
        super(producer, os);
    }

    /**
     * Constructor from ImageProducer with interlace setting.
     *
     * @param producer The ImageProducer to encode.
     * @param os The stream to write the GIF to.
     */
    public GifEncoder(ImageProducer producer, OutputStream os, boolean interlace) throws IOException {
        super(producer, os);
        this.interlace = interlace;
    }

    /** */
    private int[][] rgbPixels;

    /* */
    protected void encodeStart(int width, int height) throws IOException {
        this.width = width;
        this.height = height;
        rgbPixels = new int[height][width];
    }

    /* */
    protected void encodePixels(int x, int y, int w, int h, int[] rgbPixels, int offset, int scanSize) throws IOException {
        // Save the pixels.
        for (int row = 0; row < h; row++) {
            System.arraycopy(rgbPixels, row * scanSize + offset, this.rgbPixels[y + row], x, w);
        }
    }

    /** */
    private Map<Integer, PixelInfo> colorHash;

    /* */
    protected void encodeDone() throws IOException {
        int transparentIndex = -1;
        int transparentRgb = -1;
        // Put all the pixels into a hash table.
        colorHash = new HashMap<>();
        int index = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int rgb = rgbPixels[row][col];
                boolean isTransparent = (rgb >>> 24) < 0x80;
                if (isTransparent) {
                    if (transparentIndex < 0) {
                        // First transparent color; remember it.
                        transparentIndex = index;
                        transparentRgb = rgb;
                    } else if (rgb != transparentRgb) {
                        // A second transparent color; replace it with
                        // the first one.
                        rgbPixels[row][col] = rgb = transparentRgb;
                    }
                }
                PixelInfo item = colorHash.get(rgb);
                if (item == null) {
                    if (index >= 256) {
                        throw new IllegalStateException("too many colors for a GIF");
                    }
                    item = new PixelInfo(rgb, 1, index, isTransparent);
                    index++;
                    colorHash.put(rgb, item);
                } else {
                    item.count++;
                }
            }
        }

        // Figure out how many bits to use.
        int logColors;
        if (index <= 2) {
            logColors = 1;
        } else if (index <= 4) {
            logColors = 2;
        } else if (index <= 16) {
            logColors = 4;
        } else {
            logColors = 8;
        }

        // Turn colors into colormap entries.
        int mapSize = 1 << logColors;
        byte[] reds = new byte[mapSize];
        byte[] greens = new byte[mapSize];
        byte[] blues = new byte[mapSize];
        for (PixelInfo item : colorHash.values()) {
            reds[item.index] = (byte) ((item.rgb >> 16) & 0xff);
            greens[item.index] = (byte) ((item.rgb >> 8) & 0xff);
            blues[item.index] = (byte) (item.rgb & 0xff);
        }

        encodeGif((byte) 0, transparentIndex, logColors, reds, greens, blues);
    }

    /** */
    private byte getPixel(int x, int y) {
        PixelInfo item = colorHash.get(rgbPixels[y][x]);
        if (item == null) {
            throw new IllegalStateException("color not found");
        }
        return (byte) item.index;
    }

    private int curX, curY;

    private int countDown;

    private int pass = 0;

    /**
     * Adapted from ppmtogif, which is based on GIFENCOD by David
     * Rowley <mgardi@watdscu.waterloo.edu>. Lempel-Zim compression
     * based on "compress".
     */
    private void encodeGif(byte background, int transparent, int bitsPerPixel, byte[] red, byte[] green, byte[] blue) throws IOException {
        int initCodeSize;

        int colorMapSize = 1 << bitsPerPixel;

        // Calculate number of bits we are expecting
        countDown = width * height;

        // Indicate which pass we are on (if interlace)
        pass = 0;

        // The initial code size
        if (bitsPerPixel <= 1) {
            initCodeSize = 2;
        } else {
            initCodeSize = bitsPerPixel;
        }

        // Set up the current x and y position
        curX = 0;
        curY = 0;

        LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(out);

        // Write out extension for transparent colour index, if necessary.
        if (transparent != -1) {
            dos.writeByte('!');
            dos.writeByte(0xf9);
            dos.writeByte(4);
            dos.writeByte(((0x7 & disposalMethod.ordinal()) << 2) | 0x1);
            dos.writeShort(getDelayTime());
            dos.writeByte(transparent);
            dos.writeByte(0);
        } else {
            dos.writeByte('!');
            dos.writeByte(0xf9);
            dos.writeByte(4);
            dos.writeByte(((0x7 & disposalMethod.ordinal()) << 2));
            dos.writeShort(getDelayTime());
            dos.writeByte(0);
            dos.writeByte(0);
        }

        // Write an Image separator
        dos.writeByte(',');

        // Write the Image header
        dos.writeShort(this.drawX);
        dos.writeShort(this.drawY);
        dos.writeShort(width);
        dos.writeShort(height);

        // 1bitローカルカラーテーブルを使用
        // 6-8bitローカルカラーテーブルの大きさを指定

        // Write out whether or not the image is interlaced
        if (interlace) {
            dos.writeByte(0xc0 | (0x7 & (bitsPerPixel - 1)));
        } else {
            dos.writeByte(0x80 | (0x7 & (bitsPerPixel - 1)));
        }

        // Write out the Global Colour Map
        for (int i = 0; i < colorMapSize; ++i) {
            dos.writeByte(red[i]);
            dos.writeByte(green[i]);
            dos.writeByte(blue[i]);
        }

        // Write out the initial code size
        dos.writeByte((byte) initCodeSize);

        // Go and actually compress the data
        compress(initCodeSize + 1);

        // Write out a Zero-length packet (to end the series)
        dos.writeByte(0);
    }

    // Bump the 'curx' and 'cury' to point to the next pixel
    private void bumpPixel() {
        // Bump the crrent X position
        ++curX;

        // If we are at the end of a scan line, set curx back to the beginning
        // If we are interlaced, bump the cury to the appropriate spot,
        // otherwise, just increment it.
        if (curX == width) {
            curX = 0;

            if (!interlace) {
                ++curY;
            } else {
                switch (pass) {
                case 0:
                    curY += 8;
                    if (curY >= height) {
                        ++pass;
                        curY = 4;
                    }
                    break;

                case 1:
                    curY += 8;
                    if (curY >= height) {
                        ++pass;
                        curY = 2;
                    }
                    break;

                case 2:
                    curY += 4;
                    if (curY >= height) {
                        ++pass;
                        curY = 1;
                    }
                    break;

                case 3:
                    curY += 2;
                    break;
                }
            }
        }
    }

    private static final int EOF = -1;

    /** return the next pixel from the image */
    private int nextPixel() {

        if (countDown == 0) {
            return EOF;
        }

        --countDown;

        byte r = getPixel(curX, curY);

        bumpPixel();

        return r & 0xff;
    }

    // GIFCOMPR.C - GIF Image compression routines
    //
    // Lempel-Ziv compression based on 'compress'. GIF modifications by
    // David Rowley (mgardi@watdcsu.waterloo.edu)

    // General DEFINEs

    private static final int BITS = 12;

    /** 80% occupancy */
    private static final int HSIZE = 5003;

    // GIF Image compression - modified 'compress'
    //
    // Based on: compress.c - File compression ala IEEE Computer, June 1984.
    //
    // By Authors: Spencer W. Thomas (decvax!harpo!utah-cs!utah-gr!thomas)
    // Jim McKie (decvax!mcvax!jim)
    // Steve Davies (decvax!vax135!petsd!peora!srd)
    // Ken Turkowski (decvax!decwrl!turtlevax!ken)
    // James A. Woods (decvax!ihnp4!ames!jaw)
    // Joe Orost (decvax!vax135!petsd!joe)

    /** number of bits/code */
    private int nBits;

    /** user settable max # bits/code */
    private int maxBits = BITS;

    /** maximum code, given n_bits */
    private int maxCode;

    /** should NEVER generate this code */
    private int maxMaxCode = 1 << BITS;

    private int maxCode(int nBits) {
        return (1 << nBits) - 1;
    }

    private int[] hTab = new int[HSIZE];

    private int[] codeTab = new int[HSIZE];

    /** for dynamic table sizing */
    private int hSize = HSIZE;

    /** first unused entry */
    private int freeEnt = 0;

    // block compression parameters -- after all codes are used up,
    // and compression rate changes, start over.
    private boolean clearFlag = false;

    private int initBits;

    private int clearCode;

    private int eofCode;

    /**
     * Algorithm: use open addressing double hashing (no chaining) on the
     * prefix code / next character combination. We do a variant of Knuth's
     * algorithm D (vol. 3, sec. 6.4) along with G. Knott's relatively-prime
     * secondary probe. Here, the modular division first probe is gives way
     * to a faster exclusive-or manipulation. Also do block compression with
     * an adaptive reset, whereby the code table is cleared when the compression
     * ratio decreases, but after the table fills. The variable-length output
     * codes are re-sized at this point, and a special CLEAR code is generated
     * for the decompressor. Late addition: construct the table according to
     * file size for noticeable speed improvement on small files. Please direct
     * questions about this implementation to ames!jaw.
     */
    private void compress(int initBits) throws IOException {

        // Set up the globals: g_init_bits - initial number of bits
        this.initBits = initBits;

        // Set up the necessary values
        clearFlag = false;
        nBits = initBits;
        maxCode = maxCode(nBits);

        clearCode = 1 << (initBits - 1);
        eofCode = clearCode + 1;
        freeEnt = clearCode + 2;

        initChar();

        int ent = nextPixel();

        int hShift = 0;
        int fCode;
        for (fCode = hSize; fCode < 65536; fCode *= 2) {
            ++hShift;
        }
        hShift = 8 - hShift; // set hash code range bound

        int hSizeReg = hSize;
        clearHash(hSizeReg); // clear hash table

        output(clearCode);

        int c;
outer_loop:
        while ((c = nextPixel()) != EOF) {
            fCode = (c << maxBits) + ent;
            int i = (c << hShift) ^ ent; // xor hashing

            if (hTab[i] == fCode) {
                ent = codeTab[i];
                continue;
            } else if (hTab[i] >= 0) { // non-empty slot
                int disp = hSizeReg - i; // secondary hash (after G. Knott)
                if (i == 0) {
                    disp = 1;
                }
                do {
                    if ((i -= disp) < 0) {
                        i += hSizeReg;
                    }

                    if (hTab[i] == fCode) {
                        ent = codeTab[i];
                        continue outer_loop;
                    }
                } while (hTab[i] >= 0);
            }
            output(ent);
            ent = c;
            if (freeEnt < maxMaxCode) {
                codeTab[i] = freeEnt++; // code -> hashtable
                hTab[i] = fCode;
            } else {
                clearBlock();
            }
        }
        // Put out the final code.
        output(ent);
        output(eofCode);
    }

    private int curAccum = 0;

    private int curBits = 0;

    private static final int[] masks = {
        0x0000, 0x0001, 0x0003, 0x0007,
        0x000f, 0x001f, 0x003f, 0x007f,
        0x00ff, 0x01ff, 0x03ff, 0x07ff,
        0x0fff, 0x1fff, 0x3fff, 0x7fff,
        0xffff
    };

    /**
     * Output the given code.
     * <p>
     * <li>Assumptions:
     * Chars are 8 bits long.
     * <li>Algorithm:
     * Maintain a BITS character long buffer (so that 8 codes will
     * fit in it exactly). Use the VAX insv instruction to insert each
     * code in turn. When the buffer fills up empty it and start over.
     * <li>Outputs:
     * code to the file.
     * @param code A n_bits-bit integer. If == -1, then EOF. This assumes
     *             that n_bits =< wordsize - 1.
     */
    private void output(int code) throws IOException {
        curAccum &= masks[curBits];

        if (curBits > 0) {
            curAccum |= (code << curBits);
        } else {
            curAccum = code;
        }

        curBits += nBits;

        while (curBits >= 8) {
            outChar((byte) (curAccum & 0xff));
            curAccum >>= 8;
            curBits -= 8;
        }

        // If the next entry is going to be too big for the code size,
        // then increase it, if possible.
        if (freeEnt > maxCode || clearFlag) {
            if (clearFlag) {
                maxCode = maxCode(nBits = initBits);
                clearFlag = false;
            } else {
                ++nBits;
                if (nBits == maxBits) {
                    maxCode = maxMaxCode;
                } else {
                    maxCode = maxCode(nBits);
                }
            }
        }

        if (code == eofCode) {
            // At EOF, write the rest of the buffer.
            while (curBits > 0) {
                outChar((byte) (curAccum & 0xff));
                curAccum >>= 8;
                curBits -= 8;
            }

            flushChar();
        }
    }

    // Clear out the hash table

    /** table clear for block compress */
    private void clearBlock() throws IOException {
        clearHash(hSize);
        freeEnt = clearCode + 2;
        clearFlag = true;

        output(clearCode);
    }

    /** reset code table */
    private void clearHash(int hSize) {
        for (int i = 0; i < hSize; i++) {
            hTab[i] = -1;
        }
    }

    // GIF Specific routines

    /** Number of characters so far in this 'packet' */
    private int aCount;

    /** Set up the 'byte output' routine */
    private void initChar() {
        aCount = 0;
    }

    /** Define the storage for the packet accumulator */
    private byte[] accum = new byte[256];

    /**
     * Add a character to the end of the current packet, and if it is 254
     * characters, flush the packet to disk.
     */
    private void outChar(byte c) throws IOException {
        accum[aCount++] = c;
        if (aCount >= 254) {
            flushChar();
        }
    }

    /**
     * Flush the packet to disk, and reset the accumulator
     */
    private void flushChar() throws IOException {
        if (aCount > 0) {
            out.write(aCount);
            out.write(accum, 0, aCount);
            aCount = 0;
        }
    }

    /** */
    public int getDelayTime() {
        return delayTime;
    }

    /** */
    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    /** */
    public DisposalMethod getDisposalMethod() {
        return disposalMethod;
    }

    /**
     * @param disposalMethod The disposalMethod to set.
     */
    public void setDisposalMethod(DisposalMethod disposalMethod) {
        this.disposalMethod = disposalMethod;
    }

    /**
     * @return Returns the drawX.
     */
    public int getDrawX() {
        return drawX;
    }

    /**
     * @param drawX The drawX to set.
     */
    public void setDrawX(int drawX) {
        this.drawX = drawX;
    }

    /**
     * @return Returns the drawY.
     */
    public int getDrawY() {
        return drawY;
    }

    /**
     * @param drawY The drawY to set.
     */
    public void setDrawY(int drawY) {
        this.drawY = drawY;
    }

    /** */
    private static class PixelInfo {
        /** */
        int rgb;
        /** */
        int count;
        /** */
        int index;
        /** */
        boolean isTransparent;
        /** */
        PixelInfo(int rgb, int count, int index, boolean isTransparent) {
            this.rgb = rgb;
            this.count = count;
            this.index = index;
            this.isTransparent = isTransparent;
        }
    }
}

/* */
