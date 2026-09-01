package com.sedmelluq.discord.lavaplayer.natives.aac

import spock.lang.Specification

class AacDecoderNativeAbiSpec extends Specification {
  def "AAC wrapper uses the packed-long native configuration ABI"() {
    expect:
    AacDecoderLibrary.getDeclaredMethod('configure', Long.TYPE, Long.TYPE).native
  }

  def "AAC LC decoder accepts a stereo stream configuration"() {
    when:
    def decoder = new AacDecoder()
    decoder.configure(AacDecoder.AAC_LC, 44_100, 2)
    decoder.close()

    then:
    noExceptionThrown()
  }
}
