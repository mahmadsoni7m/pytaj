package tj.pythontaj.content

import org.json.JSONObject
import tj.pythontaj.domain.*

data class Curriculum(val modules: List<CourseModule>, val lessons: List<Lesson>) {
    private val byId = lessons.associateBy { it.id }

    fun lesson(id: String): Lesson? = byId[id]
    fun lessonsOf(moduleId: String): List<Lesson> = lessons.filter { it.moduleId == moduleId }
    fun exercise(id: String): Exercise? = lessons.firstNotNullOfOrNull { l -> l.exercises.firstOrNull { it.id == id } }
    fun lessonOfExercise(exerciseId: String): Lesson? = lessons.firstOrNull { l -> l.exercises.any { it.id == exerciseId } }
    val exerciseCount: Int get() = lessons.sumOf { it.exercises.size }

    /** Ҷустуҷӯ аз рӯи сарлавҳа ва матни дарс; филтр аз рӯи модул ва душворӣ ихтиёрӣ аст. */
    fun search(query: String, moduleId: String? = null, difficulty: Int? = null): List<Lesson> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return lessons.filter { l ->
            (moduleId == null || l.moduleId == moduleId) &&
                (difficulty == null || l.difficulty == difficulty) &&
                (l.title.lowercase().contains(q) ||
                    l.introduction.lowercase().contains(q) ||
                    l.summary.lowercase().contains(q) ||
                    l.sections.any { it.heading.lowercase().contains(q) || it.body.lowercase().contains(q) })
        }
    }
}

/** [readText] мазмунро аз роҳи нисбӣ (масалан "curriculum/modules.json") мехонад. */
class ContentLoader(private val readText: (String) -> String) {
    fun load(): Curriculum {
        val modules = LessonParser.parseModules(readText("curriculum/modules.json"))
        val manifest = try {
            JSONObject(readText("curriculum/manifest.json")).getJSONArray("lessons")
        } catch (e: org.json.JSONException) {
            throw ContentException("manifest.json: ${e.message}", e)
        }
        val lessons = List(manifest.length()) {
            val file = manifest.getString(it)
            LessonParser.parseLesson(readText("curriculum/lessons/$file"), file)
        }
        return Curriculum(modules, lessons).also { CurriculumValidator.validate(it) }
    }
}

object CurriculumValidator {
    fun validate(c: Curriculum) {
        fun fail(msg: String): Nothing = throw ContentException(msg)

        val moduleIds = c.modules.map { it.id }
        if (moduleIds.toSet().size != moduleIds.size) fail("ID-и модулҳо такрор шудаанд")

        val lessonIds = c.lessons.map { it.id }
        if (lessonIds.toSet().size != lessonIds.size) fail("ID-и дарсҳо такрор шудаанд")

        val position = lessonIds.withIndex().associate { it.value to it.index }
        val exerciseIds = c.lessons.flatMap { l -> l.exercises.map { it.id } }
        if (exerciseIds.toSet().size != exerciseIds.size) fail("ID-и машқҳо такрор шудаанд")
        val quizIds = c.lessons.flatMap { l -> l.quiz.map { it.id } }
        if (quizIds.toSet().size != quizIds.size) fail("ID-и саволҳои quiz такрор шудаанд")

        for (l in c.lessons) {
            if (l.moduleId !in moduleIds) fail("${l.id}: модули ${l.moduleId} вуҷуд надорад")
            if (l.title.isBlank() || l.introduction.isBlank() || l.summary.isBlank() || l.completionCriteria.isBlank()) {
                fail("${l.id}: майдонҳои холӣ")
            }
            if (l.objectives.isEmpty() || l.sections.isEmpty() || l.examples.isEmpty()) fail("${l.id}: мазмуни нокифоя")
            if (l.difficulty !in 1..5) fail("${l.id}: душворӣ бояд 1..5 бошад")
            for (p in l.prerequisites) {
                val pp = position[p] ?: fail("${l.id}: пешшарти $p вуҷуд надорад")
                if (pp >= position.getValue(l.id)) fail("${l.id}: пешшарти $p бояд пештар ояд")
            }
            for (e in l.exercises) {
                when (e.kind) {
                    ExerciseKind.OUTPUT -> if (e.expectedOutput.isBlank()) fail("${e.id}: expectedOutput холӣ аст")
                    ExerciseKind.FUNCTION -> if (e.tests.isEmpty()) fail("${e.id}: tests холӣ аст")
                }
                if (e.solution.isBlank()) fail("${e.id}: solution холӣ аст")
            }
            for (q in l.quiz) {
                if (q.options.size < 2) fail("${q.id}: камаш 2 вариант лозим")
                if (q.answerIndex !in q.options.indices) fail("${q.id}: answerIndex берун аз ҳудуд")
            }
        }
    }
}
