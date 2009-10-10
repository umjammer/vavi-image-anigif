/*
 * Blob Detection library
 * 
 * v3ga
 */

package vavi.awt.image.blobDetection;

import java.lang.reflect.Method;


/**
 * BlobDetection
 * 
 * @see "http://www.v3ga.net/processing/BlobDetection/"
 */
public class BlobDetection extends EdgeDetection {
    // Temp
    Object parent;

    Method filterBlobMethod;

    // Temp

    public static int blobMaxNumber = 1000;

    public int blobNumber;

    public Blob[] blob;

    public boolean[] gridVisited;

    public int blobWidthMin, blobHeightMin;

    /**
     * Constructor of the class. Parameters are the dimensions of the image on
     * which the detection is going to be applied.
     */
    public BlobDetection(int imgWidth, int imgHeight) {
        super(imgWidth, imgHeight);

        gridVisited = new boolean[nbGridValue];
        blob = new Blob[blobMaxNumber];
        blobNumber = 0;
        for (int i = 0; i < blobMaxNumber; i++)
            blob[i] = new Blob(this);

        blobWidthMin = 0;
        blobHeightMin = 0;

        filterBlobMethod = null;
    }

    /**
     * setBlobDimensionMin
     */
//  public void setBlobDimensionMin(int w, int h) {
//        if (w < 0)
//            w = 0;
//        if (h < 0)
//            h = 0;
//        if (w > imgWidth)
//            w = imgWidth;
//        if (h > imgHeight)
//            h = imgHeight;
//
//        blobWidthMin = w;
//        blobHeightMin = h;
//    }

    /**
     * setNumberBlobMax
     */
    public void setBlobMaxNumber(int nb) {
        blobMaxNumber = nb;
    }

    /**
     * Returns the blob whose index is n in the list of blobs. Returns null if n
     * is outside the range [0;blobNumber-1].
     */
    public Blob getBlob(int n) {
        Blob b = null;
        if (n < blobNumber)
            return blob[n];
        return b;
    }

    /**
     * Returns the numbers of blobs detected in an image.
     */
    public int getBlobNb() {
        return blobNumber;
    }

    /**
     * Compute the blobs in the image.
     */
    public void computeBlobs(int[] pixels) {
        // Image
        setImage(pixels);

        // Clear gridVisited
        for (int i = 0; i < nbGridValue; i++) {
            gridVisited[i] = false;
        }

        // Compute Isovalue
        computeIsovalue();

        // Get Lines indices
        int x, y, squareIndex /* , n */;
//      int iEdge;
//      int offx, offy, offAB;
//      int toCompute;
        int offset;
//      float t;
        float vx, vy;

        nbLineToDraw = 0;
        vx = 0.0f;
        blobNumber = 0;
        for (x = 0; x < resx - 1; x++) {
            vy = 0.0f;
            for (y = 0; y < resy - 1; y++) {
                // > offset in the grid
                offset = x + resx * y;

                // > if we were already there, just go the next square!
                if (gridVisited[offset] == true) {
                    continue;
                }

                // > squareIndex
                squareIndex = getSquareIndex(x, y);

                // >Found something
                if (squareIndex > 0 && squareIndex < 15) {
                    if (blobNumber < blobMaxNumber) {
                        findBlob(blobNumber, x, y);
                        blobNumber++;
                    }
                }
                vy += stepy;
            }
            vx += stepx;
        }
        nbLineToDraw /= 2;
        //blobNumber+=1;
    }

    /** */
    public void findBlob(int iBlob, int x, int y) {
        // Reset Blob values

        blob[iBlob].id = iBlob;
        blob[iBlob].xMin = 1000.0f;
        blob[iBlob].xMax = -1000.0f;
        blob[iBlob].yMin = 1000.0f;
        blob[iBlob].yMax = -1000.0f;
        blob[iBlob].nbLine = 0;

        // Find it !!    
        computeEdgeVertex(iBlob, x, y);

        // > This is just a temp patch (somtimes 'big' blobs are detected on the grid edges)

        if (blob[iBlob].xMin >= 1000.0f || blob[iBlob].xMax <= -1000.0f || blob[iBlob].yMin >= 1000.0f ||
            blob[iBlob].yMax <= -1000.0f) {
            blobNumber--;
        } else {
            blob[iBlob].update();
            // User Filter
            if (filterBlobMethod != null) {
                try {
                    Boolean returnObj = (Boolean) (filterBlobMethod.invoke(parent, new Object[] {
                        blob[iBlob]
                    }));
                    boolean returnValue = returnObj.booleanValue();
                    if (returnValue == false) {
                        blobNumber--;
                    }
                } catch (Exception e) {
                    System.err.println("Disabling filterBlobMethod() because of an error.");
                    filterBlobMethod = null;
                }
            }
        }
    }

