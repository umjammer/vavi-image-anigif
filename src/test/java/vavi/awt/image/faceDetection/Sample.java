/*
 * face detection
 * 
 * NI-Lab.
 */

package vavi.awt.image.faceDetection;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import vavi.awt.image.blobDetection.BlobDetection;


/**
 * Sample. 
 */
public class Sample {

    /** */
    public static void main(String[] args) throws Exception {

        // The image 'warai.png' from  
        //   「顔面認識→笑い男貼り付け」ツールを Ruby で書く - にっき (2007-03-12)
        //   http://elpeo.jp/diary/20070312.html#p03
        final String base = "sample/Images/";
        BufferedImage laughingManImage = ImageIO.read(new File(base + "warai.png"));

        for (int i = 15; i <= 15; i++) {

            String f1 = base + "sample" + i + ".jpg";
            String f2 = base + "sample" + i + "_2.jpg";
            String f3 = base + "sample" + i + "_3.jpg";
            String f4 = base + "sample" + i + "_4.jpg";
            String f5 = base + "sample" + i + "_5.jpg";

            BufferedImage b1 = ImageIO.read(new File(f1));

            // 肌色部分を白色とする白黒2値画像を生成
            BufferedImage b2 = new FleshDetectOp().filter(b1, null);
            ImageIO.write(b2, "jpeg", new File(f2));

            // 白色部分を膨張化
            BufferedImage b3 = new MorphOp(3).filter(b2, null); // 閾値周辺5ピクセル
            ImageIO.write(b3, "jpeg", new File(f3));

            // BlobDetect にて肌色部分を取得
            BlobDetection bd = FleshDetector.detectWhite(b3);

            // 肌色認識部分を図示
            BufferedImage b4 = new FleshEffectOp(bd, true, false).filter(b1, null);
            ImageIO.write(b4, "jpeg", new File(f4));

            // 肌色認識部分に笑い男貼り付け(The Laughing Man Hacks Them!)
            BufferedImage b5 = new FleshReplaceOp(bd, laughingManImage).filter(b1, null);
            ImageIO.write(b5, "jpeg", new File(f5));
        }
    }
}

/* */
