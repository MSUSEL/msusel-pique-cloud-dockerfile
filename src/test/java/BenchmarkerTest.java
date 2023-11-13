import calibration.CloudBenchmarker;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pique.utility.PiqueProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;

public class BenchmarkerTest {



    public BenchmarkerTest(){

    }


    @Test
    public void testBenchmarkImport(){
        CloudBenchmarker benchmarker = new CloudBenchmarker();
        Properties prop = PiqueProperties.getProperties();
        Set<Path> dockerImages = benchmarker.getDockerImagesToAnalyze(Paths.get(prop.getProperty("benchmark.repo")));

    }
}
