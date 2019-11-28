package io.lemonlabs.uri.config

import io.lemonlabs.uri.encoding.PercentEncoder
import io.lemonlabs.uri.encoding.PercentEncoder.{
  FRAGMENT_CHARS_TO_ENCODE,
  PATH_CHARS_TO_ENCODE,
  QUERY_CHARS_TO_ENCODE,
  USER_INFO_CHARS_TO_ENCODE
}

package object encoder {
  implicit val default: UriEncoderConfig = UriEncoderConfig(
    userInfoEncoder = PercentEncoder(USER_INFO_CHARS_TO_ENCODE),
    pathEncoder = PercentEncoder(PATH_CHARS_TO_ENCODE),
    queryEncoder = PercentEncoder(QUERY_CHARS_TO_ENCODE),
    fragmentEncoder = PercentEncoder(FRAGMENT_CHARS_TO_ENCODE),
    charset = "UTF-8",
    renderQuery = RenderQuery.default
  )

  implicit val noopEncoding: UriEncoderConfig = default.withNoEncoding

  /**
    * Probably more than you need to percent encode. Wherever possible try to use a tighter Set of characters
    * to encode depending on your use case
    */
  implicit val conservative: UriEncoderConfig = UriEncoderConfig(PercentEncoder())
}
