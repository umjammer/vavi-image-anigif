/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.susie;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

import spic.SPIConnector;


/**
 * SusieImageReaderSpi.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070723 nsano initial version <br>
 */
public class SusieImageReaderSpi extends ImageReaderSpi {

    private static final String vendorName = "http://www.vavisoft.com";
    private static final String version = "0.00";
    private static final String readerClassName =
        "vavi.imageio.susie.SusieImageReader";
    private static final String names[] = {
        // TODO 
        "BMP", "JPEG", "WMF"
    };
    private static final String suffixes[] = {
        // TODO 
        "bmp", "BMP", "wmf", "WMF"
    };
    private static final String mimeTypes[] = {
        "image/x-bmp"
    };
    static final String writerSpiNames[] = {
        /*"vavi.imageio.susie.SusieImageWriterSpi"*/
    };
    private static final boolean supportsStandardStreamMetadataFormat = false;
    private static final String nativeStreamMetadataFormatName = null;
    private static final String nativeStreamMetadataFormatClassName = null;
    private static final String extraStreamMetadataFormatNames[] = null;
    private static final String extraStreamMetadataFormatClassNames[] = null;
    private static final boolean supportsStandardImageMetadataFormat = false;
    private static final String nativeImageMetadataFormatName = "bmp";
    private static final String nativeImageMetadataFormatClassName =
        /*"vavi.imageio.susie.SusieMetaData"*/ null;
    private static final String extraImageMetadataFormatNames[] = null;
    private static final String extraImageMetadataFormatClassNames[] = null;

    static {
        // TODO 
        try {
            final Pattern pattern = Pattern.compile("[Ii][Ff](.+)\\.[Ss][Pp][Ii]");
            String path = System.getProperty("susie.plugin.path");
            File[] files = new File(path).listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    Matcher matcher = pattern.matcher(pathname.getName());
                    return matcher.matches();
                }
            });
            for (File file : files) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    System.err.println("plugin: " + matcher.group(1).toLowerCase());
                }
            }
        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** */
    public SusieImageReaderSpi() {
        super(vendorName,
              version,
              names,
              suffixes,
              mimeTypes,
              readerClassName,
              new Class[] { File.class/*, ImageInputStream.class, InputStream.class*/ },
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
        return "Image loaded by Susie Plugin";
    }
    
    /* */
    public boolean canDecodeInput(Object obj)
        throws IOException {
        if (obj instanceof File) {
            File file = (File) obj;
            if (SPIConnector.getImageInfo(file.getPath()) != null) {
                return true;
            }
        }
        return false;
    }
    
    /* */
    public ImageReader createReaderInstance(Object obj) {
        return new SusieImageReader(this);
    }
}

/* */
