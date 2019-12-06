package io.lemonlabs.uri

import io.lemonlabs.uri.config.{UriDecoderConfig, UriEncoderConfig}

package object dsl {
  import scala.language.implicitConversions

  implicit def urlToUrlDsl(uri: Url): UrlDsl = new UrlDsl(uri)

  implicit def stringToUri(s: String)(implicit c: UriDecoderConfig = UriDecoderConfig.default): Url = Url.parse(s)(c)
  implicit def stringToUriDsl(s: String)(implicit c: UriDecoderConfig = UriDecoderConfig.default): UrlDsl =
    new UrlDsl(stringToUri(s)(c))

  implicit def queryParamToUriDsl(kv: (String, Any))(implicit c: UriDecoderConfig = UriDecoderConfig.default): UrlDsl =
    new UrlDsl(RelativeUrl.empty.addParam(kv._1, kv._2.toString))

  implicit def uriToString(uri: Uri)(implicit c: UriEncoderConfig = encoding.default): String =
    uri.render(c)
}
