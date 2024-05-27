/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.faceDetection;

import java.awt.image.BufferedImage;

import vavi.awt.image.blobDetection.Blob;
import vavi.awt.image.blobDetection.BlobDetection;


/**
 * 肌色探知機。
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080229 nsano initial version <br>
 */
public class FleshDetector {

    /** */
    public static BlobDetection detectFaces(BufferedImage src) {

     // 肌色部分を白色とする白黒2値画像を生成
        BufferedImage bin = new FleshDetectOp().filter(src, null);

        // 白色部分を膨張化
        BufferedImage dil = new MorphOp(5).filter(bin, null);

        // BlobDetect にて肌色部分を取得
        BlobDetection bd = FleshDetector.detectWhite(dil);

        return bd;
    }

 /** */
    public static BlobDetection detectWhite(BufferedImage cs) {

        Blob.MAX_NBLINE = 4000;// default 4000
        BlobDetection.blobMaxNumber = 1000; //default 1000

        BlobDetection bd = new BlobDetection(cs.getWidth(), cs.getHeight());
        bd.setPosDiscrimination(false);
        bd.setThreshold(0.38f);
        int[] pixels = cs.getRGB(0, 0, cs.getWidth(), cs.getHeight(), null, 0, cs.getWidth());

        bd.computeBlobs(pixels);

        return bd;
    }
}
