package io.lemonlabs.uri.config

import io.lemonlabs.uri.decoding.{PercentDecoder, UriDecoder}

case class UriDecoderConfig(userInfoDecoder: UriDecoder,
                            pathDecoder: UriDecoder,
                            queryDecoder: UriDecoder,
                            fragmentDecoder: UriDecoder) {}

object UriDecoderConfig {
  val default = UriDecoderConfig(
    userInfoDecoder = PercentDecoder,
    pathDecoder = PercentDecoder,
    queryDecoder = PercentDecoder,
    fragmentDecoder = PercentDecoder
  )

  /**
    * Probably more than you need to percent encode. Wherever possible try to use a tighter Set of characters
    * to encode depending on your use case
    */
  val conservative = UriDecoderConfig(PercentDecoder)

  def apply(decoder: UriDecoder = PercentDecoder): UriDecoderConfig =
    UriDecoderConfig(decoder, decoder, decoder, decoder)
}
