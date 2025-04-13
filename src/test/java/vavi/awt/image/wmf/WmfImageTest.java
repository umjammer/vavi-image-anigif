/*
 * Copyright (c) 2017 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.wmf;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import javax.swing.JFrame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.swing.JImageComponent;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * WmfImageTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 Nov 5, 2017 umjammer initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class WmfImageTest {

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
    void test1() throws Exception {
        WmfImage wmfImage = new WmfImage(Files.newInputStream(Path.of(wmf)));
        BufferedImage image = wmfImage.getImage();
        show(image, (x, y) -> {
            wmfImage.setSize(new Dimension(x, y));
            return wmfImage.getImage();
        });
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
            void repaint() {
                BufferedImage image;
                try {
                    image = new WmfImage(Files.newInputStream(paths.get(index.get()))).getImage();
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
    static void show(BufferedImage image, BiFunction<Integer, Integer, BufferedImage> resize) throws Exception {
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
                if (resize != null)
                    ic.setImage(resize.apply(e.getComponent().getWidth(), e.getComponent().getHeight()));
                ic.repaint();
            }
        });
        frame.pack();
        frame.setVisible(true);
        cdl.await();
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
        InputStream is;
        if (args.length > 0) {
            String file = args[0];
Debug.println("file: " + file);
            is = Files.newInputStream(Paths.get(file));
        } else {
            WmfImageTest app = new WmfImageTest();
            app.setup();
Debug.println("file: " + app.wmf);
            is = Files.newInputStream(Path.of(app.wmf));
        }

        WmfImage wmf = new WmfImage(is, 960, 1280);

        show(wmf.getImage(), null);
    }
}
