import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun main() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
    transaction {
        SchemaUtils.create(StudentTable)

        val students = listOf("Penny", "Amy").map { Student(it, "Girls") } +
                listOf("Sheldon", "Leonard", "Howard", "Raj").map { Student(it, "Boys") }

        // create
        val dbStudents = students.map { student ->
            StudentDao.new(UUID.randomUUID()) {
                name = student.name
                group = student.group
            }
        }

        println(dbStudents)

        // read
        println(StudentDao.all().toList())
        println(StudentDao.find{
            StudentTable.group eq "Boys"
        }.toList())

        // update

        val amy = StudentDao.find{
            StudentTable.name eq "Amy"
        }.first()
        amy.name = "Amy Farrah Fowler"

        // delete
        StudentDao.find{
            StudentTable.group eq "Boys"
        }.map { it.delete() }

        println(StudentDao.all().toList())
    }
}