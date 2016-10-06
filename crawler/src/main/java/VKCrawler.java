import com.google.gson.Gson;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;

import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

public class VKCrawler {
    private static int GROUP_ID = -94119361;

    private static void makeDump() {
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient, new Gson());

//        UserActor actor = new UserActor(USER_ID, TOKEN);
        File output = new File("raw_small_dirty_dump");
        try {
            if (!output.exists()) {
                Files.createFile(output.toPath());
            }
            FileWriter fw = new FileWriter(output.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            GetResponse getResponse = vk.wall().get().ownerId(GROUP_ID).count(100).execute();
            for (WallpostFull post : getResponse.getItems()) {
                bw.append("$TEXT$\n");
                bw.append(post.getText());
                bw.append("\n");
                bw.append("==============\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        makeDump();
    }
}
