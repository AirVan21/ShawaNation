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
 * Crawler is a class for collecting information from web
 *
 * Currently it just pulls data from specific VK group
 * using VK API
 */
public class Crawler {
    private static int GROUP_ID = -94119361;

    /**
     * Gets vk posts sending VkApi query
     */
    public static List<Post> getVkPosts() {
        final TransportClient transportClient = HttpTransportClient.getInstance();
        final VkApiClient vk = new VkApiClient(transportClient, new Gson());
        final List<Post> result = new ArrayList<>();

        int offset = 0;
        while (true) {
            try {
                GetResponse getResponse = vk.wall()
                        .get()
                        .ownerId(GROUP_ID)
                        .count(100)
                        .offset(offset)
                        .execute();
                if (getResponse.getItems().isEmpty()) {
                    break;
                }
                result.addAll(getResponse.getItems()
                        .stream()
                        .map(Post::new)
                        .collect(Collectors.toList()));
                offset += 100;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
