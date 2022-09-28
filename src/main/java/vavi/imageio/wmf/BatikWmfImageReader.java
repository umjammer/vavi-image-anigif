/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.wmf;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
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
import org.apache.batik.transcoder.wmf.tosvg.WMFTranscoder;
import vavi.imageio.WrappedImageInputStream;
import vavi.io.InputEngine;
import vavi.io.InputEngineOutputStream;
import vavi.util.Debug;


/**
 * BatikWmfImageReader.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 220926 nsano initial version <br>
 */
public class BatikWmfImageReader extends ImageReader {
    /** */
    private BufferedImage image;
    /** */
    private IIOMetadata metadata;

    /** */
    public BatikWmfImageReader(ImageReaderSpi originatingProvider) {
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

        BufferedImageTranscoder() {
        }

        @Override
        public BufferedImage createImage(int width, int height) {
Debug.println(Level.FINER, "size: " + width + "x" + height);
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage image, TranscoderOutput output) throws TranscoderException {
            // ignore output parameter
Debug.println(Level.FINER, "writeImage: " + image.getWidth() + "x" + image.getHeight());
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
                    public void close() throws IOException {
//Debug.println("ignore close()");
                        // fuckin' hack cause DocumentBuilder#parse() closes input
                    }
                };
            } else {
                throw new IllegalStateException("ImageInputStream is only supported for input: " + (input == null ? null : input.getClass().getName()));
            }

            Dimension size = param.getSourceRenderSize();

            OutputStream os = new InputEngineOutputStream(new InputEngine() {
                BufferedImageTranscoder trans = new BufferedImageTranscoder();
                TranscoderInput input;
                @Override public void initialize(InputStream inputStream) throws IOException {
                     input = new TranscoderInput(inputStream);
                }
                @Override public void execute() throws IOException {
                    try {
                        trans.transcode(input, null);
                    } catch (TranscoderException e) {
                        throw new IOException(e);
                    }
                }
                @Override public void finish() throws IOException {
                    image = trans.getImage();
                }
            });

            TranscoderInput input = new TranscoderInput(is);
            TranscoderOutput output = new TranscoderOutput(os);
            WMFTranscoder transcoder = new WMFTranscoder();
            if (size != null) {
                transcoder.addTranscodingHint(WMFTranscoder.KEY_WIDTH, (float) size.width);
                transcoder.addTranscodingHint(WMFTranscoder.KEY_WIDTH, (float) size.height);
            }
            transcoder.transcode(input, output);

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

        ImageTypeSpecifier specifier = null;
        List<ImageTypeSpecifier> l = new ArrayList<>();
        l.add(specifier);
        return l.iterator();
    }

    @Override
    public ImageReadParam getDefaultReadParam() {
        return new BatikWmfImageReadParam();
    }
}

/* */
