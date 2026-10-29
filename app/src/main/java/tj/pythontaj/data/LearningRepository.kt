package tj.pythontaj.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Ягона ҷои дастрасӣ ба маълумоти таълимии маҳаллӣ. */
class LearningRepository(private val db: AppDatabase) {
    private val dao = db.dao()

    fun completedLessonIds(): Flow<Set<String>> = dao.completedLessonIds().map { it.toSet() }
    fun passedExerciseIds(): Flow<Set<String>> = dao.passedExerciseIds().map { it.toSet() }
    fun bookmarkIds(): Flow<Set<String>> = dao.bookmarkIds().map { it.toSet() }
    fun studyDays(): Flow<Set<Long>> = dao.studyDays().map { it.toSet() }
    fun attemptsFor(exerciseId: String) = dao.attemptsFor(exerciseId)
    fun snippets() = dao.snippets()
    fun quizResults() = dao.quizResults()

    suspend fun passedExerciseIdsOnce(): Set<String> = dao.passedExerciseIdsOnce().toSet()

    suspend fun recordAttempt(exerciseId: String, lessonId: String, passed: Boolean, code: String, epochDay: Long) {
        val now = System.currentTimeMillis()
        dao.insertAttempt(ExerciseAttemptEntity(exerciseId = exerciseId, lessonId = lessonId, passed = passed, code = code, createdAt = now))
        dao.addStudyDay(StudyDayEntity(epochDay))
    }

    suspend fun markLessonCompleted(lessonId: String, epochDay: Long) {
        dao.upsertLesson(LessonProgressEntity(lessonId, System.currentTimeMillis()))
        dao.addStudyDay(StudyDayEntity(epochDay))
    }

    suspend fun recordQuiz(lessonId: String, score: Int, total: Int, epochDay: Long) {
        dao.insertQuiz(QuizResultEntity(lessonId = lessonId, score = score, total = total, createdAt = System.currentTimeMillis()))
        dao.addStudyDay(StudyDayEntity(epochDay))
    }

    suspend fun setBookmark(lessonId: String, on: Boolean) {
        if (on) dao.addBookmark(BookmarkEntity(lessonId, System.currentTimeMillis())) else dao.removeBookmark(lessonId)
    }

    suspend fun saveSnippet(title: String, code: String) = dao.insertSnippet(SnippetEntity(title = title, code = code, createdAt = System.currentTimeMillis()))
    suspend fun deleteSnippet(id: Long) = dao.deleteSnippet(id)

    /** Тамоми пешрафтро пок мекунад. Танҳо пас аз тасдиқи корбар даъват шавад. */
    suspend fun clearAll() = withContext(Dispatchers.IO) { db.clearAllTables() }
}
