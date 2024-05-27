/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.wmf;

import javax.imageio.ImageReadParam;


/**
 * BatikWmfImageReadParam.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 220926 nsano initial version <br>
 */
public class BatikWmfImageReadParam extends ImageReadParam {

    public static final int DEFAULT_WIDTH = 400;
    public static final int DEFAULT_HEIGHT = 400;

    {
        canSetSourceRenderSize = true;
    }
}
