package tool;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import pique.model.Finding;
import pique.utility.PiqueProperties;
import utilities.HelperFunctions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DiveWrapper extends Tool implements ITool {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiveWrapper.class);


    public DiveWrapper() {
        super("Dive", null);
    }


    @Override
    public Path analyze(Path projectLocation) {
        //workaround because Dive targets images, which are loaded by docker
        String imageName = projectLocation.toString();
        System.out.println("Executing SAT " + this.getName() + " on image: " + imageName);
        LOGGER.debug("Executing SAT " + this.getName() + " on image: " + imageName);
        String imageNameForDirectory = imageName.split(":")[0];
        //set up results dir
        String workingDirectoryPrefix = "";

        try {
            //read output dir from properties file. FIXME we need better properties import
            Properties prop = PiqueProperties.getProperties("src/main/resources/pique-properties.properties");
            Path resultsDir = Paths.get(prop.getProperty("results.directory"));

            workingDirectoryPrefix = resultsDir + "/tool-out/" + imageNameForDirectory + "/";
            Files.createDirectories(Paths.get(workingDirectoryPrefix));
        }catch(java.io.IOException e) {
            e.printStackTrace();
            LOGGER.debug("Error creating directory to save Dive tool results");
            System.out.println("Error creating directory to save Dive tool results");
        }
        File tempResults = new File(workingDirectoryPrefix + "dive-" + imageName + ".json");
        if (tempResults.exists()){
            LOGGER.info("Already ran Dive on: " + imageName + ", results stored in: " + tempResults.toString());
        }else {
            LOGGER.info("Have not run Dive on: " + imageName + ", running now and storing in:" +  tempResults.toString());
            System.out.println("Have not run Dive on: " + imageName + ", running now and storing in:" +  tempResults.toString());
            tempResults.getParentFile().mkdirs();

            String[] cmd = {"dive",
                    imageName,
                    "-j", tempResults.toPath().toAbsolutePath().toString()};
            LOGGER.info(Arrays.toString(cmd));
            try {
                HelperFunctions.getOutputFromProgram(cmd, LOGGER);
            } catch (IOException e) {
                LOGGER.error("Failed to run Dive");
                LOGGER.error(e.toString());
                e.printStackTrace();
            }
        }

        return tempResults.toPath();
    }

    @Override
    public Map<String, Diagnostic> parseAnalysis(Path toolResults) {
        System.out.println("Parsing output from SAT " + this.getName());
        LOGGER.debug("Parsing output from SAT " + this.getName());

        Map<String, Diagnostic> diagnostics = HelperFunctions.initializeDiagnostics(this.getName());

        String results = "";

        try {
            results = HelperFunctions.readFileContent(toolResults);
        } catch (IOException e) {
            LOGGER.info("No results to read from Dive.");
        }

        try {
            JSONObject jsonResults = new JSONObject(results);
            JSONObject metrics = jsonResults.getJSONObject("image");
            //no findings for Dive because Dive metrics capture a summary of the image

            //bigger bug in pique core, this doesn't work because setValue kicks off the calculation of the utility function
            // I should be able to run this, but the default setter of value is overridden to kick off the attached utility function
            //diagnostics.get("Inefficient Bytes Diagnostic Dive").setValue(new BigDecimalWithContext(metrics.get("inefficientBytes").toString()));

            //finding utility function is to use the severity as the value, so rely on that functionality. Will not work for doubles. Damn.
            Finding f1 = new Finding("",0,0, Integer.parseInt(metrics.get("inefficientBytes").toString()));
            f1.setName("inefficientBytes");
            diagnostics.get("Inefficient Bytes Diagnostic Dive").setChild(f1);

            //fails when the efficiency score is a double. FIXME
            //Finding f2 = new Finding("",0,0, Integer.parseInt(metrics.get("efficiencyScore").toString()));
            Finding f2 = new Finding("",0,0, 1);
            f2.setName("efficiencyScore");
            diagnostics.get("Image Efficiency Score Diagnostic Dive").setChild(f2);

            Finding f3 = new Finding("",0,0, Integer.parseInt(metrics.get("sizeBytes").toString()));
            f3.setName("sizeBytes");
            diagnostics.get("Size in Bytes Diagnostic Dive").setChild(f3);

        } catch (JSONException e) {
            LOGGER.warn("Unable to read results from Dive");
        }

        return diagnostics;
    }

    @Override
    public Path initialize(Path toolRoot) {
        return null;
    }
}
