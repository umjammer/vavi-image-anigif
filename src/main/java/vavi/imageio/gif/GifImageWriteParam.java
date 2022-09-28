/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import javax.imageio.ImageWriteParam;

import vavi.awt.image.gif.GifEncoder.DisposalMethod;


/**
 * GifImageWriteParam.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070725 nsano initial version <br>
 */
public class GifImageWriteParam extends ImageWriteParam {

    /** */
    public GifImageWriteParam() {
        canWriteProgressive = true;
        progressiveMode = MODE_DEFAULT;
    }

    /** in [10 msec] */
    private int delayTime;

    /** in [10 msec] */
    public int getDelayTime() {
        return delayTime;
    }

    /** */
    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    /** */
    private DisposalMethod disposalMethod;

    /** */
    public DisposalMethod getDisposalMethod() {
        return disposalMethod;
    }

    /** TODO don't use {@link GifImageWriteParam#disposalMethod} */
    public void setDisposalMethod(DisposalMethod disposalMethod) {
        this.disposalMethod = disposalMethod;
    }
}

/* */
