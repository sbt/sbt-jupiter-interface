package com.github.sbt.junit.jupiter.internal.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class OptionsParserTest {

  @Test
  public void toSetShouldSplitAtComma() {
    Options options = parse("--include-tags=development,production");
    assertThat(options.getIncludeTags(), contains("development", "production"));
  }

  @Test
  public void toSetShouldTrimDoubleQuotes() {
    Options options = parse("--include-tags=\"(development & integration)\",development");
    assertThat(options.getIncludeTags(), contains("(development & integration)", "development"));
  }

  @Test
  public void toSetShouldTrimSingleQuotes() {
    Options options = parse("--include-tags='(development & integration)'");
    assertThat(options.getIncludeTags(), contains("(development & integration)"));
  }

  @Test
  public void toSetShouldTrimSingleQuotesBeforeSplitting() {
    Options options = parse("--include-tags='(development & integration), production'");
    assertThat(options.getIncludeTags(), contains("(development & integration)", "production"));
  }

  @Test
  public void toSetShouldTrimDoubleQuotesBeforeSplitting() {
    Options options = parse("--include-tags=\"(development & integration), production\"");
    assertThat(options.getIncludeTags(), contains("(development & integration)", "production"));
  }

  @Test
  public void autoRegistrationDefaultsAreAllTrue() {
    Options options = parse();
    assertThat(options.isTestEngineAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherSessionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherDiscoveryListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isTestExecutionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isPostDiscoveryFilterAutoRegistrationEnabled(), is(true));
  }

  @Test
  public void disablingTestEngineAutoRegistrationLeavesOthersTrue() {
    Options options = parse("--test-engine-auto-registration=false");
    assertThat(options.isTestEngineAutoRegistrationEnabled(), is(false));
    assertThat(options.isLauncherSessionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherDiscoveryListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isTestExecutionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isPostDiscoveryFilterAutoRegistrationEnabled(), is(true));
  }

  @Test
  public void disablingLauncherSessionListenerAutoRegistrationLeavesOthersTrue() {
    Options options = parse("--launcher-session-listener-auto-registration=false");
    assertThat(options.isTestEngineAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherSessionListenerAutoRegistrationEnabled(), is(false));
    assertThat(options.isLauncherDiscoveryListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isTestExecutionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isPostDiscoveryFilterAutoRegistrationEnabled(), is(true));
  }

  @Test
  public void disablingLauncherDiscoveryListenerAutoRegistrationLeavesOthersTrue() {
    Options options = parse("--launcher-discovery-listener-auto-registration=false");
    assertThat(options.isTestEngineAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherSessionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherDiscoveryListenerAutoRegistrationEnabled(), is(false));
    assertThat(options.isTestExecutionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isPostDiscoveryFilterAutoRegistrationEnabled(), is(true));
  }

  @Test
  public void disablingTestExecutionListenerAutoRegistrationLeavesOthersTrue() {
    Options options = parse("--test-execution-listener-auto-registration=false");
    assertThat(options.isTestEngineAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherSessionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherDiscoveryListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isTestExecutionListenerAutoRegistrationEnabled(), is(false));
    assertThat(options.isPostDiscoveryFilterAutoRegistrationEnabled(), is(true));
  }

  @Test
  public void disablingPostDiscoveryFilterAutoRegistrationLeavesOthersTrue() {
    Options options = parse("--post-discovery-filter-auto-registration=false");
    assertThat(options.isTestEngineAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherSessionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherDiscoveryListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isTestExecutionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isPostDiscoveryFilterAutoRegistrationEnabled(), is(false));
  }

  @Test
  public void explicitlyEnablingAutoRegistrationMatchesDefault() {
    Options options =
        parse(
            "--test-engine-auto-registration=true",
            "--launcher-session-listener-auto-registration=true",
            "--launcher-discovery-listener-auto-registration=true",
            "--test-execution-listener-auto-registration=true",
            "--post-discovery-filter-auto-registration=true");
    assertThat(options.isTestEngineAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherSessionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherDiscoveryListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isTestExecutionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isPostDiscoveryFilterAutoRegistrationEnabled(), is(true));
  }

  @Test
  public void mixedAutoRegistrationValuesAcrossAllFiveFlags() {
    Options options =
        parse(
            "--test-engine-auto-registration=true",
            "--launcher-session-listener-auto-registration=false",
            "--launcher-discovery-listener-auto-registration=true",
            "--test-execution-listener-auto-registration=false",
            "--post-discovery-filter-auto-registration=true");
    assertThat(options.isTestEngineAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherSessionListenerAutoRegistrationEnabled(), is(false));
    assertThat(options.isLauncherDiscoveryListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isTestExecutionListenerAutoRegistrationEnabled(), is(false));
    assertThat(options.isPostDiscoveryFilterAutoRegistrationEnabled(), is(true));
  }

  @Test
  public void autoRegistrationValueParsingIsCaseInsensitive() {
    Options options =
        parse(
            "--test-engine-auto-registration=False",
            "--launcher-session-listener-auto-registration=TRUE");
    assertThat(options.isTestEngineAutoRegistrationEnabled(), is(false));
    assertThat(options.isLauncherSessionListenerAutoRegistrationEnabled(), is(true));
  }

  @Test
  public void malformedAutoRegistrationValueThrowsIllegalArgumentException() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> parse("--test-engine-auto-registration=garbage"));
    assertThat(ex.getMessage(), containsString("--test-engine-auto-registration=garbage"));
  }

  @Test
  public void unrecognisedFlagsDoNotAffectAutoRegistrationDefaults() {
    Options options = parse("-q", "--include-tags=fast", "-Dfoo=bar", "someGlob");
    assertThat(options.isTestEngineAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherSessionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isLauncherDiscoveryListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isTestExecutionListenerAutoRegistrationEnabled(), is(true));
    assertThat(options.isPostDiscoveryFilterAutoRegistrationEnabled(), is(true));
  }

  @Test
  public void manualRegistrationListsDefaultToEmpty() {
    Options options = parse();
    assertThat(options.getTestEngines(), is(empty()));
    assertThat(options.getLauncherSessionListeners(), is(empty()));
    assertThat(options.getLauncherDiscoveryListeners(), is(empty()));
    assertThat(options.getTestExecutionListeners(), is(empty()));
    assertThat(options.getPostDiscoveryFilters(), is(empty()));
  }

  @Test
  public void testEnginesPreserveOrderWhenCommaSeparated() {
    Options options = parse("--test-engines=a.B,c.D,e.F");
    assertThat(options.getTestEngines(), contains("a.B", "c.D", "e.F"));
  }

  @Test
  public void emptyManualRegistrationFlagYieldsEmptyList() {
    Options options = parse("--test-engines=");
    assertThat(options.getTestEngines(), is(empty()));
  }

  @Test
  public void launcherSessionListenersFlagPopulatesOnlyItsGetter() {
    Options options = parse("--launcher-session-listeners=a.B");
    assertThat(options.getLauncherSessionListeners(), contains("a.B"));
    assertThat(options.getTestEngines(), is(empty()));
    assertThat(options.getLauncherDiscoveryListeners(), is(empty()));
    assertThat(options.getTestExecutionListeners(), is(empty()));
    assertThat(options.getPostDiscoveryFilters(), is(empty()));
  }

  @Test
  public void launcherDiscoveryListenersFlagPopulatesOnlyItsGetter() {
    Options options = parse("--launcher-discovery-listeners=a.B");
    assertThat(options.getLauncherDiscoveryListeners(), contains("a.B"));
    assertThat(options.getTestEngines(), is(empty()));
    assertThat(options.getLauncherSessionListeners(), is(empty()));
    assertThat(options.getTestExecutionListeners(), is(empty()));
    assertThat(options.getPostDiscoveryFilters(), is(empty()));
  }

  @Test
  public void testExecutionListenersFlagPopulatesOnlyItsGetter() {
    Options options = parse("--test-execution-listeners=a.B");
    assertThat(options.getTestExecutionListeners(), contains("a.B"));
    assertThat(options.getTestEngines(), is(empty()));
    assertThat(options.getLauncherSessionListeners(), is(empty()));
    assertThat(options.getLauncherDiscoveryListeners(), is(empty()));
    assertThat(options.getPostDiscoveryFilters(), is(empty()));
  }

  @Test
  public void postDiscoveryFiltersFlagPopulatesOnlyItsGetter() {
    Options options = parse("--post-discovery-filters=a.B");
    assertThat(options.getPostDiscoveryFilters(), contains("a.B"));
    assertThat(options.getTestEngines(), is(empty()));
    assertThat(options.getLauncherSessionListeners(), is(empty()));
    assertThat(options.getLauncherDiscoveryListeners(), is(empty()));
    assertThat(options.getTestExecutionListeners(), is(empty()));
  }

  @Test
  public void manualRegistrationListTrimsWhitespaceAndQuotes() {
    Options options = parse("--test-engines=\" a.B , c.D \"");
    assertThat(options.getTestEngines(), contains("a.B", "c.D"));
  }

  private static Options parse(String... args) {
    return new OptionsParser().parse(args);
  }
}
