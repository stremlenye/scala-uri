package io.lemonlabs.uri

import io.lemonlabs.uri.config.{UriConfig, UriEncoderConfig}
import io.lemonlabs.uri.config.encoder.default
import org.scalatest.{FlatSpec, Matchers}

class EncodingTests extends FlatSpec with Matchers {
  import encoding._

  "URI paths" should "be percent encoded" in {
    val url = Url.parse("http://theon.github.com/üris-in-scàla.html")
    url.toString should equal("http://theon.github.com/%C3%BCris-in-sc%C3%A0la.html")
  }

  "Raw paths" should "not be encoded" in {
    val url = Url.parse("http://theon.github.com/üris-in-scàla.html")
    url.path.toStringRaw should equal("/üris-in-scàla.html")
  }

  "toStringRaw" should "not be encoded" in {
    val url = Url.parse("http://theon.github.com/üris-in-scàla.html?càsh=£50")
    url.toStringRaw should equal("http://theon.github.com/üris-in-scàla.html?càsh=£50")
  }

  "URI path spaces" should "be percent encoded by default" in {
    val url = Url.parse("http://theon.github.com/uri with space")
    url.render should equal("http://theon.github.com/uri%20with%20space")
  }

  "URI path double quotes" should "be percent encoded when using conservative encoder" in {
    val url = Url.parse("""http://theon.github.com/blah/"quoted"""")
    url.render(config.encoder.conservative) should equal("http://theon.github.com/blah/%22quoted%22")
  }

  "URI path spaces" should "be plus encoded if configured" in {
    val config: UriEncoderConfig = UriEncoderConfig(encoder = percentEncode + spaceAsPlus)
    val url = Url.parse("http://theon.github.com/uri with space")
    url.render(config) should equal("http://theon.github.com/uri+with+space")
  }

  "Path chars" should "be encoded as custom strings if configured" in {
    val config: UriEncoderConfig = UriEncoderConfig(encoder = percentEncode + encodeCharAs(' ', "_"))
    val url = Url.parse("http://theon.github.com/uri with space")
    url.render(config) should equal("http://theon.github.com/uri_with_space")
  }

  "Querystring parameters" should "be percent encoded" in {
    val url = Url.parse("http://theon.github.com/uris-in-scala.html?càsh=+£50&©opyright=false")
    url.render should equal("http://theon.github.com/uris-in-scala.html?c%C3%A0sh=%2B%C2%A350&%C2%A9opyright=false")
  }

  "Querystring double quotes" should "be percent encoded when using conservative encoder" in {
    val url = Url.parse("""http://theon.github.com?blah="quoted"""")
    url.render(config.encoder.conservative) should equal("http://theon.github.com?blah=%22quoted%22")
  }

  "Reserved characters" should "be percent encoded when using conservative encoder" in {
    val url = Url(
      query = QueryString.fromPairs("reserved" -> ":/?#[]@!$&'()*+,;={}\\\n\r")
    )
    url.render(config.encoder.conservative) should equal(
      "?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D%7B%7D%5C%0A%0D"
    )
  }

  "Chinese characters" should "be percent encoded" in {
    val url = Url.parse("http://theon.github.com/uris-in-scala.html?chinese=网址")
    url.render should equal("http://theon.github.com/uris-in-scala.html?chinese=%E7%BD%91%E5%9D%80")
  }

  "Russian characters" should "be percent encoded" in {
    val url = Url.parse("http://theon.github.com/uris-in-scala.html?russian=Скала")
    url.render should equal("http://theon.github.com/uris-in-scala.html?russian=%D0%A1%D0%BA%D0%B0%D0%BB%D0%B0")
  }

  "Fragments" should "be percent encoded" in {
    val url = Url.parse("http://theon.github.com/uris-in-scala.html#chinese# 网址")
    url.render should equal("http://theon.github.com/uris-in-scala.html#chinese%23%20%E7%BD%91%E5%9D%80")
  }

  "Percent encoding with custom reserved characters" should "be easy" in {
    val config: UriEncoderConfig = UriEncoderConfig(encoder = percentEncode('#'))
    val url = Url(
      query = QueryString.fromPairs("reserved" -> ":/?#[]@!$&'()*+,;={}\\")
    )
    url.render(config) should equal("?reserved=:/?%23[]@!$&'()*+,;={}\\")
  }

  "Percent encoding with a few less reserved characters that the defaults" should "be easy" in {
    val config: UriEncoderConfig = UriEncoderConfig(encoder = percentEncode -- '+')
    val url = Url(
      path = "/uris-in-scala.html",
      query = QueryString.fromPairs("reserved" -> ":/?#[]@!$&'()*+,;={}\\\n\r")
    )
    url.render(config) should equal(
      "/uris-in-scala.html?reserved=%3A%2F%3F%23%5B%5D%40%21%24%26%27%28%29%2A+%2C%3B%3D%7B%7D%5C%0A%0D"
    )
  }

  "Percent encoding with a few extra reserved characters on top of the defaults" should "be easy" in {
    val encoderConfig = UriEncoderConfig(encoder = percentEncode() ++ ('a', 'b'))
    val url = Url.parse("http://theon.github.com/abcde")
    url.render(encoderConfig) should equal("http://theon.github.com/%61%62cde")
  }

  "URI path pchars" should "not be encoded by default" in {
    val url = Url.parse("http://example.com/-._~!$&'()*+,;=:@/test")
    url.render(config.encoder.default) should equal("http://example.com/-._~!$&'()*+,;=:@/test")
  }

  "Query parameters" should "have control characters encoded" in {
    val url = Url.parse("http://example.com/?control=\u0019\u007F")
    url.render(config.encoder.default) should equal("http://example.com/?control=%19%7F")
  }

  "Percent Encoded forward slashes in the path" should "be preserved" in {
    Url.parse("/%2F/").render should equal("/%2F/")
    val builtUrl = Url.parse("http://example.com").addPathPart("1/2")
    builtUrl.render(config.encoder.default) should equal("http://example.com/1%2F2")
  }
}
