package io.lemonlabs.uri.json

import io.circe.Decoder.Result
import io.circe._
import io.circe.parser._
import io.lemonlabs.uri.UriException
import io.lemonlabs.uri.inet.Trie
import cats.syntax.either._

case object CirceSupport extends JsonSupport {

  implicit val charKeyDecoder: KeyDecoder[Char] = new KeyDecoder[Char] {
    def apply(key: String): Option[Char] = key.headOption
  }

  implicit val trieDecoder: Decoder[Trie] = new Decoder[Trie] {
    def apply(c: HCursor): Result[Trie] =
      for {
        children <- c.downField("c").as[Map[Char, Trie]]
        wordEnd <- c.downField("e").as[Boolean]
      } yield {
        new Trie(children, wordEnd)
      }
  }

  override lazy val publicSuffixTrie: Trie =
    decode[Trie](publicSuffixJson).getOrElse(throw new UriException("Unable to parse public suffix JSON"))
}
