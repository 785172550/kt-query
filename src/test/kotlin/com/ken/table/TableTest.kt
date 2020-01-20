package com.ken.table

import com.ken.builder.SqlSelectBuilder
import com.ken.builder.query
import com.ken.data.BaseTable
import org.junit.Assert
import org.junit.Test

/**
 * @Author hw83770
 * @Date 17:39 2020/1/20
 *
 */

object Coutry : BaseTable("coutry") {
  val id = long("id")
  val name = varchar("name")
  val nickName = varchar("nick_name")
  val createTime = date("create_time")
}

object Province : BaseTable("province") {
  val id = long("id")
  val name = varchar("name")
  val country = varchar("country_name")
}

class TableTest {
  fun doTest(expected: String, sql: SqlSelectBuilder.() -> Unit) {
    Assert.assertEquals(query(sql).build(), expected)
  }

  @Test
  fun `test table schema define`() {
    val sqlBuilder: SqlSelectBuilder.() -> Unit = {
      select(Province.id, Province.name)
      from(table = Province.tblName)
      where {
        exist(query {
          select(Province.name)
          from(Province.tblName)
          where {
            Province.country eq Coutry.name
            Coutry.name eq "China"
          }
        })

      }
    }
    println(query(sqlBuilder).build())
    doTest("select province.id, province.name from province where exist " +
      "(select province.name from province where province.country_name = coutry.name and coutry.name = 'China')", sqlBuilder)

    println(Province)
  }
}
