package ru.spbau.shawanation.crawler;

import com.google.gson.Gson;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;

import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import ru.spbau.shawanation.database.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Crawler is a class for collecting
 */
public class Crawler {
    private static int GROUP_ID = -94119361;

    /**
     * Gets vk posts sending VkApi query
     * @return
     */
    public static List<Post> getVkPosts() {
        final TransportClient transportClient = HttpTransportClient.getInstance();
        final VkApiClient vk = new VkApiClient(transportClient, new Gson());
        final List<Post> result = new ArrayList<>();

        try {
            GetResponse getResponse = vk.wall()
                    .get()
                    .ownerId(GROUP_ID)
                    .count(100)
                    .execute();
            result.addAll(getResponse.getItems()
                    .stream()
                    .map(Post::new)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
