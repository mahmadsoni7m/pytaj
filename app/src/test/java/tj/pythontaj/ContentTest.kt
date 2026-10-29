package tj.pythontaj

import org.junit.Assert.*
import org.junit.Test
import tj.pythontaj.content.*
import java.io.File

class ContentTest {
    private fun realLoader() = ContentLoader { File("src/main/assets/$it").readText(Charsets.UTF_8) }

    @Test fun realCurriculumLoadsAndValidates() {
        val c = realLoader().load()
        assertEquals(18, c.modules.size)
        assertTrue(c.lessons.size >= 6)
        assertTrue(c.exerciseCount >= 12)
    }

    @Test fun searchFindsByTitleAndContent() {
        val c = realLoader().load()
        assertTrue(c.search("print").isNotEmpty())
        assertTrue(c.search("Шарҳҳо").any { it.id == "m01_l05" })
        assertTrue(c.search("zzzz-ёфт-намешавад").isEmpty())
        assertTrue(c.search("print", moduleId = "m02").isEmpty())
    }

    @Test fun malformedLessonJsonThrowsContentException() {
        assertThrows(ContentException::class.java) { LessonParser.parseLesson("{ not json", "bad.json") }
        assertThrows(ContentException::class.java) { LessonParser.parseLesson("{\"id\":\"x\"}", "bad.json") }
    }

    @Test fun duplicateLessonIdsAreRejected() {
        val c = Curriculum(listOf(module()), listOf(lesson("a"), lesson("a")))
        assertThrows(ContentException::class.java) { CurriculumValidator.validate(c) }
    }

    @Test fun unknownModuleIsRejected() {
        val c = Curriculum(listOf(module()), listOf(lesson("a", moduleId = "zzz")))
        assertThrows(ContentException::class.java) { CurriculumValidator.validate(c) }
    }

    @Test fun prerequisiteMustExistAndComeEarlier() {
        val missing = Curriculum(listOf(module()), listOf(lesson("a", prerequisites = listOf("nope"))))
        assertThrows(ContentException::class.java) { CurriculumValidator.validate(missing) }
        val later = Curriculum(listOf(module()), listOf(lesson("a", prerequisites = listOf("b")), lesson("b")))
        assertThrows(ContentException::class.java) { CurriculumValidator.validate(later) }
    }

    @Test fun exerciseWithoutEvaluationDataIsRejected() {
        val bad = lesson("a", exercises = listOf(exercise(expected = "")))
        assertThrows(ContentException::class.java) { CurriculumValidator.validate(Curriculum(listOf(module()), listOf(bad))) }
    }

    @Test fun validCurriculumPasses() {
        CurriculumValidator.validate(Curriculum(listOf(module()), listOf(lesson("a"), lesson("b", prerequisites = listOf("a")))))
    }

    private fun module() = tj.pythontaj.domain.CourseModule("m01", 1, "t", "d")
}
