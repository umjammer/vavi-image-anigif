/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import vavi.awt.image.gif.GifAnimationEncoder;
import vavi.awt.image.gif.GifAnimationEncoder.GifFrame;
import vavi.awt.image.gif.GifEncoder.DisposalMethod;


/**
 * Sample4. (direct)
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070619 nsano initial version <br>
 */
public class Sample4 {

    public static void main(String[] args) throws Exception {

        // �طʤ򥻥å�
        File baseFile = new File("Images", "orlando3.gif");
        BufferedImage baseImage = ImageIO.read(baseFile);

        // ���֥������Ȥ�����
        GifAnimationEncoder encoder = new GifAnimationEncoder(baseImage.getWidth(), baseImage.getHeight());

        // �롼�ײ����̵����
        encoder.setLoopNumber(0);

        // 
        File file = new File("Images", "8.gif");
        // Image plane = ImageIO.read(file);
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        ImageInputStream in = new FileImageInputStream(file);
        reader.setInput(in, true);
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        List<Point> imagePos = new ArrayList<Point>();
        List<BufferedImage> backImages = new ArrayList<BufferedImage>();
        for (int i = 0;; i++) {
            try {
                BufferedImage image = reader.read(i);
                images.add(image);

                IIOMetadata metadata = reader.getImageMetadata(i);
                IIOMetadataNode metadataNode = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
                Point point = new Point();
                // IIOMetadataNode xpath ���Ȥ��ʤ� ... orz
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

        for (int i = 0; i < images.size(); i++) {
            GifFrame overFrame = new GifFrame(backImages.get(i));
            // ɽ�����֤�0.5��
            overFrame.setDelayTime(10);

            // ɽ��������β���������ʤȤ������ȤϺǽ�Υ��᡼���ˤ��֤���ɽ�����뤳�Ȥˤʤ��
            overFrame.setDisposalMethod(DisposalMethod.RestoreToPrevious);

            Graphics g = backImages.get(i).getGraphics();
            g.drawImage(baseImage, 0, 0, null);

            for (int j = 0; j < points.length; j++) {
                int k = (i + j) % 2;
                int x = points[j].x + imagePos.get(k).x;
                int y = points[j].y + imagePos.get(k).y;
                g.drawImage(images.get(k), x, y, null);
            }

            // ���᡼���򥻥å�
            encoder.addImage(overFrame);
        }

        // ���󥳡��ɤ���
        File outFile = new File("Images", "animationSample4.gif");
        encoder.encode(new FileOutputStream(outFile));
System.err.println("done");
    }
}
