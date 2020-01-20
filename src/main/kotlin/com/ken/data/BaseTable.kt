package com.ken.data

import java.time.LocalDate
import java.util.LinkedHashMap

/**
 * @Author hw83770
 * @Date 17:25 2020/1/20
 *
 */
open class BaseTable(val tblName: String) : Table {
  val _columns = LinkedHashMap<String, Col<*>>()
    get() = field

  fun registerColumn(colums: Col<*>) {
    if (colums.name in _columns.keys) {
      throw IllegalArgumentException("Duplicate column name: ${colums.name}")
    }
    _columns[colums.name] = colums
  }

  inline fun <reified T> column(name: String): Col<T> {
    val col = Col<T>(name, tblName)
    this.registerColumn(colums = col)
    return col
  }

  fun varchar(name: String) = column<String>(name)
  fun int(name: String) = column<Int>(name)
  fun date(name: String) = column<LocalDate>(name)
  fun long(name: String) = column<Long>(name)
  fun float(name: String) = column<Float>(name)
  fun double(name: String) = column<Double>(name)

  override fun toString(): String {
    return _columns.toString()
  }
}
