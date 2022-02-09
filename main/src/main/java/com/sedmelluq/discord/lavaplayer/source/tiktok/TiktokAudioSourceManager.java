package com.sedmelluq.discord.lavaplayer.source.tiktok;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.*;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class TiktokAudioSourceManager implements AudioSourceManager, HttpConfigurable {
  private static final String TRACK_URL_REGEX = "^(?:https?://(?:www\\.|m\\.)?|)tiktok\\.com/@[^/]+/video/(\\d+)";
  private static final String MOBILE_URL_REGEX = "^(?:https?://)?vm\\.tiktok\\.com/\\w+";

  private static final Pattern TRACK_URL_PATTERN = Pattern.compile(TRACK_URL_REGEX);
  private static final Pattern MOBILE_URL_PATTERN = Pattern.compile(MOBILE_URL_REGEX);

  public static final String API_URL = "https://api2.musical.ly/aweme/v1/aweme/detail/?aweme_id=";

  private final HttpInterfaceManager httpInterfaceManager;

  public TiktokAudioSourceManager() {
    this.httpInterfaceManager = new ThreadLocalHttpInterfaceManager(
            HttpClientTools
                    .createSharedCookiesHttpBuilder()
                    .setRedirectStrategy(new HttpClientTools.NoRedirectsStrategy()),
            HttpClientTools.DEFAULT_REQUEST_CONFIG
    );
  }

  @Override
  public String getSourceName() {
    return "tiktok";
  }

  @Override
  public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
    final Matcher match = TRACK_URL_PATTERN.matcher(reference.identifier);

    if (match.find()) {
      return extractFromApi(match.group(1));
    }

    final Matcher mobileMatch = MOBILE_URL_PATTERN.matcher(reference.identifier);

    if (mobileMatch.find()) {
      String id;

      try {
        id = getRealId(reference.identifier);
      } catch (IOException e) {
        throw new FriendlyException("Failed to get real url of tiktok video.", SUSPICIOUS, e);
      }

      return extractFromApi(id);
    }

    return null;
  }

  @Override
  public boolean isTrackEncodable(AudioTrack track) {
    return true;
  }

  @Override
  public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
    // nothing to encode
  }

  @Override
  public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
    return new TiktokAudioTrack(trackInfo, this);
  }

  @Override
  public void shutdown() {
    ExceptionTools.closeWithWarnings(httpInterfaceManager);
  }

  /**
   * @return Get an HTTP interface for a playing track.
   */
  public HttpInterface getHttpInterface() {
    return httpInterfaceManager.getInterface();
  }

  @Override
  public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
    httpInterfaceManager.configureRequests(configurator);
  }

  @Override
  public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
    httpInterfaceManager.configureBuilder(configurator);
  }

  private AudioTrack extractFromApi(String id) {
    try (final CloseableHttpResponse res = getHttpInterface().execute(new HttpGet(API_URL + id))) {
      int statusCode = res.getStatusLine().getStatusCode();

      if (!HttpClientTools.isSuccessWithContent(statusCode)) {
        throw new IOException("Unexpected response code from tiktok api: " + statusCode);
      }

      JsonBrowser json = JsonBrowser.parse(res.getEntity().getContent());
      JsonBrowser data = json.get("aweme_detail");
      JsonBrowser video = data.get("video");

      String title = data.get("desc").safeText();
      String author = data.get("author").get("nickname").safeText();
      long duration = video.get("duration").asLong(Units.DURATION_MS_UNKNOWN);
      String uri = "https://www.tiktok.com/@" + data.get("author").get("unique_id").safeText() + "/video/" + id;
      String thumbnailUrl = video.get("cover").get("url_list").index(0).safeText();

      return new TiktokAudioTrack(new AudioTrackInfo(
          title,
          author,
          duration,
          id,
          false,
          uri,
          thumbnailUrl
      ), this);
    } catch (IOException e) {
      throw new FriendlyException("Failed to fetch tiktok video info.", SUSPICIOUS, e);
    }
  }

  private String getRealUrl(String url) {
    try (CloseableHttpResponse response = getHttpInterface().execute(new HttpGet(url))) {
      return HttpClientTools.getRedirectLocation(url, response);
    } catch (IOException e) {
      throw new FriendlyException("Failed to get real url of tiktok video.", SUSPICIOUS, e);
    }
  }

  private String getRealId(String reference) throws IOException {
    final String realUrl = getRealUrl(reference);

    final Matcher match = TRACK_URL_PATTERN.matcher(realUrl);

    if (!match.find()) {
      throw new IOException("The tiktok video url did not match the regex.");
    }

    return match.group(1);
  }
}
