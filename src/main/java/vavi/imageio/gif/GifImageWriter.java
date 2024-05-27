/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import vavi.awt.image.gif.GifAnimationEncoder;
import vavi.awt.image.gif.GifAnimationEncoder.GifFrame;
import vavi.imageio.WrappedImageOutputStream;


/**
 * GifImageWriter.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070725 nsano initial version <br>
 */
public class GifImageWriter extends ImageWriter {

    /** */
    private GifAnimationEncoder encoder;

    /** */
    protected GifImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        if (output == null) {
            throw new IllegalStateException("Output is unset.");
        }

        if (image == null) {
            throw new IllegalArgumentException("Image may not be null");
        }

        RenderedImage renderedImage = image.getRenderedImage();

        if(!(renderedImage instanceof BufferedImage)) {
            throw new IllegalArgumentException("Image is not a BufferedImage");
        }

        if (param == null) {
            param = getDefaultWriteParam();
        }

        GifFrame frame = new GifFrame((BufferedImage) renderedImage);
        // TODO use ImageWriteParam
        GifImageWriteParam gifParam = (GifImageWriteParam) param;
        frame.setDelayTime(gifParam.getDelayTime());
        frame.setDisposalMethod(gifParam.getDisposalMethod());
        encoder.addImage(frame);

        if (!writeSequencePrepared) {
            this.encoder = new GifAnimationEncoder(renderedImage.getWidth(), renderedImage.getHeight());
            encoder.encode(new WrappedImageOutputStream((ImageOutputStream) output));
        }
    }

    @Override
    public boolean canWriteSequence() {
        return true;
    }

    /** */
    private IIOMetadata streamMetadata;

    /** */
    private boolean writeSequencePrepared = false;

    @Override
    public void prepareWriteSequence(IIOMetadata streamMetadata) {
        this.streamMetadata = streamMetadata;
        this.writeSequencePrepared = true;
        this.encoder = new GifAnimationEncoder();
    }

    @Override
    public void writeToSequence(IIOImage image, ImageWriteParam imageWriteParam) throws IOException {
        if (!writeSequencePrepared) {
            throw new IllegalStateException("prepareWriteSequence has not been called");
        }
        // get size from first image
        if (encoder.getWidth() == -1 || encoder.getHeight() == -1) {
            encoder.setLoopNumber(0); // TODO from param
            encoder.setWidth(image.getRenderedImage().getWidth());
            encoder.setHeight(image.getRenderedImage().getHeight());
        }
        write(streamMetadata, image, imageWriteParam);
    }

    @Override
    public void endWriteSequence() throws IOException {
        if (!writeSequencePrepared) {
            throw new IllegalStateException("prepareWriteSequence has not been called");
        }
        encoder.encode(new WrappedImageOutputStream((ImageOutputStream) output));
        this.writeSequencePrepared = false;
    }

    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new GifImageWriteParam();
    }
}
