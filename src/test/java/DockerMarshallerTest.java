import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import tool.DockerMarshaller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DockerMarshallerTest {

    public String alpineTestName = "alpine";
    public String[] alpineTestVersions = new String[]{"3.18.4", "3.15"};
    public DockerMarshaller dockerMarshaller;

    public DockerMarshallerTest(){

    }

    @Before
    public void init() {
        //initialize dockerMarshaller
        dockerMarshaller = new DockerMarshaller();
        dockerMarshaller.initiateDockerClient();


        deleteExistingTestImages();
        //pull fresh alpine images from dockerhub for testing
        pullFreshTestImages();

        //identify if existing

    }

    private void pullFreshTestImages(){
        Map<String, String[]> testImageTags = new HashMap<>();
        testImageTags.put(alpineTestName, alpineTestVersions);

        for (String name : testImageTags.keySet()){
            for (String version : testImageTags.get(name)){
                try {
                    dockerMarshaller.getClient().pullImageCmd(name).withTag(version).exec(new PullImageResultCallback() {
                        @Override
                        public void onNext(PullResponseItem item) {
                            super.onNext(item);
                        }
                    }).awaitCompletion(5, TimeUnit.MINUTES);
                }catch (InterruptedException interruptedException){
                    System.out.println("Timeout pulling docker images... Might be taking a long time to pull and install all the images.");
                }
            }
        }

    }

    /***
     * test helper method to delete any existing images with the same Tag (name:version) as some test images
     */
    private void deleteExistingTestImages(){
        Map<String, String[]> testImageTags = new HashMap<>();
        testImageTags.put(alpineTestName, alpineTestVersions);

        List<Image> existingImages = dockerMarshaller.getClient().listImagesCmd().exec();
        List<Image> flaggedForDeletion = new ArrayList<>();
        for (Image image : existingImages){
            for (String name : testImageTags.keySet()){
                for (String version : testImageTags.get(name)){
                    String target = name + ":" + version;
                    for (String repoTag : image.getRepoTags()){
                        if (repoTag.equals(target)){
                            flaggedForDeletion.add(image);
                        }
                    }
                }
            }
        }
        for (Image toDelete : flaggedForDeletion){
            dockerMarshaller.getClient().removeImageCmd(toDelete.getRepoTags()[0]).exec();
        }
    }

    @Test
    public void testImages(){
        //simple test to make sure the docker marshaller is initiated correctly and returns some images.
        //not really a test, but oh well.
        assertNotNull(dockerMarshaller.getClient().listImagesCmd().exec());
    }

    private void printImageTags(Image image){
        for (String s : image.getRawValues().keySet()){
            System.out.println("Key: " + s + " ::: Value: " +  image.getRawValues().get(s));
        }
    }


    @Test @Ignore
    public void getImageFromSearchTerm(){

        //if a name and a tag is given, return only 1 image
        Image oneImage = dockerMarshaller.getDockerImageFromName(alpineTestName, alpineTestVersions[0]);
        printImageTags(oneImage);
        System.exit(0);
        for (String s : oneImage.getRepoDigests()){
            System.out.println(s);
        }
        System.exit(0);
        //

        //assertEquals(oneImage.getRawValues().get(""), );

        //if only a name is given, return 2 images
        List<Image> twoImages = dockerMarshaller.getDockerImagesFromName(alpineTestName);
        assertEquals(twoImages.size(), 2);

    }

    @Test @Ignore
    public void getImageFromID(){
        Image oneImage = dockerMarshaller.getDockerImageFromRepoDigest("sha256:48d9183eb12a05c99bcc0bf44a003607b8e941e1d4f41f9ad12bdcc4b5672f86");

    }
}
