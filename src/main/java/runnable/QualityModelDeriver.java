package runnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.model.QualityModel;
import pique.model.QualityModelExport;
import pique.model.QualityModelImport;
import pique.runnable.AQualityModelDeriver;
import pique.utility.PiqueProperties;
import presentation.PiqueData;
import presentation.PiqueDataFactory;
import tool.DiveWrapper;
import tool.GrypeWrapper;
import tool.TrivyWrapper;
import utilities.HelperFunctions;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QualityModelDeriver extends AQualityModelDeriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(QualityModelDeriver.class);

    public static void main(String[] args){
        new QualityModelDeriver();
    }

    public QualityModelDeriver(String propertiesPath){
        init(propertiesPath);
    }

    public QualityModelDeriver(){
        init(null);
    }

    private void init(String propertiesPath){
        LOGGER.info("Beginning deriver");

        Properties prop = null;
        try {
            prop = propertiesPath==null ? PiqueProperties.getProperties() : PiqueProperties.getProperties(propertiesPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Path blankqmFilePath = Paths.get(prop.getProperty("blankqm.filepath"));
        Path derivedModelFilePath = Paths.get(prop.getProperty("results.directory"));

        // Initialize objects
        String projectRootFlag = "";
        Path benchmarkRepo = Paths.get(prop.getProperty("benchmark.repo"));

        PiqueData piqueData = new PiqueDataFactory(prop.getProperty("database-credentials")).getPiqueData();

        ITool gyrpeWrapper = new GrypeWrapper(piqueData);
        ITool trivyWrapper = new TrivyWrapper(piqueData);
        ITool diveWrapper = new DiveWrapper();
        Set<ITool> tools = Stream.of(gyrpeWrapper, trivyWrapper, diveWrapper).collect(Collectors.toSet());
        QualityModelImport qmImport = new QualityModelImport(blankqmFilePath);
        QualityModel qmDescription = qmImport.importQualityModel();

        QualityModel derivedQualityModel = deriveModel(qmDescription, tools, benchmarkRepo, projectRootFlag);

        Path jsonOutput = new QualityModelExport(derivedQualityModel)
                .exportToJson(derivedQualityModel
                        .getName(), derivedModelFilePath);
        LOGGER.info("Quality Model derivation finished. You can find the file at " + jsonOutput.toAbsolutePath());



        //uncomment when we decide to remove trimming or not

        QualityModel trimmedDerivedQualityModel = HelperFunctions.trimBenchmarkedMeasuresWithNoFindings(derivedQualityModel);

        Path trimmedJsonOutput = new QualityModelExport(trimmedDerivedQualityModel)
                .exportToJson(trimmedDerivedQualityModel
                        .getName() + "_trimmed", derivedModelFilePath);

        LOGGER.info("Quality Model derivation finished with trimmed model. You can find the file at " + trimmedJsonOutput.toAbsolutePath());
        System.out.println("Quality Model derivation finished with trimmed model. You can find the file at " + trimmedJsonOutput.toAbsolutePath());


    }



}
