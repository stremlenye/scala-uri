package io.lemonlabs.uri

import cats.implicits._
import cats.{Eq, Order, Show}
import io.lemonlabs.uri.config.{All, ExcludeNones, UriDecoderConfig, UriEncoderConfig}
import io.lemonlabs.uri.parsing.UrlParser

import scala.util.Try

case class QueryString(params: Vector[(String, Option[String])])(implicit config: UriDecoderConfig =
                                                                   UriDecoderConfig.default) {
  lazy val paramMap: Map[String, Vector[String]] = params.foldLeft(Map.empty[String, Vector[String]]) {
    case (m, (k, Some(v))) =>
      val values = m.getOrElse(k, Vector.empty)
      m + (k -> (values :+ v))

    // For query parameters with no value (e.g. /blah?q), Put at explicit Nil into the Map
    // If there is already an entry in the Map from a previous parameter with the same name, maintain it
    case (m, (k, None)) =>
      val values = m.getOrElse(k, Vector.empty)
      m + (k -> values)
  }

  /**
    * Adds a new parameter key-value pair.
    *
    * @return A new instance with the new parameter added
    */
  def addParam(k: String, v: String): QueryString =
    addParam(k, Some(v))

  /**
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    *
    * @return A new instance with the new parameter added
    */
  def addParam(k: String, v: Option[String]): QueryString =
    QueryString(params :+ (k -> v))

  /**
    * Adds a new parameter key with no value, e.g. `?param`
    *
    * @return A new instance with the new parameter added
    */
  def addParam(k: String): QueryString =
    addParam(k, None)

  /**
    * Adds a new Query String parameter key-value pair.
    */
  def addParam(kv: (String, String)): QueryString =
    QueryString(params :+ (kv._1 -> Some(kv._2)))

  /**
    * Adds a new Query String parameter key-value pair.
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    */
  def addParamOptionValue(kv: (String, Option[String])): QueryString =
    QueryString(params :+ kv)

  /**
    * Adds all the specified key-value pairs as parameters to the query
    */
  def addParams(other: QueryString): QueryString =
    QueryString(params ++ other.params)

  /**
    * Adds all the specified key-value pairs as parameters to the query
    */
  def addParams(kvs: (String, String)*): QueryString =
    addParams(kvs)

  /**
    * Adds all the specified key-value pairs as parameters to the query
    */
  def addParams(kvs: Iterable[(String, String)]): QueryString =
    addParamsOptionValues(kvs.map { case (k, v) => (k, Some(v)) })

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    */
  def addParamsOptionValues(kvs: Iterable[(String, Option[String])]): QueryString =
    QueryString(params ++ kvs)

  /**
    * Adds all the specified key-value pairs as parameters to the query
    *
    * Pairs with values, such as `("param", Some("value"))`, represent query params with values, i.e `?param=value`
    *
    * By default, pairs without values, such as `("param", None)`, represent query params without values, i.e `?param`
    * Using a `UriConfig(renderQuery = ExcludeNones)`, will cause pairs with `None` values not to be rendered
    */
  def addParamsOptionValues(kvs: (String, Option[String])*): QueryString =
    addParamsOptionValues(kvs)

  def params(key: String): Vector[Option[String]] = params.collect {
    case (k, v) if k == key => v
  }

  def param(key: String): Option[String] = params.collectFirst {
    case (k, Some(v)) if k == key => v
  }

  /**
    * Transforms the Query String by applying the specified PartialFunction to each Query String Parameter
    *
    * Parameters not defined in the PartialFunction will be left as-is.
    *
    * @param f A function that returns a new Parameter when applied to each Parameter
    * @return
    */
  def map(f: PartialFunction[(String, Option[String]), (String, Option[String])]): QueryString = {
    QueryString(params.map { kv =>
      if (f.isDefinedAt(kv)) f(kv) else kv
    })
  }

  /**
    * Transforms the Query String by applying the specified PartialFunction to each Query String Parameter
    *
    * Parameters not defined in the PartialFunction will be removed.
    *
    * @param f A function that returns a new Parameter when applied to each Parameter
    * @return
    */
  def collect(f: PartialFunction[(String, Option[String]), (String, Option[String])]): QueryString =
    QueryString(params.collect(f))

  /**
    * Transforms each parameter by applying the specified Function
    *
    * @param f A function that returns a collection of Parameters when applied to each parameter
    * @return
    */
  def flatMap(f: ((String, Option[String])) => Iterable[(String, Option[String])]): QueryString =
    QueryString(params.flatMap(f))

  /**
    * Transforms each parameter name by applying the specified Function
    *
    * @param f
    * @return
    */
  def mapNames(f: String => String): QueryString =
    QueryString(params.map {
      case (n, v) => (f(n), v)
    })

  /**
    * Transforms each parameter value by applying the specified Function
    *
    * @param f
    * @return
    */
  def mapValues(f: String => String): QueryString =
    QueryString(params.map {
      case (n, v) => (n, v map f)
    })

  /**
    * Filters out just the parameters for which the provided function holds true
    *
    * @param f
    * @return
    */
  def filter(f: ((String, Option[String])) => Boolean): QueryString =
    QueryString(params.filter(f))

  /**
    * Filters out just the parameters for which the provided function holds true when applied to the parameter name
    *
    * @param f
    * @return
    */
  def filterNames(f: String => Boolean): QueryString =
    QueryString(params.filter {
      case (n, _) => f(n)
    })

  /**
    * Filters out just the parameters for which the provided function holds true when applied to the parameter value
    *
    * @param f
    * @return
    */
  def filterValues(f: String => Boolean): QueryString =
    QueryString(params.filter {
      case (_, Some(v)) => f(v)
      case _            => false
    })

  /**
    * Filters out just the parameters for which the provided function holds true when applied to the parameter value
    *
    * @param f
    * @return
    */
  def filterOptionValues(f: Option[String] => Boolean): QueryString =
    QueryString(params.filter {
      case (_, v) => f(v)
    })

  /**
    * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
    * with the specified value.
    *
    * If the value passed in is None, then all Query String parameters with the specified key are replaces with a
    * valueless query param. E.g. `replaceParams("q", None)` would turn `?q=1&q=2` into `?q`
    *
    * @param k Key for the Query String parameter(s) to replace
    * @param v value to replace with
    * @return A new QueryString with the result of the replace
    */
  def replaceAll(k: String, v: Option[String]): QueryString =
    QueryString(params.filterNot(_._1 == k) :+ (k -> v))

  /**
    * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
    * with the specified value.
    *
    * @param k Key for the Query String parameter(s) to replace
    * @param v value to replace with
    * @return A new QueryString with the result of the replace
    */
  def replaceAll(k: String, v: String): QueryString =
    replaceAll(k, Some(v))

  /**
    * Removes all Query String parameters with the specified key
    * @param k Key for the Query String parameter(s) to remove
    * @return
    */
  def removeAll(k: String): QueryString =
    filterNames(_ != k)

  /**
    * Removes all Query String parameters with a name in the specified list
    * @param k Names of Query String parameter(s) to remove
    * @return
    */
  def removeAll(k: String*): QueryString =
    removeAll(k)

  /**
    * Removes all Query String parameters with a name in the specified list
    * @param k Names of Query String parameter(s) to remove
    * @return
    */
  def removeAll(k: Iterable[String]): QueryString =
    filterNames(name => !k.exists(_ == name))

  def isEmpty: Boolean = params.isEmpty
  def nonEmpty: Boolean = params.nonEmpty

  type ParamToString = PartialFunction[(String, Option[String]), String]

  private[uri] def render(c: UriEncoderConfig): String = {
    val enc = c.queryEncoder
    val charset = c.charset

    val someToString: ParamToString = {
      case (k, Some(v)) => enc.encode(k, charset) + "=" + enc.encode(v, charset)
    }
    val paramToString: ParamToString = someToString orElse {
      case (k, None) => enc.encode(k, charset)
    }

    val paramsAsString = c.renderQuery match {
      case All          => params.map(paramToString)
      case ExcludeNones => params.collect(someToString)
    }

    if (paramsAsString.isEmpty) ""
    else paramsAsString.mkString("&")
  }

  /**
    * Returns the query string with no encoding taking place (e.g. non ASCII characters will not be percent encoded)
    * @return String containing the raw query string for this Uri
    */
  def toStringRaw(implicit config: UriEncoderConfig): String =
    render(config.withNoEncoding)

  override def toString: String = render(io.lemonlabs.uri.encoding.default)
}

