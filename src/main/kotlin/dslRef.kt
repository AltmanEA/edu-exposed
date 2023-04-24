import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
    transaction {
        listOf(StudentTable, CourseTable, GradeTable).map {
            SchemaUtils.create(it)
        }

        val students = listOf("Penny", "Amy").map { Student(it, "Girls") } +
                listOf("Sheldon", "Leonard", "Howard", "Raj").map { Student(it, "Boys") }
        StudentTable.batchInsert(students) {
            save(it)
        }
        val dbStudents = StudentTable.selectAll().map { it.toStudent() }
        val courses = listOf("Math", "Phys", "History").map {
            Course(it)
        }
        CourseTable.batchInsert(courses) {
            save(it)
        }
        val dbCourse = CourseTable.selectAll().map { it.toCourse() }

        dbCourse.map { course ->
            dbStudents.map { student ->
                check(student.id != null)
                course.addGrade(Grade(student.id))
            }
        }
        println(readCourse())

        val pennyId = dbStudents.find { it.name == "Penny" }?.id!!
        val mathId = dbCourse.find { it.name == "Math" }?.id!!
        setGrade(mathId, pennyId, 5)

        println(readCourse(mathId))
    }
}

