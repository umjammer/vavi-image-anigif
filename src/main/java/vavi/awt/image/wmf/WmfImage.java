/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.wmf;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

import vavi.awt.image.wmf.WindowsMetafile.Renderer;


/**
 * WmfImage. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070630 nsano initial version <br>
 */
class WmfImage {

    /** */
    private WindowsMetafile metafile;

    /** */
    public Image getImage() {
        return (Image) metafile.render();
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
    private Renderer<Image> renderer = new ImageRenderer();

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

    //----

    /** */
    public static void main(String[] args) throws Exception {
        String file = args[0];
System.err.println("file: " + file);

        final WmfImage wmf = new WmfImage(new FileInputStream(file)/*, 640, 480*/);

        final JFrame frame = new JFrame();
        frame.setTitle(file);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(wmf.getImage(), 0, 0, this);
            }
            public Dimension getPreferredSize() {
                return wmf.getSize();
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                wmf.setSize(frame.getSize());
                frame.repaint();
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}

/* */
