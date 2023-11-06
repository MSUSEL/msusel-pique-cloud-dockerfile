import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pique.utility.PiqueProperties;
import tool.GrypeWrapper;

import java.nio.file.Paths;
import java.util.Properties;

public class GrypeWrapperTest {

    private static GrypeWrapper grypeWrapper;

    public GrypeWrapperTest(){

    }

    @BeforeClass
    public static void setup(){
        //load properties
        Properties prop = PiqueProperties.getProperties();
        grypeWrapper = new GrypeWrapper(prop.getProperty("github-token-path"));

        //load docker marshaller
        DockerMarshallerTest.init();
    }

    @Test
    public void assess(){
        grypeWrapper.analyze(Paths.get("alpine:3.15"));
        //System.out.println();
    }

}
