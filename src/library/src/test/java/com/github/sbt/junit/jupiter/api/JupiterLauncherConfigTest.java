package com.github.sbt.junit.jupiter.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

import java.util.List;
import org.junit.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.TestExecutionListener;

public class JupiterLauncherConfigTest {

  @Test
  public void defaultConfigBuildsJUnitConfig() {

    final var jUnitConfig =
        JupiterLauncherConfig.DEFAULT.toJUnitConfig(getClass().getClassLoader());
    assertThat(jUnitConfig, is(notNullValue()));
  }

  @Test
  public void registersExplicitTestEngineByFqn() {

    final var cfg =
        new JupiterLauncherConfig(
            false,
            true,
            true,
            true,
            true,
            List.of(NoOpTestEngine.class.getName()),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    final var jUnitConfig = cfg.toJUnitConfig(getClass().getClassLoader());
    assertThat(jUnitConfig, is(notNullValue()));
  }

  @Test
  public void registersExplicitTestExecutionListenerByFqn() {

    final var cfg =
        new JupiterLauncherConfig(
            true,
            true,
            true,
            false,
            true,
            List.of(),
            List.of(),
            List.of(),
            List.of(NoOpTestExecutionListener.class.getName()),
            List.of());
    final var jUnitConfig = cfg.toJUnitConfig(getClass().getClassLoader());
    assertThat(jUnitConfig, is(notNullValue()));
  }

  @Test
  public void unknownFqnThrowsRuntimeException() {

    final var cfg =
        new JupiterLauncherConfig(
            false,
            true,
            true,
            true,
            true,
            List.of("does.not.Exist"),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    final var ex =
        assertThrows(RuntimeException.class, () -> cfg.toJUnitConfig(getClass().getClassLoader()));
    assertThat(ex.getMessage(), containsString("does.not.Exist"));
  }

  @Test
  public void wrongInterfaceThrowsRuntimeException() {

    // NoOpTestEngine implements TestEngine, not TestExecutionListener.
    final var cfg =
        new JupiterLauncherConfig(
            true,
            true,
            true,
            false,
            true,
            List.of(),
            List.of(),
            List.of(),
            List.of(NoOpTestEngine.class.getName()),
            List.of());
    final var ex =
        assertThrows(RuntimeException.class, () -> cfg.toJUnitConfig(getClass().getClassLoader()));
    assertThat(ex.getMessage(), containsString(NoOpTestEngine.class.getName()));
  }

  public static final class NoOpTestEngine implements TestEngine {
    @Override
    public String getId() {
      return "no-op";
    }

    @Override
    public TestDescriptor discover(
        org.junit.platform.engine.EngineDiscoveryRequest request, UniqueId uniqueId) {
      return new org.junit.platform.engine.support.descriptor.EngineDescriptor(uniqueId, "no-op");
    }

    @Override
    public void execute(org.junit.platform.engine.ExecutionRequest request) {}
  }

  public static final class NoOpTestExecutionListener implements TestExecutionListener {}
}
