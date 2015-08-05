package com.mehmetakiftutuncu.muezzinapi.models.base

/**
 * A base trait for custom enumerations
 *
 * <ol>
 *   <li>Have an abstract super class or trait E for all your enum items.</li>
 *   <li>Generate case objects for each enumeration item of type E.</li>
 *   <li>Then add them to values set when you implement it.</li>
 * </ol>
 *
 * Example:
 * {{{
 *   abstract class Animal(val legs: Int)
 *
 *   object Animals extends EnumBase[Animal] {
 *     case object Chicken extends Animal(legs = 2)
 *     case object Cat     extends Animal(legs = 4)
 *     case object Ant     extends Animal(legs = 6)
 *
 *     override val values: Set[Animal] = Set(Chicken, Cat, Ant)
 *   }
 * }}}
 *
 * @tparam E Type of items in the enum
 */
trait EnumBase[E] {
  /** Set of enumeration values, you need to override this in your enumeration object! */
  val values: Set[E]

  /** A mapping from enumeration item name to item itself */
  protected lazy val valueMap: Map[String, E] = values.map(v => toName(v) -> v).toMap

  /**
   * Converts an enumeration value to a name
   *
   * @param value Enumeration value to convert to name
   *
   * @return Name of the enumeration value
   */
  def toName(value: E): String = value.toString

  /**
   * Returns enumeration item by it's name optionally
   *
   * @param name Name of the enumeration item
   *
   * @return Option(item) if found, None if not found
   */
  def withName(name: String): Option[E] = valueMap.get(name)
}
