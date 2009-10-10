/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.svg;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import spic.ImageInfo;
import spic.SPIConnector;

import vavi.awt.image.svg.SvgImage;
import vavi.imageio.WrappedImageInputStream;


/**
 * SuvgImageReader.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070723 nsano initial version <br>
 */
public class SvgImageReader extends ImageReader {
    /** */
    private BufferedImage image;
    /** */
    private IIOMetadata metadata;

    /**
     * "susie.plugin.path" 
     */
    public SvgImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /** @see ImageReader */
    public int getNumImages(boolean allowSearch) throws IIOException {
        return 1;
    }

    /** @see ImageReader */
    public int getWidth(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }
        return image.getWidth();
    }

    /** @see ImageReader */
    public int getHeight(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }
        return image.getHeight();
    }

    /** @see ImageReader */
    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IIOException {

        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        try {
            InputStream is;

            if (input instanceof File) {
                is = new BufferedInputStream(new FileInputStream((File) input));
            } else if (input instanceof ImageInputStream) {
                is = new WrappedImageInputStream((ImageInputStream) input) {
                    public void close() throws IOException {
//System.err.println("ignore close()"); // fuckin' hack cause DocumentBuilder#parse() closes input
                    }
                };
            } else {
                is = new BufferedInputStream((InputStream) input);
            }

            image = new SvgImage(is).getImage();

            return image;

        } catch (IOException e) {
            throw new IIOException(e.getMessage(), e);
        }
    }

    /** @see ImageReader */
    public IIOMetadata getStreamMetadata() throws IIOException {
        if (metadata == null) {
            this.metadata = readMetadata();
        }

        return metadata;
    }

    /** @see ImageReader */
    public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        if (metadata == null) {
            this.metadata = readMetadata();
        }

        return metadata;
    }

    /** */
    private IIOMetadata readMetadata() throws IIOException {
        File file = null;
        if (input instanceof File) {
            file = (File) input;
        } else {
            throw new IllegalArgumentException(input.getClass().getName());
        }

        ImageInfo imageInfo = SPIConnector.getImageInfo(file.getPath());
System.err.println(imageInfo.getWidth() + ", " + imageInfo.getHeight());

        return null;
    }

    /** */
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        ImageTypeSpecifier specifier = null;
        List<ImageTypeSpecifier> l = new ArrayList<ImageTypeSpecifier>();
        l.add(specifier);
        return l.iterator();
    }
}

/* */
