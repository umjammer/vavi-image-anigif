/*
 * Copyright (c) 2017 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.wmf;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.fail;


/**
 * SvgRendererTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 Nov 5, 2017 umjammer initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class SvgRendererTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "wmf")
    String wmf = "src/test/resources/tucan.wmf";

    @Property(name = "wmf.dir")
    String dir = "src/test/resources";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    public void test() throws Exception {
        InputStream is = Files.newInputStream(Path.of(wmf));
        WindowsMetafile metafile = WindowsMetafile.readFrom(is);
        metafile.setRenderer(new SvgRenderer());

        String result = (String) metafile.render();
Debug.println(result);

        Path out = Paths.get("tmp/wmf2svg.svg");
        OutputStream os = Files.newOutputStream(out);
        PrintStream ps = new PrintStream(os);
        ps.println(result);

        os.close();

        Desktop.getDesktop().browse(out.toUri());
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test11() throws Exception {
        InputStream is = Files.newInputStream(Path.of(wmf));
        WindowsMetafile metafile = WindowsMetafile.readFrom(is);
        metafile.setRenderer(new SvgRenderer());

        String result = (String) metafile.render();
Debug.println(result);

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        PrintStream ps0 = new PrintStream(baos);
//        ps0.println(result);

        Path out0 = Paths.get("tmp/wmf2svg.svg");
        OutputStream os = Files.newOutputStream(out0);
        PrintStream ps0 = new PrintStream(os);
        ps0.println(result);

        Path out = Paths.get("tmp/out.html");
        PrintStream ps = new PrintStream(Files.newOutputStream(out));
        ps.println("<html>");
        ps.println("<body>");

//        ps.print("<img src=\"data:image/svg+xml;base64,");
//        ps.print(Base64.getEncoder().encodeToString(baos.toByteArray()));

//        ps.print("<img src=\"data:image/svg+xml;charset=utf8,");
//        ps.print(URLEncoder.encode(result, StandardCharsets.UTF_8));

        ps.print("<img width=\"80%\" src=\"wmf2svg.svg");

        ps.println("\"/>");
        ps.println("</body>");
        ps.println("</html>");
        ps.flush();
        ps.close();

        Desktop.getDesktop().browse(out.toUri());
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {

        if (args.length == 0 || args.length > 2) {
            System.err.println("Useage: java wmf2svg <source file><destination file>");
            System.exit(0);
        }

        String inFile = args[0];
        String outFile = args[1];

        InputStream is = Files.newInputStream(Paths.get(inFile));
        OutputStream os = Files.newOutputStream(Paths.get(outFile));

        // get it to input stream and output stream here
        // make the

        // ?? make is stage WMF2SVG.read
        // WMF2SVGwrite

        WindowsMetafile metafile = WindowsMetafile.readFrom(is);
        metafile.setRenderer(new SvgRenderer());

        String result = (String) metafile.render();

        PrintStream ps = new PrintStream(os);
        ps.println(result);

        os.close();
        System.err.println("done");
    }
}
