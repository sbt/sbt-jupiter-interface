package jupiter.samples;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/** Sample test methods for the junit-platform-suite-engine. */
@Suite
@SelectClasses({SimpleTests.class, VintageTests.class})
public class SuiteTest {}
