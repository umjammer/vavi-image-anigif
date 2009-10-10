/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import vavi.awt.image.gif.GifAnimationEncoder;
import vavi.awt.image.gif.GifAnimationEncoder.GifFrame;
import vavi.awt.image.gif.GifEncoder.DisposalMethod;


/**
 * Sample2. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070619 nsano initial version <br>
 */
public class Sample2 {

    public static void main(String[] args) throws Exception {

        // ���֥������Ȥ�����
        GifAnimationEncoder gifimage = new GifAnimationEncoder(400, 300);

        // �롼�ײ����̵����
        gifimage.setLoopNumber(0);

            // �طʤ򥻥å�
        GifFrame baseFrame = new GifFrame(ImageIO.read(new File("Images", "back.gif")));

        // ɽ�����֤�1��
        baseFrame.setDelayTime(100);

        // ������Ĥ�
        baseFrame.setDisposalMethod(DisposalMethod.DoNotDispose);

        // ���᡼���򥻥å�
        gifimage.addImage(baseFrame);

        // ���Ե����˽񤭹���
        Image plane = ImageIO.read(new File("Images", "plane.gif"));
        for (int i = 350; i >= 0; i -= 50) {
            GifFrame overFrame = new GifFrame(plane);
            // ɽ�����֤�0.5��
            overFrame.setDelayTime(50);

            // ɽ��������β���������ʤȤ������ȤϺǽ�Υ��᡼���ˤ��֤���ɽ�����뤳�Ȥˤʤ��
            overFrame.setDisposalMethod(DisposalMethod.RestoreToPrevious);

            // ɽ�����֤򥻥å�
            overFrame.setImageLeftPosition(i);
            overFrame.setImageTopPosition(20);

            // ���᡼���򥻥å�
            gifimage.addImage(overFrame);
        }

        //	���󥳡��ɤ���
        gifimage.encode(new FileOutputStream(new File("Images", "animationSample2.gif")));
    }
}
