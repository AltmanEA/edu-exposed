import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(StudentTable)

        val students = listOf("Penny", "Amy").map { Student(it, "Girls") } +
                listOf("Sheldon", "Leonard", "Howard", "Raj").map { Student(it, "Boys") }

        // create
        students.map { student ->
            StudentTable.insert { insertStatement: InsertStatement<Number> ->
                insertStatement.save(student)
            }
        }

        // read
        val allQuery: Query = StudentTable.selectAll()
        println("Students: ${allQuery.map { it.toStudent() }}")

        val boysQuery = StudentTable.select {
            StudentTable.group eq "Boys"
        }
        println("Boys: ${boysQuery.map { it.toStudent() }}")

        // update
        val amy = StudentTable.select {
            StudentTable.name eq "Amy"
        }.first().toStudent()

        StudentTable.update({
            StudentTable.name eq "Amy"
        }) {
            it.save(amy.copy(name = "Amy Farrah Fowler"))
        }
        println("Students after update: ${StudentTable.selectAll().map { it.toStudent() }}")

        // delete
        StudentTable.deleteWhere {
            group eq "Boys"
        }
        println("Students after delete: ${StudentTable.selectAll().map { it.toStudent() }}")
    }
}