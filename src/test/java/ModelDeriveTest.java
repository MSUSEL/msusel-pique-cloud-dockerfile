import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import pique.model.Finding;
import runnable.QualityModelDeriver;

public class ModelDeriveTest {

    public ModelDeriveTest(){

    }

    @BeforeClass
    public static void setup(){

    }

    @Test
    public void deriveModel(){
        // kick off model deriver
        QualityModelDeriver deriver = new QualityModelDeriver("src/test/resources/pique-test-properties.properties");

    }

    @AfterClass
    public static void cleanup(){

    }


}
