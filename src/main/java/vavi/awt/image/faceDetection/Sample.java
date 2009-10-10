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
        //   �u��ʔF�����΂��j�\��t���v�c�[���� Ruby �ŏ��� - �ɂ��� (2007-03-12)
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

            // ���F�����𔒐F�Ƃ��锒��2�l�摜�𐶐�
            BufferedImage b2 = new FleshDetectOp().filter(b1, null);
            ImageIO.write(b2, "jpeg", new File(f2));

            // ���F������c����
            BufferedImage b3 = new MorphOp(3).filter(b2, null); // 臒l����5�s�N�Z��
            ImageIO.write(b3, "jpeg", new File(f3));

            // BlobDetect �ɂĔ��F�������擾
            BlobDetection bd = FleshDetector.detectWhite(b3);

            // ���F�F��������}��
            BufferedImage b4 = new FleshEffectOp(bd, true, false).filter(b1, null);
            ImageIO.write(b4, "jpeg", new File(f4));

            // ���F�F�������ɏ΂��j�\��t��(The Laughing Man Hacks Them!)
            BufferedImage b5 = new FleshReplaceOp(bd, laughingManImage).filter(b1, null);
            ImageIO.write(b5, "jpeg", new File(f5));
        }
    }
}

/* */
