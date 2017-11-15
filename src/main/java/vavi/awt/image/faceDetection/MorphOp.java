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
 * 画像加工。
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
     * 白い部分を膨張させる画像処理(Dilation)をかける。
     *
     * @param src binary image (white and black)
     * @param dst when null, created by {@link #createCompatibleDestImage(BufferedImage, ColorModel)}
     * @return binary image (white and black)
     * @see "Visual C++ 6.0を用いた易しい画像処理 (14) -- 赤色を抽出し、拡張し収縮 -- http://homepage3.nifty.com/ishidate/vcpp6_g14/vcpp6_g14.htm"
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
                    // 対象のピクセル周辺の白いピクセルの個数がlimit以上であれば、
                    // 対象ピクセルを白にする
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

    /** 画素の周辺画素の白の数が しきい値以上になったら true を返す */
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

    /** 画素が白の場合に true を返す */
    private boolean isOn(BufferedImage image, int x, int y) {
        return Color.WHITE.equals(new Color(image.getRGB(x, y)));
    }
}

/* */
