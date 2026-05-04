package com.github.sbt.junit.jupiter.internal.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.github.sbt.junit.jupiter.api.JupiterLauncherConfig;
import org.junit.Test;

public class LauncherConfigParserTest {

  @Test
  public void emptyArgsYieldDefault() {

    final var cfg = LauncherConfigParser.parse(new String[0]);
    assertThat(cfg, is(equalTo(JupiterLauncherConfig.DEFAULT)));
  }

  @Test
  public void unrelatedArgsAreIgnored() {

    final var cfg =
        LauncherConfigParser.parse(new String[] {"-v", "--include-tags=fast", "-Dfoo=bar"});
    assertThat(cfg, is(equalTo(JupiterLauncherConfig.DEFAULT)));
  }

  @Test
  public void singleFlagFlipsOnlyThatField() {

    final var cfg =
        LauncherConfigParser.parse(new String[] {"--test-engine-auto-registration-enabled=false"});
    assertThat(cfg.testEngineAutoRegistrationEnabled(), is(false));
    assertThat(cfg.launcherSessionListenerAutoRegistrationEnabled(), is(true));
    assertThat(cfg.launcherDiscoveryListenerAutoRegistrationEnabled(), is(true));
    assertThat(cfg.testExecutionListenerAutoRegistrationEnabled(), is(true));
    assertThat(cfg.postDiscoveryFilterAutoRegistrationEnabled(), is(true));
  }

  @Test
  public void allFiveFlagsParsed() {

    final var cfg =
        LauncherConfigParser.parse(
            new String[] {
              "--test-engine-auto-registration-enabled=false",
              "--launcher-session-listener-auto-registration-enabled=false",
              "--launcher-discovery-listener-auto-registration-enabled=false",
              "--test-execution-listener-auto-registration-enabled=false",
              "--post-discovery-filter-auto-registration-enabled=false"
            });
    assertThat(cfg, is(equalTo(new JupiterLauncherConfig(false, false, false, false, false))));
  }

  @Test
  public void lastOccurrenceWins() {

    final var cfg =
        LauncherConfigParser.parse(
            new String[] {
              "--test-engine-auto-registration-enabled=true",
              "--test-engine-auto-registration-enabled=false"
            });
    assertThat(cfg.testEngineAutoRegistrationEnabled(), is(false));
  }
}
