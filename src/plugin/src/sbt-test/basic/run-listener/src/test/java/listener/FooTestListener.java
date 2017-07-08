package listener;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.io.Writer;
import java.io.FileWriter;

public class FooTestListener implements TestExecutionListener {

    @Override
    public void executionFinished(TestIdentifier identifier, TestExecutionResult result) {

        try {

            String name = identifier.getDisplayName();

            Writer writer = new FileWriter("target/testsrun", true);
            writer.write(name + "\n");
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}