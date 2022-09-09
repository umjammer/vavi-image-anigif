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
import java.nio.file.Files;
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

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import vavi.imageio.WrappedImageInputStream;


/**
 * BatikSvgImageReader.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070723 nsano initial version <br>
 */
public class BatikSvgImageReader extends ImageReader {
    /** */
    private BufferedImage image;
    /** */
    private IIOMetadata metadata;

    /**
     * "susie.plugin.path" 
     */
    public BatikSvgImageReader(ImageReaderSpi originatingProvider) {
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

    /** */
    private static class BufferedImageTranscoder extends ImageTranscoder {
        @SuppressWarnings("hiding")
        private BufferedImage image;

        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        public void writeImage(BufferedImage image, TranscoderOutput output) throws TranscoderException {
            // ignore output parameter
            this.image = image;
        }

        public BufferedImage getImage() {
            return image;
        }
    }

    /** @see ImageReader */
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IIOException {

        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        try {
            InputStream is;

            if (input instanceof File) {
                is = new BufferedInputStream(Files.newInputStream(((File) input).toPath()));
            } else if (input instanceof ImageInputStream) {
                is = new WrappedImageInputStream((ImageInputStream) input) {
                    public void close() throws IOException {
//System.err.println("ignore close()"); // fuckin' hack cause DocumentBuilder#parse() closes input
                    }
                };
            } else {
                is = new BufferedInputStream((InputStream) input);
            }

            BufferedImageTranscoder trans = new BufferedImageTranscoder();
            TranscoderInput input = new TranscoderInput(is);
            trans.transcode(input, null);
            image = trans.getImage();

            return image;

        } catch (Exception e) {
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

        return null;
    }

    /** */
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        ImageTypeSpecifier specifier = null;
        List<ImageTypeSpecifier> l = new ArrayList<>();
        l.add(specifier);
        return l.iterator();
    }
}

/* */
