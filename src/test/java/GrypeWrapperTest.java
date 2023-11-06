import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pique.model.Diagnostic;
import pique.utility.PiqueProperties;
import tool.GrypeWrapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
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
    public void runGrype(){
        Path alpine3_15_output = grypeWrapper.analyze(Paths.get("alpine:3.15"));
        //maybe one or two test to see if grype ran correctly

        Map<String, Diagnostic> results = grypeWrapper.parseAnalysis(alpine3_15_output);
    }

}
