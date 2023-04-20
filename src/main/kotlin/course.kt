import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.LocalDate
import java.util.*

data class Grade(
    val studentId: UUID,
//    val studentName: String,
    val value: Int? = null,
    val date: LocalDate? = null,
)

data class Course(
    val name: String,
    val grades: List<Grade> = emptyList(),
    val id: UUID? = UUID.randomUUID()
)

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