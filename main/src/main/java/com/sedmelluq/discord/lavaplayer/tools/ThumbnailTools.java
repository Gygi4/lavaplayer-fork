package com.sedmelluq.discord.lavaplayer.tools;

import java.util.List;

public class ThumbnailTools {

    public static String getYouTubeMusicThumbnail(JsonBrowser videoData, String videoId) {
        JsonBrowser thumbnails = videoData.get("thumbnail").get("thumbnails").index(0);

        if (!thumbnails.isNull()) {
            return thumbnails.get("url").text().replaceFirst("=.*", "=w1000-h1000");
        }

        return String.format("https://i.ytimg.com/vi/%s/maxresdefault.jpg", videoId);
    }

    public static String getYouTubeThumbnail(JsonBrowser videoData, String videoId) {
        List<JsonBrowser> thumbnails = videoData.get("thumbnail").get("thumbnails").values();

        if (!thumbnails.isEmpty()) {
            String lastThumbnail = thumbnails.get(thumbnails.size() - 1).get("url").text();

            if (lastThumbnail.contains("maxresdefault")) {
                return lastThumbnail;
            }
        }

        return String.format("https://i.ytimg.com/vi/%s/hqdefault.jpg", videoId);
    }

    public static String getSoundCloudThumbnail(JsonBrowser trackData) {
      return PBJUtils.getSoundCloudThumbnail(trackData);
    }
}
