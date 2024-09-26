import org.junit.BeforeClass;
import org.junit.Test;
import pique.model.Diagnostic;
import pique.model.Finding;
import pique.utility.PiqueProperties;
import presentation.PiqueData;
import presentation.PiqueDataFactory;
import tool.DiveWrapper;
import tool.GrypeWrapper;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class DiveWrapperTest {


    private static DiveWrapper diveWrapper;


    public DiveWrapperTest(){

    }

    @BeforeClass
    public static void setup(){
        //load properties
        Properties prop = PiqueProperties.getProperties();

        diveWrapper = new DiveWrapper();

        //load docker marshaller
        DockerMarshallerTest.init();
    }

    @Test
    public void runDive(){
        Path alpine3_15_output = diveWrapper.analyze(Paths.get("alpine:3.15"));
        //maybe one or two tests to see if dive ran correctly

        Map<String, Diagnostic> results = diveWrapper.parseAnalysis(alpine3_15_output);

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
