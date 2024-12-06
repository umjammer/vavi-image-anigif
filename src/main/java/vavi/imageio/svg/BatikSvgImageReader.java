/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.svg;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
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
import javax.imageio.stream.ImageInputStream;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import vavi.imageio.WrappedImageInputStream;

import static java.lang.System.getLogger;


/**
 * BatikSvgImageReader.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070723 nsano initial version <br>
 */
public class BatikSvgImageReader extends ImageReader {

    private static final Logger logger = getLogger(BatikSvgImageReader.class.getName());

    /** */
    private BufferedImage image;
    /** */
    private IIOMetadata metadata;

    /** */
    public BatikSvgImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
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

    /** */
    private static class BufferedImageTranscoder extends ImageTranscoder {
        @SuppressWarnings("hiding")
        private BufferedImage image;

        BufferedImageTranscoder(Dimension size) {
            if (size != null) {
                addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) size.width);
                addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) size.height);
            }
        }

        @Override
        public BufferedImage createImage(int width, int height) {
logger.log(Level.TRACE, "size: " + width + "x" + height);
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage image, TranscoderOutput output) throws TranscoderException {
            // ignore output parameter
logger.log(Level.TRACE, "writeImage: " + image.getWidth() + "x" + image.getHeight());
            this.image = image;
        }

        public BufferedImage getImage() {
            return image;
        }
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IIOException {

        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        try {
            InputStream is;

            if (input instanceof ImageInputStream) {
                is = new WrappedImageInputStream((ImageInputStream) input) {
                    @Override
                    public void close() throws IOException {
//logger.log(Level.TRACE, "ignore close()");
                        // fuckin' hack cause DocumentBuilder#parse() closes input
                    }
                };
            } else {
                throw new IllegalStateException("ImageInputStream is only supported for input: " + (input == null ? null : input.getClass().getName()));
            }

            BufferedImageTranscoder trans = new BufferedImageTranscoder(param.getSourceRenderSize());
            TranscoderInput input = new TranscoderInput(is);
            trans.transcode(input, null);
            image = trans.getImage();

            return image;

        } catch (Exception e) {
            throw new IIOException(e.getMessage(), e);
        }
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

    @Override
    public ImageReadParam getDefaultReadParam() {
        return new BatikSvgImageReadParam();
    }
}
