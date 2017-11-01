/*
 * Copyright (c) 2005 by もＱ. All rights reserved.
 *
 * Created on 2005/07/20
 */

package vavi.awt.image.gif;

import java.awt.Image;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import vavi.awt.image.gif.GifEncoder.DisposalMethod;
import vavi.io.LittleEndianDataOutputStream;


/**
 * GifAnimationEncoder
 * gif animation を生成するクラスです。
 * 
 * @author <a href="mailto:h-aiura@bd5.so-net.ne.jp">mo_q</a>
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 1.0 2005/07/20
 * @see "http://www001.upp.so-net.ne.jp/h-aiura"
 * @see "http://www.rakugakichat.com"
 */
public class GifAnimationEncoder {
    /** gifImageFrameを入れる可変配列 */
    private List<GifFrame> frames = new ArrayList<>();

    /** */
    private int delay = 0;

    /** */
    private DisposalMethod disposalMethod = DisposalMethod.DoNotDispose;

    /** */
    private int loopNumber = 0;

    /** */
    private int width = -1;

    /** */
    private int height = -1;

    /**
     * 指定サイズのGifAnimationEncoderオブジェクトを構築します。
     * あらかじめ、ループ回数は無限回、
     * １フレームあたりの表示秒数は0秒、
     * DisposalMethodは無指定に設定されます。
     */
    public GifAnimationEncoder() {
    }

