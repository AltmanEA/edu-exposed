import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun main() {
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    transaction {
        listOf(StudentTable, CourseTable, GradeTable).map {
            SchemaUtils.create(it)
        }
        val students = listOf("Penny", "Amy").map { Student(it, "Girls") } +
                listOf("Sheldon", "Leonard", "Howard", "Raj").map { Student(it, "Boys") }
        val courses = listOf("Math", "Phys", "History").map {
            Course(it)
        }

        val dbStudents = students.map { student ->
            StudentDao.new(UUID.randomUUID()) {
                name = student.name
                group = student.group
            }
        }
        val dbCourses = courses.map { course ->
            CourseDao.new(UUID.randomUUID()) {
                name = course.name
            }
        }

        println("CТУДЕНТЫ: $dbStudents")
        println("КУРСЫ: $dbCourses")

        val dbGrades = dbCourses.map { courseDao ->
            dbStudents.map { studentDao ->
                GradeDao.new(UUID.randomUUID()) {
                    student = studentDao
                    course = courseDao
                }
            }
        }
        println(CourseDao.all().map { it.fullString() })
    }
    transaction {
        println("\n\nNO EAGER LOAD\n\n")
        CourseDao.all().map { it.fullString() }
    }
    transaction {
        println("\n\nEAGER LOAD\n\n")
        CourseDao.all().with(CourseDao::grades).map { it.fullString() }
    }
    transaction {
        println("\n\nVERY EAGER LOAD\n\n")
        CourseDao.all().with(CourseDao::grades, GradeDao::student).map { it.fullString() }
    }
}