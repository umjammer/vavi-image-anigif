/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.svg;

import java.awt.Dimension;
import java.util.logging.Level;
import javax.imageio.ImageReadParam;

import vavi.util.Debug;


/**
 * BatikSvgImageReadParam. 
 * <p>
 * system property
 * <li>"vavi.imageio.svg.BatikSvgImageReadParam.size" ... size e.g "400x400"</li>
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2009/06/20 nsano initial version <br>
 */
public class BatikSvgImageReadParam extends ImageReadParam {

    {
        canSetSourceRenderSize = true;

        String size = System.getProperty("vavi.imageio.svg.BatikSvgImageReadParam.size");
        while (size != null) { // bad while usage
            String[] ss = size.split("x");
            try {
                if (ss.length == 2) {
                    int w = Integer.parseInt(ss[0]);
                    int h = Integer.parseInt(ss[1]);
                    setSourceRenderSize(new Dimension(w, h));
Debug.println(Level.FINE, "size: " + w + "x" + h);
                    break; // TODO how to write smartly
                }
            } catch (NumberFormatException e) {
            }
Debug.println(Level.INFO, "wrong syntax: " + size);
        }
    }
}
