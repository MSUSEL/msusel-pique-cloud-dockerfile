
import org.junit.BeforeClass;
import org.junit.Test;
import pique.model.Diagnostic;
import pique.model.Finding;
import pique.utility.PiqueProperties;
import presentation.PiqueData;
import presentation.PiqueDataFactory;
import tool.GrypeWrapper;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class GrypeWrapperTest {

    private static GrypeWrapper grypeWrapper;


    private static PiqueData piqueData;

    public GrypeWrapperTest(){

    }

    @BeforeClass
    public static void setup(){
        //load properties
        Properties prop = PiqueProperties.getProperties();

        System.out.println(prop.getProperty("database-credentials"));
        piqueData = new PiqueDataFactory(prop.getProperty("database-credentials")).getPiqueData();

        grypeWrapper = new GrypeWrapper(piqueData);

        //load docker marshaller
        DockerMarshallerTest.init();
    }

    @Test
    public void runGrype(){
        Path alpine3_15_output = grypeWrapper.analyze(Paths.get("alpine:3.15"));
        //maybe one or two tests to see if grype ran correctly

        Map<String, Diagnostic> results = grypeWrapper.parseAnalysis(alpine3_15_output);

        Diagnostic d = results.get("CWE-787 Diagnostic Grype");
        // one finding coming from alpine:3.15, but it appears twice. Not sure why but this is a Grype issue I think..
        assertEquals(1, d.getChildren().keySet().size());
        // check that one finding matches with our expectations (name + critical severity + value)
        Finding f = (Finding) d.getChild("CVE-2022-48174");
        assertEquals(10, f.getSeverity());
        assertEquals(new BigDecimal(10), f.getValue());

        //check the value is being aggregated correctly to diagnostic value
        assertEquals(new BigDecimal(10.0).stripTrailingZeros(), d.getValue().stripTrailingZeros());

    }

}
