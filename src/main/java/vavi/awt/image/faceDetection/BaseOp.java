/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.faceDetection;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;


/**
 * BaseOp.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2008/02/29 nsano initial version <br>
 */
abstract class BaseOp implements BufferedImageOp {

    /**
     * @param destCM when null, used src color model
     */
    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        Rectangle destBounds = (Rectangle) getBounds2D(src);
        return new BufferedImage(destBounds.width, destBounds.height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Double();
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    /** TODO impl */
    @Override
    public RenderingHints getRenderingHints() {
        return null;
    }
}
