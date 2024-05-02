/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.susie;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

import spic.SPIConnector;
import vavi.util.Debug;


/**
 * SusieImageReaderSpi.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070723 nsano initial version <br>
 */
public class SusieImageReaderSpi extends ImageReaderSpi {

    private static final String VendorName = "http://www.vavisoft.com";
    private static final String Version = "1.0.3";
    private static final String ReaderClassName =
        "vavi.imageio.susie.SusieImageReader";
    private static final String[] Names = {
        // TODO src/main/resources/vavi/imageio/susie/spi.properties
        "BMP", "JPEG", "WMF"
    };
    private static final String[] Suffixes = {
        // TODO
        "bmp", "BMP", "wmf", "WMF"
    };
    private static final String[] mimeTypes = {
        "image/x-bmp"
    };
    static final String[] WriterSpiNames = {
        /*"vavi.imageio.susie.SusieImageWriterSpi"*/
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "bmp";
    private static final String NativeImageMetadataFormatClassName =
        /*"vavi.imageio.susie.SusieMetaData"*/ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    static {
        // TODO
        try {
            Pattern pattern = Pattern.compile("[Ii][Ff](.+)\\.[Ss][Pp][Ii]");
            String path = System.getProperty("susie.plugin.path");
            File[] files = new File(path).listFiles(pathname -> {
                Matcher matcher = pattern.matcher(pathname.getName());
                return matcher.matches();
            });
            for (File file : files) {
                Matcher matcher = pattern.matcher(file.getName());
                if (Debug.isLoggable(Level.FINER)) {
                    if (matcher.matches()) {
                        Debug.println("plugin: " + matcher.group(1).toLowerCase());
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** */
    public SusieImageReaderSpi() {
        super(VendorName,
              Version,
              Names,
              Suffixes,
              mimeTypes,
              ReaderClassName,
              new Class[] { File.class/*, ImageInputStream.class, InputStream.class*/ },
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
        return "Image loaded by Susie Plugin";
    }

    @Override
    public boolean canDecodeInput(Object obj) throws IOException {
        if (obj instanceof File) {
            File file = (File) obj;
            if (SPIConnector.getImageInfo(file.getPath()) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new SusieImageReader(this);
    }
}

/* */
