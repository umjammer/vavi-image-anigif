/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.wmf;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.wmf.tosvg.WMFTranscoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.swing.JImageComponent;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * WmfImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/09/26 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class WmfImageReaderTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "wmf")
    String wmf = "src/test/resources/test.wmf";

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
    void test0() throws Exception {
        InputStream is = Files.newInputStream(Paths.get(wmf));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        TranscoderInput input = new TranscoderInput(is);
        TranscoderOutput output = new TranscoderOutput(new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8)));
        WMFTranscoder transcoder = new WMFTranscoder();
        transcoder.addTranscodingHint(WMFTranscoder.KEY_WIDTH, 400f);
        transcoder.addTranscodingHint(WMFTranscoder.KEY_WIDTH, 400f);
        transcoder.transcode(input, output);

        BufferedImage[] imageA = new BufferedImage[1];
        ImageTranscoder imageTranscoder = new ImageTranscoder() {
            @Override public BufferedImage createImage(int width, int height) {
                return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
            @Override public void writeImage(BufferedImage image, TranscoderOutput output) throws TranscoderException {
                imageA[0] = image;
            }
        };
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
Debug.println(baos.toString());
        TranscoderInput input2 = new TranscoderInput(bais);
        imageTranscoder.transcode(input2, null);
Debug.println(imageA[0]);
        show(imageA[0]);
    }

    @Test
    void test1() throws Exception {
        BufferedImage image = ImageIO.read(Files.newInputStream(Paths.get(wmf)));
        assertNotNull(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test11() throws Exception {
        BufferedImage image = ImageIO.read(Files.newInputStream(Paths.get(wmf)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        Path out = Paths.get("tmp/out.html");
        PrintStream ps = new PrintStream(Files.newOutputStream(out));
        ps.println("<html>");
        ps.println("<body>");
        ps.print("<img src=\"data:image/png;base64,");
        ps.print(Base64.getEncoder().encodeToString(baos.toByteArray()));
        ps.println("\" />");
        ps.println("</body>");
        ps.println("</html>");
        ps.flush();
        ps.close();
        Desktop.getDesktop().browse(out.toUri());
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("wmf").next();
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceRenderSize(new Dimension(400, 400));
        // #createImageInputStream() doesn't accept url
        ImageInputStream iis = ImageIO.createImageInputStream(Files.newInputStream(Paths.get(wmf)));
        assert iis != null : "is resource class correct?";
        reader.setInput(iis, true);
        BufferedImage image = reader.read(0, param);
        assertNotNull(image);
        show(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test3() throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                cdl.countDown();
            }
        });

        List<Path> paths = new ArrayList<>();
        AtomicInteger index = new AtomicInteger();

        JImageComponent panel = new JImageComponent();
        panel.setPreferredSize(new Dimension(1024, 1024));

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                try (Stream<Path> x = Files.walk(Paths.get(dir))) {
                    x.filter(p -> p.getFileName().toString().endsWith(".wmf")).forEach(paths::add);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        Path dir = Path.of("tmp/wmfs");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        frame.setTitle("WMF " + (index.get() + 1) + " / " + paths.size());
        frame.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int max = paths.size();
//Debug.println("KEY: " + e.getKeyCode() + ", max: " + max);
                switch (e.getKeyCode()) {
                case KeyEvent.VK_N:
                    if (index.get() + 1 <= max - 1) {
                        index.incrementAndGet();
                        repaint();
                    }
                    break;
                case KeyEvent.VK_B:
                    if (index.get() >= 1) {
                        index.decrementAndGet();
                        repaint();
                    }
                    break;
                }
            }
            @Override public void keyReleased(KeyEvent e) {
                int max = paths.size();
//Debug.println("KEY: " + e.getKeyCode() + ", max: " + max);
                switch (e.getKeyCode()) {
                case KeyEvent.VK_C:
                    try {
                        Path dest = dir.resolve(paths.get(index.get()).getFileName());
                        Files.copy(paths.get(index.get()), dest);
Debug.println("COPY: " + dest);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                    break;
                case KeyEvent.VK_D:
                    try {
                        Path dest = dir.resolve(paths.get(index.get()).getFileName());
                        Files.delete(dest);
Debug.println("DELETE: " + dest);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                    break;
                }
            }
            void repaint() {
                BufferedImage image;
                try {
                    image = ImageIO.read(paths.get(index.get()).toFile());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                if (image == null) {
Debug.println("image " + index + " is null");
                } else {
Debug.println("image[" + index + "]: " + image.getWidth() + " x " + image.getHeight());
                }
                panel.setImage(image);
                panel.repaint();
Debug.println("panel: " + panel.getWidth() + " x " + panel.getHeight());
                frame.setTitle("WMF " + (index.get() + 1) + " / " + paths.size());
            }
        });
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);

        cdl.await();
    }

    /** */
    static void show(BufferedImage image) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        JFrame frame = new JFrame("WMF");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                cdl.countDown();
            }
        });
        JImageComponent ic = new JImageComponent();
        ic.setImage(image);
        ic.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        frame.getContentPane().add(ic);
        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                ic.repaint();
            }
        });
        frame.pack();
        frame.setVisible(true);
        cdl.await();
    }

    /**
     * @param args Usage: java svgImage <source file>
     */
    public static void main(String[] args) throws Exception {
        String filename = args[0];

        ImageReader reader = ImageIO.getImageReadersByFormatName("wmf").next();
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceRenderSize(new Dimension(2000, 2000));
        // #createImageInputStream() doesn't accept url
        ImageInputStream iis = ImageIO.createImageInputStream(new File(filename));
        assert iis != null : "is resource class correct?";
        reader.setInput(iis, true);
        BufferedImage image = reader.read(0, param);
Debug.println("wmf: " + image.getWidth() + "x" + image.getHeight());
        show(image);
    }
}