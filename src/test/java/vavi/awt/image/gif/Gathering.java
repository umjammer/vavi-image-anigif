/*
 * Copyright (c) ${YEAR} by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.gif;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import vavi.util.Debug;


/**
 * Gathering
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070619 nsano initial version <br>
 */
public class Gathering {

    @Test
    void test1() throws Exception {
        main(new String[] {"../vavi-image-sandbox/tmp/aniout2", "tmp/gathering.gif"});
    }

    /**
     * @param args 0: dir, 1: out animation gif
     */
    public static void main(String[] args) throws Exception {

        Path dir = Paths.get(args[0]);
        Path out = Paths.get(args[1]);

        AtomicInteger i = new AtomicInteger();

        List<Path> files = Files.list(dir).filter(p -> p.toString().endsWith(".png")).sorted().collect(Collectors.toList());
        Path first = files.remove(0);
Debug.println(first);
        BufferedImage image = ImageIO.read(Files.newInputStream(first));

        GifAnimationEncoder encoder = new GifAnimationEncoder(image.getWidth(), image.getHeight());
        encoder.setLoopNumber(0);

        int delay0 = Integer.parseInt(first.getFileName().toString().split("[_.]")[1]);
        encoder.setDelay(delay0 / 10);
        encoder.addImage(ImageIO.read(Files.newInputStream(first)));

        files.forEach(p -> {
            try {
                int delay = Integer.parseInt(p.getFileName().toString().split("[_.]")[1]);
                encoder.setDelay(delay / 10);
                encoder.addImage(ImageIO.read(Files.newInputStream(p)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // encode
        encoder.encode(Files.newOutputStream(out));
    }
}
