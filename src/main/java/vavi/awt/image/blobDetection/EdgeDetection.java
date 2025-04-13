/*
 * Blob Detection library
 *
 * v3ga
 */

package vavi.awt.image.blobDetection;


/**
 * EdgeDetection
 *
 * @see "http://www.v3ga.net/processing/BlobDetection/"
 */
public class EdgeDetection extends MetaBalls2D {

    public final static byte C_R = 0x01;
    public final static byte C_G = 0x02;
    public final static byte C_B = 0x04;

//  public final static byte C_ALL = C_R|C_G|C_B;

//  public byte colorFlag;

    public final int imgWidth;
    public final int imgHeight;

    public int[] pixels;

    public boolean posDiscrimination;

    public final float m_coeff = 3.0f * 255.0f;

    /** */
    public EdgeDetection(int imgWidth, int imgHeight) {
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        super.init(imgWidth, imgHeight);

//      colorFlag=C_ALL;
        posDiscrimination = false;
    }

    /** */
    public void setPosDiscrimination(boolean is) {
        posDiscrimination = is;
    }

    /** */
    public void setThreshold(float value) {
        if (value < 0.0f) {
            value = 0.0f;
        }
        if (value > 1.0f) {
            value = 1.0f;
        }
        setIsoValue(value * m_coeff);
    }

    /** */
//    public void setComponent(byte flag) {
//        if (flag == 0)
//            flag = C_ALL;
//        colorFlag = flag;
//    }

    /** */
    public void setImage(int[] pixels) {
        this.pixels = pixels;
    }

    /** */
    public void computeEdges(int[] pixels) {
        setImage(pixels);
        computeMesh();
    }

    @Override
    public void computeIsoValue() {
        int pixel, r, g, b;
        int x, y;
        int offset;
//      float coeff = 0.0f;

        r = 0;
        g = 0;
        b = 0;
        for (y = 0; y < imgHeight; y++) {
            for (x = 0; x < imgWidth; x++) {
                offset = x + imgWidth * y;

                // Add R,G,B
                pixel = pixels[offset];
                r = (pixel & 0x00FF0000) >> 16;
                g = (pixel & 0x0000FF00) >> 8;
                b = (pixel & 0x000000FF);

                gridValue[offset] = (r + g + b); // / m_coeff
            }
        }
    }

    @Override
    protected int getSquareIndex(int x, int y) {
        int squareIndex = 0;
        int offy = resX * y;
        int offy1 = resX * (y + 1);

        if (!posDiscrimination) {
            if (gridValue[x + offy] < isoValue) {
                squareIndex |= 1;
            }
            if (gridValue[x + 1 + offy] < isoValue) {
                squareIndex |= 2;
            }
            if (gridValue[x + 1 + offy1] < isoValue) {
                squareIndex |= 4;
            }
            if (gridValue[x + offy1] < isoValue) {
                squareIndex |= 8;
            }
        } else {
            if (gridValue[x + offy] > isoValue) {
                squareIndex |= 1;
            }
            if (gridValue[x + 1 + offy] > isoValue) {
                squareIndex |= 2;
            }
            if (gridValue[x + 1 + offy1] > isoValue) {
                squareIndex |= 4;
            }
            if (gridValue[x + offy1] > isoValue) {
                squareIndex |= 8;
            }
        }

        return squareIndex;
    }

    /** */
    public EdgeVertex getEdgeVertex(int index) {
        return edgeVrt[index];
    }
}
