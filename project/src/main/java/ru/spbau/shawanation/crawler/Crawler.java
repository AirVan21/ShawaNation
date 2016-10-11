package ru.spbau.shawanation.crawler;

import com.google.gson.Gson;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;

import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;

/**
 * Crawler is a class for collecting
 */
public class Crawler {
    private static int GROUP_ID = -94119361;

    public static void makeDump() {
        final TransportClient transportClient = HttpTransportClient.getInstance();
        final VkApiClient vk = new VkApiClient(transportClient, new Gson());

        try {
            GetResponse getResponse = vk.wall()
                    .get()
                    .ownerId(GROUP_ID)
                    .count(100)
                    .execute();
            for (WallpostFull post : getResponse.getItems()) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
