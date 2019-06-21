package io.lemonlabs.uri

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.json.{CirceSupport, SprayJsonSupport}
import org.scalatest.{FlatSpec, Matchers}

class PublicSuffixTests extends FlatSpec with Matchers {

  "Uri publicSuffix method" should "match the longest public suffix" in {
    val uri = Url.parse("http://www.google.co.uk/blah")
    uri.publicSuffix should equal(Some("co.uk"))
  }

  it should "only return public suffixes that match full dot separated host parts" in {
    val uri = Url.parse("http://www.bar.com")

    // Should not match ar.com
    // Github issue #110
    uri.publicSuffix should equal(Some("com"))
  }

  "Uri publicSuffixes method" should "match the all public suffixes" in {
    val uri = Url.parse("http://www.google.co.uk/blah")
    uri.publicSuffixes should equal(Vector("co.uk", "uk"))
  }

  it should "return None for relative URLs" in {
    val uri = Url.parse("/blah")
    uri.publicSuffix should equal(None)
  }

  "SprayJsonSupport" should "return public suffixes" in {
    implicit val config: UriConfig = UriConfig(jsonSupport = SprayJsonSupport)
    val uri = Url.parse("http://www.google.co.uk/blah")
    uri.publicSuffixes should equal(Vector("co.uk", "uk"))
  }

  "CirceSupport" should "return public suffixes" in {
    implicit val config: UriConfig = UriConfig(jsonSupport = CirceSupport)
    val uri = Url.parse("http://www.google.co.uk/blah")
    uri.publicSuffixes should equal(Vector("co.uk", "uk"))
  }
}
