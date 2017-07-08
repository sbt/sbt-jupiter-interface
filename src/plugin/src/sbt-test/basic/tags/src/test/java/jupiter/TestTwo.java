package jupiter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
public class TestTwo {

    @Test
    public void a() {
        Reporter.report("two-a");
    }

    @Test
    public void b() {
        Reporter.report("two-b");
    }

}