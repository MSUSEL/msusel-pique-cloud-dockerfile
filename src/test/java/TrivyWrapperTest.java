import org.junit.BeforeClass;
import org.junit.Test;
import pique.model.Diagnostic;
import pique.model.Finding;
import pique.utility.PiqueProperties;
import presentation.PiqueData;
import presentation.PiqueDataFactory;
import tool.GrypeWrapper;
import tool.TrivyWrapper;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TrivyWrapperTest {

    private static TrivyWrapper trivyWrapper;

    private static PiqueData piqueData;

    public TrivyWrapperTest(){

    }

    @BeforeClass
    public static void setup(){
        //load properties
        Properties prop = PiqueProperties.getProperties();

        piqueData = new PiqueDataFactory(prop.getProperty("database-credentials")).getPiqueData();
        trivyWrapper = new TrivyWrapper(piqueData);
        //load docker marshaller
        DockerMarshallerTest.init();
    }

    @Test
    public void runTrivy(){
        Path alpine3_16_output = trivyWrapper.analyze(Paths.get("alpine:3.16.0"));
        System.out.println(alpine3_16_output);
        //maybe one or two tests to see if trivy ran correctly

        Map<String, Diagnostic> results = trivyWrapper.parseAnalysis(alpine3_16_output);


        Diagnostic d = results.get("CWE-295 Diagnostic Trivy");
        // one finding coming from alpine:3.15
        assertEquals(1, d.getChildren().keySet().size());
        // check that the finding matches with our expectations (name + critical severity + value)
        Finding f = (Finding) d.getChild("CVE-2023-0464");

        assertEquals(7, f.getSeverity());
        assertEquals(new BigDecimal(7), f.getValue());

        //check the value is being aggregated correctly to diagnostic value
        assertEquals(new BigDecimal(7.0).stripTrailingZeros(), d.getValue().stripTrailingZeros());

    }
}
