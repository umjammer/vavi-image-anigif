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
 * ���F�T�m�@�B
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080229 nsano initial version <br>
 */
public class FleshDetector {

    /** */
    public static BlobDetection detectFaces(BufferedImage src) {
    
        // ���F�����𔒐F�Ƃ��锒��2�l�摜�𐶐�
        BufferedImage bin = new FleshDetectOp().filter(src, null);
    
        // ���F������c����
        BufferedImage dil = new MorphOp(5).filter(bin, null);
    
        // BlobDetect �ɂĔ��F�������擾
        BlobDetection bd = FleshDetector.detectWhite(dil);
    
        return bd;
    }

    /** */
    public static BlobDetection detectWhite(BufferedImage cs) {
    
        Blob.MAX_NBLINE = 4000; // default 4000
        BlobDetection.blobMaxNumber = 1000; //default 1000
    
        BlobDetection bd = new BlobDetection(cs.getWidth(), cs.getHeight());
        bd.setPosDiscrimination(false);
        bd.setThreshold(0.38f);
        int[] pixels = cs.getRGB(0, 0, cs.getWidth(), cs.getHeight(), null, 0, cs.getWidth());
    
        bd.computeBlobs(pixels);
    
        return bd;
    }
}

/* */
