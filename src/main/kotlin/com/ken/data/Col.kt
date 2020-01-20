package com.ken.data

/**
 * @Author hw83770
 * @Date 10:43 2020/1/20
 *
 */
data class Col<T>(
  val name: String,
  var table: String = ""
) {
  val type: T = Any() as T
//  override fun toString(): String {
//    return "col_name: ${name}, type ${type}"
//  }
}
