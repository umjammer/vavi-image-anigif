/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import vavi.imageio.susie.SusieImageReaderSpi;


/**
 * GifImageWriterSpi.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070725 nsano initial version <br>
 */
public class GifImageWriterSpi extends ImageWriterSpi {

    static {
        try {
            try (InputStream is = SusieImageReaderSpi.class.getResourceAsStream("/META-INF/maven/vavi/vavi-image-anigif/pom.properties")) {
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    VERSION = props.getProperty("version", "undefined in pom.properties");
                } else {
                    VERSION = System.getProperty("vavi.test.version", "undefined");
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String VENDOR_NAME = "vavi";
    private static final String VERSION;
    private static final String[] NAMES = new String[] { "gif", "GIF" };
    private static final String[] SUFFIXES = new String[] { "gif", "GIF" };
    private static final String[] MIME_TYPES = new String[] { "image/gif" };
    private static final String WRITER_CLASSNAME = GifImageWriterSpi.class.getName();
    private static final Class<?>[] OUTPUT_TYPES = { ImageOutputStream.class };
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

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        return true;
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) throws IOException {
        return new GifImageWriter(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "vavi Animated GIF Image Writer";
    }
}
