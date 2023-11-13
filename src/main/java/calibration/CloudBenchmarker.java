package calibration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.calibration.AbstractBenchmarker;
import pique.calibration.IBenchmarker;
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
        Set<Path> dockerImages = getDockerImagesToAnalyze(benchmarkRepository);

        return null;
    }

    @Override
    public Map<String, BigDecimal[]> calculateThresholds(Map<String, ArrayList<BigDecimal>> measureBenchmarkData) {
        return null;
    }

    /***
     *
     * @param benchmarkRepository
     *              import a path to a file that contains meta-data information about which image to download
     * @return
     *              awkward, but return a set of Paths, where each Path is a string of format "imageName:version"
     *              This is not a valid Path on disk, it is a workaround because of PIQUE core's heavy reliance
     *              on things existing on disk before analysis.
     */
    public Set<Path> getDockerImagesToAnalyze(Path benchmarkRepository){
        Set<Path> images = new HashSet<>();
        try {
            JSONObject jsonResults = new JSONObject(helperFunctions.readFileContent(benchmarkRepository));
            JSONArray perProject = jsonResults.getJSONArray("images");

            for (int i = 0; i < perProject.length(); i++){
                JSONObject obj = perProject.getJSONObject(i);
                //get versions
                JSONArray jsonVersions = obj.getJSONArray("versions");
                for (int j = 0; j < jsonVersions.length(); j++){
                    images.add(Paths.get(obj.getString("name") + ":" + jsonVersions.getString(j)));
                }
            }

            System.out.println(images);
        }catch(IOException e){
            LOGGER.info("No benchmark data to read in");

        }catch (JSONException e) {
            LOGGER.info("Improper JSON format in benchmark repository file");
        }
        return images;
    }

    @Override
    public String getName() {
        return null;
    }
}
