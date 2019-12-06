package io.lemonlabs.uri

import io.lemonlabs.uri.config.{RenderQuery, UriEncoderConfig}
import io.lemonlabs.uri.encoding.PercentEncoder._

/**
  * Date: 28/08/2013
  * Time: 21:08
  */
package object encoding {
  val percentEncode = PercentEncoder()
  def percentEncode(chars: Char*) = PercentEncoder(chars.toSet)

  def encodeCharAs(c: Char, as: String) = EncodeCharAs(c, as)
  val spaceAsPlus = EncodeCharAs(' ', "+")

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
