/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.apache.batik.transcoder.wmf.tosvg;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringJoiner;


/**
 * This class holds simple properties about a WMF Metafile. It can be used
 * whenever general information must be retrieved about this file.
 * <p>
 * the original WTF doesn't have constructor for stream.
 *
 * @version $Id$
 */
public class WMFHeaderProperties2 extends WMFHeaderProperties {

    /**
     * Creates a new WMFHeaderProperties, and sets the associated WMF File.
     *
     * @param wmfin the WMF Metafile
     */
    public WMFHeaderProperties2(InputStream wmfin) throws IOException {
        reset();
        stream = new DataInputStream(new BufferedInputStream(wmfin));
        read(stream);
        stream.close();
    }

    /**
     * Creates the properties associated file.
     */
    public void setInputStream(InputStream wmfin) throws IOException {
        stream = new DataInputStream(new BufferedInputStream(wmfin));
        read(stream);
        stream.close();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", WMFHeaderProperties2.class.getSimpleName() + "[", "]")
                .add("left=" + left)
                .add("right=" + right)
                .add("top=" + top)
                .add("bottom=" + bottom)
                .add("width=" + width)
                .add("height=" + height)
                .add("inch=" + inch)
                .add("scaleX=" + scaleX)
                .add("scaleY=" + scaleY)
                .add("scaleXY=" + scaleXY)
                .add("vpW=" + vpW)
                .add("vpH=" + vpH)
                .add("vpX=" + vpX)
                .add("vpY=" + vpY)
                .add("xSign=" + xSign)
                .add("ySign=" + ySign)
                .add("bReading=" + bReading)
                .add("isAldus=" + isAldus)
                .add("isotropic=" + isotropic)
                .add("mtType=" + mtType)
                .add("mtHeaderSize=" + mtHeaderSize)
                .add("mtVersion=" + mtVersion)
                .add("mtSize=" + mtSize)
                .add("mtNoObjects=" + mtNoObjects)
                .add("mtMaxRecord=" + mtMaxRecord)
                .add("mtNoParameters=" + mtNoParameters)
                .add("windowWidth=" + windowWidth)
                .add("windowHeight=" + windowHeight)
                .add("numObjects=" + numObjects)
                .toString();
    }
}
