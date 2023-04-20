import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.util.*

data class Student(
    val name: String,
    val group: String,
    val id: UUID? = null
)

object StudentTable : IdTable<UUID>("students") {
    override val id = uuid("id").entityId().uniqueIndex()
    val name = varchar("name", 512)
    val group = varchar("group", 10).nullable()
}

fun UpdateBuilder<Number>.save(student: Student) {
    this[StudentTable.id] = student.id ?: UUID.randomUUID()
    this[StudentTable.name] = student.name
    this[StudentTable.group] = student.group
}

fun ResultRow.toStudent(): Student = Student(
    id = this[StudentTable.id].value,
    name = this[StudentTable.name],
    group = this[StudentTable.group] ?: ""
)
