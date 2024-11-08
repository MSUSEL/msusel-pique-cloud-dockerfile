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
        Double delta = 0.0005;

        Path alpine3_15_output = diveWrapper.analyze(Paths.get("alpine:3.15.0"));
        //maybe one or two tests to see if dive ran correctly

        Map<String, Diagnostic> results = diveWrapper.parseAnalysis(alpine3_15_output);

        Diagnostic d1 = results.get("Inefficient Bytes Diagnostic Dive");
        assertEquals(0, d1.getValue().doubleValue(), delta);

        Diagnostic d2 = results.get("Image Efficiency Score Diagnostic Dive");
        assertEquals(1, d2.getValue().doubleValue(), delta);

        Diagnostic d3 = results.get("Size in Bytes Diagnostic Dive");
        assertEquals(5580949, d3.getValue().doubleValue(), delta);

    }
}
