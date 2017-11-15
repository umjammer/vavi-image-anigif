/*
 * face detection
 *
 * NI-Lab.
 */

package vavi.awt.image.faceDetection;

import java.awt.Color;
import java.awt.image.BufferedImage;


/**
 * FleshDetectOp.
 *
 * @see "http://www.nilab.info/zurazure2/000603.html"
 */
public class FleshDetectOp extends BaseOp {

    /**
     * 肌色部分を白とした白黒2値画像を返します。
     *
     * @param src 写真等のカラー画像
     * @return 肌色部分を白とした白黒2値画像
     */
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
        }

        for (int x = 0; x < src.getWidth(); x++) {
            for (int y = 0; y < src.getHeight(); y++) {
                Color c = new Color(src.getRGB(x, y));
                if (isFlesh(c)) {
                    dst.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    dst.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        return dst;
    }

    /** */
    private boolean isFlesh(Color c) {
        Yuv yuv = new Yuv(c.getRed(), c.getGreen(), c.getBlue());
        if ((c.getRed() > 40) &&
            (c.getGreen() > 40) &&
            (yuv.y + 16 > 145) &&
            (yuv.v + 128 < 173) &&
            (yuv.v + 128 > 133) &&
            (yuv.u + 128 < 127) &&
            (yuv.u + 128 > 77)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * YUV (YCrCb) 表色系。
     * @see YUVフォーマット及び YUVとRGBの変換
     *    "http://vision.kuee.kyoto-u.ac.jp/~hiroaki/firewire/yuv.html"
     * @see "C MAGAZINE 2003.5 「画像処理を極めるアルゴリズムラボ 第44回 色の表現」"
     */
    private static class Yuv {
        final double y;
        final double u;
        final double v;
        /** */
        public Yuv(int r, int g, int b) {
            y = (+(0.2989 * r) + (0.5866 * g) + (0.1145 * b)); // 0〜255
            u = (-(0.1687 * r) - (0.3312 * g) + (0.5000 * b)); // -128〜127
            v = (+(0.5000 * r) - (0.4183 * g) - (0.0816 * b)); // -128〜127
        }
    }
}

/* */
