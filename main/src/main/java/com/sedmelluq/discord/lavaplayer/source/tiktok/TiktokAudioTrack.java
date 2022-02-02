package com.sedmelluq.discord.lavaplayer.source.tiktok;

import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class TiktokAudioTrack extends DelegatedAudioTrack {
  private static final Logger log = LoggerFactory.getLogger(TiktokAudioTrack.class);

  private final TiktokAudioSourceManager sourceManager;

  /**
   * @param trackInfo Track info
   * @param sourceManager The source manager
   */
  public TiktokAudioTrack(AudioTrackInfo trackInfo, TiktokAudioSourceManager sourceManager) {
    super(trackInfo);

    this.sourceManager = sourceManager;
  }

  @Override
  public void process(LocalAudioTrackExecutor executor) throws Exception {
    try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
      String playbackUrl = loadPlaybackUrl(httpInterface);

      log.debug("Starting Tiktok track from URL: {}", playbackUrl);

      try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(playbackUrl), null)) {
        processDelegate(new MpegAudioTrack(trackInfo, stream), executor);
      }
    } catch (IOException e) {
      throw new FriendlyException("Loading track from Tiktok failed.", SUSPICIOUS, e);
    }
  }

  private String loadPlaybackUrl(HttpInterface httpInterface) {
    try (final CloseableHttpResponse res = httpInterface.execute(new HttpGet(TiktokAudioSourceManager.API_URL + trackInfo.identifier))) {
      int statusCode = res.getStatusLine().getStatusCode();

      if (!HttpClientTools.isSuccessWithContent(statusCode)) {
        throw new IOException("Unexpected response code from tiktok api: " + statusCode);
      }

      return JsonBrowser.parse(res.getEntity().getContent()).get("aweme_detail").get("video").get("play_addr").get("url_list").index(0).text();
    }catch (IOException e) {
      throw new FriendlyException("Failed to get tiktok video playback url.", SUSPICIOUS, e);
    }
  }

  @Override
  protected AudioTrack makeShallowClone() {
    return new TiktokAudioTrack(trackInfo, sourceManager);
  }

  @Override
  public TiktokAudioSourceManager getSourceManager() {
    return sourceManager;
  }
}
