/*
 * Blob Detection library
 *
 * v3ga
 */

package vavi.awt.image.blobDetection;


/**
 * This class is used by class BlobDetection to store points'coordinates when
 * detecting edges of a blob. Its dimensions are normalized.
 *
 * @see "http://www.v3ga.net/processing/BlobDetection/"
 */
public class EdgeVertex {
    /** Normalized coordinates of the vertex (range [0;1]) */
    public float x, y;

    /** */
    public EdgeVertex(float x, float y) {
        this.x = x;
        this.y = y;
    }
}

/* */
