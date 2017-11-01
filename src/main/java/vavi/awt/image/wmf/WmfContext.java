/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.wmf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vavi.awt.image.wmf.WindowsMetafile.MetaRecord;


/**
 * WmfContext.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070629 nsano initial version <br>
 */
class WmfContext {
    /** */
    int recordIndex = 0;
    /** */
    boolean drawFilled = false;
    /** last point */
    Point old = new Point();
    /** */
    Dimension logExt = new Dimension();
    /** */
    Point logOrg = new Point();
    /** display */
    Dimension devExt;
    /** */
    Color penColor = Color.black;
    /** */
    Color textColor = Color.black;
    /** */
    int inch;
    /** */
    int numRectangles;
    /** */
    int numOvals;
    /** */
    int numLines;

    /**
     * {@link #devExt} will be set.
     */
    WmfContext(Dimension size, int inch) {
        this.devExt = size;
        this.inch = inch;
//System.err.println("(" + devExt.width + ", " + devExt.height + ")");
    }

    /** */
    public int mapX(int x) {
        float dX = (float) devExt.width / logExt.width;
        x = x - logOrg.x;
        x = (int) (x * dX);
        return x;
    }

    /** */
    public int mapY(int y) {
        float dY = (float) devExt.height / logExt.height;
        y = y - logOrg.y;
        y = (int) (y * dY);
        return y;
    }

    /** */
    private List<Integer> handles = new ArrayList<>();

    /** */
    private Map<Integer, MetaRecord> metaRecords = new HashMap<>();

    /** */
    public MetaRecord selectObject(int index) {
//System.err.println("H: select: index: " + index);
        int handle = handles.get(index);
//System.err.println("M: get: key: " + handle);
        return metaRecords.get(handle);
    }

    /** */
    public void deleteObject(int index) {
//System.err.println("M: delete: key: " + index);
        handles.set(index, -1);
        metaRecords.remove(index);
    }

    /** */
    public void addObject(int recordValue, MetaRecord metaRecode) {
        int h = recordValue;
        int i = -1;

        // if there is a free handle due to delete
        if (handles.contains(i)) {
            // get the index of the deleted record
            int index = handles.indexOf(i);
            // set the new value
//System.err.println("H: replace: index: " + index + ", value: " + h);
            handles.set(index, h);
        } else {
//System.err.println("H: insert: index: " + handles.size() + ", value: " + h);
            handles.add(h);
        }

//System.err.println("M: put: key: " + h + ", value: " + metaRecode.hashCode());
        metaRecords.put(h, metaRecode);
    }
}

/* */
