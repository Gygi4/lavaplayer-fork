package com.sedmelluq.discord.lavaplayer.source.odysee;

import com.sedmelluq.discord.lavaplayer.tools.http.HttpContextFilter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;

public class OdyseeHttpContextFilter implements HttpContextFilter {
  @Override
  public void onContextOpen(HttpClientContext context) {

  }

  @Override
  public void onContextClose(HttpClientContext context) {

  }

  @Override
  public void onRequest(HttpClientContext context, HttpUriRequest request, boolean isRepetition) {
    request.setHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
    request.setHeader("origin", "https://odysee.com");
    request.setHeader("referer", "https://odysee.com/");
  }

  @Override
  public boolean onRequestResponse(HttpClientContext context, HttpUriRequest request, HttpResponse response) {
    return false;
  }

  @Override
  public boolean onRequestException(HttpClientContext context, HttpUriRequest request, Throwable error) {
    return false;
  }
}
