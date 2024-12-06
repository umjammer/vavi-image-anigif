/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.svg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import static java.lang.System.getLogger;


/**
 * SvgImageReaderSpi.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070723 nsano initial version <br>
 */
public class SvgImageReaderSpi extends ImageReaderSpi {

    private static final Logger logger = getLogger(SvgImageReaderSpi.class.getName());

    static {
        try {
            try (InputStream is = SvgImageReaderSpi.class.getResourceAsStream("/META-INF/maven/vavi/vavi-image-anigif/pom.properties")) {
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    Version = props.getProperty("version", "undefined in pom.properties");
                } else {
                    Version = System.getProperty("vavi.test.version", "undefined");
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String VendorName = "vavi";
    private static final String Version;
    private static final String ReaderClassName =
        "vavi.imageio.svg.SvgImageReader";
    private static final String[] Names = {
        "svg", "SVG"
    };
    private static final String[] Suffixes = {
        "svg", "SVG"
    };
    private static final String[] mimeTypes = {
        "image/svg+xml"
    };
    static final String[] WriterSpiNames = {
        /*"vavi.imageio.svg.SvgImageWriterSpi"*/
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "svg";
    private static final String NativeImageMetadataFormatClassName =
        /*"vavi.imageio.svg.SvgMetaData"*/ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    /** */
    public SvgImageReaderSpi() {
        super(VendorName,
              Version,
              Names,
              Suffixes,
              mimeTypes,
              ReaderClassName,
              new Class[] { File.class, ImageInputStream.class, InputStream.class },
              WriterSpiNames,
              SupportsStandardStreamMetadataFormat,
              NativeStreamMetadataFormatName,
              NativeStreamMetadataFormatClassName,
              ExtraStreamMetadataFormatNames,
              ExtraStreamMetadataFormatClassNames,
              SupportsStandardImageMetadataFormat,
              NativeImageMetadataFormatName,
              NativeImageMetadataFormatClassName,
              ExtraImageMetadataFormatNames,
              ExtraImageMetadataFormatClassNames);
    }

    @Override
    public String getDescription(Locale locale) {
        return "SVG as Image Reader";
    }

    @Override
    public boolean canDecodeInput(Object obj) throws IOException {

        if (obj instanceof ImageInputStream is) {
            final int size = 150;
            byte[] bytes = new byte[size];
            try {
                is.mark();
                is.read(bytes);
                is.reset();
            } catch (IOException e) {
logger.log(Level.INFO, e.getMessage(), e);
                return false;
            }
            return new String(bytes, StandardCharsets.UTF_8).indexOf("svg") > 0;
        } else {
logger.log(Level.TRACE, obj);
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new SvgImageReader(this);
    }
}
