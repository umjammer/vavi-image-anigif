/*
 * Blob Detection library
 *
 * v3ga
 */

package vavi.awt.image.blobDetection;


/**
 * MetaBalls2D
 *
 * @see "http://www.v3ga.net/processing/BlobDetection/"
 */
public class MetaBalls2D {

    /** Isovalue */
    protected float isoValue;

    /** Grid */
    protected int resX, resY;

    protected float stepX, stepY;

    protected float[] gridValue;

    protected int nbGridValue;

    /** Voxels */
    protected int[] voxel;

    protected int nbVoxel;

    /** EdgeVertex */
    protected EdgeVertex[] edgeVrt;

    protected int nbEdgeVrt;

    /**
     * Lines
     * what we pass to the renderer
     */
    protected int[] lineToDraw;

    protected int nbLineToDraw;

    /** init(int, int) */
    public void init(int resx, int resy) {
        this.resX = resx;
        this.resY = resy;

        this.stepX = 1.0f / (resx - 1);
        this.stepY = 1.0f / (resy - 1);

        // Allocate gridValue
        nbGridValue = resx * resy;
        gridValue = new float[nbGridValue];

        // Allocate voxels
        nbVoxel = nbGridValue;
        voxel = new int[nbVoxel];

        // Allocate EdgeVertices
        edgeVrt = new EdgeVertex[2 * nbVoxel];
        nbEdgeVrt = 2 * nbVoxel;

        // Allocate Lines
        lineToDraw = new int[2 * nbVoxel];
        nbLineToDraw = 0;

        // Precompute some values
        int x, y, n, index;
        n = 0;
        for (x = 0; x < resx; x++)
            for (y = 0; y < resy; y++) {
                index = 2 * n;
                // index to edgeVrt
                voxel[x + resx * y] = index;
                // values
                edgeVrt[index] = new EdgeVertex(x * stepX, y * stepY);
                edgeVrt[index + 1] = new EdgeVertex(x * stepX, y * stepY);

                // Next!
                n++;
            }

    }

    /** computeIsoValue */
    public void computeIsoValue() {
        // A simple test : put a metaball on center of the screen
//        float ballx = 0.5f;
//        float bally = 0.5f;
//        float vx, vy;
//        float dist;
//
//        int x, y;
//        vx = 0.0f;
//        for (x = 0; x < resX; x++) {
//            vy = 0.0f;
//            for (y = 0; y < resY; y++) {
//                dist = (float) sqrt((vx - ballx) * (vx - ballx) + (vy - bally) * (vy - bally));
//                gridValue[x + resX * y] = 10.0f / (dist * dist + 0.001f);
//                vy += stepY;
//            }
//            vx += stepX;
//        }
    }

    /** */
    public void computeMesh() {
        // Compute IsoValue
        computeIsoValue();
        // Get Lines indices

        int x, y, squareIndex, n;
        int iEdge;
        int offx, offy, offAB;
        int toCompute;
        int offset;
        float t;
        float vx, vy;
        int[] edgeOffsetInfo;

        nbLineToDraw = 0;
        vx = 0.0f;
        for (x = 0; x < resX - 1; x++) {
            vy = 0.0f;
            for (y = 0; y < resY - 1; y++) {
                offset = x + resX * y;
                squareIndex = getSquareIndex(x, y);

                n = 0;
                while ((iEdge = MetaBallsTable.edgeCut[squareIndex][n++]) != -1) {
                    edgeOffsetInfo = MetaBallsTable.edgeOffsetInfo[iEdge];
                    offx = edgeOffsetInfo[0];
                    offy = edgeOffsetInfo[1];
                    offAB = edgeOffsetInfo[2];

                    lineToDraw[nbLineToDraw++] = voxel[(x + offx) + resX * (y + offy)] + offAB;
                }

                toCompute = MetaBallsTable.edgeToCompute[squareIndex];
                if (toCompute > 0) {
                    if ((toCompute & 1) > 0) // Edge 0
                    {
                        t = (isoValue - gridValue[offset]) / (gridValue[offset + 1] - gridValue[offset]);
                        edgeVrt[voxel[offset]].x = vx * (1.0f - t) + t * (vx + stepX);
                    }
                    if ((toCompute & 2) > 0) // Edge 3
                    {
                        t = (isoValue - gridValue[offset]) / (gridValue[offset + resX] - gridValue[offset]);
                        edgeVrt[voxel[offset] + 1].y = vy * (1.0f - t) + t * (vy + stepY);
                    }

                } // toCompute
                vy += stepY;
            } // for y

            vx += stepX;
        } // for x

        nbLineToDraw /= 2;
    }

    /** */
    protected int getSquareIndex(int x, int y) {
        int squareIndex = 0;
        int offy = resX * y;
        int offy1 = resX * (y + 1);
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
        return squareIndex;
    }

    /** */
    public void setIsoValue(float iso) {
        this.isoValue = iso;
    }
}

/* */
