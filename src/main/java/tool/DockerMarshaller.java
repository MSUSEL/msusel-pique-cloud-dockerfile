package tool;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.ImageInputStream;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Image> getDockerImage(String imageName, String imageTag){
        String searchQuery = imageName + ":" + imageTag;
        return getDockerImage(searchQuery);
    }

    public List<Image> getDockerImage(String imageName){
        List<Image> images = null;
        try{
            List<SearchItem> results = client.searchImagesCmd( imageName).exec();

            //convert results from search command (In a SearchItem class)to an Image class
            images = client.listImagesCmd().withReferenceFilter( imageName).exec();
            System.out.println("Search query: " +  imageName + " leads to size: " + images.size());

        }catch(Exception e){
            LOGGER.error("Failed to retrieve docker image with search");
        }
        return images;
    }

    private List<Image> getImagesFromSearchItem(List<SearchItem> results){
        List<Image> allImages = client.listImagesCmd().exec();
        for (Image image: allImages) {
            System.out.println(results.get(0).getName());
        }
        System.exit(0);
        return null;
    }

}
