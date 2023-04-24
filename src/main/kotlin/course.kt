import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.LocalDate
import java.util.*

data class Grade(
    val studentId: UUID,
    val value: Int? = null,
    val date: LocalDate? = null,
)

data class Course(
    val name: String,
    val grades: List<Grade> = emptyList(),
    val id: UUID? = UUID.randomUUID()
)

//region Tables
object GradeTable : IdTable<UUID>("grades") {
    override val id = uuid("id").entityId().uniqueIndex()
    val value = integer("value").nullable()
    val date = date("date").nullable()
    val course = reference("course", CourseTable)
    val student = reference("students", StudentTable)
}

object CourseTable : IdTable<UUID>("courses") {
    override val id = uuid("id").entityId().uniqueIndex()
    val name = varchar("name", 512)
}
//endregion

//region DSL
fun UpdateBuilder<Number>.save(course: Course) {
    this[CourseTable.id] = course.id ?: UUID.randomUUID()
    this[CourseTable.name] = course.name
}

fun ResultRow.toCourse() = Course(
    id = this[CourseTable.id].value,
    name = this[CourseTable.name]
)

fun Course.addGrade(grade: Grade) {
    check(id != null)
    GradeTable.insert {
        it[id] = UUID.randomUUID()
        it[value] = grade.value
        it[date] = grade.date
        it[course] = this@addGrade.id
        it[student] = grade.studentId
    }
}

fun readCourse(id: UUID? = null): List<Course> {
    val query = (CourseTable innerJoin GradeTable)
        .selectAll()

    id?.let {
        query.andWhere {
            CourseTable.id eq it
        }
    }
    return query.groupBy { it[CourseTable.id] }
        .map {
            val grades = it.value.map {
                Grade(
                    it[GradeTable.student].value,
                    it[GradeTable.value],
                    it[GradeTable.date]
                )
            }
            Course(
                it.value[0][CourseTable.name],
                grades,
                it.key.value
            )
        }
}

fun setGrade(courseId: UUID, studentId: UUID, newValue: Int) {
    GradeTable.update({
        GradeTable.course eq courseId and
                (GradeTable.student eq studentId)
    }) {
        it[value] = newValue
        it[date] = LocalDate.now()
    }
}
//endregion

class GradeDao(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, GradeDao>(GradeTable)

    var student by StudentDao referencedOn GradeTable.student
    var course by CourseDao referencedOn GradeTable.course
    var value by GradeTable.value
    val date by GradeTable.date

    override fun toString() = "Студент $student - Курс $course - $value - $date"
}

class CourseDao(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, CourseDao>(CourseTable)

    var name by CourseTable.name
    val grades by GradeDao referrersOn GradeTable.course

    override fun toString() =
        "Курс $name"

    fun fullString() =
        toString() + "\t" + grades.joinToString { it.toString() }
}