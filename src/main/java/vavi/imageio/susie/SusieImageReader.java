/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.susie;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import spic.ImageInfo;
import spic.SPIConnector;
import vavi.imageio.ImageConverter;

import static java.lang.System.getLogger;


/**
 * SusieImageReader.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070723 nsano initial version <br>
 */
public class SusieImageReader extends ImageReader {

    private static final Logger logger = getLogger(SusieImageReader.class.getName());

    /** */
    private BufferedImage image;
    /** */
    private IIOMetadata metadata;

    /**
     * "susie.plugin.path"
     */
    public SusieImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
        SPIConnector.setSpiDir(System.getProperty("susie.plugin.path"));
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IIOException {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }
        return image.getWidth();
    }

    @Override
    public int getHeight(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }
        return image.getHeight();
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IIOException {

        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        File file = null;
        if (input instanceof File) {
            file = (File) input;
        } else {
            throw new IllegalArgumentException(input.getClass().getName());
        }

        Image tmpImage = SPIConnector.getImage(file.getPath());
logger.log(Level.DEBUG, "tmpImage: " + tmpImage);
        this.image = ImageConverter.getInstance().toBufferedImage(tmpImage);
        return image;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IIOException {
        if (metadata == null) {
            this.metadata = readMetadata();
        }

        return metadata;
    }

    @Override
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
logger.log(Level.DEBUG, imageInfo.getWidth() + ", " + imageInfo.getHeight());

        return null;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        ImageTypeSpecifier specifier = new ImageTypeSpecifier(image);
        List<ImageTypeSpecifier> l = new ArrayList<>();
        l.add(specifier);
        return l.iterator();
    }
}