object QueryString {
  def fromPairOptions(
      params: (String, Option[String])*
  )(implicit config: UriDecoderConfig = UriDecoderConfig.default): QueryString =
    new QueryString(params.toVector)

  def fromPairs(params: (String, String)*)(implicit config: UriDecoderConfig = UriDecoderConfig.default): QueryString =
    fromTraversable(params)

  def fromTraversable(
      params: Iterable[(String, String)]
  )(implicit config: UriDecoderConfig = UriDecoderConfig.default): QueryString =
    new QueryString(params.toVector.map {
      case (k, v) => (k, Some(v))
    })

  def empty(implicit config: UriDecoderConfig = UriDecoderConfig.default): QueryString =
    new QueryString(Vector.empty)

  def parseTry(s: CharSequence)(implicit config: UriDecoderConfig = UriDecoderConfig.default): Try[QueryString] =
    UrlParser.parseQuery(s.toString)

  def parseOption(s: CharSequence)(implicit config: UriDecoderConfig = UriDecoderConfig.default): Option[QueryString] =
    parseTry(s).toOption

  def parse(s: CharSequence)(implicit config: UriDecoderConfig = UriDecoderConfig.default): QueryString =
    parseTry(s).get

  implicit val eqQueryString: Eq[QueryString] = Eq.fromUniversalEquals
  implicit val showQueryString: Show[QueryString] = Show.fromToString
  implicit val orderQueryString: Order[QueryString] = Order.by(_.params)
}
