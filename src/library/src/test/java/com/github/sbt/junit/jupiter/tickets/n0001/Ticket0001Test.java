package com.github.sbt.junit.jupiter.tickets.n0001;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import junit.TestRunner;
import org.junit.Rule;
import org.junit.Test;
import sbt.testing.Status;

public class Ticket0001Test {

  @Rule public final TestRunner testRunner = new TestRunner();

  @Test
  public void test() {

    testRunner.execute(Ticket0001.class.getName());

    assertThat(testRunner.eventHandler().byStatus(Status.Failure), hasSize(1));
    assertThat(
        testRunner.logger().byLevel("error"),
        hasItem(containsString("Could not find factory method [dataProvider]")));
  }
}
