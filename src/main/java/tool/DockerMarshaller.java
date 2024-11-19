package tool;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/***
 * class responsible for managing docker images so that other tools can run via targetting docker images
 * or running containers
 */
public class DockerMarshaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerMarshaller.class);

    @Getter
    private static DockerClient client;

    public DockerMarshaller(){

    }

    public static void initiateDockerClient(){
        try{
            client = DockerClientBuilder.getInstance().build();
        }catch(Exception e){
            LOGGER.error("Docker client initiation failed. Check configs");
        }
    }

    /***
     * returns a single docker image
     * @param imageName name of image
     * @param imageTag tag of image
     * @return single docker Image object
     */
    public Image getDockerImageFromName(String imageName, String imageTag){
        String searchQuery = imageName + ":" + imageTag;
        List<Image> images = getDockerImagesFromName(searchQuery);
        if (images.size() != 1){
            System.out.println("Two images with the same name and tag, might need to look at using hash as identifier, not name:tag");
        }
        return images.get(0);
    }

    public static void downloadDockerImageFromAzureContainerRegistry(String imageNameWithTag){
        if (client == null){
            initiateDockerClient();
        }
        try {
            //see if image exists on local listing
            if (getDockerImagesFromName(imageNameWithTag).isEmpty()){
                //image did not exist on local machine
                System.out.println("Image: " + imageNameWithTag + " not found locally.");
                LOGGER.info("Image: " + imageNameWithTag + " not found locally.");
                System.out.println("Attempting to download image: " + imageNameWithTag + " from Azure Container Registry");
                LOGGER.info("Attempting to download image: " + imageNameWithTag + " from Azure Container Registry");
                client.pullImageCmd(imageNameWithTag.split(":")[0]).withTag(imageNameWithTag.split(":")[1]).withRegistry("azure").exec(new PullImageResultCallback() {
                    @Override
                    public void onNext(PullResponseItem item) {
                        super.onNext(item);
                    }
                }).awaitCompletion(5, TimeUnit.MINUTES);
            }
        }catch (InterruptedException interruptedException){
            System.out.println("Timeout pulling docker images from DockerHub, with a 5 minute timeout. Perhaps DockerHub is down?");
        }
    }

    public static void downloadDockerImageFromDockerHub(String imageNameWithTag){
        if (client == null){
            initiateDockerClient();
        }
        try {
            //see if image exists on local listing
            if (getDockerImagesFromName(imageNameWithTag).isEmpty()){
                //image did not exist on local machine
                System.out.println("Attempting to download image: " + imageNameWithTag + " from DockerHub");
                client.pullImageCmd(imageNameWithTag.split(":")[0]).withTag(imageNameWithTag.split(":")[1]).exec(new PullImageResultCallback() {
                    @Override
                    public void onNext(PullResponseItem item) {
                        super.onNext(item);
                    }
                }).awaitCompletion(5, TimeUnit.MINUTES);
            }
        }catch (InterruptedException interruptedException){
            LOGGER.error("Timeout pulling docker images from DockerHub, with a 5 minute timeout. Perhaps DockerHub is down?");
            LOGGER.error("\t" + interruptedException);
            System.out.println("Timeout pulling docker images from DockerHub, with a 5 minute timeout. Perhaps DockerHub is down?");
        }catch (InternalServerErrorException internalServerErrorException){
            LOGGER.error("Internal dockerjava-library service error. Perhaps the image does not exist?");
            LOGGER.error("\t" + internalServerErrorException);
            System.out.println("Internal dockerjava-library service error. Perhaps the image does not exist?");

        }
    }

    /***
     * Delete image, for use after tool processing to save on system space.
     *
     * @param imageNameWithTag name and tag of docker image (i.e., "alpine:3.15"
     */
    public static void deleteDockerImageAfterProcessing(String imageNameWithTag){
        if (client == null){
            initiateDockerClient();
        }
        try {
            client.removeImageCmd(imageNameWithTag).withForce(true).exec();
        }catch (NotFoundException e){
            LOGGER.info("Image: " + imageNameWithTag + " does not exist on the filesystem, skipping removal and continuing.");
        }
    }

    /***
     * returns a list of docker images under the common image name. Will return zero to many images
     * @param imageName name of image
     * @return List of docker Image objects
     */
    public static List<Image> getDockerImagesFromName(String imageName){
        List<Image> images = null;
        try{
            images = client.listImagesCmd().withReferenceFilter(imageName).exec();
            System.out.println("Search query: " +  imageName + " leads to: " + images.size() + " images already on filesystem");

        }catch(Exception e){
            LOGGER.error("Failed to retrieve docker image with search");
        }
        return images;
    }

    public Image getDockerImageFromRepoDigest(String sha256){
        List<Image> images = null;
        try{

            images = client.listImagesCmd().exec();


            System.out.println("Search query: " +  sha256 + " leads to size: " + images.size());

        }catch(Exception e){
            LOGGER.error("Failed to retrieve docker image with search");
        }
        return images.get(0);
    }

}
