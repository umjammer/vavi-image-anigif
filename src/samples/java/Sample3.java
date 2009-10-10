/*
 * Copyright (C)2005 by ���. All rights reserved.
 *
 * Created on 2005/07/20
 */

import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import com.rakugakichat.gif.DisposalMethod;
import com.rakugakichat.gif.GifAnimationEncoder;
import com.rakugakichat.gif.GifAnimationFrame;


/**
 * GifAnimationEncoder ����ץ륽����3
 *      GifAnimationFrame���֥������Ȥ�ȤäƤ��ޤ���
 *      ���Ե��Υ��᡼���Τߤ�ŤͤƤ���Τǥե����륵�������������ʤ�ޤ���
 * E-mail: h-aiura@bd5.so-net.ne.jp
 * URL: http://www001.upp.so-net.ne.jp/h-aiura
 *      http://www.rakugakichat.com
 */
public class Sample3 {

    public static void main(String[] args) throws Exception {

        // ���֥������Ȥ�����
        GifAnimationEncoder gifimage = new GifAnimationEncoder(400, 300);

        // �롼�ײ����̵����
        gifimage.setLoopNumber(0);

            // �طʤ򥻥å�
        GifAnimationFrame baseFrame = new GifAnimationFrame(ImageIO.read(new File("." + File.separator + "Images" + File.separator + "back.gif")));

        // ɽ�����֤�1��
        baseFrame.setDelayTime(100);

        // ������Ĥ�
        baseFrame.setDisposalMethod(DisposalMethod.DoNotDispose);

        // ���᡼���򥻥å�
        gifimage.addImage(baseFrame);

        // ���Ե����˽񤭹���
        Image plane = ImageIO.read(new File("." + File.separator + "Images" + File.separator + "plane.gif"));
        for (int i = 350; i >= 0; i -= 50) {
            GifAnimationFrame overFrame = new GifAnimationFrame(plane);
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
        gifimage.encode(new FileOutputStream(new File("." + File.separator + "Images" + File.separator + "animationSample3.gif")));
    }
}
