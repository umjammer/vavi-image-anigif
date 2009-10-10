/*
 * face detection
 * 
 * NI-Lab.
 */

package vavi.awt.image.faceDetection;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;


/**
 * �摜���H�B
 * 
 * @see "http://www.nilab.info/zurazure2/000603.html"
 */
public class MorphOp extends BaseOp {

    /** */
    private int limit;

    /**
     * @param limit neighbors
     */
    public MorphOp(int limit) {
        this.limit = limit;
    }

    /**
     * ����������c��������摜����(Dilation)��������B
     * 
     * @param src binary image (white and black)
     * @param dst when null, created by {@link #createCompatibleDestImage(BufferedImage, ColorModel)} 
     * @return binary image (white and black)
     * @see "Visual C++ 6.0��p�����Ղ����摜���� (14) -- �ԐF�𒊏o���A�g�������k -- http://homepage3.nifty.com/ishidate/vcpp6_g14/vcpp6_g14.htm"    
     */
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
        }
        int w = src.getWidth();
        int h = src.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!isOn(src, x, y)) {
                    // �Ώۂ̃s�N�Z�����ӂ̔����s�N�Z���̌���limit�ȏ�ł���΁A
                    // �Ώۃs�N�Z���𔒂ɂ���
                    if (isOnByNeighbors(src, x, y, limit)) {
                        dst.setRGB(x, y, Color.WHITE.getRGB());
                    } else {
                        dst.setRGB(x, y, Color.BLACK.getRGB());
                    }
                } else {
                    dst.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        
        return dst;
    }

    /** ��f�̎��Ӊ�f�̔��̐��� �������l�ȏ�ɂȂ����� true ��Ԃ� */
    private boolean isOnByNeighbors(BufferedImage image, int x, int y, int limit) {
        int w = image.getWidth();
        int h = image.getHeight();
        // check the neighboring pixels for (x,y)
        int num = 0;
        for (int yy = y - 1; yy <= y + 1; yy++) {
            for (int xx = x - 1; xx <= x + 1; xx++) {
                if (0 <= xx && xx < w && 0 <= yy && yy < h) {
                    if (isOn(image, xx, yy)) {
                        num++;
                        if (num >= limit) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** ��f�����̏ꍇ�� true ��Ԃ� */
    private boolean isOn(BufferedImage image, int x, int y) {
        return Color.WHITE.equals(new Color(image.getRGB(x, y)));
    }
}

/* */
