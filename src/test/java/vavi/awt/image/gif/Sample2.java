/*
 * Copyright (c) ${YEAR} by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.gif;

import java.awt.Image;
import java.io.File;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import vavi.awt.image.gif.GifAnimationEncoder;
import vavi.awt.image.gif.GifAnimationEncoder.GifFrame;
import vavi.awt.image.gif.GifEncoder.DisposalMethod;


/**
 * Sample2.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070619 nsano initial version <br>
 */
public class Sample2 {

    /**
     * @param args none
     */
    public static void main(String[] args) throws Exception {

        // オブジェクトを生成
        GifAnimationEncoder gifimage = new GifAnimationEncoder(400, 300);

        // ループ回数は無限大
        gifimage.setLoopNumber(0);

        // 背景をセット
        GifFrame baseFrame = new GifFrame(ImageIO.read(new File("Images", "back.gif")));

        // 表示時間は1秒
        baseFrame.setDelayTime(100);

        // 画像を残す
        baseFrame.setDisposalMethod(DisposalMethod.DoNotDispose);

        // イメージをセット
        gifimage.addImage(baseFrame);

        // 飛行機を順に書き込む
        Image plane = ImageIO.read(new File("Images", "plane.gif"));
        for (int i = 350; i >= 0; i -= 50) {
            GifFrame overFrame = new GifFrame(plane);
            // 表示時間は0.5秒
            overFrame.setDelayTime(50);

            // 表示後は前の画像を回復（ということは最初のイメージにかぶせて表示することになる）
            overFrame.setDisposalMethod(DisposalMethod.RestoreToPrevious);

            // 表示位置をセット
            overFrame.setImageLeftPosition(i);
            overFrame.setImageTopPosition(20);

            // イメージをセット
            gifimage.addImage(overFrame);
        }

        // encode
        gifimage.encode(Files.newOutputStream(new File("Images", "animationSample2.gif").toPath()));
    }
}
