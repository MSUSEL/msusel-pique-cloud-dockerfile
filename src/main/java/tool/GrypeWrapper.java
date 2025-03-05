package tool;

import exceptions.DataAccessException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import pique.model.Finding;
import pique.utility.PiqueProperties;
import presentation.PiqueData;
import utilities.HelperFunctions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GrypeWrapper extends Tool implements ITool {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrypeWrapper.class);

    private PiqueData piqueData;

    public GrypeWrapper(PiqueData piqueData) {
        super("Grype", null);
        this.piqueData = piqueData;
    }

    // Methods
    /**
     * @param projectLocation The name of the image to analyze, with format: "name:tag"
     * @return The path to the analysis results file
     */
    @Override
    public Path analyze(Path projectLocation) {
        //workaround because grype targets images, which are loaded by docker
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
            LOGGER.debug("Error creating directory to save Grype tool results");
            System.out.println("Error creating directory to save Grype tool results");
        }
        File tempResults = new File(workingDirectoryPrefix + "grype-" + imageName + ".json");
        if (tempResults.exists()){
            LOGGER.info("Already ran Grype on: " + imageName + ", results stored in: " + tempResults.toString());
        }else {
            LOGGER.info("Have not run Grype on: " + imageName + ", running now and storing in:" +  tempResults.toString());
            System.out.println("Have not run Grype on: " + imageName + ", running now and storing in:" +  tempResults.toString());
            tempResults.getParentFile().mkdirs();

            String[] cmd = {"grype",
                    imageName,
                    "--scope", "all-layers",
                    "-o", "json",
                    "--file", tempResults.toPath().toAbsolutePath().toString()};
            LOGGER.info(Arrays.toString(cmd));
            try {
                HelperFunctions.getOutputFromProgram(cmd, LOGGER);
            } catch (IOException e) {
                LOGGER.error("Failed to run Grype");
                LOGGER.error(e.toString());
                e.printStackTrace();
            }
        }

        return tempResults.toPath();
    }

    /**
     * parses output of tool from analyze().
     *
     * @param toolResults location of the results, output by analyze()
     * @return A Map<String,Diagnostic> with findings from the tool attached. Returns null if tool failed to run.
     */
    @Override
    public Map<String, Diagnostic> parseAnalysis(Path toolResults) {
        System.out.println("Parsing output from SAT " + this.getName());
        LOGGER.debug("Parsing output from SAT " + this.getName());

        Map<String, Diagnostic> diagnostics = HelperFunctions.initializeDiagnostics(this.getName());

        String results = "";

        try {
            results = HelperFunctions.readFileContent(toolResults);
        } catch (IOException e) {
            LOGGER.info("No results to read from Grype.");
        }
        try {
            JSONObject jsonResults = new JSONObject(results);
            JSONArray matches = jsonResults.optJSONArray("matches");

            // if vulnerabilities is null we had no findings, thus return
            if (matches == null) {
                return diagnostics;
            }

            for (int i = 0; i < matches.length(); i++) {
                JSONObject jsonFinding = ((JSONObject) matches.get(i)).optJSONObject("vulnerability");
                String findingName = jsonFinding.get("id").toString();
                LOGGER.info("Found vulnerability: " + findingName);
                String findingSeverity = jsonFinding.get("severity").toString();
                String[] findingNames;
                String[] findingSeverities;
                if (findingName.substring(0,4).equals("GHSA")){
                    //found a GHSA, find the relatedVulnerabilities field for the equivalent CVE
                    LOGGER.info("\tConverting GHSA vulnerability to CVE vulnerability");
                    JSONArray cveMappings = ((JSONObject) matches.get(i)).optJSONArray("relatedVulnerabilities");
                    findingNames = new String[cveMappings.length()];
                    findingSeverities = new String[cveMappings.length()];
                    //set finding names of first/default first
                    findingName = ((JSONObject)cveMappings.get(0)).get("id").toString();
                    findingSeverity = ((JSONObject)cveMappings.get(0)).get("severity").toString();
                    findingNames[0] = findingName;
                    findingSeverities[0] = findingSeverity;
                    for (int j = 1; j < cveMappings.length(); j++) {
                        findingNames[j] = ((JSONObject)cveMappings.get(j)).get("id").toString();
                        findingSeverities[j] = ((JSONObject)cveMappings.get(j)).get("severity").toString();
                    }
                    LOGGER.info("\tConverted GHSA to CVE: " + findingName);
                }else{
                    //no GHSA
                    findingNames = new String[]{findingName};
                    findingSeverities = new String[]{findingSeverity};
                }
                if (findingNames.length > 1){
                    LOGGER.warn("Found more than one CVE for a GHSA vulnerability, unsure how to aggregate this, but only considering the first cve for now");
                    findingName = findingNames[0];
                    findingSeverity = findingSeverities[0];
                }
                List<String> cwes = new ArrayList<>();
                //FIXME--- remove try catch block after checked exceptions changes
                try {
                    //do we have a cwe for this cve?
                    System.out.println("checking PIQUE data for cwe for CVE: " + findingName);
                    System.out.println("pique data reporting: " + piqueData.getCweName(findingName));
                    cwes = piqueData.getCweName(findingName);
                }catch (DataAccessException e){
                    LOGGER.info(findingName + " has no NVD page, page likely reserved by a CNA. Skipping.");
                }
                // are CWE's unique? In some cases the NVD reports the same CWE from a Primary and Secondary source
                // we want only unique CWEs
                Set<String> cweSet = new HashSet<>(cwes);
                cwes.clear();
                cwes.addAll(cweSet);

                int uniqueFindingCounter = 0;
                for (String matchedCWE : cwes){
                    //do we have a diag for this matchedCWE? (Is it in the model definition?)
                    Diagnostic diag = diagnostics.get(matchedCWE + " Diagnostic Grype");
                    if (diag != null) {
                        //found a cwe node for this in the model definition, creating a finding for it and adding.
                        LOGGER.info("Found " + matchedCWE + " in the model definition for our " + findingName);
                        Finding finding = new Finding("" + uniqueFindingCounter,0,0, this.severityToInt(findingSeverity));
                        diag.setChild(finding);
                        uniqueFindingCounter++;
                    }else{
                        //this means that either it is unknown, mapped to a CWE outside of the expected results, or is not assigned a CWE
                        // We may want to treat this in another way in the future, but im ignoring it for now.
                        LOGGER.warn("Vulnerability " + findingName + " with CWE: " + matchedCWE + "  outside of CWE-1000 was found. Ignoring this CVE.");
                    }
                }
            }
        } catch (JSONException e) {
            LOGGER.warn("Unable to read results from Grype");
        }

        return diagnostics;
    }

    /**
     * Initializes the tool by installing it through python pip from the command line.
     */
    @Override
    public Path initialize(Path toolRoot) {
        final String[] cmd = {"grype", "version"};

        try {
            HelperFunctions.getOutputFromProgram(cmd, LOGGER);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Failed to initialize " + this.getName());
            LOGGER.error(e.getStackTrace().toString());
        }

        return toolRoot;
    }


    //maps low-critical to numeric values based on the highest value for each range.
    private Integer severityToInt(String severity) {
        Integer severityInt = 1;
        switch(severity.toLowerCase()) {
            case "low": {
                severityInt = 1;
                break;
            }
            case "medium": {
                severityInt = 3;
                break;
            }
            case "high": {
                severityInt = 6;
                break;
            }
            case "critical": {
                severityInt = 10;
                break;
            }
        }

        return severityInt;
    }
}
