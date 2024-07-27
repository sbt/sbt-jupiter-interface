package com.github.sbt.junit.jupiter.tickets.n0003;

import org.junit.jupiter.api.Test;

/**
 * Describes a test which prints to System.out
 */
class Ticket0003 {

    @Test
    void testWithLogOutput() {

        System.out.println("First line");
        System.out.println("Second line");
        System.out.print("Third line\n");
        System.out.println("and fourth line");
    }
}
