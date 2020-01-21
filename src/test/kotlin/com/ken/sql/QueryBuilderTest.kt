package com.ken.sql

import com.ken.builder.SqlSelectBuilder
import com.ken.builder.query
import org.junit.Assert
import org.junit.Test

/**
 * @Author hw83770
 * @Date 21:57 2020/1/19
 *
 */

class QueryBuilderTest {
  private fun doTest(expected: String, sql: SqlSelectBuilder.() -> Unit) {
    Assert.assertEquals(query(sql).build(), expected)
  }

  @Test
  fun `when no columns are specified then star is used`() {
    doTest("select * from table1") {
      from(table = "table1")
    }
  }

  @Test
  fun `when no condition is specified then correct query is built`() {
    doTest("select col1, col2 from table1") {
      select(col<String>(name = "col1"), col<String>(name = "col2"))
      from(table = "table1")
    }

    println(
      query {
        select(col<String>(name = "col1"), col<String>(name = "col2"))
        from(table = "table1")
      }
    )
  }

  @Test
  fun `when AND OR conditon is specified then correct query is built `() {
    var ifCondition = 2
    val sqlBuilder: SqlSelectBuilder.() -> Unit = {
      from(table = "table1")
      where {
        or {
          and {
            col<String>(name = "col1") ne "test"
            col<String>(name = "col2").isNull()
            col<Int>(name = "col3") eq 22
          }
          and {
            col<Int>("col4") notIn setOf(99, 55)
            if (ifCondition == 1) and { col<String>("col5").`in`("tests", "hello") }
          }
        }
      }
    }
    println(query(sqlBuilder).build())
    doTest("select * from table1 where (col1 != 'test' and col2 is null and col3 = 22) or col4 not in (99, 55)", sqlBuilder)

    // change condition
    ifCondition = 1
    println(query(sqlBuilder).build())
    doTest("select * from table1 where (col1 != 'test' and col2 is null and col3 = 22) or (col4 not in (99, 55) and col5 in ('tests', 'hello'))",
      sqlBuilder)
  }

  @Test
  fun `test sub query in where clause`() {
    val sqlBuilder: SqlSelectBuilder.() -> Unit = {
      from(table = "table1")
      where {
        or {
          col<String>(name = "col2").isNotNull()
          col<String>(name = "col1").isNotNull()
        }
        col<Int>(name = "col3") `in` query {
          select(col<Int>("col_A", table = "table2"))
          from("table2")
          where { col<Int>("col_B") eq col<Int>(name = "col3", table = "table1") }
        }
      }
    }
    println(query(sqlBuilder).build())
//    doTest("select * from table1 where (table1.col2 is not null or table1.col1 is not null) " +
//      "and table1.col3 in (select table2.col_A from table2 where table2.col_B = table1.col3)", sqlBuilder)

    val sqlBuilder2: SqlSelectBuilder.() -> Unit = {
      from(table = "table1")
      where {
        exists {
          select(col<Int>("col_A", table = "table2"))
          from("table2")
          where { col<Int>("col_B") eq 1 }
        }
      }
    }
    println(query(sqlBuilder2).build())
//    doTest("select * from table1 where exist (select table2.col_A from table2 where table2.col_B = 1)", sqlBuilder2)
  }

}
