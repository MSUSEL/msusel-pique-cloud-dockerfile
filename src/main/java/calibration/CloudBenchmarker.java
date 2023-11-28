package calibration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.calibration.AbstractBenchmarker;
import pique.calibration.IBenchmarker;
import pique.evaluation.BenchmarkMeasureEvaluator;
import pique.evaluation.Project;
import pique.model.Diagnostic;
import pique.model.QualityModel;
import utilities.helperFunctions;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/***
 * Unlike previous pique models which point to a directory of targets for the benchmark repository,
 * pique-cloud will point to a json file of docker images and versions. Because of this, I need a custom
 * parser to build a listing of docker image:version, and rely on the docker marshaller to download each image
 * in the listing.
 */
public class CloudBenchmarker extends AbstractBenchmarker implements IBenchmarker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudBenchmarker.class);

    @Override
    public Map<String, BigDecimal[]> deriveThresholds(Path benchmarkRepository, QualityModel qmDescription, Set<ITool> tools, String projectRootFlag) {
        // Import list of docker images from flat .json file to a set of strings
        Set<Path> dockerImages = helperFunctions.getDockerImagesToAnalyze(benchmarkRepository);

        System.out.println("* Beginning repository benchmark analysis");
        System.out.println(dockerImages.size() + " projects to analyze.\n");

        ArrayList<Project> projects = analyzeProjects(dockerImages, qmDescription, tools);
        // Map all values audited for each measure
        Map<String, ArrayList<BigDecimal>> measureBenchmarkData = mapMeasureValues(projects);

        Map<String, BigDecimal[]> measureThresholds = calculateThresholds(measureBenchmarkData);
        return measureThresholds;
    }

    @Override
    protected ArrayList<Project> analyzeProjects(Set<Path> dockerImages, QualityModel qmDescription, Set<ITool> tools) {
        ArrayList<Project> projects = new ArrayList<>();
        int totalProjects = dockerImages.size();
        int counter = 0;
        //Remember that Paths are not on-file paths but rather a string with format "image:version".
        for (Path dockerImage : dockerImages) {
            String imageName = dockerImage.toString();
            counter++;

            // Clone the QM
            // TODO (1.0): Currently need to use .clone() for benchmark repository quality model sharing. This will be
            //  confusing and problematic to people not using the default benchmarker.
            QualityModel clonedQM = qmDescription.clone();

            // Instantiate new project object
            Project project = new Project(imageName, clonedQM);

            // TODO: temp fix
            // Set measures to not use a utility function during their node evaluation
            project.getQualityModel().getMeasures().values().forEach(measure -> {
                measure.setEvaluatorObject(new BenchmarkMeasureEvaluator());
            });

            // Run the static analysis tools process
            Map<String, Diagnostic> allDiagnostics = runToolsOnTarget(tools, dockerImage);

            // Apply collected diagnostics (containing findings) to the project
            allDiagnostics.forEach((diagnosticName, diagnostic) -> {
                project.addFindings(diagnostic);
            });

            // Evaluate project up to Measure level (normalize does happen first)
            project.evaluateMeasures();

            // Add new project (with tool findings information included) to the list
            projects.add(project);

            // Print information
            System.out.println("\n\tFinished analyzing project " + project.getName());
            System.out.println("\t" + counter + " of " + totalProjects + " analyzed.\n");
        }
        return projects;
    }

    @Override
    public Map<String, BigDecimal[]> calculateThresholds(Map<String, ArrayList<BigDecimal>> measureBenchmarkData) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
