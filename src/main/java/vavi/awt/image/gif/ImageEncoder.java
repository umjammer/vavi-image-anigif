/*
 * ImageEncoder - abstract class for writing out an image
 *
 * Copyright (C) 1996 by Jef Poskanzer <jef@mail.acme.com>.  All rights reserved.
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
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;


/**
 * Abstract class for writing out an image.
 * <P>
 * A framework for classes that encode and write out an image in a particular
 * file format.
 * <P>
 * This provides a simplified rendition of the ImageConsumer interface. It
 * always delivers the pixels as ints in the RGBdefault color model. It always
 * provides them in top-down left-right order. If you want more flexibility you
 * can always implement ImageConsumer directly.
 * <P>
 * <A HREF="/resources/classes/Acme/JPM/Encoders/ImageEncoder.java">Fetch the
 * software.</A><BR>
 * <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>
 * <P>
 * 
 * @author Jef Poskanzer
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public abstract class ImageEncoder implements ImageConsumer {

    /** */
    protected OutputStream out;

    /** */
    private ImageProducer producer;

    /** */
    protected int width = -1;

    /** */
    protected int height = -1;

    /** */
    protected int hintFlags = 0;

    /** */
    private boolean started = false;

    /** */
    private boolean encoding;

    /** */
    private Exception exception;

    /** */
    private static final ColorModel rgbModel = ColorModel.getRGBdefault();

    /** */
    @SuppressWarnings("unused")
    private Hashtable<?, ?> props = null;

    /**
     * @param image The image to encode.
     * @param os The stream to write the bytes to.
     */
    public ImageEncoder(Image image, OutputStream os) throws IOException {
        this(image.getSource(), os);
    }

    /**
     * Constructor.
     * 
     * @param producer The ImageProducer to encode.
     * @param os The stream to write the bytes to.
     */
    public ImageEncoder(ImageProducer producer, OutputStream os) throws IOException {
        this.producer = producer;
        this.out = os;
    }

    // Methods that subclasses implement.

    /** Subclasses implement this to initialize an encoding. */
    protected abstract void encodeStart(int w, int h) throws IOException;

    /**
     * Subclasses implement this to actually write out some bits. They are
     * guaranteed to be delivered in top-down-left-right order. One int per
     * pixel, index is row * scansize + off + col, RGBdefault (AARRGGBB) color
     * model.
     */
    protected abstract void encodePixels(int x, int y, int w, int h, int[] rgbPixels, int off, int scansize) throws IOException;

    /** Subclasses implement this to finish an encoding. */
    protected abstract void encodeDone() throws IOException;

    // Our own methods.

    /** Call this after initialization to get things going. */
    protected synchronized void encode() throws IOException {
        encoding = true;
        exception = null;
        producer.startProduction(this);
        while (encoding) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        if (exception != null) {
            if (RuntimeException.class.isInstance(exception)) {
                throw RuntimeException.class.cast(exception);
            } else {
                throw IOException.class.cast(new IOException().initCause(exception));
            }
        }
    }

    /** */
    private boolean accumulate = false;

    /** */
    private int[] accumulator;

    /** */
    private void encodePixelsWrapper(int x, int y, int w, int h, int[] rgbPixels, int off, int scansize) throws IOException {
        if (!started) {
            started = true;
            encodeStart(width, height);
            if ((hintFlags & TOPDOWNLEFTRIGHT) == 0) {
                accumulate = true;
                accumulator = new int[width * height];
            }
        }
        if (accumulate) {
            for (int row = 0; row < h; ++row) {
                System.arraycopy(rgbPixels, row * scansize + off, accumulator, (y + row) * width + x, w);
            }
        } else {
            encodePixels(x, y, w, h, rgbPixels, off, scansize);
        }
    }

    /** */
    private void encodeFinish() throws IOException {
        if (accumulate) {
            encodePixels(0, 0, width, height, accumulator, 0, width);
            accumulator = null;
            accumulate = false;
        }
    }

    /** */
    private synchronized void stop() {
        encoding = false;
        notifyAll();
    }

    // Methods from ImageConsumer.

    /* */
    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /* */
    public void setProperties(Hashtable<?, ?> props) {
        this.props = props;
    }

    /* */
    public void setColorModel(ColorModel model) {
        // Ignore.
    }

    /* */
    public void setHints(int hintFlags) {
        this.hintFlags = hintFlags;
    }

    /* */
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int offset, int scanSize) {
        int[] rgbPixels = new int[w];
        for (int row = 0; row < h; ++row) {
            int rowOff = offset + row * scanSize;
            for (int col = 0; col < w; ++col)
                rgbPixels[col] = model.getRGB(pixels[rowOff + col] & 0xff);
            try {
                encodePixelsWrapper(x, y + row, w, 1, rgbPixels, 0, w);
            } catch (IOException e) {
                exception = e;
                stop();
                return;
            }
        }
    }

    /* */
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int offset, int scanSize) {
        if (model == rgbModel) {
            try {
                encodePixelsWrapper(x, y, w, h, pixels, offset, scanSize);
            } catch (IOException e) {
                exception = e;
                stop();
                return;
            }
        } else {
            int[] rgbPixels = new int[w];
            for (int row = 0; row < h; ++row) {
                int rowOff = offset + row * scanSize;
                for (int col = 0; col < w; ++col)
                    rgbPixels[col] = model.getRGB(pixels[rowOff + col]);
                try {
                    encodePixelsWrapper(x, y + row, w, 1, rgbPixels, 0, w);
                } catch (IOException e) {
                    exception = e;
                    stop();
                    return;
                }
            }
        }
    }

    /* */
    public void imageComplete(int status) {
        producer.removeConsumer(this);
        if (status == ImageConsumer.IMAGEABORTED) {
            exception = new IllegalStateException("image aborted");
        } else {
            try {
                encodeFinish();
                encodeDone();
            } catch (IOException e) {
                exception = e;
            }
        }
        stop();
    }
}

/* */
