/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;



/**
 * GifImageWriterSpi. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070725 nsano initial version <br>
 */
public class GifImageWriterSpi extends ImageWriterSpi {

    private static final String VENDOR_NAME = "vavisoft.com";
    private static final String VERSION = "0.1";
    private static final String[] NAMES = new String[] { "gif" };
    private static final String[] SUFFIXES = new String[] { "gif" };
    private static final String[] MIME_TYPES = new String[] { "image/gif" };
    private static final String WRITER_CLASSNAME = GifImageWriterSpi.class.getName();
    private static final Class<?>[] OUTPUT_TYPES = STANDARD_OUTPUT_TYPE;
    private static final String[] READER_SPI_NAMES = new String[] { NonLzwGifImageReader.class.getName() };
    private static final boolean SUPPORTS_STANDARD_STREAM_METADATA_FORMAT = false;
    private static final String NATIVE_STREAM_METADATA_FORMAT_NAME = null;
    private static final String NATIVE_STREAM_METADATA_FORMAT_CLASSNAME = null;
    private static final String[] EXTRA_STREAM_METADATA_FORMAT_NAMES = null;
    private static final String[]  EXTRA_STREAM_METADATA_CLASS_NAMES = null;
    private static final boolean SUPPORTS_STANDARD_IMAGE_METADATA_FORMAT = false;
    private static final String NATIVE_IMAGE_METADATA_FORMAT_NAME = null;
    private static final String NATIVE_IMAGE_METADATA_FORMAT_CLASS_NAME = null;
    private static final String[] EXTRA_IMAGE_METADATA_FORMAT_NAMES = null;
    private static final String[] EXTRA_IMAGE_METADATA_FORMAT_CLASS_NAMES =null;

    /** */
    public GifImageWriterSpi() {
        super(VENDOR_NAME, VERSION,
              NAMES, SUFFIXES,
              MIME_TYPES,
              WRITER_CLASSNAME,
              OUTPUT_TYPES,
              READER_SPI_NAMES,
              SUPPORTS_STANDARD_STREAM_METADATA_FORMAT,
              NATIVE_STREAM_METADATA_FORMAT_NAME,
              NATIVE_STREAM_METADATA_FORMAT_CLASSNAME,
              EXTRA_STREAM_METADATA_FORMAT_NAMES,
              EXTRA_STREAM_METADATA_CLASS_NAMES,
              SUPPORTS_STANDARD_IMAGE_METADATA_FORMAT,
              NATIVE_IMAGE_METADATA_FORMAT_NAME,
              NATIVE_IMAGE_METADATA_FORMAT_CLASS_NAME,
              EXTRA_IMAGE_METADATA_FORMAT_NAMES,
              EXTRA_IMAGE_METADATA_FORMAT_CLASS_NAMES);
    }

    /* */
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        return true;
    }

    /* */
    public ImageWriter createWriterInstance(Object extension) throws IOException {
        return new GifImageWriter(this);
    }

    /* */
    public String getDescription(Locale locale) {
        return "Vavisoft Animated GIF Image Writer";
    }
}

/* */
