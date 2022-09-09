/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Graphics;
import java.awt.Image;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vavi.imageio.susie.SusieImageReader;


/**
 * Test0. susie plugin
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2009/06/21 nsano initial version <br>
 */
public class Test0 {

    /** */
    public static void main(String[] args) throws Exception {
        System.err.println(args[0]);
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("BMP");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
System.err.println("ImageReader: " + tmpIr.getClass().getName());
            if (tmpIr.getClass().getName().equals(SusieImageReader.class.getName())) {
                ir = tmpIr;
System.err.println("found ImageReader: " + ir.getClass().getName());
                break;
            }
        }
        ir.setInput(Files.newInputStream(Paths.get(args[0])));
        final Image image = ir.read(0);

        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}

/* */
