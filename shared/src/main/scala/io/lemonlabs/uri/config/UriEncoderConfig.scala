package io.lemonlabs.uri.config

import io.lemonlabs.uri.encoding.{NoopEncoder, PercentEncoder, UriEncoder}

case class UriEncoderConfig(userInfoEncoder: UriEncoder,
                            pathEncoder: UriEncoder,
                            queryEncoder: UriEncoder,
                            fragmentEncoder: UriEncoder,
                            charset: String,
                            renderQuery: RenderQuery) {
  def withNoEncoding: UriEncoderConfig =
    copy(pathEncoder = NoopEncoder, queryEncoder = NoopEncoder, fragmentEncoder = NoopEncoder)
}

object UriEncoderConfig {
  object instances {}

  def apply(encoder: UriEncoder = PercentEncoder(),
            charset: String = "UTF-8",
            renderQuery: RenderQuery = RenderQuery.default): UriEncoderConfig =
    UriEncoderConfig(encoder, encoder, encoder, encoder, charset, renderQuery)
}
