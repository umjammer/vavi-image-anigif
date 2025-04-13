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
    final Point old = new Point();
    /** */
    final Dimension logExt = new Dimension();
    /** */
    final Point logOrg = new Point();
    /** display */
    final Dimension devExt;
    /** */
    Color penColor = Color.black;
    /** */
    Color textColor = Color.black;
    /** */
    final int inch;
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
//logger.log(Level.TRACE,"(" + devExt.width + ", " + devExt.height + ")");
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
    private final List<Integer> handles = new ArrayList<>();

    /** */
    private final Map<Integer, MetaRecord> metaRecords = new HashMap<>();

    /** */
    public MetaRecord selectObject(int index) {
//logger.log(Level.TRACE,"H: select: index: " + index);
        int handle = handles.get(index);
//logger.log(Level.TRACE,"M: get: key: " + handle);
        return metaRecords.get(handle);
    }

    /** */
    public void deleteObject(int index) {
//logger.log(Level.TRACE,"M: delete: key: " + index);
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
//logger.log(Level.TRACE,"H: replace: index: " + index + ", value: " + h);
            handles.set(index, h);
        } else {
//logger.log(Level.TRACE,"H: insert: index: " + handles.size() + ", value: " + h);
            handles.add(h);
        }

//logger.log(Level.TRACE,"M: put: key: " + h + ", value: " + metaRecode.hashCode());
        metaRecords.put(h, metaRecode);
    }
}
