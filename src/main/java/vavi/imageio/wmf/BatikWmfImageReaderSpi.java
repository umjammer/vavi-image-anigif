/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.wmf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.util.Debug;


/**
 * BatikWmfImageReaderSpi.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 220926 nsano initial version <br>
 */
public class BatikWmfImageReaderSpi extends ImageReaderSpi {

    private static final String VendorName = "http://www.vavisoft.com";
    private static final String Version = "1.0.3";
    private static final String ReaderClassName =
        "vavi.imageio.svg.BatikWmfImageReader";
    private static final String[] Names = {
        "wmf", "WMF"
    };
    private static final String[] Suffixes = {
        "wmf", "WMF"
    };
    private static final String[] mimeTypes = {
        "image/x-wmf"
    };
    static final String[] WriterSpiNames = {
        /*"vavi.imageio.svg.BatikWmfImageWriterSpi"*/
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "svg";
    private static final String NativeImageMetadataFormatClassName =
        /*"vavi.imageio.svg.BatikWmfMetaData"*/ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    /** */
    public BatikWmfImageReaderSpi() {
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
        return "WMF as Image Reader using Apache Batik";
    }

    @Override
    public boolean canDecodeInput(Object obj) throws IOException {

        if (obj instanceof ImageInputStream is) {
            final int size = 4;
            byte[] bytes = new byte[size];
            try {
                is.mark();
                is.read(bytes);
                is.reset();
            } catch (IOException e) {
Debug.printStackTrace(e);
                return false;
            }
            return Arrays.equals(new byte[] {(byte) 0xD7, (byte) 0xCD, (byte) 0xC6, (byte) 0x9A}, bytes);
        } else {
Debug.println(Level.FINER, obj);
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new BatikWmfImageReader2(this);
    }
}
