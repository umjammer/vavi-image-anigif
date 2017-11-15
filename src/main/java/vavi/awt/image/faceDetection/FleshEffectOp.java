/*
 * face detection
 *
 * NI-Lab.
 */

package vavi.awt.image.faceDetection;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import vavi.awt.image.blobDetection.Blob;
import vavi.awt.image.blobDetection.BlobDetection;
import vavi.awt.image.blobDetection.EdgeVertex;


/**
 * 肌色加工機。
 *
 * @see "http://www.nilab.info/zurazure2/000603.html"
 */
public class FleshEffectOp extends BaseOp {

    /** */
    private BlobDetection bd;
    /** */
    private boolean drawBlobs;
    /** */
    private boolean drawEdges;
    /** */
    private Color edgeColor;
    /** */
    private Color blobColor;

    /** */
    public FleshEffectOp(BlobDetection bd, boolean drawBlobs, boolean drawEdges) {
        super();
        this.bd = bd;
        this.drawBlobs = drawBlobs;
        this.drawEdges = drawEdges;
        this.edgeColor = new Color(0, 0, 255, 127);
        this.blobColor = new Color(0, 255, 0, 127);
    }

    /** */
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {

        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
        }

        Graphics2D g = dst.createGraphics();
        g.drawImage(src, 0, 0, null);

        int w = src.getWidth();
        int h = src.getHeight();

        for (int n = 0; n < bd.getBlobNb(); n++) {

            Blob b = bd.getBlob(n);

            if (b != null) {

                // Edges
                if (drawEdges) {
                    g.setColor(edgeColor); // blue
                    g.setStroke(new BasicStroke(1.0f));
                    for (int m = 0; m < b.getEdgeNb(); m++) {
                        EdgeVertex eA = b.getEdgeVertexA(m);
                        EdgeVertex eB = b.getEdgeVertexB(m);
                        if (eA != null && eB != null) {
                            g.draw(new Line2D.Float(eA.x * w, eA.y * h, eB.x * w, eB.y * h));
                        }
                    }
                }

                // Blobs
                if (drawBlobs) {
                    Shape s = new Ellipse2D.Float(b.xMin * w, b.yMin * h, b.w * w, b.h * h);
                    g.setColor(blobColor); // green
                    g.fill(s);
                    // いまいち見た目がきれいじゃないのでコメントアウト
                    //g.setStroke(new BasicStroke(1.0f));
                    //g.setColor(new Color(255, 0, 0, 127)); // red
                    //g.draw(s);
                }
            }
        }

        g.dispose();

        return dst;
    }
}

/* */
