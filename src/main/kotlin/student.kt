import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.util.*

data class Student(
    val name: String,
    val group: String,
    val id: UUID? = null
)

//region DSL
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
//endregion

//region DAO
class StudentDao(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, StudentDao>(StudentTable)

    var name by StudentTable.name
    var group by StudentTable.group

    override fun toString() = "$name г. $group ($id)"
}
//endregion
