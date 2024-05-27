/*
 * Copyright (c) ${YEAR} by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.wmf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import javax.imageio.ImageIO;


/**
 * for Quick Look Generator.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-01-13 nsano initial version <br>
 */
public class WmfMain {

    /**
     * @param args 0: wmfImage
     */
    public static void main(String[] args) throws Exception {
        String wmf = args[0];

        BufferedImage image = ImageIO.read(Files.newInputStream(Paths.get(wmf)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        System.out.print("<img src=\"data:image/png;base64,");
        System.out.print(Base64.getEncoder().encodeToString(baos.toByteArray()));
        System.out.print("\" />");
        System.out.flush();
    }
}
