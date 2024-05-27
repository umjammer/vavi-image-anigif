/*
 * Copyright (c) 2017 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.wmf;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;


/**
 * SvgRendererTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 Nov 5, 2017 umjammer initial version <br>
 */
public class SvgRendererTest {

    @Test
    @Disabled
    public void test() {
        fail("Not yet implemented");
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
