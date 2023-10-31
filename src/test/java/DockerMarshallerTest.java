import com.github.dockerjava.api.model.Image;
import org.junit.Before;
import org.junit.Test;
import tool.DockerMarshaller;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DockerMarshallerTest {

    public String testImageName = "alpine";
    public String testImageTag = "3.18.4";
    public DockerMarshaller dockerMarshaller;

    public DockerMarshallerTest(){

    }

    @Before
    public void init(){
        dockerMarshaller = new DockerMarshaller();
        dockerMarshaller.initiateDockerClient();
    }

    @Test
    public void testImages(){
        //simple test to make sure the docker marshaller is initiated correctly and returns some images.
        //not really a test, but oh well.
        assertNotNull(dockerMarshaller.getClient().listImagesCmd().exec());
    }


    @Test
    public void getImageFromSearchTerm(){
        String imageName = "alpine";
        String imageTag = "3.15";

        //if a name and a tag is given, return only 1 image
        List<Image> oneImage = dockerMarshaller.getDockerImage(imageName, imageTag);
        assertEquals(oneImage.size(), 1);

        //if only a name is given, return 2 images
        List<Image> twoImages = dockerMarshaller.getDockerImage(imageName);
        assertEquals(twoImages.size(), 2);

    }
}