    /**
     * 指定サイズのGifAnimationEncoderオブジェクトを構築します。
     * あらかじめ、ループ回数は無限回、
     * １フレームあたりの表示秒数は0秒、
     * DisposalMethodは無指定に設定されます。
     * 
     * @param width イメージの幅
     * @param height イメージの高さ
     */
    public GifAnimationEncoder(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * エンコードをし、結果をOutputStreamに出力します。
     * 
     * @param os OutputStream
     * @throws IOException 出力エラー
     * @throws IllegalStateException イメージが存在しないとき
     */
    public void encode(OutputStream os) throws IOException {

        // イメージがない場合はエラー
        if (frames.size() < 1) {
            throw new IllegalStateException("no image");
        }

        @SuppressWarnings("resource")
        LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(os);

        // gifデータを書き込む
        // ここから先はJef PoskanzerさんのGifEncoderのソースを参考にしました。
        // Write the Magic header
        dos.writeBytes("GIF89a");

        // Write out the screen width and height
        dos.writeShort(width);
        dos.writeShort(height);

        // グローバルカラーはなし
        dos.writeByte(0x00);

        // Write out the Background colour
        // グローバルカラーはないのでBackground colourの指定はしない
        dos.writeByte(0);

        // Pixel aspect ratio - 1:1.
        // Putbyte( (byte) 49, outs );
        // Java's GIF reader currently has a bug, if the aspect ratio byte is
        // not zero it throws an ImageFormatException. It doesn't know that
        // 49 means a 1:1 aspect ratio. Well, whatever, zero works with all
        // the other decoders I've tried so it probably doesn't hurt.
        dos.writeByte(0);

        // 繰り返しの制御を行う
        if (loopNumber != 1) {
            dos.writeByte('!');
            dos.writeByte(0xff);
            dos.writeByte(11);
            dos.writeBytes("NETSCAPE2.0");
            dos.writeByte(3);
            dos.writeByte(1);
            if (loopNumber > 1) {
                dos.writeByte(0xff & (loopNumber - 1));
                dos.writeByte(0xff & ((loopNumber - 1) >> 8));
            } else {
                dos.writeByte(0);
                dos.writeByte(0);
            }
            dos.writeByte(0);
        }

        // イメージをくっつける
        for (int i = 0; i < frames.size(); i++) {
            GifFrame frame = frames.get(i);

            GifEncoder encoder = null;
            if (frame.getImage() != null) {
                encoder = new GifEncoder(frame.getImage(), os, frame.isInterlace());
            } else if (frame.getProducer() != null) {
                encoder = new GifEncoder(frame.getProducer(), os, frame.isInterlace());
            }
            encoder.setDrawX(frame.getImageLeftPosition());
            encoder.setDrawY(frame.getImageTopPosition());
            encoder.setDelayTime(frame.getDelayTime());
            encoder.setDisposalMethod(frame.getDisposalMethod());
            encoder.encode();
        }

        // Write the GIF file terminator
        dos.writeByte(';');
    }

    /**
     * １フレームあたりの表示秒数(1/100秒単位)の規定値を設定します。<br>
     * 
     * @param delay １フレームあたりの表示秒数
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * ループする回数を指定します。<BR>
     * 
     * @param num ループ回数,0は無制限<br>
     *            一部のブラウザ(Operaなど)では、引数を2以上にした場合にループ回数が(引数-1)になることがあります。
     */
    public void setLoopNumber(int num) {
        loopNumber = num;
    }

    /**
     * イメージの高さを取得します。
     * 
     * @return イメージの高さ
     */
    public int getHeight() {
        return height;
    }

    /**
     * イメージの高さを設定します。
     * 
     * @param height イメージの高さ
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * イメージの幅を取得します。
     * 
     * @return Returns the width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * イメージの幅を設定します。
     * 
     * @param width width.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Disposal method(イメージの重ね方)を設定します。 インターフェースDisposalMethodの定数で指定してください。
     * 
     * @param disposalMethod Disposal method(イメージの重ね方)
     * @see DisposalMethod
     */
    public void setDisposalMethod(DisposalMethod disposalMethod) throws IndexOutOfBoundsException {
        this.disposalMethod = disposalMethod;
    }

    /**
     * エンコードするイメージを追加します。<br>
     * 追加した順にイメージが表示されます。<br>
     * 
     * @param aniFrame GifAnimationFrameオブジェクト
     */
    public void addImage(GifFrame aniFrame) {
        frames.add(aniFrame);
    }

    /**
     * エンコードするイメージを追加します。<br>
     * 追加した順にイメージが表示されます。<br>
     * 1フレームあたりの表示秒数とDisposal
     * methodは、setDefaultDelayメソッド,setDefaultDisposalMethodメソッドでの設定値が適用されます。
     * 
     * @param image Imageオブジェクト
     * @see #setDelay(int)
     * @see #setDisposalMethod(int)
     */
    public void addImage(Image image) {
        frames.add(new GifFrame(image, delay, disposalMethod));
    }

    /**
     * エンコードするイメージを追加します。<br>
     * 追加した順にイメージが表示されます。<br>
     * 1フレームあたりの表示秒数とDisposal
     * methodは、setDefaultDelayメソッド,setDefaultDisposalMethodメソッドでの設定値が適用されます。
     * 
     * @param producer ImageProducerオブジェクト
     * @see #setDelay(int)
     * @see #setDisposalMethod(int)
     */
    public void addImage(ImageProducer producer) {
        frames.add(new GifFrame(producer, delay, disposalMethod));
    }

    /**
     * 指定のインデックスにエンコードするイメージを追加します。<br>
     * インデックス順にイメージが表示されます。<br>
     * 追加した順にイメージが表示されます。<br>
     * 
     * @param index インデックス番号
     * @param aniFrame GifAnimationFrameオブジェクト
     */
    public void addImage(int index, GifFrame aniFrame) {
        frames.add(index, aniFrame);
    }

    /**
     * 指定のインデックスにエンコードするイメージを追加します。<br>
     * インデックス順にイメージが表示されます。<br>
     * 追加した順にイメージが表示されます。<br>
     * 1フレームあたりの表示秒数とDisposal
     * methodは、setDefaultDelayメソッド,setDefaultDisposalMethodメソッドでの設定値が適用されます。
     * 
     * @param index インデックス番号
     * @param image Imageオブジェクト
     * @see #setDelay(int)
     * @see #setDisposalMethod(int)
     */
    public void addImage(int index, Image image) {
        frames.add(index, new GifFrame(image, delay, disposalMethod));
    }

    /**
     * 指定のインデックスにエンコードするイメージを追加します。<br>
     * インデックス順にイメージが表示されます。<br>
     * 追加した順にイメージが表示されます。<br>
     * 1フレームあたりの表示秒数とDisposal
     * methodは、setDefaultDelayメソッド,setDefaultDisposalMethodメソッドでの設定値が適用されます。
     * 
     * @param index インデックス番号
     * @param producer ImageProducerオブジェクト
     * @see #setDelay(int)
     * @see #setDisposalMethod(int)
     */
    public void addImage(int index, ImageProducer producer) {
        frames.add(index, new GifFrame(producer, delay, disposalMethod));
    }

    /**
     * エンコードするイメージを追加します。<br>
     * 追加した順にイメージが表示されます。<br>
     * 第二引数で指定した秒数(1/100秒単位)表示したのち次のイメージに移ります。<br>
     * Disposal methodは、setDefaultDisposalMethodメソッドでの設定値が適用されます。
     * 
     * @param image Imageオブジェクト
     * @param delayTime 表示時間(1/100秒単位)
     * @see #setDisposalMethod(int)
     */
    public void addImage(Image image, int delayTime) {
        frames.add(new GifFrame(image, delayTime, disposalMethod));
    }

    /**
     * エンコードするイメージを追加します。<br>
     * 追加した順にイメージが表示されます。<br>
     * 第二引数で指定した秒数(1/100秒単位)表示したのち次のイメージに移ります。<br>
     * Disposal methodは、setDefaultDisposalMethodメソッドでの設定値が適用されます。
     * 
     * @param producer ImageProducerオブジェクト
     * @param delayTime 表示時間(1/100秒単位)
     * @see #setDisposalMethod(int)
     */
    public void addImage(ImageProducer producer, int delayTime) {
        frames.add(new GifFrame(producer, delayTime, disposalMethod));
    }

    /**
     * 指定のインデックスにエンコードするイメージを追加します。<br>
     * インデックス順にイメージが表示されます。<br>
     * 第二引数で指定した秒数(1/100秒単位)表示したのち次のイメージに移ります。<br>
     * Disposal methodは、setDefaultDisposalMethodメソッドでの設定値が適用されます。
     * 
     * @param index インデックス番号
     * @param image Imageオブジェクト
     * @param delayTime 表示時間(1/100秒単位)
     * @see #setDisposalMethod(int)
     */
    public void addImage(int index, Image image, int delayTime) {
        frames.add(index, new GifFrame(image, delayTime, disposalMethod));
    }

    /**
     * 指定のインデックスにエンコードするイメージを追加します。<br>
     * インデックス順にイメージが表示されます。<br>
     * 第二引数で指定した秒数(1/100秒単位)表示したのち次のイメージに移ります。<br>
     * Disposal methodは、setDefaultDisposalMethodメソッドでの設定値が適用されます。
     * 
     * @param index インデックス番号
     * @param producer ImageProducerオブジェクト
     * @param delayTime 表示時間(1/100秒単位)
     * @see #setDisposalMethod(int)
     */
    public void addImage(int index, ImageProducer producer, int delayTime) {
        frames.add(index, new GifFrame(producer, delayTime, disposalMethod));
    }

    /**
     * 指定のインデックスにあるイメージをエンコードするイメージの一覧から削除します。
     * 
     * @param index インデックス番号
     * @return 削除したGifAnimationFrameオブジェクト
     */
    public Object removeImage(int index) {
        Object result = null;
        GifFrame frame = frames.remove(index);
        if (frame.getImage() != null) {
            result = frame.getImage();
        } else if (frame.getProducer() != null) {
            result = frame.getProducer();
        }
        return result;
    }

    /**
     * エンコードするイメージの枚数を返します。
     * 
     * @return エンコードするイメージの枚数
     */
    public int getImageSize() {
        return frames.size();
    }

    /**
     * gif animation のフレームのイメージと設定値を格納するオブジェクトです。<br>
     * このオブジェクトにより1フレーム毎の表示位置、インターレス処理の有無、
     * 表示秒数、disposal Method(イメージの重ね方)の設定ができます。
     * 
     * @author aiura
     */
    public static class GifFrame {
        /** */
        private Image image;

        /** */
        private ImageProducer producer;

        /** */
        private boolean interlace = false;

        /** */
        private int delayTime = 0;

        /** */
        private DisposalMethod disposalMethod = DisposalMethod.Unspecified;

        /** 描画位置 x */
        private int imageLeftPosition = 0;

        /** 描画位置 y */
        private int imageTopPosition = 0;

        /**
         * ImageオブジェクトからGifAnimationFrameオブジェクトを構築します。<br>
         * あらかじめ、表示位置は(0,0)、インターレスは無し、表示秒数は0秒、 disposal Methodは無指定に設定されます。
         * 
         * @param image Imageオブジェクト
         */
        public GifFrame(Image image) {
            this.image = image;
        }

        /**
         * ImageProducerオブジェクトからGifAnimationFrameオブジェクトを構築します。<br>
         * あらかじめ、表示位置は(0,0)、インターレスは無し、表示秒数は0秒、 disposal Methodは無指定に設定されます。
         * 
         * @param producer ImageProducerオブジェクト
         */
        public GifFrame(ImageProducer producer) {
            this.producer = producer;
        }

        /**
         * ImageオブジェクトからGifAnimationFrameオブジェクトを構築します。<br>
         * 表示秒数とdisposal Methodは引数の値に 表示位置は(0,0)、インターレスは無しに設定されます。
         * 
         * @param image Imageオブジェクト
         * @param delayTime 1フレームあたりの表示秒数
         * @param disposalMethod disposalMethod
         */
        public GifFrame(Image image, int delayTime, DisposalMethod disposalMethod) {
            this.image = image;
            this.delayTime = delayTime;
            this.disposalMethod = disposalMethod;
        }

        /**
         * ImageProducerオブジェクトからGifAnimationFrameオブジェクトを構築します。<br>
         * 表示秒数とdisposal Methodは引数の値に 表示位置は(0,0)、インターレスは無しに設定されます。
         * 
         * @param producer ImageProducerオブジェクト
         * @param delayTime 1フレームあたりの表示秒数
         * @param disposalMethod disposalMethod
         */
        public GifFrame(ImageProducer producer, int delayTime, DisposalMethod disposalMethod) {
            this.producer = producer;
            this.delayTime = delayTime;
            this.disposalMethod = disposalMethod;
        }

        /**
         * ImageオブジェクトからGifAnimationFrameオブジェクトを構築します。<br>
         * 表示位置は(0,0)、インターレス、表示秒数、 disposal Methodは引数の値に設定されます。
         * 
         * @param image Imageオブジェクト
         * @param imageLeftPosition 左端からの表示位置
         * @param imageTopPosition 上端からの表示位置
         * @param interlace インターレス処理をおこなうか？(true:おこなう)
         * @param delayTime 1フレームあたりの表示秒数
         * @param disposalMethod disposalMethod
         */
        public GifFrame(Image image, int imageLeftPosition, int imageTopPosition, boolean interlace, int delayTime, DisposalMethod disposalMethod) {
            this.image = image;
            this.imageLeftPosition = imageLeftPosition;
            this.imageTopPosition = imageTopPosition;
            this.interlace = interlace;
            this.delayTime = delayTime;
            this.disposalMethod = disposalMethod;
        }

        /**
         * ImageオブジェクトからGifAnimationFrameオブジェクトを構築します。<br>
         * 表示位置は(0,0)、インターレス、表示秒数、 disposal Methodは引数の値に設定されます。
         * 
         * @param producer ImageProducerオブジェクト
         * @param imageLeftPosition 左端からの表示位置
         * @param imageTopPosition 上端からの表示位置
         * @param interlace インターレス処理をおこなうか？(true:おこなう)
         * @param delayTime 1フレームあたりの表示秒数
         * @param disposalMethod disposalMethod
         */
        public GifFrame(ImageProducer producer, int imageLeftPosition, int imageTopPosition, boolean interlace, int delayTime, DisposalMethod disposalMethod) {
            this.producer = producer;
            this.imageLeftPosition = imageLeftPosition;
            this.imageTopPosition = imageTopPosition;
            this.interlace = interlace;
            this.delayTime = delayTime;
            this.disposalMethod = disposalMethod;
        }

        /**
         * 1フレームあたりの表示秒数を取得します。
         * 
         * @return 1フレームあたりの表示秒数
         */
        public int getDelayTime() {
            return delayTime;
        }

        /**
         * 1フレームあたりの表示秒数を設定します。
         * 
         * @param delayTime 1フレームあたりの表示秒数
         */
        public void setDelayTime(int delayTime) {
            this.delayTime = delayTime;
        }

        /**
         * Disposal method(イメージの重ね方)を取得します。 戻り値はインターフェースDisposalMethodの定数を参照してください。
         * 
         * @return Disposal method
         * @see DisposalMethod
         */
        public DisposalMethod getDisposalMethod() {
            return disposalMethod;
        }

        /**
         * Disposal method(イメージの重ね方)を設定します。 インターフェースDisposalMethodの定数で指定してください。
         * 
         * @param disposalMethod Disposal method(イメージの重ね方)
         * @see DisposalMethod
         * @throws IndexOutOfBoundsException DisposalMethodの定数の範囲を超えた場合
         */
        public void setDisposalMethod(DisposalMethod disposalMethod) throws IndexOutOfBoundsException {
            this.disposalMethod = disposalMethod;
        }

        /**
         * 左端からの表示位置を取得します。
         * 
         * @return 左端からの表示位置
         */
        public int getImageLeftPosition() {
            return imageLeftPosition;
        }

        /**
         * 左端からの表示位置を設定します。
         * 
         * @param imageLeftPosition 左端からの表示位置
         */
        public void setImageLeftPosition(int imageLeftPosition) {
            this.imageLeftPosition = imageLeftPosition;
        }

        /**
         * 上端からの表示位置を取得します。
         * 
         * @return 上端からの表示位置
         */
        public int getImageTopPosition() {
            return imageTopPosition;
        }

        /**
         * 上端からの表示位置を設定します。
         * 
         * @param imageTopPosition 上端からの表示位置
         */
        public void setImageTopPosition(int imageTopPosition) {
            this.imageTopPosition = imageTopPosition;
        }

        /**
         * インターレス処理の有無を取得します。
         * 
         * @return インターレス処理の有無(true:有)
         */
        public boolean isInterlace() {
            return interlace;
        }

        /**
         * インターレス処理の有無を設定します。
         * 
         * @param interlace インターレス処理の有無(true:有)
         */
        public void setInterlace(boolean interlace) {
            this.interlace = interlace;
        }

        /**
         * 設定したImageオブジェクトを取得します。
         * 
         * @return Imageオブジェクト
         */
        public Image getImage() {
            return image;
        }

        /**
         * 設定したImageProducerオブジェクトを取得します。
         * 
         * @return Returns ImageProducerオブジェクト
         */
        public ImageProducer getProducer() {
            return producer;
        }
    }
}

/* */
