/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.svg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.logging.Level;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.util.Debug;


/**
 * SvgImageReaderSpi.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070723 nsano initial version <br>
 */
public class SvgImageReaderSpi extends ImageReaderSpi {

    private static final String VendorName = "http://www.vavisoft.com";
    private static final String Version = "1.0.3";
    private static final String ReaderClassName =
        "vavi.imageio.svg.SvgImageReader";
    private static final String[] Names = {
        "svg", "SVG"
    };
    private static final String[] Suffixes = {
        "svg", "SVG"
    };
    private static final String[] mimeTypes = {
        "image/x-svg"
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

        if (obj instanceof ImageInputStream) {
            ImageInputStream is = (ImageInputStream) obj;
            final int size = 150;
            byte[] bytes = new byte[size];
            try {
                is.mark();
                is.read(bytes);
                is.reset();
            } catch (IOException e) {
Debug.printStackTrace(e);
                return false;
            }
            return new String(bytes, StandardCharsets.UTF_8).indexOf("svg") > 0;
        } else {
Debug.println(Level.FINER, obj);
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new SvgImageReader(this);
    }
}

/* */
