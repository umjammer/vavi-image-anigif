/*
 * face detection
 * 
 * NI-Lab.
 */

package vavi.awt.image.faceDetection;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import vavi.awt.image.blobDetection.Blob;
import vavi.awt.image.blobDetection.BlobDetection;


/**
 * 肌色加工機。
 * 
 * @see "http://www.nilab.info/zurazure2/000603.html"
 */
public class FleshReplaceOp extends BaseOp {

    /** */
    private BlobDetection bd;
    /** */
    private BufferedImage smudger;

    /** */
    public FleshReplaceOp(BlobDetection bd, BufferedImage smudger) {
        super();
        this.bd = bd;
        this.smudger = smudger;
    }

    /** */
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {

        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
        }

        int w = src.getWidth();
        int h = src.getHeight();

        // 貼り付ける画像サイズの1/6以下は、貼り付けない閾値
        double smudger_min_size = (smudger.getWidth() + smudger.getHeight()) / 2.0 / 6.0;

        Graphics2D g = dst.createGraphics();
        g.drawImage(src, 0, 0, null);

        for (int n = 0; n < bd.getBlobNb(); n++) {

            Blob b = bd.getBlob(n);

            if (b != null) {
                double ww = b.w * w;
                double hh = b.h * h;
                double size = (ww + hh) / 2.0;
                double limit = size;
                if (limit > smudger_min_size) {
                    double x = (b.xMax + b.xMin) * w / 2.0 - (size / 2.0);
                    double y = (b.yMax + b.yMin) * h / 2.0 - (size / 2.0);
                    g.drawImage(smudger, (int) x, (int) y, (int) size, (int) size, null);
                }
            }
        }

        g.dispose();

        return dst;
    }
}

/* */
