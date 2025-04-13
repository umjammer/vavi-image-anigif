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
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import vavi.awt.image.blobDetection.BlobDetection;
import vavi.awt.image.faceDetection.FleshDetectOp;
import vavi.awt.image.faceDetection.FleshDetector;
import vavi.awt.image.faceDetection.FleshEffectOp;
import vavi.awt.image.faceDetection.MorphOp;


/**
 * JdkAniGifTest7. (jdk6 ImageIO)
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070619 nsano initial version <br>
 */
public class JdkAniGifTest7 {

    /** */
    public static void main(String[] args) throws Exception {
        new JdkAniGifTest7(args);
    }

    /** */
    JdkAniGifTest7(String[] args) throws IOException {
        // 背景をセット
        File baseFile = new File("Images", "orlando3.gif");
        BufferedImage baseImage = ImageIO.read(baseFile);

        // 肌色部分を白色とする白黒2値画像を生成
        BufferedImage b2 = new FleshDetectOp().filter(baseImage, null);
        // 白色部分を膨張化
        BufferedImage b3 = new MorphOp(3).filter(b2, null); // 閾値周辺5ピクセル
        // BlobDetect にて肌色部分を取得
        BlobDetection bd = FleshDetector.detectWhite(b3);
        // 肌色認識部分を図示
        BufferedImage white = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        BufferedImage face = new FleshEffectOp(bd, true, false).filter(white, null);
//System.err.println("w, h: " + face.getWidth() + ", " + face.getHeight());
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

        Point max = new Point();
        for (Point pos : imagePos) {
            if (pos.x > max.x) {
                max.x = pos.x;
            }
            if (pos.y > max.y) {
                max.y = pos.y;
            }
        }

        File outFile = new File("Images", "animationSample8.gif");
        ImageOutputStream out = new FileImageOutputStream(outFile);

        Iterator<ImageWriter> iws = ImageIO.getImageWritersByFormatName("gif");
        ImageWriter writer = null;
        while (iws.hasNext()) {
            ImageWriter iw = iws.next();
// System.err.println("writer: " + iw.getOriginatingProvider().getDescription(Locale.getDefault()));
// System.err.println("writer: " + iw.getClass().getName());
            if (iw.getClass().getName().equals("com.sun.imageio.plugins.gif.GIFImageWriter")) {
                writer = iw;
                break;
            }
        }
System.err.println("writer: " + writer.getClass().getName());

        Point[] points = new Point[600];
        int p = 0;
        while (p < points.length) {
            int x = (int) (Math.random() * (baseImage.getWidth() - max.x));
            int y = (int) (Math.random() * (baseImage.getHeight() - max.y));
            if (face.getRGB(x, y) != 0xff00_0000) {
                continue;
            } else {
                points[p] = new Point();
                points[p].x = x;
                points[p].y = y;
                p++;
            }
        }

        for (int i = 0; i < images.size(); i++) {
            Graphics g = backImages.get(i).getGraphics();
            g.drawImage(baseImage, 0, 0, null);

            for (int j = 0; j < points.length; j++) {
                int k = (i + j) % images.size(); // TODO images.size() が 2 でない場合
                int x = points[j].x + imagePos.get(k).x;
                int y = points[j].y + imagePos.get(k).y;
                g.drawImage(images.get(k), x, y, null);
            }
        }

write(writer, backImages, out);

        out.flush();
        out.close();

        // エンコードする
        System.err.println("done");
    }

    /** */
    final int timeBetweenFramesMS = 10;

    /** */
    final boolean loopContinuously = true;

    /** */
    void write(ImageWriter writer, List<BufferedImage> images, ImageOutputStream os) throws IOException {

        // create a writer
        ImageWriteParam imageWriteParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier imageTypeSpecifier = new ImageTypeSpecifier(images.get(0));

        IIOMetadata imageMetaData = writer.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

        String metaFormatName = imageMetaData.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");

        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(timeBetweenFramesMS / 10));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
        commentsNode.setAttribute("CommentExtension", "Created by Vavi");

        IIOMetadataNode appEntensionsNode = getNode(root, "ApplicationExtensions");
        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");

        int loop = loopContinuously ? 0 : 1;

        child.setUserObject(new byte[] {
            0x1, (byte) (loop & 0xFF), (byte) ((loop >> 8) & 0xFF)
        });
        appEntensionsNode.appendChild(child);

        imageMetaData.setFromTree(metaFormatName, root);

        writer.setOutput(os);

        writer.prepareWriteSequence(null);

        for (BufferedImage image : images) {
            // Draw into the BufferedImage, and then do
            writer.writeToSequence(new IIOImage(image, null, imageMetaData), imageWriteParam);

        }
        writer.endWriteSequence();
    }

    /**
     * Returns an existing child node, or creates and returns a new child node
     * (if the requested node does not exist).
     * 
     * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child
     *            node.
     * @param nodeName the name of the child node.
     * 
     * @return the child node, if found or a new node created with the given
     *         name.
     */
    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }
}
