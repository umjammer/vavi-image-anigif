/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import vavi.awt.image.gif.GifAnimationEncoder;


/**
 * Sample1
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070619 nsano initial version <br>
 */
public class Sample1 {

	public static void main(String[] args) throws Exception {

		//	オブジェクトを生成
		GifAnimationEncoder encoder = new GifAnimationEncoder(80,80);
		
		//	ループ回数は無限大
		encoder.setLoopNumber(0);

		//	1コマあたりの表示時間は1秒
		encoder.setDelay(100);

        // イメージを読み込む
        for (int i = 0; i < 5; i++) {
            File file = new File("Images", "image" + i + ".gif");
            encoder.addImage(ImageIO.read(file));
        }

        // エンコードする
        File file = new File("Images", "animationSample1.gif");
        encoder.encode(new FileOutputStream(file));
	}
}
