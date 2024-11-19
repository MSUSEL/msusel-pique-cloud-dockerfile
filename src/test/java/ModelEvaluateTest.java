import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import runnable.SingleProjectEvaluator;

public class ModelEvaluateTest {

    public ModelEvaluateTest(){

    }

    @BeforeClass
    public static void setup(){

    }

    @Test
    public void testEvaluate(){
        //new SingleProjectEvaluator("input/docker-alpine-small-benchmark.json");
        // new SingleProjectEvaluator("input/docker-image-target-single.json");
        //new SingleProjectEvaluator("input/docker-image-target.json");
        new SingleProjectEvaluator("input/docker-image-target-bad-projects.json");
    }

    @AfterClass
    public static void cleanup(){

    }
}
