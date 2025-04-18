/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.wmf;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
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
import org.apache.batik.transcoder.wmf.tosvg.WMFHeaderProperties2;
import org.apache.batik.transcoder.wmf.tosvg.WMFTranscoder;
import vavi.imageio.WrappedImageInputStream;

import static java.lang.System.getLogger;


/**
 * BatikWmfImageReader2.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 240111 nsano initial version <br>
 */
public class BatikWmfImageReader2 extends ImageReader {

    private static final Logger logger = getLogger(BatikWmfImageReader2.class.getName());

    /** */
    private BufferedImage image;
    /** */
    private IIOMetadata metadata;

    /** */
    public BatikWmfImageReader2(ImageReaderSpi originatingProvider) {
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

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IIOException {

        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        try {
            InputStream is;
            WMFHeaderProperties2 props;

            if (input instanceof ImageInputStream iis) {
                is = new WrappedImageInputStream(iis) {
                    @Override
                    public void close() throws IOException {
//logger.log(Level.TRACE, "ignore close()");
                        // fuckin' hack cause DocumentBuilder#parse() closes input
                    }
                };
                // read props
                iis.mark();
                props = new WMFHeaderProperties2(is);
                iis.reset();
            } else {
                throw new IllegalStateException("ImageInputStream is only supported for input: " + (input == null ? null : input.getClass().getName()));
            }

            Dimension size = param.getSourceRenderSize();

            TranscoderInput input = new TranscoderInput(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8)));
            WMFTranscoder transcoder = new WMFTranscoder();
            transcoder.addTranscodingHint(WMFTranscoder.KEY_XOFFSET, props.getXOffset());
            transcoder.addTranscodingHint(WMFTranscoder.KEY_YOFFSET, props.getYOffset());
logger.log(Level.DEBUG, "offset: " + props.getXOffset() + "+" + props.getYOffset() + "\n" + props);
            if (size != null) {
                transcoder.addTranscodingHint(WMFTranscoder.KEY_WIDTH, (float) size.width);
                transcoder.addTranscodingHint(WMFTranscoder.KEY_WIDTH, (float) size.height);
logger.log(Level.DEBUG, "size is specified: " + size);
            } else {
                transcoder.addTranscodingHint(WMFTranscoder.KEY_WIDTH, (float) props.getWidthBoundsPixels());
                transcoder.addTranscodingHint(WMFTranscoder.KEY_WIDTH, (float) props.getHeightBoundsPixels());
logger.log(Level.DEBUG, "size is not specified");
            }
            transcoder.transcode(input, output);

            ImageTranscoder imageTranscoder = new ImageTranscoder() {
                @Override public BufferedImage createImage(int width, int height) {
                    return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                }
                @Override public void writeImage(BufferedImage image, TranscoderOutput output) throws TranscoderException {
                    BatikWmfImageReader2.this.image = image;
                }
            };
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            TranscoderInput input2 = new TranscoderInput(bais);
            imageTranscoder.transcode(input2, null);

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
        return new BatikWmfImageReadParam();
    }
}