    /** */
    void computeEdgeVertex(int iBlob, int x, int y) {
        // offset
        int offset = x + resx * y;

        // Mark voxel as visited
        if (gridVisited[offset] == true)
            return;
        gridVisited[offset] = true;

        // 
        int iEdge, offx, offy, offAB;
        int[] edgeOffsetInfo;
        int squareIndex = getSquareIndex(x, y);
        float vx = x * stepx;
        float vy = y * stepy;

        int n = 0;
        while ((iEdge = MetaballsTable.edgeCut[squareIndex][n++]) != -1) {
            edgeOffsetInfo = MetaballsTable.edgeOffsetInfo[iEdge];
            offx = edgeOffsetInfo[0];
            offy = edgeOffsetInfo[1];
            offAB = edgeOffsetInfo[2];

            if (blob[iBlob].nbLine < Blob.MAX_NBLINE) {
                lineToDraw[nbLineToDraw++] = blob[iBlob].line[blob[iBlob].nbLine++] = voxel[(x + offx) + resx * (y + offy)] + offAB;
            } else {
                return;
            }
        }

        int toCompute = MetaballsTable.edgeToCompute[squareIndex];
        float t = 0.0f;
        float value = 0.0f;
        if (toCompute > 0) {
            if ((toCompute & 1) > 0) { // Edge 0
                t = (isovalue - gridValue[offset]) / (gridValue[offset + 1] - gridValue[offset]);
                value = vx * (1.0f - t) + t * (vx + stepx);
                edgeVrt[voxel[offset]].x = value;

                if (value < blob[iBlob].xMin) {
                    blob[iBlob].xMin = value;
                }
                if (value > blob[iBlob].xMax) {
                    blob[iBlob].xMax = value;
                }

            }
            if ((toCompute & 2) > 0) { // Edge 3
                t = (isovalue - gridValue[offset]) / (gridValue[offset + resx] - gridValue[offset]);
                value = vy * (1.0f - t) + t * (vy + stepy);
                edgeVrt[voxel[offset] + 1].y = value;

                if (value < blob[iBlob].yMin) {
                    blob[iBlob].yMin = value;
                }
                if (value > blob[iBlob].yMax) {
                    blob[iBlob].yMax = value;
                }
            }
        } // toCompute

        // Propagate to neightbors : use of Metaballs.neighborsTable 
        byte neighborVoxel = MetaballsTable.neightborVoxel[squareIndex];
        if (x < resx - 2 && (neighborVoxel & (1 << 0)) > 0) {
            computeEdgeVertex(iBlob, x + 1, y);
        }
        if (x > 0 && (neighborVoxel & (1 << 1)) > 0) {
            computeEdgeVertex(iBlob, x - 1, y);
        }
        if (y < resy - 2 && (neighborVoxel & (1 << 2)) > 0) {
            computeEdgeVertex(iBlob, x, y + 1);
        }
        if (y > 0 && (neighborVoxel & (1 << 3)) > 0) {
            computeEdgeVertex(iBlob, x, y - 1);
        }
    }

    /**
     * filterBlob
     */
//    public boolean acceptBlob(Blob b) {
//        if ((b.w * imgWidth >= blobWidthMin) || (b.h * imgHeight >= blobHeightMin))
//            return true;
//        return false;
//    }

    /**
     * Make a call to this function to allow BlobDetection to fire an event each
     * time a blob has been found by calling the function boolean
     * newBlobDetectedEvent (Blob b), which has to be implemented in parent
     * object passed to the BlobDetection. Inside the callback, a boolean value
     * has to be returned: true means 'keep this blob', false means 'discard
     * this blob'.
     */
    public void activeCustomFilter(Object parent) {
        this.parent = parent;
        try {
            filterBlobMethod = parent.getClass().getMethod("newBlobDetectedEvent", Blob.class);
            //System.out.println("newBlobDetectedEvent found!");
        } catch (Exception e) {
            //System.out.println("no such method or error");
            // no such method, or an error.. which is fine, just ignore
        }
    }
}

/* */
