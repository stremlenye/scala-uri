package io.lemonlabs.uri.encoding

/**
  * Date: 28/08/2013
  * Time: 21:07
  */
case class ChainedUriEncoder(encoders: Seq[UriEncoder]) extends UriEncoder {
  def shouldEncode(ch: Char): Boolean = findFirstEncoder(ch).isDefined
  def encodeChar(ch: Char): String = findFirstEncoder(ch).getOrElse(NoopEncoder).encodeChar(ch)

  def findFirstEncoder(ch: Char): Option[UriEncoder] = {
    encoders.find(_.shouldEncode(ch))
  }

  override def +(encoder: UriEncoder): ChainedUriEncoder = copy(encoders = encoder +: encoders)
}
