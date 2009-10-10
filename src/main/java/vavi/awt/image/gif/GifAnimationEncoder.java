/*
 * Copyright (c) 2005 by ���. All rights reserved.
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
 * gif animation ���������륯�饹�Ǥ���
 * 
 * @author <a href="mailto:h-aiura@bd5.so-net.ne.jp">mo_q</a>
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 1.0 2005/07/20
 * @see "http://www001.upp.so-net.ne.jp/h-aiura"
 * @see "http://www.rakugakichat.com"
 */
public class GifAnimationEncoder {
    /** gifImageFrame�������������� */
    private List<GifFrame> frames = new ArrayList<GifFrame>();

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
     * ���ꥵ������GifAnimationEncoder���֥������Ȥ��ۤ��ޤ���
     * ���餫���ᡢ�롼�ײ����̵�²�
     * ���ե졼�ढ�����ɽ���ÿ���0�á�
     * DisposalMethod��̵��������ꤵ��ޤ���
     */
    public GifAnimationEncoder() {
    }

    /**
     * ���ꥵ������GifAnimationEncoder���֥������Ȥ��ۤ��ޤ���
     * ���餫���ᡢ�롼�ײ����̵�²�
     * ���ե졼�ढ�����ɽ���ÿ���0�á�
     * DisposalMethod��̵��������ꤵ��ޤ���
     * 
     * @param width ���᡼������
     * @param height ���᡼���ι⤵
     */
    public GifAnimationEncoder(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * ���󥳡��ɤ򤷡���̤�OutputStream�˽��Ϥ��ޤ���
     * 
     * @param os OutputStream
     * @throws IOException ���ϥ��顼
     * @throws IllegalStateException ���᡼����¸�ߤ��ʤ��Ȥ�
     */
    public void encode(OutputStream os) throws IOException {

        // ���᡼�����ʤ����ϥ��顼
        if (frames.size() < 1) {
            throw new IllegalStateException("no image");
        }

        LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(os);

        // gif�ǡ�����񤭹���
        // �����������Jef Poskanzer�����GifEncoder�Υ������򻲹ͤˤ��ޤ�����
        // Write the Magic header
        dos.writeBytes("GIF89a");

        // Write out the screen width and height
        dos.writeShort(width);
        dos.writeShort(height);

        // �����Х륫�顼�Ϥʤ�
        dos.writeByte(0x00);

        // Write out the Background colour
        // �����Х륫�顼�Ϥʤ��Τ�Background colour�λ���Ϥ��ʤ�
        dos.writeByte(0);

        // Pixel aspect ratio - 1:1.
        // Putbyte( (byte) 49, outs );
        // Java's GIF reader currently has a bug, if the aspect ratio byte is
        // not zero it throws an ImageFormatException. It doesn't know that
        // 49 means a 1:1 aspect ratio. Well, whatever, zero works with all
        // the other decoders I've tried so it probably doesn't hurt.
        dos.writeByte(0);

        // �����֤��������Ԥ�
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

        // ���᡼���򤯤äĤ���
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
     * ���ե졼�ढ�����ɽ���ÿ�(1/100��ñ��)�ε����ͤ����ꤷ�ޤ���<br>
     * 
     * @param delay ���ե졼�ढ�����ɽ���ÿ�
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * �롼�פ���������ꤷ�ޤ���<BR>
     * 
     * @param num �롼�ײ��,0��̵����<br>
     *            �����Υ֥饦��(Opera�ʤ�)�Ǥϡ�������2�ʾ�ˤ������˥롼�ײ����(����-1)�ˤʤ뤳�Ȥ�����ޤ���
     */
    public void setLoopNumber(int num) {
        loopNumber = num;
    }

    /**
     * ���᡼���ι⤵��������ޤ���
     * 
     * @return ���᡼���ι⤵
     */
    public int getHeight() {
        return height;
    }

    /**
     * ���᡼���ι⤵�����ꤷ�ޤ���
     * 
     * @param height ���᡼���ι⤵
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * ���᡼��������������ޤ���
     * 
     * @return Returns the width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * ���᡼�����������ꤷ�ޤ���
     * 
     * @param width width.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Disposal method(���᡼���νŤ���)�����ꤷ�ޤ��� ���󥿡��ե�����DisposalMethod������ǻ��ꤷ�Ƥ���������
     * 
     * @param disposalMethod Disposal method(���᡼���νŤ���)
     * @see DisposalMethod
     */
    public void setDisposalMethod(DisposalMethod disposalMethod) throws IndexOutOfBoundsException {
        this.disposalMethod = disposalMethod;
    }

    /**
     * ���󥳡��ɤ��륤�᡼�����ɲä��ޤ���<br>
     * �ɲä�����˥��᡼����ɽ������ޤ���<br>
     * 
     * @param aniFrame GifAnimationFrame���֥�������
     */
    public void addImage(GifFrame aniFrame) {
        frames.add(aniFrame);
    }

    /**
     * ���󥳡��ɤ��륤�᡼�����ɲä��ޤ���<br>
     * �ɲä�����˥��᡼����ɽ������ޤ���<br>
     * 1�ե졼�ढ�����ɽ���ÿ���Disposal
     * method�ϡ�setDefaultDelay�᥽�å�,setDefaultDisposalMethod�᥽�åɤǤ������ͤ�Ŭ�Ѥ���ޤ���
     * 
     * @param image Image���֥�������
     * @see #setDelay(int)
     * @see #setDisposalMethod(int)
     */
    public void addImage(Image image) {
        frames.add(new GifFrame(image, delay, disposalMethod));
    }

    /**
     * ���󥳡��ɤ��륤�᡼�����ɲä��ޤ���<br>
     * �ɲä�����˥��᡼����ɽ������ޤ���<br>
     * 1�ե졼�ढ�����ɽ���ÿ���Disposal
     * method�ϡ�setDefaultDelay�᥽�å�,setDefaultDisposalMethod�᥽�åɤǤ������ͤ�Ŭ�Ѥ���ޤ���
     * 
     * @param producer ImageProducer���֥�������
     * @see #setDelay(int)
     * @see #setDisposalMethod(int)
     */
    public void addImage(ImageProducer producer) {
        frames.add(new GifFrame(producer, delay, disposalMethod));
    }

    /**
     * ����Υ���ǥå����˥��󥳡��ɤ��륤�᡼�����ɲä��ޤ���<br>
     * ����ǥå�����˥��᡼����ɽ������ޤ���<br>
     * �ɲä�����˥��᡼����ɽ������ޤ���<br>
     * 
     * @param index ����ǥå����ֹ�
     * @param aniFrame GifAnimationFrame���֥�������
     */
    public void addImage(int index, GifFrame aniFrame) {
        frames.add(index, aniFrame);
    }

    /**
     * ����Υ���ǥå����˥��󥳡��ɤ��륤�᡼�����ɲä��ޤ���<br>
     * ����ǥå�����˥��᡼����ɽ������ޤ���<br>
     * �ɲä�����˥��᡼����ɽ������ޤ���<br>
     * 1�ե졼�ढ�����ɽ���ÿ���Disposal
     * method�ϡ�setDefaultDelay�᥽�å�,setDefaultDisposalMethod�᥽�åɤǤ������ͤ�Ŭ�Ѥ���ޤ���
     * 
     * @param index ����ǥå����ֹ�
     * @param image Image���֥�������
     * @see #setDelay(int)
     * @see #setDisposalMethod(int)
     */
    public void addImage(int index, Image image) {
        frames.add(index, new GifFrame(image, delay, disposalMethod));
    }

    /**
     * ����Υ���ǥå����˥��󥳡��ɤ��륤�᡼�����ɲä��ޤ���<br>
     * ����ǥå�����˥��᡼����ɽ������ޤ���<br>
     * �ɲä�����˥��᡼����ɽ������ޤ���<br>
     * 1�ե졼�ढ�����ɽ���ÿ���Disposal
     * method�ϡ�setDefaultDelay�᥽�å�,setDefaultDisposalMethod�᥽�åɤǤ������ͤ�Ŭ�Ѥ���ޤ���
     * 
     * @param index ����ǥå����ֹ�
     * @param producer ImageProducer���֥�������
     * @see #setDelay(int)
     * @see #setDisposalMethod(int)
     */
    public void addImage(int index, ImageProducer producer) {
        frames.add(index, new GifFrame(producer, delay, disposalMethod));
    }

    /**
     * ���󥳡��ɤ��륤�᡼�����ɲä��ޤ���<br>
     * �ɲä�����˥��᡼����ɽ������ޤ���<br>
     * ��������ǻ��ꤷ���ÿ�(1/100��ñ��)ɽ�������Τ����Υ��᡼���˰ܤ�ޤ���<br>
     * Disposal method�ϡ�setDefaultDisposalMethod�᥽�åɤǤ������ͤ�Ŭ�Ѥ���ޤ���
     * 
     * @param image Image���֥�������
     * @param delayTime ɽ������(1/100��ñ��)
     * @see #setDisposalMethod(int)
     */
    public void addImage(Image image, int delayTime) {
        frames.add(new GifFrame(image, delayTime, disposalMethod));
    }

    /**
     * ���󥳡��ɤ��륤�᡼�����ɲä��ޤ���<br>
     * �ɲä�����˥��᡼����ɽ������ޤ���<br>
     * ��������ǻ��ꤷ���ÿ�(1/100��ñ��)ɽ�������Τ����Υ��᡼���˰ܤ�ޤ���<br>
     * Disposal method�ϡ�setDefaultDisposalMethod�᥽�åɤǤ������ͤ�Ŭ�Ѥ���ޤ���
     * 
     * @param producer ImageProducer���֥�������
     * @param delayTime ɽ������(1/100��ñ��)
     * @see #setDisposalMethod(int)
     */
    public void addImage(ImageProducer producer, int delayTime) {
        frames.add(new GifFrame(producer, delayTime, disposalMethod));
    }

    /**
     * ����Υ���ǥå����˥��󥳡��ɤ��륤�᡼�����ɲä��ޤ���<br>
     * ����ǥå�����˥��᡼����ɽ������ޤ���<br>
     * ��������ǻ��ꤷ���ÿ�(1/100��ñ��)ɽ�������Τ����Υ��᡼���˰ܤ�ޤ���<br>
     * Disposal method�ϡ�setDefaultDisposalMethod�᥽�åɤǤ������ͤ�Ŭ�Ѥ���ޤ���
     * 
     * @param index ����ǥå����ֹ�
     * @param image Image���֥�������
     * @param delayTime ɽ������(1/100��ñ��)
     * @see #setDisposalMethod(int)
     */
    public void addImage(int index, Image image, int delayTime) {
        frames.add(index, new GifFrame(image, delayTime, disposalMethod));
    }

    /**
     * ����Υ���ǥå����˥��󥳡��ɤ��륤�᡼�����ɲä��ޤ���<br>
     * ����ǥå�����˥��᡼����ɽ������ޤ���<br>
     * ��������ǻ��ꤷ���ÿ�(1/100��ñ��)ɽ�������Τ����Υ��᡼���˰ܤ�ޤ���<br>
     * Disposal method�ϡ�setDefaultDisposalMethod�᥽�åɤǤ������ͤ�Ŭ�Ѥ���ޤ���
     * 
     * @param index ����ǥå����ֹ�
     * @param producer ImageProducer���֥�������
     * @param delayTime ɽ������(1/100��ñ��)
     * @see #setDisposalMethod(int)
     */
    public void addImage(int index, ImageProducer producer, int delayTime) {
        frames.add(index, new GifFrame(producer, delayTime, disposalMethod));
    }

    /**
     * ����Υ���ǥå����ˤ��륤�᡼���򥨥󥳡��ɤ��륤�᡼���ΰ������������ޤ���
     * 
     * @param index ����ǥå����ֹ�
     * @return �������GifAnimationFrame���֥�������
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
     * ���󥳡��ɤ��륤�᡼����������֤��ޤ���
     * 
     * @return ���󥳡��ɤ��륤�᡼�������
     */
    public int getImageSize() {
        return frames.size();
    }

    /**
     * gif animation �Υե졼��Υ��᡼���������ͤ��Ǽ���륪�֥������ȤǤ���<br>
     * ���Υ��֥������Ȥˤ��1�ե졼�����ɽ�����֡����󥿡��쥹������̵ͭ��
     * ɽ���ÿ���disposal Method(���᡼���νŤ���)�����꤬�Ǥ��ޤ���
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

        /** ������� x */
        private int imageLeftPosition = 0;

        /** ������� y */
        private int imageTopPosition = 0;

        /**
         * Image���֥������Ȥ���GifAnimationFrame���֥������Ȥ��ۤ��ޤ���<br>
         * ���餫���ᡢɽ�����֤�(0,0)�����󥿡��쥹��̵����ɽ���ÿ���0�á� disposal Method��̵��������ꤵ��ޤ���
         * 
         * @param image Image���֥�������
         */
        public GifFrame(Image image) {
            this.image = image;
        }

        /**
         * ImageProducer���֥������Ȥ���GifAnimationFrame���֥������Ȥ��ۤ��ޤ���<br>
         * ���餫���ᡢɽ�����֤�(0,0)�����󥿡��쥹��̵����ɽ���ÿ���0�á� disposal Method��̵��������ꤵ��ޤ���
         * 
         * @param producer ImageProducer���֥�������
         */
        public GifFrame(ImageProducer producer) {
            this.producer = producer;
        }

        /**
         * Image���֥������Ȥ���GifAnimationFrame���֥������Ȥ��ۤ��ޤ���<br>
         * ɽ���ÿ���disposal Method�ϰ������ͤ� ɽ�����֤�(0,0)�����󥿡��쥹��̵�������ꤵ��ޤ���
         * 
         * @param image Image���֥�������
         * @param delayTime 1�ե졼�ढ�����ɽ���ÿ�
         * @param disposalMethod disposalMethod
         */
        public GifFrame(Image image, int delayTime, DisposalMethod disposalMethod) {
            this.image = image;
            this.delayTime = delayTime;
            this.disposalMethod = disposalMethod;
        }

        /**
         * ImageProducer���֥������Ȥ���GifAnimationFrame���֥������Ȥ��ۤ��ޤ���<br>
         * ɽ���ÿ���disposal Method�ϰ������ͤ� ɽ�����֤�(0,0)�����󥿡��쥹��̵�������ꤵ��ޤ���
         * 
         * @param producer ImageProducer���֥�������
         * @param delayTime 1�ե졼�ढ�����ɽ���ÿ�
         * @param disposalMethod disposalMethod
         */
        public GifFrame(ImageProducer producer, int delayTime, DisposalMethod disposalMethod) {
            this.producer = producer;
            this.delayTime = delayTime;
            this.disposalMethod = disposalMethod;
        }

        /**
         * Image���֥������Ȥ���GifAnimationFrame���֥������Ȥ��ۤ��ޤ���<br>
         * ɽ�����֤�(0,0)�����󥿡��쥹��ɽ���ÿ��� disposal Method�ϰ������ͤ����ꤵ��ޤ���
         * 
         * @param image Image���֥�������
         * @param imageLeftPosition ��ü�����ɽ������
         * @param imageTopPosition ��ü�����ɽ������
         * @param interlace ���󥿡��쥹�����򤪤��ʤ�����(true:�����ʤ�)
         * @param delayTime 1�ե졼�ढ�����ɽ���ÿ�
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
         * Image���֥������Ȥ���GifAnimationFrame���֥������Ȥ��ۤ��ޤ���<br>
         * ɽ�����֤�(0,0)�����󥿡��쥹��ɽ���ÿ��� disposal Method�ϰ������ͤ����ꤵ��ޤ���
         * 
         * @param producer ImageProducer���֥�������
         * @param imageLeftPosition ��ü�����ɽ������
         * @param imageTopPosition ��ü�����ɽ������
         * @param interlace ���󥿡��쥹�����򤪤��ʤ�����(true:�����ʤ�)
         * @param delayTime 1�ե졼�ढ�����ɽ���ÿ�
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
         * 1�ե졼�ढ�����ɽ���ÿ���������ޤ���
         * 
         * @return 1�ե졼�ढ�����ɽ���ÿ�
         */
        public int getDelayTime() {
            return delayTime;
        }

        /**
         * 1�ե졼�ढ�����ɽ���ÿ������ꤷ�ޤ���
         * 
         * @param delayTime 1�ե졼�ढ�����ɽ���ÿ�
         */
        public void setDelayTime(int delayTime) {
            this.delayTime = delayTime;
        }

        /**
         * Disposal method(���᡼���νŤ���)��������ޤ��� ����ͤϥ��󥿡��ե�����DisposalMethod������򻲾Ȥ��Ƥ���������
         * 
         * @return Disposal method
         * @see DisposalMethod
         */
        public DisposalMethod getDisposalMethod() {
            return disposalMethod;
        }

        /**
         * Disposal method(���᡼���νŤ���)�����ꤷ�ޤ��� ���󥿡��ե�����DisposalMethod������ǻ��ꤷ�Ƥ���������
         * 
         * @param disposalMethod Disposal method(���᡼���νŤ���)
         * @see DisposalMethod
         * @throws IndexOutOfBoundsException DisposalMethod��������ϰϤ�Ķ�������
         */
        public void setDisposalMethod(DisposalMethod disposalMethod) throws IndexOutOfBoundsException {
            this.disposalMethod = disposalMethod;
        }

        /**
         * ��ü�����ɽ�����֤�������ޤ���
         * 
         * @return ��ü�����ɽ������
         */
        public int getImageLeftPosition() {
            return imageLeftPosition;
        }

        /**
         * ��ü�����ɽ�����֤����ꤷ�ޤ���
         * 
         * @param imageLeftPosition ��ü�����ɽ������
         */
        public void setImageLeftPosition(int imageLeftPosition) {
            this.imageLeftPosition = imageLeftPosition;
        }

        /**
         * ��ü�����ɽ�����֤�������ޤ���
         * 
         * @return ��ü�����ɽ������
         */
        public int getImageTopPosition() {
            return imageTopPosition;
        }

        /**
         * ��ü�����ɽ�����֤����ꤷ�ޤ���
         * 
         * @param imageTopPosition ��ü�����ɽ������
         */
        public void setImageTopPosition(int imageTopPosition) {
            this.imageTopPosition = imageTopPosition;
        }

        /**
         * ���󥿡��쥹������̵ͭ��������ޤ���
         * 
         * @return ���󥿡��쥹������̵ͭ(true:ͭ)
         */
        public boolean isInterlace() {
            return interlace;
        }

        /**
         * ���󥿡��쥹������̵ͭ�����ꤷ�ޤ���
         * 
         * @param interlace ���󥿡��쥹������̵ͭ(true:ͭ)
         */
        public void setInterlace(boolean interlace) {
            this.interlace = interlace;
        }

        /**
         * ���ꤷ��Image���֥������Ȥ�������ޤ���
         * 
         * @return Image���֥�������
         */
        public Image getImage() {
            return image;
        }

        /**
         * ���ꤷ��ImageProducer���֥������Ȥ�������ޤ���
         * 
         * @return Returns ImageProducer���֥�������
         */
        public ImageProducer getProducer() {
            return producer;
        }
    }
}

/* */
