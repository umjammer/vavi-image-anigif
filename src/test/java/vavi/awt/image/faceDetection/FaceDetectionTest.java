/*
 * face detection
 *
 * NI-Lab.
 */

package vavi.awt.image.faceDetection;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

import vavi.awt.image.blobDetection.BlobDetection;


/**
 * FaceDetectionTest.
 */
public class FaceDetectionTest {

    /** */
    public static void main(String[] args) throws Exception {

        // The image 'warai.png' from
        //   「顔面認識→笑い男貼り付け」ツールを Ruby で書く - にっき (2007-03-12)
        //   http://elpeo.jp/diary/20070312.html#p03

        Path out1 = Paths.get("tmp/bd_out1.jpg");
        Path out2 = Paths.get("tmp/bd_out2.jpg");
        Path out3 = Paths.get("tmp/bd_out3.jpg");
        Path out4 = Paths.get("tmp/bd_out4.jpg");

        BufferedImage laughingManImage = ImageIO.read(FaceDetectionTest.class.getResourceAsStream("/warai.png"));

        BufferedImage b1 = ImageIO.read(FaceDetectionTest.class.getResourceAsStream("/404.jpg"));

        // 肌色部分を白色とする白黒2値画像を生成
        BufferedImage b2 = new FleshDetectOp().filter(b1, null);
        ImageIO.write(b2, "jpg", Files.newOutputStream(out1));

        // 白色部分を膨張化
        BufferedImage b3 = new MorphOp(3).filter(b2, null); // 閾値周辺5ピクセル
        ImageIO.write(b3, "jpg", Files.newOutputStream(out2));

        // BlobDetect にて肌色部分を取得
        BlobDetection bd = FleshDetector.detectWhite(b3);

        // 肌色認識部分を図示
        BufferedImage b4 = new FleshEffectOp(bd, true, false).filter(b1, null);
        ImageIO.write(b4, "jpg", Files.newOutputStream(out3));

        // 肌色認識部分に笑い男貼り付け(The Laughing Man Hacks Them!)
        BufferedImage b5 = new FleshReplaceOp(bd, laughingManImage).filter(b1, null);
        ImageIO.write(b5, "jpg", Files.newOutputStream(out4));
    }
}
