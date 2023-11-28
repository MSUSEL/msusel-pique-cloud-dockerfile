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
        new SingleProjectEvaluator("input/docker-image-target.json");
    }

    @AfterClass
    public static void cleanup(){

    }
}
