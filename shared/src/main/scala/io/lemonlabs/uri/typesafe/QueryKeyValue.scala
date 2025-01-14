package io.lemonlabs.uri.typesafe

import shapeless._
import shapeless.labelled._
import shapeless.ops.coproduct.Reify
import shapeless.ops.hlist.ToList
import simulacrum.typeclass

import scala.language.implicitConversions

@typeclass trait QueryKey[A] {
  def queryKey(a: A): String
}

object QueryKey extends QueryKeyInstance

sealed trait QueryKeyInstance {
  implicit val stringQueryKey: QueryKey[String] = a => a
}

@typeclass trait QueryValue[-A] { self =>
  def queryValue(a: A): Option[String]
  def contramap[B](f: B => A): QueryValue[B] = (b: B) => self.queryValue(f(b))
}

object QueryValue extends QueryValueInstances {
  def derive[A]: Derivation[A] = new Derivation[A](())

  class Derivation[A](private val dummy: Unit) extends AnyVal {
    def by[C <: Coproduct, R <: HList](
        key: A => String
    )(implicit gen: Generic.Aux[A, C], reify: Reify.Aux[C, R], toList: ToList[R, A]): QueryValue[A] =
      a => toList(reify()).iterator.map(x => x -> key(x)).toMap.get(a)
  }
}

sealed trait QueryValueInstances1 {
  implicit final val stringQueryValue: QueryValue[String] = Option(_)
  implicit final val booleanQueryValue: QueryValue[Boolean] = stringQueryValue.contramap(_.toString)
  implicit final val charQueryValue: QueryValue[Char] = stringQueryValue.contramap(_.toString)
  implicit final val intQueryValue: QueryValue[Int] = stringQueryValue.contramap(_.toString)
  implicit final val longQueryValue: QueryValue[Long] = stringQueryValue.contramap(_.toString)
  implicit final val floatQueryValue: QueryValue[Float] = stringQueryValue.contramap(_.toString)
  implicit final val doubleQueryValue: QueryValue[Double] = stringQueryValue.contramap(_.toString)
  implicit final val uuidQueryValue: QueryValue[java.util.UUID] = stringQueryValue.contramap(_.toString)
  implicit final val noneQueryValue: QueryValue[None.type] = _ => None
}

sealed trait QueryValueInstances extends QueryValueInstances1 {
  implicit final def optionQueryValue[A: QueryValue]: QueryValue[Option[A]] = _.flatMap(QueryValue[A].queryValue)
}

@typeclass trait QueryKeyValue[A] {
  def queryKey(a: A): String

  def queryValue(a: A): Option[String]
}

object QueryKeyValue extends QueryKeyValueInstances

sealed trait QueryKeyValueInstances {
  implicit def tuple2QueryKeyValue[K: QueryKey, V: QueryValue]: QueryKeyValue[(K, V)] =
    new QueryKeyValue[(K, V)] {
      def queryKey(a: (K, V)): String = QueryKey[K].queryKey(a._1)

      def queryValue(a: (K, V)): Option[String] = QueryValue[V].queryValue(a._2)
    }
}

sealed trait TraversableParamsInstances {
  implicit def seqTraversableParams[A](implicit tc: QueryKeyValue[A]): TraversableParams[Seq[A]] =
    (ax: Seq[A]) => ax.map((a: A) => tc.queryKey(a) -> tc.queryValue(a))

  implicit def listTraversableParams[A](implicit tc: QueryKeyValue[A]): TraversableParams[List[A]] =
    (ax: List[A]) => ax.map((a: A) => tc.queryKey(a) -> tc.queryValue(a))

  implicit def traversableParams[K, V](implicit tck: QueryKey[K], tcv: QueryValue[V]): TraversableParams[Map[K, V]] =
    (ax: Map[K, V]) => ax.map { case (k, v) => tck.queryKey(k) -> tcv.queryValue(v) }.toSeq
}

@typeclass trait TraversableParams[A] {
  def toSeq(a: A): Seq[(String, Option[String])]
}

object TraversableParams extends TraversableParamsInstances {
  implicit def field[K <: Symbol, V](implicit K: Witness.Aux[K], V: QueryValue[V]): TraversableParams[FieldType[K, V]] =
    (a: FieldType[K, V]) => Seq(K.value.name -> V.queryValue(a))

  implicit def sub[K <: Symbol, V](implicit K: Witness.Aux[K],
                                   V: TraversableParams[V]): TraversableParams[FieldType[K, V]] =
    (a: FieldType[K, V]) => V.toSeq(a)

  implicit val hnil: TraversableParams[HNil] =
    (_: HNil) => Seq.empty

  implicit def hcons[H, T <: HList](implicit H: TraversableParams[H],
                                    T: TraversableParams[T]): TraversableParams[H :: T] =
    (a: H :: T) => H.toSeq(a.head) ++ T.toSeq(a.tail)

  def product[A, R <: HList](implicit gen: LabelledGeneric.Aux[A, R], R: TraversableParams[R]): TraversableParams[A] =
    (a: A) => R.toSeq(gen.to(a))
}
