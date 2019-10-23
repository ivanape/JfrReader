package org.programwar.jfr;

import java.io.File;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class JfrReaderTest {

    private String jfrFilePath;

    @Before
    public void setUp() {
        jfrFilePath = Objects.requireNonNull(JfrReaderTest.class.getClassLoader().getResource("demo.jfr")).getPath();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void getStacksMap() {
        JfrReader parser = new JfrReader(new File(jfrFilePath));

        assertFalse(parser.getStacksMap().isEmpty());
    }

    @Test
    public void getStacks() {
        JfrReader parser = new JfrReader(new File(jfrFilePath));

        var stacks = parser.getStacks();
        System.out.println(stacks);

        assertFalse(stacks.isEmpty());
    }
}