package com.ken.builder

import com.ken.condition.And
import com.ken.condition.CompositeCondition
import com.ken.data.Col


/**
 * @Author hw83770
 * @Date 21:31 2020/1/19
 *
 */
class SqlSelectBuilder {

  // col
  inline fun <reified T> col(name: String): Col<T> = Col(name)

  inline fun <reified T> col(name: String, table: String): Col<T> = Col(name, table)

  fun build(): String {
    if (!::table.isInitialized) {
      throw IllegalStateException("Failed to build an sql select - target table is undefined")
    }
    return toString()
  }

  override fun toString(): String {
    val columnsToFetch =
      if (cols.isEmpty()) {
        "*"
      } else {
        cols.joinToString(", ")
      }

    return if (::condition.isInitialized) {
      val conStr = condition.toString()
      // remove out side ()
      val prettyStr = if (condition.conditions.size > 1) conStr.substring(1, conStr.lastIndex) else conStr
      "select $columnsToFetch from $table where $prettyStr"
    } else {
      "select $columnsToFetch from $table"
    }
  }

  // Defining select clause
  private val cols = mutableListOf<String>()

  fun select(vararg colums: Col<*>) {
    if (colums.isEmpty()) {
      throw IllegalArgumentException("At least one column should be defined")
    }
    if (this.cols.isNotEmpty()) {
      throw IllegalStateException(
        "Detected an attempt to re-define columns to fetch. Current columns list: ${this.cols}, new columns list: $cols"
      )
    }
    this.cols.addAll(colums.map { "${it.table}.${it.name}" })
  }

  // Defininig table
  lateinit var table: String

  fun from(table: String) {
    this.table = table
  }

  // Defining where clause
  private lateinit var condition: CompositeCondition

  fun where(init: CompositeCondition.() -> Unit) {
    this.condition = And().apply(init)
//    condition.table = this.table
  }

}

fun query(init: SqlSelectBuilder.() -> Unit) = SqlSelectBuilder().apply(init)
