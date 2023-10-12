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

    public List<Image> getDockerImage(String imageName, String imageTag){
        List<Image> images = null;
        try{
            images = client.listImagesCmd().exec();
        }catch(Exception e){
            LOGGER.error("Failed to retrive docker image with search");
        }
        return images;
    }

}
