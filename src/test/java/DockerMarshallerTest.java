import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import tool.DockerMarshaller;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DockerMarshallerTest {

    public static String alpineTestName = "alpine";
    public static String[] alpineTestVersions = new String[]{"3.18.4", "3.15"};
    public static DockerMarshaller dockerMarshaller;

    public DockerMarshallerTest(){

    }

    public static void init() {
        //initialize dockerMarshaller
        dockerMarshaller = new DockerMarshaller();
        dockerMarshaller.initiateDockerClient();
    }

    @BeforeClass
    public static void pullFreshTestImages(){
        init();
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
    @AfterClass
    public static void deleteExistingTestImages(){
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
            System.out.println("Image: " + Arrays.toString(toDelete.getRepoTags()) + " was successfully removed");
        }
    }

    @Test
    public void testImages(){
        //simple test to make sure the docker marshaller is initiated correctly and returns some images.
        //not really a test, but because of very little documentation with docker-java I am shooting in the dark
        assertNotNull(dockerMarshaller.getClient().listImagesCmd().exec());
    }

    @Test
    public void getImageFromSearchTerm(){

        //if a name and a tag is given, return only 1 image
        Image oneImage = dockerMarshaller.getDockerImageFromName(alpineTestName, alpineTestVersions[0]);
        assertNotNull(oneImage);
        //if only a name is given, return number of images equal to the number of test versions in alpineTestVersions
        List<Image> twoImages = dockerMarshaller.getDockerImagesFromName(alpineTestName);
        assertEquals(alpineTestVersions.length, twoImages.size());

    }

    /***
     * Helper method to print all meta data from an image
     * @param image Image to print meta data of
     */
    private void printImageTags(Image image){
        for (String s : image.getRawValues().keySet()){
            System.out.println("Key: " + s + " ::: Value: " +  image.getRawValues().get(s));
        }
    }
}
