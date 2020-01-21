package com.ken.table

import com.ken.builder.SqlSelectBuilder
import com.ken.builder.query
import com.ken.data.BaseTable
import me.liuwj.ktorm.database.Database
import org.junit.Assert
import org.junit.Test
import java.sql.ResultSet

/**
 * @Author hw83770
 * @Date 17:39 2020/1/20
 *
 */

object DEPARTMENT : BaseTable("t_department") {
  val id = int("id")
  val name = varchar("name")
  val location = varchar("location")
}

object EMPLOYEE : BaseTable("t_employee") {
  val id = int("id")
  val name = varchar("name")
  val job = varchar("job")
  val managerId = int("manager_id")
  val hireDate = date("hire_date")
  val salary = long("salary")
  val departmentId = int("department_id")
}

class TableTest {
  fun doTest(expected: String, sql: SqlSelectBuilder.() -> Unit) {
    Assert.assertEquals(query(sql).build(), expected)
  }

  @Test
  fun `test table schema define`() {
    val con = Database.connect(
      url = "jdbc:h2:tcp://localhost/~/Documents/db/kt_rest",
      driver = "org.h2.Driver",
      user = "sa",
      password = ""
    )

    val builder = EMPLOYEE.query {
      select(EMPLOYEE.id, EMPLOYEE.name)
      where {
        exists(DEPARTMENT.query {
          where {
            DEPARTMENT.id eq EMPLOYEE.departmentId
            DEPARTMENT.name eq "ocean"
          }
        })
      }
    }
    println(builder.build())

    con.useConnection {
      val stat = it.createStatement()
      stat.execute(builder.build())
      for (row in stat.resultSet) {
        println(row.getString("name"))
      }
    }
  }
}

operator fun ResultSet.iterator() = object : Iterator<ResultSet> {
  private val rs = this@iterator
  private var hasNext: Boolean = false

  override fun next(): ResultSet = if (hasNext) rs else throw NoSuchElementException()
  override fun hasNext(): Boolean = run {
    hasNext = rs.next()
    hasNext
  }
}
