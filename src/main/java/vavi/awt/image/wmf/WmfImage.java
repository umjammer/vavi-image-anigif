/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.wmf;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import vavi.awt.image.wmf.WindowsMetafile.Renderer;


/**
 * WmfImage.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070630 nsano initial version <br>
 */
class WmfImage {

    /** */
    private final WindowsMetafile metafile;

    /** */
    public BufferedImage getImage() {
        return (BufferedImage) metafile.render();
    }

    /** */
    public Dimension getSize() {
        return metafile.getSize();
    }

    /** */
    public void setSize(Dimension size) {
        metafile.setSize(size);
    }

    /** */
    private final Renderer<BufferedImage> renderer = new ImageRenderer();

    /** */
    public WmfImage(InputStream is) throws IOException {

        this.metafile = WindowsMetafile.readFrom(is);
        metafile.setRenderer(renderer);
    }

    /** */
    public WmfImage(InputStream is, int width, int height) throws IOException {

        this(is);
        metafile.setSize(new Dimension(width, height));
    }
}
