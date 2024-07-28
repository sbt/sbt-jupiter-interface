package example;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("example.foo")
@IncludeClassNamePatterns({".*Test"})
public class TestSuite {

}
