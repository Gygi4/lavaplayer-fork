package com.sedmelluq.discord.lavaplayer.container.mp3;

import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;

/**
 * Seeker for an MP3 stream, which actually does not allow seeking and reports UnitConstants.DURATION_MS_UNKNOWN as
 * duration.
 */
public class Mp3StreamSeeker implements Mp3Seeker {
  @Override
  public long getDuration() {
    return Units.DURATION_MS_UNKNOWN;
  }

  @Override
  public boolean isSeekable() {
    return false;
  }

  @Override
  public long seekAndGetFrameIndex(long timecode, SeekableInputStream inputStream) {
    throw new UnsupportedOperationException("Cannot seek on a stream.");
  }
}
