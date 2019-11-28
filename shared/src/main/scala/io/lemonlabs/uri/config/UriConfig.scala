package io.lemonlabs.uri.config

import io.lemonlabs.uri.decoding.{PercentDecoder, UriDecoder}

case class UriConfig(userInfoDecoder: UriDecoder,
                     pathDecoder: UriDecoder,
                     queryDecoder: UriDecoder,
                     fragmentDecoder: UriDecoder) {}

object UriConfig {
  val default = UriConfig(
    userInfoDecoder = PercentDecoder,
    pathDecoder = PercentDecoder,
    queryDecoder = PercentDecoder,
    fragmentDecoder = PercentDecoder
  )

  /**
    * Probably more than you need to percent encode. Wherever possible try to use a tighter Set of characters
    * to encode depending on your use case
    */
  val conservative = UriConfig(PercentDecoder)

  def apply(decoder: UriDecoder = PercentDecoder): UriConfig =
    UriConfig(decoder, decoder, decoder, decoder)
}
