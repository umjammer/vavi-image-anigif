/*
 * Copyright (c) ${YEAR} by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.wmf;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import org.freehep.graphicsio.emf.EMFDisplay;
import org.freehep.graphicsio.emf.EMFHeader;
import org.freehep.graphicsio.emf.EMFInputStream;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * FreehepTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-06-10 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class FreehepTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "emf")
    String wmf = "src/test/resources/test.emf";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    @Disabled("got EOFException")
    void test1() throws Exception {
        EMFInputStream emf = new EMFInputStream(new BufferedInputStream(Files.newInputStream(Path.of(wmf))));

        EMFHeader header = emf.readHeader();
Debug.println(header);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        EMFDisplay.main(new String[] {wmf});
        cdl.await();
    }
}
