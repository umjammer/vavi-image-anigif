/*
 * Copyright (C)2005 by もＱ. All rights reserved.
 *
 * Created on 2005/07/20
 */

import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import com.rakugakichat.gif.DisposalMethod;
import com.rakugakichat.gif.GifAnimationEncoder;
import com.rakugakichat.gif.GifAnimationFrame;


/**
 * GifAnimationEncoder サンプルソース3
 *      GifAnimationFrameオブジェクトを使っています。
 *      飛行機のイメージのみを重ねているのでファイルサイズが小さくなります。
 * E-mail: h-aiura@bd5.so-net.ne.jp
 * URL: http://www001.upp.so-net.ne.jp/h-aiura
 *      http://www.rakugakichat.com
 */
public class Sample3 {

    public static void main(String[] args) throws Exception {

        // オブジェクトを生成
        GifAnimationEncoder gifimage = new GifAnimationEncoder(400, 300);

        // ループ回数は無限大
        gifimage.setLoopNumber(0);

            // 背景をセット
        GifAnimationFrame baseFrame = new GifAnimationFrame(ImageIO.read(new File("." + File.separator + "Images" + File.separator + "back.gif")));

        // 表示時間は1秒
        baseFrame.setDelayTime(100);

        // 画像を残す
        baseFrame.setDisposalMethod(DisposalMethod.DoNotDispose);

        // イメージをセット
        gifimage.addImage(baseFrame);

        // 飛行機を順に書き込む
        Image plane = ImageIO.read(new File("." + File.separator + "Images" + File.separator + "plane.gif"));
        for (int i = 350; i >= 0; i -= 50) {
            GifAnimationFrame overFrame = new GifAnimationFrame(plane);
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

        //	エンコードする
        gifimage.encode(new FileOutputStream(new File("." + File.separator + "Images" + File.separator + "animationSample3.gif")));
    }
}
