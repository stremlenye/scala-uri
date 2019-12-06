package io.lemonlabs.uri.typesafe

import io.lemonlabs.uri.config.UriDecoderConfig
import io.lemonlabs.uri.{RelativeUrl, Url}

package object dsl {
  import PathPart.ops._

  import scala.language.implicitConversions

  implicit def stringToUri(s: String)(implicit c: UriDecoderConfig = UriDecoderConfig.default): Url = Url.parse(s)(c)

  implicit def stringToUriDsl(s: String)(implicit c: UriDecoderConfig = UriDecoderConfig.default): TypesafeUrlDsl =
    new TypesafeUrlDsl(stringToUri(s)(c))

  implicit def urlToUrlDsl(uri: Url): TypesafeUrlDsl = new TypesafeUrlDsl(uri)

  implicit def pathPartToUrlDsl[A: PathPart](a: A): TypesafeUrlDsl = new TypesafeUrlDsl(a.path)

  implicit def queryParamToUriDsl[A](a: A)(implicit c: UriDecoderConfig = UriDecoderConfig.default,
                                           tc: QueryKeyValue[A]): TypesafeUrlDsl =
    new TypesafeUrlDsl(RelativeUrl.empty.addParam(tc.queryKey(a), tc.queryValue(a)))
}
