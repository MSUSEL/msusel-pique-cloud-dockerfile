import calibration.CloudBenchmarker;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pique.analysis.ITool;
import pique.evaluation.Project;
import pique.model.QualityModel;
import pique.utility.PiqueProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

public class BenchmarkerTest {

    private static CloudBenchmarker benchmarker;



    public BenchmarkerTest(){

    }

    @BeforeClass
    public static void setup(){
        benchmarker = new CloudBenchmarker();
    }


    @Test
    public void testBenchmarkImport(){
        Properties prop = PiqueProperties.getProperties();
        Set<Path> dockerImages = benchmarker.getDockerImagesToAnalyze(Paths.get(prop.getProperty("benchmark.repo")));

    }

    public void testAnalyzeProjects(){

       // ArrayList<Project> projects = benchmarker.analyzeProjects(Set<Path> dockerImages, QualityModel qmDescription, Set< ITool > tools)

    }
}
