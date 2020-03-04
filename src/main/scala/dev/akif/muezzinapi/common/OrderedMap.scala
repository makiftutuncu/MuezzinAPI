package dev.akif.muezzinapi.common

import scala.collection.immutable.AbstractMap

final class OrderedMap[K, +V] private(private val underlying: Map[K, V],
                                      private val valueVector: Vector[(K, V)]) extends AbstractMap[K, V] { self =>
  def append[VV >: V](tuple: (K, VV)): OrderedMap[K, VV] =
    get(tuple._1).fold(new OrderedMap[K, VV](underlying + tuple, valueVector.appended(tuple))) { _ =>
      val (key, newValue) = tuple
      val newTuple        = key -> newValue

      new OrderedMap[K, VV](underlying + newTuple, valueVector.filterNot(_._1 == key).appended(newTuple))
    }

  override def +[VV >: V](tuple: (K, VV)): OrderedMap[K, VV] = append(tuple)

  override def ++[VV >: V](tuples: IterableOnce[(K, VV)]): OrderedMap[K, VV] = tuples.iterator.foldLeft[OrderedMap[K, VV]](self)(_ append _)

  override def removed(key: K): OrderedMap[K, V] =
    new OrderedMap[K, V](underlying - key, valueVector.filterNot(_._1 == key))

  override def updated[VV >: V](key: K, value: VV): OrderedMap[K, VV] = append(key -> value)

  override def get(key: K): Option[V] = underlying.get(key)

  override def iterator: Iterator[(K, V)] = valueVector.iterator

  def toMap: Map[K, V] = underlying

  override def equals(other: Any): Boolean =
    other match {
      case that: OrderedMap[_, _] => valueVector == that.valueVector
      case _                      => false
    }

  override def hashCode(): Int = valueVector.hashCode()

  override def toString: String = valueVector.map { case (k, v) => s"$k -> $v" }.mkString("OrderedMap(", ",", ")")
}

object OrderedMap {
  def empty[K, V]: OrderedMap[K, V] = new OrderedMap[K, V](Map.empty, Vector.empty)

  def apply[K, V](tuples: (K, V)*): OrderedMap[K, V] = empty ++ tuples

  def byKeys[K: Ordering, V](map: Map[K, V]): OrderedMap[K, V] = empty ++ map.toList.sortBy(_._1)

  def byValues[K, V: Ordering](map: Map[K, V]): OrderedMap[K, V] = empty ++ map.toList.sortBy(_._2)
}
