import org.junit.Test;
import pique.utility.PiqueProperties;
import presentation.PiqueData;
import presentation.PiqueDataFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class PIQUEDataAccessor {

    protected PiqueData piqueData;
    protected String propertiesLocation = "src/test/resources/pique-test-properties.properties";

    public PIQUEDataAccessor() {
        Properties prop = null;
        try {
            prop = propertiesLocation == null ? PiqueProperties.getProperties() : PiqueProperties.getProperties(propertiesLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        piqueData = new PiqueDataFactory(prop.getProperty("database-credentials")).getPiqueData();
    }

    @Test
    public void getCWEsFromCVE(){
        List<String> cwes = piqueData.getCweName("CVE-2016-5002");

        cwes.forEach(cwe -> System.out.println(cwe));
    }
}
