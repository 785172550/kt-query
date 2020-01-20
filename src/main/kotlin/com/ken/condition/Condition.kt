package com.ken.condition

import com.ken.builder.SqlSelectBuilder
import com.ken.data.Col

/**
 * @Author hw83770
 * @Date 21:43 2020/1/19
 *
 */

abstract class Condition {
  //  inline fun <reified T> col(name: String): Col<T> = Col(name)
  fun <T> Col<T>.isNull() {
    addCondition(EqOrNot(this, null, eq = true))
  }

  fun <T> Col<T>.isNotNull() {
    addCondition(EqOrNot(this, null, eq = false))
  }

  infix fun <T> Col<T>.eq(value: T) {
    addCondition(EqOrNot(this, value, eq = true))
  }

  infix fun <T> Col<T>.eq(col: Col<*>) {
    addCondition(EqOrNot2(this, "${col.table}.${col.name}", eq = true))
  }

  infix fun <T> Col<T>.ne(value: T?) {
    addCondition(EqOrNot(this, value, eq = false))
  }

  infix fun <T> Col<T>.gt(value: T) {
    addCondition(Compare(this, value, cmp = Compare.GT))
  }

  infix fun <T> Col<T>.lt(value: T) {
    addCondition(Compare(this, value, cmp = Compare.LT))
  }

  infix fun <T> Col<T>.gte(value: T) {
    addCondition(Compare(this, value, cmp = Compare.GTE))
  }

  infix fun <T> Col<T>.lte(value: T) {
    addCondition(Compare(this, value, cmp = Compare.LTE))
  }

  infix fun <T> Col<T>.`in`(values: Set<T>) {
    addCondition(CollectionCondition(this, values, cmp = "in"))
  }

  infix fun <T> Col<T>.notIn(values: Set<T>) {
    addCondition(CollectionCondition(this, values, cmp = "notIn"))
  }

  fun <T> Col<T>.`in`(vararg values: T) {
    addCondition(CollectionCondition(this, values.asList(), cmp = "in"))
  }

  fun <T> Col<T>.notIn(vararg values: T) {
    addCondition(CollectionCondition(this, values.asList(), cmp = "notIn"))
  }

  infix fun <T> Col<T>.`in`(select: SqlSelectBuilder) {
    addCondition(ExistCondition(this, select, cmp = "in"))
  }

  infix fun <T> Col<T>.notIn(select: SqlSelectBuilder) {
    addCondition(ExistCondition(this, select, cmp = "notIn"))
  }

  fun exist(select: SqlSelectBuilder) {
    addCondition(ExistCondition(Col<Any>(""), select, cmp = "exist"))
  }

  fun notExist(select: SqlSelectBuilder) {
    addCondition(ExistCondition(Col<Any>(""), select, cmp = "notExist"))
  }

  fun and(init: Condition.() -> Unit) {
    addCondition(And().apply(init))
  }

  fun or(init: Condition.() -> Unit) {
    addCondition(Or().apply(init))
  }

  abstract fun addCondition(condition: Condition)
}


open class CompositeCondition(private val sqlOperator: String) : Condition() {
  val conditions = mutableListOf<Condition>()
    get() = field

  override fun addCondition(condition: Condition) {
    conditions += condition
  }

  override fun toString(): String {
    return if (conditions.size == 1) {
      conditions.first().toString()
    } else {
      conditions.joinToString(prefix = "(", postfix = ")", separator = " $sqlOperator ")
    }
  }
}

class And : CompositeCondition("and")
class Or : CompositeCondition("or")

// ------------- col operation ------------
class EqOrNot<T>(private val column: Col<T>, private val value: T?, private val eq: Boolean) :
  Condition() {

  init {
    if (value != null && value !is Number && value !is String) {
      throw IllegalArgumentException("Only <null>, numbers and strings values can be used in the 'where' clause")
    }
  }

  override fun addCondition(condition: Condition) {
    throw IllegalStateException("Can't add a nested condition to the sql 'eq'")
  }

  override fun toString(): String {
    val actualCol = if (column.table.isEmpty()) column.name else "${column.table}.${column.name}"
    return when (value) {
      null -> if (eq) "${actualCol} is null" else "${actualCol} is not null"
      is String -> if (eq) "${actualCol} = '$value'" else "${actualCol} != '$value'"
      else -> if (eq) "${actualCol} = $value" else "${actualCol}!= $value"
    }
  }
}

class EqOrNot2<T>(private val column: Col<T>, private val value: String, private val eq: Boolean) :
  Condition() {

  override fun addCondition(condition: Condition) {
    throw IllegalStateException("Can't add a nested condition to the sql 'eq'")
  }

  override fun toString(): String {
    val actualCol = if (column.table.isEmpty()) column.name else "${column.table}.${column.name}"
    return if (eq) "${actualCol} = $value" else "${actualCol}!= $value"
  }
}

class Compare<T>(private val column: Col<T>, private val value: T, private val cmp: String) : Condition() {
  companion object {
    const val GT = ">"
    const val GTE = ">="
    const val LT = "<"
    const val LTE = "<="
  }

  override fun addCondition(condition: Condition) {
    throw IllegalStateException("Can't add a nested condition to the sql 'eq'")
  }

  override fun toString(): String {
    val actualCol = if (column.table.isEmpty()) column.name else "${column.table}.${column.name}"

    val vals: String = if (value is String) {
      "'$value'"
    } else {
      "$value"
    }
    return when (cmp) {
      GT -> "${actualCol} > $vals"
      GTE -> "${actualCol} >= $vals"
      LT -> "${actualCol} < $vals"
      LTE -> "${actualCol} <= $vals"
      else -> "${actualCol} = $vals"
    }
  }
}

class CollectionCondition<T>(private val column: Col<T>, private val values: Collection<T>, private val cmp: String) :
  Condition() {

  override fun addCondition(condition: Condition) {
    throw IllegalStateException("Can't add a nested condition to the sql 'eq'")
  }

  override fun toString(): String {
    val actualCol = if (column.table.isEmpty()) column.name else "${column.table}.${column.name}"

    val vals = if (values.first() is String) {
      values.map { "'$it'" }.joinToString(prefix = "(", postfix = ")", separator = ", ")
    } else {
      values.joinToString(prefix = "(", postfix = ")", separator = ", ")
    }

    return when (cmp) {
      "in" -> "${actualCol} in $vals"
      "notIn" -> "${actualCol} not in $vals"
      else -> "$actualCol = $vals"
    }
  }
}

class ExistCondition<T>(private val column: Col<T>, private val select: SqlSelectBuilder, private val cmp: String) :
  Condition() {

  override fun addCondition(condition: Condition) {
    throw IllegalStateException("Can't add a nested condition to the sql 'eq'")
  }

  override fun toString(): String {
    val actualCol = if (column.table.isEmpty()) column.name else "${column.table}.${column.name}"
    val queryResult = select.build()

    return when (cmp) {
      "exist" -> "exist ($queryResult)"
      "not exist" -> "not exist ($queryResult)"
      "in" -> "${actualCol} in ($queryResult)"
      "not in" -> "${actualCol} not in ($queryResult)"
      else -> "${actualCol} not exist ($queryResult)"
    }
  }
}