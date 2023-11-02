package tool;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/***
 * class responsible for managing docker images so that other tools can run via targetting docker images
 * or running containers
 */
public class DockerMarshaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerMarshaller.class);

    @Getter
    private DockerClient client;

    public DockerMarshaller(){

    }

    public void initiateDockerClient(){
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
            System.out.println("");
        }
        return images.get(0);
    }

    /***
     * returns a list of docker images under the common image name. Will return zero to many images
     * @param imageName name of image
     * @return List of docker Image objects
     */
    public List<Image> getDockerImagesFromName(String imageName){
        List<Image> images = null;
        try{
            images = client.listImagesCmd().withReferenceFilter(imageName).exec();
            System.out.println("Search query: " +  imageName + " leads to size: " + images.size());

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
