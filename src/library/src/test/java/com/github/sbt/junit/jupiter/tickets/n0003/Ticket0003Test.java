package com.github.sbt.junit.jupiter.tickets.n0003;

import junit.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class Ticket0003Test {

    @Rule
    public final TestRunner testRunner = new TestRunner();

    @Test
    @SuppressWarnings("unchecked")
    public void test() {

        testRunner.withArgs("+q", "+v");
        testRunner.execute(Ticket0003.class.getName());

        assertThat(testRunner.eventHandler().all(), hasSize(1));
        assertThat(testRunner.logger().byLevel("info"), contains(
                containsString("First line"),
                containsString("Second line"),
                containsString("Third line"),
                containsString("and fourth line")
        ));
    }
}
