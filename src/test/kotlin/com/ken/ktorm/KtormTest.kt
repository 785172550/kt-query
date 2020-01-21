package com.ken.ktorm

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.schema.*


/**
 * @Author hw83770
 * @Date 10:05 2020/1/21
 *
 */

/*
create table t_department (
    id int NOT NULL PRIMARY KEY,
    name varchar(255),
    location varchar(255),
);

create table t_employee (
    id int NOT NULL PRIMARY KEY,
    name varchar(255),
    job varchar(255),
    manager_id int,
    hire_date date,
    salary long,
    department_id int,
    CONSTRAINT FK_t_employee_t_department FOREIGN KEY (department_id) REFERENCES t_department(id)
);

*/

object DEPARTMENT : Table<Nothing>("t_department") {
  val id by int("id").primaryKey()
  val name by varchar("name")
  val location by varchar("location")
}

object EMPLOYEE : Table<Nothing>("t_employee") {
  val id by int("id").primaryKey()
  val name by varchar("name")
  val job by varchar("job")
  val managerId by int("manager_id")
  val hireDate by date("hire_date")
  val salary by long("salary")
  val departmentId by int("department_id")
}

fun main() {
  Database.connect(
    url = "jdbc:h2:tcp://localhost/~/Documents/db/kt_rest",
    driver = "org.h2.Driver",
    user = "sa",
    password = ""
  )

//  prepareData()

//  for (row in Employees.select()) {
//    println(row[Employees.name])
//  }

  EMPLOYEE
    .select()
    .where {
      exists(DEPARTMENT
        .select(DEPARTMENT.name)
        .where {
          (DEPARTMENT.id eq EMPLOYEE.departmentId) and (DEPARTMENT.name eq "aspen")
        }
      )
    }.forEach { println(it[EMPLOYEE.name]) }

//  Employees.leftJoin()

}


fun prepareData() {
  DEPARTMENT.insert {
    it.id to 1
    it.name to "ocean"
    it.location to "shanghai"
  }

  DEPARTMENT.insert {
    it.id to 2
    it.name to "aspen"
    it.location to "shanghai"
  }

  EMPLOYEE.insert {
    it.id to 1
    it.name to "ken"
    it.job to "Coder"
    it.departmentId to 1
  }

  EMPLOYEE.insert {
    it.id to 2
    it.name to "rayman"
    it.job to "Coder"
    it.departmentId to 2
  }

  EMPLOYEE.insert {
    it.id to 3
    it.name to "vivian"
    it.job to "BA"
    it.departmentId to 1
  }

}