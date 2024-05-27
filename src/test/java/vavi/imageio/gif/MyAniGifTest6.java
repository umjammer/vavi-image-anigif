/*
 * Copyright (c) ${YEAR} by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import vavi.awt.image.gif.GifEncoder.DisposalMethod;


/**
 * MyAniGifTest6. (my ImageIO)
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070725 nsano initial version <br>
 */
public class MyAniGifTest6 {

    /** */
    public static void main(String[] args) throws Exception {
        new MyAniGifTest6(args);
    }

    /** */
    MyAniGifTest6(String[] args) throws IOException {
        File baseFile = new File("Images", "orlando3.gif");
        BufferedImage baseImage = ImageIO.read(baseFile);

        // 
        File file = new File("Images", "8.gif");
        // Image plane = ImageIO.read(file);
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        ImageInputStream in = new FileImageInputStream(file);
        reader.setInput(in, true);
        List<BufferedImage> images = new ArrayList<>();
        List<Point> imagePos = new ArrayList<>();
        List<BufferedImage> backImages = new ArrayList<>();
        for (int i = 0;; i++) {
            try {
                BufferedImage image = reader.read(i);
                images.add(image);

                IIOMetadata metadata = reader.getImageMetadata(i);
                IIOMetadataNode metadataNode = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
                Point point = new Point();
                // IIOMetadataNode xpath が使えない ... orz
                point.x = Integer.parseInt(metadataNode.getElementsByTagName("ImageDescriptor").item(0).getAttributes().getNamedItem("imageLeftPosition").getNodeValue());
                point.y = Integer.parseInt(metadataNode.getElementsByTagName("ImageDescriptor").item(0).getAttributes().getNamedItem("imageTopPosition").getNodeValue());
                imagePos.add(point);
System.err.println(i + " pos: " + point);

                BufferedImage backImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
                backImages.add(backImage);
            } catch (IndexOutOfBoundsException e) {
                System.err.println(i + " images");
                break;
            }
        }

        Point[] points = new Point[300];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point();
            points[i].x = (int) (Math.random() * baseImage.getWidth());
            points[i].y = (int) (Math.random() * baseImage.getHeight());
        }

        File outFile = new File("Images", "animationSample6.gif");
        ImageOutputStream out = new FileImageOutputStream(outFile);

        Iterator<ImageWriter> iws = ImageIO.getImageWritersByFormatName("gif");
        ImageWriter writer = null;
        while (iws.hasNext()) {
            ImageWriter iw = iws.next();
//System.err.println("writer D: " + iw.getOriginatingProvider().getDescription(Locale.getDefault()));
//System.err.println("writer C: " + iw.getClass().getName());
            if (iw.getClass().getName().equals("vavi.imageio.gif.GifImageWriter")) {
                writer = iw;
                break;
            }
        }
System.err.println("writer: " + writer.getClass().getName());

        for (int i = 0; i < images.size(); i++) {
            Graphics g = backImages.get(i).getGraphics();
            g.drawImage(baseImage, 0, 0, null);

            for (int j = 0; j < points.length; j++) {
                int k = (i + j) % 2;
                int x = points[j].x + imagePos.get(k).x;
                int y = points[j].y + imagePos.get(k).y;
                g.drawImage(images.get(k), x, y, null);
            }
        }

write(writer, backImages, out);

        out.flush();
        out.close();

        // 鐃緒申鐃藷コ¥申鐃宿わ申鐃緒申
        System.err.println("done");
    }

    /** */
    void write(ImageWriter writer, List<BufferedImage> images, ImageOutputStream os) throws IOException {

        // create a writer
        writer.setOutput(os);

        writer.prepareWriteSequence(null);

        for (BufferedImage image : images) {
            GifImageWriteParam imageWriteParam = new GifImageWriteParam();
            imageWriteParam.setDelayTime(10);
            imageWriteParam.setDisposalMethod(DisposalMethod.RestoreToPrevious);
            writer.writeToSequence(new IIOImage(image, null, null), imageWriteParam);
        }

        writer.endWriteSequence();
    }
}
