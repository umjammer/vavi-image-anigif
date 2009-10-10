/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.svg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.util.Debug;


/**
 * BatikSvgImageReaderSpi.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070723 nsano initial version <br>
 */
public class BatikSvgImageReaderSpi extends ImageReaderSpi {

    private static final String vendorName = "http://www.vavisoft.com";
    private static final String version = "0.00";
    private static final String readerClassName =
        "vavi.imageio.svg.BatikSvgImageReader";
    private static final String names[] = {
        "svg", "SVG"
    };
    private static final String suffixes[] = {
        "svg", "SVG"
    };
    private static final String mimeTypes[] = {
        "image/x-svg"
    };
    static final String writerSpiNames[] = {
        /*"vavi.imageio.svg.BatikSvgImageWriterSpi"*/
    };
    private static final boolean supportsStandardStreamMetadataFormat = false;
    private static final String nativeStreamMetadataFormatName = null;
    private static final String nativeStreamMetadataFormatClassName = null;
    private static final String extraStreamMetadataFormatNames[] = null;
    private static final String extraStreamMetadataFormatClassNames[] = null;
    private static final boolean supportsStandardImageMetadataFormat = false;
    private static final String nativeImageMetadataFormatName = "svg";
    private static final String nativeImageMetadataFormatClassName =
        /*"vavi.imageio.svg.BatikSvgMetaData"*/ null;
    private static final String extraImageMetadataFormatNames[] = null;
    private static final String extraImageMetadataFormatClassNames[] = null;

    /** */
    public BatikSvgImageReaderSpi() {
        super(vendorName,
              version,
              names,
              suffixes,
              mimeTypes,
              readerClassName,
              new Class[] { File.class, ImageInputStream.class, InputStream.class },
              writerSpiNames,
              supportsStandardStreamMetadataFormat,
              nativeStreamMetadataFormatName,
              nativeStreamMetadataFormatClassName,
              extraStreamMetadataFormatNames,
              extraStreamMetadataFormatClassNames,
              supportsStandardImageMetadataFormat,
              nativeImageMetadataFormatName,
              nativeImageMetadataFormatClassName,
              extraImageMetadataFormatNames,
              extraImageMetadataFormatClassNames);
    }

    /* */
    public String getDescription(Locale locale) {
        return "SVG as Image Reader using Apache Batik";
    }
    
    /* */
    public boolean canDecodeInput(Object obj) throws IOException {

        if (obj instanceof ImageInputStream) {
            ImageInputStream is = ImageInputStream.class.cast(obj);
            final int size = 150;
            byte bytes[] = new byte[size];
            try {
                is.mark();
                is.read(bytes);
                is.reset();
            } catch (IOException e) {
Debug.printStackTrace(e);
                return false;
            }
            return new String(bytes, "UTF-8").indexOf("svg") > 0;
        } else {
System.err.println(obj);
            return false;
        }
    }
    
    /* */
    public ImageReader createReaderInstance(Object obj) {
        return new BatikSvgImageReader(this);
    }
}

/* */
