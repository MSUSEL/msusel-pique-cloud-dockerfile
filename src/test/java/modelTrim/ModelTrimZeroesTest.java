package modelTrim;

import org.junit.Test;
import pique.model.QualityModel;
import pique.model.QualityModelExport;
import pique.model.QualityModelImport;
import pique.utility.PiqueProperties;
import utilities.HelperFunctions;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ModelTrimZeroesTest {

    public ModelTrimZeroesTest() {}


    @Test
    public void testTrimZeroes() {
        //load model --missing, had to delete it from git and tracking due to file size
        Path qmPath = Paths.get("src/test/resources/PIQUECloud-dockerfilequalitymodel.json");
        QualityModelImport qmImport = new QualityModelImport(qmPath);
        QualityModel model = qmImport.importQualityModel();

        QualityModel newModel = HelperFunctions.trimBenchmarkedMeasuresWithNoFindings(model);

        QualityModelExport qmExport = new QualityModelExport(newModel);
        qmExport.exportToJson(newModel.getName() + "_trimmed", Paths.get("src/test/resources"));

    }
}
