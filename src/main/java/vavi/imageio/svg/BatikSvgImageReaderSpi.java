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

    private static final String VendorName = "http://www.vavisoft.com";
    private static final String Version = "0.00";
    private static final String ReaderClassName =
        "vavi.imageio.svg.BatikSvgImageReader";
    private static final String Names[] = {
        "svg", "SVG"
    };
    private static final String Suffixes[] = {
        "svg", "SVG"
    };
    private static final String mimeTypes[] = {
        "image/x-svg"
    };
    static final String WriterSpiNames[] = {
        /*"vavi.imageio.svg.BatikSvgImageWriterSpi"*/
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String ExtraStreamMetadataFormatNames[] = null;
    private static final String ExtraStreamMetadataFormatClassNames[] = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "svg";
    private static final String NativeImageMetadataFormatClassName =
        /*"vavi.imageio.svg.BatikSvgMetaData"*/ null;
    private static final String ExtraImageMetadataFormatNames[] = null;
    private static final String ExtraImageMetadataFormatClassNames[] = null;

    /** */
    public BatikSvgImageReaderSpi() {
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

    /* */
    public String getDescription(Locale locale) {
        return "SVG as Image Reader using Apache Batik";
    }

    /* */
    public boolean canDecodeInput(Object obj) throws IOException {

        if (obj instanceof ImageInputStream) {
            ImageInputStream is = ImageInputStream.class.cast(obj);
            final int size = 160;
            byte bytes[] = new byte[size];
            try {
                is.mark();
                is.read(bytes);
                is.reset();
            } catch (IOException e) {
Debug.printStackTrace(e);
                return false;
            }
            String string = new String(bytes, "UTF-8");
System.err.println(string);
            return string.indexOf("svg") > 0;
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
