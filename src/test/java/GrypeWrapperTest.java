import org.junit.Before;
import org.junit.Test;
import pique.utility.PiqueProperties;
import tool.GrypeWrapper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class GrypeWrapperTest {

    private GrypeWrapper grypeWrapper;

    public GrypeWrapperTest(){

    }

    @Before
    public void setup(){
        //load properties
        Properties prop = PiqueProperties.getProperties();
        grypeWrapper = new GrypeWrapper(prop.getProperty("github-token-path"));
    }

    @Test
    public void assess(){
        grypeWrapper.analyze(Paths.get("src/test/resources/dockerfile"));
        //System.out.println();
    }

}
