package com.github.sbt.junit.jupiter.internal.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

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

  private static Options parse(String... args) {
    return new OptionsParser().parse(args);
  }
}
